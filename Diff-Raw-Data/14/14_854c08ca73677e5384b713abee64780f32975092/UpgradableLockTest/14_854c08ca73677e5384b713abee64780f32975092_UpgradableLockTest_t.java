 package javaUtilities;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.*;
 import java.util.concurrent.*;
 import java.util.concurrent.atomic.*;
 import java.util.concurrent.locks.Condition;
 
 import javaUtilities.UpgradableLock.Mode;
 
 import org.junit.*;
 import org.junit.rules.Timeout;
 
 public class UpgradableLockTest {
   private static int MAX_TEST_LENGTH_MILLIS = 5_000;
   private static long MAX_WAIT_FOR_LOCK_MILLIS = 10;
   
   private UpgradableLock myLock;
   
   @Rule
   public Timeout myTimeout = new Timeout(MAX_TEST_LENGTH_MILLIS);
   
   @Before
   public void setup() {
     myLock = new UpgradableLock();
     Thread.interrupted();
   }
 
   @Test
   public void testWriteLock() throws Throwable {
     myLock.lock(Mode.WRITE);
     myLock.lock(Mode.READ);
     assertTrue(hasWriter());
     myLock.unlock();
     assertTrue(hasWriter());
     myLock.unlock();
     assertTrue(isUnlocked());
   }
 
   @Test
   public void testUpgrading() throws Throwable {
     myLock.lock(Mode.UPGRADABLE);
     assertTrue(hasUpgradable());
     myLock.lock(Mode.READ);
     assertTrue(hasUpgradable());
     myLock.upgrade();
     assertTrue(hasWriter());
     myLock.downgrade();
     assertTrue(hasUpgradable());
     myLock.lock(Mode.WRITE);
     assertTrue(hasWriter());
     myLock.unlock();
     assertTrue(hasUpgradable());
     myLock.unlock();
     assertTrue(hasUpgradable());
     myLock.unlock();
     assertTrue(isUnlocked());
   }
 
   @Test
   public void testReadLock() throws Throwable {
     myLock.lock(Mode.READ);
     assertTrue(hasReaders());
     myLock.lock(Mode.READ);
     assertTrue(hasReaders());
     myLock.unlock();
     assertTrue(hasReaders());
     myLock.unlock();
     assertTrue(isUnlocked());
   }
   
   @Test
   public void clearUpgradesAfterFullUnlock() throws Throwable {
     myLock.lock(Mode.UPGRADABLE);
     myLock.upgrade();
     assertTrue(hasWriter());
     myLock.unlock();
     assertTrue(isUnlocked());
     myLock.lock(Mode.UPGRADABLE);
     assertTrue(hasUpgradable());
   }
   
   @Test
   public void keepUpgradesAfterReleasingWriteLock() throws Throwable {
     myLock.lock(Mode.UPGRADABLE);
     myLock.lock(Mode.WRITE);
     myLock.upgrade();
     assertTrue(hasWriter());
     myLock.unlock();
     assertTrue(hasWriter());
     myLock.downgrade();
     assertTrue(hasUpgradable());
   }
   
   @Test
   public void testTimeout() throws InterruptedException {
     lockPermanently(Mode.WRITE);
     for (Mode mMode : Mode.values()) {
       boolean mSuccess = myLock.tryLock(mMode, 100, TimeUnit.MICROSECONDS);
       assertFalse(mSuccess);
     }
   }
   
   @Test
   public void testNegativeTimeout() throws InterruptedException {
     lockPermanently(Mode.UPGRADABLE);
     boolean mSuccess = myLock.tryLock(Mode.UPGRADABLE, -3L, TimeUnit.MICROSECONDS);
     assertFalse(mSuccess);
   }
   
   @Test
   public void testTryLockWhenAvailable() throws Throwable {
     for (Mode mMode : Mode.values()) {
       assertTrue(myLock.tryLock(mMode));
       myLock.unlock();
       assertTrue(isUnlocked());
     }
   }
   
   @Test
   public void testTryLockWhenNotAvailable() throws InterruptedException {
     lockPermanently(Mode.WRITE);
     for (Mode mMode : Mode.values()) {
       assertFalse(myLock.tryLock(mMode));
     }
   }
 
   @Test
   public void testInterruption() throws InterruptedException {
     final AtomicBoolean mInterrupted = new AtomicBoolean();
     Thread mThread = new Thread() {
       @Override
       public void run() {
         try {
           myLock.lockInterruptibly(Mode.UPGRADABLE);
         } catch (InterruptedException e) {
           mInterrupted.set(true);
         }
       }
     };
     lockPermanently(Mode.WRITE);
     mThread.start();
     Thread.sleep(MAX_WAIT_FOR_LOCK_MILLIS);
     mThread.interrupt();
     mThread.join();
     assertTrue(mInterrupted.get());
   }
   
   @Test
   public void retainInterruptedStatusWithTryLock() throws InterruptedException {
     Thread mCurrent = Thread.currentThread();
     mCurrent.interrupt();
     assertTrue(myLock.tryLock(Mode.WRITE));
     myLock.unlock();
     assertTrue(Thread.interrupted());
     lockPermanently(Mode.READ);
     mCurrent.interrupt();
     assertFalse(myLock.tryLock(Mode.WRITE));
     assertTrue(Thread.interrupted());
   }
   
   /*
    * Several threads with read locks and one thread with an upgradable lock
    * wait on a barrier.
    */
   @Test
   public void allowConcurrentReadAndUpgradableAccess() throws Throwable {
     int mNThreads = 4;
     final CyclicBarrier mBarrier = new CyclicBarrier(mNThreads);
     final AtomicReference<Throwable> mError = new AtomicReference<Throwable>();
     ExecutorService mPool = Executors.newCachedThreadPool();
     for (int i = 0; i < mNThreads - 1; i++) {
       mPool.execute(newBarrierTask(mBarrier, Mode.READ, mError));
     }
     mPool.execute(newBarrierTask(mBarrier, Mode.UPGRADABLE, mError));
     mPool.shutdown();
     assertTrue(mPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS));
     assertEquals(null, mError.get());
     assertTrue(isUnlocked());
   }
   
   private Runnable newBarrierTask(
       final CyclicBarrier aBarrier,
       final Mode aMode,
       final AtomicReference<Throwable> aError) {
     return new Runnable() {
       @Override
       public void run() {
         myLock.lock(aMode);
         try {
           aBarrier.await();
         } catch (Exception e) {
           aError.set(e);
         } finally {
           myLock.unlock();
         }
       }
     };
   }
 
 //  /*
 //   * Threads 0 - n wait on a condition for a counter to reach their index
 //   * numbers. Then they increment the counter and notify the other threads. The
 //   * counter starts at zero to allow the first thread to proceed without
 //   * waiting.
 //   */
 //  @Test
 //  public void testCondition() throws InterruptedException {
 //    final Condition mIncremented = myLock.newCondition();
 //    final AtomicInteger mCounter = new AtomicInteger();
 //    ExecutorService mPool = Executors.newCachedThreadPool();
 //    int mNThreads = 10;
 //    for (int i = 0; i < mNThreads; i++) {
 //      mPool.execute(newWaitTask(i, mCounter, mIncremented));
 //    }
 //    mPool.shutdown();
 //    mPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
 //    assertEquals(mNThreads, mCounter.get());
 //  }
   
   private Runnable newWaitTask(
       final int aExpectedCount,
       final AtomicInteger aCounter,
       final Condition aIncremented) {
     return new Runnable() {
       @Override
       public void run() {
         myLock.lock(Mode.WRITE);
         try {
           while (aCounter.get() != aExpectedCount) {
             aIncremented.await();
           }
           aCounter.incrementAndGet();
           aIncremented.signalAll();
         } catch (InterruptedException e) {
           // return
         } finally {
           myLock.unlock();
         }
       }
     };
   }
   
   @Test
   public void serializeWithWriter() throws Throwable {
     lockPermanently(Mode.WRITE);
     byte[] mSerializedLock = serialize(myLock);
     assertTrue(hasWriter());
     myLock = (UpgradableLock) deserialize(mSerializedLock);
     assertTrue(isUnlocked());
   }
   
   @Test
   public void serializeWithReaders() throws Throwable {
     Mode[] mModes = {Mode.READ, Mode.UPGRADABLE};
     for (Mode mMode : mModes) {
       lockPermanently(mMode);
       byte[] mSerializedLock = serialize(myLock);
       assertFalse(canLock(Mode.WRITE));
       myLock = (UpgradableLock) deserialize(mSerializedLock);
       assertTrue(isUnlocked());
     }
   }
   
   private static byte[] serialize(Serializable aValue) throws IOException {
     ByteArrayOutputStream mOS = new ByteArrayOutputStream();
     ObjectOutputStream mOOS = new ObjectOutputStream(mOS);
     mOOS.writeObject(aValue);
     return mOS.toByteArray();
   }
   
   private static Serializable deserialize(byte[] aSerialized) throws IOException, ClassNotFoundException {
     InputStream mIS = new ByteArrayInputStream(aSerialized);
     ObjectInputStream mOIS = new ObjectInputStream(mIS);
     return (Serializable) mOIS.readObject();
   }
   
 //  @Test(expected=IllegalMonitorStateException.class)
 //  public void preventWaitingWithoutWriteLock() throws InterruptedException {
 //    myLock.lock(Mode.READ);
 //    Condition mCondition = myLock.newCondition();
 //    mCondition.await();
 //  }
 //
 //  @Test(expected=IllegalMonitorStateException.class)
 //  public void preventSignallingWithoutWriteLock() {
 //    myLock.lock(Mode.UPGRADABLE);
 //    Condition mCondition = myLock.newCondition();
 //    mCondition.signal();
 //  }
 
   /*
    * One thread tries to acquire the write lock multiple times while several
    * threads use a counter to try to trade off acquiring the read lock.
    */
   @Test
   public void preventWriterStarvation() throws Throwable {
     Mode[] mModes = {Mode.WRITE, Mode.UPGRADABLE};
     for (Mode mMode : mModes) {
       final AtomicInteger mCounter = new AtomicInteger();
       ExecutorService mPool = Executors.newCachedThreadPool();
       for (int i = 0; i < 3; i++) {
         mPool.execute(new Runnable() {
           @Override
           public void run() {
             try {
               while (true) {
                 myLock.lockInterruptibly(Mode.READ);
                 try {
                   int mStartCount = mCounter.incrementAndGet();
                   long mEndTime = System.nanoTime() + 5_000_000;
                   while (mCounter.get() == mStartCount && System.nanoTime() < mEndTime) {
                     Thread.sleep(0, 1_000);
                   }
                 } finally {
                   myLock.unlock();
                 }
               }
             } catch (InterruptedException e) {
               // return
             }
           }
         });
       }
       for (int i = 0; i < 5; i++) {
         int mCount = mCounter.get();
         while (mCount == mCounter.get()) {
           // wait for readers to acquire lock
         }
         myLock.lock(mMode);
         myLock.upgrade();
         myLock.unlock();
       }
       mPool.shutdownNow();
       mPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
       assertTrue(isUnlocked());
     }
   }
   
   @Test
   public void releaseWriteWithWaitingThreads() throws Throwable {
     Mode[] mModes = {Mode.UPGRADABLE, Mode.WRITE};
     for (Mode mMode : mModes) {
       Thread mThread = new Thread() {
         @Override
         public void run() {
           myLock.lock(Mode.READ);
           myLock.unlock();
         }
       };
       myLock.lock(mMode);
       myLock.upgrade();
       mThread.start();
       Thread.sleep(MAX_WAIT_FOR_LOCK_MILLIS);
       myLock.downgrade();
       if (mMode == Mode.WRITE) myLock.unlock();
       mThread.join();
       if (mMode == Mode.UPGRADABLE) myLock.unlock();
       assertTrue(isUnlocked());
     }
   }
   
   @Test
   public void releaseReadWithWaitingThreads() throws Throwable {
     Mode[] mUnlockModes = {Mode.UPGRADABLE, Mode.READ};
     Mode[] mLockModes = {Mode.UPGRADABLE, Mode.WRITE};
     for (Mode mUnlockMode : mUnlockModes) {
       for (final Mode mLockMode : mLockModes) {
         Thread mThread = new Thread() {
           @Override
           public void run() {
             myLock.lock(mLockMode);
             myLock.upgrade();
             myLock.unlock();
           }
         };
         myLock.lock(mUnlockMode);
         mThread.start();
         Thread.sleep(MAX_WAIT_FOR_LOCK_MILLIS);
         myLock.unlock();
         mThread.join();
         assertTrue(isUnlocked());
       }
     }
   }
   
   /*
    * Thread 1 locks upgradable. Then Thread 2 waits for upgradable. Thread 3
    * temporarily locks read while Thread 1 tries to upgrade, ensuring that
    * Thread 1 blocks. This could cause a deadlock if Thread 3 signals the
    * longest waiting thread, instead of the thread waiting to upgrade.
    * Thread 1 should unblock and upgrade.
    */
   @Test
   public void upgradeAfterBlocking() throws Throwable {
     Thread mThread2 = new Thread() {
       @Override
       public void run() {
         myLock.lock(Mode.UPGRADABLE);
         myLock.unlock();
       }
     };
    final AtomicReference<Throwable> mError = new AtomicReference<>();
     final CyclicBarrier mBarrier = new CyclicBarrier(2);
     Thread mThread3 = new Thread() {
       @Override
       public void run() {
         try {
          myLock.lock(Mode.READ);
           mBarrier.await();
           Thread.sleep(MAX_WAIT_FOR_LOCK_MILLIS);
           myLock.unlock();
        } catch (Throwable e) {
          mError.set(e);
         }
       }
     };
     myLock.lock(Mode.UPGRADABLE);
     mThread2.start();
     Thread.sleep(MAX_WAIT_FOR_LOCK_MILLIS);
     mThread3.start();
     mBarrier.await();
     myLock.upgrade();
     mThread3.join();
    assertEquals(null, mError.get());
   }
 
   @Test(expected=IllegalMonitorStateException.class)
   public void preventLockingWriteAfterRead() throws Throwable {
     myLock.lock(Mode.READ);
     try {
       myLock.lock(Mode.WRITE);
     } finally {
       assertTrue(hasReaders());
       myLock.unlock();
       assertTrue(isUnlocked());
     }
   }
 
   @Test(expected=IllegalMonitorStateException.class)
   public void preventUpgradeFromRead() {
     myLock.lock(Mode.READ);
     myLock.upgrade();
   }
 
   @Test(expected=IllegalMonitorStateException.class)
   public void preventDowngradeWithoutUpgrade() throws Throwable {
     myLock.lock(Mode.UPGRADABLE);
     try {
       myLock.downgrade();
     } finally {
       myLock.unlock();
       assertTrue(isUnlocked());
     }
   }
 
   @Test(expected=IllegalMonitorStateException.class)
   public void preventUnlockWithoutLock() throws Throwable {
     try {
       myLock.unlock();
     } finally {
       assertTrue(isUnlocked());
     }
   }
   
   @Test(expected=NullPointerException.class)
   public void disallowNullLockModeWithLock() {
     myLock.lock(null);
   }
   
   @Test(expected=NullPointerException.class)
   public void disallowNullLockModeWithTryLock() {
     myLock.tryLock(null);
   }
   
   @Test(expected=NullPointerException.class)
   public void disallowNullTimeUnitWithLock() throws InterruptedException {
     myLock.tryLock(Mode.READ, 10, null);
   }
   
   @Test(expected=NullPointerException.class)
   public void disallowNullTimeUnitWithUpgrade() throws InterruptedException {
     myLock.lock(Mode.UPGRADABLE);
     myLock.tryUpgrade(-10, null);
   }
   
   private void lockPermanently(final Mode aMode) throws InterruptedException {
     final AtomicBoolean mSuccess = new AtomicBoolean();
     Thread mThread = new Thread() {
       @Override
       public void run() {
         mSuccess.set(myLock.tryLock(aMode));
       }
     };
     mThread.start();
     mThread.join();
     assertTrue(mSuccess.get());
   }
 
   private boolean isUnlocked() throws Throwable {
     return canLock(Mode.WRITE);
   }
   
   private boolean hasReaders() throws Throwable {
     return !canLock(Mode.WRITE) && canLock(Mode.UPGRADABLE);
   }
   
   private boolean hasUpgradable() throws Throwable {
     return !canLock(Mode.UPGRADABLE) && canLock(Mode.READ);
   }
   
   private boolean hasWriter() throws Throwable {
     return !canLock(Mode.READ);
   }
 
   private boolean canLock(final Mode aMode) throws Throwable {
     RunnableFuture<Boolean> mFuture = new FutureTask<>(new Callable<Boolean>() {
       @Override
       public Boolean call() {
         boolean mSuccess = myLock.tryLock(aMode);
         if (mSuccess) myLock.unlock();
         return mSuccess;
       }
     });
     new Thread(mFuture).start();
     try {
       return mFuture.get();
     } catch (ExecutionException e) {
       throw e.getCause();
     }
   }
 }
