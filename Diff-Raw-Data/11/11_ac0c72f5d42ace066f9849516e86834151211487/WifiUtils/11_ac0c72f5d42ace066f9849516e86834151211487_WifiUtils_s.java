 package com.spydiko.socialwifi;
 
 import android.net.ConnectivityManager;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 /**
  * Created by jim on 28/10/2013.
  */
 public class WifiUtils {
 
	private static final String TAG = "WifiUtils";
 	public static final int SECURITY_NONE = 0;
 	public static final int SECURITY_WEP = 1;
 	public static final int SECURITY_PSK = 2;
 	public static final int SECURITY_EAP = 3;
 	private static String ITEM_KEY = "key", BSSID_KEY = "bssid", EXISTS_KEY = "exist", EXTRAS_KEY = "extra", IMAGE_KEY = "image", SIGNAL_KEY = "signal", ENTERPRISE_CONFIG_KEY = "enterprise";
 

 	/**
 	 * Create a configuration file for the specifies WiFi.
 	 *
 	 * @param networkSSID      The SSID of the network
 	 * @param networkPass      The password of the network
 	 * @param typeOfEncryption The encryption of the network
 	 * @return The desired configuration in order to connect to the wifi
 	 */
 	public static WifiConfiguration createConfigFile(String networkSSID, String networkPass, String typeOfEncryption) {
 
 		WifiConfiguration wifiConfiguration = new WifiConfiguration();
 
 		wifiConfiguration.SSID = "\"" + networkSSID + "\""; // Please note the quotes. String should contain ssid in quotes
 		// Case of WPA
 
 		switch (Integer.valueOf(typeOfEncryption)) {
 			case SECURITY_NONE:
 				Log.d(TAG, "SECURITY_NONE");
 				wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
 				break;
 			case SECURITY_WEP:
 				Log.d(TAG, "SECURITY_WEP");
 				wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
 				wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
 				wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
 				if (networkPass.length() != 0) {
 					int length = networkPass.length();
 					//                                        String password = networkPass.getText().toString();
 					// WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
 					if ((length == 10 || length == 26 || length == 58) &&
 							networkPass.matches("[0-9A-Fa-f]*")) {
 						wifiConfiguration.wepKeys[0] = networkPass;
 					} else {
 						wifiConfiguration.wepKeys[0] = '"' + networkPass + '"';
 					}
 				}
 				break;
 			case SECURITY_PSK:
 				Log.d(TAG, "SECURITY_PSK");
 				wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
 				if (networkPass.length() != 0) {
 					//                                        String password = mPasswordView.getText().toString();
 					if (networkPass.matches("[0-9A-Fa-f]{64}")) {
 						wifiConfiguration.preSharedKey = networkPass;
 					} else {
 						wifiConfiguration.preSharedKey = '"' + networkPass + '"';
 					}
 				}
 				break;
 			case SECURITY_EAP:
 
 				wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
 				wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
 				//                                config.enterpriseConfig = new WifiEnterpriseConfig();
 				//                                WifiEnterpriseConfig enterpriseConfig = mAccessPoint.getConfig().enterpriseConfig;
 				//                                int eapMethod = enterpriseConfig.getEapMethod();
 				//                                int phase2Method = enterpriseConfig.getPhase2Method();
 				//                                config.enterpriseConfig.setEapMethod(eapMethod);
 				//                                switch (eapMethod) {
 				//                                        case WifiEnterpriseConfig.Eap.PEAP:
 				// PEAP supports limited phase2 values
 				// Map the index from the PHASE2_PEAP_ADAPTER to the one used
 				// by the API which has the full list of PEAP methods.
 				//                                                switch(phase2Method) {
 				//                                                        case WIFI_PEAP_PHASE2_NONE:
 				//                                                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
 				//                                                                break;
 				//                                                        case WIFI_PEAP_PHASE2_MSCHAPV2:
 				//                                                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
 				//                                                                break;
 				//                                                        case WIFI_PEAP_PHASE2_GTC:
 				//                                                                config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.GTC);
 				//                                                                break;
 				//                                                        default:
 				//                                                                Log.e(TAG, "Unknown phase2 method" + phase2Method);
 				//                                                                break;
 				//                                                }
 				//                                                break;
 				//                                        default:
 				// The default index from PHASE2_FULL_ADAPTER maps to the API
 				//                                                config.enterpriseConfig.setPhase2Method(phase2Method);
 				//                                                break;
 				//                                }
 
 				break;
 			default:
 				break;
 		}
 
 		wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
 
 		return wifiConfiguration;
 
 	}
 

 	/**
 	 * Try to connect to the specifies network.
 	 *
 	 * @param wifiConfiguration   The WiFi configuration for the specific network
 	 * @param wifiManager         The WiFi manager created in SocialWifi
 	 * @param connectivityManager The connectivity manager created in Social Wifi
 	 * @return True if connection was successful, otherwise false
 	 */
 	public static boolean connect(WifiConfiguration wifiConfiguration, WifiManager wifiManager, ConnectivityManager connectivityManager) {
 
 		boolean connected = false;
 		int inet = wifiManager.addNetwork(wifiConfiguration);
 		Log.d(TAG, "Added " + inet);
 		if (inet > 0) {
 			List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
 			for (WifiConfiguration i : list) {
 				if (i.SSID != null && i.SSID.equals(wifiConfiguration.SSID)) {
 					Log.d(TAG, "Done!");
 					wifiManager.disconnect();
 					boolean b = wifiManager.enableNetwork(inet, true);
 					Log.d(TAG, "Result " + b);
 					boolean c = wifiManager.reconnect();
 					Log.d(TAG, "Result " + c);
 					connected = b && c;
 					break;
 				}
 			}
 		}
 		return connected;
 	}
 
 	/**
 	 * Get all the networks available
 	 *
 	 * @param wifiManager The WiFi manager created in SocialWifi
 	 * @param wifies      An ArrayList of all stored wifies
 	 * @return An ArrayList<HashMap<String,String>> for all the WiFi that were available in last scan
 	 */
 	public static ArrayList<HashMap<String, String>> getScanResults(WifiManager wifiManager, ArrayList<WifiPass> wifies) {
 
 		//		Log.d(TAG,"ScanResult");
 		List<ScanResult> results = wifiManager.getScanResults();
 		ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();
 		int size = results.size();
 		Log.d(TAG, "ScanResult size: " + size);
 		boolean containsFlag;
 		for (WifiPass wifi : wifies) {
 			Log.d("WifiUtils/Scan", "Update: " + wifi.getSsid() + " " + wifi.getPassword() + " " + wifi.getBssid());
 
 		}
 		try {
 			size = size - 1;
 			while (size >= 0) {
 				containsFlag = false;
 				HashMap<String, String> item = new HashMap<String, String>();
 				item.put(ITEM_KEY, results.get(size).SSID);
 				Log.d(TAG, "level (db) of " + results.get(size).SSID + " :" + results.get(size).level);
 				item.put(SIGNAL_KEY, Integer.toString(results.get(size).level));
 				if (results.get(size).level > -60)
 					item.put(IMAGE_KEY, Integer.toString(R.drawable.wifi_full));
 				else if (results.get(size).level > -80)
 					item.put(IMAGE_KEY, Integer.toString(R.drawable.wifi_good));
 				else if (results.get(size).level > -90)
 					item.put(IMAGE_KEY, Integer.toString(R.drawable.wifi_weak));
 				else
 					item.put(IMAGE_KEY, Integer.toString(R.drawable.wifi_no_signal));
 				item.put(EXTRAS_KEY, String.valueOf(WifiUtils.getSecurity(results.get(size))));
 				item.put(BSSID_KEY, results.get(size).BSSID);
 				item.put(EXISTS_KEY, "n");
 				for (HashMap<String, String> anArrayList : arrayList) {
 					if (anArrayList.containsValue(item.get(ITEM_KEY).trim())) {
 						Log.d(TAG, "Same SSID: " + item.get(ITEM_KEY));
 						containsFlag = true;
 					}
 				}
 				size--;
 				if (containsFlag) continue;
 				arrayList.add(item);
 				for (WifiPass wifi : wifies) {
 					if (item.get(BSSID_KEY).contains(wifi.getBssid())) {
 						item.put(EXISTS_KEY, "y");
 						//todo Check ssid
 					}
 				}
 			}
 			Collections.sort(arrayList, new SortBySignalStrength());
 			Collections.sort(arrayList, new SortByExist());
 			Log.d(TAG, "arraylist: " + arrayList.size());
 			return arrayList;
 			// lv.setAdapter(simpleAdapter);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Get the security for a network
 	 *
 	 * @param result The ScanResult for the specific network
 	 * @return An integer, which shows the security status
 	 */
 	public static int getSecurity(ScanResult result) {
 		if (result.capabilities.contains("WEP")) {
 			return SECURITY_WEP;
 		} else if (result.capabilities.contains("PSK")) {
 			return SECURITY_PSK;
 		} else if (result.capabilities.contains("EAP")) {
 			return SECURITY_EAP;
 		}
 		return SECURITY_NONE;
 	}
 
 	public static void removeNetwork(String ssid, WifiManager wifiManager) {
 		List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
 		for (WifiConfiguration i : list) {
 			Log.d(TAG, i.SSID + " " + i.BSSID);
 			if (i.SSID != null && i.SSID.equals("\"" + ssid + "\"")) {
 				wifiManager.removeNetwork(i.networkId);
 			}
 		}
 	}
 
 
 }
