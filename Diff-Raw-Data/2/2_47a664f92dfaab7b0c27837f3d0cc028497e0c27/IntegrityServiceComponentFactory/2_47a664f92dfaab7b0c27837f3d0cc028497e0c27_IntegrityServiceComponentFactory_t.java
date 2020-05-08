 /*
  * #%L
  * Bitrepository Protocol
  * *
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.integrityservice;
 
 import org.bitrepository.access.AccessComponentFactory;
 import org.bitrepository.access.getchecksums.GetChecksumsClient;
 import org.bitrepository.access.getfileids.GetFileIDsClient;
 import org.bitrepository.common.database.DBConnector;
 import org.bitrepository.common.database.DBSpecifics;
 import org.bitrepository.common.database.DatabaseSpecificsFactory;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.integrityservice.alerter.IntegrityAlarmDispatcher;
 import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
 import org.bitrepository.integrityservice.cache.IntegrityDatabase;
 import org.bitrepository.integrityservice.cache.IntegrityModel;
 import org.bitrepository.integrityservice.checking.IntegrityChecker;
 import org.bitrepository.integrityservice.checking.SimpleIntegrityChecker;
 import org.bitrepository.integrityservice.collector.DelegatingIntegrityInformationCollector;
 import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
 import org.bitrepository.integrityservice.workflow.IntegrityWorkflowScheduler;
 import org.bitrepository.integrityservice.workflow.TimerWorkflowScheduler;
 import org.bitrepository.protocol.ProtocolComponentFactory;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.bitrepository.protocol.security.SecurityManager;
 import org.bitrepository.service.audit.AuditTrailContributerDAO;
 import org.bitrepository.service.audit.AuditTrailManager;
 import org.bitrepository.service.contributor.ContributorContext;
 
 /**
  * Provides access to the different component in the integrity module (Spring/IOC wannabe)
  */
 public final class IntegrityServiceComponentFactory {
 
     //---------------------Singleton-------------------------
     private static IntegrityServiceComponentFactory instance;
 
     /**
      * The singletonic access to the instance of this class
      * @return The one and only instance
      */
     public static synchronized IntegrityServiceComponentFactory getInstance() {
         if (instance == null) {
             instance = new IntegrityServiceComponentFactory();
         }
         return instance;
     }
 
     /**
      * The singleton constructor.
      */
     private IntegrityServiceComponentFactory() {}
 
     // --------------------- Components-----------------------
     /** The integrity information scheduler. */
     private IntegrityWorkflowScheduler integrityInformationScheduler;
     /** The integrity information collector. */
     private IntegrityInformationCollector integrityInformationCollector;
     /** The integrity information collector. */
     private IntegrityModel cachedIntegrityInformationStorage;
     /** The integrity checker. */
     private IntegrityChecker integrityChecker;
 
     /**
      * Gets you an <code>IntegrityInformationScheduler</code> that schedules integrity information collection.
      * @param settings The settings for the information scheduler.
      * @return an <code>IntegrityInformationScheduler</code> that schedules integrity information collection.
      */
     public IntegrityWorkflowScheduler getIntegrityInformationScheduler(Settings settings) {
         if (integrityInformationScheduler == null) {
             integrityInformationScheduler = new TimerWorkflowScheduler(settings);
         }
         return integrityInformationScheduler;
     }
 
     /**
      * Gets you an <code>IntegrityInformationCollector</code> that collects integrity information.
      * @param getFileIDsClient The client for performing the collecting of the file ids.
      * @param getChecksumsClient The client for performing the collecting of the checksums.
      * @param auditManager The manager of audit trails.
      * @return an <code>IntegrityInformationCollector</code> that collects integrity information.
      */
     public IntegrityInformationCollector getIntegrityInformationCollector(GetFileIDsClient getFileIDsClient, 
             GetChecksumsClient getChecksumsClient, AuditTrailManager auditManager) {
         if (integrityInformationCollector == null) {
             integrityInformationCollector = new DelegatingIntegrityInformationCollector(getFileIDsClient, 
                     getChecksumsClient, auditManager);
         }
         return integrityInformationCollector;
     }
     
     /**
      * Gets you an <code>IntegrityChecker</code> the can perform the integrity checks.
      * @param settings The settings for this instance. 
      * @param cache The cache for the integrity system.
      * @param auditManager The manager of audit trails.
      * @return An <code>IntegrityChecker</code> the can perform the integrity checks.
      */
     public IntegrityChecker getIntegrityChecker(Settings settings, IntegrityModel cache, 
             AuditTrailManager auditManager) {
         if(integrityChecker == null) {
             integrityChecker = new SimpleIntegrityChecker(settings, cache, auditManager);
         }
         return integrityChecker;
     }
 
     /**
      * Gets you a <code>CachedIntegrityInformationStorage</code> that collects integrity information.
      * TODO implement the database based integrity cache.
      * @return an <code>CachedIntegrityInformationStorage</code> that collects integrity information.
      */
     public IntegrityModel getCachedIntegrityInformationStorage(Settings settings) {
         if (cachedIntegrityInformationStorage == null) {
             cachedIntegrityInformationStorage = new IntegrityDatabase(settings);
         }
         return cachedIntegrityInformationStorage;
     }
     
     /**
      * Creates an instance og the SimpleIntegrityService.
      * @param settings The settings for the service. The component ID will be set to the integrity service ID.
      * @param securityManager The security manager.
      * @return The integrity service.
      */
     public IntegrityService createIntegrityService(Settings settings, SecurityManager securityManager) {
         MessageBus messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
         ContributorContext context = new ContributorContext(messageBus, settings);
         DBSpecifics dbSpecifics = DatabaseSpecificsFactory.retrieveDBSpecifics(
                settings.getReferenceSettings().getIntegrityServiceSettings().getAuditContributerDatabaseSpecifics());
         AuditTrailManager auditManager = new AuditTrailContributerDAO(settings, new DBConnector(dbSpecifics, 
                 settings.getReferenceSettings().getIntegrityServiceSettings().getAuditContributerDatabaseUrl()));
         
         IntegrityModel model = getCachedIntegrityInformationStorage(settings);
         IntegrityWorkflowScheduler scheduler = getIntegrityInformationScheduler(settings);
         IntegrityChecker checker = getIntegrityChecker(settings, model, auditManager);
         IntegrityAlerter alarmDispatcher = new IntegrityAlarmDispatcher(context);
         
         IntegrityInformationCollector collector = getIntegrityInformationCollector( 
                 AccessComponentFactory.getInstance().createGetFileIDsClient(settings, securityManager, 
                         settings.getReferenceSettings().getIntegrityServiceSettings().getID()),
                 AccessComponentFactory.getInstance().createGetChecksumsClient(settings, securityManager, 
                         settings.getReferenceSettings().getIntegrityServiceSettings().getID()), 
                         auditManager);
         
         return new SimpleIntegrityService(model, scheduler, checker, alarmDispatcher, collector, auditManager, 
                 settings, messageBus);
     }
 }
