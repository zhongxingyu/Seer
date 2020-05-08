 package com.timgroup.status;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentType;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import static org.junit.Assert.assertEquals;
 
 public class StatusPageTest {
     
     private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;
     static {
         DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
         DOCUMENT_BUILDER_FACTORY.setValidating(true);
     }
     
     private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
     
     @Test
     public void unconfiguredStatusPageRendersBasicXMLStructure() throws Exception {
         StatusPage statusPage = new StatusPage("myapp");
         
         long renderTime = System.currentTimeMillis();
         Document document = render(statusPage);
         
         Element root = document.getDocumentElement();
         assertEquals("myapp", root.getAttribute("id"));
         assertEquals("ok", root.getAttribute("class"));
         
         assertEquals(0, root.getElementsByTagName("component").getLength());
         
         Element timestamp = getSingleElementByTagName(root, "timestamp");
         
         assertEquals(iso8601(renderTime), timestamp.getTextContent());
     }
     
     @Test
     public void canAddAnInformativeComponentStatus() throws Exception {
         StatusPage statusPage = new StatusPage("myapp");
         statusPage.addComponent(new Component("mycomponent", "Number of coincidences today") {
             
             @Override
             public Report getReport() {
                 return new Report(Status.INFO, 23);
             }
             
         });
         
         Document document = render(statusPage);
         
         Element root = document.getDocumentElement();
         assertEquals("ok", root.getAttribute("class"));
         
         Element component = getSingleElementByTagName(root, "component");
         assertEquals("mycomponent", component.getAttribute("id"));
         assertEquals("info", component.getAttribute("class"));
         assertEquals("Number of coincidences today: 23", component.getTextContent());
         assertEquals("23", getSingleElementByTagName(component, "value").getTextContent());
     }
     
     @Test
     public void canAddANormativeComponentStatus() throws Exception {
         StatusPage statusPage = new StatusPage("myapp");
         statusPage.addComponent(new Component("mycomponent", "Number of coincidences today") {
             
             @Override
             public Report getReport() {
                return new Report(Status.WARN, 23);
             }
             
         });
         
         Document document = render(statusPage);
         
         Element root = document.getDocumentElement();
        assertEquals("warn", root.getAttribute("class"));
         
         Element component = getSingleElementByTagName(root, "component");
         assertEquals("mycomponent", component.getAttribute("id"));
        assertEquals("warn", component.getAttribute("class"));
         assertEquals("Number of coincidences today: 23", component.getTextContent());
         assertEquals("23", getSingleElementByTagName(component, "value").getTextContent());
     }
     
     @Test
     public void canAddANormativeComponentStatusWithoutAValue() throws Exception {
         StatusPage statusPage = new StatusPage("myapp");
         statusPage.addComponent(new Component("mycomponent", "Eschatological immanency") {
             
             @Override
             public Report getReport() {
                return new Report(Status.WARN);
             }
             
         });
         
         Document document = render(statusPage);
         
         Element root = document.getDocumentElement();
        assertEquals("warn", root.getAttribute("class"));
         
         Element component = getSingleElementByTagName(root, "component");
         assertEquals("mycomponent", component.getAttribute("id"));
        assertEquals("warn", component.getAttribute("class"));
         assertEquals("Eschatological immanency", component.getTextContent());
         assertEquals(0, component.getElementsByTagName("value").getLength());
     }
     
     private String iso8601(long time) {
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
         df.setTimeZone(UTC);
         return df.format(time);
     }
     
     private Element getSingleElementByTagName(Element element, String name) {
         NodeList elementsByTagName = element.getElementsByTagName(name);
         assertEquals(1, elementsByTagName.getLength());
         return (Element) elementsByTagName.item(0);
     }
     
     private Document render(StatusPage statusPage) throws ParserConfigurationException, SAXException, IOException {
         StringWriter writer = new StringWriter();
         
         statusPage.render(writer);
         
         InputSource source = new InputSource(new StringReader(writer.toString()));
         Document document = parse(source);
         DocumentType doctype = document.getDoctype();
         String doctypeSystemId = doctype.getSystemId();
         assertEquals("status-page.dtd", doctypeSystemId);
         return document;
     }
     
     private Document parse(InputSource source) throws ParserConfigurationException, SAXException, IOException {
         DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
         
         final List<SAXParseException> problems = new ArrayList<SAXParseException>();
         
         builder.setEntityResolver(new EntityResolver() {
             
             public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                 String filename;
                 try {
                     filename = new File(new URI(systemId).getPath()).getName();
                 } catch (URISyntaxException e) {
                     throw new SAXException(e);
                 }
                 InputStream stream = StatusPage.class.getResourceAsStream(filename);
                 return (stream != null) ? new InputSource(stream) : null;
             }
         });
         
         builder.setErrorHandler(new ErrorHandler() {
             
             public void warning(SAXParseException exception) throws SAXException {
                 problems.add(exception);
             }
             
             public void fatalError(SAXParseException exception) throws SAXException {
                 problems.add(exception);
             }
             
             public void error(SAXParseException exception) throws SAXException {
                 problems.add(exception);
             }
         });
         
         Document document = builder.parse(source);
         
         assertEquals(Collections.emptyList(), problems);
         
         return document;
     }
     
     /*
      * measure time each component takes
      * 
      * polling/cached components
      * 
      * boolean components
      * 
      * metric components with a threshold (use Comparable)
      * 
      * boolean component attached to a clicker with success() and failure()
      * calls, and a lastClick time
      */
     
 }
