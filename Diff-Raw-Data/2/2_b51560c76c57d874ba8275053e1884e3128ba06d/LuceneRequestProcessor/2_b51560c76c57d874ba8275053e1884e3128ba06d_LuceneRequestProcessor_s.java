 package com.gentics.cr.lucene.search;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Vector;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Fieldable;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.Query;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRError;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
 import com.gentics.cr.lucene.indexer.index.LuceneAnalyzerFactory;
 import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
 import com.gentics.cr.lucene.search.highlight.AdvancedContentHighlighter;
 import com.gentics.cr.lucene.search.highlight.ContentHighlighter;
 import com.gentics.cr.lucene.search.query.CRQueryParserFactory;
 import com.gentics.cr.monitoring.MonitorFactory;
 import com.gentics.cr.monitoring.UseCase;
 import com.gentics.cr.util.generics.Lists;
 
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  */
 public class LuceneRequestProcessor extends RequestProcessor {
 
 	/**
 	 * Log4j logger.
 	 */
 	private static final Logger LOGGER = Logger.getLogger(LuceneRequestProcessor.class);
 	
 	/**
 	 * Initialized in the constructor with the provided CRConfig.
 	 * Used to search in the contentrepository and retrieve the objects.
 	 */
 	private CRSearcher searcher = null;
 	
 	/**
 	 * Name of the provided config. Initialized on constructor initialization.
 	 */
 	private String name = null;
 
 
 	/**
 	 * init CRMetaResolvableBean with or without parsed_query.
 	 */
 	private boolean showParsedQuery = false;
 
 	/**
 	 * The score of a document provides information about the relevance of the document for the searchquery.
 	 * Key: SCOREATTRIBUTE
 	 */
 	private static final String SCORE_ATTRIBUTE_KEY = "SCOREATTRIBUTE";
 	
 	/**
 	 * Provide the documents as is - that means no indexing has happend on the documents yet.
 	 * Key: GETSTOREDATTRIBUTES
 	 */
 	private static final String GET_STORED_ATTRIBUTE_KEY = "GETSTOREDATTRIBUTES";
 
 	/**
 	 * Provide the stored attributes.
 	 * Default value: false
 	 * Can be overwritten in config using key {@link LuceneRequestProcessor#GET_STORED_ATTRIBUTE_KEY}.
 	 */
 	private boolean getStoredAttributes = false;
 
 	/**
 	 * Define the maximum number of results to return.
 	 * Key: SEARCHCOUNT
 	 */
 	private static final String SEARCH_COUNT_KEY = "SEARCHCOUNT";
 
 	/**
 	 * Id of the document to use for creating a CRResolvableBean.
 	 * In most cases this should be: contentid
 	 */
 	private static final String ID_ATTRIBUTE_KEY = "idAttribute";
 	
 	/**
 	 * Map of all highlighters to use for content highlighting.
 	 * This map is created by {@link ContentHighlighter#getTransformerTable(GenericConfiguration)}
 	 */
 	private ConcurrentHashMap<String, ContentHighlighter> highlighters;
 
 	/**
 	 * Key where to find the total hits of the search in the metaresolvable.
 	 * Metaresolvable has to be enabled => LuceneRequestProcessor.META_RESOLVABLE_KEY
 	 */
 	public static final String META_HITS_KEY = "totalhits";
 
 	/**
 	 * Key where to find the start position of the search in the metaresolvable.
 	 * Metaresolvable has to be enabled => LuceneRequestProcessor.META_RESOLVABLE_KEY
 	 */
 	public static final String META_START_KEY = "start";
 
 	/**
 	 * Key where to find the total number of objects that have been retrieved (may be unequal to the number of totalhits).
 	 * Metaresolvable has to be enabled => LuceneRequestProcessor.META_RESOLVABLE_KEY
 	 */
 	public static final String META_COUNT_KEY = "count";
 
 	/**
 	 * Key where to find the query used for searching.
 	 * Metaresolvable has to be enabled => LuceneRequestProcessor.META_RESOLVABLE_KEY
 	 */
 	public static final String META_QUERY_KEY = "query";
 
 	/**
 	 * Key where to find the highest score for a document. 
 	 * Metaresolvable has to be enabled => LuceneRequestProcessor.META_RESOLVABLE_KEY
 	 */
 	public static final String META_MAXSCORE_KEY = "maxscore";
 
 	/**
 	 * Key where to find the query used for highlighting the content. Usually this is the 
 	 * searchqery without the permissions and meta search informations.
 	 * If this is not set, the requestFilter (default query) will be used
 	 */
 	public static final String HIGHLIGHT_QUERY_KEY = "highlightquery";
 
 	/**
 	 * Configuration key for the attributes to be searched when no explicit attribute is given in the query.
 	 */
 	public static final String SEARCHED_ATTRIBUTES_KEY = "searchedAttributes";
 
 	/**
 	 * Key to store the parsed query in the meta resolvable.
 	 */
 	public static final String PARSED_QUERY_KEY = "parsed_query";
 
 	/**
 	 * Key to configure if CRMetaResolvableBean should contain parsed_query.
 	 */
 	private static final String SHOW_PARSED_QUERY_KEY = "showparsedquery";
 
 	/**
 	 * Create new instance of LuceneRequestProcessor.
 	 * @param config CRConfig to use for initializing the searcher, highlighters and configuring this class.
 	 * @throws CRException {@link RequestProcessor} throws CRExcpetion in case of no config or cache initialization exception
 	 */
 	public LuceneRequestProcessor(final CRConfig config) throws CRException {
 		super(config);
 		this.name = config.getName();
 		this.searcher = new CRSearcher(config);
 		getStoredAttributes = Boolean.parseBoolean((String) config.get(GET_STORED_ATTRIBUTE_KEY));
 		highlighters = ContentHighlighter.getTransformerTable((GenericConfiguration) config);
 		showParsedQuery = Boolean.parseBoolean((String) this.config.get(SHOW_PARSED_QUERY_KEY));
 	}
 
 	/**
 	 * Converts a generic List to a List of Field.
 	 * @param l - generic list
 	 * @return list of vectors, null in case l was null, Vector<Field> with size
 	 * null if l.size() was 0
 	 */
 	private static List<Field> toFieldList(final List<Fieldable> l) {
 		if (l == null) {
 			return null;
 		} else if (l.size() > 0) {
 			return Lists.toSpecialList(l, Field.class);
 		} else {
 			return new Vector<Field>(0);
 		}
 	}
 
 	/**
 	 * This returns a collection of CRResolvableBeans containing the IDATTRIBUTE
 	 * and all STORED ATTRIBUTES of the Lucene Documents.
 	 * @param request - CRRequest containing the query in RequestFilter
 	 * @param doNavigation - if set to true there will be generated explanation
 	 * output to the explanation logger of CRSearcher
 	 * @return search result as Collection of CRResolvableBean
 	 * @throws CRException 
 	 */
 	public final Collection<CRResolvableBean> getObjects(final CRRequest request, final boolean doNavigation)
 			throws CRException {
 		UseCase ucGetObjects = startUseCase("LuceneRequestProcessor." + "getObjects(" + name + ")");
 
 		/**
 		 * search preparations (instantiate/validate all needed variables)
 		 */
 		UseCase ucPrepareSearch = startUseCase("LuceneRequestProcessor.getObjects(" + name + ")#prepareSearch");
 		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
 		int count = getCount(request);
 		int start = getStart(request);
 		ucPrepareSearch.stop();
 		/** * search preparations */
 
 		/**
 		 * Get results
 		 */
 		long indexSearchStartTime = System.currentTimeMillis();
 		UseCase ucSearch = startUseCase("LuceneRequestProcessor." + "getObjects(" + name + ")#search");
 		HashMap<String, Object> searchResult = null;
 		try {
 			searchResult = this.searcher.search(
 				request.getRequestFilter(),
 				getSearchedAttributes(),
 				count,
 				start,
 				doNavigation,
 				request.getSortArray(),
 				request);
 		} catch (IOException ex) {
 			LOGGER.error("Error while getting search results from index.");
 			throw new CRException(ex);
 		}
 		ucSearch.stop();
 		LOGGER.debug("Search in Index took " + (System.currentTimeMillis() - indexSearchStartTime) + "ms");
 		/** * Get results */
 
 		if (LOGGER.isDebugEnabled()) {
 			if (searchResult != null) {
 				for (Object res : searchResult.values()) {
 					if (res instanceof LinkedHashMap) {
 						LinkedHashMap<?, ?> documents = (LinkedHashMap<?, ?>) res;
 						if (documents != null) {
 							for (Entry<?, ?> entry : documents.entrySet()) {
 								Object object = entry.getKey();
 								if (object instanceof Document) {
 									Document doc = (Document) object;
 									if (doc != null) {
 										LOGGER.debug("LuceneRequestProcessor.getObjects: "
												+ doc.getField("contentid").toString());
 									}
 								}
 							}
 						}
 					}
 				}
 			} else {
 				LOGGER.debug("No results found.");
 			}
 		}
 		/**
 		 * process search
 		 */
 		UseCase ucProcessSearch = startUseCase("LuceneRequestProcessor." + "getObjects(" + name + ")#processSearch");
 		if (searchResult != null) {
 			Query parsedQuery = (Query) searchResult.get(CRSearcher.RESULT_QUERY_KEY);
 
 			result = processMetaData(result, searchResult, parsedQuery, request, start, count);
 			result = processSearchResolvables(result, searchResult, parsedQuery, request);
 		} else {
 			// searchresult is null - we don't want to proceed - we want to throw an error
 			result = null;
 		}
 		ucProcessSearch.stop();
 		/** * process search */
 
 		ucGetObjects.stop();
 		return result;
 	}
 
 	/**
 	 * Start a usecase.
 	 * @param message Use the specified message as description.
 	 * @return Instantiated usecase
 	 */
 	private UseCase startUseCase(final String message) {
 		return MonitorFactory.startUseCase(message);
 	}
 
 	/**
 	 * Get count (number of items to return) from request and validate it. Fall back to config count if not set.
 	 * @param request Request to get the count of.
 	 * @return count integer
 	 * @throws CRException If cound can not be determined
 	 */
 	private int getCount(final CRRequest request) throws CRException {
 		int count = request.getCount();
 		//IF COUNT IS NOT SET IN THE REQUEST, USE DEFAULT VALUE LOADED FROM CONFIG
 		if (count <= 0) {
 			String countConfigValue = (String) this.config.get(SEARCH_COUNT_KEY);
 			if (countConfigValue != null) {
 				count = Integer.valueOf(countConfigValue);
 			}
 		}
 		if (count <= 0) {
 			String message = "Default count is lower or equal to 0! This will "
 					+ "result in an error. Overthink your config (insert rp."
 					+ "<number>.searchcount=<value> in your properties file)!";
 			LOGGER.error(message);
 			throw new CRException(new CRError("Error", message));
 		}
 		return count;
 	}
 
 	/**
 	 * Get the start position from the request.
 	 * @param request request to get the start position from.
 	 * @return return a position great than 0
 	 * @throws CRException if start < 0 an error is thrown
 	 */
 	private int getStart(final CRRequest request) throws CRException {
 		int start = request.getStart();
 		if (start < 0) {
 			String message = "Bad request: start is lower than 0!";
 			LOGGER.error(message);
 			throw new CRException(new CRError("Error", message));
 		}
 		return start;
 	}
 
 	/**
 	 * Create a metadata bean using the provided arguments (contentid: 10001).
 	 * @param result List of resolvables to add it to
 	 * @param searchResult List of searchresults to use for metadata object
 	 * @param parsedQuery query used to fetch the results
 	 * @param request CRRequest
 	 * @param start start position
 	 * @param count number of items to return
 	 * @return list of results with added metadata bean
 	 */
 	private ArrayList<CRResolvableBean> processMetaData(final ArrayList<CRResolvableBean> result,
 			final HashMap<String, Object> searchResult, final Query parsedQuery, final CRRequest request,
 			final int start, final int count) {
 		UseCase ucProcessSearchMeta = startUseCase("LuceneRequestProcessor.getObjects(" + name
 				+ ")#processSearch.Metaresolvables");
 
 		Object metaKey = request.get(META_RESOLVABLE_KEY);
 		if (metaKey != null && (Boolean) metaKey) {
 			final CRResolvableBean metaBean;
 			if (showParsedQuery) {
 				metaBean = new CRMetaResolvableBean(searchResult, request, parsedQuery, start, count);
 			} else {
 				metaBean = new CRMetaResolvableBean(searchResult, request, start, count);
 			}
 			result.add(metaBean);
 		}
 
 		ucProcessSearchMeta.stop();
 		return result;
 	}
 
 	/**
 	 * do the actual search, parse the highlight query and process all documents.
 	 * @param result List to store the resulting documents in
 	 * @param searchResult Actual searchresults from Searcher
 	 * @param parsedQuery query to use for storing with the documents
 	 * @param request needed for highlighting the query
 	 * @return list of results containing all documents
 	 */
 	private ArrayList<CRResolvableBean> processSearchResolvables(final ArrayList<CRResolvableBean> result,
 			final HashMap<String, Object> searchResult, Query parsedQuery, final CRRequest request) {
 		UseCase ucProcessSearchResolvables = startUseCase("LuceneRequestProcessor.getObjects(" + name
 				+ ")#processSearch.Resolvables");
 
 		LinkedHashMap<Document, Float> docs = objectToLinkedHashMapDocuments(searchResult
 				.get(CRSearcher.RESULT_RESULT_KEY));
 
 		LuceneIndexLocation idsLocation = LuceneIndexLocation.getIndexLocation(this.config);
 		IndexAccessor indexAccessor = idsLocation.getAccessor();
 		IndexReader reader = null;
 		try {
 			reader = indexAccessor.getReader(false);
 
 			parseHighlightQuery(request, reader, parsedQuery);
 
 			processDocuments(docs, result, reader, parsedQuery);
 
 		} catch (IOException e) {
 			LOGGER.error("Cannot get Index reader for highlighting", e);
 		} finally {
 			indexAccessor.release(reader, false);
 		}
 
 		ucProcessSearchResolvables.stop();
 		return result;
 	}
 
 	/**
 	 * Parse the highlight query with the analyzer/parser provided by the config.
 	 * @param request CRRequest used to get the parser instance
 	 * @param reader IndexReader for rewriting the parsedQuery
 	 * @param parsedQuery query for parsed query.
 	 * @return highlighted query
 	 * @throws IOException if rewriting the query goes wrong this exception is thrown
 	 */
 	private Query parseHighlightQuery(final CRRequest request, final IndexReader reader, Query parsedQuery)
 			throws IOException {
 		//PARSE HIGHLIGHT QUERY
 		Object highlightQuery = request.get(HIGHLIGHT_QUERY_KEY);
 		if (highlightQuery != null) {
 			Analyzer analyzer = LuceneAnalyzerFactory.createAnalyzer((GenericConfiguration) this.config);
 			QueryParser parser = CRQueryParserFactory.getConfiguredParser(
 				getSearchedAttributes(), analyzer, request, config);
 			try {
 				parsedQuery = parser.parse((String) highlightQuery);
 				parsedQuery = parsedQuery.rewrite(reader);
 
 			} catch (ParseException e) {
 				LOGGER.error("Error while parsing hightlight query", e);
 			}
 		}
 		return parsedQuery;
 	}
 
 	/**
 	 * Perform highlighting for one document.
 	 * @param crBean bean to check if we need to highlight something and set the highlighting afterwards.
 	 * @param doc document to get the document id for the highligther
 	 * @param parsedQuery rewritten Query
 	 * @param reader prepared index Reader
 	 */
 	private void doHighlighting(final CRResolvableBean crBean, final Document doc, final Query parsedQuery,
 			final IndexReader reader) {
 
 		//IF HIGHLIGHTERS ARE CONFIGURED => DO HIGHLIGHTNING
 		if (highlighters != null) {
 			UseCase ucProcessSearchHighlight = MonitorFactory.startUseCase("LuceneRequestProcessor." + "getObjects("
 					+ name + ")#processSearch.Highlight");
 			long s2 = System.currentTimeMillis();
 			for (Entry<String, ContentHighlighter> contentHighlighter : highlighters.entrySet()) {
 				ContentHighlighter highligther = contentHighlighter.getValue();
 				String att = contentHighlighter.getKey();
 				//IF crBean matches the highlighters rule => highlight
 				if (highligther.match(crBean)) {
 					String ret = null;
 					if (highligther instanceof AdvancedContentHighlighter) {
 						AdvancedContentHighlighter advancedHighlighter = (AdvancedContentHighlighter) highligther;
 						int documentId = Integer.parseInt(doc.get("id"));
 
 						ret = advancedHighlighter.highlight(parsedQuery, reader, documentId, att);
 
 					} else {
 						ret = highligther.highlight((String) crBean.get(att), parsedQuery);
 					}
 					if (ret != null && !"".equals(ret)) {
 						crBean.set(att, ret);
 					}
 				}
 			}
 			LOGGER.debug("Highlighters took " + (System.currentTimeMillis() - s2) + "ms");
 			ucProcessSearchHighlight.stop();
 		}
 	}
 
 	private void processDocuments(final LinkedHashMap<Document, Float> docs, final ArrayList<CRResolvableBean> result,
 			final IndexReader reader, final Query parsedQuery) {
 		String scoreAttribute = (String) config.get(SCORE_ATTRIBUTE_KEY);
 
 		//PROCESS RESULT
 		if (docs != null) {
 			String idAttribute = (String) this.config.get(ID_ATTRIBUTE_KEY);
 			for (Entry<Document, Float> entry : docs.entrySet()) {
 				Document doc = entry.getKey();
 				Float score = entry.getValue();
 				CRResolvableBean crBean = new CRResolvableBean(doc.get(idAttribute));
 				if (getStoredAttributes) {
 					for (Field field : toFieldList(doc.getFields())) {
 						if (field.isStored()) {
 							if (field.isBinary()) {
 								crBean.set(field.name(), field.getBinaryValue());
 							} else {
 								crBean.set(field.name(), field.stringValue());
 							}
 						}
 					}
 				}
 				if (scoreAttribute != null && !"".equals(scoreAttribute)) {
 					crBean.set(scoreAttribute, score);
 				}
 				//DO HIGHLIGHTING
 				doHighlighting(crBean, doc, parsedQuery, reader);
 
 				LOGGER.debug("Found " + crBean.getContentid() + " with score " + score.toString());
 				result.add(crBean);
 			}
 		}
 	}
 
 	/**
 	 * TODO javadoc.
 	 * @param obj TODO javadoc
 	 * @return TODO javadoc
 	 */
 	@SuppressWarnings("unchecked")
 	private LinkedHashMap<Document, Float> objectToLinkedHashMapDocuments(final Object obj) {
 		return (LinkedHashMap<Document, Float>) obj;
 	}
 
 	/**
 	 * @return the attributes to search in from the condfig.
 	 */
 	private String[] getSearchedAttributes() {
 		String sa = (String) this.config.get(SEARCHED_ATTRIBUTES_KEY);
 		String[] ret = null;
 		if (sa != null) {
 			ret = sa.split(",");
 		}
 		return ret;
 	}
 
 	@Override
 	public void finalize() {
 
 	}
 
 }
