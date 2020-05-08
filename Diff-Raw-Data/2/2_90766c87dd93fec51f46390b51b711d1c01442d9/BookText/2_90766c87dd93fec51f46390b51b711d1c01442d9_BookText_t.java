 /*
  *@autor pozpl
  */
 package ru.urbancamper.audiobookmarker.text;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  *Class to hold book text, it's tokenized structure and
  * perform operations to improve it with audiobook information.
  * @author pozpl
  */
 public class BookText {
 
     protected final Log logger = LogFactory.getLog(getClass());
 
     /**
      * the full text of Audiobook in plain text fomat
      */
     private String fullText = "";
 
     private String[] tokenizedBookText;
 
     private Integer[] textInNumericForm;
 
     private ArrayList<RecognizedTextOfSingleAudiofile> recognizedAudioFiles;
 
     private LanguageModelBasedTextTokenizer textTokenizer;
 
     private WordsToNumsMap wordsToNumMapper;
 
     private LongestSubsequenceFinder longestSubsequenceFinder;
 
     private HashMap<String, Integer> registredFileMapper;
 
     private BitapSubtextFinding bitapSubtextFinder;
 
     public BookText(LanguageModelBasedTextTokenizer textTokenizer,
             WordsToNumsMap wordsToNumMapper,
             LongestSubsequenceFinder subsequnceFinder,
             BitapSubtextFinding bitapSubtextFinder
             ){
         this.recognizedAudioFiles = new ArrayList<RecognizedTextOfSingleAudiofile>();
         this.textTokenizer = textTokenizer;
         this.wordsToNumMapper = wordsToNumMapper;
         this.longestSubsequenceFinder = subsequnceFinder;
         this.registredFileMapper = new HashMap<String, Integer>();
         this.bitapSubtextFinder = bitapSubtextFinder;
     }
 
 
 
     public void setFullText(String fullText){
         this.fullText = fullText;
         this.tokenizedBookText = textTokenizer.tokenize(fullText);
         this.textInNumericForm = this.wordsToNumMapper.getNumbersFromWords(tokenizedBookText);
     }
 
     public void registerRecognizedTextPiece(RecognizedTextOfSingleAudiofile recognizedFileText){
         this.recognizedAudioFiles.add(recognizedFileText);
         this.registredFileMapper.put(recognizedFileText.getAudioFileHash(), this.recognizedAudioFiles.size());
     }
 
     public RecognizedTextOfSingleAudiofile[] getListOfRegistredAudiofiles(){
         return this.recognizedAudioFiles.toArray(
                 new RecognizedTextOfSingleAudiofile[this.recognizedAudioFiles.size()]);
     }
 
     /**
      * Iterate through recognized text peaces and find mapping between words of
      * this recognized peaces to full text words
      * @return ArrayList of mappings of recognized text to full text
      */
     private ArrayList<TreeMap<Integer, Integer>> getLongestSubsequenceMappingFromRecognizedTexts(){
         ArrayList<TreeMap<Integer, Integer>> recognizedTextLongestSubsequences = new ArrayList<TreeMap<Integer, Integer>>();
         for (Iterator<RecognizedTextOfSingleAudiofile> it = this.recognizedAudioFiles.iterator(); it.hasNext();) {
             RecognizedTextOfSingleAudiofile recognizedText = it.next();
             String[] recognizedTextAsTokens = recognizedText.getTokens();
             Integer[] recognizedTextAsNumbers = this.wordsToNumMapper.getNumbersFromWords(recognizedTextAsTokens);
 
             Integer maxErrors = recognizedTextAsNumbers.length / 3;
             maxErrors = maxErrors > 4 ? maxErrors : 4;
             Integer recognizedTextBeginIndex = this.bitapSubtextFinder.findWithReducedError(
                    this.textInNumericForm, recognizedTextAsNumbers);
             this.logger.info("Text with length " + this.recognizedAudioFiles.size() +
                     "was found in " + recognizedTextBeginIndex + " position");
             Integer[] fullTextSnippetToAlign = new Integer[recognizedTextAsNumbers.length];
             Integer endOfInterval = recognizedTextAsNumbers.length + recognizedTextBeginIndex + maxErrors;
             fullTextSnippetToAlign = Arrays.copyOfRange(this.textInNumericForm,
                     recognizedTextBeginIndex, endOfInterval);
             TreeMap<Integer, Integer> recTextLongestSubsequence =
                     this.longestSubsequenceFinder.getLongestSubsequenceWithMinDistance(fullTextSnippetToAlign, recognizedTextAsNumbers);
             recTextLongestSubsequence = this.shiftMappingOfSubText(recTextLongestSubsequence, recognizedTextBeginIndex);
             recognizedTextLongestSubsequences.add(recTextLongestSubsequence);
         }
 
         return recognizedTextLongestSubsequences;
     }
 
 
     /**
      * Shift mapping of sub text relatively of shift
      * @param fullSubTextMap - tree map of equality between full text and sub text map
      * @param shiftIndex - shift index of sub text in the full text recognized index
      * @return return Tree map with shifted results
      */
     private TreeMap<Integer, Integer> shiftMappingOfSubText(TreeMap<Integer, Integer> fullSubTextMap,
             Integer shiftIndex){
         TreeMap<Integer, Integer> shiftedTreeMap = new TreeMap<Integer, Integer>();
         for(Integer fullTextWordIndex : fullSubTextMap.keySet()){
             Integer shiftedFullTextWordIndex = fullTextWordIndex + shiftIndex;
             shiftedTreeMap.put(shiftedFullTextWordIndex,
                     fullSubTextMap.get(fullTextWordIndex));
         }
         return shiftedTreeMap;
     }
 
     private String constructWordWithMarkInfo(String word, Integer fileIndex,Double startTime){
         String fileIndexStr = fileIndex.toString();
         String startTimeStr = startTime.toString();
 
         String returnToken = "<" + fileIndexStr + ":" + startTimeStr + "/>" + word;
         return returnToken;
     }
 
     @Deprecated
     private String implodeTokensArray(String[] tokensArray){
         String resultString = "";
         for(Integer tokensCounter = 0;
         tokensCounter < tokensArray.length; tokensCounter++){
             String token = tokensArray[tokensCounter];
             if(tokensCounter < tokensArray.length - 1){
             resultString += token + " ";
             }else{
                 resultString += token;
             }
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
 
                 Double beginTime = recognizedFile.getBeginTimeOfTokenAtPosition(recognizedTextWordIndex);
                 Double endTime = recognizedFile.getEndTimeOfTokenAtPosition(recognizedTextWordIndex);
 
                 String wordWithMarkerInfo = this.constructWordWithMarkInfo(
                         this.tokenizedBookText[fullTextWordIndex], fileIndex, beginTime);
                 this.tokenizedBookText[fullTextWordIndex] = wordWithMarkerInfo;
 
             }
             subsequenceCounter++;
         }
 
         return this.textTokenizer.deTokenize(tokenizedBookText);//this.implodeTokensArray(this.tokenizedBookText);
     }
 
 
 }
