 package problemSolver.algorithms;
 
 import problemSolver.statemanager.StateManager;
 
 public abstract class SearchAlgorithm {
 	public final StateManager P;
 	
 	public SearchAlgorithm(StateManager P){
 		this.P = P;
 	}
 	
 	public void run(){
 		//Start timer
 		long time = System.nanoTime();
 		//Initiate state
		P.makeStatePermanent();
 		P.initState();
 		
 		//Print init solution
 		System.out.println("---Initial solution--");
 		P.printState();
 		
 		this.solve();
 		// Display solution
 		System.out.println("---Final solution---");
 		
 		P.printState();
 		System.out.println("Runtime: " + ((System.nanoTime()-time)/1000000f) + " milliseconds");
 		System.out.println();
 		
 	}
 	protected abstract void solve();
 }
