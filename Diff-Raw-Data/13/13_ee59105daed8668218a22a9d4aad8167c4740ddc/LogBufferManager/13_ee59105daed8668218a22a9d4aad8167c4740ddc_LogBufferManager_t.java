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
     
     flushManager = new FlushManager(flushManagerName);
     flushManager.setDaemon(true);  // so we can shutdown while flushManager is running
   }
   
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
    * The LogBuffer that is currently being filled.
    */
   private LogBuffer fillBuffer = null;
   
   /**
    * array of LogBuffer objects available for filling
    */
   private LogBuffer[] freeBuffer = null;
 
   /**
    * index into freeBuffer list maintained in getBuffer.
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
    * number of times buffer was forced because it is full
    */
   private long noRoomInBuffer = 0;
   
   /**
    * number of times buffer size was increased because
    * of threads waiting for buffers.
    */
   private int growPoolCounter = 0;
 
   /**
    * number of fatal errors in buffer management logic
    */
   private int bufferManagerError = 0;
 
   /**
    * next block sequence number for fillBuffer
    */
   int nextFillBSN = 1;
 
   /**
    * next BSN to be written to log
    * <p>synchronized by forceManagerLock
    */
   int nextWriteBSN = 1;
   
   /**
    * last BSN forced to log
    * <p>synchronized by forceManagerLock
    */
   int lastForceBSN = 0;
   
   /**
    * number of times channel.force() called
    */
   private long forceCount = 0;
   
   /**
    * number of times channel.write() called
    */
   private long writeCount = 0;
   
   /**
    * minimum number of buffers forced by channel.force()
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
   final Thread flushManager;
   
   /**
    * name of flush manager thread
    */
   private static final String flushManagerName = "FlushManager";
   
   /**
    * queue of buffers waiting to be written.  The queue guarantees that 
    * buffers are written to disk in BSN order.  Buffers are placed into 
    * the forceQueue using the fqPut index, and removed from the forceQueue
    * using the fqGet index.  Access to these two index members is 
    * synchronized using separate objects to allow most threads to be
    * storing log records while a single thread is blocked waiting for
    * a physical force.
    * 
    * <p>Buffers are added to the queue when put() detects the buffer is full,
    * and when the FlushManager thread detects that a buffer has waited
    * too long to be written.  The <i> fqPut </i> member is the index of
    * the next location in forceQueue to put a LogBuffer that is to
    * be written.  <i> fqPut </i> is protected by <i> bufferManagerLock </i>.
    * 
    * <p>Buffers are removed from the queue in force() and written to disk.
    * The <i> fqGet </i> member is an index to the next buffer to remove
    * from the forceQueue.  <i> fqGet </i> is protected by
    * <i> forceManagerLock </i>.
    * 
    * <p>The size of forceQueue[] is one larger than the size of freeBuffer[]
    * so that fqPut == fqGet always means the queue is empty.
    */
   private LogBuffer[] forceQueue = null;
   
   /**
    * next put index into <i> forceQueue </i>.
    * <p>synchronized by bufferManagerLock.
    */
   private int fqPut = 0;
 
   /**
    * next get index from <i> forceQueue </i>.
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
     
     // make sure buffers are written in ascending BSN sequence
     long startWait = System.currentTimeMillis();
     synchronized(forceManagerLock)
     {
       totalWaitForWriteLockTime += elapsedTime(startWait);
       
       logBuffer = forceQueue[fqGet]; // logBuffer stuffed into forceQ
       fqGet = (fqGet + 1) % forceQueue.length;
       
       // write the logBuffer to disk (hopefully non-blocking)
       try {
         assert logBuffer.bsn == nextWriteBSN : "BSN error expecting " + nextWriteBSN + " found " + logBuffer.bsn;
         long startWrite = System.currentTimeMillis();
         logBuffer.write();
         long writeTime = elapsedTime(startWrite);
         totalWriteTime += writeTime;
         if (writeTime > maxWriteTime) maxWriteTime = writeTime;
         ++writeCount;
         nextWriteBSN = logBuffer.bsn + 1;
       }
       catch (IOException ioe) {
         // ignore for now. buffer.iostatus is checked later for errors.
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
        */
       if (timeout)
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
         logBuffer.lf.force(false);
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
         forceManagerLock.notifyAll();
       }
 
       // wait for thisLogBuffer's write to be forced
       while (lastForceBSN < logBuffer.bsn)
       {
         forceManagerLock.wait();
       }
      }
     
     // notify threads waiting for this buffer to force
    synchronized(logBuffer)
    {
      // BUG: 300613 must synchronize the update of iostatus
      if (logBuffer.iostatus == LogBufferStatus.WRITING)
        logBuffer.iostatus = LogBufferStatus.COMPLETE;

      logBuffer.notifyAll(); 
    }
 
     releaseBuffer(logBuffer);
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
    * decrements count of waiting threads and returns buffer
    * to freeBuffer list if count goes to zero 
    * @param buffer LogBuffer to be released
    */
   private void releaseBuffer(LogBuffer buffer)
   {
     if (buffer.release() == 0)
     {
       synchronized(bufferManagerLock)
       {
         freeBuffer[buffer.index] = buffer;
         bufferManagerLock.notifyAll();
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
           fillBuffer = b.init(nextFillBSN, lfm);
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
    * writes <i> data </i> byte[][] to log and returns a log key.
    * <p>waits for IO to complete if sync is true.
    *
    * @return token reference (log key) for record just written
    * @throws LogRecordSizeException
    *   when size of byte[] is larger than the maximum possible
    *   record for the configured buffer size. 
    */
   long put(short type, byte[][] data, boolean sync)
     throws LogRecordSizeException, LogFileOverflowException, 
                 InterruptedException, IOException
   {
     long token = 0;
     LogBuffer currentBuffer = null;
     
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
         if (token == 0)
         {
           // buffer is full -- make it unavailable to other threads until force() completes
           fillBuffer = null;
           forceQueue[fqPut] = currentBuffer;
           fqPut = (fqPut + 1) % forceQueue.length;
         }
       }
 
       if (token == 0)
       {
         // force current buffer if there was no room for data
         ++noRoomInBuffer;
         force(false);
         if (currentBuffer.iostatus == LogBufferStatus.ERROR)
           throw currentBuffer.ioexception;
       }
       else if (sync)  // otherwise sync as requested by caller
       {
         sync(currentBuffer);
       }
 
     } while (token == 0);
 
     return token;
   }
   
   /**
    * Replays log from requested mark forward to end of log.
    * 
    * @param listener ReplayListener to receive notifications for each log record.
    * @param mark log key for the first record to be replayed.
    * <p>If mark is zero then the entire active log is replayed.
    * @param replayCtrlRecords indicates whether to return control records.
    * <p>used by utility routines such as CopyLog.
    * 
    * @see org.objectweb.howl.log.Logger#replay(ReplayListener, long)
    */
   void replay(ReplayListener listener, long mark, boolean replayCtrlRecords)
   	throws LogConfigurationException, InvalidLogKeyException
   {
     if (mark < 0)
       throw new InvalidLogKeyException("log key [" + mark + "] must be >= zero");
     
     LogBuffer buffer = null;
     
     // get a LogBuffer for reading
     try {
       buffer = getLogBuffer(-1);
     } catch (ClassNotFoundException e) {
       throw new LogConfigurationException(e.toString());
     }
     
     // get a LogRecord from caller
     LogRecord record = listener.getLogRecord();
 
     // read block containing requested mark
     try {
       lfm.read(buffer, bsnFromMark(mark));
     } catch (IOException e) {
       String msg = "Error reading " + buffer.lf.file + " @ position [" + buffer.lf.position + "]";
       listener.onError(new LogException(msg + e.toString()));
       return;
     } catch (InvalidLogBufferException e) {
       listener.onError(new LogException(e.toString()));
       return;
     }
 
     // get log file containing the requested mark
     if (buffer.bsn == -1) {
       record.type = LogRecordType.END_OF_LOG;
       listener.onRecord(record);
       return;
     }
     
     // verify we have the desired block
     // if requested mark == 0 then we start with the oldest block available
     long markBSN = (mark == 0) ? buffer.bsn : bsnFromMark(mark);
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
       if (mark > 0) {
         while(record.key < mark) {
           record.get(buffer);
         }
         if (record.key != mark) {
           String msg = "The initial mark [" + Long.toHexString(mark) + 
             "] requested for replay was not found in the log.";
           listener.onError(new InvalidLogKeyException(msg));
           return;
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
         if (buffer.bsn == -1) {
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
     for (short i=0; i< bufferPoolSize; ++i)
     {
       freeBuffer[i] = getLogBuffer(i);
     }
     
     synchronized(forceManagerLock)
     {
       // one larger than bufferPoolSize to guarantee we never overrun this queue
       forceQueue = new LogBuffer[bufferPoolSize + 1];
     }
 
     // start a thread to flush buffers that have been waiting more than 50 ms.
     if (flushManager != null) {
       flushManager.start();
     }
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
     assert lfm != null : "constructor requires non-null LogFileManager parameter";
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
     try
     {
       // wait until all buffers are returned to the freeBuffer pool
       for (int i=0; i < freeBuffer.length; ++i)
       {
         synchronized(bufferManagerLock)
         {
           while(freeBuffer[i] == null)
           {
             bufferManagerLock.wait();
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
            "\n  <bufferSize value='" + config.getBufferSize() + "'>" +
                 "Buffer Size (in bytes)" +
                  "</bufferSize>" +
            "\n  <poolsize    value='" + freeBuffer.length + "'>" +
                 "Number of buffers in the pool" +
                 "</poolsize>" + 
            "\n  <initialPoolSize value='" + config.getMinBuffers() + "'>" +
                 "Initial number of buffers in the pool" +
                 "</initialPoolSize>" +
            "\n  <growPoolCounter value='" + growPoolCounter + "'>" +
                 "Number of times buffer pool was grown" +
                 "</growPoolCounter>" +
            "\n  <bufferwait  value='" + getWaitForBuffer()     + "'>" +
                 "Wait for available buffer" +
                 "</bufferwait>" +
            "\n  <forceCount  value='" + forceCount        + "'>Number of channel.force() calls</forceCount>" +
            "\n  <totalForceTime   value='" + totalForceTime         + "'>Total time (ms) spent in channel.force</totalForceTime>" +
            "\n  <avgForceTime value='" + avgForceTime + "'>Average channel.force() time (ms)</avgForceTime>" + 
            "\n  <totalTimeBetweenForce value='" + totalTimeBetweenForce + "'>Total time (ms) between calls to channel.force()</totalTimeBetweenForce>" + 
            "\n  <minTimeBetweenForce value='" + minTimeBetweenForce + "'>Minimum time (ms) between calls to channel.force()</minTimeBetweenForce>" + 
            "\n  <maxTimeBetweenForce value='" + maxTimeBetweenForce + "'>Maximum time (ms) between calls to channel.force()</maxTimeBetweenForce>" + 
            "\n  <avgTimeBetweenForce value='" + avgTimeBetweenForce + "'>Average time (ms) between calls to channel.force()</avgTimeBetweenForce>" + 
            "\n  <writeCount  value='" + writeCount        + "'>Number of channel.write() calls</writeCount>" +
            "\n  <avgBuffersPerForce value='" + avgBuffersPerForce + "'>Average number of buffers per force</avgBuffersPerForce>" +
            "\n  <minBuffersForced value='" + minBuffersForced + "'>Minimum number of buffers forced</minBuffersForced>" +
            "\n  <maxBuffersForced value='" + maxBuffersForced + "'>Maximum number of buffers forced</maxBuffersForced>" +
            "\n  <totalWriteTime   value='" + totalWriteTime         + "'>Total time (ms) spent in channel.write</totalWriteTime>" +
            "\n  <avgWriteTime value='" + avgWriteTime + "'>Average channel.write() time (ms)</avgWriteTime>" + 
            "\n  <maxWriteTime value='" + maxWriteTime + "'>Maximum channel.write() time (ms)</maxWriteTime>" + 
            "\n  <totalWaitForWriteLockTime   value='" + totalWaitForWriteLockTime         + "'>Total time (ms) spent waiting for forceManagerLock to issue a write</totalWaitForWriteLockTime>" +
            "\n  <avgWaitForWriteLockTime   value='" + avgWaitForWriteLockTime         + "'>Average time (ms) spent waiting for forceManagerLock to issue a write</avgWaitForWriteLockTime>" +
            "\n  <bufferfull  value='" + noRoomInBuffer    + "'>Buffer full</bufferfull>" + 
            "\n  <nextfillbsn value='" + nextFillBSN       + "'></nextfillbsn>" +
            "\n  <forceOnTimeout value='" + forceOnTimeout + "'></forceOnTimeout>" +
            "\n  <forceNoWaitingThreads value='" + forceNoWaitingThreads + "'>force because no other threads waiting on force</forceNoWaitingThreads>" +
            "\n  <forceHalfOfBuffers value='" + forceHalfOfBuffers + "'>force due to 1/2 of buffers waiting</forceHalfOfBuffers>" +
            "\n  <forceMaxWaitingThreads value='" + forceMaxWaitingThreads + "'>force due to max waiting threads</forceMaxWaitingThreads>" +
            "\n  <forceOnFileSwitch value='" + forceOnFileSwitch + "'>force last block prior to switching to next file</forceOnFileSwitch>" +
            "\n  <maxThreadsWaitingForce value='" + maxThreadsWaitingForce + "'>maximum threads waiting</maxThreadsWaitingForce>" +
            "\n  <avgThreadsWaitingForce value='" + avgThreadsWaitingForce + "'>Avg threads waiting force</avgThreadsWaitingForce>" +
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
   final long getWaitForBuffer()
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
    * TODO Currently this thread is never shut down.
    */
   class FlushManager extends Thread
   {
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
             
             ++growPoolCounter;
 
             // initialize the new slots
             boolean haveNewArray = true;
             for(int i=freeBuffer.length; i < fb.length; ++i)
             {
               try {
                 fb[i] = getLogBuffer(i);
               } catch (ClassNotFoundException e) {
                 haveNewArray = false;
                 break;
               }
             }
             if (haveNewArray)
             {
               LogBuffer[] fq = new LogBuffer[fb.length + 1];
               synchronized(bufferManagerLock)
               {
                 // copy original buffers to new array
                 for(int i=0; i<freeBuffer.length; ++i)
                   fb[i] = freeBuffer[i];
 
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
                   fqPut = fqx;
                 }
               }
             }
           }
           // end of resizing buffer pool
           
           waitForBuffer = parent.getWaitForBuffer();
 
           synchronized(bufferManagerLock)
           {
 
             buffer = fillBuffer;
             if (buffer != null && buffer.shouldForce())
             {
               fillBuffer = null;
               forceQueue[fqPut] = buffer;
               fqPut = (fqPut + 1) % forceQueue.length;
             }
             else
               buffer = null;
           } // release bufferManagerLock before we issue a force.
 
           if (buffer != null)
           {
               force(true);
           }
 
         }
         catch (InterruptedException e)
         {
           return;
         }
         catch (IOException e)
         {
           // TODO: report IOException to error log
         }
       }
     }
   }
 
 }
