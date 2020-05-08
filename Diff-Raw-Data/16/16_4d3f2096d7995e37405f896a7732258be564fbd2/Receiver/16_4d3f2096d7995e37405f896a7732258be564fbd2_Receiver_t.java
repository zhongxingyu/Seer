 /***
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may
  * not use this file except in compliance with the License. You may obtain
  * a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  */
 package de.mangelow.syncwifi;
 
 import de.mangelow.syncwifi.Helper;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.util.Log;
 
 public class Receiver extends BroadcastReceiver {
 
 	private final String TAG = "SW";
	private final boolean D = false;
 
 	private Helper mHelper = new Helper();
 
 	public static final String ACTION_PERIODIC_SYNC = "ACTION_PERIODIC_SYNC";
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 
 		String action = intent.getAction();
 
 		boolean enabled = mHelper.loadBooleanPref(context, "enabled", mHelper.SYNC);
 		if(enabled) {
 			if(action.equals(Intent.ACTION_BOOT_COMPLETED)) {
 				if(D)Log.d(TAG, "ACTION_BOOT_COMPLETED");		
 				check(context);			
 			}
 			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
 				check(context);			
 			}
 			if (action.equals(ACTION_PERIODIC_SYNC)) {
 				if(D)Log.d(TAG, "ACTION_PERIODIC_SYNC");
 				// TODO
 			}
 		}
 	}
 	private void check(Context context) {
 
 		long current_timestamp = System.currentTimeMillis();
 		long timestamp = mHelper.loadLongPref(context, "timestamp", 0);
 
 		if(mHelper.isConnected(context, mHelper.CELL)) {
 			if((timestamp+3000<current_timestamp)||timestamp==0) {
 				if(D)Log.d(TAG, "Connected to CELL");
 				mHelper.saveLongPref(context, "timestamp", current_timestamp);
 
 				mHelper.setAccounts(context, mHelper.CELL, true);
 
 			}
 		}
 
 		if(mHelper.isConnected(context, mHelper.WIFI)) {
 			if((timestamp+3000<current_timestamp||timestamp==0)) {
 				if(D)Log.d(TAG, "Connected to WIFI");
 				mHelper.saveLongPref(context, "timestamp", current_timestamp);
 
 				mHelper.setAccounts(context, mHelper.WIFI, true);
 
 			}
 		}
 	}
 
 }
