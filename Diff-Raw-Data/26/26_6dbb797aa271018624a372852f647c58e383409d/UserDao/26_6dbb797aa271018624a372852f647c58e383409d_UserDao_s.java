 
 package edu.chl.dat076.foodfeed.model.dao;
 
 import edu.chl.dat076.foodfeed.model.entity.User;
 import org.springframework.stereotype.Repository;
 
 @Repository("userDao")
 public class UserDao extends AbstractDao<User, String> {
     
     public UserDao(){
         super(User.class);
    }    
 }
