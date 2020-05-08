 package ethz.nlp.headgen.prob;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Map;
 import java.util.TreeMap;
 
 import ethz.nlp.headgen.Doc;
 
 public class NgramLightFilter extends NgramSimple implements NGramProbs {
 
 	private int filterType = 1;
 	
 	public NgramLightFilter(TreeMap<ArrayList<String>, Double> inNgrams) {
 		super(inNgrams);
 		// TODO Auto-generated constructor stub
 	}
 
 	public NgramLightFilter(TreeMap<ArrayList<String>, Double> inNgrams, int N) {
 		super(inNgrams, N);
 		// TODO Auto-generated constructor stub
	}
 	/**
 	 * 
 	 * @param inNgrams
 	 * @param N
 	 * @param filterCode // 1 = Doc contains one of the ngram words, 2 = doc contains all of the ngram words but not that order
 	 */
 	public NgramLightFilter(TreeMap<ArrayList<String>, Double> inNgrams, int N, int filterCode) {
 		super(inNgrams, N);
 		if(filterCode<3&&filterCode>0){
 			filterType=filterCode;
 		}
 		
 	}
 	
 	
 	public TreeMap<ArrayList<String>,Double> filterNgrams(Doc doc){
 
 		return filterNgrams(doc,(Comparator<ArrayList<String>>)ngramFreq.comparator());
 	}
	// Smallchange
 	public TreeMap<ArrayList<String>,Double> filterNgrams(Doc doc, Comparator<ArrayList<String>> comp){  //TODO paramaterize Comparator in this method
 		
 		
 		
 		TreeMap<ArrayList<String>,Double> outNgram = new TreeMap<ArrayList<String>,Double>(comp);		
 		for(Map.Entry<ArrayList<String>, Double> ele : this.ngramFreq.entrySet()){
 			if(filterType==1){
 				if(doc.containsOneOrMore(ele.getKey())){
 					outNgram.put(ele.getKey(), ele.getValue());
 				}
 			}
 			else{
 				if(doc.containsAll(ele.getKey())){
 					outNgram.put(ele.getKey(), ele.getValue());
 				}
 			}
 		}
 		
 		
 		//TODO Append all the ngrams from the original document into the outNgram. Add frequencies if an ngram alreayd excists. Weigh down the ngrams from the query doc because its frequencies are not weighted by that probability of topic to doc which all the ngrams from our corpus are weighted with 
 		/*
 		 *  JaredsFunctionTHatFUndsNgramsPerDoc
 		 * 
 		 * 
 		 * 
 		 * 
 		 */
 		return outNgram;
 	}
 
 }
