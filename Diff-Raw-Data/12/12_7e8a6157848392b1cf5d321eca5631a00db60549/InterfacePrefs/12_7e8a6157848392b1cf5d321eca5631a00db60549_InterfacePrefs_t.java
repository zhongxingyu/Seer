 package com.evervolv.EVParts.Preferences;
 
 
 import com.evervolv.EVParts.R;
 import com.evervolv.EVParts.R.bool;
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
 import android.preference.PreferenceCategory;
 import android.preference.EditTextPreference;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.widget.Toast;
 import android.util.Log;
 import android.provider.Settings;
 
 public class InterfacePrefs extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {
 
 	private static final String USE_SCREENOFF_ANIM = "pref_use_screenoff_anim";
     private static final String USE_SCREENON_ANIM = "pref_use_screenon_anim";
     private static final String BATTERY_STYLE = "pref_battery_style";
     private static final String HIDE_CLOCK_PREF = "pref_hide_clock";
     private static final String AM_PM_PREF = "pref_hide_ampm";
     private static final String USE_TRANSPARENT_STATUSBAR = "pref_use_transparent_statusbar";
     private static final String TRACKBALL_WAKE_PREF = "pref_trackball_wake";
     private static final String GENERAL_CATEGORY = "pref_category_general_interface";
     private static final String STATUSBAR_CATEGORY = "pref_category_statusbar_interface";
     
     private static final String HIDE_DATA_ICON_PREF = "pref_hide_data_icon";
     private static final String HIDE_BLUETOOTH_ICON_PREF = "pref_hide_bluetooth_icon";
     private static final String HIDE_BATTERY_ICON_PREF = "pref_hide_battery_icon";
     private static final String HIDE_SYNC_ICON_PREF = "pref_hide_sync_icon";
     private static final String HIDE_GPS_ICON_PREF = "pref_hide_gps_icon";
     private static final String HIDE_WIFI_ICON_PREF = "pref_hide_wifi_icon";
     private static final String HIDE_SIGNAL_ICON_PREF = "pref_hide_signal_icon";
     private static final String CLEAR_ICON_FLAGS_PREF = "pref_clear_icon_flags";
     
     private CheckBoxPreference mTrackballWakePref;
     private CheckBoxPreference mHideClock;
     private CheckBoxPreference mHideAmPm;
     private CheckBoxPreference mUseScreenOnAnim;
     private CheckBoxPreference mUseScreenOffAnim;
     private CheckBoxPreference mUseTransparentStatusBar;
     
     private CheckBoxPreference mHideDataIcon;
     private CheckBoxPreference mHideBluetoothIcon;
     private CheckBoxPreference mHideBatteryIcon;
     private CheckBoxPreference mHideSyncIcon;
     private CheckBoxPreference mHideGpsIcon;
     private CheckBoxPreference mHideWifiIcon;
     private CheckBoxPreference mHideSignalIcon;
     private Preference mClearIconFlags;
     
     
     private ListPreference mBatteryOption;
     
     private static final String TAG = "EVParts";
     private static final boolean DEBUG = false;
     
     private static final int ICONWIFI 			= 1;
     private static final int ICONGPS 			= 2;
     private static final int ICONSYNC 			= 4;
     private static final int ICONDATA 			= 8;
     private static final int ICONALARM 			= 16;
     private static final int ICONBLUETOOTH 		= 32;
     private static final int ICONSIGNAL 		= 64;
     private static final int ICONBATTERY 		= 128;
     private static final int ICONVOLUME 		= 256;
     
     private int sbIconFlags;
     
     
 	@Override
     public void onCreate(Bundle savedInstanceState) {	
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.interface_prefs);
 		PreferenceScreen prefSet = getPreferenceScreen();
 		
 		PreferenceCategory generalCategory = (PreferenceCategory) prefSet
 		.findPreference(GENERAL_CATEGORY);
 		
 		mUseScreenOnAnim = (CheckBoxPreference)prefSet.findPreference(USE_SCREENON_ANIM);
 		mUseScreenOnAnim.setChecked(Settings.System.getInt(getContentResolver(), 
 						Settings.System.USE_SCREENON_ANIM, 0) == 1);
 		
 		mUseScreenOffAnim = (CheckBoxPreference)prefSet.findPreference(USE_SCREENOFF_ANIM);
 		mUseScreenOffAnim.setChecked(Settings.System.getInt(getContentResolver(), 
 						Settings.System.USE_SCREENOFF_ANIM, 0) == 1);		
 		
 		mBatteryOption = (ListPreference) prefSet.findPreference(BATTERY_STYLE);
 		mBatteryOption.setOnPreferenceChangeListener(this);
 
 		mUseTransparentStatusBar = (CheckBoxPreference)prefSet.findPreference(USE_TRANSPARENT_STATUSBAR);
 		mUseTransparentStatusBar.setChecked(Settings.System.getInt(getContentResolver(), 
 						Settings.System.USE_TRANSPARENT_STATUSBAR, 1) == 1);	
 		
 		mHideClock = (CheckBoxPreference) prefSet.findPreference(HIDE_CLOCK_PREF);
 		mHideClock.setChecked(Settings.System.getInt(getContentResolver(), 
 						Settings.System.HIDE_CLOCK, 0) == 1);
 		mHideAmPm = (CheckBoxPreference) prefSet.findPreference(AM_PM_PREF);
 		mHideAmPm.setChecked(Settings.System.getInt(getContentResolver(), 
 						Settings.System.HIDE_CLOCK_AMPM, 0) == 1);
 
 
 		sbIconFlags = Settings.System.getInt(getContentResolver(), 
 				Settings.System.STATUSBAR_ICON_FLAGS, 0);
 		mHideDataIcon = (CheckBoxPreference)prefSet.findPreference(HIDE_DATA_ICON_PREF);
 		mHideDataIcon.setChecked(((sbIconFlags & ICONDATA) == ICONDATA));
 
 		mHideBluetoothIcon = (CheckBoxPreference)prefSet.findPreference(HIDE_BLUETOOTH_ICON_PREF);
 		mHideBluetoothIcon.setChecked(((sbIconFlags & ICONBLUETOOTH) == ICONBLUETOOTH));
 		
 		mHideBatteryIcon = (CheckBoxPreference)prefSet.findPreference(HIDE_BATTERY_ICON_PREF);
 		mHideBatteryIcon.setChecked(((sbIconFlags & ICONBATTERY) == ICONBATTERY));
 		
 		mHideSyncIcon = (CheckBoxPreference)prefSet.findPreference(HIDE_SYNC_ICON_PREF);
 		mHideSyncIcon.setChecked(((sbIconFlags & ICONSYNC) == ICONSYNC));
 		
 		mHideGpsIcon = (CheckBoxPreference)prefSet.findPreference(HIDE_GPS_ICON_PREF);
 		mHideGpsIcon.setChecked(((sbIconFlags & ICONGPS) == ICONGPS));
 		
 		mHideWifiIcon = (CheckBoxPreference)prefSet.findPreference(HIDE_WIFI_ICON_PREF);
 		mHideWifiIcon.setChecked(((sbIconFlags & ICONWIFI) == ICONWIFI));
 		
 		mHideSignalIcon = (CheckBoxPreference)prefSet.findPreference(HIDE_SIGNAL_ICON_PREF);
 		mHideSignalIcon.setChecked(((sbIconFlags & ICONSIGNAL) == ICONSIGNAL));
 		
 		mClearIconFlags = (Preference)prefSet.findPreference(CLEAR_ICON_FLAGS_PREF);
 		mClearIconFlags.setOnPreferenceClickListener(this);
 		
 		        /* Trackball Wake */
     	mTrackballWakePref = (CheckBoxPreference) prefSet.findPreference(TRACKBALL_WAKE_PREF);
     	mTrackballWakePref.setChecked(Settings.System.getInt(getContentResolver(),
             			Settings.System.TRACKBALL_WAKE_SCREEN, 0) == 1);
     	
 		if (!getResources().getBoolean(R.bool.has_trackball)) {
 			generalCategory.removePreference(mTrackballWakePref);
 		}
 		
     	if (mHideClock.isChecked()) {
     		mHideAmPm.setEnabled(false);
     	} else if (!mHideClock.isChecked()) {
     		mHideAmPm.setEnabled(true);
     	}
 		
     }
 	
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
         boolean value;
         int flagValue;
         
         if (preference == mHideClock) {
         	value = mHideClock.isChecked();
     	    	Settings.System.putInt(getContentResolver(), Settings.System.HIDE_CLOCK, value ? 1 : 0);
     	    	if (value) {
     	    		mHideAmPm.setEnabled(false);
     	    	} else if (!value) {
     	    		mHideAmPm.setEnabled(true);
     	    	}
     	} else if (preference == mHideAmPm) {
     		value = mHideAmPm.isChecked();
     	    	Settings.System.putInt(getContentResolver(), 
     	    			Settings.System.HIDE_CLOCK_AMPM, value ? 1 : 0);
    	    	Toast.makeText(this, R.string.toast_reboot_required, Toast.LENGTH_LONG).show();
     	} else if (preference == mUseTransparentStatusBar) {
     		value = mUseTransparentStatusBar.isChecked();
     	    	Settings.System.putInt(getContentResolver(), 
     	    			Settings.System.USE_TRANSPARENT_STATUSBAR, value ? 1 : 0);
    	    	Toast.makeText(this, R.string.toast_reboot_required, Toast.LENGTH_LONG).show();
     	} else if (preference == mUseScreenOnAnim) {
     		value = mUseScreenOnAnim.isChecked();
             	Settings.System.putInt(getContentResolver(), 
             			Settings.System.USE_SCREENON_ANIM, value ? 1 : 0);
         } else if (preference == mUseScreenOffAnim) {
         	value = mUseScreenOffAnim.isChecked();
             	Settings.System.putInt(getContentResolver(), 
             			Settings.System.USE_SCREENOFF_ANIM, value ? 1 : 0);
         } else if (preference == mTrackballWakePref) {
             value = mTrackballWakePref.isChecked();
             	Settings.System.putInt(getContentResolver(), 
             			Settings.System.TRACKBALL_WAKE_SCREEN, value ? 1 : 0);
         } else if (preference == mHideDataIcon) {
         	if (mHideDataIcon.isChecked()) {
         		flagValue = ((sbIconFlags | ICONDATA));
         	} else {
         		flagValue = ((sbIconFlags & ~ICONDATA));
         	}
         	Settings.System.putInt(getContentResolver(), 
         			Settings.System.STATUSBAR_ICON_FLAGS, flagValue);
         	sbIconFlags = flagValue;
         } else if (preference == mHideBluetoothIcon) {
         	if (mHideBluetoothIcon.isChecked()) {
         		flagValue = ((sbIconFlags | ICONBLUETOOTH));
         	} else {
         		flagValue = ((sbIconFlags & ~ICONBLUETOOTH));
         	}
         	Settings.System.putInt(getContentResolver(), 
         			Settings.System.STATUSBAR_ICON_FLAGS, flagValue);
         	sbIconFlags = flagValue;
         } else if (preference == mHideBatteryIcon) {
         	if (mHideBatteryIcon.isChecked()) {
         		flagValue = ((sbIconFlags | ICONBATTERY));
         	} else {
         		flagValue = ((sbIconFlags & ~ICONBATTERY));
         	}
         	Settings.System.putInt(getContentResolver(), 
         			Settings.System.STATUSBAR_ICON_FLAGS, flagValue);
         	sbIconFlags = flagValue;
         } else if (preference == mHideSyncIcon) {
         	if (mHideSyncIcon.isChecked()) {
         		flagValue = ((sbIconFlags | ICONSYNC));
         	} else {
         		flagValue = ((sbIconFlags & ~ICONSYNC));
         	}
         	Settings.System.putInt(getContentResolver(), 
         			Settings.System.STATUSBAR_ICON_FLAGS, flagValue);
         	sbIconFlags = flagValue;
         } else if (preference == mHideGpsIcon) {
         	if (mHideGpsIcon.isChecked()) {
         		flagValue = ((sbIconFlags | ICONGPS));
         	} else {
         		flagValue = ((sbIconFlags & ~ICONGPS));
         	}
         	Settings.System.putInt(getContentResolver(), 
         			Settings.System.STATUSBAR_ICON_FLAGS, flagValue);
         	sbIconFlags = flagValue;
         } else if (preference == mHideWifiIcon) {
         	if (mHideWifiIcon.isChecked()) {
         		flagValue = ((sbIconFlags | ICONWIFI));
         	} else {
         		flagValue = ((sbIconFlags & ~ICONWIFI));
         	}
         	Settings.System.putInt(getContentResolver(), 
         			Settings.System.STATUSBAR_ICON_FLAGS, flagValue);
         	sbIconFlags = flagValue;
         } else if (preference == mHideSignalIcon) {
         	if (mHideSignalIcon.isChecked()) {
         		flagValue = ((sbIconFlags | ICONSIGNAL));
         	} else {
         		flagValue = ((sbIconFlags & ~ICONSIGNAL));
         	}
         	Settings.System.putInt(getContentResolver(), 
         			Settings.System.STATUSBAR_ICON_FLAGS, flagValue);
         	sbIconFlags = flagValue;
         }
         
         return true;
     }
     
     public boolean onPreferenceChange(Preference preference, Object objValue) {
         if (preference == mBatteryOption) {
        	Settings.System.putInt(getContentResolver(), Settings.System.BATTERY_STYLE, 
        			Integer.valueOf((String) objValue));
	    	Toast.makeText(this, R.string.toast_reboot_required, Toast.LENGTH_LONG).show();
         }
         return true;
     }
 
 	@Override
 	public boolean onPreferenceClick(Preference preference) {
 		
 		if (preference == mClearIconFlags) {
         	Settings.System.putInt(getContentResolver(), 
         			Settings.System.STATUSBAR_ICON_FLAGS, 0);
         	mHideBluetoothIcon.setChecked(false);
         	mHideDataIcon.setChecked(false);
         	mHideBatteryIcon.setChecked(false);
         	mHideSyncIcon.setChecked(false);
         	mHideGpsIcon.setChecked(false);
         	mHideWifiIcon.setChecked(false);
         	mHideSignalIcon.setChecked(false);
         	sbIconFlags = 0;
 		}
 		return false;
 	}
     
 
 }
