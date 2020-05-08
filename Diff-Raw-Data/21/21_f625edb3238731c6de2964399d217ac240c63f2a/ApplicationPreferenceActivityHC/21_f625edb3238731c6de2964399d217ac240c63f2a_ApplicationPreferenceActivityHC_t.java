 package com.anderspersson.xbmcwidget.configuration;
 
 import java.util.List;
 
 import com.anderspersson.xbmcwidget.R;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 public class ApplicationPreferenceActivityHC extends PreferenceActivity {
 	
	private PreferenceChangedListener preferenceChangedListener;

	public ApplicationPreferenceActivityHC() {
		preferenceChangedListener = new PreferenceChangedListener(this);
	}
	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(preferenceChangedListener);
 	}
 	
 	@Override
 	public void onBuildHeaders(List<Header> target) {	
 		loadHeadersFromResource(R.layout.preference_headers_hc, target);
 	}
 }
