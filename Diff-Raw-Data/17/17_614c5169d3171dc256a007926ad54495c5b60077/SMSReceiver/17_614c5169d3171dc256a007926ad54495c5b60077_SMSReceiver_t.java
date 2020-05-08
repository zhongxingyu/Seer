 package com.example.smsbeacon;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.telephony.SmsMessage;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 
 import com.example.smsbeacon.RingActivity.Action;
 
 public class SMSReceiver extends BroadcastReceiver {
 
 	private final String TAG = this.getClass().getSimpleName();
 	 
 	
 	@Override
 	/**
 	 * Called when a SMS is received and check the content of the SMS for a special code.
 	 * If the code is found, it start a new Intent. 
 	 */
 	public void onReceive(Context context, Intent intent)
 	{
 		Log.i(TAG, "action received : " + intent.getAction());
 		if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
 			smsReceived(context, intent);
 		else if (intent.getAction().equals("android.intent.action.PHONE_STATE"))
 			callReceived(context, intent);
 	}
 
 
 	/**
 	 * A call is received and need to be filtered out to see if it comes from the specific 
 	 * registered phone number.
 	 *  
 	 * @param context
 	 * @param intent
 	 */
 	private void callReceived(Context context, Intent intent) {
 		Log.i(TAG, "PHONE_STATE action received");
 		Bundle extra = intent.getExtras();
 			
 		if(extra != null) {
 			String newState = extra.getString(TelephonyManager.EXTRA_STATE);
 			if(newState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
 				String phoneNumber = extra.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
 				Log.i(TAG, "Phone ringing, a call from '" + phoneNumber + "'");
 				if(phoneNumber.equals(getRingPhoneNumber(context))) {
 					specificCallerDetected(context);
 				}
 				
 			}
 		}
 	
 	}
 
 
 	/**
 	 * A SMS has been received, and we need to see if it match the theft or the lost SMS code
 	 * @param context
 	 * @param intent
 	 */
 	private void smsReceived(Context context, Intent intent) {
 		Log.i(TAG, "SMS_RECEIVED action received");
 		Bundle extras = intent.getExtras();
 
 		if ( extras != null ) {
 			// get the SMS data from the "pdus" key. Can hold multiple messages (multi-part ?)  
 			Object[] smsextras = (Object[]) extras.get( "pdus" );
 
 			for ( Object o : smsextras) {
 				SmsMessage smsmsg = SmsMessage.createFromPdu((byte[])o);
 				String strMsgBody = smsmsg.getMessageBody();
 				if(strMsgBody.equals(getTriggerRingToneCodeSMS(context))) {
 					lostCodeDetected(context, smsmsg.getOriginatingAddress(), Action.RINGTONE);
 					break;
 				} else if(strMsgBody.equals(getTriggerLocationCodeSMS(context))) {
 					lostCodeDetected(context, smsmsg.getOriginatingAddress(), Action.SMS_LOCATION);
 					break;
 				}
 			}
 
 		}
 	}
 
 
 	private Object getRingPhoneNumber(Context c) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
 		return preferences.getString(c.getString(R.string.pref_prio_key_specific_caller), "");
 	}
 
 	private String getTriggerRingToneCodeSMS(Context c) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
 		return preferences.getString(c.getString(R.string.pref_home_key_pwd), "");
 	}
 
 	private String getTriggerLocationCodeSMS(Context c) {
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
 		return preferences.getString(c.getString(R.string.pref_out_key_passwd), "");
 	}
 
 	private void specificCallerDetected(Context context) {
 		Log.i(TAG, "Ringing volume is set to its maximum");		
 		RingToneHandler ring = new RingToneHandler(context);
 		ring.SetMaxVolume();
 	}
 	
 	private void  lostCodeDetected(Context context, String phoneNumber, Action act) {
 		Log.i(TAG, "SMS lost code trigger detected");
		//abortBroadcast(); // Do not dispatch the SMS to anybody else
 		Intent newintent = new Intent(context, RingActivity.class);
 		newintent.putExtra(RingActivity.DATA_EXTRA_CALLER, phoneNumber);
 		newintent.putExtra(RingActivity.DATA_EXTRA_ACTION, act);
 		newintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		context.startActivity(newintent);		
 	}
 
 }
