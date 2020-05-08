 /*
  * Copyright [2005] [University Corporation for Advanced Internet Development, Inc.]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.opensaml.xml.io;
 
 import javax.xml.namespace.QName;
 
 import org.apache.log4j.Logger;
 import org.opensaml.xml.Configuration;
 import org.opensaml.xml.Namespace;
 import org.opensaml.xml.XMLObject;
 import org.opensaml.xml.XMLObjectBuilder;
 import org.opensaml.xml.XMLObjectBuilderFactory;
 import org.opensaml.xml.util.DatatypeHelper;
 import org.opensaml.xml.util.XMLConstants;
 import org.opensaml.xml.util.XMLHelper;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 /**
  * An thread safe abstract unmarshaller. This unmarshaller will:
  * <ul>
  * <li>Unmarshalling namespace decleration attributes</li>
  * <li>Unmarshalling schema instance type (xsi:type) decleration attributes</li>
  * <li>Delegating to child classes element, text, and attribute processing</li>
  * </ul>
  * 
  * <strong>NOTE:</strong> In the case of Text nodes this unmarshaller will use {@link org.w3c.dom.Text#getWholeText()}
  * to retrieve the textual content. This is probably exceptable in almost all cases, if, however, you need to deal with
  * elements that contain multiple text node children you will need to override
  * {@link #unmarshallTextContent(XMLObject, Text)} and do "the right thing" for your implementation.
  */
 public abstract class AbstractXMLObjectUnmarshaller implements Unmarshaller {
 
     /** Logger */
     private static Logger log = Logger.getLogger(AbstractXMLObjectUnmarshaller.class);
 
     /** The target name and namespace for this unmarshaller. */
     private QName targetQName;
 
     /** Factory for XMLObjectBuilders */
     private XMLObjectBuilderFactory xmlObjectBuilderFactory;
 
     /** Factory for creating unmarshallers for child elements */
     private UnmarshallerFactory unmarshallerFactory;
 
     /**
      * Constructor.
      */
     protected AbstractXMLObjectUnmarshaller() {
         xmlObjectBuilderFactory = Configuration.getBuilderFactory();
         unmarshallerFactory = Configuration.getUnmarshallerFactory();
     }
 
     /**
      * This constructor supports checking a DOM Element to be unmarshalled, either element name or schema type, against
      * a given namespace/local name pair.
      * 
      * @param targetNamespaceURI the namespace URI of either the schema type QName or element QName of the elements this
      *            unmarshaller operates on
      * @param targetLocalName the local name of either the schema type QName or element QName of the elements this
      *            unmarshaller operates on
      * 
      * @throws IllegalArgumentException if any of the arguments are null (or empty in the case of String parameters)
      */
     protected AbstractXMLObjectUnmarshaller(String targetNamespaceURI, String targetLocalName)
             throws IllegalArgumentException {
         if (DatatypeHelper.isEmpty(targetNamespaceURI)) {
             throw new IllegalArgumentException("Target Namespace URI may not be null or an empty");
         }
 
         if (DatatypeHelper.isEmpty(targetLocalName)) {
             throw new IllegalArgumentException("Target Local Name may not be null or an empty");
         }
         targetQName = XMLHelper.constructQName(targetNamespaceURI, targetLocalName, null);
 
         xmlObjectBuilderFactory = Configuration.getBuilderFactory();
         unmarshallerFactory = Configuration.getUnmarshallerFactory();
     }
 
     /** {@inheritDoc} */
     public XMLObject unmarshall(Element domElement) throws UnmarshallingException {
         if (log.isDebugEnabled()) {
             log.debug("Starting to unmarshall DOM element " + XMLHelper.getNodeQName(domElement));
         }
 
         checkElementIsTarget(domElement);
 
         XMLObject xmlObject = buildXMLObject(domElement);
 
         if (log.isDebugEnabled()) {
             log.debug("Unmarshalling attributes of DOM Element " + XMLHelper.getNodeQName(domElement));
         }
         NamedNodeMap attributes = domElement.getAttributes();
         Node attribute;
         for (int i = 0; i < attributes.getLength(); i++) {
             attribute = attributes.item(i);
 
             // These should allows be attribute nodes, but just in case...
             if (attribute.getNodeType() == Node.ATTRIBUTE_NODE) {
                 unmarshallAttribute(xmlObject, (Attr) attribute);
             }
         }
 
         if (log.isDebugEnabled()) {
             log.debug("Unmarshalling other child nodes of DOM Element " + XMLHelper.getNodeQName(domElement));
         }
         NodeList childNodes = domElement.getChildNodes();
         Node childNode;
         for (int i = 0; i < childNodes.getLength(); i++) {
             childNode = childNodes.item(i);
 
             if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                 unmarshallAttribute(xmlObject, (Attr) childNode);
             } else if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                 unmarshallChildElement(xmlObject, (Element) childNode);
             } else if (childNode.getNodeType() == Node.TEXT_NODE) {
                 unmarshallTextContent(xmlObject, (Text) childNode);
             }
         }
 
         xmlObject.setDOM(domElement);
         return xmlObject;
     }
 
     /**
      * Checks that the given DOM Element's XSI type or namespace qualified element name matches the target QName of this
      * unmarshaller.
      * 
      * @param domElement the DOM element to check
      * 
      * @throws UnmarshallingException thrown if the DOM Element does not match the target of this unmarshaller
      */
     protected void checkElementIsTarget(Element domElement) throws UnmarshallingException {
         QName elementName = XMLHelper.getNodeQName(domElement);
 
         if (targetQName == null) {
             if (log.isDebugEnabled()) {
                 log.debug("Targeted QName checking is not available for this unmarshaller, DOM Element " + elementName
                         + " was not verified");
             }
 
             return;
         }
 
         if (log.isDebugEnabled()) {
             log.debug("Checking that " + elementName + " meets target criteria.");
         }
 
         QName type = XMLHelper.getXSIType(domElement);
 
         if (type != null && type.equals(targetQName)) {
             if (log.isDebugEnabled()) {
                 log.debug(elementName + " schema type matches target.");
             }
             return;
         } else {
             if (elementName.equals(targetQName)) {
                 if (log.isDebugEnabled()) {
                     log.debug(elementName + " element name matches target.");
                 }
                 return;
             } else {
                String errorMsg = "This unmarshaller only operates on " + targetQName + " elements not "
                         + elementName;
                 log.error(errorMsg);
                 throw new UnmarshallingException(errorMsg);
             }
         }
     }
 
     /**
      * Constructs the XMLObject that the given DOM Element will be unmarshalled into. If the DOM element has an XML
      * Schema type defined this method will attempt to retrieve an XMLObjectBuilder, from the factory given at
      * construction time, using the schema type. If no schema type is present or no builder is registered with the
      * factory for the schema type, the elements QName is used. Once the builder is found the XMLObject is create by
      * invoking {@link XMLObjectBuilder#buildObject(String, String, String)}. Extending classes may wish to override
      * this logic if more than just schema type or element name (e.g. element attributes or content) need to be used to
      * determine which XMLObjectBuilder should be used to create the XMLObject.
      * 
      * @param domElement the DOM Element the created XMLObject will represent
      * 
      * @return the empty XMLObject that DOM Element can be unmarshalled into
      * 
      * @throws UnmarshallingException thrown if there is now XMLObjectBuilder registered for the given DOM Element
      */
     protected XMLObject buildXMLObject(Element domElement) throws UnmarshallingException {
         if (log.isDebugEnabled()) {
             log.debug("Building XMLObject for " + XMLHelper.getNodeQName(domElement));
         }
         XMLObjectBuilder xmlObjectBuilder;
 
         xmlObjectBuilder = xmlObjectBuilderFactory.getBuilder(domElement);
         if (xmlObjectBuilder == null) {
             xmlObjectBuilder = xmlObjectBuilderFactory.getBuilder(Configuration.getDefaultProviderQName());
             if (xmlObjectBuilder == null) {
                 String errorMsg = "Unable to located builder for " + XMLHelper.getNodeQName(domElement);
                 log.error(errorMsg);
                 throw new UnmarshallingException(errorMsg);
             } else {
                 if (log.isDebugEnabled()) {
                     log.debug("No builder was registered for " + XMLHelper.getNodeQName(domElement)
                             + " but the default builder " + xmlObjectBuilder.getClass().getName()
                             + " was available, using it.");
                 }
             }
         }
 
         return xmlObjectBuilder.buildObject(domElement);
     }
 
     /**
      * Unmarshalls the attributes from the given DOM Attr into the given XMLObject. If the attribute is an XML namespace
      * declaration the attribute is passed to
      * {@link AbstractXMLObjectUnmarshaller#unmarshallNamespaceAttribute(XMLObject, Attr)}. If it is an schema type
      * decleration (xsi:type) it is ignored because this attribute is handled by {@link #buildXMLObject(Element)}. All
      * other attributes are passed to the {@link #processAttribute(XMLObject, Attr)}
      * 
      * @param attribute the attribute to be unmarshalled
      * @param xmlObject the XMLObject that will recieve information from the DOM attribute
      * 
      * @throws UnmarshallingException thrown if there is a problem unmarshalling an attribute
      */
     protected void unmarshallAttribute(XMLObject xmlObject, Attr attribute) throws UnmarshallingException {
         if (log.isDebugEnabled()) {
             log.debug("Pre-processing attribute " + XMLHelper.getNodeQName(attribute));
         }
 
         String attributeNamespace = DatatypeHelper.safeTrimOrNullString(attribute.getNamespaceURI());
         if (DatatypeHelper.safeEquals(attributeNamespace, XMLConstants.XMLNS_NS)) {
             if (log.isDebugEnabled()) {
                 log.debug(XMLHelper.getNodeQName(attribute)
                         + " is a namespace declaration, adding it to the list of namespaces on the XMLObject");
             }
             unmarshallNamespaceAttribute(xmlObject, attribute);
         } else if (DatatypeHelper.safeEquals(attributeNamespace, XMLConstants.XSI_NS)
                 && DatatypeHelper.safeEquals(attribute.getLocalName(), "type")) {
             // Skip over schema type declerations as they are handled by the builder
         } else {
             if (log.isDebugEnabled()) {
                 log.debug("Attribute " + XMLHelper.getNodeQName(attribute)
                         + " is neither a schema type nor namespace, calling processAttribute()");
             }
             String attributeNSURI = attribute.getNamespaceURI();
             String attributeNSPrefix;
             if (attributeNSURI != null) {
                 attributeNSPrefix = attribute.lookupPrefix(attributeNSURI);
                 Namespace attributeNS = new Namespace(attributeNSURI, attributeNSPrefix);
                 attributeNS.setAlwaysDeclare(false);
                 xmlObject.addNamespace(attributeNS);
             }
 
             processAttribute(xmlObject, attribute);
         }
     }
 
     /**
      * Unmarshalls a namespace declaration attribute.
      * 
      * @param xmlObject the xmlObject to recieve the namespace decleration
      * @param attribute the namespace decleration attribute
      */
     protected void unmarshallNamespaceAttribute(XMLObject xmlObject, Attr attribute) {
         Namespace namespace = new Namespace(attribute.getValue(), attribute.getLocalName());
         namespace.setAlwaysDeclare(true);
         xmlObject.addNamespace(namespace);
     }
 
     /**
      * Unmarshalls given Element's children. For each child an unmarshaller is retrieved using
      * {@link UnmarshallerFactory#getUnmarshaller(Element)}. The unmarshaller is then used to unmarshall the child
      * element and the resultant XMLObject is passed to {@link #processChildElement(XMLObject, XMLObject)} for further
      * processing.
      * 
      * @param xmlObject the parent object of the unmarshalled children
      * @param childElement the child element to be unmarshalled
      * 
      * @throws UnmarshallingException thrown if an error occurs unmarshalling the chilren elements
      */
     protected void unmarshallChildElement(XMLObject xmlObject, Element childElement) throws UnmarshallingException {
         if (log.isDebugEnabled()) {
             log.debug("Unmarshalling child elements of XMLObject " + xmlObject.getElementQName());
         }
 
         Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(childElement);
 
         if (unmarshaller == null) {
             unmarshaller = unmarshallerFactory.getUnmarshaller(Configuration.getDefaultProviderQName());
             if (unmarshaller == null) {
                 String errorMsg = "No unmarshaller available for " + XMLHelper.getNodeQName(childElement)
                         + ", child of " + xmlObject.getElementQName();
                 log.error(errorMsg);
                 throw new UnmarshallingException(errorMsg);
             } else {
                 if (log.isDebugEnabled()) {
                     log.debug("No unmarshaller was registered for " + XMLHelper.getNodeQName(childElement)
                             + ", child of " + xmlObject.getElementQName() + " but the default unmarshaller "
                             + unmarshaller.getClass().getName() + " was available, using it.");
                 }
             }
         }
 
         if (log.isDebugEnabled()) {
             log.debug("Unmarshalling child element " + XMLHelper.getNodeQName(childElement) + " with unmarshaller "
                     + unmarshaller.getClass().getName());
         }
 
         processChildElement(xmlObject, unmarshaller.unmarshall(childElement));
     }
 
     /**
      * Unmarshalls the given Text node into a usable string by way of {@link Text#getWholeText()} and passes it off to
      * {@link AbstractXMLObjectUnmarshaller#processElementContent(XMLObject, String)} if the string is not null and
      * contains something other than whitespace.
      * 
      * @param xmlObject the XMLObject recieving the element content
      * @param content the textual content
      * 
      * @throws UnmarshallingException thrown if there is a problem unmarshalling the text node
      */
     protected void unmarshallTextContent(XMLObject xmlObject, Text content) throws UnmarshallingException {
         String textContent = DatatypeHelper.safeTrimOrNullString(content.getWholeText());
         if (textContent != null) {
             processElementContent(xmlObject, textContent);
         }
     }
 
     /**
      * Called after a child element has been unmarshalled so that it can be added to the parent XMLObject.
      * 
      * @param parentXMLObject the parent XMLObject
      * @param childXMLObject the child XMLObject
      * 
      * @throws UnmarshallingException thrown if there is a problem adding the child to the parent
      */
     protected abstract void processChildElement(XMLObject parentXMLObject, XMLObject childXMLObject)
             throws UnmarshallingException;
 
     /**
      * Called after an attribute has been unmarshalled so that it can be added to the XMLObject.
      * 
      * @param xmlObject the XMLObject
      * @param attribute the attribute
      * 
      * @throws UnmarshallingException thrown if there is a problem adding the attribute to the XMLObject
      */
     protected abstract void processAttribute(XMLObject xmlObject, Attr attribute) throws UnmarshallingException;
 
     /**
      * Called if the element being unmarshalled contained textual content so that it can be added to the XMLObject.
      * 
      * @param xmlObject XMLObject the content will be given to
      * @param elementContent the Element's content
      */
     protected abstract void processElementContent(XMLObject xmlObject, String elementContent);
 }
