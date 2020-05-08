 package edu.kpi.pzks.core.model;
 
 import edu.kpi.pzks.core.exceptions.ValidationException;
 
import java.util.Locale;
 import java.util.Objects;
import java.util.ResourceBundle;
 
 /**
  * @author Aloren
  */
 public class Link extends GraphObject {
 
     private static final long serialVersionUID = 3;
     private Node fromNode;
     private Node toNode;
    public static final Locale locale = Locale.getDefault();
    private ResourceBundle messages = ResourceBundle.getBundle("messages", locale);

 
     public Link() {
         this(null, null);
     }
 
     public Link(Node fromNode, Node toNode) {
         this(fromNode, toNode, 0);
     }
 
     public Link(Node fromNode, Node toNode, int weight) {
         super(weight);
         checkNodesEqual(fromNode, toNode);
         this.fromNode = fromNode;
         this.toNode = toNode;
         addToOutputNodes();
         addToInputNodes();
     }
 
     private void checkNodesEqual(Node fromNode, Node toNode) {
         if((fromNode != null && toNode != null) && fromNode.hashCode() == toNode.hashCode()) {
            throw new ValidationException(messages.getString("core.validation.error.cycles.direct"));
         }
     }
 
     public Node getFromNode() {
         return fromNode;
     }
 
     public void setFromNode(Node fromNode) {
         checkNodesEqual(fromNode, this.toNode);
         this.fromNode = fromNode;
 //        arrow.pn1 = node1;
     }
 
     public Node getToNode() {
         return toNode;
     }
 
     public void setToNode(Node toNode) {
         checkNodesEqual(this.fromNode, toNode);
         this.toNode = toNode;
         //TODO please move me to gui
 //        arrow.pn2 = node2;
     }
 
     void removeFromOutputNodes() {
         if (fromNode != null && toNode != null) {
             fromNode.getOutputNodes().remove(toNode);
         }
     }
 
     void removeFromInputNodes() {
         if (fromNode != null && toNode != null) {
             toNode.getInputNodes().remove(fromNode);
         }
     }
 
     private void addToOutputNodes() {
         if (fromNode != null && toNode != null) {
             fromNode.getOutputNodes().add(toNode);
         }
     }
 
     private void addToInputNodes() {
         if (fromNode != null && toNode != null) {
             toNode.getInputNodes().add(fromNode);
         }
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 79 * hash + Objects.hashCode(this.fromNode);
         hash = 79 * hash + Objects.hashCode(this.toNode);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Link other = (Link) obj;
         return Objects.equals(this.fromNode, other.fromNode) && Objects.equals(this.toNode, other.toNode);
     }
 }
