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
 package org.eclipse.jubula.client.core.persistence;
 
 import java.sql.SQLException;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityNotFoundException;
 import javax.persistence.LockTimeoutException;
 import javax.persistence.OptimisticLockException;
 import javax.persistence.PersistenceException;
 import javax.persistence.PessimisticLockException;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.jubula.client.core.businessprocess.progress.OperationCanceledUtil;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.ProjectDeletedException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 
 /**
  * base persistence manager class
  * @author BREDEX GmbH
  * @created 07.09.2004
  */
 public abstract class PersistenceManager {
 
     /** the logger */
     private static Log log = LogFactory.getLog(PersistenceManager.class);
 
     /**
      * 
      */
     protected PersistenceManager() {
         log.debug("deprecated"); //$NON-NLS-1$
     }
     
     /**
      * @param obj obj to handle the PersistenceException for
      * @param e thrown exception
      * @throws PMException in case of general db error
      * @throws PMDirtyVersionException in case of version conflict
      * @throws PMAlreadyLockedException in case of locked obj
      * @throws OperationCanceledException in case of canceled operation
      */
     private static void handleDetailedPersistenceException(
         IPersistentObject obj, 
         PersistenceException e) throws PMException, PMDirtyVersionException, 
         PMAlreadyLockedException, OperationCanceledException {
 
         OperationCanceledUtil.checkForOperationCanceled(e);
         
         String msg = null;
         String objName = null;
         if (obj == null) {
            objName = Messages.UnknownObject;
         } else {
             objName = obj.getName();
         }
         // please attend the order of instance of verifications
         Throwable rootCause = ExceptionUtils.getRootCause(e);
         if (e instanceof EntityNotFoundException) {
             msg = objName + StringConstants.SPACE 
                 + Messages.WasDeletedByAnotherTransaction + StringConstants.DOT;
             log.debug(msg, e);
             throw new PMObjectDeletedException(obj, msg,
                 MessageIDs.E_DELETED_OBJECT);
         } else if (e instanceof OptimisticLockException) {
             msg = objName + StringConstants.SPACE
                 + Messages.WasModifiedInDBDirtyVersion + StringConstants.DOT;
             log.debug(msg, e);
             throw new PMDirtyVersionException(obj, msg, 
                 MessageIDs.E_STALE_OBJECT); 
         } else if (isLockException(e)) {
             msg = objName + StringConstants.SPACE
                 + Messages.AlreadyLockedCurrentlyLockAttemptFailed 
                 + StringConstants.DOT;
             log.debug(msg, e);
             throw new PMAlreadyLockedException(obj, msg, 
                 MessageIDs.E_OBJECT_IN_USE);
         
         } else if (rootCause instanceof SQLException) {
             SQLException sqlException = (SQLException)rootCause;           
             msg = sqlException.getMessage();
             log.debug(msg, e);
             throw new PMException(msg, MessageIDs.E_SQL_EXCEPTION);
             
         } 
         msg = Messages.GeneralDatabaseErrorFor + StringConstants.SPACE 
             + objName + StringConstants.DOT;
         log.error(msg, e);
         throw new PMException(msg, MessageIDs.E_DATABASE_GENERAL);
     }
     
     /**
      * check if the exception is caused by a failed lock attempt
      * @param e hibernate exception
      * @return true if e is caused by a failed lock attempt
      */
     private static boolean isLockException(PersistenceException e) {
        for (Throwable cause : ExceptionUtils.getThrowables(e)) {
            if (cause instanceof PessimisticLockException
                    || cause instanceof OptimisticLockException
                    || cause instanceof LockTimeoutException) {
                 return true;
            } else if (cause instanceof SQLException) {
                if (((SQLException)cause).getSQLState() != null
                        && ((SQLException)cause).getSQLState().startsWith("61000")) {  //$NON-NLS-1$ // NOPMD by al on 3/19/07 1:38 PM
                    // error state in Oracle
                    return true;
                }
             }
         }
         return false;
     }
 
     /**
      * @param obj obj caused exception
      * @param e hibernate exception
      * @param editSupp associated editSupport for editor
      * @throws PMException in case of any db error
      */
     public static void handleDBExceptionForEditor(IPersistentObject obj,
         PersistenceException e, EditSupport editSupp) throws PMException {
         if (isLockException(e)) {
             editSupp.reinitializeEditSupport();
         } else if (e instanceof EntityNotFoundException) {
             editSupp.close();
         } else {
             editSupp.reloadEditSession();
         }
         handleDetailedPersistenceException(obj, e);
     }
     
     /**
      * @param obj
      *            obj to handle the PersistenceException for hint: obj could be
      *            null (for example for a db commit, where the causing object is
      *            unknown
      * @param e
      *            thrown exception
      * @throws PMAlreadyLockedException
      *             in case of locked obj
      * @throws PMDirtyVersionException
      *             in case of version conflict
      * @throws PMException
      *             in case of general db error
      * @throws ProjectDeletedException
      *             if the project was deleted in another instance
      */
     public static void handleDBExceptionForMasterSession(IPersistentObject obj,
         PersistenceException e)
         throws PMAlreadyLockedException, PMDirtyVersionException, PMException, 
         ProjectDeletedException {
         final GeneralStorage gs = GeneralStorage.getInstance();
         if (isLockException(e)) {
             gs.recoverSession();
         } else {
             gs.reloadMasterSession(new NullProgressMonitor());
         }
         handleDetailedPersistenceException(obj, e);                   
     }
     
     /**
      * handles DBExceptions for any session
      * @param obj obj caused exception
      * @param e hibernate exception
      * @param s associated session
      * @throws PMException in case of any db error
      */
     public static void handleDBExceptionForAnySession(IPersistentObject obj, 
         PersistenceException e, EntityManager s) throws PMException {
         Hibernator.instance().dropSession(s);
         handleDetailedPersistenceException(obj, e);
     }
 }
