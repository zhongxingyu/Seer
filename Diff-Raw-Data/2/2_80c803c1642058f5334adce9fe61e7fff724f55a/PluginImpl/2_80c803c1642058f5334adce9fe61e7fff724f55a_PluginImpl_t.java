 package hudson.plugins.javanet_uploader;
 
 import hudson.tasks.BuildStep;
 import hudson.Plugin;
 
 /**
  * @author Kohsuke Kawaguchi
  * @plugin
  */
 public class PluginImpl extends Plugin {
     public void start() throws Exception {
        BuildStep.PUBLISHERS.addRecorder(JNUploaderPublisher.DESCRIPTOR);
     }
 }
