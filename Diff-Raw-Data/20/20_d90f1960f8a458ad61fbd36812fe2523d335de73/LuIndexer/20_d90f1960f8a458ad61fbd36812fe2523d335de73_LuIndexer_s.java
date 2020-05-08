 package com.videoSearch.search;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.apache.lucene.util.Version;
 import org.beldyk.video.harvester.Indexer;
 
 import com.videoSearch.media.AbstractMediaItem;
 import com.videoSearch.media.VideoItem;
 
 public class LuIndexer {
 	protected IndexWriter iWriter;
 	protected Directory iDir;
 	protected Indexer vIndexer;
 	
 	public LuIndexer(IndexWriter iWriter, Directory iDir, Indexer vIndexer){
 		this.iWriter = iWriter;
 		this.iDir = iDir;
 		this.vIndexer = vIndexer;
 		
 	}
 	
 	
 	public Integer fullIndex() throws CorruptIndexException, IOException {
 		vIndexer.spiderFS();
 		vIndexer.parseFiles();
 		vIndexer.pullInSeries();
 		vIndexer.mapFiles2Series();
 		Integer countDocs = luceneIndex();
 		return countDocs;
 	}
 	
 	public void close() throws CorruptIndexException, IOException{
 		iWriter.close();
 	}
 	
 	public Integer luceneIndex() throws CorruptIndexException, IOException {
 		for(AbstractMediaItem item: vIndexer.getMediaItems()){
 			Document doc = item.toDocument();
	
 			
 			if(doc != null){
 				iWriter.addDocument(doc);
 			}
 		}
 		
 		return iWriter.numDocs();
 	}
 	public IndexWriter getIWriter() {
 		return iWriter;
 	}
 
 	public void setIWriter(IndexWriter writer) {
 		iWriter = writer;
 	}
 
 	public Directory getIDir() {
 		return iDir;
 	}
 
 	public void setIDir(Directory dir) {
 		iDir = dir;
 	}
 
 	public Indexer getVIndexer() {
 		return vIndexer;
 	}
 
 	public void setVIndexer(Indexer indexer) {
 		vIndexer = indexer;
 	}
 
 
 }
