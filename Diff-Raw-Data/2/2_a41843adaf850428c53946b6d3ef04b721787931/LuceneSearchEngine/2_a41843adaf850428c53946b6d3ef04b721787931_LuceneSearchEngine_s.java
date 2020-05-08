 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.search;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopScoreDocCollector;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.highlight.Highlighter;
 import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
 import org.apache.lucene.search.highlight.QueryScorer;
 import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
 import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.store.LockFactory;
 import org.apache.lucene.store.SimpleFSLockFactory;
 import org.apache.lucene.util.Version;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.jamwiki.Environment;
 import org.jamwiki.SearchEngine;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.SearchResultEntry;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicType;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * An implementation of {@link org.jamwiki.SearchEngine} that uses
  * <a href="http://lucene.apache.org/java/">Lucene</a> to perform searches of
  * Wiki content.
  */
 public class LuceneSearchEngine implements SearchEngine {
 
 	/** Where to log to */
 	private static final WikiLogger logger = WikiLogger.getLogger(LuceneSearchEngine.class.getName());
 	/** Directory for search index files */
 	private static final String SEARCH_DIR = "search";
 	/** Name of the search index field that holds the processed topic content. */
 	private static final String FIELD_TOPIC_CONTENT = "topic_content";
 	/** Name of the search index field that holds the un-processed topic name. */
 	protected static final String FIELD_TOPIC_NAME = "topic_name";
 	/** Name of the search index field that holds the processed topic name. */
 	private static final String FIELD_TOPIC_NAME_ANALYZED = "topic_name_analyzed";
 	/** Name of the search index field that holds the un-processed topic namespace. */
 	private static final String FIELD_TOPIC_NAMESPACE = "topic_namespace";
 	/** Lucene compatibility version. */
	protected static final Version USE_LUCENE_VERSION = Version.LUCENE_33;
 	/** Maximum number of results to return per search. */
 	// FIXME - make this configurable
 	protected static final int MAXIMUM_RESULTS_PER_SEARCH = 200;
 	/** Flag indicating whether or not to commit search index changes immediately. */
 	private boolean autoCommit = true;
 	/** Store Searchers (once opened) for re-use for performance reasons. */
 	private Map<String, IndexSearcher> searchers = new HashMap<String, IndexSearcher>();
 	/** Store Writers (once opened) for re-use for performance reasons. */
 	private Map<String, IndexWriter> indexWriters = new HashMap<String, IndexWriter>();
 
 	/**
 	 * Add a topic to the search index.
 	 *
 	 * @param topic The Topic object that is to be added to the index.
 	 */
 	public void addToIndex(Topic topic) {
 		try {
 			long start = System.currentTimeMillis();
 			IndexWriter writer = this.retrieveIndexWriter(topic.getVirtualWiki(), false);
 			this.addToIndex(writer, topic);
 			this.commit(writer, this.autoCommit);
 			if (logger.isDebugEnabled()) {
 				logger.debug("Add to search index for topic " + topic.getVirtualWiki() + " / " + topic.getName() + " in " + ((System.currentTimeMillis() - start) / 1000.000) + " s.");
 			}
 		} catch (Exception e) {
 			logger.error("Exception while adding topic " + topic.getVirtualWiki() + " / " + topic.getName(), e);
 		}
 	}
 
 	/**
 	 * Add a topic to the search index.
 	 *
 	 * @param writer The IndexWriter to use when updating the search index.
 	 * @param topic The Topic object that is to be added to the index.
 	 */
 	private void addToIndex(IndexWriter writer, Topic topic) throws IOException {
 		if (topic.getTopicType() == TopicType.REDIRECT) {
 			// do not index redirects
 			return;
 		}
 		Document standardDocument = createStandardDocument(topic);
 		writer.addDocument(standardDocument);
 		this.resetIndexSearcher(topic.getVirtualWiki());
 	}
 
 	/**
 	 * Force a flush of any pending commits to the search index.
 	 *
 	 * @param virtualWiki The virtual wiki for which pending updates are being
 	 *  committed.
 	 */
 	public void commit(String virtualWiki) {
 		try {
 			this.commit(this.retrieveIndexWriter(virtualWiki, false), true);
 		} catch (IOException e) {
 			logger.error("Exception while committing pending changes for virtual wiki " + virtualWiki, e);
 		}
 	}
 
 	/**
 	 * Commit pending changes to the writer only if the commitNow value is true.
 	 * This is primarily a utility method for working with the autoCommit flag.
 	 */
 	private void commit(IndexWriter writer, boolean commitNow) throws IOException {
 		if (commitNow) {
 			writer.commit();
 		}
 	}
 
 	/**
 	 * Given the search text, searcher object, and query analyzer generate an
 	 * appropriate Lucene search query.
 	 */
 	protected Query createSearchQuery(IndexSearcher searcher, StandardAnalyzer analyzer, String text, List<Integer> namespaces) throws IOException, ParseException {
 		BooleanQuery fullQuery = new BooleanQuery();
 		QueryParser qp;
 		// build the namespace portion the query
 		if (namespaces != null && !namespaces.isEmpty()) {
 			qp = new QueryParser(USE_LUCENE_VERSION, FIELD_TOPIC_NAMESPACE, analyzer);
 			StringBuilder namespaceText = new StringBuilder();
 			for (Integer namespaceId : namespaces) {
 				if (namespaceText.length() != 0) {
 					namespaceText.append(" ").append(QueryParser.Operator.OR).append(" ");
 				}
 				namespaceText.append(namespaceId);
 			}
 			fullQuery.add(qp.parse(namespaceText.toString()), Occur.MUST);
 		}
 		// create a sub-query for topic name & topic text
 		BooleanQuery nameAndContentQuery = new BooleanQuery();
 		// topic name
 		qp = new QueryParser(USE_LUCENE_VERSION, FIELD_TOPIC_NAME_ANALYZED, analyzer);
 		nameAndContentQuery.add(qp.parse(text), Occur.SHOULD);
 		// topic content
 		qp = new QueryParser(USE_LUCENE_VERSION, FIELD_TOPIC_CONTENT, analyzer);
 		nameAndContentQuery.add(qp.parse(text), Occur.SHOULD);
 		// rewrite the sub-query to expand it - required for wildcards to work with highlighter
 		Query subQuery = searcher.rewrite(nameAndContentQuery);
 		// add the sub-query to the main query
 		fullQuery.add(subQuery, Occur.MUST);
 		return fullQuery;
 	}
 
 	/**
 	 * Create a basic Lucene document to add to the index.  This document
 	 * is suitable to be parsed with the StandardAnalyzer.
 	 */
 	private Document createStandardDocument(Topic topic) {
 		String topicContent = topic.getTopicContent();
 		if (topicContent == null) {
 			topicContent = "";
 		}
 		Document doc = new Document();
 		// store the (not analyzed) topic name to use when deleting records from the index.
 		doc.add(new Field(FIELD_TOPIC_NAME, topic.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
 		// add the topic namespace (not analyzed) topic namespace to allow retrieval by namespace.
 		// this field is used internally in searches.
 		doc.add(new Field(FIELD_TOPIC_NAMESPACE, topic.getNamespace().getId().toString(), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
 		// analyze the topic name so that (for example) a search for "New York" will match "New York City"
 		Field nameField = new Field(FIELD_TOPIC_NAME_ANALYZED, topic.getName(), Field.Store.NO, Field.Index.ANALYZED);
 		// make the topic name worth 3x as much as topic content in searches
 		nameField.setBoost(3.0f);
 		doc.add(nameField);
 		// analyze & store the topic content so that it is searchable and also usable for display in
 		// search result summaries
 		doc.add(new Field(FIELD_TOPIC_CONTENT, topicContent, Field.Store.YES, Field.Index.ANALYZED));
 		return doc;
 	}
 
 	/**
 	 * Remove a topic from the search index.
 	 *
 	 * @param topic The topic object that is to be removed from the index.
 	 */
 	public void deleteFromIndex(Topic topic) {
 		try {
 			long start = System.currentTimeMillis();
 			// delete the current document
 			IndexWriter writer = this.retrieveIndexWriter(topic.getVirtualWiki(), false);
 			this.deleteFromIndex(writer, topic);
 			this.commit(writer, this.autoCommit);
 			if (logger.isDebugEnabled()) {
 				logger.debug("Delete from search index for topic " + topic.getVirtualWiki() + " / " + topic.getName() + " in " + ((System.currentTimeMillis() - start) / 1000.000) + " s.");
 			}
 		} catch (Exception e) {
 			logger.error("Exception while adding topic " + topic.getName(), e);
 		}
 	}
 
 	/**
 	 * Remove a topic from the search index.
 	 *
 	 * @param writer The IndexWriter to use when updating the search index.
 	 * @param topic The topic object that is to be removed from the index.
 	 */
 	private void deleteFromIndex(IndexWriter writer, Topic topic) throws IOException {
 		writer.deleteDocuments(new Term(FIELD_TOPIC_NAME, topic.getName()));
 		this.resetIndexSearcher(topic.getVirtualWiki());
 	}
 
 	/**
 	 * Find all documents that contain a specific search term, ordered by relevance.
 	 * This method supports all Lucene search query syntax.
 	 *
 	 * @param virtualWiki The virtual wiki for the topic.
 	 * @param text The search term being searched for.
 	 * @return A list of SearchResultEntry objects for all documents that
 	 *  contain the search term.
 	 */
 	public List<SearchResultEntry> findResults(String virtualWiki, String text, List<Integer> namespaces) {
 		StandardAnalyzer analyzer = new StandardAnalyzer(USE_LUCENE_VERSION);
 		List<SearchResultEntry> results = new ArrayList<SearchResultEntry>();
 		logger.trace("search text: " + text);
 		try {
 			IndexSearcher searcher = this.retrieveIndexSearcher(virtualWiki);
 			Query query = this.createSearchQuery(searcher, analyzer, text, namespaces);
 			// actually perform the search
 			TopScoreDocCollector collector = TopScoreDocCollector.create(MAXIMUM_RESULTS_PER_SEARCH, true);
 			searcher.search(query, collector);
 			Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>"), new SimpleHTMLEncoder(), new QueryScorer(query, FIELD_TOPIC_CONTENT));
 			ScoreDoc[] hits = collector.topDocs().scoreDocs;
 			for (int i = 0; i < hits.length; i++) {
 				int docId = hits[i].doc;
 				Document doc = searcher.doc(docId);
 				String summary = retrieveResultSummary(doc, highlighter, analyzer);
 				SearchResultEntry result = new SearchResultEntry();
 				result.setRanking(hits[i].score);
 				result.setTopic(doc.get(FIELD_TOPIC_NAME));
 				result.setSummary(summary);
 				results.add(result);
 			}
 		} catch (Exception e) {
 			logger.error("Exception while searching for " + text, e);
 		}
 		return results;
 	}
 
 	/**
 	 * Get the path, which holds all index files
 	 */
 	private File getSearchIndexPath(String virtualWiki) throws IOException {
 		File parent = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR), SEARCH_DIR);
 		try {
 			if (System.getProperty("org.apache.lucene.lockdir") == null) {
 				// set the Lucene lock directory.  this defaults to java.io.tmpdir,
 				// which may not be writable on some systems.
 				System.setProperty("org.apache.lucene.lockdir", parent.getPath());
 			}
 		} catch (Exception e) {
 			// probably a security exception
 			logger.warn("Unable to specify Lucene lock directory, default will be used: " + e.getMessage());
 		}
 		File child = new File(parent.getPath(), "index" + virtualWiki + File.separator);
 		if (!child.exists()) {
 			// create the search instance
 			child.mkdirs();
 			IndexWriter writer = this.openIndexWriter(child, true);
 			writer.close();
 		}
 		return child;
 	}
 
 	/**
 	 * Open an IndexWriter, executing error handling as needed.
 	 */
 	private IndexWriter openIndexWriter(File searchIndexPath, boolean create) throws IOException {
 		// NFS doesn't work with Lucene default locking as of Lucene 3.3, so use
 		// SimpleFSLockFactory instead.
 		LockFactory lockFactory = new SimpleFSLockFactory();
 		FSDirectory fsDirectory = FSDirectory.open(searchIndexPath, lockFactory);
 		IndexWriter indexWriter = null;
 		try {
 			indexWriter = new IndexWriter(fsDirectory, this.retrieveIndexWriterConfig(create));
 		} catch (LockObtainFailedException e) {
 			logger.warn("Unable to obtain lock for " + searchIndexPath.getAbsolutePath() + ".  Attempting to forcibly unlock the index.");
 			if (IndexWriter.isLocked(fsDirectory)) {
 				try {
 					IndexWriter.unlock(fsDirectory);
 					logger.info("Successfully unlocked search directory " + searchIndexPath.getAbsolutePath());
 				} catch (IOException ex) {
 					logger.warn("Unable to unlock search directory " + searchIndexPath.getAbsolutePath() + " " + ex.toString());
 				}
 			}
 		}
 		if (indexWriter == null) {
 			// try again, there could have been a stale lock
 			indexWriter = new IndexWriter(fsDirectory, this.retrieveIndexWriterConfig(create));
 		}
 		return indexWriter;
 	}
 
 	/**
 	 * Refresh the current search index by re-visiting all topic pages.
 	 *
 	 * @throws Exception Thrown if any error occurs while re-indexing the Wiki.
 	 */
 	public void refreshIndex() throws Exception {
 		List<VirtualWiki> allWikis = WikiBase.getDataHandler().getVirtualWikiList();
 		Topic topic;
 		for (VirtualWiki virtualWiki : allWikis) {
 			long start = System.currentTimeMillis();
 			int count = 0;
 			IndexWriter writer = null;
 			try {
 				writer = this.retrieveIndexWriter(virtualWiki.getName(), true);
 				List<String> topicNames = WikiBase.getDataHandler().getAllTopicNames(virtualWiki.getName(), false);
 				// FIXME - parsing all documents will be intolerably slow with even a
 				// moderately large Wiki
 				for (String topicName : topicNames) {
 					topic = WikiBase.getDataHandler().lookupTopic(virtualWiki.getName(), topicName, false);
 					if (topic == null) {
 						logger.info("Unable to rebuild search index for topic: " + topicName);
 						continue;
 					}
 					// note: no delete is necessary since a new index is being created
 					this.addToIndex(writer, topic);
 					count++;
 				}
 			} catch (Exception ex) {
 				logger.error("Failure while refreshing search index", ex);
 			} finally {
 				try {
 					if (writer != null) {
 						writer.optimize();
 					}
 				} catch (Exception e) {
 					logger.error("Exception during optimize", e);
 				}
 				try {
 					if (writer != null) {
 						writer.close();
 					}
 				} catch (Exception e) {
 					logger.error("Exception during close", e);
 				}
 			}
 			if (logger.isInfoEnabled()) {
 				logger.info("Rebuilt search index for " + virtualWiki.getName() + " (" + count + " documents) in " + ((System.currentTimeMillis() - start) / 1000.000) + " seconds");
 			}
 		}
 	}
 
 	/**
 	 * Call this method after a search index is updated to reset the searcher.
 	 */
 	private void resetIndexSearcher(String virtualWiki) throws IOException {
 		IndexSearcher searcher = searchers.get(virtualWiki);
 		if (searcher != null) {
 			searchers.remove(virtualWiki);
 			searcher.close();
 		}
 	}
 
 	/**
 	 * For performance reasons cache the IndexSearcher for re-use.
 	 */
 	protected IndexSearcher retrieveIndexSearcher(String virtualWiki) throws IOException {
 		IndexSearcher searcher = searchers.get(virtualWiki);
 		if (searcher == null) {
 			searcher = new IndexSearcher(this.retrieveIndexWriter(virtualWiki, false).getReader());
 			searchers.put(virtualWiki, searcher);
 		}
 		return searcher;
 	}
 
 	/**
 	 * For performance reasons create a cache of writers.  Since writers are not being
 	 * re-initialized then commit() must be called to explicitly flush data to the index,
 	 * otherwise it will be flushed on a programmatic basis by Lucene.
 	 */
 	private IndexWriter retrieveIndexWriter(String virtualWiki, boolean create) throws IOException {
 		IndexWriter indexWriter = indexWriters.get(virtualWiki);
 		if (create && indexWriter != null) {
 			// if the writer is going to blow away the existing index and create a new one then it
 			// should not be cached.  instead, close any open writer, create a new one, and return.
 			indexWriter.close();
 			indexWriters.remove(virtualWiki);
 			indexWriter = null;
 		}
 		if (indexWriter == null) {
 			File searchIndexPath = this.getSearchIndexPath(virtualWiki);
 			indexWriter = this.openIndexWriter(searchIndexPath, create);
 			if (!create) {
 				indexWriters.put(virtualWiki, indexWriter);
 			}
 		}
 		return indexWriter;
 	}
 
 	/**
 	 * Retrieve an IndexWriter configuration object.
 	 */
 	private IndexWriterConfig retrieveIndexWriterConfig(boolean create) {
 		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(USE_LUCENE_VERSION, new StandardAnalyzer(USE_LUCENE_VERSION));
 		indexWriterConfig.setOpenMode(((create) ? IndexWriterConfig.OpenMode.CREATE : IndexWriterConfig.OpenMode.CREATE_OR_APPEND));
 		return indexWriterConfig;
 	}
 
 	/**
 	 *
 	 */
 	protected String retrieveResultSummary(Document document, Highlighter highlighter, StandardAnalyzer analyzer) throws InvalidTokenOffsetsException, IOException {
 		String content = document.get(FIELD_TOPIC_CONTENT);
 		TokenStream tokenStream = analyzer.tokenStream(FIELD_TOPIC_CONTENT, new StringReader(content));
 		String summary = highlighter.getBestFragments(tokenStream, content, 3, "...");
 		if (StringUtils.isBlank(summary) && !StringUtils.isBlank(content)) {
 			summary = StringEscapeUtils.escapeHtml(content.substring(0, Math.min(200, content.length())));
 			if (Math.min(200, content.length()) == 200) {
 				summary += "...";
 			}
 		}
 		return summary;
 	}
 
 	/**
 	 *
 	 */
 	public void setAutoCommit(boolean autoCommit) {
 		this.autoCommit = autoCommit;
 	}
 
 	/**
 	 * 
 	 */
 	public void shutdown() throws IOException {
 		for (IndexSearcher searcher : this.searchers.values()) {
 			searcher.close();
 		}
 		for (IndexWriter indexWriter : this.indexWriters.values()) {
 			indexWriter.close();
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateInIndex(Topic topic) {
 		try {
 			long start = System.currentTimeMillis();
 			IndexWriter writer = this.retrieveIndexWriter(topic.getVirtualWiki(), false);
 			this.deleteFromIndex(writer, topic);
 			this.addToIndex(writer, topic);
 			this.commit(writer, this.autoCommit);
 			if (logger.isDebugEnabled()) {
 				logger.debug("Update search index for topic " + topic.getVirtualWiki() + " / " + topic.getName() + " in " + ((System.currentTimeMillis() - start) / 1000.000) + " s.");
 			}
 		} catch (Exception e) {
 			logger.error("Exception while updating topic " + topic.getVirtualWiki() + " / " + topic.getName(), e);
 		}
 	}
 }
