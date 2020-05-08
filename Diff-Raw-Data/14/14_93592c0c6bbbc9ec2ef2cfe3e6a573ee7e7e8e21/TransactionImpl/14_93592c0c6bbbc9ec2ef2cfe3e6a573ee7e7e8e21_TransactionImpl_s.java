 package ee.homies.gaffer;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.transaction.RollbackException;
 import javax.transaction.Status;
 import javax.transaction.Synchronization;
 import javax.transaction.Transaction;
 import javax.transaction.xa.XAException;
 import javax.transaction.xa.XAResource;
 
 import ee.homies.gaffer.util.*;
 
 public class TransactionImpl implements Transaction {
   private final static FormatLogger log = new FormatLogger(TransactionImpl.class);
 
   private int status = Status.STATUS_NO_TRANSACTION;
   private final Uid globalTransactionId;
   private final List<XAResource> xaResources = new CopyOnWriteArrayList<>();
   private final List<Synchronization> synchronizations = new CopyOnWriteArrayList<>();
   private final List<Synchronization> interposedSynchronizations = new CopyOnWriteArrayList<>();
   private final Map<Object, Object> resources = new ConcurrentHashMap<>();
   private final Clock clock;
   private final long startTimeMillis;
   private long timeoutMillis = -1;
   private boolean notAbandoned;
   private boolean suspended;
   private final ExceptionThrower exceptionThrower;
 
   public TransactionImpl() {
     ServiceRegistry serviceRegistry = ServiceRegistryHolder.getServiceRegistry();
     String instanceId = serviceRegistry.getConfiguration().getInstanceId();
     this.clock = serviceRegistry.getClock();
     startTimeMillis = clock.currentTimeMillis();
     globalTransactionId = new UidImpl(instanceId, startTimeMillis);
     exceptionThrower = new ExceptionThrower(serviceRegistry.getConfiguration().isLogExceptions());
   }
 
   public void setSuspended(boolean suspended) {
     this.suspended = suspended;
   }
 
   public void putResource(Object key, Object val) {
     if (key == null) {
       exceptionThrower.throwException(new IllegalArgumentException("Resource key can not be null."));
     }
     resources.put(key, val);
   }
 
   public Object getResource(Object key) {
     if (key == null) {
       exceptionThrower.throwException(new IllegalArgumentException("Resource key can not be null."));
     }
     return resources.get(key);
   }
 
   public void begin(Integer timeoutSeconds) {
     if (log.isDebugEnabled()) {
       log.debug("Starting transaction '%s' with timeout of '%s' seconds.", getTransactionInfo(), timeoutSeconds == null ? "infinite" : timeoutSeconds);
     }
     if (timeoutSeconds != null) {
       setTimeoutMillis(timeoutSeconds * 1000);
     }
     status = Status.STATUS_ACTIVE;
   }
 
   @Override
   public void commit() throws RollbackException, IllegalStateException {
     if (log.isDebugEnabled()) {
       log.debug("Committing transaction '%s'.", getTransactionInfo());
     }
     if (status == Status.STATUS_NO_TRANSACTION) {
       exceptionThrower.throwException(new IllegalStateException("Can not commit '" + getTransactionInfo() + "'. Transaction has not been started."));
     }
     if (isDoneOrFinishing()) {
       exceptionThrower.throwException(new IllegalStateException("Can not commit '" + getTransactionInfo() + "' with status '" + status
           + "''. Transaction is finishing or finished."));
     }
 
     try {
       fireBeforeCompletionEvent();
     } catch (RuntimeException e) {
       rollback();
       exceptionThrower.throwException(new RollbackExceptionImpl("Can not commit '" + getTransactionInfo() + "'. Before completion event firing failed.", e));
     }
 
     if (status == Status.STATUS_MARKED_ROLLBACK) {
       rollback();
       exceptionThrower.throwException(new RollbackExceptionImpl("Can not commit '" + getTransactionInfo() + "'. Transaction was marked as to be rolled back."));
     }
 
     if (isTimedOut()) {
       rollback();
       exceptionThrower.throwException(new RollbackExceptionImpl("Can not commit '" + getTransactionInfo() + "'. Transaction has timed out."));
     }
 
     try {
       setStatus(Status.STATUS_COMMITTING);
 
       Exception ex = null;
       int idx = 0;
       for (XAResource xaResource : xaResources) {
         try {
           xaResource.commit(null, true);
         } catch (Exception e) {
           ex = e;
           break;
         }
         idx++;
       }
 
       if (ex != null) {
         if (idx > 0) {
           getTransactionManagerStatistics().markHeuristicCommit();
         }
         rollback();
         if (idx == 0) {
           exceptionThrower.throwException(new RollbackExceptionImpl("Can not commit '" + getTransactionInfo() + "'. Commiting a resource failed.", ex));
         }
         exceptionThrower.throwException(new HeuristicMixedExceptionImpl("Can not commit '" + getTransactionInfo() + "'. Commiting a resource failed.", ex));
       }
       setStatus(Status.STATUS_COMMITTED);
       notAbandoned = true;
       getTransactionManagerStatistics().markCommitted(suspended);
       if (log.isDebugEnabled()) {
         log.debug("Transaction '%s' successfully commited.", getTransactionInfo());
       }
     } finally {
       fireAfterCompletionEvent();
       xaResources.clear();
       resources.clear();
     }
   }
 
   @Override
   public boolean delistResource(XAResource xaRes, int flag) {
     if (log.isDebugEnabled()) {
       log.debug("Delisting resource '%s' for transaction '%s'.", xaRes, getTransactionInfo());
     }
     if (status == Status.STATUS_NO_TRANSACTION) {
       exceptionThrower.throwException(new IllegalStateException("Can not delist resource. Transaction '" + getTransactionInfo() + "' has not been started."));
     }
     if (isWorking()) {
       exceptionThrower.throwException(new IllegalStateException("Can not delist resource. Transaction '" + getTransactionInfo() + "' commit is in progress."));
     }
     return xaResources.remove(xaRes);
   }
 
   @Override
   public boolean enlistResource(XAResource xaRes) {
     if (log.isDebugEnabled()) {
       log.debug("Enlisting resource '%s' for transaction '%s'.", xaRes, getTransactionInfo());
     }
     if (status == Status.STATUS_NO_TRANSACTION) {
       exceptionThrower.throwException(new IllegalStateException("Can not enlist resource. Transaction '" + getTransactionInfo() + "' has not been started."));
     }
     if (status == Status.STATUS_MARKED_ROLLBACK) {
       exceptionThrower.throwException(new IllegalStateException("Can not enlist resource. Transaction '" + getTransactionInfo()
           + "' has been marked to roll back."));
     }
     if (isDoneOrFinishing()) {
       exceptionThrower
           .throwException(new IllegalStateException("Can not enlist resource. Transaction '" + getTransactionInfo() + "' is finished or finishing."));
     }
     if (!(xaRes instanceof DummyXAResource)) {
       exceptionThrower.throwException(new IllegalStateException("Full XA is not supported yet for transaction '" + getTransactionInfo() + "', only "
           + DummyXAResource.class + " can participate."));
     }
     xaResources.add(xaRes);
     return true;
   }
 
   @Override
   public int getStatus() {
     return status;
   }
 
   @Override
   public void registerSynchronization(Synchronization sync) throws RollbackException {
     if (log.isDebugEnabled()) {
       log.debug("Registering synchronization '%s' for transaction '%s'.", sync, getTransactionInfo());
     }
 
     if (getStatus() == Status.STATUS_MARKED_ROLLBACK) {
       exceptionThrower.throwException(new RollbackException("Transaction is marked as rollback-only."));
     }
     synchronizations.add(sync);
   }
 
   public void registerInterposedSynchronization(Synchronization sync) {
     if (log.isDebugEnabled()) {
       log.debug("Registering interposed synchronization '%s' for transaction '%s'.", sync, getTransactionInfo());
     }
     interposedSynchronizations.add(sync);
   }
 
   @Override
   public void rollback() {
     try {
       notAbandoned = true;
       if (log.isDebugEnabled()) {
         log.debug("Rolling back transaction '%s'.", getTransactionInfo());
       }
       setStatus(Status.STATUS_ROLLING_BACK);
       for (XAResource xaResource : xaResources) {
         try {
           xaResource.rollback(null);
         } catch (XAException e) {
           log.error("Rollback failed.", e);
         }
       }
       xaResources.clear();
       resources.clear();
       setStatus(Status.STATUS_ROLLEDBACK);
       getTransactionManagerStatistics().markRollback(suspended);
     } catch (RuntimeException e) {
       getTransactionManagerStatistics().markRollbackFailure(suspended);
       exceptionThrower.throwException(e);
     }
   }
 
   @Override
   public void setRollbackOnly() {
     if (log.isDebugEnabled()) {
       log.debug("Marking transaction '%s' to roll back.", getTransactionInfo());
     }
     setStatus(Status.STATUS_MARKED_ROLLBACK);
   }
 
   public void setStatus(int status) {
     if (log.isDebugEnabled()) {
       log.debug("Setting transaction '%s' status to '%s'.", getTransactionInfo(), TransactionStatuses.toString(status));
     }
     this.status = status;
   }
 
   public Uid getGlobalTransactionId() {
     return globalTransactionId;
   }
 
   @Override
   public boolean equals(Object o) {
     if (o == null) {
       return false;
     }
     if (!(o instanceof TransactionImpl)) {
       return false;
     }
     return getGlobalTransactionId().equals(((TransactionImpl) o).getGlobalTransactionId());
   }
 
   @Override
   public int hashCode() {
     return globalTransactionId.hashCode();
   }
 
   public long getStartTimeMillis() {
     return startTimeMillis;
   }
 
   public long getTimeoutMillis() {
     return timeoutMillis;
   }
 
   public void setTimeoutMillis(long timeoutMillis) {
     this.timeoutMillis = timeoutMillis;
   }
 
   public boolean isTimedOut() {
     long timeoutMillis = getTimeoutMillis();
     if (timeoutMillis < 0) {
       return false;
     }
     return clock.currentTimeMillis() > getStartTimeMillis() + timeoutMillis;
   }
 
   private void fireBeforeCompletionEvent() {
     for (Synchronization synchronization : synchronizations) {
       synchronization.beforeCompletion();
     }
     for (Synchronization synchronization : interposedSynchronizations) {
       synchronization.beforeCompletion();
     }
   }
 
   private void fireAfterCompletionEvent() {
     for (Synchronization synchronization : interposedSynchronizations) {
       try {
         synchronization.afterCompletion(getStatus());
       } catch (RuntimeException e) {
         log.error(e.getMessage(), e);
       }
     }
     for (Synchronization synchronization : synchronizations) {
       try {
         synchronization.afterCompletion(getStatus());
       } catch (RuntimeException e) {
         log.error(e.getMessage(), e);
       }
     }
   }
 
   public String getTransactionInfo() {
     return globalTransactionId + "/" + TransactionStatuses.toString(getStatus());
   }
 
   @Override
   protected void finalize() throws Throwable {
     try {
       if (!notAbandoned) {
         getTransactionManagerStatistics().markAbandoned();
       }
     } finally {
       super.finalize();
     }
   }
 
   private TransactionManagerImpl getTransactionManager() {
     return ServiceRegistryHolder.getServiceRegistry().getTransactionManager();
   }
 
   private TransactionManagerStatistics getTransactionManagerStatistics() {
     return getTransactionManager().getTransactionManagerStatistics();
   }
 
   private boolean isDoneOrFinishing() {
     switch (status) {
     case Status.STATUS_PREPARING:
     case Status.STATUS_PREPARED:
     case Status.STATUS_COMMITTING:
     case Status.STATUS_COMMITTED:
     case Status.STATUS_ROLLING_BACK:
     case Status.STATUS_ROLLEDBACK:
       return true;
     default:
       return false;
     }
   }
 
   private boolean isWorking() {
     switch (status) {
     case Status.STATUS_PREPARING:
     case Status.STATUS_PREPARED:
     case Status.STATUS_COMMITTING:
     case Status.STATUS_ROLLING_BACK:
       return true;
     default:
       return false;
     }
   }
 }
