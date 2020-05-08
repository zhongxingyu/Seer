 package com.videoSearch.search;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 import org.beldyk.video.harvester.Indexer;
 
 public class LuFull {
 	LuIndexer luIndexer;
 	LuQuerier luQuerier;
 
 	private Directory indexDirectory;
 
 
 	public LuFull() throws CorruptIndexException, LockObtainFailedException, IOException{
 		indexDirectory = new RAMDirectory();
 
 		IndexWriter iWriter = new IndexWriter( indexDirectory, 
 				new StandardAnalyzer(Version.LUCENE_30),
 				true,
 				IndexWriter.MaxFieldLength.UNLIMITED
 		);
 
 		Indexer dexer  = new Indexer("videoPatterns.txt", "testData/television/");
 		luIndexer = new LuIndexer(iWriter, indexDirectory, dexer);
 
 		luQuerier = new LuQuerier(indexDirectory);
 
 	}
 
 	public void init() throws CorruptIndexException, IOException{
 		luIndexer.fullIndex();
		
 	}
 
 	public Collection<String> search(String query) throws IOException, ParseException{
 		Collection<Document> docs = luQuerier.search(query, 50);
 		Collection<String> urls = new ArrayList<String>();
 		for(Document doc: docs){
 			urls.add(doc.getField("fileUrl").stringValue());
 		}
 
 		return urls;
 	}
 
 	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException, ParseException{
 		LuFull luFull = new LuFull();
 		luFull.init();
 		java.io.BufferedReader stdin = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
 
 		while(true){
 			System.out.print("Enter your query:");
 			String query = stdin.readLine();
 			try {
 				Collection<String> results = luFull.search(query);
 				System.out.printf("Found %d results\n", results.size());
 				for(String i:results){
 					System.out.println(i);
 				}
 			}catch(ParseException e){
 				System.err.println("Unable to parse '"+query+"'");
 			}
 		}
 	}
 
 }
