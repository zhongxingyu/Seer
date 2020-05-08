 package edu.mit.csail.sensors;
 
 import java.util.ArrayList;
 import java.util.LinkedHashSet;
 import java.util.List;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiManager;
 import edu.mit.csail.ada.Global;
 
 
 public class WiFi {
 	private static WifiManager wifiManager;
 	private static WifiReceiver wifiReceiver = new WifiReceiver();
 	private static List<ArrayList<ScanResult>> wifiList = new ArrayList<ArrayList<ScanResult>>(Global.LOOKBACK_NUM);
 	private static double aveDistance = Global.INVALID_FEATURE;
 	private static int listSize = 0;
 	private static boolean isRegistered = false;
 	private static boolean isWorking = false;
 	
 	
 	public static void init(){
 		wifiManager = (WifiManager)Global.context.getSystemService(Context.WIFI_SERVICE);
 	}
 	
 	public static void start(){
 		isWorking = true;
 		listSize = 0;
 		if (!isRegistered){
 			Global.context.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
 			isRegistered = true;
 		}
 	}
 	
 	public static void scan(){
 		wifiManager.startScan();
 	}
 	
 	public static boolean isWorking(){
 		return isWorking;
 	}
 	
 	public static void stop()
 	{
 		isWorking = false;
 		listSize = 0;
		
 		if (isRegistered){
 			Global.context.unregisterReceiver(wifiReceiver);
 			isRegistered = false;
 		}
 	}
 	
 	public static double getFeature(){
 		return aveDistance;
 	}
 	
 	static class WifiReceiver extends BroadcastReceiver {
 	  	 public void onReceive(Context c, Intent intent) {  		
 	  		ArrayList<ScanResult> curScanResult = (ArrayList<ScanResult>) wifiManager.getScanResults();
 	  		
 	  		if(listSize == Global.LOOKBACK_NUM){
 	  			wifiList.remove(0);
 	  			--listSize;
 	  		}
 	  		
 	  		/**System.out.println("wifi received + " + curScanResult.size());
 	  		for (int i = 0; i < curScanResult.size(); i++)
 	    	{
 	    		System.out.println(curScanResult.get(i).SSID + "," + curScanResult.get(i).BSSID + "," + curScanResult.get(i).level);
 	    	}**/
 	  		
 	  		wifiList.add(curScanResult);
 	  		++listSize;
 	  		if(listSize == Global.LOOKBACK_NUM){
 	  			updateDistance(listSize);
 	  		}
 		 }
 	}
 	
 	public static boolean isDensityHigh(){
 		
 		if (wifiList.size() > 0){
 			// we have some wifi scans
 			
 			double aveApNum = 0.0;
 			for(int i = 0; i < wifiList.size(); ++i){
 				aveApNum += wifiList.get(i).size();
 			}
 			aveApNum /= (wifiList.size() * 1.0);
 			if(aveApNum > 1){
 				// the density is high
 				return true;
 			}else{ // density is low
 				return false;
 			}
 		}
 		// not enough sample, assuming the density is high
 		return true;
 	}
 	private static double updateDistance(int scanNum){
 		
 		LinkedHashSet<String> aPDimHashSet = new LinkedHashSet<String>();
 		
		if (wifiList.size() > 0){			
 			for(int i = 0; i < wifiList.size(); ++i){
 				for (int j = 0; j < wifiList.get(i).size(); ++j){
 					aPDimHashSet.add(wifiList.get(i).get(j).BSSID);						
 				}				
 			}
 
 			int obAPNum = aPDimHashSet.size();
 			List<String> apDimlist = new ArrayList<String>(aPDimHashSet);
 			
 			double[][] scanVect = new double[scanNum][obAPNum];
 			for(int i = 0; i < scanNum; ++i){
 				List<ScanResult> scanList = wifiList.get(i);				
 				for(int j = 0; j < obAPNum; ++j){
 					String macAdd = apDimlist.get(j);
 					for(int k = 0; k < scanList.size(); ++k){					
 						if(scanList.get(k).BSSID.equals(macAdd)){
 							scanVect[i][j] = (double)scanList.get(k).level;
 							break;
 						}
 						scanVect[i][j] = 0.0;
 					}
 				}
 			}
 		
 			double[] distance = new double[scanNum - 1];
 			for(int i = 0; i < (scanNum - 1); ++i){
 				double f1f2, f12, f22;
 				f1f2 = f12 = f22 = 0.0;
 				
 				for(int j = 0; j < obAPNum; ++j){
 					f1f2 += scanVect[i][j] * scanVect[i+1][j];
 					f12 += scanVect[i][j] * scanVect[i][j];
 					f22 += scanVect[i+1][j] * scanVect[i+1][j];									
 				}
 				distance[i] = f1f2 / (f12 + f22 - f1f2);				
 			}
 			
 			double aveDist = 0.0;
 			for(int i = 0; i < (scanNum - 1); ++i){
 				aveDist += distance[i];
 			}
 			
 			aveDistance =  aveDist/ ((scanNum - 1) * 1.0);
 			return aveDistance;
 		}
 		
 		aveDistance =  Global.INVALID_FEATURE;
 		return aveDistance;
 	}
 	
 	
 }
