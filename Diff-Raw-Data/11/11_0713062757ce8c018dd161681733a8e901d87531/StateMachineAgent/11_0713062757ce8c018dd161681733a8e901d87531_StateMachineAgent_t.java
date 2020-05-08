 package passphraseReplacement;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class StateMachineAgent {
 
 	// Instance variables
 	private Path best;  //best path from init to goal the agent knows atm
 	private StateMachineEnvironment env;
 	private char[] alphabet;
 	private ArrayList<String> episodicMemory;
 
 	//These are used as indexes into the the sensor array
 	private static final int IS_NEW_STATE = 0;
 	private static final int IS_GOAL = 1;
 
 	//Sensor values
 	public static final int NO_TRANSITION = 0;
 	public static final int TRANSITION_ONLY = 1;
 	public static final int GOAL = 2;
 	
 	//Reset limit
 	public static final int MAX_RESETS = 1;
 
 	// Turns debug printing on and off
 	boolean debug = true;
 
 	/**
 	 * The constructor for the agent simply initializes it's instance variables
 	 */
 	public StateMachineAgent() {
 		env = new StateMachineEnvironment();
 		alphabet = env.getAlphabet();
 		episodicMemory = new ArrayList<String>();
 	}
 
 	/**
 	 * Runs through the "Brute Force" algorithm for the agent, setting the
 	 * "best" passphrase to the result
 	 */
 	public void bruteForce() {
 		// Generate an initial path
 		generatePath();
 
 		// DEBUG: try the path that was successful (sanity check)
 		//tryPath(best);
 		//best.printpath();
 
 		// Trim moves off the successful path until we only have the
 		// necessary moves remaining. Make this the new best path
 		best = trimPath(best);
 
 		// // DEBUG: Print out what the agent has determined the shortests path is
 		//best.printpath();
 	}
 
 	/**
 	 * Guesses randomly until a path to the goal is generated
 	 * 
 	 * @return
 	 * 		The path to the goal that was found
 	 */
 	public Path generatePath() {
 		Random random = new Random();
 		char toCheck;
 		ArrayList<Character> pass = new ArrayList<Character>();
 		boolean[] sensors;
 		int encodedSensorResult;
 
 		// Generate random steps until the goal is reached.  Record each
 		// step in 'pass'
 		do {
 			toCheck = alphabet[random.nextInt(alphabet.length)];
 			pass.add(toCheck); // Add current char to the string of characters that defines the path
 			sensors = env.tick(toCheck);
 			encodedSensorResult = encodeSensors(sensors);
 			episodicMemory.add("" + toCheck + encodedSensorResult);
 			
 		} while (!sensors[IS_GOAL]); // Keep going until we've found the goal
 
 		//return the result
 		best = new Path(pass);
 		// best.printpath();   // DEBUG
 		return best;
 	}
 
 	/**
 	 * Given a full string of moves, tryPath will enter the moves
 	 * one by one and determine if the entered path is successful
 	 *
 	 * CAVEAT:  This method returns 'true' even if the goal is reached
 	 * prematurely (before the path has been passed)
 	 *
 	 * @param best
 	 * 		An ArrayList of Characters representing the path to try
 	 * 
 	 * @return
 	 * 		A boolean which is true if the path was reached the goal and
 	 * 		false if it did not
 	 */
 	public boolean tryPath(Path best) {
 		boolean[] sensors;
 		// Enter each character in the path
 		for (int i = 0; i < best.size(); i++) {
 			sensors = env.tick(best.get(i));
 			int encodedSensorResult = encodeSensors(sensors);
 			episodicMemory.add("" + best.get(i) + encodedSensorResult);
 			
 			if (sensors[IS_GOAL]) {
 				//DEBUG
 				//System.out.println("Given path works");
 
 				// If we successfully find the goal, return true
 				return true;
 			}
 		}
 
 		//DEBUG
 		//System.out.println("Given path fails");
 
 		// If we make it through the entire loop, the path was unsuccessful
 		smartReset();
 		return false;
 	}
 
 	/**
 	 * trimPassphrase takes in a passphrase (which has been confirmed as
 	 * successful) and removes one character at a time until it is able to
 	 * determine the shortest version of the passphrase that is still
 	 * successful
 	 * 
 	 * @param toTrim
 	 * 		The passphrase to trim characters from
 	 * @return
 	 * 		toTrim reduced to the least amount of characters possible (not including equivalencies)
 	 */
 	public Path trimPath(Path toTrim) {
 		// Make a copy of the passed-in passphrase so as not to modify it
 		Path trimmed = toTrim.copy();
 		char removed; //Allows us to keep track of the removed character and add it back in if necessary
 
 		for (int i = 0; i < trimmed.size(); i++) {
 			// Trim the current character from the passphrase and test the
 			// result
 			removed = trimmed.get(i);
 			trimmed.remove(i); 
 			if (tryPath(trimmed)) {
 				// If the result is successful, decrement the index, as we
 				// have now no longer seen the element at index i
 				i--;
 			}
 			else {
 				// If the result is unsuccessful, the removed element is an
 				// important character and must be added back in to the
 				// passphrase
 				smartReset();
 				trimmed.add(i, removed);
 			}
 		}
 		return trimmed;
 	}
 
 	/**
 	 * Resets the agent by having it act randomly until it reaches the goal.
 	 * This will be changed to a more intelligent scheme later on
 	 */
 	public void reset() {
 		char toCheck;
 		Random random = new Random();
 		boolean[] sensors;
 		int encodedSensorResult;
 		
 		//Currently, the agent will just move randomly until it reaches the goal
 		//and magically resets itself
 		do {
 			//toCheck = alphabet[random.nextInt(alphabet.length)];
 			toCheck = generateAction();
 			sensors = env.tick(toCheck);
 			encodedSensorResult = encodeSensors(sensors);
 			episodicMemory.add("" + toCheck + encodedSensorResult);
 			
 		} while (!sensors[IS_GOAL]); // Keep going until we've found the goal
 	}
 	
 	/**
 	 * A helper method that generates an action for the dumb reset by matching the
 	 * shortest unseen string and taking the action at the end of that string
 	 * @return A character chosen from random among the valid actions
 	 */
 	private char generateAction() {
 		//The string of actions the Agent can take. This must be populated with
 		//actions at the end of strings the Agent has never seen before
 		ArrayList<Character> validActions = new ArrayList<Character>();
 		
 		//The memory string to match against the episodic memory
 		ArrayList<String> memoryString = new ArrayList<String>();
 		
 		//Add this to the memoryString so that we have something to replace
 		memoryString.add("a");
 		
 		//Our current index into the episodic memory. This is the index of the element
 		//we will add onto the front of the memoryString if the current set of 
 		//string matches fails
 		int currentMemoryBeforeStringIndex = episodicMemory.size() - 1;
 		
 		//The loop should terminate if we get valid actions or if we go beyond
 		//the size of the episodic memory
		while (validActions.isEmpty() && currentMemoryBeforeStringIndex > findLastGoal(episodicMemory.size())) {
 			
 			//For each character in the alphabet, replace the last element in the
 			//memoryString. Match the resulting string with the episodic memory, and
 			//add the current character to the list of Valid Actions if there is a match
 			for (int i = 0; i < alphabet.length; i++) {
 				memoryString.remove(memoryString.size() - 1);
 				memoryString.add("" + alphabet[i]);
 				if (!matchString(memoryString)) {
 					validActions.add(alphabet[i]);
 				}
 			}
 			
 			//Extend the current string and move back in episodic memory
 			memoryString.add(0, episodicMemory.get(currentMemoryBeforeStringIndex));
 			currentMemoryBeforeStringIndex--;
 		}
 		
 		Random random = new Random();
		
		if(validActions.size() == 0){
			return alphabet[random.nextInt(alphabet.length)];
		}
		else {
			return validActions.get(random.nextInt(validActions.size()));
		}
 	}
 	
 	/**
 	 * A helper method that takes in a string of action/result pairs and checks
 	 * if that string exists in the episodic memory
 	 * @param actionString The string to match
 	 * @return True if the string was matched in the episodic memory, otherwise false
 	 */
 	private boolean matchString(ArrayList<String> actionString) {
 		int elementToMatchIndex = 0;
 		for(int i = 0; i < episodicMemory.size(); i++)
 		{
 			if (elementToMatchIndex < actionString.size() - 1) {
 				if (episodicMemory.get(i).equals(actionString.get(elementToMatchIndex))) {
 					elementToMatchIndex++;
 				}
 				else {
 					elementToMatchIndex = 0;
 				}
 			}
 			else {
 				if (episodicMemory.get(i).charAt(0) == actionString.get(elementToMatchIndex).charAt(0)){
 					return true;
 				}
 				else {
 					elementToMatchIndex = 0;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public void smartReset() {
 		/*boolean successCode;
 		for(int i = 0; i < MAX_RESETS; i++) {
 			successCode = smartResetHelper();
 			if (successCode) {
 				return;
 			}
 		}*/
 		reset();
 	}
 	
 	/**
 	 * An intelligent reset method for the agent that resets by searching its previous moves
 	 */
 	public boolean smartResetHelper() {
 		int matchedStringEndIndex = maxMatchedStringIndex();
 		char transitionCharacter;
 		boolean[] sensors;
 		int sensorEncoding;
 		int lastGoal = findLastGoal(episodicMemory.size()) + 1;
 		String action;
 		if (matchedStringEndIndex == -1) {
 			return false;
 		}
 		for (int i = matchedStringEndIndex + 1; i < lastGoal; i++) {
 			transitionCharacter = episodicMemory.get(i).charAt(0);
 			sensors = env.tick(transitionCharacter);
 			sensorEncoding = encodeSensors(sensors);
 			action = "" + transitionCharacter + sensorEncoding;
 			episodicMemory.add(action);
 			if (sensorEncoding == GOAL) {
 				return true;
 			}
 			
 			//System.err.println(episodicMemory.get(i) + " " + action);
 			if (!episodicMemory.get(i).equals(action)) {
 				//We're lost, so attempt another reset
 				return false;
 			}
 		}
 		
 		return false;
 	}
 	
 	private int maxMatchedStringIndex() {
 		int lastGoalIndex = findLastGoal(episodicMemory.size());
 		int maxStringIndex = -1;
 		int maxStringLength = 0;
 		int currStringLength;
 		boolean actionMatched;
 		for (int i = lastGoalIndex; i >= 0; i--) {
 			actionMatched = episodicMemory.get(i).equals(episodicMemory.get(episodicMemory.size() - 1));
 			if (actionMatched) {
 				currStringLength = matchedMemoryStringLength(i);
 				if (currStringLength > maxStringLength) {
 					maxStringLength = currStringLength;
 					maxStringIndex = i;
 				}
 			}
 		}
 		
 		return maxStringIndex;
 	}
 	
 	private int matchedMemoryStringLength(int endOfStringIndex) {
 		int length = 0;
 		int indexOfMatchingAction = episodicMemory.size() - 1;
 		boolean match;
 		for (int i = endOfStringIndex; i >= 0; i--) {
 			match = episodicMemory.get(i).equals(episodicMemory.get(indexOfMatchingAction));
 			if (match) {
 				length++;
 				indexOfMatchingAction--;
 			}
 			else {
 				return length;
 			}
 		}
 		
 		return length;
 	}
 	
 	
 	/**
 	 * Searches backwards through the list of move-result pairs from the given index
 	 * @param toStart The index from which to start the backwards search
 	 * @return The index of the previous goal
 	 */
 	private int findLastGoal(int toStart) {
 		for (int i = toStart - 1; i > 0; i --) {
 			if (episodicMemory.get(i).endsWith("" + IS_GOAL)) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * Takes in an agent's sensor data and encodes it into an integer
 	 * @param sensors The agent's sensor data
 	 * @return the integer encoding of that sensor data
 	 */
 	private int encodeSensors(boolean[] sensors) {
 		int encodedSensorResult;
 		
 		if (sensors[IS_GOAL]) {
 			encodedSensorResult = GOAL;
 		}
 		
 		else if (sensors[IS_NEW_STATE]) {
 			encodedSensorResult = TRANSITION_ONLY;
 		}
 		
 		else {
 			encodedSensorResult = NO_TRANSITION;
 		}
 		
 		return encodedSensorResult;
 	}
 	
 	/**
 	 * runs multiple trials wherein a random state machine is solved and the
 	 * resulting 'best passphrase' for each is analyzed
 	 */
 	public static void main(String [ ] args)
 	{
 		//CSV output heading line
 		System.out.println("NumStates, AlphaSize, NumTransition, AvgBestPath, AvgBestAgentPath, AvgEpisodicMemorySize");
 
 
         for(int numStates = 5; numStates <= 100; ++numStates)
         {
             for(int alphaSize = 5; alphaSize <= 26; alphaSize += 3)
             {
                 for(int numTrans = alphaSize / 2; numTrans <= alphaSize; numTrans += 2)
                 {
                     StateMachineEnvironment.NUM_STATES = numStates;
                     StateMachineEnvironment.GOAL_STATE = numStates - 1;
                     StateMachineEnvironment.ALPHABET_SIZE = alphaSize;
                     StateMachineEnvironment.NUM_TRANSITIONS = numTrans;
 
 					
                     int trials = 25;
                     int pathLengthTotal  = 0;
                     int envPathLengthTotal = 0;
                     int episodicMemorySizeTotal = 0;
                     StateMachineAgent ofSPECTRE;
 
                     for (int i = 0; i < trials; i++) {
                         ofSPECTRE = new StateMachineAgent();
                         ofSPECTRE.bruteForce();
 
                         String [] envPaths = ofSPECTRE.env.getPaths();
 
                         pathLengthTotal += ofSPECTRE.best.size();
                         envPathLengthTotal += envPaths[0].length();
                         episodicMemorySizeTotal += ofSPECTRE.episodicMemory.size();
                     }
 
                     double agentLengthAvg = (double)pathLengthTotal/(double)trials;
                     double envLengthAvg = (double)envPathLengthTotal/(double)trials;
                     double episodicMemorySizeAvg = (double)episodicMemorySizeTotal/(double)trials;
 
                     System.out.printf("%d, %d, %d, %g, %g, %g\n",
                                       numStates, alphaSize, numTrans,
                                       envLengthAvg, agentLengthAvg, episodicMemorySizeAvg);
 
                     // System.out.println("Average shortest path guessed by agent: " + agentLengthAvg);
                     // System.out.println("Average shortest path generated by environment: " + envLengthAvg);
 
                 }
             }
         }
 
 		/*StateMachineAgent ofSPECTRE;
 		ofSPECTRE = new StateMachineAgent();
 		ofSPECTRE.bruteForce();
 
 		String [] envPaths = ofSPECTRE.env.getPaths();
 
 		System.out.print(ofSPECTRE.best);
 		System.out.println("\n\n\n");
 		for (int i = 0; i < ofSPECTRE.episodicMemory.size(); i++) {
 			System.out.println(ofSPECTRE.episodicMemory.get(i));
 		}*/
 	}
 
 }
