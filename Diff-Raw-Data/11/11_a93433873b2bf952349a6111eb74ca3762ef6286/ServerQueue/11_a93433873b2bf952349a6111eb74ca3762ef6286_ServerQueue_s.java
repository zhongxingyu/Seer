 package controller;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ArrayBlockingQueue;
 
 /**
  * ServerQueue.java
  * 
  * @author			Devin Barry
  * @date			25.10.2012
  * @lastModified	28.10.2012
  * 
  * A very simply wait / notify queue implementation used to
  * pass elements of type E from different socket threads between
  * themselves. This class allows consumer threads to sleep while
  * waiting for producer threads to put an item into the queue.
  *
  */
 public class ServerQueue<E> {
 	
 	BlockingQueue<E> q;
 	
 	public ServerQueue() {
 		q = new ArrayBlockingQueue<E>(10);
 	}
 
 	/**
 	 * This get methods get an object from the Queue
 	 * 
 	 * Here we synchronize over this ServerQueue object
 	 * and therefore can call wait and notify
 	 * 
 	 * @return
 	 */
	public synchronized E get() {
 		E e = null;
 		try {
 			e = q.take();
 		} catch (InterruptedException ie) {
 			System.out.println("InterruptedException caught");
 		}
		//System.out.println("Got: " + e);
		notifyAll(); //notify all sleeping threads that something has been removed
 		return e;
 	}
 
 	/**
 	 * This put method puts an object into the Queue.
 	 * 
 	 * Here we synchronize over this ServerQueue object
 	 * and therefore can call wait and notify.
 	 * 
 	 * @param e
 	 */
	public synchronized void put(E e) {
 		try {
 			//if it is full, sleep thread
 			q.put(e);
 		} catch (InterruptedException ie) {
 			System.out.println("InterruptedException caught");
 		}
 		//System.out.println("Put: " + e);
		notifyAll(); //notify all sleeping threads that something new is in queue
 	}
 
 }
