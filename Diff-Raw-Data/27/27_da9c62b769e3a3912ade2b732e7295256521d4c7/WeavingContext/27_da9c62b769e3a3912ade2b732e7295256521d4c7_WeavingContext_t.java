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
 
 package org.apache.myfaces.extensions.scripting.core.api;
 
 import org.apache.myfaces.extensions.scripting.core.common.util.ClassUtils;
 import org.apache.myfaces.extensions.scripting.core.common.util.ReflectUtil;
 import org.apache.myfaces.extensions.scripting.core.engine.FactoryEngines;
 import org.apache.myfaces.extensions.scripting.core.engine.ThrowAwayClassloader;
 import org.apache.myfaces.extensions.scripting.core.engine.api.ClassScanner;
 import org.apache.myfaces.extensions.scripting.core.engine.api.CompilationResult;
 import org.apache.myfaces.extensions.scripting.core.engine.api.ScriptingEngine;
 import org.apache.myfaces.extensions.scripting.core.monitor.ClassResource;
 import org.apache.myfaces.extensions.scripting.core.monitor.WatchedResource;
 import org.apache.myfaces.extensions.scripting.core.reloading.GlobalReloadingStrategy;
 import org.apache.myfaces.extensions.scripting.core.reloading.MethodLevelReloadingHandler;
 import org.apache.myfaces.extensions.scripting.jsf.adapters.MyFacesSPI;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Proxy;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Logger;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  *          <p/>
  *          Central weaving context
  */
 
 public class WeavingContext
 {
     static final Logger log = Logger.getLogger(WeavingContext.class.getName());
 
     /**
      * lock var which can be used for recompilation
      */
     public AtomicBoolean recompileLock = new AtomicBoolean(false);
     /**
      * configuration which stores all external configuration entries
      */
     protected Configuration configuration = new Configuration();
 
     //ClassDependencies _dependencyMap = new ClassDependencies();
     /**
      * Service provider for the implementation under which this extension
      * runs
      */
     ImplementationSPI _implementation = null;
     /**
      * the collection of reloading strategies depending on their artifact type
      */
     GlobalReloadingStrategy _reloadingStrategy = new GlobalReloadingStrategy();
     /**
      * the annotation scanning reference
      */
     ClassScanner _annotationScanner = null;
 
     /**
      * true only if the startup has performed without errors
      */
     boolean _scriptingEnabled = true;
 
     /**
      * holder for various operations within our lifecycle
      */
     ConcurrentHashMap<String, Long> lifecycleRegistry = new ConcurrentHashMap<String, Long>();
 
     /**
      * This is a log which keeps track of the taints
      * over time, we need that mostly for bean refreshes
      * in multiuser surroundings, because only tainted beans need
      * to be refreshed.
      * Now if a user misses multiple updates he has to get a full
      * set of changed classes to be able to drop all personal scoped beans tainted
      * since the he refreshed last! Hence we have to move away from our
      * two dimensional &lt;class, taint&gt; to a three dimensional &lt;class, taint, time&gt;
      * view of things
      */
     private List<TaintingHistoryEntry> _taintLog = Collections.synchronizedList(new LinkedList<TaintingHistoryEntry>());
 
     /**
      * compilation results holder for the compiler listeners (components etc...)
      */
     private static final Map<Integer, CompilationResult> _compilationResults = new ConcurrentHashMap<Integer, CompilationResult>();
 
     /**
      * we keep a 10 minutes timeout period to keep the performance in place
      */
     private final long TAINT_HISTORY_TIMEOUT = 10 * 60 * 1000;
 
     /**
      * internal class used by our own history log
      */
     static class TaintingHistoryEntry
     {
         long _timestamp;
         ClassResource _data;
 
         public TaintingHistoryEntry(ClassResource data)
         {
             _data = data;
             _timestamp = System.currentTimeMillis();
         }
 
         public long getTimestamp()
         {
             return _timestamp;
         }
 
         public ClassResource getData()
         {
             return _data;
         }
     }
 
     public void initEngines() throws IOException
     {
         FactoryEngines.getInstance().init();
         initScanner();
     }
 
     public void initScanner()
     {
         try
         {
             Class scanner = ClassUtils.getContextClassLoader().loadClass("org.apache.myfaces.extensions.scripting.jsf.annotation.GenericAnnotationScanner");
             this._annotationScanner = (ClassScanner) ReflectUtil.instantiate(scanner);
 
         }
         catch (ClassNotFoundException e)
         {
             //we do nothing here
             //generic annotation scanner can be missing in jsf 1.2 environments
             //_logger.log(Level.FINER, "", e);
         }
     }
 
     public Collection<ScriptingEngine> getEngines()
     {
         return FactoryEngines.getInstance().getEngines();
     }
 
     public ScriptingEngine getEngine(int engineType)
     {
         return FactoryEngines.getInstance().getEngine(engineType);
     }
 
     public ScriptingEngine getAssociatedEngine(Object artifact) {
         for(ScriptingEngine engine : getEngines()) {
             if(engine.isArtifactOfEngine(artifact)) return engine;
         }
         return null;
     }
     
     
     /**
      * returns the mitable watche resource maps for the various engines
      *
      * @return
      */
     public Map<Integer, Map<String, ClassResource>> getWatchedResources()
     {
         Map<Integer, Map<String, ClassResource>> ret = new HashMap<Integer, Map<String, ClassResource>>();
         for (ScriptingEngine engine : getEngines())
         {
             ret.put(engine.getEngineType(), engine.getWatchedResources());
         }
         return ret;
     }
 
     /**
      * @return a map of all watched resources over all engines
      */
     public Map<String, ClassResource> getAllWatchedResources()
     {
         Map<String, ClassResource> ret = new HashMap<String, ClassResource>();
         for (ScriptingEngine engine : getEngines())
         {
             Map<String, ClassResource> watchedResourceMap = engine.getWatchedResources();
             for (Map.Entry<String, ClassResource> entry : watchedResourceMap.entrySet())
             {
                 ret.put(entry.getKey(), entry.getValue());
             }
         }
         return ret;
     }
 
     /**
      * @param key the watched resource classname
      * @return the watched resource from the given key or null
      */
     public ClassResource getWatchedResource(String key)
     {
         for (ScriptingEngine engine : getEngines())
         {
             if (!engine.getWatchedResources().containsKey(key)) continue;
             return engine.getWatchedResources().get(key);
         }
         return null;
     }
 
     public Collection<ClassResource> getTaintedClasses(int scriptingEngine) {
             Map<String, ClassResource> watchedResources = getEngine(scriptingEngine).getWatchedResources();
             List<ClassResource> res = new LinkedList<ClassResource>();
             for(Map.Entry<String, ClassResource> entry: watchedResources.entrySet()) {
                 if(entry.getValue().isTainted()) {
                     res.add(entry.getValue());
                 }
             }
             return res;
         }
 
 
     public Collection<ClassResource> getTaintedClasses() {
         Map<String, ClassResource> watchedResources = getAllWatchedResources();
         List<ClassResource> res = new LinkedList<ClassResource>();
         for(Map.Entry<String, ClassResource> entry: watchedResources.entrySet()) {
             if(entry.getValue().isTainted()) {
                 res.add(entry.getValue());
             }
         }
         return res;
     }
     
     /**
      * checks if a resource idenified by key is tainted
      *
      * @param key the identifier for the resource
      * @return true in case of being tainted
      */
     public boolean isTainted(String key)
     {
         ClassResource res = getWatchedResource(key);
         if (res == null) return false;
         return res.isTainted();
     }
 
     public Set<String> loadPossibleDynamicClasses()
     {
         return getAllWatchedResources().keySet();
     }
 
     public Configuration getConfiguration()
     {
         return configuration;
     }
 
     public void setConfiguration(Configuration configuration)
     {
         this.configuration = configuration;
     }
 
     public boolean needsRecompile()
     {
         for (ScriptingEngine engine : getEngines())
         {
             //log.info("[EXT-SCRIPTING] scanning " + engine.getEngineType() + " files");
             if (engine.needsRecompile()) return true;
             //log.info("[EXT-SCRIPTING] scanning " + engine.getEngineType() + " files done");
         }
         return false;
     }
 
    public void fullScan()
     {
         for (ScriptingEngine engine : getEngines())
         {
             engine.scanForAddedDeleted();
         }
         //the scanner scans only the tainted classes
         //hence this should work whatever happens
 
     }
 
     public void annotationScan()
     {
         if (_annotationScanner != null)
             _annotationScanner.scanPaths();
     }
 
     public boolean compile()
     {
         boolean compile = false;
         for (ScriptingEngine engine : getEngines())
         {
             if (!engine.needsRecompile()) continue;
             compile = true;
             log.info("[EXT-SCRIPTING] compiling " + engine.getEngineTypeAsStr() + " files");
             CompilationResult result = engine.compile();
             if(result != null) {
                 WeavingContext.getInstance().setCompilationResult(engine.getEngineType(), result);
             }
             
             log.info("[EXT-SCRIPTING] compiling " + engine.getEngineTypeAsStr() + " files done");
         }
         return compile;
     }
 
     public void scanDependencies()
     {
         for (ScriptingEngine engine : getEngines())
         {
             if (engine.isTainted())
             {
                 log.info("[EXT-SCRIPTING] scanning " + engine.getEngineTypeAsStr() + " dependencies");
                 engine.scanDependencies();
                 log.info("[EXT-SCRIPTING] scanning " + engine.getEngineTypeAsStr() + " dependencies end");
             }
         }
     }
 
     public void markTaintedDependends()
     {
         for (ScriptingEngine engine : getEngines())
         {
             engine.markTaintedDependencies();
         }
     }
 
     public WatchedResource getResource(String className)
     {
         WatchedResource ret = null;
         for (ScriptingEngine engine : getEngines())
         {
             ret = engine.getWatchedResources().get(className);
             if (ret != null) return ret;
         }
         return ret;
     }
 
     public boolean isDynamic(Class clazz)
     {
         return (clazz.getClassLoader() instanceof ThrowAwayClassloader)/*<> || (clazz.getClassLoader() instanceof
             ScannerClassloader))*/;
     }
 
     /**
      * we create a proxy to an existing object
      * which does reloading of the internal class
      * on method level
      * <p/>
      * this works only on classes which implement contractual interfaces
      * it cannot work on things like the navigation handler
      * which rely on base classes
      *
      * @param o            the source object to be proxied
      * @param theInterface the proxying interface
      * @param artifactType the artifact type to be reloaded
      * @return a proxied reloading object of type theInterface
      */
     public Object createMethodReloadingProxyFromObject(Object o, Class theInterface, int artifactType)
     {
         //if (!isScriptingEnabled()) {
         //    return o;
         //}
         return Proxy.newProxyInstance(o.getClass().getClassLoader(),
                 new Class[]{theInterface},
                 new MethodLevelReloadingHandler(o, artifactType));
     }
 
     /**
      * reload the class dynamically
      */
     public Class reload(Class clazz)
     {
         if (!isDynamic(clazz)) return clazz;
         ClassResource resource = (ClassResource) getResource(clazz.getName());
         if (resource == null) return clazz;
         if (resource.isTainted() || resource.getAClass() == null)
         {
             clazz = _implementation.forName(clazz.getName());
             //TODO not needed anymore, done by the forName now
             resource.setAClass(clazz);
         }
         return clazz;
     }
 
     public Object reload(Object instance,  int strategyType)
     {
         int engineType = getAssociatedEngine(instance).getEngineType();
 
         return _reloadingStrategy.reload(instance, engineType, strategyType);
     }
 
     /**
      * we create a proxy to an existing object
      * which does reloading of the internal class
      * on newInstance level
      *
      * @param o            the original object
      * @param theInterface the proxy interface
      * @param artifactType the artifact type to be handled
      * @return the proxy of the object if scripting is enabled, the original one otherwise
      */
     @SuppressWarnings("unused")
     public static Object createConstructorReloadingProxyFromObject(Object o, Class theInterface, int artifactType)
     {
         //if (!isScriptingEnabled()) {
         //    return o;
         //}
         return Proxy.newProxyInstance(o.getClass().getClassLoader(),
                 new Class[]{theInterface},
                 new MethodLevelReloadingHandler(o, artifactType));
     }
 
     /**
      * un-mapping of a proxied object
      *
      * @param o the proxied object
      * @return the un-proxied object
      */
     public static Object getDelegateFromProxy(Object o)
     {
         if (o == null)
         {
             return null;
         }
         if (o instanceof Decorated)
             return ((Decorated) o).getDelegate();
 
         if (!Proxy.isProxyClass(o.getClass())) return o;
         InvocationHandler handler = Proxy.getInvocationHandler(o);
         if (handler instanceof Decorated)
         {
             return ((Decorated) handler).getDelegate();
         }
         return o;
     }
 
     public void addDependency(int engineType, String fromClass, String toClass)
     {
         //TODO implement this tomorrow
     }
 
     public ImplementationSPI getImplementationSPI() {
         return MyFacesSPI.getInstance();
     }
 
     //----------------------------------------------------------------------
     //lifecycle related tasks
     public boolean isPostInit()
     {
         return (lifecycleRegistry.get("LIFECYCLE_POST_INIT") != null);
     }
 
     public void markPostInit()
     {
         lifecycleRegistry.put("LIFECYCLE_POST_INIT", System.currentTimeMillis());
     }
 
     public void markLastTaint()
     {
         lifecycleRegistry.put("LIFECYCLE_LAST_TAINTED", System.currentTimeMillis());
     }
 
     /**
      * @return the time value of the last taint happening
      */
     public long getLastTaint()
     {
         Long lastTainted = lifecycleRegistry.get("LIFECYCLE_LAST_TAINTED");
         lastTainted = (lastTainted != null) ? lastTainted : -1L;
         return lastTainted;
     }
 
     /**
      * marks the last annotation scan
      */
     public void markLastAnnotationScan()
     {
         lifecycleRegistry.put("LIFECYCLE_LAST_ANN_SCAN", System.currentTimeMillis());
     }
 
     /**
      * @return a the time value of the last annotation scan
      */
     public long getLastAnnotationScan()
     {
         Long lastTainted = lifecycleRegistry.get("LIFECYCLE_LAST_ANN_SCAN");
         lastTainted = (lastTainted != null) ? lastTainted : -1L;
         return lastTainted;
     }
 
     //------------------------------ tainting history entries -----------------------
 
     /**
      * adds a new entry into our taint log
      * which allows us to access tainting data
      * from a given point in time
      *
      * @param data the tainting data to be added
      */
     public void addTaintLogEntry(ClassResource data)
     {
         _taintLog.add(new TaintingHistoryEntry(data));
     }
 
     /**
      * garbage collects our tainting data
      * and removes all entries which are not
      * present anymore due to timeout
      * this gc code is called asynchronously
      * from our tainting thread to keep the
      * performance intact
      */
     public void gcTaintLog()
     {
         long timeoutTimestamp = System.currentTimeMillis() - TAINT_HISTORY_TIMEOUT;
         Iterator<TaintingHistoryEntry> it = _taintLog.iterator();
 
         while (it.hasNext())
         {
             TaintingHistoryEntry entry = it.next();
             if (entry.getTimestamp() < timeoutTimestamp)
             {
                 it.remove();
             }
         }
     }
 
     /**
      * returns the last noOfEntries entries in the taint history
      *
      * @param noOfEntries the number of entries to be delivered
      * @return a collection of the last &lt;noOfEntries&gt; entries
      */
     public Collection<ClassResource> getLastTainted(int noOfEntries)
     {
         Iterator<TaintingHistoryEntry> it = _taintLog.subList(Math.max(_taintLog.size() - noOfEntries, 0), _taintLog.size()).iterator();
         List<ClassResource> retVal = new LinkedList<ClassResource>();
         while (it.hasNext())
         {
             TaintingHistoryEntry entry = it.next();
             retVal.add(entry.getData());
         }
         return retVal;
     }
 
     /**
      * Returns a set of tainting data from a given point in time up until now
      *
      * @param timestamp the point in time from which the tainting data has to be derived from
      * @return a set of entries which are a union of all points in time beginning from timestamp
      */
     public Collection<ClassResource> getTaintHistory(long timestamp)
     {
         List<ClassResource> retVal = new LinkedList<ClassResource>();
         Iterator<TaintingHistoryEntry> it = _taintLog.iterator();
 
         while (it.hasNext())
         {
             TaintingHistoryEntry entry = it.next();
             if (entry.getTimestamp() >= timestamp)
             {
                 retVal.add(entry.getData());
             }
         }
         return retVal;
     }
 
     /**
      * Returns a set of tainted classes from a given point in time up until now
      *
      * @param timestamp the point in time from which the tainting data has to be derived from
      * @return a set of classnames which are a union of all points in time beginning from timestamp
      */
     public Set<String> getTaintHistoryClasses(long timestamp)
     {
         Set<String> retVal = new HashSet<String>();
         Iterator<TaintingHistoryEntry> it = _taintLog.iterator();
 
         while (it.hasNext())
         {
             TaintingHistoryEntry entry = it.next();
             if (entry.getTimestamp() >= timestamp)
             {
                 if (entry.getData() instanceof ClassResource)
                 {
                     retVal.add(((ClassResource) entry.getData()).getAClass().getName());
                 } else
                 {
                     retVal.add(entry.getData().getFile().getAbsolutePath());
                 }
             }
         }
         return retVal;
     }
 
     //----------------------------------------------------------------------
     /*public ClassDependencies getDependencyMap()
     {
         return _dependencyMap;
     }
 
     public void setDependencyMap(ClassDependencies dependencyMap)
     {
         _dependencyMap = dependencyMap;
     } */
 
     protected static WeavingContext _instance = new WeavingContext();
 
     public static WeavingContext getInstance()
     {
         return _instance;
     }
 
     public ImplementationSPI getImplementation()
     {
         return _implementation;
     }
 
     public void setImplementation(ImplementationSPI implementation)
     {
         _implementation = implementation;
     }
 
     public boolean isScriptingEnabled()
     {
         return _scriptingEnabled;
     }
 
     public void setScriptingEnabled(boolean scriptingEnabled)
     {
         _scriptingEnabled = scriptingEnabled;
     }
 
     public CompilationResult getCompilationResult(Integer scriptingEngine)
     {
         return _compilationResults.get(scriptingEngine);
     }
 
     public void setCompilationResult(Integer scriptingEngine, CompilationResult result)
     {
         _compilationResults.put(scriptingEngine, result);
     }
 
 }
