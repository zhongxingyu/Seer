 package pl.ug.ash;
 
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.CordovaPlugin;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import java.lang.Class;
 import java.lang.IllegalAccessException;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import android.content.Context;
 import android.content.pm.ActivityInfo;
 import android.util.Log;
 import android.net.ConnectivityManager;
 
 public class AshPlugin extends CordovaPlugin {
 
   public static final String ACTION_ORIENTATION_HORIZONTAL = "orientationHorizontal";
   public static final String ACTION_ORIENTATION_VERTICAL = "orientationVertical";
   public static final String ACTION_NETWORK_OFF = "networkOff";
   public static final String ACTION_NETWORK_ON = "networkOn";
   
   @Override
   public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
 
     if (ACTION_ORIENTATION_HORIZONTAL.equals(action)) {
       try {
         Log.d("HelloPlugin", "Changing orientation to horizontal");
         changeOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         
         callbackContext.success("");
         return true;
       }
       catch (Exception ex) {
         Log.d("AshPlugin error:", ex.toString());
       }  
     }
     if (ACTION_ORIENTATION_VERTICAL.equals(action)) {
       try {
         Log.d("HelloPlugin", "Changing orientation to vertical");
         changeOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         
         callbackContext.success("");
         return true;
       }
       catch (Exception ex) {
         Log.d("AshPlugin error:", ex.toString());
       }  
     }
     if (ACTION_NETWORK_OFF.equals(action)) {
       try {
         Log.d("HelloPlugin", "Blocking access to network");
         
         setNetworkConnectivity(false);
         
         callbackContext.success("");
         return true;
       }
       catch (Exception ex) {
    //DEBUG
          Context context = this.cordova.getActivity().getApplicationContext();
    final ConnectivityManager conman = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
    //final Class conmanClass = Class.forName(conman.getClass().getName());
    
        Log.d("AshPlugin error:", /*ex.toString() + " " +*/ conman.getClass().getName());
       }  
     }
     if (ACTION_NETWORK_ON.equals(action)) {
       try {
         Log.d("HelloPlugin", "Enabling network");
           
         setNetworkConnectivity(true);
   
         callbackContext.success("");
         return true;
       }
       catch (Exception ex) {
         Log.d("AshPlugin error:", ex.toString());
       }  
     }
 
     Log.d("AshPlugin error: No action " + action, "");
    callbackContext.error("Error");
     return false;
   }
 
   private void setNetworkConnectivity(boolean turnOn) 
       throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
     Context context = this.cordova.getActivity().getApplicationContext();
       
     final ConnectivityManager conman = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
     final Class conmanClass = Class.forName(conman.getClass().getName());
    /*final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
     iConnectivityManagerField.setAccessible(true);
     final Object iConnectivityManager = iConnectivityManagerField.get(conman);
     final Class iConnectivityManagerClass =  Class.forName(iConnectivityManager.getClass().getName());
     final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
     setMobileDataEnabledMethod.setAccessible(true);
 
    setMobileDataEnabledMethod.invoke(iConnectivityManager, turnOn);*/
   }
     
   private void disableNetwork() {
   
 //    Settings.System.putInt(
 //      cordova.getActivity().getApplicationContext().getContentResolver(),
 //      Settings.System.AIRPLANE_MODE_ON, 0);
 //
 //    Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
 //    intent.putExtra("state", 0);
 //    cordova.getActivity().sendBroadcast(intent);
 
     //TODO: 
     throw new RuntimeException("Since Android 4.x it's not posible to send Intent.ACTION_AIRPLANE_MODE_CHANGED without root permissions");
   }
 
   private void changeOrientation(int orientation) {
     this.cordova.getActivity().setRequestedOrientation(orientation);
   }
 }
