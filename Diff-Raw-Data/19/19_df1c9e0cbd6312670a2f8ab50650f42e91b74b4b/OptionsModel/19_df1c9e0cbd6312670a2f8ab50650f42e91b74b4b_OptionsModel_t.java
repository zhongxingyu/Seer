 /**
  * Helper classes
  */
 package com.japtar.chessclock.models;
 
 import com.japtar.chessclock.enums.DelayMode;
 
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.provider.Settings;
 
 /**
  * Options shared between each layout
  * @author japtar10101
  */
 public class OptionsModel implements SaveStateModel {
 	/* ===========================================================
 	 * Constants
 	 * =========================================================== */
 	// == Stored key values ==
 	// == White Player's time ==
 	/** The saved key value for time limit */
 	public static final String KEY_WHITE_TIME_LIMIT = "timeLimit";
 	/** The saved key value for delay time */
 	public static final String KEY_WHITE_DELAY_TIME = "delayTime";
 
 	// == Black Player's time ==
 //	/** The saved key value for time limit */
 //	public static final String KEY_BLACK_TIME_LIMIT = "timeLimitBlack";
 //	/** The saved key value for delay time */
 //	public static final String KEY_BLACK_DELAY_TIME = "delayTimeBlack";
 	
 	// == Feedback ==
 	/** The saved key value for alarm */
 	public static final String KEY_ALARM = "alarm";
 	/** The saved key value for enabling click sound */
 	public static final String KEY_CLICK = "click";
 	/** The saved key value for vibrating */
 	public static final String KEY_VIBRATE = "vibrate";
 	
 	// == Advanced Options ==
 //	/** The saved key value for enabling handicap */
 //	public static final String KEY_HANDICAP = "enableHandicap";
 	/** The saved key value for the list of delay modes */
 	public static final String KEY_DELAY_MODE = "delayMode";
 //	/** The saved key value for allowing the timer to continue after
 //	 * a player expires */
 //	public static final String KEY_NEGATIVE = "enableNegative";
 //	/** The saved key value for enabling penalties */
 //	public static final String KEY_PENALTY_ENABLED = "enablePenalty";
 //	/** The saved key value for the list of delay modes */
 //	public static final String KEY_PENALTY_TIME = "penaltyTime";
 	
 	// == Default values ==
 	// == Player's time ==
 	/** The default time limit */
 	public static final int DEFAULT_TIME_LIMIT = 300;
 	/** The default delay time */
 	public static final byte DEFAULT_DELAY_TIME = 2;
 
 	// == Feedback ==
 	/** The default value for {@link #enableClick} */
 	public static final boolean DEFAULT_ENABLE_CLICK = true;
 	/** The default value for {@link #enableVibrate} */
 	public static final boolean DEFAULT_ENABLE_VIBRATE = true;
 	
 	// == Advanced Options ==
 //	/** The default value for {@link #enableHandicap} */
 //	public static final boolean DEFAULT_ENABLE_HANDICAP = false;
 //	/** The default value for {@link #enableHandicap} */
 //	public static final boolean DEFAULT_ENABLE_NEGATIVES = false;
 //	/** The default value for {@link #enablePenality} */
 //	public static final boolean DEFAULT_ENABLE_PENALTY = false;
 //	/** The default penalty */
 //	public static final int DEFAULT_PENALTY_TIME = 60;
 	
 	/* ===========================================================
 	 * Members
 	 * =========================================================== */
 	// == White player's time ==
 	/** The white player's time limit */
 	public final TimeModel savedWhiteTimeLimit = new TimeModel(DEFAULT_TIME_LIMIT);
 	/** The white player's delay time */
 	public final TimeModel savedWhiteDelayTime = new TimeModel(DEFAULT_DELAY_TIME);
 	
 	// == Black player's time ==
 //	/** The black player's time limit */
 //	public final TimeModel savedBlackTimeLimit = new TimeModel(DEFAULT_TIME_LIMIT);
 //	/** The black player's delay time */
 //	public final TimeModel savedBlackDelayTime = new TimeModel(DEFAULT_DELAY_TIME);
 	
 	// == Alarm ==
 	/** The stored alarm */
 	public Uri alarmUri = null;
 	
 	// == Feedback ==
 	/** If true, make a sound when pressing a game button */
 	public boolean enableClick = DEFAULT_ENABLE_CLICK;
 	/** If true, vibrates when clicking any button */
 	public boolean enableVibrate = DEFAULT_ENABLE_VIBRATE;
 	
 	// == Advanced Options ==
 //	/** If true, the white and black player's options will be used respectively.
 //	 * Otherwise, only the white player's options will be used for both players. */
 //	public boolean enableHandicap = DEFAULT_ENABLE_HANDICAP;
 	/** The delay mode for this game */
 	public byte delayMode = DelayMode.BASIC;
 //	/** If true, when a player expires, the displayed values will continue to
 //	 * decrement, displaying negative values. Otherwise, the timer stops at
 //	 * the moment a player expires. */
 //	public boolean enableNegatives = DEFAULT_ENABLE_NEGATIVES;
 //	/** If true, when the other player clicks the game button while <i>not</i>
 //	 * in his or her turn, a penality is applied, and the opposing player gains
 //	 * some amount of time. */
 //	public boolean enablePenality = DEFAULT_ENABLE_PENALTY;
 //	/** The black player's delay time */
 //	public final TimeModel penalityTime = new TimeModel(DEFAULT_PENALTY_TIME);
 	
 	/* ===========================================================
 	 * Override Methods
 	 * =========================================================== */
 	/**
 	 * Saves the current options state to a bundle
 	 * @see com.japtar.chessclock.models.SaveStateModel#recallSettings(android.content.SharedPreferences)
 	 */
 	@Override
 	public void recallSettings(final SharedPreferences savedState) throws
 			IllegalArgumentException {
 		// Make sure the parameter isn't null
 		if(savedState == null) {
 			// If so, complain
 			throw new IllegalArgumentException("savedState cannot be null");
 		}
 		
 		// Recall the white player attributes to the bundle
 		savedWhiteTimeLimit.recallTime(savedState, KEY_WHITE_TIME_LIMIT,
 				DEFAULT_TIME_LIMIT);
 		savedWhiteDelayTime.recallTime(savedState, KEY_WHITE_DELAY_TIME,
 				DEFAULT_DELAY_TIME);
 		
 		// Recall the black player attributes to the bundle
 //		savedBlackTimeLimit.recallTime(savedState, KEY_BLACK_TIME_LIMIT,
 //				DEFAULT_TIME_LIMIT);
 //		savedBlackDelayTime.recallTime(savedState, KEY_BLACK_DELAY_TIME,
 //				DEFAULT_DELAY_TIME);
 		
 		// Recall the alarm
 		String savedString = savedState.getString(KEY_ALARM, null);
 		alarmUri = null;
 		if((savedString != null) && (savedString.length() > 0)) {
 			if(savedString.equals("defaultRingtone")) {
 				alarmUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
 				this.saveSettings(savedState);
 			} else {
 				alarmUri = Uri.parse(savedString);
 			}
 		}
 		
 		// Recall feedback-related conditionals
 		enableClick = savedState.getBoolean(KEY_CLICK, DEFAULT_ENABLE_CLICK);
 		enableVibrate = savedState.getBoolean(KEY_VIBRATE,
 				DEFAULT_ENABLE_VIBRATE);
 		
 		// Recall advanced options
 //		enableHandicap = savedState.getBoolean(KEY_HANDICAP,
 //				DEFAULT_ENABLE_HANDICAP);
 //		enableNegatives = savedState.getBoolean(KEY_NEGATIVE,
 //				DEFAULT_ENABLE_NEGATIVES);
 //		enablePenality = savedState.getBoolean(KEY_PENALTY_ENABLED,
 //				DEFAULT_ENABLE_PENALTY);
 //		penalityTime.recallTime(savedState, KEY_PENALTY_TIME,
 //				DEFAULT_PENALTY_TIME);
 		
 		// Recall the game mode
 		savedString = savedState.getString(KEY_DELAY_MODE, DelayMode.STRING_BASIC);
 		if(savedString.equals(DelayMode.STRING_BRONSTEIN)) {
 			delayMode = DelayMode.BRONSTEIN;
 		} else if(savedString.equals(DelayMode.STRING_FISCHER)) {
 			delayMode = DelayMode.FISCHER;
 		} else if(savedString.equals(DelayMode.STRING_FISCHER_AFTER)) {
 			delayMode = DelayMode.FISCHER_AFTER;
 		} else if(savedString.equals(DelayMode.STRING_HOUR_GLASS)) {
 			delayMode = DelayMode.HOUR_GLASS;
 		} else {
 			delayMode = DelayMode.BASIC;
 		}
 	}
 
 	/**
 	 * Saves options
 	 * @see com.japtar.chessclock.models.SaveStateModel#saveSettings(android.content.SharedPreferences)
 	 */
 	@Override
 	public void saveSettings(final SharedPreferences saveState) throws
 			IllegalArgumentException {
 		// Make sure the parameter isn't null
 		if(saveState == null) {
 			// If so, complain
 			throw new IllegalArgumentException("saveState cannot be null");
 		}
 		
 		// Grab the editor, and start saving
 		final SharedPreferences.Editor saveEditor = saveState.edit();
 		
 		// Save the white player attributes to the bundle
 		savedWhiteTimeLimit.saveTime(saveEditor, KEY_WHITE_TIME_LIMIT);
 		savedWhiteDelayTime.saveTime(saveEditor, KEY_WHITE_DELAY_TIME);
 		
 		// Save the black player attributes to the bundle
 //		savedBlackTimeLimit.saveTime(saveEditor, KEY_BLACK_TIME_LIMIT);
 //		savedBlackDelayTime.saveTime(saveEditor, KEY_BLACK_DELAY_TIME);
 		
 		// Save the alarm
 		saveEditor.putString(KEY_ALARM, alarmUri.toString());
 		
 		// Save feedback-related conditionals
 		saveEditor.putBoolean(KEY_CLICK, enableClick);
 		saveEditor.putBoolean(KEY_VIBRATE, enableVibrate);
 		
 		// Save advanced options
 //		saveEditor.putBoolean(KEY_HANDICAP, enableHandicap);
 //		saveEditor.putBoolean(KEY_NEGATIVE, enableNegatives);
 //		saveEditor.putBoolean(KEY_PENALTY_ENABLED, enablePenality);
 //		penalityTime.saveTime(saveEditor, KEY_PENALTY_TIME);
		switch(delayMode) {
			case DelayMode.FISCHER:
				saveEditor.putString(KEY_DELAY_MODE, DelayMode.STRING_FISCHER);
				break;
			case DelayMode.FISCHER_AFTER:
				saveEditor.putString(KEY_DELAY_MODE, DelayMode.STRING_FISCHER_AFTER);
				break;
			case DelayMode.BRONSTEIN:
				saveEditor.putString(KEY_DELAY_MODE, DelayMode.STRING_BRONSTEIN);
				break;
			case DelayMode.HOUR_GLASS:
				saveEditor.putString(KEY_DELAY_MODE, DelayMode.STRING_HOUR_GLASS);
				break;
			case DelayMode.BASIC:
			default:
				saveEditor.putString(KEY_DELAY_MODE, DelayMode.STRING_BASIC);
				break;
		}
 		
 		// Write it in!
 		saveEditor.commit();
 	}
 }
