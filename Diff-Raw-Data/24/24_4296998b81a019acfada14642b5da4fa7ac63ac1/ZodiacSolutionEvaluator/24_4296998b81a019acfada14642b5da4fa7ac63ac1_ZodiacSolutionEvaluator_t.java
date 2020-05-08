 package com.ciphertool.zodiacengine.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Required;
 
 import com.ciphertool.zodiacengine.dao.CipherDao;
 import com.ciphertool.zodiacengine.dao.SolutionDao;
 import com.ciphertool.zodiacengine.entities.Cipher;
 import com.ciphertool.zodiacengine.entities.Ciphertext;
 import com.ciphertool.zodiacengine.entities.Plaintext;
 import com.ciphertool.zodiacengine.entities.Solution;
 
 public class ZodiacSolutionEvaluator implements SolutionEvaluator {
 
 	private static Logger log = Logger.getLogger(ZodiacSolutionEvaluator.class);
 	Cipher cipher;
 	HashMap<String, List<Ciphertext>> ciphertextKey;
 	private int confidenceThreshold;
 	private int uniqueMatchThreshold;
 	private int adjacencyThreshold;
 	private SolutionDao solutionDao;
 	
 	/**
 	 * @param cipherName
 	 * @param cipherDao
 	 */
 	public ZodiacSolutionEvaluator(String cipherName, CipherDao cipherDao) {
 		cipher = cipherDao.findByCipherName(cipherName);
 		ciphertextKey = createKeyFromCiphertext();
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.ciphertool.zodiacengine.util.SolutionEvaluator#determineConfidenceLevel(com.ciphertool.zodiacengine.entities.Solution)
 	 * 
 	 * Calculates the confidence level as the number of instances that a ciphertext character has the same plaintext character mapped 
 	 * to it.  If a ciphertext character has multiple matches, then select the plaintext character with the most matches (or if 
 	 * there's a tie, then the first one wins).
 	 */
 	@Override
 	public int determineConfidenceLevel(Solution solution) {
 		Plaintext plaintext = null;
 		int total = 0;
 		int totalUnique = 0;
 		int maxMatches = 0;
 		String bestMatch = null;
 		boolean uniqueMatch = false;
 		String currentValue = null;
 		List<Plaintext> plaintextCharacters = solution.getPlaintextCharacters();
 		Map<String, List<Plaintext>> plaintextMatchMap;
 		
 		/*
 		 * Iterate for each List of occurrences of the same Ciphertext
 		 */
 		for (List<Ciphertext> ciphertextIndices : ciphertextKey.values()) {
 			maxMatches = 0;
 			uniqueMatch = false;
 			bestMatch = null;
 			plaintextMatchMap = new HashMap<String, List<Plaintext>>();
 			
 			/*
 			 * Now iterate for each occurrence of the current Ciphertext character
 			 */
 			for (Ciphertext ciphertextIndice : ciphertextIndices) {
 				/*
 				 *  This just returns the Plaintext character that corresponds to the given Ciphertext character.
 				 *  The usage of List.get() assumes that the ArrayList is properly sorted by CiphertextId
				 *  
				 *  We could also make this into a map with the ciphertextId as the key.  Then we would no 
				 *  longer have to worry about order and or subtracting one from the id.  It does come with a 
				 *  performance hit though.
 				 */
 				plaintext = plaintextCharacters.get(ciphertextIndice.getCiphertextId().getId()-1);
 				
 				currentValue = plaintext.getValue();
 				
 				if (!plaintextMatchMap.containsKey(currentValue)) {
 					plaintextMatchMap.put(currentValue, new ArrayList<Plaintext>());
 				}
 				else {
 					uniqueMatch = true;
 				}
 				
 				plaintextMatchMap.get(currentValue).add(plaintext);
 				
 				if (plaintextMatchMap.get(currentValue).size() > maxMatches) {
 					/*
 					 * Subtract by one when setting maxMatches so that a match on just a pair does not count as two matches.
 					 */
 					maxMatches = plaintextMatchMap.get(currentValue).size() - 1;
 					
 					bestMatch = currentValue;
 				}
 			}
 			
 			/*
 			 * If there was a match on this Ciphertext, set the hasMatch property to true on all the Plaintext matches.
 			 * Use the bestMatch value so that only the Plaintext with the optimal number of matches is set. 
 			 */
 			if (bestMatch != null) {
 				for (Plaintext pt : plaintextMatchMap.get(bestMatch)) {
 					pt.setHasMatch(true);
 				}
 			}
 			
 			/*
 			 * Add the Plaintext matches on this Ciphertext character to the overall confidence value, represented by total
 			 */
 			total += maxMatches;
 			
 			/*
 			 * Increment the unique matches by converting a boolean to an int
 			 */
 			totalUnique += (uniqueMatch ? 1 : 0);
 		}
 		solution.setConfidence(total);
 		solution.setUniqueMatches(totalUnique);
 		
 		boolean countAdjacent = false;
 		int adjacentMatchCount = 0;
 		for (Ciphertext ct : cipher.getCiphertextCharacters()) {
			if(countAdjacent == false && plaintextCharacters.get(ct.getCiphertextId().getId()-1).getHasMatch()) {
 				countAdjacent = true;
 			}
			else if (countAdjacent == true && plaintextCharacters.get(ct.getCiphertextId().getId()-1).getHasMatch()) {
 				adjacentMatchCount ++;
 			}
 			else {
 				countAdjacent = false;
 			}
 		}
 		
 		solution.setAdjacentMatchCount(adjacentMatchCount);
 		
 		log.debug("Solution " + solution.getId() + " has a confidence level of: " + total);
 		
 		if (solution.getConfidence() >= confidenceThreshold) {
 			log.info("Found solution with confidence: " + solution.getConfidence() + ".  Persisting to solution table.");
 			solutionDao.insert(solution);
 		}
 		else if (solution.getUniqueMatches() >= uniqueMatchThreshold) {
 			log.info("Found solution with " + solution.getUniqueMatches() + " unique matches.  Persisting to solution table.");
 			solutionDao.insert(solution);
 		}
 		else if (solution.getAdjacentMatchCount() >= adjacencyThreshold) {
 			log.info("Found solution with " + solution.getAdjacentMatchCount() + " adjacent matches.  Persisting to solution table.");
 			solutionDao.insert(solution);
 		}
 		
 		return total;
 	}
 	
 	/**
 	 * Creates a map with the key as the String value of the Ciphertext character and the value as a List of occurrences within the cipher
 	 * 
 	 * There's no reason to run this for every single iteration of the validator since the ciphertext is not going to change during a run
 	 * 
 	 * @return
 	 */
 	public HashMap<String, List<Ciphertext>> createKeyFromCiphertext() {
 		HashMap<String, List<Ciphertext>> ciphertextKey = new HashMap<String, List<Ciphertext>>();
 		for (Ciphertext ct : cipher.getCiphertextCharacters()) {
 			if (!ciphertextKey.containsKey(ct.getValue())) {
 				ciphertextKey.put(ct.getValue(), new ArrayList<Ciphertext>());
 			}
 			ciphertextKey.get(ct.getValue()).add(ct);
 		}
 		return ciphertextKey;
 	}
 
 	/**
 	 * @param confidenceThreshold the confidenceThreshold to set
 	 */
 	@Required
 	public void setConfidenceThreshold(int confidenceThreshold) {
 		this.confidenceThreshold = confidenceThreshold;
 	}
 
 	/**
 	 * @param uniqueMatchThreshold the uniqueMatchThreshold to set
 	 */
 	@Required
 	public void setUniqueMatchThreshold(int uniqueMatchThreshold) {
 		this.uniqueMatchThreshold = uniqueMatchThreshold;
 	}
 
 	/**
 	 * @param solutionDao the solutionDao to set
 	 */
 	@Required
 	public void setSolutionDao(SolutionDao solutionDao) {
 		this.solutionDao = solutionDao;
 	}
 
 	/**
 	 * @param adjacencyThreshold the adjacencyThreshold to set
 	 */
 	@Required
 	public void setAdjacencyThreshold(int adjacencyThreshold) {
 		this.adjacencyThreshold = adjacencyThreshold;
 	}
 }
