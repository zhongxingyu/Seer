 package sk.uniza.fri.activities;
 
 import sk.uniza.fri.R;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 
 public class SettingsPreferenceActivity extends PreferenceActivity {
 
 	public static final String PREFERENCE_RECEIVER = "broadcastreceiver_enabled";
 	public static final String PREFERENCE_UPDATE = "updates_interval";
 	public static final String PREFERENCE_SERVER_URL = "server_url";
 	
	public static final String SERVER_URL = "http://95.105.179.251";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.preferences);
 	}
 }
