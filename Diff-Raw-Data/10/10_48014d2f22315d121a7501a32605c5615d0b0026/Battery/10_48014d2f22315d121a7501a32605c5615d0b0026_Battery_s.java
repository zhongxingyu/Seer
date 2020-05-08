 package com.gingbear.githubtest;
 
 import com.gingbear.githubtest.receiver.BatteryReceiver;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.BatteryManager;
 
 public class Battery extends BatteryData{
 	
 	   private static Battery instance;
 	   static BroadcastReceiver mBroadcastReceiver;
 	   private Battery(){}; 
 	   public static synchronized Battery getInstance(){
 	     if(instance == null){ 
 	       instance = new Battery();
 		   mBroadcastReceiver = new BatteryReceiver(instance);
 	     }
 	     return instance;
 	   }
 	   
 	   public void registerReceiver(Activity activity){
 
 	        IntentFilter filter = new IntentFilter();
 	        
 	        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
 	        activity.registerReceiver(mBroadcastReceiver, filter);
 	   }
 	   public void unregisterReceiver(Activity activity){
 		   activity.unregisterReceiver(mBroadcastReceiver);
 	   }
 	
	
 	public boolean checkBattery(Context context, Intent intent) {
         String action = intent.getAction();
         
         if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
              status = intent.getIntExtra("status", 0);
              health = intent.getIntExtra("health", 0);
              present = intent.getBooleanExtra("present", false);
              level = intent.getIntExtra("level", 0);
              scale = intent.getIntExtra("scale", 0);
              icon_small = intent.getIntExtra("icon-small", 0);
              plugged = intent.getIntExtra("plugged", 0);
              voltage = intent.getIntExtra("voltage", 0);
              temperature = intent.getIntExtra("temperature", 0);
              technology = intent.getStringExtra("technology");
             
             switch (status) {
             case BatteryManager.BATTERY_STATUS_UNKNOWN:
                 statusString = "unknown";
                 break;
             case BatteryManager.BATTERY_STATUS_CHARGING:
                 statusString = "charging";
                 break;
             case BatteryManager.BATTERY_STATUS_DISCHARGING:
                 statusString = "discharging";
                 break;
             case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                 statusString = "not charging";
                 break;
             case BatteryManager.BATTERY_STATUS_FULL:
                 statusString = "full";
                 break;
             }
             
             switch (health) {
             case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                 healthString = "unknown";
                 break;
             case BatteryManager.BATTERY_HEALTH_GOOD:
                 healthString = "good";
                 break;
             case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                 healthString = "overheat";
                 break;
             case BatteryManager.BATTERY_HEALTH_DEAD:
                 healthString = "dead";
                 break;
             case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                 healthString = "voltage";
                 break;
             case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                 healthString = "unspecified failure";
                 break;
             }
             
             switch (plugged) {
             case BatteryManager.BATTERY_PLUGGED_AC:
                 acString = "plugged ac";
                 break;
             case BatteryManager.BATTERY_PLUGGED_USB:
                 acString = "plugged usb";
                 break;
             }    
             return true;
         }
         return false;
     }
 	
     
 }
