 package com.easycard;
 
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 public class EasyCardPreferences extends PreferenceActivity {
 	private final EasyCardPreferenceChangeListener preferenceChangeListener = new EasyCardPreferenceChangeListener();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences);
 		
 		// Get the preference fields
 		Preference namePref = (Preference) findPreference(Constants.NAME);
 		Preference companyPref = (Preference) findPreference(Constants.COMPANY);
 		Preference emailPref = (Preference) findPreference(Constants.EMAIL);
 		Preference workPhonePref = (Preference) findPreference(Constants.WORKPHONE);
 		Preference personalPhonePref = (Preference) findPreference(Constants.PERSONALPHONE);
 		Preference websitePref = (Preference) findPreference(Constants.WEBSITE);
 		Preference picturePref = (Preference) findPreference(Constants.PICTURE);
 		
 		namePref.setOnPreferenceChangeListener(preferenceChangeListener);
 		companyPref.setOnPreferenceChangeListener(preferenceChangeListener);
 		emailPref.setOnPreferenceChangeListener(preferenceChangeListener);
 		workPhonePref.setOnPreferenceChangeListener(preferenceChangeListener);
 		personalPhonePref.setOnPreferenceChangeListener(preferenceChangeListener);
 		websitePref.setOnPreferenceChangeListener(preferenceChangeListener);
 		picturePref.setOnPreferenceChangeListener(preferenceChangeListener);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		// Get the preference fields
 		Preference namePref = (Preference) findPreference(Constants.NAME);
 		Preference companyPref = (Preference) findPreference(Constants.COMPANY);
 		Preference emailPref = (Preference) findPreference(Constants.EMAIL);
 		Preference workPhonePref = (Preference) findPreference(Constants.WORKPHONE);
 		Preference personalPhonePref = (Preference) findPreference(Constants.PERSONALPHONE);
 		Preference websitePref = (Preference) findPreference(Constants.WEBSITE);
 		Preference picturePref = (Preference) findPreference(Constants.PICTURE);
 		
 		namePref.setSummary(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(namePref.getKey(), ""));
 		companyPref.setSummary(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(companyPref.getKey(), ""));
 		emailPref.setSummary(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(emailPref.getKey(), ""));
 		workPhonePref.setSummary(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(workPhonePref.getKey(), ""));
 		personalPhonePref.setSummary(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(personalPhonePref.getKey(), ""));
		websitePref.setOnPreferenceChangeListener(preferenceChangeListener);
 		picturePref.setSummary(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(picturePref.getKey(), ""));
 	}
 }
