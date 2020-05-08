 package filters;
 
 import general.Email;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.mail.MessagingException;
 
 import weka.core.Attribute;
 import weka.core.FastVector;
 
 public class WordFrequencyFilterCreator implements FilterCreator {
 
 	//Map from the label (class) name to its LabelTermFrequencyManager 
 	private HashMap<String, LabelTermFrequencyManager> labelFreqMgrMap;
 	
 	//Map from each word to the set of labels it appeared in (for the Inverse Document Frequency [IDF] calculations)
 	private HashMap<String, HashSet<String>> wordToLabelsMap;
 	
 	//Appending a prefix to each word that will become a feature reduces the probability of having 2 features from different filters with the same name, which causes a run time error
 	private final String ATT_NAME_PREFIX = "WFF_";
 	
 	//Maximum number of words feature taken from EACH label 
 	private int impWordsPerLabel;
 	
 	//[own-heuristic] Words that have TF-IDF score less than (thresholdPercentage/100) * maximum TF-IDF score in the label, will be ignored
 	//To turn this feature off, set the threshold to zero
 	private int thresholdPercentage;
 	// normalized frequencies
 	private boolean freqNormalization;
 	private boolean useBinaryAttributes;
 	
 	private final int DEFAULT_IMP_WORDS_PER_LABEL = 80;
 	private final int DEFAULT_THRESHOLD_PERCENTAGE = 20;
 	
 	//grams
 	private final int NGRAMS_MAX = 1;
 	private final int[] IGNORED_GRAMS = new int[] {};
 
 	public WordFrequencyFilterCreator() {
 		labelFreqMgrMap = new HashMap<String, WordFrequencyFilterCreator.LabelTermFrequencyManager>();
 		wordToLabelsMap = new HashMap<String, HashSet<String>>();
 		
 		impWordsPerLabel = DEFAULT_IMP_WORDS_PER_LABEL;
 		thresholdPercentage = DEFAULT_THRESHOLD_PERCENTAGE;
 		freqNormalization = true;
 		useBinaryAttributes = true;
 		
 		// sort ignored grams array
 		Arrays.sort(IGNORED_GRAMS);
 	}
 
 	/**
 	 * Getter for impWordsPerLabel
 	 * @return Value of the maximum number of words feature taken from EACH label
 	 */
 	public int getImpWordsPerLable(){
 		return this.impWordsPerLabel;
 	}
 
 	/**
 	 * Setter for impWordsPerLable
 	 * @param impWordsPerLabel Value of the maximum number of words feature taken from EACH label (>= 0)
 	 */
 	public void setImpWordsPerLabel(int impWordsPerLabel){
 		if(impWordsPerLabel >= 0)
 			this.impWordsPerLabel = impWordsPerLabel;
 	}
 
 	/**
 	 * getter for the thresholdPercentage
 	 * @return The percentage value that specifies the minimum feature score that will be considered
 	 * Words that have score less than (thresholdPercentage/100) * maximum TF-IDF score in the label, will be ignored
 	 */
 	public int getTresholdPercentage(){
 		return this.thresholdPercentage;
 	}
 	
 	/**
 	 * Setter for thresholdPercentage
 	 * @param thresholdPercentage Value of the threshold percentage that specifies the minimum feature score that will be considered
 	 */
 	public void setThresholdPercentage(int thresholdPercentage){
 		if(thresholdPercentage >=0 && thresholdPercentage <= 100){
 			this.thresholdPercentage = thresholdPercentage;
 		}
 	}
 
 	/**
 	 * Getter for FreqNormalization
 	 * @return True if Frequency Normalization is applied. False otherwise
 	 */
 	public boolean getFreqNormalization(){
 		return freqNormalization;
 	}
 	
 	/**
 	 * Setter for FreqNormalization
 	 * @param freqNormalization True if Frequency Normalization is required to be applied. False otherwise
 	 */
 	public void setFreqNormalization(boolean freqNormalization){
 		this.freqNormalization = freqNormalization;
 	}
 	
 	/**
 	 * Getter for the useBinaryAttributes
 	 * @return True if weka binary attributes is used. False otherwise
 	 */
 	public boolean getUseBinaryAttributes(){
 		return useBinaryAttributes;
 	}
 	
 	/**
 	 * set useBinaryAttributes option
 	 * @param useBinaryAttributes True for binaryAttributes, false for double attributes
 	 */
 	public void setUseBinaryAttributes(boolean useBinaryAttributes){
 		this.useBinaryAttributes = useBinaryAttributes;
 	}
 	
 	/**
 	 * builds Ngrams from the given tokens list starting from 1 to NGRAMS_MAX ,
 	 * ignoring the grams specified in the IGRNOREDGRAMS array
 	 * 
 	 * @param wordsList
 	 *            array of strings (tokens)
 	 * @return List of hashmaps where the first contains the unigrams count the
 	 *         second contains the bigram counts .. etc
 	 */
 	private List<HashMap<String, Double>> buildGrams(String[] wordsList) {
 		ArrayList<HashMap<String, Double>> gramsList = new ArrayList<HashMap<String, Double>>();
 		for (int i = 1; i <= NGRAMS_MAX; ++i) {
 			HashMap<String, Double> grams = new HashMap<String, Double>();
 			// if this Ngrams are not ignored
 			if (Arrays.binarySearch(IGNORED_GRAMS, i) < 0) {
 				for (int j = 0; j < wordsList.length - i + 1; j++) {
 					String gram = wordsList[j];
 					for (int j2 = j + 1; j2 < j + i; j2++)
 						gram += " " + wordsList[j2];
 
 					if (grams.containsKey(gram))
 						grams.put(gram, grams.get(gram) + 1);
 					else
 						grams.put(gram, 1.0);
 				}
 			}
 			gramsList.add(grams);
 		}
 		return gramsList;
 	}
 
 	/**
 	 * extracts the Attributes which are the important words from all labels
 	 * 
 	 * @return an ArrayList of Attribute objects
 	 */
 	private ArrayList<Attribute> extractAttributes() {
 		ArrayList<Attribute> atts = new ArrayList<Attribute>();
 
 		FastVector fv = new FastVector(2);
 		fv.addElement("False");
 		fv.addElement("True");
 
 		Iterator<Map.Entry<String, LabelTermFrequencyManager>> itr = labelFreqMgrMap
 				.entrySet().iterator();
 		HashSet<String> uniqueWords = new HashSet<String>();
 		while (itr.hasNext()) {
 			Map.Entry<String, LabelTermFrequencyManager> pair = itr.next();
 			String[] words = pair.getValue().extractImportantWords(impWordsPerLabel);
 			for(int i=0; i<words.length; i++){
 				if(!uniqueWords.contains(words[i])){
 					uniqueWords.add(words[i]);
 					if(useBinaryAttributes){
 						atts.add(new Attribute(ATT_NAME_PREFIX + words[i], fv));
 					} else{
 						atts.add(new Attribute(ATT_NAME_PREFIX + words[i]));
 					}
 				}
 			}
 		}
 
 		return atts;
 	}
 
 	@Override
 	public Filter createFilter(ArrayList<Email> emails) {
 		// Steps
 		// 1- loop on emails and for each email
 		// a- calculate normalized frequencies
 		// b- update frequencies for each label (labelFreqMgr)
 		// c- update wordTolabels (how many times each term appears /label)
 
 		for (Email email : emails) {
 			String lbl = null;
 			try {
 				lbl = email.getHeader("X-label")[0];
 			} catch (MessagingException e1) {
 				e1.printStackTrace();
 			}
 
 			LabelTermFrequencyManager mgr = labelFreqMgrMap.get(lbl);
 			if (mgr == null) {
 				mgr = new LabelTermFrequencyManager();
 				labelFreqMgrMap.put(lbl, mgr);
 			}
 
 			String[] wordsList = null;
 			try {
 				//TODO: adding the subject here improves accuracy, but now there is a SubjectFilter that we need to improve
 				wordsList = ((String) email.getContent()).split("\\s+");
 			} catch (MessagingException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			List<HashMap<String, Double>> grams = buildGrams(wordsList);
 
 			if (freqNormalization) {
 				for (int i = 0; i < grams.size(); i++) {
 					Iterator<Map.Entry<String, Double>> it = grams.get(i)
 							.entrySet().iterator();
 					while (it.hasNext()) {
 						Map.Entry<String, Double> entry = it.next();
 						// divides each frequency of unigrams by number of words
 						// and each bigrams by number of bigrams (number of
 						// words - 1) and so on
 						entry.setValue(entry.getValue()
 								/ (wordsList.length - i));
 					}
 				}
 			}
 
 			mgr.updateFrequencies(grams);
 
 			for (int i = 0; i < NGRAMS_MAX; ++i) {
 				Iterator<Map.Entry<String, Double>> it = grams.get(i)
 						.entrySet().iterator();
 				while (it.hasNext()) {
 					String word = it.next().getKey();
 					if (!wordToLabelsMap.containsKey(word))
 						wordToLabelsMap.put(word, new HashSet<String>());
 
 					wordToLabelsMap.get(word).add(lbl);
 				}
 			}
 		}
 
 		//passing arguments to the filter
 		ArrayList<Attribute> atts = extractAttributes();
 		String[] options = new String[5];
 		options[0] = ATT_NAME_PREFIX;
 		options[1] = NGRAMS_MAX + "";
 		options[2] = "";
 		options[3] = freqNormalization + "";
 		for (int i = 0; i < IGNORED_GRAMS.length; i++) {
 			if (i == IGNORED_GRAMS.length - 1)
 				options[2] += IGNORED_GRAMS[i];
 			else
 				options[2] += IGNORED_GRAMS[i] + ",";
 		}
 		options[4] = useBinaryAttributes + "";
 		
 		Filter wordFrequencyFilter = new WordFrequencyFilter(atts, options);
 
 		return wordFrequencyFilter;
 	}
 
 	/**
 	 * used to hold term frequency information for each label
 	 * @author Moustafa Mahmoud , Ahmed Kotb*
 	 */
 	private class LabelTermFrequencyManager {
 
 		public List<HashMap<String, Double>> gramsFreq;
 
 		public LabelTermFrequencyManager() {
 			// init grams frequencies list
 			gramsFreq = new ArrayList<HashMap<String, Double>>();
 			for (int i = 0; i < NGRAMS_MAX; i++)
 				gramsFreq.add(new HashMap<String, Double>());
 
 		}
 
 		/**
 		 * updates the grams frequencies by the given hashmaps
 		 * @param grams the new grams frequencies to update the current one with
 		 */
 		public void updateFrequencies(List<HashMap<String, Double>> grams) {
 			// merge the two hash maps for each ngram
 			for (int i = 0; i < NGRAMS_MAX; i++) {
 				HashMap<String, Double> labelGrams = gramsFreq.get(i);
 				Iterator<Map.Entry<String, Double>> itr = grams.get(i)
 						.entrySet().iterator();
 				
 				while (itr.hasNext()) {
 					Map.Entry<String, Double> pair = itr.next();
 
 					double newValue = pair.getValue();
 					if (labelGrams.containsKey(pair.getKey()))
 						newValue += labelGrams.get(pair.getKey());
 
 					labelGrams.put(pair.getKey(), newValue);
 				}
 			}
 
 		}
 
 		/**
 		 * extracts the important words from the whole words stored in this object
 		 * @param maxSize the size of the returning array
 		 * @return list of important words
 		 */
 		public String[] extractImportantWords(int maxSize) {
 			ArrayList<TfIdfScore> tfidf = new ArrayList<TfIdfScore>();
 
 			for (int i = 0; i < gramsFreq.size(); i++) { // for each gram size
 				Iterator<Map.Entry<String, Double>> itr = gramsFreq.get(i)
 						.entrySet().iterator();
 				while (itr.hasNext()) {
 					Map.Entry<String, Double> pair = itr.next();
 					double tf = pair.getValue();
 
 					double idf = Math.log10(((double) labelFreqMgrMap.size())
 							/ wordToLabelsMap.get(pair.getKey()).size());
 					tfidf.add(new TfIdfScore(pair.getKey(), tf * idf));
 				}
 			}
 
			if (tfidf.size() > maxSize)
				Collections.sort(tfidf);
 
 			//Heuristic: if a term has score < 10% of the highest score, then ignore it
 			double threshold = thresholdPercentage/100.0 * tfidf.get(0).score;
 			int sz = 0;
 			for (; sz < tfidf.size(); sz++)
 				if (tfidf.get(sz).score < threshold)
 					break;
 			String[] importantWords = new String[Math.min(maxSize, sz)];
 
 			for (int i = 0; i < importantWords.length; i++)
 				importantWords[i] = tfidf.get(i).word;
 			
 			return importantWords;
 		}
 	}
 
 	private class TfIdfScore implements Comparable<TfIdfScore> {
 		public String word;
 		public double score;
 		private double eps = 1e-10;
 
 		public TfIdfScore(String word, double score) {
 			this.word = word;
 			this.score = score;
 		}
 
 		@Override
 		public int compareTo(TfIdfScore s) {
 			if (score > s.score + eps)
 				return -1;
 			else if (score + eps < s.score)
 				return 1;
 			return 0;
 		}
 	}
 }
