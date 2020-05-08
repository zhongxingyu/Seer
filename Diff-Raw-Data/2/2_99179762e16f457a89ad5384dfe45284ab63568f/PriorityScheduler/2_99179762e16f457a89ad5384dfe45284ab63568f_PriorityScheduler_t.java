 package nachos.threads;
 
 import nachos.machine.*;
 
 import java.util.TreeSet;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import java.util.Comparator;
 
 /**
  * A scheduler that chooses threads based on their priorities.
  *
  * <p>
  * A priority scheduler associates a priority with each thread. The next thread
  * to be dequeued is always a thread with priority no less than any other
  * waiting thread's priority. Like a round-robin scheduler, the thread that is
  * dequeued is, among all the threads of the same (highest) priority, the
  * thread that has been waiting longest.
  *
  * <p>
  * Essentially, a priority scheduler gives access in a round-robin fassion to
  * all the highest-priority threads, and ignores all other threads. This has
  * the potential to
  * starve a thread if there's always a thread waiting with higher priority.
  *
  * <p>
  * A priority scheduler must partially solve the priority inversion problem; in
  * particular, priority must be donated through locks, and through joins.
  */
 public class PriorityScheduler extends Scheduler {
     /**
      * Allocate a new priority scheduler.
      */
     public PriorityScheduler() {
     }
     
     /**
      * Allocate a new priority thread queue.
      *
      * @param	transferPriority	<tt>true</tt> if this queue should
      *					transfer priority from waiting threads
      *					to the owning thread.
      * @return	a new priority thread queue.
      */
     public ThreadQueue newThreadQueue(boolean transferPriority) {
 	return new PriorityQueue(transferPriority);
     }
 
     public int getPriority(KThread thread) {
 	Lib.assertTrue(Machine.interrupt().disabled());
 		       
 	return getThreadState(thread).getPriority();
     }
 
     public int getEffectivePriority(KThread thread) {
 	Lib.assertTrue(Machine.interrupt().disabled());
 		       
 	return getThreadState(thread).getEffectivePriority();
     }
 
     public void setPriority(KThread thread, int priority) {
 	Lib.assertTrue(Machine.interrupt().disabled());
 		       
 	Lib.assertTrue(priority >= priorityMinimum &&
 		   priority <= priorityMaximum);
 	
 	getThreadState(thread).setPriority(priority);
     }
 
     public boolean increasePriority() {
 	boolean intStatus = Machine.interrupt().disable();
 		       
 	KThread thread = KThread.currentThread();
 
 	int priority = getPriority(thread);
 	if (priority == priorityMaximum)
 	    return false;
 
 	setPriority(thread, priority+1);
 
 	Machine.interrupt().restore(intStatus);
 	return true;
     }
 
     public boolean decreasePriority() {
 	boolean intStatus = Machine.interrupt().disable();
 		       
 	KThread thread = KThread.currentThread();
 
 	int priority = getPriority(thread);
 	if (priority == priorityMinimum)
 	    return false;
 
 	setPriority(thread, priority-1);
 
 	Machine.interrupt().restore(intStatus);
 	return true;
     }
 
     /**
      * The default priority for a new thread. Do not change this value.
      */
     public static final int priorityDefault = 1;
     /**
      * The minimum priority that a thread can have. Do not change this value.
      */
     public static final int priorityMinimum = 0;
     /**
      * The maximum priority that a thread can have. Do not change this value.
      */
     public static final int priorityMaximum = 7;    
 
     /**
      * Return the scheduling state of the specified thread.
      *
      * @param	thread	the thread whose scheduling state to return.
      * @return	the scheduling state of the specified thread.
      */
     protected ThreadState getThreadState(KThread thread) {
 	if (thread.schedulingState == null)
 	    thread.schedulingState = new ThreadState(thread);
 
 	return (ThreadState) thread.schedulingState;
     }
 
     /**
      * A <tt>ThreadQueue</tt> that sorts threads by priority.
      */
     protected class PriorityQueue extends ThreadQueue {
 	
 		PriorityQueue(boolean transferPriority) {
 		    this.transferPriority = transferPriority;
 		}
 
 		public void waitForAccess(KThread thread) {
 		    Lib.assertTrue(Machine.interrupt().disabled());
 		    getThreadState(thread).waitForAccess(this); //threadState.waitForAcess(me who is queue)
 		}
 
 		public void acquire(KThread thread) {
 		    Lib.assertTrue(Machine.interrupt().disabled());
 		    getThreadState(thread).acquire(this);
 		}
 
 		public KThread nextThread() {
 		    Lib.assertTrue(Machine.interrupt().disabled());
 		    Lib.assertTrue(!waitQueue.isEmpty(), "Getting thread from next waitQueue");
 		    KThread nextThread = waitQueue.poll().thread;
 		    acquire(nextThread);
 		    return this.lockHolder.thread;
 		}
 
 		/**
 		 * Return the next thread that <tt>nextThread()</tt> would return,
 		 * without modifying the state of this queue.
 		 *
 		 * @return	the next thread that <tt>nextThread()</tt> would
 		 *		return.
 		 */
 		protected ThreadState pickNextThread() {
 			return waitQueue.peek();
 		}
 		
 		public void print() {
 		    Lib.assertTrue(Machine.interrupt().disabled());
 		    // implement me (if you want)
 		}
 
 		/**
 		 * <tt>true</tt> if this queue should transfer priority from waiting
 		 * threads to the owning thread.
 		 */
 		public boolean transferPriority;
 		private java.util.PriorityQueue<ThreadState> waitQueue = new java.util.PriorityQueue<ThreadState>(10, new ThreadComparator<ThreadState>());
 		private ThreadState lockHolder = null;
 		
 		private class ThreadComparator<T> implements Comparator<T> {
 			@Override
 			public int compare(T o1, T o2) {
 				ThreadState t1 = (ThreadState) o1;
 				ThreadState t2 = (ThreadState) o2;
 				if (t1.getEffectivePriority() > t2.getEffectivePriority()) {
 					return 1;
 				} else if (t1.getEffectivePriority() < t2.getEffectivePriority()) {
 					return -1;
 				} else {
 					Lib.assertTrue(t1.getWaitingQueue() != t2.getWaitingQueue(), "Comparing times for threads with different wait");
 					if (t1.getWaitingTime() > t2.getWaitingTime()) {
 						return 1;
 					} else {
 						return -1;
 					}
 				}
 			}
 		}
 	}
 
     /**
      * The scheduling state of a thread. This should include the thread's
      * priority, its effective priority, any objects it owns, and the queue
      * it's waiting for, if any.
      *
      * @see	nachos.threads.KThread#schedulingState
      */
     protected class ThreadState {
 		/**
 		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
 		 * specified thread.
 		 *
 		 * @param	thread	the thread this state belongs to.
 		 */
 		public ThreadState(KThread thread) {
 		    this.thread = thread;
		    this.acquired = new HashSet<PriorityQueue>();
 		    setPriority(priorityDefault);
 		}
 
 		//getter methods for the comparator
 		public long getWaitingTime() {
 			return this.waitingTime;
 		}
 
 		public Object getWaitingQueue() {
 			return this.waitingQueue;
 		}
 
 		/**
 		 * Return the priority of the associated thread.
 		 *
 		 * @return	the priority of the associated thread.
 		 */
 		public int getPriority() {
 		    return priority;
 		}
 
 		/**
 		 * Return the effective priority of the associated thread.
 		 *
 		 * @return	the effective priority of the associated thread.
 		 */
 		public int getEffectivePriority() {
 		    return effectivePriority;
 		}
 		
 		private void updateEffectivePriority() {
 			int tempPriority = this.effectivePriority;
 			for (PriorityQueue resource: acquired){
 				int resourceMax = resource.waitQueue.peek().getEffectivePriority();
 				if (tempPriority < resourceMax) {
 					tempPriority = resourceMax;
 				}
 			}
 			if (tempPriority != this.effectivePriority){
 				this.effectivePriority = tempPriority;
 				this.waitingQueue.lockHolder.updateEffectivePriority();
 			}
 		}
 		
 		/**
 		 * Set the priority of the associated thread to the specified value.
 		 *
 		 * @param	priority	the new priority.
 		 */
 		public void setPriority(int priority) {
 		    if (this.priority == priority)
 			return;
 		    
 		    this.priority = priority;
 		    
 		    if(waitingQueue != null && waitingQueue.lockHolder != null) {
 		    	if (waitingQueue.transferPriority){
 		    		waitingQueue.lockHolder.updateEffectivePriority();
 		    	}
 		    }
 		}
 
 		
 		/**
 		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
 		 * the associated thread) is invoked on the specified priority queue.
 		 * The associated thread is therefore waiting for access to the
 		 * resource guarded by <tt>waitQueue</tt>. This method is only called
 		 * if the associated thread cannot immediately obtain access.
 		 *
 		 * @param	waitQueue	the queue that the associated thread is
 		 *				now waiting on.
 		 *
 		 * @see	nachos.threads.ThreadQueue#waitForAccess
 		 */
 		public void waitForAccess(PriorityQueue waitQueue) {
 		    this.waitingQueue = waitQueue;
 		    this.waitingTime = Machine.timer().getTime();
 		    waitingQueue.waitQueue.add(this);
 		    if (waitingQueue.lockHolder != null && waitingQueue.transferPriority){
 		    	waitingQueue.lockHolder.updateEffectivePriority();
 		    }
 		}
 		
 		
 		/**
 		 * Called when the associated thread has acquired access to whatever is
 		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
 		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
 		 * <tt>thread</tt> is the associated thread), or as a result of
 		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
 		 *
 		 * @see	nachos.threads.ThreadQueue#acquire
 		 * @see	nachos.threads.ThreadQueue#nextThread
 		 */
 		
 		public void acquire(PriorityQueue waitQueue) {
 		    if(waitQueue.lockHolder != null) {
 		    	waitQueue.lockHolder.relinquish(waitQueue);
 		    }
 		    waitQueue.waitQueue.remove(this); //this was different from the design doc!
 		    waitQueue.lockHolder = this; //this was also different!
 		    this.acquired.add(waitQueue);
 		    if (waitQueue.transferPriority){
 		    	this.updateEffectivePriority();
 		    }
 		}
 		/**
 		 * Helper function for acquired.
 		 * @param waitQueue will leave the function with no lockholder
 		 */
 		private void relinquish(PriorityQueue waitQueue){
 			this.effectivePriority = this.priority; //not sure about this tbh
 			this.acquired.remove(waitQueue);
 			waitQueue.lockHolder = null; //maybe this doesnt need to be there
 		}
 
 		/** The thread with which this object is associated. */	   
 		protected KThread thread;
 		/** The priority of the associated thread. */
 		protected int priority;
 		
 		private int effectivePriority;
 		private PriorityQueue waitingQueue = null;
 		private long waitingTime;
 		private HashSet<PriorityQueue> acquired;
     }
 }
