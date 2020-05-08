 /**
  * This is the suggestion class. It generates a suggestion based on the
  * activation of the various flags.
  * @author hacheson
  */
 
 package autocorrect;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 public class SuggestionGenerator implements Generator {
 	private String text_;
 	private boolean isLED_;
 	private boolean isPrefix_;
 	private boolean isWhitespace_;
 	private boolean isSmart_;
 	private WordStructure trie_;
 	private int maxEdits_;
 	
 	
 	/**
 	 * Constructor for a suggestion.
 	 * @param isLED Whether to use levenstein edit distance.
 	 * @param isPrefix Whether to use prefix matching.
 	 * @param isWhitespace Whether to use whitespace.
 	 */
 	public SuggestionGenerator(boolean isLED, boolean isPrefix, boolean isWhitespace, int maxEdits, WordStructure trie){
 		isLED_ = isLED;
 		isPrefix_ = isPrefix;
 		isWhitespace_ = isWhitespace;
 		maxEdits_ = maxEdits;
 		trie_ = trie;
 	}
 	
 	/**
 	 * Sets the text that should be suggested.
 	 * @param text The text to be suggested.
 	 */
 	public void setText(String text){
 		text_ = text;
 	}
 	
 	/**
 	 * Gets a list of suggestions based on prefix, LED, and whitespace.
 	 * @return A list of suggestions.
 	 */
 	public List<String> generate(){
 		//String[] textArr = text_.split(" ");
 		//String text = textArr[textArr.length-1].replace("\n", "");
 		String text = text_;
 		ArrayList<List<String>> allSuggestions = new ArrayList<List<String>>();
 		if(isPrefix_){
 			allSuggestions.add(getWordsWrapper(text));
 		}
 		if(isLED_){
 			allSuggestions.add(LED(text, maxEdits_));
 		}
 
 		if(isWhitespace_){
 			allSuggestions.add(whiteSpace(text));
 		}
 		
 		//If all are turned off, return the matched word if it exists.
 		if(!isPrefix_ && !isLED_ && !isWhitespace_ && !isSmart_){
 			return findMatch(text);
 		}
 		return combineLists(allSuggestions);
 	}
 	
 	/**
 	 * Returns a list with an the exact match of the word if it exists.
 	 * @param text The text to find a match on.
 	 * @return The list containing the word if it exists.
 	 */
 	public List<String> findMatch(String text){
 		TrieNode node = trie_.find(text);
 		ArrayList<String> match = new ArrayList<String>();
 		if(node!=null && node.getTerminal()){
 			match.add(node.getWord());
 		}
 		return match;
 	}
 	
 	/**
 	 * Combines a list of lists without duplicates.
 	 * @param allSuggestions The 2D list to combine.
 	 * @return A 1D array list of combined elements.
 	 */
	public static List<String>combineLists(List<List<String>> allSuggestions){
 		ArrayList<String> combinedSuggestions = new ArrayList<String>();
 		for(int i = 0; i < allSuggestions.size();i++){
 			if(allSuggestions.get(i)!=null){
 				for(int j = 0; j<allSuggestions.get(i).size();j++){
 					if(!combinedSuggestions.contains((allSuggestions.get(i).get(j))))
 						combinedSuggestions.add(allSuggestions.get(i).get(j));
 				}
 			}
 		}
 		return combinedSuggestions;
 	}
 	
 	/**
 	 * White space function, generates suggestions given that that two words are
 	 * located in the text.
 	 * @param text The text to check if two words exist in.
 	 */
 	public List<String> whiteSpace(String text){
 		ArrayList<String> suggestions = new ArrayList<String>();
 		for(int i = 1; i < text.length(); i++){
 			TrieNode nodeA = trie_.find(text.substring(0, i));
 			TrieNode nodeB = trie_.find(text.substring(i, text.length()));
 			if(nodeA!=null && nodeB !=null && nodeA.getTerminal() && nodeB.getTerminal()){
 				String suggestion = nodeA.getWord() + " " + nodeB.getWord();
 				suggestions.add(suggestion);
 			}
 		}
 		return suggestions;
 	}
 	
 	/**
 	 * Computes the levenstein edit distance of a given word, given a maxNumber of edits.
 	 * Calls LED Recursive to dynamically pass arrays of the parent nodes through to the children.
 	 * @param text The text to find the edit distance on.
 	 * @param maxEdits The max number of edits allowed.
 	 * @return ArrayList<Integer> The words within an LED of 2 from the given word.
 	 */
 	public List<String> LED(String text, int maxEdits){
 		ArrayList<Integer> rootEdits = new ArrayList<Integer>();
 		//Set root arr to 1 more than the number of letters.
 		for(int i = 0; i < text.length()+1; i++){
 			rootEdits.add(i);
 		}
 		return LEDRecursive(text, maxEdits, trie_.getRoot(), new ArrayList<String>(), rootEdits);
 	}
 	
 	/**
 	 * The function that calculates the LED for each node if necessary. Calculates the array 
 	 * at each level.
 	 * @param text The text that is being searched.
 	 * @param maxEdits The max number of edits.
 	 * @param curr The curr node searching.
 	 * @param suggestions The list of suggestions accumulated so far.
 	 * @return the ArrayList containing smart suggestions.
 	 */
 	public List<String>LEDRecursive(String text, int maxEdits, TrieNode curr, ArrayList<String> suggestions,
 			ArrayList<Integer> editList){
 		if(curr.getParent()!=null){
 			//The bottom right int of the current edit list.
 			int checkBottom = editList.get(editList.size()-1);
 			if(checkBottom<=maxEdits && curr.getTerminal()){
 				suggestions.add(curr.getWord());
 			}
 		}
 		//Iterate through all the children.
 		Iterator<TrieNode> it = curr.getChildren().values().iterator();
 		TrieNode node = null;
 		try{
 			node = it.next();
 		} catch(NoSuchElementException e){
 			
 		}
 		while(node!=null){
 			if(belowMaxEdits(maxEdits, editList)){
 				ArrayList<Integer> nodeEdits = findEdits(editList, text, node.getChar());
 				LEDRecursive(text, maxEdits, node, suggestions, nodeEdits);
 			}
 			else
 				return suggestions;
 			try{
 				node = it.next();
 			} catch(NoSuchElementException e){
 				break;
 			}
 		}
 		return suggestions;
 	}
 	
 	/**
 	 * Finds the edit array for the child node given the parent's node.
 	 * @param parentList The list of integers representing the edit's from the parent.
 	 * @param text The text to find edits on.
 	 * @param c The char that we are matching in the child.
 	 * @return List of edits for the child.
 	 */
 	public ArrayList<Integer> findEdits(ArrayList<Integer> parentList, String text, char c){
 		ArrayList<Integer> childList = new ArrayList<Integer>();
 		//Edge case, should be one more than the parent's list.
 		childList.add(parentList.get(0)+1);
 		for(int i = 1; i < parentList.size(); i++){
 			int min;
 			if(text.charAt(i-1) == c){
 				min = Math.min(Math.min(parentList.get(i-1), parentList.get(i)+1),
 						childList.get(i-1) + 1);
 			}
 			else{
 				min = Math.min(Math.min(parentList.get(i-1)+1, parentList.get(i)+1),
 						childList.get(i-1) + 1);
 			}
 			childList.add(min);
 		}
 		return childList;
 	}
 	
 	/**
 	 * Gets a list of words given a prefix.
 	 * @param prefix
 	 * @return A list of words with a given prefix.
 	 */
 	public List<String> getWordsWrapper(String prefix){
 		//1. Find the node that has the given prefix
 		//2. Recursively find all words with that given prefix.
 		TrieNode node = trie_.find(prefix);
 		if(node == null)
 			return null;
 		
 		return getWords(prefix, null, node);		
 	}
 	
 	/**
 	 * Gets words by recursively traversing the tree starting from the top.
 	 * @param prefix The prefix to find.
 	 * @param words ArrayList of words that come from the given prefix.
 	 * @param curr The current node of traversal.
 	 * @return An array list of words that come after that prefix.
 	 */
 	public List<String> getWords(String prefix, ArrayList<String> words, TrieNode curr){	
 		if(words == null)
 			words = new ArrayList<String>();
 		
 		if(curr.getTerminal()){
 			words.add(curr.getWord());
 		}
 		Iterator<TrieNode> it = curr.getChildren().values().iterator();
 		TrieNode node = null;
 		try{
 			node = it.next();
 		} catch(NoSuchElementException e){
 			
 		}
 		while(node!=null){
 			getWords(prefix, words, node);
 			try{
 				node = it.next();
 			} catch(NoSuchElementException e){
 				break;
 			}
 		}
 		return words;
 	}
 	
 	/*public ArrayList<String>LED(String text, int maxEdits){
 		ArrayList<Integer> temp = new ArrayList<Integer>();
 		//Set root arr to 1 more than the number of letters.
 		for(int i = 0; i < text.length()+1; i++){
 			temp.add(i);
 		}
 		trie_.getRoot().setEditList(temp);
 		return LEDRecursive(text, maxEdits, trie_.getRoot(), new ArrayList<String>());
 	}*/
 	
 	/**
 	 * The function that calculates the LED for each node if necessary. Calculates the array 
 	 * at each level.
 	 * @param text The text that is being searched.
 	 * @param maxEdits The max number of edits.
 	 * @param curr The curr node searching.
 	 * @param suggestions The list of suggestions accumulated so far.
 	 * @return the ArrayList containing smart suggestions.
 	 */
 	/*public ArrayList<String>LEDRecursive(String text, int maxEdits, TrieNode curr, ArrayList<String> suggestions){
 		if(curr.getParent()!=null){
 			int checkBottom = curr.getEditList().get(curr.getEditList().size()-1);
 			if(checkBottom<=maxEdits && curr.getTerminal()){
 				suggestions.add(curr.getWord());
 			}
 		}
 		
 		Iterator<TrieNode> it = curr.getChildren().values().iterator();
 		TrieNode node = null;
 		try{
 			node = it.next();
 		} catch(NoSuchElementException e){
 			
 		}
 		while(node!=null){
 			if(belowMaxEdits(maxEdits, node.getParent().getEditList())){
 				node.setEditList(findEdits(node.getParent().getEditList(), text, node.getChar()));
 				LEDRecursive(text, maxEdits, node, suggestions);
 			}
 			else
 				return suggestions;
 			try{
 				node = it.next();
 			} catch(NoSuchElementException e){
 				break;
 			}
 		}
 		return suggestions;
 	}*/
 	
 	/**
 	 * Finds the edit array for the child node given the parent's node.
 	 * @param parentList The list of integers representing the edit's from the parent.
 	 * @param text The text to find edits on.
 	 * @param c The char that we are matching in the child.
 	 * @return List of edits for the child.
 	 */
 	/*public ArrayList<Integer> findEdits(ArrayList<Integer> parentList, String text, char c){
 		ArrayList<Integer> childList = new ArrayList<Integer>();
 		//Edge case, should be one more than the parent's list.
 		childList.add(parentList.get(0)+1);
 		for(int i = 1; i < parentList.size(); i++){
 			int min;
 			if(text.charAt(i-1) == c){
 				min = Math.min(Math.min(parentList.get(i-1), parentList.get(i)+1),
 						childList.get(i-1) + 1);
 			}
 			else{
 				min = Math.min(Math.min(parentList.get(i-1)+1, parentList.get(i)+1),
 						childList.get(i-1) + 1);
 			}
 			childList.add(min);
 		}
 		return childList;
 	}*/
 	
 	
 	/**
 	 * Returns true if the current node should continue editing, ie
 	 * if there is any index in the parent's array that is less than or 
 	 * equal to the maxEdits.
 	 * @param maxEdits maxEdits allowed.
 	 * @param list list of integers searching.
 	 * @return
 	 */
 	public boolean belowMaxEdits(int maxEdits, ArrayList<Integer> list){
 		for (Integer i: list){
 			if(i<=maxEdits)
 				return true;
 		}
 		return false;
 	}
 	
 
 	
 	/**
 	 * Inserts a given character at a given position.
 	 * @param index The index at which to insert the character.
 	 * @param newChar The character in which to insert.
 	 * @param text The string to change on.
 	 * @return The new string with the character inserted.
 	 */
 	public static String insertCharacterAt(int index, char newChar, String text){
 		try{
 			String firstSubstring = text.substring(0, index);
 			String secondSubstring = text.substring(index, text.length());
 			return firstSubstring + newChar + secondSubstring;
 		} catch(StringIndexOutOfBoundsException e){
 			System.out.println("That is not a valid string");
 			return null;
 		}
 	}	
 	
 	/**
 	 * Replaces a given character at a given position.
 	 * @param index The index at which to replace the String.
 	 * @param newString The string in which to change.
 	 * @param text The string to change on.
 	 * @return The new string with the String replaced.
 	 */
 	public static String replaceCharacterAt(int index, String newString, String text){
 		try{
 			String firstSubstring = text.substring(0, index);
 			String secondSubstring = text.substring(index+1, text.length());
 			return firstSubstring + newString + secondSubstring;
 		} catch(StringIndexOutOfBoundsException e){
 			System.out.println("That is not a valid string");
 			return null;
 		}
 	}
 }
