 package com.vacuumhead.wesplit.dao;
 
 import com.vacuumhead.wesplit.tables.User;
 
 import javax.persistence.EntityManager;
 
 /**
  * Created by IntelliJ IDEA.
  * User: pratyushverma
  * Date: 26/03/13
  * Time: 12:30 AM
  * To change this template use File | Settings | File Templates.
  */
 
 public class UserDao implements IUserDao {
 /*
     @PersistenceUnit
     private EntityManagerFactory emf;
 
     public void setEmf(EntityManagerFactory emf) {
         this.emf = emf;
     }*/
 
     public UserDao() {
     }
 
     public User retrieveUserById(EntityManager entityManager,Integer accountId) {
         //EntityManager entityManager = emf.createEntityManager();
         User user = entityManager.find(User.class, accountId);
         return user;
     }
 
     public void updateUser(EntityManager entityManager,User user) {
         //EntityManager entityManager = emf.createEntityManager();
         //entityManager.getTransaction().begin();
         entityManager.merge(user);
         //entityManager.getTransaction().commit();
     }
 }
