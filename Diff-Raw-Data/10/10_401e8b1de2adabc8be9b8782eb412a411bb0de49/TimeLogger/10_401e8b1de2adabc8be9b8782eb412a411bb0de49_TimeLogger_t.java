 /*
  * SCI-Flex: Flexible Integration of SOA and CEP
  * Copyright (C) 2008, 2009  http://sci-flex.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.sciflex.plugins.synapse.esper.core.util;
 
 import java.util.concurrent.locks.ReentrantLock;
 
 /*
  * This class was generously borrowed from WSO2 Statistics
  * to be used as a Request/Response Time statistic computing
  * utility.
  */
 
 /**
  * Computes Request/Response Time statistics.
  */
 public class TimeLogger implements ResetEnabled {
 
     /**
      * Stores Maximum Time tracked so far.
      */
     private long maxTime = 0;
 
     /**
      * Stores Minimum Time tracked so far, or -1,
      * if not yet started.
      */
     private long minTime = -1;
 
     /**
      * Stores Total Time tracked so far.
      */
     private double totalTime = 0;
 
     /**
      * Stores Total number of calls to {@link #add}.
      */
     private long totalCount = 0; 
 
     /**
      * Stores Average Time.
      */
     private double avgTime = 0;
 
     /**
      * Lock used for reset operation.
      */
     private final ReentrantLock resetLock = new ReentrantLock();
 
     /**
      * Resets the counters.
      */
     public synchronized void reset() {
         resetLock.lock();
         try {
             maxTime = 0;
             minTime = -1;
             totalTime = 0;
             totalCount = 0;
             avgTime = 0;
         } finally {
             resetLock.unlock();
         }
     }
 
     /**
      * This method adds the incoming time, to the logger
      * It will determine the new min/max/avg/total time,
      * and also will increment the count by 1.
      *
      * @param time time to add to logger.
      */
     public synchronized void add(long time) {
         while (resetLock.isLocked()) {
             // wait for lock to release.
             try {
                 Thread.sleep(1);
             } catch (InterruptedException e) {}
         }
         if (maxTime < time) {
             maxTime = time;
         }
 
         if (minTime > time) {
             minTime = time;
         } else if (minTime == -1) {
             minTime = time;
         }
 
         totalTime = totalTime + time;
         totalCount++;
 
         // We calculate average time in here in order
         // to synchronize the call.
         avgTime = totalTime / totalCount;
     }
 
     /**
      * Gets Maximum Time logged.
      *
      * @return maximum time.
      */
     public long getMaxTime() {
         return maxTime;
     }
 
     /**
      * Gets Average Time logged.
      *
      * @return average time.
      */
     public double getAvgTime() {
         return avgTime;
     }
 
     /**
      * Gets Minimum Time logged.
      *
      * @return minimum time.
      */
     public long getMinTime() {
         if (minTime == -1) {
             return 0;
         }
         return minTime;
     }
 }
