 package test.webui.dashboard.services;
 
 import static framework.utils.AdminUtils.loadGSCs;
 import static framework.utils.AdminUtils.loadGSM;
 import static framework.utils.LogUtils.log;
 
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.space.SpaceDeployment;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import framework.utils.AssertUtils;
 import framework.utils.AssertUtils.RepetitiveConditionProvider;
 
 import test.webui.AbstractSeleniumTest;
 import test.webui.objects.LoginPage;
 import test.webui.objects.dashboard.DashboardTab;
 import test.webui.objects.dashboard.ServicesGrid;
 import test.webui.objects.dashboard.ServicesGrid.ApplicationServicesGrid;
 import test.webui.objects.dashboard.ServicesGrid.ApplicationServicesGrid.StatefullModule;
 import test.webui.objects.dashboard.ServicesGrid.Icon;
 
 public class PuStatusBeforeDeploymentEndsTest extends AbstractSeleniumTest {
 	
 	Machine machineA;
 	ProcessingUnit pu;
 	GridServiceManager gsmA;
 	
 	@BeforeMethod(alwaysRun = true)
 	public void startSetup() {
 		log("waiting for 1 machine");
 		admin.getMachines().waitFor(1);
 
 		log("waiting for 1 GSA");
 		admin.getGridServiceAgents().waitFor(1);
 
 		GridServiceAgent[] agents = admin.getGridServiceAgents().getAgents();
 		GridServiceAgent gsaA = agents[0];
 
 		machineA = gsaA.getMachine();
 
 		log("starting: 1 GSM and 2 GSC's on 1 machine");
 		gsmA = loadGSM(machineA); 
 		loadGSCs(machineA, 2);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT)
 	public void puStatusTest() throws InterruptedException {
 		
 		LoginPage loginPage = getLoginPage();
 		
 		DashboardTab dashboardTab = loginPage.login().switchToDashboard();
 		
 		ServicesGrid appGrid = dashboardTab.getServicesGrid();
 		
 		final ApplicationServicesGrid applicationServices = appGrid.getApplicationServicesGrid();
 		
 		// deploy a pu
		SpaceDeployment deployment = new SpaceDeployment("Test").partitioned(2, 0).maxInstancesPerVM(1);
 		pu = gsmA.deploy(deployment);
 		
 		RepetitiveConditionProvider condition = new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				if (applicationServices.getStatefullModule() != null) return true;
 				return false;
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 		StatefullModule module = applicationServices.getStatefullModule();
 		
 		DeploymentStatus testStatus = admin.getProcessingUnits().getProcessingUnit("Test").getStatus();
 		while (!testStatus.equals(DeploymentStatus.INTACT)) {
 			Icon icon = module.getIcon();
 			assertTrue((icon != null) && !icon.equals(Icon.OK));
 			testStatus = admin.getProcessingUnits().getProcessingUnit("Test").getStatus();
 		}
 		
 		condition = new RepetitiveConditionProvider() {
 			
 			@Override
 			public boolean getCondition() {
 				StatefullModule module = applicationServices.getStatefullModule();
 				return module.getIcon().equals(Icon.OK);
 			}
 		};
 		AssertUtils.repetitiveAssertTrue(null, condition, waitingTime);
 		
 	}
 
 }
