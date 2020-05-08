 package com.gentics.cr.lucene.search;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.Explanation;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.Searcher;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.TopDocsCollector;
 import org.apache.lucene.search.TopFieldCollector;
 import org.apache.lucene.search.TopScoreDocCollector;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.lucene.didyoumean.DidYouMeanProvider;
 import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
 import com.gentics.cr.lucene.indexer.index.LuceneAnalyzerFactory;
 import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
 import com.gentics.cr.lucene.search.query.CRQueryParserFactory;
 import com.gentics.cr.util.StringUtils;
 import com.gentics.cr.util.generics.Instanciator;
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class CRSearcher {
 
 	private static Logger log = Logger.getLogger(CRSearcher.class);
 	private static Logger log_explain = Logger.getLogger(CRSearcher.class);
 
 	protected static final String INDEX_LOCATION_KEY = "indexLocation";
 	protected static final String COMPUTE_SCORES_KEY = "computescores";
 	protected static final String STEMMING_KEY = "STEMMING";
 	protected static final String STEMMER_NAME_KEY = "STEMMERNAME";
 	private static final String COLLECTOR_CLASS_KEY = "collectorClass";
 	private static final String COLLECTOR_CONFIG_KEY = "collector";
 
 	/**
 	 * Key to store the searchquery in the result.
 	 */
 	public static final String RESULT_QUERY_KEY = "query";
 	/**
 	 * Key to store the hitcount in the result.
 	 */
 	public static final String RESULT_HITS_KEY = "hits";
 	/**
 	 * Key to store the result in the result.
 	 */
 	public static final String RESULT_RESULT_KEY = "result";
 	
 	/**
 	 * Key to store the maximum score of the result in the result.
 	 */
 	public static final String RESULT_MAXSCORE_KEY = "maxscore";
 	
 	/**
 	 * Key to store the bestquery in the result.
 	 */
 	public static final String RESULT_BESTQUERY_KEY = "bestquery";
 	
 	/**
 	 * Key to store the hitcount of the bestquery in the result.
 	 */
 	public static final String RESULT_BESTQUERYHITS_KEY = "bestqueryhits";
 	
 	/**
 	 * Key to store the hitcount of the bestquery in the result.
 	 */
 	public static final String RESULT_SUGGESTIONS_KEY = "suggestions";
 	
 	/**
 	 * Key to put the suggested term for the bestresult into the result.
 	 */
 	public static final String RESULT_SUGGESTEDTERM_KEY = "suggestedTerm";
 
 	/**
 	 * Key to configure the limit of results we activate the didyoumean code. 
 	 */
 	private static final String DIDYOUMEAN_ACTIVATE_KEY =
 		"didyoumean_activatelimit";
 	
 	
 	public static final String DIDYOUMEAN_ENABLED_KEY = "didyoumean";
 	private static final String DIDYOUMEAN_BESTQUERY_KEY = "didyoumeanbestquery";
 	private static final String ADVANCED_DIDYOUMEAN_BESTQUERY_KEY = "didyoumeanbestqueryadvanced";
 	private static final String DIDYOUMEAN_SUGGEST_COUNT_KEY = "didyoumeansuggestions";
 	private static final String DIDYOUMEAN_MIN_SCORE = "didyoumeanminscore";
 	
 	protected CRConfig config;
 	private boolean computescores = true;
 	private boolean didyoumeanenabled=false;
 	private boolean didyoumeanbestquery=true;
 	private boolean advanceddidyoumeanbestquery=false;
 	private int didyoumeansuggestcount = 5;
 	private float didyoumeanminscore = 0.5f;
 	/**
 	 * resultsizelimit to activate the didyoumeanfunctionality
 	 */
 	private int didyoumeanactivatelimit = 0;
 	
 	private DidYouMeanProvider didyoumeanprovider = null;
 	
 	/**
 	 * Create new instance of CRSearcher
 	 * @param config
 	 */
 	public CRSearcher(CRConfig config) {
 		this.config = config;
 		computescores = config.getBoolean(COMPUTE_SCORES_KEY,computescores);
 		didyoumeanenabled =
 			config.getBoolean(DIDYOUMEAN_ENABLED_KEY, didyoumeanenabled);
 		didyoumeansuggestcount =
 			config.getInteger(DIDYOUMEAN_SUGGEST_COUNT_KEY, didyoumeansuggestcount);
 		didyoumeanminscore =
 			config.getFloat(DIDYOUMEAN_MIN_SCORE, didyoumeanminscore);
 
 		if(didyoumeanenabled) {
 			didyoumeanprovider = new DidYouMeanProvider(config);
 			didyoumeanbestquery =
 				config.getBoolean(DIDYOUMEAN_BESTQUERY_KEY, didyoumeanbestquery);
 			advanceddidyoumeanbestquery = config.getBoolean(
 					ADVANCED_DIDYOUMEAN_BESTQUERY_KEY, advanceddidyoumeanbestquery);
 			didyoumeanactivatelimit =
 				config.getInteger(DIDYOUMEAN_ACTIVATE_KEY, didyoumeanactivatelimit);
 		}
 		
 		
 	}
 
 	/**
 	 * Create the appropriate collector.
 	 * @param hits
 	 * @param sorting
 	 * @return
 	 * @throws IOException 
 	 */
 	@SuppressWarnings("unchecked")
 private TopDocsCollector<?> createCollector(final Searcher searcher,
 			final int hits, final String[] sorting, final boolean computescores,
 			final String[] userPermissions) throws IOException {
 		TopDocsCollector<?> coll = null;
 		String collectorClassName = (String) config.get(COLLECTOR_CLASS_KEY);
 		if (collectorClassName != null) {
 			Class<?> genericCollectorClass;
 			try {
 				genericCollectorClass = Class.forName(collectorClassName);
 				GenericConfiguration collectorConfiguration =
 					config.getSubConfigs().get(COLLECTOR_CONFIG_KEY.toUpperCase());
 				Object[][] prioritizedParameters = new Object[3][];
 				prioritizedParameters[0] =
 					new Object[]{searcher, hits, collectorConfiguration, userPermissions};
 				prioritizedParameters[1] =
 					new Object[]{searcher, hits, collectorConfiguration};
 				prioritizedParameters[2] =
 					new Object[]{hits, collectorConfiguration};
 				Object collectorObject = Instanciator.getInstance(genericCollectorClass,
 						prioritizedParameters);
 				if (collectorObject instanceof TopDocsCollector) {
 					coll = (TopDocsCollector<?>) collectorObject;
 				}
 			} catch (ClassNotFoundException e) {
 				log.error("Cannot find configured collector class: \""
 						+ collectorClassName + "\" in " + config.getName(), e);
 			}
 
 		}
 		if (coll == null && sorting != null) {
 			//TODO make collector configurable
 			coll = TopFieldCollector.create(createSort(sorting), hits, true,
 					computescores, computescores, computescores);
 		}
 		if (coll == null) {
 			coll = TopScoreDocCollector.create(hits, true);
 		}
 		return coll;
 	}
 
 	/**
 	 * Creates a Sort object for the Sort collector. The general syntax for sort
 	 * properties is [property][:asc|:desc] where the postfix determines the
 	 * sortorder. If neither :asc nor :desc is given, the sorting will be done
 	 * ascending for this property.
 	 * 
 	 * NOTE: using "score:asc" or "score:desc" will both result in an ascating relevance sorting
 	 * @param sorting
 	 * @return
 	 */
 	private Sort createSort(String[] sorting) {
 		Sort ret = null;
 		ArrayList<SortField> sf = new ArrayList<SortField>();
 		for (String s : sorting) {
 			// split attribute on :. First element is attribute name the
 			// second is the direction
 			String[] sort = s.split(":");
 
 			if (sort[0] != null) {
 				boolean reverse;
 				if ("desc".equals(sort[1].toLowerCase())) {
 					reverse = true;
 				} else {
 					reverse = false;
 				}
 				if("score".equalsIgnoreCase(sort[0]))
 					sf.add(SortField.FIELD_SCORE);
 				else
 					sf.add(new SortField(sort[0], Locale.getDefault(), reverse));
 			}
 
 		}
 		ret = new Sort(sf.toArray(new SortField[]{}));
 
 		return ret;
 	}
 
 	public void finalize() {
 		LuceneIndexLocation idsLocation =
 			LuceneIndexLocation.getIndexLocation(this.config);
 		if (idsLocation != null) {
 			idsLocation.stop();
 		}
 	}
 
 	/**
 	 * Run a Search against the lucene index.
 	 * @param searcher TODO javadoc
 	 * @param parsedQuery TODO javadoc
 	 * @param count TODO javadoc
 	 * @param collector TODO javadoc
 	 * @param explain TODO javadoc
 	 * @param start TODO javadoc
 	 * @return ArrayList of results
 	 */
 	private HashMap<String, Object> runSearch(
 			final TopDocsCollector<?> collector, final Searcher searcher,
 			final Query parsedQuery, final boolean explain, final int count,
 			final int start) {
 		try {
 
 			searcher.search(parsedQuery, collector);
 			TopDocs tdocs = collector.topDocs();
 			Float maxScoreReturn = tdocs.getMaxScore();
 			ScoreDoc[] hits = tdocs.scoreDocs;
 			
 			LinkedHashMap<Document, Float> result =
 				new LinkedHashMap<Document, Float>(hits.length);
 
 				//Calculate the number of documents to be fetched
 				int num = Math.min(hits.length - start, count);
 				for (int i = 0; i < num; i++) {
 					Document doc = searcher.doc(hits[start + i].doc);
 					//add id field for AdvancedContentHighlighter
 					doc.add(new Field("id", hits[start + i].doc + "", Field.Store.YES,
 							Field.Index.NO));
 					result.put(doc, hits[start + i].score);
 					if (explain) {
 						Explanation ex = searcher.explain(parsedQuery, hits[start + i].doc);
 						log_explain.debug("Explanation for " + doc.toString() + " - "
 								+ ex.toString());
 					}
 			}
 			log.debug("Fetched Document " + start + " to " + (start + num) + " of "
 					+ collector.getTotalHits() + " found Documents");
 
 			HashMap<String,Object> ret = new HashMap<String,Object>(2);
 			ret.put(RESULT_RESULT_KEY, result);
 			ret.put(RESULT_MAXSCORE_KEY, maxScoreReturn);
 			return ret;
 
 		} catch (Exception e) {
 			log.error("Error running search for query " + parsedQuery, e);
 		}
 		return null;
 	}
 
 	public HashMap<String,Object> search(String query,
 			String[] searchedAttributes, int count, int start, boolean explain)
 			throws IOException {
 		return search(query, searchedAttributes, count, start, explain, null);
 	}
 
 	public HashMap<String,Object> search(String query,String[] searchedAttributes,int count,int start,boolean explain, String[] sorting) throws IOException{
 		return search(query, searchedAttributes, count, start, explain, sorting, null);
 	}
 	/**
 	 * Search in lucene index.
 	 * @param query query string
 	 * @param searchedAttributes TODO javadoc
 	 * @param count - max number of results that are to be returned
 	 * @param start - the start number of the page e.g. if start = 50 and count =
 	 * 10 you will get the elements 50 - 60
 	 * @param explain - if set to true the searcher will add extra explain output
 	 * to the logger com.gentics.cr.lucene.searchCRSearcher.explain
 	 * @param sorting - this argument takes the sorting array that can look like
 	 * this: ["contentid:asc","name:desc"]
 	 * @param request TODO javadoc
 	 *
 	 * @return HashMap&lt;String,Object&gt; with two entries. Entry "query"
 	 * contains the parsed query and entry "result" contains a Collection of
 	 * result documents.
 	 * @throws IOException TODO javadoc
 	 */
 	@SuppressWarnings("unchecked")
 	public final HashMap<String, Object> search(final String query,
 			final String[] searchedAttributes, final int count, final int start,
 			final boolean explain, final String[] sorting, final CRRequest request)
 			throws IOException {
 
 
 		Searcher searcher;
 		Analyzer analyzer;
 		//Collect count + start hits
 		int hits = count + start;
 
 		LuceneIndexLocation idsLocation =
 			LuceneIndexLocation.getIndexLocation(this.config);
 
 		IndexAccessor indexAccessor = idsLocation.getAccessor();
 		searcher = indexAccessor.getPrioritizedSearcher();
 		Object userPermissionsObject = request.get(CRRequest.PERMISSIONS_KEY);
 		String[] userPermissions = new String[0];
 		if (userPermissionsObject instanceof String[]) {
 			userPermissions = (String[]) userPermissionsObject;
 		}
 		TopDocsCollector<?> collector = createCollector(searcher, hits, sorting,
 				computescores, userPermissions);
 		HashMap<String, Object> result = null;
 		try {
 
 			analyzer = LuceneAnalyzerFactory
 					.createAnalyzer((GenericConfiguration) this.config);
 
 			if (searchedAttributes != null && searchedAttributes.length > 0) {
 				QueryParser parser = CRQueryParserFactory.getConfiguredParser(
 						searchedAttributes, analyzer, request, config);
 				
 				Query parsedQuery = parser.parse(query);
 				//GENERATE A NATIVE QUERY
 				
 				parsedQuery = searcher.rewrite(parsedQuery);
 				
 				result = new HashMap<String, Object>(3);
 				result.put(RESULT_QUERY_KEY, parsedQuery);
 				
 				
 				Map<String, Object> ret = runSearch(collector, searcher,
 						parsedQuery, explain, count, start);
 				LinkedHashMap<Document, Float> coll =
 					(LinkedHashMap<Document, Float>) ret.get(RESULT_RESULT_KEY);
 				Float maxScore	= (Float) ret.get(RESULT_MAXSCORE_KEY);
 				result.put(RESULT_RESULT_KEY, coll);
 				int totalhits = collector.getTotalHits();
 				
 				result.put(RESULT_HITS_KEY, totalhits);
 				result.put(RESULT_MAXSCORE_KEY, maxScore);
 				
 				//PLUG IN DIDYOUMEAN
 				boolean didyoumeanEnabledForRequest = StringUtils
 						.getBoolean(request.get(DIDYOUMEAN_ENABLED_KEY), true);
 
 				if (start == 0 && didyoumeanenabled
 						&& didyoumeanEnabledForRequest
 						&& (totalhits <= didyoumeanactivatelimit
 								|| maxScore == Float.NaN
 								|| maxScore < this.didyoumeanminscore)) {
 					String parsedQueryString = parsedQuery.toString().replaceAll("\\(\\)", "");
 					
 					HashMap<String, Object> didyoumeanResult =
 						didyoumean(query, parsedQuery, indexAccessor, parser,
 								searcher, sorting, userPermissions);
 					result.putAll(didyoumeanResult);
 				}
 				
 				//PLUG IN DIDYOUMEAN END
 				int size = 0;
 				if (coll != null) {
 					size = coll.size();
 				}
 				log.debug("Fetched " + size + " objects with query: " + query);
 			}
 		} catch (Exception e) {
 			log.error("Error getting the results.", e);
 			result = null;
 		} finally {
 			indexAccessor.release(searcher);
 		}
 		return result;
 	}
 	/**
 	 * get Result for didyoumean.
 	 * @param originalQuery - original query for fallback when wildcards are
 	 * replaced with nothing.
 	 * @param parsedQuery - parsed query in which we can replace the search
 	 * words
 	 * @param indexAccessor - accessor to get results from the index
 	 * @param parser - query parser
 	 * @param searcher - searcher to search in the index
 	 * @param sorting - sorting to use
 	 * @param userPermissions - user permission used to get the original result
 	 * @return Map containing the replacement for the searchterm and the result
 	 * for the resulting query.
 	 */
 	private HashMap<String, Object> didyoumean(final String originalQuery,
 			final Query parsedQuery, final IndexAccessor indexAccessor,
 			final QueryParser parser, final Searcher searcher,
 			final String[] sorting, final String[] userPermissions) {
 		long dymStart = System.currentTimeMillis();
 		HashMap<String, Object> result = new HashMap<String, Object>(3);
 		
 		try {
 			IndexReader reader = indexAccessor.getReader(false);
 			Query rwQuery = parsedQuery;
 			Set<Term> termset = new HashSet<Term>();
 			rwQuery.extractTerms(termset);
 			
 			Map<String, String[]> suggestions = this.didyoumeanprovider
 					.getSuggestions(termset, this.didyoumeansuggestcount,
 							reader);
 			boolean containswildcards = (originalQuery.indexOf('*') != -1);
 			if (suggestions.size() == 0 && containswildcards) {
 				String newSuggestionQuery =
 					originalQuery.replaceAll(":\\*?([^*]*)\\*?", ":$1");
 				try {
 					rwQuery = parser.parse(newSuggestionQuery);
 					termset = new HashSet<Term>();
					//REWRITE NEWLY PARSED QUERY
					rwQuery = rwQuery.rewrite(reader);
 					rwQuery.extractTerms(termset);
 					suggestions = this.didyoumeanprovider
 					.getSuggestions(termset, this.didyoumeansuggestcount,
 							reader);
 					
 				} catch (ParseException e) {
 					log.error("Cannot Parse Suggestion Query.", e);
 				}
 				
 			}
 			result.put(RESULT_SUGGESTIONS_KEY, suggestions);
 			
 			log.debug("DYM Suggestions took "
 					+ (System.currentTimeMillis() - dymStart) + "ms");
 			String rewrittenQuery =
 				rwQuery.toString().replaceAll("\\(\\)", "");
 			indexAccessor.release(reader, false);
 			
 			if (didyoumeanbestquery || advanceddidyoumeanbestquery) {
 				//SPECIAL SUGGESTION
 				//TODO Test if the query will be altered and if any suggestion
 				//have been made... otherwise don't execute second query and
 				//don't include the bestquery
 				for (Entry<String, String[]> e : suggestions.entrySet()) {
 					String term = e.getKey();
 					String[] suggestionsForTerm = e.getValue();
 					if (advanceddidyoumeanbestquery) {
 						TreeMap<Integer, HashMap<String, Object>>
 							suggestionsResults = new TreeMap<Integer,
 								HashMap<String, Object>>(
 										Collections.reverseOrder());
 						for (String suggestedTerm : suggestionsForTerm) {
 							String newquery =
 								rewrittenQuery.replaceAll(term, suggestedTerm);
 							HashMap<String, Object> resultOfNewQuery =
 								getResultsForQuery(newquery, parser, searcher,
 										sorting, userPermissions);
 							if (resultOfNewQuery != null) {
 								resultOfNewQuery.put(RESULT_SUGGESTEDTERM_KEY,
 										suggestedTerm);
 								Integer resultCount =
 									(Integer) resultOfNewQuery
 											.get(RESULT_BESTQUERYHITS_KEY);
 								if (resultCount > 0) {
 									suggestionsResults.put(resultCount,
 											resultOfNewQuery);
 								}
 							}
 							
 						}
 						result.put(RESULT_BESTQUERY_KEY, suggestionsResults);
 					} else {
 						String newquery = rewrittenQuery
 								.replaceAll(term, suggestionsForTerm[0]);
 						HashMap<String, Object> resultOfNewQuery = 
 							getResultsForQuery(newquery, parser, searcher,
 									sorting, userPermissions);
 						result.putAll(resultOfNewQuery);
 					}
 				}
 			}
 			log.debug("DYM took " + (System.currentTimeMillis() - dymStart)
 					+ "ms");
 			return result;
 		} catch (IOException e) {
 			log.error("Cannot access index for didyoumean functionality.", e);
 		}
 		return null;
 	}
 	
 	private HashMap<String, Object> getResultsForQuery(final String query,
 			final QueryParser parser, final Searcher searcher,
 			final String[] sorting, final String[] userPermissions){
 		HashMap<String, Object> result = new HashMap<String, Object>(3);
 		try {
 			Query bestQuery = parser.parse(query);
 			TopDocsCollector<?> bestcollector = createCollector(searcher, 1,
 					sorting, computescores, userPermissions);
 			runSearch(bestcollector, searcher, bestQuery, false, 1, 0);
 			result.put(RESULT_BESTQUERY_KEY, bestQuery);
 			result.put(RESULT_BESTQUERYHITS_KEY, bestcollector.getTotalHits());
 			return result;
 		} catch (ParseException e) {
 			log.error("Cannot parse query to get results for.", e);
 		} catch (IOException e) {
 			log.error("Cannot create collector to get results for query.", e);
 		}
 		return null;
 	}
 }
