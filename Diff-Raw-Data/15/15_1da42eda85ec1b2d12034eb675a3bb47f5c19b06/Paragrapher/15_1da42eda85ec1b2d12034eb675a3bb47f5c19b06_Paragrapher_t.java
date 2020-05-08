 package ie.dcu.cngl.tokenizer;
 
 import java.util.*;
 
 /**
  * Separates the tokenized content into paragraphs. A paragraph is marked by a double newline.
  * @author Shane
  *
  */
 public class Paragrapher implements IParagrapher {
 	
 	private static Paragrapher instance;
     
     private Paragrapher() {
     }
 
     /**
      * Initializing a paragrapher is computationally expensive, so it exists as a singleton.
      * @return Paragrapher singleton.
      */
     public static Paragrapher getInstance() {
     	if(instance == null) {
     		synchronized(Paragrapher.class) {
 	    		instance = new Paragrapher();
     		}
     	}
     	return instance;
     }
 
     /**
      * Tokenize the content, and divide the tokens by paragraph.
      * @return A 2-dimensional array of each paragraph and its tokens.
      */
     public ArrayList<String> paragraph(String s) {
     	
     	ArrayList<String> paragraphs = new ArrayList<String>();
     	String newline = "\\r?\\n";
     	//a paragraph is set as two consecutive newlines
     	//which could be separated by any number of whitespaces
    	paragraphs = new ArrayList<String>(Arrays.asList(s.split(newline+"\\s*"+newline)));
     	
     	return paragraphs;
     }
 
 }
 
