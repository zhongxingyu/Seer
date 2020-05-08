 package org.cloudifysource.quality.iTests.test.esm;
 
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import iTests.framework.utils.AssertUtils;
 import org.cloudifysource.quality.iTests.framework.utils.ByonMachinesUtils;
 import iTests.framework.utils.LogUtils;
 
 public class ByonStopMachineTest extends AbstractFromXenToByonGSMTest {
 	
 	@BeforeMethod
     public void beforeTest() {
 		super.beforeTestInit();
 	}
 	
 	@BeforeClass
 	protected void bootstrap() throws Exception {
 		super.bootstrapBeforeClass();
 	}
 	
 	@AfterMethod
     public void afterTest() {
 		super.afterTest();
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardownAfterClass() throws Exception {
 		super.teardownAfterClass();
 	}
 	
 	//This test is covered in other tests
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false)
 	public void stopMachineBasicTest() throws Exception {
 		LogUtils.log("Stop new machine test");
 		
 		AssertUtils.assertEquals(0, admin.getGridServiceContainers().getSize());	
 		admin.getGridServiceAgents().waitFor(1,OPERATION_TIMEOUT,TimeUnit.MILLISECONDS);
 		AssertUtils.assertEquals(1, admin.getGridServiceAgents().getSize());
 		
 		GridServiceAgent agent = ByonMachinesUtils.startNewByonMachine(getElasticMachineProvisioningCloudifyAdapter(),OPERATION_TIMEOUT,TimeUnit.MILLISECONDS);
 		AssertUtils.assertNotNull(agent);
 		repetitiveAssertNumberOfGSAsAdded(2, OPERATION_TIMEOUT);
 		
		stopByonMachine(getElasticMachineProvisioningCloudifyAdapter(), agent, OPERATION_TIMEOUT,TimeUnit.MILLISECONDS);
 		repetitiveAssertNumberOfGSAsRemoved(1, OPERATION_TIMEOUT);
 		AssertUtils.assertEquals(1, admin.getGridServiceAgents().getSize());
 		
         LogUtils.log("Stop machine test passed");
 	}
 }
