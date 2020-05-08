 import java.util.*;
 
 public interface PhraseParser {
 	
		public void parse(Webpage w);
	
 		public HashMap<String, Integer> getPhrase(int length);
 		
 		public String getDescription(String title); //Not used currently.
 }
