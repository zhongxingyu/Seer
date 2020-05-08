 /**
  * 
  */
 package spellcheck;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import corpus.TainedData;
 import wordnet.Dictionary;
 
 /**
  * @author abilng
  *
  */
 public class WordCheck {
 
 	Dictionary dictionary;
 	ConfusionMatrix cMatrix;
 	TainedData trainedData;
 	
 	public WordCheck(Dictionary dictionary) {
 		this.dictionary = dictionary;
 		this.cMatrix = new ConfusionMatrix();
 		this.trainedData = new TainedData();
 	}
 
 	private List<String> edits(final String word) {
 		
 		//TODO parallel each For
 		
 		final List<String> wordarray = Collections.synchronizedList(new ArrayList<String>());
 
 		Thread del,rev,ins,sub;
 		
 		del = (new Thread() {
 			public void run() {
 				del(word, wordarray,2);
 			}
 		});
 		rev = new Thread() {
 			public void run() {
 				rev(word, wordarray,2);
 			}
 		};
 		ins = new Thread() {
 			public void run() {
 				ins(word, wordarray,2);
 			}
 		};
 		sub = new Thread() {
 			public void run() {
 				sub(word, wordarray,2);
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
 		
 		return wordarray;
 	}
 
 	private void sub(final String word, final List<String> wordarray,int edits) {
 		if(edits <= 0) return;
 		for(int i=0; i < word.length(); ++i) {
 			for(char c='a'; c <= 'z'; ++c) {
 				if(cMatrix.sub(word.charAt(i),c)>0) {
 					String newstr = word.substring(0, i) + String.valueOf(c) +
 							word.substring(i+1);
 					isValidWord(wordarray,newstr);
 				}
 			}
 		}
 	}
 
 	private void ins(final String word, final List<String> wordarray,int edits) {
 		if(edits <= 0) return;
 		for(int i=0; i < word.length(); ++i) {
 			for(char c='a'; c <= 'z'; ++c) {
 				if((i==0 && cMatrix.add('@',c)>0)||
 						(i!=0&&cMatrix.add(word.charAt(i-1),c)>0)) {
 					String newstr = word.substring(0, i) + String.valueOf(c) + word.substring(i);
 					//System.err.println(newstr);
 					isValidWord(wordarray,newstr);
					//ins(newstr, wordarray, edits-1);
 				}
 			}
 		}
 	}
 
 	private void rev(final String word, final List<String> wordarray,int edits) {
 		if(edits <= 0) return;
 		for(int i=0; i < word.length()-1; ++i){
 			if(cMatrix.rev(word.charAt(i),word.charAt(i+1))>0) {
 				String newstr = word.substring(0, i) + word.substring(i+1, i+2) +
 						word.substring(i, i+1) + word.substring(i+2);
 				isValidWord(wordarray,newstr);
 			}
 		}
 	}
 
 	private void del(final String word, final List<String> wordarray,int edits) {
 		if(edits <= 0) return;
 		for(int i=0; i < word.length(); ++i){//delete i -th element
 			if((i==0 && cMatrix.del('@',word.charAt(i))>0)||
 					(i!=0 && cMatrix.del(word.charAt(i-1),word.charAt(i))>0)){   
 				String newstr = word.substring(0, i) + word.substring(i+1);
 				isValidWord(wordarray,newstr);	
 			}
 		}
 	}
 	private void isValidWord(final List<String> words,final String word) {
 		if(dictionary.hasWord(word)){
 			words.add(word);
 		}
 	}
 
 	public Map<String,Integer> getCorrect(final String word){
 	     List<String> words = edits(word);
 	     Map<String, Integer> correct = new HashMap<String, Integer>();
 	     int count;
 	     for (String str : words) {
 			count = trainedData.count(str);
 			correct.put(str, count);
 		}
 	     return correct;
 	}
 }
