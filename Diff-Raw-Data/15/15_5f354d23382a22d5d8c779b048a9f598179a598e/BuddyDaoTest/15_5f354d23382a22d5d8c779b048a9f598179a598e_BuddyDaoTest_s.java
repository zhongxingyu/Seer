 package smartpool.persistence.dao;
 
 import junit.framework.Assert;
 import org.junit.Test;
 import smartpool.domain.Buddy;
 
 public class BuddyDaoTest {
     @Test
     public void shouldGetBuddyByUserName(){
        Buddy expectedBuddy = new Buddy("suganthk");
         BuddyDao buddyDao = new BuddyDao();
        Assert.assertEquals(expectedBuddy, buddyDao.selectBuddy("suganthk"));
     }
 
     @Test
     public void shouldGetFirstBuddy(){
         Buddy expectedBuddy = new Buddy("1");
         BuddyDao buddyDao = new BuddyDao();
         Assert.assertEquals(expectedBuddy, buddyDao.selectAllBuddies());
     }
 
 
 
 }
