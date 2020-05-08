 package com.cst.stormdroid.utils;
 
 import com.cst.stormdroid.utils.log.SDLog;
 import com.cst.stormdroid.utils.log.SDLog.SDLogLevel;
 
 
 /**
  * configuration of this application
  * @author MonsterStorm
  * @version 1.0
  */
 public class Config {
 	
 	/**
 	 * whether is in debug mode
 	 */
 	public static boolean mDebug;
 	
 	/**
 	 * whether handler crash by CrashHandler
 	 */
 	public static final boolean mHandleCrash = false;
 	
 	static {
 		mDebug = SDLog.SD_LOG_LEVEL == SDLogLevel.SD_LOG_LEVEL_DEBUG;
 	}
 	
 	/**
 	 * http params encode
 	 */
 	public static final String PARAMS_ENCODE = "UTF-8";
 	
 	/**
 	 * enable or disable debug
 	 * @param enable
 	 */
 	public static void enableDebug(boolean enable){
 		mDebug = enable;
 		if(enable){
 			SDLog.setLogLevel(SDLogLevel.SD_LOG_LEVEL_DEBUG);
 		} else {
 			SDLog.setLogLevel(SDLogLevel.SD_LOG_LEVEL_NONE);
 		}
 	}
	
	/**
	 * is enable debug
	 * @return
	 */
	public static boolean isDebug(){
		return mDebug;
	}
 }
