 package net.sf.flophase.test.account;
 
 import static org.junit.Assert.*;
 
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
  * This class tests that deleting accounts works.
  */
 public class DeleteAccountTest {
 
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
 
 	/**
 	 * Tears down the test case. Logs out.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs.
 	 */
 	@After
 	public void tearDown() throws Exception {
 		helper.logout();
 	}
 
 	/**
 	 * Creates a single account and deletes that account.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs
 	 */
 	@Test
 	public void testDeleteSecondAccount() throws Exception {
 		helper.addAccount("New Account", 12.34);
 
 		// we need to find the header cell for the new account
 		// start by finding the header row
 		WebElement accountHeaderRow = helper.waitForElement(By
 				.id("accountHeaderRow"));
 		List<WebElement> accountEntryHeaderCells = accountHeaderRow
 				.findElements(By.tagName("th"));
 
 		// ensure we now have 4 header cells (2 accounts*2 cells)
 		assertEquals(4, accountEntryHeaderCells.size());
 
 		// get the second cell
 		WebElement newAccountEntryHeaderCell = accountEntryHeaderCells.get(1);
 		newAccountEntryHeaderCell.click();
 
 		// click the delete button
 		WebElement deleteButton = helper.waitForVisibleElement(By
 				.id("accountDelete"));
 		deleteButton.click();
 
 		// wait for things to happen
 		Thread.sleep(2000);
 
 		// reload the header cells
 		accountEntryHeaderCells = accountHeaderRow.findElements(By
 				.tagName("th"));
 
 		// make sure there are now only 2
 		assertEquals(2, accountEntryHeaderCells.size());
 
 		// make sure the right cell was removed
 		newAccountEntryHeaderCell = accountEntryHeaderCells.get(1);
 		assertEquals("My Account", newAccountEntryHeaderCell.getText());
 	}
 
 	/**
 	 * Creates a second account and deletes the first account.
 	 * 
 	 * @throws Exception
 	 *             If an error occurs
 	 */
 	@Test
 	public void testDeleteFirstAccount() throws Exception {
 		helper.addAccount("New Account", 12.34);
 
 		// we need to find the header cell for the new account
 		// start by finding the header row
 		WebElement accountHeaderRow = helper.waitForElement(
 				By.id("accountHeaderRow"), 5000);
 		List<WebElement> accountEntryHeaderCells = accountHeaderRow
 				.findElements(By.tagName("th"));
 
 		// ensure we now have 4 header cells (2 accounts*2 cells)
 		assertEquals(4, accountEntryHeaderCells.size());
 
 		// get the third cell
 		WebElement accountEntryHeaderCell = accountEntryHeaderCells.get(2);
 		accountEntryHeaderCell.click();
 
 		// click the delete button
 		WebElement deleteButton = helper.waitForVisibleElement(By
 				.id("accountDelete"));
 		deleteButton.click();
 
 		// wait for things to happen
 		Thread.sleep(2000);
 
 		// reload the header cells
 		accountEntryHeaderCells = accountHeaderRow.findElements(By
 				.tagName("th"));
 
 		// make sure there are now only 2
 		assertEquals(2, accountEntryHeaderCells.size());
 
 		// make sure the right cell was removed
 		accountEntryHeaderCell = accountEntryHeaderCells.get(0);
 		assertEquals("New Account", accountEntryHeaderCell.getText());
 	}
 
 	@Test
 	public void testUnableToDeleteLastAccount() throws Exception {
 		// we need to find the header cell for the account
 		// start by finding the header row
 		WebElement accountHeaderRow = helper.waitForElement(
 				By.id("accountHeaderRow"), 5000);
 		List<WebElement> accountEntryHeaderCells = accountHeaderRow
 				.findElements(By.tagName("th"));
 
 		// get the header cells
 		accountEntryHeaderCells = accountHeaderRow.findElements(By
 				.tagName("th"));
 
 		// make sure there are only 2
 		assertEquals(2, accountEntryHeaderCells.size());
 
 		// get the first cell
 		WebElement accountEntryHeaderCell = accountEntryHeaderCells.get(0);
 		accountEntryHeaderCell.click();
 
 		// click the delete button
 		WebElement deleteButton = helper.waitForVisibleElement(By
 				.id("accountDelete"));
		assertTrue(deleteButton.getAttribute("class").contains("disabled"));
 	}
 
 }
