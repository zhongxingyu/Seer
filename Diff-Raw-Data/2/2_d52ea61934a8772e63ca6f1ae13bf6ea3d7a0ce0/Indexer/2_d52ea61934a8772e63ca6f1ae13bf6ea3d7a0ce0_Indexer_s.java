 /**
  * COPYRIGHT (C) 2012 Christian Hall. All Rights Reserved.
  * Indexer file for part 1 of software traceability project
  * Runs indexer functions
  * CSE 4214 | Indexer
  * @author Christian Hall
  * @version 1.0 9.10.12
  */
 package tracer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Indexer
 {
   private ArrayList<String> codeArray, commentArray;
   private String pathName;
   
   /**
    * @about Constructor creates new path name and array lists and runs indexer
    * functions for Indexer object.
    * @param file - file to be indexed
    */
   public Indexer(File file)
   {
     pathName = file.getAbsolutePath();
     codeArray = new ArrayList<String>();
     commentArray = new ArrayList<String>();
     try
     {
       separating(file);
       commentArray = spliting(commentArray);
       codeArray = spliting(codeArray);
       commentArray = stemming(commentArray);
       codeArray = stemming(codeArray);
       commentArray = trimming(commentArray);
       codeArray = trimming(codeArray);
     } 
     catch (IOException e)
     {
       System.out.println(e.getCause());
     }
   }
   
   /**
    * @about Basic getter functions for path string and code and comment arrays
    * @return the array
    */
   public String getPathName()
   {
     return pathName;
   }
   public ArrayList<String> getCommentArray()
   {
     return commentArray;
   }
   public ArrayList<String> getCodeArray()
   {
     return codeArray;
   }
   
   /**
    * @about Trims out special characters and splits file into two arrays for 
    * code and comments.
    * Module written by Zhangji
    * @param file - File to be indexed
    * @throws IOException
    */
   private void separating(File file) throws IOException
   {	
     BufferedReader inputFile = new BufferedReader(new FileReader(file));
     Pattern pattern = Pattern.compile("[a-zA-Z'/]+"); //regular expression
     String word, word2;
     while ((word = inputFile.readLine()) != null)
     {
       if (!word.startsWith("package") && !word.startsWith("import"))
       {
         if (word.contains("*"))
         {
           Matcher match = pattern.matcher(word);
           while (match.find())
           {
             word2 = match.group();
             if (!word2.contains("/"))
             {
               commentArray.add(word2 + " ");
             }
           }
         } 
         else
         {
           Matcher match = pattern.matcher(word);
           while (match.find())
           {
             word2 = match.group();
             if (word2.contains("//"))
             {
               while (match.find())
               {
                 word2 = match.group();
                 commentArray.add(word2 + " ");
               }
               break;
             }				  		
             codeArray.add(word2 + " ");
           }
         }
       }
     }
   }
   
   /**
    * @about Takes one of the word arrays and splits camelCase Strings into 
    * separate words and places them into a temporary ArrayList that will 
    * replace the old one.
    * Module written by Seth
    * @param arrayToSplit - Either the comment or code array in this class
    * @return tempArray - New array with split words
    */
   private ArrayList<String> spliting(ArrayList<String> arrayToSplit)
   {
     ArrayList<String> tempArray = new ArrayList();
     char letter;
     for(String stringToCheck : arrayToSplit)
     {
       for(int i = 0; i < stringToCheck.length(); i++)
       {
         letter = stringToCheck.charAt(i);
         if(letter >= 'A' && letter <= 'Z' || letter == '\'' || letter == '/')
         {
           tempArray.add(stringToCheck.substring(0, i).toLowerCase());
           stringToCheck = stringToCheck.substring(i);
           i = 0;
         }
        if(stringToCheck.startsWith("/"))
           stringToCheck = stringToCheck.substring(1);
       }
       tempArray.add(stringToCheck.toLowerCase());
     }
     return tempArray;
   }
     
   /**
    * @about Stems commentArray using the Porter Stemmer algorithm in 
    * Stemmer.java
    * Module written by Matthew
    */
   private ArrayList<String> stemming(ArrayList<String> arrayToStem)
   {
     ArrayList<String> stemmedArray = new ArrayList<String>();
     Stemmer stemmer = new Stemmer();
     for (int i=0; i < arrayToStem.size(); i++)
     {
       // Build word to be stemmed, then stem
       for (int j=0; j < arrayToStem.get(i).length(); j++)
       {
         char letter = arrayToStem.get(i).charAt(j);
         if (letter != ' ')
         {
           stemmer.add(letter);
         }
       }  	
       stemmer.stem();
 
       // Stop double words with checkWord, add word if not double or stem list
       // is empty
       if(arrayToStem.size() > 0){
         boolean isDouble = checkWord(stemmedArray, stemmer);
         if(!isDouble)
         {
           stemmedArray.add(stemmer.toString());
         }
       }
       else
       {
         stemmedArray.add(stemmer.toString());
       }
     }
     return stemmedArray; // Return new stemmedArray to replace old arrayToStem
   }
 
   /**
    * @about Check to see if stemmed word already exists
    * Module written by Matthew
    * @param stemmedArray - Array that will replace commentArray
    * @param stemmer - Stemmer from Porter Stemmer
    * @return true if word is in stemmedArray, false if not
    */
   private boolean checkWord(ArrayList<String> stemmedArray, Stemmer stemmer)
   {
     for(int k=0; k < stemmedArray.size(); k++)
     {
       String doubleWord = stemmedArray.get(k).toString();
       String compare = stemmer.toString();	
       if(compare.equals(doubleWord))
       {
         return true;
       }
     }
     return false;
   }
 
   /**
    * @about Processes the input files of the list of stop words into 
    * listOfStopWords
    * Module written by Adam
    * @throws FileNotFoundException
    * @return listOfStopWords - ArrayList of stop words
    */
   private ArrayList<String> generateStopWords()
   {
     ArrayList<String> stopwords = new ArrayList<String>();
     try
     {
       File source = new File("stopwords.txt");
       Scanner scanner = new Scanner(new FileReader(source));
       try
       {
         while (scanner.hasNextLine())
         {
           stopwords.add(scanner.nextLine());
         }
       } 
       finally
       {
           scanner.close();
       }
     }
     catch (FileNotFoundException e)
     {
       System.out.println(e.getMessage());
     }
     return stopwords;
   }
 
   /**
    * @about Removes stop words by matching each comment word against the 
    * entire list of stop words and adding the comment to newArray if the 
    * comment does not match any stop words.
    * Module written by Adam
    * @param arrayToTrim - Comment or code array to be trimmed of stop words
    * @param stopwords - Array of stop words to check against
    * @return newArray - Array to replace un-trimmed comment or code array
    */
   private ArrayList<String> trimming(ArrayList<String> arrayToTrim)
   {
     ArrayList<String> newArray = new ArrayList<String>();
     ArrayList<String> stopwords = generateStopWords();
     for (int i = 0; i < arrayToTrim.size(); i++)
     {
         int counter = 0;
         String comment = arrayToTrim.get(i);
         comment = comment.trim();
         comment = comment.toLowerCase();
         if (!(comment.length() <3)){
         for (int j = 0; j < stopwords.size(); j++)
         {
           String stopword = stopwords.get(j);
           stopword = stopword.trim();
           stopword = stopword.toLowerCase();
           if (!comment.equals(stopword))
           {
             counter++;
           }
         }
         if (counter == stopwords.size())
         {
           newArray.add(comment);
         }
       }
     }
     return newArray;
   }
 }
