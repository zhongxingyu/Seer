 package org.snow.tokenizer;
 
 import org.wltea.analyzer.cfg.Configuration;
 import org.wltea.analyzer.cfg.DefaultConfig;
 import org.wltea.analyzer.core.IKSegmenter;
 import org.wltea.analyzer.core.Lexeme;
 import org.wltea.analyzer.dic.Dictionary;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: snowhyzhang
  * Date: 13-4-17
  * Time: 上午11:21
  * To change this template use File | Settings | File Templates.
  */
 public class IKAnalyzer {
 
     static {
         Configuration configuration = DefaultConfig.getInstance();
         Dictionary.initial(configuration);
     }
 
     public static List<String> tokenizer(String sentence){
        String filterString = sentence.replaceAll("[^（\u4E00-\u9FA0]｜[A-Za-z0-9]+$）"," ");
 
         if (filterString == null || filterString.isEmpty()){
             return null;
         }
 
         List<String> wordList = new ArrayList<String>();
         StringReader stringReader = new StringReader(filterString);
         IKSegmenter ikSegmenter = new IKSegmenter(stringReader, true);
         Lexeme lexeme;
 
         try {
             while ((lexeme = ikSegmenter.next()) != null){
                 if (lexeme.getLexemeText() != null || !lexeme.getLexemeText().isEmpty()){
                     wordList.add(lexeme.getLexemeText());
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
 
         return wordList;
     }
 
     public static void addDicWords (List<String> dic){
         Set<String> extractDictionary = new HashSet<String>();
         for (String word: dic){
             extractDictionary.add(word);
         }
         Dictionary dictionary = Dictionary.getSingleton();
         dictionary.addWords(extractDictionary);
     }
 
     public static void addDicWords(String fileName){
         File file = new File(fileName);
         BufferedReader bufferedReader = null;
         Set<String> extractDictionary = new HashSet<String>();
         Dictionary dictionary = Dictionary.getSingleton();
         try {
             bufferedReader = new BufferedReader(new FileReader(file));
             String word;
             while ((word = bufferedReader.readLine()) != null){
                 extractDictionary.add(word);
             }
             dictionary.addWords(extractDictionary);
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } finally {
             try{
                 if (bufferedReader != null)
                     bufferedReader.close();
             } catch (Exception e){
                 e.printStackTrace();
             }
         }
 
     }
 }
