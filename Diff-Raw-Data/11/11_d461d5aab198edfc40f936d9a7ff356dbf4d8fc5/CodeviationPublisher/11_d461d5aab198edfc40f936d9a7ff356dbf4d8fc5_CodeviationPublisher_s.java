 /* Copyright (c) 2007, http://www.codeviation.org project 
  * This program is made available under the terms of the MIT License. 
  */
 
 package hudson.plugins.codeviation;
 
 import hudson.Launcher;
 import hudson.model.Action;
 import hudson.model.Build;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.model.Project;
 import hudson.tasks.Publisher;
 import java.io.IOException;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  *
  * @author pzajac
  */
 public class CodeviationPublisher extends Publisher{
 
     public CodeviationPublisher() {
     }
 
     public boolean perform(Build arg0, Launcher arg1, BuildListener arg2) throws InterruptedException,IOException {
         return true;                                                            
     }
 
     @Override
     public Action getProjectAction(Project prj) {
         return new MetricsAction(prj);
     }
 
     
 
        public Descriptor<Publisher> getDescriptor() {
         return DESCRIPTOR;
     }
 
     public static final Descriptor<Publisher> DESCRIPTOR = new DescriptorImpl();
 
     public static class DescriptorImpl extends Descriptor<Publisher> {
         public DescriptorImpl() {
             super(CodeviationPublisher.class);
         }
 
         public String getDisplayName() {
             return "Codeviation publisher";
         }
 
         public String getHelpFile() {
            return "/plugin/emma/help.html";
         }
 
         public Publisher newInstance(StaplerRequest req) throws FormException {
             CodeviationPublisher pub = new CodeviationPublisher();
           //  req.bindParameters(pub, "codeviationpublisher.");
             return pub;
         }
     }
 
 }
