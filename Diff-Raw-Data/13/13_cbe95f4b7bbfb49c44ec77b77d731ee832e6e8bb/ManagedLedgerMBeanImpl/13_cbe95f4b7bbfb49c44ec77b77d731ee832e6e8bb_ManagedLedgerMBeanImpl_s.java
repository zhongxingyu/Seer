 /**
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
 package org.apache.bookkeeper.mledger.impl;
 
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.apache.bookkeeper.mledger.Entry;
 import org.apache.bookkeeper.mledger.ManagedCursor;
 import org.apache.bookkeeper.mledger.ManagedLedgerMXBean;
 import org.apache.bookkeeper.mledger.util.StatsBuckets;
 
 public class ManagedLedgerMBeanImpl implements ManagedLedgerMXBean {
 
     private final ManagedLedgerImpl managedLedger;
 
     class RecordedStats {
         final long periodStart = System.nanoTime();
         double periodDuration;
         final StatsBuckets addEntryLatencyStatsMs = new StatsBuckets(0.5, 1.0, 5.0, 10.0, 20.0, 50.0, 100.0, 200.0);
         final StatsBuckets entryStats = new StatsBuckets(128, 512, 1024, 2 * 1042, 4 * 1024, 16 * 1024, 100 * 1024,
                 1024 * 1024);
         final AtomicLong addEntryOpsFailed = new AtomicLong(0);
         final AtomicLong readEntriesOpsFailed = new AtomicLong(0);
         final AtomicLong readEntriesOpsSucceeded = new AtomicLong(0);
         final AtomicLong readEntriesMsgCount = new AtomicLong(0);
         final AtomicLong readEntriesSize = new AtomicLong(0);
     }
 
     private AtomicReference<RecordedStats> lastCompletedPeriod = new AtomicReference<RecordedStats>();
     private AtomicReference<RecordedStats> currentPeriod = new AtomicReference<RecordedStats>();
 
     public ManagedLedgerMBeanImpl(ManagedLedgerImpl managedLedger) {
         this.managedLedger = managedLedger;
         currentPeriod.set(new RecordedStats());
 
         managedLedger.getExecutor().scheduleAtFixedRate(new Runnable() {
             public void run() {
                 refreshStats();
             }
         }, 0, 60, TimeUnit.SECONDS);
     }
 
     public synchronized void refreshStats() {
         RecordedStats newStats = new RecordedStats();
         RecordedStats oldStats = currentPeriod.getAndSet(newStats);
         oldStats.periodDuration = (System.nanoTime() - oldStats.periodStart) / 1e9;
         lastCompletedPeriod.set(oldStats);
     }
 
     public void addAddEntrySample(long size) {
         currentPeriod.get().entryStats.addValue(size);
     }
 
     public void recordAddEntryError() {
         currentPeriod.get().addEntryOpsFailed.incrementAndGet();
     }
 
     public void recordReadEntriesError() {
         currentPeriod.get().readEntriesOpsFailed.incrementAndGet();
     }
 
     public void addAddEntryLatencySample(double latencyMs) {
         currentPeriod.get().addEntryLatencyStatsMs.addValue(latencyMs);
     }
 
     public void addReadEntriesSample(List<Entry> entries) {
         long totalSize = 0;
         for (Entry entry : entries) {
             totalSize += entry.getLength();
         }
 
         RecordedStats stats = currentPeriod.get();
         stats.readEntriesOpsSucceeded.incrementAndGet();
         stats.readEntriesMsgCount.addAndGet(entries.size());
         stats.readEntriesSize.addAndGet(totalSize);
     }
 
     @Override
     public String getName() {
         return managedLedger.getName();
     }
 
     @Override
     public double getAddEntryMessagesRate() {
         RecordedStats stats = lastCompletedPeriod.get();
         return stats.entryStats.getCount() / stats.periodDuration;
     }
 
     @Override
     public double getAddEntryBytesRate() {
         RecordedStats stats = lastCompletedPeriod.get();
        return stats.entryStats.getSum() / stats.periodDuration;
     }
 
     @Override
     public double getReadEntriesRate() {
         RecordedStats stats = lastCompletedPeriod.get();
         return stats.readEntriesMsgCount.get() / stats.periodDuration;
     }
 
     @Override
     public double getReadEntriesBytesRate() {
         RecordedStats stats = lastCompletedPeriod.get();
         return stats.readEntriesSize.get() / stats.periodDuration;
     }
 
     @Override
     public long getAddEntrySucceed() {
         RecordedStats stats = lastCompletedPeriod.get();
         return stats.entryStats.getCount();
     }
 
     @Override
     public long getAddEntryErrors() {
         RecordedStats stats = lastCompletedPeriod.get();
         return stats.addEntryOpsFailed.get();
     }
 
     @Override
     public long getReadEntriesSucceeded() {
         RecordedStats stats = lastCompletedPeriod.get();
         return stats.readEntriesOpsSucceeded.get();
     }
 
     @Override
     public long getReadEntriesErrors() {
         RecordedStats stats = lastCompletedPeriod.get();
         return stats.readEntriesOpsFailed.get();
     }
 
     @Override
     public double getEntrySizeMin() {
         RecordedStats stats = lastCompletedPeriod.get();
         return stats.entryStats.getMin();
     }
 
     @Override
     public double getEntrySizeMax() {
         RecordedStats stats = lastCompletedPeriod.get();
         return stats.entryStats.getMax();
     }
 
     @Override
     public long[] getEntrySizeBuckets() {
         RecordedStats stats = lastCompletedPeriod.get();
         StatsBuckets buck = stats.entryStats;
         long[] res = buck.getBuckets();
         return res;
     }
 
     @Override
     public double getAddEntryLatencyMin() {
         return lastCompletedPeriod.get().addEntryLatencyStatsMs.getMin();
     }
 
     @Override
     public double getAddEntryLatencyMax() {
         return lastCompletedPeriod.get().addEntryLatencyStatsMs.getMax();
     }
 
     @Override
     public long[] getAddEntryLatencyBuckets() {
         return lastCompletedPeriod.get().addEntryLatencyStatsMs.getBuckets();
     }
 
     @Override
     public long getStoredMessagesSize() {
         return managedLedger.getTotalSize() * managedLedger.getConfig().getWriteQuorumSize();
     }
 
     @Override
     public long getNumberOfMessagesInBacklog() {
         long count = 0;
 
         for (ManagedCursor cursor : managedLedger.getCursors()) {
             count += cursor.getNumberOfEntries();
         }
 
         return count;
     }
 
 }
