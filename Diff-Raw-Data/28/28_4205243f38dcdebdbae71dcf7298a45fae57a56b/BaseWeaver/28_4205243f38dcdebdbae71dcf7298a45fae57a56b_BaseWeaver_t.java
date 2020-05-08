 package org.apache.myfaces.scripting.api;
 
 import org.apache.myfaces.scripting.api.ScriptingWeaver;
 import org.apache.myfaces.scripting.api.ScriptingConst;
 import org.apache.myfaces.scripting.refresh.ReloadingMetadata;
 import org.apache.myfaces.scripting.refresh.FileChangedDaemon;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Map;
 import java.io.File;
 
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
 
 
     public BaseWeaver() {
         //work around for yet another groovy bug
     }
 
     public BaseWeaver(String fileEnding, int scriptingEngine) {
         this.fileEnding = fileEnding;
         this.scriptingEngine = scriptingEngine;
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
     protected boolean isReloadCandidate(ReloadingMetadata reloadMeta) {
         return reloadMeta != null && assertScriptingEngine(reloadMeta) && reloadMeta.isTaintedOnce();
     }
 
     /**
      * helper for accessing the reloading metadata map
      *
      * @return
      */
     protected Map<String, ReloadingMetadata> getClassMap() {
         return FileChangedDaemon.getInstance().getClassMap();
     }
 
     /**
      * reloads a scripting instance object
      *
      * @param scriptingInstance the object which has to be reloaded
      * @return the reloaded object with all properties transferred or the original object if no reloading was needed
      */
     public Object reloadScriptingInstance(Object scriptingInstance) {
         Map<String, ReloadingMetadata> classMap = getClassMap();
         if (classMap.size() == 0) {
             return scriptingInstance;
         }
 
         ReloadingMetadata reloadMeta = classMap.get(scriptingInstance.getClass().getName());
 
         //This gives a minor speedup because we jump out as soon as possible
         //files never changed do not even have to be considered
         //not tained even once == not even considered to be reloaded
         if (isReloadCandidate(reloadMeta)) {
 
             //reload the class to get new static content if needed
             Class aclass = reloadScriptingClass(scriptingInstance.getClass());
             if (aclass.hashCode() == scriptingInstance.getClass().hashCode()) {
                 //class of this object has not changed although
                 // reload is enabled we can skip the rest now
                 return scriptingInstance;
             }
             getLog().info("possible reload for " + scriptingInstance.getClass().getName());
             /*only recreation of empty constructor classes is possible*/
             try {
                 //reload the object by instiating a new class and
                 // assigning the attributes properly
                 Object newObject = aclass.newInstance();
 
                 /*now we shuffle the properties between the objects*/
                 mapProperties(newObject, scriptingInstance);
 
                 return newObject;
             } catch (Exception e) {
                 getLog().error(e);
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
             String fileName = className.replaceAll("\\.", File.separator) + getFileEnding();
 
             //TODO this code can probably be replaced by the functionality
             //already given in the Groovy classloader, this needs further testing
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
 
     protected abstract void mapProperties(Object target, Object src);
 
     protected abstract Class loadScriptingClassFromFile(String sourceRoot, String file);
 
     public abstract boolean isDynamic(Class clazz);
 
     public List<String> getScriptPaths() {
         return scriptPaths;
     }
 
    public ScriptingWeaver getWeaverInstance(Class weaverClass) {
        if(getClass().equals(weaverClass)) return this;

        return null;
    }

 }
