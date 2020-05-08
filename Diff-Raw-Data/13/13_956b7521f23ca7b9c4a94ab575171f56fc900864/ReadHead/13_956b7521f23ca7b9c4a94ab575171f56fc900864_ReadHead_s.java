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
 
 package us.exultant.ahs.core;
 
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  * <p>
  * Provides an interface to make file, network, and internal pipe operations all
  * transparently and operational in either blocking or nonblocking modes. This unified
  * scheme has the noteworthy aspect of always allowing incoming data to be requested at
  * the application's leisure (as opposed to requiring any sort of event listener), while
  * simultaneously allowing event listeners to be attached if the old "listener" pattern is
  * desired or "select"-like functionality need be implemented across multiple ReadHead
  * instances. ReadHead is the complement of {@link WriteHead}.
  * </p>
  * 
  * <p>
  * Unlike other stream and channel interfaces, ReadHead intends to always be aligned to
  * something <i>meaningful</i> &mdash; though what exactly constitutes "meaningful" is
  * left up to the implementation, and is specified by the generic type. ReadHead
  * specifically intends to prevent any excessive copying of arrays around by its clients;
  * a single instance of the generic object returned by the read methods should be
  * sufficient to warrant processing in its own right, and the readAll methods are provided
  * only as a matter of completeness and convenience (and because many stream and channel
  * interfaces lack such simple conveniences) for use when the total volume of data is
  * manageably small. Thus, a binary transport with a ReadHead interface would typically be
  * designed to return ByteBuffer representing an entire frame of data.
  * </p>
  * 
  * <p>
  * Implementers of this interface could potentially be used to decorate an instance
  * {@link java.io.InputStream}, {@link java.util.concurrent.ConcurrentLinkedQueue},
  * {@link java.nio.channels.Channel}... whatever. Most typically, communication between
  * threads in a single VM is handled by using a {@link us.exultant.ahs.thread.Pipe}, which
  * provides highly efficient synchronized message passing with ReadHead and WriteHead
  * interfaces. Communication across sockets or to the filesystem is provided ready-made by
  * ReadHead implementations in the {@link us.exultant.ahs.io} package.
  * </p>
  * 
  * <p>
  * Implementers are typically expected to follow some sort of RAII (Resource Acquisition
  * Is Initialization) pattern, as no methods regarding opening or initializing are
  * provided by this interface, but there is absolutely no requirement that implementers do
  * so, and methods responsible for initialization may be as apt.
  * </p>
  * 
  * <p>
  * Implementers should avoid throwing OperationNotSupported exceptions from methods
  * relating to nonblocking reads if at all possible even when the underlying stream or
  * channel is provides only blocking services. Such issues can be resolved by reading the
  * blocking stream into a nonblocking buffer using an external thread, then providing
  * direct access only to the nonblocking buffer. (The {@link us.exultant.ahs.thread.Pipe}
  * class in the {@link us.exultant.ahs.thread} module of AHSlib is an ideal helper for
  * such a role.)
  * </p>
  * 
  * <p>
  * Note that the lack of methods for reading contiguous blocks; this is neither accidental
  * nor an oversight. The author asserts that if you find yourself with a desire for this
  * behavior, you're Doing It Wrong. The problem you are confronting is probably best
  * solved by using a ReadHead with a generic type that it itself a generic List, or using
  * some other sort of object as a container for batches.
  * </p>
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  * @param <$T>
  */
 public interface ReadHead<$T> {
 	/**
 	 * <p>
 	 * Sets a {@link Listener} for critical events in the life cycle of the ReadHead.
 	 * The Listener must be invoked by the implementer every time a new chunk of data
 	 * becomes available. Implementations may also choose to use it to signal other
 	 * critical events such as the closing of the underlying stream, or the drain of
 	 * the final piece of data from an already-closed stream. In all situations, the
 	 * Listener is handed a reference to this ReadHead.
 	 * </p>
 	 * 
 	 * <p>
 	 * Despite the listener's intended purpose, it is critical to note that even if a
 	 * {@link #hasNext()} call is the first thing within the Listener's
 	 * {@link Listener#hear(Object) hear(ReadHead<$T>)} procedure, that call may
 	 * return false. This may situation may arise even if the call to the Listener was
 	 * intended to signal new data availability, since multithreaded access to the
 	 * ReadHead can result in another thread having pre-empted the Listener and
 	 * consumed the data before the Listener can respond.
 	 * </p>
 	 * 
 	 * <p>
 	 * The Listener's {@link Listener#hear(Object) hear(ReadHead<$T>)} method will be
 	 * invoked from the thread that caused the state change, and as such must not be
 	 * responsible for any intensive or time-consuming operations, nor is it allowed
 	 * to block. The recommended usage is to simply provide an event listener that
 	 * marks the ReadHead as (probably) having data available in some other
 	 * "selector"-like scheme. (If you're unfamiliar with
 	 * {@link us.exultant.ahs.thread.WorkTarget} and
 	 * {@link us.exultant.ahs.thread.WorkScheduler} tools, consider reading up on
 	 * those; you may find them extremely useful because they're able to deal with
 	 * such concepts of readiness very powerfully and efficiently).
 	 * </p>
 	 * 
 	 * <p>
 	 * Implementers typically call the listener's {@link Listener#hear(Object)} method
 	 * once per semantic event &mdash however, this is not required, and if multiple
 	 * new chunks become available in a batch, then it is allowed for the implementer
 	 * to only invoke the listener one time after the entire batch is loaded in order
 	 * to reduce noise. Therefore, users of events from ReadHead are advised to use
 	 * {@link #readAllNow()} instead of just {@link #readNow()} (or use
 	 * {@link #readNow()} in a loop until it returns null, which may provide better
 	 * load balancing between reading threads if applicable) in response to hearing an
 	 * update. (Again, consider applying a {@link us.exultant.ahs.thread.WorkTarget}
 	 * here: implementing a {@link us.exultant.ahs.thread.WorkTarget#call()} method
 	 * that uses {@link #readNow()} in a WorkTarget and defines
 	 * {@link us.exultant.ahs.thread.WorkTarget#isReady()} by {@link #hasNext()} is a
 	 * useful idiom.)
 	 * </p>
 	 * 
 	 * @param $el
 	 */
 	public void setListener(Listener<ReadHead<$T>> $el);
 	
 	/**
 	 * <p>
 	 * Blocking read. Elements that are read are removed from the stream.
 	 * </p>
 	 * 
 	 * <p>
 	 * If multiple threads block on this concurrently, the choice of whether or not to
 	 * provide a guarantee of fairness is left up to the implementer. Regardless,
 	 * basic synchronicity must be maintained &mdash; there will be no double-reads,
 	 * null pointer exceptions, concurrent modification exceptions, or etc.
 	 * </p>
 	 * 
 	 * @return next chunk of input, or null if there is no data available and the
 	 *         underlying stream has reached an {@code EOF} state. Some
 	 *         implementations may also choose to return null in the case that the
 	 *         underlying stream reaches an {@code EOF} state while this call is still
 	 *         blocking for more input; others may try to return a partial chunk.
 	 */
 	public $T read();
 	
 	/**
 	 * Nonblocking read. Elements that are read are removed from the stream.
 	 * 
 	 * @return a chunk of input if possible, or null otherwise; null may indicate
 	 *         either <code>EOF</code> or simply nothing available at the time.
 	 *         {@link #isClosed()} should be used to determine the difference.
 	 */
 	public $T readNow();
 
 	/**
 	 * <p>
 	 * Blocking read with timeout. Elements that are read are removed from the stream.
 	 * </p>
 	 * 
 	 * <p>
 	 * If multiple threads block on this concurrently, the choice of whether or not to
 	 * provide a guarantee of fairness is left up to the implementer. Regardless,
 	 * basic synchronicity must be maintained &mdash; there will be no double-reads,
 	 * null pointer exceptions, concurrent modification exceptions, or etc.
 	 * </p>
 	 * 
 	 * <p>
 	 * Since null is returned from this method both in the case of timeouts and
 	 * emptiness of a closed data source, if it is necessary to make sure all data has
 	 * been drained from the ReadHead, then a null return from this method should be
 	 * followed by a check of {@link #isExhausted()}, followed by looping back to
 	 * another {@link #readSoon(long,TimeUnit)} if the ReadHead is not exhausted. Note
 	 * that this is not the same as checking {@link #isClosed()} after a null return!!
 	 * Checking {@link #isClosed()} instead of {@link #isExhausted()} would allow a
 	 * race condition where a final piece of data becomes available for reading
 	 * between a {@link #readSoon(long,TimeUnit)} call that timed out and when the
 	 * {@link #isClosed()} check is made, which would result in that data being
 	 * unnoticed.
 	 * </p>
 	 * 
 	 * @return next chunk of input, or null if the timeout elapses without a chunk
 	 *         becoming available, or null if there is no data available and the
 	 *         underlying stream has reached an {@code EOF} state. Some
 	 *         implementations may also choose to return null in the case that the
 	 *         underlying stream reaches an {@code EOF} state while this call is still
 	 *         blocking for more input; others may try to return a partial chunk.
 	 */
 	public $T readSoon(long $timeout, TimeUnit $unit);
 	
 	/**
 	 * Tells whether or not input is immediately available to be read. If the ReadHead
 	 * is shared by multiple threads this should not be relied upon to determine if a
 	 * subsequent blocking call will return without waiting, since other threads may
 	 * have already pre-empted it.
 	 * 
 	 * @return true if a chunk of input is stream to be read immediately; false
 	 *         otherwise. Similarly to {@link #readNow()}, a return of false may
 	 *         indicate either <code>EOF</code> or simply nothing available at the time.
 	 *         {@link #isClosed()} should be used to determine the difference.
 	 */
 	public boolean hasNext();
 	
 	/**
 	 * <p>
 	 * Blocks until the stream is closed, then returns its entire contents at once
 	 * (minus any entries that have already been read, even if those other reads take
 	 * place <i>after</i> the invocation of <code>readAll()</code>, and including even
 	 * entries that may have been written to the stream after this invocation of
 	 * {@link #readAll()}). If multiple threads invoke this, then one of them will
 	 * receive a normal result, and the rest will receive empty lists (as will
 	 * subsequent invocations).
 	 * </p>
 	 * 
 	 * <p>
 	 * <i>Note:</i> if you feel this behavior (waiting until the end of stream but
 	 * still allowing other reads) odd, consider the following points:
 	 * <ol>
 	 * <li>If you're afraid of losing data between invocation and reaching EOF, simply
 	 * ensure you've already stopped reading that might take place in any other
 	 * threads.
 	 * <li>This behavior is meant to help ensure all data has been retrieved by the
 	 * time a reading thread exits &mdash; call it in every thread and between all of
 	 * them they'll get everything.
 	 * <li>In some implementations (namely those based on semaphore permits), it's
 	 * difficult to stop other readers internally without linking the locking of reads
 	 * and writes, which is highly undesirable since it implies greater complexity,
 	 * overhead, and contention to all calls.
 	 * </ol>
 	 * </p>
 	 * 
 	 * @return a list containing one entry for each chunk of input following the last
 	 *         invocation of a read method that is available from the stream between
 	 *         the time of this method's invocation and the closing of the stream. The
 	 *         array returned may have zero entries if no data ever becomes available
 	 *         (including if the stream is already closed and empty when the
 	 *         invocation occurs), but null may never be returned.
 	 * @throws UnsupportedOperationException
 	 *                 if the underlying stream has no notion of closed or finished,
 	 *                 since this method is then not well defined.
 	 * 
 	 */
 	public List<$T> readAll();
 	
 	/**
 	 * Immediately returns entire contents of this stream at once (minus, of course,
 	 * any entries that have already been read). Similarly to the blocking
 	 * {@link #readAll()} method, if multiple threads invoke this after the stream is
 	 * closed then the first will receive a normal result, and other threads and
 	 * subsequent invocations will receive empty lists.
 	 * 
 	 * @return a list containing one entry for each chunk of input following the last
 	 *         invocation of a read method that is currently available from the
 	 *         stream. The list returned may have zero entries if there is no input
 	 *         currently available, but null may never be returned.
 	 */
 	public List<$T> readAllNow();
 	
 	
 
 	/**
 	 * <p>
 	 * Returns the closure status of this ReadHead (in other words, whether or not
 	 * this buffer is capable of growing).
 	 * </p>
 	 * 
 	 * <p>
 	 * Note that if a ReadHead is semantically paired with a WriteHead (as in a the
 	 * two heads of a pipe, or the two endpoints of a network connections), their
 	 * closures are not necessarily related. In a network connection for example
 	 * clearly it is possible for one side to consider the connection closed well
 	 * before the other side notices the change in state. Check the documentation of
 	 * implementators in any system that deals with semantically linked ReadHead and
 	 * WriteHead to see how they deal with synchronity in this situation.
 	 * </p>
 	 * 
 	 * @return true if the ReadHead has internally reached some sort of
 	 *         <code>EOF</code> state; false otherwise. If true is ever returned, no
 	 *         subsequent invocations may return false (i.e. this method is
 	 *         idempotent). Upon returning true, data may still exist in buffers
 	 *         waiting to be read; however, it is guaranteed that once both
 	 *         {@link #isClosed()} and {@link #hasNext()} return true and false
 	 *         respectively that no further invocations of hasNext() will return true.
 	 */
 	public boolean isClosed();
 	
 	/**
 	 * <p>
 	 * Closes the underlying stream or channel. The semantics of this are not always
 	 * well defined in all contexts; however, at bare minimum, implementers should
 	 * adhere to the general contract that read invocations blocking for any reason
 	 * (and, in particular, reads blocking for stream completion) will return
 	 * following invocation of this function.
 	 * </p>
 	 * 
 	 * <p>
 	 * Implementers may choose to simply relay the "close" invocation to their
 	 * underlying stream or channel, and then nonetheless continue to pump that
 	 * underlying mechanism until it begins to throw exceptions. This behavior avoids
 	 * problems that would otherwise arise in some situations where we must avoid
 	 * losing data in the abyss between buffers if the underlying mechanism is willing
 	 * to return already buffered data even if it has been closed, but has the
 	 * unfortunate side effect that invocations of {@link #isClosed()} are not
 	 * required to return <code>true</code> immediately after <code>close()</code> is
 	 * called.
 	 * </p>
 	 * 
 	 * <p>
 	 * In situations that bear resemblance to pipes (that is, a ReadHead is paired
 	 * with a WriteHead at some other position along the same underlying stream or
 	 * channel; network connections are typically exemplary of this as well as
 	 * in-program pipes), this method may typically be expected to maintain the same
	 * semantics as the general contract of close methods of the underlying type --
	 * namely, that the matching WriteHead (or equivalent) may find its stream or
	 * channel to have become closed as well.
 	 * </p>
 	 * 
 	 * <p>
 	 * Only the first invocation of this function should have any effect; closing a
 	 * closed ReadHead is illogical (but should not throw exceptions).
 	 * </p>
 	 */
 	// i might consider making a boolean parameter to this method for forcefulness.  perhaps there are applications that would actually want to be able to insist on losing data to the abyss between buffers?
 	public void close();
 	
 	/**
 	 * Signals whether or not a read may ever again return data. This method is
 	 * functionally identical to calling <code>(isClosed() && !hasNext())</code>, but
	 * may be more efficient. A return of true does not guarantee that a future read
	 * <i>will</i> return data, only that it <i>may</i>.
 	 * 
	 * @return true if it is possible for a read to return data in the future; false
 	 *         otherwise.
 	 */
 	public boolean isExhausted();
 }
