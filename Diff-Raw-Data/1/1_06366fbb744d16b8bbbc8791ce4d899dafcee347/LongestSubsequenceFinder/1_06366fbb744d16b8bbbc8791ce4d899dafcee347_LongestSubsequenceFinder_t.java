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
 
     private TreeMap<Integer, Integer> findLogestSubsequence(Integer[] fullArray,
             Integer[] subArray) {
 
         int[][] lengths = new int[fullArray.length + 1][subArray.length + 1];
 
         // row 0 and column 0 are initialized to 0 already
 
         for (int i = 0; i < fullArray.length; i++) {
             for (int j = 0; j < subArray.length; j++) {
                 if (fullArray[i] == subArray[j]) {
                     lengths[i + 1][j + 1] = lengths[i][j] + 1;
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
                 resultBuffer.put(x - 1, y - 1);
                 x--;
                 y--;
             }
         }
 
         return resultBuffer;
     }
 
     public TreeMap<Integer, Integer> getLongestSubsequenceWithMinDistance(
             Integer[] fullArray,
             Integer[] subArray) {
         TreeMap<Integer, Integer> longestSubsequence = this.findLogestSubsequence(fullArray, subArray);
 
         Integer previousFullTextIndex = 0;
         Integer previousFullTextElement = -1;
 
         ArrayList<Integer> fullTextBuffer = new ArrayList<Integer>();
         ArrayList<Integer> subTextBuffer = new ArrayList<Integer>();
         Integer entriesCounter = 0;
         for (Map.Entry<Integer, Integer> fullTextToRecTextMapEntry : longestSubsequence.entrySet()) {
             Integer fullTextWordIndex = fullTextToRecTextMapEntry.getKey();
             Integer subTextWordIndex = fullTextToRecTextMapEntry.getValue();
 
             fullTextBuffer.add(fullTextWordIndex);
             subTextBuffer.add(subTextWordIndex);
 
             Integer currentFullTextElement = fullArray[fullTextWordIndex];
 
             for (Integer spanCounter = previousFullTextIndex + 1; spanCounter < fullTextWordIndex; spanCounter++) {
                 Integer elementlToCheck = fullArray[spanCounter];
                 if (elementlToCheck == previousFullTextElement) {
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
             ArrayList<Integer> subTextBuffer) {
 
         TreeMap<Integer, Integer> resultBuffer = new TreeMap<Integer, Integer>();
         for (Integer elementCounter = 0; elementCounter < fullTextBuffer.size(); elementCounter++) {
             resultBuffer.put(fullTextBuffer.get(elementCounter), subTextBuffer.get(elementCounter));
         }
         return resultBuffer;
     }
 
     public TreeMap<Integer, Integer> longestSubsequenceLengthWithDistanceCorrection(Integer[] fullArray,
             Integer[] subArray) {
 
         Integer[][] subsequenceLengths = new Integer[fullArray.length + 1][subArray.length + 1];
 
         // initialyse subsequncw length to -1; -1 is a flag of not processed subarray
         for (Integer i = 0; i < fullArray.length; i++) {
             for (Integer j = 0; j < subArray.length; j++) {
                 subsequenceLengths[i][j] = -1;
             }
         }
         Integer longestSubsequenceLength = this.subproblemLongestSubsequenceWithDistanceCorrection(
                 fullArray, subArray, 0, 0, subsequenceLengths);
         subsequenceLengths[0][0] = longestSubsequenceLength;
         // read the substring out from the matrix
 //        StringBuffer sb = new StringBuffer();
         TreeMap<Integer, Integer> resultBuffer = new TreeMap<Integer, Integer>();
         for (int x = fullArray.length, y = subArray.length;
                 x != 0 && y != 0;) {
             if (subsequenceLengths[x][y] == subsequenceLengths[x - 1][y]) {
                 x--;
             } else if (subsequenceLengths[x][y] == subsequenceLengths[x][y - 1]) {
                 y--;
             } else {
                 assert fullArray[x - 1] == subArray[y - 1];
 //                sb.append(a.get(x - 1));
                 resultBuffer.put(x - 1, y - 1);
                 x--;
                 y--;
             }
         }
 
         return resultBuffer;
 
     }
 
     private Integer subproblemLongestSubsequenceWithDistanceCorrection(
             Integer[] fullArray,
             Integer[] subArray,
             Integer fullArrayBeginIndex,
             Integer subArrayBeginIndex,
             Integer[][] subsequenceLengths) {
         if (fullArrayBeginIndex < fullArray.length && subArrayBeginIndex < subArray.length) {
             if (subsequenceLengths[fullArrayBeginIndex][subArrayBeginIndex] < 0) {
 
                 if (fullArray[fullArrayBeginIndex] == subArray[subArrayBeginIndex]) {
                     Integer subproblemLength = subproblemLongestSubsequenceWithDistanceCorrection(fullArray,
                             subArray, fullArrayBeginIndex + 1, subArrayBeginIndex + 1, subsequenceLengths);
 
                     subsequenceLengths[fullArrayBeginIndex + 1][subArrayBeginIndex + 1] = subproblemLength;
                     return 1 + subproblemLength;
                 } else {
                     Integer subproblemLengthWithShortenedSubArray = subproblemLongestSubsequenceWithDistanceCorrection(fullArray,
                             subArray, fullArrayBeginIndex, subArrayBeginIndex + 1, subsequenceLengths);
                     subsequenceLengths[fullArrayBeginIndex][subArrayBeginIndex + 1] = subproblemLengthWithShortenedSubArray;
 
                     Integer subproblemLengthWithShortenedFullArray = subproblemLongestSubsequenceWithDistanceCorrection(fullArray,
                             subArray, fullArrayBeginIndex + 1, subArrayBeginIndex, subsequenceLengths);
                     subsequenceLengths[fullArrayBeginIndex + 1][subArrayBeginIndex] = subproblemLengthWithShortenedFullArray;
 
                     Integer subproblemLength = Math.max(subproblemLengthWithShortenedSubArray,
                             subproblemLengthWithShortenedFullArray);
 
 
                     return subproblemLength;
                 }
 
             } else {
                 return subsequenceLengths[fullArrayBeginIndex][subArrayBeginIndex];
             }
         } else {
            subsequenceLengths[fullArrayBeginIndex][subArrayBeginIndex] = 0;
             return 0;
         }
     }
 }
