 package utils;
 
 import java.io.InputStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 
 public class Configuration {
 
 	private Element docEle;	// the document element of the Config.xml file
 	
 	public final String PARSING = "Parsing";
 	public final String DB = "DB";
 
 	// constructor
 	public Configuration(){
 		//open the Config.xml file
 		InputStream stream = this.getClass().getResourceAsStream("/Config.xml");
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		try {
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc;
 			//getting the document itself
 			doc = dBuilder.parse(stream);
 			doc.getDocumentElement().normalize();
 			//get the document element
 			this.docEle = doc.getDocumentElement();		
 		}
 		catch (Exception e) {				
 			e.printStackTrace();
 		}	
 	}
 
 	/** get the path of the file yagoSimpleTypes.ttl */
 	public String getYagoSimpleTypes(){		
 		return getConfig(PARSING, "YagoSimpleTypesFilePath");
 	}
 
 	/** get the path of the file yagoFacts.ttl */
 	public String getYagoFacts(){
 		return getConfig(PARSING, "YagoFactsFilePath");
 	}
 
 	/** get the path of the file yagoLiteralFacts.ttl */
 	public String getYagoLiteralFacts(){
 		return getConfig(PARSING, "YagoLiteralFactsFilePath");
 	}
 	
 	/** get the path of the file yagoWikipediaInfo.ttl */
 	public String getYagoWikipediaInfo(){
 		return getConfig(PARSING, "YagoWikipediaInfoFilePath");
 	}
 	
 	/** get the path of the file genres.list provided by IMDB */
 	public String getImdbGenres(){
 		return getConfig(PARSING, "IMDBgenreListFilePath");
 	}
 	
 	/** get the path of the file plot.list provided by IMDB */
 	public String getImdbPlot(){
 		return getConfig(PARSING, "IMDBplotListFilePath");
 	}
 	
 	
 	/** returns the value of the element configElem, child of packageName element */
 	private String getConfig(String packageName, String configElem){

 		// get the element of the packageName provided (child of root element)
 		Node node = docEle.getElementsByTagName(packageName).item(0);
 
 		if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
 			Element e = (Element) node;
 			
 			//get the wanted element configuration (child of the "package" element)
 			NodeList nodeList = e.getElementsByTagName(configElem);
 			
 			//return the value 
 			return nodeList.item(0).getChildNodes().item(0).getNodeValue();
 		}
 		
 		return null;
 	}
 
 }
