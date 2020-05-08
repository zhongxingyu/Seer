 package com.caseware.fave100;
 
 import java.io.File;
 import java.io.IOException;
 
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 public class LuceneIndex {
	public static WhitespaceAnalyzer ANALYZER;
 	public static IndexSearcher SEARCHER;
 	public static final Version LUCENE_VERSION = Version.LUCENE_43;
 
 	static {
		ANALYZER = new WhitespaceAnalyzer(LUCENE_VERSION);
 
 		final File file = new File(Thread.currentThread().getContextClassLoader().getResource("lucene-index").getPath());
 
 		try {
 			final Directory index = FSDirectory.open(file);
 			final IndexReader reader = DirectoryReader.open(index);
 			SEARCHER = new IndexSearcher(reader);
 		}
 		catch (final IOException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 }
