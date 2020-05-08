 package edu.columbia.watson.twitter;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.mahout.math.RandomAccessSparseVector;
 import org.apache.mahout.math.Vector;
 
 /**
  * Convert a query into vector 
  * TODO: Change to built-in tokenizer
  * @author qiaoyu
  */
 
 public class QueryVectorization {
 
 	public static Vector getSparseVectorFromString(String query){
 		/**
 		 * a very ugly way now - split by space
 		 */
 		String [] splitted = query.split(" ");
		Map<Integer,Integer> termFrequencyCount = new HashMap<Integer,Integer>();	//wordID -> frequency
 		for (String term : splitted){
 			int wordID = DictionaryCache.getInstance().getWordID(term);
 			if (termFrequencyCount.containsKey(wordID))
 				termFrequencyCount.put(wordID, termFrequencyCount.get(wordID) + 1);
 			else
 				termFrequencyCount.put(wordID, 1);
 		}	
 		
		Vector result = new RandomAccessSparseVector(DictionaryCache.getInstance().getDicSize());
 		
 		for (Map.Entry<Integer, Integer> entry : termFrequencyCount.entrySet())
 			result.set(entry.getKey(), entry.getValue());
 		
 		return result;
 	}
 	
 	
 	public static void main(String args[]) throws IOException
 	{
		QueryVectorization.getSparseVectorFromString("hello world");
 //		QueryVectorization zizi = new QueryVectorization();
 	//	zizi.loadDictionaryToMap("/mnt/corpus/dict/dictionary.file-0");
 	}
 
 }
