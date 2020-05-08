 package pl.orellana.telephonydemo;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.telephony.SmsManager;
 import android.telephony.SmsMessage;
 
 public class IncomingSMSReceiver extends BroadcastReceiver {
 	public static String SMS_REC_ACTION = "android.provider.Telephony.SMS_RECEIVED";
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		SharedPreferences sp = PreferenceManager
 				.getDefaultSharedPreferences(context);
 		if (sp.getBoolean("smsactivated", false)) {
 			if (intent.getAction().equals(SMS_REC_ACTION)) {
 
 				SmsManager sm = SmsManager.getDefault();
 				Bundle bundle = intent.getExtras();
 				if (bundle != null) {
 					Object[] pdus = (Object[]) bundle.get("pdus");
 					for (Object pdu : pdus) {
 						SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
 
						if (sms.getDisplayMessageBody().equals("REQUEST")) {
 							String text = "";
 
 							text += sp.getString("answertext", "") + "\n";
							if (sp.getBoolean("", true)) {
 								text += "Guillermo Orellana\n";
 							}
							if (sp.getBoolean("", true)) {
 								LocationManager lm = (LocationManager) context
 										.getSystemService(Context.LOCATION_SERVICE);
 								Location loc = lm
 										.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 								if (loc != null) {
 									text += "Last know location: lat:"
 											+ loc.getLatitude() + ",lon:"
 											+ loc.getLongitude()
 											+ "\nAccuracy: "
 											+ loc.getAccuracy();
 								}
 							}
 							sm.sendTextMessage(
 									sms.getDisplayOriginatingAddress(), null,
 									text, null, null);
 
 							this.abortBroadcast();
 						}
 
 					}
 				}
 			}
 		}
 	}
 }
