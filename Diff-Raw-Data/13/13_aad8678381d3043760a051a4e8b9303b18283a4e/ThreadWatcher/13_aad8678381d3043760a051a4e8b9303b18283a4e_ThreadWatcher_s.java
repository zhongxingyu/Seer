 /*
  * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
  * This is a java.net project, see https://jets3t.dev.java.net/
  * 
  * Copyright 2006 James Murty
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package org.jets3t.service.multithread;
 
 import java.util.Vector;
 
 /**
  * A thread watcher is associated with a multi-threaded S3 operation and contains information about
  * the progress of the operation.
  * <p>
  * As a minimum, this object will contain a count of the total number of threads involved in the operation
  * (via {@link #getThreadCount}) and the count of threads that have already finished 
  * (via {@link #getCompletedThreads}).
  * <p>
  * For operations involving data transfer, such as S3 object creation or downloads, this object may
  * also include a count of the total bytes being transferred (via {@link #getBytesTotal}) and a count of
  * how many bytes have already been transferred (via {@link #getBytesTransferred}). 
  * <p>
  * It may be possible some S3 operations. If an operation may be cancelled, this object will include
  * a {@link CancelEventTrigger} (via {@link #getCancelEventListener()}) which can be used to trigger a 
  * cancellation.
  * 
  * @author James Murty
  */
 public class ThreadWatcher {
     /**
      * The number of bytes transferred updates (via setBytesTransferredInfo) that must occurbefore transfer
      * before transfer rate and ETA is calculated.
      */
     private static final int MIN_TRANSFER_UPDATES_FOR_RATE = 20;
     
     private long completedThreads = 0;
     private long threadCount = 0;
     private CancelEventTrigger cancelEventListener = null;
     private long watcherStartTimeMS = -1;
     private long bytesTransferred = -1;
     private long bytesTotal = -1;
     private long bytesPerSecond = -1;
     private long timeRemainingSeconds = -1;
 
     /*
      * Variables to track the transfer rate over the last MIN_TRANSFER_UPDATES_FOR_RATE updates.
      */
     private int bytesUpdateCount = 0;
     private Vector historicBytesTransferredQueue = new Vector();
     private Vector historicTimeQueue = new Vector();
 
     public ThreadWatcher() {
         this.watcherStartTimeMS = System.currentTimeMillis();
     }
     
     public void setThreadsCompletedRatio(long completedThreads, long threadCount) {
         setThreadsCompletedRatio(completedThreads, threadCount, null);
     }
     
     public void setThreadsCompletedRatio(long completedThreads, long threadCount, 
         CancelEventTrigger cancelEventListener) 
     {
         this.completedThreads = completedThreads;
         this.threadCount = threadCount;
         this.cancelEventListener = cancelEventListener;        
     }    
     
     public void setBytesTransferredInfo(long bytesTransferred, long bytesTotal) {
         long now = System.currentTimeMillis();
 
         this.bytesTotal = bytesTotal;
         this.bytesTransferred = bytesTransferred;
         
         // Store historic information for transfer rate calculations.
         historicTimeQueue.add(new Long(now));
         historicBytesTransferredQueue.add(new Long(bytesTransferred));
 
         // Calculate the current transfer rate, and the time remaining for the data transfer,
         // once the minimum number of bytes transferred udpates have occurred.
         this.bytesUpdateCount++;
         if (this.bytesUpdateCount > MIN_TRANSFER_UPDATES_FOR_RATE) {
             // Get the number of bytes transfered as of MIN_UPDATES_FOR_CURRENT_RATE updates ago.
             Long historicByteTransferCount = (Long) historicBytesTransferredQueue.firstElement();
             historicBytesTransferredQueue.remove(0);
             
             // Get the time as of MIN_UPDATES_FOR_CURRENT_RATE updates ago.
             Long historicTime = (Long) historicTimeQueue.firstElement();
             historicTimeQueue.remove(0);
 
             long intervalsElapsedTimeMS = now - historicTime.longValue();
             long intervalsBytesTransferred = bytesTransferred - historicByteTransferCount.longValue();
             long bytesRemaining = bytesTotal - bytesTransferred;
             
             // Calculate the current bytes/s transfer rate.
            if (intervalsElapsedTimeMS != 0) {
                 this.bytesPerSecond = 1000 * intervalsBytesTransferred / intervalsElapsedTimeMS;                
             }
             
             // Calculate the averate bytes/s transfer rate.
            long overallBytesPerSecond = 1000 * bytesTransferred / 
                (System.currentTimeMillis() - watcherStartTimeMS);  
             
             // Calculate the time until the transfer is complete, using the *overall* bytes/second rate.
            if (bytesRemaining > 0) {
                 double remainingSecsDouble = bytesRemaining / overallBytesPerSecond;
                 this.timeRemainingSeconds = Math.round(remainingSecsDouble);
             } else {
                 this.timeRemainingSeconds = 0;
             }            
         }
     }
 
     public long getCompletedThreads() {
         return completedThreads;
     }
 
     public long getThreadCount() {
         return threadCount;
     }
     
     /**
      * If this method returns true, the methods {@link #getBytesTotal()} and {@link #getBytesTransferred()}
      * will contain information about the amount of data being transferred by the watched threads.
      * 
      * @return 
      * true if this watcher contains information about the bytes transferred by
      * the threads it is watching.  
      */
     public boolean isBytesTransferredInfoAvailable() {
         return (bytesTotal != -1 && bytesTransferred != -1);
     }    
     
     /**
      * @return
      * the expected total of bytes that will be transferred by the watched threads.
      * @throws IllegalStateException
      * if the bytes transferred information is not available - check this availability
      * with the {@link #isBytesTransferredInfoAvailable()} method.
      */
     public long getBytesTotal() throws IllegalStateException {
         if (!isBytesTransferredInfoAvailable()) {
             throw new IllegalStateException("Bytes Transferred Info is not available in this object");
         }
         return bytesTotal;
     }
 
     /**
      * @return
      * the count of bytes that have been transferred by the watched threads.
      * @throws IllegalStateException
      * if the bytes transferred information is not available - check this availability
      * with the {@link #isBytesTransferredInfoAvailable()} method.
      */
     public long getBytesTransferred() {
         if (!isBytesTransferredInfoAvailable()) {
             throw new IllegalStateException("Bytes Transferred Info is not available in this object");
         }
         return bytesTransferred;
     }
 
     /**
      * If this method returns true, the method {@link #getBytesPerSecond()} will contain 
      * an estimate of the per-second rate of data transfer.
      * 
      * @return 
      * true if this watcher contains an estimate of the per-second rate of data transfer.  
      */
     public boolean isBytesPerSecondAvailable() {
         return (this.bytesPerSecond != -1);
     }
     
     /**
      * @return
      * an estimate of the per-second byte rate transfer speed.
      * @throws IllegalStateException
      * if the per-second byte transfer rate estimate is not available - check this availability
      * with the {@link #isBytesPerSecondAvailable()} method.
      */
     public long getBytesPerSecond() {
         if (!isBytesPerSecondAvailable()) {
             throw new IllegalStateException("Bytes per-second estimate is not available in this object");
         }
         return this.bytesPerSecond;
     }
     
     /**
      * If this method returns true, the method {@link #getEtaTime()} will contain 
      * an estimate of the completion time for the data transfer.
      * 
      * @return 
      * true if this watcher contains an estimate of the completion time for the data transfer.  
      */
     public boolean isTimeRemainingAvailable() {
         return (this.timeRemainingSeconds != -1);
     }
     
     /**
      * @return
      * an estimate of the how many <b>seconds</b> until the data transfer completes, based on the
      * overall byte rate of the transmission.
      * @throws IllegalStateException
      * if the time remaining estimave is not available - check this availability
      * with the {@link #isTimeRemainingAvailable()} method.
      */
     public long getTimeRemaining() {
         if (!isTimeRemainingAvailable()) {
             throw new IllegalStateException("Time remaining estimate is not available in this object");
         }
         return this.timeRemainingSeconds;
     }
 
     /** 
      * @return
      * true if the S3 operation this object is associated with can be cancelled, and a 
      * {@link CancelEventTrigger} is available.
      */
     public boolean isCancelTaskSupported() {
         return cancelEventListener != null;
     }
 
     /**
      * Convenience method to trigger an event cancellation via {@link CancelEventTrigger#cancelTask} 
      * if this thread watcher is associated with an operation that can be cancelled.
      */
     public void cancelTask() {
         if (isCancelTaskSupported()) {
             cancelEventListener.cancelTask(this);
         }
     }
 
     /**
      * @return
      * the cancel event trigger associated with an S3 operation, if any.
      */
     public CancelEventTrigger getCancelEventListener() {
         return cancelEventListener;
     }
 
 }
