 /**
  * Copyright 2010 Eric Taix Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language governing permissions and limitations under the
  * License.
  */
 package com.bigpupdev.synodroid.preference;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.server.SynoServer;
 import com.bigpupdev.synodroid.server.SynoServerConnection;
 import com.bigpupdev.synodroid.data.DSMVersion;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.util.Log;
 
 /**
  * A facade pattern which implements utilities methods to read/write preferences
  * 
  * @author Eric Taix (eric.taix at gmail.com)
  */
 public class PreferenceFacade {
 
 	public static final String USEWIFI_SUFFIX = ".iswifi";
 	public static final String USEEXT_SUFFIX = ".isexternal";
 	public static final String REFRESHVALUE_SUFFIX = ".refreshvalue";
 	public static final String SHOWUPLOAD_SUFFIX = ".showupload";
 	public static final String REFRESHSTATE_SUFFIX = ".refreshstate";
 	public static final String PASSWORD_SUFFIX = ".password";
 	public static final String USER_SUFFIX = ".user";
 	public static final String DSM_SUFFIX = ".dsm";
 	public static final String PORT_SUFFIX = ".port";
 	public static final String HOST_SUFFIX = ".host";
 	public static final String PROTOCOL_SUFFIX = ".protocol";
 	public static final String NICKNAME_SUFFIX = ".nickname";
 
 	public static final String SSID_SUFFIX = ".ssid";
 	public static final String WLAN_RADICAL = ".wlan";
 
 	public static final String SERVER_PREFIX = "synodroid_server.";
 
 	/**
 	 * Load servers from the SharedPreference
 	 */
 	public static void processLoadingServers(SharedPreferences sharedPreferencesP, PreferenceProcessor processorP) {
 		processLoadingServers(sharedPreferencesP, processorP, null, null);
 	}
 	
 	public static void processLoadingServers(SharedPreferences sharedPreferencesP, PreferenceProcessor processorP, ListPreferenceWithValue defaultP, String alwaysAsk) {
 		List<String> srvIDs = new ArrayList<String>();
 		List<String> srvNicks = new ArrayList<String>();
 		
 		// Read all preferences
 		Map<String, ?> prefs = sharedPreferencesP.getAll();
 		for (String key : prefs.keySet()) {
 			// If the key starts with 'server.' and then an integer
 			if (key.startsWith(SERVER_PREFIX)) {
 				String idStr = key.substring(SERVER_PREFIX.length());
 				try {
 					int idx = idStr.indexOf(".");
 					if (idx != -1) {
 						// Get the sub key
 						String subKey = idStr.substring(idx);
 						// Create the server if only the subkey is 'nickname'
 						if (subKey != null && subKey.equals(NICKNAME_SUFFIX)) {
 							idStr = idStr.substring(0, idx);
 							int id = Integer.parseInt(idStr);
 							// Retreive server's informations
 							Properties props = new Properties();
 							props.setProperty(NICKNAME_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + NICKNAME_SUFFIX)));
 							props.setProperty(DSM_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + DSM_SUFFIX)));
 							props.setProperty(USER_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + USER_SUFFIX)));
 							props.setProperty(PASSWORD_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + PASSWORD_SUFFIX)));
 							loadConnectionProperties(false, props, prefs, id);
 							loadConnectionProperties(true, props, prefs, id);
 							// Process the current server's item
 							processorP.process(id, SERVER_PREFIX + id, props);
 							srvIDs.add(SERVER_PREFIX + id);
 							srvNicks.add(convert2String(prefs.get(SERVER_PREFIX + id + NICKNAME_SUFFIX)));
 						}
 					}
 				}
 				// Not a number: don't care
 				catch (NumberFormatException ex) {
 				}
 			}
 		}
 		
 		//Add to default list
 		if (defaultP != null && alwaysAsk != null){
 			String[] defLabels = new String[srvIDs.size()+1];
 			String[] defValues = new String[srvNicks.size()+1];
 			defLabels[0] = alwaysAsk;
 			defValues[0] = "0";
 			for (int iLoop = 0; iLoop < srvIDs.size(); iLoop++) {
 				defLabels[iLoop+1] = srvNicks.get(iLoop);
 				defValues[iLoop+1] = srvIDs.get(iLoop);
 			}
 			
 			defaultP.setEntries(defLabels);
 			defaultP.setEntryValues(defValues);
 		}
 	}
 
 	/**
 	 * Load connection properties
 	 * 
 	 * @param localP
 	 * @param props
 	 * @param prefs
 	 * @param id
 	 */
 	private static void loadConnectionProperties(boolean localP, Properties props, Map<String, ?> prefs, int id) {
 		String localRadical = "";
 		if (localP) {
 			localRadical += PreferenceFacade.WLAN_RADICAL;
 		}
 		props.setProperty(localRadical + PROTOCOL_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + localRadical + PROTOCOL_SUFFIX)));
 		props.setProperty(localRadical + HOST_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + localRadical + HOST_SUFFIX)));
 		props.setProperty(localRadical + PORT_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + localRadical + PORT_SUFFIX)));
 		props.setProperty(localRadical + REFRESHSTATE_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + localRadical + REFRESHSTATE_SUFFIX)));
 		props.setProperty(localRadical + REFRESHVALUE_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + localRadical + REFRESHVALUE_SUFFIX)));
 		props.setProperty(localRadical + SHOWUPLOAD_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + localRadical + SHOWUPLOAD_SUFFIX)));
 		// Don't try to load SSID if public mode
 		if (localP) {
 			props.setProperty(localRadical + SSID_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + localRadical + SSID_SUFFIX)));
 			props.setProperty(localRadical + USEWIFI_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + localRadical + USEWIFI_SUFFIX)));
 		} else {
 			props.setProperty(localRadical + USEEXT_SUFFIX, convert2String(prefs.get(SERVER_PREFIX + id + localRadical + USEEXT_SUFFIX)));
 		}
 	}
 
 	/**
 	 * Cast to a String instance but NEVER return null
 	 * 
 	 * @param objP
 	 * @return
 	 */
 	private static String convert2String(Object objP) {
 		if (objP != null) {
 			return "" + objP;
 		}
 		return "";
 	}
 
 	/**
 	 * Load all servers from the shared preference
 	 */
 	public static ArrayList<SynoServer> loadServers(Context contextP, final SharedPreferences sharedPreferencesP, boolean debug, final boolean autoDetect) {
 		
 		// Determine the current network access
 		WifiManager wifiMgr = (WifiManager) contextP.getSystemService(Context.WIFI_SERVICE);
 		boolean wifiOn = wifiMgr.isWifiEnabled();
 		final WifiInfo currentWifi = wifiMgr.getConnectionInfo();
 		final boolean wifiConnected = (wifiOn && currentWifi.getNetworkId() != -1);
 		final boolean DEBUG = debug;
 		
 		// Create the servers list
 		final ArrayList<SynoServer> result = new ArrayList<SynoServer>();
 		processLoadingServers(sharedPreferencesP, new PreferenceProcessor() {
 			public void process(int idP, String keyP, Properties propertiesP) {
 				try {
 
 					SynoServerConnection loc = SynoServerConnection.createFromProperties(true, propertiesP, DEBUG);
 					SynoServerConnection pub = SynoServerConnection.createFromProperties(false, propertiesP, DEBUG);
 
 					SynoServer server = new SynoServer(propertiesP.getProperty(PreferenceFacade.NICKNAME_SUFFIX), loc, pub, propertiesP.getProperty(PreferenceFacade.USER_SUFFIX), propertiesP.getProperty(PreferenceFacade.PASSWORD_SUFFIX), DEBUG, autoDetect, keyP);
 					// DSM version
 					DSMVersion vers = DSMVersion.titleOf(propertiesP.getProperty(PreferenceFacade.DSM_SUFFIX));
 					if (vers == null) {
 						vers = DSMVersion.VERSION2_2;
 					}
 					server.setDsmVersion(vers, true);
 					// Sort informations
 					String sortAttr = sharedPreferencesP.getString("sort", "task_id");
 					boolean asc = sharedPreferencesP.getBoolean("asc", true);
 					server.setSortAttribute(sortAttr);
 					server.setAscending(asc);
 					// If this server has a public connection
 					if (pub != null) {
 						result.add(server);
 					}
 					// Or if this server has a local connection AND the wifi is connected AND one SSID matchs
 					else {
 						if (loc != null && wifiConnected && loc != null && loc.wifiSSID != null && loc.wifiSSID.size() > 0) {
 							for (String ssid : loc.wifiSSID) {
								if (ssid.equals(currentWifi.getSSID())) {
 									result.add(server);
 								}
 							}
 						}
 					}
 				} catch (Exception ex) {
 					if (DEBUG) Log.e(Synodroid.DS_TAG, "An exception occured while loading servers from preference", ex);
 				}
 			}
 		});
 		// If at
 		return result;
 	}
 }
