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
 
     /**
     * Naive longest subsequence algorithm, which do not keep in mind distance between
      * words.
      * @param fullArray
      * @param subArray
      * @return
      */
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
 
     /**
      * Improved version of longest subsequence algorithm with filtration according to
      * distance betwin words.
      * @param  fullArray Integer[]
      * @param  subArray Integer[]
      * @return TreeMap
      */
     @Deprecated
     public TreeMap<Integer, Integer> longestSubsequenceLengthWithDistanceCorrection(Integer[] fullArray,
             Integer[] subArray) {
 
         Integer[][] subsequenceLengths = new Integer[fullArray.length + 1][subArray.length + 1];
         Integer[][] fullArrayLastEqualsIndexes = new Integer[fullArray.length + 1][subArray.length + 1];
         Integer[][] subArrayLastEqualsIndexes = new Integer[fullArray.length + 1][subArray.length + 1];
         Boolean[][] equalsElementsFlag = new Boolean[fullArray.length + 1][subArray.length + 1];
         Boolean[][] sequenceStartFlag = new Boolean[fullArray.length + 1][subArray.length + 1];
 
         // initialyse subsequncw length to -1; -1 is a flag of not processed subarray
         for (Integer i = 0; i <= fullArray.length; i++) {
             for (Integer j = 0; j <= subArray.length; j++) {
                 subsequenceLengths[i][j] = -1;
                 equalsElementsFlag[i][j] = false;
                 sequenceStartFlag[i][j] = false;
                 fullArrayLastEqualsIndexes[i][j] = -1;
                 subArrayLastEqualsIndexes[i][j] = -1;
             }
         }
         Integer longestSubsequenceLength = this.subproblemLongestSubsequenceWithDistanceCorrection(
                 fullArray, subArray, 0, 0, subsequenceLengths,equalsElementsFlag, sequenceStartFlag,
                 fullArrayLastEqualsIndexes, subArrayLastEqualsIndexes, -2, -2);
 
 
         TreeMap<Integer, Integer> resultBuffer =
                 this.extractLongestSubsequenceFromLengthMatrix(fullArray,
                 subArray, subsequenceLengths, equalsElementsFlag);
 
         return resultBuffer;
     }
 
     /**
      * read the substring out from the matrix
      * @param subsequenceLengths
      * @return
      */
     private TreeMap<Integer, Integer> extractLongestSubsequenceFromLengthMatrix(
             Integer[] fullArray,
             Integer[] subArray,
             Integer[][] subsequenceLengths,
             Boolean[][] equalsElementsFlags){
             Integer biggestSubsequenceLength = subsequenceLengths[0][0];
             Integer biggestSubsequenceElementStartX = 0;
             Integer biggestSubsequenceElementStartY = 0;
             for (int x = 0; x < fullArray.length; x++) {
                 for (int y = 0; y < subArray.length; y++) {
                     if(subsequenceLengths[x][y] > biggestSubsequenceLength){
                         biggestSubsequenceLength = subsequenceLengths[x][y];
                         biggestSubsequenceElementStartX = x;
                         biggestSubsequenceElementStartY = y;
                     }
                 }
             }
 
            return this.extractLongestSubsequenceFromLengthsSubmatrix(
                    fullArray, subArray, subsequenceLengths, equalsElementsFlags,
                    biggestSubsequenceElementStartX, biggestSubsequenceElementStartY);
 
     }
 
     private TreeMap<Integer, Integer> extractLongestSubsequenceFromLengthsSubmatrix(
             Integer[] fullArray,
             Integer[] subArray,
             Integer[][] subsequenceLengths,
             Boolean[][] equlasElementsFlag,
             Integer submatrixX,
             Integer submatrixY){
         TreeMap<Integer, Integer> resultBuffer = new TreeMap<Integer, Integer>();
         //currentLength is bigger on 1 for first element addition to longest subsequencs
         Integer currentLength = subsequenceLengths[0][0] + 1;
 
         for (int x = submatrixX; x < fullArray.length; x++) {
             for (int y = submatrixY; y < subArray.length; y++) {
 //                if(x == 4 && y == 3){
 //                    System.out.println("");
 //                }
                 if(subsequenceLengths[x][y] != subsequenceLengths[x+1][y]
                    && subsequenceLengths[x][y] != subsequenceLengths[x][y+1]
                    && subsequenceLengths[x][y] < currentLength
                    && equlasElementsFlag[x][y]){
                     assert fullArray[x] == subArray[y];
                     currentLength = subsequenceLengths[x][y];
                     resultBuffer.put(x, y);
                     break;
                 }
 //                if(subsequenceLengths[x][y] != subsequenceLengths[x][y+1]){
 //                    break;
 //                }
             }
         }
 
         return resultBuffer;
     }
 
     private Integer subproblemLongestSubsequenceWithDistanceCorrection(
             Integer[] fullArray,
             Integer[] subArray,
             Integer fullArrayBeginIndex,
             Integer subArrayBeginIndex,
             Integer[][] subsequenceLengths,
             Boolean[][] equalsElementsFlag,
             Boolean[][] sequenceStartFlag,
             Integer[][] fullArrayLastEqualsIndexes,
             Integer[][] subArrayLastEqualsIndexes,
             Integer fullArrayLastEqualsIndex,
             Integer subArrayLastEqualsIndex) {
         if (fullArrayBeginIndex < fullArray.length && subArrayBeginIndex < subArray.length) {
             if (subsequenceLengths[fullArrayBeginIndex][subArrayBeginIndex] < 0
                 || fullArrayLastEqualsIndexes[fullArrayBeginIndex][subArrayBeginIndex] != fullArrayLastEqualsIndex
                 || subArrayLastEqualsIndexes[fullArrayBeginIndex][subArrayBeginIndex] != subArrayLastEqualsIndex) {
                     Integer subProblemLengthAdd = 0;
                     Integer fullArrayLastEqualsIndexActive = fullArrayLastEqualsIndex;
                     Integer subArrayLastEqualsIndexActive = subArrayLastEqualsIndex;
 
                     if(fullArrayLastEqualsIndexes[fullArrayBeginIndex][subArrayBeginIndex] > fullArrayLastEqualsIndex
                            || subArrayLastEqualsIndexes[fullArrayBeginIndex][subArrayBeginIndex] > subArrayLastEqualsIndex){
 
                         sequenceStartFlag[fullArrayBeginIndex][subArrayBeginIndex] = true;
                     }else{
                         fullArrayLastEqualsIndexes[fullArrayBeginIndex][subArrayBeginIndex] = fullArrayLastEqualsIndex;
                         subArrayLastEqualsIndexes[fullArrayBeginIndex][subArrayBeginIndex] = subArrayLastEqualsIndex;
                     }
                     if (fullArray[fullArrayBeginIndex] == subArray[subArrayBeginIndex]) {
                         fullArrayLastEqualsIndexActive = fullArrayBeginIndex;
                         subArrayLastEqualsIndexActive = subArrayBeginIndex;
                         equalsElementsFlag[fullArrayBeginIndex][subArrayBeginIndex] = true;
                         if(fullArrayLastEqualsIndex >= 0 && subArrayLastEqualsIndex >= 0){
                             Integer fullArrayDistance = fullArrayBeginIndex - fullArrayLastEqualsIndex;
                             Integer subArrayDistance = subArrayBeginIndex - subArrayLastEqualsIndex;
                             if(Math.abs(fullArrayDistance - subArrayDistance) < 3){
                                 subProblemLengthAdd = 1;
                             }else{
                                 sequenceStartFlag[fullArrayBeginIndex][subArrayBeginIndex] = true;
                             }
                         }else{
                             subProblemLengthAdd = 1;
                         }
                     }
 
                     Integer subproblemLengthWithShortenedFullArray = subproblemLongestSubsequenceWithDistanceCorrection(fullArray,
                             subArray, fullArrayBeginIndex + 1, subArrayBeginIndex, subsequenceLengths, equalsElementsFlag,
                             sequenceStartFlag,
                             fullArrayLastEqualsIndexes, subArrayLastEqualsIndexes, fullArrayLastEqualsIndexActive, subArrayLastEqualsIndexActive);
 
                     Integer subproblemLengthWithShortenedSubArray = subproblemLongestSubsequenceWithDistanceCorrection(fullArray,
                             subArray, fullArrayBeginIndex, subArrayBeginIndex + 1, subsequenceLengths, equalsElementsFlag,
                             sequenceStartFlag,
                             fullArrayLastEqualsIndexes, subArrayLastEqualsIndexes, fullArrayLastEqualsIndexActive, subArrayLastEqualsIndexActive);
 
                     Integer subproblemLengthBothShortened = subproblemLongestSubsequenceWithDistanceCorrection(fullArray,
                             subArray, fullArrayBeginIndex + 1, subArrayBeginIndex + 1, subsequenceLengths, equalsElementsFlag,
                             sequenceStartFlag,
                             fullArrayLastEqualsIndexes, subArrayLastEqualsIndexes, fullArrayLastEqualsIndexActive, subArrayLastEqualsIndexActive);
 
                     Integer subproblemLength = Math.max(subproblemLengthWithShortenedSubArray,
                             subproblemLengthWithShortenedFullArray);
 
                     Integer newSubProblem =
                     Math.max(subproblemLength, subproblemLengthBothShortened) + subProblemLengthAdd;
 
                     if(sequenceStartFlag[fullArrayBeginIndex + 1][subArrayBeginIndex]
                        && sequenceStartFlag[fullArrayBeginIndex][subArrayBeginIndex + 1]
                        && sequenceStartFlag[fullArrayBeginIndex + 1][subArrayBeginIndex + 1]){
                           newSubProblem = subProblemLengthAdd;
                     }
                     if(subsequenceLengths[fullArrayBeginIndex][subArrayBeginIndex ] < newSubProblem){
                         subsequenceLengths[fullArrayBeginIndex][subArrayBeginIndex ] = newSubProblem;
                     }
                     return subsequenceLengths[fullArrayBeginIndex][subArrayBeginIndex];
 
             } else {
                 return subsequenceLengths[fullArrayBeginIndex][subArrayBeginIndex];
             }
         } else {
             subsequenceLengths[fullArrayBeginIndex][subArrayBeginIndex] = 0;
             return 0;
         }
     }
 }
