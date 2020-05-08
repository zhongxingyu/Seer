 package edu.columbia.watson.twitter;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Writable;
 import org.apache.mahout.common.Pair;
 import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterator;
 
 import edu.columbia.watson.twitter.util.GlobalProperty;
 
 /**
  * Dictionary cache used when creating the vector of a query
  * @author qiaoyu
  */
 
 public class DictionaryCache {
 	private static DictionaryCache instance = null;
 	private Map<String,Integer> dictionary = new HashMap<String,Integer>();
 
 	public static DictionaryCache getInstance() 
 	{
 		if (instance == null)
 			instance = new DictionaryCache();
 		return instance;
 	}
 
 	public int getWordID(String word)
 	{
 		return dictionary.get(word);
 	}
 	
	public int getDicSize()
	{
		return dictionary.size();
	}
	
 	private DictionaryCache() 
 	{
 		loadDictionaryToMap(GlobalProperty.getInstance().getDicPath());
 	}
 	
	
 	private void loadDictionaryToMap(String fileName)
 	{
 		Configuration conf = new Configuration();
 		Path input = new Path(fileName);
 
 		SequenceFileIterator<Writable, IntWritable> iterator;
 		try {
 			iterator = new SequenceFileIterator<Writable, IntWritable>(input, true, conf);
 			while (iterator.hasNext()) {
 				Pair<Writable,IntWritable> record = iterator.next();
 				String key = record.getFirst().toString();
 				Integer id = record.getSecond().get();
 				dictionary.put(key,id);
 			}
 			System.out.println("Successfully loaded dictionary map, size = " + dictionary.size());
 		} catch (IOException e) {
 			System.err.println("Error loading sequence file: " + fileName);
 			e.printStackTrace();
 		}
 	}
 	
 	public static void main(String args[]) throws IOException
 	{
 		System.out.println(DictionaryCache.getInstance().getWordID("hello"));
 	}
 		
 }
