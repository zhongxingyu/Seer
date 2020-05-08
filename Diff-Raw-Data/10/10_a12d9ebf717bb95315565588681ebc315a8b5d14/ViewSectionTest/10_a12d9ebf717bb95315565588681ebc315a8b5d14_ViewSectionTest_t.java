 package functional.com.thoughtworks.twu;
 
 import org.junit.After;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import javax.annotation.Nullable;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 @RunWith(Parameterized.class)
 public class ViewSectionTest {
     public static final String SECTION_ID = "1";
     public static final String SECTION_NAME = "Went Well";
     public static final By SECTION_HEADING_SELECTOR = By.id("sectionName");
 
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
 
     public ViewSectionTest(FirefoxPreference firefoxPreference) {
         this.firefoxPreference = firefoxPreference;
     }
 
     @Before
     public void setUp() {
         testHelper = new TestHelper(this.firefoxPreference);
     }
 
     @Test
     public void shouldDisplaySectionNameWhenGoingThroughBoardPage() {
         navigateToSectionPage();
 
         WebElement sectionHeading = testHelper.findElement(SECTION_HEADING_SELECTOR);
         assertEquals(SECTION_NAME, sectionHeading.getText());
     }
 
     @Test
     public void shouldDisplaySectionNameWhenGoingDirectlyToSectionPage() {
         goDirectlyToSectionPage();
 
         WebElement sectionHeading = testHelper.findElement(SECTION_HEADING_SELECTOR);
         assertEquals(SECTION_NAME, sectionHeading.getText());
     }
 
     @After
     public void tearDown() {
         testHelper.closeWebDriver();
     }
 
     private void goDirectlyToSectionPage() {
         testHelper.navigateToSectionView(SECTION_ID);
         testHelper.waitForElement(SECTION_HEADING_SELECTOR);
     }
 
     private void navigateToSectionPage() {
         testHelper.navigateToMainBoardView();
         testHelper.findElement(By.linkText(SECTION_NAME)).click();
         testHelper.waitForElement(SECTION_HEADING_SELECTOR);
     }
 
 }
 
