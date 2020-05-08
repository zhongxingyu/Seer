 package org.cloudifysource.quality.iTests.test.esm.stateless.manual.memory;
 
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
 
 import org.cloudifysource.quality.iTests.test.esm.AbstractFromXenToByonGSMTest;
 import org.cloudifysource.quality.iTests.framework.utils.DeploymentUtils;
 
 public class DedicatedStatelessManualByonDeployTest extends AbstractFromXenToByonGSMTest {
 	
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
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT)
 	public void test() {
	    File archive = DeploymentUtils.getArchive("servlet.war");
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
 	    
 	    pu.waitFor(2);
 	    
 	    //TODO check why expected number of GSA's added was 2 before
 	    repetitiveAssertNumberOfGSAsAdded(1, OPERATION_TIMEOUT);
 	    repetitiveAssertNumberOfGSAsRemoved(0, OPERATION_TIMEOUT);
 	    repetitiveAssertNumberOfGSCsAdded(2, OPERATION_TIMEOUT);
 	    
 	    
 	    assertUndeployAndWait(pu);
 	}
 }
