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
 package com.photon.phresco.framework.actions.applications;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Type;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileExistsException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.InitCommand;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.api.errors.InvalidRemoteException;
 import org.eclipse.jgit.api.errors.TransportException;
 import org.eclipse.jgit.lib.Config;
 import org.tmatesoft.svn.core.SVNAuthenticationException;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.wc.SVNStatus;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ArtifactInfo;
import com.photon.phresco.commons.model.Category;
 import com.photon.phresco.commons.model.CoreOption;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.Element;
 import com.photon.phresco.commons.model.FunctionalFramework;
 import com.photon.phresco.commons.model.FunctionalFrameworkProperties;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.SelectedFeature;
 import com.photon.phresco.commons.model.SettingsTemplate;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.WebService;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.framework.api.ProjectManager;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.framework.impl.SCMManagerImpl;
 import com.photon.phresco.framework.model.LockDetail;
 import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Scm;
 import com.phresco.pom.util.PomProcessor;
 
 
 public class Applications extends FrameworkBaseAction implements Constants {
 
     private static final long serialVersionUID = -4282767788002019870L;
 
     private static final Logger S_LOGGER = Logger.getLogger(Applications.class);
     private static Boolean s_debugEnabled = S_LOGGER.isDebugEnabled();
 
     private List<DownloadInfo> downloadInfos = null;
     private String oldAppDirName = "";
 
     private String userName = "";
     private String password = "";
     private String revision = "";
     private String revisionVal = "";
     private String repoType = "";
     private String repoUrl = "";
     private String testUserName = "";
     private String testPassword = "";
     private String testRevision = "";
     private String testRevisionVal = "";
     private String testRepoUrl = "";
     private boolean testClone;
     private String commitMessage = "";
     private List<String> commitableFiles = null;
 	private Properties commitableGITFiles = null;
     private List<DownloadInfo> servers = null;
     List<String> restrictedLogs =  null;
 
     private String technology = "";
     
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
     private boolean isRepoExistForCommit;
     private boolean isRepoExistForUpdate;
     private String logMessage = "";
     private String actionType = "";
     private String customerId = "";
     private String projectId = "";
     private String pomVersion = "";
     private int selectedServersCount;
     private int selectedDatabasesCount;
     
     private boolean locked = false;
     private String lockedBy = "";
     private String lockedDate = "";
 
     public String loadMenu() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.loadMenu()");
         }
 
         try {
         	ApplicationInfo applicationInfo = getApplicationInfo();
         	getApplicationProcessor().adoptApplication(applicationInfo);
 			String techId = applicationInfo.getTechInfo().getId();
 			Technology technology = getServiceManager().getArcheType(techId, getCustomerId());
 			List<String> optionIds = technology.getOptions();
 			
 			ApplicationInfo appInfo = getApplicationInfo();
             if (appInfo != null) {
                 techId = appInfo.getTechInfo().getId();
             }
             List<SettingsTemplate> settingsTemplates = getServiceManager().getConfigTemplates(getCustomerId(), techId);
             if (CollectionUtils.isNotEmpty(settingsTemplates)) {
             	List<SettingsTemplate> favouriteConfigs = new ArrayList<SettingsTemplate>();
             	for (SettingsTemplate settingsTemplate : settingsTemplates) {
             		if (settingsTemplate.isFavourite()) {
             			favouriteConfigs.add(settingsTemplate);
             		}
             	}
             	setReqAttribute(REQ_FAVOURITE_CONFIGS, favouriteConfigs);
             }
 			setSessionAttribute(REQ_OPTION_ID, optionIds);
             setReqAttribute(REQ_CURRENT_APP_NAME, getApplicationInfo().getName());
             setReqAttribute(REQ_PROJECT_ID, getProjectId());
             setReqAttribute(REQ_APP_ID, getAppId());
         } catch(PhrescoException e) {
         	return showErrorPopup(e, EXCEPTION_LOADMENU);
         }
 
         return APP_MENU;
     }
     
     public String editApplication() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.editApplication()");
         }
         
         try {
             removeSessionAttribute(getAppId() + SESSION_APPINFO);
             updateLatestProject();
         } catch (PhrescoException e) {
             // TODO: handle exception
         } 
         
         return appInfo();
     }
     
     public String appInfo() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.appInfo()");
         }
 
         try {
         	setReqAttribute(REQ_UI_TYPE, getUiType());
         	ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
         	ProjectInfo projectInfo = null;
         	String technologyId = "";
         	if (getSessionAttribute(getAppId() + SESSION_APPINFO) == null) {
         		projectInfo = projectManager.getProject(getProjectId(), getCustomerId(), getAppId());
         		ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
         		if (LAYER_MOB_ID.equals(appInfo.getTechInfo().getAppTypeId())) {
                     ProjectInfo project = projectManager.getProject(getProjectId(), getCustomerId());
                     List<ApplicationInfo> appInfos = project.getAppInfos();
                     for (ApplicationInfo applicationInfo : appInfos) {
                         if (LAYER_WEB_ID.equals(applicationInfo.getTechInfo().getAppTypeId())) {
                             setReqAttribute(REQ_WEB_LAYER_APPINFO, applicationInfo);
                             break;
                         }
                     }
                 }
         		ServiceManager serviceManager = getServiceManager();
         		technologyId = projectInfo.getAppInfos().get(0).getTechInfo().getId();
         		List<ApplicationInfo> pilotProjects = serviceManager.getPilotProjects(getCustomerId(), technologyId);
         		Technology technologyInfo = serviceManager.getTechnology(technologyId);
         		setSessionAttribute(REQ_TECHNOLOGY, technologyInfo);
         		setSessionAttribute(REQ_PILOT_PROJECTS, pilotProjects);
         		setSessionAttribute(getAppId() + SESSION_APPINFO, projectInfo);
         		setReqAttribute(REQ_OLD_APPDIR, projectInfo.getAppInfos().get(0).getName());
         		List<FunctionalFramework> functionalFrameworks = technologyInfo.getFunctionalFrameworks();
         		setSessionAttribute(REQ_FUNCTIONAL_TEST_FRAMEWORKS, functionalFrameworks);
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
         	
         	technologyId = projectInfo.getAppInfos().get(0).getTechInfo().getId();
         	//To check whether the selected technology has servers
         	
         	List<DownloadInfo> applicableServers = getServiceManager().getDownloads(getCustomerId(), 
         			technologyId, Category.SERVER.name(), FrameworkUtil.findPlatform());
         	boolean hasServer = false;
         	if(CollectionUtils.isNotEmpty(applicableServers)) {
         		hasServer = true;
 			}
         	setReqAttribute(REQ_TECH_HAS_SERVER, hasServer);
 
         	//To check whether the selected technology has databases
         	boolean hasDb = false;
         	List<DownloadInfo> applicableDbs = getServiceManager().getDownloads(getCustomerId(), 
         			technologyId, Category.DATABASE.name(), FrameworkUtil.findPlatform());
         	if(CollectionUtils.isNotEmpty(applicableDbs)) {
         		hasDb = true;
 			}
         	setReqAttribute(REQ_TECH_HAS_DB, hasDb);
         	
         	//To check whether the selected technology has webservices
         	boolean hasWebservice = false;
         	SettingsTemplate webserviceConfigTemplate = getServiceManager().getConfigTemplate(FrameworkConstants.TECH_WEBSERVICE_ID);
         	List<Element> webServiceAppliesToTechs = webserviceConfigTemplate.getAppliesToTechs();
         	for (Element webServiceAppliesToTech : webServiceAppliesToTechs) {
 				if (webServiceAppliesToTech.getId().equals(technologyId)) {
 					hasWebservice = true;
 					break;
 				}
 			}
         	setReqAttribute(REQ_TECH_HAS_WEBSERVICE, hasWebservice);
         	
             List<WebService> webServices = getServiceManager().getWebServices();
             setReqAttribute(REQ_WEBSERVICES, webServices);
             setReqAttribute(REQ_APP_ID, getAppId());
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             PomProcessor processor = frameworkUtil.getPomProcessor(getApplicationInfo().getAppDirName());
             setPomVersion(processor.getModel().getVersion());
             setReqAttribute(REQ_POM_VERSION, getPomVersion());
         } catch (PhrescoException e) {
         	return showErrorPopup(e, EXCEPTION_APPLICATION_EDIT);
         }
 
         return APP_APPINFO;
     }
     
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
             List<DownloadInfo> downloadInfos = getServiceManager().getDownloads(getCustomerId(), techId, type, FrameworkUtil.findPlatform());
             if (CollectionUtils.isNotEmpty(downloadInfos)) {
             	Collections.sort(downloadInfos, sortdownloadInfoInAlphaOrder());
             }
 			setDownloadInfos(downloadInfos);
 			ProjectInfo projectInfo = null;
 			projectInfo = (ProjectInfo)getSessionAttribute(getAppId() + SESSION_APPINFO);
 			ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
 			
         	int selectedServersCount;
 			if(appInfo.getSelectedServers()!=null){
 				selectedServersCount = appInfo.getSelectedServers().size();
 				setSelectedServersCount(selectedServersCount);
 			}else{
 				selectedServersCount = -1;
 				setSelectedServersCount(selectedServersCount);
 			}
 			
 			int selectedDatabasesCount;
 			if(appInfo.getSelectedDatabases() != null){
 				selectedDatabasesCount = appInfo.getSelectedDatabases().size();
 				setSelectedDatabasesCount(selectedDatabasesCount);
 			}else{
 				selectedDatabasesCount = -1;
 				setSelectedDatabasesCount(selectedDatabasesCount);
 			}
         } catch (PhrescoException e) {
             return showErrorPopup(e, getText(EXCEPTION_DOWNLOADINFOS));
         }
 
         return SUCCESS;
     }
     
     private Comparator sortdownloadInfoInAlphaOrder() {
 		return new Comparator() {
 		    public int compare(Object firstObject, Object secondObject) {
 		    	DownloadInfo downloadInfo1 = (DownloadInfo) firstObject;
 		    	DownloadInfo downloadInfo2 = (DownloadInfo) secondObject;
 		       return downloadInfo1.getName().compareToIgnoreCase(downloadInfo2.getName());
 		    }
 		};
 	}
     
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
     		if (!newlySelectedModuleGrpIds.contains(artifactInfo.getArtifactGroupId())) {
     			ArtifactGroup artifactGroupInfo = getServiceManager().getArtifactGroupInfo(artifactInfo.getArtifactGroupId());
     			artifactGroups.add(artifactGroupInfo);
     		}
     	}
     }
     
     public String update() throws IOException {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Applications.update()");
 		}  
     	
     	BufferedReader bufferedReader = null;
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
 					ArtifactInfo artifactInfo = getServiceManager().getArtifactInfo(obj.getVersionID());
 					artifactInfo.setScope(obj.getScope());
 					if(artifactInfo != null) {
 						artifactGroup.setVersions(Collections.singletonList(artifactInfo));
 					}
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
         	
         	File filePath = null;
         	if (StringUtils.isNotEmpty(getOldAppDirName())) {
 	        	StringBuilder sb = new StringBuilder(Utility.getProjectHome())
 	        	.append(getOldAppDirName())
 	        	.append(File.separator)
 	        	.append(Constants.DOT_PHRESCO_FOLDER)
 	        	.append(File.separator)
 	        	.append(Constants.APPLICATION_HANDLER_INFO_FILE);
 				filePath = new File(sb.toString());
         	} else {
         		StringBuilder sb = new StringBuilder(Utility.getProjectHome())
 	        	.append(appInfo.getAppDirName())
 	        	.append(File.separator)
 	        	.append(Constants.DOT_PHRESCO_FOLDER)
 	        	.append(File.separator)
 	        	.append(Constants.APPLICATION_HANDLER_INFO_FILE);
 				filePath = new File(sb.toString());
         	}
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
 					DownloadInfo downloadInfo = getServiceManager().getDownloadInfo(selectedDatabase.getArtifactGroupId());
 					String id = downloadInfo.getArtifactGroup().getId();
 					ArtifactGroup artifactGroupInfo = getServiceManager().getArtifactGroupInfo(id);
 					List<ArtifactInfo> dbVersionInfos = artifactGroupInfo.getVersions();//version infos from downloadInfo
 					List<ArtifactInfo> selectedDBVersionInfos = new ArrayList<ArtifactInfo>();//for selected version infos from ui
 					for (ArtifactInfo versionInfo : dbVersionInfos) {
 						String versionId = versionInfo.getId();
 						if (selectedDatabase.getArtifactInfoIds().contains(versionId)) {
 							selectedDBVersionInfos.add(versionInfo);//Add selected version infos to list
 						}
 					}
 					downloadInfo.getArtifactGroup().setVersions(selectedDBVersionInfos);
 					selectedDatabaseGroup.add(downloadInfo);
 				}
 				if (CollectionUtils.isNotEmpty(selectedDatabaseGroup)) {
 					String databaseGroup = gson.toJson(selectedDatabaseGroup);
 					applicationHandler.setSelectedDatabase(databaseGroup);
 				}
 			} else {//To remove selectedDatabse tag from application-handler.xml
 				applicationHandler.setSelectedDatabase(null);
 			}
 			
 			//To write selected Servers into phresco-application-Handler-info.xml
 			List<ArtifactGroupInfo> selectedServers = appInfo.getSelectedServers();
 			if (CollectionUtils.isNotEmpty(selectedServers)) {
 				for (ArtifactGroupInfo selectedServer : selectedServers) {
 					DownloadInfo downloadInfo = getServiceManager().getDownloadInfo(selectedServer.getArtifactGroupId());
 					String id = downloadInfo.getArtifactGroup().getId();
 					ArtifactGroup artifactGroupInfo = getServiceManager().getArtifactGroupInfo(id);
 					List<ArtifactInfo> serverVersionInfos = artifactGroupInfo.getVersions();//version infos from downloadInfo
 					List<ArtifactInfo> selectedServerVersionInfos = new ArrayList<ArtifactInfo>();//for selected version infos from ui
 					for (ArtifactInfo versionInfo : serverVersionInfos) {
 						String versionId = versionInfo.getId();
 						if (selectedServer.getArtifactInfoIds().contains(versionId)) {
 							selectedServerVersionInfos.add(versionInfo);//Add selected version infos to list
 						}
 					}
 					downloadInfo.getArtifactGroup().setVersions(selectedServerVersionInfos);//set only selected version infos to current download info
 					selectedServerGroup.add(downloadInfo);
 				}
 				if (CollectionUtils.isNotEmpty(selectedServerGroup)) {
 					String serverGroup = gson.toJson(selectedServerGroup);
 					applicationHandler.setSelectedServer(serverGroup);
 				}
 			} else {//To remove selectedServer tag from application-handler.xml
 				applicationHandler.setSelectedServer(null);
 			}
 			
 			//To write selected WebServices info to phresco-plugin-info.xml
 			List<String> selectedWebservices = appInfo.getSelectedWebservices();
 			List<WebService> webServiceList = new ArrayList<WebService>();
 			if (CollectionUtils.isNotEmpty(selectedWebservices)) {
 				for (String selectedWebService : selectedWebservices) {
 					WebService webservice = getServiceManager().getWebService(selectedWebService);
 					webServiceList.add(webservice);//add selected webservice infos to list
 				}
 				if (CollectionUtils.isNotEmpty(webServiceList)) {
 					String serverGroup = gson.toJson(webServiceList);
 					applicationHandler.setSelectedWebService(serverGroup);
 				}
 			} else {//To remove selectedWebService tag from application-handler.xml
 				applicationHandler.setSelectedWebService(null);
 			}
 
         	mojo.save();
         	appInfo.setSelectedModules(selectedFeatures);
         	appInfo.setSelectedJSLibs(selectedJsLibs);
         	appInfo.setSelectedComponents(selectedComponents);
         	
         	StringBuilder sbs = null;
         	if(StringUtils.isNotEmpty(getOldAppDirName())) {
         		sbs = new StringBuilder(Utility.getProjectHome()).append(getOldAppDirName()).append(
 					File.separator).append(Constants.DOT_PHRESCO_FOLDER).append(File.separator).append("project.info");
         	} else {
         		sbs = new StringBuilder(Utility.getProjectHome()).append(appInfo.getAppDirName()).append(
     					File.separator).append(Constants.DOT_PHRESCO_FOLDER).append(File.separator).append("project.info");
         	}
 			bufferedReader = new BufferedReader(new FileReader(sbs.toString()));
 			Type type = new TypeToken<ProjectInfo>() {}.getType();
 			ProjectInfo projectinfo = gson.fromJson(bufferedReader, type);
 			ApplicationInfo applicationInfo = projectinfo.getAppInfos().get(0);
 			
 			bufferedReader.close();
 			deleteSqlFolder(applicationInfo, selectedDatabases);
 			
     		projectInfo.setAppInfos(Collections.singletonList(appInfo));
     		ProjectManager projectManager = PhrescoFrameworkFactory.getProjectManager();
     		projectManager.update(projectInfo, getServiceManager(), getOldAppDirName());
     		updateFunctionalTestProperties(appInfo);
             List<ProjectInfo> projects = projectManager.discover(getCustomerId());
             if (CollectionUtils.isNotEmpty(projects)) {
             	Collections.sort(projects, sortByNameInAlphaOrder());
             }
             setReqAttribute(REQ_PROJECTS, projects);
             removeSessionAttribute(getAppId() + SESSION_APPINFO);
             removeSessionAttribute(REQ_SELECTED_FEATURES);
             removeSessionAttribute(REQ_DEFAULT_FEATURES);
     	} catch (PhrescoException e) {
     		return showErrorPopup(e, EXCEPTION_PROJECT_UPDATE);
     	} catch (FileNotFoundException e) {
     		return showErrorPopup(new PhrescoException(e), EXCEPTION_PROJECT_UPDATE);
     	} finally {
     		Utility.closeReader(bufferedReader);
     	}
     	return SUCCESS;
     }
     
     private void updateFunctionalTestProperties(ApplicationInfo appInfo) throws PhrescoException {
     	try {
     		if (StringUtils.isNotEmpty(appInfo.getFunctionalFramework())) {
     			FunctionalFramework functionalFramework = getServiceManager().getFunctionalTestFramework(appInfo.getFunctionalFramework(), appInfo.getTechInfo().getId());
     			List<FunctionalFrameworkProperties> funcFrameworkProperties = functionalFramework.getFuncFrameworkProperties();
     			if (CollectionUtils.isNotEmpty(funcFrameworkProperties)) {
     				FunctionalFrameworkProperties frameworkProperties = funcFrameworkProperties.get(0);
     				String testDir = frameworkProperties.getTestDir();
     				String testReportDir = frameworkProperties.getTestReportDir();
     				String testcasePath = frameworkProperties.getTestcasePath();
     				String testsuiteXpathPath = frameworkProperties.getTestsuiteXpathPath();
     				String adaptConfigPath = frameworkProperties.getAdaptConfigPath();
 
     				FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
     				PomProcessor pomProcessor = frameworkUtil.getPomProcessor(appInfo.getAppDirName());
     				pomProcessor.setProperty(POM_PROP_KEY_FUNCTEST_SELENIUM_TOOL, appInfo.getFunctionalFramework());
     				pomProcessor.setProperty(POM_PROP_KEY_FUNCTEST_DIR, testDir);
     				pomProcessor.setProperty(POM_PROP_KEY_FUNCTEST_RPT_DIR, testReportDir);
     				pomProcessor.setProperty(POM_PROP_KEY_FUNCTEST_TESTCASE_PATH, testcasePath);
     				pomProcessor.setProperty(POM_PROP_KEY_FUNCTEST_TESTSUITE_XPATH, testsuiteXpathPath);
     				pomProcessor.setProperty(PHRESCO_FUNCTIONAL_TEST_ADAPT_DIR, adaptConfigPath);
     				pomProcessor.save();
     			}
     		}
 		} catch (PhrescoException e) {
 			throw e;
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	public void checkForVersions(String newArtifactid, String oldArtifactGroupId, ApplicationInfo appInfo) throws PhrescoException {
     	try {
     		FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
     		File sqlPath = new File(Utility.getProjectHome() + File.separator + appInfo.getAppDirName() + frameworkUtil.getSqlFilePath(appInfo.getAppDirName()));
 			DownloadInfo oldDownloadInfo = getServiceManager().getDownloadInfo(oldArtifactGroupId);
 			DownloadInfo newDownloadInfo = getServiceManager().getDownloadInfo(newArtifactid);
 			List<ArtifactInfo> oldVersions = oldDownloadInfo.getArtifactGroup().getVersions();
 			List<ArtifactInfo> newVersions = newDownloadInfo.getArtifactGroup().getVersions();
 			for (ArtifactInfo artifactInfo : oldVersions) {
 				for (ArtifactInfo newartifactInfo : newVersions) {
 					if(!newartifactInfo.getVersion().equals(artifactInfo.getVersion())) {
 						String deleteVersion = "/" + oldDownloadInfo.getName() + "/" + artifactInfo.getVersion();
 						FileUtils.deleteDirectory(new File(sqlPath, deleteVersion));
 					}
 				}
 			}
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
     }
 
 	public void deleteSqlFolder(ApplicationInfo applicationInfo, List<ArtifactGroupInfo> selectedDatabases)
 			throws PhrescoException {
 		try {
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			List<String> dbListToDelete = new ArrayList<String>();
 			List<ArtifactGroupInfo> existingDBList = applicationInfo.getSelectedDatabases();
 			if (CollectionUtils.isEmpty(existingDBList)) {
 				return;
 			}
 			for (ArtifactGroupInfo artifactGroupInfo : existingDBList) {
 				String oldArtifactGroupId = artifactGroupInfo.getArtifactGroupId();
 				for (ArtifactGroupInfo newArtifactGroupInfo : selectedDatabases) {
 					String newArtifactid = newArtifactGroupInfo.getArtifactGroupId();
 					if (newArtifactid.equals(oldArtifactGroupId)) {
 						checkForVersions(newArtifactid, oldArtifactGroupId, applicationInfo);
 						break;
 					} else {
 						DownloadInfo downloadInfo = getServiceManager().getDownloadInfo(oldArtifactGroupId);
 							dbListToDelete.add(downloadInfo.getName());
 					}
 				}
 			}
 			File sqlPath = null;
 			if (StringUtils.isNotEmpty(applicationInfo.getAppDirName())) {
 				sqlPath = new File(Utility.getProjectHome() + File.separator + applicationInfo.getAppDirName()
 					+ frameworkUtil.getSqlFilePath(applicationInfo.getAppDirName()));
 			} else {
 				sqlPath = new File(Utility.getProjectHome() + File.separator + applicationInfo.getAppDirName()
 						+ frameworkUtil.getSqlFilePath(applicationInfo.getAppDirName()));
 			}
 				for (String dbVersion : dbListToDelete) {
 					File dbVersionFolder = new File(sqlPath, dbVersion.toLowerCase());
 					FileUtils.deleteDirectory(dbVersionFolder.getParentFile());
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	public String importSVNApplication() {
 		if(s_debugEnabled){
 			S_LOGGER.debug("Entering Method  Applications.importSVNApplication()");
 		}
 		try {
 			revision = !HEAD_REVISION.equals(revision) ? revisionVal : revision;
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			String authForAppln = checkAuthentication(repoUrl, userName, password);
 			
 			if (!SUCCESS.equalsIgnoreCase(authForAppln)) {
 				errorString = "Application Repository Url/Credentials does not match!!!";
 				errorFlag = false;
 				return SUCCESS;
 			}
 			if (testClone) {
 				String authForTest = checkAuthentication(testRepoUrl, testUserName, testPassword);
 
 				if (!SUCCESS.equalsIgnoreCase(authForTest)) {
 					errorString = "TestCheckout Repository Url/Credentials does not match!!!";
 					errorFlag = false;
 					return SUCCESS;
 				}
 			}
 			
 			ApplicationInfo importProject = scmi.importProject(SVN, repoUrl, userName, password, null, revision);
 			if (importProject != null) {
 				String importTest = "";
 				if (testClone) {
 					String path = Utility.getProjectHome() + File.separator + importProject.getAppDirName() + File.separator;
 					File testFolder = new File(path, TEST);
 					if (!testFolder.exists()) {
 						testFolder.mkdirs();
 					}
 					FileUtils.cleanDirectory(testFolder);
 					importTest = scmi.svnCheckout(testUserName, testPassword, testRepoUrl, testFolder.getPath());
 					if (SUCCESSFUL.equalsIgnoreCase(importTest)) {
 						errorString = getText(IMPORT_SUCCESS_PROJECT);
 						errorFlag = true;
 					} else {
 						errorString = importTest;
 						errorFlag = false;
 					}
 				} else {
 					errorString = getText(IMPORT_SUCCESS_PROJECT);
 					errorFlag = true;
 				}
 			} else {
 				errorString = getText(INVALID_FOLDER);
 				errorFlag = false;
 			}
 		} catch (SVNAuthenticationException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			errorFlag = false;
 			errorString = getText(APPLN_INVALID_CREDENTIALS);
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
 			errorString = getText(e.getLocalizedMessage());
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
 			ApplicationInfo importProject = scmi.importProject(GIT, repoUrl, userName, password, MASTER ,revision);
 			if (importProject != null) {
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
 	
 	public String importBitKeeperApplication() {
         if(s_debugEnabled){
             S_LOGGER.debug("Entering Method  Applications.importBitkeeperApplication()");
         }
         
         try {
             SCMManagerImpl scmi = new SCMManagerImpl();
             ApplicationInfo importProject = scmi.importProject(BITKEEPER, repoUrl, userName, password, null , null);
             if (importProject != null) {
                 errorString = getText(IMPORT_SUCCESS_PROJECT);
                 errorFlag = true;
             }
         } catch (Exception e) {
             if (s_debugEnabled) {
                 S_LOGGER.error(e.getLocalizedMessage());
             }
             if ("Project already imported".equals(e.getLocalizedMessage())) {
                 errorString = getText(e.getLocalizedMessage());
             } else  if ("Failed to import project".equals(e.getLocalizedMessage())) {
                 errorString = getText(e.getLocalizedMessage());
             } else {
                 errorString = getText(IMPORT_PROJECT_FAIL);
                 errorFlag = false;
             }
         }
         
         return SUCCESS;
 	}
 
 	public String importAppln() {
 		try {
 			setReqAttribute(REQ_ACTION, action);
 		} catch (Exception e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			return showErrorPopup(new PhrescoException(e), "Import Application");
 		}
 		return APP_IMPORT;
 	}
 
 	public String repoExistCheckForCommit() throws PhrescoException {
 		if(getConnectionUrl().startsWith("bk")) {
 			setRepoExistForCommit(true);
 		} else {	
 			checkGitProject();
 			if(getConnectionUrl().endsWith(".git")) {
 				updateProjectPopup();
 			}
 			if (!isRepoExistForCommit) {
 				updateProjectPopup();
 			}
 		}
 		return SUCCESS;
 	}
 
 	private void checkGitProject() throws PhrescoException {
 		setRepoExistForCommit(true);
 		String url = "";
 		String Path = "";
 		ApplicationInfo applicationInfo = getApplicationInfo();
 		if (applicationInfo != null) {
 			String appDirName = applicationInfo.getAppDirName();
 			Path = Utility.getProjectHome() + appDirName;
 		}
 		File projectPath = new File(Path);
 		InitCommand initCommand = Git.init();
 		initCommand.setDirectory(projectPath);
 		Git git = null;
 		try {
 			git = initCommand.call();
 		} catch (GitAPIException e) {
 			throw new PhrescoException(e);
 		}
 		
 		Config storedConfig = git.getRepository().getConfig();
 		url = storedConfig.getString(REMOTE, ORIGIN, URL);
 		if (StringUtils.isEmpty(url)) {
 			File toDelete = git.getRepository().getDirectory();
 			try {
 				FileUtils.deleteDirectory(toDelete);
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 			setRepoExistForCommit(false);
 		}
 		git.getRepository().close();
 	}
 
 	private String getConnectionUrl() {
 		try {
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			setReqAttribute(REQ_APP_INFO, applicationInfo);
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			PomProcessor processor = frameworkUtil.getPomProcessor(applicationInfo.getAppDirName());
 			Scm scm = processor.getSCM();
 			if (scm != null && !scm.getConnection().isEmpty()) {
 					return scm.getConnection();
 			}
 		} catch (PhrescoException e) {
 			if(s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 		}
 
 		return "";
 	}
 	
 	public String repoExistCheckForUpdate() {
 		isRepoExistForUpdate = true;
 		if(getConnectionUrl().isEmpty()) {
 			setRepoExistForUpdate(false);
 		}
 		
 		return SUCCESS;
 	}
 
 	public String updateProjectPopup() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Applications.updateProjectPopup()");
 		}
 		try {
 			setRepoExistForCommit(true);
 			List<SVNStatus> commitableFiles = null;
 			String repo = "";
 			commitableGITFiles = new Properties();
 			//getting commitable files for SVN repo
 			if (COMMIT.equals(action)
 					&& !getConnectionUrl().contains(BITKEEPER)
 					&& !getConnectionUrl().contains(GIT)) {
 				commitableFiles = svnCommitableFiles();
 				repo = SVN;
 			//getting commitable files for Git repo
 			} else if (COMMIT.equals(action)
 					&& !getConnectionUrl().contains(BITKEEPER)
 					&& !getConnectionUrl().contains(SVN)) {
 				commitableGITFiles = gitCommitableFiles();
 				repo = GIT;
 			}
 			setReqAttribute(REPO, repo);
 			setReqAttribute(REQ_COMMITABLE_FILES, commitableFiles);
 			if (repo.equalsIgnoreCase(GIT)) {
 				setReqAttribute(REQ_GIT_COMMITABLE_FILES, commitableGITFiles);
 			}
 			setReqAttribute(REQ_APP_ID, getAppId());
 			setReqAttribute(REQ_PROJECT_ID, getProjectId());
 			setReqAttribute(REQ_CUSTOMER_ID, getCustomerId());
 			setReqAttribute(REPO_URL, getConnectionUrl());
 			setReqAttribute(REQ_FROM_TAB, UPDATE);
 			setReqAttribute(REQ_ACTION, action);
 		} catch (PhrescoException e) {
 			if (s_debugEnabled){
 				S_LOGGER.error(e.getLocalizedMessage());
 			}
 			if (e.getLocalizedMessage().contains(IS_NOT_WORKING_COPY)) {
 				setRepoExistForCommit(false);
 			}
 			return showErrorPopup(e, "Update Application");
 		}
 
 		return APP_UPDATE;
 	}
 
 	public String updateGitProject() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Applications.updateGitProject()");
 		}
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		try {
 			//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(getLockDetail(getApplicationInfo().getId(), UPDATE)), true);
 			
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			scmi.updateProject(GIT, repoUrl, userName, password, MASTER , null, applicationInfo);
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
 			errorString = getText(UPDATE_PROJECT_FAIL);
 			errorFlag = false;
 		} finally {
 			try {
 				removeLock();
 			} catch (PhrescoException e) {
 				
 			}
 		}
 
 		return SUCCESS;
 	}
 
 	public String updateSVNProject() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Applications.updateGitProject()");
 		}
 		SCMManagerImpl scmi = new SCMManagerImpl();
 		revision = !HEAD_REVISION.equals(revision) ? revisionVal : revision;
 		try {
 			//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(getLockDetail(getApplicationInfo().getId(), UPDATE)), true);
 			
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			scmi.updateProject(SVN, repoUrl, userName, password, null, revision, applicationInfo);
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
 			} else if (e.getMessage().indexOf(SVN_IS_NOT_WORKING_COPY) != -1) {
 				errorString = getText(NOT_WORKING_COPY);
 			} else {
 				errorString = getText(UPDATE_PROJECT_FAIL);
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
 			errorString = getText(UPDATE_PROJECT_FAIL);
 			errorFlag = false;
 		} finally {
 			try {
 				removeLock();
 			} catch (PhrescoException e) {
 				
 			}
 		}
 		return SUCCESS;
 	}
 	
 	public String updateBitKeeperProject() {
 	    SCMManagerImpl scmi = new SCMManagerImpl();
         try {
         	//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(getLockDetail(getApplicationInfo().getId(), UPDATE)), true);
 			
             ApplicationInfo applicationInfo = getApplicationInfo();
             scmi.updateProject(BITKEEPER, getRepoUrl(), getUsername(), getPassword(), null, getRevision(), applicationInfo);
             errorString = getText(SUCCESS_PROJECT_UPDATE);
             errorFlag = true;
         } catch (PhrescoException e) {
             if (e.getLocalizedMessage().contains("Nothing to pull")) {
                 errorString = "No Files to update";
             } else {
                 errorString = getText(UPDATE_PROJECT_FAIL);
             }
             errorFlag = false;
         } catch (Exception e) {
             errorString = getText(UPDATE_PROJECT_FAIL);
             errorFlag = false;
         } finally {
 			try {
 				removeLock();
 			} catch (PhrescoException e) {
 
 			}
 		}
         
         return SUCCESS;
 	}
 
 	public String addSVNProject() {
 		if(s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Applications.addSVNProject()");
 		}
 		try {
 			//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(getLockDetail(getApplicationInfo().getId(), ADD_TO_REPO)), true);
 			
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			scmi.importToRepo(SVN, repoUrl, userName, password, null, null, getApplicationInfo(), commitMessage);
 			errorString = getText(ADD_PROJECT_SUCCESS);
 			errorFlag = true;
 			updateLatestProject();
 		} catch (Exception e) {
 			errorString = e.getLocalizedMessage();
 			errorFlag = false;
 		} finally {
 			try {
 				removeLock();
 			} catch (PhrescoException e) {
 
 			}
 		}
 		return SUCCESS;
 	}
 
 	public String addGitProject() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Applications.addGITProject()");
 		}
 		try {
 			//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(getLockDetail(getApplicationInfo().getId(), ADD_TO_REPO)), true);
 			
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			scmi.importToRepo(GIT, repoUrl, userName, password, null, null, getApplicationInfo(), commitMessage);
 			
 			errorString = getText(ADD_PROJECT_SUCCESS);
 			errorFlag = true;
 			updateLatestProject();
 		} catch (Exception e) {
 			if(e.getLocalizedMessage().contains("git-receive-pack not found")) {
 				errorString = "this Git repository does not exist";
 				errorFlag = false;
 			} else {
 				errorString = e.getLocalizedMessage();
 				errorFlag = false;
 			}
 		} finally {
 			try {
 				removeLock();
 			} catch (PhrescoException e) {
 
 			}
 		}
 		return SUCCESS;
 	}
 	
 	public List<SVNStatus> svnCommitableFiles() throws PhrescoException {
 		if(s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Applications.getCommitableFiles()");
 		}
 		List<SVNStatus> commitableFiles = null;
 		try {
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			String applicationHome = getApplicationHome();
 			File appDir = new File(applicationHome);
 			revision = HEAD_REVISION;
 			commitableFiles = scmi.getCommitableFiles(appDir, revision);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return commitableFiles;
 	}
 
 	public Properties gitCommitableFiles() throws PhrescoException {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Applications.getCommitableFiles()");
 		}
 		Properties prop = new Properties();
 		try {
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			String applicationHome = getApplicationHome();
 			File appDir = new File(applicationHome);
 			prop = scmi.getGITCommitableFiles(appDir);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return prop;
 	}
 
 	public String commitSVNProject() {
 		if(s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Applications.commitSVNProject()");
 		}
 		try {
 			if (CollectionUtils.isNotEmpty(commitableFiles)) {
 				List<File> listModifiedFiles = new ArrayList<File>(commitableFiles.size());
 				for (String commitableFile : commitableFiles) {
 					listModifiedFiles.add(new File(commitableFile));
 				}
 				
 				//To generate the lock for the particular operation
 				FrameworkUtil.generateLock(Collections.singletonList(getLockDetail(getApplicationInfo().getId(), COMMIT)), true);
 				
 				SCMManagerImpl scmi = new SCMManagerImpl();
 				String applicationHome = getApplicationHome();
 				File appDir = new File(applicationHome);
 //				scmi.commitToRepo(SVN, repoUrl, userName, password,  null, null, appDir, commitMessage);
 				scmi.commitSpecifiedFiles(listModifiedFiles, userName, password, commitMessage);
 			}
 			errorString = getText(COMMIT_PROJECT_SUCCESS);
 			errorFlag = true;
 		} catch (Exception e) {
 			errorString = e.getLocalizedMessage();
 			errorFlag = false;
 		} finally {
 			try {
 				removeLock();
 			} catch (PhrescoException e) {
 
 			}
 		}
 		return SUCCESS;
 	}
 
 	public String commitGitProject() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Applications.commitGITProject()");
 		}
 		try {
 			//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(getLockDetail(getApplicationInfo().getId(), COMMIT)), true);
 			
 			if (!commitableFiles.isEmpty()) {
 				SCMManagerImpl scmi = new SCMManagerImpl();
 				String applicationHome = getApplicationHome();
 				File appDir = new File(applicationHome);
 				scmi.commitToRepo(repoType, repoUrl, userName, password, null,
 						null, appDir, commitMessage);
 			}
 			errorString = getText(COMMIT_PROJECT_SUCCESS);
 			errorFlag = true;
 		} catch (Exception e) {
 			errorString = e.getLocalizedMessage();
 			errorFlag = false;
 		} finally {
 			try {
 				removeLock();
 			} catch (PhrescoException e) {
 
 			}
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * To commit the changes to the bitkeeper repo
 	 * @return 
 	 * @throws PhrescoException 
 	 */
 	public String commitBitKeeperProject() {
 	    if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.commitBitKeeperProject()");
         }
 	    try {
 	    	//To generate the lock for the particular operation
 			FrameworkUtil.generateLock(Collections.singletonList(getLockDetail(getApplicationInfo().getId(), COMMIT)), true);
 	    	
 	        SCMManagerImpl scmi = new SCMManagerImpl();
             File appDir = new File(getApplicationHome());
             scmi.commitToRepo(BITKEEPER, getRepoUrl(), getUsername(), getPassword(),  null, null, appDir, getCommitMessage());
             errorString = getText(COMMIT_PROJECT_SUCCESS);
             errorFlag = true;
         } catch (PhrescoException e) {
             if (e.getLocalizedMessage().contains("Nothing to push")) {
                 errorString = "No Files to commit";
             } else {
                 errorString = getText(COMMIT_PROJECT_FAIL);
             }
             errorFlag = false;
         } catch (Exception e) {
             errorString = getText(COMMIT_PROJECT_FAIL);
             errorFlag = false;
         } finally {
 			try {
 				removeLock();
 			} catch (PhrescoException e) {
 
 			}
 		}
         
         return SUCCESS;
 	}
 	public String fetchLogMessages() throws PhrescoException {
 		try {
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			List<String> svnLogMessages = scmi.getSvnLogMessages(getRepoUrl(), getUsername(), getPassword());
 			restrictedLogs = restrictLogs(svnLogMessages);
 		} catch (PhrescoException e) {
 			if (e.getLocalizedMessage().contains("Authorization Realm")) {
 				setLogMessage("Invalid Credentials");
 			} else if (e.getLocalizedMessage().contains("OPTIONS request failed on") || 
 					(e.getLocalizedMessage().contains("PROPFIND") && e.getLocalizedMessage().contains("405 Method Not Allowed")) 
 					|| e.getLocalizedMessage().contains("Repository moved temporarily to") ) {
 				setLogMessage("Invalid Url or Repository moved temproarily!!!");
 			} else {
 				setLogMessage(e.getLocalizedMessage());
 			}
 		}
 		return SUCCESS;
 	}
 	
 	private List<String> restrictLogs(List<String> svnLogMessages) {
 		List<String> Messages = new ArrayList<String>();
 		if (svnLogMessages.size() > 5) {
 			for(int i = svnLogMessages.size()-5; i<= svnLogMessages.size()-1; i++) {
 				Messages.add(svnLogMessages.get(i));
 			} 
 		} else {
 			for(int i = 0; i<= svnLogMessages.size()-1; i++) {
 				Messages.add(svnLogMessages.get(i));
 			} 
 		}
 		return Messages;
 	}
 	
 	private String checkAuthentication(String repoUrl, String userName, String passsword) {
 		try {
 			String authString = userName + COLON + passsword;
 			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
 			String authStringEnc = new String(authEncBytes);
 
 			URL url = new URL(repoUrl);
 			URLConnection urlConnection = url.openConnection();
 			urlConnection.setRequestProperty(AUTHORIZATION, BASIC_SPACE + authStringEnc);
 			InputStream is = urlConnection.getInputStream();
 			InputStreamReader isr = new InputStreamReader(is);
 
 			int numCharsRead;
 			char[] charArray = new char[1024];
 			StringBuffer sb = new StringBuffer();
 			while ((numCharsRead = isr.read(charArray)) > 0) {
 				sb.append(charArray, 0, numCharsRead);
 			}
 		
 		} catch (Exception e) {
 			return e.getLocalizedMessage();
 		}
 		return SUCCESS;
 	}
 	
 	/**
 	 * To remove the lock once the initiated operation has been completed
 	 * @throws PhrescoException
 	 */
 	public void removeLock() throws PhrescoException {
 		try {
 			List<LockDetail> lockDetails = FrameworkUtil.getLockDetails();
 			if (CollectionUtils.isNotEmpty(lockDetails)) {
 				List<LockDetail> availableLockDetails = new ArrayList<LockDetail>();
 				for (LockDetail lockDetail : lockDetails) {
 					if (!lockDetail.getActionType().equalsIgnoreCase(getActionType()) && lockDetail.getAppId().equals(getAppId())) {
 						availableLockDetails.add(lockDetail);
 					}
 				}
 				FrameworkUtil.generateLock(availableLockDetails, false);
 			}
 		} catch (PhrescoException e) {
 			throw e;
 		}
 	}
 	
 	/**
 	 * To check whether lock exists for the current process
 	 * @return
 	 */
 	public String checkForLock() {
 		try {
 			List<LockDetail> lockDetails = FrameworkUtil.getLockDetails();
 			if (CollectionUtils.isNotEmpty(lockDetails)) {
 				List<String> actionTypesToCheck = new ArrayList<String>();
 				if (getActionType().equals(REQ_CODE)) {
 					actionTypesToCheck.add(ADD_TO_REPO);
 					actionTypesToCheck.add(COMMIT);
 					actionTypesToCheck.add(UPDATE);
 					actionTypesToCheck.add(BUILD);
 					actionTypesToCheck.add(REQ_START);
 					actionTypesToCheck.add(UNIT);
 				} else if (getActionType().equals(BUILD)) {
 					actionTypesToCheck.add(ADD_TO_REPO);
 					actionTypesToCheck.add(COMMIT);
 					actionTypesToCheck.add(UPDATE);
 					actionTypesToCheck.add(BUILD);
 					actionTypesToCheck.add(REQ_CODE);
 					actionTypesToCheck.add(REQ_START);
 				} else if (getActionType().equals(REQ_START)) {
 					actionTypesToCheck.add(BUILD);
 					actionTypesToCheck.add(REQ_CODE);
 					actionTypesToCheck.add(ADD_TO_REPO);
 					actionTypesToCheck.add(COMMIT);
 					actionTypesToCheck.add(UPDATE);
 				} else if (getActionType().equals(UNIT)) {
 					actionTypesToCheck.add(ADD_TO_REPO);
 					actionTypesToCheck.add(COMMIT);
 					actionTypesToCheck.add(UPDATE);
 					actionTypesToCheck.add(BUILD);
 					actionTypesToCheck.add(REQ_CODE);
 					actionTypesToCheck.add(REQ_START);
 				} else if (getActionType().equals(REQ_FROM_TAB_DEPLOY)) {
 					actionTypesToCheck.add(BUILD);
 					actionTypesToCheck.add(REQ_FROM_TAB_DEPLOY);
 				} else if (getActionType().equals(ADD_TO_REPO)) {
 					actionTypesToCheck.add(ADD_TO_REPO);
 					actionTypesToCheck.add(UPDATE);
 					actionTypesToCheck.add(COMMIT);
 					actionTypesToCheck.add(BUILD);
 					actionTypesToCheck.add(REQ_CODE);
 					actionTypesToCheck.add(UNIT);
 					actionTypesToCheck.add(REQ_START);
 				} else if (getActionType().equals(COMMIT)) {
 					actionTypesToCheck.add(ADD_TO_REPO);
 					actionTypesToCheck.add(UPDATE);
 					actionTypesToCheck.add(COMMIT);
 					actionTypesToCheck.add(BUILD);
 					actionTypesToCheck.add(REQ_CODE);
 					actionTypesToCheck.add(UNIT);
 					actionTypesToCheck.add(REQ_START);
 				} else if (getActionType().equals(UPDATE)) {
 					actionTypesToCheck.add(ADD_TO_REPO);
 					actionTypesToCheck.add(COMMIT);
 					actionTypesToCheck.add(UPDATE);
 					actionTypesToCheck.add(BUILD);
 					actionTypesToCheck.add(REQ_CODE);
 					actionTypesToCheck.add(UNIT);
 					actionTypesToCheck.add(REQ_START);
 				} else {
 					actionTypesToCheck.add(getActionType());
 				}
 				for (LockDetail lockDetail : lockDetails) {
 					if (lockDetail.getAppId().equals(getAppId()) && actionTypesToCheck.contains(lockDetail.getActionType())) {
 						setLocked(true);
 						setLockedBy(lockDetail.getUserName());
 						setLockedDate(lockDetail.getStartedDate().toString());
 						break;
 					}
 				}
 			}
 		} catch (Exception e) {
 			// TODO: handle exception
 		}
 
 		return SUCCESS;
 	}
 	
 	/**
 	 * To remove the reader from the session
 	 * @return
 	 */
     public String removeReaderFromSession() {
         removeSessionAttribute(getAppId() + getActionType());
         
         return SUCCESS;
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
 
     public String getRepoType() {
         return repoType;
     }
 
     public void setRepoType(String repoType) {
         this.repoType = repoType;
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
 
 	public void setCommitableFiles(List<String> commitableFiles) {
 		this.commitableFiles = commitableFiles;
 	}
 
     public void setActionType(String actionType) {
         this.actionType = actionType;
     }
 
     public String getActionType() {
         return actionType;
     }
 
 	public String getCustomerId() {
 		return customerId;
 	}
 
 	public void setCustomerId(String customerId) {
 		this.customerId = customerId;
 	}
 
 	public String getProjectId() {
 		return projectId;
 	}
 
 	public void setProjectId(String projectId) {
 		this.projectId = projectId;
 	}
 
 	public List<String> getRestrictedLogs() {
 		return restrictedLogs;
 	}
 
 	public void setRestrictedLogs(List<String> restrictedLogs) {
 		this.restrictedLogs = restrictedLogs;
 	}
 
 	public boolean isRepoExistForUpdate() {
 		return isRepoExistForUpdate;
 	}
 
 	public void setRepoExistForUpdate(boolean isRepoExistForUpdate) {
 		this.isRepoExistForUpdate = isRepoExistForUpdate;
 	}
 
 	public boolean isRepoExistForCommit() {
 		return isRepoExistForCommit;
 	}
 
 	public void setRepoExistForCommit(boolean isRepoExistForCommit) {
 		this.isRepoExistForCommit = isRepoExistForCommit;
 	}
 
 	public Properties getCommitableGITFiles() {
 		return commitableGITFiles;
 	}
 
 	public void setCommitableGITFiles(Properties commitableGITFiles) {
 		this.commitableGITFiles = commitableGITFiles;
 	}
 	public void setLogMessage(String logMessage) {
 		this.logMessage = logMessage;
 	}
 
 	public String getLogMessage() {
 		return logMessage;
 	}
 
 	public String getTestUserName() {
 		return testUserName;
 	}
 
 	public void setTestUserName(String testUserName) {
 		this.testUserName = testUserName;
 	}
 
 	public String getTestPassword() {
 		return testPassword;
 	}
 
 	public void setTestPassword(String testPassword) {
 		this.testPassword = testPassword;
 	}
 
 	public String getTestRevision() {
 		return testRevision;
 	}
 
 	public void setTestRevision(String testRevision) {
 		this.testRevision = testRevision;
 	}
 
 	public String getTestRevisionVal() {
 		return testRevisionVal;
 	}
 
 	public void setTestRevisionVal(String testRevisionVal) {
 		this.testRevisionVal = testRevisionVal;
 	}
 
 	public String getTestRepoUrl() {
 		return testRepoUrl;
 	}
 
 	public void setTestRepoUrl(String testRepoUrl) {
 		this.testRepoUrl = testRepoUrl;
 	}
 
 	public void setTestClone(boolean testClone) {
 		this.testClone = testClone;
 	}
 
 	public boolean getTestClone() {
 		return testClone;
 	}
 	
 	public String getPomVersion() {
 		return pomVersion;
 	}
 
 	public void setPomVersion(String pomVersion) {
 		this.pomVersion = pomVersion;
 	}
 
 	public void setLocked(boolean locked) {
 		this.locked = locked;
 	}
 
 	public boolean isLocked() {
 		return locked;
 	}
 
 	public void setLockedBy(String lockedBy) {
 		this.lockedBy = lockedBy;
 	}
 
 	public String getLockedBy() {
 		return lockedBy;
 	}
 
 	public void setLockedDate(String lockedDate) {
 		this.lockedDate = lockedDate;
 	}
 
 	public String getLockedDate() {
 		return lockedDate;
 	}
 
 	public int getSelectedServersCount() {
 		return selectedServersCount;
 	}
 
 	public void setSelectedServersCount(int selectedServersCount) {
 		this.selectedServersCount = selectedServersCount;
 	}
 
 	public int getSelectedDatabasesCount() {
 		return selectedDatabasesCount;
 	}
 
 	public void setSelectedDatabasesCount(int selectedDatabasesCount) {
 		this.selectedDatabasesCount = selectedDatabasesCount;
 	}
 }
