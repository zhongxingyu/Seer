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
 * $Id: LogBufferManager.java,v 1.28 2005-11-29 23:09:48 girouxm Exp $
  * ------------------------------------------------------------------------------
  */
 package org.objectweb.howl.log;
 
 
 import java.io.IOException;
 
 import java.lang.InterruptedException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 /**
  * Provides a generalized buffer manager for journals and loggers.
  *
  * <p>log records are written to disk as blocks
  * of data.  Block size is a multiple of 512 data
  * to assure optimum disk performance.
  */
 class LogBufferManager extends LogObject
 {
   /**
    * @param config Configuration object
    */
   LogBufferManager(Configuration config)
   {
     super(config);
     threadsWaitingForceThreshold = config.getThreadsWaitingForceThreshold();
     forceRequired = config.getLogFileMode().equals("rw");
     
     flushPartialBuffers = config.isFlushPartialBuffers();
     
     flushManager = new FlushManager(flushManagerName);
     flushManager.setDaemon(true);  // so we can shutdown while flushManager is running
     flushManager.start(); // BUG 303659 
   }
   
   /**
    * @see Configuration#flushPartialBuffers
    */
   private final boolean flushPartialBuffers; 
   
   /**
    * boolean is set <b> true </b> when an IOException is returned
    * by a write or force to a log file.
    * <p>Any attempt to write or force after <i> haveIOException </i>
    * becomes true should result in an IOException being returned
    * to the caller.
    */
   private boolean haveIOException = false;  // BUG 300803
   
   /**
    * The last IOException returned to the logger
    */
   private IOException ioexception = null;   // BUG 300803
   
   /**
    * mutex for synchronizing access to buffers list.
    * <p>also synchronizes access to fqPut in routines
    * that put LogBuffers into the forceQueue[].
    */
   private final Object bufferManagerLock = new Object();
 
   /**
    * mutex for synchronizing threads through the
    * portion of force() that forces the channel.
    */
   private final Object forceManagerLock = new Object();
   
   /**
    * reference to LogFileManager that owns this Buffer Manager instance.
    * 
    * @see LogFileManager#getLogFileForWrite(LogBuffer)
    */
   private LogFileManager lfm = null;
   
   /**
    * indicates if a force() must be called in the flush() method.
    * <p>Set false in constructor if config.getLogFileMode() is "rwd".
    */
   final boolean forceRequired;
   
   /**
    * The LogBuffer that is currently being filled.
    */
   private LogBuffer fillBuffer = null;
   
   /**
    * array of LogBuffer objects available for filling
    */
   private LogBuffer[] freeBuffer = null;
   
   /**
    * array of all LogBuffer objects allocated.
    * <p>Used to find and debug buffers that are not in the
    * freeBuffer list if logger hangs waiting
    * for buffers to be returned to the freeBuffer pool.
    */
   private LogBuffer[] bufferList = null;
 
   /**
    * workerID into freeBuffer list maintained in getBuffer.
    */
   short nextIndex = 0;
   
   /**
    * number of times there were no buffers available.
    * 
    * <p>The FlushManager thread monitors this field
    * to determine if the buffer pool needs to be
    * grown.
    */
   private long waitForBuffer = 0;
   
   /**
    * number of times buffer was forced because it is full.
    */
   private long noRoomInBuffer = 0;
   
   /**
    * number of times buffer size was increased because
    * of threads waiting for buffers.
    */
   private int growPoolCounter = 0;
 
   /**
    * next block sequence number for fillBuffer.
    */
   int nextFillBSN = 1;
 
   /**
    * next BSN to be written to log.
    * <p>synchronized by forceManagerLock
    */
   int nextWriteBSN = 1;
   
   /**
    * LogBuffer.tod from previous buffer written.
    * <p>maintained in force() method.  Used to
    * check against decrement in TOD field.
    * Added to help investigate BUG 303907
    */
   long prevWriteTOD = 0;
   
   /**
    * number of buffers waiting to be forced.
    * <p>synchronized by bufferManagerLock.
    * <p>incremented in put() and decremented in releaseBuffer().
    * When a thread calls put() with sync parameter set true,
    * and buffersWaitingForce is also zero, then put() causes
    * the buffer to be forced immediately.  This strategy
    * minimizes latency in situations of low load, such as
    * a single thread running.
    */
   int buffersWaitingForce = 0;
   
   /**
    * last BSN forced to log.
    * <p>synchronized by forceManagerLock
    */
   int lastForceBSN = 0;
   
   /**
    * number of times channel.force() called.
    */
   private long forceCount = 0;
   
   /**
    * number of times channel.write() called.
    */
   private long writeCount = 0;
   
   /**
    * minimum number of buffers forced by channel.force().
    */
   private int minBuffersForced = Integer.MAX_VALUE;
   
   /**
    * maximum number of buffers forced by channel.force()
    */
   private int maxBuffersForced = Integer.MIN_VALUE;
   
   /**
    * total amount of time spent in channel.force();
    */
   private long totalForceTime = 0;
   
   /**
    * total amount of time spent in channel.write();
    */
   private long totalWriteTime = 0;
   
   /**
    * maximum time (ms) for any single write
    */
   private long maxWriteTime = 0;
   
   /**
    * total amount of time (ms) spent waiting for the forceMangerLock
    */
   private long totalWaitForWriteLockTime = 0;
   
   /**
    * total time between channel.force() calls
    */
   private long totalTimeBetweenForce = 0;
   private long minTimeBetweenForce = Long.MAX_VALUE;
   private long maxTimeBetweenForce = Long.MIN_VALUE;
   
   /**
    * time of last force used to compute totalTimeBetweenForce
    */
   private long lastForceTOD = 0;
   
   /**
    * number of threads waiting for a force
    */
   private int threadsWaitingForce = 0;
   private int maxThreadsWaitingForce = 0;
   private long totalThreadsWaitingForce = 0;
   
   private int threadsWaitingForceThreshold = 0;
   
 
   // reasons for doing force
   long forceOnTimeout = 0;
   long forceNoWaitingThreads = 0;
   long forceHalfOfBuffers = 0;
   long forceMaxWaitingThreads = 0;
   long forceOnFileSwitch = 0;
   
   /**
    * thread used to flush long waiting buffers
    */
   final FlushManager flushManager; // BUG 303659 change type from Thread to FlushManager so we can access isClosed
   
   /**
    * name of flush manager thread
    */
   private static final String flushManagerName = "FlushManager";
   
   /**
    * queue of buffers waiting to be written.  The queue guarantees that 
    * buffers are written to disk in BSN order.  Buffers are placed into 
    * the forceQueue using the fqPut workerID, and removed from the forceQueue
    * using the fqGet workerID.  Access to these two workerID members is 
    * synchronized using separate objects to allow most threads to be
    * storing log records while a single thread is blocked waiting for
    * a physical force.
    * 
    * <p>Buffers are added to the queue when put() detects the buffer is full,
    * and when the FlushManager thread detects that a buffer has waited
    * too long to be written.  The <i> fqPut </i> member is the workerID of
    * the next location in forceQueue to put a LogBuffer that is to
    * be written.  <i> fqPut </i> is protected by <i> bufferManagerLock </i>.
    * 
    * <p>Buffers are removed from the queue in force() and written to disk.
    * The <i> fqGet </i> member is an workerID to the next buffer to remove
    * from the forceQueue.  <i> fqGet </i> is protected by
    * <i> forceManagerLock </i>.
    * 
    * <p>The size of forceQueue[] is one larger than the size of freeBuffer[]
    * so that fqPut == fqGet always means the queue is empty.
    */
   private LogBuffer[] forceQueue = null;
   
   /**
    * next put workerID into <i> forceQueue </i>.
    * <p>synchronized by bufferManagerLock.
    */
   private int fqPut = 0;
 
   /**
    * next get workerID from <i> forceQueue </i>.
    * <p>synchronized by forceManagerLock.
    */
   private int fqGet = 0;
   
   /**
    * compute elapsed time for an event
    * @param startTime time event began
    * @return elapsed time (System.currentTimeMillis() - startTime)
    */
   final long elapsedTime(long startTime)
   {
     return System.currentTimeMillis() - startTime;
   }
   
   /**
    * forces buffer to disk.
    *
    * <p>batches multiple buffers into a single force
    * when possible.
    * 
    * <p>Design Note:<br/>
    * It was suggested that using forceManagerLock to
    * control writes from the forceQueue[] and forces
    * would reduce overlap due to the amount of time
    * that forceManagerLock is shut while channel.force()
    * is active. 
    * <p>Experimented with using two separate locks to
    * manage the channel.write() and the channel.force() calls,
    * but it appears that thread calling channel.force()
    * will block another thread trying to call channel.write()
    * so both locks end up being shut anyway.
    * Since two locks did not provide any measurable benefit,
    * it seems best to use a single forceManagerLock 
    * to keep the code simple.
    */
   private void force(boolean timeout)
     throws IOException, InterruptedException
   {
     LogBuffer logBuffer = null;
     
     long startWait = System.currentTimeMillis();
     synchronized(forceManagerLock)  // write buffers in ascending BSN sequence
     { 
       totalWaitForWriteLockTime += elapsedTime(startWait);
       
       logBuffer = forceQueue[fqGet]; // logBuffer stuffed into forceQ
       forceQueue[fqGet] = null;      // so someone using debug doesn't think it is in the queue 
       fqGet = (fqGet + 1) % forceQueue.length;
       
       if (haveIOException)
       {
         // BUG 300803 - do not try the write if we already have an error
         // but we have to increment count of waitingThreads so count
         // does not go negative
         synchronized(logBuffer.waitingThreadsLock)
         {
           logBuffer.waitingThreads += 1;
         }
       }
       else
       {
         // write the logBuffer to disk (hopefully non-blocking)
         try {
           assert logBuffer.bsn == nextWriteBSN : "BSN error expecting " + nextWriteBSN + " found " + logBuffer.bsn;
           assert logBuffer.tod > prevWriteTOD : "TOD error at BSN: " + logBuffer.bsn; 
           long startWrite = System.currentTimeMillis();
           logBuffer.write();
           long writeTime = elapsedTime(startWrite);
           totalWriteTime += writeTime;
           if (writeTime > maxWriteTime) maxWriteTime = writeTime;
           ++writeCount;
           nextWriteBSN = logBuffer.bsn + 1;
         }
         catch (IOException ioe) {
           // BUG 300803 - remember that we had an error
           // BUG 303907 add a message to the IOException
           ioexception = new IOException("LogBufferManager.force(): writing " + 
               logBuffer.lf.file.getName() + "[" + ioe.getMessage() + "]");
           ioexception.setStackTrace(ioe.getStackTrace());
           haveIOException = true;
         }
       }
       
       threadsWaitingForce += logBuffer.getWaitingThreads();
       // NOTE: following is not synchronized so the stats may be inaccurate.
       if (threadsWaitingForce > maxThreadsWaitingForce)
         maxThreadsWaitingForce = threadsWaitingForce;
   
       /*
        * The lastForceBSN member is updated by the thread
        * that actually does a force().  All threads
        * waiting for the force will detect the change
        * in lastForceBSN and notify any waiting threads.
        */
 
       // force() is guaranteed to have forced everything that
       // has been written prior to the force, so get the
       // bsn for the last known write prior to the force.
       int forcebsn = nextWriteBSN - 1;
       
       boolean doforce = true;
       
       /*
        * 2004-06-25 Michael Giroux
        *   Remove test for logBuffer.bsn < forcebsn.  This cannot
        *   happen now that we stay in the forceManagerLock.
        * 
        *   Rearranged tests to improve the accuracy of the counters.
        * 
        * 2004-09-09 Michael Giroux
        *   BUG 300803 - Add test for IOException 
        */
       if (haveIOException)
       {
         doforce = false;
       }
       else if (timeout)
       {
         ++forceOnTimeout;
       }
       else if ((forcebsn - lastForceBSN) > (freeBuffer.length/2))
       {
         // one half of the buffers are waiting on the force
         ++forceHalfOfBuffers;
       }
       else if (threadsWaitingForce > threadsWaitingForceThreshold)
       {
         // number of waiting threads exceeds configured limit
         ++forceMaxWaitingThreads;
       }
       else if (logBuffer.forceNow)
       {
         // number of times we forced due to switch to next log file
         ++forceOnFileSwitch;
       }
       else if (fqGet == fqPut)
       {
         // no other logBuffers waiting in forceQueue
         ++forceNoWaitingThreads;
       }
       else
       {
         doforce = false;
       }
 
       if (doforce)
       {
         ++forceCount;
 
         long startForce = System.currentTimeMillis();
         try {
           logBuffer.lf.force(false);
         } catch (IOException ioe) {
           // BUG 303907 add a message to the IOException
           ioexception = new IOException("LogBufferManager.force(): error attempting to force " + 
               logBuffer.lf.file.getName() + "[" + ioe.getMessage() + "]");
           ioexception.setStackTrace(ioe.getStackTrace());
           haveIOException = true;
           logBuffer.ioexception = ioe;
         }
         totalForceTime += elapsedTime(startForce);
         
         if (lastForceTOD > 0)
         {
           long timeBetweenForce = startForce - lastForceTOD; 
           totalTimeBetweenForce += timeBetweenForce;
           minTimeBetweenForce = Math.min(minTimeBetweenForce, timeBetweenForce);
           if (!timeout)
           {
             maxTimeBetweenForce = Math.max(maxTimeBetweenForce, timeBetweenForce);
           }
         }
         lastForceTOD = System.currentTimeMillis();
         
         if (lastForceBSN > 0)
         {
           int buffersForced = forcebsn - lastForceBSN;
           maxBuffersForced = Math.max(maxBuffersForced, buffersForced);
           minBuffersForced = Math.min(minBuffersForced, buffersForced);
         }
         totalThreadsWaitingForce += threadsWaitingForce;
         threadsWaitingForce = 0;
         
         lastForceBSN = forcebsn;
       }
       
       // notify everyone who is waiting for the force
       if (doforce || haveIOException)
         forceManagerLock.notifyAll();
 
       // wait for thisLogBuffer's write to be forced
       while (!haveIOException && lastForceBSN < logBuffer.bsn)
       {
         forceManagerLock.wait();
       }
     } // synchronized(forceManagerLock)
     
     // notify threads waiting for this buffer to force
     synchronized(logBuffer)
     {
       // BUG: 300613 must synchronize the update of iostatus
       if (logBuffer.iostatus == LogBufferStatus.WRITING)
         logBuffer.iostatus = LogBufferStatus.COMPLETE;
       
       // BUG: 300803 report error to threads that are waiting
       if (haveIOException)
       {
         logBuffer.iostatus = LogBufferStatus.ERROR;
         logBuffer.ioexception = ioexception;
       }
 
       logBuffer.notifyAll(); 
     }
 
     releaseBuffer(logBuffer);
     
     // BUG 300803 report error to our caller
     if (haveIOException) throw ioexception;
   }
 
   /**
    * Waits for logBuffer to be forced to disk.
    *
    * <p>No monitors are owned when routine is entered.
    * <p>Prior to calling sync(), the thread called put()
    * with <i> sync </i> param set true to register
    * the fact that the thread would wait for the force.
    */
   private void sync(LogBuffer logBuffer)
     throws IOException, InterruptedException
   {
     try
     {
       logBuffer.sync();
     }
     finally
     {
       releaseBuffer(logBuffer);
     }
   }
 
   /**
    * decrements count of threads waiting on this buffer.
    * <p>If count goes to zero, buffer is returned to
    * the freeBuffer list, and any threads waiting for
    * a free buffer are notified.
    * @param buffer LogBuffer to be released
    * @see #buffersWaitingForce
    */
   private void releaseBuffer(LogBuffer buffer)
   {
     if (buffer.release() == 0)
     {
       synchronized(bufferManagerLock)
       {
         freeBuffer[buffer.index] = buffer;
         bufferManagerLock.notifyAll();
         --buffersWaitingForce;
         assert buffersWaitingForce >= 0 : "buffersWaitingForce (" + buffersWaitingForce + ") < 0";
       }
     }
   }
   
   /**
    * returns a LogBuffer to be filled.
    * 
    * <p>PRECONDITION: caller holds bufferManagerLock monitor.
    * 
    * @return a LogBuffer to be filled.
    */
   private LogBuffer getFillBuffer() throws LogFileOverflowException
   {
     if (fillBuffer == null) // slight optimization when fillBuffer != null
     {
       int fbl = freeBuffer.length;
       for(int i=0; fillBuffer == null && i < fbl; ++i)
       {
         nextIndex %= fbl;
         if (freeBuffer[nextIndex] != null)
         {
           LogBuffer b = freeBuffer[nextIndex];
           freeBuffer[nextIndex] = null;
          try {
            fillBuffer = b.init(nextFillBSN, lfm);
          } catch (LogFileOverflowException e) {
            // BUG 300956 - return buffer to free list to prevent hang in close.
            freeBuffer[nextIndex] = b;
            throw e;
          }
           ++nextFillBSN;
         }
         ++nextIndex;
       }
     }
     return fillBuffer;
   }
   
   /**
    * return a new instance of LogBuffer.
    * <p>Actual LogBuffer implementation class is specified by
    * configuration.
    * 
    * @return a new instance of LogBuffer
    */
   LogBuffer getLogBuffer(int index) throws ClassNotFoundException
   {
     LogBuffer lb = null;
     Class lbcls = this.getClass().getClassLoader().loadClass(config.getBufferClassName());
     try {
       Constructor lbCtor = lbcls.getDeclaredConstructor(new Class[] { Configuration.class } );
       lb = (LogBuffer)lbCtor.newInstance(new Object[] {config});
       lb.index = index;
     } catch (InstantiationException e) {
       throw new ClassNotFoundException(e.toString());
     } catch (IllegalAccessException e) {
       throw new ClassNotFoundException(e.toString());
     } catch (NoSuchMethodException e) {
       throw new ClassNotFoundException(e.toString());
     } catch (IllegalArgumentException e) {
       throw new ClassNotFoundException(e.toString());
     } catch (InvocationTargetException e) {
       throw new ClassNotFoundException(e.toString());
     }
     
     return lb; 
   }
   
   /**
    * Add a buffer to the forceQueue.
    * <p>PRECONDITION: bufferManagerLock owned by caller
    * @param buffer LogBuffer to be added to the forceQueue
    */
   void fqAdd(LogBuffer buffer)
   {
     fillBuffer = null;
     try {
       forceQueue[fqPut] = buffer;
     } catch (ArrayIndexOutOfBoundsException e) {
       e.printStackTrace();
       throw e;
     }
     fqPut = (fqPut + 1) % forceQueue.length;
     ++buffersWaitingForce;  // BUG 303660
   }
   
   /**
    * writes <i> data </i> byte[][] to log and returns a log key.
    * <p>waits for IO to complete if sync is true.
    * 
    * <p>MG 27/Jan/05 modified code to force buffer if caller
    * has set sync == true, and there are no buffers waiting
    * to be written.  This causes buffers to be written
    * immediately in a single threaded and/or low volume
    * situation.  Change suggested by developers at ApacheCon
    * and at ObjectWebCon.  This feature is disabled by default
    * and is enabled by setting the log configuration property
    * XXX to true.
    *
    * @return token reference (log key) for record just written
    * @throws LogRecordSizeException
    *   when size of byte[] is larger than the maximum possible
    *   record for the configured buffer size.
    * 
    * @see #buffersWaitingForce 
    */
   long put(short type, byte[][] data, boolean sync)
     throws LogRecordSizeException, LogFileOverflowException, 
                 InterruptedException, IOException
   {
     long token = 0;
     LogBuffer currentBuffer = null;
     boolean forceNow = false;
     
     do {
       // allocate the current fillBuffer
       synchronized(bufferManagerLock)
       {
         while((currentBuffer = getFillBuffer()) == null)
         {
           ++waitForBuffer;
           bufferManagerLock.wait();
         }
         
         token = currentBuffer.put(type, data, sync);
         if (sync && buffersWaitingForce == 0)
         {
           forceNow = flushPartialBuffers;
           // TODO: log  this event  level DEBUG
         }
         if (token == 0 || forceNow)
         {
           fqAdd(currentBuffer);
         }
       }
 
       if (token == 0)
       {
         // force current buffer if there was no room for data
         ++noRoomInBuffer;
         force(false);
       }
       else if (forceNow)
       {
         force(true);
         releaseBuffer(currentBuffer);
       }
       else if (sync)  // otherwise sync as requested by caller
       {
         sync(currentBuffer);
       }
 
     } while (token == 0);
 
     return token;
   }
   
   /**
    * Force the current buffer to disk
    * before starting a replay().
    */
   void forceCurrentBuffer() throws IOException
   {
     LogBuffer buffer = null;
     
     synchronized(bufferManagerLock)
     {
       if (fillBuffer != null)
       {
         buffer = fillBuffer;
         fqAdd(buffer);
       }
     } // release bufferManagerLock before we issue a force.
 
     if (buffer != null)
     {
       try {
         force(true);
       } catch (InterruptedException e) {
         ; // ignore
       }
     }
 
   }
   
   /**
    * Replays log from requested mark forward to end of log.
    * 
    * <p>Blocks caller until replay completes due to end of log, 
    * or an exception is passed to listener.onError().
    * 
    * @param listener ReplayListener to receive notifications for each log record.
    * @param mark log key for the first record to be replayed.
    * <p>If mark is zero then the entire active log is replayed.
    * @param replayCtrlRecords indicates whether to return control records.
    * <p>used by utility routines such as CopyLog.
    * 
    * @throws InvalidLogKeyException
    * if the requested key is not found in the log.
    * 
    * @see org.objectweb.howl.log.Logger#replay(ReplayListener, long)
    */
   void replay(ReplayListener listener, long mark, boolean replayCtrlRecords)
   	throws LogConfigurationException, InvalidLogKeyException
   {
     int bsn = bsnFromMark(mark);
     if (mark < 0 || (bsn == 0 && mark != 0))
       throw new InvalidLogKeyException(Long.toHexString(mark));
     
     LogBuffer buffer = null;
     
     // get a LogBuffer for reading
     try {
       buffer = getLogBuffer(-1);
     } catch (ClassNotFoundException e) {
       throw new LogConfigurationException(e.toString());
     }
     
     // get a LogRecord from caller
     LogRecord record = listener.getLogRecord();
     record.buffer = buffer;
 
     // read block containing requested mark
     try {
       forceCurrentBuffer();
       lfm.read(buffer, bsn);
     } catch (IOException e) {
       String msg = "Error reading " + buffer.lf.file + " @ position [" + buffer.lf.position + "]";
       listener.onError(new LogException(msg + e.toString()));
       return;
     } catch (InvalidLogBufferException e) {
       listener.onError(new LogException(e.toString()));
       return;
     }
 
     if (buffer.bsn == -1) {
       // BUG 300733 if mark is 0L then we must have new
       // files so return END_OF_LOG.
       // otherwise throw an InvalidLogKeyException
       if (mark == 0 || mark == lfm.getHighMark()) {
         record.type = LogRecordType.END_OF_LOG;
         listener.onRecord(record);
         return;
       }
       else {
         String msg = "The mark [" + Long.toHexString(mark) + 
         "] requested for replay was not found in the log. " +
         "activeMark is [" + Long.toHexString(lfm.activeMark) +
         "]";
         throw new InvalidLogKeyException(msg);
       }
     }
     
     // verify we have the desired block
     // if requested mark == 0 then we start with the oldest block available
     int markBSN = (mark == 0) ? buffer.bsn : bsnFromMark(mark);
     if (markBSN != buffer.bsn) {
       InvalidLogBufferException lbe = new InvalidLogBufferException(
           "block read [" + buffer.bsn + "] not block requested: " + markBSN);
       listener.onError(lbe);
       return;
     }
     
     /*
      * position buffer to requested mark.
      * 
      * Although the mark contains a buffer offset, we search forward
      * through the buffer to guarantee that we have the start
      * of a record.  This protects against using marks that were
      * not generated by the current Logger.
      */
     try {
       record.get(buffer);
       // BUG 300720 - active mark might be set to offset zero
       //              by XALogger.logOverflowNotification
       if (mark > 0 && mark > markFromBsn(markBSN,0)) {
         while(record.key < mark) {
           record.get(buffer);
         }
         if (record.key != mark) {
           String msg = "The initial mark [" + Long.toHexString(mark) + 
             "] requested for replay was not found in the log.";
           // BUG 300733 following line changed to throw an exception
           throw new InvalidLogKeyException(msg);
         }
       }
     } catch (InvalidLogBufferException e) {
       listener.onError(new LogException(e.toString()));
       return;
     }
     
     /*
      * If we get this far then we have found the requested mark.
      * Replay the log starting at the requested mark through the end of log.
      */
     long nrecs = 0;
     int nextBSN = 0;
     while (true) {
       if (record.isEOB()) {
         // read next block from log
         nextBSN = buffer.bsn + 1;
         try {
           lfm.read(buffer, nextBSN);
         } catch (IOException e) {
           listener.onError(new LogException(e.toString()));
           return;
         } catch (InvalidLogBufferException e) {
           listener.onError(new LogException(e.toString()));
           return;
         }
         
         // return end of log indicator
         if (buffer.bsn == -1 || buffer.bsn < nextBSN) {
           record.type = LogRecordType.END_OF_LOG;
           listener.onRecord(record);
           return;
         }
       }
       else if (!record.isCTRL() || replayCtrlRecords) {
         listener.onRecord(record);
       }
 
       ++nrecs;
 
       // get next record
       try {
         record.get(buffer);
       } catch (InvalidLogBufferException e) {
         listener.onError(e);
         return;
       }
     }
   }
   
   /**
    * Allocate pool of IO buffers for Logger.
    * 
    * <p>The LogBufferManager class is a generalized manager for any
    * type of LogBuffer.  The class name for the type of LogBuffer
    * to use is specified by configuration parameters.
    * 
    * @throws ClassNotFoundException
    * if the configured LogBuffer class cannot be found.
    */
   void open()
     throws ClassNotFoundException
   {
     int bufferPoolSize = config.getMinBuffers();
     freeBuffer = new LogBuffer[bufferPoolSize];
     bufferList = new LogBuffer[bufferPoolSize];
     for (short i=0; i< bufferPoolSize; ++i)
     {
       freeBuffer[i] = getLogBuffer(i);
       bufferList[i] = freeBuffer[i]; // bufferList used to debug
     }
     
     synchronized(forceManagerLock)
     {
       // one larger than bufferPoolSize to guarantee we never overrun this queue
       forceQueue = new LogBuffer[bufferPoolSize + 1];
       fqPut = 0;  // BUG 304299
       fqGet = 0;  // BUG 304299
     }
 
     // inform flushManager that LogBufferManager is ready for operation
     if (flushManager != null) {
       flushManager.isClosed = false; // BUG 303659 
     }
   }
   
   /**
    * Shutdown any threads that are started by this LogBufferManager instance.
    */
   void close()
   {
     // inform the flush manager thread
     if (flushManager != null)
       flushManager.isClosed = true; // BUG 303659
   }
   
   /**
    * perform initialization following reposition of LogFileManager.
    *
    * @param lfm LogFileManager used by the buffer manager to obtain
    * log files for writing buffers. 
    * @param bsn last Block Sequence Number written by Logger. 
    */
   void init(LogFileManager lfm, int bsn)
   {
     assert lfm != null : "LogFileManager parameter is null";
     this.lfm = lfm;
 
     nextFillBSN = bsn + 1;
     synchronized(forceManagerLock)
     {
       nextWriteBSN = nextFillBSN;
     }
   }
   
   /**
    * flush active buffers to disk and wait for all LogBuffers to
    * be returned to the freeBuffer pool.
    * 
    * <p>May be called multiple times.
    */
   void flushAll() throws IOException
   {
     LogBuffer buffer = null;
     try
     {
       // BUG 303659 prevent hang if FlushManager thread has stopped
       // move current fillBuffer to forceQueue
       synchronized(bufferManagerLock)
       {
         if (fillBuffer != null)
         {
           buffer = fillBuffer;
           fqAdd(buffer);
         }
       } // release bufferManagerLock before we issue a force.
 
       if (buffer != null)
       {
           force(true);
       }
 
       // wait until all buffers are returned to the freeBuffer pool
       for (int i=0; i < freeBuffer.length; ++i)
       {
         synchronized(bufferManagerLock)
         {
           while(freeBuffer[i] == null)
           {
             bufferManagerLock.wait(100);  // wait 100 ms at a time to avoid risk of missing a notify
           }
         }
       }
     }
     catch (InterruptedException e)
     {
       // ignore it
     }
   }
   
   /**
    * convert a double to String with fixed number of decimal places
    * @param val double to be converted
    * @param decimalPlaces number of decimal places in output
    * @return String result of conversion
    */
   private String doubleToString(double val, int decimalPlaces)
   {
     String s = "" + val;
     int dp = s.indexOf('.') + 1; // include the decimal point
     if (s.length() > dp + decimalPlaces)
     {
       s = s.substring(0, dp + decimalPlaces);
     }
     return s;
   }
   /**
    * Returns an XML node containing statistics for the LogBufferManager.
    * <p>The nested <LogBufferPool> element contains entries for each
    * LogBuffer object in the buffer pool.
    * 
    * @return a String containing statistics.
    */
   String getStats()
   {
     String avgThreadsWaitingForce = doubleToString((totalThreadsWaitingForce / (double)forceCount), 2);
     String avgForceTime = doubleToString((totalForceTime / (double)forceCount), 2);
     String avgTimeBetweenForce = doubleToString((totalTimeBetweenForce / (double)forceCount), 2);
     String avgBuffersPerForce = doubleToString((writeCount / (double) forceCount), 2);
     String avgWriteTime = doubleToString((totalWriteTime / (double)writeCount), 2);
     String avgWaitForWriteLockTime = doubleToString((totalWaitForWriteLockTime / (double)writeCount), 2);
     String name = this.getClass().getName();
     
     StringBuffer stats = new StringBuffer(
            "\n<LogBufferManager  class='" + name + "'>" +
            "\n  <bufferSize value='" + (config.getBufferSize() * 1024) + "'>Buffer Size (in bytes)</bufferSize>" + /* BUG 300957 */
            "\n  <poolsize    value='" + freeBuffer.length + "'>Number of buffers in the pool</poolsize>" + 
            "\n  <initialPoolSize value='" + config.getMinBuffers() + "'>Initial number of buffers in the pool</initialPoolSize>" +
            "\n  <growPoolCounter value='" + growPoolCounter + "'>Number of times buffer pool was grown</growPoolCounter>" +
            "\n  <bufferwait  value='" + getWaitForBuffer()     + "'>Wait for available buffer</bufferwait>" +
            "\n  <bufferfull  value='" + noRoomInBuffer    + "'>Buffer full</bufferfull>" + 
            "\n  <nextfillbsn value='" + nextFillBSN       + "'></nextfillbsn>" +
            "\n  <writeStats>" +
            "\n    <writeCount  value='" + writeCount        + "'>Number of channel.write() calls</writeCount>" +
            "\n    <totalWriteTime   value='" + totalWriteTime         + "'>Total time (ms) spent in channel.write</totalWriteTime>" +
            "\n    <avgWriteTime value='" + avgWriteTime + "'>Average channel.write() time (ms)</avgWriteTime>" + 
            "\n    <maxWriteTime value='" + maxWriteTime + "'>Maximum channel.write() time (ms)</maxWriteTime>" + 
            "\n    <totalWaitForWriteLockTime   value='" + totalWaitForWriteLockTime         + "'>Total time (ms) spent waiting for forceManagerLock to issue a write</totalWaitForWriteLockTime>" +
            "\n    <avgWaitForWriteLockTime   value='" + avgWaitForWriteLockTime         + "'>Average time (ms) spent waiting for forceManagerLock to issue a write</avgWaitForWriteLockTime>" +
            "\n  </writeStats>" +
            "\n  <forceStats>" +
            "\n    <forceCount  value='" + forceCount        + "'>Number of channel.force() calls</forceCount>" +
            "\n    <totalForceTime   value='" + totalForceTime         + "'>Total time (ms) spent in channel.force</totalForceTime>" +
            "\n    <avgForceTime value='" + avgForceTime + "'>Average channel.force() time (ms)</avgForceTime>" +
            "\n    <totalTimeBetweenForce value='" + totalTimeBetweenForce + "'>Total time (ms) between calls to channel.force()</totalTimeBetweenForce>" + 
            "\n    <minTimeBetweenForce value='" + minTimeBetweenForce + "'>Minimum time (ms) between calls to channel.force()</minTimeBetweenForce>" + 
            "\n    <maxTimeBetweenForce value='" + maxTimeBetweenForce + "'>Maximum time (ms) between calls to channel.force()</maxTimeBetweenForce>" + 
            "\n    <avgTimeBetweenForce value='" + avgTimeBetweenForce + "'>Average time (ms) between calls to channel.force()</avgTimeBetweenForce>" + 
            "\n    <avgBuffersPerForce value='" + avgBuffersPerForce + "'>Average number of buffers per force</avgBuffersPerForce>" +
            "\n    <minBuffersForced value='" + minBuffersForced + "'>Minimum number of buffers forced</minBuffersForced>" +
            "\n    <maxBuffersForced value='" + maxBuffersForced + "'>Maximum number of buffers forced</maxBuffersForced>" +
            "\n    <maxThreadsWaitingForce value='" + maxThreadsWaitingForce + "'>maximum threads waiting</maxThreadsWaitingForce>" +
            "\n    <avgThreadsWaitingForce value='" + avgThreadsWaitingForce + "'>Avg threads waiting force</avgThreadsWaitingForce>" +
            "\n  </forceStats>" +
            "\n  <forceReasons>" +
            "\n    <forceOnTimeout value='" + forceOnTimeout + "'></forceOnTimeout>" +
            "\n    <forceNoWaitingThreads value='" + forceNoWaitingThreads + "'>force because no other threads waiting on force</forceNoWaitingThreads>" +
            "\n    <forceHalfOfBuffers value='" + forceHalfOfBuffers + "'>force due to 1/2 of buffers waiting</forceHalfOfBuffers>" +
            "\n    <forceMaxWaitingThreads value='" + forceMaxWaitingThreads + "'>force due to max waiting threads</forceMaxWaitingThreads>" +
            "\n    <forceOnFileSwitch value='" + forceOnFileSwitch + "'>force last block prior to switching to next file</forceOnFileSwitch>" +
            "\n  </forceReasons>" +
            "\n  <LogBufferPool>" +
            "\n"
          );
     
     /*
      * collect stats for each buffer that is in the freeBuffer list.
      * If log is active one or more buffers will not be in the freeBuffer list.
      * The only time we can be sure that all buffers are in the list is
      * when the log is closed. 
      */
     for (int i=0; i < freeBuffer.length; ++i)
     {
       if (freeBuffer[i] != null)
         stats.append(freeBuffer[i].getStats());
     }
 
     stats.append(
          "\n</LogBufferPool>" +
          "\n</LogBufferManager>" +
       "\n"
     );
      
      return stats.toString();
   }
 
   /* ------------------------------------------------------------------
    * MBean interfaces and  methods
    * ------------------------------------------------------------------
    */
   public interface BufferPoolStatsMBean {
     public abstract int getBufferPoolCurrentSize();
     public abstract int getBufferPoolInitialSize();
     public abstract int getBufferPoolGrowCount();
   }
 
   public class BufferPoolStats implements BufferPoolStatsMBean {
     public final int getBufferPoolCurrentSize() { return freeBuffer.length; }
     public final int getBufferPoolInitialSize() { return config.getMinBuffers(); }
     public final int getBufferPoolGrowCount() { return growPoolCounter; }
   }
   
   public interface ForceStatsMBean {
     public abstract double getAverageThreadsWaitingForce();
     public abstract long getForceCount();
     public abstract double getAverageForceTime();
     public abstract int getMinTimeBetweenForce();
     public abstract int getMaxTimeBetweenForce();
     public abstract double getAverageTimeBetweenForce();
     public abstract double getAverageBuffersPerForce();
     public abstract int getMinBuffersForced();
     public abstract int getMaxBuffersForced();
     public abstract int getMaxThreadsWaitingForce();
   }
   
   public class ForceStats implements ForceStatsMBean {
     public final double getAverageThreadsWaitingForce() {
       return totalThreadsWaitingForce / (double)forceCount;
     }
     public final long getForceCount() {  return forceCount;  }
     public final double getAverageForceTime() {
       return totalForceTime / (double)forceCount;
     }
     public final int getMinTimeBetweenForce() { return (int)minTimeBetweenForce; }
     public final int getMaxTimeBetweenForce() { return (int)maxTimeBetweenForce; }
     public final double getAverageTimeBetweenForce() {
       return totalTimeBetweenForce / (double)forceCount;
     }
     public final double getAverageBuffersPerForce() {
       return writeCount / (double) forceCount;
     }
     public final int getMinBuffersForced() { return minBuffersForced; }
     public final int getMaxBuffersForced() { return maxBuffersForced; } 
     public final int getMaxThreadsWaitingForce() { return maxThreadsWaitingForce; }
   }
   
   public interface WriteStatsMBean {
     public abstract long getWriteCount();
     public abstract double getAverageWriteTime();
     public abstract double getMaximumWriteTime();
     public abstract double getAverageWaitForWriteLockTime();
     public abstract long getWaitForBuffer();
   }
   
   public class WriteStats implements WriteStatsMBean
   {
     public final long getWriteCount() { return writeCount; }
     public final double getAverageWriteTime() {
       return totalWriteTime / (double)writeCount;
     }
     public final double getMaximumWriteTime() { return maxWriteTime / 1000.0; }
     public final double getAverageWaitForWriteLockTime() {
       return totalWaitForWriteLockTime / (double)writeCount;
     }
     public final long getWaitForBuffer() {
       LogBufferManager parent = LogBufferManager.this;
       return parent.getWaitForBuffer();
     }
   }
   
   /**
    * returns the BSN value portion of a log key <i> mark </i>.
    * 
    * @param mark log key or log mark to extract BSN from.
    * 
    * @return BSN portion of <i> mark </i>
    */
   int bsnFromMark(long mark)
   {
     return (int) (mark >> 24);
   }
   
   /**
    * generate a log mark (log key).
    * @param bsn Block Sequence Number.
    * @param offset offset within block.
    * <p>May be zero to allow access to the beginning of a block.
    * @return a log key.
    */
   long markFromBsn(int bsn, int offset)
   {
     return ((long)bsn << 24) | offset;
   }
   
   /**
    * provides synchronized access to waitForBuffer
    * @return the current value of waitForBuffer
    */
   public final long getWaitForBuffer()
   {
     synchronized(bufferManagerLock)
     {
       return waitForBuffer;
     }
   }
 
   /**
    * helper thread to flush buffers that have threads waiting
    * longer than configured maximum.
    * 
    * <p>This thread is shut down by #close().
    * @see #close()
    */
   class FlushManager extends Thread
   {
     /**
      * prevents FlushManager from flushing buffers when true.
      * <p>Managed by setClosed() and tested by isClosed().</p>
      * <p>Initially true to prevent flush manager thread
      * from doing anything while open processing is going on.</p> 
      */
     boolean isClosed = true; // BUG 303659
     
     FlushManager(String name)
     {
       super(name);
     }
 
     public void run()
     {
       LogBuffer buffer = null;
       LogBufferManager parent = LogBufferManager.this;
       
       int flushSleepTime = config.getFlushSleepTime();
       
       long waitForBuffer = parent.getWaitForBuffer();
 
       for (;;)
       {
         if (interrupted()) return;
 
         try
         {
           sleep(flushSleepTime); // check for timeout every 50 ms
           
           if (isClosed) continue;  // BUG 303659 - do nothing while LogBufferManager is closed
           
           /*
            * Dynamically grow buffer pool until number of waits
            * for a buffer is less than 1/2 the pool size.
            */
           long bufferWaits = parent.getWaitForBuffer() - waitForBuffer;
           int maxBuffers = config.getMaxBuffers();
           int increment = freeBuffer.length / 2;
           if (maxBuffers > 0)
           {
             // make sure max is larger than current (min)
             maxBuffers = Math.max(maxBuffers, freeBuffer.length);
             increment = Math.min(increment, maxBuffers - freeBuffer.length);
           }
 
           if ((increment > 0) && (bufferWaits > increment))
           {
             // increase size of buffer pool if number of waits > 1/2 buffer pool size
             LogBuffer[] fb = new LogBuffer[freeBuffer.length + increment];
             LogBuffer[] bl = new LogBuffer[fb.length]; // increase size of bufferList also.
             
             ++growPoolCounter;
 
             // initialize the new slots
             boolean haveNewArray = true;
             for(int i=freeBuffer.length; i < fb.length; ++i)
             {
               try {
                 fb[i] = getLogBuffer(i);
                 bl[i] = fb[i];
               } catch (ClassNotFoundException e) {
                 haveNewArray = false;
                 break;
               }
             }
             if (haveNewArray)
             {
               LogBuffer[] fq = new LogBuffer[fb.length + 1]; // new forceQueue
               synchronized(bufferManagerLock)
               {
                 // copy bufferList to new array
                 for(int i=0; i<bufferList.length; ++i)
                   bl[i] = bufferList[i];
                 
                 // replace bufferList with new array
                 bufferList = bl;
 
                 // copy current freeBuffer array to new array
                 for(int i=0; i<freeBuffer.length; ++i)
                   fb[i] = freeBuffer[i];
 
                 // then replace freeBuffer with new array
                 freeBuffer = fb;
 
                 synchronized(forceManagerLock)
                 {
                   // copy existing force queue entries to new force queue
                   int fqx = 0;
                   while (fqGet != fqPut)
                   {
                     fq[fqx++] = forceQueue[fqGet++];
                     fqGet %= forceQueue.length;
                   }
                   forceQueue = fq;
                   fqGet = 0;
                   fqPut = fqx % forceQueue.length;  // guarantee value is valid
                 }
               }
             }
           }
           waitForBuffer = parent.getWaitForBuffer();
 
           // end of resizing buffer pool logic
           // TODO: refactor to a method
 
           synchronized(bufferManagerLock)
           {
             buffer = fillBuffer;
             if (buffer != null && buffer.shouldForce())
             {
               fqAdd(buffer);
             }
             else
               buffer = null;
           } // release bufferManagerLock before we issue a force.
 
           if (buffer != null)
           {
               parent.forceOnTimeout++;
               force(true);
           }
         }
         catch (InterruptedException e)
         {
           // we have been shut down
           return;
         }
         catch (IOException e)
         {
           // TODO: report IOException to error log
           System.err.println("FlushManager: IOException in force(true)");
         }
       }
     }
   }
 
 }
