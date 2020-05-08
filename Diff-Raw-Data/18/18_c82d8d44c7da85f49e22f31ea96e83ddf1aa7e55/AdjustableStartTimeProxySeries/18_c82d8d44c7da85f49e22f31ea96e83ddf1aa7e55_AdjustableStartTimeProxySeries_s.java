 package com.od.jtimeseries.ui.timeseries;
 
 import com.od.jtimeseries.timeseries.IdentifiableTimeSeries;
 import com.od.jtimeseries.timeseries.TimeSeries;
 import com.od.jtimeseries.timeseries.TimeSeriesEvent;
 import com.od.jtimeseries.timeseries.TimeSeriesItem;
 import com.od.jtimeseries.timeseries.impl.DefaultTimeSeries;
 import com.od.jtimeseries.timeseries.impl.ProxyTimeSeriesEventHandler;
 
 import java.lang.ref.SoftReference;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 14/01/11
  * Time: 17:27
  *
  * A read only view of a wrapped series, which contain only TimeSeriesItem with timestamp equal to or later than
  * a specified startTimestamp. Maintains a local soft referenced copy of the data to improve performance
  */
 public class AdjustableStartTimeProxySeries extends ProxyingPropertyChangeTimeseries {
 
    private volatile long startTimestamp = Long.MAX_VALUE;
     private SoftReference<TimeSeries> localSnapshotSeries;
     private ProxyTimeSeriesEventHandler eventHandler;
     private long modCount = 0;
 
     public AdjustableStartTimeProxySeries(IdentifiableTimeSeries wrappedSeries) {
         super(wrappedSeries);
 
         eventHandler = new LocalModCountEventHandler();
         setProxyEventHandler(eventHandler);
     }
 
     public synchronized void setStartTimestamp(long startTimestamp) {
         this.startTimestamp = startTimestamp;
         localSnapshotSeries = null;
         eventHandler.fireSeriesChanged(TimeSeriesEvent.createSeriesChangedEvent(
             AdjustableStartTimeProxySeries.this,
             new LinkedList<TimeSeriesItem>(getLocalSnapshotSeries(true)),
             ++modCount
         ));
 
     }
 
     public boolean prepend(TimeSeriesItem item) {
         throw new UnsupportedOperationException("FlexibleStartTimeProxySeries does not yet support this operation");
     }
 
     public boolean append(TimeSeriesItem value) {
         throw new UnsupportedOperationException("FlexibleStartTimeProxySeries does not yet support this operation");
     }
 
     public TimeSeries getSubSeries(long timestamp) {
         return getLocalSnapshotSeries(true).getSubSeries(timestamp);
     }
 
     public TimeSeries getSubSeries(long startTimestamp, long endTimestamp) {
         return getLocalSnapshotSeries(true).getSubSeries(startTimestamp, endTimestamp);
     }
 
     public TimeSeriesItem getEarliestItem() {
         return super.getFirstItemAtOrAfter(startTimestamp);
     }
 
     public TimeSeriesItem getLatestItem() {
         return super.getLatestItem();
     }
 
     public TimeSeriesItem removeEarliestItem() {
         throw new UnsupportedOperationException("FlexibleStartTimeProxySeries does not yet support this operation");
     }
 
     public TimeSeriesItem removeLatestItem() {
         throw new UnsupportedOperationException("FlexibleStartTimeProxySeries does not yet support this operation");
     }
 
     public synchronized long getEarliestTimestamp() {
         TimeSeriesItem i = super.getFirstItemAtOrAfter(startTimestamp);
         return i == null ? -1 : i.getTimestamp();
     }
 
     public long getLatestTimestamp() {
         return super.getLatestTimestamp();
     }
 
     public synchronized TimeSeriesItem getFirstItemAtOrBefore(long timestamp) {
         TimeSeriesItem result;
         if ( timestamp >= this.startTimestamp) {
             result = super.getFirstItemAtOrBefore(timestamp);
             if ( result != null && result.getTimestamp() < startTimestamp) {
                 result = null;
             }
         }
         return null;
     }
 
     public synchronized TimeSeriesItem getFirstItemAtOrAfter(long timestamp) {
         TimeSeriesItem result = super.getFirstItemAtOrAfter(timestamp);
         if ( result != null && result.getTimestamp() < startTimestamp ) {
             result = null;
         }
         return result;
     }
 
     public synchronized long getTimestampAfter(long timestamp) {
         return timestamp < startTimestamp ? startTimestamp : super.getTimestampAfter(timestamp);
     }
 
     public synchronized long getTimestampBefore(long timestamp) {
         return timestamp <= startTimestamp ? -1 : super.getTimestampBefore(timestamp);
     }
 
     public Collection<TimeSeriesItem> getSnapshot() {
         DefaultTimeSeries s = new DefaultTimeSeries();
         s.addAll(getLocalSnapshotSeries(true));
         return s;
     }
 
     public int size() {
         return getLocalSnapshotSeries(true).size();
     }
 
     public boolean isEmpty() {
         return getLocalSnapshotSeries(true).isEmpty();
     }
 
     public boolean contains(Object o) {
         return getLocalSnapshotSeries(true).contains(o);
     }
 
     public Iterator<TimeSeriesItem> iterator() {
         return getLocalSnapshotSeries(true).iterator();
     }
 
     public Object[] toArray() {
         return getLocalSnapshotSeries(true).toArray();
     }
 
     public <T> T[] toArray(T[] a) {
         return getLocalSnapshotSeries(true).toArray(a);
     }
 
     public boolean add(TimeSeriesItem timeSeriesItem) {
         throw new UnsupportedOperationException("FlexibleStartTimeProxySeries does not yet support this operation");
     }
 
     public boolean remove(Object o) {
         throw new UnsupportedOperationException("FlexibleStartTimeProxySeries does not yet support this operation");
     }
 
     public boolean containsAll(Collection<?> c) {
         return getLocalSnapshotSeries(true).containsAll(c);
     }
 
     public boolean addAll(Collection<? extends TimeSeriesItem> c) {
         throw new UnsupportedOperationException("FlexibleStartTimeProxySeries does not yet support this operation");
     }
 
     public boolean removeAll(Collection<?> c) {
         throw new UnsupportedOperationException("FlexibleStartTimeProxySeries does not yet support this operation");
     }
 
     public boolean retainAll(Collection<?> c) {
         throw new UnsupportedOperationException("FlexibleStartTimeProxySeries does not yet support this operation");
     }
 
     public void clear() {
         throw new UnsupportedOperationException("FlexibleStartTimeProxySeries does not yet support this operation");
     }
 
     private synchronized TimeSeries getLocalSnapshotSeries(boolean create) {
         TimeSeries result = null;
         if ( localSnapshotSeries != null) {
             result = localSnapshotSeries.get();
         }
         if ( result == null && create ) {
             result = super.getSubSeries(startTimestamp);
             localSnapshotSeries = new SoftReference<TimeSeries>(result);
         }
         return result;
     }
 
     private class LocalModCountEventHandler extends ProxyTimeSeriesEventHandler {
 
         public LocalModCountEventHandler() {
             super(AdjustableStartTimeProxySeries.this);
         }
 
         public void itemsAddedOrInserted(TimeSeriesEvent t) {
             synchronized (AdjustableStartTimeProxySeries.this) {
                 TimeSeries s = getLocalSnapshotSeries(false);
                 TimeSeriesEvent restrictedEvent = getRestrictedEvent(t, t.getEventType());
                 if ( restrictedEvent.getItems().size() > 0 ) {
                     if (s != null && t.getFirstItemTimestamp() >= s.getLatestTimestamp()) {
                         s.addAll(t.getItems());
                         super.fireItemsAdded(restrictedEvent);
                     } else {
                         localSnapshotSeries = null;
                         super.fireItemsAdded(restrictedEvent);
                     }
                 }
             }
         }
 
         public void itemsRemoved(TimeSeriesEvent t) {
             synchronized (AdjustableStartTimeProxySeries.this) {
                 if (t.getLastItemTimestamp() >= startTimestamp) {
                     TimeSeriesEvent restrictedEvent = getRestrictedEvent(t, t.getEventType());
                     localSnapshotSeries = null;
                     super.fireItemsRemoved(restrictedEvent);
                 }
             }
         }
 
         public void itemsChanged(TimeSeriesEvent t) {
             synchronized (AdjustableStartTimeProxySeries.this) {
                 if (t.getLastItemTimestamp() >= startTimestamp) {
                     localSnapshotSeries = null;
                     TimeSeriesEvent restrictedEvent = getRestrictedEvent(t, t.getEventType());
                     super.fireItemsChanged(restrictedEvent);
                 }
             }
         }
 
         public void seriesChanged(TimeSeriesEvent t) {
             synchronized(AdjustableStartTimeProxySeries.this) {
                 localSnapshotSeries = null;
                 super.fireSeriesChanged(getRestrictedEvent(t, t.getEventType()));
             }
         }
 
         //get an event which contains only timeseries items >= startTime for this adjustable start time series
         //also use the local modCount, since modCount also changes when startTime is changed
         private TimeSeriesEvent getRestrictedEvent(TimeSeriesEvent t, TimeSeriesEvent.EventType eventType) {
             List<TimeSeriesItem> items = new LinkedList<TimeSeriesItem>();
             for (TimeSeriesItem i : t.getItems()) {
                 if (i.getTimestamp() >= startTimestamp) {
                     items.add(i);
                 }
             }
             return TimeSeriesEvent.createEvent(AdjustableStartTimeProxySeries.this, items, eventType, ++modCount);
         }
     }
 }
