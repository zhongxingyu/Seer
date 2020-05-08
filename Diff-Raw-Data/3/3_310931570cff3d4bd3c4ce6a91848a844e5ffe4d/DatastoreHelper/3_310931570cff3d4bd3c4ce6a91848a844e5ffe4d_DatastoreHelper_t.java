 package com.fizzbuzz.server.persist;
 
 import com.fizzbuzz.server.resource.BaseApplication;
 import com.google.appengine.api.datastore.DatastoreTimeoutException;
 import com.google.appengine.api.datastore.Transaction;
 import com.google.code.twig.configuration.Configuration;
 import com.google.code.twig.standard.StandardObjectDatastore;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ConcurrentModificationException;
 import java.util.HashMap;
 import java.util.Map;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkState;
 
 
 public abstract class DatastoreHelper {
     private static final Object mDsMapLock = new Object();
     private static int MAX_TIMEOUT_RETRIES = 3;
     private static Map<Long, StandardObjectDatastore> mDsMap;
 
     static {
         mDsMap = new HashMap<Long, StandardObjectDatastore>();
     }
 
     private final Logger mLogger = LoggerFactory.getLogger(LoggingManager.TAG);
 
     // get the datastore allocated to this thread
     public static StandardObjectDatastore getDs() {
         synchronized (mDsMapLock) {
             return checkNotNull(mDsMap.get(Thread.currentThread().getId()), "datastore not initialized");
         }
     }
 
     private static void releaseDatastoreForThread() {
         synchronized (mDsMapLock) {
             mDsMap.remove(Thread.currentThread().getId());
         }
     }
 
     static Configuration getConfiguration() {
         return DatastoreHelper.getDs().getConfiguration();
     }
 
     public void onAppStartup() {
         allocDatastoreForThread();
         registerKindNames(); // this just has to be done once
 
         if (BaseApplication.getExecutionContext() == BaseApplication.ExecutionContext.DEVELOPMENT) {
             if (datastoreIsEmpty()) {
                 seedDatastore();
             }
         }
     }
 
     public void onNewRequest() {
         allocDatastoreForThread();
     }
 
     public void onRequestComplete() {
         releaseDatastoreForThread();
     }
 
     public void onDatastoreException() {
         // throw out the old Datastore object and get a new one whenever there is a datastore exception.
         bounceDatastoreForThread();
     }
 
     public void resetDatastore() {
         mLogger.info("DatastoreHelper:resetDatastore: called");
         purgeDatastore();
         seedDatastore();
     }
 
     public <R> R doInTransactionWithResult(final TransactableWithResult<R> t) {
         return doInTransactionWithResult(t, false);
     }
 
     public <R> R doInTransactionWithResult(final TransactableWithResult<R> t,
                                            final boolean mustBeOuterTx) {
         doInTransaction(t, mustBeOuterTx);
         return t.getResult();
     }
 
     public void doInTransaction(final Transactable task) {
         doInTransaction(task, false);
     }
 
     /*
      * Executes the task in a transaction. If a timeout occurs, retry a few times, with an exponential backoff. GAE
      * automatically retries non-transactional datastore operations, but for transactions, we have to do it ourselves.
      * If a concurrent modification exception occurs, retry until success. If some other exception occurs, propagate it
      * upward.
      */
     public void doInTransaction(final Transactable task,
                                 final boolean mustBeOuterTx) {
         boolean done = false;
         int timeoutRetryWaitMs = 100;
         int timeoutRetryCount = 0;
         for (int tryCount = 0; !done; tryCount++) {
             Transaction tx = beginTransaction();
 
             if (tx == null && mustBeOuterTx == true) {
                 throw new PersistenceException(
                         "attempted to start an outer transaction when a transaction was already in progress.");
             }
 
             try {
                 task.run();
                 commitTransaction(tx);
                 done = true;
             } catch (DatastoreTimeoutException dte) {
                 mLogger.warn(
                         "Datastore timeout exception (occurrence #" + Integer.toString(timeoutRetryCount + 1) +
                                 ") for thread {}.", Thread.currentThread().getId(), dte);
                 onDatastoreException();
 
                 if (timeoutRetryCount < MAX_TIMEOUT_RETRIES) {
                     try {
                         Thread.sleep(timeoutRetryWaitMs);
                         timeoutRetryWaitMs *= 2; // wait longer next time
                     } catch (InterruptedException e1) {
                         e1.printStackTrace();
                     }
                     timeoutRetryCount++;
                 } else {
                     throw new PersistenceException("Tried " + Integer.toString(MAX_TIMEOUT_RETRIES)
                             + " times to execute datastore transaction for thread "
                             + Long.toString(Thread.currentThread().getId())
                             + ", but encountered DatastoreTimeoutException each time.  Giving up.");
                 }
 
             } catch (ConcurrentModificationException cme) {
                 mLogger.warn(
                         "Datastore optimistic concurrency exception (attempt #" + Integer.toString(tryCount) +
                                 ") for thread {}.", Thread.currentThread().getId(), cme);
                 onDatastoreException();
                 // go around again, until success
             }
 
             // other exceptions propagate upward...
 
             finally {
                 // the call to task.run() above may have thrown something, leaving the transaction open
                 rollbackTransactionIfActive(tx);
             }
         }
     }
 
     // to be overridden in subclass
     abstract protected void registerKindNames();
 
     abstract protected boolean datastoreIsEmpty();
 
     // commonly overridden in subclass
     protected StandardObjectDatastore createDs() {
         return new BaseDatastore(new BaseTwigConfiguration());
     }
 
     // commonly overridden in subclass
     protected void purgeDatastore() {
         checkState((BaseApplication.getExecutionContext() == BaseApplication.ExecutionContext.DEVELOPMENT),
                 "not running in development environment");
     }
 
     // commonly overridden in subclass
     protected void seedDatastore() {
     }
 
     protected void allocDatastoreForThread() {
         synchronized (mDsMapLock) {
             long threadId = Thread.currentThread().getId();
             if (mDsMap.get(threadId) == null) {
                 // instantiate a Twig datastore object and store it in the map, keyed by the current thread ID
                 mDsMap.put(threadId, createDs());
             }
         }
     }
 
     private void bounceDatastoreForThread() {
         releaseDatastoreForThread();
         allocDatastoreForThread();
     }
 
     // start a transaction if one isn't already in place. If one is already in place, return null.
     private Transaction beginTransaction() {
 
         Transaction result = null;
        Transaction lastTx = getDs().getTransaction();
        if (lastTx == null || !lastTx.isActive()){
             result = getDs().beginTransaction();
             mLogger.trace("Transaction {} started for thread {}", result, Thread.currentThread().getId());
         } else {
             mLogger.debug(
                     "BasePersist.beginTransaction: transaction already in progress for thread{}, " +
                             "no new transaction started",
                     Thread.currentThread().getId());
         }
 
         return result;
     }
 
     private void commitTransaction(final Transaction tx) {
         if (tx != null) {
             mLogger.trace("Committing transaction {} for thread {}", tx, Thread.currentThread().getId());
             tx.commit();
         }
     }
 
     private void rollbackTransactionIfActive(final Transaction tx) {
         if (tx != null) {
             if (tx.isActive()) {
                 mLogger.trace("Rolling back transaction {} for thread {}", tx, Thread.currentThread().getId());
                 tx.rollback();
             }
         }
     }
 
     // not currently using this method, but saving it for later. Commenting out to eliminate warning about unused code.
     // private static Transaction beginXGTransaction() {
     //
     // Transaction tx = getDs().getTransaction();
     // if (tx == null) {
     // TransactionOptions options = TransactionOptions.Builder.withXG(true);
     // tx = getDs().beginTransaction(options);
     // Logger logger = LoggerFactory.getLogger(LoggingManager.TAG);
     // logger.trace("Transaction {} started for thread {}", tx, Thread.currentThread().getId());
     // return tx;
     // }
     // else
     // return null;
     // }
 
     /**
      * Alternate interface to Runnable for executing transactions
      */
     public interface Transactable {
         void run();
     }
 
     /**
      * Provides a place to put the result. Note that the result
      * is only valid if the transaction completes successfully; otherwise
      * it should be ignored because it is not necessarily valid.
      */
     abstract public static class TransactableWithResult<R>
             implements Transactable {
         protected R result;
 
         public R getResult() {
             return this.result;
         }
     }
 }
