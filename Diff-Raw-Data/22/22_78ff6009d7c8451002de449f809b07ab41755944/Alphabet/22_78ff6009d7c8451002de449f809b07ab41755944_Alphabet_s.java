 package dictionary;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 
 public class Alphabet {
 	public static HashMap<Character, Integer> letterAmounts = new HashMap<Character, Integer>();
 	static HashMap<Character, Integer> letterPoints = new HashMap<Character, Integer>();
 
 	public static char[] alphabet = new char[] { 'A', 'B', 'C', 'D', 'E', 'F',
 			'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'R', 'S', 'T',
 			'U', 'V', 'X', 'Y', 'Z', 'Å', 'Ä', 'Ö' };
 	/**
 	 * Initialize alphabet with points and amount of each letter depending on
 	 * which language we are working with. Read info from a file representing
 	 * the chosen language and put info into hashmap.
 	 * 
 	 * @param language
 	 */
 	public static void initializeAlphabet(String language) {
 		String filename = "languages/" + language + ".alphabet";
 		try {
 			readFromFile(filename);
 		} catch (FileNotFoundException e) {
 			System.err.println("FileNotFoundException: Could not find file: "
 					+ filename);
 		} catch (IOException e) {
 			System.err.println("IOException: Could not read from file!");
 		}
 	}
 
 	/**
 	 * Read info about each letter from file. The format of the file is comments
 	 * in the beginning that start with '#'. And each following row represents a
 	 * letter and is written as [letter] [amount] [points].
 	 * 
 	 * Example:
 	 * # Comments in the beginning of file.
 	 * # Another comment.
 	 * A 1 2
 	 * B 3 4
 	 * 
 	 * etc.
 	 * 
 	 * @param filename
 	 * @throws IOException
 	 */
 	private static void readFromFile(String filename)
 			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
 
 		String line;
 		while ((line = reader.readLine()) != null) {
 			if (line.startsWith("#"))
 				continue;
 
 			String[] lineArguments = line.split(" ");
 			char letter = lineArguments[0].toCharArray()[0];
 			int amount = Integer.parseInt(lineArguments[1]);
 			int points = Integer.parseInt(lineArguments[2]);
 
 			letterAmounts.put(letter, amount);
 			letterPoints.put(letter, points);
 		}
 	}
 
 	public static int getLetterPoint(char letter) {
 		return letterPoints.get(letter);
 	}
 	
 	public static int getLetterAmount(char letter) {
 		return letterAmounts.get(letter);
 	}
 	
 }
