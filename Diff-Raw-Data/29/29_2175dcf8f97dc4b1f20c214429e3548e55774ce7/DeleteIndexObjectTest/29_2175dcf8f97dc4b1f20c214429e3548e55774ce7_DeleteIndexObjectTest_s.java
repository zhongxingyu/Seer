 package pt.inevo.encontra.luceneapp.test;
 
 import junit.framework.TestCase;
 import pt.inevo.encontra.descriptors.Descriptor;
 import pt.inevo.encontra.descriptors.DescriptorExtractor;
 import pt.inevo.encontra.descriptors.SimpleDescriptor;
 import pt.inevo.encontra.engine.SimpleEngine;
 import pt.inevo.encontra.index.IndexedObject;
 import pt.inevo.encontra.index.search.SimpleSearcher;
 import pt.inevo.encontra.lucene.index.LuceneIndex;
 import pt.inevo.encontra.lucene.index.LuceneIndexEntryFactory;
 import pt.inevo.encontra.query.QueryProcessorDefaultImpl;
 import pt.inevo.encontra.storage.EntityStorage;
 import pt.inevo.encontra.storage.SimpleObjectStorage;
 
 import java.io.FileNotFoundException;
 
 /**
  * Delete an indexobject from a LuceneIndex.
  * @author ricardo
  */
 public class DeleteIndexObjectTest extends TestCase {
 
    public static class TestObject extends IndexedObject<Integer,String> {}
 
     public static class D1 extends SimpleDescriptor{
         @Override
         public double getDistance(Descriptor other) {
             return 0;
         }
     }
 
     public static class D1Extractor extends DescriptorExtractor<TestObject,D1> {
 
         @Override
         protected TestObject setupIndexedObject(D1 descriptor, TestObject object) {
             object.setId(Integer.parseInt(descriptor.getId().toString()));
             object.setValue(descriptor.getValue());
             return object;
         }
 
         @Override
         public D1 extract(TestObject object) {
             D1 d = new D1();
             d.setId(object.getId());
             d.setValue("It works!");
             return d;
         }
 
     }
 
     public DeleteIndexObjectTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     public void testMain() throws FileNotFoundException {
 
         EntityStorage storage = new SimpleObjectStorage(TestObject.class);
 
         SimpleEngine<TestObject> e = new SimpleEngine<TestObject>();
         e.setQueryProcessor(new QueryProcessorDefaultImpl());
         LuceneIndex<TestObject> index=new LuceneIndex<TestObject>("luceneDelete",TestObject.class);
         SimpleSearcher searcher = new SimpleSearcher();
         searcher.setIndex(index);
         searcher.setObjectStorage(storage);
         
         e.setSearcher(TestObject.class.getName(), searcher);
         e.setObjectStorage(storage);
        
         LuceneIndexEntryFactory<D1> entryFactory=new LuceneIndexEntryFactory<D1>(D1.class);
         index.setEntryFactory(entryFactory);
 
         DescriptorExtractor<TestObject, D1> d=new D1Extractor();
         searcher.setDescriptorExtractor(d);
 
         //inserting some objects in the index
         for (int i= 1 ; i < 10 ; i++){
             TestObject object = new TestObject();
             object.setId(i);
             object.setValue("Does it work? Event number " + i);
             e.insert(object);
         }
 
         //performing the query
         TestObject queryObject = new TestObject();
         queryObject.setId(5);
 
        assert (e.remove(queryObject) == true);
     }
 }
