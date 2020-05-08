 package net.sf.flophase.test.xaction;
 
 import static org.junit.Assert.*;
 import static org.junit.Assume.*;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 
 import net.sf.flophase.test.util.FloWebHelper;
 
 import org.hamcrest.number.OrderingComparisons;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 /**
  * This class tests that adding transactions works.
  */
 public class AddTransactionTest {
 
 	/**
 	 * The selenium web driver
 	 */
 	private static WebDriver driver;
 
 	/**
 	 * Helper class for web unit tests.
 	 */
 	private static FloWebHelper helper;
 
 	/**
 	 * The username for login
 	 */
 	private String username;
 
 	/**
 	 * Sets up the driver and the helper.
 	 * 
 	 * @throws Exception
 	 *             IF an error occurs.
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		driver = new FirefoxDriver();
 
 		helper = new FloWebHelper(driver);
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		driver.close();
 	}
 
 	/**
 	 * Sets up the test case.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs.
 	 */
 	@Before
 	public void setUp() throws Exception {
 		username = helper.getEmail();
 		helper.start();
 		helper.login(username);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		helper.logout();
 	}
 
 	/**
 	 * Creates a transaction in the previous month and verifies the name and date.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs
 	 */
 	@Test
 	public void testAddTransactionLastMonth() throws Exception {
 		Calendar cal = Calendar.getInstance();
 
 		cal.add(Calendar.MONTH, -1);
 		
 		helper.addTransaction("Transaction 1", cal);
 
 		// wait for things to happen
 		Thread.sleep(2000);
 
 		// find the transaction name cell that was created
 		List<WebElement> xactionNameCells = driver
 				.findElements(By
 						.xpath("//tbody[@id='historicBody']/tr/td[@class='flo-xactionname']"));
 
 		// make sure the transaction was not displayed
 		assertEquals(0, xactionNameCells.size());
 		
 		driver.findElement(By.id("loadEarlier")).click();
 		
 		//wait a little bit
 		Thread.sleep(250);
 				
 		WebElement xactionNameCell = driver
 				.findElement(By
 						.xpath("//tbody[@id='historicBody']/tr/td[@class='flo-xactionname']"));
 
 		// use the id to get the transaction id
 		String xactionNameCellId = xactionNameCell.getAttribute("id");
 		String xactionId = xactionNameCellId.substring(xactionNameCellId
 				.indexOf('_') + 1);
 
 		// get the hidden input next to the calendar dropdown
 		WebElement xactionDateInput = helper.waitForElement(By
 				.cssSelector("#dateInput_" + xactionId + " + input"));
 
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		String today = format.format(cal.getTime());
 
 		// make sure the value is today as selected above
 		assertEquals(today, xactionDateInput.getAttribute("value"));
 	}
 
 	/**
 	 * Creates a transaction in the next month and verifies the name and date.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs
 	 */
 	@Test
 	public void testAddTransactionNextMonth() throws Exception {
 		Calendar cal = Calendar.getInstance();
 
 		cal.add(Calendar.MONTH, 1);
 		
 		helper.addTransaction("Transaction 1", cal);
 
 		// wait for things to happen
 		Thread.sleep(2000);
 
 		// find the transaction name cell that was created
 		List<WebElement> xactionNameCells = driver
 				.findElements(By
 						.xpath("//tbody[@id='upcomingBody']/tr/td[@class='flo-xactionname']"));
 
 		// make sure the transaction was not displayed
 		assertEquals(0, xactionNameCells.size());
 		
 		driver.findElement(By.id("loadUpcoming")).click();
 		
 		//wait a little bit
 		Thread.sleep(250);
 				
 		WebElement xactionNameCell = driver
 				.findElement(By
 						.xpath("//tbody[@id='upcomingBody']/tr/td[@class='flo-xactionname']"));
 
 		// use the id to get the transaction id
 		String xactionNameCellId = xactionNameCell.getAttribute("id");
 		String xactionId = xactionNameCellId.substring(xactionNameCellId
 				.indexOf('_') + 1);
 
 		// get the hidden input next to the calendar dropdown
 		WebElement xactionDateInput = helper.waitForElement(By
 				.cssSelector("#dateInput_" + xactionId + " + input"));
 
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		String today = format.format(cal.getTime());
 
 		// make sure the value is today as selected above
 		assertEquals(today, xactionDateInput.getAttribute("value"));
 	}
 
 	/**
 	 * Creates a transaction in December of last year and verifies the name and date.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs
 	 */
 	@Test
 	public void testAddTransactionLastYear() throws Exception {
 		Calendar cal = Calendar.getInstance();
 
 		cal.add(Calendar.YEAR, -1);
 		cal.set(Calendar.MONTH, cal.getMaximum(Calendar.MONTH));
 		
 		helper.addTransaction("Transaction 1", cal);
 
 		// wait for things to happen
 		Thread.sleep(2000);
 
 		// find the transaction name cell that was created
 		List<WebElement> xactionNameCells = driver
 				.findElements(By
 						.xpath("//tbody[@id='historicBody']/tr/td[@class='flo-xactionname']"));
 
 		// make sure the transaction was not displayed
 		assertEquals(0, xactionNameCells.size());
 		
 		WebElement loadEarlierButton = driver.findElement(By.id("loadEarlier"));
 		
 		//when the load earlier button contains the year, we have loaded the transaction
 		final String year = String.valueOf(cal.get(Calendar.YEAR));
 		while(!loadEarlierButton.getText().contains(year)) {
 			loadEarlierButton.click();
 			
 			//wait a little bit
 			Thread.sleep(250);
 		}
 		
		//click once more to load the last month of the previous year
		loadEarlierButton.click();
		
		WebElement xactionNameCell = helper.waitForElement(By
 						.xpath("//tbody[@id='historicBody']/tr/td[@class='flo-xactionname']"));
 
 		// use the id to get the transaction id
 		String xactionNameCellId = xactionNameCell.getAttribute("id");
 		String xactionId = xactionNameCellId.substring(xactionNameCellId
 				.indexOf('_') + 1);
 
 		// get the hidden input next to the calendar dropdown
 		WebElement xactionDateInput = helper.waitForElement(By
 				.cssSelector("#dateInput_" + xactionId + " + input"));
 
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		String today = format.format(cal.getTime());
 
 		// make sure the value is today as selected above
 		assertEquals(today, xactionDateInput.getAttribute("value"));
 	}
 
 	/**
 	 * Creates a transaction in january of next year and verifies the name and date.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs
 	 */
 	@Test
 	public void testAddTransactionNextYear() throws Exception {
 		Calendar cal = Calendar.getInstance();
 
 		cal.add(Calendar.YEAR, 1);
 		cal.set(Calendar.MONTH, Calendar.JANUARY);
 		
 		helper.addTransaction("Transaction 1", cal);
 
 		// wait for things to happen
 		Thread.sleep(2000);
 
 		// check for the transaction name cell that would be created
 		List<WebElement> xactionNameCells = driver
 				.findElements(By
 						.xpath("//tbody[@id='upcomingBody']/tr/td[@class='flo-xactionname']"));
 
 		// make sure the transaction was not displayed
 		assertEquals(0, xactionNameCells.size());
 		
 		WebElement loadUpcomingButton = driver.findElement(By.id("loadUpcoming"));
 		
 		//when the load upcoming button contains the year, one more will load the transaction
 		final String year = String.valueOf(cal.get(Calendar.YEAR));
 		while(!loadUpcomingButton.getText().contains(year)) {
 			loadUpcomingButton.click();
 			
 			//wait a little bit
 			Thread.sleep(250);
 		}
 		
 		loadUpcomingButton.click();
 		
 		WebElement xactionNameCell = helper
 				.waitForElement(By
 						.xpath("//tbody[@id='upcomingBody']/tr/td[@class='flo-xactionname']"));
 
 		// use the id to get the transaction id
 		String xactionNameCellId = xactionNameCell.getAttribute("id");
 		String xactionId = xactionNameCellId.substring(xactionNameCellId
 				.indexOf('_') + 1);
 
 		// get the hidden input next to the calendar dropdown
 		WebElement xactionDateInput = helper.waitForElement(By
 				.cssSelector("#dateInput_" + xactionId + " + input"));
 
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		String today = format.format(cal.getTime());
 
 		// make sure the value is today as selected above
 		assertEquals(today, xactionDateInput.getAttribute("value"));
 	}
 
 	/**
 	 * Creates a transaction and verifies the name and date.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs
 	 */
 	@Test
 	public void testAddTransactionAfterToday() throws Exception {
 		// find the week and day of the week for today
 		Calendar cal = Calendar.getInstance();
 
 		// this test will only work on days before the last
 		assumeThat(cal.get(Calendar.DAY_OF_MONTH),
 				OrderingComparisons.lessThan(cal
 						.getActualMaximum(Calendar.DAY_OF_MONTH)));
 
 		cal.set(Calendar.DAY_OF_MONTH,
 				cal.getActualMaximum(Calendar.DAY_OF_MONTH));
 		
 		helper.addTransaction("Transaction 1", cal);
 
 		// wait for things to happen
 		Thread.sleep(2000);
 
 		// find the transaction name cell that was created
 		WebElement xactionNameCell = driver
 				.findElement(By
 						.xpath("//tbody[@id='upcomingBody']/tr/td[@class='flo-xactionname']"));
 
 		// make sure the transaction name is correct
 		assertEquals("Transaction 1", xactionNameCell.getText());
 
 		// use the id to get the transaction id
 		String xactionNameCellId = xactionNameCell.getAttribute("id");
 		String xactionId = xactionNameCellId.substring(xactionNameCellId
 				.indexOf('_') + 1);
 
 		// get the hidden input next to the calendar dropdown
 		WebElement xactionDateInput = helper.waitForElement(By
 				.cssSelector("#dateInput_" + xactionId + " + input"));
 
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		String today = format.format(cal.getTime());
 
 		// make sure the value is today as selected above
 		assertEquals(today, xactionDateInput.getAttribute("value"));
 	}
 
 	/**
 	 * Creates a transaction on the first day of the month and verifies the name
 	 * and date.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs
 	 */
 	@Test
 	public void testAddTransactionBeforeToday() throws Exception {
 		// find the week and day of the week for today
 		Calendar cal = Calendar.getInstance();
 
 		// this test will only work on days after the first
 		assumeThat(cal.get(Calendar.DAY_OF_MONTH),
 				OrderingComparisons.greaterThan(1));
 
 		cal.set(Calendar.DAY_OF_MONTH, 1);
 		
 		helper.addTransaction("Transaction 1", cal);
 
 		// find the transaction name cell that was created
 		WebElement xactionNameCell = driver
 				.findElement(By
 						.xpath("//tbody[@id='historicBody']/tr/td[@class='flo-xactionname']"));
 
 		// make sure the transaction name is correct
 		assertEquals("Transaction 1", xactionNameCell.getText());
 
 		// use the id to get the transaction id
 		String xactionNameCellId = xactionNameCell.getAttribute("id");
 		String xactionId = xactionNameCellId.substring(xactionNameCellId
 				.indexOf('_') + 1);
 
 		// get the hidden input next to the calendar dropdown
 		WebElement xactionDateInput = helper.waitForElement(By
 				.cssSelector("#dateInput_" + xactionId + " + input"));
 
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		String today = format.format(cal.getTime());
 
 		// make sure the value is today as selected above
 		assertEquals(today, xactionDateInput.getAttribute("value"));
 	}
 
 	/**
 	 * Creates a transaction on toay's date and verifies the name and date.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs
 	 */
 	@Test
 	public void testAddTransactionToday() throws Exception {
 		// we need to time this case to allow a timeout if certain interactions
 		// fail
 		final long startTime = System.currentTimeMillis();
 
 		// get the add transaction button
 		WebElement addTransactionButton = helper.waitForElement(By
 				.id("addTransaction"));
 
 		// while the button is marked as disabled (css class)
 		while (addTransactionButton.getAttribute("class").contains("disabled")) {
 			if (System.currentTimeMillis() > startTime + 5000) {
 				fail("Add transaction button did not become enabled");
 			}
 		}
 
 		// click the add transaction button
 		addTransactionButton.click();
 
 		// get the transaction name input
 		WebElement newTransactionNameInput = helper
 				.waitForVisibleEnabledElement(By.id("newTransactionName"));
 
 		// clear the value and input a new name
 		newTransactionNameInput.clear();
 		newTransactionNameInput.sendKeys("Transaction 1");
 
 		// find the week and day of the week for today
 		Calendar cal = Calendar.getInstance();
 		int week = cal.get(Calendar.WEEK_OF_MONTH);
 		int day = cal.get(Calendar.DAY_OF_WEEK);
 
 		// find the date on the calendar
 		driver.findElement(
 				By.xpath("//table[@id='newTransactionCalendar']/tbody/tr["
 						+ week + "]/td[" + day + "]/span")).click();
 
 		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 		String today = format.format(cal.getTime());
 
 		// ensure the hidden input was updated
 		WebElement newTransactionDateInput = helper.waitForElement(By
 				.id("newTransactionDate"));
 		assertEquals(today, newTransactionDateInput.getAttribute("value"));
 
 		// click the ok button
 		driver.findElement(By.id("addTransactionOk")).click();
 
 		// wait for things to happen
 		Thread.sleep(2000);
 
 		// find the transaction name cell that was created
 		WebElement xactionNameCell = driver
 				.findElement(By
 						.xpath("//tbody[@id='historicBody']/tr/td[@class='flo-xactionname']"));
 
 		// make sure the transaction name is correct
 		assertEquals("Transaction 1", xactionNameCell.getText());
 
 		// use the id to get the transaction id
 		String xactionNameCellId = xactionNameCell.getAttribute("id");
 		String xactionId = xactionNameCellId.substring(xactionNameCellId
 				.indexOf('_') + 1);
 
 		// get the hidden input next to the calendar dropdown
 		WebElement xactionDateInput = helper.waitForElement(By
 				.cssSelector("#dateInput_" + xactionId + " + input"));
 
 		// make sure the value is today as selected above
 		assertEquals(today, xactionDateInput.getAttribute("value"));
 	}
 
 }
