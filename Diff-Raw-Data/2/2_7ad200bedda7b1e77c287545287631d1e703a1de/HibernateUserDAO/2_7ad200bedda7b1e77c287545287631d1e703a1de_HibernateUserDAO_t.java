 package com.examscam.dao;
 
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.criterion.Example;
 import org.hibernate.criterion.MatchMode;
 
 import com.examscam.model.User;
 
 // Title: Hibernate Made Easy
 // Author: Cameron McKenzie
 
 // Page: 224 create HibernateUserDAO class
 // Page: 226 implemented create method
 // Page: 227 implemented update method
 // Page: 228 implemented delete and findByPrimaryKey methods
 // Page: 229 implemented findAll and findByExample methods
 
 
 public class HibernateUserDAO extends ExamScamDAO implements UserDAO {
 
 	@Override
 	public User create(User user) {
 		if (user.getId() != null && user.getId() != 0) {
 			user = null;
 		} else {
 			user.setLastAccessTime(new java.util.Date());
 			user.setRegistrationDate(new java.util.GregorianCalendar());
 			user.setVerified(false);
 			super.save(user);
 		}
 		return user;
 	}
 
 	@Override
 	public boolean update(User user) {
 		boolean successFlag = true;
 		try {
 			if (user.getId() == null || user.getId() == 0) {
 				successFlag = false;
 			} else {
 				super.save(user);
 			}
 		} catch (Throwable th) {
 			successFlag = false;
 		}
 		return successFlag;
 	}
 
 	@Override
 	public boolean delete(User user) {
 		boolean successFlag = true;
 		try {
 			user.setPassword("");
 			super.delete(user);
 		} catch (Throwable th) {
 			successFlag = false;
 		}
 		return successFlag;
 	}
 
 	@Override
 	public User findByPrimaryKey(Long primaryKey) {
 		User user = (User) super.findByPrimaryKey(User.class, primaryKey);
 		return user;
 	}
 
 	@Override
 	public List findAll() {
		String queryString = "from User";
 		// java.lang.IllegalArgumentException: node to traverse cannot be null!
 		Query queryResult = this.getSession().createQuery(queryString);
 		return queryResult.list();
 	}
 
 	@Override
 	public List findByExample(User user, boolean fuzzy) {
 		List users = null;
 		Session session = this.getSession();
 		Criteria criteria = session.createCriteria(User.class);
 		Example example = Example.create(user);
 		if (fuzzy) {
 			example.enableLike(MatchMode.ANYWHERE);
 			example.ignoreCase();
 			example.excludeZeroes();
 		}
 		criteria.add(example);
 		users = criteria.list();
 		return users;
 	}
 }
