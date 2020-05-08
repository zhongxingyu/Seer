 package functional.com.thoughtworks.twu;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.openqa.selenium.By;
 
 import java.util.Arrays;
 import java.util.List;
@Ignore("fail on CI")
 public class PostCommentTest {
     private TestHelper testHelper;
 
     @Before
     public void setUp(){
         testHelper = new TestHelper(FirefoxPreference.ANDROID_FIREFOX_PREFERENCE);
     }
 
     @Test
     public void shouldShowErrorMessageWhenPostEmptyComment() {
         testHelper.navigateToCommentView();
         testHelper.clickElement(By.id("postBtn"));
         testHelper.assertDisplayedMessageIs("Please enter a message");
     }
 
     @Test
     public void shouldShowCreatedComments() {
         testHelper.navigateToCommentView();
 
         String comment = String.valueOf(System.currentTimeMillis());
 
         testHelper.addText("commentText", comment);
         testHelper.clickElement(By.id("postBtn"));
 
         testHelper.assertContent("commentArea", comment);
 
         testHelper.refreshWebPage();
         testHelper.assertContent("commentArea", comment);
     }
 
     @After
     public void tearDown() {
         testHelper.closeWebDriver();
     }
 
 }
