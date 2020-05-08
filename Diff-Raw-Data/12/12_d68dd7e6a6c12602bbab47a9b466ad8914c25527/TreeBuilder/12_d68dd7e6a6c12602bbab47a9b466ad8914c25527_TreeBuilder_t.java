 /*
  * Copyright (c) 2001, Mikael Stldal
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * 3. Neither the name of the author nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Note: This is known as "the modified BSD license". It's an approved
  * Open Source and Free Software license, see
  * http://www.opensource.org/licenses/
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.xtree;
 
 import java.util.*;
 import java.io.*;
 import java.net.URL;
 
 import org.xml.sax.*;
 import javax.xml.parsers.*;
 
 public class TreeBuilder implements ContentHandler, ErrorHandler
 {
 	public static final boolean DEBUG = false;
 
     private static final String XML_NS =
     	"http://www.w3.org/XML/1998/namespace";
 
 	private Element rootElement = null;
 	private Stack elementStack;
 	private URL baseURI;
 	private Vector nsPrefix = null;
 	private Vector nsURI = null;
     private StringBuffer textBuffer = null;
     private Locator locator = null;
     private String textSystemId = null;
     private int textLine = -1;
     private int textColumn = -1;
 
 
 	/**
 	 * Parse an XML file into an XTree.
 	 * Uses JAXP to find a parser.
 	 * Will not support xml:base.
 	 *
 	 * @param xmlFile  the file to parse.
 	 * @param validate  should the file be validated?
 	 *
 	 * @throws SAXException  if the file doesn't contain a well-formed
 	 * (valid) XML document.
 	 * @throws FileNotFoundException  if the file doesn't exist
 	 * @throws IOException  if there was some I/O error while reading the file.
 	 * @throws ParserConfigurationExcpetion  if a JAXP parser is not properly
 	 * setup
 	 */
 	public static Element parseXMLFile(File xmlFile, boolean validate)
 		throws SAXException, FileNotFoundException, IOException,
 		ParserConfigurationException
 	{
        // we should use File.toURL() here, but it's Java2
		String absPath = 
			xmlFile.getCanonicalPath().replace(File.separatorChar, '/');
        String systemId = "file:" 
				+ ((absPath.charAt(0) == '/') ? "//" : "///") + absPath;

 		if (!xmlFile.isFile())
			throw new FileNotFoundException(systemId);
 
		return parseXML(new InputSource(systemId), validate);
 	}
 
 
 	/**
 	 * Parse XML into an XTree.
 	 * Uses JAXP to find a parser.
 	 * Will not support xml:base.
 	 *
 	 * @param xmlInput  the input to parse.
 	 * @param validate  should the file be validated?
 	 *
 	 * @throws SAXException  if the file doesn't contain a well-formed
 	 * (valid) XML document.
 	 * @throws FileNotFoundException  if the file doesn't exist
 	 * @throws IOException  if there was some I/O error while reading the file.
 	 * @throws ParserConfigurationExcpetion  if a JAXP parser is not properly
 	 * setup
 	 */
 	public static Element parseXML(InputSource xmlInput, boolean validate)
 		throws SAXException, FileNotFoundException, IOException,
 		ParserConfigurationException
 	{
 		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
 		parserFactory.setNamespaceAware(true);
 		parserFactory.setValidating(validate);
 
 		XMLReader xmlReader = parserFactory.newSAXParser().getXMLReader();
 		TreeBuilder tb = new TreeBuilder();
 
 		xmlReader.setContentHandler(tb);
 		xmlReader.setErrorHandler(tb);
 
 		xmlReader.parse(xmlInput);
 
 		return tb.getTree();
 	}
 
 
 	/**
 	 * Constructs a TreeBuilder, ready to receive SAX events.
 	 * Will not support xml:base.
 	 */
 	public TreeBuilder()
 	{
 		elementStack = new Stack();
 		baseURI = null;
 	}
 
 
 	/**
 	 * Constructs a TreeBuilder, ready to receive SAX events.
 	 *
 	 * @param base  base URL for the document, to support xml:base.
 	 */
 	public TreeBuilder(URL base)
 	{
 		elementStack = new Stack();
 		baseURI = base;
 	}
 
 
 	/**
 	 * Obtain the XTree built from SAX events.
 	 *
 	 * @throws IllegalStateException  if the SAX events received so far
 	 * doesn't constitues a well-formed XML document.
 	 */
 	public Element getTree()
 		throws IllegalStateException
 	{
 		if (!elementStack.isEmpty())
 			throw new IllegalStateException("All elements are not yet ended");
 
 		if (rootElement == null)
 			throw new IllegalStateException("No root element");
 
 		return rootElement;
 	}
 
 
     private void addCharacters()
     {
         if ((textBuffer != null) && (textBuffer.length() > 0))
         {
             Element parent = (Element)elementStack.peek();
 			Node node = new Text(textBuffer.toString());
 			node.systemId = textSystemId;
 			node.line = textLine;
 			node.column = textColumn;
 	    	parent.addChild(node);
         }
 
         textBuffer = null;
     }
 
 
     // ContentHandler implementation
 
     public void setDocumentLocator(Locator locator)
     {
 		this.locator = locator;
     }
 
     public void startDocument()
         throws SAXException
     {
 		// nothing to do
     }
 
     public void endDocument()
         throws SAXException
     {
 		// nothing to do
     }
 
     public void startElement(String namespaceURI, String localName,
                              String qname, Attributes atts)
         throws SAXException
     {
         addCharacters();
 
         if (DEBUG) System.out.println("startElement("+namespaceURI+','+
 			localName+','+qname+')');
 
 		Element el = new Element(namespaceURI, localName, atts.getLength());
 		if (locator != null)
 		{
 			el.systemId = locator.getSystemId();
 			el.line = locator.getLineNumber();
 			el.column = locator.getColumnNumber();
 		}
 		if (rootElement == null)
 		{
 			rootElement = el;
 			if (baseURI != null) rootElement.setBaseURI(baseURI);
 		}
 		else
 		{
 			Element parent = (Element)elementStack.peek();
 			parent.addChild(el);
 		}
 
         for (int i = 0; i < atts.getLength(); i++)
         {
             el.addAttribute(atts.getURI(i), atts.getLocalName(i),
             	atts.getType(i), atts.getValue(i));
             if (atts.getURI(i).equals(XML_NS) &&
             		atts.getLocalName(i).equals("base"))
             {
 				try {
 					URL url = new URL(el.getBaseURI(), atts.getValue(i));
 					el.setBaseURI(url);
 				}
 				catch (java.net.MalformedURLException e)
 				{
 					throw new SAXException(e);
 				}
 
 			}
         }
 
      	if (nsPrefix != null)
      	{
 			el.setNamespaceMappings(nsPrefix, nsURI);
 		}
 
         elementStack.push(el);
 
         nsPrefix = null;
         nsURI = null;
     }
 
     public void endElement(String namespaceURI, String localName,
                            String qname)
         throws SAXException
     {
         addCharacters();
 
 		if (DEBUG) System.out.println("endElement("+namespaceURI+','
             +localName+','+qname+')');
 
 		elementStack.pop();
     }
 
     public void startPrefixMapping(String prefix, String uri)
         throws SAXException
     {
 		if (DEBUG) System.out.println("startPrefixMapping("+
 			((prefix.length() == 0) ? "<default>" : prefix)+','+
 			uri+')');
 
 		if (nsPrefix == null)
 		{
 			nsPrefix = new Vector();
 			nsURI = new Vector();
 		}
 		nsPrefix.addElement(prefix);
 		nsURI.addElement(uri);
     }
 
     public void endPrefixMapping(String prefix)
         throws SAXException
     {
 		if (DEBUG) System.out.println("endPrefixMapping("+
 			((prefix.length() == 0) ? "<default>" : prefix)+')');
 
         // nothing to do
     }
 
     public void characters(char ch[], int start, int length)
         throws SAXException
     {
         if (textBuffer == null)
         {
             textBuffer = new StringBuffer(length);
         }
 
         textBuffer.append(ch, start, length);
 		if (locator != null)
 		{
 			textSystemId = locator.getSystemId();
 			textLine = locator.getLineNumber();
 			textColumn = locator.getColumnNumber();
 		}
     }
 
     public void ignorableWhitespace(char ch[], int start, int length)
         throws SAXException
     {
 		// nothing to do
     }
 
     public void processingInstruction(String target, String data)
         throws SAXException
     {
         addCharacters();
 
 		if (DEBUG) System.out.println("processingInstruction("+target+','+data+')');
 
 		Element parent = (Element)elementStack.peek();
 		Node node = new ProcessingInstruction(target, data);
 		if (locator != null)
 		{
 			node.systemId = locator.getSystemId();
 			node.line = locator.getLineNumber();
 			node.column = locator.getColumnNumber();
 		}
 		parent.addChild(node);
     }
 
     public void skippedEntity(String name)
         throws SAXException
     {
         // nothing to do
     }
 
 
 	// ErrorHandler implementation
 
 	public void fatalError(SAXParseException e) throws SAXParseException
 	{
 		throw e;
 	}
 
 	public void error(SAXParseException e) throws SAXParseException
 	{
 		throw e;
 	}
 
 	public void warning(SAXParseException e)
 	{
 		// do nothing
 	}
 
 }
