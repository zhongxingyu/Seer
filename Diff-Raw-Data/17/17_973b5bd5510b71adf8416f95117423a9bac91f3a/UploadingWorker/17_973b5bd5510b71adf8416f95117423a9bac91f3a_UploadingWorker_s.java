 package ru.elifantiev.rallyresults.service;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import ru.elifantiev.rallyresults.RallyWebService;
 import ru.elifantiev.rallyresults.infrastructure.RallySection;
 import ru.elifantiev.rallyresults.infrastructure.StatRecord;
 
 import java.util.concurrent.BlockingQueue;
 
 import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
 import static android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY;
 
 public class UploadingWorker extends Thread {
 
     private final BlockingQueue<StatRecord> queue;
     private final RallyWebService service;
     private final Context ctx;
     private final BroadcastReceiver connectionListener = new BroadcastReceiver() {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             isConnected = !intent.getBooleanExtra(EXTRA_NO_CONNECTIVITY, false);
         }
     };
     private WorkerStatusChangeListener listener;
     private boolean doWork = true;
     private boolean isConnected = true;
 
     UploadingWorker(
             Context ctx,
             RallyWebService service,
             BlockingQueue<StatRecord> recordsQueue,
             WorkerStatusChangeListener listener) {
         queue = recordsQueue;
         this.service = service;
         this.listener = listener;
         this.ctx = ctx;
         ctx.registerReceiver(connectionListener, new IntentFilter(CONNECTIVITY_ACTION));
     }
 
     @Override
     public void run() {
         StatRecord rec = null;
         while(doWork) {
             if(isConnected) { // check connectivity here
                 try {
                     if(rec == null)
                         rec = queue.take();
                     RallySection result = service.updateStatRecord(rec);
                     rec = null;
                     if(listener != null) {
                         listener.onDataReceived(result);
                         listener.onQueueLengthChanged(queue.size());
                     }
                 } catch (Exception e) {
                    e.printStackTrace();
                     if(rec != null) {
                         try {
                             queue.put(rec);
                             rec = null; // if successful - clear pointer
                         } catch (InterruptedException e1) {
                             // ignore
                             // if can't put, go to the next cycle and repeat there
                         }
                     }
                 }
             }
         }
     }
 
     public void cancel() {
         ctx.unregisterReceiver(connectionListener);
         doWork = false;
     }
 
     public interface WorkerStatusChangeListener {
         void onQueueLengthChanged(int queueLength);
         void onDataReceived(RallySection section);
     }
 }
