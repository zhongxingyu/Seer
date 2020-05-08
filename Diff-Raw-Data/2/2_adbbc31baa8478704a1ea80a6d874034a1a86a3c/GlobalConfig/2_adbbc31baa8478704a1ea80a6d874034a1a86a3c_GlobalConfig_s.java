 package com.stonepeak.monkey.data;
 
 public class GlobalConfig {
 	private static String gtestAppName;
 
 	/**
 	 * @return the gtestAppPath
 	 */
 	public static String getGtestAppPath() {
		return System.getProperty("user.dir") + System.getProperty("file.separator") + gtestAppName;
 	}
 
 	/**
 	 * @param gtestAppName the gtestAppName to set
 	 */
 	public static void setGtestAppName(String gtestAppName) {
 		GlobalConfig.gtestAppName = gtestAppName;
 	}
 	
 	
 
 }
