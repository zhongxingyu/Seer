 package com.game.fickapets;
 
 import java.util.Calendar;
 import java.util.TimeZone;
 
 
 
 public class Tiredness {
 	private static final double INCREASE_RATE = (double)95 / 48;
 	private static final double DECREASE_CONSTANT = -0.432;
 	
 	private static final int HOURS_PER_SLEEP = 8;
 	private static final double MIN_TIREDNESS_AFTER_ONE_DAY = INCREASE_RATE * (24 - HOURS_PER_SLEEP);
 	
 	private static double currentTiredness;
 	private static final double MAX_TIREDNESS = 100;
	private static final double MIN_TIREDNESS = 0;
 	
 	public Tiredness (Attributes atts) {
 		currentTiredness = atts.tiredness;
 	}
 	
 	public static double getInitialTiredness (Double initialSleepTime) {
 		int currentHour;
 		int currentMinute;
 		double hoursUntilSleep;
 		
 		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
 		currentHour = cal.get(Calendar.HOUR_OF_DAY);
 		currentMinute = cal.get(Calendar.MINUTE);
 		hoursUntilSleep = initialSleepTime - currentHour;
 		if (hoursUntilSleep < 0) hoursUntilSleep = 24 - currentHour + initialSleepTime;
 		
 		hoursUntilSleep -= (double)currentMinute / 60;
 		
 		
 		return MIN_TIREDNESS_AFTER_ONE_DAY - (INCREASE_RATE * hoursUntilSleep);
 	}
 	
 	public double getTiredness () {
 		return currentTiredness;
 	}
 	
 
 	/* needs to be updated before isAwake is changed. */
 	public void update (boolean isAwake, double hoursSinceUpdate) {
 		if (isAwake) {
 			currentTiredness += hoursSinceUpdate * INCREASE_RATE;
 		} else {
 			currentTiredness = Math.exp (DECREASE_CONSTANT * hoursSinceUpdate + Math.log (currentTiredness));
 		}
 		if (currentTiredness > MAX_TIREDNESS) {
 			currentTiredness = MAX_TIREDNESS;
 		} else if (currentTiredness < MIN_TIREDNESS) {
 			currentTiredness = MIN_TIREDNESS;
 		}
 	}
 	
 	public double hoursUntil (double tirednessReached, boolean isAwake) {
 		if (isAwake) {
 			return (tirednessReached - currentTiredness) / INCREASE_RATE;
 		} else {
 			return (Math.log(tirednessReached) - Math.log(currentTiredness)) / DECREASE_CONSTANT;
 		}
 	}
 	
 	
 	
 }
