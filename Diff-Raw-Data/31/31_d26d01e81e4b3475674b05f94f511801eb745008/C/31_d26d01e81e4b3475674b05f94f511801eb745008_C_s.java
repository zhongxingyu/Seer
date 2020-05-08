 package co.shoutbreak.core;
 
 // Constants
 public class C {
 
 	// CONFIG /////////////////////////////////////////////////////////////////
 	public static final String CONFIG_CRASH_REPORT_ADDRESS = "http://app.shoutbreak.co/crash_reports/upload.php";
 	public static final String CONFIG_SUPPORT_ADDRESS = "http://shoutbreak.com/support";
 	public static final long CONFIG_DENSITY_EXPIRATION = (long) 4.32E8; // 5 days
 	public static final int CONFIG_DENSITY_GRID_X_GRANULARITY = 129600; // 10 second cells
 	public static final int CONFIG_DENSITY_GRID_Y_GRANULARITY = 64800; // 10 second cells
 	public static final int CONFIG_GPS_MIN_UPDATE_MILLISECS = 60000; // 0 gives most frequent
 	public static final int CONFIG_GPS_MIN_UPDATE_METERS = 20; // 0 gives smallest interval
 	public static final int CONFIG_MIN_TARGETS_FOR_HIT_COUNT = 3;
 	public static final int CONFIG_MAX_SIMULTANEOUS_HTTP_CONNECTIONS = 5; // for ConnectionQueue
 	public static final String CONFIG_SERVER_ADDRESS = "http://app.shoutbreak.co";
 	public static final long CONFIG_IDLE_LOOP_TIME_WITH_UI_OPEN = 20000; // 60 seconds
 	public static final int CONFIG_PEOPLE_PER_LEVEL = 5;
 	public static final double CONFIG_SHOUT_SCORING_DEFAULT_POWER = 0.10; //  0.10 to have a 95% chance that your lower bound is correct
 	public static final long CONFIG_NOTICE_DISPLAY_TIME = 5000; // 5 seconds
 	public static final double NORMAL_DIST_B[] = { 1.570796288, 0.03706987906, -0.8364353589e-3, -0.2250947176e-3,
 		0.6841218299e-5, 0.5824238515e-5, -0.104527497e-5, 0.8360937017e-7, -0.3231081277e-8, 0.3657763036e-10,
 		0.6936233982e-12 };
 
 	// STRINGS ////////////////////////////////////////////////////////////////
 	public static final String STRING_SERVER_DOWN = "Sorry, our server is not playing nice right now.\nWant to check our site for a downtime notice?";
 	public static final String STRING_NO_ACCOUNT = "Welcome to Shoutbreak!\nNo account detected, creating one now...";
 	public static final String STRING_ACCOUNT_CREATED = "Account created successfully.\nYou can now send and receive shouts!";
 	public static final String STRING_LEVEL_UP_1 = "You leveled up! You're now level ";
 	public static final String STRING_LEVEL_UP_2 = "Your shouts will reach ";
 	public static final String STRING_SHOUT_SENT = "Shout complete.";
 	public static final String STRING_SHOUT_FAILED = "Shout failed.";
 	public static final String STRING_VOTE_FAILED = "Vote failed.";
 	public static final String STRING_CREATE_ACCOUNT_FAILED = "Unable to create an account.";
 	public static final String STRING_PING_FAILED = "Unable to reach server.";
 	
 	// NOTICES ////////////////////////////////////////////////////////////////
 	// 0 - 19
 	public static final int NOTICE_SHOUTS_RECEIVED = 0;
 	public static final int NOTICE_LEVEL_UP = 1;
 	public static final int NOTICE_SHOUT_SENT = 2;
 	public static final int NOTICE_SHOUT_FAILED = 3;
 	public static final int NOTICE_NO_ACCOUNT = 4;
 	public static final int NOTICE_ACCOUNT_CREATED = 5;
 	public static final int NOTICE_CREATE_ACCOUNT_FAILED = 6;
 	public static final int NOTICE_PING_FAILED = 7;
 	public static final int NOTICE_VOTE_FAILED = 8;
 	public static final int NOTICE_POINTS = 9;
 	
 	// MAP ////////////////////////////////////////////////////////////////////
 	public static final int DEFAULT_ZOOM_LEVEL = 16;
 	public static final int DEGREE_LAT_IN_METERS = 111133; // 60 nautical miles - avg'd from http://en.wikipedia.org/wiki/Latitude#Degree_length
 	public static final int CONFIG_TOUCH_TOLERANCE = 4;
 	public static final int CONFIG_RESIZE_ICON_TOUCH_TOLERANCE = 100; // +/- 50 px from center
 	public static final int MIN_RADIUS_PX = 30; // user can't resize below this
 	
 	// HTTP CODES /////////////////////////////////////////////////////////////
 	// 20 - 29
 	public static final int HTTP_DID_START = 20;
 	public static final int HTTP_DID_ERROR = 21;
 	public static final int HTTP_DID_SUCCEED = 22;
 	
 	// JSON KEYS //////////////////////////////////////////////////////////////
 	public static final String JSON_ACTION = "a";
 	public static final String JSON_ACTION_CREATE_ACCOUNT = "create_account";
 	public static final String JSON_ACTION_SHOUT = "shout";
 	public static final String JSON_ACTION_USER_PING = "user_ping";
 	public static final String JSON_ACTION_VOTE = "vote";
 	
 	public static final String JSON_CODE = "code";	
 	public static final String JSON_CODE_EXPIRED_AUTH = "expired_auth";
 	public static final String JSON_CODE_INVALID_UID = "invalid_uid";
 	public static final String JSON_CODE_PING_OK = "ping_ok";
 	public static final String JSON_CODE_SHOUTS = "shouts";
 	
 	public static final String JSON_ANDROID_ID = "android_id";
 	public static final String JSON_AUTH = "auth";
 	public static final String JSON_CARRIER_NAME = "carrier";
 	public static final String JSON_DENSITY = "rho";
 	public static final String JSON_DEVICE_ID = "device_id";
 	public static final String JSON_LAT = "lat";
 	public static final String JSON_LEVEL = "lvl";
 	public static final String JSON_LEVEL_CHANGE = "level_change";
 	public static final String JSON_LONG = "long";
 	public static final String JSON_NEXT_LEVEL_AT = "next_lvl_at";
 	public static final String JSON_NONCE = "nonce";
 	public static final String JSON_PHONE_NUM = "phone_num";
 	public static final String JSON_POINTS = "pts";
 	public static final String JSON_PW = "pw";
 	public static final String JSON_SCORES = "scores";
 	public static final String JSON_SHOUT_APPROVAL = "approval";
 	public static final String JSON_SHOUT_DOWNS = "downs";
 	public static final String JSON_SHOUT_HIT = "hit";
 	public static final String JSON_SHOUT_ID = "shout_id";
 	public static final String JSON_SHOUT_OPEN = "open";
 	public static final String JSON_SHOUT_POWER = "power";
 	public static final String JSON_SHOUT_RE = "re";
 	public static final String JSON_SHOUT_TEXT = "txt";	
 	public static final String JSON_SHOUT_TIMESTAMP = "ts";
 	public static final String JSON_SHOUT_UPS = "ups";
 	public static final String JSON_SHOUTS = "shouts";	
 	public static final String JSON_UID = "uid";
 	public static final String JSON_VOTE = "vote";
 	
 	// JSON FALLBACKS 
 	// what we assume if value not returned by server	
 	public static final int NULL_APPROVAL = -1;
 	public static final int NULL_DOWNS = 0;
 	public static final int NULL_HIT = 0;
 	public static final boolean NULL_OPEN = false;
 	public static final int NULL_PTS = 0;
 	public static final int NULL_SCORE = 0;
 	public static final int NULL_UPS = 0;
 	public static final int NULL_VOTE = 0;
 		
 	// STATES /////////////////////////////////////////////////////////////////
 	// 30 - 39
 	public static final int STATE_CREATE_ACCOUNT_2 = 30;
 	public static final int STATE_EXPIRED_AUTH = 31;
 	public static final int STATE_RECEIVE_SHOUTS = 32;
 	public static final int STATE_SHOUT = 33;
 	public static final int STATE_VOTE = 34;
 	public static final int STATE_IDLE = 35;
 	
 	// 40 - 49
 	public static final int SHOUT_STATE_NEW = 40;
 	public static final int SHOUT_STATE_READ = 41;
 	public static final int SHOUT_VOTE_UP = 1;
 	public static final int SHOUT_VOTE_DOWN = -1;	
 	
 	// 50 - 59
 	public static final int NOTICE_STATE_NEW = 50;
 	public static final int NOTICE_STATE_READ = 51;
 	
 	// ALARM MANAGER //////////////////////////////////////////////////////////
 	public static final String ALARM_START_FROM_UI = "start_from_ui";
 	
 	// PURPOSES ///////////////////////////////////////////////////////////////
 	// 60 - 69
 	public static final int PURPOSE_LOOP_FROM_UI = 60; // no delay
 	public static final int PURPOSE_LOOP_FROM_UI_DELAYED = 61; // delay
 	public static final int PURPOSE_DEATH = 62; // don't repeat this - just die
 	
 	// DATABASE ///////////////////////////////////////////////////////////////
	public static final int DB_VERSION = 5;
 
 	public static final String DB_NAME = "sbdb";
 	public static final String DB_TABLE_DENSITY = "DENSITY";
 	public static final String DB_TABLE_SHOUTS = "SHOUTS";
 	public static final String DB_TABLE_POINTS = "POINTS";
 	public static final String DB_TABLE_NOTICES = "NOTICES";
 	public static final String DB_TABLE_USER_SETTINGS = "USER_SETTINGS";
 	
 	public static final String KEY_USER_ID = "user_id";
 	public static final String KEY_USER_LEVEL = "user_level";
 	public static final String KEY_USER_NEXT_LEVEL_AT = "user_next_level_at";
 	public static final String KEY_USER_PW = "user_pw";
 	
 	// POINTS TYPES ///////////////////////////////////////////////////////////
 	// 70 - 79
 	public static final int POINTS_LEVEL_CHANGE = 70;
 	public static final int POINTS_SHOUT = 71;
 	public static final int POINTS_VOTE = 72;
 	
 	// PREFERENCES ////////////////////////////////////////////////////////////
 	public static final String PREFERENCE_FILE = "preferences";
 	public static final String PREFERENCE_POWER_STATE = "power_state_pref";
 	public static final String PREFERENCE_IS_FIRST_RUN = "is_first_run";
 	
 	// ACTIVITY RESULTS ///////////////////////////////////////////////////////
 	// 80 - 89
 	public static final int ACTIVITY_RESULT_LOCATION = 80;
 	
 	// NOTIFICATIONS //////////////////////////////////////////////////////////
 	// 90 - 99
 	public static final String NOTIFICATION_REFERRAL_ID = "NOTIFICATION_REFERRAL_ID";
 	public static final int APP_NOTIFICATION_ID = 90;
 	public static final String EXTRA_REFERRED_FROM_NOTIFICATION = "rfn";
 	public static final String NOTIFICATION_LAUNCHED_FROM_UI = "app_launched_from_ui";
 	public static final String NOTIFICATION_LAUNCHED_FROM_ALARM = "app_launched_from_alarm";
 	public static final String NOTIFICATION_LAUNCHED_FROM_NOTIFICATION = "app_launched_from_notification";
 
 }
