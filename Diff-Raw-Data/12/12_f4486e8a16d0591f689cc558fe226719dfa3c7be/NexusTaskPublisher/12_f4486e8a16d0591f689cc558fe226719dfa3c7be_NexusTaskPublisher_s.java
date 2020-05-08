 package org.jenkinsci.plugins.nexus;
 
import groovy.json.JsonException;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Descriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 import hudson.util.FormValidation;
 import hudson.util.ListBoxModel;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 
 import org.jenkinsci.plugins.nexus.rest.NexusClient;
 import org.jenkinsci.plugins.nexus.rest.NexusJerseyClient;
 import org.jenkinsci.plugins.nexus.rest.schedules.TaskRunner;
 import org.jenkinsci.plugins.nexus.rest.schedules.TaskStatus;
 import org.jenkinsci.plugins.nexus.rest.schedules.TaskUtil;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
 import org.sonatype.nexus.rest.model.ScheduledServiceListResourceResponse;
 
 @SuppressWarnings("deprecation")
 public class NexusTaskPublisher extends Publisher {
 
 	private final String tasks;
 
 	private final NexusDescriptor selectedNexus;
 
 	private final String nexusName;
 
 	@DataBoundConstructor
 	public NexusTaskPublisher(String nexusName, String tasks) {
 		this.nexusName = nexusName;
 		this.selectedNexus = getDescriptor().getNexusMap().get(nexusName);
 		this.tasks = tasks;
 	}
 
 	public String getNexusName() {
 		return nexusName;
 	}
 
 	public String getSelected() {
 		if (selectedNexus != null) {
 			return selectedNexus.getName();
 		} else {
 			return "hoge";
 		}
 	}
 
 	public String getTasks() {
 		return tasks;
 	}
 
 	public Collection<NexusDescriptor> getNexusList() {
 		return getDescriptor().getNexusList();
 	}
 
 	@Override
 	public BuildStepMonitor getRequiredMonitorService() {
 		return BuildStepMonitor.NONE;
 	}
 
 	@Override
 	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
 
 		listener.getLogger().println("Start to submit Nexus scheduled tasks.");
 
 		// Verify configuration of Nexus is still valid.
 		if (selectedNexus == null) {
 			listener.getLogger().println("Nexus you choise is invalid.");
 			listener.getLogger().println("Please check your Jenkins's configuration.");
 			return false;
 		}
 
 		// Verify Nexus is running.
 		NexusClient client = new NexusJerseyClient(selectedNexus.getUrl(), selectedNexus.getUser(),
 				selectedNexus.getPassword());
 		client.init();
 		if (!client.ping()) {
 			listener.getLogger().println(
 					"Fail to connect Nexus. URL: " + selectedNexus.getUrl() + ", User: " + selectedNexus.getUser());
 			listener.getLogger().println("Please check your Nexus is working.");
 			return false;
 		}
 
 		TaskUtil taskUtil = new TaskUtil(client);
 
		List<TaskStatus> statusList = new ArrayList<TaskStatus>();
 
 		// Build ToDo list
 		List<ScheduledServiceListResource> todoList = new ArrayList<ScheduledServiceListResource>();
 
 		for (String taskName : tasks.split(",\\s*")) {
 			if (!taskName.matches("\\s*")) {
 				List<ScheduledServiceListResource> queryResults = taskUtil.fastFetchTasks(taskName);
 				if (queryResults != null) {
 					todoList.addAll(queryResults);
 				} else {
 					listener.getLogger().println("WARNING: There is no shceduled task whose name is " + taskName + ".");
 				}
 			}
 		}
 
 		// Execute tasks.
 		TaskRunner runner = new TaskRunner(client);
 		for (ScheduledServiceListResource task : todoList) {
 			TaskStatus status = runner.exec(task.getId());
 			listener.getLogger().println(
 					"Submitting... [id: " + task.getId() + ", name: " + task.getName() + ", type: "
 							+ task.getTypeName() + "]");
 			if (status.equals(TaskStatus.NG) || status.equals(TaskStatus.NG_UNKNOWN)) {
 				listener.getLogger().println(task.getId() + ": Return NG status. Please Check your Nexus.");
 				return false;
 			} else if (status.equals(TaskStatus.WARNING_STILL_RUNNING)) {
 				listener.getLogger().println("WARNING: This task is still running. Jenkins skipped queueing.");
 			}
 
 		}
 
 		listener.getLogger().println("End to submit Nexus scheduled tasks successfully.");
 		return true;
 	}
 
 	// Overridden for better type safety.
 	// If your plugin doesn't really define any property on Descriptor,
 	// you don't have to do this.
 	@Override
 	public DescriptorImpl getDescriptor() {
 		return (DescriptorImpl) super.getDescriptor();
 	}
 
 	@Extension
 	public static final class DescriptorImpl extends Descriptor<Publisher> {
 		private Map<String, NexusDescriptor> nexusMap;
 
 		public Collection<NexusDescriptor> getNexusList() {
 			if (nexusMap == null) {
 				nexusMap = new HashMap<String, NexusDescriptor>();
 			}
 			return nexusMap.values();
 		}
 
 		public DescriptorImpl() {
 			load();
 		}
 
 		public Map<String, NexusDescriptor> getNexusMap() {
 			return this.nexusMap;
 		}
 
 		public FormValidation doCheckTasks(@QueryParameter String value) {
 			return FormValidation.validateRequired(value);
 		}
 
 		public String getTasksAsJson() {
 
 			Map<String, List<String>> taskNameLists = new HashMap<String, List<String>>();
 
 			for (NexusDescriptor desc : nexusMap.values()) {
 				NexusClient client = new NexusJerseyClient(desc.getUrl(), desc.getUser(), desc.getPassword());
 				client.init();
 				if (client.ping()) {
 					List<ScheduledServiceListResource> serviceList = client.get("schedules",
 							ScheduledServiceListResourceResponse.class).getData();
 					if (serviceList != null && serviceList.size() > 0) {
 						List<String> serviceNameList = new ArrayList<String>();
 						for (ScheduledServiceListResource servise : serviceList) {
 							serviceNameList.add(servise.getName());
 						}
 						taskNameLists.put(desc.getName(), serviceNameList);
 					}
 				}
 			}
 
 			return JSONObject.fromObject(taskNameLists).toString();
 		}
 
 		public void setNexusMap(Map<String, NexusDescriptor> nexusMap) {
 			this.nexusMap = nexusMap;
 		}
 
 		// @DataBoundConstructor
 		// public DescriptorImpl(Map<String, NexusDescriptor> nexusMap) {
 		// super();
 		// this.nexusMap = nexusMap;
 		// }
 
 		public boolean isApplicable(Class<? extends AbstractProject<?, ?>> aClass) {
 			// Indicates that this builder can be used with all kinds of project
 			// types
 			return true;
 		}
 
 		public String getDisplayName() {
 			return "Run Nexus's task";
 		}
 
 		public ListBoxModel doFillNexusNameItems() {
 
 			ListBoxModel items = new ListBoxModel();
 			if (nexusMap != null) {
 				for (String name : nexusMap.keySet()) {
 					items.add(name + " [" + nexusMap.get(name).getUrl() + "]", name);
 				}
 			}
 			return items;
 		}
 
 		@Override
 		public synchronized boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
 
 			// The following codes are bad...
 			// How to bind formData to List<Discriptor> ?
 
 			if (nexusMap == null) {
 				nexusMap = new HashMap<String, NexusDescriptor>();
 			}
 
 			try {
 				JSONObject json = formData.getJSONObject("nexusList");
 				nexusMap.clear();
 				NexusDescriptor desc = parse(json);
 				nexusMap.put(desc.getName(), desc);
 			} catch (JSONException e) {
 				try {
 					JSONArray jsons = formData.getJSONArray("nexusList");
 					nexusMap.clear();
 					for (Object json : jsons) {
 						NexusDescriptor desc = parse((JSONObject) json);
 						nexusMap.put(desc.getName(), desc);
 					}
				} catch (JsonException ee) {
 					// exec in this path only if nexusList is empty.
 				}
 			}
 
 			save();
 			return super.configure(req, formData);
 		}
 
 		private NexusDescriptor parse(JSONObject json) {
 			return new NexusDescriptor(json.getString("name"), json.getString("url"), json.getString("user"),
 					json.getString("password"));
 		}
 
 	}
 
 }
