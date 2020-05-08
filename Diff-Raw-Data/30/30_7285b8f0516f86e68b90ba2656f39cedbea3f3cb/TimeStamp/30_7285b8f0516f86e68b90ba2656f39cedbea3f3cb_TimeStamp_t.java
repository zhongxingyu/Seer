 package se.chalmers.chessfeud.utils;
 
 /**
  * This is a class for creating and managing timestamps. This class is useful
  * when calculating how long ago a timestamp were.
  * 
  * @author Henrik Alburg
  * 
  *         Copyright (c) Henrik Alburg 2012
  */
 public class TimeStamp {
 	private static int[] daysPerMonth = C.DAYS_PER_MONTH;
 	private static int secondsPerMinute = C.SECONDS_PER_MINUTE;
 	private static int secondsPerHour = secondsPerMinute * C.SECONDS_PER_MINUTE;
 	private static int secondsPerDay = secondsPerHour * C.HOURS_PER_DAY;
 	private static int daysPerYear = C.DAYS_PER_YEAR;
 	private static int daysPerLeapYear = C.DAYS_PER_LEAPYEAR;
 
 	private long timeSeconds;
 
 	/**
 	 * Creates a timestamp from a string. The string should be on the form:
 	 * "2012-10-20 13:37:18"
 	 * 
 	 * @param s
 	 *            The string representing a date
 	 */
 	public TimeStamp(String s) {
 		String[] ymd = s.split("-");
 		String[] hms = ymd[2].split(":");
 		
 		ymd[2] = ymd[2].substring(0, 2);
 		hms[0] = hms[0].substring(2);
 
 		this.timeSeconds = 0;
 
 		int year = Integer.parseInt(ymd[0]);
 		int month = Integer.parseInt(ymd[1]);
 		int day = Integer.parseInt(ymd[2]);
 
 		int hour = Integer.parseInt(hms[0]);
 		int minute = Integer.parseInt(hms[1]);
 		int seconds = (int)Double.parseDouble(hms[2]);
 
 		timeSeconds += getYearSeconds(year);
 		timeSeconds += getMonthSeconds(month);
 		timeSeconds += getDaySeconds(day);
 
 		timeSeconds += getHourSeconds(hour);
 		timeSeconds += getMinuteSeconds(minute);
 		timeSeconds += seconds;
 		
 		if(year%C.YEARS_BETWEEN_LEAPYEAR == 0 && month > 2){
 			timeSeconds += secondsPerDay;
 		}
 
 	}
	
	/**
	 * Creates a timestamp with the current time.
	 */
	public TimeStamp(){
		timeSeconds = System.currentTimeMillis()/1000;
	}
 
 	/**
 	 * Returns the amount of seconds since the start of 1970.
 	 * 
 	 * @return
 	 */
 	public long getSeconds() {
 		return timeSeconds;
 	}
 
 	/**
 	 * Returns the number of minutes since this timestamp took place.
 	 * 
 	 * @return The number of minutes since this date.
 	 */
 	public int getMinutesSinceStamp() {
 		long currentSeconds = System.currentTimeMillis();
 		return (int) ((currentSeconds - timeSeconds * 1000) / 60000);
 	}
 
 	/* Returns the number of seconds that have passed for all the months */
 	private int getMonthSeconds(int month) {
 		int seconds = 0;
 		for (int i = 1; i < month; i++) {
 			seconds += daysPerMonth[i-1] * secondsPerDay;
 		}
 		return seconds;
 	}
 
 	/* Returns the number of seconds between 1970 and this year */
 	private int getYearSeconds(int year) {
 		int seconds = 0;
 		for (int i = C.STARTING_YEAR; i < year; i++) {
 			if (i % C.YEARS_BETWEEN_LEAPYEAR == 0) {
 				seconds += daysPerLeapYear * secondsPerDay;
 			} else {
 				seconds += daysPerYear * secondsPerDay;
 			}
 		}
 		return seconds;
 	}
 
 	/*
 	 * Returns the number of seconds between the first day in this month and
 	 * this day
 	 */
 	private int getDaySeconds(int day) {
 		int seconds = 0;
 		for (int i = 1; i < day; i++) {
 			seconds += secondsPerDay;
 		}
 		return seconds;
 	}
 
 	/* Returns the number of seconds between the days first hour and this hour */
 	private int getHourSeconds(int hour) {
 		int seconds = 0;
 		for (int i = 1; i < hour-1; i++) {
 			seconds += secondsPerHour;
 		}
 		return seconds;
 	}
 
 	/*
 	 * Returns the number of seconds between this minute and the first minute of
 	 * the hour
 	 */
 	private int getMinuteSeconds(int minute) {
 		return secondsPerMinute * minute;
 	}
 }
