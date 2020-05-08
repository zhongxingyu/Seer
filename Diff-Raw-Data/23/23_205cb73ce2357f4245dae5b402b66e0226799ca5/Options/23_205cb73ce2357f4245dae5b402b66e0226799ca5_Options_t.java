 package com.slauson.asteroid_dasher.status;
 
 import com.slauson.asteroid_dasher.R;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 
 /**
  * Game configuration
  * @author Josh Slauson
  *
  */
 public class Options {
 	
 	// control methods
 	public static final int CONTROL_TOUCH = 0;
 	public static final int CONTROL_ACCELEROMETER = 1;
 	public static final int CONTROL_KEYBOARD = 2;
 	public static final int CONTROL_DEFAULT = CONTROL_TOUCH;
 	
 	public static final String CONTROL_TOUCH_STRING = "Touch";
 	public static final String CONTROL_ACCELEROMETER_STRING = "Accelerometer";
 	public static final String CONTROL_KEYBOARD_STRING = "Keyboard";
 	
 	// accelerometer sensitivity
 	public static final int ACCELEROMETER_SENSITIVITY_LOW = 0;
 	public static final int ACCELEROMETER_SENSITIVITY_NORMAL = 1;
 	public static final int ACCELEROMETER_SENSITIVITY_HIGH = 2;
 	public static final int ACCELEROMETER_SENSITIVITY_DEFAULT = ACCELEROMETER_SENSITIVITY_NORMAL;
 
 	public static final String ACCELEROMETER_SENSITIVITY_LOW_STRING = "Low";
 	public static final String ACCELEROMETER_SENSITIVITY_NORMAL_STRING = "Normal";
 	public static final String ACCELEROMETER_SENSITIVITY_HIGH_STRING = "High";
 	
 	// player offset
 	public static final int PLAYER_OFFSET_NONE = 0;
 	public static final int PLAYER_OFFSET_SMALL = 1;
 	public static final int PLAYER_OFFSET_NORMAL = 2;
 	public static final int PLAYER_OFFSET_LARGE = 3;
 	public static final int PLAYER_OFFSET_DEFAULT = PLAYER_OFFSET_NORMAL;
 
 	public static final String PLAYER_OFFSET_NONE_STRING = "None";
 	public static final String PLAYER_OFFSET_SMALL_STRING = "Small";
 	public static final String PLAYER_OFFSET_NORMAL_STRING = "Normal";
 	public static final String PLAYER_OFFSET_LARGE_STRING = "Large";
 
 	// graphics settings
 	public static final int GRAPHICS_LOW = 0;
 	public static final int GRAPHICS_NORMAL = 1;
 	public static final int GRAPHICS_DEFAULT = GRAPHICS_NORMAL;
 	
 	public static final String GRAPHICS_NORMAL_STRING = "Normal";
 	public static final String GRAPHICS_LOW_STRING = "Low";
 	
 	// frame rate settings
 	public static final int FRAME_RATE_LOW = 15;
 	public static final int FRAME_RATE_NORMAL = 30;
 	public static final int FRAME_RATE_HIGH = 60;
 	public static final int FRAME_RATE_DEFAULT = FRAME_RATE_NORMAL;
 	
 	public static final String FRAME_RATE_LOW_STRING = "Low";
 	public static final String FRAME_RATE_NORMAL_STRING = "Normal";
 	public static final String FRAME_RATE_HIGH_STRING = "High";
 	
 	/** Game control type **/
 	public static int controlType = CONTROL_DEFAULT;
 	
 	/** Accelerometer sensitivity **/
 	public static int accelerometerSensitivity = ACCELEROMETER_SENSITIVITY_DEFAULT;
 	
 	/** Player offset **/
 	public static int playerOffset = PLAYER_OFFSET_DEFAULT;
 	
 	/** Game graphics **/
 	public static int graphicsType = GRAPHICS_DEFAULT;
 	
 	/** Game frame rate **/
 	public static int frameRate = FRAME_RATE_DEFAULT;
 	
 	/** True if this is free version **/
	public static boolean freeVersion = false;
 	
 	private static boolean initialized = false;
 	
 	/**
 	 * Loads configuration from the given preferences
 	 * @param activity activity to load keys from
 	 * @param preferences preferences to load from
 	 */
 	public static void load(Activity activity, SharedPreferences preferences) {
 		
 		// NOTE: have to store stuff as Strings since we're using a PreferenceActivity
 		controlType = Integer.valueOf(preferences.getString(activity.getString(R.string.configuration_key_controls), "" + CONTROL_DEFAULT));
 		accelerometerSensitivity = Integer.valueOf(preferences.getString(activity.getString(R.string.configuration_key_accelerometer_sensitivity), "" + ACCELEROMETER_SENSITIVITY_DEFAULT));
 		playerOffset = Integer.valueOf(preferences.getString(activity.getString(R.string.configuration_key_player_offset), "" + PLAYER_OFFSET_DEFAULT));
 		graphicsType = Integer.valueOf(preferences.getString(activity.getString(R.string.configuration_key_graphics), "" + GRAPHICS_DEFAULT));
 		frameRate = Integer.valueOf(preferences.getString(activity.getString(R.string.configuration_key_frame_rate), "" + FRAME_RATE_DEFAULT));
 
 		initialized = true;
 	}
 	
 	/**
 	 * Saves options to given preferences editor
 	 * @param activity activity to load keys from
 	 * @param preferencesEditor preferences editor to save to
 	 */
 	public static void save(Activity activity, SharedPreferences.Editor preferencesEditor) {
 		
 		// NOTE: have to store stuff as Strings since we're using a PreferenceActivity
 		preferencesEditor.putString(activity.getString(R.string.configuration_key_controls), "" + controlType);
 		preferencesEditor.putString(activity.getString(R.string.configuration_key_accelerometer_sensitivity), "" + accelerometerSensitivity);
 		preferencesEditor.putString(activity.getString(R.string.configuration_key_player_offset), "" + playerOffset);
 		preferencesEditor.putString(activity.getString(R.string.configuration_key_graphics), "" + graphicsType);
 		preferencesEditor.putString(activity.getString(R.string.configuration_key_frame_rate), "" + frameRate);
 	}
 
 	/**
 	 * Returns true if the options were initialized from application preferences
 	 * @return true if the options were initialized from application preferences
 	 */
 	public static boolean initialized() {
 		return initialized;
 	}
 }
