 package net.sf.flophase.test.xaction;
 
 import static org.junit.Assert.*;
 
 import java.util.Calendar;
 import java.util.List;
 
 import net.sf.flophase.test.util.FloWebHelper;
 
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
  * This class tests that copying transactions works.
  */
 public class CopyTransactionTest {
 
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
 	 * Creates a transaction for today and copies it to today.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs
 	 */
 	@Test
 	public void testCopyTransactionToToday() throws Exception {
 		helper.addTransaction("Transaction 1");
 
 		// find the transaction name cell that was created
 		WebElement xactionNameCell = driver.findElement(By
 				.className("flo-xactionname"));
 
 		// make sure the transaction name is correct
 		assertEquals("Transaction 1", xactionNameCell.getText());
 		
 		helper.copyTransaction(xactionNameCell, "Transaction 1 Copy", Calendar.getInstance());
 		
 		//get the cell again
 		List<WebElement> xactionNameCells = driver.findElements(By
 				.className("flo-xactionname"));
 
 		// make sure the transaction is copied
 		assertEquals(2, xactionNameCells.size());
 		
 		// make sure the transaction names are correct
 		assertEquals("Transaction 1", xactionNameCells.get(0).getText());
 		assertEquals("Transaction 1 Copy", xactionNameCells.get(1).getText());
 	}
 
 	/**
 	 * Creates a transaction for today and copies it to next month.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs
 	 */
 	@Test
 	public void testCopyTransactionToNextMonth() throws Exception {
 		helper.addTransaction("Transaction 1");
 
 		// find the transaction name cell that was created
 		WebElement xactionNameCell = driver.findElement(By
 				.className("flo-xactionname"));
 
 		// make sure the transaction name is correct
 		assertEquals("Transaction 1", xactionNameCell.getText());
 		
 		final Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.MONTH, 1);
 		
 		helper.copyTransaction(xactionNameCell, "Transaction 1 Copy", cal);
 		
 		//get the cell again
 		List<WebElement> xactionNameCells = driver.findElements(By
 				.className("flo-xactionname"));
 
 		// make sure the transaction is not shown yet
 		assertEquals(1, xactionNameCells.size());
 		
 		
 		
 		// make sure the transaction names are correct
 		assertEquals("Transaction 1", xactionNameCells.get(0).getText());
 		assertEquals("Transaction 1 Copy", xactionNameCells.get(1).getText());
 	}
 
 }
