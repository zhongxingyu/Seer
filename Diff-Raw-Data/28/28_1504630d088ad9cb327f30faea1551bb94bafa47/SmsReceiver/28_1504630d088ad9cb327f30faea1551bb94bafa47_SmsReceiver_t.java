 package net.anei.cadpage;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
 
 public class SmsReceiver extends BroadcastReceiver {
 
   @Override
   public void onReceive(Context context, Intent intent) {
     if (Log.DEBUG) Log.v("SMSReceiver: onReceive()");
     intent.setClass(context, SmsReceiverService.class);
     intent.putExtra("result", getResultCode());
 
     /*
      * This service will process the activity and show the popup (+ play notifications)
      * after it's work is done the service will be stopped.
      */
    if (CadPageCall(context,intent)==true){
     SmsReceiverService.beginStartingService(context, intent);
    }
   }
  // We need to determine if this message is for us. If so then stop further alerting to the default sms app.
  public boolean CadPageCall(Context context,Intent intent){
	    Bundle bundle = intent.getExtras();
	    if (bundle != null) {
	      SmsMessage[] messages = SmsPopupUtils.getMessagesFromIntent(intent);
	      SmsMmsMessage message = new SmsMmsMessage(context, messages,System.currentTimeMillis() );
	      String strMessage = message.getMessageFull();
	      if (strMessage.contains("Call:")==true){
	    	  this.abortBroadcast();
	    	  return true;
	      } else if (strMessage.contains("TYPE:")==true){
	    	  this.abortBroadcast();
	    	  return true;
	      }
	    }
	    return false;
  }
}

