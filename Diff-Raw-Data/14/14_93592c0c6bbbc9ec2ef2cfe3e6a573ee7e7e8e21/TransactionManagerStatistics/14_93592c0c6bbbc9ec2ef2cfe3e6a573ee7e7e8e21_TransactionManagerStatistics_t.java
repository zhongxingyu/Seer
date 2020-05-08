 package ee.homies.gaffer;
 
 import java.util.concurrent.atomic.AtomicLong;
 
 import ee.homies.gaffer.util.MBeanUtil;
 
 public class TransactionManagerStatistics implements TransactionManagerStatisticsMXBean {
   private final AtomicLong startedTransactionsCount = new AtomicLong();
   private final AtomicLong committedTransactionsCount = new AtomicLong();
   private final AtomicLong rolledBackTransactionsCount = new AtomicLong();
   private final AtomicLong activeTransactionsCount = new AtomicLong();
   private final AtomicLong suspendedTransactionsCount = new AtomicLong();
   private final AtomicLong abandonedTransactionsCount = new AtomicLong();
   private final AtomicLong failedRollbacksCount = new AtomicLong();
   private final AtomicLong heuristicCommitsCount = new AtomicLong();
 
   public TransactionManagerStatistics() {
     MBeanUtil.registerMBeanQuietly(this, "ee.homies.gaffer:type=TransactionManagerStatistics");
   }
 
   @Override
   public long getStartedTransactionsCount() {
     return startedTransactionsCount.get();
   }
 
   @Override
   public long getCommittedTransactionsCount() {
     return committedTransactionsCount.get();
   }
 
   @Override
   public long getRolledBackTransactionsCount() {
     return rolledBackTransactionsCount.get();
   }
 
   @Override
   public long getActiveTransactionsCount() {
     return activeTransactionsCount.get();
   }
 
   @Override
   public long getSuspendedTransactionsCount() {
     return suspendedTransactionsCount.get();
   }
 
   @Override
   public long getAbandonedTransactionsCount() {
     return abandonedTransactionsCount.get();
   }
 
   @Override
   public long getFailedRollbacksCount() {
     return failedRollbacksCount.get();
   }
 
   @Override
   public long getHeuristicCommitsCount() {
     return heuristicCommitsCount.get();
   }
 
   public void markBegin() {
     startedTransactionsCount.incrementAndGet();
     activeTransactionsCount.incrementAndGet();
   }
 
   public void markSuspend() {
     activeTransactionsCount.decrementAndGet();
     suspendedTransactionsCount.incrementAndGet();
   }
 
   public void markResume() {
     activeTransactionsCount.incrementAndGet();
     suspendedTransactionsCount.decrementAndGet();
   }
 
   public void markAbandoned() {
     abandonedTransactionsCount.incrementAndGet();
   }
 
   public void markCommitted(boolean wasSuspended) {
     committedTransactionsCount.incrementAndGet();
     if (wasSuspended) {
       suspendedTransactionsCount.decrementAndGet();
     } else {
       activeTransactionsCount.decrementAndGet();
     }
   }
 
   public void markRollback(boolean wasSuspended) {
    rolledBackTransactionsCount.incrementAndGet();
     if (wasSuspended) {
       suspendedTransactionsCount.decrementAndGet();
     } else {
       activeTransactionsCount.decrementAndGet();
     }
   }
 
   public void markRollbackFailure(boolean wasSuspended) {
     failedRollbacksCount.incrementAndGet();
     if (wasSuspended) {
       suspendedTransactionsCount.decrementAndGet();
     } else {
       activeTransactionsCount.decrementAndGet();
     }
   }
 
   public void markHeuristicCommit() {
     heuristicCommitsCount.incrementAndGet();
   }
 
 }
