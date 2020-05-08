 package pro.kornev.kcar.cop.services;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.hoho.android.usbserial.driver.UsbSerialDriver;
 import com.hoho.android.usbserial.util.HexDump;
 import com.hoho.android.usbserial.util.SerialInputOutputManager;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Queue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import pro.kornev.kcar.cop.State;
 import pro.kornev.kcar.cop.providers.LogsDB;
 import pro.kornev.kcar.protocol.Data;
 import pro.kornev.kcar.protocol.Protocol;
 
 /**
  * @author vkornev
  * @since 14.10.13
  */
 public class UsbService extends Service {
     private final String TAG = UsbService.class.getSimpleName();
 
     private LogsDB db;
     private static UsbSerialDriver sDriver = null;
     private SerialInputOutputManager mSerialIoManager;
     private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
     private Writer writer;
     private Thread writerThread;
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         db = new LogsDB(getApplicationContext());
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         super.onStartCommand(intent, flags, startId);
         Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
         sDriver = State.getUsbSerialDriver();
         onResume();
         return START_STICKY;
     }
 
     protected void onResume() {
         db.putLog("Resumed, sDriver=" + sDriver);
         if (sDriver == null) {
             db.putLog("No serial device.");
         } else {
             try {
                 sDriver.open();
             } catch (IOException e) {
                 db.putLog("Error setting up device: " + e.getMessage());
                 try {
                     sDriver.close();
                 } catch (IOException e2) {
                     // Ignore.
                 }
                 sDriver = null;
                 return;
             }
             db.putLog("Serial device: " + sDriver.getClass().getSimpleName());
         }
         onDeviceStateChange();
 
         writer = new Writer();
         writerThread = new Thread(writer);
         writerThread.start();
     }
 
     @Override
     public void onTaskRemoved(Intent rootIntent) {
         super.onTaskRemoved(rootIntent);
         writerThread.interrupt();
     }
 
     private final SerialInputOutputManager.Listener mListener =
             new SerialInputOutputManager.Listener() {
 
                 @Override
                 public void onRunError(Exception e) {
                     db.putLog("Runner stopped.");
                 }
 
                 @Override
                 public void onNewData(final byte[] data) {
                     db.putLog("Read data len: " + data.length);
                     db.putLog("Read data: " + HexDump.dumpHexString(data));
                     State.getToControlQueue().add(Protocol.fromByteArray(data, data.length));
                 }
             };
 
     private void stopIoManager() {
         if (mSerialIoManager != null) {
             Log.i(TAG, "Stopping io manager ..");
             mSerialIoManager.stop();
             mSerialIoManager = null;
         }
     }
 
     private void startIoManager() {
         if (sDriver != null) {
             Log.i(TAG, "Starting io manager ..");
             mSerialIoManager = new SerialInputOutputManager(sDriver, mListener);
             mExecutor.submit(mSerialIoManager);
         }
     }
 
     private void onDeviceStateChange() {
         stopIoManager();
         startIoManager();
     }
 
 class Writer implements Runnable {
     Queue<Data> queue;
     private byte[] bytes = new byte[Protocol.getMaxLength()];
     private int TIMEOUT = 10;
     UsbSerialDriver driver;
 
     Writer() {
         queue = State.getToUsbQueue();
         driver = State.getUsbSerialDriver();
     }
 
     @Override
     public void run() {
         db.putLog("Start USB writer");
         while (State.isServiceRunning()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
             if (queue.size() == 0) continue;
             db.putLog("Write data to USB");
             Data data = queue.poll();
             int bLen = Protocol.toByteArray(data, bytes);
             try {
                 driver.write(Arrays.copyOf(bytes, bLen), TIMEOUT);
             } catch (IOException e) {
                 db.putLog("Error: Failed send data to USB: " + e.getMessage());
             }
             db.putLog(String.format("USB write: Data id: %d; cmd: %d; type: %d; bData: %d; iData: %d", data.id, data.cmd, data.type, data.bData, data.iData));
         }
     }
 
 }
 
     @Override
     public void onDestroy() {
         Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
         State.setServiceRunning(false);
     }
 }
