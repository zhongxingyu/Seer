 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.failover;
 
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 /**
  * User: nirb
  * Date: 13/03/13
  */
public class managementRecoveryWithMultipleServicesTest extends AbstractByonManagementPersistencyTest {
 
     @BeforeMethod(alwaysRun = true)
     public void bootstrapAndInit() throws Exception{
         super.prepareTest(true);
     }
 
     @AfterMethod(alwaysRun = true)
     public void afterTest() throws Exception{
         super.afterTest();
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testManagementPersistency() throws Exception {
         super.testManagementPersistency();
     }
 }
