 package org.apache.bookkeeper.mledger.impl;
 
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.apache.bookkeeper.mledger.ManagedLedgerFactoryMXBean;
 
 public class ManagedLedgerFactoryMBeanImpl implements ManagedLedgerFactoryMXBean {
 
     private final ManagedLedgerFactoryImpl factory;
 
     class RecordedStats {
         final long periodStart = System.nanoTime();
         double periodDuration;
 
         final AtomicLong cacheHitsCount = new AtomicLong(0);
         final AtomicLong cacheMissesCount = new AtomicLong(0);
         final AtomicLong cacheHitsSize = new AtomicLong(0);
         final AtomicLong cacheMissesSize = new AtomicLong(0);
         final AtomicLong cacheEvictionsCount = new AtomicLong(0);
     }
 
     private AtomicReference<RecordedStats> lastCompletedPeriod = new AtomicReference<RecordedStats>();
     private AtomicReference<RecordedStats> currentPeriod = new AtomicReference<RecordedStats>();
 
     public ManagedLedgerFactoryMBeanImpl(ManagedLedgerFactoryImpl factory) throws Exception {
         this.factory = factory;
         currentPeriod.set(new RecordedStats());
         
         factory.executor.scheduleAtFixedRate(new Runnable() {
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
 
     public void recordCacheHit(long size) {
         RecordedStats stats = currentPeriod.get();
         stats.cacheHitsCount.incrementAndGet();
         stats.cacheHitsSize.addAndGet(size);
     }
 
     public void recordCacheMiss(int count, long totalSize) {
         RecordedStats stats = currentPeriod.get();
         stats.cacheMissesCount.addAndGet(count);
         stats.cacheMissesSize.addAndGet(totalSize);
     }
 
     public void recordCacheEviction() {
         currentPeriod.get().cacheEvictionsCount.incrementAndGet();
     }
 
     // //
 
     @Override
     public int getNumberOfManagedLedgers() {
         return factory.ledgers.size();
     }
 
     @Override
     public long getCacheUsedSize() {
         return factory.getEntryCacheManager().getSize();
     }
 
     @Override
     public long getCacheMaxSize() {
         return factory.getEntryCacheManager().getMaxSize();
     }
 
     @Override
     public double getCacheHitsRate() {
         RecordedStats stats = lastCompletedPeriod.get();
         return stats.cacheHitsCount.get() / stats.periodDuration;
     }
 
     @Override
     public double getCacheMissesRate() {
         RecordedStats stats = lastCompletedPeriod.get();
        return stats.cacheHitsCount.get() / stats.periodDuration;
     }
 
     @Override
     public double getCacheHitsThroughput() {
         RecordedStats stats = lastCompletedPeriod.get();
         return stats.cacheHitsSize.get() / stats.periodDuration;
     }
 
     @Override
     public double getCacheMissesThroughput() {
         RecordedStats stats = lastCompletedPeriod.get();
         return stats.cacheMissesSize.get() / stats.periodDuration;
     }
 
     @Override
     public long getNumberOfCacheEvictions() {
         return lastCompletedPeriod.get().cacheEvictionsCount.get();
     }
 
 }
