 /*
  * JOnAS: Java(TM) Open Application Server
  * Copyright (C) 2004 Bull S.A.
  * All rights reserved.
  * 
  * Contact: howl@objectweb.org
  * 
  * This software is licensed under the BSD license.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 
  *   * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *     
  *   * Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  *     
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * ------------------------------------------------------------------------------
 * $Id: XALogger.java,v 1.18 2005-09-08 14:54:34 girouxm Exp $
  * ------------------------------------------------------------------------------
  */
 package org.objectweb.howl.log.xa;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import java.util.HashMap;
 
 import org.objectweb.howl.log.Configuration;
 import org.objectweb.howl.log.InvalidFileSetException;
 import org.objectweb.howl.log.InvalidLogBufferException;
 import org.objectweb.howl.log.InvalidLogKeyException;
 import org.objectweb.howl.log.LogClosedException;
 import org.objectweb.howl.log.LogConfigurationException;
 import org.objectweb.howl.log.LogException;
 import org.objectweb.howl.log.LogFileOverflowException;
 import org.objectweb.howl.log.LogRecord;
 import org.objectweb.howl.log.LogRecordSizeException;
 import org.objectweb.howl.log.LogRecordType;
 import org.objectweb.howl.log.Logger;
 import org.objectweb.howl.log.LogEventListener;
 import org.objectweb.howl.log.ReplayListener;
 
 /**
  * A specialized subclass of {@link org.objectweb.howl.log.Logger}
  * intended to provide functionality required by any XA Transaction Manager.
  * 
  * <p>This class provides wrappers for all public methods of Logger
  * to allow enforcement of policies that are in place for an XA TM log.
  * 
  * <p>This class implements {@link org.objectweb.howl.log.LogEventListener}.
  * During logOverflowNotification processing, entries in the activeTx[]
  * are re-logged and the active mark is moved.
  * 
  * <p>If the application has called setLogEventListener, then
  * Log events are forwarded to the application after they are
  * processed by this class.
  * 
  * @author Michael Giroux
  * 
  */
 /*
  * @history
  * <pre>
  * 2004-09-01 FEATURE 300768
  *            open(ReplayListener) accepts null
  *            OpenReplayListener constructor accepts null
  *            replayActiveTx(ReplayListener) method added
  * </pre> 
  */
 public class XALogger extends Logger
   implements LogEventListener
 {
   /**
    * array of transactions in COMMIT phase and
    * waiting for DONE.
    * <p>Array is grown as needed to accomodate
    * larger number of transactions in COMMITING state.
    * <p>When the Logger detects that a log file overflow
    * condition is about to occur 
    * {@link org.objectweb.howl.log.LogEventListener#logOverflowNotification(long)}
    * is invoked to allow the application to move older records
    * forward in the log.  This approach avoides the need to
    * move records forward every time a log file switch occurs.
    * <p>During the logOverflowNotification, activeTx[] is scanned
    * for entries with log keys older than the key specified
    * on the notification.  Since this processing only needs to
    * occur when a log file is about to overflow, the array is
    * used as an alternative to a linked list to eliminate the
    * overhead of managing the linked list for every
    * log record.   
    */
   XACommittingTx[] activeTx = null;
   
   /**
    * array of available XACommittingTx objects.
    * <p>Each entry in the array is associated with 
    * an entry in activeTx[].
    * <ul>
    * <li>Entries are obtained using the atxGet member.</li>
    * <li>Entries are returned to the list using the atxPut member.</li>
    * <li>The activeTx and availableTx arrays are grown as needed.</li>
    * </ul>
    */
   XACommittingTx[] availableTx = null;
   
   /**
    * workerID into availableTx[] used to remove an
    * entry.
    */
   int atxGet = 0;
   
   /**
    * workerID into availableTx[] used to return (add)
    * an entry.
    */
   int atxPut = 0;
   
   /**
    * number of used entries in activeTx table.
    * <p>table is grown when <i> atxUsed </i> is
    * equal to the size of the table and we are
    * trying to add one more entry. 
    */
   int atxUsed = 0;
   
   /**
    * maximum number of used entries.
    */
   int maxAtxUsed = 0;
   
   /**
    * lock used to synchronize access to the 
    * availableTx array, atxGet and atxPut
    * members.
    */
   final Object activeTxLock = new Object();
   
   /**
    * number of times the activeTx table was resized
    * to accomodate a larger number of transactions
    * in the COMMITTING state.
    */
   int growActiveTxArrayCount = 0;
   
   /**
    * number of times log overflow notification event was called.
    */
   int overflowNotificationCount = 0;
   
   /**
    * number of records moved by log overflow notification processor.
    */
   int movedRecordCount = 0;
   
   /**
    * number of ms that threads waited for overflow processing to complete.
    */
   long totalWaitForThis = 0L;
   
   /**
    * number of times threads waited for overflow processing to complete.
    */
   int waitForThisCount = 0;
   
   /**
    * log key below which COMMIT records will be copied forward
    * by logOverflowNotification to avoid log overflow exceptions.
    * 
    * <p>synchronized on <i> this </i>
    */
   long overflowFence = 0L;
   
   /**
    * LogEventListener registered by TM that instantiated
    * this XALogger.
    * 
    * <p>XALogger may pass some notifications on to the
    * TM via this.eventListener.
    */
   LogEventListener eventListener = null;
   
   /**
    * Set <b> true </b> in open() to indicate that
    * replay is required prior to allowing any
    * calls to put() methods.
    * 
    * <p>Calls to any put() method while
    * <i> replayNeeded </i> is true will cause
    * an exception to be thrown.
    */
   boolean replayNeeded = true;
   
   /**
    * Resize the activeTx and availableTx tables
    * to accomodate larger number of transactions
    * in the COMMITTING state.
    * 
    * <p>PRECONDITION: thread has activeTxLock shut 
    */
   private void growActiveTxArray()
   {
     // allocate new arrays
     XACommittingTx[] newActiveTx    = new XACommittingTx[activeTx.length + 50];
     XACommittingTx[] newAvailableTx = new XACommittingTx[newActiveTx.length];
 
     // initialize new elements
     for (int i = activeTx.length; i < newActiveTx.length; ++ i)
     {
       newActiveTx[i] = null;
       newAvailableTx[i] = new XACommittingTx(i);
     }
     
     // calling thread already holds the lock, so this is really not necessary
     synchronized(activeTxLock)
     {
       // copy existing entries to new tables
       for (int i = 0; i < activeTx.length; ++ i)
       {
         newActiveTx[i] = activeTx[i];
         newAvailableTx[i] = availableTx[i];
       }
       
       // update pointers
       atxPut = 0;
       atxGet = activeTx.length;
 
       // activate the new tables
       activeTx = newActiveTx;
       availableTx = newAvailableTx;
     }
     
     ++growActiveTxArrayCount;
   }
   
   /**
    * Common initialization for all constructors.
    */
   private void init()
   {
     // allocate a table of active transaction objects
     activeTx = new XACommittingTx[50];
     for (int i=0; i < activeTx.length; ++i)
       activeTx[i] = null;
     
     // allocate and initialize the table of indexes
     // for available entries in activeTx.
     // initially, all entries are available.
     availableTx = new XACommittingTx[activeTx.length];
     for (int i=0; i < activeTx.length; ++i)
       availableTx[i] = new XACommittingTx(i);
     
     // register the event listener
     super.setLogEventListener(this);
   }
   
   /**
    * Construct a Logger using default Configuration object.
    * @throws IOException
    */
   public XALogger()
     throws IOException
   {
     super(new Configuration());
     init();
   }
   
   /**
    * Construct a Logger using a Configuration supplied
    * by the caller.
    * @param config Configuration object
    * @throws IOException
    */
   public XALogger(Configuration config)
     throws IOException
   {
     super(config);
     init();
   }
   
   private void checkPutEnabled()
   throws LogClosedException
   {
     if (replayNeeded)
       throw new LogClosedException("replay() must be called prior to put(); activeMark [0x" +
           Long.toHexString(super.getActiveMark()) + "]");
   }
   
 
   /**
    * add a USER record consisting of byte[] to the log.
    * <p>waits for overflow notification processing to complete
    * prior to putting the data to the log.
    * 
    * @throws LogClosedException
    * If the TM has called open() but has not called replay().
    * Also thrown if log is actually closed.
    * Check the toString() for details.
    * 
    * @see org.objectweb.howl.log.Logger#put(byte[], boolean)
    */
   public long put(byte[] data, boolean sync)
   throws LogClosedException, LogRecordSizeException, LogFileOverflowException,
     InterruptedException, IOException
   {
     checkPutEnabled();
     
     // wait for overflow notification processor to finish.
     onpWait();
     
     return put(LogRecordType.USER, new byte[][]{data}, sync);
   }
   
   /**
    * add a USER record consisting of byte[][] to the log.
    * <p>waits for overflow notification processing to complete
    * prior to putting the data to the log.
    * 
    * @throws LogClosedException
    * If the TM has called open() but has not called replay().
    * Also thrown if log is actually closed.
    * Check the toString() for details.
    * 
    * @see org.objectweb.howl.log.Logger#put(byte[][], boolean)
    */
   public long put(byte[][] data, boolean sync)
   throws LogClosedException, LogRecordSizeException, LogFileOverflowException,
     InterruptedException, IOException
   {
     checkPutEnabled();
     
     // wait for overflow notification processor to finish.
     onpWait();
     
     return put(LogRecordType.USER, data, sync);
   }
   
   /**
    * wait for overflow notification processor to finish.
    * 
    * <p>failure to do might cause the overflow processor itself to
    * get LogFileOverflowException.
    * 
    * @throws InterruptedException
    */
   private void onpWait()
   throws InterruptedException
   {
     long beginWait = System.currentTimeMillis();
     synchronized(this)
     {
       if (this.overflowFence != 0L)
         ++waitForThisCount;
       while (this.overflowFence != 0L)
         wait();
       totalWaitForThis += (System.currentTimeMillis() - beginWait);
     }
   }
 
   /**
    * Write a begin COMMIT record to the log.
    * <p>Call blocks until the data is forced to disk.
    * 
    * @param record byte[][] containing data to be logged
    * 
    * @return XACommittingTx object to be used whe putting the DONE record.
    * 
    * @throws LogClosedException
    * If the TM has called open() but has not called replay().
    * Also thrown if log is actually closed.
    * Check the toString() for details.
    * 
    * @throws IOException
    * @throws InterruptedException
    * @throws LogFileOverflowException
    * @throws LogRecordSizeException
    */
   public XACommittingTx putCommit(byte[][] record)
   throws LogClosedException, LogRecordSizeException, LogFileOverflowException, InterruptedException, IOException
   {
     long key = 0L;
     long overflowFence = 0L;
     
     checkPutEnabled();
     
     // wait for overflow notification processor to finish.
     onpWait();
     
     /*
      * The following loop handles the (hopefully very rare) case 
      * where the call to put() returns a key that is lower than
      * the current overflowFence.  This should not happen, but
      * just in case we have a situation in which a thread is not
      * dispatched for a very long time after the record is 
      * written to the log file, it is theoretically possible that
      * the log key returned by put() could be lower than the
      * overflowFence.  If this happens, we just rewrite the
      * record.
      * 
      * Note: A very obnoxious test case using extremely small files
      * might actually put this into an infinate loop. 
      */
     do
     {
       key = put(LogRecordType.XACOMMIT, record, true);
       synchronized(this) { overflowFence = this.overflowFence; }
     } while (key < overflowFence);
     
     return activeTxAdd(key, record);
   }
   
   /**
    * Used by putCommit() and by OpenReplayListener#onRecord() 
    * to add entries to the activeTx table.
    * 
    * @param key log key for the XACOMMIT record
    * @param record byte[][] of record data
    * 
    * @return XACommitting object representing the XACOMMIT
    * data record in the log. 
    */
   private XACommittingTx activeTxAdd(long key, byte[][] record)
   {
     XACommittingTx tx = null;
     
     synchronized(activeTxLock)
     {
       // resize activeTx[] if necessary
       if (atxUsed == activeTx.length)
         growActiveTxArray();
       
       // get an available entry workerID and save reference
       tx = availableTx[atxGet];
       assert tx != null : "availableTx[" + atxGet +"] is null";
       availableTx[atxGet] = null;
       atxGet = (atxGet + 1) % activeTx.length;
 
       /*
        * update XACommittingTx with values for this COMMIT
        * 
        * DEBUG NOTE: to test tx for null, we could put a
        * breakpoint on any of the following statements and
        * set a breakpoint condition for tx == null.  However, this uses
        * a lot of CPU time and makes the debug session run
        * very slow.  It seems to be faster to wrap the code
        * it a try/catch and set a breakpoint in the catch.
        */
       try {
         tx.setLogKey(key);
         tx.setRecord(record);
         tx.setDone(false);
         tx.setMoving(false);
       } catch (NullPointerException npe) {
         throw npe;
       }
 
       int index = tx.getIndex();
       activeTx[index] = tx;
 
       // maintain statistics
       ++atxUsed;
       maxAtxUsed = Math.max(atxUsed, maxAtxUsed);
     }
     
     return tx;
   }
   
   /**
    * Write a DONE record to the log.
    * <p>Remove XACommittingTx object from the list of
    * active transactions.
    * 
    * @param record byte[][] containing data to be logged.
    * <p>If <code>record</code> is <code>null</code> then
    * no user data is written to the journal.
    * @param tx the XACommittingTx that was returned
    * by the putCommit() routine for this transaction.
    * 
    * @return long log key for the <code>record</code> byte[][].
    * <p>If <code>record</record> is null, then return is 0L.
    * 
    * @throws IOException
    * @throws InterruptedException
    * @throws LogFileOverflowException
    * @throws LogRecordSizeException
    * 
    * @throws LogClosedException
    * If the TM has called open() but has not called replay().
    * Also thrown if log is actually closed.
    * Check the toString() for details.
    * 
    * 
    */
   public long putDone(byte[][] record, XACommittingTx tx)
   throws LogClosedException, LogRecordSizeException, LogFileOverflowException, 
     InterruptedException, IOException
   {
     checkPutEnabled();
     
     assert tx != null : "XACommitingTX is null";
     
     // remove this entry from the activeTx table so overflow processor does not see it
     synchronized(activeTxLock)
     {
       int index = tx.getIndex();
       if (activeTx[index] != tx) throw new IllegalArgumentException();
       activeTx[index] = null;
     }
     
     // mark entry as DONE and wait (if necessary) for move to complete
     synchronized(tx)
     {
       // let logOverflowNotification know that this entry does not have to be moved
       tx.setDone(true);
       
       // in case logOverflowNotification got into the object first
       while (tx.isMoving())
         tx.wait();
     }
     
     long doneKey = 0L;
     if (record != null)
     {
       // write the DONE record
       do {
         try {
           doneKey = put(LogRecordType.USER, record, false);
         } catch (LogFileOverflowException e) {
           Thread.sleep(10);  
         }
       } while (doneKey == 0L);
     }
     
     // record the XADONE record with logKey from XACOMMIT record
     long xadoneKey = 0L;
     do {
       try {
         xadoneKey = put(LogRecordType.XADONE, tx.logKeyData, false);
       } catch (LogFileOverflowException e) {
         Thread.sleep(10);
       }
     } while (xadoneKey == 0L);
 
     // make entry available for re-use.
     synchronized(activeTxLock)
     {
       tx.setLogKey(0); // prevent duplicate entries in log BUG: 303907 
       availableTx[atxPut] = tx;
       atxPut = (atxPut + 1) % activeTx.length;
 
       --atxUsed;
       assert atxUsed >= 0 : "Negative atxUsed (" + atxUsed + ")";
     }
     
     return doneKey;
   }
   
   /**
    * implements LogEventListener#isLoggable().
    * @param level a logging level defined by LogEventListener
    */
   public boolean isLoggable(int level)
   {
     boolean loggable = false;  // cannot log events if caller did not provide a listener
     if (this.eventListener != null) 
       loggable = eventListener.isLoggable(level);
     
     return loggable;
   }
   
   /**
    * delegates log request to the callers event listener.
    * @param level log level
    * @param message text to be logged
    */
   public void log(int level, String message)
   {
     if (this.eventListener != null)
       eventListener.log(level, message);
   }
   
   /**
    * delegates log request to the callers event listener.
    * @param level log level
    * @param message text to be logged
    * @param thrown Throwable related to event being logged
    */
   public void log(int level, String message, Throwable thrown)
   {
     if (this.eventListener != null)
       eventListener.log(level, message, thrown);
   }
   
   /**
    * called by Logger when log file is about to overflow.
    * 
    * <p>copies XACommittingTx records forward to allow reuse
    * of older log file space.
    * 
    * <p>calls Logger.mark() to set the new active mark
    * at the completion of the scan.
    * 
    * <p>Exceptions are ignored. Hopefully they will be
    * reported to the TM when a transaction thread attempts
    * to write a log record.
    *
    * <p>While we are processing activeTx[] we publish
    * the overflowFence so putCommit knows that any record
    * it stores with a key below the fence must be re-put.
    * 
    * <p>This notification is not forwarded to the LogEventListener
    * <var> eventListener </var> that was registered by the TM.
    *  
    * @param overflowFence COMMIT records with
    * log keys lower than <i> fence </i> must
    * be moved.  All others can be ignored
    * for now.
    */
   public void logOverflowNotification(long overflowFence)
   {
     boolean logClosed = false; // BUG 300799
     
     long newMark = Long.MAX_VALUE;
     XACommittingTx tx = null;
     long txKey = 0L;
     
     if (overflowFence == 0) throw new IllegalArgumentException("overflowFence == 0");
     
     // increment number of times we are notified
     ++overflowNotificationCount;
     
     synchronized(this)
     {
       // putCommit will re-put a record if it is below the fence
       this.overflowFence = overflowFence;
     }
     
     // allow application to process this event
     if (eventListener != null)
       eventListener.logOverflowNotification(overflowFence);
     
     /*
      * walk through the activeTx table and move any
      * records that are below the overflowFence.
      */
     for (int i=0; i < activeTx.length; ++i)
     {
       synchronized(activeTxLock)
       {
         tx = activeTx[i];
         if (tx == null) continue;
       }
 
       synchronized(tx)
       {
         // gaurd against the possibility that putDone got into the object first
         if (tx.isDone()) continue;
         
         txKey = tx.getLogKey();
         if (txKey > overflowFence)
         {
           // we are not moving this entry, but it might be the new active mark
           if (txKey < newMark)
             newMark = txKey;  // remember the oldest active transaction
           continue;
         }
 
         // make putDone wait till we get this record moved
         tx.setMoving(true);
       }
       
 
       try {
         // get reference to original record
         byte[][] record = tx.getRecord();
 
         // construct an XACOMMITMOVED record
         // with room for original log key.
         //
         // original log key is used during replay to locate and
         // update the activeTx table that is maintained by this XALogger.
         //
         // NOTE: inserted key must be removed during replay
         //       before returning record to TM.
         byte[][] movedRecord = new byte[record.length + 1][];
         
         movedRecord[0] = tx.logKeyData[0];
         System.arraycopy(record, 0, movedRecord, 1, record.length);
 
         // move the COMMIT record data
         // we force all the moved records later when we set the new mark.
         // NOTE: be careful not to use the put() methods that will
         //       cause this thread to wait in onpWait().
         txKey = put(LogRecordType.XACOMMITMOVED, movedRecord, false);
         
         // keep track of the number of records moved.
         ++movedRecordCount;
         
         synchronized(tx)
         {
           tx.setLogKey(txKey);
           tx.setMoving(false);
           tx.notifyAll(); // in case putDone is waiting
         }
       } catch (LogClosedException e1) {
           // We can safely ignore the exception.
           // Restart may see moved records multiple times, but this is not a problem.
           logClosed = true;  // BUG 300799
           break;             // BUG 300799 - remove assert
       } catch (LogRecordSizeException e1)  { // ignore
           assert false : "unexpected LogRecordSizeException";
       } catch (LogFileOverflowException e1)  { // ignore
           assert false : "unexpected LogFileOverflowException";
       } catch (InterruptedException e1)  { // ignore
       } catch (IOException e1)  { // ignore
       }
     }
     
     // set new mark using oldest log key encountered during scan of activeTx[]
     try {
       // if we have not moved any records, set new mark at the beginning of next file.
       if (newMark == Long.MAX_VALUE)
         newMark = overflowFence;
       
       // BUG 300799 if we got a LogClosedException while moving records
       // then we cannot update the mark.
       if (!logClosed)
         mark(newMark, true);  // force = true
     } catch (InvalidLogKeyException e) { // should never happen
       System.err.println(e.toString());
       Thread.yield();
     } catch (LogClosedException e) { // should never happen
       assert false : "Log closed during logOverflowNotification processing";
     } catch (IOException e) {
       // ignore here - it will get caught by a transaction thread
     } catch (InterruptedException e) {
       // ignore here - it will get caught by a transaction thread
     }
 
     // let putCommit know that overflow processing is idle
     synchronized(this)
     {
       this.overflowFence = 0L;
       notifyAll();
     }
   }
   
   /**
    * return an XML node containing statistics for
    * this object along with the base Logger,
    * the LogFile pool and the LogBuffer pool.
    * 
    * @return String contiining XML document.
    */
   public String getStats()
   {
     String name = this.getClass().getName();
     StringBuffer stats = new StringBuffer(
         "\n<XALogger  class='" + name + "'>" 
     );
     
     stats.append(
         "\n<growActiveTxArrayCount value='" + growActiveTxArrayCount +
         "'>Number of times activeTx table was resized to accomodate " +
         "a larger number of transactions in COMMITTING state" +
         "</growActiveTxArrayCount>" +
         
         "\n<maxAtxUsed value='" + maxAtxUsed +
         "'>Maximum number of active TX entries used" +
         "</maxAtxUsed>" +
         
         "\n<movedRecordCount value='" + movedRecordCount + 
         "'>Number of records moved during log overflow notification processing" +
         "</movedRecordCount>" +
         
         "\n<overflowNotificationCount value='" + overflowNotificationCount + 
         "'>number of times log overflow notification event was called." +
         "</overflowNotificationCount>" +
         
         "\n<waitForThisCount value='" + getWaitForThisCount() +
         "'>Number of times threads waited for overflow processing to complete" +
         "</waitForThisCount>" +
         
         "\n<totalWaitForThis value='" + getTotalWaitForThis() +
         "'>Total time (ms) threads waited for overflow processing to complete" +
         "</totalWaitForThis>"
         );
     
     stats.append(super.getStats());
     
     stats.append("\n</XALogger>" +
         "\n");
     
     return stats.toString();
   }
   
   /**
    * Provide synchronized access to waitForThisCount.
    * @return waitForThisCount 
    */
   private final synchronized int getWaitForThisCount()
   {
     return waitForThisCount;
   }
   
   /**
    * Provide synchronized access to totalWaitForThis.
    * @return totalWaitForThis 
    */
   private final synchronized long getTotalWaitForThis()
   {
     return totalWaitForThis;
   }
 
   /**
    * Saves a reference to callers LogEventListener.
    * 
    * <p>Some LogEventListener notifications may be
    * passed onto the callers eventListener.
    * Refer to javadocs in this source
    * for individual notifications to determine if
    * the notification is passed on to the TM.
    * 
    * @param eventListener object to be notified of logger events.
    */
   public void setLogEventListener(LogEventListener eventListener)
   {
     this.eventListener = eventListener;
   }
   
   /**
    * Not supported for XALogger.
    * <p>XALogger must rebuild the activeTx table prior to allowing
    * any calls to put() methods.  To enforce this, callers must
    * use the {@link #open(ReplayListener)} method.
    * 
    * @throws UnsupportedOperationException
    */
   public void open()
   {
     throw new UnsupportedOperationException("Use open(ReplayListener) instead");
   }
   
   /**
    * calls super.open() to perform standard open functionality then
    * replays the log to rebuild the activeTx table.
    * 
    * <p>Sets replayNeeded flag to block calls to put() methods
    * until the replay is complete. (This may be unnecessary because
    * the replay blocks until it is complete, but we do this to
    * prevent the TM from trying to make log entries on another
    * thread before the log is fully open.) 
    * 
    * <p>During the replay, the XALogger calls the TM's
    * listener.onRecord() with an XALogRecord object.
    * The TM is expected to call getTx() to obtain
    * a reference to the XACommittingTx that will be
    * used to complete the transaction usin putDone()
    * later.
    * 
    * <p>Note that XALogger does not call the TM's
    * listener.getLogRecord().  
    * 
    * @param listener
    * The TM can receive replay events by
    * providing a ReplayListener.
    * TMs that do not wish to see all records during 
    * the open replay should pass a null ReplayListener.
    * @see #replayActiveTx(ReplayListener) 
    */
   public void open(ReplayListener listener)
   throws InvalidFileSetException, LogConfigurationException,
          InvalidLogBufferException, LogClosedException,
          ClassNotFoundException, IOException, InterruptedException
   {
     this.replayNeeded = true; // block put() methods until replay completes
 
     super.open();
     
     OpenReplayListener xaListener = new OpenReplayListener(listener);
     try {
       super.replay(xaListener, getActiveMark(), true); // replay CTRL records also
     } catch (InvalidLogKeyException e) {
       throw new LogClosedException(e);
     }
     
     // something very wrong if we come back from replay and we have not cleared the flag.
     if (replayNeeded)
     {
       LogClosedException lce = new LogClosedException(xaListener.replayException);
       throw lce;
     }
   }
   
   public void close() throws IOException, InterruptedException
   {
     if (isClosed) return;
     
     long newMark = Long.MAX_VALUE;
     XACommittingTx tx = null;
 
     // set new log mark at oldest activeTx entry
     synchronized(activeTxLock)
     {
       for (int i=0; i < activeTx.length; ++i)
       {
           tx = activeTx[i];
           if (tx == null) continue;
           long key = tx.getLogKey();
           if (key < newMark) newMark = key;
       }
       if (newMark < Long.MAX_VALUE)
       {
         try {
           this.mark(newMark, true);
         } catch (InvalidLogKeyException e) {
           // will not happen
         } catch (LogClosedException e) {
           // will not happen
         }
       }
     }
     super.close();
   }
   
   
   /**
    * Used by TM and test cases to obtain the
    * number of entries in the activeTx table.
    * <p>TIP: if value does not agree with expected
    * value in TM or test case, we probably have
    * a bug somewhere.
    * 
    * @return current number of used entries in activeTx table
    */
   public int getActiveTxUsed()
   {
     return this.atxUsed;
   }
   
   /**
    * displays entries in the activeTx table.
    * <p>useful for debug.
    */
   public void activeTxDisplay()
   {
     for (int i=0; i < activeTx.length; ++i)
     {
       if (activeTx[i] == null) continue;
       synchronized(activeTx)
       {
         XACommittingTx tx = activeTx[i];
         byte[][] record = tx.getRecord();
         System.out.println("activeTx[" + i + "] key=" + Long.toHexString(tx.getLogKey()) +
             "\n  Fields: " + record.length 
             );
         for (int j = 0; j < record.length; ++j)
         {
           byte[] field = record[j];
           System.out.println("  [" + j + "] len=" + field.length + ": " + new String(field));
         }
       }
     }
   }
   
   /**
    * Wrapp Logger#replay(ReplayListener) so we can
    * intercept onRecord() notifications to process
    * XACOMMIT and XACOMMITMOVED records.
    */
   public void replay(ReplayListener listener)
   throws LogConfigurationException
   {
     try {
       this.replay(listener, getActiveMark());
     } catch (InvalidLogKeyException e) {
       // should not happen
       assert false : "Unhandled InvalidLogKeyException" + e.toString();
     }
   }
   
   /**
    * Wrapp Logger#replay(ReplayListener, long) so we can
    * intercept onRecord() notifications to process
    * XACOMMIT and XACOMMITMOVED records.
    */
   public void replay(ReplayListener listener, long key)
   throws InvalidLogKeyException, LogConfigurationException
   {
     XAReplayListener replayListener = new XAReplayListener(listener);
     super.replay(replayListener, key, true);
   }
   
   /**
    * Called by the TM to receive copies of the
    * active transaction entries.
    * 
    * <p>TMs can use this method as an alternative
    * to passing a ReplayListener to open.  The
    * advantage is that only active transaction
    * entries are returned to the ReplayListener.onRecord()
    * method.
    * 
    * @param listener The activeTx entries are passed
    * to the TM through this ReplayListener.
    */
   public void replayActiveTx(ReplayListener listener)
   {
     XACommittingTx tx = null;
     XALogRecord lr = null;
     
     // scan activeTx[] for non-null entries
     for (int i=0; i < activeTx.length; ++i)
     {
       tx = activeTx[i];
       if (tx == null) continue;
       
       // found an active entry -- rebuild the XALogRecord entries
       byte[][] record = tx.getRecord();
       short recordSize = 0;
       for(int j=0; j < record.length; ++j)
       {
         // calculate size of byte[] needed for current entry
         recordSize += (2 + record[j].length);
       }
 
       lr = new XALogRecord(recordSize);
       
       // populate the LogRecord
       lr.length = recordSize;
       lr.dataBuffer.clear();
       for (int j=0; j < record.length; ++j)
       {
         lr.dataBuffer.putShort((short)record[j].length);
         lr.dataBuffer.put(record[j]);
       }
       
       lr.key = tx.getLogKey();
       lr.tod = 0L;
       lr.setTx(tx);
       lr.type = LogRecordType.XACOMMIT;
       
       // give the LogRecord to TM for processing
       listener.onRecord(lr);
     }
 
     // signal end of table
     lr = new XALogRecord(0);
     lr.type = LogRecordType.END_OF_LOG;
     listener.onRecord(lr);
   }
   
   /**
    * private class used by XALogger to replay the log during
    * log open processing.
    * 
    * <p>As log records are replayed through onRecord method,
    * the HashMap <i> activeTxHashMap </i> is updated.  XACOMMIT type
    * records are added to the activeTxHashMap, and XADONE type records
    * remove an entry.
    * 
    * <p>When the END_OF_LOG record is encountered, the
    * activeTx table is up to date and ready for
    * new entries to be added by the TM.
    * 
    * @author Michael Giroux
    */
   private class OpenReplayListener implements ReplayListener
   {
     LogRecord lr = new XALogRecord(80);
     
     final XALogger parent = XALogger.this;
     
     // set by onError() 
     LogException replayException = null;
     
     /**
      * ReplayListener registered by TM that instantiated
      * this XALogger.
      * 
      * <p>During replay, non-CTRL records are returned
      * to the TM's replayListener. 
      */
     final ReplayListener tmListener;
     
     /**
      * Used to keep track of XACOMMIT records encountered
      * during replay.  An entry is added to the activeTxHashMap when
      * XACOMMIT type record is replayed, and removed
      * when XADONE type record is replayed.
      */
     final HashMap activeTxHashMap;
     
     // counters that might be interesting to a test case
     public int unmatchedDoneCount = 0;
     public int commitCount = 0;
     public int doneCount = 0;
     public int movedCount = 0;
     
     OpenReplayListener(ReplayListener tmListener)
     {
       // FEATURE 300768 - allow null ReplayListener during open()
       
       this.tmListener = tmListener;
       activeTxHashMap = new HashMap(256);
       activeTxHashMap.clear();
     }
     
     public void onRecord(LogRecord lr)
     {
       XACommittingTx tx = null;
       int fldSize = 0;
       
       long xacommitKey = 0L; // see XACOMMITMOVED and XADONE 
       
       ((XALogRecord)lr).setTx(tx);
 
       // pass all non-CTRL records onto TM
       if (!lr.isCTRL() && tmListener != null) {
         tmListener.onRecord(lr);
         return;
       }
       
       ByteBuffer b = lr.dataBuffer;
       
       // process XACOMMIT and XADONE CTRL records
       switch(lr.type)
       {
         case LogRecordType.END_OF_LOG:
           // signal that we are done
           synchronized(this) {
             parent.replayNeeded = false;
             notifyAll();
           }
         
           // let TM look at this LogRecord
           if (tmListener != null) tmListener.onRecord(lr);
           break;
           
         case LogRecordType.XACOMMITMOVED:
           ++movedCount;
           int sz = b.getShort();
           if (sz != 8) {
             throw new IllegalArgumentException("Expected 8 found " + sz);
           }
           // QUESTION: is there a better way to handle this here?
           
           // get log key for the record that was moved
           xacommitKey = b.getLong();
           
           // get existing XACommitingTx entry from HashMap if it exists
           tx = (XACommittingTx)activeTxHashMap.remove(new Long(xacommitKey));
           if (tx != null)
           {
             // update existing entry
             tx.setLogKey(lr.key); 
 
             // TM already has seen this entry, so we do not pass this record
             break;
           }
           
           /*
            * This is the first time we have seen this XACOMMIT
            * record, so we remove the original xacommitKey
            * from the data[] buffer and drop through into
            * normal XACOMMIT processing.
            */
           int len = b.remaining();
           int start = b.position();
           System.arraycopy(lr.data, start, lr.data, 0, len);
           b.rewind();
           b.limit(len);
           // FALL THROUGH and process as XACOMMIT
           
         case LogRecordType.XACOMMIT:
           ++commitCount;
           tx = activeTxAdd(lr.key, lr.getFields());
         
           // remember this entry in the HashMap
           activeTxHashMap.put(new Long(lr.key), tx);
         
           // give XACommittingTx to TM so it can be passed to putDone
           ((XALogRecord)lr).setTx(tx);
           
           // pass commit record on to calling TM
           if (tmListener != null) tmListener.onRecord(lr);
           break;
           
         /*
          * Remove corresponding XACOMMIT from the activeTx
          * table and return.
          * 
          * The record is not passed on to the TM. 
          */  
         case LogRecordType.XADONE:
           ++doneCount;
           fldSize = b.getShort();
           if (fldSize != 8)
             throw new IllegalArgumentException("expected 8 found " + fldSize + " at record " + Long.toHexString(lr.key));
           // QUESTION: is there a better way to handle this here?
           
           xacommitKey = b.getLong();
           
           // HOWL_0_1_10 and later generate an extra bit of data for JOTM
           // Journal files created by older versions will not have this field.
           if (b.remaining() > 0) {
             fldSize = b.getShort();  // BUG 303907 added index to XADONE for diagnostics
             if (fldSize != 4)
               throw new IllegalArgumentException("expected 4 found " + fldSize + " at record " + Long.toHexString(lr.key));
             b.getInt(); // discard index
           }
           
           assert b.remaining() == 0 : "Unexpected data in XADONE record";
           /*
            * If the XACOMMIT record was recorded to a different file
            * it is possible we will not see the XACOMMIT, but we
            * may see the XADONE.  This is not an error, so just
            * ignore this XADONE if it occurs. 
            */
           tx = (XACommittingTx)activeTxHashMap.remove(new Long(xacommitKey));
           if (tx == null) {
             ++unmatchedDoneCount;
             break;
           }
           
           // remove this entry from the activeTx table
           synchronized(activeTxLock)
           {
             int index = tx.getIndex();
             if (activeTx[index] != tx) throw new IllegalArgumentException();
             activeTx[index] = null;
 
             availableTx[atxPut] = tx;
             atxPut = (atxPut + 1) % activeTx.length;
 
             --atxUsed;
             assert atxUsed >= 0 : "Negative atxUsed (" + atxUsed + ")";
           }
           
           // this is an XALogger private record, so no need to pass it on to TM
           break;
       }
     }
     
     public void onError(LogException e)
     {
       System.err.println("onError: " + e);
       replayException = e;
       // QUESTION - mark log unusable?
       
       // pass the error onto the TM
       if (tmListener != null) tmListener.onError(e);
     }
 
     public LogRecord getLogRecord()
     {
       return lr;
     }
   }
   
   /**
    * private class used by XALogger.replay() methods.
    * 
    * Used by replay(ReplayListener) and replay(ReplayListener, long) wrapper
    * methods to intercept XACOMMIT and XACOMMITMOVED records
    * so they can be passed to caller.
    * 
    * During TM invoked replay, log records are returned to the
    * caller, but the activeTx table is not updated.
    * 
    * @author Michael Giroux
    */
   private static class XAReplayListener implements ReplayListener
   {
     final LogRecord lr;
     
     /**
      * ReplayListener registered by TM that instantiated
      * this XALogger.
      * 
      * <p>During replay, non-CTRL records are returned
      * to the TM's replayListener. 
      */
     final ReplayListener tmListener;
     
     XAReplayListener(ReplayListener tmListener)
     {
       assert tmListener != null: "ReplayListener must be non-null";
       
       this.tmListener = tmListener;
       lr = tmListener.getLogRecord();
     }
     
     public void onRecord(LogRecord lr)
     {
      long xacommitKey = 0L; // see XACOMMITMOVED and XADONE 

       // set LogRecord.tx = null for TM invoked replay. 
       ((XALogRecord)lr).setTx(null);
 
       // pass all non-CTRL records onto TM
       if (! lr.isCTRL())
       {
         tmListener.onRecord(lr);
         return;
       }
       
       // process CTRL records
       switch(lr.type)
       {
         case LogRecordType.END_OF_LOG:
           tmListener.onRecord(lr);
           break;
           
         case LogRecordType.XACOMMITMOVED:
           ByteBuffer b = lr.dataBuffer;
           if (b.getShort() != 8)
             throw new IllegalArgumentException();
           // QUESTION: is there a better way to handle this here?
           
           // discard the log key for the record that was moved
           b.getLong(); 
           
           // during a TM invoked replay(), all XACOMMIT and XACOMMITMOVED
           // records are returned to the TM after removing the xacommitKey
           // that was inserted by logOverflowNotification.
           int len = b.remaining();
           int pos = b.position();
           System.arraycopy(lr.data, pos, lr.data, 0, len);
           b.rewind();
           b.limit(len);
           // FALL THROUGH and process as XACOMMIT
           
         case LogRecordType.XACOMMIT:
           // pass commit record on to calling TM
           tmListener.onRecord(lr);
           break;
           
         // All other control records, including XADONE are discarded
         default:
           break;
       }
     }
     
     public void onError(LogException e)
     {
       // pass the error onto the TM
       tmListener.onError(e);
     }
 
     public LogRecord getLogRecord()
     {
       return lr;
     }
     
   }
 }
