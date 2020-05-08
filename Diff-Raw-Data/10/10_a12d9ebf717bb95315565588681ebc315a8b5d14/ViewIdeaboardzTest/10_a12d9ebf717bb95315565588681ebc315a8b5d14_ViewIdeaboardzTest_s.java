 package functional.com.thoughtworks.twu;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 @RunWith(Parameterized.class)
 public class ViewIdeaboardzTest {
 
     private FirefoxPreference firefoxPreference;
     private TestHelper testHelper;
 
 
     @Parameterized.Parameters
     public static List<Object[]> firefoxPreferences() {
         return Arrays.asList(
                 new Object[][]{
                         {new FirefoxPreference("general.useragent.override", "Mozilla/5.0 (Android; Linux armv7l; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 Fennec/2.0.1")},
                         {new FirefoxPreference("general.useragent.override", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; da-dk) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5")}
                 }
         );
     }
 
     public ViewIdeaboardzTest(FirefoxPreference firefoxPreference) {
         this.firefoxPreference = firefoxPreference;
     }
 
     @Before
     public void setUp() {
         testHelper = new TestHelper(this.firefoxPreference);
 
     }
 
     @Test
     public void shouldDisplayNamesOfAllSectionsIfBoardURLValid() {
         List<String> sectionNameList = new ArrayList<String>();
         sectionNameList.add("Went Well");
         sectionNameList.add("Didn't Go Well");
         sectionNameList.add("Action Items");
 
         testHelper.navigateToMainBoardView();
 
         List<WebElement> sectionItems = testHelper.findElements();
 
         assertSectionNamesDisplayedAre(sectionNameList, sectionItems);
 
     }
 
     @Test
     public void shouldDisplayBoardName() {
         testHelper.navigateToMainBoardView();
 
         WebElement heading = testHelper.findElement(By.id("boardName"));
 
         assertEquals(TestHelper.BOARD_NAME, heading.getText());
     }
 
     @Test
     public void shouldCustomizeMenuLinksAccordingToTheBoardURL() {
         List<String> menuLinks = new ArrayList<String>();
         menuLinks.add(testHelper.BOARD_URL);
         menuLinks.add(testHelper.BOARD_URL + "/comment");
         menuLinks.add(testHelper.BOARD_URL + "/createIdea");
         testHelper.navigateToMainBoardView();
 
         List<WebElement> menuIcons = new ArrayList<WebElement>();
        menuIcons.add(testHelper.findElement(By.id("logo")));
         menuIcons.add(testHelper.findElement(By.id("commentBtn")));
         menuIcons.add(testHelper.findElement(By.id("createIdeaBtn")));
         assertMenuLinkCustomization(menuLinks, menuIcons);
     }
 
     @Test
     public void shouldShowErrorForInvalidBoardURL() {
         testHelper.navigateToUrl(testHelper.BOARD_URL + "99999abcde");
         testHelper.waitForElement(By.id("alert-area"));
         assertTrue(testHelper.contains("No such board exists"));
 
     }
 
     @After
     public void tearDown() {
         testHelper.closeWebDriver();
     }
 
     private void assertSectionNamesDisplayedAre(List<String> expected, List<WebElement> actual) {
         int index = 0;
         for (WebElement element : actual) {
             assertEquals(expected.get(index), element.getText());
             index++;
         }
 
     }
 
     private void assertMenuLinkCustomization(List<String> menuLinks, List<WebElement> menuIcons) {
         int index = 0;
         for (WebElement element : menuIcons) {
             assertEquals(menuLinks.get(index), element.getAttribute("href"));
             index++;
         }
     }
 
 }
