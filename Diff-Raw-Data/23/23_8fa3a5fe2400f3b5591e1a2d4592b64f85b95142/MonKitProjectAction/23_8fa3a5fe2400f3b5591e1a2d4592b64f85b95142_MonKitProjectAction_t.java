 package net.praqma.jenkins.plugin.monkit;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Actionable;
 import hudson.model.ProminentProjectAction;
 import hudson.model.Result;
 
 public class MonKitProjectAction extends Actionable implements ProminentProjectAction {
 
     private final AbstractProject<?, ?> project;
     private boolean onlyStable;
 
     public MonKitProjectAction(AbstractProject<?, ?> project, boolean onlyStable) {
         this.project    = project;
         this.onlyStable = onlyStable;
     }
 	
 	public String getDisplayName() {
 		return "MonKit";
 	}
 
 	public String getSearchUrl() {
 		return getUrlName();
 	}
 
 	public String getIconFileName() {
 		return "graph.gif";
 	}
 
 	public String getUrlName() {
 		return "monkit";
 	}
 	
     public void doGraph(StaplerRequest req, StaplerResponse rsp) throws IOException {
         if (getLastResult() != null) {
             getLastResult().doGraph(req, rsp);
         }
     }
     
    /*
     public void doIndex(StaplerRequest req, StaplerResponse rsp) throws IOException {
     	rsp.getOutputStream().println("Her kommer der noget herre fedt paa et tidspunkt....");
     }
    */
     
     public void doSnade(StaplerRequest req, StaplerResponse rsp) throws IOException {
         rsp.getOutputStream().println("Hej, mand!");
     }
     
     public MonKitBuildAction getLastResult() {
         for (AbstractBuild<?, ?> b = getLastBuildToBeConsidered(); b != null; b = b.getPreviousNotFailedBuild()) {
             if (b.getResult() == Result.FAILURE || (b.getResult() != Result.SUCCESS && onlyStable)) {
                 continue;
             }
             
             MonKitBuildAction r = b.getAction(MonKitBuildAction.class);
             if (r != null) {
                 return r;
             }
         }
         return null;
     }
     
     private AbstractBuild<?, ?> getLastBuildToBeConsidered(){
         return onlyStable ? project.getLastStableBuild() : project.getLastSuccessfulBuild();
     }
 
 }
