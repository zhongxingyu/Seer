 package giulio.frasca.silencesched;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 public class ToggleServiceActivity extends Activity {
 
 	private static final int HELLO_ID = 1;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		toggleService();
 		finish();
 	}
 	
 	private void toggleService() {
 		
 			boolean serviceRunning=false;
 			ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
 			List<ActivityManager.RunningServiceInfo> serviceList = am.getRunningServices(Integer.MAX_VALUE);
 			if (!(serviceList.size()>0)){
 				serviceRunning=false;
 			}
 			for ( int i=0;i<serviceList.size();i++){
 				RunningServiceInfo serviceInfo = serviceList.get(i);
 				ComponentName serviceName = serviceInfo.service;
 				if (serviceName.getClassName().equals("giulio.frasca.silencesched.BackroundService")){
 					serviceRunning = true;
 				}
 			}
 			
 
 			if (!serviceRunning){
 				serviceRunning=true;
 				startService(new Intent(BackroundService.class.getName()));
 				
 			}
 			else{
 				serviceRunning=false;
 				stopService(new Intent(BackroundService.class.getName()));
 				createResumeNotification();
 			}
 	}
 
 	private void createResumeNotification() {
 		String ns = Context.NOTIFICATION_SERVICE;
     	NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
 
     	int icon = R.drawable.iconblackwhite;
     	CharSequence tickerText = "Service Paused";
     	long when = System.currentTimeMillis();
     	
     	Notification notification = new Notification(icon, tickerText, when);
     	
     	Context context = getApplicationContext();
     	CharSequence contentTitle = "Silence Paused";
     	
 
     	CharSequence contentText="Click To Resume";
     	Intent notificationIntent = new Intent(this, ToggleServiceActivity.class);
     	PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, notificationIntent, 0);
 
     	notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
     	notification.flags |= Notification.FLAG_ONGOING_EVENT;
     	notification.flags |= Notification.FLAG_NO_CLEAR;
     	
     	mNotificationManager.notify(HELLO_ID, notification);
 		
 	}
     public void logcatPrint(String message){
     	Log.v("customdebug",message + " | sent from " +this.getClass().getSimpleName());
     }
 }
