 package semanticMarkup.ling.learn.dataholder;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import semanticMarkup.ling.learn.Configuration;
 import semanticMarkup.ling.learn.auxiliary.POSInfo;
 import semanticMarkup.ling.learn.auxiliary.StringPair;
 import semanticMarkup.ling.learn.knowledge.Constant;
 import semanticMarkup.ling.learn.utility.StringUtility;
 import semanticMarkup.ling.learn.utility.WordFormUtility;
 
 public class DataHolder {
 	// all unique words in the input treatments
 	public Map<String, Integer> allWords;
 	
 	// words are singular nouns, boundary words, and modifiers
 	private Set<String> BMSWords;
 	
 	// Data holders
 
 	// Table discounted
 	private Map<DiscountedKey, String> discountedTable;
 	public static final byte DISCOUNTED = 2;
 	
 	// Table heuristicnoun
 	private Map<String, String> heuristicNounTable;
 	public static final byte HEURISTICNOUN = 1;
 	
 	// Table isATable
 	private Map<Integer, IsAValue> isATable;
 	public static final byte ISA = 3;
 	
 	// Table modifier
 	private Map<String, ModifierTableValue> modifierTable;
 	public static final byte MODIFIER = 4;
 	
 	// Table sentence
 	private List<SentenceStructure> sentenceTable = new LinkedList<SentenceStructure>();
 	private int sentenceCount;
 	//private Map<Integer, Sentence> sentenceCollection;
 	public static final byte SENTENCE = 5;
 
 	// Table singularPlural
 	private Set<SingularPluralPair> singularPluralTable;
 	public static final byte SINGULAR_PLURAL = 6;
 
 	// Table termCategory
 	private Set<StringPair> termCategoryTable;
 	public static final byte TERM_CATEGORY = 7;
 	
 	// Table unknownword
 	private Map<String, String> unknownWordTable;
 	public static final byte UNKNOWNWORD = 8;
 
 	// Table wordpos
 	private Map<WordPOSKey, WordPOSValue> wordPOSTable;
 	public static final byte WORDPOS = 9;
 	
 	// Table wordrole
 	private Map<StringPair, String> wordRoleTable;
 	public static final byte WORDROLE = 10;
 
 	private Configuration myConfiguration;
 	private WordFormUtility myWordFormUtility;
 	
 	public DataHolder(Configuration myConfiguration, WordFormUtility myWordFormUtility) {
 		this.myConfiguration = myConfiguration;
 		this.myWordFormUtility = myWordFormUtility;
 		
 		this.allWords = new HashMap<String, Integer>();
 		this.BMSWords = new HashSet<String>();
 		
 		this.discountedTable = new HashMap<DiscountedKey, String>();
 		this.heuristicNounTable = new HashMap<String, String>();
 		this.isATable = new HashMap<Integer, IsAValue>();
 		this.modifierTable = new HashMap<String, ModifierTableValue>();
 		
 		this.sentenceTable = new LinkedList<SentenceStructure>();
 		this.sentenceCount = 0;
 		
 		this.singularPluralTable = new HashSet<SingularPluralPair>();
 		this.termCategoryTable = new HashSet<StringPair>();
 		this.unknownWordTable = new HashMap<String, String>();
 		this.wordPOSTable = new HashMap<WordPOSKey, WordPOSValue>();
 		this.wordRoleTable = new HashMap<StringPair, String>();		
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == this) {
 			return true;
 		}
 
 		if (obj == null || obj.getClass() != this.getClass()) {
 			return false;
 		}
 
 		DataHolder myDataHolder = (DataHolder) obj;
 
 		return ((this.discountedTable.equals(myDataHolder.discountedTable))
 				&& (this.heuristicNounTable
 						.equals(myDataHolder.heuristicNounTable))
 				&& (this.modifierTable.equals(myDataHolder.modifierTable))
 				&& (this.sentenceTable.equals(myDataHolder.sentenceTable))
 				&& (this.singularPluralTable
 						.equals(myDataHolder.singularPluralTable))
 				&& (this.unknownWordTable.equals(myDataHolder.unknownWordTable))
 				&& (this.wordPOSTable.equals(myDataHolder.wordPOSTable)) && (this.allWords
 					.equals(myDataHolder.allWords)));
 	}
 	
 	/** Get Holder Utility **/
 	public Map<DiscountedKey, String> getDiscountedHolder(){
 		return this.discountedTable;
 	}
 	
 	public Map<String, ModifierTableValue> getModifierHolder(){
 		return this.modifierTable;
 	}
 	
 	public Map<String, String> getHeuristicNounHolder(){
 		return this.heuristicNounTable;
 	}
 
 	public List<SentenceStructure> getSentenceHolder(){
 		return this.sentenceTable;
 	}
 	
 	public Set<SingularPluralPair> getSingularPluralHolder(){
 		return this.singularPluralTable;
 	}
 	
 	public Set<StringPair> getTermCategoryHolder() {
 		return this.termCategoryTable;
 	}
 
 	public Map<String, String> getUnknownWordHolder(){
 		return this.unknownWordTable;
 	}
 
 	public Map<WordPOSKey, WordPOSValue> getWordPOSHolder(){
 		return this.wordPOSTable;
 	}
 	
 	public Map<StringPair, String> getWordRoleHolder(){
 		return this.wordRoleTable;
 	}
 	
 	/** Add To Utilities **/
 	
 	public void add2Holder(byte holderID, List<String> args){
 
 		if (holderID == DataHolder.DISCOUNTED) {
 			this.discountedTable = this.add2DiscountedHolder(this.discountedTable, args);
 		}
 		
 		if (holderID == DataHolder.ISA) {
 			this.isATable = this.add2IsAHolder(this.isATable, args);
 		}
 		
 		if (holderID == DataHolder.MODIFIER) {
 			this.add2ModifierHolder(args);
 		}
 		
 		if (holderID == DataHolder.SENTENCE) {
 			this.sentenceTable = this.add2SentenceHolder(this.sentenceTable,args);
 		}
 		
 		if (holderID == DataHolder.SINGULAR_PLURAL) {
 			this.singularPluralTable = this.add2SingularPluralHolder(this.singularPluralTable, args);
 		}
 		
 		if (holderID == DataHolder.UNKNOWNWORD) {
 			this.unknownWordTable = this.add2UnknowWordHolder(this.unknownWordTable, args);
 		}
 		
 		if (holderID == DataHolder.WORDPOS) {
 			this.add2WordPOSHolder(args);
 		}
 		
 	}
 	
 	/**
 	 * Add the terms into the heuristicNounTable with the type specified
 	 * 
 	 * @param terms
 	 *            set of terms
 	 * @param type
 	 *            type of the terms
 	 */
 	public int add2HeuristicNounTable(Set<String> terms, String type) {
 		int count = 0;
 
 		Iterator<String> iter = terms.iterator();
 		while (iter.hasNext()) {
 			String term = iter.next();
 			this.getHeuristicNounHolder().put(term, type);
 			count++;
 		}
 
 		return count;
 	}
 
 	public Map<Integer, IsAValue> add2IsAHolder (Map<Integer, IsAValue> isAHolder, List<String> args) {
 		int index = 0;
 		
 		String instance = args.get(index++);
 		String cls = args.get(index++);
 		
 		isAHolder.put(isAHolder.size()+1, new IsAValue(instance, cls));
 		
 		return isAHolder;
 	}
 	
 	public void add2ModifierHolder(List<String> args) {
 		int index = 0;
 		
 		String word = args.get(index++);
 		int count  = new Integer(args.get(index++));
 		boolean isTypeModifier = false;
 		String isTypeModifierString = args.get(index++);
 		if (StringUtils.equals(isTypeModifierString, "true")) {
 			isTypeModifier = true;
 		}
 
 		this.modifierTable.put(word, new ModifierTableValue(count, isTypeModifier));
 	}
 	
 	public void addToModifierHolder(String word, int count, boolean isTypeModifier) {
 		this.modifierTable.put(word, new ModifierTableValue(count, isTypeModifier));
 	}
 
 	public Map<String, String> add2UnknowWordHolder(Map<String, String> unknownWordHolder, List<String> args){
 		int index = 0;
 		
 		String word = args.get(index++);
 		String flag = args.get(index++);
 		unknownWordHolder.put(word, flag);
 		
 		return unknownWordHolder;
 	}
 	
 	public void add2WordPOSHolder(List<String> args){
 		int index = 0;
 		
 		String word = args.get(index++);
 		String POS = args.get(index++);
 		String role = args.get(index++);
 		int certaintyU = new Integer(args.get(index++));
 		int certaintyL = new Integer(args.get(index++));
 		String savedFlag = args.get(index++);
 		String savedID = args.get(index++);
 		this.wordPOSTable.put(
 				new WordPOSKey(word, POS), 
 				new WordPOSValue(role, certaintyU, certaintyL, savedFlag, savedID));
 	}
 	
 	public void addToWordPOSHolder(String word, String POS, String role, int certaintyU, int certaintyL, String savedFlag, String savedID) {
 		this.wordPOSTable.put(
 				new WordPOSKey(word, POS), 
 				new WordPOSValue(role, certaintyU, certaintyL, savedFlag, savedID));
 	}
 	
 	public Set<SingularPluralPair> add2SingularPluralHolder(Set<SingularPluralPair> singularPluralHolder, List<String> args){
 		int index = 0;
 		
 		String singular = args.get(index++);
 		String plural = args.get(index++);
 		singularPluralHolder.add(new SingularPluralPair(singular, plural));
 		
 		return singularPluralHolder; 
 	}
 	
 	public Map<DiscountedKey, String> add2DiscountedHolder(Map<DiscountedKey, String> discountedHolder, List<String> args){
 		int index = 0;
 		
 		String word = args.get(index++);
 		String POS = args.get(index++);
 		String newPOS = args.get(index++);
 		discountedHolder.put(new DiscountedKey(word, POS), newPOS);
 		
 		return discountedHolder; 
 	}
 
 	public List<SentenceStructure> add2SentenceHolder(List<SentenceStructure> sentenceTable,
 			List<String> args) {
 		int index = 0;
 		
 		String source=args.get(index++);
 		String sentence=args.get(index++);
 		String originalSentence=args.get(index++);
 		String lead=args.get(index++);
 		String status=args.get(index++);
 		String tag=args.get(index++);
 		String modifier=args.get(index++);
 		String type=args.get(index++);
 		
 		this.addSentence(source, sentence, originalSentence, lead, status, tag, modifier, type);
 		//sentenceTable.add(new Sentence(source, sentence, originalSentence, lead, status, tag, modifier, type));
 		return sentenceTable;
 
 	}
 	
 	/** Iterator Utility 
 	 * @return 
 	 * @return **/
 	
 	public Iterator<Entry<String, ModifierTableValue>> getModifierHolderIterator() {
 		Iterator<Entry<String, ModifierTableValue>> iter = this.getModifierHolder().entrySet().iterator();
 		
 		return iter;
 	}
 	
 	public Iterator<SentenceStructure> getSentenceHolderIterator(){
 		Iterator<SentenceStructure> iter = this.getSentenceHolder().iterator();
 		
 		return iter;
 	}
 	
 	public Set<String> getSentenceTags() {
 		Set<String> tags = new HashSet<String>();
 		Iterator<SentenceStructure> iter = this.getSentenceHolderIterator();
 		while (iter.hasNext()) {
 			SentenceStructure sentenceItem = iter.next();
 			tags.add(sentenceItem.getTag());
 		}
 		
 		return tags;
 	}
 	
 	public Iterator<Entry<WordPOSKey, WordPOSValue>> getWordPOSHolderIterator(){
 		Iterator<Entry<WordPOSKey, WordPOSValue>> iter = this.wordPOSTable.entrySet().iterator();
 		
 		return iter;
 	} 
 	
 	public boolean updateWordPOS(String word, String POS, String role,
 			int certaintyU, int certaintyL, String savedFlag, String savedID) {
 
 		
 		WordPOSKey key = new WordPOSKey(word, POS);
 		WordPOSValue value = new WordPOSValue(role, certaintyU, certaintyL,
 				savedFlag, savedID);
 		boolean result = this.updateWordPOS(key, value);
 
 
 		return result;
 	}
 	
 	public boolean updateWordPOS(WordPOSKey key, WordPOSValue value) {
     	PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.updateWordPOS");
 		
 		boolean result = true;
 		
 //		if (key.getWord().equals("shoulder")) {
 //			System.out.println();
 //		}
 		
 		if (this.wordPOSTable.containsKey(key)) {
 			if (this.wordPOSTable.get(key).equals(value)) {
 				result = false;
 				myLogger.trace(String.format(
 						"Updated [%s, %s] in WordPOS holder: No update",
 						key.toString(), value.toString()));
 			}
 			else {
 				this.wordPOSTable.put(key, value);
 				myLogger.trace(String.format(
 						"Updated [%s, %s] in WordPOS holder: Updated",
 						key.toString(), value.toString()));
 			}
 		}
 		else {
 			this.wordPOSTable.put(key, value);
 			myLogger.trace(String.format(
 					"Updated [%s, %s] in WordPOS holder: Added New",
 					key.toString(), value.toString()));
 		}
 		
 		return result;
 	}
 	
 	public boolean removeWordPOS(WordPOSKey key) {
     	PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.updateWordPOS");
 		
 		boolean result = false;
 		
 		if (this.wordPOSTable.containsKey(key)) {
 			WordPOSValue oldValue = this.wordPOSTable.remove(key);
 			myLogger.trace(String.format(
 					"Updated [%s, %s] in WordPOS holder: Added New",
 					key.toString(), oldValue.toString()));
 			result = true;
 		}
 		else {
 			result = false;
 		}
 		
 		return result;
 	}
 	
 	public Iterator<Entry<String, String>> getUnknownWordHolderIterator(){
 		return this.unknownWordTable.entrySet().iterator();
 	}
 	
 	/** Output Utility **/
 	public void printHolder(byte holderID) {
 		if (holderID == DataHolder.SENTENCE) {
 			printHolder(holderID, 0, this.sentenceTable.size()-1);
 		}
 		
 		if (holderID == DataHolder.SINGULAR_PLURAL) {
 			printHolder(holderID, 0, this.singularPluralTable.size()-1);
 		}
 		
 		if (holderID == DataHolder.UNKNOWNWORD) {
 			printHolder(holderID, 0, this.unknownWordTable.size()-1);
 		}
 		
 		if (holderID == DataHolder.WORDPOS) {
 			printHolder(holderID, 0, this.wordPOSTable.size()-1);
 		}
 		
 	}
 	
 	public void printHolder(byte holderID, int startIndex, int endIndex){
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.printHolder");
 		
 		if (holderID == DataHolder.SENTENCE) {
 			for (int i = startIndex; i<=endIndex; i++) {
 				SentenceStructure sentence = this.sentenceTable.get(i);
 				myLogger.info("Index: "+i);
 				myLogger.info(sentence.toString());
 //				myLogger.info("Sentence ID: "+sentence.getID());
 //				myLogger.info("Source: "+sentence.getSource());
 //				myLogger.info("Sentence: "+sentence.getSentence());
 //				myLogger.info("Original Sentence: "+sentence.getSentence());
 //				myLogger.info("Lead: "+sentence.getLead());
 //				myLogger.info("Status: "+sentence.getStatus());
 //				myLogger.info("Tag: "+sentence.getTag());
 //				myLogger.info("Modifier: "+sentence.getModifier());
 //				myLogger.info("Type: "+sentence.getType());
 //				myLogger.info("\n");
 			}
 		}
 		
 		if (holderID == DataHolder.SINGULAR_PLURAL) {
 			myLogger.info("==SingularPlural Table==");
 			
 //			Iterator<SingularPluralPair> iter = this.singularPluralTable.iterator();
 			
 			List<SingularPluralPair> singularPluralPairList = new LinkedList<SingularPluralPair>();
 			singularPluralPairList.addAll(singularPluralTable);
 			Collections.sort(singularPluralPairList);
 			
 			for (int i = 0; i<singularPluralPairList.size();i++) {
 				if ((i >= startIndex) && (i <=endIndex)) {
 					SingularPluralPair entry = singularPluralPairList.get(i);
 					
 					myLogger.info("Index: " + i);
 					myLogger.info("Singular: " + entry.getSingular());
 					myLogger.info("Plural: " + entry.getPlural());
 					myLogger.info("\n");
 				}
 			}
 			
 //			int index = 0;
 //			while (iter.hasNext()) {
 //				if ((index >= startIndex) && (index <=endIndex)) {
 //					SingularPluralPair entry = iter.next();
 //					
 //					myLogger.info("Index: " + index);
 //					myLogger.info("Singular: " + entry.getSingular());
 //					myLogger.info("Plural: " + entry.getPlural());
 //					myLogger.info("\n");
 //				}
 //				index++;
 //			}
 		}
 		
 		if (holderID == DataHolder.UNKNOWNWORD) {
 			int index = 0;
 			Iterator<Entry<String, String>> iter = this.unknownWordTable.entrySet().iterator();
 			
 			while (iter.hasNext()) {
 				if ((index >= startIndex) && (index <= endIndex)) {
 					Entry<String, String> entry = iter.next();
 					
 					myLogger.info("Index: " + index);
 					myLogger.info("Key: " + entry.getKey());
 					myLogger.info("Value: " + entry.getValue());
 					myLogger.info("\n");
 				}
 				index++;
 			}
 		}
 		
 		if (holderID == DataHolder.WORDPOS) {
 			int index = 0;
 			Iterator<Entry<WordPOSKey, WordPOSValue>> iter = this.getWordPOSHolderIterator();			
 			while (iter.hasNext()) {				
 				if ((index >= startIndex) && (index <= endIndex)) {
 					Entry<WordPOSKey, WordPOSValue> entry = iter.next();
 					
 					myLogger.info(entry.toString());
 					myLogger.info("\n");
 
 				}
 				index++;
 			}
 			
 		}
 		
 		myLogger.info("Total: "+(endIndex-startIndex+1)+"\n");
 	}
 
 	/** Class Methods**/
 	public SentenceStructure getSentence(int ID) {
 		
 		Iterator<SentenceStructure> iter = this.sentenceTable.iterator();
 		
 		while(iter.hasNext()) {
 			SentenceStructure sentence = iter.next();
 			if (sentence.getID()==ID) {
 				return sentence;
 			}
 		}
 		
 		return null;
 	}
 	
 	
 	/********************************************/
 	/********************************************/
 	/********************************************/
 	
 	/** Unsupervised Learning Methods**/
 	
 	/**
 	 * 
 	 * @param word
 	 * @param flag
 	 */
 	public void updateUnknownWord(String word, String flag){
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.updateUnkownWord");	
 		this.unknownWordTable.put(word, flag);
 		myLogger.trace(String.format("Added (%s, %s) into UnknownWord holder", word, flag));
 	}
 	
 	public void addSentence(String source, String sentence,
 			String originalSentence, String lead, String status, String tag,
 			String modifier, String type) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.addSentence");	
 		
 		SentenceStructure newSent = new SentenceStructure(this.sentenceCount, source, sentence, originalSentence, lead,
 				status, tag, modifier, type);
 		this.sentenceCount++;
 		this.sentenceTable.add(newSent);
 		myLogger.trace("Added Sentence: ");
 		myLogger.trace("\tSource: " + source);
 		myLogger.trace("\tSentence: " + sentence);
 		myLogger.trace("\tOriginal Sentence: " + originalSentence);
 		myLogger.trace("\tLead: " + lead);
 		myLogger.trace("\tStatus: " + status);
 		myLogger.trace("\tTag: " + tag);
 		myLogger.trace("\tModifier: " + modifier);
 		myLogger.trace("\tType: " + type);
 		
 		myLogger.trace("Quite\n");
 	}
 
     /**
      * 
      * @param word
      * @return
      */
     public List<Entry<WordPOSKey,WordPOSValue>> getWordPOSEntries(String word) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.getWordPOSEntries");
         
 		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter = this.getWordPOSHolderIterator();
 		List<Entry<WordPOSKey, WordPOSValue>> result = new ArrayList<Entry<WordPOSKey, WordPOSValue>>();
 		
 		while (iter.hasNext()) {
 			Map.Entry<WordPOSKey, WordPOSValue> wordPOSEntry = iter.next();
 			if (StringUtils.equals(wordPOSEntry.getKey().getWord(), word)) {
 				result.add(wordPOSEntry);
 			}
 		}
 		
 		myLogger.trace("Get WordPOS Entries of word: " + word);
 		myLogger.trace(StringUtils.join(result, ",\n"));		
 		
 		return result;
     }
     
     public List<Entry<WordPOSKey,WordPOSValue>> getWordPOSEntriesByWordPOS(String word, Set<String> POSs) {
 		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter = this.getWordPOSHolderIterator();
 		List<Entry<WordPOSKey, WordPOSValue>> result = new ArrayList<Entry<WordPOSKey, WordPOSValue>>();
 		
 		while (iter.hasNext()) {
 			Map.Entry<WordPOSKey, WordPOSValue> wordPOSEntry = iter.next();
 			if (StringUtils.equals(wordPOSEntry.getKey().getWord(), word)
 					&& POSs.contains(wordPOSEntry.getValue())) {
 				result.add(wordPOSEntry);
 			}
 		}		
 		
 		return result;
     }
 
 
 	/**
 	 * check if the word is in the singularPluralTable.
 	 * 
 	 * @param word
 	 *            the word to check
 	 * @return true if the word is in the SingularPluralTable; false otherwise.
 	 */
 	public boolean isInSingularPluralPair(String word) {
 		Iterator<SingularPluralPair> iter = this.singularPluralTable.iterator();
 
 		while (iter.hasNext()) {
 			SingularPluralPair spp = iter.next();
 			if ((spp.getSingular().equals(word))
 					|| (spp.getPlural().equals(word))) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Get words with specified POS tags from word-POS holder
 	 * 
 	 * @param POSTags
 	 *            the POS tags of the words searching for
 	 * @return set of words
 	 */
 	public Set<String> getWordsFromWordPOSByPOSs(Set<String> POSTags) {
 		Set<String> words = new HashSet<String>();
 
 		if (POSTags == null) {
 			return words;
 		}
 
 		Iterator<Entry<WordPOSKey, WordPOSValue>> iter = this
 				.getWordPOSHolderIterator();
 
 		while (iter.hasNext()) {
 			Entry<WordPOSKey, WordPOSValue> wordPOSEntry = iter.next();
 			String POS = wordPOSEntry.getKey().getPOS();
 			if (POSTags.contains(POS)) {
 				String word = wordPOSEntry.getKey().getWord();
 				words.add(word);
 			}
 		}
 
 		return words;
 	}
 	
 	/**
 	 * Get words from UnknowWord holder
 	 * 
 	 * @param wordPattern
 	 *            pattern the word must match
 	 * @param isWordPatternChecked
 	 *            if the word pattern is used
 	 * @param flagPattern
 	 *            pattern the flag must match
 	 * @param isFlagPatternChecked
 	 *            if the flag pattern is used
 	 * @return set of words
 	 */
 	public Set<String> getWordsFromUnknownWord(String wordPattern, boolean isWordPatternChecked, 
 			String flagPattern, boolean isFlagPatternChecked) {
 		Set<String> words = new HashSet<String>();
 
 		if ((!isWordPatternChecked) && (!isFlagPatternChecked)) {
 			return words;
 		}
 
 		Iterator<Entry<String, String>> iter = this
 				.getUnknownWordHolderIterator();
 		while (iter.hasNext()) {
 			Entry<String, String> item = iter.next();
 
 			String word = item.getKey();
 			String flag = item.getValue();
 			boolean case1 = getWordsFromUnknownWordByPatternsHelper(
 					wordPattern, isWordPatternChecked, word);
 			boolean case2 = getWordsFromUnknownWordByPatternsHelper(
 					flagPattern, isFlagPatternChecked, flag);
 
 			if (case1 && case2) {
 				words.add(word);
 			}
 		}
 
 		return words;
 	}
 
 	private boolean getWordsFromUnknownWordByPatternsHelper(String pattern,
 			boolean isPatternChecked, String text) {
 		boolean result = false;
 
 		if (!isPatternChecked) {
 			result = true;
 		}
 		else {
 			if (pattern != null) {
 //				if (StringUtility.isMatchedNullSafe(pattern, text)) {
 				if (StringUtility.isMatchedNullSafe(text, pattern)) {
 					result = true;
 				}
 			}
 		}
 
 		return result;
 	}
 	
 	
 	public boolean isWordExistInUnknownWord(String wordPattern,
 			boolean isWordPatternChecked, String flagPattern,
 			boolean isFlagPatternChecked) {
 		boolean isWordExist = false;
 		if ((!isWordPatternChecked) && (!isFlagPatternChecked)) {
 			isWordExist = false;
 			return isWordExist;
 		}
 
 		Iterator<Entry<String, String>> iter = this
 				.getUnknownWordHolderIterator();
 		while (iter.hasNext()) {
 			Entry<String, String> item = iter.next();
 
 			String word = item.getKey();
 			String flag = item.getValue();
 			boolean case1 = getWordsFromUnknownWordByPatternsHelper(
 					wordPattern, isWordPatternChecked, word);
 			boolean case2 = getWordsFromUnknownWordByPatternsHelper(
 					flagPattern, isFlagPatternChecked, flag);
 
 			if (case1 && case2) {
 				isWordExist = true;
 				return isWordExist;
 			}
 		}
 		
 		return isWordExist;
 	}
 
 	/**
 	 * Check if any sentence matches given pattern exists in the data holder
 	 * 
 	 * @param isTagged
 	 *            if the sentence has to be tagged or not
 	 * @param pattern
 	 *            pattern to match against
 	 * @return true if any sentence matches the given pattern exists; false
 	 *         otherwise
 	 */
 	public boolean isExistSentence(boolean isTagged, String pattern) {
 		boolean isExist = false;
 		
 		Iterator<SentenceStructure> iter = getSentenceHolderIterator();
 		while (iter.hasNext()) {
 			SentenceStructure sentenceItem = iter.next();
 			String tag = sentenceItem.getTag();
 			boolean isTagGood = false;
 			if (isTagged) {
 				if ((!StringUtils.equals(tag, "ignore")) || (tag == null)) {
 					isTagGood = true;
 				}
 			} else {
 				isTagGood = true;
 			}
 			if (isTagGood) {
 				String sentence = sentenceItem.getSentence();
 				if (StringUtility.isMatchedNullSafe(sentence, pattern)) {
 					isExist = true;
 					return isExist;
 				}
 			}
 		}
 		
 		return isExist;
 	}
 	
 	/**
 	 * Get all sentences match a given pattern from the data holder
 	 * 
 	 * @param dataholderHandler
 	 *            handler of dataholder
 	 * @param pattern
 	 *            pattern to match against
 	 * @return sentences matche the given pattern exists; false
 	 *         otherwise
 	 */
 	public Set<SentenceStructure> getTaggedSentenceByPattern(String pattern) {
 		Set<SentenceStructure> sentences = new HashSet<SentenceStructure>();
 		
 		Iterator<SentenceStructure> iter = getSentenceHolderIterator();
 		while (iter.hasNext()) {
 			SentenceStructure sentenceItem = iter.next();
 			String tag = sentenceItem.getTag();
 			if ((!StringUtils.equals(tag, "ignore"))||(tag == null)) {
 				String sentence = sentenceItem.getSentence();
 				if (StringUtility.isMatchedNullSafe(sentence, pattern)) {
 					sentences.add(sentenceItem);
 				}
 			}
 		}
 		
 		return sentences;
 	}
 
 	/**
 	 * Delete any wordPOS entries in WordPOS collection that meets the
 	 * requirements
 	 * 
 	 * @param isWordChecked
 	 *            if the word is checked
 	 * @param word
 	 *            the word to check
 	 * @param isPOSChecked
 	 *            if the POS tag is checked
 	 * @param POS
 	 *            the POS to check
 	 * @return true if any deletion has been made, false otherwise
 	 */
 	public boolean deleteWordPOS(boolean isWordChecked, String word,
 			boolean isPOSChecked, String POS) {
 		boolean isDeleted = false;
 		int numDeleted = 0;
 
 		if ((!isWordChecked) && (!isPOSChecked)) {
 			isDeleted = true;
 		} else {
 			Iterator<Entry<WordPOSKey, WordPOSValue>> iter = this
 					.getWordPOSHolderIterator();
 			while (iter.hasNext()) {
 				Entry<WordPOSKey, WordPOSValue> wordPOS = iter.next();
 				boolean isWordPass = false;
 				boolean isPOSPass = false;
 
 				if (isWordChecked) {
 					if (StringUtils.equals(word, wordPOS.getKey().getWord())) {
 						isWordPass = true;
 					}
 				} else {
 					isWordPass = true;
 				}
 
 				if (isPOSPass) {
 					if (StringUtils.equals(POS, wordPOS.getKey().getPOS())) {
 						isPOSPass = true;
 					}
 				} else {
 					isPOSPass = true;
 				}
 
 				if (isWordPass && isPOSPass) {
 					numDeleted++;
 				}
 			}
 
 			if (numDeleted > 0) {
 				isDeleted = true;
 			}
 		}
 
 		return isDeleted;
 	}
 	
 	public boolean updateSentenceTag(String tagPattern, String newTag){
 		boolean isTagged = false;
 		
 		Iterator<SentenceStructure> iter = this.getSentenceHolderIterator();	
 
 		while (iter.hasNext()) {
 			SentenceStructure sentenceItem = iter.next();
 			String tag = sentenceItem.getTag();
 			if (updateSentenceTagHelper(tag, tagPattern)) {
 				sentenceItem.setTag(newTag);
 				isTagged = true;
 			}
 		}
 
 		return isTagged;
 	}
 	
 	private boolean updateSentenceTagHelper (String tag, String tagPattern) {
 		if (tag == null && tagPattern == null) {
 			return true;
 		}
 		
 		return StringUtility.isMatchedNullSafe(tag, tagPattern);
 	}
 	
 	/**
 	 * get all sentences which match the pattern passed in
 	 * 
 	 * @param tagPattern
 	 *            pattern of tag of the sentences searching for
 	 * @return list of sentences
 	 */
 	public List<SentenceStructure> getSentencesByTagPattern(String tagPattern) {
 		List<SentenceStructure> sentences = new LinkedList<SentenceStructure>();
 		Iterator<SentenceStructure> iter = this.getSentenceHolderIterator();
 		while (iter.hasNext()) {
 			SentenceStructure sentenceItem = iter.next();
 			String tag = sentenceItem.getTag();
 			if (StringUtility.isMatchedNullSafe(tag, tagPattern)) {
 				sentences.add(sentenceItem);
 			}
 		}
 		
 		return sentences;
 	}
 	
 	public int getSentenceCount(boolean isModifierUsed, String mPattern,
 			boolean isTagUsed, String tPattern) {
 		int count = 0;
 		for (SentenceStructure sentenceItem : this.sentenceTable) {
 			boolean c1 = true;
 			if (isModifierUsed) {
 				c1 = StringUtility.isMatchedNullSafe(
 						sentenceItem.getModifier(), mPattern);
 			}
 
 			boolean c2 = true;
 			if (isTagUsed) {
 				c2 = StringUtility.isMatchedNullSafe(sentenceItem.getTag(),
 						tPattern);
 			}
 
 			if (c1 && c2) {
 				count++;
 			}
 		}
 
 		return count;
 	}
 	
 	
 	/**
 	 * add the singular form and the plural form of a word into the
 	 * singularPluarlTable
 	 * 
 	 * @param sgl
 	 *            singular form
 	 * @param pl
 	 *            plural form
 	 * @return if add a pair, return true; otherwise return false
 	 */
 	public boolean addSingularPluralPair(String sgl, String pl) {
 		
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.addsingularpluralpair");				
 		
 		SingularPluralPair pair = new SingularPluralPair(sgl, pl);
 		boolean result = this.singularPluralTable.add(pair);
 		
 		myLogger.debug(String.format("Added singular-plural pair (%s, %s)", sgl, pl));
 		return result;
 	}
 	
 	
 	/** Unknown Word Table Utility***********************************/
 	
 	/**
 	 * 
 	 * @param word
 	 * @param tag
 	 */
 	public void addUnknown(String word, String tag) {
 		this.unknownWordTable.put(word, tag);
 	}
 	
 	
 	/** Modifier Table Utility***************************************/
 	
 	/**
 	 * Take a new word, insert it into the modifier holder, or update its count in
 	 * modifier holder if it already exists
 	 * 
 	 * @param newWord
 	 * @param increment
 	 * @return if anything changed in modifier holder, return true; otherwise
 	 *         return false
 	 */
 	public int addModifier(String newWord, int increment) {
 		int isUpdate = 0;
 
 		if ((newWord.matches("(" + Constant.STOP + "|^.*\\w+ly$)"))
 				|| (!(newWord.matches("^.*\\w.*$")))) {
 			return isUpdate;
 		}
 
 		if (this.modifierTable.containsKey(newWord)) {
 			int count = this.modifierTable.get(newWord).getCount();
 			count = count + increment;
 			this.modifierTable.get(newWord).setCount(count);
 			// isUpdate = 1;
 		} else {
 			this.modifierTable.put(newWord, new ModifierTableValue(1, false));
 			isUpdate = 1;
 		}
 
 		return isUpdate;
 	}
 
 	/**
 	 * Pick one from bPOS and otherPOS and return it
 	 * 
 	 * @param newWord
 	 * @param bPOS
 	 * @param otherPOS
 	 * @return if the newWord appears after a plural noun in any untagged
 	 *         sentence, return the bPOS; otherwise, return the otherPOS
 	 */
 	public String resolveConflict(String newWord, String bPOS, String otherPOS) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder.resolveConflict");
 		
 		myLogger.trace("Enter resolveConflict");
 
 		int count = 0;
 		List<SentenceStructure> mySentenceHolder = this.getSentenceHolder();
 		for (int i = 0; i < mySentenceHolder.size(); i++) {
 			SentenceStructure sentence = mySentenceHolder.get(i);
 			boolean flag = false;
 			flag = sentence.getTag() == null ? 
 					true : (!sentence.getTag().equals("ignore"));
 			if (flag) {
 				String regex = "^.*?([a-z]+(" + Constant.PLENDINGS + ")) ("
 						+ newWord + ").*$";
 				Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
 				String originalSentence = sentence.getOriginalSentence();
 				Matcher m = p.matcher(originalSentence);
 				if (m.find()) {
 					String plural = m.group(1).toLowerCase();
 					if (this.myWordFormUtility.getNumber(plural)
 							.equals("p")) {
 						count++;
 					}
 					if (count >= 1) {
 						myLogger.trace("Quite resolveConflict, return: " + bPOS);
 						return bPOS;
 					}
 				}
 			}
 		}
 		
 		myLogger.trace("Quite resolveConflict, return: "+otherPOS);
 		return otherPOS;
 	}
 	
 	/**
 	 * Discount existing pos, but do not establish suggested pos
 	 * 
 	 * @param newWord
 	 * @param oldPOS
 	 * @param newPOS
 	 * @param mode
 	 *            "byone" - reduce certainty 1 by 1. "all" - remove this POS
 	 */
 	public void discountPOS(String newWord, String oldPOS, String newPOS,
 			String mode) {
 		/**
 		 * 1. Find the flag of newWord in unknownWords table
 		 * 1. Select all words from unknownWords table who has the same flag (including newWord)
 		 * 1. From wordPOS table, select certaintyU of the (word, oldPOS) where word is in the words list
 		 *     For each of them
 		 *     1.1 Case 1: certaintyu less than 1, AND mode is "all"
 		 *         1.1.1 Delete the entry from wordpos table
 		 *         1.1.1 Update unknownwords
 		 *             1.1.1.1 Case 1: the pos is "s" or "p"
 		 *                 Delete all entries contains word from singularplural table as well
 		 *         1.1.1 Insert (word, oldpos, newpos) into discounted table
 		 */
 		
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder.discountPOS");
 		
 		myLogger.trace("Enter discountPOS");
 		
 		// get the flag of the newWord
 		String flag = this.unknownWordTable.get(newWord);
 
 		// get the word list
 		List<String> wordList = new ArrayList<String>();
 		Iterator<Map.Entry<String, String>> unknownWordIter = this.unknownWordTable.entrySet().iterator();
 		while (unknownWordIter.hasNext()) {
 			Map.Entry<String, String> e = unknownWordIter.next();
 			if (e.getValue().equals(flag)) {
 				wordList.add(e.getKey());
 			}
 		}
 		myLogger.debug(wordList.toString());
 		
 		//wordList.add(newWord);
 		
 		for (int i=0;i<wordList.size();i++) {
 			String word = wordList.get(i);
 			WordPOSKey key = new WordPOSKey(word, oldPOS);
 			if (this.wordPOSTable.containsKey(key)) {
 				WordPOSValue value = this.wordPOSTable.get(key);
 				int cU = value.getCertaintyU();
 				if (cU <= 1 && mode.equals("all")) {
 					this.removeWordPOS(key);
 					this.updateUnknownWord(word, "unknown");
 					// delete from SingularPluralHolder
 					if (oldPOS.matches("^.*[sp].*$")) {
 						// list of entries to be deleted
 						ArrayList<SingularPluralPair> delList = new ArrayList<SingularPluralPair>();
 
 						// find entries to be deleted, put them into delList
 						Iterator<SingularPluralPair> iterSPTable = this.singularPluralTable.iterator();
 						while (iterSPTable.hasNext()) {
 							SingularPluralPair spp = iterSPTable.next();
 							if (spp.getSingular().equals(word)
 									|| spp.getPlural().equals(word)) {
 								delList.add(spp);
 							}
 						}
 
 						// delete all entries in delList from singularPluralTable
 						Iterator<SingularPluralPair> delListIter = delList.iterator();
 						while (delListIter.hasNext()) {
 							SingularPluralPair del = delListIter.next();
 							this.singularPluralTable.remove(del);
 						}
 					}
 					
 					DiscountedKey dKey = new DiscountedKey(word, oldPOS);
 					this.discountedTable.put(dKey, newPOS);
 				}
 				else {
 					WordPOSValue temp = this.wordPOSTable.get(key);
 					int certaintyU = temp.getCertaintyU();
 					temp.setCertiantyU(certaintyU-1);
 					this.updateWordPOS(key, temp);
 				}
 			}
 		}
 		
 		myLogger.trace("Quite discountPOS");
 	}
 	
 	
 	/**
 	 * Given a new role, and the old role, of a word, decide the right role to
 	 * return
 	 * 
 	 * @param oldRole
 	 * @param newRole
 	 * @return oldRole or newRole, whichever wins
 	 */
 	public String mergeRole(String oldRole, String newRole) {
 
 		// if old role is "*", return the new role
 		if (oldRole.equals("*")) {
 			return newRole;
 		}
 		// if the new role is "*", return the old rule
 		else if (newRole.equals("*")) {
 			return oldRole;
 		}
 
 		// if the old role is empty, return the new role
 		if (oldRole.equals("")) {
 			return newRole;
 		}
 		// if the new role is empty, return the old role
 		else if (newRole.equals("")) {
 			return oldRole;
 		}
 		// if the old role is not same as the new role, return "+"
 		else if (!oldRole.equals(newRole)) {
 			return "+";
 		}
 		// if none of above apply, return the old role by default
 		else {
 			return oldRole;
 		}
 	}
 	
 	/**
 	 * Find the tag of the sentence of which this sentid (clause) is a part of
 	 * 
 	 * @param sentID
 	 * @return a tag
 	 */
 	public String getParentSentenceTag(int sentID) {
 		/**
 		 * 1. Get the originalsent of sentence with sentID 
 		 * 1. Case 1: the originalsent of $sentence sentID starts with a [a-z\d] 
 		 * 1.1 select modifier and tag from Sentence where tag is not "ignore" OR tag is null 
 		 *      AND originalsent COLLATE utf8_bin regexp '^[A-Z].*' OR originalsent rlike ': *\$' AND id < sentID 
 		 * 1.1 take the tag of the first sentence (with smallest id), get its modifier and tag 
 		 * 1.1 if modifier matches \w, tag = modifier + space + tag 
 		 * 1.1 remove [ and ] from tag 
 		 * 1. if tag matches \w return [+tag+], else return [parenttag]
 		 */
 
 		String originalSentence = this.sentenceTable.get(sentID)
 				.getOriginalSentence();
 		String tag = "";
 		String oSentence = "";
 		if (originalSentence.matches("^\\s*[^A-Z].*$")) {
 		//if (originalSent.matches("^\\s*([a-z]|\\d).*$")) {
 			for (int i = 0; i < sentID; i++) {
 				SentenceStructure sentence = this.sentenceTable.get(i);
 				tag = sentence.getTag();
 				oSentence = sentence.getOriginalSentence();
 				boolean flag = (tag == null)? true : (!tag.matches("ignore"));
 
 				if (flag && ((oSentence.matches("^[A-Z].*$")) || (oSentence
 								.matches("^.*:\\s*$")))) {
 					String modifier = sentence.getModifier();
 					if (modifier.matches("^.*\\w.*$")) {
 						if (tag == null) {
 							tag = "";
 						}
 						tag = modifier + " " + tag;
 						tag = tag.replaceAll("[\\[\\]]", "");
 					}
 					break;
 				}
 			}
 		}
 
 		return tag.matches("^.*\\w.*$") ? "[" + tag + "]" : "[parenttag]" ;
 	}
 	
 	/**
 	 * Get modifier and tag from the parent tag
 	 * 
 	 * @param tag
 	 * @return a list with two elements. The first element is modifier. The
 	 *         second element is tag
 	 */
 	public List<String> getMTFromParentTag(String tag) {
 		String modifier = "";
 		String newTag = "";
 
 		Pattern p = Pattern.compile("^\\[(\\w+)\\s+(\\w+)\\]$");
 		Matcher m = p.matcher(tag);
 		if (m.lookingAt()) {
 			modifier = m.group(1);
 			newTag = m.group(2);
 		} else {
 			p = Pattern.compile("^(\\w+)\\s+(\\w+)$");
 			m = p.matcher(tag);
 			if (m.lookingAt()) {
 				modifier = m.group(1);
 				newTag = m.group(2);
 			}
 
 		}
 		List<String> pair = new ArrayList<String>();
 		pair.add(modifier);
 		pair.add(newTag);
 
 		return pair;
 	}
 	
 	/**
 	 * Remove ly ending word which is a "b" in the WordPOS, from the modifier
 	 * 
 	 * @param modifier
 	 * @return the new modifer
 	 */
 	public String tagSentWithMTRemoveLyEndingBoundary(String modifier) {
 		if (modifier == null) {
 			return null;
 		}
 		
 		Pattern p = Pattern.compile("^(\\w+ly)\\s*(.*)$");
 		Matcher m = p.matcher(modifier);
 		while (m.lookingAt()) {
 			String wordly = m.group(1);
 			String rest = m.group(2);
 			WordPOSKey wp = new WordPOSKey(wordly, "b");
 			if (this.wordPOSTable.containsKey(wp)) {
 				modifier = rest;
 				m = p.matcher(modifier);
 			} else {
 				break;
 			}
 		}
 		
 		return modifier;
 	}
 		
 	/**
 	 * 
 	 * @param word
 	 * @param pos
 	 * @param role
 	 * @param table
 	 * @param increment
 	 * @return
 	 */
 	public int updateDataHolder(String word, String pos, String role, String table,
 			int increment) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder");
 		myLogger.trace(String.format("Enter (%s, %s, %s, %s, %d)", word, pos, role, table, increment));
 		
 		int result = 0;
 
 		word = StringUtility.processWord(word);
 		// empty word
 		if (word.length() < 1) {
 			return 0;
 		}
 
 		// forbidden word
 		if (word.matches("\\b(?:" + Constant.FORBIDDEN + ")\\b")) {
 			return 0;
 		}
 
 		// if it is a n word, check if it is singular or plural, and update the
 		// pos
 		if (pos.equals("n")) {
 			pos = this.myWordFormUtility.getNumber(word);
 		}
 
 		result = result + markKnown(word, pos, role, table, increment);
 		myLogger.trace("result1: " + result);
 		// 1) if the word is a singular form n word, find its plural form, then add
 		// the plural form, and add the singular - pluarl pair into
 		// singularPluarlTable;
 		// 2) if the word is a plural form n word, find its singular form, then add
 		// the singular form, and add the singular - pluarl pair into
 		// singularPluarlTable;
 		if (!this.isInSingularPluralPair(word)) {
 			myLogger.trace("Search for singular-plural pair of word: " + word);
 			
 			if (pos.equals("p")) {
 				myLogger.trace("Case 1");
 				String pl = word;
 				word = this.myWordFormUtility.getSingular(word);
 				myLogger.trace(String.format("Get singular form of %s: %s", pl,
 						word));
 
 				// add "*" and 0: pos for those words are inferred based on
 				// other clues, not seen directly from the text
 				result = result + this.markKnown(word, "s", "*", table, 0);
 				myLogger.trace("result2: " + result);
 				this.addSingularPluralPair(word, pl);
 				myLogger.trace(String.format("Added (%s, %s)", word, pl));
 
 			}
 			else if (pos.equals("s")) {
 				myLogger.trace("Case 2");
 				List<String> words = this.myWordFormUtility.getPlural(word);
 				String sg = word;
 //				if (sg.equals("centrum")) {
 //					System.out.println("Return Size: "+words.size());
 //				}
 				for (int i = 0; i < words.size(); i++) {
 					if (words.get(i).matches("^.*\\w.*$")) {
 						result = result
 								+ this.markKnown(words.get(i), "p", "*", table,
 										0);
 						myLogger.trace("result3: " + result);
 					}
 					this.addSingularPluralPair(sg, words.get(i));
 					myLogger.trace(String.format("Added (%s, %s)", sg, words.get(i)));
 				}
 			}
 			else {
 				myLogger.trace("Nothing added");
 			}
 		}
 
 		myLogger.trace("Return: "+result+"\n");
 		return result;
 	}
 
 	/**
 	 * mark a word with its pos and role in wordpos holder, or ???
 	 * 
 	 * @param word
 	 *            the word to mark
 	 * @param pos
 	 *            the pos of the word
 	 * @param role
 	 *            the role of the word
 	 * @param table
 	 *            which table to mark
 	 * @param increment
 	 * @return
 	 */
 	public int markKnown(String word, String pos, String role, String table,
 			int increment) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder.markKnown");		
 		myLogger.trace("Enter markKnown");
 		
 		String pattern = "";
 		int sign = 0;
 		String otherPrefix = "";
 		String spWords = "";
 
 		// forbidden word
 		if (word.matches("\\b(?:" + Constant.FORBIDDEN + ")\\b")) {
 			return 0;
 		}
 
 		// stop words
 		if (word.matches("^(" + Constant.STOP + ")$")) {
 			sign = sign
 					+ processNewWord(word, pos, role, table, word, increment);
 			return sign;
 		}
 
 		// process this new word
 		sign = sign + processNewWord(word, pos, role, table, word, increment);
 		
 		// Case 1: we try to learn those new words based on this one
 		Pattern p = Pattern.compile("^(" + Constant.PREFIX + ")(\\S+).*$");
 		Matcher m = p.matcher(word);
 		if (m.lookingAt()) {
 			myLogger.trace("Case 1");
 			String g1 = m.group(1); // the prefix
 			String g2 = m.group(2); // the remaining
 
 			otherPrefix = StringUtility.removeFromWordList(g1, Constant.PREFIX);
 
 			spWords = "("
 					+ StringUtility.escape(this.singularPluralVariations(g2,
 							this.getSingularPluralHolder())) + ")";
 			pattern = "^(" + otherPrefix + ")?" + spWords + "$";
 
 			Iterator<Map.Entry<String, String>> iter1 = this.getUnknownWordHolder()
 					.entrySet().iterator();
 
 			
 			while (iter1.hasNext()) {
 				Map.Entry<String, String> entry = iter1.next();
 				String newWord = entry.getKey();
 				String flag = entry.getValue();
 
 				if ((newWord.matches(pattern)) && (flag.equals("unknown"))) {
 					sign = sign
 							+ processNewWord(newWord, pos, "*", table, word, 0);
 					
 					myLogger.trace("Case 1.1");
 					myLogger.trace("by removing prefix of " + word + ", know "
 							+ newWord + " is a [" + pos + "]");
 				}
 			}
 		}
 
 		// Case 2: word starts with a lower case letter
 		if (word.matches("^[a-z].*$")) {
 			myLogger.trace("Case 2");
 			spWords = "("
 					+ StringUtility.escape(this.singularPluralVariations(word,
 							this.getSingularPluralHolder())) + ")";
 			// word=shrubs, pattern = (pre|sub)shrubs
 			pattern = "^(" + Constant.PREFIX + ")" + spWords + "$";
 
 			Iterator<Map.Entry<String, String>> iter2 = this.getUnknownWordHolder()
 					.entrySet().iterator();
 
 			while (iter2.hasNext()) {
 				Map.Entry<String, String> entry = iter2.next();
 				String newWord = entry.getKey();
 
 				String flag = entry.getValue();
 				// case 2.1
 				if ((newWord.matches(pattern)) && (flag.equals("unknown"))) {
 					sign = sign
 							+ processNewWord(newWord, pos, "*", table, word, 0);
 					
 					myLogger.debug("Case 2.1");
 					myLogger.debug("by adding a prefix to " + word
 							+ ", know " + newWord + " is a [" + pos + "]");
 				
 				}
 			}
 			
 			spWords = "("
 					+ StringUtility.escape(this.singularPluralVariations(word,
 							this.getSingularPluralHolder())) + ")";
 			pattern = "^.*_" + spWords + "$";
 			Iterator<Map.Entry<String, String>> iter3 = this.getUnknownWordHolder()
 					.entrySet().iterator();
 			while (iter3.hasNext()) {
 				Map.Entry<String, String> entry = iter3.next();
 				String newWord = entry.getKey();
 				String flag = entry.getValue();
 				// case 2.2: word_$spwords
 				if ((newWord.matches(pattern)) && (flag.equals("unknown"))) {
 					sign = sign
 							+ processNewWord(newWord, pos, "*", table, word, 0);
 					
 					myLogger.debug("Case 2.2");
 					myLogger.debug("by adding a prefix to " + word
 								+ ", know " + newWord + " is a [" + pos + "]");
 				}
 			}
 		}
 
 		return sign;
 	}
 	
 	/**
 	 * This method handles a new word when the updateDataHolder method is called
 	 * 
 	 * @param newWord
 	 * @param pos
 	 * @param role
 	 * @param table which table to update. "wordpos" or "modifiers"
 	 * @param flag
 	 * @param increment
 	 * @return if a new word was added, returns 1; otherwise returns 0
 	 */
 	public int processNewWord(String newWord, String pos, String role,
 			String table, String flag, int increment) {
 				
 		int sign = 0;
 		// remove the new word from unknownword holder
 		this.updateUnknownWord(newWord, flag);
 		
 		// insert the new word to the specified data holder
 		if (table.equals("wordpos")) {
 			sign = sign + updatePOS(newWord, pos, role, increment);
 		} else if (table.equals("modifiers")) {
 			sign = sign + this.addModifier(newWord, increment);
 		}
 
 		return sign;
 	}
 	
 	/**
 	 * update the pos of a word
 	 * 
 	 * @param newWord
 	 * @param newPOS
 	 * @param newRole
 	 * @param increment
 	 * @return
 	 */
 	public int updatePOS(String newWord, String newPOS, String newRole, int increment) {		
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder.updatePOS");
 		
 		myLogger.trace("Enter updatePOS");
 		myLogger.trace("Word: "+newWord+", POS: "+newPOS);
 		
 		
 		int n = 0;
 				
 		String regex = "^.*(\\b|_)(NUM|" + Constant.NUMBER + "|"
 				+ Constant.CLUSTERSTRING + "|" + Constant.CHARACTER + ")\\b.*$";
 		//regex = "(NUM|" + "rows" + ")";
 		boolean case1 = newWord.matches(regex);
 		boolean case2 = newPOS.matches("[nsp]"); 
 		if (case1 && case2) {
 			myLogger.trace("Case 0");
 			myLogger.trace("Quite updatePOS");
 			return 0;
 		}
 
 //		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter = this.getWordPOSHolder()
 //				.entrySet().iterator();
 //		// boolean isExist = false;
 //		Map.Entry<WordPOSKey, WordPOSValue> targetWordPOS = null;
 //		while (iter.hasNext()) {
 //			Map.Entry<WordPOSKey, WordPOSValue> wordPOS = iter.next();
 //			if (wordPOS.getKey().getWord().equals(newWord)) {
 //				targetWordPOS = wordPOS;
 //				break;
 //			}
 //		}
         
         List<Entry<WordPOSKey, WordPOSValue>> entryList = getWordPOSEntries(newWord);
         int certaintyU = 0;
 		// case 1: the word does not exist, add it
         if (entryList.size()==0) {
 		// if (targetWordPOS == null) {
 			myLogger.trace("Case 1");
 			certaintyU += increment;
 			this.updateWordPOS(new WordPOSKey(newWord, newPOS), new WordPOSValue(newRole, certaintyU, 0, null, null));
 			n = 1;
 			myLogger.trace(String.format("\t: new [%s] pos=%s, role =%s, certaintyU=%d", newWord, newPOS, newRole, certaintyU));
 		// case 2: the word already exists, update it
 		} else {
 			myLogger.trace("Case 2");
 			Entry<WordPOSKey, WordPOSValue> targetWordPOS = entryList.get(0);
 			String oldPOS = targetWordPOS.getKey().getPOS();
 			String oldRole = targetWordPOS.getValue().getRole();
 			certaintyU = targetWordPOS.getValue().getCertaintyU();
 			// case 2.1 
 			// 		the old POS is NOT same as the new POS, 
 			// 	AND	the old POS is b or the new POS is b
 			if ((!oldPOS.equals(newPOS))
 					&& ((oldPOS.equals("b")) || (newPOS.equals("b")))) {
 				myLogger.trace("Case 2.1");
 				String otherPOS = newPOS.equals("b") ? oldPOS : newPOS;
 				newPOS = this.resolveConflict(newWord, "b", otherPOS);
 
 				boolean flag = false;
 				if (newPOS != null) {
 					if (!newPOS.equals(oldPOS)) {
 						flag = true;
 					}
 				}
 
 				// new pos win
 				if (flag) { 
 					newRole = newRole.equals("*") ? "" : newRole;
 					n = n + changePOS(newWord, oldPOS, newPOS, newRole, increment);
 				// old pos win
 				} else { 
 					newRole = oldRole.equals("*") ? newRole : oldRole;
 					certaintyU = certaintyU + increment;
 //					WordPOSKey key = new WordPOSKey("newWord", "pos");
 //					WordPOSValue value = new WordPOSValue(newRole, certaintyU, 0,
 //							null, null);
 //					this.getWordPOSHolder().put(key, value);
 					
 					this.updateWordPOS(newWord, newPOS, newRole, certaintyU, 0, null, null);
 					
 					myLogger.debug(String.format("\t: update [%s (%s):a] role: %s=>%s, certaintyU=%d\n",
 									newWord, newPOS, oldRole, newRole, certaintyU));
 				}
 				
 			// case 2.2: the old POS and the new POS are all [n],  update role and certaintyU
 			} else {
 				myLogger.trace("Case 2.2");
 				newRole = this.mergeRole(oldRole, newRole);
 				certaintyU += increment;
 //				WordPOSKey key = new WordPOSKey(newWord, newPOS);
 //				WordPOSValue value = new WordPOSValue(newRole, certaintyU, 0,
 //						null, null);
 //				this.getWordPOSHolder().put(key, value);
 				
 				this.updateWordPOS(newWord, newPOS, newRole, certaintyU, 0, null, null);
 				
 				myLogger.debug(String.format("\t: update [%s (%s):b] role: %s => %s, certaintyU=%d\n",
 								newWord, newPOS, oldRole, newRole, certaintyU));
 			}
 		}
 
 		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter2 = this.getWordPOSHolderIterator();
 		int certaintyL = 0;
 		while (iter2.hasNext()) {
 			Map.Entry<WordPOSKey, WordPOSValue> e = iter2.next();
 			if (e.getKey().getWord().equals(newWord)) {
 				certaintyL += e.getValue().getCertaintyU();
 			}
 		}
 		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter3 = this.getWordPOSHolderIterator();
 		while (iter3.hasNext()) {
 			Map.Entry<WordPOSKey, WordPOSValue> e = iter3.next();
 			if (e.getKey().getWord().equals(newWord)) {
 				e.getValue().setCertiantyU(certaintyL);
 			}
 		}
 
 		myLogger.debug(String.format("\t: total occurance of [%s] = %d\n", newWord, certaintyL));
 		myLogger.trace("Return: " + n);
 		return n;
 	}
 	
 	/**
 	 * This method corrects the pos of the word from N to M (establish newPOS)
 	 * 
 	 * @param newWord
 	 * @param oldPOS
 	 * @param newPOS
 	 * @param newRole
 	 * @param increment
 	 * @return
 	 */
 	public int changePOS(String newWord, String oldPOS, String newPOS,
 			String newRole, int increment) {		
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder.changePOS");		
 		myLogger.trace("Enter changePOS");
 		myLogger.trace("newWord: "+newWord);
 		myLogger.trace("oldPOS: "+oldPOS);
 		myLogger.trace("newPOS: "+newPOS);
 		myLogger.trace("newRole: "+newRole);
 		
 		oldPOS = oldPOS.toLowerCase();
 		newPOS = newPOS.toLowerCase();
 
 		String modifier = "";
 		String tag = "";
 		String sentence = null;
 		int sign = 0;
 
 		// case 1: oldPOS is "s" AND newPOS is "m"
 		//if (oldPOS.matches("^.*s.*$") && newPOS.matches("^.*m.*$")) {
 		if (oldPOS.equals("s") && newPOS.equals("m")) {
 			myLogger.trace("Case 1");
 			this.discountPOS(newWord, oldPOS, newPOS, "all");
 			sign += markKnown(newWord, "m", "", "modifiers", increment);
 			
 			// For all the sentences tagged with $word (m), re tag by finding their parent tag.
 			for (int i = 0; i < this.getSentenceHolder().size(); i++) {
 				SentenceStructure sent = this.getSentenceHolder().get(i);
 				if (sent.getTag().equals(newWord)) {
 					int sentID = i;
 					modifier = sent.getModifier();
 					tag = sent.getTag();
 					sentence = sent.getSentence();
 					
 					tag = this.getParentSentenceTag(sentID);
 					modifier = modifier + " " + newWord;
 					modifier.replaceAll("^\\s*", "");
 					List<String> pair = this.getMTFromParentTag(tag);
 					String m = pair.get(1);
 					tag = pair.get(2);
 					if (m.matches("^.*\\w.*$")) {
 						modifier = modifier + " " + m;
 					}
 					this.tagSentenceWithMT(sentID, sentence, modifier, tag, "changePOS[n->m:parenttag]");
 				}
 			}
 			
 		} 
 		// case 2: oldPOS is "s" AND newPOS is "b"
 		else if ((oldPOS.matches("s")) && (newPOS.matches("b"))) {
 			myLogger.trace("Case 2");
 			int certaintyU = 0;
 
 			// find (newWord, oldPOS)
 			WordPOSKey newOldKey = new WordPOSKey(newWord, oldPOS);
 			if (this.getWordPOSHolder().containsKey(newOldKey)) {
 				WordPOSValue v = this.getWordPOSHolder().get(newOldKey);
 				certaintyU = v.getCertaintyU();
 				certaintyU += increment;
 				this.discountPOS(newWord, oldPOS, newPOS, "all");
 			}
 
 			// find (newWord, newPOS)
 			WordPOSKey newNewKey = new WordPOSKey(newWord, newPOS);
 			if (!this.getWordPOSHolder().containsKey(newNewKey)) {
 //				this.getWordPOSHolder().put(newNewKey, new WordPOSValue(newRole,
 //						certaintyU, 0, "", ""));
 				this.add2Holder(DataHolder.WORDPOS, 
 						Arrays.asList(new String [] {newWord, newPOS, newRole, Integer.toString(certaintyU), "0", "", ""}));
 			}
 			
 			myLogger.debug("\t: change ["+newWord+"("+oldPOS+" => "+newPOS+")] role=>"+newRole+"\n");
 			sign++;
 
 			// for all sentences tagged with (newWord, "b"), re tag them
 			for (int i = 0; i < this.getSentenceHolder().size(); i++) {
 				String thisTag = this.getSentenceHolder().get(i).getTag();
 				int thisSentID = i;
 				String thisSent = this.getSentenceHolder().get(i).getSentence();
 				if (StringUtils.equals(thisTag, newWord)) {						
 					this.tagSentenceWithMT(thisSentID, thisSent, "", "NULL", "changePOS[s->b: reset to NULL]");
 				}
 			}
 		}
 		// case 3: oldPOS is "b" AND newPOS is "s"
 		else if (oldPOS.matches("b") && newPOS.matches("s")) {
 			myLogger.trace("Case 3");
 			int certaintyU = 0;
 
 			// find (newWord, oldPOS)
 			WordPOSKey newOldKey = new WordPOSKey(newWord, oldPOS);
 			if (this.getWordPOSHolder().containsKey(newOldKey)) {
 				WordPOSValue v = this.getWordPOSHolder().get(newOldKey);
 				certaintyU = v.getCertaintyU();
 				certaintyU += increment;
 				this.discountPOS(newWord, oldPOS, newPOS, "all");
 			}
 
 			// find (newWord, newPOS)
 			WordPOSKey newNewKey = new WordPOSKey(newWord, newPOS);
 			if (!this.getWordPOSHolder().containsKey(newOldKey)) {
 //				this.getWordPOSHolder().put(newNewKey, );
 				this.updateWordPOS(newNewKey, new WordPOSValue(newRole,
 						certaintyU, 0, "", ""));
 			}
 			
 			myLogger.debug("\t: change ["+newWord+"("+oldPOS+" => "+newPOS+")] role=>"+newRole+"\n");
 			sign++;
 		}
 		
 		int sum_certaintyU = this.getSumCertaintyU(newWord);
 		
 		if (sum_certaintyU > 0) {
 			Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter2 = this.getWordPOSHolderIterator();
 			while (iter2.hasNext()) {
 				Map.Entry<WordPOSKey, WordPOSValue> e = iter2.next();
 				if (e.getKey().getWord().equals(newWord)) {
 					e.getValue().setCertiantyL(sum_certaintyU);
 				}
 			}
 		}
 
 		myLogger.trace("Return: "+sign);
 		myLogger.trace("Quite changePOS\n");
 		return sign;
 	}
 	
 	/**
 	 * 
 	 * @param sentID
 	 * @param sentence
 	 * @param modifier
 	 * @param tag tag could be "null"
 	 * @param label
 	 */
 	public void tagSentenceWithMT(int sentID, String sentence, String modifier,
 			String tag, String label) {
 		/**
 		 * 1. Do some preprocessing of modifier and tag 
 		 *     1. Remove -ly words 
 		 *     1. Update modifier and tag of sentence sentID in Sentence
 		 */
 		
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.updateDataHolder.tagSentenceWithWT");
 		
 		myLogger.trace(String.format("Enter (%d, %s, %s, %s, %s)", sentID,
 				sentence, modifier, tag, label));
 		
 		if (this.getSentence(sentID) == null) {
 			return;
 		}
 		
 		if (modifier != null) {
 			// modifier preprocessing
 			modifier = this.tagSentWithMTPreProcessing(modifier);
 			// Remove any -ly ending word which is a "b" in the WordPOS, from
 			// the modifier
 			modifier = this.tagSentWithMTRemoveLyEndingBoundary(modifier);
 			modifier = StringUtility.removeAll(modifier, "(^\\s*|\\s*$)");
 		}
 		
		tag = this.tagSentWithMTPreProcessing(tag);
		tag = StringUtility.removeAll(tag, "(^\\s*|\\s*$)");
 
 		if (tag == null) {
 			this.getSentenceHolder().get(sentID).setTag(null);
 			this.getSentenceHolder().get(sentID).setModifier(modifier);			
 		}
 		else {
 			if (tag.length() > this.myConfiguration.getMaxTagLength()) {
 				tag = tag.substring(0, this.myConfiguration.getMaxTagLength());
 			}
 			this.sentenceTable.get(sentID).setTag(tag);
 			this.sentenceTable.get(sentID).setModifier(modifier);	
 		}
 
 		for (int i = 0; i < this.sentenceTable.size(); i++) {
 			this.sentenceTable.get(sentID).setTag(tag);
 			this.sentenceTable.get(sentID).setModifier(modifier);
 		}
 
 		myLogger.trace(label);
 		myLogger.trace("Quite tagSentenceWithMT");
 	}
 	
 	public String tagSentWithMTPreProcessing(String text) {	
 		if (text == null) {
 			return null;
 		}
 		
 		text = text.replaceAll("<\\S+?>", "");
 
 		text = StringUtility.removeAllRecursive(text, "^(" + Constant.STOP
 				+ "|" + Constant.FORBIDDEN+")\\b\\s*");
 
 		// remove stop and forbidden words from ending
 		text = StringUtility.removeAllRecursive(text, "\\s*\\b(" + Constant.STOP
 				+ "|" + Constant.FORBIDDEN + "|\\w+ly)$");
 
 		// remove all pronoun words
 		text = StringUtility.removeAllRecursive(text, "\\b(" + Constant.PRONOUN
 				+ ")\\b");
 		
 		return text;
 	}
 	
 	public int getSumCertaintyU(String word) {
 		int sumCertaintyU = 0;
 		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter = this.getWordPOSHolderIterator();
 		while (iter.hasNext()) {
 			Map.Entry<WordPOSKey, WordPOSValue> e = iter.next();
 			if (e.getKey().getWord().equals(word)) {
 				sumCertaintyU += e.getValue().getCertaintyU();
 			}
 		}
 		
 		return sumCertaintyU;
 	}
 
 	/**
 	 * return singular and plural variations of the word
 	 * 
 	 * @param word
 	 * @return all variations of the word
 	 */
 	public String singularPluralVariations(String word, Set<SingularPluralPair> singularPluralHolder) {
 		String variations = word + "|";
 		Iterator<SingularPluralPair> iter = singularPluralHolder.iterator();
 		while (iter.hasNext()) {
 			SingularPluralPair pair = iter.next();
 			String sg = pair.getSingular();
 			String pl = pair.getPlural();
 			if (sg.equals(word) && (!pl.equals(""))) {
 				variations = variations + pl + "|";
 			}
 			if (pl.equals(word) && (!sg.equals(""))) {
 				variations = variations + sg + "|";
 			}
 		}
 
 		variations = StringUtility.removeAll(variations, "\\|+$");
 
 		return variations;
 	}
 	
 	/**
 	 * mark the words between the start index and the end index as modifiers if
 	 * they are valid words.
 	 * 
 	 * @param start
 	 *            the start index
 	 * @param end
 	 *            the end index
 	 * @param words
 	 *            a list of words
 	 * @return number of updates made
 	 */
 	public int updateDataHolderNN(int start, int end, List<String> words) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.updateDataHolderNN");
 		myLogger.trace(String.format("Enter (%d, %d, %s)", start, end,
 				words.toString()));
 				
 		int update=0;
 		List<String> splicedWords = StringUtility.stringArraySplice(words, start, end); 
 		
 		for (int i=0;i<splicedWords.size()-1;i++) {
 			String word = splicedWords.get(i);
 
 			myLogger.trace("Check N: " + word);
 
 			if (this.updateDataHolderNNConditionHelper(word)) {
 				myLogger.trace("Update N: " + word);
 				int temp = this.updateDataHolder(word, "m", "", "modifiers", 1);
 				update = update + temp;
 				myLogger.trace("Return: " + temp);
 			}
 		}
 		
 		myLogger.trace("Return: " + update + "\n");
 		return update;
 	}
 	
 	/**
 	 * A helper of method updateDataHolderNN. Check if the condition is meet.
 	 * 
 	 * @param word
 	 *            the word to check
 	 * @return a boolean variable
 	 */
 	public boolean updateDataHolderNNConditionHelper(String word) {
 		boolean flag = false;
 		
 		flag = (   (!word.matches("^.*\\b("+Constant.STOP+")\\b.*$"))
 				&& (!word.matches("^.*ly\\s*$"))
 				&& (!word.matches("^.*\\b("+Constant.FORBIDDEN+")\\b.*$"))
 				);
 		
 		return flag;
 	}
 	
 	/**
 	 * Return (POS, role, certaintyU, certaintyL) of a word
 	 * 
 	 * @param word
 	 *            the word to check
 	 * @return entries of (POS, role, certaintyU, certaintyL) of the word in a
 	 *         list
 	 */
 	public List<POSInfo> checkPOSInfo(String word) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("dataholder.checkPOSInfo");
 		myLogger.trace("Enter ("+word+")");
 		List<POSInfo> POSInfoList = new ArrayList<POSInfo>();
 
 		word = StringUtility.removeAll(word, "^\\s*");
 		word = StringUtility.removeAll(word, "\\s+$");
 
 		if (word.matches("^\\d+.*$")) {
 			POSInfo p = new POSInfo(word, "b", "", 1, 1);
 			POSInfoList.add(p);
 			myLogger.trace("Reture: "+POSInfoList);
 			return POSInfoList;
 		}
 
 		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter = this.getWordPOSHolderIterator();
 		while (iter.hasNext()) {
 			Map.Entry<WordPOSKey, WordPOSValue> e = iter.next();
 			String w = e.getKey().getWord();
 			if (w.equals(word)) {
 				String POS = e.getKey().getPOS();
 				String role = e.getValue().getRole();
 				int certaintyU = e.getValue().getCertaintyU();
 				int certaintyL = e.getValue().getCertaintyL();
 				POSInfo p = new POSInfo(word, POS, role, certaintyU, certaintyL);
 				POSInfoList.add(p);
 			}
 		}
 
 		// nothing found
 		if (POSInfoList.size() != 0) {
 			// sort the list in ascending order of certaintyU/certaintyL
 			Collections.sort(POSInfoList);
 			// reverse it into descending order
 			Collections.reverse(POSInfoList);
 		}
 
 		myLogger.trace("Reture: "+POSInfoList);
 		return POSInfoList;
 
 	}
 	
 	public Set<String> getBMSWords() {
 		return this.BMSWords;
 	}
 	
 	public Set<String> getTypeModifierPattern() {
 		Set<String> words = new HashSet<String>();
 		
 		Iterator<Entry<String, ModifierTableValue>> modifierIter = this.getModifierHolderIterator();
 		
 		while (modifierIter.hasNext()) {
 			Entry<String, ModifierTableValue> modifierItem = modifierIter.next();
 			if (modifierItem.getValue().getIsTypeModifier()) {
 				String word = modifierItem.getKey();
 				words.add(word);
 			}
 		}
 		
 		return words;
 	}
 	
 	
 
 	/**
 	 * Get a list of all tags which is not "ignore".
 	 * 
 	 * @return a set of tags
 	 */
 	public Set<String> getCurrentTags() {
 		Set<String> tags = new HashSet<String>();
 		
 		for (int i=0;i<this.sentenceTable.size();i++) {
 			SentenceStructure sentence = this.sentenceTable.get(i);
 			String tag = sentence.getTag();
 			if ((!StringUtils.equals(tag, "ignore"))){
 				tags.add(tag);
 			}
 		}
 		
 		return tags;
 	}
 	
 	public void untagSentences(){
 		for (SentenceStructure sentenceItem : this.sentenceTable) {
 			String sentence = sentenceItem.getSentence();
 			String tag = sentenceItem.getTag();
 			boolean c1 = StringUtils.equals(tag, "ignore");
 			boolean c2 = (tag == null);
 			boolean c3 = StringUtility.isMatchedNullSafe(sentence, "<");
 			if ((c1||c2)&&c3) {
 				sentence = sentence.replaceAll("<\\S+?>", "");
 				sentence = sentence.replaceAll("'", "\\'");
 				sentenceItem.setSentence(sentence);				
 			}
 		}
 	}
 	
 	// add2Holder
 	public void addWords2WordPOSHolder(Set<String> words, String POS) {
 		Iterator<String> iter = words.iterator();
 		String word = iter.next();
 		this.add2WordPOSHolder(word, POS, "", 0, 0, null, null);
 	}
 	
 	public boolean add2WordPOSHolder(String word, String POS, String role, int certaintyU, int certaintyL, String savedFlag, String savedID) {
 		boolean isUpdated = false;
 		
 		WordPOSKey key = new WordPOSKey(word, POS);
 		WordPOSValue value = new WordPOSValue(role, certaintyU, certaintyL, savedFlag, savedID);
 		if (this.wordPOSTable.containsKey(key)) {
 			if (this.wordPOSTable.get(key).equals(value)) {
 				isUpdated = false;
 			}
 			else {
 //				this.wordPOSTable.put(key, value);
 				this.updateWordPOS(key, value);
 				isUpdated = true;
 			}
 		}
 		else {
 //			this.wordPOSTable.put(key, value);
 			this.updateWordPOS(key, value);
 			isUpdated = true;
 		}
 		
 		return isUpdated;
 	}
 	
 	public void writeToFile(String dir, String fileNamePrefix) {
 		if (fileNamePrefix == null) {
 			fileNamePrefix = "";
 		}
 
 		if (!StringUtils.equals(fileNamePrefix, "")) {
 			fileNamePrefix = fileNamePrefix + "_";
 		}
 		
 		// writer
 		PrintWriter writer = null;
 
 		// Discounted Holder
 		try {
 			String fullPath = dir + "/" + fileNamePrefix + "Discounted.csv";
 			File file = new File(fullPath);
 			file.getParentFile().mkdirs();
 			writer = new PrintWriter(fullPath, "UTF-8");
 
 			// write header
 			writer.println("word, discounted POS, possible new POS");
 
 			// write content
 			Iterator<Entry<DiscountedKey, String>> iter = this.discountedTable.entrySet().iterator();
 			while (iter.hasNext()) {
 				Entry<DiscountedKey, String> discountedEntry = iter.next();
 
 				writer.println(String.format("%s, %s, %s",
 						discountedEntry.getKey().getWord(),
 						discountedEntry.getKey().getPOS(),
 						discountedEntry.getValue()
 						));
 			}
 			writer.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// HeuristicNouns Holder
 		try {
 			String fullPath = dir + "/" + fileNamePrefix + "HeuristicNouns.csv";
 			File file = new File(fullPath);
 			file.getParentFile().mkdirs();
 			writer = new PrintWriter(fullPath, "UTF-8");
 
 			// write header
 			writer.println("word, type");
 
 			// write content
 			Iterator<Entry<String, String>> iter = this.heuristicNounTable.entrySet().iterator();
 			while (iter.hasNext()) {
 				Entry<String, String> heuristicNounEntry = iter.next();
 
 				writer.println(String.format("%s, %s",
 						heuristicNounEntry.getKey(),						
 						heuristicNounEntry.getValue()
 						));
 			}
 			writer.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// IsA Holder
 		try {
 			String fullPath = dir + "/" + fileNamePrefix + "IsA.csv";
 			File file = new File(fullPath);
 			file.getParentFile().mkdirs();
 			writer = new PrintWriter(fullPath, "UTF-8");
 
 			// write header
 			writer.println("ID, instance, class");
 
 			// write content
 			Iterator<Entry<Integer, IsAValue>> iter = this.isATable.entrySet().iterator();
 			while (iter.hasNext()) {
 				Entry<Integer, IsAValue> isAEntry = iter.next();
 
 				writer.println(String.format("%d, %s, %s",
 						isAEntry.getKey(),						
 						isAEntry.getValue().getInstance(),
 						isAEntry.getValue().getCls()
 						));
 			}
 			writer.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// Modifiers Holder
 		try {
 			String fullPath = dir + "/" + fileNamePrefix + "Modifiers.csv";
 			File file = new File(fullPath);
 			file.getParentFile().mkdirs();
 			writer = new PrintWriter(fullPath, "UTF-8");
 
 			// write header
 			writer.println("word, count, is type modifier");
 
 			// write content
 			Iterator<Entry<String, ModifierTableValue>> iter = this.modifierTable.entrySet().iterator();
 			while (iter.hasNext()) {
 				Entry<String, ModifierTableValue> modifierEntry = iter.next();
 
 				writer.println(String.format("%s, %d, %b",
 						modifierEntry.getKey(),						
 						modifierEntry.getValue().getCount(),
 						modifierEntry.getValue().getIsTypeModifier()
 						));
 			}
 			writer.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// Sentence Holder
 		try {
 			String fullPath = dir + "/" + fileNamePrefix + "Sentence.csv";
 			File file = new File(fullPath);
 			file.getParentFile().mkdirs();
 			writer = new PrintWriter(fullPath, "UTF-8");
 
 			// write header
 			writer.println("sentence ID, source, sentence, original sentence, lead, status, tag, modifier, type");
 			
 			// write content
 			for (SentenceStructure sentenceItem : this.sentenceTable) {
 				writer.println(String.format("%d, %s, %s, %s, %s, %s, %s, %s, %s",
 						sentenceItem.getID(),
 						sentenceItem.getSource(),
 						sentenceItem.getSentence(),
 						sentenceItem.getOriginalSentence(),
 						sentenceItem.getLead(),
 						sentenceItem.getStatus(),
 						sentenceItem.getTag(),
 						sentenceItem.getModifier(),
 						sentenceItem.getType()
 						));
 			}
 			writer.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// SingularPlural Holder
 		try {
 			String fullPath = dir + "/" + fileNamePrefix + "SingularPlural.csv";
 			File file = new File(fullPath);
 			file.getParentFile().mkdirs();
 			writer = new PrintWriter(fullPath, "UTF-8");
 
 			// write header
 			writer.println("singular, plural");
 			
 			// write content
 			for (SingularPluralPair pair : this.singularPluralTable) {
 				writer.println(String.format("%s, %s",
 						pair.getSingular(),
 						pair.getPlural()
 						));
 			}
 			writer.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// TermCategory Holder
 		try {
 			String fullPath = dir + "/" + fileNamePrefix + "TermCategory.csv";
 			File file = new File(fullPath);
 			file.getParentFile().mkdirs();
 			writer = new PrintWriter(fullPath, "UTF-8");
 
 			// write header
 			writer.println("term, category");
 			
 			// write content
 			for (StringPair pair : this.termCategoryTable) {
 				writer.println(String.format("%s, %s",
 						pair.getHead(),
 						pair.getTail()
 						));
 			}
 			writer.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// UnknownWord Holder
 		try {
 			String fullPath = dir + "/" + fileNamePrefix + "UnknownWords.csv";
 			File file = new File(fullPath);
 			file.getParentFile().mkdirs();
 			writer = new PrintWriter(fullPath, "UTF-8");
 
 			// write header
 			writer.println("word, flag");
 
 			// write content
 			Iterator<Entry<String, String>> iter = this.unknownWordTable.entrySet().iterator();
 			while (iter.hasNext()) {
 				Entry<String, String> unknownWordEntry = iter.next();
 
 				writer.println(String.format("%s, %s",
 						unknownWordEntry.getKey(),						
 						unknownWordEntry.getValue()
 						));
 			}
 			writer.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// WordPOS holder
 		try {
 			String fullPath = dir + "/" + fileNamePrefix + "WordPOS.csv";
 			File file = new File(fullPath);
 			file.getParentFile().mkdirs();
 			writer = new PrintWriter(fullPath, "UTF-8");
 
 			// write header
 			writer.println("word, POS, role, certaintyU, certiantyL, saved_flag, saveedID");
 			
 			// write content
 			Iterator<Entry<WordPOSKey, WordPOSValue>> iter = this
 					.getWordPOSHolderIterator();
 			while (iter.hasNext()) {
 				Entry<WordPOSKey, WordPOSValue> wordPOSItem = iter.next();				
 				
 				writer.println(String.format("%s, %s, %s, %d, %d, %s, %s", 
 						wordPOSItem.getKey().getWord(), 
 						wordPOSItem.getKey().getPOS(),
 						wordPOSItem.getValue().getRole(),
 						wordPOSItem.getValue().getCertaintyU(),
 						wordPOSItem.getValue().getCertaintyL(),
 						wordPOSItem.getValue().getSavedFlag(),
 						wordPOSItem.getValue().getSavedID()
 						));
 			}
 			writer.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// WordRole holder
 		try {
 			String fullPath = dir + "/" + fileNamePrefix + "WordRole.csv";
 			File file = new File(fullPath);
 			file.getParentFile().mkdirs();
 			writer = new PrintWriter(fullPath, "UTF-8");
 
 			// write header
 			writer.println("word, semantic role, saved ID");
 			
 			// write content
 			Iterator<Entry<StringPair, String>> iter = this.wordRoleTable.entrySet().iterator();
 			while (iter.hasNext()) {
 				Entry<StringPair, String> wordRoleItem = iter.next();				
 				
 				writer.println(String.format("%s, %s, %s", 
 						wordRoleItem.getKey().getHead(), 
 						wordRoleItem.getKey().getTail(),
 						wordRoleItem.getValue()
 						));
 			}
 			writer.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
