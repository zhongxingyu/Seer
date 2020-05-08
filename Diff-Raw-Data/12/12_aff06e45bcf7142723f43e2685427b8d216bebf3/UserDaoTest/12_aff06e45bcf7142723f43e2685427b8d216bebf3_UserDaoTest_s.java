 package de.atomfrede.mate.domain.entities.user;
 
import java.util.List;
 
import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import de.atomfrede.mate.domain.dao.user.UserDao;
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration( { "../../../../../../domain-context.xml" })
 public class UserDaoTest {
 	
 	@Autowired
 	ApplicationContext context;
 	
 	@Autowired
 	UserDao userDao;
 	
 	User testUser;
 	
 	@Before
 	public void addUser(){
 		testUser = new User();
 		testUser.setEmail("max@muster.org");
 		testUser.setFirstname("Max");
 		testUser.setLastname("Muster");
 		testUser.setUsername("mmuster");
 		userDao.persist(testUser);	
 	}
 	
 	@After
 	public void clear(){
 		userDao.remove(testUser);
 	}
 	
 	@Test
 	public void findAllTest(){
 		List<User> allUsers = userDao.findAll();
 		assertNotNull(allUsers);
 		assertEquals(1, allUsers.size());
 	}
 	
 	@Test
 	public void deleteTest(){
 		
 	}
 
 }
