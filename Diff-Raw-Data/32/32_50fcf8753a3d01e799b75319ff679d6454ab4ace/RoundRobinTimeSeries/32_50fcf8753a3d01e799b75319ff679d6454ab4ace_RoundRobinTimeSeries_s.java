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
 package com.od.jtimeseries.timeseries.impl;
 
 import com.od.jtimeseries.source.Counter;
 import com.od.jtimeseries.timeseries.TimeSeriesItem;
 
 import java.util.Collection;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nick
  * Date: 17-May-2009
  * Time: 22:04:23
  * To change this template use File | Settings | File Templates.
  *
  * A time series which has a maximum size / number of items
  *
  * When the maximum size is reached, subsequent additions will cause the earliest
  * items to drop off.
  */
 public class RoundRobinTimeSeries extends DefaultTimeSeries {
 
     //count maintained to show how many series have been released for gc
     public static volatile Counter garbageCollectionCounter;
 
     private int maxSize;
 
     public RoundRobinTimeSeries(int maxSize) {
         this.maxSize = maxSize;
     }
 
     public RoundRobinTimeSeries(Collection<TimeSeriesItem> items, int maxSize) {
         super(items);
         this.maxSize = maxSize;
         checkSize();
     }
 
     public void locked_addAllWithoutFiringEvents(Collection<TimeSeriesItem> i) {
         super.locked_addAllWithoutFiringEvents(i);
         checkSize();
     }
 
     public void locked_addItem(TimeSeriesItem i) {
         super.locked_addItem(i);
         checkSize();
     }
 
     private void checkSize() {
         while (size() > maxSize) {
             removeItem(getEarliestItem());
         }
     }
 
     public int getMaxSize() {
         return maxSize;
     }
 
     public static void setGarbageCollectionCounter(Counter c) {
         garbageCollectionCounter = c;
     }
 
     public void finalize() throws Throwable {
         super.finalize();
         try {
             if ( garbageCollectionCounter != null) {
                 garbageCollectionCounter.incrementCount();
             }
         } catch (Throwable t) {
             //to be double safe, catch all throwable here, since we don't ever want to risk
             //preventing gc
             t.printStackTrace();
         }
     }
 
 }
