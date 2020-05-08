 import java.util.*;
 import java.io.*;
 import java.lang.String;
 import java.util.Collections;
 
 public class ScrabbleSuggester {
    public int numMatchesDesired;
    public int curNumMatches;
    public String queryString;
    public String pathOfSmallestFile;
    
    public ArrayList<String> topWords;
    public Scanner fileScanner;
    
    public ScrabbleSuggester(String queryStr, int k) {
       this.queryString = queryStr;
       this.numMatchesDesired = k;
       this.curNumMatches = 0;
       this.pathOfSmallestFile ="";
    }
    
    public ScrabbleSuggester(String queryStr) {
       this.queryString = queryStr;
       this.numMatchesDesired = 10;
       this.curNumMatches = 0;
       this.pathOfSmallestFile ="";
    }
    
    public ScrabbleSuggester() {
       this.queryString = "z";
       this.numMatchesDesired = 10;
       this.curNumMatches = 0;
       this.pathOfSmallestFile ="";
    }
    
    
    
    public String getPathOfSmallestLetterContainsFile() {
       String filePrefix = "./TestOutput/wordsContaining_";
       String fileSuffix = ".txt";
       long minSize = Long.MAX_VALUE;
       
       for( char ch : queryString.toCharArray() ) {
          String tempFilename = filePrefix + String.valueOf(ch) + fileSuffix;
          File targetFile = new File(tempFilename);
          if( targetFile.length() < minSize ) {
             minSize = targetFile.length();
             pathOfSmallestFile = tempFilename;
          }
       }
       
       return pathOfSmallestFile;
    }
    
    public void cleanup() {
       if( this.fileScanner != null ) {
          fileScanner.close();
       }
    }
    
    public boolean openFileScanner() {
       boolean fileFound = false;
       try {
          File smallestFile = new File(this.pathOfSmallestFile);
          if( smallestFile.exists() && smallestFile.isFile() ) {
             fileScanner = new Scanner( pathOfSmallestFile );
             fileFound = true;
          }
       } catch (FileNotFoundException e ) {
         System.err.println("File not found at "+this.pathOfSmallestFile);
          return fileFound;
       }
       return fileFound;
    }
    
    
    public boolean getTopWordsFromFile( ) {
       if( this.fileScanner == null ) {
          this.openFileScanner();
       }
       int i = 0;
       
       String line = "";
       while ( fileScanner.hasNextLine() && curNumMatches < numMatchesDesired ) {
       
          line = fileScanner.nextLine();
          
          if( line.contains(queryString) ) {
             curNumMatches++;
             topWords.add(line);
          }
          
       } 
    
    }
    
    public void printTopScoringWords() {
       
       System.out.println("Top "+curNumMatches+" words containing "+queryString+":");
       
       for(String word: topWords ) {
          System.out.println(word);
       }
    }
    
    public void runSuggester() {
       this.getPathOfSmallestLetterContainsFile();
       this.getTopWordsFromFile();
       this.printTopScoringWords();
       this.cleanup();
    }
 }
