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
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
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
 import com.phresco.pom.model.DeploymentRepository;
 import com.phresco.pom.model.DistributionManagement;
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
 				if(CollectionUtils.isNotEmpty(applicationInfo.getModules())) {
 					String rootFolderTempPath = tempFolderPath;
 					if(!applicationInfo.isParentArchrtypeCreated()) {
 						createMultiModuleObject(projectInfo, rootFolderTempPath,
 								applicationInfo, customerId, repoInfo, techId,
 								archetypeInfo, version, groupId);
 						writeDistributionTag(customerId, applicationInfo, projectInfo.getProjectCode(), repoInfo, 
								getPhrescoPomFile(applicationInfo, new File(tempFolderPath)));
 					}
 					commandString = "";
 					tempFolderPath = tempFolderPath + "/" + applicationInfo.getCode();
 					for (ModuleInfo moduleInfo : applicationInfo.getModules()) {
 						if (!moduleInfo.isModified()) {
 							archetypeInfo = dbManager.getArchetypeInfo(moduleInfo.getTechInfo().getId(), customerId);
 							version = archetypeInfo.getVersions().get(0).getVersion();
 							commandString = buildCommandString(moduleInfo.getCode(), techId, archetypeInfo.getGroupId(), 
 									archetypeInfo.getArtifactId(), version, repoInfo.getReleaseRepoURL(), projectInfo.getVersion(), customerId, groupId);
 							
 							executeCreateCommand(tempFolderPath, commandString, customerId, projectInfo);
 							updateDefaultFeatures(projectInfo, tempFolderPath, customerId, moduleInfo.getCode(), moduleInfo);
 							updateRepository(customerId, applicationInfo, new File(
 									tempFolderPath), moduleInfo.getCode(), projectInfo.getName());
 						}
 					}
 			} else {
 				commandString = buildCommandString(applicationInfo.getCode(), techId, archetypeInfo.getGroupId(), 
 						archetypeInfo.getArtifactId(), version,	repoInfo.getReleaseRepoURL(), projectInfo.getVersion(), customerId, groupId);
 				executeCreateCommand(tempFolderPath, commandString, customerId, projectInfo);
 				updateDefaultFeatures(projectInfo, tempFolderPath, customerId, applicationInfo.getCode(), null);
 				updateRepository(customerId, applicationInfo, new File(
 						tempFolderPath), applicationInfo.getCode(), projectInfo.getName());
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
 
 	private void createMultiModuleObject(ProjectInfo projectInfo,
 			String tempFolderPath, ApplicationInfo applicationInfo,
 			String customerId, RepoInfo repoInfo, String techId,
 			ArtifactGroup archetypeInfo, String version, String groupId)
 			throws PhrescoException {
 		String commandString;
 		String MULTI_MODULE_ARCHETYPE = "phresco-multimodule-archetype";
 		
 		commandString = buildCommandString(applicationInfo.getCode(), techId, archetypeInfo.getGroupId(), 
 				MULTI_MODULE_ARCHETYPE, version, repoInfo.getReleaseRepoURL(), projectInfo.getVersion(), customerId, groupId);
 		
 		executeCreateCommand(tempFolderPath, commandString, customerId, projectInfo);
 		applicationInfo.setParentArchrtypeCreated(true);
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
 			File tempFolderPath, String modName, String projectName) throws PhrescoException {
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
 		builder.append(getPhrescoPomFile(appInfo, new File(tempFolderPath, modName)).getName());
 		File pomFile = new File(builder.toString());
 		try {
 			writeDistributionTag(customerId, appInfo, projectName, repoInfo, pomFile);
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
 
 	private void writeDistributionTag(String customerId, ApplicationInfo appInfo, String projectName,
 			RepoInfo repoInfo, File pomFile) throws PhrescoPomException {
 		PomProcessor processor = new PomProcessor(pomFile);
 		processor.setName(appInfo.getName());
 		processor.addRepositories(customerId, repoInfo.getGroupRepoURL());
 		DistributionManagement distributionManagement = new DistributionManagement();
 		DeploymentRepository repository = new DeploymentRepository();
 		repository.setId(projectName.concat("-release"));
 		repository.setUrl(repoInfo.getReleaseRepoURL());
 		distributionManagement.setRepository(repository);
 		if(StringUtils.isNotEmpty(repoInfo.getSnapshotRepoURL())) {
 			repository = new DeploymentRepository();
 			repository.setId(projectName.concat("-snapshot"));
 			repository.setUrl(repoInfo.getSnapshotRepoURL());
 			distributionManagement.setSnapshotRepository(repository);
 		}
 		processor.getModel().setDistributionManagement(distributionManagement);
 		processor.save();
 		File sourcePomFile = new File(pomFile.getParent(), appInfo.getPomFile());
 		if(sourcePomFile.exists()) {
 			PomProcessor pomProcessor = new PomProcessor(sourcePomFile);
 			pomProcessor.getModel().setDistributionManagement(distributionManagement);
 			pomProcessor.save();
 		}
 	}
 	
 	private File getPhrescoPomFile(ApplicationInfo appInfo, File path) {
 		File file = new File(path, "phresco-pom.xml");
 		if(file.exists()) {
 			appInfo.setPhrescoPomFile("phresco-pom.xml");
 			return file;
 		}
 		return new File(path, "pom.xml");
 	}
 	private String getSourceFromPom(File pomFile) throws PhrescoException {
 		String srcDir = "";
         try {
         	PomProcessor pomprocessor = new PomProcessor(pomFile);
         	srcDir = pomprocessor.getProperty("source.dir");;
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 		return srcDir;
 	}
 	
 	private void updateDefaultFeatures(ProjectInfo projectInfo,String tempFolderPath,
 			String customerId, String modName, ModuleInfo moduleInfo) throws PhrescoException, PhrescoPomException {
 		ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
 		String sourceDir = "";
 		File phrescoPomFile = null;
 		File pomFile = null;
 		if(moduleInfo != null) {
 			phrescoPomFile = getPhrescoPomFile(appInfo, new File(tempFolderPath, moduleInfo.getCode()));
             sourceDir = getSourceFromPom(phrescoPomFile);
             if(StringUtils.isNotEmpty(sourceDir)) {
             	phrescoPomFile = getPhrescoPomFile(appInfo, new File(tempFolderPath, 
             			moduleInfo.getCode().concat(File.separator).concat(sourceDir)));
             	String pomPath = new StringBuilder(tempFolderPath).append(File.separator).append(moduleInfo.getCode()).append(File.separator).
             	append(sourceDir).append(File.separator).append("pom.xml").toString();
             	pomFile = new File(pomPath);
             }
             String pomPath = new StringBuilder(tempFolderPath).append(File.separator).append(moduleInfo.getCode()).append(File.separator).
         		append("pom.xml").toString();
             pomFile = new File(pomPath);
 		} else {
 			phrescoPomFile = getPhrescoPomFile(appInfo, new File(tempFolderPath, appInfo.getAppDirName()));
 			sourceDir = getSourceFromPom(phrescoPomFile);
 			if(StringUtils.isNotEmpty(sourceDir)) {
             	phrescoPomFile = getPhrescoPomFile(appInfo, new File(tempFolderPath, 
             			appInfo.getAppDirName().concat(File.separator).concat(sourceDir)));
             	String pomPath = new StringBuilder(tempFolderPath).append(File.separator).append(appInfo.getAppDirName()).append(File.separator).
             		append(sourceDir).append(File.separator).append("pom.xml").toString();
             	pomFile = new File(pomPath);
             } else {
 			String pomPath = new StringBuilder(tempFolderPath).append(File.separator).append(appInfo.getAppDirName()).append(File.separator)
     			.append("pom.xml").toString();
 			pomFile = new File(pomPath);
             }
 		}
 		
 		List<String> selectedFeatures = new ArrayList<String>();
 		List<String> selectedJsLibs = new ArrayList<String>();
 		List<String> selectedComponentids = new ArrayList<String>();
 		List<ArtifactGroup> listArtifactGroup = new ArrayList<ArtifactGroup>();
 		Map<String, String> selectedFeatureMap = new HashMap<String, String>();
 		
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
 							ArtifactGroup clonedArtifactGroupObject = cloneArtifactGroupObject(artifactGroup, artifactInfo);
 							listArtifactGroup.add(clonedArtifactGroupObject);
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
 							ArtifactGroup clonedArtifactGroupObject = cloneArtifactGroupObject(artifactGroup, artifactInfo);
 							listArtifactGroup.add(clonedArtifactGroupObject);
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
 							ArtifactGroup clonedArtifactGroupObject = cloneArtifactGroupObject(artifactGroup, artifactInfo);
 							listArtifactGroup.add(clonedArtifactGroupObject);
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
             projectUtils.updatePOMWithPluginArtifact(pomFile, phrescoPomFile, listArtifactGroup);
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
 //		if(phrescoPomFile.exists()) {
 //			appInfo.setPhrescoPomFile("phresco-pom.xml");
 //		}
 		
 		for (ArtifactGroup group : listArtifactGroup) {
 			String selectedScope = StringUtils.isNotEmpty(group.getVersions().get(0).getScope()) ? group.getVersions().get(0).getScope() : "";
 			selectedFeatureMap.put(group.getVersions().get(0).getId(), selectedScope);
 		}
 		appInfo.setSelectedFeatureMap(selectedFeatureMap);
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
 
 	private ArtifactGroup cloneArtifactGroupObject(ArtifactGroup artifactGroup, ArtifactInfo artifactInfo) {
 		ArtifactGroup newArtifactGroup = new ArtifactGroup();
 		newArtifactGroup.setAppliesTo(artifactGroup.getAppliesTo());
 		newArtifactGroup.setArtifactId(artifactGroup.getArtifactId());
 		newArtifactGroup.setCreationDate(artifactGroup.getCreationDate());
 		newArtifactGroup.setCustomerIds(artifactGroup.getCustomerIds());
 		newArtifactGroup.setDescription(artifactGroup.getDescription());
 		newArtifactGroup.setDisplayName(artifactGroup.getDisplayName());
 		newArtifactGroup.setGroupId(artifactGroup.getGroupId());
 		newArtifactGroup.setHelpText(artifactGroup.getHelpText());
 		newArtifactGroup.setId(artifactGroup.getId());
 		newArtifactGroup.setImageURL(artifactGroup.getImageURL());
 		newArtifactGroup.setLicenseId(artifactGroup.getLicenseId());
 		newArtifactGroup.setName(artifactGroup.getName());
 		newArtifactGroup.setPackaging(artifactGroup.getPackaging());
 		newArtifactGroup.setStatus(artifactGroup.getStatus());
 		newArtifactGroup.setSystem(artifactGroup.isSystem());
 		newArtifactGroup.setType(artifactGroup.getType());
 		
 		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 		ArtifactInfo artfInfo = new ArtifactInfo();
 		artfInfo.setAppliesTo(artifactInfo.getAppliesTo());
 		artfInfo.setArtifactGroupId(artifactInfo.getArtifactGroupId());
 		artfInfo.setDescription(artifactInfo.getDescription());
 		artfInfo.setDisplayName(artifactInfo.getDisplayName());
 		artfInfo.setDownloadURL(artifactInfo.getDownloadURL());
 		artfInfo.setVersion(artifactInfo.getVersion());
 		artfInfo.setDependencyIds(artifactInfo.getDependencyIds());
 		artfInfo.setHelpText(artifactInfo.getHelpText());
 		artfInfo.setId(artifactInfo.getId());
 		artfInfo.setName(artifactInfo.getName());
 		artfInfo.setScope(artifactInfo.getScope());
 		artfInfo.setStatus(artifactInfo.getStatus());
 		artfInfo.setSystem(artifactInfo.isSystem());
 		artfInfo.setUsed(artifactInfo.isUsed());
 		artifactInfos.add(artfInfo);
 		newArtifactGroup.setVersions(artifactInfos);
 		return newArtifactGroup;
 	}
 	
 	private ProjectInfo cloneProjInfo(ProjectInfo projectInfo, ApplicationInfo appInfo, ModuleInfo moduleInfo) throws PhrescoException {
 		try {
 			ProjectInfo newProjectInfo = new ProjectInfo();
 			newProjectInfo.setId(projectInfo.getId());
 			newProjectInfo.setName(projectInfo.getName());
 			newProjectInfo.setDescription(projectInfo.getDescription());
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
