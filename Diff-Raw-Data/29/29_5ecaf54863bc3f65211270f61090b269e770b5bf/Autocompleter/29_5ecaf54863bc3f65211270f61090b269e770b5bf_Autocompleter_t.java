 package com.gentics.cr.lucene.autocomplete;
 
 import java.io.IOException;
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
 
 /**
  * This class can be used to build an autocomplete index over an existing lucene index.
  * 
  * Last changed: $Date: 2010-04-01 15:20:21 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 528 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class Autocompleter implements IEventReceiver{
 
 	protected static final Logger log = Logger.getLogger(Autocompleter.class);
 	private LuceneIndexLocation source;
 	private LuceneIndexLocation autocompleteLocation;
 	
 	private static final String GRAMMED_WORDS_FIELD ="grammedwords";
 	public static final String COUNT_FIELD="count";
 	public static final String SOURCE_WORD_FIELD ="word";
 	
 	private static final String SOURCE_INDEX_KEY="srcindexlocation";
 	private static final String AUTOCOMPLETE_INDEY_KEY="autocompletelocation";
 	
 	private static final String AUTOCOMPLETE_FIELD_KEY="autocompletefield";
 	
 	private String autocompletefield = "content";
 	
 	public Autocompleter(CRConfig config)
 	{
 		GenericConfiguration src_conf = (GenericConfiguration)config.get(SOURCE_INDEX_KEY);
 		GenericConfiguration auto_conf = (GenericConfiguration)config.get(AUTOCOMPLETE_INDEY_KEY);
 		source = LuceneIndexLocation.getIndexLocation(new CRConfigUtil(src_conf,"SOURCE_INDEX_KEY"));
 		autocompleteLocation = LuceneIndexLocation.getIndexLocation(new CRConfigUtil(auto_conf,AUTOCOMPLETE_INDEY_KEY));
 		
 		String s_autofield = config.getString(AUTOCOMPLETE_FIELD_KEY);
 		if(s_autofield!=null)this.autocompletefield=s_autofield;
 		
 		try
 		{
 			reIndex();
 		}
 		catch(IOException e)
 		{
 			log.error("Could not create autocomplete index.", e);
 		}
		EventManager.getInstance().register(this);
 	}
 	
 	
 	public void processEvent(Event event) {
 		if(IndexingFinishedEvent.INDEXING_FINISHED_EVENT_TYPE.equals(event.getType()))
 		{
 			try
 			{
 				reIndex();
 			}
 			catch(IOException e)
 			{
 				log.error("Could not reindex autocomplete index.", e);
 			}
 		}
 	}
 	
 	public Collection<CRResolvableBean> suggestWords(CRRequest request) throws IOException
 	{
 		ArrayList<CRResolvableBean> result = new ArrayList<CRResolvableBean>();
 		String term = request.getRequestFilter();
 		// get the top 5 terms for query 
 		
 		IndexAccessor ia = autocompleteLocation.getAccessor();
 		Searcher autoCompleteSearcher = ia.getPrioritizedSearcher();
 		IndexReader autoCompleteReader = ia.getReader(false);
 		try{
 			Query query = new TermQuery(new Term(GRAMMED_WORDS_FIELD, term)); 
 	        Sort sort = new Sort(new SortField(COUNT_FIELD,SortField.LONG, true)); 
 	        TopDocs docs = autoCompleteSearcher.search(query, null, 5, sort); 
 	        for (ScoreDoc doc : docs.scoreDocs) {
 	        	CRResolvableBean bean = new CRResolvableBean();
 	        	Document d = autoCompleteReader.document(doc.doc);
 	        	bean.set(SOURCE_WORD_FIELD, d.get(SOURCE_WORD_FIELD));
 	            bean.set(COUNT_FIELD, d.get(COUNT_FIELD));
 	            result.add(bean);
 	        } 
 		}finally{
 			ia.release(autoCompleteSearcher);
 	        ia.release(autoCompleteReader, false);	
 		}
         
         
         return result;
 	}
 	
 	private void reIndex() throws IOException
 	{
 		// build a dictionary (from the spell package) 
 		log.debug("Starting to reindex autocomplete index.");
 		IndexAccessor sia = this.source.getAccessor();
         IndexReader sourceReader = sia.getReader(false);
         LuceneDictionary dict = new LuceneDictionary(sourceReader, this.autocompletefield); 
         IndexAccessor aia = this.autocompleteLocation.getAccessor();
         //IndexReader reader = aia.getReader(false);
         IndexWriter writer = aia.getWriter();
         
         try
         {
 	        writer.setMergeFactor(300); 
 	        writer.setMaxBufferedDocs(150); 
 	        // go through every word, storing the original word (incl. n-grams)  
 	        // and the number of times it occurs 
 	        //CREATE WORD LIST FROM SOURCE INDEX
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
 	        //DELETE OLD OBJECTS FROM INDEX
 	        writer.deleteAll();
 	        
 	        //UPDATE DOCUMENTS IN AUTOCOMPLETE INDEX
 	        for (String word : wordsMap.keySet()) { 
 	                // ok index the word
 	                Document doc = new Document(); 
 	                doc.add(new Field(SOURCE_WORD_FIELD, word, Field.Store.YES,Field.Index.NOT_ANALYZED_NO_NORMS)); // orig term 
 	                doc.add(new Field(GRAMMED_WORDS_FIELD, word, Field.Store.YES,Field.Index.ANALYZED)); // grammed 
 	                doc.add(new Field(COUNT_FIELD,Integer.toString(wordsMap.get(word)), Field.Store.YES,Field.Index.NOT_ANALYZED_NO_NORMS)); // count 
 	                writer.addDocument(doc); 
 	        } 
 	        writer.optimize(); 
         }
         finally{
 	                
 	        sia.release(sourceReader, false);
 	        // close writer 
 	        
 	        aia.release(writer);
 	      //  aia.release(reader,false);
         }
         autocompleteLocation.createReopenFile();
         log.debug("Finished reindexing autocomplete index.");
 	}
 	
 	public void finalize()
 	{
 		source.finalize();
 		autocompleteLocation.finalize();
		EventManager.getInstance().unregister(this);
 	}
 
 }
