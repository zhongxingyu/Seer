 package edu.umass.ciir.models;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import edu.umass.ciir.models.LanguageModel;
 import edu.umass.ciir.models.SimilarityMeasure;
 import edu.umass.ciir.models.TermEntry;
 
 public class JS_KLDivergenceSimilarity implements LanguageModelSimilarity {
     
     
     private double smoothingAlpha;
     private final LanguageModel background;
     
     public JS_KLDivergenceSimilarity(LanguageModel background, double smoothingParam) {
         this.background = background;
         smoothingAlpha = smoothingParam;
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
    	if (divergence1 == Double.POSITIVE_INFINITY) {
    	    divergence1 = Double.MAX_VALUE;
    	}
    	
     	double divergence2 = Math.abs(calculateCorePart(lm2, lm1, useProbabilities));
    	if (divergence2 == Double.POSITIVE_INFINITY) {
            divergence2 = Double.MAX_VALUE;
        }
     	
     	SimilarityMeasure sm = new SimilarityMeasure((0.5 * divergence1) + (0.5 * divergence2), "No information found");
     	return sm;
     }
     
     public double calculateCorePart(LanguageModel lm1, LanguageModel lm2, boolean useProbabilities) {
  
         double p1, p2;
 
         Collection<TermEntry> vocab = lm1.getEntries();
         double total1 = (double) lm1.getCollectionFrequency();
         
         if (lm2.getCollectionFrequency() == 0) {
             return Double.MAX_VALUE;
         }
         
         double total2 = (double) lm2.getCollectionFrequency();
         double divergence = 0.0;
 
         Iterator<TermEntry> vIter = vocab.iterator();
         while (vIter.hasNext()) {
             TermEntry te1 = vIter.next();
             TermEntry te2 = lm2.getTermEntry(te1.getTerm());
             TermEntry bgTerm =  background.getTermEntry(te1.getTerm());
 
             if (bgTerm == null) {
                 System.out.println("Unable to get background model for term: " + te1.getTerm());
             }
             if (useProbabilities) {
                 p1 = te1.getProbability();
             } else {
                 p1 = ((double) te1.getFrequency() / total1);
             }
             if (te2 != null) {
                 if (useProbabilities) {
                     p2 = te2.getProbability();
                     if (p2 < 0.0000001) {
                         p2 = smoothingAlpha / total2;
                     }
                 } else {
                     p2 = ((double) te2.getFrequency() 
                             + (smoothingAlpha * bgTerm.getProbability())) 
                             / (smoothingAlpha + total2);
                 }
             } else {
                 p2 = (smoothingAlpha * bgTerm.getProbability()) 
                 / (smoothingAlpha + total2);
             }
             double logPart = Math.log(p1 / p2);
            double contrib = p1 * logPart;
            divergence += contrib;
            System.out.println(te1.getTerm() + " contrib: " + contrib );
         }
     	return divergence;   
     }
 }
