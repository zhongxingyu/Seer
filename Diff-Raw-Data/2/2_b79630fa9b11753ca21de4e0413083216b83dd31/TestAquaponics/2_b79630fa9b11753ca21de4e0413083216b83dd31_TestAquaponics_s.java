 package edu.hawaii.systemh.housesimulator.aquaponics;
 
 import static org.junit.Assert.assertEquals;
 import java.io.IOException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.resource.ClientResource;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import edu.hawaii.systemh.housesimulator.SimulatorServer;
 
 /**
  * Tests the HTTP operations of the system.
  * 
  * @author Anthony Kinsey, Michael Cera
  * @author Christopher Ramelb, David Lin, Leonardo Nguyen, Nathan Dorman
  */
 public class TestAquaponics {
 
   /**
    * Start up a test server before testing any of the operations on this resource.
    * 
    * @throws Exception If problems occur starting up the server.
    */
   @BeforeClass
   public static void startServer() throws Exception {
     SimulatorServer.runServer();
   }
 
   /**
    * Tests that we can PUT a value and then GET the value and check that it is within a certain
    * delta of the PUT value.
    * 
    * @throws Exception If GET or PUT fails
    */
   @Test
   public void testGetAndPut() throws Exception {
     // Put the values to our system.
     putValue("temperature", "SET_TEMPERATURE", "29");
     putValue("feed", "FEED_FISH", "10.0");
     putValue("harvest", "HARVEST_FISH", "0");
     putValue("nutrients", "SET_NUTRIENTS", "15.0");
     putValue("ph", "SET_PH", "7.4");
     putValue("water/level", "SET_WATER_LEVEL", "42");
 
     // Speed up time simulation to see if our value falls within the desired range.
     for (int i = 0; i < 100; i++) {
       AquaponicsData.modifySystemState();
       //HVACData.modifySystemState();
       //LightingData.modifySystemState();
       //PhotovoltaicsData.modifySystemState();
       //ElectricalData.modifySystemState();
     }
 
     // Set up the GET client
     String getUrl = "http://localhost:7101/aquaponics/state";
     ClientResource getClient = new ClientResource(getUrl);
 
     // Get the XML representation.
     DomRepresentation domRep = new DomRepresentation(getClient.get());
     Document domDoc = domRep.getDocument();
 
     // Grabs tags from XML.
     NodeList xmlList = domDoc.getElementsByTagName("state");
 
     // Grabs attributes from tags.
     String keyStr = "key"; // PMD WHY ARE YOU SO PICKY? :(
     String valStr = "value";
     
     String circKey = ((Element) xmlList.item(0)).getAttribute(keyStr);
     //String circValue = ((Element) xmlList.item(0)).getAttribute(valStr);
     String deadFishKey = ((Element) xmlList.item(1)).getAttribute(keyStr);
     //String deadFishValue = ((Element) xmlList.item(1)).getAttribute(valStr);
     String ecKey = ((Element) xmlList.item(2)).getAttribute(keyStr);
     String ecValue = ((Element) xmlList.item(2)).getAttribute(valStr);
     String tempKey = ((Element) xmlList.item(3)).getAttribute(keyStr);
     String tempValue = ((Element) xmlList.item(3)).getAttribute(valStr);
     String turbKey = ((Element) xmlList.item(4)).getAttribute(keyStr);
     //String turbValue = ((Element) xmlList.item(4)).getAttribute(valStr);
     String waterKey = ((Element) xmlList.item(5)).getAttribute(keyStr);
     String waterValue = ((Element) xmlList.item(5)).getAttribute(valStr);
     String phKey = ((Element) xmlList.item(6)).getAttribute(keyStr);
     String phValue = ((Element) xmlList.item(6)).getAttribute(valStr);
     String oxygenKey = ((Element) xmlList.item(7)).getAttribute(keyStr);
     //String oxygenValue = ((Element) xmlList.item(7)).getAttribute(valStr);
 
     // Check that we are returning the correct key
     assertEquals("Checking that key is CIRCULATION", circKey, "CIRCULATION");
     assertEquals("Checking that key is DEAD_FISH", deadFishKey, "DEAD_FISH");
     assertEquals("Checking that key is ELECTRICAL_CONDUCTIVITY", ecKey, "ELECTRICAL_CONDUCTIVITY");
     assertEquals("Checking that key is TEMPERATURE", tempKey, "TEMPERATURE");
     assertEquals("Checking that key is TURBIDITY", turbKey, "TURBIDITY");
     assertEquals("Checking that key is WATER_LEVEL", waterKey, "WATER_LEVEL");
     assertEquals("Checking that key is PH", phKey, "PH");
     assertEquals("Checking that key is OXYGEN", oxygenKey, "OXYGEN");
 
     // Check that the returned value is within a delta of our PUT value.
     assertEquals(15, Double.parseDouble(ecValue), 10);
     assertEquals(29.0, Double.parseDouble(tempValue), 16);
    assertEquals(7.4, Double.parseDouble(phValue), 0.2);
     assertEquals(42, Integer.parseInt(waterValue), 8);
 
   }
 
   /**
    * Helper function that puts a value to the system.
    * 
    * @param uri The ending of the uri to call PUT on
    * @param command The name of the command being performed
    * @param value The value we want the to be set
    * @throws ParserConfigurationException If parser fails
    * @throws IOException If there is a problem building the document
    */
   public static void putValue(String uri, String command, String value)
       throws ParserConfigurationException, IOException {
     String putUrl = "http://localhost:7101/aquaponics/" + uri;
     ClientResource putClient = new ClientResource(putUrl);
 
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder docBuilder = null;
     docBuilder = factory.newDocumentBuilder();
     Document doc = docBuilder.newDocument();
 
     // Create root tag
     Element rootElement = doc.createElement("command");
     rootElement.setAttribute("name", command);
     doc.appendChild(rootElement);
 
     // Create state tag.
     Element temperatureElement = doc.createElement("arg");
     temperatureElement.setAttribute("value", value);
     rootElement.appendChild(temperatureElement);
 
     // Convert Document to DomRepresentation.
     DomRepresentation result = new DomRepresentation();
     result.setDocument(doc);
 
     putClient.put(result);
   }
 }
