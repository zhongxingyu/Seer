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
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.dom.DOMSource;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.quartz.CronExpression;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.photon.phresco.api.ApplicationProcessor;
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.api.NonEnvConfigManager;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.ResponseCodes;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.CertificateInfo;
 import com.photon.phresco.commons.model.CoreOption;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.FeatureConfigure;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.PropertyTemplate;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.commons.model.SettingsTemplate;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.api.ProjectManager;
 import com.photon.phresco.framework.commons.FileBrowseInfo;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.model.AddCertificateInfo;
 import com.photon.phresco.framework.model.CronExpressionInfo;
 import com.photon.phresco.framework.model.RemoteCertificateInfo;
 import com.photon.phresco.framework.model.TemplateInfo;
 import com.photon.phresco.framework.rest.api.util.FrameworkServiceUtil;
 import com.photon.phresco.impl.ConfigManagerImpl;
 import com.photon.phresco.impl.NonEnvConfigManagerImpl;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.PhrescoDynamicLoader;
 import com.photon.phresco.util.ServiceConstants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.sun.jersey.api.client.ClientResponse.Status;
 
 /**
  * The Class ConfigurationService.
  */
 @Path("/configuration")
 public class ConfigurationService extends RestBase implements FrameworkConstants, ServiceConstants, ResponseCodes {
 	
 	/** The Constant S_LOGGER. */
 	private static final Logger S_LOGGER = Logger.getLogger(ConfigurationService.class);
 	
 	/** The is_debug enabled. */
 	private static Boolean is_debugEnabled = S_LOGGER.isDebugEnabled();
 	
 	/**
 	 * Adds the environment.
 	 *
 	 * @param appDirName the app dir name
 	 * @param environments the environments
 	 * @return the response
 	 * @throws PhrescoException 
 	 */
 	@POST
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response addEnvironment(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, 
 			@QueryParam(REST_QUERY_MODULE_NAME) String moduleName, List<Environment> environments,
 			@QueryParam(REST_QUERY_PROJECT_CODE) String projectCode, @QueryParam(REST_QUERY_PROJECTID) String projectId, @QueryParam(REST_QUERY_CUSTOMERID) String customerId) throws PhrescoException {
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		ResponseInfo<Environment> responseData = new ResponseInfo<Environment>();
 		try {
 			boolean duplicateEnvironment = validateEnvironment(projectId, customerId, environments, appDirName);	
 			if (duplicateEnvironment) {
 				ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null,
 						duplicateEnvironment, ERROR_DUPLICATE_NAME_IN_CONFIGURATIONS, PHR610042);
 				return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 			
 			String configFileDir = "";
 			if (StringUtils.isNotEmpty(projectCode)) {
 				configFileDir = FrameworkServiceUtil.getGlobalConfigFileDir(projectCode);
 			} else if (StringUtils.isNotEmpty(appDirName)) {
 				configFileDir = FrameworkServiceUtil.getConfigFileDir(appDirName);
 			} else {
 				throw new ConfigurationException("Project Code Or AppDirName Should Not be Empty");
 			}
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFileDir));
 			if(configManager.getEnvironments().size() == environments.size()) {
 				configManager.addEnvironments(environments);
 				ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 						environments, RESPONSE_STATUS_SUCCESS, PHR600020);
 				return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 			configManager.addEnvironments(environments);
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 					environments, RESPONSE_STATUS_SUCCESS, PHR600001);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610001);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		}
 	}
 
 	/**
 	 * List environments.
 	 *
 	 * @param appDirName the app dir name
 	 * @param envName the env name
 	 * @return the response
 	 */
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response listEnvironments(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_ENV_NAME) String envName, @QueryParam("isEnvSpecific") String isEnvSpecific, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName,
 			@QueryParam("configName") String configName,@QueryParam(REST_QUERY_PROJECT_CODE) String projectCode) {
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		ResponseInfo<Environment> responseData = new ResponseInfo<Environment>();
 		try {
 			if (StringUtils.isNotEmpty(isEnvSpecific) && isEnvSpecific.equals("false")) {
 				String nonEnvConfigFile = FrameworkServiceUtil.getnonEnvConfigFileDir(appDirName);
 				NonEnvConfigManager nonConfigManager = new NonEnvConfigManagerImpl(new File(nonEnvConfigFile));
 				Configuration configurations = nonConfigManager.getConfiguration(configName);
 				ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, null,
 						configurations, RESPONSE_STATUS_SUCCESS, PHR600002);
 				return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 			
 			String configFileDir = "";
 			if (StringUtils.isNotEmpty(projectCode)) {
 				configFileDir = FrameworkServiceUtil.getGlobalConfigFileDir(projectCode);
 			} else if (StringUtils.isNotEmpty(appDirName)) {
 				configFileDir = FrameworkServiceUtil.getConfigFileDir(appDirName);
 			} else {
 				throw new ConfigurationException("Project Code Or AppDirName Should Not be Empty");
 			}
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFileDir));
 			if (StringUtils.isNotEmpty(envName)) {
 				List<Environment> environments = configManager.getEnvironments(Arrays.asList(envName));
 				if (CollectionUtils.isNotEmpty(environments)) {
 					Environment environment = environments.get(0);
 					ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 							environment, RESPONSE_STATUS_SUCCESS, PHR600002);
 					return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 				}
 			}
 			List<Environment> environments = configManager.getEnvironmentsAlone();
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 					environments, RESPONSE_STATUS_SUCCESS, PHR600002);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610002);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610026);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		}
 	}
 
 	/**
 	 * Gets the all environments.
 	 *
 	 * @param appDirName the app dir name
 	 * @return the all environments
 	 */
 	@GET
 	@Path("allEnvironments")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getAllEnvironments(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName, @QueryParam("isEnvSpecific") String isEnvSpecific, @QueryParam("configType") String configType,
 			@QueryParam(REST_QUERY_PROJECT_CODE) String projectCode) {
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		} 
 		ResponseInfo<Environment> responseData = new ResponseInfo<Environment>();
 		try {
 			
 			if (StringUtils.isNotEmpty(isEnvSpecific) && isEnvSpecific.equals("false")) {
 				String nonEnvConfigFile = FrameworkServiceUtil.getnonEnvConfigFileDir(appDirName);
 				NonEnvConfigManager nonConfigManager = new NonEnvConfigManagerImpl(new File(nonEnvConfigFile));
 				List<Configuration> configurations = nonConfigManager.getConfigurations(configType);
 				ResponseInfo<List<Configuration>> finalOuptut = responseDataEvaluation(responseData, null,
 						configurations, RESPONSE_STATUS_SUCCESS, PHR600002);
 				return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 			
 			String configFileDir = "";
 			if (StringUtils.isNotEmpty(projectCode)) {
 				configFileDir = FrameworkServiceUtil.getGlobalConfigFileDir(projectCode);
 			} else if (StringUtils.isNotEmpty(appDirName)) {
 				configFileDir = FrameworkServiceUtil.getConfigFileDir(appDirName);
 			} else {
 				throw new ConfigurationException("Project Code Or AppDirName Should Not be Empty");
 			}
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFileDir));
 			List<Environment> environments = configManager.getEnvironments();
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 					environments, RESPONSE_STATUS_SUCCESS, PHR600002);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610002);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<Configuration>> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610027);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		}
 	}
 
 
 	/**
 	 * Delete environment.
 	 *
 	 * @param appDirName the app dir name
 	 * @param envName the env name
 	 * @return the response
 	 * @throws ConfigurationException 
 	 */
 	@DELETE
 	@Path("/deleteEnv")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response deleteEnv(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_ENV_NAME) String envName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName,
 			@QueryParam(REST_QUERY_PROJECT_CODE) String projectCode) throws ConfigurationException {
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		String configFile = "";
 		if (StringUtils.isNotEmpty(projectCode)) {
 			configFile = FrameworkServiceUtil.getGlobalConfigFileDir(projectCode);
 		} else if (StringUtils.isNotEmpty(appDirName)) {
 			configFile = FrameworkServiceUtil.getConfigFileDir(appDirName);
 		} else {
 			throw new ConfigurationException("Project Code Or AppDirName Should Not be Empty");
 		}
 		ResponseInfo<Environment> responseData = new ResponseInfo<Environment>();
 		try {
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFile));
 			List<Environment> environments = configManager.getEnvironments(Arrays.asList(envName));
 			if (CollectionUtils.isNotEmpty(environments)) {
 				Environment environment = environments.get(0);
 				if (environment.isDefaultEnv()) {
 					ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 							null, RESPONSE_STATUS_FAILURE, PHR610003);
 					return Response.status(Status.OK).entity(finalOuptut).header(
 							"Access-Control-Allow-Origin", "*").build();
 				}
 				configManager.deleteEnvironment(envName);
 				ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 						null, RESPONSE_STATUS_SUCCESS, PHR600003);
 				return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 		} catch (ConfigurationException e) {
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610004);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 		}
 		ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 				null, RESPONSE_STATUS_FAILURE, PHR610005);
 		return Response.status(Status.OK).entity(finalOuptut)
 				.header("Access-Control-Allow-Origin", "*").build();
 	}
 	
 	
 	@DELETE
 	@Path("/deleteConfig")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response deleteConfiguraion(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName,
 			@QueryParam("configName") String configName) {
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		ResponseInfo<Environment> responseData = new ResponseInfo<Environment>();
 		try {
 			String nonEnvConfigFie = FrameworkServiceUtil.getnonEnvConfigFileDir(appDirName);
 			NonEnvConfigManager nonConfigManager = new NonEnvConfigManagerImpl(new File(nonEnvConfigFie));
 			nonConfigManager.deleteConfiguration(configName);
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, null,
 					null, RESPONSE_STATUS_SUCCESS, PHR600014);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<Environment> finalOuptut = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610023);
 			return Response.status(Status.OK).entity(finalOuptut)
 					.header("Access-Control-Allow-Origin", "*").build();
 		}
 	}
 
 	/**
 	 * Gets the settings template.
 	 * @param appDirName the app dir name
 	 * @param techId the tech id
 	 * @param userId the user id
 	 * @param type the type
 	 * @return the settings template
 	 */
 	@GET
 	@Path("/settingsTemplate")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getSettingsTemplate(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName,
 			@QueryParam(REST_QUERY_TECHID) String techId, @QueryParam(REST_QUERY_USERID) String userId,
 			@QueryParam(REST_QUERY_TYPE) String type,@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId) {
 		
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		ResponseInfo<List<SettingsTemplate>> responseData = new ResponseInfo<List<SettingsTemplate>>();
 		Map<String, Object> templateMap = new HashMap<String, Object>();
 		try {
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			if (serviceManager == null) {
 				ResponseInfo<List<SettingsTemplate>> finalOutput = responseDataEvaluation(responseData, null,
 						null, RESPONSE_STATUS_FAILURE, PHR610006);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 			SettingsTemplate settingsTemplate = null;
 			List<SettingsTemplate> settingsTemplates = new ArrayList<SettingsTemplate>();
 			if (StringUtils.isEmpty(techId)) {
 				List<String> techIds = getTechId(customerId, projectId);
 				 if (CollectionUtils.isNotEmpty(techIds)) {
 					 for (String tech_Id : techIds) {
 						settingsTemplate = serviceManager.getConfigTemplateByTechId(tech_Id, type);
 						settingsTemplates.add(settingsTemplate);
 					 }
 					 settingsTemplate  = removeDuplicatePropertyTemplates(settingsTemplates);
 				 }
 			} else {
 				settingsTemplate = serviceManager.getConfigTemplateByTechId(techId, type);
 			}
 			if (StringUtils.isEmpty(appDirName)) {
 				Map<String, Map<String, List<String>>> downloadInfos = new HashMap<String, Map<String,List<String>>>();
 				List<String> appDirNameList = getAppDirNameList(projectId, customerId);
 				if (CollectionUtils.isNotEmpty(appDirNameList)) {
 					Map<String, List<String>> nameMap = new HashMap<String, List<String>>();
 					for (String appdirName: appDirNameList) {
 						getDownloadInfo(serviceManager, appdirName, userId, type, nameMap);
 					}
 					templateMap.put("downloadInfo", nameMap);
 				}
 			} else {
 				Map<String, List<String>> nameMap = new HashMap<String, List<String>>();
 				getDownloadInfo(serviceManager, appDirName, userId, type, nameMap);
 				templateMap.put("downloadInfo", nameMap);
 			}
 			if (settingsTemplate != null) {
 				templateMap.put("settingsTemplate", settingsTemplate);
 			}
 			ResponseInfo<List<SettingsTemplate>> finalOutput = responseDataEvaluation(responseData, null,
 					templateMap, RESPONSE_STATUS_SUCCESS, PHR600004);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<SettingsTemplate>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610007);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 	
 	/**
 	 * Gets the config types.
 	 *
 	 * @param customerId the customer id
 	 * @param userId the user id
 	 * @param techId the tech id
 	 * @return the config types
 	 */
 	@GET
 	@Path("/types")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getConfigTypes(@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_TECHID) String techId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId) {
 		ResponseInfo<List<TemplateInfo>> responseData = new ResponseInfo<List<TemplateInfo>>();
 		try {
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			if (serviceManager == null) {
 				ResponseInfo<List<TemplateInfo>> finalOutput = responseDataEvaluation(responseData, null,
 						null, RESPONSE_STATUS_FAILURE, PHR610006);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin",
 						"*").build();
 			}
 			List<TemplateInfo> settingsTypes = new ArrayList<TemplateInfo>();
 			List<SettingsTemplate> settingsTemplates = new ArrayList<SettingsTemplate>();
 			if (StringUtils.isEmpty(techId)) {
 				 List<String> techIds  = getTechId(customerId, projectId);
 				 if (CollectionUtils.isNotEmpty(techIds)) {
 					 for (String tech_Id : techIds) {
 						 List<SettingsTemplate> configTemplates = serviceManager.getConfigTemplates("", tech_Id);
 						 settingsTemplates.addAll(configTemplates);
 					 }
 					 settingsTemplates = removeDuplicateSettingsTemplate(settingsTemplates);
 				 }
 			} else {
 				settingsTemplates = serviceManager.getConfigTemplates(customerId, techId);
 			}
 			if (CollectionUtils.isNotEmpty(settingsTemplates)) {
 				for (SettingsTemplate settingsTemplate : settingsTemplates) {
 					TemplateInfo template = new TemplateInfo();
 					template.setTemplateName(settingsTemplate.getName());
 					template.setFavourite(settingsTemplate.isFavourite());
 					template.setEnvSpecific(settingsTemplate.isEnvSpecific());
 					settingsTypes.add(template);
 				}
 			}
 			ResponseInfo<List<TemplateInfo>> finalOutput = responseDataEvaluation(responseData, null,
 					settingsTypes, RESPONSE_STATUS_SUCCESS, PHR600004);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<TemplateInfo>> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610007);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 	
 	
 	/**
 	 * @param projectId
 	 * @param customerId
 	 * @return
 	 * @throws PhrescoException
 	 */
 	private List<String> getAppDirNameList(String projectId, String customerId) throws PhrescoException {
 		try {
 			List<String> appDirNameList = new ArrayList<String>();
 			ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 			ProjectInfo project = projectManager.getProject(projectId, customerId);
 			if (project != null) {
 				List<ApplicationInfo> appInfos = project.getAppInfos();
 				if (CollectionUtils.isNotEmpty(appInfos)) {
 					for (ApplicationInfo appInfo : appInfos) {
 						String appDirName = appInfo.getAppDirName();
 						appDirNameList.add(appDirName);
 					}
 				}
 			}
 			return appDirNameList;
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	
 	/**
 	 * @param customerId
 	 * @param projectId
 	 * @return List<String>
 	 * @throws PhrescoException
 	 */
 	private List<String> getTechId(String customerId, String projectId) throws PhrescoException {
 		ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 		ProjectInfo project = projectManager.getProject(projectId, customerId);
 		if (project != null) {
 			List<ApplicationInfo> appInfos = project.getAppInfos();
 			List<String> techIds = new ArrayList<String>();
 			if (CollectionUtils.isNotEmpty(appInfos)) {
 				for (ApplicationInfo info : appInfos) {
 					String tech_Id = info.getTechInfo().getId();
 					techIds.add(tech_Id);
 				}
 			}
 			return techIds;
 		}
 		return null;
 	}
 
 	
 	/**
 	 * @param settingsTemplates
 	 * @return unique settingTemplate
 	 */
 	private List<SettingsTemplate> removeDuplicateSettingsTemplate(List<SettingsTemplate> settingsTemplates) {
 		if (CollectionUtils.isNotEmpty(settingsTemplates)) {
 			Map<String, SettingsTemplate> settinsTplsMap = new HashMap<String, SettingsTemplate>();
 			List<SettingsTemplate> settingsTpls = new ArrayList<SettingsTemplate>();
 			for (SettingsTemplate settingsTemplate : settingsTemplates) {
 				settinsTplsMap.put(settingsTemplate.getName() , settingsTemplate);
 			}
 			Collection<SettingsTemplate> values = settinsTplsMap.values();
 			for (SettingsTemplate settingsTemplate : values) {
 				settingsTpls.add(settingsTemplate);
 			}
 			return settingsTpls;
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * @param settingsTemplates
 	 * @return 
 	 */
 	private SettingsTemplate removeDuplicatePropertyTemplates(List<SettingsTemplate> settingsTemplates) {
 		if (CollectionUtils.isNotEmpty(settingsTemplates)) {
 			SettingsTemplate settingsTpl = settingsTemplates.get(0);
 			if (settingsTpl != null) {
 				List<PropertyTemplate> properties2 = settingsTpl.getProperties();
 				List<PropertyTemplate> properties3 = new ArrayList<PropertyTemplate>();
 				settingsTemplates.remove(settingsTpl);
 				for (SettingsTemplate settingsTemplate : settingsTemplates) {
 					if (settingsTemplate != null) {
 						List<PropertyTemplate> propertyTemplates  = settingsTemplate.getProperties();
 						if (CollectionUtils.isNotEmpty(propertyTemplates)){
 							for (PropertyTemplate propertyTemplate1 : propertyTemplates) {
 								if (CollectionUtils.isNotEmpty(properties2)) {
 									for (PropertyTemplate propertyTemplate2 : properties2) {
 										if (!propertyTemplate1.getName().equalsIgnoreCase(propertyTemplate2.getName())) {
 											properties3.add(propertyTemplate1);
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 				properties3.addAll(properties2);
 				Set<PropertyTemplate> set = new HashSet<PropertyTemplate>();
 				for (PropertyTemplate propertyTemplate : properties3) {
 					set.add(propertyTemplate);
 				}
 				properties3.clear();
 				for (PropertyTemplate propertyTemplate : set) {
 					properties3.add(propertyTemplate);
 				}
 				settingsTpl.setProperties(properties3);
 				return settingsTpl;
 			}
 		}
 		return null;
 	}
 
 	
 	/**
 	 * @param customerId
 	 * @param projectId
 	 * @return AppDirName
 	 * @throws PhrescoException
 	 */
 	private String getAppDirName(String customerId, String projectId) throws PhrescoException {
 		String appDirName = "";
 		ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 		ProjectInfo project = projectManager.getProject(projectId, customerId);
 		List<ApplicationInfo> appInfos = project.getAppInfos();
 		if (CollectionUtils.isNotEmpty(appInfos)) {
 			appDirName = appInfos.get(0).getAppDirName();
 			return appDirName;
 		}
 		return appDirName;
 	}
 
 	/**
 	 * Connection alive check.
 	 * @param url the url
 	 * @return the response
 	 */
 	@GET
 	@Path("/connectionAliveCheck")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response connectionAliveCheck(@QueryParam(REST_QUERY_URL) String url) {
 		ResponseInfo<Boolean> responseData = new ResponseInfo<Boolean>();
 		if (url == null || ("".equals(url)) == true) {
 			ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, null,
 					null, RESPONSE_STATUS_FAILURE, PHR610008);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		}
 
 		boolean connection_status = false;
 		try {
 			String[] results = url.split(",");
 			String lprotocol = results[0];
 			String lhost = results[1];
 			int lport = Integer.parseInt(results[2]);
 			boolean tempConnectionAlive = isConnectionAlive(lprotocol, lhost, lport);
 			connection_status = tempConnectionAlive == true ? true : false;
 		} catch (Exception e) {
 			ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610009);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		}
 		ResponseInfo<Boolean> finalOutput = responseDataEvaluation(responseData, null,
 				connection_status, RESPONSE_STATUS_SUCCESS, PHR600005);
 		return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	/**
 	 * Update configuration.
 	 *
 	 * @param userId the user id
 	 * @param customerId the customer id
 	 * @param appDirName the app dir name
 	 * @param envName the env name
 	 * @param configurationlist the configurationlist
 	 * @return the response
 	 * @throws ConfigurationException 
 	 */
 	@POST
 	@Path("/updateConfig")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response updateConfiguration(@QueryParam(REST_QUERY_USERID) String userId,
 			@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_ENV_NAME) String envName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName,
 			@QueryParam("oldEnvName") String oldEnvName, @QueryParam("defaultEnv") String defaultEnv,
 			List<Configuration> configurationlist, @QueryParam("isEnvSpecific") String isEnvSpecific,
 			@QueryParam("configName") String configName, @QueryParam("desc") String desc , @QueryParam("isfavoric") String isfavoric,@QueryParam("favtype") String favtype,
 			@QueryParam(REST_QUERY_PROJECT_CODE) String projectCode, @QueryParam(REST_QUERY_PROJECTID) String projectId) throws ConfigurationException {
 		Environment env = new Environment();
 		String configFile = "";
		if (StringUtils.isNotEmpty(moduleName)) {
			appDirName = appDirName + File.separator + moduleName;
		}
 		if (StringUtils.isNotEmpty(projectCode)) {
 			configFile = FrameworkServiceUtil.getGlobalConfigFileDir(projectCode);
 		} else if (StringUtils.isNotEmpty(appDirName)) {
 			configFile = FrameworkServiceUtil.getConfigFileDir(appDirName);
 		} else {
 			throw new ConfigurationException("Project Code Or AppDirName Should Not be Empty");
 		}
 		ResponseInfo<Configuration> responseData = new ResponseInfo<Configuration>();
 		try {
 			if (StringUtils.isEmpty(isEnvSpecific)) {
 				ConfigManager configManager = new ConfigManagerImpl(new File(configFile));
 				String validateConfiguration = validateConfiguration(userId, customerId, appDirName, configurationlist, projectId);
 				if (StringUtils.isNotEmpty(validateConfiguration)) {
 					ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null,
 							validateConfiguration, RESPONSE_STATUS_FAILURE, PHR610024);
 					return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 				}
 				boolean defaultBoolValue = Boolean.parseBoolean(defaultEnv);
 				env.setName(envName);
 				env.setDesc(desc);
 				env.setDefaultEnv(defaultBoolValue);
 				if ("false".equalsIgnoreCase(isfavoric)) {
 					configManager.deleteEnvironment(oldEnvName);
 					env.setConfigurations(configurationlist);
 					List<Environment> environments = configManager.getEnvironments();
 					if (CollectionUtils.isNotEmpty(environments)) {
 						environments.add(env);
 						configManager.addEnvironments(environments);
 					} else {
 						configManager.addEnvironments(Arrays.asList(env));
 					}
 				} else {
 					configManager.deleteConfigurationsByType(oldEnvName, favtype);
 					List<Configuration> configurations = configManager.getConfigurations(oldEnvName);
 					configurations.addAll(configurationlist);
 					env.setConfigurations(configurations);
 					List<Environment> environments = configManager.getEnvironments();
 					if (CollectionUtils.isNotEmpty(environments)) {
 						environments.add(env);
 						configManager.addEnvironments(environments);
 					} else {
 						configManager.addEnvironments(Arrays.asList(env));
 					}
 				}
 			} else {
 				if (StringUtils.isNotEmpty(appDirName)) {
 					String nonEnvConfigFie = FrameworkServiceUtil.getnonEnvConfigFileDir(appDirName);
 					NonEnvConfigManager nonConfigManager = new NonEnvConfigManagerImpl(new File(nonEnvConfigFie));
 					if (StringUtils.isEmpty(configName)) {
 						Configuration config = configurationlist.get(0);
 						Configuration addFilesToConfigFile = addFilesToConfigFile(appDirName, config);
 						nonConfigManager.createConfiguration(addFilesToConfigFile);
 
 						ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null, "Success",
 								RESPONSE_STATUS_SUCCESS, PHR600015);
 						return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 					} else {
 						Configuration config = configurationlist.get(0);
 						Configuration addFilesToConfigFile = addFilesToConfigFile(appDirName, config);
 						nonConfigManager.updateConfiguration(configName, addFilesToConfigFile);
 					}
 				}
 			}
 			if (StringUtils.isNotEmpty(appDirName)) {
 				String pluginInfoFile = FrameworkServiceUtil.getPluginInfoPath(appDirName);
 				MojoProcessor mojoProcessor = new MojoProcessor(new File(pluginInfoFile));
 				String className = mojoProcessor.getApplicationHandler().getClazz();
 				ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 				Customer customer = serviceManager.getCustomer(customerId);
 				RepoInfo repoInfo = customer.getRepoInfo();
 				List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
 				ArtifactGroup artifactGroup = new ArtifactGroup();
 				artifactGroup.setGroupId(mojoProcessor.getApplicationHandler().getGroupId());
 				artifactGroup.setArtifactId(mojoProcessor.getApplicationHandler().getArtifactId());
 				// To set the versions of Artifact Group.
 				List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 				
 				ArtifactInfo artifactInfo = new ArtifactInfo();
 				artifactInfo.setVersion(mojoProcessor.getApplicationHandler().getVersion());
 				artifactInfos.add(artifactInfo);
 				artifactGroup.setVersions(artifactInfos);
 
 				artifactGroups.add(artifactGroup);
 				PhrescoDynamicLoader dynamicLoader = new PhrescoDynamicLoader(repoInfo, artifactGroups);
 				ApplicationProcessor applicationProcessor = dynamicLoader.getApplicationProcessor(className);
 				applicationProcessor.postConfiguration(FrameworkServiceUtil.getApplicationInfo(appDirName),	configurationlist);
 			}
 			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null, "Success",
 					RESPONSE_STATUS_SUCCESS, PHR600006);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, e, "Failure",
 					RESPONSE_STATUS_ERROR, PHR610010);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, e, "Failure",
 					RESPONSE_STATUS_ERROR, PHR610010);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		}
 	}
 
 
 	/**
 	 * Clone environment.
 	 *
 	 * @param appDirName the app dir name
 	 * @param envName the env name
 	 * @param cloneEnvironment the clone environment
 	 * @return the response
 	 * @throws ConfigurationException 
 	 */
 	@POST
 	@Path("/cloneEnvironment")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response cloneEnvironment(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam(REST_QUERY_ENV_NAME) String envName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName, Environment cloneEnvironment,
 			@QueryParam(REST_QUERY_PROJECT_CODE) String projectCode) throws ConfigurationException {
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		String configFile = "";
 		if (StringUtils.isNotEmpty(projectCode)) {
 			configFile = FrameworkServiceUtil.getGlobalConfigFileDir(projectCode);
 		} else if (StringUtils.isNotEmpty(appDirName)) {
 			configFile = FrameworkServiceUtil.getConfigFileDir(appDirName);
 		} else {
 			throw new ConfigurationException("Project Code Or AppDirName Should Not be Empty");
 		}
 		Environment clonedEnvironment = null;
 		ResponseInfo<Configuration> responseData = new ResponseInfo<Configuration>();
 		try {
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFile));
 			clonedEnvironment = configManager.cloneEnvironment(envName, cloneEnvironment);
 			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null,
 					clonedEnvironment, RESPONSE_STATUS_SUCCESS, PHR600007);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 
 		} catch (ConfigurationException e) {
 			ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, e,
 					clonedEnvironment, RESPONSE_STATUS_ERROR, PHR610029);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 
 		} catch (PhrescoException e) {
 			ResponseInfo<Configuration> finalOuptut = responseDataEvaluation(responseData, e,
 					clonedEnvironment, RESPONSE_STATUS_ERROR, PHR610011);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin",
 					"*").build();
 
 		}
 	}
 
 	/**
 	 * Cron expression for scheduler.
 	 *
 	 * @param cronExpInfo the cron exp info
 	 * @return the response
 	 */
 	@POST
 	@Path("/cronExpression")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response cronValidation(CronExpressionInfo cronExpInfo) {
 		ResponseInfo<CronExpressionInfo> responseData = new ResponseInfo<CronExpressionInfo>();
 		CronExpressionInfo cronResult = new CronExpressionInfo();
 		try {
 			String cronBy = cronExpInfo.getCronBy();
 			String cronExpression = "";
 			Date[] dates = null;
 			List<String> datesList = new ArrayList<String>();
 
 			if (REQ_CRON_BY_DAILY.equals(cronBy)) {
 				String hours = cronExpInfo.getHours();
 				String minutes = cronExpInfo.getMinutes();
 				String every = cronExpInfo.getEvery();
 
 				if ("false".equals(every)) {
 					if ("*".equals(hours) && "*".equals(minutes)) {
 						cronExpression = "0 * * * * ?";
 					} else if ("*".equals(hours) && !"*".equals(minutes)) {
 						cronExpression = "0 " + minutes + " 0 * * ?";
 					} else if (!"*".equals(hours) && "*".equals(minutes)) {
 						cronExpression = "0 0 " + hours + " * * ?";
 					} else if (!"*".equals(hours) && !"*".equals(minutes)) {
 						cronExpression = "0 " + minutes + " " + hours + " * * ?";
 					}
 				} else {
 					if ("*".equals(hours) && "*".equals(minutes)) {
 						cronExpression = "0 * * * * ?";
 					} else if ("*".equals(hours) && !"*".equals(minutes)) {
 						cronExpression = "0 " + "*/" + minutes + " * * * ?"; // 0 replace with *
 					} else if (!"*".equals(hours) && "*".equals(minutes)) {
 						cronExpression = "0 0 " + "*/" + hours + " * * ?"; // 0 replace with *
 					} else if (!"*".equals(hours) && !"*".equals(minutes)) {
 						cronExpression = "0 " + minutes + " */" + hours + " * * ?"; // 0 replace with *
 					}
 				}
 				dates = testCronExpression(cronExpression);
 
 			} else if (REQ_CRON_BY_WEEKLY.equals(cronBy)) {
 				String hours = cronExpInfo.getHours();
 				String minutes = cronExpInfo.getMinutes();
 				List<String> week = cronExpInfo.getWeek();
 				String csv = week.toString().replace("[", "").replace("]", "")
 	            .replace(", ", ",");
 				hours = ("*".equals(hours)) ? "0" : hours;
 				minutes = ("*".equals(minutes)) ? "0" : minutes;
 				cronExpression = "0 " + minutes + " " + hours + " ? * " + csv;
 				dates = testCronExpression(cronExpression);
 
 			} else if (REQ_CRON_BY_MONTHLY.equals(cronBy)) {
 				String hours = cronExpInfo.getHours();
 				String minutes = cronExpInfo.getMinutes();
 				List<String> month = cronExpInfo.getMonth();
 				String csv = month.toString().replace("[", "").replace("]", "")
 	            .replace(", ", ",");
 				String day = cronExpInfo.getDay();
 				hours = ("*".equals(hours)) ? "0" : hours;
 				minutes = ("*".equals(minutes)) ? "0" : minutes;
 				cronExpression = "0 " + minutes + " " + hours + " " + day + " " + csv + " ?";
 				dates = testCronExpression(cronExpression);
 			}
 
 			if (dates != null) {
 				cronExpression = cronExpression.replace('?', '*');
 				cronExpression = cronExpression.substring(2);
 				for (int i = 0; i < dates.length; i++) {
 					datesList.add(dates[i].toString());
 				}
 				cronResult.setDates(datesList);
 				
 			}
 			cronResult.setCronExpression(cronExpression);
 			ResponseInfo<CronExpressionInfo> finalOutput = responseDataEvaluation(responseData, null,
 					cronResult, RESPONSE_STATUS_SUCCESS, PHR600008);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 
 		} catch (PhrescoException e) {
 			ResponseInfo<CronExpressionInfo> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610012);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 	
 	/**
 	 * List environments by project id.
 	 *
 	 * @param customerId the customer id
 	 * @param projectId the project id
 	 * @return the response
 	 */
 	@GET
 	@Path("/listEnvironmentsByProjectId")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response listEnvironmentsByProjectId(@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_PROJECTID) String projectId) {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		try {
 			List<ApplicationInfo> appInfos = FrameworkServiceUtil.getAppInfos(customerId, projectId);
 			Set<String> environmentSet = new HashSet<String>();
 			for (ApplicationInfo appInfo : appInfos) {
 				List<Environment> environments = getEnvironments(appInfo);
 				for (Environment environment : environments) {
 					environmentSet.add(environment.getName());
 				}
 			}
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null,
 					environmentSet, RESPONSE_STATUS_SUCCESS, PHR600002);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR610013);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR610028);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 	
 	
 	@GET
 	@Path("/environmentList")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getEnvironmentList(@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName,
 			@QueryParam(REST_QUERY_PROJECT_CODE) String projectCode) {
 		if (StringUtils.isNotEmpty(moduleName) && StringUtils.isNotEmpty(appDirName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		try {
 			String configFileDir = "";
 			if (StringUtils.isNotEmpty(projectCode)) {
 				configFileDir = FrameworkServiceUtil.getGlobalConfigFileDir(projectCode);
 			} else if (StringUtils.isNotEmpty(appDirName)) {
 				configFileDir = FrameworkServiceUtil.getConfigFileDir(appDirName);
 			} else {
 				throw new ConfigurationException("Project Code Or AppDirName Should Not be Empty");
 			}
 			ConfigManager configManager = new ConfigManagerImpl(new File(configFileDir));
 			List<Environment> environments = configManager.getEnvironments();
 			Set<String> environmentSet = new HashSet<String>();
 			for (Environment environment : environments) {
 				environmentSet.add(environment.getName());
 			}
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null,
 					environmentSet, RESPONSE_STATUS_SUCCESS, PHR600002);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR610028);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 	
 	/**
 	 * Authenticate server.
 	 *
 	 * @param host the host
 	 * @param port the port
 	 * @param appDirName the app dir name
 	 * @return the response
 	 * @throws PhrescoException the phresco exception
 	 */
 	@GET
 	@Path("/returnCertificate")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response authenticateServer(@QueryParam("host") String host, @QueryParam("port") String port, 
 			@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName) throws PhrescoException {
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		ResponseInfo<RemoteCertificateInfo> responseData = new ResponseInfo<RemoteCertificateInfo>();
 		RemoteCertificateInfo remoteCertificateInfo = new RemoteCertificateInfo();
 		int portValue = Integer.parseInt(port);
 		boolean connctionAlive = Utility.isConnectionAlive("https", host, portValue);
 		boolean isCertificateAvailable = false;
 		String projectLocation = "";
 		projectLocation = Utility.getProjectHome() + File.separator;
 		if (StringUtils.isNotEmpty(appDirName)) {
 			projectLocation = projectLocation + appDirName;
 		}
 		if (connctionAlive) {
 			List<CertificateInfo> certificates = FrameworkServiceUtil.getCertificate(host, portValue);
 			if (CollectionUtils.isNotEmpty(certificates)) {
 				isCertificateAvailable = true;
 				remoteCertificateInfo.setCertificates(certificates);
 				remoteCertificateInfo.setProjectLocation(projectLocation);
 				remoteCertificateInfo.setCertificateAvailable(isCertificateAvailable);
 				ResponseInfo<RemoteCertificateInfo> finalOutput = responseDataEvaluation(responseData, null,
 						remoteCertificateInfo, RESPONSE_STATUS_SUCCESS, PHR600009);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 			}
 		}
 		ResponseInfo<RemoteCertificateInfo> finalOutput = responseDataEvaluation(responseData, null,
 				null, RESPONSE_STATUS_SUCCESS, PHR600016);
 		return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 	}
 
 	@POST
 	@Path("/addCertificate")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response addCertificate(AddCertificateInfo addCertificateInfo) {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		String certificatePath = "";
 		try {
 			String propValue = addCertificateInfo.getPropValue();
 			String fromPage = addCertificateInfo.getFromPage();
 			String appDirName = addCertificateInfo.getAppDirName();
 			if (StringUtils.isNotEmpty(propValue)) {
 				File file = new File(propValue);
 				if (fromPage.equals(CONFIGURATION)) {
 					certificatePath = configCertificateSave(propValue, file, appDirName, addCertificateInfo);
 				} else if (fromPage.equals(SETTINGS)) {
 					certificatePath = settingsCertificateSave(file, appDirName, addCertificateInfo);
 				}
 				ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null,
 						certificatePath, RESPONSE_STATUS_SUCCESS, PHR600010);
 				return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 						.build();
 			}
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_SUCCESS, PHR600017);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<String> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610016);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 
 	@GET
 	@Path("/fileBrowseFolder")
 	@Produces(MediaType.APPLICATION_XML)
 	public Response returnFileBorwseFolderStructure(@QueryParam("browsePath") String browsePath) {
 		ResponseInfo<DOMSource> responseData = new ResponseInfo<DOMSource>();
 		try {
 			List<FileBrowseInfo> browseList = new ArrayList<FileBrowseInfo>();
 			File browseFile = new File(browsePath);
 			File[] files = browseFile.listFiles();
 			if (files == null) {
 				ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_SUCCESS, PHR600019);
 				return Response.status(Status.OK).entity(finalOuptut).header(
 						"Access-Control-Allow-Origin", "*").build();
 			}
 			for (int i = 0; i < files.length; i++) {
 				FileBrowseInfo fileBrowse = new FileBrowseInfo();
 				if (files[i].isDirectory()) {
 					fileBrowse.setName(files[i].getName());
 					fileBrowse.setPath(files[i].getPath());
 					fileBrowse.setType("Folder");
 					browseList.add(fileBrowse);
 				} else {
 					fileBrowse.setName(files[i].getName());
 					fileBrowse.setPath(files[i].getPath());
 					fileBrowse.setType("File");
 					browseList.add(fileBrowse);
 				}
 			}
 			DOMSource outputContent = constructXml(browseFile.getName(), browsePath.toString(), browseList);
 			ResponseInfo<DOMSource> finalOuptut = responseDataEvaluation(responseData, null, outputContent, RESPONSE_STATUS_SUCCESS, PHR600012);
 			return Response.status(Status.OK).entity(outputContent).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR610020);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		}
 
 	}
 	
 	@GET
 	@Path("/fileBrowse")
 	@Produces(MediaType.APPLICATION_XML)
 	public Response returnFileBorwseEntireStructure(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName, @QueryParam("fileType") String fileType) {
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		ResponseInfo<DOMSource> responseData = new ResponseInfo<DOMSource>();
 		try {
 			String browsePath = Utility.getProjectHome() + File.separator;
 			if (StringUtils.isNotEmpty(appDirName)) {
 				browsePath = browsePath + appDirName;
 			}
 			DOMSource outputContent = createXML(browsePath, fileType);
 			if(outputContent == null) {
 				ResponseInfo<DOMSource> finalOuptut = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_FAILURE, PHR610021);
 				return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 			ResponseInfo<DOMSource> finalOuptut = responseDataEvaluation(responseData, null, outputContent, RESPONSE_STATUS_SUCCESS, PHR600013);
 			return Response.status(Status.OK).entity(outputContent).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<DOMSource> finalOuptut = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_ERROR, PHR610022);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();	
 		}
 	}
 	
 	@POST
 	@Path("/uploadFile")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
 	public Response uploadFile(@Context HttpServletRequest request) {
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		InputStream inputStream = null;
 		try {
 			String appDirName = request.getHeader("appDirName");
 			String actionType = request.getHeader("actionType");
 			String envName = request.getHeader("envName");
 			String configType = request.getHeader("configType");
 			String configName = request.getHeader("configName");
 			String oldName = request.getHeader("oldName");
 			String propName = request.getHeader("propName");
 			inputStream = request.getInputStream();
 			if (getTargetDir(configType, appDirName) == null) {
 				ConfigManager configManager = new ConfigManagerImpl(new File(FrameworkServiceUtil
 						.getConfigFileDir(appDirName)));
 				List<Configuration> configurations = configManager.getConfigurations(envName, configType);
 				boolean isNameExists = false;
 				boolean needNameValidation = true;
 				if (StringUtils.isEmpty(oldName) && FrameworkConstants.EDIT_CONFIG.equals(actionType)) {
 					oldName = configName;
 				}
 				if (configName.equalsIgnoreCase(oldName)) {
 					needNameValidation = false;
 				}
 				if (CollectionUtils.isNotEmpty(configurations) && needNameValidation) {
 					for (Configuration configuration : configurations) {
 						if (configName.trim().equalsIgnoreCase(configuration.getName())) {
 							isNameExists = true;
 							break;
 						}
 					}
 				}
 				if (!isNameExists) {
 					StringBuilder sb = new StringBuilder(Utility.getPhrescoTemp()).append(File.separator).append(
 							DO_NOT_CHECKIN_DIR).append(File.separator).append(envName).append(File.separator).append(
 							configName).append(File.separator).append(propName).append(File.separator).append(
 							request.getHeader("X-File-Name"));
 					File file = new File(sb.toString());
 					if (!file.exists()) {
 						file.mkdirs();
 					}
 					uploadZip(inputStream, file);
 					ResponseInfo finalOuptut = responseDataEvaluation(responseData, null, false,
 							RESPONSE_STATUS_SUCCESS, PHR600025);
 					return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 				} else {
 					ResponseInfo finalOuptut = responseDataEvaluation(responseData, null, false,
 							RESPONSE_STATUS_FAILURE, PHR610035);
 					return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 				}
 			} else {
 				StringBuilder sb = getTargetDir(configType, appDirName);
 				File file = new File(sb.toString() + File.separator + request.getHeader("X-File-Name"));
 				if (!file.getParentFile().exists()) {
 					file.getParentFile().mkdirs();
 				}
 				if (!file.exists()) {
 					file.createNewFile();
 				}
 				uploadZip(inputStream, file);
 				ResponseInfo finalOuptut = responseDataEvaluation(responseData, null, false, RESPONSE_STATUS_SUCCESS,
 						PHR600025);
 				return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 			}
 		} catch (PhrescoException e) {
 			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_ERROR,
 					PHR610036);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_ERROR,
 					PHR610037);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (IOException e) {
 			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_ERROR,
 					PHR610038);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		}
 	}
 
 	@GET
 	@Path("/listUploadedFiles")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response listUploadedFiles(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName,
 			@QueryParam("envName") String envName, @QueryParam("configType") String configType,
 			@QueryParam("configName") String configName, @QueryParam("isEnvSpecific") String isEnvSpecific) {
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		List<String> uploadedFiles = new ArrayList<String>();
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		try {
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			builder.append(appDirName);
 			builder.append(File.separator);
 			builder.append(FOLDER_DOT_PHRESCO);
 			builder.append(File.separator);
 			Configuration configuration = null;
 			if (StringUtils.isNotEmpty(isEnvSpecific) && "true".equals(isEnvSpecific)) {
 				builder.append(PHRESCO_ENV_CONFIG_FILE_NAME);
 				ConfigManager configManager = new ConfigManagerImpl(new File(builder.toString()));
 				configuration = configManager.getConfiguration(envName, configType, configName);
 			} else {
 				builder.append(PHRESCO_CONFIG_FILE_NAME);
 				NonEnvConfigManager configManager = new NonEnvConfigManagerImpl(new File(builder.toString()));
 				configuration = configManager.getConfiguration(configName);
 			}
 			if (configuration != null) {
 				Properties properties = configuration.getProperties();
 				String property = properties.getProperty(FILES);
 				if (StringUtils.isNotEmpty(property)) {
 					String[] splits = property.split(Constants.STR_COMMA);
 					for (String split : splits) {
 						split = split.replace("\\", "/");
 						StringBuilder sb = new StringBuilder(FrameworkServiceUtil.getApplicationHome(appDirName));
 						StringBuilder targetDir = getTargetDir(configType, appDirName);
 						if (targetDir == null) {
 							sb.append(File.separator);
 							sb.append(DO_NOT_CHECKIN_DIR);
 							sb.append(File.separator);
 							sb.append(envName);
 							sb.append(File.separator);
 							sb.append(configName);
 							sb.append(File.separator);
 						}
 						sb.append(split);
 						File file = new File(sb.toString());
 						if (file.exists() && !file.isDirectory()) {
 							uploadedFiles.add("" + split);
 						}
 					}
 				}
 			}
 			ResponseInfo finalOuptut = responseDataEvaluation(responseData, null, uploadedFiles,
 					RESPONSE_STATUS_SUCCESS, PHR600026);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_ERROR,
 					PHR610039);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (ConfigurationException e) {
 			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_ERROR,
 					PHR610040);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		}
 	}
 
 	@GET
 	@Path("/removeFile")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response removeConfigFile(@QueryParam(REST_QUERY_APPDIR_NAME) String appDirName,
 			@QueryParam("configType") String configType, @QueryParam("propName") String propName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName,
 			@QueryParam("fileName") String fileName, @QueryParam("envName") String envName,
 			@QueryParam("configName") String configName) {
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		ResponseInfo<String> responseData = new ResponseInfo<String>();
 		try {
 			if (StringUtils.isNotEmpty(appDirName) && getTargetDir(configType, appDirName) != null) {
 				StringBuilder sb = getTargetDir(configType, appDirName).append(File.separator).append(fileName);
 				FileUtil.delete(new File(sb.toString()));
 			} else {
 				if (StringUtils.isNotEmpty(appDirName)) {
 					StringBuilder sb = new StringBuilder(FrameworkServiceUtil.getApplicationHome(appDirName)).append(
 							File.separator).append(DO_NOT_CHECKIN_DIR).append(File.separator).append(envName);
 					File envNameDir = new File(sb.toString());
 					sb.append(File.separator).append(configName);
 					File configNameDir = new File(sb.toString());
 					sb.append(File.separator).append(propName);
 					File propNameDir = new File(sb.toString());
 					sb.append(File.separator).append(fileName);
 					File file = new File(sb.toString());
 					FileUtil.delete(file);
 					if (ArrayUtils.isEmpty(propNameDir.listFiles())) {
 						FileUtil.delete(propNameDir);
 					}
 					if (ArrayUtils.isEmpty(configNameDir.listFiles())) {
 						FileUtil.delete(configNameDir);
 					}
 					if (ArrayUtils.isEmpty(envNameDir.listFiles())) {
 						FileUtil.delete(envNameDir);
 					}
 				}
 			}
 			ResponseInfo finalOuptut = responseDataEvaluation(responseData, null, false, RESPONSE_STATUS_SUCCESS, PHR600027);
 			return Response.ok(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<String> finalOuptut = responseDataEvaluation(responseData, null, null, RESPONSE_STATUS_ERROR,
 					PHR610041);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		}
 	}
 
 	private void uploadZip(InputStream inputStream, File tempZipFile) throws PhrescoException {
 		OutputStream out = null;
 		try {
 			out = new FileOutputStream(tempZipFile);
 			int read = 0;
 			byte[] bytes = new byte[1024];
 			if (inputStream != null) {
 				while ((read = inputStream.read(bytes)) != -1) {
 					out.write(bytes, 0, read);
 				}
 			}
 			out.flush();
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeStream(inputStream);
 			Utility.closeStream(out);
 		}
 	}
 
 	private StringBuilder getTargetDir(String configType, String appDirName) throws PhrescoException {
 		StringBuilder sb = null;
 		try {
 			String targetDir = getTargetDirFromPom(configType, appDirName);
 			if (StringUtils.isEmpty(targetDir)) {
 				return null;
 			}
 			sb = new StringBuilder(Utility.getProjectHome()).append(appDirName).append(File.separator)
 					.append(targetDir);
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
 
 		return sb;
 	}
 
 	private String getTargetDirFromPom(String configTempType, String appDirName) throws PhrescoException {
 		String targetDir = "";
 		try {
 			String dynamicType = configTempType.toLowerCase().replaceAll("\\s", "");
 			targetDir = FrameworkServiceUtil.getPomProcessor(appDirName).getProperty(
 					PHRESCO_DOT + dynamicType + DOT_TARGET_DIR);
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 
 		return targetDir;
 	}
 
 	/**
 	 * Fetches the list of properties (features or configurations)
 	 *
 	 * @param userId the user Id
 	 * @param type the type of property (component or feature)
 	 * @param appDirName the app dir name
 	 * @param fromPage the source page (add or edit)
 	 * @param customerId the customerId
 	 * @return the response
 	 */
 	@POST
 	@Path("/configType")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response showProperties(@QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_TYPE) String type, @QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_MODULE_NAME) String moduleName,
 			@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		ResponseInfo<List<String>> responseData = new ResponseInfo<List<String>>();
 		try {
 			ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 			SettingsTemplate settingsTemplate = serviceManager.getConfigTemplateByType(customerId, type);
 			ApplicationInfo appInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 			List<String> names = new ArrayList<String>();
 			if (CONFIG_FEATURES.equals(settingsTemplate.getId())) {
 				names = setCustomModNamesInReq(appInfo, serviceManager);
 			} else if (CONFIG_COMPONENTS.equals(settingsTemplate.getId())) {
 				names = setComponentNamesInReq(appInfo, serviceManager);
 			}
 			ResponseInfo<List<String>> finalOuptut = responseDataEvaluation(responseData, null, names, RESPONSE_STATUS_SUCCESS, PHR600023);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		} catch (PhrescoException e) {
 			ResponseInfo<List<String>> finalOuptut = responseDataEvaluation(responseData, e, null, RESPONSE_STATUS_ERROR, PHR610033);
 			return Response.status(Status.OK).entity(finalOuptut).header("Access-Control-Allow-Origin", "*").build();
 		}		
 	}
 	
 	private List<String> setCustomModNamesInReq(ApplicationInfo appInfo, ServiceManager serviceManager) throws PhrescoException {
         try {
             List<String> selectedModules = appInfo.getSelectedModules();
             List<String> custFeatureNames = new ArrayList<String>();
             if (CollectionUtils.isNotEmpty(selectedModules)) {
                 for (String selectedModule : selectedModules) {
                     ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(selectedModule);
                     ArtifactGroup artifactGroup = serviceManager.getArtifactGroupInfo(artifactInfo.getArtifactGroupId());
                     List<CoreOption> appliesTo = artifactGroup.getAppliesTo();
                     for (CoreOption coreOption : appliesTo) {
                         if (coreOption.getTechId().equals(appInfo.getTechInfo().getId()) && !coreOption.isCore()) {
                             custFeatureNames.add(artifactGroup.getName());
                         }
                     }
                 }
             }
             return custFeatureNames;
         } catch (PhrescoException e) {
             throw new PhrescoException(e);
         }
     }
     
     private List<String> setComponentNamesInReq(ApplicationInfo appInfo, ServiceManager serviceManager) throws PhrescoException {
         try {
             List<String> selectedComponents = appInfo.getSelectedComponents();
             List<String> componentNames = new ArrayList<String>();
             if (CollectionUtils.isNotEmpty(selectedComponents)) {
                 for (String selectedComponent : selectedComponents) {
                     ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(selectedComponent);
                     ArtifactGroup artifactGroup = serviceManager.getArtifactGroupInfo(artifactInfo.getArtifactGroupId());
                     List<CoreOption> appliesTo = artifactGroup.getAppliesTo();
                     for (CoreOption coreOption : appliesTo) {
                         if (coreOption.getTechId().equals(appInfo.getTechInfo().getId())) {
                             componentNames.add(artifactGroup.getName());
                         }
                     }
                 }
             }
             return componentNames;
         } catch (PhrescoException e) {
             throw new PhrescoException(e);
         }
     }
 	
     /**
 	 * Fetches the options of a particular property
 	 *
 	 * @param userId the user Id
 	 * @param customerId the customerId
 	 * @param featureName the name of the feature
 	 * @param appDirName the app dir name
 	 * @param envName the environment name
 	 * 
 	 * @return the response
 	 */
 	@POST
 	@Path("/showFeatureConfigs")
 	@Produces(MediaType.APPLICATION_JSON)
 	@Consumes(MediaType.APPLICATION_JSON)
 	public Response showFeatureConfigs(@QueryParam(REST_QUERY_USERID) String userId, @QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@QueryParam(REST_QUERY_FEATURENAME) String featureName, @QueryParam(REST_QUERY_APPDIR_NAME) String appDirName, @QueryParam(REST_QUERY_EVN_NAME) String envName,@QueryParam(REST_QUERY_MODULE_NAME) String moduleName) {
 		if (StringUtils.isNotEmpty(moduleName)) {
 			appDirName = appDirName + File.separator + moduleName;
 		}
 		ResponseInfo<FeatureConfigure> responseData = new ResponseInfo<FeatureConfigure>();
         try {
         	ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
         	FeatureConfigure featureConfigure = new FeatureConfigure();
 			featureConfigure = getPropTemplateFromConfigFile(appDirName, customerId, serviceManager, featureName, envName);
 			ResponseInfo<FeatureConfigure> finalOutput = responseDataEvaluation(responseData, null,
 					featureConfigure, RESPONSE_STATUS_SUCCESS, PHR600024);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*").build();
         } catch (PhrescoException e) {
 			ResponseInfo<FeatureConfigure> finalOutput = responseDataEvaluation(responseData, e,
 					null, RESPONSE_STATUS_ERROR, PHR610034);
 			return Response.status(Status.OK).entity(finalOutput).header("Access-Control-Allow-Origin", "*")
 					.build();
 		}
 	}
 	
 	private FeatureConfigure getPropTemplateFromConfigFile(String appDirName, String customerId, ServiceManager serviceManager, String featureName, String envName) throws PhrescoException {
         List<PropertyTemplate> propertyTemplates = new ArrayList<PropertyTemplate>();
         try {
         	FeatureConfigure featureConfigure = new FeatureConfigure();
 	        FrameworkServiceUtil frameworkServiceUtil = new FrameworkServiceUtil();
             List<Configuration> featureConfigurations = frameworkServiceUtil.getApplicationProcessor(appDirName, customerId, serviceManager).preConfiguration(FrameworkServiceUtil.getApplicationInfo(appDirName), featureName, envName);
             Properties properties = null;
             if (CollectionUtils.isNotEmpty(featureConfigurations)) {
                 for (Configuration featureConfiguration : featureConfigurations) {
                     properties = featureConfiguration.getProperties();
                     Set<Object> keySet = properties.keySet();
                     for (Object objectKey : keySet) {
                         String keyStr = (String) objectKey;
                         String dispName = keyStr.replace(".", " ");
                         PropertyTemplate propertyTemplate = new PropertyTemplate();
                         propertyTemplate.setKey(keyStr);
                         propertyTemplate.setName(dispName);
                         propertyTemplates.add(propertyTemplate);
                     }
                 }
             }
             featureConfigure.setHasCustomProperty(true);
 	        featureConfigure.setProperties(properties);
 	        featureConfigure.setPropertyTemplates(propertyTemplates);
 	        return featureConfigure;
         } catch (PhrescoException e) {
             throw new PhrescoException(e);
         }
     }
 
 	// for the return of entire project structure as single xml
 	private static DOMSource createXML(String browsePath, String fileType) throws PhrescoException {
 		try {
 			File inputPath = new File(browsePath);
 			if (!inputPath.isDirectory() || inputPath.isFile()) {
 				return null;
 			}
 			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 			Document document = documentBuilder.newDocument();
 			Element rootElement = document.createElement("root");
 			document.appendChild(rootElement);
 
 			Element mainFolder = document.createElement("Item");
 			mainFolder.setAttribute("name", inputPath.getName());
 			mainFolder.setAttribute("path", inputPath.toString());
 			mainFolder.setAttribute("type", "Folder");
 			rootElement.appendChild(mainFolder);
 
 			listDirectories(mainFolder, document, inputPath, fileType);
 
 			DOMSource source = new DOMSource(document);
 			return source;
 		} catch (DOMException e) {
 			throw new PhrescoException(e);
 		} catch (ParserConfigurationException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private static void listDirectories(Element rootElement, Document document, File dir, String fileType) {
 		for (File childFile : dir.listFiles()) {
 			if (childFile.isDirectory()) {
 				Element childElement = document.createElement("Item");
 				childElement.setAttribute("name", childFile.getName());
 				childElement.setAttribute("path", childFile.getPath());
 				childElement.setAttribute("type", "Folder");
 				rootElement.appendChild(childElement);
 
 				listDirectories(childElement, document, childFile.getAbsoluteFile(), fileType);
 			} else { 
 				String fileName = childFile.getName();
 				 fileName = fileName.substring(fileName.lastIndexOf('.')+1,fileName.length());
 				if(StringUtils.isEmpty(fileType) || fileName.equals(fileType)) {
 				Element childElement = document.createElement("Item");
 				childElement.setAttribute("name", childFile.getName());
 				childElement.setAttribute("path", childFile.getPath());
 				childElement.setAttribute("type", "File");
 				rootElement.appendChild(childElement);
 				}
 			}
 		}
 	}
 	
 	// for the return of project structure on individual folders
 	private DOMSource constructXml(String name, String path, List<FileBrowseInfo> browseList) throws PhrescoException {
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
 			// root elements
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement("root");
 			doc.appendChild(rootElement);
 
 			// Main folder
 			Element item = doc.createElement("item");
 			rootElement.appendChild(item);
 
 			item.setAttribute("type", "folder");
 			item.setAttribute("name", name);
 			item.setAttribute("path", path);
 
 			// Sub foldersfor
 			for (FileBrowseInfo browseField : browseList) {
 				Element childitem = doc.createElement("item");
 				childitem.setAttribute("type", browseField.getType());
 				childitem.setAttribute("name", browseField.getName());
 				childitem.setAttribute("path", browseField.getPath());
 				item.appendChild(childitem);
 			}
 			DOMSource source = new DOMSource(doc);
 			return source;
 		} catch (ParserConfigurationException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private String settingsCertificateSave(File file, String appDirName,
 			AddCertificateInfo addCertificateInfo) throws PhrescoException {
 		String certifactPath = "";
 			StringBuilder sb = new StringBuilder(CERTIFICATES).append(File.separator).append(
 					addCertificateInfo.getEnvironmentName()).append(HYPHEN).append(addCertificateInfo.getConfigName())
 					.append(FrameworkConstants.DOT).append(FILE_TYPE_CRT);
 			certifactPath = sb.toString();
 			if (file.exists()) {
 				File dstFile = new File(Utility.getProjectHome() + certifactPath);
 				FrameworkUtil.copyFile(file, dstFile);
 			} else {
 				saveCertificateFile(certifactPath, addCertificateInfo.getHost(), Integer
 						.parseInt(addCertificateInfo.getPort()), addCertificateInfo.getCertificateName(), appDirName);
 			}
 		return certifactPath;
 	}
 
 	private String configCertificateSave(String value, File file, String appDirName,
 			AddCertificateInfo addCertificateInfo) throws PhrescoException {
 			if (file.exists()) {
 				String path = Utility.getProjectHome().replace("\\", "/");
 				value = value.replace(path + appDirName + "/", "");
 			} else {
 				StringBuilder sb = new StringBuilder(FOLDER_DOT_PHRESCO).append(File.separator).append(CERTIFICATES)
 						.append(File.separator).append(addCertificateInfo.getEnvironmentName()).append(HYPHEN).append(
 								addCertificateInfo.getConfigName()).append(FrameworkConstants.DOT)
 						.append(FILE_TYPE_CRT);
 				value = sb.toString();
 				saveCertificateFile(value, addCertificateInfo.getHost(), Integer
 						.parseInt(addCertificateInfo.getPort()), addCertificateInfo.getCertificateName(), appDirName);
 			}
 		return value;
 	}
 
 	private void saveCertificateFile(String certificatePath, String host, int port,
 			String certificateName, String appDirName) throws PhrescoException {
 		List<CertificateInfo> certificates = FrameworkServiceUtil.getCertificate(host, port);
 		if (CollectionUtils.isNotEmpty(certificates)) {
 			for (CertificateInfo certificate : certificates) {
 				if (certificate.getDisplayName().equals(certificateName)) {
 					File file = new File(Utility.getProjectHome() + appDirName + "/" + certificatePath);
 					FrameworkServiceUtil.addCertificate(certificate, file);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets the environments.
 	 *
 	 * @param appInfo the app info
 	 * @return the environments
 	 * @throws ConfigurationException the configuration exception
 	 */
 	private List<Environment> getEnvironments(ApplicationInfo appInfo) throws ConfigurationException {
 		String configFile = FrameworkServiceUtil.getConfigFileDir(appInfo.getAppDirName());
 		ConfigManager configManager = new ConfigManagerImpl(new File(configFile));
 		List<Environment> environments = configManager.getEnvironmentsAlone();
 		return environments;
 	}
 
 	/**
 	 * Validate configuration.
 	 *
 	 * @param userId the user id
 	 * @param customerId the customer id
 	 * @param appDirName the app dir name
 	 * @param configurationlist the configurationlist
 	 * @param projectId 
 	 * @return 
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String validateConfiguration(String userId, String customerId, String appDirName,
 			List<Configuration> configurationlist, String projectId) throws PhrescoException {
 		ServiceManager serviceManager = CONTEXT_MANAGER_MAP.get(userId);
 		int serverCount = 0;
 		int emailCount = 0;
 		boolean serverTypeValidation = false;
 		boolean isRequired = false;
 		String isRemote = "false";
 		String dynamicError = "";
 		
 		
 		
 //		String techId = FrameworkServiceUtil.getApplicationInfo(appDirName).getTechInfo().getId();
 		for (int i = 0; i < configurationlist.size(); i++) {
 			if (StringUtils.isEmpty(configurationlist.get(i).getName())) {
 				return "Name is Empty";
 			} else {
 				String name = configurationlist.get(i).getName();
 				for (int j = 0; j < configurationlist.size(); j++) {
 					if (i != j) {
 						if (name.equals(configurationlist.get(j).getName())) {
 							return "Name already Exists";
 						}
 					}
 				}
 			}
 		}
 
 		for (Configuration configuration : configurationlist) {
 			if (StringUtils.isEmpty(configuration.getType())) {
 				return "Configuration Type is Empty";
 			}
 
 //			if (FrameworkConstants.SERVER.equals(configuration.getType())
 //					|| FrameworkConstants.EMAIL.equals(configuration.getType())) {
 				
 				if (FrameworkConstants.SERVER.equals(configuration.getType())) {
 					serverCount++;
 				}
 				
 			if(FrameworkConstants.EMAIL.equals(configuration.getType())) {
 					String propertyEmail = configuration.getProperties().getProperty(FrameworkConstants.EMAIL_ID);
 					emailCount++;
 					if (propertyEmail.isEmpty()) {
 						return "Email ID is Empty";
 					} else {
 						String emailvalidation = emailvalidation(propertyEmail);
 						 if(StringUtils.isNotEmpty(emailvalidation)) {
 							 return emailvalidation;
 						 }
 					}
 				}
 
 			if (serverCount > 1) {
 				return "Server Configuration type Already Exists";
 			}
 
 			if (emailCount > 1) {
 				return "Email Configuration type Already Exists";
 			}
 			List<String> techIds = new ArrayList<String>();
 			if (StringUtils.isNotEmpty(appDirName)) {
 				String techId = FrameworkServiceUtil.getApplicationInfo(appDirName).getTechInfo().getId();
 				techIds.add(techId);
 			} else {
 				techIds = getTechId(customerId, projectId);
 			}
 			if (CollectionUtils.isNotEmpty(techIds)) {
 				for (String techId : techIds) {
 					if (!FrameworkConstants.OTHER.equals(configuration.getType())) {
 						SettingsTemplate configTemplateByType = serviceManager.getConfigTemplateByTechId(techId, configuration.getType());
 						if (configTemplateByType != null) {
 							List<PropertyTemplate> properties = configTemplateByType.getProperties();
 							for (PropertyTemplate propertyTemplate : properties) {
 								String propKey = propertyTemplate.getKey();
 								if(FrameworkConstants.SERVER.equals(configuration.getType())) {
 									if(propKey.equals(FrameworkConstants.REMOTE_DEPLOYMENT)) {
 										isRemote = configuration.getProperties().getProperty(propKey);
 									}
 								}
 								String propValue = configuration.getProperties().getProperty(propKey);
 								if (FrameworkConstants.REQ_TYPE.equals(propKey)
 										&& FrameworkConstants.NODEJS_SERVER.equals(propValue)
 										|| FrameworkConstants.NODEJS_MAC_SERVER.equals(propValue)
 										|| FrameworkConstants.SHAREPOINT_SERVER.equals(propValue)
 										|| FrameworkConstants.IIS_SERVER.equals(propValue)) {
 									// If nodeJs and sharepoint server selected , there should not be validation for deploy dir.
 									serverTypeValidation = true;
 								}
 
 								if (techId != null && techId.equals(FrameworkConstants.TECH_SITE_CORE)) {
 									if (FrameworkConstants.DEPLOY_DIR.equals(propKey)) {
 										isRequired = false;
 									}
 								}
 
 								if (serverTypeValidation && FrameworkConstants.DEPLOY_DIR.equals(propKey)) {
 									isRequired = false;
 								}
 
 								// validation for UserName & Password for RemoteDeployment
 
 								if (FrameworkConstants.TRUE.equals(isRemote)) {
 									if (FrameworkConstants.ADMIN_USERNAME.equals(propKey)
 											|| FrameworkConstants.ADMIN_PASSWORD.equals(propKey)) {
 										isRequired = true;
 									}
 									if (FrameworkConstants.DEPLOY_DIR.equals(propKey)) {
 										isRequired = false;
 									}
 								}
 
 								if (isRequired && StringUtils.isEmpty(propValue)) {
 									String field = propertyTemplate.getName();
 									dynamicError += propKey + Constants.STR_COLON + field + "is missing" + Constants.STR_COMMA;
 								}
 
 								if (StringUtils.isNotEmpty(dynamicError)) {
 									dynamicError = dynamicError.substring(0, dynamicError.length() - 1);
 									return dynamicError;
 								}
 
 //								Version Validation for Server and Database configuration types
 //								if (FrameworkConstants.SERVER.equals(configuration.getType())
 //										|| FrameworkConstants.DATABASE.equals(configuration.getType())) {
 //									if (StringUtils.isEmpty(configuration.getProperties().getProperty(FrameworkConstants.VERSION))) {
 //										return "Version is Empty";
 //									}
 //								}
 
 								// Site Core installation path check
 								if (techId.equals(FrameworkConstants.TECH_SITE_CORE)
 										&& FrameworkConstants.SERVER.equals(configuration.getType())
 										&& StringUtils.isEmpty(configuration.getProperties().getProperty(
 												FrameworkConstants.SETTINGS_TEMP_SITECORE_INST_PATH))) {
 									return "SiteCore Installation path Location is missing";
 								}
 							}
 						}
 					}
 				}
 			}
 			
 		}
 		return null;
 	}
 
 	
 	/**
      * To validate the Environment Name
 	 * @param projectCode2 
 	 * @param appDirName 
      * @return
      * @throws PhrescoException 
      * @throws ConfigurationException 
      */
 	
 	private boolean validateEnvironment(String projectId, String customerId, List<Environment> enviroments, String appDirName) throws PhrescoException, ConfigurationException {
 		boolean environmentExists = false;
 		try {
 			if (StringUtils.isEmpty(appDirName)) {
 				List<String> appDirNameList = getAppDirNameList(projectId, customerId);
 				if (CollectionUtils.isNotEmpty(appDirNameList)) {
 					for (String appDirectoryName: appDirNameList) {
 						File path = new File(Utility.getProjectHome() + File.separator + appDirectoryName + File.separator + FOLDER_DOT_PHRESCO + File.separator + CONFIGURATION_INFO_FILE_NAME);
 						ConfigManagerImpl configImpl = new ConfigManagerImpl(path);
 						List<Environment> configenvs = configImpl.getEnvironments();
 						for (Environment environment : enviroments) {
 							for (Environment env : configenvs) {
 								if(environment.getName().equalsIgnoreCase(env.getName())) {
 									environmentExists = true;
 									throw new PhrescoException(ERROR_DUPLICATE_NAME_IN_CONFIGURATIONS);
 								}
 							}
 						}
 					}
 				}
 			} else {
 				ProjectInfo projectInfo = FrameworkServiceUtil.getProjectInfo(appDirName);
 				String projectCode = projectInfo.getProjectCode();
 				String globalConfigFileDir = FrameworkServiceUtil.getGlobalConfigFileDir(projectCode);
 				ConfigManagerImpl configImpl = new ConfigManagerImpl(new File(globalConfigFileDir));
 				List<Environment> configenvs = configImpl.getEnvironments();
 				for (Environment environment : enviroments) {
 					for (Environment env : configenvs) {
 						if(environment.getName().equalsIgnoreCase(env.getName())) {
 							environmentExists = true;
 							throw new PhrescoException(ERROR_DUPLICATE_NAME_IN_CONFIGURATIONS);
 						}
 					}
 				}
 				
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return environmentExists;
 	}
 	
 	
 	
 	/**
 	 * Emailvalidation.
 	 *
 	 * @param propertyEmail the property email
 	 * @return 
 	 * @throws PhrescoException the phresco exception
 	 */
 	private String emailvalidation(String propertyEmail) throws PhrescoException {
 		Pattern p = Pattern
 				.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
 		Matcher m = p.matcher(propertyEmail);
 		boolean b = m.matches();
 		if (!b) {
 			return "Email Format mismatch";
 		}
 		return null;
 
 	}
 
 	/**
 	 * Test cron expression.
 	 *
 	 * @param expression the expression
 	 * @return the date[]
 	 * @throws PhrescoException the phresco exception
 	 */
 	private Date[] testCronExpression(String expression) throws PhrescoException {
 		Date[] dates = null;
 		try {
 			final CronExpression cronExpression = new CronExpression(expression);
 			final Date nextValidDate1 = cronExpression.getNextValidTimeAfter(new Date());
 			final Date nextValidDate2 = cronExpression.getNextValidTimeAfter(nextValidDate1);
 			final Date nextValidDate3 = cronExpression.getNextValidTimeAfter(nextValidDate2);
 			final Date nextValidDate4 = cronExpression.getNextValidTimeAfter(nextValidDate3);
 			dates = new Date[] { nextValidDate1, nextValidDate2, nextValidDate3, nextValidDate4 };
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return dates;
 	}
 
 	/**
 	 * Checks if is connection alive.
 	 *
 	 * @param protocol the protocol
 	 * @param host the host
 	 * @param port the port
 	 * @return true, if is connection alive
 	 */
 	public boolean isConnectionAlive(String protocol, String host, int port) {
 		boolean isAlive = true;
 		try {
 			URL url = new URL(protocol, host, port, "");
 			URLConnection connection = url.openConnection();
 			connection.connect();
 		} catch (Exception e) {
 			isAlive = false;
 		}
 
 		return isAlive;
 	}
 
 	/**
 	 * Gets the download info.
 	 *
 	 * @param serviceManager the service manager
 	 * @param appDirName the app dir name
 	 * @param userId the user id
 	 * @param type the type
 	 * @return the download info
 	 * @throws PhrescoException the phresco exception
 	 */
 	private void getDownloadInfo(ServiceManager serviceManager, String appDirName, String userId,
 			String type, Map<String, List<String>> nameMap) throws PhrescoException {
 		ApplicationInfo appInfo = FrameworkServiceUtil.getApplicationInfo(appDirName);
 		List<ArtifactGroupInfo> artifactGroupInfos = null;
 
 		if (Constants.SETTINGS_TEMPLATE_SERVER.equals(type)) {
 			artifactGroupInfos = appInfo.getSelectedServers();
 		} else if (Constants.SETTINGS_TEMPLATE_DB.equals(type)) {
 			artifactGroupInfos = appInfo.getSelectedDatabases();
 		}
 		if (CollectionUtils.isNotEmpty(artifactGroupInfos)) {
 			for (ArtifactGroupInfo artifactGroupInfo : artifactGroupInfos) {
 				ArtifactGroup artifactGroup = serviceManager.getArtifactGroupInfo(artifactGroupInfo	.getArtifactGroupId());
 				List<String> artifactInfoIds = artifactGroupInfo.getArtifactInfoIds();
 				List<String> verstions = new ArrayList<String>();
 				for (String artifactInfoId : artifactInfoIds) {
 					ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(artifactInfoId);
 					List<String> list = null;
 					if (nameMap.containsKey(artifactGroup.getName())) {
 						list = nameMap.get(artifactGroup.getName());
 						if (!list.contains(artifactInfo.getVersion())) {
 							list.add(artifactInfo.getVersion());
 							verstions.addAll(list);
 						}
 					} else {
 						verstions.add(artifactInfo.getVersion());
 					}
 				}
 				nameMap.put(artifactGroup.getName(), verstions);
 			}
 		}
 	}
 	
 	private Configuration addFilesToConfigFile(String appDirName, Configuration config) throws PhrescoException {
 		String files = config.getProperties().getProperty("files");
 		String targetDirFromPom = getTargetDirFromPom(config.getType(), appDirName);
 		files = targetDirFromPom + File.separator + files;
 		config.getProperties().setProperty("files", files);
 		return config;
 	}
 
 }
