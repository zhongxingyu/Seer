 package net.canadensys.dataportal.vascan;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.PageFactory;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 /**
  * Integration tests of the rendered name page
  * @author canadensys
  *
  */
 public class SearchPageIntegrationTest extends AbstractIntegrationTest{
 
 	@FindBy(css = "div#content")
 	private WebElement contentDiv;
 	
 	@FindBy(css = "div#footer")
 	private WebElement footerDiv;
 
 	@FindBy(css = "input#search_term")
 	private WebElement searchInput;
 		
 	@Before
 	public void setup() {
 		browser = new FirefoxDriver();
 	}
 	
 	@Test
 	public void testSearchPage() {
 		//Coordinates is the landing page
 		browser.get(TESTING_SERVER_URL + "search/");
 		
 		//bind the WebElement to the current page
 		PageFactory.initElements(browser, this);
 		
 		assertEquals("Name search",contentDiv.findElement(By.cssSelector("h1")).getText());
 		assertEquals("div",footerDiv.getTagName());
 		
 		searchInput.click();
 		searchInput.sendKeys("carex");
 		WebElement searchDropdown = new WebDriverWait(browser, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span.tt-dropdown-menu")));
 		assertTrue(searchDropdown.getText().contains("Scientific Names"));
 		assertTrue(searchDropdown.getText().contains("Vernacular Names"));
 		assertTrue(searchDropdown.getText().contains("Carex feta"));
 		assertTrue(searchDropdown.getText().contains("carex noir"));
		
 		searchDropdown.findElement(By.cssSelector(".tt-suggestion:nth-of-type(4)")).click();
 		PageFactory.initElements(browser, this);
 		assertEquals("Carex alma",contentDiv.findElement(By.cssSelector("h1")).getText());
 	}
 	
 	@After
 	public void tearDown() {
 		browser.close();
 	}
 
 }
