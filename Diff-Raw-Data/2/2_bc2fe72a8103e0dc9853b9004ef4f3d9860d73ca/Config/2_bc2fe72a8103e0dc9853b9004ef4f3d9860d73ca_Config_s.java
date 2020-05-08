 package edu.columbia.slime.conf;
 
 import java.io.InputStream;
 import java.io.FileInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.Stack;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import javax.xml.parsers.SAXParser;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import edu.columbia.slime.Slime;
 import edu.columbia.slime.util.Network;
 import edu.columbia.slime.service.Service;
 
 @SuppressWarnings("serial")
 public class Config extends HashMap<String, Object> {
 	public static final Log LOG = LogFactory.getLog(Slime.class);
 
 	private static final String configPath = "conf" + File.separator + "slime.xml";
 
 	public static final String ELEMENT_NAME_SERVER = "server";
 	public static final String ELEMENT_NAME_SERVICE = "service";
 	public static final String ELEMENT_NAME_BASE = "base";
 	public static final String ELEMENT_NAME_DIST = "dist";
 
 	public static final String ATTR_NAME_USER = "user";
 	public static final String ATTR_NAME_PASSWD = "passwd";
 	public static final String ATTR_NAME_PASSPHRASE = "passphrase";
 	public static final String ATTR_NAME_PASSPHRASEFILE = "passphrase.file";
 
 	public static final String PROPERTY_NAME_MAINCLASS = "main.class";
 	public static final String PROPERTY_NAME_LAUNCHERADDR = "launcher.address";
 
 	private Map<String, String> thisServer;
 
 	public class ConfigParser extends DefaultHandler {
 		private static final String ELEMENT_NAME_SLIME = "slime";
 		private static final String ELEMENT_NAME_CONFIG = "configuration";
 		private static final String ELEMENT_NAME_THREADS = "threads";
 		private static final String ELEMENT_NAME_DIRS = "directories";
 		private static final String ELEMENT_NAME_PLUGINS = "plugins";
 		private static final String ELEMENT_NAME_SERVERS = "servers";
 		private static final String ELEMENT_NAME_SERVICES = "services";
 
 		private static final String ATTR_NAME_NAME = "name";
 		private static final String ATTR_NAME_THREADS = "threads";
 
 		private Stack<String> elementStack = new Stack<String>();
 
 		private ServiceConfigParser serviceParser = null;
 
 		Map<String, Object> map;
 		String currentCharacters = "";
 		Attributes currentAttr = null;
 
 		ConfigParser(Map<String, Object> map) {
 			this.map = map;
 		}
 
 		protected Map<String, String> convertToMap(Attributes attr) {
 			int length = attr.getLength();
 			Map<String, String> map = new HashMap<String, String>();
 			for (int i = 0; i < length; i++) {
 				map.put(attr.getQName(i), attr.getValue(i));
 System.out.println("Converting (" + attr.getQName(i) + ":" +  attr.getValue(i));
 			}
 			return map;
 		}
 
 		public void startElement(String uri, String localName, String qName, Attributes attributes)
 			throws SAXException {
 			if (ELEMENT_NAME_SERVICE.equals(qName)) {
 				Service s = Slime.getInstance().getService(attributes.getValue(ATTR_NAME_NAME));
 				if (s == null) {
 					LOG.warn("Cannot find the service " + attributes.getValue(ATTR_NAME_NAME));
 					serviceParser = new DefaultServiceConfigParser();
 				}
 				else {
 					serviceParser = Slime.getInstance().getService(attributes.getValue(ATTR_NAME_NAME)).getConfigParser();
 				}
 			}
 
 			if (serviceParser != null) {
 				serviceParser.startElement(uri, localName, qName, attributes);
 				return;
 			}
 
 			if (elementStack.empty() && !ELEMENT_NAME_SLIME.equals(qName))
 				throw new SAXException("A Slime configuration file should begin with an element named 'slime'");
 
 			elementStack.push(qName);
 			currentCharacters = "";
 			currentAttr = attributes;
 		}
 
 		@SuppressWarnings("unchecked")
 		public void endElement(String uri, String localName, String qName)
 			throws SAXException {
 			if (serviceParser != null) {
 				serviceParser.endElement(uri, localName, qName);
 
 				if (ELEMENT_NAME_SERVICE.equals(qName)) {
 					serviceParser = null;
 				}
 				return;
 			}
 
 			if (elementStack.empty() || !elementStack.peek().equals(qName))
 				throw new SAXException("Unmatched closing element: " + qName);
 
 			String startElement = elementStack.pop();
 
 			if (startElement.equals(ELEMENT_NAME_SERVER)) {
 				Map<String, Map<String, String>> servers = (Map<String, Map<String, String>>) map.get(ELEMENT_NAME_SERVER);
 				if (servers == null) {
 					servers = new LinkedHashMap<String, Map<String, String>>();
 					map.put(ELEMENT_NAME_SERVER, servers);
 				}
 
 				Map<String, String> attrMap = convertToMap(currentAttr);
 				if (Network.checkIfMyAddress(currentCharacters))
 					thisServer = attrMap;
 				servers.put(currentCharacters, attrMap);
 			}
 			else {
 				map.put(qName, currentCharacters);
 			}
 			currentCharacters = "";
 			currentAttr = null;
 		}
 
 		public void characters(char ch[], int start, int length)
 			throws SAXException {
 			if (elementStack.empty())
 				throw new SAXException("String without element start");
 
 			if (serviceParser != null) {
 				serviceParser.characters(ch, start, length);
 				return;
 			}
 
 			currentCharacters += new String(ch, start, length);
 		}
 	}
 
 	public String get(String key) {
 		if (key.equals(ELEMENT_NAME_SERVER))
 			throw new RuntimeException("Call getServerInfo() method instead");
 
 		return (String) super.get(key);
 	}
 
 	@SuppressWarnings("unchecked")
 	public Map<String, Map<String, String>> getServerInfo() {
 		return (Map<String, Map<String, String>>) super.get(ELEMENT_NAME_SERVER);
 	}
 
 	public Map<String, String> getThisServerInfo() {
 		return thisServer;
 	}
 
 	public Config() {
 		init(configPath, getClass());
 	}
 
 	public Config(Map<String, Class> configPaths) {
 		init(configPath, getClass());
 		for (String path : configPaths.keySet()) {
 			init(path, configPaths.get(path));
 		}
 	}
 
 	public Config(String configPath, Class klass) {
 		init(configPath, klass);
 	}
 
 	private void init(String configPath, Class klass) {
 		InputStream is = null;
 		if (klass != null)
			is = klass.getResourceAsStream(File.separator + configPath);
 
 		if (is == null) {
 			try {
 				is = new FileInputStream(configPath);
 			} catch (IOException ioe) {
 				throw new IllegalArgumentException("Cannot find " + configPath + ".");
 			}
 		}
 
 		javax.xml.parsers.SAXParserFactory saxFactory = javax.xml.parsers.SAXParserFactory.newInstance();
 		try {
 			SAXParser parser = saxFactory.newSAXParser();
 			parser.parse(is, new ConfigParser(this));
 		}
 		catch (Exception e) {
 			throw new IllegalArgumentException(e);
 		}
 	}
 }
