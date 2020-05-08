 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
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
  *
  * Authors:
  * 	Andrea Ceccanti (INFN)
  */
 package org.glite.security.voms.admin.core;
 
 import it.infn.cnaf.voms.x509.X509ACGenerator;
 
 import java.util.Properties;
 
 import javax.servlet.ServletContext;
 
 import org.apache.velocity.app.Velocity;
 import org.glite.security.voms.admin.configuration.VOMSConfiguration;
 import org.glite.security.voms.admin.configuration.VOMSConfigurationConstants;
 import org.glite.security.voms.admin.configuration.VOMSConfigurationException;
 import org.glite.security.voms.admin.core.tasks.ExpiredRequestsPurgerTask;
 import org.glite.security.voms.admin.core.tasks.TaskStatusUpdater;
 import org.glite.security.voms.admin.core.tasks.ThreadUncaughtExceptionHandler;
 import org.glite.security.voms.admin.core.tasks.UpdateCATask;
 import org.glite.security.voms.admin.core.tasks.UserStatsTask;
 import org.glite.security.voms.admin.core.tasks.VOMSExecutorService;
 import org.glite.security.voms.admin.core.validation.ValidationManager;
 import org.glite.security.voms.admin.error.VOMSFatalException;
 import org.glite.security.voms.admin.event.DebugEventLogListener;
 import org.glite.security.voms.admin.event.EventManager;
 import org.glite.security.voms.admin.integration.PluginManager;
 import org.glite.security.voms.admin.notification.CertificateRequestsNotificationDispatcher;
 import org.glite.security.voms.admin.notification.DefaultNotificationDispatcher;
 import org.glite.security.voms.admin.notification.GroupMembershipNotificationDispatcher;
 import org.glite.security.voms.admin.notification.MembershipRemovalNotificationDispatcher;
 import org.glite.security.voms.admin.notification.NotificationService;
 import org.glite.security.voms.admin.notification.RoleMembershipNotificationDispatcher;
 import org.glite.security.voms.admin.notification.VOMembershipNotificationDispatcher;
 import org.glite.security.voms.admin.persistence.HibernateFactory;
 import org.glite.security.voms.admin.persistence.dao.VOMSVersionDAO;
 import org.opensaml.xml.ConfigurationException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public final class VOMSService {
 
 	static final Logger log = LoggerFactory.getLogger(VOMSService.class);
 
 	protected static void checkDatabaseVersion() {
 
 		int version = VOMSVersionDAO.instance().getVersion().getVersion();
 
 		if (version != VOMSServiceConstants.VOMS_DB_VERSION) {
 
 			if (version < VOMSServiceConstants.VOMS_DB_VERSION) {
 				log
 						.error("VOMS DATABASE SCHEMA ERROR: old voms database schema found: version "
 								+ version);
 				log
 						.error("PLEASE UPGRADE TO CURRENT VERSION, usign voms-admin-configure or voms-db-util commands!");
 				throw new VOMSFatalException(
 						"INCOMPATIBLE DATABASE SCHEMA FOUND! Is '" + version
 								+ "', while it should be '"
 								+ VOMSServiceConstants.VOMS_DB_VERSION + "'");
 			} else {
 
 				log
 						.error("UNKNOWN SCHEMA VERSION NUMBER FOUND IN DATABASE! version: "
 								+ version);
 				throw new VOMSFatalException(
 						"INCOMPATIBLE DATABASE SCHEMA FOUND! Is '" + version
 								+ "', while it should be '"
 								+ VOMSServiceConstants.VOMS_DB_VERSION + "'");
 			}
 		}
 	}
 
 	protected static void configureVelocity() {
 
 		try {
 
 			Properties p = new Properties();
 
 			p.put("resource.loader", "cpath");
 			p
 					.put("cpath.resource.loader.class",
 							"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
 
 			p.put("runtime.log.logsystem.class",
 			"org.glite.security.voms.admin.util.velocity.VelocityLogger");
 
 			Velocity.init(p);
 			log.debug("Velocity setup ok!");
 
 		} catch (Exception e) {
 
 			log.error("Error initializing velocity template engine!");
 			throw new VOMSFatalException(e);
 		}
 
 	}
 
 	protected static void configureEventManager() {
 
 		EventManager.instance();
 		
 		DebugEventLogListener.instance();
 		
 		DefaultNotificationDispatcher.instance();
 		
 		GroupMembershipNotificationDispatcher.instance();
 		
 		RoleMembershipNotificationDispatcher.instance();
 		
 		VOMembershipNotificationDispatcher.instance();
 		
 		CertificateRequestsNotificationDispatcher.instance();
 		
 		MembershipRemovalNotificationDispatcher.instance();
 	}
 	
 	
 	protected static void startBackgroundTasks() {
 		
 		
 		VOMSExecutorService es = VOMSExecutorService.instance();
 		
		es.startBackgroundTask(new UpdateCATask(),
				VOMSConfigurationConstants.TRUST_ANCHORS_REFRESH_PERIOD);
 				
 		es.startBackgroundTask(new TaskStatusUpdater(), 
 				null, 
 				30L);
 		
 		es.startBackgroundTask(new ExpiredRequestsPurgerTask(), 
 				VOMSConfigurationConstants.VO_MEMBERSHIP_EXPIRED_REQ_PURGER_PERIOD, 
 				300L);
 		
 		es.startBackgroundTask(new UserStatsTask(), VOMSConfigurationConstants.MONITORING_USER_STATS_UPDATE_PERIOD, UserStatsTask.DEFAULT_PERIOD_IN_SECONDS);
 	}
 
 	protected static void bootstrapAttributeAuthorityServices() {
 
 		VOMSConfiguration conf = VOMSConfiguration.instance();
 
 		try {
 			org.opensaml.DefaultBootstrap.bootstrap();
 
 		} catch (ConfigurationException e) {
 
 			log.error("Error initializing OpenSAML:" + e.getMessage());
 
 			if (log.isDebugEnabled())
 				log.error("Error initializing OpenSAML:" + e.getMessage(), e);
 
 			log.info("SAML endpoint will not be activated.");
 			conf.setProperty(VOMSConfigurationConstants.VOMS_AA_SAML_ACTIVATE_ENDPOINT,
 					false);
 		}
 		
 		X509ACGenerator.instance(conf.getServiceCertificate(), conf.getServicePrivateKey());
 
 	}
 
 	public static void start(ServletContext ctxt) {
 
 		Thread
 				.setDefaultUncaughtExceptionHandler(new ThreadUncaughtExceptionHandler());
 		
 		VOMSConfiguration conf;
 
 		try {
 
 			conf = VOMSConfiguration.load(ctxt);
 
 		} catch (VOMSConfigurationException e) {
 			log.error("VOMS-Admin configuration failed!", e);
 			throw new VOMSFatalException(e);
 		}
 
 		log.info("VOMS-Admin starting for VO: " + conf.getVOName());
 
 		checkDatabaseVersion();
 
 		configureVelocity();
 
 		configureEventManager();
 
 		startBackgroundTasks();
 
 		bootstrapAttributeAuthorityServices();
 		
 		PluginManager.instance().configurePlugins();
 		
 		ValidationManager.instance().startMembershipChecker();
 
 		log.info("VOMS-Admin started succesfully.");
 	}
 
 	public static void stop() {
 		
 		VOMSExecutorService.shutdown();
 		
 		NotificationService.shutdown();
 		
 		HibernateFactory.shutdown();
 
 		
 		log.info("VOMS admin stopped .");
 	}
 
 }
