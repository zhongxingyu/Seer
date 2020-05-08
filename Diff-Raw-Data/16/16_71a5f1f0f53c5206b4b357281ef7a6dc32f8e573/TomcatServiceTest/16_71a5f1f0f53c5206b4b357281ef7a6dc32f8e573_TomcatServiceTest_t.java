 package test.webui.recipes.services;
 
 import java.io.IOException;
 
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.gigaspaces.webuitf.LoginPage;
 import com.gigaspaces.webuitf.MainNavigation;
 import com.gigaspaces.webuitf.dashboard.DashboardTab;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.ApplicationServicesGrid;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.ApplicationsMenuPanel;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.Icon;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.InfrastructureServicesGrid;
 import com.gigaspaces.webuitf.services.PuTreeGrid;
 import com.gigaspaces.webuitf.services.ServicesTab;
 import com.gigaspaces.webuitf.topology.TopologyTab;
 import com.gigaspaces.webuitf.topology.applicationmap.ApplicationMap;
 import com.gigaspaces.webuitf.topology.applicationmap.ApplicationNode;
 import com.gigaspaces.webuitf.topology.healthpanel.HealthPanel;
 
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 
 public class TomcatServiceTest extends AbstractSeleniumServiceRecipeTest {
 
 	@Override
 	@BeforeMethod
 	public void install() throws IOException, InterruptedException {
 		setCurrentRecipe("tomcat");
 		super.install();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, enabled = true)
 	public void tomcatRecipeTest() throws InterruptedException, IOException {
 
 		// get new login page
 		LoginPage loginPage = getLoginPage();
 
 		MainNavigation mainNav = loginPage.login();
 
 		DashboardTab dashboardTab = mainNav.switchToDashboard();
 		
 		final InfrastructureServicesGrid infrastructureServicesGrid = dashboardTab.getServicesGrid().getInfrastructureGrid();
 		
 		RepetitiveConditionProvider condition = new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				return ((infrastructureServicesGrid.getESMInst().getCount() == 1) 
 						&& (infrastructureServicesGrid.getESMInst().getIcon().equals(Icon.OK)));
 			}
 		};
 		AssertUtils.repetitiveAssertTrue("No esm in showing in the dashboard", condition, waitingTime);
 
 		ServicesGrid servicesGrid = dashboardTab.getServicesGrid();
 
 		ApplicationsMenuPanel appMenu = servicesGrid.getApplicationsMenuPanel();
 
 		appMenu.selectApplication(MANAGEMENT);
 
 		final ApplicationServicesGrid applicationServicesGrid = servicesGrid.getApplicationServicesGrid();
 
 		condition = new RepetitiveConditionProvider() {		
 			@Override
 			public boolean getCondition() {
 				return applicationServicesGrid.getWebModule().getCount() == 2;
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 
 		appMenu.selectApplication("default");
 
 		condition = new RepetitiveConditionProvider() {		
 			@Override
 			public boolean getCondition() {
 				return applicationServicesGrid.getWebServerModule().getCount() == 1;
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 
 		TopologyTab topologyTab = mainNav.switchToTopology();
 
 		final ApplicationMap appMap = topologyTab.getApplicationMap();
 
 		appMap.selectApplication(MANAGEMENT);
 		
 		ApplicationNode restful = appMap.getApplicationNode("rest");
 
 		assertTrue(restful != null);
 		assertTrue(restful.getStatus().equals(DeploymentStatus.INTACT));
 
 		ApplicationNode webui = appMap.getApplicationNode("webui");
 
 		assertTrue(webui != null);
 		assertTrue(webui.getStatus().equals(DeploymentStatus.INTACT));
 
 		appMap.selectApplication("default");
 		
 		takeScreenShot(this.getClass(), "tomcatRecipeTest","topology");
 		
 		condition = new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
				ApplicationNode simple = appMap.getApplicationNode("tomcat");
				if (simple != null) {
					return simple.getStatus().equals(DeploymentStatus.INTACT);
				}
				else {
					return false;
				}
 			}
 		};
 		repetitiveAssertTrueWithScreenshot(
				"tomcat service is displayed as " + appMap.getApplicationNode("tomcat").getStatus() + 
 					"even though it is installed", condition, this.getClass(), "tomcatRecipeTest", "tomcat-service");
 
 		HealthPanel healthPanel = topologyTab.getTopologySubPanel().switchToHealthPanel();
 
 		takeScreenShot(this.getClass(),"tomcatRecipeTest", "topology-healthpanel");
 		
 		assertTrue("Process Cpu Usage " + METRICS_ASSERTION_SUFFIX , healthPanel.getMetric("Process Cpu Usage") != null);
 		assertTrue("Total Process Virtual Memory" + METRICS_ASSERTION_SUFFIX, healthPanel.getMetric("Total Process Virtual Memory") != null);
 		assertTrue("Num Of Active Threads" + METRICS_ASSERTION_SUFFIX , healthPanel.getMetric("Num Of Active Threads") != null);
 		assertTrue("Current Http Threads Busy" + METRICS_ASSERTION_SUFFIX, healthPanel.getMetric("Current Http Threads Busy") != null);
 		assertTrue("Request Backlog" + METRICS_ASSERTION_SUFFIX , healthPanel.getMetric("Request Backlog") != null);
 		assertTrue("Active Sessions" + METRICS_ASSERTION_SUFFIX, healthPanel.getMetric("Active Sessions") != null);
 
 		ServicesTab servicesTab = mainNav.switchToServices();
 
 		PuTreeGrid puTreeGrid = servicesTab.getPuTreeGrid();
 
 		assertTrue(puTreeGrid.getProcessingUnit("webui") != null);
 		assertTrue(puTreeGrid.getProcessingUnit("rest") != null);
 		assertTrue(puTreeGrid.getProcessingUnit("default.tomcat") != null);
 		uninstallService("tomcat", true);
 
 	}
 
 }
