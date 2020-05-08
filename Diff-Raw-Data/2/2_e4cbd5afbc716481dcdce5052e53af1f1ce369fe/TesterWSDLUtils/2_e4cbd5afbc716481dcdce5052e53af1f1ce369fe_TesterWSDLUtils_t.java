 /******************************************************************************* 
  * Copyright (c) 2011 Red Hat, Inc. 
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
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.wsdl.Binding;
 import javax.wsdl.BindingOperation;
 import javax.wsdl.Definition;
 import javax.wsdl.Operation;
 import javax.wsdl.Port;
 import javax.wsdl.PortType;
 import javax.wsdl.Service;
 import javax.wsdl.WSDLException;
 import javax.wsdl.extensions.ExtensibilityElement;
 import javax.wsdl.extensions.soap.SOAPAddress;
 import javax.wsdl.extensions.soap.SOAPOperation;
 import javax.wsdl.extensions.soap12.SOAP12Address;
 import javax.wsdl.extensions.soap12.SOAP12Operation;
 import javax.wsdl.factory.WSDLFactory;
 import javax.wsdl.xml.WSDLReader;
 import javax.xml.namespace.QName;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.jboss.tools.ws.core.utils.StatusUtils;
 import org.jboss.tools.ws.ui.messages.JBossWSUIMessages;
 
 import com.ibm.wsdl.Constants;
 
 /**
  * Cleaned up and separated WSDL from Schema utilities
  * @author bfitzpat
  *
  */
 public class TesterWSDLUtils {
 	
 	private final static String DEF_FACTORY_PROPERTY_NAME =
 		"javax.wsdl.factory.DefinitionFactory"; //$NON-NLS-1$
 	private final static String PRIVATE_DEF_FACTORY_CLASS =
 		"org.apache.wsif.wsdl.WSIFWSDLFactoryImpl"; //$NON-NLS-1$
 	
 	public final static String SOAP_NS_URI = "http://schemas.xmlsoap.org/soap/envelope/"; //$NON-NLS-1$
 	public final static String SOAP12_ENVELOPE_NS_URI = "http://www.w3.org/2003/05/soap-envelope"; //$NON-NLS-1$
 	public final static String SOAP12_NS_URI = "http://schemas.xmlsoap.org/wsdl/soap12/"; //$NON-NLS-1$
 	public final static String SOAP12_PREFIX = "soap12"; //$NON-NLS-1$
 	public final static String SOAP_PREFIX = "soap"; //$NON-NLS-1$
 
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
 
 	public static IStatus isWSDLAccessible(URL contextURL) {
 		Properties props = System.getProperties();
 		String oldPropValue = props.getProperty(DEF_FACTORY_PROPERTY_NAME);
 
 		props.setProperty(DEF_FACTORY_PROPERTY_NAME, PRIVATE_DEF_FACTORY_CLASS);
 
 		WSDLFactory factory;
 		try {
 			factory = WSDLFactory.newInstance();
 			WSDLReader wsdlReader = factory.newWSDLReader();
 			wsdlReader.setFeature(Constants.FEATURE_VERBOSE, false);
 			wsdlReader.setFeature("javax.wsdl.importDocuments", true); //$NON-NLS-1$
 			String context = null;
 			if (contextURL != null)
 				context = contextURL.toString();
 			wsdlReader.readWSDL(context);
 		} catch (WSDLException e) {
 			if (contextURL.getProtocol().equalsIgnoreCase("https")) { //$NON-NLS-1$
 				return StatusUtils.warningStatus(JBossWSUIMessages.TesterWSDLUtils_WSDL_HTTPS_Secured_Inaccessible);
 			} else {
 				return StatusUtils.errorStatus(JBossWSUIMessages.TesterWSDLUtils_WSDL_Inaccessible, e);
 			}
 		}
 		
 		if (oldPropValue != null) {
 			props.setProperty(DEF_FACTORY_PROPERTY_NAME, oldPropValue);
 		} else {
 			props.remove(DEF_FACTORY_PROPERTY_NAME);
 		}
 		return Status.OK_STATUS;
 	}
 	
 	public static Definition readWSDLURL(URL contextURL) throws WSDLException, NullPointerException {
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
 
 	public static boolean isSOAP12 (Definition wsdlDefinition, String serviceName, String portName) {
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
 						List<?> extElements = port.getExtensibilityElements();
 						for (Iterator<?> it3 = extElements.iterator(); it3.hasNext(); ) {
 							ExtensibilityElement element = (ExtensibilityElement) it3.next();
 							String nsURI = element.getElementType().getNamespaceURI();
 							if (nsURI.contentEquals(SOAP12_NS_URI)) {
 								return true;
 							}
 							return false;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	public static String[] getNSServiceNameAndMessageNameArray (Definition wsdlDefinition, String serviceName, String portName, String bindingName, String opName ) {
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
 //						String ns = portType.getQName().getNamespaceURI();
 						String ns = service.getQName().getNamespaceURI();
 						List<?> operations = portType.getOperations();
 						for (Iterator<?> it3 = operations.iterator(); it3.hasNext();){
 							Operation operation = (Operation) it3.next();
 							if (opName != null && operation.getName().contentEquals(opName)) {
 								return new String[] {ns, serviceName, portName};
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
 	
 	public static boolean isRequestBodySOAP12 ( String body ) {
 		boolean isSOAP12 = false;
		if (body.indexOf(SOAP12_ENVELOPE_NS_URI) > -1){
 			isSOAP12 = true;
 		}
 		return isSOAP12;
 	}
 }
