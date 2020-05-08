 package org.codehaus.xfire.aegis.type.java5;
 
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
 import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
 
 import javax.xml.namespace.QName;
 
 import org.codehaus.xfire.aegis.AbstractXFireAegisTest;
 import org.codehaus.xfire.aegis.type.CustomTypeMapping;
 import org.codehaus.xfire.aegis.type.DefaultTypeCreator;
 import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.aegis.type.collection.MapType;
import org.codehaus.xfire.aegis.type.java5.dto.MapDTO;
import org.codehaus.xfire.aegis.type.java5.dto.MapDTOService;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.jdom.Document;
 
 public class XmlParamTypeTest
     extends AbstractXFireAegisTest
 {
     private CustomTypeMapping tm;
     private Java5TypeCreator creator;
 
     public void setUp() throws Exception
     {
         super.setUp();
         
         tm = new CustomTypeMapping();
         creator = new Java5TypeCreator();
         creator.setNextCreator(new DefaultTypeCreator());
         tm.setTypeCreator(creator);
     }
 
     public void testType() throws Exception
     {
         Method m = CustomTypeService.class.getMethod("doFoo", new Class[] { String.class });
         
         Type type = creator.createType(m, 0);
         tm.register(type);
         assertTrue( type instanceof CustomStringType );
         assertEquals( new QName("urn:xfire:foo", "custom"), type.getSchemaType());
         
         type = creator.createType(m, -1);
         tm.register(type);
         assertTrue( type instanceof CustomStringType );
         assertEquals( new QName("urn:xfire:foo", "custom"), type.getSchemaType());
     }
     
     public void testMapServiceWSDL() throws Exception
     {
         Service service = getServiceFactory().create(CustomTypeService.class);
         getServiceRegistry().register(service);
         
         Document wsdl = getWSDLDocument(service.getSimpleName());
         addNamespace("xsd", SoapConstants.XSD);
        assertValid("//xsd:element[@name='in0'][@type='ns1:custom']", wsdl);
     }
     
     public class CustomTypeService
     {
         
         @XmlReturnType(type=CustomStringType.class, 
                        namespace="urn:xfire:foo",
                        name="custom")
         public String doFoo(@XmlParamType(type=CustomStringType.class, 
                                         namespace="urn:xfire:foo",
                                         name="custom") String s) {
         	return null;
         }
     }
 }
