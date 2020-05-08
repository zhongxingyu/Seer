 package pro.kornev.kcar.cop.services;
 
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.RemoteException;
 
 import pro.kornev.kcar.cop.Utils;
 import pro.kornev.kcar.cop.providers.LogsDB;
 import pro.kornev.kcar.cop.services.network.NetworkBinder;
 import pro.kornev.kcar.cop.services.network.NetworkService;
 import pro.kornev.kcar.cop.services.support.IWakeUpBinder;
 import pro.kornev.kcar.cop.services.support.IWakeUpCallback;
 import pro.kornev.kcar.cop.services.support.UncaughtException;
 import pro.kornev.kcar.cop.services.usb.UsbService;
 import pro.kornev.kcar.cop.services.video.VideoService;
 
 /**
  *
  */
 public class CopService extends Service {
     private final IBinder mBinder = new CopBinder();
     private LogsDB log;
     private boolean running = false;
     private VideoService videoService;
     private UsbService usbService;
     private Intent networkServiceIntent;
     private NetworkService networkService;
     private IWakeUpCallback wakeUpCallback;
 
     @Override
     public void onCreate() {
         super.onCreate();
         Thread.setDefaultUncaughtExceptionHandler(new UncaughtException());
         log = new LogsDB(this);
         log.putLog("CS Created");
         networkServiceIntent = new Intent(this, NetworkService.class);
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         log.putLog("CS Binging...");
         if ("a".equals(intent.getStringExtra("a"))) {
             return wakeUpBinder;
         }
         return mBinder;
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         log.putLog("CS Starting...");
         if (isRunning()) {
             log.putLog("Already running");
             stopSelf();
             return START_NOT_STICKY;
         }
         setRunning(true);
        //startDebugThread();
 
         videoService = new VideoService(this);
         usbService = new UsbService(this);
         bindService(networkServiceIntent, networkServiceConnection, Context.BIND_AUTO_CREATE);
 
         usbService.start();
         videoService.start();
 
         return super.onStartCommand(intent, flags, startId);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         stop();
         log.putLog("CS Destroyed");
     }
 
     @Override
     public boolean onUnbind(Intent intent) {
         log.putLog("CS Unbound");
         return super.onUnbind(intent);
     }
 
     @Override
     public void onTaskRemoved(Intent rootIntent) {
         super.onTaskRemoved(rootIntent);
         stop();
         log.putLog("CS Task removed");
     }
 
     public class CopBinder extends Binder {
         public CopService getService() {
             return CopService.this;
         }
     }
 
     public synchronized boolean isRunning() {
         return running;
     }
 
     public synchronized void stop() {
         setRunning(false);
         try {
             unbindService(networkServiceConnection);
             networkService.stop();
             videoService.stop();
             usbService.stop();
             wakeUpCallback.stop();
         } catch (Exception ignored) {}
         stopSelf();
     }
 
     public void restartUsbService() {
         if (usbService != null) {
             usbService.stop();
             usbService.start();
         }
     }
 
     public NetworkService getNetworkService() {
         return networkService;
     }
 
     private synchronized void setRunning(boolean running) {
         this.running = running;
     }
 
     @SuppressWarnings("unused")
     private void startDebugThread() {
         new Thread(new Runnable() {
             @Override
             public void run() {
                 int i=0;
                 while(isRunning()) {
                     log.putLog("CS i = " + i++);
                     if (i==10) {
                         i = i/0;
                     }
                     Utils.sleep(1000);
                 }
             }
         }).start();
     }
 
     private IWakeUpBinder.Stub wakeUpBinder = new IWakeUpBinder.Stub() {
 
         @Override
         public boolean isRunning() throws RemoteException {
             return CopService.this.isRunning();
         }
 
         @Override
         public void setCallback(IWakeUpCallback callback) throws RemoteException {
             wakeUpCallback = callback;
         }
     };
 
     private ServiceConnection networkServiceConnection  = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName name, IBinder service) {
             networkService = ((NetworkBinder)service).getService();
             networkService.addListener(videoService);
             networkService.addListener(usbService);
             if (!networkService.isRunning()) {
                 startService(networkServiceIntent);
             }
         }
 
         @Override
         public void onServiceDisconnected(ComponentName name) {
         }
     };
 }
