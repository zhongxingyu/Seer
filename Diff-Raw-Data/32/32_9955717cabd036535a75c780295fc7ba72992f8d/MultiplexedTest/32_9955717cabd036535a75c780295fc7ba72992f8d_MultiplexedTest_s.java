 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.cloudera;
 
 import java.io.InputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.lang.Thread;
 import java.lang.System;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 import java.util.StringTokenizer;
 
 import org.apache.commons.codec.binary.Hex;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.fs.permission.FsPermission;
 import org.apache.hadoop.hdfs.server.datanode.SimulatedFSDataset;
 import org.apache.hadoop.io.IOUtils;
 import org.apache.hadoop.security.AccessControlException;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 /**
  * This benchmark tests multiplexed I/O in HDFS.
  */
 public class MultiplexedTest { //extends Configured {
   private static void usage(int retval) {
     System.err.println(
         "MultiplexedTest: tests concurrent sequential writers or readers in HDFS.\n" +
         "\n" +
         "Java system properties to set:\n" +
         "muxtest.operation [op]             Operation to perform.  Valid ops are: read, write\n" +
         "muxtest.nthreads [num-threads]     Number of threads to use.\n" +
         "muxtest.total.megs [total-size]    Total size in megs to write.  Each thread will\n" +
         "                                   read or write muxtest.total.bytes / muxtest.nthreads\n" +
         "                                   bytes.\n" +
         "muxtest.hdfs.uri [uri]             The HDFS URI to talk to.\n" +
         "\n" +
         "A few notes about configuration:\n" +
         "If you want to be sure that your reads hit the disk, you need to set\n" +
         "muxtest.total.bytes to something much higher than the available memory size.\n" +
         "Otherwise, you're mostly reading from the page cache, which may or may not\n" +
         "be what you want.\n"
     );
     System.exit(retval);
   }
 
   static int getIntOrDie(String key) {
     String val = System.getProperty(key);
     if (val == null) {
       System.err.println("You must set the integer property " + key + "\n\n");
       usage(1);
     }
     return Integer.parseInt(val);
   }
 
   static int getIntWithDefault(String key, int defaultVal) {
     String val = System.getProperty(key);
     if (val == null) {
       return defaultVal;
     }
     return Integer.parseInt(val);
   }
 
   static String getStringOrDie(String key) {
     String val = System.getProperty(key);
     if (val == null) {
       System.err.println("You must set the string property " + key + "\n\n");
       usage(1);
     }
     return val;
   }
 
   static String getStringWithDefault(String key, String defaultVal) {
     String val = System.getProperty(key);
     if (val == null) {
       return defaultVal;
     }
     return val;
   }
 
   static private class Options {
     public final boolean isWriteOperation;
     public final int nThreads;
     public final long nBytesPerThread;
     public final String uri;
 
     public Options() {
       String op = getStringOrDie("muxtest.operation");
       if (op.equals("write")) {
         this.isWriteOperation = true;
       } else if (op.equals("read")) {
         this.isWriteOperation = false;
       } else {
         throw new RuntimeException("invalid muxtest operation " + op);
       }
       this.nThreads = getIntOrDie("muxtest.nthreads");
       long totalMegs = getIntOrDie("muxtest.total.megs");
       this.nBytesPerThread = (totalMegs * 1024 * 1024) / this.nThreads;
       this.uri = getStringOrDie("muxtest.hdfs.uri");
     }
   };
 
   private static Options options;
 
   static private abstract class BaseThread extends Thread {
     private static final int TRIES_BETWEEN_TIMECHECK = 100;
     private static final int MILLISECONDS_BETWEEN_PRINT = 2000;
 
     final boolean shouldPrint;
     long remaining;
     private Throwable exception;
 
     BaseThread(boolean shouldPrint, long remaining) {
       this.shouldPrint = shouldPrint;
       this.remaining = remaining;
       this.exception = null;
     }
 
     abstract public void doRun() throws IOException;
 
     public void run() {
       try {
         int checkTimeCounter = 0;
         long prevPrintTime = 0;
 
         while (remaining > 0) {
           doRun();
           if (shouldPrint) {
             if (checkTimeCounter++ == TRIES_BETWEEN_TIMECHECK) {
               long now = System.currentTimeMillis();
               if (now > prevPrintTime + MILLISECONDS_BETWEEN_PRINT) {
                 prevPrintTime = now;
                 float percent = remaining * 100;
                 percent /= options.nBytesPerThread;
                 System.out.println("thread1: " + remaining + " bytes remaining " +
                     " (" + percent + "%)");
               }
               checkTimeCounter = 0;
             }
           }
         }
       } catch (Throwable t) {
         this.exception = t;
       }
     }
 
     public Throwable getException() {
       return exception;
     }
   }
 
   static private class WriterThread extends BaseThread {
     private final FSDataOutputStream fos;
     private final byte[] buffer;
    private final static int BUFFER_SIZE = 1024 * 1024;
 
     public static WriterThread create(int idx, FileSystem fs) throws IOException {
       boolean shouldPrint = (idx == 0);
       String fileName = String.format("/t%04d", idx);
       if (fs.exists(new Path(fileName))) {
         fs.delete(new Path(fileName));
       }
       FSDataOutputStream fos = fs.create(new Path(fileName), (short)1);
       WriterThread ret = null;
       try {
         ret = new WriterThread(shouldPrint, fos, options.nBytesPerThread);
         fos = null;
       } finally {
         if (fos != null) {
           fos.close();
         }
       }
       return ret;
     }
 
     WriterThread(boolean shouldPrint, FSDataOutputStream fos, long remaining) {
       super(shouldPrint, remaining);
       this.fos = fos;
      this.buffer = new byte[BUFFER_SIZE];
       for (int i = 0; i < this.buffer.length; i++) {
         this.buffer[i] = (byte)0;
       }
     }
 
     @Override
     public void doRun() throws IOException {
       int needed;
       if (remaining > buffer.length) {
         needed = buffer.length;
       } else {
         needed = (int)remaining;
       }
       fos.write(this.buffer, 0, needed);
       remaining -= needed;
     }
   }
 
   static private class ReaderThread extends BaseThread {
     private final FSDataInputStream fis;
     private final byte[] buffer;
    private final static int BUFFER_SIZE = 1024 * 1024;
 
     public static ReaderThread create(int idx, FileSystem fs) throws IOException {
       boolean shouldPrint = (idx == 0);
       String fileName = String.format("/t%04d", idx);
       if (!fs.exists(new Path(fileName))) {
         throw new RuntimeException("file " + fileName + " does not exist!");
       }
       FSDataInputStream fis = fs.open(new Path(fileName));
       ReaderThread ret = null;
       try {
        ret = new ReaderThread(shouldPrint, fis, options.nBytesPerThread);
         fis = null;
       } finally {
         if (fis != null) {
           fis.close();
         }
       }
       return ret;
     }
 
    ReaderThread(boolean shouldPrint, FSDataInputStream fis, long remaining) {
       super(shouldPrint, remaining);
       this.fis = fis;
      this.buffer = new byte[BUFFER_SIZE];
     }
 
     @Override
     public void doRun() throws IOException {
       int needed;
       if (remaining > buffer.length) {
         needed = buffer.length;
       } else {
         needed = (int)remaining;
       }
       int ret = fis.read(this.buffer, 0, needed);
      if (ret != -1) {
        throw new RuntimeException("got unexpected EOF after reading " + 
             (options.nBytesPerThread - remaining) + " bytes!");
       }
       remaining -= ret;
     }
   }
 
   static String prettyPrintByteSize(float size) {
     return String.format("%f MBytes", size / (1024 * 1024));
   }
 
   public static void main(String[] args) throws Exception {
     options = new Options();
     final Configuration conf = new Configuration();
     final FileSystem fs = FileSystem.get(new URI(options.uri), conf);
 
     long nanoStart = System.nanoTime();
     BaseThread threads[] = new BaseThread[options.nThreads];
     if (options.isWriteOperation) {
       for (int i = 0; i < options.nThreads; i++) {
         threads[i] = WriterThread.create(i, fs);
       }
     } else {
       for (int i = 0; i < options.nThreads; i++) {
         threads[i] = ReaderThread.create(i, fs);
       }
     }
     for (int i = 0; i < options.nThreads; i++) {
       threads[i].start();
     }
     for (int i = 0; i < options.nThreads; i++) {
       threads[i].join();
     }
     for (int i = 0; i < options.nThreads; i++) {
       Throwable t = threads[i].getException();
       if (t != null) {
         System.err.println("there were exceptions.  Aborting.");
         System.exit(1);
       }
     }
     long nanoEnd = System.nanoTime();
     fs.close();
     long totalIo = options.nThreads;
     totalIo *= options.nBytesPerThread;
     float nanoDiff = nanoEnd - nanoStart;
     float seconds = nanoDiff / 1000000000;
     float rate = totalIo / seconds;
     System.out.println(String.format("Using %d threads, average rate was %s/s\n" +
         "Total time was %f seconds",
         options.nThreads, prettyPrintByteSize(rate), seconds));
   }
 }
