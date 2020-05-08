 package functional.com.thoughtworks.twu;
 
 import functional.com.thoughtworks.twu.utils.Cas;
 import functional.com.thoughtworks.twu.utils.Talk;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.internal.matchers.StringContains;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import java.util.UUID;
 
 import static org.junit.Assert.assertThat;
 
 public class ViewListOfMyTalksTest {
     public static final int HTTP_PORT = 9191;
     public static final String HTTP_BASE_URL = "http://localhost:" + HTTP_PORT + "/twu/home.html";
     public static final String TEST_USERNAME = "test.twu";
     WebDriver webDriver;
     Talk talk;
 
     @Before
     public void setUp() {
         webDriver = new FirefoxDriver();
         talk=new Talk(webDriver);
         webDriver.get(HTTP_BASE_URL);
         Cas.login(webDriver);
     }
 
     @Test
     public void shouldDisplayListOfTalksOnlyByLoggedInUser() throws Exception {
        String testTitle = "Title_" + UUID.randomUUID().toString();
         talk.newTalk(testTitle,"Seven wise men","Ajanta Ellora","28/09/2012","11:42 AM");
 
        String secondTestTitle = "Title_" + UUID.randomUUID().toString();
         talk.newTalk(secondTestTitle,"second description","second venue","28/09/2012","11:45 AM");
 
         talk.loadTalkDetails(testTitle);
 
         assertThat(talk.getHeaderDescription(), StringContains.containsString(TEST_USERNAME));
 
         talk.loadTalkDetails(secondTestTitle);
 
         assertThat(talk.getHeaderDescription(), StringContains.containsString(TEST_USERNAME));
 
     }
 
     @After
     public void tearDown() {
         Cas.logout(webDriver);
         webDriver.close();
     }
 }
