 package test.webui.recipes.applications;
 
 import static org.testng.AssertJUnit.fail;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gigaspaces.webuitf.LoginPage;
 import com.gigaspaces.webuitf.MainNavigation;
 import com.gigaspaces.webuitf.dashboard.DashboardTab;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.ApplicationServicesGrid;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.ApplicationsMenuPanel;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.Icon;
 import com.gigaspaces.webuitf.dashboard.ServicesGrid.InfrastructureServicesGrid;
 import com.gigaspaces.webuitf.services.PuTreeGrid;
 import com.gigaspaces.webuitf.services.ServicesTab;
 import com.gigaspaces.webuitf.topology.TopologyTab;
 import com.gigaspaces.webuitf.topology.applicationmap.ApplicationMap;
 import com.gigaspaces.webuitf.topology.applicationmap.ApplicationNode;
 import com.gigaspaces.webuitf.topology.applicationmap.Connector;
 
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 
 public class PetClinicTest extends AbstractSeleniumApplicationRecipeTest {
 	
 	private static final String PETCLINIC_APPLICATION_NAME = "petclinic";
 	private static final String MONGOD_FULL_SERVICE_NAME = ServiceUtils.getAbsolutePUName(PETCLINIC_APPLICATION_NAME, "mongod");
 	private static final String MONGOS_FULL_SERVICE_NAME = ServiceUtils.getAbsolutePUName(PETCLINIC_APPLICATION_NAME, "mongos");
 	private static final String MONGOCFG_FULL_SERVICE_NAME = ServiceUtils.getAbsolutePUName(PETCLINIC_APPLICATION_NAME, "mongoConfig");
 	private static final String TOMCAT_FULL_SERVICE_NAME = ServiceUtils.getAbsolutePUName(PETCLINIC_APPLICATION_NAME, "tomcat");
 	private static final String APACHELB_FULL_SERVICE_NAME = ServiceUtils.getAbsolutePUName(PETCLINIC_APPLICATION_NAME, "apacheLB");
 
 	@Override
 	@BeforeMethod
 	public void install() throws IOException, InterruptedException {
 		setCurrentApplication(PETCLINIC_APPLICATION_NAME);
 		super.install();
 	}
 
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = true)
 	public void petClinicDemoTest() throws Exception {
 		
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
 
 		ApplicationsMenuPanel appMenuPanel = dashboardTab.getServicesGrid().getApplicationsMenuPanel();
 
 		appMenuPanel.selectApplication(MANAGEMENT_APPLICATION_NAME);
 
 		final ApplicationServicesGrid applicationServices = dashboardTab.getServicesGrid().getApplicationServicesGrid();
 
 		condition = new RepetitiveConditionProvider() {
 
 			@Override
 			public boolean getCondition() {
 				return ((applicationServices.getWebModule().getCount() == 2)
 						&& (applicationServices.getWebModule().getIcon().equals(Icon.OK)));
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 		
 		appMenuPanel.selectApplication(PETCLINIC_APPLICATION_NAME);
 		
 		condition = new RepetitiveConditionProvider() {
 
 			@Override
 			public boolean getCondition() {
 				return ((applicationServices.getWebServerModule().getCount() == 1)
 						&& (applicationServices.getWebServerModule().getIcon().equals(Icon.OK)));
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 		
 		condition = new RepetitiveConditionProvider() {
 
 			@Override
 			public boolean getCondition() {
 				return ((applicationServices.getNoSqlDbModule().getCount() == 3)
 						&& (applicationServices.getNoSqlDbModule().getIcon().equals(Icon.OK)));
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 		
 		TopologyTab topologyTab = mainNav.switchToTopology();
 
 		final ApplicationMap appMap = topologyTab.getApplicationMap();
 
 		topologyTab.selectApplication(MANAGEMENT_APPLICATION_NAME);
 
 		takeScreenShot(this.getClass(),"petClinicDemoTest", "management-application");
 
 		condition = new RepetitiveConditionProvider() {
 
 			@Override
 			public boolean getCondition() {
 				ApplicationNode restNode = appMap.getApplicationNode("rest");
 				return ((restNode != null) && (restNode.getStatus().equals(DeploymentStatus.INTACT)));
 			}
 		};
 		repetitiveAssertTrueWithScreenshot(null, condition, this.getClass(), "petClinicDemoTest", "failed");
 
 		condition = new RepetitiveConditionProvider() {
 
 			@Override
 			public boolean getCondition() {
 				ApplicationNode webuiNode = appMap.getApplicationNode("webui");
 				return ((webuiNode != null) && (webuiNode.getStatus().equals(DeploymentStatus.INTACT)));
 			}
 		};
 		repetitiveAssertTrueWithScreenshot(null, condition, this.getClass(), "petClinicDemoTest", "failed");
 		
 		takeScreenShot(this.getClass(), "petClinicDemoTest","passed-topology");
 		
 		topologyTab.selectApplication(PETCLINIC_APPLICATION_NAME);
 		
 		condition = new RepetitiveConditionProvider() {
 
 			@Override
 			public boolean getCondition() {
 				ApplicationNode mongodNode = appMap.getApplicationNode(MONGOD_FULL_SERVICE_NAME);
 				return ((mongodNode != null) && (mongodNode.getStatus().equals(DeploymentStatus.INTACT)));
 			}
 		};
 		repetitiveAssertTrueWithScreenshot(null, condition, this.getClass(),"petClinicDemoTest", "failed");
 		
 		condition = new RepetitiveConditionProvider() {
 
 			@Override
 			public boolean getCondition() {
 				ApplicationNode mongosNode = appMap.getApplicationNode(MONGOS_FULL_SERVICE_NAME);
 				return ((mongosNode != null) && (mongosNode.getStatus().equals(DeploymentStatus.INTACT)));
 			}
 		};
 		repetitiveAssertTrueWithScreenshot(null, condition, this.getClass(), "petClinicDemoTest","failed");
 		
 		condition = new RepetitiveConditionProvider() {
 
 			@Override
 			public boolean getCondition() {
 				ApplicationNode mongocfgNode = appMap.getApplicationNode(MONGOCFG_FULL_SERVICE_NAME);
 				return ((mongocfgNode != null) && (mongocfgNode.getStatus().equals(DeploymentStatus.INTACT)));
 			}
 		};
 		repetitiveAssertTrueWithScreenshot(null, condition, this.getClass(),"petClinicDemoTest", "failed");	
 		
 		condition = new RepetitiveConditionProvider() {
 
 			@Override
 			public boolean getCondition() {
 				ApplicationNode tomcatNode = appMap.getApplicationNode(TOMCAT_FULL_SERVICE_NAME);
 				return ((tomcatNode != null) && (tomcatNode.getStatus().equals(DeploymentStatus.INTACT)));
 			}
 		};
 		repetitiveAssertTrueWithScreenshot(null, condition, this.getClass(), "petClinicDemoTest","failed");
 		
 		condition = new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				ApplicationNode apachelbNode = appMap.getApplicationNode(APACHELB_FULL_SERVICE_NAME);
 				return ((apachelbNode != null) && (apachelbNode.getStatus().equals(DeploymentStatus.INTACT)));
 			}
 		};
 		repetitiveAssertTrueWithScreenshot(null, condition, this.getClass(), "petClinicDemoTest","failed");
 		
 		ApplicationNode applicationNodeTomcat = appMap.getApplicationNode(TOMCAT_FULL_SERVICE_NAME);
 		List<Connector> tomcatConnectors = applicationNodeTomcat.getConnectors();
 		
 		ApplicationNode applicationNodeMongos = appMap.getApplicationNode(MONGOS_FULL_SERVICE_NAME);
 		List<Connector> mongosConnectors = applicationNodeMongos.getConnectors();
 		
 		ApplicationNode applicationNodeMongod = appMap.getApplicationNode(MONGOD_FULL_SERVICE_NAME);
 		
 		ApplicationNode applicationNodeMongoConfig = appMap.getApplicationNode(MONGOCFG_FULL_SERVICE_NAME);
 		
 		ApplicationNode applicationNodeApacheLB = appMap.getApplicationNode(APACHELB_FULL_SERVICE_NAME);
 		
 		assertTrue(tomcatConnectors.size() == 2);
		assertTrue(tomcatConnectors.get(0).getTarget().getName().equals(applicationNodeMongos.getName()));
		
		assertTrue(tomcatConnectors.size() == 2);
 		for (Connector c : tomcatConnectors) {
 			String name = c.getTarget().getName();
 			assertTrue(name.equals(applicationNodeMongos.getName()) ||name.equals(applicationNodeApacheLB.getName()));
 		}
 		
 		assertTrue(mongosConnectors.size() == 2);
 		for (Connector c : mongosConnectors) {
 			String name = c.getTarget().getName();
 			assertTrue(name.equals(applicationNodeMongod.getName()) ||name.equals(applicationNodeMongoConfig.getName()));
 		}
 
 		ServicesTab servicesTab = mainNav.switchToServices();
 		
 		PuTreeGrid puTreeGrid = servicesTab.getPuTreeGrid();
 		
 		assertTrue(puTreeGrid.getProcessingUnit(TOMCAT_FULL_SERVICE_NAME) != null);
 		assertTrue(puTreeGrid.getProcessingUnit(MONGOD_FULL_SERVICE_NAME) != null);
 		assertTrue(puTreeGrid.getProcessingUnit(MONGOS_FULL_SERVICE_NAME) != null);
 		assertTrue(puTreeGrid.getProcessingUnit(MONGOCFG_FULL_SERVICE_NAME) != null);
 		assertTrue(puTreeGrid.getProcessingUnit(APACHELB_FULL_SERVICE_NAME) != null);
 		
 		takeScreenShot(this.getClass(), "petClinicDemoTest","passed-services");
 		
 		assertPetclinicPageExists();
 		uninstallApplication(PETCLINIC_APPLICATION_NAME, true);
 	}
 	
 	private void assertPetclinicPageExists() {
 		
 		Machine localMachine = admin.getMachines().getMachines()[0];
 		
 		WebClient client = new WebClient(BrowserVersion.getDefault());
 		
         HtmlPage page = null;
         try {
             page = client.getPage("http://" + localMachine.getHostAddress() + ":8090");
         } catch (IOException e) {
             fail("Could not get a resposne from the petclinic URL " + e.getMessage());
         }
         assertEquals("OK", page.getWebResponse().getStatusMessage());
 		
 		
 	}
 
 }
