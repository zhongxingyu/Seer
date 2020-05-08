 package com.evervolv.EVParts;
 
 
 import com.evervolv.EVParts.R;
 import com.evervolv.EVParts.R.xml;
 
 import android.app.ActivityManager;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.CheckBoxPreference;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.EditTextPreference;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.widget.Toast;
 import android.util.Log;
 import android.provider.Settings;
 
 
 public class UiOptions extends PreferenceActivity implements OnPreferenceChangeListener {
 
 	private static final String USE_SCREENOFF_ANIM = "use_screenoff_anim";
 	private static final String USE_SCREENON_ANIM = "use_screenon_anim";
 	private static final String BATTERY_OPTION = "battery_option";
     private static final String HIDE_CLOCK_PREF = "hide_clock";
     private static final String AM_PM_PREF = "hide_ampm";
     
     private CheckBoxPreference mHideClock;
     private CheckBoxPreference mHideAmPm;
 	private CheckBoxPreference mUseScreenOnAnim;
 	private CheckBoxPreference mUseScreenOffAnim;
 	private ListPreference mBatteryOption;
 	
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {	
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.ui_options);
 		PreferenceScreen prefSet = getPreferenceScreen();
 
 		mUseScreenOnAnim = (CheckBoxPreference)prefSet.findPreference(USE_SCREENON_ANIM);
 		mUseScreenOnAnim.setChecked(Settings.System.getInt(getContentResolver(), Settings.System.USE_SCREENON_ANIM, 1) == 1);
 		mUseScreenOffAnim = (CheckBoxPreference)prefSet.findPreference(USE_SCREENOFF_ANIM);
 		mUseScreenOffAnim.setChecked(Settings.System.getInt(getContentResolver(), Settings.System.USE_SCREENOFF_ANIM, 1) == 1);		
 		
 		mBatteryOption = (ListPreference) prefSet.findPreference(BATTERY_OPTION);
 		mBatteryOption.setOnPreferenceChangeListener(this);
 		
 		mHideClock = (CheckBoxPreference) prefSet.findPreference(HIDE_CLOCK_PREF);
 		mHideClock.setOnPreferenceChangeListener(this);
 		mHideClock.setChecked(Settings.System.getInt(getContentResolver(), Settings.System.SHOW_STATUS_CLOCK, 1) == 0);
 		mHideAmPm = (CheckBoxPreference) prefSet.findPreference(AM_PM_PREF);
 		mHideAmPm.setOnPreferenceChangeListener(this);
		mHideAmPm.setChecked(Settings.System.getInt(getContentResolver(), Settings.System.SHOW_TWELVE_HOUR_CLOCK_PERIOD, 1) == 0);
 		
     }
 
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
         
         if (preference == mUseScreenOnAnim) {
         	value = mUseScreenOnAnim.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.USE_SCREENON_ANIM, value ? 1 : 0);
         } else if (preference == mUseScreenOffAnim) {
         	value = mUseScreenOffAnim.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.USE_SCREENOFF_ANIM, value ? 1 : 0);
         }
         
         return true;
     }
 
     public boolean onPreferenceChange(Preference preference, Object objValue) {
         if (preference == mBatteryOption) {
         	Settings.System.putInt(getContentResolver(), Settings.System.BATTERY_OPTION, Integer.valueOf((String) objValue));
         } else if (preference == mHideClock) {
     	    Settings.System.putInt(getContentResolver(), Settings.System.SHOW_STATUS_CLOCK, mHideClock.isChecked() ? 1 : 0);
     	} else if (preference == mHideAmPm) {
     	    Settings.System.putInt(getContentResolver(), Settings.System.SHOW_TWELVE_HOUR_CLOCK_PERIOD, mHideAmPm.isChecked() ? 1 : 0);
         }
         // always let the preference setting proceed.
         return true;
     }
     
 
 }
