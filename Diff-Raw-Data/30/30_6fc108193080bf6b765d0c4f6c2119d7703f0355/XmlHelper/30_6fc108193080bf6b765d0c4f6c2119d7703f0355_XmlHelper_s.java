 package com.i18n.file.output;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 public class XmlHelper implements IOutputHelper{
 	private static final String RESOURCES = "resources";
 	private static final String STRING_KEY = "string";
 	private static final String STRING_ATTR = "name";
 	
 	private volatile boolean debug;
 	private final String outFilePath;
 	
 	
	private XmlHelper(String filepath, boolean debug) throws ParserConfigurationException, TransformerException {
 		this.outFilePath = filepath;
 		this.debug = debug;
 		
 		checkXmlFile(filepath);
 	}
	private boolean checkXmlFile(String filepath) throws ParserConfigurationException, TransformerException {
 		File file = new File(filepath);
 		if (file.exists()) {
 			return true;
 		} else {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			
 			// resources elements
 			Document doc = docBuilder.newDocument();
 			Element rootElement = doc.createElement(RESOURCES);
 			doc.appendChild(rootElement);
 			
 			// write the content into xml file
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(doc);
 			StreamResult result;
			result = new StreamResult(new File(outFilePath));	
 					 
 			transformer.transform(source, result);
 		}
 		return true;
 	}
 	
 	public void write(SimpleEntry<String, String> in) throws ParserConfigurationException, TransformerException, SAXException, IOException {
 		Document doc = parseDoc();
 		
 		Node resources = doc.getFirstChild();
 	
 		Element string = doc.createElement(STRING_KEY);
 		string.setAttribute(STRING_ATTR, in.getKey());
 		string.appendChild(doc.createTextNode(in.getValue()));
 		resources.appendChild(string);		
 		
 		// write the content into xml file
 		writeDoc(doc);
 		
 	}
 	
 	public void write(HashMap<String, String> entrys) throws ParserConfigurationException, SAXException, IOException, TransformerException {
 		Document doc = parseDoc();
 		Node resources = doc.getFirstChild();
 		
 		for (Map.Entry<String, String> entry : entrys.entrySet()) {
 			Element string = doc.createElement(STRING_KEY);
 			string.setAttribute(STRING_ATTR, entry.getKey());
 			string.appendChild(doc.createTextNode(entry.getValue()));
 			resources.appendChild(string);		
 		}
 		
 		writeDoc(doc);
 	}
 	
 	private void writeDoc(Document doc)
 			throws TransformerFactoryConfigurationError,TransformerConfigurationException, TransformerException {
 		TransformerFactory transformerFactory = TransformerFactory.newInstance();
 		Transformer transformer = transformerFactory.newTransformer();
 		DOMSource source = new DOMSource(doc);
 		StreamResult result;
 		if (debug) {
 			result = new StreamResult(System.out);			
 		} else {
 			result = new StreamResult(new File(outFilePath));	
 		}		
 		 
 		transformer.transform(source, result);
 	}
 	private Document parseDoc() throws ParserConfigurationException,
 			SAXException, IOException {
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 		
 		Document doc = docBuilder.parse(outFilePath);
 		return doc;
 	}
 
 	
 	public static class Builder {
 		private boolean debug;
 		private String filepath;
 		
 		public Builder setDebug(boolean debug) {
 			this.debug = debug;
 			return this;
 		}
 		
 		public Builder setFilePath(String path) {
 			this.filepath = path;
 			return this;
 					
 		}
 		
		public XmlHelper build() throws ParserConfigurationException, TransformerException {
 			ensureSaneDefaults();
 			return new XmlHelper(filepath, debug);
 		}
 		
 		private void ensureSaneDefaults() {
 			if (filepath == null) {
 				filepath = "./string.xml";
 			}
 		}
 	}
 }
