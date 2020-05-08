 package edu.hawaii.ihale.backend.rest;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.restlet.data.Method;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import edu.hawaii.ihale.api.SystemStateEntry;
 import edu.hawaii.ihale.api.SystemStateEntryDB;
 import edu.hawaii.ihale.api.SystemStateEntryDBException;
 import edu.hawaii.ihale.api.SystemStateListener;
 import edu.hawaii.ihale.backend.db.IHaleDB;
 import edu.hawaii.ihale.backend.db.IHaleSystemStateEntry;
 
 /**
  * A class that resolves the persistency issues of the Java API by mirroring IHaleDBRedirector and
  * IHaleDB, converting SystemStateEntry objects to IHaleSystemEntry objects (has persistency
  * traits). Then passing the IHaleSystemEntry objects to IHaleDB methods to store into the database
  * repository.
  * 
  * The createSystemToKeyMap method must be updated whenever the Data Dictionary API is modified. At
  * the moment all naming conventions (system name, device name, key name, etc.) begin with lower
  * case lettering to reflect XML information in the responses.
  * 
  * The xmlToSystemStateEntry method must be updated to reflect any changes to the format the XML
  * information from system device returns.
  * 
  * @author Leonardo Nguyen, David Lin, Nathan Dorman
  */
 public class IHaleDAO implements SystemStateEntryDB {
 
   // Map from a system to its associated field values that would be returned by a system.
   // (i.e., Aquaponics system has fields Oxygen, Temp, pH). Refer to createSystemToKeyMap
   // method for its use.
   private static final Map<String, ArrayList<String>> systemToFieldMap =
       new HashMap<String, ArrayList<String>>();
 
   // List of system state listeners.
   private static List<SystemStateListener> listeners = new ArrayList<SystemStateListener>();
 
   // PMD complains about use of String literals.
   private String longString = "Long";
   private String stringString = "String";
   private String doubleString = "Double";
 
   // Contains the mapping of device ip addresses to port numbers as defined in the properties file.
   private static Map<String, String> uris = new HashMap<String, String>();
   
   // A Map from device names that support PUTs (arduino-2, arduino-4, arduino-5, etc.) to a key 
   // from the device-urls.properties file. Used primarily for the doCommand() method.
   // i.e., A key for this Map: "arduino-2" mapped to "aquaponics-control"
   private static Map<String, String> deviceToSystemControl = new HashMap<String, String>();
 
   
   /**
    * Default constructor.
    */
   public IHaleDAO() {
     // Default constructor.
   }
 
   static {
     // Create a Map from a system to its associated field values that would be returned by a system
     // device. The key to a field and its value type is concatenated into a single string separated
     // by ||.
     // i.e., key = aquaponics , value = ArrayList<String> ->
     // with items pH||Double , oxygen||Double , temp||Long
     createSystemToKeyMap();
     // Retrieve the mapping of device ip addresses to port numbers as defined in the properties
     // file.
     uris = IHaleServer.getUris();
     // createDeviceToPortMap(); // Same operation as uris=IHaleServer.getUris();
     
     // Initialize the devices to their associated key in the device-urls.properties file,
     // so the PUT URLs can be retrieved.
     deviceToSystemControl.put("arduino-2", "aquaponics-control");
     deviceToSystemControl.put("arduino-4", "hvac-control");
     deviceToSystemControl.put("arduino-5", "lighting-living-control");
     deviceToSystemControl.put("arduino-6", "lighting-dining-control");
     deviceToSystemControl.put("arduino-7", "lighting-kitchen-control");
     deviceToSystemControl.put("arduino-8", "lighting-bathroom-control");
   }
 
   /**
    * Returns the SystemStateEntry instance associated with the system, device, and timestamp, or
    * null if not found.
    * @param systemName The system name.
    * @param deviceName The device name.
    * @param timestamp The timestamp.
    * @return The associated SystemStateEntry, or null if not found.
    */
   public SystemStateEntry getEntry(String systemName, String deviceName, long timestamp) {
 
     // Lower-case the system and device name to keep entries consistent between lower-case
     // format in XML documents and from front-end form submissions.
     String system = lowerCaseFirstLetter(systemName);
     String device = lowerCaseFirstLetter(deviceName);
     IHaleSystemStateEntry entry = IHaleDB.getEntry(system, device, timestamp);
 
     if (entry == null) {
       return null;
     }
     
     SystemStateEntry returnEntry =
         new SystemStateEntry(entry.getSystemName(), entry.getDeviceName(), entry.getTimestamp());
 
     // Retrieve the Long data types from the Entity class and populate the corresponding
     // longData Map of SystemStateEntry object.
     Map<String, Long> longData = entry.getLongData();
     Iterator<Entry<String, Long>> longIt = longData.entrySet().iterator();
     while (longIt.hasNext()) {
       Map.Entry<String, Long> mapEntry = longIt.next();
       String key = mapEntry.getKey().toString();
       returnEntry.putLongValue(key, longData.get(key));
     }
 
     // The same process as above but for SystemStateEntry's stringData Map.
     Map<String, String> stringData = entry.getStringData();
     Iterator<Entry<String, String>> stringIt = stringData.entrySet().iterator();
     while (stringIt.hasNext()) {
       Map.Entry<String, String> mapEntry = stringIt.next();
       String key = mapEntry.getKey().toString();
       returnEntry.putStringValue(key, stringData.get(key));
     }
 
     // The same process as above but for SystemStateEntry's doubleData Map.
     Map<String, Double> doubleData = entry.getDoubleData();
     Iterator<Entry<String, Double>> doubleIt = doubleData.entrySet().iterator();
     while (doubleIt.hasNext()) {
       Map.Entry<String, Double> mapEntry = doubleIt.next();
       String key = mapEntry.getKey().toString();
       returnEntry.putDoubleValue(key, doubleData.get(key));
     }
     return returnEntry;
   }
 
   /**
    * Store the passed SystemStateEntry in the database.
    * @param entry The entry instance to store.
    */
   public void putEntry(SystemStateEntry entry) {
         
     // Lower-case the system and device name to keep entries consistent between lower-case
     // format in XML documents and from front-end form submissions.
     String system = lowerCaseFirstLetter(entry.getSystemName());
     String device = lowerCaseFirstLetter(entry.getDeviceName());
 
     // A Map such that the key is the device field key (i.e., Oxygen, Temp) and the
     // value mapped to the key is the value type (i.e., Double, Long).
     Map<String, String> keyTypePairMap = getTypeList(system);
 
     // The three Maps longData, stringData, doubleData are representative of the Map fields
     // of SystemStateEntry objects. There is no accessor method to them so we must create
     // our own and mirror exactly those Maps and provide them to the Entity class to be stored
     // into the database repository. In this case the Entity class is IHaleSystemStateEntry.
     Map<String, Long> longData = new HashMap<String, Long>();
     Map<String, String> stringData = new HashMap<String, String>();
     Map<String, Double> doubleData = new HashMap<String, Double>();
 
     // Retrieve the keys and iterate through them checking for the corresponding value type
     // Double, String, or Long. Then retrieve from the SystemStateEntry object and put both
     // the key and value associated with the key into the appropriate Map.
     Iterator<Entry<String, String>> iterator = keyTypePairMap.entrySet().iterator();
     while (iterator.hasNext()) {
       Map.Entry<String, String> mapEntry = iterator.next();
       String key = mapEntry.getKey().toString();
       String value = keyTypePairMap.get(key);
       if (value.equalsIgnoreCase(longString)) {
         longData.put(key, entry.getLongValue(key));
       }
       else if (value.equalsIgnoreCase(stringString)) {
         stringData.put(key, entry.getStringValue(key));
       }
       else if (value.equalsIgnoreCase(doubleString)) {
         doubleData.put(key, entry.getDoubleValue(key));
       }
     }
 
     IHaleSystemStateEntry entryToStore =
         new IHaleSystemStateEntry(system, device, entry.getTimestamp(), longData, stringData,
             doubleData);
     IHaleDB.putEntry(entryToStore);
 
     // Entry has been stored and if any system listener is interested will be notified.
     for (SystemStateListener listener : listeners) {
       if (entry.getSystemName().equalsIgnoreCase(listener.getSystemName())) {
         listener.entryAdded(entry);
       }
     }
   }
 
   /**
    * Removes the entry with the specified system name, device name, and timestamp.
    * @param systemName The system name.
    * @param deviceName The device name.
    * @param timestamp The timestamp.
    */
   public void deleteEntry(String systemName, String deviceName, long timestamp) {
     IHaleDB.deleteEntry(systemName, deviceName, timestamp);
   }
 
   /**
    * Returns a list of SystemStateEntry instances consisting of all entries between the two
    * timestamps.
    * @param systemName The system name.
    * @param deviceName The device name.
    * @param startTime The start time.
    * @param endTime The end time.
    * @return A (possibly empty) list of SystemStateEntries.
    * @throws SystemStateEntryDBException If startTime is greater than endTime.
    */
   public List<SystemStateEntry> getEntries(String systemName, String deviceName, long startTime,
       long endTime) throws SystemStateEntryDBException {
 
     // Lower-case the system and device name to keep entries consistent between lower-case
     // format in XML documents and from front-end form submissions.
     String system = lowerCaseFirstLetter(systemName);
     String device = lowerCaseFirstLetter(deviceName);
 
     List<IHaleSystemStateEntry> iHaleEntries =
         IHaleDB.getEntries(system, device, startTime, endTime);
     List<SystemStateEntry> returnEntryList = new ArrayList<SystemStateEntry>();
     
     // For each entry retrieved from the data repository, we must transform from
     // IHaleSystemStateEntry to SystemStateEntry and store it in a List to be returned.
     for (int i = 0; i < iHaleEntries.size(); i++) {
 
       IHaleSystemStateEntry iHaleEntry = iHaleEntries.get(i);
       SystemStateEntry returnEntry =
           new SystemStateEntry(iHaleEntry.getSystemName(), iHaleEntry.getDeviceName(),
               iHaleEntry.getTimestamp());
 
       // Retrieve the Long data types from the Entity class and populate the corresponding
       // longData Map of SystemStateEntry object.
       Map<String, Long> longData = iHaleEntry.getLongData();
       Iterator<Entry<String, Long>> longIt = longData.entrySet().iterator();
       while (longIt.hasNext()) {
         Map.Entry<String, Long> mapEntry = longIt.next();
         String key = mapEntry.getKey().toString();
         returnEntry.putLongValue(key, longData.get(key));
       }
 
       // The same process as above but for SystemStateEntry's stringData Map.
       Map<String, String> stringData = iHaleEntry.getStringData();
       Iterator<Entry<String, String>> stringIt = stringData.entrySet().iterator();
       while (stringIt.hasNext()) {
         Map.Entry<String, String> mapEntry = stringIt.next();
         String key = mapEntry.getKey().toString();
         returnEntry.putStringValue(key, stringData.get(key));
       }
 
       // The same process as above but for SystemStateEntry's doubleData Map.
       Map<String, Double> doubleData = iHaleEntry.getDoubleData();
       Iterator<Entry<String, Double>> doubleIt = doubleData.entrySet().iterator();
       while (doubleIt.hasNext()) {
         Map.Entry<String, Double> mapEntry = doubleIt.next();
         String key = mapEntry.getKey().toString();
         returnEntry.putDoubleValue(key, doubleData.get(key));
       }
       returnEntryList.add(returnEntry);
     }
     return returnEntryList;
   }
 
   /**
    * Returns a list of all currently defined system names.
    * @return The list of system names.
    */
   public List<String> getSystemNames() {
     return IHaleDB.getSystemNames();
   }
 
   /**
    * Returns a list of all the device names associated with the passed SystemName.
    * @param systemName A string indicating a system name.
    * @return A list of device names for this system name.
    * @throws SystemStateEntryDBException if the system name is not known.
    */
   public List<String> getDeviceNames(String systemName) throws SystemStateEntryDBException {
     return IHaleDB.getDeviceNames(systemName);
   }
 
   /**
    * Adds a listener to this repository whose entryAdded method will be invoked whenever an entry is
    * added to the database for the system name associated with this listener. This method provides a
    * way for the user interface (for example, Wicket) to update itself whenever new data comes in to
    * the repository.
    * 
    * @param listener The listener whose entryAdded method will be called.
    */
   public void addSystemStateListener(SystemStateListener listener) {
     IHaleDAO.listeners.add(listener);
   }
 
   /**
    * Emits a command to be sent to the specified system with the optional arguments.
    * @param systemName The system name.
    * @param deviceName The device name.
    * @param command The command.
    * @param args Any additional arguments required by the command.
    */
   public void doCommand(String systemName, String deviceName, String command, List<String> args) {
 
     try {
       // Create the Document instance representing the XML that will be sent to the device to
       // enact the requested command.
       DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
       DocumentBuilder builder = factory.newDocumentBuilder();
       Document doc = builder.newDocument();
 
       // Create and attach the root element <command>.
       Element rootElement = doc.createElement("command");
       doc.appendChild(rootElement);
 
       // The name attribute of element <command>, representing the command name.
       Attr commandAttribute = doc.createAttribute("name");
       commandAttribute.setValue(command);
       rootElement.setAttributeNode(commandAttribute);
 
       // Create each arg element and append to <command> element as a child.
       for (int i = 0; i < args.size(); i++) {
         Element argElement = doc.createElement("arg");
         rootElement.appendChild(argElement);
 
         Attr valueAttribute = doc.createAttribute("value");
         valueAttribute.setValue(args.get(i));
         argElement.setAttributeNode(valueAttribute);
       }
 
       String url = "";
       
       // A String used for assist matching to a URL that contains the field to be affected,
       // i.e., /lighting/living/level or /aquaponics/temp whereas the command String is
       // setLevel or setTemp.
       String fieldToAffect = lowerCaseFirstLetter(command.substring(3));
 
       String deviceControlUrl = deviceToSystemControl.get(deviceName);
       url = uris.get(deviceControlUrl) + systemName + "/" + fieldToAffect;
 
       // For console debugging.
       System.out.println("Sending PUT Request on: " + url);
 
       ClientResource client = new ClientResource(Method.PUT, url);
       DomRepresentation representation = new DomRepresentation();
       representation.setDocument(doc);
       // Send the xml representation to the device.
       client.put(representation);
     }
     catch (ParserConfigurationException pce) {
       pce.printStackTrace();
     }
     catch (IOException ioe) {
       ioe.printStackTrace();
     }
     catch (ResourceException re) {
       System.out.println(re.getStatus());
     }
   }
 
   /**
    * Create a Map from a system to its associated field values that would be returned by a system
    * device. The key to a field and its value type is concatenated into a single string separated by
    * ||.
    * 
    * Note: Ensure that the field keys and system names begin with a lower-case letter. (i.e., temp)
    * since the XML information returned from system devices uses lower-case styling. The value type
    * of the key can have its first letter upper-case. (i.e., Double, Long).
    */
   public static void createSystemToKeyMap() {
 
     ArrayList<String> keyTypePairList = new ArrayList<String>();
 
     /** Aquaponics System **/
     keyTypePairList.add("ph||Double");
     keyTypePairList.add("oxygen||Double");
     keyTypePairList.add("temp||Long");
     IHaleDAO.systemToFieldMap.put("aquaponics", keyTypePairList);
 
     /** HVAC System **/
     keyTypePairList = new ArrayList<String>();
     keyTypePairList.add("temp||Long");
     IHaleDAO.systemToFieldMap.put("hvac", keyTypePairList);
 
     /** Lighting System **/
     keyTypePairList = new ArrayList<String>();
     keyTypePairList.add("level||Long");
     IHaleDAO.systemToFieldMap.put("lighting", keyTypePairList);
 
     /** Photovoltaics System **/
     keyTypePairList = new ArrayList<String>();
     keyTypePairList.add("power||Long");
     keyTypePairList.add("energy||Long");
     IHaleDAO.systemToFieldMap.put("photovoltaics", keyTypePairList);
 
     /** Electrical System **/
     keyTypePairList = new ArrayList<String>();
     keyTypePairList.add("power||Long");
     keyTypePairList.add("energy||Long");
     IHaleDAO.systemToFieldMap.put("electrical", keyTypePairList);
   }
 
   /**
    * Returns a Map such that the key value is the device field key (i.e., Oxygen, Temp) and the
    * value mapped to the key is the value type (i.e., Double, Long).
    * 
    * @param systemName The system name.
    * @return The key type pair map.
    */
   public Map<String, String> getTypeList(String systemName) {
 
     String systemNameKey = lowerCaseFirstLetter(systemName);
     ArrayList<String> keyTypePairList = IHaleDAO.systemToFieldMap.get(systemNameKey);
 
     Map<String, String> keyTypePairMap = new HashMap<String, String>();
 
     for (int i = 0; i < keyTypePairList.size(); i++) {
       // Character | are special, need to be escaped with \\
       String[] temp = keyTypePairList.get(i).split("\\|\\|");
       keyTypePairMap.put(temp[0], temp[1]);
     }
     return keyTypePairMap;
   }
 
   /**
    * Create a mapping mapping of device ip address to port number from a properties file. (i.e.,
    * arduino-7.halepilihonua.hawaii.edu/lighting/state=7006 may be a line in the file), this method
    * has significance with the doCommand() method.
    * 
    */
   public static void createDeviceToPortMap() {
 
     // Path to where the Restlet server properties file.
     String currentDirectory = System.getProperty("user.home");
     // Restlet server properties file name.
     String configurationFile = IHaleServer.getConfigurationFileName();
     // Full path to the Restlet server properties file.
     String configFilePath = currentDirectory + "/" + configurationFile;
 
     System.out.println(IHaleServer.getConfigurationFileName());
 
     try {
 
       FileInputStream is = new FileInputStream(configFilePath);
       Properties prop = new Properties();
       prop.load(is);
       String key = "";
       String value = "";
       for (Map.Entry<Object, Object> propItem : prop.entrySet()) {
         key = (String) propItem.getKey();
         value = (String) propItem.getValue();
         uris.put(key, value);
       }
       is.close();
     }
     catch (IOException e) {
       System.out.println("failed to read properties file");
       System.out.println(configFilePath);
     }
   }
 
   /**
    * Parses XML information and creates a SystemStateEntry object.
    * 
    * @param doc The XML document from a system device response to a GET method for its current
    * state. Format of XML document below.
    * 
    * <pre>
    *            {@code
    *            <state-data system="aquaponics" device="arduino-1" timestamp="1297446335">
    *              <state key="temp" value="25"/>
    *              <state key="oxygen" value="4.5"/>
    *              <state key="pH" value="7"/>
    *            </state-data>
    *            }
    * </pre>
    * @return Returns a SystemStateEntry object.
    */
   public SystemStateEntry xmlToSystemStateEntry(Document doc) {
 
     /** The system name attribute name. */
     String systemNameAttributeName = "system";
     /** The device name attribute name. */
     String deviceNameAttributeName = "device";
     /** The timestamp attribute name. */
     String timestampAttributeName = "timestamp";
     /** The state element name. */
     String stateElementName = "state";
     /** The key attribute name */
     String keyAttributeName = "key";
     /** The value attribute name */
     String valueAttributeName = "value";
 
     // Get the root element, in this case would be state-data element.
     Element stateData = doc.getDocumentElement();
     // Retrieve the attributes from state-data element, the system name, device name, and timestamp.
     String systemName = stateData.getAttribute(systemNameAttributeName);
     systemName = lowerCaseFirstLetter(systemName);
     String deviceName = stateData.getAttribute(deviceNameAttributeName);
     deviceName = lowerCaseFirstLetter(deviceName);
     long timestamp = Long.parseLong(stateData.getAttribute(timestampAttributeName));
 
     // Create a SystemStateEntry but it still requires its Maps to be filled.
     SystemStateEntry entry = new SystemStateEntry(systemName, deviceName, timestamp);
 
     // Retrieve the list of nodes representing the state elements of state-data element.
     NodeList stateList = stateData.getElementsByTagName(stateElementName);
 
     // Retrieve a helper Map to determine the key value types for use in corresponding
     // putLongDataValue, putStringDataValue, or putDoubleValue methods when a state element
     // has a certain key (i.e., pH should be a Double, temp should be a Long).
 
     Map<String, String> keyTypePairMap = getTypeList(systemName);
 
     // For each state element, retrieve the value of the attribute key, and the value of attribute
     // value. Then dependent on the keyTypePairMap when passed the value of the key, should denote
     // its value type. Based on the value type, then the value of the value attribute should be
     // parsed corresponding to its proper type and placed into one of the Map fields of the
     // SystemStateEntry.
     for (int i = 0; i < stateList.getLength(); i++) {
 
       String key = ((Element) stateList.item(i)).getAttribute(keyAttributeName);
       
       // Special case for pH, lower-case it all.
      if (key.equals("pH")) {
         key = "ph";
       }
       
       String value = ((Element) stateList.item(i)).getAttribute(valueAttributeName);
 
       if (keyTypePairMap.get(key).equalsIgnoreCase(longString)) {
         // Sanitize input values if they use a different type to Long.
         entry.putLongValue(key, (Double.valueOf(value)).longValue());
       }
       else if (keyTypePairMap.get(key).equalsIgnoreCase(stringString)) {
         entry.putStringValue(key, value);
       }
       else if (keyTypePairMap.get(key).equalsIgnoreCase(doubleString)) {
         entry.putDoubleValue(key, Double.parseDouble(value));
       }
     }
     return entry;
   }
 
   /**
    * Parses XML information specific to the Egauge XML API defined format and creates a
    * SystemStateEntry object. Currently only supports use with Photovoltaics and Electrical system
    * devices.
    * 
    * @param doc The XML document from a system device response to a GET method for its current
    * state. Format of XML document below.
    * 
    * <pre>
    *            {@code
    *            <?xml version="1.0" encoding="UTF-8" ?>
    *            <measurements>
    *              <timestamp>1284607004</timestamp>
    *              <cpower src="Grg&amp;Bth (PHEV)" i="11" u="1">-988.9</cpower>
    *              <cpower src="Solar" i="5" u="8">-1.9</cpower>
    *              <cpower src="Grid" i="3" u="1">1621.5</cpower>
    *              <meter title="Grid">
    *                <energy>1443.5</energy>
    *                <energyWs>5196771697</energyWs>
    *                <power>2226.2</power>
    *              </meter>
    *              <meter title="Solar">
    *                <energy>5918.9</energy>
    *                <energyWs>21308130148</energyWs>
    *                <power>-3.5</power>
    *              </meter>
    *              <meter title="Grg&amp;Bth (PHEV)">
    *                <energy>4889.2</energy>
    *                <energyWs>17601054087</energyWs>
    *                <power>-988.9</power>
    *              </meter>
    *              <frequency>59.98</frequency>
    *              <voltage>119.0</voltage>
    *              <voltage>118.3</voltage>
    *              <current>5.495</current>
    *              <current>14.152</current>
    *              <current>0.223</current>
    *              <current>0.136</current>
    *              </measurements>
    *            }
    * </pre>
    * @param systemName The name of the system associated with this device reading.
    * @param deviceName The device that provided this XML reading.
    * @return Returns a SystemStateEntry object.
    */
   public SystemStateEntry xmlEgaugeToSystemStateEntry(Document doc, String systemName,
       String deviceName) {
 
     /** The timestamp attribute name. */
     String timestampAttributeName = "timestamp";
     /** The state element name. */
     String meterElementName = "meter";
     /** The title attribute name */
     String titleAttributeName = "title";
     /** The energy element name */
     String energyElementName = "energy";
     /** The power element name */
     String powerElementName = "power";
 
     // Get the root element, in this case would be measurements element.
     Element measurementsData = doc.getDocumentElement();
     // Retrieve the list of nodes (should just be 1) representing the timestamp element of the
     // measurements element.
     NodeList timestampList = measurementsData.getElementsByTagName(timestampAttributeName);
 
     // Retrieve the content between the timestamp element tags.
     long timestamp = Long.parseLong(((Element) timestampList.item(0)).getTextContent());
 
     // Create a SystemStateEntry but it still requires its Maps to be filled.
     SystemStateEntry entry = new SystemStateEntry(systemName, deviceName, timestamp);
 
     // Retrieve the list of nodes representing the meter element of the measurements element.
     NodeList meterList = measurementsData.getElementsByTagName(meterElementName);
 
     for (int i = 0; i < meterList.getLength(); i++) {
 
       Element meterElement = ((Element) meterList.item(i));
       String title = meterElement.getAttribute(titleAttributeName);
 
       // If XML parsing is for Photovoltaics system device readings, we are only interested in the
       // energy and power readings associated with the Solar meter.
       if (systemName.equalsIgnoreCase("photovoltaics") && title.equalsIgnoreCase("Solar")) {
         // Retrieve the list of energy and power elements; should only be 1 each.
         NodeList energyList = meterElement.getElementsByTagName(energyElementName);
         NodeList powerList = meterElement.getElementsByTagName(powerElementName);
 
         // Determine these fields were long value types by referring to the Data Dictionary API.
         Long energy = (Double.valueOf(energyList.item(0).getTextContent())).longValue();
         Long power = (Double.valueOf(powerList.item(0).getTextContent())).longValue();
 
         entry.putLongValue("energy", energy);
         entry.putLongValue("power", power);
 
         return entry;
       }
       // For Electrical system device readings, only energy and power associated with the
       // Grid meter.
       else if (systemName.equalsIgnoreCase("electrical") && title.equalsIgnoreCase("Grid")) {
         NodeList energyList = meterElement.getElementsByTagName(energyElementName);
         NodeList powerList = meterElement.getElementsByTagName(powerElementName);
 
         Long energy = (Double.valueOf(energyList.item(0).getTextContent())).longValue();
         Long power = (Double.valueOf(powerList.item(0).getTextContent())).longValue();
 
         entry.putLongValue("energy", energy);
         entry.putLongValue("power", power);
 
         return entry;
       }
     }
     return null;
   }
 
   /**
    * Lower-cases the first letter of a word, used to keep incoming XML information consistent with
    * key naming conventions.
    * 
    * @param word The word to modify.
    * @return A String with its first character lower-cased.
    */
   public String lowerCaseFirstLetter(String word) {
     String s1 = word.substring(0, 1);
     String s2 = word.substring(1);
     return s1.toLowerCase(Locale.US) + s2;
   }
 
   /**
    * Upper-cases the first letter of a word, used to retrieve values from a Map field of a
    * SystemStateEntry object.
    * 
    * @param word The word to modify.
    * @return A string with its first character upper-cased.
    */
   public String upperCaseFirstLetter(String word) {
     String s1 = word.substring(0, 1);
     String s2 = word.substring(1);
     return s1.toUpperCase(Locale.US) + s2;
   }
 }
