 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package edu.rpi.sss.util;
 
 import java.util.HashMap;
 
 /** A map which will be flushed after a certain time or when it reaches a
  * certain size.
  *
  * @author douglm
  *
  * @param <K> Key class
  * @param <V> Value class
  */
 public class FlushMap<K,V> extends HashMap<K,V> {
   private long lastFlush;
   private long flushTime;
   private int maxSize;
 
   private final static int defaultMaxSize = 1000;
   private final static long defaultFlushTime = 60 * 1000 * 10;  // 10 minutes
 
   /** Create a FlushMap with default maxize and flush period.
    */
   public FlushMap() {
     flushTime = defaultFlushTime;
     maxSize = defaultMaxSize;
   }
 
   /** Create a FlushMap with given size, default maxize and flush period.
    *
    * @param size
    */
   public FlushMap(final int size) {
     super(size);
 
     flushTime = defaultFlushTime;
     maxSize = defaultMaxSize;
   }
 
   /** Create a FlushMap with default size and specified maxsize and flush period.
    *
   * @param flushTime 0 means no flush time
    * @param maxSize - 0 means no max size
    */
   public FlushMap(final long flushTime,
                   final int maxSize) {
     this.flushTime = flushTime;
     this.maxSize = maxSize;
   }
 
   /** Create a FlushMap with specified size, maxsize and flush period.
    *
    * @param size
   * @param flushTime 0 means no flush time
    * @param maxSize - 0 means no max size
    */
   public FlushMap(final int size,
                   final long flushTime,
                   final int maxSize) {
     super(size);
 
     this.flushTime = flushTime;
     this.maxSize = maxSize;
   }
 
   /** Override this to modify the behavior - perhaps to preserve some special
    * entries.
    *
    * @return true if we flushed.
    */
   protected boolean testFlush() {
     boolean flushed = false;
 
     if (flushTime > 0) {
       if ((lastFlush != 0) &&
           ((System.currentTimeMillis() - lastFlush) > flushTime)) {
         clear();
         flushed = true;
       }
     }
 
     if (maxSize <= 0) {
       return flushed;
     }
 
     if (size() >= maxSize) {
       clear();
       flushed = true;
     }
 
     return flushed;
   }
 
   @Override
   public boolean containsKey(final Object key) {
     testFlush();
     return super.containsKey(key);
   }
 
   @Override
   public synchronized V put(final K key, final V val) {
     testFlush();
 
     return super.put(key, val);
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public V get(final Object key) {
     V val = super.get(key);
 
     if (val == null) {
       return null;
     }
 
     if (testFlush()) {
       super.put((K)key, val);
     }
 
     return val;
   }
 }
