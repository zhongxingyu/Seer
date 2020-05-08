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
       topWords = new ArrayList<String>(numMatchesDesired+1);
    }
    
    public ScrabbleSuggester(String queryStr) {
       this.queryString = queryStr;
       this.numMatchesDesired = 10;
       this.curNumMatches = 0;
       this.pathOfSmallestFile ="";
       topWords = new ArrayList<String>(numMatchesDesired+1);
 
    }
    
    public ScrabbleSuggester() {
       this.queryString = "z";
       this.numMatchesDesired = 10;
       this.curNumMatches = 0;
       this.pathOfSmallestFile ="";
       topWords = new ArrayList<String>(numMatchesDesired+1);
 
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
             System.err.println(" smallest filesize = " +minSize);
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
             fileScanner = new Scanner( smallestFile );
             fileFound = true;
          }
       } catch (Exception e ) {
          System.err.println("ERROR in open file Scanner: "+e.getMessage() );
          return fileFound;
       }
       return fileFound;
    }
    
    
    public void getTopWordsFromFile( ) {
       if( this.fileScanner == null ) {
          this.openFileScanner();
       }
       int i = 0;
       
       String line = "";
       while ( fileScanner.hasNextLine() && curNumMatches < numMatchesDesired ) {
          line = fileScanner.nextLine();
          System.out.println("line: "+line+"; cur:"+curNumMatches+"; total:"+numMatchesDesired);
 
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
       
       System.err.println("getPathOfSmallestLetterContainsFile");
 
       this.getPathOfSmallestLetterContainsFile();
       System.err.println(" smallest = "+this.pathOfSmallestFile);
       
       this.openFileScanner();
       
       System.err.println("getTopWordsFromFile");
       this.getTopWordsFromFile();
       System.err.println("topWords.size() = "+topWords.size());
 
       
       System.err.println("printTopScoringWords");
 
       this.printTopScoringWords();
       this.cleanup();
    }
 }
