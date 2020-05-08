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
 	private static final String PHRESCO_FOLDER_NAME = "phresco";
 	private static final String DOT_PHRESCO_FOLDER = "." + PHRESCO_FOLDER_NAME;
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
 			ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 			String customerId = projectInfo.getCustomerIds().get(0);
 			String commandString = buildCommandString(applicationInfo,
 					projectInfo.getVersion(), customerId);
 			if (isDebugEnabled) {
 				LOGGER.debug("command=" + commandString);
 			}
 			// the below implementation is required since a new command or shell
 			// is forked from the
 			// existing running web server command or shell instance
 			File file = new File(tempFolderPath);
 			if (!file.exists()) {
 				file.mkdirs();
 			}
 			BufferedReader bufferedReader = Utility.executeCommand(
 					commandString, tempFolderPath);
 			String line = null;
 			while ((line = bufferedReader.readLine()) != null) {
 				if (isDebugEnabled) {
 					LOGGER.debug(line);
 				}
 			}
 			updateRepository(customerId, applicationInfo, new File(
 					tempFolderPath));
 			updateDefaultFeatures(projectInfo, tempFolderPath, customerId);
 			if (isDebugEnabled) {
 				LOGGER.debug("ArchetypeExecutorImpl.execute:Exit");
 			}
 		} catch (IOException e) {
 			if (isDebugEnabled) {
 				LOGGER.error("ArchetypeExecutorImpl.execute",
 						"status=\"Failure\"",
 						"message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoException(e);
 		}
 	}
 
 	private void updateRepository(String customerId, ApplicationInfo appInfo,
 			File tempFolderPath) throws PhrescoException {
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
 		File pomFile = new File(tempFolderPath, appInfo.getAppDirName()
 				+ File.separatorChar + Utility.getPomFileName(appInfo));
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
 	
 	private void updateDefaultFeatures(ProjectInfo projectInfo,String tempFolderPath,String customerId) throws PhrescoException {
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
 						if (requiredOption.isRequired()) {
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
 						if (requiredOption.isRequired()) {
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
 						if (requiredOption.isRequired()) {
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
 			projectUtils .updatePOMWithPluginArtifact(getPomFile(tempFolderPath, appInfo), listArtifactGroup);
 		}
 		StringBuilder sb = new StringBuilder(tempFolderPath).append(File.separator).append(appInfo.getAppDirName())
 		.append(File.separator).append(Constants.DOT_PHRESCO_FOLDER).append(File.separator).append(
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
 		
 		projectInfo.setAppInfos(Collections.singletonList(appInfo));
 		StringBuilder sbuilder = new StringBuilder(tempFolderPath).append(File.separator).append(appInfo.getAppDirName())
 		.append(File.separator).append(Constants.DOT_PHRESCO_FOLDER).append(File.separator).append(
 				Constants.PROJECT_INFO_FILE);
 		File projectInfoPath = new File(sbuilder.toString());
 		projectInfo.setAppInfos(Collections.singletonList(appInfo));
 		ProjectUtils.updateProjectInfo(projectInfo, projectInfoPath);
 	}
 	
 	private File getPomFile(String tempPath, ApplicationInfo appInfo) {
 		String projectPath = tempPath + File.separator + appInfo.getAppDirName();
 		File file = new File(projectPath, "phresco-pom.xml");
 		if(file.exists()) {
 			appInfo.setPhrescoPomFile("phresco-pom.xml");
 			return file;
 		}
 		return new File(projectPath, "pom.xml");
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
 
 	private String buildCommandString(ApplicationInfo info,
 			String projectVersion, String customerId) throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("ArchetypeExecutorImpl.buildCommandString:Entry");
 			LOGGER.debug("ArchetypeExecutorImpl.buildCommandString", "appCode="
 					+ "\"" + info.getCode() + "\"");
 			if (StringUtils.isEmpty(customerId)) {
 				LOGGER.warn("ArchetypeExecutorImpl.buildCommandString",
 						ServiceConstants.STATUS_BAD_REQUEST,
 						"message=Customer Id Should Not Be Null");
 				throw new PhrescoException("Customer Id Should Not Be Null");
 			}
 		}
 		String techId = info.getTechInfo().getId();
 		if (StringUtils.isEmpty(techId)) {
 			LOGGER.warn("ArchetypeExecutorImpl.buildCommandString",
 					ServiceConstants.STATUS_BAD_REQUEST, "message=\"techId is empty\"");
 			throw new PhrescoException("techId Should Not Be Null");
 		}
 		ArtifactGroup archetypeInfo = dbManager.getArchetypeInfo(techId,
 				customerId);
 		// TODO to sort version
 		ArtifactInfo artifactInfo = archetypeInfo.getVersions().get(0);
 		String version = artifactInfo.getVersion();
 		RepoInfo repoInfo = dbManager.getRepoInfo(customerId);
 		
 		StringBuffer commandStr = new StringBuffer();
 		commandStr.append(Constants.MVN_COMMAND)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(Constants.MVN_ARCHETYPE)
 				.append(STR_COLON)
 				.append(Constants.MVN_GOAL_GENERATE)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_ARCHETYPEGROUPID)
 				.append(Constants.STR_EQUALS)
 				.append(archetypeInfo.getGroupId())
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_ARCHETYPEARTIFACTID)
 				.append(Constants.STR_EQUALS)
 				.append(archetypeInfo.getArtifactId())
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_ARCHETYPEVERSION)
 				.append(Constants.STR_EQUALS)
				.append(version)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_GROUPID)
 				.append(Constants.STR_EQUALS)
 				.append("com.photon.phresco")
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_ARTIFACTID)
 				.append(Constants.STR_EQUALS)
 				.append(STR_DOUBLE_QUOTES)
 				.append(info.getCode())
 				.append(STR_DOUBLE_QUOTES)
 				// artifactId --> project name could have space in between
 				.append(Constants.STR_BLANK_SPACE).append(ARCHETYPE_VERSION)
 				.append(Constants.STR_EQUALS).append(projectVersion)
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_ARCHETYPEREPOSITORYURL)
 				.append(Constants.STR_EQUALS)
 				.append(repoInfo.getGroupRepoURL())
 				.append(Constants.STR_BLANK_SPACE)
 				.append(ARCHETYPE_INTERACTIVEMODE).append(Constants.STR_EQUALS)
 				.append(INTERACTIVE_MODE);
 		if (isDebugEnabled) {
 			LOGGER.debug("ArchetypeExecutorImpl.buildCommandString:Exit");
 		}
 		return commandStr.toString();
 	}
 
 }
