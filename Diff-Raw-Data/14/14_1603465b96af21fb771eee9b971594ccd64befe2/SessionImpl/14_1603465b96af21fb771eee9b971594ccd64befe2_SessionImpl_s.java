 package org.gridlab.gridsphere.core.persistence.hibernate;
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.gridlab.gridsphere.core.persistence.Transaction;
 import org.gridlab.gridsphere.core.persistence.Session;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 
 import java.util.List;
 import java.util.Iterator;
 
 /**
  * Created by IntelliJ IDEA.
  * User: czhang
  * Date: Aug 26, 2004
  * Time: 5:51:20 PM
  * To change this template use Options | File Templates.
  */
 public class SessionImpl implements Session {
 
     private static PortletLog log = SportletLog.getInstance(SessionImpl.class);
 
     private net.sf.hibernate.Session hbSession = null;
 
     SessionImpl(net.sf.hibernate.Session hbSession) {
         this.hbSession = hbSession;
     }
     /**
 	 * Force the <tt>Session</tt> to flush. <tt>Transaction.commit()</tt> will calls
      * this method). <i>Flushing</i> is the process of synchronising the underlying
      *  persistent store with persistable state held in memory.
 	 *
 	 * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException
 	 */
 	public void flush() throws PersistenceManagerException {
         try {
             this.hbSession.flush();
         } catch (Exception e) {
             log.error("Unable to flush hibernate session", e);
             throw new PersistenceManagerException(e);
         }
     }
 
 	/**
 	 *  Create a new connection. This is used by applications which
 	 * require long transactions.
 	 *
 	 * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException
 	 */
 	public void reconnect() throws PersistenceManagerException {
         try {
             this.hbSession.reconnect();
         } catch (Exception e) {
             log.error("Unable to reconnect session", e);
             throw new PersistenceManagerException(e);
         }
     }
 
 	/**
 	 * End the <tt>Session</tt> by disconnecting from the JDBC connection and
 	 * cleaning up.
      *
 	 * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException
 	 */
 	public void close() throws PersistenceManagerException {
         try {
             this.hbSession.close();
         } catch (Exception e) {
             log.error("Unable to close hibernate session", e);
             throw new PersistenceManagerException(e);
         }
     }
 
 	/**
 	 * Check if the <tt>Session</tt> is still open.
 	 *
 	 * @return boolean
 	 */
 	public boolean isOpen() {
         return this.hbSession.isOpen();
     }
 
 	/**
 	 * Check if the <tt>Session</tt> is currently connected.
 	 *
 	 * @return boolean
 	 */
 	public boolean isConnected() {
         return this.hbSession.isConnected();
     }
 
 	/**
 	 * Does this <tt>Session</tt> contain any changes which must be
 	 * synchronized with the database? Would any SQL be executed if
 	 * we flushed this session?
 	 *
 	 * @return boolean
 	 */
 	public boolean isDirty() throws PersistenceManagerException {
         try {
             return this.hbSession.isDirty();
         } catch (Exception e) {
             log.error("Unable to check if hibernate session isdirty", e);
             throw new PersistenceManagerException(e);
         }
     }
 
     /**
      * Creates the given object in the permanent storage.
      *
      * @param object object to be saved
      * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException if the object could not be created
      */
     public void create(Object object) throws PersistenceManagerException {
         try {
             this.hbSession.save(object);
         } catch (Exception e) {
             log.error("Unable to save object in hibernate session", e);
             throw new PersistenceManagerException(e);
         }
     }
 
 
 	/**
 	 * <tt>Create()</tt> or <tt>update()</tt> the given instance, depending upon the value of
 	 * its identifier property. By default the instance is always saved. This behaviour may be
 	 * adjusted by specifying an <tt>unsaved-value</tt> attribute of the identifier property
 	 * mapping.
 	 *
 	 * @see org.gridlab.gridsphere.core.persistence.Session#create(java.lang.Object)
 	 * @param object a transient instance containing new or updated state
 	 * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException
 	 */
 	public void createOrUpdate(Object object) throws PersistenceManagerException {
         try {
             this.hbSession.saveOrUpdate(object);
         } catch (Exception e) {
             log.error("Unable to save or update object in hibernate session", e);
             throw new PersistenceManagerException(e);
         }
     }
 
 	/**
 	 * Update the persistent instance with the identifier of the given transient
 	 * instance. If there is a persistent instance with the same identifier,
 	 * an exception is thrown.<br>
 	 * <br>
 	 * If the given transient instance has a <tt>null</tt> identifier, an exception
 	 * will be thrown.<br>
 	 * <br>
 	 *
 	 * @param object a transient instance containing updated state
 	 * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException
 	 */
 	public void update(Object object) throws PersistenceManagerException {
         try {
             this.hbSession.update(object);
         } catch (Exception e) {
             log.error("Unable to update object in hibernate session", e);
             throw new PersistenceManagerException(e);
         }
     }
 
 	/**
 	 * Copy the state of the given object onto the persistent object with the same
 	 * identifier. If there is no persistent instance currently associated with
 	 * the session, it will be loaded. Return the persistent instance. If the
 	 * given instance is unsaved or does not exist in the database, save it and
 	 * return it as a newly persistent instance. Otherwise, the given instance
 	 * does not become associated with the session.
 	 *
 	 * @param object a transient instance with state to be copied
 	 * @return an updated persistent instance
 	 */
 	public Object createOrUpdateCopy(Object object) throws PersistenceManagerException {
         try {
             return this.hbSession.saveOrUpdateCopy(object);
         } catch (Exception e) {
             log.error("Unable to save or update object in hibernate session", e);
             throw new PersistenceManagerException(e);
         }
     }
 
 	/**
 	 * Remove a persistent instance from the datastore. The argument may be
 	 * an instance associated with the receiving <tt>Session</tt> or a transient
 	 * instance with an identifier associated with existing persistent state.
 	 *
 	 * @param object the instance to be removed
 	 * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException
 	 */
 	public void delete(Object object) throws PersistenceManagerException {
         try {
             this.hbSession.delete(object);
         } catch (Exception e) {
             log.error("Unable to delete object in hibernate session", e);
             throw new PersistenceManagerException(e);
         }
     }
 
 
 	/**
 	 * Delete all objects returned by the query. Return the number of objects deleted.
 	 *
 	 * @param query the query string
 	 * @return the number of instances deleted
 	 * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException
 	 */
 	public int delete(String query) throws PersistenceManagerException {
         try {
             return this.hbSession.delete(query);
         } catch (Exception e) {
             log.error("Unable to delete object in hibernate session", e);
             throw new PersistenceManagerException(e);
         }
     }
 
 	/**
 	 * Synchronize the state of the given instance with the underlying database.
 	 *
 	 * @param object a persistent or transient instance
 	 * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException
 	 */
 	public void refresh(Object object) throws PersistenceManagerException {
         try {
             this.hbSession.refresh(object);
         } catch (Exception e) {
             log.error("Unable to refresh object in hibernate session", e);
             throw new PersistenceManagerException(e);
         }
     }
 
 	/**
 	 * Begin a unit of work and return the associated <tt>Transaction</tt> object.
 	 * If a new underlying transaction is required, begin the transaction. Otherwise
 	 * continue the new work in the context of the existing underlying transaction.
 	 * The class of the returned <tt>Transaction</tt> object is determined by the
 	 * property <tt>hibernate.transaction_factory</tt>.
 	 *
 	 * @return a Transaction instance
 	 * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException
 	 * @see org.gridlab.gridsphere.core.persistence.Transaction
 	 */
 	public Transaction beginTransaction() throws PersistenceManagerException {
         try {
             return new TransactionImpl(this.hbSession.beginTransaction());
         } catch (Exception e) {
             log.error("Unable to begin transaction in hibernate session", e);
             throw new PersistenceManagerException(e);
         }
     }
 
     /**
       * Restores an object matching the query. If multiple are found the first
       * is returned.
 
       * @param query Query describing the object which should be restored
       * @return object Returns queried object or null if none was found
       * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException
       */
 
     public Object restore(String query) throws PersistenceManagerException {
         try {
             net.sf.hibernate.Query q = hbSession.createQuery(query);
             return q.list().get(0);
         } catch (Exception e) {
             log.error("Unable to retrieve object in hibernate session with query " + query, e);
             throw new PersistenceManagerException(e);
         }
     }
 
     /**
       * Restores objects from storage matching the query.
       *
       * @param query Query describing the objects
       * @return list List of objects from OQL query
       * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException If a persistence error occurs
       */
     public List restoreList(String query) throws PersistenceManagerException {
         try {
             net.sf.hibernate.Query q = hbSession.createQuery(query);
             return q.list();
         } catch (Exception e) {
             log.error("Unable to retrieve list in hibernate session with query " + query, e);
             throw new PersistenceManagerException(e);
         }
     }
 
     /**
 	 * Execute a query and return the results in an iterator. If the query has multiple
 	 * return values, values will be returned in an array of type <tt>Object[].</tt><br>
 	 * <br>
 	 * Entities returned as results are initialized on demand. The first SQL query returns
 	 * identifiers only. So <tt>iterate()</tt> is usually a less efficient way to retrieve
 	 * objects.
 	 *
 	 * @param query the query string
 	 * @return an iterator
 	 * @throws org.gridlab.gridsphere.core.persistence.PersistenceManagerException
 	 */
 	public Iterator iterate(String query) throws PersistenceManagerException {
          try {
             return this.hbSession.iterate(query);
         } catch (Exception e) {
              log.error("Unable to iterate objects in hibernate session with query " + query, e);
             throw new PersistenceManagerException(e);
         }
     }
 
 	/**
 	 * Completely clear the session. Evict all loaded instances and cancel all pending
 	 * saves, updates and deletions. Do not close open iterators or instances of
 	 * <tt>ScrollableResults</tt>.
 	 */
 	public void clear() {
         this.hbSession.clear();
     }
 }
