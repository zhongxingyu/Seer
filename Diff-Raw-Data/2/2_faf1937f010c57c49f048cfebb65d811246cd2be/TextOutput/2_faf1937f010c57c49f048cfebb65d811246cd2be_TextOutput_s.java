 /*
  * @(#)$Id$
  *
  * The Apache Software License, Version 1.1
  *
  *
  * Copyright (c) 2001 The Apache Software Foundation.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution,
  *    if any, must include the following acknowledgment:
  *       "This product includes software developed by the
  *        Apache Software Foundation (http://www.apache.org/)."
  *    Alternately, this acknowledgment may appear in the software itself
  *    if and wherever such third-party acknowledgments normally appear.
  *
  * 4. The names "Xalan" and "Apache Software Foundation" must
  *    not be used to endorse or promote products derived from this
  *    software without prior written permission. For written
  *    permission, please contact apache@apache.org.
  *
  * 5. Products derived from this software may not be called "Apache",
  *    nor may "Apache" appear in their name, without prior written
  *    permission of the Apache Software Foundation.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  *
  * This software consists of voluntary contributions made by many
  * individuals on behalf of the Apache Software Foundation and was
  * originally based on software copyright (c) 2001, Sun
  * Microsystems., http://www.sun.com.  For more
  * information on the Apache Software Foundation, please see
  * <http://www.apache.org/>.
  *
  * @author Jacek Ambroziak
  * @author Santiago Pericas-Geertsen
  * @author Morten Jorgensen
  *
  */
 
 package org.apache.xalan.xsltc.runtime;
 
 import java.io.*;
 import java.util.Stack;
 import java.util.Enumeration;
 
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.ext.LexicalHandler;
 
 import org.apache.xalan.xsltc.*;
 
 public final class TextOutput implements TransletOutputHandler {
 
     // These are the various output types we handle
     public static final int UNKNOWN = -1; // determine type from output contents
     public static final int TEXT    = 0;
     public static final int XML     = 1;
     public static final int HTML    = 2;
     public static final int QNAME   = 3;  // no special handling
 
     // These parameters are set by the <xsl:output> element, or by the
     // get/setOutputProperty() methods in TrAX
     private int	   _outputType = UNKNOWN;
     private String _encoding;
     private String _mediaType = "text/html";
     private String _doctypeSystem = null;
     private String _doctypePublic = null;
 
     private boolean   _escapeChars = false;
     private boolean   _startTagOpen = false;
     private boolean   _headTagOpen = false;
     private boolean   _cdataTagOpen = false;
 
     // Contains all elements that should be output as CDATA sections
     private Hashtable _cdata = null;
 
     private static final String XML_PREFIX = "xml";
 
     private static final char[] AMP      = "&amp;".toCharArray();
     private static final char[] LT       = "&lt;".toCharArray();
     private static final char[] GT       = "&gt;".toCharArray();
     private static final char[] CRLF     = "&#xA;".toCharArray();
     private static final char[] QUOTE    = "&quot;".toCharArray();
     private static final char[] NBSP     = "&nbsp;".toCharArray();
 
     private static final int AMP_length   = AMP.length;
     private static final int LT_length    = LT.length;
     private static final int GT_length    = GT.length;
     private static final int CRLF_length  = CRLF.length;
     private static final int QUOTE_length = QUOTE.length;
     private static final int NBSP_length  = NBSP.length;
 
     private static final char[] BEGCDATA = "<![CDATA[".toCharArray();
     private static final char[] ENDCDATA = "]]>".toCharArray();
     private static final char[] CNTCDATA = "]]]]><![CDATA[>".toCharArray();
     private static final char[] BEGCOMM  = "<!--".toCharArray();
     private static final char[] ENDCOMM  = "-->\n".toCharArray();
 
     private static final String EMPTYSTRING = "";
 
     private AttributeList _attributes = new AttributeList();
     private String        _elementName = null;
 
     // Each entry (prefix) in this hashtable points to a Stack of URIs
     private Hashtable _namespaces;
     // The top of this stack contains an id of the element that last declared
     // a namespace. Used to ensure prefix/uri map scopes are closed correctly
     private Stack     _nodeStack;
     // The top of this stack is the prefix that was last mapped to an URI
     private Stack     _prefixStack;
 
     // The top of this stack contains the QName of the currently open element
     private Stack     _qnameStack;
 
     // The top of this stack contains the element id of the last element whose
     // contents should be output as CDATA sections.
     private Stack     _cdataStack;
 
     // Holds the current tree depth (see startElement() and endElement()).
     private int _depth = 0;
 
     // Reference to the SAX2 handlers that consume this handler's output
     private ContentHandler _saxHandler;
     private LexicalHandler _lexHandler;
 
     /**
      * Creates a new translet output post-processor
      *
      * @param handler A SAX2 handler to consume the generated SAX events 
      * @throws IOException
      */
     public TextOutput(ContentHandler handler) throws IOException {
         _saxHandler = handler;
         init();
     }
 
     /**
      * Creates a new translet output post-processor
      *
      * @param handler A SAX2 handler to consume the generated SAX events 
      * @param encoding The default encoding to use (set in <xsl:output>)
      * @throws IOException
      */
     public TextOutput(ContentHandler handler, String encoding)
 	throws IOException {
         _saxHandler = handler;
         init();
 	_encoding = encoding;
     }
 
     /**
      * Creates a new translet output post-processor
      *
      * @param handler A SAX2 handler to consume the generated SAX events 
      * @param encoding The default encoding to use (set in <xsl:output>)
      * @throws IOException
      */
     public TextOutput(ContentHandler sax, LexicalHandler lex, String encoding)
 	throws IOException {
         _saxHandler = sax;
 	_lexHandler = lex;
         init();
 	_encoding = encoding;
     }
 
     /**
      * Initialise global variables
      */
     private void init() throws IOException {
 	// Reset all output configuration from <xsl:output>
 	_outputType = UNKNOWN;
 	_encoding = "utf-8";
 	_mediaType = "text/html";
 
 	// Reset all internal variables and tables
 	_escapeChars  = false;
 	_startTagOpen = false;
 	_cdataTagOpen = false;
 	_qnameStack = new Stack();
 	_cdataStack = new Stack();
 	_cdataStack.push(new Integer(-1)); // push dummy value
 
 	// Reset our internal namespace handling
 	initNamespaces();
     }
 
     /**
      * This method is used internally when the output type was initially
      * undefined and the type is set (by this handler) based on the contents
      * of the output. Set the default values for some output paramters.
      */
     private void setTypeInternal(int type) {
 	if (type == XML) {
 	    _escapeChars = true;
 	}
 	else if (type == HTML) {
 	    _escapeChars = true;
 	    setIndent(true);
 	}
 	setType(type);
     }
 
     /**
      * Emit header through the SAX handler
      */
     private void emitHeader() throws SAXException {
 	// Output HTML header as META element
 	if (_outputType == HTML) {
 	    AttributeList attrs = new AttributeList();
 	    attrs.add("http-equiv", "Content-Type");
 	    attrs.add("content", _mediaType+"; charset="+_encoding);
 	    _saxHandler.startElement(null, null, "meta", attrs);
 	    _saxHandler.endElement(null, null, "meta");
 	}
     }
 
     /**
      * This method is called when all the data needed for a call to the
      * SAX handler's startElement() method has been gathered.
      */
     public void closeStartTag() throws TransletException {
 	try {
 	    _startTagOpen = false;
 	    
 	    // Final check to assure that the element is within a namespace
 	    // that has been declared (all declarations for this element
 	    // should have been processed at this point).
 	    int col = _elementName.lastIndexOf(':');
 	    if (col > 0) {
 		final String prefix = _elementName.substring(0,col);
 		final String localname = _elementName.substring(col+1);
 		final String uri = lookupNamespace(prefix);
 		if (uri == null) {
 		    throw new TransletException("Namespace for prefix "+
 						prefix+" has not been "+
 						"declared.");
 		}
 		_saxHandler.startElement(uri, localname,
 					 _elementName, _attributes);
 	    }
 	    else {
 		final String uri = lookupNamespace(EMPTYSTRING);
 		_saxHandler.startElement(uri, _elementName,
 					 _elementName, _attributes);
 	    }
 
 	    // Insert <META> tag directly after <HEAD> element in HTML output
 	    if (_headTagOpen) {
 		emitHeader();
 		_headTagOpen = false;
 	    }
 	}
 	catch (SAXException e) {
 	    throw new TransletException(e);
 	}
     } 
 
     /**
      * Turns special character escaping on/off. Note that characters will
      * never, even if this option is set to 'true', be escaped within
      * CDATA sections in output XML documents.
      */
     public boolean setEscaping(boolean escape) throws TransletException {
 	// Set output type to XML (the default) if still unknown.
 	if (_outputType == UNKNOWN) setTypeInternal(XML);
 
 	// Save old escaping setting (for the return value) and set new value
 	boolean oldSetting = _escapeChars;
 	_escapeChars = escape;
 
 	// Characters are never escaped in output mode 'text'
 	if (_outputType == TEXT) _escapeChars = false; 
 
 	return(oldSetting);
     }
 
     /**
      * Starts the output document. Outputs the document header if the
      * output type is set to XML.
      */
     public void startDocument() throws TransletException {
         try {
             _saxHandler.startDocument();
 	    // Output escaping is _ALWAYS_ enabled for XML output
             if (_outputType == XML) _escapeChars = true;
         } catch (SAXException e) {
             throw new TransletException(e);
         }
     }
 
     /**
      * Ends the document output.
      */
     public void endDocument() throws TransletException {
         try {
             // Set output type to XML (the default) if still unknown.
             if (_outputType == UNKNOWN) setTypeInternal(XML);
 
 	    // Close any open start tag
 	    if (_startTagOpen) closeStartTag();
 	    if (_cdataTagOpen) closeCDATA();
 
 	    // Close output document
             _saxHandler.endDocument();
         } catch (SAXException e) {
             throw new TransletException(e);
         }
     }
 
     /**
      * Utility method - pass a string to the SAX handler's characters() method
      */
     private void characters(String str) throws SAXException {
 	final char[] ch = str.toCharArray();
 	_saxHandler.characters(ch, 0, ch.length);
     }
 
     /**
      * Utility method - pass a whole character array to the SAX handler
      */
     private void characters(char[] ch) throws SAXException {
 	_saxHandler.characters(ch, 0, ch.length);
     }
 
     /**
      * Utility method - escape special characters and pass to SAX handler
      */
     private void escapeCharacters(char[] ch, int off, int len)
 	throws SAXException {
 
 	final int limit = off + len;
 	int offset = off;
 
 	// Step through characters and escape all special characters
 	for (int i = off; i < limit; i++) {
 	    switch (ch[i]) {
 	    case '&':
 		_saxHandler.characters(ch, offset, i - offset);
 		_saxHandler.characters(AMP, 0, AMP_length);
 		offset = i + 1;
 		break;
 	    case '"':
 		_saxHandler.characters(ch, offset, i - offset);
 		_saxHandler.characters(QUOTE, 0, QUOTE_length);
 		offset = i + 1;
 		break;
 	    case '<':
 		_saxHandler.characters(ch, offset, i - offset);
 		_saxHandler.characters(LT, 0, LT_length);
 		offset = i + 1;
 		break;
 	    case '>':
 		_saxHandler.characters(ch, offset, i - offset);
 		_saxHandler.characters(GT, 0, GT_length);
 		offset = i + 1;
 		break;
 	    case '\u00a0':
 		_saxHandler.characters(ch, offset, i - offset);
 		_saxHandler.characters(NBSP, 0, NBSP_length);
 		offset = i + 1;
 		break;
 	    }
 	    // TODO - more characters need escaping!!!
 	}
 	// Output remaining characters (that do not need escaping).
 	if (offset < limit) _saxHandler.characters(ch, offset, limit - offset);
     }
 
     /**
      * Utility method - pass a whole charactes as CDATA to SAX handler
      */
     private void startCDATA(char[] ch, int off, int len) throws SAXException {
 	
 	final int limit = off + len;
 	int offset = off;
 
 	// Output start bracket - "<![CDATA["
 	characters(BEGCDATA);
 
 	// Detect any occurence of "]]>" in the character array
 	for (int i = offset; i < limit-2; i++) {
 	    if (ch[i] == ']' && ch[i+1] == ']' && ch[i+2] == '>') {
 		_saxHandler.characters(ch, offset, i - offset);
 		characters(CNTCDATA);
 		offset = i+3;
 		i=i+2; // Skip next chars ']' and '>'.
 	    }
 	}
 
 	// Output the remaining characters
 	if (offset < limit) _saxHandler.characters(ch, offset, limit - offset);
 
 	_cdataTagOpen = true;
     }
 
     private void closeCDATA() throws SAXException {
 	// Output closing bracket - "]]>"
 	characters(ENDCDATA);
 	_cdataTagOpen = false;
     }
 
     /**
      * Send characters to the output document
      */
     public void characters(char[] ch, int off, int len)	
 	throws TransletException {
         try {
 	    // Close any open start tag
 	    if (_startTagOpen) closeStartTag();
 	    
             // Set output type to XML (the default) if still unknown.
             if (_outputType == UNKNOWN) setTypeInternal(XML);
 
             // Take special precautions if within a CDATA section. If we
             // encounter the sequence ']]>' within the CDATA, we need to
             // break the section in two and leave the ']]' at the end of
             // the first CDATA and '>' at the beginning of the next. Other
 	    // special characters/sequences are _NOT_ escaped within CDATA.
 	    Integer I = (Integer)_cdataStack.peek();
 	    if (I.intValue() == _depth) {
 		if (_cdataTagOpen)
 		    _saxHandler.characters(ch, off, len);
 		else
 		    startCDATA(ch, off, len);
 	    }
 	    // Output escaped characters if required. Non-ASCII characters
             // within HTML attributes should _NOT_ be escaped.
 	    else if (_escapeChars) {
 		escapeCharacters(ch, off, len);
 	    }
 	    // Output the chracters as the are
 	    else {
 		_saxHandler.characters(ch, off, len);
 	    }
         }
 	catch (SAXException e) {
             throw new TransletException(e);
         }
     }
 
     /**
      * Start an element in the output document. This might be an XML
      * element (<elem>data</elem> type) or a CDATA section.
      */
     public void startElement(String elementName)
 	throws TransletException {
 
 	try {
 	    // Do not output element tags if output mode is 'text'
 	    if (_outputType == TEXT) return; 
 
 	    // Close any open start tag
 	    if (_startTagOpen) closeStartTag();
 	    if (_cdataTagOpen) closeCDATA();
 
 	    // If we don't know the output type yet we need to examine
 	    // the very first element to see if it is "html".
 	    if (_outputType == UNKNOWN) {
 		if (elementName.toLowerCase().equals("html"))
 		    setTypeInternal(HTML);
 		else
 		    setTypeInternal(XML);
 	    }
 
 	    // Handle document type declaration (for first element only)
 	    if (_lexHandler != null) {
 		if (((_outputType == XML) && (_doctypeSystem != null)) ||
 		    ((_doctypeSystem != null) || (_doctypePublic != null)))
 		    _lexHandler.startDTD(elementName,
 					 _doctypePublic,_doctypeSystem);
 		_lexHandler = null;
 	    }
 
 	    _depth++;
 	    _elementName = elementName;
 	    _attributes.clear();
 	    _startTagOpen = true;
 
 	    _qnameStack.push(elementName);
 
 	    if ((_cdata != null) && (_cdata.get(elementName) != null))
 		_cdataStack.push(new Integer(_depth));
 
 	    // Insert <META> tag directly after <HEAD> element in HTML doc
 	    if (_outputType == HTML)
 		if (elementName.toLowerCase().equals("head"))
 		    _headTagOpen = true;
 	}
 	catch (SAXException e) {
             throw new TransletException(e);
         }
     }
 
     /**
      * This method escapes special characters used in attribute values
      */
     private String escapeChars(String value) {
 
 	int i;
 	char[] ch = value.toCharArray();
 	int limit = ch.length;
 	int offset = 0;
 	StringBuffer buf = new StringBuffer();
 	
 	for (i = 0; i < limit; i++) {
 	    switch (ch[i]) {
 	    case '&':
 		buf.append(ch, offset, i - offset);
 		buf.append(AMP);
 		offset = i + 1;
 		break;
 	    case '"':
 		buf.append(ch, offset, i - offset);
 		buf.append(QUOTE);
 		offset = i + 1;
 		break;
 	    case '<':
 		buf.append(ch, offset, i - offset);
 		buf.append(LT);
 		offset = i + 1;
 		break;
 	    case '>':
 		buf.append(ch, offset, i - offset);
 		buf.append(GT);
 		offset = i + 1;
 		break;
 	    case '\n':
 		buf.append(ch, offset, i - offset);
 		buf.append(CRLF);
 		offset = i + 1;
 		break;
 	    }
 	}
 	if (offset < limit) {
 	    buf.append(ch, offset, limit - offset);
 	}
 	return(buf.toString());
     }
 
     /**
      * Put an attribute and its value in the start tag of an element.
      * Signal an exception if this is attempted done outside a start tag.
      */
     public void attribute(final String name, final String value)
 	throws TransletException {
 	
 	// Do not output attributes if output mode is 'text'
 	if (_outputType == TEXT) return; 
 
 	if (_startTagOpen) {
 	    // Intercept namespace declarations and handle them separately
 	    if (name.startsWith("xmlns")) {
 		if (name.length() == 5)
 		    namespace(EMPTYSTRING,value);
 		else
 		    namespace(name.substring(6),value);
 	    }
 	    else {
 		if (_outputType == HTML)
 		    _attributes.add(name,value);
 		else
 		    _attributes.add(name,escapeChars(value));
 	    }
 	}
 	else {
 	    throw new TransletException("attribute '"+name+
 					"' outside of element");
 	}
     }
 
     /**
      * End an element or CDATA section in the output document
      */
     public void endElement(String elementName) throws TransletException {
 	
 	// Do not output element tags if output mode is 'text'
 	if (_outputType == TEXT) return;
  
 	try {
 	    boolean closeElement = true;
 
 	    // Close any open element
 	    if (_startTagOpen) closeStartTag();
 	    if (_cdataTagOpen) closeCDATA();
 
 	    final String qname = (String)(_qnameStack.pop());
             if (closeElement) _saxHandler.endElement(null, null, qname);
 
             popNamespaces();
 
 	    Integer I = (Integer)_cdataStack.peek();
 	    if (I.intValue() == _depth) _cdataStack.pop();
 
             _depth--;
 
         } catch (SAXException e) {
             throw new TransletException(e);
         }
     }
 
     /**
      * Send a HTML-style comment to the output document
      */
     public void comment(String comment) throws TransletException {
 	try {
 	    // Close any open element before emitting comment
             if (_startTagOpen) closeStartTag();
 	    if (_cdataTagOpen) closeCDATA();
 
             // Set output type to XML (the default) if still unknown.
             if (_outputType == UNKNOWN) setTypeInternal(XML);
 
             // ...and then output the comment.
             characters(BEGCOMM);
             characters(comment);
             characters(ENDCOMM);
 	}
 	catch (SAXException e) {
 	    throw new TransletException(e);
 	}
     }
     
     /**
      * Send a processing instruction to the output document
      */
     public void processingInstruction(String target, String data)
 	throws TransletException {
         try {
 	    // Close any open element
 	    if (_startTagOpen) closeStartTag();
 	    if (_cdataTagOpen) closeCDATA();
 
 	    // Pass the processing instruction to the SAX handler
             _saxHandler.processingInstruction(target, data);
         } catch (SAXException e) {
             throw new TransletException(e);
         }
     }
 
     /**
      * Initialize namespace stacks
      */
     private void initNamespaces() {
 	_namespaces = new Hashtable();
 	_nodeStack = new Stack();
 	_prefixStack = new Stack();
 
 	// Define the default namespace (initially maps to "" uri)
 	Stack stack =  new Stack();
 	_namespaces.put(EMPTYSTRING, stack);
 	stack.push(EMPTYSTRING);
 	_prefixStack.push(EMPTYSTRING);
 	_nodeStack.push(new Integer(-1));
 	_depth = 0;
     }
 
     /**
      * Declare a prefix to point to a namespace URI
      */
     private void pushNamespace(String prefix, String uri) throws SAXException {
 
 	if (prefix.equals(XML_PREFIX)) return;
 
 	if ((!prefix.equals(EMPTYSTRING)) && (uri.equals(EMPTYSTRING))) return;
 
 	Stack stack;
 	// Get the stack that contains URIs for the specified prefix
 	if ((stack = (Stack)_namespaces.get(prefix)) == null) {
 	    stack = new Stack();
 	    _namespaces.put(prefix, stack);
 	}
 	// Quit now if the URI the prefix currently maps to is the same as this
 	if (!stack.empty() && uri.equals(stack.peek())) return;
 	// Put this URI on top of the stack for this prefix
 	stack.push(uri);
 
 	_prefixStack.push(prefix);
 	_nodeStack.push(new Integer(_depth));
 	_saxHandler.startPrefixMapping(prefix, uri);
     }
 
     /**
      * Undeclare the namespace that is currently pointed to by a given prefix
      */
     private void popNamespace(String prefix) throws SAXException {
 
 	if (prefix.equals(XML_PREFIX)) return;
 
 	Stack stack;
 	if ((stack = (Stack)_namespaces.get(prefix)) != null) {
 	    stack.pop();
 	    _saxHandler.endPrefixMapping(prefix);
 	}
     }
 
     /**
      * Pop all namespace definitions that were delcared by the current element
      */
     private void popNamespaces() throws TransletException {
 	try {
 	    while (true) {
 		if (_nodeStack.isEmpty()) return;
 		Integer i = (Integer)(_nodeStack.peek());
 		if (i.intValue() != _depth) return;
 		_nodeStack.pop();
 		popNamespace((String)_prefixStack.pop());
 	    }
 	}
 	catch (SAXException e) {
 	    throw new TransletException(e);
 	}
     }
 
     /**
      * Use a namespace prefix to lookup a namespace URI
      */
     private String lookupNamespace(String prefix) {
 	final Stack stack = (Stack)_namespaces.get(prefix);
 	return stack != null && !stack.isEmpty() ? (String)stack.peek() : null;
     }
 
     /**
      * Generates a namespace prefix for URIs that have no associated
      * prefix. Can happen quite frequently since we do not store
      * namespace prefixes in the tree (we only store the URIs).
      */
     private int _nsCounter = 0;
 
     private String generateNamespacePrefix() {
 	return(new String("ns"+Integer.toString(_nsCounter++)));
     }
 
     /**
      * Returns a prefix that is currently mapped to a given URI
      */
     public String getPrefix(String uri) throws TransletException {
 	try {
 	    String prefix = EMPTYSTRING;
 	    String theuri;
 
 	    // First a quick check for the default namespace
 	    if (uri.equals(lookupNamespace(prefix))) return prefix;
 
 	    Enumeration prefixes = _namespaces.keys();
 	    while (prefixes.hasMoreElements()) {
 		prefix = (String)prefixes.nextElement();
 		theuri = lookupNamespace(prefix);
		if (theuri.equals(uri)) return prefix;
 	    }
 	    prefix = generateNamespacePrefix();
 	    pushNamespace(prefix, uri);
 	    return prefix;
 	}
 	catch (SAXException e) {
 	    throw new TransletException(e);
 	}
     }
 
     /**
      * Send a namespace declaration in the output document. The namespace
      * declaration will not be include if the namespace is already in scope
      * with the same prefix.
      */
     public void namespace(final String prefix, final String uri)
 	throws TransletException {
 	try {
 	    if (_startTagOpen)
 		pushNamespace(prefix, uri);
 	    else
 		throw new TransletException("namespace declaration '"+prefix+
 					    "'='"+uri+"' outside of element");
 	}
 	catch (SAXException e) {
 	    throw new TransletException(e);
 	}
     }
 
     /** 
      * Takes a qname as a string on the format prefix:local-name and
      * returns a strig with the expanded QName on the format uri:local-name.
      */
     private String expandQName(String withPrefix) {
 	int col = withPrefix.lastIndexOf(':');
 	if (col == -1) return(withPrefix);
 
 	final String prefix = withPrefix.substring(0,col);
 	final String local =  withPrefix.substring(col+1,withPrefix.length());
 	final String uri = lookupNamespace(prefix);
 
 	if (uri == null)
 	    return(local);
 	else if (uri == EMPTYSTRING)
 	    return(local);
 	else
 	    return(uri+":"+local);
     }
 
     /************************************************************************
      * The following are all methods for configuring the output settings
      ************************************************************************/
     /**
      * Set the output type. The type must be wither TEXT, XML or HTML.
      */
     public void setType(int type)  {
 	try {
 	    _outputType = type;
 	    if (_encoding == null) _encoding = "utf-8";
 	    if (_saxHandler instanceof DefaultSAXOutputHandler)
 		((DefaultSAXOutputHandler)_saxHandler).setOutputType(type);
 	}
 	catch (SAXException e) { }
     }
 
     /**
      * Turns output indentation on/off. Should only be set to on
      * if the output type is XML or HTML.
      */
     public void setIndent(boolean indent) {
 	if (_saxHandler instanceof DefaultSAXOutputHandler) {
             ((DefaultSAXOutputHandler)_saxHandler).setIndent(indent);
 	}
     } 
 
     /**
      * Directive to turn xml header declaration  on/off. 
      */
     public void omitHeader(boolean value) {
 	if (_saxHandler instanceof DefaultSAXOutputHandler) {
             ((DefaultSAXOutputHandler)_saxHandler).omitHeader(value);
 	}
     }
 
     /**
      * Set the XML output document version - should be 1.0
      */
     public void setVersion(String version) {
 	if (_saxHandler instanceof DefaultSAXOutputHandler) {
             ((DefaultSAXOutputHandler)_saxHandler).setVersion(version);
 	}
     }
 
     /**
      * Set the XML standalone attribute - must be "yes" or "no"
      */
     public void setStandalone(String standalone) {
 	if (_saxHandler instanceof DefaultSAXOutputHandler) {
             ((DefaultSAXOutputHandler)_saxHandler).setStandalone(standalone);
 	}
     }
 
     /**
      * Set the output document system/public identifiers
      */
     public void setDoctype(String system, String pub) {
 	_doctypeSystem = system;
 	_doctypePublic = pub;
     }
 
     /**
      * Set the output media type - only relevant for HTML output
      */
     public void setMediaType(String mediaType) {
 	// This value does not have to be passed to the SAX handler. This
 	// handler creates the HTML <meta> tag in which the media-type
 	// (MIME-type) will be used.
 	_mediaType = mediaType;
     }
 
     /**
      * The <xsl:output method="xml"/> instruction can specify that certain
      * XML elements should be output as CDATA sections. This methods allows
      * the translet to insert these elements into a hashtable of strings.
      * Every output element is looked up in this hashtable before it is
      * output.
      */ 
     public void setCdataElements(Hashtable elements) {
 	_cdata = elements;
     }
 
 }
