 package fedora.client.bmech.xml;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Element;
 import org.w3c.dom.Attr;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.ParserConfigurationException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Vector;
 
 import fedora.client.bmech.data.*;
 import fedora.client.bmech.BMechBuilderException;
 
 /**
  *
  * <p><b>Title:</b> WSDLGenerator.java</p>
  * <p><b>Description:</b> </p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002, 2003 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author payette@cs.cornell.edu
  * @version 1.0
  */
 public class WSDLGenerator
 {
   private static final String THIS = "bmech";
 
   private static final String WSDL = "http://schemas.xmlsoap.org/wsdl/";
 
   private static final String SOAP = "http://schemas.xmlsoap.org/wsdl/soap";
 
   private static final String SOAPENC = "http://schemas.xmlsoap.org/wsdl/soap/encoding";
 
   private static final String HTTP = "http://schemas.xmlsoap.org/wsdl/http/";
 
   private static final String MIME = "http://schemas.xmlsoap.org/wsdl/mime/";
 
   private static final String XSD = "http://www.w3.org/2001/XMLSchema";
 
   private static final String XMLNS = "http://www.w3.org/2000/xmlns/";
 
 
   private Document document;
   private Element root;
   private Element types;
   private Vector messageElements;
   private Element portType;
   private Element service;
   private Element binding;
 
   public WSDLGenerator(BMechTemplate newBMech) throws BMechBuilderException
   {
     initializeTree();
     genWSDL(newBMech);
     finalizeTree();
   }
 
   private void initializeTree() throws BMechBuilderException
   {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     try
     {
         DocumentBuilder builder =   factory.newDocumentBuilder();
         document = builder.newDocument();
 
     }
     catch (ParserConfigurationException pce)
     {
       // Parser with specified options can't be built
       pce.printStackTrace();
       throw new BMechBuilderException("WSDLGenerator: error configuring parser."
         + "Underlying exception: " + pce.getMessage());
     }
     root = (Element)document.createElementNS(WSDL, "wsdl:definitions");
     types = (Element)document.createElementNS(WSDL, "wsdl:types");
     messageElements = new Vector();
     portType = (Element)document.createElementNS(WSDL, "wsdl:portType");
     service = (Element)document.createElementNS(WSDL, "wsdl:service");
     binding = (Element)document.createElementNS(WSDL, "wsdl:binding");
   }
 
   private void finalizeTree()
   {
     // put the tree together
     document.appendChild(root);
     root.appendChild(types);
     Element[] messages = (Element[])messageElements.toArray(new Element[0]);
     for (int i=0; i<messages.length; i++)
     {
       root.appendChild(messages[i]);
     }
     root.appendChild(portType);
     root.appendChild(service);
     root.appendChild(binding);
   }
 
   private void genWSDL(BMechTemplate newBMech)
   {
     // The data we gathered via the GUI
     boolean hasBaseURL = newBMech.getHasBaseURL();
     String baseURL = newBMech.getServiceBaseURL();
     String bDefPID = newBMech.getbDefContractPID();
     String bMechLabel = newBMech.getbObjLabel();
     // FIXIT!! make sure there are no spaces in middle of bMechName
     String bMechName = newBMech.getbObjName();
     Method[] methods = newBMech.getMethods();
 
     String name = (bMechLabel == null) ? "" : bMechLabel;
     root.setAttribute("name", name);
     root.setAttribute("targetNamespace", THIS);
     root.setAttributeNS(XMLNS, "xmlns:this", THIS);
     root.setAttributeNS(XMLNS, "xmlns:wsdl", WSDL);
     root.setAttributeNS(XMLNS, "xmlns:soap", SOAP);
     root.setAttributeNS(XMLNS, "xmlns:soapenc", SOAPENC);
     root.setAttributeNS(XMLNS, "xmlns:http", HTTP);
     root.setAttributeNS(XMLNS, "xmlns:mime", MIME);
     root.setAttributeNS(XMLNS, "xmlns:xsd", XSD);
     createService(bMechName, hasBaseURL, baseURL);
     processMethods(bMechName, hasBaseURL, methods);
   }
 
   private void createService(String bMechName, boolean hasBaseURL, String baseURL)
   {
     // create wsdl:service
     service.setAttribute("name", bMechName);
     Element port = (Element)document.createElementNS(WSDL, "wsdl:port");
     port.setAttribute("name", (bMechName + "_port"));
     // FIXIT! We are just assuming an HTTP port for now!!
     port.setAttribute("binding", ("this:" + bMechName + "_http"));
     Element httpAddr = (Element)document.createElementNS(HTTP, "http:address");
     if (hasBaseURL)
     {
       httpAddr.setAttribute("location", baseURL);
     }
     else
     {
       // FIXIT!! This is a Fedora-specific thing that we want to do differently
       // like maybe have a Fedora extension element for WSDL.
       httpAddr.setAttribute("location", "LOCAL");
     }
     port.appendChild(httpAddr);
     service.appendChild(port);
 
    // create wsdl:binding
    // FIXIT!! assumes only an HTTP binding at this time!!
     binding.setAttribute("name", (bMechName + "_http"));
     binding.setAttribute("type", ("this:" + bMechName + "PortType"));
    Element httpBinding = document.createElementNS(HTTP, "http:binding");
    httpBinding.setAttribute("verb", "GET");
    binding.appendChild(httpBinding);
   }
 
   private void processMethods(String bMechName, boolean hasBaseURL, Method[] methods)
   {
     Element schema = (Element)document.createElementNS(XSD, "xsd:schema");
     schema.setAttribute("targetNamespace", THIS);
     HashMap parmUnion = new HashMap();

     for (int m=0; m<methods.length; m++)
     {
       // create wsdl:message
       Element message = (Element)document.createElementNS(WSDL, "wsdl:message");
       message.setAttribute("name", (methods[m].methodName + "Request"));
 
       MethodParm[] parms = methods[m].methodProperties.methodParms;
       for (int p=0; p<parms.length; p++)
       {
         // Create wsdl:part
         Element part = (Element)document.createElementNS(WSDL, "wsdl:part");
         part.setAttribute("name", parms[p].parmName);
         part.setAttribute("type", ("this:" + parms[p].parmName + "Type"));
         message.appendChild(part);
 
         // Hold on to parm info for creation of wsdl:types
         // If the parm with this name already exists in another method
         // assume it means the same thing, and update the domain of possible
         // values for this parm with anything new showing up in the current
         // method definition.
         if (parmUnion.containsKey(parms[p].parmName))
         {
           MethodParm existingParm = (MethodParm)parmUnion.get(parms[p].parmName);
           String[] existingDomain = existingParm.parmDomainValues;
           HashSet oldVals = new HashSet();
           for (int p1=0; p1<existingDomain.length; p1++)
           {
             oldVals.add(existingDomain[p1]);
           }
           String[] newDomain = parms[p].parmDomainValues;
           HashSet newVals = new HashSet();
           for (int p2=0; p2<newDomain.length; p2++)
           {
             newVals.add(newDomain[p2]);
           }
           Set unionVals = new HashSet(oldVals);
           unionVals.addAll(newVals);
           existingParm.parmDomainValues =
             (String[])unionVals.toArray(new String[0]);
           parmUnion.put(parms[p].parmName, existingParm);
         }
         else
         {
           parmUnion.put(parms[p].parmName, parms[p]);
         }
       }
       // add wsdl:message to the message vector
       messageElements.add(message);
 
       // create wsdl:portType with one or more wsdl:operation elements
       portType.setAttribute("name", (bMechName + "PortType"));
       Element operation = document.createElementNS(WSDL, "wsdl:operation");
       operation.setAttribute("name", methods[m].methodName);
       Element input = document.createElementNS(WSDL, "wsdl:input");
       input.setAttribute("message", ("this:" + methods[m].methodName + "Request"));
       Element output = document.createElementNS(WSDL, "wsdl:output");
       output.setAttribute("message", "this:dissemResponse");
       operation.appendChild(input);
       operation.appendChild(output);
       portType.appendChild(operation);
 
       Element wsdlOperation = document.createElementNS(WSDL, "wsdl:operation");
       wsdlOperation.setAttribute("name", methods[m].methodName);
       Element httpOperation = document.createElementNS(HTTP, "http:operation");
       if (hasBaseURL)
       {
         httpOperation.setAttribute("location",
           methods[m].methodProperties.methodRelativeURL);
       }
       else
       {
         httpOperation.setAttribute("location",
           methods[m].methodProperties.methodFullURL);
       }
       Element wsdlInput = document.createElementNS(WSDL, "wsdl:input");
       wsdlInput.appendChild(document.createElementNS(HTTP, "http:urlReplacement"));
       Element wsdlOutput = document.createElementNS(WSDL, "wsdl:output");
       String[] MIMETypes = methods[m].methodProperties.returnMIMETypes;
       for (int t = 0; t < MIMETypes.length; t++)
       {
         Element MIMEType = document.createElementNS(MIME, "mime:content");
         MIMEType.setAttribute("type", MIMETypes[t]);
         wsdlOutput.appendChild(MIMEType);
       }
       wsdlOperation.appendChild(httpOperation);
       wsdlOperation.appendChild(wsdlInput);
       wsdlOperation.appendChild(wsdlOutput);
       binding.appendChild(wsdlOperation);
     }
     // end methods loop
 
     // add wsdl:message for Fedora dissemination response to wsdl:definitions
     Element responseMessage = document.createElementNS(WSDL, "wsdl:message");
     responseMessage.setAttribute("name", "dissemResponse");
     Element responseMessagePart = document.createElementNS(WSDL, "wsdl:part");
     responseMessagePart.setAttribute("name", "dissem");
     responseMessagePart.setAttribute("type", "xsd:base64Binary");
     responseMessage.appendChild(responseMessagePart);
     messageElements.add(responseMessage);
 
     // create wsdl:types
     Iterator iparm = parmUnion.values().iterator();
     while (iparm.hasNext())
     {
       MethodParm parm = (MethodParm)iparm.next();
       // XSD Schema Type element
       Element typeDef = document.createElementNS(XSD, "xsd:simpleType");
       typeDef.setAttribute("name", (parm.parmName + "Type"));
 
       // XSD Schema Type Restriction element
       Element restrict = document.createElementNS(XSD, "xsd:restriction");
       restrict.setAttribute("base", "xsd:string");
       String[] domainValues = parm.parmDomainValues;
       for (int k=0; k<domainValues.length; k++)
       {
         Element val = document.createElementNS(XSD, "xsd:enumeration");
         val.setAttribute("value", (String)domainValues[k]);
         restrict.appendChild(val);
       }
       typeDef.appendChild(restrict);
       schema.appendChild(typeDef);
     }
     types.appendChild(schema);
   }
 
   public Element getRootElement()
   {
     return document.getDocumentElement();
   }
 
   public void printWSDL()
   {
     try
     {
       String str = new XMLWriter(new DOMResult(document)).getXMLAsString();
       System.out.println(str);
     }
     catch (Exception e)
     {
       e.printStackTrace();
     }
   }
 }
