 /**
  * Phresco Service Implemenation
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.service.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.ModuleInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.commons.model.RequiredOption;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.logger.SplunkLogger;
 import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.service.api.ArchetypeExecutor;
 import com.photon.phresco.service.api.DbManager;
 import com.photon.phresco.service.api.PhrescoServerFactory;
 import com.photon.phresco.service.util.ServerConstants;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.ProjectUtils;
 import com.photon.phresco.util.ServiceConstants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 public class ArchetypeExecutorImpl implements ArchetypeExecutor,
 		ServerConstants, Constants, ServiceConstants {
 	private static final SplunkLogger LOGGER = SplunkLogger
 			.getSplunkLogger(ArchetypeExecutorImpl.class.getName());
 	private static Boolean isDebugEnabled = LOGGER.isDebugEnabled();
 	private static final String INTERACTIVE_MODE = "false";
 	public static final String WINDOWS = "Windows";
 	private DbManager dbManager = null;
 
 	public ArchetypeExecutorImpl()
 			throws PhrescoException {
 		PhrescoServerFactory.initialize();
 		dbManager = PhrescoServerFactory.getDbManager();
 	}
 
 	public void execute(ProjectInfo projectInfo, String tempFolderPath)
 		throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("ArchetypeExecutorImpl.execute:Entry");
 			if (projectInfo == null) {
 				LOGGER.warn("ArchetypeExecutorImpl.execute",
 						ServiceConstants.STATUS_BAD_REQUEST,
 						"message=\"ProjectInfo is empty\"");
 				throw new PhrescoException("ProjectInfo is empty");
 			}
 			LOGGER.info("ArchetypeExecutorImpl.execute", "customerId=\""
 					+ projectInfo.getCustomerIds().get(0) + "\"",
 					"creationDate=\"" + projectInfo.getCreationDate() + "\"",
 					"projectCode=\"" + projectInfo.getProjectCode() + "\"");
 		}
 		try {
 			String commandString = "";
 			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 			String customerId = projectInfo.getCustomerIds().get(0);
 			RepoInfo repoInfo = dbManager.getRepoInfo(customerId);
 			String techId = applicationInfo.getTechInfo().getId();
 			ArtifactGroup archetypeInfo = dbManager.getArchetypeInfo(techId,
 					customerId);
 			ArtifactInfo artifactInfo = archetypeInfo.getVersions().get(0);
 			String version = artifactInfo.getVersion();
 			String groupId = projectInfo.getGroupId();
 			if(projectInfo.isMultiModule()) {
 				tempFolderPath = tempFolderPath + "/" + applicationInfo.getCode();
 		//		commandString = buildCommandString(applicationInfo.getCode(), techId, 
 		//				archetypeInfo.getGroupId(), archetypeInfo.getArtifactId(), version, repoInfo.getReleaseRepoURL(), version, customerId);
 		//		executeCreateCommand(tempFolderPath, commandString, customerId, projectInfo);
 				if(CollectionUtils.isNotEmpty(applicationInfo.getModules())) {
 					for (ModuleInfo moduleInfo : applicationInfo.getModules()) {
 						archetypeInfo = dbManager.getArchetypeInfo(moduleInfo.getTechInfo().getId(), customerId);
 						version = archetypeInfo.getVersions().get(0).getVersion();
 						commandString = buildCommandString(moduleInfo.getCode(), techId, archetypeInfo.getGroupId(), 
 								archetypeInfo.getArtifactId(), version, repoInfo.getReleaseRepoURL(), projectInfo.getVersion(), customerId, groupId);
 						executeCreateCommand(tempFolderPath, commandString, customerId, projectInfo);
 						updateDefaultFeatures(projectInfo, tempFolderPath, customerId, moduleInfo.getCode(), moduleInfo);
 						updateRepository(customerId, applicationInfo, new File(
 								tempFolderPath), moduleInfo.getCode());
 					}
 				}
 			} else {
 				commandString = buildCommandString(applicationInfo.getCode(), techId, archetypeInfo.getGroupId(), 
 						archetypeInfo.getArtifactId(), version,	repoInfo.getReleaseRepoURL(), projectInfo.getVersion(), customerId, groupId);
 				executeCreateCommand(tempFolderPath, commandString, customerId, projectInfo);
 				updateDefaultFeatures(projectInfo, tempFolderPath, customerId, applicationInfo.getCode(), null);
 				updateRepository(customerId, applicationInfo, new File(
 						tempFolderPath), applicationInfo.getCode());
 			}
 			if (isDebugEnabled) {
 				LOGGER.debug("command=" + commandString);
 			}
 			if (isDebugEnabled) { 
 				LOGGER.debug("ArchetypeExecutorImpl.execute:Exit");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (isDebugEnabled) {
 				LOGGER.error("ArchetypeExecutorImpl.execute",
 						"status=\"Failure\"",
 						"message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void executeCreateCommand(String tempFolderPath, String command, String customerId, ProjectInfo info) throws PhrescoException {
 		File file = new File(tempFolderPath);
 		if (!file.exists()) {
 			file.mkdirs();
 		}
 		BufferedReader bufferedReader = Utility.executeCommand(
 				command, tempFolderPath);
 		String line = null;
 		try {
 			while ((line = bufferedReader.readLine()) != null) {
 				if (isDebugEnabled) {
 					LOGGER.debug(line);
 				}
 			}
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void updateRepository(String customerId, ApplicationInfo appInfo,
 			File tempFolderPath, String modName) throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("ArchetypeExecutorImpl.updateRepository:Entry");
 			if (StringUtils.isEmpty(customerId)) {
 				LOGGER.warn(ServiceConstants.ARCHETYPE_EXE_IMPL_UPDATE_REPO,
 						ServiceConstants.STATUS_BAD_REQUEST,
 						"message=\"customerId is empty\"");
 				throw new PhrescoException("Customer Id is Empty");
 			}
 			if (appInfo == null) {
 				LOGGER.warn(ServiceConstants.ARCHETYPE_EXE_IMPL_UPDATE_REPO,
 						ServiceConstants.STATUS_BAD_REQUEST,
 						"message=\"applicationInfo is empty\"");
 				throw new PhrescoException("ApplicationInfo is Empty");
 			}
 			LOGGER.info(ServiceConstants.ARCHETYPE_EXE_IMPL_UPDATE_REPO,
 					"customerId=\"" + customerId + "\"");
 		}
 		RepoInfo repoInfo = dbManager.getRepoInfo(customerId);
 		StringBuilder builder = new StringBuilder(tempFolderPath.getPath());
 		builder.append(File.separator);
 		if(StringUtils.isNotEmpty(modName)) {
 			builder.append(modName);
 		} else {
 			builder.append(appInfo.getAppDirName());
 		}
 		builder.append(File.separator);
 		builder.append(getPhrescoPomFile(tempFolderPath, appInfo).getName());
 		File pomFile = new File(builder.toString());
 		try {
 			PomProcessor processor = new PomProcessor(pomFile);
 			processor.setName(appInfo.getName());
 			processor.addRepositories(customerId, repoInfo.getGroupRepoURL());
 			processor.save();
 			if (isDebugEnabled) {
 				LOGGER.debug("ArchetypeExecutorImpl.updateRepository:Exit");
 			}
 		} catch (PhrescoPomException e) {
 			LOGGER.error(ServiceConstants.ARCHETYPE_EXE_IMPL_UPDATE_REPO,
 					"status=\"Failure\"",
 					"message=\"" + e.getLocalizedMessage() + "\"");
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private File getPhrescoPomFile(File tempPath, ApplicationInfo appInfo) {
 		File appDir = new File(tempPath, appInfo.getAppDirName());
 		File file = new File(appDir, "phresco-pom.xml");
 		if(file.exists()) {
 			return file;
 		}
 		return new File(appDir, "pom.xml");
 	}
 	
 	private void updateDefaultFeatures(ProjectInfo projectInfo,String tempFolderPath,
 			String customerId, String modName, ModuleInfo moduleInfo) throws PhrescoException, PhrescoPomException {
 		List<String> selectedFeatures = new ArrayList<String>();
 		List<String> selectedJsLibs = new ArrayList<String>();
 		List<String> selectedComponentids = new ArrayList<String>();
 		List<ArtifactGroup> listArtifactGroup = new ArrayList<ArtifactGroup>();
 		ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
 		
 		//To add default feature
 		List<ArtifactGroup> modulesList = dbManager.findDefaultFeatures(appInfo.getTechInfo().getId(), "FEATURE", customerId);
 		if(CollectionUtils.isNotEmpty(modulesList)) {
 		for (ArtifactGroup artifactGroup : modulesList) {
 			List<ArtifactInfo> versions = artifactGroup.getVersions();
 			for (ArtifactInfo artifactInfo : versions) {
 				List<RequiredOption> appliesTo = artifactInfo.getAppliesTo();
 				if(CollectionUtils.isNotEmpty(appliesTo)) {
 					for (RequiredOption requiredOption : appliesTo) {
 						if (requiredOption.isRequired() && requiredOption.getTechId().equals(appInfo.getTechInfo().getId())) {
 							ArtifactGroup selectedartifactGroup = dbManager.getArtifactGroup(artifactInfo.getArtifactGroupId());
 							listArtifactGroup.add(selectedartifactGroup);
 							selectedFeatures.add(artifactInfo.getId());
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 
 		//To add default javascript
 		List<ArtifactGroup> jsLibsList = dbManager.findDefaultFeatures(appInfo.getTechInfo().getId(), "JAVASCRIPT", customerId);
 		if(CollectionUtils.isNotEmpty(jsLibsList)) {
 		for (ArtifactGroup artifactGroup : jsLibsList) {
 			List<ArtifactInfo> versions = artifactGroup.getVersions();
 			for (ArtifactInfo artifactInfo : versions) {
 				List<RequiredOption> appliesTo = artifactInfo.getAppliesTo();
 				if(CollectionUtils.isNotEmpty(appliesTo)) {
 					for (RequiredOption requiredOption : appliesTo) {
 						if (requiredOption.isRequired() && requiredOption.getTechId().equals(appInfo.getTechInfo().getId())) {
 							ArtifactGroup selectedartifactGroup = dbManager.getArtifactGroup(artifactInfo.getArtifactGroupId());
 							listArtifactGroup.add(selectedartifactGroup);
 							selectedJsLibs.add(artifactInfo.getId());
 							break;
 						}
 					}
 				}
 			}
 		}
 	}	
 		
 		//To add default components
 		List<ArtifactGroup> componentsList = dbManager.findDefaultFeatures(appInfo.getTechInfo().getId(), "COMPONENT", customerId);
 		for (ArtifactGroup artifactGroup : componentsList) {
 			List<ArtifactInfo> versions = artifactGroup.getVersions();
 			for (ArtifactInfo artifactInfo : versions) {
 				List<RequiredOption> appliesTo = artifactInfo.getAppliesTo();
 				if(CollectionUtils.isNotEmpty(appliesTo)) {
 					for (RequiredOption requiredOption : appliesTo) {
 						if (requiredOption.isRequired() && requiredOption.getTechId().equals(appInfo.getTechInfo().getId())) {
 							ArtifactGroup selectedartifactGroup = dbManager.getArtifactGroup(artifactInfo.getArtifactGroupId());
 							listArtifactGroup.add(selectedartifactGroup);
 							selectedComponentids.add(artifactInfo.getId());
 							break;
 						}
 					}
 				}
 			}
 		}
 		
 		//To  write default prebuilt modules to its default location
 		List<String> selectedModules = appInfo.getSelectedModules();
 		pilotDefaultFeaturesToAdd(appInfo, listArtifactGroup, selectedModules);
 
 		//To  write default prebuilt jslibraries to its default location
 		List<String> selectedJSLibs = appInfo.getSelectedJSLibs();
 		pilotDefaultFeaturesToAdd(appInfo, listArtifactGroup, selectedJSLibs);
 		
 		//To  write default prebuilt components to its default location
 		List<String> selectedComponents = appInfo.getSelectedComponents();
 		pilotDefaultFeaturesToAdd(appInfo, listArtifactGroup, selectedComponents);
 		
 		Gson gson = new Gson();
 		if(CollectionUtils.isNotEmpty(listArtifactGroup)) {
             ProjectUtils projectUtils = new ProjectUtils();
             File rootPomFile = getPhrescoPomFile(new File(tempFolderPath), appInfo);
             PomProcessor pomprocessor = new PomProcessor(rootPomFile);
             String sourceDir = pomprocessor.getProperty("phresco.source.dir");
             StringBuilder appPath = new StringBuilder(tempFolderPath).append(File.separator);
             if(StringUtils.isNotEmpty(modName)) {
 	       		 appPath.append(modName);
 	       		 appPath.append(File.separator);
             }
              if (StringUtils.isNotEmpty(sourceDir)) {
         	     File sourcePomFile = new File(appPath.toString() + 
         	    		 sourceDir + File.separator + "phresco-pom.xml");
                 if (sourcePomFile.exists()) {
                 	projectUtils.updatePOMWithPluginArtifact(sourcePomFile, listArtifactGroup);
                  } else {
                 	 sourcePomFile = new File(appPath.toString() + 
             	    		 sourceDir + File.separator + "pom.xml"); 
                 	 projectUtils.updatePOMWithPluginArtifact(sourcePomFile, listArtifactGroup);
                  }
 			} else {
 				File pomFile = new File(appPath.toString(), "pom.xml");
 				File phrescoPomFile = new File(appPath.toString(), "phresco-pom.xml");
 				List<ArtifactGroup> dependencies = new ArrayList<ArtifactGroup>();
 				List<ArtifactGroup> artifacts = new ArrayList<ArtifactGroup>();
 				for (ArtifactGroup artifactGroup : listArtifactGroup) {
 					if (artifactGroup.getPackaging().equals("zip") || artifactGroup.getPackaging().equals("war")) {
 						artifacts.add(artifactGroup);
 					} else {
 						dependencies.add(artifactGroup);
 					}
 				}
 
 				if (CollectionUtils.isNotEmpty(dependencies)) {
 					projectUtils.updatePOMWithModules(pomFile, dependencies);
 				}
 
 				if (CollectionUtils.isNotEmpty(artifacts)) {
 					if (phrescoPomFile.exists()) {
 						projectUtils.updateToDependencyPlugin(phrescoPomFile, artifacts);
 					} else {
 						projectUtils.updateToDependencyPlugin(pomFile, artifacts);
 					}
 				}
 			}
 		}
 		StringBuilder sb = new StringBuilder(tempFolderPath).append(File.separator);
 		if(StringUtils.isNotEmpty(modName)) {
 			sb.append(modName).append(File.separator);
 		}
 		sb.append(Constants.DOT_PHRESCO_FOLDER).append(File.separator).append(
 				Constants.APPLICATION_HANDLER_INFO_FILE);
 		File filePath = new File(sb.toString());
 		MojoProcessor mojo = new MojoProcessor(filePath);
 		ApplicationHandler handler = mojo.getApplicationHandler();
 		// To write selected Features into phresco-application-Handler-info.xml
 		String artifactGroup = gson.toJson(listArtifactGroup);
 		handler.setSelectedFeatures(artifactGroup);
 		mojo.save();
 		List<String> appinfoModules = appInfo.getSelectedModules();
 		if(CollectionUtils.isNotEmpty(appinfoModules)) {
 			appinfoModules.addAll(selectedFeatures);
 			appInfo.setSelectedModules(appinfoModules);
 		} else {
 			appInfo.setSelectedModules(selectedFeatures);
 		}
 		
 		List<String> appinfoJSLibs = appInfo.getSelectedJSLibs();
 		if(CollectionUtils.isNotEmpty(appinfoJSLibs)) {
 		appinfoJSLibs.addAll(selectedJsLibs);
 		appInfo.setSelectedJSLibs(appinfoJSLibs);
 		} else {
 			appInfo.setSelectedJSLibs(selectedJsLibs);
 		}
 		
 		List<String> appinfoComponents = appInfo.getSelectedComponents();
 		if(CollectionUtils.isNotEmpty(appinfoComponents)) {
 			appinfoComponents.addAll(selectedComponentids);
 			appInfo.setSelectedComponents(appinfoComponents);
 		} else {
 			appInfo.setSelectedComponents(selectedComponentids);
 		}
 		File phrescoPomFile = new File(tempFolderPath + "/" + appInfo.getAppDirName(), "phresco-pom.xml");
 		if(phrescoPomFile.exists()) {
 			appInfo.setPhrescoPomFile("phresco-pom.xml");
 		}
 		projectInfo.setAppInfos(Collections.singletonList(appInfo));
 		StringBuilder sbuilder = new StringBuilder(tempFolderPath).append(File.separator);
 		if(StringUtils.isNotEmpty(modName)) {
 			sbuilder.append(modName).append(File.separator);
 		}
 		sbuilder.append(Constants.DOT_PHRESCO_FOLDER).append(File.separator).append(
 				Constants.PROJECT_INFO_FILE);
 		File projectInfoPath = new File(sbuilder.toString());
 		ProjectInfo clonedProjInfo = cloneProjInfo(projectInfo, appInfo, moduleInfo);
 		ProjectUtils.updateProjectInfo(clonedProjInfo, projectInfoPath);
 	}
 	
 	private ProjectInfo cloneProjInfo(ProjectInfo projectInfo, ApplicationInfo appInfo, ModuleInfo moduleInfo) throws PhrescoException {
 		try {
 			ProjectInfo newProjectInfo = new ProjectInfo();
 			newProjectInfo.setId(projectInfo.getId());
 			newProjectInfo.setName(projectInfo.getName());
 			newProjectInfo.setProjectCode(projectInfo.getProjectCode());
 			newProjectInfo.setVersion(projectInfo.getVersion());
 			newProjectInfo.setVersionInfo(projectInfo.getVersionInfo());
 			newProjectInfo.setNoOfApps(projectInfo.getNoOfApps());
 			newProjectInfo.setStartDate(projectInfo.getStartDate());
 			newProjectInfo.setEndDate(projectInfo.getEndDate());
 			newProjectInfo.setPreBuilt(projectInfo.isPreBuilt());
 			newProjectInfo.setMultiModule(projectInfo.isMultiModule());
 			newProjectInfo.setIntegrationTest(projectInfo.isIntegrationTest());
 			newProjectInfo.setGroupId(projectInfo.getGroupId());
 			ApplicationInfo mergedAppInfo = mergeSubModuleInfoToAppInfo(appInfo, moduleInfo);
 			newProjectInfo.setAppInfos(Collections.singletonList(mergedAppInfo));
 			newProjectInfo.setCustomerIds(projectInfo.getCustomerIds());
			newProjectInfo.setDisplayName(projectInfo.getDisplayName());
 			
 			return newProjectInfo;
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private ApplicationInfo mergeSubModuleInfoToAppInfo(ApplicationInfo appInfo, ModuleInfo moduleInfo) {
 		ApplicationInfo newAppInfo = new ApplicationInfo();
 		if (moduleInfo != null) {
 			newAppInfo.setCode(moduleInfo.getCode());
 			newAppInfo.setAppDirName(moduleInfo.getCode());
 			newAppInfo.setTechInfo(moduleInfo.getTechInfo());
 			newAppInfo.setVersion(appInfo.getVersion());
 			newAppInfo.setPhrescoPomFile(appInfo.getPhrescoPomFile());
 			newAppInfo.setPomFile(appInfo.getPomFile());
 			newAppInfo.setRootModule(moduleInfo.getRootModule());
 			newAppInfo.setModules(null);
 			newAppInfo.setId(moduleInfo.getId());
 			newAppInfo.setName(moduleInfo.getCode());
 		} else {
 			newAppInfo = appInfo;
 		}
 		
 		return newAppInfo;
 	}
 	
 	private void pilotDefaultFeaturesToAdd(ApplicationInfo appInfo,
 			List<ArtifactGroup> listArtifactGroup, List<String> selectedFeatures) throws PhrescoException {
 		if(CollectionUtils.isNotEmpty(selectedFeatures)) {
 			List<ArtifactGroup> findSelectedArtifacts = dbManager.findSelectedArtifacts(selectedFeatures);
 			if(CollectionUtils.isNotEmpty(findSelectedArtifacts)) {
 				listArtifactGroup.addAll(findSelectedArtifacts);
 			}
 		}
 	}
 
 	private String buildCommandString(String code, String techId, String archetypeGroupId, String artifactId, String version,
 			String repoUrl, String projectVersion, String customerId, String groupId) throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("ArchetypeExecutorImpl.buildCommandString:Entry");
 			LOGGER.debug("ArchetypeExecutorImpl.buildCommandString", "appCode="
 					+ "\"" + code + "\"");
 			if (StringUtils.isEmpty(customerId)) {
 				LOGGER.warn("ArchetypeExecutorImpl.buildCommandString",
 						ServiceConstants.STATUS_BAD_REQUEST,
 						"message=Customer Id Should Not Be Null");
 				throw new PhrescoException("Customer Id Should Not Be Null");
 			}
 		}
 		if (StringUtils.isEmpty(techId)) {
 			LOGGER.warn("ArchetypeExecutorImpl.buildCommandString",
 					ServiceConstants.STATUS_BAD_REQUEST, "message=\"techId is empty\"");
 			throw new PhrescoException("techId Should Not Be Null");
 		}
 		
 		StringBuffer commandStr = new StringBuffer();
 		commandStr.append(Constants.MVN_COMMAND)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(Constants.MVN_ARCHETYPE)
 				.append(STR_COLON)
 				.append(Constants.MVN_GOAL_GENERATE)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_ARCHETYPEGROUPID)
 				.append(Constants.STR_EQUALS)
 				.append(archetypeGroupId)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_ARCHETYPEARTIFACTID)
 				.append(Constants.STR_EQUALS)
 				.append(artifactId)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_ARCHETYPEVERSION)
 				.append(Constants.STR_EQUALS)
 				.append(version)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_GROUPID)
 				.append(Constants.STR_EQUALS)
 				.append(groupId)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_ARTIFACTID)
 				.append(Constants.STR_EQUALS)
 				.append(STR_DOUBLE_QUOTES)
 				.append(code)
 				.append(STR_DOUBLE_QUOTES)
 				// artifactId --> project name could have space in between
 				.append(Constants.STR_BLANK_SPACE).append(ARCHETYPE_VERSION)
 				.append(Constants.STR_EQUALS).append(projectVersion)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_ARCHETYPEREPOSITORYURL)
 				.append(Constants.STR_EQUALS)
 				.append(repoUrl)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_INTERACTIVEMODE).append(Constants.STR_EQUALS)
 				.append(INTERACTIVE_MODE);
 		if (isDebugEnabled) {
 			LOGGER.debug("ArchetypeExecutorImpl.buildCommandString:Exit");
 		}
 		return commandStr.toString();
 	}
 
 }
