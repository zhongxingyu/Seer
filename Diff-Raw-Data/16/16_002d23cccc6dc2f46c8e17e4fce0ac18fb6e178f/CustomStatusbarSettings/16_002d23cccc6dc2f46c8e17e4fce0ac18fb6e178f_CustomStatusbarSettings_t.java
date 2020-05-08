 /*
  * Copyright (C) 2010 The Android Open Source Project
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
 
 package com.android.settings;
 
 
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.Log;
 
 
 
 public class CustomStatusbarSettings extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {
     
 	private final String Tranq_Settings = "TRANQ_SETTINGS";
 	private final String SHOW_CLOCK = "show_clock";
 	private final String CLOCK_AMPM = "clock_ampm";
 	private final String CLOCK_COLOR = "clock_color";
 	private final String CLOCK_SIZE = "clock_size";
 	
     
 	private PreferenceManager prefMgr;
 	private SharedPreferences sharedPref;
 	private CheckBoxPreference mShowClock;
 	private CheckBoxPreference mClockAmPm;
     private Preference mClockColor;
     private Preference mClockSize;
     public int mSize = 17;
     

 
 	
 	
 	
 	
     /** If there is no setting in the provider, use this. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         prefMgr = getPreferenceManager();
         prefMgr.setSharedPreferencesName("Tranquility_Settings");
         prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
         prefMgr.getSharedPreferences();
 
         addPreferencesFromResource(R.xml.custom_statusbar_settings);
         
         mShowClock = (CheckBoxPreference) findPreference(SHOW_CLOCK);
 		mShowClock.setOnPreferenceChangeListener(this);
         mClockAmPm = (CheckBoxPreference) findPreference(CLOCK_AMPM);
 		mClockAmPm.setOnPreferenceChangeListener(this);
         mClockColor = (Preference) findPreference(CLOCK_COLOR);
 		mClockColor.setOnPreferenceChangeListener(this);
 	    mClockSize = (Preference) findPreference(CLOCK_SIZE);
 		mClockSize.setOnPreferenceChangeListener(this);
 		mSize = prefMgr.getSharedPreferences().getInt(CLOCK_SIZE, 17);  
 
 
         
  
     }
 
     
     @Override
     public void onResume() {
         super.onResume();
           
     }
 
     
     @Override
     public void onPause() {
         super.onPause();
 
     }
 
  
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
 
     	if (preference == mClockSize) {
 			new NumberPickerDialog(preferenceScreen.getContext(),
                     clockSizeListener,
             		(int) mSize,
                     5,
                     30,
                     R.string.clock_size).show();
         }
     	
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
     
 
     public boolean onPreferenceChange(Preference preference, Object objValue) {
   
      	final String key = preference.getKey();
         if (SHOW_CLOCK.equals(key)) {
         	Intent i = new Intent();
         	i.setAction(Tranq_Settings );
        	   	i.putExtra("ShowClock", (Boolean) objValue);
        	   	getActivity().sendBroadcast(i);
        	   	i = null;
        
         } else if (CLOCK_AMPM.equals(key)) {
         	Intent i = new Intent();
             i.setAction(Tranq_Settings );
             i.putExtra("ClockAmPm", (Boolean) objValue);
             getActivity().sendBroadcast(i);
             i = null;
         
         } else if (CLOCK_COLOR.equals(key)) {
         	Intent i = new Intent();
             i.setAction(Tranq_Settings );
             i.putExtra("ClockColor", (Integer) objValue);
             getActivity().sendBroadcast(i);
             i = null;
             
         } else if (CLOCK_SIZE.equals(key)) {
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(CLOCK_SIZE, mSize);
             editor.commit();
 
         	Intent i = new Intent();
             i.setAction(Tranq_Settings );
             i.putExtra("ClockSize", (Integer) mSize);
             getActivity().sendBroadcast(i);
             i = null;
 

             
         }
 
  
     
 
         return true;
     }
     
     
     NumberPickerDialog.OnNumberSetListener clockSizeListener =
             new NumberPickerDialog.OnNumberSetListener() {
     	public void onNumberSet(int limit) {
     		mSize = (int) limit;
     		mClockSize.getOnPreferenceChangeListener().onPreferenceChange(mClockSize, (int) limit);
     	}
     	};      
     
 }
