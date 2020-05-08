 package ru.exorg.core.lucene;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.lucene.search.*;
 import org.apache.lucene.store.SimpleFSDirectory;
 import org.apache.lucene.store.Directory;
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.util.Version;
 
 import org.springframework.beans.factory.InitializingBean;
 
 
 public class Search implements InitializingBean {
     private Directory indexDirectory;
     private IndexSearcher indexSearcher;
 
     public Search() { }
 
     public void setDirectory(final String dir) throws Exception {
         this.indexDirectory = new SimpleFSDirectory(new File(dir));
     }
 
     public void afterPropertiesSet() throws Exception {
         this.indexSearcher = new IndexSearcher(indexDirectory);
     }
 
    private QueryParser createParser() {
        return new QueryParser(Version.LUCENE_30, "id", new StandardAnalyzer(Version.LUCENE_30));
    }

     public <T> List<T> search(final String queryString, DocMapper<T> mapper) {
         try {
            QueryParser qp = createParser();

             Query q = qp.parse(queryString);
             TopDocs rs = indexSearcher.search(q, 1000);
 
             List<T> list = new ArrayList<T>();
             for (ScoreDoc sd : rs.scoreDocs) {
                 T obj = mapper.mapDoc(indexSearcher.doc(sd.doc));
                 if (obj != null) {
                     list.add(obj);
                 }
             }
 
             return list;
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return null;
     }
 }
