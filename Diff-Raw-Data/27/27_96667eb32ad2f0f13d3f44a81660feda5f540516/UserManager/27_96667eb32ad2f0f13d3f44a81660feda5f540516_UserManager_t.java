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
        users.put("jerry", new User("jerry", "1234", "ROLE_USER"));
        users.put("tom", new User("tom", "2345", "ROLE_USER, ROLE_ADMIN"));
     }
 
     public User getUser(String username) throws UsernameNotFoundException {
         if(!users.containsKey(username)) {
             throw new UsernameNotFoundException(username + " not found");
         }
         return users.get(username);
     }
 }
