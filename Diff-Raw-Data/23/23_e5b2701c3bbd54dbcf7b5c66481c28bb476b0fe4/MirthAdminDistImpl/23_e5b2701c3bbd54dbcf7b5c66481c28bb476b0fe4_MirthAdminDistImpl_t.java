 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.cophm.mirth;
 
 import org.cophm.util.*;
 import org.cophm.validation.ErrorSeverity;
 import org.cophm.validation.HL7Validator;
 import org.cophm.validation.HL7ValidatorException;
 import org.cophm.validation.Parser;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 
 import com.mirth.connect.model.converters.ER7Serializer;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Marshaller;
 import javax.xml.namespace.QName;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.List;
 /**
  *
  * @author dunnek
  */
 public class MirthAdminDistImpl {
     private org.apache.log4j.Logger       logger = org.apache.log4j.Logger.getLogger(this.getClass());
 
 
     public String buildXMLMessageWithHeader(gov.hhs.fha.nhinc.common.nhinccommonadapter.RespondingGatewaySendAlertMessageType body) throws PropertyAccessException, JDOMException, IOException {
         HL7Validator    validator;
         String          message = "";
 
         PropertyAccessor.loadAdapterProperties("AdminDistributionSource.properties");
 
         try {
             validator = new HL7Validator(PropertyAccessor.getProperty(Constants.ADMIN_DIST_HOLD_DIR_PROPERTY_NAME),
                                          PropertyAccessor.getProperty(Constants.ADMIN_DIST_REPORT_DIR_PROPERTY_NAME),
                                          PropertyAccessor.getProperty(Constants.ADMIN_DIST_VALIDATION_RULES_PROPERTY_NAME));
         }
         catch(HL7ValidatorException e) {
             logger.error("Caught an HL7ValidatorException: " + e.toString());
             return "";
         }
 
         try
         {
             ER7Serializer     er7Serializer;
             SAXBuilder        builder = new SAXBuilder();
             XMLOutputter      xmlOutputter;
             Document          doc;
             Element           root;
             Element           currentElement;
             Element           contentObjectElement;
             Element           xmlContentElement;
             Element           embeddedXMLContentElement;
             Element           nonXmlContentElement;
             Element           edxlDistributionElement;
             StringReader      reader;
             StringWriter      writer = new StringWriter();
             List              alternateNamespace;
             String            dataToValidate;
 
 
             QName             qName = new QName("urn:gov:hhs:fha:nhinc:common:nhinccommonadapter","RespondingGateway_SendAlertMessage");
             JAXBElement       element = new JAXBElement(qName,body.getClass(),body);
 
 
             javax.xml.bind.JAXBContext      jaxbContext = javax.xml.bind.JAXBContext.newInstance(body.getClass());
 
 
             Marshaller      marshaller = jaxbContext.createMarshaller();
             marshaller.marshal( element, writer);
             message = writer.toString();
 
             logger.error("MAD - 1");
             logger.error(message);
             logger.error("MAD - 2");
 
             reader = new StringReader(message);
             doc = builder.build(reader);
             root = doc.getRootElement();
             if(root == null) {
                 return "";
             }
 
             //
             // Walk through the Admin Distribution XML to get to the HL7 data.
             //
             // This is what the Admin Distribution XML looks like if the payload is in XML format.
             //  <RespondingGateway_SendAlertMessage>
             //      <EDXLDistribution>
             //          <contentObject>
             //              <xmlContent>
             //                  <embeddedXMLContent>
             //                      <HL7Message>      This is the "root" element of the transmitted data.
             //
             // This is what the Admin Distribution XML looks like if the payload is in HL7 (non-XML) format.
             //  <RespondingGateway_SendAlertMessage>
             //      <EDXLDistribution>
             //          <contentObject>
             //              <nonXMLContent>
             //                  <mimeType>
             //                  <size>
             //                  <digest>
             //                  <uri>
             //                  <contentData>  Base 64 encoded data  </contentData>
             //
             alternateNamespace = root.getAdditionalNamespaces();
 
             try {
                 currentElement = root;
                 currentElement = getChild(currentElement, XMLDefs.EDXL_DISTRIBUTION, alternateNamespace);
                 edxlDistributionElement = currentElement;
                 currentElement = getChild(currentElement, XMLDefs.CONTENT_OBJECT, alternateNamespace);
                 contentObjectElement = currentElement;
                 currentElement = getChild(currentElement, XMLDefs.XML_CONTENT, alternateNamespace, true);
 
                 if(currentElement == null) {
                     er7Serializer = new ER7Serializer();
 
                     currentElement = contentObjectElement;
 
                     currentElement = getChild(currentElement, XMLDefs.NON_XML_CONTENT, alternateNamespace);
                     nonXmlContentElement = currentElement;
                     currentElement = getChild(currentElement, XMLDefs.CONTENT_DATA, alternateNamespace);
 
                     dataToValidate = new String(Base64Coder.decode(currentElement.getText().trim()));
 
                     logger.error("MAD - 2.1");
                     //
                     // At this point, the data should not be XML data since it is contained in an
                     // Element named "nonXMLContent", but we will check just to make sure someone
                     // did not base 64 encode an XML string.
                     //
                     if(Parser.inputIsXML(dataToValidate) == false) {
                         logger.error("MAD - 2.2");
                         dataToValidate = er7Serializer.toXML(dataToValidate);
                         //
                         // Now we need to detach the non-XML content and then attach the converted
                         // data to the original XML, after building the necessary additional XML
                         // elements.
                         //
                         xmlContentElement = new Element(XMLDefs.XML_CONTENT).setNamespace(edxlDistributionElement.getNamespace());
                         embeddedXMLContentElement = new Element(XMLDefs.EMBEDDED_XML_CONTENT).setNamespace(edxlDistributionElement.getNamespace());;
                         xmlContentElement.addContent(embeddedXMLContentElement);
                         embeddedXMLContentElement.addContent(convertStringToXmlElements(dataToValidate,
                                                                                         edxlDistributionElement.getNamespace()));
 
                         nonXmlContentElement.detach();
                         contentObjectElement.addContent(xmlContentElement);
 
                         //
                         // Now we need to convert the modified XML element graph back to a string
                         // so it cam be passed along.
                         //
                         xmlOutputter = new XMLOutputter();
                         writer = new StringWriter();
 
                         xmlOutputter.output(doc, writer);
 
                         message = writer.toString();
 
                         logger.error("MAD - 2.3");
                     }
                 }
                 else {
                     currentElement = getChild(currentElement, XMLDefs.EMBEDDED_XML_CONTENT, alternateNamespace);
                     currentElement = getChild(currentElement, XMLDefs.HL7_MESSAGE, alternateNamespace);
 
                     root.detach();
                     currentElement.detach();
 
                     doc.setRootElement(currentElement);
 
                     xmlOutputter = new XMLOutputter();
                     writer = new StringWriter();
 
                     xmlOutputter.output(doc, writer);
 
                     dataToValidate = writer.toString();
                 }
             }
             catch(JDOMException e) {
                 logger.error(e.getMessage());
                 return "";
             }
 
             logger.error("MAD - 3");
             logger.error(message);
             logger.error("MAD - 4");
 
 
             validator.loadData(dataToValidate);
             validator.validateData();
         }
         catch(Exception ex)
         {
             logger.error(ex.getMessage(), ex);
         }
 
         if(validator.getMaxErrorSeverity().ordinal() > ErrorSeverity.REPORT.ordinal()) {
             logger.error("MAD - 5");
             logger.error("Validation errors were found with the input data.  Check the validation reports for details.");
             return "";
         }
         else {
             logger.error("MAD - 6");
             return message;
         }
     }
 
     public Element  getChild(Element  parent, String nameOfChild, List nameSpaces)
             throws JDOMException {
         return getChild(parent, nameOfChild, nameSpaces, false);
     }
 
     public Element  getChild(Element  parent, String nameOfChild, List nameSpaces, boolean returnNull)
             throws JDOMException {
         Element       child = null;
 
         child = parent.getChild(nameOfChild, parent.getNamespace());
 
         if(child == null) {
             for(int  x = 0; x < nameSpaces.size(); x++) {
                 child = parent.getChild(nameOfChild, (Namespace)nameSpaces.get(x));
                 if(child != null) {
                     break;
                 }
             }
 
             if(child == null) {
                 if(returnNull) {
                     return null;
                 }
                 child = parent.getChild(nameOfChild);
                 if(child == null) {
                     throw new JDOMException("Could not find Element: " + nameOfChild);
                 }
             }
         }
 
         return child;
     }
 
     private Element  convertStringToXmlElements(String  inputXmlString, Namespace  namespace)
             throws IOException {
         SAXBuilder      builder = new SAXBuilder();
         String          xmlHeader;
         Document        doc;
         Element         root;
 
         if(inputXmlString.startsWith("<?")) {
             xmlHeader = "";
         }
         else {
             xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
         }
 
         try {
             doc = builder.build(new StringReader(xmlHeader + inputXmlString));
 
             root = doc.getRootElement();
 
             root.detach();
 
             return root.setNamespace(namespace);
         }
         catch(JDOMException e) {
             logger.error("Caught a " + e.getClass().getName() + ": " + e.toString());
             logger.error("XML Data = [" + inputXmlString + "]");
             throw new IOException(e.getMessage());
         }
     }
 }
