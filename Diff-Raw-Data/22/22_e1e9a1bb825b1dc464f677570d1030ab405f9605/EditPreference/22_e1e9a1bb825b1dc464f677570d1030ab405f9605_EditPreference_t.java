 package csci498.bybeet.lunchlist;
 
 import android.content.ComponentName;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 
 public class EditPreference extends PreferenceActivity {
 
 	private SharedPreferences prefs;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		addPreferencesFromResource(R.xml.preferences);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		prefs.registerOnSharedPreferenceChangeListener(onChange);
 	}
 
 	@Override
 	public void onPause() {
 		prefs.unregisterOnSharedPreferenceChangeListener(onChange);
 
 		super.onPause();
 	}
 
 	SharedPreferences.OnSharedPreferenceChangeListener onChange =
 			new SharedPreferences.OnSharedPreferenceChangeListener() {
 
 		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 				String key) {
 			if("alarm".equals(key)) {
 				boolean enabled = prefs.getBoolean(key, false);
 				int flag = (enabled ?
 						PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
 							PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
 				ComponentName component = new ComponentName(EditPreference.this, OnBootReceiver.class);
 
 				getPackageManager().setComponentEnabledSetting(component, flag, PackageManager.DONT_KILL_APP);
 
 				if(enabled) {
 					OnBootReceiver.setAlarm(EditPreference.this);
 				}
 				else {
 					OnBootReceiver.cancelAlarm(EditPreference.this);
 				}
 			}
 			else if("alarm_time".equals(key)) {
 				OnBootReceiver.cancelAlarm(EditPreference.this);
				OnBootReceiver.setAlarm(EditPreference.this);
 			}
 
 		}
 	};
 }
