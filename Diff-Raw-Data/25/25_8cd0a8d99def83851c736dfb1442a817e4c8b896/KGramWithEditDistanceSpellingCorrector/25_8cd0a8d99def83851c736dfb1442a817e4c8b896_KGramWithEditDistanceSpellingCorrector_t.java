 package cs276.pe1.spell;
 
 import java.util.List;
 import java.util.Set;
 
 import cs276.util.StringUtils;
 import cs276.util.Counter;
 import cs276.util.Counters;
 
 public class KGramWithEditDistanceSpellingCorrector extends KGramSpellingCorrector {
     public enum ListCompare { 
         BASIC, //from the jaccard top 10, finds the one with the smallest edit distance (and highest jaccard score)
         TIEBREAKING, //uses word frequency to break ties
         NORMALIZE //Normalizes then multiplies score and edit distance
     };
    
    //Change this to change the type of corrections returned
    protected static ListCompare correctionsType = ListCompare.NORMALIZE;
 	public KGramWithEditDistanceSpellingCorrector() {
         super();
 	}
 
     protected Counter<String> editDistance(String word, List<String> potentialWords)
     {
         Counter<String> editDistance = new Counter<String>();
         for(String s:potentialWords)
             editDistance.incrementCount(s,StringUtils.levenshtein(word,s));
         return editDistance;
     }
     
     protected void reciprocal(Counter<String> target) {
 		for (String key : target.keySet()) {
 			target.setCount(key, 1.0/((double)target.getCount(key)));
 		}
 	}
     
 	public List<String> corrections(String word) {
         Counter<String> scoredCounter = jaccardScore(word);
         List<String> firstPass = scoredCounter.topK(WL);
         
         if(correctionsType == ListCompare.BASIC||correctionsType == ListCompare.TIEBREAKING)
         {
             Counter<String> editDistance = editDistance(word,firstPass);
             Set<String> minKeys = Counters.keysAt(editDistance,Counters.min(editDistance));
             
             Counter<String> smallestEDJaccard = new Counter<String>();
             for(String s:minKeys)
                 smallestEDJaccard.incrementCount(s,scoredCounter.getCount(s));
             if(correctionsType == ListCompare.TIEBREAKING)
             {
                 Set<String> minKeys2 = Counters.keysAt(smallestEDJaccard,Counters.min(smallestEDJaccard));
                 if(minKeys2.size()>1)
                 {
                     Counter<String> tie = new Counter<String>();
                     for(String s:minKeys2)
                         tie.incrementCount(s,freq.getCount(s));
                     return tie.topK(WL);
                 }
             }
             return smallestEDJaccard.topK(WL);
         }
         else if(correctionsType == ListCompare.NORMALIZE)
         {
             Counter<String>editDistance = editDistance(word,firstPass);
         
             Counters.retainTop(scoredCounter,WL);
             Counters.normalize(scoredCounter);
             Counters.normalize(editDistance);
         
             reciprocal(editDistance);
             Counters.multiplyInPlace(scoredCounter,editDistance);
         
             return scoredCounter.topK(WL);
         }
         return firstPass;
 	}
 }
