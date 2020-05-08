 /*
  * Copyright (c) 2009 - 2014 CaspersBox Web Services
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
 package com.cws.esolutions.security.utils;
 /*
  * Project: eSolutionsSecurity
  * Package: com.cws.esolutions.security.utils
  * File: DAOInitializer.java
  *
  * History
  *
  * Author               Date                            Comments
  * ----------------------------------------------------------------------------
  * kmhuntly@gmail.com   11/23/2008 22:39:20             Created.
  */
 import java.io.File;
 import org.slf4j.Logger;
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.Properties;
 import javax.naming.Context;
 import java.sql.SQLException;
 import org.slf4j.LoggerFactory;
 import java.io.FileInputStream;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import java.io.FileNotFoundException;
 import com.unboundid.util.ssl.SSLUtil;
 import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.io.FileUtils;
 import com.unboundid.ldap.sdk.ResultCode;
 import com.unboundid.ldap.sdk.LDAPException;
 import com.unboundid.ldap.sdk.LDAPConnection;
 import java.security.GeneralSecurityException;
 import org.apache.commons.dbcp.BasicDataSource;
 import com.unboundid.ldap.sdk.LDAPConnectionPool;
 import com.unboundid.ldap.sdk.LDAPConnectionOptions;
 import com.unboundid.util.ssl.TrustStoreTrustManager;
 
 import com.cws.esolutions.security.SecurityServiceBean;
 import com.cws.esolutions.security.utils.PasswordUtils;
 import com.cws.esolutions.security.SecurityServiceConstants;
 import com.cws.esolutions.security.config.enums.AuthRepositoryType;
 import com.cws.esolutions.security.exception.SecurityServiceException;
 /**
  * Interface for the Application Data DAO layer. Allows access
  * into the asset management database to obtain, modify and remove
  * application information.
  *
  * @author khuntly
  * @version 1.0
  */
 public final class DAOInitializer
 {
     private static final String REPO_TYPE = "repoType";
     private static final String IS_SECURE = "isSecure";
     private static final String TRUST_FILE= "trustStoreFile";
     private static final String TRUST_PASS = "trustStorePass";
     private static final String TRUST_TYPE = "trustStoreType";
     private static final String DS_CONTEXT = "java:comp/env/";
     private static final String CONN_DRIVER = "repositoryDriver";
     private static final String REPOSITORY_HOST = "repositoryHost";
     private static final String REPOSITORY_PORT = "repositoryPort";
     private static final String MIN_CONNECTIONS = "minConnections";
     private static final String MAX_CONNECTIONS = "maxConnections";
     private static final String REPOSITORY_USER = "repositoryUser";
     private static final String REPOSITORY_PASS = "repositoryPass";
     private static final String REPOSITORY_SALT = "repositorySalt";
     private static final String CONN_TIMEOUT = "repositoryConnTimeout";
     private static final String READ_TIMEOUT = "repositoryReadTimeout";
     private static final String CNAME = DAOInitializer.class.getName();
 
     private static final Logger DEBUGGER = LoggerFactory.getLogger(SecurityServiceConstants.DEBUGGER);
     private static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     private static final Logger ERROR_RECORDER = LoggerFactory.getLogger(SecurityServiceConstants.ERROR_LOGGER + DAOInitializer.CNAME);
 
     /**
      * @param authRepo - The <code>AuthRepo</code> object containing connection information
      * @param isContainer - A <code>boolean</code> flag indicating if this is in a container
      * @param bean - The <code>SecurityServiceBean</code> that holds the connection
      * @throws SecurityServiceException if an exception occurs opening the connection
      */
    public synchronized static void configureAndCreateAuthConnection(final String properties, final boolean isContainer, final SecurityServiceBean bean) throws SecurityServiceException
     {
        String methodName = DAOInitializer.CNAME + "#configureAndCreateAuthConnection(final String properties, final boolean isContainer, final SecurityServiceBean bean) throws SecurityServiceException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("File: {}", properties);
             DEBUGGER.debug("isContainer: {}", isContainer);
             DEBUGGER.debug("SecurityServiceBean: {}", bean);
         }
 
         try
         {
             Properties connProps = new Properties();
            connProps.load(new FileInputStream(FileUtils.getFile(properties)));
 
             if (DEBUG)
             {
                 DEBUGGER.debug("Properties: {}", connProps);
             }
 
             AuthRepositoryType repoType = AuthRepositoryType.valueOf(connProps.getProperty(DAOInitializer.REPO_TYPE));
 
             if (DEBUG)
             {
                 DEBUGGER.debug("AuthRepositoryType: {}", repoType);
             }
 
             switch (repoType)
             {
                 case LDAP:
                     LDAPConnection ldapConn = null;
                     LDAPConnectionOptions connOpts = new LDAPConnectionOptions();
 
                     connOpts.setAutoReconnect(true);
                     connOpts.setAbandonOnTimeout(true);
                     connOpts.setBindWithDNRequiresPassword(true);
                     connOpts.setConnectTimeoutMillis(Integer.parseInt(connProps.getProperty(DAOInitializer.CONN_TIMEOUT)));
                     connOpts.setResponseTimeoutMillis(Integer.parseInt(connProps.getProperty(DAOInitializer.READ_TIMEOUT)));
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("LDAPConnectionOptions: {}", connOpts);
                     }
 
                     if (Boolean.valueOf(connProps.getProperty(DAOInitializer.IS_SECURE)))
                     {
                         SSLUtil sslUtil = new SSLUtil(new TrustStoreTrustManager(
                                 connProps.getProperty(DAOInitializer.TRUST_FILE),
                                 connProps.getProperty(DAOInitializer.TRUST_PASS).toCharArray(),
                                 connProps.getProperty(DAOInitializer.TRUST_TYPE),
                                 true));
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SSLUtil: {}", sslUtil);
                         }
 
                         SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();
 
                         if (DEBUG)
                         {
                             DEBUGGER.debug("SSLSocketFactory: {}", sslSocketFactory);
                         }
 
                         ldapConn = new LDAPConnection(sslSocketFactory, connOpts, connProps.getProperty(DAOInitializer.REPOSITORY_HOST),
                                 Integer.parseInt(connProps.getProperty(DAOInitializer.REPOSITORY_PORT)),
                                 connProps.getProperty(DAOInitializer.REPOSITORY_USER),
                                 PasswordUtils.decryptText(connProps.getProperty(DAOInitializer.REPOSITORY_PASS),
                                         connProps.getProperty(DAOInitializer.REPOSITORY_SALT).length()));
                     }
                     else
                     {
                         ldapConn = new LDAPConnection(connOpts, connProps.getProperty(DAOInitializer.REPOSITORY_HOST),
                                 Integer.parseInt(connProps.getProperty(DAOInitializer.REPOSITORY_PORT)),
                                 connProps.getProperty(DAOInitializer.REPOSITORY_USER),
                                 PasswordUtils.decryptText(connProps.getProperty(DAOInitializer.REPOSITORY_PASS),
                                         connProps.getProperty(DAOInitializer.REPOSITORY_SALT).length()));
                     }
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("LDAPConnection: {}", ldapConn);
                     }
 
                     if (!(ldapConn.isConnected()))
                     {
                         throw new LDAPException(ResultCode.CONNECT_ERROR, "Failed to establish an LDAP connection");
                     }
 
                     LDAPConnectionPool connPool = new LDAPConnectionPool(ldapConn,
                             Integer.parseInt(connProps.getProperty(DAOInitializer.MIN_CONNECTIONS)),
                             Integer.parseInt(connProps.getProperty(DAOInitializer.MAX_CONNECTIONS)));
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("LDAPConnectionPool: {}", connPool);
                     }
 
                     if (connPool.isClosed())
                     {
                         throw new LDAPException(ResultCode.CONNECT_ERROR, "Failed to establish an LDAP connection");
                     }
 
                     bean.setAuthDataSource(connPool);
 
                     break;
                 case SQL:
                     // the isContainer only matters here
                     if (isContainer)
                     {
                         Context initContext = new InitialContext();
                         Context envContext = (Context) initContext.lookup(DAOInitializer.DS_CONTEXT);
 
                         bean.setAuthDataSource(envContext.lookup(DAOInitializer.REPOSITORY_HOST));
                     }
                     else
                     {
                         BasicDataSource dataSource = new BasicDataSource();
                         dataSource.setInitialSize(Integer.parseInt(connProps.getProperty(DAOInitializer.MIN_CONNECTIONS)));
                         dataSource.setMaxActive(Integer.parseInt(connProps.getProperty(DAOInitializer.MAX_CONNECTIONS)));
                         dataSource.setDriverClassName(connProps.getProperty(DAOInitializer.CONN_DRIVER));
                         dataSource.setUrl(connProps.getProperty(DAOInitializer.REPOSITORY_HOST));
                         dataSource.setUsername(connProps.getProperty(DAOInitializer.REPOSITORY_USER));
                         dataSource.setPassword(PasswordUtils.decryptText(
                                 connProps.getProperty(DAOInitializer.REPOSITORY_PASS),
                                 connProps.getProperty(DAOInitializer.REPOSITORY_SALT).length()));
 
                         bean.setAuthDataSource(dataSource);
                     }
 
                     break;
                 default:
                     throw new SecurityServiceException("Unhandled ResourceType");
             }
         }
         catch (LDAPException lx)
         {
             ERROR_RECORDER.error(lx.getMessage(), lx);
 
             throw new SecurityServiceException(lx.getMessage(), lx);
         }
         catch (GeneralSecurityException gsx)
         {
             ERROR_RECORDER.error(gsx.getMessage(), gsx);
 
             throw new SecurityServiceException(gsx.getMessage(), gsx);
         }
         catch (NamingException nx)
         {
             ERROR_RECORDER.error(nx.getMessage(), nx);
 
             throw new SecurityServiceException(nx.getMessage(), nx);
         }
         catch (FileNotFoundException fnfx)
         {
             ERROR_RECORDER.error(fnfx.getMessage(), fnfx);
 
             throw new SecurityServiceException(fnfx.getMessage(), fnfx);
         }
         catch (IOException iox)
         {
             ERROR_RECORDER.error(iox.getMessage(), iox);
 
             throw new SecurityServiceException(iox.getMessage(), iox);
         }
     }
 
     /**
      * @param authRepo - The <code>AuthRepo</code> object containing connection information
      * @param isContainer - A <code>boolean</code> flag indicating if this is in a container
      * @param bean - The <code>SecurityServiceBean</code> that holds the connection
      * @throws SecurityServiceException if an exception occurs closing the connection
      */
     public synchronized static void closeAuthConnection(final File properties, final boolean isContainer, final SecurityServiceBean bean) throws SecurityServiceException
     {
         String methodName = DAOInitializer.CNAME + "#closeAuthConnection(final File properties, final boolean isContainer, final SecurityServiceBean bean) throws SecurityServiceException";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
             DEBUGGER.debug("File: {}", properties);
             DEBUGGER.debug("isContainer: {}", isContainer);
             DEBUGGER.debug("SecurityServiceBean: {}", bean);
         }
 
         try
         {
             Properties connProps = new Properties();
             connProps.load(new FileInputStream(properties));
 
             if (DEBUG)
             {
                 DEBUGGER.debug("Properties: {}", connProps);
             }
 
             AuthRepositoryType repoType = AuthRepositoryType.valueOf(connProps.getProperty(DAOInitializer.REPO_TYPE));
 
             if (DEBUG)
             {
                 DEBUGGER.debug("AuthRepositoryType: {}", repoType);
             }
 
             switch (repoType)
             {
                 case LDAP:
                     LDAPConnectionPool ldapPool = (LDAPConnectionPool) bean.getAuthDataSource();
 
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
                         BasicDataSource dataSource = (BasicDataSource) bean.getAuthDataSource();
 
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
                     throw new SecurityServiceException("Unhandled ResourceType");
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
         }
         catch (FileNotFoundException fnfx)
         {
             ERROR_RECORDER.error(fnfx.getMessage(), fnfx);
         }
         catch (IOException iox)
         {
             ERROR_RECORDER.error(iox.getMessage(), iox);
         }
     }
 }
