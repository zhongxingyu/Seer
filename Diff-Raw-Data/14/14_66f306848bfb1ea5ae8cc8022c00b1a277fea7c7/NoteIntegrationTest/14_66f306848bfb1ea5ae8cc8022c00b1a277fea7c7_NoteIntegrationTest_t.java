 import models.*;
 
 import org.junit.*;
 
 import play.test.*;
 import play.libs.F.*;
 import play.Logger;
 
 import static play.test.Helpers.*;
 import static org.fest.assertions.Assertions.*;
 
 import static org.fluentlenium.core.filter.FilterConstructor.*;
 
 public class NoteIntegrationTest extends IntegrationTest {
 
   @Test
   public void testNoteCreator() {
     running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
             public void invoke(TestBrowser browser) {
                 login(browser);
                 createNote(browser);
                 assertThat(browser.url()).isEqualTo(myUrl + "/note/11");
             }
         });     
   }
 
   /**
   * testDeleteNote not yet working
   */
   //@Test
   public void testDeleteNote() {
     running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
             public void invoke(TestBrowser browser) {
                 login(browser);
                 createNote(browser);
                 browser.$("#delete-note-button").click();
                 Logger.info("!!!!!!!!!!!!!!!!!!!!!!!" + browser.$("#message-wrapper").getText());
                 Logger.debug("##### SE HÄR MACKAN #####" + browser.url());
                 assertThat(browser.url()).isEqualTo(myUrl + "/notes/1");
             }
         });  
   }
 
   @Test
   public void testEditNote() {
     running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
             public void invoke(TestBrowser browser) {
                 login(browser);
                 createNote(browser);
                 browser.$("#edit-note-button").click();
                browser.$("#content").text("This is an edit");
                 browser.$("#update-note-button").click();
                assertThat(browser.pageSource()).contains("This is an edit");
             }
         });  
   }
 
   @Test
   public void testQuickScribble() {
     running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
             public void invoke(TestBrowser browser) {
                 login(browser);
                 browser.$("#sub-nav-toggle").click();
                 browser.$("#title").text("This is testQuickScribble");
                 browser.$("#content").text("testing testing");
                 browser.$("#quick-create-note-button").click();
                 assertThat(browser.url()).isEqualTo(myUrl + "/note/11");
             }
         });  
   }
 
   @Test
   public void testUpVote() {
     running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
             public void invoke(TestBrowser browser) {
                 login(browser);
                browser.$("a", withText("Being awesome")).click();
                 browser.$("#upvote-button").click();
                 assertThat(browser.$("#userscore").getText()).isEqualTo("1");
             }
         });  
   }
 
   @Test
   public void testDownVote() {
     running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
             public void invoke(TestBrowser browser) {
                 login(browser);
                browser.$("a", withText("Being awesome")).click();
                 browser.$("#downvote-button").click();
                 assertThat(browser.$("#userscore").getText()).isEqualTo("-1");
             }
         });  
   }
 
   /**
   *Should only be able to vote a specific note once per user
   */
   @Test
   public void testVoteToggle() {
     running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT, new Callback<TestBrowser>() {
             public void invoke(TestBrowser browser) {
                 login(browser);
                browser.$("a", withText("Being awesome")).click();
                 browser.$("#upvote-button").click();
                 browser.$("#upvote-button").click();
                 assertThat(browser.$("#userscore").getText()).isEqualTo("0");
             }
         });  
   }
 
 }
