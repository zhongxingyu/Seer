 package edu.northwestern.bioinformatics.studycalendar.security;
 
 import org.acegisecurity.userdetails.UserDetails;
 import org.acegisecurity.userdetails.UsernameNotFoundException;
 import org.acegisecurity.userdetails.UserDetailsService;
 import org.springframework.dao.DataAccessException;
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.service.UserService;
 
 public class PscUserDetailsService implements UserDetailsService {
     private UserService userService;
 
     public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException, DataAccessException {
        return userService.getUserByName(userName);
     }
 
     public void setUserService(UserService userService) {
         this.userService = userService;
     }
 }
