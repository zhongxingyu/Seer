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
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FilenameUtils;
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
 import com.photon.phresco.commons.model.CoreOption;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.Element;
 import com.photon.phresco.commons.model.PropertyTemplate;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.commons.model.SettingsTemplate;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.PhrescoFrameworkFactory;
 import com.photon.phresco.framework.actions.FrameworkBaseAction;
 import com.photon.phresco.framework.api.ActionType;
 import com.photon.phresco.framework.api.ApplicationManager;
 import com.photon.phresco.framework.commons.FrameworkUtil;
 import com.photon.phresco.plugins.util.MojoProcessor;
 import com.photon.phresco.util.ArchiveUtil;
 import com.photon.phresco.util.ArchiveUtil.ArchiveType;
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
     private List<String> deletableEnvs = new ArrayList<String>();
     
     private List<Configuration> selectedConfigurations = new ArrayList<Configuration>();
 
 	private Environment environment = null;
     private SettingsTemplate settingTemplate = null;
     private String selectedEnvirment = null;
     private String configId = null;
     private String selectedConfigId = null;
     private String selectedType = null;
     private String propType = null;
     private String selectedEnv = null;
 	private String selectedConfigname = null;
 	private String configName = null;
 	private String copyFromEnvName = null;
 	private String emailid ="";
     private String description = null;
     private String oldName = null;
     private List<String> appliesTos = null;
     private boolean errorFound = false;
 	private String configNameError = null;
 	private String configEnvError = null;
 	private String configTypeError = null;
 	private String nameError = null;
     private String typeError = null;
     private String appliesToError = null;
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
     
     private boolean flag = false;
     private List<String> versions = null;
     
     private String featureName = "";
     
     private List<String> key = new ArrayList<String>();
     private List<String> value = new ArrayList<String>();
     
     private List<String> uploadedFiles = new ArrayList<String>();
     
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
             setReqAttribute(REQ_ENVIRONMENTS, getAllEnvironments());
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
             String techId = "";
             ApplicationInfo applicationInfo = getApplicationInfo();
             if (applicationInfo != null) {
                 techId = applicationInfo.getTechInfo().getId();
             }
             List<Environment> environments = getAllEnvironments();
             List<SettingsTemplate> configTemplates = getServiceManager().getConfigTemplates(getCustomerId(), techId);
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
     
     public String saveConfiguration() {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.saveConfiguration()");
 		}
     	
     	try {
 			save(getAppConfigPath());
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
 			artifactGroup.setVersions(artifactInfos);
 			
 			artifactGroups.add(artifactGroup);
 			PhrescoDynamicLoader dynamicLoader = new PhrescoDynamicLoader(repoInfo, artifactGroups);
 			ApplicationProcessor applicationProcessor = dynamicLoader.getApplicationProcessor(className);
			applicationProcessor.postConfiguration(getApplicationInfo(), Collections.singletonList(getConfigInstance()));
 			addActionMessage(getText(ACT_SUCC_CONFIG_ADD, Collections.singletonList(getConfigName())));
 		} catch (PhrescoException e) {
 			if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.saveConfiguration()" + FrameworkUtil.getStackTraceAsString(e));
             }
 			return showErrorPopup(e, getText(EXCEPTION_CONFIGURATION_SAVE_CONFIG));
 		} catch (ConfigurationException e) {
 			return showErrorPopup(new PhrescoException(e), getText(EXCEPTION_CONFIGURATION_UPDATE_FAILS));
 		}
     	
     	return configList();
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
     		save(getGlobalSettingsPath());
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
     
     private void save(String configPath) throws ConfigurationException, PhrescoException {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.save()");
     	}
 
     	Configuration config = getConfigInstance();
     	Environment environment = getEnvironment();
     	List<Configuration> configurations = environment.getConfigurations();
     	configurations.add(config);
     	ConfigManager configManager = getConfigManager(configPath);
     	configManager.createConfiguration(environment.getName(), config);
     }
 
 	private Configuration getConfigInstance() throws PhrescoException {
 		boolean isIISServer = false;
 		SettingsTemplate configTemplate = getServiceManager().getConfigTemplate(getConfigId(), getCustomerId());
 		Properties properties = new Properties();
 		List<PropertyTemplate> propertyTemplates = new ArrayList<PropertyTemplate>();
 		if (CONFIG_FEATURES.equals(getConfigId())) {
 		    getTemplateConfigFile(propertyTemplates);
 		} else {
 		    propertyTemplates = configTemplate.getProperties();
 		}
 		for (PropertyTemplate propertyTemplate : propertyTemplates) {
 		    if (!TYPE_ACTIONS.equals(propertyTemplate.getType())) {
     		    String key = propertyTemplate.getKey();
     		    String value = getActionContextParam(key);
     		    if (TYPE_FILE.equals(propertyTemplate.getType())) {
     		        value = FilenameUtils.removeExtension(getActionContextParam("fileName"));
                 }
     		    if (REMOTE_DEPLOYMENT.equals(key) && StringUtils.isEmpty(value)) {
     		    	value = "false";
     		    }
     		    
     		    if (StringUtils.isNotEmpty(key)) {
     		        properties.setProperty(key, value);
     		    }
     		    
     		    if (CONFIG_TYPE.equals(key) && IIS_SERVER.equals(value)) {
     		    	isIISServer = true;
     		    }
     		    
     		    if (CONFIG_TYPE.equals(key)) {
     				properties.put(TYPE_VERSION, getVersion());
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
 		if (applicationInfo != null && applicationInfo.getTechInfo().getId().equals(FrameworkConstants.TECH_SITE_CORE)) {
 			properties.put(SETTINGS_TEMP_SITECORE_INST_PATH, getSiteCoreInstPath());
 		}
 		
 		if (isIISServer) {
 			properties.put(SETTINGS_TEMP_KEY_APP_NAME, getAppName());
 			properties.put(SETTINGS_TEMP_KEY_SITE_NAME, getSiteName());
 		}
 		
 		Configuration config = new Configuration(getConfigName(), getConfigType());
 		config.setDesc(getDescription());
 		config.setAppliesTo(FrameworkUtil.listToCsv(getAppliesTos()));
 		config.setProperties(properties);
 		return config;
 	}
 	
 	private void getTemplateConfigFile(List<PropertyTemplate> propertyTemplates) {
 		if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.getTemplateConfigFile()");
     	}
 		
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
             }
         } catch (Exception e) {
         	if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.getTemplateConfigFile()" + FrameworkUtil.getStackTraceAsString(e));
             }
         }
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
     	
     	if (FrameworkConstants.ADD_SETTINGS.equals(getFromPage()) || FrameworkConstants.EDIT_SETTINGS.equals(getFromPage())) {
 	    	if (CollectionUtils.isEmpty(getAppliesTos())) {
 	    		setAppliesToError(getText(ERROR_APPLIES_TO));
 	            hasError = true;
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
              	dynamicError += key + ":" + field + " is empty" + ",";
             }
              
             if (CONFIG_TYPE.equals(key)) {
              	if (StringUtils.isEmpty(getVersion())) {
              		setVersionError(getText(ERROR_CONFIG_VERSION));
              		hasError = true;
              	}
      		}
         }
         
         if (FrameworkConstants.ADD_CONFIG.equals(getFromPage()) || FrameworkConstants.EDIT_CONFIG.equals(getFromPage())) {
 	        if (techId.equals(FrameworkConstants.TECH_SITE_CORE) && StringUtils.isEmpty(siteCoreInstPath)) {
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
     		ConfigManager configManager = getConfigManager(getConfigPath());
     		if (StringUtils.isNotEmpty(getSelectedEnvirment())) {//To delete the selected environments
     			String [] deletableEnvs = getSelectedEnvirment().split(",");
         	    List<String> deletableEnvList = Arrays.asList(deletableEnvs);
         	    for (String deletableEnv : deletableEnvList) {
         	    	configManager.deleteEnvironment(deletableEnv);
     			}
         	    addActionMessage(getText(ACT_SUCC_ENV_DELETE, Collections.singletonList(getSelectedEnvirment())));
     		}
     		if (CollectionUtils.isNotEmpty(getSelectedConfigurations())) {//To delete the selected configurations
     			configManager.deleteConfigurations(getSelectedConfigurations());
     			List<String> configToDelete = new ArrayList<String>();
     			List<Configuration> selectedConfigurations = getSelectedConfigurations();
         		for (Configuration configuration : selectedConfigurations) {
         			configToDelete.add(configuration.getName());
     			}
         		String deleteableItem = StringUtils.join(configToDelete.toArray(), ", ");
         		addActionMessage(getText(ACT_SUCC_CONFIG_DELETE, Collections.singletonList(deleteableItem)));
     		}
     		
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
 			update(getAppConfigPath());
 			addActionMessage(getText(ACT_SUCC_CONFIG_UPDATE, Collections.singletonList(getConfigName())));
 		} catch (PhrescoException e) {
 			if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.updateConfiguration()" + FrameworkUtil.getStackTraceAsString(e));
             }
 			return showErrorPopup(e, getText(EXCEPTION_CONFIGURATION_UPDATE_CONFIG));
          }
 		
 		return configList();
 	}
 	
 	public String updateSettings(){
 		if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.updateSettings()");
 		}  
 		
 	    try {
 	        update(getGlobalSettingsPath());
 	        addActionMessage(getText(ACT_SUCC_SETTINGS_UPDATE, Collections.singletonList(getConfigName())));
 	    } catch (PhrescoException e) {
 	    	if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.updateSettings()" + FrameworkUtil.getStackTraceAsString(e));
             }
 	        return showErrorPopup(e, getText(EXCEPTION_CONFIGURATION_UPDATE_SETTINGS));
 	    }
 
 	    return settingsList();
 	}
     
     private void update(String configPath) {
     	if (s_debugEnabled) {
     		S_LOGGER.debug("Entering Method Configurations.update()");
 		}
     	
         try {
         	Environment env = getEnvironment();
         	ConfigManager configManager = getConfigManager(configPath);
         	Configuration config = getConfigInstance();
         	configManager.updateConfiguration(env.getName(), oldName, config);
         	
         } catch (PhrescoException e) {
         	if (s_debugEnabled) {
                 S_LOGGER.error("Entered into catch block of Configurations.update()" + FrameworkUtil.getStackTraceAsString(e));
             }
         } catch (ConfigurationException e) {
 			e.printStackTrace();
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
 			
 			SettingsTemplate settingTemplate = getSettingTemplate();
 			if (CONFIG_FEATURES.equals(settingTemplate.getId()) && (ADD_CONFIG.equals(getFromPage()) ||
 					EDIT_CONFIG.equals(getFromPage()))) {
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
 			    } else {
 			    	setReqAttribute(REQ_FEATURE_NAMES, Collections.EMPTY_LIST);
 			    }
 			    return SUCCESS;
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
 	            String appliesTo = configuration.getAppliesTo();
 	            String [] selectedAppliesTo = appliesTo.split(",");
 	            List<String> selectedAppliesToList = Arrays.asList(selectedAppliesTo);
 	            setReqAttribute(REQ_APPLIES_TO, selectedAppliesToList);
                 Properties selectedProperties = configuration.getProperties();
                 setReqAttribute(REQ_PROPERTIES_INFO, selectedProperties);
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
     
     public String uploadFile() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Configurations.uploadFile()");
         }
 
         PrintWriter writer = null;
         try {
             byte[] byteArray = getByteArray();
             StringBuilder sb = getTargetDir();
             File file = new File(sb.toString());
             if (!file.exists()) {
                 file.mkdirs();
             }
             sb.append(File.separator);
             
             FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
             String dynamicType = getConfigTempType().toLowerCase().replaceAll("\\s", "");
             String appDirName = getApplicationInfo().getAppDirName();
             boolean needToExtract = Boolean.valueOf(frameworkUtil.getPomProcessor(appDirName).getProperty(PHRESCO_DOT_EXTRACT_DOT + dynamicType + ARCHIVE_FORMAT));
             //Check for the property in the pom.xml and then upload as said in the pom.xml
             if (needToExtract) {
                 extractTheZip(byteArray, sb.toString());
             } else {
                 uploadAsZip(byteArray, sb.toString());
             }
             
             writer = getHttpResponse().getWriter();
             writer.print(SUCCESS_TRUE);
             writer.flush();
             writer.close();
         } catch (Exception e) { //If upload fails it will be shown in UI, so no need to throw error popup
             e.printStackTrace();
             getHttpResponse().setStatus(getHttpResponse().SC_INTERNAL_SERVER_ERROR);
             writer.print(SUCCESS_FALSE);
         }
 
         return SUCCESS;
     }
     
     /**
      * To list the uploaded files
      * @return
      */
     public String listUploadedFiles() {
         try {
             File uploadedFile = new File(getTargetDir().toString());
             String[] dirs = uploadedFile.list();
             if (!ArrayUtils.isEmpty(dirs)) {
                 for (String file : dirs) {
                     uploadedFiles.add(file);
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return SUCCESS;
     }
     
     private void extractTheZip(byte[] byteArray, String targetDir) throws IOException, PhrescoException {
         StringBuilder tempPath = null;
         FileOutputStream fos = null;
         try {
           //To write the zip file inputstream in phresco temp location
             tempPath = new StringBuilder(Utility.getPhrescoTemp())
             .append(getFileName());
             fos = new FileOutputStream(tempPath.toString());
             fos.write(byteArray);
             
             //To extract the zip file from the temp location to the specified location
             ArchiveUtil.extractArchive(tempPath.toString(), targetDir, ArchiveType.ZIP);
         } catch (Exception e) {
             throw new PhrescoException(e);
         } finally {
             fos.close();
             FileUtil.delete(new File(tempPath.toString()));
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
 
     public String removeConfigFile() {
         if (s_debugEnabled) {
             S_LOGGER.debug("Entering Method Configurations.removeConfigFile()");
         }
 
         try {
             StringBuilder sb = getTargetDir()
             .append(File.separator)
             .append(FilenameUtils.removeExtension(getFileName()));
             FileUtil.delete(new File(sb.toString()));
         } catch (Exception e) {
             // TODO: handle exception
         }
 
         return SUCCESS;
     }
     
     private StringBuilder getTargetDir() throws PhrescoException, PhrescoPomException {
         String appDirName = getApplicationInfo().getAppDirName();
         FrameworkUtil frameworkUtil = FrameworkUtil.getInstance();
         String dynamicType = getConfigTempType().toLowerCase().replaceAll("\\s", "");
         String targetDir = frameworkUtil.getPomProcessor(appDirName).getProperty(PHRESCO_DOT + dynamicType + DOT_TARGET_DIR);
         StringBuilder sb = new StringBuilder(Utility.getProjectHome())
         .append(getApplicationInfo().getAppDirName())
         .append(File.separator)
         .append(targetDir);
         return sb;
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
 	    				List<String> artifactInfoIds = artifactGroupInfos.getArtifactInfoIds();
 	    				ArtifactGroup artifactGroupInfo = getServiceManager().getArtifactGroupInfo(artifactGroupInfos.getArtifactGroupId());
 	    				List<ArtifactInfo> artifactInfos = artifactGroupInfo.getVersions();
 	    				for (ArtifactInfo artifactInfo : artifactInfos) {
 							if (artifactInfoIds.contains(artifactInfo.getId())) {
 								versions.add(artifactInfo.getVersion());
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
 	    				List<ArtifactInfo> artifactInfos = artifactGroupInfo.getVersions();
 	    				for (ArtifactInfo artifactInfo : artifactInfos) {
 							if (artifactInfoIds.contains(artifactInfo.getId())) {
 								versions.add(artifactInfo.getVersion());
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
     
     public String getConfigName() {
 		return configName;
 	}
 
 	public void setConfigName(String configName) {
 		this.configName = configName;
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
 
 	public String getEnvName() {
         return envName;
     }
 
     public void setEnvName(String envName) {
         this.envName = envName;
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
 
 	public String getAppliesToError() {
 		return appliesToError;
 	}
 
 	public void setAppliesToError(String appliesToError) {
 		this.appliesToError = appliesToError;
 	}
 
 	public List<String> getAppliesTos() {
 		return appliesTos;
 	}
 
 	public void setAppliesTos(List<String> appliesTos) {
 		this.appliesTos = appliesTos;
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
 }
