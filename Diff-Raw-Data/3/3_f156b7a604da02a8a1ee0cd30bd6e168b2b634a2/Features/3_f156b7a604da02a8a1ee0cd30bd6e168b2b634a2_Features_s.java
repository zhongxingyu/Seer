 /*
  * ###
  * Framework Web Archive
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.framework.actions.applications;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.Element;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.PropertyTemplate;
 import com.photon.phresco.commons.model.RequiredOption;
 import com.photon.phresco.commons.model.SelectedFeature;
 import com.photon.phresco.commons.model.SettingsTemplate;
 import com.photon.phresco.commons.model.TechnologyInfo;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 
 public class Features extends FrameworkBaseAction {
     
     private static final long serialVersionUID = 6608382760989903186L;
 	
 	private static final Logger S_LOGGER = Logger.getLogger(Features.class);
 	private static Boolean s_debugEnabled = S_LOGGER.isDebugEnabled();
 	
 	private String projectCode = null;
 	private String externalCode = null;
 	private String fromPage = null;
 	private String selectedType = null;
 	private String application = null;
 	private List<String> techVersion = null;
 	private String moduleId = null;
 	private String version = null;
 	private String moduleType = null;
 	private String techId = null;
 	private String preVersion = null;
 	private Collection<String> dependentIds = null;
 	private Collection<String> dependentVersions = null;
 	private Collection<String> preDependentIds = null;
 	private Collection<String> preDependentVersions = null;
 	private List<String> pilotModules = null;
 	private List<String> pilotJSLibs = null;
 	private boolean isValidated = false;
 	private String configServerNames = null;
 	private String configDbNames = null;
 	private String fromTab = null;
 	private List<String> defaultModules =  null;
 	
 	private String name = "";
 	private String code = "";
 	private String description = "";
 	private String appDir = "";
 	private String oldAppDirName = "";
 	private String technology = "";
 	private String technologyVersion = "";
 	private String applicationVersion = "";
 	private String appId = "";
 	private List<String> server = null;
 	private List<String> database = null;
 	private List<String> serverVersion = null;
     private List<String> databaseVersion = null;
     private List<String> webservice = null;
 	private String technologyId = "";
 	private String type = "";
 	private String customerId = "";
 	private String pilotProject = "";
 	
 	private String nameError = "";
 	private String codeError = "";
 	private boolean errorFound = false;
 	
 	private String configTemplateType = "";
 	
 	private String featureName = "";
 	
 	private List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
 	List<String> depArtifactGroupNames = new ArrayList<String>();
 	List<String> depArtifactInfoIds = new ArrayList<String>();
 	List<String> dependencyIds = new ArrayList<String>();
 	boolean dependency = false;
 	
 	public String features() {
 		try {
 			ProjectInfo projectInfo = (ProjectInfo)getSessionAttribute(getAppId() + SESSION_APPINFO);
 			ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
 			if (appInfo == null) {
                 appInfo = getApplicationInfo();
             } else if (appInfo == null) {
 				appInfo = new ApplicationInfo();
 			}
 			List<String> selectedModules = appInfo.getSelectedModules();
 			List<SelectedFeature> listFeatures = new ArrayList<SelectedFeature>();
 			if (CollectionUtils.isNotEmpty(selectedModules)) {
 				for (String selectedModule : selectedModules) {
 					SelectedFeature selectFeature = createArtifactInformation(selectedModule);
 					listFeatures.add(selectFeature);
 				}
 			}
 			
 			List<String> selectedJSLibs = appInfo.getSelectedJSLibs();
 			if (CollectionUtils.isNotEmpty(selectedJSLibs)) {
 				for (String selectedJSLib : selectedJSLibs) {
 					SelectedFeature selectFeature = createArtifactInformation(selectedJSLib);
 					listFeatures.add(selectFeature);
 				}
 			}
 			
 			List<String> selectedComponents = appInfo.getSelectedComponents();
 			if (CollectionUtils.isNotEmpty(selectedComponents))	{
 				for (String selectedComponent : selectedComponents) {
 					SelectedFeature selectFeature = createArtifactInformation(selectedComponent);
 					listFeatures.add(selectFeature);
 				}
 			}
 			
 			if (StringUtils.isNotEmpty(getPilotProject())) {
 				String id = getPilotProject();
 				List<ApplicationInfo> pilotProjects = (List<ApplicationInfo>)getSessionAttribute(REQ_PILOT_PROJECTS);
 				for (ApplicationInfo applicationInfo : pilotProjects) {
 					if(applicationInfo.getId().equals(id)) {
 						List<String> pilotModules = applicationInfo.getSelectedModules();
 						if (CollectionUtils.isNotEmpty(pilotModules)) {
 							for (String pilotModule : pilotModules) {
 								SelectedFeature selectFeature = createArtifactInformation(pilotModule);
 								listFeatures.add(selectFeature);
 							}
 						}
 						
 						List<String> pilotJSLibs = applicationInfo.getSelectedJSLibs();
 						if (CollectionUtils.isNotEmpty(pilotJSLibs)) {
 							for (String pilotJSLib : pilotJSLibs) {
 								SelectedFeature selectFeature = createArtifactInformation(pilotJSLib);
 								listFeatures.add(selectFeature);
 							}
 						}
 						
 						List<String> pilotComponents = applicationInfo.getSelectedComponents();
 						if (CollectionUtils.isNotEmpty(pilotComponents))	{
 							for (String pilotComponent : pilotComponents) {
 								SelectedFeature selectFeature = createArtifactInformation(pilotComponent);
 								listFeatures.add(selectFeature);
 							}
 						}
 					}
 				}
 			}
 			
 			setReqAttribute(REQ_SELECTED_FEATURES, listFeatures);
 			projectInfo.setAppInfos(Collections.singletonList(createApplicationInfo(appInfo)));
 			setReqAttribute(REQ_OLD_APPDIR, getOldAppDirName());
 			setSessionAttribute(getAppId() + SESSION_APPINFO, projectInfo);
 		} catch (Exception e) {
 		    
 		}
     	
 	    return APP_FEATURES;
 	}
 	
 	private SelectedFeature createArtifactInformation(String selectedModule) throws PhrescoException {
 		
 		SelectedFeature slctFeature = new SelectedFeature();
 		ArtifactInfo artifactInfo = getServiceManager().getArtifactInfo(selectedModule);
 		
		slctFeature.setDispName(artifactInfo.getName());
 		slctFeature.setDispValue(artifactInfo.getVersion());
 		slctFeature.setVersionID(artifactInfo.getId());
 		slctFeature.setModuleId(artifactInfo.getArtifactGroupId());
 		
 		String artifactGroupId = artifactInfo.getArtifactGroupId();
 		ArtifactGroup artifactGroupInfo = getServiceManager().getArtifactGroupInfo(artifactGroupId);
 		slctFeature.setType(artifactGroupInfo.getType().name());
 		
 		return slctFeature;
 		
 	}
 
 	/**
      * To validate the form fields
      * @return
      * @throws PhrescoException 
      */
     public String validateForm() throws PhrescoException {
     	if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Features.validateForm()");
         }
     	
     	boolean hasError = false;
     	if (StringUtils.isEmpty(getName())) {
     		 setNameError(getText(ERROR_NAME));
              hasError = true;
     	}
     	
     	if (StringUtils.isEmpty(getCode())) {
     		setCodeError(getText(ERROR_CODE));
             hasError = true;
     	}
     	
     	if (hasError) {
             setErrorFound(true);
         }
     	
     	return SUCCESS;
     }
 	
 	private ApplicationInfo createApplicationInfo(ApplicationInfo appInfo) {
     	appInfo.setId(getAppId());
     	appInfo.setName(getName());
     	appInfo.setAppDirName(getAppDir());
     	appInfo.setCode(getCode());
     	appInfo.setDescription(getDescription());
     	appInfo.setVersion(getApplicationVersion());
     	TechnologyInfo techInfo = new TechnologyInfo();
     	techInfo.setId(getTechnology());
     	techInfo.setVersion(getTechnologyVersion());
 		appInfo.setTechInfo(techInfo );
 		if (StringUtils.isNotEmpty(getPilotProject())) {
 			Element element = new Element();
 			element.setId(getPilotProject());
 			appInfo.setPilotInfo(element);
 		}
 		appInfo.setSelectedWebservices(getWebservice());
 		List<ArtifactGroupInfo> selectedServers = new ArrayList<ArtifactGroupInfo>();
 		if (CollectionUtils.isNotEmpty(getServer())) {
 			for (String serverId : getServer()) {
 				if(StringUtils.isNotEmpty(serverId)) {
 					ArtifactGroupInfo artifactGroupInfo = new ArtifactGroupInfo();
 					artifactGroupInfo.setArtifactGroupId(serverId);
 					artifactGroupInfo.setArtifactInfoIds(Arrays.asList(getReqParameterValues(serverId)));
 					selectedServers.add(artifactGroupInfo);
 					appInfo.setSelectedServers(selectedServers);
 				}
 			}
 		}
     	
     	
     	List<ArtifactGroupInfo> selectedDatabases = new ArrayList<ArtifactGroupInfo>();
     	if (CollectionUtils.isNotEmpty(getDatabase())) {
     		
 			for (String databaseId : getDatabase()) {
 				ArtifactGroupInfo artifactGroupInfo = new ArtifactGroupInfo();
 				artifactGroupInfo.setArtifactGroupId(databaseId);
 				artifactGroupInfo.setArtifactInfoIds(Arrays.asList(getReqParameterValues(databaseId)));
 				selectedDatabases.add(artifactGroupInfo);
 				appInfo.setSelectedDatabases(selectedDatabases);
 			}
 			
 		}
     	return appInfo;
 	}
 	
 	public String showFeatureConfigPopup() throws PhrescoException {
 	    setConfigTemplateType(CONFIG_FEATURES);
         setReqAttribute(REQ_FEATURE_NAME, getFeatureName());
         List<PropertyTemplate> propertyTemplates = getTemplateConfigFile();
         setReqAttribute(REQ_PROPERTIES, propertyTemplates);
         setReqAttribute(REQ_SELECTED_TYPE, getSelectedType());
         
 	    return SUCCESS;
 	}
 	
 	public String configureFeature() {
 	    try {
             List<PropertyTemplate> propertyTemplates = getTemplateConfigFile();
             Properties properties = new Properties();
             for (PropertyTemplate propertyTemplate : propertyTemplates) {
                 String key  = propertyTemplate.getKey();
                 String value = getReqParameter(key);
                 properties.setProperty(key, value);
             }
             String[] keys = getReqParameterValues(REQ_KEY);
             String[] values = getReqParameterValues(REQ_VALUE);
             if (!ArrayUtils.isEmpty(keys) && !ArrayUtils.isEmpty(values)) {
                 for (int i = 0; i < keys.length; i++) {
                     if (StringUtils.isNotEmpty(keys[i]) && StringUtils.isNotEmpty(values[i])) {
                         properties.setProperty(keys[i], values[i]);
                     }
                 }
             }
             Configuration configuration = new Configuration();
             configuration.setName(getFeatureName());
             configuration.setProperties(properties);
             List<Configuration> configs = new ArrayList<Configuration>();
             configs.add(configuration);
             getApplicationProcessor().postFeatureConfiguration(getApplicationInfo(), configs, getFeatureName());
         } catch (PhrescoException  e) {
             return showErrorPopup(e, getText(EXCEPTION_FEATURE_MANIFEST_NOT_AVAILABLE));
         }
 	    
 	    return SUCCESS;
 	}
 	
 	private List<PropertyTemplate> getTemplateConfigFile() throws PhrescoException {
 	    List<PropertyTemplate> propertyTemplates = new ArrayList<PropertyTemplate>();
 	    try {
             List<Configuration> featureConfigurations = getApplicationProcessor().preFeatureConfiguration(getApplicationInfo(), getFeatureName());
             for (Configuration featureConfiguration : featureConfigurations) {
                 Properties properties = featureConfiguration.getProperties();
                 Set<Object> keySet = properties.keySet();
                 for (Object key : keySet) {
                     String keyStr = (String) key;
                     String value = properties.getProperty(keyStr);
                     String dispName = keyStr.replace(".", " ");
                     PropertyTemplate propertyTemplate = new PropertyTemplate();
                     propertyTemplate.setKey(keyStr);
                     propertyTemplate.setName(dispName);
                     //propertyTemplate.setPossibleValues(Collections.singleton(value));
                     propertyTemplates.add(propertyTemplate);
                 }
                 setReqAttribute(REQ_HAS_CUSTOM_PROPERTY, true);
             }
 	    } catch (PhrescoException e) {
 	        throw new PhrescoException(e);
 	    }
 	    
 	    return propertyTemplates;
 	}
 	
 	public String listFeatures() throws PhrescoException {
 		List<ArtifactGroup> moduleGroups = getServiceManager().getFeatures(getCustomerId(), getTechnologyId(), getType());
 		setReqAttribute(REQ_FEATURES_MOD_GRP, moduleGroups);
 		setReqAttribute(REQ_FEATURES_TYPE, getType());
 		setReqAttribute(REQ_APP_ID, getAppId());
 		setReqAttribute(REQ_TECHNOLOGY, getTechnologyId());
 		
 		return APP_FEATURES_LIST;
 	}
 	
 	public String fetchDefaultFeatures() {
 		Gson gson = new Gson();
 		String json = gson.toJson(getArtifactGroups());
 		List<ArtifactGroup> ArtifactGroups = gson.fromJson(json, new TypeToken<List<ArtifactGroup>>(){}.getType());
 		for (ArtifactGroup artifactGroup : ArtifactGroups) {
 			List<ArtifactInfo> versions = artifactGroup.getVersions();
 			for (ArtifactInfo artifactInfo : versions) {
 				List<RequiredOption> appliesTo = artifactInfo.getAppliesTo();
 				for (RequiredOption requiredOption : appliesTo) {
 					if (requiredOption.isRequired()) {
 						depArtifactGroupNames.add(artifactGroup.getName());
 						depArtifactInfoIds.add(artifactInfo.getId());
 					}
 				}
 				
 			}
 		}
 		return SUCCESS;
 	}
 	
 	public String fetchDependentFeatures() {
 		Gson gson = new Gson();
 		String json = gson.toJson(getArtifactGroups());
 		List<ArtifactGroup> artifactGroups = gson.fromJson(json, new TypeToken<List<ArtifactGroup>>(){}.getType());
 		for (ArtifactGroup artifactGroup : artifactGroups) {
 			List<ArtifactInfo> versions = artifactGroup.getVersions();
 			for (ArtifactInfo artifactInfo : versions) {
 				if (artifactInfo.getId().equals(getModuleId())) {
 				    List<String> dependencyIds = artifactInfo.getDependencyIds();
 				    if (CollectionUtils.isNotEmpty(artifactInfo.getDependencyIds())) {
 				        dependencyIds.addAll(artifactInfo.getDependencyIds());
 				    }
 				}
 			}
 		}
 		
 		//To get the artifactgroup name for the dependent artifactInfo ids
 		if (CollectionUtils.isNotEmpty(getDependencyIds())) {
     		for (String dependencyId : getDependencyIds()) {
     			for (ArtifactGroup artifactGroup : artifactGroups) {
     				List<ArtifactInfo> versions = artifactGroup.getVersions();
     				for (ArtifactInfo artifactInfo : versions) {
     					if (artifactInfo.getId().equals(dependencyId)) {
     						depArtifactGroupNames.add(artifactGroup.getName());
     					}
     				}
     			}
     		}
     		setDependency(true);
 		}
 		
 		return SUCCESS;
 	}
 	
 	/*public String features1() {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.features()");
 		}
 		String returnPage = APP_FEATURES_ONE_CLM;
 		boolean left = false;
 		boolean rightBottom = false;
 		boolean right = false;
 
 		try {
 			ApplicationInfo appInfo = null;
 			ProjectAdministrator administrator = PhrescoFrameworkFactory
 					.getProjectAdministrator();
 			if (validate(administrator) && StringUtils.isEmpty(fromPage)) {
 				return Action.SUCCESS;
 			}
 			if (StringUtils.isEmpty(fromPage)
 					&& StringUtils.isNotEmpty(projectCode)) { // previous button
 																// clicked
 				appInfo = (ApplicationInfo) getHttpSession().getAttribute(
 						projectCode);
 			} else if (StringUtils.isNotEmpty(fromPage)) { // For edit project
 				appInfo = administrator.getProject(projectCode).getApplicationInfo();
 				if (description != null) {
 					appInfo.setDescription(description);
 				}
 				//TODO:Need to Handle
 //				if (externalCode != null) {
 //					appInfo.setProjectCode(externalCode);
 //				}
 //				if (StringUtils.isNotEmpty(projectVersion)) {
 //					appInfo.setVersion(projectVersion);
 //				}
 				//TODO:Need to Handle
 //				if (groupId != null) {
 //					appInfo.setGroupId(groupId);
 //				}
 //				if (artifactId != null) {
 //					appInfo.setArtifactId(artifactId);
 //				}
 					
 				application = appInfo.getTechInfo().getAppTypeId();
 				technology = appInfo.getTechInfo().getVersion();
 
 				setTechnology(appInfo, administrator);
 			} else { // For creating new project
 				appInfo = new ApplicationInfo();
 			}
 			if (StringUtils.isEmpty(fromPage)) {
 				setAppInfos(appInfo, administrator);
 			}
 
 			getHttpRequest().setAttribute(REQ_TEMP_SELECTED_PILOT_PROJ, getHttpRequest().getParameter(REQ_TEMP_SELECTED_PILOT_PROJ));
 
 			String selectedFeatures = getHttpRequest().getParameter(REQ_TEMP_SELECTEDMODULES);
 			if (StringUtils.isNotEmpty(selectedFeatures)) {
 				Map<String, String> mapFeatures = ApplicationsUtil.stringToMap(selectedFeatures);
 				getHttpRequest().setAttribute(REQ_TEMP_SELECTEDMODULES, mapFeatures);
 			}
 
 			String selectedJsLibs = getHttpRequest().getParameter(REQ_SELECTED_JSLIBS);
 			if (StringUtils.isNotEmpty(selectedJsLibs)) {
 				Map<String, String> mapJsLibs = ApplicationsUtil.stringToMap(selectedJsLibs);
 				getHttpRequest().setAttribute(REQ_TEMP_SELECTED_JSLIBS, mapJsLibs);
 			}
 
 			setFeaturesInRequest(administrator, appInfo);
 			getHttpRequest().setAttribute(REQ_APPINFO, appInfo);
 
 			List<ArtifactGroup> coreModules = (List<ArtifactGroup>) getHttpRequest().getAttribute(REQ_CORE_MODULES);
 			List<ArtifactGroup> customModules = (List<ArtifactGroup>) getHttpRequest().getAttribute(REQ_CUSTOM_MODULES);
 			List<ArtifactGroup> allJsLibs = (List<ArtifactGroup>) getHttpRequest().getAttribute(REQ_ALL_JS_LIBS);
 
 			// Assigning the position of the coreModule
 			if (CollectionUtils.isNotEmpty(coreModules)) { // Assigning coreModule to the left position
 				left = true;
 				getHttpRequest().setAttribute(REQ_FEATURES_FIRST_MDL_CAT, REQ_EXTERNAL_FEATURES);
 				getHttpRequest().setAttribute(REQ_FEATURES_LEFT_MODULES, coreModules);
 			}
 
 			// Assigning the position of the customModule
 			if (!left && CollectionUtils.isNotEmpty(customModules)) { // Assigning customModule to the left position
 				left = true;
 				getHttpRequest().setAttribute(REQ_FEATURES_FIRST_MDL_CAT, REQ_CUSTOM_FEATURES);
 				getHttpRequest().setAttribute(REQ_FEATURES_LEFT_MODULES, customModules);
 			} else if (left && CollectionUtils.isNotEmpty(customModules)) { // Assigning customModule to the right bottom position
 				right = true;
 				getHttpRequest().setAttribute(REQ_FEATURES_SECOND_MDL_CAT, REQ_CUSTOM_FEATURES);
 				getHttpRequest().setAttribute(REQ_FEATURES_RIGHT_MODULES, customModules);
 			}
 
 			// Assigning the position of the JSLibraries
 			if (left && right && CollectionUtils.isNotEmpty(allJsLibs)) { // Assigning JSLibraries to the right bottom position
 				rightBottom = true;
 			} else if (left && !right && CollectionUtils.isNotEmpty(allJsLibs)) { // Assigning JSLibraries to the right position
 				right = true;
 				getHttpRequest().setAttribute(REQ_FEATURES_SECOND_MDL_CAT, REQ_JS_LIBS);
 				getHttpRequest().setAttribute(REQ_FEATURES_RIGHT_MODULES, allJsLibs);
 			} else if (!left && !right && CollectionUtils.isNotEmpty(allJsLibs)) { // Assigning JSLibraries to the left position
 				left = true;
 				getHttpRequest().setAttribute(REQ_FEATURES_FIRST_MDL_CAT, REQ_JS_LIBS);
 				getHttpRequest().setAttribute(REQ_FEATURES_LEFT_MODULES, allJsLibs);
 			}
 
 			if (left && right && rightBottom) {
 				returnPage = APP_FEATURES_THREE_CLM;
 			} else if (left && right && !rightBottom) {
 				returnPage = APP_FEATURES_TWO_CLM;
 			}
 			getHttpRequest().setAttribute(REQ_CONFIG_SERVER_NAMES, configServerNames);
 			getHttpRequest().setAttribute(REQ_CONFIG_DB_NAMES, configDbNames);
 			FrameworkConfiguration configuration = PhrescoFrameworkFactory.getFrameworkConfig();
 			getHttpRequest().setAttribute(REQ_SERVER_URL, configuration.getServerPath());
 			
 		} catch (PhrescoException e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Features.list()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 			new LogErrorReport(e, "Feature list");
 		}
 		
 		return returnPage;
 	}
 
 	private void setAppInfos(ApplicationInfo appInfo, ProjectAdministrator administrator) throws PhrescoException {
 		HttpServletRequest request = getHttpRequest();
 		appInfo.setName(name);
 		appInfo.setCode(code);
 		//TODO:Need to handle
 //		if (externalCode != null) {
 //			appInfo.setProjectCode(externalCode);
 //		}
 //		if (groupId != null) {
 //			appInfo.setGroupId(groupId);
 //		}
 //		if (artifactId != null) {
 //			appInfo.setArtifactId(artifactId);
 //		}
 //		appInfo.setVersion(projectVersion);
 		appInfo.setDescription(description);
 		appInfo.setTechInfo(new TechnologyInfo(application, technology));
 		String pilotProjectName = getHttpRequest().getParameter(REQ_SELECTED_PILOT_PROJ);
 		appInfo.setPilotInfo(new Element(pilotProjectName));
 
 		setTechnology(appInfo, administrator);
 		FrameworkUtil.setAppInfoDependents(request, getCustomerId());
 	}
 
 	private void setTechnology(ApplicationInfo appInfo, ProjectAdministrator administrator) throws PhrescoException {
 		ProjectInfo tempprojectInfo = null;
 		//TODO:Need to handle
 //		Technology selectedTechnology = administrator.getApplicationType(application).getTechonology(technology);
 		Technology technology = new Technology();
 
 		//TODO:Need to handle
 		technology.setId(selectedTechnology.getId());
 		technology.setName(selectedTechnology.getName());
 		if (StringUtils.isEmpty(fromPage)) {
 			technology.setVersions(techVersion);
 		} else {
 			tempprojectInfo = administrator.getProject(projectCode).getApplicationInfo();
 			List<String> projectInfoTechVersions = new ArrayList<String>();
 			List<String> tempPrjtInfoTechVersions = tempprojectInfo.getTechnology().getVersions();
 			if (tempPrjtInfoTechVersions != null && CollectionUtils.isNotEmpty(tempPrjtInfoTechVersions)) {
 				projectInfoTechVersions.addAll(tempprojectInfo.getTechnology().getVersions());
 				technology.setVersions(projectInfoTechVersions);
 			}
 		}
 		
 		if (StringUtils.isNotEmpty(fromPage)) {// For project edit
 			technology.setJsLibraries(appInfo.getTechnology()
 					.getJsLibraries());
 			technology.setModules(appInfo.getTechnology().getModules());
 		}
 
 		List<Server> servers = selectedTechnology.getServers();
 		List<Database> databases = selectedTechnology.getDatabases();
 		List<WebService> webservices = selectedTechnology.getWebservices();
 		
 		String selectedServers = getHttpRequest().getParameter("selectedServers");
 		String selectedDatabases = getHttpRequest().getParameter("selectedDatabases");
 		String[] selectedWebservices = getHttpRequest().getParameterValues(REQ_WEBSERVICES);
 		boolean isEmailSupported = false;
 		
 		if (StringUtils.isNotEmpty(fromTab)) {
 			if (selectedServers != null) {
 				List<String> listTempSelectedServers = null;
 				if (StringUtils.isNotEmpty(selectedServers)) {
 					listTempSelectedServers = new ArrayList<String>(
 							Arrays.asList(selectedServers.split("#SEP#")));
 				}
 				//TODO:Need to handle
 //				technology.setServers(ApplicationsUtil.getSelectedServers(servers, listTempSelectedServers));
 			}
 			
 			if (selectedDatabases != null) {
 				List<String> listTempSelectedDatabases = null;
 				if (StringUtils.isNotEmpty(selectedDatabases)) {
 					listTempSelectedDatabases = new ArrayList<String>(
 							Arrays.asList(selectedDatabases.split("#SEP#")));
 				}
 				//TODO:Need to handle
 //				technology.setDatabases(ApplicationsUtil.getSelectedDatabases(databases, listTempSelectedDatabases));
 			}
 			//TODO:Need to handle
 //			if (selectedWebservices != null) {
 //				technology.setWebservices(ApplicationsUtil.getSelectedWebservices(
 //						webservices, ApplicationsUtil.getArrayListFromStrArr(selectedWebservices)));
 //			}
 			
 			if (getHttpRequest().getParameter(REQ_EMAIL_SUPPORTED) != null) {
 				isEmailSupported = Boolean.parseBoolean(getHttpRequest().getParameter(REQ_EMAIL_SUPPORTED));
 			}
 //			technology.setEmailSupported(isEmailSupported);//TODO:Need to handle
 
 		} else {
 		  //TODO:Need to handle
 //			if (tempprojectInfo != null) {
 //				technology.setServers(tempprojectInfo.getTechnology().getServers());
 //				technology.setDatabases(tempprojectInfo.getTechnology().getDatabases());
 //				technology.setWebservices(tempprojectInfo.getTechnology().getWebservices());
 //				technology.setEmailSupported(tempprojectInfo.getTechnology().isEmailSupported());
 //			}
 		}
 		
 //		appInfo.setTechnology(technology);//TODO:Need to handle
 	}
 
     private boolean validate(ProjectAdministrator administrator) throws PhrescoException {
     	isValidated = true;
     	if (StringUtils.isEmpty(name)) {
     		setNameError(ERROR_NAME);
             return true;
         }
     	if (StringUtils.isEmpty(name.trim())) {
     		setNameError(ERROR_INVALID_NAME);
             return true;
         }
         if (administrator.getProject(code) != null) {
         	setNameError(ERROR_DUPLICATE_NAME);
             return true;
         }
         return false;
     }
     
 	public void setFeaturesInRequest(ProjectAdministrator administrator,
 			ApplicationInfo appInfo) throws PhrescoException {
 		if (debugEnabled) {
 			S_LOGGER.debug("Entering Method  Features.setFeaturesInRequest()");
 		}
 		//TODO:Need to handle
 		Technology selectedTechnology = appInfo.getTechnology();
 		ApplicationType applicationType = administrator
 				.getApplicationType(appInfo.getApplication());
 		Technology techonology = applicationType
 				.getTechonology(selectedTechnology.getId());
 
 		getHttpRequest().setAttribute(REQ_ALL_JS_LIBS,
 				techonology.getJsLibraries());
 		List<ModuleGroup> coreModule = (List<ModuleGroup>) administrator
 				.getCoreModules(techonology);
 		List<ModuleGroup> customModule = (List<ModuleGroup>) administrator
 				.getCustomModules(techonology);
 		if (CollectionUtils.isNotEmpty(coreModule)) {
 			getHttpRequest().setAttribute(REQ_CORE_MODULES, coreModule);
 		}
 
 		if (CollectionUtils.isNotEmpty(customModule)) {
 			getHttpRequest().setAttribute(REQ_CUSTOM_MODULES, customModule);
 		}
 
 		// This attribute for Pilot Project combo box
 		getHttpRequest().setAttribute(REQ_PILOTS_NAMES,
 				ApplicationsUtil.getPilotNames(selectedTechnology.getId()));
 
 		if (CollectionUtils.isNotEmpty(selectedTechnology.getModules())) {
 			// pilotModules.putAll(ApplicationsUtil.getMapFromModuleGroups(selectedTechnology.getModules()));
 			getHttpRequest().setAttribute(
 					REQ_ALREADY_SELECTED_MODULES,
 					ApplicationsUtil.getMapFromModuleGroups(selectedTechnology
 							.getModules()));
 		}
 
 		if (CollectionUtils.isNotEmpty(selectedTechnology.getJsLibraries())) {
 			getHttpRequest().setAttribute(
 					REQ_ALREADY_SELECTED_JSLIBS,
 					ApplicationsUtil.getMapFromModuleGroups(selectedTechnology
 							.getJsLibraries()));
 			// pilotJsLibs.putAll(ApplicationsUtil.getMapFromModuleGroups(selectedTechnology.getJsLibraries()));
 		}
 
 		getHttpRequest().setAttribute(REQ_FROM_PAGE, fromPage);
 		getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 	}
 
 	public String getPilotProjectModules() {
 		String techId = getHttpRequest().getParameter(REQ_TECHNOLOGY);
 		pilotModules = new ArrayList<String>(ApplicationsUtil
 				.getPilotModuleIds(techId).keySet());
 		pilotJSLibs = new ArrayList<String>(ApplicationsUtil.getPilotJsLibIds(
 				techId).keySet());
 		pilotModules.addAll(pilotJSLibs);
 		return SUCCESS;
 	}
 	
 	public String fetchDefaultModules() {
 		String techId = getHttpRequest().getParameter(REQ_TECHNOLOGY);
 		try {
 			defaultModules = new ArrayList<String>();
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			//TODO:Need to handle
 			Technology technology = administrator.getTechnology(techId);
 			List<ModuleGroup> coreModules = (List<ModuleGroup>) administrator.getCoreModules(technology);
 			if (CollectionUtils.isNotEmpty(coreModules) && coreModules != null) {
 				for (ModuleGroup coreModule : coreModules) {
 					if (coreModule.isRequired()) {
 						defaultModules.add(coreModule.getId());
 					}
 				}
 			}
 			List<ModuleGroup> customModules = (List<ModuleGroup>) administrator.getCustomModules(technology);
 			if (CollectionUtils.isNotEmpty(customModules) && customModules != null) {
 				for (ModuleGroup customModule : customModules) {
 					if (customModule.isRequired()) {
 						defaultModules.add(customModule.getId());
 					}
 				}
 			}
 			
 			List<ModuleGroup> jsLibraries = technology.getJsLibraries();
 			if (CollectionUtils.isNotEmpty(jsLibraries) && jsLibraries != null) {
 				for (ModuleGroup jsLibrary : jsLibraries) {
 					if (jsLibrary.isRequired()) {
 						defaultModules.add(jsLibrary.getId());
 					}
 				}
 			}
 		} catch (Exception e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of fetchDefaultModules()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 			new LogErrorReport(e, "Feature fetchDefaultModules");
 		}
 		
 		return SUCCESS;
 	}
 
 	public String checkDependency() {
 		try {
 		  //TODO:Need to handle
 			List<ModuleGroup> allModules = getAllModule();
 
 			for (ModuleGroup module : allModules) {
 				if (module.getId().equals(moduleId)) {
 					Module checkedVersion = module.getVersion(version);
 					if (StringUtils.isNotEmpty(preVersion)) {
 						Module preVerModule = module.getVersion(preVersion);
 						preDependentIds = ApplicationsUtil.getIds(preVerModule
 								.getDependentModules());
 						preDependentVersions = ApplicationsUtil
 								.getDependentVersions();
 					}
 					if (checkedVersion != null) {
 						dependentIds = ApplicationsUtil.getIds(checkedVersion
 								.getDependentModules());
 						dependentVersions = ApplicationsUtil
 								.getDependentVersions();
 					}
 				}
 			}
 		} catch (Exception e) {
 			if (debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Features.checkDependency()"
 						+ FrameworkUtil.getStackTraceAsString(e));
 			}
 			new LogErrorReport(e, "Feature Select Dependency");
 		}
 		return SUCCESS;
 	}
 
 	public List<ArtifactGroup> getAllModule() throws PhrescoException {
 		ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 		//TODO:Need to handle
 //		Technology technology = administrator.getTechnology(techId);
 //		if (REQ_CORE_MODULE.equals(moduleType)) {
 //			return administrator.getCoreModules(technology);
 //		}
 //
 //		if (REQ_CUSTOM_MODULE.equals(moduleType)) {
 //			return administrator.getCustomModules(technology);
 //		}
 //
 //		if (REQ_JSLIB_MODULE.equals(moduleType)) {
 //			return technology.getJsLibraries();
 //		}
 
 		return null;
 	}*/
 
 	public Collection<String> getDependentIds() {
 		return dependentIds;
 	}
 
 	public void setDependentIds(Collection<String> dependentIds) {
 		this.dependentIds = dependentIds;
 	}
 
 	public Collection<String> getDependentVersions() {
 		return dependentVersions;
 	}
 
 	public void setDependentVersions(Collection<String> dependentVersions) {
 		this.dependentVersions = dependentVersions;
 	}
 
 	public Collection<String> getPreDependentIds() {
 		return preDependentIds;
 	}
 
 	public void setPreDependentIds(Collection<String> dependentIds) {
 		this.preDependentIds = dependentIds;
 	}
 
 	public Collection<String> getPreDependentVersions() {
 		return preDependentVersions;
 	}
 
 	public void setPreDependentVersions(Collection<String> dependentVersions) {
 		this.preDependentVersions = dependentVersions;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	public String getPreVersion() {
 		return preVersion;
 	}
 
 	public void setPreVersion(String preVersion) {
 		this.preVersion = preVersion;
 	}
 
 	public String getModuleId() {
 		return moduleId;
 	}
 
 	public void setModuleId(String moduleId) {
 		this.moduleId = moduleId;
 	}
 
 	public String getModuleType() {
 		return moduleType;
 	}
 
 	public void setModuleType(String moduleType) {
 		this.moduleType = moduleType;
 	}
 
 	public String getTechId() {
 		return techId;
 	}
 
 	public void setTechId(String techId) {
 		this.techId = techId;
 	}
 
 	public String getProjectCode() {
 		return projectCode;
 	}
 
 	public void setProjectCode(String projectCode) {
 		this.projectCode = projectCode;
 	}
 
 	public String getFromPage() {
 		return fromPage;
 	}
 
 	public void setFromPage(String fromPage) {
 		this.fromPage = fromPage;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getCode() {
 		return code;
 	}
 
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getApplication() {
 		return application;
 	}
 
 	public void setApplication(String application) {
 		this.application = application;
 	}
 
 	public String getTechnology() {
 		return technology;
 	}
 
 	public void setTechnology(String technology) {
 		this.technology = technology;
 	}
 
 	public String getNameError() {
 		return nameError;
 	}
 
 	public void setNameError(String nameError) {
 		this.nameError = nameError;
 	}
 
 	public List<String> getPilotModules() {
 		return pilotModules;
 	}
 
 	public void setPilotModules(List<String> pilotModules) {
 		this.pilotModules = pilotModules;
 	}
 
 	public List<String> getPilotJSLibs() {
 		return pilotJSLibs;
 	}
 
 	public void setPilotJSLibs(List<String> pilotJSLibs) {
 		this.pilotJSLibs = pilotJSLibs;
 	}
 
 	public boolean isValidated() {
 		return isValidated;
 	}
 
 	public void setValidated(boolean isValidated) {
 		this.isValidated = isValidated;
 	}
 
 	public List<String> getTechVersion() {
 		return techVersion;
 	}
 
 	public void setTechVersion(List<String> techVersion) {
 		this.techVersion = techVersion;
 	}
 
 	public String getConfigServerNames() {
 		return configServerNames;
 	}
 
 	public void setConfigServerNames(String configServerNames) {
 		this.configServerNames = configServerNames;
 	}
 
 	public String getConfigDbNames() {
 		return configDbNames;
 	}
 
 	public void setConfigDbNames(String configDbNames) {
 		this.configDbNames = configDbNames;
 	}
 
 	public String getExternalCode() {
 		return externalCode;
 	}
 
 	public void setExternalCode(String externalCode) {
 		this.externalCode = externalCode;
 	}
 	
 	public String getFromTab() {
 		return fromTab;
 	}
 
 	public void setFromTab(String fromTab) {
 		this.fromTab = fromTab;
 	}
 	
 	public List<String> getDefaultModules() {
 		return defaultModules;
 	}
 
 	public void setDefaultModules(List<String> defaultModules) {
 		this.defaultModules = defaultModules;
 	}
 	
 	public String getCustomerId() {
         return customerId;
     }
 
     public void setCustomerId(String customerId) {
         this.customerId = customerId;
     }
     
     public List<String> getServerVersion() {
 		return serverVersion;
 	}
     
 	public void setServerVersion(List<String> serverVersion) {
 		this.serverVersion = serverVersion;
 	}
 	
 	public List<String> getDatabaseVersion() {
 		return databaseVersion;
 	}
 	
 	public void setDatabaseVersion(List<String> databaseVersion) {
 		this.databaseVersion = databaseVersion;
 	}
 	
 	public List<String> getWebservice() {
 		return webservice;
 	}
 	
 	public void setWebservice(List<String> webservice) {
 		this.webservice = webservice;
 	}
 	
 	public String getApplicationVersion() {
 		return applicationVersion;
 	}
 
 	public void setApplicationVersion(String applicationVersion) {
 		this.applicationVersion = applicationVersion;
 	}
 
 	public String getTechnologyId() {
 		return technologyId;
 	}
 
 	public void setTechnologyId(String technologyId) {
 		this.technologyId = technologyId;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getAppId() {
 		return appId;
 	}
 
 	public void setAppId(String appId) {
 		this.appId = appId;
 	}
 
 	public String getPilotProject() {
 		return pilotProject;
 	}
 
 	public void setPilotProject(String pilotProject) {
 		this.pilotProject = pilotProject;
 	}
 
 	public List<String> getServer() {
 		return server;
 	}
 
 	public void setServer(List<String> server) {
 		this.server = server;
 	}
 
 	public List<String> getDatabase() {
 		return database;
 	}
 
 	public String getAppDir() {
 		return appDir;
 	}
 
 	public void setAppDir(String appDir) {
 		this.appDir = appDir;
 	}
 
 	public void setDatabase(List<String> database) {
 		this.database = database;
 	}
 
 	public String getOldAppDirName() {
 		return oldAppDirName;
 	}
 
 	public void setOldAppDirName(String oldAppDirName) {
 		this.oldAppDirName = oldAppDirName;
 	}
 
     public String getConfigTemplateType() {
         return configTemplateType;
     }
 
     public void setConfigTemplateType(String configTemplateType) {
         this.configTemplateType = configTemplateType;
     }
 
     public void setFeatureName(String featureName) {
         this.featureName = featureName;
     }
 
     public String getFeatureName() {
         return featureName;
     }
 
 	public List<ArtifactGroup> getArtifactGroups() {
 		return artifactGroups;
 	}
 
 	public void setArtifactGroups(List<ArtifactGroup> artifactGroups) {
 		this.artifactGroups = artifactGroups;
 	}
 
 	public List<String> getDepArtifactGroupNames() {
 		return depArtifactGroupNames;
 	}
 
 	public List<String> getDepArtifactInfoIds() {
 		return depArtifactInfoIds;
 	}
 
 	public void setDepArtifactGroupNames(List<String> depArtifactGroupNames) {
 		this.depArtifactGroupNames = depArtifactGroupNames;
 	}
 
 	public void setDepArtifactInfoIds(List<String> depArtifactInfoIds) {
 		this.depArtifactInfoIds = depArtifactInfoIds;
 	}
 
 	public String getCodeError() {
 		return codeError;
 	}
 
 	public boolean isErrorFound() {
 		return errorFound;
 	}
 
 	public void setCodeError(String codeError) {
 		this.codeError = codeError;
 	}
 
 	public void setErrorFound(boolean errorFound) {
 		this.errorFound = errorFound;
 	}
 
 	public String getTechnologyVersion() {
 		return technologyVersion;
 	}
 
 	public void setTechnologyVersion(String technologyVersion) {
 		this.technologyVersion = technologyVersion;
 	}
 
 	public List<String> getDependencyIds() {
 		return dependencyIds;
 	}
 
 	public void setDependencyIds(List<String> dependencyIds) {
 		this.dependencyIds = dependencyIds;
 	}
 
     public boolean isDependency() {
         return dependency;
     }
 
     public void setDependency(boolean dependency) {
         this.dependency = dependency;
     }
 
     public String getSelectedType() {
         return selectedType;
     }
 
     public void setSelectedType(String selectedType) {
         this.selectedType = selectedType;
     }
 }
