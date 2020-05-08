 package com.ting.androidexample.ui.activities;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
import android.net.Uri;
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
 		
		Intent intent = new Intent(SENT_SMS_ACTION);
		intent.putExtra("uri", "content://intent/test");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		smsManager.sendTextMessage("13811200985", null, "test", pendingIntent, null);
 		
		Intent intent1 = new Intent(SENT_SMS_ACTION);
		intent.putExtra("uri", "content://intent/testtest");
		PendingIntent pendingIntent1 = PendingIntent.getBroadcast(this, 0, intent1, 0);
		smsManager.sendTextMessage("13811200985", null, "testtest", pendingIntent1, null);
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
