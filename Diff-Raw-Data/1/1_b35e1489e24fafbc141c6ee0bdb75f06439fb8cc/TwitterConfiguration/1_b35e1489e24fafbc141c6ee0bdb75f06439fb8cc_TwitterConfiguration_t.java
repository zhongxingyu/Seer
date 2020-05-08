 package de.hypoport.twitterwall.config;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 @Component
 public class TwitterConfiguration {
 
   static Logger logger = LoggerFactory.getLogger(TwitterConfiguration.class);
 
   private String consumerKey;
   private String consumerSecret;
 
   public TwitterConfiguration() {
     this.consumerKey = System.getProperty("consumerKey", System.getenv("consumerKey"));
     this.consumerSecret = System.getProperty("consumerSecret", System.getenv("consumerSecret"));
   }
 
   public boolean isFullyConfigured() {
     boolean fullyConfigured = consumerKey != null && consumerSecret != null && !consumerKey.isEmpty() && !consumerSecret.isEmpty();
     if (!fullyConfigured) {
       logger.warn("No configuration for Twitter found.\n" +
           "The search service is disabled and will only return mock data!\n" +
           "Please provide 'consumerKey' and 'consumerSecret' variables as environment variable OR as system property.");
     } else {
       logger.info("Found Twitter access tokes (consumerKey and consumerSecret).");
     }
     return fullyConfigured;
   }
 
   public String getConsumerKey() {
     return consumerKey;
   }
 
   public String getConsumerSecret() {
     return consumerSecret;
   }
 }
