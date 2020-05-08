 package smartpool.service;
 
 import edu.yale.its.tp.cas.client.filter.CASFilter;
 import org.springframework.stereotype.Service;
 import smartpool.domain.Buddy;
 import smartpool.persistence.dao.BuddyDao;
 
 @Service
 public class BuddyService {
 
     public Buddy getBuddy(String username) {
        Buddy buddy = new BuddyDao().selectBuddy(username);
         return buddy;
     }
 
 
    public String getUserNameFromCAS() {
         return CASFilter.CAS_FILTER_USER;
     }
 
    public String getUserName() {
         return "1";
     }
 
     public Object findBuddyProfile(int id) {
 
         return null;
     }
 
     public Buddy buildBuddy(String userName) {
        return BuddyBuilder.getBuddy(userName);
     }
 }
