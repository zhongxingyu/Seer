 package javaUtilities;
 
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.concurrent.atomic.*;
 import java.util.concurrent.locks.*;
 
 /**
  * A reentrant read-write lock allowing at most one designated upgradable thread
  * that can switch between reading and writing. Other readers can acquire the
  * lock while the thread with the upgradable lock is downgraded. The upgradable
  * thread blocks while upgrading if other threads hold read locks.
  * <p>
  * A thread can initially acquire the lock in any of three modes: read,
  * upgradable, and write. A thread acquiring an upgradable lock starts in the
  * downgraded state. All locks and unlocks are nested. This means that a thread
  * cannot acquire a write lock, then a read lock, and then release the write
  * lock without releasing the read lock. Calls to downgrade must be matched by
  * calls to upgrade. Calls to upgrade and downgrade can be interleaved with
  * calls to lock and unlock in any order, as long as the thread has an
  * upgradable or write lock when upgrading or downgrading. A thread with only a
  * read lock cannot acquire an upgradable or write lock. Any thread with an
  * upgradable or write lock can acquire the lock again in any of the three
  * modes. Acquiring a read lock after an upgradable or write lock has no effect,
  * though it still must be released.
  * <p>
  * This class allows {@linkplain Condition condition} waits for threads that
  * hold write locks or have upgraded.
  * <p>
  * This lock allows a running thread to acquire the lock without waiting in the
  * queue, unless the thread is acquiring a read lock, and it detects a thread
  * waiting to upgrade or acquire a write lock at the front of the queue.
  * <p>
  * This class is {@linkplain Serializable serializable}. It is always
  * deserialized in the fully unlocked state.
  */
 public final class UpgradableLock implements Serializable {
   /*
    * This class stores each thread's lock holds in a thread local variable. It
    * uses a subclass of AbstractQueuedSynchronizer (mySync) to manage the queue
    * and store the number of threads with each type of lock hold. For every call
    * to a public method, this class first uses the thread local state to
    * determine whether the state of mySync must change. Then it delegates to
    * mySync and updates the thread local state on success.
    */
   
   private static final long serialVersionUID = 0L;
   
   private static final long MIN_TIMEOUT = -1L;
   private static final long NO_WAIT = -2L;
   private static final long NO_TIMEOUT = -3L;
   
   private final Sync mySync = new Sync();
   private final ThreadLocal<ThreadState> myThreadState = new ThreadLocal<ThreadState>() {
     @Override
     protected ThreadState initialValue() {
       return ThreadState.newState();
     }
   };
   
   /**
    * The modes used to acquire the lock.
    */
   public static enum Mode {
     READ,
     UPGRADABLE,
     WRITE
   }
 
   /**
    * Thrown when a thread attempts to acquire or upgrade the lock when the lock
    * already has the maximum number of holds.
    */
   public static final class TooManyHoldsException extends RuntimeException {
     private static final long serialVersionUID = 0L;
 
     TooManyHoldsException(String aMessage) {
       super(aMessage);
     }
   }
   
   /**
    * Lock state that applies to the current thread. It stores the numbers
    * and types of holds that the thread currently has.
    */
   private static final class ThreadState {
     private static final int NO_WRITE_HOLDS = -1;
     private static final ThreadState NEW = new ThreadState(FirstHold.NONE, 0, 0, NO_WRITE_HOLDS);
     
     private final FirstHold myFirstHold;
     private final int myUpgradeCount;
     private final int myHoldCount;
     private final int myFirstWriteHold;
     
     private static enum FirstHold {
       NONE,
       READ,
       UPGRADABLE,
       WRITE;
     }
 
     static ThreadState newState() {
       // reuse instance for efficiency
       return NEW;
     }
     
     private ThreadState(FirstHold aFirstHold, int aUpgrades, int aHolds, int aFirstWrite) {
       myFirstHold = aFirstHold;
       myUpgradeCount = aUpgrades;
       myHoldCount = aHolds;
       myFirstWriteHold = aFirstWrite;
     }
     
     Mode getFirstHold() {
       switch (myFirstHold) {
         case NONE: throw new IllegalArgumentException("No hold yet");
         case READ: return Mode.READ;
         case UPGRADABLE: return Mode.UPGRADABLE;
         case WRITE: return Mode.WRITE;
         default: throw new AssertionError();
       }
     }
 
     boolean acquiredReadFirst() {
       return myFirstHold == FirstHold.READ;
     }
     
     boolean isUnlocked() {
       return myHoldCount == 0;
     }
     
     /**
      * Returns {@code true} if the thread holds only a read lock or a downgraded
      * upgradable lock.
      */
     boolean canWrite() {
       return myFirstHold == FirstHold.WRITE ||
           myFirstHold == FirstHold.UPGRADABLE &&
           (myUpgradeCount > 0 || myFirstWriteHold != NO_WRITE_HOLDS);
     }
 
     boolean isDowngraded() {
       return myUpgradeCount == 0;
     }
 
     ThreadState incrementWrite() {
       FirstHold mFirst = myFirstHold == FirstHold.NONE ? FirstHold.WRITE : myFirstHold;
       int mNewHolds = incrementHolds();
       int mFirstWrite = (myFirstWriteHold == NO_WRITE_HOLDS) ? mNewHolds : myFirstWriteHold;
       return new ThreadState(mFirst, myUpgradeCount, mNewHolds, mFirstWrite);
     }
 
     ThreadState incrementUpgradable() {
       FirstHold mFirst = myFirstHold == FirstHold.NONE ? FirstHold.UPGRADABLE : myFirstHold;
       int mNewHolds = incrementHolds();
       return new ThreadState(mFirst, myUpgradeCount, mNewHolds, myFirstWriteHold);
     }
 
     ThreadState incrementRead() {
       FirstHold mFirst = myFirstHold == FirstHold.NONE ? FirstHold.READ : myFirstHold;
       int mNewHolds = incrementHolds();
       return new ThreadState(mFirst, myUpgradeCount, mNewHolds, myFirstWriteHold);
     }
 
     ThreadState decrementHolds() {
       int mFirstWrite = (myFirstWriteHold == myHoldCount) ? NO_WRITE_HOLDS : myFirstWriteHold;
       int mNewHolds = myHoldCount - 1;
       return new ThreadState(myFirstHold, myUpgradeCount, mNewHolds, mFirstWrite);
     }
 
     ThreadState upgrade() {
       if (myUpgradeCount == Integer.MAX_VALUE) {
         throw new TooManyHoldsException("Too many upgrades");
       }
       return new ThreadState(myFirstHold, myUpgradeCount + 1, myHoldCount, myFirstWriteHold);
     }
 
     ThreadState downgrade() {
       return new ThreadState(myFirstHold, myUpgradeCount - 1, myHoldCount, myFirstWriteHold);
     }
     
     private int incrementHolds() {
       if (myHoldCount == Integer.MAX_VALUE) {
         throw new TooManyHoldsException("Too many holds");
       }
       return myHoldCount + 1;
     }
   }
 
   private static final class Sync {
     /*
      * This class uses its int state to maintain 3 counts:
      * 
      * - threads with read locks
      * - threads with downgraded upgradable locks
      * - threads with write/upgraded locks
      * 
      * It does not keep track of the number of holds per thread or which holds
      * allow downgrading. The lowest bit stores the number of writer threads,
      * the next bit stores the number of downgraded threads, and the rest of
      * the state stores the number of reader threads.
      */
     
     private static final int MAX_READ_HOLDS = Integer.MAX_VALUE >>> 2;
     
     private final AtomicInteger myState = new AtomicInteger(calcState(false, false, 0));
     private final Queue<Node> myQueue = new ConcurrentLinkedQueue<>();
     private final AtomicReference<Thread> myUpgrading = new AtomicReference<>();
     
     private static final class Node {
       final Mode myMode;
       final Thread myThread;
       
       Node(Mode aMode, Thread aThread) {
         myMode = aMode;
         myThread = aThread;
       }
     }
     
     boolean lock(Mode aMode, boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
       if (aInterruptible && Thread.interrupted()) {
         throw new InterruptedException();
       }
       if (tryLock(aMode)) return true;
       if (aTime == NO_WAIT) return false;
       return enqueue(aMode, aInterruptible, aTime, aUnit);
     }
 
     boolean upgrade(boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
       if (aInterruptible && Thread.interrupted()) {
         throw new InterruptedException();
       }
       if (tryUpgrade()) return true;
       if (aTime == NO_WAIT) return false;
       return enqueueForUpgrade(aInterruptible, aTime, aUnit);
     }
     
     void unlock(Mode aMode) {
       switch (aMode) {
         case WRITE:
           myState.set(calcState(false, false, 0));
           break;
         case UPGRADABLE: {
           int mState;
           int mNewState;
           do {
             mState = myState.get();
             mNewState = setUpgradableHold(mState, false);
           } while (!myState.compareAndSet(mState, mNewState));
           unparkNext(EnumSet.of(Mode.UPGRADABLE, Mode.WRITE), false);
           break;
         }
         case READ: 
           int mState;
           int mNewState;
           do {
             mState = myState.get();
             int mReadHolds = getReadHolds(mState);
             mNewState = setReadHolds(mState, mReadHolds - 1);
           } while (!myState.compareAndSet(mState, mNewState));
           unparkNext(EnumSet.of(Mode.WRITE), true);
           break;
         default: throw new AssertionError();
       }
       
     }
     
     void downgrade() {
       myState.set(calcState(false, true, 0));
       unparkNext(EnumSet.of(Mode.READ), false);
     }
 
     
     private boolean tryLock(Mode aMode) {
       int mState = myState.get();
       if (hasWriteHold(mState)) return false;
       int mNewState;
       switch (aMode) {
         case READ:
           mNewState = setReadHolds(mState, getReadHolds(mState) + 1);
           break;
         case UPGRADABLE:
           if (hasUpgradableHold(mState)) return false;
           mNewState = setUpgradableHold(mState, true);
           break;
         case WRITE:
           if (hasUpgradableHold(mState) || getReadHolds(mState) != 0) return false;
           mNewState = calcState(true, false, 0);
           break;
         default: throw new AssertionError();
       }
       return myState.compareAndSet(mState, mNewState);
     }
     
     private boolean tryUpgrade() {
       int mState = myState.get();
       if (hasWriteHold(mState)) return false;
       if (getReadHolds(mState) != 0) return false;
       int mNewState = calcState(true, false, 0);
       return myState.compareAndSet(mState, mNewState);
     }
 
     private boolean enqueue(Mode aMode, boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
       Thread mCurrent = Thread.currentThread();
       Node mNode = new Node(aMode, mCurrent);
       myQueue.add(mNode);
       boolean mInterrupted = false;
       while (myQueue.peek() != mNode || !tryLock(aMode)) {
         LockSupport.park(this);
         if (Thread.interrupted()) {
           if (aInterruptible) throw new InterruptedException();
           else mInterrupted = true;
         }
       }
       if (mInterrupted) mCurrent.interrupt();
       myQueue.remove();
       if (aMode == Mode.READ || aMode == Mode.UPGRADABLE) {
         unparkNext(EnumSet.of(Mode.READ, Mode.UPGRADABLE), false);
       }
       return true;
     }
     
     private boolean enqueueForUpgrade(boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
       Thread mCurrent = Thread.currentThread();
       myUpgrading.set(mCurrent);
       boolean mInterrupted = false;
       while (!tryUpgrade()) {
         LockSupport.park(this);
         if (Thread.interrupted()) {
           if (aInterruptible) throw new InterruptedException();
           else mInterrupted = true;
         }
       }
       if (mInterrupted) mCurrent.interrupt();
       return true;
     }
     
     private void unparkNext(Set<Mode> aModes, boolean aUpgrade) {
       if (aUpgrade) {
         Thread mUpgrading = myUpgrading.get();
         if (mUpgrading != null) {
           LockSupport.unpark(mUpgrading);
           return;
         }
       }
       Node mNext = myQueue.peek();
       if (mNext != null && aModes.contains(mNext.myMode)) {
         LockSupport.unpark(mNext.myThread);
       }
     }
     
     private static boolean hasWriteHold(int aState) {
       return (aState & 1) != 0;
     }
     
     private static boolean hasUpgradableHold(int aState) {
       return (aState & 2) != 0;
     }
     
     private static int getReadHolds(int aState) {
       return aState >>> 2;
     }
     
     private static int setUpgradableHold(int aState, boolean aUpgradable) {
       return setBit(aState, 1, aUpgradable);
     }
     
     private static int setBit(int aValue, int aIndex, boolean aOn) {
       return aOn ? aValue | (1 << aIndex) : aValue & ~(1 << aIndex);
     }
     
     private static int setReadHolds(int aState, int aReadHolds) {
       return aReadHolds << 2 | aState & 3;
     }
 
     private static int calcState(boolean aWrite, boolean aUpgradable, int aRead) {
       int mState = aRead << 2;
       if (aUpgradable) mState |= 2;
       if (aWrite) mState |= 1;
       return mState;
     }
     
     @Override
     public String toString() {
       int mState = myState.get();
       String mMessage;
       if (hasWriteHold(mState)) {
         mMessage = "1 write/upgraded thread";
       } else {
         boolean mUpgradableHold = hasUpgradableHold(mState);
         int mReadHolds = getReadHolds(mState);
         if (mReadHolds == 0 && !mUpgradableHold) mMessage = "unlocked";
         else {
           mMessage = "";
           if (mUpgradableHold) mMessage += "1 downgraded thread";
           if (mReadHolds > 0) {
             if (mUpgradableHold) mMessage += ", ";
             mMessage += mReadHolds + " read thread";
             if (mReadHolds > 1) mMessage += "s";
           }
         }
       }
       return "[" + mMessage + "]";
     }
   }
   
   /**
    * Acquires the lock in the given locking mode.
    * 
    * @throws IllegalMonitorStateException
    *         if the thread already holds a read lock and is trying for an
    *         upgradable or write lock.
    */
   public void lock(Mode aMode) {
     try {
       lockInternal(aMode, false, NO_TIMEOUT, TimeUnit.SECONDS);
     } catch (InterruptedException e) {
       throw new AssertionError();
     }
   }
   
   /**
    * Acquires the lock while allowing
    * {@linkplain Thread#interrupt interruption}.
    * 
    * @see UpgradableLock#lock(Mode)
    * @throws InterruptedException
    *         if the thread is interrupted before or while waiting for the lock.
    */
   public void lockInterruptibly(Mode aMode) throws InterruptedException {
     lockInternal(aMode, true, NO_TIMEOUT, TimeUnit.SECONDS);
   }
   
   /**
    * Acquires the lock only if it is currently
    * available and returns {@code true} if it succeeds.
    * 
    * @see UpgradableLock#lock(Mode)
    */
   public boolean tryLock(Mode aMode) {
     try {
       return lockInternal(aMode, false, NO_WAIT, TimeUnit.SECONDS);
     } catch (InterruptedException e) {
       throw new AssertionError();
     }
   }
   
   /**
    * Tries to acquire the lock within the given time limit and returns
    * {@code true} if it succeeds.
    * 
    * @see UpgradableLock#lock(Mode)
    * @throws InterruptedException
    *         if the thread is interrupted before or while waiting for the lock.
    */
   public boolean tryLock(Mode aMode, long aTime, TimeUnit aUnit) throws InterruptedException {
     long mTime = boundTimeout(aTime);
     return lockInternal(aMode, true, mTime, aUnit);
   }
   
   /**
    * Releases the thread's latest hold on the lock.
    * 
    * @throws IllegalMonitorStateException if the thread does not hold the lock.
    */
   public void unlock() {
     ThreadState mOld = myThreadState.get();
     if (mOld.isUnlocked()) {
       throw new IllegalMonitorStateException("Cannot unlock lock that was not held");
     }
     boolean mWasWrite = mOld.canWrite();
     ThreadState mNew = mOld.decrementHolds();
     if (mNew.isUnlocked()) {
       Mode mToRelease = mWasWrite ? Mode.WRITE : mOld.getFirstHold();
       mySync.unlock(mToRelease);
       myThreadState.remove();
       return;
     } else if (mWasWrite && !mNew.canWrite()) {
       mySync.downgrade();
     }
     myThreadState.set(mNew);
   }
   
   /**
    * Upgrades the thread's hold on the lock. This has no effect if the thread
    * holds a write lock or has already upgraded.
    * 
    * @throws IllegalMonitorStateException
    *         if the thread does not already hold the lock in upgradable or write
    *         mode.
    */
   public void upgrade() {
     try {
       upgradeInternal(false, NO_TIMEOUT, TimeUnit.SECONDS);
     } catch (InterruptedException e) {
       throw new AssertionError();
     }
   }
 
   /**
    * Upgrades the thread's hold on the lock while allowing
    * {@linkplain Thread#interrupt interruption}.
    * @see UpgradableLock#upgrade()
    * @throws InterruptedException
    *         if the thread is interrupted before or while waiting to upgrade.
    */
   public void upgradeInterruptibly() throws InterruptedException {
     upgradeInternal(true, NO_TIMEOUT, TimeUnit.SECONDS);
   }
 
   /**
    * Upgrades the thread's hold on the lock only if there are no current readers
    * and returns {@code true} if it succeeds.
    * @see UpgradableLock#upgrade()
    */
   public boolean tryUpgrade() {
     try {
       return upgradeInternal(false, NO_WAIT, TimeUnit.SECONDS);
     } catch (InterruptedException e) {
       throw new AssertionError();
     }
   }
 
   /**
    * Tries to upgrade the thread's hold on the lock for the given amount of time
    * and returns {@code true} if it succeeds.
    * 
    * @see UpgradableLock#upgrade()
    * @throws InterruptedException
    *         if the thread is interrupted before or while waiting to upgrade.
    */
   public boolean tryUpgrade(long aTime, TimeUnit aUnit) throws InterruptedException {
     long mTime = boundTimeout(aTime);
     return upgradeInternal(true, mTime, aUnit);
   }
   
   /**
    * Downgrades the thread's hold on the lock. This allows other reader threads
    * to acquire the lock, if the thread has no unmatched upgrades and does not
    * hold a write lock.
    * 
    * @throws IllegalMonitorStateException
    *         if the thread has not upgraded.
    */
   public void downgrade() {
     ThreadState mOld = myThreadState.get();
     if (mOld.isUnlocked()) {
       throw new IllegalMonitorStateException("Cannot downgrade without lock");
     }
     if (mOld.acquiredReadFirst()) {
       throw new IllegalMonitorStateException("Cannot upgrade or downgrade from read");
     }
     if (mOld.isDowngraded()) {
       throw new IllegalMonitorStateException("Cannot downgrade without upgrade");
     }
     ThreadState mNew = mOld.downgrade();
     if (!mNew.canWrite()) {
       mySync.downgrade();
     }
     myThreadState.set(mNew);
   }
   
   private boolean lockInternal(Mode aMode, boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
     switch (aMode) {
       case READ: return readLockInternal(aInterruptible, aTime, aUnit);
       case UPGRADABLE: return upgradableLockInternal(aInterruptible, aTime, aUnit);
       case WRITE: return writeLockInternal(aInterruptible, aTime, aUnit);
       default: throw new AssertionError();
     }
   }
   
   private boolean readLockInternal(boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
     ThreadState mOld = myThreadState.get();
     ThreadState mNew = mOld.incrementRead();
     if (mOld.isUnlocked()) {
       if (!mySync.lock(Mode.READ, aInterruptible, aTime, aUnit)) return false;
     }
     myThreadState.set(mNew);
     return true;
   }
   
   private boolean writeLockInternal(boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
     ThreadState mOld = myThreadState.get();
     if (mOld.acquiredReadFirst()) {
       throw new IllegalMonitorStateException("Cannot upgrade from read");
     }
     ThreadState mNew = mOld.incrementWrite();
     if (mOld.isUnlocked()) {
       if (!mySync.lock(Mode.WRITE, aInterruptible, aTime, aUnit)) return false;
     } else if (!mOld.canWrite()) {
       if (!mySync.upgrade(aInterruptible, aTime, aUnit)) return false;
     }
     myThreadState.set(mNew);
     return true;
   }
   
   private boolean upgradableLockInternal(boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
     ThreadState mOld = myThreadState.get();
     if (mOld.acquiredReadFirst()) {
       throw new IllegalMonitorStateException("Cannot upgrade from read");
     }
     ThreadState mNew = mOld.incrementUpgradable();
     if (mOld.isUnlocked()) {
       if (!mySync.lock(Mode.UPGRADABLE, aInterruptible, aTime, aUnit)) return false;
     }
     myThreadState.set(mNew);
     return true;
   }
   
   private boolean upgradeInternal(boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
     ThreadState mOld = myThreadState.get();
     if (mOld.isUnlocked()) {
       throw new IllegalMonitorStateException("Cannot upgrade without lock");
     }
     if (mOld.acquiredReadFirst()) {
       throw new IllegalMonitorStateException("Cannot upgrade from read");
     }
     ThreadState mNew = mOld.upgrade();
     if (!mOld.canWrite()) {
       if (!mySync.upgrade(aInterruptible, aTime, aUnit)) return false;
     }
     myThreadState.set(mNew);
     return true;
   }
   
   private static long boundTimeout(long aTime) {
     return aTime < 0 ? MIN_TIMEOUT : aTime;
   }
   
   @Override
   public String toString() {
     return "UpgradableLock" + mySync.toString();
   }
   
   private Object writeReplace() {
     return new SerializationProxy();
   }
   
   private void readObject(ObjectInputStream aOis) throws InvalidObjectException {
     throw new InvalidObjectException("Expecting serialization proxy");
   }
   
   /*
    * A new upgradable lock must be created after deserialization to allow all
    * fields to be final while avoiding serializing the thread local state. A
    * serialization proxy makes this easier.
    */
   private static final class SerializationProxy implements Serializable {
     private static final long serialVersionUID = 0L;
     
     private Object readResolve() {
       return new UpgradableLock();
     }
   }
 }
