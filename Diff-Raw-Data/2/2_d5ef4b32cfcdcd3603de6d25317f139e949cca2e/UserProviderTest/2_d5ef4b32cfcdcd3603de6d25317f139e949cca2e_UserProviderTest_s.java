 package org.timadorus.webapp.tests.server;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.timadorus.webapp.beans.User;
 import org.timadorus.webapp.server.rpc.service.UserServiceImpl;
 
 
 public class UserProviderTest {
 
   
   
   private User user1;
   private UserServiceImpl  userService;
   //private RegisteredUserList registeredUserList;
   
   @Before
   public void setUp() throws Exception {
     userService = new UserServiceImpl();
        
     user1 = new User("vorname", "nachname", "1.1.1970", "test@mail.org", "testuser01", "password");
     
    //TODO user in die DB bekommen, wie es aussieht geht das nur ber die GUI
     
    }
   
   
   
   @Test
   public void test() {
     Long userid = user1.getId();
     String vorname = "vorname_test";
     String nachname = "nachname_test";
     String email = "email@test.org";
     String gb = "01.01.1971";
     String pw = "pw_test";
     
     
     user1.setVorname(vorname);
     user1.setNachname(nachname);
     user1.setEmail(email);
     user1.setGeburtstag(gb);
     user1.setPassword(pw);
     
     userService.update(user1.getId(), user1);
     
     user1 = userService.getUser(user1);
     
     Assert.assertEquals(userid, user1.getId());
     Assert.assertEquals(vorname, user1.getVorname());
     Assert.assertEquals(nachname, user1.getNachname());
     Assert.assertEquals(email, user1.getEmail());
     Assert.assertEquals(gb, user1.getGeburtstag());
     Assert.assertEquals(pw, user1.getPassword());
       }
 
 }
