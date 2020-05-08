 package jp.muo.smsproxy;
 
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 
 public class SmsPreferenceActivity extends PreferenceActivity {
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.prefs);
 	}
 }
