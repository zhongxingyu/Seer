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
 package com.cws.esolutions.security.listeners;
 /*
  * Project: eSolutionsSecurity
  * Package: com.cws.esolutions.security.listeners
  * File: SecurityServiceInitializer.java
  *
  * History
  *
  * Author               Date                            Comments
  * ----------------------------------------------------------------------------
  * kmhuntly@gmail.com   11/23/2008 22:39:20             Created.
  */
 import java.net.URL;
 import java.util.Map;
 import org.slf4j.Logger;
 import java.util.HashMap;
 import javax.sql.DataSource;
 import java.sql.SQLException;
 import org.slf4j.LoggerFactory;
 import java.io.FileInputStream;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.JAXBException;
 import java.io.FileNotFoundException;
 import org.apache.log4j.helpers.Loader;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.apache.commons.dbcp.BasicDataSource;
 
 import com.cws.esolutions.security.SecurityServiceBean;
 import com.cws.esolutions.security.utils.PasswordUtils;
 import com.cws.esolutions.security.utils.DAOInitializer;
 import com.cws.esolutions.security.SecurityServiceConstants;
 import com.cws.esolutions.security.config.xml.DataSourceManager;
 import com.cws.esolutions.security.exception.SecurityServiceException;
 import com.cws.esolutions.security.config.xml.SecurityConfigurationData;
 /**
  * Interface for the Application Data DAO layer. Allows access
  * into the asset management database to obtain, modify and remove
  * application information.
  *
  * @author khuntly
  * @version 1.0
  */
 public class SecurityServiceInitializer
 {
     private static final String CNAME = SecurityServiceInitializer.class.getName();
     private static final SecurityServiceBean svcBean = SecurityServiceBean.getInstance();
 
     private static final Logger DEBUGGER = LoggerFactory.getLogger(SecurityServiceConstants.DEBUGGER);
     private static final boolean DEBUG = DEBUGGER.isDebugEnabled();
     private static final Logger ERROR_RECORDER = LoggerFactory.getLogger(SecurityServiceConstants.ERROR_LOGGER + CNAME);
 
     public static void initializeService(final String secConfig, final String logConfig) throws SecurityServiceException
     {
         URL xmlURL = null;
         JAXBContext context = null;
         Unmarshaller marshaller = null;
         SecurityConfigurationData configData = null;
 
         final ClassLoader classLoader = SecurityServiceInitializer.class.getClassLoader();
 
         try
         {
             if (StringUtils.isEmpty(logConfig))
             {
                 System.err.println("Logging configuration not found. No logging enabled !");
             }
             else
             {
                 // Load logging
                 DOMConfigurator.configure(Loader.getResource(logConfig));
             }
 
             xmlURL = classLoader.getResource(secConfig);
 
             if (xmlURL == null)
             {
                 throw new SecurityServiceException("Failed to load service configuration.");
             }
 
             context = JAXBContext.newInstance(SecurityConfigurationData.class);
             marshaller = context.createUnmarshaller();
             configData = (SecurityConfigurationData) marshaller.unmarshal(xmlURL);
 
             SecurityServiceInitializer.svcBean.setConfigData(configData);
 
             DAOInitializer.configureAndCreateAuthConnection(new FileInputStream(FileUtils.getFile(configData.getSecurityConfig().getAuthConfig())),
                     false, SecurityServiceInitializer.svcBean);
 
             Map<String, DataSource> dsMap = SecurityServiceInitializer.svcBean.getDataSources();
 
             if (DEBUG)
             {
                 DEBUGGER.debug("dsMap: {}", dsMap);
             }
 
             if (dsMap == null)
             {
                 dsMap = new HashMap<>();
             }
 
             for (DataSourceManager mgr : configData.getResourceConfig().getDsManager())
             {
                 if (!(dsMap.containsKey(mgr.getDsName())))
                 {
                     StringBuilder sBuilder = new StringBuilder()
                         .append("connectTimeout=" + mgr.getConnectTimeout() + ";")
                         .append("socketTimeout=" + mgr.getConnectTimeout() + ";")
                         .append("autoReconnect=" + mgr.getAutoReconnect() + ";")
                         .append("zeroDateTimeBehavior=convertToNull");
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("StringBuilder: {}", sBuilder);
                     }
 
                     BasicDataSource dataSource = new BasicDataSource();
                     dataSource.setDriverClassName(mgr.getDriver());
                     dataSource.setUrl(mgr.getDataSource());
                     dataSource.setUsername(mgr.getDsUser());
                     dataSource.setConnectionProperties(sBuilder.toString());
                     dataSource.setPassword(PasswordUtils.decryptText(mgr.getDsPass(), mgr.getSalt().length()));
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("BasicDataSource: {}", dataSource);
                     }
 
                     dsMap.put(mgr.getDsName(), dataSource);
                 }
             }
 
             if (DEBUG)
             {
                 DEBUGGER.debug("dsMap: {}", dsMap);
             }
 
             SecurityServiceInitializer.svcBean.setDataSources(dsMap);
         }
         catch (JAXBException jx)
         {
             throw new SecurityServiceException(jx.getMessage(), jx);
         }
         catch (FileNotFoundException fnfx)
         {
             throw new SecurityServiceException(fnfx.getMessage(), fnfx);
         }
     }
 
     public static void shutdown()
     {
         final String methodName = SecurityServiceInitializer.CNAME + "#shutdown()";
 
         if (DEBUG)
         {
             DEBUGGER.debug(methodName);
         }
 
         final SecurityConfigurationData config = SecurityServiceInitializer.svcBean.getConfigData();
         Map<String, DataSource> dsMap = SecurityServiceInitializer.svcBean.getDataSources();
 
         if (DEBUG)
         {
             DEBUGGER.debug("SecurityConfigurationData: {}", config);
         }
 
         try
         {
             DAOInitializer.closeAuthConnection(new FileInputStream(FileUtils.getFile(config.getSecurityConfig().getAuthConfig())), false, svcBean);
 
             if (dsMap != null)
             {
                 for (String key : dsMap.keySet())
                 {
                     if (DEBUG)
                     {
                         DEBUGGER.debug("Key: {}", key);
                     }
 
                     BasicDataSource dataSource = (BasicDataSource) dsMap.get(key);
 
                     if (DEBUG)
                     {
                         DEBUGGER.debug("BasicDataSource: {}", dataSource);
                     }
 
                     if ((dataSource != null ) && (!(dataSource.isClosed())))
                     {
                         dataSource.close();
                     }
                 }
             }
         }
         catch (SQLException sqx)
         {
             ERROR_RECORDER.error(sqx.getMessage(), sqx);
         }
         catch (SecurityServiceException ssx)
         {
             ERROR_RECORDER.error(ssx.getMessage(), ssx);
         }
     }
 }
