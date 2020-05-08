 package com.controlgroup.ci.jenkinsgithubcommitstatus;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import java.io.IOException;
 
 /**
  * Company: Control Group
  * User: vikash
  * Date: 4/26/13
  * Time: 3:18 PM
  *
  * This class notifies github of build status utilizing the github status api: http://developer.github.com/v3/repos/statuses/
  */
 public class GithubStatusNotification extends Notifier {
 
     private final String repoName, repoOwner, authToken;
     private static IGithubStatusAPI githubStatusAPI = new CurlImpl();
 
     public BuildStepMonitor getRequiredMonitorService() {
         return BuildStepMonitor.BUILD;
     }
 
     @DataBoundConstructor
     public GithubStatusNotification(String repoName, String repoOwner, String authToken){
         this.repoName = repoName;
         this.repoOwner = repoOwner;
         this.authToken = authToken;
     }
 
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
             throws InterruptedException, IOException {
 
         //Get build environment variables such as git sha
         EnvVars envVars = new EnvVars();
         envVars = build.getEnvironment(listener);
 
         //create notification
         Notification notification = new Notification();
         notification.setRepoName(repoName);
         notification.setRepoOwner(repoOwner);
         notification.setCommitSha(envVars.get("GIT_COMMIT"));
         notification.setAuthToken(authToken);
        notification.setBuildResult(build.getResult().toString());
        listener.getLogger().println(String.format("POSTING Status of %s to /repos/%s/%s/statuses/%s",build.getResult().toString(),repoOwner,repoName,envVars.get("GIT_COMMIT")));
         //do post
         githubStatusAPI.doPost(listener,launcher,notification);
         return true;
     }
 
     public String getAuthToken() {
         return authToken;
     }
 
     public String getRepoName() {
         return repoName;
     }
 
     public String getRepoOwner() {
         return repoOwner;
     }
 
 
     /*
     *
     * Descriptor class needed by Jenkins to help identify and manage plugin
     */
     @Extension
     public static class Descriptor extends BuildStepDescriptor<Publisher>{
         private String repoName;
         private String repoOwner;
         private String authToken;
 
         @Override
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             // Indicates that this builder can be used with all kinds of project types
             return true;
         }
 
         @Override
         public String getDisplayName() {
             return "Github Repository Status Notification";  //To change body of implemented methods use File | Settings | File Templates.
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
             repoName = json.getString("repoName");
             repoOwner = json.getString("repoOwner");
             authToken = json.getString("authToken");
             save();
             return super.configure(req, json);
         }
 
         public String getRepoName() {
             return repoName;
         }
 
         public void setRepoName(String repoName) {
             this.repoName = repoName;
         }
 
         public String getRepoOwner() {
             return repoOwner;
         }
 
         public void setRepoOwner(String repoOwner) {
             this.repoOwner = repoOwner;
         }
 
         public String getAuthToken() {
             return authToken;
         }
 
         public void setAuthToken(String authToken) {
             this.authToken = authToken;
         }
     }
 }
