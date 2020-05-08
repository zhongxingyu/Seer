 package jp.muo.smsproxy;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.telephony.SmsMessage;
 
 public class SmsReceiver extends BroadcastReceiver {
 	public static final String TAG = "smsProxy";
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		// Log.d(TAG, "received");
 		SmsProxyManager mgr = new SmsProxyManager(context);
         mgr.setType(SmsProxyManager.Mode.SMS);
 		Bundle bundle = intent.getExtras();
         SmsMessage[] msgs = null;
         if (bundle != null && mgr.isEnabled() && !mgr.getProxyTo().equals("")) {
         	String msgText = "";
             Object[] pdus = (Object[]) bundle.get("pdus");
             if (pdus.length == 0) {
             	return;
             }
         	// Log.d(TAG, Integer.toString(pdus.length) + " messages found");
             msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++){
                 msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                 msgText += "fwd " + msgs[i].getOriginatingAddress() + ":\n";
                 msgText += msgs[i].getMessageBody().toString() + "\n";
             }
         	// Log.d(TAG, "msg: " + msgText);
             mgr.send(msgText);
         }
 	}
 }
