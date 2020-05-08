 package com.technosophos.rhizome.repository.lucene;
 
 import com.technosophos.rhizome.repository.RepositorySearcher;
 import com.technosophos.rhizome.repository.RepositoryContext;
 import com.technosophos.rhizome.repository.RepositoryAccessException;
 import com.technosophos.rhizome.repository.DocumentRepository;
 import com.technosophos.rhizome.repository.SearchResults;
 import com.technosophos.rhizome.document.DocumentCollection;
 import com.technosophos.rhizome.document.DocumentList;
 import com.technosophos.rhizome.document.Metadatum;
 import com.technosophos.rhizome.document.ProxyRhizomeDocument;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.TermDocs;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.MapFieldSelector;
 import org.apache.lucene.document.SetBasedFieldSelector;
 import org.apache.lucene.search.IndexSearcher; // For simpleSearch
 import org.apache.lucene.queryParser.MultiFieldQueryParser;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.search.Hits;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.document.Document;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.List;
 import java.util.Arrays;
 //import java.util.Set;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.io.File;
 import java.io.IOException;
 
 import static com.technosophos.rhizome.repository.lucene.LuceneElements.*;
 
 public class LuceneSearcher implements RepositorySearcher {
 
 	//public static String LUCENE_INDEX_PATH_PARAM = "indexpath";
 	
 	public static final String SIMPLE_SEARCH_FIELDS = "fields";
 	public static final String SIMPLE_SEARCH_SEARCH_BODY = "search_body";
 
 	private RepositoryContext context;
 	private String indexName = null;
 	//private String indexLocation = null;
 	
 	public LuceneSearcher(String indexName) {
 		this(indexName, new RepositoryContext());
 	}
 	
 	public LuceneSearcher(String indexName, RepositoryContext cxt) {
 		this.indexName = indexName;
 		this.context = cxt;
 		//this.indexLocation = LuceneIndexer.getIndexPath(indexName, cxt);
 	}
 	
 	/**
 	 * Get an array containing metadata names.
 	 * Metadata has two main parts: the name and the list of values. This method
 	 * retrieves a complete list of unique names in the database.
 	 * @return Array of metadata names
 	 */
 	public String []  getMetadataNames() throws RepositoryAccessException {
 		String [] fields = null;
 		IndexReader lreader = null;
 		try {
 			/*
 			File indexDir = new File(this.context.getParam(LUCENE_INDEX_PATH_PARAM));
 			lreader = IndexReader.open(indexDir);
 			*/
 			lreader = this.getIndexReader();
 			Collection<String> c = lreader.getFieldNames(IndexReader.FieldOption.ALL);
 			fields = new String[c.size()];
 			Iterator<String> it = c.iterator();
 			for(int i = 0; i < c.size(); ++i) {
 				fields[i] = it.next().toString();
 			}
 			lreader.close();
 		} catch (java.io.IOException ioe) {
 
 			throw new RepositoryAccessException("IOException: " + ioe.getMessage());
 		} finally {
 			if(lreader != null) {
 				try{ lreader.close(); } catch (java.io.IOException ioe) {}
 			}
 		}
 		return fields;
 	}
 	
 	public Map<String,Integer>  getMetadataValues(String mdName) throws RepositoryAccessException {
 		HashMap<String, Integer> map = new HashMap<String, Integer>();
 		
 		MapFieldSelector fieldSelector = new MapFieldSelector(new String [] {mdName});
 		IndexReader lreader = null;
 		try {
 			lreader = this.getIndexReader();
 			int last = lreader.maxDoc();
 			
 			Document d;
 			String[] vals;
 			for(int i = 0; i < last; ++i) {
 				if(!lreader.isDeleted(i)) {
 					d = lreader.document(i, fieldSelector);
 					
 					vals = d.getValues(mdName);
 					// If this MD name exists, we want to put it in the Map, incrementing the 
 					// counter (or setting it to 1 if the entry is new)
 					if(vals != null && vals.length > 0) {
 						for(String v: vals) {
 							map.put(v,
 									map.containsKey(v) ? map.get(v) + 1 : 1
 							);
 						}
 					}
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			if(lreader != null) {
 				try{ lreader.close(); } catch (java.io.IOException ioe) {}
 			}
 		}
 		
 		return map;
 	}
 	
 	/**
 	 * Get all docIDs that have the specified name and value.
 	 * Get an array of document IDs for documents that contain the metadatum
 	 * with the name <code>name</code> and one of the values matches <code>value</code>.
 	 * @param name metadatum name
 	 * @param value value to search for in <code>name</code> metadata.
 	 * @return array of matching document IDs.
 	 */
 	public String [] getDocIDsByMetadataValue(String name, String value) throws RepositoryAccessException {
 		
 		ArrayList<String> docIDs = new ArrayList<String>();
 		String [] fields = {name, LUCENE_DOCID_FIELD};
 		
 		MapFieldSelector fieldSelector = new MapFieldSelector(fields);
 		IndexReader lreader = null;
 		
 		try {
 			lreader = this.getIndexReader();
 			int last = lreader.maxDoc();
 			Document d;
 			for(int i = 0; i < last; ++i) {
 				if(!lreader.isDeleted(i)) {
 					d = lreader.document(i, fieldSelector);
 					if(this.checkFieldValueMatches(name, value, d))
 						docIDs.add(d.get(LUCENE_DOCID_FIELD));	
 				}
 			}
 			lreader.close();
 		} catch (java.io.IOException ioe) {
 			throw new RepositoryAccessException("IOException: " + ioe.getMessage());
 		} finally {
 			if(lreader != null) {
 				try{ lreader.close(); } catch (java.io.IOException ioe) {}
 			}
 		}
 		
 		return docIDs.toArray(new String[docIDs.size()]);
 	}
 	
 	/**
 	 * Get metadatum values.
 	 * Given the name of a metadatum and the document ID for a document,
 	 * this gets the associated values for that metadatum.
 	 * @param name Name of metadata to get value for.
 	 * @param docID Name of the document to fetch
 	 * @return Metadatum containing metadata values.
 	 * @throws RepositoryAccessException if there is an underlying IO issue.
 	 */
 	public Metadatum getMetadatumByDocID(String name, String docID) 
 			throws RepositoryAccessException {
 		Metadatum md = null;
 		String [] fields = {name};
 		
 		MapFieldSelector fsel = new MapFieldSelector(fields);
 		IndexReader lreader = null;
 		TermDocs td = null;
 		
 		try {
 			lreader = this.getIndexReader();
 			td = lreader.termDocs(new Term(LUCENE_DOCID_FIELD,docID));
 			while(td.next()) {
 				Document d = lreader.document(td.doc(), fsel);
 				md = new Metadatum(name, d.getValues(name));
 			}
 		} catch (java.io.IOException ioe) {
 			throw new RepositoryAccessException("IOException: " + ioe.getMessage());
 		} finally {
 			try {
 				if(lreader != null) lreader.close(); 
 				if(td != null) td.close();
 			} catch (java.io.IOException ioe) {}
 		}
 		
 		return md;
 	}
 	
 	public String[] getReverseRelatedDocuments(String docID) throws RepositoryAccessException {
 		// We want to search for any docs that contain a relation to DocID.
 		ArrayList<String> docIDs = new ArrayList<String>();
 		String [] fields = {LUCENE_RELATION_FIELD, LUCENE_DOCID_FIELD};
 		
 		MapFieldSelector fieldSelector = new MapFieldSelector(fields);
 		IndexReader lreader = null;
 		
 		try {
 			lreader = this.getIndexReader();
 			int last = lreader.maxDoc();
 			Document d;
 			for(int i = 0; i < last; ++i) {
 				if(!lreader.isDeleted(i)) {
 					d = lreader.document(i, fieldSelector);
 					if(this.checkFieldValueMatches(LUCENE_RELATION_FIELD, docID, d))
 						docIDs.add(d.get(LUCENE_DOCID_FIELD));	
 				}
 			}
 			lreader.close();
 		} catch (java.io.IOException ioe) {
 			throw new RepositoryAccessException("IOException: " + ioe.getMessage());
 		} finally {
 			if(lreader != null) {
 				try{ lreader.close(); } catch (java.io.IOException ioe) {}
 			}
 		}
 		
 		return docIDs.toArray(new String[docIDs.size()]);
 	}
 	
 	public String[] getReverseRelatedDocuments(String docID, String relationType)  
 			throws RepositoryAccessException {
 		ArrayList<String> docIDs = new ArrayList<String>();
 		String [] fields = {LUCENE_RELATION_FIELD, LUCENE_DOCID_FIELD};
 		
 		//String value = docID + RELATION_SEPARATOR + docID;
 		String value = docID + RELATION_SEPARATOR + relationType;
 		
 		MapFieldSelector fieldSelector = new MapFieldSelector(fields);
 		IndexReader lreader = null;
 		
 		try {
 			lreader = this.getIndexReader();
 			int last = lreader.maxDoc();
 			Document d;
 			for(int i = 0; i < last; ++i) {
 				if(!lreader.isDeleted(i)) {
 					d = lreader.document(i, fieldSelector);
 					if(this.checkFieldValueMatches(LUCENE_RELATION_FIELD, value, d))
 						docIDs.add(d.get(LUCENE_DOCID_FIELD));	
 				}
 			}
 			lreader.close();
 		} catch (java.io.IOException ioe) {
 			throw new RepositoryAccessException("IOException: " + ioe.getMessage());
 		} finally {
 			if(lreader != null) {
 				try{ lreader.close(); } catch (java.io.IOException ioe) {}
 			}
 		}
 		
 		return docIDs.toArray(new String[docIDs.size()]);
 	}
 	
 	/**
 	 * Returns a map of document IDs and values.
 	 * Given a metadata name, this returns a map where the key is the document
 	 * ID for a document with that metdatum name, and the value is the list of
 	 * values (as a <code>String []</code>) for that metadata.
 	 * @param name
 	 * @return Map of documentID->String['val1','val2'...]
 	 */
 	public java.util.Map<String, String[]> getMetadataByName(String name) throws RepositoryAccessException {
 		HashMap<String,String[]> vals = new HashMap<String, String[]>();
 		String [] fields = {LUCENE_DOCID_FIELD, name};
 		
 		MapFieldSelector fieldSelector = new MapFieldSelector(fields);
 		IndexReader lreader = null;
 		
 		try {
 			lreader = this.getIndexReader();
 			int last = lreader.maxDoc();
 			Document d;
 			for(int i = 0; i < last; ++i) {
 				if(!lreader.isDeleted(i)) {
 					d = lreader.document(i, fieldSelector);
 					if(d.getField(name) != null)
 						vals.put(d.get(LUCENE_DOCID_FIELD), d.getValues(name));
 				}
 				
 			}
 			//lreader.close();
 		} catch (java.io.IOException ioe) {
 			throw new RepositoryAccessException("IOException: " + ioe.getMessage());
 		} finally {
 			if(lreader != null) {
 				try{ lreader.close(); } catch (java.io.IOException ioe) {}
 			}
 		}
 		
 		return vals;
 	}
 	
 	/**
 	 * Returns a DocumentCollection of document IDs and metadata.
 	 * <p>Given a metadata name and an array of document IDs, 
 	 * this returns a DocumentCollection where the key is the document
 	 * ID for a document with that metdatum name, and the value is the list of
 	 * values (as a <code>String []</code>) for that metadata.</p>
 	 * <p>This search ONLY checks for metdata in the document IDs given in the 
 	 * <code>docs[]</code> array.</p>
 	 * @see com.technosophos.rhizome.document.DocumentCollection
 	 * @param name metadatum name to search for
 	 * @param docs array of document IDs to search
 	 * @deprecated Use {@link getMetadataByName(String, String[], DocumentRepository)}.
 	 * @return DocumentCollection with entries for docs, each with a Metadatum for name.
 	 */
 	/*public DocumentCollection getMetadataByName(String name, String[] docs)  
 			throws RepositoryAccessException {
 		
 		String[] names = {name};
 		return this.getDocCollection(names, docs);
 	}*/
 	/**
 	 * Returns a DocumentList of ProxyRhizomeDocuments.
 	 * <p>Given a metadata name and an array of document IDs, 
 	 * this returns a DocumentList.</p>
 	 * <p>This search ONLY checks for metadata in the document IDs given in the 
 	 * <code>docs[]</code> array.</p>
 	 * @see com.technosophos.rhizome.document.DocumentList
 	 * @param name metadatum name to search for
 	 * @param docs array of document IDs to search
 	 * @param repo Initialized Document repository for use generating documents.
 	 * @return DocumentList with entries for docs, each with a Metadatum for name.
 	 */
 	public DocumentList getMetadataByName(String name, String[] docs, DocumentRepository repo) throws RepositoryAccessException {
 		String[] names = {name};
 		return this.getDocumentList(names, docs, repo);
 	}
 
 	/**
 	 * Returns top matches. No more than maxResults are returned.
 	 */
 	public SearchResults simpleSearch(String query, String names[], Map<String, String> args, DocumentRepository repo, int maxResults)
 			throws RepositoryAccessException {
 		return this.simpleSearch(query, names, args, repo, maxResults, 0);
 	}
 	
 	/**
 	 * Return no more than 25 matches.
 	 */
 	public SearchResults simpleSearch(String query, String names[], Map<String, String> args, DocumentRepository repo)
 			throws RepositoryAccessException {
 		return this.simpleSearch(query, names, args, repo, 25, 0);
 	}
 	
 	/**
 	 * Perform a simple search against a Lucene backend.
 	 * 
 	 * <p>Values that can appear in in args:</p>
 	 * <ul>
 	 * <li>'fields': A comma-separated list of fields to search: "title,subtitle". Most important fields should be listed first. Order will determine sorting.</li>
 	 * <li>'search_body': If this is a string starting with f or n, the body will not be searched.
 	 * </ul> 
 	 */
 	public SearchResults simpleSearch(String query, String names[], Map<String, String> args, DocumentRepository repo, int maxResults, int offset)
 			throws RepositoryAccessException {
 		ArrayList<String> fields = new ArrayList<String>();
 		if(args.containsKey(SIMPLE_SEARCH_FIELDS)) {
 			String p = args.get(SIMPLE_SEARCH_FIELDS);
 			String [] fieldsBuffer = p.split(",");
 			for(String pp: fieldsBuffer) fields.add(pp.trim());
 		}
 		
 		if(args.containsKey(SIMPLE_SEARCH_SEARCH_BODY)) {
 			String q = args.get(SIMPLE_SEARCH_SEARCH_BODY);
 			if(!(q.startsWith("n") || q.startsWith("f")))
 				fields.add(LUCENE_BODY_FIELD);
 		} else fields.add(LUCENE_BODY_FIELD);
 		
 		// New index searcher
 		IndexReader reader;
 		try {
 			reader = this.getIndexReader();
 		} catch (IOException e) {
 			throw new RepositoryAccessException("Could not read the index.");
 		}
 		IndexSearcher searcher = new IndexSearcher(reader);
 		MultiFieldQueryParser qp = new MultiFieldQueryParser(
 			fields.toArray(new String[fields.size()]), 
 			new StandardAnalyzer()
 		);
 		
 		Hits hits; // Sort if we have enough fields:
 		try {
 			if( fields.size() > 0 )
 				hits = searcher.search(qp.parse(query), new Sort(fields.get(0)));
 			else 
 				hits = searcher.search(qp.parse(query));
 		} catch (IOException e) {
 			throw new RepositoryAccessException("IOException: Could not search index: " + e.toString());
 		} catch (ParseException e) {
 			// TODO: Decide what to do when this happens. Maybe we shouldn't return an
 			// error to the user.
 			throw new RepositoryAccessException("ParseException: Could not parse search query: " + e.toString());
 		}
 		
 		int numHits = hits.length();
 		if(numHits == 0 || offset > numHits) {
 			return new SearchResults(query, names, args, maxResults, offset);
 			//throw new RepositoryAccessException("Offset exceeds number of returned hits.");
 		}
 		
 		DocumentList dl = new DocumentList();
 		ProxyRhizomeDocument pdoc;
 		Document ldoc;
		int max = maxResults > numHits ? numHits : maxResults;
		for(int i = 0; i < max; ++i) {
 			try {
 				ldoc = hits.doc(offset + i); // throws IOException
 				pdoc = new ProxyRhizomeDocument(
 						ldoc.get(LUCENE_DOCID_FIELD),
 						this.fetchMetadata(ldoc, names),
 						repo
 				);
 				dl.add(pdoc);
 			} catch (IOException e) {
 				// Skip document?
 				System.out.println("Skipping document."); // FIXME: This should do something useful
 			}
 		}
 		return new SearchResults(query, names, args, maxResults, offset, hits.length(), dl);
 	}
 	
 
 	
 	/**
 	 * This retrieves a DocumentCollection.
 	 * <p>The collection will have an entry for every member of docIDs that exists in 
 	 * the directory. An entry in the list will have a Metadatum item for every item
 	 * in the names array.</p>
 	 * <p>This method is used to grab a subset of available metadata for a select
 	 * batch of document IDs.</p>
 	 * @param names
 	 * @param docIDs
 	 * @return a DocumentCollection containing docs from docIDs, each with metadata.
 	 */
 	/*public DocumentCollection getDocCollection(String[] names, String[] docIDs) 
 			throws RepositoryAccessException {
 		DocumentCollection dc = new DocumentCollection(names);
 		
 		HashSet<String> activeFields = new HashSet<String>();
 		HashSet<String> lazyFields = new HashSet<String>();
 		
 		activeFields.add(LUCENE_DOCID_FIELD);
 		lazyFields.addAll(Arrays.asList(names));
 		SetBasedFieldSelector fsel = new SetBasedFieldSelector(activeFields, lazyFields);
 		IndexReader lreader = null;
 		
 		try {
 			lreader = this.getIndexReader();
 			int last = lreader.maxDoc();
 			Document d;
 			String docID;
 			for(int i = 0; i < last; ++i) {
 				if(!lreader.isDeleted(i)) {
 					d = lreader.document(i, fsel);
 					docID = d.get(LUCENE_DOCID_FIELD);
 					// This should be optimized:
 					for(String did: docIDs)
 						if(did.equals(docID)) 
 							dc.put(docID, this.fetchMetadata(d, names));
 				}
 				
 			}
 			//lreader.close();
 		} catch (java.io.IOException ioe) {
 			throw new RepositoryAccessException("IOException: " + ioe.getMessage());
 		} finally {
 			if(lreader != null) {
 				try{ lreader.close(); } catch (java.io.IOException ioe) {}
 			}
 		}
 		
 		return dc;
 		
 	}*/
 	
 	/**
 	 * This retrieves a {@link DocumentList} of {@link ProxyRizomeDocument} objects.
 	 * <p>The collection will have an entry for every member of docIDs that exists in 
 	 * the directory. An entry in the list will have a Metadatum item for every item
 	 * in the names array.</p>
 	 * <p>This method is used to grab a subset of available metadata for a select
 	 * batch of document IDs.</p>
 	 * @param names
 	 * @param docIDs
 	 * @return a DocumentCollection containing docs from docIDs, each with metadata.
 	 */
 	public DocumentList getDocumentList(String[] names, String[] docIDs, DocumentRepository repo) 
 			throws RepositoryAccessException {
 		DocumentList dl = new DocumentList(names);
 		
 		HashSet<String> activeFields = new HashSet<String>();
 		HashSet<String> lazyFields = new HashSet<String>();
 		
 		activeFields.add(LUCENE_DOCID_FIELD);
 		lazyFields.addAll(Arrays.asList(names));
 		SetBasedFieldSelector fsel = new SetBasedFieldSelector(activeFields, lazyFields);
 		IndexReader lreader = null;
 		
 		try {
 			lreader = this.getIndexReader();
 			int last = lreader.maxDoc();
 			Document d;
 			String docID;
 			for(int i = 0; i < last; ++i) {
 				if(!lreader.isDeleted(i)) {
 					d = lreader.document(i, fsel);
 					docID = d.get(LUCENE_DOCID_FIELD);
 					// This should be optimized:
 					for(String did: docIDs)
 						if(did.equals(docID)) 
 							dl.add(new ProxyRhizomeDocument(docID, this.fetchMetadata(d, names), repo));
 				}
 				
 			}
 			//lreader.close();
 		} catch (java.io.IOException ioe) {
 			throw new RepositoryAccessException("IOException: " + ioe.getMessage());
 		} finally {
 			if(lreader != null) {
 				try{ lreader.close(); } catch (java.io.IOException ioe) {}
 			}
 		}
 		
 		return dl;
 		
 	}
 	
 	/**
 	 * Search for matching documents, and return them in a DocumentCollection.
 	 * <p>This method performs a narrowing (AND) search, returning a DocumentCollection
 	 * that contains docIDs for all docs that matched everything in the narrower.</p>
 	 * <p>What metadata is in the DocumentCollection? This tries to retrieve all of the
 	 * metadata items in the narrower, plus all of the metadata in the additional_md
 	 * array.</p>
 	 * <p>So, if there are two items in the narrower, and three items in the additional_md:
 	 * <ul>
 	 * <li>The DocumentCollection will contain an entry for every document that matched 
 	 * both items in the narrower.</li>
 	 * <li>Each item will have up to five Metadatum objects: one for each narrower name/value,
 	 * and one for each additional_md entry.</li>
 	 * </ul>
 	 * </p>
 	 * <p>This sort of thing is the same operation that could be achived running 
 	 * {@see narrowingSearch(Map)}, and using getDocCollection() on the results.
 	 * This, however, is far more efficient.</p>
 	 * 
 	 * 
 	 * @param narrower
 	 * @param additional_md
 	 * @return DocumentCollection with all docs that match the narrower.
 	 * @throws RepositoryAccessException
 	 * @deprecated Use fetchDocumentList()
 	 */
 	public DocumentCollection narrowingSearch(Map<String, String> narrower, String[] additional_md)
 			throws RepositoryAccessException {
 		
 		/*
 		 * Welcome to your worst collections nightmare....
 		 * 
 		 * Here's what's going on. We want to collect only items that match the narrower.
 		 * But we also may want more metadata returned in the DocumentCollection. For
 		 * example, I might want to match PublishDate, but I want Title to be retrieved
 		 * even though no matching is done on that field.
 		 * 
 		 * PublishDate, then, needs to be actively loaded, as it will DEFINITELY be used.
 		 * Title, on the other hand, will only be used if all of the narrower conditions
 		 * match. So, we should load it lazily.
 		 * 
 		 * So... DocumentCollection needs a list of all fields. But the FieldSelector
 		 * needs to lists: one for actively loaded fields, and one for lazily loaded
 		 * fields (if there are any).
 		 * 
 		 * fields: Those that require value matching (actively loaded)
 		 * all_fields: Those that should be included in collection, but need no matching.
 		 * activeFields: fields + DOCID
 		 * lazyFields: additional_md (lazily loaded)
 		 */
 		String [] all_fields;
 		Set<String> narrower_keys = narrower.keySet();
 		String [] fields = narrower_keys.toArray(new String[narrower_keys.size()]);
 		HashSet<String> activeFields = new HashSet<String>();
 		HashSet<String> lazyFields = new HashSet<String>();
 		
 		activeFields.addAll(narrower_keys);
 		activeFields.add(LUCENE_DOCID_FIELD);
 		
 		if (additional_md.length > 0) {
 			// If there are additional fields, we need to add those to all_fields.
 			List<String> add_md = Arrays.asList(additional_md);
 			ArrayList<String> allFields = new ArrayList<String>();
 			allFields.addAll(narrower_keys);
 			allFields.addAll(add_md);
 			
 			all_fields = allFields.toArray(new String[allFields.size()]);
 			
 			// Additional fields should be lazily loaded.
 			lazyFields.addAll(add_md);
 		} else {
 			all_fields = fields;
 		}
 		
 		DocumentCollection dc = new DocumentCollection(all_fields);
 		SetBasedFieldSelector fsel = new SetBasedFieldSelector(activeFields, lazyFields);
 		IndexReader lreader = null;
 		
 		// Do the work....
 		try {
 			lreader = this.getIndexReader();
 			int last = lreader.maxDoc();
 			Document d;
 			String docID;
 			for(int i = 0; i < last; ++i) {
 				if(!lreader.isDeleted(i)) {
 					d = lreader.document(i, fsel);
 					docID = d.get(LUCENE_DOCID_FIELD);
 					if(this.checkANDFieldMatches(fields, narrower, d))
 						dc.put(docID, this.fetchMetadata(d, all_fields));
 				}
 				
 			}
 		} catch (java.io.IOException ioe) {
 			throw new RepositoryAccessException("IOException: " + ioe.getMessage());
 		} finally {
 			if(lreader != null) {
 				try{ lreader.close(); } catch (java.io.IOException ioe) {}
 			}
 		}
 		
 		return dc;
 	}
 	
 	/**
 	 * Create a list of ProxyRhizomeDocuments from a narrowing search.
 	 * <p>This performs a narrowing search, but returns the results as DocumentList containing
 	 * {@link ProxyRhizomeDocument} instances. This is more suited to daily use.</p>
 	 */
 	public DocumentList fetchDocumentList(Map<String, String> narrower, String[] additional_md, DocumentRepository r)
 			throws RepositoryAccessException {
 		
 		String [] all_fields;
 		Set<String> narrower_keys = narrower.keySet();
 		String [] fields = narrower_keys.toArray(new String[narrower_keys.size()]);
 		HashSet<String> activeFields = new HashSet<String>();
 		HashSet<String> lazyFields = new HashSet<String>();
 		
 		activeFields.addAll(narrower_keys);
 		activeFields.add(LUCENE_DOCID_FIELD);
 		
 		if (additional_md.length > 0) {
 			// If there are additional fields, we need to add those to all_fields.
 			List<String> add_md = Arrays.asList(additional_md);
 			ArrayList<String> allFields = new ArrayList<String>();
 			allFields.addAll(narrower_keys);
 			allFields.addAll(add_md);
 			
 			all_fields = allFields.toArray(new String[allFields.size()]);
 			
 			// Additional fields should be lazily loaded.
 			lazyFields.addAll(add_md);
 		} else {
 			all_fields = fields;
 		}
 		
 		//DocumentCollection dc = new DocumentCollection(all_fields);
 		DocumentList dl = new DocumentList(all_fields);
 		SetBasedFieldSelector fsel = new SetBasedFieldSelector(activeFields, lazyFields);
 		IndexReader lreader = null;
 		
 		// Do the work....
 		try {
 			lreader = this.getIndexReader();
 			int last = lreader.maxDoc();
 			Document d;
 			String docID;
 			for(int i = 0; i < last; ++i) {
 				if(!lreader.isDeleted(i)) {
 					d = lreader.document(i, fsel);
 					docID = d.get(LUCENE_DOCID_FIELD);
 					if(this.checkANDFieldMatches(fields, narrower, d))
 						dl.add(new ProxyRhizomeDocument(docID, 
 														this.fetchMetadata(d, all_fields),
 														r));
 				}
 				
 			}
 		} catch (java.io.IOException ioe) {
 			throw new RepositoryAccessException("IOException: " + ioe.getMessage());
 		} finally {
 			if(lreader != null) {
 				try{ lreader.close(); } catch (java.io.IOException ioe) {}
 			}
 		}
 		
 		return dl;
 	
 	}
 	
 	/**
 	 * Perform a search for documents with multiple metadata names.
 	 * Given a <code>Map</code> of metadatum names and values, 
 	 * this searches for documents that have *all* of
 	 * the given metadata. This performs like a (short-circuit) AND-ing search.
 	 * <p>For example, the map may contain <code>{'key1'=>'val1', 'key2'=>'val2'}</code>.
 	 * A document ID will be returned in the String[] iff it has both keys, and it has 
 	 * values that match the given values.</p> 
 	 * @param narrower
 	 * @return
 	 */
 	public String [] narrowingSearch(Map<String, String> narrower) throws RepositoryAccessException {
 		
 		ArrayList<String> docIDs = new ArrayList<String>();
 		
 		// Get String[] of names for MapFieldSelector
 		Iterator<String> keys = narrower.keySet().iterator();
 		int l = narrower.size();
 		String [] fields = new String[l];
 		for(int i = 0; i < l; ++i) fields[i] = (String)keys.next();
 		
 		/* More efficient to copy another way:
 		ArrayList<String> fieldList = new ArrayList<String>(Arrays.asList(fields));
 		fieldList.add(LUCENE_DOCID_FIELD);
 		*/
 		String [] fieldList = new String[ fields.length + 1];
 		System.arraycopy(fields, 0, fieldList, 0, fields.length);
 		fieldList[fieldList.length - 1] = LUCENE_DOCID_FIELD;
 		
 		
 		// Now we are ready to check for matches:
 		MapFieldSelector fieldSelector = new MapFieldSelector(fieldList);
 		IndexReader lreader = null;
 		try {
 			lreader = this.getIndexReader();
 			int last = lreader.maxDoc();
 			Document d;
 			for(int i = 0; i < last; ++i) {
 				if(!lreader.isDeleted(i)) {
 					d = lreader.document(i, fieldSelector);
 					if(this.checkANDFieldMatches(fields, narrower, d))
 						docIDs.add(d.get(LUCENE_DOCID_FIELD));
 				}
 			}
 		} catch (java.io.IOException ioe) {
 			throw new RepositoryAccessException("IOException: " + ioe.getMessage());
 		} finally {
 			if(lreader != null) {
 				try{ lreader.close(); } catch (java.io.IOException ioe) {}
 			}
 		}
 		
 		return docIDs.toArray(new String[docIDs.size()]);
 	}
 	
 	/** 
 	 * Helper function that checks that all fields in a list match for a document.
 	 * Takes a Map of key, val pairs to match with field, vals in doc. A Document field may 
 	 * have multiple values. This checks all values.
 	 * @return true if the document matches *all* match criteria.
 	 */
 	private boolean checkANDFieldMatches(String[] keys, Map<String, String> match, Document doc) {
 		for(String key: keys) {
 			// Fail fast:
 			if(!this.checkFieldValueMatches(key, match.get(key), doc))
 				return false;
 		}
 		return true; // everything matches for this doc.
 	}
 	
 	/** Helper function for checking field values. */
 	private boolean checkFieldValueMatches(String key, String value, Document doc) {
 		//System.err.format( "Key: %s, Val: %s, Doc: %s\n", key, value, doc.toString());
 		String[] vals = doc.getValues(key); //getValues can return null.
 		if( vals == null ) return false;
 		for( String val: vals ) {
 			if(value.equals(val)) return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Utility function: Get metadata values in a list.
 	 * @param d Initialized document
 	 * @param names Names of metadata to get
 	 * @return List of metadata objects with names and values.
 	 */
 	private ArrayList<Metadatum> fetchMetadata(Document d, String[] names) {
 		ArrayList<Metadatum> md = new ArrayList<Metadatum>(names.length);
 		
 		// Sometimes a returned key will have a null value. Need to check for that here.
 		for(String name: names)	{
 			String[] vals = d.getValues(name);
 			if(vals != null)
 				md.add(new Metadatum(name, vals));
 			//else System.err.format("LuceneSearcher: Key %s has no values.\n", name);
 		}
 		
 		return md;
 	}
 
 	
 	public boolean isReusable() {
 		return false;
 	}
 	
 	public RepositoryContext getConfiguration() {
 		return this.context;
 	}
 	
 	public void setConfiguration(RepositoryContext context) {
 		this.context = context;
 	}
 	
 	private IndexReader getIndexReader() throws java.io.IOException {
 		String ipath = LuceneIndexer.getIndexPath(this.indexName, this.context);
 		if(ipath == null) throw new java.io.IOException(ipath+" does not exist.");
 		File indexDir = new File(ipath);
 		return IndexReader.open(indexDir);
 	}
 }
