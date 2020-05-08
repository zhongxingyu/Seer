 package com.cedarsoft.serialization.test.utils;
 
 import com.cedarsoft.xml.XmlCommons;
 import com.sun.org.apache.xerces.internal.dom.DeferredNode;
 import org.junit.*;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.ByteArrayInputStream;
 import java.io.StringWriter;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class DomTest {
   @Test
   public void testIt() throws Exception {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     factory.setNamespaceAware( true );
     DocumentBuilder documentBuilder = factory.newDocumentBuilder();
 
     Document doc = documentBuilder.parse( new ByteArrayInputStream( "<a/>".getBytes() ) );
 
     Element element = doc.getDocumentElement();
     assertThat( element ).isNotNull();
     assertThat( element.getTagName() ).isEqualTo( "a" );
     assertThat( element.getNamespaceURI() ).isEqualTo( null );
 
     element.setAttribute( "daAttr", "daval" );
 
     element.appendChild( doc.createElementNS( "manuallyChangedChildNS", "DaNewChild" ) );
     element.appendChild( doc.createElement( "child2WithoutNS" ) );
 
     new XmlNamespaceTranslator()
         .addTranslation( null, "MyNS" )
         .translateNamespaces( doc, false );
 
     StringWriter out = new StringWriter();
     XmlCommons.out( doc, out );
 
     assertThat( out.toString() ).isEqualTo( "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                                                 "<a daAttr=\"daval\" xmlns=\"MyNS\">\n" +
                                                 "  <DaNewChild xmlns=\"manuallyChangedChildNS\"/>\n" +
                                                 "  <child2WithoutNS/>\n" +
                                                 "</a>\n" );
   }
 
 }
