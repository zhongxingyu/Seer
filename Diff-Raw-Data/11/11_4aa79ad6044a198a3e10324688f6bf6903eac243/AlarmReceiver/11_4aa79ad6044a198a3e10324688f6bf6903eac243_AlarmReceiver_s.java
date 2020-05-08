 package com.matze5800.paupdater;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.TaskStackBuilder;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.text.format.Time;
 import android.util.Log;
 
 public class AlarmReceiver extends BroadcastReceiver {
 private final String SOMEACTION = "com.matze5800.paupdater.ACTION";
 Context Context;
 int localVer;
 @Override
 public void onReceive(Context context, Intent intent) {
 	Context = context.getApplicationContext();
     Time now = new Time();
     now.setToNow();
     
     String action = intent.getAction();
     if(SOMEACTION.equals(action)) {
     	Log.i("AlarmReceiver", "Reiceived Alarm");
     	
         GetGooVersion GooTask = new GetGooVersion();
     	GooTask.execute();
     }
 }
 
     class GetGooVersion extends AsyncTask<Void, Void, String> {  
         @Override
         protected String doInBackground(Void... noargs) {
         	return Functions.CheckGoo(Context);
         }
         
         @Override
         protected void onPostExecute(String result) {
        	if (result == null) {
         		Log.e("AlarmReceiver", "Unable to check for updates! No connection or goo down!");
         	}else{
         	int gooVer = Integer.valueOf(result);
         	Log.i("AlarmReceiver", "gooVer: "+gooVer);
         	try { //Get local ro.goo.version
     			Process proc;
     			proc = Runtime.getRuntime().exec(new String[]{"/system/bin/getprop", "ro.goo.version"});
     	        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
     	        
     	        String resultstring = reader.readLine();
     	        
     	        if(resultstring.equals("") || resultstring.equals(null))	{
     	        	Log.i("Local Parser", "Not running on PA!!!");
     	        	localVer = gooVer + 1;
     	        } else {
     	        	localVer = Integer.valueOf(resultstring);
     	        }
     	        Log.i("AlarmReceiver", "localVer: "+localVer);
     		} catch (IOException e) {
     			Log.e("Local Parser", "Error parsing local version!");
     			Log.e("Local Parser", e.toString());
     		}
        	//localVer = 1; //debug
         	if (gooVer > localVer)	{
         		Log.i("AlarmReceiver", "Newer Version available! Show Notification!");
         		
         		Notification.Builder nBuilder;
             	nBuilder = new Notification.Builder(Context);
 
             	Intent resultIntent = new Intent(Context, MainActivity.class);
             	resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
             	TaskStackBuilder stackBuilder = TaskStackBuilder.create(Context);
 
             	stackBuilder.addParentStack(MainActivity.class);
             	
             	stackBuilder.addNextIntent(resultIntent);
             	PendingIntent resultPendingIntent =
             	        stackBuilder.getPendingIntent(
             	            0,
             	            PendingIntent.FLAG_UPDATE_CURRENT
             	        );
             	nBuilder.setContentIntent(resultPendingIntent);
             	nBuilder.setSmallIcon(R.drawable.ic_launcher);
             	nBuilder.setContentTitle("PA Updater");
             	nBuilder.setContentText("New Update available!");
             	nBuilder.setAutoCancel(true);
             	nBuilder.setOngoing(false);
             	NotificationManager mNotificationManager =
             		    (NotificationManager) Context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
             	mNotificationManager.notify(1, nBuilder.build());
         	}
         }
         }
     }
     
 }
