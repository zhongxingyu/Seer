 package com.squirrel.action;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.SupplicantState;
 import android.net.wifi.WifiManager;
 
 public class IntentRemapperReceiver extends BroadcastReceiver
 {
 	public final static String extraName = "SmartReminder_Event_Extra";
     private final static int minutesBeforeRebroadcast = 2;
 	
 	@Override
 	public void onReceive(Context context, Intent intent)
 	{
 		String action = intent.getAction();
 
 		if (action.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION))
 		{
             WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
             SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
 
 			if(state == SupplicantState.COMPLETED)
 			{
                 String connectingToSsid = manager.getConnectionInfo().getSSID().replace("\"", "");
                 WifiStateHistory.recordConnectedSsid(connectingToSsid);
 
                 if(WifiStateHistory.notBroadcastInMinutes(connectingToSsid, minutesBeforeRebroadcast))
                 {
                     WifiStateHistory.recordBroadcastNow(connectingToSsid);
                     broadcastIntent(Action.SmartReminder_Event_WifiConnected.name(), connectingToSsid, context);
                 }
 			}
 
			if(state == SupplicantState.DISCONNECTED)
 			{
 				if(manager.isWifiEnabled())
 				{
                     String disconnectedFromSsid = WifiStateHistory.getLastConnectedSsid();
 
                     if(WifiStateHistory.notBroadcastInMinutes(disconnectedFromSsid, minutesBeforeRebroadcast))
                     {
                         WifiStateHistory.recordBroadcastNow(disconnectedFromSsid);
                         broadcastIntent(Action.SmartReminder_Event_WifiDisconnected.name(), disconnectedFromSsid, context);
                     }
 				}
 			}
 		}
 
 		if (action.equals(Intent.ACTION_POWER_CONNECTED))
 		{
 			broadcastIntent(Action.SmartReminder_Event_PowerConnected.name(), null, context);
 		}
 
 		if (action.equals(Intent.ACTION_POWER_DISCONNECTED))
 		{
 			broadcastIntent(Action.SmartReminder_Event_PowerDisconnected.name(), null, context);
 		}
 	}
 
     private void broadcastIntent(String action, String extra, Context context)
 	{
 		Intent newIntent = new Intent();
 		
 		newIntent.setAction(action);
         newIntent.putExtra(extraName, extra);
         
         context.sendBroadcast(newIntent);
 	}
 }
