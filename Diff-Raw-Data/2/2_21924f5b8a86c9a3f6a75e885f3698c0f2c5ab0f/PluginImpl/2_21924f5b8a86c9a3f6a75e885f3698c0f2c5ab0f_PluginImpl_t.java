 package hudson.plugins.jwsdp_sqe;
 
 import hudson.Plugin;
 import hudson.tasks.BuildStep;
 
 /**
  * @author Kohsuke Kawaguchi
  * @plugin
  */
 public class PluginImpl extends Plugin {
     public void start() throws Exception {
         // make sure it comes before the mailer 
        BuildStep.PUBLISHERS.addRecorder(SQETestResultPublisher.DescriptorImpl.DESCRIPTOR);
     }
 }
