 package harris.GiantBomb;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class GBObject implements api {
 
 	public enum ObjectType {
 		GAME, FRANCHISE, CHARACTER, CONCEPT, OBJECT, LOCATION, PERSON, COMPANY
 	};
 
 	private ObjectType type;
 	private String name;
 	private String id;
 	private String description;
 	private String url;
 	private List<GBObject> related;
 	
 	public GBObject() {
 		setRelated(new ArrayList<GBObject>());
 	}
 
 	public ObjectType getType() {
 		return type;
 	}
 
 	public void setType(ObjectType type) {
 		this.type = type;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public void setRelated(List<GBObject> related) {
 		this.related = related;
 	}
 
 	public List<GBObject> getRelated() {
 		return related;
 	}
 	
 	public List<GBObject> getRelated(ObjectType type) {
 		List<GBObject> result = new ArrayList<GBObject>();
 		
 		for (GBObject o : related) {
 			if (o.getType() == type) {
 				result.add(o);
 			}
 		}
 		
 		return result;
 	}
 
 	public static GBObject getGBObject(String id, ObjectType type)
 			throws ParserConfigurationException, FactoryConfigurationError,
 			SAXException, IOException {
 		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
 				.newDocumentBuilder();
 		URL url = new URL(
 				"http://api.giantbomb.com/"
 						+ type.toString().toLowerCase()
 						+ "/"
 						+ id
 						+ "/?api_key="
 						+ API_KEY
 						+ "&field_list=id,name,description,site_detail_url,games,franchises,characters,concepts,objects,locations,people,companies&format=xml");
 		Document doc = docBuilder.parse(url.openConnection().getInputStream());
 
 		return parseObject((Element) doc.getElementsByTagName("results")
 				.item(0), type);
 	}
 
 	public static GBObject parseObject(Element el, ObjectType type) {
 		GBObject item = new GBObject();
 		
 		NodeList nodes = el.getChildNodes();
		try {
			for (int i = 0; i <= nodes.getLength(); i++) {
				String nodeName = nodes.item(i).getNodeName();
				
				if (nodeName.equals("name")) {
					item.setName(nodes.item(i).getFirstChild().getNodeValue());
				} else if (nodeName.equals("id")) {
					item.setId(nodes.item(i).getFirstChild().getNodeValue());
				} else if (nodeName.equals("description")) {
					item.setDescription(nodes.item(i).getFirstChild().getNodeValue());
				} else if (nodeName.equals("site_detail_url")) {
					item.setUrl(nodes.item(i).getFirstChild().getNodeValue());
				}
 			}
		} catch (Exception e) {}
 		
 //		item.setName(el.getElementsByTagName("name").item(0).getFirstChild()
 //				.getNodeValue());
 //		item.setId(el.getElementsByTagName("id").item(0).getFirstChild()
 //				.getNodeValue());
 //
 //		try {
 //			item.setDescription(el.getElementsByTagName("description").item(0)
 //					.getFirstChild().getNodeValue());
 //		} catch (Exception e) {
 //		}
 //		;
 //
 //		try {
 //			item.setUrl(el.getElementsByTagName("site_detail_url").item(0)
 //					.getFirstChild().getNodeValue());
 //		} catch (Exception e) {
 //		}
 //		;
 
 		for (ObjectType ot : ObjectType.values()) {
 			try {
 				NodeList nl = el.getElementsByTagName(ot.toString().toLowerCase());
 				for (int i = 0; i < (nl.getLength() < 5 ? nl.getLength() : 5); i++) {
 					Element el2 = (Element)nl.item(i);
 					GBObject ri = new GBObject();
 					ri.setName(el2.getElementsByTagName("name").item(0).getFirstChild()
 							.getNodeValue());
 					ri.setId(el2.getElementsByTagName("id").item(0).getFirstChild()
 							.getNodeValue());
 					ri.setType(ot);
 					if (item.getId() != ri.getId() && item.getType() != ri.getType()) {
 						item.getRelated().add(ri);
 					}
 				}
 			} catch (Exception e) {
 			}
 		}
 
 		item.setType(type);
 
 		return item;
 	}
 
 	public GBObject clone() {
 		GBObject obj = new GBObject();
 		obj.setId(this.id);
 		obj.setName(this.name);
 		obj.setType(this.type);
 		obj.setUrl(this.url);
 		obj.setDescription(this.description);
 		obj.setRelated(this.related);
 
 		return obj;
 	}
 }
