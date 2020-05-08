 package com.dbstar.app.settings;
 
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.WifiConfiguration;
 import android.net.wifi.WifiManager;
 
 import com.dbstar.app.GDApplication;
 import com.dbstar.model.GDCommon;
 
 import android.net.ethernet.EthernetManager;
 import android.net.ethernet.EthernetDevInfo;
 
 public class RestoreFactoryUtil {
 
 	private static final String ActionClearSettings = "com.dbstar.Settings.Action.CLEAR_SETTINGS";
 	public static void clearNetworkInfo() {
 		Context context = GDApplication.getAppContext();
 		WifiManager wifiMgr = (WifiManager) context
 				.getSystemService(Context.WIFI_SERVICE);
 
 		List<WifiConfiguration> allConfigs = wifiMgr.getConfiguredNetworks();
 		for(WifiConfiguration config: allConfigs) {
 			wifiMgr.removeNetwork(config.networkId);
 		}
 		
 		wifiMgr.saveConfiguration();
 		
 		
 		EthernetManager ethernetMgr = (EthernetManager) context
 		.getSystemService(Context.ETH_SERVICE);
 		
 		EthernetDevInfo ethInfo = null;
 		if (ethernetMgr.isEthConfigured()) {
 			ethInfo = ethernetMgr.getSavedEthConfig();
			if (ethInfo.getConnectMode().equals(
 					EthernetDevInfo.ETH_CONN_MODE_MANUAL)) {
 				ethInfo.setConnectMode(EthernetDevInfo.ETH_CONN_MODE_DHCP);
 				ethInfo.setIpAddress(null);
 				ethInfo.setRouteAddr(null);
 				ethInfo.setDnsAddr(null);
 				ethInfo.setNetMask(null);
 				
 				ethernetMgr.updateEthDevInfo(ethInfo);
 			}
 		}
 	}
 	
 	public static void clearSystemSettings() {
 		// send broadcast to settings package
 		Context context = GDApplication.getAppContext();
 		Intent intent = new Intent(ActionClearSettings);
 		context.sendBroadcast(intent);
 	}
 	
 	public static void clearPushSettings() {
 		// send broadcast to system
 		Context context = GDApplication.getAppContext();
 		Intent intent = new Intent(GDCommon.ActionSystemRecovery);
 		intent.putExtra(GDCommon.KeyRecoveryType, GDCommon.RecoveryTypeClearPush);
 		context.sendBroadcast(intent);
 	}
 	
 	public static void clearDrmInfo() {
 		// send broadcast to system
 		Context context = GDApplication.getAppContext();
 		Intent intent = new Intent(GDCommon.ActionSystemRecovery);
 		intent.putExtra(GDCommon.KeyRecoveryType, GDCommon.RecoveryTypeClearDrmInfo);
 		context.sendBroadcast(intent);
 	}
 	
 	public static void formatDisk () {
 		Context context = GDApplication.getAppContext();
 		Intent intent = new Intent(GDCommon.ActionSystemRecovery);
 		intent.putExtra(GDCommon.KeyRecoveryType, GDCommon.RecoveryTypeFormatDisk);
 		context.sendBroadcast(intent);
 	} 
 
 }
