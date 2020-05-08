 package models;
 
 import java.io.File;
 import java.io.StringWriter;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 
 import play.Logger;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 
 @Entity
 public class Display extends Model{
 
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	public Long id;
 
 	@Required
 	public String name;
 
 	public Integer width;
 
 	public Integer height;
 	
 	public Float latitude;
 	
 	public Float longitude;
 
 	public Long currentLayoutID;
 
 
 	public static Finder<Long,Display> find = new Finder<Long, Display>(Long.class, Display.class);
 
 
 	// Returns a list of all the displays available
 	public static List<Display> all() {
 		return find.all();
 	}
 
 	// Insert a new display inside the database
 	public static void addNew(Display display) {
 		display.save();
 		createXMLfile(Display.all());
 	}
 
 	//	Deletes a display with a given id
 	public static void delete(Long id) {
 		find.ref(id).delete();
 		createXMLfile(Display.all());
 	}
 
 	public static Display get(Long id){
 		return find.ref(id);
 	}
 
 	public static void createXMLfile(List<Display> displays){
 		Logger.info("DISPLAY: generating XML files for displays...");
 		try {
 			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 			Document doc = docBuilder.newDocument();
 			//create the root element and add it to the document
 			Element root = doc.createElement("root");
 			doc.appendChild(root);
 
 
 			Iterator<Display> it = displays.iterator();
 			while(it.hasNext()){
 				Display currentDisplay = it.next();
 				//create child element display and add it to the root
 				Element displayElement = doc.createElement("display");
 				root.appendChild(displayElement);
 				
 				createNodeInDisplay(doc, displayElement, "id", Long.toString(currentDisplay.id));
 				createNodeInDisplay(doc, displayElement, "name", currentDisplay.name);
 				createNodeInDisplay(doc, displayElement, "width", Integer.toString(currentDisplay.width));
 				createNodeInDisplay(doc, displayElement, "height", Integer.toString(currentDisplay.height));
 				createNodeInDisplay(doc, displayElement, "latitude", Float.toString(currentDisplay.latitude));
 				createNodeInDisplay(doc, displayElement, "longitude", Float.toString(currentDisplay.longitude));
 
 				
 			}
 
 			TransformerFactory transfac = TransformerFactory.newInstance();
 			Transformer trans = transfac.newTransformer();
 			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
 			trans.setOutputProperty(OutputKeys.INDENT, "yes");
 			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 
 
 			printXML(doc, trans);
 			
 	        Source source = new DOMSource(doc);
 	        File file = new File("public/displays/list.xml");
 	        Result result = new StreamResult(file);
 	        trans.transform(source, result);
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void createNodeInDisplay(Document doc, Element displayElement, String elementName, String elementValue) {
 		Element displayName = doc.createElement(elementName);
 		Text nameText = doc.createTextNode(elementValue);
 		displayName.appendChild(nameText);
 		displayElement.appendChild(displayName);
 	}
 
 	public static void printXML(Document doc, Transformer trans)
 			throws TransformerException {
 		//create string from xml tree
 		StringWriter sw = new StringWriter();
 		StreamResult result = new StreamResult(sw);
 		DOMSource source = new DOMSource(doc);
 		trans.transform(source, result);
 		String xmlString = sw.toString();
 
 		//print xml
 		Logger.info("DISPLAY - Generated XML:\n\n" + xmlString);
 	}
 }
 
