 package org.ap.croodle;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.StringUtils;
 
 public class CroodleQuestionsParser {
     private File questionsFile;
     private String questionsFileContent; // in lowercase
 
     private ArrayList<String[]> questionsFileLinesWords = new ArrayList<String[]>();
     
     public int[] tryToAnswer (String question, ArrayList<String> variants){
         System.err.println("Tryyyning to find question ----");
         String [] questionWords = sprlitExpression(question);
         ArrayList<String[]> variantsWords =new ArrayList<String[]>();
         for(int i = 0; i < variants.size(); i++){
             variantsWords.add(sprlitExpression(variants.get(i)) );
         }
         
         String firstLongWord = questionWords[0];
         if(firstLongWord.length()<3){
             firstLongWord = questionWords[1];
         }
         
         for(int i = 0; i<this.questionsFileLinesWords.size();i++){
             String[] lineWords = questionsFileLinesWords.get(i);
             if(ArrayUtils.contains(lineWords, firstLongWord)){
                 // if line contains longest of first two words , when we will check if it is this question
                 System.err.println("one word find" + "( "+ firstLongWord+ ")" +" , so we check . Result is : " + ensureItIsThisQuestion(questionWords,i));
                 if(ensureItIsThisQuestion(questionWords,i)){
                     //this is this question , now we need get correct variants
                     System.err.println("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFfinded");
                     
                     System.err.println("<["+ question +"]>");
                    
                    
                     System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    return getCorrectAnswers(i, variantsWords);
                     //break;
                 };
             }
         }
         
         System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
         return new int[]{};
     }
     
     
     public int [] getCorrectAnswers(int questionIndexInFileLines , ArrayList<String[]> variantsWords){
         ArrayList<Integer> result = new ArrayList<Integer>();
         System.err.println("try to getCorrectAnswers");
         // need line where question ends !!!!!!!!
         String [] lineWords = this.questionsFileLinesWords.get(questionIndexInFileLines);
         double variantTrueCoef = 0;
         double wordsInVariant = variantsWords.get(0).length;
         double currentLineWordlsCount = lineWords.length; 
         double matchedWords = 0; // > 65%
             
         for(int i = 0; i< variantsWords.size(); i++){ // iterate through all variants .
             variantTrueCoef = 0;
             wordsInVariant = variantsWords.get(i).length;
             if(variantsWords.get(i)[0].length()<2){
                 wordsInVariant = wordsInVariant - 1;
             }
             
             for(int j = 0; j < 5; j++){  // check 5 lines after question
                 lineWords = this.questionsFileLinesWords.get(questionIndexInFileLines + j + 1);
                 currentLineWordlsCount = lineWords.length; 
                 matchedWords = 0; // > 65%
                 
                 for(int k=0; k< lineWords.length;k++){
                     if(ArrayUtils.contains(variantsWords.get(i), lineWords[k])){
                         matchedWords = matchedWords + 1;
                     }
                 }
                 
                 variantTrueCoef = matchedWords / wordsInVariant;
                    if(variantTrueCoef>0.65){
                         result.add(i);
                         System.err.println("$$$$$$$$$$$$$$$$$$$Correct variant" + StringUtils.join(variantsWords.get(i), " ") ); 
                     }
                 }
             
         }
         
         int [] correctVariants = new int[result.size()];
         for(int i = 0; i < correctVariants.length; i++){
             correctVariants[i] = result.get(i);
         }
         System.err.println("FINAL reqult + " +result);
         return correctVariants;
     }
     
     // the most important function
     public boolean ensureItIsThisQuestion(String[] questionWords , int index){
         String [] lineWords = this.questionsFileLinesWords.get(index);
         double questionTrueCoef = 0;
         double wordsInQuestion = questionWords.length;
         double currentLineWordlsCount = lineWords.length; 
         double matchedWords = 0; // > 60%
         
         // words in line length is much more than words in question it is automaticaly not this question
        if( (currentLineWordlsCount/2) > wordsInQuestion){
             return false;
         }
         
         for(int i=0; i< lineWords.length;i++){
             
             if( ArrayUtils.contains(questionWords, lineWords[i])){
                 matchedWords = matchedWords + 1;
             }
         }
         
         // if coef is less than 0.65 it can mean : 
         // 1: it is not this question
         // 2: question in file takes more than 1 line , in this case we need get second(third, ...) line and check
         
        
         questionTrueCoef = matchedWords / wordsInQuestion;
         if(questionTrueCoef>0.86){
             System.err.println("Coef = " + questionTrueCoef + "#########" + matchedWords + " / " + wordsInQuestion);
             System.err.println(StringUtils.join(lineWords, "*"));
             return true;
         }
         
         return false;
     }
     
     
     public String[] sprlitExpression(String expression){
         String[] words = expression.split("\\s+");
         for (int i = 0; i < words.length; i++) {
             // You may want to check for a non-word character before blindly
             // performing a replacement
             // It may also be necessary to adjust the character class
             words[i] = words[i].replaceAll("[^\\w]", "").toLowerCase();
         }
         
         /*for(int i=0; i<words.length;i++){
             System.out.println(words[i]);
         }*/      
         return words;
     }
     
     public void setQuestionFile(File questionsFile){
         this.questionsFile = questionsFile;
         try {
             this.questionsFileContent = IOUtils.toString( new FileInputStream(questionsFile)).toLowerCase(); 
             //System.err.println("FIle content");
             
             String lineSeparator = System.getProperty("line.separator");
             String [] questionsFileLines = this.questionsFileContent.split(lineSeparator); // it would be good to delete empty lines =)
             
             for(int i=0;i<questionsFileLines.length;i++){
                 this.questionsFileLinesWords.add(sprlitExpression(questionsFileLines[i]));
                 //System.out.println(StringUtils.join(this.questionsFileLinesWords.get(i), "^")  );
             }
         } catch (IOException ex) {
             Logger.getLogger(CroodleQuestionsParser.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
