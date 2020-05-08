 import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
 import models.Event;
 import net.sourceforge.jwebunit.htmlunit.HtmlUnitTestingEngineImpl;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.List;
 
 public class ApplicationTest extends BaseFunctionalTest
 {
    @Before
    public void deleteEvents()
    {
       Event.deleteAll();
    }
    @Test
    public void testIndex()
    {
       wt.beginAt(getRoute("Application.index"));
       wt.assertElementPresent("createEvent");
      assertEquals(wt.getElementById("error").getTextContent(), "");
      assertEquals(wt.getElementById("success").getTextContent(), "");
    }
    @Test
    public void testAddEventBlankTitle() throws InterruptedException
    {
       wt.beginAt(getRoute("Application.index"));
       wt.setTextField("title", "");
       wt.clickButton("createEvent");
 
       Thread.sleep(2000);  //  <--- Required since you have to wait for the round trip !!
 
       wt.assertTextInElement("error", "Required");
      assertEquals(wt.getElementById("success").getTextContent(), "");
    }
    @Test
    public void testAddEventAjaxAsync() throws InterruptedException
    {
       wt.beginAt(getRoute("Application.index"));
       wt.setTextField("title", "My New Event");
       wt.clickButton("createEvent");
 
       Thread.sleep(2000);  //  <--- Required since you have to wait for the round trip !!
 
      assertEquals(wt.getElementById("error").getTextContent(), "");
       wt.assertTextInElement("success", "Created Event with Id:");
       assertEquals(1, Event.count());
    }
    @Test
    public void testAddEventAjaxSync()
    {
       wt.beginAt(getRoute("Application.index"));
 
       // This will make the ajax call synchronous - no more Thread.sleep() !
       if (wt.getTestingEngine() instanceof HtmlUnitTestingEngineImpl)
       {
          ((HtmlUnitTestingEngineImpl) wt.getTestingEngine()).getWebClient().setAjaxController(new NicelyResynchronizingAjaxController());
       }
 
       wt.setTextField("title", "My New Event Title");
       wt.clickButton("createEvent");                  //  <--- ajax call becomes synchronous
      assertEquals(wt.getElementById("error").getTextContent(), "");
       assertEquals(1, Event.count());
       List<Event> events = Event.findAll();
       wt.assertTextInElement("success", "Created Event with Id:" + events.get(0).id);
    }
 }
