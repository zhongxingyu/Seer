 package org.gridlab.gridsphere.core.persistence.castor;
 
 /*
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @team sonicteam
  * @version $Id$
  *
  * RDBMS Implementation for castor.
  *
  */
 
 import org.exolab.castor.jdo.*;
 import org.exolab.castor.jdo.PersistenceException;
 import org.exolab.castor.mapping.MappingException;
 import org.exolab.castor.util.Logger;
 import org.gridlab.gridsphere.core.persistence.*;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfigProperties;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Properties;
 import java.io.FileReader;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.net.URL;
 
 
 /**
  * This class provides methods for storing/getting data to/from rdbms storage.
  */
 public class PersistenceManagerRdbms {
 
     protected transient static PortletLog log = SportletLog.getInstance(PersistenceManagerRdbms.class);
 
     private static PersistenceManagerRdbms instance = new PersistenceManagerRdbms();
     protected boolean databaseCreated = false;
     protected JDO jdo = null;
     protected String url = null;
 
     // @todo need to check settings for rollback !!
 
     private PersistenceManagerRdbms() {
         log.info("Entering PM");
 
         String databaseDir = GridSphereConfig.getProperty(GridSphereConfigProperties.GRIDSPHERE_DATABASE_DIR);
         String databaseConfigFile = GridSphereConfig.getProperty(GridSphereConfigProperties.GRIDSPHERE_DATABASE_CONFIG);
         String databaseName = GridSphereConfig.getProperty(GridSphereConfigProperties.GRIDSPHERE_DATABASE_NAME);
 
         url = databaseConfigFile;
         log.info("Using '" + databaseName + "' as Databasename with the configfile '" + url + "'");
 
         try {
             JDO.loadConfiguration(url);
         } catch (MappingException e) {
             log.error("Unable to get JDO configuration: " + url, e);
         }
 
         jdo = new JDO();
         //PrintWriter writer = new Logger(System.out).setPrefix("test");
         //jdo.setLogWriter(writer);
 
         jdo.setDatabaseName(databaseName);
         jdo.setTransactionManager(null);
     }
 
     public static PersistenceManagerRdbms getInstance() {
         return instance;
     }
 
     /**
      * Return the connectionURL
      *
      * @return filename
      */
     public String getConnectionURL() {
         return url;
     }
 
     /**
      * Creates an object to the database
      *
      * @param object to be marshalled
      * @throws ConfigurationException if configuration is wrong
      * @throws CreateException is creation went wrong
      */
     public void create(Object object) throws PersistenceManagerException {
 
         Database db = null;
 
        //SportletGroup sa = (SportletGroup)object;
        //log.info("Name: "+sa.getName());

         try {
             db = jdo.getDatabase();
             db.begin();
             db.create(object);
             db.commit();
            db.close();
         } catch (PersistenceException e) {
             log.error("PersistenceException " + e);
             throw new PersistenceManagerException("Persistence Error " + e);
         }
     }
 
     /**
      * Updates a given object
      *
      * @param object to be updated
      * @throws ConfigurationException if configurations are not set
      * @throws UpdateException if updated failed
      */
     public void update(Object object) throws PersistenceManagerException {
         Database db = null;
         // get Database
         try {
             db = jdo.getDatabase();
             db.begin();
             db.update(object);
             db.commit();
             db.close();
         } catch (PersistenceException e) {
             log.error("PersistenceException " + e);
             throw new PersistenceManagerException("Persistence Error: " + e);
         }
     }
 
     /**
      * restores objects from storage
      *
      * @throws ConfigurationException if configurations are not set
      * @throws RestoreException if restore failes for some reason
      * @return list of objects from OQL query
      */
     public List restoreList(String query) throws PersistenceManagerException {
         OQLQuery oql = null;
         Database db = null;
         QueryResults results = null;
         List list = new ArrayList();
 
         try {
             db = jdo.getDatabase();
             db.begin();
             oql = db.getOQLQuery(query);
             results = oql.execute();
             while (results.hasMore()) {
                 list.add(results.next());
             }
             db.commit();
             db.close();
         } catch (DatabaseNotFoundException e) {
             log.error("Exception! " + e);
             throw new PersistenceManagerException("Database not found: " + e);
         } catch (PersistenceException e) {
             log.error("PersistenceException!" + e);
             throw new PersistenceManagerException("Persistence Error: " + e);
         } catch (NoSuchElementException e) {
             log.error("NoSuchElementException!" + e);
             throw new PersistenceManagerException("No such element error: " + e);
         }
 
         return list;
     }
 
     /**
      * unmarshales the queried object
      *
      * @throws ConfigurationException if configuration failes
      * @throws RestoreException if restore failed
      * @return requested object defined by setQuery()
      */
     public Object restoreObject(String query) throws PersistenceManagerException {
         List resultList = restoreList(query);
         if (resultList.size() > 0) {
             return resultList.get(0);
         } else {
             return null;
         }
     }
 
     /**
      * deletes a the given object from storage
      *
      * @param object object to be deleted
      * @throws ConfigurationException if configurations are not set
      * @throws DeleteException if deletion failed
      */
 
     public void delete(Object object) throws PersistenceManagerException {
         Database db = null;
         try {
             db = jdo.getDatabase();
             db.begin();
 
             Class cl = object.getClass();
             String Oid = null;
 
             Method m = cl.getMethod("getOid", null);
 
             Oid = (String) m.invoke(object, null);
             log.debug("got " + Oid + " from object via reflection");
 
             Object deleteObject = db.load(object.getClass(), Oid);
 
             db.remove(deleteObject);
             db.commit();
             db.close();
 
         } catch (NoSuchMethodException e) {
             log.info("Exception " + e);
             throw new PersistenceManagerException("Mapping Error :" + e);
         } catch (SecurityException e) {
             log.info("Exception " + e);
             throw new PersistenceManagerException("Mapping Error :" + e);
         } catch (IllegalAccessException e) {
             log.info("Exception " + e);
             throw new PersistenceManagerException("Mapping Error :" + e);
         } catch (IllegalArgumentException e) {
             log.info("Exception " + e);
             throw new PersistenceManagerException("Mapping Error :" + e);
         } catch (InvocationTargetException e) {
             log.info("Exception " + e);
             throw new PersistenceManagerException("Mapping Error :" + e);
         } catch (PersistenceException e) {
             log.info("Exception " + e);
             throw new PersistenceManagerException("Mapping Error :" + e);
         }
     }
 
     /**
      * returns an object identified by class and object id
      *
      * @param cl class of the object
      * @param Oid object id of the object
      * @return requested object or null if not found
      */
     public Object getObjectByOid(Class cl, String Oid) throws PersistenceManagerException {
 
         Database db = null;
         Object object = null;
         try {
             db = jdo.getDatabase();
             db.begin();
             object = db.load(cl, Oid);
             db.commit();
         } catch (DatabaseNotFoundException e) {
             log.error("Exception! " + e);
             throw new PersistenceManagerException("Database not found: " + e);
         } catch (PersistenceException e) {
             log.error("Exception! " + e);
             throw new PersistenceManagerException("Persisetnce Exception: " + e);
         }
 
         return object;
 
     }
 
     /**
      * deletes objects which are matching against the query
      *
      * @param query oql query
      * @throws ConfigurationException
      * @throws DeleteException  if something went wrong during deletion
      */
     public void deleteList(String query) throws PersistenceManagerException {
 
         Database db = null;
         OQLQuery oql = null;
         QueryResults results = null;
 
         try {
             db = jdo.getDatabase();
 
             db.begin();
             oql = db.getOQLQuery(query);
             results = oql.execute();
             while (results.hasMore()) {
                 db.remove(results.next());
             }
             db.commit();
             db.close();
         } catch (DatabaseNotFoundException e) {
             log.error("Exception! " + e);
             throw new PersistenceManagerException("Database not found: " + e);
         } catch (PersistenceException e) {
             log.error("PersistenceException!" + e);
             throw new PersistenceManagerException("Persistence Error: " + e);
         } catch (NoSuchElementException e) {
             log.error("NoSuchElementException!" + e);
             throw new PersistenceManagerException("No such element error: " + e);
         }
     }
 
     public void deleteList(ParameterList list) throws PersistenceManagerException {
 
         Database db = null;
         OQLQuery oql = null;
         QueryResults results = null;
 
         try {
             db = jdo.getDatabase();
             db.begin();
 
             while (list.hasMore()) {
                 Parameter p = list.getNextParameter();
                 String query = "select " + p.getTable() + " from " + p.getTable() + " where " + p.getCondition();
 
                 oql = db.getOQLQuery(query);
                 results = oql.execute();
                 while (results.hasMore()) {
                     db.remove(results.next());
                 }
             }
 
             db.commit();
             db.close();
         } catch (PersistenceException e) {
             //db.rollback();
             log.error("PersistenceException!" + e);
             throw new PersistenceManagerException("Persistence Error: " + e);
         } catch (NoSuchElementException e) {
             //db.rollback();
             log.error("NoSuchElementException!" + e);
             throw new PersistenceManagerException("NoSuchElement Error: " + e);
         }
     }
 
 }
