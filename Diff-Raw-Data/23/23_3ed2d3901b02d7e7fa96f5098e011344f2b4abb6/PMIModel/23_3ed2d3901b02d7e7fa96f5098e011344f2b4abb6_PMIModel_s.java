 package cs224n.wordaligner;  
 
 import cs224n.util.*;
 import java.util.List;
 
 /**
  * Simple word alignment baseline model that maps source positions to target 
  * positions along the diagonal of the alignment grid..
  * 
  * @author Daniel Chia
  * @author Frank Chen
  */
 public class PMIModel implements WordAligner {
 
   private static final long serialVersionUID = 1315751943476440515L;
   
   // TODO: Use arrays or Counters for collecting sufficient statistics
   // from the training data.
   private Counter<String> sourceCounts = new Counter<String>();
   private Counter<String> targetCounts = new Counter<String>();
   private CounterMap<String,String> sourceTargetCounts = new CounterMap<String,String>();
 
   public Alignment align(SentencePair sentencePair) {
     Alignment alignment = new Alignment();
     int numSourceWords = sentencePair.getSourceWords().size();
     int numTargetWords = sentencePair.getTargetWords().size();
     sentencePair.sourceWords.add(NULL_WORD);
 
     for(int n = 0; n < numTargetWords; ++n) {
       String target = sentencePair.getTargetWords().get(n);
       
       double max_p = -1.0;
       int best_m = -1;
 
       for(int m = 0; m < numSourceWords; ++m) {
         String source = sentencePair.getSourceWords().get(m);
        double pfe = sourceTargetCounts.getCount(source, target);
        double pf  = sourceCounts.getCount(source);
        double pe  = targetCounts.getCount(target);
         double p   = 0.0;
 
        p = (pf > 0 && pe > 0) ? pfe / (pf * pe) : 0;
  
         if(p > max_p) {
           best_m = m;
           max_p = p;
         }
       }
 
       if(best_m >= 0 && best_m < numSourceWords) {
         alignment.addPredictedAlignment(best_m, n);
       }
     }
     return alignment;
   }
 
   public void train(List<SentencePair> trainingPairs) {
     for(SentencePair pair : trainingPairs){
       List<String> targetWords = pair.getTargetWords();
       List<String> sourceWords = pair.getSourceWords();
 
       for(String source : sourceWords) 
         sourceCounts.incrementCount(source, 1);
       for(String target : targetWords)
         targetCounts.incrementCount(target, 1);
 
       for(String source : sourceWords){
         for(String target : targetWords){
           sourceTargetCounts.incrementCount(source, target, 1);
         	if(targetWords.size() > sourceWords.size()) {
             sourceTargetCounts.incrementCount(NULL_WORD, target, 1);
            sourceCounts.incrementCount(NULL_WORD, 1);
           }
         }
       }
     }
   }
 }
