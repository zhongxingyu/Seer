 package sk.uniza.fri.activities;
 
 import sk.uniza.fri.R;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 
 public class SettingsPreferenceActivity extends PreferenceActivity {
 
 	public static final String PREFERENCE_RECEIVER = "broadcastreceiver_enabled";
 	public static final String PREFERENCE_UPDATE = "updates_interval";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences);
 	}
 }
