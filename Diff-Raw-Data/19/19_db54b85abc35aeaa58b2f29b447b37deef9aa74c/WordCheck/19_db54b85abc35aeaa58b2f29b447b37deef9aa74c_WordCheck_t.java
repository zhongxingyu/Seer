 /**
  * 
  */
 package spellcheck;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import corpus.TrainedWords;
 import wordnet.Dictionary;
 
 /**
  * @author abilng
  *
  */
 public class WordCheck {
 
 	Dictionary dictionary;
 	ConfusionMatrix cMatrix;
 	TrainedWords trainedData;
 	
 	final static int MAX_EDIT = 3;
 	final static int NO_OF_SUGGESTION = 5;
 
 	public WordCheck(Dictionary dictionary, TrainedWords trainedData) {
 		this.dictionary = dictionary;
 		this.cMatrix = new ConfusionMatrix();
 		this.trainedData = trainedData;
 	}
 
 	private Set<String> edits(final String word,
 			final List<String> allWords) {
 
 		//TODO parallel each For
 
 		final Set<String> validWords = Collections.synchronizedSet(new HashSet<String>());
 
 		Thread del,rev,ins,sub;
 
 		del = (new Thread() {
 			public void run() {
 				del(word, validWords,allWords);
 			}
 		});
 		rev = new Thread() {
 			public void run() {
 				rev(word, validWords,allWords);
 			}
 		};
 		ins = new Thread() {
 			public void run() {
 				ins(word, validWords,allWords);
 			}
 		};
 		sub = new Thread() {
 			public void run() {
 				sub(word, validWords,allWords);
 			}
 		};
 
 		del.start();
 		sub.start();
 		ins.start();
 		rev.start();
 
 		try {
 			sub.join();
 			del.join();
 			ins.join();
 			rev.join();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return validWords;
 	}
 
 	private void sub(final String word, final Set<String> validWords,
 			List<String> allWords) {
 		for(int i=0; i < word.length(); ++i) {
 			for(char c='a'; c <= 'z'; ++c) {
 				if(cMatrix.sub(word.charAt(i),c)>0) {
 					String newstr = word.substring(0, i) + String.valueOf(c) +
 							word.substring(i+1);
 					allWords.add(newstr);
 					isValidWord(validWords,newstr);
 				}
 			}
 		}
 	}
 
 	private void ins(final String word, final Set<String> validWords,
 			List<String> allWords) {
 		for(int i=0; i < word.length(); ++i) {
 			for(char c='a'; c <= 'z'; ++c) {
 				if((i==0 && cMatrix.add('@',c)>0)||
 						(i!=0&&cMatrix.add(word.charAt(i-1),c)>0)) {
 					String newstr = word.substring(0, i) + String.valueOf(c) + word.substring(i);
 					allWords.add(newstr);
 					isValidWord(validWords,newstr);
 				}
 			}
 		}
 	}
 
 	private void rev(final String word, final Set<String> validWords,
 			List<String> allWords) {
 		for(int i=0; i < word.length()-1; ++i){
 			if(cMatrix.rev(word.charAt(i),word.charAt(i+1))>0) {
 				String newstr = word.substring(0, i) + word.substring(i+1, i+2) +
 						word.substring(i, i+1) + word.substring(i+2);
 				isValidWord(validWords,newstr);
 				allWords.add(newstr);
 			}
 		}
 	}
 
 	private void del(final String word, final Set<String> validWords,
 			List<String> allWords) {
 
 		for(int i=0; i < word.length(); ++i){//delete i -th element
 			if((i==0 && cMatrix.del('@',word.charAt(i))>0)||
 					(i!=0 && cMatrix.del(word.charAt(i-1),word.charAt(i))>0)){   
 				String newstr = word.substring(0, i) + word.substring(i+1);
 				isValidWord(validWords,newstr);
 				allWords.add(newstr);
 			}
 		}
 	}
 	private void isValidWord(final Set<String> wordarray,final String word) {
//		if(dictionary.hasWord(word)){
//			wordarray.add(word);
//		}
		if(trainedData.count(word)>0){
 			wordarray.add(word);
 		}
 	}
 
 	public Map<String,Integer> getCorrect(final String word){
 
 		int count;
 		int editDistance = 1;
 		
 		List<String> allWords = Collections.synchronizedList(new ArrayList<String>());
 		Map<String, Integer> correct = new TreeMap<String, Integer>();
 		Set<String> validWords;
 		
 		
 		validWords = edits(word,allWords);
 		editDistance++;
 		
 		while(validWords.isEmpty() && editDistance <=MAX_EDIT){
 			editDistance++;
 			System.err.println(word+ " "+ allWords.size() + " " + editDistance);
 			Iterator<String> iterator = allWords.iterator();
 			List<String> newAllWords =
 					Collections.synchronizedList(new ArrayList<String>());
 			while(iterator.hasNext()) {
 				String str = iterator.next();
 				iterator.remove();
 				validWords.addAll(edits(str, newAllWords));
 			}
 			allWords = newAllWords;
 		}
 
 		for (String str : validWords) {
 			count = trainedData.count(str);
 			correct.put(str, count);
 		}
 		correct = sortByValue(correct);
 		return correct;
 	}
 	
 	static Map<String,Integer> sortByValue(Map<String, Integer> map) {
 	     LinkedList<Entry<String,Integer>> list =
 	    		 new LinkedList<Entry<String,Integer>>(map.entrySet());
 	     Collections.sort(list, new Comparator<Entry<String,Integer>>() {
 			@Override
 			public int compare(Entry<String,Integer> arg0,
 					Entry<String,Integer> arg1) {
 				// reverse Order
 				return Integer.compare(arg1.getValue(), arg0.getValue());
 			}
 	     });
 
 	    Map<String, Integer> result = new LinkedHashMap<String,Integer>();
 	    int cResult = 0;
 	    for (Iterator<?> it = list.iterator(); it.hasNext();) {
 	        @SuppressWarnings("unchecked")
 			Entry<String,Integer> entry = 
 	        		(Entry<String, Integer>) it.next();
 	        result.put(entry.getKey(), entry.getValue());
 	        if ( ++cResult > NO_OF_SUGGESTION ) break;
 	    }
 	    return result;
 	} 
 }
