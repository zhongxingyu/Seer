package edu.hawaii.ihale.backend.db;
 
 import java.util.HashMap;
 import java.util.Map;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import com.sleepycat.persist.model.Entity;
 import com.sleepycat.persist.model.PrimaryKey;
 import com.sleepycat.persist.model.Relationship;
 import com.sleepycat.persist.model.SecondaryKey;
 
 /**
  * Provides a simple record of information about a device/sensor/meter in a house system.
  * @author David Lin
  */
 @Entity
 public class IHaleEntry {
 
   /** The timestamp (UTC format) indicating when this state info was collected. */
   @PrimaryKey
   private long timestamp;
 
   /** The name of the System. Example: "Aquaponics". */
   @SecondaryKey(relate = Relationship.MANY_TO_ONE)
   private String systemName;
 
   /** The name of the Device. Example: "Arduino-23". */
   @SecondaryKey(relate = Relationship.MANY_TO_ONE)
   private String deviceName;
 
   /** The state-data element name. */
   private static final String stateDataElementName = "state-data";
 
   /** The state element name. */
   private static final String stateElementName = "state";
 
   /** The timestamp attribute name. */
   private static final String timestampAttributeName = "timestamp";
 
   /** The system name attribute name. */
   private static final String systemNameAttributeName = "system";
 
   /** The device name attribute name. */
   private static final String deviceNameAttributeName = "device";
 
   private Map<String, Long> longData = new HashMap<String, Long>();
   private Map<String, String> stringData = new HashMap<String, String>();
   private Map<String, Double> doubleData = new HashMap<String, Double>();
 
   /**
    * Provide the default constructor required by BerkeleyDB.
    */
   public IHaleEntry() {
     // Do nothing.
   }
 
   /**
    * Create an IHaleEntry instance given data values.
    * @param systemName The system name.
    * @param deviceName The device name.
    * @param timestamp The timestamp.
    */
   public IHaleEntry(String systemName, String deviceName, long timestamp) {
     this.systemName = systemName;
     this.deviceName = deviceName;
     this.timestamp = timestamp;
   }
 
   /**
    * Create a IHaleEntry instance given its representation in XML.
    * @param doc The XML document.
    */
   public IHaleEntry(Document doc) {
     this.systemName = getAttribute(doc, systemNameAttributeName);
     this.deviceName = getAttribute(doc, deviceNameAttributeName);
     this.timestamp = Long.valueOf(getAttribute(doc, timestampAttributeName));
     putKeyValuePair(doc);
   }
 
   /**
    * Helper method that returns the attribute of the root element of this XML document.
    * @param doc The XML document.
    * @param attributeName The attribute to return... system, device, or timestamp.
    * @return The attribute.
    */
   private String getAttribute(Document doc, String attributeName) {
     Element element = (Element) doc.getElementsByTagName(stateDataElementName).item(0);
     if (systemNameAttributeName.equals(attributeName)) {
       return element.getAttribute(systemNameAttributeName);
     }
     else if (deviceNameAttributeName.equals(attributeName)) {
       return element.getAttribute(deviceNameAttributeName);
     }
     else if (timestampAttributeName.equals(attributeName)) {
       return element.getAttribute(timestampAttributeName);
     }
     else {
       // Will never reach here.
       return null;
     }
   }
 
   /**
    * Places all key-value pairs into a map.
    * @param doc The XML document.
    */
   private void putKeyValuePair(Document doc) {
     NodeList keyValueList = doc.getElementsByTagName(stateElementName);
     for (int i = 0; i < keyValueList.getLength(); i++) {
       Element element = (Element) doc.getElementsByTagName(stateElementName).item(i);
       String key = element.getAttribute("key");
       String value = element.getAttribute("value");
       this.stringData.put(key, value);
     }
   }
 
   /**
    * Returns this contact as an XML Document instance. For example:
    * 
    * <pre>
    * {@code
    * <state-data system="SystemName" device="DeviceName" timestamp="2345789">
    *    <state key="key1" value="value1"/>
    *    <state key="key2" value="value2"/>
    *    <state key="key3" value="value3"/>
    * </state-data>
    * }
    * </pre>
    * 
    * @return This contact as XML.
    * @throws Exception If problems occur creating the XML.
    */
   public Document toXml() throws Exception {
     // Create the Document instance representing this XML.
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder builder = factory.newDocumentBuilder();
     Document doc = builder.newDocument();
     // Create and attach the root element <state-data>.
     Element rootElement = doc.createElement(stateDataElementName);
     // Set the attributes for system, device, and timestamp.
     rootElement.setAttribute(systemNameAttributeName, this.systemName);
     rootElement.setAttribute(deviceNameAttributeName, this.deviceName);
     rootElement.setAttribute(timestampAttributeName, String.valueOf(this.timestamp));
     doc.appendChild(rootElement);
     attachElements(doc, rootElement);
     return doc;
   }
 
   /**
    * Helper function to attach key-value pairs from all 3 maps.
    * @param doc The XML document.
    * @param rootElement The root element.
    */
   private void attachElements(Document doc, Element rootElement) {
     // Create and attach the sub elements <state> for Long values.
     for (Map.Entry<String, Long> entry : longData.entrySet()) {
       String key = entry.getKey();
       Long value = entry.getValue();
       Element childElement = doc.createElement(stateElementName);
       childElement.setAttribute(key, String.valueOf(value));
       rootElement.appendChild(childElement);
     }
     // Create and attach the sub elements <state> for String values.
     for (Map.Entry<String, String> entry : stringData.entrySet()) {
       String key = entry.getKey();
       String value = entry.getValue();
       Element childElement = doc.createElement(stateElementName);
       childElement.setAttribute(key, value);
       rootElement.appendChild(childElement);
     }
     // Create and attach the sub elements <state> for double values.
     for (Map.Entry<String, Double> entry : doubleData.entrySet()) {
       String key = entry.getKey();
       Double value = entry.getValue();
       Element childElement = doc.createElement(stateElementName);
       childElement.setAttribute(key, String.valueOf(value));
       rootElement.appendChild(childElement);
     }
   }
 
   /**
    * Returns the timestamp associated with this entry.
    * @return The timestamp.
    */
   public long getTimestamp() {
     return this.timestamp;
   }
   
   /**
    * Returns the system name associated with this entry.
    * @return The system name.
    */
   public String getSystemName() {
     return this.systemName;
   }
   
   /**
    * Returns the device name associated with this entry.
    * @return The device name.
    */
   public String getDeviceName() {
     return this.deviceName;
   }
 
   /**
    * Return this entry as a formatted string.
    * @return The entry as a string.
    */
   @Override
   public String toString() {
     return String.format("[System: %s Device: %s Time: %s State: %s %s %s]", this.systemName,
         this.deviceName, this.timestamp, this.stringData, this.longData, this.doubleData);
   }
 }
