 package no.niths.services;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.Feed;
 import no.niths.domain.Student;
 import no.niths.domain.location.Location;
 import no.niths.services.interfaces.FeedService;
 import no.niths.services.interfaces.LocationService;
 import no.niths.services.interfaces.StudentService;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
 public class FeedServiceTest {
 	
 	@Autowired
 	private FeedService service;
 	
 	@Autowired
 	private LocationService locService;
 
 	@Autowired
 	private StudentService studentService;
 	
 	@Test
 	public void testCRUD(){
 		int size = service.getAll(null).size();
 
 		Feed feed = new Feed("Hello this is a message");
 		service.create(feed);
 		assertEquals(size + 1, service.getAll(null).size());

		System.out.println(feed.equals(service.getById(feed.getId())));
 		assertEquals(feed, service.getById(feed.getId()));
 
 		// update time
 		feed.setMessage("new message");
 		service.update(feed);
 		feed = service.getById(feed.getId());
 		assertEquals("new message", feed.getMessage());
 
 		service.hibernateDelete(feed.getId());
 		assertEquals(size, service.getAll(null).size());
 	}
 	
 	
 	
 
 	@Test 
 	public void testFeedLocationAndStudent(){
 		Feed feed = new Feed("Hello this is a message");
 		Student student = new Student("students@mail.no");
 		Location loc = new Location("Oslo",10.2304,90.2030);
 		
 		feed.setLocation(loc);
 		feed.setStudent(student);
 		
 		service.create(feed);
 		Feed temp = service.getAll(feed).get(0);
 		assertEquals(feed,temp);
 		
 	
 		assertEquals(loc, temp.getLocation());
 		assertEquals(student, temp.getStudent());
 		
 		// update 
 		Feed feed2 = new Feed();
 		feed2.setId(feed.getId());
 		feed2.setMessage("new message");	
 		service.update(feed2);
 		
 		temp = service.getById(feed.getId());
 		assertEquals("new message", temp.getMessage());
 		assertEquals(loc, temp.getLocation());
 		
 		service.hibernateDelete(temp.getId());		
 		assertNull(service.getById(feed.getId()));
 
 		// delete student
 		assertEquals(1,studentService.getAll(student).size());
 		studentService.hibernateDelete(student.getId());
 		assertEquals(0,studentService.getAll(student).size());
 		
 		// delete location
 		assertEquals(1,locService.getAll(loc).size());
 		locService.hibernateDelete(loc.getId());
 		assertEquals(0,locService.getAll(loc).size());
 	}
 }
