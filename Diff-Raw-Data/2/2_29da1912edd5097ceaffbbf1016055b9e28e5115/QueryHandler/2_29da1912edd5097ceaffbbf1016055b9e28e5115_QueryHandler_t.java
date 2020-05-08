 package de.haas.searchandfind.frontend;
 
 import de.haas.searchandfind.backend.App;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.SimpleFSDirectory;
 import org.apache.lucene.util.Version;
 
 /**
  *
  * @author laga
  */
 public class QueryHandler {
 
     Directory indexDirectory;
 
     public QueryHandler() throws IOException {
          this.indexDirectory = new SimpleFSDirectory(App.INDEX_DIRECTORY);
     }
 
     public List<QueryResult> handleQuery(String tokenInFileName, String tokenInContent) throws IOException, ParseException {
 
         ArrayList<QueryResult> res = new ArrayList<QueryResult>();
 
         StringBuffer queryStringBuffer = new StringBuffer();
         if (tokenInFileName != null && !tokenInFileName.isEmpty()) {
             queryStringBuffer.append("(");
             queryStringBuffer.append(App.FIELD_FILE_NAME);
             queryStringBuffer.append(":");
             queryStringBuffer.append(tokenInFileName);
             queryStringBuffer.append(")");
         }
 
         if (tokenInContent != null && !tokenInContent.isEmpty()) {
             // TODO: how does searching actually work? What is stored? How is the analyzer used?!
             queryStringBuffer.append("(");
             queryStringBuffer.append(App.FIELD_CONTENT);
             queryStringBuffer.append(":");
             queryStringBuffer.append(tokenInContent);
             queryStringBuffer.append(")");
         }
 
         // TODO: export Lucene version somewhere
         // TODO: export analyzer object somewhere
         Logger.getLogger(QueryHandler.class.getName()).log(Level.INFO, "Constructed Query String: " + queryStringBuffer.toString());
         QueryParser qp = new QueryParser(Version.LUCENE_30, App.FIELD_CONTENT, new StandardAnalyzer(Version.LUCENE_30));
         Query query = qp.parse(queryStringBuffer.toString());
 
         boolean readOnly = true;
        IndexSearcher searcher = new IndexSearcher(this.indexDirectory, readOnly);
 
         TopDocs docs = searcher.search(query, 10);
         ScoreDoc[] results = docs.scoreDocs;
         for (int ii = 0; ii < results.length; ii++) {
             Document currentDoc = searcher.doc(ii);
             String fileName = currentDoc.get(App.FIELD_FILE_NAME);
             QueryResult currentResult = new QueryResult(fileName);
             res.add(currentResult);
         }
 
         return res;
 
     }
 
     public class QueryRunner extends Thread {
     }
 
     public class QueryResult {
 
         private String fileName;
 
         public QueryResult(String file) {
             this.fileName = file;
         }
 
         public String getFileName() {
             return this.fileName;
         }
 
         public void setFileName(String file) {
             this.fileName = file;
         }
     }
 }
