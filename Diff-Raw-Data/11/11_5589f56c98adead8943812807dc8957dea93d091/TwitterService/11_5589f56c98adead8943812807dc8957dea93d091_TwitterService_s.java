 package de.hypoport.twitterwall.twitter;
 
 import de.hypoport.twitterwall.config.TwitterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 import twitter4j.*;
 import twitter4j.conf.Configuration;
 import twitter4j.conf.ConfigurationBuilder;
 
 import javax.inject.Inject;
 
 @Component
 class TwitterService {
 
  static Logger logger = LoggerFactory.getLogger(TwitterService.class);
 
   @Inject
   TwitterConfiguration configuration;
 
   private Twitter twitter;
 
   public QueryResult searchTweets(Query query) throws TwitterException {
     try {
       return doSearch(query);
     } catch (TwitterException e) {
      logger.error("Error while searching on Twitter.",e);
       twitter = null;
     }
     return null;
   }
 
   private QueryResult doSearch(Query query) throws TwitterException {
     return getTwitter().search(query);
   }
 
   private Twitter getTwitter() throws TwitterException {
     if (twitter == null) {
       twitter = new TwitterFactory(configure()).getInstance();
       twitter.getOAuth2Token();
     }
     return twitter;
   }
 
   private Configuration configure() {
     ConfigurationBuilder builder = new ConfigurationBuilder();
     builder.setUseSSL(true);
     builder.setJSONStoreEnabled(true);
     builder.setOAuthConsumerKey(configuration.getConsumerKey());
     builder.setOAuthConsumerSecret(configuration.getConsumerSecret());
     builder.setApplicationOnlyAuthEnabled(true);
     return builder.build();
   }
 
   private void useProxy(ConfigurationBuilder builder) {
     builder.setHttpProxyHost("localhost");
     builder.setHttpProxyPort(3128);
   }
 }
