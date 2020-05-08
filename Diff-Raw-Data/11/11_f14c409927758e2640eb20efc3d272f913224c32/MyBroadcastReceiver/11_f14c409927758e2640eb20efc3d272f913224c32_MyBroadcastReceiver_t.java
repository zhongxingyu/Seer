 package com.example.dummywifi;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.p2p.WifiP2pDevice;
 import android.net.wifi.p2p.WifiP2pManager;
 
 public class MyBroadcastReceiver extends BroadcastReceiver {
 	
 	private MainActivity activity;
 	
 	public MyBroadcastReceiver (MainActivity activity){
 		super();
 		this.activity = activity;
 	}
 	
 	@Override
     public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
         if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
             // Determine if Wifi P2P mode is enabled or not, alert
             // the Activity.
             int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
             if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                 activity.setIsWifiP2pEnabled(true);
             } else {
                 activity.setIsWifiP2pEnabled(false);
             }
         } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
 
             // The peer list has changed!  We should probably do something about
             // that.
 
         } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
 
             // Connection state changed!  We should probably do something about
             // that.
         
         } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
         	
         	//This is all UI related code, we won't use it.
         	/*
             DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                     .findFragmentById(R.id.frag_list);
             fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                     WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
             */
 
         }
     }
 	
 }
