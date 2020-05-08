 /**
  * Copyright (C) 2013 Arman Gal
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.smexec.jmx;
 
 import java.lang.management.ManagementFactory;
 import java.lang.management.OperatingSystemMXBean;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Base class Data Holder class for server statistics exposed via JMX. Purpose of the class is to aggregate
  * statistics for different events. The statistics will be aggregated per minute. Maximum statistic entries is
  * 1460 (1 day). When the maximum allowed statistic entries is reached, the oldest is removed. Each minute a
  * scheduled StatChunkCreator task will take snapshot of the accumulated statistics and will reset them. Class
  * is designed to be single instance per JVM, loaded by Guice.
  * 
  * @author erdoan
  * @version $Revision: 1.1 $
  */
 public abstract class AbstractJmxStatDataHolder<T extends AbstractJmxStatEntry> {
 
     protected final Logger logger;
     private static int DEFAULT_MAX_STAT_ENTRIES_IN_MEMORY = 1460;// 1 day in minutes
     private static int DEFAULT_STAT_ENTRY_PERIOD = 60; // IN SECONDS
 
     private int maxStatEntriesInMemory;
 
     /**
      * LinkedList which holds all the statistic Entries. Newest are added to the beginning.
      */
     private LinkedList<T> stats = new LinkedList<T>();
 
     protected long lastStartTime;
 
     private long previousCPUMeasurementTime;
 
     private long previousCPUTime;
 
     /**
      * simple constructor in case you want to keep one day of stats and chunk every minute.
      * 
      * @param statsName
      */
     public AbstractJmxStatDataHolder(final String statsName) {
         this(DEFAULT_MAX_STAT_ENTRIES_IN_MEMORY, DEFAULT_STAT_ENTRY_PERIOD, statsName);
     }
 
     /**
      * @param maxStatEntriesInMemory - the amount of elements to keep in memory of the server
      * @param statEntryPeriod - how often to cut a chunk
      * @param statsName -
      */
     public AbstractJmxStatDataHolder(final int maxStatEntriesInMemory, final long statEntryPeriod, final String statsName) {
         logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
 
         this.lastStartTime = getCurrentTime();
         this.maxStatEntriesInMemory = maxStatEntriesInMemory;
         Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new StatChunker(statsName), getInitialDelay(), statEntryPeriod, TimeUnit.MILLISECONDS);
     }
 
     /**
      * Converts list of AbstractJmxStatEntry object to array
      * 
      * @param list
      * @return
      */
     protected abstract T[] getStatsAsArray(List<T> list);
 
     /**
      * Takes the snapshot of the statistics accumulated during the last chunk period. By default period is 1
      * minute.Array is sorted in descending order by stat time. Newest first. It's developer responsibility to
      * reset the counter during the creating of snapshot in most efficient way.
      * 
      * @return ? extends AbstractJmxStatEntry. .
      */
     protected abstract T snapshotStats();
 
     /**
      * Retrieves all the available statistics from the memory.
      * 
      * @return array of ? extends AbstractJmxStatEntry>
      */
     public T[] getAllStats() {
         synchronized (stats) {
             return getStatsAsArray(stats);
         }
     }
 
     private void addStatEntry(T statEntry) {
         if (statEntry == null) {
             return;
         }
 
         synchronized (stats) {
             stats.addFirst(statEntry);
            if (stats.size() > this.maxStatEntriesInMemory) {
                 stats.removeLast();
             }
         }
     }
 
     /**
      * Loads all the statistics, newer than the leasUpdate time.
      * 
      * @param lastUpdate
      * @return Array of ? extends AbstractJmxStatEntry
      */
     public T[] getLastStats(long lastUpdate) {
         List<T> tempList = new ArrayList<T>();
         synchronized (stats) {
             for (T entry : stats) {
                 if (entry.getStartTime() >= lastUpdate) {
                     tempList.add(entry);
                 } else {
                     break;
                 }
             }
         }
         return getStatsAsArray(tempList);
 
     }
 
     protected static long getInitialDelay() {
         Calendar calendar = Calendar.getInstance();
         int seconds = calendar.get(Calendar.SECOND);
         return 60 - seconds;
     }
 
     /**
      * @return current time in milliseconds
      */
     protected static long getCurrentTime() {
         return System.currentTimeMillis();
     }
 
     @SuppressWarnings("restriction")
     protected double snapshotAvgCPUTime() {
         long currentTime = 0;
         long currentCPUTime = 0;
 
         try {
             OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
 
             if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean) {
                 currentCPUTime = ((com.sun.management.OperatingSystemMXBean) operatingSystemMXBean).getProcessCpuTime();
                 currentTime = System.nanoTime();
             }
 
             double cpuPercentage = calculateCPUTimePercentage(currentCPUTime, previousCPUTime, currentTime, previousCPUMeasurementTime);
             this.previousCPUTime = currentCPUTime;
             this.previousCPUMeasurementTime = currentTime;
 
             return cpuPercentage;
         } catch (Exception e) {
             logger.error("Error while measuring CPU usage ", e);
         }
 
         return 0;
 
     }
 
     public double calculateCPUTimePercentage(final long currentCPUTime, long previousCPUTime, final long currentTime, long previousTime) {
         double percent;
         if (currentCPUTime > previousCPUTime) {
             percent = ((currentCPUTime - previousCPUTime) * 100L) / (currentTime - previousTime);
         } else {
             percent = 0;
         }
 
         return percent;
     }
 
     /**
      * The purpose of the class is to take snapshots/chunks of the statistics.
      * 
      * @author erdoan
      * @version $Revision: 1.1 $
      */
     private class StatChunker
         implements Runnable {
 
         private static final String THREAD_PREFIX = "CHUNKER_";
         private String statsName;
 
         private StatChunker(String statsName) {
             this.statsName = THREAD_PREFIX + statsName;
         }
 
         public void run() {
             String name = Thread.currentThread().getName();
             Thread.currentThread().setName(statsName);
             try {
                 T statChunk = snapshotStats();
                 lastStartTime = getCurrentTime();
                 statChunk.setCpuUsage(snapshotAvgCPUTime());
                 addStatEntry(statChunk);
                 logger.info("Chunk:{}", statChunk);
 
             } catch (Throwable e) {
                 logger.error(e.getMessage(), e);
             } finally {
                 Thread.currentThread().setName(name);
             }
         }
     }
 
 }
