 package org.cloudifysource.quality.iTests.test.esm.stateless.manual.memory;
 
 import java.io.File;
 
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
 import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;
 import org.openspaces.admin.vm.VirtualMachineDetails;
 import org.openspaces.core.util.MemoryUnit;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import org.cloudifysource.quality.iTests.test.esm.AbstractFromXenToByonGSMTest;
 import org.cloudifysource.quality.iTests.framework.utils.DeploymentUtils;
 
 /**
  * Allocate more memory than java heap size used by container.
  * This feature is used by PU instances that spawn a new process or use non-java-heap in process.
  * 
  * @author itaif
  */
 public class DedicatedStatelessHeapMemoryManualByonDeployTest extends
 		AbstractFromXenToByonGSMTest {
 	
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
 	
 	private static final int XMS = 128;
 	private static final int XMX = 256;
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT)
 	public void test() {
		File archive = DeploymentUtils.getArchive("simpleStatelessPu.jar");
 		// make sure no gscs yet created
 	    repetitiveAssertNumberOfGSCsAdded(0, OPERATION_TIMEOUT);
 	    repetitiveAssertNumberOfGSAsAdded(1, OPERATION_TIMEOUT);	    
 		final ProcessingUnit pu = super
 				.deploy(new ElasticStatelessProcessingUnitDeployment(archive)
 						.memoryCapacityPerContainer(1, MemoryUnit.GIGABYTES)
 						.addCommandLineArgument("-Xmx" + XMX +"m")
 						.addCommandLineArgument("-Xms" + XMS+"m")
 						.dedicatedMachineProvisioning(
 								getMachineProvisioningConfig())
 						.scale(new ManualCapacityScaleConfigurer()
 								.memoryCapacity(2, MemoryUnit.GIGABYTES)
 								.create()));
 
 		pu.waitFor(2);
 
 		// According to the XMX we should start 8 containers to reach 2GB
 		// According to memoryCapacityPerContainer we should start only 2 containers to reach 2GB
 		repetitiveAssertNumberOfGSCsAdded(2, OPERATION_TIMEOUT);
 		repetitiveAssertNumberOfGSCsRemoved(0, OPERATION_TIMEOUT);
 		repetitiveAssertNumberOfGSCsAdded(2, OPERATION_TIMEOUT);
 		repetitiveAssertNumberOfGSCsRemoved(0, OPERATION_TIMEOUT);
 
 		VirtualMachineDetails jvmDetails = admin.getGridServiceContainers()
 				.getContainers()[0].getVirtualMachine().getDetails();
 		long xmx = MemoryUnit.MEGABYTES.convert(
 				jvmDetails.getMemoryHeapMaxInBytes(), MemoryUnit.BYTES);
 
 		long xms = MemoryUnit.MEGABYTES.convert(
 				jvmDetails.getMemoryHeapInitInBytes(), MemoryUnit.BYTES);
 		assertEquals("Xmx", 1 , (int)Math.round((1.0*XMX)/xmx));
 		assertEquals("Xms", 1 , (int)Math.round((1.0*XMS)/xms));
 		
 		assertUndeployAndWait(pu);
 	}
 }
