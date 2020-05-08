 package io.arkeus.fatebot.config;
 
 import io.arkeus.fatebot.user.UserManager;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class ConfigReader {
 	private final Document document;
 
 	public ConfigReader(final File configFile) throws ParserConfigurationException, SAXException, IOException {
 		if (!configFile.exists() || !configFile.isFile() || !configFile.canRead()) {
 			throw new IllegalArgumentException("Must pass readable config file to Fate Bot");
 		}
 		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 		final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 		this.document = documentBuilder.parse(configFile);
 	}
 
 	public Config generateConfiguration() {
 		final Config config = new Config();
 
 		config.setNick(getString("nick", null));
 		config.setAltNick(getString("altNick", ""));
 		config.setPassword(getString("password", ""));
 		config.setServer(getString("server", null));
 		config.setChannel(getString("channel", null));
 		config.setVerbose(getBoolean("verbose", false));
 		config.setLogin(getString("login", "FateBot"));
 		config.setPrefix(getString("prefix", ";"));
 		config.setRoot(getString("directory", null));
 		config.setAdministrators(getAdministrators());
 
 		return config;
 	}
 
 	private Boolean getBoolean(final String tagName, final Boolean defaultValue) {
 		return getValue(tagName, defaultValue, Boolean.class);
 	}
 
 	private String getString(final String tagName, final String defaultValue) {
 		return getValue(tagName, defaultValue, String.class);
 	}
 
 	@SuppressWarnings("unused")
 	private Integer getInteger(final String tagName, final Integer defaultValue) {
 		return getValue(tagName, defaultValue, Integer.class);
 	}
 
 	private <T> T getValue(final String tagName, final T defaultValue, final Class<T> klass) {
 		final Element element = getElement(tagName);
 		if (element == null) {
 			if (defaultValue == null) {
 				throw new IllegalArgumentException("Expected config value '" + tagName + "' to be defined");
 			}
 			return defaultValue;
 		}
 
 		final String value = element.getTextContent();
 		if (klass == Boolean.class) {
 			return klass.cast(value.equalsIgnoreCase("true") ? true : false);
 		} else if (klass == String.class) {
 			return klass.cast(value);
 		} else if (klass == Integer.class) {
 			return klass.cast(Integer.parseInt(value));
 		}
 
 		throw new IllegalArgumentException("Cannot convert value '" + value + "' to type '" + klass.getCanonicalName() + "'");
 	}
 
 	private Element getElement(final String tagName) {
 		final NodeList nodes = document.getElementsByTagName(tagName);
 		final int length = nodes.getLength();
 		if (length == 0) {
 			return null;
 		}
 		if (length != 1) {
 			throw new IllegalArgumentException("Expected exactly one config value for tag '" + tagName + "', found " + length);
 		}
 		return (Element) nodes.item(0);
 	}
 
 	private Set<String> getAdministrators() {
		final Element administratorElement = getElement("administrator");
 		final NodeList administratorNodes = administratorElement.getChildNodes();
 		final Set<String> administrators = new HashSet<String>(administratorNodes.getLength());
 		for (int i = 0; i < administratorNodes.getLength(); i++) {
 			administrators.add(UserManager.normalizeNick(administratorNodes.item(i).getTextContent()));
 		}
 		return administrators;
 	}
 }
