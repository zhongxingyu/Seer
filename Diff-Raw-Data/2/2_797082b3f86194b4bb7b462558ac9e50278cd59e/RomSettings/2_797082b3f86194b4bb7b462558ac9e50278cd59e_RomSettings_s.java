 package com.android.settings.pcf;
 
 import android.content.Context;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.RemoteException;
 import android.preference.ListPreference;
 import android.preference.Preference;
 import android.preference.PreferenceScreen;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.View.OnClickListener;
 
 import com.android.settings.R;
 import com.android.settings.SettingsPreferenceFragment;
 import com.android.settings.Utils;
 import com.android.settings.util.Helpers;
 
 public class RomSettings extends SettingsPreferenceFragment {
 
    private static final String TAG = "ROM Settings";
     private static final String KEY_STATUS_BAR = "status_bar";
     // private static final String KEY_NAVIGATION_BAR = "navigation_bar";
     // private static final String KEY_GENERAL_UI = "general_ui";
 
     private PreferenceScreen mStatusBar;
     // private PreferenceScreen mNavigationBar;
     // private PreferenceScreen mGeneralUi;
 
     private final Configuration mCurConfig = new Configuration();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         addPreferencesFromResource(R.xml.extras_settings);
 
         mStatusBar = (PreferenceScreen) findPreference(KEY_STATUS_BAR);
 	// mNavigationBar = (PreferenceScreen) findPreference(KEY_NAVIGATION_BAR);
         // mGeneralUi = (PreferenceScreen) findPreference(KEY_GENERAL_UI);
 
     }
 }
