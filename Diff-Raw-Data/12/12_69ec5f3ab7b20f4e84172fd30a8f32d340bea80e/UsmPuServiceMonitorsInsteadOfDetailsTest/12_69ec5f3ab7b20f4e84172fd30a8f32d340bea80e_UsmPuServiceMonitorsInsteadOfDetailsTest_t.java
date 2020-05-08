 package test.webui.recipes.services;
 
 import java.io.IOException;
 
 import org.junit.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.gigaspaces.webuitf.LoginPage;
 import com.gigaspaces.webuitf.MainNavigation;
 import com.gigaspaces.webuitf.WebConstants;
 import com.gigaspaces.webuitf.topology.Metric;
 import com.gigaspaces.webuitf.topology.MetricType;
 import com.gigaspaces.webuitf.topology.TopologyTab;
 import com.gigaspaces.webuitf.topology.applicationmap.ApplicationMap;
 import com.gigaspaces.webuitf.topology.applicationmap.ApplicationNode;
 import com.gigaspaces.webuitf.topology.healthpanel.HealthPanel;
 
 public class UsmPuServiceMonitorsInsteadOfDetailsTest extends AbstractSeleniumServiceRecipeTest {
 	
 	private static final String ACTIVEMQ_FULL_SERVICE_NAME = DEFAULT_APPLICATION_NAME + ".activemq";
 	private static final String STORE_PERCENT_USAGE = "gs-metric-title-CUSTOM_Store_Percent_Usage";
 	
 	@Override
 	@BeforeMethod
 	public void install() throws IOException, InterruptedException {
 		setBrowser(WebConstants.CHROME);
 		setCurrentRecipe("activemq");
 		super.install();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT)
 	public void testServiceMonitors() throws InterruptedException, IOException {
 		
 		boolean shouldFail = false;
 		String failureMessage = "";
 		
 		// get new login page
 		LoginPage loginPage = getLoginPage();
 		
 		MainNavigation mainNav = loginPage.login();
 		
 		TopologyTab topologyTab = mainNav.switchToTopology();
 		
 		ApplicationMap appMap = topologyTab.getApplicationMap();
 			
 		topologyTab.selectApplication(DEFAULT_APPLICATION_NAME);
 		
 		ApplicationNode activemq = appMap.getApplicationNode(ACTIVEMQ_FULL_SERVICE_NAME);
 		
 		HealthPanel healthPanel = topologyTab.getTopologySubPanel().switchToHealthPanel();
 		
 		activemq.select();
 		
 		Metric storePercentUsage = healthPanel.getMetric(STORE_PERCENT_USAGE);
 		
 		try { 
			storePercentUsage.swithToMetric(new MetricType("Misc.", "Child Process ID", null));
 			shouldFail = true;
 			failureMessage += "Child Process ID should not be a part of the metric selection menu\n";
 		}
		catch (Exception e) {
 			
 		}
 		try { 
 			storePercentUsage.swithToMetric(new MetricType("Misc.", "Actual Process ID", null));
 			shouldFail = true;
 			failureMessage += "Actual Process ID should not be a part of the metric selection menu\n";
 		}
		catch (Exception e) {
 			
 		}
 		try { 
 			storePercentUsage.swithToMetric(new MetricType("Misc.", "USM_State", null));
 			shouldFail = true;
 			failureMessage += "USM_State should not be a part of the metric selection menu\n";
 		}
		catch (Exception e) {
 			
 		}
 		if (shouldFail) {
 			Assert.fail(failureMessage);
 		}
 		uninstallService("activemq", true);
 	}
 }
