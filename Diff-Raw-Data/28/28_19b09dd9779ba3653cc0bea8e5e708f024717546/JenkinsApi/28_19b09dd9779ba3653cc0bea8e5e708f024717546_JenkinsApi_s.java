 package com.zombietank.jenkins.model;
 
 import java.io.Serializable;
 import java.util.List;
 
 import org.simpleframework.xml.Default;
 import org.simpleframework.xml.ElementList;
 import org.simpleframework.xml.Root;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Lists;
 
 @Root(name = "hudson", strict = false) // TODO: will this change?
 @Default(required = false)
 public class JenkinsApi implements Serializable {
 	private static final long serialVersionUID = 1L;
 	private String assignedLabel;
 	private String mode;
 	private String description;
 	private String nodeName;
 	private int numExecutors;
 	private String nodeDescription;
 	private int slaveAgentPort;
 	private boolean useSecurity;
 	private boolean useCrumbs;
 	private View primaryView;
	@ElementList(inline = true, name = "view")
 	private List<View> views = Lists.newArrayList();
 	@ElementList(inline = true, name = "job")
 	private List<Job> jobs = Lists.newArrayList();
 
 	public String getMode() {
 		return mode;
 	}
 
 	public void setMode(String mode) {
 		this.mode = mode;
 	}
 
 	public String getNodeName() {
 		return nodeName;
 	}
 
 	public void setNodeName(String nodeName) {
 		this.nodeName = nodeName;
 	}
 
 	public String getNodeDescription() {
 		return nodeDescription;
 	}
 
 	public void setNodeDescription(String nodeDescription) {
 		this.nodeDescription = nodeDescription;
 	}
 
 	public int getNumExecutors() {
 		return numExecutors;
 	}
 
 	public void setNumExecutors(int numExecutors) {
 		this.numExecutors = numExecutors;
 	}
 
 	public int getSlaveAgentPort() {
 		return slaveAgentPort;
 	}
 
 	public void setSlaveAgentPort(int slaveAgentPort) {
 		this.slaveAgentPort = slaveAgentPort;
 	}
 
 	public boolean isUseSecurity() {
 		return useSecurity;
 	}
 
 	public void setUseSecurity(boolean useSecurity) {
 		this.useSecurity = useSecurity;
 	}
 
 	public boolean isUseCrumbs() {
 		return useCrumbs;
 	}
 
 	public void setUseCrumbs(boolean useCrumbs) {
 		this.useCrumbs = useCrumbs;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setAssignedLabel(String assignedLabel) {
 		this.assignedLabel = assignedLabel;
 	}
 
 	public String getAssignedLabel() {
 		return assignedLabel;
 	}
 
 	public void setPrimaryView(View primaryView) {
 		this.primaryView = primaryView;
 	}
 
 	public View getPrimaryView() {
 		return primaryView;
 	}
 
 	public void setViews(List<View> views) {
 		this.views = views;
 	}
 
 	public List<View> getViews() {
 		return views;
 	}
 
 	public void setJobs(List<Job> jobs) {
 		this.jobs = jobs;
 	}
 
 	public List<Job> getJobs() {
 		return jobs;
 	}
 
 	@Override
 	public String toString() {
 		return Objects.toStringHelper(this).add("assignedLabel", assignedLabel)
 				.add("mode", mode).add("description", description)
 				.add("nodeName", nodeName).add("numExecutors", numExecutors)
 				.add("nodeDescription", nodeDescription)
 				.add("slaveAgentPort", slaveAgentPort)
 				.add("useSecurity", useSecurity).add("useCrumbs", useCrumbs)
 				.add("primaryView", primaryView).add("views", views)
 				.add("jobs", jobs).toString();
 	}
 
 }
