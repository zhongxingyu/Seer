 package edu.toronto.cs.cute;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 public class Cute extends Classifier {
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public Cute(Map<String, Map<String, Double>> weightedTerms) {
 	  super(weightedTerms);
   }
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
   public List<String> classify(String payload) {
     double maxSimilarity = -1.0;
     List<String> bestProtocols = new LinkedList<String>();
    
     for (Map.Entry<String, Map<String, Double>> wEntry :
     		this.weightedTerm.entrySet()) {
     	String protocol = wEntry.getKey();
     	Map<String, Double> weightedTerms = wEntry.getValue();
     	double sumOfWeights = 0;
  			int matchedTermCount = 0;
  			for (Map.Entry<String, Double> weightedTerm : weightedTerms.entrySet()) {
  				if (payload.contains(weightedTerm.getKey())) {
  					sumOfWeights += weightedTerm.getValue();
 					matchedTermCount += 1;
  				}
  			}
     	
  			if (matchedTermCount > 0) {
  				double similarity = sumOfWeights / matchedTermCount;
  				if (similarity == maxSimilarity) { 
  					bestProtocols.add(protocol);
  				} else if (similarity > maxSimilarity) {
  					bestProtocols = new LinkedList<String>();
  					bestProtocols.add(protocol);
  					maxSimilarity = similarity;
  				}
  			}
     }
 
     return bestProtocols;
   }
 }
