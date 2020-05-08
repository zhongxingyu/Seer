 package com.videoSearch.search;
 
 import static org.junit.Assert.*;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 import org.beldyk.video.harvester.Indexer;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class LuIndexerTest {
 
 	private LuIndexer luDex;
 	private Directory indexDirectory;
 	
 	private IndexWriter iWriter;
 	
 	private Indexer dexer;
 	
 	@Before
 	public void init() throws CorruptIndexException, LockObtainFailedException, IOException{
 		indexDirectory = new RAMDirectory();
 
 		iWriter = new IndexWriter( indexDirectory, 
 				new StandardAnalyzer(Version.LUCENE_30),
 				true,
 				IndexWriter.MaxFieldLength.UNLIMITED
 		);
 		
 		dexer  = new Indexer("videoPatterns.txt", "testData/television/");
 		luDex = new LuIndexer(iWriter, indexDirectory, dexer);
 	}
 	
 	@After
 	public void tearDown(){
 		luDex = null;
 		indexDirectory = null;
 		iWriter = null;
 		dexer = null;
 	}
 	
 	
 	@Test
 	public void fullRun() throws CorruptIndexException, IOException{
 		
 		// spin up my spider code
 		dexer.spiderFS();
 		dexer.parseFiles();
 		dexer.pullInSeries();
 		dexer.mapFiles2Series();
 		Integer countDocs = luDex.index();
 		System.err.printf("Indexed %d documents.\n", countDocs);
		assertTrue(countDocs > 3000);
 		
 	}
 }
