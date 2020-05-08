 package com.veenvliet.seeyousoon;
 
 import android.content.Context;
 import android.media.MediaPlayer;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.widget.Toast;
 
 public class Sms {
 
 	public static Boolean Send(final String phoneNumber, final String message,
 			final Boolean sound, final Context appContext) {
 		try {
 			Log.d("SMS.Send", "phoneNumber = " + phoneNumber + " message = "
 					+ message);
 			SmsManager smsManager = SmsManager.getDefault();
 			smsManager.sendTextMessage(phoneNumber, null, message, null, null);
 
 			if (null != appContext) {
 				if (sound) {
 					// play confirmation sound
 					MediaPlayer sound1 = MediaPlayer.create(appContext,
 							R.raw.bellsoundring); // res/raw/bellsoundring.wav
 					sound1.start();
 				}
 				Toast.makeText(
 						appContext,
 						appContext.getResources().getString(
 								R.string.textMessageSentSuccessfully)
 								+ message, Toast.LENGTH_SHORT).show();
 				return true;
 			}
 
 		} catch (IllegalArgumentException ex) {
 			Toast.makeText(
 					appContext,
 					appContext.getResources().getString(
							R.string.textMessageNotSent)
							+ ex.toString(), Toast.LENGTH_SHORT).show();
 			return false;
 		}
 		return false;
 	}
 
 }
