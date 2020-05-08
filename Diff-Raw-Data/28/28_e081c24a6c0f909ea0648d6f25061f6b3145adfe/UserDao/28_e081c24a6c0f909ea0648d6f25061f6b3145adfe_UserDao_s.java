 package com.checkers.server.dao;
 
 import com.checkers.server.beans.User;
 import com.checkers.server.exceptions.LogicException;
 
 import java.util.List;
 
 /**
  *
  *
  * @author Pavel Kuchin
  */
 public interface UserDao {
     void newUser(User user);
 
     User getUser(Long uuid) throws LogicException;
 
     void delUser(Long uuid) throws LogicException;
 
    User modUser(User user);
 
     User getUserByLogin(String login) throws LogicException;
 
     List<User> getUsers();
 }
