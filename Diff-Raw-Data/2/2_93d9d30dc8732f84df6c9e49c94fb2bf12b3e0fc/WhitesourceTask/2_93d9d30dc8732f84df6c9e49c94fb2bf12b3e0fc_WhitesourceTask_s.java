 package org.whitesource.ant;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Vector;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.Path;
 import org.apache.tools.ant.util.ProxySetup;
 import org.whitesource.agent.api.ChecksumUtils;
 import org.whitesource.agent.api.dispatch.CheckPoliciesResult;
 import org.whitesource.agent.api.dispatch.UpdateInventoryResult;
 import org.whitesource.agent.api.model.AgentProjectInfo;
 import org.whitesource.agent.api.model.Coordinates;
 import org.whitesource.agent.api.model.DependencyInfo;
 import org.whitesource.api.client.WhitesourceService;
 import org.whitesource.api.client.WssServiceException;
 
 /**
  * Sends an inventory update request to White Source.
  * 
  * @author tom.shapira
  *
  */
 public class WhitesourceTask extends Task {
 
 	/* --- Property members --- */
 
 	/**
 	 * Unique identifier of the organization with White Source.
 	 */
 	private String apiKey;
 
 	/**
 	 * Modules to update.
 	 */
 	private Vector<Module> modules;
 	
 	/**
 	 * Whether or not to stop the build when encountering an error.
 	 */
 	private boolean failOnError;
 
 	/**
 	 * Check policies nested element.
 	 */
 	private Vector<CheckPolicies> checkPolicies;
 
 	/**
 	 * Url to send requests (debug proposes).
 	 */
 	private String wssUrl;
 
 	/* --- Members --- */
 
 	private boolean shouldCheckPolicies;
 
 	private CheckPolicies policyCheck;
 
 	private Collection<AgentProjectInfo> projectInfos;
 
 	private WhitesourceService service;
 
 	/* --- Overridden Ant Task methods --- */
 
 	@Override
 	public void init() throws BuildException {
 		super.init();
 
 		modules = new Vector<Module>();
 		checkPolicies = new Vector<CheckPolicies>();
 		failOnError = true;
 		shouldCheckPolicies = false;
 		projectInfos = new ArrayList<AgentProjectInfo>();
 	}
 
 	@Override
 	public void execute() throws BuildException {
 		validate();
 		
 		log("Updating White Source");
 		
 		scanModules();
 
 		createWhitesourceService();
 
 		updateInventory();
 	}
 
 	/* --- Private methods --- */
 
 	private void scanModules() {
 		log("Collecting OSS usage information");
 		
 		for (Module module : modules) {
 			// create project info
 			AgentProjectInfo projectInfo = new AgentProjectInfo();
 			if (StringUtils.isBlank(module.getName())) {
 				projectInfo.setProjectToken(module.getToken());
 				log("Processing module with token " + module.getToken());
 			} else {
 				// set project name as maven artifactId coordinate
 				projectInfo.setCoordinates(new Coordinates(null, module.getName(), null));
 				log("Processing " + module.getName());
 			}
 			
 			// get all files located in module paths
 			/**
 			 * We're using a set in order to avoid chance of duplicate files.
 			 */
 			Set<File> fileToUpdate = new HashSet<File>();
 			for (Path path : module.getPaths()) {
 				for (String includedFile : path.list()) {
 					fileToUpdate.add(new File(includedFile));
 				}
 			}
 			
 			// calculate SHA-1 for all files
 			for (File file : fileToUpdate) {
 				try {
 					String sha1 = ChecksumUtils.calculateSHA1(file);
 					projectInfo.getDependencies().add(new DependencyInfo(sha1));
 				} catch (IOException e) {
 					log("Problem calculating SHA-1 for '" + file.getName() + "'", e, Project.MSG_DEBUG);
 				}
 			}
 			
 			projectInfos.add(projectInfo);
 			log("Found " + projectInfo.getDependencies().size() + " direct dependencies");
 		}
 	}
 
 	private void createWhitesourceService() {
 		service = new WhitesourceService(Constants.AGENT_TYPE, Constants.AGENT_VERSION, wssUrl);
 
 		// set proxy information
         Properties systemProperties = System.getProperties();
         String proxyHost = systemProperties.getProperty(ProxySetup.HTTP_PROXY_HOST);
         String proxyPort = systemProperties.getProperty(ProxySetup.HTTP_PROXY_PORT);
         String proxyUsername = systemProperties.getProperty(ProxySetup.HTTP_PROXY_USERNAME); // optional
         String proxyPassword = systemProperties.getProperty(ProxySetup.HTTP_PROXY_PASSWORD); // optional
         
         // check if proxy is enabled
         if (!StringUtils.isBlank(proxyHost) &&
         		!StringUtils.isBlank(proxyPort)) {
         	service.getClient().setProxy(
         			proxyHost,
         			Integer.parseInt(proxyPort),
         			proxyUsername,
         			proxyPassword);
         }
 	}
 
 	private void updateInventory() {
 		// check policies
 		if (shouldCheckPolicies) {
 			try {
 				log("Checking policies");
 				CheckPoliciesResult result = service.checkPolicies(apiKey, projectInfos);
 				
 				// generate report
 				ReportGenerator reportGenerator = new ReportGenerator();
 				reportGenerator.generatePolicyRejectionsReport(result, policyCheck.getReportdir());
 				log("Policies report generated successfully", Project.MSG_INFO);
 				
 				if (result.hasRejections()) {
 					String rejectionsErrorMessage = "Some dependencies did not conform with open source policies, review report for details";
 					if (policyCheck.isFailonrejection()) {
 						throw new BuildException(rejectionsErrorMessage);
 					} else {
 						log(rejectionsErrorMessage);
 					}
 				} else {
 					log("All dependencies conform with open source policies");
 				}
 			} catch (WssServiceException e) {
 				error("A problem occurred while checking policies: " + e.getMessage());
 			} catch (IOException e) {
 				error("Error generating policies report");
 			}
 		}
 
 		// update inventory
 		try {
 			log("Sending to White Source");
 			UpdateInventoryResult result = service.update(apiKey, projectInfos);
 			
 			log("White Source update results:");
 			log("White Source organization: " + result.getOrganization());
 
 			// newly created projects
 			Collection<String> createdProjects = result.getCreatedProjects();
 			if (createdProjects.isEmpty()) {
 				log("No new projects found");
 			} else {
 				log(createdProjects.size() + " Newly created projects:");
 				for (String projectName : createdProjects) {
 					log(projectName);
 				}
 			}
 
 			// updated projects
 			Collection<String> updatedProjects = result.getUpdatedProjects();
 			if (updatedProjects.isEmpty()) {
 				log("No projects were updated");
 			} else {
 				log(updatedProjects.size() + " existing projects were updated:");
 				for (String projectName : updatedProjects) {
 					log(projectName);
 				}
 			}
 		} catch (WssServiceException e) {
 			error("A problem occurred while updating projects: " + e.getMessage());
 		}
 	}
 
 	private void validate() {
 		// api key
 		if (StringUtils.isBlank(apiKey)) {
 			error("Missing API Key");
 		}
 
 		// modules
 		if (modules.size() < 1) {
 			error("No modules set");
 		} else {
 			for (Module project : modules) {
 				// project name / token
 				if (StringUtils.isBlank(project.getName()) && 
 						StringUtils.isBlank(project.getToken())) {
 					error("Module name / token not set");
 				}
 				
 				// paths
 				if (project.getPaths().size() < 1) {
 					error("Path not set");
 				}
 			}
 		}
 
 		// check policies
 		if (!checkPolicies.isEmpty()) {
 			shouldCheckPolicies = true;
 			policyCheck = checkPolicies.iterator().next();
 
 			if (checkPolicies.size() > 1) {
 				error("There should be only one check policies element");
 			} else {
 				File reportdir = policyCheck.getReportdir();
 				if (reportdir == null || !reportdir.exists()) {
 					error("Missing output directory for policies report");
 				}
 			}
 		}
 	}
 
 	private void error(String errorMsg) {
 		if (failOnError) {
 			throw new BuildException(errorMsg);
 		} else {
 			log(errorMsg, Project.MSG_ERR);
 		}
 	}
 
 	/* --- Property set methods --- */
 
	public void setFilaonerror(boolean failonerror) {
 		this.failOnError = failonerror;
 	}
 
 	public void addModule(Module module) {
 		this.modules.add(module);
 	}
 	
 	public void setApikey(String apikey) {
 		this.apiKey = apikey;
 	}
 
 	public void addCheckpolicies(CheckPolicies checkpolicies) {
 		this.checkPolicies.add(checkpolicies);
 	}
 
 	public void setWssurl(String wssurl) {
 		this.wssUrl = wssurl;
 	}
 
 }
