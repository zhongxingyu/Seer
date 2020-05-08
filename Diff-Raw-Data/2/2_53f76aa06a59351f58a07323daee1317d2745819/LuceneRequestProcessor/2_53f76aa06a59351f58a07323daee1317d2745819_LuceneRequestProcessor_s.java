 package com.gentics.cr.lucene.search;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Hashtable;
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
  *
  */
 public class LuceneRequestProcessor extends RequestProcessor {
 
 	protected static Logger log = Logger.getLogger(LuceneRequestProcessor.class);
 	protected static Logger ext_log = Logger.getLogger(LuceneRequestProcessor.class);
 	private CRSearcher searcher = null;
 	protected String name=null;
 	
 	private boolean getStoredAttributes = false;
 	
 	private static final String SCORE_ATTRIBUTE_KEY = "SCOREATTRIBUTE";
 	private static final String GET_STORED_ATTRIBUTE_KEY = "GETSTOREDATTRIBUTES";
 	
 	private Hashtable<String,ContentHighlighter> highlighters;
 	
 	private static final String SEARCH_COUNT_KEY = "SEARCHCOUNT";
 	
 	private static final String ID_ATTRIBUTE_KEY = "idAttribute";
 	
 	
 	
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
 	 * TODO
 	 */
 	public static final String META_COUNT_KEY = "count";
 	
 	/**
 	 * TODO
 	 */
 	public static final String META_QUERY_KEY = "query";
 	
 	/**
 	 * TODO
 	 */
 	public static final String META_MAXSCORE_KEY = "maxscore";
 	
 	
 	/**
 	 * Key where to find the query used for highlighting the content. Usually this is the 
 	 * searchqery without the permissions and meta search informations.
 	 * If this is not set, the requestFilter (default query) will be used
 	 */
 	public static final String HIGHLIGHT_QUERY_KEY = "highlightquery";
 
 	private static final String SEARCHED_ATTRIBUTES_KEY = "searchedAttributes";
 
 	/**
 	 * Create new instance of LuceneRequestProcessor.
 	 * @param config
 	 * @throws CRException
 	 */
 	public LuceneRequestProcessor(final CRConfig config) throws CRException {
 		super(config);
 		this.name = config.getName();
 		this.searcher = new CRSearcher(config);
 		getStoredAttributes = Boolean.parseBoolean(
 				(String) config.get(GET_STORED_ATTRIBUTE_KEY));
 		highlighters = ContentHighlighter.getTransformerTable(
 				(GenericConfiguration) config);
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
 	public final Collection<CRResolvableBean> getObjects(
 	final CRRequest request, final boolean doNavigation) throws CRException {
 		UseCase uc = MonitorFactory.startUseCase("LuceneRequestProcessor."
 				+ "getObjects(" + name + ")");
 		UseCase ucPrepareSearch = MonitorFactory.startUseCase(
 				"LuceneRequestProcessor.getObjects(" + name 
 				+ ")#prepareSearch");
 		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
 		int count = request.getCount();
 		int start = request.getStart();
 		//IF COUNT IS NOT SET IN THE REQUEST, USE DEFAULT VALUE LOADED FROM
 		//CONFIG
 		if (count <= 0) {
 			String cstring = (String) this.config.get(SEARCH_COUNT_KEY);
 			if (cstring != null) {
 				count = new Integer(cstring);
 			}
 		}
 		if (count <= 0) {
 			String message = "Default count is lower or equal to 0! This will "
 				+ "result in an error. Overthink your config (insert rp."
 				+ "<number>.searchcount=<value> in your properties file)!";
 			log.error(message);
 			throw new CRException(new CRError("Error", message));
 		}
 		if (start < 0) {
 			String message = "Bad request: start is lower than 0!";
 			log.error(message);
 			throw new CRException(new CRError("Error", message));
 		}
 		String scoreAttribute = (String) config.get(SCORE_ATTRIBUTE_KEY);
 		//GET RESULT
 		long s1 = System.currentTimeMillis();
 		ucPrepareSearch.stop();
 		UseCase ucSearch = MonitorFactory.startUseCase("LuceneRequestProcessor."
 				+ "getObjects(" + name + ")#search");
 		HashMap<String, Object> searchResult	= null;
 		try {
 			searchResult = this.searcher.search(request.getRequestFilter(),
 				getSearchedAttributes(), count, start, doNavigation,
 				request.getSortArray(), request);
 		} catch (IOException ex) {
 			log.error("Error while getting search results from index.");
 			throw new CRException(ex);
 		}
 		ucSearch.stop();
 		UseCase ucProcessSearch = MonitorFactory.startUseCase(
 				"LuceneRequestProcessor." + "getObjects(" + name
 				+ ")#processSearch");
 		long e1 = System.currentTimeMillis();
 		log.debug("Search in Index took " + (e1 - s1) + "ms");
 		if (searchResult != null) {
 			UseCase ucProcessSearchMeta = MonitorFactory.startUseCase(
 					"LuceneRequestProcessor.getObjects(" + name
 					+ ")#processSearch.Metaresolvables");
 			Query parsedQuery =
 				(Query) searchResult.get(CRSearcher.RESULT_QUERY_KEY);
 
 			Object metaKey = request.get(META_RESOLVABLE_KEY);
 			if (metaKey != null && (Boolean) metaKey) {
 				CRResolvableBean metaBean = new CRMetaResolvableBean(
 						searchResult, request, start, count);
 				result.add(metaBean);
 			}
 			ucProcessSearchMeta.stop();
 			UseCase ucProcessSearchResolvables = MonitorFactory.startUseCase(
 					"LuceneRequestProcessor.getObjects(" + name
 					+ ")#processSearch.Resolvables");
 			LinkedHashMap<Document, Float> docs =
 				objectToLinkedHashMapDocuments(searchResult.get(
 						CRSearcher.RESULT_RESULT_KEY));
 			
 			LuceneIndexLocation idsLocation =
 			LuceneIndexLocation.getIndexLocation(this.config);
 		IndexAccessor indexAccessor = idsLocation.getAccessor();
 		IndexReader reader = null;
 		try {
 			reader = indexAccessor.getReader(false);
 			
 				//PARSE HIGHLIGHT QUERY
 				Object highlightQuery = request.get(HIGHLIGHT_QUERY_KEY);
 				if (highlightQuery != null) {
 					Analyzer analyzer = LuceneAnalyzerFactory.createAnalyzer(
 							(GenericConfiguration) this.config);
 					QueryParser parser = CRQueryParserFactory
 							.getConfiguredParser(getSearchedAttributes(),
 									analyzer, request, config);
 					try {
 						parsedQuery = parser.parse((String) highlightQuery);
						parsedQuery.rewrite(reader);
 						
 					} catch (ParseException e) {
 						log.error(e.getMessage());
 						e.printStackTrace();
 					}
 				}
 				
 				//PROCESS RESULT
 				
 				if (docs != null) {
 						String idAttribute = (String) this.config.get(ID_ATTRIBUTE_KEY);
 						for (Entry<Document, Float> e : docs.entrySet()) {
 							Document doc = e.getKey();
 							Float score = e.getValue();
 							CRResolvableBean crBean = new CRResolvableBean(doc.get(idAttribute));
 							if (getStoredAttributes) {
 								for (Field f : toFieldList(doc.getFields())) {
 									if (f.isStored()) {
 										if (f.isBinary()) {
 											crBean.set(f.name(), f.getBinaryValue());
 										} else {
 											crBean.set(f.name(), f.stringValue());
 										}
 									}
 								}
 							}
 							if (scoreAttribute != null && !"".equals(scoreAttribute)) {
 								crBean.set(scoreAttribute, score);
 							}
 							//DO HIGHLIGHTING
 							doHighlighting( crBean, doc, parsedQuery, reader);
 							
 							ext_log.debug("Found " + crBean.getContentid() + " with score "
 									+ score.toString());
 							result.add(crBean);
 					}
 				}
 			
 		} catch (IOException ioException) {
 					log.error("Cannot get Index reader for highlighting");
 			} finally {
 					indexAccessor.release(reader, false);
 			}
 			
 			ucProcessSearchResolvables.stop();
 			/*if(doNavigation)
 			{
 				//NOT IMPLEMENTED YET, BUT WE DO GENERATE MORE EXPLANATION OUTPUT YET
 				//log.error("LUCENEREQUESTPROCESSER CAN NOT YET RETURN A TREE STRUCTURE");
 			}*/
 		}
 		ucProcessSearch.stop();
 		uc.stop();
 		return result;
 	}
 
 	/**
 	 * Perform highlighting for one document
 	 * @param crBean
 	 * @param doc
 	 * @param parsedQuery rewritten Query
 	 * @param reader prepared index Reader
 	 */
 	private void doHighlighting(CRResolvableBean crBean, Document doc, Query parsedQuery, IndexReader reader) {
 	
 		
 			//IF HIGHLIGHTERS ARE CONFIGURED => DO HIGHLIGHTNING
 			if (highlighters != null) {
 				UseCase ucProcessSearchHighlight = MonitorFactory.startUseCase(
 						"LuceneRequestProcessor." + "getObjects(" + name
 						+ ")#processSearch.Highlight");
 				long s2 = System.currentTimeMillis();
 				for (Entry<String, ContentHighlighter> ch
 						: highlighters.entrySet()) {
 					ContentHighlighter h = ch.getValue();
 					String att = ch.getKey();
 					//IF crBean matches the highlighters rule => highlight
 					if (h.match(crBean)) {
 						String ret = null;
 						if (h instanceof AdvancedContentHighlighter) {
 							AdvancedContentHighlighter advancedHighlighter =
 								(AdvancedContentHighlighter) h;
 							int documentId = Integer.parseInt(doc.get("id"));
 							
 								ret = advancedHighlighter.highlight(parsedQuery, reader,
 										documentId, att);
 							
 						} else {
 							ret = h.highlight((String) crBean.get(att), parsedQuery);
 						}
 						if (ret != null && !"".equals(ret)) {
 							crBean.set(att, ret);
 						}
 					}
 			}
 			log.debug("Highlighters took " + (System.currentTimeMillis() - s2) + "ms");
 			ucProcessSearchHighlight.stop();
 		}
 	}
 
 	/**
 	 * TODO javadoc.
 	 * @param obj TODO javadoc
 	 * @return TODO javadoc
 	 */
 	@SuppressWarnings("unchecked")
 	private LinkedHashMap<Document, Float> objectToLinkedHashMapDocuments(
 			final Object obj) {
 		return (LinkedHashMap<Document, Float>) obj;
 	}
 	
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
 		if(this.searcher!=null)this.searcher.finalize();
 	}
 
 	
 }
