 /**
  * Helper classes
  */
 package com.japtar.chessclock.models;
 
 import java.security.InvalidParameterException;
 
 import android.content.SharedPreferences;
 
 /**
  * Helper class representing time.
  * @author japtar10101
  */
 public class TimeModel implements Comparable<TimeModel> {
 	/* ===========================================================
 	 * Static functions
 	 * =========================================================== */
 	/**
 	 * @return Converts <b>integer</b> into a byte
 	 */
 	public final static byte intToByte(final int integer) {
 		return (integer <= Byte.MAX_VALUE ? (byte) integer : Byte.MAX_VALUE);
 	}
 	public final static int totalSeconds(final TimeModel time) {
 		return time.mSeconds;
 	}
 	
 	/* ===========================================================
 	 * Members
 	 * =========================================================== */
 	/** Seconds */
 	private int mSeconds;
 	
 	/* ===========================================================
 	 * Constructors
 	 * =========================================================== */
 	/**
 	 * Constructor
 	 */
 	public TimeModel(final int minutes, final int seconds) throws
 			InvalidParameterException {
 		this.setSeconds(seconds);
 		this.setMinutes(minutes);
 	}
 	
 	/**
 	 * Constructor for saved values.
 	 */
 	public TimeModel(final int storedValue) throws InvalidParameterException {
 		// Convert the stored value into seconds
 		mSeconds = storedValue;
 	}
 	
 	/**
 	 * Default constructor.  Sets the time to 0.
 	 */
 	public TimeModel() {
 		mSeconds = 0;
 	}
 
 	/* ===========================================================
 	 * Override
 	 * =========================================================== */
 	/**
 	 * Compares which time model is greater, and by how many seconds.
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	@Override
 	public int compareTo(final TimeModel another) {
 		return mSeconds - totalSeconds(another);
 	}
 	
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		// Generate a string
 		String toReturn = "";
 		
 		// If negative, add in a minus sign
 		if(mSeconds < 0) {
 			toReturn += '-';
 		}
 		
 		// Add in the minutes and colon
 		toReturn += Integer.toString(this.getMinutes());
 		toReturn += ':';
 		
 		// Append the seconds
 		final int seconds = this.getSeconds();
 		if(seconds < 10) {
 			// If seconds is less than 10, append a 0
 			toReturn += '0';
 		}
 		toReturn += Integer.toString(seconds);
 		
 		return toReturn;
 	}
 	
 	/* ===========================================================
 	 * Public Methods
 	 * =========================================================== */
 	/**
 	 * Sets the time.
 	 * @param minutes sets {@link mMinutes}
 	 * @param seconds sets {@link mSeconds}
 	 * @throws InvalidParameterException if <b>minutes</b> is negative
 	 * @throws InvalidParameterException if <b>seconds</b> is negative,
 	 * or greater than 59
 	 */
 	public void setTime(final int minutes, final int seconds) throws
 			InvalidParameterException {
 		// Make sure the parameter are correct
 		if(minutes < 0) {
 			throw new InvalidParameterException("Minutes must be positive value");
 		} else if(seconds < 0) {
 			throw new InvalidParameterException("Seconds must be positive value");
 		} else if(seconds > 59) {
 			throw new InvalidParameterException("Seconds must be less than 60");
 		}
 		
 		// Set the time
 		mSeconds = seconds + minutes * 60;
 	}
 	
 	/**
 	 * Sets the time.
 	 * @param model time to match
 	 */
 	public void setTime(final TimeModel model) {
 		if(model != null) {
 			mSeconds = totalSeconds(model);
 		}
 	}
 	
 	/**
 	 * Sets the time based on the saved state's value
 	 * @param savedState
 	 * @param key
 	 * @param defaultValue
 	 * @throws InvalidParameterException 
 	 * @throws InternalError 
 	 */
 	public void recallTime(final SharedPreferences savedState,
 			final String key, final int defaultValue) throws
 			InvalidParameterException, InternalError {
 		// Make sure the parameters are valid
 		if(savedState == null) {
 			throw new InvalidParameterException("SharedPreference is null");
 		} else if(key == null) {
 			throw new InvalidParameterException("String is null");
 		}
 		
 		// Recall the attributes from the bundle
 		final int savedValue = savedState.getInt(key, defaultValue);
 		
 		// Make sure this value is valid
 		if(savedValue < 0) {
 			throw new InternalError(
 					"The stored value must be a non-negative value");
 		}
 		
 		// Convert the stored value into seconds
		mSeconds += savedValue;
 	}
 	
 	/**
 	 * Stores the time
 	 * @param saveEditor
 	 * @param key
 	 * @throws InvalidParameterException
 	 */
 	public void saveTime(final SharedPreferences.Editor saveEditor,
 			final String key) throws InvalidParameterException {
 		// Make sure the parameters are valid
 		if(saveEditor == null) {
 			throw new InvalidParameterException("SharedPreference is null");
 		} else if(key == null) {
 			throw new InvalidParameterException("String is null");
 		}
 	
 		// Save the attributes to the bundle
 		saveEditor.putInt(key, totalSeconds(this));
 	}
 	
 	/**
 	 * @return True if both the minutes and seconds are 0
 	 */
 	public boolean isTimeZero() {
 		return mSeconds == 0;
 	}
 	
 	/**
 	 * @return True if the time is negative
 	 */
 	public boolean isTimeNegative() {
 		return mSeconds < 0;
 	}
 	
 	/**
 	 * Increment the time
 	 */
 	public void incrementTime(final TimeModel model) {
 		mSeconds += totalSeconds(model);
 	}
 	
 	/**
 	 * Decreases the time by a second
 	 * @return False if both minutes and seconds are 0
 	 * @see #isTimeZero()
 	 */
 	public boolean decrementASecond() {
 		// Check if we're at zero, first
 		boolean toReturn = !isTimeZero();
 		
 		// Decrement the seconds 
 		--mSeconds;
 		return toReturn;
 	}
 	
 	/**
 	 * Increases the time by a second, if not negative
 	 * @return False if time is negative
 	 * @see #isTimeZero()
 	 */
 	public boolean incrementASecond() {
 		// Check if we're negative, first
 		boolean toReturn = !isTimeNegative();
 		if(toReturn) {
 			// Increment the seconds 
 			++mSeconds;
 		}
 		return toReturn;
 	}
 
 	/* ===========================================================
 	 * Getters
 	 * =========================================================== */
 	/**
 	 * @return Minutes. Always positive.
 	 */
 	public int getMinutes() {
 		// Get the total seconds
 		int toReturn = mSeconds;
 		
 		// If negative, make it positive
 		if(toReturn < 0) {
 			toReturn *= -1;
 		}
 		
 		// Divide by 60
 		toReturn /= 60;
 		
 		return toReturn;
 	}
 	/**
 	 * @return Seconds. Always positive.
 	 */
 	public int getSeconds() {
 		// Get the total seconds
 		int toReturn = mSeconds;
 		
 		// If negative, make it positive
 		if(toReturn < 0) {
 			toReturn *= -1;
 		}
 		
 		// Modulo by 60
 		toReturn %= 60;
 		
 		return toReturn;
 	}
 
 	/* ===========================================================
 	 * Setters
 	 * =========================================================== */
 	/**
 	 * @param minutes sets {@link mMinutes}
 	 * @throws InvalidParameterException if <b>minutes</b> is negative
 	 */
 	public void setMinutes(final int minutes) {
 		// Make sure the parameter is correct
 		if(minutes < 0) {
 			throw new InvalidParameterException("Minutes must be positive value");
 		}
 		
 		// Set the time to positive seconds
 		mSeconds = this.getSeconds();
 		
 		// Add the minutes
 		mSeconds += minutes * 60;
 	}
 	
 	/**
 	 * @param seconds sets {@link mSeconds}
 	 * @throws InvalidParameterException if <b>seconds</b> is negative,
 	 * or greater than 59
 	 */
 	public void setSeconds(final int seconds) throws InvalidParameterException {
 		// Make sure the parameter is correct
 		if(seconds < 0) {
 			throw new InvalidParameterException("Seconds must be positive value");
 		} else if(seconds > 59) {
 			throw new InvalidParameterException("Seconds must be less than 60");
 		}
 		
 		// First, get the minutes
 		int minutes = this.getMinutes();
 		
 		// Set the time to positive seconds
 		mSeconds = seconds;
 		
 		// Add the minutes
 		mSeconds += minutes * 60;
 	}
 }
