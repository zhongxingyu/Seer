 package com.mddev.l8text;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.telephony.SmsManager;
 
 public class SmsService extends Service {
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		OutgoingText text = null;
 
 		text = OutgoingText.fromIntent(intent);
 		TextDbAdapter db = new TextDbAdapter(this);
 
 		db.open();
 		db.removeEntry(db.getEntryRow(text));
 		db.close();
 
 		SmsManager sms = SmsManager.getDefault();
 
 		for (String recipient : text.getRecipient().split(",")) {
			sms.sendTextMessage(recipient, null, text.getMessageContent(), null, null);
 		}
		// sms.sendTextMessage(text.getRecipient(), null, text.getMessageContent(),
		// null, null);

 		return Service.START_NOT_STICKY;
 
 	}
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 }
