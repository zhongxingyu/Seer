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
 
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.wsdl.Binding;
 import javax.wsdl.BindingInput;
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
 import javax.wsdl.extensions.UnknownExtensibilityElement;
 import javax.wsdl.extensions.schema.Schema;
 import javax.wsdl.extensions.soap.SOAPHeader;
 import javax.wsdl.factory.WSDLFactory;
 import javax.wsdl.xml.WSDLReader;
 import javax.xml.namespace.QName;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.xsd.XSDDiagnostic;
 import org.eclipse.xsd.XSDDiagnosticSeverity;
 import org.eclipse.xsd.XSDNamedComponent;
 import org.eclipse.xsd.XSDSchema;
 import org.eclipse.xsd.XSDSchemaContent;
 import org.eclipse.xsd.XSDSchemaDirective;
 import org.eclipse.xsd.impl.XSDSchemaImpl;
 import org.eclipse.xsd.util.XSDParser;
 import org.jdom.Element;
 import org.jdom.input.DOMBuilder;
 
 import com.ibm.wsdl.Constants;
 
 /**
  * Some of this code was borrowed from the org.eclipse.wst.ws.explorer Web Services Explorer
  * view from WTP. 
  * @author bfitzpat
  *
  */
 public class SchemaUtils {
 
 	private static final String SIMPLE_TYPE_NAME = "simpleType"; //$NON-NLS-1$
 	private static final String DOUBLE_TYPE_NAME = "double"; //$NON-NLS-1$
 	private static final String INT_TYPE_NAME = "int"; //$NON-NLS-1$
 	private static final String STRING_TYPE_NAME = "string"; //$NON-NLS-1$
 	private static final String BOOLEAN_TYPE_NAME = "boolean"; //$NON-NLS-1$
 	private static final String DECIMAL_TYPE_NAME = "decimal"; //$NON-NLS-1$
 	private static final String PRECISION_DECIMAL_TYPE_NAME = "precisionDecimal"; //$NON-NLS-1$
 	private static final String FLOAT_TYPE_NAME = "float"; //$NON-NLS-1$
 	private static final String DURATION_TYPE_NAME = "duration"; //$NON-NLS-1$
 	private static final String DATETIME_TYPE_NAME = "dateTime"; //$NON-NLS-1$
 	private static final String DATE_TYPE_NAME = "date"; //$NON-NLS-1$
 	private static final String TIME_TYPE_NAME = "time"; //$NON-NLS-1$
 	private static final String GYEARMONTH_TYPE_NAME = "gYearMonth"; //$NON-NLS-1$
 	private static final String GYEAR_TYPE_NAME = "gYear"; //$NON-NLS-1$
 	private static final String GMONTHDAY_TYPE_NAME = "gMonthDay"; //$NON-NLS-1$
 	private static final String GDAY_TYPE_NAME = "gDay"; //$NON-NLS-1$
 	private static final String GMONTH_TYPE_NAME = "gMonth"; //$NON-NLS-1$
 	private static final String HEXBINARY_TYPE_NAME = "hexBinary"; //$NON-NLS-1$
 	private static final String BASE64BINARY_TYPE_NAME = "base64Binary"; //$NON-NLS-1$
 	private static final String ANYURI_TYPE_NAME = "anyURI"; //$NON-NLS-1$
 	private static final String NOTATION_TYPE_NAME = "NOTATION"; //$NON-NLS-1$
 
 	private static Vector<XSDSchema> schemaList_;
 	private static Vector<QName> w3SchemaQNameList_;
 	private static Vector<XSDSchema> constantSchemaList_;
 	private static HashMap<String, String> namespacesAndPrefixes_;
 	private static String wsdlUrl_;
 	private static Vector<String> schemaURI_;
 	private static Definition definition_;
 	private static boolean rootIsQualified_ = false;
 	private static String rootURI_ = null;
 
 	private final static String DEF_FACTORY_PROPERTY_NAME =
 			"javax.wsdl.factory.DefinitionFactory"; //$NON-NLS-1$
 	private final static String PRIVATE_DEF_FACTORY_CLASS =
 			"org.apache.wsif.wsdl.WSIFWSDLFactoryImpl"; //$NON-NLS-1$
 
 	public final static String SOAP_NS_URI = "http://schemas.xmlsoap.org/soap/envelope/"; //$NON-NLS-1$
 	public final static String SOAP12_ENVELOPE_NS_URI = "http://www.w3.org/2003/05/soap-envelope"; //$NON-NLS-1$
 	public final static String SOAP12_NS_URI = "http://schemas.xmlsoap.org/wsdl/soap12/"; //$NON-NLS-1$
 	public final static String SOAP12_PREFIX = "soap12"; //$NON-NLS-1$
 	public final static String SOAP_PREFIX = "soap"; //$NON-NLS-1$
 
 	static
 	{
 		// w3 schema URI list:
 		// http://www.w3.org/2001/XMLSchema
 		// http://www.w3.org/2000/10/XMLSchema
 		// http://www.w3.org/1999/XMLSchema
 		w3SchemaQNameList_ = new Vector<QName>();
 		w3SchemaQNameList_.addElement(new QName(FragmentConstants.NS_URI_CURRENT_SCHEMA_XSD,"schema")); //$NON-NLS-1$
 		w3SchemaQNameList_.addElement(new QName(FragmentConstants.NS_URI_2000_SCHEMA_XSD,"schema")); //$NON-NLS-1$
 		w3SchemaQNameList_.addElement(new QName(FragmentConstants.NS_URI_1999_SCHEMA_XSD,"schema")); //$NON-NLS-1$
 
 		// Constant schema URI list:
 		// http://www.w3.org/2001/XMLSchema
 		// http://www.w3.org/2000/10/XMLSchema
 		// http://www.w3.org/1999/XMLSchema
 		// http://schemas.xmlsoap.org/soap/encoding/
 		// http://schemas.xmlsoap.org/wsdl/
 		constantSchemaList_ = new Vector<XSDSchema>();
 		constantSchemaList_.addElement(XSDSchemaImpl.getSchemaForSchema(FragmentConstants.NS_URI_CURRENT_SCHEMA_XSD));
 		constantSchemaList_.addElement(XSDSchemaImpl.getSchemaForSchema(FragmentConstants.NS_URI_2000_SCHEMA_XSD));
 		constantSchemaList_.addElement(XSDSchemaImpl.getSchemaForSchema(FragmentConstants.NS_URI_1999_SCHEMA_XSD));
 		constantSchemaList_.addElement(XSDSchemaImpl.getSchemaForSchema(FragmentConstants.NS_URI_SOAP_ENC));
 		constantSchemaList_.addElement(XSDSchemaImpl.getSchemaForSchema(FragmentConstants.URI_WSDL));
 		
 		namespacesAndPrefixes_ = new HashMap<String, String>();
 	}
 
 	public static Vector<String> loadWSDL(Definition wsdlDefinition ) throws WSDLException
 	{
 
 		Vector<String> errorMessages = new Vector<String>();
 		definition_ = wsdlDefinition;
 		schemaList_ = new Vector<XSDSchema>();
 
 		if (definition_ != null)
 		{
 			wsdlUrl_ = wsdlDefinition.getDocumentBaseURI();
 			gatherSchemas(definition_, wsdlUrl_);
 			// Validate the schemas.
 			for (int i=0;i<schemaList_.size();i++)
 			{
 				XSDSchema xsdSchema = (XSDSchema)schemaList_.elementAt(i);
 				//		        xsdSchema.clearDiagnostics();
 				//		        xsdSchema.validate();
 				if (xsdSchema == null) continue;
 				EList<XSDDiagnostic> errors = xsdSchema.getAllDiagnostics();
 				if (!errors.isEmpty())
 				{
 					for (ListIterator<XSDDiagnostic> li = errors.listIterator();li.hasNext();)
 					{
 						XSDDiagnostic xd = (XSDDiagnostic)li.next();
 						String msg = xd.getMessage();
 						// do not report low severity diagnostics or erroneous array reference errors.
 						if (xd.getSeverity().getValue() == XSDDiagnosticSeverity.FATAL_LITERAL.getValue() || (msg != null && msg.length() > 0 && msg.toLowerCase().indexOf("#array") != -1)) //$NON-NLS-1$
 							li.remove();
 						else
 						{
 							if (msg != null && msg.length() > 0)
 								errorMessages.addElement(xd.getMessage());
 						}
 					}
 				}
 			}
 
 			for (int i=0;i<constantSchemaList_.size();i++)
 				schemaList_.addElement(constantSchemaList_.elementAt(i));
 
 		}
 		return errorMessages;
 	}
 
 	public static String getSampleSOAPInputMessage ( Definition wsdlDefinition, String serviceName, String portName, String bindingName, String opName ) {
 
 		try {
 			loadWSDL (wsdlDefinition);
 		} catch (WSDLException e) {
 			e.printStackTrace();
 		}
 		namespacesAndPrefixes_ = new HashMap<String, String>();
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
 								StringBuffer buf = new StringBuffer();
 								for( Iterator<?> it4 = parts.iterator(); it4.hasNext(); ) {
 									Part part = (Part) it4.next();
 									if (part.getName().equalsIgnoreCase("header")) { //$NON-NLS-1$
 										continue;
 									}
 									WSDLPartsToXSDTypeMapper mapper = new WSDLPartsToXSDTypeMapper();
 									mapper.addSchemas(schemaList_);
 
 									buf.append(startProcessingPartXML(wsdlDefinition, part));
 								}
 								return buf.toString();
 							}
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public static Definition readWSDLURL(URL contextURL) throws WSDLException, NullPointerException {
 		Properties props = System.getProperties();
 		String oldPropValue = props.getProperty(DEF_FACTORY_PROPERTY_NAME);
 
 		wsdlUrl_ = contextURL.toExternalForm();
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
 
 	public static String getSampleSOAPMessageHeader ( Definition wsdlDefinition, String serviceName, String portName, String bindingName, String opName ) {
 
 		try {
 			loadWSDL (wsdlDefinition);
 		} catch (WSDLException e) {
 			e.printStackTrace();
 		}
 
 		namespacesAndPrefixes_ = new HashMap<String, String>();
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
 								BindingInput input = operation.getBindingInput();
 								List<?> extensions = input.getExtensibilityElements();
 								for (Iterator<?> extIter = extensions.iterator(); extIter.hasNext();) {
 									Object extension = extIter.next();
 									if (extension instanceof SOAPHeader) {
 										SOAPHeader header = (SOAPHeader) extension;
 										String part = header.getPart();
 										StringBuffer buf = new StringBuffer();
 										WSDLPartsToXSDTypeMapper mapper = new WSDLPartsToXSDTypeMapper();
 										mapper.addSchemas(schemaList_);
 										buf.append(startProcessingPartXML(wsdlDefinition, part, header.getMessage().getNamespaceURI()));
 										return buf.toString();
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
 
 	private static XSDSchema getSchemaForNamespace ( String namespaceURI ) {
 		Iterator<XSDSchema> iter = schemaList_.iterator();
 		while (iter.hasNext()) {
 			XSDSchema schema = (XSDSchema)iter.next();
 			if (schema != null && schema.getTargetNamespace() != null && schema.getTargetNamespace().equals(namespaceURI)) {
 				return schema;
 			}
 		}
 		return null;
 	}
 
 	private static String getURIForNamespacePrefix ( String nsPrefix ) {
 		Iterator<XSDSchema> iter = schemaList_.iterator();
 		while (iter.hasNext()) {
 			XSDSchema schema = (XSDSchema)iter.next();
 			String nsURI =
 					schema.getQNamePrefixToNamespaceMap().get(nsPrefix);
 			if (nsURI != null) {
 				return nsURI;
 			}
 		}
 		return null;
 	}
 
 	private final static void gatherSchemas(Definition definition, String definitionURL)
 	{
 		Types types = definition.getTypes();
 		if (types != null)
 		{
 			List<?> extTypes = types.getExtensibilityElements();
 			if (extTypes != null)
 			{
 				//				org.eclipse.emf.common.util.URI uri = org.eclipse.emf.common.util.URI.createURI(definitionURL);    		
 				//				Resource resource = resourceSet.createResource(uri);
 				for (int i=0;i<extTypes.size();i++)
 				{
 					XSDSchema xsdSchema = null;
 					Object obj = extTypes.get(i);
 					if (obj instanceof Schema) {
 						Schema schemaElement = (Schema) obj;
 						if (isW3SchemaElementType(schemaElement.getElementType()))
 						{
 							xsdSchema = XSDSchemaImpl.createSchema(schemaElement.getElement());
 							if (xsdSchema != null) {
 								xsdSchema.update();
 								xsdSchema.updateElement();
 								if(!checkSchemaURI(definitionURL)){
 									schemaList_.addElement(xsdSchema);
 									gatherSchemaDirective(xsdSchema, definitionURL);
 								}
 							}
 						}
 					}
 					else if (obj instanceof UnknownExtensibilityElement)
 					{
 						UnknownExtensibilityElement schemaElement = (UnknownExtensibilityElement)obj;
 						if (isW3SchemaElementType(schemaElement.getElementType()))
 						{
 							xsdSchema = XSDSchemaImpl.createSchema(schemaElement.getElement());
 							if(!checkSchemaURI(definitionURL)){
 								schemaList_.addElement(xsdSchema);
 								gatherSchemaDirective(xsdSchema, definitionURL);
 							}
 							//Add the Schema to the resource
 							//	        	  boolean success = resource.getContents().add(xsdSchema);
 							//	        	  System.out.println(success + "Added schema " + xsdSchema.getTargetNamespace() + " to wsdl resource " + uri.toString());
 						}
 					} 	
 				}
 			}
 		}
 		Map<?, ?> imports = definition.getImports();
 		if (imports != null)
 			gatherImportedSchemas(definition,imports);
 	}
 
 	private final static boolean isW3SchemaElementType(QName qname)
 	{
 		for (int i=0;i<w3SchemaQNameList_.size();i++)
 		{
 			QName w3SchemaQName = (QName)w3SchemaQNameList_.elementAt(i);
 			if (w3SchemaQName.equals(qname))
 				return true;
 		}
 		return false;
 	}
 
 	private static boolean checkSchemaURI(String schemaURI){
 		boolean found = false;
 
 		if (schemaURI != null) {
 			schemaURI = normalize(schemaURI); 
 			if(schemaURI != null) {
 				if (schemaURI.equals(normalize(wsdlUrl_))) return false;
 				if (schemaURI_ == null) return false;
 				Enumeration<String> e = schemaURI_.elements();
 				while(e.hasMoreElements()){
 					String uri = (String)e.nextElement();	
 					if(schemaURI.equals(uri)){ 
 						found = true;
 						break;
 					}	
 				}
 		
 				if (!found){
 					schemaURI_.addElement(schemaURI);
 				}
 			}
 		}
 		return found;
 
 	}
 
 	private static String normalize(String uri )
 	{
 		try {
 			String encodedURI = URIEncoder.encode(uri,"UTF-8"); //$NON-NLS-1$
 			URI normalizedURI = new URI(encodedURI);
 			normalizedURI = normalizedURI.normalize();
 			return normalizedURI.toString();
 		} catch (URISyntaxException e) {
 			return uri;
 		} catch (UnsupportedEncodingException e) {
 			return uri;
 		}
 	}
 
 	private final static void gatherSchemaDirective(XSDSchema xsdSchema, String xsdSchemaURL)
 	{
 		if (xsdSchema != null)
 		{
 			EList<XSDSchemaContent> xsdSchemaContents = xsdSchema.getContents();
 			for (Iterator<XSDSchemaContent> it = xsdSchemaContents.iterator(); it.hasNext();)
 			{
 				Object content = it.next();
 				if (content instanceof XSDSchemaDirective)
 				{
 					XSDSchemaDirective xsdSchemaDirective = (XSDSchemaDirective)content;
 					StringBuffer xsdSchemaDirectiveURL = new StringBuffer();
 					String xsdSchemaDirectiveLocation = xsdSchemaDirective.getSchemaLocation();
 					if (xsdSchemaDirectiveLocation != null && xsdSchemaDirectiveLocation.indexOf(':') == -1 && xsdSchemaURL != null && xsdSchemaURL.indexOf(':') != -1)
 					{
 						// relative URL
 						int index = xsdSchemaURL.lastIndexOf('/');
 						if (index != -1)
 							xsdSchemaDirectiveURL.append(xsdSchemaURL.substring(0, index+1));
 						else
 						{
 							xsdSchemaDirectiveURL.append(xsdSchemaURL);
 							xsdSchemaDirectiveURL.append('/');
 						}
 					}
 					xsdSchemaDirectiveURL.append(xsdSchemaDirectiveLocation);
 
 					//encode the URL so that Schemas with non-ASCII filenames can be resolved
 					String xsdSchemaDirectiveURLString = URLUtils.encodeURLString(xsdSchemaDirectiveURL.toString());      
 					// resolve schema directive
 					XSDSchema resolvedSchema = xsdSchemaDirective.getResolvedSchema();
 					if (resolvedSchema == null && xsdSchemaDirectiveURLString.length() > 0)
 						resolvedSchema = getSchema(xsdSchemaDirectiveURLString);
 					if (resolvedSchema != null)
 					{
 						if(!checkSchemaURI(xsdSchemaDirectiveURLString)){
 							schemaList_.addElement(resolvedSchema);
 							gatherSchemaDirective(resolvedSchema, xsdSchemaDirectiveURLString);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private final static XSDSchema getSchema(String locURI)
 	{
 		locURI = URLUtils.encodeURLString(locURI);
 		XSDSchema xsdSchema = XSDSchemaImpl.getSchemaForSchema(locURI);
 		if (xsdSchema == null)
 		{
 			@SuppressWarnings("deprecation")
 			XSDParser p = new XSDParser();
 			InputStream is = NetUtils.getURLInputStream(locURI);
 			if (is != null)
 			{
 				p.parse(is);
 				xsdSchema = p.getSchema();
 			}
 		}
 		return xsdSchema;
 	}
 
 	private final static void gatherImportedSchemas(Definition definition,Map<?, ?> imports)
 	{
 		for (Iterator<?> iterator = imports.keySet().iterator();iterator.hasNext();)
 		{
 			List<?> importList = (List<?>)imports.get(iterator.next());
 			for (int i=0;i<importList.size();i++)
 			{
 				Import imp = (Import)importList.get(i);
 				StringBuffer locURI = new StringBuffer(imp.getLocationURI());
 				if (!Validator.validateURL(locURI.toString()))
 				{
 					String base = definition.getDocumentBaseURI();
 					locURI.insert(0,base.substring(0,base.lastIndexOf('/')+1));
 				}
 				try
 				{
 					URL tryURL = new URL ( locURI.toString());
 					Definition importDef = readWSDLURL( tryURL );
 					gatherSchemas(importDef, locURI.toString());
 				}
 				catch (WSDLException e)
 				{
 					// May be an XSD file.
 					gatherSchema(locURI.toString());
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private final static void gatherSchema(String locURI)
 	{
 		XSDSchema xsdSchema = getSchema(locURI);
 		if (xsdSchema != null)
 		{
 			schemaList_.addElement(xsdSchema);
 			gatherSchemaDirective(xsdSchema, locURI);
 		}
 	}
 
 	private static XSDSchema findFirstSchemaWithPartNameAsChild ( String partName ) {
 		for (Iterator<XSDSchema> schemaIter = schemaList_.iterator(); schemaIter.hasNext(); ) {
 			XSDSchema schema = schemaIter.next();
 			DOMBuilder domBuilder = new DOMBuilder();
 			org.jdom.Element jdomSchemaElement = domBuilder.build(schema.getElement());
 			List<?> kids = jdomSchemaElement.getChildren();
 			for (Iterator<?> kidIter = kids.iterator(); kidIter.hasNext(); ) {
 				Object kid = kidIter.next();
 				if (kid instanceof org.jdom.Element) {
 					org.jdom.Element kidElement = (org.jdom.Element) kid;
 					if (kidElement.getAttribute("name") != null) { //$NON-NLS-1$
 						if (kidElement.getAttributeValue("name").equals(partName)) { //$NON-NLS-1$
 							return schema;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	private static org.jdom.Element findFirstElementWithPartNameInSchema( XSDSchema schema, String partName ) {
 		DOMBuilder domBuilder = new DOMBuilder();
 		org.jdom.Element jdomSchemaElement = domBuilder.build(schema.getElement());
 		List<?> kids = jdomSchemaElement.getChildren();
 		for (Iterator<?> kidIter = kids.iterator(); kidIter.hasNext(); ) {
 			Object kid = kidIter.next();
 			if (kid instanceof org.jdom.Element) {
 				org.jdom.Element kidElement = (org.jdom.Element) kid;
 				if (kidElement.getAttribute("name") != null) { //$NON-NLS-1$
 					if (kidElement.getAttributeValue("name").equals(partName)) { //$NON-NLS-1$
 						return kidElement;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	private static String startProcessingPartXML ( Definition wsdlDefinition, String partName, String partUri ) {
 		
 		XSDSchema schema = findFirstSchemaWithPartNameAsChild(partName);
 		DOMBuilder domBuilder = new DOMBuilder();
 		org.jdom.Element jdomSchemaElement = domBuilder.build(schema.getElement());
 		
 		org.jdom.Element jdomElement = findFirstElementWithPartNameInSchema(schema, partName);
 		
 		String typeAttr = jdomElement.getAttributeValue("type"); //$NON-NLS-1$
 		String nsprefix = null;
 		String nstypename = typeAttr;
 		if (typeAttr.indexOf(':') > 0) {
 			nsprefix = typeAttr.substring(0, typeAttr.indexOf(':'));
 			nstypename = typeAttr.substring(typeAttr.indexOf(':') + 1, typeAttr.length());
 		}
 		
 		String uri = schema.getTargetNamespace();
 		if (nsprefix != null && !nsprefix.equalsIgnoreCase("tns")) { //$NON-NLS-1$
 			uri = getNSURI(nsprefix, wsdlDefinition);
 		}
 
 		StringBuffer buf = new StringBuffer();
 
 		if (nsprefix != null && !nsprefix.equalsIgnoreCase("tns")) { //$NON-NLS-1$
 			if (schema.getTargetNamespace().equals(uri)) {
 				// cool, we're good
 				buf.append(createXMLForJDOMElement2( jdomSchemaElement, jdomElement ));
 			} else {
 				XSDSchema importXSD = getSchemaForNamespace(uri);
 				if (importXSD != null) {
 					importXSD.updateElement(true);
 					org.jdom.Element jdomSchemaElement2 = domBuilder.build(importXSD.getElement());
 					Element child = 
 							jdomSchemaElement2.getChild(nstypename);
 					buf.append(createXMLForJDOMElement2( jdomSchemaElement2, child ));
 				}
 			}
 		} else {
 			buf.append(createXMLForJDOMElement2( jdomSchemaElement, jdomElement ));
 		}
 
 		return buf.toString();
 	}
 
 	private static String startProcessingPartXML ( Definition wsdlDefinition, Part part ) {
 		DOMBuilder domBuilder = new DOMBuilder();
 		WSDLPartsToXSDTypeMapper mapper = new WSDLPartsToXSDTypeMapper();
 		mapper.addSchemas(schemaList_);
 		XSDNamedComponent xsdComponent = mapper.getXSDTypeFromSchema(part);
 		if (xsdComponent == null) {
 			return "<error>Generating sample SOAP request</error>"; //$NON-NLS-1$
 		}
 		xsdComponent.updateElement(true);
 		
 		XSDSchema schema = xsdComponent.getSchema();
 		schema.updateElement(true);
 
 		if (!schema.getTargetNamespace().equals(FragmentConstants.NS_URI_CURRENT_SCHEMA_XSD)) {
 			rootIsQualified_ = true;
 			rootURI_ = schema.getTargetNamespace();
 		}
 		
 		org.jdom.Element jdomSchemaElement = domBuilder.build(schema.getElement());
 		org.jdom.Element jdomElement = domBuilder.build(xsdComponent.getElement());
 		
 		String typeAttr = xsdComponent.getElement().getAttribute("type"); //$NON-NLS-1$
 		String nsprefix = null;
 		String nstypename = typeAttr;
 		if (typeAttr.indexOf(':') > 0) {
 			nsprefix = typeAttr.substring(0, typeAttr.indexOf(':'));
 			nstypename = typeAttr.substring(typeAttr.indexOf(':') + 1, typeAttr.length());
 		}
 		
 		String uri = schema.getTargetNamespace();
 		if (nsprefix != null && !nsprefix.equalsIgnoreCase("tns")) { //$NON-NLS-1$
 			uri = getNSURI(nsprefix, wsdlDefinition);
 		}
 
 		StringBuffer buf = new StringBuffer();
 
 		if (nsprefix != null && !nsprefix.equalsIgnoreCase("tns")) { //$NON-NLS-1$
 			if (schema.getTargetNamespace().equals(uri)) {
 				// cool, we're good
 				org.jdom.Element jdomSchemaElement2 = domBuilder.build(xsdComponent.getElement());
 				buf.append(createXMLForJDOMElement2( jdomSchemaElement, jdomSchemaElement2 ));
 			} else {
 				XSDSchema importXSD = getSchemaForNamespace(uri);
 				if (importXSD != null) {
 					importXSD.updateElement(true);
 					org.jdom.Element jdomSchemaElement2 = domBuilder.build(importXSD.getElement());
 					Element child = 
 							jdomSchemaElement2.getChild(nstypename);
 					buf.append(createXMLForJDOMElement2( jdomSchemaElement2, child ));
 				}
 			}
 		} else if (part.getTypeName() != null && isTypeBaseXSDOrSimple(part.getTypeName().getNamespaceURI(), part.getTypeName().getLocalPart())) {
 			buf.append('<' + part.getName() + ">?</" + part.getName() + '>'); //$NON-NLS-1$
 	    } else {
 			buf.append(createXMLForJDOMElement2( jdomSchemaElement, jdomElement ));
 		}
 
 		return buf.toString();
 	}
 	
 	private static org.jdom.Element findJDOMTypeInSchema ( org.jdom.Element schemaElement, String typeName ) {
 		if (schemaElement != null) {
 			String nstypename = null;
 			if (typeName.indexOf(':') > -1) {
 				nstypename = typeName.substring(typeName.indexOf(':') + 1, typeName.length());
 			} else {
 				nstypename = typeName;
 			}
 			List<?> kids = schemaElement.getChildren();
 			for (Iterator<?> kidsIter = kids.iterator(); kidsIter.hasNext(); ) {
 				Object kid = kidsIter.next();
 				if (kid instanceof org.jdom.Element) {
 					org.jdom.Element kidelement = (org.jdom.Element)kid;
 					if (kidelement.getName().equals("complexType")) { //$NON-NLS-1$
 						if (kidelement.getAttributeValue("name").equals(nstypename)) //$NON-NLS-1$
 							return kidelement;
 					}
 					if (kidelement.getName().equals("simpleType")) { //$NON-NLS-1$
 						if (kidelement.getAttributeValue("name").equals(nstypename)) //$NON-NLS-1$
 							return kidelement;
 					}
 					if (kidelement.getName().equals("attribute")) { //$NON-NLS-1$
 						if (kidelement.getAttributeValue("name").equals(nstypename)) //$NON-NLS-1$
 							return kidelement;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	private static org.jdom.Element findJDOMTypeInSchemaByNameAttr ( org.jdom.Element schemaElement, String name ) {
 		if (schemaElement != null) {
 			List<?> kids = schemaElement.getChildren();
 			for (Iterator<?> kidsIter = kids.iterator(); kidsIter.hasNext(); ) {
 				Object kid = kidsIter.next();
 				if (kid instanceof org.jdom.Element) {
 					org.jdom.Element kidelement = (org.jdom.Element)kid;
 					if (kidelement.getName().equals("complexType")) { //$NON-NLS-1$
 						if (kidelement.getAttributeValue("name").equals(name)) //$NON-NLS-1$
 							return kidelement;
 					}
 					if (kidelement.getName().equals("simpleType")) { //$NON-NLS-1$
 						if (kidelement.getAttributeValue("name").equals(name)) //$NON-NLS-1$
 							return kidelement;
 					}
 					if (kidelement.getName().equals("attribute")) { //$NON-NLS-1$
 						if (kidelement.getAttributeValue("name").equals(name)) //$NON-NLS-1$
 							return kidelement;
 					}
 					if (kidelement.getName().equals("element")) { //$NON-NLS-1$
 						if (kidelement.getAttributeValue("name").equals(name)) //$NON-NLS-1$
 							return kidelement;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	private static org.jdom.Element findJDOMElementInSchema ( org.jdom.Element schemaElement, String typeName ) {
 		if (schemaElement != null) {
 			List<?> kids = schemaElement.getChildren();
 			for (Iterator<?> kidsIter = kids.iterator(); kidsIter.hasNext(); ) {
 				Object kid = kidsIter.next();
 				if (kid instanceof org.jdom.Element) {
 					org.jdom.Element kidelement = (org.jdom.Element)kid;
 					if (kidelement.getName().equals("element")) { //$NON-NLS-1$
 						String attrvalue = kidelement.getAttributeValue("type"); //$NON-NLS-1$
 						if (attrvalue != null && attrvalue.equals(typeName)) {
 							kidelement = findJDOMTypeInSchema(schemaElement, typeName);
 							return kidelement;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 	
 	private static org.jdom.Element findJDOMElementInSchemaByName ( org.jdom.Element schemaElement, String typeName ) {
 		if (schemaElement != null) {
 			List<?> kids = schemaElement.getChildren();
 			for (Iterator<?> kidsIter = kids.iterator(); kidsIter.hasNext(); ) {
 				Object kid = kidsIter.next();
 				if (kid instanceof org.jdom.Element) {
 					org.jdom.Element kidelement = (org.jdom.Element)kid;
 					if (kidelement.getName().equals("element")) { //$NON-NLS-1$
 						String attrvalue = kidelement.getAttributeValue("name"); //$NON-NLS-1$
 						if (attrvalue != null && attrvalue.equals(typeName)) {
 							String elemType = kidelement.getAttributeValue("type"); //$NON-NLS-1$
 							if (elemType != null) {
 								String nsprefix = elemType.substring(0, elemType.indexOf(':'));
 								String testUri = getURIForNamespacePrefix(nsprefix);
 								XSDSchema importXSD = getSchemaForNamespace(testUri);
 								if (importXSD != null) {
 									DOMBuilder domBuilder = new DOMBuilder();
 									importXSD.updateElement(true);
 									org.jdom.Element importXSDSchema = domBuilder.build(importXSD.getElement());
 									org.jdom.Element element2 = findJDOMTypeInSchema(importXSDSchema, elemType);
 									if (element2 != null) {
 										return element2;
 									} else {
 										element2 = findJDOMElementInSchema(importXSDSchema, elemType);
 										if (element2 != null) {
 											return element2;
 										}
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
 
 	private static String handleAttributes ( org.jdom.Element element ) {
 		StringBuffer buf = new StringBuffer();
 		List<?> kids = element.getChildren();
 		boolean isQualified = false;
 		for (Iterator<?> kidIter = kids.iterator(); kidIter.hasNext(); ) {
 			Element kid = (Element) kidIter.next();
 			if (kid.getName().equals("attribute") ) {//$NON-NLS-1$
 // 				<xs:attribute ref="ns3:C" use="required" />
 				if (kid.getAttribute("ref") != null) {//$NON-NLS-1$
 					String elemType = kid.getAttributeValue("ref");//$NON-NLS-1$
 					String nsprefix = elemType.substring(0, elemType.indexOf(':'));
 					String typename = elemType.substring(elemType.indexOf(':') + 1, elemType.length());
 					if (elemType.indexOf(':') > -1) {
 						if (!(elemType.startsWith("xs:") || elemType.startsWith("xsd:"))) //$NON-NLS-1$ //$NON-NLS-2$
 							isQualified = true;
 					}
 
 					String testUri = getURIForNamespacePrefix(nsprefix);
 					XSDSchema importXSD = getSchemaForNamespace(testUri);
 					if (importXSD != null) {
 						DOMBuilder domBuilder = new DOMBuilder();
 						importXSD.updateElement(true);
 						org.jdom.Element importXSDSchema = domBuilder.build(importXSD.getElement());
 						org.jdom.Element element2 = findJDOMTypeInSchema(importXSDSchema, typename);
 						if (element2 != null) {
 							element = element2;
 						} else {
 							element2 = findJDOMElementInSchema(importXSDSchema, typename);
 							if (element2 != null) {
 								element = element2;
 							}
 						}
 						if (element != null) {
 							if (isQualified) {
 								String prefix = makePrefixFromURI(testUri);
 								// xmlns:ns1="http://schemas.xmlsoap.org/soap/http"
 								if (!namespacesAndPrefixes_.containsKey(prefix)) {
 									buf.append(" xmlns:" + prefix + "=\"" + testUri + "\"");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
 									namespacesAndPrefixes_.put(prefix, testUri);
 								}
 								buf.append(' ' + prefix + ':');
 							}
 							String name = element.getAttributeValue("name"); //$NON-NLS-1$
 							buf.append(name + "=\"?\" "); //$NON-NLS-1$
 						}
 					}
 				}
 			}
 		}
 		return buf.toString();
 	}
 	
 	private static boolean namespacePrefixListContainsURI ( String uri ) {
 		if (namespacesAndPrefixes_ != null) {
 			if (namespacesAndPrefixes_.containsValue(uri)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private static String makePrefixFromURI ( String uri ) {
 		
 		if (namespacePrefixListContainsURI(uri)) {
 			Iterator<Entry<String, String>> iter = namespacesAndPrefixes_.entrySet().iterator();
 			while (iter.hasNext()) {
 				Map.Entry<String, String> entry = iter.next();
 				if (entry.getValue().equals(uri)) {
 					return entry.getKey();
 				}
 			}
 		}
 		
 		String tnsprefix = uri.substring(uri.indexOf("://") + 3, uri.length()); //$NON-NLS-1$
		if (tnsprefix.contains(":")) {//$NON-NLS-1$
			tnsprefix = uri.substring(0, uri.indexOf(':'));
		}
 		if (tnsprefix.length() > 4) {
 			if (tnsprefix.endsWith("/")) { //$NON-NLS-1$
 				tnsprefix = tnsprefix.substring(0, tnsprefix.length() - 1);
 			}
 			if (tnsprefix.lastIndexOf('/') > -1) {
 				tnsprefix = tnsprefix.substring(tnsprefix.lastIndexOf('/') + 1, tnsprefix.length());
 			}
 			if (tnsprefix.length() > 4) {
 				tnsprefix = tnsprefix.substring(0, 4);
 			}
 			tnsprefix = tnsprefix.toLowerCase();
 			
 			if (namespacesAndPrefixes_.containsKey(tnsprefix)) {
 				int i = 1;
 				String test = tnsprefix + i;
 				while (namespacesAndPrefixes_.containsKey(test)) {
 					i++;
 					test = tnsprefix + i;
 				}
 				tnsprefix = test;
 			}
 		}
 		return tnsprefix;
 	}
 
 	private static boolean elementHasChildren ( org.jdom.Element element ) {
 		boolean rtnflag = false;
 		if (element.getChildren() != null && element.getChildren().size() > 0) {
 			List<?> kids = element.getChildren();
 			for (Iterator<?> kidIter = kids.iterator(); kidIter.hasNext(); ) {
 				Element kid = (Element) kidIter.next();
 				if (kid.getName().equals("element")) { //$NON-NLS-1$
 					rtnflag = true;
 				}
 				boolean kidflag = elementHasChildren(kid);
 				if (kidflag) 
 					rtnflag = true;
 			}
 		}
 		return rtnflag;
 	}
 	
 	private static String createXMLForJDOMElement2 ( org.jdom.Element schemaElement, org.jdom.Element element ) {
 		StringBuffer buf = new StringBuffer();
 		String nstypename = null;
 
 		String tns = null;
 		String tnsprefix = null;
 		if (schemaElement.getNamespace("tns") != null) {//$NON-NLS-1$
 			tns = schemaElement.getNamespace("tns").getURI();//$NON-NLS-1$
 			tnsprefix = makePrefixFromURI(tns);
 		}
 		
 		boolean isQualified = false;
 
 		if (element.getAttribute("name") != null) {//$NON-NLS-1$
 			nstypename = element.getAttributeValue("name");//$NON-NLS-1$
 		}
 		if (element.getAttribute("type") != null) {//$NON-NLS-1$
 			String type2FindName = element.getAttributeValue("type");//$NON-NLS-1$
 			if (type2FindName.indexOf(':') > -1) {
 				if (!(type2FindName.startsWith("xs:") || type2FindName.startsWith("xsd:"))) //$NON-NLS-1$ //$NON-NLS-2$
 					isQualified = true;
 			}
 			org.jdom.Element element2 = findJDOMTypeInSchema(schemaElement, type2FindName);
 			if (element2 != null) {
 				element = element2;
 				if (element.getParentElement() != null) {
 					org.jdom.Element parent = element.getParentElement();
 					if (parent.getNamespace("tns") != null) {//$NON-NLS-1$
 						tns = parent.getNamespace("tns").getURI();//$NON-NLS-1$
 						tnsprefix = makePrefixFromURI(tns);
 					}
 				}
 			}
 		}
 		if (element.getAttribute("ref") != null) {//$NON-NLS-1$
 			String ref2FindName = element.getAttributeValue("ref");//$NON-NLS-1$
 			if (ref2FindName.indexOf(':') > -1) {
 				if (!(ref2FindName.startsWith("xs:") || ref2FindName.startsWith("xsd:"))) //$NON-NLS-1$ //$NON-NLS-2$
 					isQualified = true;
 			}
 			org.jdom.Element element2 = findJDOMTypeInSchema(schemaElement, ref2FindName);
 			if (element2 != null) {
 				element = element2;
 				if (element.getParentElement() != null) {
 					org.jdom.Element parent = element.getParentElement();
 					if (parent.getNamespace("tns") != null) {//$NON-NLS-1$
 						tns = parent.getNamespace("tns").getURI();//$NON-NLS-1$
 						tnsprefix = makePrefixFromURI(tns);
 					}
 				}
 			} else {
 				element2 = findJDOMElementInSchema(schemaElement, ref2FindName);
 				if (element2 != null) {
 					element = element2;
 					if (element.getParentElement() != null) {
 						org.jdom.Element parent = element.getParentElement();
 						if (parent.getNamespace("tns") != null) {//$NON-NLS-1$
 							tns = parent.getNamespace("tns").getURI();//$NON-NLS-1$
 							tnsprefix = makePrefixFromURI(tns);
 						}
 					}
 				}
 			}
 		}
 		Integer minOccurs = 1;
 		String elemType = null;
 		
 		if (element.getAttributes().size() > 0) {
 			if (element.getAttribute("minOccurs") != null) {//$NON-NLS-1$
 				String value = element.getAttributeValue("minOccurs");//$NON-NLS-1$
 				minOccurs = Integer.decode(value);
 			}
 			if (element.getAttribute("type") != null) {//$NON-NLS-1$
 				elemType = element.getAttributeValue("type");//$NON-NLS-1$
 				String nsprefix = ""; //$NON-NLS-1$
 				try {
 					nsprefix = elemType.substring(0, elemType.indexOf(':'));
 				} catch (StringIndexOutOfBoundsException e) {
 					// ignore
 				}
 				String testUri = getURIfromSchemaPrefix(element, nsprefix);
 				if (elemType.indexOf(':') > -1) {
 					if (!(elemType.startsWith("xs:") || elemType.startsWith("xsd:"))){ //$NON-NLS-1$ //$NON-NLS-2$
 						isQualified = true;
 					}
 				}
 				XSDSchema importXSD = getSchemaForNamespace(testUri);
 				if (importXSD != null) {
 					DOMBuilder domBuilder = new DOMBuilder();
 					importXSD.updateElement(true);
 					org.jdom.Element importXSDSchema = domBuilder.build(importXSD.getElement());
 					org.jdom.Element element2 = findJDOMTypeInSchema(importXSDSchema, elemType);
 					if (element2 != null) {
 						element = element2;
 					} else {
 						element2 = findJDOMElementInSchema(importXSDSchema, elemType);
 						if (element2 != null) {
 							element = element2;
 						} else {
 							element2 = findJDOMElementInSchemaByName(schemaElement, nstypename);
 							if (element2 != null) {
 								element = element2;
 							}
 						}
 					}
 				} else if (schemaElement != null) {
 					org.jdom.Element element2 = findJDOMTypeInSchema(schemaElement, elemType);
 					if (element2 != null) {
 						element = element2;
 					} else {
 						element2 = findJDOMElementInSchema(schemaElement, elemType);
 						if (element2 != null) {
 							element = element2;
 						} else {
 							element2 = findJDOMElementInSchemaByName(schemaElement, nstypename);
 							if (element2 != null) {
 								element = element2;
 							}
 						}
 					}
 				}
 			}
 			if (element.getAttribute("ref") != null) {//$NON-NLS-1$
 				elemType = element.getAttributeValue("ref");//$NON-NLS-1$
 				String nsprefix = elemType.substring(0, elemType.indexOf(':'));
 				String typename = elemType.substring(elemType.indexOf(':') + 1, elemType.length());
 				nstypename = typename;
 
 				String testUri = getURIfromSchemaPrefix(element, nsprefix);
 				if (elemType.indexOf(':') > -1) {
 					if (!elemType.startsWith("xs:")) { //$NON-NLS-1$
 						isQualified = true;
 						tns = testUri;
 						tnsprefix = makePrefixFromURI(tns);
 					}
 				}
 				XSDSchema importXSD = getSchemaForNamespace(testUri);
 				if (importXSD != null) {
 					DOMBuilder domBuilder = new DOMBuilder();
 					importXSD.updateElement(true);
 					org.jdom.Element importXSDSchema = domBuilder.build(importXSD.getElement());
 					org.jdom.Element element2 = findJDOMTypeInSchema(importXSDSchema, elemType);
 					if (element2 != null) {
 						element = element2;
 					} else {
 						element2 = findJDOMElementInSchema(importXSDSchema, elemType);
 						if (element2 != null) {
 							element = element2;
 						} else {
 							element2 = findJDOMElementInSchemaByName(importXSDSchema, nstypename);
 							if (element2 != null) {
 								element = element2;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		boolean hasKids = false;
 		if (elementHasChildren(element)) {
 			hasKids = true;
 		}
 		
 		boolean isSimpleType = false;
 		if (elemType != null && isTypeBaseXSDOrSimple(tns, elemType)) {
 			isSimpleType = true;
 		}
 		
 		boolean hasEnums = false;
 		if (element.getName().equals("simpleType")) { //$NON-NLS-1$
 			String enums = getEnumerations(element);
 			if (enums != null && enums.trim().length() > 0) {
 				hasEnums = true;
 			}
 		}
 
 		boolean includeTNSInRoot = false;
 		if (rootIsQualified_ && !isQualified) {
 			includeTNSInRoot = true;
 			isQualified = true;
 			tnsprefix = "tns"; //$NON-NLS-1$
 		} else if (isQualified) {
 			rootIsQualified_ = false;
 			if (tnsprefix ==  null) {
 				tnsprefix = "tns"; //$NON-NLS-1$
 			}
 		}
 		
 		boolean isSequence = element.getName().equals("sequence"); //$NON-NLS-1$
 		if (!isSequence) {
 			// open tag
 			if (minOccurs.intValue() == 0) {
 				buf.append("<!-- optional -->\n");//$NON-NLS-1$
 			}
 			buf.append('<');
 			if (isQualified) {
 				buf.append(tnsprefix + ':');
 			}
 			
 			buf.append(nstypename);
 			String attributes = handleAttributes(element);
 			if (attributes != null && attributes.trim().length() > 0) {
 				buf.append(' ');
 				buf.append(attributes);
 			}
 			
 			if (isQualified) {
 				// xmlns:ns1="http://schemas.xmlsoap.org/soap/http"
 				if (includeTNSInRoot && !namespacePrefixListContainsURI(rootURI_)) {
 					buf.append(" xmlns:tns=\"" + rootURI_ + "\""); //$NON-NLS-1$ //$NON-NLS-2$
 					namespacesAndPrefixes_.put(tnsprefix, rootURI_);
 				}
 				else if (!namespacesAndPrefixes_.containsKey(tnsprefix)) {
 					buf.append(" xmlns:" + tnsprefix + "=\"" + tns + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					namespacesAndPrefixes_.put(tnsprefix, tns);
 				}
 			}
 
 			
 			if (hasKids || isSimpleType || hasEnums )
 				buf.append(">"); //$NON-NLS-1$
 			else
 				buf.append("/>");//$NON-NLS-1$
 		}
 
 		if (hasKids) {
 			if (!isSequence) {
 				buf.append('\n');
 			}
 			List<?> kids = element.getChildren();
 			for (Iterator<?> kidIter = kids.iterator(); kidIter.hasNext(); ) {
 				Element kid = (Element) kidIter.next();
 				if (kid.getName().equals("complexType") || kid.getName().equals("sequence") || //$NON-NLS-1$//$NON-NLS-2$
 						kid.getName().equals("simpleType") || kid.getName().equals("restriction")) { //$NON-NLS-1$ //$NON-NLS-2$
 					List<?> innerkids = kid.getChildren();
 					for (Iterator<?> kidIter2 = innerkids.iterator(); kidIter2.hasNext(); ) {
 						Element innerkid = (Element) kidIter2.next();
 						String out = createXMLForJDOMElement2 ( schemaElement, innerkid );
 						buf.append(out);
 					}
 				} else if (kid.getName().equals("element")) {//$NON-NLS-1$
 					String out = createXMLForJDOMElement2 (schemaElement, kid);
 					buf.append(out);
 				}
 			}
 		}
 
 		if (!isSequence && ( hasKids || isSimpleType || hasEnums )) {
 			// add ? for value 
 			if (!hasKids || isSimpleType || hasEnums )
 				buf.append('?');
 			
 			// close tag
 			buf.append("</");//$NON-NLS-1$
 			
 			if (isQualified) {
 				buf.append(tnsprefix + ':');
 			}
 			buf.append(nstypename);
 			buf.append(">\n"); //$NON-NLS-1$
 		}
 		
 		return buf.toString();
 	}
 	
 	private static String getEnumerations ( org.jdom.Element element ) {
 		StringBuffer buf = new StringBuffer();
 		List<?> kids = element.getChildren();
 		for (Iterator<?> kidIter = kids.iterator(); kidIter.hasNext(); ) {
 			Element kid = (Element) kidIter.next();
 			if (kid.getChildren() != null && kid.getChildren().size() > 0) {
 				buf.append(getEnumerations(kid));
 			}
 			if (kid.getName().equals("enumeration")) { //$NON-NLS-1$
 				String value = kid.getAttributeValue("value"); //$NON-NLS-1$
 				buf.append(value + '|');
 			}
 		}
 		return buf.toString();
 	}
 	
 	private static HashMap<String, String> getDefinitionNamespaces ( Definition wsdlDefinition) {
 		HashMap<String, String> namespaceMap = new HashMap<String, String>();
 		namespaceMap.put(SOAP_NS_URI, SOAP_PREFIX);
 		namespaceMap.put(SOAP12_ENVELOPE_NS_URI, SOAP12_PREFIX);
 		namespaceMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");  //$NON-NLS-1$//$NON-NLS-2$
 		namespaceMap.put("http://www.w3.org/2001/XMLSchema", "xsd");  //$NON-NLS-1$//$NON-NLS-2$
 
 		Map<?,?> namespaces = wsdlDefinition.getNamespaces();
 		Set<?> namespaceKeys = namespaces.keySet();
 		int i = 1;
 		for ( Iterator<?> it = namespaceKeys.iterator(); it.hasNext();  ) {
 			String prefix = (String) it.next();
 			String url = wsdlDefinition.getNamespace(prefix);
 			if (!namespaceMap.containsKey(url)) {
 				String newprefix = "ns" + i++; //$NON-NLS-1$
 				namespaceMap.put(url, newprefix);
 			}
 		}
 		return namespaceMap;
 	}
 
 	private static String getNSURI ( String inPrefix, Definition wsdlDefinition) {
 		HashMap<String, String> map = getDefinitionNamespaces(wsdlDefinition);
 		if (map.containsValue(inPrefix)) {
 			for (Iterator<Entry<String, String>> mapIter = map.entrySet().iterator(); mapIter.hasNext(); ) {
 				Map.Entry<String, String> test = (Entry<String, String>) mapIter.next();
 				if (test.getValue().equals(inPrefix)) 
 					return test.getKey();
 			}
 		}
 		return null;
 	}
 
 
 
 	private static String getURIfromSchemaPrefix(Element el, String prefix) {
 		Element e = el;
 		while (e.getParentElement() != null) {
 			e = e.getParentElement();
 		}
 		if (e.getNamespace(prefix) != null) {
 			return e.getNamespace(prefix).getURI();
 		}
 		return null;
 	}
 
 	private static boolean isTypeBaseXSDOrSimple ( String uri, String type ) {
 		
 		if (uri != null) {
 			XSDSchema schema = getSchemaForNamespace(uri);
 			DOMBuilder domBuilder = new DOMBuilder();
 			schema.updateElement(true);
 			org.jdom.Element importXSDSchema = domBuilder.build(schema.getElement());
 			if (type != null && type.indexOf(':') > -1) {
 				String typename = type.substring(type.indexOf(':') + 1, type.length());
 				org.jdom.Element element = findJDOMTypeInSchemaByNameAttr(importXSDSchema, typename);
 				if (element != null) {
 					if (element.getAttribute("type") != null) { //$NON-NLS-1$
 						type = element.getAttributeValue("type"); //$NON-NLS-1$
 					}
 				}
 			}
 		}
 		// typically the type starts with a namespace prefix, so
 		// we want to compare the end of the string with the actual
 		// type name
 		if (type != null && type.endsWith(STRING_TYPE_NAME) || 
 				type.endsWith(BOOLEAN_TYPE_NAME) || 
 				type.endsWith(DECIMAL_TYPE_NAME) ||
 				type.endsWith(INT_TYPE_NAME) || 
 				type.endsWith(DOUBLE_TYPE_NAME) || 
 				type.endsWith(FLOAT_TYPE_NAME) ||
 				type.endsWith(DURATION_TYPE_NAME) || 
 				type.endsWith(DATE_TYPE_NAME) || 
 				type.endsWith(TIME_TYPE_NAME) ||
 				type.endsWith(DATETIME_TYPE_NAME) ||
 				type.endsWith(ANYURI_TYPE_NAME) ||
 				type.endsWith(BASE64BINARY_TYPE_NAME) ||
 				type.endsWith(GDAY_TYPE_NAME) ||
 				type.endsWith(GMONTH_TYPE_NAME) ||
 				type.endsWith(GMONTHDAY_TYPE_NAME) ||
 				type.endsWith(GYEAR_TYPE_NAME) ||
 				type.endsWith(GYEARMONTH_TYPE_NAME) ||
 				type.endsWith(HEXBINARY_TYPE_NAME) ||
 				type.endsWith(NOTATION_TYPE_NAME) ||
 				type.endsWith(PRECISION_DECIMAL_TYPE_NAME)
 				) {
 			return true;
 		} else if (type != null && type.contains(SIMPLE_TYPE_NAME)) {
 			return true;
 		}
 		return false;
 	}
 }
