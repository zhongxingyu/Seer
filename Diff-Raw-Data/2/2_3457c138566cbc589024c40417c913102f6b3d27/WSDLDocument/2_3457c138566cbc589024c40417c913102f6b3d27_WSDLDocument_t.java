 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.tools.annoparser;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ComplexType;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.Element;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.EnumElement;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.OperationHolder;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.ParsedAnnotationInfo;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.PortType;
 import org.ebayopensource.turmeric.tools.annoparser.dataobjects.SimpleType;
 
 /**
  * The implementation of WSDLDocInterface.
  *
  * @author srengarajan
  */
 public class WSDLDocument implements WSDLDocInterface {
 
 	/** The xsd document defined in the WSDL. */
 	private XSDDocInterface xsdDocument = null;
 	
 	/** The service name. */
 	private String serviceName = null;
 	
 	/** List of all operations defined in the WSDl */
 	private List<OperationHolder> operations = new ArrayList<OperationHolder>();
 	
 	/** List of all port types defined in the WSDL. */
 	private List<PortType> portTypes;
 	
 	/** The package name. */
 	private String packageName;
 	
 	
 	
 	/** The annotations. */
 	private ParsedAnnotationInfo annotations;
 	
 	
 
 	/** The complete remote path. */
 	private String completeRemotePath;
 	
 	/** Actual URL of the  WSDL document.*/
 	private URL documentURL;
 
 
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface#getDocumentURL()
 	 */
 	public URL getDocumentURL() {
 		return documentURL;
 	}
 	
 	/**
 	 * Sets the document url.
 	 *
 	 * @param documentURL the new document url
 	 */
 	public void setDocumentURL(URL documentURL) {
 		this.documentURL = documentURL;
 	}
 
 	
 
 	/**
 	 * Gets the operations.
 	 *
 	 * @return the operations
 	 */
 	public List<OperationHolder> getOperations() {
 		return operations;
 	}
 
 	/**
 	 * Gets the xsd document.
 	 *
 	 * @return the xsd document
 	 */
 	public XSDDocInterface getXsdDocument() {
 		return xsdDocument;
 	}
 
 	/**
 	 * Sets the xsd document.
 	 *
 	 * @param xsdDocument the new xsd document
 	 */
 	public void setXsdDocument(XSDDocInterface xsdDocument) {
 		this.xsdDocument = xsdDocument;
 	}
 
 	/**
 	 * Instantiates a new wSDL document.
 	 */
 	public WSDLDocument() {
 		super();
 	}
 
 	
 
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.WSDLDocInterface#getAllOperations()
 	 */
 	public List<OperationHolder> getAllOperations() {
 		return operations;
 	}
 	
 	/**
 	 * Sets the operations.
 	 *
 	 * @param operations the new operations
 	 */
 	public void setOperations(List<OperationHolder> operations) {
 		this.operations = operations;
 	}
 	
 	/**
 	 * Adds the operation.
 	 *
 	 * @param operation the operation
 	 */
 	public void addOperation(OperationHolder operation) {
 		operations.add(operation);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.WSDLDocInterface#getServiceName()
 	 */
 	public String getServiceName() {
 		return serviceName;
 	}
 	
 	/**
 	 * Sets the service name.
 	 *
 	 * @param serviceName the new service name
 	 */
 	public void setServiceName(String serviceName) {
 		this.serviceName = serviceName;
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface#getAllComplexTypes()
 	 */
 	public List<ComplexType> getAllComplexTypes() {
 		// TODO Auto-generated method stub
 		return xsdDocument.getAllComplexTypes();
 	}
 	
 	
 
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface#getAllIndependentElements()
 	 */
 	public List<Element> getAllIndependentElements() {
 		return xsdDocument.getAllIndependentElements();
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface#getAllEnums()
 	 */
 	public List<EnumElement> getAllEnums() {
 		return xsdDocument.getAllEnums();
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface#getAllSimpleTypes()
 	 */
 	public List<SimpleType> getAllSimpleTypes() {
 		return xsdDocument.getAllSimpleTypes();
 	}
 
 	
 
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface#searchCType(java.lang.String)
 	 */
 	public ComplexType searchCType(String name) {
 		if(xsdDocument!=null){
 			return this.xsdDocument.searchCType(name);
 		}
		return null;
 	}
 
 	
 
 	
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface#searchIndependentElement(java.lang.String)
 	 */
 	public Element searchIndependentElement(String elementName) {
 		return xsdDocument.searchIndependentElement(elementName);
 	}
 
 
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface#getElementComplexTypeMap()
 	 */
 	public Map<String, List<ComplexType>> getElementComplexTypeMap() {
 		// TODO Auto-generated method stub
 		return xsdDocument.getElementComplexTypeMap();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.WSDLDocInterface#getPortTypes()
 	 */
 	public List<PortType> getPortTypes() {
 		return portTypes;
 	}
 
 	/**
 	 * Sets the port types.
 	 *
 	 * @param portTypes the new port types
 	 */
 	public void setPortTypes(List<PortType> portTypes) {
 		this.portTypes = portTypes;
 	}
 	
 	/**
 	 * Adds the port type.
 	 *
 	 * @param portType the port type
 	 */
 	public void addPortType(PortType portType){
 		if(portTypes==null){
 			portTypes=new ArrayList<PortType>();
 		}
 		this.getPortTypes().add(portType);
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.XSDDocInterface#searchSimpleType(java.lang.String)
 	 */
 	public SimpleType searchSimpleType(String name) {
 		if(xsdDocument!=null){
 			return this.xsdDocument.searchSimpleType(name);
 		}
 		return null;
 	}
 
 	/**
 	 * Sets the package name.
 	 *
 	 * @param packageName the new package name
 	 */
 	public void setPackageName(String packageName){
 		this.packageName=packageName;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.WSDLDocInterface#getPackageName()
 	 */
 	public String getPackageName() {
 		return packageName;	
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.tools.annoparser.WSDLDocInterface#getCompleteRemotePath()
 	 */
 	public String getCompleteRemotePath() {
 		
 		return completeRemotePath;
 	}
 
 	/**
 	 * Sets the complete remote path.
 	 *
 	 * @param completeRemotePath the new complete remote path
 	 */
 	public void setCompleteRemotePath(String completeRemotePath) {
 		this.completeRemotePath = completeRemotePath;
 	}
 
 	public ParsedAnnotationInfo getAnnotations() {
 		return annotations;
 	}
 
 	public void setAnnotations(ParsedAnnotationInfo annotations) {
 		this.annotations = annotations;
 	}
 
 	@Override
 	public Map<String, Set<String>> getParentToComplexTypeMap() {
 		
 		return xsdDocument.getParentToComplexTypeMap();
 	}
 }
