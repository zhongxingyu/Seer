 package org.apache.myfaces.scripting.api;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.myfaces.config.RuntimeConfig;
 import org.apache.myfaces.config.element.ManagedBean;
 import org.apache.myfaces.scripting.core.reloading.GlobalReloadingStrategy;
 import org.apache.myfaces.scripting.core.util.ClassUtils;
 import org.apache.myfaces.scripting.core.util.FileUtils;
 import org.apache.myfaces.scripting.core.util.ReflectUtil;
 import org.apache.myfaces.scripting.core.util.WeavingContext;
 import org.apache.myfaces.scripting.refresh.ReloadingMetadata;
 import org.apache.myfaces.scripting.refresh.RefreshContext;
 
 import javax.faces.context.FacesContext;
 import java.io.File;
 import java.util.*;
 
 /**
  * @author Werner Punz
  *         <p/>
  *         Refactored the common weaver code into a base class
  *         <p/>
  *         <p/>
  *         note we added a bean dropping code, the bean dropping works that way
  *         if we are the first request after a compile issued
  *         we drop all beans
  *         <p/>
  *         every other request has to drop only the session
  *         and custom scoped beans
  *         <p/>
  *         we set small mutexes to avoid at least in our code synchronisation issues
  *         the mutexes are as atomic as possible to avoid speed problems.
  *         <p/>
  *         Unfortunately if someone alters the beanmap from outside while we reload
  *         we for now cannot do anything until we have covered that in the myfaces core!
  *         <p/>
  *         Since all weavers are applicatin scoped we can handle the mutexes properly
  */
 public abstract class BaseWeaver implements ScriptingWeaver {
 
     /**
      * only be set from the
      * initialisation code so no thread safety needed
      */
 
     protected ReloadingStrategy _reloadingStrategy = null;
     private static final String SCOPE_SESSION = "session";
     private static final String SCOPE_APPLICATION = "application";
     private static final String SCOPE_REQUEST = "request";
 
     protected DynamicCompiler _compiler = null;
     protected Log _log = LogFactory.getLog(this.getClass());
     protected String _classPath = "";
     protected ClassScanner _annotationScanner = null;
     protected ClassScanner _dependencyScanner = null;
     private String _fileEnding = null;
     private int _scriptingEngine = ScriptingConst.ENGINE_TYPE_NO_ENGINE;
 
     public BaseWeaver() {
         _reloadingStrategy = new GlobalReloadingStrategy(this);
     }
 
     public BaseWeaver(String fileEnding, int scriptingEngine) {
         this._fileEnding = fileEnding;
         this._scriptingEngine = scriptingEngine;
         _reloadingStrategy = new GlobalReloadingStrategy(this);
     }
 
     /**
      * add custom source lookup paths
      *
      * @param scriptPath the new path which has to be added
      */
     public void appendCustomScriptPath(String scriptPath) {
         if (scriptPath.endsWith("/") || scriptPath.endsWith("\\/")) {
             scriptPath = scriptPath.substring(0, scriptPath.length() - 1);
         }
 
         WeavingContext.getConfiguration().addSourceDir(getScriptingEngine(), scriptPath);
         if (_annotationScanner != null) {
             _annotationScanner.addScanPath(scriptPath);
         }
         _dependencyScanner.addScanPath(scriptPath);
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
             //if not tained then we can recycle the last class loaded
             return metadata.getAClass();
         }
         synchronized (RefreshContext.COMPILE_SYNC_MONITOR) {
             //another chance just in case someone has reloaded between
             //the last if and synchronized, that way we can reduce the number of waiting threads
             if (!metadata.isTainted()) {
                 //if not tained then we can recycle the last class loaded
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
 
     protected Log getLog() {
         return LogFactory.getLog(this.getClass());
     }
 
     protected boolean assertScriptingEngine(ReloadingMetadata reloadMeta) {
         return reloadMeta.getScriptingEngine() == getScriptingEngine();
     }
 
     public String getFileEnding() {
         return _fileEnding;
     }
 
     public void setFileEnding(String fileEnding) {
         this._fileEnding = fileEnding;
     }
 
     public final int getScriptingEngine() {
         return _scriptingEngine;
     }
 
     public void setScriptingEngine(int scriptingEngine) {
         this._scriptingEngine = scriptingEngine;
     }
 
     public abstract boolean isDynamic(Class clazz);
 
     public ScriptingWeaver getWeaverInstance(Class weaverClass) {
         if (getClass().equals(weaverClass)) return this;
 
         return null;
     }
 
     /**
      * full scan, scans for all artefacts in all files
      */
     public void fullClassScan() {
         _dependencyScanner.scanPaths();
 
         if (_annotationScanner == null || FacesContext.getCurrentInstance() == null) {
             return;
         }
         _annotationScanner.scanPaths();
 
     }
 
     public void requestRefresh() {
         if (WeavingContext.getRefreshContext().isRecompileRecommended(getScriptingEngine())) {
             // we set a lock over the compile and bean refresh
             //and an inner check again to avoid unneeded compile triggers
             synchronized (RefreshContext.BEAN_SYNC_MONITOR) {
                 if (WeavingContext.getRefreshContext().isRecompileRecommended(getScriptingEngine())) {
 
                     recompileRefresh();
 
                     return;
                 }
             }
         }
         personalScopeRefresh();
 
     }
 
     protected void refreshAllManagedBeans() {
 
         if (FacesContext.getCurrentInstance() == null) {
             return;//no npe allowed
         }
         Set<String> tainted = new HashSet<String>();
         for (Map.Entry<String, ReloadingMetadata> it : WeavingContext.getFileChangedDaemon().getClassMap().entrySet()) {
             if (it.getValue().getScriptingEngine() == getScriptingEngine() && it.getValue().isTainted()) {
                 tainted.add(it.getKey());
             }
         }
         if (tainted.size() > 0) {
             boolean managedBeanTainted = false;
             //We now have to check if the tainted classes belong to the managed beans
             Set<String> managedBeanClasses = new HashSet<String>();
 
             Map<String, ManagedBean> mbeans = RuntimeConfig.getCurrentInstance(FacesContext.getCurrentInstance().getExternalContext()).getManagedBeans();
             Map<String, ManagedBean> workCopy;
 
             synchronized (RefreshContext.BEAN_SYNC_MONITOR) {
                 workCopy = makeSnapshot(mbeans);
             }
 
             for (Map.Entry<String, ManagedBean> entry : workCopy.entrySet()) {
                 managedBeanClasses.add(entry.getValue().getManagedBeanClassName());
             }
             for (String taintedClass : tainted) {
                 if (managedBeanClasses.contains(taintedClass)) {
                     managedBeanTainted = true;
                     break;
                 }
             }
 
             markSessionBeanRefreshRecommended();
 
             getLog().info("[EXT-SCRIPTING] Tainting all beans to avoid classcast exceptions");
             if (managedBeanTainted) {
 
                 for (Map.Entry<String, ManagedBean> entry : workCopy.entrySet()) {
                     Class managedBeanClass = entry.getValue().getManagedBeanClass();
                     if (WeavingContext.isDynamic(managedBeanClass)) {
                         //managed bean class found we drop the class from our session
                         removeBeanReferences(entry.getValue());
                     }
                     //one bean tainted we have to taint all dynamic beans otherwise we will get classcast
                     //exceptions
                     getLog().info("[EXT-SCRIPTING] Tainting ");
                     ReloadingMetadata metaData = WeavingContext.getFileChangedDaemon().getClassMap().get(managedBeanClass.getName());
                     if (metaData != null) {
                         metaData.setTainted(true);
                     }
                 }
             }
         }
 
     }
 
     /**
      * refreshes all personal scoped beans (aka beans which
      * have an assumed lifecycle <= session)
      * <p/>
      * This is needed for multiuser purposes because if one user alters some beans
      * other users have to drop their non application scoped beans as well!
      */
     private void refreshPersonalScopedBeans() {
         //the refreshing is only allowed if no compile is in progress
         //and vice versa
 
         synchronized (RefreshContext.BEAN_SYNC_MONITOR) {
             Map<String, ManagedBean> mbeans = RuntimeConfig.getCurrentInstance(FacesContext.getCurrentInstance().getExternalContext()).getManagedBeans();
             //the map is immutable but in between scanning might change it so we make a full copy of the map
 
             //We can synchronized the refresh, but if someone alters
             //the bean map from outside we still get race conditions
             //But for most cases this mutex should be enough
 
             Map<String, ManagedBean> workCopy;
 
             workCopy = makeSnapshot(mbeans);
 
             for (Map.Entry<String, ManagedBean> entry : workCopy.entrySet()) {
 
                 Class managedBeanClass = entry.getValue().getManagedBeanClass();
                 if (WeavingContext.isDynamic(managedBeanClass)) {
                     String scope = entry.getValue().getManagedBeanScope();
 
                     if (scope != null && !scope.equalsIgnoreCase(SCOPE_APPLICATION)) {
                         if (scope.equalsIgnoreCase(SCOPE_REQUEST)) {
                             //request, nothing has to be done here
                             return;
                         }
 
                         if (scope.equalsIgnoreCase(SCOPE_SESSION)) {
                             FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(entry.getValue().getManagedBeanName());
                         } else {
                             removeCustomScopedBean(entry.getValue());
                         }
 
                     }
 
                 }
             }
             updateBeanRefreshTime();
         }
     }
 
     /**
      * removes the references from out static scope
      * for jsf2 we probably have some kind of notification mechanism
      * which notifies custom scopes
      *
      * @param bean the managed bean which all references have to be removed from
      */
 
     private void removeBeanReferences(ManagedBean bean) {
         if (getLog().isInfoEnabled()) {
             getLog().info("[EXT-SCRIPTING] JavaScriptingWeaver.removeBeanReferences(" + bean.getManagedBeanName() + ")");
         }
 
         String scope = bean.getManagedBeanScope();
 
         if (scope != null && scope.equalsIgnoreCase(SCOPE_SESSION)) {
             FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(bean.getManagedBeanName());
         } else if (scope != null && scope.equalsIgnoreCase(SCOPE_APPLICATION)) {
             FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().remove(bean.getManagedBeanName());
             //other scope
         } else if (scope != null && !scope.equals(SCOPE_REQUEST)) {
             removeCustomScopedBean(bean);
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
        if (isFullyRecompiled()) {
             return;
         }
 
         if (_compiler == null) {
             _compiler = instantiateCompiler();//new ReflectCompilerFacade();
         }
 
         for (String scriptPath : WeavingContext.getConfiguration().getSourceDirs(getScriptingEngine())) {
             //compile via javac dynamically, also after this block dynamic compilation
             //for the entire length of the request,
             try {
                 _compiler.compileAllFiles(scriptPath, _classPath);
             } catch (ClassNotFoundException e) {
                 _log.error(e);
             }
 
         }
 
         markAsFullyRecompiled();
     }
 
     protected boolean isFullyRecompiled() {
         FacesContext context = FacesContext.getCurrentInstance();
         if (context != null) {
             return context.getExternalContext().getRequestMap().containsKey(this.getClass().getName() + "_recompiled");
         }
         return false;
     }
 
     protected void markAsFullyRecompiled() {
         FacesContext context = FacesContext.getCurrentInstance();
         if (context != null) {
             //mark the request as tainted with recompile
             if (context != null) {
                 Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
                 requestMap.put(this.getClass().getName() + "_recompiled", Boolean.TRUE);
             }
         }
        WeavingContext.getRefreshContext().setRecompileRecommended(ScriptingConst.ENGINE_TYPE_GROOVY, Boolean.FALSE);
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
 
         if (_log.isInfoEnabled()) {
             _log.info(getLoadingInfo(file));
         }
 
         Iterator<String> it = WeavingContext.getConfiguration().getSourceDirs(getScriptingEngine()).iterator();
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
         }
 
         //no refresh needed because this is done in the case of java already by
         //the classloader
         //  if (retVal != null) {
         //     refreshReloadingMetaData(sourceRoot, file, currentClassFile, retVal, ScriptingConst.ENGINE_TYPE_JAVA);
         //  }
 
         /**
          * we now scan the return value and update its configuration parameters if needed
          * this can help to deal with method level changes of class files like managed properties
          * or scope changes from shorter running scopes to longer running ones
          * if the annotation has been moved the class will be deregistered but still delivered for now
          *
          * at the next refresh the second step of the registration cycle should pick the new class up
          * //TODO we have to mark the artefacting class as deregistered and then enforce
          * //a reload this is however not the scope of the commit of this subtask
          * //we only deal with class level reloading here
          * //the deregistration notification should happen on artefact level (which will be the next subtask)
          */
         if (_annotationScanner != null && retVal != null) {
             _annotationScanner.scanClass(retVal);
         }
 
         return retVal;
     }
 
     /**
      * jsf2 helper to remove custom scoped beans
      *
      * @param bean the managed bean which has to be removed from the custom scope from
      */
     private void removeCustomScopedBean(ManagedBean bean) {
         Object scopeImpl = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get(bean.getManagedBeanScope());
         if (scopeImpl == null) return; //scope not implemented
         //we now have to revert to introspection here because scopes are a pure jsf2 construct
         //so we use a messaging pattern here to cope with it
 
         ReflectUtil.executeMethod(scopeImpl, "remove", bean.getManagedBeanName());
     }
 
     /**
      * MyFaces 2.0 keeps an immutable map over the session
      * and request scoped beans
      * if we alter that during our loop we get a concurrent modification exception
      * taking a snapshot in time fixes that
      *
      * @param mbeans the internal managed bean map which has to be investigated
      * @return a map with the class name as key and the managed bean info
      *         as value of the current state of the internal runtime config bean map
      */
     private Map<String, ManagedBean> makeSnapshot(Map<String, ManagedBean> mbeans) {
         Map<String, ManagedBean> workCopy;
 
         workCopy = new HashMap<String, ManagedBean>(mbeans.size());
         for (Map.Entry<String, ManagedBean> entry : mbeans.entrySet()) {
             workCopy.put(entry.getKey(), entry.getValue());
         }
 
         return workCopy;
     }
 
     private void updateBeanRefreshTime() {
         long sessionRefreshTime = System.currentTimeMillis();
         FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ScriptingConst.SESS_BEAN_REFRESH_TIMER, sessionRefreshTime);
     }
 
     private void markSessionBeanRefreshRecommended() {
         long sessionRefreshTime = System.currentTimeMillis();
         WeavingContext.getRefreshContext().setPersonalScopedBeanRefresh(sessionRefreshTime);
         FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ScriptingConst.SESS_BEAN_REFRESH_TIMER, sessionRefreshTime);
     }
 
     private void personalScopeRefresh() {
         //shortcut to avoid heavier operations in the beginning
         long globalBeanRefreshTimeout = WeavingContext.getRefreshContext().getPersonalScopedBeanRefresh();
         if (globalBeanRefreshTimeout == -1l) return;
 
         Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
         Long timeOut = (Long) sessionMap.get(ScriptingConst.SESS_BEAN_REFRESH_TIMER);
         if (timeOut == null || timeOut < globalBeanRefreshTimeout) {
             refreshPersonalScopedBeans();
         }
     }
 
     private void recompileRefresh() {
         synchronized (RefreshContext.COMPILE_SYNC_MONITOR) {
             fullRecompile();
         }
 
         refreshAllManagedBeans();
     }
 
     protected abstract DynamicCompiler instantiateCompiler();
 
     protected abstract String getLoadingInfo(String file);
 
 }
