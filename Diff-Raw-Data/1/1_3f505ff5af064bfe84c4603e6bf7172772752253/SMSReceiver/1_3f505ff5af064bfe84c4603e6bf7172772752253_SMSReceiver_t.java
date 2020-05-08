 package org.sudaraka.senderblock.system;
 
 import org.sudaraka.senderblock.R;
 import org.sudaraka.senderblock.data.BlockedSendersSMS;
 import org.sudaraka.senderblock.data.MessagesSMS;
 import org.sudaraka.senderblock.ui.MessageFilterStatActivity;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NotificationCompat;
 import android.telephony.SmsMessage;
 
 
 public class SMSReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Intent i = new Intent(context, MessageFilterStatActivity.class);
 		PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
 
 		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 
 		Bundle b = intent.getExtras();
 		if(null != b) {
 			Object[] pdus = (Object[]) b.get("pdus");
 			SmsMessage[] msg_list = new SmsMessage[pdus.length];
 			MessagesSMS stat = new MessagesSMS(context);
 			BlockedSendersSMS blocked = new BlockedSendersSMS(context);
 
 			for(int idx = 0; idx < msg_list.length; idx++) {
 				msg_list[idx] = SmsMessage.createFromPdu((byte[]) pdus[idx]);
 				
 				String sender = msg_list[idx].getOriginatingAddress();
 
 				stat.add(sender, (int) msg_list[idx].getTimestampMillis() / 1000);
 				
 				if(blocked.exists(sender)) {
 					abortBroadcast();
 					
 					Notification notification = new NotificationCompat.Builder(context)
 						.setContentTitle("SMS blocked from " + sender)
 						.setContentText("Touch to see the blocked list.")
 						.setSmallIcon(R.drawable.blocked)
 						.setContentIntent(pi)
 						.build()
 						;
 					notification.flags |= Notification.FLAG_AUTO_CANCEL;
 					
 					nm.notify(0, notification);
 				}
 			}
 		}
 	}
 
 }
