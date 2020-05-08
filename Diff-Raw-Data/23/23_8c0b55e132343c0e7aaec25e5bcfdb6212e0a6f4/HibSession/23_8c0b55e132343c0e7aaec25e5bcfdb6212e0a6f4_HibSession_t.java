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
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 /** Interface to do hibernate interactions.
  *
 * @author Mike Douglass douglm at bedework.edu
  */
 public interface HibSession extends Serializable {
   /** Set up for a hibernate interaction. Throw the object away on exception.
    *
    * @param sessFactory
    * @param log
    * @throws TzException
    */
   public void init(SessionFactory sessFactory,
                    Logger log) throws TzException;
 
   /**
    * @return Session
    * @throws TzException
    */
   public Session getSession() throws TzException;
 
   /**
    * @return boolean true if open
    * @throws TzException
    */
   public boolean isOpen() throws TzException;
 
   /** Clear a session
    *
    * @throws TzException
    */
   public void clear() throws TzException;
 
   /** Disconnect a session
    *
    * @throws TzException
    */
   public void disconnect() throws TzException;
 
   /** set the flushmode
    *
    * @param val
    * @throws TzException
    */
   public void setFlushMode(FlushMode val) throws TzException;
 
   /** Begin a transaction
    *
    * @throws TzException
    */
   public void beginTransaction() throws TzException;
 
   /** Return true if we have a transaction started
    *
    * @return boolean
    */
   public boolean transactionStarted();
 
   /** Commit a transaction
    *
    * @throws TzException
    */
   public void commit() throws TzException;
 
   /** Rollback a transaction
    *
    * @throws TzException
    */
   public void rollback() throws TzException;
 
   /** Did we rollback the transaction?
    *
    * @return boolean
    * @throws TzException
    */
   public boolean rolledback() throws TzException;
 
   /** Create a Criteria ready for the additon of Criterion.
    *
    * @param cl           Class for criteria
    * @return Criteria    created Criteria
    * @throws TzException
    */
   public Criteria createCriteria(Class<?> cl) throws TzException;
 
   /** Evict an object from the session.
    *
    * @param val          Object to evict
    * @throws TzException
    */
   public void evict(Object val) throws TzException;
 
   /** Create a query ready for parameter replacement or execution.
    *
    * @param s             String hibernate query
    * @throws TzException
    */
   public void createQuery(String s) throws TzException;
 
   /** Create a query ready for parameter replacement or execution and flag it
    * for no flush. This assumes that any queued changes will not affect the
    * result of the query.
    *
    * @param s             String hibernate query
    * @throws TzException
    */
   public void createNoFlushQuery(String s) throws TzException;
 
   /**
    * @return query string
    * @throws TzException
    */
   public String getQueryString() throws TzException;
 
   /** Create a sql query ready for parameter replacement or execution.
    *
    * @param s             String hibernate query
    * @param returnAlias
    * @param returnClass
    * @throws TzException
    */
   public void createSQLQuery(String s, String returnAlias, Class<?> returnClass)
         throws TzException;
 
   /** Create a named query ready for parameter replacement or execution.
    *
    * @param name         String named query name
    * @throws TzException
    */
   public void namedQuery(String name) throws TzException;
 
   /** Mark the query as cacheable
    *
    * @throws TzException
    */
   public void cacheableQuery() throws TzException;
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      String parameter value
    * @throws TzException
    */
   public void setString(String parName, String parVal) throws TzException;
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      Date parameter value
    * @throws TzException
    */
   public void setDate(String parName, Date parVal) throws TzException;
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      boolean parameter value
    * @throws TzException
    */
   public void setBool(String parName, boolean parVal) throws TzException;
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      int parameter value
    * @throws TzException
    */
   public void setInt(String parName, int parVal) throws TzException;
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      long parameter value
    * @throws TzException
    */
   public void setLong(String parName, long parVal) throws TzException;
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      Object parameter value
    * @throws TzException
    */
   public void setEntity(String parName, Object parVal) throws TzException;
 
   /** Set the named parameter with the given value
    *
    * @param parName     String parameter name
    * @param parVal      Object parameter value
    * @throws TzException
    */
   public void setParameter(String parName, Object parVal) throws TzException ;
 
   /** Set the named parameter with the given Collection
    *
    * @param parName     String parameter name
    * @param parVal      Collection parameter value
    * @throws TzException
    */
   public void setParameterList(String parName,
                                Collection<?> parVal) throws TzException ;
 
   /** Set the first result for a paged batch
    *
    * @param val      int first index
    * @throws TzException
    */
   public void setFirstResult(int val) throws TzException;
 
   /** Set the max number of results for a paged batch
    *
    * @param val      int max number
    * @throws TzException
    */
   public void setMaxResults(int val) throws TzException;
 
   /** Return the single object resulting from the query.
    *
    * @return Object          retrieved object or null
    * @throws TzException
    */
   public Object getUnique() throws TzException;
 
   /** Return a list resulting from the query.
    *
    * @return List          list from query
    * @throws TzException
    */
   public List getList() throws TzException;
 
   /**
    * @return int number updated
    * @throws TzException
    */
   public int executeUpdate() throws TzException;
 
   /** Update an object which may have been loaded in a previous hibernate
    * session
    *
    * @param obj
    * @throws TzException
    */
   public void update(Object obj) throws TzException;
 
   /** Merge and update an object which may have been loaded in a previous hibernate
    * session
    *
    * @param obj
    * @return Object   the persiatent object
    * @throws TzException
    */
   public Object merge(Object obj) throws TzException;
 
   /** Save a new object or update an object which may have been loaded in a
    * previous hibernate session
    *
    * @param obj
    * @throws TzException
    */
   public void saveOrUpdate(Object obj) throws TzException;
 
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
   public Object saveOrUpdateCopy(Object obj) throws TzException;
 
   /** Return an object of the given class with the given id if it is
    * already associated with this session. This must be called for specific
    * key queries or we can get a NonUniqueObjectException later.
    *
    * @param  cl    Class of the instance
    * @param  id    A serializable key
    * @return Object
    * @throws TzException
    */
   public Object get(Class cl, Serializable id) throws TzException;
 
   /** Return an object of the given class with the given id if it is
    * already associated with this session. This must be called for specific
    * key queries or we can get a NonUniqueObjectException later.
    *
    * @param  cl    Class of the instance
    * @param  id    int key
    * @return Object
    * @throws TzException
    */
   public Object get(Class cl, int id) throws TzException;
 
   /** Save a new object.
    *
    * @param obj
    * @throws TzException
    */
   public void save(Object obj) throws TzException;
 
   /** Delete an object
    *
    * @param obj
    * @throws TzException
    */
   public void delete(Object obj) throws TzException;
 
   /** Save a new object with the given id. This should only be used for
    * restoring the db from a save.
    *
    * @param obj
    * @throws TzException
    */
   public void restore(Object obj) throws TzException;
 
   /**
    * @param val
    * @throws TzException
    */
   public void reAttach(TzDbentity<?> val) throws TzException;
 
   /**
    * @param o
    * @throws TzException
    */
   public void lockRead(Object o) throws TzException;
 
   /**
    * @param o
    * @throws TzException
    */
   public void lockUpdate(Object o) throws TzException;
 
   /**
    * @throws TzException
    */
   public void flush() throws TzException;
 
   /**
    * @throws TzException
    */
   public void close() throws TzException;
 }
