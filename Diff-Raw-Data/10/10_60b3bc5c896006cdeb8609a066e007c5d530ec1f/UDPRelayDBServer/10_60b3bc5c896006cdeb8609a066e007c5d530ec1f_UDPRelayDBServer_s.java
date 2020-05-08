 package nl.utwente.ewi.udprelaydb;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import nl.utwente.ewi.udprelay.XMLUtil;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * Server for relaying incoming UDP packets to a database.
  *
  * @author   Daan Scheerens (d.scheerens@student.utwente.nl)
  * @version  1.00 (26-10-2010 16:01:10)
  */
 public class UDPRelayDBServer {
 	
 	/** The sockets on which the UDP packets are to be relayed. */
 	protected List<UDPRelayDBSocket> relaySockets = new ArrayList<UDPRelayDBSocket>();
 	protected DbConfiguration dbConfig;
 	
 	/**
 	 * Creates a new UDPRelayServer and initializes it using the configuration in
 	 * the configuration file.
 	 *
 	 * @param  options  The application options.
 	 * @pre    options != null
 	 */
 	public UDPRelayDBServer(Map<String, String> options) {
 		// Get configuration file.
 		String configFile = options.get("config_file");
 		if (configFile == null) {
 			configFile = getDefaultOptions().get("config_file");
 		}
 		
 		// Load the configuration.
 		Document config = loadXMLFile(configFile);
 		
 		// Setup relay server based on the configuration.
 		if (config != null) {
 			// Read options.
 			Map<String, String> configOptions = getDefaultOptions();
 			NodeList configOptionList = XMLUtil.getElementByPath(config.getDocumentElement(), "options").getElementsByTagName("option");
 			for (int optionIndex = 0; optionIndex < configOptionList.getLength(); optionIndex++) {
 				Element option = (Element)configOptionList.item(optionIndex);
 				configOptions.put(option.getAttribute("name"), option.getAttribute("value"));
 			}
 			configOptions.putAll(options);
 			options = configOptions;
 			
 			// Read socket definitions.
 			NodeList socketDefinitions = XMLUtil.getElementByPath(config.getDocumentElement(), "sockets").getElementsByTagName("socket");
 			for (int socketDefinitionIndex = 0; socketDefinitionIndex < socketDefinitions.getLength(); socketDefinitionIndex++) {
 				Element socketDefinition = (Element)socketDefinitions.item(socketDefinitionIndex);
 				
 				// Create relay socket.
 				String name = XMLUtil.getSubElementContents(socketDefinition, "name");
 				int port = Integer.parseInt(XMLUtil.getSubElementContents(socketDefinition, "port"));
 				UDPRelayDBSocket relaySocket = new UDPRelayDBSocket(name, port);
 				relaySockets.add(relaySocket);
 				relaySocket.setLogPackets(Boolean.parseBoolean(options.get("log_packets")));
 				relaySocket.addDbConfiguration(dbConfig);
 			}
 			
			// Read database configuration
			dbConfig = new DbConfiguration(XMLUtil.getElementByPath(config.getDocumentElement(), "database"));
 		}
 	}
 	
 	/**
 	 * Starts the relay server.
 	 */
 	public void start() {
 		for (UDPRelayDBSocket socket : relaySockets) {
 			socket.start();
 		}
 	}
 	
 	/**
 	 * Stops the relay server.
 	 */
 	public void stop() {
 		for (UDPRelayDBSocket socket : relaySockets) {
 			socket.stop();
 		}
 	}
 	
 	/**
 	 * Attempts to load the specified XML file. The contents of the file is parsed
 	 * and stored in a DOM instance.
 	 *
 	 * @param   xmlFile  Name of the XML file that is to be loaded.
 	 * @pre     xmlFile != null && !xmlFile.equals("")
 	 * @return  A Document DOM node that represents the parsed XML file. If the
 	 *          file could not be loaded this method will return null.
 	 */
 	protected Document loadXMLFile(String xmlFile) {
 		Document doc = null;
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder xmlParser = factory.newDocumentBuilder();
 			doc = xmlParser.parse(new File(xmlFile));
 		} catch (ParserConfigurationException pce) {
 			System.out.println("Failed to load XML parser, reason: " + pce);
 		} catch (SAXException se) {
 			System.out.println("Unable to parse XML file '" + xmlFile + "', reason: " + se);
 		} catch (IOException ioe) {
 			System.out.println("Unable to parse XML file '" + xmlFile + "', reason: " + ioe);
 		}
 		return doc;
 	}
 	
 	/**
 	 * Returns the default values of the application options.
 	 *
 	 * @return  A Map containing the default options for the application.
 	 * @post    result != null
 	 */
 	public static Map<String, String> getDefaultOptions() {
 		Map<String, String> options = new HashMap<String, String>();
 		
 		options.put("config_file", "resources/db_config.xml");
 		options.put("log_packets", "false");
 		
 		return options;
 	}
 	
 	/**
 	 * Returns the application options from the specified command line arguments.
 	 *
 	 * @return  A Map containing the options for the application.
 	 * @post    result != null
 	 */
 	public static Map<String, String> getCommandLineOptions(String[] args) {
 		Map<String, String> options = new HashMap<String, String>();
 		
 		for (int index = 0; index < args.length; index++) {
 			int assignIndex = args[index].indexOf("=");
 			if (assignIndex > 0) {
 				options.put(args[index].substring(0, assignIndex), args[index].substring(assignIndex + 1));
 			}
 		}
 		
 		return options;
 	}
 	
 	/**
 	 * Application start method.
 	 *
 	 * @param  args  The command line arguments for the application.
 	 */
 	public static void main(String[] args) {
 		// Create & start relay server.
 		final UDPRelayDBServer server = new UDPRelayDBServer(getCommandLineOptions(args));
 		server.start();
 		
 		// Add a shutdown hook.
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			public void run() {
 				server.stop();
 			}
 		});
 	}
}
