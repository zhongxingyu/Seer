 package org.apache.servicemix.common.tools.wsdl;
 
 import java.net.URI;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.wsdl.Definition;
 import javax.wsdl.Fault;
 import javax.wsdl.Import;
 import javax.wsdl.Input;
 import javax.wsdl.Message;
 import javax.wsdl.Operation;
 import javax.wsdl.Output;
 import javax.wsdl.Part;
 import javax.wsdl.PortType;
 import javax.wsdl.Types;
 import javax.wsdl.extensions.ExtensibilityElement;
 import javax.wsdl.extensions.schema.SchemaImport;
 import javax.wsdl.factory.WSDLFactory;
 import javax.xml.namespace.QName;
 
 import com.ibm.wsdl.extensions.schema.SchemaImpl;
 
 public class WSDLFlattener {
 
     private Definition definition;
     private SchemaCollection schemas;
     private Map flattened;
     private boolean initialized;
     
     public WSDLFlattener() {
         this(null, null);
     }
         
     public WSDLFlattener(Definition definition) {
         this(definition, null);
     }
         
     public WSDLFlattener(Definition definition, SchemaCollection schemas) {
         this.definition = definition;
         this.flattened = new HashMap();
         this.schemas = schemas;
     }
     
     /**
      * Parse the schemas referenced by the definition.
      * @throws Exception if an error occurs
      */
     public void initialize() throws Exception {
         if (!initialized) {
             if (schemas == null) {
                 URI uri = null;
                 if (this.definition.getDocumentBaseURI() != null) {
                     uri = URI.create(this.definition.getDocumentBaseURI());
                 }
                 this.schemas = new SchemaCollection(uri);
             }
             parseSchemas(this.definition);
             initialized = true;
         }
     }
     
     /**
      * Retrieve a flattened definition for a given port type name.
      * @param portType the port type to create a flat definition for
      * @return a flat definition for the port type
      * @throws Exception if an error occurs
      */
     public Definition getDefinition(QName portType) throws Exception {
         Definition def = (Definition) flattened.get(portType);
         if (def == null) {
             def = flattenDefinition(portType);
             flattened.put(portType, def);
         }
         return def;
     }
 
     /**
      * @return Returns the definition.
      */
     public Definition getDefinition() {
         return definition;
     }
 
     /**
      * @param definition The definition to set.
      */
     public void setDefinition(Definition definition) {
         this.definition = definition;
     }
 
     /**
      * @return Returns the schemas.
      */
     public SchemaCollection getSchemas() throws Exception {
         return schemas;
     }
 
     /**
      * @param schemas The schemas to set.
      */
     public void setSchemas(SchemaCollection schemas) {
         this.schemas = schemas;
     }
     
     private Definition flattenDefinition(QName name) throws Exception {
         // Check that schemas have been loaded
         initialize();
         // Create new definition
         Definition flat = WSDLFactory.newInstance().newDefinition();
         flat.setTargetNamespace(name.getNamespaceURI());
         addNamespaces(flat, definition);
         // Create port type
         PortType defPort = definition.getPortType(name);
         PortType flatPort = flat.createPortType();
         flatPort.setQName(defPort.getQName());
         flatPort.setUndefined(false);
         // Import all operations and related messages
         for (Iterator itOper = defPort.getOperations().iterator(); itOper.hasNext();) {
             Operation defOper = (Operation) itOper.next();
             Operation flatOper = flat.createOperation();
             flatOper.setName(defOper.getName());
             flatOper.setStyle(defOper.getStyle());
             flatOper.setUndefined(false);
             if (defOper.getInput() != null) {
                 Input flatInput = flat.createInput();
                 flatInput.setName(defOper.getInput().getName());
                 if (defOper.getInput().getMessage() != null) {
                     Message flatInputMsg = copyMessage(defOper.getInput().getMessage(), flat);
                     flatInput.setMessage(flatInputMsg);
                     flat.addMessage(flatInputMsg);
                 }
                 flatOper.setInput(flatInput);
             }
             if (defOper.getOutput() != null) {
                 Output flatOutput = flat.createOutput();
                 flatOutput.setName(defOper.getOutput().getName());
                 if (defOper.getOutput().getMessage() != null) {
                     Message flatOutputMsg = copyMessage(defOper.getOutput().getMessage(), flat);
                     flatOutput.setMessage(flatOutputMsg);
                     flat.addMessage(flatOutputMsg);
                 }
                 flatOper.setOutput(flatOutput);
             }
             for (Iterator itFault = defOper.getFaults().values().iterator(); itFault.hasNext();) {
                 Fault defFault = (Fault) itFault.next();
                 Fault flatFault = flat.createFault();
                 flatFault.setName(defFault.getName());
                 if (defFault.getMessage() != null) {
                     Message flatFaultMsg = copyMessage(defFault.getMessage(), flat);
                     flatFault.setMessage(flatFaultMsg);
                     flat.addMessage(flatFaultMsg);
                 }
                 flatOper.addFault(flatFault);
             }
             flatPort.addOperation(flatOper);
         }
         // Get all needed direct schemas
         Set namespaces = new HashSet();
         for (Iterator itMsg = flat.getMessages().values().iterator(); itMsg.hasNext();) {
             Message msg = (Message) itMsg.next();
             for (Iterator itPart = msg.getParts().values().iterator(); itPart.hasNext();) {
                 Part part = (Part) itPart.next();
                 QName elemName = part.getElementName();
                namespaces.add(elemName.getNamespaceURI());
                Schema schema = schemas.getSchema(elemName.getNamespaceURI());
                if (schema.getImports() != null) {
                    for (Iterator iter = schema.getImports().iterator(); iter.hasNext();) {
                        String ns = (String) iter.next();
                        namespaces.add(ns);
                     }
                 }
             }
         }
         // Import schemas in definition
         if (namespaces.size() > 0) {
             Types types = flat.createTypes();
             for (Iterator iter = namespaces.iterator(); iter.hasNext();) {
                 String ns = (String) iter.next();
                 javax.wsdl.extensions.schema.Schema imp = new SchemaImpl();
                 imp.setElement(schemas.getSchema(ns).getRoot());
                 imp.setElementType(new QName("http://www.w3.org/2001/XMLSchema", "schema"));
                 types.addExtensibilityElement(imp);
             }
             flat.setTypes(types);
         }
         flat.addPortType(flatPort);
         return flat;
     }
     
     private void parseSchemas(Definition def) throws Exception {
         if (def.getTypes() != null && def.getTypes().getExtensibilityElements() != null) {
             for (Iterator iter = def.getTypes().getExtensibilityElements().iterator(); iter.hasNext();) {
                 ExtensibilityElement element = (ExtensibilityElement) iter.next();
                 if (element instanceof javax.wsdl.extensions.schema.Schema) {
                     javax.wsdl.extensions.schema.Schema schema = (javax.wsdl.extensions.schema.Schema) element;
                     for (Iterator itImp = schema.getImports().values().iterator(); itImp.hasNext();) {
                         Collection imps = (Collection) itImp.next();
                         for (Iterator itSi = imps.iterator(); itSi.hasNext();) {
                             SchemaImport imp = (SchemaImport) itSi.next();
                             schemas.read(imp.getSchemaLocationURI(), null);
                         }
                     }
                 }
             }
         }
         if (def.getImports() != null) {
             for (Iterator itImp = def.getImports().values().iterator(); itImp.hasNext();) {
                 Collection imps = (Collection) itImp.next();
                 for (Iterator iter = imps.iterator(); iter.hasNext();) {
                     Import imp = (Import) iter.next();
                     parseSchemas(imp.getDefinition());
                 }
             }
         }
     }
 
     private void addNamespaces(Definition flat, Definition def) {
        for (Iterator itNs = def.getNamespaces().keySet().iterator(); itNs.hasNext();) {
            String key = (String) itNs.next();
            String val = def.getNamespace(key);
            flat.addNamespace(key, val);
        }
         for (Iterator itImport = def.getImports().values().iterator(); itImport.hasNext();) {
             List defImports = (List) itImport.next();
             for (Iterator iter = defImports.iterator(); iter.hasNext();) {
                 Import defImport = (Import) iter.next();
                 addNamespaces(flat, defImport.getDefinition());
             }
         }
     }
     
     private Message copyMessage(Message defMessage, Definition flat) {
         Message flatMsg = flat.createMessage();
         flatMsg.setUndefined(false);
         if (defMessage.getQName() != null) {
             flatMsg.setQName(new QName(flat.getTargetNamespace(), defMessage.getQName().getLocalPart()));
         }
         for (Iterator itPart = defMessage.getParts().values().iterator(); itPart.hasNext();) {
             Part defPart = (Part) itPart.next();
             Part flatPart = flat.createPart();
             flatPart.setName(defPart.getName());
             flatPart.setElementName(defPart.getElementName());
             flatMsg.addPart(flatPart);
         }
         return flatMsg;
     }
 
 }
