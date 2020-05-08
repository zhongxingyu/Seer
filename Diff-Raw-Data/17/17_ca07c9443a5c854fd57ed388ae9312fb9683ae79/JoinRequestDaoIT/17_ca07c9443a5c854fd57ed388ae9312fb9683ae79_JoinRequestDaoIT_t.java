 package smartpool.persistence.dao;
 
 import org.junit.Before;
import org.junit.Ignore;
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
    @Ignore
     public void shouldInsertRequestToDB() {
        joinRequestDao.sendJoinRequest(new JoinRequest("ayusht","carpool-1","9:00","Domlur"));
     }
 
     @Test
    @Ignore
     public void shouldVerifyRequestSentByABuddy() {
         String buddyName = "mdaliej";
         String carpoolName = "carpool-1";
         Buddy ali = new Buddy(buddyName);
 
         assertFalse(joinRequestDao.isRequestSent(ali, carpoolName));
 
         joinRequestDao.sendJoinRequest(new JoinRequest(buddyName, carpoolName,"9:00","Domlur"));
 
         assertTrue(joinRequestDao.isRequestSent(ali, carpoolName));
     }
 
 }
