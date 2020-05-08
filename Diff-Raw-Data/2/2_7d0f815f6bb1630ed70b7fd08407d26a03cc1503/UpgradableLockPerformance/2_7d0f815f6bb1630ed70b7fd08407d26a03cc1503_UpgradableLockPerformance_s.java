 package javaUtilities;
 
 import java.util.concurrent.*;
 import java.util.concurrent.locks.*;
 
 import javaUtilities.UpgradableLock.Mode;
 
 
 public class UpgradableLockPerformance {
   private static final int N_TRIALS = 10;
   private static final int N_THREADS = Runtime.getRuntime().availableProcessors() + 1;
   private static final int N_LOCKS = 1_000_000;
 
   private static final int COLUMN_WIDTH = 25;
   
   private int myCount = 0;
   
   public static void main(String[] aArgs) throws InterruptedException {
     printColumn("ReentrantReadWriteLock");
     printColumn("UpgradableLock");
    printColumn("ReentrantLock)");
     System.out.println();
     System.out.println();
     System.out.printf("%,d trials with %,d threads and %,d total locks per trial (ns/lock)", N_TRIALS, N_THREADS, N_LOCKS);
     long mReadWriteNanos = 0;
     long mUpgradableNanos = 0;
     long mReentrantLockNanos = 0;
     for (int i = 0; i < N_TRIALS; i++) {
       long mReadWriteTime = new UpgradableLockPerformance().timeNanos(new ReadWriteLockTest());
       long mUpgradableTime = new UpgradableLockPerformance().timeNanos(new UpgradableLockTest());
       long mReentrantLockTime = new UpgradableLockPerformance().timeNanos(new ReentrantLockTest());
       System.out.println();
       printTrial(mReadWriteTime);
       printTrial(mUpgradableTime);
       printTrial(mReentrantLockTime);
       mReadWriteNanos += mReadWriteTime;
       mUpgradableNanos += mUpgradableTime;
       mReentrantLockNanos += mReentrantLockTime;
     }
     System.out.println();
     System.out.println();
     long[] mTotals = {mReadWriteNanos, mUpgradableNanos, mReentrantLockNanos};
     System.out.println("Average (ns/lock)");
     for (long mTotal : mTotals) {
       printTrial(mTotal / N_TRIALS);
     }
     System.out.println();
     System.out.println();
     System.out.println("Total / ReentrantReadWriteLock:");
     for (long mTotal : mTotals) {
       double mRatio = (double) mTotal / mReadWriteNanos;
       printColumn(formatFloating(mRatio));
     }
     System.out.println();
   }
   
   private static void printTrial(long aNanos) {
     double mAvg = (double) aNanos / N_LOCKS;
     printColumn(formatFloating(mAvg));
   }
   
   private static void printColumn(String aString) {
     System.out.printf("%-" + COLUMN_WIDTH + "s", aString);
   }
   
   private static String formatFloating(double aFloating) {
     return String.format("%.4f", aFloating);
   }
   
   private long timeNanos(LockTest aTest) throws InterruptedException {
     assert myCount == 0;
     ExecutorService mPool = Executors.newFixedThreadPool(N_THREADS);
     long mStart = System.nanoTime();
     for (int i = 0; i < N_THREADS; i++) {
       Runnable mTask = newTask(aTest);
       mPool.execute(mTask); 
     }
     mPool.shutdown();
     mPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
     return System.nanoTime() - mStart;
   }
 
   private Runnable newTask(final LockTest aTest) {
     return new Runnable() {
       @Override
       public void run() {
         for (int i = 0; i < N_LOCKS / N_THREADS; i++) {
           switch (i % 4) {
             case 0:
               aTest.lockWrite();
               myCount++;
               aTest.unlockWrite();
               break;
             case 1:
               aTest.lockUpgradable();
               if (myCount % 2 == 0) {
                 aTest.upgrade();
                 myCount++;
                 aTest.downgrade();
               }
               aTest.unlockUpgradable();
               break;
             case 2: case 3:
               aTest.lockRead();
               if (myCount == 0.375 * N_LOCKS) {
                 System.out.print(" ");
               }
               aTest.unlockRead();
               break;
             default: throw new AssertionError();
           }
         }
       }
     };
   }
 
   private static interface LockTest {
     void lockRead();
     void unlockRead();
     void lockWrite();
     void unlockWrite();
     void lockUpgradable();
     void unlockUpgradable();
     void upgrade();
     void downgrade();
   }
   
   private static final class ReadWriteLockTest implements LockTest {
     private final ReadWriteLock mReadWrite = new ReentrantReadWriteLock();
     private final Lock myReadLock = mReadWrite.readLock();
     private final Lock myWriteLock = mReadWrite.writeLock();
     
     @Override
     public void lockRead() {
       myReadLock.lock();
     }
 
     @Override
     public void unlockRead() {
       myReadLock.unlock();
     }
 
     @Override
     public void lockWrite() {
       myWriteLock.lock();
     }
 
     @Override
     public void unlockWrite() {
       myWriteLock.unlock();
     }
 
     @Override
     public void lockUpgradable() {
       myWriteLock.lock();
     }
 
     @Override
     public void unlockUpgradable() {
       myWriteLock.unlock();
     }
 
     @Override
     public void upgrade() {
     }
 
     @Override
     public void downgrade() {
     }
   }
   
   private static final class UpgradableLockTest implements LockTest {
     private final UpgradableLock myUpgradableLock = new UpgradableLock();
     
     @Override
     public void lockRead() {
       myUpgradableLock.lock(Mode.READ);
     }
 
     @Override
     public void unlockRead() {
       myUpgradableLock.unlock();
     }
 
     @Override
     public void lockWrite() {
       myUpgradableLock.lock(Mode.WRITE);
     }
 
     @Override
     public void unlockWrite() {
       myUpgradableLock.unlock();
     }
 
     @Override
     public void lockUpgradable() {
       myUpgradableLock.lock(Mode.UPGRADABLE);
     }
 
     @Override
     public void unlockUpgradable() {
       myUpgradableLock.unlock();
     }
 
     @Override
     public void upgrade() {
       myUpgradableLock.upgrade();
     }
 
     @Override
     public void downgrade() {
       myUpgradableLock.downgrade();
     }
   }
 
   private static final class ReentrantLockTest implements LockTest {
     private final Lock myReentrantLock = new ReentrantLock();
     
     @Override
     public void lockRead() {
       myReentrantLock.lock();
     }
 
     @Override
     public void unlockRead() {
       myReentrantLock.unlock();
     }
 
     @Override
     public void lockWrite() {
       myReentrantLock.lock();
     }
 
     @Override
     public void unlockWrite() {
       myReentrantLock.unlock();
     }
 
     @Override
     public void lockUpgradable() {
       myReentrantLock.lock();
     }
 
     @Override
     public void unlockUpgradable() {
       myReentrantLock.unlock();
     }
 
     @Override
     public void upgrade() {
     }
 
     @Override
     public void downgrade() {
     }
   }
 }
