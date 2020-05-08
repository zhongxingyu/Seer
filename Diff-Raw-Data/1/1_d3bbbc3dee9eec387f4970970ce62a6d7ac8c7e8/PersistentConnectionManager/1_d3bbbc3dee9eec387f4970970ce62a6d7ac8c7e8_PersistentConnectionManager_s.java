 package com.push.network;
 
 import org.androidpn.client.LogUtil;
 
 import com.push.service.NotificationService;
 
 import android.util.Log;
 
 public class PersistentConnectionManager extends Thread
 {
     private static final String LOGTAG = LogUtil.makeLogTag(PersistentConnectionManager.class);
     private NotificationService notification;
 
     private int waiting;
 
     public PersistentConnectionManager(NotificationService notification) {
         this.waiting = 0;
         this.notification = notification;
     }
     
     public void run() {
         try {
             while (!isInterrupted()) {
                 Log.d(LOGTAG, "Trying to reconnect in " + waiting() + " seconds");
                 Thread.sleep((long) waiting() * 1000L);
                 notification.login();
                 waiting++;
             }
         } catch (final InterruptedException e) {
 //            xmppManager.getHandler().post(new Runnable() {
 //                public void run() {
 //                    xmppManager.getConnectionListener().reconnectionFailed(e);
 //                }
 //            });
         }
     }
 
     private int waiting() {
         if (waiting > 20) {
             return 600;
         }
         if (waiting > 13) {
             return 300;
         }
         return waiting <= 7 ? 10 : 60;
     }
 }
