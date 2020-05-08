 package com.geozen.whiterim.app;
 
 import java.io.IOException;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 import com.geozen.whiterim.error.CredentialsException;
 import com.geozen.whiterim.error.SmartTrailException;
 import com.geozen.whiterim.util.AppLog;
import com.geozen.whiterim.app.Config;
 
 public class Preferences {
 	private static final String CLASSTAG = Preferences.class.getSimpleName();
 	public static final String PREFS_NAME = "com.geozen.whiterim.app.prefs";
 
 	// Credentials related preferences
 	public static final String PREF_USERNAME = "username";
 	public static final String PREF_PASSWORD = "password";
 	public static final String DEFAULT_PASSWORD = "";
 
 	public static final String PREF_TRAIL = "trail";
 	public static final String DEFAULT_TRAIL = "4de968ee776c37207c3746a5";
 
 	public static final String PREF_TRAIL_POSITION = "trailposition";
 	public static final int DEFAULT_TRAIL_POSITION = 5;
 
 	private static final String PREF_REGION = "region";
 	private static final String PREF_REGION_NAME = "regionName";
 	public static final String PREF_AREA = "area";
	
 
 	public static final String PREF_NICKNAME = "nickname";
 
 	public static final String PREF_SIGNEDIN_AT = "signedinat";
 
 	// Bike Patrol
 	private static final String PREF_PATROL_NUM_BIKERS = "patrolNumBikers";
 	private static final String PREF_PATROL_NUM_HIKERS = "patrolNumHikers";
 	private static final String PREF_PATROL_NUM_RUNNERS = "patrolNumRunners";
 	private static final String PREF_PATROL_NUM_DOGS_LEASHED = "patrolNumDogsLeashed";
 	private static final String PREF_PATROL_NUM_DOGS_UNLEASHED = "patrolNumDogsUnLeashed";
 	private static final String PREF_PATROL_NUM_EQUESTRIANS = "patrolNumEquestrians";
 	private static final String PREF_PATROL_NUM_ANGLERS = "patrolNumAnglers";
 	private static final String PREF_IS_PATROLLER = "isPatroller";
 	private static final String PREF_SPONSOR_NAME = null;
 	private static final String PREF_SPONSOR_ALERTS_URL = null;
 	private static final String PREF_SPONSOR_TWITTER_QUERY = null;
 
 	public static CharSequence getNickname(SharedPreferences prefs) {
 		return prefs.getString(PREF_NICKNAME, null);
 	}
 
 	public static CharSequence getUsername(SharedPreferences prefs) {
 		return prefs.getString(PREF_USERNAME, null);
 	}
 
 	public static CharSequence getPassword(SharedPreferences prefs) {
 		return prefs.getString(PREF_PASSWORD, DEFAULT_PASSWORD);
 	}
 
 	public static String getArea(SharedPreferences prefs) {
		return prefs.getString(PREF_AREA, Config.DEFAULT_AREA);
 	}
 
 	public static String getTrail(SharedPreferences prefs) {
 		return prefs.getString(PREF_TRAIL, DEFAULT_TRAIL);
 	}
 
 	public static int getTrailPosition(SharedPreferences prefs) {
 		return prefs.getInt(PREF_TRAIL_POSITION, DEFAULT_TRAIL_POSITION);
 	}
 
 	public static boolean storeUsernamePassword(String username,
 			String password, Editor editor) throws CredentialsException,
 			SmartTrailException, IOException {
 		AppLog.d(CLASSTAG, "Trying to log in.");
 
 		putUsernameAndPassword(editor, username, password);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "storeLoginAndPassword commit failed");
 			return false;
 		}
 
 		return true;
 	}
 
 	public static boolean logoutUser(Editor editor) {
 		return logoutUser(editor, true, true);
 	}
 
 	public static boolean logoutUser(Editor editor, boolean clearUsername,
 			boolean clearPassword) {
 		AppLog.d(CLASSTAG, "Signing out.");
 
 		if (clearUsername) {
 			putUsername(editor, null);
 		}
 		if (clearPassword) {
 			putPassword(editor, null);
 		}
 		return editor.commit();
 	}
 
 	public static void putUsernameAndPassword(final Editor editor,
 			String login, String password) {
 		editor.putString(PREF_USERNAME, login);
 		editor.putString(PREF_PASSWORD, password);
 	}
 
 	public static void putUsername(final Editor editor, String username) {
 		editor.putString(PREF_USERNAME, username);
 	}
 
 	public static void putPassword(final Editor editor, String password) {
 		editor.putString(PREF_PASSWORD, password);
 	}
 
 	public static void putNickname(final Editor editor, String nickname) {
 		editor.putString(PREF_NICKNAME, nickname);
 	}
 
 	public static void putTrail(final Editor editor, String trail) {
 		editor.putString(PREF_TRAIL, trail);
 	}
 
 	public static void putArea(final Editor editor, String area) {
 		editor.putString(PREF_AREA, area);
 	}
 
 	public static boolean storeArea(Editor editor, String area) {
 		editor.putString(PREF_AREA, area);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store area commit failed");
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean storeTrail(Editor editor, String trail) {
 		editor.putString(PREF_TRAIL, trail);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store trail commit failed");
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean storeTrailPosition(Editor editor, int trailPosition) {
 		editor.putInt(PREF_TRAIL_POSITION, trailPosition);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store trail commit failed");
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean storeSignedinAt(Editor editor, long signedinat) {
 		editor.putLong(PREF_SIGNEDIN_AT, signedinat);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store signed in commit failed");
 			return false;
 		}
 		return true;
 	}
 
 	public static boolean storePatrolNumBikers(Editor editor, int numBikers) {
 		editor.putInt(PREF_PATROL_NUM_BIKERS, numBikers);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store patrol num bikers  commit failed");
 			return false;
 		}
 		return true;
 
 	}
 
 	public static int getPatrolNumBikers(SharedPreferences prefs) {
 		return prefs.getInt(PREF_PATROL_NUM_BIKERS, 0);
 	}
 
 	public static boolean storePatrolNumHikers(Editor editor, int numHikers) {
 		editor.putInt(PREF_PATROL_NUM_HIKERS, numHikers);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store patrol num hikers  commit failed");
 			return false;
 		}
 		return true;
 
 	}
 
 	public static int getPatrolNumHikers(SharedPreferences prefs) {
 		return prefs.getInt(PREF_PATROL_NUM_HIKERS, 0);
 	}
 
 	public static boolean storePatrolNumDogsUnleashed(Editor editor, int numDogs) {
 		editor.putInt(PREF_PATROL_NUM_DOGS_UNLEASHED, numDogs);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store patrol num dogs unleashed  commit failed");
 			return false;
 		}
 		return true;
 
 	}
 
 	public static int getPatrolNumDogsUnleashed(SharedPreferences prefs) {
 		return prefs.getInt(PREF_PATROL_NUM_DOGS_UNLEASHED, 0);
 	}
 
 	public static boolean storePatrolNumDogsLeashed(Editor editor, int numDogs) {
 		editor.putInt(PREF_PATROL_NUM_DOGS_LEASHED, numDogs);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store patrol num dogs leashed  commit failed");
 			return false;
 		}
 		return true;
 
 	}
 
 	public static int getPatrolNumDogsLeashed(SharedPreferences prefs) {
 		return prefs.getInt(PREF_PATROL_NUM_DOGS_LEASHED, 0);
 	}
 
 	public static boolean storePatrolNumRunners(Editor editor, int numRunners) {
 		editor.putInt(PREF_PATROL_NUM_RUNNERS, numRunners);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store patrol num runners  commit failed");
 			return false;
 		}
 		return true;
 
 	}
 
 	public static int getPatrolNumRunners(SharedPreferences prefs) {
 		return prefs.getInt(PREF_PATROL_NUM_RUNNERS, 0);
 	}
 
 	public static boolean storePatrolNumEquestrians(Editor editor,
 			int numEquestrians) {
 		editor.putInt(PREF_PATROL_NUM_EQUESTRIANS, numEquestrians);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store patrol num runners  commit failed");
 			return false;
 		}
 		return true;
 
 	}
 
 	public static int getPatrolNumEquestrians(SharedPreferences prefs) {
 		return prefs.getInt(PREF_PATROL_NUM_EQUESTRIANS, 0);
 	}
 
 	public static boolean storePatrolNumAnglers(Editor editor, int numAnglers) {
 		editor.putInt(PREF_PATROL_NUM_ANGLERS, numAnglers);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store patrol num runners  commit failed");
 			return false;
 		}
 		return true;
 
 	}
 
 	public static int getPatrolNumAnglers(SharedPreferences prefs) {
 		return prefs.getInt(PREF_PATROL_NUM_ANGLERS, 0);
 	}
 
 	public static boolean storeIsPatroller(Editor editor, boolean isPatroller) {
 		editor.putBoolean(PREF_IS_PATROLLER, isPatroller);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store patrol num runners  commit failed");
 			return false;
 		}
 		return true;
 
 	}
 
 	public static boolean getIsPatroller(SharedPreferences prefs) {
 		return prefs.getBoolean(PREF_IS_PATROLLER, false);
 	}
 
 	public static String getRegion(SharedPreferences prefs) {
 		return prefs.getString(PREF_REGION, Config.DEFAULT_REGION);
 	}
 
 	public static boolean storeRegion(Editor editor, String regionId) {
 		editor.putString(PREF_REGION, regionId);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store region failed");
 			return false;
 		}
 		return true;
 	}
 
 	public static String getRegionName(SharedPreferences prefs) {
 		return prefs.getString(PREF_REGION_NAME, "");
 	}
 
 	public static boolean storeRegionName(Editor editor, String regionName) {
 		editor.putString(PREF_REGION_NAME, regionName);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store region name failed");
 			return false;
 		}
 		return true;
 	}
 
 	public static String getSponsorName(SharedPreferences prefs) {
 		return prefs.getString(PREF_SPONSOR_NAME, "");
 	}
 
 	public static boolean storeSponsorName(Editor editor, String name) {
 		editor.putString(PREF_SPONSOR_NAME, name);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store sponsor name failed");
 			return false;
 		}
 		return true;
 	}
 
 	public static String getSponsorAlertsUrl(SharedPreferences prefs) {
 		return prefs.getString(PREF_SPONSOR_ALERTS_URL, "");
 	}
 
 	public static boolean storeSponsorAlertsUrl(Editor editor, String url) {
 		editor.putString(PREF_SPONSOR_ALERTS_URL, url);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store sponsor alerts url failed");
 			return false;
 		}
 		return true;
 	}
 
 	public static String getSponsorTwitterQuery(SharedPreferences prefs) {
 		return prefs.getString(PREF_SPONSOR_TWITTER_QUERY, "");
 	}
 
 	public static boolean storeSponsorTwitterQuery(Editor editor, String url) {
 		editor.putString(PREF_SPONSOR_TWITTER_QUERY, url);
 		if (!editor.commit()) {
 			AppLog.d(CLASSTAG, "store sponsor twitter query failed");
 			return false;
 		}
 		return true;
 	}
 }
