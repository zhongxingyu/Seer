 package framework.utilities;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.openqa.selenium.By;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.TakesScreenshot;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverException;
 import org.openqa.selenium.remote.Augmenter;
 
 public class TestHelper {
 
	public static WebDriver driver;
	public static boolean localDriverHelper;
	public static boolean remoteDriverHelper;
	public static boolean sauceDriverHelper;
	public static String localScreenshotPath = null;
 
 	public static void setDriver(WebDriver passedInDriver, boolean local,
 			boolean remote, boolean sauce) {
		driver = passedInDriver;
		localDriverHelper = local;
		remoteDriverHelper = remote;
		sauceDriverHelper = sauce;
 	}
 
 	public static void switchToWindow(String windowTitle) {
 		Set<String> windows = driver.getWindowHandles();
 
 		for (String window : windows) {
 			driver.switchTo().window(window);
 			if (driver.getTitle().contains(windowTitle)) {
 				System.out.println("Switching to window: " + driver.getTitle());
 				return;
 			} else {
 				System.out.println("No match for window: " + driver.getTitle());
 			}
 		}
 	}
 
 	public static void switchToFrame(By locator) {
 		String locatorSubString = getLocatorString(locator);
 
 		driver.switchTo().frame(locatorSubString);
 	}
 
 	public static String getLocatorString(By locator) {
 		String[] locatorArray = locator.toString().split("\\s");
 		String locatorSubString = locatorArray[1];
 		System.out.println("Parsing locator: " + locator.toString());
 		System.out.println("Locator String: " + locatorArray[1]);
 
 		return locatorSubString;
 	}
 
 	public By getCustomLocator(String locatorType, String locatorValue)
 			throws Exception {
 
 		// Return a instance of By class based on type of locator
 		if (locatorType.toLowerCase().equals("id")) {
 			return By.id(locatorValue); }
 		else if (locatorType.toLowerCase().equals("name")) {
 			return By.name(locatorValue); }
 		else if ((locatorType.toLowerCase().equals("classname"))
 				|| (locatorType.toLowerCase().equals("class"))) {
 			return By.className(locatorValue); }
 		else if ((locatorType.toLowerCase().equals("tagname"))
 				|| (locatorType.toLowerCase().equals("tag"))) {
 			return By.className(locatorValue); }
 		else if ((locatorType.toLowerCase().equals("linktext"))
 				|| (locatorType.toLowerCase().equals("link"))) {
 			return By.linkText(locatorValue); }
 		else if (locatorType.toLowerCase().equals("partiallinktext")) {
 			return By.partialLinkText(locatorValue); }
 		else if ((locatorType.toLowerCase().equals("cssselector"))
 				|| (locatorType.toLowerCase().equals("css"))) {
 			return By.cssSelector(locatorValue); }
 		else if (locatorType.toLowerCase().equals("xpath")) {
 			return By.xpath(locatorValue); }
 		else {
 			throw new Exception("Locator type '" + locatorType
 					+ "' not defined!!"); }
 	}
 
 	public static void getScreenshot(String path) {
 
 		try {
 
 			SCREENSHOT_PATH = path;
 
 			String timestamp = getTimestamp();
 
 			String screenshotPath = SCREENSHOT_PATH + "//" + timestamp + ".JPG";
 
 			File screenshot;
 
 			if (LOCAL_DRIVER_HELPER) {
 				screenshot = ((TakesScreenshot) DRIVER)
 						.getScreenshotAs(OutputType.FILE);
 				FileUtils.copyFile(screenshot, new File(screenshotPath));
 			} else if (REMOTE_DRIVER_HELPER) {
 				DRIVER = new Augmenter().augment(DRIVER);
 				screenshot = ((TakesScreenshot) DRIVER)
 						.getScreenshotAs(OutputType.FILE);
 				FileUtils.copyFile(screenshot, new File(screenshotPath));
 			} else if (SAUCE_DRIVER_HELPER) {
 				System.out
 						.println("Cannot take screenshot.  Driver is on Sauce.");
 			} else {
 				System.out
 						.println("Cannot take screenshot.  Unable to identify driver type.");
 			}
 		} catch (WebDriverException e) {
 			System.out.println("An exception occurred taking the screenshot.");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("An exception occurred taking the screenshot.");
 			e.printStackTrace();
 		}
 
 	}
 
 	public static String getTimestamp() {
 		
 		Date date = new java.util.Date();
 		String timestamp = (new Timestamp(date.getTime())).toString();
 
 		timestamp = timestamp.replaceAll("/", "-");
 		timestamp = timestamp.replaceAll(":", ".");
 		return timestamp;
 	}
 	
 	public static String getRandomString() {
 	    
 		int length = 10; //String hard coded to length 10 for now
 		String chars = "abcdefghijklmnopqrstuvwxyz";
 		Random rand = new Random();
 		  StringBuilder randomString = new StringBuilder();
 		  for (int i=0; i<length; i++) {
 		    randomString.append(chars.charAt(rand.nextInt(chars.length())));
 		  }
 		  return randomString.toString();
 	}
 	
 	public static void getScreenshot() {
 
 		try {
 
 			String timestamp = getTimestamp();
 
 			String screenshotPath = System.getProperty("TEST_RESULTS_PATH") + "//" + timestamp + ".JPG";
 			
 			File screenshot;
 
 			if (LOCAL_DRIVER_HELPER) {
 				screenshot = ((TakesScreenshot) DRIVER)
 						.getScreenshotAs(OutputType.FILE);
 				FileUtils.copyFile(screenshot, new File(screenshotPath));
 			} else if (REMOTE_DRIVER_HELPER) {
 				DRIVER = new Augmenter().augment(DRIVER);
 				screenshot = ((TakesScreenshot) DRIVER)
 						.getScreenshotAs(OutputType.FILE);
 				FileUtils.copyFile(screenshot, new File(screenshotPath));
 			} else if (SAUCE_DRIVER_HELPER) {
 				System.out
 						.println("Cannot take screenshot.  Driver is on Sauce.");
 			} else {
 				System.out
 						.println("Cannot take screenshot.  Unable to identify driver type.");
 			}
 		} catch (WebDriverException e) {
 			System.out.println("An exception occurred taking the screenshot.");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("An exception occurred taking the screenshot.");
 			e.printStackTrace();
 		}
 
 	}
 
 }
