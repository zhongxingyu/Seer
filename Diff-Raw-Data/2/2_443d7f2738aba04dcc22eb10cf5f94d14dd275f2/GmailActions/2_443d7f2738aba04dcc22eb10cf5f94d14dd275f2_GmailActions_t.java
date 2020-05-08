 package Actions;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import com.Softcrylic.demo.testautomation.Utilities.LogWriter;
 
 public class GmailActions {
 	public GmailActions(WebDriver driver, StringBuffer sb, LogWriter logger) {
 		this.driver = driver;
 		this.sb = sb;
 		this.logger = logger;
 	}
 
 	public WebDriver doGmailTest() throws Exception {
 		try {
 			driver.get("http://www.google.com");
			logger.createTestStepWithImage("Go to Google Home page",
 					"Google.com should be opened", "Google.com is opened", true);
 		} catch (Exception e) {
 			sb.append(e.getMessage());
 			logger.createTestStepWithImage("Go to Google.com",
 					"Google.com should be opened", "Google.com is not opened",
 					false);
 			return driver;
 		}
 		try {
 			// Look for search textbox and enter search term there
 			WebElement searchBox = driver.findElement(By.name("q"));
 			searchBox.sendKeys("WebDriver API");
 			logger.createTestStepWithImage("Type in WebDriver API", "Sucess",
 					"Sucess", true);
 		} catch (Exception e) {
 			logger.createTestStepWithImage("Type in WebDriver API", "Sucess",
 					"failed", false);
 			sb.append(e.getMessage());
 		}
 		try {
 			// Click on 'Search'
 			WebElement searchButton = driver.findElement(By.name("btnG"));
 			searchButton.click();
 			logger.createTestStepWithImage("Click on Search button", "Sucess",
 					"Sucess", true);
 		} catch (Exception e) {
 			logger.createTestStepWithImage("Click on Search button", "Sucess",
 					"failed", false);
 			sb.append(e.getMessage());
 		}
 		// Not required or recommended any where, but just wait for the last
 		// click()
 		// operation to get completed fine
 
 		System.out.println("What's the current Url: " + driver.getCurrentUrl());
 		return driver;
 	}
 
 	WebDriver driver;
 	StringBuffer sb;
 	LogWriter logger;
 }
