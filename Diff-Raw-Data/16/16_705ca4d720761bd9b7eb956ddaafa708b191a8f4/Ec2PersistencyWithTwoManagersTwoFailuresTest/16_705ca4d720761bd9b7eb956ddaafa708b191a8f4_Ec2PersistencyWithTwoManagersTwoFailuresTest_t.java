 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2.persistence;
 
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.AbstractCloudManagementPersistencyTest;
 import org.testng.annotations.*;
 
 /**
  * @author Itai Frenkel
  * Date: 25/07/13
  */
 public class Ec2PersistencyWithTwoManagersTwoFailuresTest extends AbstractCloudManagementPersistencyTest {
 
     @BeforeClass(alwaysRun = true)
     public void bootstrapAndInit() throws Exception{
         super.bootstrap();
         super.initManagementUrlsAndRestClient();
     }
 
    /** 
     * Do not call super.teardown(), since it will detect leaked management machine (that this test caused).
     * Instead just terminate all management machines.
     */
    @AfterClass(alwaysRun = true)
    public void teardown() throws Exception{
    	terminateManagementNodes();
     }
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
     public void testManagementPersistencyTwoFailures() throws Exception {
         super.testManagementPersistencyTwoFailures();
     }
 
     @Override
     protected String getCloudName() {
         return "ec2";
     }
 
     @Override
     protected boolean isReusableCloud() {
         return false;
     }
 }
