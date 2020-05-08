 package org.drooms.api;
 
 import org.apache.commons.lang3.tuple.ImmutablePair;
 
 /**
  * Represents a connection (a route) between two immediately adjacent
  * {@link Node}s in a {@link Playground}. Worms can move from one {@link Node}
  * to anoter only by using an {@link Edge}. The connection is always
  * bi-directional.
  */
 public class Edge {
 
     private final ImmutablePair<Node, Node> nodes;
 
     /**
      * Make two nodes immediately adjacent.
      * 
      * @param firstNode
      *            One node.
      * @param secondNode
      *            The other.
      */
     public Edge(final Node firstNode, final Node secondNode) {
         if (firstNode == null || secondNode == null) {
             throw new IllegalArgumentException("Neither nodes can be null.");
         } else if (firstNode.equals(secondNode)) {
             throw new IllegalArgumentException(
                     "Edges between the same node make no sense.");
         }
         if (firstNode.compareTo(secondNode) < 0) {
             this.nodes = ImmutablePair.of(firstNode, secondNode);
         } else {
             this.nodes = ImmutablePair.of(secondNode, firstNode);
         }
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof Edge)) {
             return false;
         }
         final Edge other = (Edge) obj;
         if (this.nodes == null) {
             if (other.nodes != null) {
                 return false;
             }
         } else if (!this.nodes.equals(other.nodes)) {
             return false;
         }
         return true;
     }
 
     /**
      * Retrieve nodes in this edge.
      * 
      * @return A pair of nodes. First node is always the least of the two. (See
     *         {@link Node#compareTo(Node)}.)
      */
     public ImmutablePair<Node, Node> getNodes() {
         return this.nodes;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result
                 + ((this.nodes == null) ? 0 : this.nodes.hashCode());
         return result;
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("Edge [nodes=").append(this.nodes).append("]");
         return builder.toString();
     }
 
 }
