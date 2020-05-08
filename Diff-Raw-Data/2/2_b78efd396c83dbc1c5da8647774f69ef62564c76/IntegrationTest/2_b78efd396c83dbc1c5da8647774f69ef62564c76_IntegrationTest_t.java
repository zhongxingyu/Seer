 package test;
 
 import org.junit.Test;
 import play.test.TestBrowser;
 import play.libs.F.Callback;
 import test.pages.IndexPage;
 import test.pages.LoginPage;
 import test.pages.ResultPage;
 import static play.test.Helpers.HTMLUNIT;
 import static play.test.Helpers.inMemoryDatabase;
 import static play.test.Helpers.fakeApplication;
 import static play.test.Helpers.testServer;
 import static play.test.Helpers.running;
 import static org.fest.assertions.Assertions.assertThat;
 
 /**
  * Integration tests running on an instance of the application.
  */
 public class IntegrationTest {
   /** The port number for the integration test. */
   private static final int PORT = 3333;
 
   /**
    * Check to see that the two pages can be displayed.
    */
   @Test
   public void testBasicRetrieval() {
     running(testServer(PORT, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
       public void invoke(TestBrowser browser) {
         IndexPage indexPage = new IndexPage(browser.getDriver(), PORT);
         browser.goTo(indexPage);
         indexPage.isAt();
       }
     });
   }
   
   /**
    * Check to see that a search submission goes to Results page.
    */
   @Test
   public void testSearchButtonGoesToResults() {
     running(testServer(PORT, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
       public void invoke(TestBrowser browser) {
         IndexPage indexPage = new IndexPage(browser.getDriver(), PORT);
         browser.goTo(indexPage);
         indexPage.isAt();
         indexPage.clickSearchButton();
         ResultPage resultPage = new ResultPage(browser.getDriver(), PORT);
         resultPage.isAt();
       }
     });
   }
   
   /**
    * Check to see that a search submission goes to Results page.
    */
   @Test
   public void testLogin() {
     running(testServer(PORT, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
       public void invoke(TestBrowser browser) {
         IndexPage indexPage = new IndexPage(browser.getDriver(), PORT);
         browser.goTo(indexPage);
         indexPage.isAt();
         indexPage.goToLogin();
        LoginPage loginPage = new LoginPage(browser.getDriver(), PORT);
        loginPage.isAt();
       }
     });
   }
 
 }
