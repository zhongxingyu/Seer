 package test.usm;
 
 import static framework.utils.AdminUtils.loadGSC;
 import static framework.utils.AdminUtils.loadGSM;
 import static framework.utils.LogUtils.log;
 
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.gigaspaces.cloudify.dsl.Service;
 import com.gigaspaces.cloudify.dsl.internal.CloudifyConstants;
 
 
 public class USMIncrementDecrementTest extends UsmAbstractTest {
 
 	private Machine machineA, machineB;
 
 	@Override
     @BeforeMethod
     public void beforeTest() {
 	    super.beforeTest();
 	    
         //1 GSM and 2 GSC at 2 machines
         log("waiting for 2 machines");
         admin.getMachines().waitFor(2);
 
         log("waiting for 2 GSAs");
         admin.getGridServiceAgents().waitFor(2);
 
         GridServiceAgent[] agents = admin.getGridServiceAgents().getAgents();
         GridServiceAgent gsaA = agents[0];
         GridServiceAgent gsaB = agents[1];
 
         machineA = gsaA.getMachine();
         machineB = gsaB.getMachine();
 
         //Start GSM A, GSC A, GSM B, GSC B
         log("starting: 1 GSM and 2 GSC at 2 machines");
         GridServiceManager gsmA = loadGSM(machineA); //GSM A
         GridServiceContainer gscA = loadGSC(machineA); //GSC A
         //loadGSM(machineB); //GSM B
         GridServiceContainer gscB = loadGSC(machineB); //GSC B
         processName = CloudifyConstants.DEFAULT_APPLICATION_NAME + "." + processName;
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "2")
     public void test() throws Exception {
 
         Service service = USMTestUtils.usmDeploy(processName, this.serviceFileName);
 
         ProcessingUnit pu = admin.getProcessingUnits().waitFor(processName);
         pu.waitFor(pu.getTotalNumberOfInstances());
         assertTrue("Service " + processName + " State is not RUNNING.", 
         		USMTestUtils.waitForPuRunningState(processName, 60, TimeUnit.SECONDS, admin));
         pu.startStatisticsMonitor();
 
         USMTestUtils.assertMonitors(pu);
         
         Long pid1 = USMTestUtils.getActualPID(pu.getInstances()[0]);
         pu.incrementInstance();
         pu.waitFor(2);
 
         USMTestUtils.assertPIDExists(pu, pid1);
 
         for (ProcessingUnitInstance puInstance : pu.getInstances()) {
         	USMTestUtils.assertMonitors(puInstance);
         }
 
         Assert.assertEquals(2, admin.getProcessingUnits().waitFor(processName, 20, TimeUnit.SECONDS).getInstances().length);
 
         long pidToDec = USMTestUtils.getActualPID(pu.getInstances()[0]);
         pu.getInstances()[0].decrement();
         pu.waitFor(1);
         
         USMTestUtils.assertPIDDoesntExist(pu, pidToDec);
 
         USMTestUtils.assertMonitors(pu);
 
        Assert.assertEquals(1, admin.getProcessingUnits().getProcessingUnit(processName).getInstances().length);
 
         pu.undeploy();
         Assert.assertNull(admin.getProcessingUnits().getProcessingUnit(processName));
 
     }
 
 }
