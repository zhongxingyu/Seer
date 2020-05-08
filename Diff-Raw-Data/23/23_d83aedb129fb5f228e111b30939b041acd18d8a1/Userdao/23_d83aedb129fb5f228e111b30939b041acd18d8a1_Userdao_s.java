 package com.springapp.dao;
 
 
import com.springapp.domain.User;
 
 import java.util.List;
 
 
 public interface Userdao {
     public void saveUser ( User user );
     public List<User> getUser();
 
 }
