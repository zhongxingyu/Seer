package com.mukhar.commons;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  * A class representing a node (or super node) for the Karger Min Cut algorithm.
  * At initialization, the node contains a single id-- the id of this node. For
  * the Karger algorithm, an edge is selected and the nodes identified by the
  * edge are merged. The merge is performed by the addAll method.
  */
 public class Node {
   ArrayList<Integer> nodes = new ArrayList<Integer>();
 
   /**
    * Constructor
    * @param id2 The node id
    */
   public Node(int id2) {
     nodes.add(id2);
   }
 
   /**
    * Add all IDs stored in Node n to this node. Calling this method makes this
    * node a "supernode" which contains the IDs of all nodes merged into this
    * supernode. After adding all the IDs to this node, the collection of IDs
    * is sorted numerically.
    * 
    * @param n
    *          The node to add to this node.
    */
   public void addAll(Node n) {
     nodes.addAll(n.nodes);
     Collections.sort(nodes);
   }
 
   /**
    * Get the first (or lowest ordinal) ID in the collection of IDs contained by
    * this node. When first constructed, this node has only a single ID and the
    * method returns this single ID. After merging nodes, the ID of this node is
    * the ID contained by this node which has the lowest ordinal value.
    * 
    * @return The ID of this node.
    */
   public int getId() {
     return nodes.get(0);
   }
 }
