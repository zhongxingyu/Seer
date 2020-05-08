 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hadoop.hoya.yarn.appmaster.state;
 
 import org.apache.hadoop.hoya.api.StatusKeys;
 import org.apache.hadoop.hoya.providers.ProviderRole;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * Models the ongoing status of all nodes in  
  * Nothing here is synchronized: grab the whole instance to update.
  */
 public final class RoleStatus implements Cloneable {
 
 
   private final String name;
 
 
   /**
    * Role key in the container details stored in the AM,
    * currently mapped to priority
    */
   private final int key;
 
   private final ProviderRole providerRole;
 
   private int desired, actual, requested, releasing;
   private int failed, started, startFailed, completed, totalRequested;
 
 
   public RoleStatus(ProviderRole providerRole) {
     this.providerRole = providerRole;
     this.name = providerRole.name;
     this.key = providerRole.id;
   }
 
   public String getName() {
     return name;
   }
 
   public int getKey() {
     return key;
   }
 
   public int getPriority() {
     return getKey();
   }
 
   public boolean getExcludeFromFlexing() {
     return providerRole.excludeFromFlexing;
   }
 
   public int getDesired() {
     return desired;
   }
 
   public void setDesired(int desired) {
     this.desired = desired;
   }
 
   public int getActual() {
     return actual;
   }
 
   public int incActual() {
     return ++actual;
   }
 
   public int decActual() {
     if (0 > --actual) {
       actual = 0;
     }
     return actual;
   }
 
  public synchronized int getRequested() {
     return requested;
   }
 
   public synchronized int incRequested() {
     totalRequested++;
     return ++requested;
   }
 
   public synchronized int decRequested() {
     if (0 > --requested) {
       requested = 0;
     }
     return requested;
   }
 
   public int getReleasing() {
     return releasing;
   }
 
   public int incReleasing() {
     return ++releasing;
   }
 
   public int decReleasing() {
     if (0 > --releasing) {
       releasing = 0;
     }
     return releasing;
   }
 
   public int getFailed() {
     return failed;
   }
 
   public void incFailed() {
     failed++;
   }
 
   public int getStartFailed() {
     return startFailed;
   }
 
   public void incStartFailed() {
     startFailed++;
   }
 
   public int getCompleted() {
     return completed;
   }
 
   public void setCompleted(int completed) {
     this.completed = completed;
   }
 
   public void incCompleted() {
     completed ++;
   }
   public int getStarted() {
     return started;
   }
 
   public void incStarted() {
     started++;
   }
 
   public int getTotalRequested() {
     return totalRequested;
   }
 
   /**
    * Get the number of roles we are short of.
    * nodes released are ignored.
    * @return the positive or negative number of roles to add/release.
    * 0 means "do nothing".
    */
   public synchronized int getDelta() {
     int inuse = actual + requested;
     //don't know how to view these. Are they in-use or not?
     //inuse += releasing;
     int delta = desired - inuse;
     if (delta < 0) {
       //if we are releasing, remove the number that are already released.
       delta += releasing;
       //but never switch to a positive
       delta = Math.min(delta, 0);
     }
     return delta;
   }
 
   @Override
   public String toString() {
     return "RoleStatus{" +
            "name='" + name + '\'' +
            ", key=" + key +
            ", desired=" + desired +
            ", actual=" + actual +
            ", requested=" + requested +
            ", releasing=" + releasing +
            ", failed=" + failed +
            ", started=" + started +
            ", startFailed=" + startFailed +
            ", completed=" + completed +
            '}';
   }
 
   @Override
   public Object clone() throws CloneNotSupportedException {
     return super.clone();
   }
 
   /**
    * Get the provider role
    * @return
    */
   public ProviderRole getProviderRole() {
     return providerRole;
   }
 
   /**
    * Build the statistics map from the current data
    * @return a map for use in statistics reports
    */
   public Map<String, Integer> buildStatistics() {
     Map<String, Integer> stats = new HashMap<String, Integer>();
     stats.put(StatusKeys.STAT_CONTAINERS_ACTIVE_REQUESTS, getRequested());
     stats.put(StatusKeys.STAT_CONTAINERS_COMPLETED, getCompleted());
     stats.put(StatusKeys.STAT_CONTAINERS_DESIRED, getDesired());
     stats.put(StatusKeys.STAT_CONTAINERS_FAILED, getFailed());
     stats.put(StatusKeys.STAT_CONTAINERS_LIVE, getActual());
     stats.put(StatusKeys.STAT_CONTAINERS_REQUESTED, getTotalRequested());
     stats.put(StatusKeys.STAT_CONTAINERS_STARTED, getStarted());
     stats.put(StatusKeys.STAT_CONTAINERS_START_FAILED, getStartFailed());
     return stats;
   }
 }
