 /**
  * 
  */
 package analytics.analyzer.handlers;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Set;
 import java.util.Stack;
 import java.util.Vector;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import analytics.constants.AnalyticsConstants;
 
 /**
  * @author vogias
  * 
  */
 public class XMLHandler extends DefaultHandler {
 
 	String tmpValue;
 	Repository repositoryHandler;
 	HashMap<String, Integer> dimensionalityMap;
 	Vector<String> elements;
 	Properties props;
 	AnalyticsConstants constants;
 	String branche;
 	String elPath;
 	Stack<String> xPaths;
 
 	public XMLHandler(Repository repositoryHandler) {
 		// TODO Auto-generated constructor stub
 		this.repositoryHandler = repositoryHandler;
 		elements = new Vector<>();
 		constants = new AnalyticsConstants();
 		props = new Properties();
 		branche = "";
 		elPath = "";
 		xPaths = new Stack<>();
 		try {
 			props.load(new FileInputStream("configure.properties"));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void startElement(String uri, String localName, String qName,
 			Attributes attributes) throws SAXException {
 
 		branche += qName.toLowerCase();
 		xPaths.push(branche);
 
 		for (int i = 0; i < attributes.getLength(); i++) {
 
 			String name = attributes.getLocalName(i);
 			String value = attributes.getValue(i);
 			if (!name.contains("xsi:schemaLocation") && !name.contains("xmlns")) {
 				// System.out.println("Name:" + attributes.getLocalName(i)
 				// + " Value:" + attributes.getValue(i));
 				HashMap<String, String> elmt = new HashMap<>();
 				elmt.put(branche, attributes.getValue(i));
 
 				repositoryHandler.addAttributes(attributes.getQName(i), elmt);
 			} else if (name.contains("xmlns")) {
 				repositoryHandler.setSchema(attributes.getValue(i));
 			}
 		}
 
 		branche += ".";
 
 	}
 
 	public void endElement(String uri, String localName, String qName)
 			throws SAXException {
 
 		String elmt = "";
 		qName = qName.toLowerCase();
 
 		if (branche.endsWith(qName + "" + ".")) {
 			branche = branche.substring(0, branche.length() - qName.length()
 					- 1);
 			// branche = branche.toLowerCase();
 			elmt = xPaths.elementAt(xPaths.size() - 1);
 			xPaths.removeElementAt(xPaths.size() - 1);
 			// System.out.println("--------End element-----");
 			// System.out.println(elmt);
 		}
 
 		repositoryHandler.addxmlElements(elmt);
 
 		if (!dimensionalityMap.containsKey(elmt)) {
 			dimensionalityMap.put(elmt, 1);
 		} else {
 
 			dimensionalityMap.put(elmt, dimensionalityMap.get(elmt) + 1);
 		}
 
 		if (!elements.contains(elmt)) {
 
 			elements.addElement(elmt);
 			repositoryHandler.addCompletenessElement(elmt);
 		}
 
 		// entropy calculation
 		try {
 			repositoryHandler.addEvalue2File(elmt, tmpValue);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void startDocument() {
 
 		dimensionalityMap = new HashMap<>();
 
 	}
 
 	public void endDocument() {
 
 		Set<String> keySet = dimensionalityMap.keySet();
 		Iterator<String> iterator = keySet.iterator();
 		while (iterator.hasNext()) {
 			String key = iterator.next();
 			Integer value = dimensionalityMap.get(key);
 
 			repositoryHandler.addDimensionsElement(key, value);
 		}
 
 	}
 
 	public void characters(char ch[], int start, int length)
 			throws SAXException {
 
 		tmpValue = new String(ch, start, length);
 
 	}
 
 	public void parseDocument(InputStream is) throws SAXException, IOException,
 			ParserConfigurationException {
 		// TODO Auto-generated method stub
 		SAXParserFactory spf = SAXParserFactory.newInstance();
 
		Reader reader = new InputStreamReader(is,"UTF-8");
		 
 		InputSource inputStream = new InputSource(reader);
 		inputStream.setEncoding("UTF-8");
		
		
 		SAXParser parser = spf.newSAXParser();
 
 		parser.parse(inputStream, this);
		
 
 	}
 }
