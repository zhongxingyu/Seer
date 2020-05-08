 /*Copyright [2010] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.wahtod.wififixer;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.PreferenceActivity;
 import android.preference.PreferenceManager;
 import android.provider.Settings.SettingNotFoundException;
 import android.util.Log;
 
 public class WifiFixerPreferences extends PreferenceActivity implements
 	OnSharedPreferenceChangeListener {
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
 	setWifiSleepPolicy(this);
 	addPreferencesFromResource(R.xml.preferences);
 
     }
 
     @Override
     protected void onResume() {
 	super.onResume();
 
 	// Set up a listener whenever a key changes
 	getPreferenceScreen().getSharedPreferences()
 		.registerOnSharedPreferenceChangeListener(this);
 	setWifiSleepPolicy(this);
     }
 
     @Override
     protected void onPause() {
 	super.onPause();
 
 	// Unregister the listener whenever a key changes
 	getPreferenceScreen().getSharedPreferences()
 		.unregisterOnSharedPreferenceChangeListener(this);
     }
 
     private static void setWifiSleepPolicy(final Context context) {
 	/*
 	 * Handle Wifi Sleep Policy
 	 */
 	SharedPreferences prefs = PreferenceManager
 		.getDefaultSharedPreferences(context);
 	ContentResolver cr = context.getContentResolver();
 	SharedPreferences.Editor edit = prefs.edit();
 	try {
 	    String wfsleep = String
 		    .valueOf(android.provider.Settings.System.getInt(cr,
 			    android.provider.Settings.System.WIFI_SLEEP_POLICY));
 	    edit.putString("WFSLEEP", wfsleep);
 	    edit.commit();
 	} catch (SettingNotFoundException e) {
 	    /*
 	     * Don't need a catch, all clients are >= 1.5 per manifest market
 	     * restriction
 	     */
 	}
     }
 
     public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 
 	SharedPreferences.Editor edit = prefs.edit();
 
 	if (key.contains("Performance")) {
 
 	    String sPerf = prefs.getString("Performance", "2");
 	    int pVal = Integer.parseInt(sPerf);
 
 	    switch (pVal) {
 	    case 1:
 		edit.putBoolean("WiFiLock", true);
 		edit.putBoolean("SCREEN", true);
 		if (!edit.commit())
 		    Log.i("Preferences", "Commit failed");
 		break;
 
 	    case 2:
 		edit.putBoolean("WiFiLock", true);
 		edit.putBoolean("SCREEN", false);
 		if (!edit.commit())
 		    Log.i("Preferences", "Commit failed");
 		break;
 
 	    case 3:
 		edit.putBoolean("WiFiLock", false);
 		edit.putBoolean("SCREEN", false);
 		if (!edit.commit())
 		    Log.i("Preferences", "Commit failed");
 		break;
 	    }
 	    WifiFixerPreferences.this.finish();
 
 	} else if (key.contains("Disable")) {
 	    stopService(new Intent(this, WifiFixerService.class));
 	    edit.putBoolean("SLOG", false);
 	    edit.commit();
 	} else if (key.contains("WFSLEEP")) {
 	    /*
 	     * Setting Wifi Sleep Policy
 	     */
 	    ContentResolver cr = getContentResolver();
 	    String wfsleep = prefs.getString("WFSLEEP", "3");
 	    if (wfsleep != "3") {
 
 		android.provider.Settings.System.putInt(cr,
 			android.provider.Settings.System.WIFI_SLEEP_POLICY,
 			Integer.valueOf(wfsleep));
 	    } else {
 		/*
 		 * Set to system state
 		 */
 		try {
 		    wfsleep = String
 			    .valueOf(android.provider.Settings.System
 				    .getInt(
 					    cr,
 					    android.provider.Settings.System.WIFI_SLEEP_POLICY));
 		    edit.putString("WFSLEEP", wfsleep);
 		    edit.commit();
 		} catch (SettingNotFoundException e) {
 		    /*
 		     * Should always be found since our clients are > SDK2
 		     */
 		}
 	    }
 	}
 
 	// Send reload intent to WifiFixerService when a preference value
 	// changes
 	Intent sendIntent = new Intent(WifiFixerService.class.getName());
 	startService(sendIntent);
     }
 }
