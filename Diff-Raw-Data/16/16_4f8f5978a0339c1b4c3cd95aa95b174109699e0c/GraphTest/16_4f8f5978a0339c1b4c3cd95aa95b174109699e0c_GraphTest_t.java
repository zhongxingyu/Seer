 package src.test;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.junit.Assert;
 import java.lang.Exception;
 
 import src.main.Bag;
 import src.main.Graph;
 
 public class GraphTest {
     Graph a;
     /**
      * Sets up the test fixture. 
      * (Called before every test case method.)
      */
     @Before
     public void setUp() {
         a = new Graph(5);
 
     }
 
     /**
      * Tears down the test fixture. 
      * (Called after every test case method.)
      */
     @After
     public void tearDown() {
         
     }
     
     @Test
     public void testAddEdge() {
         a.addEdge(0,1);
         a.addEdge(1,2);
         a.addEdge(2,3);
         a.addEdge(3,4);
         a.addEdge(4,0);
         assertEquals(5, a.E());
         String expected = "5 vertices, 5 edges\n0: 4 1\n1: 2 0\n2: 3 1\n3: 4 2\n4: 0 3\n";
         assertEquals(expected, a.toString());
     }
 
     @Test
     public void testV(){
         assertEquals(5, a.V());
     
     }
 
     @Test
     public void testE(){
         assertEquals(0,a.E());
         a.addEdge(0,1);
         a.addEdge(1,2);
         a.addEdge(2,3);
         a.addEdge(3,4);
         a.addEdge(4,0);
         assertEquals(5, a.E());
     }
 
     // @Test
     // public void testGenerateErdosRenyiGraph(){
     //     Graph er = Graph.generateErdosRenyiGraph(100);
     //     //System.out.println(er);
     // }
 
     @Test
     public void testGetLargestComponentVerticesDisconnected(){
         a.addEdge(0,1);
         a.addEdge(1,2);
         a.addEdge(0,2);
         a.addEdge(3,4);
         int[] comp = Graph.getLargestComponentVertices(a);
         int[] expected = {0, 1, 2};
         assertArrayEquals(expected, comp);
     }
     @Test
     public void testGetLargestComponentVerticesConnected(){
         a.addEdge(0,1);
         a.addEdge(1,2);
         a.addEdge(2,3);
         a.addEdge(3,4);
         a.addEdge(4,0);
         int[] comp = Graph.getLargestComponentVertices(a);
        int[] expected = {0, 1, 2, 3, 4};
         assertArrayEquals(expected, comp);
     }
 
     @Test
     public void testDiameterEmpty(){
         assertEquals(0, Graph.diameter(a));
     }
 
     @Test
     public void testDiameter5(){
         a.addEdge(0,1);
         a.addEdge(1,2);
         a.addEdge(2,3);
         a.addEdge(3,4);
         a.addEdge(4,0);
         assertEquals(5, Graph.diameter(a));
     }
 
     @Test
     public void testDiameterDisconnectedComponents(){
         a.addEdge(0,1);
         a.addEdge(1,2);
         a.addEdge(0,2);
         a.addEdge(3,4);
         assertEquals(3, Graph.diameter(a));
     }
 
     public static void main(String args[]) {
         org.junit.runner.JUnitCore.main("src.test.ElementTest");
     }
 
 }
