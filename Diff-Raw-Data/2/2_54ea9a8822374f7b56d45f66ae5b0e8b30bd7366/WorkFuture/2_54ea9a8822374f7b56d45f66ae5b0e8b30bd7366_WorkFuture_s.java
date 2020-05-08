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
 
 package us.exultant.ahs.thread;
 
 import us.exultant.ahs.core.*;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.concurrent.locks.*;
 
 /**
  * <p>
  * Produced internally by some WorkScheduler implementations for bookkeeping and return to
  * the function that scheduled a task.
  * </p>
  * 
  * <p>
  * Note that there is (currently) no hardcoded rule that a WorkTarget instance may only be
  * submitted once to a single WorkScheduler and thus have exactly one paired WorkFuture
  * object... but it's the only case the system is designed for, so sane results are not
  * guaranteed if one does otherwise. (This is also stated in the documentation of
  * WorkScheduler that talks about the relationship between WorkScheduler, WorkTarget, and
  * WorkFuture.)
  * </p>
  * 
  * @author hash
  * 
  * @param <$V>
  */
class WorkFuture<$V> implements Future<$V> {
 	public WorkFuture(WorkTarget<$V> $wt, ScheduleParams $schedp) {
 		this.$work = $wt;
 		this.$schedp = $schedp;
 		this.$sync = new Sync();
 	}
 	
 	
 	final Sync					$sync;
 	
 	/** The underlying callable */
         final WorkTarget<$V>				$work;
 	
 	/** The parameters with which the work target was scheduled. */
 	private final ScheduleParams			$schedp;
 	
 	/** Set to true when someone calls the cancel method.  Never again becomes false.  If there's currently a thread from the scheduler working on this, it must eventually notice this and deal with it; if there is no thread running this, the cancelling thread may act immediately. */
 	volatile boolean				$cancelPlz	= false;
 	/** The result to return from get(). Need not be volatile or synchronized since the value is only important when it is idempotent, which is once $state has made its own final idempotent transition. */
 	private $V					$result		= null;
 	/** The (already wrapped) exception to throw from get(). Need not be volatile or synchronized since the value is only important when it is idempotent, which is once $state has made its own final idempotent transition. */
 	private ExecutionException			$exception	= null;
 	
 	/** Index into delay queue, to support faster updates. */
 	int						$heapIndex	= 0;
 	
 	/** When nulled after set/cancel, this indicates that the results are accessible. */
 	volatile Thread					$runner;
 	
 	private volatile Listener<WorkFuture<$V>>	$completionListener;	//FIXME:AHS:THREAD: um, we need to be able to set this before the task starts or it's not very useful.  Oh.  Or we set it, then check for concurrent completion, then signal it.  And forbid setting more than once?
 	
 	
 	WorkTarget<$V> getWorkTarget() {	// this can't be public because giving out a WorkFuture to untrusted code isn't supposed to give that code the ability to call call() on the WT.
 		return $work;
 	}
 	
 	public State getState() {
 		return $sync.getWFState();
 	}
 	
 	public ScheduleParams getScheduleParams() {
 		return $schedp;
 	}
 	
 	public boolean isCancelled() {
 		return getState() == State.CANCELLED;
 	}
 	
 	public boolean isDone() {
 		switch (getState()) {
 			case FINISHED: return true;
 			case CANCELLED: return true;
 			default: return false;
 		}
 	}
 	
 	public $V get() throws InterruptedException, ExecutionException, CancellationException {
 		$sync.waitForDone();
 		return $sync.get(); 
 	}
 	
 	public $V get(long $timeout, TimeUnit $unit) throws InterruptedException, ExecutionException, TimeoutException, CancellationException {
 		$sync.waitForDone($unit.toNanos($timeout));
 		return $sync.get(); 
 	}
 	
 	public boolean cancel(boolean $mayInterruptIfRunning) {
 		return $sync.cancel($mayInterruptIfRunning);
 	}
 	
 	public String toString() {
 		return super.toString()+"[work:"+$work+"]";
 	}
 	
 	
 	
