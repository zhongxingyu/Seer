 package com.tealeaf.plugin.plugins;
 
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import com.tealeaf.logger;
 import com.tealeaf.TeaLeaf;
import com.amplitude.android.Amplitude;
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
 
 
 public class AmplitudePlugin implements IPlugin {
     Activity activity;
 
     public AmplitudePlugin() {
 
     }
 
     public void onCreateApplication(Context applicationContext) {
 
     }
 
     public void onCreate(Activity activity, Bundle savedInstanceState) {
         this.activity = activity;
         PackageManager manager = activity.getPackageManager();
         String amplitudeKey = "";
         try {
             Bundle meta = manager.getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA).metaData;
             if (meta != null) {
                 amplitudeKey = meta.getString("AMPLITUDE_KEY");
             }
         } catch (Exception e) {
             android.util.Log.d("EXCEPTION", "" + e.getMessage());
         }
 
        Amplitude.initialize(this, amplitudeKey);
     }
 
     public void onResume() {
         Amplitude.startSession();
     }
 
     public void onStart() {
     }
 
     public void onPause() {
         Amplitude.endSession();
     }
 
     public void onStop() {
     }
 
     public void logEvent(String json) {
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
                    logger.log("{amplitude} {android} logEvent - failure: " + eventName + " - " + e.getMessage());
                }

                if (value != null) {
                    params.put(key, value);
                }
            }
            Amplitude.logEvent(eventName, params);
             logger.log("{amplitude} {android} logEvent - success: " + eventName);
         } catch (JSONException e) {
             logger.log("{amplitude} {android} logEvent - failure: " + eventName + " - " + e.getMessage());
         }
     }
 
     public void onDestroy() {
     }
 
     public void onNewIntent(Intent intent) {
 
     }
 
     public void setInstallReferrer(String referrer) {
 
     }
 
     public void onActivityResult(Integer request, Integer result, Intent data) {
 
     }
 
     public boolean consumeOnBackPressed() {
         return false;
     }
 
     public void onBackPressed() {
     }
 
 }
