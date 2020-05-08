 package net.canadensys.api.narwhal;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.PageFactory;
 
 /**
  * Integration tests of the rendered pages.
  * @author canadensys
  *
  */
 public class APIWebPageIntegrationTest extends AbstractIntegrationTest {
 	
 	@FindBy(css = "form.narwhal-form textarea[name='data']")
 	private WebElement dataTextArea;
 	
 	@FindBy(css = "form.narwhal-form input[type='submit']")
 	private WebElement submit;
 	
 	@FindBy(css = "table.narwhal-results tbody")
 	private WebElement resultTableBody;
 	
 	@Before
 	public void setup() {
 		browser = new FirefoxDriver();
 	}
 	 
 	@Test
 	public void testCoordinatesPage() {
 		//Coordinates is the landing page
 		browser.get(TESTING_SERVER_URL);
 		
 		//bind the WebElement to the current page
 		PageFactory.initElements(browser, this);
 		
 		dataTextArea.sendKeys("2 | 40°26′47″N,74° 0' 21.5022\"W");
 		submit.click();
 		
 		assertEquals("2", resultTableBody.findElement(By.cssSelector("td:nth-child(1)")).getText());
 		assertEquals("40°26′47″N,74° 0' 21.5022\"W", resultTableBody.findElement(By.cssSelector("td:nth-child(2)")).getText());
 		assertEquals("40.4463889", resultTableBody.findElement(By.cssSelector("td:nth-child(3)")).getText());
 		assertEquals("-74.0059728", resultTableBody.findElement(By.cssSelector("td:nth-child(4)")).getText());
 	}
	
 	@Test
	public void testCoordinatesResultPageWithTabSeparator() {
		//Send the URL parameters to easily send a tab character as coordinate separator
		browser.get(TESTING_SERVER_URL+"coordinates?data=40%C2%B026%E2%80%B247%E2%80%B3N%0974%C2%B0%200'%2021.5022%22W");
 		
 		//bind the WebElement to the current page
 		PageFactory.initElements(browser, this);
		assertTrue(resultTableBody.findElement(By.cssSelector("td:nth-child(1)")).getText().contains("40°26′47″N"));
		assertTrue(resultTableBody.findElement(By.cssSelector("td:nth-child(1)")).getText().contains("74° 0' 21.5022"));
		assertEquals("40.4463889", resultTableBody.findElement(By.cssSelector("td:nth-child(2)")).getText());
		assertEquals("-74.0059728", resultTableBody.findElement(By.cssSelector("td:nth-child(3)")).getText());
 	}
 	
 	@Test
 	public void testCoordinatesResultPageWithTabSeparator() {
 		//Send the URL parameters to easily send a tab character as coordinate separator
 		browser.get(TESTING_SERVER_URL+"coordinates?data=40%C2%B026%E2%80%B247%E2%80%B3N%0974%C2%B0%200'%2021.5022%22W");
 		
 		//bind the WebElement to the current page
 		PageFactory.initElements(browser, this);
 		assertTrue(resultTableBody.findElement(By.cssSelector("td:nth-child(1)")).getText().contains("40°26′47″N"));
 		assertTrue(resultTableBody.findElement(By.cssSelector("td:nth-child(1)")).getText().contains("74° 0' 21.5022"));
 		assertEquals("40.4463889", resultTableBody.findElement(By.cssSelector("td:nth-child(2)")).getText());
 		assertEquals("-74.0059728", resultTableBody.findElement(By.cssSelector("td:nth-child(3)")).getText());
 	}
 	
 	@Test
 	public void testDatesPage() {
 		browser.get(TESTING_SERVER_URL + "dates");
 		
 		//bind the WebElement to the current page
 		PageFactory.initElements(browser, this);
 		
 		dataTextArea.sendKeys("2 VII 1986");
 		submit.click();
 		
 		assertEquals("1986", resultTableBody.findElement(By.cssSelector("td:nth-child(2)")).getText());
 		assertEquals("7", resultTableBody.findElement(By.cssSelector("td:nth-child(3)")).getText());
 		assertEquals("2", resultTableBody.findElement(By.cssSelector("td:nth-child(4)")).getText());
 		assertEquals("1986-07-02", resultTableBody.findElement(By.cssSelector("td:nth-child(5)")).getText());
 	}
 	 
 	@After
 	public void tearDown() {
 		browser.close();
 	}
 }
