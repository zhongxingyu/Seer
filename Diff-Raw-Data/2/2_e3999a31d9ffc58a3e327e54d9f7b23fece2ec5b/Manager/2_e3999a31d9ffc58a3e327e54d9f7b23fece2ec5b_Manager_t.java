 package edu.brown.cs.systems.modes.lib;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 
 import edu.brown.cs.systems.modes.lib.data.ModeData;
 
 /**
  * @author Marcelo Martins <martins@cs.brown.edu>
  * 
  */
 public class Manager {
 
     private static final String PREFERENCES_FILE = "AppModePreferences";
     private static final String TAG = "ModeManager";
 
     private IModeService modeService;
     private ModeServiceConnection modeConnection;
     private Context context;
 
     private int uid;
     private String appName;
     private String packageName;
 
     public Manager(Context context) {
 
         assert context != null;
         this.context = context;
         ApplicationInfo ai = context.getApplicationInfo();
         PackageManager pm = context.getPackageManager();
         uid = ai.uid;
         appName = (String) pm.getApplicationLabel(ai);
         packageName = context.getApplicationInfo().packageName;
     }
 
     /**
      * Connects a mode-supporting app to the middleware. Should be called by
      * apps on its main activity onCreate()
      * 
      * @return whether connection succeded or not
      */
     public boolean connectApplication() {
 
         // If app is running for the first time, register its info and modes
         // to middleware
         if (isFirstTimeRun()) {
             return bindModeService();
         }
 
         return true;
     }
 
     /**
      * Disconnects a mode-supporting app from middleware
      * 
      * @return whether disconnection was okay
      */
     public boolean disconnectApplication() {
 
         releaseModeService();
         return true;
     }
 
     /**
      * Binding occurs asynchronously and bound-service methods cannot be called
      * immediately. We can only use bound service after receiving a callback
      * telling the connection has been established (onServiceConnected).
      */
     private void connectionCallback() {
 
         // Get supported modes from application
         ArrayList<ModeData> modes = null;
         try {
             modes = (ArrayList<ModeData>) modeService.getModes();
         } catch (RemoteException e) {
             Log.e(TAG, "Couldn't get mones from application");
             e.printStackTrace();
         }
 
         // Send to middleware basic info on app. Also, send its modes as
         // intent's extra data.
         Intent intent = new Intent();
 
         intent.setComponent(new ComponentName(
                 Constants.REGISTRY_PACKAGENAME,
                 Constants.REGISTRY_CLASSNAME));
         intent.setAction(Constants.ACTION_REGISTER_APP);
         intent.putExtra("uid", uid);
         intent.putExtra("name", appName);
 
         // Here's the deal: in order to request info from an app, the
         // middleware must send an intent. Implicit intents won't work,
         // since all apps supporting the intent will respond and we want to
         // communicate to a specific one. Explicit intents will work, but
         // need to be told its package and class destination. We register
         // them here so that the middleware can use them later.
         intent.putExtra("packageName", packageName);
        intent.putExtra("modeProxyClassName", packageName + "."
                 + Constants.MODE_PROXY_CLASS);
         Bundle bundle = new Bundle();
         bundle.putParcelableArrayList("modes", modes);
         intent.putExtras(bundle);
 
         context.sendBroadcast(intent);
         setFirstTimeRun(false);
     }
 
     /**
      * 
      * @return whether mode-supporting app is being run for the first time
      */
     private boolean isFirstTimeRun() {
 
         SharedPreferences prefs = context.getSharedPreferences(
                 PREFERENCES_FILE, Context.MODE_PRIVATE);
         return !prefs.getBoolean("RegisteredModes", false);
     }
 
     /**
      * Saves whether app has been run before to storage for later reference
      * 
      * @param value
      */
     private void setFirstTimeRun(boolean value) {
 
         SharedPreferences prefs = context.getSharedPreferences(
                 PREFERENCES_FILE, Context.MODE_PRIVATE);
         SharedPreferences.Editor ed = prefs.edit();
         ed.putBoolean("RegisteredModes", !value);
         ed.commit();
     }
 
     /**
      * Binds to application implementing mode service. Used by middleware.
      * 
      * @return whether binding worked or not
      **/
     private boolean bindModeService() {
 
         boolean ret = true;
 
         if (modeService == null) {
             modeConnection = new ModeServiceConnection();
             Intent intent = new Intent();
             intent.setComponent(new ComponentName(packageName, packageName
                     + "." + Constants.MODE_PROXY_CLASS));
             ret = context.bindService(intent, modeConnection,
                     Context.BIND_AUTO_CREATE);
             Log.d(TAG, "bindModeService() bound with " + ret);
         } else {
             Log.w(TAG, "Cannot bind to mode service - Service already bound");
         }
 
         return ret;
     }
 
     /**
      * Unbinds from app implementing mode service. Used by middleware.
      * 
      **/
     private void releaseModeService() {
 
         if (modeService != null) {
             modeService = null;
             context.unbindService(modeConnection);
             modeConnection = null;
             Log.d(TAG, "releaseModeService()");
         } else {
             Log.w(TAG, "Cannot unbind mode service - Service not bound");
         }
     }
 
     /**
      * Connection to application mode service
      * 
      * @author Marcelo Martins <martins@cs.brown.edu>
      * 
      */
     class ModeServiceConnection implements ServiceConnection {
 
         public void onServiceConnected(ComponentName name, IBinder boundService) {
 
             modeService = IModeService.Stub.asInterface((IBinder) boundService);
             Log.d(TAG, "onServiceConnected() connected");
             connectionCallback();
         }
 
         public void onServiceDisconnected(ComponentName name) {
 
             modeService = null;
             Log.d(TAG, "onServiceDisconnected() disconnected");
         }
     }
 }
