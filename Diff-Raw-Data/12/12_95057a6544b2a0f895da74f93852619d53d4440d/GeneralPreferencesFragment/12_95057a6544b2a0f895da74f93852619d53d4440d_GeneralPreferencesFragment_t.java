 package com.suicune.notasupna;
 
 import android.annotation.TargetApi;
 import android.content.SharedPreferences;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceFragment;
 import android.preference.PreferenceManager;
 
 @TargetApi(Build.VERSION_CODES.HONEYCOMB)
 public class GeneralPreferencesFragment extends PreferenceFragment implements OnPreferenceChangeListener{
 	
 	private SharedPreferences prefs;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences_activity);
 		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 
 		Preference languagePreference = findPreference(getString(R.string.preference_app_language));
 		Preference sortOrderPreference = findPreference(getString(R.string.preference_sort_order));
 		
 		languagePreference.setOnPreferenceChangeListener(this);
 		sortOrderPreference.setOnPreferenceChangeListener(this);
 		
 		changeSummary(languagePreference, prefs.getString(getString(R.string.preference_app_language), getString(R.string.language_code_spanish)));
 		changeSummary(sortOrderPreference, prefs.getString(getString(R.string.preference_sort_order), getString(R.string.sort_order_time_desc_value)));
 		super.onActivityCreated(savedInstanceState);
 	}
 
 	@Override
 	public boolean onPreferenceChange(Preference preference, Object newValue) {
 		changeSummary(preference, newValue);
 		//Language changed
 		if (preference.getKey().equals(
 				getString(R.string.preference_app_language))) {
 			String newLanguage = (String) newValue;
 			
			if(newLanguage.equalsIgnoreCase(getString(R.string.language_code_basque))){
				prefs.edit().putString(getString(R.string.preference_record_language), newLanguage).commit();
			} else {
				prefs.edit().putString(getString(R.string.preference_record_language), getString(R.string.language_code_spanish)).commit();
			}
 			
 			//Sort order changed
 		} else if (preference.getKey().equals(
 				getString(R.string.preference_sort_order))) {
 			String newOrder = (String) newValue;
 			
 		}
 		return true;
 	}
 	
 	private void changeSummary(Preference preference, Object newValue){
 		if (preference.getKey().equals(
 				getString(R.string.preference_app_language))) {
 			String newLanguage = (String) newValue;
 			if(newLanguage.equals(getString(R.string.language_code_basque))) {
 				preference.setSummary(R.string.basque);
 			} else if (newLanguage.equals(getString(R.string.language_code_default))) {
 				preference.setSummary(R.string.default_language);
 			} else if (newLanguage.equals(getString(R.string.language_code_english))) {
 				preference.setSummary(R.string.english);
 			} else if (newLanguage.equals(getString(R.string.language_code_spanish))) {
 				preference.setSummary(R.string.spanish);
 			}
 			
 			//Sort order changed
 		} else if (preference.getKey().equals(
 				getString(R.string.preference_sort_order))) {
 			String newOrder = (String) newValue;
 			if(newOrder.equals(getString(R.string.sort_order_alpha_asc_value))) {
 				preference.setSummary(R.string.sort_order_alpha_asc);
 			} else if (newOrder.equals(getString(R.string.sort_order_alpha_desc_value))) {
 				preference.setSummary(R.string.sort_order_alpha_desc);
 			} else if (newOrder.equals(getString(R.string.sort_order_time_asc_value))) {
 				preference.setSummary(R.string.sort_order_time_asc);
 			} else if (newOrder.equals(getString(R.string.sort_order_time_desc_value))) {
 				preference.setSummary(R.string.sort_order_time_desc);
 			}
 		}
 	}
 }
