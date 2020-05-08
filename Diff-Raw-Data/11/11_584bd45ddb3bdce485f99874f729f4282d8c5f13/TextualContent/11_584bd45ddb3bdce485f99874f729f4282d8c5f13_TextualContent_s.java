 package no.ut.trip.xml;
 
 import org.w3c.dom.Node;
 
 public class TextualContent {
     protected Node node;
 
     protected String value;
 
     public TextualContent(Node node) {
         this.node = node;
         setupNode(node);
     }
 
     protected void setupNode(Node node) {
        value = node.getNodeValue();
     }
 
     public Node getNode() {
         return node;
     }
 
     public String getValue() {
         return value;
     }
 
     @Override
     public String toString() {
         return value;
     }
 }
