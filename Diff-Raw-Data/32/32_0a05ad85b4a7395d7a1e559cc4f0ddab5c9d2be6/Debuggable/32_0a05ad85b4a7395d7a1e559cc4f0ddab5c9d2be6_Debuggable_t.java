 package select2.debug;
 
 /**
  * A class to support debug.
  *
  * TODO: handle nested steps (ifelse, while, etc).
  */
 public abstract class Debuggable{
 	/**
 	 * Shows whether to debug.
 	 */
 	protected final boolean g_debug;
 	
 	/**
 	 * Represents the current elementary step.
 	 */
 	private volatile int g_step;
 	
 	/**
 	 * Represents the actor array of the scenario.
 	 */
 	protected final int[] g_actors;
 	
 	protected volatile boolean[] g_finished;
 	
 	/**
 	 * Represents the number of rounds of the scenario.
 	 */
 	protected final int g_num_of_rounds;
 	
 	/**
 	 * Represents the number of rounds each thread executed.
 	 */
 	protected final int[] g_rounds;
 	
 	/**
 	 * Constructs the debugger for the given threads and scenario.
 	 */
 	protected Debuggable(Scenario scenario){
 		g_num_of_rounds = scenario.getRounds();
 		g_rounds = new int[2];
 		g_actors = scenario.getActors();
 		g_debug = true;
 		g_finished = new boolean[2];
 	}
 
 	/**
 	 * A constructor for no debug.
 	 */
 	protected Debuggable(){
 		g_num_of_rounds = 0;
 		g_rounds = null;
 		g_actors = null;
 		g_debug = false;
 		g_finished = null;
 	}
 
 	/**
 	 * Get the internal thread id used in the protocol, must be implemented in the subclass.
 	 */
 	protected abstract int getInternalThreadId();
 	
 	/**
 	 * Marks that a thread wants to start a new round.
 	 */
 	protected void g_start_round(){
 		int tid = getInternalThreadId();
		if (g_rounds[tid] < g_num_of_rounds || g_rounds[(tid+1) % 2] < g_num_of_rounds){ 
 			g_new_round_info(); 
 		}
 		else{ 
 			g_final_round_info(); 
 			g_finished[tid] = true; 
 			throw new RuntimeException("Rounds finished"); 
 		}
 	}
 	
 	/**
 	 * Marks that a thread finished its round.
 	 */
 	protected void g_finish_round(){
 		int tid = getInternalThreadId();
 		int round = g_rounds[tid];
 		
		g_rounds[tid] = round + 1;
 	}
 
 	/**
 	 * Marks that a thread wants to start a new step (similar to a break point, but not the same).
 	 */
 	protected void g_step_start(Object step){
 		int tid = getInternalThreadId();
 		while(true){
 			if (tid == g_actors[g_step]){ break; }
 			if (g_finished[(tid+1) % 2]){ break; }
 			Thread.yield();
 		}
 		g_step_info(step);
 	}
 
 	/**
 	 * Marks that a thread finished a step (similar to a break point, but not the same).
 	 */
 	protected void g_step_finish(Object step){
 		g_step = (g_step + 1) % g_actors.length;
 	}
 	
 	// experimental
 	protected void g_new_round_info(){
 		g_round_info("NEW_ROUND");
 	}
 	
 	protected void g_final_round_info(){
 		g_round_info("FINISHED");
 	}
 	
 	protected void g_round_info(Object msg){
 		int tid = getInternalThreadId();
 		int round = g_rounds[tid];
 		System.out.println("DEBUG:\tTHREAD-" + tid + "[" + round + "]:\t" + msg.toString());
 	}
 	
 	protected void g_step_info(Object msg){
 		int tid = getInternalThreadId();
 		int round = g_rounds[tid];
 		System.out.println("DEBUG:\tTHREAD-" + tid + "[" + round + ", " + g_actors[g_step] + "]:\t" + msg.toString());
 	}
 }
