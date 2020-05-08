 package com.where.data.parsers.citysearch;
 
 import gnu.trove.TIntIntHashMap;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriter.MaxFieldLength;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.NIOFSDirectory;
 
 public class CSListingIndexer {
 	private static final Log logger = LogFactory.getLog(CSListingIndexer.class);
 	
 	private IndexWriter writer;
 	private String indexPath;
 	
 	private CSListingIndexer() {}
 	
 	public static CSListingIndexer newInstance(String indexPath) {
 		try {
 			CSListingIndexer indexer = new CSListingIndexer();
 			indexer.indexPath = indexPath;
 			indexer.writer = newIndexWriter(indexPath);
 			return indexer;
 		}
 		catch(Exception ex) {
 			logger.error("Error creating indexer", ex);
 			
 			throw new IllegalStateException(ex);
 		}
 	}
 	
	public static CSListingIndexer newInstance(String indexPath, org.apache.lucene.analysis.Analyzer an) {
 		try {
 			CSListingIndexer indexer = new CSListingIndexer();
 			indexer.indexPath = indexPath;
 			indexer.writer = newIndexWriter(indexPath, an);
 			return indexer;
 		}
 		catch(Exception ex) {
 			logger.error("Error creating indexer", ex);
 			
 			throw new IllegalStateException(ex);
 		}
 	}
 	
 	public String getIndexPath() {
 		return indexPath;
 	}
 	
 	public void index(CSListing poi) {
 		try {
 			//TODO: find the thread where we can begin with the analyzerwrapper
 			writer.addDocument(CSListingDocumentFactory.createDocument(poi));
 		}
 		catch(Exception ex) {
 			throw new IllegalArgumentException(ex);
 		}
 	}
 	
 	public void index(Document doc) {
 		try {
 			writer.addDocument(doc);
 		}
 		catch(Exception ex) {
 			throw new IllegalArgumentException(ex);
 		}
 	}	
 	
 	public void close() {
 		try {
 			writer.optimize();
 			writer.close();
 		}
 		catch(Exception ex) {
 			throw new IllegalStateException(ex);
 		}
 	}
 	
     private static IndexWriter newIndexWriter(String indexPath) throws IOException {
         Directory directory = new NIOFSDirectory(new File(indexPath));
         IndexWriter writer = new IndexWriter(directory, new Analyzer(), true, MaxFieldLength.UNLIMITED);
         writer.setMergeFactor(100000);
         writer.setMaxMergeDocs(Integer.MAX_VALUE);        
         return writer;
 	} 
     
     private static IndexWriter newIndexWriter(String indexPath, org.apache.lucene.analysis.Analyzer an) throws IOException {
     	Directory directory = new NIOFSDirectory(new File(indexPath));	
         IndexWriter writer = new IndexWriter(directory,an, true, MaxFieldLength.UNLIMITED);
         writer.setMergeFactor(100000);
         writer.setMaxMergeDocs(Integer.MAX_VALUE);        
         return writer;
 	}
 
     public TIntIntHashMap csId2whereId_ = null;    
 	public void setcs2whereMapping(TIntIntHashMap csId2whereId) 
 	{
 		csId2whereId_ = csId2whereId;		
 	} 
 
 }
