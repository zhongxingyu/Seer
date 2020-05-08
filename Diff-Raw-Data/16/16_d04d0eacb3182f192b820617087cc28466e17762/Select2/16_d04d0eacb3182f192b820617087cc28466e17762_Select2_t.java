 package select2.debug;
 
 /**
  * Select 2 protocol implementation, building upon the Java-builtin synchronization primitive: volatile.
  */
 public class Select2 extends Debuggable{
 	/**
 	* Holds the thread ids using the synch service.
 	*/
 	private final long[] threadIds;
 	
 	private volatile boolean[] active, selected, wait;
 	private volatile int token;
 	
 	/**
 	 * The current implementation does not deal with open systems: one must explicitely give the threads to choose between.
 	 */
 	public Select2(Thread[] threads){
 		threadIds = new long[2];
 		init(threads);
 	}
 		
 	/**
 	 * Constructor for the debug version.
 	 */
 	public Select2(Thread[] threads, Scenario scenario){
 		super(scenario);
 		threadIds = new long[2];
 		init(threads);
 	}
 	
 	/**
 	 * Initialize the object.
 	 */
 	protected void init(Thread[] threads){
 		for (int i = 0; i<2;i++){
			threadIds[i] = threads[i].getId();
 		}	
 		active = new boolean[2];
 		selected = new boolean[2];
 		wait = new boolean[2];
 	}
 
 	/**
 	 * Implements both the Select 2 application service and algorithm.
 	 */
 	public boolean execute(Closure closure){
 		if (g_debug){  g_start_round(); } 
 		
 		// get the internal thread number
 		int i =  getInternalThreadId();
 		
 		// 1. mark myself as active
 		if (g_debug){ g_step_start(Step.MARK_ACTIVE); }
 		active[i] = true;
 		if (g_debug){ g_step_finish(Step.MARK_ACTIVE); }
 		
 		// 2. check whether I am the token owner
 		if (g_debug){ g_step_start(Step.IS_OWNER); }
 		boolean token_owner = token == i;
 		if (g_debug){ g_step_finish(Step.IS_OWNER); }
 	
 		// 3. check whether the other thread already entered the selection protocol
 		if (active[(i + 1) % 2]){
 			// 3.1. if I am not the token owner then wakeup owner, cleanup and exit 
 			if (!token_owner){
 				if (g_debug){ g_step_start(Step.OWNER_ACTIVE); }
 				wait[(i+1) % 2] = false;
 				active[i] = false;
 				if (g_debug){ g_step_finish(Step.OWNER_ACTIVE); g_finish_round(); }
 				return false;
 			}
 			// 3.2. if I am the token owner wait for the other thread till it decides what to do 
 			else{
 				wait[i] = true;
 				while ( true ){
 					if (g_debug){ g_step_start(Step.WAIT_CONDITION); }
 					if (token != i || !active[(i + 1) % 2] || !wait[i]){ break; }
 					if (g_debug){ g_step_finish(Step.WAIT_CONDITION); }
 					Thread.yield();
 				}
 				wait[i] = false;
 			}
 		}
 		
 		// 4. now different cases could happen:
 		if (token_owner){
 			// 4.1. if I was the token owner but the other thread took the ownership so far, then I am not selected, cleanup and exit
 			if (token != i){
 				if (g_debug){ g_step_start(Step.LOST_OWNERSHIP); }
 				active[i] = false;
 				if (g_debug){ g_step_finish(Step.LOST_OWNERSHIP); g_finish_round(); }
 				return false;
 			}
 			// 4.2. if I was and still is the token owner, then I am selected, give up the token ownership, cleanup and exit
 			else{
 				if (g_debug){ g_step_start(Step.OWNER_SELECTED); }
 				selected[i] = true;
 				assert !selected[(i+1) % 2];
 				boolean result = closure.execute();
 				selected[i] = false;
 				token = (i + 1) % 2;
 				active[i] = false;
 				if (g_debug){ g_step_finish(Step.OWNER_SELECTED); g_finish_round(); }
 				return result; 
 			} 
 		}
 		// 4.3. if I was not the token owner but reached this point, than I am selected, get the token ownership, cleanup and exit
 		else {
 			if (g_debug){ g_step_start(Step.NOT_OWNER_SELECTED); }
 			token = i;
 			selected[i] = true;
 			assert !selected[(i+1) % 2];
 			boolean result = closure.execute();
 			selected[i] = false;
 			active[i] = false;
 			if (g_debug){ g_step_finish(Step.NOT_OWNER_SELECTED); g_finish_round(); }
 			return result;
 		}
 	}
 
 	/**
 	 * Get the internal thread id used in the protocol: 0 or 1.
 	 */
 	protected int getInternalThreadId(){
 		if (threadIds[0] == Thread.currentThread().getId()){ return 0; }
 		else{ return 1; }
 	}	
 	
 	public static enum Step{
 		MARK_ACTIVE, 
 		IS_OWNER, 
 		WAIT_CONDITION, 
 		OWNER_ACTIVE, 
 		LOST_OWNERSHIP, 
 		OWNER_SELECTED, 
 		NOT_OWNER_SELECTED;
 	}
 }
