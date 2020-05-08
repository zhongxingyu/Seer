 package com.springapp.dao;
 
 
import com.springapp.mvc.model.User;
 
 import java.util.List;
 
 
 public interface Userdao {
     public void saveUser ( User user );
     public List<User> getUser();
 
 }