 	public static enum State {
 		/**
 		 * the work has not identified itself as having things to do immediately,
 		 * so it will not be scheduled.
 		 * 
 		 * Note! This is <b>independent</b> of whether or not
 		 * {@link WorkTarget#isReady()} returns true at any given time! The
 		 * contract of the {@link WorkTarget#isReady()} method allows it to toggle
 		 * at absolutely any time and with no synchronization whatsoever. This
 		 * state enum refers only to what the {@link WorkScheduler} has most
 		 * recently noticed (typically during invocation of the
 		 * {@link WorkScheduler#update(WorkFuture)} method).
 		 */
 		WAITING,	// this actually has to be ordinal zero due to the silliness in AQS
 		/**
 		 * The {@link WorkScheduler} has found the WorkTarget of this Future to
 		 * be ready, and has queued it for execution. The WorkFuture will be
 		 * shifted to {@link #RUNNING} when it reaches the top of the
 		 * WorkScheduler's queue of {@link #SCHEDULED} work (unless at that time
 		 * {@link WorkTarget#isReady()} is no longer true, in which case this
 		 * WorkFuture will be shifted back to {@link #WAITING}).
 		 */
 		SCHEDULED,
 		/**
 		 * The {@link WorkScheduler} that produced this WorkFuture has put a
 		 * thread onto the job and it has stack frames in the execution of the
 		 * work.
 		 */
 		RUNNING,
 		///**
 		// * the work was running, but made a blocking call which returned thread
 		// * power to the scheduler. The thread that was running this WorkTarget
 		// * still has stack frames in the work, but the entire thread is paused in
 		// * a blocking call under the management of the WorkScheduler (which has
 		// * launched the activity of another thread to compensate for this thread's
 		// * inactivity, and will wake this thread and return it to RUNNING state as
 		// * soon as the task it is currently blocked on completes and any one of
 		// * the other then-RUNNING threads completes its task). not currently used.
 		// */
 		//PARKED,
 		/**
 		 * The {@link WorkTarget#isDone()} method returned true after the last
 		 * time a thread acting on behalf of this Future's {@link WorkScheduler}
 		 * pushed the WorkTarget; the WorkTarget will no longer be scheduled for
 		 * future activation, and the final result of the execution &mdash;
 		 * whether it be a return value or an exception &mdash; is now available
 		 * for immediate return via the {@link WorkFuture#get()} method. An
 		 * exception thrown from the {@link WorkTarget#call()} method will also
 		 * result in this Future becoming FINISHED, but the
 		 * {@link WorkTarget#isDone()} method may still return false.
 		 */// Actually, when I say "immediately", I mean that relatively.  the sync call in get() might still actually block for a tiny bit -- but we're talking about a handful of machine operations while the work thread finishes setting the return value after admitting completion and before releasing the locks for the last time.
 		FINISHED,
 		/**
 		 * The work was cancelled via the {@link WorkFuture#cancel(boolean)}
 		 * method before it could become {@link #FINISHED}. The work may have
 		 * previously been {@link #RUNNING}, but will now no longer be scheduled
 		 * for future activations. Since the cancellation was the result of an
 		 * external operation rather than of the WorkTarget's own volition, the
 		 * WorkTarget's {@link WorkTarget#isDone()} method may still return false.
 		 */
 		CANCELLED;
 		
 		private final static State[] values = State.values();
 	}
 	
 	
 	
 	/** Uses AQS sync state to represent run status. */
 	final class Sync extends AbstractQueuedSynchronizer {
 		Sync() {}
 		
 		State getWFState() {
 			return State.values[getState()];
 		}
 		
 		/** Implements AQS base acquire to succeed if finished or cancelled */
 		protected int tryAcquireShared(int $ignore) {
 			return isDone() ? 1 : -1;
 		}
 		
 		/** Implements AQS base release to always signal after setting final done status by nulling runner thread. */
 		protected boolean tryReleaseShared(int $ignore) {
 			$runner = null;
 			// we don't call the completion listener here because this can be hit more than once; the thread nulling just works because it's essentially idempotent in this context 
 			return true;
 		}
 		
 		void waitForDone() throws InterruptedException {
 			acquireSharedInterruptibly(0);
 		}
 		
 		void waitForDone(long $nanosTimeout) throws InterruptedException, TimeoutException {
 			if (!tryAcquireSharedNanos(0, $nanosTimeout)) throw new TimeoutException();
 		}
 		
 		$V get() throws ExecutionException, CancellationException {	// this could also be implemented in the outer class, but it's ever so slightly more efficient to use the int state here instead of go through the array lookup.  probably.  i dunno, maybe that even gets optimized out by a good enough jvm.
 			if (getState() == State.CANCELLED.ordinal()) throw new CancellationException();
 			if ($exception != null) throw $exception;
 			return $result;
 		}
 		
 		protected void hearDone() {}    //TODO:AHS:THREAD: implement this later to punt off to a Listener
 		
 		
 		
