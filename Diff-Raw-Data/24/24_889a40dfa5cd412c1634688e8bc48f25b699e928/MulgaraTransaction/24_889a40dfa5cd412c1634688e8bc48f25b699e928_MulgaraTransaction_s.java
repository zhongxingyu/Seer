 /*
  * The contents of this file are subject to the Open Software License
  * Version 3.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.rosenlaw.com/OSL3.0.htm
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  *
  * This file is an original work developed by Netymon Pty Ltd
  * (http://www.netymon.com, mailto:mail@netymon.com). Portions created
  * by Netymon Pty Ltd are Copyright (c) 2006 Netymon Pty Ltd.
  * All Rights Reserved.
  */
 package org.mulgara.resolver;
 
 // Java 2 enterprise packages
 import javax.transaction.RollbackException;
 import javax.transaction.SystemException;
 import javax.transaction.Transaction;
 import javax.transaction.xa.XAResource;
 
 // Third party packages
 import org.apache.log4j.Logger;
 
 // Local packages
 import org.mulgara.resolver.spi.DatabaseMetadata;
 import org.mulgara.resolver.spi.EnlistableResource;
 import org.mulgara.resolver.spi.ResolverSessionFactory;
 
 import org.mulgara.query.TuplesException;
 import org.mulgara.query.QueryException;
 
 /**
  * Responsible for the javax.transaction.Transaction object.
  * Responsibilities
  * Ensuring every begin or resume is followed by either a suspend or an end.
  * Ensuring every suspend or end is preceeded by either a begin or a resume.
  * In conjunction with TransactionalAnswer ensuring that
  * all calls to operations on SubqueryAnswer are preceeded by a successful resume.
  * all calls to operations on SubqueryAnswer conclude with a suspend as the last call prior to returning to the user.
  * Collaborates with DatabaseTransactionManager to determine when to end the transaction.
  *
  * @created 2006-10-06
  *
  * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
  *
  * @version $Revision: $
  *
  * @modified $Date: $
  *
  * @maintenanceAuthor $Author: $
  *
  * @company <a href="mailto:mail@netymon.com">Netymon Pty Ltd</a>
  *
  * @copyright &copy;2006 <a href="http://www.netymon.com/">Netymon Pty Ltd</a>
  *
  * @licence Open Software License v3.0
  */
 public class MulgaraTransaction {
   /** Logger.  */
   private static final Logger logger =
     Logger.getLogger(MulgaraTransaction.class.getName());
 
   private MulgaraTransactionManager manager;
   private DatabaseOperationContext context;
 
   private Transaction transaction;
   private Thread currentThread;
 
   private int inuse;
   private int using;
 
   private final int NO_ROLLBACK = 0;
   private final int IMPLICIT_ROLLBACK = 1;
   private final int EXPLICIT_ROLLBACK = 2;
 
   private int rollback;
   private Throwable rollbackCause;
 
   public MulgaraTransaction(MulgaraTransactionManager manager, DatabaseOperationContext context)
       throws Exception {
     report("Creating Transaction");
     try {
       if (manager == null) {
         throw new IllegalArgumentException("Manager null in MulgaraTransaction");
       } else if (context == null) {
         throw new IllegalArgumentException("OperationContext null in MulgaraTransaction");
       }
       this.manager = manager;
       this.context = context;
 
       inuse = 0;
       using = 0;
 
       rollback = NO_ROLLBACK;
       rollbackCause = null;
     } finally {
       report("Created Transaction");
     }
   }
 
 
   synchronized void activate() throws MulgaraTransactionException {
     report("Activating Transaction");
     try {
       if (rollback != NO_ROLLBACK) {
         throw new MulgaraTransactionException("Attempt to activate failed transaction");
       }
 
      if (currentThread == null) {
        currentThread = Thread.currentThread();
      } else if (!currentThread.equals(Thread.currentThread())) {
         throw new MulgaraTransactionException("Concurrent access attempted to transaction: Transaction has NOT been rolledback.");
       }
       
       if (manager == null) {
         errorReport("Attempt to activate terminated transaction");
         throw new MulgaraTransactionException("Attempt to activate terminated transaction");
       }
 
       if (inuse == 0) {
         if (transaction == null) {
           startTransaction();
         } else {
           resumeTransaction();
         }
       }
 
       inuse++;
 
       checkActivated();
     } finally {
       report("Activated transaction");
     }
   }
 
   private synchronized void deactivate() throws MulgaraTransactionException {
     report("Deactivating transaction");
     try {
       if (rollback == NO_ROLLBACK) {
         checkActivated();
       } // rollback'd transactions are cleaned up on the final deactivation.
 
       inuse--;
 
       if (inuse == 0) {
        if (using == 0) {
          terminateTransaction();
        } else {
          suspendTransaction();
         }
        currentThread = null;
       }
     } finally {
       report("Deactivated Transaction");
     }
   }
 
   // Note: The transaction is often not activated when these are called.
   //       This occurs when setting autocommit, as this creates and
   //       references a transaction object that won't be started/activated
   //       until it is first used.
   void reference() throws MulgaraTransactionException {
     report("Referencing Transaction");
     try {
       using++;
     } finally {
       report("Referenced Transaction");
     }
   }
 
   void dereference() throws MulgaraTransactionException {
     report("Dereferencing Transaction");
     try {
       if (using < 1) {
         throw implicitRollback(new MulgaraTransactionException(
             "Reference Failure.  Dereferencing while using < 1: " + using));
       }
       using--;
     } finally {
       report("Dereferenced Transaction");
     }
   }
 
   void execute(Operation operation,
                ResolverSessionFactory resolverSessionFactory, // FIXME: We shouldn't need this. - only used for backup and restore operations.
                DatabaseMetadata metadata) throws MulgaraTransactionException {
     report("Executing Operation");
     try {
       activate();
       try {
         operation.execute(context,
                           context.getSystemResolver(),
                           resolverSessionFactory,
                           metadata);
       } catch (Throwable th) {
         throw implicitRollback(th);
       } finally {
         deactivate();
       }
     } finally {
       report("Executed Operation");
     }
   }
 
   AnswerOperationResult execute(AnswerOperation ao) throws TuplesException {
     debugReport("Executing AnswerOperation");
     try {
       activate();
       try {
         ao.execute();
         return ao.getResult();
       } catch (Throwable th) {
         throw implicitRollback(th);
       } finally {
         deactivate();
       }
     } catch (MulgaraTransactionException em) {
       throw new TuplesException("Transaction error", em);
     } finally {
       debugReport("Executed AnswerOperation");
     }
   }
 
 
   void execute(TransactionOperation to) throws MulgaraTransactionException {
     report("Executing TransactionOperation");
     try {
       activate();
       try {
         to.execute();
       } catch (Throwable th) {
         throw implicitRollback(th);
       } finally {
         deactivate();
       }
     } finally {
       report("Executed TransactionOperation");
     }
   }
 
 
   MulgaraTransactionException implicitRollback(Throwable cause) throws MulgaraTransactionException {
     report("Implicit Rollback triggered");
 
     if (rollback == IMPLICIT_ROLLBACK) {
       logger.warn("Cascading error, transaction already rolled back", cause);
       logger.warn("Cascade error, expected initial cause", rollbackCause);
 
       return new MulgaraTransactionException("Transaction already in rollback", cause);
     }
 
     try {
       checkActivated();
       rollback = IMPLICIT_ROLLBACK;
       rollbackCause = cause;
       failTransaction();
       return new MulgaraTransactionException("Transaction in Rollback", cause);
     } catch (Throwable th) {
       abortTransaction("Failed to rollback normally", th);
       throw new MulgaraTransactionException("Abort failed to throw exception", th);
     }
   }
 
   /**
    * Rollback the transaction.
    * We don't throw an exception here when transaction fails - this is expected,
    * after all we requested it.
    */
   public void explicitRollback() throws MulgaraTransactionException {
     try {
       checkActivated();
       failTransaction();
       rollback = EXPLICIT_ROLLBACK;
     } catch (Throwable th) {
       abortTransaction("Explicit rollback failed", th);
     }
   }
 
   private void startTransaction() throws MulgaraTransactionException {
     report("Initiating transaction");
     transaction = manager.transactionStart(this);
     try {
       context.initiate(this);
     } catch (Throwable th) {
       throw implicitRollback(th);
     }
   }
 
   private void resumeTransaction() throws MulgaraTransactionException {
     report("Resuming transaction");
     try {
       manager.transactionResumed(this, transaction);
     } catch (Throwable th) {
       abortTransaction("Failed to resume transaction", th);
     }
   }
 
   private void suspendTransaction() throws MulgaraTransactionException {
     report("Suspending Transaction");
     try {
       if (rollback == NO_ROLLBACK) {
         this.transaction = manager.transactionSuspended(this);
       } else {
         terminateTransaction();
       }
     } catch (Throwable th) {
       throw implicitRollback(th);
     } finally {
       report("Finished suspending transaction");
     }
   }
 
   private void terminateTransaction() throws MulgaraTransactionException {
     report("Terminating Transaction: " + rollback);
 //    errorReport("Terminating Transaction: " + rollback);
     try {
       switch (rollback) {
         case NO_ROLLBACK:
           report("Completing Transaction");
           try {
             transaction.commit();
             transaction = null;
           } catch (Throwable th) {
             implicitRollback(th);
             terminateTransaction();
           }
           break;
         case IMPLICIT_ROLLBACK:
           report("Completing Implicitly Failed Transaction");
           // Check that transaction is cleaned up.
           throw new MulgaraTransactionException(
               "Failed transaction finalised. (ROLLBACK)", rollbackCause);
         case EXPLICIT_ROLLBACK:
           report("Completing explicitly failed transaction (ROLLBACK)");
           // Check that transaction is cleaned up.
           break;
       }
     } finally {
       try {
         try {
           context.clear();
         } catch (QueryException eq) {
           throw new MulgaraTransactionException("Error clearing context", eq);
         }
       } finally {
         try {
           manager.transactionComplete(this);
         } finally {
           manager = null;
           inuse = 0;
           using = 0;
           report("Terminated transaction");
         }
       }
     }
   }
 
   private void failTransaction() throws Throwable {
     transaction.rollback();
     manager.transactionFailed(this);
     context.clear();
   }
 
   void abortTransaction(String errorMessage, Throwable th) throws MulgaraTransactionException {
     // We need to notify the manager here - this is serious, we
     // can't rollback normally if we can't resume!  The call to
     // context.abort() is an escape hatch we use to abort the 
     // current phase behind the scenes.
     logger.error(errorMessage + " - Aborting", th);
     try {
       manager.transactionAborted(this);
     } finally {
       context.abort();
     }
     throw new MulgaraTransactionException(errorMessage + " - Aborting", th);
   }
 
   //
   // Note that OperationContext needs to be decoupled from DatabaseSession, so that it is
   // recreated for each operation - it also needs to provide an XAResource of it's own for
   // enlisting in the transaction to clean-up caches and the like.
   //
   public void enlist(EnlistableResource enlistable) throws MulgaraTransactionException {
     try {
       transaction.enlistResource(enlistable.getXAResource());
     } catch (Exception e) {
       throw new MulgaraTransactionException("Error enlisting resolver", e);
     }
   }
 
   //
   // Should only be visible to MulgaraTransactionManager.
   //
 
   /**
    * Force transaction to a conclusion.
    * If we can't activate or commit, then rollback and terminate.
    * This is called by the manager to indicate that it needs this transaction to
    * be finished NOW, one way or another.
    */
   void completeTransaction() throws MulgaraTransactionException {
     try {
       activate();
     } catch (Throwable th) {
       implicitRollback(th);  // let terminate throw this exception if required.
     } finally {
       terminateTransaction();
     }
     // We don't need to deactivate - this method is *only* for use by the
     // MulgaraTransactionManager!
   }
 
   protected void finalize() {
     report("GC-finalize");
     if (inuse != 0 || using != 0) {
       errorReport("Referernce counting error in transaction");
     }
     if (manager != null || transaction != null) {
       errorReport("Transaction not terminated properly");
     }
   }
 
   //
   // Used internally
   //
 
   private void checkActivated() throws MulgaraTransactionException {
     if (currentThread == null) {
       throw new MulgaraTransactionException("Transaction failed activation check");
     } else if (!currentThread.equals(Thread.currentThread())) {
       throw new MulgaraTransactionException("Concurrent access attempted to transaction: Transaction has NOT been rolledback.");
     } else if (inuse < 1) {
       throw implicitRollback(
           new MulgaraTransactionException("Mismatched activate/deactivate.  inuse < 1: " + inuse));
     } else if (using < 0) {
       throw implicitRollback(
           new MulgaraTransactionException("Reference Failure.  using < 0: " + using));
     }
   }
 
   private void report(String desc) {
     if (logger.isInfoEnabled()) {
       logger.info(desc + ": " + System.identityHashCode(this) +
           ", inuse=" + inuse + ", using=" + using);
     }
   }
 
   private void debugReport(String desc) {
     if (logger.isDebugEnabled()) {
       logger.debug(desc + ": " + System.identityHashCode(this) +
           ", inuse=" + inuse + ", using=" + using);
     }
   }
 
   private void errorReport(String desc) {
     logger.error(desc + ": " + System.identityHashCode(this) +
         ", inuse=" + inuse + ", using=" + using, new Throwable());
   }
 }
