 package com.skplanet.seminar.spring.board.service.impl;
 
 import com.skplanet.seminar.spring.board.entity.User;
 import com.skplanet.seminar.spring.board.repository.UserRepository;
 import com.skplanet.seminar.spring.board.service.AccountService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 /**
  * Created with IntelliJ IDEA.
  * User: synusia
  * Date: 13. 6. 20
  * Time: 오전 10:16
  * To change this template use File | Settings | File Templates.
  */
 
 @Service
 public class AccountServiceImpl implements AccountService {
 
     @Autowired
     UserRepository userRepository;
 
     @Override
    public User loginUser(String id, String pw) throws Exception {
         User retVal = null;
         User user = userRepository.selectUser(id);
         if (user != null && user.getId().equals(pw)) {
             retVal = user;
        } else {
            throw new Exception("invalid id or password");
         }
 
         return retVal;
     }
 }
