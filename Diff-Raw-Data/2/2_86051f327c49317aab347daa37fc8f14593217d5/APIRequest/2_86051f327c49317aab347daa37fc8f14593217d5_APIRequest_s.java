 package com.tim.stullich.drawerapp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 import android.app.Activity;
 
 import com.google.gson.Gson;
 
 public class APIRequest {
 
 	public static final int SERVER_STATUS = 0;
 	public static final int TARGET_REQUEST = 1;
 	public static final int TARGET_ATTRIBUTE_REQUEST = 2;
 
 	private static String FINAL_ADDRESS;
 	private static String SERVER_ADDRESS;
 	private static int SERVER_PORT;
 	//private static final String USER_PREFS_FILE = "UserPrefs";
 	
 	/**
 	 * Builds a request to be executed at a later time
 	 * @param act
 	 * @param apiType
 	 */
 	public APIRequest(Activity act, final int apiType) {
 		//SharedPreferences settings = act.getSharedPreferences(USER_PREFS_FILE, 0);
 		SERVER_ADDRESS = "iam.vm.oracle.com";
 		SERVER_PORT = 18102;
 		
 		
 		//TODO Build more cases... maybe even a better way to implement this.
 		switch (apiType) {
 			case SERVER_STATUS : 
 				StringBuilder sb = new StringBuilder();
 				sb.append("https://");
 				sb.append(SERVER_ADDRESS + ":");
 				sb.append(SERVER_PORT);
 				sb.append("/opam/");
 				FINAL_ADDRESS = sb.toString();
 				break;
 		}
 	}
 	
 	//TODO Handle Threading issue.
 	public Gson execute() throws IOException {
 		URL url = new URL(FINAL_ADDRESS);
 		URLConnection urlConnection = url.openConnection();
		InputStream in = urlConnection.getInputStream();
 		return null;
 	}
 }
