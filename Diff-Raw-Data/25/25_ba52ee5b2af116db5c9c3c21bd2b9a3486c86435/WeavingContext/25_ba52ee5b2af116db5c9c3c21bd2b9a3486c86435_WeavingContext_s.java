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
 
 package rewrite.org.apache.myfaces.extensions.scripting.core.context;
 
 import org.apache.myfaces.extensions.scripting.core.MethodLevelReloadingHandler;
 import org.apache.myfaces.extensions.scripting.core.dependencyScan.core.ClassDependencies;
 import rewrite.org.apache.myfaces.extensions.scripting.core.loader.ThrowAwayClassloader;
 
 import rewrite.org.apache.myfaces.extensions.scripting.core.common.Decorated;
 import rewrite.org.apache.myfaces.extensions.scripting.core.engine.FactoryEngines;
 import rewrite.org.apache.myfaces.extensions.scripting.core.engine.api.ScriptingEngine;
 import rewrite.org.apache.myfaces.extensions.scripting.core.monitor.ClassResource;
 import rewrite.org.apache.myfaces.extensions.scripting.core.monitor.WatchedResource;
 import rewrite.org.apache.myfaces.extensions.scripting.core.reloading.GlobalReloadingStrategy;
 import rewrite.org.apache.myfaces.extensions.scripting.jsf.adapters.ImplementationSPI;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Proxy;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
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
     /**
      * lock var which can be used for recompilation
      */
     public AtomicBoolean recompileLock = new AtomicBoolean(false);
     protected Configuration configuration = new Configuration();
     
     //ClassDependencies _dependencyMap = new ClassDependencies();
     
     ImplementationSPI _implementation = null;
     GlobalReloadingStrategy _reloadingStrategy = new GlobalReloadingStrategy();
 
     Logger log = Logger.getLogger(this.getClass().getName());
     boolean _scriptingEnabled = true;
 
     public void initEngines() throws IOException
     {
         FactoryEngines.getInstance().init();
     }
 
     public Collection<ScriptingEngine> getEngines()
     {
         return FactoryEngines.getInstance().getEngines();
     }
 
     public ScriptingEngine getEngine(int engineType)
     {
         return FactoryEngines.getInstance().getEngine(engineType);
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
 
     public void initialFullScan()
     {
         for (ScriptingEngine engine : getEngines())
         {
             //log.info("[EXT-SCRIPTING] scanning " + engine.getEngineType() + " files");
             engine.scanForAddedDeleted();
             //log.info("[EXT-SCRIPTING] scanning " + engine.getEngineType() + " files done");
         }
     }
 
     public boolean compile()
     {
         boolean compile = false;
         for (ScriptingEngine engine : getEngines())
         {
             if (!engine.needsRecompile()) continue;
             compile = true;
             log.info("[EXT-SCRIPTING] compiling " + engine.getEngineTypeAsStr() + " files");
             engine.compile();
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
         return clazz.getClassLoader() instanceof ThrowAwayClassloader;
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
     public static Object createMethodReloadingProxyFromObject(Object o, Class theInterface, int artifactType)
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
             resource.setAClass(clazz);
         }
        return null;
     }
 
     public Object reload(Object instance, int strategyType)
     {
         return _reloadingStrategy.reload(instance, strategyType);
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
 
     public void addDependency(int engineType, String fromClass, String toClass) {
         //TODO implement this tomorrow
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
 }
