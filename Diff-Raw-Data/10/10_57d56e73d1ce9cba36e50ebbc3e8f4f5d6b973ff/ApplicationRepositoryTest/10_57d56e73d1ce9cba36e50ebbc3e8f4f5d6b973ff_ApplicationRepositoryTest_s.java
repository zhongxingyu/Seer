 package no.niths.infrastructure;
 
 import static org.junit.Assert.assertEquals;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.domain.Application;
 import no.niths.infrastructure.interfaces.ApplicationRepository;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes= { TestAppConfig.class, HibernateConfig.class})
 @Transactional 
 public class ApplicationRepositoryTest {
 	
 	@Autowired
 	private ApplicationRepository appRepo;
 	
 	@Test
 	public void testCRUD(){
 		int size = appRepo.getAll(null).size();
 		
		Application a = new Application();
 		appRepo.create(a);
 		
 		assertEquals(size + 1, appRepo.getAll(null).size());
 		
 		assertEquals(a, appRepo.getById(a.getId()));
 		
 		a.setTitle("title");
 		appRepo.update(a);
 		assertEquals("title", appRepo.getById(a.getId()).getTitle());
 		
 		assertEquals(true, appRepo.delete(a.getId()));
 	}
 
 }
