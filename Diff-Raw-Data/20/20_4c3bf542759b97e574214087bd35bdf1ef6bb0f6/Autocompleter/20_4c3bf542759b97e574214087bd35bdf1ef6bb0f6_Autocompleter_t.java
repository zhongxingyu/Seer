 package com.gentics.cr.lucene.autocomplete;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.Searcher;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.spell.LuceneDictionary;
 import org.apache.lucene.store.Directory;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.events.Event;
 import com.gentics.cr.events.EventManager;
 import com.gentics.cr.events.IEventReceiver;
 import com.gentics.cr.lucene.events.IndexingFinishedEvent;
 import com.gentics.cr.lucene.indexaccessor.IndexAccessor;
 import com.gentics.cr.lucene.indexer.index.LuceneIndexLocation;
 import com.gentics.cr.monitoring.MonitorFactory;
 import com.gentics.cr.monitoring.UseCase;
 import com.gentics.cr.util.indexing.IReIndexStrategy;
 import com.gentics.cr.util.indexing.IndexLocation;
 import com.gentics.cr.util.indexing.ReIndexNoSkipStrategy;
 
 /**
  * This class can be used to build an autocomplete index over an existing lucene
  * index.
  * 
  * from version 2.0.0 the {@link AutocompleteIndexExtension} is used for all
  * Index related tasks and the {@link Autocompleter} will only handle search
  * requests.
  * 
  * Last changed: $Date: 2010-04-01 15:20:21 +0200 (Do, 01 Apr 2010) $
  * 
  * @version $Revision: 528 $
  * @author $Author: supnig@constantinopel.at $
  * @author Sebastian Vogel <s.vogel@gentics.com>
  */
 public class Autocompleter implements IEventReceiver, AutocompleteConfigurationKeys {
 
 	protected static final Logger log = Logger.getLogger(Autocompleter.class);
 	@Deprecated
 	private LuceneIndexLocation source;
 	private LuceneIndexLocation autocompleteLocation;
 
 	private String autocompletefield = "content";
 
 	@Deprecated
 	private boolean autocompletereopenupdate = false;
 
 	@Deprecated
 	private long lastupdatestored = 0;
 
 	@Deprecated
 	private IReIndexStrategy reindexStrategy;
 
 	/**
 	 * to keep backward compatibility - new implementations must declare in the
 	 * config if they use the new {@link AutocompleteIndexExtension} Class
 	 */
 	@Deprecated
 	private boolean useAutocompleteIndexExtension = false;
 
 	public Autocompleter(CRConfig config) {
 		GenericConfiguration srcConf = (GenericConfiguration) config.get(SOURCE_INDEX_KEY);
 		GenericConfiguration autoConf = (GenericConfiguration) config.get(AUTOCOMPLETE_INDEX_KEY);
 		useAutocompleteIndexExtension = config.getBoolean(
 			AUTOCOMPLETE_USE_AUTCOMPLETE_INDEXER,
 			useAutocompleteIndexExtension);
 
 		source = null;
 		if (!useAutocompleteIndexExtension) {
 			source = LuceneIndexLocation.getIndexLocation(new CRConfigUtil(srcConf, "SOURCE_INDEX_KEY"));
 		}
 		autocompleteLocation = LuceneIndexLocation
 				.getIndexLocation(new CRConfigUtil(autoConf, AUTOCOMPLETE_INDEX_KEY));
 		autocompleteLocation.registerDirectoriesSpecial();
 		String s_autofield = config.getString(AUTOCOMPLETE_FIELD_KEY);
 
 		if (!useAutocompleteIndexExtension) {
 			reindexStrategy = initReindexStrategy(config);
 		}
 		if (s_autofield != null)
 			this.autocompletefield = s_autofield;
 
 		String sReopenUpdate = config.getString(AUTOCOMPLETE_REOPEN_UPDATE);
 		if (sReopenUpdate != null) {
 			autocompletereopenupdate = Boolean.parseBoolean(sReopenUpdate);
 		}
 
 		if (!useAutocompleteIndexExtension) {
 			try {
 				// CHECK AND REMOVE LOCKING
 				autocompleteLocation.forceRemoveLock();
 				// REINDEX
 				reIndex();
 			} catch (IOException e) {
 				log.error("Could not create autocomplete index.", e);
 			}
 			EventManager.getInstance().register(this);
 		}
 	}
 
 	/**
 	 * from version 2.0.0 the {@link AutocompleteIndexExtension} is used for all
 	 * Index related tasks and the {@link Autocompleter} will only handle search
 	 * requests
 	 */
 	@Deprecated
 	public void processEvent(Event event) {
 		if (IndexingFinishedEvent.INDEXING_FINISHED_EVENT_TYPE.equals(event.getType())) {
 			IndexLocation il = (IndexLocation) event.getData();
 			if (!reindexStrategy.skipReIndex(il)) {
 				try {
 					reIndex();
 				} catch (IOException e) {
 					log.error("Could not reindex autocomplete index.", e);
 				}
 			}
 		}
 	}
 
 	public Collection<CRResolvableBean> suggestWords(CRRequest request) throws IOException {
 		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
 		String term = request.getRequestFilter();
 		// get the top 5 terms for query
 
 		if (autocompletereopenupdate || useAutocompleteIndexExtension) {
 			checkForUpdate();
 		}
 
 		IndexAccessor ia = autocompleteLocation.getAccessor();
 		Searcher autoCompleteSearcher = ia.getPrioritizedSearcher();
 		IndexReader autoCompleteReader = ia.getReader(false);
 		try {
 			Query query = new TermQuery(new Term(GRAMMED_WORDS_FIELD, term));
 			Sort sort = new Sort(new SortField(COUNT_FIELD, SortField.LONG, true));
 			TopDocs docs = autoCompleteSearcher.search(query, null, 5, sort);
			int id = 1;
 			for (ScoreDoc doc : docs.scoreDocs) {
				CRResolvableBean bean = new CRResolvableBean(id++);
 				Document d = autoCompleteReader.document(doc.doc);
 				bean.set(SOURCE_WORD_FIELD, d.get(SOURCE_WORD_FIELD));
 				bean.set(COUNT_FIELD, d.get(COUNT_FIELD));
 				result.add(bean);
 			}
 		} finally {
 			ia.release(autoCompleteSearcher);
 			ia.release(autoCompleteReader, false);
 		}
 
 		return result;
 	}
 
 	private void checkForUpdate() {
 
 		// Use the old checkForUpdate logic for backward compatibility if the
 		// AutocompleteIndexExtension is not used
 		if (!useAutocompleteIndexExtension) {
 			IndexAccessor ia = source.getAccessor();
 			boolean reopened = false;
 			try {
 				IndexReader reader = ia.getReader(false);
 				Directory dir = reader.directory();
 				try {
 					if (dir.fileExists("reopen")) {
 						long lastupdate = dir.fileModified("reopen");
 						if (lastupdate != lastupdatestored) {
 							reopened = true;
 							lastupdatestored = lastupdate;
 						}
 					}
 				} finally {
 					ia.release(reader, false);
 				}
 				if (reopened) {
 
 					reIndex();
 
 				}
 			} catch (IOException e) {
 				log.debug("Could not reIndex autocomplete index.", e);
 			}
 		} else {
 			// the new checkForUpdate Logic only calls reopenCheck on the
 			// IndexLocation
 			IndexAccessor ia = autocompleteLocation.getAccessor();
 			autocompleteLocation.reopenCheck(ia);
 		}
 	}
 
 	/**
 	 * from version 2.0.0 the {@link AutocompleteIndexExtension} is used for all
 	 * Index related tasks and the {@link Autocompleter} will only handle search
 	 * requests
 	 */
 	@Deprecated
 	private synchronized void reIndex() throws IOException {
 		UseCase ucReIndex = MonitorFactory.startUseCase("reIndex()");
 		// build a dictionary (from the spell package)
 		log.debug("Starting to reindex autocomplete index.");
 		IndexAccessor sia = this.source.getAccessor();
 		IndexReader sourceReader = sia.getReader(false);
 		LuceneDictionary dict = new LuceneDictionary(sourceReader, this.autocompletefield);
 		IndexAccessor aia = this.autocompleteLocation.getAccessor();
 		// IndexReader reader = aia.getReader(false);
 		IndexWriter writer = aia.getWriter();
 
 		try {
 			writer.setMergeFactor(300);
 			writer.setMaxBufferedDocs(150);
 			// go through every word, storing the original word (incl. n-grams)
 			// and the number of times it occurs
 			// CREATE WORD LIST FROM SOURCE INDEX
 			Map<String, Integer> wordsMap = new HashMap<String, Integer>();
 			Iterator<String> iter = (Iterator<String>) dict.getWordsIterator();
 			while (iter.hasNext()) {
 				String word = iter.next();
 				int len = word.length();
 				if (len < 3) {
 					continue; // too short we bail but "too long" is fine...
 				}
 				if (wordsMap.containsKey(word)) {
 					throw new IllegalStateException("Lucene returned a bad word list");
 				} else {
 					// use the number of documents this word appears in
 					wordsMap.put(word, sourceReader.docFreq(new Term(autocompletefield, word)));
 				}
 			}
 			// DELETE OLD OBJECTS FROM INDEX
 			writer.deleteAll();
 
 			// UPDATE DOCUMENTS IN AUTOCOMPLETE INDEX
 			for (String word : wordsMap.keySet()) {
 				// ok index the word
 				Document doc = new Document();
 				doc.add(new Field(SOURCE_WORD_FIELD, word, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS)); // orig term
 				doc.add(new Field(GRAMMED_WORDS_FIELD, word, Field.Store.YES, Field.Index.ANALYZED)); // grammed
 				doc.add(new Field(COUNT_FIELD, Integer.toString(wordsMap.get(word)), Field.Store.YES,
 						Field.Index.NOT_ANALYZED_NO_NORMS)); // count
 				writer.addDocument(doc);
 			}
 			writer.optimize();
 		} finally {
 
 			sia.release(sourceReader, false);
 			// close writer
 
 			aia.release(writer);
 			// aia.release(reader,false);
 		}
 		autocompleteLocation.createReopenFile();
 		log.debug("Finished reindexing autocomplete index.");
 		ucReIndex.stop();
 	}
 
 	public void finalize() {
 		autocompleteLocation.stop();
 		if (!useAutocompleteIndexExtension) {
 			source.stop();
 			EventManager.getInstance().unregister(this);
 		}
 	}
 
 	/**
 	 * from version 2.0.0 the {@link AutocompleteIndexExtension} is used for all
 	 * Index related tasks and the {@link Autocompleter} will only handle search
 	 * requests
 	 * 
 	 * Initialize a config class for the periodical execution flag of the
 	 * indexer. If init of the configured class fails, a fallback class is
 	 * returned.
 	 * 
 	 * @return configclass
 	 * @param config
 	 */
 	@Deprecated
 	private IReIndexStrategy initReindexStrategy(final CRConfig config) {
 		String className = config.getString(REINDEXSTRATEGYCLASS_KEY);
 
 		if (className != null && className.length() != 0) {
 			try {
 				Class<?> clazz = Class.forName(className);
 				Constructor<?> constructor = clazz.getConstructor(CRConfig.class);
 				return (IReIndexStrategy) constructor.newInstance(config);
 			} catch (Exception e) {
 				log.warn("Cound not init configured " + REINDEXSTRATEGYCLASS_KEY + ": " + className, e);
 			}
 		}
 		return new ReIndexNoSkipStrategy(config);
 	}
 
 }
