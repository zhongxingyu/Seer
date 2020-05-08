 /*
  * ###
  * Framework Web Archive
  * %%
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * %%
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
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.MapUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.photon.phresco.api.ApplicationProcessor;
 import com.photon.phresco.api.ConfigManager;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroupInfo;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.CertificateInfo;
 import com.photon.phresco.commons.model.CoreOption;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.Element;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.PropertyTemplate;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.commons.model.SettingsTemplate;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.framework.api.ActionType;
 import com.photon.phresco.framework.api.ApplicationManager;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.impl.ConfigManagerImpl;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.Constants;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.PhrescoDynamicLoader;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.exception.PhrescoPomException;
 
 public class Configurations extends FrameworkBaseAction {
     private static final long serialVersionUID = -4883865658298200459L;
     
     private static final Logger S_LOGGER = Logger.getLogger(Configurations.class);
     private static Boolean s_debugEnabled  = S_LOGGER.isDebugEnabled();
     
     private List<Environment> environments = new ArrayList<Environment>();
     private List<Environment> environmentsInfo = new ArrayList<Environment>();
     private List<String> deletableEnvs = new ArrayList<String>();
     
     private List<Configuration> selectedConfigurations = new ArrayList<Configuration>();
 
 	private Environment environment = null;
     private SettingsTemplate settingTemplate = null;
     private String selectedEnvirment = "";
     private String configId = "";
     private String selectedConfigId = "";
     private String selectedType = "";
     private String propType = "";
     private String selectedEnv = "";
 	private String selectedConfigname = "";
 	private String configName = "";
 	private String copyFromEnvName = "";
 	private String emailid ="";
     private String description = "";
     private String oldName = "";
     private boolean errorFound = false;
 	private String configNameError = "";
 	private String configEnvError = "";
 	private String configTypeError = null;
 	private String nameError = null;
     private String typeError = null;
     private String portError = null;
     private String dynamicError = "";
     private String envName = null;
     private String configType = null;
     private String oldConfigType = null;
 	private String envError = null;
 	private String emailError = null;
 	private String currentEnvName = null;
 	private String currentConfigType = null;
 	private String currentConfigName = null;
 	private String currentConfigDesc = null;
     private String appName = "";
 	private String siteName = "";
 	private String siteCoreInstPath = "";
     private String appNameError = null;
     private String versionError = null;
     private String siteNameError = null;
     private String siteCoreInstPathError = null;
     private boolean connectionAlive = false;
 	private String fromPage = null;
     private String configPath = null;
     private String url = "";
     private String version = "";
     private boolean remoteDeployment;
     private String schedulerKey = "";
     
     private boolean flag = false;
     private List<String> versions = null;
     
     private String featureName = "";
     
     private List<String> key = new ArrayList<String>();
     private List<String> value = new ArrayList<String>();
     
     private List<String> uploadedFiles = new ArrayList<String>();
     
     private String configTemplateType = "";
     private String propName = "";
     private String oldEnvName = "";
     private String csvFiles = "";
     
 	public String configList() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method Configurations.configList()");
 		}
         
     	try {
     	    removeSessionAttribute(getAppId() + SESSION_APPINFO);//To remove the appInfo from the session
     	    setReqAttribute(REQ_FROM_PAGE, REQ_CONFIG);
     	    setReqAttribute(REQ_CONFIG_PATH, getAppConfigPath().replace(File.separator, FORWARD_SLASH));
             String cloneConfigStatus = getReqParameter(CLONE_CONFIG_STATUS); 
             if (cloneConfigStatus != null) {
             	addActionMessage(getText(ENV_CLONE_SUCCESS));
             }
         } catch (PhrescoException e) {
         	 if (s_debugEnabled) {
                  S_LOGGER.error("Entered into catch block of Configurations.configList()" + FrameworkUtil.getStackTraceAsString(e));
              }
         	return showErrorPopup(e,  getText(EXCEPTION_CONFIGURATION_LIST_ENV));
         }
         
         return APP_LIST;
     }
 	
 	public String settingsList() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method Configurations.settingsList()");
 		}
 		
 		try {
 			setConfigPath(getGlobalSettingsPath().replace(File.separator, FORWARD_SLASH));
 			List<Environment> environments = getAllEnvironments();
     	    setReqAttribute(REQ_FROM_PAGE, REQ_SETTINGS);
     	    setReqAttribute(REQ_SETTINGS_PATH, getGlobalSettingsPath().replace(File.separator, FORWARD_SLASH));
     	    setReqAttribute(REQ_ENVIRONMENTS, environments);
             String cloneConfigStatus = getHttpRequest().getParameter(CLONE_CONFIG_STATUS); 
             if (cloneConfigStatus != null) {
             	addActionMessage(getText(ENV_CLONE_SUCCESS));
             }
         } catch (PhrescoException e) {
         	if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.settingsList()" + FrameworkUtil.getStackTraceAsString(e));
             }
         	return showErrorPopup(e,  getText(EXCEPTION_SETTINGS_LIST_ENV));
         } catch (ConfigurationException e) {
         	return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_SETTINGS_LIST_CONFIG));
 		} 
         
         return APP_LIST;
 	}
 
 	public String envList() {
 		if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.envList()");
 		}  
         
     	try {
     	    List<Environment> environments = getAllEnvironments();
     	    setReqAttribute(REQ_FROM_PAGE, fromPage);
             setReqAttribute(REQ_ENVIRONMENTS, environments);
             String cloneConfigStatus = getHttpRequest().getParameter(CLONE_CONFIG_STATUS); 
             if (cloneConfigStatus != null) {
             	addActionMessage(getText(ENV_CLONE_SUCCESS));
             }
         } catch (PhrescoException e) {
         	if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.envList()" + FrameworkUtil.getStackTraceAsString(e));
             }
         	return showErrorPopup(e,  getText(EXCEPTION_CONFIGURATION_READ_ENV_LIST));
         } catch (ConfigurationException e) {
         	  return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_CONFIGURATION_ENV_LIST));
 		}
         
         return APP_ENV_LIST;
     }
 	
     /**
      * @return 
      * @throws PhrescoException
      * @throws ConfigurationException
      */
 	private List<Environment> getAllEnvironments() throws PhrescoException, ConfigurationException {
 		ConfigManager configManager = getConfigManager(getConfigPath());
 		return configManager.getEnvironments();
 	}
 
 	public String openEnvironmentPopup() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method Configurations.openEnvironmentPopup()");
 		}
 		
         try {
         	List<Technology> archeTypes = getServiceManager().getArcheTypes(getCustomerId());
     		setReqAttribute(REQ_ALL_TECHNOLOGIES, archeTypes);
             setReqAttribute(REQ_ENVIRONMENTS, getAllEnvironments());
             setReqAttribute(REQ_FROM_PAGE,  getFromPage());
             setReqAttribute(REQ_CONFIG_PATH, getConfigPath());
         } catch (PhrescoException e) {
         	if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.openEnvironmentPopup()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_CONFIGURATION_OPEN_ENV_POPUP));
         } catch (ConfigurationException e) {
             return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_CONFIGURATION_UPDATE_FAILS));
         }
         return APP_ENVIRONMENT;
     }
     
 
     /**
      * @return
      * @throws PhrescoException
      */
     protected ConfigManager getConfigManager(String configPath) throws PhrescoException {
         File appDir = new File(configPath);
         return PhrescoFrameworkFactory.getConfigManager(appDir);
     }
     
     public String add() {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.add()");
     	}
     	
         try {
             removeDonotCheckInDir();
             String techId = "";
             ApplicationInfo applicationInfo = getApplicationInfo();
             if (applicationInfo != null) {
                 techId = applicationInfo.getTechInfo().getId();
             }
             List<Environment> environments = getAllEnvironments();
             List<SettingsTemplate> configTemplates = getServiceManager().getConfigTemplates(getCustomerId(), techId);
             if (CollectionUtils.isNotEmpty(configTemplates)) {
             	Collections.sort(configTemplates, sortTypeByNameInAlphaOrder());
             }
             setReqAttribute(REQ_SETTINGS_TEMPLATES, configTemplates);
             setReqAttribute(REQ_ENVIRONMENTS, environments);
             setReqAttribute(REQ_FROM_PAGE, getFromPage());
             setReqAttribute(REQ_CONFIG_PATH, getConfigPath());
         } catch (PhrescoException e) {
         	if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.add()" + FrameworkUtil.getStackTraceAsString(e));
             }
             return showErrorPopup(e, getText(EXCEPTION_CONFIGURATION_ADD));
         } catch (ConfigurationException e) {
         	return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_CONFIGURATION_UPDATE_FAILS));
         }
 
         return APP_CONFIG_ADD;
     }
     
     private Comparator sortTypeByNameInAlphaOrder() {
 		return new Comparator() {
 		    public int compare(Object firstObject, Object secondObject) {
 		    	SettingsTemplate configTemplate1 = (SettingsTemplate) firstObject;
 		    	SettingsTemplate configTemplate2 = (SettingsTemplate) secondObject;
 		       return configTemplate1.getName().compareToIgnoreCase(configTemplate2.getName());
 		    }
 		};
 	}
     
     public String saveConfiguration() {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.saveConfiguration()");
 		}
     	
     	try {
 			copyUploadedFilesToProj();
 			save(getAppConfigPath(), CONFIGURATION);
 			String pluginInfoFile = getPluginInfoPath();
 			MojoProcessor mojoProcessor = new MojoProcessor(new File(pluginInfoFile));
 			String className = mojoProcessor.getApplicationHandler().getClazz();
 			Customer customer = getServiceManager().getCustomer(getCustomerId());
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
 			applicationProcessor.postConfiguration(getApplicationInfo(), Collections.singletonList(getConfigInstance(getAppConfigPath(), CONFIGURATION)));
 			addActionMessage(getText(ACT_SUCC_CONFIG_ADD, Collections.singletonList(getConfigName())));
 		} catch (PhrescoException e) {
 			if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.saveConfiguration()" + FrameworkUtil.getStackTraceAsString(e));
             }
 			return showErrorPopup(e, getText(EXCEPTION_CONFIGURATION_SAVE_CONFIG));
 		} catch (ConfigurationException e) {
 		    if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.saveConfiguration()" + FrameworkUtil.getStackTraceAsString(e));
             }
 			return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_CONFIGURATION_UPDATE_FAILS));
 		}
     	
     	return configList();
     }
     
     private void copyUploadedFilesToProj() {
         StringBuilder srcSb = new StringBuilder(Utility.getPhrescoTemp())
         .append(File.separator)
         .append(DO_NOT_CHECKIN_DIR);
         File srcDir = new File(srcSb.toString());
         try {
             if (srcDir.exists()) {
                 StringBuilder destSb = new StringBuilder(getApplicationHome())
                 .append(File.separator)
                 .append(DO_NOT_CHECKIN_DIR);
                 File destDir = new File(destSb.toString());
                 if (destDir.exists()) {
                     File[] destFiles = destDir.listFiles();
                     File[] srcFiles = srcDir.listFiles();
                     for (File srcFile : srcFiles) {
                         for (File destFile : destFiles) {
                             if (srcFile.getName().equalsIgnoreCase(destFile.getName())) {
                                 srcSb.append(File.separator)
                                 .append(srcFile.getName())
                                 .append(File.separator);
                                 srcDir = new File(srcSb.toString());
 
                                 destSb.append(File.separator)
                                 .append(destFile.getName());
                                 destDir = new File(destSb.toString());
                                 break;
                             }
                         }
                         break;
                     }
                 }
                 FileUtils.copyDirectory(srcDir, destDir);
             }
         } catch (Exception e) {
             // TODO: handle exception
         } finally {
             if (srcDir.exists()) {
                 FileUtil.delete(srcDir);
             }
         }
     }
     
     private String getPluginInfoPath() throws PhrescoException {
     	StringBuilder builder = new StringBuilder(Utility.getProjectHome());
     	builder.append(getApplicationInfo().getAppDirName());
     	builder.append(File.separator);
     	builder.append(FOLDER_DOT_PHRESCO);
     	builder.append(File.separator);
     	builder.append(Constants.APPLICATION_HANDLER_INFO_FILE);
     	return builder.toString();
     }
     
     public String saveSettings() {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.saveSettings()");
 		}
     	try {
     		save(getGlobalSettingsPath(), SETTINGS);
     		addActionMessage(getText(ACT_SUCC_SETTINGS_ADD, Collections.singletonList(getConfigName())));
 		} catch (PhrescoException e) {
 			if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.saveSettings()" + FrameworkUtil.getStackTraceAsString(e));
             }
 			return showErrorPopup(e, getText(EXCEPTION_CONFIGURATION_SAVE_SETTINGS));
 		} catch (ConfigurationException e) {
 			return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_CONFIGURATION_UPDATE_FAILS));
 		}
     	
     	return settingsList();
     }
     
     private void save(String configPath, String fromPage) throws ConfigurationException, PhrescoException {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.save()");
     	}
 
     	Configuration config = getConfigInstance(configPath, fromPage);
     	Environment environment = getEnvironment();
     	List<Configuration> configurations = environment.getConfigurations();
     	configurations.add(config);
     	ConfigManager configManager = getConfigManager(configPath);
     	configManager.createConfiguration(environment.getName(), config);
     }
 
 	private Configuration getConfigInstance(String configPath, String fromPage) throws PhrescoException {
 			boolean isIISServer = false;
 			Properties properties = new Properties();
 			List<PropertyTemplate> propertyTemplates = new ArrayList<PropertyTemplate>();
 			if (CONFIG_FEATURES.equals(getConfigId()) || CONFIG_COMPONENTS.equals(getConfigId())) {
 			    setEnvName(getEnvironment().getName());
 			    propertyTemplates = getPropTemplateFromConfigFile();
 			    properties.setProperty(REQ_FEATURE_NAME, getActionContextParam(REQ_FEATURE_NAME));
 			} else if(!REQ_CONFIG_TYPE_OTHER.equals(getConfigType())) {
 				SettingsTemplate configTemplate = getServiceManager().getConfigTemplate(getConfigId(), getCustomerId());
 				propertyTemplates = configTemplate.getProperties();
 			}
 			
 			if (CollectionUtils.isNotEmpty(propertyTemplates)) {
 				for (PropertyTemplate propertyTemplate : propertyTemplates) {
 					if (!TYPE_ACTIONS.equals(propertyTemplate.getType())) {
 						String key = propertyTemplate.getKey();
 						String value = getActionContextParam(key);
 						if (TYPE_FILE.equals(propertyTemplate.getType())) {
 						    if (StringUtils.isNotEmpty(getCsvFiles())) {
 						        Map<String, List<String>> fileNamesMap = new HashMap<String, List<String>>();
 						        String[] csvSplits = getCsvFiles().split(Constants.STR_COMMA);
 						        for (String csvSplit : csvSplits) {
 						            String[] splits = csvSplit.split(SEPARATOR_SEP);
 						            String propName = splits[0];
 						            String fileName = splits[1];
 						            if (fileNamesMap.containsKey(propName)) {
 						                List<String> list = fileNamesMap.get(propName);
 						                list.add(fileName);
 						                fileNamesMap.put(propName, list);
 						            } else {
 						                fileNamesMap.put(propName, Collections.singletonList(fileName));
 						            }
                                 }
 						        StringBuilder sb =  new StringBuilder();
 						        if (MapUtils.isNotEmpty(fileNamesMap)) {
 						            Set<String> keySet = fileNamesMap.keySet();
 						            for (String mapKey : keySet) {
                                         List<String> fileNames = fileNamesMap.get(mapKey);
                                         for (String fileName : fileNames) {
                                             sb.append(mapKey)
                                             .append(File.separator)
                                             .append(fileName)
                                             .append(Constants.STR_COMMA);
                                         }
                                     }
 						        }
 						        key = FILES;
 						        value = sb.toString().substring(0, sb.toString().length() - 1);
 						    }
 						}
 						if (REMOTE_DEPLOYMENT.equals(key) && StringUtils.isEmpty(value)) {
 							value = "false";
 						}
 						
 						if (StringUtils.isNotEmpty(key) && !KEY_CERTIFICATE.equals(key) && StringUtils.isNotEmpty(value)) {
 							properties.setProperty(key, value);
 						} else {
 							value = getActionContextParam(key);
 							if (StringUtils.isNotEmpty(value)) {
 								File file = new File(value);
 								if(fromPage.equals(CONFIGURATION)) {
 									value = configCertificateSave(configPath, value, file);
 								} else if (fromPage.equals(SETTINGS)){
 									value = settingsCertificateSave(configPath, file);
 								}
 							properties.setProperty(key, value);
							}
 						}
 						
 						if (CONFIG_TYPE.equals(key) && IIS_SERVER.equals(value)) {
 							isIISServer = true;
 						}
 
 						if (CONFIG_TYPE.equals(key)) {
 							properties.setProperty(TYPE_VERSION, getVersion());
 						}
 					}
 				}
 			}
 			
 		//To get the custom properties
         if (CollectionUtils.isNotEmpty(getKey()) && CollectionUtils.isNotEmpty(getValue())) {
             for (int i = 0; i < getKey().size(); i++) {
                 if (StringUtils.isNotEmpty(getKey().get(i)) && StringUtils.isNotEmpty(getValue().get(i))) {
             		properties.setProperty(getKey().get(i), getValue().get(i));
                 }
             }
         }
 		
 		ApplicationInfo applicationInfo = getApplicationInfo();
 		if (applicationInfo != null && applicationInfo.getTechInfo().getId().equals(FrameworkConstants.TECH_SITE_CORE) && SERVER.equals(getConfigType())) {
 			properties.setProperty(SETTINGS_TEMP_SITECORE_INST_PATH, getSiteCoreInstPath());
 		}
 		
 		if (isIISServer) {
 			properties.setProperty(SETTINGS_TEMP_KEY_APP_NAME, getAppName());
 			properties.setProperty(SETTINGS_TEMP_KEY_SITE_NAME, getSiteName());
 		}
 		Configuration config = new Configuration(getConfigName(), getConfigType());
 		config.setDesc(getDescription());
 		config.setEnvName(getEnvironment().getName());
 		config.setProperties(properties);
 		return config;
 	}
 
 	private String settingsCertificateSave(String configPath, File file) throws PhrescoException {
 		String value = "";
 		StringBuilder sb = new StringBuilder(CERTIFICATES)
 		.append(File.separator)
 		.append(getEnvironment().getName())
 		.append(HYPHEN)
 		.append(getConfigName())
 		.append(DOT)
 		.append(FILE_TYPE_CRT);
 		value = sb.toString();					
 		if (file.exists()) {
 		File dstFile = new File(Utility.getProjectHome() + value);
 		FrameworkUtil.copyFile(file, dstFile);
 		} else {
 		saveCertificateFile(configPath, value);
 		}
 		return value;
 	}
 
 	private String configCertificateSave(String configPath, String value, File file) throws PhrescoException {
 		if (file.exists()) {
 			String path = Utility.getProjectHome().replace("\\", "/");
 			value = value.replace(path + getApplicationInfo().getAppDirName() + "/", "");
 		} else {
 			StringBuilder sb = new StringBuilder(FOLDER_DOT_PHRESCO)
 			.append(File.separator)
 			.append(CERTIFICATES)
 			.append(File.separator)
 			.append(getEnvironment().getName())
 			.append(HYPHEN)
 			.append(getConfigName())
 			.append(DOT)
 			.append(FILE_TYPE_CRT);
 			value = sb.toString();
 			saveCertificateFile(configPath, value);
 		}
 		return value;
 	}
 	
 	private void saveCertificateFile(String configPath, String certificatePath) throws PhrescoException {
 		try {
 			String host = getActionContextParam(SERVER_HOST);
 			int port = Integer.parseInt(getActionContextParam(SERVER_PORT));
 			String certificateName = getActionContextParam(KEY_CERTIFICATE);
 			ConfigManagerImpl configmanager = new ConfigManagerImpl(new File(configPath));
 			List<CertificateInfo> certificates = configmanager.getCertificate(host, port);
 			if (CollectionUtils.isNotEmpty(certificates)) {
 				for (CertificateInfo certificate : certificates) {
 					if (certificate.getDisplayName().equals(certificateName)) {
 						File file = new File(Utility.getProjectHome()+ getApplicationInfo().getAppDirName() + "/" + certificatePath);
 						configmanager.addCertificate(certificate, file);
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 	    
 	/**
      * To validate the Environment Name
      * @return
      * @throws PhrescoException 
      * @throws ConfigurationException 
      */
 	
 	public String validateEnvironment() throws PhrescoException, ConfigurationException {
 		
 		boolean hasError = false;
 		
 		List<Environment> envs = getEnvironments();
 		String envName = null;
 		for (Environment env : envs) {
 			envName= env.getName();
 		}
 		
 		if (FrameworkConstants.CONFIG.equals(getFromPage())) {
 			setConfigPath(getGlobalSettingsPath().replace(File.separator, FORWARD_SLASH));
 			List<Environment> allEnvironments = getAllEnvironments();
 			for (Environment environment : allEnvironments) {
 				if(environment.getName().equalsIgnoreCase(envName)) {
 					setConfigNameError(getText(ERROR_DUPLICATE_NAME_IN_SETTINGS));
 					hasError = true;
 				}
 			}
 		} else {
 			List<ProjectInfo> projectInfo = PhrescoFrameworkFactory.getProjectManager().discover(getCustomerId());
 			for (ProjectInfo project : projectInfo) {
 				List<ApplicationInfo> appInfos = project.getAppInfos();
 				for (ApplicationInfo applicationInfo : appInfos) {
 					StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			    	builder.append(applicationInfo.getAppDirName());
 			    	builder.append(FORWARD_SLASH);
 			    	builder.append(FOLDER_DOT_PHRESCO);
 			    	builder.append(FORWARD_SLASH);
 			    	builder.append(CONFIGURATION_INFO_FILE_NAME);
 				setConfigPath(builder.toString());
 				List<Environment> allEnvironments = getAllEnvironments();
 					for (Environment environment : allEnvironments) {
 						if(environment.getName().equalsIgnoreCase(envName)) {
 							setConfigNameError(getText(ERROR_DUPLICATE_NAME_IN_CONFIGURATIONS, Collections.singletonList(project.getName())));
 							hasError = true;
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
 	
     /**
      * To validate the form fields
      * @return
      * @throws PhrescoException 
      * @throws ConfigurationException 
      */
     public String validateConfiguration() throws PhrescoException, ConfigurationException {
     	boolean hasError = false;
     	boolean isIISServer = false;
     	boolean serverTypeValidation = false;
     	String techId = "";
     	
     	if (StringUtils.isEmpty(getConfigName().trim())) {
     		setConfigNameError(getText(ERROR_NAME));
             hasError = true;
         }
     	
     	if (StringUtils.isEmpty(getConfigType())) {
     		setConfigTypeError(getText(ERROR_CONFIG_TYPE));
             hasError = true;
         }
     	
     	if (getConfigType().equals(FrameworkConstants.EMAIL)) {
     		if (StringUtils.isEmpty(getEmailid().trim())) {
     			setEmailError(getText(ERROR_EMAIL_ID_EMPTY));
     			hasError = true; 
     		} else {
     			hasError = emailIdFormatValidation(); 
     		}
     	}
     	
     	ConfigManager configManager = getConfigManager(getConfigPath());
     	if (StringUtils.isNotEmpty(getConfigName()) && !getConfigName().equals(getOldName())) {
     		List<Configuration> configurations = configManager.getConfigurations(getEnvironment().getName(), getConfigType());
 			for (Configuration configuration : configurations) {
 				if(getConfigName().trim().equalsIgnoreCase(configuration.getName())) {
 					setConfigNameError(getText(ERROR_DUPLICATE_NAME));
 					hasError = true;
 				}
 			}
     	} 
     	
     	if (StringUtils.isEmpty(getFromPage()) || (StringUtils.isNotEmpty(getFromPage()) && !getConfigType().equals(getOldConfigType()))) {
 		    if (Constants.SETTINGS_TEMPLATE_SERVER.equals(getConfigType()) || Constants.SETTINGS_TEMPLATE_EMAIL.equals(getConfigType())) {
 	        	List<Configuration> configurations = configManager.getConfigurations(getEnvironment().getName(), getConfigType());
 	            if(CollectionUtils.isNotEmpty( configurations)) {
 	            	setConfigTypeError(getText(CONFIG_ALREADY_EXIST));
 	                hasError = true;
 	            }
 	    	}
     	}
 		    
     	if (!REQ_CONFIG_TYPE_OTHER.equals(getConfigType())) {	
 	    	SettingsTemplate configTemplate = getServiceManager().getConfigTemplate(getConfigId(), getCustomerId());
 	        List<PropertyTemplate> properties = configTemplate.getProperties();
 	        for (PropertyTemplate propertyTemplate : properties) {
 	            String key = propertyTemplate.getKey();
 	            String value = getActionContextParam(key);
 	            
 	            if (CONFIG_TYPE.equals(key) && IIS_SERVER.equals(value)) {
 	            	isIISServer = true;
 	            }
 	            
 	            if (CONFIG_TYPE.equals(key) && NODEJS_SERVER.equals(value) || NODEJS_MAC_SERVER.equals(value)) { //If nodeJs server selected , there should not be validation for deploy dir.
 	            	serverTypeValidation = true;
 	            }
 	            
 	            if(isIISServer && DEPLOY_CONTEXT.equals(key)){
 	            	propertyTemplate.setRequired(false);
 	            }
 	            
 	        	if (FrameworkConstants.ADD_CONFIG.equals(getFromPage()) || FrameworkConstants.EDIT_CONFIG.equals(getFromPage())) {
 	        		ApplicationInfo applicationInfo = getApplicationInfo();
 	            	techId = applicationInfo.getTechInfo().getId();
 		    		if (applicationInfo != null && techId.equals(FrameworkConstants.TECH_SITE_CORE)) {
 		    			if (techId.equals(FrameworkConstants.TECH_SITE_CORE) && DEPLOY_DIR.equals(key)) {
 		        			propertyTemplate.setRequired(false);
 		        		}
 		    		}
 	        	}
 	    		
 				if ((serverTypeValidation && DEPLOY_DIR.equals(key))) {
 					 propertyTemplate.setRequired(false);
 				}
 	    		 
 				// validation for UserName & Password for RemoteDeployment
 				boolean isRequired = propertyTemplate.isRequired();
 				if (isRemoteDeployment()) {
 				    if (ADMIN_USERNAME.equals(key) || ADMIN_PASSWORD.equals(key)) {
 				    	isRequired = true;
 				    }
 				    if (DEPLOY_DIR.equals(key)) {
 				    	isRequired = false;
 				    }
 				}
 	
 				if (isRequired && StringUtils.isEmpty(value)) {
 	             	String field = propertyTemplate.getName();
 	             	dynamicError += key + Constants.STR_COLON + field + PROP_TEMP_MISSING + Constants.STR_COMMA;
 	            }
 	             
 	            if (CONFIG_TYPE.equals(key)) {
 	             	if (StringUtils.isEmpty(getVersion())) {
 	             		setVersionError(getText(ERROR_CONFIG_VERSION));
 	             		hasError = true;
 	             	}
 	     		}
 	        }
 	        
 	        if (FrameworkConstants.ADD_CONFIG.equals(getFromPage()) || FrameworkConstants.EDIT_CONFIG.equals(getFromPage())) {
 		        if (techId.equals(FrameworkConstants.TECH_SITE_CORE) && StringUtils.isEmpty(siteCoreInstPath) && SERVER.equals(getConfigType())) {
 		        	setSiteCoreInstPathError(getText(ERROR_SITE_CORE_PATH_MISSING));
 		    		hasError = true;
 		    	}
 	        }
 	        
 	    	if (isIISServer) {
 	        	if (StringUtils.isEmpty(getAppName())) {
 	        		setAppNameError(getText(ERROR_CONFIG_APP_NAME ));
 	        		 hasError = true;
 	        	}
 	        	if (StringUtils.isEmpty(getSiteName())) {
 	        		setSiteNameError(getText(ERROR_CONFIG_SITE_NAME));
 	        		 hasError = true;
 	        	}
 	        }
     	}
 	        
         if (StringUtils.isNotEmpty(dynamicError)) {
 	        dynamicError = dynamicError.substring(0, dynamicError.length() - 1);
 	        setDynamicError(dynamicError);
 	        hasError = true;
 	   	}
         
         if (hasError) {
             setErrorFound(true);
         }
     	
         return SUCCESS;
     }
     
     private boolean emailIdFormatValidation() {
 		if (StringUtils.isNotEmpty(getEmailid())) {
 			Pattern p = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
 			Matcher m = p.matcher(getEmailid());
 			boolean b = m.matches();
 			if (!b) {
 				setEmailError(getText(ERROR_EMAIL_ID));
 				return true;
 			}
 		}
 		
 		return false;
 	}
     
     /*private void saveCertificateFile(String path) throws PhrescoException {
     	try {
     		String host = (String) getHttpRequest().getParameter(SERVER_HOST);
 			int port = Integer.parseInt(getHttpRequest().getParameter(SERVER_PORT));
 			String certificateName = (String)getHttpRequest().getParameter("certificate");
 			ProjectAdministrator administrator = PhrescoFrameworkFactory.getProjectAdministrator();
 			List<CertificateInfo> certificates = administrator.getCertificate(host, port);
 			if (CollectionUtils.isNotEmpty(certificates)) {
 				for (CertificateInfo certificate : certificates) {
 					if (certificate.getDisplayName().equals(certificateName)) {
 						administrator.addCertificate(certificate, new File(Utility.getProjectHome() + projectCode + "/" + path));
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
     }*/
     
     public String createEnvironment() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entered into Configurations.createEnvironment()");
         }
         
     	try {
     	    ConfigManager configManager = getConfigManager(getConfigPath());
 			configManager.addEnvironments(getEnvironments());
 			addActionMessage(getText(ACT_SUCC_ENV_ADD));
     	} catch(Exception e) {
     		if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.createEnvironment()" + FrameworkUtil.getStackTraceAsString(e));
             }
     	    return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_CONFIGURATION_CREATE_ENVIRONMENT));
     	}
     	return envList();
     }
     
     public String delete() {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.delete()");
     	}
     	
     	try {
     		boolean defaultEnv = false;
     		List<Environment> environments = getEnvironmentsInfo();
     		ConfigManager configManager = getConfigManager(getConfigPath());
     		String msg = "";
     		List<String> deletedEnvs = new ArrayList<String>(environments.size());
     		for (Environment environment : environments) {
     			defaultEnv = environment.isDefaultEnv();
         		if(!defaultEnv) { // deleteable env
         	    	configManager.deleteEnvironment(environment.getName());
         	    	deletedEnvs.add(environment.getName());
         		} else { // default env
         			msg = msg + " " + getText(ACT_ERR_CONFIG_DEFAULT_ENV, Collections.singletonList(environment.getName()));
         		}
 			}
     		
     		if (CollectionUtils.isNotEmpty(deletedEnvs)) {
     			String deletedEnv = StringUtils.join(deletedEnvs.toArray(), ", ");
     			msg = msg + " " + getText(ACT_SUCC_ENV_DELETE, Collections.singletonList(deletedEnv));
     		}
     		
     		if (CollectionUtils.isNotEmpty(getSelectedConfigurations())) {//To delete the selected configurations
     			configManager.deleteConfigurations(getSelectedConfigurations());
     			List<String> configToDelete = new ArrayList<String>();
     			List<Configuration> selectedConfigurations = getSelectedConfigurations();
         		for (Configuration configuration : selectedConfigurations) {
         			configToDelete.add(configuration.getName());
     			}
         		String deleteableItem = StringUtils.join(configToDelete.toArray(), ", ");
         		msg = msg +  " " + getText(ACT_SUCC_CONFIG_DELETE, Collections.singletonList(deleteableItem));
     		}
     		addActionMessage(msg);
     	} catch(Exception e) {
     		if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.delete()" + FrameworkUtil.getStackTraceAsString(e));
             }
     		return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_CONFIGURATION_DELETE_ENVIRONMENT));
     	}
     	return envList();
     }
     
 	public String edit() {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.edit()");
     	}
     	
         try {
             removeDonotCheckInDir();
         	List<Environment> environments = getAllEnvironments();
         	setReqAttribute(REQ_ENVIRONMENTS, environments);
         	ConfigManager configManager = getConfigManager(getConfigPath());
         	Configuration selectedConfigInfo = configManager.getConfiguration(currentEnvName, 
         			currentConfigType, currentConfigName);
         	String techId = "";
             ApplicationInfo applicationInfo = getApplicationInfo();
             if (applicationInfo != null) {
                 techId = applicationInfo.getTechInfo().getId();
             }
         	List<SettingsTemplate> configTemplates = getServiceManager().getConfigTemplates(getCustomerId(), techId);
             setReqAttribute(REQ_SETTINGS_TEMPLATES, configTemplates);
         	setReqAttribute(REQ_CONFIG_INFO, selectedConfigInfo);
         	setReqAttribute(REQ_FROM_PAGE, getFromPage());
         } catch (PhrescoException e) {
         	if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.edit()" + FrameworkUtil.getStackTraceAsString(e));
             }
         	return showErrorPopup(e, getText(EXCEPTION_CONFIGURATION_EDIT));
         } catch (ConfigurationException e) {
         	return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_CONFIGURATION_UPDATE_FAILS));
 		}
 
         getHttpRequest().setAttribute(REQ_SELECTED_MENU, APPLICATIONS);
         return APP_CONFIG_EDIT;
     }
 	
 	public String updateConfiguration(){
 		if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.updateConfiguration()");
 		}  
 		
 		try {
 			copyUploadedFilesToProj();
 			update(getAppConfigPath(), CONFIGURATION);
 			addActionMessage(getText(ACT_SUCC_CONFIG_UPDATE, Collections.singletonList(getConfigName())));
 		} catch (PhrescoException e) {
 			if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.updateConfiguration()" + FrameworkUtil.getStackTraceAsString(e));
             }
 			return showErrorPopup(e, getText(EXCEPTION_CONFIGURATION_UPDATE_CONFIG));
          }
 		
 		return configList();
 	}
 	
 	public String updateSettings() {
 		if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.updateSettings()");
 		}  
 		
 	    try {
 	        update(getGlobalSettingsPath(),SETTINGS);
 	        addActionMessage(getText(ACT_SUCC_SETTINGS_UPDATE, Collections.singletonList(getConfigName())));
 	    } catch (PhrescoException e) {
 	    	if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.updateSettings()" + FrameworkUtil.getStackTraceAsString(e));
             }
 	        return showErrorPopup(e, getText(EXCEPTION_CONFIGURATION_UPDATE_SETTINGS));
 	    }
 
 	    return settingsList();
 	}
     
     private void update(String configPath, String fromPage) {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.update()");
 		}
     	
         try {
         	Environment env = getEnvironment();
         	ConfigManager configManager = getConfigManager(configPath);
         	Configuration config = getConfigInstance(configPath, fromPage);
         	configManager.updateConfiguration(env.getName(), oldName, config);
         	
         } catch (PhrescoException e) {
         	if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.update()" + FrameworkUtil.getStackTraceAsString(e));
             }
         } catch (ConfigurationException e) {
 
         }
     }
     
     public String showProperties() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method Configurations.showProperties()");
 		}
 		
 		try {
 			
 			ApplicationInfo appInfo = getApplicationInfo();
 			if(appInfo != null && CollectionUtils.isNotEmpty(appInfo.getSelectedServers())) {
 				List<ArtifactGroupInfo> selectedServers = appInfo.getSelectedServers();
 				List<String> appinfoServers = new ArrayList<String>();
 				for (ArtifactGroupInfo selectedServerInfo : selectedServers) {
 					String serverArtifactGroupId = selectedServerInfo.getArtifactGroupId();
 					ArtifactGroup serverArtifactGroup = getServiceManager().getArtifactGroupInfo(serverArtifactGroupId);
 					appinfoServers.add(serverArtifactGroup.getName());
 				}
 				setReqAttribute(REQ_APPINFO_SERVERS, appinfoServers);
 			}
 			
 			if(appInfo != null && CollectionUtils.isNotEmpty(appInfo.getSelectedDatabases())) {
 				List<ArtifactGroupInfo> selectedDbs = appInfo.getSelectedDatabases();
 				List<String> appinfoDbs = new ArrayList<String>();
 				for (ArtifactGroupInfo selectedDbInfo : selectedDbs) {
 					String dbArtifactGroupId = selectedDbInfo.getArtifactGroupId();
 					ArtifactGroup dbArtifactGroup = getServiceManager().getArtifactGroupInfo(dbArtifactGroupId);
 					appinfoDbs.add(dbArtifactGroup.getName());
 				}
 				setReqAttribute(REQ_APPINFO_DBASES, appinfoDbs);
 			}
 			
 			if (REQ_CONFIG_TYPE_OTHER.equals(getSelectedType())) {
 				othersType();
 				return SETTINGS_TYPE;
 			}
 			
 			SettingsTemplate settingTemplate = getSettingTemplate();
 			if ((ADD_CONFIG.equals(getFromPage()) || EDIT_CONFIG.equals(getFromPage()))) {
 			    if (CONFIG_FEATURES.equals(settingTemplate.getId())) {
 			        setCustomModNamesInReq(appInfo);
 			        return SUCCESS;
 			    } else if (CONFIG_COMPONENTS.equals(settingTemplate.getId())) {
 			        setComponentNamesInReq(appInfo);
 			        return SUCCESS;
 			    }
 			}
             setReqAttribute(REQ_SETTINGS_TEMPLATE, settingTemplate);
 		    List<PropertyTemplate> properties = getSettingTemplate().getProperties();
             setReqAttribute(REQ_PROPERTIES, properties);
             setReqAttribute(REQ_APPINFO, appInfo);
             List<Element> possibleTypes = getSettingTemplate().getPossibleTypes();
             List<String> typeValues = new ArrayList<String>();
             for (Element possibleType : possibleTypes) {
             	typeValues.add(possibleType.getName());
 			}
             
             ConfigManager configManager = getConfigManager(getConfigPath());
             Configuration configuration = configManager.getConfiguration(getSelectedEnv(), getSelectedType(), getSelectedConfigname());
             
             if (configuration != null) {
                 Properties selectedProperties = configuration.getProperties();
                 setReqAttribute(REQ_PROPERTIES_INFO, selectedProperties);
             }
             if ((ADD_CONFIG.equals(getFromPage()) || EDIT_CONFIG.equals(getFromPage()))) {
             	Technology technology = getServiceManager().getTechnology(getTechId());
                 setReqAttribute(REQ_TECH_OPTIONS, technology.getOptions());
             }
             setReqAttribute(REQ_FROM_PAGE, getFromPage());
             setReqAttribute(REQ_TYPE_VALUES, typeValues);
             setReqAttribute(REQ_SELECTED_TYPE, getSelectedType());
 		} catch (PhrescoException e) {
 			if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.showProperties()" + FrameworkUtil.getStackTraceAsString(e));
             }
         	return showErrorPopup(e,  getText(EXCEPTION_CONFIGURATION_SHOW_PROPERTIES));
 		} catch (ConfigurationException e) {
 			if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.showProperties()" + FrameworkUtil.getStackTraceAsString(e));
             }
         	return showErrorPopup(new PhrescoException(e),  getText(EXCEPTION_CONFIGURATION_SHOW_PROPERTIES));
 		}
 		return SETTINGS_TYPE;
 	}
     
     private void othersType () {
 		try {
 			PropertyTemplate propertyTemplate = new PropertyTemplate();
 	    	propertyTemplate.setType(REQ_CONFIG_TYPE_OTHER);
 	    	List<PropertyTemplate> properties = new ArrayList<PropertyTemplate>();
 	    	properties.add(propertyTemplate);
 	    	setReqAttribute(REQ_PROPERTIES, properties);
 	    	setReqAttribute(REQ_FROM_PAGE, getFromPage());
 	    	setReqAttribute(REQ_SELECTED_TYPE, getSelectedType());
 			
 			ConfigManager configManager = getConfigManager(getConfigPath());
 			Configuration configuration = configManager.getConfiguration(getSelectedEnv(), getSelectedType(), getSelectedConfigname());
 			if (configuration != null) {
 				Properties selectedProperties = configuration.getProperties();
 				setReqAttribute(REQ_PROPERTIES_INFO, selectedProperties);
 			}
 		} catch (PhrescoException e) {
 			
 		} catch (ConfigurationException e) {
 			
 		}
     }
     
     private void setCustomModNamesInReq(ApplicationInfo appInfo) {
         try {
             List<String> selectedModules = appInfo.getSelectedModules();
             if (CollectionUtils.isNotEmpty(selectedModules)) {
                 List<String> custFeatureNames = new ArrayList<String>();
                 for (String selectedModule : selectedModules) {
                     ArtifactInfo artifactInfo = getServiceManager().getArtifactInfo(selectedModule);
                     ArtifactGroup artifactGroup = getServiceManager().getArtifactGroupInfo(artifactInfo.getArtifactGroupId());
                     List<CoreOption> appliesTo = artifactGroup.getAppliesTo();
                     for (CoreOption coreOption : appliesTo) {
                         if (coreOption.getTechId().equals(appInfo.getTechInfo().getId()) && !coreOption.isCore()) {
                             custFeatureNames.add(artifactGroup.getName());
                         }
                     }
                 }
                 setReqAttribute(REQ_SELECTED_TYPE, getSelectedType());
                 setReqAttribute(REQ_FEATURE_NAMES, custFeatureNames);
             }
         } catch (Exception e) {
             // TODO: handle exception
         }
     }
     
     private void setComponentNamesInReq(ApplicationInfo appInfo) {
         try {
             List<String> selectedComponents = appInfo.getSelectedComponents();
             if (CollectionUtils.isNotEmpty(selectedComponents)) {
                 List<String> componentNames = new ArrayList<String>();
                 for (String selectedComponent : selectedComponents) {
                     ArtifactInfo artifactInfo = getServiceManager().getArtifactInfo(selectedComponent);
                     ArtifactGroup artifactGroup = getServiceManager().getArtifactGroupInfo(artifactInfo.getArtifactGroupId());
                     List<CoreOption> appliesTo = artifactGroup.getAppliesTo();
                     for (CoreOption coreOption : appliesTo) {
                         if (coreOption.getTechId().equals(appInfo.getTechInfo().getId())) {
                             componentNames.add(artifactGroup.getName());
                         }
                     }
                 }
                 setReqAttribute(REQ_SELECTED_TYPE, getSelectedType());
                 setReqAttribute(REQ_FEATURE_NAMES, componentNames);
             }
         } catch (Exception e) {
             // TODO: handle exception
         }
     }
     
     public String showFeatureConfigs() throws PhrescoException {
         try {
             setConfigTemplateType(CONFIG_FEATURES);
             setReqAttribute(REQ_FEATURE_NAME, getFeatureName());
             List<PropertyTemplate> propertyTemplates = getPropTemplateFromConfigFile();
             setReqAttribute(REQ_PROPERTIES, propertyTemplates);
             setReqAttribute(REQ_SELECTED_TYPE, getSelectedType());
         } catch (PhrescoException e) {
 //          return showErrorPopup(e, getText(EXCEPTION_FEATURE_MANIFEST_NOT_AVAILABLE));
         }
         
         return SUCCESS;
     }
     
     private List<PropertyTemplate> getPropTemplateFromConfigFile() throws PhrescoException {
         List<PropertyTemplate> propertyTemplates = new ArrayList<PropertyTemplate>();
         try {
             List<Configuration> featureConfigurations = getApplicationProcessor().preConfiguration(getApplicationInfo(), getFeatureName(), getEnvName());
             Properties properties = null;
             if (CollectionUtils.isNotEmpty(featureConfigurations)) {
                 for (Configuration featureConfiguration : featureConfigurations) {
                     properties = featureConfiguration.getProperties();
                     Set<Object> keySet = properties.keySet();
                     for (Object key : keySet) {
                         String keyStr = (String) key;
                         String dispName = keyStr.replace(".", " ");
                         PropertyTemplate propertyTemplate = new PropertyTemplate();
                         propertyTemplate.setKey(keyStr);
                         propertyTemplate.setName(dispName);
                         propertyTemplates.add(propertyTemplate);
                     }
                 }
             }
             setReqAttribute(REQ_HAS_CUSTOM_PROPERTY, true);
             setReqAttribute(REQ_PROPERTIES_INFO, properties);
         } catch (PhrescoException e) {
             throw new PhrescoException(e);
         }
 
         return propertyTemplates;
     }
     
     public String uploadFile() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Configurations.uploadFile()");
         }
 
         PrintWriter writer = null;
         try {
             byte[] byteArray = getByteArray();
             if (getTargetDir() == null) {
                 ConfigManager configManager = getConfigManager(getAppConfigPath());
                 List<Configuration> configurations = configManager.getConfigurations(getEnvName(), getConfigTempType());
                 boolean isNameExists = false;
                 boolean needNameValidation = true;
                 if (StringUtils.isEmpty(getOldName()) && FrameworkConstants.EDIT_CONFIG.equals(getFromConfig())) {
                     oldName = getConfigName();
                 }
                 if (getConfigName().equalsIgnoreCase(getOldName())) {
                     needNameValidation = false;
                 }
                 if (CollectionUtils.isNotEmpty(configurations) && needNameValidation) {
                     for (Configuration configuration : configurations) {
                         if (getConfigName().trim().equalsIgnoreCase(configuration.getName())) {
                             isNameExists = true;
                             break;
                         }
                     }
                 }
                 if (!isNameExists) {
                     StringBuilder sb = new StringBuilder(Utility.getPhrescoTemp())
                     .append(File.separator)
                     .append(DO_NOT_CHECKIN_DIR)
                     .append(File.separator)
                     .append(getEnvName())
                     .append(File.separator)
                     .append(getConfigName())
                     .append(File.separator)
                     .append(getPropName());
                     File file = new File(sb.toString());
                     if (!file.exists()) {
                         file.mkdirs();
                     }
                     uploadAsZip(byteArray, sb.toString());
 
                     writer = getHttpResponse().getWriter();
                     writer.print(SUCCESS_TRUE_NAME_ERR);
                     writer.flush();
                 } else {
                     writer = getHttpResponse().getWriter();
                     writer.print(SUCCESS_FALSE_NAME_ERR);
                 }
             } else {
                 StringBuilder sb = getTargetDir();
                 File file = new File(sb.toString());
                 if (!file.exists()) {
                     file.mkdirs();
                 }
                 sb.append(File.separator);
 
                 uploadAsZip(byteArray, sb.toString());
                 writer = getHttpResponse().getWriter();
                 writer.print(SUCCESS_TRUE);
                 writer.flush();
             }
         } catch (Exception e) { //If upload fails it will be shown in UI, so no need to throw error popup
             getHttpResponse().setStatus(getHttpResponse().SC_INTERNAL_SERVER_ERROR);
             writer.print(SUCCESS_FALSE);
         } finally {
             if (writer != null) {
                 writer.close();
             }
         }
 
         return SUCCESS;
     }
     
     private StringBuilder getTargetDir() {
         StringBuilder sb = null;
         try {
             String appDirName = getApplicationInfo().getAppDirName();
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             String dynamicType = getConfigTempType().toLowerCase().replaceAll("\\s", "");
             String targetDir = frameworkUtil.getPomProcessor(appDirName).getProperty(PHRESCO_DOT + dynamicType + DOT_TARGET_DIR);
             if (StringUtils.isEmpty(targetDir)) {
                 return null;
             }
             sb = new StringBuilder(Utility.getProjectHome())
             .append(getApplicationInfo().getAppDirName())
             .append(File.separator)
             .append(targetDir);
         } catch (PhrescoException e) {
             // TODO: handle exception
         } catch (PhrescoPomException e) {
             // TODO Auto-generated catch block
         }
         
         return sb;
     }
     
     private void removeDonotCheckInDir() {
         try {
             File file = new File(Utility.getPhrescoTemp() + DO_NOT_CHECKIN_DIR);
             if (file.exists()) {
                 FileUtils.deleteDirectory(file);
             }
         } catch (Exception e) {
             // TODO: handle exception
         }
     }
     
     private void uploadAsZip(byte[] byteArray, String targetDir) throws IOException, PhrescoException {
         FileOutputStream fos = null;
         try {
             StringBuilder sb = new StringBuilder(targetDir)
             .append(File.separator)
             .append(getFileName());
             fos = new FileOutputStream(sb.toString());
             fos.write(byteArray);
         } catch (Exception e) {
            throw new PhrescoException(e);
         } finally {
             fos.close();
         }
     }
     
     /**
      * To rename the environment directory of the uploaded files 
      * @return
      */
     public String renameEnvDir() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Configurations.renameEnvDir()");
         }
         try {
             ConfigManager configManager = getConfigManager(getAppConfigPath());
             List<Configuration> configurations = configManager.getConfigurations(getEnvName(), getConfigTempType());
             boolean isNameExists = false;
             if (CollectionUtils.isNotEmpty(configurations) && !getConfigName().equals(getOldName())) {
                 for (Configuration configuration : configurations) {
                     if (getConfigName().trim().equalsIgnoreCase(configuration.getName())) {
                         isNameExists = true;
                         break;
                     }
                 }
             }
             
             if (!isNameExists) {//Move the uploaded files to the newly created environment
                 StringBuilder oldEnvSb = new StringBuilder(Utility.getPhrescoTemp())
                 .append(File.separator)
                 .append(DO_NOT_CHECKIN_DIR)
                 .append(File.separator)
                 .append(getOldEnvName());
                 File oldEnvDir = new File(oldEnvSb.toString());
                 
                 StringBuilder newEnvSb = new StringBuilder(Utility.getPhrescoTemp())
                 .append(File.separator)
                 .append(DO_NOT_CHECKIN_DIR)
                 .append(File.separator)
                 .append(getEnvName());
                 File newEnvDir = new File(newEnvSb.toString());
                 oldEnvDir.renameTo(newEnvDir);
             }
         } catch (Exception e) {
             // TODO: handle exception
         }
         return SUCCESS;
     }
     
     public String renameConfigNameDir() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Configurations.renameConfigNameDir()");
         }
         try {
             ConfigManager configManager = getConfigManager(getAppConfigPath());
             List<Configuration> configurations = configManager.getConfigurations(getEnvName(), getConfigTempType());
             boolean isNameExists = false;
             if (CollectionUtils.isNotEmpty(configurations)) {
                 for (Configuration configuration : configurations) {
                     if (getConfigName().trim().equalsIgnoreCase(configuration.getName())) {
                         isNameExists = true;
                         break;
                     }
                 }
             }
             if (!isNameExists) {
                 List<String> paths = new ArrayList<String>();
                 paths.add(Utility.getPhrescoTemp());
                 paths.add(getApplicationHome());
                 for (String path : paths) {
                     StringBuilder oldConfigSb = new StringBuilder(path)
                     .append(File.separator)
                     .append(DO_NOT_CHECKIN_DIR)
                     .append(File.separator)
                     .append(getEnvName())
                     .append(File.separator)
                     .append(getOldName());
                     File oldConfigDir = new File(oldConfigSb.toString());
                     StringBuilder newConfigSb = new StringBuilder(path)
                     .append(File.separator)
                     .append(DO_NOT_CHECKIN_DIR)
                     .append(File.separator)
                     .append(getEnvName())
                     .append(File.separator)
                     .append(getConfigName());
                     File newConfigDir = new File(newConfigSb.toString());
                     oldConfigDir.renameTo(newConfigDir);
                 }
             }
         } catch (Exception e) {
             // TODO: handle exception
         }
         
         return SUCCESS;
     }
     
     /**
      * To list the uploaded files
      * @return
      */
     public String listUploadedFiles() {
         try {
             ConfigManager configManager = getConfigManager(getAppConfigPath());
             Configuration configuration = configManager.getConfiguration(getEnvName(), getCurrentConfigType(), getConfigName());
             Properties properties = configuration.getProperties();
             String property = properties.getProperty(FILES);
             if (StringUtils.isNotEmpty(property)) {
                 String[] splits = property.split(Constants.STR_COMMA);
                 for (String split : splits) {
                     uploadedFiles.add(split);
                 }
             }
         } catch (Exception e) {
             // TODO: handle exception
         }
 
         return SUCCESS;
     }
     
     public String removeConfigFile() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Configurations.removeConfigFile()");
         }
 
         try {
             if (getTargetDir() != null) {
                 StringBuilder sb = getTargetDir()
                 .append(File.separator)
                 .append(getFileName());
                 FileUtil.delete(new File(sb.toString()));
             } else {
                 StringBuilder sb = new StringBuilder(getApplicationHome())
                 .append(File.separator)
                 .append(DO_NOT_CHECKIN_DIR)
                 .append(File.separator)
                 .append(getEnvName());
                 File envNameDir = new File(sb.toString());
                 sb.append(File.separator)
                 .append(getConfigName());
                 File configNameDir = new File(sb.toString());
                 sb.append(File.separator)
                 .append(getPropName());
                 File propNameDir = new File(sb.toString());
                 sb.append(File.separator)
                 .append(getFileName());
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
         } catch (Exception e) {
             // TODO: handle exception
         }
 
         return SUCCESS;
     }
     
     public String validateTheme() {
         try {
             StringBuilder workingDirectory = new StringBuilder(getAppDirectoryPath(getApplicationInfo()));
             ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
             BufferedReader reader = applicationManager.performAction(getProjectInfo(), ActionType.THEME_VALIDATOR, null, workingDirectory.toString());
             setSessionAttribute(getAppId() + VALIDATE_THEME, reader);
             setReqAttribute(REQ_APP_ID, getAppId());
             setReqAttribute(REQ_ACTION_TYPE, VALIDATE_THEME);
         } catch (Exception e) {
             // TODO: handle exception
         }
         
         return APP_ENVIRONMENT_READER;
     }
 
     public String validateContent() {
         try {
             StringBuilder workingDirectory = new StringBuilder(getAppDirectoryPath(getApplicationInfo()));
             ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
             BufferedReader reader = applicationManager.performAction(getProjectInfo(), ActionType.CONTENT_VALIDATOR, null, workingDirectory.toString());
             setSessionAttribute(getAppId() + VALIDATE_CONTENT, reader);
             setReqAttribute(REQ_APP_ID, getAppId());
             setReqAttribute(REQ_ACTION_TYPE, VALIDATE_CONTENT);
         } catch (Exception e) {
             // TODO: handle exception
         }
         
         return APP_ENVIRONMENT_READER;
     }
 
     public String convertTheme() {
         try {
             StringBuilder workingDirectory = new StringBuilder(getAppDirectoryPath(getApplicationInfo()));
             ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
             BufferedReader reader = applicationManager.performAction(getProjectInfo(), ActionType.THEME_CONVERTOR, null, workingDirectory.toString());
             setSessionAttribute(getAppId() + CONVERT_THEME, reader);
             setReqAttribute(REQ_APP_ID, getAppId());
             setReqAttribute(REQ_ACTION_TYPE, CONVERT_THEME);
         } catch (Exception e) {
             // TODO: handle exception
         }
         
         return APP_ENVIRONMENT_READER;
     }
 
     public String convertContent() {
         try {
             StringBuilder workingDirectory = new StringBuilder(getAppDirectoryPath(getApplicationInfo()));
             ApplicationManager applicationManager = PhrescoFrameworkFactory.getApplicationManager();
             BufferedReader reader = applicationManager.performAction(getProjectInfo(), ActionType.CONTENT_CONVERTOR, null, workingDirectory.toString());
             setSessionAttribute(getAppId() + CONVERT_CONTENT, reader);
             setReqAttribute(REQ_APP_ID, getAppId());
             setReqAttribute(REQ_ACTION_TYPE, CONVERT_CONTENT);
         } catch (Exception e) {
             // TODO: handle exception
         }
         
         return APP_ENVIRONMENT_READER;
     }
     
     public String cloneConfigPopup() {
     	if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Configurations.cloneConfigPopup()");
 		}
     	
     	try {
     		List<Environment> environments = getAllEnvironments();
     		setReqAttribute(REQ_ENVIRONMENTS, environments);
     		setReqAttribute(CLONE_FROM_CONFIG_NAME, configName);
     		setReqAttribute(CLONE_FROM_ENV_NAME, envName);
     		setReqAttribute(CLONE_FROM_CONFIG_TYPE, configType);
     		setReqAttribute(CLONE_FROM_CONFIG_DESC, currentConfigDesc);
 		} catch (Exception e) {
 			if (s_debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Configurations.cloneConfigPopup()" + FrameworkUtil.getStackTraceAsString(e));
 	    	}
 		}
     	return SUCCESS;
     }
 
 	public String cloneConfiguration() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method Configurations.cloneConfiguration()");
 		}
 		
 		try {
 			
 			boolean configExists = isConfigExists(currentEnvName, configType, configName);
 			if (!configExists) { // false to create
 				ConfigManager configManager = getConfigManager(getConfigPath());
 				List<Configuration> configurations = configManager.getConfigurations(copyFromEnvName, configType);
 				Configuration cloneconfig = null;
 				for (Configuration configuration : configurations) {
 					if (configuration.getName().equals(getConfigName())) { // old configuration
 						cloneconfig = configuration;
 						break;
 					}
 				}
 				//new configuration
 				cloneconfig.setName(currentConfigName);
 				cloneconfig.setDesc(currentConfigDesc);
 				configManager.createConfiguration(currentEnvName, cloneconfig);
 				flag = true;
 				addActionMessage(getText(ACT_SUCC_CONFIG_CLONE, Collections.singletonList(getConfigName())));
 			} else {
         		addActionMessage(getText(ACT_ERR_CONFIG_CLONE_EXISTS));
 				flag = false;
 			}
 		} catch (Exception e) {
 			flag = false;
 			setEnvError(getText(CONFIGURATION_CLONNING_FAILED));
 		}
 		return  envList();
 	}
 	
 	public boolean isConfigExists(String envName, String configType, String cloneFromConfigName) throws PhrescoException {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method Configurations.isConfigExists()");
 		}
 		
 	    try {
 	    	ConfigManager configManager = getConfigManager(getConfigPath());
 	        if (configType.equals(Constants.SETTINGS_TEMPLATE_SERVER) || configType.equals(Constants.SETTINGS_TEMPLATE_EMAIL)) {
 	            List<Configuration> configurations = configManager.getConfigurations(envName, configType);
 	            if(CollectionUtils.isNotEmpty( configurations)) {
 	                return true;
 	            }
 	        } else {
 	            List<Configuration> configurations = configManager.getConfigurations(envName, configType);
 	            for (Configuration configuration : configurations) {
 	            	String oldConfigName = configuration.getName();
 	            	if (oldConfigName.equals(getConfigName())) { 
 	            		 return true;
 	            	}
 				}
 	        }
 	        return false;
 	    } catch (Exception e) {
 	    	if (s_debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Configurations.isConfigExists()" + FrameworkUtil.getStackTraceAsString(e));
 	    	}
 	        throw new PhrescoException(e);
 	    }
 	}
 	
     public String fetchProjectInfoVersions() {
     	if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method Configurations.fetchProjectInfoVersions()");
 		}
     	
     	try {
     		versions = new ArrayList<String>();
     		ApplicationInfo appInfo = getApplicationInfo();
     		if (SERVER.equals(getSelectedType())) {
     			if(appInfo != null && CollectionUtils.isNotEmpty(appInfo.getSelectedServers())) {
 	    			List<ArtifactGroupInfo> selectedServers = appInfo.getSelectedServers();
 	    			for (ArtifactGroupInfo artifactGroupInfos : selectedServers) {
 	    				List<String> appInfoArtifactInfoIds = artifactGroupInfos.getArtifactInfoIds();
 	    				ArtifactGroup artifactGroupInfo = getServiceManager().getArtifactGroupInfo(artifactGroupInfos.getArtifactGroupId());
 	    				if (artifactGroupInfo.getName().equals(getPropType())) {
 	    					List<ArtifactInfo> artifactInfos = artifactGroupInfo.getVersions();
 	    					for (ArtifactInfo artifactInfo : artifactInfos) {
 	    						if (appInfoArtifactInfoIds.contains(artifactInfo.getId())) {
 	    							versions.add(artifactInfo.getVersion());
 	    						}
 	    					}
 	    				}
 					}
 				}
     		}
     		
     		if (DATABASE.equals(getSelectedType())) {
     			if(appInfo != null && CollectionUtils.isNotEmpty(appInfo.getSelectedDatabases())) {
 	    			List<ArtifactGroupInfo> selectedDatabases = appInfo.getSelectedDatabases();
 	    			for (ArtifactGroupInfo artifactGroupInfos : selectedDatabases) {
 	    				List<String> artifactInfoIds = artifactGroupInfos.getArtifactInfoIds();
 	    				ArtifactGroup artifactGroupInfo = getServiceManager().getArtifactGroupInfo(artifactGroupInfos.getArtifactGroupId());
 	    				if (artifactGroupInfo.getName().equals(getPropType())) {
 	    					List<ArtifactInfo> artifactInfos = artifactGroupInfo.getVersions();
 	    					for (ArtifactInfo artifactInfo : artifactInfos) {
 	    						if (artifactInfoIds.contains(artifactInfo.getId())) {
 	    							versions.add(artifactInfo.getVersion());
 	    						}
 	    					}
 	    				}
 					}
 				}
     		}
     	} catch (PhrescoException e) {
     		if (s_debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Configurations.fetchProjectInfoVersions()" + FrameworkUtil.getStackTraceAsString(e));
 	    	}
     	}
     	return SUCCESS;
     }
     
     public String fetchSettingProjectInfoVersions() {
     	if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method Configurations.fetchSettingProjectInfoVersions()");
 		}
     	
     	try {
     		versions = new ArrayList<String>();
     		if (DATABASE.equals(getSelectedType())) {
 	    		List<DownloadInfo> downloads = getServiceManager().getDownloads(getCustomerId());
 	    		for (DownloadInfo downloadInfo : downloads) {
 	    			ArtifactGroup artifactGroup = downloadInfo.getArtifactGroup();
 	    			if (getPropType().equals(downloadInfo.getName())) {
 	    				List<ArtifactInfo> artifactInfos = artifactGroup.getVersions();
 	    				for (ArtifactInfo artifactInfo : artifactInfos) {
 							versions.add(artifactInfo.getVersion());
 						}
 	    			}
 				}
     		}
     		
     		if (SERVER.equals(getSelectedType())) {
 	    		List<DownloadInfo> downloads = getServiceManager().getDownloads(getCustomerId());
 	    		for (DownloadInfo downloadInfo : downloads) {
 	    			ArtifactGroup artifactGroup = downloadInfo.getArtifactGroup();
 	    			if (getPropType().equals(downloadInfo.getName())) {
 	    				List<ArtifactInfo> artifactInfos = artifactGroup.getVersions();
 	    				for (ArtifactInfo artifactInfo : artifactInfos) {
 							versions.add(artifactInfo.getVersion());
 						}
 	    			}
 				}
     		}
     	} catch (PhrescoException e) {
     		if (s_debugEnabled) {
 				S_LOGGER.error("Entered into catch block of Configurations.fetchSettingProjectInfoVersions()" + FrameworkUtil.getStackTraceAsString(e));
 	    	}
     	}
     	
     	return SUCCESS;
     }
 
     public String connectionAliveCheck() {
 		if (s_debugEnabled) {
 			S_LOGGER.debug("Entering Method  Configurations.connectionAliveCheck()");
 		}
 		try {
 			connectionAlive = false;
 			String[] results = url.split(",");
 			String lprotocol = results[0];
 			String lhost = results[1];
 			int lport = Integer.parseInt(results[2]);
 			boolean tempConnectionAlive = isConnectionAlive(lprotocol, lhost, lport);
 			connectionAlive = tempConnectionAlive == true ? true : false;
 		} catch (Exception e) {
         	if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.connectionAliveCheck()" + FrameworkUtil.getStackTraceAsString(e));
     		}
 			addActionError(e.getLocalizedMessage());
 		}
 		return SUCCESS;
 	}
     
     private String getGlobalSettingsPath() throws PhrescoException {
     	StringBuilder builder = new StringBuilder(Utility.getProjectHome());
     	builder.append(getCustomerId());
 		builder.append("-");
 		builder.append(SETTINGS_INFO_FILE_NAME);
 		return builder.toString();
     }
     
     private String getAppConfigPath() throws PhrescoException {
     	StringBuilder builder = new StringBuilder(Utility.getProjectHome());
     	builder.append(getApplicationInfo().getAppDirName());
     	builder.append(File.separator);
     	builder.append(FOLDER_DOT_PHRESCO);
     	builder.append(File.separator);
     	builder.append(CONFIGURATION_INFO_FILE_NAME);
     	return builder.toString();
     }
     
     public String authenticateServer() throws PhrescoException {
     	try {
     		String host = (String)getHttpRequest().getParameter(SERVER_HOST);
     		int port = Integer.parseInt(getHttpRequest().getParameter(SERVER_PORT));
     		boolean connectionAlive = Utility.isConnectionAlive("https", host, port);
     		boolean isCertificateAvailable = false;	
     		if (connectionAlive) {
     			ConfigManagerImpl configmanager = new ConfigManagerImpl(null);
     			List<CertificateInfo> certificates = configmanager.getCertificate(host, port);
     			if (CollectionUtils.isNotEmpty(certificates)) {
     				isCertificateAvailable = true;
     				setReqAttribute(CERTIFICATES, certificates);
     			}
     		}
     		setReqAttribute(FILE_TYPES, FILE_TYPE_CRT);
     		setReqAttribute(FILE_BROWSE, FILE_BROWSE);
     		String projectLocation = "";
 			if (StringUtils.isNotEmpty(getProjectId())) {
     			projectLocation = Utility.getProjectHome() + getApplicationInfo().getAppDirName();
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
 
 	public String cronExpression() {
 		setReqAttribute(REQ_SCHEDULER_KEY, schedulerKey);
 		return SUCCESS;
 	}
     
 	public String getDescription() {
    		return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getOldName() {
         return oldName;
     }
 
     public void setOldName(String oldName) {
         this.oldName = oldName;
     }
 
     public String getNameError() {
 		return nameError;
 	}
 
 	public void setNameError(String nameError) {
 		this.nameError = nameError;
 	}
 	
 	public String getTypeError() {
         return typeError;
     }
 
     public void setTypeError(String typeError) {
         this.typeError = typeError;
     }
 	
 	public String getDynamicError() {
 		return dynamicError;
 	}
 
 	public void setDynamicError(String dynamicError) {
 		this.dynamicError = dynamicError;
 	}
 
     public String getConfigType() {
 		return configType;
 	}
 
 	public void setConfigType(String configType) {
 		this.configType = configType;
 	}
 	
 	public String getEnvError() {
 		return envError;
 	}
 
 	public void setEnvError(String envError) {
 		this.envError = envError;
 	}
 	
 	public String getOldConfigType() {
 		return oldConfigType;
 	}
 
 	public void setOldConfigType(String oldConfigType) {
 		this.oldConfigType = oldConfigType;
 	}
 	
 	public String getPortError() {
 		return portError;
 	}
 
 	public void setPortError(String portError) {
 		this.portError = portError;
 	}
 	
 	public String getEmailError() {
 		return emailError;
 	}
 
 	public void setEmailError(String emailError) {
 		this.emailError = emailError;
 	}
 	
 	public String getAppName() {
 		return appName;
 	}
 
 	public void setAppName(String appName) {
 		this.appName = appName;
 	}
 
 	public String getAppNameError() {
 		return appNameError;
 	}
 
 	public void setAppNameError(String appNameError) {
 		this.appNameError = appNameError;
 	}
 
 	public String getSiteNameError() {
 		return siteNameError;
 	}
 
 	public void setSiteNameError(String siteNameError) {
 		this.siteNameError = siteNameError;
 	}
 
 	public String getSiteCoreInstPath() {
 		return siteCoreInstPath;
 	}
 
 	public void setSiteCoreInstPath(String siteCoreInstPath) {
 		this.siteCoreInstPath = siteCoreInstPath;
 	}
 
 	public String getSiteCoreInstPathError() {
 		return siteCoreInstPathError;
 	}
 
 	public void setSiteCoreInstPathError(String siteCoreInstPathError) {
 		this.siteCoreInstPathError = siteCoreInstPathError;
 	}
 
 	public boolean isFlag() {
 		return flag;
 	}
 
 	public void setFlag(boolean flag) {
 		this.flag = flag;
 	}
 	
 	public String getConfigNameError() {
 		return configNameError;
 	}
 	
 	public void setConfigNameError(String configNameError) {
 		this.configNameError = configNameError;
 	}
 	
 	public String getConfigEnvError() {
 		return configEnvError;
 	}
 
 	public void setConfigEnvError(String configEnvError) {
 		this.configEnvError = configEnvError;
 	}
 	
 	public String getConfigTypeError() {
 		return configTypeError;
 	}
 
 	public void setConfigTypeError(String configTypeError) {
 		this.configTypeError = configTypeError;
 	}
 
 	public boolean isErrorFound() {
 	    return errorFound;
 	}
 	
 	public void setErrorFound(boolean errorFound) {
 		this.errorFound = errorFound;
 	}
     
     public List<Environment> getEnvironments() {
         return environments;
     }
     
     public void setEnvironments(List<Environment> environments) {
         this.environments = environments;
     }
 
     public Environment getEnvironment() {
         return environment;
     }
 
     public void setEnvironment(Environment environment) {
         this.environment = environment;
     }
 
     public SettingsTemplate getSettingTemplate() {
         return settingTemplate;
     }
 
     public void setSettingTemplate(SettingsTemplate settingTemplate) {
         this.settingTemplate = settingTemplate;
     }
 
     public String getConfigId() {
         return configId;
     }
 
     public void setConfigId(String configId) {
         this.configId = configId;
     }
     
     public String getCurrentEnvName() {
 		return currentEnvName;
 	}
 
 	public void setCurrentEnvName(String currentEnvName) {
 		this.currentEnvName = currentEnvName;
 	}
 
 	public String getCurrentConfigType() {
 		return currentConfigType;
 	}
 
 	public void setCurrentConfigType(String currentConfigType) {
 		this.currentConfigType = currentConfigType;
 	}
 
 	public String getCurrentConfigName() {
 		return currentConfigName;
 	}
 
 	public void setCurrentConfigName(String currentConfigName) {
 		this.currentConfigName = currentConfigName;
 	}
 
 	public String getSelectedEnvirment() {
 		return selectedEnvirment;
 	}
 
 	public void setSelectedEnvirment(String selectedEnvirment) {
 		this.selectedEnvirment = selectedEnvirment;
 	}
 
 	public String getSelectedConfigId() {
 		return selectedConfigId;
 	}
 
 	public void setSelectedConfigId(String selectedConfigId) {
 		this.selectedConfigId = selectedConfigId;
 	}
 	
 	public String getSelectedType() {
 		return selectedType;
 	}
 
 	public void setSelectedType(String selectedType) {
 		this.selectedType = selectedType;
 	}
 	
 	public String getSelectedEnv() {
 		return selectedEnv;
 	}
 
 	public void setSelectedEnv(String selectedEnv) {
 		this.selectedEnv = selectedEnv;
 	}
 
 	public String getSelectedConfigname() {
 		return selectedConfigname;
 	}
 
 	public void setSelectedConfigname(String selectedConfigname) {
 		this.selectedConfigname = selectedConfigname;
 	}
 
 	public String getSiteName() {
 		return siteName;
 	}
 
 	public void setSiteName(String siteName) {
 		this.siteName = siteName;
 	}
 
 	public String getFromPage() {
 		return fromPage;
 	}
 
 	public void setFromPage(String fromPage) {
 		this.fromPage = fromPage;
 	}
 
 	public String getConfigPath() {
 		return configPath;
 	}
 
 	public void setConfigPath(String configPath) {
 		this.configPath = configPath;
 	}
 	
 	public List<String> getDeletableEnvs() {
 		return deletableEnvs;
 	}
 
 	public void setDeletableEnvs(List<String> deletableEnvs) {
 		this.deletableEnvs = deletableEnvs;
 	}
 
 	public String getCopyFromEnvName() {
 		return copyFromEnvName;
 	}
 
 	public void setCopyFromEnvName(String copyFromEnvName) {
 		this.copyFromEnvName = copyFromEnvName;
 	}
 
 	public String getCurrentConfigDesc() {
 		return currentConfigDesc;
 	}
 
 	public void setCurrentConfigDesc(String currentConfigDesc) {
 		this.currentConfigDesc = currentConfigDesc;
 	}
 
 	public List<Configuration> getSelectedConfigurations() {
 		return selectedConfigurations;
 	}
 
 	public void setSelectedConfigurations(List<Configuration> selectedConfigurations) {
 		this.selectedConfigurations = selectedConfigurations;
 	}
 	  
 	public List<String> getVersions() {
 		return versions;
 	}
 
 	public void setVersions(List<String> versions) {
 		this.versions = versions;
 	}
 	
 	public void setFeatureName(String featureName) {
         this.featureName = featureName;
     }
 
     public String getFeatureName() {
         return featureName;
     }
 
     public List<String> getKey() {
         return key;
     }
 
     public void setKey(List<String> key) {
         this.key = key;
     }
 
     public List<String> getValue() {
         return value;
     }
 
     public void setValue(List<String> value) {
         this.value = value;
     }
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 	
 	public boolean isConnectionAlive() {
 		return connectionAlive;
 	}
 	
 	public void setConnectionAlive(boolean connectionAlive) {
 		this.connectionAlive = connectionAlive;
 	}
 
 	public String getPropType() {
 		return propType;
 	}
 
 	public void setPropType(String propType) {
 		this.propType = propType;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	public String getVersionError() {
 		return versionError;
 	}
 
 	public void setVersionError(String versionError) {
 		this.versionError = versionError;
 	}
 
 	public boolean isRemoteDeployment() {
 		return remoteDeployment;
 	}
 
 	public void setRemoteDeployment(boolean remoteDeployment) {
 		this.remoteDeployment = remoteDeployment;
 	}
 
 	public String getEmailid() {
 		return emailid;
 	}
 
 	public void setEmailid(String emailid) {
 		this.emailid = emailid;
 	}
 
     public List<String> getUploadedFiles() {
         return uploadedFiles;
     }
 
     public void setUploadedFiles(List<String> uploadedFiles) {
         this.uploadedFiles = uploadedFiles;
     }
     
     public String getConfigTemplateType() {
         return configTemplateType;
     }
 
     public void setConfigTemplateType(String configTemplateType) {
         this.configTemplateType = configTemplateType;
     }
 
 	public List<Environment> getEnvironmentsInfo() {
 		return environmentsInfo;
 	}
 
 	public void setEnvironmentsInfo(List<Environment> environmentsInfo) {
 		this.environmentsInfo = environmentsInfo;
 	}
 
 	public String getSchedulerKey() {
 		return schedulerKey;
 	}
 
 	public void setSchedulerKey(String schedulerKey) {
 		this.schedulerKey = schedulerKey;
 	}
 	
 	public void setPropName(String propName) {
         this.propName = propName;
     }
 
     public String getPropName() {
         return propName;
     }
 
     public void setOldEnvName(String oldEnvName) {
         this.oldEnvName = oldEnvName;
     }
 
     public String getOldEnvName() {
         return oldEnvName;
     }
 
     public void setCsvFiles(String csvFiles) {
         this.csvFiles = csvFiles;
     }
 
     public String getCsvFiles() {
         return csvFiles;
     }
 }
