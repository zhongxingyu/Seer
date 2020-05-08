 
package com.fluidtime.exampleMVC.statistic_client.org.tempuri;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.annotation.XmlElementDecl;
 import javax.xml.bind.annotation.XmlRegistry;
 import javax.xml.namespace.QName;
 
 
 /**
  * This object contains factory methods for each 
  * Java content interface and Java element interface 
  * generated in the org.tempuri package. 
  * <p>An ObjectFactory allows you to programatically 
  * construct new instances of the Java representation 
  * for XML content. The Java representation of XML 
  * content can consist of schema derived interfaces 
  * and classes representing the binding of schema 
  * type definitions, element declarations and model 
  * groups.  Factory methods for each of these are 
  * provided in this class.
  * 
  */
 @XmlRegistry
 public class ObjectFactory {
 
     private final static QName _GetStatisticValueUsername_QNAME = new QName("http://tempuri.org/", "username");
 
     /**
      * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tempuri
      * 
      */
     public ObjectFactory() {
     }
 
     /**
      * Create an instance of {@link GetStatisticValue }
      * 
      */
     public GetStatisticValue createGetStatisticValue() {
         return new GetStatisticValue();
     }
 
     /**
      * Create an instance of {@link GetStatisticValueResponse }
      * 
      */
     public GetStatisticValueResponse createGetStatisticValueResponse() {
         return new GetStatisticValueResponse();
     }
 
     /**
      * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
      * 
      */
     @XmlElementDecl(namespace = "http://tempuri.org/", name = "username", scope = GetStatisticValue.class)
     public JAXBElement<String> createGetStatisticValueUsername(String value) {
         return new JAXBElement<String>(_GetStatisticValueUsername_QNAME, String.class, GetStatisticValue.class, value);
     }
 
 }
