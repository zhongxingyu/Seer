 package com.pwr.zpi.utils;
 
 public class TimeFormatter {
 	
 	private TimeFormatter() {}
 	
 	/**
 	 * @param time
 	 * @return time in fromat hh:mm:ss always even when hours are zero.
 	 */
 	public static String formatTimeHHMMSS(long time) {
 		long hours = time / 3600000;
 		long minutes = (time / 60000) - hours * 60;
 		long seconds = (time / 1000) - hours * 3600 - minutes * 60;
 		
 		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
 	}
 	
 	/**
 	 * @param time
 	 * @return time in format mm:ss or hh:mm:ss if hours are not zero.
 	 */
 	public static String formatTimeMMSSorHHMMSS(long time) {
 		long hours = time / 3600000;
 		long minutes = (time / 60000) - hours * 60;
 		long seconds = (time / 1000) - hours * 3600 - minutes * 60;
 		
 		if (hours == 0) return String.format("%02d:%02d", minutes, seconds);
 		else return String.format("%02d:%02d:%02d", hours, minutes, seconds);
 	}
 	
 	/**
 	 * @param pace
 	 * @return pace in format mm:ss or hh:mm:ss if hours are not zero
 	 */
 	public static String formatTimeMMSSorHHMMSS(double pace) {
 		double rest = pace - (int) pace;
 		rest = rest * 60;
 		int hours = (int) (pace / 60);
 		if (hours == 0) return String.format("%d:%02d", (int) pace, (int) rest);
 		else return String.format("%02d:%02d:%02d", hours, (int) pace, (int) rest);
 		
 	}
 }
