 package com.hn.linky.utilities;
 
 import com.hn.linky.LinkyIntentService;
 import com.hn.linky.valueobjects.Constants;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.telephony.SmsMessage;
 import android.util.Log;
 
 public class SmsReceiver extends BroadcastReceiver
 {	
 	private static final String TAG = "SMS_RECEIVER";
 	
     @Override
     public void onReceive(Context context, Intent intent)
     {
     	Log.i(TAG, "Intent recieved: " + intent.getAction());
     	
     	//Get the SMS message passed in
         Bundle bundle = intent.getExtras();        
         SmsMessage[] messages = null;
         String message = null;
         String origin = null;
         
         if (bundle != null)
         {
             //Retrieve the SMS message received
             Object[] pdus = (Object[]) bundle.get("pdus");
             messages = new SmsMessage[pdus.length];     
             
             for (int i = 0; i < messages.length; i++)
             {
                 messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                 message = messages[i].getMessageBody().toString();
                 origin = messages[i].getOriginatingAddress();
                                   
                 if (message.startsWith(Constants.LINKY_KEY))
                 { 
                     abortBroadcast();
                     
                     int originStart = message.indexOf("(") + 1;
                    int originEnd = message.indexOf(")") - 1;
                     origin = message.substring(originStart, originEnd);
                     
                    message = message.substring(originEnd + 2, message.length());
                     
                     Intent insertIntent = new Intent(context, LinkyIntentService.class);
                     insertIntent.setAction(Constants.ACTION_INSERT_SMS);
                     insertIntent.putExtra(Constants.EXTRA_SMS_MESSAGE, message);
                     insertIntent.putExtra(Constants.EXTRA_ORIGINATING_ADDRESS, origin);
                     context.startService(insertIntent);  
                 } 
                 else 
                 {
                     Intent forwardIntent = new Intent(context, LinkyIntentService.class);
                     forwardIntent.setAction(Constants.ACTION_FORWARD_SMS);
                     forwardIntent.putExtra(Constants.EXTRA_SMS_MESSAGE, message);
                     forwardIntent.putExtra(Constants.EXTRA_ORIGINATING_ADDRESS, origin);
                     context.startService(forwardIntent);
                 }
                    
 //                if (message.contains(Constants.BUZZ_KEY))
 //                {
 //                	Log.i(TAG, "Sms message contains: " + Constants.BUZZ_KEY);
 //
 //                	//TODO implement linky buzz counter so we can safely abort broadcast 
 //                	//abortBroadcast();
 //                	
 //                	origin = messages[i].getOriginatingAddress();
 //                	
 //                	Intent authenticateIntent = new Intent(context, LinkyIntentService.class);
 //                	authenticateIntent.setAction(Constants.ACTION_AUTHENTICATE_BUZZ);
 //                	authenticateIntent.putExtra(Constants.EXTRA_SMS_MESSAGE, message);
 //                	authenticateIntent.putExtra(Constants.EXTRA_ORIGINATING_ADDRESS, origin);
 //                	context.startService(authenticateIntent);
 //                }       
             }
         }
     }
 }
