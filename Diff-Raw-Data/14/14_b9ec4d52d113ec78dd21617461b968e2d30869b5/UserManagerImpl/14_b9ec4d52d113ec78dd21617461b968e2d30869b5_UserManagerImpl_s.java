 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.greenland.memorycards.service;
 
 import com.greenland.memorycards.model.User;
 import com.greenland.memorycards.repository.UserDao;
 import java.util.List;
 
 /**
  *
  * @author jurijspe
  */
 public class UserManagerImpl implements UserManager {
 
     private UserDao userDao;
     private CardGroupManager cardGroupManager;
 
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 
     public void setCardGroupManager(CardGroupManager cardGroupManager) {
         this.cardGroupManager = cardGroupManager;
     }
 
     @Override
     public User getUser(String email, String password) {
         User aUser = new User();
         aUser = userDao.getUser(email, password);
         if (aUser != null) {
             aUser.setCardGroups(cardGroupManager.getCardGroupsForUser(email));
         }
         return aUser;
     }
 
     @Override
     public List<User> getAllUsers() {
         return userDao.getAllUsers();
     }
 
     @Override
     public User getUser(int userId) {
        return userDao.getUser(userId);
     }
 
     @Override
     public User combineUsers(User userToBeUpdated, User formUser) {
         
         // The code below smells. TODO: Refactor
         User user = new User();
         user.setId(userToBeUpdated.getId());
         if (formUser.getEmail() == null || formUser.getEmail().equals("")) {
             user.setEmail(userToBeUpdated.getEmail());
         } else {
             user.setEmail(formUser.getEmail());
         }
         if (formUser.getPassword() == null || formUser.getPassword().equals("") || formUser.getPassword().length()<3) {
             user.setPassword(userToBeUpdated.getPassword());
         } else {
             user.setPassword(formUser.getPassword());
         }
         if (formUser.getfName() == null || formUser.getfName().equals("")) {
             user.setfName(userToBeUpdated.getfName());
         } else {
             user.setfName(formUser.getfName());
         }
         if (formUser.getlName() == null || formUser.getlName().equals("")) {
             user.setlName(userToBeUpdated.getlName());
         } else {
             user.setlName(formUser.getlName());
         }
         System.out.println("User which will be updated: "+user);
         return user;
     }
 
     @Override
     public void updateUser(User user) {
         userDao.updateUser(user);
     }
 
     @Override
     public void deleteUserWithId(Integer userId) {
         userDao.deleteUserWithId(userId);
     }
 
     @Override
     public void createNewUser(User formUser) {
         userDao.createNewUser(formUser);
     }
 }
