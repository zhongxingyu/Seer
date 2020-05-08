 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.persistence.locking;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.LockModeType;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceException;
 import javax.persistence.Query;
 import javax.persistence.TemporalType;
 
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.persistence.PMAlreadyLockedException;
 import org.eclipse.jubula.client.core.persistence.PMDirtyVersionException;
 import org.eclipse.jubula.client.core.persistence.PMObjectDeletedException;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.JBFatalAbortException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created 30.11.2005
  */
 public final class LockManager {
     /**
      * <code>DB_GUARD_ID</code> Id of the DB lock entity. This MUST be 1.
      */
     private static final Long DB_GUARD_ID = Long.valueOf(1);
     
     /**
      * @author BREDEX GmbH
      * @created 01.12.2005
      */
     public interface DBRunnable {
         /**
          * @param sess sess
          * @return Result
          */
         public Result run(EntityManager sess);
     }
     
     /** result codes */
     private enum Result { OK, FAILED, OBJECT_DIRTY, OBJECT_DELETED }
     
     /** update interval for the application timestamp (in seconds) */
     private static final int UPDATE_TIMESTAMP_SECS = 60;
     /** time after which a application is considered dead (in seconds) */
     private static final int TIMEOUT_APPLICATION = 3 * UPDATE_TIMESTAMP_SECS;
 
     /** standard logging */
     private static Logger log = LoggerFactory.getLogger(LockManager.class);
 
     /** singleton instance */
     private static LockManager instance = null;
     
     /** the instance used for locking the complete data of the subsystem */
     private DbGuardPO m_dbGuard;
     
     /**
      * The representation of the running application in the locking 
      * subsystem. 
      */
     private ApplicationPO m_application;
     
     /** thread which periodically updates the applications timestamp */
     private Thread m_keepAliveThread;
     
     /** shall the keepAliveThread continue */
     private boolean m_keepRunning = true;
     
     /** used by instance() */
     private LockManager() {
         initDbObjects();
     }
     
     /**
      * Read the guardian row. This instance will be used for prohibiting
      * parallel modifications in the lock tables.
      * Create and persist the application object.
      */
     private void initDbObjects() {
         EntityManager sess = null;
         
         try {
             sess = Persistor.instance().openSession();
             EntityTransaction tx = sess.getTransaction();
             tx.begin();
             m_dbGuard = sess.find(DbGuardPO.class, DB_GUARD_ID);
             m_application = new ApplicationPO(Long.MIN_VALUE);           
             sess.persist(m_application);           
             
             tx.commit();
         } catch (PersistenceException e) {
             throw new JBFatalAbortException(Messages.LockingWontStart, e,
                 MessageIDs.E_DATABASE_GENERAL);
         } finally {
             Persistor.instance().dropSessionWithoutLockRelease(sess);
         }
         updateTimestamp();
     }
 
     /**
      * Runs an action inside a transaction
      * @param action Runnable holding the transaction
      * @return the return value of action
      */
     private synchronized Result runInSession(DBRunnable action) {
         EntityManager sess = null;
         EntityTransaction tx = null;
         Result result = Result.FAILED;
         try {
             sess = Persistor.instance().openSession();
             tx = sess.getTransaction();
             tx.begin();
             lockDB(sess);
 
             result = action.run(sess);
             
             tx.commit();
             tx = null;
         } catch (PersistenceException e) {
             log.error(Messages.FailedToUpdateApplicationTimestamp, e);
         } finally {
             if (tx != null) {
                 tx.rollback();
             }
             Persistor.instance().dropSessionWithoutLockRelease(sess);
         }
         return result;
     }
 
     /** singleton getter
      * @return the only instance of the LockManager 
      */
     public static LockManager instance() {
         if (instance == null) {
             instance = new LockManager();            
         }
         return instance;
     }
     
     /**
      * Updates the timestamp of the application by executing a dml statement. 
      * Note that the instance must be refreshed after the update since HQL
      * bulk queries don't synchronize the in memory state of POJOs.
      */
     void updateTimestamp() {
         runInSession(new DBRunnable() {
 
             public Result run(EntityManager sess) {
                 Query q = sess.createQuery(
                     "update ApplicationPO app set app.timestamp = CURRENT_TIMESTAMP where app.id = :id"); //$NON-NLS-1$
                 q.setParameter("id", m_application.getId()); //$NON-NLS-1$
                 if (q.executeUpdate() != 1) {
                     log.error(Messages.UpdateOfTimestampFailed);
                 }
                 return Result.OK;
             }
         });
         runInSession(new DBRunnable() {
 
             @SuppressWarnings("synthetic-access")
             public Result run(EntityManager sess) {
                 sess.detach(m_application);
                 m_application = sess.find(ApplicationPO.class, 
                         m_application.getId());
                 return Result.OK;
             }
         });
     }
     
     /**
      * Find timed out application in the DB, if found remove any locks and the
      * application itself.
      * 
      */
     @SuppressWarnings("unchecked")
     void checkForTimeouts() {
         runInSession(new DBRunnable() {
 
             public Result run(EntityManager sess) {
                 Query deadQuery = 
                     sess.createQuery("select app from ApplicationPO app where app.timestamp < :deadTime"); //$NON-NLS-1$
                 Date deadTime = new Date(m_application.getTimestamp().getTime()
                     - TIMEOUT_APPLICATION * 1000);
                 deadQuery.setParameter("deadTime", deadTime, TemporalType.TIMESTAMP); //$NON-NLS-1$
                 List<ApplicationPO> deadApps = deadQuery.getResultList();
                 for (ApplicationPO appl : deadApps) {
                     removeApp(sess, appl);
                 }
                 return Result.OK;
             }
         });
     }
 
     /**
      * @param sess working session to perform lock in
      * @throws PersistenceException in case of db error, not expected
      */
     public void lockDB(EntityManager sess) throws PersistenceException {
         m_dbGuard = sess.find(DbGuardPO.class, m_dbGuard.getId());
         sess.lock(m_dbGuard, LockModeType.PESSIMISTIC_WRITE);
     }
     /**
      * Starts a thread which update the timestamp of the application 
      * periodically.
      */
     public synchronized void startKeepAlive() {
         if (m_keepAliveThread == null) {
             m_keepAliveThread = new Thread(new Runnable() {
                 
                 public void run() {
                     while (m_keepRunning) {
                        // The timestamp is maintained in the m_application
                        // instance, therefore the order of the following
                         // statements is crucial.
                         updateTimestamp();
                         checkForTimeouts();
                         try {
                             Thread.sleep(UPDATE_TIMESTAMP_SECS * 1000);
                         } catch (InterruptedException e) {
                             // just ignore
                         }
                     }
                     
                 }
                 
             }, "LockManger.KeepAlive"); //$NON-NLS-1$
             m_keepAliveThread.start();
         } else {
             log.warn(Messages.KeepAliveAlreadyActive + StringConstants.DOT);
         }
     }
     
     /**
      * shutdown the locking subsystems, remove any remaining locks and the 
      * application from the db.
      * Discard the singleton instance.
      *
      */
     public synchronized void dispose() {
         m_keepRunning = false;
         m_keepAliveThread.interrupt();
 
         runInSession(new DBRunnable() {
 
             public Result run(EntityManager sess) {
                // Shutdown may be called several times depending on the
                // application running (ITE, testexec, ...). During shutdown 
                // the application might have been deleted before. Therefore 
                // some checking is required.
                try {
                    if (m_application != null) {
                        removeApp(sess, m_application);
                        m_application = null;
                    }
                } catch (Throwable t) {
                    log.debug("application already removed", t); //$NON-NLS-1$
                }
                 return Result.OK;
             }
         });
         instance = null;
     }
     
     /**
      *  remove an application PO and its locks from the db
      *  @param sess Session to be used as execution context
      *  @param app The application (and its locks) which shall be removed.
      */ 
     private void removeApp(EntityManager sess, ApplicationPO app) {
         Query delQuery = sess.createQuery("delete from DbLockPO lock where lock.application = :app"); //$NON-NLS-1$
         delQuery.setParameter("app", app); //$NON-NLS-1$
         delQuery.executeUpdate();
         sess.remove(sess.getReference(ApplicationPO.class, app.getId()));
     }
 
     
     /**
      * Mark a PO as locked for a given session.
      * @param userSess Lock the PO for this session.
      * @param po The PO to be locked.
      * @param checkVersion check if the PO was modified in the db.
      * @return true if the lock attempt was successful
      * @throws PMDirtyVersionException if checkVersion is true and the version
      * of the PO differs from the version of the db instance of thos PO
      * @throws PMObjectDeletedException if the object was deleted
      */
     public synchronized boolean lockPO(final EntityManager userSess,
         final IPersistentObject po, final boolean checkVersion)
         throws PMDirtyVersionException, PMObjectDeletedException {
 
         final DBRunnable checkForDirty = new DBRunnable() {
             
             public Result run(EntityManager sess) {
                 Result result = Result.OK;
                 try {
                     if (checkVersion) {
                         Query versionQuery = sess.createQuery("select obj.version from " //$NON-NLS-1$
                                 + po.getClass().getSimpleName() 
                                 + " as obj where obj.id = :poID"); //$NON-NLS-1$
                         versionQuery.setParameter("poID", po.getId()); //$NON-NLS-1$
                         Integer version = 
                             (Integer)versionQuery.getSingleResult();
                         if (!po.getVersion().equals(version)) {
                             result = Result.OBJECT_DIRTY;
                         }
                     } else {
                         Query countQuery = sess.createQuery("select count(obj.id) from " //$NON-NLS-1$
                                 + po.getClass().getSimpleName() 
                                 + " as obj where obj.id = :poID"); //$NON-NLS-1$
                         countQuery.setParameter("poID", po.getId()); //$NON-NLS-1$
                         Long count = (Long)countQuery.getSingleResult();
                         if (count == 0) {
                             result = Result.OBJECT_DELETED;
                         }
                     }
                 } catch (NoResultException nre) {
                     result = Result.OBJECT_DELETED;
                 }
 
                 return result;
             }
         };
         final Result runResult = runInSession(checkForDirty);
         if (runResult == Result.OBJECT_DELETED) {
             throw new PMObjectDeletedException(po,
                 Messages.LockFailedDueToDeletedPO,
                 MessageIDs.E_DELETED_OBJECT);
         }
         if (checkVersion && (runResult == Result.OBJECT_DIRTY)) {
             throw new PMDirtyVersionException(po,
                 Messages.LockFailedDueToDbOutOfSync,
                 MessageIDs.E_STALE_OBJECT);
         }
 
         return Result.OK == runInSession(new DBRunnable() {
 
             public Result run(EntityManager sess) {
                 Result lockOK;
                 Query lockQuery = sess.createQuery(
                         "select lock from DbLockPO as lock where lock.poId = :poID"); //$NON-NLS-1$
                 lockQuery.setParameter("poID", po.getId()); //$NON-NLS-1$
                 
                 try {
                     DbLockPO lock = (DbLockPO)lockQuery.getSingleResult();
                     lockOK = (lock.getApplication().equals(m_application) 
                         && lock.getSessionId().intValue() 
                             == System.identityHashCode(userSess)) ? Result.OK
                                     : Result.FAILED;
                 } catch (NoResultException nre) {
                     DbLockPO lock = new DbLockPO(m_application, userSess, po);
                     sess.persist(lock);
                     lockOK = Result.OK;
                 }
 
                 return lockOK;
             }
         });
 
     }
     
     /**
      * tries to lock a list of persistent objects
      * @param sess
      *      Session
      * @param objectsToLock
      *      Set<IPersistentObject>
      * @param checkVersion
      *      boolean
      * @throws PMDirtyVersionException
      *      dirty version found
      * @throws PMObjectDeletedException
      *      object was deleted
      * @throws PMAlreadyLockedException
      *      object was deleted
      * @return boolean
      */
     public synchronized boolean lockPOs(EntityManager sess, 
         final Set< ? extends IPersistentObject> objectsToLock, 
         final boolean checkVersion)
         throws PMDirtyVersionException, PMObjectDeletedException, 
         PMAlreadyLockedException {
         
         IPersistentObject failedPO = null;
         Result runResult = Result.OK;
         for (IPersistentObject po : objectsToLock) {
             boolean lockResult;
             try {
                 lockResult = lockPO(sess, po, checkVersion);
                 if (!lockResult) {
                     failedPO = po;
                     runResult = Result.FAILED;
                     break;
                 }
             } catch (PMDirtyVersionException e) {
                 failedPO = po;
                 runResult = Result.OBJECT_DIRTY;
                 break;
             } catch (PMObjectDeletedException e) {
                 failedPO = po;
                 runResult = Result.OBJECT_DELETED;
                 break;
             }
         }
         
         if (runResult != Result.OK) {
             handleLockProblems(failedPO, runResult);
         }
         return true;
     }
 
     /**
      * @param po
      *      IPersistentObject
      * @param runResult
      *      Result
      * @throws PMObjectDeletedException
      *      error
      * @throws PMDirtyVersionException
      *      error
      * @throws PMAlreadyLockedException
      *      error
      */
     private void handleLockProblems(final IPersistentObject po, 
         final Result runResult)
         throws PMObjectDeletedException, 
         PMDirtyVersionException, PMAlreadyLockedException {
         if (runResult == Result.FAILED) {
             String poName = po != null ? po.getName() : StringConstants.EMPTY;
             long poId = po != null ? po.getId() : -1;
             throw new PMAlreadyLockedException(po, 
                 "PO " + po + " (name=" + poName + "; id=" + poId + ") locked in db.", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                 MessageIDs.E_OBJECT_IN_USE);   
         }
         if (runResult == Result.OBJECT_DELETED) {
             throw new PMObjectDeletedException(po,
                 Messages.LockFailedDueToDeletedDOT,
                 MessageIDs.E_DELETED_OBJECT); 
         }
         
         if (runResult == Result.OBJECT_DIRTY) {
             throw new PMDirtyVersionException(po,
                 Messages.LockFailedDueToDbOutOfSync,
                 MessageIDs.E_DELETED_OBJECT); 
         }
         
     }
 
     /**
      * Release the lock on this PO.
      * @param po The PO to be released.
      */
     public synchronized void unlockPO(final IPersistentObject po) {
         runInSession(new DBRunnable() {
             public Result run(EntityManager sess) {
                 Query removeLockQuery = 
                     sess.createQuery("delete from DbLockPO lock where lock.poId = :poId"); //$NON-NLS-1$
                 removeLockQuery.setParameter("poId", po.getId()); //$NON-NLS-1$
                 return (removeLockQuery.executeUpdate() > 0) ? Result.OK
                     : Result.FAILED;
             }
         });
     }
 
     /**
      * @return true if an instance of the LockManager is running
      */
     public static boolean isRunning() {
         return instance != null;
     }
 
     /**
      * Release all locks for this Session.
      * @param s The session for which all locks (if any) shall be released.
      */
     public synchronized void unlockPOs(final EntityManager s) {
         runInSession(new DBRunnable() {
 
             public Result run(EntityManager sess) {
                 Query removeLockQuery = 
                     sess.createQuery("delete from DbLockPO lock where lock.sessionId = :sessId"); //$NON-NLS-1$
                 removeLockQuery.setParameter("sessId", System.identityHashCode(s)); //$NON-NLS-1$
                 return (removeLockQuery.executeUpdate() > 0) ? Result.OK
                     : Result.FAILED;
             }
         });
     }
     
     /**
      * This method is for installation support only. It will fail
      * in a normal runtime environment.
      * @param em Entity manager for db transaction
      *
      */
     public static void initDbGuard(EntityManager em) {
         final DbGuardPO guard = new DbGuardPO();
         guard.setId(DB_GUARD_ID);
         em.merge(guard);
     }
 
 }
