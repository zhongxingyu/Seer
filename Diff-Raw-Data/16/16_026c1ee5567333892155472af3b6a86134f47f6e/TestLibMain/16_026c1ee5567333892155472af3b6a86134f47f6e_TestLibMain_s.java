 package com.rucks.testlib;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 //import android.widget.Toast;
 
 
 import android.widget.Toast;
 
 import com.unity3d.player.UnityPlayer;
 import com.unity3d.player.UnityPlayerActivity;
 
 public class TestLibMain extends UnityPlayerActivity 
 {		
 	public static final String SMS_RECEIVED ="android.provider.Telephony.SMS_RECEIVED";
 	
 	public static final String COUNT_DIFFS = "CountDiffsFile";
 	public static final String SMS_SENT = "SMSSent";
 	public static final String SMS_GOTTEN = "SMSReceived";
 	private BroadcastReceiver receiverSMS;
 	private Intent SMSListenerServiceIntent;
 	
 	private boolean usingListeners;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         usingListeners = false; //starts false by default
     	Log.w("JavaPlugin", "onCreate");
     	
     	System.loadLibrary("javabridge");
         
         SMSListenerServiceIntent = new Intent(this, SMSListenerService.class);
         
     	receiverSMS = new BroadcastReceiver()
         {
     		@Override
             public void onReceive(Context context, Intent intent)
             {
                  if (intent.getAction().equals(SMS_RECEIVED))
                  {
                 	 String message = "1|0";
                 	 UnityPlayer.UnitySendMessage("PlatformBridge", "ReceiveNotification", message);
                  }
             }
         };//Java is magic.
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) 
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.test_lib_main, menu);
         return true;
     }
     
     @Override
     protected void onResume()
     {
     	super.onResume();
     	
     	String message = "";
     	
     	//pull out stored values
     	SharedPreferences countDiffs = getSharedPreferences(COUNT_DIFFS, 0);   	
     	long smsReceived = countDiffs.getLong(SMS_GOTTEN, 0);
     	long smsSent = countDiffs.getLong(SMS_SENT, 0);
     	
     	//build and send message that updates for all SMS transactions while the 
     	//app was not open
     	message = Long.toString(smsReceived) + "|" + Long.toString(smsSent);
     	UnityPlayer.UnitySendMessage("PlatformBridge", "ReceiveNotification", message);
     	
     	//reset stored values
     	SharedPreferences.Editor editor = countDiffs.edit();
     	editor.putLong(SMS_GOTTEN, 0);
     	editor.putLong(SMS_SENT, 0);
     	editor.commit();
     	
     	//Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
     	
    	//Should only need to track sms receiving during running application, because 
    	//if the person is playing our game they aren't sending texts, right?
        stopService(SMSListenerServiceIntent);
         if(usingListeners)
         {
            	//start BroadcastReceiver that runs when app is running. 
         	IntentFilter filter = new IntentFilter(SMS_RECEIVED);
             registerReceiver(receiverSMS, filter);
         }
     }
     
     @Override
     protected void onPause()
     {
     	super.onPause();
     	
    	//kill listeners that run when app is running, 
    	unregisterReceiver(receiverSMS);
    	
     	if(usingListeners)
     	{
         	//start listeners that run when app is closed.
             SMSListenerServiceIntent.setAction(SMSListenerService.ACTION);
             startService(SMSListenerServiceIntent);
     	}
     } 
     
     public void enableServiceListeners()
     {
     	usingListeners = true;
     	//start listener that runs while app is running
     	IntentFilter filter = new IntentFilter(SMS_RECEIVED);
     	registerReceiver(receiverSMS, filter);
 
         runOnUiThread(new Runnable()
         {
             public void run()
             {
             	Toast.makeText(getApplicationContext(), "Background Services have been enabled.", Toast.LENGTH_SHORT).show();
             }
         });  
     }
     
     public void disableServiceListeners()
     {
     	usingListeners = false;
     	//kill listeners that run when app is running, 
     	unregisterReceiver(receiverSMS);
     	
     	runOnUiThread(new Runnable()
         {
             public void run()
             {
             	Toast.makeText(getApplicationContext(), "Background services have been disabled.", Toast.LENGTH_SHORT).show();
             }
         }); 
     }
 }
