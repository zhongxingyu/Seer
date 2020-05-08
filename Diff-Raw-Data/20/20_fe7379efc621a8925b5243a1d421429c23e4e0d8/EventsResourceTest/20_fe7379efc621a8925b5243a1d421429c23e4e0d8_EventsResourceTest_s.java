 package se.cygni.expenses.resources;
 
 import com.sun.jersey.api.client.GenericType;
 import com.yammer.dropwizard.testing.ResourceTest;
 import org.junit.Test;
 import se.cygni.expenses.api.Event;
 import se.cygni.expenses.jdbi.EventsRepository;
 
 import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
 
 public class EventsResourceTest extends ResourceTest {
 
     private EventsRepository eventsRepository;
 
     @Override
     protected void setUpResources() throws Exception {
         eventsRepository = mock(EventsRepository.class);
         addResource(new EventsResource(eventsRepository));
     }
 
     @Test
     public void shouldReturnActionLinks() {
         //given
 
         //when
         String result = client().resource("/").get(String.class);
 
         //then
         assertThat("Result contains links element", result, is("{\"links\":\"/listEvents\"}"));
 
     }
 
     @Test
     public void shouldListEvents() {
         // given
 
         when(eventsRepository.findAll()).thenReturn(Arrays.asList(new Event(0, "Trip", new Date(0))));
 
         // when
         List<Event> result = client().resource("/listEvents").get(new GenericType<List<Event>>() {});
 
         //then
         assertThat("Result should contain one element", result.size(), is(1));
         assertThat("First element should have Trip as name", result.get(0).getName(), is("Trip"));
     }
 
     @Test
     public void shouldAddEvent() {
         //given
        Event event = new Event(1, "Big bird", new Date());
 
         //when
         client().resource("/addEvent").type(MediaType.APPLICATION_JSON_TYPE).put(Event.class, event);
 
         //then
         verify(eventsRepository).add(event);
     }
 }
