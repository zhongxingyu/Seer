 import java.util.*;
 
 public interface PhraseParser {
		/**
		 * This method is used to get a map of phrases with certain length.
		 * @param length the length of phrases desired
		 * @return a HaspMap, the key is the phrase and the value is the corresponding frequency .
		 */
 		public HashMap<String, Integer> getPhrase(int length);
 		
		/**
		 * Currently not used.
		 * @param title
		 * @return
		 */
 		public String getDescription(String title); //Not used currently.
 }
