 package functional.com.thoughtworks.twu;
 
 import functional.com.thoughtworks.twu.utils.BaseFunctionalTest;
 import functional.com.thoughtworks.twu.utils.Talk;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.UUID;
 
 public class ViewTalkDetailsFunctionalTest extends BaseFunctionalTest {
     Talk talk;
 
 
     @BeforeMethod
     public void setUp() {
         talk=new Talk(webDriver);
     }
 
     @Test
     public void shouldDisplayTalkDetailsOfCreatedTalk() throws Exception {
         String testTitle = "title_" + UUID.randomUUID().toString();
         talk.newTalk(testTitle, "test description", "test venue", "06/09/2012", "10:00 AM");
         talk.assertCreationSuccess();
         talk.clickTitle(testTitle);
 
         talk.assertHeaderMatch(testTitle, "test.twu");
 
         talk.expandDetails();
 
        talk.assertDetailsMatch("test description", "test venue", "06/09/2012", "10:00 AM", "test.twu@thoughtworks.com");
     }
 
 }
