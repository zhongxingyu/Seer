 package filters;
 
 import java.sql.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import weka.core.Attribute;
 import general.Email;
 
 public class WordFrequencyFilter extends Filter{
 
 	private static final long serialVersionUID = 1148119665295273L;
 
 	private String attPrefix;
 	private HashMap<String, Integer> indexMap;
 	
 	private int NGRAMS_MAX = 0;
 	private int[] IGNORED_GRAMS;
 	
 	/**
 	 * Constructor
 	 * @param atts List of attributes, one for each important word
 	 * @param options Only one option representing the attribute name prefix, which is appended before the word
 	 */
 	public WordFrequencyFilter(ArrayList<Attribute> atts, String[] options) {
 		super(atts, options);
 		attPrefix = options[0];
 		try {
 			NGRAMS_MAX = Integer.parseInt(options[1]);
 			String[] ignored = options[2].split(",");
 			IGNORED_GRAMS = new int[ignored.length];
 			for (int i = 0; i < ignored.length; i++){
 				if (ignored[i] != "")
 					IGNORED_GRAMS[i] = Integer.parseInt(ignored[i]);
 			}
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println("INVALID NGrams parameters value");
 			System.exit(1);
 		}
 		
 		System.out.println("GRAMS = " + NGRAMS_MAX);
 		System.out.println("IGNORED_GRAMS = " + Arrays.toString(IGNORED_GRAMS));
 		Iterator<Attribute> itr = attributes.iterator();
 		indexMap = new HashMap<String, Integer>();
 		int index=0;
 
 		while(itr.hasNext()) indexMap.put(getWord(itr.next().name()), index++);
 	}
 
 	/**
 	 * extracts the word string from the attribute name, because the name of the
 	 * attribute is on the form "prefix_word" --> "wff_word"
 	 * @param attName Attribute name
 	 */
 	private String getWord(String attName){
 		//TODO : change this function according to the convention in which will name the wordFrequency attributes
 		return attName.substring(attPrefix.length());
 	}
 	
 	private List<HashMap<String, Double>> buildGrams(String[] wordsList){
 		ArrayList<HashMap<String,Double>> gramsList = new ArrayList<HashMap<String,Double>>();
 		for (int i = 1; i <= NGRAMS_MAX; ++i) {
 			HashMap<String,Double> grams = new HashMap<String, Double>();
 			//if this Ngrams are not ignored
 			if (Arrays.binarySearch(IGNORED_GRAMS, i) < 0 ){
 				for (int j = 0; j < wordsList.length - i + 1; j++) {
 					String gram = "";
 					for (int j2 = j; j2 < j+i; j2++)
 						gram += wordsList[j2] + " ";
 					
 					gram = gram.trim();
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
 	
 	private void calcFrequencies(double[] vals, HashMap<String, Integer> indexMap, Email email){
 		String splitRegex = "\\s+";
 		String[] wordsList = (email.getSubject() + " " + email.getContent()).split(splitRegex);
 		List<HashMap<String, Double>> grams = buildGrams(wordsList);
 		
 		//iterate on each attribute and get its count
 		Iterator<String> it = indexMap.keySet().iterator();
 		while (it.hasNext()){
 			String key = it.next();
 			for (int i = 0; i < NGRAMS_MAX; i++) {
 				if (grams.get(i).containsKey(key)){
					vals[indexMap.get(key)] += grams.get(i).get(key);
 					//each key will be found on one gram map only
 					break;
 				}
 			}
 		}
 	}
 	
 	@Override
 	public double[] getAttValue(Email email){
 		double[] vals = new double[attributes.size()];
 		calcFrequencies(vals, indexMap, email);
 		return vals;
 	}
 }
