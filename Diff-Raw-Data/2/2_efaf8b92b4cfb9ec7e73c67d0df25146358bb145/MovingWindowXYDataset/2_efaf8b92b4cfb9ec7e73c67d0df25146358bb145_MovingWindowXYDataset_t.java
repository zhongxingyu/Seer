 package com.od.jtimeseries.chart;
 
 import com.od.jtimeseries.timeseries.*;
 import com.od.jtimeseries.timeseries.impl.DefaultTimeSeries;
 import com.od.jtimeseries.timeseries.impl.WeakReferenceTimeSeriesListener;
 import com.od.jtimeseries.timeseries.util.SeriesUtils;
 import com.od.jtimeseries.util.NamedExecutors;
 import com.od.jtimeseries.util.time.TimePeriod;
 import com.od.jtimeseries.util.time.TimeSource;
 import org.jfree.data.general.SeriesChangeEvent;
 import org.jfree.data.xy.AbstractXYDataset;
 
 import javax.swing.*;
 import java.lang.ref.WeakReference;
 import java.util.*;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 22/08/11
  * Time: 09:01
  *
  * Adapt one or more TimeSeries to the XYDataSet interface so that they can be charted in JFreeChart
  *
  * This class is mainly for use in Swing uis.
  * When the moving window is refreshed change events will be fired on the Swing event thread
  * (You could create a MovingWindowXYDataset in another thread as a one off snapshot, and it would be consistent view to the creating thread,
  * but in that case you can't use the moving window functionality.)
  *
  * Models used for Swing ui cannot be modified on threads other than the AWT event thread, if consistency is to be maintained.
  * This doesn't fit easily with time series which can be modified asynchronously.
  * To preserve consistency for Swing, this model keeps a snapshot of the data from each source series. The snapshots are only updated
  * on the AWT thread. When we refresh the window, new snapshots are created if the underlying series data has changed - since this
  * takes place on AWT thread the snapshots are safe to use for Swing models
  */
 public class MovingWindowXYDataset<E extends TimeSeries> extends AbstractXYDataset {
 
 
     private static ScheduledExecutorService scheduledExecutorService = NamedExecutors.newScheduledThreadPool(MovingWindowXYDataset.class.getSimpleName(), 2);
 
     private List<WrappedSourceSeries> sourceSeries = new ArrayList<WrappedSourceSeries>();
     private TimeSource startTimeSource = TimeSource.OPEN_START_TIME;
     private TimeSource endTimeSource = TimeSource.OPEN_END_TIME;
     private volatile boolean useSwingThread = true;
     private volatile Future movingWindowRefreshTask;
     private long currentStartTime = -1;
     private long currentEndTime = -1;
 
     public MovingWindowXYDataset() {
         this(TimeSource.OPEN_START_TIME, TimeSource.OPEN_END_TIME, false);
     }
 
     public MovingWindowXYDataset(TimeSource startTimeSource, TimeSource endTimeSource) {
         this(startTimeSource, endTimeSource, true);
     }
 
     public MovingWindowXYDataset(TimeSource startTimeSource, TimeSource endTimeSource, boolean useSwingThread) {
         this.startTimeSource = startTimeSource;
         this.endTimeSource = endTimeSource;
         this.useSwingThread = useSwingThread;
     }
 
     public void addTimeSeries(String key, E series) {
         sourceSeries.add(new WrappedSourceSeries(key, series));
         refresh(false);
     }
 
     public void setStartTimeSource(TimeSource s) {
         this.startTimeSource = s;
         refresh(true);
     }
 
     public void setEndTimeSource(TimeSource s) {
         this.endTimeSource = s;
         refresh(true);
     }
 
     public void startMovingWindow(TimePeriod timePeriod) {
         stopMovingWindow();
         MoveWindowTask t = new MoveWindowTask(this);
         movingWindowRefreshTask = scheduledExecutorService.scheduleWithFixedDelay(
                 t,
                 timePeriod.getLengthInMillis(),
                 timePeriod.getLengthInMillis(),
                 TimeUnit.MILLISECONDS
         );
         t.setFuture(movingWindowRefreshTask);
     }
 
     public void stopMovingWindow() {
         if ( movingWindowRefreshTask != null) {
             movingWindowRefreshTask.cancel(false);
         }
     }
 
     public E getTimeSeries(int index) {
         return sourceSeries.get(index).getSourceSeries();
     }
 
     public int getSeriesCount() {
         return sourceSeries.size();
     }
 
     public Comparable getSeriesKey(int series) {
         return sourceSeries.get(series).getKey();
     }
 
     public int getItemCount(int series) {
         return sourceSeries.get(series).size();
     }
 
     public Number getX(int series, int item) {
         return sourceSeries.get(series).getItem(item).getTimestamp();
     }
 
     public Number getY(int series, int item) {
         return sourceSeries.get(series).getItem(item).doubleValue();
     }
 
     private void refresh(final boolean forceRebuild) {
         //call refresh on the swing thread, to update the snapshots for any changed series
         Runnable refreshRunnable = new Runnable() {
             public void run() {
                 boolean changesExist = refreshSnapshotsForChangedSeries(forceRebuild);
 //                System.out.println("Changes " + changesExist);
                 if ( changesExist ) {
                     //fire the jfreechart change
                     seriesChanged(new SeriesChangeEvent(this));
                 }
             }
         };
 
         if (! SwingUtilities.isEventDispatchThread() && useSwingThread) {
             SwingUtilities.invokeLater(refreshRunnable);
         } else {
             refreshRunnable.run();
         }
 
 
     }
 
     private boolean refreshSnapshotsForChangedSeries(boolean forceRebuild) {
         currentStartTime = startTimeSource.getTime();
         currentEndTime = endTimeSource.getTime();
         boolean changesExist = false;
         for (WrappedSourceSeries s : sourceSeries) {
             changesExist |= s.refreshSnapshotData(forceRebuild);
         }
         return changesExist;
     }
 
     private class WrappedSourceSeries {
 
         private String key;
         private final E sourceSeries;
         private IndexedTimeSeries snapshotData = new DefaultTimeSeries();
         private TimeSeriesListener wrappedSeriesListener = new WrappedSeriesListener();
         private volatile long lastModCountOnEvent = -1;
         private volatile long modCountOnLastRefresh = -1;
         private int lastIndexFromSource = -1;
        private volatile boolean rebuildSnapshot = true;
 
         public WrappedSourceSeries(String key, E sourceSeries) {
             this.key = key;
             this.sourceSeries = sourceSeries;
             //use a weak ref listener, references from source series shouldn't cause this snapshot series / moving window series to be retained
             WeakReferenceTimeSeriesListener weakRefListener = new WeakReferenceTimeSeriesListener(sourceSeries, wrappedSeriesListener);
             sourceSeries.addTimeSeriesListener(weakRefListener);
         }
 
         public E getSourceSeries() {
             return sourceSeries;
         }
 
         public String getKey() {
             return key;
         }
 
         public int size() {
             return snapshotData.size();
         }
 
         public TimeSeriesItem getItem(int index) {
             return snapshotData.getItem(index);
         }
 
         /**
          * @return true, if items in snapshot changed as a result of refresh
          */
         public boolean refreshSnapshotData(boolean forceRebuild) {
             boolean modified = false;
             synchronized (sourceSeries) {
                 if (hasSourceSeriesBeenUpdated()) {
 
                     //if we haven't yet processed the latest event, we don't know whether that event was an append or a series change.
                     //Since we can't tell whether the source series has actually changed so much that the snapshot requires a rebuild, assume it has
                     if ( lastModCountOnEvent != sourceSeries.getModCount()) {
                         rebuildSnapshot = true;
                     }
 
                     if ( rebuildSnapshot || forceRebuild) {  //we need to rebuild completely
 //                        System.out.println("rebuild for " + key);
                         snapshotData = new DefaultTimeSeries(SeriesUtils.getItemsInRange(currentStartTime, currentEndTime, sourceSeries));
                         lastIndexFromSource = SeriesUtils.getIndexOfFirstItemAtOrBefore(currentEndTime, sourceSeries);
                         modified = true;
                     } else {  //source series changes since the last refresh didn't affect our current snapshot, we can simply remove and append
 //                        System.out.println("Append for " + key);
                         modified |= removeFromStartOfSnapshotSeries();
                         modified |= addToEndOfSnapshotSeries();
                     }
 
                     //ignore any more events until after this modCount
                     modCountOnLastRefresh = sourceSeries.getModCount();
                     rebuildSnapshot = false;
                 }
             }
             return modified;
         }
 
         private boolean hasSourceSeriesBeenUpdated() {
             return modCountOnLastRefresh != sourceSeries.getModCount();
         }
 
         private boolean addToEndOfSnapshotSeries() {
             boolean modified = false;
             for (int loop=lastIndexFromSource + 1; loop < sourceSeries.size(); loop++) {
                 TimeSeriesItem i = sourceSeries.getItem(loop);
                 if ( i.getTimestamp() <= currentEndTime) {
                     snapshotData.addItem(i);
                     lastIndexFromSource = loop;
                     modified = true;
                 } else {
                     break;
                 }
             }
             return modified;
         }
 
         private boolean removeFromStartOfSnapshotSeries() {
             boolean modified = false;
             Iterator<TimeSeriesItem> i = snapshotData.iterator();
             while(i.hasNext()) {
                 if ( i.next().getTimestamp() < currentStartTime) {
                     i.remove();
                     modified = true;
                 } else {
                     break;
                 }
             }
             return modified;
         }
 
         private class WrappedSeriesListener implements TimeSeriesListener {
 
             public void itemsAddedOrInserted(TimeSeriesEvent e) {
                 if (shouldProcessEvent(e)) {
                     rebuildIfCurrentSnapshotAffected(e);
                 }
                 lastModCountOnEvent = e.getSeriesModCount();
             }
 
             public void itemsRemoved(TimeSeriesEvent e) {
                 if (shouldProcessEvent(e)) {
                     rebuildIfCurrentSnapshotAffected(e);
                 }
                 lastModCountOnEvent = e.getSeriesModCount();
             }
 
             public void seriesChanged(TimeSeriesEvent e) {
                 if (shouldProcessEvent(e)) {
                     rebuildSnapshot = true;
                 }
                 lastModCountOnEvent = e.getSeriesModCount();
             }
 
             private void rebuildIfCurrentSnapshotAffected(TimeSeriesEvent e) {
                 if (SeriesUtils.fallsWithinRange(e.getFirstItemTimestamp(), snapshotData.getEarliestTimestamp(), snapshotData.getLatestTimestamp()) ||
                     SeriesUtils.fallsWithinRange(e.getLastItemTimestamp(), snapshotData.getEarliestTimestamp(), snapshotData.getLatestTimestamp())) {
                     rebuildSnapshot = true;
                 }
             }
 
             //sometimes a refresh can trigger and we can update snapshot to a later point before receiving event fired asynchronously
             private boolean shouldProcessEvent(TimeSeriesEvent e) {
                 return modCountOnLastRefresh < e.getSeriesModCount();
             }
         }
     }
 
     //a task to fresh the window, keeping only a weak reference to the XYDataset to
     //prevent the executor tasks keeping it in memory once other references have cleared
     //cancel the task once the XYDataset is collected
     private static class MoveWindowTask implements Runnable {
 
         private WeakReference<MovingWindowXYDataset> xyDatasetWeakReference;
         private Future future;
 
         public MoveWindowTask(MovingWindowXYDataset xyDataset) {
             xyDatasetWeakReference = new WeakReference<MovingWindowXYDataset>(xyDataset);
         }
 
         public void setFuture(Future future) {
             this.future = future;
         }
 
         public void run() {
             MovingWindowXYDataset d = xyDatasetWeakReference.get();
             if ( d != null) {
                 d.refresh(false);
             } else {
                 //xy dataset is collected, cancel this task
                 future.cancel(false);
             }
         }
     }
 }
