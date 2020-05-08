 package semanticMarkup.ling.learn.knowledge;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import semanticMarkup.core.Treatment;
 import semanticMarkup.ling.Token;
 import semanticMarkup.ling.learn.LearnerUtility;
 import semanticMarkup.ling.learn.StringUtility;
 import semanticMarkup.ling.learn.dataholder.DataHolder;
 
 public class Initiation implements IModule {
 	
 	private LearnerUtility myLearnerUtility;
 	private List<Treatment> treatments;
 	private int numLeadWords;
 
 	public Initiation(LearnerUtility learnerUtility, int num) {
 		this.myLearnerUtility = learnerUtility;
 		this.numLeadWords = num;
 		treatments = new LinkedList<Treatment>();
 	}
 
 	@Override
 	public void run(DataHolder myDataHolder) {
 		this.populateSentence(this.treatments, myDataHolder);
 		this.populateUnknownWordsTable(myDataHolder.allWords, myDataHolder);
 
 	}
 	
 	public void loadTreatments(List<Treatment> treatments) {
 		this.treatments.addAll(treatments);
 	}
 	
 	/**
 	 * 
 	 * @param treatments
 	 * @return number of sentences
 	 */
 	public int populateSentence(List<Treatment> treatments, DataHolder myDataHolder) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.populateSentence");
 		myLogger.info("Enter");
 		myLogger.info("Reading sentences...");
 
 		String fileName;
 		int type;
 		String text;
 		int SENTID = 0;
 
 		for (int i = 0; i < treatments.size(); i++) {
 			Treatment tm = treatments.get(i);
 			fileName = tm.getFileName();
 			text = tm.getDescription();
 			type = this.myLearnerUtility.getType(fileName);
 
 			if (text != null) {
 				// process this text
 				text = this.handleText(text);
 				myLogger.debug("Text: " + text);
 
 				//do sentence segmentation
 				List<Token> sentences = this.myLearnerUtility.segmentSentence(text);
 
 				List<String> sentCopy = new LinkedList<String>();
 				List<Integer> validIndex = new LinkedList<Integer>();
 				
 				// for each sentence, do some operations
 				for (int j = 0; j < sentences.size(); j++) {
 					myLogger.debug("Sentence " + j + ": " + sentences.get(j).getContent());
 					
 					// if(!/\w+/){next;}
 					if (!sentences.get(j).getContent().matches("^.*\\w+.*$")) {
 						continue;
 					}
 
 					// This is a valid sentence, save the index
 					validIndex.add(j);
 
 					// restore marks in brackets
 					sentences.get(j).setContent(this.myLearnerUtility.restoreMarksInBrackets(sentences.get(j).getContent()));
 					// Make a copy of the sentence
 					sentCopy.add(sentences.get(j).getContent());
					// process the sentence
//					System.out.println(true);
					
					if (StringUtility.isMatchedNullSafe("ral dermal tubercles exhibi", (sentences.get(j).getContent()))) {
						System.out.println();
					}
					System.out.println(StringUtility.isMatchedNullSafe("oral dermal tubercles exhibiting organ", (sentences.get(j).getContent())));
 					// process the sentence
 					sentences.get(j).setContent(this.handleSentence(sentences.get(j).getContent()));
 
 					// store all words
 					myDataHolder.allWords = this.getAllWords(sentences.get(j).getContent(), myDataHolder.allWords);
 				}
 
 				for (int j = 0; j < validIndex.size(); j++) {
 					String line = sentences.get(validIndex.get(j)).getContent();
 					String oline = sentCopy.get(j);
 
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
 					oline = this.myLearnerUtility.restoreMarksInBrackets(oline);
 					oline = oline.replaceAll("\'", " ");
 
 					List<String> nWords = this.getFirstNWords(line,
 							this.numLeadWords);
 					String lead = "";
 					Iterator<String> iter = nWords.iterator();
 					while (iter.hasNext()) {
 						String w = iter.next();
 						lead = lead + w + " ";
 					}
 					lead = lead.replaceAll("\\s$", "");
 
 					String status = "";
 					if (myLearnerUtility.getWordFormUtility().getNumber(nWords.get(0)).equals("p")) {
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
 
 					myDataHolder.addSentence(source, line, oline, lead,
 							status, null, null, typeStr);
 
 					SENTID++;
 				}
 			}
 		}
 
 		myLogger.info("Total sentences = " + SENTID);
 		myLogger.info("Quite");
 
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
 		
 		text = this.myLearnerUtility.hideMarksInBrackets(text);
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
 		sentence = this.myLearnerUtility.addSpace(sentence, "\\W");
 
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
 	 * Insert all words in WORDS into getUnknownWordHolder(). Insert those formed by
 	 * non words characters into getWordPOSHolder()
 	 * 
 	 * @param WORDS
 	 * @return
 	 */
 	public int populateUnknownWordsTable(Map<String, Integer> WORDS, DataHolder myDataHolder) {
 		PropertyConfigurator.configure( "conf/log4j.properties" );
 		Logger myLogger = Logger.getLogger("learn.pupluateUnknownWords");
 
 		myLogger.trace("Enter");
 		int count = 0;
 
 		Iterator<String> iter = WORDS.keySet().iterator();
 
 		while (iter.hasNext()) {
 			String word = iter.next();
 			if ((!word.matches("^.*\\w.*$")) || (word.matches("^.*ous$"))) {
 				myDataHolder.addUnknown(word, word);
 				myDataHolder.updateDataHolder(word, "b", "", "wordpos", 1);
 			} else {
 				myDataHolder.addUnknown(word, "unknown");
 			}
 			count++;
 		}
 
 		myLogger.info("Total words = " + count);
 		
 		myLogger.trace("Return: "+count);
 		myLogger.trace("Quite\n");
 		return count;
 	}
 	
 	/**
 	 * Put all words in this sentence into the words map
 	 * 
 	 * @param sent
 	 * @param words
 	 *            a map mapping all words already known to their counts
 	 * @return a new map of all words, including words in sent
 	 */
 	public Map<String, Integer> getAllWords(String sentence,
 			Map<String, Integer> words) {
 		List<String> tokens = this.getLearnerUtility().tokenizeText(sentence, "all");
 
 		for (String token: tokens) {
 			if (words.containsKey(token)) {
 				int count = words.get(token);
 				count = count + 1;
 				words.put(token, count);
 			} else {
 				words.put(token, 1);
 			}
 		}
 
 		return words;
 	}
 
 	/**
 	 * returns the first n words of the sentence
 	 * 
 	 * @param sent
 	 *            the sentence
 	 * @param n
 	 *            number of words to be returned
 	 * @return the first n words of the sentence. If the number of words in the
 	 *         sentence is less than n, return all of them.
 	 */
 	public List<String> getFirstNWords(String sentence, int n) {
 		List<String> nWords = new ArrayList<String>();
 
 		if (sentence == null || sentence == "") {
 			return nWords;
 		}
 		
 		List<String> tokens = this.getLearnerUtility().tokenizeText(sentence, "firstseg");
 		
 		
 		int minL = tokens.size() > n ? n : tokens.size();
 		for (int i = 0; i < minL; i++) {
 			nWords.add(tokens.get(i));
 		}
 
 		return nWords;
 	}
 	
 	public LearnerUtility getLearnerUtility(){
 		return this.myLearnerUtility;
 	}
 
 }
