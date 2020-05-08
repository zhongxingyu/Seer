 import java.io.*;
 import java.net.*;
 import java.util.*;
 import org.w3c.dom.*;
 import javax.xml.*;
 import javax.xml.parsers.*;
 import java.net.*;
 import java.io.FileNotFoundException;
 
 public class ResourceManager{
 	String[] routes;
 	public ResourceManager(){
 		//arbitrary constructor
 	}
 
 	public Object getData(Node node, String[] routes, int r) throws FileNotFoundException{
 	    // Print root node
 	    System.out.print(node.getNodeName());
 	    System.out.print("(");
 	    System.out.print(r + "/" + routes.length);
 	    System.out.println(")");
 
 	    // Make node list of children
 	    NodeList nodeList = node.getChildNodes();
 
 	    // Check if single node.
 			if (nodeList.getLength() == 1) {
 				// System.out.println("OUTPUT = " + nodeList.item(0).getTextContent());
 				return nodeList.item(0);
 
 			}	else {
 
 				Node firstNode = nodeList.item(1);
 
 		    
 		    // Check if first node has attributes
 		    if (firstNode.hasAttributes()) {
 		    	// System.out.println(node.getNodeName() + "'s CHILD HAS ATTRIBUTES");
 
 		    	if ((r+1) == routes.length) {
 		    		return nodeList;
 		    	}
 
 		    	// Get attribute to look for
 		    	String attribute = routes[r+1];
 		    	System.out.println("Searching for attribute " + attribute);
 
 		    	// Find child which has matching attribute
 		    	for (int i = 1; i < nodeList.getLength(); i=i+2) {//increments of 2 (and only looking at odds) because the nodeList is #text Tag #text Tag etc
 		        Node currentNode = nodeList.item(i);
 
 						if (currentNode.getAttributes().getNamedItem("id").getNodeValue().equals(attribute)){
 							return getData(currentNode, routes, r+2);
 		    		}
 		   		 }
 
 		    } else { //if no attribute
 		    	// System.out.println(node.getNodeName() + "' CHILD HAS NO ATTRIBUTES");

 		    	String tag = routes[r];
 		    	// System.out.println("TAG = "+ tag);
 
 		    	Node currentNode;
 		    	for (int i = 0; i<nodeList.getLength(); i++){
 		    		currentNode = nodeList.item(i);
 		    		if (currentNode.getNodeName().equals(tag)){
 		    			return getData(currentNode, routes, ++r);
 		    		}
 
 		    	}
 		    	throw new FileNotFoundException();
 
 		    }
 	    }
     return null;
 	}
 }
