 package hibernateManagers;
 
 import static org.junit.Assert.*;
 
 import hibernateEntities.User;
 import hibernateEntities.UserType;
 
 import java.io.File;
 import java.sql.Date;
 import java.util.ArrayList;
 
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
 
 public class CharityLoginManagerTest {
 
 	@Before
 	public void setUp() throws Exception {
 		Configuration config = new AnnotationConfiguration();
 		config.configure(new File(
 				".\\test\\testSrc\\resources\\hibernate.cfg.xml"));
 		SessionFactory factory = config.buildSessionFactory();
 		Session hibernateSession = factory.openSession();
 
 		ConnectionManagerV2.setFactory(factory); 
 		// cannot be changed to ConnectionManagerV2, no such method
 	}
 	@Test
 	public void testGetUsers() {
 		// before this, insert a new record in the table: 
 		// zwei,1,zhewei,abc,ucabwei@ucl.ac.uk,2013-02-06 22:22:00,12013-02-06 22:22:00
 		String name = "zwei";
 		String DBConfname = "hibernate.cfg.xml"; 
 		// .\\test\\testSrc\\resources\\hibernate.cfg.xml
 		// systemDBHibernateEntities/hibernate.cfg.xml
 		ArrayList<User> result = CharityLoginManager.getUsers(name, DBConfname);
 		
 		// should return a row in the user table
 		
 		assertEquals("zwei", result.get(0).getUserName());
 		assertEquals("zhewei", result.get(0).getUserPassword());
 		assertEquals("ucabwei@ucl.ac.uk", result.get(0).getUserEmail());
 		assertEquals("abc", result.get(0).getSalt());
 		assertTrue(result.get(0).getIsActive());
		assertEquals("2013-02-06 22:22:00", result.get(0).getDateCreated().toString());
 		
 		//for methods that return an object, add toString() method to the corr. class?
 		//assertEquals(" ", result.get(0).getUserTypeId());
 		
 	}
 
 }
