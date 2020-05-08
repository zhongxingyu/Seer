 package jenkins.plugins.svn_revert;
 
 import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.scm.SubversionSCM;
 
 public class SvnReverter {
 
     private final Messenger messenger;
     private final AbstractBuild<?, ?> build;
 
     SvnReverter(final AbstractBuild<?,?> build, final Messenger messenger) {
         this.build = build;
         this.messenger = messenger;
 
     }
 
     boolean revert() {
 
        final AbstractProject<?, ?> rootProject = build.getProject().getRootProject();

        if (!(rootProject.getScm() instanceof SubversionSCM)) {
            messenger.informNotSubversionSCM();
        }
         return true;
     }
 
 }
