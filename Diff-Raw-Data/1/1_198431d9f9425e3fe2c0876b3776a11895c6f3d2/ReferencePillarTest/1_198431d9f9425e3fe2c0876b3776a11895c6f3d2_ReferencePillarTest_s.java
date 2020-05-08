 /*
  * #%L
  * Bitrepository Reference Pillar
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.util.Date;
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.common.utils.Base16Utils;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.common.utils.ChecksumUtils;
 import org.bitrepository.common.utils.FileUtils;
 import org.bitrepository.pillar.DefaultFixturePillarTest;
 import org.bitrepository.pillar.cache.ChecksumStore;
 import org.bitrepository.pillar.cache.MemoryCacheMock;
 import org.bitrepository.pillar.common.MessageHandlerContext;
 import org.bitrepository.pillar.common.PillarAlarmDispatcher;
 import org.bitrepository.pillar.referencepillar.archive.CollectionArchiveManager;
 import org.bitrepository.pillar.referencepillar.archive.ReferenceChecksumManager;
 import org.bitrepository.pillar.referencepillar.messagehandler.ReferencePillarMediator;
 import org.bitrepository.service.AlarmDispatcher;
 import org.bitrepository.service.audit.MockAuditManager;
 import org.bitrepository.service.contributor.ResponseDispatcher;
 
 public abstract class ReferencePillarTest extends DefaultFixturePillarTest {
     protected CollectionArchiveManager archives;
     protected ReferenceChecksumManager csManager;
     protected ReferencePillarMediator mediator;
     protected MockAuditManager audits;
     protected ChecksumStore csCache;
     protected MessageHandlerContext context;
     protected AlarmDispatcher alarmDispatcher;
 
     protected static final String EMPTY_FILE_CHECKSUM = "d41d8cd98f00b204e9800998ecf8427e";
 
     protected static final ChecksumDataForFileTYPE EMPTY_FILE_CHECKSUM_DATA;
     static {
         EMPTY_FILE_CHECKSUM_DATA = new ChecksumDataForFileTYPE();
         EMPTY_FILE_CHECKSUM_DATA.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
         ChecksumSpecTYPE checksumSpecTYPE = new ChecksumSpecTYPE();
         checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
         EMPTY_FILE_CHECKSUM_DATA.setChecksumSpec(checksumSpecTYPE);
         EMPTY_FILE_CHECKSUM_DATA.setChecksumValue(Base16Utils.encodeBase16(EMPTY_FILE_CHECKSUM));
     }
 
     @Override
     protected void initializeCUT() {
         super.initializeCUT();
         File fileDir = new File(settingsForCUT.getReferenceSettings().getPillarSettings().getCollectionDirs().get(0).getFileDirs().get(0));
         if(fileDir.exists()) {
             FileUtils.delete(fileDir);
         }
         System.out.println("Creating pillar with: " + settingsForCUT.getRepositorySettings());
         createReferencePillar();
     }
 
     @Override
     protected void shutdownCUT() {
         shutdownMediator();
     }
 
     protected void createReferencePillar() {
         shutdownMediator();
         csCache = new MemoryCacheMock();
         archives = new CollectionArchiveManager(settingsForCUT);
         alarmDispatcher = new AlarmDispatcher(settingsForCUT, messageBus);
         audits = new MockAuditManager();
         context = new MessageHandlerContext(
                 settingsForCUT,
                 new ResponseDispatcher(settingsForCUT, messageBus),
                 new PillarAlarmDispatcher(settingsForCUT, messageBus),
                 audits);
         csManager = new ReferenceChecksumManager(archives, csCache, alarmDispatcher,
                 ChecksumUtils.getDefault(context.getSettings()), 3600000L);
         mediator = new ReferencePillarMediator(messageBus, context, archives, csManager);
         mediator.start();
         initializeArchiveWithEmptyFile();
     }
 
     public void shutdownMediator() {
         if(mediator != null) {
             mediator.close();
             mediator = null;
         }
     }
 
     @Override
     protected String getComponentID() {
         return "ReferencePillar-" + testMethodName;
     }
 
     private void initializeArchiveWithEmptyFile() {
         addFixtureSetup("Initialize the Reference pillar cache with an empty file in default collection " +
                 collectionID);
         archives.downloadFileForValidation(DEFAULT_FILE_ID, collectionID, new ByteArrayInputStream(new byte[0]));
         archives.moveToArchive(DEFAULT_FILE_ID, collectionID);
         csCache.insertChecksumCalculation(DEFAULT_FILE_ID, collectionID, EMPTY_FILE_CHECKSUM, new Date());
     }
 }
