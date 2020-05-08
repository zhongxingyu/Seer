 package com.mobot;
  
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.NetworkInfo;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class APSelectionActivity extends ListActivity {
 
 	private APItemAdapter    mAdapter;
     private ProgressDialog   mProgressDialog;
 	private WifiManager      mMainWifi;
     private WifiReceiver     mWifiReceiver;
     private NetStatReceiver  mNetStatReceiver;
     private ArrayList<APItemView> mItemList;
     private Timer 			 mTimer;
     
         
    	public class APItemAdapter extends ArrayAdapter<APItemView> {
    		public APItemAdapter(Context context, int textViewResourceId, ArrayList<APItemView> items) {
             super(context, textViewResourceId, items);
         }    
         public View getView(int position, View convertView, ViewGroup parent) {        	
         	APItemView apitem = (APItemView) convertView;
    		final ScanResult sr = mItemList.get(position).getScanResult();
         	if (apitem == null) {
         		apitem = (APItemView)LayoutInflater.from(getContext()).inflate(R.layout.aprow, parent, false);
        		apitem.setScanResult(sr);
        	}        	
    		apitem.setScanResult(sr);
         	return apitem;
         }
     }	
     
     private class NetStatReceiver extends BroadcastReceiver {
         public void onReceive(Context context, Intent intent) {
            
             final String action = intent.getAction();
             System.out.println("Action: " + action	.toString());
             System.out.println("nwInfo: " + intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO).toString());
             
             if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {           	    
             	NetworkInfo nwInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            	    System.out.println("nwInfo: " + nwInfo.getState());
            	    
            	    switch(nwInfo.getState()) {
            	    	case CONNECTED:
             	    	connectToRobot(context);            	    	
            	    		break;
            	    	case CONNECTING:
                	        System.out.println("CONNECTING...");
            	    		break;
            	    	default:
                         System.out.println("DISCONNECTED!");
            	    }
             }
         }
     }; 
     
     private class WifiReceiver extends BroadcastReceiver {
         public void onReceive(Context c, Intent intent) {
         	List<ScanResult> wifiList = mMainWifi.getScanResults();        	
         	boolean wasEmpty = mAdapter.isEmpty();
             mAdapter.clear();
             mAdapter.notifyDataSetChanged();
             for(int i = 0; i < wifiList.size(); i++){
                 ScanResult sr = wifiList.get(i);                
                 mItemList.add(new APItemView(c, sr));
         		System.out.println("AP: " + sr.SSID + ", level: " + sr.level);
             }
             mAdapter.notifyDataSetChanged();
             if(wasEmpty) {
             	dismissProgress();
             }
         }
     }
 	
     private void connectToRobot(Context context) {
     	deactivateNetStatReceiver();
         WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
     	WifiInfo myWifiInfo = wifi.getConnectionInfo();
         long ip = myWifiInfo.getIpAddress();
         /// handle negative numbers (how the hell can this number be negative?)
         if(ip < 0) {
         	ip += 0x100000000L;
         }
         
         long ip3 = ip/0x1000000;
         long ip3mod = ip%0x1000000;
         long ip2 = ip3mod/0x10000;
         long ip2mod = ip3mod%0x10000;
         long ip1 = ip2mod/0x100;
         long ip0 = ip2mod%0x100;
                 
         if(ip0 != 0 || ip1 != 0 || ip2 != 0 || ip3 != 0) {
         	dismissProgress();
         	resetTimer();
             showProgress("One last step...", "Connecting to the robot...");
             mTimer.schedule(new TimerTask() { public void run() { runOnUiThread(RobotTimeoutFunc); } }, 1000);
             System.out.println("Robot timer scheduling...");
 
             System.out.println("IP address: " + ip0 + "." + ip1 + "." + ip2 + "." + ip3);
             String robotIp = String.valueOf(ip0) + "." + String.valueOf(ip1) + "." + String.valueOf(ip2) + ".1";
             System.out.println("trying to connect to [" + robotIp + ":9999]");
 
             class RobotStarter implements Runnable {
                 String robotIp;
                 RobotStarter(String ip) { 
                 	robotIp = ip; 
                 }
                 public void run() {
                 	System.out.println("RobotStarter running......");
                     RobotDriver.sRobot.start(robotIp, 9999);              	
                 }
             }
             Thread t = new Thread(new RobotStarter(robotIp));
             t.start();
             System.out.println("RobotStarter is started...");
         }       	
     }
 
     private void connectToWiFiAP(Context context, String ssid) {
     	showProgress("Hold on...", "Connecting to the network...");
         WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
         WifiConfiguration wc = new WifiConfiguration(); 
         wc.SSID = "\"" + ssid + "\"";
         wc.hiddenSSID = true;
         wc.status = WifiConfiguration.Status.DISABLED;     
         wc.priority = 40;
         
         wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
         wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
         wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
         wc.allowedAuthAlgorithms.clear();
         wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
         wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
         wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
         wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
         wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
         wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
         
         //wc.preSharedKey = "\"" + deviceConfig.wireless_passkey + "\"";// 
         System.out.println("ssid : " + wc.SSID);
 
         wifi.setWifiEnabled(true);
         int res = wifi.addNetwork(wc);
         wifi.enableNetwork(res, true);   
         wifi.saveConfiguration();
         
         activateNetStatReceiver();        
         mTimer.schedule(new TimerTask() { public void run() { runOnUiThread(WiFiTimeoutFunc); } }, 5000);
         System.out.println("WiFi timer scheduling...");
     }
     
     
     private Runnable WiFiTimeoutFunc = new Runnable() {
         public void run() {        	
         	deactivateNetStatReceiver();
         	System.out.println("WiFi timout...");
         	dismissProgress();
         	resetTimer();
         	showAlert("Failed to connect to the access point!");
         }
     };
 
     private Runnable RobotTimeoutFunc = new Runnable() {
         public void run() {
 			dismissProgress(); 
 			resetTimer();
         	if(RobotDriver.sRobot.isRunning()) {
             	System.out.println("Connected to the robot!");
     			Intent intent = new Intent (APSelectionActivity.this, JoystickActivity.class);
                 startActivity(intent);
             } else {                            	
             	RobotDriver.sRobot.stop();
     			showAlert("Failed to connect to the robot!");
     		}
         }
     };
     
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        setContentView(R.layout.main);  
        mItemList = new ArrayList<APItemView>();
        mAdapter = new APItemAdapter(this, R.layout.aprow, mItemList);
        setListAdapter(mAdapter);       
        mMainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mWifiReceiver = new WifiReceiver();
    	   mMainWifi.startScan();       
    	   showProgress("Hold on...", "Scanning access points...");
        mTimer = new Timer();
     }
 
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add(0, 0, 0, "Refresh");
         return super.onCreateOptionsMenu(menu);
     }
 
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         mMainWifi.startScan();
         return super.onMenuItemSelected(featureId, item);
     }
 
     protected void onPause() {
     	unregisterReceiver(mWifiReceiver);
     	dismissProgress();
     	resetTimer();    	
         super.onPause();
     }
 
     protected void onResume() {
         registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));        
         super.onResume();
     }
     
     public void onListItemClick(ListView parent, View v, int position, long id) {  
     	ScanResult scanResult = mAdapter.getItem(position).getScanResult();
     	//ScanResult scanResult = mResultList.get(position);
     	System.out.println("wifi item is clicked! ");
     	connectToWiFiAP(this, scanResult.SSID);
 	}
     
     private void showProgress(String title, String content) {
     	dismissProgress();
         mProgressDialog = ProgressDialog.show(APSelectionActivity.this, title, content, true);
     }
     private void dismissProgress() {
     	if(mProgressDialog != null) {
     		mProgressDialog.dismiss();
     	}
     }
     
 	private void activateNetStatReceiver() {
 		mNetStatReceiver = new NetStatReceiver();      
 		registerReceiver(mNetStatReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
 	}    
 
 	private void deactivateNetStatReceiver() {
 		if(mNetStatReceiver != null) {
 			unregisterReceiver(mNetStatReceiver);
 			mNetStatReceiver = null;
 		}
 	}
 
 	private void showAlert(String msg) {
 
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setMessage(msg);
 		alertDialogBuilder			
 			.setCancelable(false)
 			.setPositiveButton("ok", null);
 		AlertDialog alertDialog = alertDialogBuilder.create();
 		alertDialog.show();		
 	}
     
     private void resetTimer() {
     	mTimer.cancel();
     	mTimer.purge();
     	mTimer = new Timer();
     }
 }
