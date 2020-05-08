 package no.group09.utils;
 
 /*
  * Licensed to UbiCollab.org under one or more contributor
  * license agreements.  See the NOTICE file distributed 
  * with this work for additional information regarding
  * copyright ownership. UbiCollab.org licenses this file
  * to you under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import no.group09.ucsoftwarestore.R;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.util.Log;
 
 public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener{
 
 	private String TAG = "Preferences.java";
 	private PreferenceScreen ps = null;
 	
 	private Context c = null;
 	private AlertDialog.Builder adb = null;
 	private SharedPreferences sharedPref = null;
 	private final static String CONN_DEVICE_KEY = "connected_device_dialog";
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		/*This method is deprecated, but have to be used when developing for
 		 * Android 3.0 or lower.
 		 */
 		addPreferencesFromResource(R.xml.preferences);
 		ps = getPreferenceScreen();
 		ps.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
 		//Get the context
 		this.c = this;
 		//Create the alert dialog box
 		this.adb = new AlertDialog.Builder(c);
 		
 		//Get the sharedPreference
         sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
 
 		
 		//Used when the 'connected_device_dialog' preference in the preference
 		//screen is clicked
 		ps.findPreference("connected_device_dialog").setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				String lastConnMessage = sharedPref.getString(CONN_DEVICE_KEY, "No device found");
 				adb.setTitle("Last connected device");
 				adb.setMessage(lastConnMessage);
 				adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				});
 				adb.show();
 				return true;
 			}
 		});
 		String deviceName = sharedPref.getString("connected_device_name", "");
 		ps.findPreference("connected_device_dialog").setSummary("Last connected: " + deviceName);
 		
 		//Get the device name preference
		Preference mCheckBoxPref = (Preference) findPreference("connected_device_name");	
 		//get the preferencecategory to the device_name
 		PreferenceCategory mCategory = (PreferenceCategory) findPreference("device_name_category");	
 		//remove this category from the settings-view
 		mCategory.removePreference(mCheckBoxPref);	
 		
 	}
 	
 	/**
 	 * Method called when changes are made in the shared preferences file. Can
 	 * be used to tell other instances of changes made.
 	 */
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 
 		//If the 'hide incompatible' setting is changed
 		if (key.equals("hide_incompatible")) {
 			Log.d(TAG, "'onSharedPreferenceChanged' in Preferences.java has been called");
 		}
 	}
 }
