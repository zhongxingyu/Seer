 package net.canadensys.dataportal.vascan;
 
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
  * Integration tests of the rendered name page
  * @author canadensys
  *
  */
 public class NamePageIntegrationTest extends AbstractIntegrationTest{
 	
 	
 	@FindBy(css = "div#content")
 	private WebElement contentDiv;
 	
 	//make sure we find the footer since this is the last processed element
 	@FindBy(css = "div#footer")
 	private WebElement footerDiv;
 		
 	@Before
 	public void setup() {
 		browser = new FirefoxDriver();
 	}
 	
 	@Test
 	public void testNameVernacularPage() {
 		//Coordinates is the landing page
 		browser.get(TESTING_SERVER_URL + "name/buis");
 		
 		//bind the WebElement to the current page
 		PageFactory.initElements(browser, this);
 		
 		assertEquals("Buis is a vernacular name for:",contentDiv.findElement(By.cssSelector("h4")).getText());
 
 		assertEquals("div",footerDiv.getTagName());
 	}
 	
 	@Test
 	public void testNameTaxonPage(){
 		browser.get(TESTING_SERVER_URL + "name/Taxus canadensis");
 		//bind the WebElement to the current page
 		PageFactory.initElements(browser, this);
 		
 		assertEquals("div",footerDiv.getTagName());
 	}
 	
 	@Test
 	public void testNameDisambiguationPage(){
 		browser.get(TESTING_SERVER_URL + "name/cosmos");
 		//bind the WebElement to the current page
 		PageFactory.initElements(browser, this);
 		
 		assertEquals("div",footerDiv.getTagName());
 	}
 	
 	/**
 	 * This test also ensure the name pathVariable is interpreted correctly
 	 */
 	@Test
 	public void testNameSynonymWarningPage(){
 		browser.get(TESTING_SERVER_URL + "name/Carex arctata var. faxoni");
 		//bind the WebElement to the current page
 		PageFactory.initElements(browser, this);
 		
 		assertEquals("warning round",contentDiv.findElement(By.cssSelector("h4")).getAttribute("class"));
 		assertEquals("div",footerDiv.getTagName());
 	}
 	
 	@After
 	public void tearDown() {
 		browser.close();
 	}
 
 }
