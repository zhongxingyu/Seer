 package uk.co.kalgan.app.seismic;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 
 public class UserPreferences extends PreferenceActivity {
 
 	SharedPreferences prefs;
 	
 	public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
 	public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
 	public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.userpreferences);
 	}
 	
 }
