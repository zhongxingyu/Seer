 package co.shoutbreak.core;
 
 // Constants
 public class C {
 
 	// CONFIG /////////////////////////////////////////////////////////////////
 	// Match this to the $version in index.php
 	public static final boolean CONFIG_ADMIN_SUPERPOWERS = false;
 	public static final String CONFIG_SERVER_ADDRESS = "http://23.21.234.1/005/"; //http://app.shoutbreak.co/005/
 	public static final int CONFIG_HTTP_TIMEOUT = 25000;
 	public static final int CONFIG_DROPPED_PACKET_LIMIT = 5;
 	public static final int CONFIG_NOTICES_DISPLAYED_IN_TAB = 50;
 	public static final String CONFIG_SUPPORT_ADDRESS = "http://shoutbreak.com/support";
 	public static final long CONFIG_SHOUTREACH_RADIUS_EXPIRATION = (long) 1800000; // 30 minutes
 	public static final int CONFIG_DENSITY_GRID_X_GRANULARITY = 129600; // 10 second cells
 	public static final int CONFIG_DENSITY_GRID_Y_GRANULARITY = 64800; // 10 second cells
 	public static final int CONFIG_GPS_MIN_UPDATE_MILLISECS = 60000; // 0 gives most frequent
 	public static final int CONFIG_GPS_MIN_UPDATE_METERS = 20; // 0 gives smallest interval
 	public static final int CONFIG_MAX_SIMULTANEOUS_HTTP_CONNECTIONS = 5; // for ConnectionQueue
 	public static final int CONFIG_SCORE_REQUEST_LIMIT = 30;
 	public static final int CONFIG_SHOUT_MAXLENGTH = 256; // also in integer.xml
 	public static final double CONFIG_SHOUT_SCORING_DEFAULT_POWER = 0.10; //  0.10 to have a 95% chance that your lower bound is correct
 	public static final double NORMAL_DIST_B[] = { 1.570796288, 0.03706987906, -0.8364353589e-3, -0.2250947176e-3,
 		0.6841218299e-5, 0.5824238515e-5, -0.104527497e-5, 0.8360937017e-7, -0.3231081277e-8, 0.3657763036e-10,
 		0.6936233982e-12 };
 	
 	// PHONE MODELS FOR HACKY FIXES ///////////////////////////////////////////
 	public static final String PHONE_DROID_X = "DROIDX";
 
 	// STRINGS ////////////////////////////////////////////////////////////////
 	public static final String STRING_NO_ACCOUNT = "Welcome to Shoutbreak!\nNo account detected, creating one now...";
 	public static final String STRING_ACCOUNT_CREATED = "Account created successfully.\nYou can now send and receive shouts!";
 	public static final String STRING_LEVEL_UP_1 = "You leveled up! You're now level ";
 	public static final String STRING_LEVEL_UP_2 = "Your shouts will reach ";
 	public static final String STRING_SHOUT_SENT = "Shout complete.";
 	public static final String STRING_SHOUT_FAILED = "Shout failed.";
 	public static final String STRING_VOTE_FAILED = "Vote failed.  Shout is too old.";
 	public static final String STRING_CREATE_ACCOUNT_FAILED = "Unable to create an account.";
 	public static final String STRING_FORCED_POLLING_STOP = "App turning off.  Dropped too many consecutive packets.";
 	
 	// NOTICES ////////////////////////////////////////////////////////////////
 	// 0 - 19
 	public static final int NOTICE_SHOUTS_RECEIVED = 0;
 	public static final int NOTICE_LEVEL_UP = 1;
 	public static final int NOTICE_SHOUT_SENT = 2;
 	public static final int NOTICE_SHOUT_FAILED = 3;
 	public static final int NOTICE_NO_ACCOUNT = 4;
 	public static final int NOTICE_ACCOUNT_CREATED = 5;
 	public static final int NOTICE_CREATE_ACCOUNT_FAILED = 6;
 	public static final int NOTICE_VOTE_FAILED = 8;
 	public static final int NOTICE_POINTS_VOTING = 9;
 	public static final int NOTICE_POINTS_SHOUT = 10;
 	public static final int NOTICE_FORCED_POLLING_STOP = 11;
 	public static final int LEVEL_UP_NOTICE = Integer.MIN_VALUE;
 	
 	// MAP ////////////////////////////////////////////////////////////////////
 	public static final int DEFAULT_ZOOM_LEVEL = 16;
 	public static final int CONFIG_TOUCH_TOLERANCE = 4;
 	public static final int CONFIG_RESIZE_ICON_TOUCH_TOLERANCE = 100; // +/- 50 px from center
 	public static final int MIN_RADIUS_PX = 30; // user can't resize below this
 	
 	// HTTP CODES /////////////////////////////////////////////////////////////
 	// 20 - 29
 	public static final int HTTP_DID_START = 20;
 	public static final int HTTP_DID_EXCEPTION = 21;
 	public static final int HTTP_DID_STATUS_CODE_ERROR = 22;
 	public static final int HTTP_DID_SUCCEED = 23;
 	
 	// JSON KEYS //////////////////////////////////////////////////////////////
 	public static final String JSON_ACTION = "a";
 	public static final String JSON_ACTION_CREATE_ACCOUNT = "create_account";
 	public static final String JSON_ACTION_SHOUT = "shout";
 	public static final String JSON_ACTION_USER_PING = "ping";
 	public static final String JSON_ACTION_VOTE = "vote";
 	
 	public static final String JSON_CODE = "code";
 	public static final String JSON_CODE_ANNOUNCEMENT = "announcement";
 	public static final String JSON_CODE_ERROR = "error";
 	public static final String JSON_CODE_EXPIRED_AUTH = "expired_auth";
 	public static final String JSON_CODE_INVALID_UID = "invalid_uid";
 	public static final String JSON_CODE_PING_OK = "ping_ok";
 	public static final String JSON_CODE_VOTE_OK = "vote_ok";
 	public static final String JSON_CODE_VOTE_FAIL = "vote_fail";
 	
 	public static final String JSON_ANDROID_ID = "android_id";
 	public static final String JSON_AUTH = "auth";
 	public static final String JSON_CARRIER_NAME = "carrier";
 	public static final String JSON_RADIUS = "radius";
 	public static final String JSON_RADIUS_HINT = "hint";
 	public static final String JSON_DEVICE_ID = "device_id";
 	public static final String JSON_LAT = "lat";
 	public static final String JSON_LEVEL = "lvl";
 	public static final String JSON_LEVEL_AT = "lvl_at";
 	public static final String JSON_LEVEL_CHANGE = "lvl_change";
 	public static final String JSON_LONG = "lng";
 	public static final String JSON_NEXT_LEVEL_AT = "next_lvl_at";
 	public static final String JSON_NONCE = "nonce";
 	public static final String JSON_PHONE_NUM = "phone_num";
 	public static final String JSON_POINTS = "pts";
 	public static final String JSON_PW = "pw";
 	public static final String JSON_SCORES = "scores";
 	public static final String JSON_SHOUT_DOWNS = "downs";
 	public static final String JSON_SHOUT_HIT = "hit";
 	public static final String JSON_SHOUT_ID = "shout_id";
 	public static final String JSON_SHOUT_OPEN = "open";
 	public static final String JSON_SHOUT_OUTBOX = "outbox";
 	public static final String JSON_SHOUT_POWER = "shoutreach";
 	public static final String JSON_SHOUT_RE = "re";
 	public static final String JSON_SHOUT_TEXT = "txt";	
 	public static final String JSON_SHOUT_TIMESTAMP = "ts";
 	public static final String JSON_SHOUT_UPS = "ups";
 	public static final String JSON_SHOUTS = "shouts";	
 	public static final String JSON_UID = "uid";
 	public static final String JSON_VOTE = "vote";
 	
 	// JSON FALLBACKS 
 	// what we assume if value not returned by server
 	public static final int NULL_DOWNS = 0;
 	public static final int NULL_HIT = -1;
 	public static final boolean NULL_OPEN = false;
 	public static final int NULL_PTS = 0;
 	public static final int NULL_SCORE = 0;
 	public static final int NULL_UPS = 0;
 	public static final int NULL_VOTE = 0;
 	public static final int NULL_OUTBOX = 0;
 	public static final String NULL_REPLY = "";	
 	
 	// STATES /////////////////////////////////////////////////////////////////
 	// 30 - 39
 	public static final int STATE_SHOUT = 31;
 	public static final int STATE_VOTE = 32;
 	public static final int STATE_IDLE = 33;
 	
 	// 40 - 49
 	public static final int SHOUT_STATE_NEW = 40;
 	public static final int SHOUT_STATE_READ = 41;
 	public static final int SHOUT_VOTE_UP = 1;
 	public static final int SHOUT_VOTE_DOWN = -1;	
 	
 	// 50 - 59
 	public static final int NOTICE_STATE_NEW = 50;
 	public static final int NOTICE_STATE_READ = 51;
 	
 	// PURPOSES ///////////////////////////////////////////////////////////////
 	// 60 - 69
 	public static final int PURPOSE_LOOP_FROM_UI = 60; // no delay
 	public static final int PURPOSE_LOOP_FROM_UI_DELAYED = 61; // delay
 	public static final int PURPOSE_DEATH = 62; // don't repeat this - just die
 	
 	// DATABASE ///////////////////////////////////////////////////////////////
	public static final int DB_VERSION = 12;
 
 	public static final String DB_NAME = "sbdb";
 	public static final String DB_TABLE_RADIUS = "RADIUS";
 	public static final String DB_TABLE_SHOUTS = "SHOUTS";
 	public static final String DB_TABLE_POINTS = "POINTS";
 	public static final String DB_TABLE_NOTICES = "NOTICES";
 	public static final String DB_TABLE_USER_SETTINGS = "USER_SETTINGS";
 	
 	public static final String KEY_USER_ID = "user_id";
 	public static final String KEY_USER_LEVEL = "user_level";
 	public static final String KEY_USER_LEVEL_AT = "user_level_at";
 	public static final String KEY_USER_NEXT_LEVEL_AT = "user_next_level_at";
 	public static final String KEY_USER_POINTS = "user_points"; 
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
 	public static final String PREFERENCE_SIGNATURE_ENABLED = "sig_enabled";
 	public static final String PREFERENCE_SIGNATURE_TEXT = "sig_text";
 	
 	// ACTIVITY RESULTS ///////////////////////////////////////////////////////
 	// 80 - 89
 	public static final int ACTIVITY_RESULT_LOCATION = 80;
 	
 	// NOTIFICATIONS //////////////////////////////////////////////////////////
 	// 90 - 99
 	public static final int APP_NOTIFICATION_ID = 90;
 	public static final String NOTIFICATION_LAUNCHED_FROM_UI = "app_launched_from_ui";
 	public static final String NOTIFICATION_LAUNCHED_FROM_ALARM = "app_launched_from_alarm";
 	public static final String NOTIFICATION_LAUNCHED_FROM_NOTIFICATION = "app_launched_from_notification";
 
 }
