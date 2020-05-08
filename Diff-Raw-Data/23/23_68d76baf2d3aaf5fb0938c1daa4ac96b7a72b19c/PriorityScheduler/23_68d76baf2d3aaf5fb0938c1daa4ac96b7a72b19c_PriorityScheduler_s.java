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
 		    if(!orderedThreads.isEmpty()){
 			    KThread nextThread = orderedThreads.poll().thread;
 			    acquire(nextThread);
 			    return this.lockHolder.thread;
 		    } 
 		    return null;
 		}
 
 		/**
 		 * Return the next thread that <tt>nextThread()</tt> would return,
 		 * without modifying the state of this queue.
 		 *
 		 * @return	the next thread that <tt>nextThread()</tt> would
 		 *		return.
 		 */
 		protected ThreadState pickNextThread() {
 			return orderedThreads.peek();
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
 		private java.util.PriorityQueue<ThreadState> orderedThreads = new java.util.PriorityQueue<ThreadState>(10, new ThreadComparator<ThreadState>());
 		private ThreadState lockHolder = null;
 		
 		private class ThreadComparator<T> implements Comparator<T> {
 			@Override
 			public int compare(T o1, T o2) {
 				ThreadState t1 = (ThreadState) o1;
 				ThreadState t2 = (ThreadState) o2;
 				if (t1.getEffectivePriority() > t2.getEffectivePriority()) {
 					return -1;
 				} else if (t1.getEffectivePriority() < t2.getEffectivePriority()) {
 					return 1;
 				} else {
 					if (t1.getWaitingTime() > t2.getWaitingTime()) {
 						return 1;
 					} else if(t1.getWaitingTime() < t2.getWaitingTime()) {
 						return -1;
 					} else {
 						return 0;
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
 			
 			int tempPriority = this.priority;
 			for (PriorityQueue resource: acquired){
				if (resource.orderedThreads.peek() != null){
 					int resourceMax = resource.orderedThreads.peek().getEffectivePriority();
 					if (tempPriority < resourceMax) {
 						tempPriority = resourceMax;
 					}
 				}
 			}
 
 			if (this.effectivePriority != tempPriority){
 
 				this.effectivePriority = tempPriority;
				if(waitingQueue != null && waitingQueue.lockHolder != null)
 				{
 					this.waitingQueue.lockHolder.updateEffectivePriority();
 				}
 			}
 		}
 		
 		private int maxPriority(PriorityQueue resource) {
 			int max = -100; //this is terrible
 			for(ThreadState state: resource.orderedThreads){
 				if (state.getPriority() > max){
 					max = state.getPriority();
 				}
 			}
 			return max;
 			
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
 		    
 		    if (this.priority > this.effectivePriority) {
 		    	this.effectivePriority = this.priority;
 		    }
 		    
 		    if(waitingQueue != null && waitingQueue.lockHolder != null) {
		    	if (waitingQueue.transferPriority){
		    		System.out.println("set priort is calling update");
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
 		  
 		    waitQueue.orderedThreads.add(this);
 		    
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
 		    if (waitingQueue == waitQueue){
 		    	waitingQueue = null;
 		    }
 		    waitQueue.orderedThreads.remove(this); //this was different from the design doc!
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
 		private void relinquish(PriorityQueue releasing){
 			this.effectivePriority = this.priority; //not sure about this tbh
 			this.acquired.remove(releasing);
 			releasing.lockHolder = null; //maybe this doesnt need to be there
			if (this.waitingQueue!= null && this.waitingQueue.transferPriority && this.waitingQueue.lockHolder != null){
 				this.waitingQueue.lockHolder.updateEffectivePriority();
 			}
 		}
 
 		public String toString(){
 			String s = "{" + this.thread + " e:" + this.effectivePriority + " p:" + this.priority + "}";
 			return s;
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
     
 
 	public static void selfTest() {
 		ThreadQueue tq1 = ThreadedKernel.scheduler.newThreadQueue(true), tq2 = ThreadedKernel.scheduler.newThreadQueue(true), tq3 = ThreadedKernel.scheduler.newThreadQueue(true);
 		KThread kt_1 = new KThread(), kt_2 = new KThread(), kt_3 = new KThread(), kt_4 = new KThread();
 		
 		ThreadQueue wq = ThreadedKernel.scheduler.newThreadQueue(true);
 		KThread kt_a = new KThread(), kt_b = new KThread();
 		
 		boolean status = Machine.interrupt().disable();
 
 		wq.waitForAccess(kt_a);
 		wq.waitForAccess(kt_b);
 		
 		KThread fuckface = wq.nextThread();
 		Lib.assertTrue(fuckface == kt_a);
 		
 		kt_1.setName("kt_1");
 		kt_2.setName("kt_2");
 		kt_3.setName("kt_3");
 		kt_4.setName("kt_4");
 		
 		tq1.waitForAccess(kt_1);
 		tq2.waitForAccess(kt_2);
 		tq3.waitForAccess(kt_3);
 		
 		tq1.acquire(kt_2);
 		tq2.acquire(kt_3);
 		tq3.acquire(kt_4);
 		
 		ThreadedKernel.scheduler.setPriority(kt_1, 6);
 
 		Lib.assertTrue(ThreadedKernel.scheduler.getEffectivePriority(kt_4)==6);
 		
 		KThread kt_5 = new KThread();
 		
 		ThreadedKernel.scheduler.setPriority(kt_5, 7);
 		
 		tq1.waitForAccess(kt_5);
 		
 		Lib.assertTrue(ThreadedKernel.scheduler.getEffectivePriority(kt_4)==7);
 		
 		tq1.nextThread();
 
 		Lib.assertTrue(ThreadedKernel.scheduler.getEffectivePriority(kt_4)==1);
 		
 		Machine.interrupt().restore(status);
 		
 	
 	}
 }
