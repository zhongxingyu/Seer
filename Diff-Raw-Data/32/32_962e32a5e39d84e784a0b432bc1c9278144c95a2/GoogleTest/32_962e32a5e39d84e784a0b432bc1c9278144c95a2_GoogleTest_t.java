 package test;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import test.GoogleSearchPage;
 
 import static test.UtilityClass.*;
 
 public class GoogleTest {
 	
 	@BeforeClass
 	public static void prepareBrowser() {
 		initializeBrowser( "firefox" );  // either firefox or ie
 	}
 
 	@Test
 	public void testWithPageObject() {    	
 		driver.get("http://www.google.com");
 		GoogleSearchPage gs = new GoogleSearchPage();
 		gs.setSearchString( "iphone app" );
 		gs.selectInGoogleDropdown( "development" );  
 		gs.clickSearchButton();
 		waitTimer(3, 1000);
 		clickElementWithJSE( "gbqlt" ); //click Google logo
 		System.out.println("Done with normal test.");
 	}
 	
 	@Test
 	public void testFluentPageObject() {    	
		// inspired by http://randypatterson.com/2007/09/how-to-design-a-fluent-interface/
 		driver.get("http://www.google.com");
 		GoogleSearchPage gs = new GoogleSearchPage();
 		gs.withFluent()
 		    .clickSearchField()
 		    .setSearchString("iphone app")
 		    .waitForTime(3, 1000)
		    .selectItem( "development" )
		    .clickSearchButton()
		    .waitForTime(5, 1000)
	        .clickLogo( "gbqlt" ); //click Google logo
 		System.out.println("Done with fluent test.");
 	}
 	
 	@AfterClass
 	public static void tearDown() {
		waitTimer(10, 1000);
 		driver.close();
 		System.out.println("Finished tearDown.");
 	}
 
 }
