 package com.sk.domain.dao;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 @Repository(value = "baseEntityDao")
 public class BaseEntityDao {
 
 	@Autowired
 	private SessionFactory sessionFactory;
 
 	private Session getSession() {
 		return sessionFactory.getCurrentSession();
 	}
 
 	public Object get(Class<?> clazz, Long id) {
 		return getSession().get(clazz, id);
 	}
 }
