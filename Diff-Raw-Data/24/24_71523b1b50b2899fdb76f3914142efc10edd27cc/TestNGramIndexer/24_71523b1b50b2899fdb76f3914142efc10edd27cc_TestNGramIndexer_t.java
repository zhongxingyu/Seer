 package indexer;
 
import junit.framework.TestCase;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.NIOFSDirectory;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Created by User: ting
  * Date: 10/13/12
  * Time: 12:09 PM
  */
public class TestNGramIndexer extends TestCase{
 
     public void testNGramIndexer() throws IOException {
        String sourceDir = "/home/ting/Documents/iRegexData/Enron2Lvl/dir0";
        String outputDir = "/home/ting/Documents/iRegexData/index/tmp";

         // Add documents to the index
        long start = System.currentTimeMillis();
        Directory indexDir = new NIOFSDirectory(new File(outputDir));
        NGramAnalyzer analyzer = new NGramAnalyzer(new NGramTokenizer(4));
        NGramIndexer indexer = new NGramIndexer(sourceDir,analyzer);
 
         indexer.constructIndex(indexDir);
        
        long end = System.currentTimeMillis();
        System.out.println("Time spend: " + (end - start) + " millisec");
     }
    
    
    
 }
