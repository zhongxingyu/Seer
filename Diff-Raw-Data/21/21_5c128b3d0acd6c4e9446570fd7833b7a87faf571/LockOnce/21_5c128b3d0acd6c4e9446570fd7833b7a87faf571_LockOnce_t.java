 package ch.claude_martin.lockonce;
 
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 /** The <code>LockOnce</code> class represents an object used to guard some block of code that is only to be accessed once.
  * 
  * <p>
  * This class implements the <a href="http://en.wikipedia.org/wiki/Double-checked_locking">double-checked-locking</a> idiom and allows to
  * use it just like similar classes, such as the {@link ReentrantLock}.
  * 
  * <p>
  * This could be used for lazy singleton initialization. However, this is considered to be an antipattern by some. They argue that it is
  * better to use a nested static class as a <a href="http://en.wikipedia.org/wiki/Initialization_on_demand_holder_idiom"><i>initialization
  * on demand holder</i></a>. The argument that it is broken is only true for JVM &lt;= 1.4, but not for newer versions.
  * 
  * <p>
  * The given solution in this class is safe on JVM 1.5+ thanks to the Java Memory Model. But fields of a created singleton are still not
  * safe unless final or volatile.
  * 
  * <p>
  * It is recommended practice to <em>always</em> immediately follow a call to {@code lockOnce} with a {@code try} block, most typically in a
  * before/after construction such as:
  * 
  * <pre>
  * <tt>
  * final LockOnce lo = new LockOnce();
  * if (lo.lockOnce()) try {
  *   // ... method body
  * } finally {
  *   lo.unlock();
  * }</tt>
  * </pre>
  * 
  * @author Claude Martin */
 public class LockOnce {
 	/** Object was only created, no lock requests so far. */
 	private static final byte		STATE_UNTAPPED	= 0;
 	/** Currently locked. Some Thread is doing some critical work. It is supposed to call {@link #unlock()}. */
 	private static final byte		STATE_LOCKED		= 1;
 	/** This LockOnce is spent, all further calls to {@link #lockOnce()} will return <code>false</code> quickly. */
 	private static final byte		STATE_SPENT			= 2;
 	/** Current state. */
 	private volatile byte				state						= STATE_UNTAPPED;
 	/** Used ReentrantLock-Object. */
 	private final ReentrantLock	lock						= new ReentrantLock();
 	/** Condition used to wait for a call to {@link #unlock()}. */
 	private final Condition			unlocked				= this.lock.newCondition();
 	private Thread							thread					= null;
 
 	/** Returns <code>true</code> on only one invocation. All others get <code>false</code>.
 	 * 
 	 * Note: It is not necessarily the first invocation that get's <code>true</code>. But other threads will block untill {@link #unlock()} is
 	 * called the first time. A returned <code>false</code> is guaranteed to be returned after the receiver of <code>true</code> has finished
 	 * and changes all are visible. */
 	public boolean lockOnce() {
 		try {
 			return this.lockOnce(false);
 		} catch (final InterruptedException e) {
 			// This never happens!
 			return false;
 		}
 	}
 
 	/** Returns <code>true</code> on only one invocation unless the current thread is {@linkplain Thread#interrupt interrupted} .
 	 * 
 	 * @see #lockOnce() */
 	public boolean lockOnceInterruptibly() throws InterruptedException {
 		return this.lockOnce(true);
 	}
 
 	private boolean lockOnce(final boolean interruptably) throws InterruptedException {
 		if (this.state == STATE_SPENT) return false;
 		if (interruptably) this.lock.lockInterruptibly();
 		else this.lock.lock();
 		try {
			while (this.state == STATE_LOCKED) {
				if (this.thread == Thread.currentThread())
					throw new IllegalMonitorStateException("Multiple calls by the same thread are not permitted.");
 				if (interruptably) this.unlocked.await();
 				else this.unlocked.awaitUninterruptibly();
 			}
 			if (this.state == STATE_SPENT) return false;
 			if (this.state == STATE_UNTAPPED)
 				this.state = STATE_LOCKED;
 			this.thread = Thread.currentThread();
 			return true;
 		} finally {
 			this.lock.unlock();
 		}
 	}
 
 	/** Unlock the LockOnce mechanism. This should be called from a finally-block.
 	 * 
 	 * <p>
 	 * Invocation from the wrong thread or before any call to {@link #lockOnce()} results in a {@link IllegalMonitorStateException}. */
 	public void unlock() {
 		if (this.state == STATE_UNTAPPED)
 			throw new IllegalMonitorStateException("unlock() can not be called if lockOnce() was never called before.");
 		if (this.thread != Thread.currentThread())
 			throw new IllegalMonitorStateException("This thread does not hold the lock.");
 		if (this.state == STATE_SPENT) return;
 		this.lock.lock();
 		try {
 			if (this.state == STATE_SPENT) return;
 			this.state = STATE_SPENT;
 			this.unlocked.signalAll();
 		} finally {
 			this.lock.unlock();
 		}
 	}
 
 	@Override
 	public String toString() {
 		String strState;
 		if (this.state == STATE_LOCKED) {
 			strState = "locked";
 		} else if (this.state == STATE_SPENT) {
 			strState = "spent";
 		} else if (this.state == STATE_UNTAPPED) {
 			strState = "untapped";
 		} else {
 			strState = "faulty";
 		}
 		return "LockOnce[" + strState + "]";
 	}
 }
