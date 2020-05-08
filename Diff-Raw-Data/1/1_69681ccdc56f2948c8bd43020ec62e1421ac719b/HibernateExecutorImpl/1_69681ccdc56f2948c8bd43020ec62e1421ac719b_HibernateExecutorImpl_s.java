 /*
  * Copyright 2000 - 2010 Ivan Khalopik. All Rights Reserved.
  */
 
 package org.greatage.domain.hibernate;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 
 /**
  * @author Ivan Khalopik
  * @since 1.0
  */
 public class HibernateExecutorImpl implements HibernateExecutor {
 	private final SessionFactory sessionFactory;
 
 	private Session session;
 	private Transaction transaction;
 
 	public HibernateExecutorImpl(final SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 
 	public <T> T execute(final HibernateCallback<T> callback) {
 		try {
 			final Session session = getSession();
 			return callback.doInSession(session);
 		} catch (RuntimeException ex) {
 			throw ex;
 		} catch (Throwable throwable) {
 			throw new RuntimeException(throwable);
 		}
 	}
 
 	public void begin() {
 		transaction = getSession().beginTransaction();
 	}
 
 	public void commit() {
 		transaction.commit();
 	}
 
 	public void rollback() {
 		transaction.rollback();
 	}
 
 	private Session getSession() {
 		if (session == null) {
 			session = sessionFactory.openSession();
 		}
 		return session;
 	}
 }
