 package edu.hawaii.ihale.housesimulator;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertFalse;
 import java.io.File;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Verify that the simulator server writes a properties file, creates initial data, 
  * and verify that the server runs.
  * 
  * @author Nathan Dorman, Leonardo Nguyen
  *
  */
 public class TestSimulator {
   
   private static final String dir = ".ihale";
   private static final String propFilename = "device-urls.properties";
   private static final String xmlFilename = "initial-data.xml";
   
   /**
    * Delete device-urls.properties and initial-data.xml files and start up the server
    * before performing any further testing actions.
    * 
    * @throws Exception If problems occur starting up the server.
    */
   @BeforeClass
   public static void startServer() throws Exception {
     
     // Get the users home directory and "dir" sub-directory
     File theDir = new File(System.getProperty("user.home"), dir);
     File propFile = new File(theDir, propFilename);
     File xmlFile = new File(theDir, xmlFilename);
     
     if (propFile.exists() && !propFile.delete()) {
       System.out.println("Could not delete device-url.properties file, test ending");
       System.exit(1);
     }
     if (xmlFile.exists() && !xmlFile.delete()) {
       System.out.println("Could not delete initial-data.xml file, test ending");
       System.exit(1);
     }
     
     assertFalse("Check device-urls.properties does not exist or was successfully deleted.", 
         propFile.exists());
     assertFalse("Check initial-device.xml does not exist or was successfully deleted.", 
         xmlFile.exists());  }
   
   /**
    * Verify that both device-urls.properties and initial-data.xml file are created properly.
    * 
    * @throws Exception If problems occur generating data.
    */
   @Test
   public void testStartUpFiles() throws Exception {
     
     String[] args = {"-stepinterval", "1000"};
     SimulatorServer.main(args);
 
     File theDir = new File(System.getProperty("user.home"), dir);
     File xmlFile = new File(theDir, xmlFilename);
     assertTrue("Checking if initial-data.xml exists.", xmlFile.exists());
     
     File propFile = new File(theDir, propFilename);
     assertTrue("Checking if device-urls.properties exists.", propFile.exists());
         
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     factory.setNamespaceAware(true);
     DocumentBuilder builder = factory.newDocumentBuilder();
     Document doc = builder.parse(xmlFile);
     
     XPath xpath = XPathFactory.newInstance().newXPath();
     XPathExpression stateDataRecordCountExpr = xpath.compile("count(//state-data)");
     Object stateDataRecordCount = stateDataRecordCountExpr.evaluate(doc, XPathConstants.NUMBER);
     // There should be 12 records at 5 min intervals, 24 records at 1 hour intervals, and 31 records
     // at 1 day intervals for each of the 4 systems to initialize the initial-data.xml file.
     Double count = (12.0 + 24.0 + 31.0) * 4;
     assertEquals("Checking for proper amount of state-data records.", count, stateDataRecordCount);
    
     XPathExpression hasAquaponicsDataExpr = xpath.compile(
         "count(//state-data[@system='AQUAPONICS']) > 0");
     Object hasAquaponicsData = hasAquaponicsDataExpr.evaluate(doc, XPathConstants.BOOLEAN);
     assertTrue("Asserting there are state-data records for Aquaponics system.", 
         (Boolean) hasAquaponicsData);
     
     XPathExpression hasHvacDataExpr = xpath.compile("count(//state-data[@system='HVAC']) > 0");
     Object hasHvacData = hasHvacDataExpr.evaluate(doc, XPathConstants.BOOLEAN);
     assertTrue("Asserting there are state-data records for HVAC system.", (Boolean) hasHvacData);
     
     XPathExpression hasElectricDataExpr = xpath.compile(
       "count(//state-data[@system='ELECTRIC']) > 0");
     Object hasElectricData = hasElectricDataExpr.evaluate(doc, XPathConstants.BOOLEAN);
     assertTrue("Asserting there are state-data records for Electric system.", 
         (Boolean) hasElectricData);
     
     XPathExpression hasPhotovoltaicDataExpr = xpath.compile(
       "count(//state-data[@system='PHOTOVOLTAIC']) > 0");
     Object hasPhotovoltaicData = hasPhotovoltaicDataExpr.evaluate(doc, XPathConstants.BOOLEAN);
     assertTrue("Asserting there are state-data records for Photovoltaic system.", 
       (Boolean) hasPhotovoltaicData);
     
     // Retrieve the first state-data record, and its timestamp should be the base timestamp
     // value used to create historical state-data record. i.e., (base timestamp - 1 minute) is a 
     // record 1 minute in the past.
     XPathExpression firstStateDataNodeExpr = xpath.compile("//state-data[1]");
     Object firstStateDataNode = firstStateDataNodeExpr.evaluate(doc, XPathConstants.NODE);
     final long baseTimestamp = Long.valueOf(
         ((Element) firstStateDataNode).getAttribute("timestamp"));
     
     String expression = "count(//state-data[@timestamp=" + baseTimestamp + "]) > 0";
     XPathExpression baseTimestampCountExpr = xpath.compile(expression);
     Object recordWithBaseTimtestamp = baseTimestampCountExpr.evaluate(doc, XPathConstants.BOOLEAN);
     assertTrue("Checking there are records at base timestamp.", (Boolean) recordWithBaseTimtestamp);
     
     long baseTimestampOneHourAgo = baseTimestamp - (1000 * 60 * 60);
     expression = "count(//state-data[@timestamp=" + baseTimestampOneHourAgo + "]) > 0";
     XPathExpression baseTimestampOneHourAgoExpr = xpath.compile(expression);
     Object recordWithBaseTimtestampOneHourAgo = baseTimestampOneHourAgoExpr.evaluate(
         doc, XPathConstants.BOOLEAN);
     assertTrue("Checking there are records at base timestamp.", 
         (Boolean) recordWithBaseTimtestampOneHourAgo);
   }
 }
