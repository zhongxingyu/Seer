 package com.tldr;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.Preference.OnPreferenceClickListener;
 import android.preference.PreferenceFragment;
 import android.util.Log;
 
 import com.auth.AccountHelper;
 import com.datastore.UserInfoDatastore;
 import com.google.android.gms.maps.model.LatLng;
 import com.tldr.com.tldr.userinfoendpoint.model.UserInfo;
 import com.tldr.exlap.ConnectionHelper;
 
 import de.exlap.discovery.DiscoveryListener;
 import de.exlap.discovery.DiscoveryManager;
 import de.exlap.discovery.ServiceDescription;
 
 public class SettingsFragment extends PreferenceFragment implements
 		DiscoveryListener, OnSharedPreferenceChangeListener {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getPreferenceManager().getSharedPreferences()
 				.registerOnSharedPreferenceChangeListener(this);
 
 		// Load the preferences from an XML resource
 		addPreferencesFromResource(R.xml.preferences);
 
 		Preference connectPref = findPreference("pref_exlap_connect");
 		Preference resetDataPref = findPreference("pref_reset_userdata");
 		Preference fakeLocations = findPreference("pref_fake_location");
 		connectPref
 				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 					public boolean onPreferenceClick(Preference preference) {
 						if (!getPreferenceManager().getSharedPreferences()
 								.getBoolean(preference.getKey(), false)) {
 							try {
 								performDiscovery();
 							} catch (IOException e) {
 
 								e.printStackTrace();
 								return false;
 							}
 							Log.e("tldr-exlap", "started discovery");
 
 							return true;
 						} else {
 							GlobalData.getConnectionHelper().killClient();
 							Log.e("tldr-exlap", "killed client");
 							SharedPreferences prefs = getPreferenceManager()
 									.getSharedPreferences();
 							prefs.edit()
 									.putBoolean("pref_exlap_connect", false)
 									.commit();
 							return true;
 						}
 
 					}
 				});
 		resetDataPref
 				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 
 					@Override
 					public boolean onPreferenceClick(Preference preference) {
 						Log.e("tldr", "resetting user data...");
 						AccountHelper autha;
 						UserInfoDatastore datastorea;
 						autha = new AccountHelper(getActivity());
 						datastorea = new UserInfoDatastore(null, autha
 								.getCredential());
 						UserInfo currentUser = GlobalData.getCurrentUser();
 						currentUser.setFinishedGoals(new ArrayList<Long>());
 						 currentUser.setFinishedGoalsTS(new
 						 ArrayList<Long>());
 						currentUser.setAcceptedTasks(new ArrayList<Long>());
 						 currentUser.setAcceptedTasksTS(new
 						 ArrayList<Long>());
 						datastorea.updateUser(currentUser);
 						return false;
 					}
 
 				});
 		fakeLocations.setOnPreferenceClickListener(new OnPreferenceClickListener() {
 			
 			@Override
 			public boolean onPreferenceClick(Preference preference) {
 				// TODO Auto-generated method stub
 				List<LatLng> locations= new ArrayList<LatLng>();
 				locations.add(new LatLng(52.52286,13.321506));
 				locations.add(new LatLng(52.522828,13.322027));
 				locations.add(new LatLng(52.521196,13.322357));
 				locations.add(new LatLng(52.520921,13.32137));
 				locations.add(new LatLng(52.519681,13.319739));
 				locations.add(new LatLng(52.519113,13.320621));
 				locations.add(new LatLng(52.518467,13.321876));
 				locations.add(new LatLng(52.517514,13.325061));
 				locations.add(new LatLng(52.516293,13.327187));
 				
 				locations.add(new LatLng(52.515516,13.327936));
 				locations.add(new LatLng(52.515659,13.328558));
 				locations.add(new LatLng(52.516691,13.329395));
 				
 				
 				
 				GlobalData.setFake_location_data_enabled(true);
 				GlobalData.getFakeLocationProvider().setLocations(locations);
 				GlobalData.getFakeLocationProvider().reset();
 				GlobalData.getFakeLocationProvider().startFakeLocations();
 				
 				return false;
 			}
 		});
 
 	}
 
 	@Override
 	public void discoveryEvent(int eventType,
 			ServiceDescription serviceDescription) {
 		Log.e("tldr-exlap", "discoveryevent");
 		if (eventType == DiscoveryListener.SERVICE_NEW) {
 			Log.e("tldr-exlap", "connected event");
 			ConnectionHelper connectionHelper = GlobalData
 					.getConnectionHelper();
 			// Log.e("tldr-exlap", "connectionhelper: " + connectionHelper +
 			// connectionHelper.isConnected());
 			connectionHelper.startService(serviceDescription.getAddress());
 			SharedPreferences prefs = getPreferenceManager()
 					.getSharedPreferences();
 			Log.e("tldr-exlap",
 					"connect pref: "
 							+ prefs.getBoolean("pref_exlap_connect", false));
 			prefs.edit().putBoolean("pref_exlap_connect", true).commit();
 		} else if (eventType == DiscoveryListener.SERVICE_GONE) {
 			SharedPreferences prefs = getPreferenceManager()
 					.getSharedPreferences();
 			prefs.edit().putBoolean("pref_exlap_connect", false).commit();
 			Log.e("tldr-exlap", "exlap client disconnected");
 		}
 
 	}
 
 	@Override
 	public void discoveryFinished(boolean arg0) {
 		Log.e("tldr-exlap", "discovery finished");
 
 	}
 
 	public void performDiscovery() throws IOException {
 		if (GlobalData.getCurrentUser().getEmail()
 				.equals("doriankno@googlemail.com")) {
 			Log.e("tldr-exlap", "dorian workaround");
 			GlobalData.getConnectionHelper().startService(
					"socket://192.168.1.106:28500");
 			Preference connectionPref = findPreference("pref_exlap_connect");
 			connectionPref.setSummary("Dorian Special Connection Started");
 		} else {
 			DiscoveryManager disco = new DiscoveryManager(
 					DiscoveryManager.SCHEME_SOCKET);
 			try {
 				disco.discoverServices(this, null, true);
 				Preference connectionPref = findPreference("pref_exlap_connect");
 				connectionPref.setSummary("Searching for Exlap Proxy...");
 
 			} catch (Exception e) {
 				System.out.println("ERROR. Root cause: " + e.getMessage());
 			}
 		}
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		Log.e("tldr-exlap", "preferences changed: " + key);
 		// Set summary to be the user-description for the selected value
 		if (key.equals("pref_exlap_connect")) {
 			Preference connectionPref = findPreference("pref_exlap_connect");
 			if (sharedPreferences.getBoolean(key, false)) {
 				connectionPref.setTitle("Connected to Exlap Proxy");
 				connectionPref.setSummary("Tap again to disconnect");
 			} else {
 				connectionPref.setTitle("Connect to Exlap Proxy");
 				connectionPref.setSummary("Your are now disconnected");
 			}
 
 		}
 	}
 	
 	@Override
 	public void onResume () {
 		((HomeActivity) getActivity()).animateMenuIcons(3);
 		super.onResume();
 	}
 
 }
