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
 	public static String getChildNodeValue(Element xeElement, String sTagName, String sDefaultValue) {
		// Get the list of elements with the specified tag name. If one or more elements were found then...
		NodeList xnlTags = xeElement.getElementsByTagName(sTagName);
		if(xnlTags.getLength() > 0)	{
			// Grab the first item casting it to an Element		
			Element xeItem = (Element)xnlTags.item(0);
		
			// Grab the first child. If we have an object then return it's value 
			Node xnFirstChild = xeItem.getFirstChild();
			if(xnFirstChild != null) { return xnFirstChild.getNodeValue(); }
		} // End if(xnlTags.getLength() > 0)
 		
 		// Either the tag wasn't found OR there was no value provided. Return the default value to the caller instead
 		return sDefaultValue;
 	}
 }
