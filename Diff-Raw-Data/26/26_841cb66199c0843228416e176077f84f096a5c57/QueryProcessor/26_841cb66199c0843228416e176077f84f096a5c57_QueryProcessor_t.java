 package domain;
 
 
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 
 import application.CrawlDriver;
 
 import service.helpers.BenchmarkRow;
 import service.Property;
 import service.Utils;
 
 import domain.cluster.Cluster;
 import domain.cluster.KMeansSingleton;
 import domain.cluster.SearchCluster;
 import domain.collection.ClusteredRankedCollection;
 import domain.collection.document.RankedDocument;
 import domain.index.IndexerThread;
 import domain.index.spimi.DefaultInvertedIndex;
 import domain.index.IndexSingleton;
 
 
 public class QueryProcessor {
 
 	private static DefaultInvertedIndex index = IndexSingleton.getInstance();
 
 	private static BenchmarkRow matchingTime;
 	private static BenchmarkRow pullingArticletime = new BenchmarkRow(null);
 	private static IndexerThread queryCompressor = new IndexerThread("Query Compressor");
 	
 
 	public static long getMatchingTime() {
 		return matchingTime.getDuration();
 	}
 
 	public static long getPullingTime() {
 		return pullingArticletime.getDuration();
 	}
 
 	public static LinkedList<RankedDocument> performQuery(String query){
 		System.out.println(compressQuery(query));
 		SearchCluster performSearch = new SearchCluster();
 		Cluster resultCluster = null;
 		TreeSet<Cluster> clusters = new TreeSet<Cluster>(KMeansSingleton.getInstance().getClusters());
 		HashMap<String, Integer> terms = tokenize(query);
 		ClusteredRankedCollection collection = (ClusteredRankedCollection)KMeansSingleton.getInstance().getCollection();
 		try {
 			resultCluster = performSearch.search(clusters, terms, collection);
 			 return resultCluster.getDocuments();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 
 	}
 
 
 
 	/**
 	 * Takes a user input query and compress it the way the corpus is compressed at indexation time
 	 * @param query
 	 * @return
 	 */
 	private static String compressQuery(String query) {
 		query = query.trim();
 
 		query = query.replace("( ", "(");
 		query = query.replace(" )", ")");
 		query = query.replace(" NOT", " not");
 		query = query.replace("NOT ", "not ");
 		query = query.replace(" AND", " and");
 		query = query.replace("AND ", "and ");
 		query = query.replace(" OR", " or");
 		query = query.replace("OR ", "or ");
 		query = query.replace(" and not ", "^-");
 		query = query.replace(" and not", "^-");
 		query = query.replace(" or not ", "+-");
 		query = query.replace(" or not", "+-");
 		query = query.replace(" and ", "^");
 		query = query.replace(" and", "^");
 		query = query.replace("(not", "(-");
 
 		query = query.replace(" or", "+");
 		query = query.replace("^ ", "^");
 		query = query.replace("- ", "-");
 		query = query.replace("+ ", "+");
 		query = query.replace(" not ", "^-");
 		query = query.replace(" not", "^-");
 		query = query.replace("not ", "-");
 
 		if (Property.getBoolean("DefaultToOr") == true)
 			//Setting the default boolean operator between words to OR
 			query = query.replace(" ", "+");
 		else
 			//Setting the default boolean operator between words to AND
 			query = query.replace(" ", "^");
 
 		StringTokenizer st = new StringTokenizer(query,"^-+()", true);
 		String compressedQuery = "";
 		while (st.hasMoreTokens()) {	
 			String token = st.nextToken();
 			if (token.equals("^") || token.equals("-") || token.equals("+") || token.equals("(") || token.equals(")"))
 				compressedQuery = compressedQuery+token;
 			else
 				compressedQuery = compressedQuery+queryCompressor.compressToken(token);
 		}
 		return compressedQuery;
 	}
 	
 	/**
 	 * Takes a user input query and compress it the way the corpus is compressed at indexation time
 	 * @param query
 	 * @return
 	 */
 	private static HashMap<String, Integer> tokenize(String query) {
 		query = query.trim();
 
 		query = query.replace("( ", " ");
 		query = query.replace(" )", " ");
 		query = query.replace(" NOT", " ");
 		query = query.replace("NOT ", " ");
 		query = query.replace(" AND", " ");
 		query = query.replace("AND ", " ");
 		query = query.replace(" OR", " ");
 		query = query.replace("OR ", " ");
 		query = query.replace(" and not ", " ");
 		query = query.replace(" and not", " ");
 		query = query.replace(" or not ", " ");
 		query = query.replace(" or not", " ");
 		query = query.replace(" and ", " ");
 		query = query.replace(" and", " ");
 		query = query.replace("(not", " ");
 
 		query = query.replace(" or", " ");
 		query = query.replace("^ ", " ");
 		query = query.replace("- ", " ");
 		query = query.replace("+ ", " ");
 		query = query.replace(" not ", " ");
 		query = query.replace(" not", " ");
 		query = query.replace("not ", " ");
 
 		String term;
 		StringTokenizer st = new StringTokenizer(query," ", true);
 		HashMap<String, Integer> compressedQuery = new HashMap<String, Integer>();
 		while (st.hasMoreTokens()) {	
 			String token = st.nextToken();
 			term = queryCompressor.compressToken(token);
 			if(compressedQuery.containsKey(term))
 				compressedQuery.put(term, compressedQuery.get(term)+1);
 			else 
				compressedQuery.put(term, 1);
 		}
 		return compressedQuery;
 	}
 
 
 
 
 	public static DefaultInvertedIndex getIndex() {
 		return index;
 	}
 
 }
