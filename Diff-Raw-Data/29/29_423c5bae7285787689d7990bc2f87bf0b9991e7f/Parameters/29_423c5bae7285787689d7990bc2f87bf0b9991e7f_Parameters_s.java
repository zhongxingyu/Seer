 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.xml;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URI;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Result;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import net.sf.okapi.common.DefaultEntityResolver;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.InlineCodeFinder;
 
 public class Parameters implements IParameters {
 	
 	private final static String DEFAULTS = "<?xml version='1.0' encoding='UTF-8'?>\n"
 		+ "<its:rules version='1.0'\n"
 		+ " xmlns:its='http://www.w3.org/2005/11/its'\n"
 		+ " xmlns:xlink='http://www.w3.org/1999/xlink'\n"
 		+ " xmlns:itsx='http://www.w3.org/2008/12/its-extensions'\n"
 		+ ">"
 		+ "<!-- See ITS specification at: http://www.w3.org/TR/its/ -->\n"
 		+ "</its:rules>\n";
 	
 	private String path;
 	private URI docURI;
 	private Document doc;
 	private DocumentBuilder docBuilder;
 
 	public boolean useCodeFinder;
 	public InlineCodeFinder codeFinder;
 	public boolean escapeGt;
 	public boolean escapeNbsp;
 	
 	public Parameters () {
 		codeFinder = new InlineCodeFinder();
 		// Create the document builder factory
 		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
 		fact.setNamespaceAware(true);
 		fact.setValidating(false);
 		// Create the document builder
 		try {
 			docBuilder = fact.newDocumentBuilder();
 		}
 		catch ( ParserConfigurationException e ) {
 			throw new OkapiIOException(e);
 		}
 		
 		docBuilder.setEntityResolver(new DefaultEntityResolver());
 	}
 
 	@Override
 	public String toString () {
 		if ( doc == null ) {
 			return DEFAULTS;
 		}
 		StringWriter sw = new StringWriter();
 		Result result = new StreamResult(sw);
 		// Write the DOM document to the file
 		Transformer trans;
 		try {
 			trans = TransformerFactory.newInstance().newTransformer();
 			trans.transform(new DOMSource(doc), result);
 		}
 		catch ( TransformerConfigurationException e ) {
 			throw new OkapiIOException(e);
         }
 		catch ( TransformerException e ) {
 			throw new OkapiIOException(e);
 		}
 		return sw.toString();
 	}
 	
 	public void fromString (String data) {
 		docURI = null;
 		path = null;
 		try {
 			doc = docBuilder.parse(new InputSource(new StringReader(data)));
 		}
 		catch ( SAXException e ) {
 			throw new OkapiIOException(e);
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	public String getPath () {
 		return path;
 	}
 	
 	public void setPath (String filePath) {
 		path = filePath;
 	}
 
 	public void load (URI inputURI,
 		boolean ignoreErrors)
 	{
		File f = new File(inputURI);
		try {
			doc = docBuilder.parse(f);
 		}
 		catch ( SAXException e ) {
 			throw new OkapiIOException(e);
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException(e);
 		} 
 		path = inputURI.getPath();
		docURI = f.toURI();
 	}
 
 	public void reset () {
 		doc = null;
 		docURI = null;
 
 		useCodeFinder = false;
 		codeFinder.reset();
 		escapeGt = true;
 		escapeNbsp = true;
 	}
 
 	public void save (String filePath) {
 		Result result = null;
 		Transformer trans = null;
 		try {
 			if ( doc == null ) {
 				fromString(DEFAULTS);
 			}
 			// Prepare the output file
 			File f = new File(filePath);
 			result = new StreamResult(f);
 			// Write the DOM document to the file
 			try {
 				trans = TransformerFactory.newInstance().newTransformer();
 				trans.transform(new DOMSource(doc), result);
 			}
 			catch ( TransformerConfigurationException e ) {
 				throw new OkapiIOException(e);
 	        }
 			catch ( TransformerException e ) {
 				throw new OkapiIOException(e);
 			}
 			// Update path and URI
 			path = filePath;
 			docURI = f.toURI();
 		}
 		finally {
 			// Try to make sure the file is not locked
 			trans = null;
 			result = null;
 			System.gc();
 		}
 	}
 
 	public Document getDocument () {
 		return doc;
 	}
 	
 	public URI getURI () {
 		return docURI;
 	}
 
 	public boolean getBoolean (String name) {
 		//TODO: Use DOM to store these
 		if ( name.equals("escapeGt") ) return escapeGt;
 		if ( name.equals("escapeNbsp") ) return escapeNbsp;
 		return false;
 	}
 
 	public String getString (String name) {
 		return null;
 	}
 
 	public int getInteger (String name) {
 		return 0;
 	}
 
 }
