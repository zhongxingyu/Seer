 //
 // This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
 // See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
 // Any modifications to this file will be lost upon recompilation of the source schema. 
 // Generated on: 2011.12.05 at 11:52:42 PM GMT 
 //
 
 
 package org.openprovenance.prov.xml;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.annotation.adapters.XmlAdapter;
 
 public class AnyAdapter
     extends XmlAdapter<Object,Attribute>
 {
 
     ProvFactory pFactory=new ProvFactory();
 
     public Attribute unmarshal(Object value) {
         //System.out.println("AnyAdapter2 unmarshalling for " + value);
         //System.out.println("AnyAdapter2 unmarshalling for " + value.getClass());
         if (value instanceof org.w3c.dom.Element) {
             org.w3c.dom.Element el=(org.w3c.dom.Element)value;
             String prefix=el.getPrefix();
             String namespace=el.getNamespaceURI();
             String local=el.getLocalName();
             String child=el.getTextContent();
             String type=el.getAttributeNS(NamespacePrefixMapper.XSI_NS, "type");
             type=(type==null) ? "xsd:string" : type;
             return pFactory.newAttribute(namespace,local,prefix, pFactory.convertToJava(type, child), type);
         } 
         if (value instanceof JAXBElement) {
             JAXBElement<?> je=(JAXBElement<?>) value;
             return pFactory.newAttribute(je.getName(),je.getValue());
         }
         return null;
     }
 
     public Object marshal(Attribute attribute) {
         //System.out.println("AnyAdapter2 marshalling for " + attribute);
         //System.out.println("AnyAdapter2 marshalling for " + attribute
         //                .getClass());
         //TODO: this call creates a DOM but does not encode the type as xsi:type
	//FIXME: bug #28, need to support internationalize string
 	return pFactory.newElement(attribute.getElementName(), 
 	                           attribute.getValue().toString(),
 	                           attribute.getXsdType());
         //JAXBElement<?> je=new JAXBElement(value.getElementName(),value.getValue().getClass(),value.getValue());
         //return je;
     }
 
    
 
 }
