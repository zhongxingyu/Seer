 /*
  * ###
  * Phresco Framework Implementation
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
 package com.photon.phresco.framework.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Type;
 import java.security.KeyStore;
 import java.security.cert.CertificateException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLException;
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.TrustManagerFactory;
 import javax.net.ssl.X509TrustManager;
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ApplicationType;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.BuildInfo;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.LogInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.SettingsTemplate;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.commons.model.WebService;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.api.Project;
 import com.photon.phresco.framework.api.ProjectAdministrator;
 import com.photon.phresco.framework.api.ValidationResult;
 import com.photon.phresco.framework.model.CertificateInfo;
 import com.photon.phresco.framework.model.SettingsInfo;
 import com.photon.phresco.framework.win8.util.ItemGroupUpdater;
 import com.photon.phresco.service.client.api.ServiceClientConstant;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.ProjectUtils;
 import com.photon.phresco.util.ServiceConstants;
 import com.photon.phresco.util.TechnologyTypes;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.model.Model;
 import com.phresco.pom.site.ReportCategories;
 import com.phresco.pom.site.Reports;
 import com.phresco.pom.util.PomProcessor;
 import com.phresco.pom.util.SiteConfigurator;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.ClientResponse;
 
 public class ProjectAdministratorImpl implements ProjectAdministrator, FrameworkConstants, Constants, ServiceClientConstant, ServiceConstants {
 
 	private static final Logger S_LOGGER= Logger.getLogger(ProjectAdministratorImpl.class);
 	private Map<String, List<ArtifactGroup>> coreModulesMap = Collections.synchronizedMap(new HashMap<String, List<ArtifactGroup>>(8));
 	private Map<String, List<ArtifactGroup>> customModulesMap = Collections.synchronizedMap(new HashMap<String, List<ArtifactGroup>>(8));
 	private static Map<String, String> sqlFolderPathMap = new HashMap<String, String>();
 	private static  Map<String, List<Reports>> siteReportMap = new HashMap<String, List<Reports>>(15);
 	private boolean adaptFunctionalConfig = false;
 	
 	private static ServiceManager serviceManager = null;
     
     public ServiceManager getServiceManager() {
         return serviceManager;
     }
 	
 	private static void initializeSqlMap() {
 		// TODO: This should come from database
 		sqlFolderPathMap.put(TechnologyTypes.PHP, "/source/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.PHP_DRUPAL6, "/source/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.PHP_DRUPAL7, "/source/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.NODE_JS_WEBSERVICE, "/source/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET, "/src/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.HTML5_MOBILE_WIDGET, "/src/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.HTML5_WIDGET, "/src/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.JAVA_WEBSERVICE, "/src/sql/");
 		sqlFolderPathMap.put(TechnologyTypes.WORDPRESS, "/source/sql/");
 	}
 
 	/**
 	 * Creates a project based on the given project information
 	 *
 	 * @return Project based on the given information
 	 */
 	@Override
 	public Project createProject(ApplicationInfo info, File path) throws PhrescoException {
 
 		S_LOGGER.debug("Entering Method ProjectAdministratorImpl.createProject(ProjectInfo info, File path)");
 		S_LOGGER.debug("createProject() > info name : " + info.getName());
 
 		File projectPath = new File(Utility.getProjectHome()+ File.separator+ info.getCode());
 		String techId = info.getTechInfo().getVersion();
 		/*if (StringUtils.isEmpty(info.getVersion())) {
 			info.setVersion(PROJECT_VERSION); // TODO: Needs to be fixed
 		}*/
 		ProjectInfo projectInfo = null;
 		ClientResponse response = getServiceManager().createProject(projectInfo);
 		S_LOGGER.debug("createProject response code " + response.getStatus());
 
 		if (response.getStatus() == 200) {
 			try {
 				extractArchive(response, info);
 				updateProjectPOM(info);
 				if (TechnologyTypes.WIN_METRO.equalsIgnoreCase(techId)) {
 					ItemGroupUpdater.update(info, projectPath);
 				}
 			} catch (FileNotFoundException e) {
 				throw new PhrescoException(e); 
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		}  else if(response.getStatus() == 401){
 			throw new PhrescoException("Session expired");
 		}
 		else {
 			throw new PhrescoException("Project creation failed");
 		}
 		boolean flag1 = techId.equals(TechnologyTypes.JAVA_WEBSERVICE) || techId.equals(TechnologyTypes.JAVA_STANDALONE) || techId.equals(TechnologyTypes.HTML5_WIDGET) || 
 		techId.equals(TechnologyTypes.HTML5_MOBILE_WIDGET)|| techId.equals(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET);
 		if (flag1) {
 			File pomPath = new File(projectPath,POM_FILE);
 			ServerPluginUtil spUtil = new ServerPluginUtil();
 			spUtil.addServerPlugin(info, pomPath);
 		}
 		boolean drupal = techId.equals(TechnologyTypes.PHP_DRUPAL7) || techId.equals(TechnologyTypes.PHP_DRUPAL6);
 		if(drupal) {
 			updateDrupalVersion(projectPath, info);
 			excludeModule(info);
 		}
 
 		// Creating configuration file, after successfull creation of project
 		try {
 			createConfigurationXml(info.getCode());
 		} catch (Exception e) {
 			S_LOGGER.error("Entered into the catch block of Configuration creation failed Exception" + e.getLocalizedMessage());
 			throw new PhrescoException("Configuration creation failed");
 		}
 		return new ProjectImpl(info);
 	}
 
 	private void extractArchive(ClientResponse response, ApplicationInfo info) throws  IOException, PhrescoException {
 		InputStream inputStream = response.getEntityInputStream();
 		FileOutputStream fileOutputStream = null;
 		String archiveHome = Utility.getArchiveHome();
 		File archiveFile = new File(archiveHome + info.getCode() + ARCHIVE_FORMAT);
 		fileOutputStream = new FileOutputStream(archiveFile);
 		try {
 			byte[] data = new byte[1024];
 			int i = 0;
 			while ((i = inputStream.read(data)) != -1) {
 				fileOutputStream.write(data, 0, i);
 			}
 			fileOutputStream.flush();
 			ArchiveUtil.extractArchive(archiveFile.getPath(), Utility.getProjectHome() + info.getCode(), ArchiveType.ZIP);
 		} finally {
 			Utility.closeStream(inputStream);
 			Utility.closeStream(fileOutputStream);
 		}
 	}
 
 	/**
 	 * Updates a project based on the given project information
 	 *
 	 * @return Project based on the given information
 	 */
 	@Override
 	public Project updateProject(ApplicationInfo delta, ApplicationInfo appInfo, File path, User user) throws PhrescoException {
 
 		S_LOGGER.debug("Entering Method ProjectAdministratorImpl.updateProject(ProjectInfo info, File path)");
 		S_LOGGER.debug("updateProject() > info name : " + delta.getName());
 		/*if (StringUtils.isEmpty(delta.getVersion())) {
 			delta.setVersion(PROJECT_VERSION); // TODO: Needs to be fixed
 		}*/
 		
 		ClientResponse response = null;
 		String techId = delta.getTechInfo().getVersion();
 		if(techId.equals(TechnologyTypes.PHP_DRUPAL6)|| techId.equals(TechnologyTypes.PHP_DRUPAL7)) {
 			excludeModule(delta);
 		}
 		boolean flag = !techId.equals(TechnologyTypes.JAVA_WEBSERVICE) && !techId.equals(TechnologyTypes.JAVA_STANDALONE) && !techId.equals(TechnologyTypes.ANDROID_NATIVE);
 		updateDocument(delta, path);
 		ProjectInfo projectInfo = null;
 		response = getServiceManager().updateProject(projectInfo);
 		if(response.getStatus() == 401){
 			throw new PhrescoException("Session expired");
 		}
 		else if (flag) {
 			if (response.getStatus() != 200) {
 				throw new PhrescoException("Project updation failed");
 			}
 		} 
 		else if (techId.equals(TechnologyTypes.JAVA_WEBSERVICE)) {
 			createSqlFolder(delta, path);
 		}
 		updatePomProject(delta, appInfo);
 		try {
 			if (flag) {
 				extractArchive(response, delta);
 			}
 			File projectPath = new File(Utility.getProjectHome() + delta.getCode() + File.separator);
 			if (TechnologyTypes.WIN_METRO.equalsIgnoreCase(techId)) {
 				ItemGroupUpdater.update(appInfo, projectPath);
 			}
			ProjectUtils.updateProjectInfo(appInfo, path);
 			updateProjectPOM(appInfo);
 		} catch (FileNotFoundException e) {
 			throw new PhrescoException(e);
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 		return new ProjectImpl(delta);
 	}
 
 	/**
 	 * Update PDF document with the selected Modules
 	 * @param appInfo
 	 * @param path
 	 * @throws PhrescoException
 	 */
 	private void updateDocument(ApplicationInfo appInfo, File path) throws PhrescoException {
 		List<String> modules = appInfo.getSelectedModules();
 		List<String> jsLibraries = appInfo.getSelectedJSLibs();
 		if(modules != null || jsLibraries != null) {
 			ApplicationInfo selectecdModule = selectecdModule(appInfo, path);
 			ClientResponse updateDocumentResponse = getServiceManager().updateDocumentProject(selectecdModule);
 			if (updateDocumentResponse.getStatus() != 200) {
 				throw new PhrescoException("Project updation failed");
 			}
 			try {
 				extractArchive(updateDocumentResponse, appInfo);
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		}
 	}
 
 	/**
 	 * Update dependency as selected Module in the pom file
 	 * @param delta
 	 * @param projectInfo
 	 * @param appInfoClone
 	 * @throws PhrescoException
 	 */
 	private void updatePomProject(ApplicationInfo delta, ApplicationInfo appInfoClone) throws PhrescoException {
 		String techId = delta.getTechInfo().getVersion();
 		File path = new File(Utility.getProjectHome() + File.separator + delta.getCode() + File.separator + POM_FILE);
 		boolean flag1 = techId.equals(TechnologyTypes.JAVA_WEBSERVICE) || techId.equals(TechnologyTypes.JAVA_STANDALONE) || techId.equals(TechnologyTypes.HTML5_WIDGET) || 
 		techId.equals(TechnologyTypes.HTML5_MOBILE_WIDGET)|| techId.equals(TechnologyTypes.HTML5_MULTICHANNEL_JQUERY_WIDGET) || techId.equals(TechnologyTypes.ANDROID_NATIVE)||
 		techId.equals(TechnologyTypes.ANDROID_HYBRID);
 		if (flag1) {
 			try {
 				ServerPluginUtil spUtil = new ServerPluginUtil();
 				spUtil.deletePluginFromPom(path);
 				spUtil.addServerPlugin(delta, path);
 				updatePomProject(appInfoClone);
 			} catch (Exception e) {
 				throw new PhrescoException(e);
 			}
 		} 
 	}
 
 	/**
 	 * update the projectinfo for all selected Module 
 	 * @param appInfo
 	 * @param path
 	 * @return
 	 * @throws PhrescoException
 	 */
 	private ApplicationInfo selectecdModule(ApplicationInfo appInfo, File path) throws PhrescoException {
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(path));
 			Gson gson = new Gson();
 			ApplicationInfo info = gson.fromJson(reader, ApplicationInfo.class);
 			List<String> appInfoModules = info.getSelectedModules();
 			List<String> appInfoJSLibs = info.getSelectedJSLibs();
 
 			List<String> selectedInfoModules = appInfo.getSelectedModules();
 			List<String> selectedInfoJSLibs = appInfo.getSelectedJSLibs();
 
 			if (appInfoModules != null && !appInfoModules.isEmpty() && selectedInfoModules != null) {
 				selectedInfoModules.addAll(appInfoModules);
 				appInfo.setSelectedModules(selectedInfoModules);
 			}
 			if (appInfoJSLibs != null && !appInfoJSLibs.isEmpty() && selectedInfoJSLibs != null) {
 				selectedInfoJSLibs.addAll(appInfoJSLibs);
 				appInfo.setSelectedJSLibs(selectedInfoJSLibs);
 			}
 			if (selectedInfoModules == null && appInfoModules != null && !appInfoModules.isEmpty()) {
 			    appInfo.setSelectedModules(appInfoModules);
 			}
 			if(selectedInfoJSLibs == null && appInfoJSLibs != null && !appInfoJSLibs.isEmpty() ) {
 				appInfo.setSelectedJSLibs(appInfoJSLibs);
 			}
 		} catch (Exception e) {
 			throw  new PhrescoException(e);
 		} finally {
 			Utility.closeStream(reader);
 		}
 		return appInfo;
 	}
 
 	/**
 	 * Exclude the Code Validation for core Module 
 	 * @param info
 	 * @throws PhrescoException
 	 */
 	private void excludeModule(ApplicationInfo info) throws PhrescoException {
 		try {
 			File projectPath = new File(Utility.getProjectHome()+ File.separator + info.getCode() + File.separator + POM_FILE);
 			PomProcessor processor = new PomProcessor(projectPath);
 			StringBuilder exclusionStringBuff = new StringBuilder();
 			StringBuilder exclusionValueBuff = new StringBuilder();
 			List<String> modules = info.getSelectedModules();
 			if (CollectionUtils.isEmpty(modules)) {
 				return;
 			}
 			//TODO: noee to handle the way of getting the modules
 			/*for (ModuleGroup moduleGroup : modules) {
 				if (moduleGroup.isCore()) {
 					exclusionValueBuff.append(moduleGroup.getName().toLowerCase());
 					exclusionValueBuff.append(",");
 					exclusionStringBuff.append("**\\");
 					exclusionStringBuff.append(moduleGroup.getName().toLowerCase());
 					exclusionStringBuff.append("\\**");
 					exclusionStringBuff.append(",");
 				}
 			}*/
 			String exclusionValue = exclusionStringBuff.toString();
 			if (exclusionValue.lastIndexOf(',') != -1) {
 				exclusionValue = exclusionValue.substring(0, exclusionValue.lastIndexOf(','));
 			}
 			
 			String exclusiontoolValue = exclusionValueBuff.toString();
 			if (exclusiontoolValue.lastIndexOf(',') != -1) {
 				exclusiontoolValue = exclusiontoolValue.substring(0, exclusiontoolValue.lastIndexOf(','));
 			}
 			
 			String pomExclusionValue = processor.getProperty(FrameworkConstants.SONAR_EXCLUSION);
 			if (!pomExclusionValue.equals("")) {
 				processor.setProperty(FrameworkConstants.SONAR_EXCLUSION, pomExclusionValue + FrameworkConstants.COMMA + exclusionValue);
 			} else if(pomExclusionValue.equals("")) {
 				processor.setProperty(FrameworkConstants.SONAR_EXCLUSION, exclusionValue);
 			}
 			
 			String pdependExcludeValue = processor.getProperty(FrameworkConstants.SONAR_PHPPDEPEND_ARGUMENTLINE);
 			if (!pdependExcludeValue.equals("")) {
 				processor.setProperty(FrameworkConstants.SONAR_PHPPDEPEND_ARGUMENTLINE, pdependExcludeValue + FrameworkConstants.COMMA + exclusiontoolValue);
 			} else 	if (pdependExcludeValue.equals("")) {
 				processor.setProperty(FrameworkConstants.SONAR_PHPPDEPEND_ARGUMENTLINE, FrameworkConstants.IGNORE + exclusiontoolValue);
 			}
 			
 			String pmdExcludeValue = processor.getProperty(FrameworkConstants.SONAR_PHPPMD_ARGUMENTLINE);
 			if (!pmdExcludeValue.equals("")) {
 				processor.setProperty(FrameworkConstants.SONAR_PHPPMD_ARGUMENTLINE, pmdExcludeValue + FrameworkConstants.COMMA + exclusiontoolValue);
 			} else 	if (pmdExcludeValue.equals("")) {
 				processor.setProperty(FrameworkConstants.SONAR_PHPPMD_ARGUMENTLINE, FrameworkConstants.EXCLUDE + exclusiontoolValue);
 			}
 			
 			processor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	/**
 	 * @param path
 	 * @param info
 	 * @throws PhrescoException 
 	 * @throws IOException
 	 * @throws JAXBException
 	 * @throws ParserConfigurationException
 	 */
 	private void updateDrupalVersion(File path, ApplicationInfo info) throws PhrescoException {
 		try {
 			File xmlFile = new File(path, POM_FILE);
 			PomProcessor processor = new PomProcessor(xmlFile);
 			String selectedVersion = info.getTechInfo().getVersion();
 			processor.setProperty(DRUPAL_VERSION, selectedVersion);
 			processor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	@Override
 	public Project getProject(String projectCode) throws PhrescoException {
 
 		S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getProject(String projectCode)");
 		if (StringUtils.isEmpty(projectCode)) {
 			S_LOGGER.debug("getProject() ProjectCode = "+projectCode);
 			throw new PhrescoException(FrameworkImplConstants.ERROR_PROJECT_CODE_EMPTY);
 		}
 
 		List<Project> projects = discover(Collections.singletonList(new File(Utility.getProjectHome())));
 		if (CollectionUtils.isEmpty(projects)) {
 			return null;
 		}
 
 		for (Project project : projects) {
 			if (project.getApplicationInfo().getCode().equalsIgnoreCase(projectCode)) {
 				return project;
 			}
 		}
 
 		return null;
 	}
 	
 	@Override
 	public Project getProjectByWorkspace(File baseDir) throws PhrescoException {
 
 		S_LOGGER.debug("Entering Method ProjectAdministratorImpl. getTechId(File baseDir)");
 
 		// Use the FileNameFilter for filtering .phresco directories
 		// Read the .project file and construct the Project object.
 
 		List<Project> projects = new ArrayList<Project>();
 
 		File[] dotPhrescoFolders = baseDir.listFiles(new PhrescoFileNameFilter(FOLDER_DOT_PHRESCO));
 		if (!ArrayUtils.isEmpty(dotPhrescoFolders)) {
 			for (File dotPhrescoFolder : dotPhrescoFolders) {
 				File[] dotProjectFiles = dotPhrescoFolder.listFiles(new PhrescoFileNameFilter(PROJECT_INFO_FILE));
 				fillProjects(dotProjectFiles, projects);
 			}
 			Project project = projects.get(0);
 			if (project != null && project.getApplicationInfo() != null) {
 				return project;
 			}
 		}
 		return null;
 	}
 
 	private static void updatePomProject(ApplicationInfo appInfo) throws PhrescoException {
 		File path = new File(Utility.getProjectHome() + File.separator + appInfo.getCode() + File.separator + POM_FILE);
 		try {
 			PomProcessor pomProcessor = new PomProcessor(path);
 			List<String> modules = appInfo.getSelectedModules();
 			if(CollectionUtils.isEmpty(modules)){
 				return;
 			}
 			//TODO: Need to handle
 			/*for (ModuleGroup moduleGroup : modules) {
 				pomProcessor.addDependency(moduleGroup.getGroupId(), moduleGroup.getArtifactId(), moduleGroup.getVersions().get(0).getVersion());
 				pomProcessor.save();
 			}*/
 			} catch (PhrescoPomException e) {
 				throw new PhrescoException(e);
 		}
 	}
 
 	@Override
 	public List<ApplicationType> getApplicationTypes(String customerId) throws PhrescoException {
 		try{
 			return getServiceManager().getApplicationTypes(customerId);
 		} catch (ClientHandlerException ex) {
 			S_LOGGER.error(ex.getLocalizedMessage());
 			throw new PhrescoException(ex);
 		}
 	}
 
 	@Override
 	public ApplicationType getApplicationType(String appTypeId, String customerId) throws PhrescoException {
 		try {
 			return getServiceManager().getApplicationType(appTypeId, customerId);
 		} catch (ClientHandlerException ex) {
 			S_LOGGER.error(ex.getLocalizedMessage());
 			throw new PhrescoException(ex);
 		}
 	}
 
 	@Override
 	public  List<Technology> getAppTypeTechnologies(String customerId, String appTypeId) throws PhrescoException {
 		try {
 			return getServiceManager().getArcheTypes(customerId, appTypeId);
 		} catch (ClientHandlerException ex) {
 			S_LOGGER.error(ex.getLocalizedMessage());
 			throw new PhrescoException(ex);
 		}
 	}
 
 	/**
 	 * Delete projects based on the given project codes
 	 */
 	@Override
 	 public void deleteProject(List<String> projectCodes) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.deleteProject(List<String> projectCodes)");
 		 S_LOGGER.debug("deleteProject() projectCodes = "+projectCodes);
 
 		 if (CollectionUtils.isEmpty(projectCodes)) {
 			 throw new PhrescoException(FrameworkImplConstants.ERROR_PROJECT_CODES_EMPTY);
 		 }
 
 		 File zipFile = null;
 
 		 String projectHome = Utility.getProjectHome();
 		 for (String projectCode : projectCodes) {
 			 try {
 				 FileUtils.deleteDirectory(new File(projectHome + projectCode));
 				 zipFile = new File(Utility.getArchiveHome() + projectCode + ".zip");
 				 zipFile.delete();
 			 } catch (IOException e) {
 				 S_LOGGER.error("deleteProject() error happened : " + e.getMessage());
 				 throw new PhrescoException(e);
 			 }
 		 }
 	 }
 
 	 /**
 	  * This will search the given path and return list of projects
 	  *
 	  * @return List of projects which compliance with the framework
 	  */
 	@Override
 	public List<Project> discover(List<File> paths) throws PhrescoException {
         S_LOGGER.debug("Entering Method ProjectAdministratorImpl.discover(List<File> paths)");
 
 		//Use the FileNameFilter for filtering .phresco directories
 		//Read the .project file and construct the Project object.
 
 		S_LOGGER.debug("discover( )  paths = "+paths);
 
 		if (CollectionUtils.isEmpty(paths)) {
 			throw new PhrescoException(MSG_FILE_PATH_EMPTY);
 		}
 
 		List<Project> projects = new ArrayList<Project>();
 
 		for (File path : paths) {
 		    File[] childFiles = path.listFiles();
 		    for (File childFile : childFiles) {
 	            File[] dotPhrescoFolders = childFile.listFiles(new PhrescoFileNameFilter(FOLDER_DOT_PHRESCO));
 	            if(ArrayUtils.isEmpty(dotPhrescoFolders)) {
 	                continue;
 	            }
 
 	            for (File dotPhrescoFolder : dotPhrescoFolders) {
 					File[] dotProjectFiles = dotPhrescoFolder.listFiles(new PhrescoFileNameFilter(PROJECT_INFO_FILE));
 					fillProjects(dotProjectFiles, projects);
 				}
 			 }
 		 }
 
 		 Collections.sort(projects, new ProjectComparator());
 		 return projects;
 	 }
 	
 	private List<Project> filterCustomerProjects(List<Project> projects, String customerId) throws PhrescoException {
         List<Project> customerProjects = new ArrayList<Project>();
         if (CollectionUtils.isNotEmpty(projects)) {
             for (Project project : projects) {
                 ApplicationInfo applicationInfo = project.getApplicationInfo();
                 if (applicationInfo.getCustomerIds().contains(customerId)) {
                     customerProjects.add(project);
                 }
             }
         }
         
         return customerProjects;
     }
 
 	 @Override
 	 public List<DownloadInfo> getServerDownloadInfos(String customerId, String techId) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getServerDownloadInfo()");
 		 
 		 List<DownloadInfo> serverDownloadInfos = filterDownloadInfos(customerId, techId, DownloadInfo.Category.SERVER.name());
 		 
 		 return serverDownloadInfos;
 	 }
 
 	 @Override
 	 public List<DownloadInfo> getDbDownloadInfos(String customerId, String techId) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getDbDownloadInfo()");
 
 		 List<DownloadInfo> dbDownloadInfos = filterDownloadInfos(customerId, techId, DownloadInfo.Category.DATABASE.name());
          
 		 return dbDownloadInfos;
 	 }
 
 	 @Override
 	 public List<DownloadInfo> getEditorDownloadInfos(String customerId, String techId) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getEditorDownloadInfo()");
 		 
 		 List<DownloadInfo> editorDownloadInfos = filterDownloadInfos(customerId, techId, DownloadInfo.Category.EDITOR.name());
          
 		 return editorDownloadInfos;
 	 }
 	 
 	 @Override
 	 public List<DownloadInfo> getToolsDownloadInfos(String customerId, String techId) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getToolsDownloadInfo()");
 		 
 		 List<DownloadInfo> toolsDownloadInfos = filterDownloadInfos(customerId, techId, DownloadInfo.Category.TOOLS.name());
 		 
 		 return toolsDownloadInfos;
 	 }
 	 
 	 @Override
 	 public List<DownloadInfo> getOtherDownloadInfos(String customerId, String techId) throws PhrescoException {
 	     S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getEditorDownloadInfo()");
 
 	     List<DownloadInfo> othersDownloadInfos = filterDownloadInfos(customerId, techId, DownloadInfo.Category.OTHERS.name());
 			 
 	     return othersDownloadInfos;
 	 }
 	 
 	 private List<DownloadInfo> filterDownloadInfos(String customerId, String techId, String category) throws PhrescoException {
          List<DownloadInfo> allDownloadInfos = new ArrayList<DownloadInfo>();
          /*allDownloadInfos = getServiceManager().getDownloads(customerId, techId, category);
          List<DownloadInfo> downloadInfos = new ArrayList<DownloadInfo>(); 
          if (CollectionUtils.isNotEmpty(allDownloadInfos)) {
              for (DownloadInfo downloadInfo : allDownloadInfos) {
                  if (downloadInfo.getCategory().equalsIgnoreCase(DownloadInfo.Category.valueOf(category).name())) {
                      downloadInfos.add(downloadInfo);
                  }
              }
          }*/
          
          return allDownloadInfos;
     }
 	 
 	 /**
 	  * This method is to fetch the settings template through REST service
 	  * @return List of settings template stored in the server [Database, Server and Email]
 	  */
 	 public List<SettingsTemplate> getSettingsTemplates() throws PhrescoException {
 		 try {
 		   //TODO:need to handle
 //			 return PhrescoFrameworkFactory.getServiceManager().getSettingsTemplates();
 		     return null;
 		 } catch (ClientHandlerException ex) {
 			 S_LOGGER.error(ex.getLocalizedMessage());
 			 throw new PhrescoException(ex);
 		 }
 	 }
 
 	 /**
 	  * Returns Settings template which matches the given type
 	  * @return Settings template object
 	  */
 	 public SettingsTemplate getSettingsTemplate(String type) throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getSettingsTemplate(String type)");
 
 		 List<SettingsTemplate> settingsTemplates = getSettingsTemplates();
 		 for (SettingsTemplate settingsTemplate : settingsTemplates) {
 			 if (settingsTemplate.getType().equals(type)) {
 				 return settingsTemplate;
 			 }
 		 }
 
 		 return null;
 	 }
 
 	 public void createSetting(SettingsInfo info, String selectedEnvNames) throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.createSetting(SettingsInfo info, String selectedEnvNames)");
 
 		 if(info == null) throw new PhrescoException("Settings info should not be null or empty");
 
 		 S_LOGGER.debug("createSetting()  Name = "+ info.getName());
 
 		 try {
 			 createSettingsInfo(info, selectedEnvNames, new File(Utility.getProjectHome() + SETTINGS_INFO_FILE_NAME));
 		 } catch (Exception e) {
 
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 public void updateSetting(String envName, String oldConfigName, SettingsInfo settingsInfo) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.updateSetting(String name, SettingsInfo info)");
 		 File configFile = new File(Utility.getProjectHome() + SETTINGS_INFO_FILE_NAME);
 		 updateConfiguration(envName, oldConfigName, settingsInfo, configFile);
 	 }
 
 	 public SettingsInfo getSettingsInfo(String name, String envName) throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getSettingsInfo(String name)");
 		 S_LOGGER.debug("getSettingsInfo() Name = " + name);  		
 
 
 		 List<SettingsInfo> settingsInfos = getSettingsInfos(envName);
 		 if (CollectionUtils.isEmpty(settingsInfos)) {
 			 return null;
 		 }
 
 		 for (SettingsInfo settingsInfo : settingsInfos) {
 			 if (settingsInfo.getName().equals(name)) {
 				 return settingsInfo;
 			 }
 		 }
 
 		 return null;
 	 }
 
 	 public List<SettingsInfo> getSettingsInfos(String envName) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getSettingsInfos()");
 
 		 try {
 			 File settingsFile = new File(Utility.getProjectHome() + SETTINGS_INFO_FILE_NAME);
 			 if (settingsFile.exists()) {
 				 ConfigurationReader reader = new ConfigurationReader(settingsFile);
 				 List<Configuration> configurations = reader.getConfigByEnv(envName);
 				 return getAsSettingsInfo(configurations);
 			 }
 			 return new ArrayList<SettingsInfo>(1);
 
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 public List<SettingsInfo> getSettingsInfos(String type, String projectCode, String envName) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getSettingsInfos(String type, String projectCode, String envName)");
 		 S_LOGGER.debug("Get settings projectCode ....." + projectCode);
 		 S_LOGGER.debug("Get envName....." + envName);
 		 S_LOGGER.debug("Get  type....." + type);
 
 		 if (StringUtils.isEmpty(envName) || StringUtils.isEmpty(type)) {
 			 throw new PhrescoException("Enviroment name or type is empty");
 		 }
 
 		 File directory = new File(".");
 		 S_LOGGER.debug("adaptFunctionalConfig " + adaptFunctionalConfig);
 		 try {
 			 if(adaptFunctionalConfig) {
 		  		  File currentDirPath = new File(directory.getCanonicalPath());
 		  		  String parentpath = new File(currentDirPath.getParent()).getParent();
 		  		  directory = new File(parentpath);
 			 }
 			 S_LOGGER.debug("Current directory to get the path of the project canonical path " + directory.getCanonicalPath());
 		} catch (Exception e) {
 			throw new PhrescoException("Can not find the path specified to get project...");
 		}
 		 
 		 Project project = getProjectByWorkspace(directory);
 		
 		 if(project == null) {
 			 project = getProject(projectCode);
 		 }
 		 
 		 if (project == null) {
 			 throw new PhrescoException("Project code should be valid");
 		 }
 		 String techId = project.getApplicationInfo().getTechInfo().getVersion();
 		 List<SettingsInfo> settingsInfos = filterSettingsInfo(getSettingsInfos(envName), type, techId);
 		 if (CollectionUtils.isNotEmpty(settingsInfos)) {
 			 return settingsInfos;
 		 }
 		 settingsInfos = configurationsByEnvName(envName, project);
 		 return filterConfigurations(settingsInfos, type);
 	 }
 	 
 	 public List<SettingsInfo> getSettingsInfos(String envName, String type, List<String> appliesTo, String settingsName) throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getSettingsInfos(String type, String projectCode, String envName)");
 
 		 if (StringUtils.isEmpty(envName) || StringUtils.isEmpty(type)) {
 			 throw new PhrescoException("Enviroment name or type is empty");
 		 }
 
 		 List<SettingsInfo> settingsInfos = filterConfigurations(getSettingsInfos(envName), type);
 		 //TODO: For settings info check whether the one server for applied type cannot create it again.
 		 /*List<SettingsInfo> filteredInfos = new ArrayList<SettingsInfo>(settingsInfos.size());
         for (String techId : appliesTo) {
             filteredInfos.addAll(filterSettingsInfo(settingsInfos, type, techId));
         }
 
         return filteredInfos;*/
 		 return settingsInfos;
 	 }
 
 
 	 public SettingsInfo getSettingsInfo(String name, String type, String projectCode, String envName) throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getSettingsInfo(String name, String type, String projectCode)");
 		 S_LOGGER.debug("getSettingsInfo() Name = " + name);
 		 S_LOGGER.debug("getSettingsInfo() Type = " + type);
 		 S_LOGGER.debug("getSettingsInfo() ProjectCode = " + projectCode);
 
 
 		 if (StringUtils.isEmpty(name) || StringUtils.isEmpty(type)) {
 			 throw new PhrescoException("Settings/Configuration name or type is empty");
 		 }
 
 		 SettingsInfo settingsInfo = getSettingsInfo(name, envName);
 		 if (settingsInfo != null) {
 			 return settingsInfo;
 		 }
 		 Project project = getProject(projectCode);
 		 if (project == null) {
 			 throw new PhrescoException("Project should not be null");
 		 }
 		 List<SettingsInfo> configurations = configurationsByEnvName(envName, project);
 		 configurations = filterConfigurations(configurations, type);
 		 for (SettingsInfo configuration : configurations) {
 			 if (configuration.getName().equals(name)) {
 				 return configuration;
 			 }
 		 }
 
 		 return null;
 	 }
 
 	 public List<SettingsInfo> getSettingsInfos() throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.configurations(Project project)");
 
 		 File settingsFile = new File(Utility.getProjectHome() + SETTINGS_INFO_FILE_NAME);
 
 		 if (settingsFile.exists()) {
 			 return getAllSettingsInfos(settingsFile);
 		 }
 		 return new ArrayList<SettingsInfo>(1);
 	 }
 
 	 //TODO: Remove the below method once the plugins are adapted for configuration.xml
 	 public SettingsInfo getSettingsInfo(String name, String type, String projectCode) throws PhrescoException {
 		 return null;
 	 }
 
 	 public void deleteSettingsInfos(Map<String, List<String>> selectedConfigs) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.deleteSettingsInfos(List<String> names)");
 		 S_LOGGER.debug("deleteSettingsInfos() Names = "+selectedConfigs);
 		 if (MapUtils.isEmpty(selectedConfigs)) {
 			 throw new PhrescoException("Names should not be empty");
 		 }
 
 		 File configFile = new File(Utility.getProjectHome() + SETTINGS_INFO_FILE_NAME);
 		 deleteConfigurations(selectedConfigs, configFile);
 	 }
 
 	 public List<SettingsInfo> configurations(Project project) throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.configurations(Project project)");
 		 S_LOGGER.debug(" configurations() ProjectCode= : " + project.getApplicationInfo().getCode());
 
 		 File createdConfigurationXml = createConfigurationXml(project.getApplicationInfo().getCode());
 		 return getAllSettingsInfos(createdConfigurationXml);
 	 }
 
 
 	 private File createConfigurationXml (String projectCode)  throws PhrescoException {
 		 File configFile = new File(getConfigurationPath(projectCode).toString());
 		 if (!configFile.exists()) {
 			 List<Environment> envs = getEnvFromService();
 			 createEnvironments(configFile, envs, true);
 		 }
 		 return configFile;
 	 }
 
 	 private List<SettingsInfo> getAllSettingsInfos(File configFile) throws PhrescoException {
 
 		 try {
 			 ConfigurationReader configReader = new ConfigurationReader(configFile);
 			 return getAsSettingsInfo(configReader.getConfigurations());
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 private List<SettingsInfo> getAsSettingsInfo(List<Configuration> configurations) {
 		 List<SettingsInfo> settingsInfos = new ArrayList<SettingsInfo>(configurations.size());
 		 for (Configuration configuration : configurations) {
 			 SettingsInfo settingsInfo = new SettingsInfo(configuration);
 			 settingsInfos.add(settingsInfo);
 		 }
 		 return settingsInfos;
 	 }
 
 
 	 private List<SettingsInfo> getAllSettingsInfos(File configFile, String configName) throws PhrescoException {
 
 		 try {
 			 ConfigurationReader configReader = new ConfigurationReader(configFile);
 			 return getAsSettingsInfo(configReader.getConfigurations(), configName );
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 private List<SettingsInfo> getAsSettingsInfo(List<Configuration> configurations, String configName) {
 		 List<SettingsInfo> settingsInfos = new ArrayList<SettingsInfo>(configurations.size());
 		 for (Configuration configuration : configurations) {
 			 if(!configuration.getName().equals(configName)){
 				 SettingsInfo settingsInfo = new SettingsInfo(configuration);
 				 settingsInfos.add(settingsInfo);
 			 }
 		 }
 		 return settingsInfos;
 	 }
 
 	 public SettingsInfo configuration(String name, String envName, Project project) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.configuration(String name, Project project)");
 		 S_LOGGER.debug("configuration()  Name = "+ name);
 
 		 if (StringUtils.isEmpty(name)) {
 			 throw new PhrescoException("Configuration name should not be empty");
 		 }
 
 		 String configPath = getConfigurationPath(project.getApplicationInfo().getCode()).toString();
 		 try {
 			 ConfigurationReader configReader = new ConfigurationReader(new File(configPath));
 			 List<Configuration> configurations = configReader.getConfigByEnv(envName);
 			 for (Configuration configuration : configurations) {
 				 if (name.equals(configuration.getName())) {
 					 return new SettingsInfo(configuration);
 				 }
 			 }
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 
 		 return null;
 	 }
 
 	 public List<SettingsInfo> configurations(Project project, String envName, String type, String configName) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.configuration(String name, Project project)");
 
 		 if (StringUtils.isEmpty(type)) {
 			 throw new PhrescoException("Configuration name should not be empty");
 		 }
 
 		 try {
 			 List<SettingsInfo> settingsInfos = configurationsByEnvName(envName, project);
 			 return filterConfigurations(settingsInfos, type);
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 
 	 public List<SettingsInfo> configurationsByEnvName(String envName, Project project) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.configurations(String type, Project project)");
 
 		 try {
 			 File directory = new File(".");
 			 S_LOGGER.debug("adaptFunctionalConfig in configurationsByEnvName " + adaptFunctionalConfig);
 			 try {
 				 if(adaptFunctionalConfig) {
 			  		  File currentDirPath = new File(directory.getCanonicalPath());
 			  		  String parentpath = new File(currentDirPath.getParent()).getParent();
 			  		  directory = new File(parentpath);
 				 }
 				 S_LOGGER.debug("configurationsByEnvName project canonical path " + directory.getCanonicalPath());
 			} catch (Exception e) {
 				throw new PhrescoException("Can not find the path specified to get configurationsByEnvName...");
 			}
 			 String configPath = new File(directory + File.separator + FOLDER_DOT_PHRESCO + File.separator + CONFIGURATION_INFO_FILE_NAME).getPath();
 			 S_LOGGER.debug("configurationsByEnvName config path " + configPath);
 			 if(!new File(configPath).exists()) {
 				 S_LOGGER.debug("configurationsByEnvName Doesnot exists going for next job" + configPath);
 				 configPath = getConfigurationPath(project.getApplicationInfo().getCode()).toString();
 			 }
 			 ConfigurationReader configReader = new ConfigurationReader(new File (configPath));
 			 return getAsSettingsInfo(configReader.getConfigByEnv(envName));
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 public List<SettingsInfo> configurationsByEnvName(String envName) throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.configurations(String type, Project project)");
 		 try {
 			 String configPath = Utility.getProjectHome() + SETTINGS_INFO_FILE_NAME;
 			 ConfigurationReader configReader = new ConfigurationReader(new File (configPath));
 			 return getAsSettingsInfo(configReader.getConfigByEnv(envName));
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 public List<SettingsInfo> configurations(String type, Project project) throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.configurations(String type, Project project)");
 
 		 List<SettingsInfo> configurations = configurations(project);
 		 if (CollectionUtils.isEmpty(configurations)) {
 			 return null;
 		 }
 
 		 return filterConfigurations(configurations, type);
 	 }
 
 	 private List<SettingsInfo> filterConfigurations(List<SettingsInfo> configurations, String type) {
 		 List<SettingsInfo> filterConfigs = new ArrayList<SettingsInfo>(configurations.size());
 		 for (SettingsInfo configuration : configurations) {
 			 if (configuration.getType().equals(type)) {
 				 filterConfigs.add(configuration);
 			 }	
 		 }
 		 return filterConfigs;
 	 }
 
 	 public void createConfiguration(SettingsInfo info, String selectedEnvNames, Project project) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.createConfiguration(SettingsInfo info, Project project)");
 		 if(info == null) throw new PhrescoException("Settings info should not be null or empty");
 		 S_LOGGER.debug("createConfiguration()  Name = "+ info.getName());
 
 		 try {
 			 String path = getConfigurationPath(project.getApplicationInfo().getCode()).toString();
 			 createSettingsInfo(info, selectedEnvNames, new File(path));
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 public void updateConfiguration(String envName, String oldConfigName, SettingsInfo settingsInfo, Project project) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.updateConfiguration(SettingsInfo info, Project project, String name)");
 		 S_LOGGER.debug("updateConfiguration() ProjectInfo = "+ project.getApplicationInfo());
 		 S_LOGGER.debug("updateConfiguration() Name = "+ settingsInfo.getName());
 
 		 File configFile = new File(getConfigurationPath(project.getApplicationInfo().getCode()).toString());
 		 updateConfiguration(envName, oldConfigName, settingsInfo, configFile);
 	 }
 
 	 private void updateConfiguration(String envName, String oldConfigName, SettingsInfo settingsInfo, File configFile) throws PhrescoException {
 		 try {
 			 ConfigurationReader configReader = new ConfigurationReader(configFile);
 			 ConfigurationWriter configWriter = new ConfigurationWriter(configReader, false);
 			 configWriter.updateConfiguration(envName, oldConfigName, settingsInfo);
 			 configWriter.saveXml(configFile);
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 public void deleteConfigurations(Map<String, List<String>> selectedConfigs, Project project) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.deleteConfigurations(List<String> names, Project project)");
 		 S_LOGGER.debug("deleteConfigurations()  Names = "+ selectedConfigs);
 
 		 if (MapUtils.isEmpty(selectedConfigs)) {
 			 throw new PhrescoException("Names should not be empty");
 		 }
 
 		 File configFile = new File(getConfigurationPath(project.getApplicationInfo().getCode()).toString());
 		 deleteConfigurations(selectedConfigs, configFile);
 	 }
 
 	 private void deleteConfigurations(Map<String, List<String>> selectedConfigs, File configFile) throws PhrescoException{
 		 try {
 			 ConfigurationReader reader = new ConfigurationReader(configFile);
 			 ConfigurationWriter writer = new ConfigurationWriter(reader, false);
 			 writer.deleteConfigurations(selectedConfigs);
 			 writer.saveXml(configFile);
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 private void fillProjects(File[] dotProjectFiles, List<Project> projects) throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.fillProjects(File[] dotProjectFiles, List<Project> projects)");
 
 		 if(ArrayUtils.isEmpty(dotProjectFiles)) {
 			 return;
 		 }
 
 		 Gson gson = new Gson();
 		 BufferedReader reader = null;
 
 		 for (File dotProjectFile : dotProjectFiles) {
 			 try {
 				 reader = new BufferedReader(new FileReader(dotProjectFile));
 				 ApplicationInfo appInfo = gson.fromJson(reader, ApplicationInfo.class);
 				 projects.add(new ProjectImpl(appInfo));
 			 } catch (FileNotFoundException e) {
 				 throw new PhrescoException(e);
 			 } finally {
 				 Utility.closeStream(reader);
 			 }
 		 }
 	 }
 
 	 private void createSettingsInfo(SettingsInfo settingsInfo, String selectedEnvNames, File path) throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.createSettingsInfo(SettingsInfo info, File path)");
 		 S_LOGGER.debug("fillProjects() File Path= " +path.getPath());
 
 		 try {
 			 ConfigurationReader configReader = new ConfigurationReader(path);
 			 ConfigurationWriter configWriter = new ConfigurationWriter(configReader, false);
 			 configWriter.createConfiguration(selectedEnvNames, settingsInfo);
 			 configWriter.saveXml(path);
 		 } catch (Exception e) {
 
 			 throw new PhrescoException(e);
 		 } 
 
 	 }
 
 	 public void setAsDefaultEnv(String env, Project project) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.createSettingsInfo(SettingsInfo info, File path)");
 		 try {
 			 String path = getConfigurationPath(project.getApplicationInfo().getCode()).toString();
 			 ConfigurationReader configReader = new ConfigurationReader(new File(path));
 			 ConfigurationWriter configWriter = new ConfigurationWriter(configReader, false);
 			 configWriter.setDefaultEnvironment(env);
 			 configWriter.saveXml(new File(path));
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 } 
 	 }
 	 
 	 public void createEnvironments(Project project, List<Environment> selectedEnvs, boolean isNewFile) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.createEnvironments(List<String> envNames)");
 
 		 String configPath = getConfigurationPath(project.getApplicationInfo().getCode()).toString();
 		 createEnvironments(new File(configPath), selectedEnvs, isNewFile);
 	 }
 
 	 public void createEnvironments(List<Environment> selectedEnvs) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.createEnvironments(List<String> envNames, Project project)");
 
 		 File settingsFile = new File(Utility.getProjectHome() + SETTINGS_INFO_FILE_NAME);
 		 createEnvironments(settingsFile, selectedEnvs, !settingsFile.exists());
 	 }
 
 	 private void createEnvironments(File configPath, List<Environment> selectedEnvs, boolean isNewFile) throws PhrescoException {
 		 try {
 			 ConfigurationReader reader = new ConfigurationReader(configPath);
 			 ConfigurationWriter writer = new ConfigurationWriter(reader, isNewFile);
 			 writer.createEnvironment(selectedEnvs);
 			 writer.saveXml(configPath);
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 public void deleteEnvironments(List<String> envNames) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.deleteEnvironments(List<String> envNames)");
 		 File configXml = new File(Utility.getProjectHome() + SETTINGS_INFO_FILE_NAME);
 		 deleteEnvironments(envNames, configXml);    	
 	 }
 
 	 public void deleteEnvironments(List<String> envNames, Project project) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.deleteEnvironments(List<String> envNames, Project project)");
 		 File configXml = new File(getConfigurationPath(project.getApplicationInfo().getCode()).toString());
 		 deleteEnvironments(envNames, configXml);
 	 }
 
 	 private void deleteEnvironments(List<String> envNames, File configXml) throws PhrescoException {
 		 try {
 			 ConfigurationReader reader = new ConfigurationReader(configXml);
 			 ConfigurationWriter writer = new ConfigurationWriter(reader, false);
 			 writer.deleteEnvironments(envNames);
 			 writer.saveXml(configXml);
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 public List<SettingsInfo> readSettingsInfo(File path) throws IOException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.readSettingsInfo(File path)");
 		 S_LOGGER.debug("readSettingsInfo() File path = "+path.getPath());
 		 if (!path.exists()) {
 			 S_LOGGER.error("readSettingsInfo() > " + FrameworkImplConstants.ERROR_FILE_PATH_INCORRECT + path);
 			 return new ArrayList<SettingsInfo>(1);
 		 }
 
 		 BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
 		 Gson gson = new Gson();
 		 Type type = new TypeToken<List<SettingsInfo>>(){}.getType();
 
 		 List<SettingsInfo> settingsInfos = gson.fromJson(bufferedReader, type);
 		 //TODO:need to handle
 //		 Collections.sort(settingsInfos, new SettingsInfoComparator());
 		 bufferedReader.close();
 		 return settingsInfos;
 	 }
 
 	 public List<BuildInfo> getBuildInfos(Project project) throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getBuildInfos(Project project)");
 		 try {
 			 return readBuildInfo(new File(Utility.getProjectHome() + project.getApplicationInfo().getCode() + File.separator + BUILD_DIR + File.separator + BUILD_INFO_FILE_NAME));
 		 } catch (IOException e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 public List<BuildInfo> readBuildInfo(File path) throws IOException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.readBuildInfo(File path)");
 		 S_LOGGER.debug("getBuildInfos() File Path = "+path.getPath());
 
 		 if (!path.exists()) {
 
 			 S_LOGGER.error("readBuildInfo() > " + FrameworkImplConstants.ERROR_FILE_PATH_INCORRECT + path);
 
 			 return new ArrayList<BuildInfo>(1);
 		 }
 
 		 BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
 		 Gson gson = new Gson();
 		 Type type = new TypeToken<List<BuildInfo>>(){}.getType();
 
 		 List<BuildInfo> buildInfos = gson.fromJson(bufferedReader, type);
 		 //TODO:Need to handle
 //		 Collections.sort(buildInfos, new BuildInfoComparator());
 		 bufferedReader.close();
 
 		 return buildInfos;
 	 }
 
 	 private StringBuilder getConfigurationPath(String projectCode) {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getConfigurationPath(Project project)");
 		 S_LOGGER.debug("removeSettingsInfos() ProjectCode = " + projectCode);
 
 
 		 StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 		 builder.append(projectCode);
 		 builder.append(File.separator);
 		 builder.append(FOLDER_DOT_PHRESCO);
 		 builder.append(File.separator);
 		 builder.append(CONFIGURATION_INFO_FILE_NAME);
 
 		 return builder;
 	 }
 
 
 	 private List<SettingsInfo> filterSettingsInfo(List<SettingsInfo> infos, String type, String technolgoyType) {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.filterSettingsInfo(List<SettingsInfo> infos, String type)");
 		 S_LOGGER.debug("filterSettingsInfo() Type = "+type);
 
 		 List<SettingsInfo> filteredList = new ArrayList<SettingsInfo>();
 		 for (SettingsInfo settingsInfo : infos) {
 			 if (settingsInfo.getType().equals(type) && settingsInfo.getAppliesTo().contains(technolgoyType)) {
 				 filteredList.add(settingsInfo);
 			 }
 		 }
 
 		//TODO:need to handle
 //		 Collections.sort(filteredList, new SettingsInfoComparator());
 		 return filteredList;
 	 }
 
 	 private class PhrescoFileNameFilter implements FilenameFilter {
 		 private String filter_;
 		 public PhrescoFileNameFilter(String filter) {
 			 filter_ = filter;
 		 }
 
 		 public boolean accept(File dir, String name) {
 			 return name.endsWith(filter_);
 		 }
 	 }
 
 	 @Override
 	 public BuildInfo getBuildInfo(Project project, int buildNumber) throws PhrescoException {
 		 List<BuildInfo> buildInfos = getBuildInfos(project);
 		 if (CollectionUtils.isEmpty(buildInfos)) {
 			 return null;
 		 }
 
 		 for (BuildInfo buildInfo : buildInfos) {
 			 if (buildInfo.getBuildNo() == buildNumber) {
 				 return buildInfo;
 			 }
 		 }
 
 		 return null;
 	 }
 
 	 @Override
 	 public List<BuildInfo> getBuildInfos(Project project, int[] buildNumbers) throws PhrescoException {
 
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getBuildInfos(Project project, int[] buildNumbers)");
 		 S_LOGGER.debug("getBuildInfos() Project Information = "+project.getApplicationInfo());
 
 		 List<BuildInfo> buildInfos = getBuildInfos(project);
 		 if (CollectionUtils.isEmpty(buildInfos)) {
 			 return Collections.emptyList();
 		 }
 
 		 List<BuildInfo> selectedInfos = new ArrayList<BuildInfo>(8);
 		 BuildInfo buildInfo = null;
 		 for (int i = 0; i < buildNumbers.length; i++) {
 			 buildInfo = getBuildInfo(project, buildNumbers[i]);
 			 if (buildInfo == null) {
 				 continue;
 			 }
 
 			 selectedInfos.add(buildInfo);
 		 }
 
 		 return selectedInfos;
 	 }
 
 	 @Override
 	 public void deleteBuildInfos(Project project, int[] buildNumbers) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.deleteBuildInfos(Project project, int[] buildNumbers)");
 
 		 List<BuildInfo> buildInfos = getBuildInfos(project);
 
 		 S_LOGGER.debug("deleteBuildInfos() buildNumbers = "+buildNumbers);
 
 		 List<BuildInfo> selectedInfos = getBuildInfos(project, buildNumbers);
 		 if (CollectionUtils.isEmpty(selectedInfos)) {
 			 return;
 		 }
 
 		 //Delete the build archives
 		 try {
 			 deleteBuildArchive(project, selectedInfos);
 		 } catch (IOException e) {
 			 throw new PhrescoException(e);
 		 }
 
 		 //Delete the entry from build.info
 		 Iterator<BuildInfo> iterator = buildInfos.iterator();
 		 for (BuildInfo selectedInfo : selectedInfos) {
 			 while (iterator.hasNext()) {
 				 BuildInfo buildInfo = iterator.next();
 				 if (buildInfo.getBuildNo() == selectedInfo.getBuildNo()) {
 					 iterator.remove();
 					 break;
 				 }
 			 }
 		 }
 
 		 StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 		 builder.append(project.getApplicationInfo().getCode());
 		 builder.append(File.separator);
 		 builder.append(FrameworkConstants.BUILD_DIR);
 		 builder.append(File.separator);
 		 builder.append(FrameworkConstants.BUILD_INFO_FILE_NAME);
 		 try {
 			 writeBuildInfo(buildInfos, new File(builder.toString()));
 		 } catch (IOException e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 private void deleteBuildArchive(Project project, List<BuildInfo> selectedInfos) throws IOException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.deleteBuildArchive(Project project, List<BuildInfo> selectedInfos)");
 		 File file = null;
 		 String delFilename = null;
 		 for (BuildInfo selectedInfo : selectedInfos) {
 			 if (TechnologyTypes.IPHONES.contains(project.getApplicationInfo().getTechInfo().getVersion())) {
 				 String deleivarables = selectedInfo.getDeliverables();
 				 String buildNameSubstring = deleivarables.substring(deleivarables.lastIndexOf("/") + 1);
 				 delFilename = buildNameSubstring;
 				 // Delete build folder
 				 deleteBuilFolder(project, delFilename.subSequence(0, delFilename.length() - 4).toString());
 				 //Delete zip file
 				 file = new File(getBuildInfoHome(project) + delFilename);
 				 file.delete();
 			 } else if (TechnologyTypes.ANDROIDS.contains(project.getApplicationInfo().getTechInfo().getVersion())) {
 				 // Delete zip file
 				 String deleivarables = selectedInfo.getDeliverables();
 				 delFilename = deleivarables;
 				 file = new File(getBuildInfoHome(project) + delFilename);
 				 file.delete();	            
 				 //Delete apk file
 				 delFilename = selectedInfo.getBuildName();
 				 file = new File(getBuildInfoHome(project) + delFilename);
 				 file.delete();
 			 } else {
 				 //Delete zip file
 				 delFilename = selectedInfo.getBuildName();
 				 file = new File(getBuildInfoHome(project) + delFilename);
 				 file.delete();
 			 }
 		 }
 	 }
 
 	 private void deleteBuilFolder(Project project, String buildFolderPath) throws IOException {
 		 FileUtils.deleteDirectory(new File(getBuildInfoHome(project) + buildFolderPath));
 	 }
 
 	 private String getBuildInfoHome(Project project) {
 		 StringBuilder builder = new StringBuilder();
 		 builder.append(Utility.getProjectHome());
 		 builder.append(project.getApplicationInfo().getCode());
 		 builder.append(File.separator);
 		 builder.append(FrameworkConstants.BUILD_DIR);
 		 builder.append(File.separator);
 		 return builder.toString();
 	 }
 
 	 private void writeBuildInfo(List<BuildInfo> buildInfos, File path) throws IOException {
 		 Gson gson = new Gson();
 		 String buildInfoJson = gson.toJson(buildInfos);
 		 writeJson(buildInfoJson, path);
 	 }
 
 	 private void writeJson(String json, File path) throws IOException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.writeJson(String json, File path)");
 		 FileWriter writer = null;
 		 try {
 			 S_LOGGER.debug("writeJson()  File path = " +path.getPath());
 			 writer = new FileWriter(path);
 			 writer.write(json);
 			 writer.flush();
 		 } finally {
 			 if(writer != null) {
 				 try {
 					 writer.close();
 				 } catch (IOException e) {
 					 S_LOGGER.warn("writeJson() > error inside finally");
 
 				 }
 			 }
 		 }
 	 }
 
 //	 @Override
 //	 public List<VideoInfo> getVideoInfos() throws PhrescoException {
 //		 try {
 //			 return getServiceManager().getVideoInfos();
 //		 } catch (ClientHandlerException ex) {
 //			 S_LOGGER.error(ex.getLocalizedMessage());
 //			 throw new PhrescoException(ex);
 //		 }
 //	 }
 //
 //	 @Override
 //	 public List<VideoType> getVideoTypes(String name) throws PhrescoException {
 //		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getVideoTypes(String name)");
 //		 List<VideoInfo> videoInfos = getVideoInfos();
 //		 for (VideoInfo videoInfo : videoInfos) {
 //			 S_LOGGER.debug("getVideoTypes() Name = "+name);
 //			 if(videoInfo.getName().equals(name)) {
 //				 return videoInfo.getVideoList();
 //			 }
 //		 }
 //		 return null;
 //	 }
 
 	 @Override
 	 public List<ArtifactGroup> getCoreModules(Technology technology) {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getCoreModules(Technology technology)");
 		 S_LOGGER.debug("getCoreModules() TechnologyName = "+technology.getName());
 
 		 String technologyId = technology.getId();
 		 List<ArtifactGroup> coreModules = coreModulesMap.get(technologyId);
 		 if (CollectionUtils.isNotEmpty(coreModules)) {
 			 return coreModules;
 		 }
 
 		 coreModules = new ArrayList<ArtifactGroup>();
 		 //TODO:Need to handle
 //		 List<ArtifactGroup> modules = technology.getModules();
 //		 if (CollectionUtils.isNotEmpty(modules)) {
 //			 for (ArtifactGroup module : modules) {
 //				 if (module.isCore()) {
 //					 coreModules.add(module);
 //				 }
 //			 }
 //		 }
 //
 //		 Collections.sort(coreModules, new ModuleComparator());
 		 coreModulesMap.put(technologyId, coreModules);
 		 return coreModules;
 	 }
 
 	 @Override
 	 public List<ArtifactGroup> getCustomModules(Technology technology) {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getCustomModules(Technology technology)");
 		 S_LOGGER.debug("getCustomModules() TechnologyId = "+technology.getId());
 
 		 String technologyId = technology.getId();
 		 List<ArtifactGroup> customModules = customModulesMap.get(technologyId);
 		 if (CollectionUtils.isNotEmpty(customModules)) {
 			 return customModules;
 		 }
 		 //TODO:Need to handle
 //		 customModules = new ArrayList<ArtifactGroup>();
 //		 List<ArtifactGroup> modules = technology.getModules();
 //		 if (CollectionUtils.isNotEmpty(modules)) {
 //			 for (ArtifactGroup module : modules) {
 //				 if (!module.isCore()) {
 //					 customModules.add(module);
 //				 }
 //			 }
 //		 }
 //
 //		 Collections.sort(customModules, new ModuleComparator());
 		 customModulesMap.put(technologyId, customModules);
 		 return customModules;
 	 }
 
 	 @Override
 	 public String sendReport(LogInfo loginfo) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.sendReport(LogInfo loginfo)");
 		 S_LOGGER.debug("Loginfo values : " + loginfo.toString());
 		 try {
 		     //TODO:Need to handle
 //			 ClientResponse response = getServiceManager().sendReport(loginfo);
 //			 if (response.getStatus() != 204) {
 //				 //            		throw new PhrescoException("Error Report sending failed");
 //				 return "Report submition failed";
 //			 } else {
 //				 return "Report submitted successfully";
 //			 }
 		 } catch (ClientHandlerException e) {
 			 S_LOGGER.error(e.getLocalizedMessage());
 
 			 throw new PhrescoException(e);
 		 }
 		 return null;
 	 }
 
 
 	 private void setAdminConfigInfosFrmService() throws PhrescoException {
 	     //TODO:Need to handle
 //		 if (CollectionUtils.isEmpty(adminConfigInfos)) {
 //			 adminConfigInfos = PhrescoFrameworkFactory.getServiceManager().getAdminConfig();
 //		 }
 	 }
 
 	 public String getJforumPath() throws PhrescoException {
 	       //TODO:Need to handle
 //		 if (CollectionUtils.isEmpty(adminConfigInfos)) {
 //			 setAdminConfigInfosFrmService();
 //		 }
 //
 //		 for (AdminConfigInfo adminConfigInfo : adminConfigInfos) {
 //			 if (ADMIN_CONFIG_JFORUM_PATH.equals(adminConfigInfo.getKey())) {
 //				 return adminConfigInfo.getValue();
 //			 }
 //		 }
 		 return null;
 	 }
 
 	 public List<ValidationResult> validate() throws PhrescoException {
 		 //1.Check for phresco directory structure validation
 		 //2.Check for Environment settings like Android Home, Node JS Home
 		 List<ValidationResult> results = new ArrayList<ValidationResult>(64);
 		 /*List<Validator> validators = PhrescoFrameworkFactory.getValidators(TechnologyTypes.ALL_TECHS);
 		 for (Validator validator : validators) {
 			 results.addAll(validator.validate(null));
 		 }*/
 
 		 return results;
 	 }
 
 	 public List<ValidationResult> validate(Project project) throws PhrescoException {
 		 //1.Validate the directory structure based on the archetypes
 		 //2.Validate for changes in the list of modules in projects
 		 //	info and the actual list of modules present in the directory
 		 List<ValidationResult> results = new ArrayList<ValidationResult>(64);
 		 /*String techId = project.getApplicationInfo().getTechInfo().getVersion();
 		 List<Validator> validators = PhrescoFrameworkFactory.getValidators(techId);
 
 		 for (Validator validator : validators) {
 
 			 results.addAll(validator.validate(project.getApplicationInfo().getCode()));
 		 }*/
 
 		 return results;
 	 }
 	 
 
 
 	 public List<Environment> getEnvFromService() throws PhrescoException {
 		 try {
 			 return Collections.singletonList(getServiceManager().getDefaultEnvFromServer());
 		 } catch (ClientHandlerException ex) {
 			 S_LOGGER.error(ex.getLocalizedMessage());
 			 throw new PhrescoException(ex);
 		 }
 	 }
 
 	 public List<Environment> getEnvironments(Project project) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getEnvironments(Project project)");
 		 String configPath = getConfigurationPath(project.getApplicationInfo().getCode()).toString();
 		 return getEnvironments(new File(configPath));
 	 }
 
 	 public List<Environment> getEnvironments() throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getEnvironments()");
 		 return getEnvironments(new File(Utility.getProjectHome() + SETTINGS_INFO_FILE_NAME));
 	 }
 
 	 private List<Environment> getEnvironments(File configFile) throws PhrescoException {
 		 try {
 			 if (configFile.exists()) {
 				 ConfigurationReader reader = new ConfigurationReader(configFile);
 				 List<Environment> environments = reader.getEnvironments();
 				 return environments; 
 			 }
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 		 return new ArrayList<Environment>(1);
 	 }
 
 	 public Collection<String> getEnvNames(Project project) throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getEnvNames(Project project)");
 		 String configPath = getConfigurationPath(project.getApplicationInfo().getCode()).toString();
 		 return getEnvNames(new File (configPath));
 	 }
 
 	 public Collection<String> getEnvNames() throws PhrescoException {
 		 S_LOGGER.debug("Entering Method ProjectAdministratorImpl.getEnvNames()");
 		 return getEnvNames(new File(Utility.getProjectHome() + SETTINGS_INFO_FILE_NAME));
 	 }
 
 	 private Collection<String> getEnvNames(File configFile) throws PhrescoException {
 		 try {
 			 ConfigurationReader reader = new ConfigurationReader(configFile);
 			 return reader.getEnvironmentNames();
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 public void updateTestConfiguration(Project project, String selectedEnvs, String browser, String resultConfigXml, String resolution) throws PhrescoException {
 		 try {
 			 S_LOGGER.debug("Enabling update test configuration !!!");
 			 adaptFunctionalConfig = true;
 			 String projectCode = project.getApplicationInfo().getCode();
 			 List<SettingsInfo> settingsInfos = new ArrayList<SettingsInfo>(2);
 			 settingsInfos.addAll(getSettingsInfos(Constants.SETTINGS_TEMPLATE_SERVER, projectCode, selectedEnvs));
 			 ConfigurationReader configReader = new ConfigurationReader(new File(resultConfigXml));
 			 ConfigurationWriter configWriter = new ConfigurationWriter(configReader, false);
 			 for (SettingsInfo settingsInfo : settingsInfos) {
 				configWriter.updateTestConfiguration(settingsInfo, browser, resultConfigXml, resolution);
 			 }
 			 configWriter.saveXml(new File(resultConfigXml));
 			 adaptFunctionalConfig = false;
 			 S_LOGGER.debug("Update test configuration end ");
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 @Override
 	 public String getDefaultEnvName(String projectCode) {
 		 return "Production";
 	 }
 
 	 @Override
 	 public List<WebService> getWebservices() throws PhrescoException {
          try {
              return getServiceManager().getWebServices();
          } catch (Exception ex) {
              throw new PhrescoException(ex);
          }
      }
 	 
 	 @Override
 	 public List<ApplicationInfo> getPilotProjects(String customerId, String techId) throws PhrescoException {
          try {
              return getServiceManager().getPilotProjects(customerId, techId);
          } catch (Exception ex) {
              ex.printStackTrace();
              throw new PhrescoException(ex);
          }
      }
 
 	 private void updateProjectPOM(ApplicationInfo appInfo) throws PhrescoException {
 		 try {
 			 String path = Utility.getProjectHome() + appInfo.getCode() + File.separator + POM_FILE;
 			 PomProcessor processor = new PomProcessor(new File(path));
 			 Model model = processor.getModel();
 			 /*if (StringUtils.isNotEmpty(appInfo.getVersion())) {
 				 model.setVersion(appInfo.getVersion());
 			 }*/
 			 if (StringUtils.isNotEmpty(appInfo.getPilotContent().getGroupId())) {
 				 model.setGroupId(appInfo.getPilotContent().getGroupId());
 			 }
 			 if (StringUtils.isNotEmpty(appInfo.getPilotContent().getArtifactId())) {
 				 model.setArtifactId(appInfo.getPilotContent().getArtifactId());
 			 }
 			 processor.save();
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
 	 }
 
 	 /**
 	  * Delete the Sql Folder
 	  */
 	 public void deleteSqlFolder(List<String> dbList , ApplicationInfo appInfo) throws PhrescoException {
 		 initializeSqlMap();
 		 try {
 			 File sqlPath = new File(Utility.getProjectHome() + File.separator
 					 + appInfo.getCode() + File.separator
 					 + sqlFolderPathMap.get(appInfo.getTechInfo().getVersion()));
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
 	 
 	 protected void createSqlFolder(ApplicationInfo info, File path) throws PhrescoException {
 			String databaseType = "";
 			String parentFile = path.getParentFile().getParent();
             List<ArtifactGroupInfo> databaseList = info.getSelectedDatabases();
             String techId = info.getTechInfo().getVersion();
             if (databaseList == null || databaseList.size() == 0) {
             	return;
             }
             File mysqlFolder = new File(parentFile, sqlFolderPathMap.get(techId) + Constants.DB_MYSQL);
             File mysqlVersionFolder = getMysqlVersionFolder(mysqlFolder);
             //TODO: Need to handle
             /*for (Database db : databaseList) {
             	databaseType = db.getName().toLowerCase();
             	List<String> versions = db.getVersions();
             	for (String version : versions) {
             		String sqlPath = databaseType + File.separator + version.trim();
             		File sqlFolder = new File(parentFile, sqlFolderPathMap.get(techId) + sqlPath);
             		sqlFolder.mkdirs();
             		if (databaseType.equals(Constants.DB_MYSQL) && mysqlVersionFolder != null
             				&& !(mysqlVersionFolder.getPath().equals(sqlFolder.getPath()))) {						
             			FileUtils.copyDirectory(mysqlVersionFolder, sqlFolder);
             		} else {
             			File sqlFile = new File(sqlFolder, Constants.SITE_SQL);
             			sqlFile.createNewFile();
             		}
             	}
             }*/
 		}
 	 
 		private File getMysqlVersionFolder(File mysqlFolder) {
 			File[] mysqlFolderFiles = mysqlFolder.listFiles();
 			if (mysqlFolderFiles != null && mysqlFolderFiles.length > 0) {
 				return mysqlFolderFiles[0];
 			}
 			return null;
 		}
 
 	public List<Reports> getReports(ApplicationInfo appInfo) throws PhrescoException {
 		try {
 			String techId = appInfo.getTechInfo().getVersion();
             List<Reports> reports = siteReportMap.get(techId);
 			if (CollectionUtils.isEmpty(reports)) {
 			    //TODO:Need to handle
 //				reports = getServiceManager().getReports(techId);
 				siteReportMap.put(techId, reports);
 			}
 			
 			return reports;
 		} catch (Exception ex) {
 			throw new PhrescoException(ex);
 		}
 	}
 	
 	public List<Reports> getPomReports(ApplicationInfo appInfo) throws PhrescoException {
 		try {
 			SiteConfigurator configurator = new SiteConfigurator();
 			File file = new File(Utility.getProjectHome() + File.separator + appInfo.getCode() + File.separator + POM_FILE);
 			List<Reports> reports = configurator.getReports(file);
 			return reports;
 		} catch (Exception ex) {
 			throw new PhrescoException(ex);
 		}
 	}
 	
 	public void updateRptPluginInPOM(ApplicationInfo appInfo, List<Reports> reports, List<ReportCategories> reportCategories) throws PhrescoException {
 		try {
 			SiteConfigurator configurator = new SiteConfigurator();
 			File file = new File(Utility.getProjectHome() + File.separator + appInfo.getCode() + File.separator + POM_FILE);
 				configurator.addReportPlugin(reports, reportCategories, file);
 		} catch (Exception e) {
 			throw new PhrescoException();
 		}
 	}
 	
 	public List<CertificateInfo> getCertificate(String host, int port) throws PhrescoException {
 		List<CertificateInfo> certificates = new ArrayList<CertificateInfo>();
 		CertificateInfo info;
 		try {
 			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
 			SSLContext context = SSLContext.getInstance("TLS");
 			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
 			tmf.init(ks);
 			X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
 			SavingTrustManager tm = new SavingTrustManager(defaultTrustManager);
 			context.init(null, new TrustManager[]{tm}, null);
 			SSLSocketFactory factory = context.getSocketFactory();
 			SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
 			socket.setSoTimeout(10000);
 			try {
 				socket.startHandshake();
 				socket.close();
 			} catch (SSLException e) {
 				
 			}
 			X509Certificate[] chain = tm.chain;
 			for (int i = 0; i < chain.length; i++) {
 				X509Certificate x509Certificate = chain[i];
 				String subjectDN = x509Certificate.getSubjectDN().getName();
 				String[] split = subjectDN.split(",");
 				info = new CertificateInfo();
 				info.setSubjectDN(subjectDN);
 				info.setDisplayName(split[0]);
 				info.setCertificate(x509Certificate);
 				certificates.add(info);
 			}
 		} catch (Exception e) {
 			throw new PhrescoException();
 		}
 	
 		return certificates;
 	}
 
 	public void addCertificate(CertificateInfo info, File file) throws PhrescoException {
 		char[] passphrase = "changeit".toCharArray();
 		InputStream inputKeyStore = null;
 		OutputStream outputKeyStore = null;
 		try {
 			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
 			keyStore.load(null);
 			keyStore.setCertificateEntry(info.getDisplayName(), info.getCertificate());
 			if (!file.exists()) {
 				file.getParentFile().mkdirs();
 				file.createNewFile();
 			}
 			outputKeyStore = new FileOutputStream(file);
 			keyStore.store(outputKeyStore, passphrase);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		} finally {
 			Utility.closeStream(inputKeyStore);
 			Utility.closeStream(outputKeyStore);
 		}
 	}
 }
 
 class SavingTrustManager implements X509TrustManager {
 
 	private final X509TrustManager tm;
 	X509Certificate[] chain;
 
 	SavingTrustManager(X509TrustManager tm) {
 		this.tm = tm;
 	}
 
 	public X509Certificate[] getAcceptedIssuers() {
 		throw new UnsupportedOperationException();
 	}
 
 	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
 		throw new UnsupportedOperationException();
 	}
 
 	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
 		this.chain = chain;
 		tm.checkServerTrusted(chain, authType);
 	}
 }
