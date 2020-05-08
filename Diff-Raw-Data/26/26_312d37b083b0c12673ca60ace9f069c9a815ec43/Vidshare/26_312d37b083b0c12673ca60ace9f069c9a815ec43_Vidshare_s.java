 package com.gm375.vidshare;
 
 import org.haggle.Attribute;
 import org.haggle.DataObject;
 import org.haggle.Handle;
 import org.haggle.Node;
 
 import android.app.AlertDialog;
 import android.app.Application;
 import android.content.DialogInterface;
 import android.content.res.Configuration;
 import android.util.Log;
 
 public class Vidshare extends Application implements org.haggle.EventHandler {
     
     public static final String LOG_TAG = "Vidshare";
     
     public static final int NO_OF_TRIES = 2;
     
     public static final int STATUS_OK = 0;
     public static final int STATUS_ERROR = -1;
     public static final int STATUS_REGISTRATION_FAILED = -2;
     public static final int STATUS_SPAWN_DAEMON_FAILED = -3;
     
     public static final int RECORD_VIDEO_REQUEST = 10;
     
     
     private VSActivity act = null;
     private org.haggle.Handle hh = null;
     private int status = STATUS_OK;
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         // TODO Auto-generated method stub
         super.onConfigurationChanged(newConfig);
     }
     
     @Override
     public void onCreate() {
         super.onCreate();
         
         Log.d(Vidshare.LOG_TAG, "Vidshare: ***onCreate() Thread ID = "+ Thread.currentThread().getId() +" ***");
         
         // TODO: Get vibrator here, when implementing.
         
     }
     
     @Override
     public void onLowMemory() {
         // TODO Auto-generated method stub
         super.onLowMemory();
         Log.d(Vidshare.LOG_TAG, "***onLowMemory()***");  
     }
 
     @Override
     public void onTerminate() {
         // TODO Auto-generated method stub
         super.onTerminate();
         Log.d(Vidshare.LOG_TAG, "***onTerminate()***");  
         //finiHaggle();
     }
     
     public void setVSActivity(VSActivity act) {
         Log.d(Vidshare.LOG_TAG, "***Setting Activity***");
         this.act = act;
     }
     
     public VSActivity getVSActivity() {
         return act;
     }
     
     public Handle getHaggleHandle() {
         return hh;
     }
     
     public void tryDeregisterHaggleHandle() {
         finiHaggle();
     }
     
     public int initHaggle() {
         
         if (hh != null)
             return STATUS_OK;
         
         long pid = Handle.getDaemonPid();
         
         Log.d(Vidshare.LOG_TAG, "***Attempting to spawn Haggle daemon***");
         
         if (!Handle.spawnDaemon()) {
             Log.d(Vidshare.LOG_TAG, "***Spawning failed...***");
             return STATUS_SPAWN_DAEMON_FAILED;
         }
         pid = Handle.getDaemonPid();
 
         Log.d(Vidshare.LOG_TAG, "***Haggle daemon pid is "+ pid +" ***");
         
         int tries = NO_OF_TRIES;
 
         while (tries > 0) {
             try {
                 hh = new Handle("Vidshare");
 
             } catch (Handle.RegistrationFailedException e) {
                 Log.e(Vidshare.LOG_TAG, "***Registration failed*** " + e.getMessage());
 
                 if (--tries > 0) {
                     Handle.unregister("PhotoShare");
                     continue;
                 }
 
                 Log.e(Vidshare.LOG_TAG, "***Registration failed, giving up***");
                 return STATUS_REGISTRATION_FAILED;
             }
             break;
         }
         
         hh.registerEventInterest(EVENT_NEIGHBOR_UPDATE, this);
         hh.registerEventInterest(EVENT_NEW_DATAOBJECT, this);
         hh.registerEventInterest(EVENT_INTEREST_LIST_UPDATE, this);
         hh.registerEventInterest(EVENT_HAGGLE_SHUTDOWN, this);
         
         hh.eventLoopRunAsync();
 
         hh.getApplicationInterestsAsync();
         
         Log.d(Vidshare.LOG_TAG, "***Haggle event loop started***");   
 
         return STATUS_OK;
         
     }
     
     public synchronized void finiHaggle() {
         if (hh != null) {
             hh.eventLoopStop();
             hh.dispose();
             hh = null;
         }
     }
     
     public int getStatus() {
         return status;
     }
     
     public void shutdownHaggle() {
         hh.shutdown();
     }
     
     public boolean registerInterest(Attribute interest) {
         if (hh.registerInterest(interest) == 0) 
             return true;
         return false;
     }
     
     @Override
     public void onInterestListUpdate(Attribute[] interests) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void onNeighborUpdate(Node[] neighbors) {
         
         if (act == null)
             return;
         
         Log.d(Vidshare.LOG_TAG, "***Received neighbour update, Thread ID = "+ Thread.currentThread().getId() +" ***");
         
         act.runOnUiThread(act.new DataUpdater(neighbors));
     }
 
     @Override
     public void onNewDataObject(DataObject dObj) {
         
         // TODO: At the moment, this only responds to a video file. Will probably need to change for streaming.
         
         if (act == null) {
             Log.d(Vidshare.LOG_TAG, "***Activity was null***");
             return;
         }
         
         Log.d(Vidshare.LOG_TAG, "***Got new data object, thread ID = "+ Thread.currentThread().getId() +" ***");
         
         if (dObj.getAttribute("Video", 0) == null) {
             Log.d(Vidshare.LOG_TAG, "***Received data object has no Video attribute***");
             return;
         }
         
         Log.d(Vidshare.LOG_TAG, "***Attempting to get file path***");
         
         String filepath = dObj.getFilePath();
         
         if (filepath == null || filepath.length() == 0) {
             Log.d(Vidshare.LOG_TAG, "***Bad filepath***");
             return;
         }
         
         Log.d(Vidshare.LOG_TAG, "***Success! Filepath = "+ filepath +" ***");
 
         Log.d(Vidshare.LOG_TAG, "Updating UI dobj");
         act.runOnUiThread(act.new DataUpdater(dObj));
         
     }
 
     @Override
     public void onShutdown(int reason) {
         
         Log.d(Vidshare.LOG_TAG, "***Shutdown event, reason = "+ reason +" ***");
         
         act.runOnUiThread(new Runnable() {
             public void run() {
                 AlertDialog.Builder builder = new AlertDialog.Builder(act);
                 builder
                 .setMessage("Haggle was shutdown and is no longer running. Press Quit to exit Vidshare.")
                 .setCancelable(false)
                 .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();
                         act.finish();
                     }
                 });
                 AlertDialog alert = builder.create();
                 alert.show();
             }
         });
     }
     
 }
