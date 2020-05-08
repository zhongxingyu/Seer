 package com.mukhar.wk3;
 
 import java.io.*;
 import java.util.*;

import com.mukhar.commons.Edge;
 import com.mukhar.commons.LineDataReader;
import com.mukhar.commons.Node;
 
 public class DataReader {
   /**
    * Read the data file and build the list of nodes and edges. Even though the
    * data file lists edges twice, only one instance of the edge is stored.<br>
    * 
    * The data file contains the adjacency list representation of a simple
    * undirected graph. There are 200 vertices labeled 1 to 200. The first column
    * in the file represents the vertex label, and the particular row (other
    * entries except the first column) tells all the vertices that the vertex is
    * adjacent to. So for example, the 6th row might look like :<br>
    * "6  155 56  52  120 ......". <br>
    * This just means that the vertex with label 6 is adjacent to (i.e., shares
    * an edge with) the vertices with labels 155,56,52,120,......,etc.<br>
    * 
    * Each edge is listed twice. For example, given row 6 as above, row 155 might
    * look like<br>
    * "155 91 185 53 164 108 145 171 200 6 ......"<br>
    * Any row that begins with "#" is treated as a comment and is not processed.
    * 
    * @param f
    *          The data file
    * @param nodes
    *          The collection of nodes, indexed by node id
    * @param edges
    *          The collection of edges
    */
   public void readFile(File f, HashMap<Integer, Node> nodes, List<Edge> edges)
   {
     LineDataReader ldr = new LineDataReader();
     ArrayList<String> lines = ldr.readFile(f);
 
     for (String line : lines) {
       if (line.startsWith("#"))
         continue;
 
       String[] vals = line.split("\\s");
       boolean processedId = false;
       int id = 0;
       for (String val : vals) {
         if (!processedId) {
           id = Integer.parseInt(val);
           Node n = new Node(id);
           nodes.put(id, n);
           processedId = true;
         } else {
           Edge e = new Edge(id, Integer.parseInt(val));
           if (!edges.contains(e))
             edges.add(e);
         }
       }
     }
   }
 }
