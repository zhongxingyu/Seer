 package edu.hawaii.ihale.backend;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 import org.restlet.resource.ClientResource;
 import org.w3c.dom.Document;
 import edu.hawaii.ihale.api.ApiDictionary;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleCommandType;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleRoom;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleState;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 import edu.hawaii.ihale.api.ApiDictionary.SystemStatusMessageType;
 import edu.hawaii.ihale.api.command.IHaleCommand;
 import edu.hawaii.ihale.api.repository.SystemStatusMessage;
 import edu.hawaii.ihale.api.repository.impl.Repository;
 import edu.hawaii.ihale.backend.command.PutCommand;
 import edu.hawaii.ihale.backend.exception.InvalidTypeException;
 import edu.hawaii.ihale.backend.restserver.RestServer;
 
 /**
  * Provides a sample illustration of IHale backend functionality as it relates to the iHale API
  * implementation.
  * 
  * An IHale Backend has to accomplish two basic things:
  * 
  * (1) Query house systems via HTTP for their state, then store that info in the repository. The
  * storage part is illustrated by the obtainStateFromHouseSystem method.
  * 
  * (2) Implement the IHaleCommandInterface so that the Frontend can send commands to the house
  * systems by way of this Backend. This is illustrated by the doCommand method.
  * 
  * In addition, the doCommand illustrates how to create and store a system status message which the
  * Frontend could display in the interface by attaching a listener.
  * 
  * @author Philip Johnson
  * @author Bret K. Ikehara
  * @author Tony Gaskell
  * @author Gregory Burgess
  * @author Michael Cera
  */
 public class IHaleBackend implements IHaleCommand {
 
   /**
    * Backend logger object.
    */
   private static Logger log;
 
   /**
    * String reference to device configuration file.
    */
   public static String deviceConfigRef;
 
   /**
    * String reference to the historical data.
    */
   public static String initialDataPath;
 
   /**
    * Defines all the URIs read by the URL property file.
    */
   private static Map<String, String> uriMap;
 
   /**
    * Object that polls data from HSIM.
    */
   private static Dispatcher dispatch;
 
   /**
    * Thread for polling.
    */
   private static Thread pollingThread;
 
   /**
    * The repository that can store all the data for the iHale system.
    */
   private Repository repository = new Repository();
 
   /**
    * The restlet server to handle external device requests.
    */
   private static RestServer restServer = new RestServer();
 
   /** The singleton instance of the Backend. */
   private static IHaleBackend instance = new IHaleBackend();
 
   /**
    * Initializes the singleton.
    */
   private static final void init() {
     // Interval in milliseconds between polling the system devices.
     long interval = 5000;
     log = Logger.getLogger(IHaleBackend.class.toString());
     Logger.getLogger(IHaleBackend.class.toString()).setLevel(Level.OFF);
     // Parse the URI configuration file.
     String folder = ".ihale";
     String configurationFile = "device-urls.properties";
     deviceConfigRef = System.getProperty("user.home") + "/" + folder + "/" + configurationFile;
 
     String initialDataFile = "initial-data.xml";
     initialDataPath = System.getProperty("user.home") + "/" + folder + "/" + initialDataFile;
 
     // instantiate the uris map.
     try {
       uriMap = parseURIPropertyFile(deviceConfigRef);
     }
     catch (IOException e) {
       log.warning("URI configuration FileInputStream failed to properly close.");
     }
 
     // Parse the historical data file to populate the repository.
     try {
       getHistory();
     }
     catch (XPathExpressionException e1) {
       e1.printStackTrace();
     }
     catch (ParserConfigurationException e1) {
       e1.printStackTrace();
     }
     catch (IOException e1) {
       e1.printStackTrace();
     }
 
     // Initialize Dispatcher
     dispatch = null;
     log.info("Initiatiating Dispatcher.");
 
     dispatch = new Dispatcher(uriMap, interval);
     pollingThread = new Thread(dispatch);
     pollingThread.start();
 
     log.info("Running dispatcher at an interval of " + interval + " milliseconds.");
     log.info("Initiating repository.");
 
     // Start restlet server
     try {
       RestServer.runServer(8111);
     }
     catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Reads in initial-data.xml, and stores entries into the repository.
    * 
    * @throws ParserConfigurationException - Probelm parsing XML file.
    * @throws XPathExpressionException - Problem evaluating XPath expression.
    * @throws IOException - Problem reading in file.
    */
   public static void getHistory() throws ParserConfigurationException, XPathExpressionException,
       IOException {
 
     XmlHandler parser = new XmlHandler();
     File file = null;
     Document doc = null;
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     DocumentBuilder db = dbf.newDocumentBuilder();
 
     try {
       file = new File(initialDataPath);
       doc = db.parse(file);
     }
     catch (Exception e) {
       System.err.println("Failed to convert to doc.");
     }
 
     parser.parseIhaleXml2StateEntry(doc);
   }
 
   /**
    * Parses URI properties file. Taken from team Hoike's backend files. Adapted from original Team
    * Hoike code.
    * 
    * @param file String
    * @return Map<String, String>
    * @throws IOException Thrown when unable to close the FileInputStream.
    */
   public static Map<String, String> parseURIPropertyFile(String file) throws IOException {
     Map<String, String> uris = new HashMap<String, String>();
 
     if (file == null) {
       throw new FileNotFoundException("File cannot be null.");
     }
 
     File f = new File(file);
     FileInputStream is = null;
     Properties prop = new Properties();
     Logger log = Logger.getLogger(IHaleBackend.class.toString());
 
     log.info("Reading file at: " + file);
 
     // Check the file.
     if (!f.isFile()) {
       throw new IOException("File is invalid.");
     }
 
     if (!f.canRead()) {
       throw new IOException("File read permissions is denied.");
     }
 
     // Read the file content
     try {
       is = new FileInputStream(f);
 
       prop.load(is);
 
       for (Object key : prop.keySet()) {
         uris.put(key.toString(), prop.getProperty((String) key));
       }
     }
     catch (IOException e) {
       uris = null;
       log.warning("Failed to read properties file.");
     }
     finally {
       if (is != null) {
         is.close();
       }
     }
 
     return uris;
   }
 
   /**
    * Returns the singleton instance.
    * 
    * @return The singleton instance.
    */
   public static IHaleBackend getInstance() {
     return instance;
   }
 
   /**
    * Gets this backend's URI mapping for each iHale system.
    * 
    * @return Map<String, String>
    */
   public static Map<String, String> getURImap() {
     return uriMap;
   }
 
   /**
    * Gets this REST server reference.
    * 
    * @return RestServer
    */
   public static RestServer getServer() {
     return restServer;
   }
 
   /**
    * Private constructor.
    */
   private IHaleBackend() {
     init();
   }
 
   /**
    * Implements a request from the front-end to send off a command to a house system. The backend
    * must store this command request in the repository, indicate that it occurred as a status
    * message, and finally carry out the command by sending the HTTP request to the associated
    * system.
    * 
    * @param system The house system.
    * @param room The room in the house if the system is LIGHTING, or null otherwise.
    * @param command The command requested for the system.
    * @param arg The arguments for the command.
    */
   @Override
   public void doCommand(IHaleSystem system, IHaleRoom room, IHaleCommandType command, Object arg) {
 
     ClientResource client = null;
     PutCommand cmd = null;
 
     if (arg == null) {
       throw new RuntimeException("Argument cannot be null.");
     }
 
     // All command invocations should be saved in the repository. Here's how you do it.
     Long timestamp = (new Date()).getTime();
     IHaleState state = ApiDictionary.iHaleCommandType2State(command);
     repository.store(system, room, state, timestamp, arg);
 
     // We probably also want every command invocation to be displayed as a status message.
     String msg = String.format("Sending system %s command %s with arg %s", system, command, arg);
     SystemStatusMessage message =
         new SystemStatusMessage(timestamp, system, SystemStatusMessageType.INFO, msg);
     repository.store(message);
 
     log.info(system.toString() + " command: " + command.toString() + " arg: " + arg.toString());
 
     try {
       // Of course, you also have to actually emit the HTTP request to send the command to the
       // relevant system. It might look something like the following.
       // Note the PV and ELECTRIC systems do not current support commands.
       switch (system) {
       case AQUAPONICS:
         cmd = handleAquaponicsCommand(command, arg);
         break;
       case HVAC:
         cmd = handleHvacCommand(command, arg);
         break;
       case LIGHTING:
         cmd = handleLightingCommand(room, command, arg);
         break;
       default:
         throw new RuntimeException("Unsupported IHale System Type encountered: " + system);
       }
 
       log.info("Sending " + system.toString() + " command: " + cmd + " to " + cmd.getURI());
 
       // Send the xml representation to the device.
       client = new ClientResource(cmd.getURI());
       client.getLogger().setLevel(Level.OFF);
       client.put(cmd.getDomRepresentation());
     }
     catch (IOException e) {
       throw new RuntimeException("Failed to create Dom Representation.", e);
     }
     catch (Exception e) {
       throw new RuntimeException("Failed to create command XML.", e);
     }
     finally {
       if (client != null) {
         client.release();
       }
     }
   }
 
   /**
    * Emit an HTTP command to the lighting system.
    * 
    * @param room The room to be controlled.
    * @param command The command type: SET_LIGHTING_ENABLED, SET_LIGHTING_LEVEL, SET_LIGHTING_COLOR.
    * @param arg A boolean if the command is enabled, an integer if the command is level, and a
    * string if the command is color.
    * @return PutCommand
    * @throws RuntimeException Thrown creation of XML document fails.
    * @throws ParserConfigurationException Thrown when command XML document fails to initiate.
    * @throws InvalidTypeException Thrown when argument is invalid.
    */
   protected PutCommand handleLightingCommand(IHaleRoom room, IHaleCommandType command, Object arg)
       throws ParserConfigurationException, InvalidTypeException {
 
     // Generates the XML for the command.
     PutCommand cmd = new PutCommand(command);
     StringBuilder uri = new StringBuilder(50);
 
     // Check argument type for command.
     if (ApiDictionary.iHaleCommandType2State(command).isType(arg.toString())) {
       cmd.addElement("arg", arg);
     }
     else {
       cmd = null;
       throw new InvalidTypeException();
     }
 
     if (room == null) {
       throw new NullPointerException("IHaleRoom is invalid.");
     }
     else {
       cmd.addElement("room", room);
     }
 
     // Sets the room uri
     switch (room) {
     case LIVING:
       uri.append(uriMap.get("lighting-living-control"));
       break;
     case DINING:
       uri.append(uriMap.get("lighting-dining-control"));
       break;
     case KITCHEN:
       uri.append(uriMap.get("lighting-kitchen-control"));
       break;
     case BATHROOM:
       uri.append(uriMap.get("lighting-bathroom-control"));
       break;
     default:
       throw new NullPointerException("IHaleRoom is invalid.");
     }
 
     uri.append("lighting/");
 
     switch (command) {
     case SET_LIGHTING_ENABLED:
       uri.append("enabled");
       break;
     case SET_LIGHTING_LEVEL:
       uri.append("level");
       break;
     case SET_LIGHTING_COLOR:
       uri.append("color");
       break;
     default:
       throw new NullPointerException("IHaleCommandType is invalid.");
     }
 
     cmd.setURI(uri.toString());
 
     return cmd;
   }
 
   /**
    * Creates the HTTP command for the HVAC system.
    * 
    * @param command Currently the only supported command is SET_TEMPERATURE.
    * @param arg An integer representing the new number.
    * @return PutCommand
    * @throws RuntimeException Thrown creation of XML document fails.
    * @throws ParserConfigurationException Thrown when command XML document fails to initiate.
    * @throws InvalidTypeException Thrown when argument is invalid.
    */
   protected PutCommand handleHvacCommand(IHaleCommandType command, Object arg)
       throws RuntimeException, ParserConfigurationException, InvalidTypeException {
 
     PutCommand cmd = new PutCommand(command);
     StringBuffer uri = new StringBuffer(50);
 
     // Check argument type for command.
     if (ApiDictionary.iHaleCommandType2State(command).isType(arg.toString())) {
       cmd.addElement("arg", arg);
     }
     else {
       cmd = null;
       throw new InvalidTypeException();
     }
 
     uri.append(uriMap.get("hvac-control"));
     uri.append("hvac/");
 
     // append command
     if (command.equals(IHaleCommandType.SET_TEMPERATURE)) {
       uri.append("temperature");
     }
     else {
       throw new NullPointerException("IHaleCommandType is invalid.");
     }
 
     cmd.setURI(uri.toString());
 
     return cmd;
   }
 
   /**
    * Emit an HTTP command to the Aquaponics system.
    * 
    * @param command The command type: FEED_FISH, HARVEST_FISH, SET_PH, SET_WATER_LEVEL,
    * SET_TEMPERATURE, SET_NUTRIENTS.
    * @param arg An integer for feed fish, harvest fish, water level, and temperature, a double
    * otherwise.
    * @return PutCommand
    * @throws RuntimeException Thrown creation of XML document fails.
    * @throws ParserConfigurationException Thrown when command XML document fails to initiate.
    * @throws InvalidTypeException Thrown when argument is invalid.
    */
   protected PutCommand handleAquaponicsCommand(IHaleCommandType command, Object arg)
       throws ParserConfigurationException, InvalidTypeException {
 
     PutCommand cmd = new PutCommand(command);
     StringBuilder uri = new StringBuilder(50);
 
     // Checks whether value is valid.
     if (ApiDictionary.iHaleCommandType2State(command).isType(arg.toString())) {
       cmd.addElement("arg", arg);
     }
     else {
       cmd = null;
       throw new InvalidTypeException();
     }
 
     uri.append(uriMap.get("aquaponics-control"));
     uri.append("aquaponics/");
 
     // append command
     switch (command) {
     case FEED_FISH:
       uri.append("fish/feed");
       break;
     case SET_TEMPERATURE:
       uri.append("temperature");
       break;
     case HARVEST_FISH:
       uri.append("fish/harvest");
       break;
     case SET_NUTRIENTS:
       uri.append("nutrients");
       break;
     case SET_PH:
       uri.append("ph");
       break;
     case SET_WATER_LEVEL:
       uri.append("water/level");
       break;
     default:
       throw new NullPointerException("IHaleCommandType is invalid.");
     }
 
     cmd.setURI(uri.toString());
 
     return cmd;
   }
   
/**
 * Initializes the project.
 * 
 * @param args Ignored.
 */
   public static void main(String[] args) {
    IHaleBackend.getInstance();
     System.out.println("Running...");
   }
 }
