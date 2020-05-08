 package hmm;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 public class ForwardAlgorithm {
 	private StateCollection stateCollection;
 	private List<String> outputSencence;
 	private ArrayList< HashMap<State, Double> > cache;
 	
 	private ForwardAlgorithm(final StateCollection hmm, final List<String> output) {
 		stateCollection = hmm;
 		outputSencence = output;
 		
 		cache = new ArrayList< HashMap<State, Double> >();
 	}
 
 	private void addToCache(int index, State qj, double probability) {
 		HashMap<State, Double> stateCache; 
 		
 		stateCache = cache.get(index);
 		if (stateCache == null) {
 			stateCache = new HashMap<State, Double>();
 			cache.set(index, stateCache);
 		}
 			
 		stateCache.put(qj, probability);
 	}
 	
 	private double checkCache(int index, State qj) {
 		HashMap<State, Double> stateCache; 
 		
 		stateCache = cache.get(index);
 		if (stateCache != null)
 			return stateCache.get(qj);
 		
 		return -1;
 	}
 	
 	// Get the probability for a certain element at a certain index
	public double getAlpha(int index, State qj) {
 		assert(index >= 1);
 		assert(index-1 < outputSencence.size()); 
 		
 		double cachedValue = checkCache(index, qj); 
 		if (cachedValue != -1)
 			return cachedValue;
 			
 		
 		if (index == 1) {
 			double probability;
 			probability = stateCollection.startState().nextStateProbability(qj);
 			probability *= qj.wordEmittingProbability(new Word(outputSencence.get(0)));
 			addToCache(index, qj, probability);
 			return probability;			
 		}
 		
		double sum = 0;
 		//double argmax = 1;
 		
 		for (State qi : stateCollection.states.values()) {
 			double value;
			value = getAlpha(index - 1, qj);
 			value *= qi.nextStateProbability(qj);
 			value *= qj.wordEmittingProbability(new Word(outputSencence.get(index - 1)));
			sum += value;
 		}
 		
		addToCache(index, qj, sum);
		return sum;
 	}
 }
