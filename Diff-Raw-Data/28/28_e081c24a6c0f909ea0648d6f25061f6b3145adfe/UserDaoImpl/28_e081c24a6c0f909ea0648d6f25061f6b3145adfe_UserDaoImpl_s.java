 package com.checkers.server.dao;
 
 import com.checkers.server.beans.User;
 import com.checkers.server.exceptions.LogicException;
 import org.apache.log4j.Logger;
 import org.springframework.security.crypto.password.StandardPasswordEncoder;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.List;
 
 /**
  *
  *
  * @author Pavel Kuchin
  */
 @Repository ("userDao")
 @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
 public class UserDaoImpl implements UserDao {
 
     static Logger log = Logger.getLogger(UserDaoImpl.class.getName());
 
     StandardPasswordEncoder encoder;
 
     @PersistenceContext
     private EntityManager em;
 
     public UserDaoImpl(){
         encoder = new StandardPasswordEncoder();
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
     public void newUser(User user) {
         try{
 
             user.setPassword(encoder.encode(user.getPassword()));
 
             em.persist(user);
         }catch (Exception e){
             //Catch any exception
             log.error("newUser: " + e.getMessage(), e);
         }
     }
 
     @Override
     public User getUser(Long uuid) throws LogicException{
         User user = null;
 
         try{
             user = em.find(User.class, uuid);
             if(user == null){
                 throw new LogicException(4L, "User with id " + uuid + " not found");
             }
         } catch (LogicException le){
             throw le;
         } catch (Exception e){
             //Catch any exception
             log.error("getUser: " + e.getMessage(), e);
         }
 
             return user;
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
     public void delUser(Long uuid) throws LogicException {
         try{
             User user = em.find(User.class, uuid);
             if(user == null){
                 throw new LogicException(4L, "User with id " + uuid + " not found");
             }
             em.remove(user);
         }catch (LogicException le){
             throw le;
         }catch (Exception e){
             //Catch any exception
             log.error("delUser: " + e.getMessage(), e);
         }
     }
 
     @Override
     @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public User modUser(User user) {
         try{
            if(user.getPassword() != null){
                 user.setPassword(encoder.encode(user.getPassword()));
             }
 
             em.merge(user);
         }catch (Exception e){
             //Catch any exception
             log.error("modUser" + e.getMessage(), e);
         }
 
         return user;
     }
 
     @Override
     public User getUserByLogin(String login) throws LogicException {
         User user = null;
 
         try{
             user = (User)em.createQuery("SELECT u FROM User u WHERE u.login=?1").setParameter(1, login).getSingleResult();
         }catch (Exception e){
             //Catch any exception
             log.error("getUserByLogin: " + e.getMessage(), e);
         }
 
         if(user == null){
             throw new LogicException(4L, "User with login " + login + " not found");
         }
 
         return user;
     }
 
 
     @Override
     public List<User> getUsers() {
         List<User> users = null;
 
         try{
             users = em.createQuery("SELECT u FROM User u").getResultList();
         }catch (Exception e){
             //Catch any exception
             log.error("getUsers: " + e.getMessage(), e);
         }
 
             return users;
     }
 }
