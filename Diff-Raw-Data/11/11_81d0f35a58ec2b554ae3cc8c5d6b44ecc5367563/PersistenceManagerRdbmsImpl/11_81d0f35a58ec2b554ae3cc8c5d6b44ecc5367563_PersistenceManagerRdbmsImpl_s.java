 /*
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.core.persistence.hibernate;
 
 import net.sf.hibernate.*;
 import net.sf.hibernate.cfg.Configuration;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
 import org.gridlab.gridsphere.core.persistence.PersistenceSession;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
 
 import javax.servlet.ServletContext;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 
 /**
  *
  */
 public class PersistenceManagerRdbmsImpl implements PersistenceManagerRdbms {
     private static PortletLog log = SportletLog.getInstance(PersistenceManagerRdbmsImpl.class);
 
     private SessionFactory factory = null;
     private final static int CMD_DELETE = 1;
     private final static int CMD_DELETE_LIST = 2;
     private final static int CMD_RESTORE = 3;
     private final static int CMD_RESTORE_LIST = 4;
     private final static int CMD_UPDATE = 5;
     private final static int CMD_CREATE = 6;
 
 
     private String getPropertiesFile() {
         ServletContext ctx = GridSphereConfig.getServletContext();
         return ctx.getRealPath("/WEB-INF/persistence/hibernate.properties");
     }
 
     public PersistenceManagerRdbmsImpl() {
         ServletContext ctx = GridSphereConfig.getServletContext();
         String persistenceConfigDir = ctx.getRealPath("/WEB-INF/persistence/");
         log.info("Creating Hibernate RDBMS Impl using config in " + persistenceConfigDir);
         this.factory = getFactory(persistenceConfigDir);
     }
 
     public PersistenceManagerRdbmsImpl(String persistenceConfigDir) {
         log.info("Creating Hibernate RDBMS Impl using config in " + persistenceConfigDir);
         this.factory = getProjectFactory(persistenceConfigDir);
     }
 
     private SessionFactory getFactory(String persistenceConfigDir) {
         Properties hibernateProperties = loadProperties();
         Configuration cfg = loadConfiguration(persistenceConfigDir, hibernateProperties);
         SessionFactory factory = null;
 
         try {
             factory = cfg.buildSessionFactory();
         } catch (HibernateException e) {
             log.error("Could not instantiate Hibernate Factory " + e);
         }
         return factory;
     }
 
     private SessionFactory getProjectFactory(String persistenceConfigDir) {
 
         Properties hibernateProperties = null;
 
         String filename = persistenceConfigDir + File.separator + "hibernate.properties";
         File file = new File(filename);
 
         // if there is no prop file use the one from gridsphere
         if (!file.exists()) {
             hibernateProperties = loadProperties();
             log.info("PersistenceManager: no hibernate.properties defined, using the gridsphere defaults!");
         } else {
             hibernateProperties = loadProperties(filename);
         }
 
         Configuration cfg = loadConfiguration(persistenceConfigDir, hibernateProperties);
         SessionFactory factory = null;
 
         try {
             factory = cfg.buildSessionFactory();
         } catch (HibernateException e) {
             log.error("Could not instantiate Hibernate Factory " + e);
         }
         return factory;
     }
     /**
      * Load the mappingfiles from the given dirctory location
      * @param persistenceConfigDir
      * @return
      */
     private Configuration loadConfiguration(String persistenceConfigDir, Properties prop) {
         Configuration cfg = null;
         try {
             cfg = new Configuration();
             cfg.setProperties(prop);
             File mappingdir = new File(persistenceConfigDir);
             String[] children = mappingdir.list();
             if (children == null) {
                 // Either dir does not exist or is not a directory
             } else {
                 for (int i = 0; i < children.length; i++) {
                     // Get filename of file or directory
                     String filename = children[i];
                     if (filename.endsWith(".hbm.xml")) {
                         log.debug("add hbm file :" + persistenceConfigDir + File.separator + filename);
                         cfg.addFile(persistenceConfigDir + File.separator + filename);
                     }
                 }
             }
 
         } catch (MappingException e) {
             log.error("Could not load Hibernate mapping files " + e);
         }
 
         return cfg;
     }
 
     private Properties loadProperties(String file) {
         log.debug("Loading properties from :" + file);
         Properties prop = new Properties();
         try {
             FileInputStream fis = new FileInputStream(new File(file));
             prop.load(fis);
         } catch (FileNotFoundException e) {
             log.error("Could not find Hibernate config file. Make sure you have a file  " + file);
         } catch (IOException e) {
             log.error("Could not load Hibernate config File: " + e);
         }
         return prop;
 
     }
 
     /*
      * for now we just need to load the config from the basedir of gridsphere/WEB-INF/persistence
      */
     private Properties loadProperties() {
         ServletContext ctx = GridSphereConfig.getServletContext();
         String persistenceConfigDir = ctx.getRealPath("/WEB-INF/persistence/");
         return loadProperties(persistenceConfigDir + "/hibernate.properties");
     }
 
 
     public PersistenceSession getPersistenceSession() throws PersistenceManagerException {
         try {
             Session session = factory.openSession();
             PersistenceSession psi = new PersistenceSessionImpl(session);
             return psi;
         } catch (HibernateException e) {
             throw new PersistenceManagerException(e);
         }
     }
 
     public void create(Object object) throws PersistenceManagerException {
         try {
             doTransaction(object, "", CMD_CREATE);
         } catch (Exception e) {
             throw new PersistenceManagerException(e);
         }
     }
 
     public void update(Object object) throws PersistenceManagerException {
         try {
             doTransaction(object, "", CMD_UPDATE);
         } catch (Exception e) {
            throw new PersistenceManagerException();
         }
     }
 
     public Object restore(String query) throws PersistenceManagerException {
         List list = restoreList(query);
         if (list.size() == 0) {
             return null;
 //            throw new PersistenceManagerException("No object found with given query.");
         }
         return restoreList(query).get(0);
     }
 
 
     public List restoreList(String query) throws PersistenceManagerException {
         try {
             return (List) doTransaction(null, query, CMD_RESTORE_LIST);
         } catch (Exception e) {
            throw new PersistenceManagerException();
         }
     }
 
     public void delete(Object object) throws PersistenceManagerException {
         try {
             doTransaction(object, "", CMD_DELETE);
         } catch (Exception e) {
            throw new PersistenceManagerException();
         }
     }
 
     public void deleteList(String query) throws PersistenceManagerException {
         try {
             doTransaction(null, query, CMD_DELETE_LIST);
         } catch (Exception e) {
            throw new PersistenceManagerException();
         }
     }
 
 
     private Object doTransaction(Object object, String query, int command) throws Exception {
         Session session = null;
         Transaction tx = null;
         Object result = null;
         Query q = null;
         try {
             session = factory.openSession();
             tx = null;
             tx = session.beginTransaction();
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
                 case CMD_RESTORE_LIST:
                     q = session.createQuery(query);
                     result = q.list();
                     break;
                 case CMD_RESTORE:
                     q = session.createQuery(query);
                     result = q.list().get(0);
                     break;
             }
 
             tx.commit();
         } catch (Exception e) {
             if (tx != null) {
                 tx.rollback();
             }
             log.error("Could not complete transaction: " + e);
             e.printStackTrace();
             throw e;
         } finally {
             session.close();
         }
         return result;
     }
 
     public void destroy() throws PersistenceManagerException {
         try {
             factory.close();
         } catch (HibernateException e) {
             throw new PersistenceManagerException(e);
         }
     }
 }
