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
 
 import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.Stack;
 
 /**
  * <code>XMLParser</code> is a highly-simplified XML DOM parser. It does not
  * support namespaces.
  */
 public class XmlParser {
 	private static final int[] cdata_start = { '<', '!', '[', 'C', 'D', 'A', 'T', 'A', '[' };
 
 	private static final int[] cdata_end = { ']', ']', '>' };
 
 	private Reader reader;
 
 	private Stack elements;
 
 	private XmlNode currentElement;
 
 	/**
 	 * If set to true, namespace prefixes are stripped from node and attribute
 	 * names. Otherwise they are simply part of the names.
 	 */
 	private boolean ignoreNamespaces;
 
 	public XmlParser() {
 		elements = new Stack();
 		currentElement = null;
 		ignoreNamespaces = false;
 	}
 
 	public XmlParser(boolean ignoreNamespaces) {
 		this();
 		this.ignoreNamespaces = ignoreNamespaces;
 	}
 
 	/**
 	 * Parse a string containing XML into a tree of XMLElement nodes.
 	 * 
 	 * @param xml
 	 * @return XMLElement
 	 * @throws IOException
 	 */
 	public static XmlNode parse(String xml) throws IOException {
 		XmlParser p = new XmlParser();
 
 		return p.parse(new StringReader(xml));
 	}
 
 	/**
 	 * Parse a string containing XML into a tree of XMLElement nodes.
 	 * 
 	 * @param xml
 	 * @return XMLElement
 	 * @throws IOException
 	 */
 	public static XmlNode parse(String xml, boolean ignoreNamespaces) throws IOException {
 		XmlParser p = new XmlParser(ignoreNamespaces);
 
 		return p.parse(new StringReader(xml));
 	}
 
 	/**
 	 * Parse a string containing XML into a tree of XMLElement nodes.
 	 * 
 	 * @param xml
 	 * @return XMLElement
 	 * @throws IOException
 	 */
 	public static XmlNode parse(Reader reader, boolean ignoreNamespaces) throws IOException {
 		XmlParser p = new XmlParser(ignoreNamespaces);
 
 		return p.parse(reader);
 	}
 
 	public XmlNode parse(Reader reader) throws IOException {
 		if (!(reader.markSupported())) {
 			this.reader = new BufferedReader(reader);
 		} else {
 			this.reader = reader;
 		}
 
 		// skip xml declaration or DocTypes
 		skipPrologs();
 
 		while (true) {
 			int index;
 			String tagName;
 
 			// remove the prepend or trailing white spaces
 			String currentTag = readTag().trim();
 			if (currentTag.startsWith("</")) {
 				// close tag
 				tagName = currentTag.substring(2, currentTag.length() - 1);
 
 				if (ignoreNamespaces) {
 					tagName = stripNamespace(tagName);
 				}
 
 				// no open tag
 				if (currentElement == null) {
 					throw new IOException("Got close tag '" + tagName + "' without open tag.");
 				}
 
 				// close tag does not match with open tag
 				if (!tagName.equals(currentElement.getName())) {
 					throw new IOException("Expected close tag for '" + currentElement.getName() + "' but got '" + tagName + "'.");
 				}
 
 				if (elements.empty()) {
 					// document processing is over
 					return currentElement;
 				} else {
 					// pop up the previous open tag
 					currentElement = (XmlNode) elements.pop();
 				}
 			} else {
 				// open tag or tag with both open and close tags
 				index = currentTag.indexOf(" ");
 				if (index < 0) {
 					// tag with no attributes
 					if (currentTag.endsWith("/>")) {
 						// close tag as well
 						tagName = currentTag.substring(1, currentTag.length() - 2);
 						currentTag = "/>";
 					} else {
 						// open tag
 						tagName = currentTag.substring(1, currentTag.length() - 1);
 
 						if (ignoreNamespaces) {
 							tagName = stripNamespace(tagName);
 						}
 
 						currentTag = "";
 					}
 				} else {
 					// tag with attributes
 					tagName = currentTag.substring(1, index);
 
 					if (ignoreNamespaces) {
 						tagName = stripNamespace(tagName);
 					}
 
 					currentTag = currentTag.substring(index + 1);
 				}
 
 				// create new element
 				XmlNode element = new XmlNode(tagName, "");
 
 				// parse the attributes
 				boolean isTagClosed = false;
 				while (currentTag.length() > 0) {
 					// remove the prepend or trailing white spaces
 					currentTag = currentTag.trim();
 
 					if (currentTag.equals("/>")) {
 						// close tag
 						isTagClosed = true;
 						break;
 					} else if (currentTag.equals(">")) {
 						// open tag
 						break;
 					}
 
 					index = currentTag.indexOf("=");
 					if (index < 0) {
 						throw new IOException("Invalid attribute for tag '" + tagName + "'.");
 					}
 
 					// get attribute name
 					String attributeName = currentTag.substring(0, index);
 
 					if (ignoreNamespaces) {
 						attributeName = stripNamespace(attributeName);
 					}
 
 					currentTag = currentTag.substring(index + 1);
 
 					// get attribute value
 					String attributeValue;
 					boolean isQuoted = true;
 					if (currentTag.startsWith("\"")) {
 						index = currentTag.indexOf('"', 1);
 					} else if (currentTag.startsWith("'")) {
 						index = currentTag.indexOf('\'', 1);
 					} else {
 						isQuoted = false;
 						index = currentTag.indexOf(' ');
 						if (index < 0) {
 							index = currentTag.indexOf('>');
 							if (index < 0) {
 								index = currentTag.indexOf('/');
 							}
 						}
 					}
 
 					if (index < 0) {
 						throw new IOException("Invalid attribute for tag '" + tagName + "'.");
 					}
 
 					if (isQuoted) {
 						attributeValue = currentTag.substring(1, index);
 					} else {
 						attributeValue = currentTag.substring(0, index);
 					}
 
 					// add attribute to the new element
 					element.setAttribute(attributeName, attributeValue);
 
 					currentTag = currentTag.substring(index + 1);
 				}
 
 				// read the text between the open and close tag
 				if (!isTagClosed) {
 					element.setValue(readText());
 				}
 
 				// add new element as a child element of
 				// the current element
 				if (currentElement != null) {
 					try {
 						element.setParent(currentElement);
 					} catch (SelfReferenceException e) {
 						throw new IOException(e.getMessage());
 					}
 				}
 
 				if (!isTagClosed) {
 					if (currentElement != null) {
 						elements.push(currentElement);
 					}
 
 					currentElement = element;
 				} else if (currentElement == null) {
 					// only has one tag in the document
 					return element;
 				}
 			}
 		}
 	}
 
 	private String stripNamespace(String tagName) {
 		int i = tagName.indexOf(':');
 
 		if (i > -1) {
 			return tagName.substring(i + 1);
 		}
 
 		return tagName;
 	}
 
 	private int peek() throws IOException {
 		reader.mark(1);
 		int result = reader.read();
 		reader.reset();
 
 		return result;
 	}
 
 	private void peek(int[] buffer) throws IOException {
 		reader.mark(buffer.length);
 		for (int i = 0; i < buffer.length; i++) {
 			buffer[i] = reader.read();
 		}
 		reader.reset();
 	}
 
 	private void skipWhitespace() throws IOException {
 		while (Character.isWhitespace((char) peek())) {
 			reader.read();
 		}
 	}
 
 	private void skipProlog() throws IOException {
 		// skip "<?" or "<!"
 		reader.skip(2);
 
 		while (true) {
 			int next = peek();
 
 			if (next == '>') {
 				reader.read();
 				break;
 			} else if (next == '<') {
 				// nesting prolog
 				skipProlog();
 			} else {
 				reader.read();
 			}
 		}
 	}
 
 	private void skipPrologs() throws IOException {
 		while (true) {
 			skipWhitespace();
 
 			int[] next = new int[2];
 			peek(next);
 
 			if (next[0] != '<') {
 				throw new IOException("Expected '<' but got '" + (char) next[0] + "'.");
 			}
 
 			if ((next[1] == '?') || (next[1] == '!')) {
 				skipProlog();
 			} else {
 				break;
 			}
 		}
 	}
 
 	private String readTag() throws IOException {
 		skipWhitespace();
 
 		StringBuffer sb = new StringBuffer();
 
 		int next = peek();
 		if (next != '<') {
 			throw new IOException("Expected < but got " + (char) next);
 		}
 
 		sb.append((char) reader.read());
 		while (peek() != '>') {
 			sb.append((char) reader.read());
 		}
 		sb.append((char) reader.read());
 
 		return sb.toString();
 	}
 
 	private String readText() throws IOException {
 		StringBuffer sb = new StringBuffer();
 
 		int[] next = new int[cdata_start.length];
 		peek(next);
 		if (compareIntArrays(next, cdata_start) == true) {
 			// CDATA
 			reader.skip(next.length);
 
 			int[] buffer = new int[cdata_end.length];
 			while (true) {
 				peek(buffer);
 
 				if (compareIntArrays(buffer, cdata_end) == true) {
 					reader.skip(buffer.length);
 					break;
 				} else {
 					sb.append((char) reader.read());
 				}
 			}
 		} else {
 			while (peek() != '<') {
 				sb.append((char) reader.read());
 			}
 		}
 
 		return sb.toString();
 	}
 
 	private boolean compareIntArrays(int[] a1, int[] a2) {
 		if (a1.length != a2.length) {
 			return false;
 		}
 
 		for (int i = 0; i < a1.length; i++) {
 			if (a1[i] != a2[i]) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	public boolean isIgnoreNamespaces() {
 		return ignoreNamespaces;
 	}
 
 	public void setIgnoreNamespaces(boolean ignoreNamespaces) {
 		this.ignoreNamespaces = ignoreNamespaces;
 	}
 }
