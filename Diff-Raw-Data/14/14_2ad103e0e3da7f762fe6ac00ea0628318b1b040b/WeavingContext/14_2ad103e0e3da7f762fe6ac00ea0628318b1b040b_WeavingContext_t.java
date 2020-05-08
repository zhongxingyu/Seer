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
 package org.apache.myfaces.scripting.core.util;
 
 import org.apache.myfaces.scripting.api.*;
 import org.apache.myfaces.scripting.core.DummyWeaver;
 import org.apache.myfaces.scripting.core.MethodLevelReloadingHandler;
 import org.apache.myfaces.scripting.refresh.FileChangedDaemon;
 import org.apache.myfaces.scripting.refresh.RefreshContext;
 import org.apache.myfaces.scripting.api.CompilationResult;
 
 import javax.servlet.ServletContext;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Proxy;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Logger;
 
 /**
  * A set of weaving context class called
  * by the various subsystems
  * <p/>
  * TODO move this away from static methods into a singleton which is kept
  * in the application context, to keep the context pattern.
  *
  * @author Werner Punz
  */
 public class WeavingContext {
 
     /**
      * data we can pass for other threads
      */
     static class ThreadLocalData {
         public Object _weaverHolder;
         public Object _refreshContextHolder;
         public Object _configurtion;
         public Object _externalContext;
     }
 
     static ThreadLocalData _referenceThreadHolder = null;
 
     /**
      * <p>
      * we push our _weaver into the thread local
      * to avoid too many calls into the
      * context class loading hierarchy
      * this should speed things up a little bit.
      * </p>
      * <p>
      * Note we could work with this with static
      * objects as well but since we also have to work with context
      * reloading we probably are safer with filters
      * a reference in the context and a threadLocal variable
      * </p>
      */
     static protected ThreadLocal _weaverHolder = new ThreadLocal();
     static protected ThreadLocal _refreshContextHolder = new ThreadLocal();
     static protected ThreadLocal _configuration = new ThreadLocal();
     static protected ThreadLocal _externalContext = new ThreadLocal();
 
     private static final String WARN_WEAVER_NOT_SET = "Scripting Weaver is not set. Disabling script reloading subsystem. Make sure you have the scripting servlet filter enabled in your web.xml";
 
     private static final Map<Integer, CompilationResult> _compilationResults = new ConcurrentHashMap<Integer, CompilationResult>();
 
     public static void init() {
 
     }
 
     public static void initThread(ServletContext context) {
         WeavingContext.setWeaver(context.getAttribute("ScriptingWeaver"));
         WeavingContext.setRefreshContext((RefreshContext) context.getAttribute("RefreshContext"));
         WeavingContext.setConfiguration((Configuration) context.getAttribute(ScriptingConst.CTX_CONFIGURATION));
         //WeavingContext.getRefreshContext().setCurrentlyRunningRequests(getRequestCnt());
         WeavingContext.setExternalContext(context);
     }
 
     public static CompilationResult getCompilationResult(Integer scriptingEngine) {
         return _compilationResults.get(scriptingEngine);
     }
 
     public static void setCompilationResult(Integer scriptingEngine, CompilationResult result) {
         _compilationResults.put(scriptingEngine, result);
     }
 
     public static void setExternalContext(Object context) {
         _externalContext.set(context);
     }
 
     public static Object getExternalContext() {
         return _externalContext.get();
     }
 
     /**
      * general shutdown clean
      */
     public static void clean() {
         _weaverHolder.set(null);
     }
 
     public static void setRefreshContext(RefreshContext rContext) {
         _refreshContextHolder.set(rContext);
     }
 
     public static RefreshContext getRefreshContext() {
         return (RefreshContext) _refreshContextHolder.get();
     }
 
     public static Configuration getConfiguration() {
         return (Configuration) _configuration.get();
     }
 
     public static void setConfiguration(Configuration configuration) {
         _configuration.set(configuration);
     }
 
     /**
      * the weavers are set from outside
      * we have to provide the weaver facade
      * for very thread accessing this system
      *
      * @param weaver
      */
     public static void setWeaver(Object weaver) {
         _weaverHolder.set(weaver);
 
     }
 
     /**
      * some artefacts need a full request refresh
      */
     public static void doRequestRefreshes() {
         getWeaver().requestRefresh();
     }
 
     /**
      * checks whether the system
      * has scripting enabled or not
      *
      * @return true in case of being scriptable
      */
     public static boolean isScriptingEnabled() {
         return _weaverHolder.get() != null;
     }
 
     /**
      * fetches the weavers
      * for this current thread
      *
      * @return a ScriptingWeaver chain for all weavers currently supported
      */
     public static ScriptingWeaver getWeaver() {
        //shutting down condition _weaverHolder == null due to separate thread
        if(_weaverHolder == null) {
            return null;
        }
         ScriptingWeaver weaver = (ScriptingWeaver) _weaverHolder.get();
         if (weaver == null) {
             Logger log = Logger.getLogger(WeavingContext.class.getName());
             log.warning(WARN_WEAVER_NOT_SET);
             _weaverHolder.set(new DummyWeaver());
         }
         return (ScriptingWeaver) _weaverHolder.get();
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
      * @return a proxied reloading object of type theInterface
      */
     public static Object createMethodReloadingProxyFromObject(Object o, Class theInterface, int artefactType) {
         return Proxy.newProxyInstance(o.getClass().getClassLoader(),
                 new Class[]{theInterface},
                 new MethodLevelReloadingHandler(o, artefactType));
     }
 
     /**
      * we create a proxy to an existing object
      * which does reloading of the internal class
      * on newInstance level
      *
      * @param o
      * @param theInterface
      * @return
      */
     public static Object createConstructorReloadingProxyFromObject(Object o, Class theInterface, int artefactType) {
         return Proxy.newProxyInstance(o.getClass().getClassLoader(),
                 new Class[]{theInterface},
                 new MethodLevelReloadingHandler(o, artefactType));
     }
 
     public static FileChangedDaemon getFileChangedDaemon() {
         FileChangedDaemon daemon = getRefreshContext().getDaemon();
         if (daemon.getWeavers() == null) {
             daemon.setWeavers((ScriptingWeaver) getWeaver());
         }
         return daemon;
     }
 
     /**
      * unmapping of a proxied object
      *
      * @param o the proxied object
      * @return the unproxied object
      */
     public static Object getDelegateFromProxy(Object o) {
         if (o == null) {
             return null;
         }
         if (o instanceof Decorated)
             return ((Decorated) o).getDelegate();
 
         if (!Proxy.isProxyClass(o.getClass())) return o;
         InvocationHandler handler = Proxy.getInvocationHandler(o);
         if (handler instanceof Decorated) {
             return ((Decorated) handler).getDelegate();
         }
         return o;
     }
 
     public static boolean isDynamic(Class clazz) {
         return getWeaver().isDynamic(clazz);
     }
 
     /**
      * we push the threading data into
      * a shared queue
      */
     public static void pushThreadingData() {
         ThreadLocalData data = new ThreadLocalData();
         data._configurtion = getConfiguration();
         data._refreshContextHolder = getRefreshContext();
         data._weaverHolder = getWeaver();
         data._externalContext = getExternalContext();
         _referenceThreadHolder = data;
     }
 
     public static void popThreadingData() {
         ThreadLocalData data = _referenceThreadHolder;
         setConfiguration((Configuration) data._configurtion);
         setRefreshContext((RefreshContext) data._refreshContextHolder);
         setWeaver((ScriptingWeaver) data._weaverHolder);
         setExternalContext(data._externalContext);
     }
 
     public static void cleanThreadingData() {
         //we can enforce a prematurely cleanup that way
         _referenceThreadHolder = null;
     }
 
 }
