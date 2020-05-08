 package org.bitrepository.audittrails.collector;
 
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.settings.TestSettingsProvider;
 import org.jaccept.structure.ExtendedTestCase;
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class AuditCollectorTester extends ExtendedTestCase {
     /** The settings for the tests. Should be instantiated in the setup.*/
     Settings settings;
     
     @BeforeClass (alwaysRun = true)
     public void setup() throws Exception {
         settings = TestSettingsProvider.reloadSettings();
     }
 
     @Test(groups = {"regressiontest"})
     public void AuditCollectorIntervalTest() throws Exception {
         addDescription("Test that the collector calls the AuditClient at the correct intervals.");
         addStep("Setup varables", "Should be OK.");
        settings.getReferenceSettings().getAuditTrailServiceSettings().setCollectInterval(950);
         
         MockAuditClient client = new MockAuditClient();
         MockAuditStore store = new MockAuditStore();
         AuditTrailCollector collector = new AuditTrailCollector(settings, client, store);
         
         synchronized(this) {
             this.wait(2100);
         }
         collector.close();
         
         Assert.assertEquals(client.getCallsToGetAuditTrails(), 2);
         Assert.assertEquals(store.getCallsToLargestSequenceNumber(), client.getCallsToGetAuditTrails() 
                 * settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors().size(),
                 "There should be one call for largest sequence number for each contributor for each call to the client.");
         Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);
         Assert.assertEquals(store.getCallsToGetAuditTrails(), 0);
     }
 }
