 package org.cloudifysource.quality.iTests.test.esm.stateless.manual.memory;
 
 import java.io.File;
 
 import org.cloudifysource.quality.iTests.framework.utils.DeploymentUtils;
 import org.cloudifysource.quality.iTests.framework.utils.GsmTestUtils;
 import org.cloudifysource.quality.iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.esm.AbstractFromXenToByonGSMTest;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
 import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;
 import org.openspaces.core.util.MemoryUnit;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 
 public class DedicatedManualDataGridUndeployDuringDeployTest extends AbstractFromXenToByonGSMTest {
 	
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
 	
 	private static final int REDEPLOY_ITERATIONS = 3;
 
 	/**
 	 * @see GS-10644
 	 */
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT*2, groups = "1", enabled=false)
     public void testManualDataGridDeploymentScale() {
         
		final File archive = DeploymentUtils.getArchive("servlet.war");
 		// make sure no gscs yet created
 	    repetitiveAssertNumberOfGSCsAdded(0, OPERATION_TIMEOUT);
 	    repetitiveAssertNumberOfGSAsAdded(1, OPERATION_TIMEOUT);	    
 	    
         ProcessingUnit pu = null;
         for (int i = 0 ; i < REDEPLOY_ITERATIONS; i++) {
         	LogUtils.log("Deploying iteration "+ i);
         	final ElasticStatelessProcessingUnitDeployment deployment =
         			new ElasticStatelessProcessingUnitDeployment(archive)
         	.memoryCapacityPerContainer(1, MemoryUnit.GIGABYTES)
         	.dedicatedMachineProvisioning(getMachineProvisioningConfigWithMachineZone(new String[] {"iter"+i}))
         	.scale(new ManualCapacityScaleConfigurer()
         	.memoryCapacity(2, MemoryUnit.GIGABYTES)
         	.create());
 	        pu = super.deploy(deployment);
 	
 	        GsmTestUtils.waitForScaleToComplete(pu, 2, OPERATION_TIMEOUT);
 	        LogUtils.log("Undeploying iteration "+ i);
 	        pu.undeploy();
         }
        // TODO: check why undeployAndWait got stuck if the pu is already undeployed  
        // LogUtils.log("UndeployAndWait pu: "+pu.getName());
        // GsmTestUtils.assertUndeployAndWait(pu, OPERATION_TIMEOUT*2, TimeUnit.MILLISECONDS);   
       
 	}
 
 }
