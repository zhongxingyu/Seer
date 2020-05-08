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
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ApplicationType;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.TechnologyGroup;
 import com.photon.phresco.commons.model.TechnologyInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.framework.api.ProjectManager;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 
 /**
  * Struts Action class for Handling Project related operations 
  * @author jeb
  */
 public class Projects extends FrameworkBaseAction {
 
     private static final long serialVersionUID = 7143239782158726004L;
 
     private static final Logger S_LOGGER = Logger.getLogger(Projects.class);
     private static Boolean s_debugEnabled = S_LOGGER.isDebugEnabled();
     
     private static Map<String, ApplicationType> s_layerMap = new HashMap<String, ApplicationType>();
     private static Map<String, TechnologyGroup> s_technologyGroupMap = new HashMap<String, TechnologyGroup>();
 
     private String projectName = "";
     private String projectCode = "";
     private String projectDesc = "";
     private String projectVersion = "";
     private List<String> layer = new ArrayList<String>(8);
     private String layerId = "";
     private String techGroupId = "";
 
     private List<TechnologyInfo> widgets = new ArrayList<TechnologyInfo>(8);
     private List<String> versions = new ArrayList<String>(8);
     private List<ApplicationInfo> selectedAppInfos = new ArrayList<ApplicationInfo>();
     private String appDirName ="";
 
     private boolean errorFound = false;
     private String projectNameError = "";
     private String projectCodeError = "";
     private String projectVersionError = "";
     private String layerError = "";
     private String mobTechError = "";
     private String appTechError = "";
     private String webTechError = "";
     private String statusFlag = "" ;
     private String id = "";
 
 	/**
      * To get the list of projects
      * @return
      */
     public String list() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.list()");
         }
         
         try {
             ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
             List<ProjectInfo> projects = projectManager.discover(getCustomerId());
             setReqAttribute(REQ_PROJECTS, projects);
             setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
             removeSessionAttribute(projectCode);
             if (IMPORT.equals(getStatusFlag())) {
             	addActionMessage(getText(IMPORT_SUCCESS_PROJECT));
             } else if (UPDATE.equals(getStatusFlag())) {
             	addActionMessage(getText(SUCCESS_PROJECT_UPDATE));
             }
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Projects.list()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_PROJECT_LIST));
         }
 
         return APP_LIST;
     }
 
     /**
      * To get the add project page
      * @return
      */
     public String addProject() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.applicationDetails()");
         }
 
         try {
             List<ApplicationType> layers = getServiceManager().getApplicationTypes(getCustomerId());
             setReqAttribute(REQ_PROJECT_LAYERS, layers);
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Projects.addProject()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_PROJECT_ADD));
         }
 
         return APP_APPLICATION_DETAILS;
     }
 
     /**
      * To get the selected mobile technology's version
      * @return
      */
    public String fetchTechVersions() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.fetchMobileTechVersions()");
         }
 
         try {
             List<TechnologyGroup> techGroups = filterLayer(getLayerId()).getTechGroups();
             TechnologyGroup technologyGroup = filterTechnologyGroup(techGroups, getTechGroupId());
             String techId = getReqParameter(technologyGroup.getId() + REQ_PARAM_NAME_TECHNOLOGY);
             List<TechnologyInfo> techInfos = technologyGroup.getTechInfos();
             if (CollectionUtils.isNotEmpty(techInfos)) {
                 for (TechnologyInfo techInfo : techInfos) {
                     if (techInfo.getId().equals(techId)) {
                         setVersions(techInfo.getTechVersions());
                         break;
                     }
                 }
             }
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Projects.fetchMobileTechVersions()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_PROJECT_MOB_TECH_VERSIONS));
         }
 
         return SUCCESS;
     }
 
     /**
      * To the selected web layer's widgets
      * @return
      */
     public String fetchWebLayerWidgets() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.fetchMobileTechVersions()");
         }
 
         try {
             String techGroupId = getReqParameter(getLayerId() + REQ_PARAM_NAME_TECH_GROUP);
             List<TechnologyGroup> techGroups = filterLayer(getLayerId()).getTechGroups();
             TechnologyGroup technologyGroup = filterTechnologyGroup(techGroups, techGroupId);
             setWidgets(technologyGroup.getTechInfos());
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Projects.fetchWebLayerWidgets()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_PROJECT_WEB_LAYER_WIDGETS));
         }
 
         return SUCCESS;
     }
 
     /**
      * To get the layer based on the given layer id
      * @param layers
      * @param layerName
      * @return
      * @throws PhrescoException 
      */
     private ApplicationType filterLayer(String layerId) throws PhrescoException {
         if (s_layerMap.get(layerId) == null) {
             List<ApplicationType> layers = getServiceManager().getApplicationTypes(getCustomerId());
             if (CollectionUtils.isNotEmpty(layers)) {
                 for (ApplicationType layer : layers) {
                     s_layerMap.put(layer.getId(), layer);
                 }
             }
         }
 
         return s_layerMap.get(layerId);
     }
 
     /**
      * To get the technology group based on the given technology group Id
      * @param technologyGroups
      * @param id
      * @return
      */
     private TechnologyGroup filterTechnologyGroup(List<TechnologyGroup> technologyGroups, String id) {
         if (CollectionUtils.isNotEmpty(technologyGroups)) {
             if (s_technologyGroupMap.get(id) == null) {
                 for (TechnologyGroup technologyGroup : technologyGroups) {
                     s_technologyGroupMap.put(technologyGroup.getId(), technologyGroup);
                 }
             }
         }
 
         return s_technologyGroupMap.get(id);
     }
     
     public String editProject() {
     	ProjectInfo projectInfo = null;
 		try {
 			ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 			projectInfo = projectManager.getProject(getProjectId(), getCustomerId());
 			projectInfo.setId(getProjectId());
 			List<ApplicationType> layers = getServiceManager().getApplicationTypes(getCustomerId());
 	        setReqAttribute(REQ_PROJECT_LAYERS, layers);
 			setReqAttribute(REQ_PROJECT, projectInfo);
 		} catch (PhrescoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "projectDetails";
     	
     }
     
     /**
      * To create the project with the selected applications
      * @return
      */
     public String createProject() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.createProject()");
         }
 
         try {
             PhrescoFrameworkFactory.getProjectManager().create(createProjectInfo(), getServiceManager());
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Projects.createProject()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_PROJECT_CREATE));
         }
 
         return list();
     }
 
     /**
      * To update the project with the selected applications
      * @return
      */
     public String updateProject() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.updateProject()");
         }
 
         try {
         	ProjectInfo projectInfo = createProjectInfo();
         	projectInfo.setId(getId());
             PhrescoFrameworkFactory.getProjectManager().update(projectInfo, getServiceManager(), null);
         } catch (PhrescoException e) {
             if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Projects.updateProject()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_PROJECT_UPDATE));
         }
 
         return list();
     }
     
     /**
      * To get the projectInfo with the selected application infos
      * @return
      * @throws PhrescoException
      */
     private ProjectInfo createProjectInfo() throws PhrescoException {
         ProjectInfo projectInfo = new ProjectInfo();
         projectInfo.setName(getProjectName());
         projectInfo.setVersion(getProjectVersion());
         projectInfo.setDescription(getProjectDesc());
         projectInfo.setProjectCode(getProjectCode());
         projectInfo.setCustomerIds(Collections.singletonList(getCustomerId()));
         List<ApplicationInfo> appInfos = new ArrayList<ApplicationInfo>();
         if (CollectionUtils.isNotEmpty(getLayer())) {
             for (String layerId : getLayer()) {
                 if (LAYER_MOB_ID.equals(layerId)) {
                     getMobileLayerAppInfos(appInfos, layerId);
                 } else {
                     getOtherLayerAppInfos(appInfos, layerId);
                 }
             }
         }
         projectInfo.setAppInfos(appInfos);
         projectInfo.setNoOfApps(appInfos.size());
 
         return projectInfo;
     }
 
     /**
      * To get the application infos for mobile technology
      * @param appInfos
      * @param layerId
      * @return
      * @throws PhrescoException
      */
     private List<ApplicationInfo> getMobileLayerAppInfos(List<ApplicationInfo> appInfos, String layerId) throws PhrescoException {
         String[] techGroupIds = getReqParameterValues(layerId + REQ_PARAM_NAME_TECH_GROUP);
         if (!ArrayUtils.isEmpty(techGroupIds)) {
             for (String techGroupId : techGroupIds) {
                 String techId = getHttpRequest().getParameter(techGroupId + REQ_PARAM_NAME_TECHNOLOGY);
                 String version = getHttpRequest().getParameter(techGroupId + REQ_PARAM_NAME_VERSION);
                 boolean phoneEnabled = Boolean.parseBoolean(getReqParameter(techGroupId + REQ_PARAM_NAME_PHONE));
                 boolean tabletEnabled = Boolean.parseBoolean(getReqParameter(techGroupId + REQ_PARAM_NAME_TABLET));
                 appInfos.add(getAppInfo(getProjectName() + HYPHEN + techGroupId, techId, version, phoneEnabled, tabletEnabled));
             }
         }
 
         return appInfos;
     }
 
     /**
      * To get the application infos for app and web technologies
      * @param appInfos
      * @param layerId
      * @return
      * @throws PhrescoException
      */
     private List<ApplicationInfo> getOtherLayerAppInfos(List<ApplicationInfo> appInfos, String layerId) throws PhrescoException {
         String techId = getReqParameter(layerId + REQ_PARAM_NAME_TECHNOLOGY);
         String version = getReqParameter(layerId + REQ_PARAM_NAME_VERSION);
         appInfos.add(getAppInfo(getProjectName() + HYPHEN + techId, techId, version, false, false));
 
         return appInfos;
     }
 
     /**
      * To get the single application info for the given technology
      * @param dirName
      * @param techId
      * @param version
      * @param phoneEnabled
      * @param tabletEnabled
      * @return
      * @throws PhrescoException
      */
     private ApplicationInfo getAppInfo(String dirName, String techId, String version, boolean phoneEnabled, boolean tabletEnabled) throws PhrescoException {
         ApplicationInfo applicationInfo = new ApplicationInfo();
         TechnologyInfo techInfo = new TechnologyInfo();
         techInfo.setId(techId);
         techInfo.setVersion(version);
         applicationInfo.setTechInfo(techInfo);
         applicationInfo.setName(dirName);
         applicationInfo.setAppDirName(dirName);
         applicationInfo.setPhoneEnabled(phoneEnabled);
         applicationInfo.setTabletEnabled(tabletEnabled);
 
         return applicationInfo;
     }
     
     /**
      * To delete the selected projects or applications
      * @return
      * @throws PhrescoException 
      */
     public String delete() {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method  Applications.delete()");
     	}
     	
     	try {
 	    	ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
 	    	projectManager.delete(getSelectedAppInfos());
 	    	addActionMessage(getText(ACT_SUCC_PROJECT_DELETE));
     	} catch (PhrescoException e) {
     		if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Projects.delete()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_PROJECT_DELETE));
 		}
     	return list();
     }
    
     /**
      * To validate the form fields
      * @return
      */
     public String validateForm() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.validateForm()");
         }
 
         boolean hasError = false;
         //empty validation for name
         if (StringUtils.isEmpty(getProjectName())) {
             setProjectNameError(getText(ERROR_NAME));
             hasError = true;
         }
         //empty validation for projectCode
         if (StringUtils.isEmpty(getProjectCode())) {
             setProjectCodeError(getText(ERROR_CODE));
             hasError = true;
         }
       //empty validation for projectVersion
         if (StringUtils.isEmpty(getProjectVersion())) {
             setProjectVersionError(getText(ERROR_CODE));
             hasError = true;
         }
         //validate if none of the layer is selected
         if (CollectionUtils.isEmpty(getLayer())) {
             setAppTechError(getText(ERROR_TECHNOLOGY));
             setWebTechError(getText(ERROR_TECHNOLOGY));
             setMobTechError(getText(ERROR_TECHNOLOGY));
             hasError = true;
         }
         //empty validation for technology in the selected layer
         if (CollectionUtils.isNotEmpty(getLayer())) {
             for (String layerId : getLayer()) {
                 String techId = getReqParameter(layerId + REQ_PARAM_NAME_TECHNOLOGY);
                 if (LAYER_APP_ID.equals(layerId)) {//for application layer
                     if (StringUtils.isEmpty(techId)) {
                         setAppTechError(getText(ERROR_TECHNOLOGY));
                         hasError = true;
                     }
                 }
                 if (LAYER_WEB_ID.equals(layerId)) {//for web layer
                     if (StringUtils.isEmpty(techId)) {
                         setWebTechError(getText(ERROR_TECHNOLOGY));
                         hasError = true;
                     }
                 }
                 if (LAYER_MOB_ID.equals(layerId)) {//for mobile layer
                     String[] techGroupIds = getReqParameterValues(layerId + REQ_PARAM_NAME_TECH_GROUP);
                     if (ArrayUtils.isEmpty(techGroupIds)) {//empty validation for technology group
                         setMobTechError(getText(ERROR_TECHNOLOGY));
                         hasError = true;
                     } else {
                         for (String techGroupId : techGroupIds) {//empty validation for technology in the selected technology group
                             techId = getReqParameter(techGroupId + REQ_PARAM_NAME_TECHNOLOGY);
                             if (StringUtils.isEmpty(techId)) {
                                 setMobTechError(getText(ERROR_LAYER));
                                 hasError = true;
                                 break;
                             }
                         }
                     }
                 }
             }
         }
 
         if (hasError) {
             setErrorFound(true);
         }
 
         return SUCCESS;
     }
 
     public String getProjectName() {
         return projectName;
     }
 
     public void setProjectName(String projectName) {
         this.projectName = projectName;
     }
 
     public String getProjectDesc() {
         return projectDesc;
     }
 
     public String getProjectCode() {
         return projectCode;
     }
 
     public void setProjectCode(String projectCode) {
         this.projectCode = projectCode;
     }
 
     public void setProjectDesc(String projectDesc) {
         this.projectDesc = projectDesc;
     }
 
     public String getProjectVersion() {
         return projectVersion;
     }
 
     public void setProjectVersion(String projectVersion) {
         this.projectVersion = projectVersion;
     }
 
     public List<String> getVersions() {
         return versions;
     }
 
     public void setVersions(List<String> versions) {
         this.versions = versions;
     }
 
     public boolean isErrorFound() {
         return errorFound;
     }
 
     public void setErrorFound(boolean errorFound) {
         this.errorFound = errorFound;
     }
 
     public String getProjectNameError() {
         return projectNameError;
     }
 
     public void setProjectNameError(String projectNameErr) {
         this.projectNameError = projectNameErr;
     }
 
     public String getProjectCodeError() {
         return projectCodeError;
     }
 
     public void setProjectCodeError(String projectCodeErr) {
         this.projectCodeError = projectCodeErr;
     }
 
     public String getProjectVersionError() {
         return projectVersionError;
     }
 
     public void setProjectVersionError(String projectVersionError) {
         this.projectVersionError = projectVersionError;
     }
 
     public List<TechnologyInfo> getWidgets() {
         return widgets;
     }
 
     public void setWidgets(List<TechnologyInfo> widgets) {
         this.widgets = widgets;
     }
 
     public List<String> getLayer() {
         return layer;
     }
 
     public void setLayer(List<String> layer) {
         this.layer = layer;
     }
 
     public String getMobTechError() {
         return mobTechError;
     }
 
     public void setMobTechError(String mobTechError) {
         this.mobTechError = mobTechError;
     }
 
     public String getAppTechError() {
         return appTechError;
     }
 
     public void setAppTechError(String appLayerError) {
         this.appTechError = appLayerError;
     }
 
     public String getWebTechError() {
         return webTechError;
     }
 
     public void setWebTechError(String webTechError) {
         this.webTechError = webTechError;
     }
 
     public String getLayerError() {
         return layerError;
     }
 
     public void setLayerError(String layerError) {
         this.layerError = layerError;
     }
     
     public String getLayerId() {
         return layerId;
     }
 
     public void setLayerId(String layerId) {
         this.layerId = layerId;
     }
 
     public String getTechGroupId() {
         return techGroupId;
     }
 
     public void setTechGroupId(String techGroupId) {
         this.techGroupId = techGroupId;
     }
     
     public String getStatusFlag() {
 		return statusFlag;
 	}
 
 	public void setStatusFlag(String statusFlag) {
 		this.statusFlag = statusFlag;
 	}
 
 	public String getAppDirName() {
 		return appDirName;
 	}
 
 	public void setAppDirName(String appDirName) {
 		this.appDirName = appDirName;
 	}
 
 	public List<ApplicationInfo> getSelectedAppInfos() {
 		return selectedAppInfos;
 	}
 
 	public void setSelectedAppInfos(List<ApplicationInfo> selectedAppInfos) {
 		this.selectedAppInfos = selectedAppInfos;
 	}
 	
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 }
