 package com.sohail.alam.mango_pi.smart.cache;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * User: Sohail Alam
  * Version: 1.0.0
  * Date: 6/7/13
  * Time: 10:16 PM
  */
 class SmartCacheHistoryImpl<K, V extends SmartCachePojo> implements SmartCacheHistory<K, V> {
 
     protected static final SmartCacheHistory HISTORY = new SmartCacheHistoryImpl();
     private final ConcurrentHashMap<K, SmartCacheHistoryPojo> SMART_CACHE_HISTORY;
     private final ExecutorService HISTORY_EXECUTOR = Executors.newSingleThreadExecutor();
     private AtomicLong maxElementCount = new AtomicLong(1000);
 
     private SmartCacheHistoryImpl() {
         SMART_CACHE_HISTORY = new ConcurrentHashMap<K, SmartCacheHistoryPojo>();
     }
 
     /**
      * Add to history.
      *
      * @param reason the reason
      * @param key    the key
      * @param value  the value
      */
     @Override
     public void addToHistory(String reason, K key, V value) {
         if (SMART_CACHE_HISTORY.size() >= maxElementCount.get())
             SMART_CACHE_HISTORY.clear();
 
         SMART_CACHE_HISTORY.put(key, new SmartCacheHistoryPojo<K, V>(reason, key, value));
     }
 
     /**
      * Add all to history.
      *
      * @param reason  the reason
      * @param dataMap the data map
      */
     @Override
     public void addAllToHistory(String reason, ConcurrentMap<K, V> dataMap) {
         if (dataMap != null) {
             for (K key : dataMap.keySet()) {
                 addToHistory(reason, key, dataMap.get(key));
             }
         }
     }
 
     /**
      * View history.
      *
      * @param key the key
      *
      * @return the string
      */
     @Override
     public String viewHistory(K key) {
         StringBuffer buffer = new StringBuffer();
         SmartCacheHistoryPojo found = SMART_CACHE_HISTORY.get(key);
         buffer.append("Smart Cache History: ");
         buffer.append("\r\n--------------------------------------------------------" +
                 "---------------------------------------------------------------\r\n");
         buffer.append(String.format("%-15s", "REASON"));
         buffer.append(String.format("%-50s", "KEY"));
         buffer.append(String.format("%-35s", "CREATION TIME"));
         buffer.append(String.format("%-35s", "DELETION TIME"));
         buffer.append("\r\n--------------------------------------------------------" +
                 "---------------------------------------------------------------\r\n");
         buffer.append(String.format("%-15s", found.DELETE_REASON));
         buffer.append(String.format("%-50s", found.KEY));
         buffer.append(String.format("%-35s", found.CREATION_TIME));
         buffer.append(String.format("%-35s", found.DELETION_TIME));
         buffer.append("\r\n");
         return buffer.toString();
     }
 
     /**
      * View history.
      *
      * @param reason the reason
      *
      * @return the string
      */
     @Override
     public String viewHistoryForReason(String reason) {
         StringBuffer buffer = new StringBuffer();
         SmartCacheHistoryPojo foundPojo = null;
         boolean found = false;
 
         buffer.append("Smart Cache History: ");
         buffer.append("\r\n--------------------------------------------------------" +
                 "---------------------------------------------------------------\r\n");
         buffer.append(String.format("%-15s", "REASON"));
         buffer.append(String.format("%-50s", "KEY"));
         buffer.append(String.format("%-35s", "CREATION TIME"));
         buffer.append(String.format("%-35s", "DELETION TIME"));
         buffer.append("\r\n--------------------------------------------------------" +
                 "---------------------------------------------------------------\r\n");
 
         for (K key : SMART_CACHE_HISTORY.keySet()) {
             if ((foundPojo = SMART_CACHE_HISTORY.get(key)).DELETE_REASON.equals(reason)) {
                 buffer.append(String.format("%-15s", foundPojo.DELETE_REASON));
                 buffer.append(String.format("%-50s", foundPojo.KEY));
                 buffer.append(String.format("%-35s", foundPojo.CREATION_TIME));
                 buffer.append(String.format("%-35s", foundPojo.DELETION_TIME));
                 buffer.append("\r\n");
                 found = true;
             }
         }
         if (!found) {
             buffer.append("There are no history corresponding to the reason: " + reason);
             buffer.append("\r\n");
         }
 
         return buffer.toString();
     }
 
     /**
      * View history.
      *
      * @return the string
      */
     @Override
     public String viewAllHistory() {
         StringBuffer buffer = new StringBuffer();
         SmartCacheHistoryPojo foundPojo = null;
 
         buffer.append("Smart Cache History: ");
         buffer.append("\r\n--------------------------------------------------------" +
                 "---------------------------------------------------------------\r\n");
         buffer.append(String.format("%-15s", "REASON"));
         buffer.append(String.format("%-50s", "KEY"));
         buffer.append(String.format("%-35s", "CREATION TIME"));
         buffer.append(String.format("%-35s", "DELETION TIME"));
         buffer.append("\r\n--------------------------------------------------------" +
                 "---------------------------------------------------------------\r\n");
 
         for (K key : SMART_CACHE_HISTORY.keySet()) {
             foundPojo = SMART_CACHE_HISTORY.get(key);
             buffer.append(String.format("%-15s", foundPojo.DELETE_REASON));
             buffer.append(String.format("%-50s", foundPojo.KEY));
             buffer.append(String.format("%-35s", foundPojo.CREATION_TIME));
             buffer.append(String.format("%-35s", foundPojo.DELETION_TIME));
             buffer.append("\r\n");
         }
         return buffer.toString();
     }
 
     /**
      * Set the maximum number of entries after which the History is
      * deleted permanently.
      *
      * @param maxElementCount the max element count
      */
     @Override
     public void autoDeleteHistory(int maxElementCount) {
         this.maxElementCount.set(maxElementCount);
     }
 
     /**
      * Purges the contents of History into a user defined file.
      * By default the SmartCache will dump the data into a file named -
      * SmartCacheHistory_(current-date/time).txt
      *
      * @param filePath the absolute file path for the dump file.
      */
     @Override
     public void purgeHistory(String filePath) throws Exception {
         HISTORY_EXECUTOR.execute(new PurgerClass(filePath));
     }
 
     private final class PurgerClass implements Runnable {
         String filePath;
 
         public PurgerClass(String filePath) {
             this.filePath = filePath;
         }
 
         @Override
         public void run() {
             String directory = "/";
             String fileName = "SmartCacheHistory_" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + ".txt";
 
             if (filePath.contains("/")) {
                 directory = filePath.substring(0, filePath.lastIndexOf("/") + 1);
                 fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
             } else if (filePath.contains("\\")) {
                 directory = filePath.substring(0, filePath.lastIndexOf("\\") + 1);
                 fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
             }
             // Create the directories if not present
             File dir = new File(directory);
             dir.mkdirs();
 
             // Create the file
            File file = new File(dir.getAbsoluteFile() + fileName);
             FileOutputStream fos = null;
             try {
                 fos = new FileOutputStream(file);
                 fos.write(viewAllHistory().getBytes());
                 fos.flush();
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 if (fos != null) {
                     try {
                         fos.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
     }
 }
