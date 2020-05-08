 package nachos.threads;
 
 import java.util.LinkedList;
 
 import nachos.machine.Lib;
 import nachos.machine.Machine;
 
 /**
  * A <i>communicator</i> allows threads to synchronously exchange 32-bit
  * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
  * threads can be waiting to <i>listen</i>. But there should never be a time
  * when both a speaker and a listener are waiting, because the two threads can
  * be paired off at this point.
  */
 public class Communicator {
 	/**
 	 * Allocate a new communicator.
 	 */
 	public Communicator() {
 		transferLock = new Lock();
 		listenerQueue = new LinkedList<KThread>();
 		speakerQueue = new LinkedList<KThread>();
 		transfering = false;
 	}
 
 	/**
 	 * Wait for a thread to listen through this communicator, and then transfer
 	 * <i>word</i> to the listener.
 	 * 
 	 * <p>
 	 * Do not return until this thread is paired up with a listening thread.
 	 * Exactly one listener should receive <i>word</i>.
 	 * 
 	 * @param word
 	 *            the integer to transfer.
 	 */
 	public void speak(int word) {
 		// Phase 0: if there is no listener speaker goes to the queue
 		//			otherwise goes to transfer phase
 		boolean intStatus;
 		transferLock.acquire();
 		if (transfering || listenerQueue.isEmpty()) {
 			intStatus = Machine.interrupt().disable();
 			speakerQueue.add(KThread.currentThread());
 			transferLock.release();
 			KThread.sleep();
 			Machine.interrupt().restore(intStatus);
 			transferLock.acquire();
 		}
 		
 		// Phase 1: speaker set the value and wake up listener and fall asleep
 		transfering = true;
 		intStatus = Machine.interrupt().disable();
 		storedWord = word;
 		listenerQueue.removeFirst().ready();
 		transferLock.release();
 		KThread.sleep();
 		Machine.interrupt().restore(intStatus);
 		transferLock.acquire();
 		
 		// Phase 3: was waken up by listener and quit
 		intStatus = Machine.interrupt().disable();
		if (!speakerQueue.isEmpty() && !listenerQueue.isEmpty()) {
 			speakerQueue.peekFirst().ready();
		} else transfering = false;
 		Machine.interrupt().restore(intStatus);
 		transferLock.release();
 		
 		// What the hell was written here !!!!!!!?
 	}
 
 	/**
 	 * Wait for a thread to speak through this communicator, and then return the
 	 * <i>word</i> that thread passed to <tt>speak()</tt>.
 	 * 
 	 * @return the integer transferred.
 	 */
 	public int listen() {
 		int ret = 0;
 		// Phase 0: listener goes to the queue and wake up one speaker if there is
 		transferLock.acquire();
 		boolean intStatus = Machine.interrupt().disable();
 		listenerQueue.add(KThread.currentThread());
 		if (!transfering && !speakerQueue.isEmpty()) {
 			transfering = true;
 			speakerQueue.peekFirst().ready();
 		}
 		transferLock.release();
 		KThread.sleep();
 		Machine.interrupt().restore(intStatus);
 		transferLock.acquire();
 		
 		// Phase 1: listener is waken up by speaker and retrieve the value
 		//			then wake up speaker and quit
 		ret = storedWord;
 		intStatus = Machine.interrupt().disable();
 		speakerQueue.removeFirst().ready();
 		Machine.interrupt().restore(intStatus);
 		transferLock.release();
 		return ret;
 	}
 	
 	private boolean transfering;
 	private int storedWord;
 	private LinkedList<KThread> listenerQueue, speakerQueue;
 	private Lock transferLock;
 }
