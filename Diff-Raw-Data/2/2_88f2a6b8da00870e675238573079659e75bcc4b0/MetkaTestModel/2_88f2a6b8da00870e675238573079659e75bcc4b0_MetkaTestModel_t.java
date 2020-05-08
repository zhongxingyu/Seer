 package fi.uta.fsd.metka;
 
 import org.junit.Before;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:testApplicationContext.xml"})
 public class MetkaTestModel {
 
 	@Before
 	public void before() {
 		
 	}
 }
