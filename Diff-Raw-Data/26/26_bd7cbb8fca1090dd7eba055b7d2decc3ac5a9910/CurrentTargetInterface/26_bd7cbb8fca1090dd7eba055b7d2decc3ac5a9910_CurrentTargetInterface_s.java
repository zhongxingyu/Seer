 package com.whichwayis;
 
 import org.apache.cordova.api.Plugin;
 import org.apache.cordova.api.PluginResult;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.util.Log;
 import android.webkit.WebSettings.PluginState;
 
 public class CurrentTargetInterface extends Plugin {
    @Override
    public PluginResult execute(String action, JSONArray args, String callbackId) {
      if(action.equals("setTarget")) {
       
 	     try {
 		   String target = args.getString(0);
 		  
 		   WhichWayIsActivity ctx = (WhichWayIsActivity) this.ctx.getContext();
		   ctx.setTarget(target);
 		   return new PluginResult(PluginResult.Status.OK);
 		 } catch (JSONException e) {
 		   return new PluginResult(PluginResult.Status.ERROR);
 		 }
     	 	 
        
      }
 	 return new PluginResult(PluginResult.Status.INVALID_ACTION);
    }
 }
