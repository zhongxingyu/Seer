 package functional.com.thoughtworks.twu;
 
 import functional.com.thoughtworks.twu.utils.BaseFunctionalTest;
 import functional.com.thoughtworks.twu.utils.FeedbacksPage;
 import functional.com.thoughtworks.twu.utils.Talk;
 import org.junit.internal.matchers.StringContains;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.UUID;
 
 import static com.thoughtworks.twu.utils.WaitHelper.waitForElement;
 import static org.hamcrest.CoreMatchers.is;
 import static org.joda.time.DateTime.now;
 import static org.junit.Assert.assertThat;
 import static org.testng.AssertJUnit.assertTrue;
 
 public class FeedbackFunctionalTest extends BaseFunctionalTest {
     private FeedbacksPage feedbacksPage;
     private Talk talk;
     String title;
 
     @BeforeMethod
     public void setUp() {
         talk = new Talk(webDriver);
         title = UUID.randomUUID().toString();
         talk.newTalk(title, "Seven wise men", "Ajanta Ellora", "28/07/2012", "11:42 AM");
         talk.assertCreationSuccess();
         feedbacksPage = new FeedbacksPage(webDriver);
     }
 
     @Test
     public void shouldNotBeAbleToSubmitBlankFeedbackOnTalk() throws Exception {
 
         WebElement talkLink = (new WebDriverWait(webDriver, 10)).until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(title)));
         talkLink.click();
         waitForElement(webDriver, "start_of_feedback_list");
         assertTrue(webDriver.getPageSource().contains("Past Feedback"));
         int countInitial = feedbacksPage.countNoOfFeedbacks();
         feedbacksPage.submitFeedback("");
         int countNewFeedbacks = feedbacksPage.countNoOfFeedbacks() - countInitial;
         assertThat(countNewFeedbacks, is(0));
     }
 
     @Test
     public void shouldBeAbleToEnterFeedbackOnTalk() throws InterruptedException {
 
         WebElement talkLink = (new WebDriverWait(webDriver, 10)).until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(title)));
         talkLink.click();
         waitForElement(webDriver, "start_of_feedback_list");
         assertTrue(webDriver.getPageSource().contains("Past Feedback"));
         int countInitial = feedbacksPage.countNoOfFeedbacks();
         String feedbackCreationTime = now().toString();
         feedbacksPage.submitFeedback(feedbackCreationTime);
         int countNewFeedbacks = feedbacksPage.countNoOfFeedbacks() - countInitial;
         assertThat(countNewFeedbacks, is(1));
         assertThat(webDriver.getPageSource(), StringContains.containsString(feedbackCreationTime));
     }
 
 }
