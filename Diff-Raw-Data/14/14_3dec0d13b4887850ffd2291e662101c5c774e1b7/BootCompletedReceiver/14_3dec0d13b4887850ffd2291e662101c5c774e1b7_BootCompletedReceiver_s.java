 package com.abbyy.cloudocr;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 
 public class BootCompletedReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 		Intent service = new Intent(context, TasksManagerService.class);
 		context.startService(service);
 	}
 
 }
