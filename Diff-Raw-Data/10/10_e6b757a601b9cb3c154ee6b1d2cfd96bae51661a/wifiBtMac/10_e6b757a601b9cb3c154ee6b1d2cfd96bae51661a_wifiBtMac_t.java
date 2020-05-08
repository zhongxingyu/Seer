 package com.tware.anexter;
 
 
 import java.util.List;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.wifi.ScanResult;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class wifiBtMac extends Activity {
 	private Button bPass;
 	private Button bFail;
 	private Button bNa;
 	private WifiManager mWifi;
 	private String LOG;
 	private String TAG = "getMAC";
 	private String wifiName = "Tablet"; 
 	private Boolean isSet = false;
 	private IntentFilter intentFilter = new IntentFilter();
 	
 	private String btMac, WifiMac;
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.wifibtmac);
         this.setTitle("aNexter - 无线与蓝牙地址检查");
         
         bPass = (Button)findViewById(R.id.btn_pass);
         bPass.setEnabled(false);
         bPass.setOnClickListener(new OnClickListener(){
         	@Override
 			public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "PASS|");
         		i.setClass(wifiBtMac.this , drawline.class);
         		startActivity(i);
         		wifiBtMac.this.finish();
         	}
         });
         
         TextView mac = (TextView)findViewById(R.id.view_wifi_bt);
 
         mWifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
 
         if (!mWifi.isWifiEnabled())
         {
         	mWifi.setWifiEnabled(true);
         }
         
         mWifi.startScan();
         
         WifiInfo wifiInfo = mWifi.getConnectionInfo();
         
         BluetoothAdapter bAdapt= BluetoothAdapter.getDefaultAdapter();
        
         if (bAdapt != null)
         {
             if (!bAdapt.isEnabled())
             {
             	bAdapt.enable();
             }
             
             btMac = bAdapt.getAddress().toUpperCase();
         }else{
         	btMac = "No Bluetooth Device!";
         }
         
         if((WifiMac = wifiInfo.getMacAddress().toUpperCase())== null)
         {
         	WifiMac = "No Wifi Device";
         }
 
         mac.setTextSize(38);
         mac.setText("无线MAC:  "+ WifiMac );
         if (!btMac.equalsIgnoreCase("No Bluetooth Device!"))
         	mac.append("\n蓝牙MAC:  " + btMac);
 
         	bFail = (Button)findViewById(R.id.btn_fail);
         	bFail.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "FAIL|");
         		i.setClass(wifiBtMac.this , getResults.class);
         		startActivity(i);
         		wifiBtMac.this.finish();
         	}
         });
         
         bNa = (Button)findViewById(R.id.btn_na);
         bNa.setVisibility(View.INVISIBLE);
         
         LOG = this.getIntent().getStringExtra("LOG");
         Toast.makeText(getApplicationContext(), LOG, Toast.LENGTH_SHORT).show();
 
     }
     
 	private BroadcastReceiver scanResultsReceiver = new BroadcastReceiver(){
 		@Override
 		public void onReceive(Context arg0, Intent intent) {
 			String action = intent.getAction();
 
 			if (action.equals("android.net.wifi.SCAN_RESULTS") && ! isSet)
 			{
 				Log.d(TAG, "startScan() completed...");
 				bPass.setEnabled(true);
 				new Thread(new Runnable(){
 					@Override
 					public void run() {						
 						Log.i(TAG, "start connect from thread.");
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
								if (Build.VERSION.SDK_INT >= 16){
									wc.wepKeys[0]=null;
									wc.wepKeys[1]=null;
									wc.wepKeys[2]=null;
								}
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
 					
 				}).start();
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
 		return super.onKeyDown(keyCode, event);
 	}
 }
