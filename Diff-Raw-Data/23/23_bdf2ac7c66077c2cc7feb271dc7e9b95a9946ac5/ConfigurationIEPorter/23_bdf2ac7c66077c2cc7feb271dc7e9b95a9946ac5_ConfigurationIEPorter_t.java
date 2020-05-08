 package org.paxle.tools.ieporter.cm.impl;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Vector;
 import java.util.Map.Entry;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.commons.io.IOUtils;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.paxle.tools.ieporter.cm.IConfigurationIEPorter;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class ConfigurationIEPorter implements IConfigurationIEPorter {
 	private static final String ELEM_VALUE = "value";
 	private static final String ELEM_VALUES = "values";
 	private static final String ELEM_PROPERTY = "property";
 	private static final String ELEM_PROPERTIES = "properties";
 	private static final String ELEM_CONFIG = "config";
 	private static final String ATTRIB_PROPERTY_TYPE = "type";
 	private static final String ATTRIBUTE_PROPERTY_KEY = "key";
 	
 	/**
 	 * All {@link Class classes} that are supported by the {@link ConfigurationAdmin} service.
 	 */
	private static final HashSet<Class<?>> SUPPORTED_CLASSES =  new HashSet<Class<?>>(Arrays.asList(new Class<?>[]{
 			String.class,
 			Long.class,
 			Integer.class,
 			Short.class,
 			Character.class,
 			Byte.class,
 			Double.class,
 			Float.class,
 			Boolean.class
 	}));
 	
	private static final HashMap<String, Class<?>> WRAPPERS =  new HashMap<String, Class<?>>();
	private static final HashMap<String, Class<?>> NAMELOOKUP = new HashMap<String, Class<?>>();
 	static {
		for (Class<?> clazz : SUPPORTED_CLASSES) {
 			try {
 				// add wrapper class
 				NAMELOOKUP.put(clazz.getSimpleName(), clazz);
 				if (clazz.equals(String.class)) {
 					WRAPPERS.put(String.class.getSimpleName(), String.class);
 					continue;
 				}
 
 				// add primitive type
 				Field primitiveTypeField = clazz.getDeclaredField("TYPE");
				Class<?> primitiveType = (Class<?>) primitiveTypeField.get(null);
 
 				NAMELOOKUP.put(primitiveType.getSimpleName(), primitiveType);
 				WRAPPERS.put(primitiveType.getSimpleName(), clazz);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private BundleContext context;
 	
 	public ConfigurationIEPorter(BundleContext context) {
 		this.context = context;
 	}
 	
 	private Document createNewXMLDocument(String rootElementName, String namespaceURI) throws ParserConfigurationException {
 		// creating a new document builder factory
 		DocumentBuilderFactory newDocBuilderFactory = DocumentBuilderFactory.newInstance();
 
 		// creating a new document builder
 		DocumentBuilder newDocBuilder = newDocBuilderFactory.newDocumentBuilder();
 
 		// creating a new xml document
 		Document newXMLDocument = newDocBuilder.newDocument();
 
 		if (rootElementName != null) {
 			// creating the xml root document
			Element rootElement = (namespaceURI != null) 
 							    ? newXMLDocument.createElementNS(namespaceURI, rootElementName)
 							    : newXMLDocument.createElement(rootElementName);
 			newXMLDocument.appendChild(rootElement);
 		}
 
 		return newXMLDocument;
 	}
 	
 	/**
 	 * Read an XML document from {@link File}
 	 * @param xmlFile
 	 * @return
 	 */
 	private Document readXMLDocument(File xmlFile) throws Exception {
 		// creating a new document builder factory
 		DocumentBuilderFactory newDocBuilderFactory = DocumentBuilderFactory.newInstance();
 
 		// creating a new document builder
 		DocumentBuilder newDocBuilder = newDocBuilderFactory.newDocumentBuilder();
 		
 		// parse document
 		return newDocBuilder.parse(xmlFile);
 	}
 	
 	private Document readXMLStream(InputStream input) throws Exception {
 		// creating a new document builder factory
 		DocumentBuilderFactory newDocBuilderFactory = DocumentBuilderFactory.newInstance();
 
 		// creating a new document builder
 		DocumentBuilder newDocBuilder = newDocBuilderFactory.newDocumentBuilder();
 		
 		// parse document
 		return newDocBuilder.parse(input);
 	}
 	
 	public Map<String, Dictionary<String, Object>> importConfigurations(File file) throws Exception {
 		BufferedInputStream input = null;
 		Map<String, Dictionary<String, Object>> configs = new HashMap<String, Dictionary<String,Object>>();
 		try {
 			input = new BufferedInputStream(new FileInputStream(file),5);
 			
 			// pre-read data to detect file type
 			byte[] test = new byte[5];
 			input.mark(5);
 			input.read(test);
 			input.reset();			
 
 			if (new String(test,"UTF-8").equals("<?xml")) {
 				// XML Document found
 				Document doc = this.readXMLDocument(file);
 				Map<String, Dictionary<String, Object>> config = this.importConfigurations(doc);
 				configs.putAll(config);
 			} else if (new String(test,0,2).equals("PK")) {
 				// open zip file
 				final ZipInputStream zis = new ZipInputStream(input);
 				
 				// loop through entries
 				ZipEntry ze;
 				while ((ze = zis.getNextEntry()) != null) {
 					// skip directories
 					if (ze.isDirectory()) continue;
 					
 					// read data into memory
 					long size = ze.getSize();
 					ByteArrayOutputStream bout = (size < 0) ? new ByteArrayOutputStream() : new ByteArrayOutputStream((int)size);
 					IOUtils.copy(zis, bout);
 					bout.close();
 					
 					// read XML
 					ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
 					Document doc = this.readXMLStream(bin);
 					bin.close();
 					
 					// parser configuration
 					Map<String, Dictionary<String, Object>> config = this.importConfigurations(doc);
 					configs.putAll(config);					
 				}
 				
 				zis.close();
 			} else {
 				// Unknown file
 				throw new IllegalArgumentException("Unknown file type");
 			}
 		} finally {
 			if (input != null) try { input.close(); } catch(Exception e) {/* ignore this */}
 		}
 		
 		return configs;
 	}
 	
 	public Map<String, Dictionary<String, Object>> importConfigurations(Document doc) {
 		return this.importConfigurations(new Document[]{doc});
 	}
 	
 	public Map<String, Dictionary<String, Object>> importConfigurations(Document[] docs) {
 		if (docs == null) return null;
 		
 		Map<String, Dictionary<String, Object>> propsMap = new HashMap<String, Dictionary<String,Object>>();
 		
 		// loap through docs
 		for (Document doc : docs) {
 			Dictionary<String, Object> props = this.importConfiguration(doc);
 			String servicePid = (String) props.get(Constants.SERVICE_PID);
 			
 			if (servicePid != null) {
 				propsMap.put(servicePid, props);
 			}
 		}
 		
 		return propsMap;
 	}
 	
 	public File exportConfigsAsZip(Map<String, String> pidBundleLocationTupel) throws Exception {
 		// convert configs to doc
 		Map<String, Document> docs = this.exportConfigsAsDoc(pidBundleLocationTupel);
 		
 		// write them into a zip file
 		File tempFile = null;
 		if (docs != null) {
 			tempFile = File.createTempFile("configExport", ".zip");
 			tempFile.deleteOnExit();
 			
 			FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
 			ZipOutputStream zos = new ZipOutputStream(fileOutputStream);
 			
 			for (Entry<String, Document> doc : docs.entrySet()) {
 				ZipEntry ze= new ZipEntry(doc.getKey() + ".xml");
 				zos.putNextEntry(ze);
 				this.writeToOut(doc.getValue(), zos);
 				zos.closeEntry();
 			}
 			
 			zos.close();
 		}
 		
 		return tempFile;
 	}
 	
 	public Map<String, Document> exportConfigsAsDoc(Map<String, String> pidBundleLocationTupel) throws Exception {
 		if (pidBundleLocationTupel == null) return null;
 		
 		// getting the configuration-admin service
 		ServiceReference ref = this.context.getServiceReference(ConfigurationAdmin.class.getName());
 		if (ref == null) return null;
 		
 		// getting the configuration-admin service
 		ConfigurationAdmin cm = (ConfigurationAdmin) this.context.getService(ref);
 		if (cm == null) return null;		
 		
 		// build result structure
 		HashMap<String, Document> docs = new HashMap<String, Document>();
 		for (Entry<String, String> entry : pidBundleLocationTupel.entrySet()) {
 			String pid = entry.getKey();		
 			String bundleLocation = entry.getValue();
 			
 			// getting the configuration
 			Configuration config = cm.getConfiguration(pid, bundleLocation);
 			if (config != null) {
 				Map<String, Document> doc = this.exportConfiguration(config);
 				docs.putAll(doc);
 			}
 		}
 
 		
 		return docs;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Dictionary<String, Object> importConfiguration(Document doc) {
 		if (doc == null) return null;
 		
 		Dictionary<String, Object> props = new Hashtable<String, Object>();
 		try {
 			Element configElement = doc.getDocumentElement();
 			
 			// getting the service-pid
 			Element pidElement = (Element) configElement.getElementsByTagName(Constants.SERVICE_PID).item(0);
 			props.put(Constants.SERVICE_PID, pidElement.getFirstChild().getNodeValue());
 			
 			// loop through all properties
 			Element propsElement = (Element) configElement.getElementsByTagName(ELEM_PROPERTIES).item(0);			
 			NodeList propsList = propsElement.getElementsByTagName(ELEM_PROPERTY);
 			
 			for (int i=0; i<propsList.getLength();i++) {
 				Element propertyElement = (Element) propsList.item(i);
 				
 				Object value = null;
 				String key = propertyElement.getAttributes().getNamedItem(ATTRIBUTE_PROPERTY_KEY).getNodeValue();
 				String type = propertyElement.getAttributes().getNamedItem(ATTRIB_PROPERTY_TYPE).getNodeValue();
 				
 				if (type.endsWith("[]") || type.equals(Vector.class.getSimpleName())) {
 					Element valueElements = (Element) propertyElement.getElementsByTagName(ELEM_VALUES).item(0);
 					NodeList valueElementList = valueElements.getElementsByTagName(ELEM_VALUE);
 
 					if (type.endsWith("[]")) {
 						// get type class
 						Class clazz = NAMELOOKUP.get(type.substring(0, type.length()-2));
 						
 						// create a new array
 						value = Array.newInstance(clazz, valueElementList.getLength());
 					} else {
 						// create a new vector
 						value = new Vector();
 					}
 					
 					// append all values to the array/vector
 					for (int j=0; j < valueElementList.getLength(); j++) {
 						Element valueElement = (Element) valueElementList.item(j);	
 						Object valueObj = this.valueOf(type, valueElement);
 						
 						if (type.endsWith("[]")) {
 							Array.set(value, j, valueObj);
 						} else {
 							((Vector<Object>)value).add(valueObj);
 						}
 					}
 				} else {
 					Element valueElement = (Element) propertyElement.getElementsByTagName(ELEM_VALUE).item(0);
 					
 					// get concrete value
 					value = this.valueOf(type, valueElement);		
 				}
 				
 				// add value
 				props.put(key, value);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return props;
 	}
 	
 	Object valueOf(String type, Element valueElement) throws SecurityException, NoSuchMethodException, IllegalArgumentException, DOMException, IllegalAccessException, InvocationTargetException {
		Class<?> clazz = null;
 		if (type.endsWith("[]")) {
 			clazz = WRAPPERS.get(type.substring(0, type.length()-2));
 		} else {
 			clazz = NAMELOOKUP.get(type);
 		}
 		
 		String valueElementString = valueElement.getFirstChild() == null 
 								  ? "" 
 								  : valueElement.getFirstChild().getNodeValue();
 		
 		// get parser-method
 		Method valueOf = null;
 		try {
 			valueOf = clazz.getMethod("valueOf", String.class);
 		} catch (NoSuchMethodException e) {
 			valueOf = clazz.getMethod("valueOf", Object.class);
 		}
 		
 		// get concrete value
 		return valueOf.invoke(null, valueElementString);		
 	}
 	
 	@SuppressWarnings("unchecked")
 	Map<String, Document> exportConfiguration(Configuration config) throws ParserConfigurationException {
 		if (config == null) return Collections.emptyMap();
 		else if (config.getPid() == null) return Collections.emptyMap();
 		else if (config.getProperties() == null) return Collections.emptyMap();
 		
 		// create a new XML document
 		Document doc = this.createNewXMLDocument(ELEM_CONFIG, "http://www.paxle.net/config");
 		Element root = doc.getDocumentElement();
 		
 		// adding the pid
 		Element pidElem = doc.createElement(Constants.SERVICE_PID);
 		pidElem.appendChild(doc.createTextNode(config.getPid()));
 		root.appendChild(pidElem);		
 		
 		// <properties>
 		Element propsElem = doc.createElement(ELEM_PROPERTIES);
 		root.appendChild(propsElem);
 		
 		// loop through all parameters
 		Dictionary<String, Object> props = config.getProperties();
 		Enumeration<String> keys = props.keys();
 		while (keys.hasMoreElements()) {
 			final String key = keys.nextElement();
 			final Object value = props.get(key);
 			if (key.equalsIgnoreCase(Constants.SERVICE_PID)) continue;
 			if (value == null) continue;  // TODO we should set nil instead
 			
 			// <property key="xxx" type="xxx">
 			Element propertyElement = doc.createElement(ELEM_PROPERTY);
 			propertyElement.setAttribute(ATTRIBUTE_PROPERTY_KEY, key);
 			propertyElement.setAttribute(ATTRIB_PROPERTY_TYPE, value.getClass().getSimpleName());
 			propsElem.appendChild(propertyElement);			
 			
 			if (value.getClass().isArray() || value instanceof Vector)	{
 				Element valuesElem = doc.createElement(ELEM_VALUES);
 				propertyElement.appendChild(valuesElem);
 				
 				// <values>
 				int length = (value.getClass().isArray()) ? Array.getLength(value) : ((Vector)value).size();
 				for (int i=0; i < length; i++) {
 					// <value>
 					Object valueItem = (value.getClass().isArray()) ? Array.get(value, i) :((Vector)value).get(i);
 					Element valueElem = doc.createElement(ELEM_VALUE);
 					valueElem.appendChild(doc.createTextNode(valueItem.toString()));
 					valuesElem.appendChild(valueElem);
 					// </value>
 				}
 				// </values>
 			} else {
 				// <value>
 				Element valueElem = doc.createElement(ELEM_VALUE);
 				valueElem.appendChild(doc.createTextNode(value.toString()));
 				propertyElement.appendChild(valueElem);
 				// </value>
 			}
 			// </property>
 		}
 		// </properties>
 		
 		HashMap<String, Document> result = new HashMap<String, Document>();
 		result.put(config.getPid(), doc);
 		return result;
 	}
 	
 	private void writeToOut(Document doc, OutputStream out) throws TransformerFactoryConfigurationError, TransformerException {
         // Prepare the DOM document for writing
         Source source = new DOMSource(doc);
 
         // Prepare the output file
         Result result = new StreamResult(out);
 
         // Write the DOM document to the file
         Transformer xformer = TransformerFactory.newInstance().newTransformer();
         xformer.transform(source, result);
 	}
 }
