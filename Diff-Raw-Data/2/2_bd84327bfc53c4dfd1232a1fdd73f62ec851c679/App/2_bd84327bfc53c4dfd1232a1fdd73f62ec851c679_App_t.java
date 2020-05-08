 package jamzor.threegnotify;
 
 import android.app.Application;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 
 public class App extends Application {
 	public ICheckerService mCheckerService;
 	
 	/**
	 * Service Handler
 	 */
 	private ServiceConnection mServiceConnection = new ServiceConnection() {
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			// Get our interface...
 			mCheckerService = ICheckerService.Stub.asInterface(service);
 		}
 		
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			mCheckerService = null;
 		}
 	};
 	
 	@Override
 	public void onCreate() {
     	Intent i = new Intent(App.this, CheckerService.class);
         SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
         
         /*
          * Check if they just upgraded from < 2.0, if that's the
          * case we need to update our preferences, because types
          * changed.
          */
         try {
         	settings.getString("3g_acquired_vibrate", "none");
         } catch(ClassCastException ex) {
         	// If this catch fires, they are still using pre 2.0
         	// preferences, we need to convert them.
         	SharedPreferences.Editor editor = settings.edit();
         	
         	boolean mobile_acquired_vibrate = settings.getBoolean("3g_acquired_vibrate", false);
         	editor.remove("3g_acquired_vibrate");
         	editor.putString("3g_acquired_vibrate", mobile_acquired_vibrate ? "-" : "none");
         	
         	boolean mobile_lost_vibrate = settings.getBoolean("3g_lost_vibrate", false);
         	editor.remove("3g_lost_vibrate");
         	editor.putString("3g_lost_vibrate", mobile_lost_vibrate ? "-" : "none");
         	
         	boolean wifi_acquired_vibrate = settings.getBoolean("wifi_acquired_vibrate", false);
         	editor.remove("wifi_acquired_vibrate");
         	editor.putString("wifi_acquired_vibrate", wifi_acquired_vibrate ? "-" : "none");
         	
         	boolean wifi_lost_vibrate = settings.getBoolean("wifi_lost_vibrate", false);
         	editor.remove("wifi_lost_vibrate");
         	editor.putString("wifi_lost_vibrate", wifi_lost_vibrate ? "-" : "none");
         	
         	boolean roaming_vibrate = settings.getBoolean("roaming_vibrate", false);
         	editor.remove("roaming_vibrate");
         	editor.putString("roaming_vibrate", roaming_vibrate ? "-" : "none");
         	
         	boolean not_roaming_vibrate = settings.getBoolean("not_roaming_vibrate", false);
         	editor.remove("not_roaming_vibrate");
         	editor.putString("not_roaming_vibrate", not_roaming_vibrate ? "-" : "none");
         	
         	editor.commit();
         }
         
 
     	if(startService(i) == null) {
     	}
     	
     	if(bindService(i, mServiceConnection, 0)) {
     	}
 	}
 	
 	@Override
 	public void onTerminate() {
 		unbindService(mServiceConnection);
 	}
 }
