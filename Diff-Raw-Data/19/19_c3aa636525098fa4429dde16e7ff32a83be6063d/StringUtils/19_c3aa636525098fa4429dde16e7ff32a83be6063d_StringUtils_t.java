 package ch.ethz.inf.utils;
 
 public class StringUtils {
 	public static boolean isNullOrEmpty(String str) {
 		return (str == null || str.trim().isEmpty());
 	}
 	
	public static boolean isNotNullNorEmpty(String str) {
 		return !StringUtils.isNullOrEmpty(str);
 	}
 }
