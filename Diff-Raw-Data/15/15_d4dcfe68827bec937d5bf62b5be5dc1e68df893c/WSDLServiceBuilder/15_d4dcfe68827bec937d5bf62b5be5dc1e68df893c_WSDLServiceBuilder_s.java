 package org.codehaus.xfire.wsdl11.parser;
 
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.wsdl.Definition;
 import javax.wsdl.Fault;
 import javax.wsdl.Import;
 import javax.wsdl.Input;
 import javax.wsdl.Message;
 import javax.wsdl.Operation;
 import javax.wsdl.Output;
 import javax.wsdl.Part;
 import javax.wsdl.Port;
 import javax.wsdl.PortType;
 import javax.wsdl.Types;
 import javax.wsdl.WSDLException;
 import javax.wsdl.extensions.ExtensibilityElement;
 import javax.wsdl.extensions.UnknownExtensibilityElement;
 import javax.wsdl.factory.WSDLFactory;
 import javax.xml.namespace.QName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.ws.commons.schema.XmlSchema;
 import org.apache.ws.commons.schema.XmlSchemaCollection;
 import org.apache.ws.commons.schema.XmlSchemaComplexType;
 import org.apache.ws.commons.schema.XmlSchemaElement;
 import org.apache.ws.commons.schema.XmlSchemaObject;
 import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
 import org.apache.ws.commons.schema.XmlSchemaSequence;
 import org.codehaus.xfire.XFireFactory;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.service.FaultInfo;
 import org.codehaus.xfire.service.MessageInfo;
 import org.codehaus.xfire.service.MessagePartContainer;
 import org.codehaus.xfire.service.MessagePartInfo;
 import org.codehaus.xfire.service.OperationInfo;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceInfo;
 import org.codehaus.xfire.service.binding.BindingProvider;
 import org.codehaus.xfire.transport.TransportManager;
 import org.codehaus.xfire.util.ClassLoaderUtils;
 import org.codehaus.xfire.wsdl.SchemaType;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 
 /**
  * Builds a collection of Services from a WSDL.
  * 
  * @author Dan Diephouse
  * @see org.codehaus.xfire.service.Service
  */
 public class WSDLServiceBuilder
 {
     private static final Log log = LogFactory.getLog(WSDLServiceBuilder.class);
     
     private PortType portType;
     private OperationInfo opInfo;
     private XmlSchemaCollection schemas = new XmlSchemaCollection();;
     private boolean isWrapped = false;
     private BindingProvider bindingProvider;
     
     protected final Definition definition;
 
     private List bindingAnnotators = new ArrayList();
     
     private Map portType2serviceInfo = new HashMap();
     private Map wop2op = new HashMap();
     private Map winput2msg = new HashMap();
     private Map woutput2msg = new HashMap();
     private Map wfault2msg = new HashMap();
     
     private List schemaInfos = new ArrayList();
     private List definitions = new ArrayList();
     private List definitionPaths = new ArrayList();
     private List portTypes = new ArrayList();
     private List types = new ArrayList();
     private List wsdlServices = new ArrayList();
 
     /** A collection of XFire Service classes that were built. */
     private Map xFireServices = new HashMap();
     private List allServices = new ArrayList();
 
     private TransportManager transportManager =
         XFireFactory.newInstance().getXFire().getTransportManager();
     private Service service;
     
     
     public WSDLServiceBuilder(Definition definition)
     {
         this.definition = definition;
         definitions.add(definition);
         
         bindingAnnotators.add(new SoapBindingAnnotator());
     }
 
     public WSDLServiceBuilder(InputStream is) throws WSDLException
     {
         this(WSDLFactory.newInstance().newWSDLReader().readWSDL(null, new InputSource(is)));
     }
 
     public WSDLServiceBuilder(String baseURI, InputStream is) throws WSDLException
     {
         this(WSDLFactory.newInstance().newWSDLReader().readWSDL(baseURI, new InputSource(is)));
     }
     
     public BindingProvider getBindingProvider()
     {
         if (bindingProvider == null)
         {
             try
             {
                 bindingProvider = (BindingProvider) ClassLoaderUtils
                         .loadClass("org.codehaus.xfire.aegis.AegisBindingProvider", getClass()).newInstance();
             }
             catch (Exception e)
             {
                 throw new XFireRuntimeException("Couldn't find a binding provider!", e);
             }
         }
 
         return bindingProvider;
     }
     
     public void setBindingProvider(BindingProvider bindingProvider)
     {
         this.bindingProvider = bindingProvider;
     }
 
     public Definition getDefinition()
     {
         return definition;
     }
 
     public List getDefinitions()
     {
         return definitions;
     }
 
     public TransportManager getTransportManager()
     {
         return transportManager;
     }
 
     public void setTransportManager(TransportManager transportManager)
     {
         this.transportManager = transportManager;
     }
 
     public void build() throws Exception
     {
         processImports(definition);
         
         // Import all the types..
         types.add(definition.getTypes());
         for (Iterator itr = types.iterator(); itr.hasNext();)
         {
             visit((Types) itr.next());
         }
         
         portTypes.addAll(definition.getPortTypes().values());
         for (Iterator itr = portTypes.iterator(); itr.hasNext();)
         {
             portType = (PortType) itr.next();
             visit(portType);
         }
         
         wsdlServices.addAll(definition.getServices().values());
         for (Iterator iterator = wsdlServices.iterator(); iterator.hasNext();)
         {
             javax.wsdl.Service wservice = (javax.wsdl.Service) iterator.next();
             Map portType2Ports = getPortTypeToPortMap(wservice);
             
             for (Iterator ptitr = portType2Ports.entrySet().iterator(); ptitr.hasNext();)
             {
                 Map.Entry entry = (Map.Entry) ptitr.next();
                 
                 PortType portType = (PortType) entry.getKey();
                 Collection ports = (Collection) entry.getValue();
                 
                 if (ports.size() == 0) continue;
                 
                 ServiceInfo serviceInfo = getServiceInfo(portType);
                 WSDLServiceConfigurator config = new WSDLServiceConfigurator(serviceInfo,
                                                                              definition,
                                                                              wservice, 
                                                                              portType,
                                                                              ports,
                                                                              bindingProvider,
                                                                              transportManager);
                 config.configure();
                 addService(config.getService());
             }
         }
     }
 
     /**
      * Adds a service to the map of services and also to the list of all services.
      * @param s
      */
     protected void addService(Service s)
     {
         List services = (List) xFireServices.get(s.getName());
         if (services == null)
         {
             services = new ArrayList();
             xFireServices.put(s.getName(), services);
         }
         services.add(s);
         allServices.add(s);
     }
 
     protected void processImports(Definition parent)
     {
         Collection imports = parent.getImports().values();
         for (Iterator iterator = imports.iterator(); iterator.hasNext();)
         {
             List wsdlImports = (List) iterator.next();
             for (Iterator importItr = wsdlImports.iterator(); importItr.hasNext();)
             {
                 Import i = (Import) importItr.next();
                 
                 Definition iDef = i.getDefinition();
                 
                 if (!definitionPaths.contains(i.getLocationURI())) 
                 {
                     log.info("Adding wsdl definition " + i.getLocationURI() +
                              " with baseURI of " + parent.getDocumentBaseURI());
                     
                     definitionPaths.add(i.getLocationURI());
                     
                     definitions.add(iDef);
                     types.add(iDef.getTypes());
                     portTypes.addAll(iDef.getPortTypes().values());
                     wsdlServices.addAll(iDef.getServices().values());
                     
                     processImports(iDef);
                 }
             }
         }
     }
 
     private Map getPortTypeToPortMap(javax.wsdl.Service wservice)
     {
         Map pt2port = new HashMap();
         
         for (Iterator itr = portTypes.iterator(); itr.hasNext();)
         {
             PortType pt = (PortType) itr.next();
             List ports = new ArrayList();
             pt2port.put(pt, ports);
             
             for (Iterator pitr = wservice.getPorts().values().iterator(); pitr.hasNext();)
             {
                 Port port = (Port) pitr.next();
                 
                 if (port.getBinding().getPortType().equals(pt))
                 {
                     ports.add(port);
                 }
             }
         }
         
         return pt2port;
     }
 
     /**
      * Gets a Map of Services. The key is the service name and the value is a list
      * of services with that name.
      * @return
      */
     public Map getServices()
     {
         return xFireServices;
     }
     
     /**
      * Gets a list of every service created.
      * @return
      */
     public List getAllServices()
     {
         return allServices;
     }
     
     protected void visit(Types types)
     {
         if (types == null) return;
         
         for (Iterator itr = types.getExtensibilityElements().iterator(); itr.hasNext();)
         {
             ExtensibilityElement ee = (ExtensibilityElement) itr.next();
             Element el = null;
             if (ee instanceof UnknownExtensibilityElement)
             {
                 UnknownExtensibilityElement uee = (UnknownExtensibilityElement) ee;
                 el = uee.getElement();
             }
             else
             {
                 // if we are using wsdl4j >= 1.5.1, a specific extensibility
             	// element is defined for schemas, so try retrieve the element
             	try 
             	{
             		Method mth = ee.getClass().getMethod("getElement", new Class[0]);
             		Object val = mth.invoke(ee, new Object[0]);
                     el = (Element) val;
             	} 
                 catch (Exception e) {e.printStackTrace();}
             }
             
             schemas.setBaseUri(definition.getDocumentBaseURI());
             XmlSchema schema = schemas.read(el);
             
             SchemaInfo schemaInfo = new SchemaInfo();
             schemaInfo.setDefinition(definition);
             schemaInfo.setSchema(schema);
             schemaInfo.setSchemaElement(el);
             
             schemaInfos.add(schemaInfo);
         }
     }
     
     protected void visit(PortType portType)
     {
         ServiceInfo serviceInfo = new ServiceInfo(null, Object.class);
         portType2serviceInfo.put(portType, serviceInfo);
         serviceInfo.setPortType(portType.getQName());
 
         isWrapped = true;
         Iterator itr = portType.getOperations().iterator();
         while (isWrapped && itr.hasNext())
         {
            Operation o = (Operation) itr.next();
            isWrapped = isWrapped(o, schemas);
         }
         
         serviceInfo.setWrapped(isWrapped);
         
         List operations = portType.getOperations();
         for (int i = 0; i < operations.size(); i++)
         {
             Operation operation = (Operation) operations.get(i);
             visit(operation);
             {
                 Input input = operation.getInput();
                 visit(input);
             }
             {
                 Output output = operation.getOutput();
                 if (output != null)
                     visit(output);
             }
             
             Collection faults = operation.getFaults().values();
             for (Iterator iterator2 = faults.iterator(); iterator2.hasNext();)
             {
                 Fault fault = (Fault) iterator2.next();
                 visit(fault);
             }
         }
     }
     
     protected ServiceInfo getServiceInfo(PortType portType)
     {
         return (ServiceInfo) portType2serviceInfo.get(portType);
     }
 
     protected void visit(Fault fault)
     {
         FaultInfo faultInfo = opInfo.addFault(fault.getName()); 
         wfault2msg.put(fault, faultInfo);
         
         createMessageParts(faultInfo, fault.getMessage());
     }
 
     protected void visit(Input input)
     {
         MessageInfo info = opInfo.createMessage(input.getMessage().getQName());
         winput2msg.put(input, info);
         
         opInfo.setInputMessage(info);
         
         if (isWrapped)
         {
             createMessageParts(info, getWrappedSchema(input.getMessage()));
         }
         else
         {
             createMessageParts(info,  input.getMessage());
         }
     }
 
     protected void visit(Operation operation)
     {
         opInfo = getServiceInfo(portType).addOperation(operation.getName(), null);
         wop2op.put(operation, opInfo);
     }
 
     private void createMessageParts(MessageInfo info, XmlSchemaComplexType type)
     {
         if (type.getParticle() instanceof XmlSchemaSequence)
         {
             XmlSchemaSequence seq = (XmlSchemaSequence) type.getParticle();
 
             XmlSchemaObjectCollection col = seq.getItems();
             for (Iterator itr = col.getIterator(); itr.hasNext();)
             {
                 XmlSchemaObject schemaObj = (XmlSchemaObject) itr.next();
                 
                 if (schemaObj instanceof XmlSchemaElement)
                 {
                     createMessagePart(info,  (XmlSchemaElement) schemaObj);
                 }
             }
         }
     }
 
     private void createMessagePart(MessageInfo info, XmlSchemaElement element)
     {
         int index = info.size();
         MessagePartInfo part = info.addMessagePart(element.getQName(), XmlSchemaElement.class);
         part.setIndex(index);
         
         SchemaType st = null;
         if (element.getRefName() != null)
         {
             st = getBindingProvider().getSchemaType(element.getRefName(), service);
         }
         else if (element.getSchemaTypeName() != null)
         {
             st = getBindingProvider().getSchemaType(element.getSchemaTypeName(), service);
         }
 
         part.setSchemaType(st);
     }
 
     /**
      * A message is wrapped IFF:
      * 
      * The input message has a single part. 
      * The part is an element. 
      * The element has the same name as the operation. 
      * The element's complex type has no attributes.
      * 
      * @return
      */
     public static boolean isWrapped(Operation op, XmlSchemaCollection schemas)
     {
         Input input = op.getInput();
         Output output = op.getOutput();
         if (output == null || output.getMessage().getParts() == null) return false;
         
         if (input.getMessage().getParts().size() != 1 || 
             output.getMessage().getParts().size() != 1) 
             return false;
         
         Part inPart = (Part) input.getMessage().getParts().values().iterator().next();
         Part outPart = (Part) output.getMessage().getParts().values().iterator().next();
         
         QName inElementName = inPart.getElementName();
         QName outElementName = outPart.getElementName();
         if (inElementName == null || outElementName == null) 
             return false;
         
         if (!inElementName.getLocalPart().equals(op.getName()) || 
                 !outElementName.getLocalPart().equals(op.getName() + "Response")) 
             return false;
         
         XmlSchemaElement reqSchemaEl = schemas.getElementByQName(inElementName);
         XmlSchemaElement resSchemaEl = schemas.getElementByQName(outElementName);
 
         if (reqSchemaEl == null) 
             throw new XFireRuntimeException("Couldn't find schema for part: " + inElementName);
 
         // Now lets see if we have any attributes...
         // This should probably look at the restricted and substitute types too.
         if (reqSchemaEl.getSchemaType() instanceof XmlSchemaComplexType)
         {
             if (hasAttributes((XmlSchemaComplexType) reqSchemaEl.getSchemaType()))
                 return false;
         }
         
         if (resSchemaEl.getSchemaType() instanceof XmlSchemaComplexType)
         {
             if (hasAttributes((XmlSchemaComplexType) resSchemaEl.getSchemaType()))
                 return false;
             
            if (!hasOneElement((XmlSchemaComplexType) resSchemaEl.getSchemaType()))
                 return false;
         }
 
         return true;
     }
     
     
    private static boolean hasOneElement(XmlSchemaComplexType type)
     {
        if (type.getParticle() != null && type.getParticle() instanceof XmlSchemaSequence)
         {
             XmlSchemaSequence seq = (XmlSchemaSequence) type.getParticle();
             XmlSchemaObjectCollection items = seq.getItems();
 
             if (items.getCount() == 0) 
                 return true;
             else if (items.getCount() > 1) 
                 return false;
             
             XmlSchemaObject o = items.getItem(0);
             if (!(o instanceof XmlSchemaElement)) return false;
             
             XmlSchemaElement el = (XmlSchemaElement) o;
             
             if (el.getMaxOccurs() > 1) return false;
 
             // If this is an anonymous complex type, mark it as unwrapped.
             // We're doing this because things like JAXB don't have support
             // for finding classes from anonymous type names.
             if (el.getSchemaTypeName() == null && el.getRefName() == null)
                 return false;
             
             return true;
         }
         
         return false;
     }
 
     private XmlSchemaComplexType getWrappedSchema(Message message)
     {
         Part part = (Part) message.getParts().values().iterator().next();
         
         XmlSchemaElement schemaEl = schemas.getElementByQName(part.getElementName());
         
         return (XmlSchemaComplexType) schemaEl.getSchemaType();
     }
     
     protected static boolean hasAttributes(XmlSchemaComplexType complexType)
     {
         // Now lets see if we have any attributes...
         // This should probably look at the restricted and substitute types too.
         
         if (complexType.getAnyAttribute() != null ||
                 complexType.getAttributes().getCount() > 0)
             return true;
         else
             return false;
     }
     
     private void createMessageParts(MessagePartContainer info, Message msg)
     {
         Map parts = msg.getParts();
         
         for (Iterator itr = parts.values().iterator(); itr.hasNext();)
         {
             Part entry = (Part) itr.next();
             
             // We're extending an abstract schema type
             QName typeName = entry.getTypeName();
             if (typeName != null)
             {
                 QName partName = new QName(getTargetNamespace(), entry.getName());
                 MessagePartInfo part = info.addMessagePart(typeName, null);
                 part.setSchemaElement(false);
                 part.setSchemaType(getBindingProvider().getSchemaType(typeName, service));
             }
 
             // We've got a concrete schema type
             QName elementName = entry.getElementName();
             if (elementName != null)
             {
                 MessagePartInfo part = info.addMessagePart(elementName, null);
 
                 part.setSchemaType(getBindingProvider().getSchemaType(typeName, service));
             }
         }
     }
 
     protected String getTargetNamespace()
     {
         return getDefinition().getTargetNamespace();
     }
 
     protected void visit(Output output)
     {
         MessageInfo info = opInfo.createMessage(output.getMessage().getQName());
         opInfo.setOutputMessage(info);
         woutput2msg.put(output, info);
         
         if (isWrapped)
         {
             createMessageParts(info, getWrappedSchema(output.getMessage()));
         }
         else
         {
             createMessageParts(info, output.getMessage());
         }
     }
 
     /**
      * Returns a Collection of SchemaInfo objects.
      * @return
      */
     public List getSchemas()
     {
         return schemaInfos;
     }
     
     public XmlSchemaCollection getSchemaCollection()
     {
         return schemas;
     }
 }
