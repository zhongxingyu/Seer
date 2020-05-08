 package com.socialcomputing.wps.server.persistence.hibernate;
 
 import java.util.Collection;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.socialcomputing.utils.database.DatabaseHelper;
 import com.socialcomputing.utils.database.HibernateUtil;
 import com.socialcomputing.wps.server.persistence.Dictionary;
 import com.socialcomputing.wps.server.persistence.DictionaryManager;
 import com.socialcomputing.wps.server.plandictionary.WPSDictionary;
 
 public class DictionaryManagerImpl implements DictionaryManager {
 
     private static final Logger LOG = LoggerFactory.getLogger(DictionaryManagerImpl.class);
     
     @Override
     public Collection<Dictionary> findAll() {
         Collection<Dictionary> results = null;
         Session session = null;
         Transaction tx = null;
         try {
             session = HibernateUtil.currentSession();
             tx = session.beginTransaction();
             results = session.createQuery("from DictionaryImpl").list();
             tx.commit();
         }
         catch (HibernateException e) {
             // If a transaction was opened before the error occured
             if ( tx != null )
                 tx.rollback();
             LOG.error(e.getMessage(), e);
         }
         // Do not close session here yet
         //        finally {
         //            HibernateUtil.closeSession();
         //        }
         return results;
     }
 
     @Override
     public Dictionary findByName(String name) {
         Dictionary result = null;
         Session session = null;
         Transaction tx = null;
         try {
             session = HibernateUtil.currentSession();
             tx = session.beginTransaction();
             result = (Dictionary) session.get(DictionaryImpl.class, name);
             tx.commit();
         }
         catch (HibernateException e) {
             // If a transaction was opened before the error occured
             if ( tx != null )
                 tx.rollback();
             LOG.error(e.getMessage(), e);
         }
         // Do not close session here yet 
         // closed in jsp files
         //        finally {
         //            HibernateUtil.closeSession();
         //        }
         return result;
     }
 
     @Override
     public Dictionary create(String name, String definition) {
         Dictionary result = null;
         Session session = null;
         Transaction tx = null;
         try {
             session = HibernateUtil.currentSession();
             tx = session.beginTransaction();
             
             result = new DictionaryImpl(name, definition, "");
             session.save(result);
             String coefTable = WPSDictionary.getCoefficientTableName(name);
             String queueTable = WPSDictionary.getCoefficientQueuingTableName(name);
             session.createSQLQuery("create table "
                                            + coefTable
                                            + " (id1 varchar(255) not null, id2 varchar(255) not null, ponderation float)")
                     .executeUpdate();
             session.createSQLQuery("create index id1 on " + coefTable + " (id1 , ponderation)").executeUpdate();
             session.createSQLQuery("create index id2 on " + coefTable + " (id2 , ponderation)").executeUpdate();
             switch (DatabaseHelper.GetDbType(session.connection())) {
                 case DatabaseHelper.DB_MYSQL:
                     session.createSQLQuery("create table " + queueTable + " (id varchar(255) not null, date timestamp)")
                             .executeUpdate();
                     break;
                 case DatabaseHelper.DB_SQLSERVER:
                     session.createSQLQuery("create table " + queueTable
                             + " (id varchar(255) not null, date DATETIME DEFAULT (getdate()))").executeUpdate();
                     break;
                 case DatabaseHelper.DB_HSQL:
                     session.createSQLQuery("create table " + queueTable + " (id varchar(255) not null, date timestamp)").executeUpdate();
             }
             session.createSQLQuery("create index id on " + queueTable + " (id)").executeUpdate();
             tx.commit();
         }
         catch (Exception e) {
             // If a transaction was opened before the error occured
             if ( tx != null )
                 tx.rollback();
             LOG.error(e.getMessage(), e);
         }
         // Do not close session here yet 
         // closed in jsp files
         // finally {
         //     tx.commit();
         //     HibernateUtil.closeSession();
         // }
         return result;
     }
 
     
     @Override
     public void update(Dictionary dictionary) {
         Session session = null;
         Transaction tx = null;
         try {
             session = HibernateUtil.currentSession();
             tx = session.beginTransaction();
             session.update(dictionary);
             tx.commit();
         }
         catch (Exception e) {
             // If a transaction was opened before the error occured
             if ( tx != null )
                 tx.rollback();
             LOG.error(e.getMessage(), e);
         }
         // finally {
         //     tx.commit();
         //     HibernateUtil.closeSession();
         // }
     }
 
     @Override
     public void remove(String name) {
         Session session = null;
         Transaction tx = null;
         try {
             session = HibernateUtil.currentSession();
             tx = session.beginTransaction();
             Dictionary d = (Dictionary) session.get(DictionaryImpl.class, name);
             session.delete(d);
             session.createSQLQuery("drop table " + WPSDictionary.getCoefficientTableName(name)).executeUpdate();
             session.createSQLQuery("drop table " + WPSDictionary.getCoefficientQueuingTableName(name)).executeUpdate();
             tx.commit();
         }
         catch (Exception e) {
             // If a transaction was opened before the error occured
             if ( tx != null )
                 tx.rollback();
             LOG.error(e.getMessage(), e);
         }
         // finally {
         //     tx.commit();
         //     HibernateUtil.closeSession();
         // }
     }
 
 }
