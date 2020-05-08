 package org.zeroturnaround.jenkins;
 
 /*****************************************************************
 Copyright 2011 ZeroTurnaround OÜ
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
  *****************************************************************/
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Hudson;
 import hudson.model.Result;
 import hudson.tasks.ArtifactArchiver;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.sf.json.JSONObject;
 
 import javax.servlet.ServletException;
 
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.AncestorInPath;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 import com.zeroturnaround.liverebel.api.CommandCenter;
 import com.zeroturnaround.liverebel.api.CommandCenterFactory;
 import com.zeroturnaround.liverebel.api.ConnectException;
 import com.zeroturnaround.liverebel.api.Forbidden;
 import org.zeroturnaround.jenkins.util.JenkinsLogger;
 import org.zeroturnaround.liverebel.plugins.PluginLogger;
 import org.zeroturnaround.liverebel.plugins.PluginUtil;
 import org.zeroturnaround.liverebel.plugins.Server;
 import org.zeroturnaround.liverebel.plugins.UpdateStrategies;
 
 /**
  * @author Juri Timoshin
  */
 public class LiveRebelDeployBuilder extends Builder implements Serializable {
 
   public final boolean isOverride;
   public final boolean distributeChecked;
   public final boolean undeployChecked;
   public final boolean deployOrUpdateChecked;
   public final DeployOrUpdate deployOrUpdate;
   public final Undeploy undeploy;
   public final String artifact;
   public final String app;
   public final String ver;
   public final String metadata;
   private final Action currentAction;
 
   private static final Logger LOGGER = Logger.getLogger(LiveRebelDeployBuilder.class.getName());
 
   public enum Action {
     UNDEPLOY,
     DEPLOYORUPDATE,
     DISTRIBUTE
   }
   // Fields in config.jelly must match the parameter names in the
   // "DataBoundConstructor"
   @DataBoundConstructor
   public LiveRebelDeployBuilder(String artifact, String metadata, ActionWrapper actionWrapper, OverrideForm overrideForm) {
     this.undeploy = actionWrapper.undeploy;
     this.deployOrUpdate = actionWrapper.deployOrUpdate;
     currentAction = actionWrapper.value;
 
     switch (currentAction) {
       case UNDEPLOY:
         this.undeployChecked = true;
         this.distributeChecked = false;
         this.deployOrUpdateChecked = false;
         break;
       case DEPLOYORUPDATE:
         this.deployOrUpdateChecked = true;
         this.distributeChecked = false;
         this.undeployChecked = false;
         break;
       case DISTRIBUTE:
         this.distributeChecked = true;
        this.deployOrUpdateChecked = false;
         this.undeployChecked = false;
         break;
       default:
         this.distributeChecked = true;
         this.deployOrUpdateChecked = false;
         this.undeployChecked = false;
     }
 
     this.artifact = artifact;
     this.metadata = StringUtils.trimToNull(metadata);
 
     if (overrideForm != null) {
       this.app = StringUtils.trimToNull(overrideForm.getApp());
       this.ver = StringUtils.trimToNull(overrideForm.getVer());
       this.isOverride = true;
     } else {
       this.app = null;
       this.ver = null;
       this.isOverride = false;
     }
     LOGGER.info("DATA: { artifacts="+artifact+"; app="+app+"; ver="+ver+"; metadata="+metadata+"; deployOrUpdate="+ deployOrUpdate +" }");
   }
 
   @Override
   public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
       InterruptedException {
 
     EnvVars envVars = build.getEnvironment(listener);
     String artifact = envVars.expand(this.artifact);
     String metadata = envVars.expand(this.metadata);
     String app = envVars.expand(this.app);
     String ver = envVars.expand(this.ver);
     if (deployOrUpdate != null)
       deployOrUpdate.setContextPathWithEnvVarReplaced(envVars.expand(deployOrUpdate.contextPath));
 
     FilePath deployableFile;
     FilePath metadataFilePath = null;
     if (build.getWorkspace().isRemote()) {
       new ArtifactArchiver(artifact, "", true).perform(build, launcher, listener);
       deployableFile = new FilePath(build.getArtifactsDir()).child(artifact);
       if (metadata != null)
         metadataFilePath = new FilePath(build.getArtifactsDir()).child(metadata);
     }
     else {
       deployableFile = build.getWorkspace().child(artifact);
       if (metadata != null)
         metadataFilePath = build.getWorkspace().child(metadata);
     }
 
     if (!deployableFile.exists()) {
       listener.getLogger().println("Could not find the artifact to deploy. Please, specify it in job configuration.");
       return false;
     }
 
     if (metadataFilePath != null) {
       if (!metadataFilePath.exists()) {
         listener.getLogger().println("Could not find the metadata file "+metadataFilePath.getRemote());
         return false;
       }
     }
 
     CommandCenterFactory commandCenterFactory = getCommandCenterFactory();
     PluginUtil pluginUtil = new PluginUtil(commandCenterFactory, (PluginLogger)new JenkinsLogger(listener));
 
     File metadataFile = null;
     if (metadataFilePath != null)
       metadataFile = new File(metadataFilePath.getRemote());
 
     String contextPath = "";
     UpdateStrategies updateStrategies = null;
     if (deployOrUpdate != null) {
       contextPath = deployOrUpdate.contextPath;
       updateStrategies = (UpdateStrategies) deployOrUpdate.updateStrategies;
     }
 
     if (!pluginUtil.perform(new File(deployableFile.getRemote()), metadataFile, contextPath, this.undeploy != null, updateStrategies, getDeployableServers(), app, ver))
       build.setResult(Result.FAILURE);
     return true;
   }
 
   protected CommandCenterFactory getCommandCenterFactory() {
     return new CommandCenterFactory().setUrl(getDescriptor().getLrUrl()).setVerbose(true).authenticate(getDescriptor().getAuthToken());
   }
 
   // Overridden for better type safety.
   @Override
   public DescriptorImpl getDescriptor() {
     return (DescriptorImpl) Hudson.getInstance().getDescriptor(getClass());
   }
 
   private List<String> getDeployableServers() {
     List<String> list = new ArrayList<String>();
     if (currentAction.equals(Action.DEPLOYORUPDATE)) {
       if (deployOrUpdate != null && deployOrUpdate.servers != null) {
         for (ServerCheckbox server : deployOrUpdate.servers)
           if (server.isChecked() && !server.isGroup() && server.isConnected())
             list.add(server.getId());
       }
     } else if (currentAction.equals(Action.UNDEPLOY)) {
       if (undeploy != null && undeploy.servers != null) {
         for (ServerCheckbox server : undeploy.servers)
           if (server.isChecked() && !server.isGroup() && server.isConnected())
             list.add(server.getId());
       }
     }
 
     return list;
   }
 
   @Extension
   public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
 
     private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());
     public DescriptorImpl() {
       load();
       staticAuthToken = authToken;
       staticLrUrl = lrUrl;
     }
 
     public static String staticAuthToken; //needed cause Jenkins cannot initialize static fields from xml;
     public static String staticLrUrl;
 
     private String authToken;
     private String lrUrl;
 
     public static String getAuthToken() {
       return staticAuthToken;
     }
 
     public static String getLrUrl() {
       return staticLrUrl;
     }
 
     public boolean isMetadataSupported() {
       CommandCenter cc = newCommandCenter();
       return cc != null && !cc.getVersion().equals("2.0");
     }
 
     public static CommandCenter newCommandCenter() {
       if (getLrUrl() == null || getAuthToken() == null) {
         LOGGER.warning("Please, navigate to Jenkins Configuration to specify running LiveRebel Url and Authentication Token.");
         return null;
       }
 
       try {
         return new CommandCenterFactory().setUrl(getLrUrl()).setVerbose(true).authenticate(getAuthToken()).newCommandCenter();
       }
       catch (Forbidden e) {
         LOGGER.warning("ERROR! Access denied. Please, navigate to Jenkins Configuration to specify LiveRebel Authentication Token.");
       }
       catch (ConnectException e) {
         LOGGER.warning("ERROR! Unable to connect to server.");
         LOGGER.log(Level.WARNING, "URL: {0}", e.getURL());
         if (e.getURL().equals("https://")) {
           LOGGER.warning("Please, navigate to Jenkins Configuration to specify running LiveRebel Url.");
         }
         else {
           LOGGER.log(Level.WARNING, "Reason: {0}", e.getMessage());
         }
       }
       return null;
     }
 
     public FormValidation doCheckLrUrl(@QueryParameter("lrUrl") final String value) throws IOException,
         ServletException {
       if (value != null && value.length() > 0) {
         try {
           new URL(value);
         }
         catch (Exception e) {
           return FormValidation.error("Should be a valid URL.");
         }
       }
       return FormValidation.ok();
     }
 
     public FormValidation doCheckAuthToken(@QueryParameter("authToken") final String value) throws IOException,
         ServletException {
       if (value == null || value.length() != 36) {
         return FormValidation.error("Should be a valid authentication token.");
       }
       return FormValidation.ok();
     }
 
     public FormValidation doTestConnection(@QueryParameter("authToken") final String authToken,
         @QueryParameter("lrUrl") final String lrUrl) throws IOException, ServletException {
       try {
         new CommandCenterFactory().setUrl(lrUrl).setVerbose(false).authenticate(authToken).newCommandCenter();
         return FormValidation.ok("Success");
       }
       catch (Forbidden e) {
         return FormValidation.error("Please, provide right authentication token!");
       }
       catch (ConnectException e) {
         return FormValidation.error("Could not connect to LiveRebel at (%s)", e.getURL());
       }
       catch (Exception e) {
         return FormValidation.error(e.getMessage());
       }
     }
 
     public boolean isApplicable(Class<? extends AbstractProject> aClass) {
       // Indicates that this builder can be used with all kinds of project types
       return true;
     }
 
     /**
      * This human readable name is used in the configuration screen.
      */
     public String getDisplayName() {
       return "Deploy or Update artifact with LiveRebel";
     }
 
     @Override
     public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
       // To persist global configuration information,
       // set that to properties and call save().
       authToken = formData.getString("authToken");
       lrUrl = "https://" + formData.getString("lrUrl").replaceFirst("http://", "").replaceFirst("https://", "");
       staticAuthToken = authToken;
       staticLrUrl = lrUrl;
       save();
       return super.configure(req, formData);
     }
 
     public FormValidation doCheckArtifact(@AncestorInPath AbstractProject project, @QueryParameter String value)
         throws IOException, ServletException {
 
       if (StringUtils.trimToNull(value) == null || value.length() == 0) {
         return FormValidation.error("Please provide an artifact.");
       } else  if (value.contains(",")) {
         return FormValidation.error("Please provide only one artifact.");
       }
       else {
         return FilePath.validateFileMask(project.getSomeWorkspace(), value);
       }
     }
 
     public FormValidation doCheckMetadata(@AncestorInPath AbstractProject project, @QueryParameter String value)
       throws IOException, ServletException {
 
       if (StringUtils.trimToNull(value) != null) {
         if (value.contains(",")) {
           return FormValidation.error("Please provide only one metadata file.");
         }
         String fileExtension = null;
         try {
           fileExtension = value.substring(value.lastIndexOf('.') + 1);
         }
         catch (Exception e) {
           return FormValidation.error("Metadata must be a text file!");
         }
 
         if (!fileExtension.equals("txt"))
           return FormValidation.error("Metadata must be a text file!");
         return FilePath.validateFileMask(project.getSomeWorkspace(), value);
       }
       else {
         return FormValidation.ok();
       }
     }
   }
 
   public static final class ActionWrapper {
     public final DeployOrUpdate deployOrUpdate;
     public final Undeploy undeploy;
     public final Action value;
 
     @DataBoundConstructor
     public ActionWrapper(DeployOrUpdate deployOrUpdate, String value, Undeploy undeploy) {
       this.undeploy = undeploy;
       this.deployOrUpdate = deployOrUpdate;
       this.value = Action.valueOf(value);
     }
   }
 
   private static final long serialVersionUID = 1L;
 }
