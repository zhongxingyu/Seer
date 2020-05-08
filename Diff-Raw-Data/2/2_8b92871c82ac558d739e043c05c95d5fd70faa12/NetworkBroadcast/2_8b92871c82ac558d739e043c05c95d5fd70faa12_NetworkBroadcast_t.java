 package org.xierch.mpiwifi;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 import android.net.ConnectivityManager;
 import android.net.wifi.WifiInfo;  
 import android.net.wifi.WifiManager;  
 
 
 public class NetworkBroadcast extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		if (!intent.getAction().equals
 				(ConnectivityManager.CONNECTIVITY_ACTION))
 			return;
     	SharedPreferences settings = context
     			.getSharedPreferences("settings", Context.MODE_PRIVATE);
     	if (!settings.getBoolean("autoLogin", false)) return;
     	
     	SharedPreferences loginInfo = context
     			.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
     	String netId = loginInfo.getString("netId", "");
     	String pwd   = loginInfo.getString("pwd", "");
     	
     	if (netId.isEmpty() || pwd.isEmpty()) {
     		SharedPreferences.Editor editor = settings.edit();
     		editor.putBoolean("autoLogin", false);
     		editor.commit();
     		return;
     	}
     	
     	WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
     	if (!wifiManager.isWifiEnabled()) return;
     	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
     	if (wifiInfo == null) return;
     	String ssid = wifiInfo.getSSID();
     	String target;
     	if (ssid == null) return;
     	if (wifiInfo.getSSID().equals("NamOn_Hostel")) target = "Namon";
     	else return;
     	
     	Intent login = new Intent(context, WifiLoginer.class);
     	login.putExtra("target", target);
     	login.putExtra("username", netId);
     	login.putExtra("password", pwd);
    	login.putExtra("lessToast", true);
     	context.startService(login);
 	}
 
 }
