 package com.webs.signup;
 
 import static com.thoughtworks.selenium.grid.tools.ThreadSafeSeleniumSessionStorage.session;
 import static com.thoughtworks.selenium.grid.tools.ThreadSafeSeleniumSessionStorage.startSeleniumSession;
 
 import static org.testng.Assert.*;
 import cuke4duke.annotation.After;
 import cuke4duke.annotation.Before;
 import cuke4duke.annotation.I18n.EN.When;
 import cuke4duke.annotation.I18n.EN.Given;
 import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.Pending;
 
 public class EmailSignupsteps {
 	public static String seleniumHost;
 	public static int seleniumPort;
 	public static String browser;
 	public static String webSite;
 	public String currenturl;
 	private String enteredEmail;
 	
     @Before
     public void setUpClass() throws Exception {
     	seleniumHost="localhost";
     	seleniumPort=4444;
     	browser="*firefox";
     }
 
     @After
     public void tearDownClass() throws Exception {
     	session().stop();   	
     }
     
 	@Given ("^an url \"([^\"]*)\"$")
 	 public void openUrl(String url) {
 		this.currenturl=url;
 		int i = 0;
 		//ryo is going to take chance...new signup page will adhoc display three mode 30%/30%/30% where is another 10%...
 		while (i<10){
 			++i;
 			startSeleniumSession(seleniumHost,seleniumPort,browser,url);
 			session().setTimeout("120000");
 			session().open("/");
 			session().waitForPageToLoad("30000");
 			session().getEval("window.moveTo(-0,-1);");
 			session().windowMaximize();
 			if (session().isElementPresent("//div[contains(@class,'gwo_streamlined_signup with_type') and contains(@style,'display: block')]")){
 				break;				
 			}
 			session().stop();
 		}
 		/*debug code
 		startSeleniumSession(seleniumHost,seleniumPort,browser,url);
 		session().setTimeout("120000");
 		session().open("/");
 		session().waitForPageToLoad("30000");
 		session().getEval("window.moveTo(-0,-1);");
 		session().windowMaximize();*/
 	 }
 	
 	 @Given("^an url \"([^\"]*)\" and opt to just website type$")
 	  public void openurlOptToWebsiteType(String url) {
 		 this.currenturl=url;
 			int i = 0;
 			//ryo is going to take chance...new signup page will adhoc display three mode 30%/30%/30% where is another 10%...
 			while (i<10){
 				++i;
 				startSeleniumSession(seleniumHost,seleniumPort,browser,url);
 				session().setTimeout("120000");
 				session().open("/");
 				session().waitForPageToLoad("30000");
 				session().getEval("window.moveTo(-0,-1);");
 				session().windowMaximize();
 				if (session().isElementPresent("//div[contains(@id,'get_started_block') and contains(@style,'display: block')]")){
 					break;				
 				}
 				session().stop();
 			}
 	 }
 
 	
 	@Then ("^the following fields should appear on the home page \"([^\"]*)\",\"([^\"]*)\",option \"([^\"]*)\"$")
 	 public void signupwithouttypehidden(String emaillable, String passlabel, String type) {
 		assertEquals(emaillable,session().getText("ui=PortalPage::SignupEmailLabel()"));
 		assertEquals(passlabel,session().getText("ui=PortalPage::SignupPasswordLabel()"));
 		assertEquals(type,session().getText("ui=PortalPage::SignuptypeLabel()"));
 	}
 
 	@When("^I click on the green Get Started button$")
 		public void ClickonGetStarted() {
 		session().click("ui=PortalPage::streamlinegetStart()");
		
 	}
 	
 	 @When("^I click on the green Get Started button for original signup$")
 	 public void ClickonGetStartedjustwebstype() {
 		 session().click("ui=PortalPage::getStart()");
 	 }
 	
 	@Then("^I should remain on the home page$")
 		public void TheHomePageGetStartedGreyout() {
 		assertEquals(session().getLocation(),currenturl+"/");
 		//assert style attribute of the get start button
 		assertEquals("opacity: 0.5;",session().getAttribute("//div[contains(@class,'gwo_streamlined_signup') and contains(@style,'display: block')]/descendant::input[@id='streamlined_signup_button']@style"));
     }
 
 	@Then ("^a red X should appear on the right side of the Email field$")
      public void AssertredcrossEmail() {
 		//assert class attribute of email container
 		assertEquals("text_container invalid",session().getAttribute("//div[contains(@class,'gwo_streamlined_signup') and contains(@style,'display: block')]/descendant::div@class"));
 	}
 
 	 @When("^I enter in an \"([^\"]*)\"$")
 	 public void EnterEmail(String email) {
 		 this.enteredEmail=email;
 		 session().type("ui=PortalPage::SignupEmailText()", email);
 	 }
 	 
 	 @Then("^a ticker should appear on the right side of the Email field$")
 	 public void AssertgreencheckEmail() {
 			//assert class attribute of email container
 			assertEquals("text_container valid",session().getAttribute("//div[contains(@class,'gwo_streamlined_signup') and contains(@style,'display: block')]/descendant::div@class"));
 		}
 	 
 	 @When("^I enter password \"([^\"]*)\"$")
               public void EnterPassword(String password) {
 		 session().type("ui=PortalPage::SignupPasswordText()", password);
       }
 
 	 @Then("^I should be taken back to the home page$")
 	 		public void BackToHomePage() {
 		 	assertEquals(session().getLocation(),currenturl+"/");
      }
 
 	 @Then("^the fields should be blank$")
         	public void FieldsIsBlank() {
 		 session().waitForPageToLoad("30000");
 		 assertEquals(session().getText("ui=PortalPage::SignupEmailText()"),"");
 		 assertEquals(session().getText("ui=PortalPage::SignupPasswordText()"),"");
 	 }
 	 
 	 @Then("^I should be taken to the \"([^\"]*)\" page$")
 	 	public void NavigateToDashboard(String FreeSignuptitle) {
 		 session().waitForPageToLoad("30000");
 		 assertEquals(session().getText("ui=NewSiteSignup::Signuptitle()"),FreeSignuptitle);
        }
 
 	 @Then("^I should be logged into the email address I just entered$")
 	 public void AssertEmailLoggedInSignupForm() {
 		 assertEquals(enteredEmail+". To use a different WebsID, please log out.", session().getText("//div[@id='step1']/descendant::span[@class='signup_bulletext']"));
 	 }
 
 	 @Then ("^I should see a message \"([^\"]*)\" bubble at the top$")
 	 	public void WebsIdCongratsMessage(String Message) {
 		 fail("this have not been implemented yet, put a fail statement for now");
 	 }
 	 
 	 @When("^I select \"([^\"]*)\" as Website type$")
 	 public void SelectWebsiteType(String webtype) {
 		 session().click("//div[contains(@style,'display: block;')]/form[contains(@id,'signup')]/descendant::div[@class='select_selected']");
 		 session().click("//div[contains(@style,'display: block;')]/form[contains(@id,'signup')]/descendant::ul[@class='select_ul']/li[contains(text(),'"+webtype+"')]");
 	 }
 
 	 @Then("^I should see \"([^\"]*)\" highlighted in Site Category$")
 
     public void AssertHighlightedSiteCategory(String webtype) {
 		 assertEquals(webtype, session().getText("//div[contains(@id,'site_categories_container')]/descendant::*/a[contains(@class,'selected_purpose')]"));
 	 }
 	 
 	 @Then("^I close the browser instant$")
 	 	public void CloseBrowserInstant() {
 		 session().stop();
 	 }
 
 	 
 }
