 package org.jenkinsci.plugins.sharedobjects;
 
 import hudson.Extension;
 import hudson.model.TaskListener;
 import org.jenkinsci.lib.envinject.EnvInjectException;
 import org.jenkinsci.plugins.envinject.model.EnvInjectJobPropertyContributor;
 import org.jenkinsci.plugins.envinject.model.EnvInjectJobPropertyContributorDescriptor;
 import org.jenkinsci.plugins.sharedobjects.service.SharedObjectDataStore;
 import org.jenkinsci.plugins.sharedobjects.service.SharedObjectLogger;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Gregory Boissinot
  */
 public class SharedObjectJobProperty extends EnvInjectJobPropertyContributor {
 
     private boolean populateSharedObjects;
 
     public SharedObjectJobProperty() {
     }
 
     @DataBoundConstructor
     public SharedObjectJobProperty(boolean populateSharedObjects) {
         this.populateSharedObjects = populateSharedObjects;
     }
 
     @SuppressWarnings("unused")
     public boolean getPopulateSharedObjects() {
         return populateSharedObjects;
     }
 
     @Override
     public void init() {
         populateSharedObjects = true;
     }
 
     @Override
     public Map<String, String> getEnvVars(TaskListener listener) throws EnvInjectException {
         SharedObjectLogger logger = new SharedObjectLogger(listener);
         Map<String, String> result = new HashMap<String, String>();
         if (populateSharedObjects) {
            logger.info("Injecting shared objects as environment variables");
             SharedObjectDataStore dataStore = new SharedObjectDataStore();
             try {
                 SharedObjectType[] sharedObjectTypes = dataStore.readSharedObjectsFile();
                if (sharedObjectTypes != null) {
                    for (SharedObjectType type : sharedObjectTypes) {
                        if (type != null) {
                            result.put(type.getName(), type.getEnvVarValue(logger));
                        }
                    }
                 }
 
             } catch (SharedObjectException se) {
                 throw new EnvInjectException(se);
             }
         }
         return result;
     }
 
     @Extension
     public static class SharedObjectJobPropertyDescriptor extends EnvInjectJobPropertyContributorDescriptor {
 
         @Override
         public String getDisplayName() {
             return "Populate shared objects";
         }
 
 
     }
 }
