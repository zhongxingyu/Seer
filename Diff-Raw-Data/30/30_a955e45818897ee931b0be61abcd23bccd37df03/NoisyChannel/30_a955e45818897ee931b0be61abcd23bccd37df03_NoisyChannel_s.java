 package com.github.tteofili.nlputils;
 
 /**
 * Add javadoc here
  */
 public abstract class NoisyChannel {
 
     private String[] dictionary;
 
     public void initializeDictionary(String[] dictionary) {
         this.dictionary = dictionary;
     }
 
     public String findCorrection(String mispelledWord) {
         Double val = 0d;
         String correctWord = null;
         for (String word : dictionary) {
             Double curVal = calculateLikelihood(mispelledWord, word) * calculatePrior(word);
             if (curVal > val) {
                 val = curVal;
                 correctWord = word;
             }
         }
         return correctWord;
     }
 
     public abstract Double calculatePrior(String word);
 
     public abstract Double calculateLikelihood(String mispelledWord, String word);
 
 }
