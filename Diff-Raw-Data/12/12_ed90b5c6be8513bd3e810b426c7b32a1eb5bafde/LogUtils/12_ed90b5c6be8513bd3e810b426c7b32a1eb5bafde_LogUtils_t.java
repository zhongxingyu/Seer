 package net.madz.download;
 
 import org.apache.log4j.Logger;
 
 public class LogUtils {
 
 	@SuppressWarnings("rawtypes")
 	public static void debug(Class cls, String message) {
 		Logger.getLogger(cls).debug(message);
 	}
 
 	@SuppressWarnings("rawtypes")
	public static void error(Class cls, Throwable e) {
 		Logger.getLogger(cls).error(e.getMessage(), e);
 	}
 
 }
