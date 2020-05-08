 package se2.e.engine3d;
 
 import se2.e.geometry.Geometry;
 import se2.e.geometry.GeometryObject;
 import se2.e.geometry.Track;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * The GeometryAndAppearanceLoader handles the loading of the geometry and appearance configurations.
  */
 public class GeometryAndAppearanceLoader {
 
 	/** The geometry objects. */
 	private HashMap<String, GeometryObject> geometryObjects;
 
 	/** The appearance information. */
 	private HashMap<String, Object> appearanceInfo;
 
 	/** The log. */
 	private Logger log = Logger.getLogger("GeometryAndAppearanceLoader");
 
 	/**
 	 * Instantiates a new geometry loader, loading the geometry and appearance configurations and connecting them with
 	 * each-other.
 	 * 
 	 * @param geometry the geometry
 	 * @param appearance the appearance
 	 */
 	public GeometryAndAppearanceLoader(Geometry geometry, Object appearance) {
 		// TODO: eventually fix appearance type
 		log.info("Loading geometry and appearance configurations...");
 		List<GeometryObject> geomObjs = geometry.getGeoObjects();
 		geometryObjects = new HashMap<String, GeometryObject>();
 		for (GeometryObject geomObj : geomObjs) {
 			if(geomObj instanceof Track)
 			{
				geometryObjects.put(((Track)geomObj).getName(), geomObj);
 				//TODO: Eventually maybe create a hashmap for each of the 2 types: Track/SimplePosition
 			}
 			//Fill in for all required geometry objects
 		}
 		log.info("Loaded geometry: " + geometryObjects);
 		// TODO: change class of appearance - do same thing
 
 	}
 
 	/**
 	 * Gets the geometry object with a given name.
 	 * 
 	 * @param label the label
 	 * @return the geometry object
 	 */
 	public GeometryObject getGeometryObject(String label) {
 		return geometryObjects.get(label);
 	}
 
 	/**
 	 * Gets the appearance info.
 	 * 
 	 * @param label the label
 	 * @return the appearance info
 	 */
 	public Object getAppearanceInfo(String label) {
 		return appearanceInfo.get(label);
 	}
 	
 	/**
 	 * Gets the geometry labels.
 	 *
 	 * @return the geometry labels
 	 */
 	public Set<String> getGeometryLabels(){
 		return geometryObjects.keySet();
 	}
 }
