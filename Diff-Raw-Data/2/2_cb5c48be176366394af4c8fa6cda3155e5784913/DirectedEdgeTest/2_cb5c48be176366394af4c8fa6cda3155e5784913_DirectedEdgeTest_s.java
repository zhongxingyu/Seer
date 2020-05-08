 package com.github.haw.ai.gkap.graph.test;
 
 /**
  * @author Till Theis <till.theis@haw-hamburg.de>
  */
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 import com.github.haw.ai.gkap.graph.*;
 
 import static com.github.haw.ai.gkap.graph.Graphs.*;
 import static java.util.Arrays.asList;
 
 public class DirectedEdgeTest {
     Integer c1, c2, c3, c4;
     String c5, c6;
     
     Vertex<Integer> v1, v2, v3;
     Vertex<String> v4, v5;
     
     Edge<Integer, Integer> e1, e2, e3;
     Edge<Integer, String> e4, e5, e6;
     
     @Before
     public void setUp() {
         c1 = -1;
         c2 = 0;
         c3 = 9;
         c4 = -255;
         
         c5 = "";
         c6 = "hans";
         
         v1 = vertex(c1);
         v2 = vertex(c2);
         v3 = vertex(c3);
 
         v4 = vertex(c5);
         v5 = vertex(c6);
         
         e1 = directedEdge(v1, v2, c2);
         e2 = directedEdge(v2, v3, c1);
         e3 = directedEdge(v3, v1, c3);
 
         e4 = directedEdge(v4, v5, c2);
         e5 = directedEdge(v5, v4, c4);
         e6 = directedEdge(v4, v4, c4);
     }
     
     @Test
     public void testContent() {
         assertEquals(c2, e1.content());
         assertEquals(c1, e2.content());
         assertEquals(c3, e3.content());
         assertEquals(c2, e4.content());
         assertEquals(c4, e5.content());
     }
 
     @Test
     public void testVertices() {
         assertTrue(e1.vertices().containsAll(asList(v1, v2)));
         assertTrue(e2.vertices().containsAll(asList(v3, v2)));
         assertTrue(e3.vertices().containsAll(asList(v1, v3)));
         assertTrue(e4.vertices().containsAll(asList(v4, v5)));
         assertTrue(e5.vertices().containsAll(asList(v4, v5)));
     }
 
     @Test
     public void testVerticesSize() {
         assertEquals(2, e1.vertices().size());
         assertEquals(2, e2.vertices().size());
         assertEquals(2, e3.vertices().size());
         assertEquals(2, e4.vertices().size());
         assertEquals(2, e5.vertices().size());
        assertEquals(2, e6.vertices().size());
     }
 
     @Test
     public void testIsReachable() {
         assertTrue(e1.isReachable(v1, v2));
         assertTrue(e2.isReachable(v2, v3));
         assertTrue(e3.isReachable(v3, v1));
         assertTrue(e4.isReachable(v4, v5));
         assertTrue(e5.isReachable(v5, v4));
         
         assertTrue(e6.isReachable(v4, v4));
     }
     
     @Test
     public void testIsReachableNegative() {
         assertFalse(e1.isReachable(v2, v1));
         assertFalse(e2.isReachable(v3, v2));
         assertFalse(e3.isReachable(v1, v3));
         assertFalse(e4.isReachable(v5, v4));
         assertFalse(e5.isReachable(v4, v5));
 
         assertFalse(e1.isReachable(v2, v3));
         assertFalse(e2.isReachable(v1, v2));
         assertFalse(e3.isReachable(v2, v2));
     }
 
 }
