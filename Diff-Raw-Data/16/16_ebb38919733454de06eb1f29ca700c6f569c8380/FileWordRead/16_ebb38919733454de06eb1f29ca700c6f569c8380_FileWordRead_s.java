 /**
  * This program is for reading words from input file                                                       
  * First open input file for reading: in  = new BufferedInputStream(new FileInputStream(String fileName)); 
  * Next create an object of class FileWord Read: FileWordRead readWords = new FileWordRead(in);            
  * To read a word, first check that there is a next word in file: if ( readWords.hasNextWord() )           
  * Finally, to read a word, use:  String nextWord = readWords.nextWord();  
  */                                 
 
 package spellcheck;
 
 
 import java.io.BufferedInputStream;
 import java.io.File;
 //import java.io.*;
 
 public class FileWordRead {
     private BufferedInputStream in;
     private String nextWord;
     private StringBuffer buf;
     private boolean endOfFile;
 
     public FileWordRead(BufferedInputStream inFile) throws java.io.IOException
     {
     	in = inFile;
         endOfFile = false;
         nextWord  = readWord(); 
     }
 
     private String readWord() throws java.io.IOException {
     	
         int ch;
         char nextChar;
         StringBuffer buf = new StringBuffer();
 
 		ch = in.read();
 		if ( ch == -1 ){            
 		    endOfFile = true;
 		    return(null);
 		}
 
         nextChar = Character.toLowerCase((char) ch);
         while ( ! (nextChar >= 'a' && nextChar <= 'z' )){
 		    ch = in.read();
 		    if ( ch == -1 ){            
 				endOfFile = true;
 				return(null);
 		    }
	    nextChar = Character.toLowerCase((char) ch);
         }
 
 		while ( nextChar >= 'a' && nextChar <= 'z' ){
 	        buf.append(nextChar);  
 		    ch = in.read();
 		    if ( ch == -1 ){            
 				endOfFile = true;
 				return(buf.toString());
 		    }
 	
 	        nextChar = Character.toLowerCase((char) ch);
 		}
 		
 	    return (buf.toString());
     }
 
 
     public boolean hasNextWord() {
        if ( nextWord != null ) 
     	   return(true);
        else 
     	   return(false);
     }
 
     public String nextWord() throws java.io.IOException{
         String toReturn = nextWord;
         if ( !endOfFile ) 
         	nextWord = readWord();
         else 
         	nextWord = null;
         
         return(toReturn);
     }
 }
 
 
 
 
 
