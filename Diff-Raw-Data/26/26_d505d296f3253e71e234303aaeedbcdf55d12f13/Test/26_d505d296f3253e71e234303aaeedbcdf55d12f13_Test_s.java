 package com.rackspace.xerces;
 
 import javax.xml.validation.*;
 import javax.xml.transform.stream.*;
 
 public class Test {
    public static void main(String[] args) {
       if (args.length != 2) {
          System.err.println ("Usage: need XSD and instance doc");
          return;
       }
 
       try {
          System.setProperty ("javax.xml.validation.SchemaFactory:http://www.w3.org/XML/XMLSchema/v1.1", "org.apache.xerces.jaxp.validation.XMLSchema11Factory");
 
          String xsd = args[0];
          String instance = args[1];
 
          SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
          factory.setFeature ("http://apache.org/xml/features/validation/cta-full-xpath-checking", true);
 
          Schema s = factory.newSchema(new StreamSource(xsd));
         s.newValidator().validate(new StreamSource(instance));
 
       } catch (Exception e) {
          e.printStackTrace();
       }
    }
 }
