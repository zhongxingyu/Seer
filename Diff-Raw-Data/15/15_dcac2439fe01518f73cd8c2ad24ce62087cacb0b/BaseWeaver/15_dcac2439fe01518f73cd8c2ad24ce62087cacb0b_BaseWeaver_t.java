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
 package org.apache.myfaces.extensions.scripting.api;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.myfaces.extensions.scripting.core.reloading.GlobalReloadingStrategy;
 import org.apache.myfaces.extensions.scripting.core.util.ClassUtils;
 import org.apache.myfaces.extensions.scripting.core.util.FileUtils;
 import org.apache.myfaces.extensions.scripting.core.util.StringUtils;
 import org.apache.myfaces.extensions.scripting.core.util.WeavingContext;
 import org.apache.myfaces.extensions.scripting.refresh.RefreshContext;
 import org.apache.myfaces.extensions.scripting.refresh.ReloadingMetadata;
 
 import javax.faces.context.FacesContext;
 import java.io.File;
 import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Refactored the common weaver code into a base class
  * <p/>
  * Note we added a bean dropping code, the bean dropping works that way
  * if we are the first request after a compile issued
  * we drop all beans
  * <p/>
  * every other request has to drop only the session
  * and custom scoped beans
  * <p/>
  * we set small mutexes to avoid at least in our code synchronisation issues
  * the mutexes are as atomic as possible to avoid speed problems.
  * <p/>
  * Unfortunately if someone alters the bean map from outside while we reload
  * we for now cannot do anything until we have covered that in the myfaces core!
  * <p/>
  * Since all weavers are application scoped we can handle the mutexes properly *
  *
  * @author Werner Punz
  */
 public abstract class BaseWeaver implements ScriptingWeaver {
 
     /**
      * only be set from the
      * initialisation code so no thread safety needed
      */
 
     protected ReloadingStrategy _reloadingStrategy = null;
 
     protected DynamicCompiler _compiler = null;
     protected ClassScanner _annotationScanner = null;
     protected ClassScanner _dependencyScanner = null;
 
     private BeanHandler _beanHandler;
     protected String _classPath = "";
 
     Logger _log = Logger.getLogger(this.getClass().getName());
 
     private String _fileEnding = null;
     private int _scriptingEngine = ScriptingConst.ENGINE_TYPE_JSF_NO_ENGINE;
 
     public BaseWeaver() {
         _reloadingStrategy = new GlobalReloadingStrategy(this);
         _beanHandler = new MyFacesBeanHandler(getScriptingEngine());
     }
 
     public BaseWeaver(String fileEnding, int scriptingEngine) {
         this._fileEnding = fileEnding;
         this._scriptingEngine = scriptingEngine;
         _reloadingStrategy = new GlobalReloadingStrategy(this);
         _beanHandler = new MyFacesBeanHandler(getScriptingEngine());
     }
 
     /**
      * add custom source lookup paths
      *
      * @param scriptPath the new path which has to be added
      */
     public void appendCustomScriptPath(String scriptPath) {
         String normalizedScriptPath = FilenameUtils.normalize(scriptPath);
         if (normalizedScriptPath.endsWith(File.separator)) {
             normalizedScriptPath = normalizedScriptPath.substring(0, normalizedScriptPath.length() - File.separator.length());
         }
 
         WeavingContext.getConfiguration().addSourceDir(getScriptingEngine(), normalizedScriptPath);
         if (_annotationScanner != null) {
             _annotationScanner.addScanPath(normalizedScriptPath);
         }
         _dependencyScanner.addScanPath(normalizedScriptPath);
     }
 
     /**
      * condition which marks a metadata as reload candidate
      *
      * @param reloadMeta the metadata to be investigated for reload candidacy
      * @return true if it is a reload candidate
      */
     public boolean isReloadCandidate(ReloadingMetadata reloadMeta) {
         return reloadMeta != null && assertScriptingEngine(reloadMeta) && reloadMeta.isTaintedOnce();
     }
 
     /**
      * helper for accessing the reloading metadata map
      *
      * @return a map with the class name as key and the reloading meta data as value
      */
     protected Map<String, ReloadingMetadata> getClassMap() {
         return WeavingContext.getFileChangedDaemon().getClassMap();
     }
 
     /**
      * reloads a scripting instance object
      *
      * @param scriptingInstance the object which has to be reloaded
      * @param artifactType      integer value indication which type of JSF artifact we have to deal with
      * @return the reloaded object with all properties transferred or the original object if no reloading was needed
      */
     public Object reloadScriptingInstance(Object scriptingInstance, int artifactType) {
         Map<String, ReloadingMetadata> classMap = getClassMap();
         if (classMap.size() == 0) {
             return scriptingInstance;
         }
 
         ReloadingMetadata reloadMeta = classMap.get(scriptingInstance.getClass().getName());
 
         //This gives a minor speedup because we jump out as soon as possible
         //files never changed do not even have to be considered
         //not tainted even once == not even considered to be reloaded
         if (isReloadCandidate(reloadMeta)) {
 
             Object reloaded = _reloadingStrategy.reload(scriptingInstance, artifactType);
             if (reloaded != null) {
                 return reloaded;
             }
 
         }
         return scriptingInstance;
 
     }
 
     /**
      * reweaving of an existing woven class
      * by reloading its file contents and then reweaving it
      */
     public Class reloadScriptingClass(Class aclass) {
         ReloadingMetadata metadata = getClassMap().get(aclass.getName());
 
         if (metadata == null)
             return aclass;
 
         if (!assertScriptingEngine(metadata)) {
             return null;
         }
         if (!metadata.isTainted()) {
             //if not tainted then we can recycle the last class loaded
             return metadata.getAClass();
         }
         synchronized (RefreshContext.COMPILE_SYNC_MONITOR) {
             //another chance just in case someone has reloaded between
             //the last if and synchronized, that way we can reduce the number of waiting threads
             if (!metadata.isTainted()) {
                 //if not tainted then we can recycle the last class loaded
                 return metadata.getAClass();
             }
             return loadScriptingClassFromFile(metadata.getSourcePath(), metadata.getFileName());
         }
     }
 
     /**
      * recompiles and loads a scripting class from a given class name
      *
      * @param className the class name including the package
      * @return a valid class if the sources could be found null if nothing could be found
      */
     public Class loadScriptingClassFromName(String className) {
 
         Map<String, ReloadingMetadata> classMap = getClassMap();
         ReloadingMetadata metadata = classMap.get(className);
         if (metadata == null) {
             String separator = FileUtils.getFileSeparatorForRegex();
             String fileName = className.replaceAll("\\.", separator) + getFileEnding();
 
             for (String pathEntry : WeavingContext.getConfiguration().getSourceDirs(getScriptingEngine())) {
 
                 /**
                  * the reload has to be performed synchronized
                  * hence there is no chance to do it unsynchronized
                  */
                 synchronized (RefreshContext.COMPILE_SYNC_MONITOR) {
                     metadata = classMap.get(className);
                     if (metadata != null) {
                         return reloadScriptingClass(metadata.getAClass());
                     }
                     Class retVal = loadScriptingClassFromFile(pathEntry, fileName);
                     if (retVal != null) {
                         return retVal;
                     }
                 }
             }
 
         } else {
             return reloadScriptingClass(metadata.getAClass());
         }
         return null;
     }
 
     protected boolean assertScriptingEngine(ReloadingMetadata reloadMeta) {
         return reloadMeta.getScriptingEngine() == getScriptingEngine();
     }
 
     public String getFileEnding() {
         return _fileEnding;
     }
 
     @SuppressWarnings("unused")
     public void setFileEnding(String fileEnding) {
         this._fileEnding = fileEnding;
     }
 
     public final int getScriptingEngine() {
         return _scriptingEngine;
     }
 
     @SuppressWarnings("unused")
     public void setScriptingEngine(int scriptingEngine) {
         this._scriptingEngine = scriptingEngine;
     }
 
     public abstract boolean isDynamic(Class clazz);
 
     public ScriptingWeaver getWeaverInstance(Class weaverClass) {
         if (getClass().equals(weaverClass)) return this;
 
         return null;
     }
 
     /**
      * full scan, scans for all artifacts in all files
      */
     public void fullClassScan() {
         _dependencyScanner.scanPaths();
 
         if (_annotationScanner == null || FacesContext.getCurrentInstance() == null) {
             return;
         }
         _annotationScanner.scanPaths();
 
     }
 
     public void postStartupActions() {
         if (WeavingContext.getRefreshContext().isRecompileRecommended(getScriptingEngine())) {
             // we set a lock over the compile and bean refresh
             //and an inner check again to avoid unneeded compile triggers
             synchronized (RefreshContext.BEAN_SYNC_MONITOR) {
                 if (WeavingContext.getConfiguration().isInitialCompile() && WeavingContext.getRefreshContext().isRecompileRecommended(getScriptingEngine())) {
                     recompileRefresh();
                     return;
                 }
             }
         }
         _beanHandler.personalScopeRefresh();
     }
 
     public void requestRefresh() {
         if (WeavingContext.getRefreshContext().isRecompileRecommended(getScriptingEngine())) {
             // we set a lock over the compile and bean refresh
             //and an inner check again to avoid unneeded compile triggers
             synchronized (RefreshContext.BEAN_SYNC_MONITOR) {
                 if (WeavingContext.getRefreshContext().isRecompileRecommended(getScriptingEngine())) {
                     //TODO move this over to application events once they are in place
                     clearExtvalCache();
                     recompileRefresh();
                     return;
                 }
             }
         }
 
         _beanHandler.personalScopeRefresh();
 
     }
 
     /**
      * this clears the attached EXT-VAL cache in case of a refresh,
      * note this is a temporarily hack once our application
      * event system is in place this will be moved over to a specialized event handler
      */
     private void clearExtvalCache() {
         Map requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
         if (requestMap.containsKey(ScriptingConst.EXT_VAL_REQ_KEY)) {
             return;
         }
         //we have to remove the Validator Factory to clear its cache
         //ext-val does basically the same with a replacement one
         //but in case of a normal bean validation impl this has to be done
         //this is somewhat brute force, but it will be tested it it works out
 
         requestMap.put(ScriptingConst.EXT_VAL_REQ_KEY, Boolean.TRUE);
         Map applicationMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
         Set<String> keySet = applicationMap.keySet();
         boolean extValPresent = false;
         for (String key : keySet) {
             if (key.startsWith(ScriptingConst.EXT_VAL_MARKER)) {
                 applicationMap.remove(key);
                 extValPresent = true;
             }
         }
         if (!extValPresent) {
             requestMap.remove("javax.faces.validator.beanValidator.ValidatorFactory");
         }
     }
 
     /**
      * Loads a list of possible dynamic classNames
      * for this scripting engine
      *
      * @return a list of classNames which are dynamic classes
      *         for the current compile state on the filesystem
      */
     public Collection<String> loadPossibleDynamicClasses() {
 
         Collection<String> scriptPaths = WeavingContext.getConfiguration().getSourceDirs(getScriptingEngine());
         List<String> retVal = new LinkedList<String>();
 
         for (String scriptPath : scriptPaths) {
             List<File> tmpList = FileUtils.fetchSourceFiles(new File(scriptPath), "*" + getFileEnding());
             int lenRoot = scriptPath.length();
             //ok O(n2) but we are lazy for now if this imposes a problem we can flatten the inner loop out
             for (File sourceFile : tmpList) {
                 String relativeFile = sourceFile.getAbsolutePath().substring(lenRoot + 1);
                 String className = ClassUtils.relativeFileToClassName(relativeFile);
                 retVal.add(className);
             }
         }
         return retVal;
 
     }
 
     public void fullRecompile() {
         if (isFullyRecompiled() || !isRecompileRecommended()) {
             return;
         }
 
         if (_compiler == null) {
             _compiler = instantiateCompiler();//new ReflectCompilerFacade();
         }
 
         for (String scriptPath : WeavingContext.getConfiguration().getSourceDirs(getScriptingEngine())) {
             //compile via javac dynamically, also after this block dynamic compilation
             //for the entire length of the request,
             try {
                 if (!StringUtils.isBlank(scriptPath))
                     _compiler.compileAllFiles(scriptPath, _classPath);
             } catch (ClassNotFoundException e) {
                 _log.logp(Level.SEVERE, "BaseWeaver", "fullyRecompile", e.getMessage(), e);
             }
 
         }
 
         markAsFullyRecompiled();
     }
 
     protected boolean isRecompileRecommended() {
         return WeavingContext.getRefreshContext().isRecompileRecommended(getScriptingEngine());
     }
 
     protected boolean isFullyRecompiled() {
         FacesContext context = FacesContext.getCurrentInstance();
         return context != null && context.getExternalContext().getRequestMap().containsKey(this.getClass().getName() + "_recompiled");
     }
 
     public void markAsFullyRecompiled() {
         FacesContext context = FacesContext.getCurrentInstance();
         if (context != null) {
             //mark the request as tainted with recompile
             Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
             requestMap.put(this.getClass().getName() + "_recompiled", Boolean.TRUE);
         }
         WeavingContext.getRefreshContext().setRecompileRecommended(getScriptingEngine(), Boolean.FALSE);
     }
 
     /**
      * loads a class from a given sourceroot and filename
      * note this method does not have to be thread safe
      * it is called in a thread safe manner by the base class
      * <p/>
      *
      * @param sourceRoot the source search lookup path
      * @param file       the filename to be compiled and loaded
      * @return a valid class if it could be found, null if none was found
      */
     protected Class loadScriptingClassFromFile(String sourceRoot, String file) {
         //we load the scripting class from the given className
 
         File currentClassFile = new File(sourceRoot + File.separator + file);
         if (!currentClassFile.exists()) {
             return null;
         }
 
         if (_log.isLoggable(Level.INFO)) {
             _log.info(getLoadingInfo(file));
         }
 
         Class retVal = null;
 
         try {
             //we initialize the compiler lazy
             //because the facade itself is lazy
             if (_compiler == null) {
                 _compiler = instantiateCompiler();//new ReflectCompilerFacade();
             }
             retVal = _compiler.compileFile(sourceRoot, _classPath, file);
 
             if (retVal == null) {
                 return retVal;
             }
         } catch (ClassNotFoundException e) {
             //can be safely ignored
             if (_log.isLoggable(Level.FINEST)) {
                 _log.log(Level.FINEST, "loadScriptingClassFromFile(), can be ignored", e);
             }
         }
 
         //no refresh needed because this is done in the case of java already by
         //the classloader
         //  if (retVal != null) {
         //     refreshReloadingMetaData(sourceRoot, file, currentClassFile, retVal, ScriptingConst.ENGINE_TYPE_JSF_JAVA);
         //  }
 
         /**
          * we now scan the return value and update its configuration parameters if needed
          * this can help to deal with method level changes of class files like managed properties
          * or scope changes from shorter running scopes to longer running ones
          * if the annotation has been moved the class will be de-registered but still delivered for now
          *
          * at the next refresh the second step of the registration cycle should pick the new class up
          *
          */
        try {
            if (!scanAnnotation.containsKey(retVal.getName()) && _annotationScanner != null && FacesContext.getCurrentInstance() != null && retVal != null) {
                scanAnnotation.put(retVal.getName(), "");
                _annotationScanner.scanClass(retVal);
            }
        } finally {
            scanAnnotation.remove(retVal.getName());
         }
 
         return retVal;
     }
    //blocker to prevent recursive calls to the annotation scan which can be triggered by subsequent calls of scanAnnotation and loadClass
    //a simple boolean check does not suffice here because scanClass might trigger subsequent calls to other classes
    Map<String, String> scanAnnotation = new ConcurrentHashMap<String, String>();
 
     private void recompileRefresh() {
         synchronized (RefreshContext.COMPILE_SYNC_MONITOR) {
             fullRecompile();
             //we update our dependencies and annotation info prior to going
             //into the refresh cycle
             fullClassScan();
         }
 
         /*
          * we scan all intra bean dependencies
          * which are not covered by our
          * class dependency scan
          */
         _beanHandler.scanDependencies();
 
         /*
          * Now it is time to refresh the tainted managed beans
          * by now we should have a good grasp about which beans
          * need to to be refreshed (note we cannot cover all corner cases
          * but our extended dependency scan should be able to cover
          * most refreshing cases.
          */
         _beanHandler.refreshAllManagedBeans();
     }
 
     protected abstract DynamicCompiler instantiateCompiler();
 
     protected abstract String getLoadingInfo(String file);
 
 }
