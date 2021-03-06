 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.bpel.model.resource;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.wsdl.WSDLException;
 import javax.wsdl.extensions.ExtensibilityElement;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 
 import org.apache.xerces.parsers.DOMParser;
 import org.eclipse.bpel.model.*;
 import org.eclipse.bpel.model.Process;
 import org.eclipse.bpel.model.extensions.BPELActivityDeserializer;
 import org.eclipse.bpel.model.extensions.BPELExtensionDeserializer;
 import org.eclipse.bpel.model.extensions.BPELExtensionRegistry;
 import org.eclipse.bpel.model.extensions.BPELUnknownExtensionDeserializer;
 import org.eclipse.bpel.model.extensions.ServiceReferenceDeserializer;
 import org.eclipse.bpel.model.impl.OnEventImpl;
 import org.eclipse.bpel.model.impl.OnMessageImpl;
 import org.eclipse.bpel.model.impl.PartnerActivityImpl;
 import org.eclipse.bpel.model.impl.ToImpl;
 import org.eclipse.bpel.model.messageproperties.Property;
 import org.eclipse.bpel.model.messageproperties.util.MessagepropertiesConstants;
 import org.eclipse.bpel.model.proxy.CorrelationSetProxy;
 import org.eclipse.bpel.model.proxy.LinkProxy;
 import org.eclipse.bpel.model.proxy.MessageProxy;
 import org.eclipse.bpel.model.proxy.PartnerLinkProxy;
 import org.eclipse.bpel.model.proxy.PartnerLinkTypeProxy;
 import org.eclipse.bpel.model.proxy.PropertyProxy;
 import org.eclipse.bpel.model.proxy.RoleProxy;
 import org.eclipse.bpel.model.proxy.VariableProxy;
 import org.eclipse.bpel.model.proxy.XSDElementDeclarationProxy;
 import org.eclipse.bpel.model.proxy.XSDTypeDefinitionProxy;
 import org.eclipse.bpel.model.util.BPELConstants;
 import org.eclipse.bpel.model.util.BPELUtils;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.wst.wsdl.Message;
 import org.eclipse.wst.wsdl.PortType;
 import org.eclipse.xsd.XSDElementDeclaration;
 import org.eclipse.xsd.XSDTypeDefinition;
 import org.eclipse.xsd.util.XSDConstants;
 import org.w3c.dom.Attr;
 import org.w3c.dom.CDATASection;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
  * BPELReader is invoked from BPELResourceImpl to parse the BPEL file and
  * create a Process object.
  */
 
 @SuppressWarnings("nls")
 
 public class BPELReader {
 
 	// The process we are reading
 	private Process process = null;
 	// The resource we are reading from
 	private BPELResource resource = null;
 	// The document builder controls various DOM characteristics
 	private DocumentBuilder docBuilder = null;
 	// Registry for extensibility element serializers and deserializers
 	private BPELExtensionRegistry extensionRegistry = BPELExtensionRegistry.getInstance();
 	
 	private DOMParser domParser;
 	
 	// The WS-BPEL Specification says how to resolve variables, taking into
 	// account scopes, etc. Technically, no one should override this behavior,
 	// but replacing this field with another implementation could allow
 	// you to optimize the search or provide different behavior.
 	public static VariableResolver VARIABLE_RESOLVER = new BPELVariableResolver();
 	// The WS-BPEL Specification says how to resolve links, taking into
 	// account scopes, etc. Technically, no one should override this behavior,
 	// but replacing this field with another implementation could allow
 	// you to optimize the search or provide different behavior.
 	public static LinkResolver LINK_RESOLVER = new BPELLinkResolver();
 	
 	
 	/**
 	 * Construct a new BPELReader using the given DocumentBuilder to determine
 	 * how the DOM tree is constructed.
 	 * 
 	 * @param builder  the document builder to use when parsing the file
 	 * @throws IOException if no document builder is specified
 	 */
 	public BPELReader (DocumentBuilder builder) throws IOException {
 		if (builder == null) {
 			throw new IOException(BPELPlugin.INSTANCE.getString("%BPELReader.missing_doc_builder"));
 		}
 		this.docBuilder = builder;
 	}
 
 	public BPELReader (DOMParser parser )  {		
 		this.domParser = parser;
 	}
 	
 	/**
 	 * Read from the given input stream into the given resource.
 	 * 
 	 * @param resource  the EMF resource to construct
 	 * @param inputStream  the input stream to read the BPEL from
 	 * @throws IOException if an error occurs during reading
 	 */
 	public void read(BPELResource resource, InputStream inputStream) throws IOException {
 		try {
 			
 			Document doc = null;
 			if (docBuilder != null) {
 				doc = docBuilder.parse(inputStream);
 			} else if (domParser != null) {
 				domParser.parse(new InputSource(inputStream));
 				doc = domParser.getDocument();
 			}
 			
 			// After the document has successfully parsed, it's okay
 			// to assign the resource.
 			this.resource = resource;
 			// Pass 1 and 2 are inside the try so they don't occur if
 			// an error happens during parsing.
 			// In pass 1 we parse and create the structural elements and attributes. 
 			pass1(doc);
 			// In pass 2, we run any postLoadRunnables which need to happen after
 			// pass 1 (for example, establishing object links to variables).
 			pass2();
 		} catch (SAXParseException exc) {
 			// TODO: Error handling
 			exc.printStackTrace();
 		} catch (SAXException se) {
 			// TODO: Error handling
 			se.printStackTrace();
 		} catch (EOFException exc) {
 			// Ignore end of file exception
 		} catch (IOException ioe) {
 			// TODO: Error handling
 			ioe.printStackTrace();
 		} catch (RuntimeException rte) {
 			rte.printStackTrace();			
 		}
 	}
 
 	/**
 	 * In pass 1, we parse and create the structural elements and attributes,
 	 * and add the process to the EMF resource's contents
 	 * @param document  the DOM document to parse
 	 */
 	protected void pass1(Document document) {
 		Process p = xml2Resource(document);
 		if (p != null) {
 			resource.getContents().add(p);
 		}
 	}
 	
 	/**
 	 * In pass 2, we run any post load runnables which were queued during pass 1.
 	 */
 	protected void pass2() {
 		if (process == null) {
 			return ;
 		}
 		for(Runnable r : process.getPostLoadRunnables()) {
 			r.run();
 		}
 		process.getPostLoadRunnables().clear();
 	}
 	
 	/**
      * Returns a list of child nodes of <code>parentElement</code> that are
      * {@link Element}s.
      * Returns an empty list if no elements are found.
      * 
 	 * @param parentElement  the element to find the children of
 	 * @return a node list of the children of parentElement
 	 */
 	protected List<Element> getChildElements(Element parentElement) {
 		List<Element> list = new ArrayList<Element>();
 		NodeList children = parentElement.getChildNodes();		
 		for (int i=0; i < children.getLength(); i++) {
 			if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
 				list.add( (Element) children.item(i));
 		}
 		return list;
 	}
 
     /**
      * Returns a list of child nodes of <code>parentElement</code> that are
      * {@link Element}s with a BPEL namespace that have the given <code>localName</code>.
      * Returns an empty list if no matching elements are found.
      * 
 	 * @param parentElement  the element to find the children of
 	 * @param localName  the localName to match against
 	 * @return a node list of the matching children of parentElement
      */
 	protected List<Element> getBPELChildElementsByLocalName(Element parentElement, String localName) {
 		List<Element> list = new ArrayList<Element>();
 		NodeList children = parentElement.getChildNodes();
 		for (int i = 0; i < children.getLength(); i++) {
 			Node node = children.item(i);
 			if (localName.equals(node.getLocalName()) && BPELUtils.isBPELElement(node)) {
                 list.add((Element) node);
 			}
 		}
 		return list;
 	}
 
     /**
      * Returns the first child node of <code>parentElement</code> that is an {@link Element}
      * with a BPEL namespace and the given <code>localName</code>, or <code>null</code>
      * if a matching element is not found. 
      * 
 	 * @param parentElement  the element to find the children of
 	 * @param localName  the localName to match against
 	 * @return the first matching element, or null if no element was found
       */
 	protected Element getBPELChildElementByLocalName(Element parentElement, String localName) {
 		NodeList children = parentElement.getChildNodes();
 		for (int i = 0; i < children.getLength(); i++) {
 			Node node = children.item(i);
 			if (localName.equals(node.getLocalName()) && BPELUtils.isBPELElement(node)) {
                 return (Element) node;
             }
 		}
 		return null;
 	}
 
     /**
 	 * Walk from the given element up through its parents, looking for any
 	 * xmlns definitions. Collect them all in a map (mapping the prefix to
 	 * the namespace value) and return the map.
 	 * 
 	 * @param element  the element to get the xmlns definitions for
 	 * @return a map of visible xmlns definitions
 	 */
 	protected Map getAllNamespacesForElement(Element element) {
 		Map nsMap = new HashMap();		
 		Node tempNode = element;        
 		while (tempNode != null && tempNode.getNodeType() == Node.ELEMENT_NODE) {
 			NamedNodeMap attrs = ((Element)tempNode).getAttributes();
 			for (int i = 0; i < attrs.getLength(); i++) {
 				Attr attr = (Attr)attrs.item(i);
 				// XML namespace attributes use the reserved namespace "http://www.w3.org/2000/xmlns/".
 				if (XSDConstants.XMLNS_URI_2000.equalsIgnoreCase(attr.getNamespaceURI())) {
 					final String key = BPELUtils.getNSPrefixMapKey(attr.getLocalName());
 					if (!nsMap.containsKey(key)) {
 						nsMap.put(key, attr.getValue());
 					}
 				}
 			}
 			tempNode = tempNode.getParentNode();
 		}
 		return nsMap;
 	}
 	
 	/**
 	 * For all attributes of the given element, ensure that their namespace
 	 * prefixes are in the resource's prefix-to-namespace-map.
 	 * 
 	 * @param eObject
 	 * @param element
 	 */
 	protected void saveNamespacePrefix(EObject eObject, Element element) {
 		Map nsMap = null; // lazy init since it may require a new map
 		NamedNodeMap attrs = element.getAttributes();
 		for (int i=0; i < attrs.getLength(); i++) {
 			Attr attr = (Attr) attrs.item(i);        
 			// XML namespace attributes use the reserved namespace "http://www.w3.org/2000/xmlns/". 
 			if (XSDConstants.XMLNS_URI_2000.equals(attr.getNamespaceURI())) {
 				if (nsMap == null) {
 					nsMap = resource.getPrefixToNamespaceMap(eObject);
 				}
 				nsMap.put(BPELUtils.getNSPrefixMapKey(attr.getLocalName()), attr.getValue());
 			}
 		}
 	}
 
 	/**
 	 * Given a DOM Element, find the child element which is a BPEL activity
 	 * (of some type), parse it, and return the Activity.
 	 * 
 	 * @param element  the element in which to find an activity
 	 * @return the activity, or null if no activity could be found
 	 */
 	protected Activity getChildActivity(Element element) {
 		NodeList activityElements = element.getChildNodes();
 		for (int i = 0; i < activityElements.getLength(); i++) {
 			if (activityElements.item(i).getNodeType() != Node.ELEMENT_NODE) {
 				continue;
 			}
 			           	   	         	
 			Element activityElement = (Element)activityElements.item(i);
 			Activity activity = xml2Activity(activityElement);
 							
 			if (activity != null) {
 				return activity;	
 			}
 		}
 		return null;   	
 	}
 
  
 	/**
 	 * Sets a PartnerLink element for a given EObject. The given activity element
 	 * must contain an attribute named "partnerLink".
 	 * 
 	 * @param activityElement  the DOM element of the activity
 	 * @param eObject  the EObject in which to set the partner link
 	 */
 	protected void setPartnerLink(Element activityElement, final EObject eObject, final EReference reference) {
 		if (!activityElement.hasAttribute("partnerLink")) {
 			return;
 		}
 
 		final String partnerLinkName = activityElement.getAttribute("partnerLink");
 		// We must do this as a post load runnable because the partner link might not
 		// exist yet.
 		process.getPostLoadRunnables().add(new Runnable() {
 			public void run() {	
 				PartnerLink targetPartnerLink = BPELUtils.getPartnerLink(eObject, partnerLinkName);
 				if (targetPartnerLink == null) {
 					targetPartnerLink = new PartnerLinkProxy(resource.getURI(), partnerLinkName);
 				}
 				eObject.eSet(reference, targetPartnerLink);				
 			}
 		});		
 	}
 
 	/**
 	 * Sets a Variable element for a given EObject. The given activity element
 	 * must contain an attribute with the given name
 	 * 
 	 * @param activityElement  the DOM element of the activity
 	 * @param eObject  the EObject in which to set the variable
 	 * @param variableAttrName  the name of the attribute containing the variable name
 	 * @param reference  the EReference which is the variable pointer in EObject 
 	 */
 	protected void setVariable(Element activityElement, final EObject eObject, String variableNameAttr, final EReference reference) {
 		if (!activityElement.hasAttribute(variableNameAttr)) {
 			return;
 		}
 
 		final String variableName = activityElement.getAttribute(variableNameAttr);
 		// We must do this as a post load runnable because the variable might not
 		// exist yet.
 		process.getPostLoadRunnables().add(new Runnable() {
 			public void run() {				
 				Variable targetVariable = getVariable(eObject, variableName);
 				if (targetVariable == null) {
 					targetVariable = new VariableProxy(resource.getURI(), variableName);
 				}		
 				eObject.eSet(reference, targetVariable);				
 			}
 		});
   	} 	
 
 	/**
 	 * Find a Property name in element (in the named attribute) and set it
 	 * into the given EObject. If EObject is a CorrelationSet, add the property
 	 * to the list of properties. If it is a To, set the property.
 	 * 
 	 * @param element  the DOM element containing the property name
 	 * @param eObject  the EObject in which to set the property
 	 * @param propertyName  the name of the attribute containing the property name
 	 */
 	protected void setProperties(Element element, EObject eObject, String propertyName) {
 		String propertyAttribute = element.getAttribute(propertyName);
 		
 		StringTokenizer st = new StringTokenizer(propertyAttribute);
 
 		while (st.hasMoreTokens()) {
 			QName qName = BPELUtils.createQName(element, st.nextToken());
 			Property property = new PropertyProxy(resource.getURI(), qName);
 			if (eObject instanceof CorrelationSet) {
 				((CorrelationSet)eObject).getProperties().add(property);
 			} else if (eObject instanceof To) {
 				((To)eObject).setProperty(property);
 			}
 		}
 	}
 
 	/**
 	 * Sets a CompensationHandler element for a given eObject.
 	 */
 	protected void setCompensationHandler(Element element, EObject eObject) {
        Element compensationHandlerElement = getBPELChildElementByLocalName(element, "compensationHandler");
                  
 		if (compensationHandlerElement != null) {
 			CompensationHandler compensationHandler = xml2CompensationHandler(compensationHandlerElement);
 			xml2ExtensibleElement(compensationHandler, compensationHandlerElement); 
 
 			if (eObject instanceof Invoke)	
 				((Invoke)eObject).setCompensationHandler(compensationHandler);
 			else if (eObject instanceof Scope)		
 				((Scope)eObject).setCompensationHandler(compensationHandler);
         }  
 	}
 
 	/**
 	 * Sets a FaultHandler element for a given extensibleElement.
 	 */
 	protected void setFaultHandler(Element element, ExtensibleElement extensibleElement) {
 		List<Element> faultHandlerElements = getBPELChildElementsByLocalName(element, "faultHandlers");
 		
 		if (faultHandlerElements.size() > 0) {
 			FaultHandler faultHandler =	xml2FaultHandler(faultHandlerElements.get(0)); 
 			
 			if (extensibleElement instanceof Process) {
 				((Process)extensibleElement).setFaultHandlers(faultHandler);
 			} else if (extensibleElement instanceof Invoke) {
 				((Invoke)extensibleElement).setFaultHandler(faultHandler);
 			}
 		}
 	}
 	
 	/**
 	 * Sets a EventHandler element for a given extensibleElement.
 	 */
 	protected void setEventHandler(Element element, ExtensibleElement extensibleElement) {
 		List<Element> eventHandlerElements = getBPELChildElementsByLocalName(element, "eventHandlers");
                  
 		if (eventHandlerElements.size() > 0) {
 			EventHandler eventHandler =	xml2EventHandler(eventHandlerElements.get(0)); 
 
 			if (extensibleElement instanceof Process) ((Process)extensibleElement).setEventHandlers(eventHandler);
 				else if (extensibleElement instanceof Scope) ((Scope)extensibleElement).setEventHandlers(eventHandler);
 		}
 	}	
 
 
 	/**
 	 * Sets the standard attributes (name, joinCondition, and suppressJoinFailure).
 	 */
 	protected void setStandardAttributes(Element activityElement, Activity activity) {
 
 		// Set name
 		Attr name = activityElement.getAttributeNode("name");
 		
 		if (name != null && name.getSpecified())		
 			activity.setName(name.getValue());
 
 		// Set suppress join failure
 		Attr suppressJoinFailure = activityElement.getAttributeNode("suppressJoinFailure");
 		
 		if (suppressJoinFailure != null && suppressJoinFailure.getSpecified())		
 			activity.setSuppressJoinFailure(new Boolean(suppressJoinFailure.getValue().equals("yes")));
 	}
 
 
 	/**
 	 * Sets name, portType, operation, partner, variable and correlation for a given PartnerActivity object.
 	 */
 	protected void setOperationParms(final Element activityElement,
 									 final PartnerActivity activity,
 									 EReference variableReference,
 									 EReference inputVariableReference,
 									 EReference outputVariableReference,
 									 EReference partnerReference) {
 		// Set partnerLink
 		setPartnerLink(activityElement, activity, partnerReference);
 
 		// Set portType
         PortType portType = null;
         if (activityElement.hasAttribute("portType")) {
             portType = BPELUtils.getPortType(resource.getURI(), activityElement, "portType");
             activity.setPortType(portType);
         }
 
 		// Set operation
 		if (activityElement.hasAttribute("operation")) {
             if (portType != null) {
 				activity.setOperation(BPELUtils.getOperation(resource.getURI(), portType, activityElement, "operation"));
 			} else {
                 ((PartnerActivityImpl) activity).setOperationName(activityElement.getAttribute("operation"));
             }
 		}
 		
 		// Set variable
 		if (variableReference != null) {
 			setVariable(activityElement, activity, "variable", variableReference);
 		}
 		if (inputVariableReference != null) {
 			setVariable(activityElement, activity, "inputVariable", inputVariableReference);
 		}
 		if (outputVariableReference != null) {
 			setVariable(activityElement, activity, "outputVariable", outputVariableReference);
 		}
 		
 		// Set correlations
 		Element correlationsElement = getBPELChildElementByLocalName(activityElement, "correlations");
 		if (correlationsElement != null) {
 			Correlations correlations = xml2Correlations(correlationsElement);
 			activity.setCorrelations(correlations);
 		}
 	}
 
 	/**
 	 * Sets name, portType, operation, partner, variable and correlation for a given PartnerActivity object.
 	 */
 	protected void setOperationParmsOnMessage(final Element activityElement, final OnMessage onMessage) {
 		// Set partnerLink
 		setPartnerLink(activityElement, onMessage, BPELPackage.eINSTANCE.getOnMessage_PartnerLink());
 
         // Set portType
         PortType portType = null;
         if (activityElement.hasAttribute("portType")) {
             portType = BPELUtils.getPortType(resource.getURI(), activityElement, "portType");
             onMessage.setPortType(portType);
         }
         
         // Set operation
         if (activityElement.hasAttribute("operation")) {
             if (portType != null) {
                 onMessage.setOperation(BPELUtils.getOperation(resource.getURI(), portType, activityElement, "operation"));
             } else {
                 // If portType is not specified it will be resolved lazily and so will the operation.
                 // Save the deserialized name so the operation can be later resolved.
                 ((OnMessageImpl) onMessage).setOperationName(activityElement.getAttribute("operation"));
             }
         }
 
 		// Set variable
 		setVariable(activityElement, onMessage, "variable", BPELPackage.eINSTANCE.getOnMessage_Variable());
 
 		// Set correlations
 		Element correlationsElement = getBPELChildElementByLocalName(activityElement, "correlations");
 		if (correlationsElement != null) {
 			Correlations correlations = xml2Correlations(correlationsElement);
 			onMessage.setCorrelations(correlations);
 		}
 	}
 
 	/**
 	 * Sets name, portType, operation, partner, variable, messageType and correlation for a given PartnerActivity object.
 	 */
 	protected void setOperationParmsOnEvent(final Element activityElement, final OnEvent onEvent) {
 		// Set partnerLink
 		setPartnerLink(activityElement, onEvent, BPELPackage.eINSTANCE.getOnEvent_PartnerLink());
 
         // Set portType
         PortType portType = null;
         if (activityElement.hasAttribute("portType")) {
             portType = BPELUtils.getPortType(resource.getURI(), activityElement, "portType");
             onEvent.setPortType(portType);
         }
 
         // Set operation
         if (activityElement.hasAttribute("operation")) {
             if (portType != null) {
                 onEvent.setOperation(BPELUtils.getOperation(resource.getURI(), portType, activityElement, "operation"));
             } else {
                 ((OnEventImpl) onEvent).setOperationName(activityElement.getAttribute("operation"));
             }
         }
 
 		// Set variable
 		if (activityElement.hasAttribute("variable")) {
 			Variable variable = BPELFactory.eINSTANCE.createVariable();		
 	
 			// Set name
 			String name = activityElement.getAttribute("variable");
 			variable.setName(name);
 			onEvent.setVariable(variable);
 			// Don't set the message type of the variable, this will happen
 			// in the next step.
 		}
 		
 		// Set message type
 		if (activityElement.hasAttribute("messageType")) {
 			QName qName = BPELUtils.createAttributeValue(activityElement, "messageType");
 			Message messageType = new MessageProxy(resource.getURI(), qName);
 			onEvent.setMessageType(messageType);
 		}
 
 		// Set correlations
 		Element correlationsElement = getBPELChildElementByLocalName(activityElement, "correlations");
 		if (correlationsElement != null) {
 			Correlations correlations = xml2Correlations(correlationsElement);
 			onEvent.setCorrelations(correlations);
 		}
 	}
 
 	/**
 	 * Converts an XML document to a BPEL Resource object.
 	 */
 	protected Process xml2Resource(Document document) {
 		Element processElement = (document != null)? document.getDocumentElement(): null;
 		return xml2Process(processElement);
 	}
 
 
 	/**
 	 * Converts an XML process to a BPEL Process object.
 	 */
 	@SuppressWarnings("nls")
 	protected Process xml2Process(Element processElement) {
 		if (!processElement.getLocalName().equals("process")) {
 			return null;
 		}
 		if (!BPELConstants.isBPELNamespace(processElement.getNamespaceURI())) {
 			return null;
 		}
 		
 		process = BPELFactory.eINSTANCE.createProcess();
 		process.setElement(processElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(process, processElement);
 		
 		// Handle Process element
 		if (processElement.hasAttribute("name")) 
 			process.setName(processElement.getAttribute("name"));
 		
 		if (processElement.hasAttribute("targetNamespace"))	
 			process.setTargetNamespace(processElement.getAttribute("targetNamespace"));
 		
 		if (processElement.hasAttribute("suppressJoinFailure"))
 			process.setSuppressJoinFailure(new Boolean(processElement.getAttribute("suppressJoinFailure").equals("yes")));
 		
 		if (processElement.hasAttribute("exitOnStandardFault"))
 			process.setExitOnStandardFault(new Boolean(processElement.getAttribute("exitOnStandardFault").equals("yes")));
 		
 		if (processElement.hasAttribute("variableAccessSerializable"))
 			process.setVariableAccessSerializable(new Boolean(processElement.getAttribute("variableAccessSerializable").equals("yes")));
 
 		if (processElement.hasAttribute("queryLanguage"))
 			process.setQueryLanguage(processElement.getAttribute("queryLanguage"));
 
 		if (processElement.hasAttribute("expressionLanguage"))
 			process.setExpressionLanguage(processElement.getAttribute("expressionLanguage"));
 			
 		// Handle Import Elements
 		for(Element e : getBPELChildElementsByLocalName(processElement, "import")) {
 			process.getImports().add(xml2Import(e));
 		}
 		
 		
 		// Handle PartnerLinks Element
 		Element partnerLinksElement = getBPELChildElementByLocalName(processElement, "partnerLinks");
 		if (partnerLinksElement != null)
 			process.setPartnerLinks(xml2PartnerLinks(partnerLinksElement));
 			
 		// Handle Variables Element
 		Element variablesElement = getBPELChildElementByLocalName(processElement, "variables");
 		if (variablesElement != null)
 			process.setVariables(xml2Variables(variablesElement));
 			
 		// Handle CorrelationSets Element
 		Element correlationSetsElement = getBPELChildElementByLocalName(processElement, "correlationSets");
 		if (correlationSetsElement != null)
 			process.setCorrelationSets(xml2CorrelationSets(correlationSetsElement));
 			 
 		// Handle MessageExchanges Element
 		Element messageExchangesElements = getBPELChildElementByLocalName(processElement, "messageExchanges");
 		if (messageExchangesElements != null)
 			process.setMessageExchanges(xml2MessageExchanges(messageExchangesElements));
 		
 		// Handle Extensions Element
 		Element extensionsElement = getBPELChildElementByLocalName(processElement, "extensions");
 		if (extensionsElement != null)
 			process.setExtensions(xml2Extensions(extensionsElement));
 
 		// Handle FaultHandler element
 		setFaultHandler(processElement, process);
 		
 		// Handle CompensationHandler element
 		// In BPEL 2.0, there is no compensation handler on process
 		//setCompensationHandler(processElement, process);
 		
 		// Handle EventHandler element
 		setEventHandler(processElement, process);
 		
  		// Handle Activity elements
         Activity activity = xml2Activity(processElement); 
         process.setActivity(activity); 
 
 		xml2ExtensibleElement(process,processElement);
 		
 		return process;
 	}
 	
 	/**
 	 * Converts an XML partnerLinks
 	 */
 	protected PartnerLinks xml2PartnerLinks(Element partnerLinksElement) {
 		if (!partnerLinksElement.getLocalName().equals("partnerLinks")) {
 			return null;
 		}
 			
 		PartnerLinks partnerLinks = BPELFactory.eINSTANCE.createPartnerLinks();		
 		partnerLinks.setElement(partnerLinksElement);		
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(partnerLinks, partnerLinksElement);
 		
 		for(Element e : getBPELChildElementsByLocalName(partnerLinksElement, "partnerLink")) {
 			partnerLinks.getChildren().add( xml2PartnerLink(e) );
 		}		
 		xml2ExtensibleElement(partnerLinks, partnerLinksElement);
 	
 		return partnerLinks;
 	}
 
 
 	protected Variables xml2Variables(Element variablesElement) {
 		if (!variablesElement.getLocalName().equals("variables"))
 			return null;
 			
 		Variables variables = BPELFactory.eINSTANCE.createVariables();
 		variables.setElement(variablesElement);
 						
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(variables, variablesElement);		
 		for(Element e : getBPELChildElementsByLocalName(variablesElement, "variable")) {
 			variables.getChildren().add( xml2Variable( e ) );
 		}				
 		xml2ExtensibleElement(variables, variablesElement);
 		
 		// Move variables that are extensibility elements to the list of children
 		// JM: What is this supposed to accomplish?
 		List toBeMoved = new BasicEList();
 		for (Iterator iter = variables.getExtensibilityElements().iterator(); iter.hasNext();) {
 			ExtensibilityElement element = (ExtensibilityElement) iter.next();
 			if(element instanceof Variable)
 				toBeMoved.add(element);
 		}
 		
 		List children = variables.getChildren();
 		List extensibility = variables.getExtensibilityElements();
 		for (Iterator iter = toBeMoved.iterator(); iter.hasNext();) {
 			Variable element = (Variable) iter.next();
 			extensibility.remove(element);
 			children.add(element);
 		}
 		
 		return variables;
 	}
 	
 	protected CorrelationSets xml2CorrelationSets(Element correlationSetsElement) {
 		if (!correlationSetsElement.getLocalName().equals("correlationSets"))
 			return null;
 			
 		CorrelationSets correlationSets = BPELFactory.eINSTANCE.createCorrelationSets();
 		correlationSets.setElement(correlationSetsElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(correlationSets, correlationSetsElement);		
 		
 		for(Element e : getBPELChildElementsByLocalName(correlationSetsElement, "correlationSet")) {
 			correlationSets.getChildren().add(xml2CorrelationSet(e));
 		}
 
 		xml2ExtensibleElement(correlationSets, correlationSetsElement);
 		
 		return correlationSets;
 	}
 
 	protected MessageExchanges xml2MessageExchanges(Element messageExchangesElement) {
 		if (!messageExchangesElement.getLocalName().equals("messageExchanges"))
 			return null;
 		
 		MessageExchanges messageExchanges = BPELFactory.eINSTANCE.createMessageExchanges();
 		
 		// Save all the references to external namespaces
 		saveNamespacePrefix(messageExchanges, messageExchangesElement);
 		
 		for(Element e : getBPELChildElementsByLocalName(messageExchangesElement, "messageExchange")) {
 			messageExchanges.getChildren().add(xml2MessageExchange(e));
 		}
 		
 		xml2ExtensibleElement(messageExchanges, messageExchangesElement);
 		
 		return messageExchanges;
 	}
 	
 	protected Extensions xml2Extensions(Element extensionsElement) {
 		if (!extensionsElement.getLocalName().equals("extensions"))
 			return null;
 			
 		Extensions extensions = BPELFactory.eINSTANCE.createExtensions();
 		extensions.setElement(extensionsElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(extensions, extensionsElement);		
 		for(Element e : getBPELChildElementsByLocalName(extensionsElement, "extension")) {
 			extensions.getChildren().add(xml2Extension(e));
 		}
 		
 		xml2ExtensibleElement(extensions, extensionsElement);
 		
 		return extensions;
 	}
 
 	/**
 	 * Converts an XML compensationHandler element to a BPEL CompensationHandler object.
 	 */
 	protected CompensationHandler xml2CompensationHandler(Element activityElement) {
 		CompensationHandler compensationHandler = BPELFactory.eINSTANCE.createCompensationHandler();
 		compensationHandler.setElement(activityElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(compensationHandler, activityElement);
 		
 		compensationHandler.setActivity(getChildActivity(activityElement));
 		
 		return compensationHandler;
 	}
 
 
 	/**
 	 * Converts an XML correlationSet element to a BPEL CorrelationSet object.
 	 */
 	protected CorrelationSet xml2CorrelationSet(Element correlationSetElement) {
 		CorrelationSet correlationSet = BPELFactory.eINSTANCE.createCorrelationSet();		
 		correlationSet.setElement(correlationSetElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(correlationSet, correlationSetElement);		
 		
 		if (correlationSetElement == null) return correlationSet;
 		
 		// Set name
 		Attr name = correlationSetElement.getAttributeNode("name");
 
 		if (name != null && name.getSpecified())		
 			correlationSet.setName(name.getValue());
 
 		setProperties(correlationSetElement, correlationSet, "properties");
 		
 		xml2ExtensibleElement(correlationSet, correlationSetElement);
 
 		return correlationSet;
 	}
 	
 	/**
 	 * Converts an XML messageExchange element to a BPEL MessageExchange object.
 	 */
 	protected MessageExchange xml2MessageExchange(Element messageExchangeElement) {
 		MessageExchange messageExchange = BPELFactory.eINSTANCE.createMessageExchange();
 		
 		// Save all the references to external namespaces
 		saveNamespacePrefix(messageExchange, messageExchangeElement);
 		
 		if (messageExchangeElement == null) 
 			return messageExchange;
 		
 		// Set name
 		if (messageExchangeElement.hasAttribute("name"))
 			messageExchange.setName(messageExchangeElement.getAttribute("name"));
 		
 		xml2ExtensibleElement(messageExchange, messageExchangeElement);
 		
 		return messageExchange;
 	}
 
 	/**
 	 * Converts an XML extension element to a BPEL Extension object.
 	 */
 	protected Extension xml2Extension(Element extensionElement) {
 		Extension extension = BPELFactory.eINSTANCE.createExtension();
 		extension.setElement(extensionElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(extension, extensionElement);		
 		
 		if (extensionElement == null) return extension;
 		
 		// Set namespace
 		if (extensionElement.hasAttribute("namespace"))	
 			extension.setNamespace(extensionElement.getAttribute("namespace"));
 		
 		// Set mustUnderstand
 		if (extensionElement.hasAttribute("mustUnderstand"))
 			extension.setMustUnderstand(new Boolean(extensionElement.getAttribute("mustUnderstand").equals("yes")));
 		
 		xml2ExtensibleElement(extension, extensionElement);
 
 		return extension;
 	}
 
 	/**
 	 * Converts an XML partnerLink element to a BPEL PartnerLink object.
 	 */
   	protected PartnerLink xml2PartnerLink(Element partnerLinkElement) {
 		if (!partnerLinkElement.getLocalName().equals("partnerLink"))
 			return null;
 			 
 		PartnerLink partnerLink = BPELFactory.eINSTANCE.createPartnerLink();
 		partnerLink.setElement(partnerLinkElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(partnerLink, partnerLinkElement);
 		
 		// Set name
 		if (partnerLinkElement.hasAttribute("name"))
 			partnerLink.setName(partnerLinkElement.getAttribute("name"));
 			
 		if (partnerLinkElement.hasAttribute("initializePartnerRole"))
 			partnerLink.setInitializePartnerRole(new Boolean(partnerLinkElement.getAttribute("initializePartnerRole").equals("yes")));		
 		
 		Attr partnerLinkTypeName = partnerLinkElement.getAttributeNode("partnerLinkType");
 		if (partnerLinkTypeName != null && partnerLinkTypeName.getSpecified()) {
 			QName sltQName = BPELUtils.createAttributeValue(partnerLinkElement, "partnerLinkType");
 			
 			PartnerLinkTypeProxy slt = new PartnerLinkTypeProxy(resource.getURI(), sltQName);
 			partnerLink.setPartnerLinkType(slt);
 			
 			if(slt != null) {
 				partnerLink.setPartnerLinkType(slt);
 				
 				if (partnerLinkElement.hasAttribute("myRole")) {
 					RoleProxy role = new RoleProxy(resource, slt, partnerLinkElement.getAttribute("myRole"));
 					partnerLink.setMyRole(role);
 				}
 				if (partnerLinkElement.hasAttribute("partnerRole")) {
 					RoleProxy role = new RoleProxy(resource, slt, partnerLinkElement.getAttribute("partnerRole"));
 					partnerLink.setPartnerRole(role);
 				}
 			}
 		}
 
 		xml2ExtensibleElement(partnerLink,partnerLinkElement);
 
         return partnerLink;
      }
 
 
 	/**
 	 * Converts an XML variable element to a BPEL Variable object.
 	 */
 	protected Variable xml2Variable(Element variableElement) {
 		if (!variableElement.getLocalName().equals("variable"))
 			return null;
 			 
 		Variable variable = BPELFactory.eINSTANCE.createVariable();
 		variable.setElement(variableElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(variable, variableElement);
 
 		// Set name
 		if (variableElement.hasAttribute("name")) {
 			String name = variableElement.getAttribute("name");
 			variable.setName(name);
 		}
 		
 		if (variableElement.hasAttribute("messageType")) {
 			QName qName = BPELUtils.createAttributeValue(variableElement,"messageType");
 			Message messageType = new MessageProxy(resource.getURI(), qName);
 			variable.setMessageType(messageType);
 		}
 
 		// Set xsd type
 		if (variableElement.hasAttribute("type")) {
 			QName qName = BPELUtils.createAttributeValue(variableElement, "type");
 			XSDTypeDefinition type = new XSDTypeDefinitionProxy(resource.getURI(), qName);
 			variable.setType(type);						
 		}
 		
 		// Set xsd element
 		if (variableElement.hasAttribute("element")) {
 			QName qName = BPELUtils.createAttributeValue(variableElement, "element");
 			XSDElementDeclaration element = new XSDElementDeclarationProxy(resource.getURI(), qName);
 			variable.setXSDElement(element);			
 		}
 
 		// from-spec
         Element fromElement = getBPELChildElementByLocalName(variableElement, "from");
         if (fromElement != null) {
             From from = BPELFactory.eINSTANCE.createFrom();
             from.setElement(fromElement);
             
             xml2From(from, fromElement); 
             variable.setFrom(from);
         }
 		
 		xml2ExtensibleElement(variable,variableElement);
 		
         return variable;
      }
 
 	/**
 	 * Converts an XML faultHandler element to a BPEL FaultHandler object.
 	 */
  	protected FaultHandler xml2FaultHandler(Element faultHandlerElement) {
  		String localName = faultHandlerElement.getLocalName();
  		if (!(localName.equals("faultHandlers") ||
  				localName.equals("invoke")))
  			return null;
  			
 		FaultHandler faultHandler = BPELFactory.eINSTANCE.createFaultHandler();
 		
 		if (localName.equals("faultHandlers")) {
 			// This is "overloaded", what's the proper facade for the fault handler element in this case.
 			faultHandler.setElement(faultHandlerElement);
 		}
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(faultHandler, faultHandlerElement);
 				
 		for(Element e : getBPELChildElementsByLocalName(faultHandlerElement, "catch")) { 
 			faultHandler.getCatch().add( xml2Catch(e)); 			
 		}
 
 		Element catchAllElement = getBPELChildElementByLocalName(faultHandlerElement, "catchAll");
 		if (catchAllElement != null) {
 			CatchAll catchAll = xml2CatchAll(catchAllElement);
 			faultHandler.setCatchAll(catchAll);
 		}
 		
 		// Only do this for an element named faultHandlers. If the element is named
 		// invoke, then there really is no fault handler, only a series of catches.
 		if (faultHandlerElement.getLocalName().equals("faultHandlers")) {
 			xml2ExtensibleElement(faultHandler, faultHandlerElement);
 		}
 				
 		return faultHandler;		
  	}
 
 	/**
 	 * Converts an XML catchAll element to a BPEL CatchAll object.
 	 */
 	protected CatchAll xml2CatchAll(Element catchAllElement) {
 		if (!catchAllElement.getLocalName().equals("catchAll"))
 			return null;
 			
 		CatchAll catchAll = BPELFactory.eINSTANCE.createCatchAll();
 		catchAll.setElement(catchAllElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(catchAll, catchAllElement);		
 		
 		for(Element e : getChildElements(catchAllElement)) {
 			Activity activity = xml2Activity(e);
 			if (activity != null) {
 				catchAll.setActivity(activity);
 				break;
 			}
 		}
 		
 		xml2ExtensibleElement(catchAll, catchAllElement);
 		
 		return catchAll;
 	}
 
 	/**
 	 * Converts an XML catch element to a BPEL Catch object.
 	 */
 	protected Catch xml2Catch(Element catchElement) {
 		Catch _catch = BPELFactory.eINSTANCE.createCatch();
 		_catch.setElement(catchElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(_catch, catchElement);
 		
 		if (catchElement == null) return _catch;
 		
 		if (catchElement.hasAttribute("faultName")) {
 			QName qName = BPELUtils.createAttributeValue(catchElement, "faultName");	
 			_catch.setFaultName(qName);
 		}
 
 		if (catchElement.hasAttribute("faultVariable")) {
 			// Set fault variable
 			Variable variable = BPELFactory.eINSTANCE.createVariable();
 			// TODO: Should not this be the variable proxy ?
 			variable.setName(catchElement.getAttribute("faultVariable"));
 			_catch.setFaultVariable(variable);					
 		}		
 		
 		if (catchElement.hasAttribute("faultMessageType")) {
 			QName qName = BPELUtils.createAttributeValue(catchElement,"faultMessageType");
 			Message messageType = new MessageProxy(resource.getURI(), qName);
 			_catch.setFaultMessageType(messageType);
 		}
 
 		if (catchElement.hasAttribute("faultElement")) {
 			QName qName = BPELUtils.createAttributeValue(catchElement,"faultElement");
 			XSDElementDeclaration element = new XSDElementDeclarationProxy(resource.getURI(), qName);
 			_catch.setFaultElement(element);
 		}
 
 		// Set Activities		
 		NodeList catchElements = catchElement.getChildNodes();
         
         Element activityElement = null;
 
 		if (catchElements != null && catchElements.getLength() > 0) {
           
            for (int i = 0; i < catchElements.getLength(); i++) {
            	   if (catchElements.item(i).getNodeType() != Node.ELEMENT_NODE) {
            	   	  continue;
            	   }
            	
                activityElement = (Element)catchElements.item(i); 
                Activity activity = xml2Activity(activityElement);
                if (activity != null) { 
                		_catch.setActivity(activity);
                		break;
                }
            }
         }		
 
 		xml2ExtensibleElement(_catch, catchElement);
 		return _catch;
 	}
 
     /**
 	 * Converts an XML activity element to a BPEL Activity object.
 	 */
      protected Activity xml2Activity(Element activityElement) {
 		Activity activity = null;
 		boolean checkExtensibility = true;
 
         if (!BPELUtils.isBPELElement(activityElement))
             return null;
         
 		String localName = activityElement.getLocalName();        
         if (localName.equals("process")){ 
 			activity = getChildActivity(activityElement);
 			checkExtensibility = false;
 		} else if (localName.equals("receive")) {
        		activity = xml2Receive(activityElement);
      	} else if (localName.equals("reply")) {
       		activity = xml2Reply(activityElement);
      	} else if (localName.equals("invoke")) {
       		activity = xml2Invoke(activityElement);
      	} else if (localName.equals("assign")) {
       		activity = xml2Assign(activityElement);
      	} else if (localName.equals("throw")) {
       		activity = xml2Throw(activityElement);
      	} else if (localName.equals("exit")) {
       		activity = xml2Exit(activityElement);
      	} else if (localName.equals("wait")) {
       		activity = xml2Wait(activityElement);
      	} else if (localName.equals("empty")) {
       		activity = xml2Empty(activityElement);
      	} else if (localName.equals("sequence")) {
       		activity = xml2Sequence(activityElement);
      	} else if (localName.equals("if")) {
      		activity = xml2If(activityElement);
      	} else if (localName.equals("while")) {
      		activity = xml2While(activityElement);
      	} else if (localName.equals("pick")) {
      		activity = xml2Pick(activityElement);
      	} else if (localName.equals("flow")) {
      		activity = xml2Flow(activityElement);
      	} else if (localName.equals("scope")) {
      		activity = xml2Scope(activityElement);
      	} else if (localName.equals("compensate")) {
      		activity = xml2Compensate(activityElement);
      	} else if (localName.equals("compensateScope")) {
      		activity = xml2CompensateScope(activityElement);     		
      	} else if (localName.equals("rethrow")) {
      		activity = xml2Rethrow(activityElement);
      	} else if (localName.equals("extensionActivity")) {
     		// extensionActivity is a special case. It does not have any standard
     		// attributes or elements, nor is it an extensible element.
     		// Return immediately.
     		activity = xml2ExtensionActivity(activityElement);
     		return activity;
      	} else if (localName.equals("opaqueActivity")) {
      		activity = xml2OpaqueActivity(activityElement);
      	} else if (localName.equals("forEach")) {
      		activity = xml2ForEach(activityElement);
      	} else if (localName.equals("repeatUntil")) {
      		activity = xml2RepeatUntil(activityElement);
      	} else if (localName.equals("validate")) {
      		activity = xml2Validate(activityElement);
      	} else {
      		return null;
      	}
      	  	
 		setStandardElements(activityElement, activity);
 		
 		if (checkExtensibility) {
 			xml2ExtensibleElement(activity, activityElement);
 			// Save all the references to external namespaces		
 			saveNamespacePrefix(activity, activityElement);
 		}			
 			
 		return activity;
 	}
 
  	protected void setStandardElements(Element activityElement, Activity activity) {
 		// Handle targets
 		Element targetsElement = getBPELChildElementByLocalName(activityElement, "targets");
 		if (targetsElement != null) {
 			activity.setTargets(xml2Targets(targetsElement));
 		}
 				
 		// Handle sources
 		Element sourcesElement = getBPELChildElementByLocalName(activityElement, "sources");
 		if (sourcesElement != null) {
 			activity.setSources(xml2Sources(sourcesElement));
 		}
 	}
 
  	protected Targets xml2Targets(Element targetsElement) {
 		Targets targets = BPELFactory.eINSTANCE.createTargets();
 		targets.setElement(targetsElement);
 		
 		for(Element e :  getBPELChildElementsByLocalName(targetsElement, "target")) {			
 			targets.getChildren().add( xml2Target(e));          				
 		}
 		// Join condition
 		Element joinConditionElement = getBPELChildElementByLocalName(targetsElement, "joinCondition");
 		if (joinConditionElement != null) {
 			targets.setJoinCondition(xml2Condition(joinConditionElement));
 		}
 		xml2ExtensibleElement(targets, targetsElement);
 
 		return targets;
      }
      
 	protected Target xml2Target(Element targetElement) {
 		
 		final Target target = BPELFactory.eINSTANCE.createTarget();
 		target.setElement(targetElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(target, targetElement);
 		
 		xml2ExtensibleElement(target, targetElement);
 
 		if (targetElement.hasAttribute("linkName")) {
 			final String linkName = targetElement.getAttribute("linkName");			
 			process.getPostLoadRunnables().add(new Runnable() {
 				public void run() {
 					Link link = getLink(target.getActivity(), linkName);
 					if (link != null)
 						target.setLink(link);
 					else
 						target.setLink(new LinkProxy(resource.getURI(), linkName));
 				}
 			});
 		}
 		return target;		
 	}
 	
 	protected Sources xml2Sources(Element sourcesElement) {
 		Sources sources = BPELFactory.eINSTANCE.createSources();
 		sources.setElement(sourcesElement);
 		for(Element e : getBPELChildElementsByLocalName(sourcesElement, "source")) {
 			sources.getChildren().add( xml2Source(e));          				
 		}
 		xml2ExtensibleElement(sources, sourcesElement);
 
 		return sources;
 	}
 	
 	protected Source xml2Source(Element sourceElement) {
 		final String linkName = sourceElement.getAttribute("linkName");		
 		final Source source = BPELFactory.eINSTANCE.createSource();
 		source.setElement(sourceElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(source, sourceElement);
 		
 		// Read transitionCondition element
 		Element transitionConditionElement = getBPELChildElementByLocalName(sourceElement, "transitionCondition");
 		if (transitionConditionElement != null) {
 			Condition transitionCondition = xml2Condition(transitionConditionElement);
 			source.setTransitionCondition(transitionCondition);
 		}
 		
 		
 		xml2ExtensibleElement(source, sourceElement);
 		
 		process.getPostLoadRunnables().add(new Runnable() {
 			public void run() {
 				Link link = getLink(source.getActivity(), linkName);
 				if (link != null)
 					source.setLink(link);
 				else
 					source.setLink(new LinkProxy(resource.getURI(), linkName));
 			}
 		});
 		return source;							
 	}	
 	
 	/**
 	 * Converts an XML scope element to a BPEL Scope object.
 	 */
 	protected Activity xml2Scope(Element scopeElement) {
 		
     	Scope scope = BPELFactory.eINSTANCE.createScope();
 		scope.setElement(scopeElement);
 		
     	//if (scopeElement == null) {
 		//	return scope;
 		//}
 
 		Attr name = scopeElement.getAttributeNode("name");
 		
 		if (name != null && name.getSpecified()) {
 			scope.setName(name.getValue());
 		}
 				
 		Attr isolated = scopeElement.getAttributeNode("isolated");
 		
 		if (isolated != null && isolated.getSpecified())
 			scope.setIsolated(new Boolean(isolated.getValue().equals("yes")));
 		
 		// Handle attribute exitOnStandardFault
 		Attr exitOnStandardFault = scopeElement.getAttributeNode("exitOnStandardFault");
 		if (exitOnStandardFault != null && exitOnStandardFault.getSpecified())
 			scope.setExitOnStandardFault(new Boolean(exitOnStandardFault.getValue().equals("yes")));
 				
 		// Handle Variables element
 		Element variablesElement = getBPELChildElementByLocalName(scopeElement, "variables");
 		if (variablesElement != null) {
 			Variables variables = xml2Variables(variablesElement);
 			scope.setVariables(variables);
 		}
 				
 		// Handle CorrelationSet element
 		Element correlationSetsElement = getBPELChildElementByLocalName(scopeElement, "correlationSets");
 		if (correlationSetsElement != null) {
 			CorrelationSets correlationSets = xml2CorrelationSets(correlationSetsElement);
 			scope.setCorrelationSets(correlationSets);
 		}
 		
 		// Handle PartnerLinks element
 		Element partnerLinksElement = getBPELChildElementByLocalName(scopeElement, "partnerLinks");
 		if (partnerLinksElement != null) {
 			PartnerLinks partnerLinks = xml2PartnerLinks(partnerLinksElement);
 			scope.setPartnerLinks(partnerLinks);
 		}
 		
 		// MessageExchanges element
 		Element messageExchangesElement = getBPELChildElementByLocalName(scopeElement, "messageExchanges");
 		if (messageExchangesElement != null) {
 			MessageExchanges messageExchanges = xml2MessageExchanges(messageExchangesElement);
 			scope.setMessageExchanges(messageExchanges);
 		}
 				
 		// Handle FaultHandler element
         Element faultHandlerElement = getBPELChildElementByLocalName(scopeElement, "faultHandlers");
         if (faultHandlerElement != null) {               		
 			FaultHandler faultHandler =	xml2FaultHandler(faultHandlerElement); 
 			scope.setFaultHandlers(faultHandler);
         }
 
 		// Handle CompensationHandler element
 		setCompensationHandler(scopeElement, scope);
 		
 		// Handler TerminationHandler element
 		Element terminationHandlerElement = getBPELChildElementByLocalName(scopeElement, "terminationHandler");
 		if (terminationHandlerElement != null) {
 			TerminationHandler terminationHandler = xml2TerminationHandler(terminationHandlerElement);
 			scope.setTerminationHandler(terminationHandler);
 		}
 		
 		// Handler EventHandler element
 		setEventHandler(scopeElement, scope);
 		
 		setStandardAttributes(scopeElement, scope);
 
 		// Handle activities 
         NodeList scopeElements = scopeElement.getChildNodes();
         
         Element activityElement = null;
 
 		if (scopeElements != null && scopeElements.getLength() > 0) {
           
            for (int i = 0; i < scopeElements.getLength(); i++) {
 				if (scopeElements.item(i).getNodeType() != Node.ELEMENT_NODE) {
            	   	  	continue;
 				}
            	   	             	
                	activityElement = (Element)scopeElements.item(i); 
                
 				if (activityElement.getLocalName().equals("faultHandlers") || 
 					activityElement.getLocalName().equals("compensationHandler"))
 				{
 					continue;
 				}
                
                Activity activity = xml2Activity(activityElement);
                if (activity != null) { 
                		scope.setActivity(activity);
                		break;
                }
            }
         }
         		
         return scope;
 	}
 
 	/**
 	 * Converts an XML flow element to a BPEL Flow object.
 	 */
 	protected Activity xml2Flow(Element flowElement) {
     	
 		Flow flow = BPELFactory.eINSTANCE.createFlow();
 		flow.setElement(flowElement);
 		
     	// if (flowElement == null) {
 		//	return flow;		
 		// }
 		
 		Attr name = flowElement.getAttributeNode("name");
 		
 		if (name != null && name.getSpecified()) 
 			flow.setName(name.getValue());
 		
 		Element linksElement = getBPELChildElementByLocalName(flowElement, "links");
 		if (linksElement != null) {
 			Links links = xml2Links(linksElement);
 			flow.setLinks(links);
 		}
 			 
 		Element completionConditionElement = getBPELChildElementByLocalName(flowElement, "completionCondition");
 		if (completionConditionElement != null) {
 			CompletionCondition completionCondition = xml2CompletionCondition(completionConditionElement);
 			flow.setCompletionCondition(completionCondition);
 		}
 		
         setStandardAttributes(flowElement, flow);
         
         NodeList flowElements = flowElement.getChildNodes();
         
         Element activityElement = null;
 
 		if (flowElements != null && flowElements.getLength() > 0) {
           
            for (int i = 0; i < flowElements.getLength(); i++) {
 				if ((flowElements.item(i).getNodeType() != Node.ELEMENT_NODE) || 
 				     ((Element)flowElements.item(i)).getLocalName().equals("links"))
            	   	  continue;
            	   	             	
                activityElement = (Element)flowElements.item(i); 
                Activity activity = xml2Activity(activityElement);
                if (activity != null) {
                		flow.getActivities().add(activity);
                }
            }
         }
 		
 		return flow;
 	}
 
 	protected Links xml2Links(Element linksElement) {
 		if (!linksElement.getLocalName().equals("links"))
 			return null;
 			
 		Links links = BPELFactory.eINSTANCE.createLinks();
 		links.setElement(linksElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(links, linksElement);
 		
 		for(Element e :  getBPELChildElementsByLocalName(linksElement, "link")) {
 			links.getChildren().add( xml2Link (e));
 		}
 		
 		// extensibility elements
 		xml2ExtensibleElement(links, linksElement);
 		
 		return links; 	
 	}
 	
 	/**
 	 * Converts an XML link element to a BPEL Link object.
 	 */
 	protected Link xml2Link(Element linkElement) {
 		Link link = BPELFactory.eINSTANCE.createLink();
 		link.setElement(linkElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(link, linkElement);				
 
 		Attr name = linkElement.getAttributeNode("name");
 		
 		if (name != null && name.getSpecified())
 			link.setName(name.getValue());
 		
 		xml2ExtensibleElement(link,linkElement); 
 
 		return link;		
 	}
 
 	/**
 	 * Converts an XML pick element to a BPEL Pick object.
 	 */
 	protected Activity xml2Pick(Element pickElement) {
     	Pick pick = BPELFactory.eINSTANCE.createPick();
 		pick.setElement(pickElement);
 
 		// Set name
 		Attr name = pickElement.getAttributeNode("name");
 		
 		if (name != null && name.getSpecified())
 			pick.setName(name.getValue());
 		
 		// Set createInstance
 		Attr createInstance = pickElement.getAttributeNode("createInstance");
 		
 		if (createInstance != null && createInstance.getSpecified()) 
        		pick.setCreateInstance(Boolean.valueOf(createInstance.getValue().equals("yes") ? "True":"False"));  	
 	
         NodeList pickElements = pickElement.getChildNodes();
         
         Element pickInstanceElement = null;
 
 		if (pickElements != null && pickElements.getLength() > 0) {
           
            for (int i = 0; i < pickElements.getLength(); i++) {
 				if (pickElements.item(i).getNodeType() != Node.ELEMENT_NODE)
            	   	  continue;
            	   	             	
                pickInstanceElement = (Element)pickElements.item(i);
                
 				if (pickInstanceElement.getLocalName().equals("onAlarm")) {
      				OnAlarm onAlarm = xml2OnAlarm((Element)pickInstanceElement);
      				
      				pick.getAlarm().add(onAlarm);
      			}     	
 				else
 					if (pickInstanceElement.getLocalName().equals("onMessage")) {
      					OnMessage onMessage = xml2OnMessage((Element)pickInstanceElement);
 	     				
     	 				pick.getMessages().add(onMessage);
      				}     
            }
         }
         
         setStandardAttributes(pickElement, pick);
 
 		return pick;
 	}
 
 	/**
 	 * Converts an XML eventHandler element to a BPEL eventHandler object.
 	 */
 	protected EventHandler xml2EventHandler(Element eventHandlerElement) {
 		EventHandler eventHandler = BPELFactory.eINSTANCE.createEventHandler();
 		eventHandler.setElement(eventHandlerElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(eventHandler, eventHandlerElement);			
 	
 		NodeList eventHandlerElements = eventHandlerElement.getChildNodes();        
 		Element eventHandlerInstanceElement = null;
 		if (eventHandlerElements != null && eventHandlerElements.getLength() > 0) {
           
 			for (int i = 0; i < eventHandlerElements.getLength(); i++) {
 				if (eventHandlerElements.item(i).getNodeType() != Node.ELEMENT_NODE)
 					continue;           	   	             
 			   	eventHandlerInstanceElement = (Element)eventHandlerElements.item(i);
                
 				if (eventHandlerInstanceElement.getLocalName().equals("onAlarm")) {
 					OnAlarm onAlarm = xml2OnAlarm((Element)eventHandlerInstanceElement);     				
 					eventHandler.getAlarm().add(onAlarm);
 				}   
 				else if (eventHandlerInstanceElement.getLocalName().equals("onEvent")) {
 					OnEvent onEvent = xml2OnEvent((Element)eventHandlerInstanceElement);	     				
 					eventHandler.getEvents().add(onEvent);
 				}  
 			}
 		}       
 		
 		xml2ExtensibleElement(eventHandler, eventHandlerElement); 
 		return eventHandler;
 	}
 
 	/**
 	 * Converts an XML onMessage element to a BPEL OnMessage object.
 	 */
 	protected OnMessage xml2OnMessage(Element onMessageElement) {
  		OnMessage onMessage = BPELFactory.eINSTANCE.createOnMessage();
  		onMessage.setElement(onMessageElement);
  		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(onMessage, onMessageElement); 			
 
 		// Set several parms
 		setOperationParmsOnMessage(onMessageElement, onMessage);
 				
 		// Set activity
 		onMessage.setActivity(getChildActivity(onMessageElement));
 
 		// Set the FromPart
 		for(Element e :  getBPELChildElementsByLocalName(onMessageElement, "fromPart")) {
 			onMessage.getFromPart().add( xml2FromPart ( e ));
 		}		
 
 		xml2ExtensibleElement(onMessage, onMessageElement);
 				
 		return onMessage;
 	}
 
 	/**
 	 * Converts an XML onEvent element to a BPEL OnEvent object.
 	 */
 	protected OnEvent xml2OnEvent(Element onEventElement) {
 		OnEvent onEvent = BPELFactory.eINSTANCE.createOnEvent();
 		onEvent.setElement(onEventElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(onEvent, onEventElement); 			
 
 		// Set several parms
 		setOperationParmsOnEvent(onEventElement, onEvent);
 				
 		// Set activity
 		onEvent.setActivity(getChildActivity(onEventElement));
 
 		// Set the FromPart
 		for(Element e :  getBPELChildElementsByLocalName(onEventElement, "fromPart")) {
 			onEvent.getFromPart().add(xml2FromPart(e));
 		}		
 		
 		// Handle CorrelationSets Element
 		Element correlationSetsElement = getBPELChildElementByLocalName(onEventElement, "correlationSets");
 		if (correlationSetsElement != null)
 			onEvent.setCorrelationSets(xml2CorrelationSets(correlationSetsElement));
 		
 		xml2ExtensibleElement(onEvent, onEventElement);
 				
 		return onEvent;
 	}
 
 	/**
 	 * Converts an XML onAlarm element to a BPEL OnAlarm object.
 	 */
 	protected OnAlarm xml2OnAlarm(Element onAlarmElement) {
    		OnAlarm onAlarm = BPELFactory.eINSTANCE.createOnAlarm();
    		onAlarm.setElement(onAlarmElement);
    		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(onAlarm, onAlarmElement);   			
 		
 		// Set for element
 		Element forElement = getBPELChildElementByLocalName(onAlarmElement, "for");
 		if (forElement != null) {
 			Expression expression = xml2Expression(forElement);
 			onAlarm.setFor(expression);
 		}
 		
 		// Set until element
 		Element untilElement = getBPELChildElementByLocalName(onAlarmElement, "until");
 		if (untilElement != null) {
 			Expression expression = xml2Expression(untilElement);
 			onAlarm.setUntil(expression);
 		}
 		
 		// Set repeatEvery element
 		Element repeatEveryElement = getBPELChildElementByLocalName(onAlarmElement, "repeatEvery");
 		if (repeatEveryElement != null) {
 			Expression expression = xml2Expression(repeatEveryElement);
 			onAlarm.setRepeatEvery(expression);
 		}
 		
 		// Set activity
 		onAlarm.setActivity(getChildActivity(onAlarmElement));
 		
 		xml2ExtensibleElement(onAlarm, onAlarmElement);		
 			
 		return onAlarm;					
 	}
 
 	/**
 	 * Converts an XML while element to a BPEL While object.
 	 */
 	protected Activity xml2While(Element whileElement) {
     	While _while = BPELFactory.eINSTANCE.createWhile();
 		_while.setElement(whileElement);
 
 		// Handle condition element
 		Element conditionElement = getBPELChildElementByLocalName(whileElement, "condition");
 		if (conditionElement != null) {
 			Condition condition = xml2Condition(conditionElement);
 			_while.setCondition(condition);
 		}
 
         NodeList whileElements = whileElement.getChildNodes();
         
         Element activityElement = null;
 
 		if (whileElements != null && whileElements.getLength() > 0) {
 			
 			for (int i = 0; i < whileElements.getLength(); i++) {			
 				if (whileElements.item(i).getNodeType() != Node.ELEMENT_NODE) {
            	   	  continue;
 				}
            	   	  			
 				activityElement = (Element) whileElements.item(i); 
             	Activity activity = xml2Activity(activityElement);
             	if (activity != null) { 
          	   		_while.setActivity(activity);
          	   		// only the first one
          	   		break ;
             	}
             	
 			}
         }
         
         setStandardAttributes(whileElement, _while);
 		
 		return _while;
 	}
 
 	/**
 	 * Converts an XML terminationHandler element to a BPEL TerminationHandler object.
 	 */
 	protected TerminationHandler xml2TerminationHandler(Element terminationHandlerElement) {
 		TerminationHandler terminationHandler = BPELFactory.eINSTANCE.createTerminationHandler();
 		terminationHandler.setElement(terminationHandlerElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(terminationHandler, terminationHandlerElement);
 		
 		terminationHandler.setActivity(getChildActivity(terminationHandlerElement));
 
 		xml2ExtensibleElement(terminationHandler, terminationHandlerElement); 
 
 		return terminationHandler;
 	}
 
 
 
 	/**
 	 * Converts an XML if element to a BPEL If object.
 	 */
 	protected Activity xml2If(Element ifElement) {
 		If _if = BPELFactory.eINSTANCE.createIf();		
 		_if.setElement(ifElement);
 
 		// Set activity
 		Activity activity = getChildActivity(ifElement);
 		if (activity != null) {
 			_if.setActivity(activity);
 		}
 		
 		// Handle condition element
 		Element conditionElement = getBPELChildElementByLocalName(ifElement, "condition");
 		if (conditionElement != null) {
 			Condition condition = xml2Condition(conditionElement);
 			_if.setCondition(condition);
 		}
 		
 		// Handle elseif
 		for(Element e : getBPELChildElementsByLocalName(ifElement, "elseif")) { 
 			_if.getElseIf().add( xml2ElseIf ( e ));
         }
         
 		// Handle else
 		Element elseElement = getBPELChildElementByLocalName(ifElement, "else");
 		if (elseElement != null) {
 			Else _else = xml2Else(elseElement);
 			_if.setElse(_else);
 		}
 		
 		setStandardAttributes(ifElement, _if);
 		
 		return _if;		
 	}
 
 	/**
 	 * Converts an XML elseIf element to a BPEL ElseIf object.
 	 */
 	protected ElseIf xml2ElseIf(Element elseIfElement) {
 		ElseIf elseIf = BPELFactory.eINSTANCE.createElseIf();
     	elseIf.setElement(elseIfElement);
     	
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(elseIf, elseIfElement);
 
 		// Handle condition element
 		Element conditionElement = getBPELChildElementByLocalName(elseIfElement, "condition");
 		if (conditionElement != null) {
 			Condition condition = xml2Condition(conditionElement);
 			elseIf.setCondition(condition);
 		}
 
 		// Set activity
 		Activity activity = getChildActivity(elseIfElement);
 		if (activity != null) {
 			elseIf.setActivity(activity);
 		}
 		
 		return elseIf;
 	}
 
 	/**
 	 * Converts an XML condition element to a BPEL Condition object.
 	 */
 	protected Condition xml2Condition(Element conditionElement) {
 		Condition condition = BPELFactory.eINSTANCE.createCondition();
     	condition.setElement(conditionElement);
     	
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(condition, conditionElement);    			
 
 		if (conditionElement.hasAttribute("expressionLanguage")) {
 			// Set expressionLanguage
 			condition.setExpressionLanguage(conditionElement.getAttribute("expressionLanguage"));
 		}
 		
 		// Determine whether or not there is an element in the child list.
 		Node candidateChild = null;
 		NodeList nodeList = conditionElement.getChildNodes();
 		int length = nodeList.getLength();
 		for (int i = 0; i < length; i++) {
 			Node child = nodeList.item(i);
 			if (child.getNodeType() == Node.ELEMENT_NODE) {
 				candidateChild = child;
 				break;
 			}
 		}
 		if (candidateChild == null) {
 			candidateChild = conditionElement.getFirstChild();
 		}
 		String data = getText(candidateChild);
 		
 		if (data == null) {
 			// No text or CDATA node. If it's an element node, then
 			// deserialize and install.
 			if (candidateChild != null && candidateChild.getNodeType() == Node.ELEMENT_NODE) {
 				// Look if there's an ExtensibilityElement deserializer for this element
 				Element childElement = (Element)candidateChild;
 				QName qname = new QName(childElement.getNamespaceURI(), childElement.getLocalName());
 				BPELExtensionDeserializer deserializer=null;
 				try {
 					deserializer = (BPELExtensionDeserializer)extensionRegistry.queryDeserializer(ExtensibleElement.class,qname);
 				} catch (WSDLException e) {}
 				if (deserializer!=null) {
 					// Deserialize the DOM element and add the new Extensibility element to the parent
 					// ExtensibleElement
 					try {
 						Map nsMap = getAllNamespacesForElement(conditionElement);
 						ExtensibilityElement extensibilityElement=deserializer.unmarshall(ExtensibleElement.class,qname,childElement,process,nsMap,extensionRegistry,resource.getURI());
 						condition.setBody(extensibilityElement);
 					} catch (WSDLException e) {
 						throw new WrappedException(e);
 					}
 				}
 			}			
 		} else {
 			condition.setBody(data);
 		}
 
 		return condition;
 	}
 
 	/**
 	 * Converts an XML expression element to a BPEL Expression object.
 	 * 
 	 * Accept a pre-constructed argument. This is good for sub-types
 	 * of expression.
 	 * 
 	 * Returns the second argument as a convenience.
 	 * 
 	 * TODO: Make condition use this one as well.
 	 */
 	protected Expression xml2Expression(Element expressionElement, Expression expression) {
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(expression, expressionElement);
     	
 		if (expressionElement == null) return expression;
 
 		// Set expressionLanguage
 		if (expressionElement.hasAttribute("expressionLanguage")) {
 			expression.setExpressionLanguage(expressionElement.getAttribute("expressionLanguage"));
 		}
 
 		// Set opaque
 		if (expressionElement.hasAttribute("opaque")) {
 			expression.setOpaque(new Boolean(expressionElement.getAttribute("opaque").equals("yes")));
 		}
 		
 		// Determine whether or not there is an element in the child list.
 		Node candidateChild = null;
 		NodeList nodeList = expressionElement.getChildNodes();
 		int length = nodeList.getLength();
 		for (int i = 0; i < length; i++) {
 			Node child = nodeList.item(i);
 			if (child.getNodeType() == Node.ELEMENT_NODE) {
 				candidateChild = child;
 				break;
 			}
 		}
 		if (candidateChild == null) {
 			candidateChild = expressionElement.getFirstChild();
 		}
 		String data = getText(candidateChild);
 		
 		if (data == null) {
 			// No text or CDATA node. If it's an element node, then
 			// deserialize and install.
 			if (candidateChild != null && candidateChild.getNodeType() == Node.ELEMENT_NODE) {
 				// Look if there's an ExtensibilityElement deserializer for this element
 				Element childElement = (Element)candidateChild;
 				QName qname = new QName(childElement.getNamespaceURI(), childElement.getLocalName());
 				BPELExtensionDeserializer deserializer=null;
 				try {
 					deserializer = (BPELExtensionDeserializer)extensionRegistry.queryDeserializer(ExtensibleElement.class,qname);
 				} catch (WSDLException e) {}
 				if (deserializer!=null) {
 					// Deserialize the DOM element and add the new Extensibility element to the parent
 					// ExtensibleElement
 					try {
 						Map nsMap = getAllNamespacesForElement(expressionElement);
 						ExtensibilityElement extensibilityElement=deserializer.unmarshall(ExtensibleElement.class,qname,childElement,process,nsMap,extensionRegistry,resource.getURI());
 						expression.setBody(extensibilityElement);
 					} catch (WSDLException e) {
 						throw new WrappedException(e);
 					}
 				}
 			}			
 		} else {
 			expression.setBody(data);
 		}
 
 		return expression;
 	}
 
 	/**
 	 * Converts an XML expression element to a BPEL Expression object.
 	 */
 	protected Expression xml2Expression(Element expressionElement) {
 		Expression expression = BPELFactory.eINSTANCE.createExpression();
 		expression.setElement(expressionElement);
 		
     	return xml2Expression(expressionElement, expression);
 	}
 
 	protected Else xml2Else(Element elseElement) {
 		Else _else = BPELFactory.eINSTANCE.createElse();
 		_else.setElement(elseElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(_else, elseElement);
 		
 		Activity activity = getChildActivity(elseElement);
 		_else.setActivity(activity);
 		
 		return _else;
 	}
 
 
 	/**
 	 * Converts an XML sequence element to a BPEL Sequence object.
 	 */
 	protected Activity xml2Sequence(Element sequenceElement) {
     	Sequence sequence = BPELFactory.eINSTANCE.createSequence();
     	sequence.setElement(sequenceElement);   	
 	
 		// Set name
 		Attr name = sequenceElement.getAttributeNode("name");
 		
 		if (name != null && name.getSpecified()) {
 			sequence.setName(name.getValue());
 		}
 		
         NodeList sequenceElements = sequenceElement.getChildNodes();
         
         Element activityElement = null;
 
 		if (sequenceElements != null && sequenceElements.getLength() > 0) {
           
            for (int i = 0; i < sequenceElements.getLength(); i++) {
         	   if (sequenceElements.item(i).getNodeType() != Node.ELEMENT_NODE) {
            	   	  continue;
         	   }
            	   	             	
                activityElement = (Element)sequenceElements.item(i); 
                Activity activity = xml2Activity(activityElement);
                if (activity != null) { 
                		sequence.getActivities().add(activity);               	
                }
            }
         }
         
         setStandardAttributes(sequenceElement, sequence);
 		
 		return sequence;
 	}
 
 	/**
 	 * Converts an XML empty element to a BPEL Empty object.
 	 */
 	protected Activity xml2Empty(Element emptyElement) {
 		Empty empty = BPELFactory.eINSTANCE.createEmpty();
 		empty.setElement(emptyElement);
 		
 		setStandardAttributes(emptyElement, empty);
 		 
     	return empty;
 	}
 
 	/**
 	 * Converts an XML opaqueActivity element to a BPEL OpaqueActivity object.
 	 */
 	protected Activity xml2OpaqueActivity(Element opaqueActivityElement) {
 		OpaqueActivity opaqueActivity = BPELFactory.eINSTANCE.createOpaqueActivity();
 		opaqueActivity.setElement(opaqueActivityElement);
 		
 		setStandardAttributes(opaqueActivityElement, opaqueActivity);
 		 
     	return opaqueActivity;
 	}
 
 	/**
 	 * Converts an XML valdateXML element to a BPEL ValidateXML object.
 	 */
 	protected Activity xml2Validate(Element validateElement) {
 		final Validate validate = BPELFactory.eINSTANCE.createValidate();
 		validate.setElement(validateElement);
 		
 		setStandardAttributes(validateElement, validate);
 		if (validateElement.hasAttribute("variables")) {
 			String variables = validateElement.getAttribute("variables");
 			StringTokenizer st = new StringTokenizer(variables);
 
 			while (st.hasMoreTokens()) {
 				final String variableName = st.nextToken();
 				// We must do this as a post load runnable because the variable might not
 				// exist yet.
 				process.getPostLoadRunnables().add(new Runnable() {
 					public void run() {				
 						Variable targetVariable = getVariable(validate, variableName);
 						if (targetVariable == null) {
 							targetVariable = new VariableProxy(resource.getURI(), variableName);
 						}
 						validate.getVariables().add(targetVariable);
 					}
 				});
 			}
 		}
     	return validate;
 	}
 	
 	/**
 	 * Converts an XML rethrow element to a BPEL Rethrow object.
 	 */
 	protected Activity xml2Rethrow(Element rethrowElement) {
 		Rethrow rethrow = BPELFactory.eINSTANCE.createRethrow();
 		rethrow.setElement(rethrowElement);
 		
 		setStandardAttributes(rethrowElement, rethrow);
 		 
     	return rethrow;
 	}
 
 	/**
 	 * Converts an XML extensionactivity element to a BPEL ExtensionActivity object.
 	 */
 	protected Activity xml2ExtensionActivity(Element extensionActivityElement) {
 		// Do not call setStandardAttributes here because extensionActivityElement
 		// doesn't have them.
 
 		// Find the child element.
 		List<Element> nodeList = getChildElements(extensionActivityElement);
 		
 		if (nodeList.size() == 1) {
 			Element child = nodeList.get(0);
 			// We found a child element. Look up a deserializer for this
 			// activity and call it.
 			String localName = child.getLocalName();
 			String namespace = child.getNamespaceURI();
 			QName qname = new QName(namespace, localName);
 			BPELActivityDeserializer deserializer = extensionRegistry.getActivityDeserializer(qname);
 			if (deserializer != null) {
 				// Deserialize the DOM element and return the new Activity
 				Map nsMap = getAllNamespacesForElement((Element)child);
 				Activity activity = deserializer.unmarshall(qname,child,process,nsMap,extensionRegistry,resource.getURI(), this);
 				// Now let's do the standard attributes and elements
 				setStandardAttributes((Element)child, activity);
 				setStandardElements((Element)child, activity);
 				
 				// Don't do extensibility because extensionActivity is not extensible.
 				// If individual extensionActivity subclasses are actually extensible, they
 				// have to do this themselves in their deserializer.
 				return activity;
 			}
 		}
 		// Fallback is to create a new extensionActivity.
 		return BPELFactory.eINSTANCE.createExtensionActivity();
 	}
 
 	
 	/**
 	 * Converts an XML wait element to a BPEL Wait object.
 	 */
 	protected Activity xml2Wait(Element waitElement) {
     	Wait wait = BPELFactory.eINSTANCE.createWait();
     	wait.setElement(waitElement);
 		
 		// Set name
 		Attr name = waitElement.getAttributeNode("name");
 		
 		if (name != null && name.getSpecified()) 
 			wait.setName(name.getValue());
 		
 		// Set for element
 		Element forElement = getBPELChildElementByLocalName(waitElement, "for");
 		if (forElement != null) {
 			Expression expression = xml2Expression(forElement);
 			wait.setFor(expression);
 		}
 		
 		// Set until element
 		Element untilElement = getBPELChildElementByLocalName(waitElement, "until");
 		if (untilElement != null) {
 			Expression expression = xml2Expression(untilElement);
 			wait.setUntil(expression);
 		}
 		
 		setStandardAttributes(waitElement, wait);
 			
 		return wait;						
 	}
 
 	/**
 	 * Converts an XML exit element to a BPEL Exit object.
 	 */
 	protected Activity xml2Exit(Element exitElement) {
     	Exit exit = BPELFactory.eINSTANCE.createExit();
     	exit.setElement(exitElement);
     	
 		Attr name = exitElement.getAttributeNode("name");
 		
 		if (name != null && name.getSpecified())
 			exit.setName(name.getValue());
 		
 		setStandardAttributes(exitElement, exit);
 			
 		return exit;
 	}
 
 	/**
 	 * Converts an XML throw element to a BPEL Throw object.
 	 */
 	protected Activity xml2Throw(Element throwElement) {
 		Throw _throw = BPELFactory.eINSTANCE.createThrow();
 		_throw.setElement(throwElement);			
 		
 		if (throwElement.hasAttribute("name")) {
 			_throw.setName(throwElement.getAttribute("name"));
 		}
 		if (throwElement.hasAttribute("faultName")) {
 			QName qName = BPELUtils.createAttributeValue(throwElement, "faultName");	
 			_throw.setFaultName(qName);
 		}
 
 		// Set fault variable name
 		setVariable(throwElement, _throw, "faultVariable", BPELPackage.eINSTANCE.getThrow_FaultVariable());
 		
 		setStandardAttributes(throwElement, _throw);
 		
 		return _throw;	
 	}
 
 	/**
 	 * Converts an XML assign element to a BPEL Assign object.
 	 */
 	protected Activity xml2Assign(Element assignElement) {
 		Assign assign = BPELFactory.eINSTANCE.createAssign();
 		assign.setElement(assignElement);			
         
 		if (assignElement.hasAttribute("validate"))
 			assign.setValidate(new Boolean(assignElement.getAttribute("validate").equals("yes")));
 
 		List copies = getBPELChildElementsByLocalName(assignElement, "copy");
         for (int i = 0; i < copies.size(); i++) {
             Copy copy = xml2Copy((Element) copies.get(i));
             assign.getCopy().add(copy);
         }
         
         setStandardAttributes(assignElement, assign);
 
 		return assign;
 	}
 
 	/**
 	 * Converts an XML copy element to a BPEL Copy object.
 	 */
 	protected Copy xml2Copy(Element copyElement) {
 		Copy copy = BPELFactory.eINSTANCE.createCopy();
 		copy.setElement(copyElement);
         
 
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(copy, copyElement);
 
         Element fromElement = getBPELChildElementByLocalName(copyElement, "from");
         if (fromElement != null) {
             From from = BPELFactory.eINSTANCE.createFrom();
             from.setElement(fromElement);
             
             xml2From(from, fromElement); 
             copy.setFrom(from);
         }
         
         Element toElement = getBPELChildElementByLocalName(copyElement, "to");
         if (toElement != null) {
             To to = BPELFactory.eINSTANCE.createTo();
             to.setElement(toElement);
             
             xml2To(to, toElement); 
             copy.setTo(to);
         }
  
 		if (copyElement.hasAttribute("keepSrcElementName"))
 			copy.setKeepSrcElementName(new Boolean(copyElement.getAttribute("keepSrcElementName").equals("yes")));
 		
 		if (copyElement.hasAttribute("ignoreMissingFromData")) 
 			copy.setIgnoreMissingFromData(new Boolean(copyElement.getAttribute("ignoreMissingFromData").equals("yes")));
 
 		xml2ExtensibleElement(copy, copyElement);
  		
 		return copy;
 	}
 
 	/**
 	 * Converts an XML toPart element to a BPEL ToPart object.
 	 */
 	protected ToPart xml2ToPart(Element toPartElement) {
 		ToPart toPart = BPELFactory.eINSTANCE.createToPart();
 		
 		toPart.setElement(toPartElement);        
 
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(toPart, toPartElement);
 
 		// Handle part attribute
 		if (toPartElement.hasAttribute("part")) 
 			toPart.setPart(toPartElement.getAttribute("part"));
 
 		// Handle from-spec
         Element fromElement = getBPELChildElementByLocalName(toPartElement, "from");
         if (fromElement != null) {
             From from = BPELFactory.eINSTANCE.createFrom();
             from.setElement(fromElement);
             
             xml2From(from, fromElement); 
             toPart.setFrom(from);
         }
         
         
 		return toPart;
 	}
 
 	/**
 	 * Converts an XML fromPart element to a BPEL FromPart object.
 	 */
 	protected FromPart xml2FromPart(Element fromPartElement) {
 		FromPart fromPart = BPELFactory.eINSTANCE.createFromPart();
 		fromPart.setElement(fromPartElement);		       
 
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(fromPart, fromPartElement);
 
 		// Handle part attribute
 		if (fromPartElement.hasAttribute("part")) 
 			fromPart.setPart(fromPartElement.getAttribute("part"));
 
 		// Handle to-spec
 		Element toElement = getBPELChildElementByLocalName(fromPartElement, "to");
         if (toElement != null) {
             To to = BPELFactory.eINSTANCE.createTo();
             to.setElement(toElement);
             
             xml2To(to, toElement); 
             fromPart.setTo(to);
         }
         
 		return fromPart;
 	}
 
 	/**
 	 * Converts an XML "to" element to a BPEL To object.
 	 */
 	protected void xml2To(To to, Element toElement) {
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(to, toElement);
 		
 		// Set variable
 		Attr variable = toElement.getAttributeNode("variable"); 
     
 		if (variable != null && variable.getSpecified()) {				
 			setVariable(toElement, to, "variable", BPELPackage.eINSTANCE.getTo_Variable());
 		}
 
 		// Set part
 		Attr part = toElement.getAttributeNode("part"); 		
     
 		if (part != null && part.getSpecified()) {		
 			final String partAttr = toElement.getAttribute("part");
             ((ToImpl) to).setPartName(partAttr);
 		}
 
 		// Set partnerLink			
 		Attr partnerLink = toElement.getAttributeNode("partnerLink");			
 		
 		if (partnerLink != null && partnerLink.getSpecified()) {
 			setPartnerLink(toElement, to, BPELPackage.eINSTANCE.getTo_PartnerLink());
 		}
 
 		// Set property		
 		Attr property = toElement.getAttributeNode("property");
      		
 		if (property != null && property.getSpecified()) {
 			setProperties(toElement, to, "property");
 		}
 
 		// Set query element
 		Element queryElement = getBPELChildElementByLocalName(toElement, "query");
 		if (queryElement != null) {
 			Query queryObject = BPELFactory.eINSTANCE.createQuery();
 			
 			queryObject.setElement(queryElement);
 			to.setQuery(queryObject);
 			
 			// Set queryLanguage
 			if (queryElement.hasAttribute("queryLanguage")) {
 				String queryLanguage = queryElement.getAttribute("queryLanguage");
 				queryObject.setQueryLanguage(queryLanguage);
 			}
 
 			// Set query text
 			// Get the condition text
 			String data = "";
 			Node node = queryElement.getFirstChild();
 			boolean containsValidData = false;
 			while (node != null) {
 				if (node.getNodeType() == Node.TEXT_NODE) {
 					Text text = (Text)node;
 					data += text.getData();
 				} else if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
 					data="";
 					do {
 						CDATASection cdata = (CDATASection) node;
 						data += cdata.getData();
 						node = node.getNextSibling();
 						containsValidData = true;
 					} while (node != null && node.getNodeType() == Node.CDATA_SECTION_NODE);
 					break;
 				}
 				node = node.getNextSibling();
 			}
 			if (!containsValidData) {
 				for (int i = 0; i < data.length(); i++) {
 					char charData = data.charAt(i);
 					if (charData == '\n' || Character.isWhitespace(charData)){}//ignore
 					else { //valid data
 						containsValidData = true;
 						break;
 					}
 				}
 			}
 			
 			if (containsValidData) {
 				queryObject.setValue(data);
 			}
 		}
 	}
 
 	/**
 	 * Converts an XML "from" element to a BPEL From object.
 	 */
 	protected void xml2From(From from, Element fromElement) {
 		xml2To(from,fromElement);
 		
 		Attr endpointReference = fromElement.getAttributeNode("endpointReference");
     
 		if (endpointReference != null && endpointReference.getSpecified()) {
 			from.setEndpointReference(EndpointReferenceRole.get(endpointReference.getValue()));
 		}
 		
 		// Set service-ref element		
 		Element serviceRefElement = getBPELChildElementByLocalName(fromElement, "service-ref");
 		Element literalElement = getBPELChildElementByLocalName(fromElement, "literal");
 		
 		
 		if (serviceRefElement != null) {
 					
 			ServiceRef serviceRef = BPELFactory.eINSTANCE.createServiceRef();
 			// TODO: ? serviceRef.setElement(serviceRefElement);
 			
 			from.setServiceRef(serviceRef);
 			
 			// Set reference scheme
 			if (serviceRefElement.hasAttribute("reference-scheme")) {
 				String scheme = serviceRefElement.getAttribute("reference-scheme");
 				serviceRef.setReferenceScheme(scheme);
 			}
 			
 			// Set the value of the service reference
 
 			// Determine whether or not there is an element in the child list.
 			Node candidateChild = null;
 			NodeList nodeList = serviceRefElement.getChildNodes();
 			int length = nodeList.getLength();
 			for (int i = 0; i < length; i++) {
 				Node child = nodeList.item(i);
 				if (child.getNodeType() == Node.ELEMENT_NODE) {
 					candidateChild = child;
 					break;
 				}
 			}
 			if (candidateChild == null) {
 				candidateChild = serviceRefElement.getFirstChild();
 			}
 			String data = getText(candidateChild);
 			
 			if (data == null) {
 				// No text or CDATA node. If it's an element node, then
 				// deserialize and install.
 				if (candidateChild != null && candidateChild.getNodeType() == Node.ELEMENT_NODE) {
 					// Look if there's an ExtensibilityElement deserializer for this element
 					Element childElement = (Element)candidateChild;
 					QName qname = new QName(childElement.getNamespaceURI(), childElement.getLocalName());
 					BPELExtensionDeserializer deserializer=null;
 					try {
 						deserializer = (BPELExtensionDeserializer)extensionRegistry.queryDeserializer(ExtensibleElement.class,qname);
 					} catch (WSDLException e) {}
 					if (deserializer != null && !(deserializer instanceof BPELUnknownExtensionDeserializer)) {
 						// Deserialize the DOM element and add the new Extensibility element to the parent
 						// ExtensibleElement
 						try {
 							Map nsMap = getAllNamespacesForElement(serviceRefElement);
 							ExtensibilityElement extensibilityElement=deserializer.unmarshall(ExtensibleElement.class,qname,childElement,process,nsMap,extensionRegistry,resource.getURI());
 							serviceRef.setValue(extensibilityElement);
 						} catch (WSDLException e) {
 							throw new WrappedException(e);
 						}
 					} else {
 						ServiceReferenceDeserializer referenceDeserializer = extensionRegistry.getServiceReferenceDeserializer(serviceRef.getReferenceScheme());
 						if (referenceDeserializer != null) {
 							Object serviceReference = referenceDeserializer.unmarshall(childElement, process);
 							serviceRef.setValue(serviceReference);
 						}
 					}
 				}
 			} else {
 				serviceRef.setValue(data);
 			}
 						
 		} 
 		
 		
 		// Literal node
 		if (literalElement != null) {
 			
			String elementData = BPELUtils.elementToString(literalElement);
			from.setUnsafeLiteral(Boolean.FALSE);
 			
			if (isEmptyOrWhitespace(elementData) == false) {
 				from.setUnsafeLiteral(Boolean.TRUE);
				from.setLiteral( elementData );
 			}
 			
 		} else {		
 		
 			// must be expression
 			Expression expressionObject = BPELFactory.eINSTANCE.createExpression();
 			expressionObject.setElement(fromElement);
 			
 			from.setExpression(expressionObject);
 			
 			// Set expressionLanguage
 			if (fromElement.hasAttribute("expressionLanguage")) {
 				expressionObject.setExpressionLanguage(fromElement.getAttribute("expressionLanguage"));
 			}
 
 			// Set expression text
 			// Get the condition text
 			String data = slurpTextualNodes ( fromElement );
 			if (isEmptyOrWhitespace(data) == false) {			
 				expressionObject.setBody(data);
 			}								
 		}
 
 		// Set opaque
 		Attr opaque = fromElement.getAttributeNode("opaque");
 			
 		if (opaque != null && opaque.getSpecified()) {
 			from.setOpaque(new Boolean(opaque.getValue().equals("yes")));
 		}
 
 		
 		// See if there is an xsi:type attribue.
 		if (fromElement.hasAttribute("xsi:type")) {
 			QName qName = BPELUtils.createAttributeValue(fromElement, "xsi:type");
 			XSDTypeDefinition type = new XSDTypeDefinitionProxy(resource.getURI(), qName);
 			from.setType(type);						
 		}
 	}
 
 
 	/**
 	 * Converts an XML import element to a BPEL Import object.
 	 */
 	protected Import xml2Import(Element importElement) {
 		if (!importElement.getLocalName().equals("import"))
 			return null;
 			
 		Import imp = BPELFactory.eINSTANCE.createImport();
 		imp.setElement(importElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(imp, importElement);
 		
 		// namespace
 		if (importElement.hasAttribute("namespace"))
 			imp.setNamespace(importElement.getAttribute("namespace"));
 		
 		// location
 		if (importElement.hasAttribute("location"))
 			imp.setLocation(importElement.getAttribute("location"));
 		
 		// importType
 		if (importElement.hasAttribute("importType"))
 			imp.setImportType(importElement.getAttribute("importType"));
 
 		return imp;					
 	}
 
 
 	/**
 	 * Converts an XML invoke element to a BPEL Invoke object.
 	 */
 	protected Activity xml2Invoke(Element invokeElement) {
 		Invoke invoke = BPELFactory.eINSTANCE.createInvoke();
 		invoke.setElement(invokeElement);			
 		
 		// Set several parms
 		setStandardAttributes(invokeElement, invoke);
 		setOperationParms(invokeElement, invoke, null, BPELPackage.eINSTANCE.getInvoke_InputVariable(), BPELPackage.eINSTANCE.getInvoke_OutputVariable(), BPELPackage.eINSTANCE.getPartnerActivity_PartnerLink());
 
 		// Set compensationHandler
 		setCompensationHandler(invokeElement, invoke);
 		
 		// Set the fault handler (for catche-s and catchAll-s)
 		FaultHandler faultHandler = xml2FaultHandler(invokeElement);
 		if (faultHandler != null && (!faultHandler.getCatch().isEmpty() ||  faultHandler.getCatchAll() != null)) {
 			// Only set this on the activity if there is at least one catch clause, or a catchAll clause
 			invoke.setFaultHandler(faultHandler);
 		}
 
 		// Set the ToPart		
 		for(Element e : getBPELChildElementsByLocalName(invokeElement, "toPart") ) {			
 			invoke.getToPart().add( xml2ToPart(e) );
 		}
 		// Set the FromPart
 		for(Element e : getBPELChildElementsByLocalName(invokeElement, "fromPart")) {
 			invoke.getFromPart().add( xml2FromPart(e) );
 		}		
 		return invoke;
 	}
 
 
 	/**
 	 * Converts an XML reply element to a BPEL Reply object.
 	 */
 	protected Activity xml2Reply(Element replyElement) {
 		Reply reply = BPELFactory.eINSTANCE.createReply();
 		reply.setElement(replyElement);			
 		
 		// Set several parms
 		setStandardAttributes(replyElement, reply);
 		setOperationParms(replyElement, reply, BPELPackage.eINSTANCE.getReply_Variable(), null, null, BPELPackage.eINSTANCE.getPartnerActivity_PartnerLink());
 
 		if (replyElement.hasAttribute("faultName")) {
 			QName qName = BPELUtils.createAttributeValue(replyElement, "faultName");	
 			reply.setFaultName(qName);
 		}
 
 		// Set the ToPart
 		for(Element e :  getBPELChildElementsByLocalName(replyElement, "toPart")) {
 			reply.getToPart().add( xml2ToPart ( e ));
 		}
 		
 		return reply;		
 	}
      
      
 	/**
 	 * Converts an XML receive element to a BPEL Receive object.
 	 */
 	protected Activity xml2Receive(Element receiveElement) {
 		Receive receive = BPELFactory.eINSTANCE.createReceive();
 		receive.setElement(receiveElement);				
 	
 		// Set several parms
 		setStandardAttributes(receiveElement, receive);
 		setOperationParms(receiveElement, receive, BPELPackage.eINSTANCE.getReceive_Variable(), null, null, BPELPackage.eINSTANCE.getPartnerActivity_PartnerLink());
 
 		// Set createInstance
 		if (receiveElement.hasAttribute("createInstance")) {		           
 			String createInstance = receiveElement.getAttribute("createInstance");
 			receive.setCreateInstance(new Boolean(createInstance.equals("yes")));
 		}
 
 		// Set the FromPart
 		for(Element e : getBPELChildElementsByLocalName(receiveElement, "fromPart")) {
 			receive.getFromPart().add( xml2FromPart (e) );
 		}		
 		
 		return receive;
 	}
 	
 	/**
 	 * Converts an XML forEach element to a BPEL ForEach object.
 	 */
 	protected Activity xml2ForEach(Element forEachElement) {
 		ForEach forEach = BPELFactory.eINSTANCE.createForEach();
 		forEach.setElement(forEachElement);			
 		
 		// Set several parms
 		setStandardAttributes(forEachElement, forEach);
 
 		if (forEachElement.hasAttribute("parallel")) {
 			process.setSuppressJoinFailure(new Boolean(forEachElement.getAttribute("parallel").equals("yes")));
 		}
 
 		// Set counterName variable
 		if (forEachElement.hasAttribute("counterName")) {
 			Variable variable = BPELFactory.eINSTANCE.createVariable();
 			// TODO: How to facade this ?
 			variable.setName(forEachElement.getAttribute("counterName"));
 			QName qName = new QName(XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001, "unsignedInt");
 			XSDTypeDefinition type = new XSDTypeDefinitionProxy(resource.getURI(), qName);
 			variable.setType(type);
 			forEach.setCounterName(variable);					
 		}		
 
 		// Set startCounterValue element
 		Element startCounterValueElement = getBPELChildElementByLocalName(forEachElement, "startCounterValue");
 		if (startCounterValueElement != null) {
 			Expression expression = xml2Expression(startCounterValueElement);
 			forEach.setStartCounterValue(expression);
 		}
 		
 		// Set finalCounterValue element
 		Element finalCounterValueElement = getBPELChildElementByLocalName(forEachElement, "finalCounterValue");
 		if (finalCounterValueElement != null) {
 			Expression expression = xml2Expression(finalCounterValueElement);
 			forEach.setFinalCounterValue(expression);
 		}
 		
 		// Set completionCondition element
 		Element completionConditionElement = getBPELChildElementByLocalName(forEachElement, "completionCondition");
 		if (completionConditionElement != null) {
 			CompletionCondition completionCondition = xml2CompletionCondition(completionConditionElement);
 			forEach.setCompletionCondition(completionCondition);
 		}
 
 		// Set activity
 		Activity activity = getChildActivity(forEachElement);
 		if (activity instanceof Scope) {
 			forEach.setActivity(activity);
 		}
 				
 		return forEach;
 	}
 
 	/**
 	 * Converts an XML completionCondition element to a BPEL CompletionCondition object.
 	 */
 	protected CompletionCondition xml2CompletionCondition(Element completionConditionElement) {
 		CompletionCondition completionCondition = BPELFactory.eINSTANCE.createCompletionCondition();
 		completionCondition.setElement(completionConditionElement);			
 		
 		// Set branches element
 		Element branchesElement = getBPELChildElementByLocalName(completionConditionElement, "branches");
 		if (branchesElement != null) {
 			Branches branches = xml2Branches(branchesElement);
 			completionCondition.setBranches(branches);
 		}
 		
 		return completionCondition;
 	}
 
 	/**
 	 * Converts an XML branches element to a BPEL Branches object.
 	 */
 	protected Branches xml2Branches(Element branchesElement) {
 		Branches branches = BPELFactory.eINSTANCE.createBranches();
 		branches.setElement(branchesElement);
 		
 		xml2Expression(branchesElement, branches);
 
 		if (branchesElement.hasAttribute("successfulBranchesOnly"))
 			branches.setCountCompletedBranchesOnly(new Boolean(branchesElement.getAttribute("successfulBranchesOnly").equals("yes")));
 
 		return branches;
 	}
 
 	/**
 	 * Converts an XML documentation element to a BPEL Documentation object.
 	 */
 	protected Documentation xml2Documentation(Element documentationElement) {
 		Documentation documentation = BPELFactory.eINSTANCE.createDocumentation();
 		// TODO: Facade ? 
 		// documentation.setElement(// documentationElement);
 		
 		if (documentationElement.hasAttribute("lang"))
 			documentation.setLang(documentationElement.getAttribute("lang"));
 		if (documentationElement.hasAttribute("source"))
 			documentation.setSource(documentationElement.getAttribute("source"));
     	Node textNode = documentationElement.getFirstChild();
     	if (textNode != null) {
 		    String text = getText(textNode);
 		    if (text != null)
 		        documentation.setValue(text);
     	}
 
 		return documentation;
 	}
 
 	/**
 	 * Converts an XML repeatUntil element to a BPEL RepeatUntil object.
 	 */
 	protected Activity xml2RepeatUntil(Element repeatUntilElement) {
 		RepeatUntil repeatUntil = BPELFactory.eINSTANCE.createRepeatUntil();
 		repeatUntil.setElement(repeatUntilElement);		
 		
 		// Set several parms
 		setStandardAttributes(repeatUntilElement, repeatUntil);
 
 		// Handle condition element
 		Element conditionElement = getBPELChildElementByLocalName(repeatUntilElement, "condition");
 		if (conditionElement != null) {
 			Condition condition = xml2Condition(conditionElement);
 			repeatUntil.setCondition(condition);
 		}
 
         NodeList repeatUntilElements = repeatUntilElement.getChildNodes();
         
         Element activityElement = null;
 
 		if (repeatUntilElements != null && repeatUntilElements.getLength() > 0) {
 			for (int i = 0; i < repeatUntilElements.getLength(); i++) {
 				
 				if (repeatUntilElements.item(i).getNodeType() != Node.ELEMENT_NODE) {
            	   		continue;
 				}
            	   	  			
 				activityElement = (Element)repeatUntilElements.item(i); 
             	Activity activity = xml2Activity(activityElement);
             	if (activity != null) {
             		repeatUntil.setActivity(activity);
             		break;
             	}
 			}
         }
         
 		return repeatUntil;
 	}
 
 	protected Correlations xml2Correlations(Element correlationsElement) {
 		if (!correlationsElement.getLocalName().equals("correlations"))
 			return null;
 			
 		Correlations correlations = BPELFactory.eINSTANCE.createCorrelations();
 		correlations.setElement(correlationsElement);
 		
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(correlations, correlationsElement);
 		
 		for(Element e : getBPELChildElementsByLocalName(correlationsElement, "correlation")) {
 			correlations.getChildren().add( xml2Correlation(e));			
 		}
 		
 		// extensibility elements
 		xml2ExtensibleElement(correlations, correlationsElement);
 		
 		return correlations;
 	}		
 	
 	/**
 	 * Converts an XML correlation element to a BPEL Correlation object.
 	 */
 	protected Correlation xml2Correlation(Element correlationElement) {
     	final Correlation correlation = BPELFactory.eINSTANCE.createCorrelation();
     	correlation.setElement(correlationElement);
 
 		// Save all the references to external namespaces		
 		saveNamespacePrefix(correlation, correlationElement);
     	
 		if (correlationElement == null) return correlation;
 
 		// Set set
 		if (correlationElement.hasAttribute("set")) {
 			final String correlationSetName = correlationElement.getAttribute("set");
 			process.getPostLoadRunnables().add(new Runnable() {
 				public void run() {	
 					CorrelationSet cSet = BPELUtils.getCorrelationSetForActivity(correlation, correlationSetName);
 					if (cSet == null) {
 						cSet = new CorrelationSetProxy(resource.getURI(), correlationSetName);
 					}
 					correlation.setSet(cSet);								
 				}
 			});		
 		}
 
 		
 		// Set initiation
 		Attr initiation = correlationElement.getAttributeNode("initiate");
 		if (initiation != null && initiation.getSpecified()) {
 			if (initiation.getValue().equals("yes"))
 				correlation.setInitiate("yes");
 			else if (initiation.getValue().equals("no"))
 				correlation.setInitiate("no");
 			else if (initiation.getValue().equals("join"))
 				correlation.setInitiate("join");
 		}
 			
 		// Set pattern
 		Attr pattern = correlationElement.getAttributeNode("pattern");
 
 		if (pattern != null && pattern.getSpecified()) {
 			if (pattern.getValue().equals("in"))
 				correlation.setPattern(CorrelationPattern.IN_LITERAL);
 			else if (pattern.getValue().equals("out"))
 					correlation.setPattern(CorrelationPattern.OUT_LITERAL);
 				else if (pattern.getValue().equals("out-in"))
 					correlation.setPattern(CorrelationPattern.OUTIN_LITERAL);			
 		}
 		
 		xml2ExtensibleElement(correlation, correlationElement);
 		
 		return correlation;
 	}
 	
 	protected Compensate xml2Compensate(Element compensateElement) {
 		final Compensate compensate = BPELFactory.eINSTANCE.createCompensate();
 		compensate.setElement(compensateElement);
 		setStandardAttributes(compensateElement, compensate);		
 		return compensate;
 	}
 	
 	
 	protected CompensateScope xml2CompensateScope (Element compensateScopeElement) {
 		
 		final CompensateScope compensateScope = BPELFactory.eINSTANCE.createCompensateScope();
 		compensateScope.setElement(compensateScopeElement);
 		
 		final String target = compensateScopeElement.getAttribute("target");
 		
 		if (target != null && target.length() > 0) {
 			process.getPostLoadRunnables().add(new Runnable() {
 				public void run() {
 					compensateScope.setTarget(target);
 				}
 			});
 		}
 
 		setStandardAttributes(compensateScopeElement, compensateScope);
 		
 		return compensateScope;
 	}
 	
 	/**
 	 * Converts an XML extensible element to a BPEL extensible element
 	 */
 	
 	protected void xml2ExtensibleElement(ExtensibleElement extensibleElement, Element element) {
 		if (extensionRegistry==null)
 			return;
 			
 		// Handle the documentation element first
 		Element documentationElement = getBPELChildElementByLocalName(element, "documentation");
 		if (documentationElement != null) {
 			Documentation documentation = xml2Documentation(documentationElement);
 			extensibleElement.setDocumentation(documentation);
 		}
 		
 		// Get the child nodes, elements and attributes
 		List nodes=new ArrayList();
 		NodeList nodeList=element.getChildNodes();
 		for (int i=0, n=nodeList.getLength(); i<n; i++) {
 			if (nodeList.item(i) instanceof Element) {
 				final String namespaceURI = ((Element)nodeList.item(i)).getNamespaceURI();
 				if (!(BPELConstants.isBPELNamespace(namespaceURI)))
 					nodes.add(nodeList.item(i)); 
 			}
 		}
 		
 		NamedNodeMap nodeMap=element.getAttributes();
 		for (int i=0, n=nodeMap.getLength(); i<n; i++) {
 			Attr attr = (Attr)nodeMap.item(i);
 			if (attr.getNamespaceURI() != null && !attr.getNamespaceURI().equals(XSDConstants.XMLNS_URI_2000)) {
 				nodes.add(attr);	
 			}
 		}
 		
 		for (int i=0, n=nodes.size(); i<n; i++) {
 			Node node=(Node)nodes.get(i);
 			
 			// TODO What is this check for? If we're actually checking for
 			// the BPEL namespace, use BPELConstants instead.
 			if (MessagepropertiesConstants.isMessagePropertiesNamespace(node.getNamespaceURI()))
 				continue;
 				
 			// Handle extensibility element
 			if (node.getNodeType()==Node.ELEMENT_NODE) {
 					
 				// Look if there's an ExtensibilityElement deserializer for this element
 				Element childElement=(Element)node;
 				QName qname=new QName(childElement.getNamespaceURI(),childElement.getLocalName());
 				BPELExtensionDeserializer deserializer=null;
 				try {
 					deserializer=(BPELExtensionDeserializer)extensionRegistry.queryDeserializer(ExtensibleElement.class,qname);
 				} catch (WSDLException e) {}
 				if (deserializer!=null) {
 					
 					// Deserialize the DOM element and add the new Extensibility element to the parent
 					// ExtensibleElement
 					try {
 						Map nsMap = getAllNamespacesForElement(element);
 						//ExtensibilityElement extensibilityElement=deserializer.unmarshall(ExtensibleElement.class,qname,childElement,process,nsMap,extensionRegistry,resource.getURI());
 						ExtensibilityElement extensibilityElement=deserializer.unmarshall(extensibleElement.getClass(),qname,childElement,process,nsMap,extensionRegistry,resource.getURI());
 						extensibleElement.addExtensibilityElement(extensibilityElement);
 					} catch (WSDLException e) {
 						throw new WrappedException(e);
 					}
 				}
 			} else if (node.getNodeType()==Node.ATTRIBUTE_NODE) {
 				// If the attribute is not actually in the file, ignore it.
 				// (default attributes added by the schema parser, cause some problems for us)
 				if ((node instanceof Attr) && ((Attr)node).getSpecified()) {
 					// Handle extensibility attribute
 					QName qname=new QName(node.getNamespaceURI(),"extensibilityAttributes");
 					BPELExtensionDeserializer deserializer=null;
 					try {
 						deserializer=(BPELExtensionDeserializer)extensionRegistry.queryDeserializer(ExtensibleElement.class,qname);
 					} catch (WSDLException e) {}
 					if (deserializer!=null) {
 						
 						// Create a temp element to host the extensibility attribute
 	                    // 
 	                    // This turns something that looks like this:
 	                    //   <bpws:X someNS:Y="Z"/>
 	                    // into something that looks like this:
 	                    //   <someNS:extensibilityAttributes xmlns:someNS="http://the.namespace" Y="Z"/>
 	                    
 						Element tempElement=element.getOwnerDocument().createElementNS(node.getNamespaceURI(), node.getPrefix() + ":extensibilityAttributes");
 	                    tempElement.setAttribute(BPELUtils.ATTR_XMLNS + ":" + node.getPrefix(), node.getNamespaceURI());
 						tempElement.setAttribute(node.getLocalName(), node.getNodeValue());
 						
 						// Deserialize the temp DOM element and add the new Extensibility element to the parent
 						// ExtensibleElement
 						try {
 							Map nsMap = getAllNamespacesForElement(element);
 							ExtensibilityElement extensibilityElement=deserializer.unmarshall(ExtensibleElement.class,qname,tempElement,process,nsMap,extensionRegistry,resource.getURI());
 							if (extensibilityElement!=null)
 								extensibleElement.addExtensibilityElement(extensibilityElement);
 						} catch (WSDLException e) {
 							throw new WrappedException(e);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	
 	
 	
 	
 	String slurpTextualNodes ( Element node ) {
 		
 		StringBuilder sb = new StringBuilder(128);
 		Node n = node.getFirstChild();
 		while (n != null) {
 			switch (n.getNodeType()) {
 			case Node.TEXT_NODE :
 			case Node.CDATA_SECTION_NODE :
 				sb.append( n.getNodeValue() );
 				break;
 			}
 			n = n.getNextSibling();
 		}
 		return sb.toString();
 	}
 	
 
 	/**
      * Returns true if the string is either null or contains just whitespace.
 	 * @param value 
 	 * @return true if empty or whitespace, false otherwise.
      */
 		
    static public boolean isEmptyOrWhitespace( String value )
    {
        if( value == null || value.length() == 0) {
            return true;
        }               
        for( int i = 0, j = value.length(); i < j; i++ )
        {
            if( ! Character.isWhitespace( value.charAt(i) ) ) {
                return false;
            }
        }
        return true;
    }
 	
 	
 	/**
 	 * Helper method to get a string from the given text node or CDATA text node.
 	 */
 	private String getText (Node node) {
 		
 		StringBuilder sb = new StringBuilder(128);
 		
 		boolean containsValidData = false;
 		while (node != null) {
 			if (node.getNodeType() == Node.TEXT_NODE) {
 				Text text = (Text)node;
 				sb.append(text.getData());
 			} else if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
 				sb.setLength(0);
 				do {
 					CDATASection cdata = (CDATASection) node;
 					sb.append(cdata.getData());
 					node = node.getNextSibling();
 					containsValidData = true;
 				} while (node != null && node.getNodeType() == Node.CDATA_SECTION_NODE);
 				break;
 			}
 			node = node.getNextSibling();
 		}
 		
 		if (!containsValidData) {
 			for (int i = 0; i < sb.length(); i++) {
 				char charData = sb.charAt(i);
 				if (charData == '\n' || Character.isWhitespace(charData)){}//ignore
 				else { //valid data
 					containsValidData = true;
 					break;
 				}
 			}
 		}
 		if (containsValidData) {
 			return sb.toString();
 		} else {
 			return null;
 		}
 	}
 
 	public static Variable getVariable(EObject eObject, String variableName) {
 		return VARIABLE_RESOLVER.getVariable(eObject, variableName);
 	}	
 	
 	public static Link getLink(Activity activity, String linkName) {
 		return LINK_RESOLVER.getLink(activity, linkName);
 	}	
 }
