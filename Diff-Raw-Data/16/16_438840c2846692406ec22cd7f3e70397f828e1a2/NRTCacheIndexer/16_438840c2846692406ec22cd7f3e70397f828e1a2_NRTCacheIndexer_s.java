 package eu.monnetproject.translation.sources.cache;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.SearcherFactory;
 import org.apache.lucene.search.SearcherManager;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.TopScoreDocCollector;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.SimpleFSDirectory;
 import org.apache.lucene.util.Version;
 
 import eu.monnetproject.lang.Language;
 import eu.monnetproject.translation.monitor.Messages;
 import eu.monnetproject.translation.sources.common.Indexer;
 import eu.monnetproject.translation.sources.common.Pair;
 
 public class NRTCacheIndexer {
 
 	private final double ramBuffer = 256.0;
 	private String indicesDirPath;
 	private String indexPath;	
 	private File indexDir;
 	private	static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);			
 	private Properties config;
 	private Language lang1;
 	private Language lang2;
 	private SearcherManager searchMgr;
 	private Indexer indexer;
 	private boolean searcherOnly = false;
 
 	public NRTCacheIndexer(Properties config, Language lang1, Language lang2, boolean searcherOnly) {		
 		this.config = config;	
 		this.lang1 = lang1;
 		this.lang2 = lang2;
 		this.searcherOnly = searcherOnly;
 		getConfig();
 		if(!this.searcherOnly) {
 			openWriter();	
 			openSearchManager(true);
 		} else {		
 			openSearchManager(false);
 		}
 	}
 
 	public void close() {
 		try {
 			searchMgr.close();
 			if(!searcherOnly)
 				indexer.closeIndexer();
 			Messages.info("Closing IATE Source");
 		} catch (IOException e) {
 			//	e.printStackTrace();
 			Messages.warning("Error while closing IATE source in NRTCacheIndexer.java, indexer seems to be null");
 		}
 	}
 
 	private static Analyzer getAnalyzer() {
 		return analyzer;
 	}
 
	private void openSearchManager(boolean withWriter) {
 		try {
 			if(withWriter)
 				searchMgr = new SearcherManager(indexer.getWriter(), false, new SearcherFactory());
 			else {
 				Directory index = getIndex(indexPath);	
 				searchMgr = new SearcherManager(index, new SearcherFactory());
 			}
 		} catch (IOException e) {
 			//e.printStackTrace();
			Messages.warning("Error while opening search manager in NRTCacheIndexer.java, index doesn't seem to exist in the path provided");			
		}		
 	}
 
 	private boolean openWriter() {		
 		try {
 			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, getAnalyzer());
 			config.setWriteLockTimeout(10 * 1000);
 			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
 			Directory index = getIndex(indexPath);
 			if(IndexReader.indexExists(index)) 
 				config.setOpenMode(IndexWriterConfig.OpenMode.APPEND);			
 			config.setRAMBufferSizeMB(ramBuffer);
 			indexer = new Indexer(config, index);
 		}
 		catch (IOException e) {
 			//e.printStackTrace();
 			Messages.warning("Error while opening indexWriter in NRTCacheIndexer.java, index doesn't seem to exist in the path provided");	
 			return false;
 		}			
 		return true;
 	}
 
 	private void getConfig() {
 		indicesDirPath = config.getProperty("indicesDirPath");
 		indexPath = indicesDirPath + "/" + lang1.getIso639_1() + "-" + lang2.getIso639_1();			
 		indexDir = new File(indexPath);
 		if(!indexDir.exists()) 
 			indexDir.mkdirs();			
 	}
 
 	public void cache(String lang1String, String lang2String, String context, String translationSource) {
 		Set<String> cachedTranslations = getTranslations(lang1String, context);
 		boolean cache = true;
 		if(cachedTranslations != null)
 			if(cachedTranslations.contains(lang2String))
 				cache = false;		
 		if(cache==true){
 			CachedTranslationSourceLucDoc doc = new CachedTranslationSourceLucDoc();
 			doc.addField(lang1, lang2);		
 			doc.addTranslation(lang1String, lang2String, translationSource, context);			
 			indexer.addDoc(doc.getLucDoc());								
 			try {
 				searchMgr.maybeRefresh();
 			} catch (IOException e) {
 				Messages.warning("Error while refreshing search mangager in NRTCacheIndexer.java");				
 				//		e.printStackTrace();	
 			}
 		}
 	}
 
 	private TopScoreDocCollector search(String queryString, String field, IndexSearcher searcher) throws IOException{
 		TopScoreDocCollector collector = TopScoreDocCollector.create(50, true);		
 		queryString = QueryParser.escape(queryString);		
 		QueryParser queryParser = new QueryParser(Version.LUCENE_36, field, analyzer);
 		Query query = null;
 		try {
 			query = queryParser.parse(queryString);
 			searcher.search(query, collector);		
 		} catch (ParseException e) {
 			Messages.warning("Error while parsing the query in NRTCacheIndexer.java, query may contain illegal characters");							
 			//	e.printStackTrace();
 			collector = null;
 		}						
 		return collector;				
 	}
 
 	public Set<String> getTranslations(String lang1String) {
 		Set<String> translations = new HashSet<String>();		
 		IndexSearcher searcher = searchMgr.acquire();
 		if(searcher == null) 
 			return null;				
 		try {
 			try {
 				TopScoreDocCollector collector = search("\"" + lang1String + "\"", CachedTranslationSourceLucDoc.getFieldNameLang(lang1), searcher);
 				TopDocs topDocs = collector.topDocs();
 				ScoreDoc[] scoreDocs = topDocs.scoreDocs;
 				for(int i=0; i<scoreDocs.length; i++) {
 					int docID = scoreDocs[i].doc;
 					Document doc = searcher.doc(docID);
 					String translation = doc.get(CachedTranslationSourceLucDoc.getFieldNameLang(lang2));
 					String storedChunk = doc.get(CachedTranslationSourceLucDoc.getFieldNameLang(lang1));
 					if(storedChunk.trim().equalsIgnoreCase(lang1String.trim()))
 						translations.add(translation);	
 					
 		//			if(translations.size()==0)
 			//			translations = null;		
 				}				
 			} catch (IOException e) {
 				Messages.warning("Error while searching the query in NRTCacheIndexer.java, index may not exist");										
 				//		e.printStackTrace();
 				translations = null;
 			}			
 		}
 		finally {
 			try {
 				searchMgr.release(searcher);
 			} catch (IOException e) {
 				Messages.warning("Error while releasing the current searher by search manager in NRTCacheIndexer.java");										
 				//	e.printStackTrace();
 				translations = null;
 			}
 			searcher = null;
 		}
 		if(translations.size() == 0)
 			return null;
 		return translations;
 	}
 
 	private String[] getQuotedArray(String[] array) {
 		int i = 0;
 		for(String string : array) { 
 			string = "\"" + string + "\"";
 			array[i++] = string;
 		}
 		return array;	
 	}
 
 	private TopScoreDocCollector multiFieldTermQuerySearch(
 			List<Pair<String, String>> queryWithFieldList, IndexSearcher searcher) {
 		TopScoreDocCollector collector = TopScoreDocCollector.create(50, true);				
 		String[] queries = new String[queryWithFieldList.size()];
 		String[] fields = new String[queryWithFieldList.size()];
 		int i=0;
 		for(Pair<String, String> query : queryWithFieldList) {
 			String queryString = query.getFirst();
 			String fieldName = query.getSecond();
 			queries[i] = queryString;
 			fields[i] = fieldName;
 			i++;
 		}		
 		queries = getQuotedArray(queries);			
 		BooleanClause.Occur[] flags = {BooleanClause.Occur.MUST,
 				BooleanClause.Occur.MUST};
 		Query query = null;
 		try {
 			query = MultiFieldQueryParser.parse(Version.LUCENE_36, queries, fields, flags, analyzer);
 			try {
 				searcher.search(query, collector);
 			} catch (IOException e) {
 				Messages.warning("Error while searching the multifield query in NRTCacheIndexer.java");														
 				//	e.printStackTrace();
 				return null;
 			}					
 		} catch (ParseException e) {
 			Messages.warning("Error while parsing the multifield query in NRTCacheIndexer.java");			
 			//	e.printStackTrace();
 			return null;
 		}
 		return collector;
 	}
 
 
 	public Set<String> getTranslations(String lang1String, String context) {
 		Set<String> translations = new HashSet<String>();				
 		if(context.equalsIgnoreCase("domainall") || context.equalsIgnoreCase("")) 
 			return getTranslations(lang1String);
 		IndexSearcher searcher = searchMgr.acquire();		
 		if(searcher==null) 
 			return null;
 		try {
 			try {
 				List<Pair<String, String>> queryWithFieldList = new ArrayList<Pair<String, String>>();
 				Pair<String, String> query1 = new Pair<String, String>(QueryParser.escape(lang1String), CachedTranslationSourceLucDoc.getFieldNameLang(lang1));
 				Pair<String, String> query2 = new Pair<String, String>(QueryParser.escape(context), CachedTranslationSourceLucDoc.getFieldNameContext());
 				queryWithFieldList.add(query1);
 				queryWithFieldList.add(query2);
 				TopScoreDocCollector docCollector = multiFieldTermQuerySearch(queryWithFieldList, searcher);
 				TopDocs topDocs = docCollector.topDocs();
 				ScoreDoc[] scoreDocs = topDocs.scoreDocs;
 				for(int i=0; i<scoreDocs.length; i++) {		
 					int docID = scoreDocs[i].doc;
 					Document doc = searcher.doc(docID);
 					String translation = doc.get(CachedTranslationSourceLucDoc.getFieldNameLang(lang2));
 					translations.add(translation);
 				}
 				if(translations.size()==0)
 					translations = null;							
 			} catch (IOException e) {
 				Messages.warning("Error while searching the multifield query in NRTCacheIndexer.java");																	
 				//	e.printStackTrace();
 				translations = null;
 			}			
 		}
 		finally {
 			try {
 				searchMgr.release(searcher);
 			} catch (IOException e) {
 				Messages.warning("Error while releasing the current searcher by search manager in NRTCacheIndexer.java");																	
 				//	e.printStackTrace();
 				translations = null;
 			}
 			searcher = null;
 		}		
 		return translations;
 	}
 
 	private Directory getIndex(String indexPath) {
 		Directory index = null;
 		try {
 			index = new SimpleFSDirectory(new File(indexPath + 
 					System.getProperty("file.separator")));
 		} catch (IOException e) {
 			Messages.warning("Error while opening/getting the indices in NRTCacheIndexer.java, the index may not exist in the path provided");																	
 			//	e.printStackTrace();
 		}		
 		return index;
 	}
 
 }
