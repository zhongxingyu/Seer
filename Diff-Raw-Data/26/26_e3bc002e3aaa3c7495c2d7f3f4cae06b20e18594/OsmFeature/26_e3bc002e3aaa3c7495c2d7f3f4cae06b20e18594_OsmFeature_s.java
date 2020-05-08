 package models.osm;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.sql.DataSource;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public abstract class OsmFeature {
 
 	protected final static double TOLERANCE = 0.001; // Tolerance for symplyfing geometries when inserting to PostGIS
 
 	protected DataSource ds;
 
 	protected long id; 				// ID at OSM, negative if it is created by us
 	protected int version;          // Elements version
 	protected String user;          // Users name
 	protected String uid;           // Users id
 	protected Date timeStamp;       // Elements timestamp
 	protected LinkedHashMap<String,String> tags; // Elements tags
 
 
 	/**
 	 * !!!!!!!!! SETTERS ONLY THROUGH JSON CONSTRUCTOR AND UPDATER TO ENSURE INTEGRITY !!!!!!!!!!! *
 	 */
 	public long getId() {
 		return id;
 	}
 
 	public int getVesion() {
 		return version;
 	}
 
     public void setVersion(int version) {
         this.version = version;
     }
 
 	public Map<String,String> getTags() {
 		return tags;
 	}
 	/**
 	 * !!!!!!!!! SETTERS ONLY THROUGH JSON CONSTRUCTOR AND UPDATER TO ENSURE INTEGRITY !!!!!!!!!!! *
 	 */
 
 	public abstract OsmFeature save();
 
 	public abstract void delete();
 
 	public abstract JsonNode getGeometry();
 
 	public abstract void setGeometry(JsonNode geometry);
 
 	public abstract ObjectNode toOsmJson();
 
 	public abstract Document toOsmXml() throws ParserConfigurationException;
 
 	public static Document osmXMLParser(String osmXML) throws ParserConfigurationException, SAXException, IOException{
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
 		DocumentBuilder builder = factory.newDocumentBuilder();
 		return builder.parse(osmXML);
 	}
 
 	public static String tagsToHstoreFormat(LinkedHashMap<String,String> tags){
 
 		/* EXAMPLE:
 		 * hstore(array['author','date','stock'],array['Mike','Nov 2012','200'])
 		 */
 
 		StringBuilder result = new StringBuilder("hstore(array[");
 		boolean first = true;
 
 		if (tags != null && tags.size() > 0)
 			for(String key : tags.keySet()){
 				if (first){
 					result.append("'" + key + "'");
 					first = false;
 				} else {
 					result.append(",'" + key + "'");
 				}
 			}
 
 		first = true;
 		result.append("],array[");
 
 		if (tags != null && tags.size() > 0)
 			for (String value : tags.values()){
 				if (first){
 					result.append("'" + value + "'");
 					first = false;
 				} else {
 					result.append(",'" + value + "'");
 				}
 			}
 
 		result.append("])");
 		return result.toString();
 	}
 
 	public static LinkedHashMap<String,String> hstoreFormatToTags(String hstore){
 
 		LinkedHashMap<String,String> tags = new LinkedHashMap<String,String>();
 
 		if(hstore == null){
 			return tags;
 		}
 
 		String[] tagPairs = hstore.split(",");
 
 		for (String tagKV : tagPairs){
 			String[] KV = tagKV.split("=>");
 			tags.put(KV[0].replace("\"", "").trim(), KV[1].replace("\"", "").trim());
 		}
 
 		return tags;
 	}
 
 	protected void setTags(NodeList xmlTags){
 		if (xmlTags != null) {
 
 			for (int y = 0; y < xmlTags.getLength(); y++){
 
 				Node tag = xmlTags.item(y);
 				if (tag.getNodeType() == Node.ELEMENT_NODE)
 				{
 					Element tagElement = (Element) tag;
 					if (tags == null)
 						tags = new LinkedHashMap<String, String>();
 
 					tags.put(tagElement.getAttribute("k"), tagElement.getAttribute("v"));
 				}
 			}
 		}
 	}
 
 	public static List<ObjectNode> toObjectNodes(List<OsmFeature> list) {
 		List<ObjectNode> features = new ArrayList<ObjectNode>();
 		for (OsmFeature f : list)
 			features.add(f.toOsmJson());
 		return features;
 	}
 
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + (int) (id ^ (id >>> 32));
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		OsmFeature other = (OsmFeature) obj;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 
 }
