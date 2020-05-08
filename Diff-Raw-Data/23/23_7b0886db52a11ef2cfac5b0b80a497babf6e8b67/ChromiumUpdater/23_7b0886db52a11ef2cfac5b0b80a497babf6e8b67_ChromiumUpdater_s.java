 package net.chromiumupdater;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 /**
  *
  * @author morth, cfstras
  */
 public class ChromiumUpdater {
 
     protected static GUI gui;
     static Download download;
     public static int localversion;
     public static int remoteversion;
     public static String tempDir = "C:\\temp\\chrome\\";
     public static String installDir = System.getenv("PROGRAMFILES") + "\\Chromium\\";
 
     public static void main(String[] args) {
         gui = new GUI();
         gui.runGUI();
         check(gui);
     }
 
     public static void check(GUI g) {
         VersionCheck check = new VersionCheck(VersionCheck.WINDOWS);
         remoteversion = check.checkRemote();
 
         Settings settings = new Settings(remoteversion);
         settings.load();
         localversion = settings.build;
         g.setLastUpdateTime(settings.lastChecked);
         g.setLocalVersion(localversion);
         g.setRemoteVersion(remoteversion);
         if (localversion < remoteversion) {
             g.showUpdateButton();
         } else {
             g.setLabel("already up-to-date!");
         }
     }
 
     public static void save() {
         Settings settings = new Settings(remoteversion);
         settings.save();
     }
 
     static void download(GUI g) {
         URL dlurl = null;
         if(!new File(tempDir).exists()) {
             new File(tempDir).mkdirs();      
         }
         File f = new File(tempDir+"chrome-win32-" + remoteversion + ".zip");
         try {
             dlurl = new URL("http://commondatastorage.googleapis.com/chromium-browser-continuous/Win/" + remoteversion + "/chrome-win32.zip");
         } catch (MalformedURLException ex) {
         }
         //TODO check if already downloading!
         download = new Download(dlurl, f, g);
         try {
             download.download();
         } catch (IOException ex) {
         }
     }
 
     /**
      *
      * @param filename the file to unzip
      * @param destination the desinationfolder, should look like c:\\path\\to\\folder\\
      */
     static void unzip(String filename, String destination) {
         Unzip zip = new Unzip(new File(filename), new File(destination));
         try {
             zip.extract();
         } catch (Exception ex) {
             System.out.println("Fehler beim entpacken!");
         }
     }
     
    static boolean deleteDir(File dir) {
         if(dir.isDirectory()) {
             String[] children = dir.list();
             for(int i = 0;i<children.length;i++) {
                boolean success = deleteDir(new File(dir,children[i]));
                 if(!success)
                     return false;
             }
         }
             
         return dir.delete();
     }
 
     static void update(final GUI g) {
         //start the download in a new thread
         Thread t = new Thread() {
 
             @Override
             public void run() {
                 //download the latest build
                 System.out.println("Downloading ...");
                 download(gui);
                 //TODO: delete old build and ask if user wants a backup or cancel
                 System.out.println("Download done. Deleting install directory ...");
                if(!deleteDir(new File(installDir)))
                    System.out.println("Error while deleting old installation!");
                 //unzip to program files folder 
                System.out.println("Deleting done. Unzipping new build into install directory ...");
                unzip(tempDir+"chrome-win32-" + remoteversion + ".zip", installDir);
                System.out.println("Done ... Deleting temp files.");
                if(!deleteDir(new File(tempDir)))
                     System.out.println("Error while deleting temp files!");
                 //save new build number and a timestamp
                 save();
                 localversion = remoteversion;
                 g.setLocalVersion(localversion);
                 g.setLastUpdateTime(System.currentTimeMillis());
                 g.setLabel("Done!");
                 
             }
         };
         t.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
         t.start();
     }
 }
