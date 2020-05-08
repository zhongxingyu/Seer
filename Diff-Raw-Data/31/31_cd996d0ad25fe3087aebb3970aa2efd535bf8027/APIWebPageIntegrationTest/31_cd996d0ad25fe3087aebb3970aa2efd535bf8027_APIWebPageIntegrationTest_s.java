 package net.canadensys.api.narwhal;
 
 import static org.junit.Assert.assertEquals;
 
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
 				
 		assertEquals("40.4463889", resultTableBody.findElement(By.cssSelector("td:nth-child(3)")).getText());
 		assertEquals("-74.0059728", resultTableBody.findElement(By.cssSelector("td:nth-child(4)")).getText());
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
