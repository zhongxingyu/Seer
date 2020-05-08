 package ru.efive.start.rhino.facade;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import ru.efive.start.rhino.domain.User;
 import ru.efive.start.rhino.persistence.UserRepository;
 
 import java.util.List;
 
 @Service
 public class UserFacade {
 
     @Autowired
     private UserRepository userRepository;
 
     @Transactional(readOnly = true)
     public List<User> getList() {
         return userRepository.getUserList();
     }
 
     @Transactional(readOnly = true)
     public User getByEmail(String email) {
         if (email == null) return null;
         return userRepository.getUserByEmail(email);
     }
 
     @Transactional
     public void addUser(User user) {
         userRepository.addUser(user);
     }
 }
