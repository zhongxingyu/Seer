 package cs224n.wordaligner;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import cs224n.util.Counter;
 import cs224n.util.CounterMap;
 import cs224n.util.Counters;
 
 /**
  * 
  * Implements IBM Model 1
  * 
  * @author Daniel Chia
  * @author Frank Chen
  */
 
 public class IBMModel1 implements WordAligner {
 
   private static final long serialVersionUID = -4028412990771030273L;
   
   // counters to keep track of statistics
   // source is key, target are the values
   // corresponds to t(e|f)
   CounterMap<String, String> probTargetGivenSource = null;
 
   @Override
   public Alignment align(SentencePair sentencePair) {
     Alignment alignment = new Alignment();
     
     List<String> targetWords = sentencePair.getTargetWords();
     for (int targetWordIndex = 0; targetWordIndex < targetWords.size(); targetWordIndex++) {
       String targetWord = targetWords.get(targetWordIndex);
       List<String> sourceWords = sentencePair.getSourceWords();
       
       double maxProb = -1.0;
      int maxProbSourceWordIndex = 0;
       
      for (int sourceWordIndex = 0; sourceWordIndex <= sourceWords.size(); sourceWordIndex++) {
         // extract source word (being careful of null)
         String sourceWord = (sourceWordIndex < sourceWords.size()) ? 
                             sourceWords.get(sourceWordIndex) :
                             WordAligner.NULL_WORD;
         
         double prob = probTargetGivenSource.getCount(sourceWord, targetWord);
         if (prob > maxProb) {
           maxProb = prob;
           maxProbSourceWordIndex = sourceWordIndex;
         }
       }
        
      if (maxProbSourceWordIndex != sourceWords.size())  // skip NULL words
         alignment.addPredictedAlignment(targetWordIndex, maxProbSourceWordIndex);
     }
     
     return alignment;
   }
 
   @Override
   public void train(List<SentencePair> trainingData) {
    System.out.println("Training..");
     
     probTargetGivenSource = new CounterMap<String,String>();
     
     // initialize to uniform
     // and also append NULL word to training data
     for (SentencePair sentencePair: trainingData) {
       List<String> sourceWords = sentencePair.getSourceWords();
       sourceWords.add(WordAligner.NULL_WORD);
       
       for (String sourceWord: sourceWords)
         for (String targetWord: sentencePair.getTargetWords())
           probTargetGivenSource.setCount(sourceWord, targetWord, 1.0);
     }
     // normalize
     probTargetGivenSource = Counters.conditionalNormalize(probTargetGivenSource);
     
     double deviation;
     int numEntries = 0;
     int iteration = 0;
     
     do {
       //
       // perform EM
       //
       
       // E-step
       
       // source is key, target is value
       CounterMap<String, String> countTargetSource = new CounterMap<String,String>();
       
       for (SentencePair sentencePair: trainingData) {
         Counter<String> totalTarget = new Counter<String>();
         // precalculate sum_i t(e^(k)|f^(k)_i)
         for (String targetWord: sentencePair.getTargetWords())
           for (String sourceWord: sentencePair.getSourceWords()) {
             totalTarget.incrementCount(
               targetWord, 
               probTargetGivenSource.getCount(sourceWord, targetWord)
             );
           }
         
         for (String targetWord: sentencePair.getTargetWords())
           for (String sourceWord: sentencePair.getSourceWords()) {
             double update = probTargetGivenSource.getCount(sourceWord, targetWord) / totalTarget.getCount(targetWord);
             countTargetSource.incrementCount(sourceWord, targetWord, update);
           }
       }
       
       // M-step
       // normalize to give new probs
       CounterMap<String, String> newProbTargetGivenSource = Counters.conditionalNormalize(countTargetSource);
       
       
       // calculate convergence criteria
       // based on sum (oldprob - newprob)^2 over all entries in the p(e|f) table
       deviation = 0.0;
       numEntries = 0;
       for (String sourceWord: probTargetGivenSource.keySet())
         for (String targetWord: probTargetGivenSource.getCounter(sourceWord).keySet()) {
           double difference = probTargetGivenSource.getCount(sourceWord, targetWord) -
                               newProbTargetGivenSource.getCount(sourceWord, targetWord);
           deviation += difference * difference;
           numEntries++;
         }
       
       probTargetGivenSource = newProbTargetGivenSource;
       
       iteration++;
       System.out.println("Iteration " + iteration + ": " + deviation);
     }while(deviation > 1e-9 * numEntries);
     
     System.out.println("Training done.");
   }
 }
