 /*
  *@autor pozpl
  */
 package ru.urbancamper.audiobookmarker.text;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 /**
  *Class to hold book text, it's tokenized structure and
  * perform operations to improve it with audiobook information.
  * @author pozpl
  */
 public class BookText {
 
     /**
      * the full text of Audiobook in plain text fomat
      */
     private String fullText = "";
 
     private String[] tokenizedBookText;
 
     private Integer[] textInNumericForm;
 
     private ArrayList<RecognizedTextOfSingleAudiofile> recognizedAudioFiles;
 
     private TextTokenizer textTokenizer;
 
     private WordsToNumsMap wordsToNumMapper;
 
     private LongestSubsequenceFinder longestSubsequenceFinder;
 
     private HashMap<String, Integer> registredFileMapper;
 
     public BookText(TextTokenizer textTokenizer,
             WordsToNumsMap wordsToNumMapper,
             LongestSubsequenceFinder subsequnceFinder
             ){
         this.recognizedAudioFiles = new ArrayList<RecognizedTextOfSingleAudiofile>();
         this.textTokenizer = textTokenizer;
         this.wordsToNumMapper = wordsToNumMapper;
         this.longestSubsequenceFinder = subsequnceFinder;
         this.registredFileMapper = new HashMap<String, Integer>();
     }
 
     public void setFullText(String fullText){
         this.fullText = fullText;
         this.tokenizedBookText = textTokenizer.tokenize(fullText);
         this.textInNumericForm = this.wordsToNumMapper.getNumbersFromWords(tokenizedBookText);
     }
 
     public void registerRecognisedTextPeace(RecognizedTextOfSingleAudiofile recognizedFileText){
         this.recognizedAudioFiles.add(recognizedFileText);
         this.registredFileMapper.put(recognizedFileText.getAudioFileHash(), this.recognizedAudioFiles.size());
     }
 
     public RecognizedTextOfSingleAudiofile[] getListOfRegistredAudiofiles(){
         return this.recognizedAudioFiles.toArray(
                 new RecognizedTextOfSingleAudiofile[this.recognizedAudioFiles.size()]);
     }
 
     private ArrayList<TreeMap<Integer, Integer>> getLongestSubsequenceMappingFromRecognizedTexts(){
         ArrayList<TreeMap<Integer, Integer>> recognizedTextLongestSubsequences = new ArrayList<TreeMap<Integer, Integer>>();
         for(RecognizedTextOfSingleAudiofile recognizedText: this.recognizedAudioFiles){
             String[] recognizedTextAsTokens = recognizedText.getTokens();
            Integer[] recognizedTextAsNumbers = this.wordsToNumMapper.getNumbersFromWords(tokenizedBookText);
             TreeMap<Integer, Integer> recTextLongestSubsequence = this.longestSubsequenceFinder.findLogestSubsequence(this.textInNumericForm, recognizedTextAsNumbers);
             recognizedTextLongestSubsequences.add(recTextLongestSubsequence);
         }
 
         return recognizedTextLongestSubsequences;
     }
 
     public String constructWordWithMarkInfo(String word, Integer fileIndex,Double startTime){
         String fileIndexStr = fileIndex.toString();
         String startTimeStr = startTime.toString();
 
         String returnToken = "<" + fileIndexStr + ":" + startTimeStr + "/>" + word;
         return returnToken;
     }
 
     private String implodeTokensArray(String[] tokensArray){
         String resultString = "";
         for(String token: tokensArray){
             resultString += token + " ";
         }
 
         return resultString;
     }
 
     public String buildTextWithAudioMarks(){
         ArrayList<TreeMap<Integer, Integer>> recognizedTextLongestSubsequences = this.getLongestSubsequenceMappingFromRecognizedTexts();
         String markedText = "";
         Integer subsequenceCounter = 0;
         for(TreeMap<Integer, Integer> longestSubsequence: recognizedTextLongestSubsequences){
             RecognizedTextOfSingleAudiofile recognizedFile = this.recognizedAudioFiles.get(subsequenceCounter);
             Integer fileIndex = this.registredFileMapper.get(recognizedFile.getAudioFileHash());
             for(Entry<Integer, Integer> fullTextToRecText: longestSubsequence.entrySet()){
                 Integer fullTextWordIndex = fullTextToRecText.getKey();
                 Integer recognizedTextWordIndex = fullTextToRecText.getValue();
                 System.out.println(recognizedTextWordIndex);
                 Double beginTime = recognizedFile.getBeginTimeOfTokenAtPosition(recognizedTextWordIndex);
                 Double endTime = recognizedFile.getEndTimeOfTokenAtPosition(recognizedTextWordIndex);
 
                 this.tokenizedBookText[fullTextWordIndex] =
                         this.constructWordWithMarkInfo(this.tokenizedBookText[fullTextWordIndex], fileIndex, beginTime);
             }
             subsequenceCounter++;
         }
 
         return this.implodeTokensArray(this.tokenizedBookText);
     }
 
 
 }
