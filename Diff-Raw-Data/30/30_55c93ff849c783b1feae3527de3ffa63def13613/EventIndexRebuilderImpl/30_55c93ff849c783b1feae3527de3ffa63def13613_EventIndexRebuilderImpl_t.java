 /*
  * Copyright (C) 2011, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.index.impl;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationListener;
 import org.zenoss.protobufs.zep.Zep.EventDetailItem;
 import org.zenoss.protobufs.zep.Zep.EventSummary;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.EventSummaryBaseDao;
 import org.zenoss.zep.dao.IndexMetadata;
 import org.zenoss.zep.dao.IndexMetadataDao;
 import org.zenoss.zep.dao.impl.DaoUtils;
 import org.zenoss.zep.events.IndexRebuildRequiredEvent;
 import org.zenoss.zep.impl.ThreadRenamingRunnable;
 import org.zenoss.zep.index.EventIndexDao;
 import org.zenoss.zep.index.EventIndexRebuilder;
 import org.zenoss.zep.index.EventIndexer;
 import org.zenoss.zep.index.IndexedDetailsConfiguration;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Utility used to rebuild the event index.
  */
 public class EventIndexRebuilderImpl implements EventIndexRebuilder, ApplicationListener<IndexRebuildRequiredEvent> {
     private static final Logger logger = LoggerFactory.getLogger(EventIndexRebuilderImpl.class);
 
     private final boolean enableIndexing;
     private int indexLimit = 1000;
 
     private EventIndexer eventIndexer;
     private EventSummaryBaseDao summaryBaseDao;
     private EventIndexDao indexDao;
     private IndexMetadataDao indexMetadataDao;
     private IndexedDetailsConfiguration indexedDetailsConfiguration;
 
     private ExecutorService executorService = Executors.newSingleThreadExecutor();
     private Future<?> rebuildFuture = null;
     private volatile boolean shutdown = false;
     private volatile boolean configurationChanged = false;
     private final Object lock = new Object();
     private byte[] indexVersionHash;
     private File indexStateFile;
 
     public EventIndexRebuilderImpl(boolean enableIndexing) {
         this.enableIndexing = enableIndexing;
     }
 
     public void init() {
         if (!enableIndexing) {
             return;
         }
         rebuildFuture = executorService.submit(new ThreadRenamingRunnable(new Runnable() {
             @Override
             public void run() {
                 logger.info("Index rebuilding thread started for: {}", indexDao.getName());
                 while (!shutdown) {
                     try {
                         configurationChanged = false;
                         Map<String,EventDetailItem> detailItems =
                                 indexedDetailsConfiguration.getEventDetailItemsByName();
                         indexVersionHash = calculateIndexVersionHash(detailItems);
                         recreateIndexIfNeeded();
 
                         // Wait to be interrupted if the configuration changes for the index
                        synchronized (lock) {
                            if (!shutdown && !configurationChanged) {
                                 lock.wait();
                             }
                         }
                     } catch (InterruptedException e) {
                         logger.info("Interrupted index rebuilding thread");
                     } catch (Exception e) {
                         logger.warn("Failed to rebuild event index", e);
                         /*
                          * If we got an error indexing or accessing the database, back off a bit and wait for any
                          * transient errors to be resolved.
                          */
                         synchronized (lock) {
                             try {
                                 lock.wait(30000L);
                             } catch (InterruptedException ie) {
                                 // Ignored
                             }
                         }
                     }
                 }
                 logger.info("Index rebuilding thread stopped for: {}", indexDao.getName());
             }
         }, "INDEX_REBUILDER_" + this.indexDao.getName().toUpperCase()));
     }
 
     public void setIndexLimit(int indexLimit) {
         this.indexLimit = indexLimit;
     }
 
     public void setIndexDir(File indexDir) {
         this.indexStateFile = new File(indexDir, ".index_state_" + indexDao.getName() + ".properties");
         if (this.enableIndexing) {
             File parentDir = this.indexStateFile.getParentFile();
             if (!parentDir.isDirectory() && !parentDir.mkdirs()) {
                 logger.warn("Failed to create parent directory: {}", parentDir.getAbsolutePath());
             }
         }
     }
 
     public void setEventIndexer(EventIndexer eventIndexer) {
         this.eventIndexer = eventIndexer;
     }
 
     public void setSummaryBaseDao(EventSummaryBaseDao summaryBaseDao) {
         this.summaryBaseDao = summaryBaseDao;
     }
 
     public void setIndexDao(EventIndexDao indexDao) {
         this.indexDao = indexDao;
     }
 
     public void setIndexMetadataDao(IndexMetadataDao indexMetadataDao) {
         this.indexMetadataDao = indexMetadataDao;
     }
 
     public void setIndexedDetailsConfiguration(IndexedDetailsConfiguration indexedDetailsConfiguration) {
         this.indexedDetailsConfiguration = indexedDetailsConfiguration;
     }
 
     private void deleteStateFile() {
         if (this.indexStateFile.isFile() && !this.indexStateFile.delete()) {
             logger.info("Failed to remove index rebuild state file");
         }
     }
 
     private static byte[] calculateIndexVersionHash(Map<String,EventDetailItem> detailItems) throws ZepException {
         TreeMap<String,EventDetailItem> sorted = new TreeMap<String,EventDetailItem>(detailItems);
         StringBuilder indexConfigStr = new StringBuilder();
         for (EventDetailItem item : sorted.values()) {
             // Only key and type affect the indexing behavior - ignore changes to display name
             indexConfigStr.append('|');
             indexConfigStr.append(item.getKey());
             indexConfigStr.append('|');
             indexConfigStr.append(item.getType().name());
             indexConfigStr.append('|');
         }
         if (indexConfigStr.length() == 0) {
             return null;
         }
         return DaoUtils.sha1(indexConfigStr.toString());
     }
 
     public void shutdown() throws InterruptedException {
         this.shutdown = true;
         synchronized (this.lock) {
             this.lock.notifyAll();
         }
         if (rebuildFuture != null) {
             try {
                 rebuildFuture.get();
             } catch (ExecutionException e) {
                 logger.warn("Error rebuilding event index", e);
             }
         }
         this.executorService.shutdown();
         this.executorService.awaitTermination(0, TimeUnit.SECONDS);
     }
 
     private void recreateIndexIfNeeded() throws ZepException, InterruptedException {
         final IndexMetadata indexMetadata = indexMetadataDao.findIndexMetadata(indexDao.getName());
         final int numDocs = indexDao.getNumDocs();
 
         boolean recreateIndex = false;
         IndexRebuildState indexRebuildState = null;
 
         // Stop the event indexer if it is currently running.
         eventIndexer.stop();
 
         // Recreate index if we detect an empty index (could have been wiped from disk) - This check only happens once
         if (numDocs == 0) {
             logger.info("Empty index detected.");
             deleteStateFile();
             recreateIndex = true;
         }
         // Recreate index if we detect that the schema was cleared (wiped record of indexing state)
         else if (indexMetadata == null) {
             if (numDocs > 0) {
                 logger.info("Inconsistent state between index and database. Clearing index.");
                 indexDao.clear();
             }
             deleteStateFile();
             recreateIndex = true;
         }
         // Recreate index if the version number or indexed details changed
         else if (indexVersionChanged(indexMetadata)) {
             recreateIndex = true;
             try {
                 indexRebuildState = IndexRebuildState.loadState(this.indexStateFile);
                 if (indexRebuildState != null) {
                     if (indexRebuildState.getIndexVersion() != IndexConstants.INDEX_VERSION ||
                         !Arrays.equals(indexRebuildState.getIndexVersionHash(), this.indexVersionHash)) {
                         // We have state from an previous version / hash - ignore it
                         indexRebuildState = null;
                         deleteStateFile();
                     }
                 }
             } catch (Exception e) {
                 logger.warn("Failed to restore index rebuild state from: " + this.indexStateFile.getAbsolutePath(),
                         e);
             }
         }
 
         if (recreateIndex) {
             /*
              * We add dummy index metadata here to ensure that if ZEP is stopped while rebuilding the index, we will be
              * able to restart the indexing process.
              */
             byte[] checksum = new byte[20];
             Arrays.fill(checksum, (byte) 0);
             this.indexMetadataDao.updateIndexVersion(this.indexDao.getName(), 0, checksum);
 
             // We want to start the event indexer before we start the rebuild (we want both to run in parallel).
             eventIndexer.start();
 
             recreateIndexFromDatabase(indexRebuildState);
         }
         else {
             // Start the event indexer - we have done all the necessary initialization.
             eventIndexer.start();
         }
     }
 
     private boolean indexVersionChanged(IndexMetadata indexMetadata) {
         boolean changed = false;
         if (IndexConstants.INDEX_VERSION != indexMetadata.getIndexVersion()) {
             logger.info("Index version changed: previous={}, new={}", indexMetadata.getIndexVersion(),
                     IndexConstants.INDEX_VERSION);
             changed = true;
         }
         else if (!Arrays.equals(this.indexVersionHash, indexMetadata.getIndexVersionHash())) {
             logger.info("Index configuration changed.");
             changed = true;
         }
         return changed;
     }
 
     private void recreateIndexFromDatabase(IndexRebuildState indexRebuildState) throws ZepException {
         logger.info("Recreating index for table {}", indexDao.getName());
         String startingUuid = null;
         final long throughTime;
 
         if (indexRebuildState == null) {
             throughTime = System.currentTimeMillis();
             indexRebuildState = new IndexRebuildState(IndexConstants.INDEX_VERSION, this.indexVersionHash,
                     throughTime, null);
         }
         else {
             startingUuid = indexRebuildState.getStartingUuid();
             throughTime = indexRebuildState.getThroughTime();
             logger.info("Resuming event indexing from UUID: {} and time: {}", startingUuid, throughTime);
         }
 
         int i = 0;
         int numIndexed = 0;
         List<EventSummary> events;
         do {
             events = summaryBaseDao.listBatch(startingUuid, throughTime, indexLimit);
             i++;
             for (EventSummary event : events) {
                 indexDao.stage(event);
                 startingUuid = event.getUuid();
                 ++numIndexed;
                 if (this.configurationChanged || this.shutdown) {
                     break;
                 }
             }
             indexDao.commit();
             indexRebuildState.setStartingUuid(startingUuid);
             if (i % 25 == 0) {
                 logger.info("Indexed {} events on table {}", numIndexed, indexDao.getName());
             }
             try {
                 indexRebuildState.save(this.indexStateFile);
             } catch (Exception e) {
                 logger.warn("Failed to save state of index rebuild to file: " +
                         this.indexStateFile.getAbsolutePath(), e);
             }
         } while (!events.isEmpty() && !this.configurationChanged && !this.shutdown);
 
         if (this.configurationChanged || this.shutdown) {
             logger.info("Index rebuild aborted");
             return;
         }
 
         if (numIndexed > 0) {
             indexDao.commit(true);
             indexMetadataDao.updateIndexVersion(indexDao.getName(), IndexConstants.INDEX_VERSION, indexVersionHash);
         }
         logger.info("Finished recreating index for {} events on table: {}", numIndexed, indexDao.getName());
         deleteStateFile();
     }
 
     @Override
     public void onApplicationEvent(IndexRebuildRequiredEvent event) {
         this.configurationChanged = true;
         synchronized (this.lock) {
             this.lock.notifyAll();
         }
     }
 }
