 package org.multiverse.stms;
 
 import org.multiverse.MultiverseConstants;
 import org.multiverse.api.*;
 import org.multiverse.api.exceptions.DeadTransactionException;
 import org.multiverse.api.exceptions.NoRetryPossibleException;
 import org.multiverse.api.latches.Latch;
 import org.multiverse.api.lifecycle.TransactionLifecycleEvent;
 import org.multiverse.api.lifecycle.TransactionLifecycleListener;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import static java.text.MessageFormat.format;
 
 /**
  * An abstract {@link org.multiverse.api.Transaction} implementation that contains most of the plumbing logic. Extend
  * this and prevent duplicate logic.
  * <p/>
  * The do-methods can be overridden.
  * <p/>
  * AbstractTransaction requires the clock.time to be at least 1. It used the version field to encode the transaction
  * state. See the version field for more information.
  *
  * @author Peter Veentjer.
  */
 public abstract class AbstractTransaction<C extends AbstractTransactionConfiguration, S extends AbstractTransactionSnapshot>
         implements Transaction, MultiverseConstants {
 
     protected final C config;
 
     private List<TransactionLifecycleListener> listeners;
 
     protected long version;
 
     public static final int NEW = 0;
     public static final int ACTIVE = 1;
     public static final int PREPARED = 2;
     public static final int COMMITTED = 3;
     public static final int ABORTED = 4;
 
     private TransactionStatus status = TransactionStatus.New;
     protected int statusInt = NEW;
     protected long timeoutNs;
 
     private int attempt;
 
     public AbstractTransaction(C config) {
         assert config != null;
         this.config = config;
         this.timeoutNs = config.getTimeoutNs();
     }
 
     @Override
     public final int getAttempt() {
         return attempt;
     }
 
     @Override
     public final void setAttempt(int attempt) {
         this.attempt = attempt;
     }
 
     @Override
     public final long getRemainingTimeoutNs() {
         return timeoutNs;
     }
 
     @Override
     public final void setRemainingTimeoutNs(long newTimeoutNs) {
        if (newTimeoutNs > timeoutNs) {
             throw new IllegalArgumentException();
         }
         this.timeoutNs = newTimeoutNs;
     }
 
     @Override
     public final long getReadVersion() {
         return version;
     }
 
     @Override
     public final TransactionStatus getStatus() {
         return status;
     }
 
     @Override
     public final TransactionConfiguration getConfiguration() {
         return config;
     }
 
     @Override
     public final Stm getStm() {
         return config.transactionFactory.getStm();
     }
 
     @Override
     public final TransactionFactory getTransactionFactory() {
         return config.transactionFactory;
     }
 
     /**
      * Method is designed to be overridden to add custom behavior on the init of the transaction.
      */
     protected void doStart() {
     }
 
     protected void doReset() {
     }
 
     @Override
     public void start() {
         if (___TRACING_ENABLED) {
             if (config.traceLevel.isLogableFrom(TraceLevel.course)) {
                 System.out.println(config.familyName + " starting");
             }
         }
 
         switch (status) {
             case New:
                 boolean success = false;
                 try {
                     notifyAll(TransactionLifecycleEvent.PreStart);
                     status = TransactionStatus.Active;
                     statusInt = ACTIVE;
                     version = config.clock.getVersion();
                     doStart();
                     success = true;
                 } finally {
                     if (!success) {
                         abort();
                     }
                 }
                 break;
             case Active:
                 //ignore
                 break;
             case Prepared:
                 String preparedMsg = format("Can't start already prepared transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(preparedMsg);
             case Committed:
                 String committedMsg = format("Can't start already committed transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(committedMsg);
             case Aborted:
                 String abortMsg = format("Can't start already aborted transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(abortMsg);
             default:
                 throw new IllegalStateException("unhandled transactionStatus: " + status);
         }
     }
 
     @Override
     public final void registerLifecycleListener(TransactionLifecycleListener listener) {
         if (___TRACING_ENABLED) {
             if (config.traceLevel.isLogableFrom(TraceLevel.course)) {
                 System.out.println(config.familyName + " registerLifecycleListener");
             }
         }
 
         switch (status) {
             case New:
                 //fall through
             case Active:
                 //fall through
             case Prepared:
                 if (listener == null) {
                     throw new NullPointerException();
                 }
 
                 if (listeners == null) {
                     listeners = new LinkedList<TransactionLifecycleListener>();
                 }
                 listeners.add(listener);
                 break;
             case Committed:
                 String committedMsg = format("Can't register TransactionLifecycleListener on already committed " +
                         "transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(committedMsg);
             case Aborted:
                 String abortMsg = format("Can't register TransactionLifecycleListener on already aborted transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(abortMsg);
             default:
                 throw new IllegalStateException("unhandled transactionStatus: " + status);
         }
     }
 
     @Override
     public final void prepare() {
         if (___TRACING_ENABLED) {
             if (config.traceLevel.isLogableFrom(TraceLevel.course)) {
                 System.out.println(config.familyName + " preparing");
             }
         }
 
         switch (status) {
             case New:
                 start();
                 //fall through
             case Active:
                 try {
                     notifyAll(TransactionLifecycleEvent.PreCommit);
 
                     //todo: the pre-commit tasks need to be executed
                     doPrepare();
                     status = TransactionStatus.Prepared;
                     statusInt = PREPARED;
                 } finally {
                     if (status != TransactionStatus.Prepared) {
                         abort();
                     }
                 }
                 break;
             case Prepared:
                 //ignore
                 break;
             case Committed:
                 String committedMsg = format("Can't prepare already committed transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(committedMsg);
             case Aborted:
                 String abortedMsg = format("Can't prepare already aborted transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(abortedMsg);
             default:
                 throw new IllegalStateException("unhandled transactionStatus: " + status);
         }
     }
 
     protected void doPrepare() {
     }
 
     @Override
     public final void reset() {
         if (___TRACING_ENABLED) {
             if (config.traceLevel.isLogableFrom(TraceLevel.course)) {
                 System.out.println(config.familyName + " reset");
             }
         }
 
         switch (status) {
             case New:
                 //fall through
             case Active:
                 //fall through
             case Prepared:
                 abort();
                 //fall through
             case Committed:
                 //fall through
             case Aborted:
                 clearListeners();
                 doReset();
                 version = 0;
                 status = TransactionStatus.New;
                 statusInt = NEW;
                 break;
             default:
                 throw new IllegalStateException("unhandled transactionStatus: " + status);
         }
     }
 
     private void clearListeners() {
         if (listeners != null) {
             listeners.clear();
         }
     }
 
     /**
      * Executes the ScheduledTasks for the specific event. The tasks field is not changed. If the tasks fields is null,
      * the call is ignored. If a task fails, the other tasks won't be executed and the thrown error will be propagated.
      *
      * @param event the TransactionLifecycleEvent to execute the tasks for.
      */
     private void notifyAll(TransactionLifecycleEvent event) {
         if (listeners == null) {
             return;
         }
 
         for (TransactionLifecycleListener listener : listeners) {
             listener.notify(this, event);
         }
     }
 
     @Override
     public final void abort() {
         if (___TRACING_ENABLED) {
             if (config.traceLevel.isLogableFrom(TraceLevel.course)) {
                 System.out.println(config.familyName + " aborting");
             }
         }
 
         switch (status) {
             case New:
                 //fall through
             case Active:
                 //fall through
             case Prepared:
                 try {
                     try {
                         notifyAll(TransactionLifecycleEvent.PreAbort);
                     } finally {
                         status = TransactionStatus.Aborted;
                         statusInt = ABORTED;
                         if (status == TransactionStatus.Active) {
                             doAbortActive();
                         } else {
                             doAbortPrepared();
                         }
                     }
                     notifyAll(TransactionLifecycleEvent.PostAbort);
                 } finally {
                     clearListeners();
                 }
                 break;
             case Committed:
                 String committedMsg = format("Can't abort already committed transaction '%s'", config.getFamilyName());
                 throw new DeadTransactionException(committedMsg);
             case Aborted:
                 //ignore
                 break;
             default:
                 throw new IllegalStateException("unhandled transactionStatus: " + status);
         }
     }
 
     protected void doAbortPrepared() {
     }
 
     /**
      * Method is designed to be overridden to add custom behavior on the abort of the transaction.
      */
     protected void doAbortActive() {
     }
 
     @Override
     public final void commit() {
         if (___TRACING_ENABLED) {
             if (config.traceLevel.isLogableFrom(TraceLevel.course)) {
                 System.out.println(config.familyName + " committing");
             }
         }
 
         switch (status) {
             case New:
                 status = TransactionStatus.Committed;
                 statusInt = COMMITTED;
                 break;
             case Active:
                 prepare();
                 //fall through
             case Prepared:
                 try {
                     makeChangesPermanent();
                     status = TransactionStatus.Committed;
                     statusInt = COMMITTED;
                     notifyAll(TransactionLifecycleEvent.PostCommit);
                 } finally {
                     if (status != TransactionStatus.Committed) {
                         abort();
                     }
                 }
                 break;
             case Committed:
 
                 //ignore the call
                 return;
             case Aborted:
                 String abortedMsg = format("Can't commit already aborted transaction '%s'", config.getFamilyName());
                 throw new DeadTransactionException(abortedMsg);
             default:
                 throw new IllegalStateException("unhandled transactionStatus: " + status);
         }
     }
 
     protected void makeChangesPermanent() {
     }
 
     @Override
     public final void registerRetryLatch(Latch latch) {
         if (___TRACING_ENABLED) {
             if (config.traceLevel.isLogableFrom(TraceLevel.course)) {
                 System.out.println(config.familyName + " registerRetryLatch");
             }
         }
 
         switch (status) {
             case New:
                 start();
                 //fall through
             case Active:
                 //fall through
             case Prepared:
                 if (latch == null) {
                     throw new NullPointerException();
                 }
 
                 boolean success = doRegisterRetryLatch(latch, version + 1);
 
                 if (!success) {
                     String msg = format("No retry is possible on transaction '%s' because it has no tracked reads, "
                             + "or not all reads are known (because it doesn''t support automatic read tracking) ",
                             config.getFamilyName());
                     throw new NoRetryPossibleException(msg);
                 }
                 break;
             case Committed:
                 String commitMsg = format("No retry is possible on already committed transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(commitMsg);
             case Aborted: {
                 String abortedMsg = format("No retry is possible on already aborted transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(abortedMsg);
             }
             default:
                 throw new IllegalStateException("unhandled transactionStatus: " + status);
         }
     }
 
     /**
      * Register retry. Default implementation returns 0, indicating that a retry is not possible.
      * <p/>
      * This method is designed to be overridden.
      *
      * @param latch         the latch to registerLifecycleListener. Value will never be null.
      * @param wakeupVersion the minimal version of the transactional object to wakeup for.
      * @return true if there were tracked reads (so the latch was opened or registered at at least 1 transactional
      *         object)
      */
     protected boolean doRegisterRetryLatch(Latch latch, long wakeupVersion) {
         return false;
     }
 
     public final void startOr() {
         switch (status) {
             case Active:
                 S snapshot = takeSnapshot();
                 snapshot.parent = getSnapshot();
                 snapshot.tasks = null;//listeners;
                 storeSnapshot(snapshot);
                 break;
             case Committed:
                 String commitMsg = format("Can't call startOr on already committed transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(commitMsg);
             case Aborted:
                 String abortMsg = format("Can't call startOr on already aborted transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(abortMsg);
             default:
                 throw new IllegalStateException("unhandled transactionStatus: " + status);
         }
     }
 
     public final void endOr() {
         switch (status) {
             case Active:
                 S snapshot = getSnapshot();
                 if (snapshot == null) {
                     throw new IllegalStateException();//improve exception
                 }
 
                 storeSnapshot((S) snapshot.parent);
                 break;
             case Committed:
                 String committedMsg = format("Can't call endOr on already committed transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(committedMsg);
             case Aborted:
                 String abortedMsg = format("Can't call endOr on already aborted transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(abortedMsg);
             default:
                 throw new IllegalStateException("unhandled transactionStatus: " + status);
         }
     }
 
     public final void endOrAndStartElse() {
         switch (status) {
             case Active:
                 S snapshot = getSnapshot();
                 if (snapshot == null) {
                     throw new IllegalStateException();
                 }
 
                 //listeners = snapshot.tasks;
                 snapshot.restore();
                 storeSnapshot((S) snapshot.parent);
                 break;
             case Committed:
                 String commitMsg = format("Can't call endOrAndStartElse on already committed transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(commitMsg);
             case Aborted:
                 String abortMsg = format("Can't call endOrAndStartElse on already aborted transaction '%s'",
                         config.getFamilyName());
                 throw new DeadTransactionException(abortMsg);
             default:
                 throw new IllegalStateException("unhandled transactionStatus: " + status);
         }
     }
 
     protected S takeSnapshot() {
         throw new UnsupportedOperationException();
     }
 
     protected S getSnapshot() {
         throw new UnsupportedOperationException();
     }
 
     protected void storeSnapshot(S snapshot) {
         throw new UnsupportedOperationException();
     }
 }
