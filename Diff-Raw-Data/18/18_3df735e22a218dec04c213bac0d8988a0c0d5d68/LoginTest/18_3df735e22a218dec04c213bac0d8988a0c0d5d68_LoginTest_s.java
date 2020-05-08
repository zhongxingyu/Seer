 package test;
 
import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.matchText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
 import static com.codeborne.selenide.Configuration.baseUrl;
 import static com.codeborne.selenide.Configuration.timeout;
import static com.codeborne.selenide.Selectors.byAttribute;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selectors.withText;
 import static com.codeborne.selenide.Selenide.$;
 import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.page;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openqa.selenium.Dimension;
 import org.openqa.selenium.Point;
 
 import com.codeborne.selenide.Selenide;
 
 import pageobjects.Common;
import pageobjects.CreateInstanceCarousel;
 import pageobjects.FlashButtons;
import pageobjects.InstanceList;
 import util.TestWrapper;
 
 public class LoginTest extends TestWrapper {
 
 	@BeforeClass
 	public static void openDashboard() {
		open(baseUrl);
 		timeout = BASE_TIMEOUT;
 		baseUrl = BASE_URL;
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
