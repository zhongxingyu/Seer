 package it.sevenbits.space.service;
 
 import java.util.HashMap;
 
 import it.sevenbits.space.domain.User;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Service;
 
 @Service
 public class UserManager {
 
     private HashMap<String, User> users;
 
     public UserManager() {
         users = new HashMap<String, User>();
        users.put("user", new User("user", "123", "ROLE_USER"));
        users.put("admin", new User("admin", "234", "ROLE_USER, ROLE_ADMIN"));
     }
 
     public User getUser(String username) throws UsernameNotFoundException {
         if(!users.containsKey(username)) {
             throw new UsernameNotFoundException(username + " not found");
         }
         return users.get(username);
     }
 }
