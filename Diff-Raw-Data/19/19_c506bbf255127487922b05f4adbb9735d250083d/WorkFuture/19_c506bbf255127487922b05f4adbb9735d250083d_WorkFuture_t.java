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
 import us.exultant.ahs.anno.*;
 import java.util.*;
 import java.util.concurrent.*;
 
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
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  * @param <$V>
  */
 public interface WorkFuture<$V> extends Future<$V> {
 	@ThreadSafe
 	@Nullipotent
 	public State getState();
 	
 	@ThreadSafe
 	@Nullipotent
 	public ScheduleParams getScheduleParams();
 	
 	@ThreadSafe
 	@Nullipotent
 	public boolean isCancelled();
 	
 	@ThreadSafe
 	@Nullipotent
 	public boolean isDone();
 	
 	@ThreadSafe
 	@Nullipotent
 	public $V get() throws InterruptedException, ExecutionException, CancellationException;
 	
 	@ThreadSafe
 	@Nullipotent
 	public $V get(long $timeout, TimeUnit $unit) throws InterruptedException, ExecutionException, TimeoutException, CancellationException;
 	
 	/**
 	 * <p>
 	 * Attempts to cancel execution of this task. This attempt will fail if the task
 	 * has already completed, has already been cancelled, or could not be cancelled
 	 * for some other reason. If successful, and this task has not started when cancel
 	 * is called, this task should never run. If the task has already started, then
 	 * the mayInterruptIfRunning parameter determines whether the thread executing
 	 * this task should be interrupted in an attempt to stop the task.
 	 * </p>
 	 * 
 	 * <p>
 	 * After this method returns, subsequent calls to {@link #isDone()} will always
 	 * return true. Subsequent calls to {@link #isCancelled()} will always return true
	 * if this method returned true (they may also return true if this method returned
	 * false; this depends on whether this method returned false because another
	 * thread performed cancellation concurrently, or because the task finished
	 * concurrently).
 	 * </p>
 	 * 
 	 * <p>
	 * Note: the documentation of {@link Future#cancel(boolean)} is potentially
	 * misleading. It states that the method should return "<tt>false</tt> if the task
	 * could not be cancelled, typically because it has already completed normally;
	 * <tt>true</tt> otherwise" &mdash; this use of "normally" would seem to imply
	 * that if another thread called cancel concurrently, both should return true.
	 * This is in fact NOT what the canonical implementation of
	 * {@link FutureTask#cancel(boolean)} does; that implementation acts identially to
	 * this one in that if several threads attempt to cancel a task concurrently, only
 	 * one of them should get a true return.
 	 * </p>
 	 * 
 	 * @param $mayInterruptIfRunning
 	 *                true if the thread executing this task should be interrupted;
 	 *                otherwise, in-progress tasks are allowed to complete
 	 * @returns whether or not this thread was responsible for cancelling
 	 * 
 	 */
 	@ThreadSafe
 	@Idempotent
 	public boolean cancel(boolean $mayInterruptIfRunning);
 	
 	/**
 	 * Functions exactly as calling {@link WorkScheduler#update(WorkFuture)} with this
 	 * object on its parent scheduler.
 	 */
 	@ThreadSafe
 	@Idempotent
 	public void update();
 	
 	/**
 	 * <p>
 	 * Adds a listener to this WorkFuture which will be called as soon as possible
 	 * after the task represented by this WorkFuture has become done (regardless if by
 	 * normal completion, a deadly exception, or external cancellation). The listener
 	 * will be called exactly once (presuming of course that the task ever finishes).
 	 * </p>
 	 * 
 	 * <p>
 	 * This method may be called at any time from any thread.
 	 * </p>
 	 * 
 	 * <p>
 	 * The listener should only perform very fast operations; any heavy lifting should
 	 * be performed in another thread which is triggered by this listener. The
 	 * listener may be called by a thread from within a WorkScheduler's pool, or by a
 	 * thread that caused cancellation or concurrent completion (i.e. by work
 	 * draining) of a task, or by the same thread that attempted to add it (in case
 	 * the task was already done).
 	 * </p>
 	 */
 	@ThreadSafe
 	public void addCompletionListener(Listener<WorkFuture<?>> $completionListener);
 	
 	
 	
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
 		 * result in this Future becoming FINISHED, though in that case the
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
 		
 		final static State[] values = State.values();
 	}
 	
 	
 	
 	public static class DelayComparator implements Comparator<WorkFuture<?>> {
 		public static final DelayComparator INSTANCE = new DelayComparator();
 
 		/** @return positive if the first arg should be run sooner than the second */
 		public int compare(WorkFuture<?> $o1, WorkFuture<?> $o2) {
 			final long $diff = $o1.getScheduleParams().getNextRunTime() - $o2.getScheduleParams().getNextRunTime();
 			if ($diff > 0) return -1;
 			if ($diff < 0) return 1;
 			return 0;
 		}
 	}
 }
