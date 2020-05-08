 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.myfaces.scripting.refresh;
 
 import org.apache.myfaces.scripting.core.dependencyScan.api.DependencyRegistry;
 import org.apache.myfaces.scripting.core.dependencyScan.registry.MasterDependencyRegistry;
 import org.apache.myfaces.scripting.core.util.WeavingContext;
 
 import javax.faces.context.FacesContext;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  *          <p/>
  *          a context which holds information regarding the refresh cycle
  *          which can be picked up by the request filter
  *          for refreshing strategies
  *          <p/>
  *          That way we can avoid a separate session filter and a push system
  *          we use a pull system instead
  */
 
 public class RefreshContext {
     /**
      * this is a timed marker which is
      * a point in time the last bean refresh was issued
      * every request has to dump its personal scoped
      * (aka session, or custom) scoped beans
      * if the point in time is newer than the personal refresh time
      * application scoped beans are refreshed at the first refresh cycle
      * by the calling request issuing the compile
      */
     private volatile long personalScopedBeanRefresh = -1l;
 
     /**
      * the daemon thread which marks the scripting classes
      * depending on the state, changed => tainted == true, not changed
      * tainted == false!
      */
     volatile FileChangedDaemon daemon = FileChangedDaemon.getInstance();
 
     /**
      * the bean synchronisation has to be dealt with
      * differently, we have two volatile points in the lifecycle
      * one being the compile the other one the bean refresh
      * the refresh can only happen outside of a compile cycle
      * and also a global refresh has to be atomic and no other
      * refreshes should happen
      */
     public final static Boolean BEAN_SYNC_MONITOR = new Boolean(true);
 
     /**
      * second synchronisation monitor
      * all other artifacts can only be refreshed outside of a
      * compile cycle otherwise the classloader would get
      * half finished compile states to load
      */
     public final static Boolean COMPILE_SYNC_MONITOR = new Boolean(true);
 
     private volatile AtomicInteger _currentlyRunningRequests = null;
 
     private MasterDependencyRegistry _dependencyRegistry = new MasterDependencyRegistry();
 
     public long getPersonalScopedBeanRefresh() {
         return personalScopedBeanRefresh;
     }
 
     public void setPersonalScopedBeanRefresh(long personalScopedBeanRefresh) {
         this.personalScopedBeanRefresh = personalScopedBeanRefresh;
     }
 
     /**
      * checks whether it would make sense at the current point
      * in time to enforce a recompile or not
      *
      * @param scriptingEngine
      * @return
      */
     public boolean isRecompileRecommended(int scriptingEngine) {
         Boolean recommended = daemon.getSystemRecompileMap().get(scriptingEngine);
         return recommended == null || recommended.booleanValue();
     }
 
     public void setRecompileRecommended(int scriptingEngine, boolean recompileRecommended) {
         daemon.getSystemRecompileMap().put(scriptingEngine, recompileRecommended);
     }
 
     public DependencyRegistry getDependencyRegistry(int scriptingEngine) {
         return _dependencyRegistry.getSubregistry(scriptingEngine);
     }
 
    public void setDependencyRegistry(int scriptingEngine, DependencyRegistry registry) {
         _dependencyRegistry.addSubregistry(scriptingEngine, registry);
     }
 
     public boolean isDependencyScanned(int scriptingEngine) {
         FacesContext ctx = FacesContext.getCurrentInstance();
         if (ctx == null) {
             return false;
         }
         Map<String, Object> requestMap = (Map<String, Object>) ctx.getExternalContext().getRequestMap();
         Boolean retVal = (Boolean) requestMap.get("isDependencyScanned_" + scriptingEngine);
         return (retVal == null) ? false : retVal;
     }
 
     public void setDependencyScanned(int scriptingEngine, Boolean val) {
         FacesContext ctx = FacesContext.getCurrentInstance();
         if (ctx == null) {
             return;
         }
         Map<String, Object> requestMap = (Map<String, Object>) ctx.getExternalContext().getRequestMap();
         requestMap.put("isDependencyScanned_" + scriptingEngine, val);
     }
 
     public FileChangedDaemon getDaemon() {
         return daemon;
     }
 
     public void setDaemon(FileChangedDaemon daemon) {
         this.daemon = daemon;
     }
 
     /**
      * @return true if a compile currently is in progress
      */
     public static boolean isCompileInProgress(int engineType) {
         //TODO implement this
         return false;
     }
 
     /**
      * returns whether a recompile now at the current point
      * in time for this engine is allowed or not
      * This state depends on the state of the application
      * if non locked compiles is enabled it always will return true
      * <p/>
      * if a synchronized locking compile is enabled
      * it will return true if the calling request is the only
      * one currently issued because no request is allowed to compile
      * until others have run out
      *
      * @param engineType
      * @return
      */
     public boolean isComileAllowed(int engineType) {
 
         return getCurrentlyRunningRequests().equals(1);
     }
 
     /**
      * getter for our request counter
      * we need this variable to keep a lock
      * on the number of requests
      * we only can compile if the currently
      * running request is the only one currently
      * active, to keep the compilation results in sync
      *
      * @return the request counter holder which is an atomic integer
      *         <p/>
      *         probably deprecred
      */
     public AtomicInteger getCurrentlyRunningRequests() {
         return _currentlyRunningRequests;
     }
 
     public void setCurrentlyRunningRequests(AtomicInteger currentlyRunning) {
         _currentlyRunningRequests = currentlyRunning;
     }
 
     /**
      * checks outside of the request scope for changes and taints the corresponding engine
      */
     public static void scanAndMarkChange() {
         WeavingContext.getWeaver();
     }
 
     public MasterDependencyRegistry getDependencyRegistry() {
         return _dependencyRegistry;
     }
 
     public void setDependencyRegistry(MasterDependencyRegistry dependencyRegistry) {
         _dependencyRegistry = dependencyRegistry;
     }
 }
