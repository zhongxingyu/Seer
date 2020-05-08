 package com.skplanet.seminar.spring.board.service;
 
 import com.skplanet.seminar.spring.board.entity.User;
 import org.springframework.stereotype.Service;
 
 /**
  * Created with IntelliJ IDEA.
  * User: synusia
  * Date: 13. 6. 20
  * Time: 오전 10:16
  * To change this template use File | Settings | File Templates.
  */
 
 @Service
 public interface AccountService {
    User loginUser(String id, String pw) throws Exception;
 }
