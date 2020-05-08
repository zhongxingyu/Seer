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
 
 import com.zeroturnaround.liverebel.api.CommandCenterFactory;
 import com.zeroturnaround.liverebel.api.ConnectException;
 import com.zeroturnaround.liverebel.api.Forbidden;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.plugins.deploy.ContainerAdapter;
 import hudson.plugins.deploy.ContainerAdapterDescriptor;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 import hudson.util.FormValidation;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.AncestorInPath;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 @SuppressWarnings("UnusedDeclaration")
 public class LiveRebelDeployPublisher extends Notifier implements Serializable {
 
 	public final String artifacts;
 	public final ContainerAdapter adapter;
 
 	public boolean useCargo() {
 		return adapter != null;
 	}
 
 	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
 	@DataBoundConstructor
 	public LiveRebelDeployPublisher(String artifacts, ContainerAdapter adapter) {
 		this.artifacts = artifacts;
 		this.adapter = adapter;
 	}
 
 	@Override
 	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
 		if (!build.getResult().equals(Result.SUCCESS)) return false;
 
 		DeployPluginProxy deployPluginProxy = new DeployPluginProxy(adapter, build, launcher, listener);
 
 		CommandCenterFactory commandCenterFactory = new CommandCenterFactory().
 			setUrl(getDescriptor().getLrUrl()).
 			setVerbose(true).
 			authenticate(getDescriptor().getAuthToken());
 
 		return new LiveRebelProxy(commandCenterFactory, build.getWorkspace().list(artifacts), useCargo(), listener, deployPluginProxy).performRelease();
 	}
 
 	// Overridden for better type safety.
 	@Override
 	public DescriptorImpl getDescriptor() {
 		return (DescriptorImpl)super.getDescriptor();
 	}
 
 	public BuildStepMonitor getRequiredMonitorService() {
 		return BuildStepMonitor.BUILD;
 	}
 
 	@Extension // This indicates to Jenkins that this is an implementation of an extension point.
 	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
 
 		public DescriptorImpl(){
 			load();
 		}
 
 		private String authToken;
 		private String lrUrl;
 
 		public String getAuthToken() {
 			return authToken;
 		}
 		public String getLrUrl(){
 			return lrUrl;
 		}
 
 		@SuppressWarnings("UnusedDeclaration")
 		public FormValidation doCheckLrUrl(@QueryParameter("lrUrl") final String value) throws IOException, ServletException {
 			if (value != null && value.length() > 0) {
 				try {
 					new URL(value);
 				} catch (Exception e) {
 					return FormValidation.error("Should be a valid URL.");
 				}
 			}
 			return FormValidation.ok();
 		}
 
 		@SuppressWarnings("UnusedDeclaration")
 		public FormValidation doCheckAuthToken(@QueryParameter("authToken") final String value) throws IOException, ServletException {
 			if (value == null || value.length() != 36)
 				return FormValidation.error("Should be a valid authentication token.");
 			return FormValidation.ok();
 		}
 
 		@SuppressWarnings("UnusedDeclaration")
 		public FormValidation doTestConnection(@QueryParameter("authToken") final String authToken, @QueryParameter("lrUrl") final String lrUrl) throws IOException, ServletException {
 			try {
 				new CommandCenterFactory().setUrl(lrUrl).setVerbose(false).authenticate(authToken).newCommandCenter();
 				return FormValidation.ok("Success");
 			}
 			catch (Forbidden e){
 				return FormValidation.error("Please, provide right authentication token!");
 			}
 			catch (ConnectException e){
				return FormValidation.error("Could not connect to LiveRebel Url (%s). LiveRebel should be running.", e.getURL());
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
 			return "Deploy artifacts with LiveRebel";
 		}
 
 		@Override
 		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
 			// To persist global configuration information,
 			// set that to properties and call save().
 			authToken = formData.getString("authToken");
 			lrUrl = "https://" + formData.getString("lrUrl").replaceFirst("http://", "").replaceFirst("https://", "");
 			save();
 			return super.configure(req,formData);
 		}
 
 		@SuppressWarnings("UnusedDeclaration")
 		public FormValidation doCheckArtifacts(@AncestorInPath AbstractProject project, @QueryParameter String value) throws IOException, ServletException {
 			if (value == null || value.length() == 0)
 				return FormValidation.error("Please, provide at least one artifact.");
 			else {
 				return FilePath.validateFileMask(project.getSomeWorkspace(), value);
 			}
 		}
 
 		@SuppressWarnings("UnusedDeclaration")
 		public List<ContainerAdapterDescriptor> getContainerAdapters() {
 			List<ContainerAdapterDescriptor> r = new ArrayList<ContainerAdapterDescriptor>(ContainerAdapter.all());
 			Collections.sort(r, new Comparator<ContainerAdapterDescriptor>() {
 				public int compare(ContainerAdapterDescriptor o1, ContainerAdapterDescriptor o2) {
 					return o1.getDisplayName().compareTo(o2.getDisplayName());
 				}
 			});
 			return r;
 		}
 	}
 }
 
