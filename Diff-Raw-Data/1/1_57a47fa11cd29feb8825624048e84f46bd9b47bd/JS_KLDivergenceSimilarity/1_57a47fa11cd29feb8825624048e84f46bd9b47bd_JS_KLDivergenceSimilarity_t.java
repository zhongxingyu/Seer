 package edu.umass.ciir.models;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 public class JS_KLDivergenceSimilarity implements LanguageModelSimilarity {
     
     public JS_KLDivergenceSimilarity() {
     }
 
     /**
      * This calculates the Jensen-Shannon KL Divergence: 0.5 * KL(P1||P_both) + 0.5 * KL(P2||P_both), 
      * where P_both= 0.5 (P1+P2).
      *
      * @param lm1 The first language model
      * @param lm2 The second language model
      */
     public SimilarityMeasure calculateSimilarity(LanguageModel lm1, LanguageModel lm2, boolean useProbabilities) {
 
     	double divergence1 = Math.abs(calculateCorePart(lm1, lm2, useProbabilities));
     	double divergence2 = Math.abs(calculateCorePart(lm2, lm1, useProbabilities));
     	
     	SimilarityMeasure sm = new SimilarityMeasure((0.5 * divergence1) + (0.5 * divergence2), "No information found");
     	return sm;
     }
     
     public double calculateCorePart(LanguageModel lm1, LanguageModel lm2, boolean useProbabilities) {
  
     	double p1, p2, p_both;
 
     	Collection<TermEntry> vocab1 = lm1.getVocabulary();
     	double total1 = (double) lm1.getCollectionFrequency();
     	double total2 = (double) lm2.getCollectionFrequency();    	
     	double divergence = 0.0;
 
     	Iterator<TermEntry> vIter1 = vocab1.iterator();
     	while (vIter1.hasNext()) {
     		TermEntry te1 = vIter1.next();
     		TermEntry te2 = lm2.getTermEntry(te1.getTerm());
     		if(useProbabilities) {
     			p1 = te1.getProbability();
 		}    		
     		else {
     			p1 = ((double) te1.getFrequency()) / total1;
     		}
     		if (te2 != null) {
     			if(useProbabilities) {
     				p_both = (p1 + te2.getProbability()) / 2;
     			}
     			else {
     				p2 = (double) te2.getFrequency() / total2;
     				p_both = (p1 + p2) / 2;
     			}
     		} else {
     			p_both = (p1 + (1.0 / total2)) / 2;
     		}
     		double logPart = Math.log(p1 / p_both);
     		divergence += p1 * logPart;
     	}
     	
     	return divergence;   
     }
 }
