 /*
  * This class contains utils of Search
  */
 package com.kittypad.ringtone;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.Token;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
 
 import com.google.appengine.api.datastore.DatastoreNeedIndexException;
 import com.google.appengine.api.datastore.DatastoreTimeoutException;
 import com.kittypad.ringtone.utility.SharedUtils;
 
 public class SearchUtils {
 	private static final Logger log = Logger.getLogger(SearchUtils.class.getName());
 	
 	/** From StopAnalyzer Lucene 2.9.1 */
 	public final static String[] stopWords = new String[]{
 	  	    "a", "an", "and", "are", "as", "at", "be", "but", "by",
 		    "for", "if", "in", "into", "is", "it",
 		    "no", "not", "of", "on", "or", "such",
 		    "that", "the", "their", "then", "there", "these",
 		    "they", "this", "to", "was", "will", "with"
 		  };
 	public static final int MAX_NUMBER_OF_WORDS_TO_PUT_IN_INDEX = 200;
 	public static final int MAXIMUM_NUMBER_OF_WORDS_TO_SEARCH = 10;
 	public static final int RESULTS_PER_PAGE = 10;
 
 	/*set full text search stuff for musicItem*/
 	public static void updateFTSStuffForMusicItem(MusicItem musicItem) {
 		StringBuffer sb = new StringBuffer();
 		sb.append(musicItem.getMusicName());
 		
 		Set<String> new_ftsTokens = getTokensForIndexingOrQuery(
 				sb.toString(),
 				MAX_NUMBER_OF_WORDS_TO_PUT_IN_INDEX);
 		Set<String> ftsTokens = musicItem.getFts();
 		ftsTokens.clear();
 		for(String token : new_ftsTokens){
 			ftsTokens.add(token);
 		}	
 	}
 	
 	/*get tokens from index_raw string for creating index or for query*/
 	private static Set<String> getTokensForIndexingOrQuery(String index_raw, int tokenNumber) {
 		String indexCleanedOfHTMLTags = index_raw.replaceAll("\\<.*?>", " ");
 		
 		Set<String> returnSet = new HashSet<String>();
 		try{
 			Analyzer analyzer = new SnowballAnalyzer(
 					org.apache.lucene.util.Version.LUCENE_CURRENT,
 					"English",
 					stopWords);
 			TokenStream tokenStream = analyzer.tokenStream(
 					"content", 
 					new StringReader(indexCleanedOfHTMLTags));
 			Token token = new Token();
 			
 			while(((token = tokenStream.next()) != null)
 					&& (returnSet.size() < tokenNumber)){
 				returnSet.add(token.term());
 			}
 		}catch(IOException e){
 			log.severe(e.getMessage());
 		}
 		return returnSet;
 	}
 	
 	public static List<MusicItem> searchMusicItems(
 			String queryString,
 			PersistenceManager pm,
 			int start){
 		StringBuffer queryBuffer = new StringBuffer();
 		
 		queryBuffer.append("SELECT FROM " + MusicItem.class.getName() + " WHERE ");
 		
 		Set<String> queryTokens = getTokensForIndexingOrQuery(
 				queryString,
 				MAXIMUM_NUMBER_OF_WORDS_TO_SEARCH);
 		List<String> parametersForSearch = new ArrayList<String>(queryTokens);
 		
 		StringBuffer declareParametersBuffer = new StringBuffer();
 		int parameterCounter = 0;
 		while(parameterCounter < queryTokens.size()){
 			queryBuffer.append("fts == param" + parameterCounter);
 			declareParametersBuffer.append("String param" + parameterCounter);
 			if(parameterCounter + 1 < queryTokens.size()){
 				queryBuffer.append(" && ");
 				declareParametersBuffer.append(", ");
 			}
 			parameterCounter++;
 		}
 		
 		Query query = pm.newQuery(queryBuffer.toString());
 		query.setRange(start*RESULTS_PER_PAGE, (start+1)*RESULTS_PER_PAGE);
 		query.declareParameters(declareParametersBuffer.toString());
 		
 		List<MusicItem> result = null;
 		try{
 			result = (List<MusicItem>) query.executeWithArray(parametersForSearch.toArray());
 		}catch (DatastoreTimeoutException e){
 			log.severe(e.getMessage());
 			log.severe("datastore timeout at: " + queryString);
 		}catch(DatastoreNeedIndexException e) {
 			log.severe(e.getMessage());
 			log.severe("datastore need index exception at: " + queryString);
 		}
 		return result;
 		
 	}
 	
 	public static List<MusicItem> getResultsByKeyword(String key, int start) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		List<MusicItem> searchResults = searchMusicItems(key, pm, start);
 		if (searchResults != null) {
 			return searchResults;
 		} else {
 			return new ArrayList<MusicItem>();
 		}
 	}
 
 	public static List<MusicItem> getResultsByCategory(String category, int start) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query query = pm.newQuery(MusicItem.class);
 		query.setFilter("category == lastParam");
 		query.declareParameters("String lastParam");
 		query.setRange(start*RESULTS_PER_PAGE, (start+1)*RESULTS_PER_PAGE);
 		
 		List<MusicItem> searchResults = null;
 		try{
 			searchResults = (List<MusicItem>) query.execute(category);
 		}finally{
 			query.closeAll();
 		}
 		return searchResults;
 	}
 
 	public static List<MusicItem> getResultsByDownloadCount(int start) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query query = pm.newQuery(MusicItem.class);
 		query.setOrdering("download_count desc");
 		query.setRange(start*RESULTS_PER_PAGE, (start+1)*RESULTS_PER_PAGE);
 		List<MusicItem> searchResult = null;
 		try{
 			searchResult = (List<MusicItem>) query.execute();
 		}finally{
 			query.closeAll();
 		}
 		return searchResult;
 	}
 
 	public static List<MusicItem> getResultsByDate(int start) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query query = pm.newQuery(MusicItem.class);
 		query.setOrdering("add_date desc");
 		query.setRange(start*RESULTS_PER_PAGE, (start+1)*RESULTS_PER_PAGE);
 		List<MusicItem> searchResult = null;
 		try{
 			searchResult = (List<MusicItem>) query.execute();
 		}finally{
 			query.closeAll();
 		}
 		return searchResult;
 	}
 	
 	public static List<MusicItem> getResultsByRandom() {
 		int random = (int)(Math.random()*(SharedUtils.getTotalRingCount()-10));
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query query = pm.newQuery(MusicItem.class);
 		query.setOrdering("add_date desc");
		query.setRange(random, random+10);
 		List<MusicItem> searchResult = null;
 		try{
 			searchResult = (List<MusicItem>) query.execute();
 		}finally{
 			query.closeAll();
 		}
 		return searchResult;
 	}
 
 	public static List<MusicItem> getResultsByArtist(String artist, int start) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query query = pm.newQuery(MusicItem.class);
 		query.setFilter("artist == lastParam");
 		query.declareParameters("String lastParam");
 		query.setRange(start*RESULTS_PER_PAGE, (start+1)*RESULTS_PER_PAGE);
 		
 		List<MusicItem> searchResults = null;
 		try{
 			searchResults = (List<MusicItem>) query.execute(artist);
 		}finally{
 			query.closeAll();
 		}
 		return searchResults;
 	}
 }
