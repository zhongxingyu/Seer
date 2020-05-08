 package no.niths.services;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
 
 import java.util.GregorianCalendar;
 
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.Event;
 import no.niths.domain.location.Location;
 import no.niths.services.interfaces.EventsService;
 import no.niths.services.interfaces.LocationService;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
 public class EventServiceTest {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(EventServiceTest.class);
 	
 	@Autowired
 	private EventsService eventService;
 	
 	@Autowired
 	private LocationService locService;
 	
 	@Test
 	public void testCRUD(){
 		int size = eventService.getAll(null).size();
 		Event e1 = new Event("the name xxx");
 		eventService.create(e1);
 		assertEquals(size + 1, eventService.getAll(null).size());
 		
 		Event temp = new Event("the name xxx");
 		assertEquals(1, eventService.getAll(temp).size());
 		
 		e1.setName("the new name xxx");
 		eventService.update(e1);
 		
 		assertEquals("the new name xxx", eventService.getById(e1.getId()).getName());
 		
 		eventService.hibernateDelete(e1.getId());
 		assertEquals(size, eventService.getAll(null).size());
 		
 		
 	}
 	
 
 	@Test 
 	public void testEventLocation(){
 		GregorianCalendar cal = new GregorianCalendar(2012, 11, 23, 22, 21, 23);
 		Event event = new Event("LUG Party", "Linux", cal, null);
 		Location loc = new Location("Oslo",10.2304,90.2030);
 		event.getLocation().add(loc);
 		
 		eventService.create(event);
 		Event temp = eventService.getById(event.getId());
 		assertEquals(event,temp);
 		
 		assertEquals(1, temp.getLocation().size());
 		assertEquals(loc, temp.getLocation().get(0));
 		
 		// update 
 		event.setEndTime(cal);	
 		eventService.update(event);
 		
 		temp = eventService.getById(event.getId());
 		
 		assertEquals(event.getEndTime(), temp.getEndTime());
 		assertEquals(1, temp.getLocation().size());
 		assertEquals(loc, temp.getLocation().get(0));
 		
		eventService.hibernateDelete(temp.getId());
		
		assertNull(eventService.getById(event.getId()));
		
 		assertEquals(1,locService.getAll(loc).size());
 		
 		locService.hibernateDelete(loc.getId());
 		assertEquals(0,locService.getAll(loc).size());
 	}
 }
