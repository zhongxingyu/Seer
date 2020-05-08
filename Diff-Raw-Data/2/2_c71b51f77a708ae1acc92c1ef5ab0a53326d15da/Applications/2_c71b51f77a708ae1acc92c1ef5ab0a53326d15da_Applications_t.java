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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 import com.google.gson.reflect.TypeToken;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileExistsException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.eclipse.jgit.api.errors.InvalidRemoteException;
 import org.eclipse.jgit.api.errors.TransportException;
 import org.tmatesoft.svn.core.SVNAuthenticationException;
 import org.tmatesoft.svn.core.SVNException;
 
 import com.google.gson.Gson;
 import com.opensymphony.xwork2.Action;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.CoreOption;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.SelectedFeature;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.TechnologyOptions;
 import com.photon.phresco.commons.model.WebService;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.framework.api.Project;
 import com.photon.phresco.framework.api.ProjectAdministrator;
 import com.photon.phresco.framework.api.ProjectManager;
 import com.photon.phresco.framework.api.ValidationResult;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.commons.LogErrorReport;
 import com.photon.phresco.framework.impl.SCMManagerImpl;
 import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.model.Scm;
 import com.phresco.pom.util.PomProcessor;
 
 public class Applications extends FrameworkBaseAction {
 
     private static final long serialVersionUID = -4282767788002019870L;
 
     private static final Logger S_LOGGER = Logger.getLogger(Applications.class);
     private static Boolean s_debugEnabled = S_LOGGER.isDebugEnabled();
 
     private String fromPage = "";
 
     private String globalValidationStatus = "";
     private List<String> pilotModules = null;
     private List<String> pilotJSLibs = null;
     private String showSettings = "";
     private List<String> settingsEnv = null;
     private String selectedVersions = "";
     private String selectedAttrType = "";
     private String selectedParamName = "";
     private String divTobeUpdated = "";
     private List<String> techVersions = null;
     private List<DownloadInfo> downloadInfos = null;
     private boolean hasConfiguration = false;
     private String configServerNames = "";
     private String configDbNames = "";
     List<String> deletableDbs = new ArrayList<String>();
     private String fromTab = "";
     private List<String> feature= null;
     private List<String> component= null;
     private List<String> javascript= null;
     private String oldAppDirName = "";
 
     private String userName = "";
     private String password = "";
     private String revision = "";
     private String revisionVal = "";
     private boolean svnImport = false;
     private String svnImportMsg = "";
 
     private String fileType = "";
     private String fileorfolder = null;
     //svn info
     private String credential = "";
     // import from git
     private String repoType = "";
     private String repoUrl = "";
 
     private String applicationType = "";
 
     boolean hasError = false;
     private String envError = "";
 
     private List<DownloadInfo> servers = null;
 
     private String projectCode = "";
     private String technology = "";
     
     private List<String> versions = null;
     private String techId = "";
     private String type = "";
     private String applicationId = "";
     public String errorString;
     public boolean errorFlag;
 
     private SelectedFeature selectFeature;
     private String selectedDownloadInfo = "";
     private String downloadInfoType = "";
     private String selectedDownloadInfoVersion = "";
     private String selectBoxId = "";
     private String defaultOptTxt = "";
     private String action = "";
     private List<String> jsonData = null;
     private String commitMessage = "";
     
     public String loadMenu() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.loadMenu()");
         }
 
         try {
         	ApplicationInfo applicationInfo = getApplicationInfo();
 			String techId = applicationInfo.getTechInfo().getId();
 			Technology technology = getServiceManager().getArcheType(techId, getCustomerId());
 			List<String> optionIds = technology.getOptions();
 			
 			setSessionAttribute(REQ_OPTION_ID, optionIds);
             setReqAttribute(REQ_CURRENT_APP_NAME, getApplicationInfo().getName());
             setReqAttribute(REQ_PROJECT_ID, getProjectId());
             setReqAttribute(REQ_APP_ID, getAppId());
         } catch(PhrescoException e) {
         	return showErrorPopup(e, EXCEPTION_LOADMENU);
         }
 
         return APP_MENU;
     }
     
     /**
      * To get the servers for the given customerid and techId 
      * @return
      */
     /*public String fetchServers() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.getServers");
         }
 
         try {
             setReqAttribute(REQ_CUSTOMER_ID, getCustomerId());
             setServers(getServiceManager().getServers(getCustomerId(), getTechnology()));
         } catch (PhrescoException e) {
             // TODO Auto-generated catch block
         }
 
         return SUCCESS;
     }*/
     
     public String editApplication() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.editApplication()");
         }
         
         removeSessionAttribute(getAppId() + SESSION_APPINFO);
         
         return appInfo();
     }
 
     public String appInfo() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.appInfo()");
         }
 
         try {
         	ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
         	ProjectInfo projectInfo = null;
         	if (getSessionAttribute(getAppId() + SESSION_APPINFO) == null) {
         		projectInfo = projectManager.getProject(getProjectId(), getCustomerId(), getAppId());
         		String technologyId = projectInfo.getAppInfos().get(0).getTechInfo().getId();
         		List<ApplicationInfo> pilotProjects = getServiceManager().getPilotProjects(getCustomerId(), technologyId);
         		Technology technologyInfo = getServiceManager().getTechnology(technologyId);
        		setSessionAttribute(REQ_TECHNOLOGY, technologyInfo);
         		setSessionAttribute(REQ_PILOT_PROJECTS, pilotProjects);
         		setSessionAttribute(getAppId() + SESSION_APPINFO, projectInfo);
         		setReqAttribute(REQ_OLD_APPDIR, projectInfo.getAppInfos().get(0).getName());
         	} else {
         		projectInfo = (ProjectInfo)getSessionAttribute(getAppId() + SESSION_APPINFO);
             	ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
             
             	List<String> jsonData = getJsonData();
             	List<String> selectedFeatures = new ArrayList<String>();
             	List<String> selectedJsLibs = new ArrayList<String>();
             	List<String> selectedComponents = new ArrayList<String>();
             	if (CollectionUtils.isNotEmpty(jsonData)) {
                 	for (String string : jsonData) {
     					Gson gson = new Gson();
     					SelectedFeature obj = gson.fromJson(string, SelectedFeature.class);
     					if (obj.getType().equals(ArtifactGroup.Type.FEATURE.name())) {
     						selectedFeatures.add(obj.getVersionID());
     					}
     					if (obj.getType().equals(ArtifactGroup.Type.JAVASCRIPT.name())) {
     						selectedJsLibs.add(obj.getVersionID());
     					}
     					if (obj.getType().equals(ArtifactGroup.Type.COMPONENT.name())) {
     						selectedComponents.add(obj.getVersionID());
     					}
     				}
             	}
             	appInfo.setSelectedModules(selectedFeatures);
             	appInfo.setSelectedJSLibs(selectedJsLibs);
             	appInfo.setSelectedComponents(selectedComponents);
             	
         		projectInfo.setAppInfos(Collections.singletonList(appInfo));
         		setSessionAttribute(getAppId() + SESSION_APPINFO, projectInfo);
         		setReqAttribute(REQ_OLD_APPDIR, getOldAppDirName());
         	}
             List<WebService> webServices = getServiceManager().getWebServices();
             setReqAttribute(REQ_WEBSERVICES, webServices);
             setReqAttribute(REQ_APP_ID, getAppId());
         } catch (PhrescoException e) {
         	return showErrorPopup(e, EXCEPTION_APPLICATION_EDIT);
         }
 
         return APP_APPINFO;
     }
     
    /* public String appInfo() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.appInfo()");
         }
 
         try {
             setReqAttribute(REQ_FROM_PAGE, fromPage);
             FrameworkUtil.setAppInfoDependents(getHttpRequest(), getCustomerId());
             setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             ApplicationInfo appInfo = null;
             if (FEATURES.equals(fromTab)) {
                 appInfo = (ApplicationInfo) getSessionAttribute(projectCode);
             }
             if (StringUtils.isNotEmpty(fromPage) && appInfo == null) {
                 appInfo = administrator.getProject(projectCode).getApplicationInfo();
                 setSessionAttribute(projectCode, appInfo);
             }
 
             setReqAttribute(REQ_TEMP_SELECTED_PILOT_PROJ, getHttpRequest().getParameter(REQ_SELECTED_PILOT_PROJ));
             String[] modules = getHttpRequest().getParameterValues(REQ_SELECTEDMODULES);
             if (!ArrayUtils.isEmpty(modules)) {
                 Map<String, String> mapModules = ApplicationsUtil.getIdAndVersionAsMap(getHttpRequest(), modules);
                 setReqAttribute(REQ_TEMP_SELECTEDMODULES, mapModules);
             }
 
             String[] jsLibs = getHttpRequest().getParameterValues(REQ_SELECTED_JSLIBS);
             if (!ArrayUtils.isEmpty(jsLibs)) {
                 Map<String, String> mapJsLib = ApplicationsUtil
                 .getIdAndVersionAsMap(getHttpRequest(), jsLibs);
                 setReqAttribute(REQ_TEMP_SELECTED_JSLIBS, mapJsLib);
             }
             setReqAttribute(REQ_CONFIG_SERVER_NAMES, configServerNames);
             setReqAttribute(REQ_CONFIG_DB_NAMES, configDbNames);
         } catch (PhrescoException e) {
             return showErrorPopup(e, REQ_TITLE_ADD_APPLICATION);
         }
 
         return APP_APPINFO;
     }*/
 
 	/**
      * To get the selected server's/database version
      * @return
      */
     public String fetchDownloadInfos() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.fetchDownloadInfos()");
         }
 
         try {
             String type = getReqParameter(REQ_TYPE);
             String techId = getReqParameter(REQ_PARAM_NAME_TECH__ID);
             String selectedDb = getReqParameter(REQ_SELECTED_DOWNLOADINFO);
             String selectedDbVer = getReqParameter(REQ_SELECTED_DOWNLOADINFO_VERSION);
             String selectBoxId = getReqParameter(REQ_CURRENT_SELECTBOX_ID);
             String defaultOptTxt = getReqParameter(REQ_DEFAULT_OPTION);
             setDefaultOptTxt(defaultOptTxt);
             setDownloadInfoType(type);
             setSelectedDownloadInfo(selectedDb);
             setSelectedDownloadInfoVersion(selectedDbVer);
             setSelectBoxId(selectBoxId);
             List<DownloadInfo> downloadInfos = getServiceManager().getDownloads(getCustomerId(), techId, type);
 			setDownloadInfos(downloadInfos);
         } catch (PhrescoException e) {
             return showErrorPopup(e, getText(EXCEPTION_DOWNLOADINFOS));
         }
 
         return SUCCESS;
     }
     
    /* public String applicationType() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.applicationType()");
         }
 
         try {
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             Project project = null;
             if (StringUtils.isNotEmpty(projectCode) && FROM_PAGE_EDIT.equals(fromPage)) {
                 project = administrator.getProject(projectCode);
                 ApplicationInfo appInfo = project.getApplicationInfo();
                 setReqAttribute(REQ_APPINFO, appInfo);
                 setApplicationType(appInfo.getTechInfo().getAppTypeId());
             }
             List<Technology> technologies = administrator.getAppTypeTechnologies(getCustomerId(), getApplicationType());
             setReqAttribute(REQ_APPTYPE_TECHNOLOGIES, technologies);
             setReqAttribute(REQ_SELECTED_JSLIBS, getHttpRequest().getParameter(REQ_SELECTED_JSLIBS));
             setReqAttribute(REQ_FROM_PAGE, fromPage);
         } catch (PhrescoException e) {
             return showErrorPopup(e, "Getting Application Type");
         }
 
         return APP_TYPE;
     }*/
 
 	/*public String technology() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.technology()");
         }
 
         try {
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             Project project = null;
             if (StringUtils.isNotEmpty(projectCode)) {
                 project = administrator.getProject(projectCode);
             }
             if (project != null) {
                 ApplicationInfo appInfo = project.getApplicationInfo();
                 S_LOGGER.debug("project info value"	+ appInfo.toString());
                 setReqAttribute(REQ_APPINFO, appInfo);
             }
             //To get the servers
             List<DownloadInfo> servers = administrator.getServerDownloadInfos(getCustomerId(), getTechnology());
             setReqAttribute(REQ_SERVERS, servers);
 
             //To get the databases
             List<DownloadInfo> databases = administrator.getDbDownloadInfos(getCustomerId(), getTechnology());
             setReqAttribute(REQ_DATABASE, databases);
 
             //To get the webservices
             List<WebService> webservices = administrator.getWebservices();
             setReqAttribute(REQ_WEBSERVICES, webservices);
 
             //To get the pilot projects
             List<ApplicationInfo> pilotProjects = administrator.getPilotProjects(getCustomerId(), getTechnology());
             setReqAttribute(REQ_PILOT_PROJECT_INFO, pilotProjects);
 
             setReqAttribute(REQ_FROM_PAGE, fromPage);
             setReqAttribute(REQ_APPTYPE, getApplicationType());
             setReqAttribute(REQ_SELECTED_TECHNOLOGY, getTechnology());
         } catch (PhrescoException e) {
             return showErrorPopup(e, EXCEPTION_TECHNOLOGY);
         }
 
         return APP_TECHNOLOGY;
     }
 
     public String techVersions() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.techVersions()");
         }
 
         try {
             String appType = getHttpRequest().getParameter(REQ_APPLICATION_TYPE);
             String selectedTechnology = getHttpRequest().getParameter(REQ_TECHNOLOGY);
             ApplicationType applicationType = ApplicationsUtil.getApplicationType(getHttpRequest(), appType);
             //TODO:Need to handle
             //			Technology techonology = applicationType.getTechonology(selectedTechnology);
             //			techVersions = techonology.getVersions();
         } catch(PhrescoException e) {
             return showErrorPopup(e, "Getting technology versions");
         }
 
         return SUCCESS;
     }
 */
    /* public String previous() throws PhrescoException {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entered previous()");
         }
 
         try {
             setReqAttribute(REQ_PROJECT_CODE, projectCode);
             ProjectInfo projectInfo = null;
             if (projectCode != null) {
                 projectInfo = (ProjectInfo) getHttpSession().getAttribute(
                         projectCode);
 
                 setReqAttribute(REQ_TEMP_SELECTED_PILOT_PROJ, getHttpRequest().getParameter(REQ_SELECTED_PILOT_PROJ));
                 String[] modules = getHttpRequest().getParameterValues(REQ_SELECTEDMODULES);
                 if (!ArrayUtils.isEmpty(modules)) {
                     Map<String, String> mapModules = ApplicationsUtil
                     .getIdAndVersionAsMap(getHttpRequest(), modules);
                     getHttpRequest().setAttribute(REQ_TEMP_SELECTEDMODULES,
                             mapModules);
                 }
 
                 String[] jsLibs = getHttpRequest().getParameterValues(REQ_SELECTED_JSLIBS);
                 if (!ArrayUtils.isEmpty(jsLibs)) {
                     Map<String, String> mapJsLib = ApplicationsUtil.getIdAndVersionAsMap(getHttpRequest(), jsLibs);
                     setReqAttribute(REQ_TEMP_SELECTED_JSLIBS, mapJsLib);
                 }
             }
             setSessionAttribute(projectCode, projectInfo);
             HttpServletRequest request = getHttpRequest();
             FrameworkUtil.setAppInfoDependents(request, getCustomerId());
             setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
             setReqAttribute(REQ_CONFIG_SERVER_NAMES, configServerNames);
             setReqAttribute(REQ_CONFIG_DB_NAMES, configDbNames);
         } catch (PhrescoException e) {
             return showErrorPopup(e, "AppInfo");
         }
 
         return APP_APPINFO;
     }*/
 
    /* public String save() throws PhrescoException {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Applications.save()");
         }
 
         try {
             ApplicationInfo appInfo = (ApplicationInfo) getHttpSession().getAttribute(projectCode);
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             setFeatures(administrator, appInfo);
             administrator.createProject(appInfo, null);
             addActionMessage(getText(SUCCESS_PROJECT, Collections.singletonList(appInfo.getName())));
             removeSessionAttribute(projectCode);
             setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
         } catch (PhrescoException e) {
             return showErrorPopup(e, "Save Application");
         }
 
         return discover();
     }*/
 
     /*public String update() throws PhrescoException {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.update()");
         }
 
         BufferedReader reader = null;
         try {
             ApplicationInfo appInfo = (ApplicationInfo) getHttpSession().getAttribute(projectCode);
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             setFeatures(administrator, appInfo);
             //TODO:Need to handle
             //			ApplicationInfo originalinfo = appInfo.clone();
             //			File projectPath = new File(Utility.getProjectHome(), appInfo.getCode() + File.separator 
             //			                    + FOLDER_DOT_PHRESCO + File.separator + PROJECT_INFO);
             try {
 				reader = new BufferedReader(new FileReader(projectPath));
 			} catch (FileNotFoundException e) {
 				throw new PhrescoException(e);
 			}
             //TODO:Need to handle
             //			List<ModuleGroup> modules = appInfo.getTechnology().getModules();
             //			List<ModuleGroup> jsLibraries = appInfo.getTechnology().getJsLibraries();
             //			if (CollectionUtils.isNotEmpty(modules)) {
             //				appInfo.getTechnology().setModules(null);
             //			}
             //		
             //			if (CollectionUtils.isNotEmpty(jsLibraries)) {
             //				appInfo.getTechnology().setJsLibraries(null);
             //			}
             try {
                 ApplicationInfo tempAppInfo = administrator.getProject(projectCode).getApplicationInfo();
                 List<String> newDatabases = appInfo.getSelectedDatabases();
                 List<String> newDbNames = new ArrayList<String>();
                 if (CollectionUtils.isNotEmpty(newDatabases)) {
                     //TODO:Need to handle
                     //					for (Database newDatabase : newDatabases) {
                     //						newDbNames.add(newDatabase.getName());
                     //					}
                 }
 
                 List<String> projectInfoDbs = tempAppInfo.getSelectedDatabases();
                 List<String> projectInfoDbNames = new ArrayList<String>();
                 if (CollectionUtils.isNotEmpty(projectInfoDbs)) {
                     //TODO:Need to handle
                     //					for (Database projectInfoDb : projectInfoDbs) {
                     //						projectInfoDbNames.add(projectInfoDb.getName());
                     //					}
                 }
 
                 if (CollectionUtils.isNotEmpty(projectInfoDbNames) && projectInfoDbNames != null) {
                     for (String projectInfoDbName : projectInfoDbNames) {
                         if (!newDbNames.contains(projectInfoDbName)) {
                             deletableDbs.add(projectInfoDbName);
                         } else {
                             for (String newDatabase : newDatabases) {
                                 for (String projectInfoDb : projectInfoDbs) {
                                     //TODO:Need to handle
                                     //									if (newDatabase.getName().equals(projectInfoDb.getName())) {
                                     //										List<String> newDbVersions = newDatabase.getVersions();
                                     //										List<String> projectInfoDbVersions = projectInfoDb.getVersions();
                                     //										compareVersions(projectInfoDb.getName(), projectInfoDbVersions, newDbVersions);
                                     //									}
                                 }
                             }
                         }
                     }
                 }
 
                 administrator.deleteSqlFolder(deletableDbs, appInfo);
                 UserInfo userInfo = (UserInfo) getHttpSession().getAttribute(SESSION_USER_INFO);
                 //TODO:Need to handle
                 //				administrator.updateProject(appInfo, originalinfo, projectPath,userInfo);
                 removeConfiguration();
                 addActionMessage(getText(UPDATE_PROJECT,Collections.singletonList(appInfo.getName())));
             } catch (Exception e) {
 
             }
         } catch (PhrescoException e) {
             S_LOGGER.error("Entered into catch block of  Applications.update()" + FrameworkUtil.getStackTraceAsString(e));
             new LogErrorReport(e, "Update Project");
         } finally {
             try {
                 reader.close();
             } catch (IOException e) {
                 throw new PhrescoException(e);
             }
         }
         removeSessionAttribute(projectCode);
         setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 
         return discover();
     }*/
 
    /* private void compareVersions(String dbName, List<String> projectInfoDbVersions, List<String> newDbVersions) {
         for (String projectInfoDbVersion : projectInfoDbVersions) {
             if (newDbVersions.contains(projectInfoDbVersion)) {
 
             }
             else {
                 deletableDbs.add(dbName + "/" + projectInfoDbVersion.trim());
             }
         }
     }*/
 
   /*  private void setFeatures(ProjectAdministrator administrator,
             ApplicationInfo appInfo) throws PhrescoException {
         S_LOGGER.debug("Entering Method  Applications.setFeatures()");
 
         String module = getHttpRequest().getParameter(REQ_SELECTEDMODULES);
         String[] modules = module.split(",");
 
         //TODO:Need to handle
         //		ApplicationType applicationType = administrator.getApplicationType(appInfo.getTechInfo().getAppTypeId());
         //		if (S_LOGGER.isDebugEnabled()) {
         //			S_LOGGER.debug("Application Type object value "
         //					+ applicationType.toString());
         //		}
 
         if (!ArrayUtils.isEmpty(modules)) {
             //TODO:Need to handle
             //			List<ModuleGroup> allModules = applicationType.getTechonology(
             //					appInfo.getTechnology().getId()).getModules();
             //			List<ModuleGroup> selectedModules = ApplicationsUtil.getSelectedTuples(getHttpRequest(), allModules, modules);
             //			appInfo.getTechnology().setModules(selectedModules);
         }
 
         String jsLib = getHttpRequest().getParameter(REQ_SELECTED_JSLIBS);
         String[] jsLibs = jsLib.split(",");
         if (!ArrayUtils.isEmpty(jsLibs)) {
             //TODO:Need to handle
             //			List<ModuleGroup> allModules = applicationType.getTechonology(
             //					appInfo.getTechnology().getId()).getJsLibraries();
             //			List<ModuleGroup> selectedModules = ApplicationsUtil
             //			.getSelectedTuples(getHttpRequest(), allModules, jsLibs);
             //			appInfo.getTechnology().setJsLibraries(selectedModules);
         }
     }*/
 
    /* public String edit() {
         S_LOGGER.debug("Entering Method  Applications.edit()");
 
         try {
             FrameworkConfiguration configuration = null;
             configuration = PhrescoFrameworkFactory.getFrameworkConfig();
             Client client = ClientHelper.createClient();
             WebResource resource = client.resource(configuration
                     .getServerPath() + FrameworkConstants.REST_APPS_PATH);
             Builder builder = resource.accept(MediaType.APPLICATION_JSON_TYPE);
             GenericType<List<ApplicationType>> genericType = new GenericType<List<ApplicationType>>() {
             };
             List<ApplicationType> applicationTypes = builder.get(genericType);
             if (S_LOGGER.isDebugEnabled()) {
                 S_LOGGER.debug("Application Types received from rest service");
                 if (applicationTypes != null) {
                     for (ApplicationType applicationType : applicationTypes) {
                         S_LOGGER.debug("Application Type value"
                                 + applicationType.toString());
                     }
                 }
             }
             getHttpRequest().setAttribute(REQ_FROM_PAGE, FROM_PAGE_EDIT);
             getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
         } catch (PhrescoException e) {
             return showErrorPopup(e, "Edit Application");
         }
 
         return APP_APPLICATION;
     }*/
     
     
     private List<ArtifactGroup> getRemovedModules(ApplicationInfo appInfo, List<String> jsonData) throws PhrescoException {
     	List<String> selectedFeaturesId = appInfo.getSelectedModules();
     	List<String> selectedJSLibsId = appInfo.getSelectedJSLibs();
     	List<String> selectedComponentsId = appInfo.getSelectedComponents();
     	Gson gson = new Gson();
     	List<String> newlySelectedModuleGrpIds = new ArrayList<String>();
     	if (CollectionUtils.isNotEmpty(jsonData)) {
     		for (String string : jsonData) {
     			SelectedFeature obj = gson.fromJson(string, SelectedFeature.class);
     			newlySelectedModuleGrpIds.add(obj.getModuleId());
     		}
     	}
     	List<ArtifactGroup> artifactGroups = new ArrayList<ArtifactGroup>();
     	if(CollectionUtils.isNotEmpty(selectedFeaturesId)) {
     		addArtifactGroups(selectedFeaturesId, gson, newlySelectedModuleGrpIds, artifactGroups);
     	}
     	if(CollectionUtils.isNotEmpty(selectedJSLibsId)) {
     		addArtifactGroups(selectedJSLibsId, gson, newlySelectedModuleGrpIds, artifactGroups);
     	}
     	if(CollectionUtils.isNotEmpty(selectedComponentsId)) {
     		addArtifactGroups(selectedComponentsId, gson, newlySelectedModuleGrpIds, artifactGroups);
     	}
     	return artifactGroups;
     }
 
     private void addArtifactGroups(List<String> selectedFeaturesIds, Gson gson,
     		List<String> newlySelectedModuleGrpIds,
     		List<ArtifactGroup> artifactGroups) throws PhrescoException {
     	for (String selectedfeatures : selectedFeaturesIds) {
     		ArtifactInfo artifactInfo = getServiceManager().getArtifactInfo(selectedfeatures);
     		//SelectedFeature selectedFeature = gson.fromJson(selectedfeatures, SelectedFeature.class);
     		if (!newlySelectedModuleGrpIds.contains(artifactInfo.getArtifactGroupId())) {
     			ArtifactGroup artifactGroupInfo = getServiceManager().getArtifactGroupInfo(artifactInfo.getArtifactGroupId());
     			artifactGroups.add(artifactGroupInfo);
     		}
     	}
     }
     
     public String update() {
     	BufferedReader reader = null;
     	try {
     		ProjectInfo projectInfo = (ProjectInfo)getSessionAttribute(getAppId() + SESSION_APPINFO);
         	ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
         	
         	List<String> jsonData = getJsonData();
         	List<String> selectedFeatures = new ArrayList<String>();
         	List<String> selectedJsLibs = new ArrayList<String>();
         	List<String> selectedComponents = new ArrayList<String>();
         	List<ArtifactGroup> listArtifactGroup = new ArrayList<ArtifactGroup>();
         	List<DownloadInfo> selectedServerGroup = new ArrayList<DownloadInfo>();
         	List<DownloadInfo> selectedDatabaseGroup = new ArrayList<DownloadInfo>();
         	if (CollectionUtils.isNotEmpty(jsonData)) {
             	for (String string : jsonData) {
 					Gson gson = new Gson();
 					SelectedFeature obj = gson.fromJson(string, SelectedFeature.class);
 					String artifactGroupId = obj.getModuleId();
 					ArtifactGroup artifactGroup = getServiceManager().getArtifactGroupInfo(artifactGroupId);
 					List<CoreOption> appliesTo = artifactGroup.getAppliesTo();
 					for (CoreOption coreOption : appliesTo) {
 						if (coreOption.getTechId().equals(appInfo.getTechInfo().getId())) {
 							artifactGroup.setAppliesTo(Collections.singletonList(coreOption));
 							listArtifactGroup.add(artifactGroup);
 							break;
 						}
 					}
 					if (obj.getType().equals(ArtifactGroup.Type.FEATURE.name())) {
 						selectedFeatures.add(obj.getVersionID());
 					}
 					if (obj.getType().equals(ArtifactGroup.Type.JAVASCRIPT.name())) {
 						selectedJsLibs.add(obj.getVersionID());
 					}
 					if (obj.getType().equals(ArtifactGroup.Type.COMPONENT.name())) {
 						selectedComponents.add(obj.getVersionID());
 					}
 				}
         	}
         	Gson gson = new Gson();
         	
         	StringBuilder sb = new StringBuilder(Utility.getProjectHome())
         	.append(getOldAppDirName())
         	.append(File.separator)
         	.append(Constants.DOT_PHRESCO_FOLDER)
         	.append(File.separator)
         	.append(Constants.APPLICATION_HANDLER_INFO_FILE);
 			File filePath = new File(sb.toString());
 			MojoProcessor mojo = new MojoProcessor(filePath);
 			ApplicationHandler applicationHandler = mojo.getApplicationHandler();
 			
 			//To write selected Features into phresco-application-Handler-info.xml
 			String artifactGroup = gson.toJson(listArtifactGroup);
 			applicationHandler.setSelectedFeatures(artifactGroup);
 
 			//To write Deleted Features into phresco-application-Handler-info.xml
 			List<ArtifactGroup> removedModules = getRemovedModules(appInfo, jsonData);
 			Type jsonType = new TypeToken<Collection<ArtifactGroup>>(){}.getType();
 			String deletedFeatures = gson.toJson(removedModules, jsonType);
 			applicationHandler.setDeletedFeatures(deletedFeatures);
         	
 			//To write selected Database into phresco-application-Handler-info.xml
 			List<ArtifactGroupInfo> selectedDatabases = appInfo.getSelectedDatabases();
 			if (CollectionUtils.isNotEmpty(selectedDatabases)) {
 				for (ArtifactGroupInfo selectedDatabase : selectedDatabases) {
 					DownloadInfo downloadInfo = getServiceManager().getDownloadInfo(
 							selectedDatabase.getArtifactGroupId());
 					selectedDatabaseGroup.add(downloadInfo);
 					if (CollectionUtils.isNotEmpty(selectedDatabaseGroup)) {
 						String databaseGroup = gson.toJson(selectedDatabaseGroup);
 						applicationHandler.setSelectedDatabase(databaseGroup);
 					}
 				}
 			}
 			
 			//To write selected Servers into phresco-application-Handler-info.xml
 			List<ArtifactGroupInfo> selectedServers = appInfo.getSelectedServers();
 			if (CollectionUtils.isNotEmpty(selectedServers)) {
 				for (ArtifactGroupInfo selectedservers : selectedServers) {
 					DownloadInfo downloadInfo = getServiceManager().getDownloadInfo(
 							selectedservers.getArtifactGroupId());
 					selectedServerGroup.add(downloadInfo);
 					if (CollectionUtils.isNotEmpty(selectedServerGroup)) {
 						String serverGroup = gson.toJson(selectedServerGroup);
 						applicationHandler.setSelectedServer(serverGroup);
 					}
 				}
 			}
 			
 			//To write selected WebServices info to phresco-plugin-info.xml
 			List<String> selectedWebservices = appInfo.getSelectedWebservices();
 			if (CollectionUtils.isNotEmpty(selectedWebservices)) {
 				for (String selectedWebService : selectedWebservices) {
 					WebService webservice = getServiceManager().getWebService(selectedWebService);
 					if (webservice != null) {
 						String serverGroup = gson.toJson(webservice);
 						applicationHandler.setSelectedWebService(serverGroup);
 					}
 				}
 			}
 
         	mojo.save();
         	appInfo.setSelectedModules(selectedFeatures);
         	appInfo.setSelectedJSLibs(selectedJsLibs);
         	appInfo.setSelectedComponents(selectedComponents);
 
 			List<ArtifactGroupInfo> oldSelectedDbs = getApplicationInfo().getSelectedDatabases();
 			if (CollectionUtils.isNotEmpty(oldSelectedDbs)) {
 			    List<String> deletableDbs = new ArrayList<String>();
 				for (ArtifactGroupInfo artifactGroupInfo : oldSelectedDbs) {
 					String oldArtifactGroupId = artifactGroupInfo.getArtifactGroupId();
 					if (selectedDatabases.contains(oldArtifactGroupId)) {
 						checkForVersions(oldSelectedDbs, selectedDatabases, oldArtifactGroupId, deletableDbs, appInfo);
 					} else {
 						DownloadInfo downloadInfo = getServiceManager().getDownloadInfo(oldArtifactGroupId);
 						deletableDbs.add(downloadInfo.getName());
 					}
 				}
 				deleteSqlFolder(deletableDbs, appInfo);
             }
             
     		projectInfo.setAppInfos(Collections.singletonList(appInfo));
     		ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
     		projectManager.update(projectInfo, getServiceManager(), getOldAppDirName());
             List<ProjectInfo> projects = projectManager.discover(getCustomerId());
             setReqAttribute(REQ_PROJECTS, projects);
             removeSessionAttribute(getAppId() + SESSION_APPINFO);
             removeSessionAttribute(REQ_SELECTED_FEATURES);
             removeSessionAttribute(REQ_PILOT_PROJECTS);
 		} catch (PhrescoException e) {
 			return showErrorPopup(e, EXCEPTION_PROJECT_UPDATE);
 		} finally {
 			 Utility.closeStream(reader);
 		 }
         
     	return APP_UPDATE;
     }
 
 	private void checkForVersions(List<ArtifactGroupInfo> oldSelectedDbs, List<ArtifactGroupInfo> selectedDatabases,
 			String oldArtifactGroupId, List<String> deletableDbs, ApplicationInfo appInfo) throws PhrescoException {
 		try {
 			for (ArtifactGroupInfo artifactGroupInfo : selectedDatabases) {
 				List<String> artifactInfoIds = artifactGroupInfo.getArtifactInfoIds();
 				for (ArtifactGroupInfo oldartifactGroupInfo : oldSelectedDbs) {
 					List<String> oldArtifactInfoIds = oldartifactGroupInfo.getArtifactInfoIds();
 					for (String olddbId : oldArtifactInfoIds) {
 						if (!artifactInfoIds.contains(olddbId)) {
 							DownloadInfo downloadInfo = getServiceManager().getDownloadInfo(oldArtifactGroupId);
 							List<ArtifactInfo> versions = downloadInfo.getArtifactGroup().getVersions();
 							for (ArtifactInfo artifactInfo : versions) {
 								String deleteVersion = "/" + downloadInfo.getName() + "/" + artifactInfo.getVersion();
 								deletableDbs.add(deleteVersion);
 							}
 						}
 					}
 				}
 			}
 		} catch (PhrescoException e) {
 			 throw new PhrescoException(e);
 		}
 	}
 
     public void deleteSqlFolder(List<String> dbList , ApplicationInfo appInfo) throws PhrescoException {
 		 try {
 			 FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			 File sqlPath = new File(Utility.getProjectHome() + File.separator
 					 + appInfo.getAppDirName() + frameworkUtil.getSqlFilePath(appInfo));
 			 if (CollectionUtils.isNotEmpty(dbList)) {
 				 for (String dbVersion : dbList) {
 					 File dbVersionFolder = new File(sqlPath, dbVersion.toLowerCase());
 					 FileUtils.deleteDirectory(dbVersionFolder);
 				 }
 			 }
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 	/*public String delete() {
         S_LOGGER.debug("Entering Method  Applications.delete()");
 
         try {
             HttpServletRequest request = (HttpServletRequest) ActionContext
             .getContext().get(ServletActionContext.HTTP_REQUEST);
             String selectedProjects[] = request
             .getParameterValues(REQ_SELECTEDPROJECTS);
 
             ProjectAdministrator administrator = PhrescoFrameworkFactory
             .getProjectAdministrator();
             List<String> projectCodes = new ArrayList<String>();
             if (selectedProjects != null) {
                 for (String selctedProject : selectedProjects) {
                     projectCodes.add(selctedProject);
                 }
             }
 
             administrator.deleteProject(projectCodes);
             addActionMessage(SUCCESS_PROJECT_DELETE);
         } catch (PhrescoException e) {
             return showErrorPopup(e, "Delete Application");
         }
 
 //        return list();
         return null;
     }
 */
 	public String importSVNApplication() {
 		if(s_debugEnabled){
 			S_LOGGER.debug("Entering Method  Applications.importSVNApplication()");
 		}
 		try {
 			String encodedpassword = (String) getReqAttribute(SESSION_USER_PASSWORD);
 			if (StringUtils.isEmpty(credential)) { //unchecked
 				String decryptedPass = new String(Base64.decodeBase64(encodedpassword));
 				password = decryptedPass;
 			}
 			revision = !HEAD_REVISION.equals(revision) ? revisionVal : revision;
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			boolean importProject = scmi.importProject(SVN, repoUrl, userName, password, null, revision);
 			if (importProject) {
 				errorString = getText(IMPORT_SUCCESS_PROJECT);
 				errorFlag = true;
 			} else {
 				errorString = getText(INVALID_FOLDER);
 				errorFlag = false;
 			}
 		} catch (SVNAuthenticationException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString = getText(INVALID_CREDENTIALS);
 		} catch (SVNException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			if (e.getMessage().indexOf(SVN_FAILED) != -1) {
 				errorString = getText(INVALID_URL);
 			} else if (e.getMessage().indexOf(SVN_INTERNAL) != -1) {
 				errorString = getText(INVALID_REVISION);
 			} else {
 				errorString = getText(IMPORT_PROJECT_FAIL);
 			}
 		} catch (FileExistsException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString = getText(PROJECT_ALREADY);
 		} catch (PhrescoException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString =getText(e.getMessage());
 		} catch (Exception e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorString = getText(IMPORT_PROJECT_FAIL);
 			errorFlag = false;
 		}
 		return SUCCESS;
 	}
 
 	public String importGITApplication() {
 		if(s_debugEnabled){
 			S_LOGGER.debug("Entering Method  Applications.importGITApplication()");
 		}
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		try {
 			boolean importProject = scmi.importProject(GIT, repoUrl, userName, password, MASTER ,revision);
 			if (importProject) {
 				errorString = getText(IMPORT_SUCCESS_PROJECT);
 				errorFlag = true;
 			} else {
 				errorString = getText(INVALID_FOLDER);
 				errorFlag = false;
 			}
 		} catch (SVNAuthenticationException e) {	//Will not occur for GIT
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString = getText(INVALID_CREDENTIALS);
 		} catch (SVNException e) {	//Will not occur for GIT
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			if (e.getMessage().indexOf(SVN_FAILED) != -1) {
 				errorString = getText(INVALID_URL);
 			} else if (e.getMessage().indexOf(SVN_INTERNAL) != -1) {
 				errorString = getText(INVALID_REVISION);
 			} else {
 				errorString = getText(IMPORT_PROJECT_FAIL);
 			}
 		}  catch (FileExistsException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString = getText(PROJECT_ALREADY);
 		}  catch (PhrescoException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString = getText(e.getMessage());
 		} catch (Exception e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorString = getText(IMPORT_PROJECT_FAIL);
 			errorFlag = false;
 		}
 		return SUCCESS;
 	}
 
 	public String importAppln() {
 		setReqAttribute(REQ_ACTION, action);
 		return APP_IMPORT;
 	}
 
 	public String updateProjectPopup() {
 		S_LOGGER.debug("Entering Method  Applications.updateProjectPopup()");
 		try {
 			String connectionUrl = "";
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			String appDirName = applicationInfo.getAppDirName();
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			PomProcessor processor = frameworkUtil.getPomProcessor(appDirName);
 			Scm scm = processor.getSCM();
 			if (scm != null) {
 				connectionUrl = scm.getConnection();
 			}
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_PROJECT_ID, getProjectId());
 			setReqAttribute(REQ_CUSTOMER_ID, getCustomerId());
 			setReqAttribute(REPO_URL, connectionUrl);
 			setReqAttribute(REQ_FROM_TAB, UPDATE);
 			setReqAttribute(REQ_ACTION, action);
 			setReqAttribute(REQ_APP_INFO, applicationInfo);
 		} catch (PhrescoException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			return showErrorPopup(e, "Update Application");
 		}
 
 		return APP_UPDATE;
 	}
 
 	public String updateGitProject() {
 		S_LOGGER.debug("Entering Method  Applications.updateGitProject()");
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		try {
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			String appDirName = applicationInfo.getAppDirName();
 			scmi.updateProject(GIT, repoUrl, userName, password, MASTER , null, appDirName);
 			errorString = getText(SUCCESS_PROJECT_UPDATE);
 			errorFlag = true;
 		} catch (InvalidRemoteException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorString = getText(INVALID_URL);
 			errorFlag = false;
 		} catch (TransportException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorString = getText(INVALID_URL);
 			errorFlag = false;
 		} catch (SVNAuthenticationException e) {	//Will not occur for GIT
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString = getText(INVALID_CREDENTIALS);
 		} catch (SVNException e) {	//Will not occur for GIT
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			if (e.getMessage().indexOf(SVN_FAILED) != -1) {
 				errorString = getText(INVALID_URL);
 			} else if (e.getMessage().indexOf(SVN_INTERNAL) != -1) {
 				errorString = getText(INVALID_REVISION);
 			} else {
 				errorString = getText(IMPORT_PROJECT_FAIL);
 			}
 		} catch (FileExistsException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString = getText(PROJECT_ALREADY);
 		} catch (PhrescoException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString =getText(e.getMessage());
 		} catch (Exception e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorString = getText(FAILURE_PROJECT_UPDATE);
 			errorFlag = false;
 		}
 
 		return SUCCESS;
 	}
 
 	public String updateSVNProject() {
 		S_LOGGER.debug("Entering Method  Applications.updateGitProject()");
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		revision = !HEAD_REVISION.equals(revision) ? revisionVal : revision;
 		try {
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			String appDirName = applicationInfo.getAppDirName();
 			scmi.updateProject(SVN, repoUrl, userName, password, null, revision, appDirName);
 			errorString = getText(SUCCESS_PROJECT_UPDATE);
 			errorFlag = true;
 		} catch (InvalidRemoteException e) {
 			if(s_debugEnabled) {
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorString = getText(INVALID_URL);
 			errorFlag = false;
 
 		} catch (TransportException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorString = getText(INVALID_URL);
 			errorFlag = false;
 		} catch (SVNAuthenticationException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString = getText(INVALID_CREDENTIALS);
 		} catch (SVNException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			if (e.getMessage().indexOf(SVN_FAILED) != -1) {
 				errorString = getText(INVALID_URL);
 			} else if (e.getMessage().indexOf(SVN_INTERNAL) != -1) {
 				errorString = getText(INVALID_REVISION);
 			} else {
 				errorString = getText(IMPORT_PROJECT_FAIL);
 			}
 		} catch (FileExistsException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString = getText(PROJECT_ALREADY);
 		} catch (PhrescoException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString =getText(e.getMessage());
 		} catch (Exception e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorString = getText(IMPORT_PROJECT_FAIL);
 			errorFlag = false;
 		}
 		return SUCCESS;
 	}
 
 	public String addSVNProject() {
 		if(s_debugEnabled){
 			S_LOGGER.debug("Entering Method  Applications.addSVNProject()");
 		}
 		try {
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			String applicationHome = getApplicationHome();
 			File appDir = new File(applicationHome);
 			scmi.importToRepo(SVN, repoUrl, userName, password, null, null, appDir, commitMessage);
 			errorString = getText(ADD_PROJECT_SUCCESS);
 			errorFlag = true;
 		} catch (Exception e) {
 			errorString = e.getLocalizedMessage();
 			errorFlag = false;
 		}
 		return SUCCESS;
 	}
 	
 	public String addGITProject() {
 		if(s_debugEnabled){
 			S_LOGGER.debug("Entering Method  Applications.addGITProject()");
 		}
 		try {
 			// TODO : need to handle
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return SUCCESS;
 	}
 	
 	public String commitSVNProject() {
 		if(s_debugEnabled){
 			S_LOGGER.debug("Entering Method  Applications.commitSVNProject()");
 		}
 		try {
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			String applicationHome = getApplicationHome();
 			File appDir = new File(applicationHome);
 			scmi.commitToRepo(SVN, repoUrl, userName, password,  null, null, appDir, commitMessage);
 			errorString = getText(COMMIT_PROJECT_SUCCESS);
 			errorFlag = true;
 		} catch (Exception e) {
 			errorString = e.getLocalizedMessage();
 			errorFlag = false;
 		}
 		return SUCCESS;
 	}
 	
     //TODO: No need the validator remove all validator
     public String validateFramework() {
         S_LOGGER.debug("Entering Method  Applications.validateFramework()");
 
         try {
             getHttpRequest().setAttribute(VALIDATE_FROM, VALIDATE_FRAMEWORK);
             // From home when clicking applications , it becomes true
             String validateInBg = getHttpRequest().getParameter(VALIDATE_IN_BG);
             if (validateInBg.equals("true") && getHttpSession().getAttribute(SESSION_FRMK_VLDT_RSLT) != null) {
                 setGlobalValidationStatus((String) getHttpSession().getAttribute(SESSION_FRMK_VLDT_STATUS));
                 return Action.SUCCESS;
             } else {
                 getHttpSession().removeAttribute(SESSION_FRMK_VLDT_STATUS);
                 getHttpSession().removeAttribute(SESSION_FRMK_VLDT_RSLT);
                 ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
                 List<ValidationResult> validationResults = administrator.validate();
                 String validationStatus = null;
                 for (ValidationResult validationResult : validationResults) {
                     validationStatus = validationResult.getStatus().toString();
                     if (validationStatus == "ERROR") {
                         getHttpSession().setAttribute(SESSION_FRMK_VLDT_STATUS,	"ERROR");
                     }
                 }
                 getHttpSession().setAttribute(SESSION_FRMK_VLDT_RSLT, validationResults);
                 if (validateInBg.equals("true")) {
                     setGlobalValidationStatus(validationStatus);
                     return Action.SUCCESS;
                 }
             }
         } catch (Exception e) {
             S_LOGGER.error("Entered into catch block of Applications.validateFramework()"
                     + FrameworkUtil.getStackTraceAsString(e));
             new LogErrorReport(e, "Validating framework");
         }
 
         return APP_VALIDATE_FRAMEWORK;
     }
 
     //TODO: No need the validator remove all validator
     public String showFrameworkValidationResult() {
         S_LOGGER.debug("Entering Method  Applications.showFrameworkValidationResult()");
         getHttpRequest().setAttribute(VALIDATE_FROM, VALIDATE_FRAMEWORK);
 
         return APP_SHOW_FRAMEWORK_VLDT_RSLT;
     }
 
     //TODO: No need the validator remove all validator
     public String validateProject() { 
         S_LOGGER.debug("Entering Method  Applications.validateProject()");
 
         try {
             getHttpRequest().setAttribute(VALIDATE_FROM, VALIDATE_PROJECT);
             String validateInBg = getHttpRequest().getParameter(VALIDATE_IN_BG);
             if (getHttpSession().getAttribute(projectCode + SESSION_PRJT_VLDT_RSLT) != null	&& "true".equals(validateInBg)) {
                 setGlobalValidationStatus((String) getHttpSession().getAttribute(projectCode + SESSION_PRJT_VLDT_STATUS));
                 return Action.SUCCESS;
             } else {
                 getHttpSession().removeAttribute(projectCode + SESSION_PRJT_VLDT_RSLT);
                 getHttpSession().removeAttribute(projectCode + SESSION_PRJT_VLDT_STATUS);
                 ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
                 String validationStatus = null;
                 if (!StringUtils.isEmpty(projectCode)) {
                     Project project = administrator.getProject(projectCode);
                     List<ValidationResult> validationResults = administrator.validate(project);
                     for (ValidationResult validationResult : validationResults) {
                         validationStatus = validationResult.getStatus().toString();
                         if (validationStatus == "ERROR") {
                             getHttpSession().setAttribute(projectCode + SESSION_PRJT_VLDT_STATUS, "ERROR");
                         }
                     }
                     getHttpSession().setAttribute(projectCode + SESSION_PRJT_VLDT_RSLT,	validationResults);
                 }
 
                 if (validateInBg.equals("true")) {
                     setGlobalValidationStatus(validationStatus);
                     return Action.SUCCESS;
                 }
             }
         } catch (Exception e) {
             S_LOGGER.error("Entered into catch block of Applications.validateProject()"
                     + FrameworkUtil.getStackTraceAsString(e));
             new LogErrorReport(e, "Validating project");
         }
 
         return APP_VALIDATE_PROJECT;
     }
 
     //TODO: No need the validator remove all validator
     public String showProjectValidationResult() {
         S_LOGGER.debug("Entering Method  Applications.showProjectValidationResult()");
 
         getHttpRequest().setAttribute(REQ_PROJECT_CODE, projectCode);
         getHttpRequest().setAttribute(VALIDATE_FROM, VALIDATE_PROJECT);
 
         return APP_SHOW_PROJECT_VLDT_RSLT;
     }
 
   /*  private String discover() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.discover()");
         }
 
         try {
             ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
             List<ProjectInfo> projects = projectManager.discover("photon");
             setReqAttribute(REQ_PROJECTS, projects);
         } catch (PhrescoException e) {
             S_LOGGER.error("Entered into catch block of Applications.discover()" + FrameworkUtil.getStackTraceAsString(e));
             return showErrorPopup(e, "Discovering projects");
         }
         setReqAttribute(REQ_SELECTED_MENU, APPLICATIONS);
 
         return APP_LIST;
     }
 
     public String showSettings() {
         S_LOGGER.debug("entered Applications.showSettings()");
 
         try {
             if (showSettings != null && Boolean.valueOf(showSettings)) {
                 if (CollectionUtils.isNotEmpty(getEnvironmentNames())) {
                     settingsEnv = getEnvironmentNames();
                 } else {
                     envError = getText(NO_SETTINGS_ENV);
                 }
             }
         } catch (PhrescoException e) {
             return showErrorPopup(e, "Show Settings");
         }
 
         return SUCCESS;
     }
 
     private List<String> getEnvironmentNames() throws PhrescoException {
         List<String> names = new ArrayList<String>(5);
         ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
         List<Environment> environments = administrator.getEnvironments();
         if (CollectionUtils.isNotEmpty(environments)) {
             for (Environment environment : environments) {
                 names.add(environment.getName());
             }
         }
         return names;
     }
 
     public String openAttrPopup() throws PhrescoException {
         try {
             String techId = getHttpRequest().getParameter("techId");
             String appType = getHttpRequest().getParameter(REQ_APPLICATION_TYPE);
             String type = getHttpRequest().getParameter(ATTR_TYPE);
             String from = getHttpRequest().getParameter(REQ_FROM);
             setReqAttribute(REQ_FROM, from);
             ApplicationType applicationType = null;
             applicationType = ApplicationsUtil.getApplicationType(getHttpRequest(), appType);
             //			Technology techonology = applicationType.getTechonology(techId);//TODO:Need to handle
             String attrName = null;
             if (Constants.SETTINGS_TEMPLATE_SERVER.equals(type)) {
                 List<Integer> listSelectedServerIds = null;
                 //				List<DownloadInfo> servers = techonology.getServers();//TODO:Need to handle
                 if(StringUtils.isEmpty(from)) {
                     List<String> listSelectedServers = null;
                     List<String> listSelectedServerNames = null;
                     String selectedServers = getHttpRequest().getParameter("selectedServers");
                     if (StringUtils.isNotEmpty(selectedServers)) {
                         listSelectedServerNames = new ArrayList<String>();
                         listSelectedServers = new ArrayList<String>(Arrays.asList(selectedServers.split("#SEP#")));
                         if (CollectionUtils.isNotEmpty(listSelectedServers)) {
                             for (String listSelectedServer : listSelectedServers) {
                                 String[] split = listSelectedServer.split("#VSEP#");
                                 listSelectedServerNames.add(split[0].trim());
                             }
                         }
                         listSelectedServerIds = new ArrayList<Integer>(2);
                         //TODO:Need to handle
                         //						if (CollectionUtils.isNotEmpty(servers)) {
                         //							for (DownloadInfo server : servers) {
                         //								if(listSelectedServerNames.contains(server.getName())) {
                         //									listSelectedServerIds.add(server.getId());
                         //								}
                         //							}
                         //						}
                     }
                     setReqAttribute("listSelectedServerIds", listSelectedServerIds);
                     setReqAttribute(REQ_HEADER_TYPE, "Select");
                 } else {
                     attrName = getHttpRequest().getParameter("attrName");
                     String selectedVersions = getHttpRequest().getParameter("selectedVersions");
                     selectedVersions = selectedVersions.replaceAll(" ", "");
                     List<String> listSelectedVersions = new ArrayList<String>(Arrays.asList(selectedVersions.split(",")));
                     listSelectedServerIds = new ArrayList<Integer>(2);
                     //TODO:Need to handle
                     //					if (CollectionUtils.isNotEmpty(servers)) {
                     //						for (DownloadInfo server : servers) {
                     //							String serverName = server.getName().trim();
                     //							serverName = serverName.replaceAll("\\s+", "");
                     //							if(serverName.equals(attrName)) {
                     //								listSelectedServerIds.add(server.getId());
                     //							}
                     //						}
                     //					}
                     setReqAttribute("listSelectedServerIds", listSelectedServerIds);
                     setReqAttribute(REQ_LISTSELECTED_VERSIONS, listSelectedVersions);
                     setReqAttribute(REQ_HEADER_TYPE, "Edit");
                 }
                 //				setReqAttribute("servers", servers);//TODO:Need to handle
             }
             if (Constants.SETTINGS_TEMPLATE_DB.equals(type)) {
                 List<Integer> listSelectedDatabaseIds = null;
                 //				List<DownloadInfo> databases = techonology.getDatabases();//TODO:Need to handle
                 if(StringUtils.isEmpty(from)) {
                     List<String> listSelectedDbs = null;
                     List<String> listSelectedDbNames = null;
                     List<Integer> listSelectedDbIds = null;
                     String selectedDatabases = getHttpRequest().getParameter("selectedDatabases");
                     if (StringUtils.isNotEmpty(selectedDatabases)) {
                         listSelectedDbNames = new ArrayList<String>();
                         listSelectedDbs = new ArrayList<String>(Arrays.asList(selectedDatabases.split("#SEP#")));
                         for (String listSelectedDb : listSelectedDbs) {
                             String[] split = listSelectedDb.split("#VSEP#");
                             listSelectedDbNames.add(split[0].trim());
                         }
                         listSelectedDbIds = new ArrayList<Integer>(2);
                         //TODO:Need to handle
                         //						if (CollectionUtils.isNotEmpty(databases)) {
                         //							for (DownloadInfo database : databases) {
                         //								if(listSelectedDbNames.contains(database.getName())) {
                         //									listSelectedDbIds.add(database.getId());
                         //								}
                         //							}
                         //						}
                     }
                     setReqAttribute("listSelectedDatabaseIds", listSelectedDbIds);
                     setReqAttribute(REQ_HEADER_TYPE, "Select");
                 } else {
                     attrName = getHttpRequest().getParameter("attrName");
                     ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
                     if (StringUtils.isNotEmpty(projectCode)) {
                         Project project = administrator.getProject(projectCode);
                         if (project != null) {
                             ApplicationInfo appInfo = project.getApplicationInfo();
                             //							List<DownloadInfo> projectInfoDbs = appInfo.getTechnology().getDatabases();//TODO:Need to handle
                             List<String> projectInfoDbVersions = new ArrayList<String>();
                             StringBuilder sb = new StringBuilder();
                             //TODO:Need to handle
                             //							if (CollectionUtils.isNotEmpty(projectInfoDbs)) {
                             //								for (DownloadInfo projectInfoDb : projectInfoDbs) {
                             //									if (projectInfoDb.getName().equals(attrName)) {
                             //										projectInfoDbVersions.addAll(projectInfoDb.getVersions());
                             //									}
                             //								}
                             //								if (CollectionUtils.isNotEmpty(projectInfoDbVersions)) {
                             //									for (String projectInfoDbVersion : projectInfoDbVersions) {
                             //										sb.append(projectInfoDbVersion);
                             //										sb.append(",");
                             //									}
                             //	
                             //								}
                             //								if (StringUtils.isNotEmpty(sb.toString())) {
                             //									setReqAttribute("projectInfoDbVersions", sb.toString().substring(0, sb.length() - 1));
                             //								}
                             //							}
                         }
                     }
                     String selectedVersions = getHttpRequest().getParameter("selectedVersions");
                     selectedVersions = selectedVersions.replaceAll(" ", "");
                     List<String> listSelectedVersions = new ArrayList<String>(Arrays.asList(selectedVersions.split(",")));
                     listSelectedDatabaseIds = new ArrayList<Integer>(2);
                     //TODO:Need to handle
                     //					if (CollectionUtils.isNotEmpty(databases)) {
                     //						for (DownloadInfo database : databases) {
                     //							String databaseName = database.getName().trim();
                     //							databaseName = databaseName.replaceAll("\\s+", "");
                     //							if(databaseName.equals(attrName)) {
                     //								listSelectedDatabaseIds.add(database.getId());
                     //							}
                     //						}
                     //					}
                     setReqAttribute("listSelectedDatabaseIds", listSelectedDatabaseIds);
                     setReqAttribute(REQ_LISTSELECTED_VERSIONS, listSelectedVersions);
                     setReqAttribute(REQ_HEADER_TYPE, "Edit");
                 }
                 //				setReqAttribute("databases", databases);//TODO:Need to handle
             }
 
             setReqAttribute("attrName", attrName);
             setReqAttribute("header", type);
             setReqAttribute(REQ_FROM, from);
             setReqAttribute(REQ_FROM_PAGE, fromPage);
         } catch (PhrescoException e) {
             return showErrorPopup(e, "Getting server and database");
         }
 
         return "openAttrPopup";
     }
 
     public String allVersions() throws PhrescoException {
         versions = new ArrayList<String>(2);
         String techId = getHttpRequest().getParameter("techId");
         String appType = getHttpRequest().getParameter(REQ_APPLICATION_TYPE);
         String type = getHttpRequest().getParameter("type");
 
         int selectedId = Integer.parseInt(getHttpRequest().getParameter("selectedId"));
         ApplicationType applicationType = ApplicationsUtil.getApplicationType(getHttpRequest(), appType);
         //TODO:Need to handle
         //		Technology selectedTechnology = applicationType.getTechonology(techId);
         //		
         //		if ("Server".equals(type)) {
         //			List<DownloadInfo> servers = selectedTechnology.getServers();
         //			for (DownloadInfo server : servers) {
         //				if(server.getId() == selectedId) {
         //					versions = server.getVersions();
         //				}
         //			}
         //		}
         //		if ("Database".equals(type)) {
         //			List<DownloadInfo> databases = selectedTechnology.getDatabases();
         //			for (DownloadInfo database : databases) {
         //				if(database.getId() == selectedId) {
         //					versions.addAll(database.getVersions());
         //				}
         //			}
         //		}
 
         return SUCCESS;
     }
 
     public String addDetails() throws PhrescoException {
         String type = getHttpRequest().getParameter("type");
         setSelectedParamName(getHttpRequest().getParameter("paramName"));
         divTobeUpdated = getHttpRequest().getParameter("divTobeUpdated");
         if ("Server".equals(type)) {
             String[] selectedServerVersions = getHttpRequest().getParameterValues("serverVersion");
             selectedVersions = convertToCommaDelimited(selectedServerVersions);
         } else {
             String[] selectedDbVersions = getHttpRequest().getParameterValues("databaseVersion");
             selectedVersions = convertToCommaDelimited(selectedDbVersions);
         }
         setSelectedAttrType(type);
 
         return SUCCESS;
     }
 
     public static String convertToCommaDelimited(String[] list) {
         StringBuffer retString = new StringBuffer("");
         for (int i = 0; list != null && i < list.length; i++) {
             retString.append(list[i]);
             if (i < list.length - 1) {
                 retString.append(','+" ");
             }
         }
 
         return retString.toString();
     }
 
     public String checkForRespectiveConfig() throws PhrescoException {
         try {
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             Project project = administrator.getProject(projectCode);
             Map<String, List<String>> deleteConfigs = new HashMap<String , List<String>>();
             List<String> configNames = new ArrayList<String>();
             List<SettingsInfo> configurations = administrator.configurations(selectedAttrType, project);
             if (CollectionUtils.isNotEmpty(configurations)) {
                 for (SettingsInfo config : configurations) {
                     deleteConfigs.clear();
                     configNames.clear();
                     PropertyInfo serverType = config.getPropertyInfo(Constants.SERVER_TYPE);
                     if (serverType.getValue().equalsIgnoreCase(selectedParamName)) {
                         setHasConfiguration(true);
                     }
                 }
             }
         } catch (Exception e) {
             throw new PhrescoException(e);
         }
 
         return SUCCESS;
     }
 
     private void removeConfiguration() throws PhrescoException {
         try {
             if (StringUtils.isNotEmpty(configServerNames)) {
                 deleteConfigurations("Server", configServerNames.substring(0, configServerNames.length() - 1));
             }
             if (StringUtils.isNotEmpty(configDbNames)) {
                 deleteConfigurations("Database", configDbNames.substring(0, configDbNames.length() - 1));
             }
         } catch (Exception e) {
             throw new PhrescoException(e);
         }
     }
 
     private void deleteConfigurations (String type, String configName) throws PhrescoException {
         try {
             String [] items = configName.split(",");
             List<String> deleteConfigNames = Arrays.asList(items);
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             Project project = administrator.getProject(projectCode);
             Map<String, List<String>> deleteConfigs = new HashMap<String , List<String>>();
             List<String> configNames = new ArrayList<String>();
             List<SettingsInfo> configurations = administrator.configurations(type, project);
             if (CollectionUtils.isNotEmpty(configurations)) {
                 for (SettingsInfo config : configurations) {
                     deleteConfigs.clear();
                     configNames.clear();
                     PropertyInfo serverType = config.getPropertyInfo(Constants.SERVER_TYPE);
                     for (String deleteConfigName : deleteConfigNames) {
                         if (serverType.getValue().equalsIgnoreCase(deleteConfigName)) {
                             configNames.add(config.getName());
                             deleteConfigs.put(config.getEnvName(), configNames);
                             administrator.deleteConfigurations(deleteConfigs, project);
                         }
                     }
                 }
             }
         } catch(Exception e) {
             throw new PhrescoException(e);
         }
     }
 
     public String checkForConfiguration() throws PhrescoException {
         try {
 			boolean isError = false;
 			ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 			ApplicationInfo applicationInfo = applicationManager.getApplicationInfo(getCustomerId(), getProjectId(), getAppId());
 
 //			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 //			Project project = administrator.getProject(projectCode);
 			//TODO:Need to handle
 //			Technology technology = project.getApplicationInfo().getTechnology();
 //			List<Server> servers = technology.getServers();
 //			List<Database> databases = technology.getDatabases();
 //			List<WebService> webservices = technology.getWebservices();
 //			boolean emailSupported = technology.isEmailSupported();
 
 			String envs = getHttpRequest().getParameter(ENVIRONMENTS);
 			if(StringUtils.isEmpty(envs)){
 				setHasError(false);
 				return SUCCESS;
 			}
 			String[] envArr = envs.split(",");
 			List<String> unAvailableTypes = new ArrayList<String>();
 			String from = getHttpRequest().getParameter(REQ_FROM);
 			for (String envName : envArr) {
 				if (NODEJS_RUN_AGAINST.equals(from) || JAVA_RUN_AGAINST.equals(from)) {
 //					if (CollectionUtils.isEmpty(administrator.getSettingsInfos(Constants.SETTINGS_TEMPLATE_SERVER, projectCode, envName))) {
 //						setEnvError(getText(ERROR_NO_CONFIG));
 //						setHasError(true);
 //						return SUCCESS;
 //					}
 				}
 
 				//TODO:Need to handle
 //				if (CollectionUtils.isNotEmpty(servers) && CollectionUtils.isEmpty(administrator.getSettingsInfos(Constants.SETTINGS_TEMPLATE_SERVER, projectCode, envName))) {
 //					isError = true;
 //					unAvailableTypes.add("Server");
 //				}
 //				
 //				if (CollectionUtils.isNotEmpty(databases) && CollectionUtils.isEmpty(administrator.getSettingsInfos(Constants.SETTINGS_TEMPLATE_DB, projectCode, envName))) {
 //					isError = true;
 //					unAvailableTypes.add("Database");
 // 				}
 //				
 //				if (CollectionUtils.isNotEmpty(webservices) && CollectionUtils.isEmpty(administrator.getSettingsInfos(Constants.SETTINGS_TEMPLATE_WEBSERVICE, projectCode, envName))) {
 //					isError = true;
 //					unAvailableTypes.add("WebService");
 //				}
 //				
 //				if (emailSupported && CollectionUtils.isEmpty(administrator.getSettingsInfos(Constants.SETTINGS_TEMPLATE_EMAIL, projectCode, envName))) {
 //					isError = true;
 //					unAvailableTypes.add("E-mail");
 //				}
 
 				if (isError) {
 					String csvUnAvailableTypes = "";
 					if (CollectionUtils.isNotEmpty(unAvailableTypes)) {
 						csvUnAvailableTypes = StringUtils.join(unAvailableTypes.toArray(), ",");
 					}
 					setEnvError(csvUnAvailableTypes + " Configurations not available for " + envName + " environment");
 					setHasError(true);
 					return SUCCESS;
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 
         return SUCCESS;
     }
 
     public String checkForConfigType() throws PhrescoException {
         try{
             String envs = getHttpRequest().getParameter(REQ_ENVIRONMENTS);
             if(StringUtils.isEmpty(envs)){
                 setHasError(false);
                 return SUCCESS;
             }
             String type = getHttpRequest().getParameter("type");
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             if(CollectionUtils.isEmpty(administrator.getSettingsInfos(type, projectCode, envs))) {
                 setEnvError(getText(ERROR_ENV_CONFIG, Collections.singletonList(type)));
                 setHasError(true);
             }
         } catch(Exception e) {
             throw new PhrescoException(e);
         }
 
         return SUCCESS;
     }
 
     public String browse() {
         S_LOGGER.debug("Entering Method  Applications.browse()");
         try {
             setReqAttribute(FILE_TYPES, fileType);
             setReqAttribute(FILE_BROWSE, fileorfolder);
             String projectLocation = Utility.getProjectHome() + projectCode;
             setReqAttribute(REQ_PROJECT_LOCATION, projectLocation.replace(File.separator, FORWARD_SLASH));
             setReqAttribute(REQ_PROJECT_CODE, projectCode);
         } catch (Exception e) {
             S_LOGGER.error("Entered into catch block of  Applications.browse()"	+ FrameworkUtil.getStackTraceAsString(e));
             new LogErrorReport(e, "File Browse");
         }
         return SUCCESS;
     }
 
     public String authenticateServer() throws PhrescoException {
         try {
             String host = (String)getHttpRequest().getParameter(SERVER_HOST);
             int port = Integer.parseInt(getHttpRequest().getParameter(SERVER_PORT));
             ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
             boolean connectionAlive = DiagnoseUtil.isConnectionAlive("https", host, port);
             boolean isCertificateAvailable = false;
             if (connectionAlive) {
                 List<CertificateInfo> certificates = administrator.getCertificate(host, port);
                 if (CollectionUtils.isNotEmpty(certificates)) {
                     isCertificateAvailable = true;
                     setReqAttribute("certificates", certificates);
                 }
             }
             setReqAttribute(FILE_TYPES, FILE_TYPE_CRT);
             setReqAttribute(FILE_BROWSE, FILE_BROWSE);
             String projectLocation = "";
             if (StringUtils.isNotEmpty(projectCode)) {
                 projectLocation = Utility.getProjectHome() + projectCode;
             } else {
                 projectLocation = Utility.getProjectHome();
             }
             setReqAttribute(REQ_PROJECT_LOCATION, projectLocation.replace(File.separator, FORWARD_SLASH));
             setReqAttribute(REQ_RMT_DEP_IS_CERT_AVAIL, isCertificateAvailable);
             setReqAttribute(REQ_RMT_DEP_FILE_BROWSE_FROM, CONFIGURATION);
         } catch(Exception e) {
             throw new PhrescoException(e);
         }
 
         return SUCCESS;
     }
 
     public boolean importFromGit(String url, File directory) throws Exception {
         S_LOGGER.debug("Entering Method  Applications.importFromGit()");
         S_LOGGER.debug("importing git " + url);
         Git repo = Git.cloneRepository().setURI(url).setDirectory(directory).call();
         for (Ref b : repo.branchList().setListMode(ListMode.ALL).call()) {
             S_LOGGER.debug("(standard): cloned branch " + b.getName());
         }
         repo.getRepository().close();
         return true;
     }
 
     public ApplicationInfo getAppInfo(File directory) throws Exception {
         S_LOGGER.debug("Entering Method  Applications.getProjectInfo()");
         BufferedReader reader = null;
         try {
             File dotProjectFile = new File(directory, FOLDER_DOT_PHRESCO + File.separator + PROJECT_INFO);
             S_LOGGER.debug("dotProjectFile" + dotProjectFile);
             if (!dotProjectFile.exists()) {
                 throw new PhrescoException("Phresco Project definition not found");
             }
             reader = new BufferedReader(new FileReader(dotProjectFile));
             return new Gson().fromJson(reader, ApplicationInfo.class);
         } finally {
             Utility.closeStream(reader);
         }
     }
 
     private void importToWorkspace(File gitImportTemp, String phrescoHomeDirectory, String projectCode) throws Exception {
         S_LOGGER.debug("Entering Method  Applications.importToWorkspace()");
         File workspaceProjectDir = new File(phrescoHomeDirectory + projectCode);
         S_LOGGER.debug("workspaceProjectDir "+ workspaceProjectDir);
         if (workspaceProjectDir.exists()) {
             S_LOGGER.debug("workspaceProjectDir exists"+ workspaceProjectDir);
             throw new org.apache.commons.io.FileExistsException();
         }
         S_LOGGER.debug("gitImportTemp ====> " + gitImportTemp);
         S_LOGGER.debug("workspaceProjectDir ====> " + workspaceProjectDir);
         FileUtils.copyDirectory(gitImportTemp, workspaceProjectDir);
         try {
             FileUtils.deleteDirectory(gitImportTemp);
         } catch (IOException e) {
             S_LOGGER.debug("pack file is not deleted "  + e.getLocalizedMessage());
         }
     }
 
     private void updateSCMConnection(String projCode, String repoUrl) throws Exception {
         S_LOGGER.debug("Entering Method  Applications.updateSCMConnection()");
         FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
         PomProcessor processor = frameworkUtil.getPomProcessor(projCode);
         if (processor.getSCM() == null) {
             S_LOGGER.debug("processor.getSCM() exists and repo url " + repoUrl);
             processor.setSCM(repoUrl, "", "", "");
             processor.save();
         }
     }*/
 
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
 
     public String getUsername() {
         return userName;
     }
 
     public void setUsername(String username) {
         this.userName = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getRevision() {
         return revision;
     }
 
     public void setRevision(String revision) {
         this.revision = revision;
     }
 
     public String getRevisionVal() {
         return revisionVal;
     }
 
     public void setRevisionVal(String revisionVal) {
         this.revisionVal = revisionVal;
     }
 
     public String getShowSettings() {
         return showSettings;
     }
 
     public void setShowSettings(String showSettings) {
         this.showSettings = showSettings;
     }
 
     public String getGlobalValidationStatus() {
         return globalValidationStatus;
     }
 
     public void setGlobalValidationStatus(String globalValidationStatus) {
         this.globalValidationStatus = globalValidationStatus;
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
 
     public List<String> getVersions() {
         return versions;
     }
 
     public void setVersions(List<String> versions) {
         this.versions = versions;
     }
 
     public String getSelectedVersions() {
         return selectedVersions;
     }
 
     public void setSelectedVersions(String selectedVersions) {
         this.selectedVersions = selectedVersions;
     }
 
     public String getSelectedAttrType() {
         return selectedAttrType;
     }
 
     public void setSelectedAttrType(String selectedAttrType) {
         this.selectedAttrType = selectedAttrType;
     }
 
     public String getSelectedParamName() {
         return selectedParamName;
     }
 
     public void setSelectedParamName(String selectedParamName) {
         this.selectedParamName = selectedParamName;
     }
 
     public String getDivTobeUpdated() {
         return divTobeUpdated;
     }
 
     public void setDivTobeUpdated(String divTobeUpdated) {
         this.divTobeUpdated = divTobeUpdated;
     }
 
     public List<String> getSettingsEnv() {
         return settingsEnv;
     }
 
     public void setSettingsEnv(List<String> settingsEnv) {
         this.settingsEnv = settingsEnv;
     }
 
     public boolean isHasError() {
         return hasError;
     }
 
     public void setHasError(boolean hasError) {
         this.hasError = hasError;
     }
 
     public String getEnvError() {
         return envError;
     }
 
     public void setEnvError(String envError) {
         this.envError = envError;
     }
 
     public List<String> getTechVersions() {
         return techVersions;
     }
 
     public void setTechVersions(List<String> techVersions) {
         this.techVersions = techVersions;
     }
 
     public boolean isHasConfiguration() {
         return hasConfiguration;
     }
 
     public void setHasConfiguration(boolean hasConfiguration) {
         this.hasConfiguration = hasConfiguration;
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
 
     public boolean isSvnImport() {
         return svnImport;
     }
 
     public void setSvnImport(boolean svnImport) {
         this.svnImport = svnImport;
     }
 
     public String getSvnImportMsg() {
         return svnImportMsg;
     }
 
     public void setSvnImportMsg(String svnImportMsg) {
         this.svnImportMsg = svnImportMsg;
     }
 
     public String getFromTab() {
         return fromTab;
     }
 
     public void setFromTab(String fromTab) {
         this.fromTab = fromTab;
     }
 
     public String getCredential() {
         return credential;
     }
 
     public void setCredential(String credential) {
         this.credential = credential;
     }
 
     public String getFileType() {
         return fileType;
     }
 
     public void setFileType(String fileType) {
         this.fileType = fileType;
     }
 
     public String getFileorfolder() {
         return fileorfolder;
     }
 
     public void setFileorfolder(String fileorfolder) {
         this.fileorfolder = fileorfolder;
     }
 
     public String getRepoType() {
         return repoType;
     }
 
     public void setRepoType(String repoType) {
         this.repoType = repoType;
     }
 
     public String getApplicationType() {
         return applicationType;
     }
 
     public void setApplicationType(String applicationType) {
         this.applicationType = applicationType;
     }
 
     public String getTechnology() {
         return technology;
     }
 
     public void setTechnology(String technology) {
         this.technology = technology;
     }
 
     public List<DownloadInfo> getServers() {
         return servers;
     }
 
     public void setServers(List<DownloadInfo> servers) {
         this.servers = servers;
     }
     
 	public List<DownloadInfo> getDownloadInfos() {
 		return downloadInfos;
 	}
 	
 	public void setDownloadInfos(List<DownloadInfo> downloadInfos) {
 		this.downloadInfos = downloadInfos;
 	}
 	public List<String> getFeature() {
 		return feature;
 	}
 	public void setFeature(List<String> feature) {
 		this.feature = feature;
 	}
 	public List<String> getComponent() {
 		return component;
 	}
 	public void setComponent(List<String> component) {
 		this.component = component;
 	}
 	public List<String> getJavascript() {
 		return javascript;
 	}
 	public void setJavascript(List<String> javascript) {
 		this.javascript = javascript;
 	}
 	public String getTechId() {
 		return techId;
 	}
 	public void setTechId(String techId) {
 		this.techId = techId;
 	}
 	public String getType() {
 		return type;
 	}
 	public void setType(String type) {
 		this.type = type;
 	}
 	public String getApplicationId() {
 		return applicationId;
 	}
 	public void setApplicationId(String applicationId) {
 		this.applicationId = applicationId;
 	}
 
 	public boolean isErrorFlag() {
 		return errorFlag;
 	}
 
 	public void setErrorFlag(boolean errorFlag) {
 		this.errorFlag = errorFlag;
 	}
 
 	public String getErrorString() {
 		return errorString;
 	}
 
 	public void setErrorString(String errorString) {
 		this.errorString = errorString;
 	}
 	
 	public SelectedFeature getSelectFeature() {
 		return selectFeature;
 	}
 
 	public void setSelectFeature(SelectedFeature selectFeature) {
 		this.selectFeature = selectFeature;
 	}
 
 	public List<String> getJsonData() {
 		return jsonData;
 	}
 
 	public void setJsonData(List<String> jsonData) {
 		this.jsonData = jsonData;
 	}
 
 	public String getOldAppDirName() {
 		return oldAppDirName;
 	}
 
 	public void setOldAppDirName(String oldAppDirName) {
 		this.oldAppDirName = oldAppDirName;
 	}
 
 	public String getRepoUrl() {
 		return repoUrl;
 	}
 
 	public void setRepoUrl(String repoUrl) {
 		this.repoUrl = repoUrl;
 	}
 	public String getSelectedDownloadInfo() {
 		return selectedDownloadInfo;
 	}
 
 	public void setSelectedDownloadInfo(String selectedDownloadInfo) {
 		this.selectedDownloadInfo = selectedDownloadInfo;
 	}
 
 	public String getSelectBoxId() {
 		return selectBoxId;
 	}
 
 	public void setSelectBoxId(String selectBoxId) {
 		this.selectBoxId = selectBoxId;
 	}
 
 	public String getSelectedDownloadInfoVersion() {
 		return selectedDownloadInfoVersion;
 	}
 
 	public void setSelectedDownloadInfoVersion(String selectedDownloadInfoVersion) {
 		this.selectedDownloadInfoVersion = selectedDownloadInfoVersion;
 	}
 
 	public String getDownloadInfoType() {
 		return downloadInfoType;
 	}
 
 	public void setDownloadInfoType(String downloadInfoType) {
 		this.downloadInfoType = downloadInfoType;
 	}
 
 	public String getDefaultOptTxt() {
 		return defaultOptTxt;
 	}
 
 	public void setDefaultOptTxt(String defaultOptTxt) {
 		this.defaultOptTxt = defaultOptTxt;
 	}
 
 	public String getAction() {
 		return action;
 	}
 
 	public void setAction(String action) {
 		this.action = action;
 	}
 
 	public String getCommitMessage() {
 		return commitMessage;
 	}
 
 	public void setCommitMessage(String commitMessage) {
 		this.commitMessage = commitMessage;
 	}
 }
