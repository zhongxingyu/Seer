 package hmm;
 
 import java.util.HashMap;
 import java.util.List;
 
 public class ForwardBackwardAlgorithm {
 	private final StateCollection stateCollection;
 	private final List<String> outputSencence;
 	private final HashMap<Integer, HashMap<State, Double>> cache;
 	private final Boolean isForwardAlgorithm;
 
 	public ForwardBackwardAlgorithm(final StateCollection hmm, final List<String> output, Boolean isForward) {
 		stateCollection = hmm;
 		outputSencence = output;
 
 		cache = new HashMap<Integer, HashMap<State, Double>>();
 		isForwardAlgorithm = isForward;
 	}
 
 	private void addToCache(int index, State qj, double probability) {
 		HashMap<State, Double> stateCache;
 
 		stateCache = cache.get(index);
 
 		if (stateCache == null)
 			stateCache = new HashMap<State, Double>();
 		cache.put(index, stateCache);
 
 		stateCache.put(qj, probability);
 	}
 
 	private double checkCache(int index, State qj) {
 		HashMap<State, Double> stateCache;
 
 		stateCache = cache.get(index);
 
		if (stateCache != null)
			return stateCache.get(qj);
		else
 			return -1;
 	}
 
 	// TODO: Check if this is the same for forward and backward!
 	public double getFinalProbability() {
 		// TODO cache that function?
 
 		double sum = 0;
 
 		for (State qi : stateCollection.states.values()) {
 			double value;
 			if (isForwardAlgorithm) {
 				value = getAlphaBeta(outputSencence.size() - 1, qi);
 				value *= qi.nextStateProbability(stateCollection.endState());
 			} else {
 				value = getAlphaBeta(0, qi);
 				value *= stateCollection.startState().nextStateProbability(qi);
 				value *= qi.wordEmittingProbability(stateCollection
 						.getWord(outputSencence.get(0)));
 			}
 			sum += value;
 		}
 		return sum;
 	}
 
 	// Get the probability for a certain element at a certain index
 	// Index "0" state "q3" means: Probability of getting from start state to
 	// q3 and outputting word0
 	public double getAlphaBeta(int index, State qj) {
 		assert (index >= 0);
 		assert (index < outputSencence.size());
 
 		double cachedValue = checkCache(index, qj);
 		if (cachedValue != -1)
 			return cachedValue;
 
 		if (isForwardAlgorithm) {
 			if (index == 0) {
 				double probability;
 				probability = stateCollection.startState().nextStateProbability(qj);
 				probability *= qj.wordEmittingProbability(stateCollection
 						.getWord(outputSencence.get(0)));
 
 				addToCache(index, qj, probability);
 				return probability;
 			}
 		} else {
 			if (index == outputSencence.size() - 1) {
 				double probability;
 				probability = qj.nextStateProbability(stateCollection.endState());
 				addToCache(index, qj, probability);
 				return probability;
 			}
 		}
 
 		double sum = 0;
 
 		for (State qi : stateCollection.states.values()) {
 			double value;
 			if (isForwardAlgorithm) {
 				value = getAlphaBeta(index - 1, qi);
 				value *= qi.nextStateProbability(qj);
 				value *= qj.wordEmittingProbability(stateCollection
 						.getWord(outputSencence.get(index)));
 			} else {
 				value = getAlphaBeta(index + 1, qi);
 				value *= qi.nextStateProbability(qj);
 				value *= qi.wordEmittingProbability(stateCollection
 						.getWord(outputSencence.get(index)));
 			}
 			sum += value;
 		}
 
 		addToCache(index, qj, sum);
 		return sum;
 	}
 }
