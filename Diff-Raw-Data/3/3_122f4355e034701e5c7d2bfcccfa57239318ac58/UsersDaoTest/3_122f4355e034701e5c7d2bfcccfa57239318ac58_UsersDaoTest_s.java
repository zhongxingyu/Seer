 // UsersDaoTest.java, created on Jan 19, 2013
 package eu.fabiostrozzi.chirp.dao;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.HashSet;
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * Tets the data access layer for the {@link UserEntity} entities.
  * 
  * @author fabio
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 public class UsersDaoTest {
 
     @Autowired
     private UsersDao dao;
 
     @Test
     public void testGetByUsername() {
         UserEntity user = dao.getByUsername("fabio.strozzi");
         assertNotNull(user);
         assertEquals("fabio.strozzi", user.getUsername());
         assertEquals("Fabio", user.getFirstName());
         assertEquals("Strozzi", user.getLastName());
        assertEquals("fc57c1fc-60fd-11e2-8d5b-544249f16afb", user.getHash());
 
         user = dao.getByUsername("john.doe");
         assertNull(user);
     }
 
     @Test
     public void testGetFollowers() {
         List<UserEntity> users = dao.getFollowersOf("fabio.strozzi");
         assertNotNull(users);
         assertEquals(1, users.size());
         assertEquals("jack.sparrow", users.get(0).getUsername());
 
         users = dao.getFollowersOf("tiger.man");
         assertNotNull(users);
         assertEquals(3, users.size());
 
         HashSet<String> usernames = new HashSet<>();
         for (UserEntity ud : users)
             usernames.add(ud.getUsername());
         assertTrue(usernames.contains("fabio.strozzi"));
         assertTrue(usernames.contains("jack.sparrow"));
         assertTrue(usernames.contains("ip.man"));
     }
 
     @Test
     public void testGetFollowedBy() {
         List<UserEntity> users = dao.getFollowedBy("tiger.man");
         assertNotNull(users);
         assertEquals(1, users.size());
         assertEquals("hulk.hogan", users.get(0).getUsername());
 
         users = dao.getFollowedBy("fabio.strozzi");
         assertNotNull(users);
         assertEquals(3, users.size());
 
         HashSet<String> usernames = new HashSet<>();
         for (UserEntity ud : users)
             usernames.add(ud.getUsername());
         assertTrue(usernames.contains("tiger.man"));
         assertTrue(usernames.contains("jack.sparrow"));
         assertTrue(usernames.contains("ip.man"));
     }
 
     @Test
     public void testFollow() {
         List<UserEntity> pre = dao.getFollowedBy("ip.man");
         dao.follow("ip.man", "fabio.strozzi");
         List<UserEntity> post = dao.getFollowedBy("ip.man");
         assertEquals(pre.size() + 1, post.size());
     }
 
     @Test
     public void testUnfollow() {
         List<UserEntity> pre = dao.getFollowedBy("ip.man");
         dao.unfollow("ip.man", "hulk.hogan");
         List<UserEntity> post = dao.getFollowedBy("ip.man");
         assertEquals(pre.size() - 1, post.size());
     }
 }
