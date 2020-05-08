 package com.stonepeak.monkey.data;
 
 public class GlobalConfig {
 	private static String gtestAppName;
 
 	/**
 	 * @return the gtestAppPath
 	 */
 	public static String getGtestAppPath() {
		return gtestAppName;
 	}
 
 	/**
 	 * @param gtestAppName the gtestAppName to set
 	 */
 	public static void setGtestAppName(String gtestAppName) {
 		GlobalConfig.gtestAppName = gtestAppName;
 	}
 	
 	
 
 }
