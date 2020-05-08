 package org.eduproject.web.dao;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.eduproject.web.pojo.UserEntity;
 
 import java.util.List;
 
 @Repository
 public class UserDaoImpl implements UserDao {
 
     @Autowired
     private SessionFactory sessionFactory;
 
     @Override
     public void addUser(UserEntity userEntity) {
         try{
             sessionFactory.getCurrentSession().save(userEntity);
         }
         catch (Exception e){
             System.out.println(e);
         }
     }
 
     @Override
     public void delUserById(int id) {
         sessionFactory.getCurrentSession().
                 getNamedQuery("user.delById").setParameter("userId", id).executeUpdate();
     }
 
     @Override
     public void delAllByName(String login) {
         sessionFactory.getCurrentSession().
                 getNamedQuery("user.delAllByLogin").setParameter("login", login).executeUpdate();
     }
 
     @Override
     public List selUser() {
         return sessionFactory.getCurrentSession().getNamedQuery("user.getAll").list();
     }
 
     @Override
     public UserEntity findUserByName(String login) throws IndexOutOfBoundsException{
             return (UserEntity) sessionFactory.getCurrentSession().
                     getNamedQuery("user.findBylogin").setParameter("login", login).list().get(0);
     }
 }
