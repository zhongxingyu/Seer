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
  * Integration tests of the rendered taxon page
  * @author canadensys
  *
  */
 public class TaxonPageIntegrationTest extends AbstractIntegrationTest {
 	
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
 	public void testTaxonPage() {
 		//Coordinates is the landing page
 		browser.get(TESTING_SERVER_URL + "taxon/73");
 		
 		WebElement classificationTable = browser.findElement(By.cssSelector("h2+table.custom_table"));
 		//bind the WebElement to the current page
 		PageFactory.initElements(browser, this);
 		assertEquals("Equisetopsida C. Aghard",contentDiv.findElement(By.cssSelector("h1")).getText());
		assertEquals("accepted",contentDiv.findElement(By.cssSelector("h1 + p")).getAttribute("class"));
 		
 		
 		WebElement firstClassificationRow = classificationTable.findElement(By.cssSelector("tbody tr"));
 		assertEquals("selected",firstClassificationRow.getAttribute("class"));
 		assertEquals("Equisetopsida", firstClassificationRow.findElement(By.cssSelector(":nth-child(2)")).getText());
 		
 		assertEquals("div",footerDiv.getTagName());
 	}
 	
 	@Test
 	public void testSynonymPage(){
 		//get a synonym
 		browser.get(TESTING_SERVER_URL + "taxon/15428");
 		
 		PageFactory.initElements(browser, this);
 		
 		assertEquals("Carex abdita Bicknell", contentDiv.findElement(By.cssSelector("h1")).getText());
		assertEquals("synonym",contentDiv.findElement(By.cssSelector("h1 + p")).getAttribute("class"));
 		
		assertEquals("Carex umbellata Schkuhr ex Willdenow", contentDiv.findElement(By.cssSelector("p.redirect_accepted a")).getText());
 		
 		assertEquals("div",footerDiv.getTagName());		
 	}
 	
 	@Test
 	public void testHybridPage(){
 		//http://data.canadensys.net/vascan/taxon/5482
 	}
 	 
 	@After
 	public void tearDown() {
 		browser.close();
 	}
 }
