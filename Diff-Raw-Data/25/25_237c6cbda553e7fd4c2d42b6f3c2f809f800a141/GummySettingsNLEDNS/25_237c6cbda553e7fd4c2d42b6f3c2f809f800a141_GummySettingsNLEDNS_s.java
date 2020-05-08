 package com.android.settings;
 
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 
 import java.util.List;
 
 public class GummySettingsNLEDNS extends SettingsPreferenceFragment {
 
     private static final String SYSTEMUI_TWEAKS = "systemui_tweaks";
     private static final String LOCKSCREEN_SETTINGS = "lockscreen_settings";
     private static final String PERFORMANCE_SETTINGS = "performance_settings";
 
     PreferenceScreen mSystemUITweaks;
     PreferenceScreen mLockscreenSettings;
     PreferenceScreen mLEDSettings;
     PreferenceScreen mPerformanceSettings;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.gummy_settings_nledns);
 
         mSystemUITweaks = (PreferenceScreen) findPreference(SYSTEMUI_TWEAKS);
         mLockscreenSettings = (PreferenceScreen) findPreference(LOCKSCREEN_SETTINGS);
         mPerformanceSettings = (PreferenceScreen) findPreference(PERFORMANCE_SETTINGS);
     }
 }
