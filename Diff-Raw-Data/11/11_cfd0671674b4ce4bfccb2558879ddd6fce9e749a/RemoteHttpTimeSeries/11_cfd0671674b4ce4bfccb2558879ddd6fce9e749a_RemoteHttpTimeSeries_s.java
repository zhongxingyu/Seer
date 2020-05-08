 /**
  * Copyright (C) 2009 (nick @ objectdefinitions.com)
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
 package com.od.jtimeseries.ui.timeseries;
 
 import com.od.jtimeseries.net.httpd.HttpParameterName;
 import com.od.jtimeseries.ui.config.UiTimeSeriesConfig;
 import com.od.jtimeseries.ui.util.ExecuteWeakReferencedCommandTask;
 import com.od.jtimeseries.util.NamedExecutors;
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 import com.od.jtimeseries.util.time.Time;
 import com.od.jtimeseries.util.time.TimePeriod;
 import swingcommand.BackgroundTask;
 import swingcommand.SwingCommand;
 import swingcommand.Task;
 import swingcommand.TaskListenerAdapter;
 
 import javax.swing.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 12-Jan-2009
  * Time: 09:43:39
  *
  * A time series used by the ui, which loads its data from a remote server
  *
  * It is assumed that the extra methods this class provides over and above the standard TimeSeries methods are
  * to be called on the AWT only, and so there is no need for extra synchronization here. Furthermore property change
  * events should only be fired on the AWT
  */
 public class RemoteHttpTimeSeries extends DefaultUITimeSeries implements ChartSeriesListener {
 
     private static final long STARTUP_TIME = System.currentTimeMillis();
 
    private static final LogMethods logMethods = LogUtils.getLogMethods(ChartingTimeSeries.class);
 
    private final Map<UIPropertiesTimeSeries, UIPropertiesTimeSeries> weakClientSeries = new WeakHashMap<UIPropertiesTimeSeries, UIPropertiesTimeSeries>();
 
     private static ScheduledExecutorService refreshExecutor = NamedExecutors.newSingleThreadScheduledExecutor("RemoteHttpTimeSeriesRefresh");
     private static final int MIN_REFRESH_TIME_SECONDS = 10;
 
     private RefreshDataCommand refreshDataCommand = new RefreshDataCommand();
     private volatile ScheduledFuture refreshTask;
     private volatile int displayedChartCount;
     private volatile int errorCount;
     private volatile boolean ticking = true;
 
     //a series starts not 'stale' and remains not stale until a set number of consecutive download failures have occurred
     private static final int MAX_ERRORS_BEFORE_DISCONNECT = 4;
     private static final int NOT_TICKING_REFRESH_TIME_SECONDS = 1800; //half an hour
     private static final int TICKING_FLAG_HOURS_SINCE_LAST_UPDATE = 4; //if no new data for this time a series is considered 'not ticking'
 
 
     RemoteHttpTimeSeries(UiTimeSeriesConfig config) throws MalformedURLException {
         this(config.getId(), config.getDescription(), new URL(config.getTimeSeriesUrl()), Time.seconds(config.getRefreshTimeSeconds()));
         setDisplayName(config.getDisplayName());
     }
 
     RemoteHttpTimeSeries(String id, String description, URL timeSeriesUrl, TimePeriod refreshTime) {
         super(id, description);
         setTimeSeriesURL(timeSeriesUrl);
         setRefreshFrequencySeconds((int) refreshTime.getLengthInMillis() / 1000);
     }
 
     public void setRefreshFrequencySeconds(int seconds) {
         super.setRefreshFrequencySeconds(Math.max(seconds, MIN_REFRESH_TIME_SECONDS));
         scheduleRefreshIfDisplayed(false);
     }
 
     public void setStale(boolean stale) {
         if (!stale) {
             errorCount = 0;
         }
         super.setStale(stale);
     }
 
     //Cancel any existing task and schedule a new one if series selected
     private synchronized void scheduleRefreshIfDisplayed(boolean immediateRefresh) {
         updateDisplayedChartCount();
 
         if ( refreshTask != null) {
             refreshTask.cancel(false);
         }
 
         if ( displayedChartCount > 0) {
             //A task which doesn't hold a strong reference to this series
             //the series can be collected if no longer referenced elsewhere, even if a refresh is scheduled
             ExecuteWeakReferencedCommandTask runCommandTask = new ExecuteWeakReferencedCommandTask(refreshDataCommand);
 
             if ( immediateRefresh) {
                 refreshExecutor.execute(runCommandTask);
             }
 
             int refreshTime = calculateRefreshTime();
             refreshTask = refreshExecutor.schedule(
                 runCommandTask, refreshTime, TimeUnit.SECONDS
             );
         }
     }
 
     private int calculateRefreshTime() {
         return ticking ? getRefreshFrequencySeconds() : NOT_TICKING_REFRESH_TIME_SECONDS;
     }
 
     /**
      * A flag which indicates whether a series in the server is currently 'active' / growing
      * If not, we don't want to hit the server frequently to get new data points
      */
     private boolean setTickingFlag() {
         long timeSinceUpdate = System.currentTimeMillis() - getLatestTimestamp();
 
          //enough time for local timeserious generated metrics to start to get datapoints
         boolean justStarted = System.currentTimeMillis() - STARTUP_TIME < 60000;
 
         boolean ticking = timeSinceUpdate < Time.hours(TICKING_FLAG_HOURS_SINCE_LAST_UPDATE).getLengthInMillis() || justStarted;
         boolean result = ticking != this.ticking;
         this.ticking = ticking;
         return result;
     }
 
     public void chartSeriesChanged(ChartSeriesEvent e) {
         boolean refreshImmediately = false;
         switch(e.getChartSeriesEventType()) {
             case SERIES_CHART_DISPLAYED:
                 refreshImmediately = true;
                weakClientSeries.put(e.getSourceSeries(), e.getSourceSeries());
                 break;
             case SERIES_CHART_HIDDEN:
                weakClientSeries.put(e.getSourceSeries(), e.getSourceSeries());
                 break;
             case SERIES_CHART_DISPOSED:
                 weakClientSeries.remove(e.getSourceSeries());
                 break;
             default:
         }
         scheduleRefreshIfDisplayed(refreshImmediately);
     }
 
     private void updateDisplayedChartCount() {
         Set<UIPropertiesTimeSeries> series;
         synchronized(weakClientSeries) {
             series = new HashSet<UIPropertiesTimeSeries>(weakClientSeries.keySet());
         }
 
         int displayedCount = 0;
         for ( UIPropertiesTimeSeries s : series) {
             if ( s.isSelected()) {
                 displayedCount++;
             }
         }
         this.displayedChartCount = displayedCount;
     }
 
     private class RefreshDataCommand extends SwingCommand {
 
         public RefreshDataCommand() {
 
             //if we exceed the error count when running the load, set the series to stale
             //the user will need to re-enable it to start the load off again
             addTaskListener(new SetStaleOnErrorListener());
             addTaskListener(new RescheduleListener());
         }
 
         protected Task createTask() {
             return new BackgroundTask() {
                 protected void doInBackground() throws Exception {
                     if ( ! isStale() ) {
                         URL urlForQuery = getUrlWithTimestamp();
                         new DownloadRemoteTimeSeriesDataQuery(RemoteHttpTimeSeries.this, urlForQuery).runQuery();
                     }
                 }
 
                 private URL getUrlWithTimestamp() throws MalformedURLException {
                     return new URL(
                         getTimeSeriesURL() + "?" + HttpParameterName.moreRecentThanTimestamp.name() + "=" + getEarliestItemToFetch() + "&" + HttpParameterName.statsOnly + "=" + (displayedChartCount == 0)
                     );
                 }
 
                 //the later of the last current timepoint or the earliest point calculated using max days history
                 private long getEarliestItemToFetch() {
                     return getLatestTimestamp();
                 }
 
                 protected void doInEventThread() throws Exception {
                     errorCount = 0;
                     //presently the command still runs even if we are stale, but
                     //if stale the remote call won't take place, so an exception is not thrown
                     //and the doInEventThread will get called
                     //this doesn't count as a proper refresh
                     if ( ! isStale()) {
                         setLastRefreshTime(new Date());
                     }
                 }
             };
         }
 
         //perform disconnection if task failed too many times
         private class SetStaleOnErrorListener extends TaskListenerAdapter {
 
             public void error(Task task, Throwable error) {
                 errorCount++;
                 if ( errorCount >= MAX_ERRORS_BEFORE_DISCONNECT) {
                     setStale(true);
                 }
             }
         }
 
         private class RescheduleListener extends TaskListenerAdapter {
             public void finished(Task task) {
                 setTickingFlag();
                 scheduleRefreshIfDisplayed(false);
             }
         }
     }
 
     public static void updateSummaryStats(UiTimeSeriesConfig config, Properties summaryStats) {
         final RemoteHttpTimeSeries s = RemoteHttpTimeSeriesCollection.getWeakReferencedSeries(config);
         if (s != null) {
             s.updateSummaryStats(summaryStats);
         }
     }
 
     protected void updateSummaryStats(Properties summaryStats) {
         putAllProperties(summaryStats);
 
         //at present all UI times series properties must
         //be set on the swing event thread since jide
         //bean table model expects this
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 setStatsRefreshTime(new Date());
             }
         });
     }
 
 }
