 package dictionary;
 import java.io.BufferedReader;
import java.io.FileInputStream;
 import java.io.IOException;
import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Class for importing words from a text file into a dictionary. The file is read
  * line by line, and the basic form of each word is saved into a hashset
  * representing the dictionary.
  * 
  * @author Diana Gren, Frej Connolly. 
  */
 public class RegexDictionary {
 	private static final String WORDLINEREGEX = "\\d+r\\d+<\\w+>[\\wåäö]+:(\\w*\\W*)*";
 
 	private static List<String> dictionary;
 	
 	/**
 	 * Read file and import words into hashset.
 	 * 
 	 * @param fileName Name of file to import.
 	 * @return Hashset containing all words.
 	 */
 	public static List<String> readDictionaryFromFile(String fileName) {
 		List<String> wordList = new ArrayList<String>();
 		BufferedReader br = null;
 		// 77Collections.sort(dictionary);
 		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					fileName), "UTF-8"));
 			String textLine;
 			while((textLine = br.readLine()) != null) {		
 				textLine.toLowerCase();
 				Pattern regexLineStartsWithInteger = Pattern
 						.compile(WORDLINEREGEX);
 				Matcher lineWithWord = regexLineStartsWithInteger.matcher(textLine);
 				
 				if (lineWithWord.matches()) {
 					String word = extractWordFromStringLine(textLine);
 					if (word != null) {						
 						wordList.add(word);
 					}
 				}								
 			}
 		} catch (IOException e){
 			System.err.println("File not found " + e);		
 		} finally {
 			try {
 				if (br != null)
 					br.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		Collections.sort(wordList);
 		return wordList;
 	}
 	
 	/**
 	 * Extract a word in basic form from an input line received when reading the
 	 * file.
 	 * 
 	 * @param stringLine Line to extract word from.
 	 * @return String Extracted word.
 	 */
 	private static String extractWordFromStringLine(String stringLine) {
 		String[] lineSplit = stringLine.split("[<|>|:]");
 		if (!lineSplit[1].equals("egennamn")) {
 			if (!lineSplit[2].equals(""))
 				return lineSplit[2];
 		}
 		return null; 
 	}
 	
 	/**
 	 * Print out all words stored in dictionary. Each word on new line.
 	 */
 	public static void printDictionary() {
 		StringBuilder output = new StringBuilder();
 		for (String word : dictionary) {
 			output.append(word + "\n");
 		}
 		System.out.println(output.toString());
 	}
 }
