 package edu.hawaii.ihale.backend;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 import org.restlet.resource.ClientResource;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 import edu.hawaii.ihale.api.ApiDictionary;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleCommandType;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleRoom;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleState;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 import edu.hawaii.ihale.api.ApiDictionary.SystemStatusMessageType;
 import edu.hawaii.ihale.api.command.IHaleCommand;
 import edu.hawaii.ihale.api.repository.SystemStatusMessage;
 import edu.hawaii.ihale.api.repository.impl.Repository;
 import edu.hawaii.ihale.backend.xml.PutCommand;
 import edu.hawaii.ihale.backend.xml.ValidTypeException;
 
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
  * @author Backend Team
  */
 public class IHaleBackend implements IHaleCommand {
 
   /**
    * Object that polls data from HSIM.
    */
   public Dispatcher dispatch;
 
   /**
    * Defines all the URIs read by the URL property file.
    */
   public Map<String, String> uris;
 
   /**
    * Defines all the command map objects.
    */
   public Map<String, String> commandMap;
 
   /**
    * Defines all the URIs for the aquaponics system for sending commands to the system's Arduino
    * device.
    */
   public Map<String, String> aquaMap;
 
   /**
    * Defines all the URIs for the lighting system for sending commands to the system's Arduino
    * device.
    */
   public Map<String, String> lightMap;
 
   /**
    * Full path to the system device properties file.
    */
   private static String configFilePath;
 
   /**
    * Full path to the initial data file.
    */
   private static String initialDataPath;
 
   /**
    * Defines all the static variables.
    */
   static {
     String folder = ".ihale";
     String configurationFile = "device-urls.properties";
     configFilePath = System.getProperty("user.home") + "/" + folder + "/" + configurationFile;
     String initialDataFile = "initial-data.xml";
     initialDataPath = System.getProperty("user.home") + "/" + folder + "/" + initialDataFile;
   }
 
   // A logger.
   private Logger log;
 
   // The repository that can store all the data for the iHale system.
   private Repository repository;
 
   /**
    * Default Constructor which initiates all the backend resources.
    * 
    * @throws IOException Thrown when URI configuration FileInputStream fails.
    * @throws SAXException Thrown when XML parsing fails.
    * @throws ParserConfigurationException Configuration error.
    * @throws XPathExpressionException Error in XPath expression.
    */
   public IHaleBackend() throws XPathExpressionException, ParserConfigurationException,
       SAXException, IOException {
 
     // Interval in milliseconds between polling the system devices.
     long interval = 5000;
 
     this.log = Logger.getLogger(this.getClass().toString());
 
     this.log.info("Initiating repository.");
     this.repository = new Repository();
 
     // instantiate the uris map.
     uris = new HashMap<String, String>();
     try {
       parseURIPropertyFile();
     }
     catch (IOException e) {
       this.log.warning("URI configuration FileInputStream failed to properly close.");
     }
 
     // read in history
     getHistory();
 
     this.log.info("Initiate Dispatcher.");
 
     // make a dispatcher
     dispatch = new Dispatcher(uris, interval);
 
     // grab all data before it starts
     commandMap = dispatch.getCommandMap();
     lightMap = dispatch.getLightMap();
     aquaMap = dispatch.getAquaMap();
 
     this.log.info("Running dispatcher at an interval of " + interval + " milliseconds.");
 
     // pop a new thread to run forever
     Thread poll = new Thread(dispatch);
     poll.start();
   }
 
   /**
    * Parses URI properties file. Taken from team Hoike's backend files. Adapted from original Team
    * Hoike code.
    * 
    * @throws IOException Thrown when unable to close the FileInputStream.
    */
   private void parseURIPropertyFile() throws IOException {
 
     FileInputStream is = null;
     Properties prop = new Properties();
 
     this.log.info("Reading file at: " + configFilePath);
 
     try {
       is = new FileInputStream(configFilePath);
 
       prop.load(is);
 
       for (Object key : prop.keySet()) {
         uris.put(key.toString(), prop.getProperty((String) key));
       }
     }
     catch (IOException e) {
       this.log.warning("Failed to read properties file.");
     }
     finally {
       if (is != null) {
         is.close();
       }
     }
   }
 
   /**
    * Reads in initial-data.xml, and stores entries into the repository.
    * 
    * @throws ParserConfigurationException Thrown if error exists in parser configuration.
    * @throws SAXException Thrown when XML parsing fails.
    * @throws IOException Thrown when unable to close the FileInputStream.
    * @throws XPathExpressionException Thrown if error exists in XPath expression.
    */
  public void getHistory() throws ParserConfigurationException, SAXException, IOException,
       XPathExpressionException {
 
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
 
     parser.xml2StateEntry(doc);
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
 
     String url = null;
     ClientResource client = null;
     PutCommand cmd = null;
 
     // All command invocations should be saved in the repository. Here's how you do it.
     Long timestamp = (new Date()).getTime();
     IHaleState state = ApiDictionary.iHaleCommandType2State(command);
     repository.store(system, room, state, timestamp, arg);
 
     // We probably also want every command invocation to be displayed as a status message.
     String msg = String.format("Sending system %s command %s with arg %s", system, command, arg);
     SystemStatusMessage message =
         new SystemStatusMessage(timestamp, system, SystemStatusMessageType.INFO, msg);
     repository.store(message);
 
     this.log
         .info(system.toString() + " command: " + command.toString() + " arg: " + arg.toString());
 
     try {
       // Of course, you also have to actually emit the HTTP request to send the command to the
       // relevant system. It might look something like the following.
       // Note the PV and ELECTRIC systems do not current support commands.
       switch (system) {
       case AQUAPONICS:
         cmd = handleAquaponicsCommand(command, arg);
         url = uris.get(system) + aquaMap.get(command.toString());
         break;
       case HVAC:
         cmd = handleHvacCommand(command, arg);
         url = uris.get(system) + "hvac/temp";
         break;
       case LIGHTING:
         cmd = handleLightingCommand(room, command, arg);
         url = uris.get(system) + lightMap.get(command.toString());
         break;
       default:
         throw new RuntimeException("Unsupported IHale System Type encountered: " + system);
       }
 
       this.log.info("Sending " + system.toString() + " command: " + cmd + " to " + url);
 
       // Send the xml representation to the device.
       client = new ClientResource(url);
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
    * @throws ValidTypeException Thrown when argument is invalid.
    */
   protected PutCommand handleLightingCommand(IHaleRoom room, IHaleCommandType command, Object arg)
       throws ParserConfigurationException, ValidTypeException {
 
     PutCommand cmd = null;
 
     if (command.equals(ApiDictionary.IHaleCommandType.SET_LIGHTING_ENABLED)
         || command.equals(ApiDictionary.IHaleCommandType.SET_LIGHTING_LEVEL)
         || command.equals(ApiDictionary.IHaleCommandType.SET_LIGHTING_COLOR)) {
 
       cmd = new PutCommand(command);
 
       if (ApiDictionary.iHaleCommandType2State(command).isType(arg.toString())) {
         cmd.addElement("arg", arg);
       }
       cmd.addElement("room", room);
     }
     else {
       throw new RuntimeException("IHaleCommandType is invalid.");
     }
 
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
    * @throws ValidTypeException Thrown when argument is invalid.
    */
   protected PutCommand handleHvacCommand(IHaleCommandType command, Object arg)
       throws RuntimeException, ParserConfigurationException, ValidTypeException {
 
     PutCommand cmd = null;
 
     if (command.equals(ApiDictionary.IHaleCommandType.SET_TEMPERATURE)) {
       cmd = new PutCommand(command);
 
       if (ApiDictionary.iHaleCommandType2State(command).isType(arg.toString())) {
         cmd.addElement("arg", arg);
       }
       else {
         throw new ValidTypeException();
       }
     }
     else {
       throw new RuntimeException("IHaleCommandType is invalid.");
     }
 
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
    * @throws ValidTypeException Thrown when argument is invalid.
    */
   protected PutCommand handleAquaponicsCommand(IHaleCommandType command, Object arg)
       throws ParserConfigurationException, ValidTypeException {
 
     PutCommand cmd = null;
 
     if (command.equals(ApiDictionary.IHaleCommandType.FEED_FISH)
         || command.equals(ApiDictionary.IHaleCommandType.SET_TEMPERATURE)
         || command.equals(ApiDictionary.IHaleCommandType.HARVEST_FISH)
         || command.equals(ApiDictionary.IHaleCommandType.SET_PH)
         || command.equals(ApiDictionary.IHaleCommandType.SET_WATER_LEVEL)) {
 
       // Generates the XML for the command.
       cmd = new PutCommand(command);
 
       if (ApiDictionary.iHaleCommandType2State(command).isType(arg.toString())) {
         cmd.addElement("arg", arg);
       }
       else {
         throw new ValidTypeException();
       }
     }
     else {
       throw new RuntimeException("IHaleCommandType is invalid.");
     }
 
     return cmd;
   }
 
   /**
    * This method illustrates a couple examples of what you might do after you got some state
    * information from the house.
    */
   public void exampleStateFromHouseSystems() {
     // Let's say I found out somehow that the Temperature of the house was 22.
     // First I have to represent this information appropriately.
     IHaleSystem system = IHaleSystem.HVAC;
     IHaleState state = IHaleState.TEMPERATURE;
     Integer temperature = 22;
     Long timestamp = (new Date()).getTime();
 
     // Now I can create a repository instance and store my state info.
     Repository repository = new Repository();
     repository.store(system, state, timestamp, temperature);
 
     // A little while later, I find out that there are some dead fish in the tank.
     // So let's add that info to the repository.
     system = IHaleSystem.AQUAPONICS;
     state = IHaleState.DEAD_FISH;
     Integer numDeadFish = 2; // R.I.P.
     timestamp = (new Date()).getTime();
     repository.store(system, state, timestamp, numDeadFish);
 
     // It's bad when fish die, so let's send a high priority status message.
     SystemStatusMessage message =
         new SystemStatusMessage(timestamp, system, SystemStatusMessageType.ALERT,
             "Fish are dying!!! Do something!");
     repository.store(message);
   }
 
   /**
    * A sample main program.
    * 
    * @param args Ignored.
    * @throws IOException Thrown when unable to close the FileInputStream.
    * @throws SAXException Thrown when XML parsing fails.
    * @throws ParserConfigurationException Thrown if error exists in parser configuration.
    * @throws XPathExpressionException Thrown if error exists in XPath expression.
    */
   public static void main(String[] args) throws XPathExpressionException,
       ParserConfigurationException, SAXException, IOException {
     IHaleBackend backend = new IHaleBackend();
     backend.doCommand(IHaleSystem.AQUAPONICS, null, IHaleCommandType.SET_PH, 7);
   }
 }
