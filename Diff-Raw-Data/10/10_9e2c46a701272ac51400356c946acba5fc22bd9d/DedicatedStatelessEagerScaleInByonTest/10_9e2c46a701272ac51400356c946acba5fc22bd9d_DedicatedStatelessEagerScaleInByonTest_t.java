 package org.cloudifysource.quality.iTests.test.esm.stateless.eager;
 
 import java.io.File;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
 import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfigurer;
 import org.openspaces.admin.pu.elastic.config.EagerScaleConfigurer;
 import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
 import org.openspaces.core.util.MemoryUnit;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import org.cloudifysource.quality.iTests.test.esm.AbstractFromXenToByonGSMTest;
 import iTests.framework.utils.AssertUtils.RepetitiveConditionProvider;
 import iTests.framework.utils.DeploymentUtils;
 import iTests.framework.utils.LogUtils;
 
 public class DedicatedStatelessEagerScaleInByonTest extends AbstractFromXenToByonGSMTest {
 	
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
 	
 	
     @Test(timeOut=DEFAULT_TEST_TIMEOUT, groups="1")
     public void doTest() throws Exception {
     	
     	repetitiveAssertNumberOfGSCsAdded(0, OPERATION_TIMEOUT);
         repetitiveAssertNumberOfGSAsAdded(1, OPERATION_TIMEOUT);
         
         startNewByonMachine(getElasticMachineProvisioningCloudifyAdapter(), OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         GridServiceAgent gsa3 = startNewByonMachine(getElasticMachineProvisioningCloudifyAdapter(), OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         GridServiceAgent gsa4 = startNewByonMachine(getElasticMachineProvisioningCloudifyAdapter(), OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 
         File archive = DeploymentUtils.getArchive("simpleStatelessPu.jar");
         
         final ProcessingUnit pu = super.deploy(
                 new ElasticStatelessProcessingUnitDeployment(archive)
                 .memoryCapacityPerContainer(1, MemoryUnit.GIGABYTES)
  
                 .dedicatedMachineProvisioning(
 						new DiscoveredMachineProvisioningConfigurer()
 						.reservedMemoryCapacityPerMachine(128, MemoryUnit.MEGABYTES)
 						.create())
 				.scale(new EagerScaleConfigurer().atMostOneContainerPerMachine().create())
         );
         
         final AtomicInteger removed = new AtomicInteger(0);
         final AtomicInteger added = new AtomicInteger(0);
         pu.addLifecycleListener(new ProcessingUnitInstanceLifecycleEventListener() {
 			
 			@Override
 			public void processingUnitInstanceRemoved(
 					ProcessingUnitInstance processingUnitInstance) {
 				removed.incrementAndGet();
 				
 			}
 			
 			@Override
 			public void processingUnitInstanceAdded(
 					ProcessingUnitInstance processingUnitInstance) {
 				added.incrementAndGet();
 			}
 		});
         
        assertNumberOfInstances(4,pu);
         assertEquals(0,removed.get());
         repetitiveAssertTrue("Expecting 4 pu instances added. actual " + added.get(), new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				return added.get() == 4;
 			}
 		}, OPERATION_TIMEOUT);
         
         stopByonMachine(getElasticMachineProvisioningCloudifyAdapter(), gsa3, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         assertNumberOfInstances(3,pu);
         assertEquals(1,removed.get());
         assertEquals(4,added.get());
         
         stopByonMachine(getElasticMachineProvisioningCloudifyAdapter(), gsa4, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         assertNumberOfInstances(2,pu);
         assertEquals(2,removed.get());
         assertEquals(4,added.get());
         
         assertUndeployAndWait(pu);
 
     }
 
     private void assertNumberOfInstances(final int expectedNumberOfInstances, final ProcessingUnit pu) {
         super.repetitiveAssertTrue(
                 "number of instances", 
                 new RepetitiveConditionProvider() {
                     
                     public boolean getCondition() {
                     	boolean condition = true;
                     	if (pu.getNumberOfInstances() != expectedNumberOfInstances) {
                     		LogUtils.log("Expected numberOfInstances " + expectedNumberOfInstances + " instead numberOfInstances is " + pu.getNumberOfInstances());
                     		condition = false;
                     	}
                     	
                     	if (pu.getInstances().length  != expectedNumberOfInstances) {
                     		LogUtils.log("Expected actual numberOfInstances " + expectedNumberOfInstances + " instead pu.getInstances().length is " + pu.getInstances().length);
                     		condition = false;
                     	}
                     	
                     	return condition;
                         
                     }
                 },
                 OPERATION_TIMEOUT);
     }
 }
