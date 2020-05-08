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
 import hudson.DescriptorExtensionList;
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Describable;
 import hudson.model.Descriptor;
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
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.sf.json.JSONObject;
 
 import javax.servlet.ServletException;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 import com.zeroturnaround.liverebel.api.CommandCenter;
 import com.zeroturnaround.liverebel.api.CommandCenterFactory;
 import com.zeroturnaround.liverebel.api.ConnectException;
 import com.zeroturnaround.liverebel.api.Forbidden;
 import org.zeroturnaround.jenkins.updateModes.FailBuild;
 import org.zeroturnaround.jenkins.updateModes.LiveRebelDefault;
 import org.zeroturnaround.jenkins.updateModes.UpdateMode;
 import org.zeroturnaround.jenkins.util.JenkinsLogger;
 import org.zeroturnaround.liverebel.plugins.PluginConf;
 import org.zeroturnaround.liverebel.plugins.PluginLogger;
 import org.zeroturnaround.liverebel.plugins.PluginUtil;
 import org.zeroturnaround.liverebel.plugins.UpdateStrategies;
 
 public class LiveRebelDeployBuilder extends Builder implements Serializable {
 
   public final DeployOrUpdate deployOrUpdate;
   public final Undeploy undeploy;
   public final Upload upload;
   public final ActionWrapper action;
 
   private final PluginConf conf;
 
   private static final Logger LOGGER = Logger.getLogger(LiveRebelDeployBuilder.class.getName());
 
 
   // Fields in config.jelly must match the parameter names in the
   // "DataBoundConstructor"
   @DataBoundConstructor
   public LiveRebelDeployBuilder(ActionWrapper action) {
     this.action = action;
     if (action instanceof Undeploy) {
       this.undeploy = (Undeploy) action;
       this.upload = null;
       this.deployOrUpdate = null;
       conf = new PluginConf(PluginConf.Action.UNDEPLOY);
     } else if (action instanceof Upload) {
       this.upload = (Upload) action;
       this.undeploy = null;
       this.deployOrUpdate = null;
       conf = new PluginConf(PluginConf.Action.UPLOAD);
     } else if (action instanceof DeployOrUpdate) {
       this.undeploy = null;
       this.upload = null;
       this.deployOrUpdate = (DeployOrUpdate) action;
       conf = new PluginConf(PluginConf.Action.DEPLOY_OR_UPDATE);
     } else {
       this.upload = (Upload) action;
       this.undeploy = null;
       this.deployOrUpdate = null;
       conf = new PluginConf(PluginConf.Action.UPLOAD);
     }
   }
 
   @Override
   public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException,
       InterruptedException {
     EnvVars envVars = build.getEnvironment(listener);
     switch (conf.getAction()) {
       case UPLOAD:
         conf.deployable = getArtificatOrMetadata(envVars.expand(upload.artifact), build, launcher, listener);
         conf.metadata = getArtificatOrMetadata(envVars.expand(upload.metadata), build, launcher, listener);
         if (upload.isOverride) {
           conf.isOverride = true;
           conf.overrideApp = envVars.expand(upload.app);
           conf.overrideVer = envVars.expand(upload.ver);
         }
         break;
       case DEPLOY_OR_UPDATE:
         conf.deployable = getArtificatOrMetadata(envVars.expand(deployOrUpdate.artifact), build, launcher, listener);
         conf.metadata = getArtificatOrMetadata(envVars.expand(deployOrUpdate.metadata), build, launcher, listener);
         conf.updateStrategies = deployOrUpdate.updateStrategies == null ? getDefaultUpdateStrategies() : deployOrUpdate.updateStrategies;
         if (deployOrUpdate.isOverride) {
           conf.isOverride = true;
           conf.overrideApp = envVars.expand(deployOrUpdate.app);
           conf.overrideVer = envVars.expand(deployOrUpdate.ver);
         }
         conf.serverIds = getDeployableServers();
         conf.contextPath = envVars.expand(deployOrUpdate.contextPath);
         break;
       case UNDEPLOY:
         conf.undeployId = envVars.expand(undeploy.undeployID);
         conf.serverIds = getDeployableServers();
         break;
     }
 
     CommandCenterFactory commandCenterFactory = getCommandCenterFactory();
     PluginUtil pluginUtil = new PluginUtil(commandCenterFactory, new JenkinsLogger(listener));
 
     if (pluginUtil.perform(conf) != PluginUtil.PluginActionResult.SUCCESS)
       build.setResult(Result.FAILURE);
     return true;
   }
 
   private UpdateStrategies getDefaultUpdateStrategies() {
     UpdateStrategiesImpl updateStrategies = new UpdateStrategiesImpl(new LiveRebelDefault());
     updateStrategies.updateWithWarnings = false;
     updateStrategies.requestPauseTimeout = 30;
     updateStrategies.sessionDrainTimeout = 3600;
     return updateStrategies;
   }
 
   private File getArtificatOrMetadata(String artifact, AbstractBuild build, Launcher launcher, BuildListener listener) throws InterruptedException {
     FilePath deployableFile;
     if (artifact == null)
       return null;
     if (build.getWorkspace().isRemote()) {
       new ArtifactArchiver(artifact, "", true).perform(build, launcher, listener);
       deployableFile = new FilePath(build.getArtifactsDir()).child(artifact);
     }
     else {
       deployableFile = build.getWorkspace().child(artifact);
     }
     return new File(deployableFile.getRemote());
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
     if (conf.getAction().equals(PluginConf.Action.DEPLOY_OR_UPDATE)) {
       if (deployOrUpdate != null && deployOrUpdate.servers != null) {
         for (ServerCheckbox server : deployOrUpdate.servers)
           if (server.isChecked() && !server.isGroup() && server.isConnected())
             list.add(server.getId());
       }
     } else if (conf.getAction().equals(PluginConf.Action.UNDEPLOY)) {
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
 
     public FormValidation doCheckTestConnection() throws IOException, ServletException {
       FormValidation validation = doTestConnection(getAuthToken(), getLrUrl(), true);
       if (validation.kind != FormValidation.Kind.OK) return validation;
       return FormValidation.ok();
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
        @QueryParameter("lrUrl") final String lrUrl, @QueryParameter boolean isJobConfView) throws IOException, ServletException {
       try {
         new CommandCenterFactory().setUrl(lrUrl).setVerbose(false).authenticate(authToken).newCommandCenter();
         return FormValidation.ok("Success");
       }
       catch (Forbidden e) {
         if (isJobConfView) return FormValidation.error("Please navigate to Jenkins Configuration to specify LiveRebel Authentication Token!");
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
 
     public DescriptorExtensionList<ActionWrapper,Descriptor<ActionWrapper>> getActions() {
       return Hudson.getInstance().getDescriptorList(ActionWrapper.class);
     }
   }
 
   public static class ActionWrapper implements Describable<ActionWrapper> {
 
     public Descriptor<ActionWrapper> getDescriptor() {
       return Hudson.getInstance().getDescriptor(getClass());
     }
 
     public static class ActionWrapperDescriptor extends Descriptor<ActionWrapper> {
       public ActionWrapperDescriptor(Class<? extends ActionWrapper> clazz) {
         super(clazz);
       }
       public String getDisplayName() {
         try {
           return clazz.newInstance().toString();
         } catch (InstantiationException ignored) {}
         catch (IllegalAccessException ignored) {}
         return clazz.getSimpleName(); //fallback to class name
       }
     }
   }
 
   private static final long serialVersionUID = 1L;
 }
