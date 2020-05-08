 package edu.northwestern.bioinformatics.studycalendar.security;
 
 import org.acegisecurity.userdetails.UserDetails;
 import org.acegisecurity.userdetails.UsernameNotFoundException;
 import org.acegisecurity.userdetails.UserDetailsService;
 import org.springframework.dao.DataAccessException;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
 
 public class PscUserDetailsService implements UserDetailsService {
     private UserService userService;
 
     public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException, DataAccessException {
        User user = userService.getUserByName(userName);
        if (user == null) throw new UsernameNotFoundException("Unknown user " + userName);
        return user;
     }
 
     public void setUserService(UserService userService) {
         this.userService = userService;
     }
 }
