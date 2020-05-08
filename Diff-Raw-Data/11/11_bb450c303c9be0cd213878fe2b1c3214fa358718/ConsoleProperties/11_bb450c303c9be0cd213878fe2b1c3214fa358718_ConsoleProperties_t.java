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
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.pentaho.pac.server.i18n.Messages;
 
 /**
  * By default, this class will initialize itself from a file on the class path called console.properties.
  * A client of this module can override the default initialization by simply calling 
  * setProperties() with a Properties object that provides an alternate initialization.
  * To understand what the required parameters are, see console.properties.
  * 
  * @author Ramaiz Mansoor
  *
  */
 public class ConsoleProperties {
 
   public static final String CONSOLE_PORT_NUMBER = "console.start.port.number"; //$NON-NLS-1$
   public static final String CONSOLE_HOST_NAME = "console.hostname"; //$NON-NLS-1$
   public static final String SSLENABLED = "console.ssl.enabled"; //$NON-NLS-1$
   public static final String CONSOLE_SSL_PORT_NUMBER = "console.ssl.port.number"; //$NON-NLS-1$
   public static final String KEY_ALIAS = "keyAlias"; //$NON-NLS-1$
   public static final String KEY_PASSWORD = "keyPassword"; //$NON-NLS-1$
   public static final String KEYSTORE = "keyStore"; //$NON-NLS-1$
   public static final String KEYSTORE_PASSWORD = "keyStorePassword"; //$NON-NLS-1$
   public static final String TRUSTSTORE = "trustStore"; //$NON-NLS-1$
   public static final String TRUSTSTORE_PASSWORD = "trustStorePassword"; //$NON-NLS-1$  
   public static final String WANT_CLIENT_AUTH = "wantClientAuth"; //$NON-NLS-1$
   public static final String NEED_CLIENT_AUTH = "needClientAuth"; //$NON-NLS-1$
   public static final String CONSOLE_SECURITY_REALM_NAME = "console.security.realm.name"; //$NON-NLS-1$
   public static final String CONSOLE_SECURITY_LOGIN_MODULE_NAME = "console.security.login.module.name"; //$NON-NLS-1$
   public static final String CONSOLE_SECURITY_ENABLED = "console.security.enabled"; //$NON-NLS-1$
   public static final String CONSOLE_SECURITY_AUTH_CONFIG_PATH = "console.security.auth.config.path"; //$NON-NLS-1$
   public static final String CONSOLE_SECURITY_ROLES_ALLOWED = "console.security.roles.allowed"; //$NON-NLS-1$
   public static final String DEFAULT_CONSOLE_PROPERTIES_FILE_NAME = "resource/config/console.properties"; //$NON-NLS-1$
   public static final String STOP_ARG = "-STOP"; //$NON-NLS-1$
   public static final String STOP_PORT = "console.stop.port.number";//$NON-NLS-1$
  public static final String CONSOLE_SECURITY_ROLE_DELIMITER = "console.security.roles.delimiter"; //$NON-NLS-1$
   public static final String CONSOLE_SECURITY_CALLBACK_HANDLER = "console.security.callback.handler"; //$NON-NLS-1$
   
   private static final Log logger = LogFactory.getLog(ConsoleProperties.class);
   private Properties properties = null;
   private static ConsoleProperties instance = new ConsoleProperties();
   
   protected ConsoleProperties() {
     init( DEFAULT_CONSOLE_PROPERTIES_FILE_NAME );
   }
   
   public static ConsoleProperties getInstance() {
     return instance;
   }
   
   public void init( String pathToConfigResource ) {
     FileInputStream fis = null;
     try {
       File file = new File(pathToConfigResource);
       fis = new FileInputStream(file);
     } catch (IOException e1) {
       logger.error(Messages.getString("PacProService.OPEN_PROPS_FAILED", DEFAULT_CONSOLE_PROPERTIES_FILE_NAME)); //$NON-NLS-1$
     }
     if (null != fis) {
       properties = new Properties();
       try {
         properties.load(fis);
       } catch (IOException e) {
         logger.error(Messages.getString("PacProService.LOAD_PROPS_FAILED", DEFAULT_CONSOLE_PROPERTIES_FILE_NAME)); //$NON-NLS-1$
       }
     }
   }
   
   public void init( Properties p ) {
     properties = p;
   }
  
   
   public void setProperties( Properties p )
   {
     properties = p;
   }
 
   public String getProperty( String key )
   {
     return (String)properties.get( key );
   }
 }
