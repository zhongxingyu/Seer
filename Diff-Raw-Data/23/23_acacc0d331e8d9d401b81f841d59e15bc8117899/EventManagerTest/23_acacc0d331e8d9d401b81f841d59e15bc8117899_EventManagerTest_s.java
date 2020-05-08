 package hibernateManagers;
 
 import static org.junit.Assert.*;
 import hibernateEntities.User;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.cfg.Configuration;
 import org.junit.Before;
 import org.junit.Test;
 /**
  * @author Zhe Wei
  *
  */
 public class EventManagerTest {
 
 	@Before
 	public void setUp() throws Exception {
 		Configuration config = new AnnotationConfiguration();
 		config.configure(new File(
 				".\\test\\testSrc\\resources\\hibernate.cfg.xml"));
 		SessionFactory factory = config.buildSessionFactory();
 		Session hibernateSession = factory.openSession();
 
 		ConnectionManager.setFactory(factory); 
 		// cannot be changed to ConnectionManagerV2, no such method
 	}
 	@Test
 	public void testGetEventsFirst() {
 		Map<Integer,ArrayList<String>> result = EventManager.getEvents();
 		
 		// test the first record (event_id=1)
 		assertEquals("2013-01-07",result.get(new Integer(1)).get(0));
 		assertEquals("Hope Fund Raising",result.get(new Integer(1)).get(1));
 		assertEquals("London",result.get(new Integer(1)).get(2));
 		assertEquals("09:00:00",result.get(new Integer(1)).get(3));
 		assertEquals("Fund Raising",result.get(new Integer(1)).get(4));
 		
 	}	
 	
 	@Test
 	public void testGetEventsSecond() {
 		Map<Integer,ArrayList<String>> result = EventManager.getEvents();
 		
 		// test the second record (event_id=4)
 		assertEquals("2013-02-17",result.get(new Integer(4)).get(0));
 		assertEquals("Hope Carnival Party",result.get(new Integer(4)).get(1));
 		assertEquals("London",result.get(new Integer(4)).get(2));
 		assertEquals("19:00:00",result.get(new Integer(4)).get(3));
 		assertEquals("",result.get(new Integer(4)).get(4));
 		
 	}	
 	
 	@Test
 	public void testGetEventsThird() {
 		Map<Integer,ArrayList<String>> result = EventManager.getEvents();
 		
 		// test the third record (event_id=6)
 		assertEquals("2013-02-14",result.get(new Integer(6)).get(0));
 		assertEquals("Hope Valentines Party",result.get(new Integer(6)).get(1));
 		assertEquals("London",result.get(new Integer(6)).get(2));
 		assertEquals("18:00:00",result.get(new Integer(6)).get(3));
 		assertEquals("",result.get(new Integer(6)).get(4));
 		
 	}	
 
 }