 		boolean cancel(boolean $mayInterruptIfRunning) {
 			for (;;) {
 				int $s = getState();
 				if ($s == State.FINISHED.ordinal()) return false;
 				if ($s == State.CANCELLED.ordinal()) return false;
 				if (compareAndSetState($s, State.CANCELLED.ordinal())) break;
 			}
 			if ($mayInterruptIfRunning) {
 				Thread $r = $runner;
 				if ($r != null) $r.interrupt();
 			}
 			releaseShared(0);
 			hearDone();
 			return true;
 		}
 		
 		/**
 		 * <p>
 		 * Checks the state of this WorkFuture. Changes only occur if the
 		 * WorkFuture is currently WAITING and should become SCHEDULED. If the
 		 * task is clock-based, only delay is checked; otherwise, only
 		 * $work.isReady() is checked.
 		 * </p>
 		 * 
 		 * <p>
 		 * If this method returns false, it may merely because the task isn't
 		 * ready or is delayed, but it may also be because the task is FINISHED or
 		 * CANCELLED, which is something the caller is advised to check.
 		 * </p>
 		 * 
 		 * @return true if the scheduler must now remove the WF from the waiting
 		 *         pool (or delayed heap) and push it into the scheduled heap.
 		 */
 		//FIXME:AHS:THREAD: we have to notice doneness eventually even if no update was called and isReady is now returning false because it's done!  it's a bit troublesome since we'll never bubble to the top of the scheduled heap.  though really, the most straightforward fix isn't at all wrong: just run low-priority low-frequency fixed-rate task that calls update on all of the stuff in the waiting pool.  only question with that is who decides exactly what priority and how rate that should be, since it's clearly one of those things we're really only want one of per vm.
 		boolean scheduler_shift() {
 			if ($schedp.isUnclocked() ? $work.isReady() : $schedp.getDelay() <= 0) return compareAndSetState(State.WAITING.ordinal(), State.SCHEDULED.ordinal());
 			// the CAS will occur if:
 			//   - the task if delay-free (if clocked) or ready (if unclocked).
 			//        if this is not the case, obviously we won't be switching out of waiting state.
 			// the CAS will fail and return false if:
 			//   - we're FINISHED (if this happened at the end of the task's last run, this task shouldn't have stayed in the scheduler to get to this call again.  but it could have happened concurrently as a result of a thread outside the scheduler calling an update on a task that become done concurrently (close of a data source for example).)
 			//   - we've been CANCELLED concurrently
 			return false;
 		}
 		
 		/**
 		 * Causes the current thread to take ownership of the task and power it.
 		 * 
 		 * @return true if all went well and the task can be put back in a heap
 		 *         for more action later; false if there was a finish or
 		 *         concurrent cancel that the scheduler must respond to (by
 		 *         dropping the task).
 		 */
 		boolean scheduler_power() {
 			if (!compareAndSetState(State.SCHEDULED.ordinal(), State.RUNNING.ordinal())) {
 				// we were concurrently cancelled.  weep.
 				return false;
 			}
 			
 			$runner = Thread.currentThread();
 			if (getState() == State.RUNNING.ordinal()) { // recheck after setting thread
 				try {
 					$V $potentialResult = $work.call();
 					// the saved result here can be a little weird for any task that's not one-shot:
 					//  we can't reliably differentiate between the run that made us done, or being already done when the run started, because we can't lock that... so we can't reliably ensure that what's returned by the last run before finishing isn't already something that's allowed to be insane according to the contract of WorkTarget.
 					
 					//   (I thought about having a null return from a work target causing this class to not overwrite whatever the last result was...
 					///     Okay, follow that again.
 					///     Putting the burden of repeatedly returning the same result after doneness onto the WorkTarget class means it has to store that crap, and makes every single WorkTarget include boilerplate -- not much, perhaps, but my entire goal at all times is zero boilerplate.
 					///     Putting the burden of repeatedly returning the same result after doneness onto the WorkTarget class also means it's redundant with the storage already in WorkFuture, which is just code smell.
 					///     The only thing that makes me feel seriously dodgy about it is what if someone legitimately wants to finish with a null return?  They're restricted to never returning anything else. 
 					////      Oh wait.  Null returns can cause a doneness check?  but no.  that lets the null return of a WorkTarget have wickedly nondeterministic results.
 					///       Anyway, can I actually think of any place where something wants to return a final null but also have returned other things in the meanwhile?  No.  I mean, those people can probably even throw an exception as their alt channel if they want, since we are talking about the final time here.
 					///    And going back to basics, I really don't think anyone should ever have a sane reason to give a flying fuck about the return of a WorkFuture for a WorkTarget that repeats.  That whole result thing is really only ever intended to be used in one-shots.  Anyone else is pretty much 200% guaranteed to do better with a pipe for output.
 					
 					if ($work.isDone()) {
 						tryFinish(true, $potentialResult, null);
 						return false;	// even if tryFinish failed and returned false, since we're the running thread, that still means the task is finished (just that we weren't the ones to make it happen).
 					} else {
 						if ($potentialResult != null) $result = $potentialResult;
 						boolean $waiting = compareAndSetState(State.RUNNING.ordinal(), State.WAITING.ordinal());
 						if ($waiting) $schedp.setNextRunTime();
 						return $waiting;	// false if the cas to waiting failed (which would be due to a concurrent cancel)
 					}
 				} catch (Throwable $t) {
 					$exception = new ExecutionException($t);
 					tryFinish(true, null, $exception);
 					return false;
 				}
 			} else {
 				releaseShared(0); // there was a concurrent cancel or finish.  (note that this will result in null'ing $runner via tryReleaseShared(int).)
 				return false;
 			}
 		}
 		
