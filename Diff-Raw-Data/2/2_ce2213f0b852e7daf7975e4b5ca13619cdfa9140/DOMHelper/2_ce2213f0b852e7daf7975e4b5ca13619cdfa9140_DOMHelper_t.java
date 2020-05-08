 package niagara.utils;
 
 /*
  * $RCSfile:
  * $Revision:
  * $Date:
  * $Author:
  */
 
 
 /**
  * A <code> DOMHelper </code> class which implements some functions
  * which I wish were in the DOM specification - getting children by
  * name (instead of all descendents) and getting the first child which
  * is an element and so on. Perhaps I should put this in ndom??
  *
  * @version 1.0
  *
  * @author Kristin Tufte
  */
 
 import org.w3c.dom.*;
 import java.util.*;
 
 public class DOMHelper {
 
     /* this class should never be instantiated */
    private DOMHelper() {}
 
     /**
      * Returns the first child that is of type element. This and function
      * below give the ability to iterate through the children of type element
      *
      * @param parent The parent whose children we will search and return
      *
      * @return Returns the first child of 'parent' that is an element.
      */
     public static Element getFirstChildElement(Element parent) {
 	Node child = parent.getFirstChild();
 
 	while(child != null && child.getNodeType() != Node.ELEMENT_NODE) {
 	    child = child.getNextSibling();
 	}
 
 	return (Element)child;
     }
 
     /**
      * Returns the next sibling that is of type element. 
      *
      * @param me We find 'me's next sibling of type element.
      *
      * @return Returns the next sibling element.
      */
     public static Element getNextSiblingElement(Element me) {
 	Node sibling = me.getNextSibling();
 	while(sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) {
 	    sibling = sibling.getNextSibling();
 	}
 	/* hmm, I wonder if this will work if child is null... */
 	return (Element) sibling;
     }
 
     /**
      * Returns all children with the specified tag name. This differs
      * from getElementsByTagName in that it returns only children
      * elements, not all descendent elements
      *
      * @param parent The parent whose children are to be searched
      * @param tagName The tag name to be used to select child elements
      *
      * @return Returns an ArrayList with the appropriate children elements
      */
     public static ArrayList getChildElementsByTagName(Element parent, 
 						      String tagName) {
 	Node child = parent.getFirstChild();
 	
 	ArrayList returnList = new ArrayList();
 	
 	/* iterate through the list and find the all the children with the 
 	 * given tag name
 	 */
 	while(child != null) {
 	    if(tagName.equals(child.getNodeName()) && 
 	       child.getNodeType() == Node.ELEMENT_NODE) {
 		returnList.add((Element)child);
 	    }
 	    child = child.getNextSibling();
 	}
 
 	/* return whatever I've found */
 	return returnList;
     }
 
     /**
      * Returns the content of the first text child of the given node
      *
      * @param node The element to return the child of
      *
      * @return The first text child of node
      */
     public static String getTextValue(Node node) {
 	Node child = node.getFirstChild();
 	while(child != null && child.getNodeType() != Node.TEXT_NODE) {
 	    child = child.getNextSibling();
 	}
 	if(child == null) {
 	    return null;
 	} else {
 	    return child.getNodeValue();
 	}
     }
 
     /**
      * Changes the value of the text child (assume there is only one - uugh!!)
      * to the string passed as a param.
      *
      * @param node The element to return the child of
      * @param value The new text value
      *
      * @return The first text child of node
      */
     public static void setTextValue(Node node, String value) {
 	Node child = node.getFirstChild();
 	while(child != null && child.getNodeType() != Node.TEXT_NODE) {
 	    child = child.getNextSibling();
 	}
 	if(child == null) {
 	    node.appendChild(node.getOwnerDocument().createTextNode(value));
 	} else {
 	    child.setNodeValue(value);
 	}
     }
 
     /**
      * Retrieves the first child with tag name tagName
      *
      * @param parent The element whose children to search
      * @param tagName The tag name to search for
      *
      * @return The child with the specified tag name.
      *
      */
     public static Element getFirstChildEltByTagName(Element parent, 
 						 String tagName) {
 
 	NodeList nl = parent.getChildNodes();
 	int size = nl.getLength();
 	
 	/* iterate through the list and find the first child with the
 	 * given tag name
 	 */
 	for (int i = 0; i < size; i++){
 	    Node child = nl.item(i);
 	    if(tagName.equals(child.getNodeName()) &&
 	       child.getNodeType() == Node.ELEMENT_NODE) {
 		return (Element)child;
 	    }
 	}
 	return null;
     }
 
     /**
      * prints a document
      *
      * @param doc The document to be printed
      */
     public static void printDoc(Document doc) {
 	printElt(doc.getDocumentElement(), 0);
 
     }
     
     public static void printElt(Element elt) {
 	printElt(elt, 0);
     }
 
     private static void printElt(Element elt, int indentLevel) {
 	printStartTagAndAttrs(elt, indentLevel);
 	indentLevel++;
 	Node child = elt.getFirstChild();
 	while(child != null) {
 	    if(child.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
 		String text = child.getNodeValue();
 		text = eatWhite(text);
 		if(!(text.equals(""))) {
 		    printTextLine(child.getNodeValue(), indentLevel);
 		}
 	    } else if (child.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
 		printElt((Element)child, indentLevel);
 	    } else {
 		throw new PEException("Unexpected node type");
 	    }
 	    child = child.getNextSibling();
 	}
 	indentLevel--;
 	printEndTag(elt, indentLevel);
     }
 
     private static void printStartTagAndAttrs(Element elt, int indentLevel) {
 	StringBuffer text = new StringBuffer();
 	text.append("<");
 	text.append(elt.getTagName());
 	NamedNodeMap attrs = elt.getAttributes();
 	int numAttrs = attrs.getLength();
 	for(int i = 0; i<numAttrs; i++) {
 	    text.append(" ");
 	    text.append(((Attr)attrs.item(i)).getName());
 	    text.append("=\"");
 	    text.append(((Attr)attrs.item(i)).getValue());
 	    text.append("\"");
 	}
 	text.append(">");
 	printTextLine(text.toString(), indentLevel);
 	text = null;
 	return;
     }
 
     private static void printTextLine(String str, int indentLevel) {
 	for(int i = 0; i<indentLevel; i++) {
 	    System.out.print("   ");
 	}
 	System.out.println(str);
 	return;
     }
 
     private static void printEndTag(Element elt, int indentLevel) {
 	StringBuffer text = new StringBuffer();
 	text.append("<\\");
 	text.append(elt.getTagName());
 	text.append(">");
 	printTextLine(text.toString(), indentLevel);
 	return;
     }
 
     public static String eatWhite(String str) {
 	if(str == null)
 	    return str;
 	StringBuffer newStr = new StringBuffer();
 	char ch;
 	for(int i=0; i < str.length(); i++) {
 	    ch = str.charAt(i);
 	    if(!Character.isWhitespace(ch)) {
 		newStr.append(ch);
 	    }
 	}
 	return newStr.toString();	    
     }
 
     public static String nameAndValue(Element elt) {
 	StringBuffer newStr = new StringBuffer();
 	newStr.append(elt.getTagName());
 	newStr.append("(");
 	newStr.append(DOMHelper.eatWhite(DOMHelper.getTextValue(elt)));
 	newStr.append(")");
 	return newStr.toString();
     }
 
 }
 
 
