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
 
 package us.exultant.ahs.io;
 
 import us.exultant.ahs.core.*;
 import us.exultant.ahs.util.*;
 import us.exultant.ahs.anno.*;
 import us.exultant.ahs.thread.*;
 import us.exultant.ahs.thread.Pipe;
 import java.io.*;
 import java.nio.channels.*;
 import java.util.*;
 import org.slf4j.*;
 
 /**
  * <p>
  * This class provides a comprehensive wrapper (more of an asylum, really) around
  * {@link Selector java.nio.channels.Selector} that simplifies common operation and
  * provides efficient, thread-safe controls that the java Selector fails to provide. Using
  * this class, it becomes possible to register and deregister selectable channels from any
  * thread at any time safely, and the entire system conforms to the {@link WorkTarget}
  * system so it can work in the same pools and with the same tools as any systems based on
  * the AHSlib threading module.
  * </p>
  * 
  * <p>
  * There is a default global singleton instance of this available from
  * {@link IOManager#getDefaultSelectionSignaller()} that should be used in almost every
  * situation.
  * </p>
  * 
  * <p>
  * If you choose not to use the default global instance of this selector and instead
  * construct and schedule your own, please note that this WorkTarget is not so ammenable
  * to pooling as one might like; unfortunately, there were design choices made in the core
  * of the Java standard libraries that leave us with a lot of limitations. In particular,
  * we have no way of getting events that the selector itself has events ready other than
  * full-out checking it &mdash; so, we're stuck with an {@link Worker#isReady()} method
  * that helplessly always returns true, and fundamentally no way to disbatch events
  * relating to the core selector's readiness. There are two ways to deal with this: you
  * may actually just run this system in its own personal thread (creating a
  * {@link WorkSchedulerFlexiblePriority} instance with a thread pool of size one is a
  * reasonable way to do this); otherwise, if you do wish to keep it in the same Scheduler
  * as other tasks, you should be sure to construct this selector with purely nonblocking
  * mode (negative selection timeout parameter) and fixed-delay scheduling parameters (i.e.
  * {@link ScheduleParams#makeFixedDelay(long)}).
  * </p>
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  */
 // in a perfect world, i'd like to be able to treat connection acceptance or whathaveyou has a separate priority than readability or whathaveyou.
 //   unfortunately the only way i can see to do that is by having completely separate selectors and taking it upon yourself to register things to them by category.  and eat your moar threads.
 public class SelectionSignaller {
 	/**
 	 * Creates a new system default Selector back-end. Selects run with a timeout of 1
 	 * millisecond; the WorkTarget's priority is zero.
 	 * 
 	 * This default timeout is a conservative choice: regardless of if planning to run
 	 * the WorkTargetSelector in a private thread or a WorkScheduler with pooling, the
 	 * 1 millisecond timeout won't kill you (it'll never leave a thread spinning at
 	 * 100% of a core, nor will it completely choke up a pool), but for a most optimal
 	 * system you might wish to consider other settings.
 	 */
 	public SelectionSignaller() {
 		this(1);
 	}
 	
 	/**
 	 * Creates a new system default Selector back-end. Selects run with a configurable
 	 * timeout; the WorkTarget's priority is zero.
 	 * 
 	 * @param $selectionTimeout
 	 *                the number of milliseconds a select call should block for, or
 	 *                negative for completely nonblocking operation, or zero for
 	 *                blocking without timeout. If this WorkTarget will be run in its
 	 *                own personal thread, you may set this timeout to be arbitrarily
 	 *                high.
 	 */
 	public SelectionSignaller(int $selectionTimeout) {
 		this(makeDefaultSelector(), $selectionTimeout, 0);
 	}
 	
 	/**
 	 * Creates a new system default Selector back-end. Select timeouts are
 	 * configurable, as is the WorkTarget's priority.
 	 * 
 	 * @param $selectionTimeout
 	 *                the number of milliseconds a select call should block for, or
 	 *                negative for completely nonblocking operation, or zero for
 	 *                blocking without timeout. If this WorkTarget will be run in its
 	 *                own personal thread, you may set this timeout to be arbitrarily
 	 *                high.
 	 * @param $workPriority
 	 */
 	public SelectionSignaller(int $selectionTimeout, int $workPriority) {
 		this(makeDefaultSelector(), $selectionTimeout, $workPriority);
 	}
 	
 	private static Selector makeDefaultSelector() {
 		try {
 			return Selector.open();
 		} catch (IOException $e) {
 			throw new Error("you have a seriously weird runtime.", $e);
 		}
 	}
 	
 	/**
 	 * Constructs a new WorkTargetSelector with your choice of back-end Selector,
 	 * timeouts on selects, and priority for the WorkTarget.
 	 */
 	public SelectionSignaller(Selector $selectr, int $selectionTimeout, int $workPriority) {
 		$selector = $selectr;
 		$priority = $workPriority;
 		$timeout = $selectionTimeout;
 		$pipe = new DataPipe<Event>();
 		$pipe.source().setListener(new Listener<ReadHead<Event>>() {
 			public void hear(ReadHead<Event> $eh) {
 				$selector.wakeup();
 			}
 		});
 	}
 	
 	public WorkFuture<Void> schedule(WorkScheduler $scheduler, ScheduleParams $when) {
 		return $scheduler.schedule(new Worker(), $when);
 	}
 
 	public static final Loggar logger = new Loggar(LoggerFactory.getLogger(SelectionSignaller.class.getName()));
 	public static final Loggar logger_ingress = new Loggar(LoggerFactory.getLogger(SelectionSignaller.class.getName()+".ingress"));
 	
 	private final Selector		$selector;
 	private final Pipe<Event>	$pipe;
 	private final int		$priority;
 	private final int		$timeout;
 	
 	private SelectionKey getKey(Event $evt) {
 		if ($evt.$chan != null) return $evt.$chan.keyFor($selector);
 		for (SelectionKey $k : $selector.keys()) {
 			Attache $a = (Attache) $k.attachment();
 			if ($a == null) continue;
 			if ($a.contains($evt.$listener)) return $k;
 		}
 		return null;
 	}
 	
 	/**
 	 * Registers a Listener to be triggered by the selecting thread when this channel
 	 * has readable data. If called on a channel that already has a listener set for
 	 * this purpose, this new listener will replace that listener.
 	 * 
 	 * @param $ch
 	 *                a readable channel. Note this this is a contractual thing rather
 	 *                than a strongly typed thing; this is unfortunate, but
 	 *                unavoidable due to the inheritance hierarchies in the java NIO
 	 *                package.
 	 * @param $p
 	 *                a Listener to notify when the channel has data
 	 */
 	@Idempotent
 	@ThreadSafe
 	public void registerRead(SelectableChannel $ch, Listener<SelectableChannel> $p) {
 		assert logger_ingress.debug("queuing registerRead on {channel:{}; listener:{}}", $ch, $p);
 		$pipe.sink().write(new Event_Reg($ch, $p, SelectionKey.OP_READ));
 	}
 	
 	/**
 	 * Registers a Listener to be triggered by the selecting thread when this channel
 	 * is ready to accept data writes. If called on a channel that already has a
 	 * listener set for this purpose, this new listener will replace that listener.
 	 * 
 	 * @param $ch
 	 *                a writable channel. Note this this is a contractual thing rather
 	 *                than a strongly typed thing; this is unfortunate, but
 	 *                unavoidable due to the inheritance hierarchies in the java NIO
 	 *                package.
 	 * @param $p
 	 *                a Listener to notify when the channel can accept data
 	 */
 	@Idempotent
 	@ThreadSafe
 	public void registerWrite(SelectableChannel $ch, Listener<SelectableChannel> $p) {
 		assert logger_ingress.debug("queuing registerWrite on {channel:{}; listener:{}}", $ch, $p);
 		$pipe.sink().write(new Event_Reg($ch, $p, SelectionKey.OP_WRITE));
 	}
 	
 	/**
 	 * Registers a Listener to be triggered by the selecting thread when this
 	 * ServerSocketChannel has new connections ready to accept. If called on a channel
 	 * that already has a listener set for this purpose, this new listener will
 	 * replace that listener.
 	 * 
 	 * @param $ch
 	 *                a ServerSocketChannel channel
 	 * @param $p
 	 *                a Listener to notify when connections are ready to be accepted
 	 */
 	@Idempotent
 	@ThreadSafe
 	public void registerAccept(ServerSocketChannel $ch, Listener<SelectableChannel> $p) {
 		assert logger_ingress.debug("queuing registerAccept on {channel:{}; listener:{}}", $ch, $p);
 		$pipe.sink().write(new Event_Reg($ch, $p, SelectionKey.OP_ACCEPT));
 	}
 	
 	/**
 	 * Stops selecting for read events on a channel and discards the presently set
 	 * Listener. Calling this method repeatedly will have no effect unless
 	 * {@link #registerWrite(SelectableChannel, Listener)} is called in the meanwhile.
 	 */
 	@Idempotent
 	@ThreadSafe
 	public void deregisterRead(SelectableChannel $ch) {
 		assert logger_ingress.debug("queuing deregisterRead on {channel:{}}", $ch);
 		$pipe.sink().write(new Event_Dereg($ch, SelectionKey.OP_READ));
 	}
 	
 	/**
 	 * Stops selecting for writability events on a channel and discards the presently
 	 * set Listener. Calling this method repeatedly will have no effect unless
 	 * {@link #registerWrite(SelectableChannel, Listener)} is called in the meanwhile.
 	 */
 	@Idempotent
 	@ThreadSafe
 	public void deregisterWrite(SelectableChannel $ch) {
 		assert logger_ingress.debug("queuing deregisterWrite on {channel:{}}", $ch);
 		$pipe.sink().write(new Event_Dereg($ch, SelectionKey.OP_WRITE));
 	}
 	
 	/**
 	 * Using this form of the command is slower and will behave ambiguously if the
 	 * same pump object is attached to several channels. Consider using
 	 * {@link #deregisterRead(SelectableChannel)} instead, which is both unambiguous
 	 * and more efficient.
 	 */
 	@Idempotent
 	@ThreadSafe
 	public void deregisterRead(Listener<SelectableChannel> $p) {
 		assert logger_ingress.debug("queuing deregisterRead on {listener:{}}", $p);
 		$pipe.sink().write(new Event_Dereg($p, SelectionKey.OP_READ));
 	}
 	
 	/**
 	 * Using this form of the command is slower and will behave ambiguously if the
 	 * same pump object is attached to several channels. Consider using
 	 * {@link #deregisterWrite(SelectableChannel)} instead if possible, which is both
 	 * unambiguous and more efficient.
 	 */
 	@Idempotent
 	@ThreadSafe
 	public void deregisterWrite(Listener<SelectableChannel> $p) {
 		assert logger_ingress.debug("queuing deregisterWrite on {listener:{}}", $p);
 		$pipe.sink().write(new Event_Dereg($p, SelectionKey.OP_WRITE));
 	}
 	
 	/**
 	 * Stops selecting for new connection availablity events on a server socket and
 	 * discards the presently set Listener. Calling this method repeatedly will have
 	 * no effect unless {@link #registerAccept(ServerSocketChannel, Listener)} is
 	 * called in the meanwhile.
 	 */
 	@Idempotent
 	@ThreadSafe
 	public void deregisterAccept(ServerSocketChannel $ch) {
 		assert logger_ingress.debug("queuing deregisterAccept on {channel:{}}", $ch);
 		$pipe.sink().write(new Event_Dereg($ch, SelectionKey.OP_ACCEPT));
 	}
 	
 	/**
 	 * Stops selecting for any and all events on a channel, discards all present
 	 * Listeners, and cancels it. This channel may never again be registered with this
 	 * Selector. Repeated calls of this method have no effect.
 	 */
 	@Idempotent
 	@ThreadSafe
 	public void cancel(SelectableChannel $ch) {
 		assert logger_ingress.debug("queuing cancel on {channel:{}}", $ch);
 		$pipe.sink().write(new Event_Cancel($ch));
 	}
 	
 	
 	
 	private static abstract class Event {
 		protected Event(SelectableChannel $thing, Listener<SelectableChannel> $listener, int $ops) {
 			this.$chan = $thing;
 			this.$listener = $listener;
 			this.$ops = $ops;
 		}
 		
 		public final SelectableChannel			$chan;
 		public final Listener<SelectableChannel>	$listener;
 		public final int				$ops;
 	}
 	private static class Event_Reg extends Event {
 		private Event_Reg(SelectableChannel $ch, Listener<SelectableChannel> $p, int $ops) {
 			super($ch, $p, $ops);
 			if ($p == null) throw new NullPointerException("listener cannot be null");
 		}
 	}
 	private static class Event_Dereg extends Event {
 		private Event_Dereg(Listener<SelectableChannel> $p, int $ops) {
 			super(null, $p, $ops);
 		}
 		
 		private Event_Dereg(SelectableChannel $ch, int $ops) {
 			super($ch, null, $ops);
 		}
 	}
 	private static class Event_Cancel extends Event {
 		private Event_Cancel(SelectableChannel $ch) {
 			super($ch, null, 0);
 		}
 		
 		private Event_Cancel(Listener<SelectableChannel> $p) {
 			super(null, $p, 0);
 		}
 	}
 	
 	
 	
 	private class Worker implements WorkTarget<Void> {
 		/**
 		 * This always returns true. I'm sorry. The core java api for {@link Selector}
 		 * leaves me no choice &mdash; it provides no method for inquiring about readiness
 		 * without actually performing a select.
 		 */
 		public boolean isReady() {
 			return true;
 			//try {
 			//	return 
 			//		$selector.selectedKeys().isEmpty() ||		// i don't think this works, actually, because you have to call select() in order to get that key set to be updated by the OS.  Also: mind that if you DID use selectNow here, you'd have to change the $freshWorkExists boolean in the call method, because that optimization would no longer be valid. 
 			//		$pipe.hasNext();
 			//} catch (ClosedSelectorException $e) {
 			//	return false;
 			//}
 		}
 		
 		public boolean isDone() {
 			return !$selector.isOpen();
 		}
 		
 		public int getPriority() {
 			return $priority;
 		}
 		
 		/**
 		 * <p>
 		 * This method cycles through three phases. In the first phase it blocks on the
 		 * selector. In the second phase it calls the listeners for every channel that
 		 * reported an event. In the third phase, the internal event buffer used to
 		 * synchronize registration and deregistration requests from other threads is
 		 * checked, and any buffered events are processed.
 		 * </p>
 		 */
 		public Void call() {
 			// PHASE ONE
 			// check for new registration or deregistration events
 			assert logger.debug("selector doing registration processing...");
 			callRegistrationProcessing();
 			assert logger.debug("registration processing done");
 			
 			// PHASE TWO
 			// chill out
 			assert logger.debug("selector selecting...");
			long $start = X.time();
 			boolean $freshWorkExists = callSelect() > 0;
			if (!$freshWorkExists && Thread.interrupted()) {
				if (X.time() - $start > 7000)
					{ X.sayet("yes, it's as bad as you feared!"); System.exit(20); }
				else
					return null;
			}
 			assert logger.debug("selector wake, workExists:{}", $freshWorkExists);
 			
 			// PHASE... TWO AND A HALF?
 			// if we were a blocking selector, we might have been woken up specifically to deal with a new event, so we should do so asap
 			//callRegistrationProcessing();
 			// actually no, this is silly.  if we're being used in blocking mode, we're probably also being used in our own thread, so we'll be looping back to phase one momentarily anyway and it's all kay.
 			
 			// PHASE THREE
 			// disbatch events to folks who're deserving
 			assert logger.debug("selector disbatching events...");
 			if ($freshWorkExists) callDisbatchEvents();
 			assert logger.debug("event disbatching done");
 			
 			return null;
 		}
 		
 		private int callSelect() {
 			try {
 				/* block until channel events, or wakeups triggered by the event pipe's listener, or thread interrupts. */
 				if ($timeout < 0) return $selector.selectNow();
				return $selector.select(10000);
 			} catch (ClosedSelectorException $e) {
 				/* selectors can't be closed except by their close method, which we control all access to, so this shouldn't happen in a way that surprises us. */
 				throw new MajorBug($e);
 			} catch (IOException $e) {
 				/* I just plain don't know what would cause this. */
 				throw new Error($e);
 			}
 		}
 		
 		private void callDisbatchEvents() {
 			Iterator<SelectionKey> $itr = $selector.selectedKeys().iterator();
 			while ($itr.hasNext()) {
 				SelectionKey $k = $itr.next();
 				$itr.remove();
 				
 				if (!$k.isValid()) continue;
 				
 				int $ops = $k.readyOps();
 				assert logger.trace("dispatching ops:{} on channel:{}", $ops, $k.channel());
 				Attache $a = (Attache) $k.attachment();
 				if (Primitives.containsFullMask($ops, SelectionKey.OP_CONNECT)) try { if (((SocketChannel)$k.channel()).finishConnect()) $k.interestOps(Primitives.removeMask($k.interestOps(), SelectionKey.OP_CONNECT)); } catch (IOException $e) { $k.cancel(); }
 				if (Primitives.containsFullMask($ops, SelectionKey.OP_READ) && $a.$reader != null) $a.$reader.hear($k.channel());
 				if (Primitives.containsFullMask($ops, SelectionKey.OP_WRITE) && $a.$writer != null) $a.$writer.hear($k.channel());
 				if (Primitives.containsFullMask($ops, SelectionKey.OP_ACCEPT) && $a.$accepter != null) $a.$accepter.hear($k.channel());
 			}
 		}
 		
 		private void callRegistrationProcessing() {
 			List<Event> $evts = $pipe.source().readAllNow();
 			for (Event $evt : $evts) {
 				if ($evt instanceof Event_Reg) {
 					assert logger.trace("registering type "+$evt.$ops+" on {channel:{}; listener:{}}", $evt.$chan, $evt.$listener);
 					SelectionKey $k = $evt.$chan.keyFor($selector);
 					Attache $a;
 					int $old_ops;
 					if ($k == null) {
 						$a = new Attache();
 						$old_ops = 0;
 					} else {
 						$a = (Attache) $k.attachment();
 						$old_ops = $k.interestOps();
 					}
 					$a.apply($evt);
 					// there miiiiight be some potential optimizations here for detecting when we don't actually need to change the op set.  in fact i'd say there definitely are.  well, on third thought, dunno, it might not be that big of a deal as long as we're assuming that the channel doesn't have tons of selectable keys, because deep down it's O(n)'ing on that, not the selector's whole key set.
 					try {
 						$evt.$chan.register($selector, Primitives.addMask($old_ops, $evt.$ops), $a);
 					} catch (ClosedChannelException $e) {
 						/* there's nothing we can really do to "recover" here; we just... don't do anything because there's nothing to do. */
 					}
 				} else if ($evt instanceof Event_Dereg) {
 					assert logger.trace("deregistering type "+$evt.$ops+" on {channel:{}; listener:{}}", $evt.$chan, $evt.$listener);
 					SelectionKey $k = getKey($evt);
 					$k.interestOps(Primitives.removeMask($k.interestOps(), $evt.$ops));
 					Attache $a = (Attache) $k.attachment();
 					if (Primitives.containsFullMask($evt.$ops, SelectionKey.OP_READ) && $a.$reader != null) $a.$reader = null; // gc help
 					if (Primitives.containsFullMask($evt.$ops, SelectionKey.OP_WRITE) && $a.$writer != null) $a.$writer = null; // gc help
 					if (Primitives.containsFullMask($evt.$ops, SelectionKey.OP_ACCEPT) && $a.$accepter != null) $a.$accepter = null; // gc help
 				} else if ($evt instanceof Event_Cancel) {
 					assert logger.trace("cancelling on {channel:{}; listener:{}}", $evt.$chan, $evt.$listener);
 					SelectionKey $k = getKey($evt);
 					if ($k != null) $k.cancel();
 					$k.attach(null); // gc help
 				}
 			}
 		}
 	}
 	
 	
 	
 	private class Attache {
 		public Listener<SelectableChannel>	$reader;
 		public Listener<SelectableChannel>	$writer;
 		public Listener<SelectableChannel>	$accepter;
 		
 		public void apply(Event $evt) {
 			switch ($evt.$ops) {
 				case SelectionKey.OP_READ:
 					$reader = $evt.$listener;
 					break;
 				case SelectionKey.OP_WRITE:
 					$writer = $evt.$listener;
 					break;
 				case SelectionKey.OP_ACCEPT:
 					$accepter = $evt.$listener;
 					break;
 				default:
 					throw new MajorBug("op type not supported");
 			}
 		}
 		
 		public boolean contains(Listener<SelectableChannel> $p) {
 			return ($reader == $p) || ($writer == $p) || ($accepter == $p);
 		}
 	}
 }
