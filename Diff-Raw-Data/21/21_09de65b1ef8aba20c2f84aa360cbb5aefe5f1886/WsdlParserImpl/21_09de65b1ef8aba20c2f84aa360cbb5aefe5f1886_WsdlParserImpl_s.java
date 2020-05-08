 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.annoparser.parser.impl;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.wsdl.Definition;
 import javax.wsdl.Input;
 import javax.wsdl.Operation;
 import javax.wsdl.Output;
 import javax.wsdl.Part;
 import javax.wsdl.PortType;
 import javax.wsdl.Service;
 import javax.wsdl.WSDLException;
 import javax.wsdl.factory.WSDLFactory;
 import javax.wsdl.xml.WSDLReader;
 import javax.xml.namespace.QName;
 
 import org.ebayopensource.turmeric.tools.annoparser.WSDLDocument;
 import org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface;
 import org.ebayopensource.turmeric.tools.annoparser.context.Context;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ComplexType;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.Element;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.OperationHolder;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ParsedAnnotationInfo;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ParsedAnnotationTag;
 import org.ebayopensource.turmeric.tools.annoparser.exception.ParserException;
 import org.ebayopensource.turmeric.tools.annoparser.parser.AnnotationParser;
 import org.ebayopensource.turmeric.tools.annoparser.parser.WsdlParser;
 import org.ebayopensource.turmeric.tools.annoparser.parser.XSDParser;
 import org.ebayopensource.turmeric.tools.annoparser.utils.Utils;
 import org.w3c.dom.Node;
 
 
 
 /**
  *  
  * This Class does the light-weight parsing of wsdl to POJO interface to wsdl
  * It also acts as a container to xsddocument handle and provides a window to the
  * schema
  * @author srengarajan
  */
 public class WsdlParserImpl implements WsdlParser {
 
 	
 	private final static String CLASS_NAME = WsdlParserImpl.class.getClass().getName();  
 	Logger logger  = Logger.getLogger(CLASS_NAME);
 	
 	
 	/**
 	 * handle to the schema 
 	 */
 	private XSDDocInterface xsdDocument = null;
 
 	/**
 	 * handle to the wsdl document
 	 */
 	private WSDLDocument wsdlDoc = null;
 
 	/**
 	 * association between operation names and collection of operations
 	 */
 	private Map<String, List<OperationHolder>> typeOpMap = new HashMap<String, List<OperationHolder>>();
 
 //	/**
 //	 * captures all possible paths to a particular field.
 //	 */
 //	private List<String> allPossiblePaths = new ArrayList<String>();
 //	
 //	private List<String> allInputPaths = new ArrayList<String>();
 //	
 //	private List<String> allOutputPaths = new ArrayList<String>();
 	
 	
 	/**
 	 * captures the association between element it its Calling Operations
 	 */
 	private Map<String, Set<String>> elemCallMap = new HashMap<String, Set<String>>();
 
 	/**
 	 * Gets the type op map.
 	 *
 	 * @return type to Operation association
 	 */
 	public Map<String, List<OperationHolder>> getTypeOpMap() {
 		return typeOpMap;
 	}
 
 	/**
 	 * Sets the type op map.
 	 *
 	 * @param typeOpMap setter for type to Operations association
 	 */
 	public void setTypeOpMap(Map<String, List<OperationHolder>> typeOpMap) {
 		this.typeOpMap = typeOpMap;
 	}
 
 	/**
 	 * Adds the entry.
 	 *
 	 * @param type the type
 	 * @param opHolder the op holder
 	 */
 	public void addEntry(String type, OperationHolder opHolder) {
 		logger.entering("WsdlParser", "addEntry",new Object[]{type,opHolder});
 		Object obj = this.typeOpMap.get(type);
 		if( obj == null ) {
 			this.typeOpMap.put(type, new ArrayList<OperationHolder>());
 		} 
 		this.typeOpMap.get(type).add(opHolder);
 		logger.exiting("WsdlParser", "addEntry");
 	}
 
 	/**
 	 * Instantiates a new wsdl parser impl.
 	 */
 	public WsdlParserImpl() {
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.ebayopensource.turmeric.tools.annoparser.parser.WsdlParser#parse(
 	 * java.lang.String)
 	 */
 	public synchronized WSDLDocument parse(String url) throws ParserException {
 		logger.entering("WsdlParser", "parse", url);
 		// WSDLDocument
 		wsdlDoc = Context.getContext().getNewWsdlDocument();
 		// this.wsdlDocument = wsdlDoc;
 		try {
 			XSDParser xsdParser = null;
 			xsdParser = Context.getContext().getNewXsdParser();
 			xsdDocument = xsdParser.parse(url);
 			wsdlDoc.setXsdDocument(xsdDocument);
 			wsdlDoc.setDocumentURL(xsdDocument.getDocumentURL());
 			WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
 			Definition wsdlDefinition = wsdlReader.readWSDL(null, url);
 
 			Map<QName, Service> allServices = wsdlDefinition.getAllServices();
 			
 			for (Map.Entry<QName, Service> entry : allServices.entrySet()) {
 				QName qname = entry.getKey();
 				
 				URI uri=new URI(qname.getNamespaceURI());
 				wsdlDoc.setPackageName(uri.getPath());
 				wsdlDoc.setServiceName(qname.getLocalPart());
 				wsdlDoc.setCompleteRemotePath(qname.getNamespaceURI());
 				org.w3c.dom.Element documentationElem=(org.w3c.dom.Element)entry.getValue().getDocumentationElement();
 				wsdlDoc.setAnnotations(parseAnnotation(documentationElem));
 			}
 
 			// Sai Changes
 
 			Map<QName, PortType> allPortTypes = wsdlDefinition
 					.getAllPortTypes();
 			for (Map.Entry<QName, PortType> entry : allPortTypes.entrySet()) {
 				QName name = entry.getKey();
 
 				PortType p = entry.getValue();
 				org.ebayopensource.turmeric.tools.annoparser.dataobjects.PortType portType = Context.getContext().getNewPortType();
 				portType.setName(name.getLocalPart());
 				portType.setAnnotations(parseAnnotation(p.getDocumentationElement()));
 				List<Operation> operations = p.getOperations();
 				for (Operation op : operations) {
 					OperationHolder opHolder = Context.getContext().getNewOperation();
 					opHolder.setName(op.getName());
 					opHolder.setAnnotations(parseAnnotation(op.getDocumentationElement()));
 					wsdlDoc.addOperation(opHolder);
 					portType.addOperation(opHolder);
 
 					Output output = op.getOutput();
 					Input input = op.getInput();
 					Map inputParts = (input == null || input.getMessage() == null ) ? null : input
 							.getMessage().getParts();
 					Map outputParts = (output == null || output.getMessage() == null ) ? null : output
 							.getMessage().getParts();
 
 					if (inputParts != null) {
 						for (Object obj1 : inputParts.values()) {
 							Part ipPart = (Part) obj1;
 							
 							
 							QName type = ipPart.getElementName();
 							if(type == null) {
 								type = ipPart.getTypeName();
 							}
 							String ipElemName = type.getLocalPart();
 							Element elem = xsdDocument
 									.searchIndependentElement(ipElemName);
 							if( elem != null)
 								this.addEntry(elem.getType(), opHolder);
 
 							ComplexType ctype = (elem != null) ? xsdDocument
 									.searchCType(getLocalName(elem.getType())) : null;
 							if (ctype != null) {
 								List<Element> elements = ctype
 										.getInstanceElements();
 								if (elements != null){
									opHolder.setInput(elements);
 								}
 							}
 
 							logger.info("part name "
 									+ ipElemName);
 
 						}
 					}
 
 					if (outputParts != null) {
 						for (Object obj2 : outputParts.values()) {
 							Part opPart = (Part) obj2;
 							QName type = opPart.getElementName();
 							if(type == null) {
 								type = opPart.getTypeName();
 							}
 							String opElemName = type.getLocalPart();
 							
 							//String opElementName = opPart.getElementName()
 							//		.getLocalPart();
 							Element elem = (opElemName != null ) ? xsdDocument
 									.searchIndependentElement(opElemName) : null;
 							ComplexType ctype = (elem != null ) ? xsdDocument
 									.searchCType(getLocalName(elem.getType())) : null;
 
 							if (ctype != null) {
 								List<Element> elements = ctype
 										.getInstanceElements();
								if (elements != null)
									opHolder.setOutput(elements);
 							}
 							if( elem != null )
 								this.addEntry(elem.getType(), opHolder);
 						}
 					}
 					postProcessOperation(opHolder,op);
 				}
 				postProcessPortType(portType,p);
 				wsdlDoc.addPortType(portType);
 			}
 			postProcessWsdlDocument(wsdlDoc, wsdlDefinition);
 		} catch (WSDLException e) {
 			logger.log(Level.SEVERE, "A WSDLException occurred", e);
 			throw new ParserException("Failed to parse " + url + "A WSDLException occurred. Cause for the Exception is " + e.getMessage(),e);
 		} catch (URISyntaxException e) {
 			logger.log(Level.SEVERE, "Wsdl Service Namespace is not valid", e);
 			throw new ParserException(e);
 		}
 		addExtentionElements();
 		
 		
 		logger.exiting("WsdlParser", "parse", wsdlDoc);
 		
 		return wsdlDoc;
 
 	}
 	
 	
 	protected void postProcessOperation(OperationHolder type,Operation operation){
 		//Do Nothing exposed for extensibility
 	}
 	protected void postProcessPortType(org.ebayopensource.turmeric.tools.annoparser.dataobjects.PortType type,PortType operation){
 		//Do Nothing exposed for extensibility
 	}
 	protected void postProcessWsdlDocument(WSDLDocument doc,Definition wsdlDefinition ){
 		//Do Nothing exposed for extensibility
 	}
 	/**
 	 * Parses the annotation.
 	 *
 	 * @param namespacePrefix the namespace prefix
 	 * @param elem the elem
 	 * @return parses annotation dom element and populates its POJO
 	 * representation ParsedAnnotationInfo
 	 */
 	private ParsedAnnotationInfo parseAnnotation( org.w3c.dom.Element node) {
 		logger.log(Level.FINER, "Entering parseAnnotation method in XSDParser",
 				node);
 		ParsedAnnotationInfo info = null;
 		if (node != null) {
 			info = new ParsedAnnotationInfo();
 			processDocumentation(node, info);
 		}
 		logger.log(Level.FINER, "Exiting parseAnnotation method in XSDParser",
 				info);
 		return info;
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
 		if(!Utils.isEmpty(documentation)){
 			info.setDocumentation(documentation);
 		}
 	}
 	/**
 	 * Gets the annotation parser.
 	 *
 	 * @param tagName the tag name
 	 * @return the annotation parser
 	 */
 	private AnnotationParser getAnnotationParser(String tagName){
 		logger.finer("getAnnotationParser - tagname" + tagName);
 		Context context=Context.getContext();
 		if(context.getAnnotationParsers()!=null && context.getAnnotationParsers().containsKey(tagName)){
 			return context.getAnnotationParsers().get(tagName);
 		}else if(context.getDefaultAnnotationParser()!=null){
 			return context.getDefaultAnnotationParser();
 		}
 		return null;		
 	}
 
 	/**
 	 * Gets the local name.
 	 *
 	 * @param ctype the ctype
 	 * @return local name after stripping namespace prefix
 	 */
 	private String getLocalName(String ctype) {
 		logger.info("Local Name:" + ctype);
 		String[] strs = ctype.split(":");
 		if (strs.length > 1)
 			return strs[1];
 		else
 			return strs[0];
 	}
 	
 	
 	/**
 	 * Find all paths.
 	 *
 	 * @param paths the paths
 	 * @param ctypeName the ctype name
 	 * @param parentPath This is the core functionality used to build all possible
 	 * usages of a particular field element. It also takes into
 	 * consideration the inheritance of the complex types a field is
 	 * part of.
 	 */
 	private void addExtentionElements() {
 		
 		
 		// this.xsdDocument.print();
 		logger.entering("WsdlParser", "findAllPaths", new Object[] { });
 		for(ComplexType ctype:wsdlDoc.getAllComplexTypes()){
 			Set<Element> children  = ctype.getChildElements();
 			while (true) {
 				String parent = ctype.getParentType();
 				logger.finest("parent is " + parent);
 				if (parent != null && !"".equalsIgnoreCase(parent)) {
 					ctype = this.xsdDocument.searchCType(parent);
 					if (ctype != null) {
 						children.addAll(ctype.getChildElements());
 					} else {
 						break;
 					}
 				} else {
 					break;
 				}
 			}
 		}
 
 		logger.exiting("WsdlParser", "findAllPaths");
 	}
 	
 	
 	
 	
 }
