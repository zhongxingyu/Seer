 package com.android.settings;
 
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceScreen;
 
 import java.util.List;
 
 public class GummySettingsNS extends SettingsPreferenceFragment {
 
    private static final String GENERAL_SETTINGS = "general_settings";
     private static final String SYSTEMUI_TWEAKS = "systemui_tweaks";
     private static final String LOCKSCREEN_SETTINGS = "lockscreen_settings";
     private static final String LED_SETTINGS = "led_settings";
     private static final String PERFORMANCE_SETTINGS = "performance_settings";
 
    PreferenceScreen mGeneralSettings;
     PreferenceScreen mSystemUITweaks;
     PreferenceScreen mLockscreenSettings;
     PreferenceScreen mLEDSettings;
     PreferenceScreen mPerformanceSettings;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.gummy_settings_ns);
 
        mGeneralSettings = (PreferenceScreen) findPreference(GENERAL_SETTINGS);
         mSystemUITweaks = (PreferenceScreen) findPreference(SYSTEMUI_TWEAKS);
         mLockscreenSettings = (PreferenceScreen) findPreference(LOCKSCREEN_SETTINGS);
         mLEDSettings = (PreferenceScreen) findPreference(LED_SETTINGS);
         mPerformanceSettings = (PreferenceScreen) findPreference(PERFORMANCE_SETTINGS);
     }
 }
