 package edu.hawaii.ihale.backend;
 
 import static org.junit.Assert.assertEquals;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Map;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleCommandType;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 
 /**
  * JUnit tests the IHaleBackend.
  * 
  * @author Bret K. Ikehara
  */
 public class TestIHaleBackend {
 
   private static IHaleBackend backend;
 
   /**
    * Cleans up the backend.
    * 
    * @throws IOException Thrown when unable to close the FileInputStream.
    * @throws SAXException Thrown when XML parsing fails.
    * @throws ParserConfigurationException Thrown if error exists in parser configuration.
    * @throws XPathExpressionException Thrown if error exists in XPath expression.
    */
   @BeforeClass
   public static void beforeClass() throws XPathExpressionException, ParserConfigurationException,
       SAXException, IOException {
     backend = new IHaleBackend();
   }
 
   /**
    * Checks the parsing the files.
    * 
    * @throws IOException Thrown when
    */
   @Test
   public void testParseURIPropertyFile() throws IOException {
     Map<String, String> uri = IHaleBackend.parseURIPropertyFile(IHaleBackend.deviceConfigRef);
 
     assertEquals("Electrical state", "http://localhost:7002/", uri.get("electrical-state"));
   }
 
   /**
   * Tests the HVAC doCommand for a successful PUT. Remove @Ignore tag when running with a
   * simulator.
    * 
    * @throws IOException Thrown when URL connection fails.
    * @throws ParserConfigurationException Thrown when building XML document.
    * @throws SAXException Thrown when parsing XML input stream.
    */
   @Ignore
   @Test
   public void doCommandHvacSystem() throws IOException, ParserConfigurationException, SAXException {
 
     URL url = null;
     URLConnection conn = null;
     Integer value = Integer.valueOf(45);
 
     DocumentBuilderFactory factory = null;
     DocumentBuilder docBuilder = null;
     Document doc = null;
     Element root = null;
     NamedNodeMap attributes = null;
 
     backend.doCommand(IHaleSystem.HVAC, null, IHaleCommandType.SET_TEMPERATURE, value);
 
     url = new URL(IHaleBackend.getURImap().get("hvac-control") + "hvac/state");
 
     conn = url.openConnection();
 
     factory = DocumentBuilderFactory.newInstance();
     docBuilder = factory.newDocumentBuilder();
     doc = docBuilder.parse(conn.getInputStream());
 
     // Check system.
     root = doc.getDocumentElement();
     assertEquals("Check command root node", "state-data", root.getTagName());
     assertEquals("Check command root node's name attribute", IHaleSystem.HVAC.toString(),
         root.getAttribute("system"));
 
     // Expected to fail until Simulator sets the value correctly.
     // Check the argument tag.
     NodeList nl = root.getChildNodes();
     for (int i = 0; i < nl.getLength(); i++) {
       if (nl.item(i).getNodeName().equals("state")) {
         attributes = nl.item(i).getAttributes();
         if (attributes.getNamedItem("key").getNodeValue().equals("TEMPERATURE")) {
           assertEquals("HVAC temperature changed", value.toString(),
               attributes.getNamedItem("value").getNodeValue());
           break;
         }
       }
     }
   }
 }
