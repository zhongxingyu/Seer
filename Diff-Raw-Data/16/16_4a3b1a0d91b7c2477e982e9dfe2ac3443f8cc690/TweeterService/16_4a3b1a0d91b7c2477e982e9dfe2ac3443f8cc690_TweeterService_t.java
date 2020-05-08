 package com.fhx.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.Properties;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 
 public enum TweeterService {
 	INSTANCE;
 
 	private Logger log = LogManager.getLogger(TweeterService.class);	
 	
 	private Twitter twitter = null;
 	private String ConsumerKey;
 	private String ConsumerSecret;
     private String AccessToken;
     private String AccessTokenSecret;
     
 	private TweeterService() {
 		loadTweeterProperties();
 		initTweeter();
 	}
 	
 	private void loadTweeterProperties() {
         File file = new File("conf/twitter4j.properties");
         Properties prop = new Properties();
         InputStream is = null;
         try {
             if (file.exists()) {
                 is = new FileInputStream(file);
                 prop.load(is);
                 		
                 ConsumerKey = prop.getProperty("oauth.consumerKey");
                 ConsumerSecret = prop.getProperty("oauth.consumerSecret");
                 AccessToken = prop.getProperty("oauth.accessToken");
                 AccessTokenSecret = prop.getProperty("oauth.accessTokenSecret");
                 
             }
             else {
             	log.error("cannot find twitter4j.properties.  will not send status to tweeter.");
             }
             
         } catch (IOException ioe) {
             ioe.printStackTrace();
         } finally {
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException ignore) {
                 }
             }
         }
 	}
 	
 	public void initTweeter() {
 		try {
 			// OAuth properties set via adapter properties or via SBCONF references or module parameters
 			if (ConsumerKey.isEmpty() || ConsumerSecret.isEmpty() || AccessToken.isEmpty() || AccessTokenSecret.isEmpty()) {
 				log.warn("OAuth adapter properties cannot be empty.");
 			}			
 			log.info(String.format("setting tweeter properties: key=%s, token=%s", ConsumerKey, AccessToken));
 			
 			System.setProperty("twitter4j.oauth.consumerKey", ConsumerKey );
 			System.setProperty("twitter4j.oauth.consumerSecret", ConsumerSecret );
 //			System.setProperty("twitter4j.oauth.accessToken", AccessToken );
 //			System.setProperty("twitter4j.oauth.accessTokenSecret", AccessTokenSecret );
 
 			this.twitter = new TwitterFactory().getInstance();
 			//this.twitter.setOAuthConsumer(ConsumerKey, ConsumerSecret);
 			
 			AccessToken oathAccessToken = new AccessToken(AccessToken, AccessTokenSecret);
 
 			this.twitter.setOAuthAccessToken(oathAccessToken);
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void sendTweet(String tweet) {
 		try {
 			twitter.updateStatus(tweet);
			log.info("tweet-> " + tweet);
 		}
 		catch(TwitterException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void main(String[] args) {	
 		TweeterService.INSTANCE.sendTweet("my testing@" + new Date());
 		
 		TweeterService.INSTANCE.sendTweet("my testing2: haha");
 	}
 	
 }
