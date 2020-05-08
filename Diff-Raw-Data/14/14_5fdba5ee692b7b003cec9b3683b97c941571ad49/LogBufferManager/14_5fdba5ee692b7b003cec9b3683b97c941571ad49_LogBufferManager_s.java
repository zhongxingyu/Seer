 /*
  * JOnAS: Java(TM) Open Application Server
  * Copyright (C) 2004 Bull S.A.
  * Contact: jonas-team@objectweb.org
  * 
  */
 package org.objectweb.howl.log;
 
 
 import java.io.IOException;
 
 import java.lang.InterruptedException;
 
 /**
  * Provides a generalized buffer manager for journals and loggers.
  *
  * <p>log records are written to disk as blocks
  * of data.  Block size is a multiple of 512 data
  * to assure optimum disk performance.
  */
 class LogBufferManager
 {
   /**
    * mutex for synchronizing access to buffers list
    */
   private final Object bufferManagerLock = new Object();
 
   /**
    * mutex for synchronizing writes to log
    */
   private final Object forceManagerLock = new Object();
   
   /**
    * mutex for synchronizing BSN increment
    */
   private final Object bsnManagerLock = new Object();
   
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
    * buffer size specified by log -- default 4kb
    */
   private int bufferSize = 4;
 
   /**
    * number of buffers in pool
    */
   short bufferPoolSize = 2;
 
   /**
    * time interval between log force()
    */
   private int flushSleepTime = 50;
 
   /**
    * number of log Write forces
    */
   private long forceCounter = 0;
 
   /**
    * number of times there were no buffers available
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
    */
   volatile int nextWriteBSN = 1;
   
   /**
    * last BSN forced to log
    */
   volatile int lastForceBSN = 0;
   
   /**
    * number of active threads waiting for BSN increment
    */
   volatile short waitingForBSN = 0;
   
   /**
    * number of times force() called
    */
   private long forceCount = 0;
   
   /**
    * number of threads waiting for a force
    */
   private int threadsWaitingForce = 0;
   private int maxThreadsWaitingForce = 0;
   private long totalThreadsWaitingForce = 0;
   int threadsWaitingForceThreshold = Integer.MAX_VALUE;
   
   // reasons for doing force
   long forceOnTimeout = 0;
   long forceNoWaitingThreads = 0;
   long forceHalfOfBuffers = 0;
   long forceMaxWaitingThreads = 0;
 
 
 
   /**
    * thread used to flush long waiting buffers
    */
   Thread flushManager = null;
   
   /**
    * name of flush manager thread
    */
   private String flushManagerName = "FlushManager";
   
   /**
    * name of LogBuffer implementation used by this LogBufferManager instance.
    */
   String bufferClassName = null;
   
   /**
    * switch indicating whether or not to compute buffer checksums.
    * <p>configured by -Dhowl.LogBuffer.checksum
    */
   boolean doChecksum = true;
 
   /**
    * queue of buffers waiting to be written.  The queue guarantees that 
    * buffers are written to disk in BSN order.
    * <p>Buffers are added to the queue when put() detects the buffer is full.
    * <p>Buffers are removed from the queue in force() and written to disk.
    */
   private LogBuffer[] forceQueue = null;
   
   /**
    * next put index into <i> forceQueue </i>.
    */
   private volatile int fqPut = 0;
 
   /**
    * next get index from <i> forceQueue </i>.
    */
   private volatile int fqGet = 0;
   
   /**
    * forces buffer to disk.
    *
    * <p>batches multiple buffers into a single force
    * when possible.
    */
   private void force()
     throws IOException, InterruptedException
   {
     LogBuffer buffer = null;
 
     // make sure buffers are written in ascending BSN sequence
     synchronized(bsnManagerLock)
     {
       buffer = forceQueue[fqGet]; // buffer stuffed into forceQ
       fqGet = (fqGet + 1) % forceQueue.length;
       
       // write the buffer to disk (hopefully non-blocking)
       try {
         assert buffer.bsn == nextWriteBSN : "BSN error expecting " + nextWriteBSN + " found " + buffer.bsn;
         buffer.write(false);
         nextWriteBSN = buffer.bsn + 1;
       }
       catch (IOException ioe) {
         // ignore for now. buffer.iostatus is checked later for errors.
       }
     }
     
     
     threadsWaitingForce += buffer.getWaitingThreads();
     if (threadsWaitingForce > maxThreadsWaitingForce)
       maxThreadsWaitingForce = threadsWaitingForce;
 
     /*
      * if there are buffers waitingForBSN then we
      * wait for them to complete their writes
      * before we force.
      * 
      * The lastForceBSN member is updated by the thread
      * that actually does a force().  All threads
      * waiting for the force will detect the change
      * in lastForceBsn and notify any waiting threads.
      */
     synchronized(forceManagerLock)
     {
       boolean doforce = true;
       if (buffer.bsn < lastForceBSN)
       {
         doforce = false;
       }
       else if (fqGet == fqPut)
       {
         // no other threads waiting so force now
         ++forceNoWaitingThreads;
       }
       else if ((buffer.bsn - lastForceBSN) > (freeBuffer.length/2))
       {
         // one half of the buffers are waiting on the force
         ++forceHalfOfBuffers;
       }
       else if (threadsWaitingForce > threadsWaitingForceThreshold)
       {
         // number of waiting threads exceeds configured limit
         ++forceMaxWaitingThreads;
       }
       else if (Thread.currentThread().getName().equals(flushManagerName))
       {
         ++forceOnTimeout;
       }
       else
       {
         doforce = false;
       }
 
       if (doforce)
       {
         // force() is guaranteed to have forced everything that
         // has been written prior to the force, so get the
         // bsn for the last known write prior to the force
         int forcebsn = 0;
         synchronized(bsnManagerLock) { forcebsn = nextWriteBSN - 1; }
         
         ++forceCount;
         buffer.lf.force(false);
         
         lastForceBSN = forcebsn;
         totalThreadsWaitingForce += threadsWaitingForce;
         threadsWaitingForce = 0;
         forceManagerLock.notifyAll();
       }
 
       // wait for write to be forced
       while (lastForceBSN < buffer.bsn)
       {
         forceManagerLock.wait();
       }
      }
       
     if (buffer.iostatus == LogBufferStatus.WRITING)
       buffer.iostatus = LogBufferStatus.COMPLETE;
     
     // notify threads waiting for this buffer to force
     synchronized(buffer) { buffer.notifyAll(); }
 
     releaseBuffer(buffer);
   }
 
   /**
    * Waits for buffer to be forced to disk.
    *
    * <p>no monitors are owned when routine is entered.
    */
   private void sync(LogBuffer buffer)
     throws IOException, InterruptedException
   {
     try
     {
       buffer.sync();
     }
     finally
     {
       releaseBuffer(buffer);
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
   LogBuffer getLogBuffer() throws ClassNotFoundException
   {
     LogBuffer lb = null;
     Class lbcls = this.getClass().getClassLoader().loadClass(bufferClassName);
 
     try {
       lb = (LogBuffer)lbcls.newInstance();
     } catch (InstantiationException e) {
       throw new ClassNotFoundException(e.toString());
     } catch (IllegalAccessException e) {
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
           assert fqPut < forceQueue.length : "unexpected fqPut value " + fqPut;
         }
       }
 
       if (token == 0)
       {
         // force current buffer if there was no room for data
         ++noRoomInBuffer;
         force();
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
       buffer = getLogBuffer();
       buffer.configure(this, (short)-1);
     } catch (ClassNotFoundException e) {
       throw new LogConfigurationException(e.toString());
     }
     
     // get a LogRecord from caller
     LogRecord record = listener.getLogRecord();
 
     // read block containing requested mark
     try {
       lfm.read(buffer, bsnFromMark(mark));
     } catch (IOException e) {
      String msg = "Error reading " + buffer.lf.name + " @ position [" + buffer.lf.position + "]";
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
    * 
    * @see #configure()
    */
   void open()
     throws ClassNotFoundException
   {
     configure();
     
     freeBuffer = new LogBuffer[bufferPoolSize];
     for (short i=0; i< bufferPoolSize; ++i)
     {
       freeBuffer[i] = getLogBuffer();
       freeBuffer[i].configure(this, i); // TODO: should 'this' be Properties?
     }
     
     // TODO: test forceQueue
     forceQueue = new LogBuffer[bufferPoolSize + 1]; // guarantee we never overrun this queue
 
     // start a thread to flush buffers that have been waiting more than 50 ms.
     if (flushManager == null) {
       flushManager = new FlushManager(flushManagerName);
       flushManager.setDaemon(true);  // so we can shutdown while flushManager is running
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
     nextWriteBSN = nextFillBSN;
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
    * Returns an XML node containing statistics for the LogBufferManager.
    * <p>The nested <LogBufferPool> element contains entries for each
    * LogBuffer object in the buffer pool.
    * 
    * @return a String containing statistics.
    */
   String getStats()
   {
     double avgThreadsWaitingForce = (totalThreadsWaitingForce / (double)forceCount);
     String name = this.getClass().getName();
     StringBuffer stats = new StringBuffer(
            "\n<LogBufferManager  class='" + name + "'>" +
            "\n  <bufferSize value='" + this.bufferSize + "'>" +
                 "Buffer Size (in bytes)" +
                  "</bufferSize>" +
            "\n  <poolsize    value='" + freeBuffer.length + "'>" +
                 "Number of buffers in the pool" +
                 "</poolsize>" + 
            "\n  <initialPoolSize value='" + bufferPoolSize + "'>" +
                 "Initial number of buffers in the pool" +
                 "</initialPoolSize>" +
            "\n  <bufferwait  value='" + waitForBuffer     + "'>" +
                 "Wait for available buffer" +
                 "</bufferwait>" +
            "\n  <growPoolCounter value='" + growPoolCounter + "'>" +
                 "Number of times buffer pool was grown" +
                 "</growPoolCounter>" +
            "\n  <forcecount  value='" + forceCount        + "'>Number of force() calls</forcecount>" +
            "\n  <bufferfull  value='" + noRoomInBuffer    + "'>Buffer full</bufferfull>" + 
            "\n  <nextfillbsn value='" + nextFillBSN       + "'></nextfillbsn>" +
            "\n  <forceOnTimeout value='" + forceOnTimeout + "'></forceOnTimeout>" +
            "\n  <forceNoWaitingThreads value='" + forceNoWaitingThreads + "'>force because no other trheads waiting on force</forceNoWaitingThreads>" +
            "\n  <forceHalfOfBuffers value='" + forceHalfOfBuffers + "'>force due to 1/2 of buffers waiting</forceHalfOfBuffers>" +
            "\n  <forceMaxWaitingThreads value='" + forceMaxWaitingThreads + "'>force due to max waiting threads</forceMaxWaitingThreads>" +
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
    * configures the log
    */
   void configure()
   {
     // TODO: place code to read configuration file here
 
     // override default values with commandline -Dx=y values
     flushSleepTime = Integer.getInteger("howl.log.flushSleepTime",flushSleepTime).intValue();
     bufferSize = Integer.getInteger("howl.log.bufferSize",bufferSize).intValue() * 1024;
     bufferPoolSize = Integer.getInteger("howl.log.bufferPoolSize",bufferPoolSize).shortValue();
     threadsWaitingForceThreshold = Integer.getInteger("howl.log.maxWaitingThreads",threadsWaitingForceThreshold).intValue();
     bufferClassName = System.getProperty("howl.LogBuffer.class", "org.objectweb.howl.log.LogException");
     doChecksum = Boolean.getBoolean("howl.LogBuffer.checksum");
     
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
    * @return current value of bufferSize instance variable.
    */
   int getBufferSize()
   {
     return bufferSize;
   }
 
   /**
    * helper thread to flush buffers that have threads waiting
    * longer than configured maximum.
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
       
       long waitForBuffer = parent.waitForBuffer;
 
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
           long bufferWaits = parent.waitForBuffer - waitForBuffer;
           int increment = freeBuffer.length / 2;
           if (bufferWaits > increment)
           {
             // increase size of buffer pool if number of waits > 1/2 buffer pool size
             LogBuffer[] fb = new LogBuffer[freeBuffer.length + increment];
             
             ++growPoolCounter;
 
             // initialize the new slots
             boolean haveNewArray = true;
             for(int i=freeBuffer.length; i < fb.length; ++i)
             {
               try {
                 fb[i] = getLogBuffer();
                 fb[i].configure(parent, (short)i);
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
 
                 synchronized(bsnManagerLock)
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
           waitForBuffer = parent.waitForBuffer;
 
           synchronized(bufferManagerLock)
           {
             buffer = fillBuffer;
             if (buffer != null && buffer.shouldForce())
             {
               fillBuffer = null;
             }
             else
               buffer = null;
          }
 
           if (buffer != null)
           {
            synchronized(bufferManagerLock)
            {
              forceQueue[fqPut] = buffer;
              fqPut = (fqPut + 1) % forceQueue.length;
            }
               force();
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
