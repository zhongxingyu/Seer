 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import gammaContainers.HJoinWithBloomFilter;
 import gammaContainers.MapReduceHJoin;
 import hashJoin.PrintTuple;
 import hashJoin.basicConnector.Connector;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import hashJoin.MainTest;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author bansal
  */
 public class Test1 {
 
     public Test1() {
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
     public void testJoin() {
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
 
         String[] args = null;
         MainTest.readRelation_Hjoin_PrintTupleTest("client.txt", "viewing.txt");
         MainTest.FileSort("out.txt");
         RegTest.Utility.validate("out.txt", "ClientJoinViewing.txt", false); // test passes if files are equal
     }
 
     @Test
     public void testJoinMapReduce() {
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector out2 = new Connector("output2");
         MapReduceHJoin mapJ = new MapReduceHJoin(0, "client.txt", 0 , "viewing.txt",out2.getWriteEnd() );
         mapJ.start();
         PrintTuple p2 = new PrintTuple(out2.getReadEnd());
         p2.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         MainTest.FileSort("out.txt");
        RegTest.Utility.validate("out.txt", "ClientJoinViewing.txt", false); // test passes if files are equal
     }
 
     @Test
     public void testJoinMapReduceWithBloom() {
         RegTest.Utility.redirectStdOut("out.txt");  // redirects standard out to file "out.txt"
         Connector out2 = new Connector("output2");
         HJoinWithBloomFilter mapJ = new HJoinWithBloomFilter(0, "client.txt", 0 , "viewing.txt",out2.getWriteEnd() );
         mapJ.start();
         PrintTuple p2 = new PrintTuple(out2.getReadEnd());
         p2.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
        MainTest.FileSort("out.txt");
        RegTest.Utility.validate("out.txt", "ClientJoinViewing.txt", false); // test passes if files are equal
     }
 
 
 }
