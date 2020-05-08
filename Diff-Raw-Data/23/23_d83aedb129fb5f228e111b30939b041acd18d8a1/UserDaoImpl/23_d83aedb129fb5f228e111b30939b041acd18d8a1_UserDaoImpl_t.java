 package com.springapp.dao;
 
 
import com.springapp.mvc.model.User;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import java.util.List;
 
 
 @Repository("userDao")
 public class UserDaoImpl implements Userdao {
 
 
     @Autowired
     private SessionFactory sessionfactory;
 
 
     @Override
     public void saveUser(User user) {
         sessionfactory.getCurrentSession().saveOrUpdate(user);
     }
 
 
     @Override
     public List<User> getUser() {
 
         @SuppressWarnings("unchecked")
         List<User> userlist = sessionfactory.getCurrentSession()
                 .createCriteria(User.class).list();
         return userlist;
 
     }
 
 }
