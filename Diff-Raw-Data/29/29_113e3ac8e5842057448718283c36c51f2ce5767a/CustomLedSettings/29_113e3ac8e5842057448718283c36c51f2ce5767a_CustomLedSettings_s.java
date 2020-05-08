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
 
 
 
 import java.text.DecimalFormat;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.database.Cursor;
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
 
 
 
 public class CustomLedSettings extends SettingsPreferenceFragment implements
         Preference.OnPreferenceChangeListener {
  
 
 	private final String Tranq_Settings = "TRANQ_SETTINGS";
 	private final String DEFAULT_LED_COLOR = "default_led_color";	
 	private final String DEFAULT_LED_ON_MS = "default_led_on_ms";
 	private final String DEFAULT_LED_OFF_MS = "default_led_off_ms";
 	private final String PULSE_LED_SCREEN_ON = "pulse_led_screen_on";
 	
 	private final String INCOMING_CALL_LED_COLOR = "incoming_call_color";
 	private final String INCOMING_CALL_LED_PULSE = "incoming_call_pulse";
 	private final String INCOMING_CALL_LED_ON_MS = "incoming_led_on_ms";
 	private final String INCOMING_CALL_LED_OFF_MS = "incoming_led_off_ms";
 	
 	private final String MISSED_CALL_LED_COLOR = "missed_call_color";
 	private final String MISSED_CALL_LED_PULSE = "missed_call_pulse";
 	private final String MISSED_CALL_LED_ON_MS = "missed_call_led_on_ms";
 	private final String MISSED_CALL_LED_OFF_MS = "missed_call_led_off_ms";
 	
 	private final String VOICE_MAIL_LED_COLOR = "voice_mail_color";
 	private final String VOICE_MAIL_LED_PULSE = "voice_mail_pulse";
 	private final String VOICE_MAIL_LED_ON_MS = "voice_mail_led_on_ms";
 	private final String VOICE_MAIL_LED_OFF_MS = "voice_mail_led_off_ms";
 	
 	private PreferenceManager prefMgr;
 	private SharedPreferences sharedPref;
 	private Preference mDefaultLedColor;
 	private Preference mDefaultLedOnMs;
 	private Preference mDefaultLedOffMs;
 	private CheckBoxPreference mPulseLedScreenOn;
     private int DefaultLedOnMs;
     private int DefaultLedOffMs;
     private Boolean PulseLedScreenOn;
     
     
     private Preference mIncomingCallColor;
 	private CheckBoxPreference mIncomingCallPulse;
 	private Preference mIncomingCallLedOnMs;
 	private Preference mIncomingCallLedOffMs;
 	private Boolean IncomingCallPulse;
 	private int IncomingCallLedOnMs;
     private int IncomingCallLedOffMs;
     
 
     private Preference mMissedCallColor;
 	private CheckBoxPreference mMissedCallPulse;
 	private Preference mMissedCallLedOnMs;
 	private Preference mMissedCallLedOffMs;
 	private Boolean MissedCallPulse;
 	private int MissedCallLedOnMs;
     private int MissedCallLedOffMs;
 	
     private Preference mVoiceMailColor;
 	private CheckBoxPreference mVoiceMailPulse;
 	private Preference mVoiceMailLedOnMs;
 	private Preference mVoiceMailLedOffMs;
 	private Boolean VoiceMailPulse;
 	private int VoiceMailLedOnMs;
     private int VoiceMailLedOffMs;
 	
 	
     /** If there is no setting in the provider, use this. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         prefMgr = getPreferenceManager();
         prefMgr.setSharedPreferencesName("Tranquility_Settings");
         prefMgr.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
         prefMgr.getSharedPreferences();
         
         addPreferencesFromResource(R.xml.custom_led_settings);
         
         mDefaultLedColor = (Preference) findPreference(DEFAULT_LED_COLOR);
         mDefaultLedColor.setOnPreferenceChangeListener(this);
         mDefaultLedOnMs = (Preference) findPreference(DEFAULT_LED_ON_MS);
         mDefaultLedOnMs.setOnPreferenceChangeListener(this);
         mDefaultLedOffMs = (Preference) findPreference(DEFAULT_LED_OFF_MS);
         mDefaultLedOffMs.setOnPreferenceChangeListener(this);
         mPulseLedScreenOn = (CheckBoxPreference) findPreference(PULSE_LED_SCREEN_ON);
         mPulseLedScreenOn.setOnPreferenceChangeListener(this);
 
 
         mIncomingCallColor = (Preference) findPreference(INCOMING_CALL_LED_COLOR);
         mIncomingCallColor.setOnPreferenceChangeListener(this);
         mIncomingCallPulse = (CheckBoxPreference) findPreference(INCOMING_CALL_LED_PULSE);
         mIncomingCallPulse.setOnPreferenceChangeListener(this);
         mIncomingCallLedOnMs = (Preference) findPreference(INCOMING_CALL_LED_ON_MS);
         mIncomingCallLedOnMs.setOnPreferenceChangeListener(this);
         mIncomingCallLedOffMs = (Preference) findPreference(INCOMING_CALL_LED_OFF_MS);
         mIncomingCallLedOffMs.setOnPreferenceChangeListener(this);        
         
         mMissedCallColor = (Preference) findPreference(MISSED_CALL_LED_COLOR);
         mMissedCallColor.setOnPreferenceChangeListener(this);
         mMissedCallPulse = (CheckBoxPreference) findPreference(MISSED_CALL_LED_PULSE);
         mMissedCallPulse.setOnPreferenceChangeListener(this);
         mMissedCallLedOnMs = (Preference) findPreference(MISSED_CALL_LED_ON_MS);
         mMissedCallLedOnMs.setOnPreferenceChangeListener(this);
         mMissedCallLedOffMs = (Preference) findPreference(MISSED_CALL_LED_OFF_MS);
         mMissedCallLedOffMs.setOnPreferenceChangeListener(this);                
         
    
         mVoiceMailColor = (Preference) findPreference(VOICE_MAIL_LED_COLOR);
         mVoiceMailColor.setOnPreferenceChangeListener(this);
         mVoiceMailPulse = (CheckBoxPreference) findPreference(VOICE_MAIL_LED_PULSE);
         mVoiceMailPulse.setOnPreferenceChangeListener(this);
         mVoiceMailLedOnMs = (Preference) findPreference(VOICE_MAIL_LED_ON_MS);
         mVoiceMailLedOnMs.setOnPreferenceChangeListener(this);
         mVoiceMailLedOffMs = (Preference) findPreference(VOICE_MAIL_LED_OFF_MS);
         mVoiceMailLedOffMs.setOnPreferenceChangeListener(this);            
         
         
         
         
         
 		Cursor cur = Settings.NotifOptions.getDefaultLed(getActivity().getBaseContext().getContentResolver());
 		sharedPref = prefMgr.getSharedPreferences();
     	SharedPreferences.Editor editor = sharedPref.edit();
     	editor.putBoolean(PULSE_LED_SCREEN_ON, cur.getString(2).equals("PulseScreenOn=true"));
         editor.putInt(DEFAULT_LED_COLOR, cur.getInt(3));
         editor.putInt(DEFAULT_LED_ON_MS, cur.getInt(4));
         editor.putInt(DEFAULT_LED_OFF_MS, cur.getInt(5));
         editor.commit();
 
 		cur = Settings.NotifOptions.getIncomingCallLed(getActivity().getBaseContext().getContentResolver());
     	IncomingCallPulse = cur.getString(2).equals("true");
 		editor.putBoolean(INCOMING_CALL_LED_PULSE, IncomingCallPulse);
		if (!IncomingCallPulse) {
			IncomingCallLedOnMs = -1;
			IncomingCallLedOffMs = -1;
		} else {
			IncomingCallLedOnMs = cur.getInt(4);
			IncomingCallLedOffMs = cur.getInt(5);
		}
         editor.putInt(INCOMING_CALL_LED_COLOR, cur.getInt(3));
         editor.putInt(INCOMING_CALL_LED_ON_MS, IncomingCallLedOnMs);
         editor.putInt(INCOMING_CALL_LED_OFF_MS, IncomingCallLedOffMs);
         editor.commit();        
         
         
 		cur = Settings.NotifOptions.getMissedCallLed(getActivity().getBaseContext().getContentResolver());
     	MissedCallPulse = cur.getString(2).equals("true");
 		editor.putBoolean(MISSED_CALL_LED_PULSE, MissedCallPulse);
		if (!MissedCallPulse) {
			MissedCallLedOnMs = -1;
			MissedCallLedOffMs = -1;
		} else {
			MissedCallLedOnMs = cur.getInt(4);
			MissedCallLedOffMs = cur.getInt(5);
		}
         editor.putInt(MISSED_CALL_LED_COLOR, cur.getInt(3));
         editor.putInt(MISSED_CALL_LED_ON_MS, MissedCallLedOnMs);
         editor.putInt(MISSED_CALL_LED_OFF_MS, MissedCallLedOffMs);
         editor.commit();             
         
 		cur = Settings.NotifOptions.getVoiceMailLed(getActivity().getBaseContext().getContentResolver());
     	VoiceMailPulse = cur.getString(2).equals("true");
 		editor.putBoolean(VOICE_MAIL_LED_PULSE, VoiceMailPulse);
		if (!VoiceMailPulse) {
			VoiceMailLedOnMs = -1;
			VoiceMailLedOffMs = -1;
		} else {
			VoiceMailLedOnMs = cur.getInt(4);
			VoiceMailLedOffMs = cur.getInt(5);
		}
         editor.putInt(VOICE_MAIL_LED_COLOR, cur.getInt(3));
         editor.putInt(VOICE_MAIL_LED_ON_MS, VoiceMailLedOnMs);
         editor.putInt(VOICE_MAIL_LED_OFF_MS, VoiceMailLedOffMs);
         editor.commit();          
         
         
         
         DefaultLedOnMs = prefMgr.getSharedPreferences().getInt(DEFAULT_LED_ON_MS, 3);
         DefaultLedOffMs = prefMgr.getSharedPreferences().getInt(DEFAULT_LED_OFF_MS, 3);
         PulseLedScreenOn = prefMgr.getSharedPreferences().getBoolean(PULSE_LED_SCREEN_ON, false);
         
         IncomingCallPulse = prefMgr.getSharedPreferences().getBoolean(INCOMING_CALL_LED_PULSE, true);
     	IncomingCallLedOnMs = prefMgr.getSharedPreferences().getInt(INCOMING_CALL_LED_ON_MS, 3);
         IncomingCallLedOffMs = prefMgr.getSharedPreferences().getInt(INCOMING_CALL_LED_OFF_MS, 3);
         
         MissedCallPulse = prefMgr.getSharedPreferences().getBoolean(MISSED_CALL_LED_PULSE, true);
         MissedCallLedOnMs = prefMgr.getSharedPreferences().getInt(MISSED_CALL_LED_ON_MS, 3);
         MissedCallLedOffMs = prefMgr.getSharedPreferences().getInt(MISSED_CALL_LED_OFF_MS, 3);
         
         VoiceMailPulse = prefMgr.getSharedPreferences().getBoolean(VOICE_MAIL_LED_PULSE, true);
         VoiceMailLedOnMs = prefMgr.getSharedPreferences().getInt(VOICE_MAIL_LED_ON_MS, 3);
         VoiceMailLedOffMs = prefMgr.getSharedPreferences().getInt(VOICE_MAIL_LED_OFF_MS, 3);
 		
         
     }
 
     private void updateDb(){
  		 ContentValues values = new ContentValues(4);
          // Write the default led option values to the database     
  		 values.put(Settings.NotifOptions.PKG_NAME, "PulseScreenOn=" + PulseLedScreenOn.toString());
  		 values.put(Settings.NotifOptions.LED_COLOR, prefMgr.getSharedPreferences().getInt(DEFAULT_LED_COLOR, -1));
          values.put(Settings.NotifOptions.LED_ON_MS, DefaultLedOnMs);
          values.put(Settings.NotifOptions.LED_OFF_MS, DefaultLedOffMs);
           
          Settings.NotifOptions.updateDefaultLed(getActivity().getBaseContext().getContentResolver(), values);
          values = null;
          
  		 values = new ContentValues(4);
          // Write the default led option values to the database     
  		 values.put(Settings.NotifOptions.PKG_NAME, IncomingCallPulse.toString());
  		 values.put(Settings.NotifOptions.LED_COLOR, prefMgr.getSharedPreferences().getInt(INCOMING_CALL_LED_COLOR, -1));
          values.put(Settings.NotifOptions.LED_ON_MS, IncomingCallLedOnMs);
          values.put(Settings.NotifOptions.LED_OFF_MS, IncomingCallLedOffMs);
           
          Settings.NotifOptions.updateIncomingCallLed(getActivity().getBaseContext().getContentResolver(), values);   
          values = null;
          
  		 values = new ContentValues(4);
          // Write the default led option values to the database     
  		 values.put(Settings.NotifOptions.PKG_NAME, MissedCallPulse.toString());
  		 values.put(Settings.NotifOptions.LED_COLOR, prefMgr.getSharedPreferences().getInt(MISSED_CALL_LED_COLOR, -1));
          values.put(Settings.NotifOptions.LED_ON_MS, MissedCallLedOnMs);
          values.put(Settings.NotifOptions.LED_OFF_MS, MissedCallLedOffMs);
           
          Settings.NotifOptions.updateMissedCallLed(getActivity().getBaseContext().getContentResolver(), values);   
          values = null;         
          
          
  		 values = new ContentValues(4);
          // Write the default led option values to the database     
  		 values.put(Settings.NotifOptions.PKG_NAME, VoiceMailPulse.toString());
  		 values.put(Settings.NotifOptions.LED_COLOR, prefMgr.getSharedPreferences().getInt(VOICE_MAIL_LED_COLOR, -1));
          values.put(Settings.NotifOptions.LED_ON_MS, VoiceMailLedOnMs);
          values.put(Settings.NotifOptions.LED_OFF_MS, VoiceMailLedOffMs);
           
          Settings.NotifOptions.updateVoiceMailLed(getActivity().getBaseContext().getContentResolver(), values);   
          values = null;     
          
          
          
          
          
          
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
        
     	if (preference == mDefaultLedOnMs) {
             new NumberPickerDialog(preference.getContext(),
                     mDefaultLedOnListener,
                     DefaultLedOnMs,
                     1,
                     50,
                     R.string.led_on_ms).show();
         } else if (preference == mDefaultLedOffMs) {
             new NumberPickerDialog(preference.getContext(),
                     mDefaultLedOffListener,
                     DefaultLedOffMs,
                     1,
                     50,
                     R.string.led_off_ms).show();
     	
     	} else if (preference == mIncomingCallLedOnMs) {
     		new NumberPickerDialog(preference.getContext(),
     				mIncomingCallLedOnListener,
     				IncomingCallLedOnMs,
     				1,
     				50,
     				R.string.led_on_ms).show();
     	
     	} else if (preference == mIncomingCallLedOffMs) {
     		new NumberPickerDialog(preference.getContext(),
     				mIncomingCallLedOffListener,
     				IncomingCallLedOffMs,
     				1,
     				50,
     				R.string.led_off_ms).show();
     	
     	} else if (preference == mMissedCallLedOnMs) {
     		new NumberPickerDialog(preference.getContext(),
     				mMissedCallLedOnListener,
     				MissedCallLedOnMs,
     				1,
     				50,
     				R.string.led_on_ms).show();
     	
     	} else if (preference == mMissedCallLedOffMs) {
     		new NumberPickerDialog(preference.getContext(),
     				mMissedCallLedOffListener,
     				MissedCallLedOffMs,
     				1,
     				50,
     				R.string.led_off_ms).show();
     	
     	} else if (preference == mVoiceMailLedOnMs) {
     		new NumberPickerDialog(preference.getContext(),
     				mVoiceMailLedOnListener,
     				VoiceMailLedOnMs,
     				1,
     				50,
     				R.string.led_on_ms).show();
     	
     	} else if (preference == mVoiceMailLedOffMs) {
     		new NumberPickerDialog(preference.getContext(),
     				mVoiceMailLedOffListener,
     				VoiceMailLedOffMs,
     				1,
     				50,
     				R.string.led_off_ms).show();
     	}
     	
     	
     	
 
         return super.onPreferenceTreeClick(preferenceScreen, preference);
     }
     
 
     public boolean onPreferenceChange(Preference preference, Object objValue) {
     	
     	final String key = preference.getKey();
      	if (DEFAULT_LED_COLOR.equals(key)) {
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(DEFAULT_LED_COLOR, (Integer) objValue);
             editor.commit();
             
         } else if (DEFAULT_LED_ON_MS.equals(key)) {
         	sharedPref = prefMgr.getSharedPreferences();
         	SharedPreferences.Editor editor = sharedPref.edit();
             editor.putInt(DEFAULT_LED_ON_MS, DefaultLedOnMs);
             editor.commit();
 
         } else if (DEFAULT_LED_OFF_MS.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putInt(DEFAULT_LED_OFF_MS, DefaultLedOffMs);
             editor.commit();
             
         } else if (PULSE_LED_SCREEN_ON.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putBoolean(PULSE_LED_SCREEN_ON, (Boolean) objValue);
            	PulseLedScreenOn = (Boolean) objValue;
             editor.commit();
             
         } else if (INCOMING_CALL_LED_PULSE.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putBoolean(INCOMING_CALL_LED_PULSE, (Boolean) objValue);
            	IncomingCallPulse = (Boolean) objValue;
             editor.commit();
         
         } else if (INCOMING_CALL_LED_COLOR.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putInt(INCOMING_CALL_LED_COLOR, (Integer) objValue);
             editor.commit();
         
         } else if (INCOMING_CALL_LED_ON_MS.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putInt(INCOMING_CALL_LED_ON_MS, IncomingCallLedOnMs);
             editor.commit();
        
         } else if (INCOMING_CALL_LED_OFF_MS.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putInt(INCOMING_CALL_LED_OFF_MS, IncomingCallLedOffMs);
             editor.commit();
         
         } else if (MISSED_CALL_LED_PULSE.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putBoolean(MISSED_CALL_LED_PULSE, (Boolean) objValue);
            	MissedCallPulse = (Boolean) objValue;
             editor.commit();
         
         } else if (MISSED_CALL_LED_COLOR.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putInt(MISSED_CALL_LED_COLOR, (Integer) objValue);
             editor.commit();
         
         } else if (MISSED_CALL_LED_ON_MS.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putInt(MISSED_CALL_LED_ON_MS, MissedCallLedOnMs);
             editor.commit();
        
         } else if (MISSED_CALL_LED_OFF_MS.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putInt(MISSED_CALL_LED_OFF_MS, MissedCallLedOffMs);
             editor.commit();
       
         }  else if (VOICE_MAIL_LED_PULSE.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putBoolean(VOICE_MAIL_LED_PULSE, (Boolean) objValue);
            	VoiceMailPulse = (Boolean) objValue;
             editor.commit();
         
         } else if (VOICE_MAIL_LED_COLOR.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putInt(VOICE_MAIL_LED_COLOR, (Integer) objValue);
             editor.commit();
         
         } else if (VOICE_MAIL_LED_ON_MS.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putInt(VOICE_MAIL_LED_ON_MS, VoiceMailLedOnMs);
             editor.commit();
        
         } else if (VOICE_MAIL_LED_OFF_MS.equals(key)) {
           	sharedPref = prefMgr.getSharedPreferences();
            	SharedPreferences.Editor editor = sharedPref.edit();
            	editor.putInt(VOICE_MAIL_LED_OFF_MS, VoiceMailLedOffMs);
             editor.commit();
       
         }
      	
      	
      	
         updateDb();    
         return true;
     }
     
 	NumberPickerDialog.OnNumberSetListener mDefaultLedOnListener = new NumberPickerDialog.OnNumberSetListener() {
 		public void onNumberSet(int limit) {
 			SharedPreferences.Editor editor = PreferenceManager
 					.getDefaultSharedPreferences(getActivity().getBaseContext())
 					.edit();
 			editor.putInt(DEFAULT_LED_ON_MS, limit);
 			editor.apply();
 			DecimalFormat numf = new DecimalFormat("#.##");
 			double mTime = ((double) (limit) / (double) (10));
 			mDefaultLedOnMs.setSummary(numf.format(mTime) + " seconds");
 			DefaultLedOnMs = limit;
 			mDefaultLedOnMs.getOnPreferenceChangeListener().onPreferenceChange(
 					mDefaultLedOnMs, DefaultLedOnMs);
 		}
 	};
 
 	NumberPickerDialog.OnNumberSetListener mDefaultLedOffListener = new NumberPickerDialog.OnNumberSetListener() {
 		public void onNumberSet(int limit) {
 			SharedPreferences.Editor editor = PreferenceManager
 					.getDefaultSharedPreferences(getActivity().getBaseContext())
 					.edit();
 
 			editor.putInt(DEFAULT_LED_OFF_MS, limit);
 			editor.apply();
 			DecimalFormat numf = new DecimalFormat("#.##");
 			double mTime = ((double) (limit) / (double) (10));
 			mDefaultLedOffMs.setSummary(numf.format(mTime) + " seconds");
 			DefaultLedOffMs = limit;
 			mDefaultLedOffMs.getOnPreferenceChangeListener()
 					.onPreferenceChange(mDefaultLedOffMs, DefaultLedOffMs);
 		}
 	};    
 
 	NumberPickerDialog.OnNumberSetListener mIncomingCallLedOnListener = new NumberPickerDialog.OnNumberSetListener() {
 		public void onNumberSet(int limit) {
 			SharedPreferences.Editor editor = PreferenceManager
 					.getDefaultSharedPreferences(getActivity().getBaseContext())
 					.edit();
 
 			editor.putInt(INCOMING_CALL_LED_ON_MS, limit);
 			editor.apply();
 			DecimalFormat numf = new DecimalFormat("#.##");
 			double mTime = ((double) (limit) / (double) (10));
 			mIncomingCallLedOnMs.setSummary(numf.format(mTime) + " seconds");
 			IncomingCallLedOnMs = limit;
 			mIncomingCallLedOnMs.getOnPreferenceChangeListener()
 					.onPreferenceChange(mIncomingCallLedOnMs,
 							IncomingCallLedOnMs);
 		}
 	};    
 
                 
 	NumberPickerDialog.OnNumberSetListener mIncomingCallLedOffListener = new NumberPickerDialog.OnNumberSetListener() {
 		public void onNumberSet(int limit) {
 			SharedPreferences.Editor editor = PreferenceManager
 					.getDefaultSharedPreferences(getActivity().getBaseContext())
 					.edit();
 
 			editor.putInt(INCOMING_CALL_LED_OFF_MS, limit);
 			editor.apply();
 			DecimalFormat numf = new DecimalFormat("#.##");
 			double mTime = ((double) (limit) / (double) (10));
 			mIncomingCallLedOffMs.setSummary(numf.format(mTime) + " seconds");
 			IncomingCallLedOffMs = limit;
 			mIncomingCallLedOffMs.getOnPreferenceChangeListener()
 					.onPreferenceChange(mIncomingCallLedOffMs,
 							IncomingCallLedOffMs);
 		}
 	};
 
 	NumberPickerDialog.OnNumberSetListener mMissedCallLedOnListener = new NumberPickerDialog.OnNumberSetListener() {
 		public void onNumberSet(int limit) {
 			SharedPreferences.Editor editor = PreferenceManager
 					.getDefaultSharedPreferences(getActivity().getBaseContext())
 					.edit();
 
 			editor.putInt(MISSED_CALL_LED_ON_MS, limit);
 			editor.apply();
 			DecimalFormat numf = new DecimalFormat("#.##");
 			double mTime = ((double) (limit) / (double) (10));
 			mMissedCallLedOnMs.setSummary(numf.format(mTime) + " seconds");
 			MissedCallLedOnMs = limit;
 			mMissedCallLedOnMs.getOnPreferenceChangeListener()
 					.onPreferenceChange(mMissedCallLedOnMs, MissedCallLedOnMs);
 		}
 	};
 
 	NumberPickerDialog.OnNumberSetListener mMissedCallLedOffListener = new NumberPickerDialog.OnNumberSetListener() {
 		public void onNumberSet(int limit) {
 			SharedPreferences.Editor editor = PreferenceManager
 					.getDefaultSharedPreferences(getActivity().getBaseContext())
 					.edit();
 
 			editor.putInt(MISSED_CALL_LED_OFF_MS, limit);
 			editor.apply();
 			DecimalFormat numf = new DecimalFormat("#.##");
 			double mTime = ((double) (limit) / (double) (10));
 			mMissedCallLedOffMs.setSummary(numf.format(mTime) + " seconds");
 			MissedCallLedOffMs = limit;
 			mMissedCallLedOffMs
 					.getOnPreferenceChangeListener()
 					.onPreferenceChange(mMissedCallLedOffMs, MissedCallLedOffMs);
 		}
 	};
 
 	NumberPickerDialog.OnNumberSetListener mVoiceMailLedOnListener = new NumberPickerDialog.OnNumberSetListener() {
 		public void onNumberSet(int limit) {
 			SharedPreferences.Editor editor = PreferenceManager
 					.getDefaultSharedPreferences(getActivity().getBaseContext())
 					.edit();
 
 			editor.putInt(VOICE_MAIL_LED_ON_MS, limit);
 			editor.apply();
 			DecimalFormat numf = new DecimalFormat("#.##");
 			double mTime = ((double) (limit) / (double) (10));
 			mVoiceMailLedOnMs.setSummary(numf.format(mTime) + " seconds");
 			VoiceMailLedOnMs = limit;
 			mVoiceMailLedOnMs.getOnPreferenceChangeListener()
 					.onPreferenceChange(mVoiceMailLedOnMs, VoiceMailLedOnMs);
 		}
 	};
 
 	NumberPickerDialog.OnNumberSetListener mVoiceMailLedOffListener = new NumberPickerDialog.OnNumberSetListener() {
 		public void onNumberSet(int limit) {
 			SharedPreferences.Editor editor = PreferenceManager
 					.getDefaultSharedPreferences(getActivity().getBaseContext())
 					.edit();
 
 			editor.putInt(VOICE_MAIL_LED_OFF_MS, limit);
 			editor.apply();
 			DecimalFormat numf = new DecimalFormat("#.##");
 			double mTime = ((double) (limit) / (double) (10));
 			mVoiceMailLedOffMs.setSummary(numf.format(mTime) + " seconds");
 			VoiceMailLedOffMs = limit;
 			mVoiceMailLedOffMs.getOnPreferenceChangeListener()
 					.onPreferenceChange(mVoiceMailLedOffMs, VoiceMailLedOffMs);
 		}
 	};                
                 
                 
                 
 }
