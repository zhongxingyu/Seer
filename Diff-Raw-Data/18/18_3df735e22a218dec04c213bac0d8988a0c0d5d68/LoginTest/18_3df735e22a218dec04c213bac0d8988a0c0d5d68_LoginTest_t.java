 package test;
 
 import static com.codeborne.selenide.Configuration.baseUrl;
 import static com.codeborne.selenide.Configuration.timeout;
 import static com.codeborne.selenide.Selenide.$;
 import static com.codeborne.selenide.Selenide.open;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openqa.selenium.Dimension;
 import org.openqa.selenium.Point;
 
 import com.codeborne.selenide.Selenide;
 
 import pageobjects.Common;
 import pageobjects.FlashButtons;
 import util.TestWrapper;
 
 public class LoginTest extends TestWrapper {
 
 	@BeforeClass
 	public static void openDashboard() {
 		timeout = BASE_TIMEOUT;
 		baseUrl = BASE_URL;
		open(baseUrl);
 		driver.manage().window().setPosition(new Point(0, 0));
 		driver.manage().window().setSize(new Dimension(1280, 1024));
 		Common.login();
 	}
 
 	@AfterClass
 	public static void logout() {
 		open(baseUrl + "/logout");
 		
 	}
 
 	@After
 	public void goToDashboard() {
 		open(baseUrl);
 	}
 
 	@Test
 	public void loginAndConfirm() {
 		$("#shieldButton").click();
 		
 		try {
 			driver.wait(10000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		assert(driver.getTitle().indexOf("") > 0);
 		
 		FlashButtons.confirmLogin();
 		
 	}
 	
 	public static void sleep(Long time) {
 		((LoginTest) driver).sleep(time);
 	}
 }
