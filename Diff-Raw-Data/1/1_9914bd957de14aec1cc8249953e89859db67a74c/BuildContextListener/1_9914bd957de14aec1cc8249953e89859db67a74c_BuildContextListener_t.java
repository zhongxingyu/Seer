 package org.jenkinsci.plugins.buildcontextcapture;
 
 import hudson.Extension;
 import hudson.matrix.MatrixRun;
 import hudson.model.AbstractBuild;
 import hudson.model.Job;
 import hudson.model.Run;
 import hudson.model.TaskListener;
 import hudson.model.listeners.RunListener;
 import org.jenkinsci.plugins.buildcontextcapture.type.BuildContextCaptureType;
 
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author Gregory Boissinot
  */
 @Extension
 public class BuildContextListener extends RunListener<Run> {
 
     private static Logger LOGGER = Logger.getLogger(BuildContextListener.class.getName());
 
     @Override
     public void onCompleted(Run run, TaskListener listener) {
         AbstractBuild build = (AbstractBuild) run;
         try {
             BuildContextJobProperty buildContextJobProperty = getEnvInjectJobProperty(build);
             if (buildContextJobProperty != null) {
                 BuildContextCaptureType[] captureTypes = buildContextJobProperty.getTypes();
                 File captureOutputFile = getBuildContextCaptureDir(build);
                captureOutputFile.mkdirs();
                 if (captureTypes != null) {
                     for (BuildContextCaptureType captureType : captureTypes) {
                         captureType.capture(build, captureOutputFile);
                     }
                 }
             }
         } catch (BuildContextException be) {
             LOGGER.log(Level.SEVERE, "Problems occurs to capture build context: " + be.getMessage());
             be.printStackTrace();
         }
     }
 
     private BuildContextJobProperty getEnvInjectJobProperty(AbstractBuild build) {
         if (build == null) {
             throw new IllegalArgumentException("A build object must be set.");
         }
 
         Job job;
         if (build instanceof MatrixRun) {
             job = ((MatrixRun) build).getParentBuild().getParent();
         } else {
             job = build.getParent();
         }
 
         BuildContextJobProperty contextJobProperty = (BuildContextJobProperty) job.getProperty(BuildContextJobProperty.class);
 
         if (contextJobProperty != null) {
             if (contextJobProperty.isOn()) {
                 return contextJobProperty;
             }
         }
         return null;
     }
 
     private File getBuildContextCaptureDir(AbstractBuild build) {
         return new File(build.getRootDir(), "buildContext");
     }
 
 }
