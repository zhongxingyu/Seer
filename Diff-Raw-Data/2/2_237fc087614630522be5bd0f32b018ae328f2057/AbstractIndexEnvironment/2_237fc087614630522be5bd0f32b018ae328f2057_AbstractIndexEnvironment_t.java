 package uk.ac.ebi.arrayexpress.utils.saxon.search;
 
 /*
  * Copyright 2009-2011 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 
 import net.sf.saxon.Configuration;
 import net.sf.saxon.om.DocumentInfo;
 import net.sf.saxon.om.NodeInfo;
 import net.sf.saxon.xpath.XPathEvaluator;
 
 import org.apache.commons.configuration.HierarchicalConfiguration;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.NumericField;
 import org.apache.lucene.facet.index.CategoryDocumentBuilder;
 import org.apache.lucene.facet.taxonomy.CategoryPath;
 import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.MatchAllDocsQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.TopFieldCollector;
 import org.apache.lucene.search.TotalHitCountCollector;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xmldb.api.DatabaseManager;
 import org.xmldb.api.base.Collection;
 import org.xmldb.api.base.ResourceIterator;
 import org.xmldb.api.base.ResourceSet;
 import org.xmldb.api.modules.XPathQueryService;
 
 import uk.ac.ebi.arrayexpress.app.Application;
 import uk.ac.ebi.arrayexpress.components.SaxonEngine;
 import uk.ac.ebi.arrayexpress.components.XmlDbConnectionPool;
 import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 import uk.ac.ebi.arrayexpress.utils.saxon.PrintUtils;
 import uk.ac.ebi.arrayexpress.utils.saxon.search.AbstractIndexEnvironment.AttsInfo;
 
 public abstract class AbstractIndexEnvironment {
 
 	// logging machinery
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 
 	// source index configuration (will be eventually removed)
 	public HierarchicalConfiguration indexConfig;
 
 	// index configuration, parsed
 	public String indexId;
 	public Directory indexDirectory;
 	// I need this to create an temporary directory during the relod job
 	// execution
 	public String indexLocationDirectory;
 	public PerFieldAnalyzerWrapper indexAnalyzer;
 	public String defaultField;
 
 	// I will not open the index in each request
 	private IndexReader ir = null;
 
 	// index document xpath
 	public String indexDocumentPath;
 
 	// number of documents indexed
 	private int countDocuments;
 
 	public int getCountDocuments() {
 		return countDocuments;
 	}
 
 	public void setCountDocuments(int count) {
 		this.countDocuments = count;
 	}
 
 	public String getDefaultField() {
 		return defaultField;
 	}
 
 	// private Map<String, XPathExpression> fieldXpe = new HashMap<String,
 	// XPathExpression>();
 
 	// keep information realted with attributes
 	public class AttsInfo {
 		public String name;
 		public String type;
 
 		public AttsInfo(String name, String type) {
 			setName(name);
 			setType(type);
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public void setName(String name) {
 			this.name = name;
 		}
 
 		public String getType() {
 			return type;
 		}
 
 		public void setType(String type) {
 			this.type = type;
 		}
 
 	}
 
 	// TODO: rpe (review this)
 	public IndexReader getIndexReader() {
 		if (ir == null) {
 			synchronized (this) {
 				try {
 					// logger.debug("test");
 					ir = IndexReader.open(this.indexDirectory, true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		return ir;
 
 	}
 
 	public void closeIndexReader() {
 		if (ir != null) {
 			try {
 				logger.debug("Close the closeIndexReader!!!");
 				ir.close();
 				ir = null;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	// TODO: rpe Just to test
 	// private void closeIndexReader(){
 	// ir=null;
 	// }
 
 	public void setDefaultField(String defaultField) {
 		this.defaultField = defaultField;
 	}
 
 	public String getDefaultSortField() {
 		return defaultSortField;
 	}
 
 	public void setDefaultSortField(String defaultSortField) {
 		this.defaultSortField = defaultSortField;
 	}
 
 	public boolean getDefaultSortDescending() {
 		return defaultSortDescending;
 	}
 
 	public void setDefaultSortDescending(boolean defaultSortDescending) {
 		this.defaultSortDescending = defaultSortDescending;
 	}
 
 	public int getDefaultPageSize() {
 		return defaultPageSize;
 	}
 
 	public void setDefaultPageSize(int defaultPageSize) {
 		this.defaultPageSize = defaultPageSize;
 	}
 
 	/**
 	 * Default field used to sort if anyone is specified
 	 */
 	protected String defaultSortField = "releasedate";
 
 	/**
 	 * Default orientation (Ascending)
 	 */
 	protected boolean defaultSortDescending = false;
 
 	/**
 	 * Default page size
 	 */
 	protected int defaultPageSize = 25;
 
 	public Map<String, FieldInfo> fields;
 
 	// document info
 	public int documentHashCode;
 
 	public AbstractIndexEnvironment(HierarchicalConfiguration indexConfig) {
 		this.indexConfig = indexConfig;
 		populateIndexConfiguration();
 		// TODO: review this (This is causing one error when you are forcing the
 		// index building on server start, and the lucenes directory is no there
 		// this is not common but it's an error - I will not change this until
 		// we merge ArrayExpress Code with Biosamples (the appraoch can be a
 		// little bit different)
 		setup();
 	}
 
 	private void populateIndexConfiguration() {
 		try {
 			this.indexId = this.indexConfig.getString("[@id]");
 
 			indexLocationDirectory = this.indexConfig.getString("[@location]");
 			this.indexDirectory = FSDirectory.open(new File(
 					indexLocationDirectory, this.indexId));
 			String indexAnalyzer = this.indexConfig
 					.getString("[@defaultAnalyzer]");
 			Analyzer a = (Analyzer) Class.forName(indexAnalyzer).newInstance();
 			this.indexAnalyzer = new PerFieldAnalyzerWrapper(a);
 
 			this.indexDocumentPath = indexConfig.getString("document[@path]");
 
 			this.defaultField = indexConfig
 					.getString("document[@defaultField]");
 
 			List fieldsConfig = indexConfig.configurationsAt("document.field");
 
 			this.fields = new HashMap<String, FieldInfo>();
 			for (Object fieldConfig : fieldsConfig) {
 				FieldInfo fieldInfo = new FieldInfo(
 						(HierarchicalConfiguration) fieldConfig);
 				fields.put(fieldInfo.name, fieldInfo);
 				if (null != fieldInfo.analyzer) {
 					Analyzer fa = (Analyzer) Class.forName(fieldInfo.analyzer)
 							.newInstance();
 					this.indexAnalyzer.addAnalyzer(fieldInfo.name, fa);
 				}
 			}
 
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 		}
 	}
 
 	public boolean doesFieldExist(String fieldName) {
 		return fields.containsKey(fieldName);
 	}
 
 	/*
 	 * (non-Javadoc) This is the mains function of this classe and it will
 	 * address the query, sort and paging issues
 	 * 
 	 * @see
 	 * uk.ac.ebi.arrayexpress.utils.saxon.search.IIndexEnvironment#queryPaged
 	 * (java.lang.Integer, uk.ac.ebi.arrayexpress.utils.saxon.search.QueryInfo,
 	 * uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap)
 	 */
 	public String queryPaged(Integer queryId, QueryInfo info,
 			HttpServletRequestParameterMap map) throws IOException {
 		// IndexReader ir = null;
 
 		IndexSearcher isearcher = null;
 		if (logger.isDebugEnabled()) {
 			logger.debug("start of queryPaged");
 		}
 		StringBuilder totalRes = new StringBuilder();
 		totalRes.append("<content>");
 		Query query = info.getQuery();
 		try {
 			ir = getIndexReader();
 			if (query instanceof BooleanQuery
 					&& ((BooleanQuery) query).clauses().isEmpty()) {
 				logger.info("Empty search, returned all [{}] documents",
 						getCountDocuments());
 				// this is much more faster
 				query = new MatchAllDocsQuery();
 			}
 
 			isearcher = new IndexSearcher(ir);
 			boolean descending = getDefaultSortDescending();
 			;
 			String sortBy = StringTools.arrayToString(map.get("sortby"), " ");
 			if (sortBy == null || sortBy.equalsIgnoreCase("")) {
 				sortBy = getDefaultSortField();
 			}
 			String sortOrder = StringTools.arrayToString(map.get("sortorder"),
 					" ");
 
 			if (sortOrder != null) {
 				if (sortOrder.equalsIgnoreCase("ascending")) {
 					descending = false;
 				} else {
 					descending = true;
 				}
 			}
 
 			// I have to test the sort field name. If it is a string i have to
 			// add "sort" to the name
 			// I will only sort if I have a Field
 			// TopDocs hits;
 			ScoreDoc[] hits = null;
 			Sort sort = null;
 			if (doesFieldExist(sortBy)) {
 				FieldInfo sortField = fields.get(sortBy);
 				if (sortField == null) {
 					logger.info(
 							"A sort field is trying to be used but that field is not defined! ->[{}]",
 							sortBy);
 				}
 
 				int sortFieldsSize = sortField.sortFields != null ? sortField.sortFields
 						.size() : 0;
 				SortField[] sortFieldArray = new SortField[sortFieldsSize];
 				if (sortFieldsSize > 0) {
 					StringBuilder sb = new StringBuilder();
 					for (int i = 0; i < sortField.sortFields.size(); i++) {
 						FieldInfo otherSortField = fields
 								.get(sortField.sortFields.get(i));
 
 						if (otherSortField == null) {
 							logger.info(
 									"Other sort field is trying to be used but that field is not defined! ->[{}]",
 									sortField.sortFields.get(i));
 						} else {
 							String sortByName = otherSortField.name;
 							int descendingType = SortField.STRING_VAL;
 							sb.append("new sortField ->")
 									.append(otherSortField.name).append("; ");
 							if (otherSortField.name.equalsIgnoreCase(sortBy)
 									&& otherSortField.type
 											.equalsIgnoreCase("string")) {
 								sortByName += "sort";
 							} else {
 								if (otherSortField.type
 										.equalsIgnoreCase("integer")) {
 									descendingType = SortField.LONG;
 								}
 							}
 
 							sortFieldArray[i] = new SortField(sortByName,
 									descendingType, descending);
 
 						}
 					}
 					logger.debug("Query sorted by: ->[{}] descending: ->[{}]",
 							sb.toString(), descending);
 				}
 
 				sort = new Sort(sortFieldArray);
 			}
 			// The fiels doenst exist but it can be one of the dynamic, so I
 			// have to add it
 			else {
 				SortField[] sortFieldArray = new SortField[1];
 				sortBy+= "sort";
 				sortFieldArray[0] = new SortField(sortBy, SortField.STRING_VAL,
 						descending);
 				logger.debug("Query sorted by a dynamic sample attribute: ->[{}] descending: ->[{}]",
 						sortBy, descending);
 				sort = new Sort(sortFieldArray);
 				// logger.info(
 				// "Sort query field [{}] doenst exist or the SortBy parameter was not specified",
 				// sortBy);
 			}
 
 			int pageSize = defaultPageSize;
 			if (map.containsKey("pagesize")) {
 				pageSize = Integer.parseInt(StringTools.arrayToString(
 						map.get("pagesize"), " "));
 			} else {
 				pageSize = getDefaultPageSize();
 				map.put("pagesize", Integer.toString(pageSize));
 			}
 
 			int page = 0;
 			if (map.containsKey("page")) {
 				page = Integer.parseInt(StringTools.arrayToString(
 						map.get("page"), " ")) - 1;
 			}
 
 			int initialExp = page * pageSize;
 			int finalExp = initialExp + pageSize;
 
 			// I will execute the same query with or without Sortby parameter
 			// (in the last case the sort will be null)
 			// /TopFieldCollector collectorAux = null;
 			TopFieldCollector collector = null;
 			int numHits = getCountDocuments() + 1;
 			collector = TopFieldCollector.create(sort == null ? new Sort()
 					: sort,
 			// / collectorAux = TopFieldCollector.create(sort == null ? new
 			// Sort()
 			// / : sort,
 			// TODO: rpe If im returning page 3 using pagesize of 50 i need to
 			// sort (3*50)
 					(page == 0 ? 1 : page + 1) * pageSize, false, // fillFields
 																	// - not
 																	// needed,
 																	// we want
 																	// score and
 																	// doc only
 					false, // trackDocScores - need doc and score fields
 					false, // trackMaxScore - related to trackDocScores
 					sort == null); // should docs be in docId order?
 
 			// /TopFieldCollectorReference collector= new
 			// TopFieldCollectorReference(collectorAux);
 
 			// facets:
 			// Directory taxDir = FSDirectory.open(new
 			// File("/Users/rpslpereira/Apps/apache-tomcat-6.0.33/temp/Setup/LuceneIndexesFacets/biosamplesgroup"));
 			// DirectoryTaxonomyReader taxor = new
 			// DirectoryTaxonomyReader(taxDir);
 			// FacetSearchParams fsp = new FacetSearchParams();
 			// fsp.addFacetRequest(new CountFacetRequest(new
 			// CategoryPath("samples"), 10));
 			// FacetsCollector facetsCollector = new FacetsCollector(fsp, ir,
 			// taxor);
 			// isearcher.search(new MatchAllDocsQuery(), facetsCollector);
 			// for (FacetResult fres : facetsCollector.getFacetResults()) {
 			// FacetResultNode root = fres.getFacetResultNode();
 			// System.out.println(root.getLabel() + " (" + root.getValue() +
 			// ")");
 			// for (FacetResultNode cat : root.getSubResults()) {
 			// System.out.println("  " + cat.getLabel().getComponent(1)
 			// + " (" + cat.getValue() + ")");
 			// }
 			// }
 
 			isearcher.search(query, collector);
 			// I will use this Collector to know how much results do i have
 			long timeHits = System.nanoTime();
 			TotalHitCountCollector collector2 = new TotalHitCountCollector();
 			// /TotalHitCountCollectorReference collector2 = new
 			// TotalHitCountCollectorReference();
 			isearcher.search(query, collector2);
 			double ms = (System.nanoTime() - timeHits) / 1000000d;
 			logger.info("Number of Docs TotalHitCountCollector->"
 					+ collector2.getTotalHits() + "- TOTALHITS TOOK->" + ms);
 			int totalHits = collector2.getTotalHits();
 
 			TopDocs topDocs = collector.topDocs();
 			// hits= topDocs.scoreDocs;
 			hits = topDocs.scoreDocs;
 
 			logger.info("Search of index [" + this.indexId
 					+ "] with query [{}] returned [{}] hits", query.toString(),
 					hits.length);
 
 			logger.info("Beginning of paging logic");
 
 			if (finalExp > hits.length) {
 				finalExp = hits.length;
 			}
 
 			List<String> combinedTotal = new ArrayList<String>();
 			combinedTotal.add(String.valueOf(totalHits));
 
 			map.put("total",
 					combinedTotal.toArray(new String[combinedTotal.size()]));
 
 			logger.info(
 					"End of paging logic, requesting data from [{}] to [{}]",
 					initialExp, finalExp);
 			long time = System.nanoTime();
 			if (logger.isDebugEnabled()) {
 				logger.debug("Requesting data from xml database");
 			}
 			// this QueryDB should be implemented by all subclasses and is
 			// responsible for the data collecting
 			totalRes.append(queryDB(hits, isearcher, initialExp, finalExp, map));
 			if (logger.isDebugEnabled()) {
 				logger.debug("End of requesting data from xml database");
 			}
 
 			isearcher.close();
 			// /ir.close();
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 		} finally {
 			if (null != isearcher)
 				isearcher.close();
 			// if (null != ir)
 			// ir.close();
 		}
 
 		totalRes.append("</content>");
 		if (logger.isDebugEnabled()) {
 			logger.debug("End of QueryPaged");
 		}
 		return totalRes.toString();
 	}
 
 	/**
 	 * @param hits
 	 *            this just represents a subset of the result
 	 * @param TotalHits
 	 * @param isearcher
 	 * @param initialExp
 	 * @param finalExp
 	 * @param map
 	 * @return
 	 * @throws Exception
 	 */
 	abstract public String queryDB(ScoreDoc[] hits, IndexSearcher isearcher,
 			int initialExp, int finalExp, HttpServletRequestParameterMap map)
 			throws Exception;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * uk.ac.ebi.arrayexpress.utils.saxon.search.IIndexEnvironment#queryAllDocs
 	 * (java.lang.Integer, uk.ac.ebi.arrayexpress.utils.saxon.search.QueryInfo,
 	 * uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap)
 	 */
 	public ScoreDoc[] queryAllDocs(Integer queryId, QueryInfo info,
 			HttpServletRequestParameterMap map) throws IOException {
 		IndexReader ir = null;
 		IndexSearcher isearcher = null;
 		Query query = info.getQuery();
 		ScoreDoc[] hits = null;
 		try {
 			ir = IndexReader.open(this.indexDirectory, true);
 
 			// empty query returns everything
 			if (query instanceof BooleanQuery
 					&& ((BooleanQuery) query).clauses().isEmpty()) {
 				logger.info(
 						"queryAllDocs Empty search, returned all [{}] documents",
 						getCountDocuments());
 				// I need to continue because e i need to sort the data, so I
 				// will create an empty query (this happens when I'm a curator
 				// and I dont have any search criteria)
 				// Term t = new Term(defaultField, "*");
 				// ((BooleanQuery) query).add(new BooleanClause(new
 				// WildcardQuery(
 				// t), BooleanClause.Occur.SHOULD));
 
 				// this is much more faster
 				query = new MatchAllDocsQuery();
 			}
 
 			// to show _all_ available nodes
 			isearcher = new IndexSearcher(ir);
 			// +1 is a trick to prevent from having an exception thrown if
 			// documentNodes.size() value is 0
 			boolean descending = true;
 
 			String sortBy = StringTools.arrayToString(map.get("sortby"), " ");
 			if (sortBy != null && sortBy.equalsIgnoreCase("")) {
 				sortBy = getDefaultSortField();
 			}
 			String sortOrder = StringTools.arrayToString(map.get("sortorder"),
 					" ");
 
 			if (sortOrder != null) {
 				if (sortOrder.equalsIgnoreCase("ascending")) {
 					descending = false;
 				} else {
 					descending = true;
 				}
 			}
 
 			int sortFieldType = SortField.INT;
 			// I have to test the sort field name. If it is a string i have to
 			// add "sort" to the name
 			// I will only sort if I have a Field
 			Sort sort = null;
 			if (!sortBy.equalsIgnoreCase("") && doesFieldExist(sortBy)) {
 				FieldInfo sortField = fields.get(sortBy);
 				if (sortField == null) {
 					logger.info(
 							"A sort field is trying to be used but that field is not defined! ->[{}]",
 							sortBy);
 				}
 
 				int sortFieldsSize = sortField.sortFields != null ? sortField.sortFields
 						.size() : 0;
 				SortField[] sortFieldArray = new SortField[sortFieldsSize];
 				// sortFieldArray[0]=new SortField(sortBy, sortFieldType,
 				// descending);
 				if (sortFieldsSize > 0) {
 					StringBuilder sb = new StringBuilder();
 					for (int i = 0; i < sortField.sortFields.size(); i++) {
 						FieldInfo otherSortField = fields
 								.get(sortField.sortFields.get(i));
 
 						if (otherSortField == null) {
 							logger.info(
 									"Other sort field is trying to be used but that field is not defined! ->[{}]",
 									sortField.sortFields.get(i));
 						} else {
 							String sortByName = otherSortField.name;
 							int descendingType = SortField.STRING_VAL;
 							sb.append("new sortField ->")
 									.append(otherSortField.name).append("; ");
 							if (otherSortField.name.equalsIgnoreCase(sortBy)
 									&& otherSortField.type
 											.equalsIgnoreCase("string")) {
 								sortByName += "sort";
 							} else {
 								if (otherSortField.type
 										.equalsIgnoreCase("integer")) {
 									descendingType = SortField.INT;
 								}
 							}
 
 							sortFieldArray[i] = new SortField(sortByName,
 									descendingType, descending);
 
 						}
 					}
 					logger.info("Query sorted by: ->[{}]", sb.toString());
 				}
 
 				sort = new Sort(sortFieldArray);
 
 				// hits = isearcher.search(query, getCountDocuments() + 1,
 				// sort);
 			} else {
 				// hits = isearcher.search(query, getCountDocuments() + 1);
 				logger.info(
 						"Sort query field [{}] doenst exist or the SortBy parameter was not specified",
 						sortBy);
 			}
 
 			// I will execute the same query with or without Sortby parameter
 			// (in the last case the sort will be null)
 			int numHits = getCountDocuments() + 1;
 			TopFieldCollector collector = TopFieldCollector.create(
 					sort == null ? new Sort() : sort, numHits, false, // fillFields
 																		// - not
 																		// needed,
 																		// we
 																		// want
 																		// score
 																		// and
 																		// doc
 																		// only
 					false, // trackDocScores - need doc and score fields
 					false, // trackMaxScore - related to trackDocScores
 					sort == null); // should docs be in docId order?
 			isearcher.search(query, collector);
 			TopDocs topDocs = collector.topDocs();
 			// hits= topDocs.scoreDocs;
 			hits = topDocs.scoreDocs;
 
 			map.put("total", Integer.toString(hits.length));
 
 			isearcher.close();
 			ir.close();
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 		} finally {
 			if (null != isearcher)
 				isearcher.close();
 			if (null != ir)
 				ir.close();
 		}
 
 		return hits;
 
 	}
 
 	// TODO RPE
 	public void indexReader() {
 		// IndexReader ir = null;
 		try {
 			logger.info("Reload the Lucene Index for [{}]", indexId);
 			ir = IndexReader.open(this.indexDirectory, true);
 
 			Map<String, String> map = ir.getCommitUserData();
 			logger.info("numberDocs->" + map.get("numberDocs"));
 			logger.info("date->" + map.get("date"));
 			logger.info("keyValidator->" + map.get("keyValidator"));
 			this.setCountDocuments(Integer.parseInt(map.get("numberDocs")));
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 		} finally {
 			try {
 				if (null != ir) {
 					ir.close();
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				logger.error("Caught an exception:", e);
 			}
 		}
 	}
 
 	public void setup() {
 		// TODO Auto-generated method stub
 		closeIndexReader();
 		getIndexReader(); // I need to do this, because the setup method is
 							// called when a full reload occurs and we need to
 							// open it again
 		logger.info("default setup for Index Environment");
 
 	}
 
 	public String getMetadataInformation() {
 
 		String ret = "<table>";
 		Map<String, String> map = getIndexReader().getCommitUserData();
 		for (String key : map.keySet()) {
 			ret += "<tr><td valign='top'><u>" + key + "</u></td><td>"
 					+ map.get(key) + "</td></tr>";
 		}
 		ret += "</table>";
 		return ret;
 	}
 
 	// no parameters menas that i will do all the work in the default database
 	// (the one that is configured)
 	public void indexFromXmlDB() throws Exception {
 
 		// String indexLocationDirectory = "";
 		String dbHost = "";
 		String dbPassword = "";
 		String dbName = "";
 		int dbPort = 0;
 
 		// get the default location
 		// indexLocationDirectory = this.indexLocationDirectory;
 		HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
 				.getInstance().getPreferences().getConfSubset("bs.xmldatabase");
 
 		if (null != connsConf) {
 			// connectionString = connsConf.getString("connectionstring");
 			dbHost = connsConf.getString("host");
 			dbPort = Integer.parseInt(connsConf.getString("port"));
 			dbName = connsConf.getString("dbname");
 			dbPassword = connsConf.getString("adminpassword");
 		} else {
 			logger.error("bs.xmldatabase Configuration is missing!!");
 		}
 
 		indexFromXmlDB(indexLocationDirectory, dbHost, dbPort, dbPassword,
 				dbName);
 
 	}
 
 	// TODO: I'm assuming that there is always an attribute @id in each element
 	public void indexFromXmlDB(String indexLocationDirectory, String dbHost,
 			int dbPort, String dbPassword, String dbName) throws Exception {
 		int countNodes = 0;
 		String driverXml = "";
 		String connectionString = "";
 		Collection coll;
 		IndexWriter w = null;
		DirectoryTaxonomyWriter taxoWriter=null;
 		Map<String, XPathExpression> fieldXpe = new HashMap<String, XPathExpression>();
 		try {
 
 			Directory indexTempDirectory = FSDirectory.open(new File(
 					indexLocationDirectory, indexId));
 			logger.debug("Index directory->" + indexLocationDirectory);
 			w = createIndex(indexTempDirectory, indexAnalyzer);
 
 			Directory taxDir = FSDirectory.open(new File(indexLocationDirectory
 					+ "Facets", indexId));
 
 			taxoWriter = new DirectoryTaxonomyWriter(
 					taxDir);
 			CategoryDocumentBuilder docBuilder = new CategoryDocumentBuilder(
 					taxoWriter);
 
 			HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
 					.getInstance().getPreferences()
 					.getConfSubset("bs.xmldatabase");
 
 			if (null != connsConf) {
 				// TODO: rpe use the component XmlDatabasePooling
 				driverXml = connsConf.getString("driver");
 				// I will use the connectionString that was passed by parameter
 				// (in several parameters)
 				connectionString = connsConf.getString("base") + "://" + dbHost
 						+ ":" + dbPort + "/" + dbName;
 			} else {
 				logger.error("bs.xmldatabase Configuration is missing!!");
 			}
 
 			// I cannot register this database again (this is already registered
 			// on XmlDbConnectionPool Component -
 			// java.nio.channels.OverlappingFileLockException
 			// c = Class.forName(driverXml);
 			// db = (Database) c.newInstance();
 			// DatabaseManager.registerDatabase(db);
 			logger.debug("connectionString->" + connectionString);
 			coll = DatabaseManager.getCollection(connectionString);
 			XPathQueryService service = (XPathQueryService) coll.getService(
 					"XPathQueryService", "1.0");
 
 			DocumentInfo source = null;
 			// Loop through all result items
 
 			// collect all the fields data
 			Configuration config = ((SaxonEngine) Application
 					.getAppComponent("SaxonEngine")).trFactory
 					.getConfiguration();
 
 			XPath xp = new XPathEvaluator(config);
 			// XPathExpression xpe = xp.compile(this.env.indexDocumentPath);
 
 			for (FieldInfo field : fields.values()) {
 				fieldXpe.put(field.name, xp.compile(field.path));
 				logger.debug("Field Path->[{}]", field.path);
 			}
 
 			// the xmldatabase is not very correct and have memory problem for
 			// queires with huge results, so its necessary to implement our own
 			// iteration mechanism
 			//
 			// // I will collect all the results
 			// ResourceSet set = service.query(this.env.indexDocumentPath);
 			// //TODO rpe
 			// //ResourceSet set = service.query("//Sample");
 			// logger.debug("Number of results->" + set.getSize());
 			// long numberResults = set.getSize();
 			long numberResults = 0;
 			ResourceSet set = service.query("count(" + indexDocumentPath + ")");
 			if (set.getIterator().hasMoreResources()) {
 				numberResults = Integer.parseInt((String) set.getIterator()
 						.nextResource().getContent());
 			}
 			logger.debug("Number of results->" + numberResults);
 			long pageSizeDefault = 50000;
 			// the samplegroup cannot be big otherwise I will obtain a memory
 			// error ... but the sample must b at least one million because the
 			// paging queries are really slow - we need to balance it
 			// (for samples 1million, for samplegroup 50000)
 			if (numberResults > 1000000) {
 				pageSizeDefault = 1000000;
 			}
 
 			long pageNumber = 1;
 			int count = 0;
 			// Map<String, AttsInfo[]> cacheAtt = new HashMap<String,
 			// AttsInfo[]>();
 			// Map<String, XPathExpression> cacheXpathAtt = new HashMap<String,
 			// XPathExpression>();
 			// Map<String, XPathExpression> cacheXpathAttValue = new
 			// HashMap<String, XPathExpression>();
 			while ((pageNumber * pageSizeDefault) <= (numberResults
 					+ pageSizeDefault - 1)) {
 				// while ((pageNumber<=1)) {
 				// calculate the last hit
 				long pageInit = (pageNumber - 1) * pageSizeDefault + 1;
 				long pageSize = (pageNumber * pageSizeDefault < numberResults) ? pageSizeDefault
 						: (numberResults - pageInit + 1);
 
 				service = (XPathQueryService) coll.getService(
 						"XPathQueryService", "1.0");
 
 				// xquery paging using subsequence function
 				long time = System.nanoTime();
 
 				// /set =
 				// service.query("for $x in(/Biosamples/SampleGroup/Sample/@id) return string($x)");
 				// I'm getting everything based on nodeId, because i have the
 				// sample sample in different samplegroups
 				// TODO: change this (just works with baseX)
 				set = service.query("for $x in(subsequence("
 						+ indexDocumentPath + "," + pageInit + "," + pageSize
 						+ ")) return db:node-id($x)");
 
 				// logger.debug("Number of results of page->" + set.getSize());
 				double ms = (System.nanoTime() - time) / 1000000d;
 				logger.info("Query XMLDB took ->[{}]", ms);
 
 				ResourceIterator iter = set.getIterator();
 				XPath xp2;
 				XPathExpression xpe2;
 				List documentNodes;
 				StringReader reader;
 				// cache of distinct attributes fora each sample group
 
 				while (iter.hasMoreResources()) {
 					count++;
 					logger.debug("its beeing processed the number ->" + count);
 					String idNode = (String) iter.nextResource().getContent();
 					//logger.debug("Id node->" + idNode);
 					// I need to get the sample
 					// ResourceSet setid = service.query(indexDocumentPath
 					// + "[@id='" + idSample + "']");
 					ResourceSet setid = service.query("db:open-id('" + dbName
 							+ "'," + idNode + ")");
 					ResourceIterator iterid = setid.getIterator();
 					List<CategoryPath> sampleCategories = null;
 					while (iterid.hasMoreResources()) {
 						// System.out.println("");
 						// /xml=(String) iterid.nextResource().getContent();
 
 						// /xml=(String) iter.nextResource().getContent();
 
 						// /reader = new StringReader(xml);
 						StringBuilder xml = new StringBuilder();
 						xml.append((String) iterid.nextResource().getContent());
 						// logger.debug("xml->"+xml);
 						// logger.debug(xml.toString());
 						reader = new StringReader(xml.toString());
 						source = config.buildDocument(new StreamSource(reader));
 
 						// logger.debug("XML DB->[{}]",
 						// PrintUtils.printNodeInfo((NodeInfo) source, config));
 						Document d = new Document();
 
 						xp2 = new XPathEvaluator(source.getConfiguration());
 
 						int position = indexDocumentPath.lastIndexOf("/");
 						;
 						String pathRoot = "";
 						if (position != -1) {
 							pathRoot = indexDocumentPath.substring(position);
 						} else {
 							pathRoot = indexDocumentPath;
 						}
 						// logger.debug("PathRoot->[{}]",pathRoot);
 						xpe2 = xp2.compile(pathRoot);
 						// TODO rpe
 						// xpe2 = xp2.compile("/Sample");
 						documentNodes = (List) xpe2.evaluate(source,
 								XPathConstants.NODESET);
 
 						for (Object node : documentNodes) {
 
 							try {
 								d = processEntryIndex(node, config, service,
 										fieldXpe);
 							} catch (Exception x) {
 								String xmlError = PrintUtils.printNodeInfo(
 										(NodeInfo) node, config);
 								logger.error(
 										"XML that was being processed when the error occurred DB->[{}]",
 										xmlError);
 								// to avoid the next running to stop
 								// because its not able to delete the
 								// newSetup directory
 								w.close();
 								throw new Exception(
 										"Xml that is being processed:"
 												+ xmlError, x);
 							}
 						}
 
 						documentNodes = null;
 						source = null;
 						reader = null;
 						xml = null;
 						countNodes++;
 						// logger.debug("count->[{}]", countNodes);
 
 						// facet tests
 
 						docBuilder.setCategoryPaths(sampleCategories);
 						docBuilder.build(d);
 
 						addIndexDocument(w, d);
 
 					}
 				}
  
 				logger.debug("until now it were processed->[{}]", pageNumber
 						* pageSizeDefault);
 				pageNumber++;
 				if (coll != null) {
 					try {
 						// coll.close();
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				set = null;
 
 			}
 
 			setCountDocuments(countNodes);
 			// add metadata to the lucene index
 			Map<String, String> map = new HashMap<String, String>();
 			map.put("numberDocs", Integer.toString(countNodes));
 			map.put("date", Long.toString(System.nanoTime()));
 			// logger.debug(Application.getInstance().getComponent("XmlDbConnectionPool").getMetaDataInformation());
 			// I cannot call directly
 			// getComponent("XmlDbConnectionPool").getMetaDataInformation(),
 			// because I can be working in a did
 			String dbInfo = ((XmlDbConnectionPool) Application.getInstance()
 					.getComponent("XmlDbConnectionPool")).getDBInfo(dbHost,
 					dbPort, dbPassword, dbName);
 
 			map.put("DBInfo", dbInfo);
 			// facet
 			taxoWriter.commit();
 			taxoWriter.close();
 			commitIndex(w, map);
 
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 			taxoWriter.close();
 			w.close();
 			throw x;
 		}
 	}
 
 	public void indexIncrementalFromXmlDB() throws Exception {
 
 		String indexLocationDirectory = "";
 		String dbHost = "";
 		String dbPassword = "";
 		String dbName = "";
 		int dbPort = 0;
 
 		// get the default location
 		indexLocationDirectory = indexLocationDirectory;
 		HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
 				.getInstance().getPreferences().getConfSubset("bs.xmldatabase");
 
 		if (null != connsConf) {
 			// connectionString = connsConf.getString("connectionstring");
 			dbHost = connsConf.getString("host");
 			dbPort = Integer.parseInt(connsConf.getString("port"));
 			dbName = connsConf.getString("dbname");
 			dbPassword = connsConf.getString("adminpassword");
 		} else {
 			logger.error("bs.xmldatabase Configuration is missing!!");
 		}
 
 		indexIncrementalFromXmlDB(indexLocationDirectory, dbHost, dbPort,
 				dbPassword, dbName);
 
 	}
 
 	// TODO: I'm assuming that there is always an attribute @id in each element
 	public void indexIncrementalFromXmlDB(String indexLocationDirectory,
 			String dbHost, int dbPort, String dbPassword, String dbName)
 			throws Exception {
 		// I'm upgrading so the baseline is the current nodes number
 		int countNodes = getCountDocuments();
 		String driverXml = "";
 		String connectionString = "";
 		Collection coll;
 		IndexWriter w = null;
 		Map<String, XPathExpression> fieldXpe = new HashMap<String, XPathExpression>();
 		logger.info(
 				"indexIncrementalFromXmlDB(generic) is starting for [{}], and initially I have[{}] ... ",
 				new Object[] { indexId, countNodes });
 		try {
 
 			Directory indexTempDirectory = FSDirectory.open(new File(
 					indexLocationDirectory, indexId));
 			w = openIndex(indexTempDirectory, indexAnalyzer);
 			HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
 					.getInstance().getPreferences()
 					.getConfSubset("bs.xmldatabase");
 			if (null != connsConf) {
 				driverXml = connsConf.getString("driver");
 				connectionString = connsConf.getString("base") + "://" + dbHost
 						+ ":" + dbPort + "/" + dbName;
 			} else {
 				logger.error("bs.xmldatabase Configuration is missing!!");
 			}
 			logger.debug("connectionString->" + connectionString);
 			coll = DatabaseManager.getCollection(connectionString);
 			XPathQueryService service = (XPathQueryService) coll.getService(
 					"XPathQueryService", "1.0");
 
 			DocumentInfo source = null;
 			Configuration config = ((SaxonEngine) Application
 					.getAppComponent("SaxonEngine")).trFactory
 					.getConfiguration();
 			XPath xp = new XPathEvaluator(config);
 			for (FieldInfo field : fields.values()) {
 				fieldXpe.put(field.name, xp.compile(field.path));
 				logger.debug("Field Path->[{}]", field.path);
 			}
 
 			// the xmldatabase is not very correct and have memory problem for
 			// queires with huge results, so its necessary to implement our own
 			// iteration mechanism
 			//
 			// // I will collect all the results
 			// ResourceSet set = service.query(this.env.indexDocumentPath);
 			long numberResults = 0;
 			ResourceSet set = service.query("count(" + indexDocumentPath + ")");
 			if (set.getIterator().hasMoreResources()) {
 				numberResults = Integer.parseInt((String) set.getIterator()
 						.nextResource().getContent());
 			}
 
 			// TODO:######################################Change this after -
 			// this is just a performance test
 			// float percentage=0.1F;
 			// numberResults=Math.round(numberResults * percentage);
 
 			logger.debug("Number of results->" + numberResults);
 			long pageSizeDefault = 50000;
 			if (numberResults > 1000000) {
 				pageSizeDefault = 1000000;
 			}
 
 			long pageNumber = 1;
 			int count = 0;
 			Map<String, AttsInfo[]> cacheAtt = new HashMap<String, AttsInfo[]>();
 			Map<String, XPathExpression> cacheXpathAtt = new HashMap<String, XPathExpression>();
 			Map<String, XPathExpression> cacheXpathAttValue = new HashMap<String, XPathExpression>();
 			while ((pageNumber * pageSizeDefault) <= (numberResults
 					+ pageSizeDefault - 1)) {
 				// calculate the last hit
 				long pageInit = (pageNumber - 1) * pageSizeDefault + 1;
 				long pageSize = (pageNumber * pageSizeDefault < numberResults) ? pageSizeDefault
 						: (numberResults - pageInit + 1);
 
 				service = (XPathQueryService) coll.getService(
 						"XPathQueryService", "1.0");
 
 				// xquery paging using subsequence function
 				long time = System.nanoTime();
 				// TODO: I'm assuming that there is always an attribute @id in
 				// each element
 				set = service.query("for $x in(subsequence("
 						+ indexDocumentPath + "/@id," + pageInit + ","
 						+ pageSize + ")) return string($x)");
 				double ms = (System.nanoTime() - time) / 1000000d;
 				logger.info("Query XMLDB took ->[{}]", ms);
 
 				ResourceIterator iter = set.getIterator();
 				XPath xp2;
 				XPathExpression xpe2;
 				List documentNodes;
 				StringReader reader;
 				// cache of distinct attributes fora each sample group
 
 				while (iter.hasMoreResources()) {
 					count++;
 					logger.debug("its beeing processed the number ->" + count);
 					String idToProcess = (String) iter.nextResource()
 							.getContent();
 
 					logger.debug("@id that is being processed->" + idToProcess);
 					// I need to get the sample
 					ResourceSet setid = service.query(indexDocumentPath
 							+ "[@id='" + idToProcess + "']");
 					ResourceIterator iterid = setid.getIterator();
 					while (iterid.hasMoreResources()) {
 						StringBuilder xml = new StringBuilder();
 						xml.append((String) iterid.nextResource().getContent());
 						// logger.debug(xml.toString());
 						reader = new StringReader(xml.toString());
 						source = config.buildDocument(new StreamSource(reader));
 
 						// logger.debug("XML DB->[{}]",
 						// PrintUtils.printNodeInfo((NodeInfo) source, config));
 						Document d = new Document();
 
 						xp2 = new XPathEvaluator(source.getConfiguration());
 
 						int position = indexDocumentPath.lastIndexOf("/");
 						// TODO: I also need to change this
 						String pathRoot = "";
 						if (position != -1) {
 							pathRoot = indexDocumentPath.substring(position);
 						} else {
 							pathRoot = indexDocumentPath;
 						}
 						// logger.debug("PathRoot->[{}]",pathRoot);
 						xpe2 = xp2.compile(pathRoot);
 						documentNodes = (List) xpe2.evaluate(source,
 								XPathConstants.NODESET);
 
 						for (Object node : documentNodes) {
 							// logger.debug("XML DB->[{}]",PrintUtils.printNodeInfo((NodeInfo)node,config));
 
 							String idElement = (String) fieldXpe.get("id")
 									.evaluate(node, XPathConstants.STRING);
 							// I need to see if it already exists
 							// I will also add this document if it is nor marked
 							// as "todelete"
 							Boolean toDelete = (Boolean) fieldXpe.get("delete")
 									.evaluate(node, XPathConstants.BOOLEAN);
 
 							// TODO:######################################Change
 							// this after - this is just a performance test
 							int deletePercentage = 10;
 							toDelete = (count % deletePercentage) == 0 ? true
 									: false;
 
 							logger.debug(
 									"Incremental Update - The document [{}] is being processed and is marked to delete?[{}]",
 									new Object[] { idElement, toDelete });
 							// I will always try to delete the document (i don't
 							// know if it is new or if it was changed)
 							Term idTerm = new Term("id",
 									idElement.toLowerCase());
 							int countToDelete = getIndexReader()
 									.docFreq(idTerm);
 							if (countToDelete > 0) {
 								// if has more than one, I have to send an email
 								// to warn
 								if (countToDelete > 1) {
 									Application
 											.getInstance()
 											.sendEmail(
 													null,
 													null,
 													"BIOSAMPLES ERROR - Incremental Update - Removing more than one document! id-> "
 															+ idElement,
 													" documents found:"
 															+ countToDelete);
 									// I will launch an exception
 									throw new Exception(
 											"BIOSAMPLES ERROR - Incremental Update -  Removing more than one document in incremental update id-> "
 													+ idElement
 													+ " documents found:"
 													+ countToDelete);
 								}
 								logger.debug(
 										"The document with id [{}] is being deleted from Lucene",
 										idElement);
 								w.deleteDocuments(idTerm);
 								// need to remove one from the number of
 								// documents count
 								countNodes--;
 
 							}
 							// the element doesn't exist on GUI
 							else {
 								// if it is marked to delete I will just an
 								// warning email - it's possible that it was
 								// inserted and deleted on the Backend but it
 								// had never been sent to the GUI before
 								if (toDelete) {
 									Application
 											.getInstance()
 											.sendEmail(
 													null,
 													null,
 													"BIOSAMPLES WARNING - Incremental Update - Id marked for deletion but the id doesn't exist on the GUI! id-> "
 															+ idElement, "");
 
 								}
 							}
 
 							// if (toDelete) {
 							// logger.debug(
 							// "The document with id [{}] was marked to deletion so I will not process it",
 							// idElement);
 							// } else {
 
 							// I just process it is it is not for deletion)
 							if (!toDelete) {
 								try {
 									d = processEntryIndex(node, config,
 											service, fieldXpe);
 
 								} catch (Exception x) {
 									String xmlError = PrintUtils.printNodeInfo(
 											(NodeInfo) node, config);
 									logger.error(
 											"XML that was being processed when the error occurred DB->[{}]",
 											xmlError);
 									// to avoid the next running to stop
 									// because its not able to delete the
 									// newSetup directory
 									w.close();
 									throw new Exception(
 											"Xml that is being processed:"
 													+ xmlError, x);
 								}
 								countNodes++;
 								addIndexDocument(w, d);
 							}
 						}
 
 						// }
 
 						documentNodes = null;
 						source = null;
 						reader = null;
 						xml = null;
 						// logger.debug("count->[{}]", countNodes);
 
 					}
 				}
 				logger.debug("until now it were processed->[{}]", pageNumber
 						* pageSizeDefault);
 				pageNumber++;
 //				if (coll != null) {
 //					try {
 //						// coll.close();
 //					} catch (Exception e) {
 //						// TODO Auto-generated catch block
 //						e.printStackTrace();
 //					}
 //				}
 				set = null;
 
 			}
 
 			setCountDocuments(countNodes);
 			// add metadata to the lucene index
 			Map<String, String> map = new HashMap<String, String>();
 			map.put("numberDocs", Integer.toString(countNodes));
 			map.put("date", Long.toString(System.nanoTime()));
 			// logger.debug(Application.getInstance().getComponent("XmlDbConnectionPool").getMetaDataInformation());
 			// I cannot call directly
 			// getComponent("XmlDbConnectionPool").getMetaDataInformation(),
 			// because I can be working in a did
 			String dbInfo = ((XmlDbConnectionPool) Application.getInstance()
 					.getComponent("XmlDbConnectionPool")).getDBInfo(dbHost,
 					dbPort, dbPassword, dbName);
 
 			// TODO: I need to put here what I have before - to track all the
 			// changes (old numberDocs + old date + oldDBInfo)
 			map.put("DBInfo",
 					dbInfo
 							+ "<BR>##################################################<BR>"
 							+ getMetadataInformation());
 			commitIndex(w, map);
 
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 			w.close();
 			throw x;
 		}
 	}
 
 	IndexWriter createIndex(Directory indexDirectory, Analyzer analyzer)
 			throws Exception {
 		IndexWriter iwriter = null;
 		try {
 			iwriter = new IndexWriter(indexDirectory, analyzer, true,
 					IndexWriter.MaxFieldLength.UNLIMITED);
 			// TODO: just to check if it solves the slowly indexing indexes with
 			// more
 			iwriter.setMaxBufferedDocs(500000);
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 			throw x;
 		}
 
 		return iwriter;
 	}
 
 	IndexWriter openIndex(Directory indexDirectory, Analyzer analyzer) {
 		IndexWriter iwriter = null;
 		try {
 			iwriter = new IndexWriter(indexDirectory, analyzer, false,
 					IndexWriter.MaxFieldLength.UNLIMITED);
 			// TODO: just to check if it solves the slowly indexing indexes with
 			// more
 			iwriter.setMaxBufferedDocs(500000);
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 		}
 
 		return iwriter;
 	}
 
 	void addIndexField(Document document, String name, Object value,
 			boolean shouldAnalyze, boolean shouldStore, boolean sort) {
 		String stringValue;
 		if (value instanceof String) {
 			stringValue = (String) value;
 		} else if (value instanceof NodeInfo) {
 			stringValue = ((NodeInfo) value).getStringValue();
 		} else {
 			stringValue = value.toString();
 			logger.warn(
 					"Not sure if I handle string value of [{}] for the field [{}] correctly, relying on Object.toString()",
 					value.getClass().getName(), name);
 		}
 		// TODO
 		// logger.debug("value->[{}]", stringValue);
 		document.add(new Field(name, stringValue, shouldStore ? Field.Store.YES
 				: Field.Store.NO, shouldAnalyze ? Field.Index.ANALYZED
 				: Field.Index.NOT_ANALYZED));
 		// ig Im indexing a String and the @sort=true I will always create a new
 		// field (fieldname+sort)
 		if (sort) {
 			String newF = name + "sort";
 			document.add(new Field(newF, stringValue, Field.Store.NO,
 					Field.Index.NOT_ANALYZED));
 		}
 
 	}
 
 	void addBooleanIndexField(Document document, String name, Object value,
 			boolean sort) {
 		Boolean boolValue = null;
 		if (value instanceof Boolean) {
 			boolValue = (Boolean) value;
 		} else if (null != value) {
 			String stringValue = value.toString();
 			boolValue = StringTools.stringToBoolean(stringValue);
 			logger.warn(
 					"Not sure if I handle string value [{}] for the field [{}] correctly, relying on Object.toString()",
 					stringValue, name);
 		}
 		// TODO
 		// logger.debug("value->[{}]", boolValue.toString());
 		if (!sort) {
 			document.add(new Field(name, null == boolValue ? "" : boolValue
 					.toString(), Field.Store.NO, Field.Index.NOT_ANALYZED));
 		} else {
 			document.add(new Field(name, null == boolValue ? "" : boolValue
 					.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
 		}
 
 	}
 
 	void addIntIndexField(Document document, String name, Object value,
 			boolean store, boolean sort) {
 		Long longValue = null;
 		if (value instanceof BigInteger) {
 			longValue = ((BigInteger) value).longValue();
 		} else if (value instanceof NodeInfo) {
 			longValue = Long.parseLong(((NodeInfo) value).getStringValue());
 		} else {
 
 			logger.warn(
 					"Not sure if I handle long value of [{}] for the field [{}] correctly, relying on Object.toString()",
 					value.getClass().getName(), name);
 		}
 		// TODO
 		// logger.debug( "field [{}] value->[{}]", name, longValue.toString());
 		// logger.debug( "field [{}] store->[{}]", name, store);
 		// logger.debug( "field [{}] sort->[{}]", name, sort);
 		if (null != longValue) {
 			// its more clear to divide the if statement in 3 parts
 			if (sort) {
 				// It has to be int because of sorting (otherwise the error:
 				// Invalid shift value in prefixCoded string (is encoded value
 				// really an INT?))
 				document.add(new NumericField(name, Field.Store.YES, true)
 						.setLongValue(longValue));
 			} else {
 				if (!store) {
 					document.add(new NumericField(name).setLongValue(longValue));
 				} else {
 					document.add(new NumericField(name, Field.Store.YES, true)
 							.setLongValue(longValue));
 				}
 
 			}
 		} else {
 			logger.warn("Long value of the field [{}] was null", name);
 		}
 	}
 
 	void addIndexDocument(IndexWriter iwriter, Document document)
 			throws Exception {
 		try {
 			iwriter.addDocument(document);
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 			throw x;
 		}
 	}
 
 	void commitIndex(IndexWriter iwriter) {
 		try {
 			iwriter.optimize();
 			iwriter.commit();
 			iwriter.close();
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 		}
 	}
 
 	void commitIndex(IndexWriter iwriter, Map<String, String> map) {
 		try {
 			iwriter.optimize();
 			iwriter.commit(map);
 			iwriter.close();
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 		}
 	}
 
 	
 	// process each document (this has all the logic related with dynamic
 	// attributes)
 	public Document processEntryIndex(Object node, Configuration config,
 			XPathQueryService service, Map<String, XPathExpression> fieldXpe)
 			throws Exception {
 		Document luceneDoc = new Document();
 		XPath xp = new XPathEvaluator(config);
 		for (FieldInfo field : fields.values()) {
 			try {
 				if (!field.process) {
 					List values = (List) fieldXpe.get(field.name).evaluate(
 							node, XPathConstants.NODESET);
 					for (Object v : values) {
 
 						if ("integer".equals(field.type)) {
 							addIntIndexField(luceneDoc, field.name, v,
 									field.shouldStore, field.shouldSort);
 
 						} else if ("date".equals(field.type)) {
 							// todo:
 							// addDateIndexField(d,
 							// field.name,
 							// v);
 							logger.error(
 									"Date fields are not supported yet, field [{}] will not be created",
 									field.name);
 						} else if ("boolean".equals(field.type)) {
 							addBooleanIndexField(luceneDoc, field.name, v,
 									field.shouldSort);
 						} else {
 							addIndexField(luceneDoc, field.name, v,
 									field.shouldAnalyze, field.shouldStore,
 									field.shouldSort);
 						}
 					}
 
 				} else {
 					if (field.name.equalsIgnoreCase("attributes")) {
 						// implement here the biosamples
 						// database sample attributes
 						// logic
 						// TODO: rpe
 						// logger.debug("There is A special treatment for this field->"
 						// + field.name);
 						List values = (List) fieldXpe.get(field.name).evaluate(
 								node, XPathConstants.NODESET);
 
 						for (Iterator iterator = values.iterator(); iterator
 								.hasNext();) {
 							Object object = (Object) iterator.next();
 							// logger.debug("attributes->" + object);
 							String valClass = (String) fieldXpe.get(
 									"attributeName").evaluate(object,
 									XPathConstants.STRING);
 							//TODO: document this on trac and on website documentations help
 							valClass=valClass.replace(" ", "_").toLowerCase();
 							//valClass=valClass.toLowerCase();
 							String valType = (String) fieldXpe.get(
 									"attributeType").evaluate(object,
 									XPathConstants.STRING);
 							String valValue = (String) fieldXpe.get(
 									"attributeValue").evaluate(object,
 									XPathConstants.STRING);
 
 							if (!valType.equalsIgnoreCase("integer")
 									&& !valType.equalsIgnoreCase("real")) {
 								//TODO: change this value
 								valValue=valValue.substring(0, Math.min(valValue.length(), 25));
 								addIndexField(luceneDoc,"attributes" , "="+valClass + "= " + valValue,
 										true, false, true);
 							} else {
 
 								valValue = valValue.trim();
 								int val = 0;
 								if (valValue == null
 										|| valValue.equalsIgnoreCase("")
 										|| valValue.equalsIgnoreCase("NaN")) {
 									valValue = "0";
 								}
 								BigDecimal num = new BigDecimal(valValue);
 								num = num.multiply(new BigDecimal(100));
 								int taux = num.toBigInteger().intValue();
 								valValue = String.format("%07d", taux);
 								//I need to mantain the spaces for lucene consider different words
 								addIndexField(luceneDoc, "attributes" , "="+valClass + "= " + valValue,
 										true, false, true);
 							}
 //							logger.debug("@class->" + valClass);
 //							logger.debug("@type->" + valType);
 //							logger.debug("text->" + valValue);
 						}
 					} else {
 						// logger.debug("There is NO special treatment for this field->"
 						// + field.name);
 					}
 				}
 			} catch (XPathExpressionException x) {
 				String xmlError = PrintUtils.printNodeInfo((NodeInfo) node,
 						config);
 				logger.error("Field being processed->[{}]", field.name);
 				logger.error("XPathExpressionException->[{}]", x.getMessage());
 				logger.error("Caught an exception while indexing expression ["
 						+ field.path + "] for document ["
 						+ ((NodeInfo) node).getStringValue().substring(0, 20)
 						+ "...]", x);
 
 				throw new Exception("XPathExpressionException Xml:" + xmlError,
 						x);
 			} catch (Exception xe) {
 				String xmlError = PrintUtils.printNodeInfo((NodeInfo) node,
 						config);
 				logger.error("Generic Exception->[{}]", xe.getMessage());
 				throw new Exception("Generic Exception Xml:" + xmlError, xe);
 			}
 		}
 
 		return luceneDoc;
 	}
 
 	// TODO: I'm assuming that there is always an attribute @id in each element
 	public void indexFromXmlDB_FACETS(String indexLocationDirectory,
 			String dbHost, int dbPort, String dbPassword, String dbName)
 			throws Exception {
 		int countNodes = 0;
 		String driverXml = "";
 		String connectionString = "";
 		Collection coll;
 		IndexWriter w = null;
 		Map<String, XPathExpression> fieldXpe = new HashMap<String, XPathExpression>();
 		try {
 
 			Directory indexTempDirectory = FSDirectory.open(new File(
 					indexLocationDirectory, indexId));
 			w = createIndex(indexTempDirectory, indexAnalyzer);
 
 			Directory taxDir = FSDirectory.open(new File(indexLocationDirectory
 					+ "Facets", indexId));
 
 			DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(
 					taxDir);
 			CategoryDocumentBuilder docBuilder = new CategoryDocumentBuilder(
 					taxoWriter);
 
 			HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
 					.getInstance().getPreferences()
 					.getConfSubset("bs.xmldatabase");
 
 			if (null != connsConf) {
 				// TODO: rpe use the component XmlDatabasePooling
 				driverXml = connsConf.getString("driver");
 				// I will use the connectionString that was passed by parameter
 				// (in several parameters)
 				connectionString = connsConf.getString("base") + "://" + dbHost
 						+ ":" + dbPort + "/" + dbName;
 			} else {
 				logger.error("bs.xmldatabase Configuration is missing!!");
 			}
 
 			// I cannot register this database again (this is already registered
 			// on XmlDbConnectionPool Component -
 			// java.nio.channels.OverlappingFileLockException
 			// c = Class.forName(driverXml);
 			// db = (Database) c.newInstance();
 			// DatabaseManager.registerDatabase(db);
 			logger.debug("connectionString->" + connectionString);
 			coll = DatabaseManager.getCollection(connectionString);
 			XPathQueryService service = (XPathQueryService) coll.getService(
 					"XPathQueryService", "1.0");
 
 			DocumentInfo source = null;
 			// Loop through all result items
 
 			// collect all the fields data
 			Configuration config = ((SaxonEngine) Application
 					.getAppComponent("SaxonEngine")).trFactory
 					.getConfiguration();
 
 			XPath xp = new XPathEvaluator(config);
 			// XPathExpression xpe = xp.compile(this.env.indexDocumentPath);
 
 			for (FieldInfo field : fields.values()) {
 				fieldXpe.put(field.name, xp.compile(field.path));
 				logger.debug("Field Path->[{}]", field.path);
 			}
 
 			// the xmldatabase is not very correct and have memory problem for
 			// queires with huge results, so its necessary to implement our own
 			// iteration mechanism
 			//
 			// // I will collect all the results
 			// ResourceSet set = service.query(this.env.indexDocumentPath);
 			// //TODO rpe
 			// //ResourceSet set = service.query("//Sample");
 			// logger.debug("Number of results->" + set.getSize());
 			// long numberResults = set.getSize();
 			long numberResults = 0;
 			ResourceSet set = service.query("count(" + indexDocumentPath + ")");
 			if (set.getIterator().hasMoreResources()) {
 				numberResults = Integer.parseInt((String) set.getIterator()
 						.nextResource().getContent());
 			}
 			logger.debug("Number of results->" + numberResults);
 			long pageSizeDefault = 50000;
 			// the samplegroup cannot be big otherwise I will obtain a memory
 			// error ... but the sample must b at least one million because the
 			// paging queries are really slow - we need to balance it
 			// (for samples 1million, for samplegroup 50000)
 			if (numberResults > 1000000) {
 				pageSizeDefault = 1000000;
 			}
 
 			long pageNumber = 1;
 			int count = 0;
 			Map<String, AttsInfo[]> cacheAtt = new HashMap<String, AttsInfo[]>();
 			Map<String, XPathExpression> cacheXpathAtt = new HashMap<String, XPathExpression>();
 			Map<String, XPathExpression> cacheXpathAttValue = new HashMap<String, XPathExpression>();
 			while ((pageNumber * pageSizeDefault) <= (numberResults
 					+ pageSizeDefault - 1)) {
 				// while ((pageNumber<=1)) {
 				// calculate the last hit
 				long pageInit = (pageNumber - 1) * pageSizeDefault + 1;
 				long pageSize = (pageNumber * pageSizeDefault < numberResults) ? pageSizeDefault
 						: (numberResults - pageInit + 1);
 
 				service = (XPathQueryService) coll.getService(
 						"XPathQueryService", "1.0");
 
 				// xquery paging using subsequence function
 				long time = System.nanoTime();
 
 				// /set =
 				// service.query("for $x in(/Biosamples/SampleGroup/Sample/@id) return string($x)");
 				set = service.query("for $x in(subsequence("
 						+ indexDocumentPath + "/@id," + pageInit + ","
 						+ pageSize + ")) return string($x)");
 				// logger.debug("Number of results of page->" + set.getSize());
 				double ms = (System.nanoTime() - time) / 1000000d;
 				logger.info("Query XMLDB took ->[{}]", ms);
 
 				ResourceIterator iter = set.getIterator();
 				XPath xp2;
 				XPathExpression xpe2;
 				List documentNodes;
 				StringReader reader;
 				// cache of distinct attributes fora each sample group
 
 				while (iter.hasMoreResources()) {
 					count++;
 					logger.debug("its beeing processed the number ->" + count);
 					String idSample = (String) iter.nextResource().getContent();
 					logger.debug("idSample->" + idSample);
 					// I need to get the sample
 					ResourceSet setid = service.query(indexDocumentPath
 							+ "[@id='" + idSample + "']");
 
 					// System.out.println("/Biosamples/SampleGroup/Sample[@id='"
 					// + idSample + "']");
 					ResourceIterator iterid = setid.getIterator();
 					List<CategoryPath> sampleCategories = null;
 					while (iterid.hasMoreResources()) {
 						// System.out.println("");
 						// /xml=(String) iterid.nextResource().getContent();
 
 						// /xml=(String) iter.nextResource().getContent();
 						// logger.debug("xml->"+xml);
 						// /reader = new StringReader(xml);
 						StringBuilder xml = new StringBuilder();
 						xml.append((String) iterid.nextResource().getContent());
 
 						// logger.debug(xml.toString());
 						reader = new StringReader(xml.toString());
 						source = config.buildDocument(new StreamSource(reader));
 
 						// logger.debug("XML DB->[{}]",
 						// PrintUtils.printNodeInfo((NodeInfo) source, config));
 						Document d = new Document();
 
 						xp2 = new XPathEvaluator(source.getConfiguration());
 
 						int position = indexDocumentPath.lastIndexOf("/");
 						;
 						String pathRoot = "";
 						if (position != -1) {
 							pathRoot = indexDocumentPath.substring(position);
 						} else {
 							pathRoot = indexDocumentPath;
 						}
 						// logger.debug("PathRoot->[{}]",pathRoot);
 						xpe2 = xp2.compile(pathRoot);
 						// TODO rpe
 						// xpe2 = xp2.compile("/Sample");
 						documentNodes = (List) xpe2.evaluate(source,
 								XPathConstants.NODESET);
 
 						for (Object node : documentNodes) {
 							// logger.debug("XML DB->[{}]",PrintUtils.printNodeInfo((NodeInfo)node,config));
 							for (FieldInfo field : fields.values()) {
 								try {
 
 									// Configuration
 									// config=doc.getConfiguration();
 									// I Just have to calculate the Xpath
 									if (!field.process) {
 
 										List values = (List) fieldXpe.get(
 												field.name).evaluate(node,
 												XPathConstants.NODESET);
 										// logger.debug("Field->[{}] values-> [{}]",
 										// field.name,
 										// values.toString());
 										for (Object v : values) {
 
 											if ("integer".equals(field.type)) {
 												addIntIndexField(d, field.name,
 														v, field.shouldStore,
 														field.shouldSort);
 
 												// Just to test I will put here
 												// one facet for the samples
 												if (field.name
 														.equalsIgnoreCase("samples")) {
 													System.out
 															.println("Value-->"
 																	+ v.toString());
 													sampleCategories = new ArrayList<CategoryPath>();
 													sampleCategories
 															.add(new CategoryPath(
 																	"samples",
 																	v.toString()));
 												}
 
 											} else if ("date"
 													.equals(field.type)) {
 												// todo: addDateIndexField(d,
 												// field.name,
 												// v);
 												logger.error(
 														"Date fields are not supported yet, field [{}] will not be created",
 														field.name);
 											} else if ("boolean"
 													.equals(field.type)) {
 												addBooleanIndexField(d,
 														field.name, v,
 														field.shouldSort);
 											} else {
 												addIndexField(d, field.name, v,
 														field.shouldAnalyze,
 														field.shouldStore,
 														field.shouldSort);
 											}
 										}
 
 									} else {
 										if (field.name
 												.equalsIgnoreCase("attributes")) {
 											// implement here the biosamples
 											// database sample attributes logic
 											// TODO: rpe
 											// logger.debug("There is A special treatment for this field->"
 											// + field.name);
 
 											List values = (List) fieldXpe.get(
 													field.name).evaluate(node,
 													XPathConstants.NODESET);
 
 											// XPathExpression
 											// classAtt=xp.compile("@class");
 											// XPathExpression
 											// typeAtt=xp.compile("@dataType");
 											// XPathExpression
 											// valueAtt=xp.compile("value");
 											String groupId = (String) fieldXpe
 													.get("samplegroup")
 													.evaluate(
 															node,
 															XPathConstants.STRING);
 											String id = (String) fieldXpe.get(
 													"accession").evaluate(node,
 													XPathConstants.STRING);
 
 											// logger.debug(groupId+"$$$" + id);
 
 											// logger.debug("Field->[{}] values-> [{}]",
 											// field.name,
 											// values.toString());
 
 											AttsInfo[] attsInfo = null;
 											if (cacheAtt.containsKey(groupId)) {
 												attsInfo = cacheAtt
 														.get(groupId);
 											} else {
 												logger.debug("No exists cache for samplegroup->"
 														+ groupId);
 												// ResourceSet setAtt =
 												// service.query("distinct-values(/Biosamples/SampleGroup[@id='"
 												// + groupId +
 												// "']/Sample/attribute[@dataType!='INTEGER']/replace(@class,' ', '-'))");
 												// /ResourceSet setAtt =
 												// service.query("distinct-values(/Biosamples/SampleGroup[@id='"
 												// + groupId +
 												// "']/Sample/attribute/replace(@class,' ', '-'))");
 												// /ResourceSet setAtt =
 												// service.query("distinct-values(/Biosamples/SampleGroup[@id='"
 												// + groupId +
 												// "']/Sample/attribute/@class)");
 												ResourceSet setAtt = service
 														.query("data(/Biosamples/SampleGroup[@id='"
 																+ groupId
 																+ "']/SampleAttributes/attribute/@class)");
 												// logger.debug("->"
 												// +
 												// "/Biosamples/SampleGroup[@id='"
 												// + groupId +
 												// "']/SampleAttributes/attribute/@class");
 
 												ResourceIterator resAtt = setAtt
 														.getIterator();
 												int i = 0;
 												attsInfo = new AttsInfo[(int) setAtt
 														.getSize()];
 												while (resAtt
 														.hasMoreResources()) {
 													String classValue = (String) resAtt
 															.nextResource()
 															.getContent();
 													// logger.debug("->"
 													// + classValue);
 													// need to use this because
 													// of the use of quotes in
 													// the name of the classes
 													String classValueWitoutQuotes = classValue
 															.replaceAll("\"",
 																	"\"\"");
 													// logger.debug("Class value->"
 													// + classValue);
 													XPathExpression xpathAtt = null;
 													XPathExpression xpathAttValue = null;
 													if (cacheXpathAtt
 															.containsKey(classValue)) {
 														xpathAtt = cacheXpathAtt
 																.get(classValue);
 														xpathAttValue = cacheXpathAttValue
 																.get(classValue);
 													} else {
 
 														xpathAtt = xp
 																.compile("./attribute[@class=\""
 																		+ classValueWitoutQuotes
 																		+ "\"]/@dataType");
 
 														xpathAttValue = xp
 																.compile("attribute[@class=\""
 																		+ classValueWitoutQuotes
 																		+ "\"]/value/text()[last()]");
 
 														// logger.debug("attribute[@class=\""
 														// +
 														// classValueWitoutQuotes
 														// +
 														// "\"]//value/text()");
 														// //xpathAttValue=xp.compile("./attribute[@class=\""
 														// +
 														// classValueWitoutQuotes
 														// +
 														// "\"]/value[1]/text()");
 														// logger.debug("./attribute[@class=\""
 														// +
 														// classValueWitoutQuotes
 														// +
 														// "\"]/value[1]/text()");
 														cacheXpathAtt.put(
 																classValue,
 																xpathAtt);
 														cacheXpathAttValue.put(
 																classValue,
 																xpathAttValue);
 													}
 													// this doesnt work when the
 													// first sample of sample
 													// group doens have all the
 													// attributes
 													// im using \" becuse there
 													// are some attributes thas
 													// has ' on the name!!!
 													// /ResourceSet setAttType =
 													// service.query("string((/Biosamples/SampleGroup[@id='"
 													// + groupId
 													// +"']/Sample/attribute[@class=replace(\""
 													// + classValueWitoutQuotes
 													// +
 													// "\",'-',' ')]/@dataType)[1])");
 													// /ResourceSet setAttType =
 													// service.query("string(/Biosamples/SampleGroup[@id='"
 													// + groupId
 													// +"']/Sample/attribute[@class=\""
 													// + classValueWitoutQuotes
 													// + "\"]/@dataType)");
 													ResourceSet setAttType = service
 															.query("data(/Biosamples/SampleGroup[@id='"
 																	+ groupId
 																	+ "']/SampleAttributes/attribute[@class=\""
 																	+ classValueWitoutQuotes
 																	+ "\"]/@dataType)");
 													String dataValue = (String) setAttType
 															.getIterator()
 															.nextResource()
 															.getContent();
 													// logger.debug("Data Type of "
 													// + classValue + " ->" +
 													// dataValue);
 													// String
 													// dataValue=(String)xpathAtt.evaluate(node,
 													// XPathConstants.STRING);
 													AttsInfo attsI = new AttsInfo(
 															classValue,
 															dataValue);
 													// logger.debug("Atttribute->class"
 													// + attsI.name + "->type->"
 													// + attsI.type + "->i" +
 													// i);
 													attsInfo[i] = attsI;
 													// logger.debug("distinct att->"
 													// + value);
 													// cacheAtt.put(groupId,
 													// value);
 													i++;
 												}
 												cacheAtt.put(groupId, attsInfo);
 												// distinctAtt=cacheAtt.get(groupId);
 												// logger.debug("Already exists->"
 												// + distinctAtt);
 											}
 											int len = attsInfo.length;
 											for (int i = 0; i < len; i++) {
 												// logger.debug("$$$$$$->" +
 												// attsInfo[i].name + "$$$$" +
 												// attsInfo[i].type);
 												if (!attsInfo[i].type
 														.equalsIgnoreCase("integer")
 														&& !attsInfo[i].type
 																.equalsIgnoreCase("real")) {
 
 													XPathExpression valPath = cacheXpathAttValue
 															.get(attsInfo[i].name);
 													String val = (String) valPath
 															.evaluate(
 																	node,
 																	XPathConstants.STRING);
 													// logger.debug("$$$$$$->" +
 													// "STRING->" + val + "");
 													addIndexField(d, (i + 1)
 															+ "", val, true,
 															false, true);
 												} else {
 													XPathExpression valPath = cacheXpathAttValue
 															.get(attsInfo[i].name);
 													String valS = (String) valPath
 															.evaluate(
 																	node,
 																	XPathConstants.STRING);
 													valS = valS.trim();
 													// logger.debug("Integer->"
 													// + valS);
 													int val = 0;
 													if (valS == null
 															|| valS.equalsIgnoreCase("")
 															|| valS.equalsIgnoreCase("NaN")) {
 														valS = "0";
 													}
 													// sort numbers as strings
 													// logger.debug("class->" +
 													// attsInfo[i].name
 													// +"value->##"+ valS +
 													// "##");
 													BigDecimal num = new BigDecimal(
 															valS);
 													num = num
 															.multiply(new BigDecimal(
 																	100));
 													int taux = num
 															.toBigInteger()
 															.intValue();
 													valS = String.format(
 															"%07d", taux);
 													// logger.debug("Integer->"
 													// + valS + "position->"
 													// +(i+1)+"integer");
 													addIndexField(d, (i + 1)
 															+ "", valS, true,
 															false, true);
 													// addIntIndexField(d,
 													// (i+1)+"integer", new
 													// BigInteger(valS),false,
 													// true);
 													//
 												}
 											}
 
 										} else {
 											// logger.debug("There is NO special treatment for this field->"
 											// + field.name);
 										}
 									}
 								} catch (XPathExpressionException x) {
 									String xmlError = PrintUtils.printNodeInfo(
 											(NodeInfo) node, config);
 									logger.error("XML DB->[{}]",
 											xmlError);
 									logger.error(
 											"Caught an exception while indexing expression ["
 													+ field.path
 													+ "] for document ["
 													+ ((NodeInfo) source)
 															.getStringValue()
 															.substring(0, 20)
 													+ "...]", x);
 									throw new Exception("Xml:" + xmlError, x);
 								}
 							}
 						}
 
 						documentNodes = null;
 						source = null;
 						reader = null;
 						xml = null;
 						countNodes++;
 						// logger.debug("count->[{}]", countNodes);
 
 						// facet tests
 
 						docBuilder.setCategoryPaths(sampleCategories);
 						docBuilder.build(d);
 
 						addIndexDocument(w, d);
 
 					}
 				}
 				logger.debug("until now it were processed->[{}]", pageNumber
 						* pageSizeDefault);
 				pageNumber++;
 				if (coll != null) {
 					try {
 						// coll.close();
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				set = null;
 
 			}
 
 			setCountDocuments(countNodes);
 			// add metadata to the lucene index
 			Map<String, String> map = new HashMap<String, String>();
 			map.put("numberDocs", Integer.toString(countNodes));
 			map.put("date", Long.toString(System.nanoTime()));
 			// logger.debug(Application.getInstance().getComponent("XmlDbConnectionPool").getMetaDataInformation());
 			// I cannot call directly
 			// getComponent("XmlDbConnectionPool").getMetaDataInformation(),
 			// because I can be working in a did
 			String dbInfo = ((XmlDbConnectionPool) Application.getInstance()
 					.getComponent("XmlDbConnectionPool")).getDBInfo(dbHost,
 					dbPort, dbPassword, dbName);
 
 			map.put("DBInfo", dbInfo);
 			// facet
 			taxoWriter.commit();
 			taxoWriter.close();
 			commitIndex(w, map);
 
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 			w.close();
 			throw x;
 		}
 	}
 
 }
