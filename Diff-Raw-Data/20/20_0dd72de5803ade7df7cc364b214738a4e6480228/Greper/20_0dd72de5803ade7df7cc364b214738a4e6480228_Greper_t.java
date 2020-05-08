 package com.weibogrep.indexer;
 
 import java.io.File;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Searcher;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.TermPositionVector;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.highlight.Highlighter;
 import org.apache.lucene.search.highlight.QueryScorer;
 import org.apache.lucene.search.highlight.SimpleFragmenter;
 import org.apache.lucene.search.highlight.TokenSources;
 
import com.weibogrep.util.ZLog;

 import net.paoding.analysis.examples.gettingstarted.BoldFormatter;
 
 public class Greper {
 
     private String queryStr;
     private File indexDir;
 
     public Greper(String query, File indexDir) {
         this.queryStr = query;
         this.indexDir = indexDir;
        assert(indexDir != null);
     }
 
     public String[] grep(QueryParser parser, IndexReader reader, IndexSearcher greper) {
     	try {
 	        Query query = parser.parse(queryStr);
 	        query = query.rewrite(reader);
 	        TopDocs hits = greper.search(query, 10);
 	        BoldFormatter formatter = new BoldFormatter();
 	        Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
 	        highlighter.setTextFragmenter(new SimpleFragmenter(50));
 	        String[] ret = new String[hits.scoreDocs.length];
 	        for (int i = 0; i < hits.scoreDocs.length; i++) {
 	            int docId = hits.scoreDocs[i].doc;
 	            Document hit = greper.doc(docId);
 	            String text = hit.get("content");
 	            int maxNumFragmentsRequired = 5;
 	            String fragmentSeparator = "...";
 	            TermPositionVector tpv = (TermPositionVector) reader.getTermFreqVector(docId, "content");
 	            TokenStream tokenStream = TokenSources.getTokenStream(tpv);
 	            ret[i] = highlighter.getBestFragments(tokenStream, 
 	                                                  text, 
 	                                                  maxNumFragmentsRequired, 
 	                                                  fragmentSeparator);
 	        }
 	        return ret;
     	} catch (Exception e) {
     		e.printStackTrace();
    		ZLog.err(e.toString());
     		return null;
     	}
     }
 }
