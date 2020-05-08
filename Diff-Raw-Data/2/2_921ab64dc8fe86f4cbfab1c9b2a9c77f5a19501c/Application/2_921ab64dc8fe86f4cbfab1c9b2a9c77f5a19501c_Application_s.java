 /*
  * Copyright (C) 2010-2011, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.impl;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.ApplicationListener;
 import org.springframework.dao.TransientDataAccessException;
 import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
 import org.zenoss.amqp.AmqpConnectionManager;
 import org.zenoss.amqp.QueueConfig;
 import org.zenoss.amqp.QueueConfiguration;
 import org.zenoss.amqp.ZenossQueueConfig;
 import org.zenoss.protobufs.zep.Zep.EventSeverity;
 import org.zenoss.protobufs.zep.Zep.ZepConfig;
 import org.zenoss.zep.HeartbeatProcessor;
 import org.zenoss.zep.PluginService;
 import org.zenoss.zep.ZepException;
 import org.zenoss.zep.dao.ConfigDao;
 import org.zenoss.zep.dao.DBMaintenanceService;
 import org.zenoss.zep.dao.EventArchiveDao;
 import org.zenoss.zep.dao.EventStoreDao;
 import org.zenoss.zep.dao.EventTimeDao;
 import org.zenoss.zep.dao.Purgable;
 import org.zenoss.zep.events.PluginServiceStartedEvent;
 import org.zenoss.zep.events.ZepConfigUpdatedEvent;
 import org.zenoss.zep.events.ZepEvent;
 import org.zenoss.zep.index.EventIndexRebuilder;
 import org.zenoss.zep.index.EventIndexer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Represents core application logic for ZEP, including the scheduled aging and
  * purging of events.
  */
 public class Application implements ApplicationContextAware, ApplicationListener<ZepEvent> {
 
     private static final Logger logger = LoggerFactory.getLogger(Application.class);
 
     private ThreadPoolTaskScheduler scheduler;
     private ScheduledFuture<?> eventSummaryAger = null;
     private ScheduledFuture<?> eventSummaryArchiver = null;
     private ScheduledFuture<?> eventArchivePurger = null;
     private ScheduledFuture<?> eventTimePurger = null;
     private ScheduledFuture<?> heartbeatFuture = null;
     private ScheduledFuture<?> dbMaintenanceFuture = null;
     private ZepConfig oldConfig = null;
     private ZepConfig config;
 
     private int heartbeatIntervalSeconds = 60;
 
     private long agingIntervalMilliseconds = TimeUnit.MINUTES.toMillis(1);
     private int agingLimit = 100;
 
     private long archiveIntervalMilliseconds = TimeUnit.MINUTES.toMillis(1);
     private int archiveLimit = 100;
 
     private long dbMaintenanceIntervalMinutes = 3600;
 
     private AmqpConnectionManager amqpConnectionManager;
     private ConfigDao configDao;
     private EventStoreDao eventStoreDao;
     private EventArchiveDao eventArchiveDao;
     private EventTimeDao eventTimeDao;
     private EventIndexer eventSummaryIndexer;
     private EventIndexRebuilder eventSummaryRebuilder;
     private EventIndexer eventArchiveIndexer;
     private EventIndexRebuilder eventArchiveRebuilder;
     private DBMaintenanceService dbMaintenanceService;
     private HeartbeatProcessor heartbeatProcessor;
     private PluginService pluginService;
     private ApplicationContext applicationContext;
     private ExecutorService queueExecutor;
 
     private List<String> queueListeners = new ArrayList<String>();
 
     public void setEventStoreDao(EventStoreDao eventStoreDao) {
         this.eventStoreDao = eventStoreDao;
     }
 
     public void setEventArchiveDao(EventArchiveDao eventArchiveDao) {
         this.eventArchiveDao = eventArchiveDao;
     }
 
     public void setEventTimeDao(EventTimeDao eventTimeDao) {
         this.eventTimeDao = eventTimeDao;
     }
 
     public void setAmqpConnectionManager(AmqpConnectionManager amqpConnectionManager) {
         this.amqpConnectionManager = amqpConnectionManager;
     }
 
     public void setConfigDao(ConfigDao configDao) {
         this.configDao = configDao;
     }
 
     public void setEventSummaryIndexer(EventIndexer eventSummaryIndexer) {
         this.eventSummaryIndexer = eventSummaryIndexer;
     }
 
     public void setEventSummaryRebuilder(EventIndexRebuilder eventSummaryRebuilder) {
         this.eventSummaryRebuilder = eventSummaryRebuilder;
     }
 
     public void setEventArchiveIndexer(EventIndexer eventArchiveIndexer) {
         this.eventArchiveIndexer = eventArchiveIndexer;
     }
 
     public void setEventArchiveRebuilder(EventIndexRebuilder eventArchiveRebuilder) {
         this.eventArchiveRebuilder = eventArchiveRebuilder;
     }
 
     public void setHeartbeatProcessor(HeartbeatProcessor heartbeatProcessor) {
         this.heartbeatProcessor = heartbeatProcessor;
     }
 
     public void setHeartbeatIntervalSeconds(int heartbeatIntervalSeconds) {
         this.heartbeatIntervalSeconds = heartbeatIntervalSeconds;
     }
 
     public void setPluginService(PluginService pluginService) {
         this.pluginService = pluginService;
     }
 
     public void setQueueExecutor(ExecutorService queueExecutor) {
         this.queueExecutor = queueExecutor;
     }
 
     public void setScheduler(ThreadPoolTaskScheduler scheduler) {
         this.scheduler = scheduler;
     }
 
     public void setAgingIntervalMilliseconds(long agingIntervalMilliseconds) {
         this.agingIntervalMilliseconds = agingIntervalMilliseconds;
     }
 
     public void setAgingLimit(int agingLimit) {
         this.agingLimit = agingLimit;
     }
 
     public void setArchiveIntervalMilliseconds(long archiveIntervalMilliseconds) {
         this.archiveIntervalMilliseconds = archiveIntervalMilliseconds;
     }
 
     public void setArchiveLimit(int archiveLimit) {
         this.archiveLimit = archiveLimit;
     }
 
     public void setDbMaintenanceService(DBMaintenanceService dbMaintenanceService) {
         this.dbMaintenanceService = dbMaintenanceService;
     }
 
     public void setDbMaintenanceIntervalMinutes(long dbMaintenanceIntervalMinutes) {
         this.dbMaintenanceIntervalMinutes = dbMaintenanceIntervalMinutes;
     }
 
     @Override
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         this.applicationContext = applicationContext;
     }
 
     private void init() throws ZepException {
         logger.info("Initializing ZEP");
         this.config = configDao.getConfig();
         
         /*
          * We must initialize partitions first to ensure events have a partition
          * where they can be created before we start processing the queue. This
          * init method is run before the event processor starts due to a hard
          * dependency in the Spring config on this.
          */
         initializePartitions();
         startEventSummaryAging();
         startEventSummaryArchiving();
         startEventArchivePurging();
         startEventTimePurging();
         startDbMaintenance();
         startHeartbeatProcessing();
         startQueueListeners();
         logger.info("Completed ZEP initialization");
     }
 
     public void shutdown() throws ZepException, InterruptedException {
         this.scheduler.shutdown();
 
         try {
             this.scheduler.getScheduledExecutor().awaitTermination(0L, TimeUnit.SECONDS);
             logger.info("Scheduled tasks finished");
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
 
         this.eventSummaryIndexer.shutdown();
         this.eventSummaryRebuilder.shutdown();
         this.eventArchiveIndexer.shutdown();
         this.eventArchiveRebuilder.shutdown();
 
         stopQueueListeners();
 
         this.queueExecutor.shutdown();
         try {
             this.queueExecutor.awaitTermination(0L, TimeUnit.SECONDS);
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
             this.queueExecutor.shutdownNow();
         }
 
         this.amqpConnectionManager.shutdown();
 
         this.pluginService.shutdown();
     }
 
     private void cancelFuture(Future<?> future) {
         if (future != null) {
             future.cancel(true);
             try {
                 future.get();
             } catch (ExecutionException e) {
                 logger.warn("exception", e);
             } catch (InterruptedException e) {
                 logger.debug("Interrupted", e);
             } catch (CancellationException e) {
                 /* Expected - we just canceled above */
             }
         }
     }
 
     private void initializePartitions() throws ZepException {
         eventArchiveDao.initializePartitions();
         eventTimeDao.initializePartitions();
     }
 
     private void startEventSummaryAging() {
         final int duration = config.getEventAgeIntervalMinutes();
         final EventSeverity severity = config.getEventAgeDisableSeverity();
         final boolean inclusive = config.getEventAgeSeverityInclusive();
         if (oldConfig != null
                 && duration == oldConfig.getEventAgeIntervalMinutes()
                 && severity == oldConfig.getEventAgeDisableSeverity()
                 && inclusive == oldConfig.getEventAgeSeverityInclusive()
                 ) {
             logger.info("Event aging configuration not changed.");
             return;
         }
 
         cancelFuture(this.eventSummaryAger);
         this.eventSummaryAger = null;
 
         if (duration > 0) {
             logger.info("Starting event aging at interval: {} milliseconds(s), inclusive severity: {}",
                     this.agingIntervalMilliseconds, inclusive);
             Date startTime = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));
             this.eventSummaryAger = scheduler.scheduleWithFixedDelay(
                     new ThreadRenamingRunnable(new Runnable() {
                         @Override
                         public void run() {
                             try {
                                 final int numAged = eventStoreDao.ageEvents(duration, TimeUnit.MINUTES, severity,
                                         agingLimit, inclusive);
                                 if (numAged > 0) {
                                     logger.debug("Aged {} events", numAged);
                                 }
                             } catch (TransientDataAccessException e) {
                                logger.debug("Failed to archive events", e);
                             } catch (Exception e) {
                                 logger.warn("Failed to age events", e);
                             }
                         }
                     }, "ZEP_EVENT_AGER"), startTime, this.agingIntervalMilliseconds);
         } else {
             logger.info("Event aging disabled");
         }
     }
 
     private void startEventSummaryArchiving() {
         final int duration = config.getEventArchiveIntervalMinutes();
         if (oldConfig != null && duration == oldConfig.getEventArchiveIntervalMinutes()) {
             logger.info("Event archiving configuration not changed.");
             return;
         }
 
         final Date startTime = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));
         cancelFuture(this.eventSummaryArchiver);
         this.eventSummaryArchiver = null;
         if (duration > 0) {
             logger.info("Starting event archiving at interval: {} milliseconds(s)", this.archiveIntervalMilliseconds);
             this.eventSummaryArchiver = scheduler.scheduleWithFixedDelay(
                     new ThreadRenamingRunnable(new Runnable() {
                         @Override
                         public void run() {
                             try {
                                 final int numArchived = eventStoreDao.archive(duration, TimeUnit.MINUTES, archiveLimit);
                                 if (numArchived > 0) {
                                     logger.debug("Archived {} events", numArchived);
                                     eventArchiveIndexer.index();
                                 }
                             } catch (TransientDataAccessException e) {
                                 logger.debug("Failed to archive events", e);
                             } catch (Exception e) {
                                 logger.warn("Failed to archive events", e);
                             }
                         }
                     }, "ZEP_EVENT_ARCHIVER"), startTime, this.archiveIntervalMilliseconds);
         } else {
             logger.info("Event archiving disabled");
         }
     }
 
     private ScheduledFuture<?> purge(final Purgable purgable,
                                      final int purgeDuration, final TimeUnit purgeUnit, long delayInMs,
                                      String threadName) {
         final Date startTime = new Date(System.currentTimeMillis() + 60000L);
         return scheduler.scheduleWithFixedDelay(
                 new ThreadRenamingRunnable(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             purgable.purge(purgeDuration, purgeUnit);
                         } catch (Exception e) {
                             logger.warn("Failed purging", e);
                         }
                     }
                 }, threadName), startTime, delayInMs);
     }
 
     private void startEventArchivePurging() {
         final int duration = config.getEventArchivePurgeIntervalDays();
         if (oldConfig != null
                 && duration == oldConfig.getEventArchivePurgeIntervalDays()) {
             logger.info("Event archive purging configuration not changed.");
             return;
         }
         cancelFuture(this.eventArchivePurger);
         this.eventArchivePurger = purge(eventStoreDao, duration,
                 TimeUnit.DAYS,
                 eventArchiveDao.getPartitionIntervalInMs(),
                 "ZEP_EVENT_ARCHIVE_PURGER");
     }
 
 
     private void startEventTimePurging() {
         final int duration = config.getEventTimePurgeIntervalDays();
         if (oldConfig != null
                 && duration == oldConfig.getEventTimePurgeIntervalDays()) {
             logger.info("Event Times purging configuration not changed.");
             return;
         }
         cancelFuture(this.eventTimePurger);
         this.eventTimePurger = purge(eventTimeDao, duration, TimeUnit.DAYS,
                 eventTimeDao.getPartitionIntervalInMs(), "ZEP_EVENT_TIME_PURGER");
     }
 
     private void startDbMaintenance() {
         cancelFuture(this.dbMaintenanceFuture);
         this.dbMaintenanceFuture = null;
 
         if (this.dbMaintenanceIntervalMinutes <= 0) {
             logger.info("Database table optimization disabled.");
             return;
         }
 
         // Start first task 10 minutes after ZEP has started
         logger.info("Starting database table optimization at interval: {} minutes(s)",
                 this.dbMaintenanceIntervalMinutes);
         final Date startTime = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10));
         this.dbMaintenanceFuture = scheduler.scheduleWithFixedDelay(new ThreadRenamingRunnable(new Runnable() {
             @Override
             public void run() {
                 try {
                     logger.debug("Optimizing database tables");
                     dbMaintenanceService.optimizeTables();
                     logger.debug("Completed optimizing database tables");
                 } catch (Exception e) {
                     logger.warn("Failed to optimize database tables", e);
                 }
             }
         }, "ZEP_DATABASE_MAINTENANCE"), startTime, TimeUnit.MINUTES.toMillis(this.dbMaintenanceIntervalMinutes));
     }
 
     private void startHeartbeatProcessing() {
         cancelFuture(this.heartbeatFuture);
         this.heartbeatFuture = null;
 
         Date startTime = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));
         this.heartbeatFuture = scheduler.scheduleWithFixedDelay(new ThreadRenamingRunnable(new Runnable() {
             @Override
             public void run() {
                 logger.debug("Processing heartbeats");
                 try {
                     heartbeatProcessor.sendHeartbeatEvents();
                 } catch (Exception e) {
                     logger.warn("Failed to process heartbeat events", e);
                 }
             }
         }, "ZEP_HEARTBEAT_PROCESSOR"), startTime, TimeUnit.SECONDS.toMillis(this.heartbeatIntervalSeconds));
     }
 
     private void startQueueListeners() throws ZepException {
         QueueConfig queueConfig;
         try {
             queueConfig = ZenossQueueConfig.getConfig();
         } catch (IOException e) {
             throw new ZepException(e.getLocalizedMessage(), e);
         }
         Collection<AbstractQueueListener> queueListeners =
                 applicationContext.getBeansOfType(AbstractQueueListener.class).values();
         for (AbstractQueueListener queueListener : queueListeners) {
             QueueConfiguration queue = queueConfig.getQueue(queueListener.getQueueIdentifier());
             this.queueListeners.add(this.amqpConnectionManager.addListener(queue, queueListener));
         }
     }
 
     private void stopQueueListeners() {
         for (String listenerId : this.queueListeners) {
             this.amqpConnectionManager.removeListener(listenerId);
         }
         this.queueListeners.clear();
     }
 
     @Override
     public void onApplicationEvent(ZepEvent event) {
         if (event instanceof ZepConfigUpdatedEvent) {
             ZepConfigUpdatedEvent configUpdatedEvent = (ZepConfigUpdatedEvent) event;
             this.config = configUpdatedEvent.getConfig();
             logger.info("Configuration changed: {}", this.config);
             startEventSummaryAging();
             startEventSummaryArchiving();
             startEventArchivePurging();
             startEventTimePurging();
             this.oldConfig = config;
         }
         else if (event instanceof PluginServiceStartedEvent) {
             try {
                 init();
             } catch (ZepException e) {
                 logger.error("Failed to initialize ZEP", e);
                 throw new RuntimeException(e.getLocalizedMessage(), e);
             }
         }
     }
 }
