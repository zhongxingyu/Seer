 import java.io.File;
 import java.lang.String;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class WriteXML {
 
 	public static void main(String argv[]) {
 		// testing
 		String machineName = "test";
 		String nodeName = "q0";
 		String transition = "q1";
 		String read[] = {"a","b","c"};
		String write = "q0";
 		
 		writeXMLtoFile("test.xml", machineName, nodeName, transition, read, write);
 	}
 	
 	private static void writeXMLtoFile(String fileName, String machineName, String nodeName, String transition, String read[], String write) {
 		try {
 			try {
 				TransformerFactory transformerFactory = TransformerFactory.newInstance();
 				
 				Transformer transformer = transformerFactory.newTransformer();
 				transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
 				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
 				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
 				
 				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 				Document doc = docBuilder.newDocument();
 				DOMSource source = new DOMSource(doc);
 				StreamResult result = new StreamResult(new File(fileName));
 				String readString = "";
 		
 				// create root element
 				Element rootElement = doc.createElement("machine");
 				doc.appendChild(rootElement);
 		
 				// save name of machine
 				Attr attr = doc.createAttribute("name");
 				attr.setValue(machineName);
 				rootElement.setAttributeNode(attr);
 		
 				// node element
 				Element node = doc.createElement("node");
 				rootElement.appendChild(node);
 		
 				// node name element
 				Element nameElement = doc.createElement("name");
 				nameElement.appendChild(doc.createTextNode(nodeName));
 				node.appendChild(nameElement);
 		
 				// node transition element
 				Element transitionElement = doc.createElement("transition");
 				transitionElement.appendChild(doc.createTextNode(transition));
 				node.appendChild(transitionElement);
 		
 				// node read element
 				if (read.length > 0) {
 					for (int i = 0; i < read.length; i++) {
 						readString = readString + read[i];
 						if (i < read.length-1) {
 							readString = readString + ",";
 						}
 					}
 					Element readElement = doc.createElement("read");
 					readElement.appendChild(doc.createTextNode(readString));
 					node.appendChild(readElement);
 			
 					// node write element
 					Element writeElement = doc.createElement("write");
 					writeElement.appendChild(doc.createTextNode(write));
 					node.appendChild(writeElement);
 				}
 			
 				// write the content into xml file
 			
 				transformer.transform(source, result);
 			}
 			catch (ParserConfigurationException pce) {
 				pce.printStackTrace();
 			}
 		}
 		catch (TransformerException tfe) {
 			tfe.printStackTrace();
 		}
 	} 
 
 }
