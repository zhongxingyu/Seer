 /**
  * Helper classes
  */
 package com.app.chessclock.models;
 
 import java.security.InvalidParameterException;
 
 import android.os.Bundle;
 
 import com.app.chessclock.Global;
 import com.app.chessclock.enums.TimerCondition;
 
 /**
  * Model indicating the game's current (or paused) conditions.
  * @author japtar10101
  */
 public class GameStateModel {
 	/* ===========================================================
 	 * Constants
 	 * =========================================================== */
 	/** The saved key value for who's turn it is */
 	public static final String KEY_LEFT_PLAYERS_TURN = "leftPlayersTurn";
 	/** The saved key value for the timer condition */
 	public static final String KEY_TIMER_CONDITION = "timerCondition";
 	
 	/** The saved key value for left player's time (minutes) */
 	public static final String KEY_LEFT_PLAYERS_MINUTES = "leftPlayersMinutes";
 	/** The saved key value for left player's (seconds) */
 	public static final String KEY_LEFT_PLAYERS_SECONDS = "leftPlayersSeconds";
 	
 	/** The saved key value for left player's time (minutes) */
 	public static final String KEY_RIGHT_PLAYERS_MINUTES = "rightPlayersMinutes";
 	/** The saved key value for left player's time (seconds) */
 	public static final String KEY_RIGHT_PLAYERS_SECONDS = "rightPlayersSeconds";
 	
 	/** The saved key value for delay time (minutes) */
 	public static final String KEY_DELAY_TIME_MINUTES = "gameDelayMinutes";
 	/** The saved key value for delay time (seconds) */
 	public static final String KEY_DELAY_TIME_SECONDS = "gameDelaySeconds";
 	
 	/* ===========================================================
 	 * Members
 	 * =========================================================== */
 	/** The saved state */
 	private Bundle mSavedState = null;
 	/** prepend string for delay time */
 	private String mDelayPrependString = null;
 	
 	// == Conditionals ==
 	/** Flag indicating whose turn it is */
 	public boolean leftPlayersTurn = false;
 	/** The current timer's condition */
 	public TimerCondition timerCondition = TimerCondition.STARTING;
 	
 	// == Times ==
 	/** Left player's time */
 	private final TimeModel mLeftPlayersTime = new TimeModel();
 	/** Right player's time */
 	private final TimeModel mRightPlayersTime = new TimeModel();
 	/** Time of delay */
 	private final TimeModel mDelayTime = new TimeModel();
 	
 	/* ===========================================================
 	 * Public Methods
 	 * =========================================================== */
 	/**
 	 * Saves the current omDelayPrependStringptions state to a bundle
 	 * @param savedState the bundle to save this app's options
 	 */
 	public void saveSettings() {
 		// Make sure the bundle isn't null
 		if(mSavedState != null) {
 			// Save the attributes to the bundle
 			mSavedState.putInt(KEY_LEFT_PLAYERS_MINUTES, mLeftPlayersTime.getMinutes());
 			mSavedState.putInt(KEY_LEFT_PLAYERS_SECONDS, mLeftPlayersTime.getSeconds());
 			
 			mSavedState.putInt(KEY_RIGHT_PLAYERS_MINUTES, mRightPlayersTime.getMinutes());
 			mSavedState.putInt(KEY_RIGHT_PLAYERS_SECONDS, mRightPlayersTime.getSeconds());
 			
 			mSavedState.putInt(KEY_DELAY_TIME_MINUTES, mDelayTime.getMinutes());
 			mSavedState.putInt(KEY_DELAY_TIME_SECONDS, mDelayTime.getSeconds());
 			
 			mSavedState.putBoolean(KEY_LEFT_PLAYERS_TURN, leftPlayersTurn);
 			
 			mSavedState.putInt(KEY_TIMER_CONDITION, timerCondition.ordinal());
 			
 			// FIXME: save this bundle to some personal SQL database
 		}
 	}
 	
 	/**
 	 * Decrements time.
 	 * @param numSeconds number of seconds to decrement
 	 * @return true if either player's time is up.
 	 */
 	public boolean decrementTime(final int numSeconds) {
 		boolean toReturn = false;
 		
 		// Figure out which time to decrement
 		TimeModel updateTime = mRightPlayersTime;
 		if(leftPlayersTurn) {
 			updateTime = mLeftPlayersTime;
 		}
 		
 		// Decrement time from delay or player
 		for(int second = 0; second < numSeconds; ++second) {
 			
 			// First attempt to decrement the delay time
 			if(!mDelayTime.decrementASecond()) {
 				
 				// If failed, try decrementing the player's time
 				if(!updateTime.decrementASecond()) {
 					
 					// If that failed, times up!
 					toReturn = true;
 					break;
 				}
 			}
 		}
 		return toReturn;
 	}
 	
 	/**
 	 * Resets the internal game time to option's settings
 	 */
 	public void resetTime() {
 		// Revert all the time to Option's settings
 		mLeftPlayersTime.setTime(Global.OPTIONS.savedTimeLimit);
 		mRightPlayersTime.setTime(Global.OPTIONS.savedTimeLimit);
 		this.resetDelay();
 	}
 	
 	/**
 	 * Resets the internal delay time to option's settings
 	 */
 	public void resetDelay() {
 		mDelayTime.setTime(Global.OPTIONS.savedDelayTime);
 	}
 	
 	/**
 	 * @return left player's current time, in text
 	 */
 	public String leftPlayerTime() {
 		return mLeftPlayersTime.toString();
 	}
 	
 	/**
 	 * @return right player's current time, in text
 	 */
 	public String rightPlayerTime() {
 		return mRightPlayersTime.toString();
 	}
 	
 	/**
 	 * @return If the delay time is zero, returns null.
 	 * Otherwise, returns the current delay time, in text.
 	 */
 	public String delayTime() {
 		// Check if the delay time is zero
 		if(mDelayTime.isTimeZero()) {
 			
 			// If so, return null
 			return null;
 		}
 		
 		// Calculate the total seconds to delay
 		final Integer seconds = mDelayTime.getMinutes() * 60 +
 			mDelayTime.getSeconds();
 		
 		// Prepend the string, if available
 		if(mDelayPrependString != null) {
			return mDelayPrependString + ' ' + seconds.toString();
 		} else {
 			return seconds.toString();
 		}
 	}
 		
 	/* ===========================================================
 	 * Getters
 	 * =========================================================== */
 	/**
 	 * @return {@link mSavedState}
 	 */
 	public Bundle getSavedState() {
 		return mSavedState;
 	}
 	
 	/* ===========================================================
 	 * Setters
 	 * =========================================================== */
 	/**
 	 * @param prepend sets {@link mDelayPrependString}
 	 */
 	public void setDelayPrependString(final String prepend) {
 		mDelayPrependString = prepend;
 	}
 	
 	/**
 	 * @param savedState sets {@link mSavedState}
 	 */
 	public void setSavedState(final Bundle savedState) throws InvalidParameterException {
 		// Set mSavedState
 		mSavedState = savedState;
 		
 		// Make sure the parameter is correct
 		if(mSavedState != null) {
 			// Save the attributes to the bundle
 			mLeftPlayersTime.setMinutes(mSavedState.getInt(
 					KEY_LEFT_PLAYERS_MINUTES));
 			mLeftPlayersTime.setSeconds(mSavedState.getInt(
 					KEY_LEFT_PLAYERS_SECONDS));
 
 			mRightPlayersTime.setMinutes(mSavedState.getInt(
 					KEY_RIGHT_PLAYERS_MINUTES));
 			mRightPlayersTime.setSeconds(mSavedState.getInt(
 					KEY_RIGHT_PLAYERS_SECONDS));
 
 			mDelayTime.setMinutes(mSavedState.getInt(KEY_DELAY_TIME_MINUTES));
 			mDelayTime.setSeconds(mSavedState.getInt(KEY_DELAY_TIME_SECONDS));
 			
 			leftPlayersTurn = mSavedState.getBoolean(KEY_LEFT_PLAYERS_TURN);
 			
 			final int ordinal = mSavedState.getInt(KEY_TIMER_CONDITION);
 			final TimerCondition[] conditions = TimerCondition.values();
 			if(ordinal >= conditions.length) {
 				timerCondition = TimerCondition.STARTING;
 			} else {
 				timerCondition = conditions[ordinal];
 			}
 		}
 	}
 }
