 package de.guxx.ttdroid.lib.dao;
 
 import de.guxx.ttdroid.lib.entity.User;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author rdu
  */
 public class UserDaoImplTest
 {
     public UserDaoImplTest()
     {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception
     {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception
     {
     }
 
     @Before
     public void setUp()
     {
     }
 
     @After
     public void tearDown()
     {
     }
 
     /**
      * Test of read method, of class UserDaoImpl.
      */
     @Test
     public void testRead()
     {
         UserDao userDao = new UserDaoImpl();
 
         User user = userDao.get();
 
         try
         {
             assertTrue(!user.getSession().isEmpty());
             assertTrue(!user.getEmail().isEmpty());
             assertTrue(!user.getForeName().isEmpty());
 //            assertTrue(!user.getIntroduction().isEmpty());
             assertTrue(!user.getNick().isEmpty());
 //            assertTrue(!user.getSurName().isEmpty());
             assertTrue(user.getHeight() > 0.0);
             assertEquals(User.Gender.m, user.getGender());
             assertNotNull(user.getLocation());
             assertTrue(!user.getLocation().getName().isEmpty());
             assertTrue(user.getLocation().getLatitude() > 0.0);
             assertTrue(user.getLocation().getLongitude() > 0.0);
         }
         catch (Exception e)
         {
             fail("no exception should be thrown here");
         }
     }
 }
