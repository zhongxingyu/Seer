 package com.pcwerk.seck.indexer;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.StringField;
 import org.apache.lucene.document.TextField;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopScoreDocCollector;
 
 import com.pcwerk.seck.store.WebDocument;
 
 
 public class Indexer {
 	//Test
 	public static void main(String args[]){
		Indexer indexer = new Indexer("/Users/ajo/workspace/cs454/seck/seck-web/indexing");
 		List<WebDocument> results = indexer.indexQuerySearch("up");
 		for(WebDocument result: results){
 			System.out.println(result.getTitle());
 			System.out.println(result.getUrl());
 		}
 		//indexer.indexReader();
 	}
 	
     private static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
 
     private IndexWriter writer;
 
     private String indexDir;
 
     public Indexer(String indexDir){
         this.indexDir = indexDir;
     }
 
     public void indexWriter(List<WebDocument> pages){
         try{
 
             FSDirectory dir = FSDirectory.open(new File(indexDir));
 
             IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
 
             writer = new IndexWriter(dir, config);
         }
         catch( IOException ex ){
             System.out.println("Cannot create index..." + ex.getMessage());
         }
 
 
         for (WebDocument p : pages)
             addDoc(p);
     }
 
     private void addDoc(WebDocument p){
         try
         {
             Document doc = new Document();
             doc.add(new TextField("url", p.getUrl(), Field.Store.YES));
             doc.add(new StringField("title", p.getTitle(), Field.Store.YES));
             doc.add(new TextField("content", p.getContent(), Field.Store.YES));
 
             writer.addDocument(doc);
         }
         catch( IOException ex ){
             System.out.println("Cannot write index..." + ex.getMessage());
         }
     }
 
 
     public void indexReader(){
         try
         {
             IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
 
             int num = reader.numDocs();
             for ( int i = 0; i < num; i++){
                 Document d = reader.document(i);
                 System.out.println( (i+1) + "= " + d );
                 System.out.println("---------------------------\n");
             }
             reader.close();
 
         }
         catch( IOException ex ){
             System.out.println("Cannot open index..." + ex.getMessage());
         }
     }
 
     public List<WebDocument> indexQuerySearch(String s){
         
         List<WebDocument> resultList = new ArrayList<WebDocument>(); 
         try
         { 
             IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
             IndexSearcher searcher = new IndexSearcher(reader);
             TopScoreDocCollector collector = TopScoreDocCollector.create(5, true);
 
             Query q = new QueryParser(Version.LUCENE_40, "content", analyzer).parse(s);
             //Query q = new TermQuery(new Term("id", id));
             System.out.println(q);
             System.out.println("Docs in index: " + reader.numDocs());
             searcher.search(q, collector);
             ScoreDoc[] hits = collector.topDocs().scoreDocs;
 
             System.out.println("Found " + hits.length + " hits.");
             for(int i=0;i<hits.length;++i) {
                 int docId = hits[i].doc;
                 Document d = searcher.doc(docId);
 //                System.out.println( (i + 1) + ". " + d.get("path") + " score=" + hits[i].score);
                 WebDocument webdoc = new WebDocument();
                 String[] url = d.getValues("url");
                 String [] title = d.getValues("title");
                 if(url.length > 0 && title.length > 0){
                 webdoc.setUrl( url[0]);
                 webdoc.setTitle( title[0]);
                 webdoc.setScore( Double.parseDouble( hits[i].score+"" ) );
                 resultList.add( webdoc );
                 }
             }
         }
         catch( Exception ex ){
         	ex.printStackTrace();
             System.out.println("Cannot query on the index table..." + ex.getMessage());
         }
         
         Collections.sort( resultList );
         
         return resultList;
     }
 
 
     public void closeIndex(){
         try{
             writer.close();
         }
         catch( IOException ex ){
             System.out.println("Cannot close index..." + ex.getMessage());
         }
     }
     
     
 
 }
