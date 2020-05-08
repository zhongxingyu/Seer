 /*
  * This class will provide recognition of audio files with a caching ability.
  * A structure of the cache is folowing:
  * Before a file recognition a programm will check if file with a
  * fileToRecognizePath + .recognized.txt path is exists.
  * If so, read text from this file and move on, else recognize file.
  * Text of recognized file should be stored under the path mentioned before.
  * It's assumed that program has rights to write into this path.
  */
 package ru.urbancamper.audiobookmarker.audio;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import ru.urbancamper.audiobookmarker.text.RecognizedTextOfSingleAudiofile;
 
 /**
  *
  * @author pozpl
  */
 public class AudioFileRecognizerSphinxCached implements AudioFileRecognizerInterface{
     /**
      *Extension for cached files. This files resides in the same directory with
      * original audio files and contained aligned recognized text.
      */
     public static final  String CACHE_FILR_EXTENSION = ".cached";
 
     /**
      *  Logging facility
      */
     protected final Log logger = LogFactory.getLog(getClass());
 
 
     /**
      * Instance of recognizer
      */
     private AudioFileRecognizerInterface audioFileRecognizer;
 
     /**
      *
      * @param recognizer
      */
     public AudioFileRecognizerSphinxCached(AudioFileRecognizerInterface recognizer){
         this.audioFileRecognizer = recognizer;
     }
 
     private Boolean isCacheExists(String cacheFilePath){
         File cachedFile = new File(cacheFilePath);
         if(cachedFile.exists()){
             return Boolean.TRUE;
         }else{
             return Boolean.FALSE;
         }
     }
 
     /**Convert file to string
      * @param filePath
      * @return
      */
     public String fileToString(String filePath){
         StringBuilder strBuffer = new StringBuilder();
         int BLOC_SIZE = 512;
         char[] b = new char[BLOC_SIZE];
         Reader fileReader = null;
         try {
             fileReader = new FileReader(filePath);
             int n;
             while((n = fileReader.read(b))>0){
                 strBuffer.append(b, 0, n);
             }
         } catch (IOException ex) {
             this.logger.error("Excsption during file read " + filePath + ": " + ex);
         }
         String retStr = strBuffer.toString();
         return retStr;
     }
 
     private String getCacheFilePath(String audioFilePath){
         String cacheFilePath = audioFilePath + AudioFileRecognizerSphinxCached.CACHE_FILR_EXTENSION;
         return cacheFilePath;
     }
 
     /**
      * Get a aligned text from cache file
      * @param filePath
      * @return
      */
     private String readRecognizedTextFromCache(String cacheFilePath){
         String textFromCachedFile = this.fileToString(cacheFilePath);
         return textFromCachedFile;
     }
 
     private Boolean writeResultToCache(String cacheFilePath, String textToCache){
         BufferedWriter bWriter = null;
         try {
             File cacheFile = new File(cacheFilePath);
             bWriter = new BufferedWriter(new FileWriter(cacheFile));
             bWriter.write(textToCache);
         } catch (IOException ex) {
             Logger.getLogger(AudioFileRecognizerSphinxCached.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 bWriter.close();
             } catch (IOException ex) {
                 Logger.getLogger(AudioFileRecognizerSphinxCached.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return Boolean.TRUE;
     }
 
     private Boolean isThisFileIsCacheFile(String filePath){
        String cacheRegexp = "\\.cached$";
         Pattern regexp = Pattern.compile(cacheRegexp);
         Matcher matcher = regexp.matcher(filePath);
         if(matcher.find()){
             return Boolean.TRUE;
         }
 
         return Boolean.FALSE;
     }
 
     /**
      *
      * @param filePath
      * @param fileUnicIdentifier
      * @return
      */
     @Override
     public RecognizedTextOfSingleAudiofile recognize(String filePath, String fileUnicIdentifier) {
         String resultTextAggregated;
         if (!this.isThisFileIsCacheFile(filePath)) {
             String cacheFilePath = this.getCacheFilePath(filePath);
             if (this.isCacheExists(cacheFilePath)) {
                 this.logger.info("Get allocation information from cache");
                 resultTextAggregated = this.readRecognizedTextFromCache(cacheFilePath);
             } else {
                 this.logger.info("No cache presented, try to recognize");
                 resultTextAggregated = this.audioFileRecognizer.getTextFromAudioFile(filePath, fileUnicIdentifier);
                 this.writeResultToCache(cacheFilePath, resultTextAggregated);
             }
         } else {
             resultTextAggregated = "";
         }
 
         RecognizedTextOfSingleAudiofile recognizedTextObj = new RecognizedTextOfSingleAudiofile(resultTextAggregated, fileUnicIdentifier);
 
         return recognizedTextObj;
     }
 
     public String getTextFromAudioFile(String audioFilePath, String fileIdentification) {
         return "";
     }
 
 }
