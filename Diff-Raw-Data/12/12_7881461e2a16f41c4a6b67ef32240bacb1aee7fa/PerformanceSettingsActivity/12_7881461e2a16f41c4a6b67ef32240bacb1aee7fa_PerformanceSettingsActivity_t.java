 /*
  * Copyright (C) 2011 The CyanogenMod Project
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
 
 package com.cyanogenmod.cmparts.activities;
 
 import com.cyanogenmod.cmparts.R;
 import com.cyanogenmod.cmparts.activities.CPUActivity;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.os.SystemProperties;
 import android.preference.CheckBoxPreference;
 import android.content.SharedPreferences;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * Performance Settings
  */
 public class PerformanceSettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
 
     private static final String PREF_SQUADZONE = "squadkeys";
 
     private static final String GENERAL_CATEGORY = "general_category";
 
     private static final String JIT_PREF = "pref_jit_mode";
 
     private static final String JIT_ENABLED = "int:jit";
 
     private static final String JIT_DISABLED = "int:fast";
 
     private static final String JIT_PERSIST_PROP = "persist.sys.jit-mode";
 
     private static final String JIT_PROP = "dalvik.vm.execution-mode";
 
     private static final String HEAPSIZE_PREF = "pref_heapsize";
 
     private static final String HEAPSIZE_PROP = "dalvik.vm.heapsize";
 
     private static final String HEAPSIZE_PERSIST_PROP = "persist.sys.vm.heapsize";
 
     private static final String HEAPSIZE_DEFAULT = "16m";
 
     private static final String USE_DITHERING_PREF = "pref_use_dithering";
 
     private static final String USE_DITHERING_PERSIST_PROP = "persist.sys.use_dithering";
     
     private static final String USE_DITHERING_DEFAULT = "1";
 
     private static final String USE_16BPP_ALPHA_PREF = "pref_use_16bpp_alpha";
 
     private static final String USE_16BPP_ALPHA_PROP = "persist.sys.use_16bpp_alpha";
 
     private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
 	
     private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
 	
    private static final String SCROLLINGCACHE_DEFAULT = "0";
 
     private static final String DISABLE_BOOTANIMATION_PREF = "pref_disable_bootanimation";
 
     private static final String DISABLE_BOOTANIMATION_PERSIST_PROP = "persist.sys.nobootanimation";
 
     private static final String DISABLE_BOOTANIMATION_DEFAULT = "0";
 
     private static final String ENABLE_BOOTSOUND_PREF = "pref_enable_bootsound";
 
     private static final String ENABLE_BOOTSOUND_PERSIST_PROP = "persist.sys.nobootsound";
 
     private static final int ENABLE_BOOTSOUND_DEFAULT = 1;
 
     private static final String BOOTVOLSOUND_PREF = "pref_volume_bootsound";
 
     private static final String BOOTVOLSOUND_PROP = "persist.sys.boot_volume";
 
     private static final String BOOTVOLSOUND_DEFAULT = "0.2";
 
     public static final String SDCARD_RUN_FILE = "/sys/devices/virtual/bdi/179:0/read_ahead_kb";
 
     public static final String SDCARD_PREF = "pref_sdcard";
 
     private static final String SDCARD_PROP = "sdcardread";
 
     public static final String SDCARD_PREF_DEFAULT = "2048";
 
     private static final String BATTERY_PREF = "pref_battery";
 
     private static final String BATTERY_PROP = "battery.polling";
 
     private static final String BATTERY_PERSIST_PROP = "persist.sys.battery.polling";
 
     private static final String BATTERY_DEFAULT = "1";
 
     private static final String ANIMATE_SHUTDOWN_PREF = "pref_animate_on_shutdown";
 
     private CheckBoxPreference mJitPref;
 
     private ListPreference mUseDitheringPref;
 
     private CheckBoxPreference mUse16bppAlphaPref;
 
     private ListPreference mScrollingCachePref;
 
     private ListPreference mSdReadAheadPref;
 
     private Preference mSquadzone;
 
     private CheckBoxPreference mDisableBootanimPref;
 
     private CheckBoxPreference mEnableBootSoundPref;
 
     private CheckBoxPreference mAnimateOnShutdownPref;
 
     private ListPreference mHeapsizePref;
 
     private ListPreference mVolbootPref;
 
     private ListPreference mBatteryPref;
 
     private AlertDialog alertDialog;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setTitle(R.string.performance_settings_title_subhead);
         addPreferencesFromResource(R.xml.performance_settings);
 
         PreferenceScreen prefSet = getPreferenceScreen();
 
         mSquadzone = (Preference) prefSet.findPreference(PREF_SQUADZONE);
         mSquadzone.setSummary("CyanMobile");
 
         String temp;
 
         PreferenceCategory generalCategory = (PreferenceCategory)prefSet.findPreference(GENERAL_CATEGORY);
 
         mJitPref = (CheckBoxPreference) prefSet.findPreference(JIT_PREF);
         String jitMode = SystemProperties.get(JIT_PERSIST_PROP,
                 SystemProperties.get(JIT_PROP, JIT_ENABLED));
         mJitPref.setChecked(JIT_ENABLED.equals(jitMode));
 
         String useDithering = SystemProperties.get(USE_DITHERING_PERSIST_PROP, USE_DITHERING_DEFAULT);
         mUseDitheringPref = (ListPreference) prefSet.findPreference(USE_DITHERING_PREF);
         mUseDitheringPref.setOnPreferenceChangeListener(this);
         mUseDitheringPref.setValue(useDithering);
 
         mUse16bppAlphaPref = (CheckBoxPreference) prefSet.findPreference(USE_16BPP_ALPHA_PREF);
         String use16bppAlpha = SystemProperties.get(USE_16BPP_ALPHA_PROP, "0");
         mUse16bppAlphaPref.setChecked("1".equals(use16bppAlpha));
 
         mScrollingCachePref = (ListPreference) prefSet.findPreference(SCROLLINGCACHE_PREF);
         mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                 SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
         mScrollingCachePref.setOnPreferenceChangeListener(this);
 
         mHeapsizePref = (ListPreference) prefSet.findPreference(HEAPSIZE_PREF);
         mHeapsizePref.setValue(SystemProperties.get(HEAPSIZE_PERSIST_PROP,
                 SystemProperties.get(HEAPSIZE_PROP, HEAPSIZE_DEFAULT)));
         mHeapsizePref.setOnPreferenceChangeListener(this);
 
         mAnimateOnShutdownPref = (CheckBoxPreference) prefSet.findPreference(ANIMATE_SHUTDOWN_PREF);
         mAnimateOnShutdownPref.setChecked(Settings.System.getInt(getContentResolver(),
                 Settings.System.ANIMATION_ON_SHUTDOWN, 0) == 1);
 
         mDisableBootanimPref = (CheckBoxPreference) prefSet.findPreference(DISABLE_BOOTANIMATION_PREF);
         String disableBootanimation = SystemProperties.get(DISABLE_BOOTANIMATION_PERSIST_PROP, DISABLE_BOOTANIMATION_DEFAULT);
         mDisableBootanimPref.setChecked("1".equals(disableBootanimation));
 
         mEnableBootSoundPref = (CheckBoxPreference) prefSet.findPreference(ENABLE_BOOTSOUND_PREF);
         mEnableBootSoundPref.setChecked(Settings.System.getInt(getContentResolver(),
                     Settings.System.ENABLE_BOOTSOUND, ENABLE_BOOTSOUND_DEFAULT) == 1);
         mEnableBootSoundPref.setEnabled("0".equals(disableBootanimation));
 
         mVolbootPref = (ListPreference) prefSet.findPreference(BOOTVOLSOUND_PREF);
         if (SystemProperties.get(BOOTVOLSOUND_PROP) == "1")
              SystemProperties.set(BOOTVOLSOUND_PROP, BOOTVOLSOUND_DEFAULT);
         mVolbootPref.setValue(SystemProperties.get(BOOTVOLSOUND_PROP, BOOTVOLSOUND_DEFAULT));
         mVolbootPref.setOnPreferenceChangeListener(this);
         mVolbootPref.setEnabled("0".equals(disableBootanimation));
 
         temp = CPUActivity.readOneLine(SDCARD_RUN_FILE);
 
         mSdReadAheadPref = (ListPreference) prefSet.findPreference(SDCARD_PREF);
         mSdReadAheadPref.setValue(temp);
         mSdReadAheadPref.setOnPreferenceChangeListener(this);
 
         if (temp == null) {
             generalCategory.removePreference(mSdReadAheadPref);
         }
 
         mBatteryPref = (ListPreference) prefSet.findPreference(BATTERY_PREF);
         mBatteryPref.setValue(SystemProperties.get(BATTERY_PERSIST_PROP,
                 SystemProperties.get(BATTERY_PROP, BATTERY_DEFAULT)));
         mBatteryPref.setOnPreferenceChangeListener(this);
 
         // Set up the warning
         alertDialog = new AlertDialog.Builder(this).create();
         alertDialog.setTitle("CyanMobile WARNING: Dragons Ahead!");
         alertDialog.setMessage(getResources().getString(R.string.performance_settings_warning));
         alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                 getResources().getString(com.android.internal.R.string.ok),
                 new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 return;
             }
         });
 
         alertDialog.show();
     }
 
     @Override
     public void onResume() {
         String temp;
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 
         super.onResume();
 
         temp = prefs.getString(SDCARD_PREF, null);
 
         if (temp == null) {
             temp = CPUActivity.readOneLine(SDCARD_RUN_FILE);
             mSdReadAheadPref.setValue(temp);
         }
     }
 
     @Override
     public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
 
         if (preference == mJitPref) {
             SystemProperties.set(JIT_PERSIST_PROP,
                     mJitPref.isChecked() ? JIT_ENABLED : JIT_DISABLED);
             return true;
         }
 
         if (preference == mUse16bppAlphaPref) {
             SystemProperties.set(USE_16BPP_ALPHA_PROP,
                     mUse16bppAlphaPref.isChecked() ? "1" : "0");
             return true;
         }
 
         if (preference == mAnimateOnShutdownPref) {
             boolean value = mAnimateOnShutdownPref.isChecked();
             Settings.System.putInt(getContentResolver(), Settings.System.ANIMATION_ON_SHUTDOWN,
                     value ? 1 : 0);
             return true;
         }
 
         if (preference == mDisableBootanimPref) {
             boolean value = mDisableBootanimPref.isChecked();
             SystemProperties.set(DISABLE_BOOTANIMATION_PERSIST_PROP,
                           value ? "1" : "0");
             if (value) {
                Settings.System.putInt(getContentResolver(),
                     Settings.System.ENABLE_BOOTSOUND, 0);
                mEnableBootSoundPref.setEnabled(false);
                mVolbootPref.setEnabled(false);
             } else {
                mEnableBootSoundPref.setEnabled(true);
                mVolbootPref.setEnabled(true);
             }
             return true;
         }
 
         if (preference == mEnableBootSoundPref) {
             SystemProperties.set(ENABLE_BOOTSOUND_PERSIST_PROP,
                     mEnableBootSoundPref.isChecked() ? "1" : "0");
             Settings.System.putInt(getContentResolver(),
                     Settings.System.ENABLE_BOOTSOUND, mEnableBootSoundPref.isChecked() ? 1 : 0);
             return true;
         }
 
         return false;
     }
 
     public boolean onPreferenceChange(Preference preference, Object newValue) {
         if (preference == mScrollingCachePref) {
             if (newValue != null) {
                 SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String)newValue);
                 return true;
             }
         }
 
         if (preference == mUseDitheringPref) {
             if (newValue != null) {
                 SystemProperties.set(USE_DITHERING_PERSIST_PROP, (String) newValue);
                 return true;
             }
         }
 
         if (preference == mHeapsizePref) {
             if (newValue != null) {
                 SystemProperties.set(HEAPSIZE_PERSIST_PROP, (String)newValue);
                 return true;
             }
         }
 
         if (preference == mVolbootPref) {
             if (newValue != null) {
                 SystemProperties.set(BOOTVOLSOUND_PROP, (String)newValue);
                 return true;
             }
         }
 
         if (preference == mSdReadAheadPref) {
             if (newValue != null) {
                 SystemProperties.set(SDCARD_PROP, (String)newValue);
                 CPUActivity.writeOneLine(SDCARD_RUN_FILE, (String)newValue);
                 return true;
             }
         }
 
         if (preference == mBatteryPref) {
             if (newValue != null) {
                 SystemProperties.set(BATTERY_PERSIST_PROP, (String)newValue);
                 return true;
             }
         }
 
         return false;
     }
 }
