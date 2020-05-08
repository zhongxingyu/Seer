 package com.raccoonfink.cruisemonkey.server;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.io.File;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.raccoonfink.cruisemonkey.dao.EventDao;
 import com.raccoonfink.cruisemonkey.model.Event;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={
 		"classpath:/META-INF/spring/dao-context.xml",
 		"classpath:/META-INF/spring/server-context.xml"
 })
 @Transactional
 public class CalendarTest {
 	@Autowired
 	private EventDao m_eventDao;
 	
 	@Autowired
 	private OfficialCalendarVisitor m_visitor;
 	
 	@Before
 	public void setUp() {
 		/*
 		final HibernateEventDao hibernateEventDao = new HibernateEventDao();
 		m_eventDao = hibernateEventDao;
 		@SuppressWarnings("deprecation")
 		final SessionFactory sf = new Configuration().configure().buildSessionFactory();
 		hibernateEventDao.setSessionFactory(sf);
 		m_visitor = new OfficialCalendarVisitor();
 		m_visitor.setEventDao(m_eventDao);
 		*/
 	}
 
 	@Test
     public void testOfficialCalendarVisitor() throws Exception {
         // final URL url = new URL("https://www.google.com/calendar/ical/nh76o8dgn9d86b7n3p3uofg1q0%40group.calendar.google.com/public/basic.ics");
         final CalendarManager manager = new CalendarManager();
         manager.setUrl(new File("src/test/resources/before.ics").toURI().toURL());
 		manager.setVisitor(m_visitor);
         manager.updateNow();
         
         final List<Event> events = m_eventDao.findAll();
         assertEquals(2, events.size());
         assertEquals(1347312600000L, events.get(0).getStartDate().getTime());
     }
 
 	@Test
     public void testOfficialCalendarVisitorUpdate() throws Exception {
         // final URL url = new URL("https://www.google.com/calendar/ical/nh76o8dgn9d86b7n3p3uofg1q0%40group.calendar.google.com/public/basic.ics");
 		final CalendarManager manager = new CalendarManager();
         manager.setVisitor(m_visitor);
         manager.setUrl(new File("src/test/resources/before.ics").toURI().toURL());
         manager.updateNow();
         
         final List<Event> beforeEvents = m_eventDao.findAll();
         assertEquals(2, beforeEvents.size());
         assertEquals(1347312600000L, beforeEvents.get(0).getStartDate().getTime());
         assertEquals("A", beforeEvents.get(0).getSummary());
         assertEquals("B", beforeEvents.get(1).getSummary());
 
 		manager.setUrl(new File("src/test/resources/after.ics").toURI().toURL());
         manager.updateNow();
         
         final List<Event> afterEvents = m_eventDao.findAll();
         assertEquals(2, afterEvents.size());
         assertEquals("A", afterEvents.get(0).getSummary());
         assertEquals("C", afterEvents.get(1).getSummary());
 	}
 
 	@Test
     public void testOfficialCalendarVisitorUpdateJccc2() throws Exception {
         // final URL url = new URL("https://www.google.com/calendar/ical/nh76o8dgn9d86b7n3p3uofg1q0%40group.calendar.google.com/public/basic.ics");
 
 		final CalendarManager manager = new CalendarManager();
 		manager.setVisitor(m_visitor);
         manager.setUrl(new File("src/test/resources/jccc2-before.ics").toURI().toURL());
         manager.updateNow();
         
         final List<Event> beforeEvents = m_eventDao.findAll();
         assertEquals(2, beforeEvents.size());
        assertEquals("DRAMA CLUB with Bill Corbett & Peter Sagal - Culinary Arts Center", beforeEvents.get(0).getSummary());
         assertEquals("JCCC2 \"Official\" Group Photo", beforeEvents.get(1).getSummary());
 
 		manager.setUrl(new File("src/test/resources/jccc2-after.ics").toURI().toURL());
         manager.updateNow();
         
         final List<Event> afterEvents = m_eventDao.findAll();
         assertEquals(66, afterEvents.size());
         
         final Event modifiedEvent = m_eventDao.get("u3lhp5hr1t68i8sescon20htps@google.com:20110103T220000");
         assertEquals("DRAMA CLUB with Bill Corbett & Peter Sagal - Culinary Arts Center 2", modifiedEvent.getSummary());
         
         final Event deletedEvent = m_eventDao.get("9p9ueqvvu0t9moajheeodo4ros@google.com");
         assertNull(deletedEvent);
 	}
 }
