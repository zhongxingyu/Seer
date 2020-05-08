 package nz.gen.wellington.penguin.config;
 
 import nz.gen.wellington.penguin.model.Location;
 import nz.gen.wellington.penguin.utils.DateTimeHelper;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class Config {
 
 	private static final int ONE_MINUTE = 60;
 	
 	public static String FEED_URL = "http://www.nzemperor.com/rest/Position.kml";
 	public static final int CACHE_TTL = ONE_MINUTE * 30;
	public static final String SER_VERSION = "1";
 	
 	public static Location releasePoint = new Location(DateTimeHelper.parseUTCDateTime("2011-09-03T22:30:00Z"), 169.4, -51.7);
 	
 	public static final String DARK_RED = "#aa0000";
 	public static final String DARK_GREEN = "#00aa00";
 	public static final String VERY_DARK_GREEN = "#006600";
 	public static final String VERY_DARK_RED = "#660000";

 	
 	public static boolean areUpdatesEnabled(Context context) {
 		PreferenceManager.getDefaultSharedPreferences(context);
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		return prefs != null && prefs.getBoolean("sync", true);
 	}
 
 	public static boolean areAudibleNotificationsEnabled(Context context) {
 		PreferenceManager.getDefaultSharedPreferences(context);
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		return prefs != null && prefs.getBoolean("audible", false);
 	}
 	
 }
