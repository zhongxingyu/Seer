 package com.asa.easysal.ui;
 
 import android.os.Bundle;
 import android.preference.PreferenceFragment;
 
 import com.asa.easysal.R;
 import com.asa.easysal.SettingsUtil;
 
 public class PreferencesFragment extends PreferenceFragment {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		addPreferencesFromResource(R.layout.preferences);
 
 		SettingsUtil.launchAbout(getActivity(),
 				findPreference(SettingsUtil.PREFERENCES_ABOUT));
 		SettingsUtil.launchHome(getActivity(),
 				findPreference(SettingsUtil.PREFERENCES_HOMEPAGE));
 	}
 }
