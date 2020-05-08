 package com.sis.system;
 
 import java.io.File;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 /**
  * @todo re-implement by using commons digester 
  * @author CR
  *
  */
 public class XmlConfig implements ConfigInterface {
 	private static final Logger logger = Logger.getLogger(XmlConfig.class);
 	
 	protected HashMap<String, String> configCache = new HashMap<String, String>();
 	protected HashMap<String, String> configOverwrite = new HashMap<String, String>();
 	
 	private static XmlConfig instance = null;
 	
 	public static XmlConfig getSingleton() {
 		if (instance == null) {
 			try {
 				instance = new XmlConfig();
 			} catch (Exception e) {
 				handleException(e);
 			}
 		}
 		
 		return instance;
 	}
 	
 	protected static void handleException(Exception e) {
 		logger.fatal("configuration error occured", e);	
 	}
 	
 	protected Document xmlDoc = null;
 	
 	protected URL currentConfigFile;
 	
 	protected void initXmlDocument(URL filename) throws Exception {
 	    InputStream in = filename.openStream();
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 	    docFactory.setNamespaceAware(false);
 	    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 	    xmlDoc = docBuilder.parse(in);
 	    
 	    // invalidate cache
 	    configCache.clear();
 	    currentConfigFile = filename;
 
 	    ConfigurableVariable.resetAll();
 	    
 	    logger.trace("successfully re-parsed " + filename.toString());
 	}
 	
 	public void init() {
 		// TODO: add inotify to reparse config while runtime.
 		try {
 			initXmlDocument(new File("etc/config.xml").toURI().toURL());
 		} catch (MalformedURLException e) {
 			logger.error("should never happen", e);
 		} catch (Exception e) {
 			logger.error("could not parse configuration file!", e);
 		}
 	}
 	
 	public boolean loadConfig(URL filename) {
 		try {
 			initXmlDocument(filename);
 		} catch(Exception e) {
 			handleException(e);
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public String getValue(String name) {
 		if (configOverwrite.containsKey(name)) {
 			return configOverwrite.get(name);
 		}
 		
 		// cache hit
 		if (configCache.containsKey(name)) {
 			return configCache.get(name);
 		}
 		
 		try {
 			XPath xpath = XPathFactory.newInstance().newXPath();
 			XPathExpression expr;
 			expr = xpath.compile("//config/" + name + "/text()");
 		
 			String value = (String) expr.evaluate(xmlDoc, XPathConstants.STRING);
 			
 			if (value.length() > 0) {
 				configCache.put(name, value);
 			}
 			
 			return value;
 		} catch (XPathExpressionException e) {
 			handleException(e);
 			return null;
 		}
 	}
 	
 	public HashMap<String, String> getValues(String name) {
 		try {
 			XPath xpath = XPathFactory.newInstance().newXPath();
 			XPathExpression expr;
 			expr = xpath.compile("//config/" + name + "/");
 		
 			NodeList nl = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);
 			
 			HashMap<String, String> list = new HashMap<String, String>();
 			for(int i = 0, j = nl.getLength(); i != j; i++) {
 				list.put(nl.item(i).getNodeName(), nl.item(i).getNodeValue());
 			}
 			
 			return list;
 		} catch (XPathExpressionException e) {
 			handleException(e);
 			return null;
 		}
 	}
 	
 	public List<String> getValueList(String name) {
 		ArrayList<String> list = new ArrayList<String>();
 
 		try {
 			XPath xpath = XPathFactory.newInstance().newXPath();
 			XPathExpression expr;
 			expr = xpath.compile("//config/" + name + "/*");
 		
 			NodeList nl = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);
 			
 			for(int i = 0, j = nl.getLength(); i != j; i++) {
 				list.add(nl.item(i).getTextContent());
 			}
 		} catch(XPathExpressionException e) {
 			handleException(e);
 		}
 		
 		return list;
 	}
 
 	@Override
 	public void reinitialize() {
 		loadConfig(currentConfigFile);
 		
 		logger.trace("configuration reinitialized.");
 	}
 
 	@Override
 	public void shutdown() {
 		logger.trace("configuration shut down.");
 	}
 
 	public void overwriteValue(String string, String string2) {
 		ConfigurableVariable.trySetConfigurableVariableByConfigPath(string, string2);
 		
 //		configVariables.get(string).setValueFromString(string2);
 		configOverwrite.put(string, string2);
 		
 		logger.trace("configurable variable overwritten, configPath = " + string + ", value = " + string2);
 	}
 
 	public boolean hasValue(String string) {
 		String v = getValue(string);
		return v != null && v.length() > 0;
 	}
 //
 //	@Override
 //	public void addConfigurableVariable(ConfigurableVariable<?> var) {
 //		configVariables.put(var.getConfigPath(), var);
 //	}
 }
