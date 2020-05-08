 package com.dovico.commonlibrary;
 
 import org.w3c.dom.*; // XML objects (Element, NodeList, Node, etc)
 
 // Class to help handle working with XML data
 public class CXMLHelper {
 	// Helper to pull a node's value (same as calling the overloaded method and passing "" for the default value)
 	public static String getChildNodeValue(Element xeElement, String sTagName) {
 		// Call the overloaded version of this function with the default return value being an empty string
 		return getChildNodeValue(xeElement, sTagName, "");
 	}
 	
 	
 	// Helper to pull a node's value returning the Default value should the requested node not exist or is empty
 	/// <history>
     /// <modified author="C. Gerard Gallant" date="2011-11-28" reason="While testing a recent code modification, I discovered that xeElement.getElementsByTagName returns not just the node directly under the element queried but also any matching nodes for any sub-elements! This code has been modified to no longer use xeElement.getElementsByTagName and to loop over just the child elements."/>
     /// </history>
 	public static String getChildNodeValue(Element xeElement, String sTagName, String sDefaultValue) {
 		// Loop through the element's child nodes...(we were doing xeElement.getElementsByTagName originally but it was returning us even elements of sub-nodes which
 		// is not desired)
 		for (Node xnChild = xeElement.getFirstChild(); xnChild != null; xnChild = xnChild.getNextSibling()) {
 			// If the current child node is an element AND is the element we're looking for then...
 			if ((xnChild.getNodeType() == Node.ELEMENT_NODE) && (sTagName.equals(xnChild.getNodeName()))) {
 				// Grab the first child. If we have an object then return it's value 
 				Node xnFirstChild = xnChild.getFirstChild();
 				if(xnFirstChild != null) { return xnFirstChild.getNodeValue(); }
 				
 				// We found the item we were looking for so exit this loop now (in the event the element had no text child node)
 				break;
 			} // End if ((xnChild.getNodeType() == Node.ELEMENT_NODE) && (sTagName.equals(xnChild.getNodeName())))
 		} // End of the for (Node xnChild = xeElement.getFirstChild(); xnChild != null; xnChild = xnChild.getNextSibling()) loop.
 
 		
 		// Either the tag wasn't found OR there was no value provided. Return the default value to the caller instead
 		return sDefaultValue;
 	}
 	
 	
 	// The following is only public temporarily until I can come up with an XML String Builder class
 	//
 	/// <history>
     /// <modified author="C. Gerard Gallant" date="2012-03-28" reason="Created to help fix XML strings that are included as part of a larger XML string"/>
 	/// <modified author="C. Gerard Gallant" date="2012-04-11" reason="Moved here from the Import/Export tool's code base"/>
 	/// </history>
 	public static String fixXmlString(String sValue) {
 		// Change all unsafe XML characters into their safe ones
 		String sReturnVal = sValue.replace("&", "&amp;");
 		sReturnVal = sReturnVal.replace("'", "&apos;");
 		sReturnVal = sReturnVal.replace("\"", "&quot;");
 		sReturnVal = sReturnVal.replace("<", "&lt;");
 		return sReturnVal.replace(">", "&gt;");
 	}
 }
