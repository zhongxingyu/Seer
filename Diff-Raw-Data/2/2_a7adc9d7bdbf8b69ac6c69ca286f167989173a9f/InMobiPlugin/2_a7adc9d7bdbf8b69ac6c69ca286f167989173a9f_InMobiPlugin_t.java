 package com.tealeaf.plugin.plugins;
 
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import com.tealeaf.logger;
 import com.tealeaf.TeaLeaf;
 
 import com.inmobi.commons.InMobi;
 import com.inmobi.analytics.InMobiAnalytics;
 
 import com.tealeaf.plugin.IPlugin;
 import java.io.*;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import com.tealeaf.util.HTTP;
 import java.net.URI;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.Context;
 import android.util.Log;
 import android.os.Bundle;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.content.SharedPreferences;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Iterator;
 
 
 public class InMobiPlugin implements IPlugin {
     Context ctx;
 
     public InMobiPlugin() {
 
     }
 
     public void onCreateApplication(Context applicationContext) {
         this.ctx = applicationContext;
     }
 
     public void onCreate(Activity activity, Bundle savedInstanceState) {
         PackageManager manager = activity.getPackageManager();
         String inMobiKey = "";
         try {
             Bundle meta = manager.getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA).metaData;
             if (meta != null) {
                inMobiKey = meta.get("INMOBI_KEY").toString();
             }
         } catch (Exception e) {
             android.util.Log.d("EXCEPTION", "" + e.getMessage());
         }
         InMobi.initialize(ctx, inMobiKey);
         InMobiAnalytics.startSession(ctx);
     }
 
     public void onResume() {
 
     }
 
     public void onStart() {
     }
 
     public void track(String json) {
         String eventName = "noName";
         try {
             JSONObject obj = new JSONObject(json);
             eventName = obj.getString("eventName");
             Map<String, String> params = new HashMap<String, String>();
             JSONObject paramsObj = obj.getJSONObject("params");
             Iterator<String> iter = paramsObj.keys();
             while (iter.hasNext()) {
                 String key = iter.next();
                 String value = null;
                 try {
                     value = paramsObj.getString(key);
                 } catch (JSONException e) {
                     logger.log("{inmobi} track - failure: " + eventName + " - " + e.getMessage());
                 }
 
                 if (value != null) {
                     params.put(key, value);
                 }
             }
             InMobiAnalytics.trackCustomEvent(eventName, params);
             logger.log("{inmobi} track - success: " + eventName);
         } catch (JSONException e) {
             logger.log("{inmobi} track - failure: " + eventName + " - " + e.getMessage());
         }
     }
 
     public void onPause() {
 
     }
 
     public void onStop() {
     }
 
     public void onDestroy() {
         InMobiAnalytics.endSession();
     }
 
     public void onNewIntent(Intent intent) {
 
     }
 
     public void setInstallReferrer(String referrer) {
 
     }
 
     public void onActivityResult(Integer request, Integer result, Intent data) {
 
     }
 
     public boolean consumeOnBackPressed() {
         return true;
     }
 
     public void onBackPressed() {
     }
 }
