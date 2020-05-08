 package indexer;
 
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.NIOFSDirectory;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Created by User: ting
  * Date: 10/13/12
  * Time: 12:09 PM
  */
public class TestNGramIndexer {
 
     public void testNGramIndexer() throws IOException {
         // Add documents to the index
        Directory indexDir = new NIOFSDirectory(new File("destDir"));
        NGramAnalyzer analyzer = new NGramAnalyzer(new NGramTokenizer(2));
        NGramIndexer indexer = new NGramIndexer("sourceData",analyzer);
 
         indexer.constructIndex(indexDir);
     }
 }
