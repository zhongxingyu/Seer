 /**
  * Copyright (C) 2011 (nick @ objectdefinitions.com)
  *
  * This file is part of JTimeseries.
  *
  * JTimeseries is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTimeseries is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with JTimeseries.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.od.jtimeseries.server.timeseries;
 
 import com.od.jtimeseries.server.serialization.FileHeader;
 import com.od.jtimeseries.server.serialization.RoundRobinSerializer;
 import com.od.jtimeseries.server.serialization.SerializationException;
 import com.od.jtimeseries.timeseries.*;
 import com.od.jtimeseries.timeseries.impl.RoundRobinTimeSeries;
 import com.od.jtimeseries.timeseries.impl.ProxyTimeSeriesEventHandler;
 import com.od.jtimeseries.util.NamedExecutors;
 import com.od.jtimeseries.util.TimeSeriesExecutorFactory;
 import com.od.jtimeseries.util.identifiable.Identifiable;
 import com.od.jtimeseries.util.identifiable.IdentifiableBase;
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 import com.od.jtimeseries.util.time.TimePeriod;
 
 import java.lang.ref.SoftReference;
 import java.util.*;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 18-May-2009
  * Time: 13:10:01
  *
  * A series which represents time series data stored on disk.
  * The actual serialization is carried out by RoundRobinSerializer
  *
  * TimeSeriesItem appended using s.addItem() can be stored in a local cache, and a delayed write performed, to avoid having
  * to keep all the data for the timeseries in memory at all times. It is expected that the number of appends will vastly
  * outnumber all other operations. Other operations (e.g. iterator) in general require the whole time series to be deserialized,
  * which is expensive, and are to be avoided where possible.
  *
  * TODO
  * It would almost certainly be possible to improve the local WriteBehindCache to hold inserts and removes as well as appends
  * so that the whole series to be deserialized to support these operations,
  * but I've left this for another day, I'm really expecting appends only at present
  */
 public class FilesystemTimeSeries extends IdentifiableBase implements IdentifiableTimeSeries, IndexedTimeSeries {
 
     private static final LogMethods logMethods = LogUtils.getLogMethods(FilesystemTimeSeries.class);
     private static ScheduledExecutorService clearCacheExecutor = NamedExecutors.newSingleThreadScheduledExecutor("FilesystemTimeSeriesClearCache");
 
     private Executor eventExecutor = TimeSeriesExecutorFactory.getExecutorForTimeSeriesEvents(this);
     private SoftReference<RoundRobinTimeSeries> softSeriesReference = new SoftReference<RoundRobinTimeSeries>(null);
     private RoundRobinSerializer roundRobinSerializer;
     private TimePeriod appendPeriod;
     private TimePeriod rewritePeriod;
     private FileHeader fileHeader;
     private ProxyTimeSeriesEventHandler timeSeriesEventHandler = new LocalModCountProxyTimeSeriesEventHandler(this);
     private WriteBehindCache writeBehindCache;
     private volatile long lastTimestamp = -1;
     private ScheduledFuture nextFlushTask;
     private volatile long modCount;
     private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
 
     /**
      *  Create a FilesystemTimeSeries for a series which already exists on disk, passing in the FileHeader, which must have been updated to match the latest state of the file
      */
     public FilesystemTimeSeries(FileHeader fileHeader, RoundRobinSerializer roundRobinSerializer, TimePeriod appendPeriod, TimePeriod rewritePeriod) throws SerializationException {
         super(fileHeader.getId(), fileHeader.getDescription());
         this.fileHeader = fileHeader;
         setFields(roundRobinSerializer, appendPeriod, rewritePeriod);
     }
 
     /**
      *  Create a FilesystemTimeSeries for a series
      *  The series may more may not already exist on disk, if it does exist a FileHeader will be created and synced with existing series, of not a new series file will be
      *  created
      */
     public FilesystemTimeSeries(String parentPath, String id, String description, RoundRobinSerializer roundRobinSerializer, int seriesLength, TimePeriod appendPeriod, TimePeriod rewritePeriod) throws SerializationException {
         super(id, description);
         createFileHeader(roundRobinSerializer, parentPath, seriesLength);
         setFields(roundRobinSerializer, appendPeriod, rewritePeriod);
     }
 
     private void setFields(RoundRobinSerializer roundRobinSerializer, TimePeriod appendPeriod, TimePeriod rewritePeriod) {
         this.roundRobinSerializer = roundRobinSerializer;
         this.appendPeriod = appendPeriod;
         this.rewritePeriod = rewritePeriod;
         this.lastTimestamp = fileHeader.getMostRecentItemTimestamp();
         this.writeBehindCache = new WriteBehindCache();
     }
 
     private void createFileHeader(RoundRobinSerializer roundRobinSerializer, String parentPath, int seriesLength) throws SerializationException {
         this.fileHeader = new FileHeader(parentPath + Identifiable.NAMESPACE_SEPARATOR + getId(), getDescription(), seriesLength);
         if ( roundRobinSerializer.fileExists(fileHeader) ) {
             roundRobinSerializer.updateHeader(fileHeader);
         } else {
             roundRobinSerializer.createFile(fileHeader);
         }
     }
 
     /**
      * Note, appending data does not require deserialization of the filesystem timeseries, thus preventing the
      * peformance overhead of having to deserialize to add items. If the series is not already in memory we will append
      * the item to our write behind cache only (eventually the item will get persisted, depending on the cache flush
      * scheduling)
      *
      * Inserting items is much more expensive, since we have to deserialize to do that
      */
     public void addItem(TimeSeriesItem i) {
         try {
             this.writeLock().lock();
             boolean result = doAppend(i);
             if (!result) {
                 //it is not expected we would usually get inserts - they should be rare events at present
                 //(e.g. a system clock resyncs and goes back in time, so we start to receive values with an earlier timestamp)
                 RoundRobinTimeSeries r = getRoundRobinSeries();
                 r.addItem(i);
                 writeBehindCache.cacheSeriesForRewrite(r);
             }
         } finally {
             this.writeLock().unlock();
         }
 
     }
 
     public void addAll(Iterable<TimeSeriesItem> items) {
         try {
             this.writeLock().lock();
             for (TimeSeriesItem i : items) {
                 addItem(i);
             }
         } finally {
             this.writeLock().unlock();
         }
 
     }
 
     private boolean doAppend(final TimeSeriesItem i) {
         //if the round robin series is in memory, we add the item to the series, but we also add the item to cache's list
         //this is because the round robin series is our primary source for the series data while in memory, but it is soft
         //referenced and it may be gc'd at any time. If this happens we still need a record of the items which were added
         //since it was last persisted.
         boolean result = false;
         if ( i.getTimestamp() >= lastTimestamp) {
             writeBehindCache.addItemForAppend(i);
             result = true;
             RoundRobinTimeSeries s = getRoundRobinSeries(false);
             if ( s != null) {
                 s.addItem(i);
             } else {
                 //the series has been collected, along with listener registrations, so we need to fire event ourselves
                 fireAddEvent(i);
             }
             lastTimestamp = i.getTimestamp();
         }
         return result;
     }
 
     private void fireAddEvent(final TimeSeriesItem i) {
         final TimeSeriesEvent e = TimeSeriesEvent.createItemsAddedOrInsertedEvent(
                 FilesystemTimeSeries.this, Collections.singletonList(i), ++modCount
         );
 
         eventExecutor.execute(new Runnable() {
             public void run() {
                 timeSeriesEventHandler.fireItemsAddedOrInserted(e);
             }
         });
     }
 
     public int getMaxSize() {
         return fileHeader.getSeriesMaxLength();
     }
 
     public List<TimeSeriesItem> getSnapshot() {
         try {
             this.readLock().lock();
             return getRoundRobinSeries().getSnapshot();
         } finally {
             this.readLock().unlock();
         }
 
     }
 
     public TimeSeriesItem getLatestItem() {
         //make use of the local write behind cache to do this if possible
         try {
             this.readLock().lock();
             return writeBehindCache.getAppendListSize() > 0 ?
                     writeBehindCache.getAppendItems().getLatestItem() :
                     getRoundRobinSeries().getLatestItem();
         } finally {
             this.readLock().unlock();
         }
 
     }
 
     /**
      * @return the latest timestamp in the series, without causing a load from the filesystem
      */
     public long getLatestTimestamp() {
         return lastTimestamp;
     }
 
     public long getEarliestTimestamp() {
         try {
             this.readLock().lock();
             return getRoundRobinSeries().getEarliestTimestamp();
         } finally {
             this.readLock().unlock();
         }
 
     }
 
     public TimeSeriesItem getEarliestItem() {
         try {
             this.readLock().lock();
             return getRoundRobinSeries().getEarliestItem();
         } finally {
             this.readLock().unlock();
         }
 
     }
 
     public int size() {
         try {
             this.readLock().lock();
             RoundRobinTimeSeries r = getRoundRobinSeries(false);
             if (r != null) {
                 return r.size();
             } else {
                 //when the cache is flushed, if the cache contains more items than can fit in the series, we will lose the earlist due to round robin
                 //so we will end up with maxSize items in the series
                 return Math.min(getMaxSize(), fileHeader.getCurrentSeriesSize() + writeBehindCache.getAppendItems().size());
             }
         } finally {
             this.readLock().unlock();
         }
 
     }
 
     public TimeSeriesItem getItem(int index) {
         try {
             this.readLock().lock();
             return getRoundRobinSeries().getItem(index);
         } finally {
             this.readLock().unlock();
         }
 
     }
 
     public void clear() {
         try {
             this.writeLock().lock();
             RoundRobinTimeSeries r = getRoundRobinSeries();
             r.clear();
             lastTimestamp = -1;
             writeBehindCache.cacheSeriesForRewrite(r);
         } finally {
             this.writeLock().unlock();
         }
 
     }
 
     public boolean removeItem(TimeSeriesItem item) {
         try {
             this.writeLock().lock();
             RoundRobinTimeSeries r = getRoundRobinSeries();
             boolean change = r.removeItem(item);
             if (change) {
                 writeBehindCache.cacheSeriesForRewrite(r);
             }
             lastTimestamp = r.getLatestTimestamp();
             return change;
         } finally {
             this.writeLock().unlock();
         }
 
     }
 
     public void removeAll(Iterable<TimeSeriesItem> items) {
         try {
             this.writeLock().lock();
             getRoundRobinSeries().removeAll(items);
         } finally {
             this.writeLock().unlock();
         }
 
     }
 
     public Iterator<TimeSeriesItem> iterator() {
         try {
             this.readLock().lock();
             return getRoundRobinSeries().iterator();
         } finally {
             this.readLock().unlock();
         }
     }
 
      public Iterator<TimeSeriesItem> unsafeIterator() {
         try {
             this.readLock().lock();
             return getRoundRobinSeries().unsafeIterator();
         } finally {
             this.readLock().unlock();
         }
 
     }
 
     /**
      * Intentionally break the contract of List.equals() - we shouldn't need to support logical equality of
      * FilesystemTimeSeries as a List of items - to do so would require us to deserialize from disk, which would be a bad idea
      */
     public boolean equals(Object o) {
         return o == this;
     }
 
     /**
      * Intentionally break the contract of List.hashCode() - we shouldn't need to support logical equality of
      * FilesystemTimeSeries as a List of items - to do so would require us to deserialize from disk, which would be a bad idea
      */
     public int hashCode() {
         return super.hashCode();
     }
 
     public long getModCount() {
         return modCount;
     }
 
     public TimeSeriesItem getFirstItemAtOrBefore(long timestamp) {
         return getRoundRobinSeries().getFirstItemAtOrBefore(timestamp);
     }
 
     public TimeSeriesItem getFirstItemAtOrAfter(long timestamp) {
         return getRoundRobinSeries().getFirstItemAtOrAfter(timestamp);
     }
 
     public List<TimeSeriesItem> getItemsInRange(long startTime, long endTime) {
         return getRoundRobinSeries().getItemsInRange(startTime, endTime);
     }
 
     //delegate property handling to FileHeader, so that properties get persisted
     public String setProperty_Locked(String key, String value) {
         return fileHeader.setSeriesProperty(key, value);
     }
 
     //delegate property handling to FileHeader, so that properties get persisted
     public String removeProperty_Locked(String key) {
         return fileHeader.removeSeriesProperty(key);
     }
 
     public String getProperty_Locked(String key) {
         return fileHeader.getSeriesProperty(key);
     }
 
     public Properties getProperties_Locked() {
         return fileHeader.getSeriesProperties();
     }
 
     public FileHeader getFileHeader() {
         return fileHeader;
     }
 
     public void addTimeSeriesListener(TimeSeriesListener l) {
         timeSeriesEventHandler.addTimeSeriesListener(l);
     }
 
     public void removeTimeSeriesListener(TimeSeriesListener l) {
         timeSeriesEventHandler.removeTimeSeriesListener(l);
     }
 
     protected RoundRobinTimeSeries getRoundRobinSeries() {
         return getRoundRobinSeries(true);
     }
 
     private RoundRobinTimeSeries getRoundRobinSeries(boolean deserializeIfRequired) {
         RoundRobinTimeSeries s = isSeriesInWriteCache() ? writeBehindCache.getSeries() : softSeriesReference.get();
         if ( s == null && deserializeIfRequired ) {
             try {
                 s = roundRobinSerializer.deserialize(fileHeader);
 
                 //there may be items in our cache we need to add to
                 //bring the filesystem series into sync. We will have to call a special
                 //method to add the items in the local cache to the deserialized series without firing
                 //events. This is because once it is in memory we are adding a listener to the deserialized series
                 //to propagate events to our listeners whenever an item is added, and events for the items in the local
                 //cache have already been fired to our listeners.
                 //Due to the asynchronous event firing, when we call s.add() we could end
                 //up firing duplicate events when we add these items, even though our propagating listener has not yet
                 //been added - we will probably not receive the events back until the listener has been added.
                 s.addAllWithoutFiringEvents(writeBehindCache.getAppendItems().getSnapshot());
                 //nb. the items stay in the cache append list, so we keep track that we still haven't written them to disk
 
                 s.addTimeSeriesListener(timeSeriesEventHandler);
                 softSeriesReference = new SoftReference<RoundRobinTimeSeries>(s);
             } catch (SerializationException e) {
                 throw new RuntimeException("Could not load timeseries values", e);
             }
         }
         return s;
     }
 
     /**
      * WriteBehindCache will hold values only if the local series has changed from the version held on disk
      * Mutator methods on the FilesystemTimeSeries call methods on the WriteBehindCache when the state changes.
      *
      * The write behind cache can store either be the most recently appended items or a strong reference to the whole round robin series if there were any changes apart from appends.
      * In either case, the main softReference series held by FileSystemTimeSeries should always be kept fully up to date, if it exists
      * - the cache just represents what we need to write to the filesystem to bring the persisted version up to date.
      *
      * In the case we're holding onto a list of appended items, the soft referenced wrapped series is free to be
      * reclaimed (the append list contains all the deltas in this case - we have no need
      * to rewrite the whole series, and there's no urgency to flush the cache.)
      *
      * If other changes to the wrapped series have been made (not just appends) we hold a reference
      * to the whole series to prevent it being collected, and add a task to try to bring forward the
      * cache flush operation.
      */
     private class WriteBehindCache {
 
         //when changes to series are not just appends, keep the whole series in memory until flush by holding this reference
         private RoundRobinTimeSeries roundRobinSeries;
 
         //there is never any point in appending more items than the max series size, so use a round robin series to store until flush
         private RoundRobinTimeSeries itemsToAppend = new RoundRobinTimeSeries(getMaxSize());
 
         public void cacheSeriesForRewrite(RoundRobinTimeSeries roundRobinSeries) {
             this.roundRobinSeries = roundRobinSeries;
             itemsToAppend.clear(); //clear the append items list, we don't need it, we will now rewrite the whole series instead
             scheduleFlushCacheTask(rewritePeriod.getLengthInMillis());
         }
 
         public void addItemForAppend(TimeSeriesItem timeSeriesItem) {
             if ( roundRobinSeries == null) { //only if we are not already going to rewrite the whole series
                 itemsToAppend.addItem(timeSeriesItem);
                 scheduleFlushCacheTask(appendPeriod.getLengthInMillis());
             }
         }
 
         public RoundRobinTimeSeries getAppendItems() {
             return itemsToAppend;
         }
 
         public void flush() {
             try {
                 FilesystemTimeSeries.this.writeLock().lock();
                 if ( isFlushRequired()) {
                     doFlush();
                 }
             } finally {
                 FilesystemTimeSeries.this.writeLock().unlock();
             }
 
         }
 
         private void doFlush() {
             try {
                 if (roundRobinSeries != null) {
                     //we have a local series which contains other changes, as well as possibly some appends
                     roundRobinSerializer.serialize(fileHeader, roundRobinSeries);
                 } else {
                     //only changes are appends, or if the append series is empty there may be
                     //other changes which require a header rewrite (e.g. properties)
                     roundRobinSerializer.append(fileHeader, itemsToAppend);
                 }
 
                 //clear cache if no exception / write succeeded
                 //otherwise hold on to changes until we try the write again
                 clearCache();
 
             } catch (Throwable t) {
                 logMethods.logError("Failed to write to timeseries file " + fileHeader + ", cannot bring this series up to date, I'll keep trying", t);
                 scheduleFlushCacheTask(appendPeriod.getLengthInMillis());
             }
         }
 
         private void clearCache() {
             roundRobinSeries = null;
             itemsToAppend.clear();
         }
 
         private boolean isFlushRequired() {
             return roundRobinSeries != null ||
                itemsToAppend.size() > 0 ||
                 fileHeader.isPropertiesRewriteRequired();
         }
 
         private boolean isSeriesInCache() {
             return roundRobinSeries != null;
         }
 
         private int getAppendListSize() {
             return itemsToAppend.size();
         }
 
         public RoundRobinTimeSeries getSeries() {
             return roundRobinSeries;
         }
 
         private void scheduleFlushCacheTask(long delayMillis) {
             //cancel the next flush, and schedule a new one sooner
             if ( nextFlushTask == null || nextFlushTask.isDone())  {
                 scheduleNewTask(delayMillis);
             } else if ( nextFlushTask.getDelay(TimeUnit.MILLISECONDS) > delayMillis ) {
                 //bring forward by cancelling and scheduling a new task
                 nextFlushTask.cancel(false);
                 scheduleNewTask(delayMillis);
             }
         }
 
         private void scheduleNewTask(long delayMillis) {
             nextFlushTask = clearCacheExecutor.schedule(
                 new Runnable() {
                     public void run() {
                         writeBehindCache.flush();
                     }
                 },
                 delayMillis,
                 TimeUnit.MILLISECONDS
             );
         }
     }
 
     /**
      * Queue for a rewrite, will be header only unless there are other appends/changes
      */
     public void queueHeaderRewrite() {
         try {
             readWriteLock.writeLock().lock();
             writeBehindCache.scheduleFlushCacheTask(appendPeriod.getLengthInMillis());
         } finally {
             readWriteLock.writeLock().unlock();
         }
 
     }
 
     /**
      * Write all in-memory changes to the FilesystemTimeSeries to disk
      */
     public void flush() {
         writeBehindCache.flush();
     }
 
     /**
      * @return true, if the series has in-memory changes
      */
     public boolean isFlushRequired() {
         return writeBehindCache.isFlushRequired();
     }
 
     boolean isSeriesInWriteCache() {
         return writeBehindCache.isSeriesInCache();
     }
 
     int getCacheAppendListSize() {
         try {
             readWriteLock.readLock().lock();
             return writeBehindCache.getAppendListSize();
         } finally {
             readWriteLock.readLock().unlock();
         }
     }
 
     //testing hook, trigger the garbage collection of the soft referenced series
     void triggerGarbageCollection() {
         try {
             readWriteLock.writeLock().lock();
             softSeriesReference.clear();
         } finally {
             readWriteLock.writeLock().unlock();
         }
 
     }
 
     //testing hook
     boolean isSeriesCollected() {
         try {
             readWriteLock.readLock().lock();
             return softSeriesReference.get() == null;
         } finally {
             readWriteLock.readLock().unlock();
         }
 
     }
 
     private class LocalModCountProxyTimeSeriesEventHandler extends ProxyTimeSeriesEventHandler {
 
         public LocalModCountProxyTimeSeriesEventHandler(Object proxySource) {
             super(proxySource);
         }
 
         public void itemsAddedOrInserted(TimeSeriesEvent h) {
             TimeSeriesEvent e = TimeSeriesEvent.createEvent(FilesystemTimeSeries.this, h.getItems(), h.getEventType(), ++modCount);
             fireItemsAddedOrInserted(e);
         }
 
         public void itemsRemoved(TimeSeriesEvent h) {
             TimeSeriesEvent e = TimeSeriesEvent.createEvent(FilesystemTimeSeries.this, h.getItems(), h.getEventType(), ++modCount);
             fireItemsRemoved(e);
         }
 
         public void seriesChanged(TimeSeriesEvent h) {
             TimeSeriesEvent e = TimeSeriesEvent.createEvent(FilesystemTimeSeries.this, h.getItems(), h.getEventType(), ++modCount);
             fireSeriesChanged(e);
         }
     }
 
     public Lock writeLock() {
         return readWriteLock.writeLock();
     }
 
     public Lock readLock() {
         return readWriteLock.readLock();
     }
 }
