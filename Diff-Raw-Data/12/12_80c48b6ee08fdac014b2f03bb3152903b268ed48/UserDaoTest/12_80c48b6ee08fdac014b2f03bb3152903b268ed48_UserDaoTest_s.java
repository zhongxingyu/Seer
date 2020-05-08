 package fi.helsinki.cs.dao;
 
 import java.util.List;
 
 import fi.helsinki.cs.model.User;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.test.annotation.Rollback;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 @ContextConfiguration("/test-context.xml")
 @RunWith(SpringJUnit4ClassRunner.class)
 @Transactional
 public class UserDaoTest {
 
 	@Autowired
 	private UserDao userDao;
         
        @Autowired
         private PasswordEncoder passwordEncoder;
 
         @Test
         @Rollback(false)
         public void testConvertPasswords() {
             List<User> users = userDao.getUsers();
             for (User user : users) {
                     String oldPass = user.getPassword();
                     String newPass = passwordEncoder.encodePassword(oldPass, user.getEmail());
                     user.setPassword(newPass);
                     userDao.save(user);
             }
        }
         
         @Test
 	public void testSaveAndDelete() {
             User p = new User();
             p.setName("Al Bundy");
             p.setEmail("bundy@email.com");
             p.setPassword("password");
             userDao.save(p);
             Long id = p.getId();
             Assert.assertNotNull(id);
             Boolean bol = userDao.delete(id);
 	}
         
 	@Test
 	public void testSaveAndGetUser() {
             User p = new User();
             p.setName("Al Bundy");
             p.setEmail("bundy@email.com");
             p.setPassword("password");
             System.out.println("p.created: "+p.getCreated());
             userDao.save(p);
             Long id = p.getId();
             Assert.assertNotNull(id);
             
             User p2 = userDao.find(id);
             Assert.assertEquals(id, p2.getId());
 	}
 
         @Test
 	public void testListUsers() {
             List<User> users = userDao.getUsers();
             Assert.assertTrue(users.size()>0);
 	}
         
         @Test
         public void testFindByEmail() {
             User user = userDao.findByEmail("bond@email.com");
             Assert.assertEquals("James Bond", user.getName());
         }
         
         
 
 }
