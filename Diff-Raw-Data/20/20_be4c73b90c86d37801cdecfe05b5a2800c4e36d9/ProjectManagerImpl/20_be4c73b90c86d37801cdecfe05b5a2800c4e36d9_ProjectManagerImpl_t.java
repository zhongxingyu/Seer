 package com.photon.phresco.framework.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.api.ProjectManager;
 import com.photon.phresco.service.client.api.ServiceClientConstant;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.model.Model;
 import com.phresco.pom.util.PomProcessor;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.ClientResponse;
 
 public class ProjectManagerImpl implements ProjectManager, FrameworkConstants, Constants, ServiceClientConstant {
 	
 	private static final Logger S_LOGGER= Logger.getLogger(ProjectManagerImpl.class);
 	private static boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	private Map<String, ProjectInfo> projectInfosMap = null;
 	
 	public List<ProjectInfo> discover(String customerId) throws PhrescoException {
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
 		projectInfosMap = new HashMap<String, ProjectInfo>();
 		List<ProjectInfo> projectInfos = new ArrayList<ProjectInfo>();
 	    File[] appDirs = projectsHome.listFiles();
 	    for (File appDir : appDirs) {
	        if (appDir.isDirectory()) { // Only check the folders not files
	            File[] dotPhrescoFolders = appDir.listFiles(new PhrescoFileNameFilter(FOLDER_DOT_PHRESCO));
	            if (ArrayUtils.isEmpty(dotPhrescoFolders)) {
	                throw new PhrescoException(".phresco folder not found in project " + appDir.getName());
	            }
	            File[] dotProjectFiles = dotPhrescoFolders[0].listFiles(new PhrescoFileNameFilter(PROJECT_INFO_FILE));
	            if (ArrayUtils.isEmpty(dotProjectFiles)) {
	                throw new PhrescoException("project.info file not found in .phresco of project "+dotPhrescoFolders[0].getParent());
	            }
	            fillProjects(dotProjectFiles[0], projectInfos, customerId);
	        }
 	    }
 	    
 	    Iterator<Entry<String, ProjectInfo>> iterator = projectInfosMap.entrySet().iterator();
         while (iterator.hasNext()) {
             projectInfos.add(iterator.next().getValue());
         }
 
         return projectInfos;
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
 
 	public ProjectInfo create(ProjectInfo projectInfo, ServiceManager serviceManager) throws PhrescoException {
 		if (isDebugEnabled) {
 			S_LOGGER.debug("Entering Method ProjectManagerImpl.create(ProjectInfo projectInfo)");
 		}
 		ClientResponse response = serviceManager.createProject(projectInfo);
 		
 		if (isDebugEnabled) {
 			S_LOGGER.debug("createProject response code " + response.getStatus());
 		}
 
 		if (response.getStatus() == 200) {
 			try {
 				extractArchive(response, projectInfo);
 				//TODO Define post create object and execute the corresponding technology implementation
 //				updateProjectPOM(projectInfo);
 //				if (TechnologyTypes.WIN_METRO.equalsIgnoreCase(techId)) {
 //					ItemGroupUpdater.update(projectInfo, projectPath);
 //				}
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
 
 		//TODO This needs to be moved to service
 		try {
 			List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 			Environment defaultEnv = getEnvFromService(serviceManager);
 			for (ApplicationInfo applicationInfo : appInfos) {
 				createConfigurationXml(applicationInfo.getAppDirName(), serviceManager, defaultEnv);	
 			}
 		} catch (PhrescoException e) {
 			S_LOGGER.error("Entered into the catch block of Configuration creation failed Exception" + e.getLocalizedMessage());
 			throw new PhrescoException("Configuration creation failed"+e);
 		}
 		return projectInfo;
 	}
 
 	public ProjectInfo update(ProjectInfo projectInfo, ServiceManager serviceManager) throws PhrescoException {
 		ClientResponse response = serviceManager.updateProject(projectInfo);
 		if (response.getStatus() == 200) {
 			try {
 				extractArchive(response, projectInfo);
 				//TODO Define post update object and execute the corresponding technology implementation
 //				updateProjectPOM(projectInfo);
 //				if (TechnologyTypes.WIN_METRO.equalsIgnoreCase(techId)) {
 //					ItemGroupUpdater.update(projectInfo, projectPath);
 //				}
 			} catch (FileNotFoundException e) {
 				throw new PhrescoException(e); 
 			} catch (IOException e) {
 				throw new PhrescoException(e);
 			}
 		} else if(response.getStatus() == 401){
 			throw new PhrescoException("Session expired");
 		} else {
 			throw new PhrescoException("Project updation failed");
 		}
 		
 		return null;
 	}
 
 	public boolean delete(ProjectInfo projectInfo) throws PhrescoException {
 		boolean deletionSuccess = false;
 		List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 		String projectsPath = Utility.getProjectHome();
 		for (ApplicationInfo applicationInfo : appInfos) {
 			String appDirName = applicationInfo.getAppDirName();
 			File application = new File(projectsPath + appDirName);
 			deletionSuccess = FileUtil.delete(application);
 		}
 		
 		return deletionSuccess;
 	}
 	
     private void fillProjects(File dotProjectFile, List<ProjectInfo> projectInfos, String customerId) throws PhrescoException {
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
     }
 	
 	private void extractArchive(ClientResponse response, ProjectInfo info) throws  IOException, PhrescoException {
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
 	
 	private void updateProjectPOM(ProjectInfo projectInfo) throws PhrescoException {
 		try {
 			String path = Utility.getProjectHome() + projectInfo.getProjectCode() + File.separator + POM_FILE;
 			PomProcessor processor = new PomProcessor(new File(path));
 			Model model = processor.getModel();
 			List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 			for (ApplicationInfo applicationInfo : appInfos) {
 				if (StringUtils.isNotEmpty(applicationInfo.getTechInfo().getVersion())) {
 					model.setVersion(applicationInfo.getTechInfo().getVersion());
 				}
 				if (StringUtils.isNotEmpty(applicationInfo.getPilotContent().getGroupId())) {
 					model.setGroupId(applicationInfo.getPilotContent().getGroupId());
 				}
 				if (StringUtils.isNotEmpty(applicationInfo.getPilotContent().getArtifactId())) {
 					model.setArtifactId(applicationInfo.getPilotContent().getArtifactId());
 				}
 			}
 			processor.save();
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	
 	private File createConfigurationXml(String appDirName, ServiceManager serviceManager, Environment defaultEnv) throws PhrescoException {
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
 		 builder.append(CONFIGURATION_INFO_FILE_NAME);
 
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
 }
