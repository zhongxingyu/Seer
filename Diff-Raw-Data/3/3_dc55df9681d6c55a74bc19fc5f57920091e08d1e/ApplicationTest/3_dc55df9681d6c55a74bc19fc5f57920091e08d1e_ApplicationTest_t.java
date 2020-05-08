 import models.Note;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import play.data.Form;
 import play.mvc.Content;
 import play.test.FakeApplication;
 
 import java.io.IOException;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static play.test.Helpers.*;
 
 public class ApplicationTest {
 
     private FakeApplication app;
 
     @Before
     public void startApp() throws IOException {
         app = fakeApplication(inMemoryDatabase());
         start(app);
     }
 
     @After
     public void stopApp() {
         stop(app);
     }
 
     @Test
     public void testInitialData() {
 
         // Should be 4 notes in DB
         assertEquals(4, Note.find.findRowCount());
 
         // Should have note with text "The first note"
         Note myNote = Note.find.where().eq("title", "Note title 1").findUnique();
         assertNotNull(myNote);
     }
 
     @Test
     public void renderFrontPage() {
 
         Form<Note> noteForm = Form.form(Note.class);
        Form<String> searchForm = Form.form(String.class);
        Content html = views.html.index.render(Note.all(), noteForm, searchForm);
 
         assertThat(contentType(html)).isEqualTo("text/html");
         assertThat(contentAsString(html)).contains("The first note");
     }
 }
