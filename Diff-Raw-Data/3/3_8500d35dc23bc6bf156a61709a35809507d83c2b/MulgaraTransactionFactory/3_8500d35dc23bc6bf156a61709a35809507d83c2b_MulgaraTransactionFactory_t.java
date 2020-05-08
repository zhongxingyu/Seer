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
  *
  * Derivation from MulgaraTransactionManager Copyright (c) 2007 Topaz
  * Foundation under contract by Andrae Muys (mailto:andrae@netymon.com).
  */
 
 package org.mulgara.resolver;
 
 // Java2 packages
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.ReentrantLock;
 import javax.transaction.SystemException;
 import javax.transaction.Transaction;
 import javax.transaction.TransactionManager;
 import javax.transaction.xa.XAResource;
 
 // Third party packages
 import org.apache.log4j.Logger;
 
 // Local packages
 import org.mulgara.query.MulgaraTransactionException;
 import org.mulgara.server.Session;
 import org.mulgara.transaction.TransactionManagerFactory;
 
 /**
  * Manages transactions within Mulgara.
  *
  * see http://mulgara.org/confluence/display/dev/Transaction+Architecture
  *
  * Maintains association between Answer's and TransactionContext's.
  * Manages tracking the ownership of the write-lock.
  * Maintains the write-queue and any timeout algorithm desired.
  * Provides new/existing TransactionContext's to DatabaseSession on request.
  *    Note: Returns new context unless Session is currently in a User Demarcated Transaction.
  * 
  * @created 2006-10-06
  *
  * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
  *
  * @company <A href="mailto:mail@netymon.com">Netymon Pty Ltd</A>
  *
  * @copyright &copy;2006 <a href="http://www.netymon.com/">Netymon Pty Ltd</a>
  *
  * @licence Open Software License v3.0</a>
  */
 
 public abstract class MulgaraTransactionFactory {
   private static final Logger logger =
     Logger.getLogger(MulgaraTransactionFactory.class.getName());
 
   protected final MulgaraTransactionManager manager;
   
   /**
    * Contains a reference the the current writing transaction IFF it is managed
    * by this factory.  If there is no current writing transaction, or if the
    * writing transaction is managed by a different factory then it is null.
    */
   protected MulgaraTransaction writeTransaction;
 
   private ReentrantLock mutex;
 
   protected MulgaraTransactionFactory(MulgaraTransactionManager manager) {
     this.manager = manager;
     this.mutex = new ReentrantLock();
     this.writeTransaction = null;
   }
 
 
   /**
    * Obtain a transaction context associated with a DatabaseSession.
    *
    * Either returns the existing context if:
    * a) we are currently within a recursive call while under implicit XA control
    * or
    * b) we are currently within an active user demarcated XA.
    * otherwise creates a new transaction context and associates it with the
    * session.
    */
   public abstract MulgaraTransaction getTransaction(final DatabaseSession session, boolean write)
       throws MulgaraTransactionException;
   
   protected abstract Set<? extends MulgaraTransaction> getTransactionsForSession(DatabaseSession session);
 
   /**
    * Rollback, or abort all transactions associated with a DatabaseSession.
    *
    * Will only abort the transaction if the rollback attempt fails.
    */
   public void closingSession(DatabaseSession session) throws MulgaraTransactionException {
     acquireMutex();
     logger.debug("Cleaning up any stale transactions on session close");
     try {
       Map<MulgaraTransaction, Throwable> requiresAbort = new HashMap<MulgaraTransaction, Throwable>();
       try {
         Throwable error = null;
 
         if (manager.isHoldingWriteLock(session)) {
           logger.debug("Session holds write-lock");
           try {
             if (writeTransaction != null) {
               try {
                 logger.warn("Terminating session while holding writelock:" + session + ": " + writeTransaction);
                 writeTransaction.execute(new TransactionOperation() {
                     public void execute() throws MulgaraTransactionException {
                       writeTransaction.heuristicRollback("Session closed while holding write lock");
                     }
                 });
               } catch (Throwable th) {
                 if (writeTransaction != null) {
                   requiresAbort.put(writeTransaction, th);
                   error = th;
                 }
               } finally {
                 writeTransaction = null;
               }
             }
           } finally {
            if (manager.isHoldingWriteLock(session))    // normally this will have been released
              manager.releaseWriteLock(session);
           }
         } else {
           logger.debug("Session does not hold write-lock");
         }
 
         for (MulgaraTransaction transaction : getTransactionsForSession(session)) {
           try {
             // This is final so we can create the closure.
             final MulgaraTransaction xa = transaction;
             transaction.execute(new TransactionOperation() {
                 public void execute() throws MulgaraTransactionException {
                   xa.heuristicRollback("Rollback due to session close");
                 }
             });
           } catch (Throwable th) {
             requiresAbort.put(transaction, th);
             if (error == null) {
               error = th;
             }
           }
         }
 
         if (error != null) {
           throw new MulgaraTransactionException("Heuristic rollback failed on session close", error);
         }
       } finally {
         try {
           abortTransactions(requiresAbort);
         } catch (Throwable th) {
           try {
             logger.error("Error aborting transactions after heuristic rollback failure on session close", th);
           } catch (Throwable throw_away) { }
         }
       }
     } finally {
       releaseMutex();
     }
   }
 
   /**
    * Abort as many of the transactions as we can.
    */
   protected void abortTransactions(Map<MulgaraTransaction, Throwable> requiresAbort) {
     try {
       if (!requiresAbort.isEmpty()) {
         // At this point the originating exception has been thrown in the caller
         // so we attempt to ensure it doesn't get superseeded by anything that
         // might be thrown here while logging any errors.
         try {
           logger.error("Heuristic Rollback Failed on session close- aborting");
         } catch (Throwable throw_away) { } // Logging difficulties.
 
         try {
           for (MulgaraTransaction transaction : requiresAbort.keySet()) {
             try {
               transaction.abortTransaction("Heuristic Rollback failed on session close",
                   requiresAbort.get(transaction));
             } catch (Throwable th) {
               try {
                 logger.error("Error aborting transaction after heuristic rollback failure on session close", th);
               } catch (Throwable throw_away) { }
             }
           }
         } catch (Throwable th) {
           try {
             logger.error("Loop error while aborting transactions after heuristic rollback failure on session close", th);
           } catch (Throwable throw_away) { }
         }
       }
     } catch (Throwable th) {
       try {
         logger.error("Unidentified error while aborting transactions after heuristic rollback failure on session close", th);
       } catch (Throwable throw_away) { }
     }
   }
 
   /**
    * Used to replace the built in monitor to allow it to be properly released
    * during potentially blocking operations.  All potentially blocking
    * operations involve writes, so in these cases the write-lock is reserved
    * allowing the mutex to be safely released and then reobtained after the
    * blocking operation concludes.
    */
   protected void acquireMutex() {
     mutex.lock();
   }
 
 
   protected void releaseMutex() {
     if (!mutex.isHeldByCurrentThread()) {
       throw new IllegalStateException("Attempt to release mutex without holding mutex");
     }
 
     mutex.unlock();
   }
 
   protected void runWithoutMutex(TransactionOperation proc) throws MulgaraTransactionException {
     if (!mutex.isHeldByCurrentThread()) {
       throw new IllegalStateException("Attempt to run procedure without holding mutex");
     }
     int holdCount = mutex.getHoldCount();
     for (int i = 0; i < holdCount; i++) {
       mutex.unlock();
     }
     try {
       proc.execute();
     } finally {
       for (int i = 0; i < holdCount; i++) {
         mutex.lock();
       }
     }
   }
 }
