 package org.jenkinsci.plugins.githubcommitstatusupdater;
 import hudson.Launcher;
 import hudson.Extension;
 import hudson.util.FormValidation;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.AbstractProject;
 import hudson.tasks.Builder;
 import hudson.tasks.BuildStepDescriptor;
 
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.QueryParameter;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 import hudson.tasks.Notifier;
 import hudson.EnvVars;
 import hudson.model.Result;
 import hudson.model.Hudson;
 
 import org.kohsuke.github.GHCommitState;
 import org.kohsuke.github.GHRepository;
 import org.kohsuke.github.GitHub;
 
 import java.io.IOException;
 
 /**
  * @author qluto
  */
 public class Updater extends Notifier {
   private String githubApi;
   private String repositoryName;
   private String accessToken;
 
   private GitHub github;
 
   @DataBoundConstructor
   public Updater(String githubApi, String repositoryName, String accessToken) {
     this.githubApi = githubApi;
     this.repositoryName = repositoryName;
     this.accessToken = accessToken;
   }
   public String getGithubApi() {
     return githubApi;
   }
   public String getAccessToken() {
     return accessToken;
   }
 
   @Override
   public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
     if (accessToken != null && !accessToken.isEmpty()
         && repositoryName != null && !repositoryName.isEmpty()) {
       try {
         if (githubApi != null && !githubApi.isEmpty()) {
           github = GitHub.connectUsingOAuth(githubApi, accessToken);
         } else {
           github = GitHub.connectUsingOAuth(accessToken);
         }
         GHCommitState state;
         String description = "Build finished.";
         String url = "";
 
         EnvVars env = build.getEnvironment(listener);
         //String sha1 = env.get("GIT_COMMIT", "");
         String sha1 = env.get("GIT_COMMIT");
         listener.getLogger().println("sha1: " + sha1);
 
         url = hudson.model.Hudson.getInstance().getRootUrl() + build.getUrl();
         if (Hudson.getInstance().getRootUrl() != null) {
           url = Hudson.getInstance().getRootUrl() + build.getUrl();
         }
 
         Result result = build.getResult();
         if (result.isBetterOrEqualTo(Result.SUCCESS)) {
           state = GHCommitState.SUCCESS;
         } else {
           state = GHCommitState.FAILURE;
         }
 
         GHRepository repo = github.getRepository(repositoryName);
         repo.createCommitStatus(sha1, state, url, description);
 
         listener.getLogger().println("update commit-status: success!");
       } catch (IOException e) {
         listener.getLogger().println(e);
         listener.getLogger().println("update commit-status: failue...");
         return false;
       } catch (Exception e) {
         listener.getLogger().println(e);
         listener.getLogger().println("update commit-status: failue...");
         return false;
       }
     }
     return true;
   }
   public BuildStepMonitor getRequiredMonitorService() {
     return BuildStepMonitor.NONE;
   }
 
   @Override
   public DescriptorImpl getDescriptor() {
     return (DescriptorImpl) super.getDescriptor();
   }
 
   @Extension
   public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
     @Override
     public boolean isApplicable(Class<? extends AbstractProject> jobType) {
       return true;
     }
     public DescriptorImpl() {
       load();
     }
     @Override
     public String getDisplayName() {
       return "Update commit status on Github";
     }
   }
 }
