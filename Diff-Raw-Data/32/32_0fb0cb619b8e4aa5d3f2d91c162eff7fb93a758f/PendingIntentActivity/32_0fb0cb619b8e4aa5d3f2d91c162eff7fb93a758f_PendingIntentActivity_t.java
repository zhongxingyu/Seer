 package com.ting.androidexample.ui.activities;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.telephony.SmsManager;
 import android.util.Log;
 
 public class PendingIntentActivity extends Activity {
 	
 	private static final String TAG = "PendingIntentActivity";
 	
 	//获得发送报告
     private static final String SENT_SMS_ACTION = "com.baidu.sumeru.SENT_SMS_ACTION";
     //获得接收报告
     private static final String DELIVERY_SMS_ACTION = "com.baidu.sumeru.DELIVERY_SMS_ACTION";
 	//接收短信
 	private static final String RECEIVE_SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";
 	
 	private BroadcastReceiver mSmsReceiver = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		createReceiver(this);
 		
 		SmsManager smsManager = SmsManager.getDefault();
				
		Intent intent1 = new Intent(SENT_SMS_ACTION);
		intent1.putExtra("uri", "content://intent/intent1");
		PendingIntent pendingIntent1 = PendingIntent.getBroadcast(this, intent1.hashCode(), intent1, PendingIntent.FLAG_UPDATE_CURRENT);
 		
		Intent intent2 = new Intent(DELIVERY_SMS_ACTION);
		intent2.putExtra("uri", "content://intent/intent2");
		PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, intent2.hashCode(), intent2, PendingIntent.FLAG_UPDATE_CURRENT);
 		
		smsManager.sendTextMessage("13811223985", null, "12", pendingIntent1, pendingIntent2);

		
		Intent intent3 = new Intent(SENT_SMS_ACTION);
		intent3.putExtra("uri", "content://intent/intent3");
		PendingIntent pendingIntent3 = PendingIntent.getBroadcast(this, intent3.hashCode(), intent3, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent intent4 = new Intent(DELIVERY_SMS_ACTION);
		intent4.putExtra("uri", "content://intent/intent4");
		PendingIntent pendingIntent4 = PendingIntent.getBroadcast(this, intent4.hashCode(), intent4, PendingIntent.FLAG_UPDATE_CURRENT);
		
		smsManager.sendTextMessage("13811223985", null, "34", pendingIntent3, pendingIntent4);

 	}
 
 	private void createReceiver(Context context) {
 		IntentFilter intentFilter = new IntentFilter();
         intentFilter.addAction(SENT_SMS_ACTION);
         intentFilter.addAction(DELIVERY_SMS_ACTION);
         intentFilter.addAction(RECEIVE_SMS_ACTION);
         intentFilter.setPriority(1000);
 
 	    mSmsReceiver = new BroadcastReceiver() {
 
 
 
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				String action = intent.getAction();
 				Log.d(TAG, "action:" + action);
 				
 				if(action.equals(SENT_SMS_ACTION)) {
 					//接收到发送报告
 					Log.d(TAG, "sms is sent by " + intent.getStringExtra("uri")); 	
 					
 				} else if (action.equals(DELIVERY_SMS_ACTION)) {
 					//表示对方成功收到短信     
 					Log.d(TAG, "sms is deliveried by " + intent.getStringExtra("uri")); 	
 					
 				} else if (action.equals(RECEIVE_SMS_ACTION)) {
 					//接收到短信后处理
 					Log.d(TAG, "receive sms!");
 					
 				}
 			}   
     	};
     	
     	context.registerReceiver(mSmsReceiver, intentFilter);	 
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		unregisterReceiver(mSmsReceiver);
 	}
 	
 }
