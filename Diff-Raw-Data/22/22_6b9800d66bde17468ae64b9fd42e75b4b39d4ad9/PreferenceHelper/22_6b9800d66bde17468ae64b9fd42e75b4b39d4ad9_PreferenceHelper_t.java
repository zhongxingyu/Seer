 /**
  * 
  */
 package com.madhackerdesigns.neverlate;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 /**
  * @author flintinatux
  *
  */
 public class PreferenceHelper {
 
 	// Notification methods
	protected enum NotificationMethod { ALERT, STATUS_BAR_ONLY }
 	
 	// Default preference values
 	private static final long 		DEFAULT_ADVANCE_WARNING = 900000;
 	private static final boolean 	DEFAULT_AVOID_HIGHWAYS = false;
 	private static final boolean	DEFAULT_AVOID_TOLLS = false;
 	private static final boolean 	DEFAULT_FINE_REQUIRED = false;
 	private static final boolean 	DEFAULT_INSISTENT = false;
 	private static final long 		DEFAULT_LOCATION_FREQ = 300000;
 	private static final long	 	DEFAULT_LOOKAHEAD_WINDOW = 7200000;
 	private static final boolean 	DEFAULT_NEVERLATE_ENABLED = false;
	private static final String	 	DEFAULT_NOTIFICATION_METHOD = "ALERT";
 	private static final long 		DEFAULT_SNOOZE_DURATION = 300000;
 	private static final boolean 	DEFAULT_TOS_ACCEPTED = false;
 	private static final String 	DEFAULT_TRAVEL_MODE = "driving";
 	private static final long 		DEFAULT_TRAVELTIME_FREQ = 90000;
 	private static final boolean	DEFAULT_VIBRATE = true;
 	private static final boolean 	DEFAULT_WARN_NO_LOCATION = false;
 	
 	// Preference keys
 	private final static String KEY_ADVANCE_WARNING = "advance_warning";
 	private final static String KEY_AVOID_HIGHWAYS = "avoid_highways";
 	private final static String KEY_AVOID_TOLLS = "avoid_tolls";
 	private final static String KEY_FINE_REQUIRED = "fine_required";
 	private final static String KEY_INSISTENT = "insistent";
 	private final static String KEY_LOCATION_FREQ = "location_freq";
 	private final static String KEY_LOOKAHEAD_WINDOW = "lookahead_window";
 	private final static String KEY_NEVERLATE_ENABLED = "neverlate_enabled";
 	private final static String KEY_NOTIFICATION_METHOD = "notification_method";
 	private final static String KEY_SNOOZE_DURATION = "snooze_duration";
 	private final static String KEY_TOS_ACCEPTED = "tos_accepted";
 	private final static String KEY_TRAVEL_MODE = "travel_mode";
 	private final static String KEY_TRAVELTIME_FREQ = "traveltime_freq";
 	private final static String KEY_VIBRATE = "vibrate";
 	private final static String KEY_WARN_NO_LOCATION = "warn_no_location";
 
 	// Private fields
 	private SharedPreferences mPrefs;
 	
 	public PreferenceHelper(Context applicationContext) {
 		mPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext);
 	}
 
 	/**
 	 * @return the avoidHighways
 	 */
 	public boolean isAvoidHighways() {
 		return mPrefs.getBoolean(KEY_AVOID_HIGHWAYS, DEFAULT_AVOID_HIGHWAYS);
 	}
 
 	/**
 	 * @return the avoidTolls
 	 */
 	public boolean isAvoidTolls() {
 		return mPrefs.getBoolean(KEY_AVOID_TOLLS, DEFAULT_AVOID_TOLLS);
 	}
 
 	/**
 	 * @return the advanceWarning
 	 */
 	public long getAdvanceWarning() {
 		return mPrefs.getLong(KEY_ADVANCE_WARNING, DEFAULT_ADVANCE_WARNING);
 	}
 
 	/**
 	 * @return the fineRequired
 	 */
 	public boolean isFineRequired() {
 		return mPrefs.getBoolean(KEY_FINE_REQUIRED, DEFAULT_FINE_REQUIRED);
 	}
 
 	/**
 	 * @return the insistent
 	 */
 	public boolean isInsistent() {
 		return mPrefs.getBoolean(KEY_INSISTENT, DEFAULT_INSISTENT);
 	}
 	
 	/**
 	 * @return the location polling frequency
 	 */
 	public long getLocationFreq() {
 		return mPrefs.getLong(KEY_LOCATION_FREQ, DEFAULT_LOCATION_FREQ);
 	}
 
 	/**
 	 * @return the lookaheadWindow
 	 */
 	public long getLookaheadWindow() {
 		return  mPrefs.getLong(KEY_LOOKAHEAD_WINDOW, DEFAULT_LOOKAHEAD_WINDOW);
 	}
 
 	/**
 	 * @return the neverLateEnabled
 	 */
 	public boolean isNeverLateEnabled() {
 		return mPrefs.getBoolean(KEY_NEVERLATE_ENABLED, DEFAULT_NEVERLATE_ENABLED);
 	}
 
 	/**
 	 * @param neverLateEnabled the neverLateEnabled to set
 	 * 
 	 * @return true if NeverLate successfully enabled or disabled
 	 */
 	public boolean setNeverLateEnabled(boolean neverLateEnabled) {
 		SharedPreferences.Editor editor = mPrefs.edit();
 		return editor.putBoolean(KEY_NEVERLATE_ENABLED, neverLateEnabled).commit();
 	}
 
 	/**
 	 * @return the notificationMethod
 	 */
	public NotificationMethod getNotificationMethod() {
		return Enum.valueOf(NotificationMethod.class, 
				mPrefs.getString(KEY_NOTIFICATION_METHOD, DEFAULT_NOTIFICATION_METHOD));
 	}
 
 	/**
 	 * @return the snoozeDuration
 	 */
 	public long getSnoozeDuration() {
 		return mPrefs.getLong(KEY_SNOOZE_DURATION, DEFAULT_SNOOZE_DURATION);
 	}
 
 	/**
 	 * @return the tosAccepted
 	 */
 	public boolean isTosAccepted() {
 		return mPrefs.getBoolean(KEY_TOS_ACCEPTED, DEFAULT_TOS_ACCEPTED);
 	}
 
 	/**
 	 * @param tosAccepted the tosAccepted to set
 	 * 
 	 * @return true if the TOS Accepted flag is successfully set
 	 */
 	public boolean setTosAccepted(boolean tosAccepted) {
 		SharedPreferences.Editor editor = mPrefs.edit();
 		return editor.putBoolean(KEY_TOS_ACCEPTED, tosAccepted).commit();
 	}
 
 	/**
 	 * @return the travelMode
 	 */
 	public String getTravelMode() {
 		return mPrefs.getString(KEY_TRAVEL_MODE, DEFAULT_TRAVEL_MODE);
 	}
 
 	/**
 	 * @return the traveltimeFreq
 	 */
 	public long getTraveltimeFreq() {
 		return mPrefs.getLong(KEY_TRAVELTIME_FREQ, DEFAULT_TRAVELTIME_FREQ);
 	}
 
 	/**
 	 * @return the vibrate
 	 */
 	public boolean isVibrate() {
 		return mPrefs.getBoolean(KEY_VIBRATE, DEFAULT_VIBRATE);
 	}
 
 	/**
 	 * @return the warnNoLocation
 	 */
 	public boolean isWarnNoLocation() {
 		return mPrefs.getBoolean(KEY_WARN_NO_LOCATION, DEFAULT_WARN_NO_LOCATION);
 	}
 }
