 /*******************************************************************
  * Copyright (c) 2010 Rohit Kumbhar
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  *******************************************************************/
 package net.yama.android.managers.config;
 
 import net.yama.android.Rendezvous;
 import net.yama.android.managers.connection.ConnectionManagerFactory.ConnectionType;
 import net.yama.android.util.Constants;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 /**
  * Provides storage and access methods for configuration parameters
  * @author Rohit Kumbhar
  */
 public class ConfigurationManager {
 
 	private static final String ONE_HOUR_IN_MS = "3600000";
 	public static ConfigurationManager instance;
 	private Context activity;
 
 	private int logFilesCount;
 	private String currentVersion;
 	private String apiKey;
 	private String accessToken;
 	private String accessTokenSecret;
 	private String memberId;
 	private String requestBeforePeriod;
 	private String requestAfterPeriod;
 	private int defaultStartupTab;
 	private String tempImageStoragePath;
 	private String cachingEnabled;
 	private String reminderCalendarId;
 	private boolean notificationsFlag;
 	private long notificationsCheckInterval;
 	private String newEventsList;
 	private boolean showWhatsNewDialog;
 	private static SharedPreferences prefs;
 	private static SharedPreferences defaultPrefs;
 	
 	
 	private ConfigurationManager() {
 	}
 
 	public static ConfigurationManager init(Context yama) {
 		instance = new ConfigurationManager();
 		instance.activity = yama;
 		setupPreferenceFiles(yama);
 		loadConfiguration();
 		return instance;
 	}
 
 	private static void setupPreferenceFiles(final Context yama) {
 		prefs = instance.activity.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE);
 		defaultPrefs = PreferenceManager.getDefaultSharedPreferences(yama);
 	}
 
 	public static void loadConfiguration() {
 		
 		instance.apiKey = prefs.getString(Constants.API_KEY, null);
 		instance.accessToken = prefs.getString(Constants.ACCESS_TOKEN, null);
 		instance.accessTokenSecret = prefs.getString(Constants.ACCESS_TOKEN_SECRET, null);
 		instance.memberId = prefs.getString(Constants.MEMBER_ID, null);
 		instance.newEventsList = prefs.getString(Constants.NEW_EVENTS_LIST, null);
 		instance.defaultStartupTab = Integer.valueOf(defaultPrefs.getString(Constants.STARTUP_TAB, "0"));
 		instance.tempImageStoragePath = prefs.getString(Constants.TEMP_IMAGE_FILE_PATH, null);
 		instance.requestAfterPeriod = defaultPrefs.getString(Constants.FETCH_EVENTS_FROM_PREF_KEY, Constants.DEFAULT_BEFORE_PERIOD);
 		instance.requestBeforePeriod = defaultPrefs.getString(Constants.FETCH_EVENTS_TO_PREF_KEY, Constants.DEFAULT_AFTER_PERIOD);
 		instance.cachingEnabled = defaultPrefs.getString(Constants.IS_CACHING_ENABLED, "false");
 		instance.reminderCalendarId = defaultPrefs.getString(Constants.REMINDER_CAL_ID_KEY, null);
 		instance.currentVersion = defaultPrefs.getString(Constants.CURRENT_VERSION, null);
 		instance.notificationsFlag = defaultPrefs.getBoolean(Constants.NOTIFICATION_FLAG, false);
 		instance.notificationsCheckInterval = Long.parseLong(defaultPrefs.getString(Constants.NOTIFICATIONS_CHECK_INTERVAL, ONE_HOUR_IN_MS));
 		instance.logFilesCount = defaultPrefs.getInt(Constants.LOG_FILE_COUNT, 0);
 		instance.showWhatsNewDialog = defaultPrefs.getBoolean(Constants.SHOW_WHATS_NEW_DIALOG, true);
 	}
 
 	public String getApiKey() {
 		return apiKey;
 	}
 
 	public void saveApiKey(String apiKey) {
 		addPairToPrefs(Constants.API_KEY, apiKey);
 		this.apiKey = apiKey;
 	}
 
 	public String getAccessToken() {
 		return accessToken;
 	}
 
 	public void saveAccessToken(String accessToken) {
 		addPairToPrefs(Constants.ACCESS_TOKEN, accessToken);
 		this.accessToken = accessToken;
 	}
 	
 	public String getAccessTokenSecret() {
 		return accessTokenSecret;
 	}
 
 	public void saveAccessTokenSecret(String accessTokenSecret) {
 		addPairToPrefs(Constants.ACCESS_TOKEN_SECRET, accessTokenSecret);
 		this.accessTokenSecret = accessTokenSecret;
 	}
 
 	public String getMemberId() {
 		return memberId;
 	}
 
 	public void saveMemberId(String memberId) {
 		addPairToPrefs(Constants.MEMBER_ID, memberId);
 		this.memberId = memberId;
 	}
 
 	private void addPairToPrefs(String key, String value) {
 		SharedPreferences prefs = activity.getSharedPreferences(Constants.PREF_FILE_NAME, Context.MODE_PRIVATE);
 		SharedPreferences.Editor editor = prefs.edit();
 		editor.putString(key, value);
 		editor.commit();
 	}
 
 	public boolean haveAcess() {
 		return (this.accessToken != null);
 	}
 
 	public ConnectionType getConnectionType() {
 
 		if (this.accessToken != null)
 			return ConnectionType.OAUTH;
 		else if (this.apiKey != null)
 			return ConnectionType.APIKEY;
 		
 		return null;
 	}
 
 	public String getRequestAfterPeriod() {
 		return requestAfterPeriod;
 	}
 	
 	public String getRequestBeforePeriod() {
 		return requestBeforePeriod;
 	}
 
 	public void saveRequestAfterPeriod(String requestAfterPeriod){
 		addPairToPrefs(Constants.REQUEST_AFTER_PERIOD_KEY, requestAfterPeriod);
 		this.requestAfterPeriod = requestAfterPeriod;
 	}
 
 	public int getDefaultStartupTab() {
 		return defaultStartupTab;
 	}
 
 	public void nuke() {
 		saveApiKey(null);
 		saveAccessToken(null);
 		saveAccessTokenSecret(null);
 		saveMemberId(null);
 		SharedPreferences.Editor editor = defaultPrefs.edit();
 		editor.clear();
 		editor.commit();
 	}
 
 	public String getTempImageStoragePath() {
 		return tempImageStoragePath;
 	}
 
 	public void saveTempImageStoragePath(String tempImageStoragePath) {
 		addPairToPrefs(Constants.TEMP_IMAGE_FILE_PATH,tempImageStoragePath);
 		this.tempImageStoragePath = tempImageStoragePath;
 	}
 
 	public int getImageThreads() {
 		return 3;
 	}
 
 	public String getCachingEnabled() {
 		return cachingEnabled;
 	}
 	
 	public String getReminderCalendarId() {
 		return reminderCalendarId;
 	}
 
 	public String getCurrentVersion() {
 		return currentVersion;
 	}
 	
 	public boolean isNotificationsFlag() {
 		return notificationsFlag;
 	}
 	
 	public int getLogFilesCount(){
 		return logFilesCount;
 	}
 	
 	public void setLogFilesCount(int count){
 		this.logFilesCount = count;
 		SharedPreferences.Editor editor = defaultPrefs.edit();
 		editor.putInt(Constants.LOG_FILE_COUNT, logFilesCount);
 		editor.commit();
 	}
 	
 	public void setCurrentVersion(String currentVersion) {
 		this.currentVersion = currentVersion;
 		SharedPreferences.Editor editor = defaultPrefs.edit();
 		editor.putString(Constants.CURRENT_VERSION, currentVersion);
 		editor.commit();
 	}
 
 	public long getNotificationsCheckInterval() {
 		return notificationsCheckInterval;
 	}
 	
 	public void setNewEventList(String newEventsList){
 		this.newEventsList = newEventsList;
 		addPairToPrefs(Constants.NEW_EVENTS_LIST, newEventsList);
 	}
 	
 	public String getNewEventsList(){
 		return this.newEventsList;
 	}
 	
 	public boolean showWhatsNewDialog(){
 		return showWhatsNewDialog;
 	}
 	
 	public void disableShowWhatsNewDialog(){
 		this.showWhatsNewDialog = false;
 		SharedPreferences.Editor editor = defaultPrefs.edit();
 		editor.putBoolean(Constants.SHOW_WHATS_NEW_DIALOG, false);
 		editor.commit();
	}
	
	public void enableShowWhatsNewDialog(){
		this.showWhatsNewDialog = false;
		SharedPreferences.Editor editor = defaultPrefs.edit();
		editor.putBoolean(Constants.SHOW_WHATS_NEW_DIALOG, true);
		editor.commit();
 	}
 }
