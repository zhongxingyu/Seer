 package hmm;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 public class StateCollection {
 	
 	HashMap<String, State> states;
 	HashSet<String> state_names;
 	
 	public State startTag() {
 		return states.get("");
 	}
 	
 	public StateCollection() {
 		states = new HashMap<String, State>();
 		state_names = new HashSet<String>();
 	}
 
 	public void addStateTansitionObservation(String word, String stateString,
 			String previousStateString) {
 		if (previousStateString != "")
 			state_names.add(previousStateString);
 		
 		State previousState;
 		State state;
 		
 		// load previous state
 		if (!states.containsKey(previousStateString)) {
 			previousState = new State(previousStateString);
 			states.put(previousStateString, previousState);
 		} else {
 			previousState = states.get(previousStateString);
 		}
 		
 		// load current state
 		if (!states.containsKey(stateString)) {
 			state = new State(stateString);
 			states.put(stateString, state);
 		} else {
 			state = states.get(stateString);
 		}
 		
 		state.addWordEmissionObservation(word);
 		previousState.addStateTransitionObservation(state.name);
 	}
 
 	public void addFinalStateTransitionObservation(String previousState) {
 		state_names.add(previousState);
 		states.get(previousState).addStateTransitionObservation(null);
 	}
 
 	/**
 	 * Calculate the probability of a sentence and a tag sequence
 	 * @param sentence word/tag pairs which make up the sentence
 	 * @return
 	 */
 	public double calculateProbabilityofSentenceWithStates(
 			ArrayList<String> sentence) {
 		double probability = 1;
 		String old_tag = "";
 		
 		for (String wordPair : sentence) {
 			String[] splitting = wordPair.split("/");
 			String word = splitting[0];
 			String tag = splitting[1];
 
 			// Multiply with tag-to-tag probability
 			probability *= states.get(old_tag).nextStateProbability(tag);
 			// Multiply with tag-to-word probability
 			probability *= states.get(tag).wordEmittingProbability(word);
 			
 			old_tag = tag;
 		}
 		
 		// Multiply with final-tag probability
 		probability *= states.get(old_tag).nextStateProbability(null);
 		return probability;
 	}
 	
 	public ArrayList<String> calculateStatesofSentence(
 			ArrayList<String> sentence) {
 		ArrayList<String> result = new ArrayList<String>();
 		
 		HashMap<String, Double> probabilities = new HashMap<String, Double>();
 		
 		String[] splitting = sentence.get(0).split("/");
 		String first_word = splitting[0];
 		
 		// Calculate starting probabilities
 		for (String state : state_names) {
 			double value = states.get("").nextStateProbability(state);
 			value *= states.get(state).wordEmittingProbability(first_word);
 			
 			probabilities.put(state, value);
 		}
 		result.add(getMaxState(probabilities));
 		
 		// Calculate all other probabilities
 		HashMap<String, Double> new_probabilities = new HashMap<String, Double>();
 		
 		for (int i=1; i<sentence.size(); i++) {
 			splitting = sentence.get(1).split("/");
 			String word = splitting[0];
 			
 			for (String state : state_names) {
 				double max_value = 0;
 				
 				for (String previous_state : state_names) {
 					double value = probabilities.get(previous_state);
 					value *= states.get(previous_state).nextStateProbability(state);
 					value *= states.get(state).wordEmittingProbability(word);
 	
 					if (value > max_value)
 						max_value = value;
 				}
 				
 				new_probabilities.put(state, max_value);
 			}
 	
 			result.add(getMaxState(new_probabilities));
 			probabilities = new_probabilities;
 		}
 		
 		return result;
 	}
 
 	private static String getMaxState(HashMap<String, Double> probabilities) {
 		String max_string = "";
 		double max_probability = 0;
 		for (String key : probabilities.keySet()) {
 			double new_probability = probabilities.get(key); 
 			if (new_probability > max_probability) {
 				max_probability = new_probability;
 				max_string = key;
 			}
 		}
 		return max_string;
 	}
 }
