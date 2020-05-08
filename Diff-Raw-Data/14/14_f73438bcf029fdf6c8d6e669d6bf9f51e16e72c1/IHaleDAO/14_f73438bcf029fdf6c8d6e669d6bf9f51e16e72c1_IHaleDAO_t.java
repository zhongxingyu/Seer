 package edu.hawaii.ihale.backend.restserver;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Map.Entry;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
 import org.restlet.data.Method;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.resource.ClientResource;
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
  * A class that resolves the persistency issues of the Java API by mirroring
  * IHaleDBRedirector and IHaleDB, converting SystemStateEntry objects to 
  * IHaleSystemEntry objects (has persistency traits). Then passing the 
  * IHaleSystemEntry objects to IHaleDB methods to store into the
  * database repository.
  * 
  * The createSystemToKeyMap method must be updated whenever the Data Dictionary API is modified.
  * 
  * The xmlToSystemStateEntry method must be updated to reflect any changes to the format the XML
  * information from system device returns.
  *
  * @author Leonardo Nguyen
  * @version Java 1.6.0_21
  */
 public class IHaleDAO implements SystemStateEntryDB {
   
   // Map from a system to its associated field values that would be returned by a system.
   // (i.e., Aquaponics system has fields Oxygen, Temp, pH). Refer to createSystemToKeyMap
   // method for its use.
   private static final Map<String, ArrayList<String>> systemToFieldMap = 
     new HashMap<String, ArrayList<String>>();
   
   // List of system state listeners.
   private List<SystemStateListener> listeners = new ArrayList<SystemStateListener>();
   
   // PMD complains about use of String literals.
   private String longString = "Long";
   private String stringString = "String";
   private String doubleString = "Double";
   
   // Contains the mapping of device ip addresses to port numbers as defined in the properties file.
   private static Map<String, String> uris = new HashMap<String, String>();
   
   /**
    * Default constructor.
    */
   public IHaleDAO() {
     // Default constructor.
   }
   
   static {
     createSystemToKeyMap();
     createDeviceToPortMap();
   }
 
   /**
    * Returns the SystemStateEntry instance associated with the system, device, and timestamp, 
    * or null if not found.
    * @param systemName The system name.
    * @param deviceName The device name.
    * @param timestamp The timestamp.
    * @return The associated SystemStateEntry, or null if not found.
    */
   public SystemStateEntry getEntry(String systemName, String deviceName, long timestamp) {
     
     IHaleSystemStateEntry entry = IHaleDB.getEntry(systemName, deviceName, timestamp);
     
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
         
     // A Map such that the key is the device field key (i.e., Oxygen, Temp) and the
     // value mapped to the key is the value type (i.e., Double, Long).
     Map<String, String> keyTypePairMap = getTypeList(entry.getSystemName());
 
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
       new IHaleSystemStateEntry(entry.getSystemName(), entry.getDeviceName(), entry.getTimestamp(),
           longData, stringData, doubleData);
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
    * Returns a list of SystemStateEntry instances consisting of all entries between the 
    * two timestamps. 
    * @param systemName The system name.
    * @param deviceName The device name.
    * @param startTime The start time.
    * @param endTime The end time. 
    * @return A (possibly empty) list of SystemStateEntries.
    * @throws SystemStateEntryDBException If startTime is greater than endTime. 
    */
   public List<SystemStateEntry> getEntries(String systemName, String deviceName, long startTime, 
       long endTime) throws SystemStateEntryDBException {
     
     List<IHaleSystemStateEntry> iHaleEntries = 
       IHaleDB.getEntries(systemName, deviceName, startTime, endTime);
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
    * Adds a listener to this repository whose entryAdded method will be invoked whenever an
    * entry is added to the database for the system name associated with this listener.
    * This method provides a way for the user interface (for example, Wicket) to update itself
    * whenever new data comes in to the repository. 
    * 
    * @param listener The listener whose entryAdded method will be called. 
    */
   public void addSystemStateListener(SystemStateListener listener) {
     this.listeners.add(listener);
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
             
       String host = "http://localhost:";
       String port = "";
       Map<String, String> deviceToPortMap = uris;
 
       Iterator<Entry<String, String>> iterator = deviceToPortMap.entrySet().iterator();
      while (iterator.hasNext()) {
         Map.Entry<String, String> mapEntry = iterator.next();
         String key = mapEntry.getKey().toString();
         if (key.contains(systemName) && key.contains(deviceName)) {
           port = mapEntry.getValue().toString();
         }
       }
       String url = host + port;
       System.out.println(url);
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
   }
   
   /**
    * Create a Map from a system to its associated field values that would be returned by a system
    * device. The key to a field and its value type is concatenated into a single string separated 
    * by ||.
    */
   public static void createSystemToKeyMap() {
     
     ArrayList<String> keyTypePairList = new ArrayList<String>();
     
     /** Aquaponics System **/
     keyTypePairList.add("pH||Double");
     keyTypePairList.add("Oxygen||Double");
     keyTypePairList.add("Temp||Long");
     IHaleDAO.systemToFieldMap.put("Aquaponics", keyTypePairList);
     
     /** HVAC System **/
     keyTypePairList = new ArrayList<String>();
     keyTypePairList.add("Temp||Long");
     IHaleDAO.systemToFieldMap.put("HVAC", keyTypePairList);
     
     /** Lighting System **/
     keyTypePairList = new ArrayList<String>();
     keyTypePairList.add("Level||Long");
     IHaleDAO.systemToFieldMap.put("Lighting", keyTypePairList);
     
     /** Photovoltaics System **/
     keyTypePairList = new ArrayList<String>();
     keyTypePairList.add("power||Long");
     keyTypePairList.add("energy||Long");
     IHaleDAO.systemToFieldMap.put("Photovoltaics", keyTypePairList);
     
     /** Electrical System **/
     keyTypePairList = new ArrayList<String>();
     keyTypePairList.add("power||Long");
     keyTypePairList.add("energy||Long");
     IHaleDAO.systemToFieldMap.put("Electrical", keyTypePairList);
   }
   
   /**
    * Returns a Map such that the key value is the device field key (i.e., Oxygen, Temp) and the
    * value mapped to the key is the value type (i.e., Double, Long).
    * 
    * @param systemName The system name.
    * @return The key type pair map.
    */
   public Map<String, String> getTypeList(String systemName) {
 
     ArrayList<String> keyTypePairList = IHaleDAO.systemToFieldMap.get(systemName);
 
     Map<String, String> keyTypePairMap = new HashMap<String, String>();
     //String[] temp = new String[2];
     for (int i = 0; i < keyTypePairList.size(); i++) {
       // Character | are special, need to be escaped with \\
       String[] temp = keyTypePairList.get(i).split("\\|\\|");
       keyTypePairMap.put(temp[0], temp[1]);
     }
     return keyTypePairMap;
   }
  
   /**
    * Parses XML information and creates a SystemStateEntry object.
    * 
    * @param doc The XML document from a system device response to a GET method for its
    *            current state. Format of XML document below.
    *            <pre>
    *            {@code
    *            <state-data system="aquaponics" device="arduino-1" timestamp="1297446335">
    *              <state key="temp" value="25"/>
    *              <state key="oxygen" value="4.5"/>
    *              <state key="pH" value="7"/>
    *            </state-data>
    *            }
    *            </pre>
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
     String deviceName = stateData.getAttribute(deviceNameAttributeName);
     long timestamp = Long.parseLong(stateData.getAttribute(timestampAttributeName));
    
     // Create a SystemStateEntry but it still requires its Maps to be filled.
     SystemStateEntry entry = new SystemStateEntry(systemName, deviceName, timestamp);
    
     // Retrieve the list of nodes representing the state elements of state-data element.
     NodeList stateList = stateData.getElementsByTagName(stateElementName);
 
     // Retrieve a helper Map to determine the key value types for use in corresponding
     // putLongDataValue, putStringDataValue, or putDoubleValue methods when a state element
     // has a certain key (i.e., pH should be a Double, Temp should be a Long).
     Map<String, String> keyTypePairMap = getTypeList(systemName);
    
     // For each state element, retrieve the value of the attribute key, and the value of attribute
     // value. Then dependent on the keyTypePairMap when passed the value of the key, should denote
     // its value type. Based on the value type, then the value of the value attribute should be
     // parsed corresponding to its proper type and placed into one of the Map fields of the
     // SystemStateEntry.
     for (int i = 0; i < stateList.getLength(); i++) {
       String key = ((Element) stateList.item(i)).getAttribute(keyAttributeName);
       String value = ((Element) stateList.item(i)).getAttribute(valueAttributeName);
       if (keyTypePairMap.get(key).equalsIgnoreCase(longString)) {
         entry.putLongValue(key, Long.parseLong(value));
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
    * Create a mapping mapping of device ip address to port number from a properties file.
    * (i.e., arduino-7.halepilihonua.hawaii.edu/lighting/state=7006 may be a line in the file)
    *
    */
   public static void createDeviceToPortMap() {
    
     // Path to where the Restlet server properties file.
     String currentDirectory = System.getProperty("user.dir");
     // Restlet server properties file name.
     String configurationFile = "configuration.properties";
     // Full path to the Restlet server properties file.
     String configFilePath = currentDirectory + "/" + configurationFile;
    
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
 }
