 package com.mukhar.wk3;
 
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Logger;
 import com.mukhar.commons.FilePicker;
 
 /**
  * Implementation of Karger's Min Cut Algorithm. (See
  * http://en.wikipedia.org/wiki/Karger's_algorithm) Find the minimum cut of the
  * graph given by Edges E and Nodes V. The algorithm randomly picks an edge and
  * then combines the two nodes given by the edge. Edges between the two nodes
  * are removed. Edges between either of the two nodes and other nodes are
  * modified to go between the new combined node and the original other node.
  * When only two (super)nodes remain, the minimum cut is the number of edges
  * that remain. The value after a single run of the algorithm is probably not
  * the actual minimum cut. However the algorithm is run over many trials, and
  * the smallest result is tracked. After a large enough number of trials, the
  * algorithm should successfully find the true minimum cut. This code outputs
  * the minimum number of edges.<br>
  * 
  * The data file contains the adjacency list representation of a simple
  * undirected graph. There are 200 vertices labeled 1 to 200. The first column
  * in the file represents the vertex label, and the particular row (other
  * entries except the first column) tells all the vertices that the vertex is
  * adjacent to. So for example, the 6th row might look like :<br>
  * "6  155 56  52  120 ......". <br>
  * This just means that the vertex with label 6 is adjacent to (i.e., shares an
  * edge with) the vertices with labels 155,56,52,120,......,etc.<br>
  * 
  * Each edge is listed twice. For example, given row 6 as above, row 155 might
  * look like<br>
  * "155 91 185 53 164 108 145 171 200 6 ......"<br>
  * Any row that begins with "#" is treated as a comment and is not processed.
  */
 public class GraphCutter {
   private HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
   private List<Edge> edges = new CopyOnWriteArrayList<Edge>();
   private Random rand = new Random(System.currentTimeMillis());
 
   /**
    * Constructor. Reads the Adjacency List data from the data file.
    */
   public GraphCutter(File f) {
     DataReader dr = new DataReader();
     dr.readFile(f, nodes, edges);
   }
 
   public static void main(String[] args)
   {
     int trial = 0;
     int minCut = Integer.MAX_VALUE;
     File f = FilePicker.selectFile();
     while (true) {
       GraphCutter gc = new GraphCutter(f);
       gc.findMinCut();
       if (gc.edges.size() < minCut) {
         minCut = gc.edges.size();
       }
       Logger logger = Logger.getAnonymousLogger();
       logger.info("Trial " + ++trial + ": Min cut: " + minCut);
     }
   }
 
   /**
    * While the number of nodes is greater than 2, randomly pick and edge and
    * combine the two nodes connected by the edge.
    */
   private void findMinCut()
   {
     while (nodes.size() > 2) {
       // randomly pick edge and remove it from the list of edges
       Edge e = edges.remove(rand.nextInt(edges.size()));
 
       // combine nodes connected by edge
       Node n1 = nodes.remove(e.node1);
       assert n1 != null;
       Node n2 = nodes.remove(e.node2);
       assert n2 != null;
 
       // Add all node IDs from n2 to n1
       n1.addAll(n2);
       //only return n1 to the list of nodes
       nodes.put(n1.getId(), n1);
 
       // cleanup edges
       cleanupEdges(e);
     }
   }
 
   /**
    * For the given Edge e, remove any duplicate edges from the list of edges,
    * and modify any existing edges by replacing the value of node2 in the
    * existing edge to have the value of node1 (which is the new supernode).
    */
   private void cleanupEdges(Edge e)
   {
     int node1 = e.node1;
     int node2 = e.node2;
 
     for (Edge e2 : edges) {
       if (e2.equals(e)) {
         edges.remove(e2);
         continue;
       }
 
       if (e2.node1 == node2) {
         e2.setNode1(node1);
       } else if (e2.node2 == node2) {
         e2.setNode2(node1);
       }
     }
   }
 }
