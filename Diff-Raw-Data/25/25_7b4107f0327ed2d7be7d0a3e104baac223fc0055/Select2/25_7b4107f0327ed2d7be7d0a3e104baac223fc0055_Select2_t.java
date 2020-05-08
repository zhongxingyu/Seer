 package mt;
 
 /**
  * The Select 2 protocol is a lock free protocol that can select one and only one thread from two threads.<br/>
  * <br/>
  * This synchronization primitive provides the following guaratee:<br/>
  * <br/> 
  * <b>Select 2 guarantees that only one thread is selected at any time</b><br/> 
  * <br/>
  * This synchroinzation primitive can be used to execute critical sections:<br/>
  * <br/>
  * <b>Select 2 can be used to execute critical sections atomically in a lock free manner.</b>
  */
 public class Select2 extends Debuggable{
 	private long[] threads;
	private volatile boolean[] active, selected, wait;
 	private volatile int token;
 	
 	/**
 	 * The current implementation does not deal with open systems: one must explicitely give the threads to choose between.
 	 */
 	public Select2(Thread[] threads){
 		init(threads);
 	}
 	
 	/**
 	 * Constructor for debugging purposes.
 	 */
 	public Select2(Thread[] threads, String scenario){
 		super(scenario);
 		init(threads);
 	}
 	
 	/**
 	 * Constructor for debugging purposes.
 	 */
 	public Select2(Thread[] threads, int[] scenario){
 		super(scenario);
 		init(threads);
 	}
 	
 	protected void init(Thread[] threads){
 		active = new boolean[2];
 		selected = new boolean[2];
		wait = new boolean[2];
 		this.threads = new long[2];
 		for (int i = 0; i<2;i++){
 			this.threads[i] = threads[i].getId();
 		}
 	}
 	
 	/**
 	 * Get the thread number used in the protocol (0 or 1).
 	 */
 	protected int getNum(){
 		if (threads[0] == Thread.currentThread().getId()){ return 0; }
 		else{ return 1; }
 	}
 	
 
 	/**
 	 * Selects the current thread or not - if it is selected then it executes the given closure, otherwise not.
 	 * Returns true if the closure was executed and its execution was successful, otherwise false. That is to say: false 
 	 * can mean either that the closure was not selected to run or it was executed but failed (returned false). 
 	 */
 	public boolean execute(Closure closure) throws Throwable{
 		// determine internal thread number
 		int i = getNum();
 		
 		// 1. mark myself as active
 		if (g_debug){ g_step_start(); }
 		active[i] = true;
 		if (g_debug){ g_step_finish("1. marked myself as active."); }
 		
 		// 2. check whether I am the token owner
 		if (g_debug){ g_step_start(); }
 		boolean token_owner = token == i;
 		if (g_debug){ g_step_finish("2. I am the token owner? " + token_owner); }
 	
 		// 3. check whether the other thread already entered the selection protocol
 		if (g_debug){ g_step_start(); }
 		if (active[(i + 1) % 2]){
			// 3.1. if I am not the token owner then wakeup owner, cleanup and exit 
 			if (!token_owner){
				wait[(i+1) % 2] = false;
 				active[i] = false;
 				if (g_debug){ g_step_info("3.1. token owner is active, exiting"); g_step_finish("NOT SELECTED");}
 				return false;
 			}
 			// 3.2. if I am the token owner wait for the other thread till it decides what to do 
 			else{
 				if (g_debug){ g_step_finish("3.2. waiting for the other thread..."); }
				wait[i] = true;
				while ( token == i && active[(i + 1) % 2] && wait[i]){
 					if (g_debug){ g_step_start(); }
 					Thread.yield();
 					if (g_debug){ g_step_finish(); }
 				}
				wait[i] = true;
 			}
 		}
 		else{
 			if (g_debug){ g_step_finish("3. other thread is not active"); }
 		}
 		
 		Throwable throwed = null;
 		boolean result = false;
 		// 4. now different cases could happen:
 		if (g_debug){ g_step_start(); }
 		if (token_owner){
 			// 4.1. if I was the token owner but the other thread took the ownership so far, then I am not selected, cleanup and exit
 			if (token != i){
 				active[i] = false;
 				if (g_debug){ g_step_info("4.1. other thread took the ownership, exiting"); g_step_finish("NOT SELECTED");}
 				return false;
 			}
 			// 4.2. if I was and still is the token owner, then I am selected, give up the token ownership, cleanup and exit
 			else{
 				selected[i] = true;
 				assert !selected[(i+1) % 2];
 				try{ result = closure.execute(); }
 				catch(Throwable th){ throwed = th; }
 				selected[i] = false;
 				token = (i + 1) % 2;
 				active[i] = false;
 				if (g_debug){ g_step_info("4.2. I was selected, exiting"); g_step_finish("SELECTED");}
 				if (throwed == null){ return result; }
 				else{ throw throwed; }
 			} 
 		}
 		// 4.3. if I was not the token owner but reached this point, than I am selected, get the token ownership, cleanup and exit
 		else {
 			token = i;
 			selected[i] = true;
 			assert !selected[(i+1) % 2];
 			try{ result = closure.execute(); }
 			catch(Throwable th){ throwed = th; }
 			selected[i] = false;
 			active[i] = false;
 			if (g_debug){ g_step_info("4.3. took the token, was selected, exiting"); g_step_finish("SELECTED");}
 			if (throwed == null){ return result; }
 			else{ throw throwed; }
 		}
 	}
 
 	/**
 	 * Runs the given test.
 	 */
 	public static void main(String args[]){
 		Select2Thread[] threads = new Select2Thread[2];
 		
 		for (int i = 0; i<2 ;i++){
 			threads[i] = new Select2Thread();
 		}	
 		
 		Select2 select2 = new Select2(threads, args[0]);
 		
 		for (int i = 0; i<2 ;i++){
 			threads[i].setSelect2(select2);
 			threads[i].start();
 		}
 	}
 	
 	/**
 	 * A simple class for testing purposes.
 	 */	
 	private static class Select2Thread extends Thread{
 		private Select2 select2;
 		
 		public void setSelect2(Select2 select2){
 			this.select2 = select2;
 		}
 		
 		public void run(){
 			long id = Thread.currentThread().getId();
 			try{
 				for(int i=0;i<10;i++){
 					//System.out.println( "DEBUG-" + id + ":\texecuting..." );
 					boolean executed = select2.execute(TEST);
 					//System.out.println( "DEBUG-" + id + ":\texecuted? " + executed);
 				}
 			}
 			catch(Throwable th){
 			}
 			Thread.yield();
 		}			
 	}
 	
 	private static final TestClosure TEST = new TestClosure();
 	private static class TestClosure implements Closure{
 		public boolean execute() throws Throwable{
 			return true;
 		}
 	}
 
 }
