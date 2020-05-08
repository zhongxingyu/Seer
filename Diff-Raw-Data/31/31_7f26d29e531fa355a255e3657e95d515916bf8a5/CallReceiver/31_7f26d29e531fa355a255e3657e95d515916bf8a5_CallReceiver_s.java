 package edu.ucsb.ece251.charlesmunger.roomdetector;
 
 import roboguice.receiver.RoboBroadcastReceiver;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 public class CallReceiver extends RoboBroadcastReceiver {
 	private String TAG = "CallReceiver";
 	@Override
 	public void handleReceive(Context context, Intent intent) {
 		Log.d("CallReceiver", "call received");
 		
 		Intent i = new Intent(context, CallSilencerService.class);
 		
 		switch(intent.getIntExtra(TelephonyManager.EXTRA_STATE, -1)) {
 		case TelephonyManager.CALL_STATE_IDLE: break;
 		case TelephonyManager.CALL_STATE_RINGING: 
 			final PendingIntent temp = PendingIntent.getBroadcast(context, 0, i, 0);
 			i = new Intent(context, InRoomService.class);
 			i.putExtra(InRoomService.PENDING_INTENT, temp);
 			break;
 		case TelephonyManager.CALL_STATE_OFFHOOK: break;
		default: Log.wtf(TAG, "Unexpected state");
 		}
 		context.startService(i);
 	}
 }
