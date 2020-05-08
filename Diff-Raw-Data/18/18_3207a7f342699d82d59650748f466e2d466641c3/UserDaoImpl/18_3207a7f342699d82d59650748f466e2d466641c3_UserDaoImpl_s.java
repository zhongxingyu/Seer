 package org.aphillips.hw.impl;
 
 import java.io.Serializable;
 import java.text.MessageFormat;
 import java.util.List;
 
 import org.aphillips.hw.api.UserDao;
 import org.aphillips.hw.domain.User;
 import org.aphillips.hw.domain.UserValidator;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Repository;
 
 @Repository
 @Component
 public class UserDaoImpl implements UserDao {
 
   private SessionFactory sessionFactory;
 
   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
     this.sessionFactory = sessionFactory;
   }
 
   @Override
   public void saveUser(User user) {
     
     UserValidator v = new UserValidator(user);
     v.validate();
     
     Session session = sessionFactory.openSession();
     session.beginTransaction();
     final Serializable id = session.save(user);
     session.getTransaction().commit();
     //FIXME close session in finally or use spring transactions
     session.close();
   }
 
   @Override
   public User readUser(Long id) {
     Session session = sessionFactory.openSession();
 
     User example = new User();
     example.setId(id);
 
    //FIXME: the example id value is not being used for some reason, we are getting back all users
    List results = session.createCriteria(User.class).add(Example.create(example)).list();
     if(results.size() < 1) {
       return null;
     }
     if(results.size() > 1) {
       throw new IllegalStateException(MessageFormat.format("Somehow we have more than one user with id {0}", id));
     }
     //FIXME close session in finally or use spring transactions
     session.close();
     
     return (User)results.get(0);
 
   }
   
 
 }
