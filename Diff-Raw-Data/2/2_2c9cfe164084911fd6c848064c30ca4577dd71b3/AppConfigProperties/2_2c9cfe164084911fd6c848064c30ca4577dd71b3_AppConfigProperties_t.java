 /*
  * Copyright 2005-2008 Pentaho Corporation.  All rights reserved. 
  * This software was developed by Pentaho Corporation and is provided under the terms 
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
  * this file except in compliance with the license. If you need a copy of the license, 
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
  * BI Platform.  The Initial Developer is Pentaho Corporation.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
  * the license for the specific language governing your rights and limitations.
  *
  * Created  
  * @author Steven Barkdull
  */
 
 package org.pentaho.pac.server.common;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.acegisecurity.providers.encoding.PasswordEncoder;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.pentaho.pac.common.config.IConsoleConfig;
 import org.pentaho.pac.server.config.ConsoleConfigXml;
 import org.pentaho.pac.server.config.HibernateSettingsXml;
 import org.pentaho.pac.server.config.PentahoObjectsConfig;
 import org.pentaho.pac.server.config.SpringSecurityHibernateConfig;
 import org.pentaho.pac.server.config.WebXml;
 import org.pentaho.pac.server.i18n.Messages;
 
 /**
  * By default, this class will initialize itself from <code>resource/config/console.xml</code> (relative to the current
  * working directory). 
  * 
  * @author Steven Barkdull
  * @author mlowery
  *
  */
 public class AppConfigProperties {
 
   // ~ Static fields/initializers ====================================================================================== 
 
   public static final String CONFIG_FILE_NAME = "console.xml"; //$NON-NLS-1$
   public static final String WEB_XML_PATH = "/WEB-INF/web.xml"; //$NON-NLS-1$
   public static final String HIBERNATE_MANAGED_XML_PATH = "/system/hibernate/hibernate-settings.xml"; //$NON-NLS-1$
   public static final String PENTAHO_OBJECTS_SPRING_XML = "/system/pentahoObjects.spring.xml"; //$NON-NLS-1$
   public static final String SPRING_SECURITY_HIBERNATE_XML = "/system/applicationContext-acegi-security-hibernate.xml" ; //$NON-NLS-1$
   public static final String JDBC_DRIVER_PATH = "./jdbc"; //$NON-NLS-1$
   public static final String KEY_BISERVER_STATUS_CHECK_PERIOD = "biserver-status-check-period"; //$NON-NLS-1$
   public static final String KEY_BISERVER_BASE_URL = "biserver-base-url"; //$NON-NLS-1$
   public static final String KEY_BISERVER_CONTEXT_PATH = "biserver-context-path"; //$NON-NLS-1$
   public static final String KEY_PLATFORM_USERNAME = "platform-username"; //$NON-NLS-1$
   public static final String DEFAULT_VALUE_PASSWORD_SERVICE_CLASS = "org.pentaho.platform.util.Base64PasswordService"; //$NON-NLS-1$
   public static final String DEFAULT_BISERVER_BASE_URL = "http://localhost:8080/pentaho"; //$NON-NLS-1$
   public static final String DEFAULT_BISERVER_CONTEXT_PATH = "/pentaho"; //$NON-NLS-1$
   public static final String DEFAULT_PLATFORM_USERNAME = "joe"; //$NON-NLS-1$
   public static final String DEFAULT_BISERVER_STATUS_CHECK_PERIOD = "30000"; //$NON-NLS-1$
   public static final String DEFAULT_HOMEPAGE_TIMEOUT = "15000"; //$NON-NLS-1$
   public static final String DEFAULT_HIBERNATE_CONFIG_PATH = "system/hibernate/hsql.hibernate.cfg.xml"; //$NON-NLS-1$
   public static final String DEFAULT_HELP_URL = "http://wiki.pentaho.com/display/ServerDoc2x/The+Pentaho+Administration+Console"; //$NON-NLS-1$
   public static final String DEFAULT_HOMEPAGE_URL = "http://www.pentaho.com/console_home"; //$NON-NLS-1$
   public static final String DEFAULT_SOLUTION_PATH = "./../biserver-ce/pentaho-solutions"; //$NON-NLS-1$ 
   public static final String DEFAULT_WAR_PATH = "./../biserver-ce/tomcat/webapps/pentaho"; //$NON-NLS-1$
 
   private IConsoleConfig consoleConfig = null;
   private HibernateSettingsXml hibernateSettingXml = null;
   private PentahoObjectsConfig pentahoObjectsConfig = null;
   private static SpringSecurityHibernateConfig springSecurityHibernateConfig = null;
 
   // ~ Instance fields =================================================================================================
   private static AppConfigProperties instance = new AppConfigProperties();
   
   private static final Log logger = LogFactory.getLog(AppConfigProperties.class);
 
   // ~ Constructors ====================================================================================================
 
   protected AppConfigProperties() {
   }
 
   // ~ Methods =========================================================================================================
 
   public static synchronized AppConfigProperties getInstance() {
     return instance;
   }
 
   public void refreshConfig() throws AppConfigException {
     consoleConfig = null;
     hibernateSettingXml = null;
     pentahoObjectsConfig = null;
     try {
       PasswordServiceFactory.init(getPasswordServiceClass());
     } catch (Exception e) {
       throw new AppConfigException(Messages.getErrorString(
           "AppConfigProperties.ERROR_0004_UNABLE_TO_READ_FILE", getSolutionPath() + PENTAHO_OBJECTS_SPRING_XML), e); //$NON-NLS-1$
     }
   }
 
   public boolean isValidConfiguration() {
 	  boolean solutionPathValid = false;
 	  boolean warPathValid = false;
 	  File solutionPathFile = new File(getSolutionPath());
 	  if (solutionPathFile != null && solutionPathFile.isDirectory()) {
 		  solutionPathValid = true;
 	  }
 	  File warPathFile = new File(getWarPath());
 	  if (warPathFile != null && warPathFile.isDirectory()) {
 		  warPathValid = true;
 	  }
 	  return solutionPathValid && warPathValid;
   }
   public PasswordEncoder getPasswordEncoder(){
     return getSpringSecurityHibernateConfig().getPasswordEncoder();
   }
   
   public String getPlatformUsername() {
     String platormUserName = getConsoleConfig().getPlatformUserName();
     if ((platormUserName == null) || (platormUserName.trim().length() == 0)) {
       platormUserName = DEFAULT_PLATFORM_USERNAME;
     }
     return platormUserName;
   }
 
   public String getBiServerContextPath() {
     String baseUrl = getBiServerBaseUrl();
     int start = baseUrl.lastIndexOf(":"); //$NON-NLS-1$
     int middle = baseUrl.indexOf("/", start); //$NON-NLS-1$
 
     String biserverContextPath = baseUrl.substring(middle, baseUrl.length() - 1);
     if (!(biserverContextPath != null && biserverContextPath.length() > 0)) {
       biserverContextPath = DEFAULT_BISERVER_CONTEXT_PATH;
     }
     return biserverContextPath;
   }
 
   public String getBiServerBaseUrl() {
     String baseUrl = DEFAULT_BISERVER_BASE_URL;
     try {
       WebXml webXml = new WebXml(new File(getWarPath() + WEB_XML_PATH));
       baseUrl = webXml.getBaseUrl();
      if (!(baseUrl != null && baseUrl.length() > 0)) {
         baseUrl = DEFAULT_BISERVER_BASE_URL;
       }
     } catch (Exception e) {
       // Do nothing;
     }
     return baseUrl;
   }
 
   public String getBiServerStatusCheckPeriod() {
     Long period = getConsoleConfig().getServerStatusCheckPeriod();
     return period != null ? period.toString() : DEFAULT_BISERVER_STATUS_CHECK_PERIOD;
   }
 
   /**
    * Returns a comma-separated list of roles to apply to newly created users.
    */
   public String getDefaultRolesString() {
     return getConsoleConfig().getDefaultRoles();
   }
 
   /**
    * Convenience wrapper around getDefaultRolesString that parses the default roles string into individual roles.
    */
   public List<String> getDefaultRoles() {
     String defaultRolesString = getDefaultRolesString();
     List<String> defaultRoles = new ArrayList<String>();
     if ((defaultRolesString != null) && (defaultRolesString.trim().length() > 0)) {
       StringTokenizer tokenizer = new StringTokenizer(defaultRolesString, ","); //$NON-NLS-1$
       while (tokenizer.hasMoreTokens()) {
         defaultRoles.add(tokenizer.nextToken());
       }
     }
     return defaultRoles;
   }
 
   public String getHomepageUrl() {
     String homepageUrl = getConsoleConfig().getHomePageUrl();
     if ((homepageUrl == null) || (homepageUrl.trim().length() == 0)) {
       homepageUrl = DEFAULT_HOMEPAGE_URL;
     }
     return homepageUrl;
   }
 
   public String getHomepageTimeout() {
     Integer timeout = getConsoleConfig().getHomePageTimeout();
     return timeout != null ? timeout.toString() : DEFAULT_HOMEPAGE_TIMEOUT;
   }
 
   public String getHibernateConfigPath() {
     String hibernateConfigPath = DEFAULT_HIBERNATE_CONFIG_PATH;
     String hibernateConfigFile = getHibernateSettingsXml().getHibernateConfigFile();
     if (hibernateConfigFile != null && hibernateConfigFile.length() > 0) {
       hibernateConfigPath = hibernateConfigFile;
     }
     return hibernateConfigPath;
   }
 
   public boolean isHibernateManaged() {
     return getHibernateSettingsXml().getHibernateManaged();
   }
 
   public String getSolutionPath() {
     String pentahoSolutionPath = getConsoleConfig().getSolutionPath();
     if ((pentahoSolutionPath == null) || (pentahoSolutionPath.trim().length() == 0)) {
       pentahoSolutionPath = DEFAULT_SOLUTION_PATH;
     }
     return pentahoSolutionPath;
   }
 
   public String getWarPath() {
     String pentahoWarPath = getConsoleConfig().getWebAppPath();
     if ((pentahoWarPath == null) || (pentahoWarPath.trim().length() == 0)) {
       pentahoWarPath = DEFAULT_WAR_PATH;
     }
     return pentahoWarPath;
   }
 
   public String getPasswordServiceClass() {
     String passwordServiceClass = getPentahoObjectsConfig().getPasswordService();
     if (StringUtils.isEmpty(passwordServiceClass)) {
       passwordServiceClass = DEFAULT_VALUE_PASSWORD_SERVICE_CLASS;
     }
     return passwordServiceClass;
   }
 
   public String getJdbcDriverPath() {
     return JDBC_DRIVER_PATH;
   }
 
   public String getHelpUrl() {
     String helpUrl = getConsoleConfig().getHelpUrl();
     if ((helpUrl == null) || (helpUrl.trim().length() == 0)) {
       helpUrl = DEFAULT_HELP_URL;
     }
     return helpUrl;
   }
 
   IConsoleConfig getConsoleConfig() {
     if (consoleConfig == null) {
       try {
         consoleConfig = new ConsoleConfigXml(new File(ClassLoader.getSystemResource(CONFIG_FILE_NAME).toURI()));
       } catch (Exception ex) {
         logger.warn(Messages.getErrorString("AppConfigProperties.ERROR_0004_UNABLE_TO_READ_FILE", CONFIG_FILE_NAME)); //$NON-NLS-1$
         consoleConfig = new ConsoleConfigXml();
       }
     }
     return consoleConfig;
   }
   
   HibernateSettingsXml getHibernateSettingsXml() {
     if (hibernateSettingXml == null) {
       try {
         hibernateSettingXml = new HibernateSettingsXml(new File(getSolutionPath() + HIBERNATE_MANAGED_XML_PATH));
       } catch (Exception e) {
         logger.warn(Messages.getErrorString("AppConfigProperties.ERROR_0004_UNABLE_TO_READ_FILE", getSolutionPath() + HIBERNATE_MANAGED_XML_PATH)); //$NON-NLS-1$
         hibernateSettingXml = new HibernateSettingsXml();
       }
     }
     return hibernateSettingXml;
   }
   
   PentahoObjectsConfig getPentahoObjectsConfig() {
     if (pentahoObjectsConfig == null) {
       try {
         pentahoObjectsConfig = new PentahoObjectsConfig(new File(getSolutionPath() + PENTAHO_OBJECTS_SPRING_XML));
       } catch (Exception e) {
         logger.warn(Messages.getErrorString("AppConfigProperties.ERROR_0004_UNABLE_TO_READ_FILE", getSolutionPath() + PENTAHO_OBJECTS_SPRING_XML)); //$NON-NLS-1$
         pentahoObjectsConfig = new PentahoObjectsConfig();
       }
     }
     return pentahoObjectsConfig;
   }
   
   SpringSecurityHibernateConfig getSpringSecurityHibernateConfig() {
     if (springSecurityHibernateConfig == null) {
       try {
         springSecurityHibernateConfig = new SpringSecurityHibernateConfig(new File(getSolutionPath() + SPRING_SECURITY_HIBERNATE_XML));
       } catch (Exception e) {
         logger.warn(Messages.getErrorString("AppConfigProperties.ERROR_0004_UNABLE_TO_READ_FILE", getSolutionPath() + SPRING_SECURITY_HIBERNATE_XML)); //$NON-NLS-1$
         springSecurityHibernateConfig = new SpringSecurityHibernateConfig();
       }
     }
     return springSecurityHibernateConfig;
   }
   
      
 }
