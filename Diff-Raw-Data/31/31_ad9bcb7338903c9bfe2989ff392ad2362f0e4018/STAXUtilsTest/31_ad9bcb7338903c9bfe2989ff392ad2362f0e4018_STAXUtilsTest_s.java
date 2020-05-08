 package org.codehaus.xfire.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.StringReader;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.codehaus.xfire.AbstractXFireTest;
 import org.dom4j.Document;
 import org.dom4j.io.DOMReader;
 import org.dom4j.io.SAXReader;
 import org.w3c.dom.Element;
 
 /**
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Oct 26, 2004
  */
 public class STAXUtilsTest
     extends AbstractXFireTest
 {
     public void testAmazonDoc() throws Exception
     {
         String outS = doCopy("amazon.xml");
         
        SAXReader sax = new SAXReader();
        Document doc = sax.read( new StringReader(outS) );
         
         addNamespace("a", "http://xml.amazon.com/AWSECommerceService/2004-08-01");
         assertValid("/a:ItemLookup", doc);
         assertValid("/a:ItemLookup/a:Request/a:IdType", doc);
     }
 
     public void testAmazonDoc2() throws Exception
     {
         String outS = doCopy("amazon2.xml");
         
        SAXReader sax = new SAXReader();
        Document doc = sax.read( new StringReader(outS) );
         
         addNamespace("a", "http://webservices.amazon.com/AWSECommerceService/2004-10-19");
         assertValid("//a:ItemLookupResponse", doc);
         assertValid("//a:ItemLookupResponse/a:Items", doc);
     }
    
     /**
      * @return
      * @throws FactoryConfigurationError
      * @throws XMLStreamException
      */
     private String doCopy(String resource) throws FactoryConfigurationError, XMLStreamException
     {
         XMLInputFactory ifactory = XMLInputFactory.newInstance();
         XMLStreamReader reader = ifactory.createXMLStreamReader(getClass().getResourceAsStream(resource));
         
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         XMLOutputFactory factory = XMLOutputFactory.newInstance();
         XMLStreamWriter writer = factory.createXMLStreamWriter(out);
         
         writer.writeStartDocument();
         STAXUtils.copy(reader, writer);
         writer.writeEndDocument();
         
         writer.close();
         String outS = out.toString();
         
         return outS;
     }
     
     /*
     public void testDOMWrite() throws Exception
     {
         org.w3c.dom.Document doc = DOMUtils.readXml(getResourceAsStream("amazon.xml"));
         
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         XMLOutputFactory ofactory = XMLOutputFactory.newInstance();
         XMLStreamWriter writer = ofactory.createXMLStreamWriter(bos);
         
         STAXUtils.writeElement(doc.getDocumentElement(), writer);
         
         writer.close();
         
         Document testDoc = readDocument(bos.toString());
         addNamespace("a", "http://webservices.amazon.com/AWSECommerceService/2004-10-19");
         assertValid("//a:ItemLookupResponse", testDoc);
         assertValid("//a:ItemLookupResponse/a:Items", testDoc);
     }*/
     
     public void testDOMRead() throws Exception
     {
         XMLInputFactory ifactory = XMLInputFactory.newInstance();
         XMLStreamReader reader = ifactory.createXMLStreamReader(getResourceAsStream("amazon2.xml"));
         
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         org.w3c.dom.Document doc = builder.newDocument();
         Element root = doc.createElement("root");
         doc.appendChild(root);
         
         STAXUtils.readElements(root, reader);
         
         DOMReader domReader = new DOMReader();
         Document testDoc = domReader.read(doc);
 
         addNamespace("a", "http://webservices.amazon.com/AWSECommerceService/2004-10-19");
         assertValid("//a:ItemLookupResponse", testDoc);
         assertValid("//a:ItemLookupResponse/a:Items", testDoc);
         assertValid("//a:OperationRequest/a:HTTPHeaders/a:Header[@Name='UserAgent']", testDoc);
     }
 }
