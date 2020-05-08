 package nachos.threads;
 
 import nachos.machine.*;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.TreeSet;
 import java.util.HashSet;
 import java.util.Iterator;
 import static java.lang.Math.*;
  
 public class PriorityScheduler extends Scheduler {
   //Fields
   public static final int priorityDefault = 1;
   public static final int priorityMinimum = 0;
   public static final int priorityMaximum = 7;
 	
 	//Constructor
 	public PriorityScheduler() {
   }
 
 	//Helper Methods
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
 		Lib.assertTrue(priority >= priorityMinimum && priority <= priorityMaximum);
 		getThreadState(thread).setPriority(priority);
   }
 	
 	protected ThreadState getThreadState(KThread thread) {
 		if (thread.schedulingState == null)
 			thread.schedulingState = new ThreadState(thread);
 		return (ThreadState) thread.schedulingState;
   }
 	
 	//Action Methods
   public ThreadQueue newThreadQueue(boolean transferPriority) {
 		return new PriorityQueue(transferPriority);
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
 
 
 
   protected class PriorityQueue extends ThreadQueue {
 		//Fields
 		public ThreadState holder;
 		public TreeSet<ThreadState> waitQueue ; //max effectivePriority/time pops first.
 		public boolean transferPriority;
 		
 		//Constructor
 		PriorityQueue(boolean transferPriority) {
 			this.transferPriority = transferPriority;
 			this.holder = null;
 			
 			waitQueue = new TreeSet<ThreadState>(new Comparator<ThreadState>() {
 				public int compare(ThreadState ts1, ThreadState ts2) {
 					if (ts1.getEffectivePriority() == ts2.getEffectivePriority())
 						return new Long(ts2.time).compareTo(ts1.time);
 					return (new Integer(ts1.getEffectivePriority()).compareTo(ts2.getEffectivePriority()));
 				}
 			});
 		}
 		
     //Action Methods
 		protected ThreadState pickNextThread() {
 			assert (false);
 			if (this.waitQueue.isEmpty())
 				return null;
 			return this.waitQueue.last(); //retrieve top element.
 		}
 		
 		public void waitForAccess(KThread thread) {
 			Lib.assertTrue(Machine.interrupt().disabled());
 			getThreadState(thread).waitForAccess(this);
 		}
 
 		public void acquire(KThread thread) {
 			Lib.assertTrue(Machine.interrupt().disabled());
 			getThreadState(thread).acquire(this);
 		}
 
 		public KThread nextThread() {
 			Lib.assertTrue(Machine.interrupt().disabled());
 			ThreadState newHolder = this.waitQueue.pollLast(); //return null if waitQueue is empty
 
 			if (newHolder != null) { //When waitQueue is not empty
 				this.acquire(newHolder.thread);
 				return newHolder.thread;
 			}
 			else { //When waitQueue is empty
 				this.holder = null;
 			  return null;
 			}
 		}
 		
 		public void print() {
 			Lib.assertTrue(Machine.interrupt().disabled());
 			//TODO (optional)
 		}
   }
 
 
 
   protected class ThreadState {
     //Fields
 		protected KThread thread;
 		protected int priority;
 		public int effectivePriority;
 		public ArrayList<PriorityQueue> pqHave;
 		public PriorityQueue pqWant;
 		public long time;
 		
 		
 		//Constructor
 		public ThreadState(KThread thread) {
 			this.thread = thread;
 			this.pqHave = new ArrayList<PriorityQueue>();
 			this.setPriority(priorityDefault);
 			this.pqWant = null;
 			this.time = -1;
 		}
 
 
 		//Helper Methods
 		public int getPriority() {
 			return this.priority;
 		}
 		
 		public int getEffectivePriority() {
 			return this.effectivePriority;
 		}
 		
 		public void setPriority(int priority) {
 			this.priority = priority;
 			this.effectivePriority = priority;
 			
 			if (pqWant != null) {
 				if (this.pqWant.transferPriority == true) {
 					this.updateEffectivePriority();
 					this.pqWant.holder.setEffectivePriority(this);
 				}
 				else {
 					this.pqWant.waitQueue.remove(this);
 					this.pqWant.waitQueue.add(this);
 				}
 			}
 			
 			/*
 			// when my priority is greater than my effectivepriority
 			// when i am waiting for a resource and transferpriority is true,
 			// i need to donate again
 			if (priority > this.effectivePriority) {
 				this.effectivePriority = priority;
 				this.pqWant.waitQueue.remove(this);
 				this.pqWant.waitQueue.add(this);
 				if (this.pqWant.transferPriority)
 					this.pqWant.holder.setEffectivePriority(this);
 			}
 		
 			// when i am lowering my priority, then i need to recalculate my effectivepriority
 			// when, of course, pqWant.transferpriority is true
 			if (priority < this.effectivePriority) {
 				this.effectivePriority = priority;
 				this.pqWant.waitQueue.remove(this);
 				this.pqWant.waitQueue.add(this);
 				if (pqWant.transferPriority)
 				{
 					this.updateEffectivePriority();
 				}
 			}
 			*/
 		}
 
 		/*
      * Name: setEffectivePriority
      * Input: ThreadState
      * Output: None
      * We use setEffectivePriority() for waitForAcceess().
      * Suppose we just added a ThreadState to a waitQueue and the holder has 
      * smaller effectivePriority. In this case, we need to reset the
      * effectivePriority of the holder.
      */
 		public void setEffectivePriority(ThreadState donator) {
 		  this.effectivePriority = max(this.effectivePriority, donator.effectivePriority);
 			if (this.pqWant != null) {
         this.pqWant.waitQueue.remove(this);
         this.pqWant.waitQueue.add(this);
         if (pqWant.transferPriority == true)
           this.pqWant.holder.setEffectivePriority(this);
       }
 		}
 		
     /*
      * Name: updateEffectivePriority
      * Input: None
      * Output: None
      * We use updateEffectivePriority() for acquire().
      * Suppose a holder of PriorityQueue is done with this resource.
      * Then we need to reset the holder's effectivePriority one step back.
      * Note: This function should be called ONLY is pq has 
      */
 		public void updateEffectivePriority() {
 			//Calculate new effectivePriority checking possible donations from threads that are waiting for me
 			int maxPriority = priorityMinimum;
 			for (PriorityQueue pq: this.pqHave)
 				if (pq.transferPriority == true)
 					maxPriority = max(maxPriority, pq.holder.getEffectivePriority());
 			
       //If there is a change in priority, update and propagate to other owners
 			if (maxPriority > this.effectivePriority) {
 				this.effectivePriority = maxPriority;
 				if(this.pqWant != null) {
 					//Readjust myself in the pq with new priority
 					this.pqWant.waitQueue.remove(this);
 					this.pqWant.waitQueue.add(this);
 					//Donate my priority to pq owner
 					if (pqWant.transferPriority == true)
 						pqWant.holder.setEffectivePriority(this);
 				}
 			}
 		}
 		
 
     //Action Methods
     /*
      * Name: waitForAccess
      * Input: PriorityQueue
      * Output: None
      * We use waitForAccess() when a ThreadState wants a lock
      * but cannot get it because another ThreadState is holding
      * the lock. So we add this ThreadState to pq.
      */
 		public void waitForAccess(PriorityQueue pq) {
 			this.pqWant = pq;
 			this.time = Machine.timer().getTime();
 			pq.waitQueue.add(this);
 			//Propagate this ThreadState's effectivePriority to holder of pq
 			if (pq.transferPriority == true)
 				pq.holder.setEffectivePriority(this);
 		}
 		
     /*
      * Name: acquire
      * Input: PriorityQueue
      * Output: None
      * We use acquire() when this ThreadState is getting the lock.
      * In other words, this ThreadState is set to the holder of pq.
      */
 		public void acquire(PriorityQueue pq) {
 			//Adjust the state of prev pq holder (ThreadState)
 			ThreadState prevHolder = pq.holder;
			if (prevHolder != null)
 				prevHolder.pqHave.remove(pq);
 
 			//Adjust the state of this ThreadState 
 			if (this.pqWant != null && this.pqWant.equals(pq))
 				this.pqWant = null;
       this.pqHave.add(pq);
 			
       //Adjust the state of pq
 			pq.waitQueue.remove(this);
 			pq.holder = this;	
 			
      //Adjust the effectivePriority of prev pq holder
			if (pq.transferPriority == true)
				prevHolder.updateEffectivePriority();
 		}	
   }
 }
