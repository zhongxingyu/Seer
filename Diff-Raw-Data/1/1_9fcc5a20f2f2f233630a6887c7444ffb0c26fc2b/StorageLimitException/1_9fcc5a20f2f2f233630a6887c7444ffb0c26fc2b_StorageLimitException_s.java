 package com.funambol.sync;
 
 import java.io.IOException;
 
 public class StorageLimitException extends IOException {
     
     private static final long serialVersionUID = 1L; // default
     
     private long itemSize;
     private int threshold;
     
     public StorageLimitException(long itemSize, int threshold) {
         this.itemSize = itemSize;
         this.threshold = threshold;
     }
 
     public long getItemSize() {
         return itemSize;
     }
 
     public int getThreshold() {
         return threshold;
     }
 
     /**
      * Gets a SyncException instance equivalent to this exception.
      * In this way we can consistently convert this IOException into a SyncException
      * with the proper error code and message.
      * 
      * @return a SyncException with error code 419 (Local Device Full) and this 
      *         StorageLimitException as its cause 
      */
     public SyncException getCorrespondingSyncException() {
         SyncException syncException = new SyncException(
                 SyncException.LOCAL_DEVICE_FULL, 
                 "Downloading this " + getItemSize() + "-byte item would exceed " +
                 "the " + getThreshold() + "% safety limit on used storage space " +
                 "on the device");
        syncException.initCause(this);
         return syncException;
     }
 
 }
