 package xml;
 
 import java.io.File;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
  
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class XmlWriter 
 {
 
 	DocumentBuilderFactory docFactory = null;
 	DocumentBuilder docBuilder = null;
 	Document doc = null;
 	Element rootElement = null;
 	Element xmlEntry = null;
 	
 	public void StartXMLEntry()
 	{
 		try
 		{
 			docFactory = DocumentBuilderFactory.newInstance();
 			docBuilder = docFactory.newDocumentBuilder();
 			doc = docBuilder.newDocument();
 		}
 		catch (ParserConfigurationException pce) 
 		{
 			pce.printStackTrace();
 		} 
 		rootElement = doc.createElement("TestSuite");
 		doc.appendChild(rootElement);
 		
 		Element startTest = doc.createElement("NewTestSuite");
 		rootElement.appendChild(startTest);
 	}
 	
	public void AddXMLTest(String testName, long lengthSetup, long lengthTest, long lengthTearDown)
 	{
 		xmlEntry = doc.createElement("testName");
 		rootElement.appendChild(xmlEntry);
 		Attr attr = doc.createAttribute("class");
 		attr.setValue(testName);
 		xmlEntry.setAttributeNode(attr);
 	 
 		Element setupEntry = doc.createElement("setup");
 		setupEntry.appendChild(doc.createTextNode(String.valueOf(lengthSetup)));
 		xmlEntry.appendChild(setupEntry);
 	 
 		Element testEntry = doc.createElement("test");
 		testEntry.appendChild(doc.createTextNode(String.valueOf(lengthTest)));
 		xmlEntry.appendChild(testEntry);
 	 
 		Element tearDownEntry = doc.createElement("tearDown");
 		tearDownEntry.appendChild(doc.createTextNode(String.valueOf(lengthTearDown)));
 		xmlEntry.appendChild(tearDownEntry);
 	}
 	
 	public void FinishXMLEntry(Long endTestSuiteTime)
 	{
 
 		xmlEntry = doc.createElement("TestSuiteTotalTime");
 		rootElement.appendChild(xmlEntry);
 		Attr attr = doc.createAttribute("TestSuiteTotalTime");
 		attr.setValue(String.valueOf(endTestSuiteTime));
 		xmlEntry.setAttributeNode(attr);
 		
 		TransformerFactory transformerFactory = null;
 		Transformer transformer = null;
 		DOMSource source = new DOMSource(doc);
 		StreamResult result = null;
 	
 		try
 		{
 			// write the content into xml file
 			transformerFactory = TransformerFactory.newInstance();
 			transformer = transformerFactory.newTransformer();
 			result = new StreamResult(new File("rundata.xml"));
 
 			// Output to console for testing
 			StreamResult result2 = new StreamResult(System.out);
 	
 			transformer.transform(source, result);
 			transformer.transform(source, result2);
 		}
 		catch (TransformerException tfe) 
 		{
 			tfe.printStackTrace();
 		}
 		System.out.println("File saved!");
 	}
 }
