 package dictionary;
 
 import java.util.List;
 
 public class Dawg {
 	
 	private static Node rootNode = new Node('#');
 
 	//
 	// public Dawg() {
 	// rootNode = new Node((char) 0);
 	// }
 	
 	/**
 	 * Create a DAWG (directed acyclic word graph) from text file with list of words.
 	 * 
 	 * @param String fileName
 	 */
 	public static void initDawg(String fileName) {
 		long startTime = System.currentTimeMillis();
 		List<String> dictionary = RegexDictionary
 				.readDictionaryFromFile(fileName);
 
 		for (String word : dictionary) {
 			char[] wordarray = word.toUpperCase().toCharArray();
 			addWordToDawg(rootNode, wordarray, 0);
 		} 
 		long endTime = System.currentTimeMillis();
 		System.out.println("Dawg build time: " + (endTime - startTime) 
 				+ " milliseconds.");
 		
 		// Measure time for searching the word "abortör" 500 times
 		startTime = System.currentTimeMillis();			
 		boolean found = false;
 		for(int i=0; i<52000; i++) 	
 			found = findWord("ABORTÖR");
 		endTime = System.currentTimeMillis();
 		System.out.println("Search time: " + (endTime - startTime) 
 				+ " milliseconds. " + found);
 	}
 	
 	/**
 	 * Add a word to the dawg. Creates new Node objects for letters not found.
 	 * 
 	 * @param Node currentNode
 	 * @param char[] word inserted to DAWG
 	 * @param int letterIndex index to select letter in word 
 	 */
 	private static void addWordToDawg(Node currentNode, char[] word,
 			int letterIndex) {
 		
 		if (letterIndex >= word.length) {
 			currentNode.setEow();
 //			System.out.println("eow");
 			return;
 		}
 		
 		char letter = word[letterIndex];
 		Node child;
 		if (currentNode.getChildren().containsKey(letter)) {
 			child = currentNode.getChildren().get(letter);	
 //			System.out.println(letter + " found. Following.");
 		} else {			
 			child = currentNode.addChild(letter);			
 //			System.out.println(letter + " added.");
 		}
 		addWordToDawg(child, word, letterIndex + 1);
 //		System.out.println("Next!");
 	}
 
 	/**
 	 * Search for a word string in the DAWG.
 	 *  
 	 * @param word search string
 	 * @return boolean true if found
 	 */
 	public static boolean findWord(String word) {
 		char[] wordArray = word.toCharArray();
		return findWordRecursively(rootNode, wordArray, 0);
 	}
 	
 	/** 
 	 * Help method for recursively search for a word string in the DAWG.
 	 * 
 	 * @param currentNode
 	 * @param word
 	 * @param letterIndex
 	 * @return boolean true if found
 	 */
	private static boolean findWordRecursively(Node currentNode, char[] word,
 			int letterIndex) {
 		// Finished searching through all letters  
		if (letterIndex >= word.length) {
			return true;
 		}
 		
		char letter = word[letterIndex];		
 		
 		if (currentNode.getChildren().containsKey(letter)) {	
 			// Continue searching for next letter
 			Node child = currentNode.getChildren().get(letter);			
			return findWordRecursively(child, word, letterIndex + 1);			
 		} else {
 			// Letter not found.
 			return false;
 		}			
 	}
 	
 	public static Node getRootNode() {
 		return rootNode;
 	}
 	// public static void main (String[] args){
 	// new Dawg().initDawg("dictionary/dsso-1.52_utf8.txt");
 	// }
 }
