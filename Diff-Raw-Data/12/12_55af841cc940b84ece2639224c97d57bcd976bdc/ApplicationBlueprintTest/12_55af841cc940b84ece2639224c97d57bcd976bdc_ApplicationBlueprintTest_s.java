 package test.webui.recipes.applications;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.webui.objects.LoginPage;
 import test.webui.objects.topology.TopologyTab;
 import test.webui.objects.topology.applicationmap.ApplicationMap;
 import test.webui.objects.topology.applicationmap.ApplicationNode;
 import test.webui.objects.topology.applicationmap.Connector;
 
 public class ApplicationBlueprintTest extends AbstractSeleniumApplicationRecipeTest {
 	
 	@Override
 	@BeforeMethod
 	public void install() throws IOException, InterruptedException {
 		setCurrentApplication("travel");
 		setWait(false);
 		super.install();
 	}
 	
	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false)
 	public void blueprintTest() throws IOException, InterruptedException {
 		
 		LoginPage loginPage = getLoginPage();
 		
 		TopologyTab topologyTab = loginPage.login().switchToTopology();
 		
 		ApplicationMap applicationMap = topologyTab.getApplicationMap();
 		applicationMap.selectApplication("travel");
 		
 		ApplicationNode cassandra = applicationMap.getApplicationNode("travel.cassandra");
 
 		assertTrue(cassandra != null);
 		assertTrue(
 				cassandra.getStatus().equals(DeploymentStatus.SCHEDULED)
 				|| cassandra.getStatus().equals(DeploymentStatus.INTACT));	
 
 		ApplicationNode tomcat = applicationMap.getApplicationNode("travel.tomcat");
 
 		assertTrue(tomcat != null);
 		assertTrue(
 				tomcat.getStatus().equals(DeploymentStatus.SCHEDULED)
 				|| tomcat.getStatus().equals(DeploymentStatus.INTACT));		
 
 		List<Connector> connectors = tomcat.getConnectors();
 		assertTrue(connectors.size() == 1);
 		List<Connector> targets = tomcat.getTargets();
 		assertTrue(targets.size() == 1);
 		assertTrue(targets.get(0).getTarget().getName().equals(cassandra.getName()));
 	}
 }
