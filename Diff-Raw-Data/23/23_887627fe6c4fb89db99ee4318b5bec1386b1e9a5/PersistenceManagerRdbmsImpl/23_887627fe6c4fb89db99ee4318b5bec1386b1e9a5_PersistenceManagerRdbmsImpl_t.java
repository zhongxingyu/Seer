 /*
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id: PersistenceManagerRdbmsImpl.java 4877 2006-06-26 23:13:40Z novotny $
  */
 package org.gridsphere.services.core.persistence.impl;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.gridsphere.services.core.persistence.PersistenceManagerException;
 import org.gridsphere.services.core.persistence.PersistenceManagerRdbms;
 import org.gridsphere.services.core.persistence.QueryFilter;
 import org.hibernate.*;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.stat.Statistics;
 
 import javax.servlet.ServletContext;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
 
 public class PersistenceManagerRdbmsImpl implements PersistenceManagerRdbms {
     private transient Log log = LogFactory.getLog(PersistenceManagerRdbmsImpl.class);
 
     private ThreadLocal<Session> sessionThread = new ThreadLocal<Session>();
 
     private SessionFactory factory = null;
 
     private ClassLoader classLoader;
 
     private final static int CMD_DELETE = 1;
     private final static int CMD_DELETE_LIST = 2;
     private final static int CMD_RESTORE = 3;
     private final static int CMD_RESTORE_LIST = 4;
     private final static int CMD_UPDATE = 5;
     private final static int CMD_CREATE = 6;
     private final static int CMD_SAVEORUPDATE = 7;
     private final static int CMD_COUNT = 8;
 
     private String pm = null;
 
     public void setClassLoader(ClassLoader loader) {
         this.classLoader = loader;
     }
 
     public PersistenceManagerRdbmsImpl(ServletContext context) {
         Properties prop = new Properties();
         pm = "gridsphere";
         //pm = context.getRealPath("");
         String mappingPath = context.getRealPath("/WEB-INF/persistence");
         try {
            String hibPath = context.getRealPath("/WEB-INF/CustomPortal/database/hibernate.properties");
            prop.load(new FileInputStream(hibPath));
             Configuration cfg = loadConfiguration(mappingPath, prop);
             factory = cfg.buildSessionFactory();
         } catch (IOException e) {
             log.error("Unable to load file: " + context.getRealPath("/WEB-INF/CustomPortal/database/hibernate.properties"));
         } catch (HibernateException e) {
             log.error("Could not instantiate Hibernate Factory", e);
         }
         log.info("Creating Hibernate RDBMS Impl using config in " + context.getRealPath("/WEB-INF/CustomPortal/database/hibernate.properties"));
     }
 
     public PersistenceManagerRdbmsImpl(String persistenceConfigDir) {
         this(persistenceConfigDir, persistenceConfigDir);
     }
 
     public PersistenceManagerRdbmsImpl(String pathTohibernateProperties, String pathToHibernateMappings) {
         log.info("Creating Hibernate RDBMS Impl using config in " + pathTohibernateProperties);
         String filename = pathTohibernateProperties + File.separator + "hibernate.properties";
         File file = new File(filename);
         log.debug("Loading properties from :" + file);
         Properties hibernateProperties = new Properties();
         try {
             FileInputStream fis = new FileInputStream(file);
             hibernateProperties.load(fis);
             Configuration cfg = loadConfiguration(pathToHibernateMappings, hibernateProperties);
             factory = cfg.buildSessionFactory();
         } catch (FileNotFoundException e) {
             log.error("Could not find Hibernate config file. Make sure you have a file " + file);
         } catch (IOException e) {
             log.error("Could not load Hibernate config File", e);
         } catch (HibernateException e) {
             log.error("Could not instantiate Hibernate Factory", e);
         }
     }
 
     /**
      * Load the mappingfiles from the given dirctory location
      *
      * @param mappingPath         the file path to find hibernate mapping files
      * @param hibernateProperties the hibernate properties
      * @return a hibernate configuration object
      */
     private Configuration loadConfiguration(String mappingPath, Properties hibernateProperties) {
         Configuration cfg = null;
         try {
             cfg = new Configuration();
             cfg.setProperties(hibernateProperties);
             File mappingdir = new File(mappingPath);
             String[] children = mappingdir.list();
 
             if (children != null) {
                 // Create list from children array
                 List<String> filenameList = Arrays.asList(children);
                 // Ensure that this list is sorted alphabetically
                 Collections.sort(filenameList);
                for (String aFilenameList : filenameList) {
                    String filename = aFilenameList;
                     if (filename.endsWith(".hbm.xml")) {
                         // Get filename of file or directory
                         log.debug("add hbm file :" + mappingPath + File.separator + filename);
                         cfg.addFile(mappingPath + File.separator + filename);
                     }
                 }
             }
 
         } catch (MappingException e) {
             log.error("Could not load Hibernate mapping files", e);
         }
 
         return cfg;
     }
 
     public void create(Object object) throws PersistenceManagerException {
         try {
             doTransaction(object, "", CMD_CREATE, null);
         } catch (HibernateException e) {
             throw new PersistenceManagerException(e);
         }
     }
 
     public void update(Object object) throws PersistenceManagerException {
         try {
             doTransaction(object, "", CMD_UPDATE, null);
         } catch (HibernateException e) {
             throw new PersistenceManagerException(e);
         }
     }
 
     public void saveOrUpdate(Object object) throws PersistenceManagerException {
         try {
             doTransaction(object, "", CMD_SAVEORUPDATE, null);
         } catch (HibernateException e) {
             throw new PersistenceManagerException(e);
         }
     }
 
     public Object restore(String query) throws PersistenceManagerException {
         List list = restoreList(query);
         if (list.size() == 0) return null;
         return restoreList(query).get(0);
     }
 
 
     public List restoreList(String query) throws PersistenceManagerException {
         try {
             return (List) doTransaction(null, query, CMD_RESTORE_LIST, null);
         } catch (HibernateException e) {
             throw new PersistenceManagerException(e);
         }
     }
 
     public List restoreList(String query, QueryFilter queryFilter) throws PersistenceManagerException {
         try {
             return (List) doTransaction(null, query, CMD_RESTORE_LIST, queryFilter);
         } catch (HibernateException e) {
             throw new PersistenceManagerException(e);
         }
     }
 
     public int count(String query) throws PersistenceManagerException {
         try {
             Long i = (Long) doTransaction(null, query, CMD_COUNT, null);
             return i.intValue();
         } catch (HibernateException e) {
             throw new PersistenceManagerException(e);
         }
     }
 
     public void delete(Object object) throws PersistenceManagerException {
         try {
             doTransaction(object, "", CMD_DELETE, null);
         } catch (HibernateException e) {
             throw new PersistenceManagerException(e);
         }
     }
 
     public void deleteList(String query) throws PersistenceManagerException {
         try {
             doTransaction(null, query, CMD_DELETE_LIST, null);
         } catch (HibernateException e) {
             throw new PersistenceManagerException(e);
         }
     }
 
     private Object doTransaction(Object object, String query, int command, QueryFilter queryFilter) throws HibernateException {
         ClassLoader currentloader = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(classLoader);
         Object result = null;
        Query q;
         Session session = currentSession();
         switch (command) {
             case CMD_CREATE:
                 session.save(object);
                 break;
             case CMD_DELETE:
                 session.delete(object);
                 break;
             case CMD_DELETE_LIST:
                 session.delete(query);
                 break;
             case CMD_UPDATE:
                 session.update(object);
                 break;
             case CMD_SAVEORUPDATE:
                 session.saveOrUpdate(object);
                 break;
             case CMD_RESTORE_LIST:
 
                 q = session.createQuery(query);
                 if (queryFilter != null) {
                     q.setFirstResult(queryFilter.getFirstResult());
                     q.setMaxResults(queryFilter.getMaxResults());
                 }
                 result = q.list();
                 break;
             case CMD_RESTORE:
                 q = session.createQuery(query);
                 result = q.list().get(0);
                 break;
             case CMD_COUNT:
                 q = session.createQuery(query);
                result = q.uniqueResult();
                 break;
         }
         Thread.currentThread().setContextClassLoader(currentloader);
         return result;
     }
 
 
     public Session currentSession() throws HibernateException {
        Session session = sessionThread.get();
         if (session == null) {
             session = factory.openSession();
             sessionThread.set(session);
         }
         return session;
     }
 
     public void beginTransaction() {
         log.debug("begin transaction");
         currentSession().beginTransaction();
     }
 
     public void endTransaction() {
         log.debug("end transaction");
         currentSession().getTransaction().commit();
         currentSession().close();
         sessionThread.set(null);
     }
 
     public void rollbackTransaction() {
         if (currentSession().getTransaction().isActive()) {
             log.debug("Trying to rollback database transaction after exception");
             currentSession().getTransaction().rollback();
         }
     }
 
     public void destroy() {
         System.err.println("destroying pm! " + pm);
         Statistics stats = factory.getStatistics();
         stats.logSummary();
         factory.close();
         //session = null;
     }
 
 }
