 /******************************************************************************* 
  * Copyright (c) 2010 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.ui.utils;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.wsdl.Binding;
 import javax.wsdl.BindingOperation;
 import javax.wsdl.Definition;
 import javax.wsdl.Import;
 import javax.wsdl.Message;
 import javax.wsdl.Operation;
 import javax.wsdl.Part;
 import javax.wsdl.Port;
 import javax.wsdl.PortType;
 import javax.wsdl.Service;
 import javax.wsdl.Types;
 import javax.wsdl.WSDLException;
 import javax.wsdl.extensions.schema.Schema;
 import javax.wsdl.extensions.schema.SchemaImport;
 import javax.wsdl.extensions.soap.SOAPAddress;
 import javax.wsdl.extensions.soap.SOAPOperation;
 import javax.wsdl.extensions.soap12.SOAP12Address;
 import javax.wsdl.extensions.soap12.SOAP12Operation;
 import javax.wsdl.factory.WSDLFactory;
 import javax.wsdl.xml.WSDLReader;
 import javax.xml.namespace.QName;
 
 //import org.jdom.Attribute;
 import org.jdom.input.DOMBuilder;
 
 import com.ibm.wsdl.Constants;
 
 /**
  * @author bfitzpat
  *
  */
 public class TesterWSDLUtils {
 	private static final String IMPORT_TAG = "import"; //$NON-NLS-1$
 	private static final String VALUE_ATTR = "value"; //$NON-NLS-1$
 	private static final String SEQUENCE_NAME = "sequence"; //$NON-NLS-1$
 	private static final String COMPLEX_TYPE_NAME = "complexType"; //$NON-NLS-1$
 	private static final String ENUMERATION_NAME = "enumeration"; //$NON-NLS-1$
 	private static final String RESTRICTION_NAME = "restriction"; //$NON-NLS-1$
 	private static final String SIMPLE_TYPE_NAME = "simpleType"; //$NON-NLS-1$
 	private static final String INT_TYPE_NAME = "int"; //$NON-NLS-1$
 	private static final String STRING_TYPE_NAME = "string"; //$NON-NLS-1$
 	private static final String MIN_OCCURS_ATTR = "minOccurs"; //$NON-NLS-1$
 	private static final String TYPE_ATTR = "type"; //$NON-NLS-1$
 	private static final String NAME_ATTR = "name"; //$NON-NLS-1$
 	
 	private final static String DEF_FACTORY_PROPERTY_NAME =
 		"javax.wsdl.factory.DefinitionFactory"; //$NON-NLS-1$
 	private final static String PRIVATE_DEF_FACTORY_CLASS =
 		"org.apache.wsif.wsdl.WSIFWSDLFactoryImpl"; //$NON-NLS-1$
 
 	public static Definition readWSDLURL(URL contextURL, String wsdlLoc) throws WSDLException {
 		Properties props = System.getProperties();
 		String oldPropValue = props.getProperty(DEF_FACTORY_PROPERTY_NAME);
 
 		props.setProperty(DEF_FACTORY_PROPERTY_NAME, PRIVATE_DEF_FACTORY_CLASS);
 
 		WSDLFactory factory = WSDLFactory.newInstance();
 		WSDLReader wsdlReader = factory.newWSDLReader();
 		wsdlReader.setFeature(Constants.FEATURE_VERBOSE, false);
 		wsdlReader.setFeature("javax.wsdl.importDocuments", true); //$NON-NLS-1$
 		String context = null;
 		if (contextURL != null)
 			context = contextURL.toString();
 		Definition def = wsdlReader.readWSDL(context, wsdlLoc);
 
 		if (oldPropValue != null) {
 			props.setProperty(DEF_FACTORY_PROPERTY_NAME, oldPropValue);
 		} else {
 			props.remove(DEF_FACTORY_PROPERTY_NAME);
 		}
 		return def;
 	}
 
 	public static Definition readWSDLURL(URL contextURL) throws WSDLException {
 		Properties props = System.getProperties();
 		String oldPropValue = props.getProperty(DEF_FACTORY_PROPERTY_NAME);
 
 		props.setProperty(DEF_FACTORY_PROPERTY_NAME, PRIVATE_DEF_FACTORY_CLASS);
 
 		WSDLFactory factory = WSDLFactory.newInstance();
 		WSDLReader wsdlReader = factory.newWSDLReader();
 		wsdlReader.setFeature(Constants.FEATURE_VERBOSE, false);
 		wsdlReader.setFeature("javax.wsdl.importDocuments", true); //$NON-NLS-1$
 		String context = null;
 		if (contextURL != null)
 			context = contextURL.toString();
 		Definition def = wsdlReader.readWSDL(context);
 
 		if (oldPropValue != null) {
 			props.setProperty(DEF_FACTORY_PROPERTY_NAME, oldPropValue);
 		} else {
 			props.remove(DEF_FACTORY_PROPERTY_NAME);
 		}
 		return def;
 	}
 
 	public static String getSampleSOAPInputMessage ( Definition wsdlDefinition, String serviceName, String portName, String bindingName, String opName ) {
 		Map<?, ?> services = wsdlDefinition.getServices();
 		Set<?> serviceKeys = services.keySet();
 		for( Iterator<?> it = serviceKeys.iterator(); it.hasNext(); ) {
 			QName serviceKey = (QName) it.next();
 			if (serviceName != null && serviceKey.getLocalPart().contentEquals(serviceName)) {
 				Service service = (Service) services.get( serviceKey );
 				Map<?, ?> ports = service.getPorts();
 				Set<?> portKeys = ports.keySet();
 				for( Iterator<?> it2 = portKeys.iterator(); it2.hasNext(); ) {
 					String portKey = (String) it2.next();
 					if (portName != null && portKey.contentEquals(portName)) {
 						Port port = (Port) ports.get( portKey );
 						Binding wsdlBinding = port.getBinding();
 						PortType portType = wsdlBinding.getPortType();
 						List<?> operations = portType.getOperations();
 						for (Iterator<?> it3 = operations.iterator(); it3.hasNext();){
 							Operation operation = (Operation) it3.next();
 							if (opName != null && operation.getName().contentEquals(opName)) {
 								Message inputMsg = operation.getInput().getMessage();
 								Collection<?> parts = inputMsg.getParts().values();
 								for( Iterator<?> it4 = parts.iterator(); it4.hasNext(); ) {
 									Part part = (Part) it4.next();
 									String schemaName = null;
 									if (part.getElementName() != null) {
 										schemaName = part.getElementName().getLocalPart();
 									} else {
 										schemaName = part.getName();
 									}
									String out = createMessageForSchemaElement(wsdlDefinition, schemaName);
 									return out;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	public static String getEndpointURL ( Definition wsdlDefinition, String serviceName, String portName, String bindingName, String opName ) {
 		Map<?, ?> services = wsdlDefinition.getServices();
 		Set<?> serviceKeys = services.keySet();
 		for( Iterator<?> it = serviceKeys.iterator(); it.hasNext(); ) {
 			QName serviceKey = (QName) it.next();
 			if (serviceName != null && serviceKey.getLocalPart().contentEquals(serviceName)) {
 				Service service = (Service) services.get( serviceKey );
 				Map<?, ?> ports = service.getPorts();
 				Set<?> portKeys = ports.keySet();
 				for( Iterator<?> it2 = portKeys.iterator(); it2.hasNext(); ) {
 					String portKey = (String) it2.next();
 					if (portName != null && portKey.contentEquals(portName)) {
 						Port port = (Port) ports.get( portKey );
 						List<?> elements = port.getExtensibilityElements();
 						for (Iterator<?> it3 = elements.iterator(); it3.hasNext();){
 							Object element = it3.next();
 							if (element instanceof SOAPAddress) {
 								SOAPAddress address = (SOAPAddress) element;
 								return address.getLocationURI();
 							} else if (element instanceof SOAP12Address) {
 								SOAP12Address address = (SOAP12Address) element;
 								return address.getLocationURI();
 							}
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public static String getActionURL ( Definition wsdlDefinition, String serviceName, String portName, String bindingName, String opName ) {
 		Map<?, ?> services = wsdlDefinition.getServices();
 		Set<?> serviceKeys = services.keySet();
 		for( Iterator<?> it = serviceKeys.iterator(); it.hasNext(); ) {
 			QName serviceKey = (QName) it.next();
 			if (serviceName != null && serviceKey.getLocalPart().contentEquals(serviceName)) {
 				Service service = (Service) services.get( serviceKey );
 				Map<?, ?> ports = service.getPorts();
 				Set<?> portKeys = ports.keySet();
 				for( Iterator<?> it2 = portKeys.iterator(); it2.hasNext(); ) {
 					String portKey = (String) it2.next();
 					if (portName != null && portKey.contentEquals(portName)) {
 						Port port = (Port) ports.get( portKey );
 						Binding wsdlBinding = port.getBinding();
 						List<?> operations = wsdlBinding.getBindingOperations();
 						for (Iterator<?> it3 = operations.iterator(); it3.hasNext();){
 							BindingOperation operation = (BindingOperation) it3.next();
 							if (opName != null && operation.getName().contentEquals(opName)) {
 								List<?> attributesList = operation.getExtensibilityElements();
 								for (Iterator<?> it4 = attributesList.iterator(); it4.hasNext();){
 									Object test = it4.next();
 									if (test instanceof SOAPOperation) {
 										SOAPOperation soapOp = (SOAPOperation) test;
 										return soapOp.getSoapActionURI();
 									} else if (test instanceof SOAP12Operation) {
 										SOAP12Operation soapOp = (SOAP12Operation) test;
 										return soapOp.getSoapActionURI();
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	private static org.jdom.Element getNamedSchemaElement (Definition wsdlDefinition, Types types, String elementName) {
 		if (types != null &&types.getExtensibilityElements().size() > 0) {
 			Schema schema = (Schema) types.getExtensibilityElements().get(0);
 			DOMBuilder domBuilder = new DOMBuilder();
 			org.jdom.Element jdomSchemaElement = domBuilder.build(schema.getElement());
 			if (elementName.indexOf(":") > 0 ) { //$NON-NLS-1$
 				elementName = elementName.substring(elementName.indexOf(":") + 1, elementName.length()); //$NON-NLS-1$
 			}
 			List<?> list = jdomSchemaElement.getChildren();
 			if (list.size() > 0) {
 				org.jdom.Element checkForImport = (org.jdom.Element) list.get(0);
 				if (checkForImport.getName().equals(IMPORT_TAG)) {
 					Map<?, ?> imports = schema.getImports();
 					Iterator<?> importIter = imports.values().iterator();
 					while (importIter.hasNext()) {
 						Object obj = importIter.next();
 						Vector<?> schemaImportVector = (Vector<?>) obj;
 						Iterator<?> vectorIter = schemaImportVector.iterator();
 						while (vectorIter.hasNext()) {
 							SchemaImport schemaImport = (SchemaImport) vectorIter.next();
 							org.jdom.Element jdomSchemaImportElement = domBuilder.build(schemaImport.getReferencedSchema().getElement());
 							List<?> innerList = jdomSchemaImportElement.getChildren();
 							for (int i = 0; i < innerList.size(); i++){
 								org.jdom.Element temp = (org.jdom.Element) innerList.get(i);
 								String rootName = null;
 								if (temp.getAttribute(NAME_ATTR) != null) 
 									rootName = temp.getAttribute(NAME_ATTR).getValue();
 								String tempName = temp.getNamespacePrefix() + ":" + rootName; //$NON-NLS-1$
 								if (rootName.equalsIgnoreCase(elementName)) {
 									return temp;
 								} else if (tempName.equalsIgnoreCase(elementName) ) {
 									return temp;
 								}
 							}
 						}
 					}
 				} else {
 					for (int i = 0; i < list.size(); i++){
 						org.jdom.Element temp = (org.jdom.Element) list.get(i);
 						String rootName = null;
 						if (temp.getAttribute(NAME_ATTR) != null) 
 							rootName = temp.getAttribute(NAME_ATTR).getValue();
 						String tempName = temp.getNamespacePrefix() + ":" + rootName; //$NON-NLS-1$
 						if (rootName.equalsIgnoreCase(elementName)) {
 							return temp;
 						} else if (tempName.equalsIgnoreCase(elementName) ) {
 							return temp;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	private static org.jdom.Element getNamedSchemaElement ( Definition wsdlDefinition, String messageName ) {
 		Types types = wsdlDefinition.getTypes();
 		
 		if (types == null) {
 			Map<?, ?> imports = wsdlDefinition.getImports();
 			Set<?> importKeys = imports.keySet();
 			for( Iterator<?> it2 = importKeys.iterator(); it2.hasNext(); ) {
 				String importKey = (String) it2.next();
 				Vector<?> importVector = (Vector<?>) imports.get(importKey); 
 				Iterator<?> iter = importVector.iterator();
 				while (iter.hasNext()) {
 					Import importInstance = (Import) iter.next();
 					if (importInstance.getDefinition().getTypes() != null) {
 						types = importInstance.getDefinition().getTypes();
 						org.jdom.Element attempt = getNamedSchemaElement(wsdlDefinition, types, messageName);
 						if (attempt != null)
 							return attempt;
 					} else if (importInstance.getDefinition().getImports() != null) {
 						org.jdom.Element attempt = getNamedSchemaElement(importInstance.getDefinition(), messageName);
 						if (attempt != null)
 							return attempt;
 					}
 				}
 			}
 		} else {
 			org.jdom.Element attempt = getNamedSchemaElement(wsdlDefinition, types, messageName);
 			if (attempt != null)
 				return attempt;
 		}
 		return null;
 	}
 	
	private static String createMessageForSchemaElementFromTypes ( Definition wsdlDefinition, Types types, String messageName ) {
 		if (types != null &&types.getExtensibilityElements().size() > 0) {
 			Schema schema = (Schema) types.getExtensibilityElements().get(0);
 			DOMBuilder domBuilder = new DOMBuilder();
 			org.jdom.Element jdomSchemaElement = domBuilder.build(schema.getElement());
 
 			List<?> list = jdomSchemaElement.getChildren();
 			if (list.size() > 0) {
 				org.jdom.Element checkForImport = (org.jdom.Element) list.get(0);
 				if (checkForImport.getName().equals(IMPORT_TAG)) {
 					Map<?, ?> imports = schema.getImports();
 					Iterator<?> importIter = imports.values().iterator();
 					while (importIter.hasNext()) {
 						Object obj = importIter.next();
 						Vector<?> schemaImportVector = (Vector<?>) obj;
 						Iterator<?> vectorIter = schemaImportVector.iterator();
 						while (vectorIter.hasNext()) {
 							SchemaImport schemaImport = (SchemaImport) vectorIter.next();
 							org.jdom.Element jdomSchemaImportElement = domBuilder.build(schemaImport.getReferencedSchema().getElement());
 							List<?> innerList = jdomSchemaImportElement.getChildren();
 							
 							for (int i = 0; i < innerList.size(); i++){
 								org.jdom.Element temp = (org.jdom.Element) innerList.get(i);
 								String rootName = null;
 								if (temp.getAttribute(NAME_ATTR) != null) 
 									rootName = temp.getAttribute(NAME_ATTR).getValue();
 								
 								if (rootName != null && rootName.equalsIgnoreCase(messageName)) {
 									StringBuffer buf = new StringBuffer();
 									buf.append('<' + rootName);
									buf.append(" xmlns = \"" + wsdlDefinition.getTargetNamespace() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
 									buf.append(">\n"); //$NON-NLS-1$
 									if (!temp.getChildren().isEmpty()){
 										org.jdom.Element temp2 = (org.jdom.Element)temp.getChildren().get(0);
 										if (temp2.getName().contains(COMPLEX_TYPE_NAME)) {
 											String elementStr = processComplexType(wsdlDefinition, temp2);
 											buf.append(elementStr);
 										} else if (temp2.getName().contains(RESTRICTION_NAME)){
 											String elementStr = processType(wsdlDefinition, temp2, RESTRICTION_NAME, false);
 											buf.append(elementStr);
 										} else {
 											String elementStr = processChild(wsdlDefinition, temp2);
 											buf.append(elementStr);
 										}
 									}
 									buf.append("</" + rootName + ">\n");//$NON-NLS-1$//$NON-NLS-2$
 									return buf.toString();
 								}
 							}
 						}
 					}
 				} else {
 					for (int i = 0; i < list.size(); i++){
 						org.jdom.Element temp = (org.jdom.Element) list.get(i);
 						String rootName = null;
 						if (temp.getAttribute(NAME_ATTR) != null) 
 							rootName = temp.getAttribute(NAME_ATTR).getValue();
 						
 						if (rootName.equalsIgnoreCase(messageName)) {
 							StringBuffer buf = new StringBuffer();
 							buf.append('<' + rootName);
							buf.append(" xmlns = \"" + wsdlDefinition.getTargetNamespace() + "\""); //$NON-NLS-1$ //$NON-NLS-2$
 							buf.append(">\n"); //$NON-NLS-1$
 							if (!temp.getChildren().isEmpty()){
 								org.jdom.Element temp2 = (org.jdom.Element)temp.getChildren().get(0);
 								if (temp2.getName().contains(COMPLEX_TYPE_NAME)) {
 									String elementStr = processComplexType(wsdlDefinition, temp2);
 									buf.append(elementStr);
 								} else {
 									String elementStr = processChild(wsdlDefinition, temp2);
 									buf.append(elementStr);
 								}
 							}
 							buf.append("</" + rootName + ">\n");//$NON-NLS-1$//$NON-NLS-2$
 							return buf.toString();
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	private static String processComplexType (Definition wsdlDefinition, org.jdom.Element childEl ) {
         StringBuffer buf = new StringBuffer();
 		if (!childEl.getChildren().isEmpty()) {
 			org.jdom.Element temp3 = (org.jdom.Element)childEl.getChildren().get(0);
 			if (temp3.getName().contains(SEQUENCE_NAME)) {
 				for (int j = 0; j < temp3.getChildren().size(); j++) {
 					org.jdom.Element tempEl = (org.jdom.Element) temp3.getChildren().get(j); 
 					String elementStr = processChild(wsdlDefinition, tempEl);
 					buf.append(elementStr);
 				}
 			} else {
 				String elementStr = processChild(wsdlDefinition, temp3);
 				buf.append(elementStr);
 			}
 		}
 		return buf.toString();
 	}
 		
 	private static String processType (Definition wsdlDefinition, org.jdom.Element childEl, String type, boolean isOptional) {
         StringBuffer buf = new StringBuffer();
 		if (type.contains(STRING_TYPE_NAME)) {
 			buf.append("?"); //$NON-NLS-1$
 		} else if (type.contains(INT_TYPE_NAME)) {
 			buf.append("?"); //$NON-NLS-1$
 		} else if (type.contains(SIMPLE_TYPE_NAME)) {
 			buf.append("?"); //$NON-NLS-1$
 //			for (int j = 0; j < childEl.getChildren().size(); j++) {
 //				org.jdom.Element tempEl = (org.jdom.Element) childEl.getChildren().get(j); 
 //				String elementStr = processChild(wsdlDefinition, tempEl);
 //				buf.append(elementStr);
 //			}
 		} else if (type.contains(RESTRICTION_NAME)) {
 			for (int j = 0; j < childEl.getChildren().size(); j++) {
 				org.jdom.Element tempEl = (org.jdom.Element) childEl.getChildren().get(j); 
 				String innerType = tempEl.getName();
 				String elementStr = processType(wsdlDefinition, tempEl, innerType, isOptional );
 				buf.append(elementStr);
 			}
 		} else if (type.contains(ENUMERATION_NAME)) {
 			String enumerationType = null;
 			if (childEl.getAttribute(VALUE_ATTR) != null) {
 				enumerationType = childEl.getAttribute(VALUE_ATTR).getValue();
 				buf.append(enumerationType + " | "); //$NON-NLS-1$
 			}
 		} else {
 			org.jdom.Element typeEl = getNamedSchemaElement(wsdlDefinition, type);
 			if (typeEl != null) {
 				if (typeEl.getName().contains(COMPLEX_TYPE_NAME) || typeEl.getName().contains(SEQUENCE_NAME)) {
 					String elementStr = processComplexType(wsdlDefinition, typeEl);
 					buf.append(elementStr);
 				} else {
 					String elementStr = processChild(wsdlDefinition, typeEl);
 					buf.append(elementStr);
 				}
 			} else {
 				buf.append("?"); //$NON-NLS-1$
 			}
 		}
 		return buf.toString();
 	}
 	
 	private static String processChild ( Definition wsdlDefinition, org.jdom.Element childEl ) {
         StringBuffer buf = new StringBuffer();
 		String innerChildName = null;
 		if (childEl.getAttribute(NAME_ATTR) != null) {
 			innerChildName = childEl.getAttribute(NAME_ATTR).getValue();
 		}
 		String innerChildType = null;
 		if (childEl.getAttribute(TYPE_ATTR) != null) {
 			innerChildType = childEl.getAttribute(TYPE_ATTR).getValue();
 		}
 		String innerMinOccurs = null;
 		Integer innerMinOccursNum = null;
 		if (childEl.getAttribute(MIN_OCCURS_ATTR) != null) {
 			innerMinOccurs = childEl.getAttribute(MIN_OCCURS_ATTR).getValue();
 			innerMinOccursNum = Integer.decode(innerMinOccurs);
 		}
 		if (innerChildName != null && !childEl.getName().contains(SIMPLE_TYPE_NAME)){
 			buf.append('<' + innerChildName);
 			buf.append(">"); //$NON-NLS-1$
 		}
 		
 		if (childEl.getChildren().size() > 0) {
 			if (childEl.getName().contains(SIMPLE_TYPE_NAME)) {
 				String elementStr = processType(wsdlDefinition, childEl, SIMPLE_TYPE_NAME, (innerMinOccursNum == null || innerMinOccursNum.intValue() == 0));
 				buf.append(elementStr);
 			} else {
 				for (int j = 0; j < childEl.getChildren().size(); j++) {
 					org.jdom.Element tempEl = (org.jdom.Element) childEl.getChildren().get(j); 
 					if (tempEl.getName().contains(COMPLEX_TYPE_NAME)) {
 						String elementStr = processComplexType(wsdlDefinition, tempEl);
 						buf.append(elementStr);
 					} else {
 						String elementStr = processChild(wsdlDefinition, tempEl);
 						buf.append(elementStr);
 					}
 				}
 			}
 		} else if ((innerMinOccursNum == null || innerMinOccursNum.intValue() == 0) && (innerChildType != null)) {
 			String elementStr = processType(wsdlDefinition, childEl, innerChildType, true);
 			buf.append(elementStr);
 		} else if (innerChildType != null){
 			String elementStr = processType(wsdlDefinition, childEl, innerChildType, false);
 			buf.append(elementStr);
 		}
 		if (innerChildName != null && !childEl.getName().contains(SIMPLE_TYPE_NAME))
 			buf.append("</" + innerChildName + ">\n");  //$NON-NLS-1$//$NON-NLS-2$
 		return buf.toString();
 	}
 
	public static String createMessageForSchemaElement ( Definition wsdlDefinition, String messageName ) {
 		Types types = wsdlDefinition.getTypes();
 		if (types == null) {
 			Map<?, ?> imports = wsdlDefinition.getImports();
 			Set<?> importKeys = imports.keySet();
 			for( Iterator<?> it2 = importKeys.iterator(); it2.hasNext(); ) {
 				String importKey = (String) it2.next();
 				Vector<?> importVector = (Vector<?>) imports.get(importKey); 
 				Iterator<?> iter = importVector.iterator();
 				while (iter.hasNext()) {
 					Import importInstance = (Import) iter.next();
 					if (importInstance.getDefinition().getTypes() != null) {
 						types = importInstance.getDefinition().getTypes();
						String attempt = createMessageForSchemaElementFromTypes(wsdlDefinition, types, messageName);
 						if (attempt != null)
 							return attempt;
 					} else if (importInstance.getDefinition().getImports() != null) {
						String attempt = createMessageForSchemaElement(importInstance.getDefinition(), messageName);
 						if (attempt != null)
 							return attempt;
 					}
 				}
 			}
 		} else {
			String attempt = createMessageForSchemaElementFromTypes(wsdlDefinition, types, messageName);
 			if (attempt != null)
 				return attempt;
 		}
 		return null;
 	}
 	
 	public static String getFileContents (URL inURL){
 		try {
 			BufferedReader in = new BufferedReader(
 					new InputStreamReader(
 							inURL.openStream()));
 	
 			String inputLine;
 			StringBuffer buffer = new StringBuffer();
 		
 			while ((inputLine = in.readLine()) != null) {
 				buffer.append(inputLine + '\n');
 			}
 			in.close();
 			return buffer.toString();
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
