 package pro.kornev.kcar.cop.services.support;
 
 import android.os.Environment;
 import android.util.Log;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import pro.kornev.kcar.cop.services.CustomService;
 import pro.kornev.kcar.protocol.Data;
 
 /**
  *
  */
 public class UpdateService implements CustomService, Runnable {
     private static final int DELAY = 3600;
     private static final int VERSION = 0;
     private static final String UPDATE_URL = "http://kornev.pro/cop.apk";
     private static final String VERSION_URL = "http://kornev.pro/cop_version.txt";
     private volatile ScheduledExecutorService executorService;
     private volatile boolean running = false;
 
 
     @Override
     public boolean start() {
         if (running) return false;
         executorService = Executors.newSingleThreadScheduledExecutor();
         executorService.scheduleWithFixedDelay(this, 2, DELAY, TimeUnit.SECONDS);
         running = true;
         return true;
     }
 
     @Override
     public boolean stop() {
         if (!running) return false;
         executorService.shutdown();
         running = false;
         return false;
     }
 
     @Override
     public void onDataReceived(Data data) {
 
     }
 
     @Override
     public void run() {
         if (VERSION < getActualVersion()) {
             update();
         }
     }
 
     private int getActualVersion() {
         try {
             URL url = new URL(VERSION_URL);
             HttpURLConnection c = (HttpURLConnection) url.openConnection();
             c.setRequestMethod("GET");
             c.setDoOutput(true);
             c.connect();
             InputStream is = c.getInputStream();
             int version = is.read();
             return version - '0';
         } catch (Exception e) {
             e.printStackTrace(); // TODO : alert error
             return 0;
         }
     }
 
     private void update() {
         try {
             URL url = new URL(UPDATE_URL);
             HttpURLConnection c = (HttpURLConnection) url.openConnection();
             c.setRequestMethod("GET");
             c.setDoOutput(true);
             c.connect();
 
             String PATH = Environment.getExternalStorageDirectory() + "/Download";
             File file = new File(PATH);
             if (!file.exists()) {
                 if (!file.mkdirs()) return; // TODO : alert error
             }
             File outputFile = new File(file, "/update.apk");
             if(outputFile.exists()){
                 if (!outputFile.delete()) return; // TODO : alert error
             }
             FileOutputStream fos = new FileOutputStream(outputFile);
 
             InputStream is = c.getInputStream();
 
             byte[] buffer = new byte[1024];
             int len1;
             while ((len1 = is.read(buffer)) != -1) {
                 fos.write(buffer, 0, len1);
             }
             fos.close();
             is.close();
 
             String path = (outputFile).getAbsolutePath();
 
             if (ShellInterface.isSuAvailable()) {
                ShellInterface.runCommand("pm install -r "+path);
             }
         } catch (Exception e) {
             Log.e("UpdateAPP", "Update error! " + e.getMessage());
         }
     }
 }
