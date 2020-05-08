 /*
  * #%L
  * Bitrepository Audit Trail Service
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
 package org.bitrepository.audittrails.store;
 
 import java.math.BigInteger;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
 import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
 import org.bitrepository.bitrepositoryelements.FileAction;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.TestSettingsProvider;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.service.database.DerbyDatabaseDestroyer;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 public class AuditDatabaseTest extends ExtendedTestCase {
     /** The settings for the tests. Should be instantiated in the setup.*/
     Settings settings;
     String fileId = "TEST-FILE-ID-" + new Date().getTime();
     String fileId2 = "ANOTHER-FILE-ID" + new Date().getTime();
     String pillarId = "MY-TEST-PILLAR";
     String actor1 = "ACTOR-1";
     String actor2 = "ACTOR-2";
     String collectionId;
 
     @BeforeMethod (alwaysRun = true)
     public void setup() throws Exception {
         settings = TestSettingsProvider.reloadSettings("AuditDatabaseUnderTest");
         DerbyDatabaseDestroyer.deleteDatabase(
                 settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabase());
 
         AuditTrailDatabaseCreator auditTrailDatabaseCreator = new AuditTrailDatabaseCreator();
         auditTrailDatabaseCreator.createAuditTrailDatabase(settings, null);
         
         collectionId = settings.getCollections().get(0).getID();
     }
     
     @Test(groups = {"regressiontest", "databasetest"})
     public void AuditDatabaseExtractionTest() throws Exception {
         addDescription("Testing the connection to the audit trail service database especially with regards to "
                 + "extracting the data from it.");
         addStep("Setup the variables and constants.", "Should be ok.");
         Date restrictionDate = new Date(123456789); // Sometime between epoch and now!
         
         addStep("Adds the variables to the settings and instantaites the database cache", "Should be connected.");
         AuditTrailServiceDAO database = new AuditTrailServiceDAO(settings);
 
         addStep("Validate that the database is empty and then populate it.", "Should be possible.");
         Assert.assertEquals(database.largestSequenceNumber(pillarId, collectionId), 0);
         database.addAuditTrails(createEvents(), collectionId);
         Assert.assertEquals(database.largestSequenceNumber(pillarId, collectionId), 10);
         
         addStep("Extract the audit trails", "");
         List<AuditTrailEvent> res = database.getAuditTrails(null, null, null, null, null, null, null, null, null, null);
         Assert.assertEquals(res.size(), 2, res.toString());
         
         addStep("Test the extraction of FileID", "Should be able to extract the audit of each file individually.");
         res = database.getAuditTrails(fileId, null, null, null, null, null, null, null, null, null);
         Assert.assertEquals(res.size(), 1, res.toString());
         Assert.assertEquals(res.get(0).getFileID(), fileId);
         
         res = database.getAuditTrails(fileId2, null, null, null, null, null, null, null, null, null);
         Assert.assertEquals(res.size(), 1, res.toString());
         Assert.assertEquals(res.get(0).getFileID(), fileId2);
         
         addStep("Test the extraction of CollectionID", "Only results when the defined collection is used");
         res = database.getAuditTrails(null, collectionId, null, null, null, null, null, null, null, null);
         Assert.assertEquals(res.size(), 2, res.toString());
         
         res = database.getAuditTrails(null, "NOT-THE-CORRECT-COLLECTION-ID" + System.currentTimeMillis(), null, null, null, null, null, null, null, null);
         Assert.assertEquals(res.size(), 0, res.toString());
         
         addStep("Perform extraction based on the component id.", "");
         res = database.getAuditTrails(null, null, pillarId, null, null, null, null, null, null, null);
         Assert.assertEquals(res.size(), 2, res.toString());
         res = database.getAuditTrails(null, null, "NO COMPONENT", null, null, null, null, null, null, null);
         Assert.assertEquals(res.size(), 0, res.toString());
         
         addStep("Perform extraction based on the sequence number restriction", 
                 "Should be possible to have both lower and upper sequence number restrictions.");
         res = database.getAuditTrails(null, null, null, 5L, null, null, null, null, null, null);
         Assert.assertEquals(res.size(), 1, res.toString());
         Assert.assertEquals(res.get(0).getFileID(), fileId2);
         res = database.getAuditTrails(null, null, null, null, 5L, null, null, null, null, null);
         Assert.assertEquals(res.size(), 1, res.toString());
         Assert.assertEquals(res.get(0).getFileID(), fileId);
         
         addStep("Perform extraction based on actor id restriction.", 
                 "Should be possible to restrict on the id of the actor.");
         res = database.getAuditTrails(null, null, null, null, null, actor1, null, null, null, null);
         Assert.assertEquals(res.size(), 1, res.toString());
         Assert.assertEquals(res.get(0).getActorOnFile(), actor1);
         res = database.getAuditTrails(null, null, null, null, null, actor2, null, null, null, null);
         Assert.assertEquals(res.size(), 1, res.toString());
         Assert.assertEquals(res.get(0).getActorOnFile(), actor2);
         
         addStep("Perform extraction based on operation restriction.", 
                 "Should be possible to restrict on the FileAction operation.");
         res = database.getAuditTrails(null, null, null, null, null, null, FileAction.INCONSISTENCY, null, null, null);
         Assert.assertEquals(res.size(), 1, res.toString());
         Assert.assertEquals(res.get(0).getActionOnFile(), FileAction.INCONSISTENCY);
         res = database.getAuditTrails(null, null, null, null, null, null, FileAction.FAILURE, null, null, null);
         Assert.assertEquals(res.size(), 1, res.toString());
         Assert.assertEquals(res.get(0).getActionOnFile(), FileAction.FAILURE);
         
         addStep("Perform extraction based on date restriction.", 
                 "Should be possible to restrict on the date of the audit.");
         res = database.getAuditTrails(null, null, null, null, null, null, null, restrictionDate, null, null);
         Assert.assertEquals(res.size(), 1, res.toString());
         Assert.assertEquals(res.get(0).getFileID(), fileId2);
         res = database.getAuditTrails(null, null, null, null, null, null, null, null, restrictionDate, null);
         Assert.assertEquals(res.size(), 1, res.toString());
         Assert.assertEquals(res.get(0).getFileID(), fileId);
 
         addStep("Extract only the first auditTrail", "");
         res = database.getAuditTrails(null, null, null, null, null, null, null, null, null, 1);
         Assert.assertEquals(res.size(), 1, res.toString());
         Assert.assertEquals(res.get(0).getFileID(), fileId);
         
         database.close();
     }
 
     @Test(groups = {"regressiontest", "databasetest"})
     public void AuditDatabasePreservationTest() throws Exception {
         addDescription("Tests the functions related to the preservation of the database.");
         addStep("Adds the variables to the settings and instantaites the database cache", "Should be connected.");
         AuditTrailServiceDAO database = new AuditTrailServiceDAO(settings);
 
         Assert.assertEquals(database.largestSequenceNumber(pillarId, collectionId), 0);
         database.addAuditTrails(createEvents(), collectionId);
         Assert.assertEquals(database.largestSequenceNumber(pillarId, collectionId), 10);
 
         addStep("Validate the preservation sequence number", 
                 "Should be zero, since it has not been updated yet.");
         long pindex = database.getPreservationSequenceNumber(pillarId, collectionId);
         Assert.assertEquals(pindex, 0);
 
         addStep("Validate the insertion of the preservation sequence number",
                 "Should be the same value extracted afterwards.");
         long givenPreservationIndex = 123456789;
         database.setPreservationSequenceNumber(pillarId, collectionId, givenPreservationIndex);
         Assert.assertEquals(database.getPreservationSequenceNumber(pillarId, collectionId), givenPreservationIndex);
 
         database.close();
     }
 
     @Test(groups = {"regressiontest", "databasetest"})
     public void AuditDatabaseIngestTest() throws Exception {
         addDescription("Testing ingest of audittrails into the database");
         addStep("Adds the variables to the settings and instantaites the database cache", "Should be connected.");
         AuditTrailServiceDAO database = new AuditTrailServiceDAO(settings);
         AuditTrailEvents events;
         String veryLongString = "";
         for(int i = 0; i < 255; i++) {
             veryLongString += i;
         }
         
         addStep("Test ingesting with all data", "No failure");
         events = new AuditTrailEvents();
         events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                 "actor", "auditInfo", "fileId", "info", pillarId, BigInteger.ONE));
         database.addAuditTrails(events, collectionId);
         
         addStep("Test ingesting with no timestamp", "No failure");
         events = new AuditTrailEvents();
         events.getAuditTrailEvent().add(createSingleEvent(null, FileAction.CHECKSUM_CALCULATED, 
                 "actor", "auditInfo", "fileId", "info", pillarId, BigInteger.ONE));
         database.addAuditTrails(events, collectionId);
 
         addStep("Test ingesting with no file action", "No failure");
         events = new AuditTrailEvents();
         events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), null, 
                 "actor", "auditInfo", "fileId", "info", pillarId, BigInteger.ONE));
         database.addAuditTrails(events, collectionId);
 
         addStep("Test ingesting with no actor", "Throws exception");
         events = new AuditTrailEvents();
         events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                 null, "auditInfo", "fileId", "info", pillarId, BigInteger.ONE));
         try {
             database.addAuditTrails(events, collectionId);
             Assert.fail("Should throw an exception.");
         } catch (IllegalStateException e) {
             // expected
         }
 
         addStep("Test ingesting with no audit info", "No failure");
         events = new AuditTrailEvents();
         events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                 "actor", null, "fileId", "info", pillarId, BigInteger.ONE));
         database.addAuditTrails(events, collectionId);
 
         addStep("Test ingesting with no file id", "Throws exception");
         events = new AuditTrailEvents();
         events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                 "actor", "auditInfo", null, "info", pillarId, BigInteger.ONE));
         try {
             database.addAuditTrails(events, collectionId);
             Assert.fail("Should throw an exception.");
         } catch (IllegalStateException e) {
             // expected
         } 
 
         addStep("Test ingesting with no info", "No failure");
         events = new AuditTrailEvents();
         events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                 "actor", "auditInfo", "fileId", null, pillarId, BigInteger.ONE));
         database.addAuditTrails(events, collectionId);
 
         addStep("Test ingesting with no component id", "Throws exception");
         events = new AuditTrailEvents();
         events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                 "actor", "auditInfo", "fileId", "info", null, BigInteger.ONE));
         try {
             database.addAuditTrails(events, collectionId);
             Assert.fail("Should throw an exception.");
         } catch (IllegalStateException e) {
             // expected
         }
         
         addStep("Test ingesting with no sequence number", "Throws exception");
         events = new AuditTrailEvents();
         events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                 "actor", "auditInfo", "fileId", "info", pillarId, null));
         try {
             database.addAuditTrails(events, collectionId);
             Assert.fail("Should throw an exception.");
         } catch (IllegalStateException e) {
             // expected
         }
         
         addStep("Test ingest with very long auditInfo (255+)", "Not failing any more");
         events = new AuditTrailEvents();
         events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                 "actor", veryLongString, "fileId", "info", pillarId, BigInteger.ONE));
         database.addAuditTrails(events, collectionId);
         
         addStep("Test ingest with very long info (255+)", "Not failing any more");
         events = new AuditTrailEvents();
         events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                 "actor", "auditInfo", "fileId", veryLongString, pillarId, BigInteger.ONE));
         database.addAuditTrails(events, collectionId);
     }
 
     private AuditTrailEvents createEvents() {
         AuditTrailEvents events = new AuditTrailEvents();
         
         AuditTrailEvent event1 = createSingleEvent(CalendarUtils.getEpoch(), FileAction.INCONSISTENCY, actor1, 
                 "I AM AUDIT", fileId, null, pillarId, BigInteger.ONE);
         events.getAuditTrailEvent().add(event1);
         
         AuditTrailEvent event2 = createSingleEvent(CalendarUtils.getNow(), FileAction.FAILURE, actor2, null, fileId2, 
                 "WHAT AM I DOING?", pillarId, BigInteger.TEN);
         events.getAuditTrailEvent().add(event2);
 
         return events;
     }
     
     private AuditTrailEvent createSingleEvent(XMLGregorianCalendar datetime, FileAction action, String actor, 
             String auditInfo, String fileId, String info, String component, BigInteger seqNumber) {
         AuditTrailEvent res = new AuditTrailEvent();
         res.setActionDateTime(datetime);
         res.setActionOnFile(action);
         res.setActorOnFile(actor);
         res.setAuditTrailInformation(auditInfo);
         res.setFileID(fileId);
         res.setInfo(info);
         res.setReportingComponent(component);
         res.setSequenceNumber(seqNumber);        
         return res;
     }
 }
