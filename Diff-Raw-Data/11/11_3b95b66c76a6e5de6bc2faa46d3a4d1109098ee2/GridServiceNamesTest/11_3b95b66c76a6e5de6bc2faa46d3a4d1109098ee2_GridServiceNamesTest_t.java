 package test.webui.recipes.services;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.testng.annotations.Test;
 
 import test.webui.AbstractWebUILocalCloudTest;
 
 import com.gigaspaces.webuitf.LoginPage;
 import com.gigaspaces.webuitf.MainNavigation;
 import com.gigaspaces.webuitf.dashboard.DashboardTab;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.Icon;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.InfrastructureServicesGrid;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.InfrastructureServicesGrid.ESMInst;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.InfrastructureServicesGrid.GSAInst;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.InfrastructureServicesGrid.GSCInst;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.InfrastructureServicesGrid.GSMInst;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.InfrastructureServicesGrid.LUSInst;
 import com.gigaspaces.webuitf.services.HostsAndServicesGrid;
 import com.gigaspaces.webuitf.services.ServicesTab;
 import com.gigaspaces.webuitf.topology.TopologyTab;
 import com.gigaspaces.webuitf.topology.applicationmap.ApplicationMap;
 import com.gigaspaces.webuitf.topology.logspanel.LogsMachine;
 import com.gigaspaces.webuitf.topology.logspanel.LogsPanel;
 import com.gigaspaces.webuitf.topology.logspanel.PuLogsPanelService;
 
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.LogUtils;
 
 public class GridServiceNamesTest extends AbstractWebUILocalCloudTest {
 		
 
 	private final String SERVICE_NAME = "rest";
 	
 	private final static String EXPECTED_GSA_SERVICES_NAME = "Agents";
 	private final static String EXPECTED_GSM_SERVICES_NAME = "Deployers";
 	private final static String EXPECTED_GSC_SERVICES_NAME = "USMs";
 	private final static String EXPECTED_ESM_SERVICES_NAME = "Orchestrators";
 	private final static String EXPECTED_LUS_SERVICES_NAME = "Discovery Services";	
 	
 	private final static String GSA_SERVICE_NAME = "agent";
 	private final static String GSM_SERVICE_NAME = "deployer";
 	private final static String GSC_SERVICE_NAME = "usm";	
 	private final static String ESM_SERVICE_NAME = "orchestrator";
 	private final static String LUS_SERVICE_NAME = "discovery service";
 	
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT)
 	public void nameTest() throws InterruptedException, IOException {
 
 		// get new login page
 		LoginPage loginPage = getLoginPage();
 
 		MainNavigation mainNav = loginPage.login();
 
 		DashboardTab dashboardTab = mainNav.switchToDashboard();
 		
 		final InfrastructureServicesGrid infrastructureServicesGrid = dashboardTab.getServicesGrid().getInfrastructureGrid();
 		
 		RepetitiveConditionProvider condition = new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				
 				ESMInst esmInst = infrastructureServicesGrid.getESMInst();
 				LogUtils.log( "> ESM count:" + esmInst.getCount() + ", status:" + 
 								infrastructureServicesGrid.getESMInst().getIcon() );
 				
 				return esmInst.getCount() == 1 && 
 							infrastructureServicesGrid.getESMInst().getIcon().equals( Icon.OK );
 			}
 		};
 		
 		AssertUtils.repetitiveAssertTrue("No esm in showing in the dashboard", condition, waitingTime);
 
 		
 		//check grid services name in Dashboard
 		
 		GSAInst gsaInst = infrastructureServicesGrid.getGSAInst();
 		GSCInst gscInst = infrastructureServicesGrid.getGSCInst();
 		GSMInst gsmInst = infrastructureServicesGrid.getGSMInst();
 		LUSInst lusInst = infrastructureServicesGrid.getLUSInst();
 		ESMInst esmInst = infrastructureServicesGrid.getESMInst();
 		
 		String gsaServiceName = gsaInst.getName();
 		String gscServiceName = gscInst.getName();
 		String gsmServiceName = gsmInst.getName();
 		String lusServiceName = lusInst.getName();
 		String esmServiceName = esmInst.getName();
 		
 		AssertUtils.assertEquals( "GSA Grid service name must be [" + EXPECTED_GSA_SERVICES_NAME + "]", 
 				EXPECTED_GSA_SERVICES_NAME, gsaServiceName );
 		AssertUtils.assertEquals( "GSM Grid service name must be [" + EXPECTED_GSM_SERVICES_NAME + "]", 
 				EXPECTED_GSM_SERVICES_NAME, gsmServiceName );
 		AssertUtils.assertEquals( "GSC Grid service name must be [" + EXPECTED_GSC_SERVICES_NAME + "]", 
 				EXPECTED_GSC_SERVICES_NAME, gscServiceName );
 		AssertUtils.assertEquals( "LUS Grid service name must be [" + EXPECTED_LUS_SERVICES_NAME + "]", 
 				EXPECTED_LUS_SERVICES_NAME, lusServiceName );
 		AssertUtils.assertEquals( "ESM Grid service name must be [" + EXPECTED_ESM_SERVICES_NAME + "]", 
 				EXPECTED_ESM_SERVICES_NAME, esmServiceName );
 		
 		//end of grid service name testing in Dashboard  
 		/////////////////////////////////////////
 		
 
 
 
 		//start checking grid service names in Topology/Logs 
 		TopologyTab topologyTab = mainNav.switchToTopology();
 
 		final ApplicationMap appMap = topologyTab.getApplicationMap();
 
 //		appMap.selectApplication(MANAGEMENT_APPLICATION_NAME);
 
 		ProcessingUnit processingUnit = admin.getProcessingUnits().getProcessingUnit(SERVICE_NAME);
 		ProcessingUnitInstance[] instances = processingUnit.getInstances();
 		ProcessingUnitInstance processingUnitInstance = instances[ 0 ];
 		Machine machine = processingUnitInstance.getMachine();
 		
 		appMap.selectApplication(MANAGEMENT_APPLICATION_NAME);
 		LogsPanel logsPanel = topologyTab.getTopologySubPanel().switchToLogsPanel();
 		PuLogsPanelService puLogsPanelService = logsPanel.getPuLogsPanelService( SERVICE_NAME );
 		LogsMachine machineTreeNode = puLogsPanelService.getMachine( machine, processingUnit );
 		List<String> services = machineTreeNode.getServices();
 		LogUtils.log( Arrays.toString( services.toArray( new String[ 0 ] ) ) );
 
 		//navigate to Services and check names of displayed grid services
 		
 		ServicesTab servicesTab = mainNav.switchToServices();
 
 		HostsAndServicesGrid hostAndServicesGrid = servicesTab.getHostAndServicesGrid();
 		String hostAddress = machine.getHostAddress();
		String hostName = machine.getHostName();
		hostAndServicesGrid.clickOnHost( hostAddress, hostName );
 
 		//gsa
 		int countNumberAgents = hostAndServicesGrid.countNumberOf( GSA_SERVICE_NAME );
 		//gsm
 		int countNumberDeployers = hostAndServicesGrid.countNumberOf( GSM_SERVICE_NAME );
 		//gsc
 		int countNumberUsm = hostAndServicesGrid.countNumberOf( GSC_SERVICE_NAME );
 		//lus
 		int countNumberDiscoveryServices = hostAndServicesGrid.countNumberOf( LUS_SERVICE_NAME );
 		//esm
 		int countNumberOrchestrators = hostAndServicesGrid.countNumberOf( ESM_SERVICE_NAME );		
 
 		final int expectedServicesNumber = 1;
		final int expectedUsmNumber = 3;
		
 		assertEquals( "Expected number of Agents(GSA) must be [" + expectedServicesNumber + "]", 
 				expectedServicesNumber, countNumberAgents );
 		assertEquals( "Expected number of Deployers(GSM) must be [" + expectedServicesNumber + "]", 
 				expectedServicesNumber, countNumberDeployers );
		assertEquals( "Expected number of USM(GSC) must be [" + expectedUsmNumber + "]", 
				expectedUsmNumber, countNumberUsm );
 		assertEquals( "Expected number of Discovery Service (LUS) must be [" + expectedServicesNumber + "]", 
 				expectedServicesNumber, countNumberDiscoveryServices );
 		assertEquals( "Expected number of Orchestrators(ESM) must be [" + expectedServicesNumber + "]", 
 				expectedServicesNumber, countNumberOrchestrators );
 	}
 }
