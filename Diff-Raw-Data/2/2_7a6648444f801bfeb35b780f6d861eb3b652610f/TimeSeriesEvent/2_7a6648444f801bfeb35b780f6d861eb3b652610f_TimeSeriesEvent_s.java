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
 package com.od.jtimeseries.timeseries;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * An event representing a change to a TimeSeries
  *
  * Future performance enhancements may require the reuse of TimeSeriesEvent instances, which are therefore not guaranteed to be immutable
  *
  * Because of this, it is necessary to avoid accessing received TimeSeriesEvent instances outside TimeSeriesListener callback methods.
  * If it seems necessary to keep a reference to an event outside the listener callback, create a clone of the event and store that instead.
  */
 public class TimeSeriesEvent implements Cloneable {
 
     private List<TimeSeriesItem> items;
     private EventType eventType;
     private Object source;
     private long seriesModCount;
 
     /**
      * @param source        - time series source for event
      * @param items         - list of items affected in order of timestamp
      */
     protected TimeSeriesEvent(Object source, List<TimeSeriesItem> items, EventType eventType, long seriesModCount) {
         this.items = items;
         this.source = source;
         this.eventType = eventType;
         this.seriesModCount = seriesModCount;
     }
 
     protected TimeSeriesEvent() {
     }
 
     public long getFirstItemTimestamp() {
         return items.get(0).getTimestamp();
     }
 
     public long getLastItemTimestamp() {
         return items.get(items.size() - 1).getTimestamp();
     }
 
     /**
      * Get a list of the items affected.
      *
      * Possible performance enhancements may require the returned list instance to be reused by subsequent events, so to
      * guarantee thread safety the instance should not be used outside the context of the TimeSeriesListener's event handling callback method.
      * (If you need to store the List of items for later processing, please clone it, then store the clone)
      *
      * You should not modify the contents of the List returned by this method since it may be reused by other TimeSeriesListener
      *
      * @return items affected in order of timestamp
      */
     public List<TimeSeriesItem> getItems() {
         return items;
     }
 
     /**
      * @return time series source of the event
      */
     public Object getSource() {
         return source;
     }
 
     /**
      * @return EventType for timeseries event
      */
     public EventType getEventType() {
         return eventType;
     }
 
     public boolean isAppend() {
        return eventType == EventType.APPEND;
     }
 
     public long getSeriesModCount() {
         return seriesModCount;
     }
 
     public static TimeSeriesEvent createEvent(Object source, List<TimeSeriesItem> items, EventType eventType, long seriesModCount) {
         return new TimeSeriesEvent(source, items, eventType, seriesModCount);
     }
 
     /**
      * A range of items was appended to the series
      *
      * @param source of event
      * @param items - items added
      */
     public static TimeSeriesEvent createItemsAppendedOrInsertedEvent(Object source, List<TimeSeriesItem> items, long seriesModCount, boolean isAppend) {
         return new TimeSeriesEvent(source, items, isAppend ? EventType.APPEND : EventType.INSERT, seriesModCount);
     }
 
     /**
      * A range of items in the series were removed
      *
      * @param source of event
      * @param items - items removed
      */
     public static TimeSeriesEvent createItemsRemovedEvent(Object source, List<TimeSeriesItem> items, long seriesModCount) {
         return new TimeSeriesEvent(source, items, EventType.REMOVE, seriesModCount);
     }
 
     /**
      * The time series changed in a way which could not be efficiently
      * represented using the other event types
      *
      * @param source of event
      * @param items - items in the series after change
      */
     public static TimeSeriesEvent createSeriesChangedEvent(Object source, List<TimeSeriesItem> items, long seriesModCount) {
         return new TimeSeriesEvent(source, items, EventType.SERIES_CHANGE, seriesModCount);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         TimeSeriesEvent that = (TimeSeriesEvent) o;
 
         if (eventType != that.eventType) return false;
         if (items != null ? !items.equals(that.items) : that.items != null) return false;
         if (source != null ? !source.equals(that.source) : that.source != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = items != null ? items.hashCode() : 0;
         result = 31 * result + (eventType != null ? eventType.hashCode() : 0);
         result = 31 * result + (source != null ? source.hashCode() : 0);
         return result;
     }
 
     public Object clone() {
         return TimeSeriesEvent.createEvent(source, new ArrayList<TimeSeriesItem>(items), eventType, seriesModCount);
     }
 
     public String toString() {
         return "TimeSeriesEvent{" + eventType +
                 (items.size() < 10 ? ", items " + items : ", first 10 items=" + items.subList(0, 10)) +
                 ", source=" + source + ", modCount=" + seriesModCount +
                 '}';
     }
 
     public static enum EventType {
         APPEND,
         INSERT,
         REMOVE,
         SERIES_CHANGE
     }
 }
