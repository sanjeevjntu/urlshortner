package com.fordav.urlshortner;

import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerController {

    private final StringRedisTemplate redisTemplate;

    @GetMapping("/v1/{id}")
    public String getLongUrl(@PathVariable String id) {
        String longUrl = redisTemplate.opsForValue().get(id);
        log.info("Getting longUrl: {}", longUrl);
        if (longUrl == null) {
            throw new RuntimeException("There url not found for the given id");
        }
        return longUrl;
    }

    @PostMapping(value = "/v1/createShortUrl")
    public String createShortUrl(@RequestBody String longUrl) {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});

        if (urlValidator.isValid(longUrl)) {
            //Use murmur Hash3 to generate the short URL
            String id = Hashing.murmur3_32()
                    .hashString(longUrl, StandardCharsets.UTF_8)
                    .toString();
            log.info("ShortUrl: {}", id);
            redisTemplate.opsForValue().set(id, longUrl);

            return id;
        }
        throw new IllegalArgumentException(" long url is not valid");
    }
}
