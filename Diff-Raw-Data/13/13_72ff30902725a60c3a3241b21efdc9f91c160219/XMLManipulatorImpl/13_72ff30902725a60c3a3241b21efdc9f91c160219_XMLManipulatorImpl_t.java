 package org.jboss.jbossesb.eclipse.plugin.controller;
 
 import java.io.File;
 import java.io.IOException;
import java.net.URL;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.jboss.jbossesb.eclipse.plugin.configuration.Activator;
 import org.jboss.jbossesb.eclipse.plugin.model.QueueTuple;
 import org.jboss.jbossesb.eclipse.plugin.model.XMLAttribute;
 import org.jboss.jbossesb.eclipse.plugin.model.XMLDocument;
 import org.jboss.jbossesb.eclipse.plugin.model.XMLElement;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 import org.xml.sax.SAXException;
 
 /**
  * The class allows load and store JBossESB configuration file
  * 
  * @author Tomas Sedmik, tomas.sedmik@gmail.com
  * @since 2012-11-04
  */
 public class XMLManipulatorImpl implements XMLManipulator {
 
 	private File confFile; // JBossESB configuration file
 	private ResourceBundle configuration = ResourceBundle.getBundle("org.jboss.jbossesb.eclipse.plugin.configuration.configuration"); // PlugIn configuration
 	private static Logger log = Logger.getLogger(XMLManipulatorImpl.class.getName());
 
 	/**
 	 * Create a XMLManipulator class with a given configuration file
 	 * 
 	 * @param confFile - JBossESB configuration file
 	 */
 	public XMLManipulatorImpl(File confFile) {
 		this.confFile = confFile;
 	}
 
 	/**
 	 * Testing a file if is a valid against JBossESB XML Schema
 	 * 
 	 * @return <b>true</b> - a file is valid, <b>false</b> - otherwise
 	 */
 	public boolean isValid() {
 
 		String schemaLang = "http://www.w3.org/2001/XMLSchema";
 		SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
 
 		try {
			
			// locate xml schema file
			URL url = Platform.getBundle(Activator.PLUGIN_ID).getEntry(configuration.getString("JBossESB-XMLSchema"));
			String fileURL = FileLocator.toFileURL(url).toString();
			Schema schema = factory.newSchema(new File(fileURL.substring(5)));
			
 			Validator validator = schema.newValidator();
 			validator.validate(new StreamSource(confFile));
 		} catch (SAXException e) {
 			log.log(Level.INFO, "Validation failed", e);
 			return false;
 		} catch (IOException e) {
 			log.severe("JBossESB configuration file: " + confFile + "isn't reachable" + e.toString());			
 			return false;
 		}
 
 		return true;
 	}
 	
 	/**
 	 * Backtracking parent elements and constructing address string from their names
 	 *  
 	 * @param node starting Node
 	 * @return address string - in form "/.../grandParentName/parentName/nodeName"
 	 */
 	public String getAddress(Node node) {
 		
 		if (node == null || !(node.getNodeType() == Node.ELEMENT_NODE)) return null;
 		
 		Node temp = node;
 		StringBuilder addressBuilder = new StringBuilder(node.getNodeName());
 		
 		while (temp.getParentNode().getParentNode() != null) {
 			temp = temp.getParentNode();
 			addressBuilder.insert(0, "/");
 			addressBuilder.insert(0, temp.getNodeName());
 		}
 		addressBuilder.insert(0, "/");
 		
 		return addressBuilder.toString();
 	}
 	
 	/**
 	 * Create XMLElement from parameters
 	 * 
 	 * @param node representation of XML element from configuration file
 	 * @param propertiesData additional information to element from properties file
 	 * @return XMLElement or null in case that input is null
 	 */
 	public XMLElement createXMLElement(Node node, Map<String, String> propertiesData) {
 		
 		if (node == null || propertiesData == null) return null;
 		
 		XMLElement result = new XMLElement();
 		String address = getAddress(node);
 		result.setAddress(address);
 		
 		// add additional information to the element from properties file
 		String temp = propertiesData.get(address);
 		if (temp != null) {
 			String[] temp2 = temp.split("#");
 			result.setName(temp2[2]);
 			if (temp2.length == 4) {
 				result.setHint(temp2[3]);
 			} else {
 				result.setHint(null);
 			}
 		}
 		
 		// attributes
 		NamedNodeMap attributes = node.getAttributes();
 		for (int i = 0; i < attributes.getLength(); i++) {
 			
 			String name = attributes.item(i).getNodeName();
 			String value = attributes.item(i).getNodeValue();			
 			
 			// add additional information to the attributes from properties file
 			String additionalData = propertiesData.get(address.concat("/").concat(name));
 			String[] addDataArray = additionalData.split("#");			
 			boolean required = false;
 			if (addDataArray[2].equals("R")) {
 				required = true;
 			}
 			List<String> allowedValues = new ArrayList<String>();
 			String[] allValues = addDataArray[3].split(",");
 			if (!allValues[0].equals("")) {
 				for (int j = 0; j < allValues.length; j++) {
 					allowedValues.add(allValues[j]);
 				}
 			}
 			
 			XMLAttribute attr;			
 			
 			// could be without 'hint'
 			if (addDataArray.length <= 6) {
 				attr = new XMLAttribute(addDataArray[1], required, allowedValues, value, addDataArray[5], null, name);
 			} else {
 				attr = new XMLAttribute(addDataArray[1], required, allowedValues, value, addDataArray[5], addDataArray[6], name);
 			}
 			result.addAttribute(attr);
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Write JBossESB configuration data into a file
 	 * 
 	 * @param document W3C DOM Document representation of JBossESB configuration file
 	 * @throws XMLException some error during processing
 	 */
 	private void writeDataIntoConfigurationFile(Document document) throws XMLException {
 		
 		try {
 			
 			TransformerFactory tf = TransformerFactory.newInstance();
 			tf.setAttribute("indent-number", 2);
 			Transformer tr = tf.newTransformer();
 			tr.setOutputProperty(OutputKeys.INDENT, "yes");
 			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 			DOMSource source = new DOMSource(document);
 			StreamResult result = new StreamResult(confFile);
 			tr.transform(source, result);
 			
 		} catch (TransformerConfigurationException e) {
 			log.severe("Creating configuration document failed (can't create Transformer)!");
 			throw new XMLException("Creating configuration document failed (can't create Transformer)!", e); 
 		} catch (TransformerException e) {
 			log.severe("Creating configuration document failed (some error during transformation)!");
 			throw new XMLException("Creating configuration document failed (some error during transformation)!", e);
 		} catch (NullPointerException e) {
 			log.severe("Can't write null Document!");
 			throw new XMLException("Can't write null Document!", e);
 		}
 		
 		log.info("Configuration file saved");
 	}
 	
 	/**
 	 * Creates new W3C DOM Document representation of JBossESB configuration file for further processing
 	 * 
 	 * @param empty true - creates new DOM Document, false - parse set configuration file
 	 * @return W3C DOM Document representation of JBossESB configuration file
 	 * @throws XMLException creating w3c XML document failed
 	 */
 	private Document initializeDOMDocument(boolean empty) throws XMLException {
 		
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder;
 		Document document;
 		try {
 			builder = factory.newDocumentBuilder();
 			if (empty) {
 				document = builder.newDocument();
 			} else {
 				document = builder.parse(confFile);
 			}
 			
 		} catch (Exception e1) {
 			log.severe("Creating w3c XML document failed");
 			throw new XMLException("Creating w3c XML document failed", e1);
 		}
 		
 		log.info("DOM Document initialization ... OK");
 		
 		return document;
 	}
 	
 	/**
 	 * load JBossESB configuration file into memory (XMLDocument). Using Breadth-First Search algorithm (BFS).
 	 * 
 	 * @param document w3c DOM Document representation of JBossESB configuration file
 	 * @return XMLDocument representation of JBossESB configuration file
 	 * @throws XMLException if configuration of some element/attribute in properties file is missing
 	 */
 	private XMLDocument loadData(Document document) throws XMLException {
 		
 		XMLDocument result = new XMLDocument();		
 		Queue<QueueTuple> queue = new LinkedList<QueueTuple>();
 		queue.offer(new QueueTuple(document.getFirstChild(), null));
 		while (!queue.isEmpty()) {
 			
 			Node temp = queue.peek().getNode();
 			XMLElement parent = queue.peek().getParent();
 			queue.poll();
 			
 			Map<String, String> propertiesData = PropertiesManipulator.extractConfiguration(getAddress(temp));
 			XMLElement element = createXMLElement(temp, propertiesData);
 			
 			// processing error
 			if (element == null) {
 				log.severe("Process problem with creating XMLElement (probably configuration in properties file is missing)");
 				throw new XMLException("Process problem with creating XMLElement (probably configuration in properties file is missing)");
 			}
 			
 			// setting parent
 			if (parent == null) {
 				result.setJbossesb(element);
 			} else {
 				parent.addChildElement(element);
 			}
 			
 			// enqueue child nodes
 			Node child = temp.getFirstChild();
 			while (child != null) {				
 				if (child.getNodeType() == Node.ELEMENT_NODE) {
 					queue.offer(new QueueTuple(child, element));
 				}
 				child = child.getNextSibling();
 			}
 		}
 		
 		log.info("Loading configuration file ... OK");
 		
 		return result;
 	}
 	
 	/**
 	 * actualizes data in w3c DOM Document representation of JBossESB configuration file
 	 * 
 	 * @param document XMLDocument representation of JBossESB configuration file
 	 * @return actualized w3c DOM Document representation of JBossESB configuration file
 	 * @throws XMLException error during processing
 	 */
 	private Document changeDataInConfigurationFile(XMLDocument document) throws XMLException{
 		
 		// initialization
 		Document xmlDocument = initializeDOMDocument(true);
 				
 		// change data in configuration file (Breadth-first Search algorithm)		
 		Queue<QueueTuple> queue = new LinkedList<QueueTuple>();
 		queue.offer(new QueueTuple(null, document.getJbossesb()));
 		while (!queue.isEmpty()) {
 			
 			XMLElement currentElement = queue.peek().getParent();
 			Node parent = queue.poll().getNode();
 			
 			// set element's name
 			String[] currentElementAddress = currentElement.getAddress().split("/");
 			Element element = xmlDocument.createElement(currentElementAddress[currentElementAddress.length - 1]);
 			
 			// set attributes
 			for (XMLAttribute attr : currentElement.getAttributes()) {
 				element.setAttribute(attr.getXmlName(), attr.getValue());
 			}
 			
 			// link each other
 			if (parent == null) {
 				xmlDocument.appendChild(element);
 			} else {
 				parent.appendChild(element);
 			}
 					
 			// enqueue child nodes
 			for (XMLElement temp : currentElement.getChildren()) {
 				queue.offer(new QueueTuple(element, temp));
 			}
 		}
 		
 		log.info("Actualization of configuration file ... OK");
 		
 		return xmlDocument;
 	}	
 	
 	public XMLDocument loadConfiguration() throws XMLException {
 
 		// check if a file is reachable
 		if (!confFile.exists() || !confFile.canRead()) {
 			throw new XMLException("File: " + confFile + "is unreachable!");
 		}
 
 		// check if a file is correct XML file
 		if (!isValid()) {
 			throw new XMLException("File: " + confFile + "isn't valid JBossESB configuration file");
 		}
 
 		return loadData(initializeDOMDocument(false));
 	}
 
 	public void saveConfiguration(XMLDocument document) throws XMLException {
 		
 		// input tests
 		if (document == null || document.getJbossesb() == null) {
 			throw new XMLException("Input data is null!");
 		} 
 		if (!confFile.exists() || !confFile.canWrite()) {
 			throw new XMLException("File: " + confFile + "is unreachable!");
 		}
 		if (!isValid()) {
 			throw new XMLException("File: " + confFile + "isn't valid JBossESB configuration file");
 		}
 		
 		// initialization 
 		Document xmlDocument = changeDataInConfigurationFile(document);
 		
 		// write data into configuration file
 		writeDataIntoConfigurationFile(xmlDocument);		
 	}
 }
