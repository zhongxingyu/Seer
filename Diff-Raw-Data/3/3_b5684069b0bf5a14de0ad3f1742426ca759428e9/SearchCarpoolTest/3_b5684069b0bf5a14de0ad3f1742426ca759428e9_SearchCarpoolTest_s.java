 package smartpool.functional;
 
 import org.junit.Before;
 import org.junit.Test;
 import smartpool.functional.page.HomePage;
 import smartpool.functional.page.LoginPage;
 
 public class SearchCarpoolTest extends BaseTest{
 
     private HomePage homePage;
 
     @Override
     public void setUp() {
         super.setUp();
         homePage = new LoginPage(webDriver).login();
     }
 
     @Test
     public void searchCarpoolAndVerifyResultCount() {
         homePage.enterSearchQuery("diamond district")
                 .verifyResultCount("1 result");
     }
 
     @Test
     public void searchCarpoolWithUnknownPickupPoint() {
         homePage.enterSearchQuery("Area 51")
                 .verifyResultCount("0 result");
     }
 
     @Test
     public void searchFromRoutePointsOptionBox() {
         homePage.enterSearchQuery("")
                 .selectLocationFromRoutePointList("Sony Centre")
                 .verifyResultCount("2 result");
     }
 }
