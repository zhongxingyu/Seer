 package com.rackspace.xerces;
 
 import javax.xml.validation.*;
 import javax.xml.transform.stream.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
 
 public class Test {
    public static void main(String[] args) {
       if (args.length != 2) {
          System.err.println ("Usage: need XSD and instance doc");
          return;
       }
 
       try {
          System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/XML/XMLSchema/v1.1", "org.apache.xerces.jaxp.validation.XMLSchema11Factory");
         EH eh = new EH();
 
          String xsd = args[0];
          String instance = args[1];
 
          SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
          factory.setFeature ("http://apache.org/xml/features/validation/cta-full-xpath-checking", true);
         factory.setErrorHandler(eh);
 
          Schema s = factory.newSchema(new StreamSource(xsd));
         Validator v = s.newValidator();
         v.setErrorHandler(eh);
         v.validate(new StreamSource(instance));
 
       } catch (Exception e) {
          e.printStackTrace();
       }
    }
 }

class EH implements ErrorHandler {
   public void error(SAXParseException spe) throws SAXParseException {
      throw spe;
   }

   public void fatalError(SAXParseException spe) throws SAXParseException {
      throw spe;
   }

   public void warning(SAXParseException spe) throws SAXParseException {
      throw spe;
   }
}
