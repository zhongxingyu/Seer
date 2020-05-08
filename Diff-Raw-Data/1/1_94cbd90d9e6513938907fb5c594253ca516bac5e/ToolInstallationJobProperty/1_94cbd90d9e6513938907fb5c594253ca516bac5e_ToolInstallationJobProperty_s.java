 package org.jenkinsci.plugins.sharedobjects;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.model.AbstractBuild;
 import hudson.model.Computer;
 import hudson.model.EnvironmentSpecific;
 import hudson.model.TaskListener;
 import hudson.slaves.NodeSpecific;
 import hudson.tools.ToolDescriptor;
 import hudson.tools.ToolInstallation;
 import org.jenkinsci.lib.envinject.EnvInjectException;
 import org.jenkinsci.plugins.envinject.model.EnvInjectJobPropertyContributor;
 import org.jenkinsci.plugins.envinject.model.EnvInjectJobPropertyContributorDescriptor;
 import org.jenkinsci.plugins.sharedobjects.service.SharedObjectLogger;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Gregory Boissinot
  */
 public class ToolInstallationJobProperty extends EnvInjectJobPropertyContributor {
 
     private boolean populateToolInstallation;
 
     public ToolInstallationJobProperty() {
     }
 
     @DataBoundConstructor
     public ToolInstallationJobProperty(boolean populateToolInstallation) {
         this.populateToolInstallation = populateToolInstallation;
     }
 
     @SuppressWarnings("unused")
     public boolean isPopulateToolInstallation() {
         return populateToolInstallation;
     }
 
     @Override
     public void init() {
         populateToolInstallation = false;
     }
 
     @Override
     public Map<String, String> getEnvVars(AbstractBuild build, TaskListener listener) throws EnvInjectException {
         SharedObjectLogger logger = new SharedObjectLogger(listener);
         Map<String, String> result = new HashMap<String, String>();
         if (populateToolInstallation) {
             logger.info("Injecting tool installations as environment variables");
 
             for (ToolDescriptor<?> desc : ToolInstallation.all()) {
                 for (ToolInstallation tool : desc.getInstallations()) {
 
                     if (tool instanceof NodeSpecific) {
                         try {
                             tool = (ToolInstallation) ((NodeSpecific<?>) tool).forNode(Computer.currentComputer().getNode(), listener);
                         } catch (Exception x) {
                             logger.error("Could not install " + tool.getName() + " Skip this tool.");
                             continue;
                         }
                     }
 
                     if (tool instanceof EnvironmentSpecific) {
                         EnvVars e = new EnvVars();
                         tool = (ToolInstallation) ((EnvironmentSpecific<?>) tool).forEnvironment(e);
                     }
 
                     result.put(processToolName(tool.getName()), tool.getHome());
                 }
             }
         }
         return result;
     }
 
     private String processToolName(String name) {
         if (name == null) {
             return null;
         }
 
         String result = name.replace("-", "_");
         result = result.replace(" ", "_");
         return result;
     }
 
     @Extension
     public static class SharedObjectJobPropertyDescriptor extends EnvInjectJobPropertyContributorDescriptor {
 
         @Override
         public String getDisplayName() {
             return "Populate tools installations";
         }
 
 
     }
 }
