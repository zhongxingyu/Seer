 package ndfs.mcndfs_alg2;
 
 import java.lang.Runnable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.Collections;
 
 import graph.Graph;
 import graph.State;
 
 import ndfs.Result;
 import ndfs.CycleFound;
 import ndfs.NoCycleFound;
 import ndfs.mcndfs_alg2.MapWithDefaultValues;
 
 class Worker implements Runnable
 {
     // Locals
     private final Graph graph;
     private State state;
     private MapWithDefaultValues<State,Color> colors;
     private MapWithDefaultValues<State,Boolean> isPink;
     private long randomSeed;
 
     // Globals
     private MapWithDefaultValues<State, Boolean> isRed;
     private MapWithDefaultValues<State, Integer> visitCount;
 
     public Worker(final Graph graph, MapWithDefaultValues<State, Boolean> isRed, MapWithDefaultValues<State, Integer> visitCount, long randomSeed)
     {
     	// Locals
         this.graph = graph;
         this.state = graph.getInitialState();
         this.colors = new MapWithDefaultValues<State,Color>(new HashMap<State,Color>(),Color.WHITE);
         this.isPink = new MapWithDefaultValues<State,Boolean>(new HashMap<State,Boolean>(), false);
 	this.randomSeed = randomSeed;
 
 	// Globals
 	this.isRed = isRed;
 	this.visitCount  = visitCount;
     }
 
     private void dfsRed(State s) throws Result {
         isPink.setValue(s, true);
 	
 	List<State> shuffledList = graph.post(s);
	//Collections.shuffle(shuffledList, new Random(randomSeed));
         for (State t : shuffledList) {
             if  (colors.hasKeyValuePair(t, Color.CYAN)) {
                 throw new CycleFound();
             } else if (
 	        isPink.hasKeyValuePair(t, false) &&
 		isRed.hasKeyValuePair(t,false)) 
 	    {
                 dfsRed(t);
             }
         }
 
 	if (s.isAccepting()) {
 	    atomicDecrementVisitCount(s);
 	    while (visitCount.getValue(s) != 0) {}
 	}
 
 	isRed.setValue(s, true);
 	isPink.setValue(s, false);
     }
 
 
     private void dfsBlue(State s) throws Result {
         colors.setValue(s, Color.CYAN);
 	List<State> shuffledList = graph.post(s);
	//Collections.shuffle(shuffledList, new Random(randomSeed));
         for (State t : shuffledList) {
             if (
 	    	colors.hasKeyValuePair(t, Color.WHITE) &&
 		isRed.hasKeyValuePair(t, false)
 	       ) 
 	    {
                 dfsBlue(t);
             }
         }
 
         if (s.isAccepting()) {
 	    atomicIncrementVisitCount(s);
             dfsRed(s);
         }
 	
         colors.setValue(s, Color.BLUE);
     }
 
     synchronized void atomicIncrementVisitCount(State s) {
             int value = visitCount.getValue(s);
 	    visitCount.setValueSynchronized(s, value + 1);
     }
 
     synchronized void atomicDecrementVisitCount(State s) {
             int value = visitCount.getValue(s);
 	    visitCount.setValueSynchronized(s, value - 1);
     }
 
     public void run()
     {
         try {
             dfsBlue(state);
            throw new NoCycleFound();
         } catch (Result e) {
             // TODO: Set a flag
             System.out.println("We got : " + e.toString());
         }
     }
 
 }
