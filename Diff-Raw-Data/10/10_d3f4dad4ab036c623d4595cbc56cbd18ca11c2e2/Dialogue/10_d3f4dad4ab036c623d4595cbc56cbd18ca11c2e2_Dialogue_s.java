 import java.io.*;
 import java.util.*;
 
 /* this class is used to load up long, verbose text into the game without having to  
  include it directly into the src. */
 
 public class Dialogue {
 	
 /* this method takes the name of a .txt file as an argument and creates it in the form of a Scanner object
  * and sets the delimiter of the new Scanner to a single space. */
     public static Scanner loadFile(String fileName) {
     	Scanner s = null;
 		try {
 			s = new Scanner(new File(fileName));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		s.useDelimiter(" ");
     	return s;
     }
    
 // returns the entire text of the Scanner
     public static String getEntireFile(Scanner s) {
     	String nextPart = "";
     	
     	while(s.hasNext()) {
     		nextPart += s.next() + " ";
     	}
     	return nextPart;
     }
 
/* returns a portion of the text contained within the Scanner. the argument totalWords decides how many wordss
 * from the Scanner souce will be returned. I believe it starts at 0 inclusive and ends at totalWords exclusive,
  * but I'll have to double check */
     public static String getPartialFile(Scanner s, int totalWords) {
     	String nextPart = "";
     	
     	for (int i = 0; i < totalWords; i++) {
     		nextPart += s.next() + " ";
     	}
     	return nextPart;
     }
     
/* returns a portopm of the text contained within the Scanner. the argument beginAtWord describes the nth word
  * for the Scanner to start at. likewise, the argument endAtWord describes the nth word for the Scanner to stop at.
 * like the previous method, I'm unsure which numbers are inclusive/excusive, so I'll update with that information later. */
     public static String getPartialFile(Scanner s, int beginAtWord, int endAtWord) {
     	String nextPart = "";
     	
     	for (int i = 0; i < beginAtWord; i++) {
     		s.next();
     	}
     	
     	for (int i = beginAtWord; i < endAtWord; i++) {
     		nextPart += s.next() + " ";
     	}
     	return nextPart;
     }
 }
