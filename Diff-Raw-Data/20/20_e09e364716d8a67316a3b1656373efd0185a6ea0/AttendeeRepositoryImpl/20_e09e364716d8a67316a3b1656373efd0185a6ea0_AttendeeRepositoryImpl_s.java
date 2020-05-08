 package cn.mobiledaily.repository;
 
 import cn.mobiledaily.domain.identity.User;
 import cn.mobiledaily.domain.mobile.Attendee;
 import org.springframework.stereotype.Repository;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Johnson
  * Date: 3/4/13
  * Time: 10:58 PM
  * To change this template use File | Settings | File Templates.
  */
@Repository(value = "userRepository")
 public class AttendeeRepositoryImpl implements AttendeeRepository {
     @Override
     public void persist(Attendee attendee) {
     }
 
     @Override
     public List<Attendee> findAll() {
         return null;
     }
 
     @Override
     public List<Attendee> findByName(String name) {
         return null;
     }
 
     @Override
     public User findByEmail(String email) {
         return null;
     }
 }
