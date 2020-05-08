 package org.mg8.pushr.droid.svc;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.mg8.pushr.droid.R;
 import org.mg8.pushr.flickr.FlickrException;
 import org.mg8.pushr.flickr.FlickrRest;
 
 import android.app.Service;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.RemoteException;
 import android.provider.MediaStore.Images.ImageColumns;
 import android.widget.Toast;
 
 /**
  * Implementation of the Upload Service.  This service fires up a thread in
  * the background, queues upload requests, and processes them as it can.
  *
  * @author Cliff L. Biffle
  */
 public class UploadService extends Service {
   /*
    * We manage our own queue instead of using Android's built-in equivalent
    * because we need to be able to peek into it and quit asynchronously
    * ("cancel" the queue).
    */
   final BlockingQueue<Message> messageQueue =
       new LinkedBlockingQueue<Message>();
   final List<IUploadCallback> callbacks =
       new CopyOnWriteArrayList<IUploadCallback>();
   
   volatile boolean threadRunning;
   private Thread uploadThread;
   
   ImageStore imageStore;
   FlickrRest flickr;
   
   final AtomicLong bytesToSend = new AtomicLong(0), bytesSent = new AtomicLong(0);
   
   @Override public void onCreate() {
     super.onCreate();
     
     String apiKey = getString(R.string.api_key);
     String sharedSecret = getString(R.string.shared_secret);
    String authToken = getString(R.string.auth_token);
     
    if (apiKey == null || sharedSecret == null || authToken == null) {
       Toast.makeText(this, "Did you forget secrets.xml?", Toast.LENGTH_LONG).show();
     }
     
     imageStore = new ImageStore(getContentResolver());
     flickr = new FlickrRest(apiKey, sharedSecret, authToken);
     
     threadRunning = true;
     uploadThread = new Thread(new UploadLoop());
     uploadThread.start();    
   }
 
   @Override
   public void onDestroy() {
     threadRunning = false;
     uploadThread.interrupt();
     super.onDestroy();
   }
 
   @Override
   public void onStart(Intent intent, int startId) {
     super.onStart(intent, startId);
     
     Message message = Message.obtain();
     message.obj = intent.getParcelableExtra(Intent.EXTRA_STREAM);
     message.arg1 = startId;
     
     bytesToSend.addAndGet(getImageSize((Uri) message.obj));
     messageQueue.offer(message);
   }
 
   @Override
   public IBinder onBind(Intent intent) {
     return binder;
   }
   
   private long getImageSize(Uri uri) {
     Cursor c = imageStore.getInfo(uri);
     c.moveToFirst();
     String path = c.getString(c.getColumnIndexOrThrow(ImageColumns.DATA));
     long imageSize = new File(path).length();
     c.close();
     return imageSize;
   }
   
   private final IUploadService.Stub binder = new IUploadService.Stub() {
     @Override public void registerCallback(IUploadCallback callback)
         throws RemoteException {
       callbacks.add(callback);
     }
 
     @Override public void removeCallback(IUploadCallback callback) throws RemoteException {
       callbacks.remove(callback);
     }};
   
   class UploadLoop implements Runnable {    
     @Override public void run() {
       while (threadRunning) {
         try {          
           Message m = messageQueue.take();
           final Uri uri = (Uri) m.obj;
           int token = m.arg1;
     
           Cursor c = imageStore.getInfo(uri);
           c.moveToFirst();
           String name = c.getString(c.getColumnIndexOrThrow(ImageColumns.DISPLAY_NAME));
           String path = c.getString(c.getColumnIndexOrThrow(ImageColumns.DATA));
           long imageSize = new File(path).length();
           c.close();
           
           AndroidContentBody data = new AndroidContentBody(name, imageSize, "image/jpeg",
               new Openable() {
                 @Override public InputStream open() throws IOException {
                   return imageStore.openImage(uri);
                 }});
           data.setProgressListener(progress);
           currentName = name;
           Map<String, String> meta = new HashMap<String, String>();
           
           try {
             flickr.pushPhoto(name, data, meta);
           } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
           } catch (FlickrException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
           }
           
           currentName = null;
           for (IUploadCallback callback : callbacks) {
             try {
               callback.statusUpdate(null, 0, 0);
             } catch (RemoteException e) {
               e.printStackTrace();
             }
           }
           stopSelf(token);
         } catch (InterruptedException e) {
           // Drain queue
           while (true) {
             Message m = messageQueue.poll();
             if (m != null) stopSelf(m.arg1);
           }
           // Return to idle state
         }
       }
     }
     
   }
   
   volatile String currentName;
   final AndroidContentBody.ProgressListener progress =
       new AndroidContentBody.ProgressListener() {
         @Override public void progressHasBeenMade(long bytes) {
           long soFar = bytesSent.addAndGet(bytes);
           long goal = bytesToSend.get();
           for (IUploadCallback callback : callbacks) {
             try {
               callback.statusUpdate(currentName, (int)soFar, (int)goal);
             } catch (RemoteException e) {
               e.printStackTrace();
             }
           }
         }};
   
 }
