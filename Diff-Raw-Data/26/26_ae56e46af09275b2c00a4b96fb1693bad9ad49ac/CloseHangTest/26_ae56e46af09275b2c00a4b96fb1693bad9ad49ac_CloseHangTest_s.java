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
 
 //package org.app;
 
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
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class CloseHangTest extends Configured {
   private static final Random random = new Random();
 
   private static class UploadManager {
     private static final int NUM_THREADS = 20;
     private final UploadWorker workers[] = new UploadWorker[NUM_THREADS];
     private final long lastCheckinTime[] = new long[NUM_THREADS];
 
    // base10 logarithm of the number of seconds that thread was AWOL last time we
    // printed a warning message.
    private final int lastWarningDelta[] = new int[NUM_THREADS];
 
     protected final FileSystem fs;
     private static final long MAX_CHECKIN_TIME = 30000; // must check in every 30 seconds or face a printf.
     protected static final int TEST_BYTES_LEN = 65000;
     protected static final int MAX_START_OFF = 2000;
     protected final byte[] TEST_BYTES;
 
     public UploadManager(FileSystem fs) {
       this.fs = fs;
       this.TEST_BYTES = new byte[TEST_BYTES_LEN];
       random.nextBytes(this.TEST_BYTES);
     }
 
     public void startWorkers() {
       System.out.println("starting " + NUM_THREADS + " upload worker threads.");
       for (int i = 0; i < NUM_THREADS; i++) {
         workers[i] = new UploadWorker(this, i);
         checkInWithManager(i);
         workers[i].start();
       }
     }
 
     public void monitor() {
       while (true) {
         try {
           Thread.sleep(1000);
         } catch (InterruptedException e) {
         }
         synchronized(this) {
           long curTime = System.currentTimeMillis();
           for (int workerId = 0; workerId < NUM_THREADS; workerId++) {
             // we assume no clock adjustments, etc.
             long delta = curTime - lastCheckinTime[workerId];
             if (delta > MAX_CHECKIN_TIME) {
              Double logDelta = Math.log10(delta);
               int logDeltaFloor = logDelta.intValue();
              if (logDeltaFloor > lastWarningDelta[workerId]) {
                lastWarningDelta[workerId] = logDeltaFloor;
                 System.out.println("At time " + new Date(curTime).toString() + 
                     ", WORKER " + workerId + " FAILED TO CHECK IN AFTER " +
                    delta + " seconds!");
               }
             }
           }
         }
       }
     }
 
     private synchronized void checkInWithManager(int workerId) {
       lastCheckinTime[workerId] = System.currentTimeMillis();
      lastWarningDelta[workerId] = 0;
     }
   }
 
   private static class UploadWorker extends Thread {
    private static final int CLEANUP_INTERVAL = 1000;
     private UploadManager manager;
     private int workerId;
     private long generation;
 
     public UploadWorker(UploadManager manager, int workerId) {
       this.manager = manager;
       this.workerId = workerId;
       this.generation = 0;
     }
 
     private String getFileName(long generation) {
       return "/test" + ".worker" + workerId + "." + generation;
     }
 
     public void run() {
       try {
         while (true) {
           manager.checkInWithManager(workerId);
           OutputStream outs =
             manager.fs.create(new Path(getFileName(generation)));
           DataOutputStream douts = new DataOutputStream(outs);
           douts.writeInt(random.nextInt());
           douts.writeBoolean(random.nextBoolean());
           douts.writeLong(random.nextLong());
           int startOff = random.nextInt(manager.MAX_START_OFF);
           int len = random.nextInt(manager.TEST_BYTES_LEN - startOff);
           douts.write(manager.TEST_BYTES, startOff, len);
           douts.close();
           generation++;
           if ((generation % CLEANUP_INTERVAL) == 0) {
             for (long g = generation - CLEANUP_INTERVAL; g < generation; g++) {
               manager.fs.delete(new Path(getFileName(g)), false);
             }
           }
         }
       } catch (IOException e) {
         System.out.println("********* WORKER " + workerId +
             " GOT EXCEPTION: " + e.getMessage());
         e.printStackTrace();
         System.exit(1);
       }
     }
   }
 
   public static void main(String[] args) throws Exception {
     if (args.length < 1) {
       System.out.println("CloseHangTest: must supply the HDFS uri.");
       System.exit(1);
     }
     String hdfsUri = args[0];
     final Configuration conf = new Configuration();
     FileSystem fs = FileSystem.get(new URI(hdfsUri), conf);
     UploadManager manager = new UploadManager(fs);
     manager.startWorkers();
     manager.monitor();
   }
 }
