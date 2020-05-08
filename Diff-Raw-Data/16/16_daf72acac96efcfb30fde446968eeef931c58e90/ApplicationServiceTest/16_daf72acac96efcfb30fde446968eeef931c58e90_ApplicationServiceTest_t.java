 package no.niths.services;
 
 import static org.junit.Assert.assertEquals;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.APIEvent;
 import no.niths.domain.Application;
 import no.niths.domain.Developer;
 import no.niths.services.interfaces.APIEventService;
 import no.niths.services.interfaces.ApplicationService;
 import no.niths.services.interfaces.DeveloperService;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
 public class ApplicationServiceTest {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(ApplicationServiceTest.class);
 	
 	@Autowired
 	private ApplicationService service;
 	
 	@Autowired
 	private DeveloperService devService;
 	
 	@Test
 	public void testCRUD(){
 		
 		int appSize = service.getAll(null).size();
 		
 		Application a1 = new Application();
 		a1.setTitle("this a title ok");
 		Application a2 = new Application();
 		service.create(a1);
 		service.create(a2);
 		assertEquals(appSize + 2, service.getAll(null).size());
 		
 		Application temp = new Application();
 		temp.setTitle("this a title ok");
 		assertEquals(1, service.getAll(temp).size());
 		
 		Application fetched = service.getById(a1.getId());
 		assertEquals(a1, fetched);
 		
 		a1.setTitle("this is another title");
 		service.update(a1);
 		assertEquals(0, service.getAll(temp).size());
 		assertEquals(1, service.getAll(a1).size());
 		
 		service.hibernateDelete(a1.getId());
 		service.hibernateDelete(a2.getId());
 	}
 	
 	@Test
 	public void testDevRelation(){
 		int devSize = devService.getAll(null).size();
 		int appSize = service.getAll(null).size();
		Developer d1 = new Developer("Tha name");
 		devService.create(d1);
 		assertEquals(devSize + 1, devService.getAll(null).size());
		Application a1 = new Application("Heilain");
		Application a2 = new Application("Segkland");
 		service.create(a1);
 		service.create(a2);
 		assertEquals(appSize + 2, service.getAll(null).size());
 		assertEquals(true, devService.getById(d1.getId()).getApps().isEmpty());
 		//Set developer from app
 		a1.setDeveloper(d1);
 		service.update(a1);
 		//Dev should now have the app
 		assertEquals(1, devService.getById(d1.getId()).getApps().size());
 		
 		service.hibernateDelete(a1.getId());
 		assertEquals(appSize + 1, service.getAll(null).size());
 		assertEquals(true, devService.getById(d1.getId()).getApps().isEmpty());
 		
 		devService.hibernateDelete(d1.getId());
 		service.hibernateDelete(a2.getId());
 	}
 	
 }