 		/**
 		 * @return true if this invocation causes the finishing of this Future
 		 *         (and implicitly the final set and the completion notification).
 		 *         False if:
 		 *         <ul>
 		 *         <li>Finishing already occured (or occured concurrently in another thread).
 		 *         <li>Cancelling already occured (or occured concurrently in another thread).
 		 *         <li>The work is currently being run by another thread (in which case that thread is responsible for noticing finish).
 		 *         </ul>
 		 */
 		boolean tryFinish(final boolean $iAmTheRunner, $V $finish, ExecutionException $fail) {
 			// these come as a standard check whenever a shift returns false, or whenever an update call (from any thread!) noticed isDone was true.
 			// because this method is package-protected, we're going to assume that you've checked isDone before calling this, and that it was true.  (Sure, most people's isDone method is pretty cheap, but we're going to shoot for savings/contention-avoidance here anyway.)
 			// CONCURRENT FINISHES ARE BLOCKED IF WE'RE RUNNING.
 			//   because that's INSANE.  we have to set the answer of our current run.  and things often become "done" as data sources when they give us their last thing, but that clearly doesn't mean we're finished yet and shouldn't report the results of that last data piece.
 			//   (this is different than concurrently cancelled, mind; that's totally allowed.)
 			
 			for (;;) {
 				int $s = getState();
 				if ($s == State.FINISHED.ordinal())
 					return false;		// we're already done, obviously there's nothing to do here.  (we notice this in particular since we don't want to double-sent the done notification to listeners, nor invoke ridiculous releases).
 				if ($s == State.CANCELLED.ordinal()) {
 					releaseShared(0);	// aggressively release to set runner to null, in case we are racing with a cancel request that will try to interrupt runner
 					return false;		// we're letting the cancel go though (though trying to dodge any interrupts the cancellation might have requested).
 				}
 				if ($s == State.RUNNING.ordinal())
 					if ($iAmTheRunner);	// it's kay, we can continue to the finishing since this is our job.
 					else return false;	// the working thread will deal with noticing doneness again when it's completed its cycle.
 				// $s is either SCHEDULED or WAITING.  well, or RUNNING if we're the one running it.
 				if (compareAndSetState($s, State.FINISHED.ordinal())) {
 					// do the final set	// these are the couple of lines that kinda make a fool of that claim about get() returning "immediately" when state is FINISHED... it's not true if we happen to be in these three lines right here.
 					if ($fail != null) $exception = $fail;
 					else if ($finish == null);
 					else $result = $finish;
 					// do the final release and notify completion
 					releaseShared(0);
 					hearDone();
 					return true;
 				}
 			}
 		}
 	}
 	
 	
 	
 	public static class PriorityComparator implements Comparator<WorkFuture<?>> {
 		public static final PriorityComparator INSTANCE = new PriorityComparator();
 		
 		public int compare(WorkFuture<?> $o1, WorkFuture<?> $o2) {
 			return $o1.$work.getPriority() - $o2.$work.getPriority();
 		}
 	}
 	
 	
 	
 	public static class DelayComparator implements Comparator<WorkFuture<?>> {
 		public static final DelayComparator INSTANCE = new DelayComparator();
 		
 		public int compare(WorkFuture<?> $o1, WorkFuture<?> $o2) {
 			long $diff = $o1.$schedp.getNextRunTime() - $o2.$schedp.getNextRunTime();
 			if ($diff < 0) return -1;
 			if ($diff > 0) return 1;
 			return 0;
 		}
 	}
 }
