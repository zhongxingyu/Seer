 package uk.co.jarofgreen.cityoutdoors.Service;
 
 
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.preference.PreferenceManager;
 import android.util.Log;
 /**
  * 
  * @author James Baster  <james@jarofgreen.co.uk>
  * @copyright City of Edinburgh Council & James Baster
  * @license Open Source under the 3-clause BSD License
  * @url https://github.com/City-Outdoors/City-Outdoors-Android
  */
 public class LoadDataIfStaleService  extends IntentService {
 
 	public LoadDataIfStaleService() {
 		super("LoadDataIfStaleService");
 	}
 
 	protected void onHandleIntent(Intent intent) {	
 
 		long now = java.lang.System.currentTimeMillis();
 
 		SharedPreferences settings=PreferenceManager.getDefaultSharedPreferences(this);
 		long last = settings.getLong("lastDataUpdate", 0);
 
 		boolean updateNeeded = false;
 		
 		if (last == 0) {
 			updateNeeded = true;
 			Log.d("UPDATE","Yes, never updated before");			
 		} else if (now > last) {
 			// if last updated more than a certain time ago
 			Log.d("UPDATEDATENOW",Long.toString(now));
 			Log.d("UPDATEDATELAST",Long.toString(last));
 			Log.d("UPDATEDATEMINUS",Long.toString(now - last));
 			ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 			long interval;
 			if (mWifi.isConnected()) {
 				Log.d("UPDATE","WiFi Connected so 1 week");
 				interval = 1000*60*60*24*7;
 			} else {
 				Log.d("UPDATE","No WiFi so 1 month");
				interval = 1000*60*60*24*7;
 			}
 			
 			if ((now - last) > interval) {
 				updateNeeded = true;
 				Log.d("UPDATE","Yes, to old");
 			}        	
 		} else {
 			// this is just a catch; if last > now someone has been messing with system clock and we should definetly update.
 			updateNeeded = true;
 			Log.d("UPDATE","Yes, clock has run backwards");
 		}
 		
 		if (updateNeeded) {
 			startService(new Intent(this, LoadDataService.class));
 		}
 		
 	}
 	
 	
 }
