package com.mukhar.wk3;
 
 public class Edge {
   /**
    * Constructor
    */
   public Edge(int id1, int id2) {
     if (id1 < id2) {
       node1 = id1;
       node2 = id2;
     } else {
       node1 = id2;
       node2 = id1;
     }
   }
 
   public int node1;
   public int node2;
 
   @Override
   public int hashCode()
   {
     final int prime = 31;
     int result = 1;
     result = prime * result + node1;
     result = prime * result + node2;
     return result;
   }
 
   @Override
   public boolean equals(Object obj)
   {
     if (this == obj) {
       return true;
     }
     if (obj == null) {
       return false;
     }
     if (getClass() != obj.getClass()) {
       return false;
     }
     Edge other = (Edge) obj;
     if (node1 != other.node1) {
       return false;
     }
     if (node2 != other.node2) {
       return false;
     }
     return true;
   }
 
   /**
    * Set the value of node1 to nodeId. After setting the value, the class swaps
    * the node IDs of node1 and node2 if necessary to ensure that node1 id <
    * node2 id.
    * 
    * @param nodeId
    *          The new value of the node1 value of the edge.
    */
   public void setNode1(int nodeId)
   {
     node1 = nodeId;
     ensureNodeOrder();
   }
 
   /**
    * Set the value of node2 to nodeId. After setting the value, the class swaps
    * the node IDs of node1 and node2 if necessary to ensure that node1 id <
    * node2 id.
    * 
    * @param nodeId
    *          The new value of the node1 value of the edge.
    */
   public void setNode2(int nodeId)
   {
     node2 = nodeId;
     ensureNodeOrder();
   }
 
   /**
    * Checks that node1 < node2 (comparing the node ids), and swaps the node ids
    * as necessary to ensure that node1 < node2.
    */
   private void ensureNodeOrder()
   {
     if (node2 < node1) {
       int temp = node1;
       node1 = node2;
       node2 = temp;
     }
   }
 }
