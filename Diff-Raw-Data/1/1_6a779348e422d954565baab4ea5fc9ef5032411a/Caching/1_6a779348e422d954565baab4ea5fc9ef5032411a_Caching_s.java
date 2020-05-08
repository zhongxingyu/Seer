 package org.joushou.FiveProxy;
  
 import java.io.File;
 import java.sql.DriverManager;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.io.IOException;
 import java.util.ArrayList; 
  
 class Caching extends Thread {
   private static ArrayList<Integer> caching;
   
   public static void init() {
     caching = new ArrayList<Integer>();
   }
  
   public static synchronized boolean isCaching(int id) {
     if(caching.contains(id)){
      caching.remove(id);
       return true;
     } else {
       return false;
     }
   }
  
   public static synchronized void cache(int id) {
     if(!caching.contains(id))
       caching.add(id);
   }
  
   public static synchronized void doneCaching(int id) {
     if (caching.contains(id))
       caching.remove(id);
   }
   
   public synchronized void clean() {
     notify();
   }
  
   public synchronized void run() {
     while (true) {
         try {
           Main.log("CacheManager: " + getPercentageUsed() + "% used ["+getCacheSize() / 1048576+"MiB/"+Settings.maxCache/1048576+"MiB]");
           if (getCacheSize() > Settings.maxCache) {
             Main.log("CacheManager: Initiating cleanup-routine...");
             Object[] delIds = MusicDB.getCleanupIds();
             if (!(delIds.length == 0)) {
               int i = 0;
               while(i <= delIds.length  && getCacheSize() > Settings.maxCache) {
                 int id = new Integer((Integer)delIds[i]);
                 System.out.println("Hmm.. sombodys tellin' me to delete song #"+ id);
                 if(!Caching.isCaching(id)) {
                   Main.log("CacheManager: Deleting '" + MusicDB.getTitleFromId(id) + "' from cache");
                   File f = new File(Settings.cacheFolder + id);
                   f.delete();
                   MusicDB.deleteLogById(id);
                 }
               }
               if(getCacheSize() > Settings.maxCache) {
                 Main.log("CacheManager: Still too big after cleaning up; Consider increasing the cache size"); 
               } else {
                 Main.log("CacheManager: After cleanup: " +getPercentageUsed() + "% used");
               }
             }
           }
         wait();
         } catch(Exception e) {e.printStackTrace();}
       }
   }
   private static long getCacheSize() {
     long folderSize = 0;
     File[] filelist = new File(Settings.cacheFolder).listFiles();
     int i;
     for(i=0;i<filelist.length;i++) {
       folderSize += filelist[i].length();
     }
     return folderSize;
   }
   public static Float getPercentageUsed() {
     Float percentage = ((float)getCacheSize() / (float)Settings.maxCache) *  (float)100.0;
     return percentage;
   }
 }
