 package org.jefferyemanuel.willowtweetapp;
 
 import android.net.Uri;
 
 /**
  * This class holds global constants that are used throughout the application to
  * support in-app billing.
  */
 
 public class Consts {
 
 	// lock the class from being instantiated as this is only a place holder for constants
 	private Consts() {
 
 	}
 
 	//developer and logging options
	public static final boolean DEVELOPER_MODE =false; //TODO check for traceview if its on
 	public static final String TAG = "willowtweet";
 	public static final String TAG_FRAGMENT = "fragments";
 
 	//MAP keys  (for json twitter object)
 	public static final String KEY_USER_OBJECT = "user";
 	public static final String KEY_SCREEN_NAME="screenname";
 	public static final String KEY_USERID="userid";
 	public static final String KEY_TIMEPOSTED = "timePosted";
 	public static final String KEY_TWEETDATE = "tweetDate";
 	public static final String KEY_AUTHOR = "author";
 	public static final String KEY_AVATAR = "avatar";
 	public static final String KEY_TWEED_ID = "tweetId";
 	public static final String KEY_TWEET_COUNT = "tweetCount";
 	public static final String KEY_TWEET_MSG = "tweet";
 	public static final String KEY_IS_RETWEET = "isRetweet";
 	public static final String KEY_FOLLOWERS = "followers";
 	public static final String KEY_FOLLOWING = "following";
 	public static final String KEY_CREATED_DATE= "date_created";
 	
 	// Database table
 	public static final String TABLE_TWEETER_INFO = "tweeterinfo";
 	public static final String COLUMN_ID = "_id";
 	public static final String COLUMN_USER = "username";
 
 	public static final String DATABASE_NAME = "willowtweet.db";
 	public static final String AUTHORITY = "org.jefferyemanuel.database.TweeterContentProvider";
 	public static final int DATABASE_VERSION = 1;
 
 	//content provider
 	public static final Uri CONTENT_URI = Uri.parse("content://"
 			+ Consts.AUTHORITY + "/" + "tweeters");
 
 	//  public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.tweeter.tweeterinfo";
 
 	//cursor loader
 	public static final int LOADER_ID = 0x01;
 	//asynchTaskLoader
 	public static final int LOADER_ASYNCH_ID = 0x02;
 
 	//dialogfragment constants
 	public static final String MESSAGE = "message";
 
 	//task fragment id
 	public static final String TASK_ARGUMENT_USERLIST = "userlist";
 	//Twitter authentication oAUTH keys
 	//TODO use a webservice call to get this key never leave it in local code base
 	public static final String CONSUMER_KEY = "eNLyfXOWo0rGODaI2KR0dQ"; // 
 	public static final String CONSUMER_SECRET_KEY = "FndHyAQryGeWKYPjmU92sv2F54xrLiOznK6eEtmS8";
 	public static final String ACCESS_TOKEN = "420386498-55vIULyl5xXP2FMpxc7GVv9GGqSolk7aLWm18K0o";
 	public static final String ACCESS_TOKEN_SECRET = "BRLfWdt5nqSmfn9BteAVrOA5BOXHxvUcq3uwiiXEOr0";
 	public static final String CALLBACKURL = "app://twitter"; //ignore
 	public static final String URL_REQUEST_TOKEN = "https://twitter.com/oauth/request_token";
 	public static final String URL_ACCESS_TOKEN = "https://twitter.com/oauth/access_token";
 	public static final String URL_AUTHORIZE = "https://twitter.com/oauth/authorize";
 
 	//handler message descriptions
 	public static final String HANDLER_SHOW_DIALOG = "showdialog";
 	public static final String HANDLER_HIDE_DIALOG = "hidedialog";
 	public static final String HANDLER_REMOVE_TASK = "removetask";
 	public static final String HANDLER_UPDATE_PAGER_ADAPTER = "starttask";
 	//configuration constants
 	public static final int NUMBER_OF_STATUSES = 20;
 
 	//disk cache constants
 	public static final int IO_BUFFER_SIZE = 8 * 1024;
 	public static final int DISK_CACHE_SIZE = 1024 * 1024 * 20; // 20MB
 	public static final int COMPRESS_QUALITY = 100;
 
 	/*
 	 * twitter max download reached error # (twitter.com has a max download each
 	 * registered oauth app can have.currently its 100/hr)
 	 */
 	public static final int ERROR_CODE_RATE_LIMIT_EXCEEDED = 88;
 	//admob items
 	public static final String admob_publisherID = "a151f6af36e8971";
 
 	public static final String URL_TWITTER_SEARCH="https://twitter.com/search?q=";
 	
 	//orientation Change keys
 	public static final String SAVED_INSTANCE_TWEETERS="data";
 	public static final String SAVED_INSTANCE_POSITION="current_position";
 public static final String SAVED_INSTANCE_EDIT_MODE="editing_mode";
 
 }
