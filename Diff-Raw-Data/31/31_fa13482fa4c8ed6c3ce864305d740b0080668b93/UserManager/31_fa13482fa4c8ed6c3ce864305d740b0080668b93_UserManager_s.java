 package com.exadel.borsch.managers;
 
 import com.exadel.borsch.dao.User;
 
 import java.util.List;
 import java.util.UUID;
 
 /**
  * @author Andrey Zhilka
  */
 public interface UserManager {
     User getUserById(UUID userId);
    User getUserByName(String name);
     void deleteUserById(UUID userId);
     void updateUser(User toUpdate);
     User getUserByHash(String hashId);
     void deleteUserByHash(String hashId);
     void addUsers(List<User> toAdd);
     List<User> getAllUsers();
 }
