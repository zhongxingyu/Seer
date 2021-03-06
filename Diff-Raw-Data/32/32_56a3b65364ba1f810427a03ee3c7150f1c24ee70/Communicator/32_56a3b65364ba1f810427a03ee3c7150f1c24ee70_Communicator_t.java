 package nachos.threads;
 
 import nachos.machine.*;
 
 /**
  * A <i>communicator</i> allows threads to synchronously exchange 32-bit
  * messages. Multiple threads can be waiting to <i>speak</i>,
  * and multiple threads can be waiting to <i>listen</i>. But there should never
  * be a time when both a speaker and a listener are waiting, because the two
  * threads can be paired off at this point.
  */
 public class Communicator {
 	Lock lock;
 	Condition2 waitingReceivers;
 	Condition2 waitingSenders;
 	Condition2 waitingLiveReceiver;
 	KThread liveSender;
 	KThread liveReceiver;
 	int value;
     /**
      * Allocate a new communicator.
      */
     public Communicator() {
     	lock = new Lock();
     	waitingReceivers = new Condition2(lock);
     	waitingSenders = new Condition2(lock);
     	waitingLiveReceiver = new Condition2(lock);
     	liveReceiver = null;
     	value = 0;
     }
 
     /**
      * Wait for a thread to listen through this communicator, and then transfer
      * <i>word</i> to the listener.
      *
      * <p>
      * Does not return until this thread is paired up with a listening thread.
      * Exactly one listener should receive <i>word</i>.
      *
      * @param	word	the integer to transfer.
      */
     public void speak(int word) {
     	lock.acquire();
     	//While another thread has loaded his parameter and
     	//is waiting for a receiver to return it then
     	//put this sender on the waitQueue
    	while(liveReceiver == null) {
     		waitingSenders.sleep();
     	}
     	/** There is no thread waiting to send this value
     	 *  to a receiver and so this thread loads its value 
     	 *  and becomes the next thread to send */
    	/*
     	while(liveReceiver == null) {
     		waitingReceivers.wake();
     		waitingSenders.sleep();
     	}
    	*/
    	value = word;
     	
    	waitingLiveReceiver.wake();
     	lock.release();
     }
 
     /**
      * Wait for a thread to speak through this communicator, and then return
      * the <i>word</i> that thread passed to <tt>speak()</tt>.
      *
      * @return	the integer transferred.
      */    
     public int listen() {
     	lock.acquire();
     	while(liveReceiver != null){
     		waitingReceivers.sleep();
     	}
     	liveReceiver = KThread.currentThread();
    	
    	waitingSenders.wake();
    	waitingLiveReceiver.sleep();
     		
     	waitingSenders.wake();
     	waitingReceivers.wake();
 
    	result = value;
    	liveReceiver = null;
     	lock.release();
    	
     	return result;
     }
 }
