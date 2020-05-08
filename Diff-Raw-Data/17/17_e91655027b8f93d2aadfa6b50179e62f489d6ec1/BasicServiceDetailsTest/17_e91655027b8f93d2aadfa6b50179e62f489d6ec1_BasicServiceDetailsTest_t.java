 package test.webui.topology.detailspanel;
 
 import java.util.Map;
 
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.space.SpaceDeployment;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.webui.AbstractSeleniumTest;
 import test.webui.objects.LoginPage;
 import test.webui.objects.topology.ApplicationMap;
 import test.webui.objects.topology.DetailsPanel;
import test.webui.objects.topology.LogicalPanel;
import test.webui.objects.topology.LogicalPanel.WebUIProcessingUnitInstance;
 import test.webui.objects.topology.TopologyTab;
 import test.webui.objects.topology.WebUIServiceDetails;
 import framework.utils.AdminUtils;
 import framework.utils.LogUtils;
 import framework.utils.ProcessingUnitUtils;
 
 public class BasicServiceDetailsTest extends AbstractSeleniumTest {
 	
 	Machine machineA;
 	ProcessingUnit Pu;
 	
 	@BeforeMethod(alwaysRun = true)
 	public void startSetUp() {	
 		LogUtils.log("waiting for 1 machine");
 		admin.getMachines().waitFor(1);
 
 		LogUtils.log("waiting for 1 GSA");
 		admin.getGridServiceAgents().waitFor(1);
 
 		GridServiceAgent[] agents = admin.getGridServiceAgents().getAgents();
 		GridServiceAgent gsaA = agents[0];
 
 		machineA = gsaA.getMachine();
 
 		LogUtils.log("starting: 1 GSM and 2 GSC's on 1 machine");
 		GridServiceManager gsmA = AdminUtils.loadGSM(machineA); 
 		AdminUtils.loadGSCs(machineA, 2);
 		
 		LogUtils.log("deploying processing unit...");
 		SpaceDeployment deployment = new SpaceDeployment("Test").partitioned(1, 1).maxInstancesPerVM(1).setContextProperty("com.gs.application", "MyApp");
 		Pu = gsmA.deploy(deployment);
 		ProcessingUnitUtils.waitForDeploymentStatus(Pu, DeploymentStatus.INTACT);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT)
 	public void basicDetailsTest() throws InterruptedException {
 		
 		// get new login page
 		LoginPage loginPage = getLoginPage();
 		
 		// get new topology tab
 		TopologyTab topologyTab = loginPage.login().switchToTopology();
 		
 		ApplicationMap applicationMap = topologyTab.getApplicationMap();
 		
 		applicationMap.selectApplication("MyApp");
 		
		LogicalPanel logicalPanel = topologyTab.getTopologySubPanel().swtichToLogicalPanel();
		
		WebUIProcessingUnitInstance inst1 = logicalPanel.getProcessingUnitInstance(Pu.getInstances()[0].getProcessingUnitInstanceName());
		assertTrue(inst1.select());
		
 		DetailsPanel detailsPanel = topologyTab.getDetailsPanel();
 		WebUIServiceDetails serviceDetails = detailsPanel.switchToServiceDetails();
 		Map<String, Map<String, String>> spaceDetails = serviceDetails.getDetails();
 		assertTrue(spaceDetails.size() != 0);	
		
		WebUIProcessingUnitInstance inst2 = logicalPanel.getProcessingUnitInstance(Pu.getInstances()[1].getProcessingUnitInstanceName());
		assertTrue(inst2.select());
		
		detailsPanel = topologyTab.getDetailsPanel();
		serviceDetails = detailsPanel.switchToServiceDetails();
		spaceDetails = serviceDetails.getDetails();
		assertTrue(spaceDetails.size() != 0);
 	}
 
 }
