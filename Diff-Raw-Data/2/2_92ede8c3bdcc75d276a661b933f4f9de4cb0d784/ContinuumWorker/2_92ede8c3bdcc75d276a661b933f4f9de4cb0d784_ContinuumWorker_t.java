 /*
  * Copyright 2012, MaestroDev
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.maestrodev.maestrocontinuumplugin;
 
 import com.maestrodev.MaestroWorker;
 import com.maestrodev.StompConnectionFactory;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.continuum.xmlrpc.utils.BuildTrigger;
 import org.apache.maven.continuum.xmlrpc.client.ContinuumXmlRpcClient;
 import org.apache.maven.continuum.xmlrpc.project.AddingResult;
 import org.apache.maven.continuum.xmlrpc.project.BuildAgentConfiguration;
 import org.apache.maven.continuum.xmlrpc.project.BuildAgentGroupConfiguration;
 import org.apache.maven.continuum.xmlrpc.project.BuildDefinition;
 import org.apache.maven.continuum.xmlrpc.project.BuildResult;
 import org.apache.maven.continuum.xmlrpc.project.ContinuumProjectState;
 import org.apache.maven.continuum.xmlrpc.project.ProjectGroupSummary;
 import org.apache.maven.continuum.xmlrpc.project.ProjectScmRoot;
 import org.apache.maven.continuum.xmlrpc.project.ProjectSummary;
 import org.apache.maven.continuum.xmlrpc.project.Schedule;
 import org.apache.maven.continuum.xmlrpc.system.Profile;
 import org.json.simple.JSONObject;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeoutException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Hello world!
  */
 public class ContinuumWorker extends MaestroWorker {
     private static Logger logger = Logger.getLogger(ContinuumWorker.class.getName());
 
     static final String DUPLICATE_PROJECT_ERR = "Trying to add duplicate projects in the same project group";
     static final int NO_PROJECT_GROUP = -1;
     static final String MAESTRO_SCHEDULE_NAME = "MAESTRO_SCHEDULE";
 
     static final String PARAMS_COMPOSITION_TASK_ID = "composition_task_id";
 
     static final String CONTEXT_OUTPUTS = "__context_outputs__";
     static final String PREVIOUS_CONTEXT_OUTPUTS = "__previous_context_outputs__";
 
     static final String CONTINUUM_PROJECT_ID = "continuum_project_id";
     static final String BUILD_DEFINITION_ID = "build_definition_id";
     static final String BUILD_ID = "build_id";
 
     static final String FACT_OPERATINGSYSTEM = "operatingsystem";
     static final String FACT_IPADDRESS = "ipaddress";
     static final String FACT_CONTINUUM_BUILD_AGENT = "continuum_build_agent";
 
     private ContinuumXmlRpcClient client;
     private ContinuumXmlRpcClientFactory continuumXmlRpcClientFactory;
 
     private int buildStartTimeout = 60000;
 
     public ContinuumWorker() {
         super();
         continuumXmlRpcClientFactory = ContinuumXmlRpcClientFactory.getInstance();
     }
 
     public ContinuumWorker(StompConnectionFactory stompConnectionFactory,
                            ContinuumXmlRpcClientFactory continuumXmlRpcClientFactory) {
         super(stompConnectionFactory);
         this.continuumXmlRpcClientFactory = continuumXmlRpcClientFactory;
     }
 
     void setBuildStartTimeout(int buildStartTimeout) {
         this.buildStartTimeout = buildStartTimeout;
     }
 
     private BuildAgentConfiguration getBuildAgent(String url) throws Exception {
         if (!url.contains("http")) {
             url = "http://" + url + ":8181/continuum-buildagent/xmlrpc";
         }
         List<BuildAgentConfiguration> buildAgents = client.getAllBuildAgents();
 
         for (BuildAgentConfiguration buildAgent : buildAgents) {
             if (buildAgent.getUrl().equals(url)) {
                 writeOutput("Making Sure Agent Is Enabled\n");
                 buildAgent.setEnabled(true);
                 client.updateBuildAgent(buildAgent);
                 if (!buildAgent.isEnabled()) {
                     throw new Exception("Build Agent " + buildAgent.getUrl() + " Is Currently Not Enabled");
                 }
                 return buildAgent;
             }
         }
 
         writeOutput("Adding new Continuum build agent at " + url);
         BuildAgentConfiguration buildAgentConfiguration = new BuildAgentConfiguration();
 
         buildAgentConfiguration.setDescription("Maestro Configured Build Agent (" + url + ")");
         buildAgentConfiguration.setEnabled(true);
         buildAgentConfiguration.setUrl(url);
 
         buildAgentConfiguration = client.addBuildAgent(buildAgentConfiguration);
 
         if (!buildAgentConfiguration.isEnabled()) {
             throw new Exception("Unable To Enable Build Agent At " + url);
         }
 
         return buildAgentConfiguration;
     }
 
     private BuildAgentGroupConfiguration createBuildAgentGroup(String name,
                                                                BuildAgentConfiguration buildAgentConfiguration)
             throws Exception {
         BuildAgentGroupConfiguration buildAgentGroupConfiguration = new BuildAgentGroupConfiguration();
         buildAgentGroupConfiguration.setName(name);
 
         buildAgentGroupConfiguration.addBuildAgent(buildAgentConfiguration);
 
         return client.addBuildAgentGroup(buildAgentGroupConfiguration);
     }
 
     private Profile createProfile(String name, String buildAgentGroupName) throws Exception {
         Profile profile = new Profile();
 
         profile.setBuildAgentGroup(buildAgentGroupName);
         profile.setActive(true);
         profile.setName(name);
 
         profile = client.addProfile(profile);
 
         return profile;
     }
 
     private Profile findProfile(String name) throws Exception {
         try {
             return client.getProfileWithName(name);
         } catch (Exception e) {
             writeOutput("Unable To Locate Profile With Name " + name);
             logger.warning(e.getLocalizedMessage());
         }
 
         return null;
     }
 
     private ContinuumXmlRpcClient getClient() throws MalformedURLException {
         URL url = getUrl();
         writeOutput("Using Continuum At " + url.toString() + "\n");
         return continuumXmlRpcClientFactory.getClient(url, getUsername(), getPassword());
     }
 
     private ProjectGroupSummary getProjectGroup(String projectGroupName) throws Exception {
         List<ProjectGroupSummary> projectGroups = client.getAllProjectGroups();
 
         for (ProjectGroupSummary projectGroup : projectGroups) {
             if (projectGroup.getName().equals(projectGroupName) || projectGroup.getGroupId().equals(projectGroupName)) {
                 return projectGroup;
             }
         }
 
         throw new Exception("Unable To Find Project Group " + projectGroupName);
     }
 
     private ProjectSummary getProjectSummary(String projectName, ProjectGroupSummary projectGroup) throws Exception {
         List<ProjectSummary> projects = client.getProjects(projectGroup.getId());
 
         for (ProjectSummary project : projects) {
             if (project.getName().equals(projectName))
                 return project;
         }
 
         throw new Exception("Unable To Find Project " + projectName);
     }
 
 
     private BuildDefinition getBuildDefinitionFromId(int buildDefinitionId, String goals, String arguments,
                                                      String buildFile, int projectId, Profile profile)
             throws Exception {
         BuildDefinition buildDefinition = client.getBuildDefinition(buildDefinitionId);
 
         buildDefinition.setGoals(goals);
         buildDefinition.setArguments(arguments);
         buildDefinition.setBuildFile(buildFile);
 
         if (profile != null)
             buildDefinition.setProfile(profile);
 
 
         client.updateBuildDefinitionForProject(projectId, buildDefinition);
 
         return buildDefinition;
     }
 
 
     private BuildDefinition getBuildDefinitionFromProject(String goals, String arguments, String buildFile,
                                                           int projectId, Profile profile)
             throws Exception {
         List<BuildDefinition> buildDefinitions = client.getBuildDefinitionsForProject(projectId);
         String description = createGeneratedDescription();
 
         BuildDefinition buildDefinition = null;
 
         for (BuildDefinition buildDef : buildDefinitions) {
             if (description.equals(buildDef.getDescription())) {
                 if (profile == null) {
                     buildDefinition = buildDef;
                     break;
                 }
 
                 if (buildDef.getProfile() != null &&
                         buildDef.getProfile().getName().equals(profile.getName())) {
                     buildDefinition = buildDef;
                     break;
                 }
             }
         }
         boolean update = false;
         if (buildDefinition == null) {
             writeOutput("Unable To Detect Build Definition Creation Will Begin\n");
             buildDefinition = new BuildDefinition();
         } else if (!goals.equals(buildDefinition.getGoals()) || !arguments.equals(buildDefinition.getArguments()) ||
                 !buildFile.equals(buildDefinition.getBuildFile())) {
             writeOutput("Build Definition Out of Date Update Will Begin\n");
 
             update = true;
         } else {
             return buildDefinition;
         }
 
         buildDefinition.setArguments(arguments);
         buildDefinition.setGoals(goals);
         buildDefinition.setBuildFile(buildFile);
         buildDefinition.setDescription(description);
         buildDefinition.setDefaultForProject(false);
         buildDefinition.setAlwaysBuild(false);
         buildDefinition.setBuildFresh(false);
         buildDefinition.setSchedule(getMaestroSchedule());
         buildDefinition.setType(goals);
         if (buildFile.contains("pom.xml")) {
             buildDefinition.setType("maven2");
         } else {
             buildDefinition.setType("shell");
         }
 
         if (profile != null) {
             buildDefinition.setProfile(profile);
         }
 
         try {
             if (update) {
                 client.updateBuildDefinitionForProject(projectId, buildDefinition);
                 return buildDefinition;
             }
             return client.addBuildDefinitionToProject(projectId, buildDefinition);
 
         } catch (Exception ex) {
             throw new Exception("Unable To Add Build Definition " + ex.getMessage());
         }
     }
 
     private String createGeneratedDescription() {
         return "Build Definition Generated By Maestro 4, task ID: " + getTaskId();
     }
 
     private Profile setupBuildAgent() throws Exception {
         Profile profile;
         try {
             writeOutput("Using Agent Facts To Locate Continuum Build Agent\n");
             Map<String, String> facts = getFacts();
             String agentName = getAgentName(facts);
 
             String continuumBuildAgent = facts.get(FACT_CONTINUUM_BUILD_AGENT);
             writeOutput("Configuring Continuum Build Agent At " + continuumBuildAgent + "\n");
             BuildAgentConfiguration buildAgent = getBuildAgent(continuumBuildAgent);
 
             writeOutput("Finding Build Environment " + agentName + " \n");
 
             profile = findProfile(agentName);
 
             if (profile == null) {
                 writeOutput("Build Environment Not Found, Created New [" + agentName + "]\n");
                 createBuildAgentGroup(agentName, buildAgent);
                 profile = createProfile(agentName, agentName);
             } else {
                 // verify build agent is in group
                 writeOutput("Build Environment Found, Verifying Agent\n");
                 BuildAgentGroupConfiguration buildAgentGroupConfiguration =
                         client.getBuildAgentGroup(profile.getBuildAgentGroup());
                 boolean found = false;
 
                 for (BuildAgentConfiguration ba : buildAgentGroupConfiguration.getBuildAgents()) {
                     if (ba.getUrl().equals(buildAgent.getUrl())) {
                         found = true;
                         break;
                     }
                 }
 
                 if (!found) {
                     buildAgentGroupConfiguration.addBuildAgent(buildAgent);
                     client.updateBuildAgentGroup(buildAgentGroupConfiguration);
                 }
             }
         } catch (Exception e) {
             logger.log(Level.WARNING, e.getLocalizedMessage(), e);
             throw new Exception("Error Locating Continuum Build Agent Or Creating Build Environment: " + e.getMessage(),
                     e);
         }
         return profile;
     }
 
     private BuildResult getBuildResult(int projectId) {
 
         BuildResult result = null;
 
         try {
             result = client.getLatestBuildResult(projectId);
         } catch (Exception e) {
             logger.log(Level.WARNING, "Error getting latest build results: " + e.getLocalizedMessage(), e);
         }
 
         return result;
     }
 
     private void triggerBuild(ProjectSummary project, BuildDefinition buildDefinition) throws Exception {
         int projectId = project.getId();
         int buildDefinitionId = buildDefinition.getId();
 
         // We can't construct a SCHEDULED trigger, as Continuum currently overrides it internally. Instead, call a
         // method that uses a scheduled default trigger if needed
         try {
             if (isForceBuild()) {
                 BuildTrigger buildTrigger = new BuildTrigger();
                 buildTrigger.setTrigger(ContinuumProjectState.TRIGGER_FORCED);
                 buildTrigger.setTriggeredBy(getRunUsername());
                 client.buildProject(projectId, buildDefinitionId, buildTrigger);
             } else {
                 // Trigger a "scheduled" build
                 client.addProjectToBuildQueue(projectId, buildDefinitionId);
             }
         } catch (Exception ex) {
             throw new Exception("Failed To Trigger Build " + ex.getMessage());
         }
 
         long start = System.currentTimeMillis();
 
         writeOutput("Waiting For Build To Start\n");
         setWaiting(true);
 
         // we are waiting if:
         //  - it is checking out or updating
         //  - it is currently in a queue
         // This should cover all cases until it is building, or complete
         // We can't just check for building or complete because it might not build (or do so fast) if there are no SCM
         // changes, and it is initially in a complete state until it hits one of the queues
         boolean waiting = true;
         while (waiting) {
             project = client.getProjectSummary(projectId);
 
             if (project.getState() == ContinuumProjectState.CHECKING_OUT ||
                     project.getState() == ContinuumProjectState.UPDATING ||
                     client.isProjectInPrepareBuildQueue(projectId, buildDefinitionId) ||
                     client.isProjectInBuildingQueue(projectId, buildDefinitionId) ||
                     client.isProjectCurrentlyPreparingBuild(projectId, buildDefinitionId)) {
                 if (System.currentTimeMillis() - start > buildStartTimeout) {
                     throw new TimeoutException("Failed To Detect Build Start After " + (buildStartTimeout / 1000) + " Seconds");
                 }
                 Thread.sleep(250);
             } else {
                 waiting = false;
             }
         }
         setWaiting(false);
     }
 
     private String getRunUsername() {
         JSONObject runOptions = (JSONObject) getFields().get("run_options");
         if (runOptions != null) {
             return (String) runOptions.get("username");
         }
         return null;
     }
 
     private Schedule getMaestroSchedule() throws Exception {
         List<Schedule> schedules = client.getSchedules();
         for (Schedule schedule : schedules) {
             if (schedule.getName().equals(MAESTRO_SCHEDULE_NAME)) {
                 return schedule;
             }
         }
 
         return createDefaultSchedule();
     }
 
     private Schedule createDefaultSchedule() throws Exception {
         Schedule schedule = new Schedule();
         schedule.setActive(false);
         schedule.setCronExpression("0 0 * * * ?");
         schedule.setDelay(0);
         schedule.setDescription("Generated By Maestro");
         schedule.setName(MAESTRO_SCHEDULE_NAME);
 
         return client.addSchedule(schedule);
 
     }
 
     private BuildResult waitForBuild(int projectId) throws Exception {
         ProjectSummary project = client.getProjectSummary(projectId);
         while (project.getState() != ContinuumProjectState.OK && project.getState() != ContinuumProjectState.FAILED &&
                 project.getState() != ContinuumProjectState.ERROR && project.getState() != ContinuumProjectState.NEW) {
             if (isCancelled()) {
                 client.cancelCurrentBuild();
                 break;
             }
 
             switch (project.getState()) {
                 case ContinuumProjectState.CHECKEDOUT:
                     writeOutput("Source Code Checkout Complete\n");
                     break;
                 case ContinuumProjectState.CHECKING_OUT:
                     writeOutput("Performing Source Code Checkout\n");
                     break;
                 case ContinuumProjectState.UPDATING:
                     writeOutput("Updating\n");
                     break;
                 case ContinuumProjectState.WARNING:
                     writeOutput("Warning State Detected In Continuum\n");
                     break;
                 case ContinuumProjectState.BUILDING:
                     break;
                 default:
                     throw new Exception("Unexpected project state: " + project.getState());
             }
 
             Thread.sleep(5000);
             project = client.getProjectSummary(projectId);
         }
 
         BuildResult result = getBuildResult(projectId);
         if (result == null) {
             throw new Exception("Unable to get build result for completed build for project: " + project.getId());
         }
         if (result.getExitCode() != 0 || StringUtils.isNotEmpty(result.getError())) {
             throw new Exception(result.getError());
         }
         ProjectScmRoot scmRoot = client.getProjectScmRootByProject(projectId);
         if (scmRoot != null) {
            if (scmRoot.getState() == ContinuumProjectState.ERROR) {
                 throw new Exception("Error updating from SCM: " + scmRoot.getError());
             }
         }
         return result;
     }
 
     @SuppressWarnings("unchecked")
     public void build() {
         try {
             client = getClient();
             ProjectSummary project;
             JSONObject context = getContext();
             Long projectId = (Long) context.get(CONTINUUM_PROJECT_ID);
             if (projectId != null) {
                 project = client.getProjectSummary(projectId.intValue());
             } else {
                 String projectGroupName = getGroupName();
                 writeOutput("Searching For Project Group " + projectGroupName + "\n");
                 ProjectGroupSummary projectGroup = getProjectGroup(projectGroupName);
                 writeOutput("Found Project Group " + projectGroup.getName() + "\n");
 
                 String projectName = getProjectName();
                 writeOutput("Searching For Project " + projectName + "\n");
 
                 project = getProjectSummary(projectName, projectGroup);
             }
             writeOutput("Found Project " + project.getName() + " (" + project.getId() + ")\n");
 
             String goals = getGoals();
             String arguments = getArguments();
             String buildFile = getBuildFile();
 
             Profile profile = null;
             if (getFact(FACT_CONTINUUM_BUILD_AGENT) != null) {
                 profile = setupBuildAgent();
             }
             Long taskId = getTaskId();
             writeOutput("Searching For Build Definition for task ID " + taskId + "\n");
             writeOutput("And arguments " + arguments + "\n");
 
 
             BuildDefinition buildDefinition = null;
             JSONObject previousContext = (JSONObject) getFields().get(PREVIOUS_CONTEXT_OUTPUTS);
             Long buildDefinitionId;
             if (previousContext != null &&
                     (buildDefinitionId = (Long) previousContext.get(BUILD_DEFINITION_ID)) != null) {
                 try {
                     buildDefinition = getBuildDefinitionFromId(buildDefinitionId.intValue(), goals, arguments,
                             buildFile, project.getId(), profile);
                 } catch (Exception e) {
                     logger.log(Level.FINE,
                             "Build definition not found by ID, trying project: " + e.getLocalizedMessage(), e);
                     buildDefinition = getBuildDefinitionFromProject(goals, arguments, buildFile, project.getId(), profile);
                 }
 
             }
             if (buildDefinition == null) {
                 buildDefinition = getBuildDefinitionFromProject(goals, arguments, buildFile, project.getId(), profile);
             }
 
             int previousBuildId = project.getLatestBuildId();
 
             writeOutput("Retrieved Build Definition " + buildDefinition.getId() + "\n");
 
             writeOutput("Triggering Build " + goals + "\n");
             triggerBuild(project, buildDefinition);
             writeOutput("The Build Has Started\n");
 
             BuildResult result = waitForBuild(project.getId());
 
             context.put(BUILD_DEFINITION_ID, buildDefinition.getId());
             context.put(BUILD_ID, result.getId());
 
             setField(CONTEXT_OUTPUTS, context);
 
             if (result.getId() == previousBuildId) {
                 notNeeded();
 
                 writeOutput("No SCM changes detected, build not required - previous build was #" +
                         result.getBuildNumber() + "\n");
             } else {
                 writeOutput("Completed build #" + result.getBuildNumber() + "\n");
 
                 writeOutput(client.getBuildOutput(project.getId(), project.getLatestBuildId()));
             }
 
             addLinkToBuildResult(result);
         } catch (Exception e) {
             logger.log(Level.WARNING, e.getLocalizedMessage(), e);
             setError("Continuum Build Failed: " + e.getMessage());
         }
     }
 
     private Long getTaskId() {
         JSONObject params = getParams();
         return (Long) params.get(PARAMS_COMPOSITION_TASK_ID);
     }
 
     private void addLinkToBuildResult(BuildResult result) throws Exception {
         // Not populated in Continuum 1.4.1 if it is read from a file, see CONTINUUM-2700, construct one instead
 //        String baseUrl = client.getSystemConfiguration().getBaseUrl();
         String baseUrl = getBaseUrl().toExternalForm();
         ProjectSummary project = result.getProject();
         String url = baseUrl + "/buildResult.action?projectId=" + project.getId() + "&buildId=" + result.getId();
         addLink("Continuum Build #" + result.getBuildNumber(), url);
     }
 
     private boolean isCancelled() {
         // TODO: need a way in maestro-plugin to achieve this
         return false;
     }
 
     @SuppressWarnings("unchecked")
     public void addMavenProject() {
         try {
             client = getClient();
             ProjectGroupSummary projectGroup = null;
             String groupName = getGroupName();
             if (StringUtils.isNotEmpty(groupName)) {
                 try {
                     writeOutput("Requesting Group " + groupName + " From Continuum\n");
                     projectGroup = getProjectGroup(groupName);
                     writeOutput("Found Group " + groupName + " In Continuum\n");
                 } catch (Exception e) {
                     writeOutput("Creating " + groupName + " In Continuum\n");
                     projectGroup = createProjectGroup();
                     writeOutput("Created " + groupName + " In Continuum\n");
                 }
             }
             writeOutput("Processing Project In Continuum\n");
             ProjectSummary projectSummary = createMavenProject(projectGroup);
 
             JSONObject outputData = getContext();
             outputData.put(CONTINUUM_PROJECT_ID, projectSummary.getId());
             setField(CONTINUUM_PROJECT_ID, projectSummary.getId());
             setField(CONTEXT_OUTPUTS, outputData);
 
             writeOutput("Successfully Processed Maven Project Project\n");
         } catch (Exception e) {
             logger.log(Level.FINE, e.getLocalizedMessage(), e);
             setError("Continuum Build Failed: " + e.getMessage());
         }
     }
 
     @SuppressWarnings("unchecked")
     public void addShellProject() {
         try {
             client = getClient();
             ProjectGroupSummary projectGroup;
             String groupName = getGroupName();
             try {
                 writeOutput("Requesting Group " + groupName + " From Continuum\n");
                 projectGroup = getProjectGroup(groupName);
                 writeOutput("Found Group " + groupName + " In Continuum\n");
             } catch (Exception e) {
                 writeOutput("Creating " + groupName + " In Continuum\n");
                 projectGroup = createProjectGroup();
                 writeOutput("Created " + groupName + " In Continuum\n");
             }
             ProjectSummary projectSummary;
             String projectName = getProjectName();
             try {
                 writeOutput("Requesting Project " + projectName + " In Continuum\n");
 
                 projectSummary = getProjectSummary(projectName, projectGroup);
                 writeOutput("Found Project " + projectName + " In Continuum\n");
             } catch (Exception e) {
                 writeOutput("Creating " + projectName + " In Continuum\n");
                 projectSummary = createShellProject(projectGroup.getId());
                 writeOutput("Created " + projectName + " In Continuum\n");
             }
 
             writeOutput("Successfully Processed Shell Project " + projectName + "\n");
             JSONObject outputData = getContext();
             outputData.put(CONTINUUM_PROJECT_ID, projectSummary.getId());
             setField(CONTINUUM_PROJECT_ID, projectSummary.getId());
             setField(CONTEXT_OUTPUTS, outputData);
 
         } catch (Exception e) {
             setError("Continuum Build Failed: " + e.getMessage());
         }
     }
 
     private ProjectGroupSummary createProjectGroup() throws Exception {
         ProjectGroupSummary projectGroup = new ProjectGroupSummary();
         projectGroup.setDescription(getGroupDescription());
         projectGroup.setGroupId(getGroupId());
         projectGroup.setName(getGroupName());
         client.addProjectGroup(projectGroup);
 
         return projectGroup;
     }
 
     private ProjectSummary createShellProject(int projectGroupId) throws Exception {
         ProjectSummary project = new ProjectSummary();
         project.setName(getProjectName());
         project.setDescription(getProjectDescription());
         project.setVersion(getProjectVersion());
         project.setScmUrl(getScmUrl());
         project.setScmUsername(getScmUsername());
         project.setScmPassword(getScmPassword());
         project.setScmUseCache(isScmUseCache());
         project.setScmTag(getScmBranch());
 
         project = client.addShellProject(project, projectGroupId);
 
         if (project == null) {
             throw new Exception("Unable To Create Project In " + getGroupName());
         }
         return project;
     }
 
     private ProjectSummary createMavenProject(ProjectGroupSummary projectGroup) throws Exception {
         int projectGroupId = projectGroup != null ? projectGroup.getId() : NO_PROJECT_GROUP;
 
         AddingResult result;
         String pomUrl = getPomUrl();
         if (isSingleDirectory()) {
             result = client.addMavenTwoProjectAsSingleProject(pomUrl, projectGroupId);
         } else {
             result = client.addMavenTwoProject(pomUrl, projectGroupId);
         }
         ProjectSummary project = null;
         if (result.getProjects() != null && !result.getProjects().isEmpty()) {
             project = result.getProjects().get(0);
         }
         if (result.hasErrors()) {
             if (result.getErrorsAsString().contains(DUPLICATE_PROJECT_ERR)) {
                 if (project != null) {
                     ProjectGroupSummary group = projectGroup;
                     if (group == null) {
                         group = getProjectGroup(project.getProjectGroup().getName());
                     }
                     project = getProjectSummary(project.getName(), group);
                     writeOutput("Found Existing Project (" + project.getId() + ")\n");
                 } else {
                     throw new Exception(result.getErrorsAsString() + "; unable to determine conflicting project");
                 }
             } else {
                 writeOutput("Found projects, but had errors:\n" + result.getErrorsAsString());
                 writeOutput("Projects: " + result.getProjects() + "\n");
                 writeOutput("Project Groups: " + result.getProjectGroups() + "\n");
                 throw new Exception(result.getErrorsAsString() + "; unable to determine conflicting project");
             }
         } else {
             if (project == null) {
                 throw new Exception("Unable To Create Project In " + getGroupName());
             }
             writeOutput("Project Created (" + project.getId() + ")\n");
         }
         return project;
     }
 
     JSONObject getContext() {
         JSONObject outputData = (JSONObject) getFields().get(CONTEXT_OUTPUTS);
         if (outputData == null) {
             outputData = new JSONObject();
         }
         return outputData;
     }
 
     @SuppressWarnings("unchecked")
     private Map<String, String> getFacts() {
         return (Map<String, String>) getFields().get("facts");
     }
 
     private String getFact(String name) {
         return getFacts().get(name);
     }
 
     private String getAgentName(Map<String, String> facts) {
         return facts.get(FACT_OPERATINGSYSTEM) + "-" + facts.get(FACT_IPADDRESS);
     }
 
     private URL getUrl() throws MalformedURLException {
         return getUrl("/xmlrpc");
     }
 
     private URL getBaseUrl() throws MalformedURLException {
         return getUrl("");
     }
 
     private URL getUrl(String path) throws MalformedURLException {
         String scheme = isUseSsl() ? "https" : "http";
         String webPath = getWebPath();
         if (!webPath.startsWith("/")) {
             webPath = "/" + webPath;
         }
         if (webPath.endsWith("/")) {
             webPath = webPath.substring(0, webPath.length() - 1);
         }
         return new URL(scheme, getHost(), getPort(), webPath + path);
     }
 
     private String getHost() {
         return getField("host");
     }
 
     private int getPort() {
         return Integer.parseInt(getFields().get("port").toString());
     }
 
     private String getWebPath() {
         return getField("web_path");
     }
 
     private boolean isUseSsl() {
         return Boolean.parseBoolean(getField("use_ssl"));
     }
 
     private boolean isForceBuild() {
         return Boolean.parseBoolean(getField("force_build"));
     }
 
     private String getUsername() {
         return getField("username");
     }
 
     private String getPassword() {
         return getField("password");
     }
 
     private JSONObject getParams() {
         return (JSONObject) getFields().get("params");
     }
 
     private String getGoals() {
         return getNonNullField("goals");
     }
 
     private String getArguments() {
         return getNonNullField("arguments");
     }
 
     private String getBuildFile() {
         return getNonNullField("build_file");
     }
 
     private String getNonNullField(String field) {
         String value = getField(field);
         return value != null ? value : "";
     }
 
     private String getGroupName() {
         return getField("group_name");
     }
 
     private String getGroupDescription() {
         return getField("group_description");
     }
 
     private String getGroupId() {
         return getField("group_id");
     }
 
     private String getProjectName() {
         return getField("project_name");
     }
 
     private String getProjectDescription() {
         return getField("project_description");
     }
 
     private String getProjectVersion() {
         return getField("project_version");
     }
 
     private String getScmUrl() {
         return getField("scm_url");
     }
 
     private String getScmUsername() {
         return getField("scm_username");
     }
 
     private String getScmPassword() {
         return getField("scm_password");
     }
 
     private boolean isScmUseCache() {
         return Boolean.parseBoolean(getField("scm_use_cache"));
     }
 
     private String getScmBranch() {
         return getField("scm_branch");
     }
 
     private String getPomUrl() {
         return getField("pom_url");
     }
 
     private boolean isSingleDirectory() {
         return Boolean.parseBoolean(getField("single_directory"));
     }
 }
