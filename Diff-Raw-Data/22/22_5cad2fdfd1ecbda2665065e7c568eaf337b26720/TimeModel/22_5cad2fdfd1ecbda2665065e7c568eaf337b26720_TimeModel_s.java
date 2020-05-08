 /**
  * Helper classes
  */
 package com.app.chessclock.models;
 
 import java.security.InvalidParameterException;
 
 /**
  * Helper class representing time.
  * @author japtar10101
  */
 public class TimeModel {
 	/* ===========================================================
 	 * Members
 	 * =========================================================== */
 	/** Minutes */
 	private Integer mMinutes;
 	/** Seconds */
 	private Integer mSeconds;
 	
 	/* ===========================================================
 	 * Constructors
 	 * =========================================================== */
 	/**
 	 * Default constructor.  Sets the time to 0.
 	 * @see #setTime(int, int)
 	 */
 	public TimeModel(final int minutes, final int seconds) {
 		this.setTime(minutes, seconds);
 	}
 	
 	/**
 	 * Default constructor.  Sets the time to 0.
 	 */
 	public TimeModel() {
 		this(0, 0);
 	}
 
 	/* ===========================================================
 	 * Override
 	 * =========================================================== */
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		// Generate a string, starting with the minutes
 		String toReturn = mMinutes.toString() + ':';
 		
 		// Append the seconds
 		if(mSeconds < 10) {
 			// If seconds is less than 10, append a 0
 			toReturn += '0';
 		}
 		toReturn += mSeconds.toString();
 		
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
 	 * @see #setMinutes(int)
 	 * @see #setSeconds(int)
 	 */
 	public void setTime(final int minutes, final int seconds) {
 		this.setSeconds(seconds);
 		this.setMinutes(minutes);
 	}
 	
 	/**
 	 * Sets the time.
 	 * @param time time to match
 	 */
 	public void setTime(final TimeModel time) {
 		if(time != null) {
 			this.setTime(time.getMinutes(), time.getSeconds());
 		}
 	}
 	
 	/**
 	 * @return True if both the minutes and seconds are 0
 	 * @see #getMinutes()
 	 * @see #getSeconds() 
 	 */
 	public boolean isTimeZero() {
 		return this.getMinutes() + this.getSeconds() == 0;
 	}
 	
 	/**
 	 * Decreases the time by a seconds
 	 * @return False if both minutes and seconds are 0
 	 * @see #isTimeZero()
 	 */
 	public boolean decrementASecond() {
 		boolean toReturn = !isTimeZero();
 		
 		// Check if we're not at 0
 		if(toReturn) {
			// check if the seconds is already at 0
			if(mSeconds <= 0) {
 				// If so, decrement the minutes
 				--mMinutes;
 				
 				// reset seconds to 59
 				mSeconds = 59;
			} else {
				// If not, decrement the seconds 
				--mSeconds;
 			}
 		}
 		
 		return toReturn;
 	}
 
 	/* ===========================================================
 	 * Getters
 	 * =========================================================== */
 	/**
 	 * @return {@link mMinutes}
 	 */
 	public Integer getMinutes() {
 		return mMinutes;
 	}
 	/**
 	 * @return {@link mSeconds}
 	 */
 	public Integer getSeconds() {
 		return mSeconds;
 	}
 
 	/* ===========================================================
 	 * Setters
 	 * =========================================================== */
 	/**
 	 * @param minutes sets {@link mMinutes}
 	 * @throws InvalidParameterException if <b>minutes</b> is negative,
 	 * or greater than 23 (as arbitrary as it sounds, due to current
 	 * GUI limitations, it's set to the hours limitations on a TimePicker).
 	 */
 	public void setMinutes(final int minutes) throws InvalidParameterException {
 		if(minutes < 0) {
 			// negative valuemLeftButton
 			throw new InvalidParameterException("Minutes must be positive");
 		} else if(minutes >= 24) {
 			// greater than 59
 			throw new InvalidParameterException("Minutes must be less than 24");
 		} else {
 			// update the minutes
 			this.mMinutes = minutes;
 		}
 	}
 	/**
 	 * @param seconds sets {@link mSeconds}
 	 * @throws InvalidParameterException if <b>seconds</b> is negative,
 	 * or greater than 59
 	 */
 	public void setSeconds(final int seconds) throws InvalidParameterException {
 		if(seconds < 0) {
 			// negative value
 			throw new InvalidParameterException("Seconds must be positive");
 		} else if(seconds >= 60) {
 			// greater than 59
 			throw new InvalidParameterException("Seconds must be less than 60");
 		} else {
 			// update the seconds
 			this.mSeconds = seconds;
 		}
 	}
 }
