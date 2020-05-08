 package com.tealeaf.plugin.plugins;
 import java.util.Map;
 import org.json.JSONObject;
 import org.json.JSONArray;
 import org.json.JSONException;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import com.tealeaf.EventQueue;
 import com.tealeaf.TeaLeaf;
 import com.tealeaf.logger;
 import com.tealeaf.event.PluginEvent;
 import android.content.pm.PackageManager;
 import android.content.pm.ApplicationInfo;
 import android.os.Bundle;
 import java.util.HashMap;
 
 import com.tealeaf.plugin.IPlugin;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Intent;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.util.Log;
 
 import com.tealeaf.EventQueue;
 import com.tealeaf.event.*;
 
 import com.tapjoy.TapjoyConnect;
 import com.tapjoy.TapjoyConstants;
 import com.tapjoy.TapjoyLog;
 
 public class TapJoyPlugin implements IPlugin {
 	Context _ctx;
 	HashMap<String, String> manifestKeyMap = new HashMap<String,String>();
 
 	public TapJoyPlugin() {
 	}
 
 	public void onCreateApplication(Context applicationContext) {
 		_ctx = applicationContext;
 	}
 
 	public void onCreate(Activity activity, Bundle savedInstanceState) {
 		PackageManager manager = activity.getBaseContext().getPackageManager();
 		String[] keys = {"TapJoyAppID", "TapJoySecretKey"};
 		try {
 			Bundle meta = manager.getApplicationInfo(activity.getApplicationContext().getPackageName(),
 					PackageManager.GET_META_DATA).metaData;
 			for (String k : keys) {
 				if (meta.containsKey(k)) {
 					manifestKeyMap.put(k, meta.get(k).toString());
 				}
 			}
 		} catch (Exception e) {
 			logger.log("Exception while loading manifest keys:", e);
 		}
 
 		String TapJoyAppID = manifestKeyMap.get("TapJoyAppID");
 		String TapJoySecretKey = manifestKeyMap.get("TapJoySecretKey");
 
 		logger.log("{tapjoy} Installing TapJoy for appID:", TapJoyAppID);
 
 		// Enables logging to the console.
 		TapjoyLog.enableLogging(true);
 
 		// Connect with the Tapjoy server.
 		TapjoyConnect.requestTapjoyConnect(_ctx, TapJoyAppID, TapJoySecretKey);
 	}
 
 	public void onResume() {
 	}
 
 	public void onStart() {
 	}
 
 	public void onPause() {
 	}
 
 	public void onStop() {
 	}
 
 	public void onDestroy() {
 	}
 
 	public void onNewIntent(Intent intent) {
 	}
 
 	public void setInstallReferrer(String referrer) {
 	}
 
 	public void onActivityResult(Integer request, Integer result, Intent data) {
 	}
 
 	public void logError(String error) {
 	}
 
 	public boolean consumeOnBackPressed() {
		return true;
 	}
 
 	public void onBackPressed() {
 	}
 }
 
