 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This class represents an XML Node. Any DOM document is a tree of <code>XMLNode</code>s.
  * 
  * @author kgilmer
  * 
  */
 public class XmlNode {
 	/**
 	 * Name of node.
 	 */
 	private String tagName = null;
 	/**
 	 * Content of node.
 	 */
 	private String text = null;
 
 	private Map<String, String> attributes;
 
 	private List<XmlNode> childElements;
 
 	private XmlNode parentNode = null;
 
 	/**
 	 * Create an empty node.
 	 * @param tagName
 	 */
 	public XmlNode(String tagName) {
 		this.tagName = tagName;
 		attributes = new HashMap<String, String>();
 	}
 
 	/**
 	 * Create a node with a String value.
 	 * @param tagName
 	 * @param value
 	 */
 	public XmlNode(String tagName, String value) {
 		this(tagName);
 		
 		if (value.length() > 0) {
 			this.text = value;
 		}
 	}
 	
 	/**
 	 * Create a node with children.
 	 * @param tagName
 	 * @param children
 	 */
 	public XmlNode(String tagName, List<XmlNode> children) {
 		this(tagName);
 		this.childElements = children;
 	}
 
 	/**
 	 * Create a node with a parent.
 	 * @param parent
 	 * @param tagName
 	 */
 	public XmlNode(XmlNode parent, String tagName) {
 		this(tagName);
 
 		try {
 			parent.addChildElement(this);
 		} catch (SelfReferenceException e) {
 			// this should never happen...new object creation.
 			e.printStackTrace();
 		}
 
 		this.parentNode = parent;
 	}
 	
 	/**
 	 * Create a node with a parent and children.
 	 * @param parent
 	 * @param tagName
 	 * @param children
 	 */
 	public XmlNode(XmlNode parent, String tagName, List<XmlNode> children) {
 		this(tagName);
 
 		try {
 			parent.addChildElement(this);
 		} catch (SelfReferenceException e) {
 			// this should never happen...new object creation.
 			e.printStackTrace();
 		}
 
 		this.parentNode = parent;
 		this.childElements = children;
 	}
 
 	/**
 	 * Create a node with a parent and a String value.
 	 * @param parent
 	 * @param tagName
 	 * @param value
 	 */
 	public XmlNode(XmlNode parent, String tagName, String value) {
 		this(parent, tagName);
		if (value.length() > 0) {
 			this.text = value;
 		}
 	}
 	
 	/**
 	 * @return true if the node has childern, false otherwise.
 	 */
 	public boolean hasChildren() {
 		return childElements != null && childElements.size() > 0;
 	}
 
 	/**
 	 * @return Name of this node.
 	 */
 	public String getName() {
 		return tagName;
 	}
 
 	/**
 	 * @param name
 	 * @param value
 	 * @return instance of self
 	 */
 	public XmlNode addAttribute(String name, String value) {
 		this.getAttributes().put(name, value);
 		
 		return this;
 	}
 
 	/**
 	 * Set the name of the tag.
 	 * @param tagName
 	 */
 	public void setName(String tagName) {
 		this.tagName = tagName;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getValue() {
 		return text;
 	}
 
 	public void setValue(String text) {
 		if (text != null && text.length() > 0 && hasChildren()) {
 			throw new RuntimeException("Cannot set content on a node that has children.");
 		}
 		
 		//If an empty string is passed, set text to null.  This allows children to be added to node.
 		if (text != null && text.length() == 0) {
 			this.text = null;
 		} else {
 			this.text = text;
 		}
 	}
 
 	/**
 	 * Get contents of attribute, or null if attribute does not exist.
 	 * @param name
 	 * @return
 	 */
 	public String getAttribute(String name) {
 		return (String) attributes.get(name);
 	}
 
 	/**
 	 * Set or overwrite existing attibute of node.
 	 * @param name
 	 * @param value
 	 */
 	public void setAttribute(String name, String value) {
 		attributes.put(name, value);
 	}
 	
 	/**
 	 * Clear the value of the XML node.
 	 */
 	public void clearValue() {
 		setValue(null);
 	}
 
 	/**
 	 * @param element
 	 * @return
 	 * @throws SelfReferenceException
 	 */
 	public XmlNode addChildElement(XmlNode element) throws SelfReferenceException {
 		if (element == this) {
 			throw new SelfReferenceException(element);
 		}
 		
 		if (this.text != null) {
 			throw new RuntimeException("Cannot add child elements to a node that has content.");
 		}
 
 		getChildren().add(element);
 		return element;
 	}
 	
 	/**
 	 * Equivalent to addChildElement except that unchecked exception is thrown on self referencing call.
 	 * @param element
 	 * @return
 	 */
 	public XmlNode addChild(XmlNode element) {
 		if (element == this) {
 			throw new RuntimeException("Cannot add node to itself.");
 		}
 		
 		if (this.text != null) {
 			throw new RuntimeException("Cannot add child elements to a node that has content.");
 		}
 
 		getChildren().add(element);
 		return element;
 	}
 
 	/**
 	 * @return children of node, or empty list if no children exist.
 	 */
 	public List<XmlNode> getChildren() {
 		if (childElements == null) {
 			childElements = new ArrayList<XmlNode>();
 		}
 
 		return childElements;
 	}
 
 	/**
 	 * @return Map of attribute names and values as Strings.
 	 */
 	public Map<String, String> getAttributes() {
 		return attributes;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return printXml("");
 	}
 
 	private String printXml(String indent) {
 		String s = indent + "<" + this.tagName;
 
 		if (attributes != null) {
 			for (Iterator<String> i = attributes.keySet().iterator(); i.hasNext();) {
 				String attrib = i.next();
 
 				// Only display attributes with non-null values.
 				if (attributes.get(attrib) != null) {
 					String value = attributes.get(attrib).toString();
 
 					s += " " + makeSafeXML(attrib) + "=\"" + makeSafeXML(value) + "\"";
 				}
 
 			}
 		}
 
 		if ((this.childElements == null || this.childElements.size() == 0) && text == null) {
 			s += "/>\n";
 
 			return s;
 		}
 
 		if (childElements != null && childElements.size() > 0) {
 			s += ">\n";
 			for (Iterator<XmlNode> i = getChildren().iterator(); i.hasNext();) {
 				s += i.next().printXml(indent + "  ");
 			}
 			s += indent;
 		} else if (text != null) {
 			s += ">" + makeSafeXML(text);
 		}
 
 		s += "</" + makeSafeXML(tagName) + ">\n";
 
 		return s;
 	}
 
 	/**
 	 * @param name
 	 * @return true if a node with the given name exists, false otherwise.
 	 */
 	public boolean childExists(String name) {
 		if (!hasChildren()) {
 			return false;
 		}
 
 		if (getChild(name) != null) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * @param nodeName
 	 * @return node with given name if exists or null otherwise.
 	 */
 	public XmlNode getChild(String nodeName) {
 		if (childElements == null) {
 			return null;
 		}
 
 		for (Iterator<XmlNode> i = getChildren().iterator(); i.hasNext();) {
 			XmlNode child = i.next() ;
 			if (child.tagName.equals(nodeName)) {
 				return child;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Retrieve a node from this element using xpath-like notation.
 	 * 
 	 * Ex. for <root><leaf1><leaf1><leaf2></root> call with "root/leaf1" to return first occurrence leaf1 node.
 	 * 
 	 * @param path
 	 * @return
 	 */
 	public XmlNode getFirstElement(String path) {
 		String[] elems = StringUtil.split(path, "/");
 		XmlNode root = this;
 		for (int i = 0; i < elems.length; ++i) {
 			root = (XmlNode) root.getChild(elems[i]);
 
 			if (root == null) {
 				break;
 			}
 		}
 
 		return root;
 	}
 
 	/**
 	 * @return The parent node or <code>null</code> if root node in DOM.
 	 */
 	public XmlNode getParent() {
 		return parentNode;
 	}
 
 	/**
 	 * @param parent
 	 * @throws SelfReferenceException
 	 */
 	public void setParent(XmlNode parent) throws SelfReferenceException {
 		if (parentNode != null) {
 			parentNode.getChildren().remove(this);
 		}
 
 		if (parent != null) {
 			parent.addChildElement(this);
 		}
 
 		parentNode = parent;
 	}
 
 	public static String makeSafeXML(String string) {
 		//garbage in, garbage out
 		if (string == null) {
 			return null;
 		}
 		//no occurences of any bad chars, don't spend the time examining every char
 		if (string.indexOf('&') == -1 && string.indexOf('\'') == -1 && string.indexOf('"') == -1 && string.indexOf('<') == -1 && string.indexOf('>') == -1) {
 			return string;
 		}
 
 		String temp = "";
 		for (int index = 0; index < string.length(); index++) {
 			if (string.charAt(index) == '&') {
 				if (index + 4 < string.length() && string.substring(index + 1, index + 4).equals("amp;")) {
 					continue;
 				} else if (index + 5 < string.length() && string.substring(index + 1, index + 5).equals("apos;")) {
 					continue;
 				} else if (index + 5 < string.length() && string.substring(index + 1, index + 5).equals("quot;")) {
 					continue;
 				} else if (index + 3 < string.length() && string.substring(index + 1, index + 3).equals("lt;")) {
 					continue;
 				} else if (index + 3 < string.length() && string.substring(index + 1, index + 3).equals("gt;")) {
 					continue;
 				}
 				temp += "&amp;";
 			} else if (string.charAt(index) == '"')
 				temp += "&quot;";
 			else if (string.charAt(index) == '\'')
 				temp += "&apos;";
 			else if (string.charAt(index) == '<')
 				temp += "&lt;";
 			else if (string.charAt(index) == '>')
 				temp += "&gt;";
 			else
 				temp += string.charAt(index);
 		}
 
 		return temp;
 
 	}
 }
