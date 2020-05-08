 package com.tonyhuangjun.homework;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class Preferences extends PreferenceActivity {
 	private static final String TAG = "Preferences";
 	String listPref;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		PreferenceManager prefMgr = getPreferenceManager();
 		prefMgr.setSharedPreferencesName("Default");
 		prefMgr.setSharedPreferencesMode(MODE_PRIVATE);
 		addPreferencesFromResource(R.xml.preferences);
 	}
 
 	@Override
 	protected void onStart() {
 		getPrefs();
 		super.onStart();
 	}
 
 	private void getPrefs() {
 		SharedPreferences settings = getSharedPreferences("Default",
 				MODE_PRIVATE);
 		Log.d(TAG,
 				"Current number of classes set = "
 						+ settings.getString(MainActivity.NUMBER_OF_CLASSES,
 								"one"));
 		Log.d(TAG,
 				"Current ringtone = "
 						+ settings.getString(MainActivity.NOTIFICATION_SOUND, "Null"));
 
 	}
 
 }
