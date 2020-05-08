 package org.wahtod.wififixer.utility;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.net.wifi.ScanResult;
 import android.os.Bundle;
 
 public class WFScanResult {
 	public String SSID;
 	public String BSSID;
 	public String capabilities;
 	public int level;
 	public int frequency;
 
	public static String SSID_BUNDLE_KEY = "SSID";
	public static String BSSID_BUNDLE_KEY = "BSSID";
	public static String CAPABILITIES_BUNDLE_KEY = "CAPABILITIES";
	public static String FREQUENCY_BUNDLE_KEY = "FREQUENCY";
	public static String LEVEL_BUNDLE_KEY = "LEVEL";
	public static String BUNDLE_KEY = "WFSCANRESULT";
 
 	@Override
 	public int hashCode() {
 		int hash = 1;
 		hash = hash * 31 + (BSSID == null ? 0 : BSSID.hashCode());
 		hash = hash * 31 + (SSID == null ? 0 : SSID.hashCode());
 		hash = hash * 31 + (capabilities == null ? 0 : capabilities.hashCode());
 		return hash;
 
 	}
 
 	@Override
 	public boolean equals(Object other) {
 		if (other == null)
 			return false;
 		if (this.getClass() != other.getClass())
 			return false;
 		WFScanResult o = (WFScanResult) other;
 		if (o.BSSID.equals(this.BSSID))
 			return true;
 		else
 			return false;
 
 	}
 
 	public WFScanResult() {
 		SSID = "";
 		capabilities = "";
 		BSSID = "";
 	}
 
 	public static WFScanResult fromBundle(final Bundle bundle) {
 		WFScanResult out = new WFScanResult();
 		if (bundle != null && bundle.containsKey(BUNDLE_KEY)) {
 			Bundle fields = bundle.getBundle(BUNDLE_KEY);
 			out.SSID = fields.getString(SSID_BUNDLE_KEY);
 			out.BSSID = fields.getString(BSSID_BUNDLE_KEY);
 			out.capabilities = fields.getString(CAPABILITIES_BUNDLE_KEY);
 			out.level = fields.getInt(LEVEL_BUNDLE_KEY);
 			out.frequency = fields.getInt(FREQUENCY_BUNDLE_KEY);
 		}
 		return out;
 	}
 
 	public Bundle toBundle() {
 		Bundle bundle = new Bundle();
 		bundle.putString(SSID_BUNDLE_KEY, SSID);
 		bundle.putString(BSSID_BUNDLE_KEY, BSSID);
 		bundle.putString(CAPABILITIES_BUNDLE_KEY, capabilities);
 		bundle.putInt(LEVEL_BUNDLE_KEY, level);
 		bundle.putInt(FREQUENCY_BUNDLE_KEY, frequency);
 		return bundle;
 	}
 
 	public WFScanResult(final ScanResult result) {
 		if (result.SSID == null)
 			SSID = "";
 		else
 			SSID = result.SSID;
 		if (result.capabilities == null)
 			capabilities = "";
 		else
 			capabilities = result.capabilities;
 
 		if (result.BSSID == null)
 			BSSID = "";
 		else
 			BSSID = result.BSSID;
 
 		level = result.level;
 		frequency = result.frequency;
 	}
 
 	public static List<WFScanResult> fromScanResultArray(
 			final List<ScanResult> results) {
 		if (results == null)
 			return null;
 
 		List<WFScanResult> out = new ArrayList<WFScanResult>();
 		for (ScanResult result : results) {
 			if (result == null) {
 				out.add(new WFScanResult());
 			} else
 				out.add(new WFScanResult(result));
 		}
 		return out;
 	}
 }
