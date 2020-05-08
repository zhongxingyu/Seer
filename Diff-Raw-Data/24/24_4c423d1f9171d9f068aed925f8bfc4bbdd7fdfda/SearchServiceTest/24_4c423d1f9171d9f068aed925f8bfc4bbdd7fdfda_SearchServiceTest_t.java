 package com.meltmedia.cadmium.search;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 
 public class SearchServiceTest {
 
   private IndexSearcher index;
   private QueryParser parser;
   
   private SearchService service;
   private Document docs[];
   
   @SuppressWarnings("serial")
   @Before
   public void setupIndexReader() throws Exception {
     index = mock(IndexSearcher.class);
     parser = mock(QueryParser.class);
     when(parser.parse("good_query")).thenReturn(new Query(){
 
       @Override
       public String toString(String arg0) {
         return "good";
       }
     });
     when(parser.parse("bad_query")).thenReturn(new Query(){
 
       @Override
       public String toString(String arg0) {
         return "bad";
       }
     });
     
     TopDocs hasResults = new TopDocs(5, new ScoreDoc[]{new ScoreDoc(1, 1.1f),
         new ScoreDoc(2, 1.1f),
         new ScoreDoc(3, 1.1f),
         new ScoreDoc(4, 1.1f),
         new ScoreDoc(5, 1.1f)}, 5.5f);
     
     TopDocs noResults = new TopDocs(0, new ScoreDoc[]{}, 0.0f);
     
     when(index.search(parser.parse("good_query"), null, 100000)).thenReturn(hasResults);
     when(index.search(parser.parse("bad_query"), null, 100000)).thenReturn(noResults);
     Document one = new Document();
    one.add(new Field("path", "1", Field.Store.YES, Field.Index.ANALYZED));
     Document two = new Document();
    two.add(new Field("path", "2", Field.Store.YES, Field.Index.ANALYZED));
     Document three = new Document();
    three.add(new Field("path", "3", Field.Store.YES, Field.Index.ANALYZED));
     Document four = new Document();
    four.add(new Field("path", "4", Field.Store.YES, Field.Index.ANALYZED));
     Document five = new Document();
    five.add(new Field("path", "5", Field.Store.YES, Field.Index.ANALYZED));
     
     docs = new Document[] {one, two, three, four, five};
     
     when(index.doc(1)).thenReturn(one);
     when(index.doc(2)).thenReturn(two);
     when(index.doc(3)).thenReturn(three);
     when(index.doc(4)).thenReturn(four);
     when(index.doc(5)).thenReturn(five);
     
     service = new SearchService();
     service.setIndex(index);
     service.setParser(parser);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testSearchWithResults() throws Exception {
     Map<String, Object> results = service.search("good_query");
     
     assertTrue("Should not be empty", !results.isEmpty());
    assertTrue("Should be set", results.containsKey("number-hits"));
    assertTrue("Should be integer", results.get("number-hits") instanceof Integer);
     assertTrue("Should say 5", new Integer(5).equals(results.get("number-hits")));
     assertTrue("Should have results entry", results.containsKey("results"));
     assertTrue("Should have 5 result entries", ((List<Map<String, Object>>)results.get("results")).size() == 5);
     List<Map<String, Object>> resultList = ((List<Map<String, Object>>)results.get("results"));
     for(int i=0; i<resultList.size(); i++) {
       Document doc = docs[i];
       Map<String, Object> result = resultList.get(i);
       assertTrue("Score should be 1.1f", new Float(1.1f).equals(result.get("score")));
       assertTrue("Path isn't correct", doc.get("path").equals(result.get("path")));
     }
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testSearchWithoutResults() throws Exception {
     Map<String, Object> results = service.search("bad_query");
     
     assertTrue("Should not be empty", !results.isEmpty());
    assertTrue("Should be set", results.containsKey("number-hits"));
    assertTrue("Should be integer", results.get("number-hits") instanceof Integer);
     assertTrue("Should say 0", new Integer(0).equals(results.get("number-hits")));
     assertTrue("Should have results entry", results.containsKey("results"));
     assertTrue("Should have no result entries", ((List<Map<String, Object>>)results.get("results")).isEmpty());
   }
 }
