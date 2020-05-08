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
 package de.mangelow.slideitloud;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.media.AudioManager;
 import android.os.IBinder;
 import android.provider.Settings;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.KeyEvent;
 
 
 public class Service extends android.app.Service {
 
 	private final String TAG = "SIL";
 	private final boolean D = false;
 
 	private final String BCAST_CONFIGCHANGED = "android.intent.action.CONFIGURATION_CHANGED";
 	String stateString = "";
 
 	private Helper mHelper = new Helper();
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		//if(D)Log.d(TAG, "onBind()");
 
 		return null;
 	}	
 	@Override
 	public void onCreate() {
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(BCAST_CONFIGCHANGED);
 		this.registerReceiver(mBroadcastReceiver, filter);
 		if(D)Log.d(TAG, "Service started");
 	}
 	@Override
 	public void onDestroy() {
 		this.unregisterReceiver(mBroadcastReceiver);
 		if(D)Log.d(TAG, "Service stopped");
 	}
 	private void answerCallWithHeadsethook(Context context) {
 		if(D)Log.d(TAG, "answerCallWithHeadsethook()");
 
 		Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);               
 		buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
 		context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
 	}	
 	private void hangUpWithAirPlaneMode(Context context) {
 		if(D)Log.d(TAG, "hangUpWithAirPlanceMode()");
 
 		// Enable Airplane Mode
 		Settings.System.putInt(
 				getContentResolver(),
 				Settings.System.AIRPLANE_MODE_ON, 1);
 
 		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
 		intent.putExtra("state", 1);
 		context.sendBroadcast(intent);
 		
 		// Sleep one second
 		try {
 			Thread.sleep(1000);
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 
 		// Disable Airplane Mode
 		Settings.System.putInt(
 				getContentResolver(),
 				Settings.System.AIRPLANE_MODE_ON, 0);
 		intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
 		intent.putExtra("state", 0);
 		context.sendBroadcast(intent);
 
 	}
 	public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent myIntent) {
 			//if(D)Log.d(TAG, "onReceive()");
 
 			if (myIntent.getAction().equals( BCAST_CONFIGCHANGED ) ) {
 
 				TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
 				int state = tm.getCallState();
 				if(D)Log.d(TAG, "getCallState: "+state);	
 				if(state<TelephonyManager.CALL_STATE_RINGING)return;
 
 				Configuration configuration = getResources().getConfiguration();
 				String config_string = configuration.toString();				
 				boolean closed = false;
				if(config_string.contains("keys=2/1/2"))closed=true;
 				if(D)Log.d(TAG, "closed: "+closed);
 
 				AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
 				audioManager.setMode(AudioManager.MODE_IN_CALL);
 
 				if(closed) {	
 					
 					audioManager.setSpeakerphoneOn(false);		
 					if(D)Log.d(TAG, "Loud speaker off");
 					
 					boolean autohangup = mHelper.loadBooleanPref(context, "autohangup", mHelper.AUTOHANGUP);
 					if(autohangup&&state==TelephonyManager.CALL_STATE_OFFHOOK) {
 						if(D)Log.d(TAG, "AutoHangup");
 						hangUpWithAirPlaneMode(context);
 						return;
 					}	
 				}
 				else {
 					audioManager.setSpeakerphoneOn(true);
 					if(D)Log.d(TAG, "Loud speaker on");
 
 					boolean autoanswer = mHelper.loadBooleanPref(context, "autoanswer", mHelper.AUTOANSWER);
 					if(autoanswer&&state==TelephonyManager.CALL_STATE_RINGING) {
 						if(D)Log.d(TAG, "AutoAnswer");
 						answerCallWithHeadsethook(context);
 					}
 				}
 			}
 		}
 	};
 }
