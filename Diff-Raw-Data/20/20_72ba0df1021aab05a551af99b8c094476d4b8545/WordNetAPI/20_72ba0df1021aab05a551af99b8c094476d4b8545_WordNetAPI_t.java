 package semanticMarkup.knowledge.lib;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import semanticMarkup.knowledge.IPOSKnowledgeBase;
 import semanticMarkup.ling.pos.POS;
 
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 
 import edu.mit.jwi.Dictionary;
 import edu.mit.jwi.IDictionary;
 import edu.mit.jwi.RAMDictionary;
 import edu.mit.jwi.item.IIndexWord;
 import edu.mit.jwi.morph.WordnetStemmer;
 
 public class WordNetAPI implements IPOSKnowledgeBase {
 
 	private IDictionary dictionary;
 	
 	@Inject
 	public WordNetAPI(@Named("WordNetAPI_Sourcefile") String path, @Named("WordNetAPI_LoadInRAM") boolean loadInRAM) throws IOException {
 		if(loadInRAM) 
 			dictionary = new RAMDictionary(new File(path), RAMDictionary.BACKGROUND_LOAD);
 		else 
 			dictionary = new Dictionary(new File(path));
 		dictionary.open();
 	}
 
 //	@Override
 //	public boolean isNoun(String word) {
 //		return dictionary.getIndexWord(word, edu.mit.jwi.item.POS.NOUN) != null;
 //	}
 	
 	@Override
 	public boolean isNoun(String word) {
 		WordnetStemmer myWordnetStemmer = new WordnetStemmer(dictionary);
		List<String> stems = myWordnetStemmer.findStems(word, edu.mit.jwi.item.POS.NOUN);
 
 		for (int i = 0; i < stems.size(); i++) {
 			String wordStem = stems.get(i);
 			if(dictionary.getIndexWord(wordStem, edu.mit.jwi.item.POS.NOUN) != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean isAdjective(String word) {
 		return dictionary.getIndexWord(word, edu.mit.jwi.item.POS.ADJECTIVE) != null;
 	}
 
 	@Override
 	public boolean isAdverb(String word) {
 		return dictionary.getIndexWord(word, edu.mit.jwi.item.POS.ADVERB) != null;
 	}
 
 	@Override
 	public boolean isVerb(String word) {
 		return dictionary.getIndexWord(word, edu.mit.jwi.item.POS.VERB) != null;
 	}
 	
 	/*@Override
 	public POS getMostLikleyPOS(String word) {		
 		System.out.println("getMostLikleyPOS for " + word);
 		int maxTagSenseCount = 0;
 		edu.mit.jwi.item.POS mostLikelyPOS = null;
 		for(edu.mit.jwi.item.POS pos : edu.mit.jwi.item.POS.values()) {
 				
 			System.out.println(pos);
 			IIndexWord indexWord = dictionary.getIndexWord(word, pos);
 			
 			if(indexWord != null) {
 				System.out.println(indexWord.toString());
 				int tagSenseCount = dictionary.getIndexWord(word, pos).getTagSenseCount();
 				System.out.println("tag sense count " + tagSenseCount + "\n");
 				if(tagSenseCount > maxTagSenseCount) {
 					maxTagSenseCount = tagSenseCount;
 					mostLikelyPOS = pos;
 				}
 			} else 
 				System.out.println();
 		}	
 		
 		return translateWordNetPOSToPennPOS(mostLikelyPOS);
 	}*/
 	
 	@Override
 	public POS getMostLikleyPOS(String word) {
 		WordnetStemmer stemmer = new WordnetStemmer(dictionary);
 		
 		int maxWordIdCount = 0;
 		edu.mit.jwi.item.POS mostLikelyPOS = null;
 		for(edu.mit.jwi.item.POS pos : edu.mit.jwi.item.POS.values()) {
 			List<String> stems = stemmer.findStems(word, pos);
 			for(String stem : stems) {
 				IIndexWord indexWord = dictionary.getIndexWord(stem, pos);
 				if(indexWord!=null) {
 					int tagSenseCount = indexWord.getTagSenseCount();
 					int wordIdCount = indexWord.getWordIDs().size();
 					if(wordIdCount > maxWordIdCount) {
 						maxWordIdCount = wordIdCount;
 						mostLikelyPOS = pos;
 					}
 				}
 			}
 		}	
 		
 		return translateWordNetPOSToPennPOS(mostLikelyPOS);
 	}
 	
 	private POS translateWordNetPOSToPennPOS(edu.mit.jwi.item.POS pos) {
 		if(pos==null)
 			return null;
 		switch(pos) {
 		case NOUN:
 			return POS.NN;
 		case VERB:
 			return POS.VB;
 		case ADJECTIVE:
 			return POS.JJ;
 		case ADVERB:
 			return POS.RB;
 		default:
 			return null;
 		}
 	}
 
 //	@Override
 //	public boolean contains(String word) {
 //		for(edu.mit.jwi.item.POS pos : edu.mit.jwi.item.POS.values()) {
 //			IIndexWord indexWord = dictionary.getIndexWord(word, pos);
 //			if(indexWord!=null)
 //				return true;
 //		}
 //		return false;
 //	}
 	
 	@Override
 	public boolean contains(String word) {
 		WordnetStemmer myWordnetStemmer = new WordnetStemmer(dictionary);
 		List<String> stems = myWordnetStemmer.findStems(word, null);
 
 		for (int i = 0; i < stems.size(); i++) {
 			String wordStem = stems.get(i);
 
 			for (edu.mit.jwi.item.POS pos : edu.mit.jwi.item.POS.values()) {
 				IIndexWord indexWord = dictionary.getIndexWord(wordStem, pos);
 				if (indexWord != null)
 					return true;
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public List<String> getSingulars(String word) {
 		WordnetStemmer stemmer = new WordnetStemmer(dictionary);
 		List<String> singulars = stemmer.findStems(word, edu.mit.jwi.item.POS.NOUN);
 		return singulars;
 	}
 	
 	public static void main(String[] args) throws IOException{
 		
 		WordNetAPI wordNetAPI = new WordNetAPI("C://Program Files (x86)//WordNet//2.1//dict", true);
 		System.out.println(wordNetAPI.getMostLikleyPOS("critically"));
 		System.out.println(wordNetAPI.isAdjective("dog"));
 		
 		// construct the dictionary object and open it
 		/*IDictionary dict = new Dictionary(new File("C://Program Files (x86)//WordNet//2.1//dict"));
 		dict.open();
 		// look up first sense of the word "dog"
 		IIndexWord idxWord = dict.getIndexWord("dog", POS.NOUN);
 		IWordID wordID = idxWord.getWordIDs().get(0);
 		IWord word = dict.getWord(wordID);
 		System.out.println("Id = " + wordID);
 		System.out.println("Lemma = " + word.getLemma());
 		System.out.println("Gloss = " + word.getSynset().getGloss());*/
 		
 	}
 
 	@Override
 	public void addVerb(String word) {}
 
 	@Override
 	public void addNoun(String word) {}
 
 	@Override
 	public void addAdjective(String word) {}
 
 	@Override
 	public void addAdverb(String word) {}
 
 	public boolean isSoleAdjective(String word) {
 		return (!this.isNoun(word)) 
 				&& (!this.isVerb(word)) 
 				&& (this.isAdjective(word))
 				&& (!this.isAdverb(word));
 	}
 }
