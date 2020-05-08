 package org.yajul.framework.jta;
 
 import org.apache.log4j.Logger;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.TransactionSystemException;
 import org.yajul.util.ReflectionUtil;
 import org.yajul.framework.ServiceLocator;
 
 import javax.transaction.Status;
 import javax.transaction.Synchronization;
 import javax.transaction.SystemException;
 import javax.transaction.Transaction;
 import javax.transaction.TransactionManager;
 import java.util.Map;
 
 /**
  * Binds a Spring transaction to an existing JTA transaction, which allows
  * allows JTA-based code such as EJBs with CMT to use Spring's DAO support classes.
  * To use this, simply invoke the sync(txManager) method before using any Spring-based
  * DAOs.
  * User: jdavis
  * Date: Apr 2, 2004
  * Time: 3:43:09 PM
  * @author jdavis
  */
 public class JtaSynchronizer implements Synchronization
 {
     public static final String DEFAULT_TRANSACTION_MANAGER_BEAN_ID = "transactionManager";
 
     /**
      * The logger for this class.
      */
     private static Logger log = Logger.getLogger(JtaSynchronizer.class.getName());
     private static Map statusNames = ReflectionUtil.getConstantNameMap(Status.class);
     private static ThreadLocal currentTx = new ThreadLocal();
 
     private Thread thread;
     private JtaTransactionManager txManager;
     private TransactionStatus txStatus;
     private Transaction jtaTransaction;
 
     /**
      * Synchronizes Spring with JTA by creating a new Spring transaction and notifying
      * JTA that the Spring transaction should be completed when the JTA transaction completes.
      * @param txManager The JTA transaction manager.
      */
     public static void sync(JtaTransactionManager txManager)
     {
         JtaSynchronizer txSync = (JtaSynchronizer) currentTx.get();
         if (txSync == null)
         {
             txSync = new JtaSynchronizer();
             currentTx.set(txSync);
         }
         txSync.initialize(txManager);
     }
 
 
     /**
      * Synchronizes Spring with JTA by creating a new Spring transaction and notifying
      * JTA that the Spring transaction should be completed when the JTA transaction completes.
      * This method uses the default transaction manager bean, 'transactionManager' from the
      * default bean factory 'ServiceLocator'.
      */
     public static void sync()
     {
         sync((JtaTransactionManager)ServiceLocator.getInstance().getBean(
                 DEFAULT_TRANSACTION_MANAGER_BEAN_ID,
                 JtaTransactionManager.class));
     }
 
     private JtaSynchronizer()
     {
         if (log.isDebugEnabled())
             log.debug("<ctor>");
     }
 
     private void initialize(JtaTransactionManager txManager)
     {
         if (log.isDebugEnabled())
             log.debug("initialize() "  + txManager);
         if (this.txManager != null)
         {
             if (txManager != this.txManager)
                 throw new IllegalStateException("Thread transaction is already active using a different transaction manager!");
             checkThread();
             if (log.isDebugEnabled())
                 log.debug("initialize() : Using existing thread local transaction.");
             return;
         }
 
         try
         {
             TransactionManager tm = txManager.getTransactionManager();
             if (tm == null)
                 throw new IllegalStateException("No JTA TransactionManager!");
             int status = tm.getStatus();
             if (log.isDebugEnabled())
                 log.debug("start() : status = " + statusNames.get(new Integer(status)));
             if (status == Status.STATUS_NO_TRANSACTION)
                 throw new IllegalStateException("No current JTA transaction!");
             Transaction jtaTransaction = tm.getTransaction();
             if (jtaTransaction == null)
                 throw new IllegalStateException("Current transaction is null.");
             if (jtaTransaction.getStatus() != Status.STATUS_ACTIVE)
                 throw new IllegalStateException("Transaction " + jtaTransaction + " is not 'active'!");
             if (log.isDebugEnabled())
                 log.debug("initialize() : Creating a new Spring transaction, and registering JTA synchronization...");
             TransactionStatus springTransaction = txManager.getTransaction(null);
             jtaTransaction.registerSynchronization(this);
             // Everything was okay, so set up the instance variables.
             this.txStatus = springTransaction;
             this.txManager = txManager;
             this.thread = Thread.currentThread();
             this.jtaTransaction = jtaTransaction;
         }
         catch (Exception e)
         {
             log.fatal(e,e);
             throw new TransactionSystemException("Unable to register synchronization due to: " + e, e);
         }
     }
 
     public void beforeCompletion()
     {
         if (log.isDebugEnabled())
             log.debug("beforeCompletion() " + jtaTransaction);
         checkThread();
         try
         {
             int status = jtaTransaction.getStatus();
             if (log.isDebugEnabled())
                 log.debug("beforeCompletion() status = " + statusNames.get(new Integer(status)));
             if (status == Status.STATUS_ACTIVE && !this.txStatus.isRollbackOnly())
             {
                 if (log.isDebugEnabled())
                     log.debug("beforeCompletion() : Committing active transaction...");
                 this.txManager.commit(this.txStatus);
             }
             else
             {
                 if (log.isDebugEnabled())
                     log.debug("beforeCompletion() : Rolling back...");
                 this.txManager.rollback(this.txStatus);
             }
         }
         catch (SystemException e)
         {
             log.fatal(e,e);
             throw new TransactionSystemException("Unable to process synchronization.beforeCompletion() due to: " + e, e);
         }
         finally
         {
             this.txStatus = null;
             this.txManager = null;
         }
     }
 
     public void afterCompletion(int status)
     {
         if (log.isDebugEnabled())
             log.debug("afterCompletion() status = " + statusNames.get(new Integer(status)));
         checkThread();
         jtaTransaction = null;
         thread = null;
         release();
     }
 
     private void release()
     {
        currentTx.set(null);
     }
 
     private void checkThread()
     {
         if (Thread.currentThread() != thread)
             throw new IllegalStateException("Wrong thread!");
     }
 
     /**
      * Called by the garbage collector on an object when garbage collection
      * determines that there are no more references to the object.
      */
     protected void finalize() throws Throwable
     {
         if (log.isDebugEnabled())
             log.debug("finalize()");
         if (txManager != null)
             log.error("The transaction was not completed!");
     }
 }
