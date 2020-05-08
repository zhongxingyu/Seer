 package select2.debug;
 
 import java.util.Random;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * A scenario that represents test cases for two threads. It implements two things:
  * (1) random scenarios
  * (2) all scenarios for the give actor length (number of thread ids).
  * Usage:
  * <code>
  * Scenario2 allScenarios = new Scenario2(...)
  * for ( Scenario scenario : allScenarios ){
  *		// test scenario
  * }
  * </code>
  */
 public class Scenario2 extends Scenario implements Iterable<Scenario>, Iterator<Scenario>{
 	private int actorLength;
 	private boolean shouldGenerateNext, hasNext;
 	private final static Random random = new Random();
 	
 	/**
 	 * Constructor for all scenarios by the actor given length.
 	 */
 	public Scenario2(int rounds, int actorLength){
 		setRounds(rounds);
 		this.actorLength = actorLength;
 		shouldGenerateNext = true;
 	}
 
 	public Iterator<Scenario> iterator(){
 		return this;
 	}
 	
 	public boolean hasNext(){
 		if (shouldGenerateNext){
 			hasNext = generateNext();
 			shouldGenerateNext = false;
 		}
 		return hasNext;
 	}
 	
 	public Scenario next(){
 		if ( hasNext() ){
 			shouldGenerateNext = true;
 			return this;
 		}
 		else{
 			throw new NoSuchElementException();
 		}
 	}
 	
 	public void remove(){
 		throw new UnsupportedOperationException();
 	}
 	
 	/**
 	 * Generate the next scenario if any. Returns true if there's any, false otherwise.
 	 * The algorithm implements simply an increment operation starting by 1.
 	 */
 	protected boolean generateNext(){
 		int[] actors = getActors();
 		
 		// if not yet initialized do it
 		if (actors == null){
 			actors = new int[ actorLength ];
 			actors[actorLength - 1] = 1;
 			setActors(actors);
 			return true;
 		}
 		// move onto the next scenario, which is greater than the previous by 1
 		else{
 			int actor;
 			int overflow = 1;
 			for (int i=actorLength-1; i>=0; i--){
 				actor = actors[i];
 				switch(actor + overflow){
 					case 1: actors[i] = 1; overflow = 0; break;
 					case 2: actors[i] = 0; overflow = 1; break;
 					case 3: actors[i] = 1; overflow = 1; break;
 				}
 				if (overflow == 0){ break; }
 			}
 			
 			int numberOfOnes = 0;
 			for (int i=0; i<actorLength;i++){
 				if (actors[i] == 1){ numberOfOnes++; }
 			}
 			if (numberOfOnes < actorLength){
 				return true;
 			}
 			else{
 				return false;
 			}
 		}
 	}
 	
 	/**
 	 * Creates a random scenario by the given rounds and actor length.
 	 */
 	public static Scenario random(int rounds, int actorLength){
 		int[] actors = new int[actorLength];
		for (int i=0; i<actorLength; i++){
			actors[i] = random.nextInt(2);
		}	
 		return new Scenario(rounds, actors);
 	}
 }
