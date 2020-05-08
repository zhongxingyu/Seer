 /******************************************************************************* 
  * Copyright (c) 2011-2012 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.ui.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.DOMConfiguration;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSOutput;
 import org.w3c.dom.ls.LSSerializer;
 import org.xml.sax.SAXException;
 
 /**
  * Utility class for processing SOAP request XML
  * @author bfitzpat
  *
  */
 public class SOAPDOMParser {
 
 	Document dom;
 	TreeParent root;
 
 	/**
 	 * Parse the file into the nodes
 	 * @param filepath
 	 */
 	public void parseXmlFile(String fileContents){
 		
 		root = new TreeParent("Invisible Root"); //$NON-NLS-1$
 		
 		//get the factory
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		
 		try {
 			
 			//Using factory get an instance of document builder
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			
 			//parse using builder to get DOM representation of the XML file
 			ByteArrayInputStream bais = new ByteArrayInputStream(fileContents.getBytes());
 			dom = db.parse(bais);
 			dom.getDocumentElement().normalize();
 			
 			parseDocument();
 
 		}catch(ParserConfigurationException pce) {
 			pce.printStackTrace();
 		}catch(SAXException se) {
 			se.printStackTrace();
 		}catch(IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 
 	/*
 	 * Work through the configuration 
 	 */
 	private void parseDocument(){
 		//get the root elememt
 		Element docEle = dom.getDocumentElement();
 		
 		TreeParent soapRoot = new TreeParent(docEle.getTagName());
 		soapRoot.setData(docEle);
 		
 		processChildren(soapRoot, docEle);
 		root.addChild(soapRoot);
 	}
 	
 	/*
 	 * Work down the children tree
 	 * @param parent
 	 * @param el
 	 */
 	private void processChildren ( TreeParent parent, Element el ) {
 		el.normalize();
 		parent.setData(el);
 		
 		NodeList children = el.getChildNodes();
 		for (int i = 0; i < children.getLength(); i++) {
 			if (children.item(i) instanceof Element) {
 				Element child = (Element) children.item(i);
 				String name = child.getTagName();
 				TreeParent childNode = new TreeParent(name);
 				processChildren(childNode, child);
 				parent.addChild(childNode);
 			}
 		}
 	}
 	
 	/**
 	 * Update the value in the XML
 	 * @param input
 	 * @param tp
 	 * @param value
 	 * @return
 	 */
 	public String updateValue ( String input, TreeParent tp, String value) {
 		parseXmlFile(input);
 		dom.normalizeDocument();
 		Element docEle = dom.getDocumentElement();
 		Element toFind = (Element) tp.getData();
 		NodeList nl = docEle.getElementsByTagName(toFind.getTagName());
 		if (nl.getLength() > 0) {
 			Element found = (Element) nl.item(0);
 			if (found.getChildNodes() != null && found.getChildNodes().getLength() > 0) {
 				Node node = found.getChildNodes().item(0);
 				node.setTextContent(value);
 
 				TransformerFactory transFactory = TransformerFactory.newInstance();
 				Transformer transformer;
 				try {
 					transformer = transFactory.newTransformer();
 					StringWriter buffer = new StringWriter();
 					transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$
 					transformer.transform(new DOMSource(docEle),
 					      new StreamResult(buffer));
 					String str = buffer.toString();				
 					return str;
 				} catch (TransformerConfigurationException e) {
 					e.printStackTrace();
 				} catch (TransformerException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Return the actual root
 	 * @return
 	 */
 	public TreeParent getRoot() {
 		return this.root;
 	}
 	
 	/**
 	 * Pretty print
 	 * @param xml
 	 * @return
 	 */
 	public static String prettyPrint ( String xml ) {
 		//get the factory
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		Document dom;
 		
 		try {
 			
 			//Using factory get an instance of document builder
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			
 			//parse using builder to get DOM representation of the XML file
 			ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
 			dom = db.parse(bais);
 			
 			String stringOutput = prettyPrintWithDOM3LS(dom);
 			return stringOutput;
 			
 		}catch(ParserConfigurationException pce) {
 			pce.printStackTrace();
 		}catch(SAXException se) {
 			se.printStackTrace();
 		}catch(IOException ioe) {
 			ioe.printStackTrace();
 		}
 		return null;
 	}
 	
 	/*
 	 * @param document
 	 * @return
 	 */
 	static String prettyPrintWithDOM3LS(Document document) {
 		// Pretty-prints a DOM document to XML using DOM Load and Save's LSSerializer.
 		// Note that the "format-pretty-print" DOM configuration parameter can only be set in JDK 1.6+.
 		DOMImplementation domImplementation = document.getImplementation();
 		if (domImplementation.hasFeature("LS", "3.0") && domImplementation.hasFeature("Core", "2.0")) {  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 			DOMImplementationLS domImplementationLS = (DOMImplementationLS) domImplementation.getFeature("LS", "3.0"); //$NON-NLS-1$ //$NON-NLS-2$
 			LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
 			DOMConfiguration domConfiguration = lsSerializer.getDomConfig();
 			if (domConfiguration.canSetParameter("format-pretty-print", Boolean.TRUE)) { //$NON-NLS-1$
 				lsSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE); //$NON-NLS-1$
 				LSOutput lsOutput = domImplementationLS.createLSOutput();
 				lsOutput.setEncoding("UTF-8"); //$NON-NLS-1$
 				StringWriter stringWriter = new StringWriter();
 				lsOutput.setCharacterStream(stringWriter);
 				lsSerializer.write(document, lsOutput);
 				return stringWriter.toString();
 			} else {
 				throw new RuntimeException("DOMConfiguration 'format-pretty-print' parameter isn't settable."); //$NON-NLS-1$
 			}
 		} else {
 			throw new RuntimeException("DOM 3.0 LS and/or DOM 2.0 Core not supported."); //$NON-NLS-1$
 		}
 	}	
 
 	/**
 	 * from http://jaysonlorenzen.wordpress.com/2009/01/29/48/
 	 * @param inXMLStr
 	 * @return
 	 */
 	public static boolean isXMLLike(String inXMLStr) {
 
         boolean retBool = false;
         Pattern pattern;
         Matcher matcher;
 
         // REGULAR EXPRESSION TO SEE IF IT AT LEAST STARTS AND ENDS
         // WITH THE SAME ELEMENT
         final String XML_PATTERN_STR = "<(\\S+?)(.*?)>(.*?)</\\1>"; //$NON-NLS-1$
 
         // IF WE HAVE A STRING
         if (inXMLStr != null && inXMLStr.trim().length() > 0) {
 
             // IF WE EVEN RESEMBLE XML
             if (inXMLStr.trim().startsWith("<")) { //$NON-NLS-1$
 
                 pattern = Pattern.compile(XML_PATTERN_STR,
                 Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);
 
                 // RETURN TRUE IF IT HAS PASSED BOTH TESTS
                 matcher = pattern.matcher(inXMLStr);
                 retBool = matcher.matches();
             }
         // ELSE WE ARE FALSE
         }
 
         return retBool;
     }
 
 	/**
 	 * Simple JSON pretty print to format JSON output
 	 * @param inJSON
 	 * @return
 	 */
 	public static String prettyPrintJSON ( String inJSON ) {
 		int numberOfSpaces = 4;
 		String spaces = String.format("%" + numberOfSpaces + "s", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		inJSON = inJSON.trim();
 		if (inJSON.startsWith("{") && inJSON.endsWith("}")) { //$NON-NLS-1$ //$NON-NLS-2$
 			String output = ""; //$NON-NLS-1$
 			char[] chars = inJSON.toCharArray();
 			for (int i = 0; i < chars.length; i++) {
 				char current = chars[i];
 				switch (current) {
 				case '{':
 					output = output + current + "\r\n" + spaces; //$NON-NLS-1$
 					break;
 				case '}':
 					output = output + "\r\n" + current; //$NON-NLS-1$
 					break;
 				case ',':
 					output = output + current + "\r\n" + spaces; //$NON-NLS-1$
 					break;
 				default:
 					output = output + current;
 					break;
 				}
 			}
 			return output;
 		}
 		return inJSON;
	}}
