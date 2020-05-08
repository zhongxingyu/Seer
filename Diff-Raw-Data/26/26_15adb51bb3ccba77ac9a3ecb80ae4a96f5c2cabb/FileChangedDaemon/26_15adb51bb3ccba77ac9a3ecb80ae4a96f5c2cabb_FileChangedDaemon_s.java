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
 
 import org.apache.myfaces.scripting.api.ScriptingConst;
 import org.apache.myfaces.scripting.api.ScriptingWeaver;
 import org.apache.myfaces.scripting.core.dependencyScan.core.ClassDependencies;
 import org.apache.myfaces.scripting.core.util.WeavingContext;
 
 import javax.servlet.ServletContext;
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author werpu
  *         Reimplementation of the file changed daemon thread
  *         in java. The original one was done in groovy
  *         this threads purpose is to watch the files
  *         loaded by the various engine loaders for
  *         for file changes and then if one has changed we have to mark
  *         it for further processing
  *         <p/>
  */
 public class FileChangedDaemon extends Thread {
 
     static FileChangedDaemon instance = null;
 
     Map<String, ReloadingMetadata> classMap = new ConcurrentHashMap<String, ReloadingMetadata>(8, 0.75f, 1);
     ClassDependencies dependencyMap = new ClassDependencies();
 
 
     /**
      * this map is a shortcut for the various scripting engines
      * it keeps track whether the engines source paths
      * have dirty files or not and if true we enforce a recompile at the
      * next refresh!
      * <p/>
      * We keep track on engine level to avoid to search the classMap for every refresh
      * the classMap still is needed for various identification tasks which are reload
      * related
      */
     Map<Integer, Boolean> systemRecompileMap = new ConcurrentHashMap<Integer, Boolean>(8, 0.75f, 1);
 
     boolean running = false;
     boolean contextInitialized = false;
     Logger log = Logger.getLogger(FileChangedDaemon.class.getName());
     ScriptingWeaver _weavers = null;
     WeakReference externalContext;
 
     public void initWeavingContext(Object externalContext) {
         if (this.externalContext != null) return;
         this.externalContext = new WeakReference(externalContext);
     }
 
     public static synchronized FileChangedDaemon getInstance() {
         if (instance == null) {
             instance = new FileChangedDaemon();
             instance.setDaemon(true);
             instance.setRunning(true);
             instance.start();
 
         }
 
         return instance;
     }
 
     public void run() {
         while (running) {
             if (externalContext != null && !contextInitialized) {
                 WeavingContext.initThread((ServletContext) externalContext.get());
                 contextInitialized = true;
             }
             try {
                 try {
                     Thread.sleep(ScriptingConst.TAINT_INTERVAL);
                 } catch (InterruptedException e) {
                     //if the server shuts down while we are in sleep we get an error
                     //which we better should swallow
                 }
 
                 if (classMap == null || classMap.size() == 0)
                     continue;
                 if (contextInitialized)
                     checkForChanges();
             } catch (Throwable e) {
                 log.log(Level.SEVERE, "[EXT-SCRIPTING]", e);
 
             }
         }
         if (log.isLoggable(Level.INFO)) {
             log.info("[EXT-SCRIPTING] Dynamic reloading watch daemon is shutting down");
         }
     }
 
     /**
      * central tainted mark method which keeps
      * track if some file in one of the supported engines has changed
      * and if yes marks the file as tainted as well
      * as marks the engine as having to do a full recompile
      */
     private final void checkForChanges() {
 
         WeavingContext.getWeaver().scanForAddedClasses();
 
         //TODO move this code also into the weaver so that
         //we have it centralized
         for (Map.Entry<String, ReloadingMetadata> it : this.classMap.entrySet()) {
             //if (!it.getValue().isTainted()) {
 
             File proxyFile = new File(it.getValue().getSourcePath() + File.separator + it.getValue().getFileName());
             if (/*!it.getValue().isTainted() &&*/ isModified(it, proxyFile)) {
 
                 systemRecompileMap.put(it.getValue().getScriptingEngine(), Boolean.TRUE);
                 ReloadingMetadata meta = it.getValue();
                 meta.setTainted(true);
                 meta.setTaintedOnce(true);
                 printInfo(it, proxyFile);
                 meta.setTimestamp(proxyFile.lastModified());
                 dependencyTainted(meta.getAClass().getName());
                 
                 //we add our log entry for further reference
                 WeavingContext.getRefreshContext().addTaintLogEntry(meta);
             }
             //}
         }
         //we clean up the taint log
         WeavingContext.getRefreshContext().gcTaintLog();
     }
 
     /**
      * recursive walk over our meta data to taint also the classes
      * which refer to our refreshing class so that those
      * are reloaded as well, this helps to avoid classcast
      * exceptions caused by imports and casts on long running artefacts
      *
      * @param className the origin classname which needs to be walked recursively
      */
     private void dependencyTainted(String className) {
         Set<String> referrers = dependencyMap.getReferringClasses(className);
         if (referrers == null) return;
         for (String referrer : referrers) {
             ReloadingMetadata metaData = classMap.get(referrer);
             if (metaData == null) continue;
             if (metaData.isTainted()) continue;
             printInfo(metaData);
 
             metaData.setTainted(true);
             metaData.setTaintedOnce(true);
             dependencyTainted(metaData.getAClass().getName());
             WeavingContext.getRefreshContext().addTaintLogEntry(metaData);
         }
     }
 
     private final boolean isModified(Map.Entry<String, ReloadingMetadata> it, File proxyFile) {
         return proxyFile.lastModified() != it.getValue().getTimestamp();
     }
 
     private void printInfo(ReloadingMetadata it) {
         if (log.isLoggable(Level.INFO)) {
             log.log(Level.INFO, "[EXT-SCRIPTING] Tainting Dependency: {0}", it.getFileName());
         }
     }
 
     private void printInfo(Map.Entry<String, ReloadingMetadata> it, File proxyFile) {
         if (log.isLoggable(Level.INFO)) {
             log.log(Level.INFO, "[EXT-SCRIPTING] comparing {0} Dates: {1} {2} ", new String[]{it.getKey(), Long.toString(proxyFile.lastModified()), Long.toString(it.getValue().getTimestamp())});
             log.log(Level.INFO, "[EXT-SCRIPTING] Tainting: {0}", it.getValue().getFileName());
         }
     }
 
     public boolean isRunning() {
         return running;
     }
 
     public void setRunning(boolean running) {
         this.running = running;
     }
 
     public Map<Integer, Boolean> getSystemRecompileMap() {
         return systemRecompileMap;
     }
 
     public void setSystemRecompileMap(Map<Integer, Boolean> systemRecompileMap) {
         this.systemRecompileMap = systemRecompileMap;
     }
 
     public Map<String, ReloadingMetadata> getClassMap() {
         return classMap;
     }
 
     public void setClassMap(Map<String, ReloadingMetadata> classMap) {
         this.classMap = classMap;
     }
 
     public ScriptingWeaver getWeavers() {
         return _weavers;
     }
 
     public void setWeavers(ScriptingWeaver weavers) {
         _weavers = weavers;
     }
 
     public ClassDependencies getDependencyMap() {
         return dependencyMap;
     }
 }
 
