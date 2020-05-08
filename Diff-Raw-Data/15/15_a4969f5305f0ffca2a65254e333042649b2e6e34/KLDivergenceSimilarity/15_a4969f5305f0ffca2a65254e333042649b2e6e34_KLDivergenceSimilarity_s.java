 package edu.umass.ciir.models;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 public class KLDivergenceSimilarity implements LanguageModelSimilarity {
     
     private double absoluteSmoothingCnt = 0.1;
 	private double dirichletSmoothingParam;
 	private final LanguageModel background;
 
 	/**
 	 * Constructor
 	 */
     
     public KLDivergenceSimilarity(LanguageModel background, double smoothingParam) {
     	this.background = background;
     	dirichletSmoothingParam = smoothingParam;
     }
 
     
     /**
      * This iterates over the vocabulary of the first argument passed in, and produces a Kullback-Leibler
      * divergence measure. This is mathematically equivalent to KL(lm1 || lm2)
      *
      * @param lm1 The language model to compare against the reference language model.
      * @param lm2 The reference language model. 
      */
     public SimilarityMeasure calculateSimilarity(LanguageModel lm1, LanguageModel lm2, boolean useProbabilities) {
     	double p1, p2;
 
     	Collection<TermEntry> vocab = lm1.getEntries();
     	double total1 = (double) lm1.getCollectionFrequency();
     	
     	if (lm2.getCollectionFrequency() == 0) {
     		return new SimilarityMeasure(Double.MAX_VALUE, "No information found");
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
 				bgTerm = new TermEntry(te1.getTerm(), 1, 1);
 			} 
 			
			if (bgTerm.getProbability() <  (1 / background.getCollectionFrequency())) {
                bgTerm.setProbability(0.5 / background.getCollectionFrequency());
             }
 			
     		if (useProbabilities) {
     			p1 = te1.getProbability();
     		} else {
     			p1 = ((double) te1.getFrequency() 
                        + (dirichletSmoothingParam * bgTerm.getProbability())) 
                         / (dirichletSmoothingParam + total1);
     		}
     		if (te2 != null) {
     			if (useProbabilities) {
     				p2 = te2.getProbability();
     				// avoid 0 prob by using a small value
     				if (p2 < 0.0000001) {
     					p2 = absoluteSmoothingCnt / total2;
     				}
     			} else {
     				p2 = ((double) te2.getFrequency() 
    						+ (dirichletSmoothingParam * bgTerm.getProbability())) 
     						/ (dirichletSmoothingParam + total2);
     			}
     		} else {
     		    if (useProbabilities) {
     		        p2 = absoluteSmoothingCnt / total2;
     		    } else {
    		        p2 = (dirichletSmoothingParam * bgTerm.getProbability()) 
     		                / (dirichletSmoothingParam + total2);
     		    }
     		}
     		double logPart = Math.log(p1 / p2);
     		double contrib = p1 * logPart;
     		//System.out.println(te1.getTerm() + " " + contrib);
     		divergence += contrib;
     	}
 
     	SimilarityMeasure sm = new SimilarityMeasure(divergence, "No information found");
     	return sm;
     }
 }
