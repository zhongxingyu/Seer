 package com.wolflink289.bukkit.worldregions.util;
 
 public class TimeUtil {
 	
 	// Constructor
 	private TimeUtil() {
 	}
 	
 	// Methods
 	/**
 	 * Convert ticks to a human readable HH:MM:SS time format.
 	 * 
 	 * @param ticks the ticks to convert.
 	 * @return a human readable time format.
 	 */
 	static public String ticksAsReadable(int ticks) {
 		int tc = (int) Math.floor(ticks / 20d);
 		int s = tc % 60;
 		int m = ((int) Math.floor(tc / 60d)) % 60;
 		int h = (int) Math.floor(tc / 60d / 60d);
 		
 		if (h > 0) {
 			return pad(h, 1) + ":" + pad(m, 2) + ":" + pad(s, 2);
 		} else {
 			return pad(m, 1) + ":" + pad(s, 2);
 		}
 	}
 	
 	/**
 	 * Convert a human readable HH:MM:SS time format to ticks.
 	 * 
 	 * @param hrtf the human readable time format.
 	 * @return the time in ticks.
 	 */
 	static public int readableAsTicks(String hrtf) {
 		String[] split = hrtf.split(":");
 		
 		int h = 0;
 		int m;
 		int s;
 		
 		int i = 0;
 		
 		if (split.length == 3) h = Integer.parseInt(split[i++]);
 		m = Integer.parseInt(split[i++]);
 		s = Integer.parseInt(split[i]);
 		
 		return (h * 60 * 60 + m * 60 + s) * 20;
 	}
 	
 	/**
 	 * Convert ticks into the 24-hour time format.
 	 * 
 	 * @param ticks the ticks to convert.
 	 * @return the ticks in a 24-hour time format.
 	 */
 	static public String cticksAsTime(int ticks) {
 		int hours = (int) Math.floor(ticks / 1000d);
 		int mins = (int) Math.ceil((ticks % 1000) / 1000 * 60);
 		
 		return pad(hours, 2) + ":" + pad(mins, 2);
 	}
 	
 	/**
 	 * Convert a time format into Minecraft day cycle ticks. <br>
 	 * Supported formats:
 	 * 15:00 (24 hour)
 	 * 6:31PM (12 hour, requires (AM or PM))
 	 * 16800 (ticks)
 	 * 
 	 * @param str the string to convert.
 	 * @return the time in ticks.
 	 * @throws NumberFormatException
 	 */
 	static public int timeAsCticks(String str) {
 		// Ticks
 		try {
 			int ticks = Integer.parseInt(str);
 			if (ticks < 0 || ticks > 24000) throw new RuntimeException("Ticks range from 0 to 24000.");
 			return ticks;
 		} catch (NumberFormatException ex) {
 			// Ignore and continue
 		} catch (RuntimeException ex) {
 			throw new NumberFormatException(ex.getMessage());
 		}
 		
 		// 12h
 		str = str.toLowerCase();
 		if (str.endsWith("am") || str.endsWith("pm")) {
 			boolean pm = str.endsWith("pm");
 			
 			str = str.substring(0, str.length() - 2);
 			String[] split = str.split(":");
 			
 			int hours = 0;
 			int mins = 0;
 			
 			try {
 				hours = Integer.parseInt(split[0].trim());
 				mins = Integer.parseInt(split[1].trim());
 			} catch (Exception ex) {
 				throw new NumberFormatException("Invalid time.");
 			}
 			
 			if (hours < 1 || hours > 12) throw new NumberFormatException("Hours in 12-hour time range from 1 to 12");
 			if (mins < 0 || mins > 59) throw new NumberFormatException("Minutes range from 0 to 59");
 			
			if (hours == 12) pm = !pm;
 			if (pm) hours += 12;
 			
 			return (hours * 1000) + (1000 / 60 * mins);
 		} else {
 			// 24h
 			String[] split = str.split(":");
 			
 			int hours = 0;
 			int mins = 0;
 			
 			try {
 				hours = Integer.parseInt(split[0].trim());
 				mins = Integer.parseInt(split[1].trim());
 			} catch (Exception ex) {
 				throw new NumberFormatException("Invalid time.");
 			}
 			
 			if (hours < 0 || hours > 23) throw new NumberFormatException("Hours in 24-hour time range from 0 to 23");
 			if (mins < 0 || mins > 59) throw new NumberFormatException("Minutes range from 0 to 59");
 			
 			return (hours * 1000) + (1000 / 60 * mins);
 		}
 	}
 	
 	// Utility in a utility
 	static private String pad(long number, int padding) {
 		String padded = String.valueOf(number);
 		while (padded.length() < padding) {
 			padded = "0" + padded;
 		}
 		return padded;
 	}
 }
