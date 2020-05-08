 package test.webui;
 
 import org.testng.annotations.Test;
 
 import test.webui.objects.LoginPage;
 import test.webui.objects.dashboard.DashboardTab;
 
 public class LicenseTest extends AbstractSeleniumTest {
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT)
 	public void testCloudifyLicense() throws InterruptedException {
 		
 		LoginPage loginPage = getLoginPage();
 		
 		DashboardTab dashboard = loginPage.login().switchToDashboard();
 		
 		assertTrue(dashboard.isCloudify());
 	}
 
 }
