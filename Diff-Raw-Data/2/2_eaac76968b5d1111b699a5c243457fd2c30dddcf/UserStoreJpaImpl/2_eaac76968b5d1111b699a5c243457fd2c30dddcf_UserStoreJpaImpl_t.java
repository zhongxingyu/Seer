 package com.financial.tools.recorderserver.store.impl;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.springframework.transaction.annotation.Transactional;
 
 import com.financial.tools.recorderserver.entity.User;
 import com.financial.tools.recorderserver.store.UserStore;
 
 @Transactional
 public class UserStoreJpaImpl implements UserStore {
 
 	private EntityManager entityManager;
 
 	@Override
 	public User getUser(long userId) {
 		return entityManager.find(User.class, userId);
 	}
 
 	@Override
 	public User getUserByName(String userName) {
 		Query query = entityManager.createQuery("select U from User U where U.name=:name");
 		query.setParameter("name", userName);
 
 		try {
 			User user = (User) query.getSingleResult();
 			return user;
 		} catch (NoResultException e) {
 			return null;
 		}
 	}
 
 	@Override
 	public long saveUser(User user) {
 		Long userId = user.getId();
		if (userId != null && userId != 0) {
 			User userFromDB = getUser(userId);
 			if (userFromDB != null) {
 				userFromDB.setBalance(user.getBalance());
 				userFromDB.setName(user.getName());
 				userFromDB.setPassword(user.getPassword());
 				userFromDB.setType(user.getType());
 
 				entityManager.persist(userFromDB);
 			}
 		} else {
 			entityManager.persist(user);
 		}
 		return user.getId();
 	}
 
 	@Override
 	public void updateBalance(long userId, float balance) {
 		Query query = entityManager.createQuery("UPDATE User T SET T.balance =:balance where T.id=:id");
 		query.setParameter("balance", balance);
 		query.setParameter("id", userId);
 
 		query.executeUpdate();
 	}
 
 	@Override
 	public List<User> findAll() {
 		Query query = entityManager.createQuery("SELECT u FROM User u", User.class);
 		return query.getResultList();
 	}
 
 	@PersistenceContext
 	public void setEntityManager(EntityManager entityManager) {
 		this.entityManager = entityManager;
 	}
 
 }
