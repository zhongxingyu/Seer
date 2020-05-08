 package fr.univsavoie.src.s4.ltp;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 public class LtpClientPreferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
 
 	private SharedPreferences preferences;
 	/* (non-Javadoc)
 	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		addPreferencesFromResource(R.xml.prefs);
 		preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		preferences.registerOnSharedPreferenceChangeListener(this);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.preference.PreferenceActivity#onDestroy()
 	 */
 	@Override
 	protected void onDestroy() {
 		preferences.unregisterOnSharedPreferenceChangeListener(this);
 		super.onDestroy();
 	}
 
	@Override
 	public void onSharedPreferenceChanged(SharedPreferences pref, String clef) {
 		if ("ltp_service".equals(clef)){
 			Intent service = new Intent(this, LtpTrackerService.class);
 			if (pref.getBoolean(clef, false)){
 				this.startService(service);
 			} else {
 				this.stopService(service);
 			}
 		}
 	}
 	
 
 }
