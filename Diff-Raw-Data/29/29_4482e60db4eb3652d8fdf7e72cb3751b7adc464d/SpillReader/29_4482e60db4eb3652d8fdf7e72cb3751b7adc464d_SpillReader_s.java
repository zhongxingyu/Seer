 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.hbase.spill;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 
 import javax.annotation.Nonnull;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Preconditions;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.skife.config.TimeSpan;
 
 import com.nesscomputing.hbase.AbstractHBaseSupport;
 import com.nesscomputing.hbase.HBaseWriterConfig;
 import com.nesscomputing.logging.Log;
 
 public class SpillReader extends AbstractHBaseSupport implements Runnable
 {
     private static final Log LOG = Log.findLog();
 
     private AtomicBoolean taskRunning = new AtomicBoolean(true);
 
     private final AtomicReference<Thread> readerThread = new AtomicReference<Thread>(null);
 
     private final HBaseWriterConfig hbaseWriterConfig;
     private final SpillController spillController;
 
     public SpillReader(@Nonnull final HBaseWriterConfig hbaseWriterConfig,
                        @Nonnull final Configuration hadoopConfig,
                        @Nonnull final SpillController spillController)
     {
         super(hbaseWriterConfig, hadoopConfig);
 
         Preconditions.checkNotNull(spillController, "spill controller not be null!");
 
         this.hbaseWriterConfig = hbaseWriterConfig;
         this.spillController = spillController;
     }
 
     public synchronized void start()
     {
         if (spillController.isSpillingEnabled()) {
             Preconditions.checkState(readerThread.get() == null, "already started, boldly refusing to start twice!");
             Preconditions.checkState(table.get() == null, "Already have a htable object, something went very wrong!");
 
             LOG.info("Starting Spill Reader for HBase table %s.", tableName);
 
             final Thread thread = new Thread(this, String.format("hbase-%s-spill-reader", tableName));
             readerThread.set(thread);
             thread.start();
         }
     }
 
     public synchronized void stop()
     {
         final Thread thread = readerThread.getAndSet(null);
         if (thread != null) {
             LOG.info("Stopping HBase spill reader for table %s.", tableName);
             try {
                 taskRunning.set(false);
                 thread.interrupt();
                 thread.join(500L);
             }
             catch (InterruptedException ie) {
                 Thread.currentThread().interrupt();
             }
 
             disconnectHTable();
         } else {
             LOG.debug("Never started, ignoring stop()");
         }
     }
 
     @Override
     public void run()
     {
         final TimeSpan spillCycleTime = hbaseWriterConfig.getSpillCycleTime();
         LOG.info("HBase spill reader for %s starting (cycle time: %s)...", tableName, spillCycleTime);
 
         try {
             while (taskRunning.get()) {
                 runLoop();
                 Thread.sleep(spillCycleTime.getMillis());
             }
         }
         catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
         catch (Exception e) {
             LOG.warnDebug(e, "Caught exception before killing the spill reader thread!");
         }
         LOG.info("Exiting.");
     }
 
     @VisibleForTesting
     void runLoop()
         throws InterruptedException
     {
         final List<SpilledFile> spillFiles = spillController.findSpilledFiles();
 
        if (spillFiles.size() > 0) {
             try {
 
                 for (SpilledFile spillFile : spillFiles) {
                     try {
                         final HTable htable = connectHTable();
 
                         final List<Put> spilledElements = spillFile.load();
 
                         htable.put(spilledElements);
                         LOG.info("Wrote %d spilled ops from spill file %s to HBase table %s.", spilledElements.size(), spillFile.getName(), tableName);
 
                         spillController.fileOk(spillFile);
                     }
                     catch (IOException e) {
                         LOG.warn(e, "While spilling puts from %s", spillFile.getName());
                         spillController.fileFailed(spillFile);
                     }
                 }
             }
             finally {
                 disconnectHTable();
             }
         }
     }
 }
