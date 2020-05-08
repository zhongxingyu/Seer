 package com.coffeeandpower.location;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import com.coffeeandpower.cont.UserSmart;
 import com.coffeeandpower.maps.MyScanResult;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import android.util.Log;
 
 public class WifiScanBroadcastReceiver extends BroadcastReceiver{
 	private WifiManager wifiManager;
 	
 	public WifiScanBroadcastReceiver(Context context){
 	  	wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 
 		
 	}
 	
 	
 	public void registerForWifiScans(Context context){
 	  	    //Forcing the scan requires a permission and I don't think we need it because the scan happens
 	  	    //frequently enough
 	  	    //boolean scanStarted = wifiManager.startScan();
 	  	    context.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 	}
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		final String action = intent.getAction();
 		if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction()))
 		{			
 			//FIXME
 			//This is a fake test list for C&P
 			List<String> cp_bssids = Arrays.asList("98:fc:11:8f:8f:b0", "00:1c:b3:ff:8d:53", "f4:6d:04:6d:33:2e", "e0:91:f5:87:71:2b", "74:91:1a:50:eb:98","c4:3d:c7:8d:6b:f8");
 			ArrayList<MyScanResult> venueWifiNetworks = new ArrayList<MyScanResult>();
 			for(String currBssid:cp_bssids)
 			{
 				venueWifiNetworks.add(new MyScanResult(currBssid));
 			}
 			
 			//Grab results from fresh scan of wifi networks
 			List<ScanResult> visibleWifiNetworks = wifiManager.getScanResults();
 			this.collectWifiSignature(8, visibleWifiNetworks);
 			
 			//TODO
 			//Compare list of visible wifi networks, and see how that 
 			//compares to our list of key networks at that venue
 			boolean match = this.checkForMatch(visibleWifiNetworks, venueWifiNetworks);
 			if(match)
 			{
 				Log.d("WifiBroadcast","Positive WiFi signature match, we are at: C&P");
				//context.unregisterReceiver(this);
 			}
 			else
 			{
 				Log.d("WifiBroadcast","No match, we are not at C&P");
 			}
 
 
 		}
 	    }
 	
 	private List<MyScanResult> collectWifiSignature(int maxBssidsSig, List<ScanResult> visibleWifiNetworks){
         	Log.d("WifiScanBroadcast","Forming wifi signature");
 
 		ArrayList<MyScanResult> wifiSignature = new ArrayList<MyScanResult>();
 		//Object[] visibleWifiNetworksArray = visibleWifiNetworks.toArray();
 		
 		Collections.sort(visibleWifiNetworks, new Comparator<ScanResult>() {
 			@Override
 			public int compare(ScanResult m1, ScanResult m2) {
 				if (m1.level > m2.level) {
 					return -1;
 				}
 				return 1;
 			}
 		});
 		for(ScanResult currNet : visibleWifiNetworks)
 		{
                 	//Need to find if wifiSignature 
                 	MyScanResult myCurrNet = new MyScanResult(currNet);
                	boolean tmpBool = wifiSignature.contains(myCurrNet);
                 	if(wifiSignature.contains(myCurrNet)==false)
                 	{
                 		wifiSignature.add(myCurrNet);
                         	Log.d("WifiScanBroadcast","SSID: " + myCurrNet.SSID + " PowerLevel " + myCurrNet.level);
                 		if(wifiSignature.size()>=maxBssidsSig)
                 		{
                                 	Log.d("WifiScanBroadcast","MaxBssid reached. " + String.valueOf(maxBssidsSig) + " Bssid's in signature");
                 			return wifiSignature;
                 		}
                 	}
 		}		
 		return wifiSignature;
 		
 		
 	}
 	    
 	    private boolean checkForMatch(List<ScanResult> visibleWifiNetworks, List<MyScanResult> venueWifiNetworks) {
 		    //This is the match threshold, once hit we know we have a match
 		    int threshold = 4;
 		    int matches = 0;
 	                for(ScanResult currNet:visibleWifiNetworks)
 	                {
 	                	for(MyScanResult currVenueNet:venueWifiNetworks)
 	                	{
	                		String testArg1 = currNet.BSSID;
	                		String TestArg2 = currVenueNet.BSSID;
	                		//The below was returning false positives, why is not clear
	                		//if(currNet.BSSID.equalsIgnoreCase(currVenueNet.BSSID));
	                		if(testArg1.equalsIgnoreCase(TestArg2))
 	                		{
 	                			matches++;
 	                			if(matches>threshold)
 	                				return true;
 	                		}
 	                	}
 	                	//Log.d("WifiBroadcast","SSID: " + currNet.SSID + " BSSID: " + currNet.BSSID);
 	                }
 	                return false;
 		    
 		    
 	    }
 }
