 package org.codehaus.xfire.aegis.type.java5;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
import org.codehaus.xfire.aegis.jdom.YOMReader;
import org.codehaus.xfire.aegis.jdom.YOMWriter;
 import org.codehaus.xfire.aegis.type.CustomTypeMapping;
 import org.codehaus.xfire.aegis.type.Type;
 import org.codehaus.xfire.aegis.type.java5.EnumType;
 import org.codehaus.xfire.aegis.type.java5.Java5TypeCreator;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.jdom.Document;
 import org.jdom.Element;
 
 public class EnumTypeTest
     extends AbstractXFireAegisTest
 {
     private CustomTypeMapping tm;
     
     private enum smallEnum { VALUE1, VALUE2 };
     
     public void setUp() throws Exception
     {
         super.setUp();
         
         tm = new CustomTypeMapping();
         Java5TypeCreator creator = new Java5TypeCreator();
         tm.setTypeCreator(creator);
     }
 
     public void testType() throws Exception
     {
         EnumType type = new EnumType();
         type.setTypeClass(smallEnum.class);
         type.setSchemaType(new QName("urn:test", "test"));
 
         tm.register(type);
         
         Element root = new Element("root");
        YOMWriter writer = new YOMWriter(root);
         
         type.writeObject(smallEnum.VALUE1, writer, new MessageContext());
         
         assertEquals("VALUE1", root.getValue());
         
        YOMReader reader = new YOMReader(root);
         Object value = type.readObject(reader, new MessageContext());
         
         assertEquals(smallEnum.VALUE1, value);
     }
     
     public void testAutoCreation() throws Exception
     {
         Type type = (Type) tm.getTypeCreator().createType(smallEnum.class);
         
         assertTrue( type instanceof EnumType );
     }
     
     public void testTypeAttributeOnEnum() throws Exception
     {
         Type type = (Type) tm.getTypeCreator().createType(TestEnum.class);
         
         assertEquals("urn:xfire:foo", type.getSchemaType().getNamespaceURI());
         
         assertTrue( type instanceof EnumType );
     }
 
     public void testWSDL() throws Exception
     {
         EnumType type = new EnumType();
         type.setTypeClass(smallEnum.class);
         type.setSchemaType(new QName("urn:test", "test"));
 
         Element root = new Element("root");
         Document wsdl = new Document(root);
         type.writeSchema(root);
 
         addNamespace("xsd", SoapConstants.XSD);
         assertValid("//xsd:simpleType[@name='test']/xsd:restriction[@base='xsd:string']", wsdl);
         assertValid("//xsd:restriction[@base='xsd:string']/xsd:enumeration[@value='VALUE1']", wsdl);
         assertValid("//xsd:restriction[@base='xsd:string']/xsd:enumeration[@value='VALUE2']", wsdl);
     }
     
     public void testCurrencyService() throws Exception
     {
         getServiceRegistry().register( getServiceFactory().create(CurrencyService.class) );
         
         Document wsdl = getWSDLDocument("CurrencyService");
 
         addNamespace("xsd", SoapConstants.XSD);
         assertValid("//xsd:simpleType[@name='Currency']/xsd:restriction[@base='xsd:string']", wsdl);
         assertValid("//xsd:restriction[@base='xsd:string']/xsd:enumeration[@value='USD']", wsdl);
         assertValid("//xsd:restriction[@base='xsd:string']/xsd:enumeration[@value='EURO']", wsdl);
         assertValid("//xsd:restriction[@base='xsd:string']/xsd:enumeration[@value='POUNDS']", wsdl);
     }
 }
