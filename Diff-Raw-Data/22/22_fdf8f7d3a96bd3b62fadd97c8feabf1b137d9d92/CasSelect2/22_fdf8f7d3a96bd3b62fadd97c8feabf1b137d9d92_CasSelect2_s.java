 package mt.bench;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * Select 2 service implemented upon the Java-builtin synchronization primitive: compare and swap (CAS for short).
  * It does not implement the protocol itself, just its features and services. Ie. it provides the same features and 
  * services as Select 2 but uses a different algorithm.
  */
 public class CasSelect2 extends AbstractSelect2{
 	// TODO: should volatile be added?
 	/**
 	 * Holds the atomic integer for CAS.
 	 */
 	private final AtomicInteger atomic = new AtomicInteger(0);
 	
 	/**
 	 * Used for debugging and assertion, just for sure.
 	 */
 	private volatile boolean[] selected;
 
 	/**
 	 * The current implementation does not deal with open systems: one must explicitely give the threads to choose between.
 	 */
 	public CasSelect2(Thread[] threads){
 		super(threads);
 		selected = new boolean[2];
 	}
 	
 	/**
 	 * Implements the Select 2 application service with a different algorithm, using CAS.
 	 */
 	public boolean execute(Closure closure){
 		// Get my internal thread number
 		int tid = getInternalThreadId();
 		
 		// 1. Get the current value
 		int oldValue = atomic.get();
 		
 		// 2. Set the value locally
 		int newValue = -1;
 		
		// 2.1. if the value was different then my thread id, then set it to my id
 		if (oldValue != tid){
 			newValue = tid;
 		}
		// 2.2. if the values are the same offset my id with 2, that is set the value to 2 + tid
 		// This is necessarry otherwise if we use the same value, then the value does not change
 		// hence the other thread might mistakenly think that the old value did not change.
 		else{
 			newValue = 2 + tid;
 		}
 		
 		// 3. Compare and swap
 		boolean casResult = atomic.compareAndSet(oldValue, newValue);
 		
 		// 4. Evaluate the CAS result and act upon it
 		
 		// 4.1. Check whether CAS failed, if yes return
 		if ( !casResult ){
 			return false;
 		}
		// 4.2. Else if CAS succeeded mark myself as selected (for debugging and assertion) and execute closure
 		else{
 			selected[tid] = true;
 			assert !selected[ (tid + 1) % 2 ];
 			boolean result = closure.execute();
 			selected[tid] = false;
 			return result;
 		}
 	}
 }
