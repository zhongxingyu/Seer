 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package edu.tufts.eecs.graphtheory.collapsiblegraph.edge;
 
 import edu.tufts.eecs.graphtheory.collapsiblegraph.node.Node;
 
 /**
  *
  * @author Jeremy
  */
 public class SkeletonEdge implements Edge {
 
     private Node sourceNode;
     private Node targetNode;
     private int hashCode;
 
     public SkeletonEdge(Node sourceNode, Node targetNode) {
         this.sourceNode = sourceNode;
         this.targetNode = targetNode;
         generateHashCode();
     }
 
     public void setSource(Node sourceNode) {
         this.sourceNode=sourceNode;
         generateHashCode();
     }
 
     public void setTarget(Node targetNode) {
         this.targetNode=targetNode;
         generateHashCode();
     }
 
     public Node getSource() {
        return sourceNode;
     }
 
     public Node getTarget() {
         return targetNode;
     }
 
     private void generateHashCode() {
         hashCode = (sourceNode.getName()+targetNode.getName()).hashCode();
     }
 
     @Override
     public int hashCode() {
         return hashCode;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final SkeletonEdge other = (SkeletonEdge) obj;
         if (this.sourceNode != other.sourceNode && (this.sourceNode == null || !this.sourceNode.equals(other.sourceNode))) {
             return false;
         }
         if (this.targetNode != other.targetNode && (this.targetNode == null || !this.targetNode.equals(other.targetNode))) {
             return false;
         }
         if (this.hashCode != other.hashCode) {
             return false;
         }
         return true;
     }
 
 }
