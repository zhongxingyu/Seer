 package com.carclub.cordova.plugins;
 
 import org.apache.cordova.api.CallbackContext;
 import org.apache.cordova.api.CordovaPlugin;
 import org.apache.cordova.api.LOG;
 import org.apache.cordova.api.PluginResult;
 import org.json.JSONArray;
 
 import android.content.Context;
 import android.telephony.TelephonyManager;
 
 public class TelephoneNumber extends CordovaPlugin {
 
     public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
         if (action.equals("get")) {
             TelephonyManager telephonyManager =
                 (TelephonyManager)this.cordova.getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            //String result = telephonyManager.getLine1Number();
            String result = telephonyManager.getSimSerialNumber();
             
             LOG.d("TelephoneNumber", "**********************Phone Number:"+result);
             
             if (result != null && result != "") {
                 callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
                return true;
             }
         }
         callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR));
         return false;
     }
 }
