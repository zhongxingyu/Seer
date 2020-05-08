 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.persistence;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.eclipse.jgit.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.persist.PersistService;
 import com.meltmedia.cadmium.core.config.ConfigurationListener;
 
 /**
  * Listens for persistence configuration changes and manages the Jpa Service.
  * 
  * @author John McEntire
  *
  */
 @Singleton
 public class PersistenceConfigurationListener implements
     ConfigurationListener<PersistenceConfiguration>, Closeable {
   private static final Logger log = LoggerFactory.getLogger(PersistenceConfigurationListener.class);
   
   @CadmiumJpaProperties
   protected Properties jpaOverrideProperties;
   
   @Inject
   protected PersistService jpaService;
 
   @Override
   public void configurationUpdated(Object configuration) {
     log.debug("received a new configuration for the jpa persistence service.");
     PersistenceConfiguration cfg = (PersistenceConfiguration) configuration;
     Properties updatedProps = new Properties();
     if(!StringUtils.isEmptyOrNull(cfg.getJndiName())) {
       updatedProps.setProperty("javax.persistence.nonJtaDataSource", cfg.getJndiName());
       log.debug("JNDIName: "+cfg.getJndiName());
     }
     if(!StringUtils.isEmptyOrNull(cfg.getDatabaseDialect())) {
       updatedProps.setProperty("hibernate.dialect", cfg.getDatabaseDialect());
       log.debug("Dialect: "+cfg.getDatabaseDialect());
     }
     if(cfg.getExtraConfiguration() != null) {
       updatedProps.putAll(cfg.getExtraConfiguration());
       log.debug("Extra Config: "+cfg.getExtraConfiguration());
     }
     Properties oldJpaProperties = new Properties();
     oldJpaProperties.putAll(jpaOverrideProperties);
     if(!jpaOverrideProperties.equals(updatedProps)) { 
       try {
         try {
           log.debug("Stopping jpa service...");
           jpaService.stop();
         } catch(Throwable t) {
           log.debug("Jpa service has already been stopped.", t);
         }
         log.debug("Setting override properties to new values.");
         jpaOverrideProperties.clear();
         jpaOverrideProperties.putAll(updatedProps);
         log.debug("Starting jpa service...");
         jpaService.start();
         log.debug("Jpa service is up and running.");
       } catch(Throwable t) {
         log.error("Failed to set new properties into jpa service. Resetting back to older values.", t);
         jpaOverrideProperties.clear();
         jpaOverrideProperties.putAll(oldJpaProperties);
         jpaService.start();
       }
     }
   }
 
   @Override
   public void configurationNotFound() {
     try {
       log.debug("No persistence configuration found. Making sure jpa service is not running.");
       jpaService.stop();
     } catch(Throwable t) {
       log.debug("Jpa service has already been stopped.");
     }
   }
 
   @Override
   public void close() throws IOException {
     try {
       jpaService.stop();
     } catch(Throwable t) {
       log.debug("Jpa service has already been stopped.");
     }
   }
 
 }
