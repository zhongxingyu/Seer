 package semanticMarkup.ling.learn;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
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
 
 import semanticMarkup.core.Treatment;
 import semanticMarkup.know.IGlossary;
 import semanticMarkup.know.lib.WordNetPOSKnowledgeBase;
 import semanticMarkup.knowledge.KnowledgeBase;
 import semanticMarkup.ling.learn.auxiliary.GetNounsAfterPtnReturnValue;
 import semanticMarkup.ling.learn.auxiliary.KnownTagCollection;
 import semanticMarkup.ling.learn.auxiliary.POSInfo;
 import semanticMarkup.ling.learn.auxiliary.SentenceLeadLengthComparator;
 import semanticMarkup.ling.learn.auxiliary.StringAndInt;
 import semanticMarkup.ling.learn.dataholder.DataHolder;
 import semanticMarkup.ling.learn.dataholder.ModifierTableValue;
 import semanticMarkup.ling.learn.dataholder.SentenceStructure;
 import semanticMarkup.ling.learn.dataholder.WordPOSKey;
 import semanticMarkup.ling.learn.dataholder.WordPOSValue;
 import semanticMarkup.ling.learn.knowledge.AdditionalBootstrappingLearner;
 import semanticMarkup.ling.learn.knowledge.AdjectiveSubjectBootstrappingLearner;
 import semanticMarkup.ling.learn.knowledge.AdjectiveVerifier;
 import semanticMarkup.ling.learn.knowledge.AnnotationNormalizer;
 import semanticMarkup.ling.learn.knowledge.CommaAsAndAnnotator;
 import semanticMarkup.ling.learn.knowledge.Constant;
 import semanticMarkup.ling.learn.knowledge.CoreBootstrappingLearner;
 import semanticMarkup.ling.learn.knowledge.DittoAnnotator;
 import semanticMarkup.ling.learn.knowledge.FiniteSetsLoader;
 import semanticMarkup.ling.learn.knowledge.HeuristicNounsLearner;
 import semanticMarkup.ling.learn.knowledge.IgnorePatternAnnotator;
 import semanticMarkup.ling.learn.knowledge.IgnoredFinalizer;
 import semanticMarkup.ling.learn.knowledge.Initializer;
 import semanticMarkup.ling.learn.knowledge.NullSentenceTagger;
 import semanticMarkup.ling.learn.knowledge.POSBasedAnnotator;
 import semanticMarkup.ling.learn.knowledge.PatternBasedAnnotator;
 import semanticMarkup.ling.learn.knowledge.PhraseClauseAnnotator;
 import semanticMarkup.ling.learn.knowledge.PronounCharactersAnnotator;
 import semanticMarkup.ling.learn.knowledge.SeedNounsLearner;
 import semanticMarkup.ling.learn.knowledge.UnknownWordBootstrappingLearner;
 import semanticMarkup.ling.learn.utility.LearnerUtility;
 import semanticMarkup.ling.learn.utility.StringUtility;
 import semanticMarkup.ling.transform.ITokenizer;
 
 public class Learner {
 	private static final Set<String> NONS = null; // ??
 	private Configuration myConfiguration;
 	private ITokenizer myTokenizer;
 
 	// Data holder
 	private DataHolder myDataHolder;
 
 	// Learner utility
 	private LearnerUtility myLearnerUtility;
 
 	// Class variables
 	private int NUM_LEAD_WORDS; // Number of leading words
 
 	// leading three words of sentences
 	private Set<String> checkedWordSet;
 
 
 
 	// modules
 	KnowledgeBase knowledgeBase;
 	Initializer initializer;
 	
 	HeuristicNounsLearner heuristicNounsLearner;
 
 	FiniteSetsLoader finiteSetsLoader;
 	
 	SeedNounsLearner seedNounsLearner;
 	
 	PatternBasedAnnotator patternBasedAnnotator; 
 	
 	IgnorePatternAnnotator ignorePatternAnnotator;
 	
 	CoreBootstrappingLearner coreBootstrappingLearner;
 	
 	AdditionalBootstrappingLearner additionalBootstrappingLearner;
 	
 	UnknownWordBootstrappingLearner unknownWordBootstrappingLearner;
 	
 	AdjectiveVerifier adjectiveVerifier;
 	
 	AdjectiveSubjectBootstrappingLearner adjectiveSubjectBootstrappingLearner;
 
 	POSBasedAnnotator posBasedAnnotator;
 	
 	PhraseClauseAnnotator phraseClauseAnnotator;
 	
 	DittoAnnotator dittoAnnotator;
 	
 	PronounCharactersAnnotator pronounCharactersAnnotator;
 	
 	IgnoredFinalizer ignoredFinalizer; 
 	
 	CommaAsAndAnnotator commaAsAndAnnotator;
 	
 	NullSentenceTagger nullSentenceTagger;
 	
 	AnnotationNormalizer annotationNormalizer; 
 	
 	Map<String, Boolean> checkedModifiers;
 
 	public Learner(Configuration configuration, ITokenizer tokenizer,
 			LearnerUtility learnerUtility) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("Learner");
 
 		this.myConfiguration = configuration;
 		this.myTokenizer = tokenizer;
 
 		// Utilities
 		this.myLearnerUtility = learnerUtility;
 
 		// Data holder
 		this.myDataHolder = new DataHolder(myConfiguration,
 				myLearnerUtility.getConstant(), myLearnerUtility.getWordFormUtility());
 
 		// Class variables
 		NUM_LEAD_WORDS = this.myConfiguration.getNumLeadWords(); // Set the
 																	// number of
 																	// leading
 																	// words be
 																	// 3
 
 		checkedWordSet = new HashSet<String>();
 
 		this.checkedModifiers = new HashMap<String, Boolean>();
 
 		myLogger.info("Created Learner");
 		myLogger.info("\tLearning Mode: " + myConfiguration.getLearningMode());
 		myLogger.info("\tMax Tag Lengthr: " + myConfiguration.getMaxTagLength());
 		myLogger.info("\n");
 
 		this.knowledgeBase = new KnowledgeBase();
 		
 		this.initializer = new Initializer(this.myLearnerUtility,
 				this.NUM_LEAD_WORDS);
 		this.heuristicNounsLearner = new HeuristicNounsLearner(this.myLearnerUtility);
 		
 		this.finiteSetsLoader = new FiniteSetsLoader(this.myLearnerUtility);
 		
 		this.seedNounsLearner = new SeedNounsLearner(this.myLearnerUtility);
 		
 		this.patternBasedAnnotator = new PatternBasedAnnotator();
 		
 		this.ignorePatternAnnotator = new IgnorePatternAnnotator();
 		
 		this.coreBootstrappingLearner = new CoreBootstrappingLearner(this.myLearnerUtility, this.myConfiguration);
 		
 		this.additionalBootstrappingLearner = new AdditionalBootstrappingLearner(this.myLearnerUtility, this.myConfiguration);
 		
 		this.unknownWordBootstrappingLearner = new UnknownWordBootstrappingLearner(
 				this.myLearnerUtility);
 		
 		this.adjectiveVerifier = new AdjectiveVerifier(this.myLearnerUtility);
 		
 		this.adjectiveSubjectBootstrappingLearner = new AdjectiveSubjectBootstrappingLearner(this.myLearnerUtility, this.myConfiguration.getLearningMode(), this.myConfiguration.getMaxTagLength());
 		
 		this.posBasedAnnotator = new POSBasedAnnotator(this.myLearnerUtility);
 		
 		this.phraseClauseAnnotator = new PhraseClauseAnnotator(this.myLearnerUtility);
 		
 		this.dittoAnnotator = new DittoAnnotator(this.myLearnerUtility);
 		
 		this.pronounCharactersAnnotator = new PronounCharactersAnnotator(this.myLearnerUtility);
 		
 		this.ignoredFinalizer = new IgnoredFinalizer();
 		
 		this.commaAsAndAnnotator = new CommaAsAndAnnotator(this.myLearnerUtility);
 		
		this.nullSentenceTagger = new NullSentenceTagger(this.myLearnerUtility, this.myConfiguration.getDefaultGeneralTag());
 		
 		this.annotationNormalizer 
 			= new AnnotationNormalizer(this.getConfiguration().getLearningMode(), 
 					this.checkedModifiers, this.getLearnerUtility());
 		
 		
 
 	}
 
 	public DataHolder learn(List<Treatment> treatments, IGlossary glossary,
 			String markupMode) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("Learn");
 		myLogger.trace("Enter Learn");
 		myLogger.trace(String.format("Learning Mode: %s",
 				this.myConfiguration.getLearningMode()));
 
 		this.knowledgeBase.importKnowledgeBase(this.myDataHolder, "kb", this.myLearnerUtility.getConstant());
 		
 		this.initializer.loadTreatments(treatments);
 		this.initializer.run(myDataHolder);
 
 		this.heuristicNounsLearner.run(this.myDataHolder);
 
 		this.finiteSetsLoader.run(this.myDataHolder);
 
 		this.seedNounsLearner.run(myDataHolder);
 	
 		// Set the certaintyU and certaintyL value of every entry in WordPOS collection to be 0
 		this.resetCounts(myDataHolder);
 		
 		this.patternBasedAnnotator.run(myDataHolder);
 
 		this.ignorePatternAnnotator.run(myDataHolder);
 
 		
 		this.coreBootstrappingLearner.setStatus("start");
 		this.coreBootstrappingLearner.run(myDataHolder);
 		
 		this.coreBootstrappingLearner.setStatus("normal");
 		this.coreBootstrappingLearner.run(myDataHolder);
 		
 		this.additionalBootstrappingLearner.run(myDataHolder);
 
 		myLogger.info("Unknownword bootstrappings:");
 		this.unknownWordBootstrappingLearner.run(myDataHolder);
 
 		myLogger.info("Adjectives Verification:");
 		this.adjectiveVerifier.run(myDataHolder);
 
 		this.separateModifierTag(myDataHolder); // !!!
 
 		this.resolveNMB(myDataHolder); // !!!
 
 		this.setAndOr(myDataHolder); // !!!
 
 		this.adjectiveSubjectBootstrappingLearner.run(myDataHolder);
 
 		// set tags of sentences with "andor" tag to null
 		this.resetAndOrTags(myDataHolder);
 
 		this.getLearnerUtility().tagAllSentences(myDataHolder, "singletag",
 				"sentence");
 
 		this.posBasedAnnotator.run(myDataHolder);
 
 		this.phraseClauseAnnotator.run(myDataHolder);
 
 		this.dittoAnnotator.run(myDataHolder);
 
 		this.pronounCharactersAnnotator.run(myDataHolder);
 		
 		this.ignoredFinalizer.run(myDataHolder);
 		
 		this.posBasedAnnotator.run(myDataHolder);
 
 		// tag remaining sentences with null tags 
 		this.nullSentenceTagger.run(myDataHolder);
 
 		if (StringUtils.equals(this.myConfiguration.getLearningMode(), "adj")) {
 			 this.commonSubstructure(myDataHolder); // !!!
 		}
 		
 		this.commaAsAndAnnotator.run(myDataHolder);
 		
 		this.annotationNormalizer.run(myDataHolder);
 		
 		this.prepareTables4Parser(myDataHolder);
 
 		myDataHolder.writeToFile("dataholder", "");
 
 		myLogger.info("Learning done!");
 
 		return myDataHolder;
 	}
 
 	private void adjectiveSubjectBootstrappingLearner(DataHolder dataholderHandler,
 			String learningMode) {
 		if (StringUtils.equals(learningMode, "adj")) {
 //			myLogger.info("Bootstrapping on adjective subjects");
 			 adjectiveSubjectBootstrapping(myDataHolder); // !!!
 		} else {
 			int v = 0;
 			do {
 				v = 0;
 				this.handleAndOr(myDataHolder); // !!!
 			} while (v > 0);
 		}
 		
 	}
 
 	public void addGlossary(IGlossary glossary) {
 		if (glossary != null) {
 			String category = "struture";
 			Set<String> pWords = glossary.getWords(category);
 			Set<String> categories = new HashSet<String>();
 			categories.add(category);
 			Set<String> bWords = glossary.getWordsNotInCategories(categories);
 			this.getDataHolder().addWords2WordPOSHolder(pWords, "p");
 			this.getDataHolder().addWords2WordPOSHolder(bWords, "b");
 		}
 	}
 
 //	private void addPredefinedWords() {
 //		this.addStopWords();
 //		this.addCharacters();
 //		this.addNumbers();
 //		this.addClusterStrings();
 //		this.addProperNouns();
 //	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public DataHolder getDataHolder() {
 		return this.myDataHolder;
 	}
 
 	/**
 	 * 
 	 */
 	public void addHeuristicsNouns() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.addHeuristicsNouns");
 
 		myLogger.trace("Enter addHeuristicsNouns");
 
 		Set<String> nouns = this.learnHeuristicsNouns();
 		myLogger.debug("Nouns learned from heuristics:");
 		myLogger.debug("\t" + nouns.toString());
 		myLogger.debug("Total: " + nouns.size());
 
 		List<Set<String>> results = this.characterHeuristics();
 		Set<String> rnouns = results.get(0);
 		Set<String> descriptors = results.get(1);
 		addDescriptors(descriptors);
 		addNouns(rnouns);
 
 		// this.myDataHolder.printHolder(DataHolder.SINGULAR_PLURAL);
 
 		myLogger.debug("Total: " + nouns.size());
 		Iterator<String> iter = nouns.iterator();
 		myLogger.info("Learn singular-plural pair");
 		while (iter.hasNext()) {
 			String e = iter.next();
 			myLogger.trace("Check Word: " + e);
 
 			if ((e.matches("^.*\\w.*$"))
 					&& (!StringUtility.isMatchedWords(e, "NUM|"
 							+ this.myLearnerUtility.getConstant().NUMBER + "|" + this.myLearnerUtility.getConstant().CLUSTERSTRING
 							+ "|" + this.myLearnerUtility.getConstant().CHARACTER + "|"
 							+ this.myLearnerUtility.getConstant().PROPERNOUN))) {
 				myLogger.trace("Pass");
 
 				// same word may have two different pos tags
 				String[] nounArray = e.split("\\|");
 				for (int i = 0; i < nounArray.length; i++) {
 					String nounAndPOS = nounArray[i];
 					Pattern p = Pattern.compile("(\\w+)\\[([spn])\\]");
 					Matcher m = p.matcher(nounAndPOS);
 					if (m.lookingAt()) {
 						String word = m.group(1);
 						String pos = m.group(2);
 						this.myDataHolder.updateDataHolder(word, pos, "*",
 								"wordpos", 0);
 
 						if (pos.equals("p")) {
 							String plural = word;
 							String singular = this.myLearnerUtility
 									.getWordFormUtility().getSingular(plural);
 							if (singular != null) {
 								if (!singular.equals("")) {
 									this.myDataHolder.addSingularPluralPair(
 											singular, plural);
 								}
 							}
 						}
 
 						if (pos.equals("s")) {
 							String singular = word;
 							List<String> pluralList = this.myLearnerUtility
 									.getWordFormUtility().getPlural(singular);
 							Iterator<String> pluralIter = pluralList.iterator();
 							while (pluralIter.hasNext()) {
 								String plural = pluralIter.next();
 								if (plural != null) {
 									if (!plural.equals("")) {
 										this.myDataHolder
 												.addSingularPluralPair(
 														singular, plural);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		myLogger.trace("Quite addHeuristicsNouns");
 	}
 
 	/**
 	 * 
 	 * @param descriptors
 	 */
 	public void addDescriptors(Set<String> descriptors) {
 		Iterator<String> iter = descriptors.iterator();
 		while (iter.hasNext()) {
 			String descriptor = iter.next();
 
 			if (!StringUtility.isMatchedWords(descriptor, this.myLearnerUtility.getConstant().FORBIDDEN)) {
 				this.myDataHolder.updateDataHolder(descriptor, "b", "",
 						"wordpos", 1);
 			}
 		}
 
 	}
 
 	/**
 	 * 
 	 * @param rnouns
 	 */
 	public void addNouns(Set<String> rnouns) {
 		Iterator<String> iter = rnouns.iterator();
 		while (iter.hasNext()) {
 			String noun = iter.next();
 			if (!StringUtility.isMatchedWords(noun, this.myLearnerUtility.getConstant().FORBIDDEN)) {
 				this.myDataHolder.updateDataHolder(noun, "n", "", "wordpos", 1);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @return nouns learned by heuristics
 	 */
 	public Set<String> learnHeuristicsNouns() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger
 				.getLogger("learn.addHeuristicsNouns.learnHeuristicsNouns");
 
 		// Set of words
 		Set<String> words = new HashSet<String>();
 
 		// Set of nouns
 		Set<String> nouns = new HashSet<String>();
 
 		List<String> sentences = new LinkedList<String>();
 		for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 			String originalSentence = this.myDataHolder.getSentenceHolder()
 					.get(i).getOriginalSentence();
 			myLogger.trace("Original Sentence: " + originalSentence);
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
 			List<String> tokens = this.myLearnerUtility.tokenizeText(sentence,
 					"all");
 			for (String token : tokens) {
 				if (StringUtility.isWord(token)) {
 					words.add(token);
 					myLogger.trace("Add a word into words: " + token);
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
 			String root = myLearnerUtility.getWordFormUtility().getRoot(word);
 			if (wordMap.containsKey(root)) {
 				Set<String> wordList = wordMap.get(root);
 				wordList.add(word);
 				// List<String> wordList2 = wordMap.get(root);
 				// System.out.println(wordList2);
 			} else {
 				Set<String> wordList = new HashSet<String>();
 				wordList.add(word);
 				wordMap.put(root, wordList);
 			}
 		}
 
 		// print out the wordMap
 		myLogger.trace("WordMap:");
 		Iterator<Map.Entry<String, Set<String>>> wordMapIter = wordMap
 				.entrySet().iterator();
 		while (wordMapIter.hasNext()) {
 			Map.Entry<String, Set<String>> e = wordMapIter.next();
 			myLogger.trace(e.toString());
 		}
 
 		// find nouns
 		myLogger.info("Learn singular-plural pair");
 		Iterator<Map.Entry<String, Set<String>>> iter = wordMap.entrySet()
 				.iterator();
 		while (iter.hasNext()) {
 			Map.Entry<String, Set<String>> e = iter.next();
 			Set<String> wordSet = e.getValue();
 			Iterator<String> wordIterator = wordSet.iterator();
 			while (wordIterator.hasNext()) {
 				String word = wordIterator.next();
 
 				// getnouns
 				if (word.matches("^.*" + Constant.NENDINGS)) {
 					nouns.add(word + "[s]");
 					if (wordSet.contains(word + "s")) {
 						nouns.add(word + "s" + "[p]");
 						this.myDataHolder.addSingularPluralPair(word, word
 								+ "s");
 					}
 					if (wordSet.contains(word + "es")) {
 						nouns.add(word + "es" + "[p]");
 						this.myDataHolder.addSingularPluralPair(word, word
 								+ "es");
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
 			// for (int i1 = 0; i1 < wordList.size(); i1++) {
 			Iterator<String> wordIterator = wordSet.iterator();
 			while (wordIterator.hasNext()) {
 				String tempWord = wordIterator.next();
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
 						List<String> pair = myLearnerUtility
 								.getWordFormUtility().getSingularPluralPair(
 										word1, word2);
 						if (pair.size() == 2) {
 							String singular = pair.get(0);
 							String plural = pair.get(1);
 							nouns.add(singular + "[s]");
 							nouns.add(plural + "[p]");
 							this.myDataHolder.addSingularPluralPair(singular,
 									plural);
 						}
 					}
 				}
 			}
 		}
 
 		// print out nouns
 		myLogger.debug("Nouns: " + nouns);
 
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
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger
 				.getLogger("learn.addHeuristicsNouns.learnHeuristicsNouns.getPresentAbsentNouns");
 
 		String pachecked = "and|or|to";
 
 		// if (text.matches("(\\w+?)\\s+(present|absent)")) {
 		// System.out.println(text);
 		// }
 
 		Matcher matcher = Pattern.compile("^.*?(\\w+?)\\s+(present|absent).*$")
 				.matcher(text);
 		if (matcher.lookingAt()) {
 			String word = matcher.group(1);
 			if ((!word.matches("\\b(" + pachecked + ")\\b"))
 					&& (!word.matches("\\b(" + this.myLearnerUtility.getConstant().STOP + ")\\b"))
 					&& (!word
 							.matches("\\b(always|often|seldom|sometimes|[a-z]+ly)\\b"))) {
 
 				myLogger.trace("present/absent " + word);
 
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
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger
 				.getLogger("learn.addHeuristicsNouns.characterHeuristics");
 
 		Set<String> taxonNames = new HashSet<String>();
 		Set<String> nouns = new HashSet<String>();
 		Set<String> anouns = new HashSet<String>();
 		Set<String> pnouns = new HashSet<String>();
 		Set<String> descriptors = new HashSet<String>();
 		Map<String, Boolean> descriptorMap = new HashMap<String, Boolean>();
 
 		int sent_num = this.myDataHolder.getSentenceHolder().size();
 		for (int i = 0; i < sent_num; i++) {
 
 			// taxon rule
 			SentenceStructure sent = this.myDataHolder.getSentenceHolder().get(
 					i);
 			String source = sent.getSource();
 			String sentence = sent.getSentence();
 			String originalSentence = sent.getOriginalSentence();
 
 			myLogger.trace("Source: " + source);
 			myLogger.trace("Sentence: " + sentence);
 			myLogger.trace("Original Sentence: " + originalSentence);
 
 			originalSentence = StringUtility.trimString(originalSentence);
 
 			// noun rule 0: taxon names
 			taxonNames = this.getTaxonNameNouns(originalSentence);
 
 			// $sentence =~ s#<\s*/?\s*i\s*>##g;
 			// $originalsent =~ s#<\s*/?\s*i\s*>##g;
 
 			sentence = sentence.replaceAll("<\\s*/?\\s*i\\s*>", "");
 			originalSentence = originalSentence.replaceAll("<\\s*/?\\s*i\\s*>",
 					"");
 			// Update getSentenceHolder()
 			this.myDataHolder.getSentenceHolder().get(i).setSentence(sentence);
 
 			// noun rule 0.5: Meckle#s cartilage
 
 			Set<String> nouns0 = this
 					.getNounsMecklesCartilage(originalSentence);
 			nouns.addAll(nouns0);
 			sentence = sentence.replaceAll("#", "");
 			// Update getSentenceHolder()
 			this.myDataHolder.getSentenceHolder().get(i).setSentence(sentence);
 
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
 			// System.out.println("oSent:");
 			// System.out.println(originalSentence);
 
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
 
 		this.getDataHolder().add2HeuristicNounTable(nouns, "organ");
 		this.getDataHolder().add2HeuristicNounTable(anouns, "acronyms");
 		this.getDataHolder().add2HeuristicNounTable(pnouns, "propernouns");
 		this.getDataHolder().add2HeuristicNounTable(taxonNames, "taxonnames");
 
 		nouns.addAll(anouns);
 		nouns.addAll(pnouns);
 		nouns.addAll(taxonNames);
 
 		List<Set<String>> results = new LinkedList<Set<String>>();
 		results.add(nouns);
 		results.add(descriptors);
 
 		return results;
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
 	public Set<String> filterOutDescriptors(Set<String> rNouns,
 			Set<String> rDescriptors) {
 		Set<String> filtedNouns = new HashSet<String>();
 
 		Iterator<String> iter = rNouns.iterator();
 		while (iter.hasNext()) {
 			String noun = iter.next();
 			noun = noun.toLowerCase();
 
 			Pattern p = Pattern.compile("\\b(" + this.myLearnerUtility.getConstant().PREPOSITION + "|"
 					+ this.myLearnerUtility.getConstant().STOP + ")\\b", Pattern.CASE_INSENSITIVE);
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
 				+ this.myLearnerUtility.getConstant().PREPOSITION + "\\b)(.*)";
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
 				String regex2 = "\\b(" + this.myLearnerUtility.getConstant().PREPOSITION + "|"
 						+ this.myLearnerUtility.getConstant().STOP + ")\\b";
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
 		if (source.matches("^.*\\.xml_\\S+_.*$")
 				&& (!sentence.matches("^.*\\s.*$"))) {
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
 			for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 				String originalSentence = this.myDataHolder.getSentenceHolder()
 						.get(i).getOriginalSentence();
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
 
 	/**
 	public void addStopWords() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.addStopWords");
 		myLogger.trace("Add stop words");
 
 		List<String> stops = new ArrayList<String>();
 		stops.addAll(Arrays.asList(this.myLearnerUtility.getConstant().STOP.split("\\|")));
 		stops.addAll(Arrays.asList(new String[] { "NUM", "(", "[", "{", ")",
 				"]", "}", "d+" }));
 
 		myLogger.trace("Stop Words: " + stops);
 		for (int i = 0; i < stops.size(); i++) {
 			String word = stops.get(i);
 			if (word.matches("\\b(" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateDataHolder(word, "b", "*", "wordpos", 0);
 			myLogger.trace(String.format(
 					"(\"%s\", \"b\", \"*\", \"wordpos\", 0) added\n", word));
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("*", 0, 0, null, null));
 			// System.out.println("Add Stop Word: " + word+"\n");
 		}
 		myLogger.trace("Quite\n");
 	}
 
 	public void addCharacters() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.addCharacters");
 		myLogger.trace("Add characters");
 
 		List<String> chars = new ArrayList<String>();
 		chars.addAll(Arrays.asList(this.myLearnerUtility.getConstant().CHARACTER.split("\\|")));
 		//
 		// System.out.println(chars);
 		// System.out.println(this.myLearnerUtility.getConstant().CHARACTER);
 
 		for (int i = 0; i < chars.size(); i++) {
 			String word = chars.get(i);
 			// String reg="\\b("+this.myLearnerUtility.getConstant().FORBIDDEN+")\\b";
 			// boolean f = word.matches(reg);
 			if (word.matches("\\b(" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateDataHolder(word, "b", "*", "wordpos", 0);
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("", 0, 0, null, null));
 			// System.out.println("addCharacter word: " + word);
 		}
 	}
 
 	public void addNumbers() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.addNumbers");
 		myLogger.trace("Add numbers");
 
 		List<String> nums = new ArrayList<String>();
 		nums.addAll(Arrays.asList(this.myLearnerUtility.getConstant().NUMBER.split("\\|")));
 
 		// System.out.println(nums);
 		// System.out.println(this.myLearnerUtility.getConstant().NUMBER);
 
 		for (int i = 0; i < nums.size(); i++) {
 			String word = nums.get(i);
 			// String reg="\\b("+this.myLearnerUtility.getConstant().FORBIDDEN+")\\b";
 			// boolean f = word.matches(reg);
 			if (word.matches("\\b(" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateDataHolder(word, "b", "*", "wordpos", 0);
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("*", 0, 0, null, null));
 			// System.out.println("add Number: " + word);
 		}
 		this.myDataHolder.updateDataHolder("NUM", "b", "*", "wordpos", 0);
 		// this.getWordPOSHolder().put(new WordPOSKey("NUM", "b"), new
 		// WordPOSValue("*",0, 0, null, null));
 	}
 
 	public void addClusterStrings() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.addClusterstrings");
 		myLogger.trace("Add clusterstrings");
 
 		List<String> cltstrs = new ArrayList<String>();
 		cltstrs.addAll(Arrays.asList(this.myLearnerUtility.getConstant().CLUSTERSTRING.split("\\|")));
 
 		// System.out.println(cltstrs);
 		// System.out.println(this.myLearnerUtility.getConstant().CLUSTERSTRING);
 
 		for (int i = 0; i < cltstrs.size(); i++) {
 			String word = cltstrs.get(i);
 			if (word.matches("\\b(" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateDataHolder(word, "b", "*", "wordpos", 0);
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "b"), new
 			// WordPOSValue("*", 1, 1, null, null));
 			// System.out.println("addClusterString: " + word);
 		}
 	}
 
 	public void addProperNouns() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.addProperNouns");
 		myLogger.trace("Add proper nouns");
 
 		List<String> ppnouns = new ArrayList<String>();
 		ppnouns.addAll(Arrays.asList(Constant.PROPERNOUN.split("\\|")));
 
 		for (int i = 0; i < ppnouns.size(); i++) {
 			String word = ppnouns.get(i);
 			if (word.matches("\\b(" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b")) {
 				continue;
 			}
 			this.myDataHolder.updateDataHolder(word, "b", "*", "wordpos", 0);
 			// this.getWordPOSHolder().put(new WordPOSKey(word, "z"), new
 			// WordPOSValue("*", 0, 0, null, null));
 			// System.out.println("Add ProperNoun: " + word);
 		}
 	}
 	**/
 
 	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	// suffix: -fid(adj), -form (adj), -ish(adj), -less(adj), -like (adj)),
 	// -merous(adj), -most(adj), -shaped(adj), -ous(adj)
 	// -ly (adv), -er (advj), -est (advj),
 	// foreach unknownword in unknownwords table
 	// seperate root and suffix
 	// if root is a word in WN or in unknownwords table
 	// make the unknowword a "b" boundary
 
 	/**
 	 * for each unknown word in unknownwords table seperate root and suffix if
 	 * root is a word in WN or in unknownwords table make the unknowword a "b"
 	 * boundary
 	 * 
 	 * suffix: -fid(adj), -form (adj), -ish(adj), -less(adj), -like (adj)),
 	 * -merous(adj), -most(adj), -shaped(adj), -ous(adj)
 	 */
 	public void posBySuffix() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.posBySuffix");
 		myLogger.trace("Enter posBySuffix");
 
 		Iterator<Map.Entry<String, String>> iterator = this.myDataHolder
 				.getUnknownWordHolder().entrySet().iterator();
 
 		while (iterator.hasNext()) {
 			Map.Entry<String, String> unknownWordEntry = iterator.next();
 			String unknownWord = unknownWordEntry.getKey();
 			String unknownWordTag = unknownWordEntry.getValue();
 
 			if (unknownWordTag.equals("unknown")) {
 				// boolean flag1 =
 				posBySuffixCase1Helper(unknownWord);
 				// boolean flag2 =
 				posBySuffixCase2Helper(unknownWord);
 			}
 		}
 
 		myLogger.trace("Quite posBySuffix");
 	}
 
 	/**
 	 * Set the certaintyU and certaintyL value of every entry in WordPOS
 	 * collection to be 0
 	 * 
 	 * @param dh
 	 *            DataHolder handler to update the dataholder and return the
 	 *            updated dataholder
 	 * @return Number of records that have been changed
 	 */
 	public int resetCounts(DataHolder dh) {
 		int count = 0;
 		Iterator<Entry<WordPOSKey, WordPOSValue>> iter = dh
 				.getWordPOSHolderIterator();
 		while (iter.hasNext()) {
 			Entry<WordPOSKey, WordPOSValue> wordPOSObject = iter.next();
 			wordPOSObject.getValue().setCertiantyU(0);
 			wordPOSObject.getValue().setCertiantyL(0);
 			count++;
 		}
 
 		return count;
 	}
 
 	public boolean posBySuffixCase1Helper(String unknownWord) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.posBySuffix");
 
 		String pattern1 = "^[a-z_]+(" + Constant.SUFFIX + ")$";
 		myLogger.debug("Pattern1: " + pattern1);
 
 		if (unknownWord.matches(pattern1)) {
 			Matcher matcher = Pattern
 					.compile("(.*?)(" + Constant.SUFFIX + ")$").matcher(
 							unknownWord);
 			if ((unknownWord.matches("^[a-zA-Z0-9_-]+$")) && matcher.matches()) {
 				myLogger.debug("posBySuffix - check word: " + unknownWord);
 				String base = matcher.group(1);
 				String suffix = matcher.group(2);
 				if (this.containSuffix(unknownWord, base, suffix)) {
 					myLogger.debug("Pass\n");
 					this.myDataHolder.updateDataHolder(unknownWord, "b", "*",
 							"wordpos", 0);
 					myLogger.debug("posBySuffix - set word: " + unknownWord);
 					return true;
 				} else {
 					myLogger.debug("Not Pass\n");
 				}
 			}
 		}
 		return false;
 	}
 
 	public boolean posBySuffixCase2Helper(String unknownWord) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.posBySuffix");
 
 		String pattern2 = "^[._.][a-z]+"; // , _nerved
 		myLogger.debug("Pattern2: " + pattern2);
 
 		if (unknownWord.matches(pattern2)) {
 			this.myDataHolder.getWordPOSHolder().put(
 					new WordPOSKey(unknownWord, "b"),
 					new WordPOSValue("*", 0, 0, null, null));
 			myLogger.debug("posbysuffix set " + unknownWord
 					+ " a boundary word\n");
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * return false or true depending on if the word contains the suffix as the
 	 * suffix
 	 * 
 	 * @param word
 	 * @param base
 	 * @param suffix
 	 * @return
 	 */
 	public boolean containSuffix(String word, String base, String suffix) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.posBySuffix.containSuffix");
 		myLogger.trace("Enter containSuffix");
 
 		boolean flag = false; // return value
 		boolean wordInWN = false; // if this word is in WordNet
 		boolean baseInWN = false;
 		WordNetPOSKnowledgeBase myWN = this.myLearnerUtility
 				.getWordNetPOSKnowledgeBase();
 
 		// check base
 		if (base.length() == 0) {
 			myLogger.trace("case 0");
 			return true;
 		}
 
 		base.replaceAll("_", ""); // cup_shaped
 
 		if (myWN.contains(word)) {
 			myLogger.trace("case 1.1");
 			wordInWN = true; // word is in WordNet
 		} else {
 			myLogger.trace("case 1.2");
 			wordInWN = false;
 		}
 
 		if (myWN.contains(base)) {
 			myLogger.trace("case 2.1");
 			baseInWN = true;
 		} else {
 			myLogger.trace("case 2.2");
 			baseInWN = false;
 		}
 
 		// if WN pos is adv, return 1: e.g. ly, or if $base is in
 		// unknownwords table
 		if (suffix.equals("ly")) {
 			myLogger.trace("case 3.1");
 			if (wordInWN) {
 				if (myWN.isAdverb(word)) {
 					return true;
 				}
 			}
 			// if the word is in unknown word set, return true
 			if (this.myDataHolder.getUnknownWordHolder().containsKey(base)) {
 				return true;
 			}
 		}
 
 		// if WN recognize superlative, comparative adjs, return 1: e.g. er, est
 		else if (suffix.equals("er") || suffix.equals("est")) {
 			myLogger.trace("case 3.2");
 			if (wordInWN) {
 				boolean case1 = !myWN.isAdjective(word);
 				boolean case2 = myWN.isAdjective(base);
 				if (case1 && case2) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 		}
 
 		// if $base is in WN or unknownwords table, or if $word has sole pos
 		// adj in WN, return 1: e.g. scalelike
 		else {
 			myLogger.trace("case 3.3");
 			if (myWN.isSoleAdjective(word)) {
 				return true;
 			}
 			if (baseInWN) {
 				return true;
 			}
 			if (this.myDataHolder.getUnknownWordHolder().containsKey(base)) {
 				return true;
 			}
 		}
 
 		return flag;
 	}
 
 	public void markupByPattern() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.markupByPattern");
 		myLogger.trace("Enter markupByPattern");
 
 		int size = this.myDataHolder.getSentenceHolder().size();
 
 		for (int i = 0; i < size; i++) {
 			boolean flag = markupByPatternHelper(this.myDataHolder
 					.getSentenceHolder().get(i));
 			if (flag) {
 				myLogger.debug("Updated Sentence #" + i);
 			}
 		}
 		myLogger.trace("Quite markupByPattern");
 	}
 
 	public boolean markupByPatternHelper(SentenceStructure sentence) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("markupByPattern");
 		// case 1
 		if (sentence.getOriginalSentence().matches("^x=.*")) {
 			myLogger.trace("Case 1");
 			sentence.setTag("chromosome");
 			sentence.setModifier("");
 			return true;
 		}
 		// case 2
 		else if (sentence.getOriginalSentence().matches("^2n=.*")) {
 			myLogger.trace("Case 2");
 			sentence.setTag("chromosome");
 			sentence.setModifier("");
 			return true;
 		}
 		// case 3
 		else if (sentence.getOriginalSentence().matches("^x .*")) {
 			myLogger.trace("Case 3");
 			sentence.setTag("chromosome");
 			sentence.setModifier("");
 			return true;
 		}
 		// case 4
 		else if (sentence.getOriginalSentence().matches("^2n .*")) {
 			myLogger.trace("Case 4");
 			sentence.setTag("chromosome");
 			sentence.setModifier("");
 			return true;
 		}
 		// case 5
 		else if (sentence.getOriginalSentence().matches("^2 n.*")) {
 			myLogger.trace("Case 5");
 			sentence.setTag("chromosome");
 			sentence.setModifier("");
 			return true;
 		}
 		// case 6
 		else if (sentence.getOriginalSentence().matches("^fl.*")) {
 			myLogger.trace("Case 6");
 			sentence.setTag("flowerTime");
 			sentence.setModifier("");
 			return true;
 		}
 		// case 7
 		else if (sentence.getOriginalSentence().matches("^fr.*")) {
 			myLogger.trace("Case 7");
 			sentence.setTag("fruitTime");
 			sentence.setModifier("");
 			return true;
 		}
 		return false;
 	}
 
 	// private String IGNOREPTN ="(IGNOREPTN)"; //disabled
 	public void markupIgnore() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.markupIgnore");
 		myLogger.trace("Enter markupIgnore");
 
 		for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 			boolean flag = markupIgnoreHelper(this.myDataHolder
 					.getSentenceHolder().get(i));
 			if (flag) {
 				myLogger.debug("Updated Sentence #" + i);
 			}
 		}
 
 		myLogger.trace("Quite markupIgnore");
 	}
 
 	public boolean markupIgnoreHelper(SentenceStructure sentence) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("markupIgnore");
 
 		String thisOriginalSentence = sentence.getOriginalSentence();
 		String pattern = "(^|^ )" + Constant.IGNOREPTN + ".*$";
 		if (thisOriginalSentence.matches(pattern)) {
 			sentence.setTag("ignore");
 			sentence.setModifier("");
 			myLogger.trace("Set Tag to \"ignore\", Modifier to \"\"");
 
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param status
 	 *            "start" or "normal"
 	 * @return
 	 */
 	public int discover(String status) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.discover");
 
 		myLogger.trace("Enter Discover - Status: " + status);
 
 		int newDisc = 0;
 
 		// this.myDataHolder.printHolder(DataHolder.SENTENCE);
 
 		for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 			SentenceStructure sentEntry = this.myDataHolder.getSentenceHolder()
 					.get(i);
 			// sentid
 			String thisSentence = sentEntry.getSentence();
 			String thisLead = sentEntry.getLead();
 			String thisTag = sentEntry.getTag();
 			String thisStatus = sentEntry.getStatus();
 			// if (!(thisTag == null || !thisTag.equals("ignore")
 
 			// myLogger.debug("Tag: "+thisTag);
 
 			if ((!StringUtils.equals(thisTag, "ignore") || (thisTag == null))
 					&& thisStatus.equals(status)) {
 
 				myLogger.debug("Sentence #: " + i);
 				myLogger.debug("Lead: " + thisLead);
 
 				myLogger.debug("Tag: " + thisTag);
 
 				myLogger.debug("Sentence: " + thisSentence);
 				// tag is not null
 				if (isMarked(this.myDataHolder.getSentenceHolder().get(i))) {
 					myLogger.debug("Not Pass");
 					continue;
 				}
 				// tag is null
 				else {
 					myLogger.debug("Pass");
 				}
 
 				String[] startWords = thisLead.split("\\s+");
 				myLogger.debug("startWords: " + startWords.toString());
 
 				String pattern = buildPattern(startWords);
 
 				if (pattern != null) {
 					myLogger.debug("Build pattern [" + pattern
 							+ "] from starting words [" + thisLead + "]");
 					// IDs of untagged sentences that match the pattern
 					Set<Integer> matched = matchPattern(pattern, status, false);
 					int round = 0;
 					int numNew = 0;
 
 					do {
 						numNew = ruleBasedLearn(matched);
 						newDisc = newDisc + numNew;
 						myLogger.trace("Round: " + round);
 						round++;
 					} while (numNew > 0);
 				} else {
 					myLogger.debug("Build no pattern from starting words ["
 							+ thisLead + "]");
 				}
 			}
 		}
 
 		myLogger.trace("Return " + newDisc);
 		myLogger.trace("Quite discover");
 		return newDisc;
 	}
 
 	/**
 	 * A helper of method discover(). Check if the tag of the i-th sentence is
 	 * NOT null
 	 * 
 	 * @param sentence
 	 *            the sentence to check
 	 * @return if the tag of the i-th sentence is NOT null, returns true;
 	 *         otherwise returns false
 	 */
 	public boolean isMarked(SentenceStructure sentence) {
 		String thisTag = sentence.getTag();
 
 		if (thisTag != null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * build a pattern based on existing checked word set, and the start words
 	 * 
 	 * @param startWords
 	 * @return a pattern. If no pattern is generated, return null
 	 */
 	public String buildPattern(String[] startWords) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.discover.buildPattern");
 
 		myLogger.trace("Enter buildPattern");
 		myLogger.trace("Start Words: " + startWords);
 
 		Set<String> newWords = new HashSet<String>();
 		String temp = "";
 		String prefix = "\\w+\\s";
 		String pattern = "";
 
 		Set<String> checkedWords = this.checkedWordSet;
 		myLogger.trace("checkedWords: " + checkedWords);
 
 		for (int i = 0; i < startWords.length; i++) {
 			String word = startWords[i];
 			// This is not very sure, need to make sure - Dongye
 			if ((!word.matches("[\\p{Punct}0-9]"))
 					&& (!checkedWords.contains(word))) {
 				temp = temp + word + "|";
 				newWords.add(word);
 			}
 		}
 		myLogger.trace("temp: " + temp);
 
 		// no new words
 		if (temp.length() == 0) {
 			myLogger.trace("No new words");
 			myLogger.trace("Return null");
 			myLogger.trace("Quite buildPattern");
 			myLogger.trace("\n");
 			return null;
 		} else {
 
 			// remove the last char, which is a '|'
 			temp = temp.substring(0, temp.length() - 1);
 		}
 
 		temp = "\\b(?:" + temp + ")\\b";
 		pattern = "^" + temp + "|";
 
 		for (int j = 0; j < this.NUM_LEAD_WORDS - 1; j++) {
 			temp = prefix + temp;
 			pattern = pattern + "^" + temp + "|";
 		}
 		myLogger.trace("Pattern: " + pattern);
 
 		pattern = pattern.substring(0, pattern.length() - 1);
 		pattern = "(?:" + pattern + ").*$";
 		checkedWords.addAll(newWords);
 		this.checkedWordSet = checkedWords;
 
 		myLogger.trace("Return Pattern: " + pattern);
 		myLogger.trace("Quite buildPattern");
 		myLogger.trace("\n");
 		return pattern;
 	}
 
 	/**
 	 * Find the IDs of the sentences that matches the pattern
 	 * 
 	 * @param pattern
 	 * @param status
 	 * @param hasTag
 	 * @return a set of sentence IDs of the sentences that matches the pattern
 	 */
 	public Set<Integer> matchPattern(String pattern, String status,
 			boolean hasTag) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.discover.matchPattern");
 
 		myLogger.trace("Enter matchPattern");
 		myLogger.trace("Pattern: " + pattern);
 		myLogger.trace("Status: " + status);
 		myLogger.trace("HasTag: " + hasTag);
 
 		Set<Integer> matchedIDs = new HashSet<Integer>();
 
 		for (int i = 0; i < this.myDataHolder.getSentenceHolder().size(); i++) {
 			SentenceStructure sent = this.myDataHolder.getSentenceHolder().get(
 					i);
 			String thisSentence = sent.getSentence();
 			String thisStatus = sent.getStatus();
 			String thisTag = sent.getTag();
 
 			boolean a = hasTag;
 			boolean b = (thisTag == null);
 
 			if ((a ^ b) && (StringUtils.equals(status, thisStatus))) {
 				Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
 				Matcher m = p.matcher(thisSentence);
 				if (m.lookingAt()) {
 					myLogger.debug("Push Sentence #" + i);
 					myLogger.debug("Sentence: " + thisSentence);
 					myLogger.debug("Status: " + thisStatus);
 					myLogger.debug("Tag: " + thisTag);
 					myLogger.debug("\n");
 
 					matchedIDs.add(i);
 				}
 			}
 		}
 
 		myLogger.trace("Return IDs: " + matchedIDs);
 		myLogger.trace("Quite matchPattern");
 		myLogger.trace("\n");
 		return matchedIDs;
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
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.discover.ruleBasedLearn");
 
 		myLogger.trace("Enter ruleBasedLearn");
 		myLogger.trace("Matched IDs: " + matched);
 
 		int sign = 0;
 
 		Iterator<Integer> iter = matched.iterator();
 		while (iter.hasNext()) {
 			int sentID = iter.next().intValue();
 			SentenceStructure sentence = this.myDataHolder.getSentenceHolder()
 					.get(sentID);
 			if (!isMarked(sentence)) {
 				StringAndInt tagAndNew = null;
 				String tag = null;
 				int numNew = 0;
 
 				tagAndNew = this.myLearnerUtility.doIt(this.myDataHolder, sentID);
 				tag = tagAndNew.getString();
 				numNew = tagAndNew.getInt();
 
 				this.myLearnerUtility.tagSentence(this.myDataHolder, this.myConfiguration.getMaxTagLength(), sentID, tag);
 				sign = sign + numNew;
 			}
 		}
 
 		myLogger.trace("Return: " + sign);
 		myLogger.trace("Quit ruleBaseLearn");
 		myLogger.trace("\n");
 
 		return sign;
 	}
 
 
 
 
 
 
 
 
 
 
 	/**
 	 * 
 	 */
 	public void additionalBootstrapping() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.additionalBootStrapping");
 		myLogger.trace("[additionalBootStrapping]Start");
 
 		// this.myDataHolder.printHolder(DataHolder.SENTENCE);
 
 		int flag = 0;
 
 		do {
 			myLogger.trace(String.format("Enter one do-while loop iteration"));
 			flag = 0;
 
 			// warmup markup
 			int cmReturn = wrapupMarkup();
 			myLogger.trace(String
 					.format("wrapupMarkup() returned %d", cmReturn));
 			flag += cmReturn;
 
 			// one lead word markup
 			Set<String> tags = myDataHolder.getCurrentTags();
 			myLogger.trace(tags.toString());
 			int omReturn = oneLeadWordMarkup(tags);
 			myLogger.trace(String.format("oneLeadWordMarkup() returned %d",
 					omReturn));
 			flag += omReturn;
 
 			// doit markup
 			int dmReturn = this.myLearnerUtility.doItMarkup(this.myDataHolder, this.myConfiguration.getMaxTagLength());
 			myLogger.trace(String.format("doItMarkup() returned %d", dmReturn));
 			flag += dmReturn;
 
 			myLogger.trace(String.format("Quite this iteration with flag = %d",
 					flag));
 		} while (flag > 0);
 
 		myLogger.trace("[additionalBootStrapping]End");
 	}
 
 	/**
 	 * In the sentence collections, search for such sentence, whose lead is
 	 * among the tags passed in, and add the lead into word POS collections as a
 	 * noun
 	 * 
 	 * @param tags
 	 *            a set of all tags in the tagged sentences in the sentence
 	 *            collection
 	 * @return the numbet of updates made
 	 */
 	public int oneLeadWordMarkup(Set<String> tags) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger
 				.getLogger("learn.additionalBootStrapping.oneLeadWordMarkup");
 		// String tags = StringUtility.joinList("|", tags);
 		int sign = 0;
 		myLogger.trace(String.format("Enter (%s)", tags));
 
 		Iterator<SentenceStructure> iter = this.myDataHolder
 				.getSentenceHolder().iterator();
 
 		while (iter.hasNext()) {
 			SentenceStructure sentence = iter.next();
 			int ID = sentence.getID();
 			String tag = sentence.getTag();
 			String lead = sentence.getLead();
 
 			if ((tag == null)
 					&& (!(StringUtility.createMatcher(lead, ".* .*").find()))) {
 				if (tags.contains(lead)) {
 					this.myLearnerUtility.tagSentence(this.myDataHolder, this.myConfiguration.getMaxTagLength(), ID, lead);
 					myLogger.trace(String.format(
 							"updateDataHolder(%s, n, -, wordpos, 1)", lead));
 					sign += myDataHolder.updateDataHolder(lead, "n", "-",
 							"wordpos", 1);
 				}
 			}
 		}
 
 		myLogger.trace("Return: " + sign);
 		return 0;
 	}
 
 	/**
 	 * for the remaining of sentences that do not have a tag yet, look for lead
 	 * word co-ocurrance, use the most freq. co-occured phrases as tags e.g.
 	 * plication induplicate (n times) and plication reduplicate (m times) =>
 	 * plication is the tag and a noun e.g. stigmatic scar basal (n times) and
 	 * stigmatic scar apical (m times) => stigmatic scar is the tag and scar is
 	 * a noun. what about externally like A; externally like B, functionally
 	 * staminate florets, functionally staminate xyz?
 	 * 
 	 * @return
 	 */
 	public int wrapupMarkup() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger
 				.getLogger("learn.additionalBootStrapping.wrapupMarkup");
 		myLogger.trace("Enter");
 
 		int sign = 0;
 		Set<Integer> checkedIDs = new HashSet<Integer>();
 		List<SentenceStructure> sentenceList = new LinkedList<SentenceStructure>();
 
 		for (int id1 = 0; id1 < this.myDataHolder.getSentenceHolder().size(); id1++) {
 			SentenceStructure sentence = this.myDataHolder.getSentenceHolder()
 					.get(id1);
 			String tag = sentence.getTag();
 			String lead = sentence.getLead();
 
 			if ((tag == null)
 					&& (StringUtility.createMatcher(lead, ".* .*").find())) {
 				sentenceList.add(sentence);
 			}
 		}
 
 		SentenceLeadLengthComparator myComparator = new SentenceLeadLengthComparator(
 				false);
 		Collections.sort(sentenceList, myComparator);
 
 		Iterator<SentenceStructure> iter1 = sentenceList.iterator();
 		while (iter1.hasNext()) {
 			SentenceStructure sentence = iter1.next();
 			int ID1 = sentence.getID();
 			String lead = sentence.getLead();
 			// if this sentence has been checked, pass
 			if (checkedIDs.contains(ID1)) {
 				continue;
 			}
 
 			List<String> words = new ArrayList<String>();
 			words.addAll(Arrays.asList(lead.split("\\s+")));
 
 			List<String> sharedHead = new ArrayList<String>();
 			sharedHead.addAll(words.subList(0, words.size() - 1));
 			String match = StringUtility.joinList(" ", sharedHead);
 
 			Set<SentenceStructure> sentenceSet = new HashSet<SentenceStructure>();
 			for (int index = 0; index < this.myDataHolder.getSentenceHolder()
 					.size(); index++) {
 				SentenceStructure thisSentence = this.myDataHolder
 						.getSentenceHolder().get(index);
 				String thisLead = thisSentence.getLead();
 				String tag = thisSentence.getTag();
 				String pTemp = "^" + match + " [\\S]+$";
 				myLogger.trace(thisLead);
 				myLogger.trace(pTemp);
 
 				// if ((tag==null) && StringUtility.isMatchedNullSafe(pTemp,
 				// thisLead)) {
 				if ((tag == null)
 						&& StringUtility.isMatchedNullSafe(thisLead, pTemp)) {
 					if (!StringUtils.equals(thisLead, lead)) {
 						sentenceSet.add(thisSentence);
 					}
 				}
 			}
 
 			if (sentenceSet.size() > 1) {
 				String ptn = this.myLearnerUtility.getPOSptn(this.myDataHolder, sharedHead);
 				String wnPOS = this.myLearnerUtility.getWordFormUtility()
 						.checkWN(sharedHead.get(sharedHead.size() - 1), "pos");
 
 				myLogger.trace("ptn: " + ptn);
 				myLogger.trace("wnPOS: " + wnPOS);
 
 				if ((StringUtility.createMatcher(ptn, "[nsp]$").find())
 						|| ((StringUtility.createMatcher(ptn, "\\?$").find()) && (StringUtility
 								.createMatcher(wnPOS, "n").find()))) {
 
 					Iterator<SentenceStructure> iter2 = sentenceSet.iterator();
 					while (iter2.hasNext()) {
 						SentenceStructure thisSentence = iter2.next();
 						int ID = thisSentence.getID();
 						String thisLead = thisSentence.getLead();
 
 						List<String> words2 = new ArrayList<String>();
 						words2.addAll(Arrays.asList(thisLead.split("\\s+")));
 
 						// case 1
 						boolean case1 = false;
 						boolean case2 = false;
 						case1 = words2.size() > sharedHead.size();
 						if (case1) {
 							List<String> checkWord = new ArrayList<String>();
 							checkWord.add(words2.get(sharedHead.size()));
 							case2 = StringUtility.createMatcher(
 									this.myLearnerUtility.getPOSptn(this.myDataHolder, checkWord), "[psn]").find();
 						}
 
 						if (case1 && case2) {
 							myLogger.trace("Case 1");
 							String nb = words2.size() >= sharedHead.size() + 2 ? words2
 									.get(sharedHead.size() + 1) : "";
 							words2 = StringUtility.stringArraySplice(words2, 0,
 									sharedHead.size() + 1);
 							String nmatch = StringUtility.joinList(" ", words2);
 
 							this.myLearnerUtility.tagSentence(this.myDataHolder, this.myConfiguration.getMaxTagLength(),ID, nmatch);
 							myLogger.trace(String.format("tag (%d, %s)", ID,
 									nmatch));
 							this.myLearnerUtility.tagSentence(this.myDataHolder, this.myConfiguration.getMaxTagLength(),ID1, match);
 							myLogger.trace(String.format("tag (%d, %s)", ID1,
 									match));
 
 							String updatedWord = words2.get(words2.size() - 1);
 							int update1 = this.myDataHolder.updateDataHolder(
 									updatedWord, "n", "-", "wordpos", 1);
 							sign += update1;
 							myLogger.trace(String.format("update (%s)",
 									updatedWord));
 
 							if (!StringUtils.equals(nb, "")) {
 								int update2 = this.myDataHolder
 										.updateDataHolder(nb, "b", "",
 												"wordpos", 1);
 								sign += update2;
 								myLogger.trace(String.format("update (%s)", nb));
 							}
 
 							updatedWord = words.get(words.size() - 1);
 							int update3 = this.myDataHolder.updateDataHolder(
 									words.get(words.size() - 1), "b", "",
 									"wordpos", 1);
 							sign += update3;
 							myLogger.trace(String.format("update (%s)",
 									updatedWord));
 						}
 						// case 2
 						else {
 							myLogger.trace("Case 2");
 							String b = words2.size() >= sharedHead.size() + 1 ? words2
 									.get(sharedHead.size()) : "";
 
 							this.myLearnerUtility.tagSentence(this.myDataHolder, this.myConfiguration.getMaxTagLength(),ID, match);
 							this.myLearnerUtility.tagSentence(this.myDataHolder, this.myConfiguration.getMaxTagLength(),ID1, match);
 
 							// if (sharedHead.get(sharedHead.size() -
 							// 1).equals("tissue")) {
 							// System.out.println();
 							// }
 
 							int update1 = this.myDataHolder.updateDataHolder(
 									sharedHead.get(sharedHead.size() - 1), "n",
 									"-", "wordpos", 1);
 							sign += update1;
 							if (!StringUtils.equals(b, "")) {
 								int update2 = this.myDataHolder
 										.updateDataHolder(b, "b", "",
 												"wordpos", 1);
 								sign += update2;
 							}
 							int update3 = this.myDataHolder.updateDataHolder(
 									words.get(words.size() - 1), "b", "",
 									"wordpos", 1);
 							sign += update3;
 
 						}
 						checkedIDs.add(ID);
 					}
 				} else {
 					Iterator<SentenceStructure> iter2 = sentenceSet.iterator();
 					while (iter2.hasNext()) {
 						SentenceStructure thisSentence = iter2.next();
 						int ID = thisSentence.getID();
 						checkedIDs.add(ID);
 					}
 				}
 			} else {
 				checkedIDs.add(ID1);
 			}
 		}
 
 		myLogger.trace("Return " + sign);
 		return sign;
 	}
 
 	/**
 	 * check if the lead has the head in the beginning of it
 	 * 
 	 * @param head
 	 * @param lead
 	 * @return true if it has, false if it does not have
 	 */
 	public boolean hasHead(List<String> head, List<String> lead) {
 
 		// null case
 		if (head == null || lead == null) {
 			return false;
 		}
 
 		int headSize = head.size();
 		int leadSize = lead.size();
 		if (headSize > leadSize) {
 			return false;
 		}
 
 		for (int i = 0; i < headSize; i++) {
 			if (!StringUtils.equals(head.get(i), lead.get(i))) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 
 
 	public void unknownWordBootstrapping() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.unknownWordBootstrapping");
 		myLogger.trace("[unknownWordBootstrapping]Start");
 
 		unknownWordBootstrappingPreprocessing();
 		unknownWordBootstrappingMain();
 		unknownWordBootstrappingPostprocessing();
 
 		myLogger.trace("[unknownWordBootstrapping]End");
 	}
 
 	public void unknownWordBootstrappingPreprocessing() {
 		this.myLearnerUtility.tagAllSentences(this.myDataHolder, "singletag",
 				"sentence");
 	}
 
 	public void unknownWordBootstrappingMain() {
 		String plMiddle = "(ee)";
 
 		int newInt = 0;
 		do {
 			// this.unknownWordBootstrappingGetUnknownWord(plMiddle);
 		} while (newInt > 0);
 	}
 
 	public void unknownWordBootstrappingPostprocessing() {
 		// pistillate_zone
 		// get all nouns from wordPOS holder
 		Set<String> POSTags = new HashSet<String>();
 		POSTags.add("p");
 		POSTags.add("s");
 		Set<String> nouns = this.getDataHolder().getWordsFromWordPOSByPOSs(
 				POSTags);
 
 		// get boudaries
 		Set<String> boundaries = new HashSet<String>();
 		Set<String> words = this.getDataHolder().getWordsFromUnknownWord(
 				"^.*_.*$", true, "^unknown$", true);
 		Iterator<String> wordIter = words.iterator();
 		String pattern = "_(" + StringUtils.join(nouns, "|") + ")$";
 		while (wordIter.hasNext()) {
 			String word = wordIter.next();
 			Pattern p1 = Pattern.compile("^[a-zA-Z0-9_-]+$");
 			Matcher m1 = p1.matcher(word);
 			Pattern p2 = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
 			Matcher m2 = p2.matcher(word);
 			if (m1.matches() && (!m2.matches())) {
 				if (!StringUtility.createMatcher(word,
 						"\\b(" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b").find()) {
 					boundaries.add(word);
 				}
 				this.getDataHolder().updateDataHolder(word, "b", "", "wordpos",
 						1);
 			}
 		}
 
 		// if the boundaries is not empty
 		if (boundaries.size() > 0) {
 			Iterator<SentenceStructure> iter = this.getDataHolder()
 					.getSentenceHolderIterator();
 			while (iter.hasNext()) {
 				SentenceStructure sentenceItem = iter.next();
 				String tag = sentenceItem.getTag();
 				String sentence = sentenceItem.getSentence();
 				int sentenceID = sentenceItem.getID();
 
 				if ((!(StringUtils.equals(tag, "ignore")) || (tag == null))
 						&& (StringUtility.createMatcher(sentence, "(^| )("
 								+ StringUtils.join(boundaries, "|") + ") ")
 								.find())) {
 					KnownTagCollection tags = new KnownTagCollection(null,
 							null, null, boundaries, null, null);
 					sentence = this.myLearnerUtility.annotateSentence(sentence,
 							tags, this.myDataHolder.getBMSWords());
 					SentenceStructure updatedSentence = this.getDataHolder()
 							.getSentence(sentenceID);
 					updatedSentence.setSentence(sentence);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Utilities
 	 * 
 	 * @return
 	 */
 	public Set<String> getCheckedWordSet() {
 		return this.checkedWordSet;
 	}
 
 	public void setCheckedWordSet(Set<String> wordSet) {
 		this.checkedWordSet = wordSet;
 	}
 
 
 
 
 
 
 
 	public void separateModifierTag(DataHolder dataholderHandler) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.separateModifierTag");
 
 		List<SentenceStructure> sentences = dataholderHandler
 				.getSentencesByTagPattern("^.* .*$");
 
 		for (SentenceStructure sentenceItem : sentences) {
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 			String tag = sentenceItem.getTag();
 			myLogger.trace("ID: " + sentenceID);
 			myLogger.trace("Sentence: " + sentence);
 			myLogger.trace("Tag: " + tag);
 
 			// case 1
 			String tagBackup = "" + tag;
 			// if (StringUtility.isMatchedNullSafe("\\w+", tagBackup)) {
 			if (StringUtility.isMatchedNullSafe(tagBackup, "\\w+")) {
 				myLogger.trace("Case 1");
 				if (!StringUtility.isMatchedNullSafe(tagBackup,
 						String.format("\\b(%s)\\b", this.myLearnerUtility.getConstant().STOP))) {
 
 					List<String> words = new LinkedList<String>();
 					words.addAll(Arrays.asList(tagBackup.split("\\s+")));
 					tag = words.get(words.size() - 1);
 
 					String modifier = "";
 					if (words.size() > 1) {
 						modifier = StringUtils.join(
 								StringUtility.stringArraySplice(words, 0,
 										words.size() - 1), " ");
 					}
 
 					if (sentenceID == 22) {
 						System.out.println();
 					}
 					if (StringUtility.isMatchedNullSafe(tag, "\\w")) {
 						// case 1.1
 						myLogger.trace("Case 1.1");
 						dataholderHandler.tagSentenceWithMT(sentenceID,
 								sentence, modifier, tag, "separatemodifiertag");
 					} else {
 						// case 1.2
 						myLogger.trace("Case 1.2");
 						myLogger.trace(sentenceID);
 						dataholderHandler.tagSentenceWithMT(sentenceID,
 								sentence, null, tag, "separatemodifiertag");
 					}
 				}
 
 			}
 			// case 2
 			else {
 				// treat them case by case
 				// case 2: in some species, abaxially with =>NULL
 				myLogger.trace("Case 2");
 				if ((StringUtility.isMatchedNullSafe(tagBackup, "^in"))
 						&& (StringUtility.isMatchedNullSafe(tagBackup,
 								"\\b(with|without)\\b"))) {
 					myLogger.trace("Case 2.1");
 					dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 							"", null, "separtemodifiertag");
 				} else {
 					myLogger.trace("Case 2.2");
 					String tagWithStopWordsReplaced = "" + tagBackup;
 					if (tagWithStopWordsReplaced != null) {
 						Pattern p = Pattern.compile("@ ([^@]+)$");
 						Matcher m = p.matcher(tagWithStopWordsReplaced);
 						if (m.find()) {
 							String tg = m.group(1);
 							ArrayList<String> tagWords = new ArrayList<String>();
 							tagWords.addAll(Arrays.asList(tg.split("\\s+")));
 							tag = tagWords.get(tagWords.size() - 1);
 							String modifier = "";
 							if (tagWords.size() > 1) {
 								modifier = StringUtils.join(StringUtility
 										.stringArraySplice(tagWords, 0,
 												tagWords.size()), " ");
 							}
 
 							if (StringUtility.isMatchedNullSafe(tag, "\\w")) {
 								myLogger.trace("Case 2.2.1");
 								dataholderHandler.tagSentenceWithMT(sentenceID,
 										sentence, modifier, tag,
 										"separatemodifiertag");
 							} else {
 								myLogger.trace("Case 2.2.2");
 								dataholderHandler.tagSentenceWithMT(sentenceID,
 										sentence, "", null,
 										"separatemodifiertag");
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void resolveNMB(DataHolder dataholderHandler) {
 		Set<String> tags = dataholderHandler.getSentenceTags();
 		Iterator<Entry<WordPOSKey, WordPOSValue>> wordPOSIter = dataholderHandler
 				.getWordPOSHolderIterator();
 
 		// get words
 		Set<String> words = new HashSet<String>();
 		while (wordPOSIter.hasNext()) {
 			Entry<WordPOSKey, WordPOSValue> wordPOSEntry = wordPOSIter.next();
 			if (StringUtils.equals(wordPOSEntry.getKey().getPOS(), "b")) {
 				String word = wordPOSEntry.getKey().getWord();
 				boolean case1 = dataholderHandler.getWordPOSHolder()
 						.containsKey(new WordPOSKey(word, "s"));
 				boolean case2 = tags.contains(word);
 				if (case1 || case2) {
 					words.add(word);
 				}
 			}
 		}
 
 		// update wordPOS holder and / or sentence holder
 		Iterator<String> wordIter = words.iterator();
 		while (wordIter.hasNext()) {
 			String word = wordIter.next();
 
 			if (dataholderHandler.getModifierHolder().containsKey(word)) {
 				// remove N role
 				dataholderHandler.getWordPOSHolder().remove(
 						new WordPOSKey(word, "s"));
 
 				// reset sentence tags
 				Iterator<SentenceStructure> sentenceIter = dataholderHandler
 						.getSentenceHolderIterator();
 				while (sentenceIter.hasNext()) {
 					SentenceStructure sentenceItem = sentenceIter.next();
 					String tag = sentenceItem.getSentence();
 					boolean case1 = StringUtils.equals(tag, word);
 					boolean case2 = StringUtility.isMatchedNullSafe(tag, " "
 							+ word);
 					if (case1 || case2) {
 						sentenceItem.setModifier("");
 						sentenceItem.setTag(null);
 					}
 				}
 
 				dataholderHandler.getBMSWords().add(word);
 			}
 		}
 
 		// retag clauses with <N><M><B> tags
 		Iterator<SentenceStructure> sentenceIter = dataholderHandler
 				.getSentenceHolderIterator();
 		while (sentenceIter.hasNext()) {
 			SentenceStructure sentenceItem = sentenceIter.next();
 			String sentence = sentenceItem.getSentence();
 			sentence = sentence.replaceAll("<[ON]><M><B>", "<M><B>");
 			sentence = sentence.replaceAll("</B></M></[ON]>", "</B></M>");
 			sentenceItem.setSentence(sentence);
 		}
 
 	}
 
 	public void setAndOr(DataHolder dataholderHandler) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.separateModifierTag");
 		myLogger.debug("Tag and/or sentences andor");
 
 		String ptn1 = "^(?:[mbq,]{0,10}[onp]+(?:,|(?=&)))+&(?:[mbq,]{0,10}[onp]+)"; // n,n,n&n
 		String ptn2 = "^(?:[mbq,]{0,10}(?:,|(?=&)))+&(?:[mbq,]{0,10})[onp]+"; // m,m,&mn
 
 		Iterator<SentenceStructure> sentenceIter = dataholderHandler
 				.getSentenceHolderIterator();
 		while (sentenceIter.hasNext()) {
 			SentenceStructure sentenceItem = sentenceIter.next();
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 			String lead = sentenceItem.getLead();
 			if (isIsAndOrSentence(sentenceID, sentence, lead, ptn1, ptn2)) {
 				sentenceItem.setTag("andor");
 			}
 		}
 	}
 
 	public boolean isIsAndOrSentence(int sentenceID, String sentence,
 			String lead, String ptn1, String ptn2) {
 
 		Set<String> token = new HashSet<String>();
 		token.addAll(Arrays.asList("and or nor".split(" ")));
 		token.add("\\");
 		token.add("and / or");
 
 		int limit = 80;
 
 		List<String> words = new ArrayList<String>();
 		words.addAll(Arrays.asList(sentence.split(" ")));
 
 		String sentencePtn = this.getLearnerUtility().getSentencePtn(
 				myDataHolder, token, limit, words);
 
 		if (sentencePtn == null) {
 			return false;
 		}
 
 		boolean result = isIsAndOrSentenceHelper(words, sentencePtn, ptn1, ptn2);
 
 		return result;
 	}
 
 	public boolean isIsAndOrSentenceHelper(List<String> words,
 			String sentencePtn, String ptn1, String ptn2) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.isIsAndOrSentence");
 
 		sentencePtn = sentencePtn.toLowerCase();
 		// ignore the distinction between type modifiers and modifiers
 		sentencePtn = sentencePtn.replaceAll("t", "m");
 
 		Pattern p1 = Pattern.compile(ptn1);
 		Matcher m1 = p1.matcher(sentencePtn);
 
 		Pattern p2 = Pattern.compile(ptn2);
 		Matcher m2 = p2.matcher(sentencePtn);
 
 		int end = -1;
 		boolean case1 = false;
 		boolean case2 = false;
 
 		if (m1.find()) {
 			end = m1.end();
 			case1 = true;
 		}
 
 		if (m2.find()) {
 			end = m2.end();
 			case2 = true;
 		}
 
 		if (case1 || case2) {
 			String matchedWords = StringUtils.join(words.subList(0, end), " ");
 			String regex = String.format("\\b(%s)\\b", this.myLearnerUtility.getConstant().PREPOSITION);
 			if (StringUtility.isMatchedNullSafe(matchedWords, regex)) {
 				myLogger.trace("Case 1");
 				return false;
 			}
 			myLogger.trace("Case 2");
 			return true;
 		}
 		myLogger.trace("Case 3");
 		return false;
 	}
 	
 	public void adjectiveSubjectBootstrapping(DataHolder dataholderHandler) {
 		int flag = 0;
 		int count = 0;
 		
 		do {
 			// tag all sentences
 			this.myLearnerUtility.tagAllSentences(dataholderHandler, "singletag", "sentence");
 			
 			// adjective subject markup: may discover new modifier, new boundary, and new nouns
 			int res1 = this.adjectiveSubjects(dataholderHandler);
 			flag += res1;
 			
 			// work on tag='andor' clauses, move to the main bootstrapping
 			int res2 = discoverNewModifiers(dataholderHandler);
 			flag += res2;
 			
 			int res3 = this.handleAndOr(dataholderHandler);
 			flag += res3;			
 			dataholderHandler.untagSentences();
 			
 			int res4 = this.myLearnerUtility.doItMarkup(this.myDataHolder, this.myConfiguration.getMaxTagLength());
 			
 		} while (flag > 0);
 		
 		// reset unsolvable andor to NULL
 		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
 			String tag = sentenceItem.getTag();
 			if (StringUtils.equals(tag, "andor")) {
 				sentenceItem.setTag(null);
 			}
 		}
 		
 		// cases releazed from andor[m&mn] may be marked by adjectivesubjects
 		this.myLearnerUtility.tagAllSentences(dataholderHandler, "singletag", "sentence");
 		this.adjectiveSubjects(dataholderHandler);
 	}
 	
 	/**
 	 * works on annotated sentences that starts with a M in all non-ignored
 	 * sentences, find sentences that starts with a modifer <m> followed by a
 	 * boundary word <b>. (note, if the <B> is a punct mark, this sentence
 	 * should be tagged as ditto) Use the context to find the tag, use the
 	 * modifier as the modifie (markup process, no new discovery). for
 	 * "modifier unknown" pattern, check WNPOS of the "unknown" to decide if
 	 * "unknown" is a structure name (if it is a pl) or a boundary word (may
 	 * have new discoveries). Works on sentences, not leads
 	 * 
 	 * @param dataholderHandler
 	 * @return # of updates
 	 */
 	public int adjectiveSubjects(DataHolder dataholderHandler) {
 		Set<String> typeModifiers = new HashSet<String>();
 		
 		// Part 1: collect evidence for the usage of "modifier boundry":
 		typeModifiers = adjectiveSubjectsPart1(dataholderHandler, typeModifiers);
 		
 		for (String typeModifier : typeModifiers) {
 			if (dataholderHandler.getModifierHolder().containsKey(typeModifier)) {
 				dataholderHandler.getModifierHolder().get(typeModifier)
 						.setIsTypeModifier(true);
 			}
 		}
 		
 		// Part 2: process "typemodifier unknown" patterns
 		int flag = adjectiveSubjectsPart2(dataholderHandler, typeModifiers);
 		
 		return flag;		
 	}
 	
 	public Set<String> adjectiveSubjectsPart1(DataHolder dataholderHandler, Set<String> typeModifiers) {
 		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
 			String sentenceCopy = ""+sentenceItem.getSentence();
 			String tag = sentenceItem.getTag();
 			
 			if (!StringUtils.equals(tag, "ignore") || tag == null) {
 				Pattern p = Pattern.compile(".*?<M>(\\S+)</M> <B>[^,.]+</B> (.*)");
 				Matcher m = p.matcher(sentenceCopy);
 				while (m.find()) {
 					sentenceCopy = m.group(2);
 					String temp = m.group(1);
 					temp = temp.replaceAll("<\\S+?>", "");
 					if (!typeModifiers.contains(temp)) {
 						typeModifiers.add(temp);
 					}
 				}
 			}
 					
 		}
 		
 		return typeModifiers;
 
 	}
 	
 	public int adjectiveSubjectsPart2(DataHolder dataholderHandler,
 			Set<String> typeModifiers) {
 		String pos = null;
 		int flag = 0;
 		
 		
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 			String tag = sentenceItem.getTag();
 			String pattern = "<M>\\S*(" + StringUtils.join(typeModifiers, "|")
 					+ ")\\S*</M> .*";
 			int count = 0;
 			
 			if (((tag == null) || StringUtils.equals(tag, "") || StringUtils
 					.equals(tag, "unknown"))
 					&& adjectiveSubjectsPart2Helper1(sentence, typeModifiers)) {
 				
 				
 				if (sentence != null) {
 					String sentenceCopy = sentence + "";
 					String regex = "(.*?)((?:(\\S+)\\s*(?:and|or|nor|and / or|or / and)\\s*)*(?:<M>\\S+</M>\\s*)+) (\\S+)\\s*(.*)";
 					Pattern p = Pattern.compile(regex);
 					Matcher m = p.matcher(sentenceCopy);
 					while (m.find()) {
 						int knownPOS = 0;
 						String start = m.group(1);
 						String modifier = m.group(2);
 						String newModifier = m.group(3);
 						String word = m.group(4);
 						sentenceCopy = m.group(5);
 
 						// case 1
 						if (!this.myLearnerUtility.getConstant().forbiddenWords
 								.contains(word)) {
 							count++;
 							continue;
 						}
 
 						// case 2
 						if (StringUtility.isMatchedNullSafe(
 								newModifier.toUpperCase(), "<N>")
 								|| StringUtility.isMatchedNullSafe(
 										start.toUpperCase(), "<N>")) {
 							count++;
 						continue;
 						}
 						
 						// case 3
 						boolean c3 = this.myLearnerUtility.getConstant().prepositionWords.contains(word);
 						if (count == 0 
 								&& ((StringUtility.isMatchedNullSafe(word, "[;,]") || c3) 
 										|| (StringUtility.isMatchedNullSafe(word, "[.;,]") 
 												&& !StringUtility.isMatchedNullSafe(sentence, "\\w")))) {
 							// case 3.1
 							// start with a <[BM]>, followed by a <[BM]>
 							if ((StringUtility.isMatchedNullSafe(word,
 									"\\b(with|without|of)\\b"))
 									&& ((StringUtility.isMatchedNullSafe(modifier,
 													"^(<M>)?<B>(<M>)?\\w+(</M)?</B>(</M>)? (?:and|or|nor|and / or|or / and)?\\s*(<[BM]>)+\\w+(</[BM]>)+\\s*$")) 
 									|| (StringUtility.isMatchedNullSafe(modifier, "^(<[BM]>)+\\w+(</[BM]>)+$")))) { 
 								dataholderHandler.tagSentenceWithMT(sentenceID,
 										sentenceCopy, "", "ditto",
 										"adjectivesubject[ditto]");
 								count++;
 								continue;
 							} 
 							// case 3.2
 							// modifier={<M>outer</M> <M><B>pistillate</B></M>} word= <B>,</B> sentence= <N>corollas</N>....
 							// make the last modifier b
 							else {
 								if (modifier != null) {
 									Pattern p2 = Pattern
 											.compile("^(.*) (\\S+)$");
 									Matcher m2 = p2.matcher(modifier);
 									if (m2.find()) {
 										modifier = m2.group(1);
 										String b = m2.group(2);
 										String bCopy = "" + b;
 										b = b.replaceAll("<\\S+?>", "");
 										dataholderHandler.updateDataHolder(b,"b", "", "wordpos", 1);
 										tag = dataholderHandler.getParentSentenceTag(sentenceID);
 										List<String> modifierAndTag = 
 												dataholderHandler.getMTFromParentTag(tag);
 										String modifier2 = modifierAndTag.get(0);
 										tag = modifierAndTag.get(1);
 										modifier = modifier.replaceAll(
 												"<\\S+?>", "");
 										if (StringUtility.isMatchedNullSafe(modifier2, "\\w")) {
 											modifier = modifier + " " + modifier2;
 										}
 										dataholderHandler.tagSentenceWithMT(
 												sentenceID, sentence, modifier,
 												tag, "adjectivesubject[M-B,]");
 										count++;
 										continue;
 									}
 								}
 							}
 						}
 						
 						// case 4
 						// get new modifier from modifiers like
 						// "mid and/or <m>distal</m>"
 						if (!StringUtility.isMatchedNullSafe(newModifier,"<")
 								&& StringUtility.isMatchedNullSafe(newModifier, "\\w")
 								&& StringUtility.isMatchedNullSafe(start,",(?:</B>)?\\s*$")) {
 
 						
 							flag += dataholderHandler.updateDataHolder(newModifier, "m", "", "modifiers", 1);
 //							print "find a modifier [E0]: $newm\n" if $debug;
 						}
 						
 						// case 5
 						// pos = "N"/"B"
 						if (word != null) {
 							Pattern p5 = Pattern.compile("([A-Z])>(<([A-Z])>)?(.*?)<");
 							Matcher m5 = p5.matcher(word);
 							if (m5.find()) {
 								String g1 = m5.group(1);
 								String g2 = m5.group(2);
 								String g3 = m5.group(3);
 								String g4 = m5.group(4);
 								
 								String t1 = g1;
 								String t2 = g3;
 								
 								word = g4;
 								pos = t1 + t2;
 								
 								// if <N><B>, decide on one tag
 								if (pos.length() > 1) {
 									if (StringUtility.isMatchedNullSafe(sentence, "^\\s*<B>[,;:]<\\/B>\\s*<N>")
 											||StringUtility.isMatchedNullSafe(sentence, "^\\s*<B>\\.<\\/B>\\s*$")){
 										pos = "B";
 									}
 									else {
 										pos = "N";
 									}
 								}
 								knownPOS = 1;
 							}
 							else {
 								List<POSInfo> POSs = dataholderHandler.checkPOSInfo(word);
 								pos = POSs.get(0).getPOS();
 							}
 						}
 						
 						pos = StringUtils.equals(pos, "?") ? this.myLearnerUtility.getWordFormUtility().getNumber(word) : pos;
 						
 						// part 6
 						// markup sentid, update pos for word, new modifier
 						if (StringUtils.equals(pos, "p") || StringUtils.equals(pos, "N")) {
 							if (knownPOS != 0) {
 								flag += dataholderHandler.updateDataHolder(word, "p", "-", "wordpos", 1);
 //								/print "update [$word] pos: p\n" if (!$knownpos) && $debug;
 							}
 							
 							if (count == 0 
 									&& (StringUtility.isMatchedNullSafe(start, "^\\S+\\s?(?:and |or |and \\/ or |or \\/ and )?$")
 											||start.length() == 0)) {
 								modifier = start + modifier;
 								modifier = modifier.replaceAll("<\\S+?>", "");
 								word = word.replaceAll("<\\S+?>", "");
 								dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "adjectivesubject[M-N]");
 								// new modifier
 								start = start.replaceAll("\\s*(and |or |and \\/ or |or \\/ and )\\s*", "");
 								start = start.replaceAll("<\\S+?>", "");
 								
 								while (StringUtility.isMatchedNullSafe(start, "^("+this.myLearnerUtility.getConstant().STOP+")\\b")) {
 									start = start.replaceAll("^("+this.myLearnerUtility.getConstant().STOP+")\\b\\s*", "");
 								}
 								
 								if (start.length() > 0) {
 									flag += dataholderHandler.updateDataHolder(start, "m", "", "modifiers", 1);
 									//print "find a modifier [E]: $start\n" if $debug;
 											
 								}
 							}
 						}
 						// not p
 						else {
 							if (knownPOS != 0) {
 								// update pos for word, markup sentid (get tag
 								// from context), new modifier
 								flag += dataholderHandler.updateDataHolder(word, "b", "", "wordpos", 1);
 								// print "update [$word] pos: b\n" if $debug;
 							}
 							
 							if (count == 0 
 									&& (StringUtility.isMatchedNullSafe(start, "^\\S+\\s?(?:and |or |and \\/ or |or \\/ and )?$")
 											||start.length() == 0)) {
 								while (StringUtility.isMatchedNullSafe(start, "^("+this.myLearnerUtility.getConstant().STOP+"|"+this.myLearnerUtility.getConstant().FORBIDDEN+"|\\w+ly)\\b")) {
 									start = start.replaceAll("^("+this.myLearnerUtility.getConstant().STOP+"|"+this.myLearnerUtility.getConstant().FORBIDDEN+"|\\w+ly)\\b\\s*", "");									
 								}
 								
 								modifier = start + modifier;
 								modifier = modifier.replaceAll("<\\S+?>", "");
 								tag = dataholderHandler.getParentSentenceTag(sentenceID);
 								List<String> modifierAndTag = dataholderHandler.getMTFromParentTag(tag);
 								String newM = modifierAndTag.get(0);
 								tag = modifierAndTag.get(1);
 								if (StringUtility.isMatchedNullSafe(newM, "\\w")) {
 									modifier = modifier + " " + newM;
 								}
 								dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "adjectivesubject[M-B]");
 								// new modifier
 								start = start.replaceAll("\\s*(and |or |and \\/ or |or \\/ and )\\s*", "");
 								start = start.replaceAll("<\\S+?>", "");
 								if (start.length() > 0) {
 									if (!StringUtility.isMatchedNullSafe(start, "ly\\s*$") 
 											&& !StringUtility.isMatchedNullSafe(start, "\\b(" + this.myLearnerUtility.getConstant().STOP + "|" + this.myLearnerUtility.getConstant().FORBIDDEN + ")\\b")) {
 										flag += dataholderHandler.updateDataHolder(word, "m", "", "modifiers", 1);
 										// print "find a modifier [F]: $start\n" if $debug;
 									}
 								}	
 							}
 						}
 
 						count++;
 					}
 				}
 			}
 		}
 		
 		return flag;
 	}
 	
 	public boolean adjectiveSubjectsPart2Helper1(String sentence,
 			Set<String> typeModifiers) {
 		String pattern = "<M>\\S*(" + StringUtils.join(typeModifiers, "|")
 				+ ")\\S*</M> .*";
 		return StringUtility.isMatchedNullSafe(sentence, pattern);
 	}
 	
 	/**
 	 * Discover new modifiers using and/or pattern. 
 	 * For "modifier and/or unknown boundary" pattern or
 	 * "unknown and/or modifier boundary" pattern, make "unknown" a modifier
 	 * 
 	 * @param dataholderHandler
 	 * @return
 	 */
 	public int discoverNewModifiers(DataHolder dataholderHandler) {
 		int sign = 0;
 		
 		// "modifier and/or unknown boundary" pattern
 		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
 			String sentenceTag = sentenceItem.getTag();
 			String sentence = sentenceItem.getSentence();	
 			int sentenceID = sentenceItem.getID();
 			if ((!StringUtility.isMatchedNullSafe(sentenceTag, "ignore") || sentenceTag == null) 
 				&& StringUtility.isMatchedNullSafe(sentence, "<M>[^\\s]+</M> (or|and|and / or|or / and) .*")){
 				String POS = "";
 				// if "<m>xxx</m> (and|or) yyy (<b>|\d)" pattern appears at the
 				// beginning or is right after the 1st word of the sentence,
 				// mark up the sentence, add yyy as a modifier
 				if (sentence != null) {
 					Pattern p1 = Pattern.compile("^(?:\\w+\\s)?<M>(\\S+)<\\/M> (and|or|nor|and \\/ or|or \\/ and) ((?:<[^M]>)*[^<]+(?:<\\/[^M]>)*) <B>[^,;:\\.]");
 					Matcher m1 = p1.matcher(sentence);
 					if (m1.find()) {
 						String g1 = m1.group(1);
 						String g2 = m1.group(2);
 						String g3 = m1.group(3);
 						String modifier = g1 +" "+ g2+" "+ g3;
 						String newM = g3;
 						
 						if (!StringUtility.isMatchedNullSafe(newM, "\\b("+this.myLearnerUtility.getConstant().STOP+")\\b")) {
 							modifier = modifier.replaceAll("<\\S+?>", "");
 							if (newM != null) {
 								Pattern p11 = Pattern.compile("(.*?>)(\\w+)<\\/");
 								Matcher m11 = p11.matcher(newM);
 								if (m11.find()) {
 									newM = m11.group(2);
 									POS = m11.group(1);
 								}
 							}
 							
 							// update N to M: retag sentences tagged as $newm, remove [s] record from wordpos
 							if (StringUtility.isMatchedNullSafe(POS, "<N>")) {
 								sign += dataholderHandler.changePOS(newM, "s", "m", "", 1);
 							}
 							// B
 							else {
 								sign += dataholderHandler.updateDataHolder(newM, "m", "", "modifiers", 1);
 							}
 							// print "find a modifier [A]: $newm\n" if $debug;
 							String tag = dataholderHandler.getParentSentenceTag(sentenceID);
 							List<String> modifierAndTag = dataholderHandler.getMTFromParentTag(tag);
 							String m = modifierAndTag.get(0);
 							tag = modifierAndTag.get(1);
 							if (StringUtility.isMatchedNullSafe(m, "\\w")) {
 								modifier = modifier + " "+m;
 							}
 							dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "discovernewmodifiers");							
 						}
 					}
 					// if the pattern appear in the middle of the sentence, add yyy as modifier
 					else {
 						Pattern p2 = Pattern.compile("<M>(\\S+)<\\/M> (and|or|nor|and \\/ or|or \\/ and) (\\w+) <B>[^,;:\\.]");
 						Matcher m2 = p2.matcher(sentence); 
 						if (m2.find()) {
 							String newM = m2.group(3);
 							sign += dataholderHandler.updateDataHolder(newM, "m", "", "modifiers", 1);
 							// print "find a modifier[B]: $newm\n" if $debug;
 						}
 						
 					}
 				}
 			}
 		}
 		
 		// "unknown and/or modifier boundary"
 		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
 			String sentence = sentenceItem.getSentence();
 			String sentenceTag = sentenceItem.getTag();
 			if ((!StringUtility.isMatchedNullSafe(sentenceTag, "ignore") || sentenceTag == null) 
 					&& StringUtility.isMatchedNullSafe(sentence, "[^\\w]+ (and|or|nor|and / or|or / and) <M>[^\\w]+</M> .*")) {
 				int sentenceID = sentenceItem.getID();
 				
 				String POS = "";
 				// if "xxx (and|or|nor) <m>yyy</m> (<b>|\d)" pattern appear at the beginning or is right after the 1st word of the sentence, mark up the sentence, add yyy as a modifier
 				if (sentence != null) {
 					Pattern p3 = Pattern.compile("^(?:\\w+\\s)?((?:<[^M]>)*[^<]+(?:<\\/[^M]>)*) (and|or|nor|and \\/ or|or \\/ and) <M>(\\S+)<\\/M> <B>[^:;,\\.]");
 					Matcher m3 = p3.matcher(sentence);
 					if (m3.find()) {
 						String g1 = m3.group(1);
 						String g2 = m3.group(2);
 						String g3 = m3.group(3);
 						
 						String modifier = g1 + " " + g2 + " " + g3; 
 						String newM = g1;
 						modifier = modifier.replaceAll("<\\S+?>", "");
 						if (newM != null) {
 							Pattern p31 = Pattern.compile("(.*?>)(\\w+)<\\/");							
 							Matcher m31 = p31.matcher(newM);
 							if (m31.find()) { // N or B
 								newM = m31.group(2);
 								POS = m31.group(1);
 							}
 						}
 						
 						if (StringUtility.isMatchedNullSafe(POS, "<N>")) { // update N to M
 							sign += dataholderHandler.changePOS(newM, "s", "m", "", 1); // update $newm to m
 						}
 						else { // B
 							sign += dataholderHandler.updateDataHolder(newM, "m", "", "modifiers", 1);
 						}
 						// print "find a modifier [C]: $newm\n" if $debug;
 						String tag = dataholderHandler.getParentSentenceTag(sentenceID);
 						List<String> modifierAndTag = dataholderHandler.getMTFromParentTag(tag);
 						String m = modifierAndTag.get(0);
 						tag = modifierAndTag.get(1);
 						
 						if (StringUtility.isMatchedNullSafe(m, "\\w")) {
 							modifier = modifier +" "+m;
 						}
 						
 						dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "discovernewmodifiers");
 					}
 					else {
 						Pattern p32 = Pattern.compile("(\\w+) (and|or|nor|and \\/ or|or \\/ and) <M>(\\S+)<\\/M> <B>[^,:;\\.]");
 						Matcher m32 = p32.matcher(sentence);
 						// if the pattern appear in the middle of the sentence, add yyy as modifier
 						if (m32.find()) {
 							String newM = m32.group(1);
 							sign += dataholderHandler.updateDataHolder(newM, "m", "", "modifiers", 1);
 						}
 						//print "find a modifier [D]: $newm\n" if $debug;
 					}
 				}
 			}
 		}
 		
 		return sign;
 	}
 
 	public int handleAndOr(DataHolder dataholderHandler) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.handleAndOr");
 
 		myLogger.info("to match pattern " + Constant.ANDORPTN);
 
 		List<SentenceStructure> sentenceItems = dataholderHandler
 				.getSentencesByTagPattern("^andor$");
 
 		int sign = 0;
 		for (SentenceStructure sentenceItem : sentenceItems) {
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 			// myLogger.trace(Constant.SEGANDORPTN);
 			// myLogger.trace(Constant.ANDORPTN);
 			int result = this.andOrTag(dataholderHandler, sentenceID, sentence,
 					Constant.SEGANDORPTN, Constant.ANDORPTN);
 			sign = sign + result;
 		}
 		
 		return sign;
 	}
 
 	public int andOrTag(DataHolder dataholderHandler, int sentenceID,
 			String sentence, String sPattern, String wPattern) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.andOrTag");
 		myLogger.trace("Enter");
 
 		int sign = 0;
 
 		List<String> mPatterns = new ArrayList<String>();
 		List<String> sPatterns = new ArrayList<String>();
 		List<String> mSegments = new ArrayList<String>();
 		List<String> sSegments = new ArrayList<String>();
 
 		Set<String> token = new HashSet<String>();
 		token.addAll(Arrays.asList("and or nor".split(" ")));
 		token.add("\\");
 		token.add("and / or");
 		String strToken = "(" + StringUtils.join(token, " ") + ")";
 
 		int limit = 80;
 		List<String> words = new ArrayList<String>();
 		words.addAll(Arrays.asList(sentence.split(" ")));
 		String pattern = this.getLearnerUtility().getSentencePtn(
 				dataholderHandler, token, limit, words);
 		pattern = pattern.replaceAll("t", "m");
 
 		myLogger.info(String.format("Andor pattern %s for %s", pattern,
 				words.toString()));
 
 		if (pattern == null) {
 			return -1;
 		}
 
 		// Matcher m1 = StringUtility.createMatcher(pattern, wPattern);
 		Matcher m2 = StringUtility.createMatcher(pattern, "^b+&b+[,:;.]");
 
 		if (sentenceID == 163) {
 			System.out.println();
 		}
 
 		List<List<String>> res = this.andOrTagCase1Helper(pattern, wPattern, words, token);
 		if (res != null) {
 			mPatterns = res.get(0);
 			mSegments = res.get(1);
 			sPatterns = res.get(2);
 			sSegments = res.get(3);
 			List<String> tagAndModifier1 = res.get(4);
 			List<String> tagAndModifier2 = res.get(5);
 			List<String> update1 = res.get(6);
 			List<String> update2 = res.get(7);
 
 			if (tagAndModifier1.size() > 0) {
 				String modifier = tagAndModifier1.get(0);
 				String tag = tagAndModifier1.get(1);
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "",
 						tag, "andor[n&n]");
 				myLogger.trace("tagSentenceWithMT(" + sentenceID + ", "
 						+ sentence + ", , " + tag + ", andor[n&n]");
 			} else {
 				myLogger.debug(String.format(
 						"Andor can not determine a tag or modifier for %d: %s",
 						sentenceID, sentence));
 			}
 
 			if (tagAndModifier2.size() > 0) {
 				String modifier = tagAndModifier2.get(0);
 				String tag = tagAndModifier2.get(1);
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 						modifier, tag, "andor[m&mn]");
 				myLogger.trace("tagSentenceWithMT(" + sentenceID + ", "
 						+ sentence + ", " + modifier + ", " + tag
 						+ ", andor[m&mn]");
 			} else {
 				myLogger.debug(String.format(
 						"Andor can not determine a tag or modifier for %d: %s",
 						sentenceID, sentence));
 			}
 
 			if (update1.size() > 0) {
 				String newBoundaryWord = update1.get(0);
 				sign = sign
 						+ dataholderHandler.updateDataHolder(newBoundaryWord,
 								"b", "", "wordpos", 1);
 			}
 
 			if (update2.size() > 0) {
 				for (String tempWord : update2) {
 					sign = sign
 							+ dataholderHandler.updateDataHolder(tempWord, "p",
 									"-", "wordpos", 1);
 				}
 			}
 		}
 
 		else if (m2.find()) {
 			myLogger.trace("Case 2");
 			dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "",
 					"ditto", "andor");
 		} else {
 			myLogger.trace("Case 3");
 			myLogger.trace("[andortag]Andor can not determine a tag or modifier for "
 					+ sentenceID + ": " + sentence);
 		}
 		myLogger.trace("Return " + sign + "\n");
 		return sign;
 	}
 
 	public List<List<String>> andOrTagCase1Helper(String pattern,
 			String wPattern, List<String> words, Set<String> token) {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		Logger myLogger = Logger.getLogger("learn.andOrTag");
 
 		List<String> mPatterns = new ArrayList<String>();
 		List<String> sPatterns = new ArrayList<String>();
 		List<String> mSegments = new ArrayList<String>();
 		List<String> sSegments = new ArrayList<String>();
 
 		List<String> update1 = new ArrayList<String>();
 		List<String> update2 = new ArrayList<String>();
 
 		List<String> tagAndModifier1 = new ArrayList<String>();
 		List<String> tagAndModifier2 = new ArrayList<String>();
 
 		String strToken = "(" + StringUtils.join(token, " ") + ")";
 
 		Matcher m1 = StringUtility.createMatcher(pattern, wPattern);
 
 		if (m1.find()) {
 			myLogger.trace("Case 1");
 			if (pattern.equals("n&qqnbq")) {
 				// System.out.println();
 			}
 
 			int start1 = m1.start(1);
 			int end1 = m1.end(1);
 
 			int start2 = m1.start(2);
 			int end2 = m1.end(2);
 
 			int start3 = m1.start(3);
 			int end3 = m1.end(3);
 
 			int start4 = m1.start(4);
 			int end4 = m1.end(4);
 
 			int start5 = m1.start(5);
 			int end5 = m1.end(5);
 
 
 			// System.out.println(pattern);
 			// System.out.println(start1);
 			// System.out.println();
 			String earlyGroupsPattern = start1 == -1 ? "" : pattern.substring(
 					0, start1);
 			String[] patterns = earlyGroupsPattern.split("s*<B>,<\\/B>\\s*");
 			String earlyGroupsWords = start1 == -1 ? "" : StringUtils.join(
 					words.subList(0, start1), " ");
 			String[] segments = earlyGroupsWords.split("\\s*<B>,<\\/B>s*");
 
 			String secondLastModifierPattern = m1.group(1);
 			String secondLastModifierWords = secondLastModifierPattern == null ? ""
 					: StringUtils.join(words.subList(start1, end1), " ");
 
 			String sencondLastStructurePattern = m1.group(2);
 			String secondLastStructureWords = sencondLastStructurePattern == null ? ""
 					: StringUtils.join(words.subList(start2, end2), " ");
 
 			String lastModifierPattern = m1.group(3);
 			String lastModifierWords = lastModifierPattern == null ? ""
 					: StringUtils.join(words.subList(start3, end3), " ");
 
 			String lastStructurePattern = m1.group(4);
 			String lastStructureWords = lastStructurePattern == null ? ""
 					: StringUtils.join(words.subList(start4, end4), " ");
 
 			String endSegmentPattern = m1.group(5);
 			String endSegmentWords = endSegmentPattern == null ? ""
 					: StringUtils.join(words.subList(start5, end5), " ");
 
 			int bIndex = start5;
 
 			// matching pattern with original text
 			if (!(patterns.length == 1 && StringUtils.equals(patterns[0], ""))) {
 				for (int i = 0; i < patterns.length; i++) {
 					Pattern p = Pattern.compile("sPattern");
 					Matcher m10 = p.matcher(patterns[i]);
 					if (m10.find()) {
 						String g1 = m10.group(1);
 						mPatterns.add(g1);
 						String g2 = m10.group(2);
 						sPatterns.add(g2);
 
 						List<String> w = new ArrayList<String>(
 								Arrays.asList(segments[i].split(" ")));
 						String m = StringUtils.join(w.subList(0, m10.end(1)),
 								" ");
 
 						if (StringUtility.isMatchedNullSafe(m,
 								"\\b(although|but|when|if|where)\\b")) {
 							return null;
 						}
 
 						mSegments.add(m);
 						sSegments.add(StringUtils.join(
 								w.subList(m10.end(1), w.size()), " "));
 					} else {
 						myLogger.info("wrong segment: " + patterns[i] + "=>"
 								+ segments[i] + "\n");
 						return null;
 					}
 				}
 			}
 
 			if (secondLastModifierPattern != null)
 				mPatterns.add(secondLastModifierPattern);
 			if (!StringUtils.equals(secondLastModifierWords, ""))
 				mSegments.add(secondLastModifierWords);
 			if (sencondLastStructurePattern != null)
 				sPatterns.add(sencondLastStructurePattern);
 			if (!StringUtils.equals(secondLastStructureWords, ""))
 				sSegments.add(secondLastStructureWords);
 
 			if (lastModifierPattern != null)
 				mPatterns.add(lastModifierPattern);
 			if (!StringUtils.equals(lastModifierWords, ""))
 				mSegments.add(lastModifierWords);
 			if (lastStructurePattern != null)
 				sPatterns.add(lastStructurePattern);
 			if (!StringUtils.equals(lastStructureWords, ""))
 				sSegments.add(lastStructureWords);
 
 			// find the modifier and the tag for sentenceID
 			// case 1.1
 			if (this.countStructures(sPatterns) > 1) {
 				// compound subject involving multiple structures: mn,mn,&mn =>
 				// use all but bounary as the tag, modifier="";
 				String tag = StringUtils.join(words.subList(0, bIndex), " ");
 				String modifier = "";
 				tag = tag.replaceAll("<\\S+?>", "");
 				if (tag != null) {
 					String regex11 = "\\b(" + StringUtils.join(token, "|")
 							+ ")\\b";
 					Matcher m11 = StringUtility.createMatcher(tag, regex11);
 
 					if (m11.find()) {
 						String conj = m11.group(1);
 
 						tag = tag.replaceAll(",", " " + conj + " ");
 						tag = tag.replaceAll("\\s+", " ");
 						tag = tag.replaceAll("(" + conj + " )+", "$1");
 						tag = tag.replaceAll("^\\s+", "");
 						tag = tag.replaceAll("\\s+$", "");
 
 						// dataholderHandler.tagSentenceWithMT(sentenceID,
 						// sentence, "", tag, "andor[n&n]");
 						tagAndModifier1.add("");
 						tagAndModifier1.add(tag);
 					}
 					// else {
 					// myLogger.debug(String.format("Andor can not determine a tag or modifier for %d: %s",
 					// sentenceID, sentence));
 					// }
 				}
 				// case 1.2
 				else if (this.countStructures(sPatterns) == 1) {
 					// m&mn => connect all modifiers as the modifier, and the n
 					// as the tag
 					int i = 0;
 					for (i = 0; i < sPatterns.size(); i++) {
 						if (StringUtility.isMatchedNullSafe(sPatterns.get(i),
 								"\\w")) {
 							break;
 						}
 					}
 
 					tag = sSegments.get(i);
 					tag = tag.replaceAll("<\\S+?>", "");
 					modifier = StringUtils.join(mSegments, " ");
 					modifier = modifier.replaceAll("<\\S+?>", "");
 
 					tag = StringUtility.trimString(tag);
 					modifier = StringUtility.trimString(modifier);
 
 					String myStop = this.myLearnerUtility.getConstant().STOP;
 					myStop = myStop.replaceAll(
 							String.format("\\b%s\\b", token), "");
 					myStop = myStop.replaceAll("\\s+$", "");
 
 					if (StringUtility.isMatchedNullSafe(modifier, "\\b"
 							+ strToken + "\\b")
 							&& StringUtility.isEntireMatchedNullSafe(modifier,
 									"\\b(" + myStop + "|to)\\b")) {
 						// case 1.2.1
 						List<String> wordsTemp = new ArrayList<String>();
 						wordsTemp.addAll(Arrays.asList(tag.split("\\s+")));
 						modifier = modifier
 								+ " "
 								+ StringUtils.join(wordsTemp.subList(0,
 										wordsTemp.size() - 1), " ");
 						tag = wordsTemp.get(wordsTemp.size() - 1);
 						// dataholderHandler.tagSentenceWithMT(sentenceID,
 						// sentence, modifier, tag, "andor[m&mn]");
 						tagAndModifier2.add(modifier);
 						tagAndModifier2.add(tag);
 
 					}
 					// else {
 					// myLogger.debug(String.format("Andor can not determine a tag or modifier for %d: %s",
 					// sentenceID, sentence));
 					// }
 				}
 				// case 1.3
 				else {
 					myLogger.debug("Andor can not determine a tag or modifier");
 				}
 
 				int q = -1;
 				if (endSegmentPattern != null) {
 					Matcher m13 = StringUtility.createMatcher(
 							endSegmentPattern, "q");
 					if (m13.find()) {
 						q = m13.start();
 					}
 				}
 
 				if (q >= 0) {
 					String newBoundaryWord = endSegmentWords.split(" ")[q];
 					if (StringUtility.isMatchedNullSafe(newBoundaryWord, "\\w")) {
 						update1.add(newBoundaryWord);
 						// sign = sign +
 						// dataholderHandler.updateDataHolder(newBoundaryWord,
 						// "b", "", "wordpos", 1);
 					}
 				}
 
 				// structure patterns and segments: $nptn =
 				// "((?:[np],?)*&?[np])"; #grouped #must present, no q allowed
 				// mark all ps "p"
 				for (int i = 0; i < sPatterns.size(); i++) {
 					String sPatternI = sPatterns.get(i);
 					sPatternI = sPatternI.replaceAll("(.)", "$1 ");
 					sPatternI = StringUtility.trimString(sPatternI);
 					String[] ps = sPatternI.split(" ");
 					String[] ts = sSegments.get(i).split("\\s+");
 
 					for (int j = 0; j < ps.length; j++) {
 						if (StringUtils.equals(ps[j], "p")) {
 							ts[j] = StringUtility.trimString(ts[j]);
 							update2.add(ts[j]);
 							// sign = sign
 							// + dataholderHandler.updateDataHolder(ts[j],
 							// "p", "-", "wordpos", 1);
 						}
 					}
 
 				}
 
 			}
 
 			List<List<String>> res = new ArrayList<List<String>>();
 			res.add(mPatterns);
 			res.add(mSegments);
 			res.add(sPatterns);
 			res.add(sSegments);
 			res.add(tagAndModifier1);
 			res.add(tagAndModifier2);
 			res.add(update1);
 			res.add(update2);
 
 			return res;
 		} else {
 			return null;
 		}
 	}
 
 	public int countStructures(List<String> patterns) {
 		int count = 0;
 		for (String pattern : patterns) {
 			if (StringUtility.isMatchedNullSafe(pattern, "\\w")) {
 				count++;
 			}
 		}
 
 		return count;
 	}
 
 	public void resetAndOrTags(DataHolder dataholderHandler) {
 		dataholderHandler.updateSentenceTag("^andor$", null);
 	}
 
 	public void ditto(DataHolder dataholderHandler) {
 		String nPhrasePattern = "(?:<[A-Z]*[NO]+[A-Z]*>[^<]+?<\\/[A-Z]*[NO]+[A-Z]*>\\s*)+";
 		String mPhrasePattern = "(?:<[A-Z]*M[A-Z]*>[^<]+?<\\/[A-Z]*M[A-Z]*>\\s*)+";
 
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			if (sentenceItem.getTag() == null) {
 				int sentenceID = sentenceItem.getID();
 				String sentence = sentenceItem.getSentence();
 				this.dittoHelper(dataholderHandler, sentenceID, sentence,
 						nPhrasePattern, mPhrasePattern);
 			}
 		}
 	}
 
 	public int dittoHelper(DataHolder dataholderHandler, int sentenceID,
 			String sentence, String nPhrasePattern, String mPhrasePattern) {
 		int res = 0;
 		String sentenceCopy = "" + sentence;
 		sentenceCopy = sentenceCopy.replaceAll("></?", "");
 		String modifier = "";
 
 		Matcher m2 = StringUtility.createMatcher(sentenceCopy, "(.*?)"
 				+ nPhrasePattern);
 
 		if (!StringUtility.isMatchedNullSafe(sentence, "<[NO]>")) {
 			String tag = "ditto";
 			dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "", tag,
 					"ditto-no-N");
 			res = 1;
 		} else if (m2.find()) {
 			String head = m2.group(1);
 			String pattern21 = String
 					.format("\\b(%s)\\b", this.myLearnerUtility.getConstant().PREPOSITION);
 			if (StringUtility.isMatchedNullSafe(head, pattern21)) {
 				String tag = "ditto";
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 						modifier, tag, "ditto-proposition");
 				res = 21;
 			} else if (StringUtility.isMatchedNullSafe(head, ",<\\/B>\\s*$")) {
 				String tag = "ditto";
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 						modifier, tag, "ditto-,-N");
 				res = 22;
 			}
 		}
 
 		return res;
 	}
 
 	public void phraseClause(DataHolder dataholderHandler) {
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			if (sentenceItem.getTag() == null) {
 				int sentenceID = sentenceItem.getID();
 				String sentence = sentenceItem.getSentence();
 				List<String> res = this.phraseClauseHelper(sentence);
 				if (res != null && res.size() == 2) {
 					String modifier = res.get(0);
 					String tag = res.get(1);
 					dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 							modifier, tag, "phraseclause");
 				}
 			}
 		}
 	}
 
 	public List<String> phraseClauseHelper(String sentence) {
 		if (sentence == null) {
 			return null;
 		}
 
 		List<String> res = new ArrayList<String>(2);
 		String pattern = "^(.*?)((?:<[A-Z]*M[A-Z]*>[^<]*?<\\/[A-Z]*M[A-Z]*>\\s*)*)((?:<[A-Z]*[NO]+[A-Z]*>[^<]*?<\\/[A-Z]*[NO]+[A-Z]*>\\s*)+)<B>[,:\\.;]<\\/B>\\s*$";
 		String sentenceCopy = "" + sentence;
 		sentenceCopy = sentenceCopy.replaceAll("></?", "");
 
 		Matcher m = StringUtility.createMatcher(sentenceCopy, pattern);
 		if (m.find()) {
 			String head = m.group(1);
 			String modifier = m.group(2);
 			String tag = m.group(3);
 
 			String prepositionPattern = String.format("\\b(%s)\\b",
 					this.myLearnerUtility.getConstant().PREPOSITION);
 			if (!StringUtility.isMatchedNullSafe(head, prepositionPattern)
 					&& !StringUtility.isMatchedNullSafe(head, "<\\/N>")
 					&& !StringUtility.isMatchedNullSafe(modifier,
 							prepositionPattern)) {
 				if (tag != null) {
 					Matcher m2 = StringUtility.createMatcher(tag,
 							"(.*?)<N>([^<]+)<\\/N>\\s*$");
 					if (m2.find()) {
 						modifier = modifier + m2.group(1);
 						tag = m2.group(2);
 					}
 					tag = tag.replaceAll("<\\S+?>", "");
 					modifier = modifier.replaceAll("<\\S+?>", "");
 					tag = tag.replaceAll("(^\\s*|\\s*$)", "");
 					modifier = modifier.replaceAll("(^\\s*|\\s*$)", "");
 					res.add(modifier);
 					res.add(tag);
 
 					return res;
 				}
 			}
 		}
 		return res;
 	}
 
 	public void pronounCharacterSubject(DataHolder dataholderHandler) {
 
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 
 			int sentenceID = sentenceItem.getID();
 			String lead = sentenceItem.getLead();
 			String sentence = sentenceItem.getSentence();
 			String modifier = sentenceItem.getModifier();
 			String tag = sentenceItem.getTag();
 
 			List<String> mt = pronounCharacterSubjectHelper(lead, sentence,
 					modifier, tag);
 			if (mt != null) {
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 						modifier, tag,
 						"pronouncharactersubject[character subject]");
 			}
 		}
 
 		// preposition cases
 		String prepositionPattern = String
 				.format("^(%s)", this.myLearnerUtility.getConstant().PREPOSITION);
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			int sentenceID = sentenceItem.getID();
 			String lead = sentenceItem.getLead();
 			String modifier = sentenceItem.getModifier();
 			String tag = sentenceItem.getTag();
 			String sentence = sentenceItem.getSentence();
 			boolean case1 = (StringUtils.equals(tag, "ignore"));
 			boolean case2 = (tag == null);
 			boolean case3 = StringUtility.isMatchedNullSafe(tag,
 					prepositionPattern + " ");
 			if ((case1 || case2) && case3) {
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence, "",
 						"", "pronouncharactersubject[proposition subject]");
 			}
 		}
 
 		// pronoun cases
 		String pronounPattern = String.format("(%s)", this.myLearnerUtility.getConstant().PRONOUN);
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			int sentenceID = sentenceItem.getID();
 			String lead = sentenceItem.getLead();
 			String modifier = sentenceItem.getModifier();
 			String tag = sentenceItem.getTag();
 			String sentence = sentenceItem.getSentence();
 
 			boolean case1 = StringUtility.isMatchedNullSafe(tag,
 					String.format("(^| )%s( |\\$)", pronounPattern));
 			boolean case2 = StringUtility.isMatchedNullSafe(modifier,
 					String.format("(^| )%s( |\\$)", pronounPattern));
 			if (case1 || case2) {
 				modifier = modifier.replaceAll("\\b(" + this.myLearnerUtility.getConstant().PRONOUN
 						+ ")\\b", "");
 				tag = tag.replaceAll("\\b(" + this.myLearnerUtility.getConstant().PRONOUN + ")\\b", "");
 				modifier = modifier.replaceAll("\\s+", " ");
 				tag = tag.replaceAll("\\s+", " ");
 
 				if (!StringUtility.isMatchedNullSafe(tag, "\\w")
 						|| StringUtility.isMatchedNullSafe(tag, "ditto")) {
 					tag = dataholderHandler.getParentSentenceTag(sentenceID);
 				}
 
 				modifier = modifier.replaceAll("(^\\s*|\\s*$)", "");
 				tag = tag.replaceAll("(^\\s*|\\s*$)", "");
 
 				List<String> mt = dataholderHandler.getMTFromParentTag(tag);
 				String m = mt.get(0);
 				tag = mt.get(1);
 
 				if (StringUtility.isMatchedNullSafe(m, "\\w")) {
 					modifier = modifier + m;
 					dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 							modifier, tag,
 							"pronouncharactersubject[pronoun subject]");
 				}
 			}
 		}
 
 		// correct to missed N
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			int sentenceID = sentenceItem.getID();
 			String lead = sentenceItem.getLead();
 			String modifier = sentenceItem.getModifier();
 			String tag = sentenceItem.getTag();
 			String sentence = sentenceItem.getSentence();
 
 			List<String> mt = this.pronounCharacterSubjectHelper4(lead,
 					sentence, modifier, tag);
 			if (mt != null) {
 				modifier = mt.get(0);
 				tag = mt.get(1);
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 						modifier, tag,
 						"pronouncharactersubject[correct to missed N]");
 			}
 		}
 	}
 
 	public List<String> pronounCharacterSubjectHelper4(String lead,
 			String sentence, String modifier, String tag) {
 		boolean case1 = (StringUtils.equals(tag, "ignore"));
 		boolean case2 = (tag == null);
 		boolean case3 = !StringUtility.isMatchedNullSafe(tag, " (and|nor|or) ");
 		boolean case4 = !StringUtility.isMatchedNullSafe(sentence, "\\[");
 		boolean case5 = false;
 		if (sentence != null) {
 			Pattern p = Pattern.compile("^[^N]*<N>" + tag);
 			Matcher m = p.matcher(sentence);
 			if (m.find()) {
 				case5 = true;
 			}
 		}
 
 		if ((case1 || case2) && case3 && case4 && case5) {
 			if (sentence != null) {
 				sentence = sentence.replaceAll("></?", "");
 				Pattern p = Pattern
 						.compile("^(\\S*) ?<N>([^<]+)<\\/N> <[MB]+>(\\S+)<\\/[MB]+> \\S*\\b"
 								+ tag + "\\b\\S*");
 				Matcher m2 = p.matcher(sentence);
 				if (m2.find()) {
 					modifier = m2.group(1);
 					tag = m2.group(2);
 					String g3 = m2.group(3);
 					if (!StringUtility.isMatchedNullSafe(g3, "\\bof\\b")) {
 						modifier = modifier.replaceAll("<\\S+?>", "");
 						tag = tag.replaceAll("<\\S+?>", "");
 						modifier = modifier.replaceAll("(^\\s*|\\s*$)", "");
 						tag = tag.replaceAll("(^\\s*|\\s*$)", "");
 						List<String> mt = new ArrayList<String>();
 						mt.add(modifier);
 						mt.add(tag);
 						return mt;
 					}
 				}
 			}
 		}
 		return null;
 
 	}
 
 	public List<String> pronounCharacterSubjectHelper(String lead,
 			String sentence, String modifier, String tag) {
 		String t = "(?:<\\/?[A-Z]+>)?";
 
 		boolean b1 = !StringUtils.equals(tag, "ignore");
 		boolean b2 = (tag == null);
 		boolean b3 = StringUtility.isMatchedNullSafe(lead, "(^| )("
 				+ this.myLearnerUtility.getConstant().CHARACTER + ")( |$)");
 		boolean b4 = StringUtility.isMatchedNullSafe(tag, "(^| )("
 				+ this.myLearnerUtility.getConstant().CHARACTER + ")( |$)");
 		if (((b1 || b2) && b3) || b4) {
 			sentence = sentence.replaceAll("></?", "");
 			if (sentence != null) {
 				String pattern1 = String
 						.format("^.*?%s\\b(%s)\\b%s %s(?:of)%s (.*?)(<[NO]>([^<]*?)<\\/[NO]> ?)+ ",
 								t, this.myLearnerUtility.getConstant().CHARACTER, t, t, t);
 				Matcher m1 = StringUtility.createMatcher(sentence, pattern1);
 
 				String pattern2 = String
 						.format("^(.*?)((?:<\\/?[BM]+>\\w+?<\\/?[BM]+>\\s*)*)%s\\b(%s)\\b%s",
 								t, this.myLearnerUtility.getConstant().CHARACTER, t);
 				Matcher m2 = StringUtility.createMatcher(sentence, pattern2);
 
 				// case 1.1
 				if (m1.find()) {
 					tag = m1.group(4);
 					modifier = sentence.substring(m1.start(2), m1.start(4));
 					String s2 = m1.group(2);
 					String s3 = m1.group(3);
 
 					if ((!StringUtility.isMatchedNullSafe(s2,
 							String.format("\\b(%s)\\b", this.myLearnerUtility.getConstant().PREPOSITION)))
 							&& (!StringUtility.isMatchedNullSafe(s3, String
 									.format("\\b(%s|\\d)\\b", this.myLearnerUtility.getConstant().STOP)))) {
 						modifier = modifier.replaceAll("<\\S+?>", "");
 						modifier = modifier.replaceAll("(^\\s*|\\s*$)", "");
 						tag = tag.replaceAll("<\\S+?>", "");
 						tag = tag.replaceAll("(^\\s*|\\s*$)", "");
 					} else {
 						modifier = "";
 						tag = "ditto";
 					}
 				}
 
 				// case 1.2
 				else if (m2.find()) {
 					String text = m2.group(1);
 
 					if ((!StringUtility.isMatchedNullSafe(text, "\\b("
 							+ this.myLearnerUtility.getConstant().STOP + "|\\d+)\\b"))
 							&& (StringUtility.isMatchedNullSafe(text, "\\w"))
 							&& (!StringUtility
 									.isMatchedNullSafe(text, "[,:;.]"))) {
 						text = text.replaceAll("<\\S+?>", "");
 						// $text =~ s#(^\s*|\s*$)##g;
 						// $text =~ s#[[:punct:]]##g;
 						text = text.replaceAll("(^\\s*|\\s*$)", "");
 						text = text.replaceAll("\\p{Punct}", "");
 
 						String[] textArray = text.split("\\s+");
 						// List<String> textList = new LinkedList<String>();
 						// textList.addAll(Arrays.asList(textArray));
 						if (textArray.length >= 1) {
 							tag = textArray[textArray.length - 1];
 							String pattern = "<[NO]>" + tag + "</[NO]>";
 							if (StringUtility.isMatchedNullSafe(sentence,
 									pattern)) {
 								// 1.2.1.1
 								text = text.replaceAll(tag, "");
 								modifier = text;
 							} else {
 								// 1.2.1.2
 								modifier = "";
 								tag = "ditto";
 							}
 						}
 					} else {
 						// 1.2.2
 						modifier = "";
 						tag = "ditto";
 					}
 				}
 
 				// case 1.3
 				else if (StringUtility.isMatchedNullSafe(sentence, "\\b("
 						+ this.myLearnerUtility.getConstant().CHARACTER + ")\\b")) {
 					modifier = "";
 					tag = "ditto";
 				}
 
 			}
 			List<String> mt = new ArrayList<String>(2);
 			mt.add(modifier);
 			mt.add(tag);
 			return mt;
 		} else {
 			return null;
 		}
 
 	}
 
 	// sentences that are tagged with a commons substructure, such as blades,
 	// margins need to be modified with its parent structure
 	public void commonSubstructure(DataHolder dataholderHandler) {
 		Set<String> commonTags = this
 				.getCommonStructures(dataholderHandler);
 
 		String pattern = StringUtils.join(commonTags, "|");
 		pattern = "\\\\[?(" + pattern + ")\\\\]?";
 
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			String tag = sentenceItem.getTag();
 			boolean c1 = StringUtils.equals(tag, "ignore");
 			boolean c2 = (tag == null);
 			boolean c3 = (StringUtility.isMatchedNullSafe(tag, "^" + pattern
 					+ "$"));
 
 			if ((c1 || c2) && c3) {
 				int sentenceID = sentenceItem.getID();
 				String modifier = sentenceItem.getModifier();
 				String sentence = sentenceItem.getSentence();
 
 				if (!isModifierContainsStructure(dataholderHandler, modifier)
 						&& !StringUtility.isMatchedNullSafe(tag, "\\[")) {
 					// when the common substructure is not already modified by a
 					// structure, and
 					// when the tag is not already inferred from parent tag:
 					// mid/[phyllaries]
 
 					String parentStructure = dataholderHandler
 							.getParentSentenceTag(sentenceID);
 
 					String pTag = "" + parentStructure;
 					parentStructure = parentStructure.replaceAll("([\\[\\]])",
 							"");
 					if (!StringUtils.equals(parentStructure, "[parenttag]")
 							&& !StringUtility.isMatchedNullSafe(modifier,
 									parentStructure)
 							&& !StringUtility.isMatchedNullSafe(tag,
 									parentStructure)) {
 						// remove any overlapped words btw parentStructure and
 						// tag
 						pTag = pTag.replaceAll("\\b" + tag + "\\b", "");
 						String modifierCopy = "" + modifier;
 						modifier = StringUtility.trimString(modifier);
 						pTag = StringUtility.trimString(pTag);
 						pTag = pTag.replaceAll("\\s+", " ");
 						if (isTypeModifier(dataholderHandler, modifier)) {
 							// cauline/base => cauline [leaf] / base
 							modifier = modifier + " " + pTag;
 						} else {
 							// main marginal/spine => [leaf blade] main
 							// marginal/spine
 							modifier = pTag + " " + modifier;
 						}
 
 						// tagsentwmt($sentid, $sentence, $modifier, $tag,
 						// "commonsubstructure");
 						dataholderHandler.tagSentenceWithMT(sentenceID,
 								sentence, modifier, tag, "commonsubstructure");
 					}
 				}
 			}
 		}
 	}
 
 	public boolean isTypeModifier(DataHolder dataholderHandler, String modifier) {
 		boolean res = false;
 
 		String[] words = modifier.split("\\s+");
 		String word = words[words.length - 1];
 
 		if (dataholderHandler.getModifierHolder().containsKey(word)) {
 			ModifierTableValue modifierItem = dataholderHandler
 					.getModifierHolder().get(modifier);
 			if (modifierItem.getIsTypeModifier()) {
 				res = true;
 			}
 		}
 
 		return res;
 	}
 
 	public boolean isModifierContainsStructure(DataHolder dataholderHandler,
 			String modifier) {
 		boolean res = false;
 
 		String[] words = modifier.split("\\s+");
 
 		for (String word : words) {
 			Set<String> POSTags = new HashSet<String>();
 			POSTags.add("p");
 			POSTags.add("s");
 			Set<String> PSWords = dataholderHandler
 					.getWordsFromWordPOSByPOSs(POSTags);
 			if (PSWords.contains(word)) {
 				res = true;
 				break;
 			}
 		}
 
 		return res;
 	}
 
 	/**
 	 * find tags with more than one different structure modifiers
 	 * 
 	 * @param dataholderHandler
 	 * @return
 	 */
 	public Set<String> getCommonStructures(DataHolder dataholderHandler) {
 
 		// Get structures.
 		// Structures are just words from WordPOS holder that are P/S but not B
 		Set<String> PSTags = new HashSet<String>(
 				Arrays.asList("s p".split(" ")));
 		Set<String> BTags = new HashSet<String>();
 		BTags.add("b");
 		Set<String> PSWords = dataholderHandler
 				.getWordsFromWordPOSByPOSs(PSTags);
 		Set<String> BWords = dataholderHandler.getWordsFromWordPOSByPOSs(BTags);
 
 		Set<String> allStructures = StringUtility.setSubtraction(PSWords,
 				BWords);
 
 		Set<String> commonTags = new HashSet<String>();
 
 		// Get a map maps tags to their structures
 		Map<String, Set<String>> tagToModifiers = new HashMap<String, Set<String>>();
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			String tag = sentenceItem.getTag();
 			String modifier = sentenceItem.getModifier();
 
 			boolean c1 = StringUtils.equals(tag, "ignore");
 			boolean c2 = (tag == null);
 			boolean c3 = StringUtility.isMatchedNullSafe(tag, " ");
 			boolean c4 = StringUtility.isMatchedNullSafe(tag, "\\[");
 			if ((!c1 || c2) && !c3 && !c4) {
 				if (allStructures.contains(modifier)) {
 					if (tagToModifiers.containsKey(tag)) {
 						tagToModifiers.get(tag).add(modifier);
 					} else {
 						HashSet<String> modifiers = new HashSet<String>();
 						modifiers.add(modifier);
 						tagToModifiers.put(tag, modifiers);
 					}
 				}
 			}
 		}
 
 		// Added all tags with more than 1 structures into the common tags
 		// collection
 		Iterator<String> iter = tagToModifiers.keySet().iterator();
 		while (iter.hasNext()) {
 			String key = iter.next();
 			if (tagToModifiers.get(key).size() > 1) {
 				String commonTag = new String(key);
 				commonTag = commonTag.replaceAll("\\|+", "\\|");
 				commonTag = commonTag.replaceAll("\\|+$", "");
 				commonTags.add(key);
 			}
 		}
 
 		return commonTags;
 	}
 
 	/**
 	 * comma used for 'and': seen in TreatiseH, using comma for 'and' as in
 	 * "adductor , diductor scars clearly differentiated ;", which is the same
 	 * as "adductor and diductor scars clearly differentiated ;". ^m*n+,m*n+ or
 	 * m*n+,m*n+;$, or m,mn. Clauses dealt in commaand do not contain "and/or".
 	 * andortag() deals with clauses that do.
 	 * 
 	 * @param dataholderHandler
 	 */
 	public void commaAnd(DataHolder dataholderHandler) {
 		// cover m,mn
 
 		// last + =>*
 		// "(?:<[A-Z]*[NO]+[A-Z]*>[^<]+?<\/[A-Z]*[NO]+[A-Z]*>\\s*)+"
 		String nPhrasePattern = "(?:<[A-Z]*[NO]+[A-Z]*>[^<]+?<\\/[A-Z]*[NO]+[A-Z]*>\\s*)+";
 
 		// add last \\s*
 		// "(?:<[A-Z]*M[A-Z]*>[^<]+?<\/[A-Z]*M[A-Z]*>\\s*)"
 		String mPhrasePattern = "(?:<[A-Z]*M[A-Z]*>[^<]+?<\\/[A-Z]*M[A-Z]*>\\s*)";
 
 		// "(?:<[A-Z]*B[A-Z]*>[,:\.;<]<\/[A-Z]*B[A-Z]*>)"
 		String bPattern = "(?:<[A-Z]*B[A-Z]*>[,:.;<]<\\/[A-Z]*B[A-Z]*>)";
 
 		String commaPattern = "<B>,</B>";
 
 		String phrasePattern = mPhrasePattern + "\\s*" + nPhrasePattern;
 		String pattern = phrasePattern + "\\s+" + commaPattern + "\\s+(?:"
 				+ phrasePattern + "| |" + commaPattern + ")+";
 		String pattern1 = "^(" + pattern + ")";
 		String pattern2 = "(.*?)(" + pattern + ")\\s*" + bPattern + "\\$";
 		// changed last * to +
 		String pattern3 = "^((?:" + mPhrasePattern + "\\s+)+" + commaPattern
 				+ "\\s+(?:" + mPhrasePattern + "|\\s*|" + commaPattern + ")+"
 				+ mPhrasePattern + "+\\s*" + nPhrasePattern + ")";
 
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 
 			String sentenceCopy = "" + sentence;
 			sentenceCopy = sentenceCopy.replaceAll("></?", "");
 
 			Matcher m1 = StringUtility.createMatcher(sentenceCopy, pattern1);
 			Matcher m2 = StringUtility.createMatcher(sentenceCopy, pattern2);
 			Matcher m3 = StringUtility.createMatcher(sentenceCopy, pattern3);
 
 			// case 1
 			if (m1.find()) {
 				String tag = m1.group(1);
 				tag = tag.replaceAll(",", "and");
 				tag = tag.replaceAll("</?\\S+?>", "");
 				tag = StringUtility.trimString(tag);
 				// case 1.1
 				if (!StringUtility.isMatchedNullSafe(tag, " and$")) {
 					dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 							"", tag, "commaand[CA1]");
 				}
 			}
 			// case 2
 			else if (m2.find()) {
 				String g1 = m2.group(1);
 				String tag = m2.group(2);
 				if (!StringUtility.isMatchedNullSafe(g1, "\\b("
 						+ this.myLearnerUtility.getConstant().PREPOSITION + ")\\b")
 						&& !StringUtility.isMatchedNullSafe(g1, "<N>")) {
 					tag = tag.replaceAll(",", "and");
 					tag = tag.replaceAll("</?\\S+?>", "");
 					tag = StringUtility.trimString(tag);
 					// case 2.1.1
 					if (!StringUtility.isMatchedNullSafe(tag, " and$")) {
 						dataholderHandler.tagSentenceWithMT(sentenceID,
 								sentence, "", tag, "commaand[CA2]");
 					}
 
 				}
 			}
 			// case 3
 			else if (m3.find()) {
 				String tag = m3.group(1);
 				String g1 = m3.group(1);
 				// case 3.1
 				if (!StringUtility.isMatchedNullSafe(g1, "\\b("
 						+ this.myLearnerUtility.getConstant().PREPOSITION + ")\\b")) {
 					tag = tag.replaceAll(",", "and");
 					tag = tag.replaceAll("</?\\S+?>", "");
 					tag = StringUtility.trimString(tag);
 					// case 3.1.1
 					if (!StringUtility.isMatchedNullSafe(tag, " and$")) {
 						String[] tagWords = tag.split("\\s+");
 						List<String> tagWordsList = new ArrayList<String>(
 								Arrays.asList(tagWords));
 						tag = tagWordsList.get(tagWordsList.size() - 1);
 						String modifier = StringUtils.join(tagWordsList
 								.subList(0, tagWordsList.size() - 1), " ");
 						dataholderHandler.tagSentenceWithMT(sentenceID,
 								sentence, modifier, tag, "commaand[CA3]");
 					}
 				}
 			}
 		}
 	}
 	
 	public void normalizeModifiers(DataHolder dataholderHandler) {
 		Comparator<SentenceStructure> stringLengthComparator = new Comparator<SentenceStructure>() {
 			@Override
 			public int compare(SentenceStructure s1, SentenceStructure s2) {
 				String m1 = s1.getModifier();
 				String m2 = s2.getModifier();
 				if (m1.length() == m2.length()) {
 					return 0;
 				} else {
 					return m1.length() < m2.length() ? -1 : 1;
 				}
 			}
 		};
 
 		// Part 1
 		// non- and/or/to/plus cases
 		List<SentenceStructure> sentenceList = new ArrayList<SentenceStructure>();
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			String modifier = sentenceItem.getModifier();
 			boolean c1 = !StringUtils.equals(modifier, "");
 			boolean c2 = !StringUtility.isMatchedNullSafe(modifier,
 					" (and|or|nor|plus|to) ");
 			if (c1 && c2) {
 				sentenceList.add(sentenceItem);
 			}
 		}
 
 		Collections.sort(sentenceList, stringLengthComparator);
 		Collections.reverse(sentenceList);
 
 		for (SentenceStructure sentenceItem : sentenceList) {
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 			String tag = sentenceItem.getTag();
 			String modifier = sentenceItem.getModifier();
 
 			String mCopy = "" + modifier;
 			modifier = finalizeModifier(dataholderHandler, modifier, tag, sentence);
 			modifier = modifier.replaceAll("\\s*\\[.*?\\]\\s*", " ");
 			modifier = StringUtility.trimString(modifier);
 
 			if (!StringUtils.equals(mCopy, modifier)) {
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 						modifier, tag, "normalizemodifiers");
 			}
 		}
 
 		// Part 2
 		// deal with to: characterA to characterB organ (small to median shells)
 		List<SentenceStructure> sentenceList2 = new ArrayList<SentenceStructure>();
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			String modifier = sentenceItem.getModifier();
 			boolean c1 = StringUtility.isMatchedNullSafe(modifier, " to ");
 			if (c1) {
 				sentenceList2.add(sentenceItem);
 			}
 		}
 
 		Collections.sort(sentenceList2, stringLengthComparator);
 		for (SentenceStructure sentenceItem : sentenceList2) {
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 			String tag = sentenceItem.getTag();
 			String modifier = sentenceItem.getModifier();
 
 			String mCopy = "" + modifier;
 			modifier = modifier.replaceAll(".*? to ", "");
 			List<String> mWords = new ArrayList<String>(Arrays.asList(modifier
 					.split("\\s+")));
 			Collections.reverse(mWords);
 
 			String m = "";
 			int count = dataholderHandler.getSentenceCount(true, m, true, tag);
 			String modi = "" + m;
 			for (String word : mWords) {
 				m = word + " " + m;
 				m = m.replaceAll("\\s+$", "");
 				int c = dataholderHandler.getSentenceCount(true, m, true, tag);
 				if (c > count) {
 					count = c;
 					modi = "" + m;
 				}
 			}
 			// tagsentwmt($sentid, $sentence, $modi, $tag,
 			// "normalizemodifiers");
 			dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modi,
 					tag, "normalizemodifiers");
 		}
 
 		// Part 3
 		// modifier with and/or/plus
 		List<SentenceStructure> sentenceList3 = new ArrayList<SentenceStructure>();
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			String modifier = sentenceItem.getModifier();
 			boolean con = !StringUtility.isMatchedNullSafe(modifier,
 					" (and|or|nor|plus|to) ");
 			if (con) {
 				sentenceList3.add(sentenceItem);
 			}
 		}
 
 		Collections.sort(sentenceList3, stringLengthComparator);
 		Collections.reverse(sentenceList3);
 
 		for (SentenceStructure sentenceItem : sentenceList3) {
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 			String tag = sentenceItem.getTag();
 			String modifier = sentenceItem.getModifier();
 
 			String mCopy = "" + modifier;
 			modifier = this.finalizeCompoundModifier(dataholderHandler,
 					modifier, tag, sentence);
 
 			modifier = modifier.replaceAll("\\s*\\[.*?\\]\\s*", " ");
 			modifier = StringUtility.trimString(modifier);
 
 			if (!StringUtils.equals(mCopy, modifier)) {
 				// tagsentwmt($sentid, $sentence, $modifier, $tag,
 				// "normalizemodifiers");
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 						modifier, tag, "normalizemodifiers");
 			}
 		}
 
 		// Part 4
 		// modifier with and/or/plus
 		List<SentenceStructure> sentenceList4 = new ArrayList<SentenceStructure>();
 		for (SentenceStructure sentenceItem : dataholderHandler
 				.getSentenceHolder()) {
 			String modifier = sentenceItem.getModifier();
 			// ???
 			boolean con = !StringUtility.isMatchedNullSafe(modifier,
 					"[_ ](and|or|nor|plus|to)[ _]");
 			if (con) {
 				sentenceList4.add(sentenceItem);
 			}
 		}
 
 		Collections.sort(sentenceList4, stringLengthComparator);
 		Collections.reverse(sentenceList4);
 
 		for (SentenceStructure sentenceItem : sentenceList4) {
 			int sentenceID = sentenceItem.getID();
 			String sentence = sentenceItem.getSentence();
 			String tag = sentenceItem.getTag();
 			String modifier = sentenceItem.getModifier();
 
 			String mTag = "" + tag;
 			tag = this.finalizeCompoundTag(tag, sentence);
 			tag = tag.replaceAll("\\s*\\[.*?\\]\\s*", " ");
 			tag = StringUtility.trimString(tag);
 
 			if (!StringUtils.equals(mTag, tag)) {
 				// tagsentwmt($sentid, $sentence, $modifier, $tag,
 				// "normalizemodifiers");
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence,
 						modifier, tag, "normalizemodifiers");
 			}
 		}
 	}
 
 	public String finalizeCompoundModifier(DataHolder dataholderHandler, String modifier, String tag,
 			String sentence) {
 		// case 1
 		if (StringUtility.isMatchedNullSafe(modifier, "\\[")) {
 			return modifier;
 		}
 		
 		modifier = modifier.replaceAll("\\(.*?\\)", " ");
 		modifier = modifier.replaceAll("\\(.*", "");
 		modifier = modifier.replaceAll("\\W","");
 		modifier = modifier.replaceAll("\\s+", " ");
 		
 		String mCopy = ""+modifier;
 		String result = "";
 		String m = "";
 				String n = "";
 				
 		List<String> lastPart = new ArrayList(Arrays.asList(modifier.split("\\s+")));
 		Collections.reverse(lastPart);
 		int cut = 0;		
 		for (String l : lastPart) {
 			if (cut == 0 && StringUtility.isMatchedNullSafe(sentence, "<N>"+l)) {
 				n = l + " " + n;
 				n = StringUtility.trimString(n);
 			}
 			else {
 				cut = 1;
 				String tm = StringUtility.isMatchedNullSafe(n, "\\w") ? l + " "
 						+ n : l;
 				for (SentenceStructure sentenceItem : dataholderHandler
 						.getSentenceHolder()) {
 					if (StringUtils.equals(sentenceItem.getModifier(), tm)
 							&& StringUtils.equals(sentenceItem.getTag(), tag)) {
 						m = l + " " + m;
 					}
 				}
 				break;
 			}
 		}
 		
 		m = StringUtility.trimString(m);
 		n = StringUtility.trimString(n);
 		modifier = modifier.replaceAll("\\s*"+n, "");
 		
 		// components
 		List<String> parts = new ArrayList<String>();
 		List<String> conj = new ArrayList<String>();
 		conj.add("");
 		if (modifier != null) {
 			Matcher m1 = StringUtility.createMatcher(modifier, "(^.*?) (and|or|nor|plus) (.*)");
 			while (m1.find()) {
 				String g1 = m1.group(1);
 				String g2 = m1.group(2);
 				String g3 = m1.group(3);
 				parts.add(g1);
 				parts.add(g2);
 				modifier = g3;
 				m1 = StringUtility.createMatcher(modifier, "(^.*?) (and|or|nor|plus) (.*)");
 			}
 		}
 		parts.add(modifier);
 		
 		// at least one m in a part
 //		for (String part : parts) {
 		for (int i = 0; i < parts.size(); i++) {
 			String part = parts.get(i);
 			String[] words = part.split("\\s+");
 			boolean isFound = false;
 			String r = "";
 			
 			for (String word : words) {
 				if ((this.checkedModifiers.containsKey(word) && this.checkedModifiers.get(word)) || StringUtility.isMatchedNullSafe(sentence, "<N>"+word)) {
 					isFound = true;
 					r = r + " " + word;
 				}
 			}
 			r = StringUtility.trimString(r);
 			
 			result = result + " " + conj.get(i)+ " "+r;
 			String regex2 = "\\b(" + this.myLearnerUtility.getConstant().CHARACTER + "|" + this.myLearnerUtility.getConstant().STOP
 					+ "|" + this.myLearnerUtility.getConstant().NUMBER + "|" + this.myLearnerUtility.getConstant().CLUSTERSTRING
 					+ ")\\b";
 			if (!StringUtility.isMatchedNullSafe(r, "\\w")
 					|| StringUtility.isMatchedNullSafe(r, regex2)) {
 				result = "";
 				break;
 			}
 		}
 		result = StringUtility.isMatchedNullSafe(result, "\\w") ? result
 				+ " " + n : m + " " + n;
 		result = StringUtility.trimString(result);
 		
 		return result;
 	}
 
 	// [bm]+n+&[bm]+n+
 	public String finalizeCompoundTag(String tag, String sentence) {
 		// avoid unmatched ( in regexp
 		tag = tag.replaceAll("\\(.*?\\)", " ");
 		tag = tag.replaceAll("\\(.*", "");
 		tag = tag.replaceAll("\\s+", " ");
 		
 		String tCopy = "" + tag;
 		String result = "";
 		
 		// components
 		List<String> parts = new ArrayList<String>();
 		List<String> conj = new ArrayList<String>();
 		conj.add("");
 		
 		Matcher m1 = StringUtility.createMatcher(tag, "(^.*?)[_ ](and|or|nor|plus)[_ ](.*)");
 		while (m1.find()) {
 			String g1 = m1.group(1);
 			String g2 = m1.group(2);
 			String g3 = m1.group(3);
 			parts.add(g1);
 			conj.add(g2);
 			tag = g3;
 			m1 = StringUtility.createMatcher(tag, "(^.*?)[_ ](and|or|nor|plus)[_ ](.*)");
 		}
 		
 		parts.add(tag);
 		
 		// at least one m in a part
 //		for (String part : parts) {
 		for (int i = 0; i < parts.size(); i++) {
 			String part = parts.get(i);
 			String[] words = part.split("\\s+");
 			boolean isFoundM = false;
 			String r = "";
 			for (String word : words) {
 				String escapedW = StringUtility.escapePerlRegex(word);
 				if ((this.checkedModifiers.containsKey(word) && this.checkedModifiers
 						.get(word))
 						|| StringUtility.isMatchedNullSafe(sentence, "<N>"
 								+ escapedW)) {
 					isFoundM = true;
 					r = r + " " + word;
 				}
 			}
 			String regex = "\\b(" + this.myLearnerUtility.getConstant().CHARACTER + "|" + this.myLearnerUtility.getConstant().STOP
 					+ "|" + this.myLearnerUtility.getConstant().NUMBER + "|" + this.myLearnerUtility.getConstant().CLUSTERSTRING
 					+ ")\\b";
 			r = r.replaceAll(regex, "");
 			r = StringUtility.trimString(r);
 			
 			if (StringUtility.isMatchedNullSafe(r, "\\w")) {
 				result = result + " " + conj.get(i) +" "+r;
 			}
 		}
 		
 		result = result.replaceAll("\\s+", " ");
 		result = StringUtility.trimString(result);
 		
 		return result;
 	}
 
 	public String finalizeModifier(DataHolder dataholderHandler, String modifier, String tag, String sentence) {
 		String fModifier = "";
 		modifier = modifier.replaceAll("\\[.*?\\]", "");
 		modifier = StringUtility.trimString(modifier);
 		if (StringUtility.isMatchedNullSafe(modifier, "\\w")) {
 			List<String> mWords = new ArrayList<String>(Arrays.asList(modifier.split("\\s+")));
 			Collections.reverse(mWords);
 			
 			for (String mWord : mWords) {
 				boolean isModifier = this.isModifier(dataholderHandler, mWord, modifier, tag);
 				if (isModifier) {
 					fModifier = mWord + " " + fModifier;
 				}
 				else {
 					break;
 				}
 			}
 			
 			fModifier = fModifier.replaceAll("\\s+", "");
 		}
 		
 		return fModifier;
 	}
 
 	public boolean isModifier(DataHolder dataholderHandler, String word, String modifier, String tag) {
 		if (this.checkedModifiers.containsKey(word)) {
 			if (this.checkedModifiers.get(word)) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 		
 		// if word is a "s", return 1
 		Set<String> nouns = new HashSet<String>(Arrays.asList("s p n"
 				.split(" ")));
 		List<Entry<WordPOSKey, WordPOSValue>> entries = dataholderHandler
 				.getWordPOSEntriesByWordPOS(word, nouns);
 		if (entries.size() > 0) {
 
 			this.checkedModifiers.put(word, true);
 			return true;
 
 		}
 		
 		// if word is a "b", and not a "m", return 0
 		Set<String> bPOS = new HashSet<String>();
 		bPOS.add("b");
 		List<Entry<WordPOSKey, WordPOSValue>> boundaries = dataholderHandler
 				.getWordPOSEntriesByWordPOS(word, bPOS);
 		boolean c1 = (boundaries.size() > 0);		
 		boolean c2 = dataholderHandler.getModifierHolder().containsKey(word);
 		if (c1 && !c2) {
 			// the word is a boundary word, but not a modifier
 			this.checkedModifiers.put(word, false);
 			return false;
 		}
 		
 		if (!c1 && c2) {
 			this.checkedModifiers.put(word, true);
 			return true;
 		}
 		
 		// when word has been used as "b" and "m" or neither "b" nor "m" and is not a "s"
 		int mCount = this.getMCount(dataholderHandler, word);
 		String wCopy = ""+word;
 		if (StringUtility.isMatchedNullSafe(word, "_")) {
 			wCopy = wCopy.replaceAll("_", " - ");
 		}
 		
 		int tCount = 0;
 		String pattern = "(^| )"+wCopy+" ";
 		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
 			String oSentence = sentenceItem.getOriginalSentence();
 			if (StringUtility.isMatchedNullSafe(oSentence, pattern)) {
 				tCount++;
 			}
 		}
 		
 		if (tCount == 0 || tCount > 0.25 * mCount) {
 			this.checkedModifiers.put(word, false);
 			return false;
 		}
 		else {
 			this.checkedModifiers.put(word, true);
 			return true;			
 		}
 	}
 	
 	public int getMCount(DataHolder dataholderHandler, String word) {
 		int count = 0;
 		String pattern = "(>| )"+word+"(</B></M>)? <N";
 		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
 			String sentence = sentenceItem.getSentence();
 			if (StringUtility.isMatchedNullSafe(sentence, pattern)) {
 				count++;
 			}
 		}
 		
 		return count;
 	}
 	
 	public void normalizeTags(DataHolder dataholderHandler) {
 		for (SentenceStructure sentenceItem : dataholderHandler.getSentenceHolder()) {
 			int sentenceID = sentenceItem.getID();
 			String modifier = sentenceItem.getModifier();
 			String tag = sentenceItem.getTag();
 			if (tag != null && StringUtils.equals(tag, "ignore")) {				
 				tag = this.normalizeItem(tag);
 				modifier = this.normalizeItem(modifier);
 			}
 			
 			String sentence = sentenceItem.getSentence();
 			sentence = sentence.replaceAll("</?[NBM]>", "");
 			dataholderHandler.getSentence(sentenceID).setSentence(sentence);
 			if (StringUtility.isMatchedNullSafe(tag, "\\w")) {
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, tag, "normalizetags");
 			}
 			else {
 				dataholderHandler.tagSentenceWithMT(sentenceID, sentence, modifier, null, "normalizetags");
 			}
 		}
 	}
 	
 	
 
 	public String normalizeItem(String tag) {
 		tag = tag.replaceAll("\\s*NUM\\s*", " ");
 		tag = StringUtility.trimString(tag);
 
 		if (StringUtility.isMatchedNullSafe(tag, "\\w")) {
 			tag = tag.replaceAll("\\[", "[*");
 			tag = tag.replaceAll("\\]", "*]");
 
 			String[] twSegs = tag.split("[\\]\\[]");
 
 			StringBuilder tagSB = new StringBuilder();
 
 			for (int j = 0; j < twSegs.length; j++) {
 				StringBuilder outSB = new StringBuilder();
 				// case 1
 				if (StringUtility.isMatchedNullSafe(twSegs[j], "\\*")) {
 					twSegs[j] = twSegs[j].replaceAll("\\*", "");
 					String[] tagWords = twSegs[j].split("\\s+");
 					outSB.append('[');
 					for (int i = 0; i < tagWords.length; i++) {
 						tagWords[i] = this.myLearnerUtility
 								.getWordFormUtility().getSingular(tagWords[i]);
 						outSB.append(tagWords[i]);
 						outSB.append(" ");
 					}
 					outSB.deleteCharAt(outSB.length() - 1);
 					outSB.append(']');
 				} 
 				// case 2
 				else if (StringUtility.isMatchedNullSafe(twSegs[j], "\\w")) {
 					String[] tagWords = twSegs[j].split("\\s+");
 					for (int i = 0; i < tagWords.length; i++) {
 						tagWords[i] = this.myLearnerUtility
 								.getWordFormUtility().getSingular(tagWords[i]);
 						outSB.append(tagWords[i]);
 						outSB.append(" ");
 					}
 					outSB.deleteCharAt(outSB.length() - 1);
 				}
 				String out = outSB.toString();
 				if (StringUtility.isMatchedNullSafe(out, "\\w")) {
 					tagSB.append(out.toString());
 					tagSB.append(' ');
 				}
 			}
 
 			tagSB.deleteCharAt(tagSB.length() - 1);
 			tag = tagSB.toString();
 			tag = tag.replaceAll("\\s+", " ");
 		}
 
 		return tag;
 	}
 	
 	/**
 	 * set saved_flag to red for the following terms in preparation to run the Parser
 	 * 1. words that are not in allwords table 
 	 * 2. special words added
 	 */
 	public void prepareTables4Parser(DataHolder dataholderHandler) {
 		Set<String> toRemove = new HashSet<String>();
 		toRemove.addAll(this.myLearnerUtility.getConstant().pronounWords);
 		toRemove.addAll(this.myLearnerUtility.getConstant().characterWords);
 		toRemove.addAll(this.myLearnerUtility.getConstant().numberWords);
 		toRemove.addAll(this.myLearnerUtility.getConstant().clusterStringWords);
 		toRemove.addAll(this.myLearnerUtility.getConstant().pronounWords);
 		toRemove.addAll(this.myLearnerUtility.getConstant().stopWords);	
 		
 		Set<String> unknownWords =dataholderHandler.getUnknownWordHolder().keySet(); 
 		
 		// set saved_flag to red in WordPOS collection
 		Iterator<Entry<WordPOSKey, WordPOSValue>> iter = dataholderHandler.getWordPOSHolderIterator();
 		while (iter.hasNext()) {
 			Entry<WordPOSKey, WordPOSValue> entry = iter.next();
 			WordPOSKey key = entry.getKey();
 			WordPOSValue value = entry.getValue();
 			String word = key.getWord();
 //			boolean c1 = toRemove.contains(word);
 //			boolean c2 = StringUtility.isMatchedNullSafe(word, "[a-z]");
 //			boolean c3 = unknownWords.contains(word);
 			
 			if (toRemove.contains(word)
 					|| !StringUtility.isMatchedNullSafe(word, "[a-z]")
 					|| !unknownWords.contains(word)) {
 				value.setSavedFlag("red");
 			}
 		}
 		
 		// handle -ly words
 		// If a word in WordPOS collection, has ending of -ly, and after
 		// removing the -ly ending, it appears in the UnknownWords collections,
 		// then set the savedFlag to "red"
 		Iterator<Entry<WordPOSKey, WordPOSValue>> iter2 = dataholderHandler.getWordPOSHolderIterator();
 		while (iter2.hasNext()) {
 			Entry<WordPOSKey, WordPOSValue> entry = iter2.next();
 			WordPOSKey key = entry.getKey();
 			WordPOSValue value = entry.getValue();
 			String lyWord = key.getWord();
 			if (StringUtility.isMatchedNullSafe(lyWord, "ly$")) {
 				String nWord = lyWord.replaceAll("ly$", "");
 				if (unknownWords.contains(nWord)) {
 					value.setSavedFlag("red");
 				}
 			}
 		}
 	}
 
 	// some unused variables in perl
 	// directory of /descriptions folder
 	private String desDir = "";
 	// directory of /characters folder
 	private String chrDir = "";
 	// prefix for all tables generated by this program
 	private String prefix = "";
 	// default general tag
 
 	// knowledge base
 	private String knlgBase = "phenoscape";
 
 	private int DECISIONID = 0;
 
 	private Map<String, String> numberRecords = new HashMap<String, String>(); // word->(p|s)
 	private Map<String, String> singularRecords = new HashMap<String, String>();// word->singular
 	private Map<String, String> POSRecords = new HashMap<String, String>(); // word->POSs
 	// private Map<String, String> POSRecordsRECORDS = new HashMap<String,
 	// String>();
 	private String NEWDESCRIPTION = ""; // record the index of sentences that
 										// ends a description
 
 	private Hashtable<String, String> PLURALS = new Hashtable<String, String>();
 
 	private String TAGS = "";
 
 	// grouped #may contain q but not the last m, unless it is followed by a p
 	private String mptn = "((?:[mbq][,&]*)*(?:m|b|q(?=[pon])))";
 	// grouped #must present, no q allowed
 	private String nptn = "((?:[nop][,&]*)*[nop])";
 	// grouped #when following a p, a b could be a q
 	private String bptn = "([,;:\\\\.]*\\$|,*[bm]|(?<=[pon]),*q)";
 	private String SEGANDORPTN = "(?:" + mptn + nptn + ")";
 	private String ANDORPTN = "^(?:" + SEGANDORPTN + "[,&]+)*" + SEGANDORPTN
 			+ bptn;
 
 	// utility method
 	public LearnerUtility getLearnerUtility() {
 		return this.myLearnerUtility;
 	}
 
 	public ITokenizer getTokenizer() {
 		return this.myTokenizer;
 	}
 
 	public Configuration getConfiguration() {
 		return this.myConfiguration;
 	}
 
 	public static void main(String[] args) {
 		assertEquals("tagAllSentenceHelper", 1, 12);
 	}
 
 }
