 /*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.rocketmbsoft.protectme;
 
 import com.rocketmbsoft.protectme.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 import android.provider.Contacts;
 import android.util.Log;
 
 /**
  * Example that shows finding a preference from the hierarchy and a custom preference type.
  */
 public class ProtectMePreferences extends PreferenceActivity implements Preference.OnPreferenceClickListener {
 
 
 	private static final int PICK_CONTACT = 54365;
 	private static final boolean D = false;
 	
 	private static final String TAG = "ProtectMePreferences";
 	
 	private CheckBoxPreference shakeCb = null;
 	private PreferenceScreen orientationPs = null;
 	private PreferenceScreen shakePs = null;
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Load the XML preferences file
 		addPreferencesFromResource(R.xml.preferences);
 
 		if (D) Log.d(TAG,"Prefernce Count : "+getPreferenceScreen().getPreferenceCount());
 
 		getPreferenceManager().findPreference("str_contact").setOnPreferenceClickListener(this);
 		
 		shakeCb = (CheckBoxPreference)getPreferenceScreen().findPreference(
                 "shake_check_box");
 		
 		orientationPs = (PreferenceScreen)getPreferenceScreen().findPreference(
         	"orientation_preference_screen");
 		
 		shakePs = (PreferenceScreen)getPreferenceScreen().findPreference(
     		"shake_preference_screen");
 		
 		shakeCb.setOnPreferenceClickListener(this);
 		
 		orientationPs.setEnabled(false);
 		shakeCb.setEnabled(false);
		shakeCb.setChecked(true);
 
 		updateTriggerMethod();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// Set up a listener whenever a key changes
 		// getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 
 		// Unregister the listener whenever a key changes
 		// getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
 
 	}
 
 	@Override
 	public void onActivityResult(int reqCode, int resultCode, Intent data) {
 		super.onActivityResult(reqCode, resultCode, data);
 
 		switch (reqCode) {
 		case (PICK_CONTACT) :
 			if (resultCode == Activity.RESULT_OK) {
 				Uri contactData = data.getData();
 				// Cursor c =  managedQuery(contactData, null, null, null, null);
 				
 				Cursor c = getContentResolver().query(contactData,
 		                new String[]{Contacts.People.DISPLAY_NAME, Contacts.Phones.NUMBER}, null, null, null);
 				
 				for (int i = 0; i < c.getColumnCount(); i++) {
 					if (D) Log.d(TAG,"Column Name : *"+c.getColumnName(i)+"*");
 				}
 				
 				if (c.moveToFirst()) {
 
 					String name = c.getString(0);
 					String phone = c.getString(1);
 					//String email = c.getString(c.getColumnIndexOrThrow(People.PRIMARY_EMAIL_ID));
 
 					if (D) Log.d(TAG,"Selected Name : "+name);
 					if (D) Log.d(TAG,"Selected Phone : "+phone);
 					//if (D) Log.d("AdvancedPreferences","Selected Email : "+email);
 
 					SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
 					editor.putString("contact_name", name);
 					editor.putString("contact_phone", phone);
 					//editor.putString("contact_email", email);
 					editor.commit();
 				}
 			}
 		break;
 		}
 	}
 
 	@Override
 	public boolean onPreferenceClick(Preference preference) {
 	
 		if (preference.getKey().equals("str_contact")) {
 		if (D) Log.d(TAG,"Trying to start intent");
 
 		Intent intent = new Intent(
 				Intent.ACTION_PICK, 
 				Contacts.Phones.CONTENT_URI);
 
 		startActivityForResult(intent, PICK_CONTACT);
 
 		} else if (preference.getKey().equals("shake_check_box")) {
 			updateTriggerMethod();
 		}
 		
 		return false;
 	}
 	
 	public void updateTriggerMethod() {
 
 		if (shakeCb.isChecked()) {
 			shakeCb.setTitle(R.string.shake_cb_enabled);
 			shakePs.setEnabled(true);
 			orientationPs.setEnabled(false);
 		} else {
 			shakeCb.setTitle(R.string.shake_cb_disabled);
 			shakePs.setEnabled(false);
 			orientationPs.setEnabled(true);
 		}
 	}
 
 }
