 /*
  * #%L
  * Bitmagasin integrationstest
  * 
  * $Id: ReferencePillar.java 685 2012-01-06 16:35:17Z jolf $
  * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/main/java/org/bitrepository/pillar/ReferencePillar.java $
  * %%
  * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
 package org.bitrepository.pillar.referencepillar;
 
 import org.bitrepository.common.ArgumentValidator;
 import org.bitrepository.common.filestore.FileStore;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.utils.SettingsUtils;
 import org.bitrepository.pillar.Pillar;
 import org.bitrepository.pillar.cache.ChecksumDAO;
 import org.bitrepository.pillar.cache.ChecksumStore;
 import org.bitrepository.pillar.common.MessageHandlerContext;
 import org.bitrepository.pillar.common.PillarAlarmDispatcher;
 import org.bitrepository.pillar.common.SettingsHelper;
 import org.bitrepository.pillar.referencepillar.archive.CollectionArchiveManager;
 import org.bitrepository.pillar.referencepillar.archive.ReferenceChecksumManager;
 import org.bitrepository.pillar.referencepillar.messagehandler.ReferencePillarMediator;
 import org.bitrepository.pillar.referencepillar.scheduler.RecalculateChecksumJob;
 import org.bitrepository.protocol.CoordinationLayerException;
 import org.bitrepository.protocol.messagebus.MessageBus;
 import org.bitrepository.service.audit.AuditTrailContributerDAO;
 import org.bitrepository.service.audit.AuditTrailManager;
 import org.bitrepository.service.contributor.ResponseDispatcher;
 import org.bitrepository.service.database.DBConnector;
 import org.bitrepository.service.scheduler.JobScheduler;
 import org.bitrepository.service.scheduler.TimerbasedScheduler;
 import org.bitrepository.service.workflow.SchedulableJob;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.jms.JMSException;
 import java.lang.reflect.Constructor;
 import java.util.Arrays;
 
 /**
  * Reference pillar. This very simply starts the PillarMediator, which handles all the communications.
  */
 public class ReferencePillar implements Pillar {
     /** The log.*/
     private Logger log = LoggerFactory.getLogger(getClass());
     /** The messagebus for the pillar.*/
     private final MessageBus messageBus;
     /** The settings.*/
     private final Settings settings;
     /** The mediator for the messages.*/
     private final ReferencePillarMediator mediator;
     /** The archives for the data.*/
     private final FileStore archiveManager;
     /** The checksum store.*/
     private final ChecksumStore csStore;
     /** The scheduler for the recalculation workflows.*/
     private final JobScheduler scheduler;
     /** The manager of the checksums with regard to the archive.*/
     private final ReferenceChecksumManager manager;
     /** The default time for running the recalculation workflow, when the settings is not set.
      * The default is every hour. */
     private static final Long DEFAULT_RECALCULATION_WORKFLOW_TIME = 3600000L;
 
     /**
      * Constructor.
      * @param messageBus The messagebus for the communication.
      * @param settings The settings for the pillar.
      */
     public ReferencePillar(MessageBus messageBus, Settings settings) {
         ArgumentValidator.checkNotNull(messageBus, "messageBus");
         ArgumentValidator.checkNotNull(settings, "settings");
         this.messageBus = messageBus;
         SettingsUtils.initialize(settings);
         this.settings = settings;
 
         log.info("Starting the ReferencePillar");
         archiveManager = getFileStore(settings);
         csStore = new ChecksumDAO(settings);
         PillarAlarmDispatcher alarmDispatcher = new PillarAlarmDispatcher(settings, messageBus);
         manager = new ReferenceChecksumManager(archiveManager, csStore, alarmDispatcher, settings);
         AuditTrailManager audits = new AuditTrailContributerDAO(settings, new DBConnector(
                 settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase()));
         MessageHandlerContext context = new MessageHandlerContext(
                 settings,
                 SettingsHelper.getPillarCollections(settings.getComponentID(), settings.getCollections()),
                 new ResponseDispatcher(settings, messageBus),
                 alarmDispatcher,
                 audits);
         messageBus.setCollectionFilter(Arrays.asList(context.getPillarCollections()));
         mediator = new ReferencePillarMediator(messageBus, context, archiveManager, manager);
         mediator.start();
         
         this.scheduler = new TimerbasedScheduler();
         initializeWorkflows();
        log.info("ReferencePillar started for collections: " + Arrays.asList(context.getPillarCollections()));
     }
     
     /**
      * Initializes one RecalculateChecksums workflow for each collection.
      */
     private void initializeWorkflows() {
         Long interval = DEFAULT_RECALCULATION_WORKFLOW_TIME;
         if(settings.getReferenceSettings().getPillarSettings().getRecalculateOldChecksumsInterval() != null) {
             interval = settings.getReferenceSettings().getPillarSettings()
                     .getRecalculateOldChecksumsInterval().longValue();
         }
         for(String collectionId : SettingsUtils.getCollectionIDsForPillar(
                 settings.getReferenceSettings().getPillarSettings().getPillarID())) {
             SchedulableJob workflow = new RecalculateChecksumJob(collectionId, manager);
             scheduler.schedule(workflow, interval);
         }
     }
     
     /**
      * Retrieves the FileStore defined in the settings.
      * @param settings The settings.
      * @return The filestore from settings, or the CollectionArchiveManager, if the setting is missing.
      */
     private FileStore getFileStore(Settings settings) {
         if(settings.getReferenceSettings().getPillarSettings().getFileStoreClass() == null) {
             return new CollectionArchiveManager(settings);
         }
         
         try {
             Class<FileStore> fsClass = (Class<FileStore>) Class.forName(
                     settings.getReferenceSettings().getPillarSettings().getFileStoreClass());
             Constructor<FileStore> fsConstructor = fsClass.getConstructor(Settings.class);
             return fsConstructor.newInstance(settings);
         } catch (Exception e) {
             throw new CoordinationLayerException("Could not instantiate the FileStore", e);
         }
     }
 
     /**
      * Closes the ReferencePillar.
      */
     public void close() {
         try {
             mediator.close();
             messageBus.close();
             archiveManager.close();
             log.info("ReferencePillar stopped!");
         } catch (JMSException e) {
             log.warn("Could not close the messagebus.", e);
         }
     }
 }
