 package org.cloudifysource.quality.iTests.test.webui.recipes.services;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.AssertUtils.RepetitiveConditionProvider;
 import iTests.framework.utils.LogUtils;
 import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
 import org.cloudifysource.quality.iTests.test.webui.AbstractWebUILocalCloudTest;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.testng.annotations.Test;
 
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
 
 
 	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT)
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
 
 		topologyTab.selectApplication(MANAGEMENT_APPLICATION_NAME);
 		LogsPanel logsPanel = topologyTab.getTopologySubPanel().switchToLogsPanel();
 		PuLogsPanelService puLogsPanelService = logsPanel.getPuLogsPanelService( SERVICE_NAME );
 		LogsMachine machineTreeNode = puLogsPanelService.getMachine( machine, processingUnit );
 		List<String> services = machineTreeNode.getServices();
 		LogUtils.log( Arrays.toString( services.toArray( new String[ 0 ] ) ) );
 
 		//navigate to Services and check names of displayed grid services
 
 		ServicesTab servicesTab = mainNav.switchToServices();
 
 		
 		final HostsAndServicesGrid hostAndServicesGrid = servicesTab.getHostAndServicesGrid();
 		
 		final AtomicReference<HostsAndServicesGrid> ref = new AtomicReference<HostsAndServicesGrid>(hostAndServicesGrid);
 
 		String hostAddress = machine.getHostAddress();
 		String hostName = machine.getHostName();
 		hostAndServicesGrid.clickOnHost( hostAddress, hostName );
 
 		//gsa
 		Integer countNumberAgents = 0;
 		final AtomicReference<Integer> countNumberAgentsRef = new AtomicReference<Integer>(countNumberAgents);
 		//gsm
 		Integer countNumberDeployers = 0;
 		final AtomicReference<Integer> countNumberDeployersRef = new AtomicReference<Integer>(countNumberDeployers);
 		//gsc
 		Integer countNumberUsm = 0;
 		final AtomicReference<Integer> countNumberUsmRef = new AtomicReference<Integer>(countNumberUsm);
 		//lus
 		Integer countNumberDiscoveryServices = 0;
 		final AtomicReference<Integer> countNumberDiscoveryServicesRef = new AtomicReference<Integer>(countNumberDiscoveryServices);
 		//esm
 		Integer countNumberOrchestrators = 0;
 		final AtomicReference<Integer> countNumberOrchestratorsRef = new AtomicReference<Integer>(countNumberOrchestrators);
 
 		final int expectedServicesNumber = 1;
 		final int expectedUsmNumber = 1;
 
 		condition = new RepetitiveConditionProvider() {
 			@Override
 			public boolean getCondition() {
 				countNumberAgentsRef.set(hostAndServicesGrid.countNumberOf( GSA_SERVICE_NAME ));
 				return (countNumberAgentsRef.get() == expectedServicesNumber);
 			}
 		};
 		AssertUtils.repetitiveAssertTrue("Expected number of Agents(GSA) must be [" + expectedServicesNumber + "]. Actual [" + countNumberAgentsRef.get() + "]", condition, waitingTime);
 		
 		condition = new RepetitiveConditionProvider() {
 			@Override
 			public boolean getCondition() {
 				countNumberDeployersRef.set(hostAndServicesGrid.countNumberOf( GSM_SERVICE_NAME ));
 				return (countNumberDeployersRef.get() == expectedServicesNumber);
 			}
 		};
 		AssertUtils.repetitiveAssertTrue("Expected number of Deployers(GSM) must be [" + expectedServicesNumber + "]. Actual [" + countNumberDeployersRef.get() + "]", condition, waitingTime);
 		
 		condition = new RepetitiveConditionProvider() {
 			@Override
 			public boolean getCondition() {
 				countNumberUsmRef.set(hostAndServicesGrid.countNumberOf( GSC_SERVICE_NAME ));
 				return (countNumberUsmRef.get() == expectedServicesNumber);
 			}
 		};
 		AssertUtils.repetitiveAssertTrue("Expected number of USM(GSC) must be [" + expectedUsmNumber + "]. Actual [" + countNumberUsmRef.get() + "]", condition, waitingTime);
 		
 		condition = new RepetitiveConditionProvider() {
 			@Override
 			public boolean getCondition() {
 				countNumberDiscoveryServicesRef.set(hostAndServicesGrid.countNumberOf( LUS_SERVICE_NAME ));
 				return (countNumberDiscoveryServicesRef.get() == expectedServicesNumber);
 			}
 		};
 		AssertUtils.repetitiveAssertTrue("Expected number of Discovery Service (LUS) must be [" + expectedServicesNumber + "]. Actual [" + countNumberDiscoveryServicesRef.get() + "]", condition, waitingTime);
 		
 		condition = new RepetitiveConditionProvider() {
 			@Override
 			public boolean getCondition() {
 				countNumberOrchestratorsRef.set(hostAndServicesGrid.countNumberOf( ESM_SERVICE_NAME ));
 				return (countNumberOrchestratorsRef.get() == expectedServicesNumber);
 			}
 		};
 		AssertUtils.repetitiveAssertTrue("Expected number of Orchestrators(ESM) must be [" + expectedServicesNumber + "]. Actual [" + countNumberOrchestratorsRef.get() + "]", condition, waitingTime);
 
 	}
 }
