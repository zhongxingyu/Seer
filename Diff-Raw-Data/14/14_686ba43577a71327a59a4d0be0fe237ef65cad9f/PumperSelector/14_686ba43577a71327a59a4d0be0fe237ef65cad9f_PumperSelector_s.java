 package ahs.util.thread;
 
 import ahs.util.*;
 
 import java.io.*;
 import java.nio.channels.*;
 import java.util.*;
 
 /**
  * If using this with a ReadHead that is backed by NIO channels, you'll probably be
  * handing a reference to this pumper to the ReadHead; ReadHead don't typically expose the
  * details of their base enough for this class to do its job that way.
  * 
  * @author hash
  * 
  */
 public class PumperSelector implements Pumper {
 	public PumperSelector() {
 		Selector $bees = null;
 		try {
 			$bees = Selector.open();
 		} catch (IOException $e) {
 			X.cry($e);
 		}
 		$selector = $bees;
 	}
 	
 	private final Selector $selector; 
 	
 	/**
	 * Starts the pump in a brand new thread of its own.
 	 */
 	public synchronized void start() {
		new Thread(this, "SelectorPumper").start();
 	}
 	
 	/** {@inheritDoc} */
 	public void run() {
 		while (true) {
 			try {
 				// block for events
 				//   but still wake up every once and a while so that the sync on selector's internals loosens up enough to let registrations of new channels get through
 				//     (obscenely, $selector.wakeup() is of no use to avoid this periodicy, since it only helps if you call it -after- the register function, but the register function -blocks- until the wakeup.)
 				//SOMEDAY: i can fix this
 				//   have an event queue that registration functions push and event to... and by queue i mean Pipe -- that beautiful concurrent one i just made
 				//   then have the listener on the pipe call wakeup on the selector
 				//   and then have this loop readAllNow on the pipe and act as appropriate
 				$selector.select(100);
 			} catch (IOException $e) {
 				X.cry($e);
 			}
 			
 			// Get list of selection keys with pending events
 			Iterator<SelectionKey> $itr = $selector.selectedKeys().iterator();
 			
 			// Process each key
 			while ($itr.hasNext()) {
 				SelectionKey $k = $itr.next();
 				$itr.remove();
 				
 				if (!$k.isValid()) continue;
 				
 				((Pump)$k.attachment()).run(Integer.MAX_VALUE);	// the pump is told to get as much as it can, but with the expectation that it will return much sooner when the channel runs out of immediately available data.
 			}
 		}
 	}
 	
 	public void register(SelectableChannel $ch, Pump $p) {
 		if ($p == null) throw new NullPointerException("pump cannot be null");
 		try {
 			$selector.wakeup();	// this has probabilistic (and unlikely at that) success, but can't really hurt us, so... feh. 
 			$ch.register($selector, SelectionKey.OP_READ, $p);	// we basically can't use write.  it makes for spin.
 		} catch (ClosedChannelException $e) {
 			throw new IllegalStateException($e);
 		}
 	}
 	
 	public void register(ServerSocketChannel $ch, Pump $p) {
 		if ($p == null) throw new NullPointerException("pump cannot be null");
 		try {
 			$selector.wakeup();	// this has probabilistic (and unlikely at that) success, but can't really hurt us, so... feh.
 			$ch.register($selector, SelectionKey.OP_ACCEPT, $p);
 		} catch (ClosedChannelException $e) {
 			throw new IllegalStateException($e);
 		}
 	}
 	
 	public void deregister(Pump $p) {
 		for (Iterator<SelectionKey> $itr = $selector.keys().iterator(); $itr.hasNext();) {
 			SelectionKey $k = $itr.next();
 			if ($p == $k.attachment()) $itr.remove();
 		}
 	}
 	
 	public void deregister(SelectableChannel $ch) {
 		for (Iterator<SelectionKey> $itr = $selector.keys().iterator(); $itr.hasNext();) {
 			SelectionKey $k = $itr.next();
 			if ($ch == $k.channel()) $itr.remove();
 		}
 	}
 		
 }
