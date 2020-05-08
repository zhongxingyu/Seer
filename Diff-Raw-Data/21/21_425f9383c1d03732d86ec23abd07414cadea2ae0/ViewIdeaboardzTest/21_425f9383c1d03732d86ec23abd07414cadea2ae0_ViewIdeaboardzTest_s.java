 package functional.com.thoughtworks.twu;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
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
 
 public class ViewIdeaboardzTest {
 
     private TestHelper testHelper;
 
 
     @Before
     public void setUp() {
         testHelper = new TestHelper(FirefoxPreference.ANDROID_FIREFOX_PREFERENCE);
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
         menuIcons.add(testHelper.findElement(By.id("sectionsBtn")));
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
