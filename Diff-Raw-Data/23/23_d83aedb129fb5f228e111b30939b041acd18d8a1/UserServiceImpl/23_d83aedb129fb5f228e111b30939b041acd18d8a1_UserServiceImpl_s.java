 package com.springapp.service;
 
 import com.springapp.dao.Userdao;
import com.springapp.domain.User;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.List;
 
 
 @Service
 @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
 public class UserServiceImpl implements UserService {
 
 
     @Autowired
     Userdao userDao;
     @Override
     @Transactional
     public void addUser(User user) {
         userDao.saveUser(user);
 
     }
 
 
     @Override
     @Transactional
     public List<User> getUser() {
         return userDao.getUser();
 
     }
 }
