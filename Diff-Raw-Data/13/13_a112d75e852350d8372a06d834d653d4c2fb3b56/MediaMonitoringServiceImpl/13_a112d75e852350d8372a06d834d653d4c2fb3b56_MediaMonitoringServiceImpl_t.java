 package webservice.api.impl;
 
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.PriorityQueue;
 import java.util.TreeMap;
 
 import javax.annotation.PostConstruct;
 import javax.ws.rs.core.Response;
 
 import org.aap.monitoring.Article;
 import org.aap.monitoring.ArticleCount;
 import org.aap.monitoring.SQLManager;
 import org.aap.monitoring.SolrManager;
 import org.aap.monitoring.WordCloud;
 import org.apache.log4j.Logger;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.springframework.stereotype.Service;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import webservice.api.MediaMonitoringService;
 
 
 @Service( "media#default" )
 public class MediaMonitoringServiceImpl
     implements MediaMonitoringService
 {
 	private static Logger LOG = Logger.getLogger(MediaMonitoringServiceImpl.class);
 	private SolrManager solrManager;
 	private SQLManager sqlManager;
 	List<String> candidateList;
 	private int TOP_N = 20;
 	
 	public MediaMonitoringServiceImpl(){
 		solrManager = new SolrManager();
 		try {
 			sqlManager = new SQLManager(solrManager);
 			getCandidateList();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public Collection<Article> getArticles(String keyword, long startDate, long endDate, String src,  int start, int count) {
 		keyword = toPhrase(keyword);
 		try {
 			List<Article> articles;
 			if(startDate == 0 && endDate == 0){
 				articles =  solrManager.getArticlesForKeywords(keyword, src, start, count);
 			}
 			if(endDate == 0 ){
 				endDate = new Date().getTime();
 			}
 			articles =  solrManager.getArticlesForKeywords(keyword, new Date(startDate), new Date(endDate), src, start, count);
 			for(Article article: articles){
 				String content = article.getContent();
				if(content.length() > 0){
					article.setContent(content.substring(0, Math.min(content.length()-1, 300)) + "...");
				}
 			}
 			return articles;
 		} catch (SolrServerException e) {
 			LOG.info("Failed to get articles",e);
 		}
 		return null;
 	}
 	
 	
 	@Override
 	public ArticleCount getNumArticles(String keyword, long startDate, long endDate, String src,  int start, int count){
 		keyword = toPhrase(keyword);
 		if(endDate==0){
 			endDate = new Date().getTime();
 		}
 		try {
 			return solrManager.getNumArticlesForKeywordsAndDate(keyword, new Date(startDate), new Date(endDate), src, start, count);
 		} catch (SolrServerException e) {
 			LOG.info("Failed to get articles count",e);
 		}
 		return null;
 	}
 
 
 	@Override
 	public Response triggerIndexer(String date) {
 		try {
 			sqlManager.triggerIndexer(date);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public Collection<Article> getArticlesFromSolr(String solrQuery, int start, int count) {
 		try {
 			return solrManager.getArticlesForSolrQuery(solrQuery,start,count);
 		} catch (SolrServerException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public Map<String, Integer> getWordCloud(String query, String src, long startDate,  long endDate,int count) {
 		query = toPhrase(query);
 		WordCloud wc = new WordCloud(solrManager);
 		Map<String, Integer> wordCount = wc.getWordCloud(query, null, null, src,count);
 		return filterMap(wordCount, TOP_N);
 	}
 	
 	private Map<String,Integer> filterMap(Map<String,Integer> map, int topN){			
 		ArrayList<Entry<String,Integer>> newArrayList = Lists.newArrayList(map.entrySet());
 		Collections.sort(newArrayList, new Comparator<Entry<String, Integer>>() {
 
 			@Override
 			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
 				// TODO Auto-generated method stub
 				return (o2.getValue().compareTo(o1.getValue()));
 			}
 		});
 				
 		Map<String, Integer> newMap = Maps.newHashMap();
 		for (Entry<String, Integer> e : newArrayList.subList(0, Math.min(newArrayList.size(), topN))) {			
 			newMap.put(e.getKey(), e.getValue());
 		}
 		return newMap;
 				
 //		ValueComparator vc = new ValueComparator(map);
 //		Map<String,Integer> sortedMap  = new TreeMap<String, Integer>(vc);
 //		sortedMap.putAll(map);
 //		System.out.println("Before printing >." + sortedMap.size());
 //		if(sortedMap.size() > topN){
 //			int count = 0;
 //			Map<String,Integer> prunedMap = new HashMap<String, Integer>();
 //			System.out.println("key set size: " + map.keySet().size());
 //			for(String key: sortedMap.keySet()){
 //				System.out.println(" key checking : " + key);
 //				if(count<=topN && sortedMap.get(key) != null){
 //					System.out.println("map vlaue : " + sortedMap.get(key));
 //					prunedMap.put(key, sortedMap.get(key));
 //					count++;
 //				}
 //			}
 //			return prunedMap;
 //		}else{
 //			return sortedMap;
 //		}
 	}
 	
 	class ValueComparator implements Comparator<String> {
 
 	    Map<String, Integer> base;
 	    public ValueComparator(Map<String, Integer> base) {
 	        this.base = base;
 	    }
 
 	    // Note: this comparator imposes orderings that are inconsistent with equals.    
 	    public int compare(String a, String b) {
 	        if (base.get(a) >= base.get(b)) {
 	            return -1;
 	        } else {
 	            return 1;
 	        } 
 	    }
 	}
 	
 	private String toPhrase(String keyword){
 		if(candidateList.contains(keyword.toLowerCase())){
 			return "\"" + keyword + "\"";
 		}
 		return keyword;
 	}
 	
 	private void getCandidateList(){
 		candidateList = new ArrayList<String>();
 		try{
 			InputStream is = this.getClass().getResourceAsStream("candidateList");
 			String line;
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			while((line=br.readLine()) != null){
 				candidateList.add(line.toLowerCase());
 			}
 		}catch(Exception e){
 			LOG.error("Failed to load candidate list", e);
 		}
 	}
 }
