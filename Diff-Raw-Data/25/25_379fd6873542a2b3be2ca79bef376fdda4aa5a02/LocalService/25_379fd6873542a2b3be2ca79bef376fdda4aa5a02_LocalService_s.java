 package com.danesh.moveover;
 
 import java.io.File;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.os.IBinder;
 
 public class LocalService extends Service {
     public static boolean serviceRunning;
     NotificationManager manager;
 
     @Override
     public IBinder onBind(Intent arg0) {
         return null;
     }
 
     @Override
     public void onCreate() {
     }
 
     @Override    
     public void onStart(Intent intent, int startId){
         super.onStart(intent, startId);
         onStartCommand(intent,0,startId);
     }
 
     @Override
     public void onDestroy() {
         serviceRunning = false;
         manager.cancelAll();
     }
 
     public void displayNotification(int id, String ticker, String msg){
         manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
         Notification notification1 = new Notification(R.drawable.icon, ticker, System.currentTimeMillis());
         notification1.flags = Notification.FLAG_ONGOING_EVENT;
         PendingIntent contentIntent = PendingIntent.getActivity(this, id, new Intent(this, MoveOverActivity.class), 0);
         notification1.setLatestEventInfo(this, ticker, msg, contentIntent);
         manager.notify(id, notification1);
     }
 
     /**
      * Cycles through folders and registers any subfolders
      * Parameters :
      * @param sourceDir Source folder to cycle through
      * @param item Target folder to set as destination
      */
     public static void cycleThrough(File sourceDir, String item){
        MoveOverActivity.print(sourceDir + " " + item);
         for (File file : sourceDir.listFiles()){
             if (file.isDirectory()){
                 cycleThrough(file, item);
             }
         }
        MyFileObserver newOne = new MyFileObserver(sourceDir.getPath().toString()+"/",item);
         newOne.startWatching();
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         serviceRunning = true;
         displayNotification(0,"Service is running","Click to launch");
         for (String item : MoveOverActivity.sharedMap.keySet()){
             File sourceDir = new File(item);
            MyFileObserver newOne = new MyFileObserver(item,MoveOverActivity.sharedMap.get(item).toString());
             newOne.startWatching();
             for (File file : sourceDir.listFiles()){
                 if (file.isDirectory()){
                    cycleThrough(file,MoveOverActivity.sharedMap.get(item).toString()+file.getPath().replaceFirst(sourceDir.getPath()+"/", "")+"/");
                 }
             }
         }
         return 0;
     }
 }
