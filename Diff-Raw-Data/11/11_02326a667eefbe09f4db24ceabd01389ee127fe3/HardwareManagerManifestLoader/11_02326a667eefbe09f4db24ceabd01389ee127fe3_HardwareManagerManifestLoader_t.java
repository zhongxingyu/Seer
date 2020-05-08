 package edu.mines.acmX.exhibit.input_services.hardware;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.logging.log4j.*;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  * Creates a HardwareManagerMetaData object given a specified configuration
  * file. Throws manifest exceptions for errors related to improper tags and
  * attributes. 
  * 
  * @author Aakash Shah
  * @author Ryan Stauffer
  * 
  * @see
  * 	{@link HardwareManager}
  *  {@link HardwareManagerManifestException}
  */
 public class HardwareManagerManifestLoader {
 	private static Logger log = LogManager.getLogger(HardwareManagerManifestLoader.class.getName()); 
 	
 	/**
 	 * Loads a manifest given a specific file. 
 	 * 
 	 * @param filename
 	 * @return A HardwareManagerMetaData object
 	 * @throws HardwareManagerManifestException
 	 */
 	public static HardwareManagerMetaData load(String filename) 
 			throws HardwareManagerManifestException {
 		return load(HardwareManagerManifestLoader.class.getClassLoader().getResourceAsStream(filename));		
 	}
 	
 	/**
 	 * Loads a manifest given a specific input stream. 
 	 * 
 	 * @param is The input stream
 	 * @return A HardwareManagerMetaData object
 	 * @throws HardwareManagerManifestException 
 	 */
 	public static HardwareManagerMetaData load(InputStream is)
 			throws HardwareManagerManifestException{
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			Document manifest = docBuilder.parse(is);
 			manifest.getDocumentElement().normalize();
 			return parseManifest(manifest);
 		} catch (Exception e) {
			throw new HardwareManagerManifestException("Could not load manifest\n" + e.toString());
 		}
 	}
 	
 	/**
 	 * Parses the manifest tag
 	 * 
 	 * @param manifest The XML document object
 	 * @return A HardwareManagerMetaData object
 	 * @throws HardwareManagerManifestException
 	 */
 	private static HardwareManagerMetaData parseManifest(Document manifest)
 			throws HardwareManagerManifestException {
 		
 		HardwareManagerMetaData hmmd = new HardwareManagerMetaData();
		Element element = (Element) manifest.getElementsByTagName("manifest").item(0);
 		
 		NodeList funcTagList = element.getElementsByTagName("functionalities");
 		if (funcTagList.getLength() <= 0) {
 			
 			log.info("No functionalities tag");
 			throw new HardwareManagerManifestException("No functionalities tag!");
 		}
 		NodeList deviceTagList = element.getElementsByTagName("devices");
 		if (deviceTagList.getLength() <= 0) {
 			log.info("No devices tag");
 			throw new HardwareManagerManifestException("No devices tag!");
 		}
 		
 		parseFunctionalities(funcTagList, hmmd);
 		parseDevices(deviceTagList, hmmd);
 		
 		return hmmd;
 	}
 	
 	/**
 	 * Parses the functionalities tag.
 	 * @param funcTagList NodeList with all the functionalities tag
 	 * @param hdsmd the meta data object to store information into
 	 * @throws HardwareManagerManifestException
 	 */
	private static void parseFunctionalities(NodeList funcTagList, HardwareManagerMetaData hdsmd)
 			throws HardwareManagerManifestException {
 		
 		Map<String, String> supportList = new HashMap<String, String>();
 		for (int i = 0; i < funcTagList.getLength(); ++i) {
 			Element funcTag = (Element) funcTagList.item(i);
 			NodeList supportTagList = funcTag.getElementsByTagName("supports");
 			
 			if (supportTagList.getLength() <= 0) {
 				throw new HardwareManagerManifestException("No supports definition in functionalities tag");
 			}
 			
 			for (int j = 0; j < supportTagList.getLength(); ++j) {
 				Element supportTag = (Element) supportTagList.item(j);
 				String name = supportTag.getAttribute("name").toLowerCase();
 				String intrface = supportTag.getAttribute("interface");
 				
 				if (name.isEmpty() || intrface.isEmpty()) {
 					log.info("No name or interface tag");
 					throw new HardwareManagerManifestException("Missing attribute for support tag");
 				}
 				
 				if (supportList.containsKey(name)) {
 					throw new HardwareManagerManifestException("Duplicate support defintions");
 				}
 				
 				supportList.put(name, intrface);
 			}
 		}
 		
 		hdsmd.setFunctionalities(supportList);
 	}
 	
 	/**
 	 * Parses the devices tags
 	 * @param devicesTagList NodeList with all the devices tag
 	 * @param hmmd the meta data object to store information into
 	 * @throws HardwareManagerManifestException
 	 */
 	private static void parseDevices(NodeList devicesTagList,
 									 HardwareManagerMetaData hmmd)
 			throws HardwareManagerManifestException {
 		
 		Map<String, String> deviceDrivers = new HashMap<String, String>();
 		Map<String, List<String>> deviceFunc = new HashMap<String, List<String>>();
 		
 		for (int i = 0; i < devicesTagList.getLength(); ++i) {
 			Element devicesTag = (Element) devicesTagList.item(i);
 			NodeList deviceTagList = devicesTag.getElementsByTagName("device");
 			
 			if (deviceTagList.getLength() <= 0) {
 				throw new HardwareManagerManifestException("No device definitions in devices tag");
 			}
 			
 			for (int j = 0; j < devicesTagList.getLength(); ++j) {
 				Element deviceTag = (Element) deviceTagList.item(j);
 				String name = deviceTag.getAttribute("name").toLowerCase();
 				String driver = deviceTag.getAttribute("driver");
 				
 				if (name.isEmpty() || driver.isEmpty()) {
 					throw new HardwareManagerManifestException("Missing attribute for device tag");
 				}
 				
 				if (deviceDrivers.containsKey(name)) {
 					throw new HardwareManagerManifestException("Duplicate driver definitions");
 				}
 				
 				NodeList provideTagList = deviceTag.getElementsByTagName("provides");
 				
 				if (provideTagList.getLength() <= 0) {
 					throw new HardwareManagerManifestException("No provides definitions in device tag");
 				}
 				
 				List<String> provideList = parseDeviceProvides(provideTagList);
 				
 				deviceDrivers.put(name, driver);
 				deviceFunc.put(name, provideList);
 			}
 		}
 		
 		hmmd.setDevices(deviceDrivers);
 		hmmd.setDeviceSupports(deviceFunc);
 	}
 	
 	/**
 	 * 
 	 * @param provideTagList
 	 * @return List of provided functionalities for a device
 	 * @throws HardwareManagerManifestException
 	 */
 	private static List<String> parseDeviceProvides(NodeList provideTagList)
 			throws HardwareManagerManifestException{
 		List<String> deviceFunc = new ArrayList<String>();
 		
 		for (int k = 0; k < provideTagList.getLength(); ++k) {
 			Element provideTag = (Element) provideTagList.item(k);
 			String name = provideTag.getAttribute("name").toLowerCase();
 			if (name.isEmpty()) {
 				throw new HardwareManagerManifestException("Missing attribute for provides tag");
 			}
 			
 			deviceFunc.add(name);
 		}
 		
 		return deviceFunc;
 	}
 }
