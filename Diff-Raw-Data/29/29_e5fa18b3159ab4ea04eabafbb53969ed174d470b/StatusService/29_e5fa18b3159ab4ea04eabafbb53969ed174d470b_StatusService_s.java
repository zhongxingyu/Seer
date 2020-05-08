 package com.pspace.gr;
 
 import android.app.ActivityManager;
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.http.AndroidHttpClient;
 import android.os.Build;
 import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
 import android.util.Log;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.util.EntityUtils;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * Created by tsagi on 9/3/13.
  */
 public class StatusService extends IntentService {
 
     public static final String ACTION_MyIntentService = "com.pspace.gr.RESPONSE";
     public static final String ACTION_MyUpdate = "com.pspace.gr.UPDATE";
     public static final String EXTRA_KEY_OUT = "EXTRA_OUT";
     public static final String EXTRA_KEY_UPDATE = "EXTRA_UPDATE";
 
     int status = -1;
     int notifyid = -1;
     NotificationManager mNM;
 
     boolean notificationState;
     boolean notificationSelect;
     boolean testMenu;
 
     int delay = 0;
     int period = 5000;
 
     public StatusService() {
         super("StatusService");
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         mNM = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         Timer timer = new Timer();
         timer.scheduleAtFixedRate(new TimerTask() {
 
             public void run() {
                 checkStatus();
             }
 
         }, delay, period);
     }
 
     public void checkStatus(){
         loadPref();
         AndroidHttpClient client = AndroidHttpClient
                 .newInstance("pspace_android");
         HttpGet request;
         request = new HttpGet(getResources().getString(R.string.pspaceurl));
 
         HttpResponse response = null;
 
         try {
 
             response = client.execute(request);
             client.close();
 
             switch (response.getStatusLine().getStatusCode()) {
 
                 case 200:
                     int oldStatus = status;
                     status =  Integer.valueOf(EntityUtils.toString(response
                             .getEntity()));
                     Log.d("status", Integer.toString(status));
                     if(status != oldStatus)
                         chooseNotification();
                     break;
 
 
                 default:
                     status = -1;
                     Log.d("status", Integer.toString(status));
             }
 
         } catch (ClientProtocolException e) {
             //no network
             e.printStackTrace();
             status = -1;
             chooseNotification();
         } catch (IOException e) {
             //http problem
             e.printStackTrace();
             status = -2;
             Log.d("status", Integer.toString(status));
         } catch (IllegalStateException e) {
             e.printStackTrace();
             status = -1;
             Log.d("status", Integer.toString(status));
         }
     }
 
     public boolean isForeground(String myPackage){
         ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
         List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);
 
         ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
         if(componentInfo.getPackageName().equals(myPackage)) return true;
         return false;
     }
 
     /**
      * Show a notification while this service is running.
      */
     private void chooseNotification() {
         CharSequence text;
 
         if (status == 0) {
             text = "P-Space just closed...";
             mNM.cancelAll();
             notifyid = 0;
             updateActivity();
             if(!notificationSelect)
                 showNotification(text);
         }
         if (status == 1) {
             text = "P-Space just opened!";
             mNM.cancelAll();
             notifyid = 1;
             updateActivity();
             showNotification(text);
         }
         else
             updateActivity();
             notifyid = -1;
     }
 
     private void showNotification(CharSequence text){
 
         Intent intent = new Intent(this, MainActivity.class);
 
         TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
         // Adds the back stack for the Intent (but not the Intent itself)
         stackBuilder.addParentStack(MainActivity.class);
         // Adds the Intent that starts the Activity to the top of the stack
         stackBuilder.addNextIntent(intent);
         PendingIntent pendingIntent =
                 stackBuilder.getPendingIntent(
                         0,
                         PendingIntent.FLAG_UPDATE_CURRENT
                 );
 
         if(!isForeground("com.pspace.gr")&&notificationState){
         Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                 R.drawable.ic_icon);
         // Set the icon, scrolling text and timestamp
 
             if (Build.VERSION.SDK_INT >= 11){
                 Notification notification;
                 notification = new Notification.Builder(getApplicationContext())
                     .setContentTitle("P-Space")
                     .setContentText(text)
                     .setDefaults(Notification.DEFAULT_ALL)
                     .setAutoCancel(true)
                     .setOnlyAlertOnce(true)
                     .setSmallIcon(R.drawable.ic_icon)
                     .setLargeIcon(icon)
                     .setContentIntent(pendingIntent)
                     .build();
 
                 mNM.notify(notifyid, notification);
             }
             else{
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                         getApplicationContext())
                         .setSmallIcon(R.drawable.ic_icon)
                         .setContentTitle("P-Space")
                         .setContentText(text);
                 mNM.notify(notifyid,mBuilder.build());
             }
         }
     }
 
     private void updateActivity(){
         //send update to the main activity to show that status changed
         Intent intentUpdate = new Intent();
         intentUpdate.setAction(ACTION_MyUpdate);
         intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
         intentUpdate.putExtra(EXTRA_KEY_UPDATE, status);
         sendBroadcast(intentUpdate);
     }
 
     private void loadPref(){
         SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
         notificationState = mySharedPreferences.getBoolean("notification_preference", true);
         notificationSelect = mySharedPreferences.getBoolean("notification_select_preference", false);
         testMenu = mySharedPreferences.getBoolean("test_preference", false);
     }
 }
