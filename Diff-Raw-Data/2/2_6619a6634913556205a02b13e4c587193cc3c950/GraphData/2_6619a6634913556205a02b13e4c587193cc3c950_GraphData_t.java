 package homeworks.week3;
 
 import java.util.*;
 
 public class GraphData implements Cloneable {
 
     private static Random RND = new Random();
 
     private final List<Integer> nodes;
     private final Map<Integer, List<Integer>> edges;
 
     private GraphData() {
         nodes = new ArrayList<Integer>();
         edges = new HashMap<Integer, List<Integer>>();
     }
 
     public GraphData(int[][] data) {
         this();
         for (int i = 0; i < data.length; i++) {
             int node = data[i][0];
             nodes.add(node);
 
             List<Integer> eList = new ArrayList<Integer>();
             for (int j = 1; j < data[i].length; j++) {
                 eList.add(data[i][j]);
             }
             edges.put(node, eList);
         }
     }
 
     public GraphData(GraphData graph) {
         this();
         for (Integer node: graph.nodes) {
             nodes.add(node);
             List<Integer> eList = graph.edges.get(node);
             edges.put(node, new ArrayList(eList));
         }
     }
 
     int getFirstNode() {
         int firstNodeIdx = RND.nextInt(nodes.size());
         return getNode(firstNodeIdx);
     }
 
     Integer getNode(int nodeIdx) {
         return nodes.get(nodeIdx);
     }
 
     int getSecondNode(int firstNode) {
         List<Integer> eList = edges.get(firstNode);
         int secondNodeIdx = RND.nextInt(eList.size());
         return eList.get(secondNodeIdx);
     }
 
     void mergeFirstToSecond(int firstNode, int secondNode) {
         List<Integer> eList = edges.get(secondNode);
 
         for (Integer node: edges.get(firstNode)) {
             List<Integer> otherNodes = edges.get(node);
             for (int i = 0; i < otherNodes.size(); i++) {
                 if (otherNodes.get(i).equals(firstNode)) {
                     otherNodes.set(i, secondNode);
                    eList.add(node);
                 }
             }
         }
     }
 
     void deleteNode(int node) {
         nodes.remove(Integer.valueOf(node));
         edges.remove(node);
     }
 
     void removeSelfLoops(int node) {
         Iterator<Integer> nodes = edges.get(node).iterator();
         while (nodes.hasNext()) {
             Integer otherNode = nodes.next();
             if (otherNode.equals(node))
                 nodes.remove();
         }
     }
 
     int getEdgesCount(int node) {
         return edges.get(node).size();
     }
 
     int getNotesCount() {
         return nodes.size();
     }
 }
