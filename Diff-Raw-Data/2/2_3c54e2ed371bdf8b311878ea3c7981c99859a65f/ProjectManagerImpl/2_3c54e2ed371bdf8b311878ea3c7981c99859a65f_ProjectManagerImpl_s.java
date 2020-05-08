 /**
  * Phresco Framework Implementation
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
 package com.photon.phresco.framework.impl;
 
 import com.photon.phresco.api.ApplicationProcessor;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.CIJob;
 import com.photon.phresco.commons.model.ContinuousDelivery;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.Dashboard;
 import com.photon.phresco.commons.model.DashboardInfo;
 import com.photon.phresco.commons.model.Dashboards;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.ProjectDelivery;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.commons.model.Widget;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.api.ActionType;
 import com.photon.phresco.framework.api.ApplicationManager;
 import com.photon.phresco.framework.api.DocumentGenerator;
 import com.photon.phresco.framework.api.ProjectManager;
 import com.photon.phresco.plugins.model.Mojos.ApplicationHandler;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.service.client.api.ServiceClientConstant;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.DashboardSearchInfo;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.PhrescoDynamicLoader;
 import com.photon.phresco.util.ProjectUtils;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 import com.phresco.pom.util.PomProcessor;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.wink.json4j.OrderedJSONObject;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.Map.Entry;
 
 import java.lang.reflect.Type;
 import java.net.MalformedURLException;
 
 import javax.ws.rs.QueryParam;
 import org.eclipse.ui.internal.handlers.WidgetMethodHandler;
 import org.json.JSONObject;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.splunk.Args;
 import com.splunk.JobResultsArgs;
 import com.splunk.Service;
 import com.splunk.ServiceArgs;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.ClientResponse;
 
 public class ProjectManagerImpl implements ProjectManager, FrameworkConstants, Constants, ServiceClientConstant {
 	
 	private static final Logger S_LOGGER= Logger.getLogger(ProjectManagerImpl.class);
 	private static boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	
 	public List<ProjectInfo> discover(String customerId) throws PhrescoException {
 		try {
 			if (isDebugEnabled) {
 				S_LOGGER.debug("Entering Method ProjectManagerImpl.discover(String CustomerId)");
 			}
 			File projectsHome = new File(Utility.getProjectHome());
 			if (isDebugEnabled) {
 				S_LOGGER.debug("discover( )  projectHome = "+projectsHome);
 			}
 			if (!projectsHome.exists()) {
 				return null;
 			}
 			Map<String, ProjectInfo> projectInfosMap = new HashMap<String, ProjectInfo>();
 			List<ProjectInfo> projectInfos = new ArrayList<ProjectInfo>();
 			File[] appDirs = projectsHome.listFiles();
 			for (File appDir : appDirs) {
 			    if (appDir.isDirectory()) { 
 			        File[] dotPhrescoFolders = appDir.listFiles(new PhrescoFileNameFilter(FOLDER_DOT_PHRESCO));
 			        if (ArrayUtils.isEmpty(dotPhrescoFolders)) {
 			        	continue;
 			        }
 			        File[] dotProjectFiles = dotPhrescoFolders[0].listFiles(new PhrescoFileNameFilter(PROJECT_INFO_FILE));
 			        if (!ArrayUtils.isEmpty(dotProjectFiles)) {
 			        	  projectInfosMap = fillProjects(dotProjectFiles[0], customerId, projectInfosMap);
 //			        	throw new PhrescoException("project.info file not found in .phresco of project " + dotPhrescoFolders[0].getParent());
 			        }
 			    }
 			}
 
 			Iterator<Entry<String, ProjectInfo>> iterator = projectInfosMap.entrySet().iterator();
 			while (iterator.hasNext()) {
 			    projectInfos.add(iterator.next().getValue());
 			}
 			return projectInfos;
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	public List<ProjectInfo> discover() throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method ProjectManagerImpl.discover()");
 		}
 
 		File projectsHome = new File(Utility.getProjectHome());
 		if (isDebugEnabled) {
 			S_LOGGER.debug("discover( )  projectHome = "+projectsHome);
 		}
 		if (!projectsHome.exists()) {
 			return null;
 		}
 		List<ProjectInfo> projectInfos = new ArrayList<ProjectInfo>();
 	    File[] appDirs = projectsHome.listFiles();
 	    for (File appDir : appDirs) {
 	        if (appDir.isDirectory()) { 
 	            File[] dotPhrescoFolders = appDir.listFiles(new PhrescoFileNameFilter(FOLDER_DOT_PHRESCO));
 	            if (ArrayUtils.isEmpty(dotPhrescoFolders)) {
 	            	continue;
 	            }
 	            File[] dotProjectFiles = dotPhrescoFolders[0].listFiles(new PhrescoFileNameFilter(PROJECT_INFO_FILE));
 	            if (!ArrayUtils.isEmpty(dotProjectFiles)) {
 	            	 fillProjects(dotProjectFiles[0], projectInfos);
 //	                throw new PhrescoException("project.info file not found in .phresco of project " + dotPhrescoFolders[0].getParent());
 	            }
 	           
 	        }
 	    }
 
         return projectInfos;
 	}
 	
 	public List<ProjectInfo> discoverFromRootModule(String rootModule) throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method ProjectManagerImpl.discover()");
 		}
 
 		File projectsHome = new File(Utility.getProjectHome());
 		if (isDebugEnabled) {
 			S_LOGGER.debug("discover( )  projectHome = "+projectsHome);
 		}
 		File rootModuleFolder = new File(projectsHome + File.separator + rootModule);
 		if (!projectsHome.exists() || !rootModuleFolder.exists()) {
 			return null;
 		}
 		List<ProjectInfo> projectInfos = new ArrayList<ProjectInfo>();
 	    File[] appDirs = rootModuleFolder.listFiles();
 	    for (File appDir : appDirs) {
 	        if (appDir.isDirectory()) { 
 	            File[] dotPhrescoFolders = appDir.listFiles(new PhrescoFileNameFilter(FOLDER_DOT_PHRESCO));
 	            if (ArrayUtils.isEmpty(dotPhrescoFolders)) {
 	            	continue;
 	            }
 	            File[] dotProjectFiles = dotPhrescoFolders[0].listFiles(new PhrescoFileNameFilter(PROJECT_INFO_FILE));
 	            if (ArrayUtils.isEmpty(dotProjectFiles)) {
 	                throw new PhrescoException("project.info file not found in .phresco of project " + dotPhrescoFolders[0].getParent());
 	            }
 	            fillProjects(dotProjectFiles[0], projectInfos);
 	        }
 	    }
 
         return projectInfos;
 	}
 	
     private void fillProjects(File dotProjectFile, List<ProjectInfo> projectInfos) throws PhrescoException {
         S_LOGGER.debug("Entering Method ProjectManagerImpl.fillProjects(File[] dotProjectFiles, List<Project> projects)");
 
         Gson gson = new Gson();
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(dotProjectFile));
             ProjectInfo projectInfo = gson.fromJson(reader, ProjectInfo.class);
             projectInfos.add(projectInfo);
         } catch (FileNotFoundException e) {
             throw new PhrescoException(e);
         } finally {
             Utility.closeStream(reader);
         }
     }
 
 	public ProjectInfo getProject(String projectId, String customerId) throws PhrescoException {
 		List<ProjectInfo> discover = discover(customerId);
 		for (ProjectInfo projectInfo : discover) {
 			if (projectInfo.getId().equals(projectId)) {
 				return projectInfo;
 			}
 		}
 		return null;
 	}
 	
 	@Override
 	public ProjectInfo getProject(String projectId, String customerId, String appId) throws PhrescoException {
 		File[] appDirs = new File(Utility.getProjectHome()).listFiles();
 	    for (File appDir : appDirs) {
 	        if (appDir.isDirectory()) { 
 	            File[] dotPhrescoFolders = appDir.listFiles(new PhrescoFileNameFilter(FOLDER_DOT_PHRESCO));
 	            if (ArrayUtils.isEmpty(dotPhrescoFolders)) {
 	            	continue;
 	            }
 	            File[] dotProjectFiles = dotPhrescoFolders[0].listFiles(new PhrescoFileNameFilter(PROJECT_INFO_FILE));
 	            ProjectInfo projectInfo = getProjectInfo(dotProjectFiles[0], projectId, customerId, appId);
 	            if (projectInfo != null) {
 	            	return projectInfo;
 	            }
 	        }
 	    }
 		
 		return null;
 	}
 
 	public ProjectInfo create(ProjectInfo projectInfo, ServiceManager serviceManager) throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method ProjectManagerImpl.create(ProjectInfo projectInfo)");
 		}
 		ClientResponse response = null;
 		try {
 			response = serviceManager.createProject(projectInfo);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 
 		if (isDebugEnabled) {
 			S_LOGGER.debug("createProject response code " + response.getStatus());
 		}
 		if (response.getStatus() == 200) {
 			try {
 				extractArchive(response, projectInfo);
 				ProjectUtils projectUtils = new ProjectUtils();
 				String customerId = projectInfo.getCustomerIds().get(0);
 				Customer customer = serviceManager.getCustomer(customerId);
 				RepoInfo repoInfo = customer.getRepoInfo();
 				List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 				for (ApplicationInfo appInfo : appInfos) {
 					String pluginInfoFile = Utility.getProjectHome() + appInfo.getAppDirName() + File.separator + DOT_PHRESCO_FOLDER +File.separator +  APPLICATION_HANDLER_INFO_FILE;
 					File path = new File(Utility.getProjectHome() + appInfo.getAppDirName());
 					projectUtils.updateTestPom(path);
 					//For Pdf Document Creation In Docs Folder
 					DocumentGenerator documentGenerator = PhrescoFrameworkFactory.getDocumentGenerator();
 					documentGenerator.generate(appInfo, path, null, serviceManager);
 					
 					MojoProcessor mojoProcessor = new MojoProcessor(new File(pluginInfoFile));
 					ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
 					if (applicationHandler != null) {
 						List<ArtifactGroup> plugins = setArtifactGroup(applicationHandler);
 						//Dynamic Class Loading
 						PhrescoDynamicLoader dynamicLoader = new PhrescoDynamicLoader(repoInfo, plugins);
 						ApplicationProcessor applicationProcessor = dynamicLoader.getApplicationProcessor(applicationHandler.getClazz());
 						applicationProcessor.postCreate(appInfo);
 					}
 					if (isCallEclipsePlugin(appInfo)) {
 						ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 						String baseDir = Utility.getProjectHome() + File.separator + appInfo.getAppDirName();
 						List<String> buildArgCmds = new ArrayList<String>();
 			            String pomFileName = Utility.getPomFileName(appInfo);
 						if(!POM_NAME.equals(pomFileName)) {
 							buildArgCmds.add(HYPHEN_F);
 							buildArgCmds.add(pomFileName);
 						}
 						 applicationManager.performAction(projectInfo, ActionType.ECLIPSE, buildArgCmds, baseDir);
 					}
 
 				}
 			} catch (FileNotFoundException e) {
 				throw new PhrescoException(e); 
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		} else if(response.getStatus() == 401){
 			throw new PhrescoException("Session expired");
 		} else {
 			throw new PhrescoException("Project creation failed");
 		}
 
 		createEnvConfigXml(projectInfo, serviceManager);
 		return projectInfo;
 	}
 	
 	private boolean isCallEclipsePlugin(ApplicationInfo appInfo) throws PhrescoException {
 		StringBuilder pomFile = new StringBuilder(Utility.getProjectHome());
 		if (StringUtils.isNotEmpty(appInfo.getRootModule())) {
 			pomFile.append(appInfo.getRootModule()).append(File.separator);
 		}
 		pomFile.append(appInfo.getAppDirName());
 		String pomName = Utility.getPhrescoPomFromWorkingDirectory(appInfo, new File(pomFile.toString()));
 		pomFile.append(File.separator).append(pomName);
 		try {
 			PomProcessor processor = new PomProcessor(new File(pomFile.toString()));
 			String eclipsePlugin = processor.getProperty(POM_PROP_KEY_PHRESCO_ECLIPSE);
 			if(StringUtils.isNotEmpty(eclipsePlugin) && TRUE.equals(eclipsePlugin)) {
 				return true;
 			}
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 		return false;
 	}
 
 	public ProjectInfo updateApplicationFeatures(ProjectInfo projectInfo, ServiceManager serviceManager, String rootModule) throws PhrescoException {
 			ClientResponse response = serviceManager.updateProject(projectInfo);
 			if (response.getStatus() == 200) {
 				File backUpProjectInfoFile = null;
 				try {
 					//application path with new app dir
 					StringBuilder newAppDirSb = new StringBuilder(Utility.getProjectHome());
 					if(StringUtils.isNotEmpty(rootModule)) {
 						newAppDirSb.append(rootModule)
 						.append(File.separator);
 					}
 					newAppDirSb.append(projectInfo.getAppInfos().get(0).getAppDirName());
 					File projectInfoFile = new File(newAppDirSb.toString());
 					StringBuilder dotPhrescoPathSb = new StringBuilder(projectInfoFile.getPath());
 					dotPhrescoPathSb.append(File.separator);
 					dotPhrescoPathSb.append(DOT_PHRESCO_FOLDER);
 					dotPhrescoPathSb.append(File.separator);
 	
 					String customerId = projectInfo.getCustomerIds().get(0);
 					Customer customer = serviceManager.getCustomer(customerId);
 					RepoInfo repoInfo = customer.getRepoInfo();
 					ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
 					
 					String pluginInfoFile = dotPhrescoPathSb.toString() + APPLICATION_HANDLER_INFO_FILE;
 					MojoProcessor mojoProcessor = new MojoProcessor(new File(pluginInfoFile));
 					ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
 					
 					//Need to remove later
 					if(StringUtils.isNotEmpty(rootModule)) {
 						appInfo.setRootModule(rootModule);
 					}
 					
 					if (applicationHandler != null) {
 						String selectedFeatures = applicationHandler.getSelectedFeatures();
 						String deletedFeatures = applicationHandler.getDeletedFeatures();
 						Gson gson = new Gson();
 						Type jsonType = new TypeToken<Collection<ArtifactGroup>>(){}.getType();
 						List<ArtifactGroup> artifactGroups = gson.fromJson(selectedFeatures, jsonType);
 						List<ArtifactGroup> deletedArtifacts = gson.fromJson(deletedFeatures, jsonType);
 						
 						List<ArtifactGroup> plugins = setArtifactGroup(applicationHandler);
 						//For Pdf Document Creation In Docs Folder
 						DocumentGenerator documentGenerator = PhrescoFrameworkFactory.getDocumentGenerator();
 						documentGenerator.generate(appInfo, projectInfoFile, artifactGroups, serviceManager);
 						// Dynamic Class Loading
 						PhrescoDynamicLoader dynamicLoader = new PhrescoDynamicLoader(repoInfo, plugins);
 						ApplicationProcessor applicationProcessor = dynamicLoader
 								.getApplicationProcessor(applicationHandler.getClazz());
 						applicationProcessor.postUpdate(appInfo, artifactGroups, deletedArtifacts);
 	
 						File projectInfoPath = new File(dotPhrescoPathSb.toString() + PROJECT_INFO_FILE);
 						ProjectUtils.updateProjectInfo(projectInfo, projectInfoPath);
 					}
 					
 					if (isCallEclipsePlugin(appInfo)) {
 						ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 						StringBuilder baseDir = new StringBuilder(Utility.getProjectHome());
 						if(StringUtils.isNotEmpty(rootModule)) {
 							baseDir.append(rootModule)
 							.append(File.separator);
 						}
 						baseDir.append(appInfo.getAppDirName());
 //						String baseDir = Utility.getProjectHome() + File.separator + appInfo.getAppDirName();
 						List<String> buildArgCmds = new ArrayList<String>();
 			            String pomFileName = Utility.getPomFileNameFromRootModule(appInfo,rootModule);
 						if(!POM_NAME.equals(pomFileName)) {
 							buildArgCmds.add(HYPHEN_F);
 							buildArgCmds.add(pomFileName);
 						}
 						applicationManager.performAction(projectInfo, ActionType.ECLIPSE, buildArgCmds, baseDir.toString());
                     }
 				} finally {
 					if(backUpProjectInfoFile!= null && backUpProjectInfoFile.exists()) {
 						FileUtil.delete(backUpProjectInfoFile);
 					}
 				}
 			} else if (response.getStatus() == 401) {
 				throw new PhrescoException("Session expired");
 			} else {
 				throw new PhrescoException("Project updation failed");
 			}
 		return projectInfo;
 	}
 	
 	
 	public ProjectInfo updateApplication(ProjectInfo projectInfo, ServiceManager serviceManager, String oldAppDirName, String rootModule) throws PhrescoException {
 			ClientResponse response = serviceManager.updateProject(projectInfo);
 			if (response.getStatus() == 200) {
 				String appDirWithModule = oldAppDirName;
 				//application path with old app dir
 				StringBuilder oldAppDirSb = new StringBuilder(Utility.getProjectHome());
 				if (StringUtils.isNotEmpty(rootModule)) {
 					oldAppDirSb.append(rootModule).append(File.separator);
 					appDirWithModule = rootModule + File.separator + oldAppDirName;
 				}
 				oldAppDirSb.append(oldAppDirName);
 				File oldDir = new File(oldAppDirSb.toString());
 				backUpProjectInfoFile(oldDir.getPath());
 				//application path with new app dir
 				StringBuilder newAppDirSb = new StringBuilder(Utility.getProjectHome());
 				if (StringUtils.isNotEmpty(rootModule)) {
 					newAppDirSb.append(rootModule).append(File.separator);
 				}
 				newAppDirSb.append(projectInfo.getAppInfos().get(0).getAppDirName());
 				File projectInfoFile = new File(newAppDirSb.toString());
 				//rename to application app dir
 				boolean renameTo = oldDir.renameTo(projectInfoFile);
 				updateProjectPom(projectInfo, rootModule);
 				updateCiInfoFile(projectInfo, appDirWithModule);
 				
 				StringBuilder dotPhrescoPathSb = new StringBuilder(projectInfoFile.getPath());
 				dotPhrescoPathSb.append(File.separator);
 				dotPhrescoPathSb.append(DOT_PHRESCO_FOLDER);
 				dotPhrescoPathSb.append(File.separator);
 				ApplicationInfo appInfo = projectInfo.getAppInfos().get(0);
 				String pluginInfoFile = dotPhrescoPathSb.toString() + APPLICATION_HANDLER_INFO_FILE;
 				createSqlFolder(appInfo, projectInfoFile, serviceManager, rootModule);
 				//For Pdf Document Creation In Docs Folder
 					DocumentGenerator documentGenerator = PhrescoFrameworkFactory.getDocumentGenerator();
 					documentGenerator.generate(appInfo, projectInfoFile, null, serviceManager);
 					if(! appInfo.getAppDirName().equals(oldAppDirName)) {
 						documentGenerator.deleteOldDocument(projectInfoFile, oldAppDirName);
 					}
 
 					File projectInfoPath = new File(dotPhrescoPathSb.toString() + PROJECT_INFO_FILE);
 					ProjectUtils.updateProjectInfo(projectInfo, projectInfoPath);
 					ProjectUtils pu = new ProjectUtils();
 					String pom = newAppDirSb.toString() + File.separator + Utility.getPhrescoPomFromWorkingDirectory(appInfo, new File(newAppDirSb.toString()));
 					pu.deletePluginFromPom(new File(pom));
 					pu.addServerPlugin(appInfo, new File(pom));
 				if (isCallEclipsePlugin(appInfo)) {
 					ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
 					StringBuilder baseDir = new StringBuilder(Utility.getProjectHome());
 					if (StringUtils.isNotEmpty(rootModule)) {
 						baseDir.append(rootModule).append(File.separator);
 					}
 					baseDir.append(appInfo.getAppDirName());
 					List<String> buildArgCmds = new ArrayList<String>();
 				    String pomFileName = Utility.getPomFileNameFromRootModule(appInfo, rootModule);
 					if(!POM_NAME.equals(pomFileName)) {
 						buildArgCmds.add(HYPHEN_F);
 						buildArgCmds.add(pomFileName);
 					}
 					applicationManager.performAction(projectInfo, ActionType.ECLIPSE, buildArgCmds, baseDir.toString());
 				}
 			} else if (response.getStatus() == 401) {
 				throw new PhrescoException("Session expired");
 			} else {
 				throw new PhrescoException("Project updation failed");
 			}
 			createEnvConfigXml(projectInfo, serviceManager);
 		return projectInfo;
 	}
 
 	private void updateCiInfoFile(ProjectInfo projectInfo, String oldDir) throws PhrescoException {
 		ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 		
 		String ciJobInfoPath = Utility.getCiJobInfoPath(oldDir);
 		File ciJobInfoFile = new File(ciJobInfoPath);
 		if(ciJobInfoFile.exists()) {
 			updateCiInfo(applicationInfo, ciJobInfoPath, ciJobInfoFile, oldDir, projectInfo.getId());
 		}
 		
 		String ciInfoPath = Utility.getCiJobInfoPath(null);
 		
 		File ciJobInfoFilePath = new File(ciInfoPath);
 		if(ciJobInfoFilePath.exists()) {
 			updateCiInfo(applicationInfo, ciInfoPath, ciJobInfoFilePath, oldDir, projectInfo.getId());
 		}
 		
 	}
 
 	private void updateCiInfo(ApplicationInfo applicationInfo, String ciJobInfoPath, File ciJobInfoFile, String oldDir, String projectId) throws PhrescoException {
 		List<ProjectDelivery> projectDeliveries = Utility.getProjectDeliveries(ciJobInfoFile);
 		if(CollectionUtils.isNotEmpty(projectDeliveries)) {
 			for (ProjectDelivery projectDelivery : projectDeliveries) {
 				if(projectDelivery.getId().equals(projectId)) {
 					List<ContinuousDelivery> continuousDeliveries = projectDelivery.getContinuousDeliveries();
 					if(CollectionUtils.isNotEmpty(continuousDeliveries)) {
 						for (ContinuousDelivery continuousDelivery : continuousDeliveries) {
 							List<CIJob> jobs = continuousDelivery.getJobs();
 							if(CollectionUtils.isNotEmpty(jobs)) {
 								for (CIJob ciJob : jobs) {
 									if(ciJob.getAppDirName().equals(oldDir)) {
 										ciJob.setAppDirName(applicationInfo.getAppDirName());
 										ciJob.setAppName(applicationInfo.getName());
 									}
 								}
 							}
 						}
 						ciInfoFileWriter(ciJobInfoPath, projectDeliveries);
 					}
 				}
 			}
 		}
 	}
 	
 	private boolean ciInfoFileWriter(String filePath, Object object ) throws PhrescoException {
 		Gson gson = new Gson();
 		FileWriter writer = null;
 		try {
 			writer = new FileWriter(filePath);
 			String json = gson.toJson(object);
 			writer.write(json);
 			writer.flush();
 			return true;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 
 	private void createEnvConfigXml(ProjectInfo projectInfo, ServiceManager serviceManager) throws PhrescoException {
 		try {
 			List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 			Environment defaultEnv = getEnvFromService(serviceManager);
 			for (ApplicationInfo applicationInfo : appInfos) {
 				createConfigurationXml(applicationInfo.getRootModule(), applicationInfo.getAppDirName(), defaultEnv);	
 			}
 		} catch (PhrescoException e) {
 			S_LOGGER.error("Entered into the catch block of Configuration creation failed Exception" + e.getLocalizedMessage());
 			throw new PhrescoException("Configuration creation failed"+e);
 		}
 	}
 	
 	private File backUpProjectInfoFile(String oldDirPath) throws PhrescoException {
 		if(StringUtils.isNotEmpty(oldDirPath)) {
 			return null;
 		}
 		StringBuilder oldDotPhrescoPathSb = new StringBuilder(oldDirPath);
 		oldDotPhrescoPathSb.append(File.separator);
 		oldDotPhrescoPathSb.append(DOT_PHRESCO_FOLDER);
 		oldDotPhrescoPathSb.append(File.separator);
 		File projectInfoFile = new File(oldDotPhrescoPathSb.toString() + PROJECT_INFO_FILE);
 		if(!projectInfoFile.exists()) {
 			return null;
 		}
 		File backUpInfoFile = new File(oldDotPhrescoPathSb.toString() + PROJECT_INFO_BACKUP_FILE);
 		try {
 			FileUtils.copyFile(projectInfoFile, backUpInfoFile);
 			return backUpInfoFile;
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private void updateProjectPom(ProjectInfo projectInfo, String rootModule) throws PhrescoException {
 		ApplicationInfo applicationInfo = projectInfo.getAppInfos().get(0);
 		StringBuilder path = new StringBuilder(Utility.getProjectHome());
 		if (StringUtils.isNotEmpty(rootModule)) {
 			path.append(rootModule).append(File.separator);
 		}
 		path.append(applicationInfo.getAppDirName()).append(File.separator);
 		path.append(Utility.getPomFileNameFromRootModule(applicationInfo, rootModule));
 		File pomFile = new File(path.toString());
 		if(!pomFile.exists()) {
 			return;
 		}
 		PomProcessor pomProcessor;
 		try {
 			pomProcessor = new PomProcessor(pomFile);
 			pomProcessor.setArtifactId(applicationInfo.getCode());
 			pomProcessor.setName(applicationInfo.getName());
 			pomProcessor.setVersion(applicationInfo.getVersion());
 			pomProcessor.save();
 		} catch (PhrescoPomException e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private List<ArtifactGroup> setArtifactGroup(ApplicationHandler applicationHandler) {
 		List<ArtifactGroup> plugins = new ArrayList<ArtifactGroup>();
 		ArtifactGroup artifactGroup = new ArtifactGroup();
 		artifactGroup.setGroupId(applicationHandler.getGroupId());
 		artifactGroup.setArtifactId(applicationHandler.getArtifactId());
 		List<ArtifactInfo> artifactInfos = new ArrayList<ArtifactInfo>();
 		ArtifactInfo artifactInfo = new ArtifactInfo();
 		artifactInfo.setVersion(applicationHandler.getVersion());
 		artifactInfos.add(artifactInfo);
 		artifactGroup.setVersions(artifactInfos);
 		plugins.add(artifactGroup);
 		return plugins;
 	}
 	@Override
 	public boolean delete(List<String> appDirNames) throws PhrescoException {
 		boolean deletionSuccess = false;
 		String projectsPath = Utility.getProjectHome();
 		for (String appDirName : appDirNames) {
 			File application = new File(projectsPath + appDirName);
 			deletionSuccess = FileUtil.delete(application);
 		}
 		return deletionSuccess;
 	}
 
 	
     private Map<String, ProjectInfo> fillProjects(File dotProjectFile, String customerId, Map<String, ProjectInfo> projectInfosMap) throws PhrescoException {
         S_LOGGER.debug("Entering Method ProjectManagerImpl.fillProjects(File[] dotProjectFiles, List<Project> projects)");
 
         Gson gson = new Gson();
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(dotProjectFile));
             ProjectInfo projectInfo = gson.fromJson(reader, ProjectInfo.class);
             if (projectInfo.getCustomerIds().get(0).equalsIgnoreCase(customerId)) {
                 ProjectInfo projectInfoInMap = projectInfosMap.get(projectInfo.getId());
                 if (projectInfoInMap != null) {
                     projectInfoInMap.getAppInfos().add(projectInfo.getAppInfos().get(0));
                     projectInfosMap.put(projectInfo.getId(), projectInfoInMap);
                 } else {
                     projectInfosMap.put(projectInfo.getId(), projectInfo);
                 }
             }
         } catch (FileNotFoundException e) {
             throw new PhrescoException(e);
         } finally {
             Utility.closeStream(reader);
         }
         return projectInfosMap;
     }
     
     private ProjectInfo getProjectInfo(File dotProjectFile, String projectId, String customerId, String appId) throws PhrescoException {
         S_LOGGER.debug("Entering Method ProjectManagerImpl.fillProjects(File[] dotProjectFiles, List<Project> projects)");
 
         Gson gson = new Gson();
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(dotProjectFile));
             ProjectInfo projectInfo = gson.fromJson(reader, ProjectInfo.class);
             if (projectInfo.getCustomerIds().get(0).equalsIgnoreCase(customerId) && projectInfo.getId().equals(projectId)) {
                 List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
                 for (ApplicationInfo applicationInfo : appInfos) {
 					if (applicationInfo.getId().equals(appId)) {
 						return projectInfo;
 					}
 				}
             }
         } catch (FileNotFoundException e) {
             throw new PhrescoException(e);
         } finally {
             Utility.closeStream(reader);
         }
         
 		return null;
     }
 	
 	private void extractArchive(ClientResponse response, ProjectInfo info) throws IOException, PhrescoException {
 		InputStream inputStream = response.getEntityInputStream();
 		FileOutputStream fileOutputStream = null;
 		String archiveHome = Utility.getArchiveHome();
 		File archiveFile = new File(archiveHome + info.getProjectCode() + ARCHIVE_FORMAT);
 		fileOutputStream = new FileOutputStream(archiveFile);
 		try {
 			byte[] data = new byte[1024];
 			int i = 0;
 			while ((i = inputStream.read(data)) != -1) {
 				fileOutputStream.write(data, 0, i);
 			}
 			fileOutputStream.flush();
 			ArchiveUtil.extractArchive(archiveFile.getPath(), Utility.getProjectHome(), ArchiveType.ZIP);
 		} finally {
 			Utility.closeStream(inputStream);
 			Utility.closeStream(fileOutputStream);
 		}
 	}
 	
 	private void createSqlFolder(ApplicationInfo appInfo, File path, ServiceManager serviceManager, String rootModule)
 	throws PhrescoException {
 		String dbName = "";
 		try {
 			StringBuilder directory = new StringBuilder(Utility.getProjectHome());
 			if (StringUtils.isNotEmpty(rootModule)) {
 				directory.append(rootModule).append(File.separator);
 			}
 			directory.append(appInfo.getAppDirName());
 			String pomName = Utility.getPhrescoPomFromWorkingDirectory(appInfo, new File(directory.toString()));
 			File pomPath = new File(directory + File.separator + pomName);
 			PomProcessor pompro = new PomProcessor(pomPath);
 			String sqlFolderPath = pompro.getProperty(POM_PROP_KEY_SQL_FILE_DIR);
 			File mysqlFolder = new File(path, sqlFolderPath + Constants.DB_MYSQL);
 			File mysqlVersionFolder = getMysqlVersionFolder(mysqlFolder);
 			File pluginInfoFile = new File(directory.toString() + File.separator
 					+ DOT_PHRESCO_FOLDER + File.separator + APPLICATION_HANDLER_INFO_FILE);
 			MojoProcessor mojoProcessor = new MojoProcessor(pluginInfoFile);
 			ApplicationHandler applicationHandler = mojoProcessor.getApplicationHandler();
 			String selectedDatabases = applicationHandler.getSelectedDatabase();
 			if (StringUtils.isNotEmpty(selectedDatabases) && StringUtils.isNotEmpty(sqlFolderPath)) {
 				Gson gson = new Gson();
 				java.lang.reflect.Type jsonType = new TypeToken<Collection<DownloadInfo>>() {
 				}.getType();
 				List<DownloadInfo> dbInfos = gson.fromJson(selectedDatabases, jsonType);
 				List<ArtifactGroupInfo> newSelectedDatabases = appInfo.getSelectedDatabases();
 				if(CollectionUtils.isNotEmpty(newSelectedDatabases)) {
 					for (ArtifactGroupInfo artifactGroupInfo : newSelectedDatabases) {
 						List<String> artifactInfoIds = artifactGroupInfo.getArtifactInfoIds();
 						for (String artifactId : artifactInfoIds) {
 							ArtifactInfo artifactInfo = serviceManager.getArtifactInfo(artifactId);
 							String selectedVersion = artifactInfo.getVersion();
 							for (DownloadInfo dbInfo : dbInfos) {
 								dbName = dbInfo.getName().toLowerCase();
 								ArtifactGroup artifactGroup = dbInfo.getArtifactGroup();
 								mySqlFolderCreation(path, dbName, sqlFolderPath, mysqlVersionFolder,selectedVersion, artifactGroup);
 							}
 						}
 					}
 				}
 			}
 		} catch (PhrescoPomException e) {
 			e.printStackTrace();
 			throw new PhrescoException(e);
 		}
 	}
 
 	private void mySqlFolderCreation(File path, String dbName, String sqlFolderPath, File mysqlVersionFolder, String selectedVersion,
 			ArtifactGroup artifactGroup) throws PhrescoException {
 		try {
 			List<ArtifactInfo> versions = artifactGroup.getVersions();
 			for (ArtifactInfo version : versions) {
 				if (selectedVersion.equals(version.getVersion())) {
 					String dbversion = version.getVersion();
 					String sqlPath = dbName + File.separator + dbversion.trim();
 					File sqlFolder = new File(path, sqlFolderPath + sqlPath);
 					sqlFolder.mkdirs();
 					if (dbName.equals(Constants.DB_MYSQL) && mysqlVersionFolder != null
 							&& !(mysqlVersionFolder.getPath().equals(sqlFolder.getPath()))) {
 						FileUtils.copyDirectory(mysqlVersionFolder, sqlFolder);
 					} else {
 						File sqlFile = new File(sqlFolder, Constants.SITE_SQL);
 						if (!sqlFile.exists()) {
 							sqlFile.createNewFile();
 						}
 					}
 				}
 			}
 		} catch (IOException e) {
 			throw new PhrescoException(e);
 		}
 	}
 	 
 	private File getMysqlVersionFolder(File mysqlFolder) {
 		File[] mysqlFolderFiles = mysqlFolder.listFiles();
 		if (mysqlFolderFiles != null && mysqlFolderFiles.length > 0) {
 			return mysqlFolderFiles[0];
 		}
 		return null;
 	}
 	
 	private File createConfigurationXml(String rootModule, String appDirName, Environment defaultEnv) throws PhrescoException {
 		if (StringUtils.isNotEmpty(rootModule)) {
 			appDirName = rootModule + File.separator + appDirName;
 		}
 		File configFile = new File(getConfigurationPath(appDirName).toString());
 		if (!configFile.exists()) {
 			createEnvironments(configFile, defaultEnv, true);
 		}
 		return configFile;
 	}
 	
 	private StringBuilder getConfigurationPath(String projectCode) {
 		 S_LOGGER.debug("Entering Method ProjectManager.getConfigurationPath(Project project)");
 		 S_LOGGER.debug("removeSettingsInfos() ProjectCode = " + projectCode);
 
 		 StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 		 builder.append(projectCode);
 		 builder.append(File.separator);
 		 builder.append(FOLDER_DOT_PHRESCO);
 		 builder.append(File.separator);
 		 builder.append(PHRESCO_ENV_CONFIG_FILE_NAME);
 		 return builder;
 	 }
 	
 	private Environment getEnvFromService(ServiceManager serviceManager) throws PhrescoException {
 		 try {
 			 return serviceManager.getDefaultEnvFromServer();
 		 } catch (ClientHandlerException ex) {
 			 S_LOGGER.error(ex.getLocalizedMessage());
 			 throw new PhrescoException(ex);
 		 }
 	 }
 	
 	private void createEnvironments(File configPath, Environment defaultEnv, boolean isNewFile) throws PhrescoException {
 		 try {
 			 ConfigurationReader reader = new ConfigurationReader(configPath);
 			 ConfigurationWriter writer = new ConfigurationWriter(reader, isNewFile);
 			 writer.createEnvironment(Collections.singletonList(defaultEnv));
 			 writer.saveXml(configPath);
 		 } catch (Exception e) {
 			 throw new PhrescoException(e);
 		 }
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
 	
 	private String getProjectPhresoFolder(String appDir) {
 		File projectDotPhr = new File(Utility.getProjectHome().concat(FORWARD_SLASH).concat(appDir).concat(FORWARD_SLASH).concat(DOT_PHRESCO_FOLDER));
 		if (projectDotPhr.exists()) {
 			return projectDotPhr.getAbsolutePath().toString();
 		}
 		return null;
 	}
 
 	
 	
 	
 	@Override
 	public String configureDashboardConfig(DashboardInfo dashboardInfo) throws PhrescoException {
 		Gson gson =new Gson();
 		Dashboards dashboards;
 		String json;
 		HashMap<String, Dashboard> dashboardMap;
 		Dashboard dashboard = new Dashboard();
 		try {
 			File dashboardInfoFile = new File(getProjectPhresoFolder(dashboardInfo.getAppdirname()).concat(FORWARD_SLASH).concat(DASHBOARD_INFO_FILE));
 			if( dashboardInfoFile.exists()) {
 				json = FileUtils.readFileToString(dashboardInfoFile);
 				dashboards = gson.fromJson(json, Dashboards.class);
 				dashboardMap = dashboards.getDashboards();
 			} else {
 				dashboards =  new Dashboards();
 				dashboards.setProjectid(dashboardInfo.getProjectid());
 				dashboards.setAppid(dashboardInfo.getAppid());
 				dashboards.setAppcode(dashboardInfo.getAppcode());
 				dashboards.setAppname(dashboardInfo.getAppname());
 				dashboardMap = new HashMap<String, Dashboard>();
 			}
 			UUID uniqueId = UUID.randomUUID();
 			String dashboardId = uniqueId.toString();
 			dashboard.setDashboardname(dashboardInfo.getDashboardname());
 			dashboard.setDatatype(dashboardInfo.getDatatype());
 			dashboard.setUsername(dashboardInfo.getUsername());
 			dashboard.setPassword(dashboardInfo.getPassword());
 			dashboard.setUrl(dashboardInfo.getUrl());
 			dashboardMap.put(dashboardId, dashboard);
 			dashboards.setDashboards(dashboardMap);
 			json = gson.toJson(dashboards, Dashboards.class);
 			FileUtils.writeStringToFile(dashboardInfoFile, json);
 			return dashboardId;
 		} catch (IOException e) {
 			e.printStackTrace();
 		} 
 		return null;
 	}
 	
 	
 
 	@Override
 	public Dashboard getDashboardConfig(String projectid, String appdirname, String dashboardid) throws PhrescoException {
 		Gson gson =new Gson();
 		Dashboards dashboards;
 		String json;
 		HashMap<String, Dashboard> dashboardMap;
 		try {
 			File dashboardInfoFile = new File(getProjectPhresoFolder(appdirname).concat(FORWARD_SLASH).concat(DASHBOARD_INFO_FILE));
 			if( dashboardInfoFile.exists()) {
 				json = FileUtils.readFileToString(dashboardInfoFile);
 				dashboards = gson.fromJson(json, Dashboards.class);
 				dashboardMap = dashboards.getDashboards();
 				if (dashboardMap.containsKey(dashboardid)) {
 					return dashboardMap.get(dashboardid);
 				}
 				return null;
 			} 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	
 	@Override
 	public boolean updateDashboardConfig(DashboardInfo dashboardInfo) throws PhrescoException {
 		Gson gson =new Gson();
 		Dashboards dashboards;
 		String json;
 		HashMap<String, Dashboard> dashboardMap;
 		Dashboard dashboard = new Dashboard();
 		try {
 			File dashboardInfoFile = new File(getProjectPhresoFolder(dashboardInfo.getAppdirname()).concat(FORWARD_SLASH).concat(DASHBOARD_INFO_FILE));
 			if( dashboardInfoFile.exists()) {
 				json = FileUtils.readFileToString(dashboardInfoFile);
 				dashboards = gson.fromJson(json, Dashboards.class);
 				dashboardMap = dashboards.getDashboards();
 				if (dashboardMap.containsKey(dashboardInfo.getDashboardid())) {
 					dashboard = dashboardMap.get(dashboardInfo.getDashboardid());
 					dashboard.setDashboardname(dashboardInfo.getDashboardname());
 					dashboard.setDatatype(dashboardInfo.getDatatype());
 					dashboard.setUsername(dashboardInfo.getUsername());
 					dashboard.setPassword(dashboardInfo.getPassword());
 					dashboard.setUrl(dashboardInfo.getUrl());
 					dashboardMap.put(dashboardInfo.getDashboardid(), dashboard);
 					dashboards.setDashboards(dashboardMap);
 					json = gson.toJson(dashboards, Dashboards.class);
 					FileUtils.writeStringToFile(dashboardInfoFile, json);
 					return true;
 				}
 				return false;
 			} 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	@Override
 	public HashMap<String, Dashboards> listAllDashboardConfig(String projectid) throws PhrescoException {
 		Gson gson =new Gson();
 		List<ProjectInfo> projectInfos = discover();
 		HashMap<String, Dashboards> appDashboards = new HashMap<String, Dashboards>();
 		if (!projectInfos.isEmpty()) {
 			for (ProjectInfo projectInfo : projectInfos) {
 				if (projectInfo.getId().equals(projectid)) {
 					try {
 						File dashboardInfoFile = new File(getProjectPhresoFolder(projectInfo.getAppInfos().get(0).getAppDirName()).concat(FORWARD_SLASH).concat(DASHBOARD_INFO_FILE));
 						if(dashboardInfoFile.exists()) {
 						String json = FileUtils.readFileToString(dashboardInfoFile);
 						Dashboards dashboards =  gson.fromJson(json, Dashboards.class);
						appDashboards.put(dashboards.getAppcode(), dashboards);
 						}
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			return appDashboards;
 		}
 		return null;
 	}
 	
 	@Override
 	public Boolean deleteDashboardConfig(DashboardInfo dashboardInfo) throws PhrescoException {
 		Gson gson =new Gson();
 		Dashboards dashboards;
 		String json;
 		HashMap<String, Dashboard> dashboardMap;
 		Dashboard dashboard = new Dashboard();
 		try {
 			File dashboardInfoFile = new File(getProjectPhresoFolder(dashboardInfo.getAppdirname()).concat(FORWARD_SLASH).concat(DASHBOARD_INFO_FILE));
 			if( dashboardInfoFile.exists()) {
 				json = FileUtils.readFileToString(dashboardInfoFile);
 				dashboards = gson.fromJson(json, Dashboards.class);
 				dashboardMap = dashboards.getDashboards();
 				if (dashboardMap.containsKey(dashboardInfo.getDashboardid())) {
 					dashboardMap.remove(dashboardInfo.getDashboardid());
 					dashboards.setDashboards(dashboardMap);
 					json = gson.toJson(dashboards, Dashboards.class);
 					FileUtils.writeStringToFile(dashboardInfoFile, json);
 					return true;
 				}
 				return false;
 			} 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	@Override
 	public String addDashboardWidgetConfig(DashboardInfo dashboardInfo) throws PhrescoException {
 		Gson gson =new Gson();
 		Dashboards dashboards;
 		String json;
 		HashMap<String, Dashboard> dashboardMap;
 		Widget widget;
 		HashMap<String, Widget> widgets;
 		HashMap<String, String[]> widProperties = new HashMap<String, String[]>();
 		HashMap<String, HashMap<String, String>> colorcodes = new HashMap<String, HashMap<String,String>>();
 
 		try {
 			File dashboardInfoFile = new File(getProjectPhresoFolder(dashboardInfo.getAppdirname()).concat(FORWARD_SLASH).concat(DASHBOARD_INFO_FILE));
 			if( dashboardInfoFile.exists()) {
 				json = FileUtils.readFileToString(dashboardInfoFile);
 				dashboards = gson.fromJson(json, Dashboards.class);
 				dashboardMap = dashboards.getDashboards();
 				if (dashboardMap.containsKey(dashboardInfo.getDashboardid())) {
 					if (dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets() != null) {
 						widgets = dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets();
 					} else {
 						widgets = new HashMap<String, Widget>();
 					}
 					UUID uniqueId = UUID.randomUUID();
 					String widgetId = uniqueId.toString();
 					widget =new Widget();
 					widget.setName(dashboardInfo.getName());
 					widget.setQuery(dashboardInfo.getQuery());
 					widget.setAutorefresh(dashboardInfo.getAutorefresh());
 					widget.setStarttime(dashboardInfo.getStarttime());
 					widget.setEndtime(dashboardInfo.getEndtime());
 					if (dashboardInfo.getProperties() != null &&  widget.getProperties() == null) {
 						widProperties = dashboardInfo.getProperties();
 					} else {
 						widProperties = widget.getProperties();
 						Set<String> keys = dashboardInfo.getProperties().keySet();  
 						for (String key : keys) {  
 							widProperties.put(key, dashboardInfo.getProperties().get(key));
 						} 
 					}
 					widget.setProperties(widProperties);
 					if (dashboardInfo.getColorcodes() != null &&  widget.getColorcodes() == null) {
 						colorcodes = dashboardInfo.getColorcodes();
 					} 
 					widget.setColorcodes(colorcodes);
 					widgets.put(widgetId, widget);
 					dashboardMap.get(dashboardInfo.getDashboardid()).setWidgets(widgets);
 					dashboards.setDashboards(dashboardMap);
 					json = gson.toJson(dashboards, Dashboards.class);
 					FileUtils.writeStringToFile(dashboardInfoFile, json);
 					return widgetId;
 				}
 				return null;
 			} 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 
 	@Override
 	public Widget getDashboardWidgetConfig(String projectid, String appdirname, String dashboardid, String widgetid) throws PhrescoException {
 		Gson gson =new Gson();
 		Dashboards dashboards;
 		String json;
 		HashMap<String, Dashboard> dashboardMap;
 		try {
 			File dashboardInfoFile = new File(getProjectPhresoFolder(appdirname).concat(FORWARD_SLASH).concat(DASHBOARD_INFO_FILE));
 			if( dashboardInfoFile.exists()) {
 				json = FileUtils.readFileToString(dashboardInfoFile);
 				dashboards = gson.fromJson(json, Dashboards.class);
 				dashboardMap = dashboards.getDashboards();
 				if (dashboardMap.containsKey(dashboardid)) {
 					if (dashboardMap.get(dashboardid).getWidgets() != null) {
 						if (dashboardMap.get(dashboardid).getWidgets().containsKey(widgetid)) {
 							return dashboardMap.get(dashboardid).getWidgets().get(widgetid);
 						} 
 					} 
 				}
 				return null;
 			} 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public Boolean updateDashboardWidgetConfig(DashboardInfo dashboardInfo) throws PhrescoException {
 		Gson gson =new Gson();
 		Dashboards dashboards;
 		String json;
 		HashMap<String, Dashboard> dashboardMap;
 		HashMap<String, String[]> widProperties = new HashMap<String, String[]>();
 		HashMap<String, HashMap<String, String>> colorcodes = new HashMap<String, HashMap<String,String>>();
 		try {
 			File dashboardInfoFile = new File(getProjectPhresoFolder(dashboardInfo.getAppdirname()).concat(FORWARD_SLASH).concat(DASHBOARD_INFO_FILE));
 			if( dashboardInfoFile.exists()) {
 				json = FileUtils.readFileToString(dashboardInfoFile);
 				dashboards = gson.fromJson(json, Dashboards.class);
 				dashboardMap = dashboards.getDashboards();
 				if (dashboardMap.containsKey(dashboardInfo.getDashboardid())) {
 					if (dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets() != null) {
 						if (dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().containsKey(dashboardInfo.getWidgetid())) {
 							dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().get(dashboardInfo.getWidgetid()).setName(dashboardInfo.getName());
 							dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().get(dashboardInfo.getWidgetid()).setQuery(dashboardInfo.getQuery());
 							dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().get(dashboardInfo.getWidgetid()).setAutorefresh(dashboardInfo.getAutorefresh());
 							dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().get(dashboardInfo.getWidgetid()).setStarttime(dashboardInfo.getStarttime());
 							dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().get(dashboardInfo.getWidgetid()).setEndtime(dashboardInfo.getEndtime());
 							if (dashboardInfo.getProperties() != null) {
 							if (dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().get(dashboardInfo.getWidgetid()).getProperties() == null) {
 								widProperties = dashboardInfo.getProperties();
 							} else {
 								widProperties = dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().get(dashboardInfo.getWidgetid()).getProperties();
 								Set<String> keys = dashboardInfo.getProperties().keySet();  
 								for (String key : keys) {  
 								    widProperties.put(key, dashboardInfo.getProperties().get(key));
 								}  
 							}
 							}
 							if (dashboardInfo.getColorcodes() != null) {
 								if (dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().get(dashboardInfo.getWidgetid()).getColorcodes() == null) {
 									colorcodes = dashboardInfo.getColorcodes();
 								} else {
 									colorcodes = dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().get(dashboardInfo.getWidgetid()).getColorcodes();
 									Set<String> keys = dashboardInfo.getColorcodes().keySet();  
 									for (String key : keys) {  
 										colorcodes.put(key, dashboardInfo.getColorcodes().get(key));
 									}  
 								}
 								}
 							dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().get(dashboardInfo.getWidgetid()).setProperties(widProperties);
 							dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().get(dashboardInfo.getWidgetid()).setColorcodes(colorcodes);
 							dashboards.setDashboards(dashboardMap);
 							json = gson.toJson(dashboards, Dashboards.class);
 							FileUtils.writeStringToFile(dashboardInfoFile, json);
 							return true;
 						} 
 					} 
 				}
 				return false;
 			} 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	@Override
 	public Dashboards listDashboardWidgetConfig(String projectid, String appdirname) throws PhrescoException {
 		Gson gson =new Gson();
 		Dashboards dashboards;
 		String json;
 		try {
 			File dashboardInfoFile = new File(getProjectPhresoFolder(appdirname).concat(FORWARD_SLASH).concat(DASHBOARD_INFO_FILE));
 			if( dashboardInfoFile.exists()) {
 				json = FileUtils.readFileToString(dashboardInfoFile);
 				dashboards = gson.fromJson(json, Dashboards.class);
 				return dashboards;
 			} 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	@Override
 	public Boolean deleteDashboardWidgetConfig(DashboardInfo dashboardInfo) throws PhrescoException {
 		Gson gson =new Gson();
 		Dashboards dashboards;
 		String json;
 		HashMap<String, Dashboard> dashboardMap;
 		HashMap<String, String[]> widProperties = new HashMap<String, String[]>();
 		try {
 			File dashboardInfoFile = new File(getProjectPhresoFolder(dashboardInfo.getAppdirname()).concat(FORWARD_SLASH).concat(DASHBOARD_INFO_FILE));
 			if( dashboardInfoFile.exists()) {
 				json = FileUtils.readFileToString(dashboardInfoFile);
 				dashboards = gson.fromJson(json, Dashboards.class);
 				dashboardMap = dashboards.getDashboards();
 				if (dashboardMap.containsKey(dashboardInfo.getDashboardid())) {
 					if (dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets() != null) {
 						if (dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().containsKey(dashboardInfo.getWidgetid())) {
 							dashboardMap.get(dashboardInfo.getDashboardid()).getWidgets().remove(dashboardInfo.getWidgetid());
 							dashboards.setDashboards(dashboardMap);
 							json = gson.toJson(dashboards, Dashboards.class);
 							FileUtils.writeStringToFile(dashboardInfoFile, json);
 							return true;
 						} 
 					} 
 				}
 				return false;
 			} 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	@Override
 	public OrderedJSONObject getdata(DashboardSearchInfo dashboardsearchinfo)
 			throws PhrescoException {
 		if(dashboardsearchinfo.getDatatype() != null && !("null".equalsIgnoreCase(dashboardsearchinfo.getDatatype())) && SPLUNK_DATATYPE.equalsIgnoreCase(dashboardsearchinfo.getDatatype())){
 			return getsplunkdata(dashboardsearchinfo);
 		}else {
 			return null;
 		}
 	}
 	
 	public OrderedJSONObject getsplunkdata(DashboardSearchInfo dashboardsearchinfo)
 			throws PhrescoException {
 		try {
 			System.out.println("Get splunk data");
 			try{
 			String arr1[] = dashboardsearchinfo.getUrl().split("//");
 			String arr2[] = arr1[1].split(":");
 			dashboardsearchinfo.setHost(arr2[0]);
 			String arr3[] = arr2[1].split("/");
 			dashboardsearchinfo.setPort(Integer.parseInt(arr3[0]));
 			} catch (Exception e) {
 				throw new PhrescoException("Exception while parsing the URL");
 			}
 			ServiceArgs loginArgs = new ServiceArgs();
 			loginArgs.setUsername(dashboardsearchinfo.getUsername());
 			loginArgs.setPassword(dashboardsearchinfo.getPassword());
 			loginArgs.setHost(dashboardsearchinfo.getHost());
 			loginArgs.setPort(dashboardsearchinfo.getPort());
 			Service service = Service.connect(loginArgs);
 			// Set the parameters for the search:
 			Args oneshotSearchArgs = new Args();
 			oneshotSearchArgs.put(DASHBOARD_RESULT_OUTPUT_MODE, JobResultsArgs.OutputMode.JSON);
 			if (dashboardsearchinfo.getEarliest_time() != null && !dashboardsearchinfo.getEarliest_time().isEmpty() ) {
 				oneshotSearchArgs.put(DASHBOARD_RESULT_EARLIEST_TIME, dashboardsearchinfo.getEarliest_time());
 				if (dashboardsearchinfo.getLatest_time() != null && !dashboardsearchinfo.getLatest_time().isEmpty() ) {
 					oneshotSearchArgs.put(DASHBOARD_RESULT_LATEST_TIME, dashboardsearchinfo.getLatest_time());
 				} 
 			} else {
 				if (dashboardsearchinfo.getLatest_time() != null && !dashboardsearchinfo.getLatest_time().isEmpty() ) {
 					throw new PhrescoException("Earliest time not set");
 				}
 			}
 			String oneshotSearchQuery = dashboardsearchinfo.getQuery();
 			System.out.println("Query changed to "+oneshotSearchQuery);
 
 			// The search results are returned directly
 			InputStream results_oneshot =  service.oneshotSearch(oneshotSearchQuery,oneshotSearchArgs);
 			String line = null;
 			String JSONResponse="";
 		    BufferedReader reader = new BufferedReader(new InputStreamReader(results_oneshot, "UTF-8"));
 		    while ((line = reader.readLine()) != null) {
 		    	JSONResponse = JSONResponse +line;
 		        System.out.println(line);
 		    }
 		    reader.close();
 		    results_oneshot.close();
 		    OrderedJSONObject search_result = new OrderedJSONObject(JSONResponse);
 		    return search_result;
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new PhrescoException("Exception occured while trying to retrieve the search result");
 		}
 	}
 	
 }
