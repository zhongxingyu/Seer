 package com.sis.util;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 /**
  * miscellaneous helper for String related tasks
  * 
  * @author CR
  *
  */
 public class StringHelper {
 //	public static byte[] parse
 	
 	private static final DateFormat databaseDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	
 	public static String databaseDate() {
 		return databaseDate(new Date());
 	}
 	
 	public static String databaseDate(Date date) {
 		return databaseDateFormat.format(date);
 	}
 	
 	public static Date createDateByDatabaseDate(String databaseDate) {
 		return createDateByDatabaseDate(databaseDate, new Date());
 	}
 	
 	public static Date createDateByDatabaseDate(String databaseDate, Date defaultValue) {
 		try {
 			return databaseDateFormat.parse(databaseDate);
 		} catch (ParseException e) {
 			return defaultValue;
 		}
 	}
 	
 	public static String calculateMD5(java.lang.String input) {
 		MessageDigest md5;
 		try {
 			md5 = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException e) {
 			return null;
 		}
 		md5.reset();
 		md5.update(input.getBytes());
 		byte[] result = md5.digest();
 		
 		StringBuffer hexString = new StringBuffer();
         for (int i=0; i<result.length; i++) {
            hexString.append(Integer.toHexString(0xFF & result[i]));
         }
         
         return hexString.toString();
 	}
 	
 	public static String[] objectArrayToStringArray(Object [] objArray) {
 		String [] array = new String[objArray.length];
 		
 		int i = 0;
 		
 		for (Object obj : objArray) {
 			array[i++] = obj.toString();
 		}
 		
 		return array;
 	}
 	
 	/**
 	 * @deprecated use {@link ArrayHelper#objectArrayToObjectArray(Object[])} instead
 	 * @param objArray
 	 * @return
 	 */
 	public static Object[] objectArrayToObjectArray(Object [] objArray) {
 		return ArrayHelper.objectArrayToObjectArray(objArray);
 	}
 	
 	public static String coalesce(Object[] coll, Character seperator) {
 		StringBuilder sb = new StringBuilder();
 		
 		for (Object object : coll) {
 			if (object == null) {
 				continue;
 			}
 			
 			if (sb.length() > 0) {
 				sb.append(seperator);
 			}
 			
 			sb.append(object.toString());
 		}
 		
 		return sb.toString();
 	}
 	public static String coalesce(Collection<?> coll, Character seperator) {
 		StringBuilder sb = new StringBuilder();
 		
 		for (Object object : coll) {
 			if (object == null) {
 				continue;
 			}
 			
 			if (sb.length() > 0) {
 				sb.append(seperator);
 			}
 			
 			sb.append(object.toString());
 		}
 		
 		return sb.toString();
 	}
 
 	/**
 	 * @deprecated use {@link ArrayHelper#inArrayList(ArrayList, Object)} instead
 	 */
 	public static boolean inArrayList(ArrayList<?> strings, String string) {
 		for (Object object : strings) {
 			if (object.toString().equals(string)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	/**
 	 * @deprecated use {@link ArrayHelper#inArray(Object[], Object)} instead
 	 */
 	public static boolean inArray(String[] strings, String string) {
 		for (Object object : strings) {
 			if (object.toString().equals(string)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public static String escapeHtml(String input) {
 		return StringEscapeUtils.escapeHtml(input);
 	}
 }
