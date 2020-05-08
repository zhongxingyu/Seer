 /*
  *
  * This is a simple Content Management System (CMS)
  * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.smartitengineering.emailq.binder.guice;
 
 import com.smartitengineering.cms.api.common.MediaType;
 import com.smartitengineering.cms.api.factory.SmartContentAPI;
 import com.smartitengineering.cms.api.factory.type.WritableContentType;
 import com.smartitengineering.cms.api.factory.workspace.WorkspaceAPI;
 import com.smartitengineering.cms.api.workspace.Workspace;
 import com.smartitengineering.cms.api.workspace.WorkspaceId;
 import com.smartitengineering.dao.hbase.ddl.HBaseTableGenerator;
 import com.smartitengineering.dao.hbase.ddl.config.json.ConfigurationJsonParser;
 import com.smartitengineering.dao.impl.hbase.HBaseConfigurationFactory;
 import com.smartitengineering.util.bean.PropertiesLocator;
 import com.smartitengineering.util.bean.guice.GuiceUtil;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Properties;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.MasterNotRunningException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author imyousuf
  */
 public final class Initializer {
 
   private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);
   public static final String PROP_FILE = "com/smartitengineering/emailq/binder/guice/emailq-modules.properties";
 
   private Initializer() {
   }
 
   public static void init() {
     //Create CMS Tables
     Configuration config = HBaseConfigurationFactory.getConfigurationInstance();
     final ClassLoader classLoader = Initializer.class.getClassLoader();
     try {
       new HBaseTableGenerator(ConfigurationJsonParser.getConfigurations(classLoader.getResourceAsStream(
           "com/smartitengineering/cms/spi/impl/schema.json")), config, false).generateTables();
     }
     catch (MasterNotRunningException ex) {
       LOGGER.error("Master could not be found!", ex);
     }
     catch (Exception ex) {
       LOGGER.error("Could not create table!", ex);
     }
     //Initialize CMS API
     com.smartitengineering.cms.binder.guice.Initializer.init();
     //Create workspace and content type
     PropertiesLocator propertiesLocator = new PropertiesLocator();
     propertiesLocator.setSmartLocations(PROP_FILE);
     Properties properties = new Properties();
     try {
       propertiesLocator.loadProperties(properties);
     }
     catch (Exception ex) {
       throw new IllegalStateException(ex);
     }
     LOGGER.info("Binder properties " + properties);
     PropertiesLocator mainPropertiesLocator = new PropertiesLocator();
     mainPropertiesLocator.setSmartLocations(properties.getProperty(EmailModule.DOMAIN_PROPS));
     Properties mainProps = new Properties();
     try {
       mainPropertiesLocator.loadProperties(mainProps);
     }
     catch (Exception ex) {
       throw new IllegalStateException(ex);
     }
     LOGGER.info("Domain properties " + mainProps);
     String workspaceIdNamespace = mainProps.getProperty(
         "com.smartitengineering.emailq.domains.workspaceId.namespace", "");
     String workspaceIdName = mainProps.getProperty("com.smartitengineering.emailq.domains.workspaceId.name",
                                                    "");
     final WorkspaceAPI workspaceApi = SmartContentAPI.getInstance().getWorkspaceApi();
     WorkspaceId workspaceId = workspaceApi.createWorkspaceId(workspaceIdNamespace, workspaceIdName);
    Workspace workspace = workspaceId.getWorkspace();
     if (workspace == null) {
       workspaceApi.createWorkspace(workspaceId);
     }
     try {
       InputStream stream = classLoader.getResourceAsStream(
           "com/smartitengineering/emailq/domain/domain-content-type.xml");
       if (stream == null) {
         throw new IllegalArgumentException("Content Type XML Does not exist");
       }
       Collection<WritableContentType> types = SmartContentAPI.getInstance().getContentTypeLoader().parseContentTypes(
           workspaceId, stream, MediaType.APPLICATION_XML);
       for (WritableContentType type : types) {
         type.put();
       }
     }
     catch (Exception ex) {
       throw new IllegalStateException(ex);
     }
     //DI Generator Engine
     GuiceUtil.getInstance(PROP_FILE).register();
   }
 }
