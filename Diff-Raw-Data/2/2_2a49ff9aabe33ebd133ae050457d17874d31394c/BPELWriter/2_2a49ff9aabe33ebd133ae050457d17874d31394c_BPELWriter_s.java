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
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.wsdl.WSDLException;
 import javax.wsdl.extensions.ExtensibilityElement;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.xerces.util.DOMUtil;
 import org.eclipse.bpel.model.Activity;
 import org.eclipse.bpel.model.Assign;
 import org.eclipse.bpel.model.BPELFactory;
 import org.eclipse.bpel.model.BPELPackage;
 import org.eclipse.bpel.model.Case;
 import org.eclipse.bpel.model.Catch;
 import org.eclipse.bpel.model.CatchAll;
 import org.eclipse.bpel.model.Compensate;
 import org.eclipse.bpel.model.CompensationHandler;
 import org.eclipse.bpel.model.Condition;
 import org.eclipse.bpel.model.Copy;
 import org.eclipse.bpel.model.Correlation;
 import org.eclipse.bpel.model.CorrelationSet;
 import org.eclipse.bpel.model.CorrelationSets;
 import org.eclipse.bpel.model.Correlations;
 import org.eclipse.bpel.model.Else;
 import org.eclipse.bpel.model.ElseIf;
 import org.eclipse.bpel.model.Empty;
 import org.eclipse.bpel.model.EventHandler;
 import org.eclipse.bpel.model.Exit;
 import org.eclipse.bpel.model.Expression;
 import org.eclipse.bpel.model.Extension;
 import org.eclipse.bpel.model.ExtensionActivity;
 import org.eclipse.bpel.model.Extensions;
 import org.eclipse.bpel.model.FaultHandler;
 import org.eclipse.bpel.model.Flow;
 import org.eclipse.bpel.model.ForEach;
 import org.eclipse.bpel.model.From;
 import org.eclipse.bpel.model.FromPart;
 import org.eclipse.bpel.model.If;
 import org.eclipse.bpel.model.Import;
 import org.eclipse.bpel.model.Invoke;
 import org.eclipse.bpel.model.Link;
 import org.eclipse.bpel.model.Links;
 import org.eclipse.bpel.model.OnAlarm;
 import org.eclipse.bpel.model.OnEvent;
 import org.eclipse.bpel.model.OnMessage;
 import org.eclipse.bpel.model.OpaqueActivity;
 import org.eclipse.bpel.model.Otherwise;
 import org.eclipse.bpel.model.PartnerLink;
 import org.eclipse.bpel.model.PartnerLinks;
 import org.eclipse.bpel.model.Pick;
 import org.eclipse.bpel.model.Process;
 import org.eclipse.bpel.model.Query;
 import org.eclipse.bpel.model.Receive;
 import org.eclipse.bpel.model.RepeatUntil;
 import org.eclipse.bpel.model.Reply;
 import org.eclipse.bpel.model.Rethrow;
 import org.eclipse.bpel.model.Scope;
 import org.eclipse.bpel.model.Sequence;
 import org.eclipse.bpel.model.ServiceRef;
 import org.eclipse.bpel.model.Source;
 import org.eclipse.bpel.model.Sources;
 import org.eclipse.bpel.model.Switch;
 import org.eclipse.bpel.model.Target;
 import org.eclipse.bpel.model.Targets;
 import org.eclipse.bpel.model.TerminationHandler;
 import org.eclipse.bpel.model.Then;
 import org.eclipse.bpel.model.Throw;
 import org.eclipse.bpel.model.To;
 import org.eclipse.bpel.model.ToPart;
 import org.eclipse.bpel.model.ValidateXML;
 import org.eclipse.bpel.model.Variable;
 import org.eclipse.bpel.model.Variables;
 import org.eclipse.bpel.model.Wait;
 import org.eclipse.bpel.model.While;
 import org.eclipse.bpel.model.extensions.BPELExtensionRegistry;
 import org.eclipse.bpel.model.extensions.BPELExtensionSerializer;
 import org.eclipse.bpel.model.extensions.ServiceReferenceSerializer;
 import org.eclipse.bpel.model.messageproperties.Property;
 import org.eclipse.bpel.model.partnerlinktype.PartnerLinkType;
 import org.eclipse.bpel.model.partnerlinktype.Role;
 import org.eclipse.bpel.model.proxy.IBPELServicesProxy;
 import org.eclipse.bpel.model.reordering.extensions.ExtensionFactory;
 import org.eclipse.bpel.model.util.BPELConstants;
 import org.eclipse.bpel.model.util.BPELServicesUtility;
 import org.eclipse.bpel.model.util.BPELUtils;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.wst.wsdl.Definition;
 import org.eclipse.wst.wsdl.ExtensibleElement;
 import org.eclipse.wst.wsdl.Message;
 import org.eclipse.wst.wsdl.Operation;
 import org.eclipse.wst.wsdl.util.WSDLConstants;
 import org.eclipse.wst.wsdl.util.WSDLResourceImpl;
 import org.eclipse.xsd.XSDElementDeclaration;
 import org.eclipse.xsd.XSDNamedComponent;
 import org.eclipse.xsd.XSDTypeDefinition;
 import org.eclipse.xsd.util.XSDConstants;
 import org.eclipse.xsd.util.XSDResourceImpl;
 import org.w3c.dom.Attr;
 import org.w3c.dom.CDATASection;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 
 /**
  * BPELWriter is responsible for serializing the BPEL EMF model to an output stream.
  */
 public class BPELWriter {
 
 	private Document document = null;
 	private BPELResource bpelResource = null;
 	private WsdlImportsManager wsdlNamespacePrefixManager;
 	private NamespacePrefixManager bpelNamespacePrefixManager;
 	private List extensibilityElementListHandlers = null;
 	protected BPELPackage bpelPackage = null;
 	
 	private BPELExtensionRegistry extensionRegistry = BPELExtensionRegistry.getInstance();
 
 	private Process process;
 	
 	public class NamespacePrefixManager implements BPELResourceImpl.MapListener {
 		private Map myNamespacePrefixMap; // for performance, we need to know which prefix to use for a namespace
 		private BPELResource resource;
 		
 		public NamespacePrefixManager(BPELResource resource) {
 			this.resource = resource;
 			myNamespacePrefixMap = new Hashtable();
 			// for performance, just register the process namespace map first
 			Process process = resource.getProcess();
 			BPELResource.NotifierMap nsMap = 
 				(BPELResource.NotifierMap)resource.getPrefixToNamespaceMap(process);
 			myNamespacePrefixMap.put(process, nsMap.reserve());
 			// listen to the process namespace map if any extension model modify this map;
 			nsMap.addListener(this);
             
             if (resource.getOptionUseNSPrefix()) {
                 addNewRootPrefix(BPELConstants.PREFIX, resource.getNamespaceURI());
             }
 		}
 		
 		/**
 		 * add prefix to the root of a bpel, i.e. the process level
 		 */
 		public String addNewRootPrefix(String basePrefix, String namespace) {
 			Process process = resource.getProcess();
 			Map prefixNSMap = (Map)resource.getPrefixToNamespaceMap(process);
 			Map nsPrefixMap = (Map)myNamespacePrefixMap.get(process);
 			
 			if (nsPrefixMap.get(namespace) == null) {
 				// Compute unique prefix for the current namespace
 				int i = 0;
 				String prefix = basePrefix;
 				while (prefixNSMap.containsKey(prefix)) {
 					prefix = basePrefix + i;
 					i++;					
 				}
 				prefixNSMap.put(prefix, namespace);
 				// We will now get notified by the NotifierMap that this prefix and namespace are added,
 				// and will respond to the event there, not below:
 				// myNamespacePrefixMap.put(namespace, testPrefix);
 				return prefix;				
 			}
 			return (String)nsPrefixMap.get(namespace);
 		}
 
         public String getRootPrefix(String namespaceURI) {
             Process process = resource.getProcess();
             Map nsPrefixMap = (Map)myNamespacePrefixMap.get(process);
             return (String) nsPrefixMap.get(namespaceURI);
         }
 
 		public String qNameToString(EObject eObject, QName qname) {
 			BPELResource.NotifierMap prefixNSMap = null;
 			EObject context = eObject;
 			String prefix = null;
 			String namespace = qname.getNamespaceURI();
 				
 			if (namespace != null && namespace.length() > 0) {
 				// Transform BPEL namespaces to the latest version so that references to the old namespace are not serialized.
 				if (BPELUtils.isBPELNamespace(namespace)) {
 					namespace = BPELConstants.NAMESPACE;
 				}
 				while (context != null) {
 					prefixNSMap = resource.getPrefixToNamespaceMap(context);
 					if (!prefixNSMap.isEmpty()) {
 						if (prefixNSMap.containsValue(namespace)) {
 							Map nsPrefixMap = (Map)myNamespacePrefixMap.get(context);
 							if (nsPrefixMap == null) {
 								nsPrefixMap = prefixNSMap.reserve();
 								myNamespacePrefixMap.put(context, nsPrefixMap);
 							}
 							prefix = (String)nsPrefixMap.get(namespace);
 							if (prefix != null) 
 								break;
 						}
 					}
 					context = context.eContainer();
 				}				
 				// if a prefix is not found for the namespaceURI, create a new prefix
 				if (prefix == null) 
 					prefix = addNewRootPrefix("ns", namespace);
 				if (prefix != null && !prefix.equals(""))
 					return prefix + ":" + qname.getLocalPart();
 			}
 			return qname.getLocalPart();
 		}
         
 		public void serializePrefixes(EObject eObject, Element context) {
 			Map nsMap = resource.getPrefixToNamespaceMap(eObject);
 			if (!nsMap.isEmpty()) {
 				for (Iterator i = nsMap.keySet().iterator(); i.hasNext(); ) {
 					String prefix = (String)i.next();
 					String namespace = (String)nsMap.get(prefix);
 					if (prefix == "")
 						context.setAttributeNS(XSDConstants.XMLNS_URI_2000, "xmlns", namespace);
 					else
 						context.setAttributeNS(XSDConstants.XMLNS_URI_2000, "xmlns:"+prefix, namespace);
 				}
 			}
 		}
 		
 		public void objectAdded(Object key, Object value) {
 			// we only listen the process namespace map
             if (! resource.getContents().isEmpty()) {
     			Process process = resource.getProcess();
     			((Map)myNamespacePrefixMap.get(process)).put(value, key);
             }
             // TODO What should happen if the process does not yet exist?
 		}
 	}
 
 	/**
 	 * WsdlImportsManager is responsible for ensuring that, for a given
 	 * namespace and resource uri, an import exists in the bpel file.
 	 */
 	private class WsdlImportsManager {
 		private Process process;
 		
 		// Map of resource uris to namespaces
 		private Map resourceNamespaceMap;
 		
 		// The URI of the BPEL resource. We use this to calculate
 		// relative locations for the imported WSDL files.
 		private URI bpelResourceURI;
 		
 		// Constructor for new-style writing
 		public WsdlImportsManager(Process process) {
 			this.process = process;
 			init();
 		}
 		
 		private void init() {
 			Resource bpelResource = process.eResource();
 			// Cache the URI of the BPEL resource for later.
 			this.bpelResourceURI = bpelResource.getURI();
 
 			resourceNamespaceMap = new HashMap();
 			
 			// For each existing import in the process, add it to the namespace map.
 			Iterator it = process.getImports().iterator();
 			while (it.hasNext()) {
 				Import imp = (org.eclipse.bpel.model.Import)it.next();
 				if (imp.getLocation() == null) {
 				    System.err.println("Import location is unexpectedly null: " + imp);
 				} else {
 					URI locationURI = URI.createURI(imp.getLocation());
 					String importPath = locationURI.resolve(bpelResourceURI).toString();
 					resourceNamespaceMap.put(importPath, imp.getNamespace());
 				}
 			}
 		}
 		
 		/**
 		 * Ensure that there exists an import mapping the given namespace
 		 * to the given resource. If the import doesn't exist in our map,
 		 * add it to the map and create a new Import in the process.
 		 */
 		public void ensureImported(Resource resource, String namespace) {
 			// For service references. If the declaration comes from the
 			// bpel xsd, bail out.
 			if (BPELConstants.NAMESPACE.equals(namespace)) return;
 			
 			String key = resource.getURI().toString();
 
 			if (!resourceNamespaceMap.containsKey(key)) {
 				// second check to ensure the calculated path is not empty
 				String locationURI = getRelativeLocation(resource.getURI());
 				if (locationURI != null && locationURI.length() != 0) {
 					// Create and add the import to the process
 					org.eclipse.bpel.model.Import _import = BPELFactory.eINSTANCE.createImport();
 					_import.setNamespace(namespace);
 					_import.setLocation(locationURI);
                     if (resource instanceof WSDLResourceImpl)
                         _import.setImportType(WSDLConstants.WSDL_NAMESPACE_URI);
                     else if (resource instanceof XSDResourceImpl)
                         _import.setImportType(XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001);
 					process.getImports().add(_import);
 					
 					// Add it to our namespace map for easy reference
 					resourceNamespaceMap.put(key, namespace);
 				}
 			}
 		}
 		
 		/**
 		 * Helper method. Return the relative location of the given file uri,
 		 * relative to the location of the BPEL file.
 		 */
 		private String getRelativeLocation(URI importedFileUri) {
 		    URI relativeURI = importedFileUri.deresolve(bpelResourceURI, true, true, false);
 		    return relativeURI.toString();
 		}
 	}
     
 	/** 
 	 * Convert the BPEL model to an XML DOM model and then write the DOM model
 	 * to the output stream.
 	 * 
 	 * @see org.eclipse.emf.ecore.resource.impl.ResourceImpl#doSave(OutputStream, Map)
 	 */
 	public void write(BPELResource resource, OutputStream out, Map args) throws IOException
 	{
 		try 
 		{
 			// Create a DOM document.
 			
 			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 			documentBuilderFactory.setNamespaceAware(true);
 			documentBuilderFactory.setValidating(false);
 			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
 			document = builder.newDocument();
 
 			// Transform the EMF model to the DOM document.
 			
 			bpelPackage = BPELPackage.eINSTANCE;
 
 			this.bpelResource = resource;
 
 			bpelNamespacePrefixManager = new NamespacePrefixManager(resource);
 			Process process = resource.getProcess();
 			wsdlNamespacePrefixManager = new WsdlImportsManager(process);
 			
 			walkExternalReferences();
 			
 			document = resource2XML(resource);			
 
 			// Transform the DOM document to its serialized form.
 			
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
 		      
 			// Unless a width is set, there will be only line breaks but no indentation.
 			// The IBM JDK and the Sun JDK don't agree on the property name,
 			// so we set them both.
 			//
 			transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
 			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 			// TODO Do we need to support different encodings?
 //			if (encoding != null)
 //			{
 //				transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
 //			}
 
 			transformer.transform(new DOMSource(document), new StreamResult(out));
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 
 	protected Document resource2XML(BPELResource resource) {
 		Process process = resource.getProcess();
 		Element procElement = process2XML(process);
 		document.appendChild(procElement);		
 		bpelNamespacePrefixManager.serializePrefixes(process, procElement);		
 		return document;
 	}
 
 	protected Element process2XML(Process process) {
 		this.process=process;
 		
 		Element processElement = createBPELElement("process");
 		if (process.getName() != null)
 			processElement.setAttribute("name", process.getName());
 		if (process.getTargetNamespace() != null)
 			processElement.setAttribute("targetNamespace", process.getTargetNamespace());
 		if (process.isSetSuppressJoinFailure())
 			processElement.setAttribute("suppressJoinFailure", BPELUtils.boolean2XML(process.getSuppressJoinFailure()));
 		if (process.getExitOnStandardFault() != null)
 			processElement.setAttribute("exitOnStandardFault", BPELUtils.boolean2XML(process.getExitOnStandardFault()));
 		if (process.isSetVariableAccessSerializable())
 			processElement.setAttribute("variableAccessSerializable", BPELUtils.boolean2XML(process.getVariableAccessSerializable()));
 		if (process.isSetQueryLanguage())
 			processElement.setAttribute("queryLanguage", process.getQueryLanguage());
 		if (process.isSetExpressionLanguage())
 			processElement.setAttribute("expressionLanguage", process.getExpressionLanguage());
 			
 		Iterator it = process.getImports().iterator();
 		while (it.hasNext()) {
 			org.eclipse.bpel.model.Import imp = (org.eclipse.bpel.model.Import)it.next();
 			processElement.appendChild(import2XML(imp));
 		}
 		
 		if (process.getPartnerLinks() != null)
 			processElement.appendChild(partnerLinks2XML(process.getPartnerLinks()));
 			
 		if (process.getVariables() != null)
 			processElement.appendChild(variables2XML(process.getVariables()));
 
 		if (process.getCorrelationSets() != null)
 			processElement.appendChild(correlationSets2XML(process.getCorrelationSets()));
 		
 		if (process.getExtensions() != null)
 			processElement.appendChild(extensions2XML(process.getExtensions()));
 
 		if (process.getFaultHandlers() != null) 
 			processElement.appendChild(faultHandlers2XML(process.getFaultHandlers()));
 		
 		if (process.getEventHandlers() != null) 
 			processElement.appendChild(eventHandler2XML(process.getEventHandlers()));
 		
 		if (process.getActivity() != null) 
 			processElement.appendChild(activity2XML(process.getActivity()));
 		
 		extensibleElement2XML(process,processElement);
 		
 		return processElement;
 	}
 	
 	protected void walkExternalReferences() {        
 		Map crossReferences = EcoreUtil.ExternalCrossReferencer.find(bpelResource);
 
         for (Iterator externalRefIt = crossReferences.keySet().iterator(); externalRefIt.hasNext(); ) {
 			EObject externalObject = (EObject)externalRefIt.next();
 			String namespace = getNamespace(externalObject);
 			if (XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001.equals(namespace)) {
 				bpelNamespacePrefixManager.addNewRootPrefix("xsd", namespace);
 			} else if (namespace != null && externalObject.eResource() != null) {
 				wsdlNamespacePrefixManager.ensureImported(externalObject.eResource(), namespace);					
 			}
 		}
 	}
 	
 	protected QName getQName(EObject object) {
 		QName qname = null;
 		
 		if (object.eIsProxy() && object instanceof IBPELServicesProxy) {
 			qname = ((IBPELServicesProxy)object).getQName();
 		} else if (object instanceof PartnerLinkType) {
 			qname = BPELServicesUtility.getQName((PartnerLinkType)object);
 		} else if (object instanceof Property) {
 			qname = BPELServicesUtility.getQName((Property)object);
 		}
 		
 		return qname;
 	}
 	
 	protected String getNamespace(EObject object) {
 		String namespace = null;
 		if (object instanceof IBPELServicesProxy) {
 			return ((IBPELServicesProxy)object).getQName().getNamespaceURI();
 		} else if (object instanceof PartnerLinkType || object instanceof Property) {
 			Definition def = ((org.eclipse.wst.wsdl.ExtensibilityElement)object).getEnclosingDefinition();
 			if (def != null) {
 				namespace = def.getTargetNamespace();
 			}
 		} else if (object instanceof XSDNamedComponent) {
 			return ((XSDNamedComponent)object).getTargetNamespace();			
 		} else {
 			for(Iterator featureIt = object.eClass().getEAllAttributes().iterator(); featureIt.hasNext() && namespace == null; ) {
 				EAttribute attr = (EAttribute)featureIt.next();
 				if (attr.getName().equals("qName")) {
 					QName qName = (QName) object.eGet(attr);
 					if (qName != null)
 					    namespace = qName.getNamespaceURI();
 				}
 			}
 		}
 		return namespace;
 	}
 	
 	protected String getOperationSignature(Operation op) {
 		String signature = "";
 		if (op != null) {
 			signature = op.getName();
 		}
 		return signature;
 	}
 
 	protected Element import2XML(org.eclipse.bpel.model.Import imp) {
 		Element importElement = createBPELElement("import");
 		if (imp.getNamespace() != null) {
 			importElement.setAttribute("namespace", imp.getNamespace());
 		}
 		if (imp.getLocation() != null) {
 			importElement.setAttribute("location", imp.getLocation());
 		}
 		if (imp.getImportType() != null) {
 			importElement.setAttribute("importType", imp.getImportType());
 		}
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(imp, importElement);			
 
 		return importElement;
 	}
 	
 	protected Element partnerLinks2XML(PartnerLinks partnerLinks) {
 		Element partnerLinksElement = createBPELElement("partnerLinks");
 		
 		Iterator it = partnerLinks.getChildren().iterator();
 		while (it.hasNext()) {
 			PartnerLink partnerLink = (PartnerLink)it.next();
 			partnerLinksElement.appendChild(partnerLink2XML(partnerLink));
 		}
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(partnerLinks, partnerLinksElement);			
 		extensibleElement2XML(partnerLinks, partnerLinksElement);
 		
 		return partnerLinksElement;			
 	}
 	
 	protected Element partnerLink2XML(PartnerLink partnerLink) {
 		Element partnerLinkElement = createBPELElement("partnerLink");
 		if (partnerLink.getName() != null) {
 			partnerLinkElement.setAttribute("name", partnerLink.getName());
 		}
 		
 		if (partnerLink.isSetInitializePartnerRole())
 			partnerLinkElement.setAttribute("initializePartnerRole", BPELUtils.boolean2XML(partnerLink.getInitializePartnerRole()));
 				
 		PartnerLinkType plt = partnerLink.getPartnerLinkType();
 		if (plt != null) {
 			String qnameStr = bpelNamespacePrefixManager.qNameToString(partnerLink, getQName(plt));
 			partnerLinkElement.setAttribute("partnerLinkType", qnameStr);
 			Role myRole = partnerLink.getMyRole();
 			if (myRole != null) {
 				partnerLinkElement.setAttribute("myRole",myRole.getName());
 			}
 			Role partnerRole = partnerLink.getPartnerRole();
 			if (partnerRole != null) {
 				partnerLinkElement.setAttribute("partnerRole",partnerRole.getName());
 			}
 				
 		}
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(partnerLink, partnerLinkElement);			
 		extensibleElement2XML(partnerLink,partnerLinkElement);
 
 		return partnerLinkElement;
 	}
 
 	protected Element variables2XML(Variables variables) {
 		Element variablesElement = createBPELElement("variables");
 		
 		Iterator it = variables.getChildren().iterator();
 		while (it.hasNext()) {
 			Variable variable = (Variable)it.next();
 			if (variable instanceof Variable){
 				if(variable instanceof Variable)
 					variablesElement.appendChild(variable2XML((Variable)variable));
 				else if(variable instanceof ExtensibilityElement){
 					Element varElement = extensibilityElement2XML((ExtensibilityElement)variable);
 					if(variablesElement.getFirstChild() == null)  
 						variablesElement.appendChild(varElement);
 					else
 						variablesElement.insertBefore(varElement,variablesElement.getFirstChild());
 				}
 			}	
 				
 		}
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(variables, variablesElement);			
 		extensibleElement2XML(variables, variablesElement);									
 
 		return variablesElement;				
 	}
 	
 	protected Element variable2XML(Variable variable) {
 		Element variableElement = createBPELElement("variable");
 		
 		if (variable.getName() != null) {
 			variableElement.setAttribute("name", variable.getName());
 		}
 		
 		Message msg = variable.getMessageType();
 		if (msg != null) {
 			variableElement.setAttribute("messageType", bpelNamespacePrefixManager.qNameToString(variable, msg.getQName()));
 		}
 				
 		if (variable.getType() != null) {
 			XSDTypeDefinition type = variable.getType();
 			QName qname = new QName(type.getTargetNamespace(), type.getName());
 			variableElement.setAttribute("type", bpelNamespacePrefixManager.qNameToString(variable, qname));
 		}
 				
 		if (variable.getXSDElement() != null) {
 			XSDElementDeclaration element = variable.getXSDElement();
 			QName qname = new QName(element.getTargetNamespace(), element.getName());
 			variableElement.setAttribute("element", bpelNamespacePrefixManager.qNameToString(variable, qname));
 		}		
 
 		// from-spec
 		From from = variable.getFrom();
 		if (from != null) {
 			Element fromElement = createBPELElement("from");
 			from2XML(from, fromElement);
 			variableElement.appendChild(fromElement);
 		}
 		
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(variable, variableElement);	
 		extensibleElement2XML(variable,variableElement);
 		
 		return variableElement;
 	}
 
 	protected Element fromPart2XML(FromPart fromPart) {
 		Element fromPartElement = createBPELElement("fromPart");
 		
 		if (fromPart.getPart() != null) {
 			fromPartElement.setAttribute("part", fromPart.getPart());
 		}
 		
 		if (fromPart.getTo() != null) {
 			Element toElement = createBPELElement("to");
 			to2XML(fromPart.getTo(), toElement);
 			fromPartElement.appendChild(toElement);
 		}
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(fromPart, fromPartElement);	
 		
 		return fromPartElement;
 	}
 
 	protected Element toPart2XML(ToPart toPart) {
 		Element toPartElement = createBPELElement("toPart");
 		
 		if (toPart.getPart() != null) {
 			toPartElement.setAttribute("part", toPart.getPart());
 		}
 		
 		if (toPart.getFrom() != null) {
 			Element fromElement = createBPELElement("from");
 			from2XML(toPart.getFrom(), fromElement);
 			toPartElement.appendChild(fromElement);
 		}
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(toPart, toPartElement);	
 		
 		return toPartElement;
 	}
 
 	protected Element extensions2XML(Extensions extensions) {
 		Element extensionsElement = createBPELElement("extensions");
 
 		Iterator it = extensions.getChildren().iterator();
 		while (it.hasNext()) {
 			Extension extension = (Extension)it.next();
 			extensionsElement.appendChild(extension2XML(extension));			
 		}
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(extensions, extensionsElement);	
 		extensibleElement2XML(extensions, extensionsElement);
 		
 		return extensionsElement;
 	}
 
 	protected Element extension2XML(Extension extension) {
 		Element extensionElement = createBPELElement("extension");
 		if (extension.getNamespace() != null) {
 			extensionElement.setAttribute("namespace", extension.getNamespace());
 		}
 		if (extension.isSetMustUnderstand()) {
 			extensionElement.setAttribute("mustUnderstand", BPELUtils.boolean2XML(extension.getMustUnderstand()));
 		}		
 		
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(extension, extensionElement);			
 		extensibleElement2XML(extension, extensionElement);
 		return extensionElement;
 	}
 
 	protected Element correlationSets2XML(CorrelationSets correlationSets) {
 		Element correlationSetsElement = createBPELElement("correlationSets");
 
 		Iterator it = correlationSets.getChildren().iterator();
 		while (it.hasNext()) {
 			CorrelationSet correlationSet = (CorrelationSet)it.next();
 			correlationSetsElement.appendChild(correlationSet2XML(correlationSet));			
 		}
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(correlationSets, correlationSetsElement);	
 		extensibleElement2XML(correlationSets, correlationSetsElement);
 		
 		return correlationSetsElement;
 	}
 	
 	protected Element correlationSet2XML(CorrelationSet correlationSet) {
 		Element correlationSetElement = createBPELElement("correlationSet");
 		if (correlationSet.getName() != null) {
 			correlationSetElement.setAttribute("name", correlationSet.getName());
 		}
 		StringBuffer propertiesList = new StringBuffer();
 		Iterator properties = correlationSet.getProperties().iterator();
 		while (properties.hasNext()) {
 			Property property = (Property)properties.next();
 			String qnameStr = bpelNamespacePrefixManager.qNameToString(correlationSet, getQName(property));
 			propertiesList.append(qnameStr);
 			if (properties.hasNext()) propertiesList.append(" ");
 		}
 		if (propertiesList.length() > 0) {
 			correlationSetElement.setAttribute("properties", propertiesList.toString());
 		}
 		
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(correlationSet, correlationSetElement);			
 		extensibleElement2XML(correlationSet, correlationSetElement);
 		return correlationSetElement;
 	}
 
 	protected Element correlations2XML(Correlations correlations) {
 		Element correlationsElement = createBPELElement("correlations");
 		
 		Iterator it = correlations.getChildren().iterator();
 		while (it.hasNext()) {		
 			Correlation correlation = (Correlation)it.next();
 			correlationsElement.appendChild(correlation2XML(correlation));
 		}
 		
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(correlations, correlationsElement);			
 		extensibleElement2XML(correlations, correlationsElement);
 		
 		return correlationsElement;
 	}
 	
 	protected Element correlation2XML(Correlation correlation){
 		Element correlationElement = createBPELElement("correlation");
 		
 		if (correlation.getSet() != null && correlation.getSet().getName() != null) 
 			correlationElement.setAttribute("set",correlation.getSet().getName());
 		
 		if (correlation.isSetInitiate()) 
 			correlationElement.setAttribute("initiate",correlation.getInitiate());
 				
 		if (correlation.isSetPattern()) 
 			correlationElement.setAttribute("pattern",correlation.getPattern().toString());
 				
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(correlation, correlationElement);					
 		extensibleElement2XML(correlation, correlationElement);
 		
 		return correlationElement;
 	}
 
 	protected Element faultHandlers2XML(FaultHandler faultHandler) {
 		Element faultHandlersElement = createBPELElement("faultHandlers");
 		faultHandler2XML(faultHandlersElement, faultHandler);	
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(faultHandler, faultHandlersElement);	
 		extensibleElement2XML(faultHandler, faultHandlersElement);
 		
 		return faultHandlersElement;
 	}
 
 	protected void faultHandler2XML(Element parentElement, FaultHandler faultHandler) {
 		Iterator catches = faultHandler.getCatch().iterator();
 		while (catches.hasNext()) {
 			Catch _catch = (Catch)catches.next();
 			parentElement.appendChild(catch2XML(_catch));
 		}
 		if (faultHandler.getCatchAll() != null) {		
 			parentElement.appendChild(catchAll2XML(faultHandler.getCatchAll()));
 		}
 	}
 
 	protected Element compensationHandler2XML(CompensationHandler compensationHandler) {
 		Element compensationHandlerElement = createBPELElement("compensationHandler");
 		
 		if (compensationHandler.getActivity() != null) {
 			Element activityElement = activity2XML(compensationHandler.getActivity());
 			compensationHandlerElement.appendChild(activityElement);
 		}
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(compensationHandler, compensationHandlerElement);			
 		extensibleElement2XML(compensationHandler, compensationHandlerElement);
 		return compensationHandlerElement;
 	}
 
 	protected Element terminationHandler2XML(TerminationHandler terminationHandler) {
 		Element terminationHandlerElement = createBPELElement("terminationHandler");
 		
 		if (terminationHandler.getActivity() != null) {
 			Element activityElement = activity2XML(terminationHandler.getActivity());
 			terminationHandlerElement.appendChild(activityElement);
 		}
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(terminationHandler, terminationHandlerElement);
 		extensibleElement2XML(terminationHandler, terminationHandlerElement);
 		return terminationHandlerElement;
 	}
 
 	protected Element eventHandler2XML(EventHandler eventHandler) {
 		Element eventHandlerElement = createBPELElement("eventHandlers");
 		
 		// TODO: For backwards compatibility with 1.1 we should serialize
 		// OnMessages here.
 		for (Iterator it = eventHandler.getEvents().iterator(); it.hasNext(); ) {
 			OnEvent onEvent = (OnEvent)it.next();			
 			eventHandlerElement.appendChild(onEvent2XML(onEvent));			
 		}
 		for (Iterator it = eventHandler.getAlarm().iterator(); it.hasNext(); ) {
 			OnAlarm onAlarm = (OnAlarm)it.next();			
 			eventHandlerElement.appendChild(onAlarm2XML(onAlarm));			
 		}
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(eventHandler, eventHandlerElement);			
 		extensibleElement2XML(eventHandler, eventHandlerElement);
 		return eventHandlerElement;
 	}
 	
 	protected Element activity2XML(Activity activity) {
 		Element activityElement = null;
 
 		if (activity instanceof Empty)
 			activityElement = empty2XML(activity);
 		else if (activity instanceof Invoke)
 			activityElement = invoke2XML(activity);
 		else if (activity instanceof Receive)
 			activityElement = receive2XML(activity);
 		else if (activity instanceof Reply)
 			activityElement = reply2XML(activity);
 		else if (activity instanceof Assign)
 			activityElement = assign2XML(activity);
 		else if (activity instanceof Wait)
 			activityElement = wait2XML(activity);
 		else if (activity instanceof Throw)
 			activityElement = throw2XML(activity);
 		else if (activity instanceof Exit)
 			activityElement = exit2XML(activity);
 		else if (activity instanceof Flow)
 			activityElement = flow2XML(activity);
 		else if (activity instanceof Switch)
 			activityElement = switch2XML(activity);
 		else if (activity instanceof If)
 			activityElement = if2XML(activity);
 		else if (activity instanceof While)
 			activityElement = while2XML(activity);
 		else if (activity instanceof Sequence)
 			activityElement = sequence2XML(activity);
 		else if (activity instanceof Pick)
 			activityElement = pick2XML(activity);
 		else if (activity instanceof Scope)
 			activityElement = scope2XML(activity);
 		else if (activity instanceof Compensate)
 			activityElement = compensate2XML(activity);
 		else if (activity instanceof Rethrow)
 			activityElement = rethrow2XML(activity);
 		else if (activity instanceof ExtensionActivity)
 			activityElement = extensionActivity2XML(activity);
 		else if (activity instanceof OpaqueActivity)
 			activityElement = opaqueActivity2XML(activity);
 		else if (activity instanceof ForEach)
 			activityElement = forEach2XML(activity);
 		else if (activity instanceof RepeatUntil)
 			activityElement = repeatUntil2XML(activity);
 		else if (activity instanceof ValidateXML)
 			activityElement = validateXML2XML(activity);
 		else
 			return null;
 
 		if (activity.getName() != null)
 			activityElement.setAttribute("name", activity.getName());
 		if (activity.isSetSuppressJoinFailure()) {
 			activityElement.setAttribute(
 				"suppressJoinFailure",
 				BPELUtils.boolean2XML(activity.getSuppressJoinFailure()));
 		}
 
 		// NOTE: Mind the order of these elements.
 		Node firstChild = activityElement.getFirstChild();
 		Targets targets = activity.getTargets();
 		if (targets != null) {
 			activityElement.insertBefore(targets2XML(targets), firstChild);
 		}
 		Sources sources = activity.getSources();
 		if (sources != null) {
 			activityElement.insertBefore(sources2XML(sources), firstChild); 
 		}
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(activity, activityElement);			
 		extensibleElement2XML(activity,activityElement);
 			
 		return activityElement;
 	}
 
 	protected Element catch2XML(Catch _catch) {
 		Element catchElement = createBPELElement("catch");
 		if (_catch.getFaultName() != null) {
 			catchElement.setAttribute("faultName", bpelNamespacePrefixManager.qNameToString(_catch, _catch.getFaultName()));
 		}
 		if (_catch.getFaultVariable() != null) {
 			catchElement.setAttribute("faultVariable", _catch.getFaultVariable().getName());
 		}
 		if (_catch.getFaultMessageType() != null) {
 			catchElement.setAttribute("faultMessageType", bpelNamespacePrefixManager.qNameToString(_catch, _catch.getFaultMessageType().getQName()));
 		}
 		if (_catch.getFaultElement() != null) {
 			XSDElementDeclaration element = _catch.getFaultElement();
 			QName qname = new QName(element.getTargetNamespace(), element.getName());
 			catchElement.setAttribute("faultElement", bpelNamespacePrefixManager.qNameToString(_catch, qname));
 		}
 		if (_catch.getActivity() != null) {
 			catchElement.appendChild(activity2XML(_catch.getActivity()));  // might be a compensate activity
 		}		
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(_catch, catchElement);			
 		extensibleElement2XML(_catch, catchElement);
 		return catchElement;
 	}
 
 	protected Element catchAll2XML(CatchAll catchAll) {
 		Element catchAllElement = createBPELElement("catchAll");
 		
 		Activity activity = catchAll.getActivity();
 		if (activity != null) 
 			catchAllElement.appendChild(activity2XML(activity));  // might be a compensate activity
 		
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(catchAll, catchAllElement);			
 		extensibleElement2XML(catchAll, catchAllElement);
 		
 		return catchAllElement;
 	}
 
 	protected Element empty2XML(Activity activity) {
 		Element activityElement = createBPELElement("empty");
 		return activityElement;
 	}
 
 	protected Element opaqueActivity2XML(Activity activity) {
 		Element activityElement = createBPELElement("opaqueActivity");
 		return activityElement;
 	}
 
 	protected Element forEach2XML(Activity activity) {
 		ForEach forEach = (ForEach)activity;
 		Element activityElement = createBPELElement("forEach");
 		
 		if (forEach.getParallel() != null)
 			activityElement.setAttribute("parallel", BPELUtils.boolean2XML(forEach.getParallel()));
 		
 		if (forEach.getCounterName() != null) {
 			activityElement.setAttribute("counterName", forEach.getCounterName().getName());
 		}
 
 		if (forEach.getStartCounterValue() != null) {
 			activityElement.appendChild(expression2XML(forEach.getStartCounterValue(), "startCounterValue"));
 		}
 		
 		if (forEach.getFinalCounterValue() != null) {
 			activityElement.appendChild(expression2XML(forEach.getFinalCounterValue(), "finalCounterValue"));
 		}
 
 		return activityElement;
 	}
 
 	protected Element rethrow2XML(Activity activity) {
 		Element activityElement = createBPELElement("rethrow");
 		return activityElement;
 	}
 
 	protected Element validateXML2XML(Activity activity) {
 		Element activityElement = createBPELElement("validateXML");
 		return activityElement;
 	}
 
 	protected Element extensionActivity2XML(Activity activity) {
		Element activityElement = createBPELElement("activityExtension");
 		return activityElement;
 	}
 
 	protected Element invoke2XML(Activity activity) {
 		Invoke invoke = (Invoke)activity;
 		Element activityElement = createBPELElement("invoke");
 		if (invoke.getPartnerLink() != null)
 			activityElement.setAttribute("partnerLink", invoke.getPartnerLink().getName());
 		if (invoke.getPortType() != null)
 			activityElement.setAttribute("portType", bpelNamespacePrefixManager.qNameToString(invoke, invoke.getPortType().getQName()));
 		if (invoke.getOperation() != null)
 			activityElement.setAttribute("operation", getOperationSignature(invoke.getOperation()));
 		if (invoke.getInputVariable() != null)
 			activityElement.setAttribute("inputVariable", invoke.getInputVariable().getName());
 		if (invoke.getOutputVariable() != null)
 			activityElement.setAttribute("outputVariable", invoke.getOutputVariable().getName());
 			
 		if (invoke.getCorrelations() != null)
 			activityElement.appendChild(correlations2XML(invoke.getCorrelations()));
 		
 		FaultHandler faultHandler = invoke.getFaultHandler();
 		if (faultHandler != null) {
 			faultHandler2XML(activityElement, faultHandler);
 		}
 		
 		if (invoke.getCompensationHandler() != null)
 			activityElement.appendChild(compensationHandler2XML(invoke.getCompensationHandler()));
 		
 		Iterator it = invoke.getFromPart().iterator();
 		while (it.hasNext()) {
 			FromPart fromPart = (FromPart)it.next();
 			activityElement.appendChild(fromPart2XML(fromPart));
 		}
 		it = invoke.getToPart().iterator();
 		while (it.hasNext()) {
 			ToPart toPart = (ToPart)it.next();
 			activityElement.appendChild(toPart2XML(toPart));
 		}
 		
 		return activityElement;
 	}
 
 	protected Element receive2XML(Activity activity) {
 		Receive receive = (Receive)activity;
 		Element activityElement = createBPELElement("receive");
 		if (receive.getPartnerLink() != null)
 			activityElement.setAttribute("partnerLink", receive.getPartnerLink().getName());
 		if (receive.getPortType() != null)
 			activityElement.setAttribute("portType", bpelNamespacePrefixManager.qNameToString(receive, receive.getPortType().getQName()));
 		if (receive.getOperation() != null)
 			activityElement.setAttribute("operation", getOperationSignature(receive.getOperation()));
 		if (receive.getVariable() != null)
 			activityElement.setAttribute("variable", receive.getVariable().getName());
 		if (receive.isSetCreateInstance())
 			activityElement.setAttribute("createInstance",BPELUtils.boolean2XML(receive.getCreateInstance()));
 
 		if (receive.getCorrelations() != null)
 			activityElement.appendChild(correlations2XML(receive.getCorrelations()));			
 		
 		Iterator it = receive.getFromPart().iterator();
 		while (it.hasNext()) {
 			FromPart fromPart = (FromPart)it.next();
 			activityElement.appendChild(fromPart2XML(fromPart));
 		}
 
 		return activityElement;
 	}
 
 	protected Element reply2XML(Activity activity) {
 		Reply reply = (Reply)activity;
 		Element activityElement = createBPELElement("reply");
 		if (reply.getPartnerLink() != null )
 			activityElement.setAttribute("partnerLink", reply.getPartnerLink().getName());
 		if (reply.getPortType() != null )
 			activityElement.setAttribute("portType", bpelNamespacePrefixManager.qNameToString(reply, reply.getPortType().getQName()));
 		if (reply.getOperation() != null )
 			activityElement.setAttribute("operation", getOperationSignature(reply.getOperation()));
 		if (reply.getVariable() != null )
 			activityElement.setAttribute("variable", reply.getVariable().getName());
 		if (reply.getFaultName() != null) {
 			activityElement.setAttribute("faultName", bpelNamespacePrefixManager.qNameToString(reply, reply.getFaultName()));
 		}
 		if (reply.getCorrelations() != null)
 			activityElement.appendChild(correlations2XML(reply.getCorrelations()));
 			
 		Iterator it = reply.getToPart().iterator();
 		while (it.hasNext()) {
 			ToPart toPart = (ToPart)it.next();
 			activityElement.appendChild(toPart2XML(toPart));
 		}
 
 		return activityElement;
 	}
 
 	protected Element assign2XML(Activity activity) {
 		Assign assign = (Assign)activity;
 		Element activityElement = createBPELElement("assign");
 		
 		if (assign.getValidateXML() != null)
 			activityElement.setAttribute("validateXML", BPELUtils.boolean2XML(assign.getValidateXML()));
 		
 		List copies = assign.getCopy();
 		if (!copies.isEmpty()) {
 			for (Iterator i = copies.iterator(); i.hasNext();) {
 				Copy copy = (Copy)i.next();
 				activityElement.appendChild(copy2XML(copy));				
 			}
 		}
 		return activityElement;
 	}
 
 	protected Element copy2XML(Copy copy) {
 		Element copyElement = createBPELElement("copy");
 		From from = copy.getFrom();
 		if( from != null ){
 			Element fromElement = createBPELElement("from");
 			from2XML(from,fromElement);
 			copyElement.appendChild(fromElement);
 		}
 		To to  = copy.getTo();
 		if( to != null ){
 			Element toElement = createBPELElement("to");
 			to2XML(to, toElement);
 			copyElement.appendChild(toElement);
 		}
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(copy, copyElement);			
 		extensibleElement2XML(copy, copyElement);
 		
 		return copyElement;
 	}
 
 	protected void from2XML(From from,Element fromElement) {
 		to2XML(from,fromElement);
 		if ( from.isSetEndpointReference() )
 			fromElement.setAttribute("endpointReference", from.getEndpointReference().toString());
 		if( from.isSetOpaque())
 			fromElement.setAttribute("opaque", BPELUtils.boolean2XML(from.getOpaque()));
 		if ( from.isSetLiteral() && from.getLiteral()!=null && !from.getLiteral().equals("")) {
 			Node node = null;
 			if (Boolean.TRUE.equals(from.getUnsafeLiteral())) {
 				node = BPELUtils.convertStringToNode(from.getLiteral(), bpelResource);
 			}
 			if (node != null) {
 				for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
 					DOMUtil.copyInto(child, fromElement);
 				}
 			} else {
 				CDATASection cdata = BPELUtils.createCDATASection(document, from.getLiteral());
 				fromElement.appendChild(cdata);
 			}
 		}
 
 		if (from.getServiceRef() != null) {
 			ServiceRef serviceRef = from.getServiceRef();
 			Element serviceRefElement = createBPELElement("service-ref");
 			String referenceScheme = serviceRef.getReferenceScheme();
 			if (referenceScheme != null) {
 				serviceRefElement.setAttribute("reference-scheme", referenceScheme);
 			}
 			if (serviceRef.getValue() != null) {
 				Object value = serviceRef.getValue();
 				if (value instanceof ExtensibilityElement) {
 					ExtensibilityElement extensibilityElement = (ExtensibilityElement)value;
 					BPELExtensionSerializer serializer = null;
 					QName qname = extensibilityElement.getElementType();
 					try {
 					    serializer=(BPELExtensionSerializer)extensionRegistry.querySerializer(ExtensibleElement.class,qname);
 					} catch (WSDLException e) {
 					}
 					
 					if (serializer != null) {
 						// Deserialize the DOM element and add the new Extensibility element to the parent
 						// ExtensibleElement
 						DocumentFragment fragment=document.createDocumentFragment();
 						try {
 						    serializer.marshall(ExtensibleElement.class,qname,extensibilityElement,fragment,process,extensionRegistry);
 							Element child = (Element)fragment.getFirstChild();
 							serviceRefElement.appendChild(child);
 						} catch (WSDLException e) {
 							throw new WrappedException(e);
 						}
 					}
 				} else {
 					ServiceReferenceSerializer serializer = extensionRegistry.getServiceReferenceSerializer(referenceScheme);
 					if (serializer != null) {
 						DocumentFragment fragment=document.createDocumentFragment();
 					    serializer.marshall(value, fragment, process, from, this);
 						Element child = (Element)fragment.getFirstChild();
 						serviceRefElement.appendChild(child);
 					} else {
 						CDATASection cdata = BPELUtils.createCDATASection(document, serviceRef.getValue().toString());
 						serviceRefElement.appendChild(cdata);
 					}
 				}
 				fromElement.appendChild(serviceRefElement);
 			}
 		}
 		
 		if (from.getExpression() != null) {
 			Expression expression = from.getExpression();
 			Element expressionElement = createBPELElement("expression");
 			if (expression.getExpressionLanguage() != null) {
 				expressionElement.setAttribute("expressionLanguage", expression.getExpressionLanguage());
 			}
 			if (expression.getBody() != null) {
 				CDATASection cdata = BPELUtils.createCDATASection(document, (String)expression.getBody());
 				expressionElement.appendChild(cdata);
 			}
 			fromElement.appendChild(expressionElement);
 		}
 		if (from.getType() != null) {
 			XSDTypeDefinition type = from.getType();
 			QName qname = new QName(type.getTargetNamespace(), type.getName());
 			fromElement.setAttribute("xsi:type", bpelNamespacePrefixManager.qNameToString(from, qname));
 		}
 	}
 
 	protected void to2XML(To to, Element toElement) {
 		if( to.getVariable() != null )
 			toElement.setAttribute("variable", to.getVariable().getName());
 		if( to.getPart() != null )
 			toElement.setAttribute("part", to.getPart().getName());
 		if( to.getPartnerLink() != null )
 			toElement.setAttribute("partnerLink", to.getPartnerLink().getName());
 		Property property = to.getProperty();
 		if( property != null )  {
 			String qnameStr = bpelNamespacePrefixManager.qNameToString(to, getQName(property));
 			toElement.setAttribute("property", qnameStr);
 		}
 
 		if (to.getQuery() != null) {
 			Query query = to.getQuery();
 			Element queryElement = createBPELElement("query");
 			if (query.getQueryLanguage() != null) {
 				queryElement.setAttribute("queryLanguage", query.getQueryLanguage());
 			}
 			if (query.getValue() != null) {
 				CDATASection cdata = BPELUtils.createCDATASection(document, query.getValue());
 				queryElement.appendChild(cdata);
 			}
 			toElement.appendChild(queryElement);
 		}
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(to, toElement);	
 		extensibleElement2XML(to, toElement);			
 	}
 	
 	protected Element wait2XML(Activity activity) {
 		Element activityElement = createBPELElement("wait");
 		if (((Wait)activity).getFor() != null) {
 			activityElement.appendChild(expression2XML(((Wait)activity).getFor(), "for"));
 		}
 		if (((Wait)activity).getUntil() != null) {
 			activityElement.appendChild(expression2XML(((Wait)activity).getUntil(), "until"));
 		}
 		return activityElement;
 	}
 
 	protected Element throw2XML(Activity activity) {
 		Element activityElement = createBPELElement("throw");
 		if (((Throw)activity).getFaultVariable() != null && ((Throw)activity).getFaultVariable().getName() != null) {
 			activityElement.setAttribute("faultVariable", ((Throw)activity).getFaultVariable().getName()); 
 		}
 		if (((Throw)activity).getFaultName() != null) {
 			activityElement.setAttribute("faultName", bpelNamespacePrefixManager.qNameToString(activity, ((Throw)activity).getFaultName()));
 		}
 		return activityElement;
 	}
 
 	protected Element exit2XML(Activity activity) {
 		Element activityElement = createBPELElement("exit");
 		return activityElement;
 	}
 
 	protected Element flow2XML(Activity activity) {
 		Element activityElement = createBPELElement("flow");
 		
 		Links links = ((Flow)activity).getLinks();
 		if (links != null) {
 			Element linksElement = links2XML(links);
 			activityElement.appendChild(linksElement);
 		}
 			
 		List activities = ((Flow)activity).getActivities();
 		if( !activities.isEmpty() ){
 			for( Iterator i=activities.iterator(); i.hasNext(); ){
 				Activity a = (Activity) i.next();
 				activityElement.appendChild( activity2XML(a) );				
 			}
 		}
 
 		return activityElement;
 	}
 	
 	protected Element links2XML(Links links) {
 		Element linksElement = createBPELElement("links");
 					
 		for( Iterator i = links.getChildren().iterator(); i.hasNext(); ){
 			Link link = (Link)i.next();
 			linksElement.appendChild(link2XML(link));				
 		}
 		
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(links, linksElement);			
 		extensibleElement2XML(links, linksElement);
 		
 		return linksElement;		
 	}
 
 	protected Element link2XML(Link link){
 		Element linkElement = createBPELElement("link");
 		if (link.getName() != null)
 			linkElement.setAttribute("name", link.getName() );
 		
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(link, linkElement);			
 		extensibleElement2XML(link,linkElement);
 		
 		return linkElement;
 	}
 	
 	protected Element switch2XML(Activity activity) {
 		Element activityElement = createBPELElement("switch");
 		List cases = ((Switch)activity).getCases();
 		if (!cases.isEmpty()) {
 			for (Iterator i = cases.iterator(); i.hasNext();) {
 				Case c = (Case) i.next();
 				Element caseElement = createBPELElement("case");
 				activityElement.appendChild(caseElement);
 				if (c.getCondition() != null) {
 					caseElement.appendChild(expression2XML(c.getCondition(), "condition"));
 				}
 				if( c.getActivity() != null ){
 					caseElement.appendChild(activity2XML(c.getActivity()));
 				}			
 				// serialize local namespace prefixes to XML
 				bpelNamespacePrefixManager.serializePrefixes(c, caseElement);						
 				extensibleElement2XML(c,caseElement);
 			}
 		}
 		Otherwise otherwise = ((Switch)activity).getOtherwise(); 
 		if (otherwise != null) {
 			Element otherwiseElement = otherwise2XML(otherwise);
 			activityElement.appendChild(otherwiseElement);
 		}
 		
 		return activityElement;
 	}
 
 	protected Element if2XML(Activity activity) {
 		If _if = (If)activity;
 		Element activityElement = createBPELElement("if");
 		
 		if (_if.getCondition() != null) {
 			activityElement.appendChild(expression2XML(_if.getCondition(), "condition"));
 		}
 		Then then = _if.getThen();
 		if (then != null) {
 			Element thenElement = then2XML(then);
 			activityElement.appendChild(thenElement);
 		}
 		
 		List elseIfs = _if.getElseIf();
 		if (!elseIfs.isEmpty()) {
 			for (Iterator i = elseIfs.iterator(); i.hasNext();) {
 				ElseIf elseIf = (ElseIf)i.next();
 				Element elseIfElement = createBPELElement("elseif");
 				activityElement.appendChild(elseIfElement);
 				if (elseIf.getCondition() != null) {
 					elseIfElement.appendChild(expression2XML(elseIf.getCondition(), "condition"));
 				}
 				if (elseIf.getActivity() != null) {
 					elseIfElement.appendChild(activity2XML(elseIf.getActivity()));
 				}			
 				// serialize local namespace prefixes to XML
 				bpelNamespacePrefixManager.serializePrefixes(elseIf, elseIfElement);						
 				extensibleElement2XML(elseIf, elseIfElement);
 			}
 		}
 		Else _else = _if.getElse(); 
 		if (_else != null) {
 			Element elseElement = else2XML(_else);
 			activityElement.appendChild(elseElement);
 		}
 		
 		return activityElement;
 	}
 
 	protected Element otherwise2XML(Otherwise otherwise) {
 		Element otherwiseElement = createBPELElement("otherwise");
 		if (otherwise.getActivity() != null) {
 			Element activityElement = activity2XML(otherwise.getActivity());
 			otherwiseElement.appendChild(activityElement);
 		}
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(otherwise, otherwiseElement);			
 		extensibleElement2XML(otherwise, otherwiseElement);
 		
 		return otherwiseElement;
 	}
 	
 	protected Element else2XML(Else _else) {
 		Element elseElement = createBPELElement("else");
 		if (_else.getActivity() != null) {
 			Element activityElement = activity2XML(_else.getActivity());
 			elseElement.appendChild(activityElement);
 		}
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(_else, elseElement);			
 		extensibleElement2XML(_else, elseElement);
 		
 		return elseElement;
 	}
 	
 	protected Element then2XML(Then then) {
 		Element thenElement = createBPELElement("then");
 		if (then.getActivity() != null) {
 			Element activityElement = activity2XML(then.getActivity());
 			thenElement.appendChild(activityElement);
 		}
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(then, thenElement);			
 		extensibleElement2XML(then, thenElement);
 		
 		return thenElement;
 	}
 	
 	protected Element while2XML(Activity activity) {
 		Element activityElement = createBPELElement("while");
 		
 		if (((While)activity).getCondition() != null) {
 			activityElement.appendChild(expression2XML(((While)activity).getCondition(), "condition"));
 		}
 		if (((While)activity).getActivity() != null) {
 			activityElement.appendChild(activity2XML(((While)activity).getActivity()));
 		}
 
 		return activityElement;
 	}
 	
 	protected Element repeatUntil2XML(Activity activity) {
 		RepeatUntil repeatUntil = (RepeatUntil)activity;
 		Element activityElement = createBPELElement("repeatUntil");
 		
 		if (repeatUntil.getActivity() != null) {
 			activityElement.appendChild(activity2XML(repeatUntil.getActivity()));
 		}
 		if (repeatUntil.getCondition() != null) {
 			activityElement.appendChild(expression2XML(repeatUntil.getCondition(), "condition"));
 		}
 
 		return activityElement;
 	}
 
 	protected Element expression2XML(Expression expression, String elementName) {
 		Element expressionElement = createBPELElement(elementName);
 		
 		if (expression.getExpressionLanguage() != null) {
 			expressionElement.setAttribute("expressionLanguage", expression.getExpressionLanguage());
 		}
 		if (expression.getOpaque() != null) {
 			expressionElement.setAttribute("opaque", BPELUtils.boolean2XML(expression.getOpaque()));
 		}
 		if (expression.getBody() != null) {
 			Object body = expression.getBody();
 			if (body instanceof ExtensibilityElement) {
 				ExtensibilityElement extensibilityElement = (ExtensibilityElement)body;
 				BPELExtensionSerializer serializer = null;
 				QName qname = extensibilityElement.getElementType();
 				try {
 				    serializer=(BPELExtensionSerializer)extensionRegistry.querySerializer(ExtensibleElement.class,qname);
 				} catch (WSDLException e) {
 					return null;
 				}
 				
 				// Deserialize the DOM element and add the new Extensibility element to the parent
 				// ExtensibleElement
 				DocumentFragment fragment=document.createDocumentFragment();
 				try {
 				    serializer.marshall(ExtensibleElement.class,qname,extensibilityElement,fragment,process,extensionRegistry);
 					Element child = (Element)fragment.getFirstChild();
 					expressionElement.appendChild(child);
 				} catch (WSDLException e) {
 					throw new WrappedException(e);
 				}
 				
 			} else {
 				CDATASection cdata = BPELUtils.createCDATASection(document, expression.getBody().toString());
 				expressionElement.appendChild(cdata);
 			}
 		}
 		
 		return expressionElement;
 	}
 
 	protected Element sequence2XML(Activity activity) {
 		Element activityElement = createBPELElement("sequence");
 		List activities = ((Sequence) activity).getActivities();
 		if (!activities.isEmpty()) {
 			for (Iterator i = activities.iterator(); i.hasNext();) {
 				Activity a = (Activity) i.next();
 				activityElement.appendChild(activity2XML(a));
 			}
 		}
 		
 		return activityElement;
 	}
 	
 	protected Element sources2XML(Sources sources) {
 		Element sourcesElement = createBPELElement("sources");		
 		Iterator it = sources.getChildren().iterator();
 		while (it.hasNext()) {
 			Element sourceElement = createBPELElement("source");
 			sourcesElement.appendChild(sourceElement);
 			Source source = (Source)it.next();
 			sourceElement.setAttribute("linkName", source.getLink().getName());
 			Condition transitionCondition = source.getTransitionCondition();
 			if (transitionCondition != null) {
 				sourceElement.appendChild(expression2XML(transitionCondition, "transitionCondition"));
 			}
 			extensibleElement2XML(source, sourceElement);
 		}
 		return sourcesElement;
 	}
 	
 	protected Element targets2XML(Targets targets) {
 		Element targetsElement = createBPELElement("targets");		
 		// Write out the join condition
 		Condition joinCondition = targets.getJoinCondition();
 		if (joinCondition != null) {
 			targetsElement.appendChild(expression2XML(joinCondition, "joinCondition"));
 		}
 		// Write out each of the targets
 		Iterator it = targets.getChildren().iterator();
 		while (it.hasNext()) {
 			Element targetElement = createBPELElement("target");
 			targetsElement.appendChild(targetElement);
 			Target target = (Target)it.next();
 			targetElement.setAttribute("linkName", target.getLink().getName());
 			extensibleElement2XML(target, targetElement);
 		}
 		return targetsElement;
 	}
 	
 	protected Element onMessage2XML(OnMessage onMsg) {
 		Element onMessageElement = createBPELElement("onMessage");
 		if (onMsg.getPartnerLink() != null && onMsg.getPartnerLink().getName() != null) {
 			onMessageElement.setAttribute("partnerLink", onMsg.getPartnerLink().getName());
 		}
 		if (onMsg.getPortType() != null && onMsg.getPortType().getQName() != null) {
 			onMessageElement.setAttribute("portType", bpelNamespacePrefixManager.qNameToString(onMsg, onMsg.getPortType().getQName()));
 		}
 		if (onMsg.getOperation() != null) {
 			onMessageElement.setAttribute("operation", getOperationSignature(onMsg.getOperation()));
 		}
 		if (onMsg.getVariable() != null && onMsg.getVariable().getName() != null) {
 			onMessageElement.setAttribute("variable", onMsg.getVariable().getName());
 		}		
 		if (onMsg.getCorrelations() != null) {
 			onMessageElement.appendChild(correlations2XML(onMsg.getCorrelations()));
 		}		
 		if (onMsg.getActivity() != null) {
 			onMessageElement.appendChild(activity2XML(onMsg.getActivity()));
 		}
 		
 		Iterator it = onMsg.getFromPart().iterator();
 		while (it.hasNext()) {
 			FromPart fromPart = (FromPart)it.next();
 			onMessageElement.appendChild(fromPart2XML(fromPart));
 		}
 
 		
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(onMsg, onMessageElement);			
 
 		// TODO: Why do we have this? I don't think OnMessage is extensible.
 		extensibleElement2XML(onMsg, onMessageElement);		
 		return onMessageElement;
 	}
 	
 	protected Element onEvent2XML(OnEvent onEvent) {
 		Element onEventElement = createBPELElement("onEvent");
 		if (onEvent.getPartnerLink() != null && onEvent.getPartnerLink().getName() != null) {
 			onEventElement.setAttribute("partnerLink", onEvent.getPartnerLink().getName());
 		}
 		if (onEvent.getPortType() != null && onEvent.getPortType().getQName() != null) {
 			onEventElement.setAttribute("portType", bpelNamespacePrefixManager.qNameToString(onEvent, onEvent.getPortType().getQName()));
 		}
 		if (onEvent.getOperation() != null) {
 			onEventElement.setAttribute("operation", getOperationSignature(onEvent.getOperation()));
 		}
 		if (onEvent.getVariable() != null && onEvent.getVariable().getName() != null) {
 			onEventElement.setAttribute("variable", onEvent.getVariable().getName());
 		}	
 		if (onEvent.getMessageType() != null) {
 			onEventElement.setAttribute("messageType", bpelNamespacePrefixManager.qNameToString(onEvent, onEvent.getMessageType().getQName()));
 		}
 		if (onEvent.getCorrelations() != null) {
 			onEventElement.appendChild(correlations2XML(onEvent.getCorrelations()));
 		}
 		if (onEvent.getActivity() != null) {
 			onEventElement.appendChild(activity2XML(onEvent.getActivity()));
 		}
 		Iterator it = onEvent.getFromPart().iterator();
 		while (it.hasNext()) {
 			FromPart fromPart = (FromPart)it.next();
 			onEventElement.appendChild(fromPart2XML(fromPart));
 		}
 
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(onEvent, onEventElement);			
 
 		// TODO: Why do we have this? I don't think OnEvent is extensible.
 		extensibleElement2XML(onEvent, onEventElement);		
 		return onEventElement;
 	}
 
 	protected Element onAlarm2XML(OnAlarm onAlarm) {		
 		Element onAlarmElement = createBPELElement("onAlarm");
 		if (onAlarm.getFor() != null) {
 			onAlarmElement.appendChild(expression2XML(onAlarm.getFor(), "for"));
 		}
 		if (onAlarm.getUntil() != null) {
 			onAlarmElement.appendChild(expression2XML(onAlarm.getUntil(), "until"));
 		}
 		if (onAlarm.getRepeatEvery() != null) {
 			onAlarmElement.appendChild(expression2XML(onAlarm.getRepeatEvery(), "repeatEvery"));
 		}
 		if (onAlarm.getActivity() != null) {
 			onAlarmElement.appendChild(activity2XML(onAlarm.getActivity()));
 		}
 		// serialize local namespace prefixes to XML
 		bpelNamespacePrefixManager.serializePrefixes(onAlarm, onAlarmElement);			
 
 		// TODO: Why do we have this? I don't think OnAlarm is extensible.
 		extensibleElement2XML(onAlarm, onAlarmElement);			
 		return onAlarmElement;		
 	}
 
 	protected Element pick2XML(Activity activity) {
 		Element activityElement = createBPELElement("pick");
 		if (((Pick)activity).isSetCreateInstance()) {
 			activityElement.setAttribute("createInstance", BPELUtils.boolean2XML(((Pick)activity).getCreateInstance()));
 		}
 		for (Iterator it = ((Pick)activity).getMessages().iterator(); it.hasNext(); ) {
 			OnMessage onMsg = (OnMessage)it.next();
 			activityElement.appendChild(onMessage2XML(onMsg));			
 		}
 		for (Iterator it = ((Pick)activity).getAlarm().iterator(); it.hasNext(); ) {
 			OnAlarm onAlarm = (OnAlarm)it.next();			
 			activityElement.appendChild(onAlarm2XML(onAlarm));			
 		}
 		return activityElement;
 	}
 
 	protected Element scope2XML(Activity activity) {
 		Scope scope = (Scope)activity;
 		Element activityElement = createBPELElement("scope");
 		
 		if (scope.isSetIsolated())
 			activityElement.setAttribute("isolated", BPELUtils.boolean2XML(scope.getIsolated()));					
 		if (scope.getVariables() != null)
 			activityElement.appendChild(variables2XML(scope.getVariables()));
 		if (scope.getCorrelationSets() != null)
 			activityElement.appendChild(correlationSets2XML(scope.getCorrelationSets()));
 		if (scope.getPartnerLinks() != null)
 			activityElement.appendChild(partnerLinks2XML(scope.getPartnerLinks()));
 		if (scope.getFaultHandlers() != null )
 			activityElement.appendChild(faultHandlers2XML(scope.getFaultHandlers()));
 		if (scope.getCompensationHandler() != null )
 			activityElement.appendChild(compensationHandler2XML(scope.getCompensationHandler()));
 		if (scope.getTerminationHandler() != null )
 			activityElement.appendChild(terminationHandler2XML(scope.getTerminationHandler()));
 		if (scope.getEventHandlers() != null)
 			activityElement.appendChild(eventHandler2XML(scope.getEventHandlers()));
 		if (scope.getActivity() != null )
 			activityElement.appendChild(activity2XML(scope.getActivity()));
 		return activityElement;
 	}
 
 	protected Element compensate2XML(Activity activity) {
 		Element compensateElement = createBPELElement("compensate");
 		if ( ((Compensate)activity).getScope() != null ) {
 			Activity scopeOrInvoke = (Activity)((Compensate)activity).getScope();
 			compensateElement.setAttribute("scope",scopeOrInvoke.getName());
 		}
 		return compensateElement;
 	}
 	
 	protected QName getQName(org.eclipse.wst.wsdl.ExtensibilityElement element, String localName) {
 		EObject container = null;
 		for (container = element.eContainer(); container != null && !(container instanceof Definition); ) {
 			container = container.eContainer();
 		}
 		if (container == null) {
 			return null;
 		} 
 		return new QName(((Definition)container).getTargetNamespace(), localName);
 	}
 
 	/**
 	 * Convert a BPEL ExtensibileElement to XML
 	 */
 	protected void extensibleElement2XML(ExtensibleElement extensibleElement, Element element) {
 
 		// Get the extensibility elements and if the platform is running try to order them.
 		// If the platform is not running just serialize the elements in the order they appear.
 		List extensibilityElements;
 		if (Platform.isRunning()) {
 			if (extensibilityElementListHandlers == null) {
 				extensibilityElementListHandlers = ExtensionFactory.instance().createHandlers(ExtensionFactory.ID_EXTENSION_REORDERING); 
 			}
 			extensibilityElements = BPELUtils.reorderExtensibilityList(extensibilityElementListHandlers,extensibleElement);			
 		} else {
 			extensibilityElements = extensibleElement.getExtensibilityElements();
 		}
 		
 		// Loop through the extensibility elements
 		for (Iterator i=extensibilityElements.iterator(); i.hasNext(); ) {
 			ExtensibilityElement extensibilityElement=(ExtensibilityElement)i.next();
 			
 			// Lookup a serializer for the extensibility element
 			BPELExtensionSerializer serializer=null;
 			QName qname=extensibilityElement.getElementType();
 			try {
 				serializer=(BPELExtensionSerializer)extensionRegistry.querySerializer(ExtensibleElement.class,qname);
 			} catch (WSDLException e) {
 				// TODO: Exception handling
 			}
 			if (serializer!=null) {
 				
 				// Create a temp document fragment for the serializer
 				DocumentFragment fragment=document.createDocumentFragment();
 				
 				// Serialize the extensibility element into the parent DOM element
 				try {
 					serializer.marshall(ExtensibleElement.class,qname,extensibilityElement,fragment,process,extensionRegistry);
 				} catch (WSDLException e) {
 					throw new WrappedException(e);
 				}
 				
 				Node tempElement=(Element)fragment.getFirstChild();
 				String nodeName=tempElement.getNodeName();
 				nodeName=nodeName.substring(nodeName.lastIndexOf(':')+1);
 				if (nodeName.equals("extensibilityAttributes")) {
 					
 					// Add the attributes to the parent DOM element
 					String elementName=tempElement.getNodeName();
 					String prefix=elementName.lastIndexOf(':')!=-1? elementName.substring(0,elementName.indexOf(':')):null;
 					NamedNodeMap attributes=tempElement.getAttributes();
 					for (int a=0, n=attributes.getLength(); a<n; a++) {
 						Attr attr=(Attr)attributes.item(a);
 						String attrName=attr.getNodeName();
 						if (attrName.indexOf(':')==-1 && prefix!=null)
 							attrName=prefix+':'+attrName;
 						if (attrName.startsWith("xmlns:")) {
 							String localName = attrName.substring("xmlns:".length());
 							Map nsMap = ((BPELResource)process.eResource()).getPrefixToNamespaceMap(extensibleElement);
 							if (!nsMap.containsKey(localName) || !attr.getNodeValue().equals(nsMap.get(localName))) {
 								nsMap.put(localName, attr.getNodeValue());								
 							}																
 						} else {
 							element.setAttribute(attrName,attr.getNodeValue());
 						}
 					}
 				} else {
 					// The extensibility element was serialized into a DOM element, simply
 					// add it to the parent DOM element
 					// always append the extension element to the 
 					// begining of the children list
 					if(element.getFirstChild() == null)  
 						element.appendChild(tempElement);
 					else
 						element.insertBefore(tempElement,element.getFirstChild());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Convert a BPEL ExtensibilityElement to XML
 	 */
 	protected Element extensibilityElement2XML(ExtensibilityElement extensibilityElement) {
 			
 		BPELExtensionSerializer serializer=null;
 		QName qname=extensibilityElement.getElementType();
 		try {
 			serializer=(BPELExtensionSerializer)extensionRegistry.querySerializer(ExtensibleElement.class,qname);
 		} catch (WSDLException e) {
 			return null;
 		}
 		
 		// Deserialize the DOM element and add the new Extensibility element to the parent
 		// ExtensibleElement
 		DocumentFragment fragment=document.createDocumentFragment();
 		try {
 			serializer.marshall(ExtensibleElement.class,qname,extensibilityElement,fragment,process,extensionRegistry);
 			return (Element)fragment.getFirstChild();
 		} catch (WSDLException e) {
 			throw new WrappedException(e);
 		}
 	}	
 	
     private Element createBPELElement(String tagName) {
         if (bpelResource.getOptionUseNSPrefix()) {
             String namespaceURI = bpelResource.getNamespaceURI();
             String prefix = bpelNamespacePrefixManager.getRootPrefix(namespaceURI);
             return document.createElementNS(namespaceURI, prefix + ":" + tagName);
         } else {
             return document.createElement(tagName);
         }
     }
     
 	public NamespacePrefixManager getNamespacePrefixManager() {
 		return bpelNamespacePrefixManager;
 	}
 }
