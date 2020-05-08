 package mt.bench;
 
 /**
  * Represents Select 2 'abstract' features. Does two things: convert 
  * (1) threads to thread ids and 
  * (2) threads to internal numbers.
  */
 public abstract class AbstractSelect2{
 	/**
 	* Holds the thread ids using the synch service.
 	*/
 	private final long[] threadIds;
 	
 	/**
 	 * The current implementation does not deal with open systems: one must explicitely give the threads to choose between.
 	 */
 	public AbstractSelect2(Thread[] threads){
 		threadIds = new long[2];
 		for (int i = 0; i<2;i++){
 			threadIds[i] = threads[i].getId();threads[i].getId();
 		}
 	}
 
 	/**
 	 * Get the internal thread id used in the protocol: 0 or 1.
 	 */
 	protected int getInternalThreadId(){
 		if (threadIds[0] == Thread.currentThread().getId()){ return 0; }
 		else{ return 1; }
 	}
 
 	/**
 	 * Select 2 application service that should
 	 * (1) select the current thread or not,
 	 * (2) if it is selected then it should execute the given closure, otherwise not.
 	 * Returns true if the closure was executed and its execution was successful, otherwise false. That is to say: false 
 	 * can mean either that the closure was not selected to run or it was executed but failed (returned false). 
 	 */	
 	public abstract boolean execute(Closure closure);
 }
