 package com.cedarsoft.serialization.test.utils;
 
 import com.cedarsoft.xml.XmlCommons;
 import com.sun.org.apache.xerces.internal.dom.DeferredNode;
 import org.junit.*;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
import javax.annotation.Nonnull;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.ByteArrayInputStream;
 import java.io.StringWriter;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class DomTest {
  @Nonnull
  byte[] xml = "<a/>".getBytes();

   @Test
   public void testIt() throws Exception {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     factory.setNamespaceAware( true );
     DocumentBuilder documentBuilder = factory.newDocumentBuilder();
 
    Document doc = documentBuilder.parse( new ByteArrayInputStream( xml ) );
 
     Element element = doc.getDocumentElement();
     assertThat( element ).isNotNull();
     assertThat( element.getTagName() ).isEqualTo( "a" );
     assertThat( element.getNamespaceURI() ).isEqualTo( null );
     assertThat( element ).isInstanceOf( DeferredNode.class );
 
 
     element.setAttribute( "daAttr", "daval" );
 
     element.appendChild( doc.createElementNS( "manuallyChangedChildNS", "DaNewChild" ) );
     element.appendChild( doc.createElement( "child2WithoutNS" ) );
 

     new XmlNamespaceTranslator()
      .addTranslation( null, "MyNS" )
//      .addTranslation( "", "MyNS" )
      .translateNamespaces( doc, false );


//    Element copy = doc.createElementNS( "dans", element.getTagName() );
//    NamedNodeMap attributes = element.getAttributes();
//    for ( int i = 0; i < attributes.getLength(); i++ ) {
//      Node item = attributes.item( i );
//      copy.setAttributeNodeNS(  )
//    }

 
     StringWriter out = new StringWriter();
     XmlCommons.out( doc, out );
 
    assertThat( out.toString() ).isEqualTo( "asdf" );
   }
 
 }
