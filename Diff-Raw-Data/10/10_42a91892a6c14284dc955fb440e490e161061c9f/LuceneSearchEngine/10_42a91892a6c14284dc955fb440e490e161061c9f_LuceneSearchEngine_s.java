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
 import java.io.StringReader;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Vector;
 import org.apache.lucene.analysis.Token;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.KeywordAnalyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.Hits;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.PhraseQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.highlight.Highlighter;
 import org.apache.lucene.search.highlight.QueryScorer;
 import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
 import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.store.IndexInput;
 import org.apache.lucene.store.IndexOutput;
 import org.apache.lucene.store.RAMDirectory;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.parser.ParserInput;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.utils.Utilities;
 import org.springframework.util.StringUtils;
 
 /*
  *
  */
 public class LuceneSearchEngine {
 
 	/** Where to log to */
 	private static final WikiLogger logger = WikiLogger.getLogger(LuceneSearchEngine.class.getName());
 	/** Directory for search index files */
 	private static final String SEARCH_DIR = "search";
 	/** Id stored with documents to indicate the searchable topic name */
 	private static final String ITYPE_TOPIC = "topic";
 	/** Id stored with documents to indicate the searchable content. */
 	private static final String ITYPE_CONTENT = "content";
 	/** Id stored with documents to indicate the raw Wiki markup */
 	private static final String ITYPE_CONTENT_PLAIN = "content_plain";
 	/** Id stored with documents to indicate the topic name. */
 	private static final String ITYPE_TOPIC_PLAIN = "topic_plain";
 	/** Id stored with the document to indicate the search names of topics linked from the page.  */
 	private static final String ITYPE_TOPIC_LINK = "topic_link";
 
 	/**
 	 * Adds to the in-memory table. Does not remove indexed items that are
 	 * no longer valid due to deletions, edits etc.
 	 */
 	public static synchronized void addToIndex(Topic topic, Collection links) {
 		String virtualWiki = topic.getVirtualWiki();
 		String topicName = topic.getName();
 		String contents = topic.getTopicContent();
 		IndexWriter writer = null;
 		IndexReader reader = null;
 		try {
 			FSDirectory directory = FSDirectory.getDirectory(getSearchIndexPath(virtualWiki), false);
 			// delete the current document
 			try {
 				reader = IndexReader.open(directory);
 				reader.deleteDocuments(new Term(ITYPE_TOPIC_PLAIN, topicName));
 			} finally {
 				if (reader != null) {
 					try {
 						reader.close();
 					} catch (Exception e) {}
 				}
 			}
 			directory.close();
 			// add new document
 			try {
 				writer = new IndexWriter(directory, new StandardAnalyzer(), false);
 				KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
 				writer.optimize();
 				Document standardDocument = createStandardDocument(topic);
 				if (standardDocument != null) writer.addDocument(standardDocument);
 				Document keywordDocument = createKeywordDocument(topic, links);
 				if (keywordDocument != null) writer.addDocument(keywordDocument, keywordAnalyzer);
 			} finally {
 				try {
 					if (writer != null) {
 						writer.optimize();
 					}
 				} catch (Exception e) {}
 				try {
 					if (writer != null) {
 						writer.close();
 					}
 				} catch (Exception e) {}
 			}
 		} catch (Exception e) {
 			logger.severe("Exception while adding topic " + topicName, e);
 		}
 	}
 
 	/**
 	 * Copy an index from RAM to file
 	 * @param ram The index in RAM
 	 * @param indexFile The index on disc
 	 * @throws Exception
 	 */
 	private static void copyRamIndexToFileIndex(RAMDirectory ram, File indexFile) throws Exception {
 		FSDirectory index = FSDirectory.getDirectory(indexFile, true);
 		try {
 			if (IndexReader.isLocked(index)) {
 				// FIXME - huh?
 				// wait up to ten seconds until unlocked
 				int count = 0;
 				while (IndexReader.isLocked(index) && count < 20) {
 					try {
 						Thread.sleep(500);
 					} catch (InterruptedException ie) {
 						; // do nothing
 					}
 					count++;
 				}
 				// if still locked, force to unlock it
 				if (IndexReader.isLocked(index)) {
 					IndexReader.unlock(index);
 					logger.severe("Unlocking search index by force");
 				}
 			}
 			IndexWriter indexWriter = new IndexWriter(index, null, true);
 			indexWriter.close();
 		} catch (Exception e) {
 			logger.severe("Cannot create empty directory: ", e);
 			// delete all files in the temp directory
 			File[] files = indexFile.listFiles();
 			for (int i = 0; i < files.length; i++) {
 				files[i].delete();
 			}
 		}
 		// actually copy files
 		String[] ar = ram.list();
 		for (int i = 0; i < ar.length; i++) {
 			// make place on ram disk
 			IndexOutput os = index.createOutput(ar[i]);
 			// read current file
 			IndexInput is = ram.openInput(ar[i]);
 			// and copy to ram disk
 			int len = (int) is.length();
 			byte[] buf = new byte[len];
 			is.readBytes(buf, 0, len);
 			os.writeBytes(buf, len);
 			// graceful cleanup
 			is.close();
 			os.close();
 		}
 	}
 
 	/**
 	 * Create a basic Lucene document to add to the index that does treats
 	 * the topic content as a single keyword and does not tokenize it.
 	 */
 	private static Document createKeywordDocument(Topic topic, Collection links) throws Exception {
 		String topicContent = topic.getTopicContent();
 		if (topicContent == null) topicContent = "";
 		Document doc = new Document();
 		// store topic name for later retrieval
 		doc.add(new Field(ITYPE_TOPIC_PLAIN, topic.getName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
 		if (links == null) {
 			links = new Vector();
 		}
 		// index topic links for search purposes
 		for (Iterator iter = links.iterator(); iter.hasNext();) {
 			String linkTopic = (String)iter.next();
 			doc.add(new Field(ITYPE_TOPIC_LINK, linkTopic, Field.Store.NO, Field.Index.UN_TOKENIZED));
 		}
 		return doc;
 	}
 
 	/**
 	 * Create a basic Lucene document to add to the index.  This document
 	 * is suitable to be parsed with the StandardAnalyzer.
 	 */
 	private static Document createStandardDocument(Topic topic) throws Exception {
 		String topicContent = topic.getTopicContent();
 		if (topicContent == null) topicContent = "";
 		Document doc = new Document();
 		// store topic name and content for later retrieval
 		doc.add(new Field(ITYPE_TOPIC_PLAIN, topic.getName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
 		doc.add(new Field(ITYPE_CONTENT_PLAIN, topicContent, Field.Store.YES, Field.Index.NO));
 		// index topic name and content for search purposes
 		doc.add(new Field(ITYPE_TOPIC, new StringReader(topic.getName())));
 		doc.add(new Field(ITYPE_CONTENT, new StringReader(topicContent)));
 		return doc;
 	}
 
 	/**
 	 *
 	 */
 	public static synchronized void deleteFromIndex(Topic topic) {
 	}
 
 	/**
 	 *
 	 */
 	public static Collection findLinkedTo(String virtualWiki, String topicName) throws Exception {
 		KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
 		Collection results = new Vector();
 		IndexSearcher searcher = null;
 		try {
 			PhraseQuery query = new PhraseQuery();
 			Term term = new Term(ITYPE_TOPIC_LINK, topicName);
 			query.add(term);
 			searcher = new IndexSearcher(FSDirectory.getDirectory(getSearchIndexPath(virtualWiki), false));
 			// actually perform the search
 			Hits hits = searcher.search(query);
 			for (int i = 0; i < hits.length(); i++) {
 				SearchResultEntry result = new SearchResultEntry();
 				result.setRanking(hits.score(i));
 				result.setTopic(hits.doc(i).get(LuceneSearchEngine.ITYPE_TOPIC_PLAIN));
 				results.add(result);
 			}
 		} catch (Exception e) {
 			logger.severe("Exception while searching for " + topicName, e);
 		} finally {
 			if (searcher != null) {
 				try {
 					searcher.close();
 				} catch (Exception e) {}
 			}
 		}
 		return results;
 	}
 
 	/**
 	 * Find topics that contain any of the space delimited terms.
 	 * Note: Use this method for full text search.
 	 *
 	 * @param virtualWiki The virtual wiki to use
 	 * @param text The text to find
 	 *
 	 * @return A collection of SearchResultEntry, containing the search results
 	 */
 	public static Collection findMultiple(String virtualWiki, String text) {
 		StandardAnalyzer analyzer = new StandardAnalyzer();
 		Collection results = new Vector();
 		logger.fine("search text: " + text);
 		IndexSearcher searcher = null;
 		try {
 			BooleanQuery query = new BooleanQuery();
 			QueryParser qp;
 			qp = new QueryParser(ITYPE_TOPIC, analyzer);
 			query.add(qp.parse(text), Occur.SHOULD);
 			qp = new QueryParser(ITYPE_CONTENT, analyzer);
 			query.add(qp.parse(text), Occur.SHOULD);
 			searcher = new IndexSearcher(FSDirectory.getDirectory(getSearchIndexPath(virtualWiki), false));
 			// rewrite the query to expand it - required for wildcards to work with highlighter
 			Query rewrittenQuery = searcher.rewrite(query);
 			// actually perform the search
 			Hits hits = searcher.search(rewrittenQuery);
 			Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>"), new SimpleHTMLEncoder(), new QueryScorer(rewrittenQuery));
 			for (int i = 0; i < hits.length(); i++) {
 				String summary = retrieveResultSummary(hits.doc(i), highlighter, analyzer);
 				SearchResultEntry result = new SearchResultEntry();
 				result.setRanking(hits.score(i));
 				result.setTopic(hits.doc(i).get(LuceneSearchEngine.ITYPE_TOPIC_PLAIN));
 				result.setSummary(summary);
 				results.add(result);
 			}
 		} catch (Exception e) {
 			logger.severe("Exception while searching for " + text, e);
 		} finally {
 			if (searcher != null) {
 				try {
 					searcher.close();
 				} catch (Exception e) {}
 			}
 		}
 		return results;
 	}
 
 	/**
 	 * Get the path, which holds all index files
 	 */
 	private static String getSearchIndexPath(String virtualWiki) {
 		File parent = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR), SEARCH_DIR);
 		File child = new File(parent.getPath(), "index" + virtualWiki + File.separator);
 		if (!child.exists()) {
 			child.mkdirs();
 			IndexWriter writer = null;
 			try {
 				// create the search instance
 				FSDirectory directory = FSDirectory.getDirectory(getSearchIndexPath(virtualWiki), true);
 				StandardAnalyzer analyzer = new StandardAnalyzer();
 				writer = new IndexWriter(directory, analyzer, true);
 				directory.close();
 			} catch (Exception e) {
 				logger.severe("Unable to create search instance " + child.getPath(), e);
 			} finally {
 				try {
 					if (writer != null) {
 						writer.close();
 					}
 				} catch (Exception e) {
 					logger.severe("Exception during close", e);
 				}
 			}
 		}
 		return child.getPath();
 	}
 
 	/**
 	 * Trawls all the files in the wiki directory and indexes them
 	 */
 	public static synchronized void refreshIndex() throws Exception {
 		Collection allWikis = WikiBase.getHandler().getVirtualWikiList();
 		Topic topic;
 		for (Iterator iterator = allWikis.iterator(); iterator.hasNext();) {
 			long start = System.currentTimeMillis();
 			int count = 0;
 			VirtualWiki virtualWiki = (VirtualWiki)iterator.next();
 			File indexFile = new File(LuceneSearchEngine.getSearchIndexPath(virtualWiki.getName()));
 			// initially create index in ram
 			RAMDirectory ram = new RAMDirectory();
 			StandardAnalyzer analyzer = new StandardAnalyzer();
 			KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
 			IndexWriter writer = null;
 			try {
 				writer = new IndexWriter(ram, analyzer, true);
 				Collection topicNames = WikiBase.getHandler().getAllTopicNames(virtualWiki.getName());
 				for (Iterator iter = topicNames.iterator(); iter.hasNext();) {
 					String topicName = (String)iter.next();
 					topic = WikiBase.getHandler().lookupTopic(virtualWiki.getName(), topicName);
 					Document standardDocument = createStandardDocument(topic);
 					if (standardDocument != null) writer.addDocument(standardDocument);
 					// FIXME - parsing all documents will be intolerably slow with even a
 					// moderately large Wiki
 					ParserOutput parserOutput = Utilities.parserOutput(topic.getTopicContent());
 					Document keywordDocument = createKeywordDocument(topic, parserOutput.getLinks());
 					if (keywordDocument != null) writer.addDocument(keywordDocument, keywordAnalyzer);
 					count++;
 				}
 			} catch (Exception ex) {
 				logger.severe("Failure while refreshing search index", ex);
 			} finally {
 				try {
 					if (writer != null) {
 						writer.optimize();
 					}
 				} catch (Exception e) {
 					logger.severe("Exception during optimize", e);
 				}
 				try {
 					if (writer != null) {
 						writer.close();
 					}
 				} catch (Exception e) {
 					logger.severe("Exception during close", e);
 				}
 			}
 			// write back to disc
 			copyRamIndexToFileIndex(ram, indexFile);
 			logger.info("Rebuilt search index for " + virtualWiki.getName() + " (" + count + " documents) in " + ((System.currentTimeMillis() - start) / 1000.000) + " seconds");
 		}
 	}
 
 	/**
 	 *
 	 */
 	private static String retrieveResultSummary(Document document, Highlighter highlighter, StandardAnalyzer analyzer) throws Exception {
 		String content = document.get(ITYPE_CONTENT_PLAIN);
 		TokenStream tokenStream = analyzer.tokenStream(ITYPE_CONTENT_PLAIN, new StringReader(content));
 		String summary = highlighter.getBestFragments(tokenStream, content, 3, "...");
 		if (!StringUtils.hasText(summary) && StringUtils.hasText(content)) {
 			summary = Utilities.escapeHTML(content.substring(0, Math.min(200, content.length())));
 			if (Math.min(200, content.length()) == 200) summary += "...";
 		}
 		return summary;
 	}
 }
