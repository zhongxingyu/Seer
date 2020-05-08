 package com.coffeeandpower;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.telephony.TelephonyManager;
 import android.text.Html;
 import android.util.Log;
 
 import com.coffeeandpower.datatiming.Counter;
 import com.coffeeandpower.urbanairship.IntentReceiver;
 import com.coffeeandpower.utils.HttpUtil;
 import com.urbanairship.AirshipConfigOptions;
 import com.urbanairship.UAirship;
 import com.urbanairship.push.PushManager;
 import com.urbanairship.push.PushPreferences;
 
 public class AppCAP extends Application {
 
 	// How to generate a map key for debug
 	// 1. Use keytool to get MD5 for your debug app:
 	//    - e.g. c:\Program Files\Java\jre6\bin\keytool -list -alias androiddebugkey -keystore c:\Users\<username>\.android\debug.keystore
 	//    - debug keystore password is: android
 	// 2. Go to this URL: https://developers.google.com/maps/documentation/android/maps-api-signup?hl=en-US
 	// 3. Enter the MD5 from keytool
 	// 4. Once you have your key, replace the property android:apiKey in res/layout/tab_activity_map.xml
 	
 	// Google maps api key for debug Kep:
 	//   0PV0Dp_6Dj6PkG_8xJqiTbSPxXwq2XEiEqXkO_Q
 	// Google maps api key for debug Tengai home:
 	//   0PV0Dp_6Dj6M_WBuUrThj9-fW3btGy9kxl83wgQ
 	// Map key for andrewa debug:
 	//   08WpTLaphEjlVeOsrM0kfBODmF3ieB49C4lEHJA
 	// Google maps key for debug Andres:
 	//  0N2B-_20GlM_H0LiHavOsRcF1VIqEQmyxijXZ3w
 
 	public static final String TAG = "CoffeeAndPower";
 
 	private static final String TAG_USER_EMAIL = "tag_user_email";
 	private static final String TAG_USER_EMAIL_PASSWORD = "tag_user_email_password";
 	private static final String TAG_USER_ENTERED_INVITE_CODE = "entered_invite_code";
 	private static final String TAG_USER_LAST_VENUE_CHECKIN_ID = "tag_user_last_venue_checkin_id";
 	private static final String TAG_USER_LINKEDIN_TOKEN = "tag_user_linkedin_token";
 	private static final String TAG_USER_LINKEDIN_TOKEN_SECRET = "tag_user_linkedin_token_secret";
 	private static final String TAG_USER_LINKEDIN_ID = "tag_user_linkedin_id";
 	private static final String TAG_USER_PHOT_URL = "tag_user_photo_url";
 	private static final String TAG_USER_PHOT_LARGE_URL = "tag_user_photo_large_url";
 	private static final String TAG_LOGGED_IN_USER_ID = "tag_logged_in_user_id";
 	private static final String TAG_LOGGED_IN_USER_NICKNAME = "tag_logged_in_user_nickname";
 	private static final String TAG_USER_COORDINATES = "tag_user_coordinates";
 	private static final String TAG_IS_USER_CHECKED_IN = "tag_is_user_checked_in";
 	private static final String TAG_SHOULD_FINISH_ACTIVITY_MAP = "tag_sgould_finish_activity_map";
 	private static final String TAG_SHOULD_START_LOG_IN = "tag_sgould_start_log_in";
 	private static final String TAG_COOKIE_STRING = "tag_cookie_string";
 	private static final String TAG_METRIC_SYSTEM = "tag_metric_system";
 	private static final String TAG_PUSH_DISTANCE = "tag_push_distance";
 	private static final String TAG_START_LOGIN_PAGE_FROM_CONTACTS = "tag_start_login_page_from_contacts";
 	private static final String TAG_IS_LOGGED_IN = "tag_is_logged_in";
 	private static final String TAG_SCREEN_WIDTH = "tag_screen_width";
 	private static final String TAG_FIRST_START = "tag_first_start";
 	private static final String TAG_INFO_DIALOG = "tag_info_dialog";
 
 	// Notification settings
 	private static final String TAG_NOTIFICATION_FROM = "tag_notification_from";
 	private static final String TAG_NOTIFICATION_TOGGLE = "tag_notification_toggle";
 
 	public static final String URL_WEB_SERVICE = "https://www.candp.me/"; //
 	// production
 	//public static final String URL_WEB_SERVICE = "https://staging.candp.me/"; // staging
 	public static final String URL_FOURSQUARE = "https://api.foursquare.com/v2/venues/search?oauth_token=BCG410DXRKXSBRWUNM1PPQFSLEFQ5ND4HOUTTTWYUB1PXYC4&v=20120302";
 	public static final String FOURSQUARE_OAUTH = "BCG410DXRKXSBRWUNM1PPQFSLEFQ5ND4HOUTTTWYUB1PXYC4";
 	public static final String URL_FUNDS = "http://www.coffeeandpower.com/m/?ios#addFundsiPhone";
 	public static final String URL_LOGIN = "login.php";
 	public static final String URL_LOGOUT = "logout.php";
 	public static final String URL_SIGNUP = "signup.php";
 	public static final String URL_API = "api.php";
 
 	// Activity codes
 	public static final int ACT_CHECK_IN = 1888;
 	public static final int ACT_QUIT = 1333;
 
 	// Http return codes
 	public static final int HTTP_ERROR = 1403;
 	public static final int HTTP_REQUEST_SUCCEEDED = 1404;
 	public static final int ERROR_SUCCEEDED_SHOW_MESS = 1407;
 	
 	// App wide observables
 	
 
 	private static AppCAP instance;
 	private static int mapCenterLng;
 	private static int mapCenterLat;	
 
 	private HttpUtil http;
 	
 	private Counter timingCounter;
 
 	public AppCAP() {
 		instance = this;
 	}
 	/**
 	 * 
 	 * @category viewLifeCycle
 	 */
 	@Override
 	public void onCreate() {
 		
 		// You should not actually see any of the Log.d messages in onCreate() - don't know why
 		if (Constants.debugLog)
 			Log.d("Coffee","AppCAP.onCreate(): ");
 		
 		
 		super.onCreate();
 
 		this.http = new HttpUtil();
 		
 		// Set up Urban Airship and push preferences
 		UAirship.takeOff(this);
 		PushPreferences prefs = PushManager.shared().getPreferences();
 		prefs.setSoundEnabled(true);
 		prefs.setVibrateEnabled(true);
 		
 		
 		
 		// Create app timing Counter
 		if (Constants.debugLog)
 			Log.d("Coffee","Creating counter...");
 		instance.timingCounter = new Counter(10, 1);
 		
 
 		PushManager.enablePush();
 		PushManager.shared().setIntentReceiver(IntentReceiver.class);
 
		PushPreferences prefs = PushManager.shared().getPreferences();
 		if (Constants.debugLog)
 			Log.d("LOG", "Found APID: " + prefs.getPushId());
 
 		// Get country code for metrics/imperial units
 		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 		if (Constants.debugLog)
 			Log.d("LOG", "Locale: " + tm.getSimCountryIso());
 		if (tm.getSimCountryIso() != null && !tm.getSimCountryIso().equals("")) {
 			if (tm.getSimCountryIso().contains("US") || tm.getSimCountryIso().contains("us") || tm.getSimCountryIso().contains("usa")
 					|| tm.getSimCountryIso().contains("um")) {
 				setMetricsSys(false);
 			} else {
 				setMetricsSys(true);
 			}
 		} else {
 			setMetricsSys(false);
 		}
 		
 		
 	}
 
 	
 	
 	/**
 	 * 
 	 * @category sharedResource
 	 */
 	public static HttpUtil getConnection() {
 		return instance.http;
 	}
 	/**
 	 * 
 	 * @category sharedResource
 	 */
 	public static Counter getCounter () {
 		return instance.timingCounter;
 	}
 	/**
 	 * 
 	 * @category counter
 	 */
 	public static void startCounter() {
 		instance.timingCounter.start();
 			
 	}
 	
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	private static SharedPreferences getSharedPreferences() {
 		return instance.getSharedPreferences(AppCAP.TAG, MODE_PRIVATE);
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static boolean isFirstStart() {
 		return getSharedPreferences().getBoolean(TAG_FIRST_START, true);
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setNotFirstStart() {
 		getSharedPreferences().edit().putBoolean(TAG_FIRST_START, false).commit();
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static boolean getEnteredInviteCode() {
 		return getSharedPreferences().getBoolean(TAG_USER_ENTERED_INVITE_CODE, false);
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setEnteredInviteCode() {
 		getSharedPreferences().edit().putBoolean(TAG_USER_ENTERED_INVITE_CODE, true).commit();
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static boolean shouldShowInfoDialog() {
 		return getSharedPreferences().getBoolean(TAG_INFO_DIALOG, true);
 	}
 
 	public static void dontShowInfoDialog() {
 		getSharedPreferences().edit().putBoolean(TAG_INFO_DIALOG, false).commit();
 	}
 
 	/**
 	 * 
 	 * @category globalSetting
 	 */
 	public static boolean isMetrics() {
 		return getSharedPreferences().getBoolean(TAG_METRIC_SYSTEM, false);
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	private void setMetricsSys(boolean set) {
 		getSharedPreferences().edit().putBoolean(TAG_METRIC_SYSTEM, set).commit();
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static String getUserEmail() {
 		return getSharedPreferences().getString(TAG_USER_EMAIL, "");
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setUserEmail(String email) {
 		getSharedPreferences().edit().putString(TAG_USER_EMAIL, email).commit();
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static String getUserEmailPassword() {
 		return getSharedPreferences().getString(TAG_USER_EMAIL_PASSWORD, "");
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setUserEmailPassword(String pass) {
 		getSharedPreferences().edit().putString(TAG_USER_EMAIL_PASSWORD, pass).commit();
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static int getUserLastCheckinVenueId() {
 		return getSharedPreferences().getInt(TAG_USER_LAST_VENUE_CHECKIN_ID, 0);
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setUserLastCheckinVenueId(int venueId) {
 		getSharedPreferences().edit().putInt(TAG_USER_EMAIL_PASSWORD, venueId).commit();
 	}
 	/**
 	 * 
 	 * @category unknown
 	 */
 	public static String cleanResponseString(String data) {
 		String retS = data;
 		data = Html.fromHtml(data).toString();
 
 		try {
 			retS = URLDecoder.decode(data, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (IllegalArgumentException e){
 			e.printStackTrace();
 		}
 		return retS;
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static String getLocalUserPhotoURL() {
 		return getSharedPreferences().getString(TAG_USER_PHOT_URL, "");
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setLocalUserPhotoURL(String url) {
 		getSharedPreferences().edit().putString(TAG_USER_PHOT_URL, url).commit();
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setLocalUserPhotoLargeURL(String url) {
 		getSharedPreferences().edit().putString(TAG_USER_PHOT_LARGE_URL, url).commit();
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static String getLocalUserPhotoLargeURL() {
 		return getSharedPreferences().getString(TAG_USER_PHOT_LARGE_URL, "");
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static String setUserLinkedInToken() {
 		return getSharedPreferences().getString(TAG_USER_LINKEDIN_TOKEN, "");
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static String setUserLinkedInTokenSecret() {
 		return getSharedPreferences().getString(TAG_USER_LINKEDIN_TOKEN_SECRET, "");
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static String setUserLinkedInID() {
 		return getSharedPreferences().getString(TAG_USER_LINKEDIN_ID, "");
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setUserLinkedInDetails(String token, String tokenSecret, String id) {
 		getSharedPreferences().edit().putString(TAG_USER_LINKEDIN_ID, id).commit();
 		getSharedPreferences().edit().putString(TAG_USER_LINKEDIN_TOKEN, token).commit();
 		getSharedPreferences().edit().putString(TAG_USER_LINKEDIN_TOKEN_SECRET, tokenSecret).commit();
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static String getUserLinkedInID() {
 		return getSharedPreferences().getString(TAG_USER_LINKEDIN_ID, "");
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static String getUserLinkedInToken() {
 		return getSharedPreferences().getString(TAG_USER_LINKEDIN_TOKEN, "");
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static String getUserLinkedInTokenSecret() {
 		return getSharedPreferences().getString(TAG_USER_LINKEDIN_TOKEN_SECRET, "");
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static void setLoggedInUserId(int userId) {
 
 		// code will send user ID of zero on logout
 		// if nonzero (login), update the Urban Airship alias and enable push
 		// TODO: add user preferences to control whether to enable push
 		if (userId != 0) {
         		// Register userID as UAirship alias for server-side pushes
         		PushPreferences prefs = PushManager.shared().getPreferences();
         		prefs.setAlias(String.valueOf(userId));
         		        		
         		PushManager.shared().setIntentReceiver(IntentReceiver.class);
         		PushManager.enablePush();
 		}
 		
 		// Save logged in user ID
 		getSharedPreferences().edit().putInt(TAG_LOGGED_IN_USER_ID, userId).commit();
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static int getLoggedInUserId() {
 		return getSharedPreferences().getInt(TAG_LOGGED_IN_USER_ID, 0);
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static String getLoggedInUserNickname() {
 		return getSharedPreferences().getString(TAG_LOGGED_IN_USER_NICKNAME, "");
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static void setLoggedInUserNickname(String nickname) {
 		getSharedPreferences().edit().putString(TAG_LOGGED_IN_USER_NICKNAME, nickname).commit();
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setUserCoordinates(double[] data) {
 		if(data[4]!=0 && data[5]!=0)
 		{
 			getSharedPreferences().edit().putFloat(TAG_USER_COORDINATES + "sw_lat", (float) data[0]).commit();
 			getSharedPreferences().edit().putFloat(TAG_USER_COORDINATES + "sw_lng", (float) data[1]).commit();
 			getSharedPreferences().edit().putFloat(TAG_USER_COORDINATES + "ne_lat", (float) data[2]).commit();
 			getSharedPreferences().edit().putFloat(TAG_USER_COORDINATES + "ne_lng", (float) data[3]).commit();
 			getSharedPreferences().edit().putFloat(TAG_USER_COORDINATES + "user_lat", (float) data[4]).commit();
 			getSharedPreferences().edit().putFloat(TAG_USER_COORDINATES + "user_lng", (float) data[5]).commit();
 		}
 	}
 
 	/**
 	 * data[0] = sw_lat; data[1] = sw_lng; data[2] = ne_lat; data[3] =
 	 * ne_lng; data[4] = user_lat; data[5] = user_lng;
 	 * @category localUserData
 	 */
 	public static double[] getUserCoordinates() {
 		double[] data = new double[6];
 		data[0] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "sw_lat", 0);
 		data[1] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "sw_lng", 0);
 		data[2] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "ne_lat", 0);
 		data[3] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "ne_lng", 0);
 		data[4] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "user_lat", 0);
 		data[5] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "user_lng", 0);
 		return data;
 	}
 	/**
 	 * data[0] = user_lat; data[1] = user_lng;
 	 * @category localUserData
 	 */
 	public static double[] getUserLatLon() {
 		double[] data = new double[2];
 		data[0] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "user_lat", 0);
 		data[1] = (double) getSharedPreferences().getFloat(TAG_USER_COORDINATES + "user_lng", 0);
 		return data;
 	}
 	/**
 	 * 
 	 * @category localUserData
 	 */
 	public static boolean isUserCheckedIn() {
 		return getSharedPreferences().getBoolean(TAG_IS_USER_CHECKED_IN, false);
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setUserCheckedIn(boolean set) {
 		getSharedPreferences().edit().putBoolean(TAG_IS_USER_CHECKED_IN, set).commit();
 	}
 	/**
 	 * 
 	 * @category unknown
 	 */
 	public static boolean shouldFinishActivities() {
 		return getSharedPreferences().getBoolean(TAG_SHOULD_FINISH_ACTIVITY_MAP, false);
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setShouldFinishActivities(boolean set) {
 		getSharedPreferences().edit().putBoolean(TAG_SHOULD_FINISH_ACTIVITY_MAP, set).commit();
 	}
 	/**
 	 * 
 	 * @category unknown
 	 */
 	public static boolean shouldStartLogIn() {
 		return getSharedPreferences().getBoolean(TAG_SHOULD_START_LOG_IN, false);
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setShouldStartLogIn(boolean set) {
 		getSharedPreferences().edit().putBoolean(TAG_SHOULD_START_LOG_IN, set).commit();
 	}
 	/**
 	 * 
 	 * @category unknown
 	 */
 	public static boolean isStartingLoginPageFromContacts() {
 		return getSharedPreferences().getBoolean(TAG_START_LOGIN_PAGE_FROM_CONTACTS, false);
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setStartLoginPageFromContacts(boolean set) {
 		getSharedPreferences().edit().putBoolean(TAG_START_LOGIN_PAGE_FROM_CONTACTS, set).commit();
 	}
 	/**
 	 * 
 	 * @category unknown
 	 */
 	public static String getCookieString() {
 		return getSharedPreferences().getString(TAG_COOKIE_STRING, "");
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setCookieString(String cookie) {
 		getSharedPreferences().edit().putString(TAG_COOKIE_STRING, cookie).commit();
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setLoggedIn(boolean set) {
 		getSharedPreferences().edit().putBoolean(TAG_IS_LOGGED_IN, set).commit();
 	}
 	/**
 	 * 
 	 * @category localUserState
 	 */
 	public static boolean isLoggedIn() {
 		return getSharedPreferences().getBoolean(TAG_IS_LOGGED_IN, false);
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setPushDistance(String dist) {
 		getSharedPreferences().edit().putString(TAG_PUSH_DISTANCE, dist).commit();
 	}
 	/**
 	 * 
 	 * @category utility
 	 */
 	public static String getPushDistance() {
 		return getSharedPreferences().getString(TAG_PUSH_DISTANCE, "city");
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setNotificationFrom(String from) {
 		getSharedPreferences().edit().putString(TAG_NOTIFICATION_FROM, from).commit();
 	}
 	/**
 	 * 
 	 * @category setter
 	 */
 	public static void setNotificationToggle(boolean res) {
 		getSharedPreferences().edit().putBoolean(TAG_NOTIFICATION_TOGGLE, res).commit();
 	}
 	/**
 	 * 
 	 * @category unknown
 	 */
 	public static String getNotificationFrom() {
 		return getSharedPreferences().getString(TAG_NOTIFICATION_FROM, "in city");
 	}
 	/**
 	 * 
 	 * @category unknown
 	 */
 	public static boolean getNotificationToggle() {
 		return getSharedPreferences().getBoolean(TAG_NOTIFICATION_TOGGLE, false);
 	}
 	/**
 	 * 
 	 * @category utility
 	 */
 	public static int getScreenWidth() {
 		return getSharedPreferences().getInt(TAG_SCREEN_WIDTH, 480);
 	}
 	/**
 	 * 
 	 * @category utility
 	 */
 	public static void saveScreenWidth(int screenWidth) {
 		getSharedPreferences().edit().putInt(TAG_SCREEN_WIDTH, screenWidth).commit();
 	}
 	/**
 	 * 
 	 * @category unknown
 	 */
 	public static void logInFile(String data) {
 		try {
 			FileOutputStream fOut = instance.openFileOutput("big_log.txt", MODE_WORLD_READABLE);
 			OutputStreamWriter osw = new OutputStreamWriter(fOut);
 			osw.write(data);
 			osw.flush();
 			osw.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	public static void setMapCenterCoordinates(int lngSpan, int latSpan) {
 		mapCenterLng = lngSpan;
 		mapCenterLat = latSpan;
 	}
 
 	public static double[] getMapCenterLatLon() {
 		double[] data = new double[2];
 		data[0] = (double) mapCenterLat / 1000000;
 		data[1] = (double) mapCenterLng / 1000000;
 		return data;
 	}
 
 }
