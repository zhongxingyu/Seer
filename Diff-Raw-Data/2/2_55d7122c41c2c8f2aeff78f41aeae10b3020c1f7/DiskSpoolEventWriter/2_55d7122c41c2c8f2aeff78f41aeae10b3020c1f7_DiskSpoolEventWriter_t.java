 /*
  * Copyright 2010-2011 Ning, Inc.
  *
  * Ning licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package com.ning.metrics.serialization.writer;
 
 import com.ning.metrics.serialization.event.Event;
 import com.ning.metrics.serialization.event.EventSerializer;
 import org.apache.commons.io.FileUtils;
 import org.joda.time.Period;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.weakref.jmx.Managed;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * Disk-backed persistent queue. The DiskSpoolEventWriter writes events to disk and pass them to an EventHandler on
  * a periodic basis.
  * <p/>
  * This writer writes events to disk in a temporary spool area directly upon receive. Events are stored in flat files.
  * <p/>
  * One can control the type of writes performed by specifying one of SyncType values. For instance, if data integrity is
  * important, specify SyncType.SYNC to trigger a sync() of the disk after each write. Note that this will seriously impact
  * performance.
  * <p/>
  * Commit and forced commit have the same behavior and will promote the current file to the final spool area. Note that
  * the DiskSpoolEventWriter will never promote files automatically. To control this behavior programmatically, use ThresholdEventWriter.
  * There are also JMX knobs available.
  * <p/>
  * Periodically, events in the final spool area will be flushed to the specified EventHandler. On failure, files are moved
  * to a quarantine area. Quarantined files are never retried, except on startup.
  * <p/>
  * The rollback operation moves the current open file to the quarantine area.
  *
  * @see com.ning.metrics.serialization.writer.SyncType
  */
 public class DiskSpoolEventWriter implements EventWriter
 {
     private static final Logger log = LoggerFactory.getLogger(DiskSpoolEventWriter.class);
 
     private final AtomicLong fileId = new AtomicLong(System.currentTimeMillis() * 1000000);
     private final AtomicBoolean flushEnabled;
     private final AtomicLong flushIntervalInSeconds;
     private final EventHandler eventHandler;
     private final int rateWindowSizeMinutes;
     private final SyncType syncType;
     private final int syncBatchSize;
     private final File spoolDirectory;
     private final ScheduledExecutorService executor;
     private final File tmpSpoolDirectory;
     private final File quarantineDirectory;
     private final File lockDirectory;
     private final AtomicBoolean currentlyFlushing = new AtomicBoolean(false);
     private final AtomicLong eventSerializationFailures = new AtomicLong(0);
     private final EventRate writeRate;
     private final EventSerializer eventSerializer;
 
     private volatile ObjectOutputter currentOutputter;
     private volatile File currentOutputFile;
 
     private boolean acceptsEvents = false;
 
     public DiskSpoolEventWriter(
         final EventHandler eventHandler,
         final String spoolPath,
         final boolean flushEnabled,
         final long flushIntervalInSeconds,
         final ScheduledExecutorService executor,
         final SyncType syncType,
         final int syncBatchSize,
         final int rateWindowSizeMinutes
     )
     {
         this(eventHandler, spoolPath, flushEnabled, flushIntervalInSeconds, executor, syncType, syncBatchSize, rateWindowSizeMinutes, null);
     }
 
     public DiskSpoolEventWriter(
         final EventHandler eventHandler,
         final String spoolPath,
         final boolean flushEnabled,
         final long flushIntervalInSeconds,
         final ScheduledExecutorService executor,
         final SyncType syncType,
         final int syncBatchSize,
         final int rateWindowSizeMinutes,
         final EventSerializer eventSerializer
     )
     {
         this.eventHandler = eventHandler;
         this.rateWindowSizeMinutes = rateWindowSizeMinutes;
         this.syncType = syncType;
         this.syncBatchSize = syncBatchSize;
         this.spoolDirectory = new File(spoolPath);
         this.executor = executor;
         this.tmpSpoolDirectory = new File(spoolDirectory, "_tmp");
         this.quarantineDirectory = new File(spoolDirectory, "_quarantine");
         this.lockDirectory = new File(spoolDirectory, "_lock");
         this.flushEnabled = new AtomicBoolean(flushEnabled);
         this.flushIntervalInSeconds = new AtomicLong(flushIntervalInSeconds);
         this.eventSerializer = eventSerializer;
 
         writeRate = new EventRate(Period.minutes(rateWindowSizeMinutes));
 
         createSpoolDir(spoolDirectory);
         createSpoolDir(tmpSpoolDirectory);
         createSpoolDir(quarantineDirectory);
         createSpoolDir(lockDirectory);
 
         // Fail early
         if (!spoolDirectory.exists() || !tmpSpoolDirectory.exists() || !quarantineDirectory.exists() || !lockDirectory.exists()) {
             throw new IllegalArgumentException("Eventwriter misconfigured - couldn't create the spool directories");
         }
 
         scheduleFlush();
         recoverFiles();
 
         acceptsEvents = true;
     }
 
     private void createSpoolDir(final File dir)
     {
         if (!dir.exists() && !dir.mkdirs()) {
             log.error("unable to create spool directory {}", dir);
         }
     }
 
     private void recoverFiles()
     {
         // Only called on startup
         for (final File file : tmpSpoolDirectory.listFiles()) {
             renameFile(file, spoolDirectory);
         }
     }
 
     private void scheduleFlush()
     {
         executor.schedule(new Runnable()
         {
             @Override
             public void run()
             {
                 try {
                     flush();
                 }
                 catch (Exception e) {
                    log.error(String.format("Failed commit by %s", eventHandler.toString()), e);
                 }
                 finally {
                     final long sleepSeconds = getSpooledFileList().isEmpty() || !flushEnabled.get() ? flushIntervalInSeconds.get() : 0;
                     log.debug("Sleeping {} seconds before next flush by {}", sleepSeconds, eventHandler.toString());
 
                     try {
                         executor.schedule(this, sleepSeconds, TimeUnit.SECONDS);
                     }
                     catch (RejectedExecutionException e) {
                         // Ignore if we've been asked to shutdown
                         if (!executor.isShutdown()) {
                             throw e;
                         }
                     }
                 }
             }
         }, flushIntervalInSeconds.get(), TimeUnit.SECONDS);
     }
 
     //protected for overriding during unit tests
 
     protected List<File> getSpooledFileList()
     {
         final List<File> spooledFileList = new ArrayList<File>();
 
         for (final File file : spoolDirectory.listFiles()) {
             if (file.isFile()) {
                 spooledFileList.add(file);
             }
         }
 
         return spooledFileList;
     }
 
     @Override
     public synchronized void write(final Event event) throws IOException
     {
         if (!acceptsEvents) {
             log.warn("Writer not ready, discarding event: {}", event);
             return;
         }
 
         if (currentOutputter == null) {
             currentOutputFile = new File(tmpSpoolDirectory, String.format("%d.bin", fileId.incrementAndGet()));
 
             if (eventSerializer == null) {
                 currentOutputter = ObjectOutputterFactory.createObjectOutputter(new FileOutputStream(currentOutputFile), syncType, syncBatchSize);
             }
             else {
                 currentOutputter = ObjectOutputterFactory.createObjectOutputter(new FileOutputStream(currentOutputFile), syncType, syncBatchSize, eventSerializer);
             }
         }
 
         try {
             currentOutputter.writeObject(event);
             writeRate.increment();
         }
         catch (RuntimeException e) {
             eventSerializationFailures.incrementAndGet();
             //noinspection AccessToStaticFieldLockedOnInstance
             throw new IOException("unable to serialize event", e);
         }
         catch (IOException e) {
             eventSerializationFailures.incrementAndGet();
             //noinspection AccessToStaticFieldLockedOnInstance
             throw new IOException("unable to serialize event", e);
         }
     }
 
     @Override
     public synchronized void commit() throws IOException
     {
         forceCommit();
     }
 
     @Override
     public synchronized void forceCommit() throws IOException
     {
         if (currentOutputFile != null) {
             currentOutputter.close();
 
             renameFile(currentOutputFile, spoolDirectory);
 
             currentOutputFile = null;
             currentOutputter = null;
         }
     }
 
     @Override
     public synchronized void rollback() throws IOException
     {
         if (currentOutputFile != null) {
             currentOutputter.close();
 
             renameFile(currentOutputFile, quarantineDirectory);
 
             currentOutputFile = null;
             currentOutputter = null;
         }
     }
 
     @Override
     public synchronized void close() throws IOException
     {
         acceptsEvents = false;
 
         // Stop the flusher
         executor.shutdown();
         try {
             executor.awaitTermination(5, TimeUnit.SECONDS);
         }
         catch (InterruptedException ex) {
             Thread.currentThread().interrupt();
         }
         executor.shutdownNow();
 
         // Cleanup the current state
         forceCommit();
         flush();
     }
 
     /**
      * @return local spool path used by the writer
      */
     @Override
     public String getSpoolPath()
     {
         return spoolDirectory.getAbsolutePath();
     }
 
     @Managed(description = "Flush events (forward them to final handler)")
     public void flush()
     {
         if (!currentlyFlushing.compareAndSet(false, true)) {
             return;
         }
 
         for (final File file : getSpooledFileList()) {
             if (flushEnabled.get()) {
                 // Move files aside, to avoid sending dups (the handler can take longer than the flushing period)
                 final File lockedFile = renameFile(file, lockDirectory);
                 final CallbackHandler callbackHandler = new CallbackHandler()
                 {
                     @Override
                     public synchronized void onError(final Throwable t, final File file)
                     {
                         log.warn("Error trying to flush file {}: {}", file, t.getLocalizedMessage());
 
                         if (file != null && file.exists()) {
                             quarantineFile(lockedFile);
                         }
                     }
 
                     @Override
                     public void onSuccess(final File file)
                     {
                         // Delete the file
                         if (!file.exists()) {
                             log.warn("Trying to delete a file that does not exist: {}", file);
                         }
                         else if (!file.delete()) {
                             log.warn("Unable to delete file {}", file);
                         }
                         log.debug("Deleted [{}]", file);
                     }
                 };
 
                 try {
                     eventHandler.handle(lockedFile, callbackHandler);
                 }
                 catch (RuntimeException e) {
                     log.warn(String.format("Unknown error transferring events from local disk spool to flusher. Quarantining local file %s to directory %s", file, quarantineDirectory), e);
                     callbackHandler.onError(e, lockedFile);
                 }
             }
         }
 
         currentlyFlushing.set(false);
     }
 
     private void quarantineFile(final File file)
     {
         renameFile(file, quarantineDirectory);
     }
 
     @Managed(description = "enable/disable flushing to hdfs")
     public void setFlushEnabled(final boolean enabled)
     {
         log.info("Setting flush enabled to {}", enabled);
         flushEnabled.set(enabled);
     }
 
     @Managed(description = "check if hdfs flushing is enabled")
     public boolean getFlushEnabled()
     {
         return flushEnabled.get();
     }
 
     @Managed(description = "set the commit interval for next scheduled commit to hdfs in seconds")
     public void setFlushIntervalInSeconds(final long seconds)
     {
         log.info("setting persistent flushing to {} seconds", seconds);
         flushIntervalInSeconds.set(seconds);
     }
 
     @Managed(description = "get the current commit interval to hdfs in seconds")
     public long getFlushIntervalInSeconds()
     {
         return flushIntervalInSeconds.get();
     }
 
     @Managed(description = "size in kilobytes of disk spool queue not yet written to hdfs")
     public long getDiskSpoolSize()
     {
         long size = 0;
 
         for (final File file : getSpooledFileList()) {
             size += file.length();
         }
 
         return size / 1024;
     }
 
     @Managed(description = "size in kilobytes of quarantined data that could not be written to hdfs")
     public long getQuarantineSize()
     {
         long size = 0;
 
         for (final File file : quarantineDirectory.listFiles()) {
             size += file.length();
         }
 
         return size / 1024;
     }
 
     @Managed(description = "attempt to process quarantined files")
     public synchronized void processQuarantinedFiles()
     {
         for (final File file : quarantineDirectory.listFiles()) {
             if (file.isFile()) {
                 renameFile(file, spoolDirectory);
             }
         }
     }
 
     @Managed(description = "rate at which write() calls are succeeding to local disk")
     public long getWriteRate()
     {
         return writeRate.getRate() / rateWindowSizeMinutes;
     }
 
     @Managed(description = "count of events that could not be serialized from memory to disk")
     public long getEventSeralizationFailureCount()
     {
         return eventSerializationFailures.get();
     }
 
     private File renameFile(final File srcFile, final File destDir)
     {
         final File destinationOutputFile = new File(destDir, srcFile.getName());
 
         try {
             FileUtils.moveFile(srcFile, destinationOutputFile);
             log.debug("Moved [{}] to [{}]", srcFile, destDir);
         }
         catch (IOException e) {
             log.warn(String.format("Error renaming spool file %s to %s: %s", srcFile, destinationOutputFile, e));
         }
 
         return destinationOutputFile;
     }
 }
