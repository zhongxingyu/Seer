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
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileExistsException;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.eclipse.jgit.api.errors.InvalidRemoteException;
 import org.eclipse.jgit.api.errors.TransportException;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
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
 import com.photon.phresco.commons.model.CoreOption;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.commons.model.DownloadInfo.Category;
 import com.photon.phresco.commons.model.Element;
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
 import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Scm;
 import com.phresco.pom.util.PomProcessor;
 
 public class Applications extends FrameworkBaseAction {
 
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
     private String commitMessage = "";
     private List<String> commitableFiles = null;
 
     private List<DownloadInfo> servers = null;
 
     private String technology = "";
     
     private String errorString;
     private boolean errorFlag;
 
     private SelectedFeature selectFeature;
     private String selectedDownloadInfo = "";
     private String downloadInfoType = "";
     private String selectedDownloadInfoVersion = "";
     private String selectBoxId = "";
     private String defaultOptTxt = "";
     private String action = "";
     private List<String> jsonData = null;
     private boolean isRepoExist;
     private String actionType = "";
     private String customerId = "";
     private String projectId = "";
     private String appId = "";
     
     public String loadMenu() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.loadMenu()");
         }
 
         try {
         	ApplicationInfo applicationInfo = getApplicationInfo();
 			String techId = applicationInfo.getTechInfo().getId();
 			Technology techInfo = getServiceManager().getArcheType(techId, getCustomerId());
 			List<String> optionIds = techInfo.getOptions();
 			
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
         		
         		technologyId = projectInfo.getAppInfos().get(0).getTechInfo().getId();
         		List<ApplicationInfo> pilotProjects = getServiceManager().getPilotProjects(getCustomerId(), technologyId);
         		Technology technologyInfo = getServiceManager().getTechnology(technologyId);
         		setSessionAttribute(REQ_TECHNOLOGY, technologyInfo);
         		setSessionAttribute(REQ_PILOT_PROJECTS, pilotProjects);
         		setSessionAttribute(getAppId() + SESSION_APPINFO, projectInfo);
         		setReqAttribute(REQ_OLD_APPDIR, projectInfo.getAppInfos().get(0).getName());
         	} else {
         		projectInfo = (ProjectInfo)getSessionAttribute(getAppId() + SESSION_APPINFO);
             	ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
             
             	List<String> allJsonData = getJsonData();
             	List<String> selectedFeatures = new ArrayList<String>();
             	List<String> selectedJsLibs = new ArrayList<String>();
             	List<String> selectedComponents = new ArrayList<String>();
             	if (CollectionUtils.isNotEmpty(allJsonData)) {
                 	for (String string : allJsonData) {
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
             String selectedBoxId = getReqParameter(REQ_CURRENT_SELECTBOX_ID);
             String defaultOption = getReqParameter(REQ_DEFAULT_OPTION);
             setDefaultOptTxt(defaultOption);
             setDownloadInfoType(type);
             setSelectedDownloadInfo(selectedDb);
             setSelectedDownloadInfoVersion(selectedDbVer);
             setSelectBoxId(selectedBoxId);
             List<DownloadInfo> selectedDownloadInfos = getServiceManager().getDownloads(getCustomerId(), techId, type, FrameworkUtil.findPlatform());
             if (CollectionUtils.isNotEmpty(selectedDownloadInfos)) {
             	Collections.sort(selectedDownloadInfos, sortdownloadInfoInAlphaOrder());
             }
 			setDownloadInfos(selectedDownloadInfos);
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
         	
         	List<String> allJsonData = getJsonData();
         	List<String> selectedFeatures = new ArrayList<String>();
         	List<String> selectedJsLibs = new ArrayList<String>();
         	List<String> selectedComponents = new ArrayList<String>();
         	List<ArtifactGroup> listArtifactGroup = new ArrayList<ArtifactGroup>();
         	List<DownloadInfo> selectedServerGroup = new ArrayList<DownloadInfo>();
         	List<DownloadInfo> selectedDatabaseGroup = new ArrayList<DownloadInfo>();
         	if (CollectionUtils.isNotEmpty(allJsonData)) {
             	for (String string : allJsonData) {
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
 			List<ArtifactGroup> removedModules = getRemovedModules(appInfo, allJsonData);
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
 					List<ArtifactInfo> dbVersionInfos = artifactGroupInfo.getVersions();
 					//for selected version infos from ui
 					List<ArtifactInfo> selectedDBVersionInfos = new ArrayList<ArtifactInfo>();
 					for (ArtifactInfo versionInfo : dbVersionInfos) {
 						String versionId = versionInfo.getId();
 						if (selectedDatabase.getArtifactInfoIds().contains(versionId)) {
 							//Add selected version infos to list
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
 			
 			//To write selected Servers into phresco-application-Handler-info.xml
 			List<ArtifactGroupInfo> selectedServers = appInfo.getSelectedServers();
 			if (CollectionUtils.isNotEmpty(selectedServers)) {
 				for (ArtifactGroupInfo selectedServer : selectedServers) {
 					DownloadInfo downloadInfo = getServiceManager().getDownloadInfo(selectedServer.getArtifactGroupId());
 					String id = downloadInfo.getArtifactGroup().getId();
 					ArtifactGroup artifactGroupInfo = getServiceManager().getArtifactGroupInfo(id);
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
 			
 			//To write selected WebServices info to phresco-plugin-info.xml
 			List<String> selectedWebservices = appInfo.getSelectedWebservices();
 			List<WebService> webServiceList = new ArrayList<WebService>();
 			if (CollectionUtils.isNotEmpty(selectedWebservices)) {
 				for (String selectedWebService : selectedWebservices) {
 					WebService webservice = getServiceManager().getWebService(selectedWebService);
 					webServiceList.add(webservice);
 				}
 				if (CollectionUtils.isNotEmpty(webServiceList)) {
 					String serverGroup = gson.toJson(webServiceList);
 					applicationHandler.setSelectedWebService(serverGroup);
 				}
 			} else {
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
             List<ProjectInfo> projects = projectManager.discover(getCustomerId());
             if (CollectionUtils.isNotEmpty(projects)) {
             	Collections.sort(projects, sortByNameInAlphaOrder());
             }
             setReqAttribute(REQ_PROJECTS, projects);
             removeSessionAttribute(getAppId() + SESSION_APPINFO);
             removeSessionAttribute(REQ_SELECTED_FEATURES);
     	} catch (PhrescoException e) {
     		return showErrorPopup(e, EXCEPTION_PROJECT_UPDATE);
     	} catch (FileNotFoundException e) {
     		return showErrorPopup(new PhrescoException(e), EXCEPTION_PROJECT_UPDATE);
     	} finally {
     		Utility.closeReader(bufferedReader);
     	}
     	return SUCCESS;
     }
     
     public void checkForVersions(String newArtifactid, String oldArtifactGroupId) throws PhrescoException {
     	try {
     		FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
     		File sqlPath = new File(Utility.getProjectHome() + File.separator + oldAppDirName + frameworkUtil.getSqlFilePath(oldAppDirName));
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
 						checkForVersions(newArtifactid, oldArtifactGroupId);
 						break;
 					} else {
 						DownloadInfo downloadInfo = getServiceManager().getDownloadInfo(oldArtifactGroupId);
 							dbListToDelete.add(downloadInfo.getName());
 					}
 				}
 			}
 			File sqlPath = null;
 			if (StringUtils.isNotEmpty(oldAppDirName)) {
 				sqlPath = new File(Utility.getProjectHome() + File.separator + oldAppDirName
 					+ frameworkUtil.getSqlFilePath(oldAppDirName));
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
             boolean importProject = scmi.importProject(BITKEEPER, repoUrl, userName, password, null , null);
             if (importProject) {
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
 	
 	public String repoExistCheck() {
 		updateProjectPopup();
 
 		return SUCCESS;
 	}
 
 	public String updateProjectPopup() {
 		S_LOGGER.debug("Entering Method  Applications.updateProjectPopup()");
 		try {
 			isRepoExist = true;
 			String connectionUrl = "";
 			ApplicationInfo applicationInfo = getApplicationInfo();
 			String appDirName = applicationInfo.getAppDirName();
 			FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
 			PomProcessor processor = frameworkUtil.getPomProcessor(appDirName);
 			Scm scm = processor.getSCM();
 			if (scm != null) {
 				connectionUrl = scm.getConnection();
 			}
 			
 			List<SVNStatus> commitFiles = null;
 			if (COMMIT.equals(action) && !connectionUrl.contains(BITKEEPER)) {
 				commitFiles = svnCommitableFiles();
 			}
 			setReqAttribute(REQ_COMMITABLE_FILES, commitFiles);
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
 			if (e.getLocalizedMessage().contains(IS_NOT_WORKING_COPY)) {
 				setRepoExist(false);
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
 			errorString = getText(UPDATE_PROJECT_FAIL);
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
 		}
 		return SUCCESS;
 	}
 	
 	public String updateBitKeeperProject() {
 	    SCMManagerImpl scmi = new SCMManagerImpl();
         try {
             ApplicationInfo applicationInfo = getApplicationInfo();
             String appDirName = applicationInfo.getAppDirName();
             scmi.updateProject(BITKEEPER, getRepoUrl(), getUsername(), getPassword(), null, getRevision(), appDirName);
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
         }
         
         return SUCCESS;
 	}
 
 	public String addSVNProject() {
 		if(s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Applications.addSVNProject()");
 		}
 		try {
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			String applicationHome = getApplicationHome();
 			File appDir = new File(applicationHome);
 			scmi.importToRepo(SVN, repoUrl, userName, password, null, null, appDir, commitMessage);
 			errorString = getText(ADD_PROJECT_SUCCESS);
 			errorFlag = true;
 			updateLatestProject();
 		} catch (Exception e) {
 			errorString = e.getLocalizedMessage();
 			errorFlag = false;
 		}
 		return SUCCESS;
 	}
 	
 	public String addGITProject() {
 		if(s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Applications.addGITProject()");
 		}
 		try {
 			// TODO : need to handle
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return SUCCESS;
 	}
 	
 	public List<SVNStatus> svnCommitableFiles() throws PhrescoException {
 		if(s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Applications.getCommitableFiles()");
 		}
 		List<SVNStatus> commitFiles = null;
 		try {
 			SCMManagerImpl scmi = new SCMManagerImpl();
 			String applicationHome = getApplicationHome();
 			File appDir = new File(applicationHome);
 			revision = HEAD_REVISION;
 			commitFiles = scmi.getCommitableFiles(appDir, revision);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 		return commitFiles;
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
 				SCMManagerImpl scmi = new SCMManagerImpl();
 				String applicationHome = getApplicationHome();
 				File appDir = new File(applicationHome);
 				scmi.commitSpecifiedFiles(listModifiedFiles, userName, password, commitMessage);
 			}
 			errorString = getText(COMMIT_PROJECT_SUCCESS);
 			errorFlag = true;
 		} catch (Exception e) {
 			errorString = e.getLocalizedMessage();
 			errorFlag = false;
 		}
 		return SUCCESS;
 	}
 	
 	/**
 	 * To commit the changes to the bitkeeper repo
 	 * @return 
 	 */
 	public String commitBitKeeperProject() {
 	    if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method  Applications.commitBitKeeperProject()");
         }
 	    try {
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
 
 	public boolean isRepoExist() {
 		return isRepoExist;
 	}
 
 	public void setRepoExist(boolean isRepoExist) {
 		this.isRepoExist = isRepoExist;
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
 
 	public String getAppId() {
 		return appId;
 	}
 
 	public void setAppId(String appId) {
 		this.appId = appId;
 	}
 }
