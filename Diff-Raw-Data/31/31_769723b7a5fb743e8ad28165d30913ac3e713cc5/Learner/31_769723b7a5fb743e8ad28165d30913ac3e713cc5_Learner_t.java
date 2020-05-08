 package semanticMarkup.ling.learn;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 

 
 import semanticMarkup.core.Treatment;
 import semanticMarkup.knowledge.lib.WordNetAPI;
 
 public class Learner {	
 	/**************************************************
 	 * Debug variables used by developer
 	 */
 	private boolean populateSentence_debug = false;
 	private boolean addHeuristicsNouns_debug = false;
 	private boolean getHeuristicNouns_debug = false;
 	private boolean posBySuffix_debug = false;
 	private boolean characterHeuristics_debug = false;
 	/**************************************************
 	 */
 
 	private Configuration myConfiguration;
 	
 	// Data holder
 	DataHolder myDataHolder = new DataHolder();
 	
 	// Utilities
 	private WordFormUtility myWordFormUtility;
 	private PopulateSentenceUtility myPopulateSentenceUtility;
 	
 	// Class variables
 	private int NUM_LEAD_WORDS; // Number of leading words
 	
 	// others
 	// tag length
 	private int tagLength = 150;
 	// leading three words of sentences
 	private String CHECKEDWORDS = ":"; 
 	
 	public Learner(Configuration configuration) {
 		this.myConfiguration = configuration;
 		
 		// Data holder
 		myDataHolder = new DataHolder();
 		
 		// Utilities
 		this.myWordFormUtility = new WordFormUtility(this.myConfiguration.getWordNetDictDir());
 		this.myPopulateSentenceUtility = new PopulateSentenceUtility(this.myConfiguration.getSentenceDetector(), this.myConfiguration.getTokenizer());
 		
 		// Class variables
 		NUM_LEAD_WORDS = 3; // Set the number of leading words be 3
 		
 	}
 
 	public DataHolder Learn(List<Treatment> treatments) {
 		System.out.println(String
 				.format("Learning Mode: %s", this.myConfiguration.getLearningMode()));
 		this.populateSentences(treatments);
 		this.populateUnknownWordsTable(this.myDataHolder.allWords);
 
 		/*
 		Map<String, String> myHeuristicNounTable = myDataHolder.getHeuristicNounTable();
 		myHeuristicNounTable.put("word1", "type1");
 		
 		List<Sentence> mySentenceTable = myDataHolder.getSentenceTable();
 		mySentenceTable.add(new Sentence("source1", "sentence1", "originalSentence", "lead1", "status1", "tag1", "modifier1", "type1"));
 		*/
 		
 		// List<String> fileNameList = fileLoader.getFileNameList();
 		// List<Integer> typeList = fileLoader.getTypeList();
 
 		// List<String> textList = fileLoader.getTextList();
 
 		// process treatments
 		//this.populateSentences(treatments);
 		
 
 		// pre load words
 		this.addHeuristicsNouns();
 		this.addPredefinedWords();
 
 
 		// ???
 		//this.posBySuffix();
 		//this.markupByPattern();
 		//this.markupIgnore();
 
 		// learning rules with high certainty
 		//this.discover("start");
 		// bootstrapping rules
 		//this.discover("normal");
 
 		//System.out.println("Method: learn - Done!\n");
 
 		return myDataHolder;
 	}
 	
 	private void addPredefinedWords() {
 		this.addStopWords();
 		this.addCharacters();
 		this.addNumbers();
 		this.addClusterstrings();
 		this.addProperNouns();		
 	}
 
 	/**
 	 * 
 	 * @param treatments
 	 * @return number of sentences
 	 */
 	public int populateSentences(List<Treatment> treatments) {
 		System.out.println("Reading sentences:\n");
 
 		String fileName;
 		int type;
 		String text;
 		int SENTID = 0;
 
 		for (int i = 0; i < treatments.size(); i++) {
 			Treatment tm = treatments.get(i);
 			fileName = tm.getFileName();
 			text = tm.getDescription();
 			type = this.myPopulateSentenceUtility.getType(fileName);
 
 			if (text != null) {
 				// process this text
 				text = this.handleText(text);
 				if (populateSentence_debug)
 					System.out.println("Text: " + text);
 
 				// use Apache OpenNLP to do sentence segmentation
 				String sentences[] = {};
 				sentences = this.myPopulateSentenceUtility.segmentSentence(text);
 
 				List<String> sentCopy = new LinkedList<String>();
 				List<Integer> validIndex = new LinkedList<Integer>();
 				
 				// for each sentence, do some operations
 				for (int j = 0; j < sentences.length; j++) {
 					if (populateSentence_debug)
 						System.out.println("Sentence " + j + ": "
 								+ sentences[j]);
 					if (populateSentence_debug)
 						System.out.println(sentences[j]);
 //					if (sentences[j].equals("mesodentine")) {
 //						System.out.println(sentences[j]);
 //					}
 					// if(!/\w+/){next;}
 					if (!sentences[j].matches("^.*\\w+.*$")) {
 						continue;
 					}
 
 					// This is a valid sentence, save the index
 					validIndex.add(j);
 
 					// restore marks in brackets
 					sentences[j] = this.myPopulateSentenceUtility.restoreMarksInBrackets(sentences[j]);
 					// Make a copy of the sentence
 					sentCopy.add(sentences[j]);
 
 					// process the sentence
 					sentences[j] = this.handleSentence(sentences[j]);
 
 					// store all words
 					this.myDataHolder.allWords = this.myPopulateSentenceUtility.getAllWords(sentences[j], this.myDataHolder.allWords);
 				}
 
 				for (int j = 0; j < validIndex.size(); j++) {
 					String line = sentences[validIndex.get(j)];
 					String oline = sentCopy.get(j);
 //					if (oline.equals("mesodentine")) {
 //						System.out.println(oline);
 //					}
 
 					// handle line first
 					// remove all ' to avoid escape problems
 					// $line =~ s#'# #g;
 					line.replaceAll("\'", " ");
 
 					// then handle oline
 					Matcher matcher = Pattern.compile(
 							"(\\d)\\s*\\[\\s*DOT\\s*\\]\\s*(\\d)").matcher(
 							oline);
 					if (matcher.lookingAt()) {
 						oline = oline.replaceAll(
 								"(\\d)\\s*\\[\\s*DOT\\s*\\]\\s*(\\d)",
 								matcher.group(1) + matcher.group(2));
 					}
 
 					// restore ".", "?", ";", ":", "."
 					oline = this.myPopulateSentenceUtility.restoreMarksInBrackets(oline);
 					oline = oline.replaceAll("\'", " ");
 
 					List<String> nWords = this.myPopulateSentenceUtility.getFirstNWords(line,
 							this.NUM_LEAD_WORDS);
 					String lead = "";
 					Iterator<String> iter = nWords.iterator();
 					while (iter.hasNext()) {
 						String w = iter.next();
 						lead = lead + w + " ";
 					}
 					lead = lead.replaceAll("\\s$", "");
 
 					String status = "";
 					if (myWordFormUtility.getNumber(nWords.get(0)).equals("p")) {
 						status = "start";
 					} else {
 						status = "normal";
 					}
 
 					lead = StringUtility.removeAll(lead, "\\s+$");
 					lead = StringUtility.removeAll(lead, "^\\s*");
 					lead = lead.replaceAll("\\s+", " ");
 
 					String source = fileName + "-" + Integer.toString(j);
 					if (oline.length() >= 2000) { // EOL
 						oline = line;
 					}
 					String typeStr = null;
 					switch (type) {
 					case 1:
 						typeStr = "character";
 						break;
 					case 2:
 						typeStr = "description";
 						break;
 					}
 
 					Sentence newSent = new Sentence(source, line, oline, lead,
 							status, null, null, typeStr);
 					this.myDataHolder.sentenceTable.add(newSent);
 
 					SENTID++;
 				}
 			}
 		}
 
 		//int numWord = this.populateUnknownWordsTable(WORDS);
 
 		System.out.println("Total sentences = " + SENTID);
 		//System.out.println("Total words = " + numWord);
 
 		return SENTID;
 	}
 	
 	/**
 	 * A helper of method pupulateSentence to handle text process
 	 * 
 	 * @param t
 	 * @return text after process
 	 */
 	public String handleText(String t) {
 
 		if (t == null || t == "") {
 			return t;
 		}
 
 		String text = t;
 
 		//
 		text = text.replaceAll("[\"']", "");
 
 		// plano - to
 		text = text.replaceAll("\\s*-\\s*to\\s+", " to ");
 
 		//
 		text = text.replaceAll("[-_]+shaped", "-shaped");
 
 		// unhide <i>
 		text = text.replaceAll("&lt;i&gt;", "<i>");
 
 		// unhide </i>, these will be used by characterHeuristics to
 		// collect taxon names
 		text = text.replaceAll("&lt;/i&gt;", "</i>");
 
 		// remove 2a. (key marks)
 		text = text.replaceAll("^\\s*\\d+[a-z].\\s*", "");
 
 		// this is not used any more, see perl code - Dongye
 		// store text at this point in original
 		// String original = text;
 
 		// remove HTML entities
 		text = text.replaceAll("&[;#\\w\\d]+;", " ");
 
 		//
 		text = text.replaceAll(" & ", " and ");
 
 		// replace '.', '?', ';', ':', '!' within brackets by some
 		// special markers, to avoid split within brackets during
 		// sentence segmentation
 		// System.out.println("Before Hide: "+text);
 		text = this.myPopulateSentenceUtility.hideMarksInBrackets(text);
 		// System.out.println("After Hide: "+text+"\n");
 
 		text = text.replaceAll("_", "-"); // _ to -
 		text = text.replaceAll("", ""); //
 
 		// absent ; => absent;
 		while (true) {
 			Matcher matcher1 = Pattern.compile("(^.*?)\\s+([:;\\.].*$)")
 					.matcher(text);
 			if (matcher1.lookingAt()) {
 				text = matcher1.group(1) + matcher1.group(2);
 			} else {
 				break;
 			}
 		}
 
 		// absent;blade => absent; blade
 		while (true) {
 			Matcher matcher2 = Pattern.compile("(^.*?\\w)([:;\\.])(\\w.*$)")
 					.matcher(text);
 			if (matcher2.lookingAt()) {
 				// text = text.replaceAll("^.*\\w[:;\\.]\\w.*",
 				// matcher2.group(1)
 				// + matcher2.group(2) + " " + matcher2.group(3));
 				text = matcher2.group(1) + matcher2.group(2) + " "
 						+ matcher2.group(3);
 			} else {
 				break;
 			}
 		}
 
 		// 1 . 5 => 1.5
 		while (true) {
 			Matcher matcher3 = Pattern.compile("(^.*?\\d\\s*\\.)\\s+(\\d.*$)")
 					.matcher(text);
 			if (matcher3.lookingAt()) {
 				text = matcher3.group(1) + matcher3.group(2);
 			} else {
 				break;
 			}
 		}
 
 		// ###NOT necessary at all, done before in "absent ; => absent;"###
 		// diam . =>diam.
 		// Matcher matcher4 =
 		// Pattern.compile("(\\sdiam)\\s+(\\.)").matcher(text);
 		// if (matcher4.lookingAt()) {
 		// text = text.replaceAll("\\sdiam\\s+\\.", matcher4.group(1)
 		// + matcher4.group(2));
 		// }
 
 		// ca . =>ca.
 		// Matcher matcher5 = Pattern.compile("(\\sca)\\s+(\\.)").matcher(text);
 		// if (matcher5.lookingAt()) {
 		// text = text.replaceAll("\\sca\\s+\\.",
 		// matcher5.group(1) + matcher5.group(2));
 		// }
 
 		//
 		while (true) {
 			Matcher matcher6 = Pattern.compile(
 					"(^.*\\d\\s+(cm|mm|dm|m)\\s*)\\.(\\s+[^A-Z].*$)").matcher(
 					text);
 			if (matcher6.lookingAt()) {
 				text = matcher6.group(1) + "[DOT]" + matcher6.group(3);
 			} else {
 				break;
 			}
 		}
 
 		return text;
 	}
 
 	/**
 	 * remove bracketed text from sentence (keep those in originalsent). Tthis
 	 * step will not be able to remove nested brackets, such as (petioles
 	 * (2-)4-8 cm). Nested brackets will be removed after threedsent step in
 	 * POSTagger4StanfordParser.java
 	 * 
 	 * @param s
 	 *            sentence to be handled
 	 * @return sentence after being processed
 	 */
 	public String handleSentence(String s) {
 		if (s == null || s == "") {
 			return s;
 		}
 
 		String sentence = s;
 
 		// remove (.a.)
 		sentence = sentence.replaceAll("\\([^()]*?[a-zA-Z][^()]*?\\)", " ");
 
 		// remove [.a.]
 		sentence = sentence.replaceAll("\\[[^\\]\\[]*?[a-zA-Z][^\\]\\[]*?\\]",
 				" ");
 
 		// remove {.a.}
 		sentence = sentence.replaceAll("\\{[^{}]*?[a-zA-Z][^{}]*?\\}", " ");
 
 		// to fix basi- and hypobranchial
 		while (true) {
 			Matcher matcher = Pattern.compile("(^.*?)\\s*[-]+\\s*([a-z].*$)")
 					.matcher(sentence);
 			if (matcher.lookingAt()) {
 				sentence = matcher.group(1) + "_ " + matcher.group(2);
 			} else {
 				break;
 			}
 		}
 
 		// add space around nonword char
 		sentence = this.myPopulateSentenceUtility.addSpace(sentence, "\\W");
 
 		// multiple spaces => 1 space
 		sentence = sentence.replaceAll("\\s+", " ");
 
 		// trim: remove leading and ending spaces
 		sentence = sentence.replaceAll("^\\s*", "");
 		sentence = sentence.replaceAll("\\s*$", "");
 
 		// all to lower case
 		sentence = sentence.toLowerCase();
 
 		return sentence;
 	}
 	
 	/**
 	 * Insert all words in WORDS into unknownWordTable. Insert those formed by
 	 * non words characters into wordPOSTable
 	 * 
 	 * @param WORDS
 	 * @return
 	 */
 	public int populateUnknownWordsTable(Map<String, Integer> WORDS) {
 		int count = 0;
 
 		Iterator<String> iter = WORDS.keySet().iterator();
 
 		while (iter.hasNext()) {
 			String word = iter.next();
 			if ((!word.matches("^.*\\w.*$")) || (word.matches("^.*ous$"))) {
 				this.myDataHolder.addUnknown(word, word);
 				this.updateTable(word, "b", "", "wordpos", 1);
 			} else {
 				this.myDataHolder.addUnknown(word, "unknown");
 			}
 			count++;
 		}
 
 		return count;
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
 	public int updateTable(String word, String pos, String role, String table,
 			int increment) {
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
 			pos = myWordFormUtility.getNumber(word);
 		}
 
 //		if (word.equals("mesodentine")) {
 //			System.out.println(word);
 //		}
 		result = result + markKnown(word, pos, role, table, increment);
 
 		// 1) if the word is a singular form n word, find its plural form, then add
 		// the plural form, and add the singular - pluarl pair into
 		// singularPluarlTable;
 		// 2) if the word is a plural form n word, find its singular form, then add
 		// the singular form, and add the singular - pluarl pair into
 		// singularPluarlTable;
 		if (!this.myDataHolder.isInSingularPluralPair(word)) {
 			if (pos.equals("p")) {
 				String pl = word;
 				word = myWordFormUtility.getSingular(word);
 				// add "*" and 0: pos for those words are inferred based on
 				// other clues, not seen directly from the text
 				result = result + this.markKnown(word, "s", "*", table, 0);
 				this.myDataHolder.addSingularPluralPair(word, pl);
 			}
 			if (pos.equals("s")) {
 				List<String> words = myWordFormUtility.getPlural(word);
 				String sg = word;
 				for (int i = 0; i < words.size(); i++) {
 					if (words.get(i).matches("^.*\\w.*$")) {
 						result = result
 								+ this.markKnown(words.get(i), "p", "*", table,
 										0);
 					}
 					this.myDataHolder.addSingularPluralPair(sg, words.get(i));
 				}
 			}
 		}
 
 		return result;
 	}
 	
 	/**
 	 * mark a word an its pos and role
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
 
 		boolean markknown_debug=true;
 		
 		String sth = "";
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
 
 		sign = sign + processNewWord(word, pos, role, table, word, increment);
 
 //		System.out.println(word);
 //		if (word.equals("mesodentine")){
 //			System.out.println("Find One!");
 //		}
 		
 		// case 1: have benn tested by Dongye
 		Pattern p = Pattern.compile("^(" + Constant.PREFIX + ")(\\S+).*$");
 		Matcher m = p.matcher(word);
 		if (m.lookingAt()) {
 			String g1 = m.group(1); // the prefix
 			String g2 = m.group(2); // the remaining
 			// String temp = g2;
 
 			// remove g1 from the prefix
 			// otherPrefix = Constant.PREFIX;
 			// otherPrefix = otherPrefix.replace("\\b" + g1 + "\\b", "");
 			// otherPrefix = otherPrefix.replace("\\|\\|", "|");
 			// otherPrefix = otherPrefix.replace("^\\|", "");
 
 			otherPrefix = StringUtility.removeFromWordList(g1, Constant.PREFIX);
 
 			spWords = "("
 					+ StringUtility.escape(singularPluralVariations(g2,
 							this.myDataHolder.singularPluralTable)) + ")";
 			pattern = "^(" + otherPrefix + ")?" + spWords + "$";
 
 			Iterator<Map.Entry<String, String>> iter1 = this.myDataHolder.unknownWordTable
 					.entrySet().iterator();
 			// if
 			// (this.myDataHolder.unknownWordTable.containsKey("semidentine")) {
 			// System.out.println(this.myDataHolder.unknownWordTable.get("semidentine"));
 			// }
 
 			// case 1
 			while (iter1.hasNext()) {
 				Map.Entry<String, String> entry = iter1.next();
 				String newWord = entry.getKey();
 				String flag = entry.getValue();
 
 				// if (newWord.equals("semidentine")){
 				// System.out.println("Find Two!");
 				// }
 
 				if ((newWord.matches(pattern)) && (flag.equals("unknown"))) {
 					sign = sign
 							+ processNewWord(newWord, pos, "*", table, word, 0);
 
 					if (markknown_debug) {
 						System.out.print("case 1");
 						System.out.println("by removing prefix of" + word
 								+ ", know " + newWord + " is a [" + pos + "]");
 					}
 				}
 			}
 		}
 //		if (word.equals("pair")) {
 //			System.out.println();
 //		}
 		// word starts with a lower case letter
 		if (word.matches("^[a-z].*$")) {
 			spWords = "("
 					+ StringUtility.escape(singularPluralVariations(word,
 							this.myDataHolder.singularPluralTable)) + ")";
 			// word=shrubs, pattern = (pre|sub)shrubs
 			pattern = "^(" + Constant.PREFIX + ")" + spWords + "$";
 
 			Iterator<Map.Entry<String, String>> iter2 = this.myDataHolder.unknownWordTable
 					.entrySet().iterator();
 
 			// case 2
 			while (iter2.hasNext()) {
 				Map.Entry<String, String> entry = iter2.next();
 				String newWord = entry.getKey();
 //				if (newWord.equals("semicircular")) {
 //					System.out.println();
 //				}
 				String flag = entry.getValue();
 				if ((newWord.matches(pattern)) && (flag.equals("unknown"))) {
 					sign = sign
 							+ processNewWord(newWord, pos, "*", table, word, 0);
 					
 					if (markknown_debug) {
 						System.out.print("case 2");
 						System.out.println("by removing prefix of" + word
 								+ ", know " + newWord + " is a [" + pos + "]");
 					}
 				
 				}
 			}
 
 			// case 3: word_$spwords
 			spWords = "("
 					+ StringUtility.escape(singularPluralVariations(word,
 							this.myDataHolder.singularPluralTable)) + ")";
 			pattern = "^.*_" + spWords + "$";
 			Iterator<Map.Entry<String, String>> iter3 = this.myDataHolder.unknownWordTable
 					.entrySet().iterator();
 			while (iter3.hasNext()) {
 				Map.Entry<String, String> entry = iter3.next();
 				String newWord = entry.getKey();
 				String flag = entry.getValue();
 				if ((newWord.matches(pattern)) && (flag.equals("unknown"))) {
 					sign = sign
 							+ processNewWord(newWord, pos, "*", table, word, 0);
 					
 					if (markknown_debug) {
 						System.out.print("case 3");
 						System.out.println("by removing prefix of" + word
 								+ ", know " + newWord + " is a [" + pos + "]");
 					}
 				}
 			}
 		}
 
 		return sign;
 	}
 
 	
 	/**
 	 * This method handles a new word when for updateTable method
 	 * 
 	 * @param newWord
 	 * @param pos
 	 * @param role
 	 * @param table
 	 * @param flag
 	 * @param increment
 	 * @return
 	 */
 	public int processNewWord(String newWord, String pos, String role,
 			String table, String flag, int increment) {
 		
 //		if (newWord.equals("semidentine"))
 //			System.out.println("update");
 		
 		
 		int sign = 0;
 		// remove $newword from unknownwords
 		updateUnknownWords(newWord, flag);
 		// insert $newword to the specified table
 		if (table.equals("wordpos")) {
 			sign = sign + updatePOS(newWord, pos, role, increment);
 		} else if (table.equals("modifiers")) {
 			sign = sign + this.myDataHolder.addModifier(newWord, increment);
 		}
 
 		return sign;
 	}
 	
 	/**
 	 * return singular and plural variations of the word
 	 * 
 	 * @param word
 	 * @return all variations of the word
 	 */
 	public String singularPluralVariations(String word, Set<SingularPluralPair> singularPluralTable) {
 		String variations = word + "|";
 		Iterator<SingularPluralPair> iter = singularPluralTable.iterator();
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
 	 * This method updates a new word in the unknownWord table
 	 * 
 	 * @param newWord
 	 * @param sourceWord
 	 * @return if any updates occured, return true; otherwise, return false
 	 */
 	public boolean updateUnknownWords(String newWord, String flag) {
 		boolean result = false;
 		Iterator<Map.Entry<String, String>> iter = this.myDataHolder.unknownWordTable
 				.entrySet().iterator();
 
 		while (iter.hasNext()) {
 			Map.Entry<String, String> unknownWord = iter.next();
 			if (unknownWord.getKey().equals(newWord)) {
 				unknownWord.setValue(flag);
 				result = true;
 			}
 		}
 
 		return result;
 	}
 	
 	/**
 	 * 
 	 * @param newWord
 	 * @param pos
 	 * @param role
 	 * @param increment
 	 * @return
 	 */
 	public int updatePOS(String newWord, String pos, String role, int increment) {
 		int n = 0;
 
 		if ((newWord.matches("^.*(\b|_)(NUM|" + Constant.NUMBER + "|"
 				+ Constant.CLUSTERSTRING + "|" + Constant.CHARACTER + ")\b.*$"))
 				&& (pos.matches("[nsp]"))) {
 			return 0;
 		}
 
 		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter = this.myDataHolder.wordPOSTable
 				.entrySet().iterator();
 		// boolean isExist = false;
 		Map.Entry<WordPOSKey, WordPOSValue> targetWordPOS = null;
 		while (iter.hasNext()) {
 			Map.Entry<WordPOSKey, WordPOSValue> wordPOS = iter.next();
 			if (wordPOS.getKey().getWord().equals(newWord)) {
 				targetWordPOS = wordPOS;
 				break;
 			}
 		}
 		if (targetWordPOS == null) {
 			int certaintyU = 0;
 			certaintyU += increment;
 			this.myDataHolder.wordPOSTable.put(new WordPOSKey(newWord, pos),
 					new WordPOSValue(role, certaintyU, 0, null, null));
 			n = 1;
 		} else {
 			String oldPOS = targetWordPOS.getKey().getPOS();
 			String oldRole = targetWordPOS.getValue().getRole();
 			int certaintyU = targetWordPOS.getValue().getCertaintyU();
 			if ((!oldPOS.equals(pos))
 					&& ((oldPOS.equals("b")) || (pos.equals("b")))) {
 				String otherPOS = pos.equals("b") ? oldPOS : pos;
 				pos = resolveConflicts(newWord, "b", otherPOS);
 
 				boolean flag = false;
 				if (pos != null) {
 					if (!pos.equals(oldPOS)) {
 						flag = true;
 					}
 				}
 
 				if (flag) { // new pos win
 					role = role.equals("*") ? "" : role;
 					n = n + changePOS(newWord, oldPOS, pos, role, increment);
 				} else { // olde pos win
 					role = oldRole.equals("*") ? role : oldRole;
 					certaintyU = certaintyU + increment;
 					WordPOSKey key = new WordPOSKey("newWord", "pos");
 					WordPOSValue value = new WordPOSValue(role, certaintyU, 0,
 							null, null);
 					this.myDataHolder.wordPOSTable.put(key, value);
 				}
 			} else {
 				role = mergeRole(oldRole, role);
 				certaintyU += increment;
 				WordPOSKey key = new WordPOSKey("newWord", "pos");
 				WordPOSValue value = new WordPOSValue(role, certaintyU, 0,
 						null, null);
 				this.myDataHolder.wordPOSTable.put(key, value);
 			}
 		}
 
 		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter2 = this.myDataHolder.wordPOSTable
 				.entrySet().iterator();
 		int certaintyL = 0;
 		while (iter2.hasNext()) {
 			Map.Entry<WordPOSKey, WordPOSValue> e = iter2.next();
 			if (e.getKey().getWord().equals(newWord)) {
 				certaintyL += e.getValue().getCertaintyU();
 			}
 		}
 		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter3 = this.myDataHolder.wordPOSTable
 				.entrySet().iterator();
 		while (iter3.hasNext()) {
 			Map.Entry<WordPOSKey, WordPOSValue> e = iter3.next();
 			if (e.getKey().getWord().equals(newWord)) {
 				e.getValue().setCertiantyU(certaintyL);
 			}
 		}
 
 		return n;
 
 	}
 	
 	/**
 	 * 
 	 * @param newWord
 	 * @param bPOS
 	 * @param otherPOS
 	 * @return
 	 */
 	private String resolveConflicts(String newWord, String bPOS, String otherPOS) {
 		int count = 0;
 
 		for (int i = 0; i < this.myDataHolder.sentenceTable.size(); i++) {
 			Sentence sent = this.myDataHolder.sentenceTable.get(i);
 			if(sent.getTag()==null){
 				continue;
 			}
 			if (!sent.getTag().equals("ignore")) {
 				Pattern p = Pattern.compile("([a-z]+(" + Constant.PLENDINGS
 						+ ")) (" + newWord + ")", Pattern.CASE_INSENSITIVE);
 				Matcher m = p.matcher(newWord);
 				if (m.lookingAt()) {
 					String pl = m.group(1).toLowerCase();
 					if (myWordFormUtility.getNumber(pl).equals("p")) {
 
 						count++;
 					}
 					if (count >= 1) {
 						return bPOS;
 					}
 				}
 			}
 		}
 		
 		return null;
 	}
 
 	/**
 	 * This method corrects the pos of the word from N to M (establish newPOS)
 	 * 
 	 * @param newWord
 	 * @param oldPOS
 	 * @param pos
 	 * @param role
 	 * @param increment
 	 * @return
 	 */
 	public int changePOS(String newWord, String oldPOS, String newPOS,
 			String role, int increment) {
 		oldPOS = oldPOS.toLowerCase();
 		newPOS = newPOS.toLowerCase();
 
 		String modifier = "";
 		String tag = "";
 		String sentence = null;
 		int sign = 0;
 
 		// case 1: oldPOS is "s" AND newPOS is "m"
 		if (oldPOS.matches("^.*s.*$") && newPOS.matches("^.*m.*$")) {
 			discount(newWord, oldPOS, newPOS, "all");
 			sign += markKnown(newWord, "m", "", "modifiers", increment);
 			for (int i = 0; i < this.myDataHolder.sentenceTable.size(); i++) {
 				Sentence sent = this.myDataHolder.sentenceTable.get(i);
 				if (sent.getTag().equals(newWord)) {
 					modifier = sent.getModifier();
 					tag = sent.getTag();
 					sentence = sent.getSentence();
 					tag = getParentSentenceTag(i);
 					modifier = modifier + " " + newWord;
 					modifier.replaceAll("^\\s*", "");
 					List<String> pair = getMTFromParentTag(tag);
 					String m = pair.get(1);
 					tag = pair.get(2);
 					if (m.matches("^.*\\w.*$")) {
 						modifier = modifier + " " + m;
 					}
 					tagSentWithMT(i, sentence, modifier, tag,
 							"changePOS[n->m:parenttag]");
 				}
 			}
 			// case 2: oldPOS is "s" AND newPOS is "b"
 		} else if ((oldPOS.matches("^.*s.*$")) && (newPOS.matches("^.*b.*$"))) {
 			int certaintyU = 0;
 
 			// case 2.1: (newWord, oldPOS)
 			WordPOSKey newOldKey = new WordPOSKey(newWord, oldPOS);
 			if (this.myDataHolder.wordPOSTable.containsKey(newOldKey)) {
 				WordPOSValue v = this.myDataHolder.wordPOSTable.get(newOldKey);
 				certaintyU = v.getCertaintyU();
 				certaintyU += increment;
 				discount(newWord, oldPOS, newPOS, "all");
 			}
 
 			// case 2.2: (newWord, newPOS)
 			WordPOSKey newNewKey = new WordPOSKey(newWord, newPOS);
 			if (!this.myDataHolder.wordPOSTable.containsKey(newOldKey)) {
 				this.myDataHolder.wordPOSTable.put(newNewKey, new WordPOSValue(role,
 						certaintyU, 0, "", ""));
 			}
 			sign++;
 
 			// for all sentences tagged with (newWord, "b"), re tag them
 			for (int i = 0; i < this.myDataHolder.sentenceTable.size(); i++) {
 				Sentence sent = this.myDataHolder.sentenceTable.get(i);
 				if (sent.getTag().equals(newWord)) {
 					int sentID = i;
 					String s = sent.getSentence();
 					this.tagSentWithMT(sentID, s, "", "NULL",
 							"changePOS[s->b: reset to NULL]");
 				}
 			}
 		}
 		// case 3: oldPOS is "b" AND newPOS is "s"
 		else if (oldPOS.matches("^.*b.*$") && newPOS.matches("^.*s.*$")) {
 			int certaintyU = 0;
 
 			// case 3.1: (newWord, oldPOS)
 			WordPOSKey newOldKey = new WordPOSKey(newWord, oldPOS);
 			if (this.myDataHolder.wordPOSTable.containsKey(newOldKey)) {
 				WordPOSValue v = this.myDataHolder.wordPOSTable.get(newOldKey);
 				certaintyU = v.getCertaintyU();
 				certaintyU += increment;
 				discount(newWord, oldPOS, newPOS, "all");
 			}
 
 			// case 3.2: (newWord, newPOS)
 			WordPOSKey newNewKey = new WordPOSKey(newWord, newPOS);
 			if (!this.myDataHolder.wordPOSTable.containsKey(newOldKey)) {
 				this.myDataHolder.wordPOSTable.put(newNewKey, new WordPOSValue(role,
 						certaintyU, 0, "", ""));
 			}
 			sign++;
 		}
 
 		int sum_certaintyU = 0;
 		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter1 = this.myDataHolder.wordPOSTable
 				.entrySet().iterator();
 		while (iter1.hasNext()) {
 			Map.Entry<WordPOSKey, WordPOSValue> e = iter1.next();
 			if (e.getKey().getWord().equals(newWord)) {
 				sum_certaintyU += e.getValue().getCertaintyU();
 			}
 		}
 		if (sum_certaintyU > 0) {
 			Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter2 = this.myDataHolder.wordPOSTable
 					.entrySet().iterator();
 			while (iter2.hasNext()) {
 				Map.Entry<WordPOSKey, WordPOSValue> e = iter2.next();
 				if (e.getKey().getWord().equals(newWord)) {
 					e.getValue().setCertiantyL(sum_certaintyU);
 				}
 			}
 		}
 
 		return sign;
 	}
 	
 	/**
 	 * Given a new role, and the old role, of a word, decide the right role to
 	 * return
 	 * 
 	 * @param oldRole
 	 * @param role
 	 * @return
 	 */
 	public String mergeRole(String oldRole, String role) {
 		String role1 = oldRole;
 		String role2 = role;
 
 		// if old role is "*", return the new role
 		if (role1.equals("*")) {
 			return role2;
 		}
 		// if the new role is "*", return the old rule
 		else if (role2.equals("*")) {
 			return role1;
 		}
 
 		// if the old role is empty, return the new role
 		if (role1.equals("")) {
 			return role2;
 		}
 		// if the new role is empty, return the old role
 		else if (role2.equals("")) {
 			return role1;
 		}
 		// if the old role is not same as the new role, return "+"
 		else if (!role1.equals(role2)) {
 			return "+";
 		}
 		// if none of above apply, return the old role by default
 		else {
 			return role1;
 		}
 	}
 
 	/**
 	 * Discount existing pos, but do not establish $suggestedpos
 	 * 
 	 * @param newWord
 	 * @param oldPOS
 	 * @param newPOS
 	 * @param mode
 	 *            "byone" - reduce certainty 1 by 1. "all" - remove this POS
 	 */
 	public void discount(String newWord, String oldPOS, String newPOS,
 			String mode) {
 		/**
 		 * 1. Find the flag of newWord in unknownWords table
 		 * 1. Select all words from unknownWords table who have the same flag (including newWord)
 		 * 1. From wordPOS table, select certaintyU of the (word, oldPOS) where word is in the words list
 		 *     For each of them
 		 *     1.1 Case 1: certaintyu less than 1, AND mode is "all"
 		 *         1.1.1 Delete the entry from wordpos table
 		 *         1.1.1 Update unknownwords
 		 *             1.1.1.1 Case 1: the pos is "s" or "p"
 		 *                 Delete all entries contains word from singularplural table as well
 		 *         1.1.1 Insert (word, oldpos, newpos) into discounted table
 		 */
 
 		String flag = this.myDataHolder.unknownWordTable.get(newWord);
 		Iterator<Map.Entry<String, String>> iter = this.myDataHolder.unknownWordTable
 				.entrySet().iterator();
 		while (iter.hasNext()) {
 			Map.Entry<String, String> e = iter.next();
 			if (e.getValue().equals(flag)) {
 				String word = e.getKey();
 				WordPOSKey key = new WordPOSKey(word, oldPOS);
 				WordPOSValue value = this.myDataHolder.wordPOSTable.get(key);
 				int cU = value.getCertaintyU();
 				if (cU < 1 && mode.equals("all")) {
 					this.myDataHolder.wordPOSTable.remove(key);
 					this.updateUnknownWords(word, "unknown");
 					if (oldPOS.matches("^.*[sp].*$")) {
 						// list of entries to be deleted
 						ArrayList<SingularPluralPair> delList = new ArrayList<SingularPluralPair>();
 
 						// find entries to be deleted, put them into delList
 						Iterator<SingularPluralPair> iterSPTable = this.myDataHolder.singularPluralTable
 								.iterator();
 						while (iterSPTable.hasNext()) {
 							SingularPluralPair spp = iterSPTable.next();
 							if (spp.getSingular().equals(word)
 									|| spp.getPlural().equals(word)) {
 								delList.add(spp);
 							}
 						}
 
 						// delete all entries in delList from
 						// singularPluralTable
 						for (int i = 0; i < delList.size(); i++) {
 							this.myDataHolder.singularPluralTable.remove(delList.get(i));
 						}
 					}
 
 					DiscountedKey dKey = new DiscountedKey(word, oldPOS);
 					this.myDataHolder.discountedTable.put(dKey, newPOS);
 				}
 			}
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
 		 * 1. Get the originalsent of sentence sentID 
 		 * 1. Case 1: the originalsent of $sentence sentID starts with a [a-z\d] 
 		 * 1.1 select modifier and tag from sentenceTable where tag is not "ignore" 
 		 *     	OR tag is null 
 		 *      AND originalsent COLLATE utf8_bin regexp '^[A-Z].*' 
 		 *      OR originalsent rlike ': *\$' AND id < sentID 
 		 * 1.1 take the tag of the first sentence (with smallest id), get its modifier and tag 
 		 * 1.1 if modifier match \w, tag = modifier + space + tag 
 		 * 1.1 remove [ and ] from tag 
 		 * 1. if tag matches \w return [+tag+], else return [parenttag]
 		 */
 
 		String tag = "";
 
 		String originalSent = this.myDataHolder.sentenceTable.get(sentID)
 				.getOriginalSentence();
 		if (originalSent.matches("^\\s*[^A-Z].*$")) {
 		//if (originalSent.matches("^\\s*([a-z]|\\d).*$")) {
 			for (int i = 0; i < this.myDataHolder.sentenceTable.size(); i++) {
 				Sentence sent = this.myDataHolder.sentenceTable.get(i);
 				tag = sent.getTag();
 				if (((!tag.equals("ignore")) || (tag == null))
 						&& ((originalSent.matches("^[A-Z].*$")) || (originalSent
 								.matches("^.*:\\s*$"))) && (i < sentID)) {
 					String modifier = sent.getModifier();
 					if (modifier.matches("^.*\\w.*$")) {
 						tag = modifier + " " + tag;
 						tag.replaceAll("[\\[\\]]", "");
 					}
 					break;
 				}
 			}
 		}
 
 		return tag.matches("^.*\\w.*$") ? "[parenttag]" : "[" + tag + "]";
 	}
 	
 	/**
 	 * 
 	 * @param sentID
 	 * @param sentence
 	 * @param modifier
 	 * @param tag
 	 * @param label
 	 */
 	public void tagSentWithMT(int sentID, String sentence, String modifier,
 			String tag, String label) {
 
 		/**
 		 * 1. Do some preprocessing of modifier and tag 1. Remove -ly words 1.
 		 * Update modifier and tag of sentence sentID in sentenceTable
 		 */
 
 		modifier.replaceAll("<\\S+?>", "");
 		tag.replaceAll("<\\S+?>", "");
 
 		// remove stop and forbidden words from beginning
 		modifier = StringUtility.removeAll(modifier, "\\s*\\b(" + Constant.STOP
 				+ "|" + Constant.FORBIDDEN + "|\\w+ly)$");
 		tag = StringUtility.removeAll(tag, "\\s*\\b(" + Constant.STOP + "|"
 				+ Constant.FORBIDDEN + "|\\w+ly)$");
 
 		// remove stop and forbidden words from ending
 		modifier = StringUtility.removeAll(modifier, "\\s*\\b(" + Constant.STOP
 				+ "|" + Constant.FORBIDDEN + "|\\w+ly)$");
 		tag = StringUtility.removeAll(tag, "\\s*\\b(" + Constant.STOP + "|"
 				+ Constant.FORBIDDEN + "|\\w+ly)$");
 
 		// remove all pronoun words
 		modifier = StringUtility.removeAll(modifier, "\\b(" + Constant.PRONOUN
 				+ ")\\b");
 
 		Pattern p = Pattern.compile("^(\\w+ly)\\s*(.*)$");
 		Matcher m = p.matcher(modifier);
 		while (m.lookingAt()) {
 			String ly = m.group(1);
 			String rest = m.group(2);
 			WordPOSKey wp = new WordPOSKey(ly, "b");
 			if (this.myDataHolder.wordPOSTable.containsKey(wp)) {
 				modifier = rest;
 				m = p.matcher(modifier);
 			} else {
 				break;
 			}
 		}
 
 		modifier = StringUtility.removeAll(modifier, "(^\\s*|\\s*$)");
 		tag = StringUtility.removeAll(tag, "(^\\s*|\\s*$)");
 
 		if (tag != null) {
 			if (tag.length() > this.tagLength) {
 				tag = tag.substring(0, this.tagLength);
 			}
 		}
 
 		for (int i = 0; i < this.myDataHolder.sentenceTable.size(); i++) {
 			Sentence sent = this.myDataHolder.sentenceTable.get(i);
 		}
 
 		Sentence sent = this.myDataHolder.sentenceTable.get(sentID);
 		sent.setTag(tag);
 		sent.setModifier(modifier);
 	}
 
 	/**
 	 * 
 	 * @param tag
 	 * @return
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
 
 	public void addHeuristicsNouns() {
 		if (this.addHeuristicsNouns_debug)
 			System.out.println("Enter addHeuristicsNouns:\n");
 
 		// part 1
 		List<Set<String>> results = this.characterHeuristics();
 		Set<String> rnouns = results.get(0);
 		Set<String> descriptors = results.get(1);
 		
 		addDescriptors(descriptors);
 		addNouns(rnouns);
 		
 		// part 2
 		Set<String> nouns = this.learnHeuristicsNouns();
 		Iterator<String> iter = nouns.iterator();
 		while (iter.hasNext()) {
 			String e = iter.next();
 			if ((e.matches("^.*\\w.*$"))
 					&& (!StringUtility.isMatchedWords(e, "NUM|" + Constant.NUMBER
 							+ "|" + Constant.CLUSTERSTRING + "|"
 							+ Constant.CHARACTER + "|" + Constant.PROPERNOUN))) {
 				// same word may have two different pos tags
 				String[] nounArray = e.split("\\|");
 				for (int i = 0; i < nounArray.length; i++) {
 					String nounAndPOS = nounArray[i];
 					Pattern p = Pattern.compile("(\\w+)\\[([spn])\\]");
 					Matcher m = p.matcher(nounAndPOS);
 					if (m.lookingAt()) {
 						String word = m.group(1);
 						String pos = m.group(2);
 						this.updateTable(word, pos, "*", "wordpos", 0);
 
 						if (pos.equals("p")) {
 							String plural = word;
 							String singular = this.myWordFormUtility
 									.getSingular(plural);
 							if (singular != null) {
 								if (!singular.equals("")) {
 									this.myDataHolder.addSingularPluralPair(singular, plural);
 								}
 							}
 						}
 
 						if (pos.equals("s")) {
 							String singular = word;
 							List<String> pluralList = this.myWordFormUtility
 									.getPlural(singular);
 							iter = pluralList.iterator();
 							while (iter.hasNext()) {
 								String plural = iter.next();
 								if (plural != null) {
 									if (!plural.equals("")) {
 										this.myDataHolder.addSingularPluralPair(singular, plural);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 
 	
 	/**
 	 * 
 	 * @param descriptors
 	 */
 	public void addDescriptors(Set<String> descriptors) {
 		Iterator<String> iter = descriptors.iterator();
 		while (iter.hasNext()) {
 			String descriptor = iter.next();
 //			if (descriptor.equals("circular")){
 //				System.out.println();
 //			}
 			if (!StringUtility.isMatchedWords(descriptor, Constant.FORBIDDEN)) {
 				this.updateTable(descriptor, "b", "", "wordpos", 1);
 			}
 		}
 		
 	}
 	
 	/**
 	 * 
 	 * @param rnouns
 	 */
 	public void addNouns(Set<String> rnouns) {
 		// TODO Auto-generated method stub
 		Iterator<String> iter = rnouns.iterator();
 		while (iter.hasNext()) {
 			String noun = iter.next();
 			if (!StringUtility.isMatchedWords(noun, Constant.FORBIDDEN)) {
 				this.updateTable(noun, "n", "", "wordpos", 1);
 			}
 		}
 	}
 
 
 
 	/**
 	 * 
 	 * @return nouns learned by heuristics
 	 */
 	public Set<String> learnHeuristicsNouns() {
 		// Set of words
 		Set<String> words = new HashSet<String>();
 
 		// Set of nouns
 		Set<String> nouns = new HashSet<String>();
 
 		List<String> sentences = new LinkedList<String>();
 		for (int i = 0; i < this.myDataHolder.sentenceTable.size(); i++) {
 			String originalSentence = this.myDataHolder.sentenceTable.get(i)
 					.getOriginalSentence();
 			if (this.getHeuristicNouns_debug) {
 				 System.out.println(originalSentence+"\n");
 			}
 			sentences.add(StringUtility.strip(originalSentence));
 		}
 
 		// Now we have original sentences in sentences
 		// Method addWords
 		for (int i = 0; i < sentences.size(); i++) {
 			String sentence = sentences.get(i);
 			sentence = sentence.toLowerCase();
 			String noun = this.getPresentAbsentNouns(sentence);
 			if (!noun.equals("")) {
 				nouns.add(noun);
 			}
 
 			// add words
 			String[] tokens = this.myConfiguration.getTokenizer().tokenize(sentence);
 			for (int j = 0; j < tokens.length; j++) {
 				String token = tokens[j];
 				if (StringUtility.isWord(token)) {
 					// if (token.equals("arch"))
 					// token="arch";
 					words.add(token);
 					if (this.getHeuristicNouns_debug) {
 						System.out.println("Add a word into words:");
 						System.out.println(token);
 					}
 				}
 			}
 		}
 
 		// solve the problem: septa and septum are both s
 		Iterator<String> nounsIterator = nouns.iterator();
 		while (nounsIterator.hasNext()) {
 			String oldNoun = nounsIterator.next();
 			String newNoun = this.getHeuristicsNounsHelper(oldNoun, nouns);
 			if (!newNoun.equals(oldNoun)) {
 				nouns.remove(oldNoun);
 				nouns.add(newNoun);
 			}
 		}
 
 		// sort all words
 		Map<String, Set<String>> wordMap = new HashMap<String, Set<String>>();
 		Iterator<String> wordsIterator = words.iterator();
 		while (wordsIterator.hasNext()) {
 			String word = wordsIterator.next();
 			String root = myWordFormUtility.getRoot(word);
 			if (wordMap.containsKey(root)) {
 				Set<String> wordList = wordMap.get(root);
 				wordList.add(word);
 				//List<String> wordList2 = wordMap.get(root);
 				//System.out.println(wordList2);
 			} else {
 				Set<String> wordList = new HashSet<String>();
 				wordList.add(word);
 				wordMap.put(root, wordList);
 			}
 		}
 
 		// print out the wordMap
 		if (getHeuristicNouns_debug) {
 			Iterator<Map.Entry<String, Set<String>>> iter = wordMap.entrySet().iterator();
 			while (iter.hasNext()){
 				Map.Entry<String, Set<String>> e = iter.next();
 				System.out.println(e.getKey());
 				System.out.println(e.getValue());
 			}
 		}
 		
 		// find nouns
 		Iterator<Map.Entry<String, Set<String>>> iter = wordMap.entrySet().iterator();
 		while (iter.hasNext()){
 			Map.Entry<String, Set<String>> e = iter.next();
 			Set<String> wordSet = e.getValue();
 			Iterator<String> wordIterator = wordSet.iterator();
 			while(wordIterator.hasNext()){
 				String word = wordIterator.next();
 				
 				// getnouns
 				if (word.matches("^.*" + Constant.NENDINGS)) {
 					nouns.add(word + "[s]");
 					if (wordSet.contains(word + "s")) {
 						nouns.add(word + "s" + "[p]");
 						this.myDataHolder.addSingularPluralPair(word, word+"s");						
 					}
 					if (wordSet.contains(word + "es")) {
 						nouns.add(word + "es" + "[p]");
 						this.myDataHolder.addSingularPluralPair(word, word+"es");
 					}
 				}
 			}
 		}
 
 		// Iterator<LinkedList> wordMapIterator = wordMap.i
 		Iterator<Map.Entry<String, Set<String>>> wordMapIterator = wordMap
 				.entrySet().iterator();
 		while (wordMapIterator.hasNext()) {
 			Map.Entry<String, Set<String>> wordMapEntry = wordMapIterator
 					.next();
 			Set<String> wordSet = wordMapEntry.getValue();
 
 			// check if there is a word with Vending
 			boolean hasVending = false;
 			//for (int i1 = 0; i1 < wordList.size(); i1++) {
 			Iterator<String> wordIterator = wordSet.iterator();
 			while(wordIterator.hasNext()){
 				String tempWord =wordIterator.next(); 
 				if (tempWord.matches("^.*" + Constant.VENDINGS)) {
 					hasVending = true;
 					break;
 				}
 			}
 
 			// at least two words without verb endings
 			if ((!hasVending) && (wordSet.size() > 1)) {
 				List<String> wordList = new LinkedList<String>(wordSet);
 				for (int i = 0; i < wordList.size(); i++) {
 					for (int j = i + 1; j < wordList.size(); j++) {
 						String word1 = wordList.get(i);
 						String word2 = wordList.get(j);
 						List<String> pair = myWordFormUtility.getSingularPluralPair(word1, word2);
 						if (pair.size() == 2) {
 							String singular = pair.get(0);
 							String plural = pair.get(1);
 							nouns.add(singular + "[s]");
 							nouns.add(plural + "[p]");
 							this.myDataHolder.addSingularPluralPair(singular, plural);
 						}
 					}
 				}
 			}
 		}
 		
 		//print out nouns
 		if (this.getHeuristicNouns_debug) {
 			System.out.println("Nouns:\n");
 			System.out.println(nouns);
 		}
 				
 		return nouns;
 	}
 	
 	// ---------------addHeuristicsNouns Help Function----
 	// #solve the problem: septa and septum are both s
 	// septum - Singular
 	// septa -Plural
 	// septa[s] => septa[p]
 	public String getHeuristicsNounsHelper(String oldNoun, Set<String> words) {
 		String newNoun = oldNoun;
 
 		if (oldNoun.matches("^.*a\\[s\\]$")) {
 			String noun = oldNoun.replaceAll("\\[s\\]", "");
 			System.out.println(noun);
 			if (words.contains(noun)) {
 				newNoun = noun + "[p]";
 			}
 		}
 
 		return newNoun;
 	}
 
 	/**
 	 * any word preceeding "present"/"absent" would be a n
 	 * 
 	 * @param text
 	 *            the content to learn from
 	 * @return nouns learned
 	 */
 	public String getPresentAbsentNouns(String text) {
 		String pachecked = "and|or|to";
 
 		if (text.matches("(\\w+?)\\s+(present|absent)")) {
 			System.out.println(text);
 		}
 
 		Matcher matcher = Pattern.compile("^.*?(\\w+?)\\s+(present|absent).*$")
 				.matcher(text);
 		if (matcher.lookingAt()) {
 			String word = matcher.group(1);
 			if ((!word.matches("\\b(" + pachecked + ")\\b"))
 					&& (!word.matches("\\b(" + Constant.STOP + ")\\b"))
 					&& (!word
 							.matches("\\b(always|often|seldom|sometimes|[a-z]+ly)\\b"))) {
 				if (this.getHeuristicNouns_debug)
 					System.out.println("present/absent " + word + "\n");
 
 				if (((word.matches("^.*" + Constant.PENDINGS))
 						|| (word.matches("^.*[^s]s$")) || (word
 							.matches("teeth")))
 						&& (!word.matches(Constant.SENDINGS))) {
 					return word + "[p]";
 				} else {
 					return word + "[s]";
 				}
 			}
 		}
 
 		return "";
 	}
 
 	/**
 	 * Discover nouns and descriptors according to a set of rules
 	 * 
 	 * @return a linked list, whose first element is a set of nouns, and second
 	 *         element is a set of descriptors
 	 */
 	public List<Set<String>> characterHeuristics() {
 		
 		Set<String> taxonNames = new HashSet<String>();
 		Set<String> nouns = new HashSet<String>();
 		Set<String> anouns = new HashSet<String>();
 		Set<String> pnouns = new HashSet<String>();
 		Set<String> descriptors = new HashSet<String>();
 		Map<String, Boolean> descriptorMap = new HashMap<String, Boolean>();
 
 		int sent_num = this.myDataHolder.sentenceTable.size();
 		for (int i = 0; i < sent_num; i++) {
 
 			// taxon rule
 			Sentence sent = this.myDataHolder.sentenceTable.get(i);
 			String source = sent.getSource();
 			String sentence = sent.getSentence();
 			String originalSentence = sent.getOriginalSentence();
 			
 //			if (originalSentence.equals("mesodentine")){
 //				System.out.println("oSent:");
 //				System.out.println(originalSentence);
 //			}
 
 			if (this.characterHeuristics_debug) {
 				System.out.println(source);
 				System.out.println(sentence);
 				System.out.println(originalSentence + "\n");
 			}
 
 			originalSentence = StringUtility.trimString(originalSentence);
 
 			// noun rule 0: taxon names
 			taxonNames = this.getTaxonNameNouns(originalSentence);
 			
 			//$sentence =~ s#<\s*/?\s*i\s*>##g;
 			//$originalsent =~ s#<\s*/?\s*i\s*>##g;
 			
 			sentence = sentence.replaceAll("<\\s*/?\\s*i\\s*>", "");
 			originalSentence = originalSentence.replaceAll("<\\s*/?\\s*i\\s*>",
 					"");
 			// Update sentenceTable
 			this.myDataHolder.sentenceTable.get(i).setSentence(sentence);
 
 			// noun rule 0.5: Meckle#s cartilage
 
 			Set<String> nouns0 = this
 					.getNounsMecklesCartilage(originalSentence);
 			nouns.addAll(nouns0);
 			sentence = sentence.replaceAll("#", "");
 			// Update sentenceTable
 			this.myDataHolder.sentenceTable.get(i).setSentence(sentence);
 
 			// noun rule 2: end of sentence nouns
 			// (a|an|the|some|any|this|that|those|these) noun$
 			Set<String> nouns2 = this.getNounsRule2(originalSentence);
 			nouns.addAll(nouns2);
 
 			// noun rule 3: proper nouns and acronyms
 			String copy = originalSentence;
 			Set<String> nouns_temp = this.getNounsRule3Helper(copy);
 			Iterator<String> iter = nouns_temp.iterator();
 			while (iter.hasNext()) {
 				String token = iter.next();
 				if (token.matches("^.*[A-Z].+$")
 						&& (!token.matches("^.*-\\w+ed$"))) {
 					if (token.matches("^[A-Z0-9]+$")) {
 						token = token.toLowerCase();
 						anouns.add(token);
 					} else {
 						token = token.toLowerCase();
 						pnouns.add(token);
 					}
 					nouns.add(token);
 				}
 			}
 
 			// noun rule 1: sources with 1 _ are character statements, 2 _ are
 			// descriptions
 			Set<String> nouns1 = getNounsRule1(source, originalSentence,
 					descriptorMap);
 			nouns.addAll(nouns1);
 
 			// noun rule 4: non-stop/prep followed by a number: epibranchial 4
 			// descriptor heuristics
 			Set<String> nouns4 = this.getNounsRule4(originalSentence);
 			nouns.addAll(nouns4);
 
 			// remove puncts for descriptor rules
 			originalSentence = StringUtility.removePunctuation(
 					originalSentence, "-");
 //			System.out.println("oSent:");
 //			System.out.println(originalSentence);
 //			if (originalSentence.equals("mesodentine")){
 //				System.out.println("oSent:");
 //				System.out.println(originalSentence);
 //			}
 			
 //			if (originalSentence.equals("Body scale profile")) {
 //				System.out.println("Body scale profile");
 //			}
 			// Descriptor rule 1: single term descriptions are descriptors
 			descriptors.addAll(this.getDescriptorsRule1(source,
 					originalSentence, nouns));
 
 			// Descriptor rule 2: (is|are) red: isDescriptor
 			descriptors.addAll(this.getDescriptorsRule2(originalSentence,
 					descriptorMap));
 		}
 
 		nouns = this.filterOutDescriptors(nouns, descriptors);
 		anouns = this.filterOutDescriptors(anouns, descriptors);
 		pnouns = this.filterOutDescriptors(pnouns, descriptors);
 
 		this.add2HeuristicNounTable(nouns, "organ");
 		this.add2HeuristicNounTable(anouns, "acronyms");
 		this.add2HeuristicNounTable(pnouns, "propernouns");
 		this.add2HeuristicNounTable(taxonNames, "taxonnames");
 
 		nouns.addAll(anouns);
 		nouns.addAll(pnouns);
 		nouns.addAll(taxonNames);
 
 		List<Set<String>> results = new LinkedList<Set<String>>();
 		results.add(nouns);
 		results.add(descriptors);
 
 		return results;
 	}
 
 	/**
 	 * Add the terms into the heuristicNounTable with the type specified
 	 * 
 	 * @param terms
 	 *            set of terms
 	 * @param type
 	 *            type of the terms
 	 * @return number of the terms that have been added
 	 */
 	public int add2HeuristicNounTable(Set<String> terms, String type) {
 		int count = 0;
 
 		Iterator<String> iter = terms.iterator();
 		while (iter.hasNext()) {
 			String term = iter.next();
 			this.myDataHolder.heuristicNounTable.put(term, type);
 			count++;
 		}
 
 		return count;
 	}
 
 	/**
 	 * filter out descriptors from nouns, and return remaining nouns
 	 * 
 	 * @param rNouns
 	 *            set of nouns
 	 * @param rDescriptors
 	 *            set of descriptors
 	 * @return set of nouns that are not descriptors
 	 */
 	Set<String> filterOutDescriptors(Set<String> rNouns,
 			Set<String> rDescriptors) {
 		Set<String> filtedNouns = new HashSet<String>();
 
 		Iterator<String> iter = rNouns.iterator();
 		while (iter.hasNext()) {
 			String noun = iter.next();
 			noun = noun.toLowerCase();
 
 			Pattern p = Pattern.compile("\\b(" + Constant.PREPOSITION + "|"
 					+ Constant.STOP + ")\\b", Pattern.CASE_INSENSITIVE);
 			Matcher m = p.matcher(noun);
 
 			if ((!m.lookingAt()) && (!rDescriptors.contains(noun))) {
 				filtedNouns.add(noun);
 			}
 		}
 		return filtedNouns;
 	}
 
 	/**
 	 * Nouns rule 0: get <i></i> enclosed taxon names
 	 * 
 	 * @param oSent
 	 * @return
 	 */
 	public Set<String> getTaxonNameNouns(String oSent) {
 		Set<String> taxonNames = new HashSet<String>();
 		String regex = "(.*?)<\\s*i\\s*>\\s*([^<]*)\\s*<\\s*\\/\\s*i\\s*>(.*)";
 		String copy = oSent;
 
 		while (true) {
 			Matcher matcher = Pattern.compile(regex).matcher(copy);
 			if (matcher.lookingAt()) {
 				String taxonName = matcher.group(2);
 				if (taxonName.length() > 0) {
 					taxonNames.add(taxonName);
 					String[] taxonNameArray = taxonName.split("\\s+");
 					for (int i = 0; i < taxonNameArray.length; i++) {
 						taxonNames.add(taxonNameArray[i]);
 					}
 					copy = matcher.group(3);
 				} else {
 					break;
 				}
 			} else {
 				break;
 			}
 		}
 
 		return taxonNames;
 	}
 
 	/**
 	 * Nouns rule 0.5: Meckle#s cartilage
 	 * 
 	 * @param oSent
 	 * @return
 	 */
 	public Set<String> getNounsMecklesCartilage(String oSent) {
 		Set<String> nouns = new HashSet<String>();
 		String regex = "^.*\\b(\\w+#s)\\b.*$";
 		Matcher m = Pattern.compile(regex).matcher(oSent);
 		if (m.lookingAt()) {
 			String noun = "";
 			noun = m.group(1);
 
 			noun = noun.toLowerCase();
 			nouns.add(noun);
 
 			noun = noun.replaceAll("#", "");
 			nouns.add(noun);
 
 			noun = noun.replaceAll("s$", "");
 			nouns.add(noun);
 		}
 
 		return nouns;
 	}
 
 	/**
 	 * 
 	 * @param source
 	 * @param originalSentence
 	 * @param descriptorMap
 	 * @return
 	 */
 	public Set<String> getNounsRule1(String source, String originalSentence,
 			Map<String, Boolean> descriptorMap) {
 		Set<String> nouns = new HashSet<String>();
 
 		if ((!(source.matches("^.*\\.xml_\\S+_.*$")))
 				&& (!(originalSentence.matches("^.*\\s.*$")))) {
 			if (!this.isDescriptor(originalSentence, descriptorMap)) {
 				originalSentence = originalSentence.toLowerCase();
 				nouns.add(originalSentence);
 			}
 		}
 
 		return nouns;
 	}
 
 	/**
 	 * 
 	 * @param oSent
 	 * @return
 	 */
 	public Set<String> getNounsRule2(String oSent) {
 		String copy = oSent;
 		String regex = "(.*?)\\b(a|an|the|some|any|this|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth) +(\\w+)\\s*($|\\(|\\[|\\{|\\b"
 				+ Constant.PREPOSITION + "\\b)(.*)";
 		Set<String> nouns = new HashSet<String>();
 
 		while (true) {
 			if (copy == null) {
 				break;
 			}
 			Matcher m = Pattern.compile(regex).matcher(copy);
 			if (m.lookingAt()) {
 				String t = m.group(3);
 				String prep = m.group(4);
 				copy = m.group(5);
 
 				if (prep.matches("^.*\\w.*$")
 						&& t.matches("^.*\\b(length|width|presence|\\w+tion)\\b.*$")) {
 					continue;
 				}
 				t = t.toLowerCase();
 				nouns.add(t);
 			} else {
 				break;
 			}
 		}
 
 		return nouns;
 	}
 
 	/**
 	 * 
 	 * @param sentence
 	 * @return
 	 */
 	public Set<String> getNounsRule3Helper(String sentence) {
 		Set<String> nouns = new HashSet<String>();
 
 		String[] segs = sentence.split("[()\\[\\]\\{\\}]");
 		for (int i1 = 0; i1 < segs.length; i1++) {
 			String seg = segs[i1];
 			seg = StringUtility.removePunctuation(seg, "-");
 			String[] tokens = seg.split("\\s+");
 
 			// #ignore the first word in character statements--this is normally
 			// capitalized
 			for (int j = 1; j < tokens.length; j++) {
 				String token = tokens[j];
 				if (token.matches("^.*[A-Z].+$")
 						&& (!token.matches("^.*-\\w+ed$"))) {
 					nouns.add(token);
 				}
 			}
 		}
 
 		return nouns;
 	}
 
 	/**
 	 * noun rule 4: non-stop/prep followed by a number: epibranchial 4
 	 * descriptor heuristics
 	 * 
 	 * @param oSent
 	 * @return a set of nouns
 	 */
 	public Set<String> getNounsRule4(String oSent) {
 		Set<String> nouns = new HashSet<String>();
 
 		String copy = oSent;
 		String regex = "(.*?)\\s(\\w+)\\s+\\d+(.*)";
 
 		while (true) {
 			if (copy == null) {
 				break;
 			}
 			Matcher m = Pattern.compile(regex).matcher(copy);
 			if (m.lookingAt()) {
 				String t = m.group(2);
 				copy = m.group(3);
 				String regex2 = "\\b(" + Constant.PREPOSITION + "|"
 						+ Constant.STOP + ")\\b";
 				if (!t.matches(regex2)) {
 					t = t.toLowerCase();
 					nouns.add(t);
 				}
 			} else {
 				break;
 			}
 		}
 
 		return nouns;
 	}
 
 	/**
 	 * 
 	 * @param source
 	 * @param sentence
 	 * @param nouns
 	 * @return
 	 */
 	public Set<String> getDescriptorsRule1(String source, String sentence,
 			Set<String> nouns) {
 		Set<String> descriptors = new HashSet<String>();
 		// single word
 		if (source.matches("^.*\\.xml_\\S+_.*$") && (!sentence.matches("^.*\\s.*$"))) {
 			Iterator<String> iter = nouns.iterator();
 			boolean isExist = false;
 			while (iter.hasNext()) {
 				String noun = iter.next();
 				if (noun.equals(sentence)) {
 					isExist = true;
 					break;
 				}
 			}
 			if (isExist == false) {
 				sentence = sentence.toLowerCase();
 				descriptors.add(sentence);
 			}
 		}
 
 		return descriptors;
 	}
 
 	/**
 	 * (is|are) red: isDescriptor
 	 * 
 	 * @param oSent
 	 * @return
 	 */
 	public Set<String> getDescriptorsRule2(String sentence,
 			Map<String, Boolean> descriptorMap) {
 		Set<String> descriptors = new HashSet<String>();
 
 		String[] tokens = sentence.split("\\s+");
 
 		for (int i = 0; i < tokens.length; i++) {
 			String token = tokens[i];
 			token = token.toLowerCase();
 			if (isDescriptor(token, descriptorMap)) {
 				token = token.toLowerCase();
 				descriptors.add(token);
 			}
 		}
 
 		return descriptors;
 	}
 
 	/**
 	 * Check if the term is a descriptor
 	 * 
 	 * @param term
 	 * @param descriptorMap
 	 *            descriptors have already learned
 	 * @return a boolean value indicating whether the term is a descriptor. This
 	 *         result will be stored in the descriptorMap for future use
 	 */
 	public boolean isDescriptor(String term, Map<String, Boolean> descriptorMap) {
 		if (descriptorMap.containsKey(term)) {
 			if (descriptorMap.get(term).booleanValue()) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			for (int i = 0; i < this.myDataHolder.sentenceTable.size(); i++) {
 				String originalSentence = this.myDataHolder.sentenceTable.get(i)
 						.getOriginalSentence();
 				if (isMatched(originalSentence, term, descriptorMap)) {
 					return true;
 				}
 			}
 			term = term.toLowerCase();
 			descriptorMap.put(term, false);
 			return false;
 		}
 
 	}
 
 	/**
 	 * Check if the term matches the sentence
 	 * 
 	 * @param sentence
 	 * @param term
 	 * @param descriptorMap
 	 * @return a boolean value indicating whether the term matches the sentence
 	 */
 	public boolean isMatched(String sentence, String term,
 			Map<String, Boolean> descriptorMap) {
 		if (sentence.matches("^.*" + " (is|are|was|were|be|being) " + term
 				+ ".*$")) {
 			term = term.toLowerCase();
 			descriptorMap.put(term, true);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public void addStopWords() {
 		List<String> stops = new ArrayList<String>();
 		stops.addAll(Arrays.asList(Constant.STOP.split("\\|")));
 		stops.addAll(Arrays.asList(new String[] { "NUM", "(", "[", "{", ")",
 				"]", "}", "\\\\d+" }));
 
 //		System.out.println(stops);
 //		System.out.println(Constant.FORBIDDEN);
 
 		for (int i = 0; i < stops.size(); i++) {
 			String word = stops.get(i);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.updateTable(word, "b", "*", "wordpos", 0);
 			// this.wordPOSTable.put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("*", 0, 0, null, null));
 			// System.out.println("Add Stop Word: " + word+"\n");
 		}
 	}
 
 	public void addCharacters() {
 		List<String> chars = new ArrayList<String>();
 		chars.addAll(Arrays.asList(Constant.CHARACTER.split("\\|")));
 //
 //		System.out.println(chars);
 //		System.out.println(Constant.CHARACTER);
 
 		for (int i = 0; i < chars.size(); i++) {
 			String word = chars.get(i);
 			// String reg="\\b("+Constant.FORBIDDEN+")\\b";
 			// boolean f = word.matches(reg);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.updateTable(word, "b", "*", "wordpos", 0);
 			// this.wordPOSTable.put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("", 0, 0, null, null));
 			// System.out.println("addCharacter word: " + word);
 		}
 	}
 
 	public void addNumbers() {
 		List<String> nums = new ArrayList<String>();
 		nums.addAll(Arrays.asList(Constant.NUMBER.split("\\|")));
 
 //		System.out.println(nums);
 //		System.out.println(Constant.NUMBER);
 
 		for (int i = 0; i < nums.size(); i++) {
 			String word = nums.get(i);
 			// String reg="\\b("+Constant.FORBIDDEN+")\\b";
 			// boolean f = word.matches(reg);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.updateTable(word, "b", "*", "wordpos", 0);
 			// this.wordPOSTable.put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("*", 0, 0, null, null));
 			// System.out.println("add Number: " + word);
 		}
 		this.updateTable("NUM", "b", "*", "wordpos", 0);
 		// this.wordPOSTable.put(new WordPOSKey("NUM", "b"), new
 		// WordPOSValue("*",0, 0, null, null));
 	}
 
 	public void addClusterstrings() {
 		List<String> cltstrs = new ArrayList<String>();
 		cltstrs.addAll(Arrays.asList(Constant.CLUSTERSTRING.split("\\|")));
 
 //		System.out.println(cltstrs);
 //		System.out.println(Constant.CLUSTERSTRING);
 
 		for (int i = 0; i < cltstrs.size(); i++) {
 			String word = cltstrs.get(i);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.updateTable(word, "b", "*", "wordpos", 0);
 			// this.wordPOSTable.put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("*", 1, 1, null, null));
 			// System.out.println("addClusterString: " + word);
 		}
 	}
 
 	public void addProperNouns() {
 		List<String> ppnouns = new ArrayList<String>();
 		ppnouns.addAll(Arrays.asList(Constant.PROPERNOUN.split("\\|")));
 
 		for (int i = 0; i < ppnouns.size(); i++) {
 			String word = ppnouns.get(i);
 			if (word.matches("\\b(" + Constant.FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.updateTable(word, "b", "*", "wordpos", 0);
 			// this.wordPOSTable.put(new WordPOSKey(word, "z"), new
 			// WordPOSValue("*", 0, 0, null, null));
 			// System.out.println("Add ProperNoun: " + word);
 		}
 	}
 	
 	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	// suffix: -fid(adj), -form (adj), -ish(adj), -less(adj), -like (adj)),
 	// -merous(adj), -most(adj), -shaped(adj), -ous(adj)
 	// -ly (adv), -er (advj), -est (advj),
 	// foreach unknownword in unknownwords table
 	// seperate root and suffix
 	// if root is a word in WN or in unknownwords table
 	// make the unknowword a "b" boundary
 
 	/**
 	 * for each unknownword in unknownwords table seperate root and suffix if
 	 * root is a word in WN or in unknownwords table make the unknowword a "b"
 	 * boundary
 	 * 
 	 * suffix: -fid(adj), -form (adj), -ish(adj), -less(adj), -like (adj)),
 	 * -merous(adj), -most(adj), -shaped(adj), -ous(adj)
 	 */
 	public void posBySuffix() {
 		String p1 = "^[a-z_]+(" + Constant.SUFFIX + ")$";
 		String p2 = "^[._.][a-z]+"; // , _nerved
 		Iterator<Map.Entry<String, String>> iterator = this.myDataHolder.unknownWordTable
 				.entrySet().iterator();
 
 		while (iterator.hasNext()) {
 			Map.Entry<String, String> unknownWordEntry = iterator.next();
 			String unknownWord = unknownWordEntry.getKey();
 			String unknownWordTag = unknownWordEntry.getValue();
 
 			if (unknownWordTag.equals("unknown")) {
 				if (unknownWord.matches(p1)) {
 					Matcher matcher = Pattern.compile(
 							"(.*?)(" + Constant.SUFFIX + ")$").matcher(
 							unknownWord);
 					if ((unknownWord.matches("^[a-zA-Z0-9_-]+$"))
 							&& matcher.matches()) {
 						if (this.posBySuffix_debug) {
 							System.out.println("posBySuffix - check word:");
 							System.out.println(unknownWord);
 						}
 						String base = matcher.group(1);
 						String suffix = matcher.group(2);
 						if (this.containSuffix(unknownWord, base, suffix)) {
 							this.updateTable(unknownWord, "b", "*", "wordpos", 0);
 							if (this.posBySuffix_debug) {
 								System.out.println("posBySuffix - set word:");
 								System.out.println(unknownWord);
 							}
 						}
 					}
 				}
 
 				if (unknownWord.matches(p2)) {
 					// unknownWordTable.put(unknownWord, "b");
 					this.myDataHolder.wordPOSTable.put(new WordPOSKey(unknownWord, "b"),
 							new WordPOSValue("*", 0, 0, null, null));
 					System.out
 							.println("posbysuffix set $unknownword a boundary word\n");
 				}
 			}
 		}
 	}
 
 	// return false or true depending on if the word contains the suffix as the
 	// suffix
 	public boolean containSuffix(String word, String base, String suffix) {
 		boolean flag = false; // return value
 		boolean wordInWN = false; // if this word is in WordNet
 		boolean baseInWN = false;
 		WordNetAPI myWN;
 
 		// check base
 		// this if statement is added by Dongye
 		if (base.length() == 0) {
 			return true;
 		}
 
 		base.replaceAll("_", ""); // cup_shaped
 
 		myWN = this.myConfiguration.getWordNet();
 		if (myWN.contains(word)) {
 			wordInWN = true; // word is in WordNet
 		} else {
 			// $wnoutputword =~ s#\n# #g;
 
 			wordInWN = false;
 		}
 
 		if (myWN.contains(base)) {
 			baseInWN = true;
 		} else {
 			// $wnoutputbase =~ s#\n# #g;
 			baseInWN = false;
 		}
 
 		// if WN pos is adv, return 1: e.g. ly, or if $base is in
 		// unknownwords
 		// table
 		if (suffix.equals("ly")) {
 			if (wordInWN) {
 				// if($wnoutputword =~/Overview of adv $word/){
 				if (myWN.isAdverb(word)) {
 					return true;
 				}
 			}
 			// if the word is in unknown word set, return true
 			if (this.myDataHolder.unknownWordTable.containsKey(base)) {
 				return true;
 			}
 		}
 
 		// if WN recognize superlative, comparative adjs, return 1: e.g. er,
 		// est
 		else if (suffix.equals("er") || suffix.equals("est")) {
 			if (wordInWN) {
 				// if($wnoutputword =~/Overview of adj (\w+)/){#$word =
 				// softer,
 				// $1 = soft vs. $word=$1=neuter
 				// $word = softer, $1 = soft vs. $word=$1=neuter
 				if (myWN.isAdjective(word) || myWN.isAdverb(word)) {
 					return true;
 				}
 				// return 1 if $word=~/^$1\w+/;
 			}
 		}
 
 		// if $base is in WN or unknownwords table, or if $word has sole pos
 		// adj
 		// in WN, return 1: e.g. scalelike
 		else {
 			if (myWN.isSoleAdjective(word)) {
 				return true;
 			}
 			if (baseInWN) {
 				return true;
 			}
 			if (this.myDataHolder.unknownWordTable.containsKey(base)) {
 				return true;
 			}
 		}
 
 		return flag;
 	}
 
 	public void markupByPattern() {
 		System.out.println("markupbypattern start");
 		// int cap=this.sentence.size();
 		int cap = this.myDataHolder.sentenceTable.size();
 		// ((ArrayList)this.tag).ensureCapacity(cap);
 		// ((ArrayList)this.modifier).ensureCapacity(cap);
 		// for (int i=0;i<this.originalSent.size();i++) {
 		for (int i = 0; i < cap; i++) {
 			// case 1
 			// if (this.originalSent.get(i).matches("^x=.*")) {
 			if (this.myDataHolder.sentenceTable.get(i).getOriginalSentence()
 					.matches("^x=.*")) {
 				// tag.set(i, "chromosome");
 				// modifier.set(i, "");
 				this.myDataHolder.sentenceTable.get(i).setTag("chromosome");
 				this.myDataHolder.sentenceTable.get(i).setModifier("");
 			}
 			// case 2
 			else if (this.myDataHolder.sentenceTable.get(i).getOriginalSentence()
 					.matches("^2n=.*")) {
 				// tag.set(i, "chromosome");
 				// modifier.set(i, "");
 				this.myDataHolder.sentenceTable.get(i).setTag("chromosome");
 				this.myDataHolder.sentenceTable.get(i).setModifier("");
 			}
 			// case 3
 			else if (this.myDataHolder.sentenceTable.get(i).getOriginalSentence()
 					.matches("^x .*")) {
 				// tag.set(i, "chromosome");
 				// modifier.set(i, "");
 				this.myDataHolder.sentenceTable.get(i).setTag("chromosome");
 				this.myDataHolder.sentenceTable.get(i).setModifier("");
 			}
 			// case 4
 			else if (this.myDataHolder.sentenceTable.get(i).getOriginalSentence()
 					.matches("^2n .*")) {
 				// tag.set(i, "chromosome");
 				// modifier.set(i, "");
 				this.myDataHolder.sentenceTable.get(i).setTag("chromosome");
 				this.myDataHolder.sentenceTable.get(i).setModifier("");
 			}
 			// case 5
 			else if (this.myDataHolder.sentenceTable.get(i).getOriginalSentence()
 					.matches("^2 n.*")) {
 				// tag.set(i, "chromosome");
 				// modifier.set(i, "");
 				this.myDataHolder.sentenceTable.get(i).setTag("chromosome");
 				this.myDataHolder.sentenceTable.get(i).setModifier("");
 			}
 			// case 6
 			else if (this.myDataHolder.sentenceTable.get(i).getOriginalSentence()
 					.matches("^fl.*")) {
 				// tag.set(i, "flowerTime");
 				// modifier.set(i, "");
 				this.myDataHolder.sentenceTable.get(i).setTag("flowerTime");
 				this.myDataHolder.sentenceTable.get(i).setModifier("");
 			}
 			// case 7
 			else if (this.myDataHolder.sentenceTable.get(i).getOriginalSentence()
 					.matches("^fr.*")) {
 				// tag.set(i, "flowerTime");
 				// modifier.set(i, "");
 				this.myDataHolder.sentenceTable.get(i).setTag("flowerTime");
 				this.myDataHolder.sentenceTable.get(i).setModifier("");
 			}
 		}
 		System.out.println("markupbypattern end");
 	}
 
 	// private String IGNOREPTN ="(IGNOREPTN)"; //disabled
 	public void markupIgnore() {
 		// $sth =
 		// $dbh->prepare("update ".$prefix."_sentence set tag = 'ignore', modifier='' where originalsent rlike '(^| )$IGNOREPTN ' ");
 		for (int i = 0; i < this.myDataHolder.sentenceTable.size(); i++) {
 			String thisSent = this.myDataHolder.sentenceTable.get(i).getOriginalSentence();
 			String p = "(^| )" + Constant.IGNOREPTN;
 			if (thisSent.matches("(^|^ )" + Constant.IGNOREPTN + ".?")) {
 				this.myDataHolder.sentenceTable.get(i).setTag("ignore");
 				this.myDataHolder.sentenceTable.get(i).setModifier("");
 			}
 		}
 	}
 
 	public int discover(String s) {
 		int newDisc = 0;
 
 		for (int i = 0; i < this.myDataHolder.sentenceTable.size(); i++) {
 			Sentence sentEntry = this.myDataHolder.sentenceTable.get(i);
 			// sentid
 			String sent = sentEntry.getSentence();
 			String lead = sentEntry.getLead();
 			String tag = sentEntry.getTag();
 			String status = sentEntry.getStatus();
 			if (!(tag == null || !tag.equals("ignore") && status.equals(s))) {
 				continue;
 			}
 
 			String[] startWords = lead.split("\\s+");
 			// @startwords = split(/\s+/,$lead);
 
 			// $pattern = buildpattern(@startwords);
 			String pattern = buildPattern(startWords);
 
 			if (pattern.matches("^.*\\w+.*$")) {
 				// ids of untagged sentences that match the pattern
 				Set<Integer> matched = matchPattern(pattern, status, false);
 				int round = 0;
 				int numNew = 0;
 
 				do {
 					numNew = ruleBasedLearn(matched);
 					newDisc = newDisc + numNew;
 					round++;
 				} while (numNew > 0);
 			}
 
 		}
 
 		return newDisc;
 	}
 
 	/**
 	 * return a positive number if anything new is learnt from @source sentences
 	 * by applying rules and clues to grow %NOUNS and %BDRY and to confirm tags
 	 * create and maintain decision tables
 	 * 
 	 * @param matched
 	 * @return
 	 */
 	public int ruleBasedLearn(Set<Integer> matched) {
 
 		int sign = 0;
 		int numNew = 0;
 		String tag = "";
 
 		Iterator<Integer> iter = matched.iterator();
 		while (iter.hasNext()) {
 			int sentID = iter.next().intValue();
 			Sentence sent = this.myDataHolder.sentenceTable.get(sentID);
 			if (sent.getTag() != null) {
 				// ($tag, $new) = doit($sentid);
 				doIt(sentID);
 				// tag($sentid, $tag);
 				tagIt(sentID, tag);
 				sign = sign + numNew;
 			}
 		}
 
 		return 0;
 	}
 
 	// update wordpos table (on certainty) when a sentence is tagged for the
 	// first time. this update should not be done when a pos is looked up,
 	// because we may lookup a pos for the same example multiple times. if the
 	// tag need to be adjusted (not by doit function), also need to adjust
 	// certainty counts.
 	public void doIt(int sentID) {
 		int sign = 0;
 
 		Sentence sentEntry = this.myDataHolder.sentenceTable.get(sentID);
 		String sent = sentEntry.getSentence();
 		String lead = sentEntry.getLead();
 
 		String[] words = lead.split("\\s+");
 		String ptn = this.getPOSptn(words);
 
 		Pattern p;
 		Matcher m;
 
 		// Case 1: single word case
 		if (ptn.matches("^[pns]$")) {
 			String tag = words[0];
 			sign = sign + updateTable(tag, ptn, "-", "wordpos", 1);
 		}
 
 		// Case 2: the POSs are "ps"
 		else {
 			p = Pattern.compile("^.*ps.*$");
 			m = p.matcher(ptn);
 			if (m.find()) {
 				int start = m.start();
 				int end = m.end();
 				String pWord = words[start];
 				String sWord = words[end - 1];
 
 				sign += updateTable(pWord, "p", "-", "wordpos", 1);
 				sign += updateTable(sWord, "s", "", "wordpos", 1);
 
 				// $sign += updatenn(0, $#tws+1, @tws); #up to the "p" inclusive
 
 			} else {
 				p = Pattern.compile("^.*p(\\?).*$");
 				m = p.matcher(ptn);
 				if (m.find()) {
 					int start = m.start();
 				}
 			}
 		}
 	}
 
 	/**
 	 * The length of the ptn must be the same as the number of words in words.
 	 * If certainty is < 50%, replace POS with ?.
 	 * 
 	 * @param words
 	 * @return
 	 */
 	public String getPOSptn(String[] words) {
 		String ptn = "";
 		for (int i = 0; i < words.length; i++) {
 			String word = words[i];
 			List<POSInfo> POSInfoList = checkPOSInfo(word);
 			if (POSInfoList.size() > 0) {
 				POSInfo p = POSInfoList.get(0);
 				String POS = p.getPOS();
 				String role = p.getRole();
 				double certainty = (double) p.getCertaintyU()
 						/ (double) p.getCertaintyL();
 
 				if ((!POS.equals("?")) && (certainty <= 0.5)) {
 					// This POS has a certainty less than 0.5. It is ignored.
 					POS = "?";
 				}
 				ptn = ptn + POS;
 			}
 		}
 
 		return ptn;
 	}
 
 	/**
 	 * It usually return all
 	 * 
 	 * @param word
 	 * @return a list of POSInfo objects in descending order of
 	 *         certaintyU/certaintyL
 	 */
 	public List<POSInfo> checkPOSInfo(String word) {
 		List<POSInfo> POSInfoList = new ArrayList<POSInfo>();
 
 		word = StringUtility.removeAll(word, "^\\s*");
 		word = StringUtility.removeAll(word, "\\s+$");
 
 		if (word.matches("^\\d+.*$")) {
 			POSInfo p = new POSInfo("b", "", 1, 1);
 			POSInfoList.add(p);
 			return POSInfoList;
 		}
 
 		Iterator<Map.Entry<WordPOSKey, WordPOSValue>> iter = this.myDataHolder.wordPOSTable
 				.entrySet().iterator();
 		while (iter.hasNext()) {
 			Map.Entry<WordPOSKey, WordPOSValue> e = iter.next();
 			String w = e.getKey().getWord();
 			if (w.equals(word)) {
 				String POS = e.getKey().getPOS();
 				String role = e.getValue().getRole();
 				int certaintyU = e.getValue().getCertaintyU();
 				int certaintyL = e.getValue().getCertaintyL();
 				POSInfo p = new POSInfo(POS, role, certaintyU, certaintyL);
 				POSInfoList.add(p);
 			}
 		}
 
 		// nothing found
 		if (POSInfoList.size() == 0) {
 			return new ArrayList<POSInfo>();
 		} else {
 			// sort the list in ascending order of certaintyU/certaintyL
 			Collections.sort(POSInfoList);
 			// reverse it into descending order
 			Collections.reverse(POSInfoList);
 
 			return POSInfoList;
 		}
 	}
 
 	public void tagIt(int sentID, String tag) {
 		;
 	}
 
 	public Set<Integer> matchPattern(String pattern, String s, boolean hasTag) {
 
 		Set<Integer> matchedIDs = new HashSet<Integer>();
 
 		for (int i = 0; i < this.myDataHolder.sentenceTable.size(); i++) {
 			Sentence sent = this.myDataHolder.sentenceTable.get(i);
 			String sentence = sent.getSentence();
 			String status = sent.getStatus();
 			String tag = sent.getTag();
 			if ((hasTag && (tag != null) && (status.equals(s)))
 					|| ((!hasTag) && (tag == null) && (status.equals(s)))) {
 				Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
 				Matcher m = p.matcher(sentence);
 				if (m.lookingAt()) {
 					matchedIDs.add(i);
 				}
 			}
 		}
 
 		return matchedIDs;
 	}
 
 	/**
 	 * 
 	 * @param startWords
 	 * @return
 	 */
 	public String buildPattern(String[] startWords) {
 		Set<String> newWords = new HashSet<String>();
 		String temp = "";
 		String prefix = "\\w+\\s";
 		String pattern = "";
 
 		for (int i = 0; i < startWords.length; i++) {
 			String word = startWords[i];
 			Pattern p = Pattern.compile(":" + word + ":",
 					Pattern.CASE_INSENSITIVE);
 			Matcher m = p.matcher(this.CHECKEDWORDS);
 			// This is not very sure, need to make sure - Dongye
 			if ((!word.matches("[\\p{Punct}0-9]")) && (!m.lookingAt())) {
 				temp = temp + word + "|";
 				newWords.add(word);
 			}
 		}
 
 		// no new words
 		if (!temp.matches("^.*\\w.*$")) {
 			return "";
 		}
 
 		// remove the last char, which is a '|'
 		temp = temp.substring(0, temp.length() - 1);
 		temp = "\\b(?:" + temp + ")\\b";
 
 		pattern = "^" + temp + "|";
 
 		for (int j = 0; j < this.NUM_LEAD_WORDS - 1; j++) {
 			temp = prefix + temp;
 			pattern = pattern + "^" + temp + "|";
 		}
 
 		pattern = pattern.substring(0, pattern.length() - 1);
 
 		pattern = "(?:" + pattern + ")";
 
 		this.CHECKEDWORDS = this.updateCheckedWords(":", this.CHECKEDWORDS,
 				newWords);
 
 		return pattern;
 	}
 	
 	/**
 	 * 
 	 * @param expr
 	 * @param checkedWords
 	 * @param list
 	 * @return
 	 */
 	public String updateCheckedWords(String expr, String checkedWords,
 			Set<String> list) {
 		String newCheckedWords = checkedWords;
 		Iterator<String> iter = list.iterator();
 
 		while (iter.hasNext()) {
 			newCheckedWords = newCheckedWords + iter.next() + ":";
 		}
 		// newCheckedWords = newCheckedWords + ":";
 
 		return newCheckedWords;
 	}
 	
 }
