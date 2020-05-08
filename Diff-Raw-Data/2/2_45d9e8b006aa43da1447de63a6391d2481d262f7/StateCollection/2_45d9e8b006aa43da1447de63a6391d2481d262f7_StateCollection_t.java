 package hmm;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Random;
 
 public class StateCollection {
 
 	private boolean isFrozen;
 	public HashMap<String, State> states=new HashMap<String, State>();
 	public HashMap<String, Word> words=new HashMap<String, Word>();
 	private Random random;
 
 	public void setWords(HashMap<String, Word> new_words) {
 		words = new_words;
 	}
 
 	private State getStateTraining(String s){
 		if (s==null || s.equals(""))
 			throw new Error("invalid state name");
 
 		// return existing state if available
 		if (states.containsKey(s)){
 			return states.get(s);
 		}
 
 		// create new state
 		State result=new State(s,this);
 		states.put(s,result);
 		return result;
 	}
 
 	public State getState(String s){
 		// return existing state if available
 		if (states.containsKey(s)){
 			return states.get(s);
 		}
 
 		return unknownState();
 	}
 
 	private Word getWordTraining(String s){
 		// return existing word if available
 		if (words.containsKey(s)){
 			return words.get(s);
 		}
 
 		// create new word
 		Word result=new Word(s);
 		words.put(s,result);
 		return result;
 	}
 
 
 	public Word getWord(String s){
 		// return existing word if available
 		if (words.containsKey(s)){
 			return words.get(s);
 		}
 
 		return unknownWord();
 	}
 
 	public State startState() {
 		return getStateTraining("<start>");
 	}
 
 	public State endState() {
 		return getStateTraining("<end>");
 	}
 
 	public State unknownState() {
 		return getStateTraining("<unk>");
 	}
 
 	public Word unknownWord(){
 		return getWordTraining("<unk>");
 	}
 
 	public StateCollection() {
 		// crate start and end state
 		startState();
 		endState();
 		unknownState();
 	}
 
 	public StateCollection(int numStates, HashSet<String> all_words) {
 		super();
 
 		random = new Random();
 
 		// Add states to list
 		for (int i=0; i<numStates; i++) {
 			getStateTraining("<state_" + i + ">");
 		}
 
 		// Add words to list
 		for (String string : all_words) {
 			Word word = new Word(string);
 			words.put(string, word);
 
 			// Set random word output probabilities
 			for (State state : states.values()) {
 				state.setWordEmissionObservations(word, random.nextInt()%1000000);
 			}
 		}
 
 		// Set random transmission probabilities
 		for (State state1 : states.values()) {
 			for (State state2 : states.values()) {
 				state1.setStateTransitionObservation(state2, random.nextInt()%1000000);
 			}
 		}
 
 		freeze();
 	}
 
 	public StateCollection reEstimateProbabilites(ArrayList<ArrayList<String>> trainingSentences) {
 		StateCollection new_collection = new StateCollection();
 
 		// Add states to list
 		for (int i=0; i<states.size(); i++) {
 			new_collection.getStateTraining("<state_" + i + ">");
 		}
 
 		// Add words to list
 		new_collection.setWords(words);
 
 		// Estimate new transition probabilities
 		for (State qi : states.values()) {
 			for (State qj : states.values()) {
 				double ep_qi_qj = 0;
 				for (ArrayList<String> sentence : trainingSentences) {
 					ForwardBackwardAlgorithm forward_algorithm =
 						new ForwardBackwardAlgorithm(new_collection, sentence, true);
 
 					ForwardBackwardAlgorithm backward_algorithm =
 						new ForwardBackwardAlgorithm(new_collection, sentence, false);
 
 					double p_qi_qj_o = 0;
 
					for (int t = 0; t < sentence.size() - 1; t++) {
 						double forward = forward_algorithm.getAlphaBeta(t, qi);
 						double backward = backward_algorithm.getAlphaBeta(t+1, qj);
 
 						p_qi_qj_o += forward *
 						qi.nextStateProbability(qj) *
 						qj.wordEmittingProbability(getWord(sentence.get(t+1))) *
 						backward;
 					}
 
 					double p_o = forward_algorithm.getFinalProbability();
 					double p_qi_qj_when_o = p_qi_qj_o / p_o;
 
 					ep_qi_qj += p_qi_qj_when_o;
 				}
 
 				// COMMENT: The following is not needed, as we normalize automatically
 				// in the get probability function!
 
 				/*double ep_qi = 0;
 				for (ArrayList<String> sentence : trainingSentences) {
 					for (State qj_any : states.values()) {
 						ep_qi +=
 					}
 				}*/
 
 				new_collection.getState(qi.name).setStateTransitionObservation(
 						new_collection.getState(qj.name), ep_qi_qj);
 			}
 		}
 
 		for (State qi : states.values()) {
 			for (Word word : words.values()) {
 				double p_vk_given_qj = 0;
 
 				for (ArrayList<String> sentence : trainingSentences) {
 					ForwardBackwardAlgorithm forward_algorithm =
 						new ForwardBackwardAlgorithm(new_collection, sentence, true);
 
 					ForwardBackwardAlgorithm backward_algorithm =
 						new ForwardBackwardAlgorithm(new_collection, sentence, false);
 
 					double p_vk_qi_o = 0;
 
 					for (int t=0; t<sentence.size(); t++) {
 						if (sentence.get(t).equals(word.name)) {
 							double forward = forward_algorithm.getAlphaBeta(t, qi);
 							double backward = backward_algorithm.getAlphaBeta(t, qi);
 
 							p_vk_qi_o += forward * backward;
 						}
 					}
 
 					double p_o = forward_algorithm.getFinalProbability();
 					double p_vk_qi_when_o = p_vk_qi_o / p_o;
 
 					p_vk_given_qj += p_vk_qi_when_o;
 				}
 
 
 				// COMMENT: Normalization should be done automatically... REALLY?
 				new_collection.getState(qi.name).setWordEmissionObservations(
 						word, p_vk_given_qj);
 			}
 		}
 
 		return new_collection;
 	}
 
 	public void addStateTansitionObservation(String wordString, String stateString, String previousStateString) {
 		if (isFrozen()) throw new Error();
 		State previousState;
 		State state;
 		Word word;
 
 		// load states
 		previousState = getStateTraining(previousStateString);
 		state = getStateTraining(stateString);
 
 		// load word
 		word=getWordTraining(wordString);
 
 		state.addWordEmissionObservation(word);
 		previousState.addStateTransitionObservation(state);
 	}
 
 	public void addFinalStateTransitionObservation(String previousState) {
 		if (isFrozen()) throw new Error();
 		getStateTraining(previousState).addStateTransitionObservation(endState());
 	}
 
 	/**
 	 * Calculate the probability of a sentence and a tag sequence
 	 * @param sentence word/tag pairs which make up the sentence
 	 * @return
 	 */
 	public double calculateProbabilityofSentenceWithStates(ArrayList<String> sentence) {
 		double probability = 1;
 		State lastState = startState();
 
 		for (String wordPair : sentence) {
 			String[] splitting = wordPair.split("/");
 			String wordString = splitting[0];
 			String stateString = splitting[1];
 
 			Word word=getWord(wordString);
 			State state=getState(stateString);
 
 			// Multiply with tag-to-tag probability
 			probability *= lastState.nextStateProbability(state);
 			// Multiply with tag-to-word probability
 			probability *= state.wordEmittingProbability(word);
 
 			lastState = state;
 		}
 
 		// Multiply with final-tag probability
 		probability *= lastState.nextStateProbability(unknownState());
 		return probability;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder builder=new StringBuilder();
 
 		// print transition matrix
 		builder.append(String.format("\t"));
 		for (State column: states.values()){
 			builder.append(String.format("%s\t",column.name));
 		}
 		builder.append(String.format("\n"));
 
 		for (State row: states.values()){
 			builder.append(String.format("%s\t",row.name));
 			for (State column: states.values()){
 				builder.append(String.format("%.2f\t",row.nextStateProbability(column)));
 			}
 			builder.append(String.format("\n"));
 		}
 		builder.append(String.format("\n"));
 
 		// print emission matrix
 		builder.append(String.format("\t"));
 		for (Word column: words.values()){
 			builder.append(String.format("%s\t",column.name));
 		}
 		builder.append(String.format("\n"));
 
 		for (State row: states.values()){
 			builder.append(String.format("%s\t",row.name));
 			for (Word column: words.values()){
 				builder.append(String.format("%.2f\t",row.wordEmittingProbability(column)));
 			}
 			builder.append(String.format("\n"));
 		}
 
 		return builder.toString();
 	}
 
 	public void freeze() {
 		this.isFrozen = true;
 	}
 
 	public boolean isFrozen() {
 		return isFrozen;
 	}
 }
