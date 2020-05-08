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
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Node;
 import org.pentaho.pac.server.config.HibernateSettingsXml;
 import org.pentaho.platform.api.engine.ISystemSettings;
 import org.pentaho.platform.engine.core.system.SystemSettings;
 import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
 
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
 
   private static final Log logger = LogFactory.getLog(AppConfigProperties.class);
 
   public static final String WEB_XML_PATH = "/WEB-INF/web.xml"; //$NON-NLS-1$
   public static final String HIBERNATE_MANAGED_XML_PATH = "/system/hibernate/hibernate-settings.xml"; //$NON-NLS-1$
   public static final String HIBERNATE_CONFIG_PATH = "hibernateConfigPath"; //$NON-NLS-1$
   public static final String PENTAHO_OBJECTS_SPRING_XML = "/system/pentahoObjects.spring.xml" ; //$NON-NLS-1$
   public static final String SLASH = "/" ; //$NON-NLS-1$
   public static final String COLON = ":" ; //$NON-NLS-1$
   public static final String COMMA = "," ; //$NON-NLS-1$
   public static final String XPATH_TO_CONTEXT_PARAM = "web-app/context-param"; //$NON-NLS-1$
   public static final String XPATH_TO_HIBERNATE_CFG_FILE = "settings/config-file"; //$NON-NLS-1$
   public static final String XPATH_TO_PARAM_NAME = "param-name"; //$NON-NLS-1$
  
   public static final String XPATH_TO_PARAM_VALUE = "param-value"; //$NON-NLS-1$
  
   public static final String XPATH_TO_PASSWORD_SERVICE = "beans/bean/constructor-arg/list/bean[@id='pentahoObjects.passwordService']"; //$NON-NLS-1$  
 
   public static final String KEY_WAR_PATH = "war-path"; //$NON-NLS-1$
  
   public static final String KEY_PASSWORD_SERVICE_CLASS = "password-service-class"; //$NON-NLS-1$
  
   public static final String KEY_SOLUTION_PATH = "solution-path"; //$NON-NLS-1$
   
   public static final String KEY_DEFAULT_ROLES = "default-roles"; //$NON-NLS-1$
 
   public static final String KEY_HIBERNATE_CONFIG_PATH = "hibernate-config-path"; //$NON-NLS-1$
 
   public static final String KEY_HOMEPAGE_TIMEOUT = "homepage-timeout"; //$NON-NLS-1$
 
   public static final String KEY_BISERVER_STATUS_CHECK_PERIOD = "biserver-status-check-period"; //$NON-NLS-1$
 
   public static final String KEY_BISERVER_BASE_URL = "biserver-base-url"; //$NON-NLS-1$
 
   public static final String KEY_BISERVER_CONTEXT_PATH = "biserver-context-path"; //$NON-NLS-1$
 
   public static final String KEY_PLATFORM_USERNAME = "platform-username"; //$NON-NLS-1$
   
   public static final String KEY_JDBC_DRIVERS_PATH = "jdbc-drivers-path"; //$NON-NLS-1$
   
   public static final String DEFAULT_PROPERTIES_FILE_NAME = "console.xml"; //$NON-NLS-1$
 
   public static final String DEFAULT_VALUE_PASSWORD_SERVICE_CLASS = "org.pentaho.platform.util.Base64PasswordService"; //$NON-NLS-1$
   
   public static final String DEFAULT_BISERVER_BASE_URL = "http://localhost:8080/pentaho"; //$NON-NLS-1$
   
   public static final String DEFAULT_BISERVER_CONTEXT_PATH = "/pentaho"; //$NON-NLS-1$
   
   public static final String DEFAULT_PLATFORM_USERNAME = "joe"; //$NON-NLS-1$
   
   public static final String DEFAULT_BISERVER_STATUS_CHECK_PERIOD = "30000"; //$NON-NLS-1$
   
   public static final String DEFAULT_HOMEPAGE_TIMEOUT = "15000"; //$NON-NLS-1$
   
   public static final String DEFAULT_HIBERNATE_CONFIG_PATH = "hsql.hibernate.cfg.xml"; //$NON-NLS-1$
     
   private static final String BASE_URL = "base-url"; //$NON-NLS-1$
   
   // ~ Instance fields =================================================================================================
 
   private ISystemSettings settings = new OpenAdminConsoleSettings();
 
   private static AppConfigProperties instance = new AppConfigProperties();
 
   // ~ Constructors ====================================================================================================
 
   protected AppConfigProperties() {
     initPasswordService();
   }
 
   // ~ Methods =========================================================================================================
 
   public static synchronized AppConfigProperties getInstance() {
     return instance;
   }
 
   protected void initPasswordService() {
     String passwordServiceClass = getPasswordServiceClass();
     if(passwordServiceClass != null) {
       PasswordServiceFactory.init(passwordServiceClass);  
     } else  {
       PasswordServiceFactory.init(DEFAULT_VALUE_PASSWORD_SERVICE_CLASS);
     }
   }
 
   public String getPlatformUsername() {
     return settings.getSystemSetting(KEY_PLATFORM_USERNAME, DEFAULT_PLATFORM_USERNAME);
   }
 
   public String getBiServerContextPath() {
     String returnValue = null;
     String value = getBiServerBaseUrl();
     int start = value.lastIndexOf(COLON); //$NON-NLS-1$
     int middle = value.indexOf(SLASH, start); //$NON-NLS-1$
     
     returnValue = value.substring(middle, value.length()-1);
     if (!(returnValue != null && returnValue.length() > 0)) {
       returnValue = DEFAULT_BISERVER_CONTEXT_PATH;
     }
     
     return returnValue;
   }
 
   public String getBiServerBaseUrl() {
     String warPath = getWarPath();
     String returnValue = null;
     StringBuffer xmlBuffer = new StringBuffer();
     FileReader fileReader = null;
     try {
       fileReader = new FileReader(new File(warPath + WEB_XML_PATH));
       char[] inputString = new char[1000];
       int numCharsRead = fileReader.read(inputString, 0, 1000);
       while (numCharsRead >= 0) {
         xmlBuffer.append(inputString, 0, numCharsRead);
         numCharsRead = fileReader.read(inputString, 0, 1000);
       }
       Document document = XmlDom4JHelper.getDocFromString(xmlBuffer.toString(), null);
       List<Node> nodes = document.selectNodes(XPATH_TO_CONTEXT_PARAM);
       for (Node node : nodes) {
         Node paramNameNode = node.selectSingleNode(XPATH_TO_PARAM_NAME);
         
         if(paramNameNode.getStringValue() != null && paramNameNode.getStringValue().equals(BASE_URL)) {
           Node paramValueNode = node.selectSingleNode(XPATH_TO_PARAM_VALUE);
           returnValue = paramValueNode.getStringValue();
           break;
         }
       }
     } catch(Exception e) {
      logger.error("Unable to read file : " + warPath + WEB_XML_PATH); //$NON-NLS-1$
       returnValue = null;
     } finally {
       if ( null != fileReader ) {
         try { fileReader.close(); }
         catch( IOException e) {
          logger.error("Failed to close stream associated with : " + warPath + WEB_XML_PATH); //$NON-NLS-1$
         }
       }
     }    if (!(returnValue != null && returnValue.length() > 0)) {
       returnValue = DEFAULT_BISERVER_BASE_URL;
     }
     return returnValue;
   }
 
   public String getBiServerStatusCheckPeriod() {
     return settings.getSystemSetting(KEY_BISERVER_STATUS_CHECK_PERIOD, DEFAULT_BISERVER_STATUS_CHECK_PERIOD);
   }
 
   /**
    * Returns a comma-separated list of roles to apply to newly created users.
    */
   public String getDefaultRolesString() {
     return settings.getSystemSetting(KEY_DEFAULT_ROLES, null);
   }
   
   /**
    * Convenience wrapper around getDefaultRolesString that parses the default roles string into individual roles.
    */
   public List<String> getDefaultRoles() {
     List<String> defaultRoles = new ArrayList<String>();
     String defaultRolesString = getDefaultRolesString();
     if (defaultRolesString == null) {
       return Collections.emptyList();
     }
     StringTokenizer tokenizer = new StringTokenizer(defaultRolesString, COMMA); //$NON-NLS-1$
     while (tokenizer.hasMoreTokens()) {
       defaultRoles.add(tokenizer.nextToken());
     }
     return defaultRoles;
   }
   
   public String getHomepageTimeout() {
     return settings.getSystemSetting(KEY_HOMEPAGE_TIMEOUT, DEFAULT_HOMEPAGE_TIMEOUT);
   }
 
   public String getHibernateConfigPath() {
     String solutionPath = getSolutionPath();
     String returnValue = null;
     try {
       HibernateSettingsXml hibernateSettingXml = new HibernateSettingsXml(new File(solutionPath + HIBERNATE_MANAGED_XML_PATH));
       String hibernateConfigFile = hibernateSettingXml.getHibernateConfigFile();
       if(hibernateConfigFile != null && hibernateConfigFile.length() > 0) {
         returnValue = hibernateConfigFile.substring(hibernateConfigFile.lastIndexOf(SLASH)+1, hibernateConfigFile.length());  
       }
     } catch(Exception e) {
      logger.error("Unable to read file : " +solutionPath + HIBERNATE_MANAGED_XML_PATH); //$NON-NLS-1$
       returnValue = null;
     } 
     if ( StringUtils.isEmpty(returnValue) ) {
       returnValue = DEFAULT_HIBERNATE_CONFIG_PATH;
      }
     return returnValue;
   }
 
   public String getSolutionPath() {
     return settings.getSystemSetting(KEY_SOLUTION_PATH, null);
   }
 
   public String getWarPath() {
     return settings.getSystemSetting(KEY_WAR_PATH, null);
   }
 
   public String getPasswordServiceClass() {
     String solutionPath = getSolutionPath();
     String returnValue = null;
     StringBuffer xmlBuffer = new StringBuffer();
     FileReader fileReader = null;
     try {
       fileReader = new FileReader(new File(solutionPath + PENTAHO_OBJECTS_SPRING_XML));
       char[] inputString = new char[1000];
       int numCharsRead = fileReader.read(inputString, 0, 1000);
       while (numCharsRead >= 0) {
         xmlBuffer.append(inputString, 0, numCharsRead);
         numCharsRead = fileReader.read(inputString, 0, 1000);
       }
       Document document = XmlDom4JHelper.getDocFromString(xmlBuffer.toString(), null);
       Node node = document.selectSingleNode(XPATH_TO_PASSWORD_SERVICE);
       if (node != null) {
         returnValue = node.getStringValue();  
       }
     } catch(Exception e) {
      logger.error("Unable to read file : " + solutionPath + PENTAHO_OBJECTS_SPRING_XML); //$NON-NLS-1$
       returnValue = null;
     } finally {
       if ( null != fileReader ) {
         try { fileReader.close(); }
         catch( IOException e) {
          logger.error("Failed to close stream associated with : " + solutionPath + PENTAHO_OBJECTS_SPRING_XML); //$NON-NLS-1$ 
         }
       }
     }
     if (!(returnValue != null && returnValue.length() > 0)) {
      returnValue = DEFAULT_VALUE_PASSWORD_SERVICE_CLASS;
     }
     
     return returnValue;
   }
 
   public String getJdbcDriverPath() {
     return settings.getSystemSetting(KEY_JDBC_DRIVERS_PATH, null);
   }
   /**
    * Reuse of {@link SystemSettings} where settings are read from <code>resource/config/console.xml</code>.
    * 
    * @author mlowery
    */
   private class OpenAdminConsoleSettings extends SystemSettings {
 
     private static final long serialVersionUID = -5912709145466266140L;
 
     public OpenAdminConsoleSettings() {
       super();
     }
 
     /**
      * Need to override since default implementation points to solution path (e.g. pentaho-solutions).
      */
     @Override
     protected String getAbsolutePath(String path) {
       return "resource/config/" + path; //$NON-NLS-1$
     }
 
     /**
      * Need to override since default implementation points to pentaho.xml.
      */
     @Override
     public String getSystemSetting(final String settingName, final String defaultValue) {
       return getSystemSetting(DEFAULT_PROPERTIES_FILE_NAME, settingName, defaultValue);
     }
 
     /**
      * Need to override to stop caching behavior. Caching is disabled to allow this object to see changes to
      * the console.xml file that are made by other objects.
      */
     @Override
     public Document getSystemSettingsDocument(final String actionPath) {
       File f = new File(getAbsolutePath(actionPath));
       if (!f.exists()) {
         return null;
       }
       Document systemSettingsDocument = null;
 
       try {
         systemSettingsDocument = XmlDom4JHelper.getDocFromFile(f, null);
       } catch (DocumentException e) {
         logger.error(e);
       } catch (IOException e) {
         logger.error(e);
       }
 
       return systemSettingsDocument;
     }
 
   }
 
 }
