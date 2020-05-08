 package com.example.dummywifi;
 
 
 
 
 import android.net.wifi.p2p.WifiP2pConfig;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.net.wifi.p2p.WifiP2pManager.ActionListener;
 import android.net.wifi.p2p.WifiP2pManager.Channel;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 import android.content.BroadcastReceiver;
 
 public class MainActivity extends Activity {
 
 	
 	private Button p2pbutt;
 	private Button discoverbutt;
 	private Button frontButton;
 	//We need an intent filter to catch only the intents we care about
 	private final IntentFilter intentFilter = new IntentFilter();
 	
 	
 	//We need a boolean to determine if the devices WiFiP2p is enabled
 	private boolean isWifiP2pEnabled = false;
 	
 	//Declare the WifiP2pManager
 	WifiP2pManager mManager;
 	
 	//Declare the Channel
 	Channel mChannel;
 
 	//We need to make a MyBroadCastReceiver to... receive broadcasts!?
 	BroadcastReceiver mReceiver;
 	
 	//We need a setter function to set the value of the boolean isWifiP2pEnabled
 	//Do this when the user's peer-to-peer wifi gets enabled from disabled or disabled from enabled.
 	void setIsWifiP2pEnabled(boolean newVal){
 		this.isWifiP2pEnabled = newVal;
 	}
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         
         
         p2pbutt = (Button) findViewById(R.id.atn_direct_enable);
         discoverbutt = (Button) findViewById(R.id.atn_direct_discover);
         frontButton = (Button) findViewById(R.id.atn_front_page);
         
         /*We care about 4 intents:
         When the WiFi status changes, when the list of available peers changes,
         when the state of the WiFi connectivity has changed, and when the state
         of the device has changed */
         
         //The "WiFi has changed status" intent
         intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
         
         //The "peers list has changed" intent
         intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
         
         //The "state of the WiFi connectivity has changed" intent
         intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
         
         //Indicates the device's details have changed
         intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
         
         //This creates an instance of a WiFiP2pManager object
         //We are going to use it to make a WiFiP2pManager.Channel object
         mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
         //This makes the WifiP2pManager.CHannel object
         mChannel = mManager.initialize(this,getMainLooper(), null);
         
         p2pbutt.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
             	if (mManager != null && mChannel != null) {
 
                     // Since this is the system wireless settings activity, it's
                     // not going to send us a result. We will be notified by
                     // WiFiDeviceBroadcastReceiver instead.
             		startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                 } else {
                     System.out.println("channel or manager is null");
                 }
             }
         });
         discoverbutt.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
             	if (!isWifiP2pEnabled) {
                     Toast.makeText(MainActivity.this, R.string.p2p_off_warning,
                             Toast.LENGTH_SHORT).show();
                 }
                 mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
 
                     @Override
                     public void onSuccess() {
                         Toast.makeText(MainActivity.this, "Discovery Initiated",
                                 Toast.LENGTH_SHORT).show();
                     }
 
                     @Override
                     public void onFailure(int reasonCode) {
                         Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,
                                 Toast.LENGTH_SHORT).show();
                     }
                 });
             }
         });
         
         frontButton.setOnClickListener(new OnClickListener() {
         	
         	public void onClick(View v){
         		Intent intent = new Intent(MainActivity.this,FrontActivity.class);
         	
         		startActivity(intent);
         	}
         });
         
     }
 
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     /*This is the function That registers a MyBroadcastReceiver when the activity resumes from being paused.
       This allows the app to switch the broadcast receiver to switch on and off when the user pauses using the
       app but does not end the process completley.
     */
     
     @Override
     public void onPause(){
     	//Since this is an overwrite of Activities onPause() function, we call that as well
     	super.onPause();
     	//This is a call to unregister a previously registered BroadcastRecceiver object
     	if (mReceiver != null){
     		unregisterReceiver(mReceiver);
     	}
     }
     
     @Override
     public void onResume(){
    	super.onResume();
     	//Register the receiver to receive only the intents our intent Filter sets up
    	mReceiver = new MyBroadcastReceiver(this);
     	registerReceiver(mReceiver,intentFilter);
     	
     }
     
 
     
     public void connect(WifiP2pConfig config) {
         mManager.connect(mChannel, config, new ActionListener() {
 
             @Override
             public void onSuccess() {
                 // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
             }
 
             @Override
             public void onFailure(int reason) {
                 Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                         Toast.LENGTH_SHORT).show();
             }
         });
     }
     
 }
