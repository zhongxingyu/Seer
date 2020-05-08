 package edu.hawaii.ihale.backend;
 
 import java.io.IOException;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.w3c.dom.Document; 
 
 import edu.hawaii.ihale.api.ApiDictionary.IHaleRoom;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleState;
 import edu.hawaii.ihale.api.repository.impl.Repository;
 
 /**
  * Handles parsing XML documents and storing to the repository.
  * @author Tony Gaskell
  */
 public class XmlHandler {
   static Repository repository = new Repository();
   private static XPathFactory factory = XPathFactory.newInstance(); 
   private static XPath xpath = factory.newXPath();
   
   /**
    * Takes in an XML document formatted according to the System-H REST API specified here:
    * <url>http://code.google.com/p/solar-decathlon-teamhawaii/wiki/HouseSystemRestAPI</url>
    * as of March 15, 2011, and stores it to the IHaleRepository.
    * @param doc the Document to be parsed.
    * @throws IOException in case of parsing error.
    * @throws XPathExpressionException in the event of failed compilation.
    * @return true if successful, false otherwise.
    * @author Gregory Burgess, Tony Gaskell
    */
   public Boolean xml2StateEntry(Document doc)
     throws IOException, XPathExpressionException {
 
 //    This currently only works with getHistory() which may be all we need it to work with.
 
 //    In theory this method should be all-or-nothing.
 //    If something fails to load, they should ALL fail.  Source: ICS 321.
 
 //    XPathExpression stateDataPath = xpath.compile("//state-data");
     
     // Temporary variables.
     IHaleSystem systemEnum = null;
     String device;
     Long timestamp;
     IHaleState stateEnum = null;
     IHaleRoom roomEnum = null;
     String tempVal = null;
     Object finalVal = null;
     String stateData = "//state-data[";
     // Determine the number of system entries
     Double systems = (Double) xpath.evaluate("count(//state-data)",
         doc, XPathConstants.NUMBER);
 //    System.out.println(systems.intValue());
     
     // Pull the state-data attributes: system, device, timestamp
     for (int i = 1; i <= systems.intValue(); i ++) {
       systemEnum = IHaleSystem.valueOf((String) xpath.evaluate(stateData + i + "]/@system",
           doc, XPathConstants.STRING));
       device = (String) xpath.evaluate(stateData + i + "]/@device",
           doc, XPathConstants.STRING);
       timestamp = Long.parseLong((String) xpath.evaluate(stateData + i + "]/@timestamp",
           doc, XPathConstants.STRING));
 //      System.out.println("********************");
 //      System.out.println(systemEnum.toString() + " " + device + " " + timestamp);
       Double states = (Double) xpath.evaluate("count(//state-data[" + i + "]/state)",
           doc, XPathConstants.NUMBER);
       // Mapping LIGHTING arduino devices to IHaleRooms.
       if ("arduino-5".equals(device)) {
         roomEnum = IHaleRoom.LIVING;
       }
       else if ("arduino-6".equals(device)) {
         roomEnum = IHaleRoom.DINING;
       }
       else if ("arduino-7".equals(device)) {
         roomEnum = IHaleRoom.KITCHEN;
       }
       else if ("arduino-8".equals(device)) {
         roomEnum = IHaleRoom.BATHROOM;
       }
       else {
         // Default
         roomEnum = null;
       }
       
 //      System.out.println(states.intValue());
       // Pull state attributes: key, value
       for (int j = 1; j <= states.intValue(); j++) {
         stateEnum = IHaleState.valueOf((String) xpath.evaluate(stateData + i + "]" +
             "/state[" + j + "]/@key", doc, XPathConstants.STRING));
         tempVal = (String) xpath.evaluate(stateData + i + "]" +
             "/state[" + j + "]/@value", doc, XPathConstants.STRING);
         if (stateEnum.getType().equals(Double.class)) {
           finalVal = Double.parseDouble(tempVal);
         }
         else if (stateEnum.getType().equals(Integer.class)) {
           finalVal = Integer.parseInt(tempVal);
         }
         else if (stateEnum.getType().equals(Boolean.class)) {
           finalVal = Boolean.parseBoolean(tempVal);
         }
         else if (stateEnum.getType().equals(String.class)) {
           finalVal = tempVal;
         }
         else {
           System.err.println("ERROR: Could not parse value: " + tempVal + 
               " for IHaleState: " + stateEnum.toString());
         }
 /*        System.out.print(stateEnum.toString());
         if (roomEnum != null) {
           System.out.print(" " + roomEnum.toString());
         }
         System.out.println(" " + tempVal);
 */
         // Store to the repository
         repository.store(systemEnum, roomEnum, stateEnum, timestamp, finalVal);
       }
     }
 
     // This return value is supposed to be for testing purposes,
     // but I need to come up with an instance of when this thing would fail.
     // It's useless right now.
     return true;
   }
   
   /**
    * Takes in an XML representation formatted according to the System-H REST API specified here:
    * <url>http://code.google.com/p/solar-decathlon-teamhawaii/wiki/HouseSystemRestAPI</url>
    * as of March 15, 2011, and stores it to the IHaleRepository.
    * @param rep the representation to be parsed.
    * @return boolean true if successful, false otherwise. 
    * @throws IOException in case of parsing error.
    * @throws XPathExpressionException in the event of failed compilation.
    * @author Gregory Burgess, Tony Gaskell
    */
   public Boolean xml2StateEntry(Representation rep)
   throws IOException, XPathExpressionException {
 
 //  This should handle all poll() requests
 
 //  In theory this method should be all-or-nothing.
 //  If something fails to load, they should ALL fail.  Source: ICS 321.
 
     // Convert Representation to Document so we can operate on it with XPath.
     DomRepresentation dom = new DomRepresentation(rep);
     Document doc = dom.getDocument();
     return xml2StateEntry(doc);
   }
   
   /**
    * Takes in an XML document formatted according to the eGauge API specified here:
    * <url>http://www.egauge.net/docs/egauge-xml-api.pdf</url>
    * as of March 15, 2011, and stores it to the IHaleRepository. 
    * @param doc the Document to be parsed.
    * @return boolean true if successful, false otherwise. 
    * @throws IOException in case of parsing error.
    * @throws XPathExpressionException in the event of failed compilation.
    * @author Gregory Burgess, Tony Gaskell
    */
   public Boolean eGauge2StateEntry(Document doc)
     throws IOException, XPathExpressionException {
 
 //    This currently only works with getHistory() which may be all we need it to work with.
 
 //    In theory this method should be all-or-nothing.
 //    If something fails to load, they should ALL fail.  Source: ICS 321.
 
 //    XPathExpression stateDataPath = xpath.compile("//state-data");
     
     // Temporary variables.
     IHaleSystem systemEnum = null;
     Long timestamp;
     String title;
     IHaleState stateEnum = null;
     IHaleRoom roomEnum = null;
     String tempVal = null;
     Object finalVal = null;
     String meter = "//meter[";
     // Determine the number of system entries
     Double meters = (Double) xpath.evaluate("count(//meter)",
         doc, XPathConstants.NUMBER);
 //    System.out.println(meters.intValue());
     
     // Pull the state-data attributes: system, title, timestamp
     timestamp = Long.parseLong((String) xpath.evaluate("//timestamp/text()",
         doc, XPathConstants.STRING));
 
     // We don't care about the last meter, hence the '<' instead of '<='
     for (int i = 1; i < meters.intValue(); i++) {
 //      System.out.println("********************");
 //      System.out.println("Iteration: " + i);
       title = (String) xpath.evaluate(meter + i + "]/@title",
           doc, XPathConstants.STRING);
       if ("Grid".equals(title)) {
         systemEnum = IHaleSystem.ELECTRIC;
         stateEnum = IHaleState.POWER;
         // Grab data from the power (usage)
         tempVal = (String) xpath.evaluate(meter + i + "]" +
             "/power/text()", doc, XPathConstants.STRING);
       }
       else if ("Solar".equals(title)) {
         systemEnum = IHaleSystem.PHOTOVOLTAIC;
         stateEnum = IHaleState.ENERGY;
         // Grab data from energy (generation)
         tempVal = (String) xpath.evaluate(meter + i + "]" +
             "/energy/text()", doc, XPathConstants.STRING);
       }
       else {
         System.err.println("Unknown meter: " + title);
         systemEnum = null;
       }
 /*      if (systemEnum != null) {
         System.out.print(systemEnum.toString() + " ");
       }
 
       System.out.println(timestamp);
 */
       // Cast data according to the current IHaleState enum.
       if (stateEnum.getType().equals(Double.class)) {
         finalVal = Double.parseDouble(tempVal);
       }
       else if (stateEnum.getType().equals(Integer.class)) {
         finalVal = Integer.parseInt(tempVal);
       }
       else if (stateEnum.getType().equals(Boolean.class)) {
         finalVal = Boolean.parseBoolean(tempVal);
       }
       else if (stateEnum.getType().equals(String.class)) {
         finalVal = tempVal;
       }
       else {
         System.err.println("ERROR: Could not parse value: " + tempVal + 
             " for IHaleState: " + stateEnum.toString());
       }
 /*      System.out.print(stateEnum.toString());
       if (roomEnum != null) {
         System.out.print(" " + roomEnum.toString());
       }
       System.out.println(" " + tempVal);
 */
       // Store to the repository
       repository.store(systemEnum, roomEnum, stateEnum, timestamp, finalVal);
       
     }
 
     // This return value is supposed to be for testing purposes,
     // but I need to come up with an instance of when this thing would fail.
     // It's useless right now.
     return true;
   }
   
   /**
    * Takes in an XML representation formatted according to the System-H REST API specified here:
    * <url>http://code.google.com/p/solar-decathlon-teamhawaii/wiki/HouseSystemRestAPI</url>
    * as of March 15, 2011, and stores it to the IHaleRepository. 
    * @param rep the representation to be parsed.
    * @return boolean true if successful, false otherwise. 
    * @throws IOException in case of parsing error.
    * @throws XPathExpressionException in the event of failed compilation.
    * @author Gregory Burgess, Tony Gaskell
    */
  public Boolean eGauge2StateEntry(Representation rep)
   throws IOException, XPathExpressionException {
 
 //  This should handle all poll() requests
 
 //  In theory this method should be all-or-nothing.
 //  If something fails to load, they should ALL fail.  Source: ICS 321.
 
     // Convert Representation to Document so we can operate on it with XPath.
     DomRepresentation dom = new DomRepresentation(rep);
     Document doc = dom.getDocument();
     return eGauge2StateEntry(doc);
   }
 }
