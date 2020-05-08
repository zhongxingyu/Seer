 package org.tomhume.fbcall;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 public class PhoneCallReporter extends BroadcastReceiver {
 
 	private static final String TAG = "PhoneCallReporter";
 	
 	private static int lastCallState[] = {TelephonyManager.CALL_STATE_IDLE,TelephonyManager.CALL_STATE_IDLE};
 
 	@Override
 	public void onReceive(Context con, Intent intent) {
 
 		/* If a call has just ended, get the number of it */
 
 		TelephonyManager telephony = (TelephonyManager) con.getSystemService(Context.TELEPHONY_SERVICE); 
 
 		int currentState = telephony.getCallState();
 		Log.d(TAG, "onReceive() lastState=" + lastCallState + ",currentState=" + currentState);
 
 		/* If we've just finished a phone call we initiated, log it...
 		 * IDLE -> OFFHOOK -> IDLE = we made the call
 		 * IDLE -> RINGING -> OFFHOOK -> IDLE = we received the call
 		 */
 		
 		if ((lastCallState[0]==TelephonyManager.CALL_STATE_IDLE)
 			&& (lastCallState[1]==TelephonyManager.CALL_STATE_OFFHOOK)
 			&& (currentState==TelephonyManager.CALL_STATE_IDLE)) {
 			Log.d(TAG, "just finished a call we made, logging");
 			SharedPreferences prefs = con.getSharedPreferences(FacebookCallLoggerActivity.PREFS_NAME, Context.MODE_PRIVATE);
 			if (prefs.getBoolean("active", false)) {
 				String num = android.provider.CallLog.Calls.getLastOutgoingCall(con);
 				Log.d(TAG, "Save details for " + num);
 				Intent i = new Intent(con, LoggingService.class);
 				i.putExtra("msisdn", num);
 				con.startService(i);
 			}
 		} else 
 			Log.d(TAG, "no need to log action");
 
 		lastCallState[0] = lastCallState[1];
 		lastCallState[1] = currentState;
 	}
 
 }
