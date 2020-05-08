 /*
  * Copyright 2010, 2011 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  *
  * AHSlib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at the original copyright holder's option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 /*
  * This code is inspired by and borrows heavily from code originally written
  * by Doug Lea with assistance from members of JCP JSR-166 Expert Group and
  * released to the public domain.  The author of this code greatfully 
  * acknowledges their contributions to the field.
  */
 
 package us.exultant.ahs.thread;
 
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.concurrent.locks.*;
 
 public class FlippableSemaphore {
 	public void flip(boolean $flip) {
 		$sync.flip($flip);
 	}	// ... should i support a cas'able variation to this?  otherwise it could be a bit tough to use outside of an idempotent situation
 	
 	public final boolean isFlipped() {
 		return $sync.isFlipped();
 	}
 	
 	
 	
 	private final Sync	$sync;
 	
 	/**
 	 * 
 	 * @param $status
 	 *                the number "currently" held by CAS
 	 * @param $delta
 	 *                number of permits to release or acquire (positive and negative,
 	 *                respectively)
 	 * @return the number to push into CAS, or {@link Integer#MAX_VALUE} if the
 	 *         transition would be illegal (i.e. trying to acquire more permits than
 	 *         are available).
 	 * @throws Error
 	 *                 if the numbers involved were so big we hit an integer overflow
 	 *                 situation
 	 */
 	private static final int shift(int $status, int $delta) {
 		final int $real = real($status);
 		final int $next = $real + $delta;
 		if ($next < 0)
 			if ($delta < 0)
 				return Integer.MAX_VALUE;		// if acquiring (delta is negative), reject if insufficient permits available (aka real + delta < 0)
 			else
 				throw new Error("integer overflow");	// if releasing (delta is positive) and we somehow got a negative by increasing a positive?  scream.
 		// if $next == Integer.MAX_VALUE here that could arguably be considered an overflow in context since returning it unmolested is supposed to signal a completely different situation... but eh. 
		return ($status >= 0) ? $real : ($next == 0) ? Integer.MIN_VALUE : -$real;
 	}
 	
 	private static final int real(int $status) {
 		return ($status == Integer.MIN_VALUE) ? 0 : Math.abs($status);
 	}
 	
 	
 	/**
 	 * State herein is mostly how you would expect semaphore permits to be described,
 	 * except:
 	 * <ul>
 	 * <li>All negative numbers are the equivalent in permit count to their absolute
 	 * value, but also signify that we're in a "flipped" state. (Zero is not a
 	 * negative number.)
 	 * <li>{@link Integer#MIN_VALUE} means we are flipped, and there are no permits
 	 * available.
 	 * </ul>
 	 * 
 	 * @author hash
 	 * 
 	 */
 	abstract static class Sync extends AbstractQueuedSynchronizer {
 		//Sync() { setState(0); }
 		
 		final int getPermitsRaw() {
 			return getState();
 		}
 		
 		final int getPermits() {
 			return real(getState());
 		}
 		
 		final boolean isFlipped() {
 			return (getState() < 0);
 		}
 		
 		final void flip(boolean $flip) {
 			if ($flip) {
 				for (;;) {
 					final int state = getState();
 					if (state < 0 || compareAndSetState(state, (state == 0) ? Integer.MIN_VALUE : -state)) return;
 				}
 			} else {
 				for (;;) {
 					final int state = getState();
 					if (state >= 0 || compareAndSetState(state, (state == Integer.MIN_VALUE) ? 0 : -state)) return;
 				}
 			}
 		}
 		
 		/**
 		 * @return -1 for impossible, 0 for success and no more permits, &gt;1 for success and permits still available.
 		 */
 		final int nonfairTryAcquireShared(int $acquires) {
 			for (;;) {
 				int $status = getState();
 				int $next = shift($status, -$acquires);
 				if ($next == Integer.MAX_VALUE) return -1;	// not enough permits to acquire that many
 				if (compareAndSetState($status, $next)) return ($next == Integer.MIN_VALUE) ? 0 : Math.abs($next);
 			}
 		}
 		
 		/**
 		 * @return true.  the only way this function can fail is if there's an integer overflow issue, and then there's shit thrown.
 		 */
 		protected final boolean tryReleaseShared(int $releases) {
 			for (;;) {
 				int $status = getState();
 				int $next = shift($status, $releases);
 				if (compareAndSetState($status, $next)) return true;
 			}
 		}
 		
 		final void reducePermits(int $reductions) {
 			for (;;) {
 				int $status = getState();
 				int $next = shift($status, Math.min($reductions,$status));
 				if (compareAndSetState($status, $next)) return;
 			}
 		}
 		
 		final int drainPermits() {
 			for (;;) {
 				int current = getState();
 				if (current == 0 || current == Integer.MIN_VALUE) return 0;
 				if (compareAndSetState(current, (current < 0) ? Integer.MIN_VALUE : 0)) return Math.abs(current);
 			}
 		}
 	}
 	static final class NonfairSync extends Sync {
 		protected int tryAcquireShared(int acquires) {
 			return nonfairTryAcquireShared(acquires);
 		}
 	}
 	static final class FairSync extends Sync {
 		protected int tryAcquireShared(int $acquires) {
 			for (;;) {
 				//if (hasQueuedPredecessors()) return -1;		// ... this is 1.7 only?!  rage.  the below is functionally equivalent, but is likely to be slower.
 				if (hasQueuedThreads() && getFirstQueuedThread() != Thread.currentThread()) return -1;
 				int $status = getState();
 				int $next = shift($status, -$acquires);
 				if ($next == Integer.MAX_VALUE) return -1;	// not enough permits to acquire that many
 				if (compareAndSetState($status, $next)) return ($next == Integer.MIN_VALUE) ? 0 : Math.abs($next);
 			}
 		}
 	}
 	
 	
 
 	/**
 	 * Creates a {@code ClosableSemaphore} with zero permits and nonfair fairness
 	 * setting.
 	 */
 	public FlippableSemaphore() {
 		this(false);
 	}
 	
 	/**
 	 * Creates a {@code ClosableSemaphore} with zero permits and the given fairness
 	 * setting.
 	 * 
 	 * @param $fair
 	 *                {@code true} if this semaphore will guarantee first-in first-out
 	 *                granting of permits under contention, else {@code false}
 	 */
 	public FlippableSemaphore(boolean $fair) {
 		$sync = $fair ? new FairSync() : new NonfairSync();
 	}
 	
 	/**
 	 * Acquires a permit from this semaphore, blocking until one is available, or the
 	 * thread is {@linkplain Thread#interrupt interrupted}.
 	 * 
 	 * <p>
 	 * Acquires a permit, if one is available and returns immediately, reducing the
 	 * number of available permits by one.
 	 * 
 	 * <p>
 	 * If no permit is available then the current thread becomes disabled for thread
 	 * scheduling purposes and lies dormant until one of two things happens:
 	 * <ul>
 	 * <li>Some other thread invokes the {@link #release} method for this semaphore
 	 * and the current thread is next to be assigned a permit; or
 	 * <li>Some other thread {@linkplain Thread#interrupt interrupts} the current
 	 * thread.
 	 * </ul>
 	 * 
 	 * <p>
 	 * If the current thread:
 	 * <ul>
 	 * <li>has its interrupted status set on entry to this method; or
 	 * <li>is {@linkplain Thread#interrupt interrupted} while waiting for a permit,
 	 * </ul>
 	 * then {@link InterruptedException} is thrown and the current thread's
 	 * interrupted status is cleared.
 	 * 
 	 * @throws InterruptedException
 	 *                 if the current thread is interrupted
 	 */
 	public void acquire() throws InterruptedException {
 		$sync.acquireSharedInterruptibly(1);
 	}
 	
 	/**
 	 * Acquires a permit from this semaphore, blocking until one is available.
 	 * 
 	 * <p>
 	 * Acquires a permit, if one is available and returns immediately, reducing the
 	 * number of available permits by one.
 	 * 
 	 * <p>
 	 * If no permit is available then the current thread becomes disabled for thread
 	 * scheduling purposes and lies dormant until some other thread invokes the
 	 * {@link #release} method for this semaphore and the current thread is next to be
 	 * assigned a permit.
 	 * 
 	 * <p>
 	 * If the current thread is {@linkplain Thread#interrupt interrupted} while
 	 * waiting for a permit then it will continue to wait, but the time at which the
 	 * thread is assigned a permit may change compared to the time it would have
 	 * received the permit had no interruption occurred. When the thread does return
 	 * from this method its interrupt status will be set.
 	 */
 	public void acquireUninterruptibly() {
 		$sync.acquireShared(1);
 	}
 	
 	/**
 	 * Acquires a permit from this semaphore, only if one is available at the time of
 	 * invocation.
 	 * 
 	 * <p>
 	 * Acquires a permit, if one is available and returns immediately, with the value
 	 * {@code true}, reducing the number of available permits by one.
 	 * 
 	 * <p>
 	 * If no permit is available then this method will return immediately with the
 	 * value {@code false}.
 	 * 
 	 * <p>
 	 * Even when this semaphore has been set to use a fair ordering policy, a call to
 	 * {@code tryAcquire()} <em>will</em> immediately acquire a permit if one is
 	 * available, whether or not other threads are currently waiting. This
 	 * &quot;barging&quot; behavior can be useful in certain circumstances, even
 	 * though it breaks fairness. If you want to honor the fairness setting, then use
 	 * {@link #tryAcquire(long, TimeUnit) tryAcquire(0, TimeUnit.SECONDS) } which is
 	 * almost equivalent (it also detects interruption).
 	 * 
 	 * @return {@code true} if a permit was acquired and {@code false} otherwise
 	 */
 	public boolean tryAcquire() {
 		return $sync.nonfairTryAcquireShared(1) >= 0;
 	}
 	
 	/**
 	 * Acquires a permit from this semaphore, if one becomes available within the
 	 * given waiting time and the current thread has not been
 	 * {@linkplain Thread#interrupt interrupted}.
 	 * 
 	 * <p>
 	 * Acquires a permit, if one is available and returns immediately, with the value
 	 * {@code true}, reducing the number of available permits by one.
 	 * 
 	 * <p>
 	 * If no permit is available then the current thread becomes disabled for thread
 	 * scheduling purposes and lies dormant until one of three things happens:
 	 * <ul>
 	 * <li>Some other thread invokes the {@link #release} method for this semaphore
 	 * and the current thread is next to be assigned a permit; or
 	 * <li>Some other thread {@linkplain Thread#interrupt interrupts} the current
 	 * thread; or
 	 * <li>The specified waiting time elapses.
 	 * </ul>
 	 * 
 	 * <p>
 	 * If a permit is acquired then the value {@code true} is returned.
 	 * 
 	 * <p>
 	 * If the current thread:
 	 * <ul>
 	 * <li>has its interrupted status set on entry to this method; or
 	 * <li>is {@linkplain Thread#interrupt interrupted} while waiting to acquire a
 	 * permit,
 	 * </ul>
 	 * then {@link InterruptedException} is thrown and the current thread's
 	 * interrupted status is cleared.
 	 * 
 	 * <p>
 	 * If the specified waiting time elapses then the value {@code false} is returned.
 	 * If the time is less than or equal to zero, the method will not wait at all.
 	 * 
 	 * @param $timeout
 	 *                the maximum time to wait for a permit
 	 * @param $unit
 	 *                the time unit of the {@code timeout} argument
 	 * @return {@code true} if a permit was acquired and {@code false} if the waiting
 	 *         time elapsed before a permit was acquired
 	 * @throws InterruptedException
 	 *                 if the current thread is interrupted
 	 */
 	public boolean tryAcquire(long $timeout, TimeUnit $unit) throws InterruptedException {
 		return $sync.tryAcquireSharedNanos(1, $unit.toNanos($timeout));
 	}
 	
 	/**
 	 * Releases a permit, returning it to the semaphore.
 	 * 
 	 * <p>
 	 * Releases a permit, increasing the number of available permits by one. If any
 	 * threads are trying to acquire a permit, then one is selected and given the
 	 * permit that was just released. That thread is (re)enabled for thread scheduling
 	 * purposes.
 	 * 
 	 * <p>
 	 * There is no requirement that a thread that releases a permit must have acquired
 	 * that permit by calling {@link #acquire}. Correct usage of a semaphore is
 	 * established by programming convention in the application.
 	 */
 	public void release() {
 		$sync.releaseShared(1);
 	}
 	
 	/**
 	 * Acquires the given number of permits from this semaphore, blocking until all
 	 * are available, or the thread is {@linkplain Thread#interrupt interrupted}.
 	 * 
 	 * <p>
 	 * Acquires the given number of permits, if they are available, and returns
 	 * immediately, reducing the number of available permits by the given amount.
 	 * 
 	 * <p>
 	 * If insufficient permits are available then the current thread becomes disabled
 	 * for thread scheduling purposes and lies dormant until one of two things
 	 * happens:
 	 * <ul>
 	 * <li>Some other thread invokes one of the {@link #release() release} methods for
 	 * this semaphore, the current thread is next to be assigned permits and the
 	 * number of available permits satisfies this request; or
 	 * <li>Some other thread {@linkplain Thread#interrupt interrupts} the current
 	 * thread.
 	 * </ul>
 	 * 
 	 * <p>
 	 * If the current thread:
 	 * <ul>
 	 * <li>has its interrupted status set on entry to this method; or
 	 * <li>is {@linkplain Thread#interrupt interrupted} while waiting for a permit,
 	 * </ul>
 	 * then {@link InterruptedException} is thrown and the current thread's
 	 * interrupted status is cleared. Any permits that were to be assigned to this
 	 * thread are instead assigned to other threads trying to acquire permits, as if
 	 * permits had been made available by a call to {@link #release()}.
 	 * 
 	 * @param $permits
 	 *                the number of permits to acquire
 	 * @throws InterruptedException
 	 *                 if the current thread is interrupted
 	 * @throws IllegalArgumentException
 	 *                 if {@code permits} is negative
 	 */
 	public void acquire(int $permits) throws InterruptedException {
 		if ($permits < 0) throw new IllegalArgumentException();
 		$sync.acquireSharedInterruptibly($permits);
 	}
 	
 	/**
 	 * Acquires the given number of permits from this semaphore, blocking until all
 	 * are available.
 	 * 
 	 * <p>
 	 * Acquires the given number of permits, if they are available, and returns
 	 * immediately, reducing the number of available permits by the given amount.
 	 * 
 	 * <p>
 	 * If insufficient permits are available then the current thread becomes disabled
 	 * for thread scheduling purposes and lies dormant until some other thread invokes
 	 * one of the {@link #release() release} methods for this semaphore, the current
 	 * thread is next to be assigned permits and the number of available permits
 	 * satisfies this request.
 	 * 
 	 * <p>
 	 * If the current thread is {@linkplain Thread#interrupt interrupted} while
 	 * waiting for permits then it will continue to wait and its position in the queue
 	 * is not affected. When the thread does return from this method its interrupt
 	 * status will be set.
 	 * 
 	 * @param $permits
 	 *                the number of permits to acquire
 	 * @throws IllegalArgumentException
 	 *                 if {@code permits} is negative
 	 * 
 	 */
 	public void acquireUninterruptibly(int $permits) {
 		if ($permits < 0) throw new IllegalArgumentException();
 		$sync.acquireShared($permits);
 	}
 	
 	/**
 	 * Acquires the given number of permits from this semaphore, only if all are
 	 * available at the time of invocation.
 	 * 
 	 * <p>
 	 * Acquires the given number of permits, if they are available, and returns
 	 * immediately, with the value {@code true}, reducing the number of available
 	 * permits by the given amount.
 	 * 
 	 * <p>
 	 * If insufficient permits are available then this method will return immediately
 	 * with the value {@code false} and the number of available permits is unchanged.
 	 * 
 	 * <p>
 	 * Even when this semaphore has been set to use a fair ordering policy, a call to
 	 * {@code tryAcquire} <em>will</em> immediately acquire a permit if one is
 	 * available, whether or not other threads are currently waiting. This
 	 * &quot;barging&quot; behavior can be useful in certain circumstances, even
 	 * though it breaks fairness. If you want to honor the fairness setting, then use
 	 * {@link #tryAcquire(int, long, TimeUnit) tryAcquire(permits, 0,
 	 * TimeUnit.SECONDS) } which is almost equivalent (it also detects interruption).
 	 * 
 	 * @param $permits
 	 *                the number of permits to acquire
 	 * @return {@code true} if the permits were acquired and {@code false} otherwise
 	 * @throws IllegalArgumentException
 	 *                 if {@code permits} is negative
 	 */
 	public boolean tryAcquire(int $permits) {
 		if ($permits < 0) throw new IllegalArgumentException();
 		return $sync.nonfairTryAcquireShared($permits) >= 0;
 	}
 	
 	/**
 	 * Acquires the given number of permits from this semaphore, if all become
 	 * available within the given waiting time and the current thread has not been
 	 * {@linkplain Thread#interrupt interrupted}.
 	 * 
 	 * <p>
 	 * Acquires the given number of permits, if they are available and returns
 	 * immediately, with the value {@code true}, reducing the number of available
 	 * permits by the given amount.
 	 * 
 	 * <p>
 	 * If insufficient permits are available then the current thread becomes disabled
 	 * for thread scheduling purposes and lies dormant until one of three things
 	 * happens:
 	 * <ul>
 	 * <li>Some other thread invokes one of the {@link #release() release} methods for
 	 * this semaphore, the current thread is next to be assigned permits and the
 	 * number of available permits satisfies this request; or
 	 * <li>Some other thread {@linkplain Thread#interrupt interrupts} the current
 	 * thread; or
 	 * <li>The specified waiting time elapses.
 	 * </ul>
 	 * 
 	 * <p>
 	 * If the permits are acquired then the value {@code true} is returned.
 	 * 
 	 * <p>
 	 * If the current thread:
 	 * <ul>
 	 * <li>has its interrupted status set on entry to this method; or
 	 * <li>is {@linkplain Thread#interrupt interrupted} while waiting to acquire the
 	 * permits,
 	 * </ul>
 	 * then {@link InterruptedException} is thrown and the current thread's
 	 * interrupted status is cleared. Any permits that were to be assigned to this
 	 * thread, are instead assigned to other threads trying to acquire permits, as if
 	 * the permits had been made available by a call to {@link #release()}.
 	 * 
 	 * <p>
 	 * If the specified waiting time elapses then the value {@code false} is returned.
 	 * If the time is less than or equal to zero, the method will not wait at all. Any
 	 * permits that were to be assigned to this thread, are instead assigned to other
 	 * threads trying to acquire permits, as if the permits had been made available by
 	 * a call to {@link #release()}.
 	 * 
 	 * @param $permits
 	 *                the number of permits to acquire
 	 * @param $timeout
 	 *                the maximum time to wait for the permits
 	 * @param $unit
 	 *                the time unit of the {@code timeout} argument
 	 * @return {@code true} if all permits were acquired and {@code false} if the
 	 *         waiting time elapsed before all permits were acquired
 	 * @throws InterruptedException
 	 *                 if the current thread is interrupted
 	 * @throws IllegalArgumentException
 	 *                 if {@code permits} is negative
 	 */
 	public boolean tryAcquire(int $permits, long $timeout, TimeUnit $unit) throws InterruptedException {
 		if ($permits < 0) throw new IllegalArgumentException();
 		return $sync.tryAcquireSharedNanos($permits, $unit.toNanos($timeout));
 	}
 	
 	/**
 	 * Releases the given number of permits, returning them to the semaphore.
 	 * 
 	 * <p>
 	 * Releases the given number of permits, increasing the number of available
 	 * permits by that amount. If any threads are trying to acquire permits, then one
 	 * is selected and given the permits that were just released. If the number of
 	 * available permits satisfies that thread's request then that thread is
 	 * (re)enabled for thread scheduling purposes; otherwise the thread will wait
 	 * until sufficient permits are available. If there are still permits available
 	 * after this thread's request has been satisfied, then those permits are assigned
 	 * in turn to other threads trying to acquire permits.
 	 * 
 	 * <p>
 	 * There is no requirement that a thread that releases a permit must have acquired
 	 * that permit by calling {@link #acquire acquire}. Correct usage of a
 	 * semaphore is established by programming convention in the application.
 	 * 
 	 * @param $permits
 	 *                the number of permits to release
 	 * @throws IllegalArgumentException
 	 *                 if {@code permits} is negative
 	 */
 	public void release(int $permits) {
 		if ($permits < 0) throw new IllegalArgumentException();
 		$sync.releaseShared($permits);
 	}
 	
 	/**
 	 * Returns the current number of permits available in this semaphore.
 	 * 
 	 * <p>
 	 * This method is typically used for debugging and testing purposes.
 	 * 
 	 * @return the number of permits available in this semaphore
 	 */
 	public int availablePermits() {
 		return $sync.getPermits();
 	}
 	
 	/**
 	 * Acquires and returns all permits that are immediately available.
 	 * 
 	 * @return the number of permits acquired
 	 */
 	public int drainPermits() {
 		return $sync.drainPermits();
 	}
 	
 	/**
 	 * Shrinks the number of available permits by the indicated reduction. This method
 	 * can be useful in subclasses that use semaphores to track resources that become
 	 * unavailable. This method differs from {@code acquire} in that it does not block
 	 * waiting for permits to become available.
 	 * 
 	 * <p>
 	 * If there are fewer permits available than reduction requested, the number of
 	 * available permits will become zero.
 	 * 
 	 * @param $reduction
 	 *                the number of permits to remove
 	 * @throws IllegalArgumentException
 	 *                 if {@code reduction} is negative
 	 */
 	protected void reducePermits(int $reduction) {
 		if ($reduction < 0) throw new IllegalArgumentException();
 		$sync.reducePermits($reduction);
 	}
 	
 	/**
 	 * Returns {@code true} if this semaphore has fairness set true.
 	 * 
 	 * @return {@code true} if this semaphore has fairness set true
 	 */
 	public boolean isFair() {
 		return $sync instanceof FairSync;
 	}
 	
 	/**
 	 * Queries whether any threads are waiting to acquire. Note that because
 	 * cancellations may occur at any time, a {@code true} return does not guarantee
 	 * that any other thread will ever acquire. This method is designed primarily for
 	 * use in monitoring of the system state.
 	 * 
 	 * @return {@code true} if there may be other threads waiting to acquire the lock
 	 */
 	public final boolean hasQueuedThreads() {
 		return $sync.hasQueuedThreads();
 	}
 	
 	/**
 	 * Returns an estimate of the number of threads waiting to acquire. The value is
 	 * only an estimate because the number of threads may change dynamically while
 	 * this method traverses internal data structures. This method is designed for use
 	 * in monitoring of the system state, not for synchronization control.
 	 * 
 	 * @return the estimated number of threads waiting for this lock
 	 */
 	public final int getQueueLength() {
 		return $sync.getQueueLength();
 	}
 	
 	/**
 	 * Returns a collection containing threads that may be waiting to acquire. Because
 	 * the actual set of threads may change dynamically while constructing this
 	 * result, the returned collection is only a best-effort estimate. The elements of
 	 * the returned collection are in no particular order. This method is designed to
 	 * facilitate construction of subclasses that provide more extensive monitoring
 	 * facilities.
 	 * 
 	 * @return the collection of threads
 	 */
 	protected Collection<Thread> getQueuedThreads() {
 		return $sync.getQueuedThreads();
 	}
 	
 	/**
 	 * Returns a string identifying this semaphore, as well as its state. The state,
 	 * in brackets, includes the String {@code "Permits ="} followed by the number of
 	 * permits.
 	 * 
 	 * @return a string identifying this semaphore, as well as its state
 	 */
 	public String toString() {
 		return super.toString() + "[Permits=" + $sync.getPermits() + ";Flipped="+$sync.isFlipped()+"]";
 	}
 	
 	/**
 	 * This is the exact same as asking isFlipped() && !availablePermits() (assuming that when you flip, it's idempotent/permanent), but ever so slightly more efficient. 
 	 */
 	boolean isFlippedAndZero() {
 		return ($sync.getPermitsRaw() == Integer.MIN_VALUE); 
 	}
 }
