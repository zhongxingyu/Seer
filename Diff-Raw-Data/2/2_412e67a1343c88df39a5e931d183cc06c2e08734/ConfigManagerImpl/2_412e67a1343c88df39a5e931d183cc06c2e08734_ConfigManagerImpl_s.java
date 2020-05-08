 package com.photon.phresco.commons.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 import com.photon.phresco.commons.api.ConfigManager;
 import com.photon.phresco.configuration.ConfigReader;
 import com.photon.phresco.configuration.Configuration;
 import com.photon.phresco.configuration.Environment;
 import com.photon.phresco.exception.ConfigurationException;
 import com.photon.phresco.util.Utility;
 
 public class ConfigManagerImpl implements ConfigManager {
 	
 	private static Document document = null;
 	private static Element rootElement = null;
 	private File configFile = null;
 	
 	public ConfigManagerImpl(File configFile) throws ConfigurationException {
 		this.configFile = configFile;
 		try {
 			if(!configFile.exists()) {
 				createNewDoc();
 			} else {
 				createDocFromExist(new FileInputStream(configFile));
 			}
 		} catch (Exception e) {
 			throw new ConfigurationException(e);
 		}
 		
 	}
 	
 	@Override
 	public List<Environment> getEnvironments(List<String> environmentNames)
 			throws ConfigurationException {
 		
 		if(!configFile.exists()) {
 			throw new ConfigurationException("Config File Not Exists");
 		}
 		ConfigReader configReader = new ConfigReader(configFile);
 		return configReader.getEnvironments(environmentNames);
 	}
 	
 	@Override
 	public void addEnvironments(List<Environment> environments)
 			throws ConfigurationException {
 		createEnvironment(environments, configFile);
 	}
 	
 	@Override
 	public void updateEnvironment(Environment environment) throws ConfigurationException {
 		String envName = environment.getName();
 		Node oldNode = getNode(getXpathEnv(envName).toString());
 		Element newNode = createEnvironmentNode(environment);
 		rootElement.replaceChild(newNode, oldNode);
 		try {
 			writeXml(new FileOutputStream(configFile));
 		} catch (FileNotFoundException e) {
 			throw new ConfigurationException(e);
 		}
 	}
 	
 	@Override
 	public void deleteEnvironment(String envName) throws ConfigurationException {
 		String xpath = getXpathEnv(envName).toString();
 		Element envNode = (Element) getNode(xpath);
 		envNode.getParentNode().removeChild(envNode);
 		try {
 			writeXml(new FileOutputStream(configFile));
 		} catch (FileNotFoundException e) {
 			throw new ConfigurationException(e);
 		}
 	}
 	
 	private void createEnvironment(List<Environment> envs, File configFile) throws ConfigurationException {
 		for (Environment env : envs) {
 			Element envNode = createEnvironmentNode(env);
 			rootElement.appendChild(envNode);
 		}
 		try {
 			writeXml(new FileOutputStream(configFile));
 		} catch (Exception e) {
 			throw new ConfigurationException(e);
 		}
 	}
 	
 	private Element createEnvironmentNode(Environment environment) throws ConfigurationException {
 		Element envNode = document.createElement("environment");
 		envNode.setAttribute("name", environment.getName());
 		envNode.setAttribute("desc", environment.getDesc());
 		envNode.setAttribute("default", Boolean.toString(environment.isDefaultEnv()));
 		for (Configuration configuration : environment.getConfigurations()) {
 			Element configNode = document.createElement(configuration.getType());
 			configNode.setAttribute("name", configuration.getName());
 //			configNode.setAttribute("appliesTo", configuration.getAppliesTo());
 			createProperties(configNode, configuration.getProperties());
 			envNode.appendChild(configNode);
 		}
 		return envNode;
 	}
 	
 	private void createProperties(Element configNode, Properties properties) {
 		Set<Object> keySet = properties.keySet();
 		for (Object key : keySet) {
 			String value = (String) properties.get(key);
 			Element propNode = document.createElement(key.toString());
 			propNode.setTextContent(value);
 			configNode.appendChild(propNode);
 		}
 	}
 	
 	private void createNewDoc() throws ConfigurationException {
 		try {
 			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
             domFactory.setNamespaceAware(false);
             DocumentBuilder builder = domFactory.newDocumentBuilder();
             document = builder.newDocument();
             rootElement = document.createElement("environments");
             document.appendChild(rootElement);
 		} catch (ParserConfigurationException e) {
 			throw new ConfigurationException(e);
 		}
 	}
 	
 	private void createDocFromExist(InputStream configFileStream) throws ConfigurationException {
 		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
 		domFactory.setNamespaceAware(false);
 		DocumentBuilder builder;
 		try {
 			builder = domFactory.newDocumentBuilder();
 			document = builder.parse(configFileStream);
 			rootElement = (Element) document.getElementsByTagName("environments").item(0);
 		} catch (ParserConfigurationException e) {
 			throw new ConfigurationException(e);
 		} catch (SAXException e) {
 			throw new ConfigurationException(e);
 		} catch (IOException e) {
 			throw new ConfigurationException(e);
 		} finally {
 			if(configFileStream != null) {
 				Utility.closeStream(configFileStream);
 			}
 		}
 	}
 	
 	private void writeXml(OutputStream fos) throws ConfigurationException {
 			TransformerFactory tFactory = TransformerFactory.newInstance();
 			Transformer transformer;
 			try {
 				transformer = tFactory.newTransformer();
 				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
 				Source src = new DOMSource(document);
 				Result res = new StreamResult(fos);
 				transformer.transform(src, res);
 			} catch (TransformerConfigurationException e) {
 				throw new ConfigurationException(e);
 			} catch (TransformerException e) {
 				throw new ConfigurationException(e);
 			} finally {
 				if(fos != null) {
 					Utility.closeStream(fos);
 				}
 			}
 		}
 	
 	private StringBuilder getXpathEnv(String envName) {
 		StringBuilder expBuilder = new StringBuilder();
 		expBuilder.append("/environments/environment[@name='"); 
 		expBuilder.append(envName);
 		expBuilder.append("']");	
 		return expBuilder;
 	}
 	
 	private Node getNode(String xpath) throws ConfigurationException {
 		XPathFactory xPathFactory = XPathFactory.newInstance();
 	    XPath newXPath = xPathFactory.newXPath();	
 		XPathExpression xPathExpression;
 		Node xpathNode = null;
 		try {
 			xPathExpression = newXPath.compile(xpath);
 			xpathNode = (Node) xPathExpression.evaluate(document, XPathConstants.NODE);
 		} catch (XPathExpressionException e) {
 			throw new ConfigurationException(e);
 		}
 		return xpathNode;
 	}
 
 	@Override
 	public List<Environment> getEnvironments() throws ConfigurationException {
 		ConfigReader configReader = new ConfigReader(configFile);
 		return configReader.getAllEnvironments();
 	}
 
 	@Override
 	public List<Configuration> getConfigurations(String envName, String type)
 			throws ConfigurationException {
 		ConfigReader configReader = null;
 		try {
 			configReader = new ConfigReader(configFile);
 			if (envName == null || envName.isEmpty() ) {
 				envName = configReader.getDefaultEnvName();
 			}
 		} catch (ConfigurationException e) {
 			throw new ConfigurationException(e);
 		}
 		return configReader.getConfigurations(envName, type);
 	}
 }
