 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.data.db.impl;
 
 import java.net.URL;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 import org.paxle.core.queue.ICommandProfile;
 import org.paxle.core.queue.ICommandProfileManager;
 
 public class CommandProfileDB implements ICommandProfileManager {
 	
 	/**
 	 * The logger
 	 */
 	private Log logger = LogFactory.getLog(this.getClass());	
 	
 	/**
 	 * The currently used db configuration
 	 */
 	private Configuration config; 	
 		
 	/**
 	 * The hibernate {@link SessionFactory}
 	 */
 	private SessionFactory sessionFactory;	
 	
 	/**
 	 * @param configURL an {@link URL} to the DB configuration
 	 * @param mappings a list of {@link URL} to the hibernate-mapping files to use
 	 */
 	public CommandProfileDB(URL configURL, List<URL> mappings) {
 		if (configURL == null) throw new NullPointerException("The URL to the hibernate config file is null.");
 		if (mappings == null) throw new NullPointerException("The list of mapping files was null.");
 		
 		try {
 			
 			/* ===========================================================================
 			 * Init Hibernate
 			 * =========================================================================== */
 			try {
 				Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
 				
 				// Read the hibernate configuration from *.cfg.xml
 				this.logger.info(String.format("Loading DB configuration from URL '%s'.",configURL));
 				this.config = new Configuration().configure(configURL);
 				
 				// register an interceptor (required to support our interface-based command model)
 				this.config.setInterceptor(new InterfaceInterceptor());
 				
 				// configure caching
 				this.config.setProperty("hibernate.cache.provider_class", "net.sf.ehcache.hibernate.SingletonEhCacheProvider");
 				
 				// post-processing of read properties
 				ConnectionUrlTool.postProcessProperties(this.config);				
 				
 				// load the various mapping files
 				for (URL mapping : mappings) {
 					if (this.logger.isDebugEnabled()) this.logger.debug(String.format("Loading mapping file from URL '%s'.",mapping));
 					this.config.addURL(mapping);
 				}
 				
 				// String[] sql = this.config.generateSchemaCreationScript( new org.hibernate.dialect.MySQLDialect());
 				
 				// create the session factory
 				this.sessionFactory = this.config.buildSessionFactory();
 			} catch (Throwable ex) {
 				// Make sure you log the exception, as it might be swallowed
 				this.logger.error("Initial SessionFactory creation failed.",ex);
 				throw new ExceptionInInitializerError(ex);
 			}
 
 		} catch (Throwable e) {
 			this.logger.error(String.format(
 					"Unexpected '%s' while initializing command-profile DB",
 					e.getClass().getName()
 			),e);
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * @see ICommandProfileManager#getProfileByID(int)
 	 */
 	public ICommandProfile getProfileByID(int profileID) {		
 		Session session = null;
 		Transaction transaction = null;
 		final Integer profileIDInt = Integer.valueOf(profileID);
 		try {
 			// open session and transaction
 			session = this.sessionFactory.openSession();
 			transaction = session.beginTransaction();
 			
 			// load profile
 			ICommandProfile profile = (ICommandProfile) session.load(ICommandProfile.class, profileIDInt);
 			transaction.commit();
 			return profile;
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
			this.logger.error(String.format("Error while reading profile '%d' from db.", profileIDInt),e);
 			throw e;
 		} finally {
 			// closing session
 			if (session != null) try { session.close(); } catch (Exception e) { 
 				this.logger.error(String.format("Unexpected '%s' while closing session.", e.getClass().getName()), e);
 			}			
 		}
 	}
 	
 	/**
 	 * @see ICommandProfileManager#storeProfile(ICommandProfile)
 	 */
 	public void storeProfile(ICommandProfile profile) {
 		if (profile == null) throw new NullPointerException("Profile was null");
 		
 		Session session = null;
 		Transaction transaction = null;
 		try {
 			// open session and transaction
 			session = sessionFactory.openSession();
 			transaction = session.beginTransaction();
 			
 			// store profile
 			session.saveOrUpdate(profile);			
 			transaction.commit();		
 		} catch (HibernateException e) {
 			if (transaction != null && transaction.isActive()) transaction.rollback(); 
 			this.logger.error(String.format("Error while writing profile '%s' to db.", profile.getName()),e);
 			throw e;
 		} finally {
 			// closing session
 			if (session != null) try { session.close(); } catch (Exception e) { 
 				this.logger.error(String.format("Unexpected '%s' while closing session.", e.getClass().getName()), e);
 			}			
 		}
 	}
 	
 	public void close() throws InterruptedException {
 		try {		
 			// close the DB
 			this.sessionFactory.close();
 		} catch (Throwable e) {
 			this.logger.error(String.format(
 					"Unexpected '%s' while tryping to shutdown %s: %s",
 					e.getClass().getName(),
 					this.getClass().getSimpleName(),
 					e.getMessage()
 			),e);
 		}		
 	}
 }
