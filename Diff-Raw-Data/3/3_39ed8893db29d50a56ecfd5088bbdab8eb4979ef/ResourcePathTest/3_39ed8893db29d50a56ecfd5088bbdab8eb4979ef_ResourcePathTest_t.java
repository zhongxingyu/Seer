 package org.eclipselabs.recommenders.test.codesearch.rcp.indexer;
 
 import java.io.File;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.store.RAMDirectory;
 import org.eclipse.recommenders.codesearch.rcp.index.Fields;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.CodeIndexerIndex;
 import org.eclipse.recommenders.codesearch.rcp.index.indexer.ResourcePathIndexer;
 import org.eclipse.recommenders.codesearch.rcp.index.searcher.CodeSearcherIndex;
import org.junit.Ignore;
 import org.junit.Test;
 
 public class ResourcePathTest {
 
     @Test
    @Ignore("Well, this obviously fails when executed in a *nix environment. Silly me...")
     public void testPath() {
         File f = new File("c:\\test-folder\\test.java");
         String actualPath = ResourcePathIndexer.getResourcePathForQuery(f);
 
         Assert.assertEquals("c\\:\\\\test\\-folder\\\\test.java", actualPath);
     }
 
     @Test
     public void testIndexPathIndexedCorrectly() throws Exception {
         String filePath = "C:\\eclipseworkspace\\junit-workspace\\testProject\\MyInstanceOfClass.java";
 
         Document doc = new Document();
         CodeIndexerIndex.addAnalyzedField(doc, Fields.RESOURCE_PATH, filePath);
 
         CodeIndexerIndex index = new CodeIndexerIndex(new RAMDirectory());
         index.addDocument(doc);
         index.commit();
 
         CodeSearcherIndex searcherIndex = new CodeSearcherIndex(index.getIndex());
         List<Document> results = searcherIndex.getDocuments();
 
         Assert.assertEquals(1, results.size());
         Assert.assertNotNull(results.get(0).get(Fields.RESOURCE_PATH));
         Assert.assertEquals(filePath, results.get(0).get(Fields.RESOURCE_PATH));
     }
 
     @Test
     public void testIndexPathFoundCorrectly() throws Exception {
         String filePath = "C:\\eclipseworkspace\\junit-workspace\\testProject\\MyInstanceOfClass.java";
 
         Document doc = new Document();
         CodeIndexerIndex.addAnalyzedField(doc, Fields.RESOURCE_PATH, filePath);
 
         CodeIndexerIndex index = new CodeIndexerIndex(new RAMDirectory());
         index.addDocument(doc);
         index.commit();
 
         CodeSearcherIndex searcherIndex = new CodeSearcherIndex(index.getIndex());
 
         List<Document> results = searcherIndex.search(Fields.RESOURCE_PATH + ":"
                 + ResourcePathIndexer.getResourcePathForQuery(filePath));
 
         Assert.assertEquals(1, results.size());
         Assert.assertNotNull(results.get(0).get(Fields.RESOURCE_PATH));
         Assert.assertEquals(filePath, results.get(0).get(Fields.RESOURCE_PATH));
     }
 }
