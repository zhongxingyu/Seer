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
  * This class represents an XML Node. Any DOM document is a tree of type
  * XMLNode.
  * 
  * @author kgilmer
  * 
  */
 public class XmlNode {
 	private String tagName;
 
 	private String text;
 
 	private Map attributes;
 
 	private List childElements;
 
 	private XmlNode parentNode;
 
 	public XmlNode(String tagName) {
 		this.tagName = tagName;
 		attributes = new HashMap();
 	}
 
 	public XmlNode(String tagName, String value) {
 		this(tagName);
 		this.text = value;
 	}
 
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
 
 	public XmlNode(XmlNode parent, String tagName, String value) {
 		this(parent, tagName);
 		this.text = value;
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
 
 	public void setName(String tagName) {
 		this.tagName = tagName;
 	}
 
 	public String getValue() {
 		return text;
 	}
 
 	public void setValue(String text) {
 		this.text = text;
 	}
 
 	public String getAttribute(String name) {
 		return (String) attributes.get(name);
 	}
 
 	public void setAttribute(String name, String value) {
 		attributes.put(name, value);
 	}
 
 	public XmlNode addChildElement(XmlNode element) throws SelfReferenceException {
 		if (element == this) {
 			throw new SelfReferenceException(element);
 		}
 
 		getChildren().add(element);
 		return element;
 	}
 
 	public List getChildren() {
 		if (childElements == null) {
 			childElements = new ArrayList();
 		}
 
 		return childElements;
 	}
 
 	public Map getAttributes() {
 		return attributes;
 	}
 
 	public String toString() {
 		return printXml("");
 	}
 
 	private String printXml(String indent) {
 		String s = indent + "<" + this.tagName;
 
 		if (attributes != null) {
 			for (Iterator i = attributes.keySet().iterator(); i.hasNext();) {
 				String attrib = (String) i.next();
 
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
 			for (Iterator i = getChildren().iterator(); i.hasNext();) {
 				XmlNode n = (XmlNode) i.next();
 				s += n.printXml(indent + "  ");
 			}
 			s += indent;
 		} else if (text != null) {
 			s += ">" + makeSafeXML(text);
 		}
 
 		s += "</" + makeSafeXML(tagName) + ">\n";
 
 		return s;
 	}
 
 	public boolean childExists(String name) {
 		// TODO optimize
 
 		if (getChild(name) != null) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public XmlNode getChild(String nodeName) {
 		// TODO Optimize
 
 		for (Iterator i = getChildren().iterator(); i.hasNext();) {
 			XmlNode child = (XmlNode) i.next();
 			if (child.tagName.equals(nodeName)) {
 				return child;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Retrieve a node from this element using xpath-like notation.
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
 				if (index + 4 < string.length() && string.substring(index + 1, index + 4) == "amp;") {
 					continue;
 				} else if (index + 5 < string.length() && string.substring(index + 1, index + 5) == "apos;") {
 					continue;
 				} else if (index + 5 < string.length() && string.substring(index + 1, index + 5) == "quot;") {
 					continue;
 				} else if (index + 3 < string.length() && string.substring(index + 1, index + 3) == "lt;") {
 					continue;
 				} else if (index + 3 < string.length() && string.substring(index + 1, index + 3) == "gt;") {
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
