 package skaphy.intenthelper2;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceActivity;
 
 public class IntentHelperPreferencesActivity extends PreferenceActivity
 {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.addPreferencesFromResource(R.xml.preferences);
 		findPreference("howtouse").setOnPreferenceClickListener(new OnPreferenceClickListener(){
 			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.howtouse_url))));
 				return true;
 			}
 		});
 		findPreference("developedby").setOnPreferenceClickListener(new OnPreferenceClickListener(){
 			public boolean onPreferenceClick(Preference preference) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.developer_url))));
 				return true;
 			}
 		});
 		findPreference("onetap_intent").setOnPreferenceClickListener(new OnPreferenceClickListener(){
 			public boolean onPreferenceClick(Preference preference) {
 				startActivity(new Intent(getApplicationContext(), OnetapIntentPreference.class));
 				return true;
 			}
 		});
 	}
 }
