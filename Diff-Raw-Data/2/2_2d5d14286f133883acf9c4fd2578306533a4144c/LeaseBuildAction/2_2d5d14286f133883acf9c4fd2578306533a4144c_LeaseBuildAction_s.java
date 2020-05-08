 package org.jenkinsci.plugins.remote_terminal_access.lease;
 
 import hudson.Extension;
 import hudson.model.InvisibleAction;
 import hudson.model.Run;
 import hudson.model.TaskListener;
 import hudson.model.listeners.RunListener;
 
 import javax.annotation.Nonnull;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * keeps tracks of leases made during a build.
  *
  * @author Kohsuke Kawaguchi
  */
 public class LeaseBuildAction extends InvisibleAction {
     final Set<String> leases = new HashSet<String>();
 
     void addLease(LeaseContext lease) {
         leases.add(lease.id);
     }
 
     /**
      * Ends all the leases.
      * @param listener
      */
     void endAll(TaskListener listener) {
         for (String lease : leases) {
             LeaseContext l = LeaseContext.getById(lease);
             if (l!=null) {
                 listener.getLogger().println("Ending lease "+lease);
                 l.end();
             }
         }
     }
 
     /**
      * When the build ends, release all the leases.
      */
     @Extension
    public class RunListenerImpl extends RunListener {
         @Override
         public void onCompleted(Run run, @Nonnull TaskListener listener) {
             LeaseBuildAction lba = run.getAction(LeaseBuildAction.class);
             if (lba!=null) {
                 lba.endAll(listener);
             }
         }
     }
 }
