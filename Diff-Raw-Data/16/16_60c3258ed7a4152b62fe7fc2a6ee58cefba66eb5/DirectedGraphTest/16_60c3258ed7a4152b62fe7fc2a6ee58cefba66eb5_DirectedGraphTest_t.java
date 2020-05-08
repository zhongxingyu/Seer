 package com.algorithms.datastructures;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.algorithms.datastructures.graph.DirectedGraph;
 import com.algorithms.datastructures.graph.GraphUtils;
 
 public class DirectedGraphTest {
 
     private DirectedGraph g;
 
    private final String dataFileLocation = "test\\data\\datastructures\\graph.txt";
 
     private final int[][] verificationMatrix = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 },
             { 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0 }, { 2, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0 },
             { 3, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0 }, { 4, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0 },
             { 5, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0 }, { 6, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0 },
             { 7, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0 }, { 8, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1 },
             { 9, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1 }, { 10, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 } };
 
     @Before
     public void setUp() throws Exception {
         g = DirectedGraph.fromFile(dataFileLocation);
     }
 
     @Test
     public void testGetAdjencyMatrix() {
         GraphUtils.printAdjencyMatrix(g);
         assertTrue(Arrays.deepEquals(verificationMatrix, g.getAdjacencyMatrix()));
     }
 
 }
