 package com.maestrodev.maestrocontinuumplugin;
 
 import com.maestrodev.MaestroWorker;
 import com.maestrodev.StompConnectionFactory;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeoutException;
 import org.apache.continuum.xmlrpc.utils.BuildTrigger;
 import org.apache.maven.continuum.xmlrpc.client.ContinuumXmlRpcClient;
 import org.apache.maven.continuum.xmlrpc.project.*;
 import org.apache.maven.continuum.xmlrpc.system.Profile;
 import org.json.simple.JSONObject;
 
 /**
  * Hello world!
  *
  */
 public class ContinuumWorker extends MaestroWorker
 {
     
     private ContinuumXmlRpcClient client;
     private ContinuumXmlRpcClientFactory continuumXmlRpcClientFactory;
     
     public ContinuumWorker(){
         super();
         continuumXmlRpcClientFactory = ContinuumXmlRpcClientFactory.getInstance();
     }    
     
     public ContinuumWorker(StompConnectionFactory stompConnectionFactory, 
             ContinuumXmlRpcClientFactory continuumXmlRpcClientFactory) {
         super(stompConnectionFactory);
         this.continuumXmlRpcClientFactory = continuumXmlRpcClientFactory;
     }
     
     private BuildAgentConfiguration getBuildAgent(String url) throws Exception {
         if(!url.contains("http")) {
           url = "http://" + url + ":8181/continuum-buildagent/xmlrpc";
         }
         List<BuildAgentConfiguration> buildAgents = client.getAllBuildAgents();
         
         for(BuildAgentConfiguration buildAgent: buildAgents){
             if(buildAgent.getUrl().equals(url)){
                 writeOutput("Making Sure Agent Is Enabled\n");
                 buildAgent.setEnabled(true);
                 client.updateBuildAgent(buildAgent);
                 if(!buildAgent.isEnabled()){
                     throw new Exception("Build Agent " + buildAgent.getUrl() + 
                             " Is Currently Not Enabled");
                 }
                 return buildAgent;
             }
         }
         
         BuildAgentConfiguration buildAgentConfiguration = new BuildAgentConfiguration();
         
         buildAgentConfiguration.setDescription("Maestro Configured Build Agent (" + url + ")");
         buildAgentConfiguration.setEnabled(true);
         buildAgentConfiguration.setUrl(url);
         
         
         buildAgentConfiguration = client.addBuildAgent(buildAgentConfiguration);
         
         
         if(!buildAgentConfiguration.isEnabled()){
             throw new Exception("Unable To Find Build Agent At " + url);
         }
         
         return buildAgentConfiguration;
     }
     
     private BuildAgentGroupConfiguration createBuildAgentGroup(String name, BuildAgentConfiguration buildAgentConfiguration) throws Exception{
         BuildAgentGroupConfiguration buildAgentGroupConfiguration = new BuildAgentGroupConfiguration();
         buildAgentGroupConfiguration.setName(name);
                 
         buildAgentGroupConfiguration.addBuildAgent(buildAgentConfiguration);
         
         return client.addBuildAgentGroup(buildAgentGroupConfiguration);
     }
     
     private Profile createProfile(String name, String buildAgentGroupName) throws Exception{
         Profile profile = new Profile();
         
         profile.setBuildAgentGroup(buildAgentGroupName);
         profile.setActive(true);
         profile.setName(name);
         
         profile = client.addProfile(profile);
         
         return profile;
     }
     
     
     private Profile findProfile(String name) throws Exception{
         try{
             return client.getProfileWithName(name);
         }catch(Exception e){
             writeOutput("Unable To Locate Profile With Name " + name);
         }
         
         return null;
     }
 
     private ContinuumXmlRpcClient getClient() throws MalformedURLException {
       URL url = getUrl();
       this.writeOutput("Using Continuum At " + url.toString() + "\n");
       return continuumXmlRpcClientFactory.getClient(url, this.getField("username"), 
               this.getField("password"));
     }
     
     
     private ProjectGroup getProjectGroup(String projectGroupName) throws Exception{
         List<ProjectGroup> projectGroups = client.getAllProjectGroupsWithAllDetails();
         
         for(ProjectGroup projectGroup : projectGroups){
             if(projectGroup.getName().equals(projectGroupName) ||
                     projectGroup.getGroupId().equals(projectGroupName))
                 return projectGroup;
         }
         
         throw new Exception("Unable To Find Project Group " + projectGroupName);
     }
     
     
     private Project getProjectFromProjectGroup(String projectName, ProjectGroup projectGroup) throws Exception{
         
         return client.getProjectWithAllDetails(getProjectSummary(projectName, projectGroup).getId());        
     }
     
     
     private ProjectSummary getProjectSummary(String projectName, 
             ProjectGroup projectGroup) throws Exception{
         List<ProjectSummary> projects = projectGroup.getProjects();
         
         for(ProjectSummary project : projects){
             if(project.getName().equals(projectName))
                return project;
         }
         
         throw new Exception("Unable To Find Project " + projectName);
     }
     
     
     private BuildDefinition getBuildDefinitionFromId(int buildDefinitionId, String goals, String arguments, String buildFile, Project project, Profile profile) throws Exception {
         BuildDefinition buildDefinition = client.getBuildDefinition(buildDefinitionId);
         
         buildDefinition.setGoals(goals);
         buildDefinition.setArguments(arguments);
        buildDefinition.setBuildFile(buildFile);
         
         if(profile != null)
             buildDefinition.setProfile(profile);
         
         
         client.updateBuildDefinitionForProject(project.getId(), buildDefinition);
         
         return buildDefinition;
     }
     
     
     private BuildDefinition getBuildDefinitionFromProject(String goals, String arguments, String buildFile, Project project, Profile profile, Long taskId) throws Exception {
         List<BuildDefinition> buildDefinitions = project.getBuildDefinitions();
         String description = "Build Definition Generated By Maestro 4, task ID: " + taskId;
 
         BuildDefinition buildDefinition = null;
         
         for(BuildDefinition buildDef : buildDefinitions){
             if (description.equals(buildDef.getDescription())) {
                 if(profile == null) {
                     buildDefinition = buildDef;
                     break;
                 }
                 
                 if(buildDef.getProfile() != null && 
                         buildDef.getProfile().getName().equals(profile.getName())) {
                     buildDefinition = buildDef;
                     break;
                 }
             }
         }
         boolean update = false;
         if (buildDefinition == null) {
             this.writeOutput("Unable To Detect Build Definition Creation Will Begin\n");
             buildDefinition = new BuildDefinition();
         } else if(!goals.equals(buildDefinition.getGoals()) ||
                 !arguments.equals(buildDefinition.getArguments()) ||
                 !buildFile.equals(buildDefinition.getBuildFile())) {                    
             this.writeOutput("Build Definition Out of Date Update Will Begin\n");
             
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
         if(buildFile.contains("pom.xml")) {
           buildDefinition.setType("maven2");
         } else {
           buildDefinition.setType("shell");
         }
         
         
         if(profile != null){
             buildDefinition.setProfile(profile);
         }
         
         try {
             if (update) {
                 client.updateBuildDefinitionForProject(project.getId(), buildDefinition);
                 return buildDefinition;
             }
             return client.addBuildDefinitionToProject(project.getId(), buildDefinition);
             
         } catch (Exception ex) {
             throw new Exception("Unable To Add Build Definition " + ex.getMessage());
         }
     }
 
     private URL getUrl() throws MalformedURLException {
       URL url;
       String scheme = "http" + (Boolean.parseBoolean(this.getField("use_ssl")) ? "s" : "");
       url = new URL(scheme + "://"+this.getField("host")+":"+
               this.getField("port") + "/" + 
               this.getField("web_path").replaceAll("^\\/", "") + "/" +
               "xmlrpc");
       return url;
     }
 
   private Profile setupBuildAgent(Profile profile) throws Exception {
     try{
         writeOutput("Using Agent Facts To Locate Continuum Build Agent\n");
         profile = findProfile(getField("composition"));
         @SuppressWarnings("rawtypes")
         Map facts = (Map)(getFields().get("facts"));
         BuildAgentConfiguration buildAgent = this.getBuildAgent((String)facts.get("continuum_build_agent"));
         
         if(profile == null){
             writeOutput("Build Environment Not Found, Created New ("+getField("composition")+")\n");
             profile = this.createProfile(getField("composition"), this.createBuildAgentGroup(getField("composition"), buildAgent).getName());
         } else {
 //                        verify build agent is in group
             writeOutput("Build Environment Found, Verifying Agent\n");
             BuildAgentGroupConfiguration buildAgentGroupConfiguration = client.getBuildAgentGroup(profile.getBuildAgentGroup());
             boolean found = false;
             
             for(BuildAgentConfiguration ba : buildAgentGroupConfiguration.getBuildAgents()){
                 if(ba.getUrl().equals(buildAgent.getUrl())){
                     found = true;
                     break;
                 }
             }
             
             if(!found){
                 buildAgentGroupConfiguration.addBuildAgent(buildAgent);
                 client.updateBuildAgentGroup(buildAgentGroupConfiguration);
             }
         }
     }catch(Exception e){
         throw new Exception("Error Locating Continuum Build Agent Or Creating Build Environment" + e.getMessage());
     }
     return profile;
   }
   
   private BuildResult getBuildResult(int projectId) {
     
     BuildResult result = null;
     try {
       
       result = client.getLatestBuildResult(projectId);;
     } catch (Exception ex) {
     }
     
     return result;
   }
     
     private Project triggerBuild(Project project, BuildDefinition buildDefinition) throws Exception{
         
         BuildTrigger buildTrigger = new BuildTrigger();
         buildTrigger.setTrigger(ContinuumProjectState.TRIGGER_FORCED);
         buildTrigger.setTriggeredBy(this.getField("username"));
         try {
             client.buildProject(project.getId(), buildDefinition.getId(), buildTrigger);
         } catch (Exception ex) {
             throw new Exception("Failed To Trigger Build " + ex.getMessage());
         }
         
         int timeout = 60000;
         long start = System.currentTimeMillis();
 
         this.writeOutput("Waiting For Build To Start\n");
 
         while(project.getState() != ContinuumProjectState.BUILDING){
             if(System.currentTimeMillis() - start >  timeout){
               BuildResult result = getBuildResult(project.getId());
               if(result == null){
                 throw new TimeoutException("Failed To Detect Build Start After " + (timeout/1000) + " Seconds");
               }else{
                 if(result.getState() == ContinuumProjectState.FAILED ||
                         result.getState() == ContinuumProjectState.ERROR) {
                   throw new Exception(result.getError());
                 }
               }
             }
             Thread.sleep(1000);
 
             project = client.getProjectWithAllDetails(project.getId());
         }
         
 
         return project;
     }
      
     private Schedule getMaestroSchedule() throws Exception {
       List<Schedule> schedules = client.getSchedules();
       for(Schedule schedule : schedules){
         if(schedule.getName().equals("MAESTRO_SCHEDULE")){
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
       schedule.setName("MAESTRO_SCHEDULE");
       
       return client.addSchedule(schedule);
             
     }
     
     private void waitForBuild(Project project) throws Exception {
         project = client.getProjectWithAllDetails( project.getId() );
         String output = "";
         String runningTotal = "";
         while( project.getState() != ContinuumProjectState.OK &&
                project.getState() != ContinuumProjectState.FAILED &&
                project.getState() != ContinuumProjectState.ERROR &&
                project.getState() != ContinuumProjectState.NEW){
             switch(project.getState()) {
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
             }
             
             Thread.sleep(5000);
             project = client.getProjectWithAllDetails( project.getId() );
         }
         
         project = client.getProjectWithAllDetails( project.getId() );
         
         String newOutput = client.getBuildOutput(project.getId(), project.getLatestBuildId());
         
         output = newOutput.replace(runningTotal, "");
         
         writeOutput(output);
 
         BuildResult result = getBuildResult(project.getId());
         if(result.getExitCode() != 0) {
           throw new Exception(result.getError());
         }
     }
     
     @SuppressWarnings({ "rawtypes", "unchecked" })
     public void build() {
         try{
             client = getClient();
             Long projectId = null;
             Project project = null;
             if (getFields().get("__context_outputs__") != null &&
                     (projectId = (Long)((JSONObject)this.getFields().get("__context_outputs__")).get("continuum_project_id")) != null) {
                 project = client.getProjectWithAllDetails(projectId.intValue());
             } else {
                 String projectGroupName = this.getField("group_name");
                 this.writeOutput("Searching For Project Group " + projectGroupName + "\n");
                 ProjectGroup projectGroup = this.getProjectGroup(projectGroupName);
                 this.writeOutput("Found Project Group " + projectGroup.getName()+ "\n");
             
                 String projectName = this.getField("project_name");
                 this.writeOutput("Searching For Project " + projectName+ "\n");
                 project = this.getProjectFromProjectGroup(projectName, projectGroup);
             }
             this.writeOutput("Found Project " + project.getName()+ "\n");
             
             String goals = this.getField("goals");
             if(goals == null)
               goals = "";
             String arguments = this.getField("arguments");
             if(arguments == null)
               arguments = "";
             String buildFile = this.getField("build_file");
             if(buildFile == null)
               buildFile = "";
 
             Profile profile = null;
             if(((Map)getFields().get("facts")).get("continuum_build_agent") != null){
               profile = setupBuildAgent(profile);
             }
             JSONObject params = (JSONObject)this.getFields().get("params");
             Long taskId = (Long)params.get("composition_task_id");
             if (taskId == null) {
                 this.setError("Task Is is missing");
             }
             this.writeOutput("Searching For Build Definition for task ID " + taskId+ "\n");
             this.writeOutput("And Arguements " + arguments+ "\n");
 
            
             
             BuildDefinition buildDefinition = null;
             if(this.getFields().get("__previous_context_outputs__") != null &&
                     ((JSONObject)this.getFields().get("__previous_context_outputs__")).get("build_definition_id") != null){
                 try{
                     buildDefinition = this.getBuildDefinitionFromId(Integer.parseInt(((JSONObject)this.getFields().get("__previous_context_outputs__")).get("build_definition_id").toString()),goals, arguments, buildFile, project, profile);
                 } catch(Exception w){
                     buildDefinition = this.getBuildDefinitionFromProject(goals, arguments, buildFile, project, profile, taskId);
                 }
                 
             }
             if(buildDefinition == null){
                 buildDefinition = this.getBuildDefinitionFromProject(goals, arguments, buildFile, project, profile, taskId);
             }
             
             this.writeOutput("Retrieved Build Definition " + buildDefinition.getId()+ "\n");
             
             this.writeOutput("Triggering Build " + goals + "\n");
             project = triggerBuild(project, buildDefinition);
             this.writeOutput("The Build Has Started\n");
             
             waitForBuild(project);
             
             JSONObject outputData = (JSONObject)getFields().get("__context_outputs__");
             if(outputData == null)
                 outputData = new JSONObject();
             outputData.put("build_definition_id", buildDefinition.getId());
             outputData.put("build_id", project.getLatestBuildId());      
             
             getFields().put("__context_outputs__", outputData);
             
             BuildResult buildResult = getBuildResult(project.getId());
             if(buildResult != null){
               addLink("Continuum Build " + project.getBuildNumber(), buildResult.getBuildUrl());
             }
         }catch(Exception e){
             e.printStackTrace();
             this.setError("Continuum Build Failed: " + e.getMessage());   
         }
     }
 
   
     @SuppressWarnings("unchecked")
     public void addMavenProject() {
       try {
         client = this.getClient();
         ProjectGroup projectGroup = null;        
         if(getField("group_name") != null && getField("group_name") != "") {
           try{
             writeOutput("Requesting Group " + getField("group_name") + " From Continuum\n");
             projectGroup = getProjectGroup(getField("group_name"));
             writeOutput("Found Group " + getField("group_name") + " In Continuum\n");
           } catch (Exception e) {
             writeOutput("Creating " + getField("group_name") + " In Continuum\n");
             projectGroup = createProjectGroup();
             writeOutput("Created " + getField("group_name") + " In Continuum\n");
           }
         }        
         writeOutput("Processing Project In Continuum\n");
         ProjectSummary projectSummary;
         try{
             if (projectGroup != null)
                 projectSummary = createMavenProject(projectGroup.getId());
             else
                 projectSummary = createMavenProject();
             if (projectSummary.getId() == 0) {
                 
                 ProjectGroup group = getProjectGroup(projectSummary.getName());
                 projectSummary = getProjectSummary(projectSummary.getName(), group);
                 writeOutput("Found Existing Project (" + projectSummary.getId() + ")\n");
             } else {
                 writeOutput("Project Created (" + projectSummary.getId() + ")\n");
             }
             
             JSONObject outputData = (JSONObject)this.getFields().get("__context_outputs__");
             if(outputData == null)
                 outputData = new JSONObject();
             outputData.put("continuum_project_id", projectSummary.getId());
             this.setField("continuum_project_id", projectSummary.getId());            
             this.setField("__context_outputs__", outputData);
         } catch(Exception e){
           if(e.getMessage() != null && !e.getMessage().contains("Trying to add duplicate projects in the same project group")) {
             throw new Exception(e);
           }
           writeOutput("Project Detected\n");
         }
         
         
         writeOutput("Successfully Processed Maven Project Project\n");
       } catch (Exception e) {
         e.printStackTrace();
         this.setError("Continuum Build Failed: " + e.getMessage());   
       }
     }
 
     @SuppressWarnings("unchecked")
     public void addShellProject() {
       try {
         client = this.getClient();
         ProjectGroup projectGroup = null;
         try{
           writeOutput("Requesting Group " + getField("group_name") + " From Continuum\n");
           projectGroup = getProjectGroup(getField("group_name"));
           writeOutput("Found Group " + getField("group_name") + " In Continuum\n");
         } catch (Exception e) {
           writeOutput("Creating " + getField("group_name") + " In Continuum\n");
           projectGroup = createProjectGroup();
           writeOutput("Created " + getField("group_name") + " In Continuum\n");
         }
         ProjectSummary projectSummary;
         try{
           writeOutput("Requesting Project " + getField("project_name") + " In Continuum\n");
           projectSummary = getProjectFromProjectGroup(getField("project_name"), projectGroup);
           writeOutput("Found Project " + getField("project_name") + " In Continuum\n");
         }catch(Exception e) {
           writeOutput("Creating " + getField("project_name") + " In Continuum\n");
           projectSummary = createShellProject(projectGroup.getId());
           writeOutput("Created " + getField("project_name") + " In Continuum\n");
         }
         
         writeOutput("Successfully Processed Shell Project " + getField("project_name") + "\n");
         JSONObject outputData = (JSONObject)this.getFields().get("__context_outputs__");
         if(outputData == null)
             outputData = new JSONObject();
         outputData.put("continuum_project_id", projectSummary.getId());
         this.setField("continuum_project_id", projectSummary.getId());            
         this.setField("__context_outputs__", outputData);
 
       } catch (Exception e) {
         this.setError("Continuum Build Failed: " + e.getMessage());   
       }
     }
     
     private ProjectGroup createProjectGroup() throws Exception {
       ProjectGroupSummary projectGroup = new ProjectGroupSummary();
       projectGroup.setDescription(getField("group_description"));
       projectGroup.setGroupId(getField("group_id"));
       projectGroup.setName(getField("group_name"));
       client.addProjectGroup(projectGroup);
       
       return getProjectGroup(getField("group_name"));
     }
 
     private ProjectSummary createShellProject(int projectGroupId) throws Exception {
       ProjectSummary project = new ProjectSummary();
       project.setName(getField("project_name"));
       project.setDescription(getField("project_description"));
       project.setVersion(getField("project_version"));
       project.setScmUrl(getField("scm_url"));
       project.setScmUsername(getField("scm_username"));
       project.setScmPassword(getField("scm_password"));
       project.setScmUseCache(Boolean.parseBoolean(getField("scm_use_cache")));
       project.setScmTag(getField("scm_branch"));
       
       
       
       project = client.addShellProject(project, projectGroupId);
       
       if(project == null) {
         throw new Exception("Unable To Create Project In " + getField("group_name"));
       }
       return project;
     }
     
     private ProjectSummary createMavenProject(int projectGroupId) throws Exception { 
       AddingResult result;
       if(Boolean.parseBoolean(getField("single_directory"))){
        result = client.addMavenTwoProjectAsSingleProject(getField("pom_url"), projectGroupId);
       }else{
         result = client.addMavenTwoProject(getField("pom_url"), projectGroupId);
       }
       if(result.hasErrors() && (result.getProjects() == null || result.getProjects().isEmpty())) {
           throw new Exception(result.getErrorsAsString());
       } 
         
       ProjectSummary project = result.getProjects().get(0);
       if( project == null) {
         throw new Exception("Unable To Create Project In " + getField("group_name"));
       }
       return project;
     }
     
     private ProjectSummary createMavenProject() throws Exception { 
         AddingResult result;
         
         result = client.addMavenTwoProject(getField("pom_url"));
 //        if(result.hasErrors() && (result.getProjects() == null || result.getProjects().isEmpty())) {
 //          throw new Exception(result.getErrorsAsString());
 //        } 
         
         ProjectSummary project = result.getProjects().get(0);
         if( project == null) {
           throw new Exception("Unable To Create Project In " + getField("group_name"));
         }
         return project;
       }
 }
