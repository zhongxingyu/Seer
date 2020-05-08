 package de.geotweeter;
 
 import java.util.regex.Pattern;
 
 import android.app.Application;
 import android.graphics.Path;
 
 public class Constants extends Application {
 
 	public static Object THREAD_LOCK = new Object();
 	
 	public static final String PREFS_APP = "geotweeter_general";
 	public static final String PREFS_ACCOUNTS = "geotweeter_accounts";
 	
 	public static final String OAUTH_CALLBACK = "oauth://twitter";
 	
 	public static final long PIC_SIZE_TWITTER = 3145728;
 	
 	public static final String URI_VERIFY_CREDENTIALS_1	= "https://api.twitter.com/1/account/verify_credentials.json"; // API v1: wird für Twitpic benötigt
 	public static final String URI_VERIFY_CREDENTIALS   = "https://api.twitter.com/1.1/account/verify_credentials.json";
 	public static final String URI_UPDATE_PROFILE       = "https://api.twitter.com/1.1/account/update_profile.json";
 	public static final String URI_USER_STREAM          = "https://userstream.twitter.com/1.1/user.json";
 	public static final String URI_HOME_TIMELINE        = "https://api.twitter.com/1.1/statuses/home_timeline.json";
 	public static final String URI_MENTIONS             = "https://api.twitter.com/1.1/statuses/mentions_timeline.json";
 	public static final String URI_CONFIGURATION        = "https://api.twitter.com/1.1/help/configuration.json";
 	public static final String URI_DESTROY              = "https://api.twitter.com/1.1/statuses/destroy/:id.json";
 	public static final String URI_DIRECT_MESSAGES      = "https://api.twitter.com/1.1/direct_messages.json";
 	public static final String URI_DIRECT_MESSAGES_SENT = "https://api.twitter.com/1.1/direct_messages/sent.json";
 	public static final String URI_SINGLE_STATUS        = "https://api.twitter.com/1.1/statuses/show.json";
 	public static final String URI_SEARCH               = "https://api.twitter.com/1.1/search/tweets.json";
 	public static final String URI_SHOW_TWEET           = "https://api.twitter.com/1.1/statuses/show.json";
 	public static final String URI_SINGLE_USER          = "https://api.twitter.com/1.1/users/show.json";
 	public static final String URI_USER_LIST            = "https://api.twitter.com/1.1/users/lookup.json";
 	public static final String URI_USER_TIMELINE        = "https://api.twitter.com/1.1/statuses/user_timeline.json";
 	public static final String URI_UPDATE               = "https://api.twitter.com/1.1/statuses/update.json";
 	public static final String URI_UPDATE_WITH_MEDIA    = "https://api.twitter.com/1.1/statuses/update_with_media.json";
 	public static final String URI_RETWEET				= "https://api.twitter.com/1.1/statuses/retweet/:id.json";
 	
 	public static final String URI_TWEETMARKER_LASTREAD = "https://api.tweetmarker.net/v1/lastread?collection=timeline,mentions,messages&username=";
 		
 	public static final String PATH_AVATAR_IMAGES       = "avatars";
 	public static final String PATH_TIMELINE_DATA       = "timelines";
 		
 	public static final Pattern REGEXP_FIND_SOURCE = Pattern.compile(">(.+)</a>");
 	
 	public static final String TWITPIC_URI = "http://api.twitpic.com/2/upload.json";
 	
 	public static final Path LOCATION_MARKER = new LocationMarker();
 
 	public static final int SENDING_TWEET_STATUS_NOTIFICATION_ID = 654616410;
 	
 	public static final int CHECKED_ALPHA_VALUE = 255;
	public static final int UNCHECKED_ALPHA_VALUE = 127;
 	
 	private static class LocationMarker extends Path {
 		
 		public LocationMarker() {
 			super();
 			moveTo(0, 0);
 			lineTo(50, 0);
 			lineTo(50, 50);
 			lineTo(30, 50);
 			lineTo(25, 55);
 			lineTo(20, 50);
 			lineTo(0, 50);
 			close();
 		}
 		
 	}
 	
 }
