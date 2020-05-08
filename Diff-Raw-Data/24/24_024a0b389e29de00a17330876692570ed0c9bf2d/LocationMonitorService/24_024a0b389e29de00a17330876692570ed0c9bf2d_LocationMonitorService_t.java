 package ru.spb.cupchinolabs.androidlocator;
 
 import android.app.Notification;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.widget.Toast;
 
 /**
  * Created with IntelliJ IDEA.
  * User: VladimirK
  * Date: 02.03.13
  * Time: 9:49
  */
 public class LocationMonitorService extends Service {
 
     protected static final int MSG_START = 1;
     protected static final int MSG_STOP  = 2;
 
     /**
      * Target we publish for clients to send messages to IncomingHandler.
      */
     final private Messenger messenger = new Messenger(new IncomingHandler());
 
     /**
      * Handler of incoming messages from clients.
      */
     private class IncomingHandler extends Handler {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case MSG_START:
                     notifyOnStart();
                     //TODO start worker thread (see HandlerThread, Looper)
                     //TODO replace Toast with notification
                    //TODO I must be undestroyable
                     break;
                 case MSG_STOP:
                     notifyOnStop();
                     //TODO stop worker thread
                    //TODO I must be destroyable
                     break;
                 default:
                     super.handleMessage(msg);
             }
         }
     }
 
     /**
      * When binding to the service, we return an interface to our messenger
      * for sending messages to the service.
      */
     @Override
     public IBinder onBind(Intent intent) {
         Toast.makeText(this, "Location monitor service - binding ...", Toast.LENGTH_SHORT).show();
         return messenger.getBinder();
     }
 
     @Override
     public void onCreate() {
 //        The system calls this method when the service is first created, to perform one -time setup procedures (before
 //        it calls either onStartCommand () or onBind ()).If the service is already running, this method is not called.
         //TODO init ContentProvider, check wifi, gps?
         Toast.makeText(this, "Location monitor service - creating ...", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public void onDestroy() {
 //        The system calls this method when the service is no longer used and is being destroyed.Your service
 //        should implement this to clean up any resources such as threads, registered listeners, receivers, etc.This
 //        is the last call the service receives.
         //TODO release all the resources
         Toast.makeText(this, "Location monitor service - destroying ...", Toast.LENGTH_SHORT).show();
     }
 
     private void notifyOnStop() {
         //TODO replace Toast with notification
         Toast.makeText(this, "Location monitor service is stopping ...", Toast.LENGTH_SHORT).show();
         stopForeground(true);
     }
 
     private void notifyOnStart() {
         Notification notification = new Notification(R.drawable.stat_happy, getText(R.string.locator_notification_ticker_text),
                 System.currentTimeMillis());
         Intent notificationIntent = new Intent(this, LocationMonitorAction.class);
         PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
         notification.setLatestEventInfo(this, getText(R.string.locator_notification_content_title),
                 getText(R.string.locator_notification_content_text), pendingIntent);
         startForeground(R.string.locator_notification_content_title, notification);
         Toast.makeText(this, "Location monitor service is starting ...", Toast.LENGTH_SHORT).show();
     }
 }
