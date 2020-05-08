 /**
  * Tests for the SuggestionGenerator methods. Tests prefix matching and LED against naive implementations
  * of both.
  */
 
 package autocorrect;
 
 import static org.junit.Assert.assertTrue;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 public class SuggestionGeneratorTest {
 	/**
 	 * Tests the whitespace method when there is one split.
 	 */
 	@Test
 	public void testWhitespaceOne() {
 		List<List<String>> wordLists = new ArrayList<List<String>> ();
 		ArrayList<String> words = new ArrayList<String> ();
 		words.add("ab");
 		words.add("abc");
 		wordLists.add(words);
 		WordStructure trie = new Trie(wordLists);
 		SuggestionGenerator generator = new SuggestionGenerator(true, true, true, 2, trie);
 		assertTrue(generator.whiteSpace("ababc").get(0).equals("ab abc"));
 		assertTrue(generator.whiteSpace("ababc").size() == 1);
 	}
 	
 	/**
 	 * Tests whitespace when there are multiple returns.
 	 */
 	@Test
 	public void testWhitespaceMultiple(){
 		List<List<String>> wordLists = new ArrayList<List<String>> ();
 		ArrayList<String> words = new ArrayList<String> ();
 		words.add("a");
 		words.add("ab");
 		words.add("aba");
 		words.add("abc");
 		words.add("bc");
 		words.add("babc");
 		wordLists.add(words);
 		Trie trie = new Trie(wordLists);
 		SuggestionGenerator generator = new SuggestionGenerator(true, true, true, 2, trie);
 		assertTrue(generator.whiteSpace("ababc").get(0).equals("a babc"));
 		assertTrue(generator.whiteSpace("ababc").get(1).equals("ab abc"));
 		assertTrue(generator.whiteSpace("ababc").get(2).equals("aba bc"));
 		assertTrue(generator.whiteSpace("ababc").size() == 3);
 	}
 	
 	/**
 	 * Tests the whitespace method when there is no findings.
 	 */
 	@Test
 	public void testWhitespaceZero() {
 		List<List<String>> wordLists = new ArrayList<List<String>> ();
 		ArrayList<String> words = new ArrayList<String> ();
 		words.add("ab");
 		words.add("abc");
 		wordLists.add(words);
 		Trie trie = new Trie(wordLists);
 		SuggestionGenerator generator = new SuggestionGenerator(true, true, true,2, trie);
 		assertTrue(generator.whiteSpace("zzz").size()==0);
 	}
 	
 	/*******************************************************************
 	 * Tests for the combineList method.
 	 *******************************************************************/
 	/**
 	 * Tests the combine list method. General test.
 	 */
 	@Test
 	public void testCombineList(){
		List<String> list1 = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();
 		list1.add("ab");
 		list1.add("ab");
 		list2.add("bc");
 		list2.add("ab");
 		List<List<String>> lists = new ArrayList<List<String>> ();
 		//ArrayList<ArrayList<String>> lists = new ArrayList<ArrayList<String>>();
 		lists.add(list1);
 		lists.add(list2);
 		assertTrue(SuggestionGenerator.combineLists(lists).get(0).equals("ab"));
 		assertTrue(SuggestionGenerator.combineLists(lists).get(1).equals("bc"));
 	}
 	
 	/**
 	 * Tests the combine list method when one list is null.
 	 */
 	@Test
 	public void testCombineListNull(){
		List<String> list1 = new ArrayList<String>();
		List<String> list2 = null;
 		list1.add("ab");
 		list1.add("bc");
		List<List<String>> lists = new ArrayList<List<String>> ();
 		lists.add(list1);
 		lists.add(list2);
 		assertTrue(SuggestionGenerator.combineLists(lists).get(0).equals("ab"));
 		assertTrue(SuggestionGenerator.combineLists(lists).get(1).equals("bc"));
 	}
 	
 	/*******************************************************************
 	 * Tests for the LED methods.
 	 *******************************************************************/
 	
 	/**
 	 * Tests the replaceCharacterAt method.
 	 */
 	@Test
 	public void testReplaceCharacterAt(){
 		assertTrue(SuggestionGenerator.replaceCharacterAt(1, "", "abc").equals("ac"));
 		assertTrue(SuggestionGenerator.replaceCharacterAt(0, "", "abc").equals("bc"));
 		assertTrue(SuggestionGenerator.replaceCharacterAt(2, "", "abc").equals("ab"));
 		assertTrue(SuggestionGenerator.replaceCharacterAt(0, "d", "abc").equals("dbc"));
 	}
 	
 	/**
 	 * Tests the replaceCharacterAt method.
 	 */
 	@Test
 	public void testInsertCharacterAt(){
 		assertTrue(SuggestionGenerator.insertCharacterAt(1, 'a', "abc").equals("aabc"));
 		assertTrue(SuggestionGenerator.insertCharacterAt(0, 'b', "abc").equals("babc"));
 		assertTrue(SuggestionGenerator.insertCharacterAt(2, 'c', "abc").equals("abcc"));
 	}
 	
 	/**
 	 * Tests the LED method when the word can only be formed by insertions.
 	 */
 	@Test
 	public void testLEDAllInsert(){
 		List<List<String>> wordLists = new ArrayList<List<String>> ();
 		ArrayList<String> words = new ArrayList<String> ();
 		words.add("abc");
 		words.add("dogs");
 		wordLists.add(words);
 		Trie trie = new Trie(wordLists);
 		SuggestionGenerator suggestionGen = new SuggestionGenerator(true, true, true, 2, trie);
 		assertTrue(suggestionGen.LED("a", 2).get(0).equals("abc"));
 		assertTrue(suggestionGen.LED("ds", 2).get(0).equals("dogs"));
 		assertTrue(suggestionGen.LED("og", 2).get(0).equals("dogs"));
 	}
 	
 	/**
 	 * Tests the LED method when the word can only be formed by matches.
 	 */
 	@Test
 	public void testLEDAllMatch(){
 		List<List<String>> wordLists = new ArrayList<List<String>> ();
 		ArrayList<String> words = new ArrayList<String> ();
 		words.add("abc");
 		words.add("dogs");
 		wordLists.add(words);
 		Trie trie = new Trie(wordLists);
 		SuggestionGenerator suggestionGen = new SuggestionGenerator(true, true, true, 2, trie);
 		assertTrue(suggestionGen.LED("abc", 2).get(0).equals("abc"));
 		assertTrue(suggestionGen.LED("dogs", 2).get(0).equals("dogs"));
 	}
 	
 	/**
 	 * Tests the LED method when the word can only be formed by mismatches.
 	 */
 	@Test
 	public void testLEDAllMismatch(){
 		List<List<String>> wordLists = new ArrayList<List<String>> ();
 		ArrayList<String> words = new ArrayList<String> ();
 		words.add("abc");
 		words.add("dogs");
 		wordLists.add(words);
 		Trie trie = new Trie(wordLists);
 		SuggestionGenerator suggestionGen = new SuggestionGenerator(true, true, true,2, trie);
 		assertTrue(suggestionGen.LED("ddc", 2).get(0).equals("abc"));
 		assertTrue(suggestionGen.LED("cogs", 2).get(0).equals("dogs"));
 		assertTrue(suggestionGen.LED("zzgs", 2).get(0).equals("dogs"));
 	}
 	
 	/**
 	 * Tests the LED method when the word can only be formed by deletions.
 	 */
 	@Test
 	public void testLEDAllDeletions(){
 		List<List<String>> wordLists = new ArrayList<List<String>> ();
 		ArrayList<String> words = new ArrayList<String> ();
 		words.add("abc");
 		words.add("dogs");
 		wordLists.add(words);
 		Trie trie = new Trie(wordLists);
 		SuggestionGenerator suggestionGen = new SuggestionGenerator(true,true, true,2, trie);
 		assertTrue(suggestionGen.LED("abcdd", 2).get(0).equals("abc"));
 		assertTrue(suggestionGen.LED("doogss", 2).get(0).equals("dogs"));
 		assertTrue(suggestionGen.LED("zzdogs", 2).get(0).equals("dogs"));
 	}
 	
 	/**
 	 * Tests the LED method when the word can only be formed by all possible edits.
 	 */
 	@Test
 	public void testLEDAllDeletionsGeneral(){
 		List<List<String>> wordLists = new ArrayList<List<String>> ();
 		ArrayList<String> words = new ArrayList<String> ();
 		words.add("abc");
 		words.add("dogs");
 		wordLists.add(words);
 		Trie trie = new Trie(wordLists);
 		SuggestionGenerator suggestionGen = new SuggestionGenerator(true,true, true,2, trie);
 		assertTrue(suggestionGen.LED("accc", 2).get(0).equals("abc"));
 		assertTrue(suggestionGen.LED("dgd", 2).get(0).equals("dogs"));
 		assertTrue(suggestionGen.LED("fcgs", 2).get(0).equals("dogs"));
 	}
 	
 	/**
 	 * Tests the LED method when the word can only be formed by all possible edits.
 	 */
 	@Test
 	public void testLEDAllLargeWords(){
 		List<List<String>> wordLists = new ArrayList<List<String>> ();
 		ArrayList<String> words = new ArrayList<String> ();
 		words.add("ab");
 		words.add("dogs");
 		wordLists.add(words);
 		Trie trie = new Trie(wordLists);
 		SuggestionGenerator suggestionGen = new SuggestionGenerator(true, true, true,2, trie);
 		assertTrue(suggestionGen.LED("zzzz", 2).size()==0);
 		assertTrue(suggestionGen.LED("dgdasdfsdf", 2).size() == 0);
 		assertTrue(suggestionGen.LED("fcgs", 2).get(0).equals("dogs"));
 	}
 	
 	/**
 	 * Tests the LED method by searching against a naive implementation.
 	 */
 	@Test
 	public void testLEDWithNaive(){
		List<String> words = null;
 		try{
 			words = Main.readFile("/course/cs032/data/autocorrect/dictionary.txt");
 		}
 		catch(IOException e){
 			
 		}
 		Trie trie = new Trie();
 		trie.add(words);
 		
 		ArrayList<String> naiveLEDCAT = naiveLED("cat", 2, trie);
 		ArrayList<String> naiveLEDTREE = naiveLED("tree", 2, trie);
 		//Loops through the list of words and finds all words with that prefix.
 		SuggestionGenerator suggestionGen = new SuggestionGenerator(true, true, true, 2, trie);
 		List<String> fastLEDCAT = suggestionGen.LED("cat", 2);
 		List<String> fastLEDTREE = suggestionGen.LED("tree", 2);
 		
 		//Compares all words in each.
 		for(String word: naiveLEDCAT)
 			assertTrue(fastLEDCAT.contains(word));
 		for(String word: naiveLEDTREE)
 			assertTrue(fastLEDTREE.contains(word));
 	}
 	
 	/**
 	 * The method that returns a list of all possible LED suggestions given the trie
 	 * and a word to search. Calls a helper method to generate more suggestions given 
 	 * the list of current possibilities. 
 	 * @param text Text to work with.
 	 * @param maxEdits The maximum number of edits allowed.
 	 * @return
 	 */
 	public ArrayList<String> naiveLED(String text, int maxEdits, Trie trie){
 		ArrayList<String> editSuggestions = new ArrayList<String>();
 		editSuggestions.add(text);
 		for(int i = 0; i<maxEdits; i++){
 			ArrayList<String> oldEdits = editSuggestions;
 			editSuggestions = getLEDSuggestions(oldEdits);
 		}
 		ArrayList<String> finalSuggestions = new ArrayList<String>();
 		//Now use the trie to see if any of those suggestions actually exist.
 		for(int i = 0; i < editSuggestions.size();i++){
 			if(editSuggestions.get(i).length()!=0){
 				TrieNode node = trie.find(editSuggestions.get(i));
 				if(node!=null && node.getTerminal() ){
 					finalSuggestions.add(node.getWord());
 				}
 			}
 		}
 		return finalSuggestions;
 	}
 	
 	/**
 	 * Generates possible words within one edit of the list of words.
 	 * @param words list of words to find edits within.
 	 * @return The list of words.
 	 */
 	public ArrayList<String> getLEDSuggestions(ArrayList<String> words){
 		ArrayList<String> oldWords = new ArrayList<String>(words);
 		//For number of maxEdits
 		for(int i = 0; i < oldWords.size(); i++){
 			//Mismatch
 			String mismatched;
 			for(int l = 0; l < oldWords.get(i).length();l++){
 				for(char c = 'a'; c<='z'; c++){
 					//Convert to array to pass as String.
 					char[] cArr = {c};
 					String cString = new String(cArr);
 					StringBuilder builder = new StringBuilder(oldWords.get(i));
 					builder.replace(l, l+1, cString);
 					mismatched = builder.substring(0);
 					//mismatched = replaceCharacterAt(l, cString, words.get(i));
 					if(!words.contains(mismatched)){
 						words.add(mismatched);
 					}
 				}
 			}
 			
 			//Insertion
 			String inserted;
 			//To length+1 to insert at end of string
 			for(int l = 0; l < oldWords.get(i).length()+1;l++){
 				for(char c = 'a'; c<='z'; c++){
 					//inserted = insertCharacterAt(l, c, oldWords.get(i));
 					StringBuilder builder = new StringBuilder(oldWords.get(i));
 					builder.insert(l, c);
 					inserted = builder.substring(0);
 					if(!words.contains(inserted)){
 						words.add(inserted);
 					}
 				}
 			}
 			
 			//Deletions
 			String deleted;
 			//To length+1 to insert at end of string
 			for(int l = 0; l < oldWords.get(i).length();l++){
 				//deleted = replaceCharacterAt(l, "", oldWords.get(i));
 				StringBuilder builder = new StringBuilder(oldWords.get(i));
 				builder.replace(l, l+1, "");
 				deleted = builder.substring(0);
 				if(!words.contains(deleted)){
 					words.add(deleted);
 				}
 			}
 		}
 		return words;
 	}
 	
 	
 	/*********************************************************************
 	 * Tests for getWords method. Generates all words with a given prefix.
 	 *********************************************************************/
 	
 	/**
 	 * Tests if the trie can find the appropriate one word given the prefix.
 	 */
 	@Test
 	public void testGetOneWords() {
 		String word = "cat";
 		Trie trie = new Trie();
 		trie.add(word);
 		SuggestionGenerator suggestionGen = new SuggestionGenerator(true, true, true, 2, trie);
 		assertTrue(suggestionGen.getWordsWrapper("ca").get(0).equals(word));
 		assertTrue(suggestionGen.getWordsWrapper("cat").get(0).equals(word));
 	}
 	
 	/**
 	 * Tests that null is returned if no prefix is found.
 	 */
 	@Test
 	public void testGetZeroWords() {
 		String word = "cat";
 		Trie trie = new Trie();
 		trie.add(word);
 		SuggestionGenerator suggestionGen = new SuggestionGenerator(true, true, true, 2, trie);
 		assertTrue(suggestionGen.getWordsWrapper("zzz") == null);
 	}
 	
 	/**
 	 * Tests a general case for the trie with several branches.
 	 */
 	@Test
 	public void testGetWordsGeneral() {
 		String word = "a";
 		String word2 = "to";
 		String word3 = "tea";
 		String word4 = "ted";
 		String word5 = "ten";
 		String word6 = "i";
 		String word7 = "in";
 		String word8 = "inn";
 		Trie trie = new Trie();
 		trie.add(word);
 		trie.add(word2);
 		trie.add(word3);
 		trie.add(word4);
 		trie.add(word5);
 		trie.add(word6);
 		trie.add(word7);
 		trie.add(word8);
 		SuggestionGenerator suggestionGen = new SuggestionGenerator(true, true, true, 2, trie);
 		assertTrue(suggestionGen.getWordsWrapper("t").contains(word2));
 		assertTrue(suggestionGen.getWordsWrapper("t").contains(word3));
 		assertTrue(suggestionGen.getWordsWrapper("t").contains(word4));
 		assertTrue(suggestionGen.getWordsWrapper("t").contains(word5));
 		assertTrue(suggestionGen.getWordsWrapper("in").contains(word7));
 		assertTrue(suggestionGen.getWordsWrapper("in").contains(word8));
 		assertTrue(suggestionGen.getWordsWrapper("a").contains(word));
 		assertTrue(suggestionGen.getWordsWrapper("te").contains(word3));
 		assertTrue(suggestionGen.getWordsWrapper("te").contains(word4));
 		assertTrue(suggestionGen.getWordsWrapper("te").contains(word5));
 	}
 	
 	/**
 	 * Tests if the prefix is the word itself.
 	 */
 	@Test
 	public void testGetWordsPrefixAsWord() {
 		String word = "cat";
 		Trie trie = new Trie();
 		trie.add(word);
 		SuggestionGenerator suggestionGen = new SuggestionGenerator(true, true, true, 2, trie);
 		assertTrue(suggestionGen.getWordsWrapper("cat").get(0).equals(word));
 	}
 	
 	/**
 	 * Tests against a naive implementation of prefix matching.
 	 */
 	@Test
 	public void testGetWordsNaive() {
 		List<String> words = null;
 		try{
 			words = Main.readFile("/course/cs032/data/autocorrect/dictionary.txt");
 		}
 		catch(IOException e){
 			
 		}
 		Trie trie = new Trie();
 		trie.add(words);
 		
 		List<String> naivePrefixes = naivePrefixMatching("a", words);
 		//Loops through the list of words and finds all words with that prefix.
 		SuggestionGenerator suggestionGen = new SuggestionGenerator(true, true, true, 2, trie);
 		List<String> fastPrefixes = suggestionGen.getWordsWrapper("a");
 		
 		//Compares all words in each.
 		for(String word: naivePrefixes)
 			assertTrue(fastPrefixes.contains(word));
 	}
 	
 	/**
 	 * Generates a list of words given a prefix in linear time.
 	 * @param text Prefix to match to.
 	 * @param words List of words to search through.
 	 * @return ArrayLIst<String> Words with that given prefix.
 	 */
 	public List<String> naivePrefixMatching(String prefix, List<String> words){
 		ArrayList<String> suggs = new ArrayList<String>();
 		for(String word: words){
 			if(word.startsWith(prefix))
 				suggs.add(word);	
 		}
 		return suggs;
 	}
 }
