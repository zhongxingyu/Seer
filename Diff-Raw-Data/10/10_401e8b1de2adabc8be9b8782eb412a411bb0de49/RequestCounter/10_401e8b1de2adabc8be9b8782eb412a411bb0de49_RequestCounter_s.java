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
 
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * Computes Number of requests handled. This counter
  * uses {@link AtomicLong} values to store counts
  * providing maximum concurrency support.
  */
 public class RequestCounter implements ResetEnabled {
 
     /**
      * Stores Total Number of requests.
      */
     private static AtomicLong totalRequests = new AtomicLong();
 
     /**
      * Lock used for reset operation.
      */
     private final ReentrantLock resetLock = new ReentrantLock();
 
     /**
      * Stores Number of requests counted by this
      * instance.
      */
     private AtomicLong requests = new AtomicLong();
 


     /**
      * This method increments the number of requests by one.
      */
     public synchronized void increment() {
         while (resetLock.isLocked()) {
             // wait for lock to release.
             try {
                 Thread.sleep(1);
             } catch (InterruptedException e) {}
         }
         requests.incrementAndGet();
         totalRequests.incrementAndGet();
     }
 
     /**
      * Resets the counters.
      */
     public synchronized void reset() {
         resetLock.lock();
         try { 
             totalRequests = new AtomicLong();
             requests = new AtomicLong();
         } finally {
             resetLock.unlock();
         }
     }
 
     /**
      * Gets Number of requests counted by this
      * instance.
      *
      * @return number of requests counted by this
      * instance.
      */
     public long getRequests() {
         return requests.get();
     }
 
     /**
      * Gets Total Number of requests.
      *
      * @return total Number of requests.
      */
     public long getTotalRequests() {
         return totalRequests.get();
     }
 }
