 package us.exultant.ahs.thread;
 
 import us.exultant.ahs.core.*;
 import us.exultant.ahs.util.*;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.concurrent.locks.*;
 
 /**
  * <p>
  * This class behaves almost exactly as per ConcurrentLinkedQueue (and is implemented
  * using it), but also provides additional semantics that allow one to determine at least
  * how many entries are in the queue as a constant time operation, and also provides a
  * blocking get functionality. This allows easy use as a work distributor, since one can
  * make get calls that will not return null simply because work is not instantaneously
  * available.
  * </p>
  * 
  * <p>
  * Requests that are blocking in nature are given fair ordering; nonblocking requests
  * disregard this entirely.
  * </p>
  * 
  * <p>
  * This Pipe will not accept nulls, and its WriteHead will throw a NullPointerException in
  * response to any attempt to write nulls.
  * </p>
  * 
  * @author hash
  * 
  */
 public class Pipe<$T> implements Flow<$T> {
 	/**
 	 * Constructs a new, open, active, empty, usable Pipe.
 	 */
 	public Pipe() {
 		$closed = new boolean[] { false };
 		$queue = new ConcurrentLinkedQueue<$T>();
 		$gate = new InterruptableSemaphore(0, true); // fair.
 		SRC = new Source();
 		SINK = new Sink();
 		$lock = new ReentrantLock();
 	}
 	
 	/**
 	 * <p>
 	 * The source from which one reads data from the pipe.
 	 * </p>
 	 * 
 	 * <p>
 	 * (Note that it would be perfectly possible to use just the ReadHead and
 	 * WriteHead interfaces internally as well... but this lets clients avoid ever
 	 * having to wrap calls in a no-op try-catch block that actually happens to be
 	 * unreachable.)
 	 * </p>
 	 */
 	public final Source			SRC;
 	
 	/**
 	 * <p>
 	 * The sink to which one writes data into the pipe.
 	 * </p>
 	 * 
 	 * <p>
 	 * (Note that it would be perfectly possible to use just the ReadHead and
 	 * WriteHead interfaces internally as well... but this lets clients avoid ever
 	 * having to wrap calls in a no-op try-catch block that actually happens to be
 	 * unreachable.)
 	 * </p>
 	 */
 	public final Sink			SINK;
 	
 	/**
 	 * This Listener is triggered for every completed write operation and for close
 	 * operations.
 	 */
 	private volatile Listener<ReadHead<$T>>	$el;
 	
 	/**
 	 * This is the data-containing buffer itself.
 	 */
 	private final ConcurrentLinkedQueue<$T>	$queue;
 	
 	/**
 	 * The write lock.
 	 */
 	public final Lock			$lock;
 	
 	/**
 	 * <p>
 	 * We always update this semaphore so that it's a <i>minimal</i> value &mdash;
 	 * that is, permits are not released until <i>after</i> an insertion into the
 	 * queue completes, and permits are acquired <i>before</i> any read from the queue
 	 * is attempted. Because of this policy, the semaphore may sometimes severely
 	 * underestimate the available work in order to provide this service with
 	 * guaranteeable correctness, since reading the amount of available work from this
 	 * semaphore is an operation that is never blocked. (Operations that simply effect
 	 * the head or tail of the queue are not likely to lead to drastic varation in the
 	 * number of permits available to the semphore).
 	 * </p>
 	 * 
 	 * <p>
 	 * When write is locked, the number of permits in this semaphore cannot grow (but
 	 * it can still shrink, because the semaphore is used to synchronize and order
 	 * read requests and there is no actual direct read lock). Thus, if write is
 	 * locked and all permits are then drained, an effcetive read locked is attained.
 	 * </p>
 	 */
 	private final InterruptableSemaphore	$gate;
 	
 	/**
 	 * This array is of length one. Its only value describes whether or not this Pipe
 	 * is closed to new writes; {@link #$gate} is interrupted immediately after this
 	 * value is set to true when closing a Pipe. (An array is used here instead of a
 	 * boolean primitive directly in order to provide a monitor for synchronization.)
 	 */
 	private final boolean[]			$closed;
 	
 	/**
 	 * @return {@link #SRC}.
 	 */
 	public Source source() {
 		return SRC;
 	}
 
 	/**
 	 * @return {@link #SINK}.
 	 */
 	public Sink sink() {
 		return SINK;
 	}
 	
 	/**
 	 * The minimal amount of work immediately available (see the documentation of {@link #$gate} for more details).
 	 * 
 	 * @return the minimal amount of work immediately available.
 	 */
 	public int size() {
 		return $gate.availablePermits();
 	}
 	
 	
 	
 	/**
 	 * {@link Pipe}'s internal implementation of ReadHead.
 	 * 
 	 * @author hash
 	 *
 	 */
 	public final class Source implements ReadHead<$T> {
 		private Source() {} // this should be a singleton per instance of the enclosing class
 		
 		/**
 		 * Sets the Listener that will be triggered for every completed write
 		 * operation on the matching {@link Sink} and upon close.
 		 */
 		public void setListener(Listener<ReadHead<$T>> $el) {
 			Pipe.this.$el = $el;
 		}
 		
 		public $T read() {
 			if (isClosed()) {
 				return readNow();
 			} else {
 				try {
 					// so... i need to acquire atomically with the closed check, or else acquire can happen immediately after an interrupt and end up blocking forever.
 					// the above is impossible.  so instead i just made the semaphore -always- throw interrupted exceptions after it's been interrupted once.
 					$gate.acquire();
 				} catch (InterruptedException $e) {
 					return null;
 				}
 				$T $v = $queue.remove();
 				return $v;
 			}
 		}
 		
 		public $T readNow() {
 			boolean $one = $gate.tryAcquire();
 			if (!$one) return null;
 			return $queue.poll();
 		}
 		
 		public boolean hasNext() {
 			return $gate.availablePermits() > 0;
 		}
 		
 		public List<$T> readAll() {
 			waitForClose();
 			return readAllNow();
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		public List<$T> readAllNow() {
 			$lock.lock();
 			try {
 				int $p = $gate.drainPermits();
 				List<$T> $v = new ArrayList<$T>($p);
 				for (int $i = 0; $i < $p; $i++)
 					$v.add($queue.poll());
 				return $v;
 			} finally {
 				$lock.unlock();
 			}
 		}
 		
 		public boolean isClosed() {
 			return $closed[0];
 		}
 		
 		/**
		 * Closes the pipe. No more data will be allowed to be written to this
		 * Pipe's WriteHead. Any blocked {@link Pipe.Source#readAll()} will return
		 * whatever data they can, and other blocked {@link Pipe.Source#read()}
		 * will immediately return null following this call even if they cannot
		 * get data (these two processes still happen in fair order). All
		 * subsequent reads (blocking or nonblocking) will either return data
		 * immediately as long as any is still buffered in the pipe, and then
		 * forevermore immediately return null once all buffered data is depleted.
 		 */
 		public void close() {
 			$lock.lock();
 			try {
 				$closed[0] = true; // set our state to closed
 			} finally {
 				$lock.unlock();
 			}
 			$gate.interrupt(); // interrupt any currently blocking reads
 			X.notifyAll($closed); // trigger the return of any final readAll calls
 			
 			// give our listener a chance to notice our closure.
 			Listener<ReadHead<$T>> $dated_el = $el;
 			if ($dated_el != null) $dated_el.hear(this);
 		}
 		
 		private void waitForClose() {
 			synchronized ($closed) {
 				while (!isClosed())
 					X.wait($closed);
 			}
 		}
 	}
 	
 	public final class Sink implements WriteHead<$T> {
 		private Sink() {} // this should be a singleton per instance of the enclosing class
 		
 		/**
 		 * Writes a single chunk of data to this Sink. After the data is commited,
 		 * a permit is released and the data becomes available for reading from
 		 * the {@link Source}, and the pipe's listener is then notified of the
 		 * event (note: the listener is called using the writing thread).
 		 * 
 		 * @throws IllegalStateException
 		 *                 if this Pipe is closed.
 		 * @throws NullPointerException
 		 *                 if the chunk is null
 		 */
 		public void write($T $chunk) throws IllegalStateException {
 			$lock.lock();
 			try {
 				if (isClosed()) throw new IllegalStateException("Pipe has been closed.");
 				$queue.add($chunk);
 				$gate.release();
 				
 				Listener<ReadHead<$T>> $el_dated = $el;
 				if ($el_dated != null) $el_dated.hear(SRC);
 				//XXX:AHS:EFFIC:THREAD: I don't think we have to do this notification from within the write-lock, do we?  (i guess we do in a batch situation whether we want to or not, though.)
 			} finally {
 				$lock.unlock();
 			}
 		}
 		
 		/**
 		 * <p>
 		 * Writes every element of the given collection to this Sink. All elements
 		 * added as a group in this way are guaranteed to come out of the Pipe in
 		 * the same order as the original ordering of the collection, and shall
 		 * not be intermingled with other objects. Some elements added as a group
 		 * in this way may be read and cause notifications to the pipe's listener
 		 * before the entire group is added (so it may be possible for a thread
 		 * reading from a pipe in a non-blocking fashion to read half of the group
 		 * of elements, then get nulls, and then later return to see the other
 		 * half of the group).
 		 * </p>
 		 * 
 		 * <p>
 		 * Put another way: a single write-lock is maintained for the duration of
 		 * this method, but every element causes a release of permit and
 		 * notification to the pipe's listener.
 		 * </p>
 		 * 
 		 * @throws IllegalStateException
 		 *                 if this Pipe is closed.
 		 * @throws NullPointerException
 		 *                 if any chunk in the collection is null
 		 */
 		public void writeAll(Collection<? extends $T> $chunks) {
 			// at first i thought i could implement this with addAll on the queue and a single big release... not actually so.  addAll on the queue can throw exceptions but still have made partial progress.
 			$lock.lock();
 			try {
 				for ($T $chunk : $chunks)
 					write($chunk);
 			} finally {
 				$lock.unlock();
 			}
 		}
 		
 		/**
 		 * Returns true. Pipe doesn't implement any capacity restrictions, so this
 		 * isn't really ever in question. isClosed is technically an unrelated
 		 * question.
 		 * 
 		 * @return true
 		 */
 		public boolean hasRoom() {
 			return true;
 		}
 		
 		public boolean isClosed() {
 			return $closed[0];
 		}
 		
 		/**
		 * Closes the pipe. No more data will be allowed to be written to this
		 * Pipe's WriteHead. Any blocked {@link Pipe.Source#readAll()} will return
		 * whatever data they can, and other blocked {@link Pipe.Source#read()}
		 * will immediately return null following this call even if they cannot
		 * get data (these two processes still happen in fair order). All
		 * subsequent reads (blocking or nonblocking) will either return data
		 * immediately as long as any is still buffered in the pipe, and then
		 * forevermore immediately return null once all buffered data is depleted.
 		 */
 		public void close() {
 			SRC.close();
 		}
 	}
 
 	public void close() {
 		SRC.close();
 	}
 	
 	/** Do not use this method unless you know what you're doing.  It should never be needed under normal circumstances. */
 	void lockWrite() {
 		$lock.lock();
 	}
 	/** Do not use this method unless you know what you're doing.  It should never be needed under normal circumstances. */
 	void unlockWrite() {
 		$lock.unlock();
 	}
 }
