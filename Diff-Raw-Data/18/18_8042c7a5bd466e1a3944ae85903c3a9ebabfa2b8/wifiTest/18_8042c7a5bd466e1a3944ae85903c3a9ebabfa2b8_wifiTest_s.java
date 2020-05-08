 package com.tware.anexter;
 
 
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.ConnectivityManager;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class wifiTest extends Activity {
 	private Button bPass;
 	private Button bFail;
 	private Button bNa;
 	private Button bWifi;
 	private WifiManager mWifi;
 	private WifiInfo wifiInfo;
 	private String LOG;
 	private ConnectivityManager con;
 	private String TAG = "WiFiTest";
 	private String wifiName = "Tablet"; 
 	private Boolean isSet = false;
 	private IntentFilter intentFilter = new IntentFilter();
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.wifi);
         this.setTitle("aNexter - 无线连接测试");
 
         mWifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
 		con=(ConnectivityManager)getSystemService(Activity.CONNECTIVITY_SERVICE);
 
 		intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
 		registerReceiver(scanResultsReceiver, intentFilter);
 		
         timer.schedule(task, 1000, 1000);
 
         bPass = (Button)findViewById(R.id.btn_pass);       
         bPass.setEnabled(false);
         /*        
         bPass.setOnClickListener(new OnClickListener(){
         	@Override
 			public void onClick(View v)
         	{
         		startActivity(new Intent(wifiTest.this , inputchar.class));
         		wifiTest.this.finish();
         	}
         });
 */        
         bWifi = (Button)findViewById(R.id.btn_wifi);
         bWifi.setOnClickListener(new OnClickListener(){
         	@Override
         	public void onClick(View v)
         	{	
         		bWifi.setEnabled(false);
         		startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
         	}
         });
         
         bFail = (Button)findViewById(R.id.btn_fail);
         bFail.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
 	        	if (timer != null)
 	        	{
 	        		timer.cancel();
 	        		timer = null;
 	        	}
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "FAIL|");
         		i.setClass(wifiTest.this , getResults.class);
         		startActivity(i);
         		wifiTest.this.finish();
         	}
         });
         
         bNa = (Button)findViewById(R.id.btn_na);
         bNa.setVisibility(View.VISIBLE);
         bNa.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
 	        	if (timer != null)
 	        	{
 	        		timer.cancel();
 	        		timer = null;
 	        	}
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "FAIL|");
         		i.setClass(wifiTest.this , imageMcu.class);
         		startActivity(i);
         		wifiTest.this.finish();
         	}
         });
         
         LOG = this.getIntent().getStringExtra("LOG");
         
     }
     
 	private Handler uHandler = new Handler();
 	private Timer timer = new Timer();
 	private TimerTask task = new TimerTask() {
 
 		@Override
 		public void run() {
 			uHandler.post(new Runnable() {
 				@Override
 				public void run() {
 		        	wifiInfo = mWifi.getConnectionInfo();
 		        	if (!mWifi.isWifiEnabled())
 		        	{
 		        		mWifi.setWifiEnabled(true);
 		        	}
 					if(con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected())
 					{
 			        	if (timer != null)
 			        	{
 			        		timer.cancel();
 			        		timer = null;
 			        	}
 			        	Intent i = new Intent();
 		        		i.putExtra("LOG", LOG + wifiInfo.getMacAddress().toUpperCase() + "|");
 		        		i.setClass(wifiTest.this , imageMcu.class);
 		        		startActivity(i);
 		        		wifiTest.this.finish();
 			        }
 					/*start scan after connection check*/
 		        	mWifi.startScan();
 				}
 			});
 		}
 
 	};
 
 	private BroadcastReceiver scanResultsReceiver = new BroadcastReceiver(){
 		@Override
 		public void onReceive(Context arg0, Intent intent) {
 			String action = intent.getAction();
 
 			if (action.equals("android.net.wifi.SCAN_RESULTS") && ! isSet)
 			{
 				Log.d(TAG, "startScan() completed...");
 				List<ScanResult> sRet = mWifi.getScanResults();
 				for (int i=0; i<sRet.size(); i++)
 				{
 					ScanResult retS = sRet.get(i); 
 					Log.d(TAG, "WiFi: " + retS.SSID +" " + retS.BSSID);
 
 					if (retS.SSID.equalsIgnoreCase(wifiName))  /* Here defined SSID to connected , default is "Tablet"*/
 					{
 						Log.e(TAG, "Found: " + retS.SSID +" " + retS.BSSID + "\n");
 
 						WifiConfiguration wc = new WifiConfiguration();
 						wc.allowedAuthAlgorithms.clear();
 						wc.allowedGroupCiphers.clear();
 						wc.allowedKeyManagement.clear();
 						wc.allowedProtocols.clear();
 						wc.allowedPairwiseCiphers.clear();
 
 						wc.SSID = "\""+retS.SSID+"\"";
 						if (Build.VERSION.SDK_INT >= 16)
							wc.wepKeys[0]=null;
 						else
 							wc.wepKeys[0]="";
 						wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
 						wc.wepTxKeyIndex = 0;
 
 						int netID = mWifi.addNetwork(wc); // add network
 						Log.d(TAG, "addNetwork: "+ Integer.toString(netID));
 
 						if (mWifi.saveConfiguration()) /*Save it for later use*/
 						{
 							Log.d(TAG, "saveConfiguration ok");
 						}else{
 							Log.d(TAG, "saveConfiguration Failed");
 						}
 
 						if(mWifi.enableNetwork(netID, true)) // enable network
 						{
 							Log.d(TAG, "enableNetwork: true\n");
 						}else
 						{
 							Log.d(TAG, "enableNetwork: false\n");
 						}
 						isSet = true;  /* if found the SSID then ignore the actions  */
 					}
 				}
 			}
 		}
 
 	};
 	
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		intentFilter.addAction("android.net.wifi.SCAN_RESULTS");
 		registerReceiver(scanResultsReceiver, intentFilter);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(scanResultsReceiver);
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return true;
 		}
 		bWifi.setEnabled(true);
 		return super.onKeyDown(keyCode, event);
 	}
     
 }
