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
 package org.objectweb.howl.log.xa;
 
 import junit.framework.TestCase;
 import org.objectweb.howl.log.Configuration;
 import org.objectweb.howl.log.LogException;
import org.objectweb.howl.log.Barrier;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * 
  * @author Michael Giroux
  */
 public class XALoggerTest extends TestCase
   implements org.objectweb.howl.log.TestDriver
 {
   XALogger log = null;
   Configuration cfg = null;
   
   Properties prop = null;
  final Barrier startBarrier = new Barrier();
  final Barrier stopBarrier = new Barrier();
   
   int workers = 0;
   
   int delayedWorkers = 0;
   
   long delayBeforeDone = 500;
   
   boolean autoMarkMode = true;
   
 
   public static void main(String[] args) {
     junit.textui.TestRunner.run(XALoggerTest.class);
   }
   
   public final Properties getProperties() { return prop; }
   
   public final org.objectweb.howl.log.Barrier getStartBarrier() { return startBarrier; }
 
   public final org.objectweb.howl.log.Barrier getStopBarrier() { return stopBarrier; }
   
   public final XALogger getXALogger() { return log; }
   
   /**
    * process properties for this test case
    * @throws FileNotFoundException
    * @throws IOException
    */
   void parseProperties() throws FileNotFoundException, IOException
   {
     String val = null;
     String key = null;
     
     prop = new Properties();
     prop.load(new FileInputStream("conf/test.properties"));
     
 
     val = prop.getProperty( key = "XALoggerTest.workers", "200" );
     workers = Integer.parseInt(val);
     if (workers <= 0) throw new IllegalArgumentException(key);
     
     val = prop.getProperty( key = "XALoggerTest.delayedWorkers", "0");
     delayedWorkers = Integer.parseInt(val);
     if (delayedWorkers < 0) throw new IllegalArgumentException(key);
     
     val = prop.getProperty( key = "XALoggerTest.delayBeforeDone", "500");
     delayBeforeDone = Long.parseLong(val);
     if (delayBeforeDone < 0) throw new IllegalArgumentException(key);
     
 }
   
   /*
    * Refresh test properties and log configuration from
    * property files.
    * <p>Individual test cases will override values as
    * needed for specific tests.
    * 
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception {
     super.setUp();
     
     parseProperties();
     cfg = new Configuration(new File("conf/log.properties"));
   }
 
   /*
    * @see TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
     super.tearDown();
   }
 
   /**
    * Constructor for XALoggerTest.
    * @param name
    */
   public XALoggerTest(String name) {
     super(name);
   }
   
   /**
    * creates worker objects and runs the test as defined by
    * the calling testXxx() routine.
    * @param workers number of XAWorker objects to create
    * @throws LogException
    * @throws Exception
    */
   private void runWorkers(int workers)
   throws LogException, Exception
   {
     if (workers <=0) throw new IllegalArgumentException();
     
     XAWorker[] xaWorker = new XAWorker[workers];
     
    startBarrier.setCount(workers + 1);
    stopBarrier.setCount(workers + 1);
 
     for (int i = 0; i < workers; ++i)
     {
       XAWorker w = new XAWorker(this);
       xaWorker[i] = w;
       if (delayedWorkers >0) 
       {
         --delayedWorkers;
         w.setDelayBeforeDone(delayBeforeDone);
       }
       w.start();
     }
 
     synchronized(startBarrier)
     {
       while (startBarrier.getCount() > 1) startBarrier.wait();
     }
 
     long startTime = System.currentTimeMillis();
     startBarrier.barrier(); // put all threads into execution.
 
     // Wait for all the workers to finish.
     stopBarrier.barrier();
     long stopTime = System.currentTimeMillis();
     
     // Collect stats from workers
     long totalBytesLogged = 0L;
     int totalLatency = 0;
     int totalTransactions = 0;
     for (int i = 0; i < workers; ++i)
     {
       XAWorker w = xaWorker[i];
       totalBytesLogged += w.bytesLogged;
       totalLatency += w.latency;
       totalTransactions += w.transactions;
       if (w.exception != null)
       {
         w.exception.printStackTrace();
         throw w.exception;
       }
     }
     
     long elapsedTime = stopTime - startTime;
     float avgLatency = (float)totalLatency / (float)totalTransactions;
     float txPerSecond = (float)(totalTransactions / (elapsedTime / 1000.0));
     
     StringBuffer stats = new StringBuffer(
         "<?xml version='1.0' ?>" +
         "\n<TestResults>"
         );
     
     // append test metrics
     stats.append(
         "\n<TestMetrics>" +
         "\n  <elapsedTime value='" + elapsedTime +
         "'>Elapsed time (ms) for run</elapsedTime>" +
         "\n  <totalTransactions value='" + totalTransactions +
         "'>Total number of transactions</totalTransactions>" +
         "\n  <txPerSecond value='" + txPerSecond +
         "'>Number of transactions per second</txPerSecond>" +
         "\n  <avgLatency value='" + avgLatency +
         "'>Average Latency</avgLatency>" +
         "\n</TestMetrics>"
         );
     
     stats.append(log.getStats());
     
     stats.append(
         "\n</TestResults>"
         );
     
     System.out.println(stats.toString());
   }
   
   /**
    * Test a single thread.
    * <p>This test verifies that a single thread is able to log
    * transactions successfully.  It depends on the 
    * LogBufferManager.FlushManager thread to force the
    * COMMIT records.
    * <p>msg.count is set to 10.
    * 
    * @throws LogException
    * @throws Exception
    */
   public void testSingleThread()
   throws LogException, Exception
   {
     log = new XALogger(cfg);
     log.open();
     log.setAutoMark(true);
     
     prop.setProperty("msg.count", "10");
     runWorkers(1);
     
     log.close();
 
   }
   
   /**
    * Test with automark TRUE so we never get overflow.
    * <p>This test drives messages through the logger and
    * confirms that the normal buffer full conditions will
    * force buffers to disk.
    * 
    * @throws LogException
    * @throws Exception
    */
   public void testAutoMarkTrue()
   throws LogException, Exception
   {
     log = new XALogger(cfg);
     log.open();
     log.setAutoMark(true);
 
     runWorkers(workers);
     
     log.close();
   }
   
   /**
    * Test with automark FALSE so we can checkout the
    * log overflow processing.
    * <p>Test uses a single delayed worker.
    * 
    * @throws LogException
    * @throws Exception
    */
   public void testAutoMarkFalseOneDelayedWorker()
   throws LogException, Exception
   {
     log = new XALogger(cfg);
     log.open();
     log.setAutoMark(false);
     
     delayedWorkers = 1;
     runWorkers(workers);
     
     log.close();
   }
 
   /**
    * Test with automark FALSE so we can checkout the
    * log overflow processing.
   * <p>Test uses four delayed workers.
    * 
    * @throws LogException
    * @throws Exception
    */
   public void testAutoMarkFalseFourDelayedWorker()
   throws LogException, Exception
   {
     log = new XALogger(cfg);
     log.open();
     log.setAutoMark(false);
     
     delayedWorkers = 4;
     runWorkers(workers);
     
     log.close();
   }
 
 }
