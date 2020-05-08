 import java.net.Proxy.Type;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import models.Booking;
 import models.Event;
 import models.User;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import play.mvc.Http.Response;
 import play.mvc.Http.StatusCode;
 import play.test.Fixtures;
 import play.test.FunctionalTest;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.reflect.TypeToken;
 
 public class ApplicationTest extends FunctionalTest {
 	
 	@Before
 	public void setUp() {
 	    Fixtures.deleteDatabase();
 	    Fixtures.loadModels("fixtures.yml");
 	}
 
     @Test
     public void testThatIndexPageWorks() {
         Response response = GET("/");
         assertIsOk(response);
         assertContentType("text/html", response);
         assertCharset(play.Play.defaultWebEncoding, response);
         
         assertContentMatch("Pommes", response);
         assertContentMatch("Schnitzel", response);
         assertContentMatch("Spaghetti", response);
         
         //assertContentMatch("Auflauf", response);
     }
     
     @Test
     public void testGetJsonEvent() {
     	// get event id
     	Event e = Event.all().first();
         Response response = GET("/event/" + e.id);
         
         assertIsOk(response);
         assertContentType("application/json", response);
         //assertCharset("utf-8", response);
 
         String content = getContent(response);
         Gson gson = new Gson();
         Event event = gson.fromJson(content, Event.class);
 
         assertEquals( event.title, "Pommes" );
         
     }
     
     @Test
     public void testPOSTJsonEvent() {
     	Event e = Event.all().first();
     	if (e != null) {
     		for (int i = 0; i< e.bookings.size();i++) {
     			e.bookings.get(i).event = null;
     		}
     	}
     	e.id = null;
     	Gson gson = new Gson();
     	String jsonEvent = gson.toJson(e);
     	
     	long eventCount = Event.count();
         Response response = POST("/event", "application/json", jsonEvent);
         
         assertEquals(eventCount + 1, Event.count());
         assertStatus(StatusCode.CREATED, response);
     }
     
     @Test
     public void testPOSTInvalidJsonEvent() {
     	Response response = POST("/event", "application/json", "invalid");
     	assertStatus(500, response);
     }
     
     @Test
     public void testUPDATEJsonEvent() {
     	Event e = Event.all().first();
     	if (e != null) {
     		for (int i = 0; i< e.bookings.size();i++) {
     			e.bookings.get(i).event = null;
     		}
     	}
     	Gson gson = new Gson();
     	String jsonEvent = gson.toJson(e);
     	
         Event newEvent = Event.all().from(1).first();
         Response response = PUT("/event/" + newEvent.id, "application/json", jsonEvent);
        newEvent = Event.findById(newEvent.id);
         
         assertStatus(204, response);
         assertEquals(newEvent.title, e.title);
         assertEquals(newEvent.details, e.details);
        assertEquals(newEvent.bookings, e.bookings);
         assertEquals(newEvent.date, e.date);
     }
     
     @Test
     public void testDELETEJsonEvent() {
     	Event e = Event.all().first();
     	long eventCount = Event.count();
     	
         Response response = DELETE("/event/" + e.id);
         
         assertEquals(eventCount - 1, Event.count());
         assertStatus(204, response);
     }
     
     
     @Test
     public void testBookings() {
     	assertEquals(2, User.count());
     	assertEquals(3, Event.count());
     	assertEquals(3, Booking.count());
     	
     	List<Booking> bookings = Booking.all().fetch();
     	
     	List<Event> events = Event.find("byTitle", "Pommes").fetch();
     	
     	for (Event event : events) {
     		assertEquals(1, event.bookings.size());
     		for (ListIterator<Booking> iter = event.bookings.listIterator(); iter.hasNext();) {
     			assertNotNull(iter.next().user);
     		}
 		}
     }
     
 }
