 package org.cloudifysource.quality.iTests.test.esm.stateless.manual.memory;
 
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.AssertUtils.RepetitiveConditionProvider;
 import iTests.framework.utils.DeploymentUtils;
 import iTests.framework.utils.GsmTestUtils;
 import iTests.framework.utils.LogUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.esc.driver.provisioning.CloudifyMachineProvisioningConfig;
 import org.cloudifysource.esc.driver.provisioning.byon.ByonProvisioningDriver;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.CloudTestUtils;
 import org.cloudifysource.quality.iTests.test.esm.AbstractFromXenToByonGSMTest;
 import org.openspaces.admin.esm.ElasticServiceManager;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
 import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;
 import org.openspaces.core.util.MemoryUnit;
 import org.openspaces.grid.gsm.machines.plugins.exceptions.ElasticMachineProvisioningException;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.gigaspaces.log.LogEntries;
 import com.gigaspaces.log.LogEntryMatcher;
 import com.gigaspaces.log.LogEntryMatchers;
 
 public class DedicatedStatelessManualFailoverAwareTest extends AbstractFromXenToByonGSMTest {
 
     private static final String UNEXPECTED_ESM_LOG_STATEMENT = ElasticMachineProvisioningException.class.getName();
    private static final String EXPECTED_ESM_LOG_STATEMENT ="failover-aware-provisioning-driver";
 
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
 
     /**
      * CLOUDIFY-2180
      * Tests that after failover the cloud driver #start() method receives MachineInfo of the failed machine.
      */
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled=true)
     public void testFailedMachineDetails() throws Exception {
     	repetitiveAssertNumberOfGSAsRemoved(0, OPERATION_TIMEOUT);
     	
         final ProcessingUnit pu = deployProcessingUnitOnSeperateMachine();
         GsmTestUtils.waitForScaleToCompleteIgnoreCpuSla(pu, 1, OPERATION_TIMEOUT);
         repetitiveAssertNumberOfGSAsRemoved(0, OPERATION_TIMEOUT);
     	        
         // stop machine and check ESM log that it starts a machine that is aware of the failed machine.
        final GridServiceAgent agent = getAgent(pu);
        LogUtils.log("Stopping agent " + agent.getUid());
        stopByonMachine(getElasticMachineProvisioningCloudifyAdapter(), agent, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         repetitiveAssertNumberOfGSAsRemoved(1, OPERATION_TIMEOUT);
        
        // check CLOUDIFY-2180
         repetitiveAssertFailoverAware();
         GsmTestUtils.waitForScaleToCompleteIgnoreCpuSla(pu, 1, OPERATION_TIMEOUT);
        
        // check ESM not tried to start too many machines, which would eventually result in machine start failure
         repetitiveAssertNoStartMachineFailures();
        assertUndeployAndWait(pu);
     }
 
 	private ProcessingUnit deployProcessingUnitOnSeperateMachine() {
 		final File archive = DeploymentUtils.getArchive("simpleStatelessPu.jar");
         final ElasticStatelessProcessingUnitDeployment deployment =
                 new ElasticStatelessProcessingUnitDeployment(archive)
                 .memoryCapacityPerContainer(1, MemoryUnit.GIGABYTES)
         		.scale(
 	                new ManualCapacityScaleConfigurer()
 	                .memoryCapacity(1, MemoryUnit.GIGABYTES)
 	                .create());
         
         final CloudifyMachineProvisioningConfig provisioningConfig = getMachineProvisioningConfig();
         provisioningConfig.setDedicatedManagementMachines(true); // do not deploy instance on management machine, since it is going down.
 		deployment.dedicatedMachineProvisioning(provisioningConfig);
 
         return super.deploy(deployment);
 	}
 
     private GridServiceAgent getAgent(ProcessingUnit pu) {
     	return pu.getInstances()[0].getGridServiceContainer().getGridServiceAgent();
     }
     
     @Override
     public void beforeBootstrap() throws IOException {
     	CloudTestUtils.replaceCloudDriverImplementation(
     			getService(),
     			ByonProvisioningDriver.class.getName(), //old class
     			"org.cloudifysource.quality.iTests.FailoverAwareByonProvisioningDriver", //new class
     			"location-aware-provisioning-byon", "2.1-SNAPSHOT"); //jar
     }
     
     private void repetitiveAssertFailoverAware() {
     	
     	final ElasticServiceManager esm = admin.getElasticServiceManagers().waitForAtLeastOne(OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         LogUtils.log("Waiting for 1 "+ EXPECTED_ESM_LOG_STATEMENT  + " log entry before undeploying PU");
         final LogEntryMatcher logMatcher = LogEntryMatchers.containsString(EXPECTED_ESM_LOG_STATEMENT);
         repetitiveAssertTrue("Expected " + EXPECTED_ESM_LOG_STATEMENT +" log", new RepetitiveConditionProvider() {
             
             @Override
             public boolean getCondition() {
                 final LogEntries logEntries = esm.logEntries(logMatcher);
                 final int count = logEntries.logEntries().size();
                 LogUtils.log("Exepcted at least one "+ EXPECTED_ESM_LOG_STATEMENT + " log entries. Actual :" + count);
                 return count > 0;
             }
         } , OPERATION_TIMEOUT);
     }
     
     private void repetitiveAssertNoStartMachineFailures() {
     	
     	final ElasticServiceManager esm = admin.getElasticServiceManagers().waitForAtLeastOne(OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         LogUtils.log("Checking there is no "+ UNEXPECTED_ESM_LOG_STATEMENT  + " log entry before undeploying PU");
        final LogEntryMatcher logMatcher = LogEntryMatchers.containsString(UNEXPECTED_ESM_LOG_STATEMENT);
         AssertUtils.repetitiveAssertConditionHolds("Unexpected " + UNEXPECTED_ESM_LOG_STATEMENT +" log", new RepetitiveConditionProvider() {
             
             @Override
             public boolean getCondition() {
                 final LogEntries logEntries = esm.logEntries(logMatcher);
                 final int count = logEntries.logEntries().size();
                 LogUtils.log("Exepcted no "+ UNEXPECTED_ESM_LOG_STATEMENT + " log entries. Actual :" + count);
                 return count == 0;
             }
        } , TimeUnit.SECONDS.toMillis(30));
     }
 }
