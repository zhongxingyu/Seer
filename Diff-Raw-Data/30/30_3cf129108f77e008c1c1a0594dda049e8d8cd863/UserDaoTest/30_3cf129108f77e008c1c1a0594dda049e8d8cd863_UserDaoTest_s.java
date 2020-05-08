 package com.worthsoln.test.repository;
 
 import com.worthsoln.patientview.model.User;
 import com.worthsoln.repository.UserDao;
 import org.junit.Test;
 
 import javax.inject.Inject;
 import java.util.Date;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 
 /**
  *
  */
 public class UserDaoTest extends BaseDaoTest {
 
     @Inject
     private UserDao userDao;
 
     @Test
     public void testAddGetUser() {
 
         User user = new User();
         user.setEmail("test@worthsolns.com");
         user.setName("Firstname Lastname");
         user.setPassword("password");
         user.setUsername("test");
        user.setCreated(new Date());
        user.setUpdated(new Date());
 
         userDao.save(user);
 
         assertTrue("Invalid id for new user", user.getId() > 0);
 
         User checkUser = userDao.get(user.getId());
 
         assertEquals("Name not persisted", user.getName(), checkUser.getName());
 
         checkUser = userDao.get(user.getUsername());
 
         assertTrue("User not found via username", checkUser != null && checkUser.equals(user));
     }
 }
