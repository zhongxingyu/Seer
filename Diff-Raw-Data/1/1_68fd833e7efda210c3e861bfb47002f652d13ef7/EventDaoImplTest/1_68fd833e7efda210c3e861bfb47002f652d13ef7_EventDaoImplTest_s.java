 package com.eventsharing.business.dao.impl;
 
 import javax.inject.Inject;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.asset.EmptyAsset;
 import org.jboss.shrinkwrap.api.spec.JavaArchive;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import com.eventsharing.business.dao.EventDao;
 import com.eventsharing.business.filter.EventFilter;
 import com.eventsharing.entity.Event;
 
 @RunWith(Arquillian.class)
 public class EventDaoImplTest {
 
 	@Deployment
 	public static JavaArchive createDeployment() {
 		JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
 				.addClasses(Event.class, EventFilter.class, EventDao.class, EventDaoImpl.class)
 				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
 		System.out.println(jar.toString(true));
 		return jar;
 	}
 	
 	@Inject
 	private EventDao eventDao;
 	
 	@Test
 	public void testRetrieveEvet() {
 		System.out.println(eventDao.getClass().getName());
 		Event event = eventDao.retrieveEvent(1);
 		if (event != null) {
 			System.out.println("Event " + event.getEventID() + " :");
 			System.out.println(" " + event.getName());
 			System.out.println(" at " + event.getLocation());
 			System.out.println(" on " + event.getDateTime());
 		}
 	}
 }
