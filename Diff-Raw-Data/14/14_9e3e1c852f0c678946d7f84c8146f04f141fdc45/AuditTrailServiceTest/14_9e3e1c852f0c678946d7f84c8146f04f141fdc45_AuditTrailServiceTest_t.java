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
 package org.bitrepository.audittrails;
 
 import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
 import org.bitrepository.audittrails.collector.AuditTrailCollector;
 import org.bitrepository.bitrepositoryelements.FileAction;
 import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
 import org.bitrepository.client.eventhandler.CompleteEvent;
 import org.bitrepository.client.eventhandler.EventHandler;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.TestSettingsProvider;
 import org.bitrepository.service.contributor.ContributorMediator;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class AuditTrailServiceTest extends ExtendedTestCase {
     /** The settings for the tests. Should be instantiated in the setup.*/
     Settings settings;
     
     public static final String TEST_COLLECTION = "dummy-collection";
  
     
     @BeforeClass (alwaysRun = true)
     public void setup() throws Exception {
         settings = TestSettingsProvider.reloadSettings("AuditTrailServiceUnderTest");
     }
     
     @Test(groups = {"unstable"})
     public void auditTrailServiceTest() throws Exception {
         addDescription("Test the Audit Trail Service");
         settings.getRepositorySettings().getGetAuditTrailSettings().getContributorIDs().clear();
         settings.getRepositorySettings().getGetAuditTrailSettings().getContributorIDs().add("Contributor1");
         
         MockAuditStore store = new MockAuditStore();
         MockAuditClient client = new MockAuditClient();
         MockAuditPreserver preserver = new MockAuditPreserver();
         ContributorMediator mediator = new MockContributorMediator();
        AuditTrailCollector collector = new AuditTrailCollector(TEST_COLLECTION, settings, client, store);
         
         addStep("Instantiate the service.", "Should work.");
         AuditTrailService service = new AuditTrailService(store, collector, mediator, preserver, settings);
        Assert.assertEquals(client.getCallsToGetAuditTrails(), 0);
         service.start();
         
         addStep("Try to collect audit trails.", "Should make a call to the client.");
         CollectionRunner collectionRunner = new CollectionRunner(service);
         Thread t = new Thread(collectionRunner);
         t.start();
         Thread.sleep(100);
         EventHandler eventHandler = client.getLatestEventHandler();
         eventHandler.handleEvent(new AuditTrailResult("Contributor1", TEST_COLLECTION, new ResultingAuditTrails(), false));
         eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
         Assert.assertEquals(client.getCallsToGetAuditTrails(), 1);
         
         addStep("Retrieve audit trails with and without an action", "Should work.");
         Assert.assertEquals(store.getCallsToAddAuditTrails(), 1);
         Assert.assertEquals(store.getCallsToGetAuditTrails(), 0);        
         service.queryAuditTrailEvents(null, null, null, null, null, null, null, null);
         Assert.assertEquals(store.getCallsToGetAuditTrails(), 1);        
         service.queryAuditTrailEvents(null, null, null, null, null, null, FileAction.FAILURE, null);
         Assert.assertEquals(store.getCallsToGetAuditTrails(), 2);        
         
         addStep("Shutdown", "");
         service.shutdown();
     }
 
     private class MockContributorMediator implements ContributorMediator {
         @Override
         public void start() { }
         @Override
         public void close() { }
     }
 
     public class CollectionRunner implements Runnable {
         private final AuditTrailService service;
         boolean finished = false;
 
         public CollectionRunner(AuditTrailService service) {
             this.service = service;
         }
 
         public void run() {
             service.collectAuditTrails();
             finished = true;
         }
     }
 }
