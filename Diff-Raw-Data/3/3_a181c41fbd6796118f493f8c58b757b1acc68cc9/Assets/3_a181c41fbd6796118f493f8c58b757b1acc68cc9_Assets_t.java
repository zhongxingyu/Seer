 package edu.rit.se.sse.rapdevx.api.dataclasses;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  * To and from JSON for the Assets.
  * 
  * @author Paul Cassidy
  * 
  */
 public class Assets {
 
 	private int					width;
 	private int					height;
 	private List<ShipClass>		ship_classes;
 	// needs to be changed to AssetImage once the object exists.
 	private List<AssetImage>	images;
 	private List<Ability>		abilities;
 	private static ObjectMapper		mapper	= new ObjectMapper();
 
 	public int getWidth() {
 		return width;
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public void setHeight(int height) {
 		this.height = height;
 	}
 
 	public List<ShipClass> getShipClasses() {
 		return ship_classes;
 	}
 
 	public void setShipClasses(List<ShipClass> shipClasses) {
 		this.ship_classes = shipClasses;
 	}
 
 	public List<AssetImage> getImages() {
 		return images;
 	}
 
 	public void setImages(List<AssetImage> images) {
 		this.images = images;
 	}
 
 	public List<Ability> getAbilities() {
 		return abilities;
 	}
 
 	public void setAbilities(List<Ability> abilities) {
 		this.abilities = abilities;
 	}
 
 	/**
 	 * Creates and mapps to an assets object.
 	 * 
 	 * @return The mapped Assets as an Assets object. or null if error.
 	 */
 	public static Assets fromJSON(String incomingJson) {
 
 		try {
 			Assets assets = mapper.readValue(incomingJson,
 					Assets.class);
 			return assets;
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 
 	}
 
 	/**
 	 * Creates a JSON file from an Assets object.
 	 * 
 	 * @param assets
 	 */
 	public void toJSON(Assets assets) {
 		try {
 			mapper.writeValue(new File("AssetsFromJava.json"), assets);
 		} catch (JsonGenerationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public Assets() {
 		ship_classes = new LinkedList<ShipClass>();
 		images = new LinkedList<AssetImage>();
 		abilities = new LinkedList<Ability>();
 	}
 
 	public static void main(String args[]) {
 		Assets assets = new Assets();
 		assets.width = 123;
 		assets.height = 321;
 		assets.ship_classes.add(new ShipClass());
 		assets.ship_classes.add(new ShipClass());
 		assets.ship_classes.add(new ShipClass());
 		assets.images.add(new AssetImage());
 		assets.images.add(new AssetImage());
 		assets.images.add(new AssetImage());
 		assets.toJSON(assets);
 	}
 
 }
