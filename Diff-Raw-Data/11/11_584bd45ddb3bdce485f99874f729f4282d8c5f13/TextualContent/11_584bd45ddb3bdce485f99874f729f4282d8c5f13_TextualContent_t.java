 package no.ut.trip.xml;
 
import no.ut.trip.xml.util.NodeListAdapter;

 import org.w3c.dom.Node;
 
 public class TextualContent {
     protected Node node;
 
     protected String value;
 
     public TextualContent(Node node) {
         this.node = node;
         setupNode(node);
     }
 
     protected void setupNode(Node node) {
        StringBuilder b = new StringBuilder();

        for (Node child : new NodeListAdapter<Node>(node.getChildNodes())) {
            b.append(child.getNodeValue());
        }

        value = b.toString();
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
