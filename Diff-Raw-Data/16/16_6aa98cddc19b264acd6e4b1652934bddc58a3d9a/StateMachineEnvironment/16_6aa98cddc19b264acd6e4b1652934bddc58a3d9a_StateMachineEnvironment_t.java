 package passphraseReplacement;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 
 /**
  * <!-- class StateMachineEnvironment -->
  * 
  * An environment in which the agent can make moves
  * in an attempt to change it's state and reach the 
  * goal state. The agent will make moves and if a chosen
  * move brings the agent to a new state, it's sensor will 
  * be true, if not it will be false. The agent is also able
  * to sense if it has reached the desired goal state.
  * 
  * @author Hailee Kenney
  * @author Preben Ingvaldsen
  * 
  * @version September 27, 2013
  *
  */
 public class StateMachineEnvironment {
 	
 	// Instance variables
 	public static int NUM_STATES = 8;
 	public static int GOAL_STATE = NUM_STATES - 1;
 	public static int ALPHABET_SIZE = 6;  //for now, this can't exceed 26
 	public static int NUM_TRANSITIONS = ALPHABET_SIZE/2;
 
	 //These are used as indexes into the the sensor array
 	private static final int IS_NEW_STATE = 0;
 	private static final int IS_GOAL = 1;
 
     
 	private int[][] transition;
 	private char[] alphabet;
 	private String[] paths;
 	public int currentState;
 	
 	//DEBUG
 	private boolean debug = false;
 
 	
 	public StateMachineEnvironment() {
 		paths = new String[NUM_STATES];
 		paths[GOAL_STATE] = "";
 		fillAlphabet();
 		currentState = 0;
 		generateStateMachine();
 		if(debug){
 			printStateMachine();
 		}
 		
 		findShortestPaths();
 		if (debug) {
 			System.out.println("Shortest Path: " + paths[0]);
 		}
 	}
 
     /**
      * fills the alphabet array with ALPHABET_SIZE characters
      *
      * In the future, it'd be nice to handle sizes greater than 26.  Right now
      * that's the max.
      */
     void fillAlphabet() {
         alphabet = new char[ALPHABET_SIZE];
         for(int i = 0; i < alphabet.length; ++i) {
             char next = (char)('a' + i);
             alphabet[i] = next;
         }
     }
 
     /**
      * initializes the list of shortest paths to nulls again so that they can be
      * recalcualted by findShortestPath
      */
     private void initPaths()
     {
         for(int i = 0; i < paths.length-1; ++i)
         {
             paths[i] = null;
         }
         paths[paths.length - 1] = "";
     }
     
 	/**
 	 * A helper method to generate a random state machine environment
 	 */
 	private void generateStateMachine() {
 		
 		//Create the transition table for our state machine. Each state has a
 		//numerical designation. We index into the array using the number of the
 		//state we are transitioning from, then the numerical index of the
 		//alphabetical character being read
 		transition = new int[NUM_STATES][alphabet.length];
 		Random random = new Random();
 		int charToTransition;
 
        	// //DEBUG
 		// System.out.println(transition[0][0]);
 		
 		//Initialize all the values to -1 so we can tell if there's a transition
 		//there or not (since 0 is a valid state to transition to, and the array
 		//will initially consist of all 0s)
 		for (int i = 0; i < NUM_STATES; i++) {
 			for (int j = 0; j < transition[i].length; j++) {
 				transition[i][j] = -1;
 			}
 		}
 		
 		//Iterate through each row of the Transition Table so we can set the
 		//transitions out of each state in the state machine
 		for (int i = 0; i < NUM_STATES; i++) {
 			
 			//Generate a number of transitions to separate states equal to the
 			//number of transitions previously set
 			for (int j = 0; j < NUM_TRANSITIONS; j++) {
 				
 				//Randomly generate a character to transition on
 				charToTransition = random.nextInt(transition[i].length);
 				
 				//if there is already a transition for that character, generate a new character to transition on
 				if (transition[i][charToTransition] != -1) {
 					j--;
 					continue;
 				}
 				
 				//Randomly select a state to transition to that is not the
 				//current state, then set the transition from the current state
 				//on the randomly generated character to the randomly generated
 				//state
 				int nextState = random.nextInt(NUM_STATES);
 				
 				while(nextState == i) {
 					nextState = random.nextInt(NUM_STATES);
 				}
 				
 				transition[i][charToTransition] = nextState;
 				
 			}
 			
 			//For all characters for the current state that do not have a
 			//transition, set the transition equal to current state
 			for(int j = 0; j < transition[i].length; j++) {
 				if(transition[i][j] == -1){
 					transition[i][j] = i;
 				}
 			}
 		}		
 	}
 	
 	 /**
      * A method which iterates through and prints out
      * the two-dimension array that represents the state machine
      */
     public void printStateMachine() {
         System.out.print("     ");
         for(int i = 0; i < ALPHABET_SIZE; ++i) {
             System.out.printf("%3c", alphabet[i]);
         }
         System.out.println();
 
         for (int i = 0; i < NUM_STATES; i++) {
             System.out.printf("%3d: ", i);
 
             for (int j = 0; j < alphabet.length; j++) {
                 System.out.printf("%3d", transition[i][j]);
             }
             System.out.println();
         }
 
         System.out.print("     ");
         for(int i = 0; i < ALPHABET_SIZE; ++i) {
             System.out.printf("%3c", alphabet[i]);
         }
         System.out.println();
     }
 	
 	/**
 	 * Resets the current state back to the initial state
 	 */
 	private void reset() {
 		currentState = 0;
 	}
 	
 	/**
 	 * A method which takes in a move from the agent and updates
 	 * the current state and the agent's sensors if needed.
 	 * 
 	 * @param move
 	 * 		The move the agent is making
 	 * @return
 	 * 		The agent's updated sensors
 	 */
 	public boolean[] tick(char move) {
 		// An array of booleans to keep track of the agents
 		// two sensors
 		boolean[] sensors = {false, false};
 		int newState = transition[currentState][findAlphabetIndex(move)];
 		
 		// If the attempted letter brings us to a new state
 		// update the current state and the new state sensor
 		if(newState != currentState){
 			currentState = newState;
 			sensors[IS_NEW_STATE] = true;
 		}
 		
 		// If we have reached the goal, update the goal sensor
 		if(newState == GOAL_STATE){
 			sensors[IS_GOAL] = true;
 			reset();
 		}
 		
 		return sensors;
 	}
 	
 	/**
 	 * A helper method which determines a given letter's
 	 * location in the alphabet for the tick method
 	 * 
 	 * @param letter
 	 * 		The letter who's index we wish to find
 	 * @return
 	 * 		The index of the given letter (or -1 if the letter was not found)
 	 */
 	private int findAlphabetIndex(char letter) {
 		// Iterate the through the alphabet to find the index of letter
 		for(int i = 0; i < alphabet.length; i++){
 			if(alphabet[i] == letter)
 				return i;
 		}
 		
 		// Error if letter is not found
 		return -1;
 	}
 	
 	/**
 	 * A helper method which checks if one state has a transition to another
 	 * @param fromState The state to transition from
 	 * @param toState The state to transition to
 	 * @return The index into the alphabet array of the character fromState reads to transition to toState,
 	 * 			or -1 if no such character exists
 	 */
 	private int hasTransition(int fromState, int toState) {
 		for (int i = 0; i < transition[fromState].length; i++) {
 			if (transition[fromState][i] == toState) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * A helper method that generates the shortest path to the goal from each
 	 * state using Dijkstra's algorithm
 	 */
 	private void findShortestPaths() {
 
         initPaths();
         
 		//Create a queue and add the Goal State to the queue
 		ArrayList<Integer> queue = new ArrayList<Integer>();
 		queue.add(GOAL_STATE);
 		int currState;
 		int transitionChar;
 		
 		
 		while (!queue.isEmpty()) {
 			//Grab the element at the front of the queue
 			currState = queue.get(0);
 			queue.remove(0);
 			
 			//Move through each state that doesn't have a path yet. Find the
 			//transition from that state to the current state.
 			for (int i = 0; i < NUM_STATES; i++) {
 
                 //skip the ones that have a path
                 if (paths[i] != null) continue;
                 
 				transitionChar = hasTransition(i, currState);
 				
 				//If state i has a transition to the current state and has no
 				//path, set the path for state i equal to the transition
 				//character from state i to the current state added to the front
 				//of the shortest path to the current state, and add state i
 				//onto the queue.
 				if (transitionChar != -1) {
 					paths[i] = alphabet[transitionChar] + paths[currState];
 					queue.add(i);
 				}
 			}
 
        		 //detect if any state does not have a path to the goal
 			boolean noPath = false;
 			for(int i = 0; i < paths.length; i++){
 				if(paths[i] == null){
 					noPath = true;
 				}
 			}
 
            		//If there is a state with no path to the goal, we have a bum state
            		//machine. Regenerate and try again
 			if(queue.size() == 0 && noPath){
 				generateStateMachine();
 				findShortestPaths();  //recurse
 				return;
 			}
 		}
 
        	//DEBUG
 		// if (debug) {
 		// 	printPaths();
 		// }
 	}
 	
 	/**
 	 * A helper method that prints the shortest path from each state to the goal.
 	 */
 	public void printPaths() {
 		System.out.println("Paths: ");
 		for (String path : paths) {
 			System.out.println(path);
 		}
 	}
 	
 	public String[] getPaths() {
 		return paths;
 	}
 
     public char[] getAlphabet() {
         return alphabet;
     }
     
 }
