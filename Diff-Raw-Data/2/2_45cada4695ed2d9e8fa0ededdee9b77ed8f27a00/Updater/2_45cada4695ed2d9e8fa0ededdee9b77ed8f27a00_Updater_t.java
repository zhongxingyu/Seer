 
 package me.heldplayer.util.HeldCore;
 
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.logging.Level;
 
 /**
  * Host class for HeldCore
  * 
  * @author heldplayer
  * 
  */
 public class Updater implements Runnable {
 
     private String modId;
     private String modVersion;
 
     /**
      * Creates an updater for a mod.
      * 
      * @param modId
      *        The mod ID of the mod.
      * @param modVersion
      *        The current version of the mod
      */
     public static void initializeUpdater(String modId, String modVersion) {
         Updater updater = new Updater(modId, modVersion);
         Thread thread = new Thread(updater, modId + " update checker");
         thread.setDaemon(true);
         thread.setPriority(Thread.MIN_PRIORITY);
         thread.start();
     }
 
     protected Updater(String modId, String modVersion) {
         this.modId = modId;
         this.modVersion = modVersion;
     }
 
     @Override
     public void run() {
         HttpURLConnection request = null;
         InputStream stream = null;
 
         try {
             request = (HttpURLConnection) new URL("http://dsiwars.x10.mx/files/version.php?mod=" + this.modId).openConnection();
             request.setRequestMethod("GET");
             request.connect();
 
             stream = request.getInputStream();
 
             if (request.getResponseCode() == 200) {
                 long time = System.currentTimeMillis();
                 while (stream.available() <= 0) {
                     if (time + 5000L < System.currentTimeMillis()) {
                         throw new RuntimeException("Read took too long");
                     }
                 }
 
                 byte[] bytes = new byte[stream.available()];
 
                 stream.read(bytes);
 
                 String latestVersion = new String(bytes);
 
                 String[] version = this.modVersion.split("\\.");
                 String[] lastVersion = latestVersion.split("\\.");
 
                 for (int i = 0; i < version.length && i < lastVersion.length; i++) {
                     int newest = Integer.parseInt(lastVersion[i]);
                     int old = Integer.parseInt(version[i]);
                     if (newest > old) {
                        HeldCore.log.log(Level.INFO, "The mod '" + this.modId + "' has a new version available!");
                         HeldCore.log.log(Level.INFO, "   Current version: " + this.modVersion + "  new version: " + latestVersion);
 
                         break;
                     }
                     else if (newest < old) {
                         break;
                     }
                 }
             }
             else {
                 throw new RuntimeException("Server returned HTTP response code " + request.getResponseCode());
             }
         }
         catch (NumberFormatException e) {}
         catch (Exception e) {
             try {
                 stream.close();
             }
             catch (Exception e2) {}
             HeldCore.log.log(Level.SEVERE, "Update check failed for '" + this.modId + "': " + e.getMessage());
         }
         finally {
 
             if (request != null) {
                 request.disconnect();
             }
         }
     }
 
 }
