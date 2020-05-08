 package org.mule.galaxy.wsi.wsdl;
 
 import java.io.IOException;
 
 import javax.wsdl.Definition;
 import javax.xml.XMLConstants;
 import javax.xml.transform.Source;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 /**
  * R2028, R2029 - validate the WSDL via schemas.
  */
 public class WsdlSoapSchemaValidationRule extends AbstractWsdlRule {
     
 
     private SchemaFactory schemaFactory;
     private Schema wsdlSoapSchema;
 
     public WsdlSoapSchemaValidationRule() throws SAXException {
         super("R2029");
         schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 
        // TODO GALAXY-48 need proper logging libs
         System.out.println("============= schemaFactory = " + schemaFactory);
 
         Source wsdlSoapSchemaSource = new StreamSource(getClass().getResourceAsStream("/org/mule/galaxy/wsi/wsdl/wsdl-2004-08-24.xsd"));
         wsdlSoapSchema = schemaFactory.newSchema(wsdlSoapSchemaSource);
     }
 
     public ValidationResult validate(Document document, Definition def) {
         ValidationResult result = new ValidationResult();
         try {
             SchemaErrorHandler errorHandler = new SchemaErrorHandler("R2029");
             Validator wsdlSoapValidator = wsdlSoapSchema.newValidator();
             wsdlSoapValidator.setErrorHandler(errorHandler);
             wsdlSoapValidator.validate(new DOMSource(document));
             
             if (errorHandler.hasErrors()) {
                 result.addAssertionResult(errorHandler.getAssertionResult());
             }
         } catch (SAXException e) {
             result.addAssertionResult(new AssertionResult("R2029", true, e.getMessage()));
         } catch (IOException e) {
             result.addAssertionResult(new AssertionResult("R2029", true, e.getMessage()));
         }
         
         return result;
     }
 
 }
