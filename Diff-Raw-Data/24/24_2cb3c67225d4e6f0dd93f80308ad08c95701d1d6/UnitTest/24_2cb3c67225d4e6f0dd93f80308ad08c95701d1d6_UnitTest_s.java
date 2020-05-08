 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import gammaContainers.HJoinWithBloomFilter;
 import gammaContainers.MapReduceBFilter;
 import gammaContainers.MapReduceBloom;
 import gammaContainers.MapReduceGamma;
 import gammaContainers.MapReduceHJoin;
 import hashJoin.BFilter;
 import hashJoin.Bloom;
 import hashJoin.BloomSimulator;
 import hashJoin.HJoin;
 import hashJoin.HSplit;
 import hashJoin.PrintTuple;
 import hashJoin.basicConnector.Connector;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import hashJoin.MainTest;
 import hashJoin.Merge;
 import hashJoin.ReadRelation;
 import hashJoin.Sink;
 import hashJoin.basicConnector.ReadEnd;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author bansal
  */
 public class UnitTest {
 
     public UnitTest() {
     }
 
     @BeforeClass
     public static void setUpClass() {
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {        
     }
 
     @After
     public void tearDown() {
     }
 
     // TODO add test methods here.
     // The methods must be annotated with annotation @Test. For example:
     //
     
     @Test
     public void testReadRelation_PrintTuple() {
         System.out.println("Starting test ReadRelation_PrintTuple connector... ");       
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector read_A = new Connector("input1");
         ReadRelation r = new ReadRelation("input/client.txt","clientDB", read_A.getWriteEnd());
         PrintTuple p = new PrintTuple(read_A.getReadEnd());
         r.start();
         p.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/readRelationPrint.txt", false); // test passes if files are equal
                System.setOut(System.out);
     }    
     
     @Test       
      public void testMapReduceBloom() throws Exception {
         System.out.println("Starting Map Reduce Bloom Test...");
 
 //        RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector print_bloom = new Connector("print_bloom");
         ReadRelation r = new ReadRelation("input/client.txt","clientDB", print_bloom.getWriteEnd());
         Connector bloom_join = new Connector("bloom_join");
         Sink p1 = new Sink(bloom_join.getReadEnd());
         Connector bloom_bmap = new Connector("bloom_bmap");
         ReadEnd readEnd = bloom_bmap.getReadEnd();
         Bloom mapReduceBloom = new Bloom(print_bloom.getReadEnd(), bloom_join.getWriteEnd(), bloom_bmap.getWriteEnd(), 0);
         r.start();
         mapReduceBloom.start();
         p1.start();       
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         
         assert("fftffftfffffffftfffffftfffff".equals(readEnd.getNextString())); 
     }
    
     @Test       
     public void testBloom() throws Exception {
         System.out.println("Starting test Bloom...");
 
 //        RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector print_bloom = new Connector("print_bloom");
         ReadRelation r = new ReadRelation("input/client.txt","clientDB", print_bloom.getWriteEnd());
         Connector bloom_join = new Connector("bloom_join");
         Sink p1 = new Sink(bloom_join.getReadEnd());
         Connector bloom_bmap = new Connector("bloom_bmap");
         ReadEnd readEnd = bloom_bmap.getReadEnd();
         Bloom bloom = new Bloom(print_bloom.getReadEnd(), bloom_join.getWriteEnd(), bloom_bmap.getWriteEnd(), 0);
         r.start();
         bloom.start();
         p1.start();    
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         assert("fftffftfffffffftfffffftfffff".equals(readEnd.getNextString())); 
                System.setOut(System.out);
     }
     
     @Test
     public void testJoin_ClientXViewing() {
         System.out.println("Starting test simple HJoin..._ClientXViewing");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
 
         String[] args = null;
         Connector read_A = new Connector("input1");
         ReadRelation r1 = new ReadRelation("input/client.txt", "clientDB", read_A.getWriteEnd());
         Connector read_B = new Connector("input2");
         ReadRelation r2 = new ReadRelation("input/viewing.txt", "clientDB", read_B.getWriteEnd());
         Connector out = new Connector("output");
         HJoin h = new HJoin(0, read_A.getReadEnd(),0,read_B.getReadEnd(),out.getWriteEnd());
         PrintTuple p = new PrintTuple(out.getReadEnd());
         r1.start();
         r2.start();
         h.start();
         p.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/ClientJoinViewing.txt", false); // test passes if files are equal
         System.setOut(System.out);
     }
     
     @Test
     public void testJoin_OrdersXOdetails() {
         System.out.println("Starting test simple HJoin..._OrdersXOdetails");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
 
         String[] args = null;
         Connector read_A = new Connector("input1");
         ReadRelation r1 = new ReadRelation("input/orders.txt", "clientDB", read_A.getWriteEnd());
         Connector read_B = new Connector("input2");
         ReadRelation r2 = new ReadRelation("input/odetails.txt", "clientDB", read_B.getWriteEnd());
         Connector out = new Connector("output");
         HJoin h = new HJoin(0, read_A.getReadEnd(),0,read_B.getReadEnd(),out.getWriteEnd());
         PrintTuple p = new PrintTuple(out.getReadEnd());
         r1.start();
         r2.start();
         h.start();
         p.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/OrdersJoinOdetails.txt", false); // test passes if files are equal
         System.setOut(System.out);
     }
     
     @Test
     public void testJoin_PartsXOdetails() {
         System.out.println("Starting test simple HJoin..._PartsXOdetails");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
 
         String[] args = null;
         Connector read_A = new Connector("input1");
         ReadRelation r1 = new ReadRelation("input/parts.txt", "clientDB", read_A.getWriteEnd());
         Connector read_B = new Connector("input2");
         ReadRelation r2 = new ReadRelation("input/odetails.txt", "clientDB", read_B.getWriteEnd());
         Connector out = new Connector("output");
         HJoin h = new HJoin(0, read_A.getReadEnd(), 1,read_B.getReadEnd(),out.getWriteEnd());
         PrintTuple p = new PrintTuple(out.getReadEnd());
         r1.start();
         r2.start();
         h.start();
         p.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/PartsJoinOdetails.txt", false); // test passes if files are equal
         System.setOut(System.out);
     }
 
     @Test
     public void testJoinMapReduce_ClientXViewing() {
         System.out.println("Starting test MapReduce hJoin..._ClientXViewing");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector out2 = new Connector("output2");
         MapReduceHJoin mapJ = new MapReduceHJoin(0, "input/client.txt", 0 , "input/viewing.txt",out2.getWriteEnd() );
         mapJ.start();
         PrintTuple p2 = new PrintTuple(out2.getReadEnd());
         p2.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/ClientJoinViewing.txt", false); // test passes if files are equal
     }
 
     @Test
     public void testJoinMapReduce_OrdersXOdetails() {
         System.out.println("Starting test MapReduce hJoin..._OrdersXOdetails");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector out2 = new Connector("output2");
         MapReduceHJoin mapJ = new MapReduceHJoin(0, "input/orders.txt", 0 , "input/odetails.txt",out2.getWriteEnd() );
         mapJ.start();
         PrintTuple p2 = new PrintTuple(out2.getReadEnd());
         p2.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/OrdersJoinOdetails.txt", false); // test passes if files are equal
         System.setOut(System.out);
     }
 
     @Test
     public void testJoinMapReduce_PartsXOdetails() {
         System.out.println("Starting test MapReduce hJoin..._PartsXOdetails");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector out2 = new Connector("output2");
         MapReduceHJoin mapJ = new MapReduceHJoin(0, "input/parts.txt", 1, "input/odetails.txt",out2.getWriteEnd() );
         mapJ.start();
         PrintTuple p2 = new PrintTuple(out2.getReadEnd());
         p2.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/PartsJoinOdetails.txt", false); // test passes if files are equal
     }
 
     @Test
     public void testJoinMapReduceWithBloom_ClientXViewing() {
         System.out.println("Starting test MapReduce BloomWithJoin..._ClientXViewing");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector out2 = new Connector("output2");
         HJoinWithBloomFilter mapJ = new HJoinWithBloomFilter(0, "input/client.txt", 0 , "input/viewing.txt",out2.getWriteEnd() );
         mapJ.start();
         PrintTuple p2 = new PrintTuple(out2.getReadEnd());
         p2.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/ClientJoinViewing.txt", false); // test passes if files are equal
     } 
 
     @Test
     public void testJoinMapReduceWithBloom_OrdersXOdetails() {
         System.out.println("Starting test MapReduce BloomWithJoin..._OrdersXOdetails");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector out2 = new Connector("output2");
         HJoinWithBloomFilter mapJ = new HJoinWithBloomFilter(0, "input/orders.txt", 0 , "input/odetails.txt",out2.getWriteEnd() );
         mapJ.start();
         PrintTuple p2 = new PrintTuple(out2.getReadEnd());
         p2.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/OrdersJoinOdetails.txt", false); // test passes if files are equal
     } 
 
     @Test
     public void testJoinMapReduceWithBloom_PartsXOdetails() {
         System.out.println("Starting test MapReduce BloomWithJoin..._PartsXOdetails");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector out2 = new Connector("output2");
         HJoinWithBloomFilter mapJ = new HJoinWithBloomFilter(0, "input/parts.txt", 1 , "input/odetails.txt",out2.getWriteEnd() );
         mapJ.start();
         PrintTuple p2 = new PrintTuple(out2.getReadEnd());
         p2.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/PartsJoinOdetails.txt", false); // test passes if files are equal
     } 
     
     @Test
     public void testReadRelation_Hsplit_PrintTuple() {
         // read --> sort --> print
         System.out.println("Starting ReadRelation _ Hsplit _  PrintTuple");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector read_A = new Connector("input1");
         ReadRelation r = new ReadRelation("input/client.txt","clientDB", read_A.getWriteEnd());
         Connector hash_1 = new Connector("pipe1");
         Connector hash_2 = new Connector("pipe2");
         Connector hash_3 = new Connector("pipe3");
         Connector hash_4 = new Connector("pipe4");
         HSplit h = new HSplit(0,read_A.getReadEnd(),hash_1.getWriteEnd(),hash_2.getWriteEnd(),hash_3.getWriteEnd(),hash_4.getWriteEnd());
         PrintTuple p1 = new PrintTuple(hash_1.getReadEnd());
         PrintTuple p2 = new PrintTuple(hash_2.getReadEnd());
         PrintTuple p3 = new PrintTuple(hash_3.getReadEnd());
         PrintTuple p4 = new PrintTuple(hash_4.getReadEnd());
         r.start();
         h.start();
         p1.start();
         p2.start();
         p3.start();
         p4.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/readRelationHsplitPrint.txt", false); // test passes if files are equal        
     }
 
     @Test   
     public void testGamma_ClientXViewing() {
         System.out.println("Starting Gamma Test...ClientXViewing");
         String file1 = "input/client.txt";
         String file2 = "input/viewing.txt";
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector out = new Connector("output");
         MapReduceGamma h = new MapReduceGamma(0, file1, 0, file2,out.getWriteEnd());
         PrintTuple p = new PrintTuple(out.getReadEnd());
         h.start();
         p.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }        
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/ClientJoinViewing.txt", false); // test passes if files are equal        
     }
     
    @Test   
     public void testGamma_OrdersXOdetails() {
         System.out.println("Starting Gamma Test.._OrdersXOdetails");
         String file1 = "input/orders.txt";
         String file2 = "input/odetails.txt";
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector out = new Connector("output");
         MapReduceGamma h = new MapReduceGamma(0, file1, 0, file2,out.getWriteEnd());
         PrintTuple p = new PrintTuple(out.getReadEnd());
         h.start();
         p.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }        
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/OrdersJoinOdetails.txt", false); // test passes if files are equal        
     }
     
    @Test   
     public void testGamma_PartsXOdetails() {
         System.out.println("Starting Gamma Test.._PartsXOdetails");
         String file1 = "input/parts.txt";
         String file2 = "input/odetails.txt";
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector out = new Connector("output");
         MapReduceGamma h = new MapReduceGamma(0, file1, 1, file2,out.getWriteEnd());
         PrintTuple p = new PrintTuple(out.getReadEnd());
         h.start();
         p.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }        
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/PartsJoinOdetails.txt", false); // test passes if files are equal        
     }
     
     @Test
     public void readRelation_Hsplit_Merge_PrintTuple() {
         System.out.println("Starting ReadRelation _ Hsplit _ Merge _ PrintTuple");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector read_A = new Connector("input1");
         ReadRelation r = new ReadRelation("input/client.txt","clientDB", read_A.getWriteEnd());
         Connector hash_1 = new Connector("pipe1");
         Connector hash_2 = new Connector("pipe2");
         Connector hash_3 = new Connector("pipe3");
         Connector hash_4 = new Connector("pipe4");
         Connector out = new Connector("output");
         HSplit h = new HSplit(0,read_A.getReadEnd(),hash_1.getWriteEnd(),hash_2.getWriteEnd(),hash_3.getWriteEnd(),hash_4.getWriteEnd());
         Merge m = new Merge(out.getWriteEnd(),hash_1.getReadEnd(),hash_2.getReadEnd(),hash_3.getReadEnd(),hash_4.getReadEnd() );
         PrintTuple p = new PrintTuple(out.getReadEnd());
         r.start();
         h.start();
         m.start();
         p.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/readRelationHsplitMergePrintOut.txt", false); // test passes if files are equal        
     }
     
     @Test
     public void testBFilterAndPrint() {
         System.out.println("Starting Test BFilter and Print");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector readBloomRelationConnector = new Connector("print_bloom");
         Connector readBloomFilterRelationConnector = new Connector("print_bloomFilter");
         Connector bloomConnector = new Connector("bloomConnector");
         Connector hBloomJoinConnector = new Connector("hBloomJoinConnector");
         Connector hBloomFilterJoinConnector = new Connector("hBloomFilterJoinConnector");
         
         int fieldNumber = 0;
         
         ReadRelation r_bloom = new ReadRelation("input/client.txt","clientDB", readBloomRelationConnector.getWriteEnd());
         ReadRelation r_bloomFilter = new ReadRelation("input/client.txt","clientDB", readBloomFilterRelationConnector.getWriteEnd());
         
         Bloom bloom = new Bloom(readBloomRelationConnector.getReadEnd(),
                 hBloomJoinConnector.getWriteEnd(), bloomConnector.getWriteEnd(), fieldNumber);   
         r_bloom.start();
         r_bloomFilter.start();
         bloom.start();
       
         MapReduceBFilter filter = 
                 new MapReduceBFilter(bloomConnector.getReadEnd(), 
                     readBloomFilterRelationConnector.getReadEnd(), 
                     hBloomFilterJoinConnector.getWriteEnd(), fieldNumber);
         filter.start();
         
         PrintTuple readEnd = new PrintTuple(hBloomFilterJoinConnector.getReadEnd());
         readEnd.start();
  
         try {
             Thread.sleep(100);
         } catch (Exception e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/readRelationHsplitMergePrintOut.txt", false); // test passes if files are equal        
         System.setOut(System.out);
     }
     
     @Test
     public void testBloomSimulator() throws Exception {
         System.out.println("Starting test BloomSimulator");
         Connector bloom_print = new Connector("bloom_print");
         BloomSimulator bloomSimulator = new BloomSimulator(bloom_print.getWriteEnd(), "input/client.txt");
         ReadEnd readEnd = bloom_print.getReadEnd();
         bloomSimulator.start();
         try {
             Thread.sleep(100);
         } catch (Exception e) {
             e.printStackTrace();
         }
         assert("fftffftfffffffffffffffffffff".equals(readEnd.getNextString()));        
     }
 
     @Test
     public void testBloomSimulatorFilter() throws Exception {
         System.out.println("Starting test BloomSimulator with Filter");
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector bloom_print = new Connector("bloom_print");
         BloomSimulator bloomSimulator = new BloomSimulator(bloom_print.getWriteEnd(), "input/client.txt");
         Connector readBloomFilterRelationConnector = new Connector("print_bloomFilter");
         ReadRelation r_bloom = new ReadRelation("input/client.txt","clientDB", readBloomFilterRelationConnector.getWriteEnd());
         Connector filter_print = new Connector("filter_print");
         PrintTuple p = new PrintTuple(filter_print.getReadEnd());
         BFilter filter = new BFilter(bloom_print.getReadEnd(), 
                 readBloomFilterRelationConnector.getReadEnd(), filter_print.getWriteEnd(), 0);
         r_bloom.start();
         p.start();
         filter.start();
         bloomSimulator.start();
         try {
             Thread.sleep(100);
         } catch (Exception e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "expected/bloomSimulatorFilterOut.txt", false); // test passes if files are equal        
         System.setOut(System.out);
     }
 
 
 }
