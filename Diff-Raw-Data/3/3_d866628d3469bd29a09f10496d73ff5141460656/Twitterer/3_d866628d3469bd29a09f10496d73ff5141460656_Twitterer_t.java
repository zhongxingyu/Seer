 package org.acl.root;
 
 import static org.acl.root.TwitterOAuthConstants.CONSUMER_KEY;
 import static org.acl.root.TwitterOAuthConstants.CONSUMER_SECRET;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.conf.Configuration;
 import twitter4j.conf.ConfigurationBuilder;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.util.Log;
 
 public enum Twitterer implements CallObserver {
 	
 	INSTANCE;
 	
 	private static final String TAG = "Twitterer";
 	
 	private static final String TWITTER_PREFS = "twitter_preferences";
	// Max length is 120 (and not 140) because the date/time is also inserted
	private static final int MAX_TWEET_LENGTH = 120;
 	
 	private static final String TWITTER_OAUTH_ACCESS_TOKEN = "twitter_access_token";
 	private static final String TWITTER_OAUTH_ACCESS_TOKEN_SECRET = "twitter_access_token_secret";
 	
 	//private static Twitterer instance;
 	
 	private String defaultTweet = "Sorry, the person you are trying to communicate with is busy";
 	
 	private Twitter twitter = null;
 	
 //	public static Twitterer getInstance(Context context) {
 //		if(instance == null)
 //			instance = new Twitterer(context);
 //		return instance;
 //	}
 //	
 //	private Twitterer(Context context) {
 //		
 //		SharedPreferences twitterPreferences = 
 //				context.getSharedPreferences(TWITTER_PREFS, Activity.MODE_PRIVATE);
 //		
 //		Configuration conf = new ConfigurationBuilder()
 //	    .setOAuthConsumerKey(CONSUMER_KEY)
 //	    .setOAuthConsumerSecret(CONSUMER_SECRET)
 //	    .setOAuthAccessToken(twitterPreferences.getString(TWITTER_OAUTH_ACCESS_TOKEN, "" ))
 //	    .setOAuthAccessTokenSecret(twitterPreferences.getString(TWITTER_OAUTH_ACCESS_TOKEN_SECRET, "" ))
 //	    .build();
 //	 
 //		twitter = new TwitterFactory(conf).getInstance();
 //	}
 		
 	public boolean setDefaultTweet(String text) {
 		
 		if (text.length() > MAX_TWEET_LENGTH) {
 			defaultTweet = text.substring(0, MAX_TWEET_LENGTH);
 			return false;
 		} else {
 			defaultTweet = text;
 			return true;
 		}
 	}
 	
 	@Override
 	public void callNotification(CallInfo callInfo) {
 		if(twitter == null) {
 			getTwitterInstance(callInfo.getContext());
 		}
 		
 		try {
 			Log.d(TAG, "Sending defaultTweet: " + defaultTweet);
 			twitter.updateStatus(callInfo.getDate() + " " + callInfo.getTime() + " " + defaultTweet);
 			Log.d(TAG, "Tweet sent");
 		} catch (TwitterException e) {
 			Log.e(TAG, "Can't send Tweet", e);
 		}	
 	}
 
 	private void getTwitterInstance(Context context) {
 		SharedPreferences twitterPreferences = 
 				context.getSharedPreferences(TWITTER_PREFS, Activity.MODE_PRIVATE);
 		
 		Configuration conf = new ConfigurationBuilder()
 		.setOAuthConsumerKey(CONSUMER_KEY)
 		.setOAuthConsumerSecret(CONSUMER_SECRET)
 		.setOAuthAccessToken(twitterPreferences.getString(TWITTER_OAUTH_ACCESS_TOKEN, "" ))
 		.setOAuthAccessTokenSecret(twitterPreferences.getString(TWITTER_OAUTH_ACCESS_TOKEN_SECRET, "" ))
 		.build();
  
 		twitter = new TwitterFactory(conf).getInstance();
 	}
 
 }
