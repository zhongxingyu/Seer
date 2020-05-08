 package edu.nyu.trendingtopics.storm.util;
 
 import java.io.FileReader;
 import java.util.HashSet;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.apache.lucene.analysis.core.StopAnalyzer;
 import org.apache.lucene.analysis.util.CharArraySet;
 import org.apache.lucene.util.Version;
 
 
 /**
  * This class holds the list of stopwords. It populates the words from the file or 
  * it uses the stopwords set defined by lucene.
  * 
  * @author samitpatel
  * */
 public class StopWords {
 
 	private static CharArraySet stopWordsCharArraySet = null;
 	private static String stopWordsFile = "files/stopwords";
	private static boolean useLuceneStopWords = true;
 	private static Lock lock = new ReentrantLock();
 
 	/**
 	 * Checks whether the word is a stopword. This method is thread safe.
 	 * 
 	 * @param word Word to be checked.
 	 * 
 	 *@return Returns <code>True</code> if word is stopword, otherwise <code>False</code>.
 	 * */
 	public static boolean isStopWord(String word){
 		try{
 			if(stopWordsCharArraySet == null)
 				populateStopWords();
 
 			lock.tryLock();
 			return stopWordsCharArraySet.contains(word);
 			
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			lock.unlock();
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the set containing all the stopwords.
 	 * 
 	 * @return Set of stopwords.
 	 * */
	public static synchronized CharArraySet getStopWordSet(){
 		if(stopWordsCharArraySet == null)
 			populateStopWords();
 
 		return stopWordsCharArraySet;
 	}
 
 
 	/**
 	 * Private method that lazily populates the stopwords from the file. 
 	 * This method is Thread-safe
 	 * */
 	private static synchronized void populateStopWords(){
 
		if(useLuceneStopWords){
			stopWordsCharArraySet = StopAnalyzer.ENGLISH_STOP_WORDS_SET;
			return;
		}

 		Set<String> stopWords = new HashSet<String>();
 
 		Scanner scanner = null;
 		FileReader fileReader = null;
 		try{
 			fileReader = new FileReader(stopWordsFile);
 			scanner = new Scanner(fileReader);
 
 			//populates the list of stopwords from the stopwords file.
 			//ignores te empty line or the one that starts with '#'
 			while(scanner.hasNextLine()){
 				String word = scanner.nextLine();
 				//ignores the comments in the stopwords file that start with '#'.
 				if(word.isEmpty() || word.startsWith("#")) continue;
 				String[] words = word.trim().split("\\s+");
 				for(String w : words)
 					stopWords.add(w);
 			}
 
 		}catch(Exception e){
 			e.printStackTrace();
 		}finally{
 			if(scanner != null) scanner.close();
 			try{
 				if(fileReader != null) fileReader.close();
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 
 		stopWordsCharArraySet = new CharArraySet(Version.LUCENE_42, stopWords, true);
 	}
 }
