 import static org.testng.AssertJUnit.*;
 
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.thoughtworks.selenium.Selenium;
 
 
 
 public class OnlineFrontend {
 	private Selenium selenium;
 	protected static String baseUrl = SeleniumHelper.baseUrl;
 
     @BeforeClass(alwaysRun = true)
     public void setUp() throws Exception {
         SeleniumHelper.setupSelenium();
         this.selenium = SeleniumHelper.selenium;
     }
 
     @Test(groups = {"MyApp1", "setupNewCustomer"}, dependsOnGroups = {"createAndGet"})
     public void setupNewCustomer() {
         selenium.open("/ncr");
         clickAndWait("link=About Google");
        assertTrue(selenium.isTextPresent("Googles mission is to organize the worlds information and make it universally accessible and useful."));
     }
 
 	private void clickAndWait(String link) {
 		selenium.click(link);
 		selenium.waitForPageToLoad("30000");
 		assertTrue(selenium.getLocation().startsWith(baseUrl));
 		assertTrue(!selenium.isTextPresent("ERROR"));
 	}
 
 	@AfterClass(alwaysRun = true)
 	public void tearDown() {
 		SeleniumHelper.tearDownSelenium();
 	}
 }
