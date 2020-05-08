 package com.od.jtimeseries.ui.timeseries;
 
 import com.od.jtimeseries.ui.config.UiTimeSeriesConfig;
 import com.od.jtimeseries.ui.util.InternStringFieldOptimiser;
 
 import java.awt.*;
 import java.net.URL;
 import java.util.Date;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 29-Nov-2010
  * Time: 13:31:30
  */
 public class DefaultUITimeSeries extends PropertyChangeTimeSeries implements UIPropertiesTimeSeries {
 
     private static InternStringFieldOptimiser<URL> urlOptimiser = new InternStringFieldOptimiser<URL>(URL.class, "host", "protocol", "authority");
     private static final ColorRotator colorRotator = new ColorRotator();
     private static final int MIN_REFRESH_TIME_SECONDS = 10;
     private static final int DEFAULT_REFRESH_FREQUENCY_SECONDS = 300;
 
     private volatile int refreshFrequencySeconds = DEFAULT_REFRESH_FREQUENCY_SECONDS;
     private volatile boolean selected;
     private volatile boolean stale;
     private volatile String displayName;
     private Date lastRefreshTime;
     private URL timeSeriesUrl;
     private Color color = colorRotator.getNextColor();
     private Date statsRefreshTime;
 
     public DefaultUITimeSeries(String id, String description) {
         //intern the id and descriptions since there is generally massive duplication with these
         //we'll accept the risk the out of permgen space error which could result from too many differing long descriptions
         //in exchange for the big reduction in overall memory usage
         super(id.intern(), description.intern());
     }
 
     public boolean isStale() {
         return stale;
     }
 
     public void setStale(boolean stale) {
         boolean oldValue = this.stale;
         this.stale = stale;
         firePropertyChange(STALE_PROPERTY, oldValue, stale);
         fireNodeChanged(UIPropertiesTimeSeries.STALE_PROPERTY);
     }
 
     public boolean isSelected() {
         return selected;
     }
 
     public void setSelected(boolean selected) {
         boolean oldValue = this.selected;
         this.selected = selected;
         firePropertyChange(UIPropertiesTimeSeries.SELECTED_PROPERTY, oldValue, selected);
         fireNodeChanged(UIPropertiesTimeSeries.SELECTED_PROPERTY);
     }
 
     public String getDisplayName() {
         if ( displayName == null ) {
             setDisplayName(getId());
         }
         return displayName;
     }
 
     public void setDisplayName(String displayName) {
         String oldValue = this.displayName;
         this.displayName = displayName;
         firePropertyChange(UIPropertiesTimeSeries.DISPLAY_NAME_PROPERTY, oldValue, displayName);
         fireNodeChanged(UIPropertiesTimeSeries.DISPLAY_NAME_PROPERTY);
     }
 
     public Date getLastRefreshTime() {
         return lastRefreshTime;
     }
 
     public void setLastRefreshTime(Date time) {
         Date oldValue = lastRefreshTime;
         this.lastRefreshTime = time;
         firePropertyChange(UIPropertiesTimeSeries.LAST_REFRESH_TIME_PROPERTY, oldValue, time);
         fireNodeChanged(UIPropertiesTimeSeries.LAST_REFRESH_TIME_PROPERTY);
     }
 
     public URL getTimeSeriesURL() {
         return timeSeriesUrl;
     }
 
     public void setTimeSeriesURL(URL url) {
         URL oldValue = this.timeSeriesUrl;
         timeSeriesUrl = url;
         urlOptimiser.optimise(timeSeriesUrl);
         firePropertyChange(URL_PROPERTY_NAME, oldValue, url);
     }
 
     public int getRefreshFrequencySeconds() {
         return refreshFrequencySeconds;
     }
 
     public void setRefreshFrequencySeconds(int refreshTimeSeconds) {
         long oldValue = this.refreshFrequencySeconds;
         this.refreshFrequencySeconds = Math.max(refreshTimeSeconds, MIN_REFRESH_TIME_SECONDS);
         firePropertyChange(UIPropertiesTimeSeries.REFRESH_FREQUENCY_PROPERTY, oldValue, this.refreshFrequencySeconds);
         fireNodeChanged(UIPropertiesTimeSeries.REFRESH_FREQUENCY_PROPERTY);
     }
 
     public Date getStatsRefreshTime() {
         return statsRefreshTime;
     }
 
     public UiTimeSeriesConfig getConfig() {
        return getConfig();
     }
 
     public static UiTimeSeriesConfig getConfig(UIPropertiesTimeSeries s) {
         return new UiTimeSeriesConfig(
             s.getParentPath(),
             s.getId(),
             s.getDescription(),
             s.getTimeSeriesURL().toExternalForm(),
             s.getRefreshFrequencySeconds(),
             s.isSelected(),
             s.getDisplayName(),
             s.getColor()
         );
     }
 
     public void setStatsRefreshTime(Date statsRefreshTime) {
         Date oldValue = this.statsRefreshTime;
         this.statsRefreshTime = statsRefreshTime;
         firePropertyChange(UIPropertiesTimeSeries.STATS_REFRESH_TIME_PROPERTY, oldValue, this.statsRefreshTime);
     }
 
     public Color getColor() {
         return color;
     }
 
     public void setColor(Color color) {
         Color oldColor = this.color;
         this.color = color;
         firePropertyChange(UIPropertiesTimeSeries.COLOUR_PROPERTY, oldColor, color);
         fireNodeChanged(UIPropertiesTimeSeries.COLOUR_PROPERTY);
     }
 }
