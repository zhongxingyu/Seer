 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.urbancamper.audiobookmarker.text;
 
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  *
  * @author pozpl
  */
 public class LongestSubsequenceFinder {
 
     private static final Integer WORDS_INTERVAL_THRESHHOLD = 3;
 
     private TreeMap<Integer, Integer> findLogestSubsequence(Integer[] fullArray,
             Integer[] subArray) {
 
         int[][] lengths = new int[fullArray.length + 1][subArray.length + 1];
         int[] fullArrayIndexesOfSameWords = new int[subArray.length + 1];
 
         // row 0 and column 0 are initialized to 0 already
 
         for (int i = 0; i < fullArray.length; i++) {
             for (int j = 0; j < subArray.length; j++) {
                 if (fullArray[i] == subArray[j] &&
                        this.isWodsBelongToInterval(fullArrayIndexesOfSameWords, j, i)) {
                     lengths[i + 1][j + 1] = lengths[i][j] + 1;
                     fullArrayIndexesOfSameWords[j] = i;
                 } else {
                     lengths[i + 1][j + 1] =
                             Math.max(lengths[i + 1][j], lengths[i][j + 1]);
                 }
             }
         }
 
         // read the substring out from the matrix
 //        StringBuffer sb = new StringBuffer();
         TreeMap<Integer, Integer> resultBuffer = new TreeMap<Integer, Integer>();
         for (int x = fullArray.length, y = subArray.length;
                 x != 0 && y != 0;) {
             if (lengths[x][y] == lengths[x - 1][y]) {
                 x--;
             } else if (lengths[x][y] == lengths[x][y - 1]) {
                 y--;
             } else {
                 assert fullArray[x - 1] == subArray[y - 1];
 //                sb.append(a.get(x - 1));
                 resultBuffer.put(x-1, y-1);
                 x--;
                 y--;
             }
         }
 
         return resultBuffer;
     }
 
     /**
      * function to decide if words is close enough to be in same sequence
      * @param  longestSequenceWordsOffsets array of word offsets tat matched
      * @param  fullArrayIndex
      * @param  subArrayIndex
      * @return decigion if words is close enough
      */
     private Boolean isWodsBelongToInterval(int[] longestSequenceWordsOffsets, int fullArrayIndex, int subArrayIndex){
         if(subArrayIndex == 0){
             return true;
         }
         Integer subArrayWordsCounter = 0;
         for(Integer wordsLengthsIndex = subArrayIndex -1; wordsLengthsIndex >= 0; wordsLengthsIndex--){
             int subArrayWordOffset = longestSequenceWordsOffsets[wordsLengthsIndex];
             int wordsDistance = fullArrayIndex - subArrayWordOffset;
             if(wordsDistance > 0 && wordsDistance - subArrayWordsCounter < WORDS_INTERVAL_THRESHHOLD){
                 return true;
             }
         }
         return false;
     }
 
     public TreeMap<Integer, Integer> getLongestSubsequenceWithMinDistance(
             Integer[] fullArray,
             Integer[] subArray){
         TreeMap<Integer, Integer> longestSubsequence = this.findLogestSubsequence(fullArray, subArray);
 
         Integer previousFullTextIndex = 0;
         Integer previousFullTextElement = -1;
 
         ArrayList<Integer> fullTextBuffer = new ArrayList<Integer>();
         ArrayList<Integer> subTextBuffer = new ArrayList<Integer>();
         Integer entriesCounter = 0;
         for(Map.Entry<Integer, Integer> fullTextToRecTextMapEntry: longestSubsequence.entrySet()){
             Integer fullTextWordIndex = fullTextToRecTextMapEntry.getKey();
             Integer subTextWordIndex = fullTextToRecTextMapEntry.getValue();
 
             fullTextBuffer.add(fullTextWordIndex);
             subTextBuffer.add(subTextWordIndex);
 
             Integer currentFullTextElement = fullArray[fullTextWordIndex];
 
             for(Integer spanCounter = previousFullTextIndex + 1; spanCounter < fullTextWordIndex; spanCounter++){
                 Integer elementlToCheck = fullArray[spanCounter];
                 if(elementlToCheck == previousFullTextElement){
                     fullTextBuffer.set(previousFullTextIndex, spanCounter);
                 }
             }
             previousFullTextElement = currentFullTextElement;
             previousFullTextIndex = entriesCounter;
             entriesCounter++;
         }
 
         return this.getFullTextSubTextTreeMapFromLists(fullTextBuffer, subTextBuffer);
     }
 
     private TreeMap<Integer, Integer> getFullTextSubTextTreeMapFromLists(ArrayList<Integer> fullTextBuffer,
         ArrayList<Integer> subTextBuffer){
 
         TreeMap<Integer, Integer> resultBuffer = new TreeMap<Integer, Integer>();
         for(Integer elementCounter = 0; elementCounter < fullTextBuffer.size(); elementCounter++){
             resultBuffer.put(fullTextBuffer.get(elementCounter), subTextBuffer.get(elementCounter));
         }
         return resultBuffer;
     }
 }
