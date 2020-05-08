 package org.jenkinsci.plugins.sharedobjects.type;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.Hudson;
 import hudson.model.TaskListener;
 import hudson.tasks.CommandInterpreter;
 import hudson.tasks.Shell;
 import org.jenkinsci.plugins.sharedobjects.SharedObjectException;
 import org.jenkinsci.plugins.sharedobjects.SharedObjectType;
 import org.jenkinsci.plugins.sharedobjects.SharedObjectTypeDescriptor;
 import org.jenkinsci.plugins.sharedobjects.service.SharedObjectLogger;
 import org.jenkinsci.plugins.sharedobjects.service.SharedObjectManagementFile;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.IOException;
 
 /**
  * @author Gregory Boissinot
  */
 public class ClearcaseSharedObjectType extends SharedObjectType {
 
     private String viewName;
 
     private String elementPath;
 
     @DataBoundConstructor
     public ClearcaseSharedObjectType(String name, String profile, String viewName, String elementPath) {
         this.name = Util.fixEmpty(name);
         this.profile = Util.fixEmpty(profile);
         this.viewName = Util.fixEmpty(viewName);
         this.elementPath = Util.fixEmpty(elementPath);
     }
 
     @SuppressWarnings("unused")
     public String getViewName() {
         return viewName;
     }
 
     @SuppressWarnings("unused")
     public String getElementPath() {
         return elementPath;
     }
 
     @Override
     public String getEnvVarValue(SharedObjectLogger logger) throws SharedObjectException {
 
         logger.info(String.format("Executing a cleartool command to retrieve the shared object with the name %s.", name));
         SharedObjectManagementFile sharedObjectManagementFile = new SharedObjectManagementFile();
        String tmpFilePath = sharedObjectManagementFile.getTemporaryFilePath(name, profile);
         try {
             int cmdCode = runCommandAndReturn(String.format("cleartool setview -exec 'cat %s | tee %s' %s", elementPath, tmpFilePath, viewName), logger.getListener());
             if (cmdCode != 0) {
                 throw new SharedObjectException("Command exit on failure.");
             }
             return tmpFilePath;
         } catch (IOException ioe) {
             throw new SharedObjectException(ioe);
         } catch (InterruptedException ie) {
             throw new SharedObjectException(ie);
         }
     }
 
     private int runCommandAndReturn(String command, TaskListener listener) throws IOException, InterruptedException {
         listener.getLogger().println("[SharedObject] - Running the command " + command);
         Launcher launcher = new Launcher.LocalLauncher(listener);
         CommandInterpreter batchRunner = new Shell(command);
         FilePath tmpFile = batchRunner.createScriptFile(Hudson.getInstance().getRootPath());
         return launcher.launch().cmds(batchRunner.buildCommandLine(tmpFile)).stdout(launcher.getListener()).join();
     }
 
     @Extension
     public static class ClearcaseSharedObjectTypeDescriptor extends SharedObjectTypeDescriptor {
 
         @Override
         public String getDisplayName() {
             return "Clearcase Element";
         }
 
         @Override
         public Class<? extends SharedObjectType> getType() {
             return ClearcaseSharedObjectType.class;
         }
     }
 
 }
