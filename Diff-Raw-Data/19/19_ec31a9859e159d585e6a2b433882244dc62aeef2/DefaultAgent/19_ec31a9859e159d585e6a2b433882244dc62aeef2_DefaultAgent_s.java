 import java.util.*;
 
 public class DefaultAgent implements Agent
 {
 	private Random random = new Random();
 	private String role;
 	//private int myRole;
 	private long turnStarted;
 	private int playClock;
 	private boolean myTurn;
 	private State currentState; 
 	private int[] curDepthLevel;
 	private int maxDepth;
 	private final int MAX = 1000000;
 	private final int MIN = -1000000;
 
 	/*
 		init(String role, int playClock) is called once before you have to select the first action. Use it to initialize the agent. role is either "WHITE" or "RED" and playClock is the number of seconds after which nextAction must return.
 	*/
     public void init(String role, int playClock) {
 		System.out.println("Start init");
 		this.maxDepth = 43;
 		this.role = role;
 		this.playClock = playClock;
 		this.curDepthLevel = new int[7];
 		System.out.println("Play clock" + playClock);
 		
 		myTurn = role.equals("WHITE");
 		currentState = new State(0,0,myTurn);
         
 		//for(State i : currentState.legalMoves()) 
 		//	System.out.print(heuristic(i) + " ");
 		//System.out.println();
 		
         //for(State i : currentState.legalMoves()) {
 		//    for (State j : i.legalMoves())
 		//	    System.out.print(heuristic(j) + " ");
 		//	System.out.println();
 		//}
 		
 		System.out.println("Done init");
     }
 
     public int heuristic(State state){
         int Value = 0;
         long red = state.red;
         long white = state.white;
         for(int i = 0; i<42; i++){            
             //horizontal pairs
            if(red & (red >> 1))&1) Value--;
            if(white & (white >> 1)) &1) Value++;
             
             //vertical lines
            if((red & (red >> 7))&1) Value--;
            if((white & (white >> 7)) &1) Value++;          
             
             //left leaning lines           
            if((red & (red >> 8)) & 1)&1) Value--;
            if((white & (white >> 8)) & 1)&1) Value++;
 
             //right leaning lines
            if((red & (red >> 6)) & 2) Value--;
            if((white & (white >> 6)) & 2) Value--;
         }
         if(state.whiteTurn) return Value;
 		return -Value;
 	}
 	// lastDrop is 0 for the first call of nextAction (no action has been executed),
 	// otherwise it is a number n with 0<n<8 indicating the column that the last piece was dropped in by the player whose turn it was
     public String nextAction(int lastDrop) {
 		turnStarted = System.nanoTime();
 		maxDepth--;
         System.out.println("-------------------->  Enemy dropped at " +lastDrop);
 
 		if(lastDrop != 0)
             currentState.addMove(lastDrop,myTurn); 
 		System.out.println("Red: "+ (long)currentState.red + " White: " +currentState.white );
 		
 		myTurn = !myTurn; 		
         if(!myTurn){
 			int drop = whereToDrop();
 			System.out.println("------------------------->   Droping to " + drop);
 			return "(DROP " + drop + ")";
 		}
 		else
 		    return "NOOP";
 	}
 	
 	/// Returns the column which is best to drop inn.
 	int whereToDrop(){
 		//System.out.println("hasTime: " + hasTime());
 		int result = 0;
 		for (int deep = 1; result == 0; deep++){
 			result = calculate(deep,currentState);
 			System.out.println("depth: " + deep);
 			
 			if (result > 0)
 				return result;
 			
 			if (deep > maxDepth)
 				break;
 		}
 		int best = 0;
 		for (int i = 0; i < 7; i++ ) {
 			System.out.println( i +" " + curDepthLevel[i]);
 			if (curDepthLevel[i] > curDepthLevel[best]) 
 				best = i;
 		
 		}
 		return best + 1;
 	}
 	
 	// Call: value = AlphaBeta( MaxDepth, s, -INF, INF )
 	int calculate(int depth, State state) {
 		int bestIndex = 0;
 		int i = 0;
 		
 		int[] bestValues = new int[7];
 		Integer beta = MAX; //Call by referance :)
 		Integer alpha = MIN;
 		for ( State s : state.legalMoves()) {
 			if (s == null) {
 				bestValues[i++] = MIN;
 				continue;
 			}
 			bestValues[i] = -alphaBeta( depth-1, s, -beta, -alpha );
 			if ( bestValues[i] > alpha ) {
 				alpha = bestValues[i];
 				if ( alpha >= beta ) break;
 			}
 			i++;
 		}
 		if (hasTime()){
 			//We did not run out of time :)
 			int numMin = 0;
 			for (int k = 0; k < 7; k++ ) {// and can safely use these values.
 				System.out.println( k +" " + bestValues[k]);
 				curDepthLevel[k] = bestValues[k];
 				if (bestValues[k] == MAX)
 					return k+1;
 				if (bestValues[k] == MIN)
 					numMin++;
 			}
 			if (numMin == 6)
 				for (int k = 0; k < 7; k++ )
 					if (bestValues[k] != MIN)
 						return k +1;
 			return 0;
 		}
 		else{
 			for (int k = 0; k < 7; k++ )
 				System.out.println( k +" " + bestValues[k]);
 			return -1;
 		}
 	}
 	
 	// Call: value = AlphaBeta( MaxDepth, s, -INF, INF )
 	int alphaBeta(int depth, State state, Integer alpha, Integer beta) {
 		int h = heuristic(state);
 		if (depth <= 0 || h == MAX || h == MIN || !hasTime()){
 			//System.out.println("Heuristics: " + heuristics + " size " + state.getCombos().size() + " ocupied " + state.getOccupied());
 			return h;
 		}
 		int bestValue = MIN;	
 		for ( State s : state.legalMoves()) {
 			if (s == null) continue;
 			int value = -alphaBeta( depth - 1, s, -beta, -alpha );
 			if (value > bestValue) bestValue = value;
 			if ( bestValue > alpha ) {
 				alpha = bestValue;
 				//System.out.println("New Alpha value "+alpha+" Heuristics: " + heuristics + " size " + state.getCombos().size() + " ocupied " + state.getOccupied());
 				if ( alpha >= beta ) {
 					//System.out.println("Prooning!!");
 					break;
 				}
 			}
 		}
 		return bestValue;
 	}
 	//returns in ms.
 	//return example 5 sec is 500.
 	boolean hasTime()
 	{
 		return (System.nanoTime() - turnStarted) / 10000000 <= playClock * 100  - 50;
 	}
 		
 }
