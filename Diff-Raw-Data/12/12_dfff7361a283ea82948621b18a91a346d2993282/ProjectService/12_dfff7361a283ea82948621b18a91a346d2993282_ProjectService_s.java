 /**
  * Framework Web Archive
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
 package com.photon.phresco.framework.rest.api;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.json.JSONObject;
 import org.sonar.wsclient.Host;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.connectors.HttpClient4Connector;
 import org.sonar.wsclient.services.DeleteQuery;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.LockUtil;
 import com.photon.phresco.commons.ResponseCodes;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.CoreOption;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.FunctionalFramework;
 import com.photon.phresco.commons.model.FunctionalFrameworkInfo;
 import com.photon.phresco.commons.model.FunctionalFrameworkProperties;
 import com.photon.phresco.commons.model.ModuleInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.SelectedFeature;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.commons.model.UserPermissions;
 import com.photon.phresco.commons.model.WebService;
 import com.photon.phresco.configuration.ConfigurationInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.exception.PhrescoWebServiceException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.api.ProjectManager;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.model.DeleteProjectInfo;
 import com.photon.phresco.framework.rest.api.util.FrameworkServiceUtil;
 import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.service.client.impl.ServiceManagerImpl;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.ProjectUtils;
 import com.photon.phresco.util.ServiceConstants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Dependency;
 import com.phresco.pom.model.Model.Modules;
 import com.phresco.pom.model.Profile;
 import com.phresco.pom.util.PomProcessor;
 import com.sun.jersey.api.client.ClientResponse.Status;
 
 /**
  * The Class ProjectService.
  */
 @Path(ServiceConstants.REST_API_PROJECT)
 public class ProjectService extends RestBase implements FrameworkConstants, ServiceConstants, ResponseCodes {
 	
 	String status;
 	String errorCode;
 	String successCode;
 
 	/**
 	 * To return the List of available projects.
 	 *
 	 * @param customerId the customer id
 	 * @return the response
 	 */
 	@GET
 	@Path(REST_API_PROJECTLIST)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response list(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
 		ResponseInfo<List<ProjectInfo>> responseData = new ResponseInfo<List<ProjectInfo>>();
 		try {
 			ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 			List<ProjectInfo> projects = projectManager.discover(customerId);
 			if (CollectionUtils.isNotEmpty(projects)) {
 				Collections.sort(projects, sortByDateToLatest());
 			}
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200001;
 			ResponseInfo<List<ProjectInfo>> finalOutput = responseDataEvaluation(responseData, null,
 					projects, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210001;
 			ResponseInfo<List<ProjectInfo>> finalOutput = responseDataEvaluation(responseData, e,
 					null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER)
 					.build();
 		}
 	}
 
 	/**
 	 * Appinfo list of a specific project .
 	 *
 	 * @param customerId the customer id
 	 * @param projectId the project id
 	 * @return the response
 	 */
 	@GET
 	@Path(REST_API_APPINFOS)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response appinfoList(@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId) {
 		ResponseInfo<List<ApplicationInfo>> responseData = new ResponseInfo<List<ApplicationInfo>>();
 		try {
 			List<ApplicationInfo> appInfos = com.photon.phresco.framework.impl.util.FrameworkUtil.getAppInfos(customerId, projectId);
 			if (CollectionUtils.isNotEmpty(appInfos)) {
 				status = RESPONSE_STATUS_SUCCESS;
 				successCode = PHR200002;
 				ResponseInfo<List<ProjectInfo>> finalOutput = responseDataEvaluation(responseData, null,
 						appInfos, status, successCode);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER)
 						.build();
 			}
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210002;
 			ResponseInfo<List<ProjectInfo>> finalOutput = responseDataEvaluation(responseData, e,
 					null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER)
 					.build();
 		}
 		status = RESPONSE_STATUS_SUCCESS;
 		successCode = PHR200003;
 		ResponseInfo<List<ProjectInfo>> finalOutput = responseDataEvaluation(responseData, null,
 				null, status, successCode);
 		return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 	}
 
 	/**
 	 * Creates the project.
 	 *
 	 * @param projectinfo the projectinfo
 	 * @param userId the user id
 	 * @return the response
 	 */
 	@POST
 	@Path(REST_API_PROJECT_CREATE)
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response createProject(ProjectInfo projectinfo, @QueryParam(REST_QUERY_USERID) String userId) {
 		ResponseInfo<ProjectInfo> responseData = new ResponseInfo<ProjectInfo>();
 		try {
 				ResponseInfo validationResponse = validateProject(projectinfo);
 				if (validationResponse != null) {
 				ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, null,
 						validationResponse.getData(), validationResponse.getStatus(), validationResponse.getResponseCode());
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 			}
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			if (serviceManager == null) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210003;
 				ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, null,
 						null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 						"*").build();
 			}
 			if (projectinfo != null) {
 				ProjectInfo projectInfo = PhrescoFrameworkFactory.getProjectManager().create(projectinfo, serviceManager);
				handleDependencies(Utility.getProjectHome(), projectInfo, null);
 				status = RESPONSE_STATUS_SUCCESS;
 				successCode = PHR200004;
 				ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, null,
						projectInfo, status, successCode);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 			}
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210004;
 			ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, e,
 					null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER)
 					.build();
 		}
 		return null;
 	}
 
 	/**
 	 * Edits the project.
 	 *
 	 * @param projectId the project id
 	 * @param customerId the customer id
 	 * @return the response
 	 */
 	@GET
 	@Path(REST_API_PROJECT_EDIT)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response editProject(@QueryParam(REST_QUERY_PROJECTID) String projectId,
 			@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
 		ProjectInfo projectInfo = null;
 		ResponseInfo<ProjectInfo> responseData = new ResponseInfo<ProjectInfo>();
 		try {
 			ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 			projectInfo = projectManager.getProject(projectId, customerId);
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200005;
 			ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, null,
 					projectInfo, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210005;
 			ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, e, null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER)
 					.build();
 		}
 	}
 
 	/**
 	 * Update project.
 	 *
 	 * @param projectinfo the projectinfo
 	 * @param userId the user id
 	 * @return the response
 	 */
 	@PUT
 	@Path(REST_API_UPDATEPROJECT)
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response updateProject(ProjectInfo projectinfo, @QueryParam(REST_QUERY_USERID) String userId) {
 		ResponseInfo<ProjectInfo> responseData = new ResponseInfo<ProjectInfo>();
 		try {
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			if (serviceManager == null) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210003;
 				ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, null,
 						null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 						"*").build();
 			}
 			List<ApplicationInfo> appInfosFromPage = projectinfo.getAppInfos();
 			ProjectInfo availableProjectInfo = getProject(projectinfo.getId(), projectinfo.getCustomerIds().get(0));
 			if (CollectionUtils.isNotEmpty(appInfosFromPage)) {
 				projectinfo = PhrescoFrameworkFactory.getProjectManager().create(projectinfo, serviceManager);
 				handleDependencies(Utility.getProjectHome(), projectinfo, availableProjectInfo);
 				if (projectinfo.isMultiModule() && projectinfo != null) {
 					projectinfo = createProjectInfo(availableProjectInfo, projectinfo);
 				} else {
 					projectinfo = getProject(projectinfo.getId(), projectinfo.getCustomerIds().get(0));
 				}
 
 				for (ApplicationInfo applicationInfo : projectinfo.getAppInfos()) {
 					if (CollectionUtils.isNotEmpty(applicationInfo.getModules())) {
 						updateParentPom(applicationInfo);
 					}
 					projectinfo.setAppInfos(Collections.singletonList(applicationInfo));
 					StringBuilder sb = new StringBuilder(Utility.getProjectHome()).append(
 							applicationInfo.getAppDirName()).append(File.separator)
 							.append(Constants.DOT_PHRESCO_FOLDER).append(File.separator).append(
 									Constants.PROJECT_INFO_FILE);
 					ProjectUtils.updateProjectInfo(projectinfo, new File(sb.toString()));
 				}
 			} else {
 				for (ApplicationInfo applicationInfo : availableProjectInfo.getAppInfos()) {
 					String appDirPath = Utility.getProjectInfoPath(Utility.getProjectHome()
 							+ applicationInfo.getAppDirName(), null);
 					ProjectInfo projectInfoFile = Utility.getProjectInfo(Utility.getProjectHome()
 							+ applicationInfo.getAppDirName(), null);
 					
 					projectInfoFile.setDescription(projectinfo.getDescription());
 					projectInfoFile.setIntegrationTest(projectinfo.isIntegrationTest());
 					ProjectUtils.updateProjectInfo(projectInfoFile, new File(appDirPath));
 					
 					if(projectinfo.isIntegrationTest()) {
 						projectinfo = PhrescoFrameworkFactory.getProjectManager().addIntegrationTest(projectinfo, serviceManager);
 						
 					}
 				}
 			}
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200006;
 			ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, null,
 					projectinfo, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210006;
 			ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, e,
 					null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER)
 					.build();
 		}
 	}
 	
 	private void updateParentPom(ApplicationInfo applicationInfo) throws PhrescoException {
 		String pomPath = Utility.getProjectHome() +  
 			applicationInfo.getAppDirName() + File.separator + "pom.xml";
 		try {
 			PomProcessor pomProcessor = new PomProcessor(new File(pomPath));
 			Modules modules = pomProcessor.getModel().getModules();
 			List<String> moduleNames = new ArrayList<String>();
 			if(modules != null) {
 				moduleNames =  modules.getModule();
 				if(CollectionUtils.isNotEmpty(applicationInfo.getModules())) {
 					for (ModuleInfo moduleInfo : applicationInfo.getModules()) {
 						if(CollectionUtils.isNotEmpty(moduleNames)) {
 							if(!moduleNames.contains(moduleInfo.getCode())) {
 								moduleNames.add(moduleInfo.getCode());
 							}
 						} else {
 							moduleNames.add(moduleInfo.getCode());
 						}
 					}
 				}
 			}
 			if(CollectionUtils.isNotEmpty(moduleNames)) {
 				for (String moduleName : moduleNames) {
 					pomProcessor.addModule(moduleName);
 				}
 			}
 			pomProcessor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private ProjectInfo createProjectInfo(ProjectInfo availableProjectInfo, ProjectInfo updatedProjectInfo) throws PhrescoException {
 		List<ApplicationInfo> availableApInfos = availableProjectInfo.getAppInfos();
 		List<ApplicationInfo> updatedAppInfos = updatedProjectInfo.getAppInfos();
 		Map<String, ApplicationInfo> appMap = new HashMap<String, ApplicationInfo>();
 		for (ApplicationInfo applicationInfo : availableApInfos) {
 			appMap.put(applicationInfo.getAppDirName(), applicationInfo);
 		}
 		for (ApplicationInfo applicationInfo : updatedAppInfos) {
 			if(!appMap.containsKey(applicationInfo.getAppDirName())) {
 				File path = new File(Utility.getProjectHome() + applicationInfo.getAppDirName());
 				ProjectInfo newProjecInfo = ProjectUtils.getProjectInfoFile(path);
 				appMap.put(applicationInfo.getAppDirName(), newProjecInfo.getAppInfos().get(0));
 				continue;
 			}
 			ApplicationInfo applicationInfo2 = appMap.get(applicationInfo.getAppDirName());
 			List<ModuleInfo> modules = applicationInfo2.getModules();
 			Map<String, ModuleInfo> moduleMap = new HashMap<String, ModuleInfo>();
 			if(CollectionUtils.isNotEmpty(modules)) {
 				for (ModuleInfo moduleInfo : modules) {
 					moduleMap.put(moduleInfo.getCode(), moduleInfo);
 				}
 			}
 			List<ModuleInfo> updatedModules = applicationInfo.getModules();
 			if(CollectionUtils.isNotEmpty(updatedModules)) {
 				for (ModuleInfo moduleInfo : updatedModules) {
 					moduleMap.put(moduleInfo.getCode(), moduleInfo);
 				}
 			}
 			if(!moduleMap.isEmpty()) {
 				modules = new ArrayList<ModuleInfo>(moduleMap.values());
 			}
 			applicationInfo.setModules(modules);
 			appMap.put(applicationInfo.getAppDirName(), applicationInfo);
 		}
 		availableProjectInfo.setAppInfos(new ArrayList<ApplicationInfo>(appMap.values()));
 		return availableProjectInfo;
 	}
 	
 	
 	private List<ApplicationInfo> findNewlyAddedApps(List<ApplicationInfo> appInfosFromPage, List<ApplicationInfo> oldApps) {
 		Map<String, ApplicationInfo> appMap = new HashMap<String, ApplicationInfo>();
 		for (ApplicationInfo applicationInfo : oldApps) {
 			appMap.put(applicationInfo.getAppDirName(), applicationInfo);
 		}
 		for (ApplicationInfo applicationInfo : appInfosFromPage) {
 			if(!appMap.containsKey(applicationInfo.getAppDirName())) {
 				appMap.put(applicationInfo.getAppDirName(), applicationInfo);
 				continue;
 			}
 			List<ModuleInfo> modules = applicationInfo.getModules();
 			if(CollectionUtils.isNotEmpty(modules)) {
 				List<ModuleInfo> newModules = new ArrayList<ModuleInfo>();
 				for (ModuleInfo moduleInfo : modules) {
 					if(!moduleInfo.isModified()) {
 						newModules.add(moduleInfo);
 					}
 				}
 				applicationInfo.setModules(newModules);
 			}
 			applicationInfo.setCreated(true);
 			appMap.put(applicationInfo.getAppDirName(), applicationInfo);
 		}
 		if(appMap.isEmpty()) {
 			return null;
 		}
 		return new ArrayList<ApplicationInfo>(appMap.values());
 	}
 
 	private ProjectInfo getProject(String projectId, String customerId) throws PhrescoException {
 		List<ProjectInfo> discover = PhrescoFrameworkFactory.getProjectManager().discover(customerId);
 		for (ProjectInfo projectInfo : discover) {
 			if (projectInfo.getId().equals(projectId)) {
 				return projectInfo;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Update application features.
 	 *
 	 * @param selectedFeaturesFromUI the selected features from ui
 	 * @param appDirName the app dir name
 	 * @param userId the user id
 	 * @param customerId the customer id
 	 * @return the response
 	 */
 	@PUT
 	@Path(REST_API_UPDATE_FEATRUE)
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response updateApplicationFeatures(List<SelectedFeature> selectedFeaturesFromUI,
 			@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_USERID) String userId,
 			@QueryParam(REST_QUERY_CUSTOMERID) String customerId, @QueryParam("displayName") String displayName,
 			@QueryParam(REST_QUERY_MODULE_NAME) String module, @QueryParam(REST_QUERY_ROOT_MODULE_NAME) String rootModule) {
 		File filePath = null;
 		BufferedReader bufferedReader = null;
 		String rootModulePath = "";
 		String subModuleName = "";
 		Gson gson = new Gson();
 		ResponseInfo responseData = new ResponseInfo();
 		List<String> selectedFeatures = new ArrayList<String>();
 		List<String> selectedJsLibs = new ArrayList<String>();
 		List<String> selectedComponents = new ArrayList<String>();
 		Map<String, String> selectedFeatureMap = new HashMap<String, String>();
 		List<ArtifactGroup> listArtifactGroup = new ArrayList<ArtifactGroup>();
 		String unique_key = "";
 		try {
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			if (serviceManager == null) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210003;
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 						"*").build();
 			}
 			
 			if (StringUtils.isNotEmpty(rootModule)) {
 				rootModulePath = Utility.getProjectHome() + rootModule;
 				subModuleName = module;
 			} else {
 				rootModulePath = Utility.getProjectHome() + appDirName;
 			}
 			ProjectInfo projectinfo = Utility.getProjectInfo(rootModulePath, subModuleName);
 			ApplicationInfo applicationInfo = projectinfo.getAppInfos().get(0);
 			UUID uniqueKey = UUID.randomUUID();
 			unique_key = uniqueKey.toString();
 			LockUtil.generateLock(Collections.singletonList(LockUtil.getLockDetail(applicationInfo.getId(), FrameworkConstants.FEATURE_UPDATE, displayName, unique_key)), true);
 			Technology technology = serviceManager.getTechnology(applicationInfo.getTechInfo().getId());
 			List<String> archetypeFeatures = technology.getArchetypeFeatures();
 			if (CollectionUtils.isNotEmpty(selectedFeaturesFromUI)) {
 				for (SelectedFeature selectedFeatureFromUI : selectedFeaturesFromUI) {
 					String selectedScope = StringUtils.isNotEmpty(selectedFeatureFromUI.getScope()) ? selectedFeatureFromUI.getScope() : "";
 					selectedFeatureMap.put(selectedFeatureFromUI.getVersionID(), selectedScope);
 					String artifactGroupId = selectedFeatureFromUI.getModuleId();
 					ArtifactGroup artifactGroup = serviceManager.getArtifactGroupInfo(artifactGroupId);
 					ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(selectedFeatureFromUI.getVersionID());
 					artifactInfo.setScope(selectedFeatureFromUI.getScope());
 					if (artifactInfo != null) {
 						artifactGroup.setVersions(Collections.singletonList(artifactInfo));
 					}
 					List<CoreOption> appliesTo = artifactGroup.getAppliesTo();
 					if (CollectionUtils.isNotEmpty(appliesTo)) {
 						for (CoreOption coreOption : appliesTo) {
 							
 							if (coreOption.getTechId().equals(applicationInfo.getTechInfo().getId())) {
 								artifactGroup.setAppliesTo(Collections.singletonList(coreOption));
 								listArtifactGroup.add(artifactGroup);
 								break;
 							} else if(CollectionUtils.isNotEmpty(archetypeFeatures)) {
 								for (String archetypeFeature : archetypeFeatures) {
 									if(archetypeFeature.equalsIgnoreCase(coreOption.getTechId())){
 										artifactGroup.setAppliesTo(Collections.singletonList(coreOption));
 										listArtifactGroup.add(artifactGroup);
 										break;
 									}
 								}
 							}
 						}
 					}
 					if (selectedFeatureFromUI.getType().equals(ArtifactGroup.Type.FEATURE.name())) {
 						selectedFeatures.add(selectedFeatureFromUI.getVersionID());
 					}
 					if (selectedFeatureFromUI.getType().equals(ArtifactGroup.Type.JAVASCRIPT.name())) {
 						selectedJsLibs.add(selectedFeatureFromUI.getVersionID());
 					}
 					if (selectedFeatureFromUI.getType().equals(ArtifactGroup.Type.COMPONENT.name())) {
 						selectedComponents.add(selectedFeatureFromUI.getVersionID());
 					}
 				}
 			}
 			String dotPhrescoFolderPath = Utility.getDotPhrescoFolderPath(rootModulePath, subModuleName);
 				filePath = new File(dotPhrescoFolderPath + File.separator + Constants.APPLICATION_HANDLER_INFO_FILE);
 			MojoProcessor mojo = new MojoProcessor(filePath);
 			ApplicationHandler applicationHandler = mojo.getApplicationHandler();
 			// To write selected Features into phresco-application-Handler-info.xml
 			String artifactGroup = gson.toJson(listArtifactGroup);
 			applicationHandler.setSelectedFeatures(artifactGroup);
 
 			// To write Deleted Features into phresco-application-Handler-info.xml
 			List<ArtifactGroup> removedModules = getRemovedModules(applicationInfo, selectedFeaturesFromUI,	serviceManager);
 			Type jsonType = new TypeToken<Collection<ArtifactGroup>>() {}.getType();
 			String deletedFeatures = gson.toJson(removedModules, jsonType);
 			applicationHandler.setDeletedFeatures(deletedFeatures);
 
 			mojo.save();
 
 			applicationInfo.setSelectedModules(selectedFeatures);
 			applicationInfo.setSelectedJSLibs(selectedJsLibs);
 			applicationInfo.setSelectedComponents(selectedComponents);
 			applicationInfo.setSelectedFeatureMap(selectedFeatureMap);
 			applicationInfo.setCreated(true);
 			projectinfo.setAppInfos(Collections.singletonList(applicationInfo));
 			ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 			projectManager.updateApplicationFeatures(projectinfo, serviceManager, rootModule);
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210008;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, e, null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER)
 					.build();
 		} finally {
 			try {
 				LockUtil.removeLock(unique_key);
 			} catch (PhrescoException e) {
 			}
 		}
 		status = RESPONSE_STATUS_SUCCESS;
 		successCode = PHR200007;
 		ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, status, successCode);
 		return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 
 	}
 
 	/**
 	 * Update application.
 	 *
 	 * @param oldAppDirName the old app dir name
 	 * @param appInfo the app info
 	 * @param userId the user id
 	 * @param customerId the customer id
 	 * @return the response
 	 */
 	@PUT
 	@Path(REST_UPDATE_APPLICATION)
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response updateApplication(@QueryParam(REST_QUERY_OLD_APPDIR_NAME) String oldAppDirName,
 			ApplicationInfo appInfo, @QueryParam(REST_QUERY_USERID) String userId,
 			@QueryParam(REST_QUERY_CUSTOMERID) String customerId,  @QueryParam("displayName") String displayName,
 			@QueryParam("rootModule") String rootModule) {
 		BufferedReader bufferedReader = null;
 		File filePath = null;
 		String unique_key = "";
 		String rootModulePath = "";
 		String subModuleName = "";
 		ResponseInfo<JSONObject> responseData = new ResponseInfo<JSONObject>();
 		Map json = new HashMap();
 		try {
 			UUID uniqueKey = UUID.randomUUID();
 			unique_key = uniqueKey.toString();
 			
 				ResponseInfo validationResponse = validateAppInfo(oldAppDirName,appInfo, rootModule);
 				if (validationResponse != null) {
 				ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, null,
 						null, validationResponse.getStatus(), validationResponse.getResponseCode());
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 						"*").build();
 			}
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			if (serviceManager == null) {
 				status = RESPONSE_STATUS_FAILURE;
 				errorCode = PHR210003;
 				ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, null,
 						null, status, errorCode);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 						"*").build();
 			}
 			
 			//To generate the lock for the particular operation
 			LockUtil.generateLock(Collections.singletonList(LockUtil.getLockDetail(appInfo.getId(), FrameworkConstants.APPLN_UPDATE, displayName, unique_key)), true);
 			
 			List<DownloadInfo> selectedServerGroup = new ArrayList<DownloadInfo>();
 			List<DownloadInfo> selectedDatabaseGroup = new ArrayList<DownloadInfo>();
 			List<ArtifactGroupInfo> selectedDatabases = null;
 			
 			Gson gson = new Gson();
 			String folder = oldAppDirName;
 			if (StringUtils.isNotEmpty(rootModule)) {
 				folder = rootModule + File.separator + oldAppDirName;
 				rootModulePath = Utility.getProjectHome() + rootModule;
 				subModuleName = oldAppDirName;
 			} else {
 				rootModulePath = Utility.getProjectHome() + oldAppDirName;
 			}
 			String applicationHandlerXml = Utility.getDotPhrescoFolderPath(rootModulePath, subModuleName);
 			filePath = new File(applicationHandlerXml + File.separator + Constants.APPLICATION_HANDLER_INFO_FILE);
 			if (new File(filePath.toString()).exists()) {
 				MojoProcessor mojo = new MojoProcessor(filePath);
 				ApplicationHandler applicationHandler = mojo.getApplicationHandler();
 				
 				// To write selected Database into phresco-application-Handler-info.xml
 				selectedDatabases = appInfo.getSelectedDatabases();
 				updateSelectedDBinApplnHandler(serviceManager, selectedDatabaseGroup, gson, applicationHandler, selectedDatabases);
 	
 				// To write selected Servers into phresco-application-Handler-info.xml
 				List<ArtifactGroupInfo> selectedServers = appInfo.getSelectedServers();
 				updateSelectedServersInApplnHandlerXml(serviceManager, selectedServerGroup, gson, applicationHandler, selectedServers);
 	
 				// To write selected WebServices info to phresco-plugin-info.xml
 				List<String> selectedWebservices = appInfo.getSelectedWebservices();
 				
 				updateSelectedWSinApplnHandlerXml(serviceManager, gson, applicationHandler, selectedWebservices);
 				mojo.save();
 			} else {
 				throw new PhrescoException("Application Handler xml does not exist");
 			}
 				ProjectInfo projectInfo =  Utility.getProjectInfo(rootModulePath, subModuleName);
 			appInfo.setCreated(true);
 			deleteSqlFolder(projectInfo, selectedDatabases, serviceManager, rootModulePath, subModuleName);
 
 			projectInfo.setAppInfos(Collections.singletonList(appInfo));
 			ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 			projectManager.updateApplication(projectInfo, serviceManager, oldAppDirName, rootModule);
 
 			//to update submodule's appdirname in root module's project info - in appInfo.getModules() entry
 			if (StringUtils.isNotEmpty(rootModule) && !oldAppDirName.equals(appInfo.getAppDirName())) {
 				updateSubModuleNameInRootProjInfo(rootModule, oldAppDirName, appInfo.getAppDirName());
 			} 
 			String newFolderDir = appInfo.getAppDirName();
 			String subMod = "";
 			if (StringUtils.isEmpty(rootModule)) {
 				rootModulePath = Utility.getProjectHome() + appInfo.getAppDirName();
 				ProjectInfo rootProjectInfo = Utility.getProjectInfo(Utility.getProjectHome() + appInfo.getAppDirName(), "");
 				List<ModuleInfo> modules = rootProjectInfo.getAppInfos().get(0).getModules();
 				updateRootModuleNameInSubProjectInfo(modules, oldAppDirName, appInfo.getAppDirName());
 			} else {
 				newFolderDir = rootModule + File.separator + appInfo.getAppDirName();
 				rootModulePath = Utility.getProjectHome() + rootModule;
 				subMod = appInfo.getAppDirName();
 			}
 			
 //			To update parent tags in submodule's pom
 			updateSubModulePomParentTagInfo(newFolderDir, appInfo, rootModulePath, subMod);
 			
 			// to update functional framework in pom.xml
 			updateFunctionalTestProperties(appInfo, serviceManager, rootModule);
 			json = embedApplication(json, projectInfo, serviceManager, projectManager, folder);
 					status = RESPONSE_STATUS_SUCCESS;
 					successCode = PHR200008;
 					json.put("projectInfo", projectInfo);
 					ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, null,
 							json, status, successCode);
 					return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER)
 							.build();
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210009;
 			ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, e,
 					null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 					"*").build();
 		} finally {
 			Utility.closeReader(bufferedReader);
 			try {
 				LockUtil.removeLock(unique_key);
 			} catch (PhrescoException e) {
 			}
 		}
 	}
 
 	private void updateSelectedWSinApplnHandlerXml(ServiceManager serviceManager, Gson gson, ApplicationHandler applicationHandler, List<String> selectedWebservices)
 			throws PhrescoException {
 		List<WebService> webServiceList = new ArrayList<WebService>();
 		if (CollectionUtils.isNotEmpty(selectedWebservices)) {
 			for (String selectedWebService : selectedWebservices) {
 				WebService webservice = serviceManager.getWebService(selectedWebService);
 				webServiceList.add(webservice);
 			}
 			if (CollectionUtils.isNotEmpty(webServiceList)) {
 				String serverGroup = gson.toJson(webServiceList);
 				applicationHandler.setSelectedWebService(serverGroup);
 			}
 		} else {
 			applicationHandler.setSelectedWebService(null);
 		}
 	}
 
 	private void updateSelectedServersInApplnHandlerXml(ServiceManager serviceManager, List<DownloadInfo> selectedServerGroup, Gson gson, ApplicationHandler applicationHandler,
 			List<ArtifactGroupInfo> selectedServers) throws PhrescoException {
 		if (CollectionUtils.isNotEmpty(selectedServers)) {
 			for (ArtifactGroupInfo selectedServer : selectedServers) {
 				DownloadInfo downloadInfo = serviceManager.getDownloadInfo(selectedServer.getArtifactGroupId());
 				String id = downloadInfo.getArtifactGroup().getId();
 				ArtifactGroup artifactGroupInfo = serviceManager.getArtifactGroupInfo(id);
 				List<ArtifactInfo> serverVersionInfos = artifactGroupInfo.getVersions();
 				List<ArtifactInfo> selectedServerVersionInfos = new ArrayList<ArtifactInfo>();
 				for (ArtifactInfo versionInfo : serverVersionInfos) {
 					String versionId = versionInfo.getId();
 					if (selectedServer.getArtifactInfoIds().contains(versionId)) {
 						selectedServerVersionInfos.add(versionInfo);
 					}
 				}
 				downloadInfo.getArtifactGroup().setVersions(selectedServerVersionInfos);
 				selectedServerGroup.add(downloadInfo);
 			}
 			if (CollectionUtils.isNotEmpty(selectedServerGroup)) {
 				String serverGroup = gson.toJson(selectedServerGroup);
 				applicationHandler.setSelectedServer(serverGroup);
 			}
 		} else {
 			applicationHandler.setSelectedServer(null);
 		}
 	}
 
 	private void updateSelectedDBinApplnHandler(ServiceManager serviceManager, List<DownloadInfo> selectedDatabaseGroup, Gson gson, ApplicationHandler applicationHandler,
 			List<ArtifactGroupInfo> selectedDatabases) throws PhrescoException {
 		if (CollectionUtils.isNotEmpty(selectedDatabases)) {
 			for (ArtifactGroupInfo selectedDatabase : selectedDatabases) {
 				DownloadInfo downloadInfo = serviceManager.getDownloadInfo(selectedDatabase.getArtifactGroupId());
 				String id = downloadInfo.getArtifactGroup().getId();
 				ArtifactGroup artifactGroupInfo = serviceManager.getArtifactGroupInfo(id);
 				List<ArtifactInfo> dbVersionInfos = artifactGroupInfo.getVersions();
 				// for selected version infos from ui
 				List<ArtifactInfo> selectedDBVersionInfos = new ArrayList<ArtifactInfo>();
 				for (ArtifactInfo versionInfo : dbVersionInfos) {
 					String versionId = versionInfo.getId();
 					if (selectedDatabase.getArtifactInfoIds().contains(versionId)) {
 						// Add selected version infos to list
 						selectedDBVersionInfos.add(versionInfo);
 					}
 				}
 				downloadInfo.getArtifactGroup().setVersions(selectedDBVersionInfos);
 				selectedDatabaseGroup.add(downloadInfo);
 			}
 			if (CollectionUtils.isNotEmpty(selectedDatabaseGroup)) {
 				String databaseGroup = gson.toJson(selectedDatabaseGroup);
 				applicationHandler.setSelectedDatabase(databaseGroup);
 			}
 		} else {
 			applicationHandler.setSelectedDatabase(null);
 		}
 	}
 
 	private void updateSubModulePomParentTagInfo(String newFolderDir, ApplicationInfo appInfo, String rootModulePath, String submodule) throws PhrescoException  {
 		try {
 			ProjectInfo rootProjInfo = Utility.getProjectInfo(rootModulePath, submodule);
 			String pomFile = rootProjInfo.getAppInfos().get(0).getPomFile();
 			File rootPom = new File(rootModulePath +  File.separator + pomFile);
 			if(!rootPom.exists()) {
 				rootPom = new File(rootModulePath + File.separator + newFolderDir + File.separator + pomFile);
 			}
 			if (rootPom.exists()) {
 				PomProcessor mainPomProcessor = new PomProcessor(rootPom);
 				Modules modules = mainPomProcessor.getModel().getModules();
 				if (modules != null && CollectionUtils.isNotEmpty(modules.getModule())) {
 					List<String> subModules = modules.getModule();
 					for (String subModule : subModules) {
 						ProjectInfo subProjInfo = Utility.getProjectInfo(rootModulePath, subModule);
 						String subPomFile = subProjInfo.getAppInfos().get(0).getPomFile();
 						File subModPom = new File(rootModulePath +  File.separator + subModule +  File.separator + subPomFile);
 						if(!subModPom.exists()) {
 							subModPom = new File(rootModulePath + File.separator + newFolderDir + File.separator + subModule + File.separator +  subPomFile);
 						}
 						if (subModPom.exists()) {
 							PomProcessor subPomProcessor = new PomProcessor(subModPom);
 							if(subPomProcessor.getParent() != null) {
 							subPomProcessor.getParent().setArtifactId(mainPomProcessor.getArtifactId());
 							subPomProcessor.getParent().setGroupId(mainPomProcessor.getGroupId());
 							subPomProcessor.getParent().setVersion(mainPomProcessor.getVersion());
 							subPomProcessor.getParent().setRelativePath("../");
 							subPomProcessor.save();
 							}
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void updateSubModuleNameInRootProjInfo(String rootModule, String oldSubModuleName, String newSubModuleName) throws PhrescoException {
 		try {
 			String rootModulePath = "";
 			if (StringUtils.isNotEmpty(rootModule)) {
 				rootModulePath = Utility.getProjectHome() + rootModule;
 			} else {
 				rootModulePath = Utility.getProjectHome() + newSubModuleName;
 			}
 			
 			ProjectInfo rootProjInfo = Utility.getProjectInfo(rootModulePath, "");
 			File rootModuleProjInfo =  new File(Utility.getProjectInfoPath(rootModulePath, ""));
 			ApplicationInfo rootAppInfo = rootProjInfo.getAppInfos().get(0);
 			List<ModuleInfo> modules = rootAppInfo.getModules();
 			List<ModuleInfo> newModuleInfos = new ArrayList<ModuleInfo>();
 			if (CollectionUtils.isNotEmpty(modules)) {
 				for (ModuleInfo module : modules) {
 					if (oldSubModuleName.equals(module.getCode())) {
 						module.setCode(newSubModuleName);
 					}
 					List<String> dependentModules = module.getDependentModules();
 				
 					if (CollectionUtils.isNotEmpty(dependentModules)&& dependentModules.contains(oldSubModuleName)) {
 						int itemIndex = dependentModules.indexOf(oldSubModuleName);
 						dependentModules.remove(itemIndex);
 						dependentModules.add(itemIndex, newSubModuleName);
 					}
 					module.setDependentModules(dependentModules);
 					newModuleInfos.add(module);
 				}
 			}
 			rootAppInfo.setModules(newModuleInfos);
 			rootProjInfo.setAppInfos(Collections.singletonList(rootAppInfo));
 			ProjectUtils.updateProjectInfo(rootProjInfo, rootModuleProjInfo);
 			updateRootPomModules(rootModule, oldSubModuleName, newSubModuleName, rootModulePath, rootProjInfo);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		} 
 	}
 	
 	private void updateRootPomModules(String rootModule, String oldSubModuleName, String newSubModuleName, String rootModulePath,
 			ProjectInfo projectinfo) throws PhrescoPomException, PhrescoException {
 		File docFolderLocation = Utility.getSourceFolderLocation(projectinfo, rootModulePath, newSubModuleName);
 		File rootPom = new File(docFolderLocation.getParent() + File.separator + Constants.POM_NAME);
 		if(rootPom.exists()) {
 		PomProcessor processor = new PomProcessor(rootPom);
 		Modules pomModules = processor.getModel().getModules();
 		if (pomModules != null && CollectionUtils.isNotEmpty(pomModules.getModule())) {
 			List<String> pomModulesList = pomModules.getModule();
 			Modules newModules = new Modules();
 			for (String pomModule : pomModulesList) {
 				if (oldSubModuleName.equals(pomModule)) {
 					newModules.getModule().add(newSubModuleName);
 				} else {
 					newModules.getModule().add(pomModule);
 				}
 			}
 			processor.getModel().setModules(newModules);
 			processor.save();
 		}
 	}
 	}
 	
 	private void updateRootModuleNameInSubProjectInfo(List<ModuleInfo> modules, String oldRootAppDirName, String newRootAppDirName) throws PhrescoException {
 		try {
 			if (CollectionUtils.isNotEmpty(modules) && !oldRootAppDirName.equals(newRootAppDirName))  {
 				for (ModuleInfo module : modules) {
 					File subModuleProjInfoFile = new File(Utility.getProjectInfoPath(Utility.getProjectHome() + newRootAppDirName, module.getCode()));
 					Gson gson = new Gson();
 					BufferedReader bufferedReader = new BufferedReader(new FileReader(subModuleProjInfoFile.getPath()));
 					Type type = new TypeToken<ProjectInfo>() {
 					}.getType();
 					ProjectInfo subModuleProjInfo = gson.fromJson(bufferedReader, type);
 					ApplicationInfo subModuleAppInfo = subModuleProjInfo.getAppInfos().get(0);
 					subModuleAppInfo.setRootModule(newRootAppDirName);
 					subModuleProjInfo.setAppInfos(Collections.singletonList(subModuleAppInfo));
 					ProjectUtils.updateProjectInfo(subModuleProjInfo, subModuleProjInfoFile);
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private Map embedApplication(Map json, ProjectInfo projectInfo, ServiceManager serviceManager, ProjectManager projectManager, String appDirName) throws PhrescoException {
 		ProjectInfo parentProjectInfo = null;
 		Map<String, String> embedList = new HashMap<String, String>();
 		if (projectInfo != null) {
 			String technologyId = projectInfo.getAppInfos().get(0).getTechInfo().getId();
 			Technology technology = serviceManager.getTechnology(technologyId);
 			List<String> options = technology.getOptions();
 			if (options.contains(EMBED_APPLICATION)) {
 				List<String> applicableEmbedTechnology = technology.getApplicableEmbedTechnology();
 				parentProjectInfo = projectManager.getProject(projectInfo.getId(), projectInfo.getCustomerIds().get(0));
 				List<ApplicationInfo> allChildAppinfos = parentProjectInfo.getAppInfos();
 				for (ApplicationInfo allAppinfo : allChildAppinfos) {
 					String techId = allAppinfo.getTechInfo().getId();
 					if (applicableEmbedTechnology.contains(techId) && !allAppinfo.getAppDirName().equals(appDirName)) {
 						String EmbedName = allAppinfo.getName();
 						String EmbedAppId = allAppinfo.getId();
 						embedList.put(EmbedName, EmbedAppId);
 					}
 				}
 			}
 		}
 		json.put("embedList", embedList);
 		return json;
 	}
 	
 	private void updateFunctionalTestProperties(ApplicationInfo appInfo, ServiceManager serviceManager, String rootModule) throws PhrescoException {
 		FunctionalFrameworkInfo functionalFrameworkInfo = appInfo.getFunctionalFrameworkInfo();
 		if(functionalFrameworkInfo == null) {
 			return;
 		}
 		String techId = appInfo.getTechInfo().getId();
 		FunctionalFramework functionalFramework = serviceManager.getFunctionalFramework(functionalFrameworkInfo.getFrameworkIds(), techId);
 		List<FunctionalFrameworkProperties> funcFrameworkProperties = functionalFramework.getFuncFrameworkProperties();
 		if (CollectionUtils.isNotEmpty(funcFrameworkProperties)) {
 			FunctionalFrameworkProperties frameworkProperties = funcFrameworkProperties.get(0);
 			String testDir = frameworkProperties.getTestDir();
 			String testReportDir = frameworkProperties.getTestReportDir();
 			String testcasePath = frameworkProperties.getTestcasePath();
 			String testsuiteXpathPath = frameworkProperties.getTestsuiteXpathPath();
 			String adaptConfigPath = frameworkProperties.getAdaptConfigPath();
 			
 			try {
 				String subModuleName = "";
 				String rootModulePath = "";
 				if (StringUtils.isNotEmpty(rootModule)) {
 					rootModulePath = Utility.getProjectHome() + rootModule;
 					subModuleName = appInfo.getAppDirName();
 				}else {
 					rootModulePath = Utility.getProjectHome() + appInfo.getAppDirName();
 				}
 				File pomFile = Utility.getPomFileLocation(rootModulePath, subModuleName);
 				PomProcessor pomProcessor = new PomProcessor(pomFile);
                 pomProcessor.setProperty(Constants.POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL, functionalFramework.getName());
 				pomProcessor.setProperty(Constants.POM_PROP_KEY_FUNCTEST_DIR, testDir);
 				pomProcessor.setProperty(Constants.POM_PROP_KEY_FUNCTEST_RPT_DIR, testReportDir);
 				pomProcessor.setProperty(Constants.POM_PROP_KEY_FUNCTEST_TESTCASE_PATH, testcasePath);
 				pomProcessor.setProperty(Constants.POM_PROP_KEY_FUNCTEST_TESTSUITE_XPATH, testsuiteXpathPath);
 				pomProcessor.setProperty(Constants.PHRESCO_FUNCTIONAL_TEST_ADAPT_DIR, adaptConfigPath);
 				pomProcessor.save();
 			} catch (PhrescoPomException e) {
 				throw new PhrescoException(e);
 			}
 		}
 	}
 	
 	/**
 	 * Edits the application.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the response
 	 */
 	@GET
 	@Path(REST_API_EDIT_APPLICATION)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response editApplication(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_TYPE_MODULE) String module) {
 		StringBuilder projectInfoFilePath = new StringBuilder(Utility.getProjectHome());
 		projectInfoFilePath.append(appDirName);
 		ResponseInfo<JSONObject> responseData = new ResponseInfo<JSONObject>();
 		Map json = new HashMap();
 		try {
 			ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 			ProjectInfo projectInfo = Utility.getProjectInfo(projectInfoFilePath.toString(), module);
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			json = embedApplication(json, projectInfo, serviceManager, projectManager, appDirName);			
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200009;
 			json.put("projectInfo", projectInfo);
 			ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, null,
 					json, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER)
 							.build();
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210007;
 			ResponseInfo<ProjectInfo> finalOutput = responseDataEvaluation(responseData, e,
 					null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER)
 					.build();
 		}
 	}
 
 	/**
 	 * Deleteproject.
 	 *
 	 * @param appDirnames the app dirnames
 	 * @return the response
 	 */
 	@DELETE
 	@Path(REST_API_PROJECT_DELETE)
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response deleteproject(DeleteProjectInfo deleteProjectInfo , @Context HttpServletRequest request) {
 		BufferedReader reader = null;
 		ResponseInfo responseData = new ResponseInfo();
 		String rootModulePath = "";
 		String subModule = "";
 		ProjectInfo projectinfo = null;
 		try {
 			ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 			List<String> appDirNames = deleteProjectInfo.getAppDirNames();
 			String actionType = deleteProjectInfo.getActionType();
 			String rootModule = deleteProjectInfo.getRootModule();
 			if (CollectionUtils.isNotEmpty(appDirNames)) {
 				for (String appDirName : appDirNames) {
 					if (REQ_MODULE.equals(actionType) && StringUtils.isNotEmpty(rootModule)) {
 							rootModulePath = Utility.getProjectHome() + rootModule;
 							subModule = appDirName;
 					} else {
 						rootModulePath = Utility.getProjectHome() + appDirName;
 					}
 					String dotPhrescoFolderPath = Utility.getDotPhrescoFolderPath(rootModulePath, subModule);
 					File getpomFileLocation = Utility.getPomFileLocation(rootModulePath, subModule);
 					if (getpomFileLocation != null) {
 					projectinfo = Utility.getProjectInfo(rootModulePath, subModule);
 						StringBuilder sb = new StringBuilder(dotPhrescoFolderPath).append(File.separator).append(
 								RUNAGNSRC_INFO_FILE);
 						File file = new File(sb.toString());
 						if (file.exists()) {
 							Gson gson = new Gson();
 							reader = new BufferedReader(new FileReader(file));
 							ConfigurationInfo configInfo = gson.fromJson(reader, ConfigurationInfo.class);
 							int port = Integer.parseInt(configInfo.getServerPort());
 							boolean connectionAlive = Utility.isConnectionAlive(HTTP_PROTOCOL, LOCALHOST, port);
 							if (connectionAlive) {
 								status = RESPONSE_STATUS_FAILURE;
 								errorCode = PHR210011;
 								ResponseInfo finalOutput = responseDataEvaluation(responseData, null,
 										null, status, errorCode);
 								return Response.status(Status.OK).entity(finalOutput).header(
 										ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 							}
 						}
 					}
 					
 					if(projectinfo.isIntegrationTest()) {
 						File integrationFolder = new File(Utility.getProjectHome() + projectinfo.getProjectCode() + "-integrationtest");
 						if (integrationFolder.exists()) {
 							 FileUtil.delete(integrationFolder);
 						}
 					}
 //					String applicationHome = FrameworkServiceUtil.getApplicationHome(appDirName);
 					Utility.killProcess(getpomFileLocation.getParent(), REQ_EQLIPSE); // need to handle for build version
 					deleteProjectFromSonar(getpomFileLocation, request);
 				}
 			}
 			
 			if (REQ_MODULE.equals(actionType) && StringUtils.isNotEmpty(rootModule)) {
 				removeAllEntriesOfModuleToBeDeleted(deleteProjectInfo.getDependents(), deleteProjectInfo.getAppDirNames().get(0), rootModulePath);
 			}
 			boolean status  = projectManager.delete(deleteProjectInfo);
 			if(status && actionType.equals(APPLICATION_PROJECT)) {
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_SUCCESS, PHR200010);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 			} else if (status && actionType.equals(REQ_APPLICATION)) {
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_SUCCESS, PHR200026);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 			} else if (status && actionType.equals(REQ_MODULE)) { 
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_SUCCESS, PHR200026);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 			} else {
 				ResponseInfo finalOutput = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_FAILURE, PHR210047);
 				return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 			}
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210012;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, e, null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 					"*").build();
 		} catch (FileNotFoundException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210013;
 			ResponseInfo finalOutput = responseDataEvaluation(responseData, e, null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 					"*").build();
 		}
 	}
 
 	private void deleteProjectFromSonar(File pomFile, HttpServletRequest request)  throws PhrescoException {
 		try {
 			if (pomFile.exists() && request != null) {
 				FrameworkUtil frameworkUtil = new FrameworkUtil(request);
 				String sonarHomeURL = frameworkUtil.getSonarHomeURL() + "/";
 				if (!sonarHomeURL.endsWith(SONAR)) {
 					sonarHomeURL = sonarHomeURL +  SONAR;
 				}
 
 				Sonar sonar = new Sonar(new HttpClient4Connector(new Host(sonarHomeURL, SONAR_USERNAME , SONAR_PASSWORD)));
 
 				PomProcessor pomProcessor = new PomProcessor(pomFile);
 				String groupId = pomProcessor.getGroupId();
 				String artifactId = pomProcessor.getArtifactId();
 
 				List<String> validateAgainst = new ArrayList<String>();
 				Profile javaProfile = pomProcessor.getProfile(JAVA_PROFILE);
 				if (javaProfile != null) {
 					validateAgainst.add(JAVA_PROFILE);
 				}
 				Profile jsProfile = pomProcessor.getProfile(JS_PROFILE);
 				if (jsProfile != null) {
 					validateAgainst.add(JS_PROFILE);
 				}
 
 				Profile webProfile = pomProcessor.getProfile(WEB_PROFILE);
 				if (webProfile != null) {
 					validateAgainst.add(WEB_PROFILE);
 				}
 
 				Profile htmlProfile = pomProcessor.getProfile(HTML_PROFILE);
 				if (htmlProfile != null) {
 					validateAgainst.add(HTML_PROFILE);
 				}
 
 				if (CollectionUtils.isEmpty(validateAgainst)) {
 					validateAgainst.add(JAVA_PROFILE);
 				}
 
 				for (String string : validateAgainst) {
 					final String projectKey = groupId + SONAR_COLON + artifactId + SONAR_COLON + string;
 					boolean urlExists = checkUrlExists(sonarHomeURL + DASHBORAD_INDEX + projectKey);
 					if (urlExists) {
 						DeleteQuery query = new DeleteQuery() {
 							@Override
 							public String getUrl() {
 								return SONAR_API_URL + projectKey;
 							}
 						};
 						sonar.delete(query);
 					}
 				}
 			}
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 
 	}
 
 	private boolean checkUrlExists(String sonarPath) {
 		boolean urlExists = false;
 		try {
 			HttpURLConnection httpURLConnection = null;
 			try {
 				java.net.URL url = new java.net.URL(sonarPath);
 				URLConnection urlConnection = url.openConnection();
 				HttpURLConnection.setFollowRedirects(false);  
 				httpURLConnection = (HttpURLConnection) urlConnection;  
 				httpURLConnection.setRequestMethod(HEAD_REVISION);  
 				if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {  
 					urlExists = true;	
 				} else {  
 					urlExists = false;  
 				}  
 			} catch (MalformedURLException e) {
 				urlExists = false;  
 			} catch (ProtocolException e) {
 				urlExists = false;  
 			} catch (IOException e) {
 				urlExists = false;  
 			} 
 		} catch (Exception e) {
 			urlExists = false;  
 		}
 		return urlExists;
 	}
 
 	private void removeAllEntriesOfModuleToBeDeleted(List<String> dependents, String moduleNameToDelete, String rootModulePath) throws PhrescoException {
 		try {
 			//To delete entries in root project
 			removeModuleInfoFromRootProject(moduleNameToDelete, rootModulePath);
 			if (CollectionUtils.isNotEmpty(dependents)) {
 				//To remove dependency entry in other sub module's pom
 				removeDependencies(dependents, moduleNameToDelete, rootModulePath);
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private void removeDependencies(List<String> dependents, String moduleNameToDelete, String rootModulePath) throws PhrescoException, PhrescoPomException {
		ProjectInfo projectInfo = Utility.getProjectInfo(rootModulePath, moduleNameToDelete);
 		File docFolderLocation = Utility.getSourceFolderLocation(projectInfo, rootModulePath, moduleNameToDelete);
 		File currentPomFile = new File(docFolderLocation + File.separator + Constants.POM_NAME);
 		if (currentPomFile.exists()) {
 			PomProcessor processor = new PomProcessor(currentPomFile);
 			String groupId = processor.getGroupId();
 			String artifactId = processor.getArtifactId();
 			for (String dependent : dependents) {
				ProjectInfo subProjectInfo = Utility.getProjectInfo(rootModulePath, moduleNameToDelete);
 				File subFolderLocation = Utility.getSourceFolderLocation(subProjectInfo, rootModulePath, dependent);
 				File dependentPomFile = new File(subFolderLocation + File.separator + Constants.POM_NAME);
 				if (dependentPomFile.exists()) {
 					PomProcessor proc = new PomProcessor(dependentPomFile);
 					Dependency dependency = proc.getDependency(groupId, artifactId);
 					if (dependency != null) {
 						proc.deleteDependency(groupId, artifactId, "");
 						proc.save();
 					}
 				}
 			}
 		}
 	}
 
 	private void removeModuleInfoFromRootProject(String moduleNameToDelete, String rootModulePath) throws PhrescoException {
 		try {
 			
 			ProjectInfo rootProjectInfo = Utility.getProjectInfo(rootModulePath, "");
 			String rootProjectInfoPath = Utility.getProjectInfoPath(rootModulePath, "");
 			File rootProjectInfoFile = new File(rootProjectInfoPath);
 			ApplicationInfo rootAppInfo = rootProjectInfo.getAppInfos().get(0);
 			File docFolderLocation = Utility.getSourceFolderLocation(rootProjectInfo, rootModulePath, "");
 			File rootPom = new File(docFolderLocation + File.separator + Constants.POM_NAME);
 			//To remove modules entry in root pom
 			if (rootPom.exists()) {
 				PomProcessor processor = new PomProcessor(rootPom);
 				Modules modules = processor.getModel().getModules();
 				if (modules != null && CollectionUtils.isNotEmpty(modules.getModule()) && modules.getModule().contains(moduleNameToDelete)) {
 					processor.removeModule(moduleNameToDelete);
 					processor.save();
 				}
 			}
 			//To remove current module info and update dependent modules info in root project's project.info
 			List<ModuleInfo> modules = rootAppInfo.getModules();
 			List<ModuleInfo> newModuleInfos = new ArrayList<ModuleInfo>();
 			if (CollectionUtils.isNotEmpty(modules)) {
 				for (ModuleInfo module : modules) {
 					if (!module.getCode().equals(moduleNameToDelete)) {
 						List<String> dependentModules = module.getDependentModules();
 						if (CollectionUtils.isNotEmpty(dependentModules)&& dependentModules.contains(moduleNameToDelete)) {
 							int itemIndex = dependentModules.indexOf(moduleNameToDelete);
 							dependentModules.remove(itemIndex);
 							module.setDependentModules(dependentModules);
 						}
 						newModuleInfos.add(module);
 					} 
 				}
 				rootAppInfo.setModules(newModuleInfos);
 				ProjectUtils.updateProjectInfo(rootProjectInfo, rootProjectInfoFile);
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	/**
 	 * Gets the permission.
 	 *
 	 * @param userId the user id
 	 * @return the permission
 	 */
 	@GET
 	@Path(REST_API_GET_PERMISSION)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getPermission(@QueryParam(REST_QUERY_USERID) String userId) {
 		ResponseInfo<UserPermissions> responseData = new ResponseInfo<UserPermissions>();
 		try {
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			User user = ServiceManagerImpl.USERINFO_MANAGER_MAP.get(userId);
 			FrameworkUtil futil = new FrameworkUtil();
 			UserPermissions userPermissions = futil.getUserPermissions(serviceManager, user);
 			status = RESPONSE_STATUS_SUCCESS;
 			successCode = PHR200011;
 			ResponseInfo<UserPermissions> finalOutput = responseDataEvaluation(responseData, null,
 					userPermissions, status, successCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 		} catch (PhrescoWebServiceException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210014;
 			ResponseInfo<UserPermissions> finalOutput = responseDataEvaluation(responseData, e,
 					null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 					"*").build();
 		} catch (PhrescoException e) {
 			status = RESPONSE_STATUS_ERROR;
 			errorCode = PHR210014;
 			ResponseInfo<UserPermissions> finalOutput = responseDataEvaluation(responseData, e,
 					null, status, errorCode);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 					"*").build();
 		}
 	}
 	
 	/**
 	 * Get dependents of particular sub module application
 	 */
 	@GET
 	@Path(REST_API_MODULE_DEPENDENTS)
 	@Produces(MediaType.APPLICATION_JSON)
 		public Response getModuleDependents(@QueryParam(REST_QUERY_MODULE_NAME) String moduleName, @QueryParam(REST_QUERY_ROOT_MODULE_NAME) String rootModule) throws PhrescoException {
 		List<String> dependents = new ArrayList<String>();
 		ResponseInfo<List<String>> responseData = new ResponseInfo<List<String>>();
 		try {
 			ApplicationInfo rootAppInfo = FrameworkServiceUtil.getApplicationInfo(rootModule);
 			if (rootAppInfo != null && CollectionUtils.isNotEmpty(rootAppInfo.getModules())) {
 				File currentModule = new File(Utility.getProjectHome() + rootModule + File.separator + moduleName);
 				ApplicationInfo currentModuleAppInfo = ProjectUtils.getProjectInfoFile(currentModule).getAppInfos().get(0);
 				String currentModulePomName = Utility.getPomFileName(currentModuleAppInfo);
 				File currentModulePom = new File(currentModule.getPath() + File.separator + currentModulePomName);
 				if (currentModulePom.exists()) {
 					PomProcessor proc = new PomProcessor(currentModulePom);
 					String groupId = proc.getGroupId();
 					String artifactId = proc.getArtifactId();
 					for (ModuleInfo module : rootAppInfo.getModules()) {
 						if (!moduleName.equals(module.getCode())) {
 							File otherModuleDir = new File(Utility.getProjectHome() + rootModule + File.separator + module.getCode());
 							ApplicationInfo otherModuleAppInfo = ProjectUtils.getProjectInfoFile(otherModuleDir).getAppInfos().get(0);
 							String pomFileName = Utility.getPomFileName(otherModuleAppInfo);
 							File pom = new File (otherModuleDir.getPath() + File.separator + pomFileName);
 							if (pom.exists()) {
 								PomProcessor processor = new PomProcessor(pom);
 								Dependency dependency = processor.getDependency(groupId, artifactId);
 								if (dependency != null) {
 									dependents.add(module.getCode());
 								}
 							}
 						}
 					}
 				}	
 			}
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, null,
 					dependents, RESPONSE_STATUS_SUCCESS, PHR210051);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,ALL_HEADER).build();
 		} catch (Exception e) {
 			ResponseInfo<List<String>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR210052);
 			return Response.status(Status.OK).entity(finalOutput).header(ACCESS_CONTROL_ALLOW_ORIGIN,
 					"*").build();
 		}
 	}
 	
 	
 	/**
 	 * Sort by date to latest.
 	 *
 	 * @return the comparator
 	 */
 	public Comparator sortByDateToLatest() {
 		return new Comparator() {
 			public int compare(Object firstObject, Object secondObject) {
 				ProjectInfo projectInfo1 = (ProjectInfo) firstObject;
 				ProjectInfo projectInfo2 = (ProjectInfo) secondObject;
 				return projectInfo1.getCreationDate().compareTo(projectInfo2.getCreationDate()) * -1;
 			}
 		};
 	}
 
 	/**
 	 * Delete sql folder.
 	 *
 	 * @param applicationInfo the application info
 	 * @param selectedDatabases the selected databases
 	 * @param serviceManager the service manager
 	 * @param oldAppDirName the old app dir name
 	 * @throws PhrescoException the phresco exception
 	 */
 	public void deleteSqlFolder(ProjectInfo projectinfo, List<ArtifactGroupInfo> selectedDatabases,
 			ServiceManager serviceManager, String rootModulePath,String subModuleName) throws PhrescoException {
 		try {
 			File sqlPath = null;
 			File pomFile = Utility.getPomFileLocation(rootModulePath, subModuleName);
 				PomProcessor pom = new PomProcessor(pomFile);
 				String sql = pom.getProperty(PHRESCO_SQL_PATH);
 				File docFolderLocation = Utility.getSourceFolderLocation(projectinfo, rootModulePath, subModuleName);
 				if(docFolderLocation.exists()) {
 					sqlPath = new File(docFolderLocation + sql);
 				}
 			List<String> dbListToDelete = new ArrayList<String>();
 			List<ArtifactGroupInfo> existingDBList = projectinfo.getAppInfos().get(0).getSelectedDatabases();
 			if (CollectionUtils.isEmpty(existingDBList)) {
 				return;
 			}
 			for (ArtifactGroupInfo artifactGroupInfo : existingDBList) {
 				String oldArtifactGroupId = artifactGroupInfo.getArtifactGroupId();
 				for (ArtifactGroupInfo newArtifactGroupInfo : selectedDatabases) {
 					String newArtifactid = newArtifactGroupInfo.getArtifactGroupId();
 					if (newArtifactid.equals(oldArtifactGroupId)) {
 						checkForVersions(newArtifactid, oldArtifactGroupId, sqlPath, serviceManager);
 						break;
 					} else {
 						DownloadInfo downloadInfo = serviceManager.getDownloadInfo(oldArtifactGroupId);
 						dbListToDelete.add(downloadInfo.getName());
 					}
 				}
 			}
 			for (String dbVersion : dbListToDelete) {
 				File dbVersionFolder = new File(sqlPath, dbVersion.toLowerCase());
 				FileUtils.deleteDirectory(dbVersionFolder.getParentFile());
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Check for versions.
 	 *
 	 * @param newArtifactid the new artifactid
 	 * @param oldArtifactGroupId the old artifact group id
 	 * @param oldAppDirName the old app dir name
 	 * @param serviceManager the service manager
 	 * @throws PhrescoException the phresco exception
 	 */
 	private void checkForVersions(String newArtifactid, String oldArtifactGroupId, File sqlFilePath,
 			ServiceManager serviceManager) throws PhrescoException {
 		try {
 			DownloadInfo oldDownloadInfo = serviceManager.getDownloadInfo(oldArtifactGroupId);
 			DownloadInfo newDownloadInfo = serviceManager.getDownloadInfo(newArtifactid);
 			List<ArtifactInfo> oldVersions = oldDownloadInfo.getArtifactGroup().getVersions();
 			List<ArtifactInfo> newVersions = newDownloadInfo.getArtifactGroup().getVersions();
 			for (ArtifactInfo artifactInfo : oldVersions) {
 				for (ArtifactInfo newartifactInfo : newVersions) {
 					if (!newartifactInfo.getVersion().equals(artifactInfo.getVersion())) {
 						String deleteVersion = "/" + oldDownloadInfo.getName() + "/" + artifactInfo.getVersion();
 						FileUtils.deleteDirectory(new File(sqlFilePath, deleteVersion));
 					}
 				}
 			}
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * Gets the removed modules.
 	 *
 	 * @param appInfo the app info
 	 * @param jsonData the json data
 	 * @param serviceManager the service manager
 	 * @return the removed modules
 	 * @throws PhrescoException the phresco exception
 	 */
 	private List<ArtifactGroup> getRemovedModules(ApplicationInfo appInfo, List<SelectedFeature> jsonData,
 			ServiceManager serviceManager) throws PhrescoException {
 		List<String> selectedFeaturesId = appInfo.getSelectedModules();
 		List<String> selectedJSLibsId = appInfo.getSelectedJSLibs();
 		List<String> selectedComponentsId = appInfo.getSelectedComponents();
 		List<String> newlySelectedModuleGrpIds = new ArrayList<String>();
 		if (CollectionUtils.isNotEmpty(jsonData)) {
 			for (SelectedFeature obj : jsonData) {
 				newlySelectedModuleGrpIds.add(obj.getModuleId());
 			}
 		}
 		List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
 		if (CollectionUtils.isNotEmpty(selectedFeaturesId)) {
 			addArtifactGroups(selectedFeaturesId, newlySelectedModuleGrpIds, artifactGroups, serviceManager);
 		}
 		if (CollectionUtils.isNotEmpty(selectedJSLibsId)) {
 			addArtifactGroups(selectedJSLibsId, newlySelectedModuleGrpIds, artifactGroups, serviceManager);
 		}
 		if (CollectionUtils.isNotEmpty(selectedComponentsId)) {
 			addArtifactGroups(selectedComponentsId, newlySelectedModuleGrpIds, artifactGroups, serviceManager);
 		}
 		return artifactGroups;
 	}
 
 	/**
 	 * Adds the artifact groups.
 	 *
 	 * @param selectedFeaturesIds the selected features ids
 	 * @param gson the gson
 	 * @param newlySelectedModuleGrpIds the newly selected module grp ids
 	 * @param artifactGroups the artifact groups
 	 * @param serviceManager the service manager
 	 * @throws PhrescoException the phresco exception
 	 */
 	private void addArtifactGroups(List<String> selectedFeaturesIds, List<String> newlySelectedModuleGrpIds,
 			List<ArtifactGroup> artifactGroups, ServiceManager serviceManager) throws PhrescoException {
 		for (String selectedfeatures : selectedFeaturesIds) {
 			ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(selectedfeatures);
 			if (!newlySelectedModuleGrpIds.contains(artifactInfo.getArtifactGroupId())) {
 				ArtifactGroup artifactGroupInfo = serviceManager
 						.getArtifactGroupInfo(artifactInfo.getArtifactGroupId());
 				artifactGroups.add(artifactGroupInfo);
 			}
 		}
 	}
 	
 	private ResponseInfo validateProject(ProjectInfo projectinfo) throws PhrescoException {
 		ResponseInfo response = null;
 		ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 		List<ProjectInfo> discoveredProjectInfos = projectManager.discover();
 		if(!FrameworkUtil.isCharacterExists(projectinfo.getName().trim())) {
 			response = new ResponseInfo();
 			response.setStatus(RESPONSE_STATUS_FAILURE);
 			response.setResponseCode(PHR210040);
 			return response;
 		}
 		for (ProjectInfo projectInfos : discoveredProjectInfos) {
 			if(StringUtils.isNotEmpty(projectinfo.getName().trim()) && projectInfos.getName().trim().equalsIgnoreCase(projectinfo.getName().trim())) {
 				response = new ResponseInfo();
 				response.setStatus(RESPONSE_STATUS_FAILURE);
 				response.setResponseCode(PHR210041);
 				return response;
 			}
 			if(StringUtils.isNotEmpty(projectinfo.getProjectCode().trim())) {
 				if(projectinfo.getProjectCode().trim().equalsIgnoreCase(projectInfos.getProjectCode().trim())) {
 					response = new ResponseInfo();
 					response.setStatus(RESPONSE_STATUS_FAILURE);
 					response.setResponseCode(PHR210042);
 					return response;
 				}
 			}
 			List<ApplicationInfo> appInfos = projectinfo.getAppInfos();
 			List<ApplicationInfo> discoveredAppInfos = projectInfos.getAppInfos();
 			for(int i = 0; i < appInfos.size(); i++) {
 				for(int j = 0; j < discoveredAppInfos.size(); j++) {
 					if(appInfos.get(i).getCode().equalsIgnoreCase(discoveredAppInfos.get(j).getCode())) {
 						response = new ResponseInfo();
 						response.setStatus(RESPONSE_STATUS_FAILURE);
 						response.setResponseCode(PHR210043);
 						response.setData(appInfos.get(i).getCode());
 						return response;
 					}
 					if(appInfos.get(i).getAppDirName().equalsIgnoreCase(discoveredAppInfos.get(j).getAppDirName())) {
 						response = new ResponseInfo();
 						response.setStatus(RESPONSE_STATUS_FAILURE);
 						response.setResponseCode(PHR210044);
 						return response;
 					}
 				}
 			}
 		}
 		return response;
 	}
 
 	private ResponseInfo validateAppInfo(String oldAppDirName, ApplicationInfo appInfo, String rootModule) throws PhrescoException {
 		ResponseInfo response = null;
 		ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 		List<ProjectInfo> discoveredProjectInfos = new ArrayList<ProjectInfo>(); 
 		if (StringUtils.isNotEmpty(rootModule)) {
 			discoveredProjectInfos = projectManager.discoverFromRootModule(rootModule);
 		} else {
 			discoveredProjectInfos = projectManager.discover();
 		}
 		
 		for (ProjectInfo projectInfo : discoveredProjectInfos) {
 			List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 			for (int i = 0; i < appInfos.size(); i++) {
 				if(appInfo.getAppDirName().equals(oldAppDirName)) {
 					continue;
 				} else if(appInfo.getAppDirName().equalsIgnoreCase(appInfos.get(i).getAppDirName())) {
 					response = new ResponseInfo();
 					response.setStatus(RESPONSE_STATUS_FAILURE);
 					response.setResponseCode(PHR210044);
 					return response;
 				}
 			}
 		}
 		return response;
 	}
 	
 	private void handleDependencies(String tempFolderPath, ProjectInfo projectInfo, ProjectInfo availableProjectInfo) throws PhrescoException {
 		List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 		for (ApplicationInfo applicationInfo : appInfos) {
 			List<ModuleInfo> modules = applicationInfo.getModules();
 			if (CollectionUtils.isNotEmpty(modules)) {
 				for (ModuleInfo module : modules) {
 					List<String> dependentModules = module.getDependentModules();
 					if (availableProjectInfo != null) {
 						List<ApplicationInfo> availableAppInfos = availableProjectInfo.getAppInfos();
 						for (ApplicationInfo availableAppInfo : availableAppInfos) {
 							List<ModuleInfo> availableModules = availableAppInfo.getModules();
 							if (availableAppInfo.getAppDirName().equalsIgnoreCase(applicationInfo.getAppDirName()) && CollectionUtils.isNotEmpty(availableModules)) {
 								for (ModuleInfo availableModule : availableModules) {
 									if (availableModule.getCode().equals(module.getCode())) {
 										List<String> availableDependentModules = availableModule.getDependentModules();
 										if (CollectionUtils.isNotEmpty(availableDependentModules)) {
 											for (String availableDependentModule : availableDependentModules) {
 												if (!dependentModules.contains(availableDependentModule)) {
 													removeModuleDependency(availableAppInfo.getAppDirName(), module.getCode(), availableDependentModule);
 												}
 											}
 										}
 									}
 								}
 								break;
 							}
 						}
 					}
 					if (CollectionUtils.isNotEmpty(dependentModules)) {
 						for (String appCode : dependentModules) {
 //							String depApp = getApplicationInfo(appCode, projectInfo.getAppInfos());
 							File depPomFile = new File(tempFolderPath + applicationInfo.getCode() + File.separator + appCode
 									+ File.separator + "pom.xml");
 							String packageType = getPackageOfDependentSubModule(depPomFile);
 							File file = new File(tempFolderPath +  applicationInfo.getCode() + File.separator + module.getCode()
 									+ File.separator + "pom.xml");
 							addDependency(appCode, file, projectInfo.getVersion(), projectInfo.getGroupId(), packageType);
 						}
 					}
 				}
 			}
 //			if (CollectionUtils.isNotEmpty(applicationInfo.getModules())) {
 //				createModuleDependencies(applicationInfo.getModules(), tempFolderPath, projectInfo, applicationInfo
 //						.getCode());
 //			}
 		}
 	}
 	
 	private void removeModuleDependency(String appDirName, String moduleName, String moduleToDelete) throws PhrescoException {
 		try {
 			File depndPomFile = Utility.getPomFileLocation(Utility.getProjectHome() + appDirName, moduleName);
 			PomProcessor depndPomProcessor = new PomProcessor(new File(depndPomFile.getParent(), POM_FILE));
 			File pomFile = Utility.getPomFileLocation(Utility.getProjectHome() + appDirName, moduleName);
 			PomProcessor pomProcessor = new PomProcessor(new File(pomFile.getParent(), POM_FILE));
 			pomProcessor.deleteDependency(depndPomProcessor.getGroupId(), moduleToDelete, "");
 			pomProcessor.save();
 		} catch (PhrescoException e) {
 			throw e;
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private String getPackageOfDependentSubModule(File depPomFile) throws PhrescoException {
 		try {
 			PomProcessor processor = new PomProcessor(depPomFile);
 			if (StringUtils.isNotEmpty(processor.getPackage())) {
 				return processor.getPackage();
 			}
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 		return "";
 	}
 
 	private void addDependency(String depAppCode, File pomFile, String version, String groupId, String packageType) throws PhrescoException {
 		try {
 			PomProcessor processor = new PomProcessor(pomFile);
 			processor.addDependency(groupId, depAppCode, version, "", packageType , "");
 			processor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 }
