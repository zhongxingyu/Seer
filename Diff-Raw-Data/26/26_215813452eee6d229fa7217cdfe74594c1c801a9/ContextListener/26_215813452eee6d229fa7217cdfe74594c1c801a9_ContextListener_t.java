 package de.consistec.syncframework.server;
 
 /*
  * #%L
  * doppelganger
  * %%
  * Copyright (C) 2011 - 2013 consistec GmbH
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
 *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
 import de.consistec.syncframework.common.Config;
 import de.consistec.syncframework.common.exception.ConfigException;
 import de.consistec.syncframework.common.util.PropertiesUtil;
 import de.consistec.syncframework.impl.proxy.http_servlet.HttpServletProcessor;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.ServletException;
 import javax.sql.DataSource;
 import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 //import javax.servlet.annotation.WebListener;
 
 /**
  * Web application lifecycle listener.
  * <ul>
  * <li><b>Company:</b> consistec Engineering and Consulting GmbH</li>
  * </ul>
  * <p/>
  *
  * @author Piotr Wieczorek
  * @since 0.0.1-SNAPSHOT
  */
 //@WebListener("Listener initializes syncframework")
 public class ContextListener implements ServletContextListener {
 
     public static final String SERVER_OPTION_POOLING = "pooling";
     public static final String SERVER_OPTION_DEBUG = "debug";
     public static final String HTTP_PROCESSOR_CTX_ATTR = "HTTP_PROCESSOR";
     private static final Logger LOGGER = LoggerFactory.getLogger(ContextListener.class.getCanonicalName());
     private static final String PARAMA_SYNC_CONFIG_FILE_NAME = "sync_config_file_name";
     private static final String PARAMA_SERVER_CONFIG_FILE_NAME = "server_config_file_name";
     private static final String DEFAULT_FRAMEWORK_CONFIG_FILE = "syncframework.properties";
     private static final String DEFAULT_SERVER_CONFIG_FILE = "server.properties";
     private ServletContext ctx;
     private boolean isPooling;
     private boolean isDebugEnabled;
     private final String configFileName;
     private InitialContext initCxt;
 
     public ContextListener(String configFileName) {
         this.configFileName = configFileName;
         try {
             initCxt = new InitialContext();
         } catch (NamingException e) {
             throw new RuntimeException("Could not create initial context", e);
         }
     }
 
     public ContextListener() {
         this("/WEB-INF/" + DEFAULT_FRAMEWORK_CONFIG_FILE);
     }
 
     @Override
     public void contextInitialized(ServletContextEvent sce) {
 
         LOGGER.info("ContextListener startet ...");
 
         ctx = sce.getServletContext();
 
 
         try {
             loadSyncFrameworkConfig();
             LOGGER.info("Sync framework configuration loaded");
         } catch (IOException ex) {
             throw new ConfigException("Error while loading configuration for synchronization framework", ex);
         }
 
         try {
             loadServerConfig();
         } catch (IOException ex) {
             throw new ConfigException("Error while loading server configuration", ex);
         }
 
         try {
             prepareHttpProcessor();
         } catch (Exception ex) {
             throw new RuntimeException("Couldn't construct Http processor", ex);
         }
 
     }
 
     private void prepareHttpProcessor() throws Exception {
 
         HttpServletProcessor processor;
 
         if (isPooling) {
 
             initCxt = new InitialContext();
             if (initCxt == null) {
                 throw new Exception("Uh oh -- no context!");
             }
             DataSource ds = (DataSource) initCxt.lookup("java:/comp/env/jdbc/sync");
 
             if (ds == null) {
                 throw new ServletException("Data source not found!");
             } else {
                 LOGGER.debug("Datasource found");
             }
 
             processor = new HttpServletProcessor(ds, isDebugEnabled);
         } else {
             processor = new HttpServletProcessor(isDebugEnabled);
         }
 
         ctx.setAttribute(HTTP_PROCESSOR_CTX_ATTR, processor);
     }
 
     private String readParamFromContext(String defValue, String paramName) {
         return PropertiesUtil.defaultIfNull(defValue, ctx.getInitParameter(paramName));
     }
 
     private File readFile(String fileName) {
         if (fileName.contains("WEB-INF")) {
             return new File(ctx.getRealPath(fileName));
         }
 
         return new File(ctx.getRealPath("/WEB-INF/" + fileName));
     }
 
     private void loadSyncFrameworkConfig() throws IOException {
 
         FileInputStream fis = null;
 
         try {
             fis = new FileInputStream(readFile(readParamFromContext(configFileName,
                 PARAMA_SYNC_CONFIG_FILE_NAME)));
             Config config = Config.getInstance();
             config.init(fis);
         } finally {
             if (fis != null) {
                 try {
                     fis.close();
                 } catch (IOException ex) {
                     LOGGER.warn("Can't close sync framework config file");
                 }
             }
         }
     }
 
     private void loadServerConfig() throws IOException {
 
         FileInputStream fis = null;
         Properties props = new Properties();
 
         try {
             fis = new FileInputStream(readFile(readParamFromContext(DEFAULT_SERVER_CONFIG_FILE,
                 PARAMA_SERVER_CONFIG_FILE_NAME)));
             props.load(fis);
 
         } finally {
             if (fis != null) {
                 try {
                     fis.close();
                 } catch (IOException ex) {
                     LOGGER.warn("Can't close server config file");
                 }
             }
         }
 
         isPooling = PropertiesUtil.defaultIfNull(Boolean.FALSE, PropertiesUtil.readBoolean(props, SERVER_OPTION_POOLING,
             false)).booleanValue();
         LOGGER.debug("db pooling is {}", isPooling);
         ctx.setAttribute(SERVER_OPTION_POOLING, isPooling);
 
         isDebugEnabled = PropertiesUtil.defaultIfNull(Boolean.FALSE,
             PropertiesUtil.readBoolean(props, SERVER_OPTION_DEBUG,
                 false)).booleanValue();
         LOGGER.debug("debug mode {}", isDebugEnabled);
         ctx.setAttribute(SERVER_OPTION_DEBUG, isDebugEnabled);
     }
 
     @Override
     public void contextDestroyed(final ServletContextEvent servletContextEvent) {
 
         try {
            if (initCxt.lookup("java:/comp/env/jdbc/sync") instanceof BasicDataSource)
            {
                 BasicDataSource ds = (BasicDataSource) initCxt.lookup("java:/comp/env/jdbc/sync");
                try {
                    if (ds != null) {
                        ds.close();
                    }
                } catch (Exception e) {
                    LOGGER.warn("Couldn't close connection");
                 }
             }
         } catch (Exception e) {
             throw new RuntimeException("Couldn't get datasource to close connections", e);
         }
 
         LOGGER.info("ContextListener finished ...");
     }
 }
