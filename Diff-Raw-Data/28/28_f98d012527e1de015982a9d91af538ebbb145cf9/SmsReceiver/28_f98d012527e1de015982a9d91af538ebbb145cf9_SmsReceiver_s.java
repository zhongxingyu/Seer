 /**
  * 
  */
 package com.aboveware.abovetracker;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.telephony.SmsMessage;
 
 /**
  * @author saa
  * 
  */
 public class SmsReceiver extends BroadcastReceiver {
 
 	private void HandleSecureSms(Context context, Intent intent) {
 		abortBroadcast();
 
 		intent.setClass(context, TrackService.class);
 		intent.putExtra(TrackService.IS_REMOTE_COMMAND, true);
 		context.startService(intent);
 	}
 
	// Onle handle the first received sms
 	private boolean IsRemoteCommand(Context context, SmsMessage[] smsMessages,
 	    Intent intent) {
 		if (smsMessages.length > 0) {
 			String messageBody = smsMessages[0].getMessageBody();
 			SmsParser smsParser = new SmsParser(messageBody);
 			if (smsParser.parse(Preferences.getTriggerKeyword(context))){
 				intent.putExtra(TrackService.REMOTE_COMMAND, messageBody);
 				intent.putExtra(TrackService.REMOTE_ORGINATOR, smsMessages[0].getOriginatingAddress());
 				return true;
 			}
 
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
 	 * android.content.Intent)
 	 */
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Object[] pduArray = (Object[]) intent.getExtras().get("pdus");
 		SmsMessage[] smsMessages = new SmsMessage[pduArray.length];
 		for (int i = 0; i < pduArray.length; i++) {
 			smsMessages[i] = SmsMessage.createFromPdu((byte[]) pduArray[i]);
 		}
 
 		Intent remoteIntent = new Intent();
 		if (IsRemoteCommand(context, smsMessages, remoteIntent)) {
 			HandleSecureSms(context, remoteIntent);
 		}
 	}
 }
