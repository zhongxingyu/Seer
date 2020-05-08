 package mt;
 
 /**
  * A class to support mt debug. Not yet really used...
  */
 public class Debuggable{
 	protected volatile boolean g_debug;
 
 	private volatile int g_step;
 	private volatile int[] g_scenario;
 	
 	public Debuggable(){
 	}
 	
 	public Debuggable(int[] scenario){
 		g_scenario = scenario;
 		g_debug = true;
 	}
 	
 	public Debuggable(String scenario){
 		int len = scenario.length();
 		g_scenario = new int[len];
 		for (int i=0; i<len; i++){
 			g_scenario[i] = Integer.parseInt( scenario.substring(i, i+1) );
 		}
 		g_debug = true;
 	}
 	
 	/**
 	 * Indicates that the current thread wants to execute a new step.
 	 */
 	protected void g_step_start(){
 		g_step_start(null);
 	}
 	
 	/**
 	 * Indicates that the current thread wants to execute a new step.
 	 */
 	protected void g_step_start(String msg){
 		int waits = 0;
 		while(getNum() != g_scenario[g_step]){
 			if (waits > 10){ 
 				g_step_info("possible infinite cycle...");
 				throw new RuntimeException("possible infinite cycle"); 
 			}
 			g_step_info("waiting...");
 			waits++;
			try{
				Thread.sleep(1000);
			}
			catch(Throwable th){}
 		}
 		if (msg != null){
 			g_step_info(msg);
 		}
 	}
 	
 	/**
 	 * Indicates that the current thread finished executing a step.
 	 */
 	protected void g_step_finish(){
 		g_step_finish(null);
 	}
 
 	/**
 	 * Indicates that the current thread finished executing a step.
 	 */
 	protected void g_step_finish(String msg){
 		if (msg!=null){ g_step_info(msg); }
 		g_step = (g_step + 1) % g_scenario.length;
 	}
 	
 	protected void g_step_info(Object msg){
 		String msg2 = "STEP-" + (g_step + 1) + "[" + g_scenario[g_step] + "]:\tTHREAD-" + getNum() + ": " + msg;
 		g_info(msg2);
 	}
 	
 	protected void g_info(Object msg){
 		if (msg!=null){
 			System.out.println("DEBUG-" + Thread.currentThread().getId() + ":\t" + msg.toString());
 		}
 	}
 	
 	/**
 	 * Return the current thread's number used in the scenario.
 	 */
 	protected int getNum(){
 		throw new UnsupportedOperationException();
 	}
 }
