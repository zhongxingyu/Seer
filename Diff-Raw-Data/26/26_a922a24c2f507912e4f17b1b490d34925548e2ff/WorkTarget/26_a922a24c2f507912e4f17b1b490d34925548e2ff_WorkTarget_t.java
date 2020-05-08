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
 
 /**
  * <p>
  * A WorkTarget is similar to {@link Runnable} and/or {@link Callable}, but combines the
  * concept of runnability with properties essential for intelligent scheduling of work.
  * Implementations of the WorkTarget interface which define their readiness for scheduling
  * based on availability of messages from a {@link us.exultant.ahs.core.ReadHead} form the
  * essence of the Actor model of concurrent programming.
  * </p>
  * 
  * <p>
  * One WorkTarget instance must be created for every thread that you wish to be able to
  * perform that type of task concurrently, and it never makes sense to submit the same
  * WorkTarget into more than one WorkScheduler.
  * </p>
  * 
  * @author hash
  * @param <$V>
  *                The type returned by {@link #call()}. (This can often be {@link Void} in
  *                the case of WorkTarget that are designed to be called repeatedly, since
  *                they tend to ferry data in and out via {@link ReadHead} and
  *                {@link WriteHead} instead of simply returning one piece of data.)
  */
 public interface WorkTarget<$V> extends Callable<$V> {
 	/**
 	 * <p>
 	 * Defines whether or not this WorkTarget currently has enough data available to
 	 * complete some atom of work immediately if powered.
 	 * </p>
 	 * 
 	 * <p>
 	 * The return value of this method is used by {@link WorkScheduler} to determine
 	 * whether or not it is presently appropriate to consider scheduling this
 	 * WorkTarget to be powered by a thread. {@link WorkScheduler} checks this method
	 * under two conditions: whenever it finishes a run of the work, and sometime
	 * after an update is requested via the {@link WorkScheduler#update(WorkFuture)}
	 * method. (Typically, updates are requested when triggered (i.e. by a callback on
	 * a {@link Pipe} that feeds work data for this target; this is the preferred
	 * mechanism); otherwise it may be done in polling fashion (this mechanism can be
	 * useful if work data is produced at such a high volume that the reduced
	 * synchronization is a performance improvement, but the event-based mechanism is
	 * generally preferred and will have better performance when the system is at
	 * rest. In either case it is necessary to make these arrangements after
	 * scheduling a WorkTarget, since the WorkFuture returned from the scheduling
	 * process is necessary.)
 	 * </p>
 	 * 
 	 * <p>
 	 * The answer to this question is based on a best-effort system and may not be
 	 * exactly true under all circumstances due to the concurrent nature of the
 	 * question. For example, this WorkTarget may claim to be ready when asked by the
 	 * WorkScheduler, but then may turn out to not find work when it's actually
	 * powered because some other WorkTarget has drained from the same Pipe that
 	 * provides the flow of work input data to both WorkTarget.
 	 * </p>
 	 * 
 	 * @return true if this WorkTarget has at least one atom of work available; false
 	 *         otherwise.
 	 */
 	public boolean isReady();
 	
 	/**
 	 * <p>
 	 * Causes the current thread to be consumed in the running of the
 	 * <code>WorkTarget</code>, similarly to the {@link Runnable#run()} method.
 	 * </p>
 	 * 
 	 * <p>
 	 * This method can be called at any time, and any number of times, and need not be
 	 * reentrant &mdash it is the responsibility of the caller to make sure that calls
 	 * to this method are properly synchronized. (If a WorkTarget is scheduled with a
 	 * {@link WorkScheduler}, this is handled automatically.)
 	 * </p>
 	 * 
 	 * <p>
 	 * Each invocation of this method may return a value or throw an exception after
 	 * performing its work, and this result may be different with every invocation.
 	 * Typically this result is most convenient to use for tasks that are only run
 	 * once; for tasks that may be run repeatedly, it is often more convenient to use
 	 * a {@link Pipe.Sink} to gather output and set the generic return type to
 	 * {@link Void}.
 	 * </p>
 	 * 
 	 * <p>
 	 * Calling this method after {@link #isDone()} returns true is allowed to have
 	 * undefined results (i.e., may return any value, or null, or throw an exception),
 	 * but it MUST return immediately. (When submitted to a {@link WorkScheduler}, the
 	 * {@link WorkFuture#get()} method of the returned {@link WorkFuture} can be used
 	 * to consistently access the final result of the work.)
 	 * </p>
 	 * 
 	 * <p>
 	 * It is typically expected that this method will be called by a thread from a
 	 * pool kept within a {@link WorkScheduler}. As such, all actions taken by this
 	 * method should not resort to waiting nor use blocking operations in order for a
 	 * complete system of scheduled WorkTargets to work smoothly and with high
 	 * efficiency. (In a practical sense, this interface cannot impose a strict limit
 	 * on the actual time that will be consumed by this call; if for example the
 	 * <code>WorkTarget</code> is assigned to some sort of blocking I/O channel, the
 	 * call may wait indefinitely in the same fashion as the underlying channel.
 	 * However, this sort of behavior should be avoided at all costs since
 	 * {@link WorkScheduler} assigns threads based on the assumption that this sort of
 	 * idiocy will not be performed.)
 	 * </p>
 	 * 
 	 * <h3>Additional return constraints when using a {@link WorkScheduler}</h3>
 	 * <p>
 	 * WorkScheduler implementations and the WorkFuture they link to a WorkTarget make
 	 * it possible to consistently access the "final" result of a WorkTarget that has
 	 * become done. However, they also add a few special rules:
 	 * <ul>
 	 * <li>Exceptions thrown from this {@link #call()} method cause the WorkScheduler
 	 * to stop handling this WorkTarget, and the WorkFuture will always thrown that
 	 * exception (wrapped in an {@link ExecutionException}) from
 	 * {@link WorkFuture#get()}.
 	 * <li>Whenever this {@link #call()} method returns <tt>null</tt>, that return is
 	 * ignored &mdash; if the WorkTarget becomes finished before any other invocations
 	 * of <tt>call()</tt>, then the {@link WorkFuture#get()} method will return the
 	 * most recent non-null return from the <tt>call()</tt> method. (It is still
 	 * possible for {@link WorkFuture#get()} to return <tt>null</tt> if
 	 * <tt>call()</tt> only ever returns null.)
 	 * <li>{@link WorkFuture#get()} will throw a {@link CancellationException} if
 	 * {@link WorkFuture#cancel(boolean)} was called before this WorkTarget became
 	 * done (regardless of anything this <tt>call</tt> method has already returned).
 	 * </ul>
 	 * </p>
 	 */
 	public $V call() throws Exception;
 	
 	/**
 	 * <p>
 	 * Implementers use this method to allow the WorkTarget's <code>run()</code>
 	 * method to know when to stop spinning.
 	 * </p>
 	 * 
 	 * <p>
	 * Once this method returns true, it should never again return false. (In other
	 * words, the property of doneness must be idempotent.)
 	 * </p>
 	 * 
 	 * @return false if the WorkScheduler should continue to call
 	 *         <code>run(int)</code> cyclically; true to when the cycle should exit at
 	 *         its earliest opportunity.
 	 */
 	public boolean isDone();
 	
 	/**
 	 * <p>
 	 * Estimates the priority with which this WorkTarget should be scheduled relative
 	 * to other WorkTarget at the same WorkScheduler; a higher priority indicates that
 	 * this WorkTarget should be scheduled preferentially.
 	 * </p>
 	 * 
 	 * <p>
 	 * WorkScheduler may use this priority to the best of its ability in scheduling
 	 * work for powering by the scheduler's threads, but the priority at any point in
 	 * time is at most a best-effort basis and not a guarantee of order due to the
 	 * concurrent nature of the scheduling and furthermore because a WorkScheduler
 	 * implementation may balance concerns other than priority (such as tasks
 	 * scheduled based on wall-clock time).
 	 * </p>
 	 * 
 	 * <p>
 	 * This priority may change over time (this could be useful for example in a
 	 * program which has several work buffers and always wishes to service the fullest
 	 * one, or similar the least recently served one); WorkScheduler who obey the
 	 * priority hint will do their best to respond to this in a timely manner every
 	 * time they are told to update their relationship with this WorkTarget via an
 	 * invocation of the {@link WorkScheduler#update(WorkFuture)} method.
 	 * </p>
 	 * 
 	 * <p>
 	 * Another example of effective use of the priority system is a server application
 	 * which is composed of three types of task: nonblocking reads, some application
 	 * logic, and nonblocking writes of the result. By simply giving writes the
 	 * highest priority, application logic a medium priority, and reads the lowest
 	 * priority, the entire application becomes optimized to keep all of its pipes
 	 * between these task as small as possible, and furthermore if the reads happen to
 	 * be based on some system including a sliding window (namely, TCP), the TCP
 	 * buffer naturally fills in the OS kernel layer and causes the TCP window to
 	 * automatically adapt when the application level logic is over burdened &mdash;
 	 * all with no special effort from the application designer except for setting
 	 * those three priorities.
 	 * </p>
 	 * 
 	 * @return an integer representing the priority with which this WorkTarget should
 	 *         currently be considered if it has work ready.
 	 */
 	public int getPriority();
 	
 	
 	
 	/**
 	 * <p>
 	 * Compares two WorkTarget based on their priority alone. This is useful for
 	 * priority queues.
 	 * </p>
 	 * 
 	 * <p>
 	 * Note: this comparator imposes orderings that are inconsistent with equals.
 	 * </p>
 	 * 
 	 * <p>
 	 * Implementation note: this comparator is implemented as simple integer math
 	 * without overflow checking, so if it is applied to datasets which contain
 	 * priority values that can have a difference larger than
 	 * {@link Integer#MAX_VALUE} results are unpleasant. Basically, keep your priority
 	 * values between a billion and negative one billion and you'll be fine.
 	 * </p>
 	 * 
 	 * @author hash
 	 * 
 	 */
 	public static class PriorityComparator implements Comparator<WorkTarget<?>> {
 		public int compare(WorkTarget<?> $o1, WorkTarget<?> $o2) {
 			// we could of course just compare Math.max(prio,2^31)... but i'm intending to use this in the tight bit of a synchronization block that the whole vm pivots around.  so, we're going with the 99% solution here because it runs faster.
 			return $o1.getPriority() - $o2.getPriority();
 		}
 	}
 	
 
 
 	/**
 	 * <p>
 	 * Bridges the gap between {@link Runnable} and WorkTarget.
 	 * </p>
 	 * 
 	 * <p>
 	 * Implementation note: all of the methods of this class are synchronized, which
 	 * means if you ask if this WorkTarget is ready while it's being run, you might
 	 * well be waiting a while for an answer. This design choice was made on the
 	 * presumption that there's no rational reason to be asking those state questions
 	 * whilst the task is in progress.
 	 * </p>
 	 * 
 	 * @author hash
 	 */
 	public static final class RunnableWrapper implements WorkTarget<Void> {
 		public RunnableWrapper(Runnable $wrap) { this($wrap,0,true); }
 		public RunnableWrapper(Runnable $wrap, boolean $once) { this($wrap,0,$once); }
 		public RunnableWrapper(Runnable $wrap, int $prio) { this($wrap,$prio,true); }
 		public RunnableWrapper(Runnable $wrap, int $prio, boolean $once) {
 			this.$once = $once;
 			this.$prio = $prio;
 			this.$wrap = $wrap;
 		}
 		
 		private final boolean		$once;
 		private final int		$prio;
 		private volatile Runnable	$wrap;	// flip this to null when it's done.
 		
 		public Void call() {
 			try {
 				if ($wrap == null) throw new IllegalStateException("This task can only be run once, and is already done!");
 				// yes, it's possible for $wrap to become null betweent the above check and the below call
 				//   and yes, that'll throw a NullPointerException.
 				// That's acceptable within the contract of WorkTarget!
 				//   1. We're returning immediately without doing work since we're done (with an unchecked exception, but nonetheless.)
 				//   2. We're allowed to return either null or throw an exception; either is legit.
 				//   3. The NullPointerException only even comes up if more than one thread calls this method at the same time.
 				//        We aren't sync'd against that -- which is fine because the contract says that's not our responsibility.
 				$wrap.run();
 			} finally {
 				if ($once) $wrap = null;
 			}
 			return null;
 		}
 		
 		/** We have no clue whether or not the runnable has work to do, so unless it was a one-time task that has been finished, we have no choice but to assume it does. */
 		public boolean isReady() { return !isDone(); }
 		/** We have no clue whether or not the runnable has work to do, so unless it was a one-time task that has been finished, we have no choice but to assume it does. */
 		public boolean isDone() { return ($wrap == null); }
 		public int getPriority() { return $prio; }
 	}
 	
 	//TODO:AHS:THREAD: a readymade WorkTarget implementation which oneshots itself in response to one or more Future becoming done.  this will tend to be what gets used whenever you might otherwise have wished for a continuation/park (and where other libraries are resorting to serious weaving).
 }
