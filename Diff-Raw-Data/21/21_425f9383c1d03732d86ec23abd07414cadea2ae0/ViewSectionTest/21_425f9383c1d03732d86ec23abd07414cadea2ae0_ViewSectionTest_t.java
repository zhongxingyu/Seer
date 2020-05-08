 package functional.com.thoughtworks.twu;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class ViewSectionTest {
     public static final String SECTION_ID = "1";
     public static final String SECTION_NAME = "Went Well";
     public static final By SECTION_HEADING_SELECTOR = By.id("sectionName");
 
     private TestHelper testHelper;
 
     @Before
     public void setUp() {
         testHelper = new TestHelper(FirefoxPreference.ANDROID_FIREFOX_PREFERENCE);
     }
 
     @Test
     public void shouldDisplaySectionNameWhenGoingThroughBoardPage() {
         navigateToSectionPage(SECTION_NAME);
 
         WebElement sectionHeading = testHelper.findElement(SECTION_HEADING_SELECTOR);
         assertThat(sectionHeading.getText(), is(SECTION_NAME));
     }
 
     @Test
     public void shouldDisplaySectionNameWhenGoingDirectlyToSectionPage() {
         goDirectlyToSectionPage(SECTION_ID);
 
         WebElement sectionHeading = testHelper.findElement(SECTION_HEADING_SELECTOR);
         assertThat(sectionHeading.getText(), is(SECTION_NAME));
     }
 
     @Test
     public void shouldDisplayNoticeIfSectionIsEmpty() {
         navigateToSectionPage("Action Items");
         testHelper.assertContent("emptyMessage", "Got any ideas?");
     }
 
     @Test
     public void shouldDisplayErrorMessageIfProvideInvalidSectionId(){
        testHelper.navigateToUrl(TestHelper.BOARD_URL + "/" + "999999");
         testHelper.waitForElement(By.id("alert-area"));
         assertTrue(testHelper.contains("No such section exists"));
     }
 
     @After
     public void tearDown() {
         testHelper.closeWebDriver();
     }
 
     private void goDirectlyToSectionPage(String sectionId) {
         testHelper.navigateToSectionView(sectionId);
         testHelper.waitForElement(SECTION_HEADING_SELECTOR);
     }
 
     private void navigateToSectionPage(String sectionName) {
         testHelper.navigateToMainBoardView();
         testHelper.findElement(By.linkText(sectionName)).click();
         testHelper.waitForElement(SECTION_HEADING_SELECTOR);
     }
 
 }
 
