 package com.codeb.cordova.plugins.settings;
  
 import org.apache.cordova.CordovaPlugin;
 import org.apache.cordova.CallbackContext;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.net.wifi.WifiManager;
 import android.bluetooth.BluetoothAdapter;
 import android.provider.Settings;
 import android.os.Looper;
 import android.util.Log;
 
 /**
  * This class echoes a string called from JavaScript.
  */
 public class Settings extends CordovaPlugin {
 
 	private static final String LOG_TAG = "Settings Plugin";
 	
     @Override
     public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
 		try {
 			JSONObject arg_object = args.getJSONObject(0);
 			if (action.equals("getBluetooth")) {
 				String message = arg_object.getString("action");
 				this.getBluetooth(callbackContext);
 				return true;
 			} else if (action.equals("setBluetooth")) {
 				String message = arg_object.getString("action");
 				this.setBluetooth(message, callbackContext);
 				return true;
 			}
 			return false;
 		} catch(Exception e) {
             callbackContext.error(e.getMessage());
             return false;
 		}
     }
 
     private void setBluetooth(String action, CallbackContext callbackContext) {
         Log.d(LOG_TAG, "Execute setBluetooth");
 		if (action != null && action.length() > 0) {
             callbackContext.success(action);
         } else {
             callbackContext.error("Expected one non-empty string argument.");
         }
     }
 	
 	private void getBluetooth(CallbackContext callbackContext) {
 		Looper.prepare();
 		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
 		boolean result = bluetoothAdapter.isEnabled();
 		Log.d(LOG_TAG, "Bluetooth enabled: " + result);
		return result;
 	}
 	
 }
 
