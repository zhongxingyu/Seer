 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.annoparser.parser.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface;
 import org.ebayopensource.turmeric.tools.annoparser.XSDDocument;
 import org.ebayopensource.turmeric.tools.annoparser.context.Context;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.Attribute;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.AttributeElement;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.Comment;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ComplexType;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.Element;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.EnumElement;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ParsedAnnotationInfo;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ParsedAnnotationTag;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.SimpleType;
 import org.ebayopensource.turmeric.tools.annoparser.exception.ParserException;
 import org.ebayopensource.turmeric.tools.annoparser.exception.XsdDocException;
 import org.ebayopensource.turmeric.tools.annoparser.parser.AnnotationParser;
 import org.ebayopensource.turmeric.tools.annoparser.parser.XSDParser;
 import org.ebayopensource.turmeric.tools.annoparser.utils.Utils;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * Implementation of the XSDParser interface Provides a light weight
  * representation of the xsd document either provided as input or part of the
  * wsdl document
  * 
  * @author srengarajan
  */
 public class XSDParserImpl implements XSDParser {
 
 	private final static String CLASS_NAME = XSDParserImpl.class.getClass()
 			.getName();
 	Logger logger = Logger.getLogger(CLASS_NAME);
 
 	/**
 	 * handle to the dom document created out of xsd
 	 */
 	private Document doc = null;
 
 	/**
 	 * List of complex types across all name spaces from the dom
 	 */
 	private NodeList ctypes = null;
 
 	/**
 	 * association of types to elements
 	 */
 	private Map typeElementsMap = new HashMap<String, List<Element>>();
 
 	/**
 	 * Inverse association from elem -> complex type
 	 */
 	private Map elemCTypeMap = new HashMap<Element, List<ComplexType>>();
 
 	/**
 	 * association of ComplexType -> Operations
 	 */
 
 	/**
 	 * Gets the elem c type map.
 	 * 
 	 * @return the elemCTypeMap
 	 */
 	public Map getElemCTypeMap() {
 		return elemCTypeMap;
 	}
 
 	/**
 	 * Sets the elem c type map.
 	 * 
 	 * @param elemCTypeMap
 	 *            the elemCTypeMap to set
 	 */
 	public void setElemCTypeMap(Map elemCTypeMap) {
 		this.elemCTypeMap = elemCTypeMap;
 	}
 
 	/**
 	 * Universal set of all node names
 	 */
 	private Set nameNodeSet = new HashSet();
 
 	/**
 	 * Gets the ctypes.
 	 * 
 	 * @return all complex types
 	 */
 	public NodeList getCtypes() {
 		return ctypes;
 	}
 
 	/**
 	 * Sets the ctypes.
 	 * 
 	 * @param ctypes
 	 *            setter for complex types
 	 */
 	public void setCtypes(NodeList ctypes) {
 		this.ctypes = ctypes;
 	}
 
 	/**
 	 * Gets the doc.
 	 * 
 	 * @return dom handle
 	 */
 	public Document getDoc() {
 		return doc;
 	}
 
 	/**
 	 * Sets the doc.
 	 * 
 	 * @param doc
 	 *            setter for dom handle
 	 */
 	public void setDoc(Document doc) {
 		this.doc = doc;
 	}
 
 	/**
 	 * Instantiates a new xSD parser impl.
 	 * 
 	 */
 	public XSDParserImpl(){
 	}
 
 	/**
 	 * Initialize.
 	 * 
 	 * @param file
 	 *            the file
 	 * @return the document
 	 * @throws SAXException
 	 *             the sAX exception
 	 * @throws IOException
 	 *             Signals that an I/O exception has occurred.
 	 */
 	private Document initialize(URL file) throws SAXException, IOException {
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		dbf.setNamespaceAware(true);
 		try {
 			DocumentBuilder builder = dbf.newDocumentBuilder();
 
 			doc = builder.parse(file.openStream());
 		} catch (ParserConfigurationException e) {
 			logger.log(Level.SEVERE, "A ParserConfigurationException occurred",
 					e);
 		}
 		return doc;
 	}
 
 	/**
 	 * Visit.
 	 * 
 	 * @param node
 	 *            traverse and collect node names
 	 */
 	public void visit(Node node) {
 		logger.log(Level.INFO, "Node Info", node);
 		this.nameNodeSet.add(node.getNodeName());
 		NodeList children = node.getChildNodes();
 		if (children != null) {
 			int noOfNodes = children.getLength();
 			for (int i = 0; i < noOfNodes; i++) {
 				Node tmpNode = children.item(i);
 				this.nameNodeSet.add(tmpNode.getNodeName());// , tmpNode);
 				visit(tmpNode);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.ebayopensource.turmeric.tools.annoparser.parser.XSDParser#parse(java
 	 * .lang.String)
 	 */
 	public synchronized XSDDocInterface parse(String url)
 			throws ParserException {
 		logger.log(Level.FINER, "Entering parse method in XSDParser", url);
 		XSDDocument xsdDocument = Context.getContext().getNewXsdDocument();
 		try {
 			URL file = new URL(url);
 			Document doc = initialize(file);
 			xsdDocument.setDocumentURL(file);
			mergeImportedXsd(doc, xsdDocument,"redefine");
 			if (doc != null) {
 
 				this.visit(doc);
 				this.parseAllNSElements(doc, xsdDocument);
 				this.parseAllNSComplexTypes(doc, xsdDocument);
 				this.linkChildTypes(xsdDocument);
 				this.parseAllNSSimpleTypes(doc, xsdDocument);
 				this.buildElementToTypeMap(xsdDocument);
 				mergeImportedXsd(doc, xsdDocument,"import");
 				handleAnonymous(doc, xsdDocument);
 			}
 		} catch (SAXException e) {
 			logger.log(Level.SEVERE, "A SAXException occurred", e);
 			throw new ParserException("Failed to parse : " + url
 					+ "A SAXException occurred. Cause for the Exception: "
 					+ e.getMessage(), e);
 		} catch (IOException e) {
 			logger.log(Level.SEVERE, "An IOException occurred", e);
 			throw new ParserException("Failed to parse : " + url
 					+ "An IOException occurred. Cause for the Exception: "
 					+ e.getMessage(), e);
 		}
 		logger.log(Level.FINER, "Exiting parse method in XSDParser",
 				xsdDocument);
 		postProcessXsdDocument(xsdDocument, doc);
 		return xsdDocument;
 	}
 	private void handleAnonymous(Document document, XSDDocument xsdDocument){
 		List<Element> elements=new ArrayList<Element>(xsdDocument.getAllIndependentElements());
 		int anonymousIndex=0;
 		for(ComplexType cType:xsdDocument.getAllComplexTypes()){
 			if(Utils.isEmpty(cType.getName())){
 				cType.setName("Type "+anonymousIndex++);
 			}
 			elements.addAll(cType.getChildElements());
 			if(cType.getSimpleAttributeContent()!=null){
 				for(AttributeElement attr: cType.getSimpleAttributeContent()){
 					if(Utils.isEmpty(attr.getName())){
 						attr.setName("Attribute "+anonymousIndex++);
 					}
 					if(Utils.isEmpty(attr.getType())){
 						attr.setType("string");
 						if(Utils.isEmpty(attr.getBaseType())){
 							attr.setBaseType("string");
 						}
 					}else if(Utils.isEmpty(attr.getBaseType())){
 						attr.setBaseType(attr.getType());
 					}
 					
 					
 				}
 			}
 		}
 		
 		for(SimpleType cType:xsdDocument.getAllSimpleTypes()){
 			if(Utils.isEmpty(cType.getName())){
 				String typeName="Type "+anonymousIndex++;
 				cType.setName(typeName);
 				if(cType.getEnums()!=null){
 					for(EnumElement enumE:cType.getEnums()){
 						enumE.setType(typeName);
 					}
 				}
 			}
 			
 		}
 		
 		for(Element element:elements){
 			if(Utils.isEmpty(element.getName())){
 				String refType=null;
 				for(Attribute attr:element.getAttributes()){
 					if(attr.getName().equals("ref")){
 						refType=attr.getValue();
 						refType=Utils.removeNameSpace(refType);
 						break;
 					}
 				}
 				if(refType!=null){
 					Element refElem=xsdDocument.searchIndependentElement(refType);
 					if(refElem!=null){
 						element.setName(refElem.getName());
 						element.setType(refElem.getType());
 					}
 				}
 				if(Utils.isEmpty(element.getName())){
 					element.setName("Element "+anonymousIndex++);
 				}
 				if(Utils.isEmpty(element.getType())){
 					element.setName("Anonymous Type");
 				}
 			}
 			
 		}
 	}
 	/**
 	 * Convert to url. The path supplied is first tried as a URL external form
 	 * string, if it fails it is tried as a Class path resource, Then it is
 	 * tried as a local file path.
 	 * 
 	 * @param path
 	 *            the path
 	 * @return the uRL
 	 */
 	private static URL convertToURL(String path, URL relativeURL) {
 		URL url = null;
 		try {
 			URI uri = relativeURL.toURI();
 			if (uri != null) {
 				url = relativeURL.toURI().resolve(path).toURL();
 			} else {
 				url = convertToURLAsLocal(path, relativeURL);
 			}
 		} catch (URISyntaxException e) {
 			url = convertToURLAsLocal(path, relativeURL);
 		} catch (MalformedURLException e) {
 			url = convertToURLAsLocal(path, relativeURL);
 		}
 		try {
 			url.openStream();
 		} catch (IOException e) {
 			return null;
 		}
 		return url;
 	}
 
 	private static URL convertToURLAsLocal(String path, URL url) {
 		File file = new File(path);
 		if (file.exists()) {
 			try {
 				url = file.toURI().toURL();
 			} catch (MalformedURLException e1) {
 				Logger.getLogger(XSDParserImpl.class.getName()).log(
 						Level.SEVERE, path + " is Not valid", e1);
 
 			}
 		} else {
 			File f = new File(url.toExternalForm());
 			path = f.getParent() + File.separator + "." + File.separator + path;
 			file = new File(path);
 			if (file.exists()) {
 				try {
 					url = file.toURI().toURL();
 				} catch (MalformedURLException e1) {
 					Logger.getLogger(XSDParserImpl.class.getName()).log(
 							Level.SEVERE, path + " is Not valid", e1);
 
 				}
 			}
 		}
 		return url;
 	}
 
 
 	private void mergeImportedXsd(Document document, XSDDocument xsdDocument,
 			String tagName) throws ParserException, SAXException, IOException {
 		if (document != null) {
 			NodeList nodes = document.getElementsByTagNameNS("*", tagName);
 			if (nodes != null) {
 				for (int i = 0; i < nodes.getLength(); i++) {
 					org.w3c.dom.Element element = (org.w3c.dom.Element) nodes
 							.item(i);
 					if (element.hasAttribute("schemaLocation")) {
 						URL url = convertToURL(element
 								.getAttribute("schemaLocation"), xsdDocument
 								.getDocumentURL());
 						if (url != null) {
 							String schemaLocation = url.toExternalForm();
 							if (!xsdDocument.getXsdsProcessed().contains(
 									schemaLocation)) {
 								XSDParserImpl parser = Context.getContext().getNewXsdParser();
 								XSDDocument parsedDocument = (XSDDocument) parser
 										.parse(schemaLocation);
 								for (Element elem : parsedDocument
 										.getAllIndependentElements()) {
 									xsdDocument.addIndependentElement(elem);
 								}
 								for (ComplexType elem : parsedDocument
 										.getAllComplexTypes()) {
									xsdDocument.addComplexType(elem);
 								}
 								for (SimpleType elem : parsedDocument
 										.getAllSimpleTypes()) {
 									xsdDocument.addSimpleType(elem);
 								}
 								xsdDocument.getAllEnums().addAll(
 										parsedDocument.getAllEnums());
 								xsdDocument.getElementComplexTypeMap().putAll(
 										parsedDocument
 												.getElementComplexTypeMap());
 								xsdDocument.getParentToComplexTypeMap().putAll(
 										parsedDocument
 												.getParentToComplexTypeMap());
 								xsdDocument.getXsdsProcessed().addAll(
 										parsedDocument.getXsdsProcessed());
 								xsdDocument.getXsdsProcessed().add(
 										schemaLocation);
 							}
 						} else {
 							Logger
 									.getLogger(XSDParserImpl.class.getName())
 									.log(
 											Level.SEVERE,
 											element
 													.getAttribute("schemaLocation")
 													+ " is Not valid");
 						}
 					}
 				}
 			}
 
 		}
 	}
 	
 
 	/**
 	 * Builds the element to type map.
 	 * 
 	 * @param xsdDocument
 	 *            builds association element -> complex type
 	 */
 	private void buildElementToTypeMap(XSDDocument xsdDocument) {
 		logger.log(Level.FINER,
 				"Entering buildElementToTypeMap method in XSDParser",
 				xsdDocument);
 		List<ComplexType> ctypes = (List<ComplexType>) xsdDocument
 				.getAllComplexTypes();
 		for (ComplexType ctype : ctypes) {
 			Set<Element> children = ctype.getChildElements();
 			for (Element elem : children) {
 				String name = elem.getType();
 				if (elem.getType().indexOf(':') != -1) {
 					name = elem.getType().substring(
 							elem.getType().indexOf(':') + 1);
 				}
 				Object ctypeList = this.elemCTypeMap.get(name);
 				if (ctypeList == null) {
 					ctypeList = new ArrayList<ComplexType>();
 					((List<ComplexType>) ctypeList).add(ctype);
 					this.elemCTypeMap.put(name, ctypeList);
 				} else {
 					((List<ComplexType>) ctypeList).add(ctype);
 				}
 			}
 		}
 		xsdDocument.setElementToComplexTypeMap(elemCTypeMap);
 		logger.log(Level.FINER,
 				"Exiting buildElementToTypeMap method in XSDParser",
 				xsdDocument);
 	}
 
 	/**
 	 * Search node names for all name spaces.
 	 * 
 	 * @param endsWithPattern
 	 *            the ends with pattern
 	 * @return look up method to return collection of nodes from all namespaces
 	 */
 	private List<String> searchNodeNamesForAllNameSpaces(String endsWithPattern) {
 		logger.log(Level.FINER,
 				"Entering searchNodeNamesForAllNameSpaces method in XSDParser",
 				endsWithPattern);
 		List<String> nodeNames = new ArrayList<String>();
 		for (Object nodeName : this.nameNodeSet) {
 			String nodeNameString = (String) nodeName;
 			String prefixedString = ":" + endsWithPattern;
 			if (nodeNameString.equalsIgnoreCase(endsWithPattern)
 					|| nodeNameString.endsWith(prefixedString)) {
 				nodeNames.add(nodeNameString);
 			}
 		}
 		logger.log(Level.FINER,
 				"Exiting searchNodeNamesForAllNameSpaces method in XSDParser",
 				nodeNames);
 		return nodeNames;
 	}
 
 	/**
 	 * Parses the all ns elements.
 	 * 
 	 * @param srcDoc
 	 *            the src doc
 	 * @param xsdDocument
 	 *            the xsd document
 	 * @throws ParserException 
 	 */
 	private void parseAllNSElements(Document srcDoc, XSDDocument xsdDocument) throws ParserException {
 		logger.log(Level.FINER,
 				"Entering parseAllNSElements method in XSDParser",
 				new Object[] { srcDoc, xsdDocument });
 		List<String> allNSElements = this
 				.searchNodeNamesForAllNameSpaces("element");
 		for (String tagName : allNSElements) {
 			parseAllElements(tagName, srcDoc, xsdDocument);
 		}
 		logger.log(Level.FINER,
 				"Exiting parseAllNSElements method in XSDParser", new Object[] {
 						srcDoc, xsdDocument });
 	}
 
 	/**
 	 * Parses the all ns complex types.
 	 * 
 	 * @param srcDoc
 	 *            the src doc
 	 * @param xsdDocument
 	 *            the xsd document
 	 * @throws XsdDocException 
 	 */
 	private void parseAllNSComplexTypes(Document srcDoc, XSDDocument xsdDocument) throws ParserException {
 		parseComplexTypes(srcDoc, xsdDocument);
 		logger.log(Level.FINER,
 				"Exiting parseAllNSComplexTypes method in XSDParser",
 				new Object[] { srcDoc, xsdDocument });
 	}
 
 	/**
 	 * Parses the all ns simple types.
 	 * 
 	 * @param srcDoc
 	 *            the src doc
 	 * @param xsdDocument
 	 *            the xsd document
 	 * @throws XsdDocException 
 	 */
 	private void parseAllNSSimpleTypes(Document srcDoc, XSDDocument xsdDocument) throws ParserException {
 		logger.log(Level.FINER,
 				"Entering parseAllNSSimpleTypes method in XSDParser",
 				new Object[] { srcDoc, xsdDocument });
 		parseSimpleTypes( srcDoc, xsdDocument);
 		
 		logger.log(Level.FINER,
 				"Exiting parseAllNSComplexTypes method in XSDParser",
 				new Object[] { srcDoc, xsdDocument });
 	}
 
 	/**
 	 * Parses the all elements.
 	 * 
 	 * @param tagName
 	 *            the tag name
 	 * @param srcDoc
 	 *            the src doc
 	 * @param xsdDocument
 	 *            parses "element" nodes from the document and constructs its
 	 *            POJO representation. sticks the annotation associated with the
 	 *            element.
 	 * @throws ParserException 
 	 */
 	private void parseAllElements(String tagName, Document srcDoc,
 			XSDDocument xsdDocument) throws ParserException {
 		
 		logger.log(Level.FINER,
 				"Entering parseAllElements method in XSDParser", new Object[] {
 						tagName, srcDoc, xsdDocument });
 		List<Element> elements = new ArrayList<Element>();
 		NodeList domElements = srcDoc.getElementsByTagName(tagName);
 		int noOfDomElements = domElements.getLength();
 		for (int i = 0; i < noOfDomElements; i++) {
 			org.w3c.dom.Element obj = (org.w3c.dom.Element) domElements.item(i);
 			String name = obj.getAttribute("name");
 			Element elem = Context.getContext().getNewElement();
 			
 			elem.setName(name);
 			String type = obj.getAttribute("type");
 			if(!Utils.isEmpty(type)){
 				String[] typeParts = type.split(":");
 				if (typeParts != null && typeParts.length > 1){
 					type = typeParts[1];
 				}
 			}else{
 				NodeList nodes=obj.getElementsByTagNameNS("*", "complexType");
 				if(nodes!=null && nodes.getLength()>0){
 					org.w3c.dom.Element node=(org.w3c.dom.Element)nodes.item(0);
 					ComplexType cType=processComplexType(xsdDocument, node);
 					if(Utils.isEmpty(cType.getName())){
 						cType.setName(name+"Type");
 						List<Element> instances = (List<Element>) typeElementsMap.get(name);
 						if (instances != null) {
 							cType.setInstanceElements(instances);
 						}else{
 							instances = new ArrayList<Element>();
 							instances.add(elem);
 							cType.setInstanceElements(instances);
 						}
 					}
 					type=cType.getName();
 					xsdDocument.addComplexType(cType);
 				}
 			}
 			
 			elem.setType(type);
 			String prevComment=Utils.getPreviousComment(obj);
 			String nextComment=Utils.getNextComment(obj);
 			if(prevComment!=null || nextComment!=null){
 				Comment comment=new Comment();
 				comment.setNextComment(nextComment);
 				comment.setPreviousComment(prevComment);
 				elem.setComment(comment);
 			}
 			List<Element> instances = (List<Element>) typeElementsMap.get(type);
 			if (instances == null) {
 				instances = new ArrayList<Element>();
 				instances.add(elem);
 				typeElementsMap.put(type, instances);
 			} else {
 				instances.add(elem);
 				typeElementsMap.put(type, instances);
 			}
 
 			NamedNodeMap nameNodeMap = obj.getAttributes();
 			int size = nameNodeMap.getLength();
 
 			for (int j = 0; j < size; j++) {
 				Attr attr = (Attr) nameNodeMap.item(j);
 				if (attr != null) {
 					Attribute attribute = new Attribute();
 					attribute.setName(attr.getName());
 					attribute.setValue(attr.getValue());
 					elem.getAttributes().add(attribute);
 				}
 			}
 			elem.setAnnotationInfo(parseAnnotation( obj));
 			postProcessElement(elem, obj);
 			elements.add(elem);
 			xsdDocument.addIndependentElement(elem);
 			logger
 					.log(Level.FINER,
 							"Exiting parseAllElements method in XSDParser",
 							xsdDocument);
 		}
 	}
 
 	/**
 	 * Parses the child elements.
 	 * 
 	 * @param nsprefix
 	 *            namespace
 	 * @param ctype
 	 *            complex type
 	 * @param ctypes
 	 *            collection of all complex types
 	 * @param element
 	 *            parent complex type
 	 * @param xsdDocument
 	 *            handle to xsd document
 	 * @return list of all children of the given element node and also
 	 *         associates with the complex type to enable traversal.
 	 * @throws XsdDocException 
 	 */
 	private Set<Element> parseChildElements(ComplexType ctype,
 			 org.w3c.dom.Element element,
 			XSDDocument xsdDocument) throws ParserException {
 		logger.log(Level.FINER,
 				"Entering parseChildElements method in XSDParser",
 				new Object[] {  ctype, ctypes, element, xsdDocument });
 		Set<Element> elements = new TreeSet<Element>();
 		// System.out.println("element name is " +
 		// element.getAttribute("name"));
 		NodeList domElements = element.getElementsByTagNameNS( "*",
 				 "element");
 		int noOfDomElements = domElements.getLength();
 		for (int i = 0; i < noOfDomElements; i++) {
 			org.w3c.dom.Element obj = (org.w3c.dom.Element) domElements.item(i);
 			String name = obj.getAttribute("name");
 			String type = obj.getAttribute("type");
 			String[] typeParts = type.split(":");
 			if (typeParts != null && typeParts.length > 1)
 				type = typeParts[1];
 			Element elem = Context.getContext().getNewElement();
 			elem.setName(name);
 			elem.setType(type);
 			elem.setContainerComplexType(ctype);
 			String prevComment=Utils.getPreviousComment(obj);
 			String nextComment=Utils.getNextComment(obj);
 			if(prevComment!=null || nextComment!=null){
 				Comment comment=new Comment();
 				comment.setNextComment(nextComment);
 				comment.setPreviousComment(prevComment);
 				elem.setComment(comment);
 			}
 			NamedNodeMap nameNodeMap = obj.getAttributes();
 			int size = nameNodeMap.getLength();
 
 			for (int j = 0; j < size; j++) {
 				Attr attr = (Attr) nameNodeMap.item(j);
 				if (attr != null) {
 					Attribute attribute = new Attribute();
 					attribute.setName(attr.getName());
 					attribute.setValue(attr.getValue());
 					elem.getAttributes().add(attribute);
 				}
 			}
 			elem.setAnnotationInfo(parseAnnotation( obj));
 			elements.add(elem);
 			postProcessElement(elem, obj);
 			xsdDocument.addIndependentElement(elem);
 		}
 		NodeList attributeNodes = element.getElementsByTagNameNS( "*", "attribute");
 		if (attributeNodes != null) {
 			int count = attributeNodes.getLength();
 			for (int j = 0; j < count; j++) {
 				org.w3c.dom.Element attrElem = (org.w3c.dom.Element) attributeNodes
 						.item(j);
 
 				AttributeElement attr =Context.getContext().getNewAttribute();
 				String name = attrElem.getAttribute("name");
 				if(Utils.isEmpty(name)){
 					for (int k = 0; k < count; k++) {
 						org.w3c.dom.Element ref = (org.w3c.dom.Element) attributeNodes
 						.item(k);
 						name = ref.getAttribute("name");
 					}
 				}
 				String type = attrElem.getAttribute("type");
 				attr.setContainerComplexType(ctype);
 				String prevComment=Utils.getPreviousComment(attrElem);
 				String nextComment=Utils.getNextComment(attrElem);
 				if(prevComment!=null || nextComment!=null){
 					Comment comment=new Comment();
 					comment.setNextComment(nextComment);
 					comment.setPreviousComment(prevComment);
 					attr.setComment(comment);
 				}
 				if(Utils.isEmpty(type)){
 					NodeList simpleTypes=attrElem.getElementsByTagNameNS("*", "simpleType");
 					if(simpleTypes.getLength()>0){
 						org.w3c.dom.Element elem=(org.w3c.dom.Element) simpleTypes.item(0);
 						
 						SimpleType simpleType=populateSimpleType(xsdDocument,elem,true);
 						if(Utils.isEmpty(simpleType.getName())){
 							simpleType.setName(ctype.getName()+ name+"Type");
 							if(simpleType.getEnums()!=null){
 								for(EnumElement enumE:simpleType.getEnums()){
 									enumE.setType(simpleType.getName());
 								}
 							}
 							
 						}
 						xsdDocument.addSimpleType(simpleType);
 						attr.setType(simpleType.getName());
 						attr.setBaseType(simpleType.getName());
 					}
 				}else{
 					attr.setType(type);
 				}
 				attr.setName(name);
 				
 				org.w3c.dom.Element parent = (org.w3c.dom.Element) attrElem
 						.getParentNode();
 				if (parent.getTagName().endsWith(":extension")) {
 					String baseType = parent.getAttribute("base");
 					attr.setBaseType(baseType);
 				}
 				NamedNodeMap nameNodeMap = attrElem.getAttributes();
 				int size = nameNodeMap.getLength();
 				for (int k = 0; k < size; k++) {
 					Attr attri = (Attr) nameNodeMap.item(k);
 					if (attri != null) {
 						Attribute attribute = new Attribute();
 						attribute.setName(attri.getName());
 						attribute.setValue(attri.getValue());
 						attr.getAttributes().add(attribute);
 					}
 				}
 				attr.setAnnotationInfo(parseAnnotation(attrElem));
 				postProcessAttribute(attr, attrElem);
 				ctype.addSimpleAttributeContent(attr);
 			}
 		}
 		NodeList extensibleElements = element.getElementsByTagNameNS("*", "extension");
 		org.w3c.dom.Element extensionbaseElem = (org.w3c.dom.Element) extensibleElements
 				.item(0);
 		if (extensionbaseElem != null) {
 			String attribute = extensionbaseElem.getAttribute("base");
 			String parentType = null;
 			String[] strs = attribute.split(":");
 			if (strs != null && strs.length > 1)
 				parentType = strs[1];
 			else
 				parentType = attribute;
 			ctype.setParentType(parentType);
 
 			if (!Utils.isEmpty(parentType)) {
 				xsdDocument.addParentToComplexTypeMap(parentType, ctype
 						.getName());
 			}
 		}
 		logger.log(Level.FINER,
 				"Exiting parseChildElements method in XSDParser",
 				new Object[] { elements });
 		return elements;
 	}
 
 	/**
 	 * Search c type.
 	 * 
 	 * @param ctypes
 	 *            the ctypes
 	 * @param elementName
 	 *            the element name
 	 * @return search for a complex type and return the matching one by name
 	 */
 	public static org.w3c.dom.Element searchCType(final NodeList ctypes,
 			final String elementName) {
 		Logger.getLogger(XSDParserImpl.class.getName()).log(Level.FINER,
 				"Entering searchCType method in XSDParser",
 				new Object[] { ctypes, elementName });
 		int size = ctypes.getLength();
 		org.w3c.dom.Element elem = null;
 		for (int i = 0; i < size; i++) {
 			elem = (org.w3c.dom.Element) ctypes.item(i);
 			String name = elem.getAttribute("name");
 			// System.out.println("ctype name is " + name);
 			if (elementName.equalsIgnoreCase(name)) {
 				break;
 			} else {
 				elem = null;
 			}
 		}
 		Logger.getLogger(XSDParserImpl.class.getName()).log(Level.FINER,
 				"Exiting searchCType method in XSDParser",
 				new Object[] { elem });
 		return elem;
 	}
 
 	/**
 	 * Link child types.
 	 * 
 	 * @param xsdDocument
 	 *            build relationships of complex types to its derived types in
 	 *            the hierarchy
 	 */
 	private void linkChildTypes(XSDDocument xsdDocument) {
 		logger.log(Level.FINER, "Entering linkChildTypes method in XSDParser",
 				xsdDocument);
 		List<ComplexType> ctypes = xsdDocument.getAllComplexTypes();
 		for (ComplexType ctype : ctypes) {
 			if (ctype != null) {
 				String ctypeName = ctype.getName();
 				String parentType = ctype.getParentType();
 				if (parentType != null) {
 					ComplexType parent = xsdDocument.searchCType(parentType);
 					if (parent != null) {
 						parent.getChildType().add(ctypeName);
 					}
 				}
 			}
 		}
 		logger.log(Level.FINER, "Exiting linkChildTypes method in XSDParser",
 				xsdDocument);
 	}
 
 	/**
 	 * Parses the complex types.
 	 * 
 	 * @param tagName
 	 *            the tag name
 	 * @param srcDoc
 	 *            dom handle
 	 * @param xsdDocument
 	 *            handle parses the complex types, its annotations and populates
 	 *            the xsd
 	 * @throws XsdDocException 
 	 */
 	private void parseComplexTypes(Document srcDoc,
 			XSDDocument xsdDocument) throws ParserException {
 		logger.log(Level.FINER,
 				"Entering parseComplexTypes method in XSDParser", new Object[] {
 						 srcDoc, xsdDocument });
 		NodeList ctypeElements = srcDoc.getElementsByTagNameNS("*", "complexType");
 		this.setCtypes(ctypeElements);
 		int noOfDomElements = ctypeElements.getLength();
 		for (int i = 0; i < noOfDomElements; i++) {
 			org.w3c.dom.Element obj = (org.w3c.dom.Element) ctypeElements
 					.item(i);
 			xsdDocument.addComplexType(processComplexType(xsdDocument, obj));
 		}
 		logger.log(Level.FINER,
 				"Exiting parseComplexTypes method in XSDParser", xsdDocument);
 	}
 
 	private ComplexType processComplexType(XSDDocument xsdDocument,
 			org.w3c.dom.Element cTypeElem) throws ParserException {
 		String name = cTypeElem.getAttribute("name");
 		ComplexType cType = Context.getContext().getNewComplexType();
 		cType.setName(name);
 		cType.setAnnotationInfo(parseAnnotation( cTypeElem));
 		NamedNodeMap nameNodeMap = cTypeElem.getAttributes();
 		int size = nameNodeMap.getLength();
 
 		for (int j = 0; j < size; j++) {
 			Attr attr = (Attr) nameNodeMap.item(j);
 			if (attr != null) {
 				Attribute attribute = new Attribute();
 				attribute.setName(attr.getName());
 				attribute.setValue(attr.getValue());
 				cType.getAttributes().add(attribute);
 			}
 		}
 		cType.setChildElements(this.parseChildElements( cType,
 				 cTypeElem, xsdDocument));
 		List<Element> instances = (List<Element>) typeElementsMap.get(name);
 		if (instances != null) {
 			cType.setInstanceElements(instances);
 		}
 		postProcessComplexType(cType, cTypeElem);
 		return cType;
 	}
 
 	/**
 	 * Parses the enum elements.
 	 * 
 	 * @param namespacePrefix
 	 *            the namespace prefix
 	 * @param element
 	 *            the element
 	 * @param xsdDocument
 	 *            the xsd document
 	 * @return collection of enums contained by an element associates enums to
 	 *         its annotations
 	 * @throws ParserException 
 	 */
 	private List<EnumElement> parseEnumElements(
 			org.w3c.dom.Element element, XSDDocument xsdDocument) throws ParserException {
 		logger.log(Level.FINER,
 				"Entering parseEnumElements method in XSDParser", new Object[] {
 						element, xsdDocument });
 		List<EnumElement> elements = new ArrayList<EnumElement>();
 		NodeList domElements = element.getElementsByTagNameNS("*",  "enumeration");
 		int noOfDomElements = domElements.getLength();
 		for (int i = 0; i < noOfDomElements; i++) {
 			org.w3c.dom.Element obj = (org.w3c.dom.Element) domElements.item(i);
 			String value = obj.getAttribute("value");
 
 			EnumElement elem = Context.getContext().getNewEnumeration();
 			elem.setValue(value);
 			elem.setType(element.getAttribute("name"));
 			elem.setAnnotations(parseAnnotation( obj));
 			elements.add(elem);
 			postProcessEnum(elem, obj);
 			xsdDocument.addEnum(elem);
 		}
 		logger.log(Level.FINER,
 				"Exiting parseEnumElements method in XSDParser", elements);
 		return elements;
 	}
 
 	/**
 	 * Parses the simple types.
 	 * 
 	 * @param tagName
 	 *            the tag name
 	 * @param srcDoc
 	 *            the src doc
 	 * @param xsdDocument
 	 *            parses SimpleTypes within the document TBD: has to make it
 	 *            generic across namespaces
 	 * @throws XsdDocException 
 	 */
 	private void parseSimpleTypes(Document srcDoc,
 			XSDDocument xsdDocument) throws ParserException {
 		logger.log(Level.FINER,
 				"Entering parseSimpleTypes method in XSDParser", new Object[] {
 						srcDoc, xsdDocument });
 		NodeList domElements = srcDoc.getElementsByTagNameNS("*", "simpleType");
 		int noOfDomElements = domElements.getLength();
 		// System.out.println("no. of simple types" + noOfDomElements);
 		for (int i = 0; i < noOfDomElements; i++) {
 			org.w3c.dom.Element obj = (org.w3c.dom.Element) domElements.item(i);
 			SimpleType sType=populateSimpleType(xsdDocument, obj,false);
 				org.w3c.dom.Element parent = (org.w3c.dom.Element) obj
 				.getParentNode();
 				if(!parent.getTagName().endsWith(":attribute")){
 					if(Utils.isEmpty(sType.getName())){
 						sType.setName("SimpleType"+i);
 					}
 					xsdDocument.addSimpleType(sType);
 				}
 			
 		}
 		logger.log(Level.FINER, "Exiting parseSimpleTypes method in XSDParser",
 				xsdDocument);
 	}
 
 	private SimpleType populateSimpleType(XSDDocument xsdDocument,
 			org.w3c.dom.Element obj,boolean fromAttribute) throws ParserException {
 		NodeList nodes = obj.getElementsByTagNameNS("*",  "restriction");
 		String base = ((org.w3c.dom.Element) nodes.item(0))
 				.getAttribute("base");
 		String name = obj.getAttribute("name");
 		//String type = obj.getAttribute("type");
 		// System.out.println("name + ......." + name + " type " + type);
 		SimpleType sType = Context.getContext().getNewSimpleType();
 		sType.setBase(base);
 		sType.setName(name);
 		List<Element> instances = (List<Element>) typeElementsMap.get(name);
 		NamedNodeMap nameNodeMap = obj.getAttributes();
 		int size = nameNodeMap.getLength();
 
 		for (int j = 0; j < size; j++) {
 			Attr attr = (Attr) nameNodeMap.item(j);
 			if (attr != null) {
 				Attribute attribute = new Attribute();
 				attribute.setName(attr.getName());
 				attribute.setValue(attr.getValue());
 				sType.getAttributes().add(attribute);
 			}
 		}
 		sType.setAnnotationInfo(parseAnnotation( obj));
 		sType.setEnums(this.parseEnumElements( obj, xsdDocument));
 		if (instances != null) {
 			sType.setInstanceElements(instances);
 		}
 		
 		postProcessSimpleType(sType,obj);
 		return sType;
 	}
 
 	protected void postProcessSimpleType(SimpleType type,org.w3c.dom.Element typeElement){
 		//Do Nothing exposed for extensibility
 	}
 	
 	
 	protected void postProcessComplexType(ComplexType type,org.w3c.dom.Element typeElement){
 		//Do Nothing exposed for extensibility
 	}
 	protected void postProcessElement(Element type,org.w3c.dom.Element typeElement){
 		//Do Nothing exposed for extensibility
 	}
 	protected void postProcessAttribute(AttributeElement type,org.w3c.dom.Element typeElement){
 		//Do Nothing exposed for extensibility
 	}
 	protected void postProcessEnum(EnumElement type,org.w3c.dom.Element typeElement){
 		//Do Nothing exposed for extensibility
 	}
 	protected void postProcessXsdDocument(XSDDocument doc,Document document){
 		//Do Nothing exposed for extensibility
 	}
 	/**
 	 * Parses the annotation.
 	 * 
 	 * @param namespacePrefix
 	 *            the namespace prefix
 	 * @param elem
 	 *            the elem
 	 * @return parses annotation dom element and populates its POJO
 	 *         representation ParsedAnnotationInfo
 	 */
 	private ParsedAnnotationInfo parseAnnotation(
 			org.w3c.dom.Element elem) {
 		logger.log(Level.FINER, "Entering parseAnnotation method in XSDParser",
 				elem);
 		org.w3c.dom.Element anno= (org.w3c.dom.Element) getFirstChildElementWithTagName(elem,"annotation");
 		ParsedAnnotationInfo info = new ParsedAnnotationInfo();
 		if(anno!=null){
 			org.w3c.dom.Element docElement = (org.w3c.dom.Element) getFirstChildElementWithTagName(anno,"documentation");
 			if (docElement != null) {
 				processDocumentation(docElement, info);
 			}
 			org.w3c.dom.Element node = (org.w3c.dom.Element)  getFirstChildElementWithTagName(anno,"appinfo");
 			if (node != null) {
 				for (Node childNode = node.getFirstChild(); childNode != null;) {
 					Node nextChild = childNode.getNextSibling();
 					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
 						String tagName = childNode.getNodeName();
 						AnnotationParser parser = getAnnotationParser(tagName);
 						if (parser != null) {
 							ParsedAnnotationTag parsedData = parser
 									.parseAnnotation((org.w3c.dom.Element) childNode);
 							info.addParsedAnnotationTag(tagName, parsedData);
 						}
 					}
 					childNode = nextChild;
 				}
 			}
 		}
 		logger.log(Level.FINER, "Exiting parseAnnotation method in XSDParser",
 				info);
 		return info;
 	}
 	private Node getFirstChildElementWithTagName(org.w3c.dom.Element element,
 			String withTagName) {
 		for (Node childNode = element.getFirstChild(); childNode != null;) {
 			Node nextChild = childNode.getNextSibling();
 			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
 				String tagName = ((org.w3c.dom.Element)childNode).getLocalName();
 				if(tagName.equals(withTagName)){
 					return childNode;
 				}
 			}
 			childNode = nextChild;
 		}
 		return null;
 	}
 	protected void processDocumentation(org.w3c.dom.Element docElement,
 			ParsedAnnotationInfo info) {
 		String documentation="";
 		for (Node childNode = docElement.getFirstChild(); childNode != null;) {
 			Node nextChild = childNode.getNextSibling();
 			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
 				String tagName = childNode.getNodeName();
 				AnnotationParser parser = getAnnotationParser(tagName);
 				if (parser != null) {
 					ParsedAnnotationTag parsedData = parser
 							.parseAnnotation((org.w3c.dom.Element) childNode);
 					info.addParsedAnnotationTag(tagName, parsedData);
 				}
 			}else if(childNode.getNodeType()==Node.TEXT_NODE){
 				documentation=documentation+childNode.getNodeValue();
 			}else if(childNode.getNodeType()==Node.COMMENT_NODE){
 				documentation=documentation+("<!--"+childNode.getNodeValue()+"-->");
 			}
 			childNode = nextChild;
 		}
 		info.setDocumentation(documentation);
 	}
 
 	/**
 	 * Gets the annotation parser.
 	 * 
 	 * @param tagName
 	 *            the tag name
 	 * @return the annotation parser
 	 */
 	private AnnotationParser getAnnotationParser(String tagName) {
 		logger.finer("getAnnotationParser - tagname" + tagName);
 		Context context = Context.getContext();
 		if (context.getAnnotationParsers() != null
 				&& context.getAnnotationParsers().containsKey(tagName)) {
 			return context.getAnnotationParsers().get(tagName);
 		} else if (context.getDefaultAnnotationParser() != null) {
 			return context.getDefaultAnnotationParser();
 		}
 		return null;
 	}
 
 	
 
 }
