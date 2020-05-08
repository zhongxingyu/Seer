 package com.services;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Binder;
 import android.os.Handler;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 import com.activities.Activity_Chat;
 import com.activities.R;
 import com.data.ApplicationUser;
 import data.Message;
 import data.User;
 import data.contents.ChatContent;
 
 import java.io.IOException;
 import java.util.Date;
 
 import static java.lang.Thread.sleep;
 
 /**
  *
  */
 public class MessageService extends Service {
     private NotificationManager nm; // the notification manager to throw notifications
 
     private int nr = 0;
 
     private ApplicationUser me;
 
     private Context activityContext;
     private Uri alarmSound;
     private Intent intent;
     private Handler handler;
     private boolean stop;
 
     @Override
     public void onCreate() {
         nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
         activityContext = this;
 
         alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
     }
 
     private class ReceiveMessages extends AsyncTask<Object, Object, Object> {
 
         @Override
         protected Object doInBackground(Object... objects) {
             while(!stop) {
                 try {
 
                     me.connect();
                     me.registerToServer();
                     printMessage("S/Chat connected");
 
                     while(me.isConnected() && !stop)
                         sleep(1000);
 
                     printMessage("S/Chat disconnected!");
                 }
                 catch (Exception e) {
                 }
                 try {
                     sleep(5000);
                 } catch (InterruptedException e1) {}
             }
             return new Object();
         }
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         new ReceiveMessages().execute(null);
 
         handler = new Handler();
         try {
             me = ApplicationUser.getInstance();
             me.setMessageService(this);
         } catch (IOException e) {
             e.printStackTrace();
         }
         stop = false;
 
         return START_STICKY;
     }
 
     public void throwNotification(Message<ChatContent> message) {
         intent = new Intent(activityContext, Activity_Chat.class);
         intent.putExtra("notyou", new User(message.getSender()));
        PendingIntent pIntent = PendingIntent.getActivity(activityContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 
         Notification n = new Notification.Builder(activityContext)
                 .setContentTitle("SChat Message from: " + message.getSender())
                 .setContentText(message.getContent().getMessage())
                 .setSmallIcon(R.drawable.ic_launcher)
                 .setSound(alarmSound)
                 .setContentIntent(pIntent)
                 .setAutoCancel(false).build();
         n.flags |= Notification.FLAG_AUTO_CANCEL;
 
         nm.notify(nr, n);
         // nr++;
     }
 
     public void receiveMessage(final Message<ChatContent> message) {
         handler.post(new Runnable() {
             public void run() {
                throwNotification(message);
             }
         });
     }
 
     public void printMessage(final String s) {
         handler.post(new Runnable() {
             public void run() {
                 Toast.makeText(activityContext, s, Toast.LENGTH_SHORT).show();
             }
         });
     }
 
     /* we probably don't even need it, added it anyway because it was in the example */
     public class MyBinder extends Binder {
         MessageService getService() {
             return MessageService.this;
         }
     }
 
     private final IBinder mBinder = new MyBinder();
 
     @Override
     public IBinder onBind(Intent intent) {
         return mBinder;
     }
 }
