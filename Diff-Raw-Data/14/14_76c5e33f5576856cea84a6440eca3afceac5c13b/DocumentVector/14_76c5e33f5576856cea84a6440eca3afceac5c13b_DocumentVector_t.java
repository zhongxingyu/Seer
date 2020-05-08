 package org.snow.documentprocessing;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: snowhyzhang
  * Date: 13-4-17
  * Time: 下午9:32
  * To change this template use File | Settings | File Templates.
  */
 public class DocumentVector {
     private List<String> wordList;
     private Map<String, List<Integer>> wordCountMapper;
     private String documentName;
 
     public DocumentVector(List<String> wordList) {
         this.wordList = wordList;
         documentBuild();
     }
 
     public DocumentVector(List<String> wordList, String documentName) {
         this.wordList = wordList;
         this.documentName = documentName;
         documentBuild();
     }
 
     public String getDocumentName() {
         return documentName;
     }
 
     public void setDocumentName(String documentName) {
         this.documentName = documentName;
     }
 
     private void documentBuild(){
        if (wordList == null || wordList.isEmpty()){
            return;
        }
         wordCountMapper = new HashMap<String, List<Integer>>();
         List<Integer> positionList;
         int position = 0;
         for (String word: wordList){
             if (!wordCountMapper.containsKey(word)){
                 positionList = new ArrayList<Integer>();
                 positionList.add(position);
                 wordCountMapper.put(word, positionList);
             } else {
                 positionList = wordCountMapper.get(word);
                 positionList.add(position);
             }
             ++position;
         }
     }
 
     public String getWord(int position){
         if (position < 0 || position >= wordList.size()){
             return null;
         }
         return wordList.get(position);
     }
 
     public int getWordCount(String word){
         return wordCountMapper.get(word).size();
     }
 
     public int getTotalCount(){
         return wordList.size();
     }
 
     public Set<String> getWordSet(){
         return wordCountMapper.keySet();
     }
 
     public List<Integer> getWordPositions(String word){
         return wordCountMapper.get(word);
     }
 
     public List<String> getWordList(){
         return wordList;
     }
 }
