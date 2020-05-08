 package it.sevenbits.space.dao.hibernate;
 
 import it.sevenbits.space.dao.IUserDao;
 import it.sevenbits.space.domain.User;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import java.util.List;
 
 @Repository
 public class UserDao implements IUserDao {
 
     @PersistenceContext
     private EntityManager entityManager;
 
     @Transactional
     @Override
     public User addUser(User user) {
         entityManager.persist(user);
         entityManager.flush();
         return entityManager.find(User.class, user.getId());
     }
 
     @Transactional
     @Override
     public boolean removeUser(Long id) {
         //To Do.
         return false;
     }
 
     @Transactional
     @Override
     public List<User> findAllUsers() {
         return entityManager.
                 createQuery("select u from User u", User.class).
                 getResultList();
     }
 
     @Transactional
     @Override
     public User findUserById(Long id) {
         return entityManager.
                 createQuery("select u from User u where u.id = :id", User.class).
                 setParameter("id", id).
                 getSingleResult();
     }
 
     @Transactional
     @Override
     public User findUserByUsername(String username) {
         List<User> result = entityManager.
                 createQuery("select u from User u where u.username = :username", User.class).
                 setParameter("username", username).
                 getResultList();
 
         if (result.isEmpty()) {
             return null;
         }
         return result.get(0);
     }
 
     @Transactional
     @Override
     public User findUserByEmail(String email) {
         List<User> result = entityManager.
                 createQuery("select u from User u where u.email = :email", User.class).
                 setParameter("email", email).
                 getResultList();
 
         if (result.isEmpty()) {
             return null;
         }
         return result.get(0);
     }
 }
