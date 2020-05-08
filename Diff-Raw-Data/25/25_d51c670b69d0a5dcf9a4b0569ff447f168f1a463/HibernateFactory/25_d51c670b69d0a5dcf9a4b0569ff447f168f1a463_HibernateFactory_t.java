 /**
  * Copyright (c) Members of the EGEE Collaboration. 2006-2009.
  * See http://www.eu-egee.org/partners/ for details on the copyright holders.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * Authors:
  * 	Andrea Ceccanti (INFN)
  */
 package org.glite.security.voms.admin.persistence;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.glite.security.voms.admin.configuration.VOMSConfiguration;
 import org.glite.security.voms.admin.error.VOMSFatalException;
 import org.glite.security.voms.admin.persistence.error.VOMSDatabaseException;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.cfg.Configuration;
 
 public class HibernateFactory {
 
 	private static final Logger log = LoggerFactory.getLogger(HibernateFactory.class);
 
 	private static final SessionFactory sessionFactory;
 
 	private static final ThreadLocal threadSession = new ThreadLocal();
 
 	private static final ThreadLocal threadTransaction = new ThreadLocal();
 
 	private static final ThreadLocal threadInterceptor = new ThreadLocal();
 
	
 
 	static {
 
		
 		VOMSConfiguration conf = VOMSConfiguration.instance();
 
 		try {
 
 			Configuration hibernateConf = new AnnotationConfiguration();
 			hibernateConf.addProperties(conf.getDatabaseProperties());
 
 			sessionFactory = hibernateConf.configure().buildSessionFactory();
 
 		} catch (HibernateException e) {
 
 			log.error("Hibernate session factory creation failed!", e);
 			throw new ExceptionInInitializerError(e);
 
 		}
 
 	}
 
 	public static SessionFactory getFactory() {
 
 		return sessionFactory;
 	}
 
 	public static Session getSession() {
 
 		log.debug("Getting session for thread: " + Thread.currentThread());
 		Session s = (Session) threadSession.get();
 		try {
 
 			if (s == null) {
 
 				s = sessionFactory.openSession();
 				threadSession.set(s);
 
 				log.debug("Opening new session for thread "
 						+ Thread.currentThread());
 
 			}
 
 		} catch (HibernateException ex) {
 
 			log.error("Error getting hibernate session!", ex);
 			throw new VOMSFatalException(ex.getMessage(), ex);
 		}
 		return s;
 	}
 
 	public static void closeSession() {
 
 		try {
 
 			Session s = (Session) threadSession.get();
 			threadSession.set(null);
 
 			if (s != null && s.isOpen()) {
 				log.debug("Closing session for thread "
 						+ Thread.currentThread());
 				s.close();
 			}
 
 		} catch (HibernateException ex) {
 
 			log.error("Error closing hibernate session!", ex);
 			throw new VOMSDatabaseException(ex.getMessage(), ex);
 		}
 	}
 
 	public static void beginTransaction() {
 
 		Transaction tx = (Transaction) threadTransaction.get();
 		try {
 
 			if (tx == null) {
 				tx = getSession().beginTransaction();
 				threadTransaction.set(tx);
 			}
 		} catch (HibernateException ex) {
 
 			log.error("Error creating hibernate transaction!", ex);
 			throw new VOMSDatabaseException(ex.getMessage(), ex);
 
 		}
 	}
 
 	public static void commitTransaction() {
 
 		Transaction tx = (Transaction) threadTransaction.get();
 		try {
 
 			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack())
 				tx.commit();
 			threadTransaction.set(null);
 		} catch (HibernateException ex) {
 			rollbackTransaction();
 			log.error("Error committing hibernate transaction:"
 					+ ex.getMessage());
 
 			if (log.isDebugEnabled())
 				log.error("Error committing hibernate transaction!", ex);
 
 			throw new VOMSDatabaseException(ex.getMessage(), ex);
 		}
 	}
 
 	public static void rollbackTransaction() {
 
 		Transaction tx = (Transaction) threadTransaction.get();
 		try {
 
 			threadTransaction.set(null);
 			if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack())
 				tx.rollback();
 		} catch (HibernateException ex) {
 			log.error("Error rolling back hibernate transaction:"
 					+ ex.getMessage());
 
 			if (log.isDebugEnabled())
 				log.error("Error committing hibernate transaction!", ex);
 
 			throw new VOMSDatabaseException(ex.getMessage(), ex);
 
 		} finally {
 			closeSession();
 		}
 
 	}
 }
