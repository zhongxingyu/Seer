 package edu.wustl.cab2b.server.path.pathgen;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 /**
  * An immutable representation of a path as a collection of the following:
  * <ul>
  * <li>Source/From {@link edu.wustl.cab2b.server.path.pathgen.Node}
  * <li> Destination/To {@link edu.wustl.cab2b.server.path.pathgen.Node}
  * <li> A {@link java.util.List} of intermediate nodes needed to traverse from
  * <code>fromNode</code> to <code>toNode</code>.
  * </ul>
  * <p>
  * A path is a <i>self-edge</i> if and only if
  * <code>fromNode.equals(toNode)</code>. A node can occur twice in a path if
  * and only if the path is a self-edge; in other words, if a path is not a
  * self-edge, a node can appear in the path at atmost one of the following
  * locations:
  * <ul>
  * <li><code>fromNode</code>
  * <li><code>toNode</code>
  * <li>one of the <code>intermediateNodes</code>
  * </ul>
  * This class itself does not provide validation checks for this, since the
  * validation checks may hamper performance.
  * @author srinath_k
  */
 public class Path {
     private SourceDestinationPair sdp;
 
     private List<Node> intermediateNodes;
 
     private int hashCode;
 
     Path(Node fromNode, Node toNode) {
         this(fromNode, toNode, null);
     }
 
     Path(Node fromNode, Node toNode, List<Node> intermediateNodes) {
         this.sdp = new SourceDestinationPair(fromNode, toNode);
         if (intermediateNodes == null) {
             this.intermediateNodes = Collections.unmodifiableList(new ArrayList<Node>(0));
         } else {
             this.intermediateNodes = Collections.unmodifiableList(intermediateNodes);
         }
     }
 
     public boolean containsNode(Node node) {
         return fromNode().equals(node) || toNode().equals(node) || getIntermediateNodes().contains(node);
     }
 
     public boolean isCyclePresent() {
         if (getIntermediateNodes().isEmpty()) {
             return false;
         }
         Set<Node> nodesSet = new HashSet<Node>(getIntermediateNodes());
         nodesSet.add(fromNode());
         nodesSet.add(toNode());
        return nodesSet.size() == getIntermediateNodes().size() + 2;
     }
 
     public boolean isSelfEdge() {
         return getIntermediateNodes().isEmpty() && fromNode().equals(toNode());
     }
 
     /**
      * @param origVal
      *            the node to be replaced.
      * @param replacement
      *            the new node which will replace origVal.
      * @return a newly constructed path which has the <code>replacement</code>
      *         node in place of the <code>origVal</code> node.
      */
     Path replace(Node origVal, Node replacement) {
         if (isSelfEdge() && fromNode().equals(origVal)) {
             return new Path(replacement, replacement);
         }
         if (fromNode().equals(origVal)) {
             return new Path(replacement, toNode(), getIntermediateNodes());
         }
         if (toNode().equals(origVal)) {
             return new Path(fromNode(), replacement, getIntermediateNodes());
         }
         List<Node> intermediateNodes = new ArrayList<Node>(getIntermediateNodes().size());
 
         for (Node origNode : getIntermediateNodes()) {
             if (origNode.equals(origVal)) {
                 intermediateNodes.add(replacement);
             } else {
                 intermediateNodes.add(origNode);
             }
         }
         return new Path(fromNode(), toNode(), intermediateNodes);
     }
 
     public List<Node> getIntermediateNodes() {
         return intermediateNodes;
     }
 
     public Node fromNode() {
         return sdp.getSrcNode();
     }
 
     public Node toNode() {
         return sdp.getDestNode();
     }
 
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (!(obj instanceof Path)) {
             return false;
         }
         Path otherPath = (Path) obj;
         return otherPath.fromNode().equals(fromNode()) && otherPath.toNode().equals(toNode())
                 && otherPath.getIntermediateNodes().equals(getIntermediateNodes());
     }
 
     public int hashCode() {
         if (hashCode == 0) {
             hashCode = new HashCodeBuilder().append(fromNode()).append(toNode()).append(getIntermediateNodes()).toHashCode();
         }
         return hashCode;
     }
 
     public String toString() {
         String delim = "#";
         String s = fromNode().toString();
         for (Node intermediateNode : getIntermediateNodes()) {
             s += delim + intermediateNode;
         }
         s += delim + toNode();
         return s;
     }
 }
