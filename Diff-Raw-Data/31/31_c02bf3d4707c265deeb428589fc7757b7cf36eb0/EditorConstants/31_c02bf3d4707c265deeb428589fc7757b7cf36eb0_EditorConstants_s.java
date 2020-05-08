 package net.fhtagn.zoobeditor;
 
 import android.app.Activity;
 import android.graphics.Color;
 
 public class EditorConstants {
 	public static final String TAG  = "ZoobEditor";
 	
 	public static final String ACCOUNT_TYPE = "com.google";
 	
 	/**
 	 * onActivityResult request code
 	 */
 	public static final int GET_LOGIN = 1;
 	public static final int SEND_TO_ZOOB_WEB = 2;
 	
 	public static final int RESULT_ERROR = Activity.RESULT_FIRST_USER;
 	
 	/**
 	 * Colors
 	 */
 	
 	public static final int COLOR_UPLOADED = Color.parseColor("#FF50AB86");
 	public static final int COLOR_NOT_UPLOADED = Color.parseColor("#FFBC3300");
 	
 	/**
 	 * Zoobweb
 	 */
	private static final boolean production = false; 
 	private static final String SERVER_URL = "http://zoobweb.appspot.com/rest";
 	private static final String LOCAL_URL = "http://192.168.0.2:8080/rest";
 	
 	private static final String SERVER_LOGIN = "http://zoobweb.appspot.com"; 
 	private static final String LOCAL_LOGIN = "http://192.168.0.2:8080";
 	
 	public static boolean isProd () {
 		return production;
 	}
 	
 	public static String getServerUrl () {
 		if (production)
 			return SERVER_URL;
 		else
 			return LOCAL_URL;
 	}
 	
 	public static String getLoginUrl () {
 		if (production)
 			return SERVER_LOGIN;
 		else
 			return LOCAL_LOGIN;
 	}
 	
 	public static String getPutUrl () {
 		return getServerUrl()+"/put";
 	}
 	
 	public static String getPutUrl (long id) {
 		return getServerUrl()+"/put?id="+id;
 	}
 	
 	public static String getListUrl () {
 		return getServerUrl()+"/";
 	}
 	
 	public static String getDetailsUrl (int serieId) {
 		return getServerUrl()+"/show?id="+serieId;
 	}
 }
