 package org.cloudifysource.quality.iTests.test.esm.stateless.manual.memory;
 
 import iTests.framework.utils.DeploymentUtils;
 import iTests.framework.utils.GsmTestUtils;
 
 import java.io.File;
 
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
 import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;
 import org.openspaces.core.util.MemoryUnit;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 public class DedicatedStatelessManualByonCleanupTest extends AbstractStatelessManualByonCleanupTest {
 
	private static final String ESM_LOG = "on-service-uninstalled-complete";
 
 	@BeforeMethod
     public void beforeTest() {
 		super.beforeTestInit();
 	}
 	
 	@BeforeClass
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 	}
 	
 	@AfterMethod
     public void afterTest() {
 		super.afterTest();
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardownAfterClass() throws Exception {
 		super.teardownAfterClass();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT)
 	public void test() {
 	    File archive = DeploymentUtils.getArchive("simpleStatelessPu.jar");
 	 // make sure no gscs yet created
 	    repetitiveAssertNumberOfGSCsAdded(0, OPERATION_TIMEOUT);
 	    repetitiveAssertNumberOfGSAsAdded(1, OPERATION_TIMEOUT);	    
 		final ProcessingUnit pu = super.deploy(
 				new ElasticStatelessProcessingUnitDeployment(archive)
 	            .memoryCapacityPerContainer(1, MemoryUnit.GIGABYTES)
 	            .dedicatedMachineProvisioning(getMachineProvisioningConfig())
 	            .scale(new ManualCapacityScaleConfigurer()
 	            	  .memoryCapacity(2, MemoryUnit.GIGABYTES)
                       .create())
 	    );
 	    
 		GsmTestUtils.waitForScaleToCompleteIgnoreCpuSla(pu, 2, OPERATION_TIMEOUT);
 		
 	    assertUndeployAndWait(pu);
        
         repetitiveAssertOnServiceUninstalledInvoked(ESM_LOG);
     }
 
 }
