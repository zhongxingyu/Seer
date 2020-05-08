 package test;
 
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.Keys;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.PageFactory;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.SlowLoadableComponent;
 import org.openqa.selenium.support.ui.SystemClock;
 import org.openqa.selenium.support.ui.Wait;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import static test.UtilityClass.*;
 
 public class GoogleSearchPage extends SlowLoadableComponent<GoogleSearchPage> {
 	
 	private static int timeOutInSeconds = 3;
 	private GSPFluentInterface gspfi;
 
     @FindBy(id = "gbqfq") private WebElement searchField;
 	@FindBy(id = "gbqfb") private WebElement searchButton;
 
 	public GoogleSearchPage() {
         super( new SystemClock(), timeOutInSeconds);
         gspfi = new GSPFluentInterface( this );
 		System.out.println("Loaded Google Search Page");
 		this.get(); //calls load and isLoaded
 		PageFactory.initElements(driver, this); 
     }
 	
 	public GSPFluentInterface withFluent()
     {
 		System.out.println("Returning fluent page object.");
         return gspfi; 
     }
 	
 	@Override
 	protected void isLoaded() throws Error {
 		System.out.println("Calling GoogleSearchPage.isLoaded...");
 		final WebElement coreField = (new WebDriverWait(driver, 10))
 				.until(new ExpectedCondition<WebElement>(){
 					public WebElement apply(WebDriver d) {
 						return d.findElement( By.id("gbqfq") );
 					}});
 		if ( coreField.isDisplayed() )
 		{
 			System.out.println("Google search page is loaded.\nWill initialize page object.");
 		} else {
 			System.out.println("Google search page is not yet loaded." );
 		} 
 	}
 	
 	@Override
 	protected void load() {
 		System.out.println("Calling GoogleSearchPage.load...");
 		Wait<WebDriver> wait = new WebDriverWait(driver, 30); 
 		if ( driver.findElement( By.id("//div[@id='navBar']//div[1]")).getAttribute("onclick") == null )
 		{			
 			WebElement defnav = wait.until( visibilityOfElementLocated( By.id("gbqfq") ) );
 			defnav.click();
 			WebElement adnav = wait.until( visibilityOfElementLocated( By.id("gbqfq") ) );
 			adnav.click();			
 		} else {
 			WebElement adnav = wait.until( visibilityOfElementLocated( By.id("gbqfq") ) );
 			adnav.click();
 		}
 	}
 	
 	/**
 	 *  This next section are the page object actions
 	 * 
 	 */
 	
 	public void setSearchString( String sstr ) {
 		clearAndType( searchField, sstr );
 	}
 	
 	public void clickSearchField() {
 		searchField.click();
 	}
 	
 	public void clickSearchButton() {
 		searchButton.click();
 	}
 	
 	public void selectInGoogleDropdown( String match )
 	{
 		WebElement dd = driver.findElement( By.xpath( "//input[@id='gbqfq']" ) );
 		waitTimer(4, 500);
 		long end = System.currentTimeMillis() + 5000;
 		while ( System.currentTimeMillis() < end ) {
 			WebElement resultsLi = driver.findElement( By.xpath( "//div[@class='gsq_a']" ) );
 			if ( resultsLi.isDisplayed() ) break;
 		}
 		int matchedPosition = 0;
 		int optpos = 0;
 		List<WebElement> allSuggestions = driver.findElements( By.xpath( "//div[@class='gsq_a']/table/tbody/tr/td/span" ) );        
 		for ( WebElement suggestion : allSuggestions ) {
 			if ( suggestion.getText().contains( match ) ) {
 				matchedPosition = optpos;
 			} else {
 				// do nothing
 			}
 			optpos++;
 		}
 		for ( int i= 0; i < matchedPosition ; i++ ) {
 			dd.sendKeys( Keys.ARROW_DOWN );
 			System.out.println("...key down");
 			waitTimer(1, 500); // to slow down the arrow key so you can see it
 		}
 		WebElement targetItem = allSuggestions.get( matchedPosition );
 		targetItem.click();
		waitTimer(1, 500);
 	}
 	
 	/**
 	 *  Fluent interface inner class
 	 * 
 	 */
 	
 	public class GSPFluentInterface {
         
 		private GoogleSearchPage gsp;
 		
 		public GSPFluentInterface(GoogleSearchPage googleSearchPage) {
 			gsp = googleSearchPage;
 		}
 
 		public GSPFluentInterface setSearchString( String sstr ) {
 			clearAndType( gsp.searchField, sstr );
 			return this;
 		}
 		
 		public GSPFluentInterface clickSearchField() {
 			gsp.searchField.click();
 			return this;
 		}
 		
 		public GSPFluentInterface clickSearchButton() {
 			gsp.searchButton.click();
 			return this;
 		}
 		
 		public GSPFluentInterface waitForTime( int units, int ms ) {
 			waitTimer( units, ms );
 			return this;
 		}
 		
		public GSPFluentInterface selectItem( String ele ) {
 			System.out.println("Selecting item in list using fluent API.");
			selectInGoogleDropdown( ele );
 			return this;
		}		
		public GSPFluentInterface clickLogo( String id ) {
			System.out.println("Click Google logo using fluent API.");
			clickElementWithJSE( id );
			return this;
		}
 
 	}	
     
 }
