 package smartpool.persistence.dao;
 
 import org.junit.Before;
 import org.junit.Test;
 import smartpool.domain.Buddy;
 import smartpool.domain.JoinRequest;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 public class JoinRequestDaoIT {
 
     private JoinRequestDao joinRequestDao;
 
     @Before
     public void setUp() throws Exception {
         joinRequestDao = new JoinRequestDao();
     }
 
     @Test
     public void shouldInsertRequestToDB() {
        joinRequestDao.sendJoinRequest(new JoinRequest("govindm","carpool-1","9:00","Domlur"));
     }
 
     @Test
     public void shouldVerifyRequestSentByABuddy() {
         String buddyName = "mdaliej";
         String carpoolName = "carpool-1";
         Buddy ali = new Buddy(buddyName);
 
         assertFalse(joinRequestDao.isRequestSent(ali, carpoolName));
 
         joinRequestDao.sendJoinRequest(new JoinRequest(buddyName, carpoolName,"9:00","Domlur"));
 
         assertTrue(joinRequestDao.isRequestSent(ali, carpoolName));
     }
 
 }
