 package com.andreibacalu.android.copied.application;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import com.andreibacalu.android.copied.R;
 import com.andreibacalu.android.copied.utils.SharedPreferencesUtil;
 import com.bugsense.trace.BugSenseHandler;
 
 import android.app.Application;
 import android.util.Log;
 
 public class CopiedApplication extends Application {
 
 	private final static String TAG_LOG = CopiedApplication.class.getSimpleName();
 	
 	public final static String DEFAULT_STRING = "You have no copied data yet.";
 	
 	private static List<String> clipboardStrings;
 	private static String currentSelectedString;
 	
 	private static CopiedApplication instance;
 	
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense_api_key));
 		clipboardStrings = new ArrayList<String>(SharedPreferencesUtil.getInstance(getApplicationContext()).getTextsList());
 		currentSelectedString = clipboardStrings.size() == 0 ? "" : clipboardStrings.get(0);
 		instance = this;
 	}
 	
 	public static String getCurrentSelectedString() {
 		if (clipboardStrings.size() > 0) {
 			return currentSelectedString;
 		} else {
 			return DEFAULT_STRING;
 		}
 	}
 	
 	public static void setCurrentSelectedString(String string) {
 		currentSelectedString = string;
 	}
 	
 	public static String getClipboarString(int position) {
 		if (clipboardStrings.size() > position && position >= 0) {
 			return clipboardStrings.get(position);
 		} else {
 			return null;
 		}
 	}
 	
 	public static void addStringToClipboard(String string) {
 		clipboardStrings.add(string);
 		currentSelectedString = string;
 		SharedPreferencesUtil.getInstance(instance).setTextsList(new HashSet<String>(clipboardStrings));
 	}
 	
 	public static int removeStringFromClipboard(String string) {
 		int stringLocation = clipboardStrings.indexOf(string);
 		clipboardStrings.remove(string);
 		SharedPreferencesUtil.getInstance(instance).setTextsList(new HashSet<String>(clipboardStrings));
 		return stringLocation;
 	}
 	
 	public static int getPositionOfCurrentSelectedString() {
 		if (clipboardStrings.size() > 0) {
 			return clipboardStrings.indexOf(currentSelectedString);
 		}
 		return -1;
 	}
 	
 	public static int getNumberOfTextsInClipboard() {
 		Log.e(TAG_LOG, "getNumberOfTextsInClipboard: " + clipboardStrings.size());
 		return clipboardStrings.size();
 	}
 	
 	public static boolean clipboarStringsContain(String string) {
 		return clipboardStrings.contains(string);
 	}
 	
 	public static ArrayList<String> getList() {
 		return new ArrayList<String>(clipboardStrings);
 	}
 	
 	public static void replaceList(List<String> list) {
 		clipboardStrings.clear();
 		clipboardStrings.addAll(list);
 		setCurrentSelectedString(clipboardStrings.size() > 0 ? clipboardStrings.get(0) : DEFAULT_STRING);
 	}
 	
	public static void replaceString(String oldString, String newString) {
 		if (clipboardStrings.indexOf(oldString) > -1) {
 			clipboardStrings.set(clipboardStrings.indexOf(oldString), newString);
 			SharedPreferencesUtil.getInstance(instance).setTextsList(new HashSet<String>(clipboardStrings));
 		}
 	}
 }
