 package fr.quoteBrowser.service;
 
 import android.app.AlarmManager;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class Preferences {
 
 	private SharedPreferences prefs;
 
 	private static Preferences instance;
 
 	private final static String DATABASE_NOTIFICATION_PREFERENCE = "notify_on_database_update_preference";
 	private final static String DATABASE_UPDATE_INTERVAL_PREFERENCE = "database_update_interval_preference";
 	private final static String DISPLAY_CATEGORY_PREFERENCE = "display_category_preference";
 	private final static String QUOTES_PER_PAGE_PREFERENCE = "quotes_per_page_preference";
 	private final static String COLORIZE_USERNAMES_PREFERENCE = "colorize_usernames_preference";
 
 	public static Preferences getInstance(Context context) {
 		if (instance == null) {
 			instance = new Preferences(context);
 		}
 		return instance;
 	}
 
 	private Preferences(Context context) {
 		prefs = PreferenceManager.getDefaultSharedPreferences(context);
 	}
 
 	public void saveDisplayPreference(String value) {
 		prefs.edit().putString(DISPLAY_CATEGORY_PREFERENCE, value).commit();
 	}
 
 	public String getDisplayPreference() {
 		return prefs.getString(DISPLAY_CATEGORY_PREFERENCE, QuoteUtils.PROVIDERS[0].getSource());
 	}
 
 	public long getUpdateIntervalPreference() {
 		return Long.valueOf(prefs.getString(
 				DATABASE_UPDATE_INTERVAL_PREFERENCE,
				String.valueOf(AlarmManager.INTERVAL_DAY)));
 	}
 
 	public boolean databaseNotificationEnabled() {
 		return prefs.getBoolean(DATABASE_NOTIFICATION_PREFERENCE, true);
 	}
 
 	public int getNumberOfQuotesPerPage() {
 		return Integer.valueOf(prefs
 				.getString(QUOTES_PER_PAGE_PREFERENCE, "25"));
 	}
 
 	public boolean colorizeUsernames() {
 		return prefs.getBoolean(COLORIZE_USERNAMES_PREFERENCE, true);
 	}
 
 }
