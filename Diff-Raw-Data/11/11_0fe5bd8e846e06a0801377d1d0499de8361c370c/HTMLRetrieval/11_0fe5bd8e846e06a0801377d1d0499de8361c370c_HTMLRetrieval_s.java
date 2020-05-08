 package edu.columbia.watson.twitter;
 
 /**
  * With reference to: Apache Lucene Demo program
  */
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.queryparser.classic.ParseException;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 import edu.columbia.watson.twitter.util.GlobalProperty;
 
 public class HTMLRetrieval {
 
 	private static Logger logger = Logger.getLogger(HTMLRetrieval.class);
 	private String indexDir = GlobalProperty.getInstance().getHtmlIndexPath();
 	private IndexReader reader;
 	private IndexSearcher searcher;
 	private Analyzer analyzer;
 
 	public HTMLRetrieval() throws IOException{
 		try {
 			reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
 		} catch (IOException e) {
 			logger.error("Error creating index reader, index dir = " + indexDir);
 			logger.error(e);
 			throw e;
 		}
 		searcher = new IndexSearcher(reader);
 		analyzer = new StandardAnalyzer(Version.LUCENE_42);
 	}
 
 	public float getLinkedHtmlScore(String queryText, long tweetID) throws ParseException, IOException{
		QueryParser parser = new QueryParser(Version.LUCENE_42, "content", analyzer);
 		Query query = parser.parse(queryText);
 
 		TopDocs results = searcher.search(query, 2 * GlobalProperty.getInstance().getK());
 		ScoreDoc[] hits = results.scoreDocs;

 		for (ScoreDoc doc : hits){
 			String filePath = searcher.doc(doc.doc).get("path").trim();
 			int startIndex = filePath.lastIndexOf('/');
 			int endIndex = filePath.lastIndexOf('.');
			String id = filePath.substring(startIndex, endIndex - startIndex + 1).trim();
 			logger.info("filepath = " + filePath + ", id = " + id + ", score = " + doc.score);
 		}
 		return 0.0f;
 	}
 
 
 	public static void main(String[] args){
 
 		HTMLRetrieval dr;
 		try {
 			dr = new HTMLRetrieval();
 			dr.getLinkedHtmlScore("BBC", 31653409140514816L);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 
 
 	}
 
 
 
 }
