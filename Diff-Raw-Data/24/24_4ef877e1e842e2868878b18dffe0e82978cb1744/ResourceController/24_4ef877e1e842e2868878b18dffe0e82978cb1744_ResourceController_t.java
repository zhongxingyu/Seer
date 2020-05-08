 /**
  * Copyright (c) 2009 - 2013 By: CWS, Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.cws.esolutions.core.controllers;
 
 import java.net.URL;
 import java.util.Map;
 import java.util.Locale;
 import org.slf4j.Logger;
 import java.util.HashMap;
 import java.io.IOException;
 import java.util.Properties;
 import javax.naming.Context;
 import javax.sql.DataSource;
 import java.sql.SQLException;
 import java.io.BufferedReader;
 import org.slf4j.LoggerFactory;
 import java.util.ResourceBundle;
 import java.io.InputStreamReader;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import com.unboundid.ldap.sdk.ResultCode;
 import java.util.MissingResourceException;
 import org.apache.commons.lang.StringUtils;
 import com.unboundid.ldap.sdk.LDAPException;
 import com.unboundid.ldap.sdk.LDAPConnection;
 import org.apache.commons.dbcp.BasicDataSource;
 import com.unboundid.ldap.sdk.LDAPConnectionPool;
 import com.unboundid.ldap.sdk.LDAPConnectionOptions;
  
 import com.cws.esolutions.core.Constants;
 import com.cws.esolutions.security.config.AuthRepo;
 import com.cws.esolutions.security.utils.PasswordUtils;
 import com.cws.esolutions.core.config.DataSourceManager;
 import com.cws.esolutions.security.enums.AuthRepositoryType;
 import com.cws.esolutions.core.exception.CoreServiceException;
 /**
  * eSolutionsCore
  * com.cws.esolutions.core.controllers
  * ResourceController.java
  *
  * $Id: $
  * $Author: $
  * $Date: $
  * $Revision: $
  * @author kmhuntly@gmail.com
  * @version 1.0
  *
  * History
  * ----------------------------------------------------------------------------
  * kh05451 @ Nov 23, 2012 8:21:09 AM
  *     Created.
  */
 public class ResourceController
 {
     private static final String DS_CONTEXT = "java:comp/env/";
     private static final String CNAME = ResourceController.class.getName();
 
     private static final Logger DEBUGGER = LoggerFactory.getLogger(Constants.DEBUGGER);
     private static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     private static final Logger ERROR_RECORDER = LoggerFactory.getLogger(Constants.ERROR_LOGGER + ResourceController.CNAME);
 
     public synchronized static void configureAndCreateAuthConnection(final AuthRepo authRepo, final boolean isContainer, final ResourceControllerBean resBean) throws CoreServiceException
     {
         String methodName = CNAME + "#configureAndCreateAuthConnection(final AuthRepo authRepo, final boolean isContainer, final ResourceControllerBean resBean) throws CoreServiceException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AuthRepo: {}", authRepo);
             DEBUGGER.debug("isContainer: {}", isContainer);
             DEBUGGER.debug("ResourceControllerBean: {}", resBean);
         }
 
         int minConnections = 1;
         int maxConnections = 10;
 
         final AuthRepositoryType authType = AuthRepositoryType.valueOf(authRepo.getRepoType());
 
         if (DEBUG)
         {
             DEBUGGER.debug("AuthRepositoryType: {}", authType);
         }
 
         switch (authType)
         {
             case LDAP:
                 LDAPConnection ldapConn = null;
                 LDAPConnectionOptions connOpts = new LDAPConnectionOptions();
 
                 try
                 {
                     connOpts.setAbandonOnTimeout(true);
                     connOpts.setAutoReconnect(true);
                     connOpts.setBindWithDNRequiresPassword(true);
 
                     if (isContainer)
                     {
                         Context initContext = new InitialContext();
                         Context envContext = (Context) initContext.lookup(DS_CONTEXT);
 
                         URL ldapConfigFile = (URL) envContext.lookup(authRepo.getConfigFile());
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("ldapConfigFile: {}", ldapConfigFile);
                         }
 
                         if (ldapConfigFile != null)
                         {
                             BufferedReader configReader = new BufferedReader(new InputStreamReader(ldapConfigFile.openStream(), "UTF-8")); // TODO: string encoding should be config option
 
                             if (DEBUG)
                             {
                                 DEBUGGER.debug("BufferedReader: {}", configReader);
                             }
 
                             if (configReader.ready())
                             {
                                 Properties ldapProperties = new Properties();
                                 ldapProperties.load(configReader);
 
                                 if (DEBUG)
                                 {
                                     DEBUGGER.debug("Properties: {}", ldapProperties);
                                 }
 
                                minConnections = Integer.parseInt(ldapProperties.getProperty(authRepo.getMinConnections()));
                                maxConnections = Integer.parseInt(ldapProperties.getProperty(authRepo.getMaxConnections()));

                                connOpts.setConnectTimeoutMillis(Integer.parseInt(ldapProperties.getProperty(authRepo.getRepositoryConnTimeout())));
                                connOpts.setResponseTimeoutMillis(Integer.parseInt(ldapProperties.getProperty(authRepo.getRepositoryReadTimeout())));

                                ldapConn = new LDAPConnection(connOpts, ldapProperties.getProperty(authRepo.getRepositoryHost()),
                                        Integer.parseInt(ldapProperties.getProperty(authRepo.getRepositoryPort())),
                                        ldapProperties.getProperty(authRepo.getRepositoryUser()),
                                        PasswordUtils.decryptText(ldapProperties.getProperty(authRepo.getRepositoryPass()),
                                                ldapProperties.getProperty(authRepo.getRepositorySalt()).length()));
                             }
                             else
                             {
                                 throw new IOException("Unable to load LDAP configuration file. Cannot continue.");
                             }
                         }
                         else
                         {
                             throw new IOException("Unable to load LDAP configuration file. Cannot continue.");
                         }
                     }
                     else
                     {
                         minConnections = Integer.parseInt(authRepo.getMinConnections());
                         maxConnections = Integer.parseInt(authRepo.getMaxConnections());
 
                         connOpts.setConnectTimeoutMillis(Integer.parseInt(authRepo.getRepositoryConnTimeout()));
                         connOpts.setResponseTimeoutMillis(Integer.parseInt(authRepo.getRepositoryReadTimeout()));
 
                         ldapConn = new LDAPConnection(connOpts,
                                 authRepo.getRepositoryHost(),
                                 Integer.parseInt(authRepo.getRepositoryPort()),
                                 authRepo.getRepositoryUser(),
                                 PasswordUtils.decryptText(
                                 authRepo.getRepositoryPass(),
                                 authRepo.getRepositorySalt().length()));
                     }
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("LDAPConnectionOptions: {}", connOpts);
                     }
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("LDAPConnection: {}", ldapConn);
                     }
 
                     if ((ldapConn != null) && (ldapConn.isConnected()))
                     {
                         LDAPConnectionPool connPool = new LDAPConnectionPool(ldapConn, minConnections, maxConnections);
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("LDAPConnectionPool: {}", connPool);
                         }
 
                         if (!(connPool.isClosed()))
                         {
                             resBean.setAuthDataSource(connPool);
                         }
                         else
                         {
                             throw new LDAPException(ResultCode.CONNECT_ERROR, "Failed to create LDAP connection pool");
                         }
                     }
                     else
                     {
                         throw new LDAPException(ResultCode.CONNECT_ERROR, "Failed to establish an LDAP connection");
                     }
                 }
                 catch (LDAPException lx)
                 {
                     ERROR_RECORDER.error(lx.getMessage(), lx);
 
                     throw new CoreServiceException(lx.getMessage(), lx);
                 }
                 catch (NamingException nx)
                 {
                     ERROR_RECORDER.error(nx.getMessage(), nx);
 
                     throw new CoreServiceException(nx.getMessage(), nx);
                 }
                 catch (IOException iox)
                 {
                     ERROR_RECORDER.error(iox.getMessage(), iox);
 
                     throw new CoreServiceException(iox.getMessage(), iox);
                 }
 
                 break;
             case SQL:
                 // the isContainer only matters here
                 if (isContainer)
                 {
                     try
                     {
                         Context initContext = new InitialContext();
                         Context envContext = (Context) initContext.lookup(ResourceController.DS_CONTEXT);
 
                         resBean.setAuthDataSource((DataSource) envContext.lookup(authRepo.getRepositoryHost()));
                     }
                     catch (NamingException nx)
                     {
                         ERROR_RECORDER.error(nx.getMessage(), nx);
 
                         throw new CoreServiceException(nx.getMessage(), nx);
                     }
                 }
                 else
                 {
                     BasicDataSource dataSource = new BasicDataSource();
                     dataSource.setDriverClassName(authRepo.getRepositoryDriver());
                     dataSource.setUrl(authRepo.getRepositoryHost());
                     dataSource.setUsername(authRepo.getRepositoryUser());
                     dataSource.setPassword(PasswordUtils.decryptText(
                             authRepo.getRepositoryPass(),
                             authRepo.getRepositorySalt().length()));
 
                     resBean.setAuthDataSource(dataSource);
                 }
 
                 break;
             default:
                 throw new CoreServiceException("Unhandled ResourceType");
         }
     }
 
     public synchronized static void closeAuthConnection(final AuthRepo authRepo, final boolean isContainer, final ResourceControllerBean resBean) throws CoreServiceException
     {
         String methodName = CNAME + "#closeAuthConnection(final AuthRepo authRepo, final boolean isContainer, final ResourceControllerBean resBean) throws CoreServiceException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("AuthRepo: {}", authRepo);
             DEBUGGER.debug("isContainer: {}", isContainer);
             DEBUGGER.debug("ResourceControllerBean: {}", resBean);
         }
 
         final AuthRepositoryType authType = AuthRepositoryType.valueOf(authRepo.getRepoType());
 
         if (DEBUG)
         {
             DEBUGGER.debug("AuthRepositoryType: {}", authType);
         }
 
         try
         {
             switch (authType)
             {
                 case LDAP:
                     LDAPConnectionPool ldapPool = (LDAPConnectionPool) resBean.getAuthDataSource();
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("LDAPConnectionPool: {}", ldapPool);
                     }
 
                     if ((ldapPool != null) && (!(ldapPool.isClosed())))
                     {
                         ldapPool.close();
                     }
 
                     break;
                 case SQL:
                     // the isContainer only matters here
                     if (!(isContainer))
                     {
                         BasicDataSource dataSource = (BasicDataSource) resBean.getAuthDataSource();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("BasicDataSource: {}", dataSource);
                         }
 
                         if ((dataSource != null ) && (!(dataSource.isClosed())))
                         {
                             dataSource.close();
                         }
                     }
 
                     break;
                 default:
                     throw new CoreServiceException("Unhandled ResourceType");
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
         }
     }
 
     /**
      * Sets up an application datasource connection
      * via JNDI to the requested resource reference
      *
      * @param dsManager
      * @param resBean
      * @throws CoreServiceException
      */
     public synchronized static void configureAndCreateDataConnection(final DataSourceManager dsManager, final ResourceControllerBean resBean) throws CoreServiceException
     {
         final String methodName = ResourceController.CNAME + "#configureAndCreateDataConnection(final DataSourceManager dsManager, final ResourceControllerBean resBean) throws CoreServiceException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("DataSourceManager: {}", dsManager);
             DEBUGGER.debug("ResourceControllerBean: {}", resBean);
         }
 
         Map<String, DataSource> dsMap = resBean.getDataSource();
 
         if (DEBUG)
         {
             DEBUGGER.debug("dsMap: {}", dsMap);
         }
 
         if (dsMap == null)
         {
             dsMap = new HashMap<String, DataSource>();
         }
 
         if (!(dsMap.containsKey(dsManager.getDsName())))
         {
             if (StringUtils.isNotEmpty(dsManager.getDriver()))
             {
                 StringBuilder sBuilder = new StringBuilder()
                     .append("connectTimeout=" + dsManager.getConnectTimeout() + ";")
                     .append("socketTimeout=" + dsManager.getConnectTimeout() + ";")
                     .append("autoReconnect=" + dsManager.getAutoReconnect() + ";")
                     .append("zeroDateTimeBehavior=convertToNull");
 
                 BasicDataSource dataSource = new BasicDataSource();
                 dataSource.setDriverClassName(dsManager.getDriver());
                 dataSource.setUrl(dsManager.getDataSource());
                 dataSource.setUsername(dsManager.getDsUser());
                 dataSource.setConnectionProperties(sBuilder.toString());
 
                 // handle both encrypted and non-encrypted passwords
                 // prefer encrypted
                 if (StringUtils.isNotEmpty(dsManager.getSalt()))
                 {
                     dataSource.setPassword(PasswordUtils.decryptText(
                             dsManager.getDsPass(),
                             dsManager.getSalt().length()));
                 }
                 else
                 {
                     dataSource.setPassword(dsManager.getDsPass());
                 }
 
                 dsMap.put(dsManager.getDsName(), dataSource);
             }
             else
             {
                 try
                 {
                     Context initContext = new InitialContext();
                     Context envContext = (Context) initContext.lookup(DS_CONTEXT);
 
                     DataSource dataSource = (DataSource) envContext.lookup(dsManager.getDataSource());
 
                     dsMap.put(dsManager.getDsName(), dataSource);
                 }
                 catch (NamingException nx)
                 {
                     ERROR_RECORDER.error(nx.getMessage(), nx);
 
                     throw new CoreServiceException(nx.getMessage(), nx);
                 }
             }
         }
 
         resBean.setDataSource(dsMap);
     }
 
     /**
      * Retrieves and returns a system property housed in an application
      * configuration file
      *
      * @param pkgName
      * @param reqProperty
      * @param classLoader
      * @throws MissingResourceException
      * @return String
      */
     public static String returnSystemPropertyValue(final String pkgName, final String reqProperty, final ClassLoader classLoader) throws CoreServiceException
     {
         final String methodName = ResourceController.CNAME + "#returnSystemPropertyValue(final String pkgName, final String reqProperty, final ClassLoader classLoader) throws CoreServiceException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug(pkgName);
             DEBUGGER.debug(reqProperty);
         }
 
         String sProperty = null;
         ResourceBundle resourceBundle = null;
 
         try
         {
             resourceBundle = ResourceBundle.getBundle(pkgName, Locale.getDefault(), classLoader);
             sProperty = resourceBundle.getString(reqProperty);
         }
         catch (MissingResourceException mrx)
         {
             ERROR_RECORDER.error(mrx.getMessage(), mrx);
 
             throw new CoreServiceException(mrx.getMessage(), mrx);
         }
         finally
         {
             resourceBundle = null;
         }
 
         return sProperty;
     }
 }
