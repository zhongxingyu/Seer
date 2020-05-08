 /*
  * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
  * All rights reserved. This material contains unpublished, copyrighted
  * work including confidential and proprietary information of Percussion.
  *
  * com.percussion.pso.utils RxServerUtils.java
  *
  */
 package com.percussion.pso.utils;
 
 import java.io.File;
 import java.io.RandomAccessFile;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileLock;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * A collection of generally useful routines for Rhythmyx Server interactions.
  * 
  *
  * @author DavidBenua
  *
  */
 public class RxServerUtils
 {
    private static Log log = LogFactory.getLog(RxServerUtils.class);
    
    /**
     * Static methods only, never constructed. 
     */
    private RxServerUtils()
    {
       
    }
    
    /**
     * Wait for the server to be ready. 
     * When initializing or performing actions from a service on startup,
     * the Rhythmyx server may not be ready.  This routine polls for the
     * server run lock file on 10 second intervals. 
     * <p>
     * This routine should be called from a new thread only, it should
     * not block the server initialization thread. 
    * <p>
    * <em>WARNING:</em> This method works only on Windows, and should not be 
    * relied upon in cross platform code. 
     * 
     * @throws InterruptedException
     */
    public static void waitForServerReady() throws InterruptedException
    {
       FileLock lock = null; 
       boolean ready  = false; 
       while(!ready) 
       {
          File f = new File(LOCK_FILE_NAME);
          try
          {
          if(f.exists())
          {
             RandomAccessFile lockF = new RandomAccessFile(f, "rw");
             FileChannel channel = lockF.getChannel();       
             lock = channel.tryLock(0, 1, false);
             if(lock == null)
             {
                ready = true;
             }
             else
             {
                lock.release();
             }
          }
          else
          {
             log.debug("lock file does not exist"); 
          }
          }
          catch(Exception e)
          {
             log.debug("error reading lock file"); 
          }
          if(!ready)
          {
             log.debug("sleeping for lock file"); 
             Thread.sleep(LOCK_FILE_INTERVAL);
          }
       }
       log.debug("server is ready now"); 
    }
    
    private static final String LOCK_FILE_NAME = "server_run_lock";
    private static final long LOCK_FILE_INTERVAL = 10000L; 
    
 }
