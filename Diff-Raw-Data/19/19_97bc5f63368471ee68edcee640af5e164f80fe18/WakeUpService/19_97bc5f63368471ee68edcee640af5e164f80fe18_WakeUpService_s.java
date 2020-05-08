 package pro.kornev.kcar.cop.services.support;
 
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 
 import pro.kornev.kcar.cop.Utils;
 
 /**
  *
  */
 public class WakeUpService extends Service implements Runnable {
     private static final String TAG = "WakeUpService";
     private boolean running = false;
     private Intent copServiceIntent;
     private IWakeUpBinder copService;
 
     private final ServiceConnection connection = new ServiceConnection() {
         @Override
         public void onServiceConnected(ComponentName name, IBinder service) {
             Log.w(TAG, "Bound to COP service");
             copService = IWakeUpBinder.Stub.asInterface(service);
             try {
                 copService.setCallback(wakeUpCallback);
             } catch (RemoteException e) {
                 e.printStackTrace();
             }
         }
 
         @Override
         public void onServiceDisconnected(ComponentName name) {
             Log.w(TAG, "Unbound from COP service");
             copService = null;
         }
     };
 
     private final IWakeUpCallback.Stub wakeUpCallback = new IWakeUpCallback.Stub() {
         @Override
         public void stop() throws RemoteException {
             Log.w(TAG, "Stopping..");
             setRunning(false);
             stopSelf();
         }
     };
 
     @Override
     public void onCreate() {
         super.onCreate();
         Thread.setDefaultUncaughtExceptionHandler(new UncaughtException());
         copServiceIntent = new Intent("pro.kornev.kcar.cop.COP");
         copServiceIntent.putExtra("a", "a");
         Log.w(TAG, "Created");
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Log.w(TAG, "Starting");
         if (isRunning()) {
             Log.w(TAG, "Already started");
             stopSelf();
             return START_NOT_STICKY;
         }
         setRunning(true);
         new Thread(this).start();
         Log.w(TAG, "Started");
         return super.onStartCommand(intent, flags, startId);
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         Log.w(TAG, "Destroyed");
     }
 
     @Override
     public void onTaskRemoved(Intent rootIntent) {
         super.onTaskRemoved(rootIntent);
         Log.w(TAG, "Task removed");
     }
 
     @Override
     public void run() {
             while (isRunning()) {
                 try {
                     Log.w(TAG, "Check COP service state");
                     if (copService != null) {
                         if (!copService.isRunning()) {
                             Log.w(TAG, "Starting COP service...");
                             startService(copServiceIntent);
                         }
                     }
                     else {
                         Log.w(TAG, "Binding to COP service...");
                         bindService(copServiceIntent, connection, BIND_AUTO_CREATE);
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 Utils.sleep(5000);
             }
     }
 
     private synchronized boolean isRunning() {
         return running;
     }
 
     private synchronized void setRunning(boolean running) {
         this.running = running;
     }
 }
