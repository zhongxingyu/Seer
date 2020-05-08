 package com.vacuumhead.wesplit.application;
 
 import com.vacuumhead.wesplit.ViewObject.GroupViewObject;
 import com.vacuumhead.wesplit.ViewObject.UserViewObject;
 import com.vacuumhead.wesplit.dao.IUserAccountDao;
 import com.vacuumhead.wesplit.dao.IUserDao;
 import com.vacuumhead.wesplit.dao.UserAccountDao;
 import com.vacuumhead.wesplit.tables.Group;
 import com.vacuumhead.wesplit.tables.User;
 import com.vacuumhead.wesplit.tables.UserAccount;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceUnit;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: pratyushverma
  * Date: 24/03/13
  * Time: 12:47 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class UserServiceApplicationLogic implements IUserServiceApplicationLogic {
     @PersistenceUnit
     private EntityManagerFactory emf;
 
     public void setEmf(EntityManagerFactory emf) {
         this.emf = emf;
     }
 
     private IUserAccountDao userAccountDao;
     private IUserDao userDao;
 
     public UserServiceApplicationLogic(IUserAccountDao userAccountDao, IUserDao userDao) {
         this.userAccountDao = userAccountDao;
         this.userDao = userDao;
     }
 
     public UserServiceApplicationLogic(UserAccountDao userAccountDao) {
     }
 
     public UserViewObject retrieveUser(Integer userId) {
         EntityManager em = emf.createEntityManager();
         UserAccount user;
         try {
             em.getTransaction().begin();
             user = retrieveUserAccount(em, userId);
         } finally {
             em.getTransaction().commit();
         }
         return new UserViewObject(user);
     }
 
     public UserViewObject createUser(String username, String password) {
         EntityManager em = emf.createEntityManager();
         UserAccount userAccount;
         try {
             em.getTransaction().begin();
             userAccount = new UserAccount(username, password);
             User user = new User();
             userAccount.setUserEmbedded(user);
             user.setUserAccountEmbedded(userAccount);
             userAccountDao.createUserAccount(em, userAccount);
 
         } finally {
             em.getTransaction().commit();
         }
         return new UserViewObject(userAccount);
     }
 
     public UserViewObject loginUser(String username, String password) {
         EntityManager em = emf.createEntityManager();
         UserAccount userInfo;
         try {
             em.getTransaction().begin();
             userInfo = retrieveUserAccount(em, username);
 
         } finally {
             em.getTransaction().commit();
         }
        if(userInfo == null || !userInfo.getPassword().equals(password))
             return null;
         return new UserViewObject(userInfo);
     }
 
     public boolean checkIfUserExist(String username) {
         EntityManager em = emf.createEntityManager();
         boolean exist;
         try {
             em.getTransaction().begin();
             exist = checkExistUser(em, username);
         } finally {
             em.getTransaction().commit();
         }
         return exist;
     }
 
     public List<GroupViewObject> retrieveAllGroupForUser(Integer accountId) {
         EntityManager em = emf.createEntityManager();
         List<GroupViewObject> groupViewObjectList = new ArrayList<GroupViewObject>();
         User user;
         try {
             em.getTransaction().begin();
             user = userDao.retrieveUserById(em, accountId);
             for(Group group : user.getGroupMemberList()) {
                 groupViewObjectList.add(new GroupViewObject(group));
             }
         } finally {
             em.getTransaction().commit();
         }
         return groupViewObjectList;
     }
 
     private boolean checkExistUser(EntityManager em, String username) {
         return userAccountDao.retrieveUserAccount(em, username) != null;
     }
 
     private UserAccount retrieveUserAccount(EntityManager em, String username) {
         return userAccountDao.retrieveUserAccount(em, username);
     }
 
     private UserAccount retrieveUserAccount(EntityManager em, Integer accountId) {
         return userAccountDao.retrieveUserAccount(em, accountId);
     }
 }
