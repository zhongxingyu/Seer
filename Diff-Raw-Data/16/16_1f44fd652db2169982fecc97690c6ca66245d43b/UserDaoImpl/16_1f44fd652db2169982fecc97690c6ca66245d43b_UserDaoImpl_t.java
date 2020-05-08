 package com.rmkane.dominion.dao.impl;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.rmkane.dominion.dao.UserDao;
 import com.rmkane.dominion.domain.User;
 
 @Service
 public class UserDaoImpl implements UserDao {
 
 	private static Logger logger = Logger.getLogger(UserDaoImpl.class.getName());
 
 	private final SessionFactory sessionFactory;
 
 	@Autowired
 	public UserDaoImpl(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 
 	private Session currentSession() {
 		return this.sessionFactory.getCurrentSession();
 	}
 
 	@Transactional(readOnly = true)
 	public User retrieveUserByUsername(String username) {
 		logger.info("Retrieving User: " + username);
 		User user = null;
 		try {
 			user = getUserByUsername(username);
 		} catch (IndexOutOfBoundsException e) {
 			logger.warn("Could not retrieve User: " + username);
 		}
 		return user;
 	}
 	
 	@Transactional(readOnly = false)
 	public void deleteUserByUsername(String username) {
 		logger.info("Deleting User: " + username);
 		User user = retrieveUserByUsername(username);
 		if (user != null) {
 			delete(user);
 		} else {
 			logger.warn("Could not delete User: " + username);
 		}
 	}
 
 	@Transactional(readOnly = true)
 	public User getUserByUsername(String username) {
 		logger.info("Retrieving User: " + username);
 		Query query = currentSession().getNamedQuery(User.FIND_USER_BY_USERNAME).setString("username", username);
 		return (User) query.list().get(0);
 	}
 
	@Transactional(readOnly = true)
 	public User retrieve(Long id) {
 		logger.info("Retrieving User: #" + id);
 		return (User) currentSession().get(User.class, id);
 	}
 
 	@Transactional(readOnly = false)
 	public User insert(User user) {
 		logger.info("Inserting User: " + user.getUsername());
 		try {
 			currentSession().save(user);
 		} catch(Exception e) {
 			logger.error("User: " + user.getUsername() + " already exists!");
 			currentSession().close();
 		}
 		return user;
 	}
 
 	@Transactional(readOnly = false)
 	public User update(User user) {
 		logger.info("Updating User: " + user.getUsername());
 		currentSession().update(user);
 		return user;
 	}
 
 	@Transactional(readOnly = true)
 	public void delete(User user) {
 		logger.info("Removing User: " + user.getUsername());
 		currentSession().delete(user);
 	}
 
 	@Transactional(readOnly = true)
 	@SuppressWarnings("unchecked")
 	public List<User> retrieveAll() {
 		logger.info("Retrieving All Users");
 		Criteria criteria = currentSession().createCriteria(User.class);
 		return criteria.list();
 	}
 	
 	@Transactional(readOnly = false)
 	public void resetAutoIncrement() {
 		Query query = currentSession().createSQLQuery("alter table USER auto_increment = 1");
 		query.executeUpdate();
 	}
 }
