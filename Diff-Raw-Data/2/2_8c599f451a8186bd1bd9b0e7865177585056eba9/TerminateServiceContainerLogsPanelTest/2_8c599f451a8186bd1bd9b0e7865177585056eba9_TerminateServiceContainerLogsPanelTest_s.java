 package test.webui.recipes.applications;
 
 import java.io.IOException;
 
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.ProcessingUnitUtils;
 
 import test.webui.objects.LoginPage;
 import test.webui.objects.MainNavigation;
 import test.webui.objects.services.HostsAndServicesGrid;
 import test.webui.objects.services.ServicesTab;
 import test.webui.objects.topology.TopologyTab;
 import test.webui.objects.topology.applicationmap.ApplicationMap;
 import test.webui.objects.topology.applicationmap.ApplicationNode;
 import test.webui.objects.topology.logspanel.LogsMachine;
 import test.webui.objects.topology.logspanel.LogsPanel;
 import test.webui.objects.topology.logspanel.PuLogsPanelService;
 import test.webui.resources.WebConstants;
 
 public class TerminateServiceContainerLogsPanelTest extends AbstractSeleniumApplicationRecipeTest {
 	
 	@Override
 	@BeforeMethod
 	public void install() throws IOException, InterruptedException {
 		setBrowser(WebConstants.CHROME);
 		setCurrentApplication("travel");
 		super.install();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2)
 	public void terminateContainerTest() throws InterruptedException {
 		
 		// get new login page
 		LoginPage loginPage = getLoginPage();
 		
 		MainNavigation mainNav = loginPage.login();
 
 		TopologyTab topology = mainNav.switchToTopology();
 		
 		ApplicationMap appMap = topology.getApplicationMap();
 		
 		appMap.selectApplication("travel");
 		
 		ApplicationNode travelNode = appMap.getApplicationNode("tomcat");
 		
 		travelNode.select();
 		
 		ProcessingUnit travelPu = admin.getProcessingUnits().getProcessingUnit("travel.tomcat");
 		
 		final GridServiceContainer travelContainer = travelPu.getInstances()[0].getGridServiceContainer();
 		
 		LogsPanel logsPanel = topology.getTopologySubPanel().switchToLogsPanel();
 		
 		PuLogsPanelService travelLogsService = logsPanel.getPuLogsPanelService("travel.tomcat");
 		
 		Machine localHost = travelContainer.getMachine();
 		
 		final LogsMachine logsLocalHost = travelLogsService.getMachine(localHost.getHostName());
 		
 		assertTrue(logsLocalHost.containsGridServiceContainer(travelContainer));
 		
 		ServicesTab servicesTab = mainNav.switchToServices();
 		
 		HostsAndServicesGrid hostAndServicesGrid = servicesTab.getHostAndServicesGrid();
		hostAndServicesGrid.terminateGSC(travelContainer);
 		
 		mainNav.switchToTopology();
 		
 		travelNode.select();
 		
 		ProcessingUnitUtils.waitForDeploymentStatus(travelPu, DeploymentStatus.SCHEDULED);
 		ProcessingUnitUtils.waitForDeploymentStatus(travelPu, DeploymentStatus.INTACT);
 		
 		appMap.deselectAllNodes();
 		
 		RepetitiveConditionProvider condition = new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				return (!logsLocalHost.containsGridServiceContainer(travelContainer));
 			}
 		};
 		AssertUtils.repetitiveAssertTrue("Container" + travelContainer.getAgentId() + "is still present", condition, waitingTime);
 		
 	}
 
 }
