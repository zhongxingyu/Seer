 package org.apache.myfaces.scripting.api;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.myfaces.config.RuntimeConfig;
 import org.apache.myfaces.config.element.ManagedBean;
 import org.apache.myfaces.scripting.core.reloading.GlobalReloadingStrategy;
 import org.apache.myfaces.scripting.core.util.FileUtils;
 import org.apache.myfaces.scripting.core.util.ReflectUtil;
 import org.apache.myfaces.scripting.core.util.WeavingContext;
 import org.apache.myfaces.scripting.core.util.ClassUtils;
 import org.apache.myfaces.scripting.refresh.ReloadingMetadata;
 
 import javax.faces.context.FacesContext;
 import java.io.File;
 import java.util.*;
 
 /**
  * @author Werner Punz
  *         <p/>
  *         Refactored the common weaver code into a base class
  */
 public abstract class BaseWeaver implements ScriptingWeaver {
 
     private String fileEnding = null;
     private int scriptingEngine = ScriptingConst.ENGINE_TYPE_NO_ENGINE;
     /**
      * only be set from the
      * initialisation code so no thread safety needed
      */
     protected List<String> scriptPaths = new LinkedList<String>();
 
     protected ReloadingStrategy _reloadingStrategy = null;
     private static final String SCOPE_SESSION = "session";
     private static final String SCOPE_APPLICATION = "application";
     private static final String SCOPE_REQUEST = "request";
 
 
     public BaseWeaver() {
         _reloadingStrategy = new GlobalReloadingStrategy(this);
     }
 
     public BaseWeaver(String fileEnding, int scriptingEngine) {
         this.fileEnding = fileEnding;
         this.scriptingEngine = scriptingEngine;
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
 
         getScriptPaths().add(scriptPath);
     }
 
 
     /**
      * condition which marks a metadata as reload candidate
      */
     public boolean isReloadCandidate(ReloadingMetadata reloadMeta) {
         return reloadMeta != null && assertScriptingEngine(reloadMeta) && reloadMeta.isTaintedOnce();
     }
 
     /**
      * helper for accessing the reloading metadata map
      *
      * @return
      */
     protected Map<String, ReloadingMetadata> getClassMap() {
         return WeavingContext.getFileChangedDaemon().getClassMap();
     }
 
     /**
      * reloads a scripting instance object
      *
      * @param scriptingInstance the object which has to be reloaded
      * @return the reloaded object with all properties transferred or the original object if no reloading was needed
      */
     public Object reloadScriptingInstance(Object scriptingInstance, int artefactType) {
         Map<String, ReloadingMetadata> classMap = getClassMap();
         if (classMap.size() == 0) {
             return scriptingInstance;
         }
 
         ReloadingMetadata reloadMeta = classMap.get(scriptingInstance.getClass().getName());
 
         //This gives a minor speedup because we jump out as soon as possible
         //files never changed do not even have to be considered
         //not tained even once == not even considered to be reloaded
         if (isReloadCandidate(reloadMeta)) {
 
             Object reloaded = _reloadingStrategy.reload(scriptingInstance, artefactType);
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
         synchronized (BaseWeaver.class) {
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
      * recompiles and loads a scripting class from a given classname
      *
      * @param className the classname including the package
      * @return a valid class if the sources could be found null if nothing could be found
      */
     public Class loadScriptingClassFromName(String className) {
         if (className.contains("TestBean2")) {
             getLog().debug("debugpoint found");
         }
 
         Map<String, ReloadingMetadata> classMap = getClassMap();
         ReloadingMetadata metadata = classMap.get(className);
         if (metadata == null) {
             String separator = FileUtils.getFileSeparatorForRegex();
             String fileName = className.replaceAll("\\.", separator) + getFileEnding();
 
             for (String pathEntry : getScriptPaths()) {
 
                 /**
                  * the reload has to be performed synchronized
                  * hence there is no chance to do it unsynchronized
                  */
                 synchronized (BaseWeaver.class) {
                     metadata = classMap.get(className);
                     if (metadata != null) {
                         return reloadScriptingClass(metadata.getAClass());
                     }
                     Class retVal = (Class) loadScriptingClassFromFile(pathEntry, fileName);
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
 
     //TODO move this into the classloader to cover dependend classes as well
     protected void refreshReloadingMetaData(String sourceRoot, String file, File currentClassFile, Class retVal, int engineType) {
         ReloadingMetadata reloadingMetaData = new ReloadingMetadata();
         reloadingMetaData.setAClass(retVal);
 
         reloadingMetaData.setFileName(file);
         reloadingMetaData.setSourcePath(sourceRoot);
         reloadingMetaData.setTimestamp(currentClassFile.lastModified());
         reloadingMetaData.setTainted(false);
         reloadingMetaData.setScriptingEngine(engineType);
         //ReloadingMetadata oldMetadata = getClassMap().get(retVal.getName());
         reloadingMetaData.setTaintedOnce(getClassMap().containsKey(retVal.getName()));
 
         getClassMap().put(retVal.getName(), reloadingMetaData);
     }
 
     protected Log getLog() {
         return LogFactory.getLog(this.getClass());
     }
 
     protected boolean assertScriptingEngine(ReloadingMetadata reloadMeta) {
         return reloadMeta.getScriptingEngine() == getScriptingEngine();
     }
 
 
     public String getFileEnding() {
         return fileEnding;
     }
 
     public void setFileEnding(String fileEnding) {
         this.fileEnding = fileEnding;
     }
 
     public int getScriptingEngine() {
         return scriptingEngine;
     }
 
     public void setScriptingEngine(int scriptingEngine) {
         this.scriptingEngine = scriptingEngine;
     }
 
 
     protected abstract Class loadScriptingClassFromFile(String sourceRoot, String file);
 
     public abstract boolean isDynamic(Class clazz);
 
     public List<String> getScriptPaths() {
         return scriptPaths;
     }
 
     public ScriptingWeaver getWeaverInstance(Class weaverClass) {
         if (getClass().equals(weaverClass)) return this;
 
         return null;
     }
 
     public void fullAnnotationScan() {
     }
 
 
     public void requestRefresh() {
         if (WeavingContext.getRefreshContext().isRecompileRecommended(getScriptingEngine())) {
             fullRecompile();
             refreshAllManagedBeans();
         } else {
             //shortcut to avoid heavier operations in the beginning
             long globalBeanRefreshTimeout = WeavingContext.getRefreshContext().getPersonalScopedBeanRefresh();
             if (globalBeanRefreshTimeout == -1l) return;
 
             Map sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
             Long timeOut = (Long) sessionMap.get(ScriptingConst.SESS_BEAN_REFRESH_TIMER);
             if (timeOut == null || timeOut < globalBeanRefreshTimeout) {
                 refreshPersonalScopedBeans();
             }
         }
     }
 
 
     protected void refreshAllManagedBeans() {
         //TODO set a mutex and a double check here to avoid
         //double dropping the entire managed beans
         //in case of multiuser access
 
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
             for (Map.Entry<String, ManagedBean> entry : mbeans.entrySet()) {
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
 
                 Map<String, ManagedBean> workCopy = makeSnapshot(mbeans);
 
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
 
    private Map<String, ManagedBean> makeSnapshot(Map<String, ManagedBean> mbeans) {
         Map<String, ManagedBean> workCopy = new HashMap<String, ManagedBean>(mbeans.size());
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
 
     /**
      * refreshes all personal scoped beans (aka beans which
      * have an assumed lifecycle <= session)
      * <p/>
      * This is needed for multiuser purposes because if one user alters some beans
      * other users have to drop their non application scoped beans as well!
      */
     private void refreshPersonalScopedBeans() {
 
         Map<String, ManagedBean> mbeans = RuntimeConfig.getCurrentInstance(FacesContext.getCurrentInstance().getExternalContext()).getManagedBeans();
         //the map is immutable but in between scanning might change it so we make a full copy of the map
 
         Map<String, ManagedBean> workCopy = makeSnapshot(mbeans);
 
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
 
     /**
      * removes the references from out static scope
      * for jsf2 we probably have some kind of notification mechanism
      * which notifies custom scopes
      *
      * @param bean
      */
     private void removeBeanReferences(ManagedBean bean) {
         getLog().info("[EXT-SCRIPTING] JavaScriptingWeaver.removeBeanReferences(" + bean.getManagedBeanName() + ")");
 
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
      * jsf2 helper to remove custom scoped beans
      *
      * @param bean
      */
     private void removeCustomScopedBean(ManagedBean bean) {
         Object scopeImpl = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get(bean.getManagedBeanScope());
         if (scopeImpl == null) return; //scope not implemented
         //we now have to revert to introspection here because scopes are a pure jsf2 construct
         //so we use a messaging pattern here to cope with it
 
         ReflectUtil.executeMethod(scopeImpl, "remove", bean.getManagedBeanName());
     }
 
     /**
      * Loads a list of possible dynamic classNames
      * for this scripting engine
      *
      * @return a list of classNames which are dynamic classes
      *         for the current compile state on the filesystem
      */
     public Collection<String> loadPossibleDynamicClasses() {
 
         List<String> scriptPaths = getScriptPaths();
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
 }
