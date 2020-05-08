package com.phonegap.plugins.flurry
 
 import org.apache.cordova.CordovaPlugin;
 import org.apache.cordova.CallbackContext;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * This class echoes a string called from JavaScript.
  */
 public class FlurryCDVPlugin extends CordovaPlugin {
 
     @Override
     public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
         if (action.equals("logEvent")) {
             String eventName = args.getString(0);
             this.logEvent(eventName, callbackContext);
             return true;
         }
         return false;
     }
 
     private void logEvent(String eventName, CallbackContext callbackContext) {
         if (eventName != null && eventName.length() > 0) {
             callbackContext.success(eventName);
         } else {
             callbackContext.error("Expected one non-empty string argument.");
         }
     }
 }
