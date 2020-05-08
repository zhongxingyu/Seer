 package org.schematron.filter;
 
 import org.schematron.commons.XMLWriter;
 import org.schematron.exception.SchematronException;
 import org.schematron.model.Assertion;
 import org.schematron.model.SchematronResult;
 import org.schematron.model.SchematronResultImpl;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.XMLFilterImpl;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import java.io.ByteArrayOutputStream;
 import java.io.OutputStreamWriter;
 
 /**
  * ValidationFilter parse the SVRL validation-report and generates events to ErrorHandler if an error is
  * encountered. It also flags if the validated document is invalid or not. This filter is used by
  * {@link org.schematron.validation.SchematronValidator}.
  * @author bard.langoy
  */
 public class SVRLSchematronResultTransformer implements SchematronResultTransformer {
 
     public SchematronResult transform(InputSource source) throws SchematronException {
         try {
             return new SVRLXMLFilter().transform(source);
         } catch (SAXException e) {
             throw new SchematronException("Error occured when transforming Schematron Validation Report Language (SVRL) to SchematronResult.", e);
         }
     }
 
     private class SVRLXMLFilter extends XMLFilterImpl {
         protected ElementPath currentElement;
         protected boolean valid = true;
         protected SchematronResultImpl result;
 
         private SVRLXMLFilter() throws SAXException {
             super();
             setParent(XMLReaderFactory.createXMLReader());
             setContentHandler(new XMLWriter(new OutputStreamWriter(new ByteArrayOutputStream())));
         }
 
         public SchematronResult transform(InputSource source) throws SchematronException {
             result = new SchematronResultImpl();
             currentElement = null;
             valid = true;
             try {
                 parse(source);
                 return result;
             } catch (Exception e) {
                 throw new SchematronException("Error occured when transforming Schematron Validation Report Language (SVRL) to SchematronResult.", e);
             }
         }
 
         public void startElement(String namespaceURI, String localName, String qualifiedName, Attributes atts) throws SAXException {
             currentElement = new ElementPath(localName, currentElement);
             if (currentElement.name.equals("failed-assert")) {
                 currentElement.setMessage(atts.getValue("test"));
                 currentElement.setLevel(convertFlagToValidationLevel(atts.getValue("flag")));
 
                 if (currentElement.getLevel() == ValidationLevel.ERROR || currentElement.getLevel() == ValidationLevel.FATAL) {
                     valid = false;
                 }
             }
 
             super.startElement(namespaceURI, localName, qualifiedName, atts);
         }
 
         private ValidationLevel convertFlagToValidationLevel(String value) {
             ValidationLevel result = null;
            if ("warning".equals(value.toLowerCase())) {
                 result = ValidationLevel.WARN;
             } else if ("error".equals(value.toLowerCase())) {
                 result = ValidationLevel.ERROR;
             } else if ("fatal".equals(value.toLowerCase())) {
                 result = ValidationLevel.FATAL;
             }
 
             if (result == null) {
                 throw new SchematronException("Unable to parse validation error level [" + value + "] when executing validation filter for test [" + currentElement.getMessage() + "].");
             }
 
             return result;
         }
 
         @Override
         public void characters(char[] ch, int start, int length) throws SAXException {
             if (currentElement.name.equals("text")) {
                 StringBuilder message = new StringBuilder();
                message.append(ch, start, length);
                 message.append(" ");
                 message.append(currentElement.parent.getLevel().getDisplayString());
                 message.append(" when performing test [");
                 message.append(currentElement.parent.getMessage());
                 message.append("].");
 
                 if (currentElement.parent.getLevel() == ValidationLevel.ERROR) {
                    result.getErrors().add(new Assertion(currentElement.parent.name, message.toString()));
                 } else if (currentElement.parent.getLevel() == ValidationLevel.WARN) {
                    result.getWarnings().add(new Assertion(currentElement.parent.name, message.toString()));
                 } else if (currentElement.parent.getLevel() == ValidationLevel.FATAL) {
                    result.getFatals().add(new Assertion(currentElement.parent.name, message.toString()));
                 }
             }
 
             super.characters(ch, start, length);
         }
 
         public void endElement(String namespaceURI, String localName, String qualifiedName) throws SAXException {
             currentElement = currentElement.parent;
             super.endElement(namespaceURI, localName, qualifiedName);
         }
     }
 }
