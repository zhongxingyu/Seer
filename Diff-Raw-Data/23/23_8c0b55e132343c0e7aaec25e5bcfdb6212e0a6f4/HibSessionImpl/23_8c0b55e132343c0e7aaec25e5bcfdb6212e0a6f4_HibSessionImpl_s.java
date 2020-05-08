 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package org.bedework.timezones.common.db;
 
 import org.bedework.timezones.common.TzException;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Criteria;
 import org.hibernate.FlushMode;
 import org.hibernate.LockOptions;
 import org.hibernate.Query;
 import org.hibernate.ReplicationMode;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.StaleStateException;
 import org.hibernate.Transaction;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 /** Convenience class to do the actual hibernate interaction. Intended for
  * one use only.
  *
 * @author Mike Douglass douglm@rpi.edu
  */
 public class HibSessionImpl implements HibSession {
   transient Logger log;
 
   Session sess;
   transient Transaction tx;
   boolean rolledBack;
 
   transient Query q;
   transient Criteria crit;
 
   /** Exception from this session. */
   Throwable exc;
 
   private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
 
   /** Set up for a hibernate interaction. Throw the object away on exception.
    *
    * @param sessFactory
    * @param log
    * @throws TzException
    */
   @Override
   public void init(final SessionFactory sessFactory,
                    final Logger log) throws TzException {
     try {
       this.log = log;
       sess = sessFactory.openSession();
       rolledBack = false;
       //sess.setFlushMode(FlushMode.COMMIT);
 //      tx = sess.beginTransaction();
     } catch (Throwable t) {
       exc = t;
       tx = null;  // not even started. Should be null anyway
       close();
     }
   }
 
   @Override
   public Session getSession() throws TzException {
     return sess;
   }
 
   /**
    * @return boolean true if open
    * @throws TzException
    */
   @Override
   public boolean isOpen() throws TzException {
     try {
       if (sess == null) {
         return false;
       }
       return sess.isOpen();
     } catch (Throwable t) {
       handleException(t);
       return false;
     }
   }
 
   /** Clear a session
    *
    * @throws TzException
    */
   @Override
   public void clear() throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       sess.clear();
       tx =  null;
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** Disconnect a session
    *
    * @throws TzException
    */
   @Override
   public void disconnect() throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       if (exc instanceof TzException) {
         throw (TzException)exc;
       }
       throw new TzException(exc);
     }
 
     try {
       sess.disconnect();
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** set the flushmode
    *
    * @param val
    * @throws TzException
    */
   @Override
   public void setFlushMode(final FlushMode val) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       if (tx != null) {
         throw new TzException("Transaction already started");
       }
 
       sess.setFlushMode(val);
     } catch (Throwable t) {
       exc = t;
       throw new TzException(t);
     }
   }
 
   /** Begin a transaction
    *
    * @throws TzException
    */
   @Override
   public void beginTransaction() throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       if (tx != null) {
         throw new TzException("Transaction already started");
       }
 
       tx = sess.beginTransaction();
       rolledBack = false;
       if (tx == null) {
         throw new TzException("Transaction not started");
       }
     } catch (TzException cfe) {
       exc = cfe;
       throw cfe;
     } catch (Throwable t) {
       exc = t;
       throw new TzException(t);
     }
   }
 
   /** Return true if we have a transaction started
    *
    * @return boolean
    */
   @Override
   public boolean transactionStarted() {
     return tx != null;
   }
 
   /** Commit a transaction
    *
    * @throws TzException
    */
   @Override
   public void commit() throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
 //      if (tx != null &&
 //          !tx.wasCommitted() &&
 //          !tx.wasRolledBack()) {
         //if (getLogger().isDebugEnabled()) {
         //  getLogger().debug("About to comnmit");
         //}
       if (tx != null) {
         tx.commit();
       }
 
       tx = null;
     } catch (Throwable t) {
       exc = t;
 
       if (t instanceof StaleStateException) {
         throw new DbStaleStateException(t.getMessage());
       }
       throw new TzException(t);
     }
   }
 
   /** Rollback a transaction
    *
    * @throws TzException
    */
   @Override
   public void rollback() throws TzException {
 /*    if (exc != null) {
       // Didn't hear me last time?
       throw new WebdavException(exc);
     }
 */
     if (getLogger().isDebugEnabled()) {
       getLogger().debug("Enter rollback");
     }
     try {
       if ((tx != null) &&
           !tx.wasCommitted() &&
           !tx.wasRolledBack()) {
         if (getLogger().isDebugEnabled()) {
           getLogger().debug("About to rollback");
         }
         tx.rollback();
         //tx = null;
         clear();
         rolledBack = true;
       }
     } catch (Throwable t) {
       exc = t;
       throw new TzException(t);
     }
   }
 
   @Override
   public boolean rolledback() throws TzException {
     return rolledBack;
   }
 
   /** Create a Criteria ready for the additon of Criterion.
    *
    * @param cl           Class for criteria
    * @return Criteria    created Criteria
    * @throws TzException
    */
   @Override
   public Criteria createCriteria(final Class cl) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       crit = sess.createCriteria(cl);
       q = null;
 
       return crit;
     } catch (Throwable t) {
       handleException(t);
       return null;  // Don't get here
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calcorei.HibSession#evict(java.lang.Object)
    */
   @Override
   public void evict(final Object val) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       sess.evict(val);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calcorei.HibSession#createQuery(java.lang.String)
    */
   @Override
   public void createQuery(final String s) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q = sess.createQuery(s);
       crit = null;
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   @Override
   public void createNoFlushQuery(final String s) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q = sess.createQuery(s);
       crit = null;
       q.setFlushMode(FlushMode.COMMIT);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calcorei.HibSession#getQueryString()
    */
   @Override
   public String getQueryString() throws TzException {
     if (q == null) {
       return "*** no query ***";
     }
 
     try {
       return q.getQueryString();
     } catch (Throwable t) {
       handleException(t);
       return null;
     }
   }
 
   /** Create a sql query ready for parameter replacement or execution.
    *
    * @param s             String hibernate query
    * @param returnAlias
    * @param returnClass
    * @throws TzException
    */
   @Override
   public void createSQLQuery(final String s, final String returnAlias, final Class returnClass)
         throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       SQLQuery sq = sess.createSQLQuery(s);
       sq.addEntity(returnAlias, returnClass);
 
       q = sq;
       crit = null;
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** Create a named query ready for parameter replacement or execution.
    *
    * @param name         String named query name
    * @throws TzException
    */
   @Override
   public void namedQuery(final String name) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q = sess.getNamedQuery(name);
       crit = null;
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** Mark the query as cacheable
    *
    * @throws TzException
    */
   @Override
   public void cacheableQuery() throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q.setCacheable(true);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      String parameter value
    * @throws TzException
    */
   @Override
   public void setString(final String parName, final String parVal) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q.setString(parName, parVal);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      Date parameter value
    * @throws TzException
    */
   @Override
   public void setDate(final String parName, final Date parVal) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       // Remove any time component
       synchronized (dateFormatter) {
         q.setDate(parName, java.sql.Date.valueOf(dateFormatter.format(parVal)));
       }
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      boolean parameter value
    * @throws TzException
    */
   @Override
   public void setBool(final String parName, final boolean parVal) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q.setBoolean(parName, parVal);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      int parameter value
    * @throws TzException
    */
   @Override
   public void setInt(final String parName, final int parVal) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q.setInteger(parName, parVal);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      long parameter value
    * @throws TzException
    */
   @Override
   public void setLong(final String parName, final long parVal) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q.setLong(parName, parVal);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      Object parameter value
    * @throws TzException
    */
   @Override
   public void setEntity(final String parName, final Object parVal) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q.setEntity(parName, parVal);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calcorei.HibSession#setParameter(java.lang.String, java.lang.Object)
    */
   @Override
   public void setParameter(final String parName, final Object parVal) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q.setParameter(parName, parVal);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calcorei.HibSession#setParameterList(java.lang.String, java.util.Collection)
    */
   @Override
   public void setParameterList(final String parName, final Collection parVal) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q.setParameterList(parName, parVal);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calcorei.HibSession#setFirstResult(int)
    */
   @Override
   public void setFirstResult(final int val) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q.setFirstResult(val);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calcorei.HibSession#setMaxResults(int)
    */
   @Override
   public void setMaxResults(final int val) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       q.setMaxResults(val);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calcorei.HibSession#getUnique()
    */
   @Override
   public Object getUnique() throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       if (q != null) {
         return q.uniqueResult();
       }
 
       return crit.uniqueResult();
     } catch (Throwable t) {
       handleException(t);
       return null;  // Don't get here
     }
   }
 
   /** Return a list resulting from the query.
    *
    * @return List          list from query
    * @throws TzException
    */
   @Override
   public List getList() throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       List l;
       if (q != null) {
         l = q.list();
       } else {
         l = crit.list();
       }
 
       if (l == null) {
         return new ArrayList();
       }
 
       return l;
     } catch (Throwable t) {
       handleException(t);
       return null;  // Don't get here
     }
   }
 
   /**
    * @return int number updated
    * @throws TzException
    */
   @Override
   public int executeUpdate() throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       if (q == null) {
         throw new TzException("No query for execute update");
       }
 
       return q.executeUpdate();
     } catch (Throwable t) {
       handleException(t);
       return 0;  // Don't get here
     }
   }
 
   /** Update an object which may have been loaded in a previous hibernate
    * session
    *
    * @param obj
    * @throws TzException
    */
   @Override
   public void update(final Object obj) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       beforeSave(obj);
       sess.update(obj);
       deleteSubs(obj);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** Merge and update an object which may have been loaded in a previous hibernate
    * session
    *
    * @param obj
    * @return Object   the persistent object
    * @throws TzException
    */
   @Override
   public Object merge(Object obj) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       beforeSave(obj);
 
       obj = sess.merge(obj);
       deleteSubs(obj);
 
       return obj;
     } catch (Throwable t) {
       handleException(t, obj);
       return null;
     }
   }
 
   /** Save a new object or update an object which may have been loaded in a
    * previous hibernate session
    *
    * @param obj
    * @throws TzException
    */
   @Override
   public void saveOrUpdate(final Object obj) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       beforeSave(obj);
 
       sess.saveOrUpdate(obj);
       deleteSubs(obj);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** Copy the state of the given object onto the persistent object with the
    * same identifier. If there is no persistent instance currently associated
    * with the session, it will be loaded. Return the persistent instance.
    * If the given instance is unsaved or does not exist in the database,
    * save it and return it as a newly persistent instance. Otherwise, the
    * given instance does not become associated with the session.
    *
    * @param obj
    * @return Object
    * @throws TzException
    */
   @Override
   public Object saveOrUpdateCopy(final Object obj) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       return sess.merge(obj);
     } catch (Throwable t) {
       handleException(t);
       return null;  // Don't get here
     }
   }
 
   /** Return an object of the given class with the given id if it is
    * already associated with this session. This must be called for specific
    * key queries or we can get a NonUniqueObjectException later.
    *
    * @param  cl    Class of the instance
    * @param  id    A serializable key
    * @return Object
    * @throws TzException
    */
   @Override
   public Object get(final Class cl, final Serializable id) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       return sess.get(cl, id);
     } catch (Throwable t) {
       handleException(t);
       return null;  // Don't get here
     }
   }
 
   /** Return an object of the given class with the given id if it is
    * already associated with this session. This must be called for specific
    * key queries or we can get a NonUniqueObjectException later.
    *
    * @param  cl    Class of the instance
    * @param  id    int key
    * @return Object
    * @throws TzException
    */
   @Override
   public Object get(final Class cl, final int id) throws TzException {
     return get(cl, new Integer(id));
   }
 
   /** Save a new object.
    *
    * @param obj
    * @throws TzException
    */
   @Override
   public void save(final Object obj) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       beforeSave(obj);
       sess.save(obj);
       deleteSubs(obj);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /* * Save a new object with the given id. This should only be used for
    * restoring the db from a save or for assigned keys.
    *
    * @param obj
    * @param id
    * @throws WebdavException
    * /
   public void save(Object obj, Serializable id) throws WebdavException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new WebdavException(exc);
     }
 
     try {
       sess.save(obj, id);
     } catch (Throwable t) {
       handleException(t);
     }
   }*/
 
   /** Delete an object
    *
    * @param obj
    * @throws TzException
    */
   @Override
   public void delete(final Object obj) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       beforeDelete(obj);
 
       sess.delete(obj);
       deleteSubs(obj);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /** Save a new object with the given id. This should only be used for
    * restoring the db from a save.
    *
    * @param obj
    * @throws TzException
    */
   @Override
   public void restore(final Object obj) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       sess.replicate(obj, ReplicationMode.IGNORE);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calcorei.HibSession#reAttach(org.bedework.calfacade.base.BwUnversionedDbentity)
    */
   @Override
   public void reAttach(final TzDbentity<?> val) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       if (!val.unsaved()) {
         sess.buildLockRequest(LockOptions.NONE).lock(val);
       }
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /**
    * @param o
    * @throws TzException
    */
   @Override
   public void lockRead(final Object o) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       sess.buildLockRequest(LockOptions.READ).lock(o);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /**
    * @param o
    * @throws TzException
    */
   @Override
   public void lockUpdate(final Object o) throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     try {
       sess.buildLockRequest(LockOptions.UPGRADE).lock(o);
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /**
    * @throws TzException
    */
   @Override
   public void flush() throws TzException {
     if (exc != null) {
       // Didn't hear me last time?
       throw new TzException(exc);
     }
 
     if (getLogger().isDebugEnabled()) {
       getLogger().debug("About to flush");
     }
     try {
       sess.flush();
     } catch (Throwable t) {
       handleException(t);
     }
   }
 
   /**
    * @throws TzException
    */
   @Override
   public void close() throws TzException {
     if (sess == null) {
       return;
     }
 
 //    throw new WebdavException("XXXXXXXXXXXXXXXXXXXXXXXXXXXXX");/*
     try {
       if (sess.isDirty()) {
         sess.flush();
       }
       if ((tx != null) && !rolledback()) {
         tx.commit();
       }
     } catch (Throwable t) {
       if (exc == null) {
         exc = t;
       }
     } finally {
       tx = null;
       if (sess != null) {
         try {
           sess.close();
         } catch (Throwable t) {}
       }
     }
 
     sess = null;
     if (exc != null) {
       throw new TzException(exc);
     }
 //    */
   }
 
   private void handleException(final Throwable t) throws TzException {
     handleException(t, null);
   }
 
   private void handleException(final Throwable t,
                                final Object o) throws TzException {
     try {
       if (getLogger().isDebugEnabled()) {
         getLogger().debug("handleException called");
         if (o != null) {
           getLogger().debug(o.toString());
         }
         getLogger().error(this, t);
       }
     } catch (Throwable dummy) {}
 
     try {
       if (tx != null) {
         try {
           tx.rollback();
         } catch (Throwable t1) {
           rollbackException(t1);
         }
         tx = null;
       }
     } finally {
       try {
         sess.close();
       } catch (Throwable t2) {}
       sess = null;
     }
 
     exc = t;
 
     if (t instanceof StaleStateException) {
       throw new DbStaleStateException(t.getMessage());
     }
 
     throw new TzException(t);
   }
 
   private void beforeSave(final Object o) throws TzException {
     /*
     if (!(o instanceof TzDbentity)) {
       return;
     }
 
     TzDbentity ent = (TzDbentity)o;
 
     ent.beforeSave();
     */
   }
 
   private void beforeDelete(final Object o) throws TzException {
     /*
     if (!(o instanceof TzDbentity)) {
       return;
     }
 
     DbEntity ent = (DbEntity)o;
 
     ent.beforeDeletion();
     */
   }
 
   private void deleteSubs(final Object o) throws TzException {
     /*
     if (!(o instanceof DbEntity)) {
       return;
     }
 
     DbEntity ent = (DbEntity)o;
 
     Collection<DbEntity> subs = ent.getDeletedEntities();
     if (subs == null) {
       return;
     }
 
     for (DbEntity sub: subs) {
       delete(sub);
     }
     */
   }
 
   /** This is just in case we want to report rollback exceptions. Seems we're
    * likely to get one.
    *
    * @param t   Throwable from the rollback
    */
   private void rollbackException(final Throwable t) {
     if (getLogger().isDebugEnabled()) {
       getLogger().debug("HibSession: ", t);
     }
     getLogger().error(this, t);
   }
 
   protected Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 }
