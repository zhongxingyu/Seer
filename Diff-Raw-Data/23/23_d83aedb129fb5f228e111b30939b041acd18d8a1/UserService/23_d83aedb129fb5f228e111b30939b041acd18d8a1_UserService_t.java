 package com.springapp.service;
 
import com.springapp.mvc.model.User;
 
 import java.util.List;
 
 public interface UserService {
     public void addUser(User user);
     public List<User> getUser();
 
 }
