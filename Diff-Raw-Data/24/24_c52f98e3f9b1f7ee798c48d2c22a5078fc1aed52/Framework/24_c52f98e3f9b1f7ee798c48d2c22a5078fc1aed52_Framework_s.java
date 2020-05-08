 package framework;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Random;
 
 import org.openqa.selenium.Alert;
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.Wait;
 import org.testng.Assert;
 
 /**
  * Class between selenium driver and page objects
  */
 @Deprecated
 public abstract class Framework {
 	// ------------------- Re implement !
 	/** Driver that do every thing */
 	public static WebDriver driver;
 
 	/** Wait element */
 	public static Wait<WebDriver> wait;
 
 	// ------------------- If needed re implement !
 	/** Time to wait */
 	public static int WaitTime = 30;
 
 	/** Path where all stored */
 	public static String PathToFolder = "c:\\tmp\\";
 
 	/** ThrowExeptions on fail and stop tests */
 	public static boolean throwExeptions = true;
 
 	// ------------------------------------
 	public static Random r = new Random();
 	protected static final String NEW_LINE = System
 			.getProperty("line.separator");
 
 	// -- assets --
 	/**
 	 * Compare equals if not equals - test fail
 	 * 
 	 * @param actual
 	 *            Object - what to compare
 	 * @param expected
 	 *            Object - what we expect
 	 */
 	protected static void assertEquals(Object actual, Object expected) {
 		// Our own assert
 		if (!actual.equals(expected))
 			errorHeppens("Wrong assertEquals");
 		Assert.assertEquals(actual, expected);
 	}
 
 	/**
 	 * @param condition
 	 *            Boolean if false than test fails
 	 */
 	protected static void assertTrue(Boolean condition) {
 		if (!condition)
 			errorHeppens("Wrong assertTrue");
 		Assert.assertTrue(condition);
 	}
 
 	/**
 	 * What actions to perform what get error
 	 */
 	protected static void errorHeppens(String errorMessage) {
 		customErrorHandler(errorMessage);
 
 		if (throwExeptions) {
 			Assert.fail();
 		}
 	}
 
 	/**
 	 * Custom actions with error TODO: move it away from Framework
 	 * 
 	 * @param errorMessage
 	 */
 	public static void customErrorHandler(String errorMessage) {
 		String prefix = now("yyyyMMddhhmmss");
 
 		// Printing to console
 		errorPrint(errorMessage + " " + prefix);
 
 		//
 		tackeScreenShot(prefix);
 		createAndWriteTextFile(errorMessage + NEW_LINE + driver.getCurrentUrl()
 				+ NEW_LINE + driver.getTitle() + NEW_LINE, prefix);
 	}
 
 	// ------------------------- general methods
 	/**
 	 * Getting current time
 	 * 
 	 * @param dateFormat
 	 * @return
 	 */
 	protected static String now(String dateFormat) {
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
 		return sdf.format(cal.getTime());
 	}
 
 	/**
 	 * Printing error message
 	 * 
 	 * @param errorMes
 	 *            - what to print
 	 */
 	protected static void errorPrint(String errorMes) {
 		lnprint(errorMes);
 	}
 
 	/**
 	 * Tacking screen shot
 	 * 
 	 * @return true if a passed good
 	 */
 	protected static boolean tackeScreenShot(String filePrefix) {
//		File scrFile = ((TakesScreenshot) driver)
//				.getScreenshotAs(OutputType.FILE);
//		String fileName = PathToFolder + filePrefix + ".png";
//		try {
			//TODO: add import
//			FileUtils.copyFile(scrFile, new File(fileName));
//		} catch (IOException e) {
//			errorPrint("Screen shot fail");
//			return false;
//		}
 		return true;
 	}
 
 	protected static boolean createAndWriteTextFile(String text, String fileName) {
 		try {
 			Writer output = null;
 			File file = new File(PathToFolder + fileName + ".txt");
 			output = new BufferedWriter(new FileWriter(file));
 			output.write(text);
 			output.close();
 		} catch (IOException e) {
 			errorPrint("File write fail");
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Search for text on page
 	 * 
 	 * @param text
 	 *            - text to search
 	 * @return true if we have such text
 	 */
 	protected static boolean isTextOnPage(String text) {
 		if (driver.getPageSource().contains(text))
 			return true;
 		return false;
 	}
 
 	/**
 	 * Search for text on in some element
 	 * 
 	 * @param text
 	 *            - text to search
 	 * @param element
 	 *            - in what element we are locking in
 	 * @return true if we have such text
 	 */
 	protected static boolean isTextInElement(String text, By element) {
 		if (driver.findElement(element).getText().contains(text))
 			return true;
 		return false;
 	}
 
 	// click open
 	/**
 	 * Perform click on element
 	 * 
 	 * @param by
 	 *            - selector of element
 	 */
 	protected static void click(By by) {
 		try {
 			driver.findElement(by).click();
 		} catch (NoSuchElementException e) {
 			errorHeppens(e.getMessage());
 		}
 	}
 
 	/**
 	 * Perform click on element if it is present
 	 * 
 	 * @param by
 	 *            - selector of element
 	 */
 	protected static boolean clickIfPresent(By by) {
 		if (isElementPresent(by)) { // Is present
 			click(by); // Than click
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Waiting for element to become visible and click on it
 	 * 
 	 * @param by
 	 *            - selector of element
 	 */
 	protected static void waitAndClick(By by) {
 		waitForVisible(by);
 		click(by);
 	}
 
 	/**
 	 * If we have a group of elements on selector we cat click on direct number
 	 * 
 	 * @param pathToElements
 	 *            - selector to group of elements
 	 * @param elementNumber
 	 *            - on which element we are clicking on. Start from 1.
 	 */
 	protected static void clickOnOneElementInGroup(By pathToElements,
 			int elementNumber) {
 		try {
 			WebElement element = driver.findElements(pathToElements).get(
 					elementNumber - 1);
 			element.click();
 		} catch (IndexOutOfBoundsException e) {
 
 		}
 
 	}
 
 	/**
 	 * If we have a group of elements on selector we cat click on direct number
 	 * 
 	 * @param pathToElements
 	 *            - selector to group of elements
 	 */
 	protected static void clickOnOneAnyElementInGroup(By pathToElements) {
 		int size = driver.findElements(pathToElements).size();
 		if (size == 0) {
 			errorHeppens("clickOnOneAnyElementInGroup size == 0");
 		} else {
 			WebElement element = driver.findElements(pathToElements).get(
 					r.nextInt(size));
 			element.click();
 		}
 	}
 
 	// Title
 	/**
 	 * Check if there is such text in title with Assert
 	 * 
 	 * @param textInTitle
 	 *            - string in title
 	 */
 	protected static void isTitleContainsAssert(String textInTitle) {
 		// Waiting for page
 		if (!driver.getTitle().contains(textInTitle))
 			wait(2000);
 
 		// Is title contains
 		assertTrue(driver.getTitle().contains(textInTitle));
 	}
 
 	/**
 	 * Check if there is such text in title
 	 * 
 	 * @param textInTitle
 	 *            - string in title
 	 * @return true if such text is it title
 	 */
 	protected static boolean isTitleContains(String textInTitle) {
 		if (driver.getTitle().contains(textInTitle))
 			return true;
 		return false;
 	}
 
 	/**
 	 * Checking page title with Assert
 	 * 
 	 * @param title
 	 *            - page title
 	 */
 	protected static void isTitleAssert(String title) {
 		assertEquals(driver.getTitle(), title);
 	}
 
 	/**
 	 * Checking page title
 	 * 
 	 * @param title
 	 *            - page title
 	 * @return boolean - true if page have same title
 	 */
 	protected static boolean isTitle(String title) {
 		if (driver.getTitle().equals(title))
 			return true;
 		return false;
 
 	}
 
 	// Forms
 	/**
 	 * Filling value to element
 	 * 
 	 * @param by
 	 *            - what element
 	 * @param value
 	 *            - what to fill
 	 */
 	protected static void fill(By by, String value) {
 		driver.findElement(by).sendKeys(value);
 	}
 
 	/**
 	 * Clearing element value and Filling value to element
 	 * 
 	 * @param by
 	 *            - what element
 	 * @param value
 	 *            - what to fill
 	 */
 	protected static void clearAndFill(By by, String value) {
 		driver.findElement(by).clear();
 		fill(by, value);
 	}
 
 	/**
 	 * Submitting the form
 	 * 
 	 * @param by
 	 *            - what element
 	 */
 	protected static void submitForm(By by) {
 		driver.findElement(by).submit();
 	}
 
 	/**
 	 * Fill all elements in group with value
 	 * 
 	 * @param byGroup
 	 *            - what to fill
 	 * @param value
 	 *            - with what fill, if "" than random 6 numbers
 	 */
 	protected static void fillAll(By byGroup, String value) {
 		List<WebElement> elements = driver.findElements(byGroup);
 		for (WebElement element : elements) {
 			if (value.equals("")) {
 				element.sendKeys(Generator.numbers(6));
 			} else {
 				element.sendKeys(value);
 			}
 		}
 	}
 
 	// Waiting
 	/**
 	 * waits for a minute till page with required title appear
 	 * 
 	 * @param pageTitle
 	 *            - what page to wait
 	 * @return boolean - true when we get page, false if such page was not found
 	 */
 	protected static boolean waitForPage(String pageTitle) {
 		for (int secondNow = 0; secondNow < WaitTime; secondNow++) {
 			try {
 				if (driver.getTitle().contains(pageTitle))
 					return true;
 			} catch (Exception e) {
 			}
 			wait(1000);
 		}
 		return false;
 	}
 
 	/**
 	 * Waits for element to be present
 	 * 
 	 * @param by
 	 *            - element locator Exception if after time put no element found
 	 */
 	protected static void waitForElement(final By by) {
 		wait.until(new ExpectedCondition<Boolean>() {
 			public Boolean apply(WebDriver webDriver) {
 				return webDriver.findElement(by) != null;
 			}
 		});
 	}
 
 	/**
 	 * Waits for text to be present
 	 * 
 	 * @param by
 	 *            - element locator Exception if after time put no element found
 	 */
 	protected static boolean waitForTextOnPage(final String text) {
 		// wait.until(new ExpectedCondition<Boolean>() {
 		// public Boolean apply(WebDriver webDriver) {
 		// return isTextOnPage(text);
 		// }
 		// });
 
 		for (int secondNow = 0; secondNow < WaitTime; secondNow++) {
 			try {
 				if (isTextOnPage(text))
 					return true;
 			} catch (Exception e) {
 			}
 			wait(1000);
 		}
 		return false;
 	}
 
 	/**
 	 * Waits for element to be shown
 	 * 
 	 * @param by
 	 *            - element
 	 * @return true - if element was finally found, false - if no element found
 	 */
 	protected static boolean waitForVisible(By by) {
 		for (int secondNow = 0; secondNow < WaitTime; secondNow++) {
 			// Browsers which render content (such as Firefox and IE) return
 			// "RenderedWebElements"
 			WebElement resultsDiv = driver.findElement(by);
 
 			// If results have been returned, the results are displayed in a
 			// drop down.
 			if (resultsDiv.isDisplayed()) {
 				return true;
 			}
 			wait(1000);
 		}
 		return false;
 	}
 
 	/**
 	 * Waits for element to be hidden
 	 * 
 	 * @param by
 	 *            - element
 	 * @return true - if element was finally found, false - if no element found
 	 */
 	protected static boolean waitForNotVisible(By by) {
 		for (int secondNow = 0; secondNow < WaitTime; secondNow++) {
 			// Browsers which render content (such as Firefox and IE) return
 			// "RenderedWebElements"
 			WebElement resultsDiv = driver.findElement(by);
 
 			// If results have been returned, the results are displayed in a
 			// drop down.
 			if (!resultsDiv.isDisplayed())
 				return true;
 			wait(1000);
 		}
 		return false;
 	}
 
 	/**
 	 * Do no thing for some time
 	 * 
 	 * @param timeToWait
 	 *            - what time to wait in Seconds (sec)
 	 */
 	protected static void wait(int timeToWait) {// TODO:Do not working for
 												// HtmlUnit
 		long end = System.currentTimeMillis() + timeToWait * 1000;
 		while (System.currentTimeMillis() < end) {
 		}
 	}
 
 	/**
 	 * Waiting for element present for some time
 	 * 
 	 * @param by
 	 *            - what element
 	 * @param timeToWait
 	 *            - Seconds to wait
 	 * @return true if element become present in time
 	 */
 	protected static boolean isElementPresentWait(By by, int timeToWait) {
 		if (timeToWait == 0)
 			timeToWait = WaitTime;
 
 		for (int secondNow = 0; secondNow < timeToWait; secondNow++) {
 			if (isElementPresent(by))
 				return true;
 			wait(1000);
 		}
 		return false;
 	}
 
 	// Getting element info
 	/**
 	 * Getting text of the element
 	 * 
 	 * @param by
 	 *            - what element
 	 * @return text of element
 	 */
 	protected static String getText(By by) {
 		String text = "";
 		try {
 			WebElement element = driver.findElement(by);
 			text = element.getText();
 			if (text.isEmpty())
 				text = element.getAttribute("value"); // For input fields
 		} catch (NoSuchElementException e) {
 			errorHeppens(e.getMessage());
 		}
 		return text;
 	}
 
 	/**
 	 * Getting the double of element
 	 * 
 	 * @param by
 	 *            - what element
 	 * @return double
 	 */
 	protected static double getDouble(By by) {
 		String workStr = driver.findElement(by).getText();
 		if (workStr.charAt(0) == '$')
 			workStr = workStr.substring(1);
 		return Double.parseDouble(workStr);
 	}
 
 	/**
 	 * If we can locate element on screen
 	 * 
 	 * @param by
 	 *            - what element
 	 * @return true - there is such element, false - no element
 	 */
 	protected static boolean isElementPresent(By by) {
 		WebElement element = null;
 		try {
 			element = driver.findElement(by);
 		} catch (Exception e) {
 			return false;
 		}
 
 		boolean isPlesent = false;
 		if (element != null)
 			isPlesent = true;
 		return isPlesent;
 	}
 
 	// Alerts
 	/**
 	 * Accepting java script alert
 	 */
 	protected static void alertAccept() {
 		wait(500); // Waiting for message
 		Alert alert = driver.switchTo().alert();
 		alert.accept();
 	}
 
 	// ----------------------------------------------------------
 
 	/**
 	 * Show info in console
 	 * 
 	 * @param str
 	 */
 	protected static void print(String str) {
 		System.out.print(str);
 	}
 
 	/**
 	 * Printing data to command line + new line
 	 * 
 	 * @param str
 	 *            String
 	 */
 	protected static void println(String str) {
 		System.out.println(str);
 	}
 
 	/**
 	 * new line + Printing data to vommand line
 	 * 
 	 * @param str
 	 *            String
 	 */
 	protected static void lnprint(String str) {
 		println("");
 		print(str);
 	}
 
 }
