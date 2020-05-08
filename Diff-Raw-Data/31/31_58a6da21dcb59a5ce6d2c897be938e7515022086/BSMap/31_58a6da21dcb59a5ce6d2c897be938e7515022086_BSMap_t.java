 package model;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.security.NoSuchAlgorithmException;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.SequenceGenerator;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import common.BSUtil;
 
 /**
  * Representation of map information to be sent over network between Worker and Server
  * @author stevearc
  *
  */
 @Entity
 public class BSMap implements Serializable {
 	private static final long serialVersionUID = -3033262234181516847L;
	public static enum SIZE {SMALL, MEDIUM, LARGE}
 	private Long id;
 	private String mapName;
 	private Long height;
 	private Long width;
 	private Long rounds;
 	private String hash;
 	private Boolean invisible;
	private SIZE size;
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO, generator="map_id_gen")
 	@SequenceGenerator(name="map_id_gen", sequenceName="MAP_ID_GEN")
 	public Long getId() {
 		return id;
 	}
 
 	@Column(nullable=false,updatable=false,unique=true)
 	public String getMapName() {
 		return mapName;
 	}
 
 	@Column(nullable=false,updatable=false)
 	public Long getHeight() {
 		return height;
 	}
 
 	@Column(nullable=false,updatable=false)
 	public Long getWidth() {
 		return width;
 	}
 
 	@Column(nullable=false,updatable=false)
 	public Long getRounds() {
 		return rounds;
 	}
 	
 	@Column(nullable=false)
 	public String getHash() {
 		return hash;
 	}
 	
 	@Column(nullable=false, columnDefinition="boolean default false")
 	public Boolean getInvisible() {
 		return invisible;
 	}
 	
	@Column(nullable=false)
	@Deprecated
	public SIZE getSize() {
		return size;
	}
	
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public void setMapName(String mapName) {
 		this.mapName = mapName;
 	}
 
 	public void setHeight(Long height) {
 		this.height = height;
 	}
 
 	public void setWidth(Long width) {
 		this.width = width;
 	}
 
 	public void setRounds(Long rounds) {
 		this.rounds = rounds;
 	}
 	
 	public void setHash(String hash) {
 		this.hash = hash;
 	}
 	
 	public void setInvisible(Boolean invisible) {
 		this.invisible = invisible;
 	}
 	
	@Deprecated
	public void setSize(SIZE size) {
		this.size = size;
	}
	
 	public BSMap() {
 		
 	}
 	
 	/**
 	 * Reads file and parses out necessary information
 	 * @param map
 	 * @throws ParserConfigurationException
 	 * @throws SAXException
 	 * @throws IOException
 	 * @throws NoSuchAlgorithmException 
 	 */
 	public BSMap(File map) throws ParserConfigurationException, SAXException, IOException, NoSuchAlgorithmException {
 		this.mapName = map.getName().substring(0, map.getName().length() - 4);
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document doc = db.parse(map);
 		doc.getDocumentElement().normalize();
 		NodeList nodeLst = doc.getElementsByTagName("map");
 
 		Node n = nodeLst.item(0);
 		NamedNodeMap nl = n.getAttributes();
 		width = new Long(Integer.parseInt(nl.getNamedItem("width").getNodeValue()));
 		height = new Long(Integer.parseInt(nl.getNamedItem("height").getNodeValue()));
 
 		nodeLst = doc.getElementsByTagName("game");
 		n = nodeLst.item(0);
 		nl = n.getAttributes();
 		rounds = new Long(Integer.parseInt(nl.getNamedItem("rounds").getNodeValue()));
 		hash = BSUtil.bsHashDependency(map.getAbsolutePath());
 		invisible = false;
		size = SIZE.MEDIUM;
 	}
 	
 	/**
 	 * Manually specify necessary information
 	 * @param map
 	 * @param height
 	 * @param width
 	 * @param rounds
 	 * @param points
 	 */
 	public BSMap(String map, Long height, Long width, Long rounds) {
 		this.mapName = map;
 		this.height = height;
 		this.width = width;
 		this.rounds = rounds;
 	}
 	
 	@Override
 	public int hashCode() {
 		return id.intValue();
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof BSMap) {
 			BSMap m = (BSMap) o;
 			return id.equals(m.id);
 		}
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return mapName;
 	}
 }
