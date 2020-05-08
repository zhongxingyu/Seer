 package com.epam.urlchopper.service.simple;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.annotation.PostConstruct;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.io.support.PropertiesLoaderUtils;
 import org.springframework.stereotype.Service;
 
 import com.epam.urlchopper.domain.OriginalUrl;
 import com.epam.urlchopper.domain.ShortUrl;
 import com.epam.urlchopper.domain.User;
 import com.epam.urlchopper.repository.UrlRepository;
 import com.epam.urlchopper.repository.UserRepository;
 import com.epam.urlchopper.service.GeneratorService;
 
 /**
  * .
  *
  */
 @Service
 public class SimpleGeneratorService implements GeneratorService {
 
     private static final long DEFAULT_LIFESPAN = 86400000L;
 
     private static final int DEFAULT_SHORTURL_LENGTH = 6;
 
     private static final int DIGITS_ASCII_END = 57;
 
     private static final int DIGITS_ASCII_START = 48;
 
     private static final int LOWER_CASE_ASCII_END = 122;
 
     private static final int LOWER_CASE_ASCII_START = 97;
 
     private static final int MULTIPLY = 1000;
 
     private Logger logger = LoggerFactory.getLogger(SimpleGeneratorService.class);
 
     private Integer generateUrlLength = DEFAULT_SHORTURL_LENGTH;
 
     private Long shortUrlLifeSpan = DEFAULT_LIFESPAN;
 
     private Properties properties;
 
     private List<Character> characters = new ArrayList<Character>();
 
     @Autowired
     private UrlRepository urlRepository;
 
     @Autowired
     private UserRepository userRepository;
 
     /**
      * The characters are initialized.
      */
     @PostConstruct
     private void createCharacterList() {
         for (int i = LOWER_CASE_ASCII_START; i <= LOWER_CASE_ASCII_END; i++) {
             characters.add((char) i);
         }
         for (int i = DIGITS_ASCII_START; i <= DIGITS_ASCII_END; i++) {
             characters.add((char) i);
         }
     }
 
     @PostConstruct
     private void loadProperties() {
         try {
             properties = PropertiesLoaderUtils.loadAllProperties("config.properties");
             generateUrlLength = Integer.valueOf(properties.getProperty("shorturls.generateUrlLength"));
             shortUrlLifeSpan = Long.valueOf(properties.getProperty("shorturls.lifespan"));
         } catch (NumberFormatException e) {
             logger.error(e.getMessage());
         } catch (IOException e) {
             logger.error(e.getMessage());
         }
     }
 
     @Override
     public String generate(String requestedOriginalUrl, Long userId) {
         String generatedShortUrlPostfix = "";
         OriginalUrl originalUrl = urlRepository.findOriginalUrl(convertToValidUrl(requestedOriginalUrl));
         User user = userRepository.findUser(userId);
         if (shortUrlExistsForOriginal(originalUrl)) {
             originalUrl.increaseReferenceCount();
             generatedShortUrlPostfix = originalUrl.getShortUrl().getShortUrlPostfix();
             urlRepository.lengthenLifespan(originalUrl.getShortUrl(), calculateLifeSpanEnd());
             urlRepository.mergeOriginalUrl(originalUrl);
             user.addShortUrl(originalUrl.getShortUrl());
         } else if (originalUrlIsExistWithoutShortUrl(originalUrl)) {
             originalUrl.increaseReferenceCount();
             generatedShortUrlPostfix = generateUniqueShortUrlPostfix();
             urlRepository.mergeOriginalUrl(originalUrl);
             createShortUrl(originalUrl, generatedShortUrlPostfix);
             user.addShortUrl(originalUrl.getShortUrl());
         } else {
             generatedShortUrlPostfix = generateUniqueShortUrlPostfix();
             createOriginalUrl(requestedOriginalUrl);
             ShortUrl shortUrl = createShortUrl(requestedOriginalUrl, generatedShortUrlPostfix);
             user.addShortUrl(shortUrl);
         }
         userRepository.update(user);
         return generatedShortUrlPostfix;
     }
 
     private boolean shortUrlExistsForOriginal(OriginalUrl originalUrl) {
         return originalUrl != null && originalUrl.getShortUrl() != null;
     }
 
     private boolean originalUrlIsExistWithoutShortUrl(OriginalUrl existUrl) {
         return existUrl != null && existUrl.getShortUrl() == null;
     }
 
     private OriginalUrl createOriginalUrl(String originalUrl) {
         return urlRepository.createOriginalUrl(new OriginalUrl(convertToValidUrl(originalUrl), 1));
     }
 
     private ShortUrl createShortUrl(String originalUrl, String generatedShortUrl) {
         OriginalUrl tmp = new OriginalUrl(convertToValidUrl(originalUrl), 1);
         return createShortUrl(tmp, generatedShortUrl);
     }
 
     private ShortUrl createShortUrl(OriginalUrl originalUrl, String generatedShortUrl) {
         ShortUrl url = new ShortUrl(generatedShortUrl, originalUrl, calculateLifeSpanEnd());
         originalUrl.setShortUrl(url);
         return urlRepository.createShortUrl(url);
     }
 
     private long calculateLifeSpanEnd() {
         return new Date().getTime() + shortUrlLifeSpan;
     }
 
     private String generateUniqueShortUrlPostfix() {
         String ret = "";
         ret = generateShortUrl();
         while (urlAlreadyExists(ret)) {
             ret = generateShortUrl();
         }
         return ret;
     }
 
     private String convertToValidUrl(String originalUrl) {
         String ret = originalUrl;
         if (!originalUrl.startsWith("http://")) {
             ret = "http://" + originalUrl;
         }
         return ret;
     }
 
     private String generateShortUrl() {
         String ret = "";
 
         for (int i = 0; i < generateUrlLength; i++) {
             double rnd = Math.random();
             int index = (int) ((rnd * MULTIPLY) % characters.size());
             ret += characters.get(index);
         }
         return ret;
     }
 
     private boolean urlAlreadyExists(String url) {
 
         Boolean ret = true;
 
         if (urlRepository.findShortUrl(url) == null) {
             ret = false;
         }
 
         return ret;
     }
 
     @Override
    public String findOriginalUrl(String url) {       
         return urlRepository.findOriginalUrl(url).getUrl();
     }
 }
