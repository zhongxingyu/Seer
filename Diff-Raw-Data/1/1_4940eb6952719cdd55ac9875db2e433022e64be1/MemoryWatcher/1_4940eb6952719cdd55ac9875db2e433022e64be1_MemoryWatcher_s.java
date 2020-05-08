 package jDistsim.utils.gc;
 
 import jDistsim.utils.event.ActionObject;
 import jDistsim.utils.logging.Logger;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 28.10.12
  * Time: 1:12
  */
 public class MemoryWatcher extends ActionObject implements Runnable {
 
     private static final long MEGABYTE = 1024L * 1024L;
     private int sleepTime;
     private long totalMemory;
     private long freeMemory;
 
     public MemoryWatcher() {
         this(1000);
     }
 
     public MemoryWatcher(int sleepTime) {
         this.sleepTime = sleepTime;
     }
 
     @Override
     public void run() {
         while (true) {
             try {
                 calculateMemory();
                 doActionPerformed();
                 Thread.sleep(sleepTime);
             } catch (InterruptedException exception) {
                 Logger.log(exception);
             }
         }
     }
 
     public long getFreeMemory() {
         return bytesToMegabytes(freeMemory);
     }
 
     public long getTotalMemory() {
         return bytesToMegabytes(totalMemory);
     }
 
     public long getUsedMemory() {
         return bytesToMegabytes(totalMemory - freeMemory);
     }
 
     private void calculateMemory() {
         totalMemory = Runtime.getRuntime().totalMemory();
         freeMemory = Runtime.getRuntime().freeMemory();
     }
 
     private static long bytesToMegabytes(long bytes) {
         return bytes / MEGABYTE;
     }
 }
